/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

import java.io.*;
import java.net.*;
import java.util.*;

public class HL7Segment {

	public String name;

	Hashtable<Integer,String> fieldTable;

	static final String separator = "|";

	/**
	 * Create a new HL7Segment.
	 */
	public HL7Segment(String name) {
		this.name = name.trim().toUpperCase();
		fieldTable = new Hashtable<Integer,String>();
	}

	public void setField(int seq, String value) {
		fieldTable.put(new Integer(seq), value);
	}

	public String getField(int seq) {
		return fieldTable.get(new Integer(seq));
	}

	public String toString() {
		int currentSeq = 0;
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		if (name.equals("MSH")) {
			sb.append(separator + "^~\\&");
			currentSeq = 2;
		}
		Set<Integer> keySet = fieldTable.keySet();
		Integer[] keys = keySet.toArray(new Integer[0]);
		Arrays.sort(keys);

		for (int i=0; i<keys.length; i++) {
			int seq = keys[i].intValue();
			while (currentSeq < seq) {
				sb.append(separator);
				currentSeq++;
			}
			sb.append(fieldTable.get(keys[i]));
		}
		sb.append("\r");
		return sb.toString();
	}

}

