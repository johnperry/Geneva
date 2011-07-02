/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.servlets;

import java.io.*;
import org.apache.log4j.Logger;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.servlets.Servlet;
import org.rsna.geneva.main.Configuration;

/**
 * The ConfigurationServlet. This implementation simply returns the
 * system configuration as an HTML page.
 */
public class ConfigurationServlet extends Servlet {

	static final Logger logger = Logger.getLogger(ConfigurationServlet.class);

	public ConfigurationServlet(File root, String context) {
		super(root, context);
	}

	//The GET handler.
	public void doGet(HttpRequest req, HttpResponse res) {
		res.disableCaching();
		res.write(getPage(req.getParameter("filter", "")));
		res.setContentType("html");
		res.send();
	}

	//The POST handler
	public void doPost(HttpRequest req, HttpResponse res) {
		//Do the same as a GET
		doGet(req,res);
	}

	//Make a page showing the important data.
	private String getPage(String filter) {
		Configuration config = Configuration.getInstance();
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><title>Geneva Configuration</title></head><body>");

		sb.append("<table border=\"0\" width=\"100%\">");
		sb.append("<tr>");
		sb.append("<td><h1 style=\"font-family:sans-serif\">Geneva Configuration</h1></td>");
		sb.append("<td style=\"text-align:right\">");
		sb.append("<form id=\"f1\" method=\"post\" target=\"_self\" action=\"\" accept-charset=\"UTF-8\">");
		sb.append("<input type=\"submit\" value=\"Filter:\">");
		sb.append("&nbsp;&nbsp;");
		sb.append("<input type=\"text\" name=\"filter\" value=\""+filter.replace("%20"," ")+"\"/>");
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		sb.append("<input type=\"button\" name=\"filter\" value=\"Home\" onclick=\"window.open('/','_self');\"/>");
		sb.append("</form>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");

		sb.append("<p>Configuration file: "+config.getFile().getName()+".</p>");
		sb.append("<p>");
		sb.append(config.getTable(filter.replace("%20"," ")));
		sb.append("</p>");
		sb.append("</body></html>");
		return sb.toString();
	}

}

