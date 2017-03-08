/*
 * Copyright (C) 2017 pholthau
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

import com.google.protobuf.ByteString;
import rsb.InitializeException;
import rst.communicationpatterns.TaskStateType.TaskState;
import rst.communicationpatterns.TaskStateType.TaskState.State;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class TaskStateUtils {

	public static TaskState build(String state) {
		return build(State.valueOf(state));
	}

	public static TaskState build(State state) {
		return build(state, SerializationService.EMPTY, SerializationService.UTF8);
	}

	public static TaskState buildFrom(String state, TaskState original) {
		return build(State.valueOf(state), original.getPayload(), original.getWireSchema());
	}

	public static TaskState buildFrom(State state, TaskState original) {
		return build(state, original.getPayload(), original.getWireSchema());
	}

	public static TaskState build(String state, Object payload) throws InitializeException {
		return build(State.valueOf(state), payload);
	}

	public static TaskState build(State state, Object payload) throws InitializeException {
		if (payload == null) {
			return build(state);
		} else {
			SerializationService s = new SerializationService(payload.getClass());
			return build(state, s.serialize(payload), s.getSchema());
		}
	}

	public static TaskState build(String state, ByteString payload, ByteString wireschema) {
		return build(State.valueOf(state), payload, wireschema);
	}

	public static TaskState build(State state, ByteString payload, ByteString wireschema) {
		TaskState.Builder taskBuilder = TaskState.newBuilder();

		int serial = 1;
		switch (state) {
			case ABORT:
				serial++;
			case INITIATED:
				taskBuilder.setOrigin(TaskState.Origin.SUBMITTER);
				taskBuilder.setSerial(serial);
				break;
			case COMPLETED:
				serial++;
			case FAILED:
				serial++;
			case REJECTED:
			case ACCEPTED:
			case ABORTED:
			case UPDATE:
				serial++;
				taskBuilder.setOrigin(TaskState.Origin.HANDLER);
				taskBuilder.setSerial(serial);
				break;
		}
		taskBuilder.setState(state);

		taskBuilder.setPayload(payload);
		taskBuilder.setWireSchema(wireschema);

		return taskBuilder.build();
	}

}
