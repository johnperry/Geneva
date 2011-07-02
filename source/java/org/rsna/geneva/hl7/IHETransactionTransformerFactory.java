package org.rsna.geneva.hl7;

import java.io.File;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Transformers are not thread safe so return new instances with each call.
 */
public class IHETransactionTransformerFactory {
    private static File iti44TemplateFile;

    public IHETransactionTransformerFactory(
                File iti44TemplateFile)
    {
        if( ! iti44TemplateFile.exists()) {
            String msg = "ITI44 XSLT template file does not exist: "
                + iti44TemplateFile;
            throw new IllegalArgumentException(msg);
        }
        this.iti44TemplateFile = iti44TemplateFile;
    }

    public static Transformer getITI44Transformer()
        throws TransformerConfigurationException
    {
        return getTransformer( iti44TemplateFile);
    }

    private static Transformer getTransformer( File xsltTemplateFile)
        throws TransformerConfigurationException
    {
        Source xsltSource = new StreamSource( xsltTemplateFile);
        return TransformerFactory.newInstance()
                                 .newTransformer( xsltSource);
    }

}
