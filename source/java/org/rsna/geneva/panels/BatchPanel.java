/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.panels;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.misc.RegistrationDatabase;
import org.rsna.geneva.elements.Product;
import org.rsna.util.FileUtil;
import org.rsna.util.XmlUtil;
import org.rsna.geneva.elements.EHRSystem;
import org.rsna.geneva.elements.DCMSystem;


/**
 * A JPanel to provide a user interface for processing registrations from an XML file..
 */
public class BatchPanel extends JPanel implements ActionListener {

	static final Logger logger = Logger.getLogger(BatchPanel.class);

	JFileChooser chooser = null;
	JTextPane text;
	JButton selectButton;
	JTextField intervalField;

    /**
     * Class constructor; creates a user interface and processes batch registrations from an XML file..
     */
    public BatchPanel() {
		super();
		this.setLayout(new BorderLayout());
		JPanel top = new JPanel();
		selectButton = new JButton("Select Batch File");
		selectButton.addActionListener(this);
		top.add(selectButton);
		top.add(Box.createHorizontalStrut(15));
		top.add(new JLabel("Interval (sec): "));
		intervalField = new JTextField("10",5);
		top.add(intervalField);
		this.add(top, BorderLayout.NORTH);
		text = new JTextPane();
		text.setContentType("text/html");
		JScrollPane jsp = new JScrollPane();
		jsp.setViewportView(text);
		this.add(jsp, BorderLayout.CENTER);
    }

	public void actionPerformed(ActionEvent event) {
		int interval;
		if (chooser == null) {
			File here = new File(System.getProperty("user.dir"));
			chooser = new JFileChooser(here);
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File regFile = chooser.getSelectedFile();
			Document regXML = getRegFileAsXML(regFile);
			if (regXML != null) {
				selectButton.setEnabled(false);
				try { interval = Math.max(Integer.parseInt(intervalField.getText()),0); }
				catch (Exception oops) { interval = 10; }
				intervalField.setText(""+interval);
				RegistrationProcessorThread rpt = new RegistrationProcessorThread(regXML, interval);
				rpt.start();
			}
			else {
				text.setText("<font color=\"red\">Unable to load the registration file: "+regFile+"</font>");
				return;
			}
		}
	}

	private Document getRegFileAsXML(File regFile) {
		//First try to parse the file as XML
		// This will emit "Contents not allowed in prolog" error to stdout
		// if the file is not xml.  This error can be ignored.
		try { return XmlUtil.getDocument(regFile); }
		catch (Exception ex) { }

		//That didn't work; now try it as CSV.
		//First, get the column properties.
		Properties columns = getColumns();
		if (columns == null) return null;

		BufferedReader br;
		try {
			//Create the output Document and set the root element
			Document doc = XmlUtil.getDocument();
			Element root = doc.createElement("registrations");
			doc.appendChild(root);

			//Get the file
			br = new BufferedReader(new InputStreamReader(new FileInputStream(regFile), FileUtil.latin1));
			//Get the first line and ignore it
			String line = br.readLine();
			//Now do the rest
			while ((line = br.readLine()) != null) {
				Hashtable<String,String> dataTable = getDataTable(line);
				Element reg = getRegistration(root, dataTable, columns);
				if (reg != null) root.appendChild(reg);
			}
			br.close();
			return doc;
		}
		catch (Exception ex) { logger.warn("Exception in CSV processing",ex); }
		return null;
	}

