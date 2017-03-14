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
import rst.generic.KeyValuePairType;
import rst.generic.ValueType;

/**
 *
 * @author nkoester
 */
public class GenericsUtils {

    private static final KeyValuePairType.KeyValuePair.Builder KVpairBuilder = KeyValuePairType.KeyValuePair.newBuilder();

    /**
     * Creates a KeyValuePair for any given String and object. Object is casted to the 
     * according type available in RST if possible. UnknownTypeException is raised 
     * otherwise. Only support base types.
     * 
     * @param key The key for the KVP
     * @param value the target value for the KVP
     * @return 
     */
    private static KeyValuePairType.KeyValuePair getKeyValuePair(String key, Object value) throws UnknownTypeException {
        KVpairBuilder.clear();
        KVpairBuilder.setKey(key);
        ValueType.Value.Builder valueBuilder = KVpairBuilder.getValueBuilder();

        if (value == null) {
            valueBuilder.setType(ValueType.Value.Type.VOID);

        } else if (value instanceof String) {
            valueBuilder.setType(ValueType.Value.Type.STRING).setString((String) value);

        } else if (value instanceof Double || value instanceof Float) {
            valueBuilder.setType(ValueType.Value.Type.DOUBLE).setDouble((double) value);

        } else if (value instanceof Integer) {
            valueBuilder.setType(ValueType.Value.Type.INT).setInt((int) value);

        } else if (value instanceof Long) {
            valueBuilder.setType(ValueType.Value.Type.INT).setInt((int) ((long) value));

        } else if (value instanceof Boolean) {
            valueBuilder.setType(ValueType.Value.Type.BOOL).setBool((boolean) value);

        } else if (value instanceof ByteString) {
            valueBuilder.setType(ValueType.Value.Type.BINARY).setBinary((ByteString) value);

        } else {
            throw new UnknownTypeException("Unknown type: " + value + " - " + value.getClass());
        }
        return KVpairBuilder.build();
    }

}
