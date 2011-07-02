/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

public class HL7ITI8 extends HL7A04 {

	/**
	 * Create an A04 message.
	 */
	public HL7ITI8() {
		super("ITI8(A04)");
	}

	public HL7Segment setPV1() {
		HL7Segment pv1 = getSegment("PV1");
		if (pv1 != null) {
			pv1.setField( 2,"O");
			pv1.setField(52,"");
		}
		return pv1;
	}

}

