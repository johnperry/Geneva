<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform">

    <xsl:param  name=   "oldPrefix" select= "'urn:uuid:'"   />
    <xsl:param  name=   "newPrefix" select= "'urn_uuid_'"   />

    <xsl:template   match=  "node() | @*">
        <xsl:copy>
            <xsl:apply-templates    select= "node() | @*"   />
        </xsl:copy>
    </xsl:template>

    <xsl:template   match=  "@*[starts-with(., $oldPrefix)]">
        <xsl:attribute  name=   "{local-name()}"    namespace="{namespace-uri()}">
            <xsl:value-of   select= "concat($newPrefix, substring-after(., $oldPrefix))"    />
        </xsl:attribute>
    </xsl:template>

</xsl:stylesheet>
