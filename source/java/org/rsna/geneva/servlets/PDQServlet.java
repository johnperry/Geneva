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
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.main.Configuration;

/**
 * The Patient Demographic Query Servlet.
 */
public class PDQServlet extends Servlet {

	static final Logger logger = Logger.getLogger(PDQServlet.class);

	public PDQServlet(File root, String context) {
		super(root, context);
	}

	//Handle a registration event (POST)
	public void doPost(HttpRequest req, HttpResponse res) {
		//Get the contents of the form as a Registration
		//for matching against the RegistrationDatabase.
		Registration match = new Registration(req);

		StringBuffer sb = new StringBuffer();

		res.write("<html><head>");
		res.write("<title>Geneva Patient Demographic Query</title>");
		res.write("<link rel=\"stylesheet\" href=\"/styles.css\" type=\"text/css\"/>");
		res.write("</head><body>");
		res.write("<center>");
		res.write("<img src=\"/masthead.jpg\"/>");
		res.write("<table class=\"header\"><tr>");
		res.write("<td class=\"left\"><a href=\"/\">Home</a></td>");
		res.write("<td class=\"right\"><a href=\"/registration\">New Registration</a></td>");
		res.write("</tr></table>");
		sb.append("<h1>Geneva Patient Demographic Query</h1>");
		res.write("</center>");

		//Get the tables for all the registrations that match the form.
		String[] tables = Configuration.getInstance().getRegistrationDatabase().getTablesFor(match);

		if (tables.length == 0)
			sb.append("<p class=\"center\">No matches found.</p>");
		else {
			for (int i=0; i<tables.length; i++) {
				sb.append("<center>");
				sb.append(tables[i]);
				sb.append("</center>");
			}
		}

		sb.append("</body><html>");

		res.disableCaching();
		res.write(sb.toString());
		res.setContentType("html");
		res.send();
	}

}

