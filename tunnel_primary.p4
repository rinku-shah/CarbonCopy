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
            egressSpec_t port = 1;
            standard_metadata.egress_spec = port;
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
            default_action = NoAction();
        }



        apply {
            if (standard_metadata.ingress_port == CPU_PORT) {
            // Packet received from CPU_PORT, this is a packet-out sent by the controller. Set the egress port as requested by the controller (packet_out header) and remove the packet_out header.
            standard_metadata.egress_spec = hdr.packet_out.egress_port;
            hdr.packet_out.setInvalid();
            return;

            }
            else if(hdr.data.type_sync==READ){
                kv_store.apply();
                return;
            }
            else if(hdr.data.type_sync==WRITE){
                /* If this is primary switch, then packet has to be cloned */
                clone3(CloneType.I2E, I2E_CLONE_SESSION_ID, standard_metadata); /* Clone the packet */


                /* Send it to the local controller for rule insertion */
                standard_metadata.egress_spec = CPU_PORT;
                hdr.packet_in.setValid();
                hdr.packet_in.ingress_port = standard_metadata.ingress_port;
                return;
            }

        }
}

// ------------------------- EGRESS -----------------------------

control c_egress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {


    apply {
        if (IS_I2E_CLONE(standard_metadata)) {
            hdr.data.type_sync = WRITE_CLONE;
            egressSpec_t secondary_port = 2;
            standard_metadata.egress_spec = secondary_port;  /* Specify the port here */
            // standard_metadata.egress_spec = SECONDARY_PORT;  /* Specify the port here */
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
