/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.objects;

import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.*;
import javax.imageio.ImageIO;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObject;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.VRs;

/**
  * Class which encapsulates a DICOM object and provides access to its elements.
  */
public class DicomObject {

	static final DcmParserFactory pFact = DcmParserFactory.getInstance();
	static final DcmObjectFactory oFact = DcmObjectFactory.getInstance();
	static final DictionaryFactory dFact = DictionaryFactory.getInstance();
	static final TagDictionary tagDictionary = dFact.getDefaultTagDictionary();
	static final UIDDictionary uidDictionary = dFact.getDefaultUIDDictionary();

	File file;
	Dataset dataset = null;
	SpecificCharacterSet charset = null;
	DcmParser parser = null;
	FileFormat fileFormat = null;
	FileMetaInfo fileMetaInfo = null;
	BufferedInputStream in = null;

	/**
	 * Class constructor; parses a file to create a new DicomObject.
	 * @param file the file containing the DicomObject.
	 * @throws IOException if the file cannot be read or the file does not parse.
	 */
	public DicomObject(File file) throws Exception {
		try {
			this.file = file;
			in = new BufferedInputStream(new FileInputStream(file));
			parser = pFact.newDcmParser(in);
			fileFormat = parser.detectFileFormat();
			if (fileFormat == null)
				throw new IOException("Unrecognized file format: "+file);
			dataset = oFact.newDataset();
			parser.setDcmHandler(dataset.getDcmHandler());
			parser.parseDcmFile(fileFormat, Tags.PixelData);
			charset = dataset.getSpecificCharacterSet();
            fileMetaInfo = dataset.getFileMetaInfo();
		}
		catch (Exception exception) {
			close();
			throw exception;
		}
	}

	/**
	 * Close the input stream and forget the file.
	 */
	public void close() {
		if (in != null) {
			try { in.close(); }
			catch (Exception ignore) { }
		}
		file = null;
	}

	/**
	 * Get the DcmParser.
	 * @return the parser used to parse the object.
	 */
	public DcmParser getDcmParser() {
		return parser;
	}

	/**
	 * Get the FileFormat.
	 * @return the FileFormat acquired when the object was parsed.
	 */
	public FileFormat getFileFormat() {
		return fileFormat;
	}

	/**
	 * Get the FileMetaInfo.
	 * @return the FileMetaInfo acquired when the object was parsed.
	 */
	public FileMetaInfo getFileMetaInfo() {
		return fileMetaInfo;
	}

	/**
	 * Get the Dataset.
	 * @return the Dataset containing all the elements up to the pixel data.
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Get the Transfer Syntax UID.
	 */
	public String getTransferSyntaxUID() {
		return fileMetaInfo.getTransferSyntaxUID();
	}

	/**
	 * Get the contents of a DICOM element in the DicomObject's dataset.
	 * This method returns an empty String if the element does not exist.
	 * @param tag the tag specifying the element (in the form 0xggggeeee).
	 * @return the text of the element, or the empty String if the
	 * element does not exist.
	 */
	public String getElementValue(int tag) {
		return getElementValue(tag,"");
	}

	/**
	 * Get the contents of a DICOM element in the DicomObject's dataset.
	 * This method returns the defaultString argument if the element does not exist.
	 * @param tag the tag specifying the element (in the form 0xggggeeee).
	 * @param defaultString the String to return if the element does not exist.
	 * @return the text of the element, or defaultString if the element does not exist.
	 */
	public String getElementValue(int tag, String defaultString) {
		String value = null;
		try { value = dataset.getString(tag); }
		catch (Exception e) { }
		if (value == null) value = defaultString;
		return value;
	}

