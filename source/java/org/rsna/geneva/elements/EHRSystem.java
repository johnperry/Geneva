/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import org.w3c.dom.Element;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.hl7.*;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.util.StringUtil;

public class EHRSystem extends DataSystem {

	public String type = "EHR System";

	public String localAssigningAuthority;
	public boolean sendsITI8withLocalID;
	public boolean acceptsRAD1;
	public boolean acceptsMessages;
	public String pointOfCare;

	public EHRSystem(Element el) {
		super(el);
		this.localAssigningAuthority
            = el.getAttribute("localAssigningAuthority").trim();
		this.sendsITI8withLocalID
            = !el.getAttribute("sendsITI8withLocalID").trim().equals("no");
		this.acceptsRAD1 = !el.getAttribute("acceptsRAD1").trim().equals("no");
		this.acceptsMessages
            = el.getAttribute("acceptsMessages").trim().equals("yes");
		this.pointOfCare = el.getAttribute("pointOfCare").trim();

		localAssigningAuthority = getLocalAssigningAuthority();
	}

	void processRegistration(Registration reg) {
		super.processRegistration(reg);

		if (acceptsITI8withLocalID) {
            sendPatientIdFeedWithLocalID( reg);
		}

		if (acceptsRAD1) {
			sendRAD1(fields, reg, reg.localIDTable.get(id));
		}

		//Now process the Messages for this EHRSystem
		if (acceptsMessages) {
			Message[] messages = Configuration.getInstance().getMessages();
			for (int i=0; i<messages.length; i++) {
				if (messages[i].enabled && messages[i].systemID.equals(id)) {
					sendMessage(messages[i], reg);
				}
			}
		}
	}

    protected void sendPatientIdFeedWithLocalID(Registration reg) {
        PatientIdFeed pif
            = IHETransactionFactory
            		.getPatientIdFeedInstance(
                              el, reg, Configuration.getInstance());
        pif.setAssigningAuthority( localAssigningAuthority);

        String patientId = reg.localIDTable.get(id);
        pif.setPatientId( patientId);
        IHETransactionResponse response = pif.send();
        RegSysEvent event =
            new RegSysEvent(
                this,
                response.getStatusInt(),
                RegSysEvent.TYPE_HL7,
                pif.getName() +" sent to "+type + " ["+id+"]" +
                "<br>Local ID: "+ patientId +
                "<br>Local Assigning Authority: " + localAssigningAuthority +
                "<br>Message:<br>" +
                StringUtil.displayable(pif.toString()) +
                "Response: "+StringUtil.displayable(response.getStatusString()));
        Configuration.getInstance().getEventLog().append(event);
    }

	public void sendRAD1(
				HL7Field[] fields,
				Registration reg,
				String localID) {

		Configuration config = Configuration.getInstance();
		if (enabled) {
			HL7RAD1 a04 = new HL7RAD1();
			String dateTime = config.getDateTime();
			String referringDoctor = config.getPhysicianName();
			String visit = config.getPtVisit();
			a04.setMSH(
					receivingApplication,
					receivingFacility,
					dateTime,
					config.getMessageControlID());
			a04.setEVN(
					dateTime,
					dateTime);
			a04.setPID(
					localID,
					localAssigningAuthority,
					reg.getName(),
					reg.email,
					reg.birthdate,
					reg.sex,
					reg.street,
					reg.city,
					reg.state,
					reg.zip,
					reg.country);
			a04.setPV1(
					config.getPtLocation(pointOfCare),
					referringDoctor,
					dateTime,
					visit);

			a04.setFields(fields);
			String response = a04.send(hl7URL,timeout);
			int status = HL7Message.toStatus(response);

			RegSysEvent event =
				new RegSysEvent(
						this,
						status,
						RegSysEvent.TYPE_HL7,
						a04.name+" sent to "+type + " ["+id+"]" +
						"<br>Local ID: " + localID +
						"<br>Local Assigning Authority: " + localAssigningAuthority +
						"<br>Message:<br>" +
						StringUtil.displayable(a04.toString()) +
						"Response: "+StringUtil.displayable(response));
			config.getEventLog().append(event);
		}
	}

	public void sendMessage(
				Message message,
				Registration reg) {

		Configuration config = Configuration.getInstance();
		if (enabled) {
			HL7Message msg = message.createHL7Message(this, reg, config);
			if (msg != null) {
				msg.setFields(fields);
				String response = msg.send(hl7URL,timeout);
				int status = HL7Message.toStatus(response);

				RegSysEvent event =
					new RegSysEvent(
							this,
							status,
							RegSysEvent.TYPE_HL7,
							msg.name+" sent to "+type + " ["+id+"]" +
							"<br>Message:<br>" +
							StringUtil.displayable(msg.toString()) +
							"Response: "+StringUtil.displayable(response));
				config.getEventLog().append(event);
			}
		}
	}

	public String getLocalAssigningAuthority() {
		if (!localAssigningAuthority.equals(""))
			return localAssigningAuthority;
		return Configuration.getInstance().getLocalAssigningAuthority();
	}

	public void appendDataRows(StringBuffer sb) {
		super.appendDataRows(sb);
		sb.append("<tr><td>Local Assigning Authority:</td><td>"+getLocalAssigningAuthority()+"</td></tr>");
		sb.append("<tr><td>Sends ITI8 with Local ID:</td><td>"+(sendsITI8withLocalID?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Accepts RAD1:</td><td>"+(acceptsRAD1?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Accepts Messages:</td><td>"+(acceptsMessages?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Point of Care:</td><td>"+pointOfCare+"</td></tr>");
	}

	public String getType() {
		return type;
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("localAssigningAuthority", localAssigningAuthority);
			e.setAttribute("sendsITI8withLocalID", yesNo(sendsITI8withLocalID));
			e.setAttribute("acceptsRAD1", yesNo(acceptsRAD1));
			e.setAttribute("acceptsMessages", yesNo(acceptsMessages));
			e.setAttribute("pointOfCare", pointOfCare);
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = DataSystem.createNewElement(name, id);
			e.setAttribute("type", "EHRSystem");
			e.setAttribute("sendsITI8withLocalID", "yes");
			e.setAttribute("acceptsRAD1", "yes");
			e.setAttribute("acceptsMessages", "no");
			Configuration config = Configuration.getInstance();
			e.setAttribute("localAssigningAuthority", config.getLocalAssigningAuthority());
			return e;
		}
		catch (Exception ex) { return null; }
	}

}
