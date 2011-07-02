<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform">


    <xsl:param  name=   "dicomInstanceUri"  select= "'instance.xml'"    />
    <xsl:param  name=   "sreportUri"        select= "'sreport.xml'" />
    <xsl:param  name=   "id"                select= "'DOC01'"   />
    <xsl:param  name=   "creationTime"      select= "'1970010100'"  />
    <xsl:param  name=   "hash"              select= "'ffffffffffffffffffffffffffffffffffffffff'"    />
    <xsl:param  name=   "uniqueId"          select= "'999.999'" />
    <xsl:param  name=   "uri"               select= "'unknown'" />
    <xsl:param  name=   "size"              select= "'0'"   />

    <xsl:template   match=  "/">
        <params>
            <dicomInstance  href=   "{$dicomInstanceUri}"   />
            <sreport        href=   "{$sreportUri}" />
            <XDSDocumentEntry   id= "{$id}">
                <creationTime>
                        <xsl:value-of select= "$creationTime"   />
                </creationTime>
                <uniqueId>
                        <xsl:value-of select= "$uniqueId"   />
                </uniqueId>
            </XDSDocumentEntry>
        </params>
    </xsl:template>

</xsl:stylesheet>
