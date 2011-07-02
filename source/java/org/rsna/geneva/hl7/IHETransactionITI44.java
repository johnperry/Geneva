package org.rsna.geneva.hl7;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;

import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;

public class IHETransactionITI44 implements PatientIdFeed {
    private static String name = "Patient Id Feed, ITI44, hl7v3";
    private RegistrationXml regx;
    private String hl7URL;

    private Registration reg;
    private Configuration cfg;
    private Transformer transformer;

    private SOAPWrapper wrapper;
    private String soapVersion;
    private boolean saveMessages;
    private int timeout;

    public IHETransactionITI44 (Element el,
                                Registration reg,
                                Configuration cfg)
    {
        try {
            regx = new RegistrationXml( el, reg, cfg);
            transformer
                = IHETransactionTransformerFactory.getITI44Transformer();
        }
        catch( Exception e) {
            throw new IllegalArgumentException(
                "Error creating ITI44 Transaction.", e);
        }

        this.reg = reg;
        this.cfg = cfg;
        wrapper = null;
        saveMessages = false;

        soapVersion = el.getAttribute("soapVersion").trim();
        if( soapVersion == null || soapVersion.equals("")) {
            soapVersion = "SOAP_1_2";
        }

        hl7URL = el.getAttribute("hl7URL").trim();
        if( hl7URL == null || hl7URL.equals("")) {
            throw new IllegalArgumentException(
                "Missing hl7URL attribute.");
        }
    }

    public String getName() {
        return name;
    }

    public void setAssigningAuthority( String s) {
        regx.setAssigningAuthority( s);
    }

    public void setPatientId( String s) {
        regx.setPatientId( s);
    }

    public void setSaveMessages( boolean b) {
        saveMessages = b;
    }

    public void setTimeout( int i) {
        timeout = i;
    }

    /**
     * send this transaction.
     */
    public IHETransactionResponse send() {

        try {
/*
            DOMResult result = new DOMResult();
            transformer.transform( regx.getSource(), result);

            Document doc = DocumentBuilderFactory.newInstance()
                                                 .newDocumentBuilder()
                                                 .newDocument();
            //doc.adoptNode( result.getNode());
            doc.importNode( result.getNode(), true);
*/
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform( regx.getSource(), result);

            StringReader sr = new StringReader(sw.toString());
            Document doc = DocumentBuilderFactory.newInstance()
                                                 .newDocumentBuilder()
                                                 .parse(new InputSource(sr));

            Map<String, Object> config = new HashMap<String, Object>();
            config.put("wsa:ReplyTo",
                new HashMap<String, Object>() {{
            //        put("wsa:Address",
            //            "http://schemas.xmlsoap.org/ws/2004/08/addressing/anonymous");
                    put("wsa:Address",
                        "http://www.w3.org/2005/08/addressing/anonymous");
                }});
            config.put("wsa:To", hl7URL);
            config.put("wsa:MessageID", UUID.class);

            wrapper = new SOAPWrapper( doc, config, soapVersion);
            wrapper.generateSoapEnvelope();

            URL endpnt = new URL( hl7URL);
            SOAPMessage response = wrapper.send(endpnt);

            if( response == null) {
                return new IHETransactionResponse( 1, "response is NULL.");
            }

            SOAPBody body = response.getSOAPBody();
            if( body.hasFault()) {
                SOAPFault fault = body.getFault();
                StringBuffer sb = new StringBuffer("\nError: Soap Fault:\n");
                sb.append( "[Fault code: " + fault.getFaultCode() + "]");
                sb.append( "\n[Fault string: " + fault.getFaultString() + "]");
                return new IHETransactionResponse( 1, sb.toString());
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            response.writeTo(os);

            return new IHETransactionResponse( 0, "Success: " + os.toString());
        }
        catch( Exception e) {
            /*
            StringBuffer sb
                = new StringBuffer( "Failed: " + e.getMessage() + "\n");
            StackTraceElement[] ste = e.getStackTrace();
            for( StackTraceElement s : ste) {
                sb.append(s + "\n");
            }
            Throwable cause = e.getCause();
            if( cause != null) {
                sb.append("<br>Cause: " + cause.getMessage());
                ste = cause.getStackTrace();
                for( StackTraceElement s : ste) {
                    sb.append(s + "\n");
                }
            }
            return new IHETransactionResponse( 1, sb.toString());
           */
            return new IHETransactionResponse( 1, dumpException(e));
        }
    }

    public String dumpException( Exception e) {
        if( e == null) return "";

        StringBuffer sb
            = new StringBuffer( "Failed: " + e.getMessage() + "\n");
        StackTraceElement[] ste = e.getStackTrace();
        for( StackTraceElement s : ste) {
            sb.append(s + "\n");
        }
        Throwable cause = e.getCause();
        while( cause != null ) {
            sb.append("Cause: " + cause.getMessage() + "\n");
            //ste = cause.getStackTrace();
            //for( StackTraceElement s : ste) {
            //    sb.append(s + "\n");
            //}
            cause = cause.getCause();
        }
        return sb.toString();
    }

    public String toString() {
        if( wrapper == null) {
            return "regx: " + regx.toString();
        }
        else {
            return wrapper.toString();
        }
    }
}

