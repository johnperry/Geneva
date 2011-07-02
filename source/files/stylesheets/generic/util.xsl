<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:set=          "http://exslt.org/sets"
    xmlns:func=         "http://exslt.org/functions"
    xmlns:util=         "http://etsmtl.ca/ihe/xdsitest/util"
    extension-element-prefixes="set func util">

    <func:function  name=   "util:returnConditional">
        <xsl:param  name=   "value"             select= "false()"   />
        <xsl:param  name=   "alternativeTrue"   select= "''"        />
        <xsl:param  name=   "alternativeFalse"  select= "''"        />

        <xsl:choose>
            <xsl:when   test=   "$value">
                <func:result    select= "$alternativeTrue"/>
            </xsl:when>
            <xsl:otherwise>
                <func:result    select= "$alternativeFalse"/>
            </xsl:otherwise>
        </xsl:choose>
    </func:function>

    <func:function  name=   "util:alternative">
        <xsl:param  name=   "value"             select= "false()"   />
        <xsl:param  name=   "alternativeTrue"   select= "''"        />
        <xsl:param  name=   "alternativeFalse"  select= "''"        />

        <xsl:choose>
            <xsl:when   test=   "$value">
                <func:result    select= "$alternativeTrue"/>
            </xsl:when>
            <xsl:otherwise>
                <func:result    select= "$alternativeFalse"/>
            </xsl:otherwise>
        </xsl:choose>
    </func:function>

    <func:function   name=   "util:getPosition">
        <xsl:param      name=   "list_target"   select= "/.."   />
        <xsl:param      name=   "list_source"   select= "/.."   />
        <xsl:param      name=   "index"         select= "1"     />

        <xsl:variable   name=   "nodeToLookFor" select= "$list_source[number($index)]"/>
        <xsl:variable   name=   "pos">
            <xsl:for-each   select= "$list_target">
                <xsl:if test=   "set:has-same-node(., $nodeToLookFor)">
                    <xsl:value-of   select= "position()"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <func:result    select= "number(concat('0', $pos))"/>
    </func:function>

</xsl:stylesheet>
