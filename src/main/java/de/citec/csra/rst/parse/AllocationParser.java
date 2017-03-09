/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.rst.parse;

import de.citec.csra.rst.util.IntervalUtils;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Policy;
import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation.Priority;
import rst.timing.IntervalType.Interval;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class AllocationParser implements StringParser<ResourceAllocation> {

	@Override
	public ResourceAllocation getValue(String msc) throws IllegalArgumentException {
		String[] split = msc.split(",");

		StringBuilder tgt = new StringBuilder("");
		long duration = -1;
		Policy pol = null;
		Priority pri = null;

		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			try {
				duration = Long.valueOf(s);
				split[i] = null;
			} catch (NumberFormatException ex) {
			}
			try {
				pri = Priority.valueOf(s);
				split[i] = null;
			} catch (IllegalArgumentException ex) {
			}
			try {
				pol = Policy.valueOf(s);
				split[i] = null;
			} catch (IllegalArgumentException ex) {
			}
		}

		for (String s : split) {
			if (s != null) {
				tgt.append(s);
				tgt.append(",");
			}
		}

		if (duration == -1 || tgt.length() == 0 || pol == null || pri == null) {
			throw new IllegalArgumentException("must provide duration, policy, priority, and at least one resource id");
		}

		tgt.delete(tgt.length() - 1, tgt.length());

		Interval interval = IntervalUtils.buildRelativeRst(0, duration);

		return ResourceAllocation.newBuilder().
				addResourceIds(tgt.toString()).
				setPolicy(pol).
				setPriority(pri).
				setSlot(interval).buildPartial();
	}

	@Override
	public Class<ResourceAllocation> getTargetClass() {
		return ResourceAllocation.class;
	}

	@Override
	public String getString(ResourceAllocation obj) {
		StringBuilder bld = new StringBuilder();
		bld.append(String.valueOf(obj.getSlot().getEnd().getTime() - obj.getSlot().getBegin().getTime())).
				append(",").
				append(obj.getPriority()).
				append(",").
				append(obj.getPolicy());
		for (String resource : obj.getResourceIdsList()) {
			bld.append(",").
			append(resource);
		}
		return bld.toString();
	}
}
