#include <core.p4>
#include <v1model.p4>
#include "include/headers.p4"
#include "include/parser.p4"
#include "include/checksums.p4"
#include "include/definitions.p4"
#define MAX_PORTS 255

control c_ingress(inout headers hdr,
                  inout metadata meta,
                  inout standard_metadata_t standard_metadata) {


        action send_to_cpu() {
            standard_metadata.egress_spec = CPU_PORT;
            // Packets sent to the controller needs to be prepended with the
            // packet-in header. By setting it valid we make sure it will be
            // deparsed on the wire (see c_deparser).
            hdr.packet_in.setValid();
            hdr.packet_in.ingress_port = standard_metadata.ingress_port;
        }

        action _drop() {
            mark_to_drop();
        }



        action reply_to_read() {
            // hdr.data.value = something;
            hdr.data.type_sync = READ_REPLY;
        }

        table 

        apply {
            if (standard_metadata.ingress_port == CPU_PORT) {
            // Packet received from CPU_PORT, this is a packet-out sent by the
            // controller. Skip table processing, set the egress port as
            // requested by the controller (packet_out header) and remove the
            // packet_out header.
            standard_metadata.egress_spec = hdr.packet_out.egress_port;
            // hdr.ipv4.ttl = hdr.ipv4.ttl - 1;
            hdr.packet_out.setInvalid();
            return;

            }
            else if(hdr.data.type_sync==READ){
                read_table.apply();
                return;
            }
            else if(hdr.data.type_sync==READ_REPLY){

            }
            else if(hdr.data.type_sync==WRITE){

            }
            else if(hdr.data.type_sync==WRITE_REPLY){

            }
            else if(hdr.data.type_sync==WRITE_CLONE){

            }
            else if(hdr.data.type_sync==WRITE_CLONE_REPLY){

            }
        }
}
