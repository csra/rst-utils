/*
 * Copyright (C) 2017 Bielefeld University, nkoester
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
import rst.generic.KeyValuePairType.KeyValuePair;
import rst.generic.ValueType;
import rst.generic.ValueType.Value;
import static rst.generic.ValueType.Value.Type.*;

/**
 *
 * @author nkoester
 */
public class GenericsUtils {

    private static final KeyValuePair.Builder KVpairBuilder = KeyValuePair.newBuilder();

    /**
     * Creates a KeyValuePair for any given String and object. Object is casted to the 
     * according type available in RST if possible. UnknownTypeException is raised 
     * otherwise. Only support base types.
     * 
     * @param key The key for the KVP
     * @param value the target value for the KVP
     * @return 
     */
    public static KeyValuePair getKeyValuePair(String key, Object value) throws UnknownTypeException {
        KVpairBuilder.clear();
        KVpairBuilder.setKey(key);
        ValueType.Value.Builder valueBuilder = KVpairBuilder.getValueBuilder();

        if (value == null) {
            valueBuilder.setType(VOID);

        } else if (value instanceof String) {
            valueBuilder.setType(STRING).setString((String) value);

        } else if (value instanceof Double || value instanceof Float) {
            valueBuilder.setType(DOUBLE).setDouble((double) value);

        } else if (value instanceof Integer) {
            valueBuilder.setType(INT).setInt((int) value);

        } else if (value instanceof Long) {
            valueBuilder.setType(INT).setInt((int) ((long) value));

        } else if (value instanceof Boolean) {
            valueBuilder.setType(BOOL).setBool((boolean) value);

        } else if (value instanceof ByteString) {
            valueBuilder.setType(BINARY).setBinary((ByteString) value);

        } else {
            throw new UnknownTypeException("Unknown type: " + value + " - " + value.getClass());
        }
        return KVpairBuilder.build();
    }
	
	
	public static Object valueToObject(Value v) {
		switch (v.getType()) {
			case BOOL:
				return v.getBool();
			case DOUBLE:
				return v.getDouble();
			case INT:
				return v.getInt();
			case STRING:
				return v.getString();
			case ARRAY:
				int size = v.getArrayCount();
				Object[] oa = new Object[size];
				for (int i = 0; i < size; i++) {
					Value vi = v.getArray(i);
					oa[i] = valueToObject(vi);
				}
				return oa;
			case BINARY:
				return v.getBinary();
			case VOID:
			default:
				return null;
		}
	}
}
