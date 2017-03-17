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

import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Patrick Holthaus
 */
public class TypesUtils {

	private final static Logger LOG = Logger.getLogger(TypesUtils.class.getName());

	public static Object parseText(String definition) throws IllegalArgumentException {
		if (definition.startsWith(".")) {
			try {
				String rstName = definition.substring(definition.indexOf(".") + 1, definition.indexOf(":"));
				String typeInfo = definition.substring(definition.indexOf("{") + 1, definition.lastIndexOf("}"));
				LOG.log(Level.FINER, "Assuming RST of type ''{0}'' specified as ''{1}''.", new Object[]{rstName, typeInfo});
				
				String pkg = rstName.substring(0, rstName.lastIndexOf(".") + 1);
				String clz = rstName.substring(rstName.lastIndexOf(".") + 1);
				String fqClz = pkg + clz + "Type$" + clz;
				
				LOG.log(Level.FINER, "Trying to instantiate builder for class ''{0}''.", fqClz);
				
				Class<?> cls = Class.forName(fqClz);
				Message.Builder msgBuilder = (Message.Builder) cls.getMethod("newBuilder").invoke(null);
				TextFormat.merge(typeInfo, msgBuilder);
				return msgBuilder.build();
			} catch (StringIndexOutOfBoundsException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException | TextFormat.ParseException e) {
				LOG.log(Level.FINER, "Unable to parse string ''{0}'' as an rst data type ({1}), returning original definiton.", new Object[]{definition, e.getMessage()});
				return definition;
			}
		} else {
			return definition;
		}
	}
}
