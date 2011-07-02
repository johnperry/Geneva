/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.net.*;
import java.util.*;
import org.rsna.geneva.main.Configuration;
import org.rsna.server.HttpRequest;
import org.rsna.util.StringUtil;

public class Registration {

	public String givenName;
	public String familyName;
	public String street;
	public String city;
	public String state;
	public String zip;
	public String country;
	public String email;
	public String birthdate;
	public String sex;
	public String globalID;
	public LinkedList<LocalID> localIDs;
	public Hashtable<String,String> localIDTable;
	public String localID; //for matching only

	//This constructor is used for new registrations
	public Registration(HttpRequest req, Configuration config) {
		givenName = req.getParameter("givenName", "");
		givenName = givenName.replaceAll("[^a-zA-Z]","");
		familyName = req.getParameter("familyName", "");
		familyName = familyName.replaceAll("[^a-zA-Z]","");
		birthdate = filterDate(req.getParameter("birthdate", ""));
		email = req.getParameter("email", "").trim();
		if (email.equals("")) {
			email = givenName + "."
						+ familyName + "."
							+ ( (System.currentTimeMillis()/1000) % 1000 )
								+"@ihe.net";
		}
		sex = req.getParameter("sex", "M");
		sex = sex.equals("") ? "M" : sex;
		Address address = config.getAddress(sex);
		street = address.street;
		city = address.city;
		state = address.state;
		zip = address.zip;
		country = address.country;

		//get the globalID
		globalID = req.getParameter("globalID", "").replaceAll("\\D","");
		if (globalID.length() > 15) globalID = globalID.substring(0,15);
		if (globalID.length() == 0) {
			//The globalID was empty or illegal, create a new one based on the date/time
			globalID = StringUtil.makeNameFromDate();
			//Cut off the milliseconds
			int len = globalID.length();
			globalID = globalID.substring(0, len-3);
		}
		localID = req.getParameter("localID", ""); //for matching only
		localIDs = new LinkedList<LocalID>();
		localIDTable = new Hashtable<String,String>();
	}

	//A kludge to fix up an oddly entered date.
	//This only barely works.
	public static String filterDate(String date) {
		if (date == null) return "";
		if (date.indexOf("/") != -1) {
			String[] ss = date.split("/");
			if (ss.length != 3) return "";
			ss[0] = fix(ss[0],2);
			ss[1] = fix(ss[1],2);
			ss[2] = fix(ss[2],4);
			if (ss[2].startsWith("00")) {
				ss[2] = ((ss[2].charAt(2) == '0') ? "20" : "19") + ss[2].substring(2);
			}
			date = ss[2] + ss[0] + ss[1];
		}
		if (date.length() == 6) {
			date = date.substring(0,4)
					+ ((date.charAt(4) == '0') ? "20" : "19")
						+ date.substring(4);
		}
		if (date.length() == 8) {
			String s = date.substring(0,2);
			if (s.equals("19") || s.equals("20")) return fixMonthDay(date);
			else return fixMonthDay(date.substring(4) + date.substring(0,4));
		}
		else return "19460201";
	}
	private static String fix(String s, int n) {
		while (s.length() < n) s = "0" + s;
		return s;
	}
	private static int[] days = new int[] { 0,31,28,31,30,31,30,31,31,30,31,30,31 };
	private static String fixMonthDay(String d) {
		try {
			int year = Integer.parseInt(d.substring(0,4));
			int month = Integer.parseInt(d.substring(4,6));
			int day = Integer.parseInt(d.substring(6,8));
			if (month < 1) month = 1;
			if (month > 12) month = 12;
			if (day < 1) day = 1;
			if (day > days[month]) day = days[month];
			return fix(Integer.toString(year),4) + fix(Integer.toString(month),2) + fix(Integer.toString(day),2);
		}
		catch (Exception ex) { return "19460201"; }
	}

