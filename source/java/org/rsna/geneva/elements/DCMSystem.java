/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import ca.etsmtl.ihe.rsna.kos.*;
import ca.etsmtl.ihe.xdsitest.registry.RegistryMessage;
import java.io.File;
import java.net.*;
import java.util.*;
import javax.xml.soap.SOAPMessage;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.DicomSender;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.objects.DicomObject;
import org.rsna.geneva.objects.KOS;
import org.rsna.util.StringUtil;
import org.rsna.geneva.hl7.*;

public class DCMSystem extends EHRSystem {

	static final Logger logger = Logger.getLogger(DCMSystem.class);

	public String type = "DCM System";

	public String dcmURL;
	public String repositoryID;
	public String institutionName;
	public String retrieveAET;
	public boolean acceptsRAD4;
	public boolean sendsKOS;

	public DCMSystem(Element el) {
		super(el);
		this.dcmURL = el.getAttribute("dcmURL").trim();
		this.repositoryID = el.getAttribute("repositoryID").trim();
		this.institutionName = el.getAttribute("institutionName").trim();
		this.retrieveAET = el.getAttribute("retrieveAET").trim();
		this.acceptsRAD4 = !el.getAttribute("acceptsRAD4").trim().equals("no");
		this.sendsKOS = !el.getAttribute("sendsKOS").trim().equals("no");
	}

