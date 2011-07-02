package org.rsna.geneva.hl7;

import org.w3c.dom.Element;
import org.rsna.geneva.main.Configuration;
import org.rsna.geneva.misc.Registration;

public class IHETransactionFactory {

    public static final String ITI8 = "iti8";
    public static final String ITI44 = "iti44";
    public static final String RAD1 = "rad1";
    public static final String RAD4 = "rad4";

    public static PatientIdFeed getPatientIdFeedInstance(
                                  Element el,
                                  Registration reg,
                                  Configuration cfg)
    {
        String hl7Version = el.getAttribute("hl7Version").trim();
        if( hl7Version.equals("3"))
            return new IHETransactionITI44( el, reg, cfg);
        else
            return new IHETransactionITI8( el, reg, cfg);
    }


    public static IHETransaction getInstance( String type,
                                              Element el,
                                              Registration reg,
                                              Configuration cfg)
    {
        if( ITI8.equals(type)) {
            return new IHETransactionITI8( el, reg, cfg);
        }
        /*
        else if( ITI44.equals(type)) {
            return new IHETransactionITI44( el, reg, cfg);
        }
        else if( RAD1.equals(type)) {
            return new IHETransactionRAD1( el, reg, cfg);
        }
        else if( RAD4.equals(type)) {
            return new IHETransactionRAD4( el, reg, cfg);
        }
        */
        else {
            String msg = "Unrecognized transaction type: " + type;
            throw new IllegalArgumentException(msg);
        }
    }
}
