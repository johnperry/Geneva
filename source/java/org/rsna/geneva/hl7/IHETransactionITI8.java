package org.rsna.geneva.hl7;

import org.w3c.dom.Element;

import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;

public class IHETransactionITI8 implements PatientIdFeed {
    private HL7Field[] fields;
    private Registration reg;
    private Configuration cfg;
    private HL7ITI8 a04;

    private String name;
    private String assigningAuthority;
    private String patientId;
    private String receivingApplication;
    private String receivingFacility;
    private String hl7URL;
    private int timeout;


    public IHETransactionITI8 ( Element el,
                                Registration reg,
                                Configuration cfg)
    {
        this.reg = reg;
        this.cfg = cfg;
        name = "Patient Id Feed, ITI8, hl7v2.3.1";
        a04 = new HL7ITI8();
        fields = cfg.getFields( el);

        receivingApplication = el.getAttribute("receivingApplication").trim();
        if( receivingApplication == null || receivingApplication.equals("")) {
            throw new IllegalArgumentException(
                "Missing receivingApplication attribute.");
        }

        receivingFacility = el.getAttribute("receivingFacility").trim();
        if( receivingFacility == null || receivingFacility.equals("")) {
            throw new IllegalArgumentException(
                "Missing receivingFacility attribute.");
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
        assigningAuthority = s;
    }

    public void setPatientId( String s) {
        patientId = s;
    }

    public void setTimeout( int i) {
        timeout = i;
    }

    /**
     * send this transaction, return integer status.
     */
    public IHETransactionResponse send() {
        String dateTime = cfg.getDateTime();

        a04.setMSH(
                receivingApplication,
                receivingFacility,
                dateTime,
                cfg.getMessageControlID());
        a04.setEVN(
                dateTime,
                dateTime);
        a04.setPID(
                patientId,
                assigningAuthority,
                reg.getName(),
                reg.email,
                reg.birthdate,
                reg.sex,
                reg.street,
                reg.city,
                reg.state,
                reg.zip,
                reg.country);
        a04.setPV1();
        a04.setFields(fields);

        String response = a04.send(hl7URL, timeout);
        return new IHETransactionResponse(
                        HL7Message.toStatus( response), response);
    }

    public String toString() {
        return a04.toString();
    }
}

