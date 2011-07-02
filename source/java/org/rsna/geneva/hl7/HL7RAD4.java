/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

public class HL7RAD4 extends HL7Message {

	/**
	 * Create an A04 message.
	 */
	public HL7RAD4() {
		super("RAD4(ORM)");
		addSegment(new HL7Segment("MSH"));
		addSegment(new HL7Segment("PID"));
		addSegment(new HL7Segment("PV1"));
		addSegment(new HL7Segment("ORC"));
		addSegment(new HL7Segment("OBR"));
		addSegment(new HL7Segment("ZDS"));
	}

	public HL7Segment setMSH(
				String receivingApplication,
				String receivingFacility,
				String dateTime,
				String messageControlID) {

		HL7Segment msh = getSegment("MSH");
		if (msh != null) {
			msh.setField( 3,"XDSDEMO_OF");
			msh.setField( 4,"XDSDEMO");
			msh.setField( 5,receivingApplication);
			msh.setField( 6,receivingFacility);
			msh.setField( 7,dateTime);
			msh.setField( 9,"ORM^O01^ORM_O01");
			msh.setField(10,messageControlID);
			msh.setField(11,"P");
			msh.setField(12,"2.3.1");
			msh.setField(20,"");
		}
		return msh;
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
			pid.setField(39,"");
		}
		return pid;
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

	public HL7Segment setORC(
				String placerOrderNumber,
				String fillerOrderNumber,
				String dateTime,
				String enteredBy,
				String orderingProvider,
				String enteringOrganization) {
		HL7Segment orc = getSegment("ORC");
		if (orc != null) {
			orc.setField( 1,"NW");
			orc.setField( 2,placerOrderNumber);
			orc.setField( 3,fillerOrderNumber);
			orc.setField( 5,"SC");
			orc.setField( 7,"1^once^^^^S");
			orc.setField( 9,dateTime);
			orc.setField(10,enteredBy);
			orc.setField(12,orderingProvider);
			orc.setField(14,"3145551212");
			orc.setField(15,dateTime);
			orc.setField(17,enteringOrganization);
			orc.setField(19,"");
		}
		return orc;
	}

	public HL7Segment setOBR(
				String setID,
				String placerOrderNumber,
				String fillerOrderNumber,
				String universalServiceID,
				String orderingProvider,
				String placerField1,
				String placerField2,
				String fillerField1,
				String diagnosticServiceID,
				String procedureCode) {
		HL7Segment obr = getSegment("OBR");
		if (obr != null) {
			obr.setField( 1,setID);
			obr.setField( 2,placerOrderNumber);
			obr.setField( 3,fillerOrderNumber);
			obr.setField( 4,universalServiceID);
			obr.setField(13,"relevant clinical info");
			obr.setField(15,"Radiology^^^^R");
			obr.setField(16,orderingProvider);
			obr.setField(17,"3145551212");
			obr.setField(18,placerField1);
			obr.setField(19,placerField2);
			obr.setField(20,fillerField1);
			obr.setField(24,diagnosticServiceID);
			obr.setField(27,"1^once^^^^S");
			obr.setField(30,"WALK");
			obr.setField(41,"A");
			obr.setField(44,procedureCode);
		}
		return obr;
	}

	public HL7Segment setZDS(
				String studyInstanceUID) {
		HL7Segment zds = getSegment("ZDS");
		if (zds != null) {
			zds.setField( 1,studyInstanceUID+"^100^Application^DICOM");
		}
		return zds;
	}

}

