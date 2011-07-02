/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.servlets;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.servlets.Servlet;
import org.rsna.geneva.elements.*;
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.misc.RegistrationDatabase;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.geneva.main.Configuration;
import org.rsna.util.StringUtil;

/**
 * The RegistrationServlet.
 */
public class RegistrationServlet extends Servlet {

	static final Logger logger = Logger.getLogger(RegistrationServlet.class);

	public RegistrationServlet(File root, String context) {
		super(root, context);
	}

	//Handle a registration event (POST)
	public void doPost(HttpRequest req, HttpResponse res) {

		Configuration config = Configuration.getInstance();

		//Log this event in the server's event log.
		logEvent(req);

		//Get the contents of the form as a Registration
		Registration reg = new Registration(req, config);

		//Build the result page.
		res.write("<html><head>");
		res.write("<title>Geneva Registration</title>");
		res.write("<link rel=\"stylesheet\" href=\"/styles.css\" type=\"text/css\"/>");
		res.write("</head><body>");
		res.write("<center>");
		res.write("<img src=\"/masthead.jpg\"/>");
		res.write("<table class=\"header\"><tr>");
		res.write("<td class=\"left\"><a href=\"/\">Home</a></td>");
		res.write("<td class=\"right\"><a href=\"/registration\">New Registration</a></td>");
		res.write("</tr></table>");
		res.write("<h1>Geneva Registration</h1>");

		//Vet the input.
		String x = validateInput(reg);
		if (!x.equals("")) {
			res.write(x + "</center></body><html>");
			send(res);
			return;
		}

		//See if the registration already exists.
		RegistrationDatabase rdb = config.getRegistrationDatabase();
		Registration regx = rdb.lookup(reg.globalID);
		if (regx != null) {
			res.write("<p>A registration already exists for "+reg.globalID+".</p>");
			res.write(regx.toTable());
			res.write("</center></body><html>");
			send(res);
			return;
		}

		//Not a duplicate; set the local IDs.
		Hashtable<String,String> idgroups = new Hashtable<String,String>();
		DCMSystem[] dcmsystems = config.getDCMSystems();
		for (int i=0; i<dcmsystems.length; i++) {
			String localAssigningAuthority = dcmsystems[i].localAssigningAuthority;
			String localID = idgroups.get(localAssigningAuthority);
			if (localID == null) {
				localID = config.getLocalID();
				idgroups.put(localAssigningAuthority,localID);
			}
			reg.addLocalID(dcmsystems[i].id,localID);
		}
		EHRSystem[] ehrsystems = config.getEHRSystems();
		for (int i=0; i<ehrsystems.length; i++) {
			String localAssigningAuthority = ehrsystems[i].localAssigningAuthority;
			String localID = idgroups.get(localAssigningAuthority);
			if (localID == null) {
				localID = config.getLocalID();
				idgroups.put(localAssigningAuthority,localID);
			}
			reg.addLocalID(ehrsystems[i].id,localID);
		}

		//Add the registration to the database and send the
		//response page. This is done before the processing
		//actually takes place since that is asynchronous.
		rdb.add(reg);
		res.write("<p>"+reg.getFullName() + " has been registered and queued for processing.</p>");
		boolean showLocalIDs = req.getParameter("localIDs", "no").equals("yes");
		res.write(reg.toTable(showLocalIDs));
		res.write("</body><html>");
		send(res);

		//Start up all the processing threads.
		startProcessingThreads(reg, config.getPIXMgrs(), Thread.NORM_PRIORITY);
		startProcessingThreads(reg, config.getRegistries(), Thread.MAX_PRIORITY);
		startProcessingThreads(reg, config.getPDQMgrs(), Thread.NORM_PRIORITY);
		startProcessingThreads(reg, config.getRepositories(), Thread.NORM_PRIORITY);
		startProcessingThreads(reg, config.getEHRSystems(), Thread.MIN_PRIORITY);
		startProcessingThreads(reg, config.getDCMSystems(), Thread.MIN_PRIORITY);
	}

	private void startProcessingThreads(Registration reg, Product[] systems, int priority) {
		for (int i=0; i<systems.length; i++) {
			systems[i].process(reg, priority);
		}
	}

	private void send(HttpResponse res) {
		res.disableCaching();
		res.setContentType("html");
		res.send();
	}

	private String validateInput(Registration reg) {
		StringBuffer sb = new StringBuffer();
		if (reg.globalID.equals("")) {
			sb.append("<p>There must be at least one numeric digit in the registration number.</p>");
		}
		if (reg.familyName.equals("")) {
			sb.append("<p>The Family Name field is blank.</p>");
		}
		return sb.toString();
	}

	//Log an event that contains the request information.
	public void logEvent(HttpRequest req) {
		RegSysEvent event =
			new RegSysEvent(
					this,
					RegSysEvent.STATUS_OK,
					RegSysEvent.TYPE_HTTP,
					req.method + " " + req.path
					+ (!req.query.equals("") ? "?"+req.query : "")
					+ "<br>"+req.getContentType()
					+ (req.method.equals("POST") ? "<br>"+StringUtil.displayable(req.content) : "")
				);
		Configuration.getInstance().getEventLog().append(event);
	}

}

