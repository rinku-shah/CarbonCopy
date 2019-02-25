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

        apply {
            if (standard_metadata.ingress_port == CPU_PORT) {
            // Packet received from CPU_PORT, this is a packet-out sent by the controller. Set the egress port as requested by the controller (packet_out header) and remove the packet_out header.
            standard_metadata.egress_spec = hdr.packet_out.egress_port;
            hdr.packet_out.setInvalid();
            return;

            // gateway has to triggered for failover to switch packets from primary port to secondary port

            //definitions will now have SWITCH_OVER as 6

            // if primary has failed
            if (hdr.data.type_sync == SWITCH_OVER && hdr.udp.dstPort == PRIMARY_PORT) {
                    standard_metadata.egress_spec = SECONDARY_PORT;
                }

            }
            
            
        }
}

// ------------------------- EGRESS -----------------------------

control c_egress(inout headers hdr,
                 inout metadata meta,
                 inout standard_metadata_t standard_metadata) {


    apply {
        if (standard_metadata.ingress_port == CPU_PORT) {
            _drop();   // drop those packets from controller informing gateway to switch over to secondary switch    
        }
    }

}

// ----------------------- SWITCH -----------------------------

gateway(c_parser(),
         c_verify_checksum(),
         c_ingress(),
         c_egress(),
         c_compute_checksum(),
         c_deparser()) main;