package org.onosproject.p4tutorial.mytunnel;

import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.ImmutableByteSequence;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.flow.criteria.MplsCriterion;
import org.onosproject.net.flow.criteria.VlanIdCriterion;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.ObjectiveError;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;

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
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.List;
import java.util.Arrays;

public class Rule_insertion{
	/* Ignoring logger service */

	/* Declare the variables inside the pipeconf file */

	public void populate_gf(ApplicationId appId,FlowRuleService flowRuleService,DeviceId switchId,String key, String value1, byte[] value2){
	PiTableId tunnelIngressTableId = PiTableId.of("c_ingress.gateway_forward");
    PiMatchFieldId keyID = PiMatchFieldId.of("hdr.data.type_sync");
		// byte[] MASK = new byte[] { (byte)0xff, (byte)0xff, (byte)0xff,
    // (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
    // (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff };
		byte[] key_byte = key.getBytes();

		PiCriterion match = PiCriterion.builder()
            .matchExact(keyID, key_byte)
            .build();

    PiActionId ingressActionId = PiActionId.of("c_ingress.myforward");
	byte[] value1_byte = value1.getBytes();
    PiActionParam valueParam1 = new PiActionParam(PiActionParamId.of("port"), value1_byte);
    byte[] value2_byte = value2;
    PiActionParam valueParam2 = new PiActionParam(PiActionParamId.of("dst_mac"), value2_byte);
    List<PiActionParam> list = Arrays.asList(valueParam1, valueParam2);

    PiAction action = PiAction.builder()
            .withId(ingressActionId)
            .withParameters(list)
            .build();

    insertPiFlowRule(appId,flowRuleService,switchId, tunnelIngressTableId, match, action);

	}


    private void insertPiFlowRule(ApplicationId appId,FlowRuleService flowRuleService,DeviceId switchId, PiTableId tableId,
                                  PiCriterion piCriterion, PiAction piAction) {
        FlowRule rule = DefaultFlowRule.builder()
                .forDevice(switchId)
                .forTable(tableId)
                .fromApp(appId)
                .withPriority(Constants.FLOW_RULE_PRIORITY)
                .makePermanent()
                .withSelector(DefaultTrafficSelector.builder()
                                      .matchPi(piCriterion).build())
                .withTreatment(DefaultTrafficTreatment.builder()
                                       .piTableAction(piAction).build())
                .build();
        flowRuleService.applyFlowRules(rule);
    }



}
