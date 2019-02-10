#ifndef __PARSER__
#define __PARSER__
#include "definitions.p4"

/*************************************************************************
*********************** P A R S E R  ***********************************
*************************************************************************/

parser c_parser(packet_in packet,
                out headers hdr,
                inout metadata meta,
                inout standard_metadata_t standard_metadata) {

    state start {
        transition select(standard_metadata.ingress_port) {
            CPU_PORT: parse_packet_out;
            default: parse_ethernet;
        }
    }

    state parse_packet_out {
        packet.extract(hdr.packet_out);
        transition parse_ethernet;
    }

    state parse_ethernet {
        packet.extract(hdr.ethernet);
        transition select(hdr.ethernet.etherType) {
            TYPE_IPV4: parse_ipv4;
            // TYPE_VLAN : parse_vlan;
            default: accept;
        }
    }

    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        transition select(hdr.ipv4.protocol) {
            // PROTO_TCP: parse_tcp;
            PROTO_UDP: parse_udp;
            default: accept;
        }
    }

    state parse_udp{
        packet.extract(hdr.udp);
        transition select(hdr.udp.dstPort) {
            // if UDP_PORT_GTPU i.e. 2152 is present then parse gtpu header (on switch SGW and PGW)
            // UDP_PORT_GTPU : parse_gtpu;
            // else we are on DGW switch extract the data and take the decision accordingly
            default: parse_data;
        }
    }

    state parse_data{
        packet.extract(hdr.data);
        transition accept;
    }

}

/*************************************************************************
***********************  D E P A R S E R  *******************************
*************************************************************************/

control c_deparser(packet_out packet, in headers hdr) {
    apply {
        packet.emit(hdr.packet_in);
        packet.emit(hdr.ethernet);

        // packet.emit(hdr.gtpu_ipv4);
        // packet.emit(hdr.gtpu_udp);
        // packet.emit(hdr.gtpu);

        packet.emit(hdr.ipv4);
        // packet.emit(hdr.tcp);
        packet.emit(hdr.udp);

        packet.emit(hdr.data);
    }
}
#endif
