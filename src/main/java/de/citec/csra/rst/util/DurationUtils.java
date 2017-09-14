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

import static de.citec.csra.rst.util.StringRepresentation.shortString;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Patrick Holthaus
 */
public class DurationUtils {

	private final static Logger LOG = Logger.getLogger(DurationUtils.class.getName());
	private final static Map<TimeUnit, Pattern> PATTERNS = new EnumMap<>(TimeUnit.class);

	private static TimeUnit defaultUnit = TimeUnit.MICROSECONDS;

	static {
		PATTERNS.put(DAYS, Pattern.compile(".*?(\\d+)\\s*d.*"));
		PATTERNS.put(HOURS, Pattern.compile(".*?(\\d+)\\s*h.*"));
		PATTERNS.put(MINUTES, Pattern.compile(".*?(\\d+)\\s*m.*"));
		PATTERNS.put(SECONDS, Pattern.compile(".*?(\\d+)\\s*s.*"));
		PATTERNS.put(MILLISECONDS, Pattern.compile(".*?(\\d+)\\s*ms.*"));
		PATTERNS.put(MICROSECONDS, Pattern.compile(".*?(\\d+)\\s*µs.*"));
		PATTERNS.put(NANOSECONDS, Pattern.compile(".*?(\\d+)\\s*ns.*"));
	}

	public static void setDefaultUnit(TimeUnit unit) {
		defaultUnit = unit;
	}

	public static long parse(String dsc, TimeUnit target) {
		if (target == null) {
			LOG.log(Level.FINER, "Target unit is null, using default value ''{0}''.", defaultUnit);
			target = defaultUnit;
		}

		if (dsc == null) {
			LOG.log(Level.FINER, "Description is null, returning '0'.");
			return 0;
		}

		try {
			long value = 0;
			for (Map.Entry<TimeUnit, Pattern> pair : PATTERNS.entrySet()) {
				Matcher m = pair.getValue().matcher(dsc);
				if (m.matches()) {
					value += target.convert(Long.valueOf(m.group(1)), pair.getKey());
					LOG.log(Level.FINEST, "Adding {0} {1}", new String[]{m.group(1), pair.getKey().name()});
				}
			}
			LOG.log(Level.FINER, "Converted ''{0}'' to {1} {2}", new Object[]{dsc, value, target.name()});
			return value;
		} catch (NumberFormatException ex) {
			LOG.log(Level.FINER, "Could not infer time value from description string ''{0}'', returning '0'.", shortString(dsc));
			return 0;
		}
	}
}
