/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

package org.rsna.geneva.elements;

import java.io.*;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;

//JAXP
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;

// FOP
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;

import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.util.XmlUtil;
import org.rsna.util.StringUtil;
import org.rsna.util.FileUtil;
import org.rsna.geneva.hl7.*;
import org.rsna.geneva.misc.RegSysEvent;
import org.rsna.geneva.misc.XDSSoapRequest;
import org.rsna.util.Base64;

public class Repository extends Product {

	static final Logger logger = Logger.getLogger(Repository.class);

	public String type = "Repository";

	public String globalAssigningAuthority;
	public String soapURL;
	public boolean sendsSOAP;
	public String soapVersion;
	public int docsetDelay = 0;

	public Repository(Element el) {
		super(el);
		this.enabled = !el.getAttribute("enabled").trim().equals("no");
		this.globalAssigningAuthority = el.getAttribute("globalAssigningAuthority").trim();
		this.globalAssigningAuthority = getGlobalAssigningAuthority();
		this.soapURL = el.getAttribute("soapURL").trim();
		this.sendsSOAP = !el.getAttribute("sendsSOAP").trim().equals("no");
		this.soapVersion = el.getAttribute("soapVersion").trim();
		if (this.soapVersion.equals("")) this.soapVersion = "SOAP_1_2";
		try { this.docsetDelay = Integer.parseInt(el.getAttribute("docsetDelay").trim()); }
		catch (Exception ex) { this.docsetDelay = 0; }
	}

	public String getGlobalAssigningAuthority() {
		if (!globalAssigningAuthority.equals(""))
			return globalAssigningAuthority;
		return Configuration.getInstance().getGlobalAssigningAuthority();
	}

	public void appendTableRow(StringBuffer sb) {
		sb.append("<tr><td><b>" + type + "</b><br>ID: "+id+"</td><td>");
		sb.append("<table border=\"0\" width=\"100%\">");
		appendDataRows(sb);
		sb.append("</table>");
		sb.append("</td></tr>");
	}

