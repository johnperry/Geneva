/*---------------------------------------------------------------
*  Copyright 2010 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.servlets;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.server.User;
import org.rsna.servlets.Servlet;
import org.rsna.geneva.elements.ConfigElement;
import org.rsna.geneva.main.Configuration;

/**
 * The ControlPanelServlet. This implementation allows a user
 * to control the enables of the elements in the configuration..
 */
public class ControlPanelServlet extends Servlet {

	static final Logger logger = Logger.getLogger(ControlPanelServlet.class);

	public ControlPanelServlet(File root, String context) {
		super(root, context);
	}

	//The GET handler.
	public void doGet(HttpRequest req, HttpResponse res) {
		res.disableCaching();
		User user = req.getUser();
		if (user != null) {
			res.write(getPage(user.getUsername(), req.userHasRole("admin")));
			res.setContentType("html");
		}
		else res.setResponseCode(res.notfound);
		res.send();
	}

	//The POST handler
	public void doPost(HttpRequest req, HttpResponse res) {
		User user = req.getUser();
		if (user == null) {
			res.setResponseCode(res.notfound);
			res.send();
		}
		String userString = "_" + user.getUsername() + "_";
		boolean isAdmin = user.hasRole("admin");
		Configuration config = Configuration.getInstance();
		Properties props = config.getProperties();
		for (String id : props.stringPropertyNames()) {
			if (isAdmin || id.contains(userString)) {
				String reqProp = req.getParameter(id);
				boolean reqEnb = (reqProp != null);
				boolean configEnb = props.getProperty(id, "true").equals("true");
				if (configEnb != reqEnb) {
					props.setProperty(id, ""+reqEnb);
					config.setChanged(true);
				}
				ConfigElement ce = config.getConfigElement(id);
				if (ce != null) {
					ce.setEnable(reqEnb);
				}
			}
		}
		doGet(req, res);
	}

	//Make a page showing the important data.
	private String getPage(String username, boolean isAdmin) {
		Configuration config = Configuration.getInstance();
		StringBuffer sb = new StringBuffer();
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<title>Geneva Control Panel</title>\n");
		sb.append("<style>\n");
		sb.append("body {background-color: #c6d8f9;}\n");
		sb.append("td {vertical-align: top; padding: 10px;}\n");
		sb.append("h1 {font-family: sans-serif; font-size: 12pt;}\n");
		sb.append(".button {width: 100px;}\n");
		sb.append(".selectable {color:black; font-family: sans-serif; font-weight:bold;}\n");
		sb.append(".notselectable {color:gray; font-family: sans-serif; font-weight:normal;}\n");
		sb.append("</style>\n");
		sb.append("</head><body>\n");

		sb.append("<form id=\"f1\" method=\"post\" target=\"_self\" action=\"\" accept-charset=\"UTF-8\">\n");

		sb.append("<table border=\"0\" width=\"100%\">\n");
		sb.append("<tr>\n");
		sb.append("<td><h1>Geneva Control Panel</h1></td>\n");
		sb.append("<td style=\"text-align:right\">\n");
		sb.append("<input class=\"button\" type=\"button\" value=\"Home\" onclick=\"window.open('/','_self');\"/>\n");
		sb.append("<br>");
		sb.append("<input class=\"button\" type=\"submit\" value=\"Update\">");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("</table>\n");

		sb.append("<table border=\"1\">\n");

		sb.append("<tr>\n");
		sb.append("<td><h1>PIX Managers</h1></td>\n");
		sb.append("<td><h1>Registries</h1></td>\n");
		sb.append("<td><h1>PDQ Managers</h1></td>\n");
		sb.append("<td><h1>Repositories</h1></td>\n");
		sb.append("<td><h1>EHR Systems</h1></td>\n");
		sb.append("<td><h1>DCM Systems</h1></td>\n");
		sb.append("<td><h1>Studies</h1></td>\n");
		sb.append("<td><h1>DocSets</h1></td>\n");
		sb.append("<td><h1>Messages</h1></td>\n");
		sb.append("</tr>\n");

		sb.append("<tr>\n");

		sb.append("<td>");
		sb.append(getCell(config.getPIXMgrIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getRegistryIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getPDQMgrIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getRepositoryIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getEHRSystemIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getDCMSystemIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getStudyIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getDocSetIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("<td>");
		sb.append(getCell(config.getMessageIDs(), username, isAdmin));
		sb.append("</td>");

		sb.append("</tr>\n");
		sb.append("</table>\n");

		sb.append("</form>\n");

		sb.append("</body></html>");
		return sb.toString();
	}

	private StringBuffer getCell(String[] ids, String username, boolean isAdmin) {
		Configuration config = Configuration.getInstance();
		StringBuffer sb = new StringBuffer();
		String userString = "_" + username + "_";
		for (int i=0; i<ids.length; i++) {
			ConfigElement ce = config.getConfigElement(ids[i]);
			if (ce != null) {
				boolean enb = isAdmin || ids[i].contains(userString);
				sb.append("<input name=\"" + ids[i] + "\" type=\"checkbox\"");
				if (!enb) sb.append(" disabled");
				if (ce.enabled) sb.append(" checked");
				sb.append(">&nbsp;&nbsp;&nbsp;");
				if (enb) sb.append("<span class=\"selectable\">" + ids[i] + "</span><br>");
				else sb.append("<span class=\"notselectable\">" + ids[i] + "</span><br>");
			}
		}
		return sb;
	}

}
