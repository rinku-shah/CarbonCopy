/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package org.onosproject.p4tutorial.mytunnel;


 import org.onlab.packet.Ethernet;
 import org.onlab.packet.IPacket;
 import org.onlab.packet.UDP;
 import org.onlab.packet.IPv4;
 import org.onlab.packet.Ip4Prefix;
 import org.onlab.packet.IpPrefix;

 import org.onlab.packet.Ip4Address;
 import org.onlab.packet.TCP;
 import org.onlab.packet.TpPort;
 import org.onlab.packet.UDP;
 import org.onlab.packet.MacAddress;
 import com.google.common.collect.Lists;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.ReferenceCardinality;
 import org.apache.felix.scr.annotations.Service;
 import org.onlab.packet.IpAddress;
 import org.onosproject.core.ApplicationId;
 import org.onosproject.core.CoreService;
 import org.onosproject.net.DeviceId;
 import org.onosproject.net.Host;
 import org.onosproject.net.Link;
 import org.onosproject.net.Path;
 import org.onosproject.net.PortNumber;
 import org.onosproject.net.flow.DefaultFlowRule;
 import org.onosproject.net.flow.DefaultTrafficSelector;
 import org.onosproject.net.flow.DefaultTrafficTreatment;
 import org.onosproject.net.flow.FlowRule;
 import org.onosproject.net.HostId;
 import org.onosproject.net.flow.FlowRuleService;
 import org.onosproject.net.flow.criteria.PiCriterion;
 import org.onosproject.net.host.HostEvent;
 import org.onosproject.net.host.HostListener;
 import org.onosproject.net.host.HostService;
 import org.onosproject.net.packet.InboundPacket;
 import org.onosproject.net.packet.PacketContext;
 import org.onosproject.net.packet.PacketPriority;
 import org.onosproject.net.packet.PacketProcessor;
 import org.onosproject.net.packet.PacketService;
 import org.onosproject.net.pi.model.PiActionId;
 import org.onosproject.net.pi.model.PiActionParamId;
 import org.onosproject.net.pi.model.PiMatchFieldId;
 import org.onosproject.net.pi.model.PiTableId;
 import org.onosproject.net.pi.runtime.PiAction;
 import org.onosproject.net.pi.runtime.PiActionParam;
 import org.onosproject.net.topology.Topology;
 import org.onosproject.net.topology.TopologyService;
 import org.onosproject.net.flow.TrafficSelector;
 import org.onosproject.net.ConnectPoint;
 import org.onlab.packet.Data;
 import org.onosproject.net.packet.DefaultOutboundPacket;
 import org.onosproject.net.packet.OutboundPacket;
 import org.onosproject.net.packet.PacketService;
 import org.onosproject.net.flow.DefaultTrafficTreatment;
 import org.onosproject.net.flow.TrafficTreatment;

 import org.slf4j.Logger;

 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.nio.charset.Charset;
 import java.util.concurrent.atomic.AtomicInteger;

 import java.nio.charset.StandardCharsets;



 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;


 import static org.slf4j.LoggerFactory.getLogger;

/**
 * MyTunnel application which provides forwarding between each pair of hosts via
 * MyTunnel protocol as defined in mytunnel.p4.
 * <p>
 * The app works by listening for host events. Each time a new host is
 * discovered, it provisions a tunnel between that host and all the others.
 */
@Component(immediate = true)
public class MyTunnelApp {

    private static final String APP_NAME = "org.onosproject.p4tutorial.mytunnel";

    // Default priority used for flow rules installed by this app.
    private static final int FLOW_RULE_PRIORITY = 100;

    // private final HostListener hostListener = new InternalHostListener();
    private ApplicationId appId;
    // private AtomicInteger nextTunnelId = new AtomicInteger();

    private static final Logger log = getLogger(MyTunnelApp.class);



    //--------------------------------------------------------------------------
    // ONOS core services needed by this application.
    //--------------------------------------------------------------------------

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private TopologyService topologyService;

    // @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    // private HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private int counter = 0;
    


    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    @Activate
    public void activate() {
        // Register app and event listeners.

        log.info("Starting...");

        appId = coreService.registerApplication(APP_NAME);
        packetService.addProcessor(processor, PacketProcessor.director(2));

        requestIntercepts();


        // hostService.addListener(hostListener);
        log.info("STARTED", appId.id());
    }

    @Deactivate
    public void deactivate() {
        // Remove listeners and clean-up flow rules.
        log.info("Stopping...");
        withdrawIntercepts();

        // hostService.removeListener(hostListener);
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        log.info("STOPPED");
    }


