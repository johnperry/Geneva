package org.rsna.geneva.hl7;

import java.io.File;
import java.io.ByteArrayOutputStream;
//import java.io.StringWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class SOAPWrapper
{
    private static final Map<String, Object> defaultConf
        = new HashMap<String, Object>() {{
            put("wsa:replyTo",
                new HashMap<String, Object>() {{
                    put("wsa:address",
                        "http://www.w3.org/2005/08/addressing/anonymous");
                }});
            put("wsa:To|mustUnderstand=1",
                "http://128.252.175.111:80");
            put("wsa:MessageID", UUID.class);
    }};

    private Map<String, Object> config;
    private Document messageDocument;
    private SOAPMessage soapMessage;
    private String soapVersion;

    /**
     * Generates (and stores) the SOAP wrapper around the document.
     *
     * @throws SOAPException
     * @throws XPathException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws ParserConfigurationException
    */
    public void generateSoapEnvelope()
        throws SOAPException, XPathException, TransformerException,
               ParserConfigurationException
    {
        MessageFactory factory = soapVersion.equals("SOAP_1_1")
            ? MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL)
            : MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage msg = factory.createMessage();
        SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        SOAPHeader head = msg.getSOAPHeader();

        //env.addNamespaceDeclaration("wsa",
        //    "http://schemas.xmlsoap.org/ws/2004/08/addressing");
        env.addNamespaceDeclaration("wsa",
            "http://www.w3.org/2005/08/addressing");

        insertHeaderElements(head, config);

        head.addChildElement("Action", "wsa")
 //           .addAttribute(env.createName("env:mustUnderstand"), "1")
            .addAttribute(env.createName("mustUnderstand"), "1")
            .addTextNode("urn:hl7-org:v3:" + getMessageName());
        clone(messageDocument);
        msg.getSOAPBody().addDocument(clone(messageDocument));
        this.soapMessage = msg;
    }

    /**
     * Inserts header elements from the configuration map based on their type.
     * Strings are inserted as text nodes, nested maps get processed
     * recursively, and UUID class references are used to generate a new random
     * UUID.
     *
     * @param head
     * @param conf
     * @throws SOAPException
    */
    @SuppressWarnings("unchecked")
    private void insertHeaderElements(SOAPElement head,
                                      Map<String, Object> conf)
        throws SOAPException
    {
        for ( String o : conf.keySet() ) {
            Object v = conf.get(o);
            SOAPElement e = makeElement(head, o);

            if ( v instanceof String ) {
                e.addTextNode((String)v);
            }
            else if ( v instanceof Map ) {
                insertHeaderElements(e, (Map<String, Object>) v);
            }
            else if ( v instanceof Class && v == UUID.class ) {
                e.addTextNode( "uuid:" + UUID.randomUUID().toString());
            }
        }
    }

    /**
     * Makes a header element based on a key name. The format is:
     * namespace:tag [| namespace:attribute=value, ... ]
     *
     * @param head
     * @param o
     * @return
     * @throws SOAPException
    */
    private SOAPElement makeElement(SOAPElement head, String o)
        throws SOAPException
    {
        String tag, ns;
        String[] attrs = new String[0], parts;

        parts = o.split(":", 2);
        tag = parts[1].trim();
        ns = parts[0].trim();

        parts = tag.split("\\|", 2);
        if ( parts.length > 1 ) {
            tag = parts[0].trim();
            attrs = parts[1].trim().split("(?<!\\\\),"); // negative lookbehind for backslash
        }

        SOAPElement e = head.addChildElement(tag, ns);
        for ( String attrStr : attrs ) {
            String[] attr = attrStr.split("=", 2);
            String name = attr[0].trim();
            String val = attr[1].trim().replace("\\", ""); // remove ignored backslashes
            e.setAttribute(name, val);
        }
        return e;
    }

    /**
     * Sends the SOAP message to the specified endpoint, blocking until a
     * response is recieved.
     */
    public SOAPMessage send(URL endpoint)
        throws SOAPException, XPathException, TransformerException,
               ParserConfigurationException
    {
        SOAPMessage response = null;
        SOAPConnectionFactory soapConnectionFactory
            = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = soapConnectionFactory.createConnection();
        if ( this.soapMessage == null ) generateSoapEnvelope();

        MimeHeaders hd = soapMessage.getMimeHeaders();
        //hd.addHeader("SOAPAction", getSOAPHeaderElement("//wsa:Action"));
        hd.addHeader("SOAPAction", "\""+getSOAPHeaderElement("//wsa:Action")+"\"");

        response = connection.call(soapMessage, endpoint);
        connection.close();

        return response;
    }

    /**
     * Sends the SOAP message with the endpoint given in the header's To field,
     * blocking until a response is recieved.
     */
    public SOAPMessage send()
        throws MalformedURLException, SOAPException, XPathException,
               TransformerException, ParserConfigurationException
    {
        return send( new URL( getSOAPHeaderElement("//wsa:To") ) );
    }

    private String getSOAPHeaderElement(String XpathToElement)
        throws XPathException, SOAPException
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(wsaNS);
        return xpath.evaluate(XpathToElement, soapMessage.getSOAPHeader());
    }

    public static Document clone(Document doc)
        throws TransformerException, ParserConfigurationException
    {
        Document output = null;
        DocumentBuilderFactory docFactory
            = DocumentBuilderFactory.newInstance();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        output = docBuilder.newDocument();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        DOMResult result = new DOMResult(output);
        transformer.transform(source, result);
        return output;
    }

    /**
     * The namespace context for all wsa:* fields.
    */
    private static final NamespaceContext wsaNS = new NamespaceContext()
    {
        public String getNamespaceURI(String prefix)
        {
            if ( prefix == null ) throw new NullPointerException("Null prefix");
            else if ( "wsa".equals(prefix) )
                return "http://schemas.xmlsoap.org/ws/2004/08/addressing";
            else if ( "xml".equals(prefix) ) return XMLConstants.XML_NS_URI;
            else return "";
        }

        public String getPrefix(String namespaceURI) { return null; }
        public Iterator getPrefixes(String namespaceURI) { return null; }
    };

    /**
     * Constructor
     */
    public SOAPWrapper(Document messageDocument,
                       Map<String, Object> config,
                       String soapVersion)
    {
        this.messageDocument = messageDocument;
        this.config = config;
        this.soapVersion = soapVersion;
    }

    /**
     * Convenience Constructor with default soap version = 1.2.
     */
    public SOAPWrapper(Document messageDocument, Map<String, Object> config)
    {
        this(messageDocument, config, "SOAP_1_2");
    }

    /**
     * Convenience Constructor with default configuration.
     */
    public SOAPWrapper(Document messageDocument, String soapVersion)
    {
        this(messageDocument, defaultConf, soapVersion);
    }

    /**
     * Convenience Constructor with default configuration and default
     * soap version = 1.2.
     */
    public SOAPWrapper(Document messageDocument)
    {
        this(messageDocument, defaultConf);
    }

    /**
     * Convenience Constructor.
     */
    public SOAPWrapper(File file,
                       Map<String, Object> config,
                       String soapVersion)
        throws SAXException, IOException, ParserConfigurationException
    {
        this( DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(file),
              config, soapVersion);
    }

    /**
     * Convenience Constructor.
     */
    public SOAPWrapper(File file, Map<String, Object> config)
        throws SAXException, IOException, ParserConfigurationException
    {
        this( DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(file),
              config);
    }

    /**
     * Convenience Constructor.
     */
    public SOAPWrapper(File file, String soapVersion)
        throws SAXException, IOException, ParserConfigurationException
    {
        this( DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(file),
              soapVersion);
    }

    /**
     * Convenience Constructor.
     */
    public SOAPWrapper(File file)
        throws SAXException, IOException, ParserConfigurationException
    {
        this( DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder()
                                    .parse(file));
    }

    public SOAPMessage getSoapMessage()
    {
        return this.soapMessage;
    }

    public String getMessageName()
    {
        return messageDocument.getDocumentElement().getNodeName();
    }

    public String toString() {
        if( soapMessage == null) {
            return "soapMessage is null\n";
        }
        else {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                soapMessage.writeTo( os);
                return os.toString();
/*
                StringBuffer sb = new StringBuffer();
                StringWriter sw = new StringWriter();
                DOMSerializer ds = new DOMSerializer();
                ds.setIndent(2);
                ds.serializeNode( soapMessage.getSOAPHeader(), sw, "");
                sb.append("<br>SOAP Header: ");
                sb.append(sw.toString());
                sw = new StringWriter();
                ds.serializeNode( soapMessage.getSOAPBody(), sw, "");
                sb.append("<br>SOAP body: ");
                sb.append(sw.toString());
                return sb.toString();
*/
            }
            catch( Exception e) {
                return "Failed serializing message: " + e.getMessage();
            }
        }
    }
}