	private Properties getColumns() {
		Properties columns = new Properties();
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File("columns.properties"));
			columns.load(stream);
			stream.close();
			return columns;
		}
		catch (Exception ex) {
			if (stream != null) {
				try { stream.close(); }
				catch (Exception ignore) { }
			}
		}
		return null;
	}

	private Hashtable<String,String> getDataTable(String line) {
		Hashtable<String,String> dt = new Hashtable<String,String>();
		int k=0;
		int fieldNumber = 0;
		while (k<line.length()) {
			StringBuffer sb = new StringBuffer();
			char c = line.charAt(k);
			boolean quotedString = (c == '"');
			boolean quote = false;
			if (quotedString) k++;
			while (k < line.length()) {
				c = line.charAt(k++);
				if (c == '"') {
					if (quote) sb.append(c);
					quote = !quote;
				}
				else if (c != ',') sb.append(c);
				else if (quotedString & !quote) sb.append(c);
				else break;
			}
			dt.put(getFieldKey(fieldNumber++),(sb.toString()));
		}
		return dt;
	}

	String digits = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String getFieldKey(int n) {
		int d1 = n % 26;
		int d2 = n / 26 - 1 ;
		String s1 = digits.substring(d1, d1+1);
		if (d2 >= 0) return digits.substring(d2, d2+1) + s1;
		return s1;
	}

	private Element getRegistration(Element root, Hashtable<String,String> dataTable, Properties columns) {
		Document doc = root.getOwnerDocument();
		Element reg = doc.createElement("registration");
		appendElement("globalID",columns,dataTable,reg);
		appendElement("givenName",columns,dataTable,reg);
		appendElement("familyName",columns,dataTable,reg);

		String defaultEmail =
				columns.getProperty("givenName")
					+ "."
						+ columns.getProperty("familyName")
							+ "@ihe.org";
		fixEmail(appendElement("email",columns,dataTable,reg), defaultEmail);

		fixBirthdate(appendElement("birthdate",columns,dataTable,reg));
		fixSex(appendElement("sex",columns,dataTable,reg));
		appendElement("street",columns,dataTable,reg);
		appendElement("city",columns,dataTable,reg);
		appendElement("state",columns,dataTable,reg);
		fixCountry(appendElement("country",columns,dataTable,reg), columns.getProperty("defaultCountry"));
		appendElement("zip",columns,dataTable,reg);
		return reg;
	}

	private void logElement(Element el) {
		try {
			Node n = el.getFirstChild();
			while (n != null) {
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					logger.info(n.getNodeName() + " = " + n.getTextContent());
				}
				n = n.getNextSibling();
			}
			logger.info("-------------------------------");
		}
		catch (Exception ex) { }
	}

	private Element appendElement(String name, Properties columns, Hashtable<String,String> dataTable, Element el) {
		try {
			String column = columns.getProperty(name);
			if (column != null) {
				String value = dataTable.get(column);
				Element child = el.getOwnerDocument().createElement(name);
				child.setTextContent(value);
				el.appendChild(child);
				return child;
			}
		}
		catch (Exception skip) { }
		return null;
	}

	private void fixBirthdate(Element el) {
		if (el == null) return;
		try {
			String s = el.getTextContent();
			s = Registration.filterDate(s);
			el.setTextContent(s);
		}
		catch (Exception ex) { }
	}

	private void fixSex(Element el) {
		try {
			String s = el.getTextContent();
			s = s.toUpperCase().trim();
			if (s.startsWith("F")) s = "F";
			else s = "M";
			el.setTextContent(s);
		}
		catch (Exception ex) { }
	}

	private void fixCountry(Element el, String defaultCountry) {
		if (defaultCountry == null) return;
		try {
			String s = el.getTextContent();
			if (s.trim().equals("")) el.setTextContent(defaultCountry);
		}
		catch (Exception ex) { }
	}

	private void fixEmail(Element el, String emailAddress) {
		if (emailAddress == null) return;
		try {
			String s = el.getTextContent();
			if (s.trim().equals("")) el.setTextContent(emailAddress);
		}
		catch (Exception ex) { }
	}

	public void setText(String s) {
		final String fs = s;
		Runnable runner = new Runnable() {
			public void run() {
				text.setText(fs);
			}
		};
		SwingUtilities.invokeLater(runner);
	}

	public void done() {
		Runnable runner = new Runnable() {
			public void run() {
				selectButton.setEnabled(true);
			}
		};
		SwingUtilities.invokeLater(runner);
	}

	class RegistrationProcessorThread extends Thread {
		Document regXML;
		int interval = 10;
		int nRegs = 0;

		public RegistrationProcessorThread(Document regXML, int interval) {
			this.regXML = regXML;
			this.interval = interval;
		}

		public void run() {
			Element root = regXML.getDocumentElement();
			NodeList regList = root.getElementsByTagName("registration");
			nRegs = regList.getLength();
			setText("The registration file contains "+nRegs+" registration"+(nRegs!=1?"s":"")+".");
			for (int i=0; i<nRegs; i++) {
				String s = process((Element)regList.item(i));
				setText("Registration "+(i+1)+"/"+nRegs+"<br><br>"+s);
				try { Thread.sleep(interval * 1000); }
				catch (Exception ex) { return; }
			}
			setText(nRegs+" registration"+(nRegs!=1?"s":"")+" processed.");
			done();
		}

		private String process(Element regElement) {
			Configuration config = Configuration.getInstance();
			Registration reg = new Registration(regElement);
			if (validateInput(reg)) {

				//See if the registration already exists.
				RegistrationDatabase rdb = config.getRegistrationDatabase();
				Registration regx = rdb.lookup(reg.globalID);
				//If the registration already exists, use the data from the
				//database in order not to confuse any of the systems.
				if (regx != null) reg = regx;

				//Set the local IDs.
				//This must be done even if the registration came from the database
				//because the configuration may have changed and some localIDs may
				//be missing.
				Hashtable<String,String> idgroups = new Hashtable<String,String>();
				DCMSystem[] dcmsystems = config.getDCMSystems();
				for (int i=0; i<dcmsystems.length; i++) {
					String localAssigningAuthority = dcmsystems[i].localAssigningAuthority;
					String localID = idgroups.get(localAssigningAuthority);
					if (localID == null) {
						localID = reg.localIDTable.get(dcmsystems[i].id);
						if (localID == null) localID = config.getLocalID();
						idgroups.put(localAssigningAuthority,localID);
					}
					reg.addLocalID(dcmsystems[i].id,localID);
				}
				EHRSystem[] ehrsystems = config.getEHRSystems();
				for (int i=0; i<ehrsystems.length; i++) {
					String localAssigningAuthority = ehrsystems[i].localAssigningAuthority;
					String localID = idgroups.get(localAssigningAuthority);
					if (localID == null) {
						localID = reg.localIDTable.get(ehrsystems[i].id);
						if (localID == null) localID = config.getLocalID();
						idgroups.put(localAssigningAuthority,localID);
					}
					reg.addLocalID(ehrsystems[i].id,localID);
				}

				//Add the registration to the database
				rdb.add(reg);

				//Start up all the processing threads.
				startProcessingThreads(reg, config.getPIXMgrs(), Thread.NORM_PRIORITY);
				startProcessingThreads(reg, config.getRegistries(), Thread.MAX_PRIORITY);
				startProcessingThreads(reg, config.getPDQMgrs(), Thread.NORM_PRIORITY);
				startProcessingThreads(reg, config.getRepositories(), Thread.NORM_PRIORITY);
				startProcessingThreads(reg, config.getEHRSystems(), Thread.MIN_PRIORITY);
				startProcessingThreads(reg, config.getDCMSystems(), Thread.MIN_PRIORITY);

				return reg.toTable(true);
			}
			else return "Invalid registration.";
		}

		private void startProcessingThreads(Registration reg, Product[] systems, int priority) {
			for (int i=0; i<systems.length; i++) {
				systems[i].process(reg, priority);
			}
		}

		private boolean validateInput(Registration reg) {
			return !reg.globalID.equals("") && !reg.familyName.equals("");
		}

	}

}
