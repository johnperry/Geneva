/*---------------------------------------------------------------
*  Copyright 2005 by the Radiological Society of North America
*
*  This source software is released under the terms of the
*  RSNA Public License (http://mirc.rsna.org/rsnapubliclicense)
*----------------------------------------------------------------*/

/*---------------------------------------------------------------
* Adapted from xdsSoapSubmission.java by author "gunn"
* provided by Bill Majurski of NIST
*----------------------------------------------------------------*/

package org.rsna.geneva.misc;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.Iterator;
import java.net.ConnectException;
import java.security.PrivilegedActionException;
import java.io.IOException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Text;
import javax.xml.soap.AttachmentPart;
import java.net.URL;
import javax.mail.internet.MimeMultipart;
import javax.activation.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;

/**
 * A class to encapsulate a SOAP message and to send it.
 */
public class XDSSoapRequest {

	SOAPMessage message;

	/**
	 * Create the SOAP message.
	 * @param metadataFile the file containing the SOAP metadata.
	 * @param attachedFile the attachment to the SOAP message.
	 * @param mimeType the mimeType of the attachment.
	 * @param soapVersion "SOAP_1_1" for SOAP version 1; default is version 2.
	 */
	public XDSSoapRequest(
					File metadataFile,
					File attachedFile,
					String mimeType,
					String soapVersion) throws Exception {

        MessageFactory messageFactory =
        		((soapVersion != null) && soapVersion.equals("SOAP_1_1"))
            ? MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL)
            : MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		message = messageFactory.createMessage();
		SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
		SOAPHeader soapHeader = envelope.getHeader();

		//These two lines were in the module for XDS.a.
		//They were removed in a vain attempt to get it to run for XDS.b.
		//soapHeader.addChildElement("SOAPAction");
		//soapHeader.addTextNode(" ");  //add an empty string to the SOAP header

		SOAPBody soapBody = envelope.getBody();
		SOAPElement bodyElement = soapBody.addDocument(buildDoc(metadataFile));

		// Attach the file.
		DataSource attachmentDS = new FileDataSource(attachedFile);
		DataHandler attachmentDH = new DataHandler(attachmentDS);

		// Set the mime type for the attachment.
		AttachmentPart attachmentPart = message.createAttachmentPart(attachmentDH);
		attachmentPart.setMimeHeader("Content-Type", mimeType);
		 //Is the next line correct, or should it be the UUID of the attachment?
		 //(In the Geneva docset metadata files, the attachment is called "theDocument".)
		attachmentPart.setContentId("<theDocument>");
		message.addAttachmentPart(attachmentPart);
	}

	/**
	 * Send the SOAP message created in the constructor.
	 * @param url the destination URL.
	 * @return the SOAP response.
	 */
	public SOAPMessage send(String url) throws Exception {
		SOAPConnectionFactory connFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection conn = connFactory.createConnection();
		SOAPMessage response = conn.call(message, url);
		conn.close();
		return response;
	}

	//Use the JAXP API to build a DOM Document. This parses the
	//SubmitObjectsRequest containing registry metadata for addition
	//to the soap body.
	private Document buildDoc(File documentFile) throws Exception {
		Document document = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		document = builder.parse(documentFile);
		return document;
	}

	/**
	 * Get the text of a SOAP message
	 * @param message the SOAP message
	 * @return the text value of the message, or the empty string if an error occurs.
	 */
	public static String getText(SOAPMessage message) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			message.writeTo(baos);
			return baos.toString();
		}
		catch (Exception ex) { return ""; }
	}

}
