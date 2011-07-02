/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.main;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.rsna.geneva.elements.*;
import org.rsna.geneva.hl7.*;
import org.rsna.geneva.misc.*;
import org.rsna.geneva.panels.*;
import org.rsna.util.FileUtil;
import org.rsna.util.XmlUtil;

public class Configuration {

	static Configuration configuration = null;
	static File configFile = null;

	static final Logger logger = Logger.getLogger(Configuration.class);

    public static final String configFN		= "config.xml";
    public static final String exconfigFN	= "example-config.xml";
    public static final String propsFN		= "config.properties";
    public static final String addressesFN	= "addresses.xml";
    public static final String exaddressesFN= "example-addresses.xml";

	private static Calendar calendar = new GregorianCalendar();
	private static final String zeroes = "0000";

	//These two default values are made public in order to allow
	//the RegistrationXml class to compile. It appears that that
	//class doesn't get the assigning authority from the Registration
	//object (or something associated with the system sending the
	//HL7 message). This is probably a bug in the RegistrationXml
	//class, but for now, just accept it.
	public String globalAssigningAuthority = "&amp;1.3.6.1.4.1.21367.2007.1.2.200&amp;ISO";
	public String localAssigningAuthority = "&amp;1.3.6.1.4.1.21367.2007.1.2.200&amp;ISO";

	//Indexes of configuration elements
	Hashtable<String, ConfigElement> elementsTable;
	Hashtable<String, Repository> repositoriesTable;
	Hashtable<String, Registry> registriesTable;
	Hashtable<String, PIXMgr> pixmgrsTable;
	Hashtable<String, PDQMgr> pdqmgrsTable;
	Hashtable<String, EHRSystem> ehrsystemsTable;
	Hashtable<String, DCMSystem> dcmsystemsTable;
	Hashtable<String, Study> studiesTable;
	Hashtable<String, DocSet> docsetsTable;
	Hashtable<String, Message> messagesTable;

	File propertiesFile;
	Properties props;
	Document configXML = null;
	Message[] messages;
	int logDepth = 100;
	int serverPort = 80;
	String uidRoot = "1.2.3.4.5.6.7";
	String uidSubroot = "1";
	String ipAddress = "unknown";
	boolean askOnClose = true;
	int uidCount = 0; //UID count
	int accCount = 0; //Accession number count
	int lidCount = 0; //Local ID count
	int cidCount = 0; //HL7 message control ID
	int setCount = 0; //HL7 set UD
	int seqCount = 0; //Procedure ID
	int lastPtLocation = 9991;
	EventLog eventLog = null;
	ConfigurationPanel configPanel = null;
	ControlPanel controlPanel = null;
	RegistrationDatabase registrationDatabase;
	Address[] addresses;
	String[] streets;
	int lastAddressIndexM = -1;
	int lastAddressIndexF = -1;
	Random randomizer;
	boolean hasChanged = false;

	String[] physicianNames = {
		"5101^Welby^Marcus^^^DR",
		"5102^Kildare^Richard^^^DR",
		"5103^Leakey^Louis^^^DR",
		"5104^Curie^Marie^^^DR",
		"5105^Cleaver^Ward^^^DR",
		"5106^Remore^Drake^^^DR",
		"5107^Howser^Doogie^^^DR",
		"5108^House^Gregory^^^DR"
	};

    public File iti44XsltTemplateFile = new File("stylesheets/hl7v3/iti44.xsl");
    public String senderDeviceId;
    public String senderDeviceName;

    public static Configuration getInstance() {
		if (configuration == null) {
			try {
				configuration = new Configuration();
				configuration.load(configFile);
			}
			catch (Exception ex) {
				logger.error("Error loading the configuration.",ex);
				System.exit(0);
			}
		}
		return configuration;
	}

