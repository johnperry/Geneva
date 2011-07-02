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

public class DocSet extends ConfigElement {

	public String type = "DocSet";

	public String repositoryID;
	public String directory;
	public File directoryFile;
	public String title;
	public String date;
	public String institutionName;
	public String sex;

	public DocSet(Element el) {
		super(el);
		this.enabled = !el.getAttribute("enabled").trim().equals("no");
		this.repositoryID = el.getAttribute("repositoryID").trim();
		this.title = el.getAttribute("title").trim();
		this.date = el.getAttribute("date").trim();
		this.institutionName = el.getAttribute("institutionName").trim();
		this.sex = el.getAttribute("sex").trim().toUpperCase();
		if (sex.startsWith("M")) sex = "MALE";
		else if (sex.startsWith("F")) sex = "FEMALE";
		else sex = "BOTH";


		//Get the directory and set the directoryFile.
		this.directory = el.getAttribute("directory").trim();
		this.directoryFile = new File(directory);
	}

	public void appendTableRow(StringBuffer sb) {
		sb.append("<tr><td><b>DocSet</b><br>ID: "+id+"</td><td>");
		sb.append("<table border=\"0\" width=\"100%\">");
		sb.append("<tr><td width=\"165\">enabled:</td><td>"+(enabled?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Repository ID:</td><td>"+repositoryID+"</td></tr>");
		sb.append("<tr><td>Directory:</td><td>"+directory+"</td></tr>");
		if (!directoryFile.exists())
			sb.append(
				"<tr><td/><td><td><font color=\"red\">Directory does not exist</font></td></tr>");
		sb.append("<tr><td>Document Title:</td><td>"+title+"</td></tr>");
		sb.append("<tr><td>Date:</td><td>"+date+"</td></tr>");
		sb.append("<tr><td>Institution Name:</td><td>"+institutionName+"</td></tr>");
		sb.append("<tr><td>Sex:</td><td>"+sex+"</td></tr>");
		sb.append("</table>");
		sb.append("</td></tr>");
	}

	public String getType() {
		return type;
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("repositoryID", repositoryID);
			e.setAttribute("directory", directory);
			e.setAttribute("title", title);
			e.setAttribute("date", date);
			e.setAttribute("institutionName", institutionName);
			e.setAttribute("sex", sex);
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = ConfigElement.createNewElement(name, id);
			e.setAttribute("type", "DocSet");
			e.setAttribute("date", "*");
			e.setAttribute("sex", "BOTH");
			return e;
		}
		catch (Exception ex) { return null; }
	}

}

