<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl=         "http://exslt.org/common"
    xmlns:func=         "http://exslt.org/functions"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "exsl func xdsi">


    <func:function  name=   "xdsi:extractSubmissionSetInfoFromDocumentEntry">
        <xsl:param      name=   "documentEntry" select= "." />

        <xsl:variable   name=   "__result">
            <XDSSubmissionSet>>
                <patientId>
                        <xsl:value-of  select= "$documentEntry/patientId"   />
                </patientId>
		    <sourceId>
                        <xsl:value-of  select= "$documentEntry/sourcePatientId"   />
                </sourceId>

            </XDSSubmissionSet>
        </xsl:variable>
        <func:result    select= "exsl:node-set($__result)/*"    />
    </func:function>

</xsl:stylesheet>
