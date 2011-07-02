/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

public class HL7Field {

	public String msg;
	public String segment;
	public int seq;
	public String value;

	public HL7Field(String msg, String segment, String seq, String value) throws Exception {
		this.msg = msg;
		this.segment = segment;
		this.seq = Integer.parseInt(seq);
		this.value = value;
	}

	public HL7Field(String msg, String segment, int seq, String value) {
		this.msg = msg;
		this.segment = segment;
		this.seq = seq;
		this.value = value;
	}

}