    /**
     * Request packet in via packet service.
     */

    Rule_insertion RI = new Rule_insertion();
    DeviceId deviceId = org.onosproject.net.DeviceId.deviceId("device:bmv2:s3");

    private void requestIntercepts() {

        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);

        byte [] prim_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163ea84e01L).array();
        byte [] lg_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163e7867d6L).array();
        

        // String primary = new String(prim_mac, StandardCharsets.UTF_8);
        // String load_gen = new String(lg_mac, StandardCharsets.UTF_8);
        // String secondary = new String(sec_mac, StandardCharsets.UTF_8);

        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(6),Integer.toString(2),prim_mac);
        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(2),Integer.toString(2),prim_mac);
        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(1),Integer.toString(1),lg_mac);
        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(3),Integer.toString(1),lg_mac);

        // RI.populate_gf(appId,flowRuleService,deviceId,"0","3",secondary);
        // RI.populate_gf(appId,flowRuleService,deviceId,"2","3",secondary);
        // RI.populate_gf(appId,flowRuleService,deviceId,"1","1",load_gen);
        // RI.populate_gf(appId,flowRuleService,deviceId,"3","1",load_gen);

        // /* PacketPriority.REACTIVE = packets are only sent to the
        // controller if they fail to match any of the rules installed in the switch.  */
        // /* PacketPriority.CONTROL = High priority for control traffic this will result in all
        // traffic matching the selector to be sent to controller */
        // int packet_type = Constants.WRITE;
        // int PORTMASK = 0xff;
        // PiMatchFieldId packetType = PiMatchFieldId.of("hdr.data.type_sync");
        // // PiCriterion match = PiCriterion.builder()
        // //         .matchTernary(packetType, packet_type,PORTMASK)
        // //         .build();
        // PiCriterion match = PiCriterion.builder()
        //         .matchExact(packetType, packet_type)
        //         .build();
        // packetService.requestPackets(selector.matchPi(match).build(), PacketPriority.CONTROL, appId);

    }


    /**
     * Cancel request for packet in via packet service.
     */
    private void withdrawIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
    }

    /**
     * Packet processor responsible for forwarding packets along their paths.
     */
    private class ReactivePacketProcessor implements PacketProcessor {

      // Rule_insertion RI = new Rule_insertion();

        @Override
        public void process(PacketContext context) {
            // Stop processing if the packet has been handled, since we
            // can't do any more to it.

            if (context.isHandled()) {
               log.info("context is already handled");
                return;
            }

            log.info("Got the Pcaket");

            InboundPacket pkt = context.inPacket();
            ConnectPoint connectPoint = pkt.receivedFrom();
            DeviceId deviceId = pkt.receivedFrom().deviceId();
            if(Constants.DEBUG) {
                log.warn("Packet received from {}", connectPoint);
                log.warn("Device ID {}", deviceId);
                log.warn("Packet details {}", pkt);
            }
            // parse the incoming packet as Ethernet frame
            Ethernet ethPkt = pkt.parsed();

            // Bail if this is deemed to be a control packet.
            if (isControlPacket(ethPkt)) {
                return;
            }

            HostId id = HostId.hostId(ethPkt.getDestinationMAC());

            // Do not process LLDP MAC address in any way.
            if (id.mac().isLldp()) {
                return;
            }

            // Do not process IPv4 multicast packets, let mfwd handle them
            if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                if (id.mac().isMulticast()) {
                    return;
                }
            }
            // Filter out ARP packets
            if (ethPkt.getEtherType() == Ethernet.TYPE_ARP) {
                return;
            }

            /****************   PARSE Ethernet srcMAC and dstMAC ***************************/
            MacAddress srcMac = ethPkt.getSourceMAC();
            MacAddress dstMac = ethPkt.getDestinationMAC();
            //            log.warn("srcMACAddrestype = {}",ethPkt.getSourceMAC().getClass().getName());
            if(Constants.DEBUG) {
                log.warn("srcMACAddres = {}", srcMac);
                log.warn("dstMACAddres = {}", dstMac);
            }
           log.warn("srcMACAddres = {}",dstMac.getClass().getName());     // gives org.onlab.packet.MacAddress


            //parse the incoming packet as IP packet
            IPacket ipPkt = pkt.parsed();
            // ipheader will have IPv4 header
            IPacket ipheader = ipPkt.getPayload();
            // tcp_udp_header will have tcp/udp header
            IPacket tcp_udp_header = ipheader.getPayload();
            // final_payload will have actual payload of the packet
            IPv4 tmp_ipv4Packet = (IPv4) ipPkt.getPayload();
            int srcAddress = tmp_ipv4Packet.getSourceAddress();
            String srcIPAddr = tmp_ipv4Packet.fromIPv4Address(srcAddress);

            byte protocol = tmp_ipv4Packet.getProtocol();
            IPacket final_payload = tcp_udp_header.getPayload(); 
            if (protocol == IPv4.PROTOCOL_UDP) {
                if(Constants.DEBUG){
                    log.info("received non-UDP packet. Returning ");
                    }
                
            }
            else {
                return;
            }            
            

            byte ipv4Protocol=IPv4.PROTOCOL_UDP;
            int ipv4SourceAddress = 0;
            int ipv4DstAddress = 0;
            int udp_srcport=0;
            int udp_dstport=0;
            int tcp_srcport=0;
            int tcp_dstport=0;
            String DGW_IPAddr = "";
            int sgw_teid = 0;
            int outPort = 0;

            /****************   PARSE Ipv4 SrcIP and DstIP ***************************/

            if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4Packet = (IPv4) ipPkt.getPayload();
                ipv4Protocol = ipv4Packet.getProtocol();
                ipv4SourceAddress = ipv4Packet.getSourceAddress();
                ipv4DstAddress = ipv4Packet.getDestinationAddress();
                Ip4Prefix matchIp4SrcPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                                          Ip4Prefix.MAX_MASK_LENGTH);
                Ip4Prefix matchIp4DstPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                                          Ip4Prefix.MAX_MASK_LENGTH);

                 DGW_IPAddr = ipv4Packet.fromIPv4Address(ipv4SourceAddress);  // returns string IP of DGW_IPAddr

                //    log.warn("ipv4srcAddres = {}", matchIp4SrcPrefix);
                //    log.warn("ipv4dstAddres = {}", matchIp4DstPrefix);



