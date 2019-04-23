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

public class Rule_insertion2{
	/* Ignoring logger service */

	/* Declare the variables inside the pipeconf file */

	public boolean populate_kv_store(ApplicationId appId,FlowRuleService flowRuleService,DeviceId switchId,byte[] key_byte, byte [] value_byte){
	boolean is_inserted = false;
	PiTableId tunnelIngressTableId = PiTableId.of("c_ingress.kv_store");
    PiMatchFieldId keyID = PiMatchFieldId.of("hdr.data.key1");
		// byte[] MASK = new byte[] { (byte)0xff, (byte)0xff, (byte)0xff,
    // (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
    // (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff };
		// byte[] key_byte = key.getBytes();

		PiCriterion match = PiCriterion.builder()
            .matchExact(keyID, key_byte)
            .build();

    PiActionId ingressActionId = PiActionId.of("c_ingress.reply_to_read");
		// byte[] value_byte = value.getBytes();
    PiActionParam valueParam = new PiActionParam(PiActionParamId.of("value"), value_byte);

    PiAction action = PiAction.builder()
            .withId(ingressActionId)
            .withParameter(valueParam)
            .build();

    FlowRule ins_rule = insertPiFlowRule(appId,flowRuleService,switchId, tunnelIngressTableId, match, action);

		// Thread.sleep(1000);
		while(true){
			Iterable<FlowRule> rules = flowRuleService.getFlowRulesById(appId);
	    for (FlowRule f : rules) {
				boolean equality = ins_rule.equals(f);
				if (equality == true) {
					is_inserted = true;
					break;
					}
			}
			if(is_inserted)	break;	
		}

		return is_inserted;
		// return true;
  }


    private FlowRule insertPiFlowRule(ApplicationId appId,FlowRuleService flowRuleService,DeviceId switchId, PiTableId tableId,
                                  PiCriterion piCriterion, PiAction piAction) {
// makeTemporary(int timeout)
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
	return rule;
    }



}
