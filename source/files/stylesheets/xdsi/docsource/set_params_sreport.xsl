<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform">


    <xsl:param  name=   "studyUid"                  select= "''"    />
    <xsl:param  name=   "seriesUid"                 select= "'999.999'" />
    <xsl:param  name=   "objectUid"                 select= "'999.999'" />
    <xsl:param  name=   "creationDate"              select= "'19700101'"    />
    <xsl:param  name=   "creationTime"              select= "'00'"  />
    <xsl:param  name=   "referencedInstanceType"    select= "'image'"   />
    <xsl:param  name=   "referencedInstanceUri"     select= "'instance.xml'"    />
    <xsl:param  name=   "retrieveAeTitle"           select= "''"    />

    <xsl:template   match=  "/">
        <instance uid="{$objectUid}">
            <study uid="{$studyUid}"    />
            <series uid="{$seriesUid}"  />
            <creationDate><xsl:value-of select= "$creationDate" /></creationDate>
            <creationTime><xsl:value-of select= "$creationTime" /></creationTime>
            <listReferencedSOPInstance>
                <xsl:element    name=   "{$referencedInstanceType}">
                    <xsl:attribute  name=   "href"><xsl:value-of    select= "$referencedInstanceUri"    /></xsl:attribute>
                    <xsl:attribute  name=   "retrieveAeTitle"><xsl:value-of select= "$retrieveAeTitle"  /></xsl:attribute>
                </xsl:element>
            </listReferencedSOPInstance>
        </instance>
    </xsl:template>

</xsl:stylesheet>