	/**
	 * Convenience method to get the contents of the Modality element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getModality() {
		return getElementValue(Tags.Modality);
	}

	/**
	 * Convenience method to get the contents of the PatientName element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getPatientName() {
		return getElementValue(Tags.PatientName);
	}

	/**
	 * Convenience method to get the contents of the PatientID element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getPatientID() {
		return getElementValue(Tags.PatientID);
	}

	/**
	 * Convenience method to get the contents of the PatientBirthDate element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getPatientBirthDate() {
		return getElementValue(Tags.PatientBirthDate);
	}

	/**
	 * Convenience method to get the contents of the PatientSex element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getPatientSex() {
		return getElementValue(Tags.PatientSex);
	}

	/**
	 * Convenience method to get the contents of the StudyDate element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getStudyDate() {
		return getElementValue(Tags.StudyDate);
	}

	/**
	 * Convenience method to get the contents of the StudyTime element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getStudyTime() {
		return getElementValue(Tags.StudyTime);
	}

	/**
	 * Convenience method to get the contents of the StudyID element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getStudyID() {
		return getElementValue(Tags.StudyID);
	}

	/**
	 * Convenience method to get the contents of the AccessionNumber element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getAccessionNumber() {
		return getElementValue(Tags.AccessionNumber);
	}

	/**
	 * Convenience method to get the contents of the ReferringPhysicianName element.
	 * @return the text of the element or the empty String if the
	 * element does not exist.
	 */
	public String getReferringPhysicianName() {
		return getElementValue(Tags.ReferringPhysicianName);
	}

	/**
	 * Convenience method to get the contents of the SOPClassUID element.
	 * @return the text of the element or null if the element does not exist.
	 */
	public String getSOPClassUID() {
		return getElementValue(Tags.SOPClassUID,null);
	}

	/**
	 * Convenience method to get the contents of the SOPInstanceUID element.
	 * @return the text of the element or null if the element does not exist.
	 */
	public String getSOPInstanceUID() {
		return getElementValue(Tags.SOPInstanceUID,null);
	}

	/**
	 * Convenience method to get the contents of the StudyInstanceUID element.
	 * @return the text of the element or null if the element does not exist.
	 */
	public String getStudyInstanceUID() {
		return getElementValue(Tags.StudyInstanceUID,null);
	}

	/**
	 * Convenience method to get the contents of the SeriesInstanceUID element.
	 * @return the text of the element or null if the element does not exist.
	 */
	public String getSeriesInstanceUID() {
		return getElementValue(Tags.SeriesInstanceUID,null);
	}

	/**
	 * Set the contents of a DICOM element in the DicomObject's dataset.
	 * @param tag the tag specifying the element (in the form 0xggggeeee).
	 * @param value the value to insert in the element.
	 */
	public void setElementValue(int tag, String value) {
		dataset.putXX(tag, value);
	}

	/**
	 * Convenience method to set the contents of the PatientName element.
	 */
	public void setPatientName(String patientName) {
		dataset.putXX(Tags.PatientName,patientName);
	}

	/**
	 * Convenience method to set the contents of the PatientID element.
	 */
	public void setPatientID(String patientID) {
		dataset.putXX(Tags.PatientID,patientID);
	}

	/**
	 * Convenience method to set the contents of the PatientID element.
	 */
	public void setClinicalTrialSubjectID(String id) {
		dataset.putXX(Tags.ClinicalTrialSubjectID,id);
	}

	/**
	 * Convenience method to set the contents of the PatientBirthDate element.
	 */
	public void setPatientBirthDate(String patientBirthDate) {
		dataset.putXX(Tags.PatientBirthDate,patientBirthDate);
	}

	/**
	 * Convenience method to set the contents of the PatientSex element.
	 */
	public void setPatientSex(String patientSex) {
		dataset.putXX(Tags.PatientSex,patientSex);
	}

	/**
	 * Convenience method to set the contents of the StudyDescription element.
	 */
	public void setStudyDescription(String studyDescription) {
		dataset.putXX(Tags.StudyDescription,studyDescription);
	}

	/**
	 * Convenience method to set the contents of the StudyDate element.
	 */
	public void setStudyDate(String studyDate) {
		dataset.putXX(Tags.StudyDate,studyDate);
	}