//            log.warn("ipv4srcAddrestype = {}",matchIp4SrcPrefix.getClass().getName());  // gives org.onlab.packet.Ip4Prefix
                if(Constants.DEBUG) {
                   log.warn("ipv4srcAddres = {}", matchIp4SrcPrefix);
                   log.warn("ipv4dstAddres = {}", matchIp4DstPrefix);
//                    log.warn("ipv4Packet class = {}",ipv4Packet.getClass().getName());
//                    log.warn("ipv4SourceAddress value = {}",ipv4SourceAddress);
//                    log.warn("dstip clas = {}",dstip.getClass().getName());
                }


                /****************   PARSE TCP SrcPort and DstPort ***************************/

                if (ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv4Packet.getPayload();
                    tcp_srcport = tcpPacket.getSourcePort();
                    tcp_dstport = tcpPacket.getDestinationPort();
                    if (Constants.DEBUG) {
                        log.warn("TCP srcPort = {}", tcp_srcport);
                        log.warn("TCP dstPort = {}", tcp_dstport);
                    }
                }
                /****************   PARSE UDP SrcPort and DstPort ***************************/

                if (ipv4Protocol == IPv4.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv4Packet.getPayload();
                    udp_srcport = udpPacket.getSourcePort();
                    udp_dstport = udpPacket.getDestinationPort();
                    if(Constants.DEBUG) {
                        log.warn("UDP srcPort = {}", udp_srcport);
                        log.warn("UDP dstPort = {}", udp_dstport);
                    }
                }
                if (ipv4Protocol == IPv4.PROTOCOL_ICMP) {
                    if(Constants.DEBUG){
                        log.info("received ICMP packet returning ");
                        }
                        return;
                }
            }
        
            if (!srcIPAddr.equals("192.168.100.100")) {
                String payload;
                if(Constants.BITWISE_DEBUG){
                    log.warn("payload direct = {}",((Data)final_payload).getData());
                }
                byte[] p =((Data)final_payload).getData();

                byte [] b1 = Arrays.copyOfRange(p, 0, 1); //code
                byte [] b2 = Arrays.copyOfRange(p, 1, 17); //key
                byte [] b3 = Arrays.copyOfRange(p, 17, 33); //value
                byte [] b4 = Arrays.copyOfRange(p, 33, 34); //value
                

                byte code = ByteBuffer.wrap(b1).get();
                int type = code;
                String key1 = new String(b2, StandardCharsets.UTF_8); //16 byte
                      String value = new String(b3, StandardCharsets.UTF_8); //16 byte


                ByteBuffer bb = ByteBuffer.wrap(((Data)final_payload).getData());
                // int first = bb.getShort(); //pull off a 16 bit short (1, 5)
                // int second = bb.get(); //pull off the next byte (5)
                // log.warn("msg id  = {}",second);
                // String third = Integer.toString(bb.getInt()); //pull off the next 32 bit int (0, 1, 0, 5)
                // log.warn("sep = {}",third);
                // System.out.println(first + " " + second + " " + third);
                payload = new String((((Data)final_payload).getData()),  Charset.forName("UTF-8"));
                if (ethPkt == null) {
                    return;
                }
                else{
    //                log.warn("Packet contains !!!!! {}",ethPkt.toString());
                    // if(Constants.DEBUG) {
                    if(Constants.DEBUG) {
                        log.warn(" {}", ipheader);
                        log.warn(" {}", tcp_udp_header);
                        log.warn("Packet payload = {}",payload);

                    }
                    // returns org.onlab.packet.Data as type of final_payload
                    // final_payload is ASCII encoded bytearray
    //                log.warn("Packet contains ????? {}",final_payload.getClass().getName());    // gives org.onlab.packet.Data
                }

                String response;
                byte [] sec_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163e0c3710L).array();
                byte [] lg_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163e7867d6L).array();
                if(type == Constants.SWO){
                    if (Constants.DEBUG) {
                        log.warn("================= Inserting NEW entry ==============");
                    }
                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(6),Integer.toString(2),sec_mac);
                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(2),Integer.toString(2),sec_mac);
                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(1),Integer.toString(1),lg_mac);
                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(3),Integer.toString(1),lg_mac);
                  // build_response_pkt(connectPoint,srcMac,dstMac,ipv4Protocol,ipv4SourceAddress,udp_dstport,udp_srcport,response);
                }

            }
            else {
                    if(Constants.DEBUG){
                        log.info("received non-UDP packet. Returning ");
                    }
                return;
            }
        }


        // private void build_response_pkt(ConnectPoint connectPoint,MacAddress srcMac,MacAddress dstMac,byte ipv4Protocol,int ipv4SourceAddress,int udp_dstport,int udp_srcport,String response){
        //   log.warn("Here .....");
        //     Data payload_data = new Data();
        //     payload_data.setData(response.toString().getBytes());
        //     UDP udp = new UDP();
        //     udp.setSourcePort(udp_dstport);
        //     udp.setDestinationPort(udp_srcport);
        //     udp.setPayload(payload_data);

        //     IPv4 ip_pkt = new IPv4();
        //     byte ttl = 64;
        //     ip_pkt.setDestinationAddress(ipv4SourceAddress);
        //     ip_pkt.setSourceAddress(Constants.CONTROLLER_IP);   // controller IP is hardcoded in Constants.java file
        //     ip_pkt.setProtocol(ipv4Protocol);   //assuming that pacet will always be UDP
        //     ip_pkt.setTtl(ttl);
        //     ip_pkt.setPayload(udp);


        //     if(Constants.DEBUG){
        //         log.warn("sending payload as = {}",response);
        //         log.warn("Sending IP header as  : {}",ip_pkt);
        //     }

        //     Ethernet ethernet = new Ethernet();
        //     ethernet.setEtherType(Ethernet.TYPE_IPV4)
        //             .setDestinationMACAddress(srcMac)
        //             .setSourceMACAddress(dstMac)
        //             .setPayload(ip_pkt);
        //             if(Constants.DEBUG){
        //                 log.warn("1 sending payload as = {}",response);
        //                 log.warn("1 Sending IP header as  : {}",ip_pkt);
        //             }

        //     TrafficTreatment treatment = DefaultTrafficTreatment.builder()
        //             .setOutput(connectPoint.port())
        //             .build();
        //             if(Constants.DEBUG){
        //                 log.warn("2 sending payload as = {}",response);
        //                 log.warn("2 Sending IP header as  : {}",ip_pkt);
        //             }
        //     OutboundPacket outboundPacket =
        //             new DefaultOutboundPacket(connectPoint.deviceId(), treatment,
        //                                       ByteBuffer.wrap(ethernet.serialize()));
        //     if(Constants.DEBUG) {
        //       log.warn("Processing outbound packet: {}", outboundPacket);
        //         log.warn("Ethernet packet: {}", ethernet);
        //     }

        //     packetService.emit(outboundPacket);

        // }

        // Indicates whether this is a control packet, e.g. LLDP, BDDP
        private boolean isControlPacket(Ethernet eth) {
            short type = eth.getEtherType();
            return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
        }
    }
}