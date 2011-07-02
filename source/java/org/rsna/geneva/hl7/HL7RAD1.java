/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

public class HL7RAD1 extends HL7A04 {

	/**
	 * Create an A04 message.
	 */
	public HL7RAD1() {
		super("RAD1(A04)");
	}

	public HL7Segment setPV1(
				String patientLocation,
				String referringDoctor,
				String dateTime,
				String visitNumber) {
		HL7Segment pv1 = getSegment("PV1");
		if (pv1 != null) {
			pv1.setField( 2,"O");
			pv1.setField( 3,patientLocation);
			pv1.setField( 8,referringDoctor);
			pv1.setField(19,visitNumber);
			pv1.setField(44,dateTime);
			pv1.setField(51,"V");
			pv1.setField(52,"");
		}
		return pv1;
	}

}

