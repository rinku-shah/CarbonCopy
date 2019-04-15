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

import org.onosproject.net.Device;
import org.onosproject.net.Port;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkProvider;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;

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
public class MyTunnelApp extends AbstractProvider implements LinkProvider {

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
	protected LinkProviderRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;


    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    // private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private int counter = 0;

    private LinkProviderService providerService;
    private DeviceListener deviceListener = new InternalDeviceListener();
	private LinkListener linkListener = new InternalLinkListener();

	private int num_devices = 3;
	private boolean [] dev = new boolean[num_devices+1];
	DeviceId [] devIds = new DeviceId[num_devices+1];

    public MyTunnelApp() {
        super(new ProviderId("mytunnel", "org.onosproject.p4tutorial.mytunnel"));
	}



    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    @Activate
    public void activate() {
        // Register app and event listeners.

        log.info("============= Starting... =====================");

        appId = coreService.registerApplication(APP_NAME);
        // packetService.addProcessor(processor, PacketProcessor.director(2));
        deviceService.addListener(deviceListener);
		linkService.addListener(linkListener);
		providerService = registry.register(this);
        for (int i=1; i <=3; i++)
        {
        	devIds[i] = org.onosproject.net.DeviceId.deviceId("device:bmv2:s"+Integer.toString(i));
        	dev[i] = false;
        }
	    requestIntercepts();


        // hostService.addListener(hostListener);
        log.info("============ STARTED ===========================", appId.id());
    }

    @Deactivate
    public void deactivate() {
        // Remove listeners and clean-up flow rules.
        deviceService.removeListener(deviceListener);
		linkService.removeListener(linkListener);
		registry.unregister(this);
        log.info("Stopping...");
        withdrawIntercepts();

        // hostService.removeListener(hostListener);
        flowRuleService.removeFlowRulesById(appId);
        // packetService.removeProcessor(processor);
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
    }


    /**
     * Cancel request for packet in via packet service.
     */
    private void withdrawIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
    }


	private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            Device device = event.subject();
            DeviceId devId = device.id();
            // log.warn("=========== Type : " + type.toString() + " " + device.toString() + " ============");
            if (type == DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED || type == DeviceEvent.Type.DEVICE_REMOVED) {
                // processDeviceLinks(device);
                log.warn("--------- Device removed -----------");
                for (int i=1;i<=3; i++) {
                	if (devId.equals(devIds[i])) {
                		if (dev[i] == true) {
                			log.info("***************** Device Removed : " + devId.toString() + " ******************");
                			dev[i] = false;
                			if (i==1) {
                				byte [] sec_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163e4276b4L).array();
                				byte [] lg_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163ef1c877L).array();
                				RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(6),Integer.toString(3),sec_mac);
			                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(2),Integer.toString(3),sec_mac);
			                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(1),Integer.toString(1),lg_mac);
			                    RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(3),Integer.toString(1),lg_mac);
			                    log.info("============== Switching over to secondary ===============================");
                			}
                		}
                	}
                }

            }
            else if (type == DeviceEvent.Type.DEVICE_ADDED) {
                // processPortLinks(device, event.port());
                log.info("------------- Device added -------------");
                for (int i=1;i<=3; i++) {
                	if (devId.equals(devIds[i])) {
                		dev[i] = true;
                		log.info("***************** Device Added : " + devId.toString() + " **********************");
                		if (i==3) {
                			byte [] prim_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163ebcc799L).array();
					        byte [] lg_mac = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(0x00163ef1c877L).array();

					        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(6),Integer.toString(2),prim_mac);
					        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(2),Integer.toString(2),prim_mac);
					        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(1),Integer.toString(1),lg_mac);
					        RI.populate_gf(appId,flowRuleService,deviceId,Integer.toString(3),Integer.toString(1),lg_mac);

					        log.info("=============== Primary table entries pushed ======================");
                		}
                	}
                }

            }
        }
 	}

 	//Listens to link events and processes the link additions.
    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            if (event.type() == LinkEvent.Type.LINK_ADDED) {
                Link link = event.subject();
                if (link.providerId().scheme().equals("cfg")) {
                    // processLink(event.subject());
                }
            }
        }
	}

}
