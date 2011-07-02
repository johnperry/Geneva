/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.rsna.util.XmlUtil;

public class RegistrationDatabase {

	static final Logger logger = Logger.getLogger(RegistrationDatabase.class);
    public static final String regdbFN = "registrations.xml";

	public LinkedList<Registration> regs;
	public Hashtable<String,Registration> index;
	public File file;

	public RegistrationDatabase() {
		file = new File(regdbFN);
		this.regs = new LinkedList<Registration>();
		this.index = new Hashtable<String,Registration>();
		if (file.exists()) {
			try {
				Document doc = XmlUtil.getDocument(file);
				Element root = doc.getDocumentElement();
				Node child = root.getFirstChild();
				while (child != null) {
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						Element elem = (Element)child;
						if (elem.getNodeName().equals("registration")) {
							add(new Registration(elem));
						}
					}
					child = child.getNextSibling();
				}
			}
			catch (Exception ex) {
				logger.error("Unable to load the registration database.",ex);
			}
		}
	}

	public int size() {
		return regs.size();
	}

	public void add(Registration reg) {
		if (index.get(reg.globalID) == null) regs.add(reg);
		index.put(reg.globalID,reg);
	}

	public Registration lookup(String globalID) {
		return index.get(globalID);
	}

	public boolean save() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			osw.write("<registrations>\n");

			ListIterator<Registration> lit = regs.listIterator();
			while (lit.hasNext()) osw.write(lit.next().toXMLString());;

			osw.write("</registrations>\n");
			osw.flush();
			osw.close();
			return true;
		}
		catch (Exception ex) { return false; }
	}

	public String[] getTablesFor(Registration match) {
		ArrayList<String> list = new ArrayList<String>();
		Registration reg;
		ListIterator<Registration> lit = regs.listIterator();
		while (lit.hasNext()) {
			reg = lit.next();
			if (reg.matches(match)) list.add(reg.toTable());
		}
		String[] results = new String[list.size()];
		return list.toArray(results);
	}

}