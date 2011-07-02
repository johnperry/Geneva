<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:xds=          "http://etsmtl.ca/ihe/xdsitest/xds"
    extension-element-prefixes= "xds">

    <xsl:import href=   "../../xds/internal_to_ebxml_rs.xsl" />


    <xsl:template   match="/">
        <xsl:variable   name=   "submissionSet"         select= "document(params/submissionSet/@href)/*"    />
        <xsl:variable   name=   "documentEntry"         select= "document(params/documentEntry/@href)/*"    />

        <xsl:call-template  name=   "xds:submissionRequest_createEbXMLRepresentation_rtf">
            <xsl:with-param name=   "setObject" select= " $submissionSet | $documentEntry"  />
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
