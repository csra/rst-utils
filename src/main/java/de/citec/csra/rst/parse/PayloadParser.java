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
package de.citec.csra.rst.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import rsb.InitializeException;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public abstract class PayloadParser<K, V> {

	private final StringParser<K> keyp;
	private final StringParser<V> valuep;
	private final String ignore;
	private final String partSep;
	private final String keySep;
	private final String valSep;

	private final static Logger LOG = Logger.getLogger(PayloadParser.class.getName());

	public PayloadParser(String ignore, StringParser<K> k, StringParser<V> p) throws InitializeException {
		this(ignore, "%", ":", ";", k, p);
	}
	
	public PayloadParser(String ignore, String partSep, String keySep, String valSep, StringParser<K> k, StringParser<V> p) throws InitializeException {
		this.valuep = p;
		this.keyp = k;
		this.ignore = ignore;
		this.partSep = partSep;
		this.keySep = keySep;
		this.valSep = valSep;
	}


	public Map<String, String> parsePayload(String payload) {
		Map<String, String> stringcfg = new HashMap<>();
		String[] configs = payload.split(partSep);
		for (String config : configs) {
			String[] parts = config.split(keySep);
			if (parts.length != 2) {
				LOG.log(Level.WARNING, "invalid action specification: ''{0}'', ignoring.", config);
			} else {
				if (parts[0].length() < 1) {
					LOG.log(Level.WARNING, "invalid action specification: ''{0}'', ignoring.", parts[0]);
				} else {
					String[] locs = parts[1].split(valSep);
					if (locs.length < 1) {
						LOG.log(Level.WARNING, "value empty or invalid: ''{0}'', ignoring.", parts[1]);
					} else {
						for (String loc : locs) {
							if (loc.length() < 1) {
								LOG.log(Level.WARNING, "invalid value specification: ''{0}'', ignoring.", loc);
							} else {
								stringcfg.put(loc, parts[0]);
							}
						}
					}
				}
			}
		}
		return stringcfg;
	}

	public Map<K, V> parseConfiguration(Map<String, String> cfgs) throws Exception {
		Map<K, V> parsedcfg = new HashMap<>();
		for (Map.Entry<String, String> entry : cfgs.entrySet()) {
			K key = keyp.getValue(entry.getKey());
			V value = valuep.getValue(entry.getValue());
			parsedcfg.put(key, value);
		}
		return parsedcfg;
	}

	public boolean isIgnored(String label) {
		return Pattern.matches(ignore, label);
	}
}
