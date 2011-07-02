package org.rsna.geneva.hl7;

import java.io.IOException;
import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;

/**
 * Repackage info from the configuration and registration event into an
 * xml format that can be translated into the iti44 message.
 */
public class RegistrationXml {
    private Document doc;

    public RegistrationXml ( Element el, Registration reg, Configuration cfg)
        throws ClassNotFoundException, InstantiationException,
               IllegalAccessException
    {
        DOMImplementationRegistry registry
            = DOMImplementationRegistry.newInstance();
        DOMImplementation domImpl = registry.getDOMImplementation("XML 1.0");
        doc = domImpl.createDocument(null, "registration", null);

        Element root = doc.getDocumentElement();

        root.appendChild(getCreationTimeElement( el, reg, cfg));
        root.appendChild(getReceiverElement( el, reg, cfg));
        root.appendChild(getSenderElement( el, reg, cfg));

        // patient
        Element patient = doc.createElement("patient");
        patient.appendChild(getPatientIdElement( el, reg, cfg));
        patient.appendChild(getPatientNameElement( el, reg, cfg));
        patient.appendChild(getGenderCodeElement( el, reg, cfg));
        patient.appendChild(getBirthdateElement( el, reg, cfg));
        patient.appendChild(getAddressElement( el, reg, cfg));
        patient.appendChild(getProviderOrganizationElement( el, reg, cfg));

        root.appendChild(patient);
    }

    private Element getCreationTimeElement( Element el,
                                            Registration reg,
                                            Configuration cfg)
    {
        Element creationTimeElement = doc.createElement("creationTime");
        Text creationTime = doc.createTextNode( cfg.getDateTime() );
        creationTimeElement.appendChild(creationTime);
        return creationTimeElement;
    }

    private Element getReceiverElement( Element el,
                                        Registration reg,
                                        Configuration cfg)
    {
        Element receiverElement = doc.createElement("receiver");

        Element assigningAuthorityName
            = doc.createElement("assigningAuthorityName");
        assigningAuthorityName.appendChild(
            doc.createTextNode( rawOID(el.getAttribute("receiverDeviceId" ))));

        Element name = doc.createElement("name");
        name.appendChild(
            doc.createTextNode( el.getAttribute("receiverDeviceName" )));

        receiverElement.appendChild(assigningAuthorityName);
        receiverElement.appendChild(name);
        return receiverElement;
    }

    private Element getSenderElement( Element el,
                                      Registration reg,
                                      Configuration cfg)
    {
        Element senderElement = doc.createElement("sender");

        Element assigningAuthorityName
            = doc.createElement("assigningAuthorityName");
        assigningAuthorityName.appendChild(
                doc.createTextNode( rawOID( cfg.senderDeviceId )));

        Element name = doc.createElement("name");
        name.appendChild( doc.createTextNode( cfg.senderDeviceName ));

        senderElement.appendChild(assigningAuthorityName);
        senderElement.appendChild(name);
        return senderElement;
    }

    private Element getPatientIdElement( Element el,
                                         Registration reg,
                                         Configuration cfg)
    {
        Element id = doc.createElement("id");

        Element extension = doc.createElement("extension");
        extension.appendChild( doc.createTextNode( reg.globalID ));

        Element root = doc.createElement("root");
        root.appendChild( doc.createTextNode( rawOID(cfg.globalAssigningAuthority )));

        id.appendChild(extension);
        id.appendChild(root);
        return id;
    }

    private Element getPatientNameElement( Element el,
                                           Registration reg,
                                           Configuration cfg)
    {
        Element name = doc.createElement("name");

        Element family = doc.createElement("family");
        family.appendChild( doc.createTextNode( reg.familyName));

        Element given = doc.createElement("given");
        given.appendChild( doc.createTextNode( reg.givenName));

        name.appendChild(family);
        name.appendChild(given);
        return name;
    }

