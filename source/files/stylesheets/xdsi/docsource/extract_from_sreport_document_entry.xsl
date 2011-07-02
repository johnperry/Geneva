<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl=         "http://exslt.org/common"
    xmlns:func=         "http://exslt.org/functions"
    xmlns:util=         "http://etsmtl.ca/ihe/xdsitest/util"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "exsl func util xdsi"

    xmlns:dcmsr=        "http://dicom.offis.de/dcmsr"
    exclude-result-prefixes=    "dcmsr">

    <xsl:import href=   "../../generic/util.xsl"    />


    <func:function  name=   "xdsi:extractDocumentEntryInfoFromSReport">
        <xsl:param      name=   "report"    select= "." />

        <xsl:variable   name=   "__scrPatientId"           select= "string($report/dcmsr:patient/dcmsr:id)"    />
        <xsl:variable   name=   "__patientId"           select= "string($report/dcmsr:PatientGlobalID)"    />
        <xsl:variable   name=   "patientId">
            <xsl:value-of   select= "util:alternative($__patientId, $__patientId, 'UNKNOWN')"   /><xsl:text>^</xsl:text>
            <xsl:text>^</xsl:text>
            <xsl:text>^</xsl:text>
            <xsl:value-of select= "string($report/dcmsr:AssigningAuthorithyAD)"    />
        </xsl:variable>
        <xsl:variable   name=   "srcPatientId">
            <xsl:value-of   select= "util:alternative($__scrPatientId, $__scrPatientId, 'UNKNOWN')"   /><xsl:text>^</xsl:text>
            <xsl:text>^</xsl:text>
            <xsl:text>^</xsl:text>
            <xsl:value-of select= "string($report/dcmsr:AssigningAuthorithyLocal)" />
        </xsl:variable>
        <xsl:variable   name=   "srcPatientName">
            <xsl:value-of   select= "$report/dcmsr:patient/dcmsr:name/dcmsr:last"   /><xsl:text>^</xsl:text>
            <xsl:value-of   select= "$report/dcmsr:patient/dcmsr:name/dcmsr:first"  /><xsl:text>^</xsl:text>
            <xsl:value-of   select= "$report/dcmsr:patient/dcmsr:name/dcmsr:middle" /><xsl:text>^</xsl:text>
            <xsl:value-of   select= "$report/dcmsr:patient/dcmsr:name/dcmsr:suffix" /><xsl:text>^</xsl:text>
            <xsl:value-of   select= "$report/dcmsr:patient/dcmsr:name/dcmsr:prefix" />
        </xsl:variable>
        <xsl:variable   name=   "srcPatientBirthDate"   select= "string($report/dcmsr:patient/dcmsr:birthday/dcmsr:date)"   />
        <xsl:variable   name=   "__srcPatientGender"    select= "string($report/dcmsr:patient/dcmsr:sex)"   />
        <xsl:variable   name=   "srcPatientGender"      select= "util:alternative($__srcPatientGender, $__srcPatientGender, 'U')"   />

        <xsl:variable   name=   "__result">
            <XDSDocumentEntry>
                <formatCode         displayName=    "Key Object Selection Document"
                                    codingScheme=   "1.2.840.10008.2.6.1"
                                                    >1.2.840.10008.5.1.4.1.1.88.59</formatCode>
                <mimeType                           >application/dicom</mimeType>

                <patientId>
                        <xsl:value-of  select= "$patientId" />
                </patientId>
                <sourcePatientId>
				<xsl:value-of  select= "$srcPatientId"  />
		    </sourcePatientId>
		    <sourcePatientInfo>
                        <xsl:text>PID-3|</xsl:text>
                        <xsl:value-of  select= "$srcPatientId"  />
                </sourcePatientInfo>
                <sourcePatientInfo>
                        <xsl:text>PID-5|</xsl:text>
                        <xsl:value-of  select= "$srcPatientName"    />
                </sourcePatientInfo>
                <xsl:if test=   "$srcPatientBirthDate">
                    <sourcePatientInfo>
                            <xsl:text>PID-7|</xsl:text>
                            <xsl:value-of  select= "$srcPatientBirthDate"   />
                    </sourcePatientInfo>
                </xsl:if>
                <sourcePatientInfo>
                        <xsl:text>PID-8|</xsl:text>
                        <xsl:value-of  select= "$srcPatientGender"  />
                </sourcePatientInfo>
            </XDSDocumentEntry>
        </xsl:variable>
        <func:result    select= "exsl:node-set($__result)/*"    />
    </func:function>

</xsl:stylesheet>