	public void appendDataRows(StringBuffer sb) {
		super.appendDataRows(sb);
		sb.append("<tr><td>Accepts RAD4:</td><td>"+(acceptsRAD4?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>DCM URL:</td><td>"+dcmURL+"</td></tr>");
		sb.append("<tr><td>Retrieve AET:</td><td>"+retrieveAET+"</td></tr>");
		sb.append("<tr><td>Sends KOS:</td><td>"+(sendsKOS?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Repository ID:</td><td>"+repositoryID+"</td></tr>");
		sb.append("<tr><td>Institution Name:</td><td>"+institutionName+"</td></tr>");
	}

	void processRegistration(Registration reg) {
		//First handle all the HL7 messages
		super.processRegistration(reg);
		//Now process the Studies for this DCMSystem
		Study[] studies = Configuration.getInstance().getStudies();
		for (int i=0; i<studies.length; i++) {
			if (studies[i].enabled && studies[i].systemID.equals(id)) {
				processStudy(reg,studies[i]);
			}
		}
	}

	public void processStudy(Registration reg, Study study) {
		//Only service process requests if the system is enabled.
		if (!enabled) return;

		//Get the parameters to be used for both DICOM and HL7.
		DicomObject dicomObject = getDicomObject(study.directoryFile);

		//If nothing is there, quit.
		if (dicomObject == null) return;

		//Got something, process it.
		Configuration config = Configuration.getInstance();

		String modality = dicomObject.getModality();
		String accessionNumber = config.getAccessionNumber();
		int seqInt = config.getSeqInt();
		String rpID = "RPID" + seqInt;
		String spsID = "SPSID" + seqInt;
		String placerOrderNumber = "PN" + seqInt;
		String fillerOrderNumber = "FN" + seqInt;

		UIDMap map = new UIDMap();
		String studyInstanceUID = dicomObject.getStudyInstanceUID();
		studyInstanceUID = map.getUID(studyInstanceUID);

		//Start by sending an ORM if the system accepts one.
		sendRAD4(reg, study, studyInstanceUID, accessionNumber, rpID,
				spsID, placerOrderNumber, fillerOrderNumber, modality);

		//Next, process the DICOM files.
		String studyDate = study.date;
		String studyTime = null;
		if (studyDate.indexOf("*") != -1) studyDate = config.today();
		studyTime = config.now();
		KOS kos = new KOS(config,retrieveAET,institutionName);
		String localID = reg.localIDTable.get(id);

		InstanceNumber.reset();
		SendResult result = new SendResult();
		DicomSender sender = new DicomSender(dcmURL);
		sendInstances(reg, localID, study, study.directoryFile, studyDate, studyTime, studyInstanceUID,
					  accessionNumber, rpID, spsID, sender, map, kos, result);
		sender.close();
		RegSysEvent event =
			new RegSysEvent(
					this,
					((result.failure == 0) ? RegSysEvent.STATUS_OK : RegSysEvent.STATUS_ERROR),
					RegSysEvent.TYPE_DICOM,
					"Study ID: "+study.id +
					"<br>DCM System ID: "+id +
					"<br>Instances transferred: Success: "+result.success+
					"; Failure: "+result.failure
				);
		config.getEventLog().append(event);

		//Finally, send the KOS
		sendKOS(kos, study.id, reg.globalID, modality);
	}

	private void sendRAD4(
				Registration reg,
				Study study,
				String studyInstanceUID,
				String accessionNumber,
				String reqProcID,
				String schedProcStepID,
				String placerOrderNumber,
				String fillerOrderNumber,
				String diagnosticServiceID) {

		Configuration config = Configuration.getInstance();
		if (acceptsRAD4) {
			HL7RAD4 orm = new HL7RAD4();
			String dateTime = config.getDateTime();
			String referringDoctor = config.getPhysicianName();
			String enteredBy = config.getPhysicianName();
			String orderingProvider = config.getPhysicianName();

			orm.setMSH(
					receivingApplication,
					receivingFacility,
					dateTime,
					config.getMessageControlID());
			orm.setPID(
					reg.localIDTable.get(id),
					localAssigningAuthority,
					reg.getName(),
					reg.email,
					reg.birthdate,
					reg.sex,
					reg.street,
					reg.city,
					reg.state,
					reg.zip,
					reg.country);
			orm.setPV1(
					config.getPtLocation(pointOfCare),
					referringDoctor,
					dateTime,
					config.getPtVisit());
			orm.setORC(
					placerOrderNumber + "^" + study.placerOrderAuthority,
					fillerOrderNumber + "^" + study.fillerOrderAuthority,
					dateTime,
					enteredBy,
					orderingProvider,
					study.enteringOrganization);
			String universalServiceID =
				study.procedureCode +
				(study.localProcedureCode.equals("") ? "" : "^") +
				study.localProcedureCode;
			orm.setOBR(
					config.getSetID(),
					placerOrderNumber + "^" + study.placerOrderAuthority,
					fillerOrderNumber + "^" + study.fillerOrderAuthority,
					universalServiceID,
					orderingProvider,
					accessionNumber,
					reqProcID,
					schedProcStepID,
					diagnosticServiceID,
					study.procedureCode);
			orm.setZDS(
					studyInstanceUID);

			orm.setFields(fields);
			String response = orm.send(hl7URL,timeout);
			int status = HL7Message.toStatus(response);

			RegSysEvent event =
				new RegSysEvent(
						this,
						status,
						RegSysEvent.TYPE_HL7,
						orm.name+" sent to "+type + " ["+id+"]" +
						"<br>Global ID: "+reg.globalID +
						"<br>Local ID: "+reg.localIDTable.get(id) +
						"<br>Local ID: "+localAssigningAuthority +
						"<br>Message:<br>" +
						StringUtil.displayable(orm.toString()) +
						"Response: "+StringUtil.displayable(response));
			config.getEventLog().append(event);
		}
	}

	private void sendInstances(
					Registration reg,
					String localID,
					Study study,
					File file,
					String studyDate,
					String studyTime,
					String studyInstanceUID,
					String accessionNumber,
					String requestedProcedureID,
					String scheduledProcedureStepID,
					DicomSender sender,
					UIDMap map,
					KOS kos,
					SendResult result) {
		//Walk the tree, sending everything that parses.
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i=0; i<files.length; i++)
				sendInstances(
					reg, localID, study, files[i], studyDate, studyTime,
					studyInstanceUID, accessionNumber,
					requestedProcedureID, scheduledProcedureStepID,
					sender, map, kos, result);
		}
		else {
			try {
				DicomObject dicomObject = new DicomObject(file);

				//get the modality for later
				String modality = dicomObject.getModality();

				dicomObject.setPatientName(reg.getName());
				dicomObject.setPatientID(localID);
				dicomObject.setIssuerOfPatientID(localAssigningAuthority);

				//put the global ID in the ClinicalTrialSubjectID (0012,0040)
				//as a kludge for the Euro Demo
				dicomObject.setClinicalTrialSubjectID(reg.globalID);

				if (!reg.birthdate.equals("")) dicomObject.setPatientBirthDate(reg.birthdate);
				dicomObject.setPatientSex(reg.sex);
				dicomObject.setStudyDate(studyDate);
				if (studyTime != null) dicomObject.setStudyTime(studyTime);
				dicomObject.setAccessionNumber(accessionNumber);
				dicomObject.setSeriesDate(studyDate);
				dicomObject.setContentDate(studyDate);
				dicomObject.setInstitutionName(institutionName);
				dicomObject.setRequestedProcedureID(requestedProcedureID);
				dicomObject.setScheduledProcedureStepID(scheduledProcedureStepID);

				dicomObject.setSOPInstanceUID(Configuration.getInstance().getUID());
				dicomObject.setStudyInstanceUID(studyInstanceUID);
				if (!study.description.trim().equals(""))
					dicomObject.setStudyDescription(study.description);
				if (!study.bodyPartExamined.trim().equals(""))
					dicomObject.setBodyPartExamined(study.bodyPartExamined);
				dicomObject.setCodeSeq("RequestedProcedureCodeSeq",
						   				study.rpcsCodeValue,
						   				study.rpcsCodingSchemeDesignator,
						   				study.rpcsCodingSchemeVersion,
						   				study.rpcsCodeMeaning);
				dicomObject.setCodeSeq("AnatomicRegionSeq",
						   				study.arcsCodeValue,
						   				study.arcsCodingSchemeDesignator,
						   				study.arcsCodingSchemeVersion,
						   				study.arcsCodeMeaning);

				String origSeriesInstanceUID = dicomObject.getSeriesInstanceUID();
				String newSeriesInstanceUID = map.getUID(origSeriesInstanceUID);
				dicomObject.setSeriesInstanceUID(newSeriesInstanceUID);

				dicomObject.setInstanceNumber(Integer.toString(InstanceNumber.getNext()));

				sender.send(dicomObject);
				kos.add(dicomObject);
				result.success++;
				dicomObject.close();
				dicomObject = null;
			}
			catch (Exception ex) {
				ex.printStackTrace();
				result.failure++;
			}
		}
	}

	private void sendKOS(KOS kos, String studyID, String globalID, String modality) {
		String kosResult = "";
		try {
			File dir = new File(System.getProperty("user.dir"));
			dir = new File(dir,"temp");
			dir.mkdirs();
			File kosFile = File.createTempFile("kos-",".dcm",dir);
			kos.save(kosFile);
			if (sendsKOS) {
				Repository repository = Configuration.getInstance().getRepository(repositoryID);
				if (repository.enabled) {
					//send the KOS
					KOSsubmission.DomainsConfig kosDC = KOSsubmission.getNewConfig();
					kosDC.addDomain(true,getGlobalAssigningAuthority(),null);
					kosDC.addDomain(false,localAssigningAuthority,new URL(repository.soapURL));
					KOSsubmission submission = new KOSsubmission(kosDC);
					RegistryMessage regmsg =
						submission.send(
							kosFile,
							modality,
							globalID,
							localAssigningAuthority);
					kosResult = "KOS Result: " + regmsg.getResponseStatus();
					sendKOSEvent(true, kosResult, studyID, globalID);
					if (!kosFile.delete())
						System.out.println("Unable to delete KOS file:\n"+kosFile.getAbsolutePath());
				}
			}
		}
		catch (Exception ex) {
			kosResult = "KOS Result: " + ex.getMessage();
			logger.warn("Error processing the KOS.\n",ex);
			sendKOSEvent(false, kosResult, studyID, globalID);
		}
	}

	private void sendKOSEvent(boolean kosStatus, String kosResult,
							  String studyID, String globalID) {
		RegSysEvent event =
			new RegSysEvent(
					this,
					(kosStatus ? RegSysEvent.STATUS_OK : RegSysEvent.STATUS_ERROR),
					RegSysEvent.TYPE_SOAP,
					"Study ID: "+studyID +
					"<br>DCM System ID: "+id +
					"<br>Repository ID: "+repositoryID +
					"<br>" + kosResult
				);
		Configuration.getInstance().getEventLog().append(event);
	}

	//Walk a directory until you find a parsable DICOM file
	//and return its StudyInstanceUID and modality.
	private DicomObject getDicomObject(File file) {
		if (file.isDirectory()) {
			DicomObject dicomObject = null;
			File[] files = file.listFiles();
			for (int i=0; i<files.length; i++) {
				if ((dicomObject = getDicomObject(files[i])) != null)
					return dicomObject;
			}
		}
		else {
			try { return new DicomObject(file); }
			catch (Exception ex) { }
		}
		return null;
	}

	class SendResult {
		public int success = 0;
		public int failure = 0;
	}

	class UIDMap {

		Hashtable<String,String> map;

		public UIDMap() {
			map = new Hashtable<String,String>();
		}

		public String getUID(String oldUID) {
			String newUID = map.get(oldUID);
			if (newUID == null) {
				newUID = Configuration.getInstance().getUID();
				map.put(oldUID,newUID);
			}
			return newUID;
		}
	}

	static class InstanceNumber {
		static int instanceNumber = 0;
		public static void reset() {
			instanceNumber = 0;
		}
		public static int getNext() {
			instanceNumber++;
			return instanceNumber;
		}
	}

	public String getType() {
		return type;
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("dcmURL", dcmURL);
			e.setAttribute("repositoryID", repositoryID);
			e.setAttribute("institutionName", institutionName);
			e.setAttribute("retrieveAET", retrieveAET);
			e.setAttribute("acceptsRAD4", yesNo(acceptsRAD4));
			e.setAttribute("sendsKOS", yesNo(sendsKOS));
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = EHRSystem.createNewElement(name, id);
			e.setAttribute("type", "DCMSystem");
			e.setAttribute("acceptsRAD4", "yes");
			e.setAttribute("sendsKOS", "no");
			return e;
		}
		catch (Exception ex) { return null; }
	}

}

