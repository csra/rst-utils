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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;

/**
 *
 * @author Patrick Holthaus
 */
public class TypesUtils {

	private final static Logger LOG = Logger.getLogger(TypesUtils.class.getName());
	private final static Pattern RST = Pattern.compile("^\\.(.*?):\\{(.*)\\}$");
	
	public static Object parse(String dsc) {
		if (dsc == null) {
			LOG.log(Level.FINER, "Description is null, returning.");
			return dsc;
		}

		try {
			return NumberFormat.getInstance().parse(dsc);
		} catch (ParseException ex) {
			LOG.log(Level.FINER, "Could not infer number from description string ''{0}''.", dsc);
		}

		switch (dsc.toLowerCase()) {
			case "false":
				return false;
			case "true":
				return true;
			default:
				LOG.log(Level.FINER, "Could not infer boolean from description string ''{0}''.", dsc);
		}

        Matcher matcher = RST.matcher(dsc);
		if (matcher.matches()) {
			try {
				String rstName =  matcher.group(1);
				String typeInfo = matcher.group(2);
				LOG.log(Level.FINER, "Assuming RST of type ''{0}'' specified as ''{1}''.", new Object[]{rstName, typeInfo});

				String pkg = rstName.substring(0, rstName.lastIndexOf(".") + 1);
				String clz = rstName.substring(rstName.lastIndexOf(".") + 1);
				String fqClz = pkg + clz + "Type$" + clz;

				LOG.log(Level.FINER, "Trying to instantiate builder for class ''{0}''.", fqClz);

				Class<?> cls = Class.forName(fqClz);
				Message.Builder msgBuilder = (Message.Builder) cls.getMethod("newBuilder").invoke(null);
				TextFormat.merge(typeInfo, msgBuilder);
				
				Message msg = msgBuilder.build();
				DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(msg));
				LOG.log(Level.FINER, "Loaded default converter for message ''{0}''.", StringRepresentation.shortString(msg));
				return msg;
			} catch (StringIndexOutOfBoundsException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException | TextFormat.ParseException e) {
				LOG.log(Level.FINER, "Unable to parse string ''{0}'' as an rst data type ({1}), returning original definiton.", new Object[]{dsc, e});
				return dsc;
			}
		} else {
			LOG.log(Level.FINER, "Could not infer rst from description string ''{0}''.", dsc);
			return dsc;
		}
	}
}