	/**
	 * Convenience method to set the contents of the StudyTime element.
	 */
	public void setStudyTime(String studyTime) {
		dataset.putXX(Tags.StudyTime,studyTime);
	}

	/**
	 * Convenience method to set the contents of the SeriesDate element.
	 */
	public void setSeriesDate(String seriesDate) {
		dataset.putXX(Tags.SeriesDate,seriesDate);
	}

	/**
	 * Convenience method to set the contents of the ContentDate element.
	 */
	public void setContentDate(String contentDate) {
		dataset.putXX(Tags.ContentDate,contentDate);
	}

	/**
	 * Convenience method to set the contents of the AccessionNumber element.
	 */
	public void setAccessionNumber(String accessionNumber) {
		dataset.putXX(Tags.AccessionNumber,accessionNumber);
	}

	/**
	 * Convenience method to set the contents of the InstanceNumber element.
	 */
	public void setInstanceNumber(String instanceNumber) {
		dataset.putXX(Tags.InstanceNumber,instanceNumber);
	}

	/**
	 * Convenience method to set the contents of the RequestedProcedureID element.
	 */
	public void setRequestedProcedureID(String rpID) {
		dataset.putXX(Tags.RequestedProcedureID,rpID);
	}

	/**
	 * Convenience method to set the contents of the IssuerOfPatientID element.
	 */
	public void setIssuerOfPatientID(String issuer) {
		dataset.putXX(Tags.IssuerOfPatientID, issuer);
	}

	/**
	 * Convenience method to set the contents of the BodyPartExamined element.
	 */
	public void setBodyPartExamined(String bodyPartExamined) {
		dataset.putXX(Tags.BodyPartExamined, bodyPartExamined);
	}

	/**
	 * Method to set a CodeSeq element.
	 */
	public void setCodeSeq(String name,
						   String codeValue,
						   String codingSchemeDesignator,
						   String codingSchemeVersion,
						   String codeMeaning) {
		try {
			if ((codeValue != null) && !codeValue.equals("")) {
				DcmElement de = dataset.putSQ(Tags.forName(name));
				Dataset ds = de.addNewItem();
				ds.putXX(Tags.CodeValue, codeValue);
				if ((codingSchemeDesignator != null) && !codingSchemeDesignator.equals("")) {
					ds.putXX(Tags.CodingSchemeDesignator, codingSchemeDesignator);
				}
				if ((codingSchemeVersion != null) && !codingSchemeVersion.equals("")) {
					ds.putXX(Tags.CodingSchemeVersion, codingSchemeVersion);
				}
				if ((codeMeaning != null) && !codeMeaning.equals("")) {
					ds.putXX(Tags.CodeMeaning, codeMeaning);
				}
				//dataset.dumpDataset(System.out, null);;
			}
		}
		catch (Exception ignore) { ignore.printStackTrace(); }
	}

	/**
	 * Convenience method to set the contents of the ScheduledProcedureStepID element.
	 */
	public void setScheduledProcedureStepID(String spsID) {
		dataset.putXX(Tags.SPSID,spsID);
	}

	/**
	 * Convenience method to set the contents of the InsitutionName element.
	 */
	public void setInstitutionName(String institutionName) {
		dataset.putXX(Tags.InstitutionName,institutionName);
	}

	/**
	 * Convenience method to set the contents of the SOPInstanceUID element.
	 */
	public void setSOPInstanceUID(String sopInstanceUID) {
		dataset.putXX(Tags.SOPInstanceUID,sopInstanceUID);
	}

	/**
	 * Convenience method to set the contents of the StudyInstanceUID element.
	 */
	public void setStudyInstanceUID(String studyInstanceUID) {
		dataset.putXX(Tags.StudyInstanceUID,studyInstanceUID);
	}

	/**
	 * Convenience method to set the contents of the SeriesInstanceUID element.
	 */
	public void setSeriesInstanceUID(String seriesInstanceUID) {
		dataset.putXX(Tags.SeriesInstanceUID,seriesInstanceUID);
	}

}