	//This constructor is used for PDQ searches
	public Registration(HttpRequest req) {
		givenName = req.getParameter("givenName", "");
		familyName = req.getParameter("familyName", "");
		street = req.getParameter("street", "");
		city = req.getParameter("city", "");
		state = req.getParameter("state", "");
		zip = req.getParameter("zip", "");
		country = req.getParameter("country", "");
		email = req.getParameter("email", "");
		birthdate = req.getParameter("birthdate", "");
		sex = req.getParameter("sex", "");
		globalID = req.getParameter("globalID", "");
		localID = req.getParameter("localID", ""); //for matching only
		localIDs = new LinkedList<LocalID>();
		localIDTable = new Hashtable<String,String>();
	}

	public Registration(Element reg) {
		givenName = getChildElementText(reg,"givenName");
		familyName = getChildElementText(reg,"familyName");
		street = getChildElementText(reg,"street");
		city = getChildElementText(reg,"city");
		state = getChildElementText(reg,"state");
		zip = getChildElementText(reg,"zip");
		country = getChildElementText(reg,"country");
		email = getChildElementText(reg,"email");
		birthdate = getChildElementText(reg,"birthdate");
		sex = getChildElementText(reg,"sex");
		globalID = getChildElementText(reg,"globalID");
		localIDs = new LinkedList<LocalID>();
		localIDTable = new Hashtable<String,String>();
		Node child = reg.getFirstChild();
		while (child != null) {
			if ((child.getNodeType() == Node.ELEMENT_NODE)
				 && child.getNodeName().equals("localID")) {
				LocalID localID = new LocalID((Element)child);
				localIDs.add(localID);
				localIDTable.put(localID.systemID,localID.localID);
			}
			child = child.getNextSibling();
		}
	}

	public String getFullName() {
		return givenName + " " + familyName;
	}

	public String getName() {
		return familyName + "^" + givenName;
	}

	public boolean matches(Registration reg) {
		if (!test(givenName, reg.givenName)) return false;
		if (!test(familyName, reg.familyName)) return false;
		if (!test(street, reg.street)) return false;
		if (!test(city, reg.city)) return false;
		if (!test(state, reg.state)) return false;
		if (!test(zip, reg.zip)) return false;
		if (!test(country, reg.country)) return false;
		if (!test(email, reg.email)) return false;
		if (!test(birthdate, reg.birthdate)) return false;
		if (!test(sex, reg.sex)) return false;
		if (!test(globalID, reg.globalID)) return false;
		ListIterator<LocalID> lit = localIDs.listIterator();
		while (lit.hasNext()) {
			if (!test(lit.next().localID, reg.localID)) return false;
		}
		return true;
	}

	private boolean test(String value, String substring) {
		if (substring.equals("")) return true;
		return (value.toLowerCase().indexOf(substring.toLowerCase()) != -1);
	}

	public void addLocalID(String systemID, String localID) {
		if (localIDTable.get(systemID) ==  null) {
			localIDs.add(new LocalID(systemID, localID));
			localIDTable.put(systemID,localID);
		}
	}