	public void appendDataRows(StringBuffer sb) {
		sb.append("<tr><td width=\"165\">enabled:</td><td>"+(enabled?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Global Assigning Authority:</td><td>"+globalAssigningAuthority+"</td></tr>");
		sb.append("<tr><td>SOAP URL:</td><td>"+soapURL+"</td></tr>");
		sb.append("<tr><td>Sends SOAP Message:</td><td>"+(sendsSOAP?"yes":"no")+"</td></tr>");
		sb.append("<tr><td>Thread Startup Delay (ms):</td><td>"+startupDelay+"</td></tr>");
		sb.append("<tr><td>Docset Delay (ms):</td><td>"+docsetDelay+"</td></tr>");
	}

	void processRegistration(Registration reg) {
		//Process the DocSets for this Repository
		DocSet[] docSets = Configuration.getInstance().getDocSets();
		if ((docSets.length > 0) && (docsetDelay > 0)) {
			try { Thread.sleep(docsetDelay); }
			catch (Exception ignore) { }
		}
		for (int i=0; i<docSets.length; i++) {
			if (docSets[i].enabled && docSets[i].repositoryID.equals(id)) {
				processDocSet(reg,docSets[i]);
			}
		}
	}

	void processDocSet(Registration reg, DocSet docSet) {
		Configuration config = Configuration.getInstance();
		//First make sure that the sex of this registrant
		//matches the constraint of this docset.
		String sex = docSet.sex;
		String regsex = reg.sex.toUpperCase().trim();
		boolean accepted = sex.equals("BOTH") || (regsex.length()==0)  || sex.startsWith(regsex.substring(0,1));
		if (!accepted) return;

		//A DocSet is a directory containing files for producing
		//a submission to a repository. There are three types of
		//DocSets:
		//
		//  1. One with files for producing a CDA only.
		//	2. One with files for producing a PDF and a CDA
		//	   which encapsulates the PDF.
		//  3. One with files for producing a PDF only.
		//
		//Types 1 and 2 are XDS-compliant. Type 3 is XDSI-compliant.
		//
		//In each case, the files (for the PDF and the CDA, as required)
		//are processed to insert information from the registration
		//into them where required.
		//
		//The result of the processing is an attachment which is
		//wrapped in XDS metadata and transmitted to the repository
		//in a SOAP request.
		try {
			//Make a temp directory.
			File temp = getTempDir(docSet.directoryFile);

			//Declare a File for the attachment.
			File attachment = null;
			String mimeType = "text/xml";

			//Set up the path so we point to the directory.
			//This is necessary because we will be working in
			//a temporary subdirectory and the transform will
			//need to reference objects in the docSet's directory.
			String path = docSet.directory.replaceAll("\\\\","/");

			//Setup the parameters for the transformations.
			//The same parameters are used in all the transformations
			//(xml to FO and doc to metadata).
			String date = docSet.date;
			if (date.indexOf("*") != -1) date = config.today();
			String uuid = config.getUUID();
			String[] params = new String[] {
				"path",					path,
				"patient-name",			reg.getName(),
				"full-name",			reg.getFullName(),
				"given-name",			reg.givenName,
				"family-name",			reg.familyName,
				"patient-id",			reg.globalID,
				"assigning-authority",	globalAssigningAuthority,
				"institution-name",		docSet.institutionName,
				"document-id",			config.getAccessionNumber(),
				"title",				docSet.title,
				"date",					date,
				"time",					config.now(),
				"street",				reg.street,
				"city",					reg.city,
				"state",				reg.state,
				"zip",					reg.zip,
				"country",				reg.country,
				"sex",					reg.sex,
				"birth-date",			reg.birthdate,
				"uuid",					uuid,
				"uid1",					config.getUID(),
				"uid2",					config.getUID(),
				"uid3",					config.getUID(),
				"uid4",					config.getUID(),
				"pdf",					""	//This param must be last in the array (see *** below).
			};

			String pdfMessage = "";

			//If the pdfSource.xml file (containing the instructions for
			//producing the PDF file) exists, process it and set the attachment
			//to point to the resulting PDF.
			File pdfSource = new File(docSet.directoryFile,"pdfSource.xml");
			if (pdfSource.exists()) {

				//The process for producing a PDF is first to transform the
				//pdfSource.xml file into a formatting objects file and then
				//to convert the fcFile into a PDF.
				File foFile = File.createTempFile("DS-",".fo",temp);

				//Get the transform program.
				File pdfSourceToFO = new File("docxsl/pdfSourceToFO.xsl");

				//Create the FO file and save it to disk
				FileUtil.setFileText(
					foFile,
					XmlUtil.toString(
						XmlUtil.getTransformedDocument(
							pdfSource, pdfSourceToFO, params
						)
					)
				);

				//Now convert the FO file to a PDF
				File pdfFile = new File(foFile.getParentFile(),foFile.getName()+".pdf");
				int pages = convertFO2PDF(foFile,pdfFile);
				pdfMessage = "PDF ("+pages+" page"+((pages!=1)?"s":"")+") created.<br>";

				//Now get the PDF as a byte array and encode it as a B64 string
				//in case we will be encapsulating the file in a CDA.
				byte[] pdfBytes = FileUtil.getFileBytes(pdfFile);
				String pdfB64 = Base64.encodeToString(pdfBytes);

				//Put the encoded PDF in the parameters.
				//The entry is already there, but the value is empty.
		/***/	params[params.length-1] = pdfB64;	/***/

				//Since we actually produced a PDF and don't yet know whether we will
				//produce a CDA, let the attachment point to the PDF for now.
				attachment = pdfFile;
				mimeType = "application/pdf";
			}

			//If the cdaSource.xml file (containing the instructions for
			//producing the CDA file) exists, process it and set the attachment
			//to point to the resulting CDA.
			File cdaSource = new File(docSet.directoryFile,"cdaSource.xml");
			if (cdaSource.exists()) {

				File cda = new File(temp,"cda.xml");

				//Get the transform program.
				File cdaSourceToCDA = new File("docxsl/cdaSourceToCDA.xsl");

				//Create the CDA
				FileUtil.setFileText(
					cda,
					XmlUtil.toString(
						XmlUtil.getTransformedDocument(
							cdaSource, cdaSourceToCDA, params
						)
					)
				);

				//Since we actually produced a CDA, make it the attachment.
				//If a PDF was produced, it must have been encapsulated in the CDA.
				attachment = cda;
				mimeType = "text/xml";
			}

			//Create the metadata file for transmission to the Repository.
			File metadataSource = new File(docSet.directoryFile,"metadataSource.xml");
			File metadata = new File(temp,"metadata.xml");
			File metadataSourceToMetadata = new File("docxsl/metadataSourceToMetadata.xsl");
			if (!metadataSourceToMetadata.exists() || !metadataSourceToMetadata.isFile()) return;
			FileUtil.setFileText(
				metadata,
				XmlUtil.toString(
					XmlUtil.getTransformedDocument(
						metadataSource, metadataSourceToMetadata, params
					)
				)
			);

			String responseText = "transmission disabled";
			if (sendsSOAP) {
				//Transmit the metadata and the attachment to the Repository.
				XDSSoapRequest xsr = new XDSSoapRequest(
						metadata,
						attachment,
						mimeType,
						soapVersion);
				SOAPMessage response = xsr.send(soapURL);
				responseText = xsr.getText(response);
			}

			//Finally, log an event.
			RegSysEvent event =
				new RegSysEvent(
						this,
						toStatus(responseText),
						RegSysEvent.TYPE_SOAP,
						"DocSet "+docSet.id+" processed.<br>"
							+pdfMessage
							+"SOAP Response: "+StringUtil.displayable(responseText)
					);
			config.getEventLog().append(event);
			if (isOK(responseText)) FileUtil.deleteAll(temp);

			//At this point, we are done. delete the temp directory.
			//Note that if an exception occurs, the temp directory will
			//not be deleted. This is intentional.
			FileUtil.deleteAll(temp);
		}
		catch (Exception ex) {
			//Get the top of the stack track
			StackTraceElement[] ste = ex.getStackTrace();
			String topElement = ste[0].toString();
			//Log an event
			RegSysEvent event =
				new RegSysEvent(
						this,
						RegSysEvent.STATUS_ERROR,
						RegSysEvent.TYPE_SOAP,
						"Unable to process DocSet "+docSet.id+
						"<br>"+StringUtil.displayable(ex.getMessage())+
						"<br>"+StringUtil.displayable(topElement)
					);
			config.getEventLog().append(event);
			//logger.warn("Unable to process DocSet "+docSet.id,ex);
		}
	}

	private synchronized File getTempDir(File dir) throws Exception {
		File temp = File.createTempFile("temp-","",dir);
		temp.delete();
		temp.mkdirs();
		return temp;
	}

	public static boolean isOK(String response) {
		return (response.toLowerCase().indexOf("status=\"success\"") != -1);
	}

	public static int toStatus(String response) {
		return  isOK(response) ? RegSysEvent.STATUS_OK : RegSysEvent.STATUS_ERROR;
	}

    //Convert an FO file to a PDF file using FOP
    private int convertFO2PDF(File fo, File pdf) throws Exception {

        OutputStream out = null;
    	FopFactory fopFactory = FopFactory.newInstance();

        try {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            // Setup output stream.  Note: Using BufferedOutputStream
            // for performance reasons (helpful with FileOutputStreams).
            out = new FileOutputStream(pdf);
            out = new BufferedOutputStream(out);

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Setup input stream
            Source src = new StreamSource(fo);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);
            out.close();

            // Result processing
            FormattingResults foResults = fop.getResults();
            return foResults.getPageCount();
        }
        catch (Exception e) {
            if (out != null) out.close();
			throw e;
		}
    }

	public String getType() {
		return type;
	}

	public Element getXML() {
		try {
			Element e = super.getXML();
			e.setAttribute("globalAssigningAuthority", globalAssigningAuthority);
			e.setAttribute("soapURL", soapURL);
			e.setAttribute("sendsSOAP", yesNo(sendsSOAP));
			e.setAttribute("soapVersion", soapVersion);
			e.setAttribute("docsetDelay", Integer.toString(docsetDelay));
			return e;
		}
		catch (Exception ex) { return null; }
	}

	public static Element createNewElement(String name, String id) {
		try {
			Element e = Product.createNewElement(name, id);
			e.setAttribute("type", "Repository");
			e.setAttribute("soapVersion", "SOAP_1_2");
			e.setAttribute("sendsSOAP", "yes");
			e.setAttribute("docsetDelay", "10000");
			Configuration config = Configuration.getInstance();
			e.setAttribute("globalAssigningAuthority", config.getGlobalAssigningAuthority());
			return e;
		}
		catch (Exception ex) { return null; }
	}
}
