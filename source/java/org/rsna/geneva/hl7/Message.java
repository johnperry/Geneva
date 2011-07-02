/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.hl7;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.io.File;
import java.net.*;
import java.util.*;
import org.rsna.geneva.elements.*;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;
import org.rsna.util.XmlUtil;
import org.rsna.geneva.misc.Address;

public class Message extends ConfigElement {

	public static String type = "Message";

	public Element element;
	public String filename;
	public String systemID;
	public String description;
	public File file;

	public Message(Element el) {
		super(el);
		this.enabled = !el.getAttribute("enabled").trim().equals("no");
		this.systemID = el.getAttribute("ehrsystemID").trim();
		this.filename = el.getAttribute("file").trim();
		this.file = new File(filename);
	}

	public HL7Message createHL7Message(EHRSystem ehrsystem, Registration reg, Configuration config) {
		try {
			Document msgDoc = XmlUtil.getDocument(file);
			Element root = msgDoc.getDocumentElement();
			HL7Message msg = new HL7Message(root.getTagName().toUpperCase());
			Node ch = root.getFirstChild();
			while (ch != null) {
				if (ch.getNodeType() == Node.ELEMENT_NODE) {
					Element segment = (Element)ch;
					String name = segment.getTagName().toUpperCase();
					HL7Segment seg = new HL7Segment(name);
					msg.addSegment(seg);

					//Prepopulate the segment if we can
					if (name.equals("MSH")) setMSH(seg, ehrsystem, reg, config);
					else if (name.equals("EVN")) setEVN(seg, ehrsystem, reg, config);
					else if (name.equals("PID")) setPID(seg, ehrsystem, reg, config);
					else if (name.equals("PV1")) setPV1(seg, ehrsystem, reg, config);
					else if (name.equals("DG1")) setDG1(seg, ehrsystem, reg, config);

					//Now process the child fields
					Node gch = ch.getFirstChild();
					while (gch != null) {
						if (gch.getNodeType() == Node.ELEMENT_NODE) {
							Element field = (Element)gch;
							try {
								int n = Integer.parseInt(field.getAttribute("n"));
								String value = field.getTextContent().trim();
								value = filter(value,ehrsystem,reg,config);
								if (n > 0) seg.setField(n, value);
							}
							catch (Exception skipField) { }
						}
						gch = gch.getNextSibling();
					}
				}
				ch = ch.getNextSibling();
			}
			return msg;
		}
		catch (Exception ex) { }
		return null;
	}

	private String filter(String value, EHRSystem ehrsystem, Registration reg, Configuration config) {
		if (value.equals("@dateTime")) { //for backward compatibility
			value = config.getDateTime();
			return value;
		}
		value = value.replace("{reg.name}",reg.getName());
		value = value.replace("{reg.birthdate}",reg.birthdate);
		value = value.replace("{reg.email}",reg.email);
		value = value.replace("{reg.sex}",reg.sex);
		value = value.replace("{reg.address}",
					reg.street + "^^" + reg.city + "^" + reg.state + "^" + reg.zip + "^" + reg.country);
		value = value.replace("{reg.globalID}",reg.globalID);
		value = value.replace("{reg.localID}",reg.localIDTable.get(ehrsystem.id));
		value = value.replace("{config.dateTime}",config.getDateTime());
		int k;
		while ((k=value.indexOf("{config.address}")) != -1) {
			Address adrs = config.getAddress(reg.sex);
			String address =
					adrs.street + "^^" + adrs.city + "^" + adrs.state + "^" + adrs.zip + "^" + adrs.country;
			value = value.substring(0,k) + address + value.substring(k+16);
		}
		while ((k=value.indexOf("{config.docName}")) != -1) {
			value = value.substring(0,k) + config.getPhysicianName() + value.substring(k+16);
		}
		return value;
	}

	private void setMSH(HL7Segment msh, EHRSystem ehr, Registration reg, Configuration config) {
		msh.setField( 3,"XDSDEMO_ADT");
		msh.setField( 4,"XDSDEMO");
		msh.setField( 5,ehr.receivingApplication);
		msh.setField( 6,ehr.receivingFacility);
		msh.setField( 7,config.getDateTime());
		msh.setField( 9,"ADT^A03^ADT_A01");
		msh.setField(10,config.getMessageControlID());
		msh.setField(11,"P");
		msh.setField(12,"2.3.1");
		msh.setField(20,"");
	}

	public void setEVN(HL7Segment evn, EHRSystem ehr, Registration reg, Configuration config) {
		evn.setField( 1,"A03");
		evn.setField( 2,config.getDateTime());
		evn.setField( 6,config.getDateTime());
	}

	private void setPID(HL7Segment pid, EHRSystem ehr, Registration reg, Configuration config) {
		String id = reg.localIDTable.get(ehr.id);
		pid.setField( 3,id + "^^^" + ehr.localAssigningAuthority /*+ "^PI"*/);
		pid.setField( 5,reg.getName());
		pid.setField( 7,reg.birthdate);
		pid.setField( 8,reg.sex);
		pid.setField(11,reg.street + "^^" + reg.city + "^" + reg.state + "^" + reg.zip + "^" + reg.country);
		pid.setField(13,"^^^"+reg.email);
		pid.setField(18,id);
		pid.setField(30,"");
	}

	private void setPV1(HL7Segment pv1, EHRSystem ehr, Registration reg, Configuration config) {
		pv1.setField( 2,"I^INPATIENT^HL70004");
		pv1.setField( 3,config.getPtLocation(ehr.pointOfCare));
		pv1.setField( 7,config.getPhysicianName());
		pv1.setField(17,config.getPhysicianName());
		pv1.setField(19,config.getPtVisit());
		pv1.setField(44,config.getDateTime());
		pv1.setField(45,config.getDateTime());
		pv1.setField(51,"V");
		pv1.setField(52,"");
	}

	public void setDG1(HL7Segment dg1, EHRSystem ehr, Registration reg, Configuration config) {
		dg1.setField( 1,"1");
		dg1.setField( 2,"FF");
		dg1.setField( 5,config.getDateTime());
		dg1.setField( 6,"A");
		dg1.setField(15,"0");
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("ehrsystemID", systemID);
			e.setAttribute("file", filename);
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public String getType() {
		return type;
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = Product.createNewElement(name, id);
			e.setAttribute("type", "Message");
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public void appendTableRow(StringBuffer sb) {
		sb.append("<tr><td><b>Message</b><br>ID: "+id+"</td><td>");
		sb.append("<table border=\"0\" width=\"100%\">");
		sb.append("<tr><td width=\"165\">enabled:</td><td>"+(enabled?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>EHR System ID:</td><td>"+systemID+"</td></tr>");
		String color = (file.exists()) ? "black" : "red";
		sb.append("<tr><td>File:</td><td><font color=\""+color+"\">"+file+"</font></td></tr>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</td></tr>");
	}

}


