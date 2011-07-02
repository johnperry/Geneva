/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

public abstract class HL7A04 extends HL7Message {

	/**
	 * Create an A04 message.
	 */
	public HL7A04(String name) {
		super(name);
		addSegment(new HL7Segment("MSH"));
		addSegment(new HL7Segment("EVN"));
		addSegment(new HL7Segment("PID"));
		addSegment(new HL7Segment("PV1"));
	}

	public HL7Segment setMSH(
				String receivingApplication,
				String receivingFacility,
				String dateTime,
				String messageControlID) {

		HL7Segment msh = getSegment("MSH");
		if (msh != null) {
			msh.setField( 3,"XDSDEMO_ADT");
			msh.setField( 4,"XDSDEMO");
			msh.setField( 5,receivingApplication);
			msh.setField( 6,receivingFacility);
			msh.setField( 7,dateTime);
			msh.setField( 9,"ADT^A04^ADT_A01");
			msh.setField(10,messageControlID);
			msh.setField(11,"P");
			msh.setField(12,"2.3.1");
			msh.setField(20,"");
		}
		return msh;
	}

	public HL7Segment setEVN(
				String dateTime,
				String eventOccurred) {

		HL7Segment evn = getSegment("EVN");
		if (evn != null) {
			evn.setField( 2,dateTime);
			evn.setField( 6,eventOccurred);
		}
		return evn;
	}

	public HL7Segment setPID(
				String id,
				String assigningAuthority,
				String patientName,
				String email,
				String birthdate,
				String sex,
				String streetAddress,
				String city,
				String state,
				String zip,
				String country) {

		HL7Segment pid = getSegment("PID");
		if (pid != null) {
			pid.setField( 3,id + "^^^" + assigningAuthority /*+ "^PI"*/);
			pid.setField( 5,patientName);
			pid.setField( 7,birthdate);
			pid.setField( 8,sex);
			pid.setField(11,streetAddress + "^^" + city + "^" + state + "^" + zip + "^" + country);
			pid.setField(13,"^^^"+email);
			pid.setField(18,id);
			pid.setField(30,"");
		}
		return pid;
	}

}

