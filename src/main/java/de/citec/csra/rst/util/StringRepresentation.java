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
package de.citec.csra.rst.util;

import rst.communicationpatterns.ResourceAllocationType.ResourceAllocation;
import rst.timing.IntervalType.Interval;

/**
 * Utility class that provides readable (short) string representations of 
 * {@link rst.communicationpatterns.ResourceAllocationType.ResourceAllocation} types.
 * @author pholthau
 */
public class StringRepresentation {

	/**
	 * Generates a short string representation of a single
	 * {@link rst.communicationpatterns.ResourceAllocationType.ResourceAllocation}.
	 * The generated string representation does not contain newlines, so it can be
	 * used for logging purposes. It also does not contain field names but only
	 * values in the following format: [id:state,priority,policy,initiator,interval;resource_ids].
	 * @param a The allocation that should be represented.
	 * @return a short string representation of the given allocation.
	 */
	public static String shortString(ResourceAllocation a) {
		if (a == null) {
			return "null";
		}
		StringBuilder b = new StringBuilder("[");
		b.append(a.getId()).append(":")
				.append(a.getState()).append(",")
				.append(a.getPriority()).append(",")
				.append(a.getPolicy()).append(",")
				.append(a.getInitiator()).append(",")
				.append(shortString(a.getSlot()));
		if (a.getResourceIdsCount() > 0) {
			b.append(";");
			for (String s : a.getResourceIdsList()) {
				b.append(s).append(",");
			}
			b.deleteCharAt(b.length() - 1);
		}
		b.append("]");
		return b.toString();
	}

	/**
	 * Generates a compact string representation of a single
	 * {@link rst.timing.IntervalType.Interval}.
	 * The generated string representation does not contain newlines, so it can be
	 * used for logging purposes. It only consists of the time stamps in parentheses
	 * connected with a dash, e.g., (1234)-(5678).
	 * @param i The interval that should be represented.
	 * @return a short string representation of the given interval.
	 */
	public static String shortString(Interval i) {
		if (i == null) {
			return "null";
		}
		long now = System.currentTimeMillis();
		StringBuilder b = new StringBuilder("(");
		b.append(i.getBegin().getTime() - now)
				.append(")-(")
				.append(i.getEnd().getTime() - now)
				.append(")");
		return b.toString();
	}
	
	/**
	 * Generates a compact string representation of any object.
	 * The generated string representation does not contain newlines, so it can be
	 * used for logging purposes. In particular, the following expression is used
	 * to generate the representation: {@code (o == null) ? "null" : o.toString().replaceAll("\n", " ");}
	 * @param o The object that should be represented.
	 * @return a short string representation of the given object.
	 */
	public static String shortString(Object o) {
		return (o == null) ? "null" : o.toString().replaceAll("\n", " ");
	}
}
