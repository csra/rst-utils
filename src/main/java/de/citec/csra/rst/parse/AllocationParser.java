/* 
 * Copyright (C) 2017 Patrick Holthaus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
@Deprecated
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
