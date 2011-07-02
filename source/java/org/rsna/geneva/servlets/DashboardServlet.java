/*---------------------------------------------------------------
*  Copyright 2010 by the Radiological Society of North America
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
 * The DashboardServlet. This implementation simply returns the
 * current contents of the Geneva event log as an HTML page.
 */
public class DashboardServlet extends Servlet {

	static final Logger logger = Logger.getLogger(DashboardServlet.class);

	public DashboardServlet(File root, String context) {
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
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<title>Dashboard</title>");

		sb.append("<script>\n");
		sb.append("function scroll() {\n");
		sb.append("var scrollPoint = document.getElementById('here');\n");
		sb.append("if (scrollPoint && scrollPoint.scrollIntoView) scrollPoint.scrollIntoView();\n");
		sb.append("}");
		sb.append("window.onload = scroll;\n");
		sb.append("</script\n");

		sb.append("</head><body>\n");

		sb.append("<table border=\"0\" width=\"100%\">\n");
		sb.append("<tr>\n");
		sb.append("<td><h1 style=\"font-family:sans-serif\">Geneva Dashboard</h1></td>\n");
		sb.append("<td style=\"text-align:right\">\n");
		sb.append("<form id=\"f1\" method=\"post\" target=\"_self\" action=\"\" accept-charset=\"UTF-8\">\n");
		sb.append("<input type=\"submit\" value=\"Filter:\">");
		sb.append("&nbsp;&nbsp;");
		sb.append("<input type=\"text\" name=\"filter\" value=\""+filter.replace("%20"," ")+"\"/>");
		sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		sb.append("<input type=\"button\" name=\"filter\" value=\"Home\" onclick=\"window.open('/','_self');\"/>\n");
		sb.append("</form>\n");
		sb.append("</td>\n");
		sb.append("</tr>\n");
		sb.append("</table>\n");

		sb.append("<p>Number of registrations: "+config.getRegistrationDatabase().size()+".</p>");
		sb.append("<p>");
		sb.append(config.getEventLog().getTable(filter.replace("%20"," ")));
		sb.append("</p>");
		sb.append("</body></html>");
		return sb.toString();
	}

}

