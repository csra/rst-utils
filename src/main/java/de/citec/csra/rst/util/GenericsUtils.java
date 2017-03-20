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
import rst.generic.ValueType.Value;
import static rst.generic.ValueType.Value.Type.*;

/**
 * Utility class for an easy manipulation of {@link rst.generic.KeyValuePairType.KeyValuePair KeyValuePair} objects.
 * @author nkoester
 */
public class GenericsUtils {

    private static final KeyValuePair.Builder PAIRBLD = KeyValuePair.newBuilder();
	private static final Value.Builder VALBLD = PAIRBLD.getValueBuilder();
	
    /**
     * Creates a {@link rst.generic.KeyValuePairType.KeyValuePair} for any given String and object. The given object is casted to the 
     * according {@link rst.generic.ValueType.Value} available in RST if possible.
     * 
	 * @see de.citec.csra.rst.util.GenericsUtils#objectToValue
     * @param key the key for the KVP.
     * @param value the target value for the KVP.
     * @return a KeyValuePair containing the provided key and an object as a {@link rst.generic.ValueType.Value}.
	 * @throws IllegalArgumentException if the object cannot be casted into an appropriate {@link rst.generic.ValueType.Value}.
     */
    public static KeyValuePair getKeyValuePair(String key, Object value) throws IllegalArgumentException {
        PAIRBLD.clear();
        PAIRBLD.setKey(key);
        PAIRBLD.setValue(objectToValue(value));
        return PAIRBLD.build();
    }
	
	/**
	 * Retrieves a plain java object from a given {@link rst.generic.ValueType.Value}.
	 * Reads the value's description in order to return the appropriate content.
	 * Recursively converts the value's content to an object array if the value's
	 * type is {@link rst.generic.ValueType.Value.Type#ARRAY}. May be null if the
	 * value's type is {@link rst.generic.ValueType.Value.Type#VOID}. Furthermore,
	 * {@link rst.generic.ValueType.Value.Type#BINARY} types are represented as a
	 * {@link com.google.protobuf.ByteString}.
	 * @param value the value to convert.
	 * @return a java object representing the value's content.
	 */
	public static Object valueToObject(Value value) {
		switch (value.getType()) {
			case BOOL:
				return value.getBool();
			case DOUBLE:
				return value.getDouble();
			case INT:
				return value.getInt();
			case STRING:
				return value.getString();
			case ARRAY:
				int size = value.getArrayCount();
				Object[] oa = new Object[size];
				for (int i = 0; i < size; i++) {
					Value vi = value.getArray(i);
					oa[i] = valueToObject(vi);
				}
				return oa;
			case BINARY:
				return value.getBinary();
			case VOID:
			default:
				return null;
		}
	}
	
	/**
	 * Converts a java object o an appropriate {@link rst.generic.ValueType.Value}.
	 * If the given object is an instance of a supported type, the object is casted
	 * and converted as an according {@link rst.generic.ValueType.Value} type.
	 * An {@link IllegalArgumentException} is raised otherwise. Only supports basic types that are
	 * specified as an enumeration item in {@link rst.generic.ValueType.Value Value}.
	 * @param object the object to convert.
	 * @return a value with the appropriate type.
	 * @throws IllegalArgumentException if the object cannot be casted into an appropriate {@link rst.generic.ValueType.Value}.
	 */
	public static Value objectToValue(Object object) {
		VALBLD.clear();
		if (object == null) {
            VALBLD.setType(VOID);
        } else if (object instanceof String) {
            VALBLD.setType(STRING).setString((String) object);
        } else if (object instanceof Double || object instanceof Float) {
            VALBLD.setType(DOUBLE).setDouble((double) object);
        } else if (object instanceof Integer) {
            VALBLD.setType(INT).setInt((int) object);
        } else if (object instanceof Long) {
            VALBLD.setType(INT).setInt((int) ((long) object));
        } else if (object instanceof Boolean) {
            VALBLD.setType(BOOL).setBool((boolean) object);
        } else if (object instanceof ByteString) {
            VALBLD.setType(BINARY).setBinary((ByteString) object);
        } else {
            throw new IllegalArgumentException("Unknown type: " + object + " - " + object.getClass());
        }
		return VALBLD.build();
	}
}
