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

public class Study extends ConfigElement {

	public String type = "Study";

	public String directory;
	public File directoryFile;
	public String systemID;
	public String date;
	public String description;
	public String bodyPartExamined;
	//Requested Procedure Code Seq element values
	public String rpcsCodeValue;
	public String rpcsCodingSchemeDesignator;
	public String rpcsCodingSchemeVersion;
	public String rpcsCodeMeaning;
	//Anatomic Region Seq element values
	public String arcsCodeValue;
	public String arcsCodingSchemeDesignator;
	public String arcsCodingSchemeVersion;
	public String arcsCodeMeaning;
	//ORM attributes
	public String placerOrderAuthority;
	public String fillerOrderAuthority;
	public String enteringOrganization;
	public String procedureCode;
	public String localProcedureCode;

	public Study(Element el) {
		super(el);
		this.enabled = !el.getAttribute("enabled").trim().equals("no");
		this.systemID = el.getAttribute("dcmsystemID").trim();
		this.directory = el.getAttribute("directory").trim();
		this.directoryFile = new File(directory);
		this.date = el.getAttribute("date").trim();
		this.description = el.getAttribute("description").trim();
		this.bodyPartExamined = el.getAttribute("bodyPartExamined").trim();
		//Requested Procedure Code Seq element values
		this.rpcsCodeValue = el.getAttribute("rpcsCodeValue").trim();
		this.rpcsCodingSchemeDesignator = el.getAttribute("rpcsCodingSchemeDesignator").trim();
		this.rpcsCodingSchemeVersion = el.getAttribute("rpcsCodingSchemeVersion").trim();
		this.rpcsCodeMeaning = el.getAttribute("rpcsCodeMeaning").trim();
		//Anatomic Region Seq element values
		this.arcsCodeValue = el.getAttribute("arcsCodeValue").trim();
		this.arcsCodingSchemeDesignator = el.getAttribute("arcsCodingSchemeDesignator").trim();
		this.arcsCodingSchemeVersion = el.getAttribute("arcsCodingSchemeVersion").trim();
		this.arcsCodeMeaning = el.getAttribute("arcsCodeMeaning").trim();
		//ORM attributes
		this.placerOrderAuthority = el.getAttribute("placerOrderAuthority").trim();
		this.fillerOrderAuthority = el.getAttribute("fillerOrderAuthority").trim();
		this.enteringOrganization = el.getAttribute("enteringOrganization").trim();
		this.procedureCode = el.getAttribute("procedureCode").trim();
		this.localProcedureCode = el.getAttribute("localProcedureCode").trim();
	}

	public void appendTableRow(StringBuffer sb) {
		sb.append("<tr><td><b>Study</b><br>ID: "+id+"</td><td>");
		sb.append("<table border=\"0\" width=\"100%\">");
		sb.append("<tr><td width=\"165\">enabled:</td><td>"+(enabled?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>DICOM System ID:</td><td>"+systemID+"</td></tr>");
		sb.append("<tr><td>Directory:</td><td>"+directory+"</td></tr>");
		sb.append("<tr><td>Number of files:</td>");
		if (directoryFile.exists())
			sb.append("<td>"+getFileCount(directoryFile)+"</td>");
		else
			sb.append("<td><font color=\"red\">Directory does not exist</font></td>");
		sb.append("</tr>");
		sb.append("<tr><td>Date:</td><td>"+date+"</td></tr>");
		sb.append("<tr><td>Description:</td><td>"+description+"</td></tr>");
		//Requested Procedure Code Seq element values
		sb.append("<tr><td>Req Proc Code Value:</td><td>"+rpcsCodeValue+"</td></tr>");
		sb.append("<tr><td>Req Proc Code Scheme:</td><td>"+rpcsCodingSchemeDesignator+"</td></tr>");
		sb.append("<tr><td>Req Proc Code Scheme Version:</td><td>"+rpcsCodingSchemeVersion+"</td></tr>");
		sb.append("<tr><td>Req Proc Code Meaning:</td><td>"+rpcsCodeMeaning+"</td></tr>");
		//Anatomic Region Seq element values
		sb.append("<tr><td>Anat Reg Code Value:</td><td>"+arcsCodeValue+"</td></tr>");
		sb.append("<tr><td>Anat Reg Code Scheme:</td><td>"+arcsCodingSchemeDesignator+"</td></tr>");
		sb.append("<tr><td>Anat Reg Code Scheme Version:</td><td>"+arcsCodingSchemeVersion+"</td></tr>");
		sb.append("<tr><td>Anat Reg Code Meaning:</td><td>"+arcsCodeMeaning+"</td></tr>");
		//ORM attributes
		sb.append("<tr><td>Placer Order Authority:</td><td>"+placerOrderAuthority+"</td></tr>");
		sb.append("<tr><td>Filler Order Authority:</td><td>"+fillerOrderAuthority+"</td></tr>");
		sb.append("<tr><td>Entering Organization:</td><td>"+enteringOrganization+"</td></tr>");
		sb.append("<tr><td>Procedure Code:</td><td>"+procedureCode+"</td></tr>");
		sb.append("<tr><td>Local Procedure Code:</td><td>"+localProcedureCode+"</td></tr>");
		sb.append("</table>");
		sb.append("</td></tr>");
	}

	private int getFileCount(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			int count = 0;
			for (int i=0; i<files.length; i++) count += getFileCount(files[i]);
			return count;
		}
		return 1;
	}

	public String getType() {
		return type;
	}

	public Element getXML() {
		try {
			Element e = super.getXML();

			e.setAttribute("directory", directory);
			e.setAttribute("dcmsystemID", systemID);
			e.setAttribute("date", date);
			e.setAttribute("description", description);
			e.setAttribute("bodyPartExamined", bodyPartExamined);

			e.setAttribute("rpcsCodeValue", rpcsCodeValue);
			e.setAttribute("rpcsCodingSchemeDesignator", rpcsCodingSchemeDesignator);
			e.setAttribute("rpcsCodingSchemeVersion", rpcsCodingSchemeVersion);
			e.setAttribute("rpcsCodeMeaning", rpcsCodeMeaning);

			e.setAttribute("arcsCodeValue", arcsCodeValue);
			e.setAttribute("arcsCodingSchemeDesignator", arcsCodingSchemeDesignator);
			e.setAttribute("arcsCodingSchemeVersion", arcsCodingSchemeVersion);
			e.setAttribute("arcsCodeMeaning", arcsCodeMeaning);

			e.setAttribute("placerOrderAuthority", placerOrderAuthority);
			e.setAttribute("fillerOrderAuthority", fillerOrderAuthority);
			e.setAttribute("enteringOrganization", enteringOrganization);
			e.setAttribute("procedureCode", procedureCode);
			e.setAttribute("localProcedureCode", localProcedureCode);

			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = ConfigElement.createNewElement(name, id);
			e.setAttribute("type", "Study");
			e.setAttribute("date", "*");
			return e;
		}
		catch (Exception ex) { return null; }
	}

}


