<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:xds=          "http://etsmtl.ca/ihe/xdsitest/xds"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "xds xdsi">

    <xsl:import href=   "../../xds/object_internal.xsl" />
    <xsl:import href=   "./extract_from_document_entry_submission_set.xsl"  />


    <xsl:template   match="/">
        <xsl:variable   name=   "defaultsSubmissionSet" select= "document('defaults_submission_set.xml')/*" />
        <xsl:variable   name=   "paramsSubmissionSet"   select= "params/XDSSubmissionSet"   />
        <xsl:variable   name=   "documentEntry"         select= "document(params/documentEntry/@href)/*"    />

        <xsl:variable   name=   "tmp1"
                        select= "xds:object_addAttributes($paramsSubmissionSet,
                                                          $defaultsSubmissionSet)"  />
        <xsl:variable   name=   "tmp2"
                        select= "xds:object_addAttributes(xdsi:extractSubmissionSetInfoFromDocumentEntry($documentEntry),
                                                          $tmp1)"   />
        <xsl:copy-of    select= "$tmp2" />
    </xsl:template>

</xsl:stylesheet>
