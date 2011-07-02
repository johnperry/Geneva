<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl=         "http://exslt.org/common"
    xmlns:func=         "http://exslt.org/functions"
    xmlns:util=         "http://etsmtl.ca/ihe/xdsitest/util"
    xmlns:xds=          "http://etsmtl.ca/ihe/xdsitest/xds"
    extension-element-prefixes= "exsl func util xds">

    <xsl:import href=   "../generic/util.xsl"   />


    <xsl:variable   name=   "xds:dictionary"    select= "document('dictionary.xml')/*"  />

    <func:function  name=   "xds:object_isReference">
        <xsl:param      name=   "object"    select= "." />
        <func:result    select= "boolean($object[1][@reference = '1'])" />
    </func:function>

    <func:function  name=   "xds:setObject_filterByClass">
        <xsl:param      name=   "setObject"     />
        <xsl:param      name=   "setClassName"  />

        <func:result    select= "exsl:node-set($setObject[name() = $setClassName])" />
    </func:function>

    <func:function  name=   "xds:setValue_filterByAttribute">
        <xsl:param      name=   "setValue"          />
        <xsl:param      name=   "setAttributeName"  />

        <func:result    select= "exsl:node-set($setValue[name() = $setAttributeName])"  />
    </func:function>

    <func:function  name=   "xds:setObject_getUsedDictionaryItemsForcingObjectRef">
        <xsl:param      name=   "setObject" />

        <xsl:variable   name=   "setObject_notRef"      select= "$setObject[not(xds:object_isReference())]" />
        <xsl:variable   name=   "setDescriptionClass"
                        select= "$xds:dictionary/class[xds:setObject_filterByClass($setObject_notRef, @name)]"  />
        <xsl:variable   name=   "setDescriptionAttr"
                        select= "$xds:dictionary/attribute
                                    [xds:setValue_filterByAttribute(
                                        xds:setObject_filterByClass($setObject_notRef, @parent)/*,
                                        @name)]"    />
        <func:result    select= "($setDescriptionClass | $setDescriptionAttr)
                                    [@repr = $xds:dictionary/table[@name = 'ebXMLRepresentation']/*
                                                [@discriminatorValueNeedsObjectRef = '1']/@repr]"   />
    </func:function>

    <xsl:template   name=   "xds:createXDSRel_Contains_rtf">
        <xsl:param      name=   "container"                             />
        <xsl:param      name=   "member"                                />
        <xsl:param      name=   "SubmissionSetStatus"   select= "''"    />

        <XDSRel_Contains>
            <container ><xsl:value-of   select= "$container/@id"    /></container>
            <member    ><xsl:value-of   select= "$member/@id"       /></member>
            <xsl:if test=   "$SubmissionSetStatus">
                <SubmissionSetStatus   ><xsl:value-of   select= "$SubmissionSetStatus"  /></SubmissionSetStatus>
            </xsl:if>
        </XDSRel_Contains>
    </xsl:template>

    <xsl:template   name=   "xds:createXDSRel_Doc_rtf">
        <xsl:param      name=   "type"      />
        <xsl:param      name=   "newDoc"    />
        <xsl:param      name=   "oldDoc"    />

        <xsl:element    name=   "{concat('XDSRel_Doc', $type)}">
            <newDoc><xsl:value-of   select= "$newDoc/@id"   /></newDoc>
            <oldDoc><xsl:value-of   select= "$oldDoc/@id"   /></oldDoc>
        </xsl:element>
    </xsl:template>

    <func:function  name=   "xds:createXDSRel_Contains">
        <xsl:param      name=   "container"                             />
        <xsl:param      name=   "member"                                />
        <xsl:param      name=   "SubmissionSetStatus"   select= "''"    />

        <xsl:variable   name=   "__rel">
            <xsl:call-template  name=   "xds:createXDSRel_Contains_rtf">
                <xsl:with-param name=   "container"             select= "$container"            />
                <xsl:with-param name=   "member"                select= "$member"               />
                <xsl:with-param name=   "SubmissionSetStatus"   select= "$SubmissionSetStatus"  />
            </xsl:call-template>
        </xsl:variable>
        <func:result    select= "exsl:node-set($__rel)/*" />
    </func:function>

    <func:function  name=   "xds:createXDSRel_Doc">
        <xsl:param      name=   "type"      />
        <xsl:param      name=   "newDoc"    />
        <xsl:param      name=   "oldDoc"    />

        <xsl:variable   name=   "__rel">
            <xsl:call-template  name=   "xds:createXDSRel_Doc_rtf">
                <xsl:with-param name=   "type"      select= "$type"     />
                <xsl:with-param name=   "newDoc"    select= "$newDoc"   />
                <xsl:with-param name=   "oldDoc"    select= "$oldDoc"   />
            </xsl:call-template>
        </xsl:variable>
        <func:result    select= "exsl:node-set($__rel)/*" />
    </func:function>

    <func:function  name=   "xds:object_addAttributes">
        <xsl:param      name=   "object1"   />
        <xsl:param      name=   "object2"   />

        <xsl:variable   name=   "class" select= "name($object1)"    />
        <xsl:if test=   "$class !=  name($object2)">
            <xsl:message    terminate=  "yes">Merge for objects of different class.</xsl:message>
        </xsl:if>

        <xsl:variable   name=   "__result">
            <xsl:for-each   select= "$object1">
                <xsl:copy>
                    <xsl:copy-of    select= "util:alternative(@id, @id, $object2/@id)"  />
                    <xsl:for-each   select= "$xds:dictionary/attribute[@parent = $class]">
                        <xsl:variable   name=   "descriptionAttr"   select= "." />
                        <xsl:variable   name=   "listValue1"    select= "$object1/*[name() = $descriptionAttr/@name]"   />
                        <xsl:variable   name=   "listValue2"    select= "$object2/*[name() = $descriptionAttr/@name]"   />

                        <xsl:copy-of    select= "util:alternative($listValue1, $listValue1, $listValue2)"   />
                    </xsl:for-each>
                </xsl:copy>
            </xsl:for-each>
        </xsl:variable>
        <func:result    select= "exsl:node-set($__result)/*" />
    </func:function>

</xsl:stylesheet>