	protected Configuration() throws Exception {
		configFile = new File(configFN);
		File exconfigFile = new File(exconfigFN);
		if (!configFile.exists() && exconfigFile.exists())
			FileUtil.copy(exconfigFile,configFile);
		propertiesFile = new File(propsFN);
		loadProperties();

		registrationDatabase = new RegistrationDatabase();
		ipAddress = getIPAddress();
		uidCount = 0;

		String temp = props.getProperty("lastPtLocation");
		try { lastPtLocation = Integer.parseInt(temp); }
		catch (Exception ignore) { }
		temp = props.getProperty("seqCount");
		try { seqCount = Integer.parseInt(temp); }
		catch (Exception ignore) { }

		loadAddresses();
		hasChanged = true;
	}

	private void loadAddresses() throws Exception {
		File addressesFile = new File(addressesFN);
		File exaddressesFile = new File(exaddressesFN);
		if (!addressesFile.exists() && exaddressesFile.exists())
		FileUtil.copy(exaddressesFile,addressesFile);
		Document adds = XmlUtil.getDocument(addressesFile);
		Element root = adds.getDocumentElement();
		ArrayList<String> streetList = new ArrayList<String>();
		ArrayList<Address> adrsList = new ArrayList<Address>();
		Node child = root.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)child;
				if (childElement.getTagName().equals("address")) {
					Address adrs = new Address(childElement);
					adrsList.add(adrs);
					if (!adrs.street.equals("")) streetList.add(adrs.street);
				}
			}
			child = child.getNextSibling();
		}
		addresses = new Address[adrsList.size()];
		addresses = adrsList.toArray(addresses);
		streets = new String[streetList.size()];
		streets = streetList.toArray(streets);
		randomizer = new Random();
	}

	private void load(File configFile) throws Exception {
		elementsTable = new Hashtable<String, ConfigElement>();
		repositoriesTable = new Hashtable<String, Repository>();
		dcmsystemsTable = new Hashtable<String, DCMSystem>();
		if (configFile.exists()) {
			configXML = XmlUtil.getDocument(configFile);
			Element root = configXML.getDocumentElement();

			globalAssigningAuthority = root.getAttribute("globalAssigningAuthority");
			localAssigningAuthority = root.getAttribute("localAssigningAuthority");
			uidRoot = root.getAttribute("uidRoot");
			uidRoot = uidRoot.replaceAll("[\\.]+",".");
			if (!uidRoot.endsWith(".")) uidRoot += ".";
			uidSubroot = (System.currentTimeMillis() % 1000000) + "."; //!!!kludge!!!

			logDepth = getInt(root,"logDepth",100);
			serverPort = getInt(root,"serverPort",80);

			askOnClose = !root.getAttribute("askOnClose").equals("no");

            senderDeviceId= root.getAttribute("senderDeviceId");
            senderDeviceName= root.getAttribute("senderDeviceName");

			elementsTable = new Hashtable<String, ConfigElement>();
			repositoriesTable = new Hashtable<String, Repository>();
			registriesTable = new Hashtable<String, Registry>();
			pixmgrsTable = new Hashtable<String, PIXMgr>();
			pdqmgrsTable = new Hashtable<String, PDQMgr>();
			ehrsystemsTable = new Hashtable<String, EHRSystem>();
			dcmsystemsTable = new Hashtable<String, DCMSystem>();
			studiesTable = new Hashtable<String, Study>();
			docsetsTable = new Hashtable<String, DocSet>();
			messagesTable = new Hashtable<String, Message>();

			ArrayList<Message> messageList = new ArrayList<Message>();

			Node child = root.getFirstChild();
			while (child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element)child;
					if (childElement.getTagName().equals("pixmgr")) {
						PIXMgr pixmgr = new PIXMgr(childElement);
						pixmgrsTable.put(pixmgr.id, pixmgr);
						elementsTable.put(pixmgr.id, pixmgr);
					}
					else if (childElement.getTagName().equals("registry")) {
						Registry registry = new Registry(childElement);
						registriesTable.put(registry.id, registry);
						elementsTable.put(registry.id, registry);
					}
					else if (childElement.getTagName().equals("pdqmgr")) {
						PDQMgr pdqmgr = new PDQMgr(childElement);
						pdqmgrsTable.put(pdqmgr.id, pdqmgr);
						elementsTable.put(pdqmgr.id, pdqmgr);
					}
					else if (childElement.getTagName().equals("repository")) {
						Repository repository = new Repository(childElement);
						repositoriesTable.put(repository.id, repository);
						elementsTable.put(repository.id, repository);
					}
					else if (childElement.getTagName().equals("ehrsystem")) {
						EHRSystem ehrsystem = new EHRSystem(childElement);
						ehrsystemsTable.put(ehrsystem.id, ehrsystem);
						elementsTable.put(ehrsystem.id, ehrsystem);
					}
					else if (childElement.getTagName().equals("dcmsystem")) {
						DCMSystem dcmsystem = new DCMSystem(childElement);
						dcmsystemsTable.put(dcmsystem.id, dcmsystem);
						elementsTable.put(dcmsystem.id, dcmsystem);
					}
					else if (childElement.getTagName().equals("study")) {
						Study study = new Study(childElement);
						studiesTable.put(study.id, study);
						elementsTable.put(study.id, study);
					}
					else if (childElement.getTagName().equals("docset")) {
						DocSet docset = new DocSet(childElement);
						docsetsTable.put(docset.id, docset);
						elementsTable.put(docset.id, docset);
					}
					else if (childElement.getTagName().equals("message")) {
						Message message = new Message(childElement);
						messagesTable.put(message.id, message);
						elementsTable.put(message.id, message);
					}
				}
				child = child.getNextSibling();
			}
		}
	}

	public void reload() {
		reload("main console");
	}

	public void reload(String username) {
		synchronized (this) {
			try {
				load(configFile);
				logEvent("Configuration reloaded by "+username);
			}
			catch (Exception ex) {
				logger.error("Error loading the configuration.",ex);
			}
		}
		hasChanged = true;
		configPanel.display();
		controlPanel.reload();
	}

	public File getFile() {
		return configFile;
	}

	//Save the configuration (the persistent properties and the
	//registration database). This method is called when
	//the application exits. If you want to save other things,
	//do it here.
	public void save() {
		storeProperties();
		registrationDatabase.save();
	}

	public boolean getAskOnClose() {
		return askOnClose;
	}

	//Get the default global assigning authority
	public String getGlobalAssigningAuthority() {
		return globalAssigningAuthority;
	}

	//Get the default local assigning authority
	public String getLocalAssigningAuthority() {
		return localAssigningAuthority;
	}

	public int getLogDepth() {
		return logDepth;
	}

	public void setControlPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}

	public void setConfigurationPanel(ConfigurationPanel configPanel) {
		this.configPanel = configPanel;
	}

	public void setEventLog(EventLog eventLog) {
		this.eventLog = eventLog;
	}

	public EventLog getEventLog() {
		return eventLog;
	}

	public RegistrationDatabase getRegistrationDatabase() {
		return registrationDatabase;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getUIDRoot() {
		return uidRoot;
	}

	//Create a new UID.
	public String getUID() {
		return uidRoot + uidSubroot + uidCount++;
	}

	//Create a UUID.
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	//Create a new Accession Number.
	public String getAccessionNumber() {
		return "IHE" + uidSubroot + accCount++;
	}

	//Create a Local ID
	public String getLocalID() {
		String s = Integer.toString(++lidCount) + Long.toString(System.currentTimeMillis());
		s = getUSMD5(s);
		if (s.length() < 9) s = "000000000" + s;
		s = s.substring(s.length()-9, s.length());
		//kludge for Siemens:
		if (s.charAt(0) == '0') s = "1"+s.substring(1);
		return s;
	}

	//Create an HL7 message control ID
	public String getMessageControlID() {
		String s = Integer.toString(++cidCount);
		if (s.length() < 8) s = "00000000" + s;
		s = s.substring(s.length()-8, s.length());
		return s;
	}

	//Create an HL7 message set ID
	public String getSetID() {
/*
		//The following is commented out because
		//the participating systems just want the
		//value "1".
		String s = Integer.toString(++setCount);
		if (s.length() < 6) s = "000000" + s;
		s = s.substring(s.length()-6, s.length());
		return s;
*/
		return "1";
	}

	public Address getAddress(String sex) {
		Address adrs;

		//IMPORTANT: This empty loop will run forever if no address is available for the specified sex.
		//Make sure that at least one entry appears in the address file with no sex attribute to avoid this problem.
		while ( !(adrs=getNextAddress(sex)).sex.equals(sex) && !adrs.sex.equals("") ) ;

		String street =
			(1 + randomizer.nextInt(999)) + " " +
			streets[randomizer.nextInt(streets.length)];
		adrs.street = street;
		return adrs;
	}

	private Address getNextAddress(String sex) {
		if (sex.equals("F")) {
			lastAddressIndexF++;
			if (lastAddressIndexF >= addresses.length) lastAddressIndexF = 0;
			return addresses[lastAddressIndexF];
		}
		else {
			lastAddressIndexM++;
			if (lastAddressIndexM >= addresses.length) lastAddressIndexM = 0;
			return addresses[lastAddressIndexM];
		}
	}

	//Get a patient location string
	public synchronized String getPtLocation(String pointOfCare) {
		lastPtLocation++;
		props.setProperty("lastPtLocation",""+lastPtLocation);
		return pointOfCare + "^" + lastPtLocation + "^1";
	}

	//Get a patient visit string
	public synchronized String getPtVisit() {
		return "V" + getSeqInt() + "-1^^^XDSDEMO_ADT";
	}

	//Get a physician name string
	public synchronized String getPhysicianName() {
		return physicianNames[randomizer.nextInt(physicianNames.length)];
	}

	//Get a sequential integer for use in procedure IDs, etc.
	public synchronized int getSeqInt() {
		seqCount++;
		props.setProperty("seqCount",""+seqCount);
		return seqCount;
	}

	//Get arrays of the various system types
	public PIXMgr[] getPIXMgrs() {
		PIXMgr[] x = pixmgrsTable.values().toArray( new PIXMgr[ pixmgrsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public PDQMgr[] getPDQMgrs() {
		PDQMgr[] x = pdqmgrsTable.values().toArray( new PDQMgr[ pdqmgrsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public Registry[] getRegistries() {
		Registry[] x = registriesTable.values().toArray( new Registry[ registriesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public Repository[] getRepositories() {
		Repository[] x = repositoriesTable.values().toArray( new Repository[ repositoriesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public EHRSystem[] getEHRSystems() {
		EHRSystem[] x = ehrsystemsTable.values().toArray( new EHRSystem[ ehrsystemsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public DCMSystem[] getDCMSystems() {
		DCMSystem[] x = dcmsystemsTable.values().toArray( new DCMSystem[ dcmsystemsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public Study[] getStudies() {
		Study[] x = studiesTable.values().toArray( new Study[ studiesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public DocSet[] getDocSets() {
		DocSet[] x = docsetsTable.values().toArray( new DocSet[ docsetsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public Message[] getMessages() {
		Message[] x = messagesTable.values().toArray( new Message[ messagesTable.size() ] );
		Arrays.sort(x);
		return x;
	}
	//Get arrays of the various system IDs
	public String[] getPIXMgrIDs() {
		String[] x = pixmgrsTable.keySet().toArray( new String[ pixmgrsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getPDQMgrIDs() {
		String[] x = pdqmgrsTable.keySet().toArray( new String[ pdqmgrsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getRegistryIDs() {
		String[] x = registriesTable.keySet().toArray( new String[ registriesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getRepositoryIDs() {
		String[] x = repositoriesTable.keySet().toArray( new String[ repositoriesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getEHRSystemIDs() {
		String[] x = ehrsystemsTable.keySet().toArray( new String[ ehrsystemsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getDCMSystemIDs() {
		String[] x = dcmsystemsTable.keySet().toArray( new String[ dcmsystemsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getStudyIDs() {
		String[] x = studiesTable.keySet().toArray( new String[ studiesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getDocSetIDs() {
		String[] x = docsetsTable.keySet().toArray( new String[ docsetsTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	public String[] getMessageIDs() {
		String[] x = messagesTable.keySet().toArray( new String[ messagesTable.size() ] );
		Arrays.sort(x);
		return x;
	}

	//Get specific element types
	public DCMSystem getDCMSystem(String id) {
		return dcmsystemsTable.get(id);
	}

	public Repository getRepository(String id) {
		return repositoriesTable.get(id);
	}

	public ConfigElement getConfigElement(String id) {
		return  elementsTable.get(id);
	}

	public void setChanged(boolean changed) {
		hasChanged = changed;
	}

	public boolean hasChanged() {
		return hasChanged;
	}

	public Element createNewElement(String username, String type) {
		synchronized (this) {
			String id;
			if (type.equals("study")) id = "ST";
			else if (type.equals("docset")) id = "DS";
			else id = type.substring(0,3).toUpperCase();
			id += "_" + username + "_";
			Element el = null;
			if (type.equals("study")) {
				id += getNextNumber( studiesTable, username );
				el = Study.createNewElement(type, id);
				Study newX = new Study(el);
				studiesTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("docset")) {
				id += getNextNumber( docsetsTable, username );
				el = DocSet.createNewElement(type, id);
				DocSet newX = new DocSet(el);
				docsetsTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("repository")) {
				id += getNextNumber( repositoriesTable, username );
				el = Repository.createNewElement(type, id);
				Repository newX = new Repository(el);
				repositoriesTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("pdqmgr")) {
				id += getNextNumber( pdqmgrsTable, username );
				el = PDQMgr.createNewElement(type, id);
				PDQMgr newX = new PDQMgr(el);
				pdqmgrsTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("pixmgr")) {
				id += getNextNumber( pixmgrsTable, username );
				el = PIXMgr.createNewElement(type, id);
				PIXMgr newX = new PIXMgr(el);
				pixmgrsTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("registry")) {
				id += getNextNumber( registriesTable, username );
				el = Registry.createNewElement(type, id);
				Registry newX = new Registry(el);
				registriesTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("ehrsystem")) {
				id += getNextNumber( ehrsystemsTable, username );
				el = EHRSystem.createNewElement(type, id);
				EHRSystem newX = new EHRSystem(el);
				ehrsystemsTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			else if (type.equals("dcmsystem")) {
				id += getNextNumber( dcmsystemsTable, username );
				el = DCMSystem.createNewElement(type, id);
				DCMSystem newX = new DCMSystem(el);
				dcmsystemsTable.put(newX.id, newX);
				elementsTable.put(newX.id, newX);
			}
			if (el != null) {
				hasChanged = true;
				logEvent("Element "+el.getAttribute("id")+" created by "+username);
			}
			return el;
		}
	}

	public String deleteElement(String username, String id) {
		synchronized (this) {
			ConfigElement ce = elementsTable.get(id);
			if (ce != null) {
				elementsTable.remove(id);
				studiesTable.remove(id);
				docsetsTable.remove(id);
				repositoriesTable.remove(id);
				pdqmgrsTable.remove(id);
				pixmgrsTable.remove(id);
				registriesTable.remove(id);
				ehrsystemsTable.remove(id);
				dcmsystemsTable.remove(id);
				elementsTable.remove(id);
				hasChanged = true;
				logEvent("Element "+id+" deleted by "+username);
				return "OK";
			}
			else return "Element not found.";
		}
	}

	public String saveAs(String username, File file) {
		synchronized (this) {
			if (file == null) file = configFile;
			try {
				Element server = getServerXML();
				Registry[] regs = getRegistries();
				for (int i=0; i<regs.length; i++) {
					Node n = regs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				Repository[] reps = getRepositories();
				for (int i=0; i<reps.length; i++) {
					Node n = reps[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				PIXMgr[] pixs = getPIXMgrs();
				for (int i=0; i<pixs.length; i++) {
					Node n = pixs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				PDQMgr[] pdqs = getPDQMgrs();
				for (int i=0; i<pdqs.length; i++) {
					Node n = pdqs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				EHRSystem[] ehrs = getEHRSystems();
				for (int i=0; i<ehrs.length; i++) {
					Node n = ehrs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				DCMSystem[] dcms = getDCMSystems();
				for (int i=0; i<dcms.length; i++) {
					Node n = dcms[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				Study[] studs = getStudies();
				for (int i=0; i<studs.length; i++) {
					Node n = studs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				DocSet[] docs = getDocSets();
				for (int i=0; i<docs.length; i++) {
					Node n = docs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				Message[] msgs = getMessages();
				for (int i=0; i<msgs.length; i++) {
					Node n = msgs[i].getXML();
					n = server.getOwnerDocument().importNode(n, true);
					server.appendChild(n);
				}
				String xml = XmlUtil.toPrettyString(server);
				FileUtil.setFileText(file, xml);
				hasChanged = true;
				logEvent("Configuration saved by "+username);
				reload(username);
				return "OK";
			}
			catch (Exception ex) { return ex.getMessage(); }
		}
	}

	private void logEvent(String message) {
		RegSysEvent event =
			new RegSysEvent(
					this,
					RegSysEvent.STATUS_OK,
					RegSysEvent.TYPE_STATUS,
					message);
		eventLog.append(event);
		logger.info(message);
	}

	public String saveElement(String username, Element el) {
		synchronized (this) {
			String name = el.getTagName();
			if (name.equals("config")) {
				globalAssigningAuthority = el.getAttribute("globalAssigningAuthority");
				localAssigningAuthority = el.getAttribute("localAssigningAuthority");
				uidRoot = el.getAttribute("uidRoot");
				logDepth = getInt(el,"logDepth",100);
				serverPort = getInt(el,"serverPort",80);
				senderDeviceId = el.getAttribute("senderDeviceId");
				senderDeviceName = el.getAttribute("senderDeviceName");
				askOnClose = !el.getAttribute("askOnClose").equals("no");
			}
			else if (name.equals("registry")) {
				Registry x = new Registry(el);
				registriesTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("repository")) {
				Repository x = new Repository(el);
				repositoriesTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("pixmgr")) {
				PIXMgr x = new PIXMgr(el);
				pixmgrsTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("pdqmgr")) {
				PDQMgr x = new PDQMgr(el);
				pdqmgrsTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("ehrsystem")) {
				EHRSystem x = new EHRSystem(el);
				ehrsystemsTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("dcmsystem")) {
				DCMSystem x = new DCMSystem(el);
				dcmsystemsTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("study")) {
				Study x = new Study(el);
				studiesTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("docset")) {
				DocSet x = new DocSet(el);
				docsetsTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else if (name.equals("docset")) {
				DocSet x = new DocSet(el);
				docsetsTable.put(x.id, x);
				elementsTable.put(x.id, x);
			}
			else return ("Request received to save an unknown element.");
			hasChanged = true;
			String msg = "Element "+el.getAttribute("id")+" updated by "+username;
			logEvent(msg);
			return "OK";
		}
	}

	private ConfigElement[] append (ConfigElement[] ceArray, ConfigElement ce) {
		ArrayList<ConfigElement> list = new ArrayList<ConfigElement>();
		for (int i=0; i<ceArray.length; i++) list.add(ceArray[i]);
		list.add(ce);
		ceArray = new ConfigElement[list.size()];
		ceArray = list.toArray(ceArray);
		Arrays.sort(ceArray);
		return ceArray;
	}

	private int getNextNumber(Hashtable table, String username) {
		Enumeration en = table.elements();
		int n = 0;
		while (en.hasMoreElements()) {
			username = username.toLowerCase();
			String id = ((ConfigElement)en.nextElement()).id;
			if (id.toLowerCase().contains("_" + username + "_")) {
				int k = id.lastIndexOf("_");
				if (k > 0) {
					String s = id.substring(k+1);
					try {
						int nn = Integer.parseInt(s);
						if (nn > n) n = nn;
					}
					catch (Exception ex) { }
				}
			}
		}
		return n+1;
	}

	public String getPage(String filter) {
		StringBuffer sb = new StringBuffer();
		sb.append("<center><h1>Configuration</h1>");
		sb.append("<h3>"+configFile.getName()+"</h3>");
		sb.append(getTable(filter));
		sb.append("</center>");
		hasChanged = false;
		return sb.toString();
	}

	public String getTable(String filter) {
		filter = filter.toLowerCase();
		StringBuffer sb = new StringBuffer();
		sb.append("<table border=\"1\" width=\"100%\">");
		sb.append("<tr><td width=\"20%\">IP Address:</td><td>"+ipAddress+"</td></tr>");
		sb.append("<tr><td>Server Port:</td><td>"+serverPort+"</td></tr>");
		sb.append("<tr><td>Default Global Assigning Authority:</td><td>"+globalAssigningAuthority+"</td></tr>");
		sb.append("<tr><td>Default Local Assigning Authority:</td><td>"+localAssigningAuthority+"</td></tr>");
		sb.append("<tr><td>Log Depth:</td><td>"+logDepth+"</td></tr>");
		sb.append("<tr><td>UID Root:</td><td>"+uidRoot+"</td></tr>");
		sb.append("<tr><td>senderDeviceId:</td><td>"+senderDeviceId+"</td></tr>");
		sb.append("<tr><td>senderDeviceName:</td><td>"+senderDeviceName+"</td></tr>");
		PIXMgr[] pixmgrs = getPIXMgrs();
		for (int i=0; i<pixmgrs.length; i++) {
			if (pixmgrs[i].id.toLowerCase().contains(filter))
				pixmgrs[i].appendTableRow(sb);
		}
		Registry[] registries = getRegistries();
		for (int i=0; i<registries.length; i++) {
			if (registries[i].id.toLowerCase().contains(filter))
				registries[i].appendTableRow(sb);
		}
		PDQMgr[] pdqmgrs = getPDQMgrs();
		for (int i=0; i<pdqmgrs.length; i++) {
			if (pdqmgrs[i].id.toLowerCase().contains(filter))
				pdqmgrs[i].appendTableRow(sb);
		}
		Repository[] repositories = getRepositories();
		for (int i=0; i<repositories.length; i++) {
			if (repositories[i].id.toLowerCase().contains(filter))
				repositories[i].appendTableRow(sb);
		}
		EHRSystem[] ehrsystems = getEHRSystems();
		for (int i=0; i<ehrsystems.length; i++) {
			if (ehrsystems[i].id.toLowerCase().contains(filter))
				ehrsystems[i].appendTableRow(sb);
		}
		DCMSystem[] dcmsystems = getDCMSystems();
		for (int i=0; i<dcmsystems.length; i++) {
			if (dcmsystems[i].id.toLowerCase().contains(filter))
				dcmsystems[i].appendTableRow(sb);
		}
		Study[] studies = getStudies();
		for (int i=0; i<studies.length; i++) {
			if (studies[i].id.toLowerCase().contains(filter))
				studies[i].appendTableRow(sb);
		}
		DocSet[] docsets = getDocSets();
		for (int i=0; i<docsets.length; i++) {
			if (docsets[i].id.toLowerCase().contains(filter))
				docsets[i].appendTableRow(sb);
		}
		Message[] messages = getMessages();
		for (int i=0; i<messages.length; i++) {
			if (messages[i].id.toLowerCase().contains(filter))
				messages[i].appendTableRow(sb);
		}
		sb.append("</table>");
		return sb.toString();
	}

	public synchronized String today() {
		calendar.setTimeInMillis(System.currentTimeMillis());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
 		return zero(year,4) + zero(month,2) + zero(day,2);
	}

	public synchronized String now() {
		calendar.setTimeInMillis(System.currentTimeMillis());
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
 		return zero(hour,2) + zero(min,2) + zero(sec,2);
	}

	public synchronized String getDateTime() {
		calendar.setTimeInMillis(System.currentTimeMillis());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		/*int sec = calendar.get(Calendar.SECOND);*/
 		return zero(year,4) + zero(month,2) + zero(day,2)
 			 + zero(hour,2) + zero(min,2)/* + zero(sec,2)*/;
	}

	private String zero(int val, int len) {
		String valText = Integer.toString(val);
		int n = len - valText.length();
		if (n <= 0) return valText;
		return zeroes.substring(0,n) + valText;
	}

	private int getInt(Element el, String attr, int def) {
		String valueString = el.getAttribute(attr);
		int valueInt = def;
		try { valueInt = Integer.parseInt(valueString); }
		catch (Exception keepDefault) { }
		return valueInt;
	}

	private static String getUSMD5(String string) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] hashed = messageDigest.digest(string.getBytes("UTF-8"));
			BigInteger bi = new BigInteger(1,hashed);
			return bi.toString();
		}
		catch (Exception ex) {  }
		return Long.toString(System.currentTimeMillis());
	}

	//Get the computer's IP address from the OS.
	private String getIPAddress() {
		InetAddress localHost;
		try { localHost = InetAddress.getLocalHost(); }
		catch (Exception e) { return "unknown"; }
		return localHost.getHostAddress();
	}

	//*******************************
	//The persistent properties
	//*******************************
	//Load the properties file containing the
	//enables, ignoring any exceptions.
	private void loadProperties() {
		props = new Properties();
		try {
			FileInputStream fis = new FileInputStream(propertiesFile);
			props.load(fis);
		}
		catch (Exception e) { }
	}

	//Save the properties file. Return true if
	//the save was successful; false otherwise.
	private boolean storeProperties() {
		try {
			FileOutputStream fos = new FileOutputStream(propertiesFile);
			props.store(fos,"Geneva Persistent Properties");
			fos.flush();
			fos.close();
			return true;
		}
		catch (Exception e) { }
		return false;
	}

	public Properties getProperties() {
		return props;
	}

    /**
     * extract HL7 fields from the specified element.
     */
    public HL7Field[] getFields(Element el) {
        ArrayList<HL7Field> list = new ArrayList<HL7Field>();
        Node child = el.getFirstChild();
            while (child != null) {
                if ((child.getNodeType() == Node.ELEMENT_NODE)
                    && child.getNodeName().equals("hl7")) {
                    Element e = (Element)child;
                    try {
                        list.add(
                            new HL7Field(
                            e.getAttribute("msg"),
                            e.getAttribute("segment"),
                            e.getAttribute("seq"),
                            e.getAttribute("value")
                        )
                    );
                }
                catch (Exception ignore) { }
            }
            child = child.getNextSibling();
        }
        HL7Field[] fields = new HL7Field[list.size()];
        fields = list.toArray(fields);
        return fields;
    }

	public Element getServerXML() {
		try {
			Document doc = XmlUtil.getDocument();
			Element e = doc.createElement("config");
			e.setAttribute("globalAssigningAuthority", globalAssigningAuthority);
			e.setAttribute("localAssigningAuthority", localAssigningAuthority);
			e.setAttribute("uidRoot", uidRoot);
			e.setAttribute("logDepth", Integer.toString(logDepth));
			e.setAttribute("serverPort", Integer.toString(serverPort));
			e.setAttribute("senderDeviceId", senderDeviceId);
			e.setAttribute("senderDeviceName", senderDeviceName);
			e.setAttribute("askOnClose", ConfigElement.yesNo(askOnClose));
			return e;
		}
		catch (Exception ex) { return null; }
	}

}
