<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "xdsi">

    <xsl:import href=   "./constants.xsl" />


    <xsl:param  name=   "documentEntryUri"  select= "'document_entry.xml'"  />
    <xsl:param  name=   "id"                select= "'SBM01'"   />
    <xsl:param  name=   "sourceId"          select= "'0'"           />
    <xsl:param  name=   "submissionTime"    select= "'1970010100'"  />
    <xsl:param  name=   "uniqueId"          select= "'999.999'"     />

    <xsl:template   match=  "/">
        <params>
            <documentEntry  href=   "{$documentEntryUri}"   />
            <XDSSubmissionSet   id= "{$id}">
                
                <submissionTime>
                        <xsl:value-of select= "$submissionTime" />
                </submissionTime>
                <uniqueId>
                        <xsl:value-of select= "$uniqueId"   />
                </uniqueId>
            </XDSSubmissionSet>
        </params>
    </xsl:template>

</xsl:stylesheet>
