/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.objects;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.DictionaryFactory;
import org.dcm4che.dict.TagDictionary;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDDictionary;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;

import org.rsna.geneva.main.Configuration;

/**
  * Class which encapsulates a DICOM KOS object.
  */
public class KOS {

	static final DcmParserFactory pFact = DcmParserFactory.getInstance();
	static final DcmObjectFactory oFact = DcmObjectFactory.getInstance();
	static final DictionaryFactory dFact = DictionaryFactory.getInstance();
	static final TagDictionary tagDictionary = dFact.getDefaultTagDictionary();
	static final UIDDictionary uidDictionary = dFact.getDefaultUIDDictionary();

	Dataset dataset = null;
	KOSContent kosContent = null;
	Configuration config = null;;
	boolean firstInstance = true;
	String retrieveAET = null;
	String institutionName = null;

	public KOS(Configuration config, String retrieveAET, String institutionName) {
		this.config = config;
		this.retrieveAET = retrieveAET;
		this.institutionName = institutionName;
		dataset = oFact.newDataset();
		insertSOPCommonModule();
		insertKeyObjectSeriesModule();
		insertKeyObjectDocumentModule();
		insertGeneralEquipmentModule();
		kosContent = insertSRDocumentContentModule();
	}

	public void add(DicomObject dicomObject) {
		if (firstInstance) {
			firstInstance = false;
			insertPatientModule(dicomObject);
			insertGeneralStudyModule(dicomObject);
		}
		kosContent.add(dicomObject);
	}

	/**
	 * Save the manifest in a file.
	 * @param file the File designating where to save the manifest.
	 */
	public File save(File file) throws Exception {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);

			// Select the encoding
			String prefEncodingUID = UIDs.ExplicitVRLittleEndian;

			// Create and write the metainfo for the encoding we are using
			FileMetaInfo fmi = oFact.newFileMetaInfo(dataset, prefEncodingUID);
			dataset.setFileMetaInfo(fmi);
			fmi.write(out);