    private Element getGenderCodeElement( Element el,
                                          Registration reg,
                                          Configuration cfg)
    {
        Element genderCode = doc.createElement("genderCode");
        genderCode.appendChild( doc.createTextNode( reg.sex));
        return genderCode;
    }

    private Element getBirthdateElement( Element el,
                                         Registration reg,
                                         Configuration cfg)
    {
        Element birthDate = doc.createElement("birthDate");
        birthDate.appendChild( doc.createTextNode( reg.birthdate));
        return birthDate;
    }

    private Element getAddressElement( Element el,
                                       Registration reg,
                                       Configuration cfg)
    {
        Element address = doc.createElement("address");

        Element streetAddressLine = doc.createElement("streetAddressLine");
        streetAddressLine.appendChild( doc.createTextNode( reg.street));

        Element city = doc.createElement("city");
        city.appendChild( doc.createTextNode( reg.city));

        Element state = doc.createElement("state");
        state.appendChild( doc.createTextNode( reg.state));

        Element postalCode = doc.createElement("postalCode");
        postalCode.appendChild( doc.createTextNode( reg.zip));

        address.appendChild(streetAddressLine);
        address.appendChild(city);
        address.appendChild(state);
        address.appendChild(postalCode);
        return address;
    }

    private Element getProviderOrganizationElement( Element el,
                                                    Registration reg,
                                                    Configuration cfg)
    {
        Element providerOrganization
            = doc.createElement("providerOrganization");

        Element assigningAuthorityName
            = doc.createElement("assigningAuthorityName");
        assigningAuthorityName.appendChild(
            doc.createTextNode( rawOID(cfg.globalAssigningAuthority )));

        Element name = doc.createElement("name");
        name.appendChild( doc.createTextNode("XDSDEMO_ADT"));

        providerOrganization.appendChild(assigningAuthorityName);
        providerOrganization.appendChild(name);
        return providerOrganization;
    }

    /**
     * assigningAuthority is set in two places: patient/id/root and
     * providerOrganization/assigningAuthorityName.
     */
    public void setAssigningAuthority( String s) {
        Node idNode = doc.getElementsByTagName( "id").item(0);
        NodeList list = idNode.getChildNodes();
        for( int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if( node.getNodeName().equals("root")) {
                node.getFirstChild().setNodeValue( rawOID(s));
                break;
            }
        }
        Node poNode = doc.getElementsByTagName( "providerOrganization").item(0);
        list = poNode.getChildNodes();
        for( int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if( node.getNodeName().equals("assigningAuthorityName")) {
                node.getFirstChild().setNodeValue( rawOID(s));
                break;
            }
        }
    }

    public void setPatientId( String s) {
        Node idNode = doc.getElementsByTagName( "id").item(0);
        NodeList list = idNode.getChildNodes();
        for( int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if( node.getNodeName().equals("extension")) {
                node.getFirstChild().setNodeValue(s);
                break;
            }
        }
    }

    public Source getSource() {
        return new DOMSource( doc);
    }

    public void dump() throws IOException {
        DOMSerializer ds = new DOMSerializer();
        ds.setIndent(2);
        ds.serialize( doc, System.out);
    }

    public String toString() {
        DOMSerializer ds = new DOMSerializer();
        ds.setIndent(2);
        StringWriter sw = new StringWriter();
        try {
            ds.serialize( doc, sw);
            return sw.toString();
        }
        catch( Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * Strip all but ".0-9" from the given string.
     */
    public String rawOID( String s) {
        StringBuffer sb = new StringBuffer();
        if( s != null) {
            for( int i = 0; i < s.length(); i++) {
                String ss = s.substring(i,i+1);
                if( ss.matches("[.0-9]")) {
                     sb.append(ss);
                }
            }
        }
        return sb.toString();
    }

    public static void main( String[] args) {
        try {
            RegistrationXml reg = new RegistrationXml( null, null, null);
            reg.dump();
        }
        catch( Exception e) {
            e.printStackTrace();
        }
    }

}

