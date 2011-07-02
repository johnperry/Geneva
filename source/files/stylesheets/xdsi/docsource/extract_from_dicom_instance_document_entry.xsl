<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl=         "http://exslt.org/common"
    xmlns:func=         "http://exslt.org/functions"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes="exsl func xdsi"

    xmlns:dcmtk=        "http://dicom.offis.de/dcmtk"
    exclude-result-prefixes=    "dcmtk">


    <func:function  name=   "xdsi:extractDocumentEntryInfoFromDicomInstance">
        <xsl:param      name=   "instance"              select= "." />

        <xsl:variable   name=   "listDcmTagMetaHeader"  select= "$instance/dcmtk:meta-header/dcmtk:element" />
        <xsl:variable   name=   "listDcmTagDataSet"     select= "$instance/dcmtk:data-set/dcmtk:element"    />
        <xsl:variable   name=   "__result">
            <XDSDocumentEntry>
                <creationTime>
                        <xsl:value-of select= "$listDcmTagDataSet[@name = 'InstanceCreationDate']"  />
                        <xsl:value-of select= "substring($listDcmTagDataSet[@name = 'InstanceCreationTime'], 1, 6)" />
                </creationTime>
    
                <xsl:variable   name=   "modality"  select= "string($listDcmTagDataSet[@name = 'Modality'])"    />
                <xsl:if test=   "$modality">
                    <eventCodeList  displayName=    "Some modality" codingScheme=   "DCM">
                            <xsl:value-of select= "$modality"   />
                    </eventCodeList>
                </xsl:if>
            </XDSDocumentEntry>
        </xsl:variable>
        <func:result    select= "exsl:node-set($__result)/*"    />
    </func:function>

</xsl:stylesheet>
