/*---------------------------------------------------------------
*  Copyright 2010 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.servlets;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.elements.ConfigElement;
import org.rsna.server.HttpRequest;
import org.rsna.server.HttpResponse;
import org.rsna.servlets.Servlet;
import org.rsna.util.FileUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Configuration Editor Servlet.
 */
public class ConfigEditorServlet extends Servlet {

	static final Logger logger = Logger.getLogger(ConfigEditorServlet.class);

	public ConfigEditorServlet(File root, String context) {
		super(root, context);
	}

	public void doGet(HttpRequest req, HttpResponse res) {

		Configuration config = Configuration.getInstance();

		res.disableCaching();
		if (!isAuthorized(req, res)) return;
		String username = req.getUser().getUsername();

		String path = req.getPath().substring( ("/" + context).length() );

		if (path.equals("")) {
			//This is a request for the main editor page
			String page = FileUtil.getResourceText(getClass().getResource("/editor/editor.html"));
			Properties props = new Properties();
			String openpath = req.getParameter("openpath","");
			props.setProperty("openpath", openpath);
			page = StringUtil.replace(page, props);
			res.write(page);
			res.setContentType("html");
			res.send();
		}

		else if (path.equals("/tree")) {
			Document doc = null;
			try { doc = XmlUtil.getDocument(); }
			catch (Exception willNotHappen) { }

			Element root = doc.createElement("tree");
			doc.appendChild(root);
			Element x = appendCategory(root, "Server");
			x.setAttribute("sclickHandler", "openElement");

			x = appendCategory(root, "PIXMgr");
			appendElements(x, config.getPIXMgrs());

			x = appendCategory(root, "Registry");
			appendElements(x, config.getRegistries());

			x = appendCategory(root, "PDQMgr");
			appendElements(x, config.getPDQMgrs());

			x = appendCategory(root, "Repository");
			appendElements(x, config.getRepositories());

			x = appendCategory(root, "EHRSystem");
			appendElements(x, config.getEHRSystems());

			x = appendCategory(root, "DCMSystem");
			appendElements(x, config.getDCMSystems());

			x = appendCategory(root, "Study");
			appendElements(x, config.getStudies());

			x = appendCategory(root, "DocSet");
			appendElements(x, config.getDocSets());

			res.write(XmlUtil.toString(root));
			res.setContentType("xml");
			res.send();
		}

		else if (path.equals("/openElement")) {
			path = req.getParameter("path", "");
			if (path.equals("Server")) {
				Element el = config.getServerXML();
				res.write(XmlUtil.toString(el));
			}
			else {
				path = path.substring( path.lastIndexOf("/") + 1 );
				ConfigElement ce = config.getConfigElement(path);
				res.write(XmlUtil.toString(ce.getXML()));
			}
			res.setContentType("xml");
			res.send();
		}

		else if (path.equals("/newElement")) {
			String type = req.getParameter("type", "");
			Element el = config.createNewElement(req.getUser().getUsername(), type);
			res.write(XmlUtil.toString(el));
			res.setContentType("xml");
			res.send();
		}

		else if (path.equals("/save")) {
			res.setContentType("txt");
			String result= "OK";
			String xml = req.getParameter("xml", "").trim();
			if (!xml.equals("")) {
				try {
					Document doc = XmlUtil.getDocument(xml);
					Element el = doc.getDocumentElement();
					boolean isAdmin = req.userHasRole("admin");
					if (isAdmin ||
						(!el.getTagName().equals("config")
							&& el.getAttribute("id").contains("_" + username + "_"))) {

						result = config.saveElement(username, el);
					}
					else {
						res.write("You are not authorized to save the current element.");
						res.send();
						return;
					}
				}
				catch (Exception ex) {
					res.write(ex.getMessage());
					res.send();
					return;
				}
			}
			if (result.equals("OK")) {
				result = config.saveAs(username, null);
				res.write(result);
				res.send();
			}
		}

		else if (path.equals("/deleteElement")) {
			path = req.getParameter("path", "");
			path = path.substring( path.lastIndexOf("/") + 1 );
			res.setContentType("txt");
			boolean isAdmin = req.userHasRole("admin");
			if (isAdmin || (path.contains("_" + username + "_"))) {
				String result = config.deleteElement(username, path);
				if (result.equals("OK")) result = config.saveAs(username, null);
				res.write(result);
			}
			else res.write("You are not authorized to delete "+path);
			res.send();
		}

		else super.doGet(req, res);
	}

	private boolean isAuthorized(HttpRequest req, HttpResponse res) {
		if (!req.userHasRole("admin") && !req.userHasRole("company")) {
			res.setResponseCode(res.notfound);
			res.send();
			return false;
		}
		return true;
	}

	//Add a category node to a tree
	private Element appendCategory(Node parent, String title) {
		Element el = parent.getOwnerDocument().createElement("node");
		el.setAttribute("name", title);
		parent.appendChild(el);
		return el;
	}

	//Add an array of ConfigElements to a tree
	private void appendElements(Node parent, ConfigElement[] elements) {
		try {
			Document doc = parent.getOwnerDocument();
			for (int i=0; i<elements.length; i++) {
				Element el = doc.createElement("node");
				el.setAttribute("name", elements[i].id);
				el.setAttribute("sclickHandler", "openElement");
				parent.appendChild(el);
			}
		}
		catch (Exception skip) { }
	}

}