	public Element toElement(Document doc) {
		Element reg = doc.createElement("registration");
		reg.appendChild(makeElement(doc,"givenName",givenName));
		reg.appendChild(makeElement(doc,"familyName",familyName));
		reg.appendChild(makeElement(doc,"street",street));
		reg.appendChild(makeElement(doc,"city",city));
		reg.appendChild(makeElement(doc,"state",state));
		reg.appendChild(makeElement(doc,"zip",zip));
		reg.appendChild(makeElement(doc,"country",country));
		reg.appendChild(makeElement(doc,"email",email));
		reg.appendChild(makeElement(doc,"birthdate",birthdate));
		reg.appendChild(makeElement(doc,"sex",sex));
		reg.appendChild(makeElement(doc,"globalID",globalID));
		ListIterator<LocalID> lit = localIDs.listIterator();
		while (lit.hasNext()) reg.appendChild(lit.next().toElement(doc));
		return reg;
	}

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<registration>");
		sb.append(elementString("givenName",givenName));
		sb.append(elementString("familyName",familyName));
		sb.append(elementString("street",street));
		sb.append(elementString("city",city));
		sb.append(elementString("state",state));
		sb.append(elementString("zip",zip));
		sb.append(elementString("country",country));
		sb.append(elementString("email",email));
		sb.append(elementString("birthdate",birthdate));
		sb.append(elementString("sex",sex));
		sb.append(elementString("globalID",globalID));
		ListIterator<LocalID> lit = localIDs.listIterator();
		while (lit.hasNext()) {
			sb.append("<localID>");
			LocalID lid = lit.next();
			sb.append(elementString("systemID",lid.systemID));
			sb.append(elementString("localID",lid.localID));
			sb.append("</localID>");
		}
		sb.append("</registration>");
		return sb.toString();
	}

	private String elementString(String name, String value) {
		return "<"+name+">" + value + "</"+name+">";
	}

	public String toTable() {
		return toTable(true);
	}

	public String toTable(boolean showLocalIDs) {
		StringBuffer sb = new StringBuffer();
		sb.append("<p><table border=\"1\">");

		sb.append("<tr><td>Given Name</td><td>"+givenName+"</td></tr>");
		sb.append("<tr><td>Family Name</td><td>"+familyName+"</td></tr>");
		sb.append("<tr><td>Street Address</td><td>"+street+"</td></tr>");
		sb.append("<tr><td>City</td><td>"+city+"</td></tr>");
		sb.append("<tr><td>State</td><td>"+state+"</td></tr>");
		sb.append("<tr><td>Zip</td><td>"+zip+"</td></tr>");
		sb.append("<tr><td>Country</td><td>"+country+"</td></tr>");
		sb.append("<tr><td>Email Address</td><td>"+email+"</td></tr>");
		sb.append("<tr><td>Birth Date</td><td>"+birthdate+"</td></tr>");
		sb.append("<tr><td>Sex</td><td>"+sex+"</td></tr>");
		sb.append("<tr><td>Global ID</td><td>"+globalID+"</td></tr>");

		if (showLocalIDs) {
			sb.append("<tr><td>Local IDs</td><td><table>");
			ListIterator<LocalID> lit = localIDs.listIterator();
			while (lit.hasNext()) {
				LocalID lid = lit.next();
				sb.append("<tr><td>&nbsp;System ID:&nbsp;"+lid.systemID
							+"&nbsp;</td><td>&nbsp;Local ID:&nbsp;"+lid.localID+"&nbsp;</td></tr>");
			}
			sb.append("</table></td></tr>");
		}

		sb.append("</table></p>");
		return sb.toString();
	}

	class LocalID {
		public String systemID;
		public String localID;

		public LocalID(String systemID, String localID) {
			this.systemID = systemID;
			this.localID = localID;
		}

		public LocalID(Element element) {
			systemID = getChildElementText(element,"systemID");
			localID = getChildElementText(element,"localID");
		}

		public Element toElement(Document doc) {
			Element e = doc.createElement("localID");
			e.appendChild(makeElement(doc,"systemID",systemID));
			e.appendChild(makeElement(doc,"localID",localID));
			return e;
		}
	}

	private static Element makeElement(Document doc, String name, String value) {
		Element e = doc.createElement(name);
		e.appendChild(doc.createTextNode(value));
		return e;
	}

	//Find a child element and return its first child text node.
	//Note: this method is very specifically limited to the schema
	//of the registration element. Each element can have exactly
	//one child node and it must be a text node.
	private static String getChildElementText(Element element, String name) {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (child.getNodeName().equals(name)) {
					Node text = ((Element)child).getFirstChild();
					if (text != null) return text.getNodeValue().trim();
					else return "";
				}
			}
			child = child.getNextSibling();
		}
		return "";
	}
}
