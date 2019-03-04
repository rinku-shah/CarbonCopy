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

        action _drop() {
            mark_to_drop();
        }

        action myforward(egressSpec_t port, bit<48> dst_mac) {
          hdr.ethernet.dstAddr = dst_mac;
          standard_metadata.egress_spec = port;

        }

        table gateway_forward {

            key = {
                hdr.data.type_sync : exact; /* Do an exact match on the type */
            }
            actions = {
                myforward;
                _drop;
                NoAction;
            }
            default_action = NoAction();
            // counters = kv_store_counter;
        }

        apply {
              
              gateway_forward.apply();
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
       
    }

}

// ----------------------- SWITCH -----------------------------

V1Switch(c_parser(),
         c_verify_checksum(),
         c_ingress(),
         c_egress(),
         c_compute_checksum(),
         c_deparser()) main;