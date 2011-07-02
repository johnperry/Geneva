<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:str=          "http://exslt.org/strings"
    xmlns:util=         "http://etsmtl.ca/ihe/xdsitest/util"
    extension-element-prefixes= "str util"

    xmlns:dcmtk=        "http://dicom.offis.de/dcmtk"
    xmlns=              "http://dicom.offis.de/dcmsr"
    exclude-result-prefixes=    "dcmtk">

    <xsl:import href=   "../../generic/util.xsl"    />


    <xsl:template   match="/">

        <xsl:variable   name=   "descriptionReport"             select= "*" />
        <xsl:variable   name=   "descriptionReferencedInstance" select= "$descriptionReport/listReferencedSOPInstance/*[1]" />
        <xsl:variable   name=   "instance"                      select= "document($descriptionReferencedInstance/@href)/*"  />
        <xsl:variable   name=   "listDcmTagMetaHeader"          select= "$instance/dcmtk:meta-header/dcmtk:element" />
        <xsl:variable   name=   "listDcmTagDataSet"             select= "$instance/dcmtk:data-set/dcmtk:element"    />

        <xsl:variable   name=   "studyUid"  select= "util:alternative(string($descriptionReport/study/@uid),
                                                                      $descriptionReport/study/@uid,
                                                                      $listDcmTagDataSet[@name = 'StudyInstanceUID'])"    />

<xsl:if test="true()" xml:space="preserve">
<report type="Key Object Selection Document">
    <sopclass uid="1.2.840.10008.5.1.4.1.1.88.59"/>
    <charset><xsl:value-of   select= "$listDcmTagDataSet[@name = 'SpecificCharacterSet']"   /></charset>

    <patient>
        <id><xsl:value-of   select= "$listDcmTagDataSet[@name = 'PatientID']"   /></id>
        <name><!--
             --><xsl:variable   name=   "listTokenName" select= "str:tokenize($listDcmTagDataSet[@name = 'PatientsName'], '^')" />
            <prefix><xsl:value-of   select= "$listTokenName[4]" /></prefix>
            <first><xsl:value-of    select= "$listTokenName[2]" /></first>
            <middle><xsl:value-of   select= "$listTokenName[3]" /></middle>
            <last><xsl:value-of     select= "$listTokenName[1]" /></last>
            <suffix><xsl:value-of   select= "$listTokenName[5]" /></suffix>
        </name><!--

     <<< --><xsl:variable   name=   "birthDate" select= "string($listDcmTagDataSet[@name = 'PatientsBirthDate'])"
          /><xsl:if                             test=   "$birthDate">
        <birthday>                              
            <date><xsl:value-of select= "$birthDate"    /></date>
        </birthday><!--
     >>> --></xsl:if><!--

     <<< --><xsl:variable   name=   "sex"       select= "string($listDcmTagDataSet[@name = 'PatientsSex'])"
          /><xsl:if                             test=   "$sex">
        <sex><xsl:value-of      select= "$sex"      /></sex><!--
     >>> --></xsl:if>
    </patient>

    <study      uid=    "{$studyUid}"   />
    <series     uid=    "{$descriptionReport/series/@uid}"  />
    <instance   uid=    "{$descriptionReport/@uid}">
        <creation>
            <date><xsl:value-of select= "$descriptionReport/creationDate"   /></date>
            <time><xsl:value-of select= "$descriptionReport/creationTime"   /></time>
        </creation>
    </instance>

    <evidence type="Current Requested Procedure">
        <study  uid=    "{$listDcmTagDataSet[@name = 'StudyInstanceUID']}">
            <series uid=    "{$listDcmTagDataSet[@name = 'SeriesInstanceUID']}">
                <aetitle><xsl:value-of  select= "$descriptionReferencedInstance/@retrieveAeTitle"   /></aetitle>
                <value>
                    <sopclass   uid=    "{$listDcmTagDataSet[@name = 'SOPClassUID']}"   />
                    <instance   uid=    "{$listDcmTagDataSet[@name = 'SOPInstanceUID']}"    />
                </value>
            </series>
        </study>
    </evidence>

    <document>
        <content>
            <date><xsl:value-of select= "$descriptionReport/creationDate"   /></date>
            <time><xsl:value-of select= "$descriptionReport/creationTime"   /></time>
            <container flag="SEPARATE">
                <concept>
                    <value>113000</value>
                    <scheme>
                        <designator>DCM</designator>
                    </scheme>
                    <meaning>Of Interest</meaning>
                </concept>
                <image>
                    <relationship>CONTAINS</relationship>
                    <value>
                        <sopclass   uid=    "{$listDcmTagDataSet[@name = 'SOPClassUID']}"   />
                        <instance   uid=    "{$listDcmTagDataSet[@name = 'SOPInstanceUID']}"    />
                    </value>
                </image>
            </container>
        </content>
    </document>
</report>
</xsl:if>

    </xsl:template>

</xsl:stylesheet>
