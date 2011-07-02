/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.io.File;
import java.net.*;
import java.util.*;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.hl7.*;
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.util.StringUtil;

public class DataSystem extends Product {

	public String hl7URL;
	public String hl7Version;
	public String soapVersion;
    // HL7 v2
	public String receivingApplication;
	public String receivingFacility;
    // HL7 v3
	public String receiverDeviceId;
	public String receiverDeviceName;
	public boolean acceptsITI8withGlobalID;
	public boolean acceptsITI8withLocalID;
	public int timeout;
	public HL7Field[] fields;
	public String globalAssigningAuthority;

	public DataSystem(Element el) {
		super(el);

		this.enabled = !el.getAttribute("enabled").trim().equals("no");
		this.hl7Version = el.getAttribute("hl7Version").trim();
		if (this.hl7Version.equals("")) this.hl7Version = "2";
		this.soapVersion = el.getAttribute("soapVersion").trim();
		if (this.soapVersion.equals("")) this.soapVersion = "SOAP_1_2";
		this.hl7URL = el.getAttribute("hl7URL").trim();

		this.receivingApplication
            = el.getAttribute("receivingApplication").trim();
		this.receivingFacility = el.getAttribute("receivingFacility").trim();

		this.receiverDeviceId = el.getAttribute("receiverDeviceId").trim();
		this.receiverDeviceName = el.getAttribute("receiverDeviceName").trim();

		this.acceptsITI8withGlobalID
            = !el.getAttribute("acceptsITI8withGlobalID").trim().equals("no");
		this.acceptsITI8withLocalID
            = !el.getAttribute("acceptsITI8withLocalID").trim().equals("no");

		String timeoutString = el.getAttribute("timeout").trim();
		try { this.timeout = Integer.parseInt(timeoutString); }
		catch (Exception ex) { this.timeout = 0; }
		if (this.timeout == 0) this.timeout = 5000;

		fields = Configuration.getInstance().getFields(el);

		this.globalAssigningAuthority
            = el.getAttribute("globalAssigningAuthority").trim();
		globalAssigningAuthority = getGlobalAssigningAuthority();
	}

	public String getGlobalAssigningAuthority() {
		if (!globalAssigningAuthority.equals(""))
			return globalAssigningAuthority;
		return Configuration.getInstance().getGlobalAssigningAuthority();
	}

	void processRegistration(Registration reg) {
		if (acceptsITI8withGlobalID) sendPatientIdFeedWithGlobalID(reg);
	}

	//Send the globalID.
	private void sendPatientIdFeedWithGlobalID(Registration reg) {
        PatientIdFeed pif
                = IHETransactionFactory.getPatientIdFeedInstance(
                                                     el, reg, Configuration.getInstance());
        pif.setAssigningAuthority( globalAssigningAuthority);
        pif.setPatientId( reg.globalID);

        IHETransactionResponse response = pif.send();

        RegSysEvent event =
            new RegSysEvent(
                this,
                response.getStatusInt(),
                RegSysEvent.TYPE_HL7,
                pif.getName() +" sent to "+getType() + " ["+id+"]" +
                "<br>Global ID: "+reg.globalID +
                "<br>Message:<br>" +
                StringUtil.displayable(pif.toString()) +
                "<br>Response:<br>" +
                StringUtil.displayable(response.getStatusString()));
        Configuration.getInstance().getEventLog().append(event);
	}

	public void appendTableRow(StringBuffer sb) {
		sb.append("<tr><td><b>" + getType() + "</b><br>ID: "+id+"</td><td>");
		sb.append("<table border=\"0\" width=\"100%\">");
		appendDataRows(sb);
		sb.append("</table>");
		sb.append("</td></tr>");
	}

	public void appendDataRows(StringBuffer sb) {
		sb.append("<tr><td width=\"165\">enabled:</td><td>"+(enabled?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>HL7 URL:</td><td>"+hl7URL+"</td></tr>");
		sb.append("<tr><td>HL7 Version:</td><td>"+hl7Version+"</td></tr>");
		sb.append("<tr><td>SOAP Version:</td><td>"+soapVersion+"</td></tr>");
		sb.append("<tr><td>Receiving Application:</td><td>"+receivingApplication+"</td></tr>");
		sb.append("<tr><td>Receiving Facility:</td><td>"+receivingFacility+"</td></tr>");
		sb.append("<tr><td>Receiver Device Id:</td><td>"+receiverDeviceId+"</td></tr>");
		sb.append("<tr><td>Receiver Device Name:</td><td>"+receiverDeviceName+"</td></tr>");
		sb.append("<tr><td>Accepts ITI8 with Global ID:</td><td>"+(acceptsITI8withGlobalID?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Accepts ITI8 with Local ID:</td><td>"+(acceptsITI8withLocalID?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Thread Startup Delay (ms):</td><td>"+startupDelay+"</td></tr>");
		sb.append("<tr><td>HL7 Timeout (ms):</td><td>"+timeout+"</td></tr>");
		listFields(sb,fields,"ITI8 Fields");
		sb.append("<tr><td>Global Assigning Authority:</td><td>"+globalAssigningAuthority+"</td></tr>");
	}

	void listFields(StringBuffer sb, HL7Field[] fields, String name) {
		if (fields.length > 0) {
			for (int i=0; i<fields.length; i++) {
				HL7Field f = fields[i];
				if (i == 0)
					sb.append("<tr><td>"+name+":</td>");
				else
					sb.append("<tr><td></td>");
				sb.append("<td>"+f.segment+"["+f.seq+"]=\""+f.value+"\"</td></tr>");
			}
		}
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("hl7URL", hl7URL);
			e.setAttribute("hl7Version", hl7Version);
			e.setAttribute("soapVersion", soapVersion);
			e.setAttribute("receivingApplication", receivingApplication);
			e.setAttribute("receivingFacility", receivingFacility);
			e.setAttribute("receiverDeviceId", receiverDeviceId);
			e.setAttribute("receiverDeviceName", receiverDeviceName);
			e.setAttribute("acceptsITI8withGlobalID", yesNo(acceptsITI8withGlobalID));
			e.setAttribute("acceptsITI8withLocalID", yesNo(acceptsITI8withLocalID));
			e.setAttribute("timeout", Integer.toString(timeout));
			e.setAttribute("globalAssigningAuthority", globalAssigningAuthority);
			//TBD -- insert child elements for the fields here
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = Product.createNewElement(name, id);
			e.setAttribute("hl7Version", "2");
			e.setAttribute("soapVersion", "SOAP_1_2");
			e.setAttribute("receivingApplication", "APPLICATION");
			e.setAttribute("receivingFacility", "FACILITY");
			e.setAttribute("acceptsITI8withGlobalID", "yes");
			e.setAttribute("acceptsITI8withLocalID", "yes");
			Configuration config = Configuration.getInstance();
			e.setAttribute("globalAssigningAuthority", config.getGlobalAssigningAuthority());
			return e;
		}
		catch (Exception ex) { return null; }
	}

}
