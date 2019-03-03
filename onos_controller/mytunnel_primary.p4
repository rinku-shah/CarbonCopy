#include <core.p4>
#include <v1model.p4>
#include "include/headers.p4"
#include "include/parser.p4"
#include "include/checksums.p4"
#include "include/definitions.p4"
#define MAX_PORTS 255


#define IS_I2E_CLONE(std_meta) (std_meta.instance_type == BMV2_V1MODEL_INSTANCE_TYPE_INGRESS_CLONE)


// ---------------- INGRESS ---------------------------------------

control c_ingress(inout headers hdr,
                  inout metadata meta,
                  inout standard_metadata_t standard_metadata) {


        counter(MAX_PORTS, CounterType.packets_and_bytes) tx_port_counter;
        counter(MAX_PORTS, CounterType.packets_and_bytes) rx_port_counter;

        action ipv4_forward(egressSpec_t port) {

            standard_metadata.egress_spec = port;

        }


        action send_to_cpu() {
            standard_metadata.egress_spec = CPU_PORT;
            // Packets sent to the controller needs to be prepended with the packet-in header. By setting it valid we make sure it will be deparsed on the wire (see c_deparser).
            hdr.packet_in.setValid();
            hdr.packet_in.ingress_port = standard_metadata.ingress_port;
        }

        action _drop() {
            mark_to_drop();
        }


        /* Take the value from the key value container pushed table */
        action reply_to_read(bit<128> value) {
            hdr.data.type_sync = READ_REPLY;
            hdr.data.value = value;
            standard_metadata.egress_spec = standard_metadata.ingress_port;
        }

        // direct_counter(CounterType.packets_and_bytes) kv_store_counter;


        table kv_store {
            key = {
                hdr.data.key1 : exact; /* Do an exact match on the key */
            }
            actions = {
                reply_to_read;
                _drop;
                NoAction;
            }
            default_action = NoAction();
            // counters = kv_store_counter;
        }



        apply {
            if (standard_metadata.ingress_port == CPU_PORT) {
            // Packet received from CPU_PORT, this is a packet-out sent by the controller. Set the egress port as requested by the controller (packet_out header) and remove the packet_out header.
            standard_metadata.egress_spec = hdr.packet_out.egress_port;
            hdr.packet_out.setInvalid();
            return;

            }
            else if(hdr.data.type_sync==READ){
                if(kv_store.apply().hit){
                    return;
                }
                else{
                    // Code for READ NOT FOUND
                }
            }
            else if(hdr.data.type_sync==WRITE){
                /* If this is primary switch, then packet has to be cloned */
                egressSpec_t secondary_port = 2;
                standard_metadata.egress_spec = secondary_port;  /* Specify the port here */
                clone3(CloneType.I2E, I2E_CLONE_SESSION_ID, standard_metadata); /* Clone the packet */


                /* Send it to the local controller for rule insertion */
                // egressSpec_t port = 3;
                // standard_metadata.egress_spec = port;
                standard_metadata.egress_spec = CPU_PORT;
                hdr.packet_in.setValid();
                hdr.packet_in.ingress_port = standard_metadata.ingress_port;
                return;
            }
                         // Update port counters at index = ingress or egress port.
             if (standard_metadata.egress_spec < MAX_PORTS) {
                 tx_port_counter.count((bit<32>) standard_metadata.egress_spec);
             }
             if (standard_metadata.ingress_port < MAX_PORTS) {
                 rx_port_counter.count((bit<32>) standard_metadata.ingress_port);
             }

        }
}

// ------------------------- EGRESS -----------------------------


control c_egress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {

        direct_counter(CounterType.packets_and_bytes) dummy_counter;
        table dummy {
            key = {
                hdr.data.key1 : exact; /* Do an exact match on the key */
            }
            actions = {
                NoAction;
            }
            counters = dummy_counter;
        }


    apply {
        if (IS_I2E_CLONE(standard_metadata)) {
            dummy.apply();
            hdr.data.type_sync = WRITE_CLONE;
            hdr.ipv4.srcAddr = hdr.ipv4.dstAddr;
            hdr.ethernet.srcAddr = hdr.ethernet.dstAddr;
            hdr.ethernet.dstAddr = sec_mac;
            hdr.ipv4.dstAddr = sec_ipaddr;
            
        }
        else if (hdr.data.type_sync == READ_REPLY){
            macAddr_t tempMac;
            tempMac = hdr.ethernet.srcAddr;
            hdr.ethernet.srcAddr = hdr.ethernet.dstAddr;
            hdr.ethernet.dstAddr = tempMac;

            ip4Addr_t tempip4;
            tempip4 = hdr.ipv4.srcAddr;
            hdr.ipv4.srcAddr = hdr.ipv4.dstAddr;
            hdr.ipv4.dstAddr = tempip4;
        }
    }

}

// ----------------------- SWITCH -----------------------------

V1Switch(c_parser(),
         c_verify_checksum(),
         c_ingress(),
         c_egress(),
         c_compute_checksum(),
         c_deparser()) main;
