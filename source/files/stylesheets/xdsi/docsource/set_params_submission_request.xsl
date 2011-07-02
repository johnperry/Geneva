<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "xdsi">


    <xsl:param  name=   "submissionSetUri"  select= "'submission_set.xml'"  />
    <xsl:param  name=   "documentEntryUri"  select= "'document_entry.xml'"  />

    <xsl:template   match=  "/">
        <params>
            <submissionSet  href=   "{$submissionSetUri}"   />
            <documentEntry  href=   "{$documentEntryUri}"   />
        </params>
    </xsl:template>

</xsl:stylesheet>
