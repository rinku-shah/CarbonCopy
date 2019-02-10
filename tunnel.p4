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

        action _drop() {
            mark_to_drop();
        }

        action reply_to_read() {
            // hdr.data.value = something;
            hdr.data.type_sync = READ_REPLY;
        }

        apply {
            if(hdr.data.type_sync==READ){
                reply_to_read();
            }
            else if(hdr.data.type_sync==2){

            }
        }
}