			// write the dataset
			DcmEncodeParam encoding = DcmDecodeParam.valueOf(prefEncodingUID);
			dataset.writeDataset(out, encoding);
			out.flush();
			out.close();
			return file;
		}

		catch (Exception ex) {
			try { if (out != null) out.close(); }
			catch (Exception ignore) { }
			throw ex;
		}
	}

	private void insertSOPCommonModule() {
		dataset.putUI(Tags.SOPClassUID, UIDs.KeyObjectSelectionDocument);
		dataset.putUI(Tags.SOPInstanceUID, config.getUID());
	}

	private void insertKeyObjectSeriesModule() {
		dataset.putCS(Tags.Modality, "KO");
		dataset.putUI(Tags.SeriesInstanceUID, config.getUID());
		dataset.putIS(Tags.SeriesNumber, "1");
	}

	private void insertKeyObjectDocumentModule() {
		dataset.putIS(Tags.InstanceNumber, "1");
		dataset.putDA(Tags.ContentDate, config.today());
		dataset.putTM(Tags.ContentTime, config.now());
	}

	private void insertGeneralEquipmentModule() {
		dataset.putLO(Tags.Manufacturer, "");
		dataset.putLO(Tags.InstitutionName, institutionName);
	}

	private void insertPatientModule(DicomObject dicomObject) {
		dataset.putPN(Tags.PatientName, dicomObject.getPatientName());
		dataset.putLO(Tags.PatientID, dicomObject.getPatientID());
		dataset.putDA(Tags.PatientBirthDate, dicomObject.getPatientBirthDate());
		dataset.putCS(Tags.PatientSex, dicomObject.getPatientSex());
	}

	private void insertGeneralStudyModule(DicomObject dicomObject) {
		dataset.putUI(Tags.StudyInstanceUID, dicomObject.getStudyInstanceUID());
		dataset.putDA(Tags.StudyDate, dicomObject.getStudyDate());
		dataset.putTM(Tags.StudyTime, dicomObject.getStudyTime());
		dataset.putPN(Tags.ReferringPhysicianName, dicomObject.getReferringPhysicianName());
		dataset.putSH(Tags.StudyID, dicomObject.getStudyID());
		dataset.putSH(Tags.AccessionNumber, dicomObject.getAccessionNumber());
	}

	private KOSContent insertSRDocumentContentModule() {
		dataset.putCS(Tags.ValueType, "CONTAINER");
		dataset.putCS(Tags.ContinuityOfContent, "SEPARATE");
		DcmElement cncs = dataset.putSQ(Tags.ConceptNameCodeSeq);
		Dataset cncsItem = cncs.addNewItem();
		cncsItem.putSH(Tags.CodeValue,"113030");
		cncsItem.putSH(Tags.CodingSchemeDesignator,"DCM");
		cncsItem.putLO(Tags.CodeMeaning,"Manifest");
		KOSContent content = new KOSContent(dataset);
		return content;
	}

	class KOSContent {
		Dataset dataset;
		DcmElement crpes;
		DcmElement cs;
		Hashtable<String,Study> studiesTable;

		public KOSContent(Dataset dataset) {
			this.dataset = dataset;
			studiesTable = new Hashtable<String,Study>();
			crpes = dataset.putSQ(Tags.CurrentRequestedProcedureEvidenceSeq);
			cs = dataset.putSQ(Tags.ContentSeq);
		}

		public void add(DicomObject dicomObject) {
			String studyInstanceUID = dicomObject.getStudyInstanceUID();
			Study study = studiesTable.get(studyInstanceUID);
			if (study == null) {
				study = new Study(crpes,studyInstanceUID);
				studiesTable.put(studyInstanceUID,study);
			}
			study.add(dicomObject);
			//Now put the object into the ContentSeq
			Dataset csItem = cs.addNewItem();
			DcmElement rss = csItem.putSQ(Tags.RefSOPSeq);
			Dataset rssItem = rss.addNewItem();
			rssItem.putUI(Tags.RefSOPClassUID,dicomObject.getSOPClassUID());
			rssItem.putUI(Tags.RefSOPInstanceUID,dicomObject.getSOPInstanceUID());
			csItem.putCS(Tags.RelationshipType,"CONTAINS");
			csItem.putCS(Tags.ValueType,"IMAGE");
		}
	}

	class Study {
		DcmElement crpes;
		Dataset studyItem;
		DcmElement refSeriesSeq;
		Hashtable<String,Series> seriesTable;

		public Study(DcmElement crpes, String studyInstanceUID) {
			this.crpes = crpes;
			seriesTable = new Hashtable<String,Series>();
			studyItem = crpes.addNewItem();
			refSeriesSeq = studyItem.putSQ(Tags.RefSeriesSeq);
			studyItem.putUI(Tags.StudyInstanceUID, studyInstanceUID);
		}

		public void add(DicomObject dicomObject) {
			String seriesInstanceUID = dicomObject.getSeriesInstanceUID();
			Series series = seriesTable.get(seriesInstanceUID);
			if (series == null) {
				series = new Series(refSeriesSeq,seriesInstanceUID);
				seriesTable.put(seriesInstanceUID,series);
			}
			series.add(dicomObject);
		}
	}

	class Series {
		DcmElement refSeriesSeq;
		Dataset seriesItem;
		DcmElement refSOPSeq;

		public Series(DcmElement refSeriesSeq, String seriesInstanceUID) {
			this.refSeriesSeq = refSeriesSeq;
			seriesItem = refSeriesSeq.addNewItem();
			refSOPSeq = seriesItem.putSQ(Tags.RefSOPSeq);
			seriesItem.putAE(Tags.RetrieveAET, retrieveAET);
			seriesItem.putUI(Tags.SeriesInstanceUID, seriesInstanceUID);
		}

		public void add(DicomObject dicomObject) {
			Dataset item = refSOPSeq.addNewItem();
			item.putUI(Tags.RefSOPClassUID,dicomObject.getSOPClassUID());
			item.putUI(Tags.RefSOPInstanceUID,dicomObject.getSOPInstanceUID());
		}
	}
}