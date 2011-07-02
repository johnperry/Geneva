<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform">

    <xsl:variable   name=   "indentPerLevel"    select= "'    '"/>

    <xsl:template   match=  "/">
        <xsl:apply-templates    select= "*">
            <xsl:with-param name=   "whitespacePrefix"  select= "'&#x0A;'"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template   match=  "*[*]">
        <xsl:param  name=   "whitespacePrefix"/>
 
        <xsl:value-of   select= "$whitespacePrefix"/>
        <xsl:copy>
            <xsl:apply-templates    select= "* | comment() | @*">
                <xsl:with-param name=   "whitespacePrefix"  select= "concat($whitespacePrefix, $indentPerLevel)"/>
            </xsl:apply-templates>
            <xsl:value-of   select= "$whitespacePrefix"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template   match=  "* | comment()">
        <xsl:param  name=   "whitespacePrefix"/>
 
        <xsl:value-of   select= "$whitespacePrefix"/>
        <xsl:copy>
            <xsl:apply-templates    select= "text() | @*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template   match=  "text() | @*">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>
