#include <core.p4>
#include <v1model.p4>
#include "include/headers.p4"
#include "include/parser.p4"
#include "include/checksums.p4"
#include "include/definitions.p4"
#define MAX_PORTS 255


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
        action reply_to_read(bit<32> value) {
            hdr.data.type_sync = READ_REPLY;
            hdr.data.value = value;
            standard_metadata.egress_spec = standard_metadata.ingress_port;
        }

        table kv_store {
            key = {
                hdr.data.key1 : exact; /* Do an exact match on the key */
            }
            actions = {
                reply_to_read;
                _drop;
                NoAction;
            }
            size = 8192;
            default_action = NoAction();
        }




        apply {
            if (standard_metadata.ingress_port == CPU_PORT) {
                standard_metadata.egress_spec = hdr.packet_out.egress_port;
                hdr.packet_out.setInvalid();
                return;
            }
            else if(hdr.data.type_sync==READ){
                kv_store.apply();
                return;
            }
            else if(hdr.data.type_sync==WRITE){
                /* Send it to the local controller for rule insertion */
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


    apply {
        if (hdr.data.type_sync == READ_REPLY){
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
