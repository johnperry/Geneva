<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:str=          "http://exslt.org/strings"
    xmlns:xds=          "http://etsmtl.ca/ihe/xdsitest/xds"
    xmlns:util=         "http://etsmtl.ca/ihe/xdsitest/util"
    xmlns:private=      "http://etsmtl.ca/ihe/xdsitest/xds/internal_to_ebxml_rim.xsl"
    extension-element-prefixes= "str xds util private"

    xmlns:ebxml_rim=    "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1"
    exclude-result-prefixes=    "ebxml_rim">

    <xsl:import href=   "../generic/util.xsl"   />
    <xsl:import href=   "./object_internal.xsl" />


    <xsl:template   name=   "xds:object_createEbXMLRepresentation_rtf">
        <xsl:param      name=   "object"    select= "." />

        <xsl:choose>
            <xsl:when   test=   "xds:object_isReference($object)">
                <xsl:call-template  name=   "xds:listId_createEbXMLObjectRef">
                    <xsl:with-param name=   "listId"    select= "$object/@id"   />
                </xsl:call-template>
                <xsl:for-each   select= "str:tokenize('extcl')">
                    <xsl:call-template  name=   "private:processAttributeGroup">
                        <xsl:with-param name=   "attrGroup" select= "."         />
                        <xsl:with-param name=   "object"    select= "$object"   />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable   name=   "descriptionClass"  select= "$xds:dictionary/class[@name = name($object)]"  />
                <xsl:element    name=   "{concat('ebxml_rim:', $descriptionClass/@repr)}">
                    <xsl:if test=   "$object/@id">
                        <xsl:attribute  name=   "id">
                            <xsl:value-of   select= "$object/@id"   />
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:for-each   select= "str:tokenize('attr elem slot extcl extid')">
                        <xsl:call-template  name=   "private:processAttributeGroup">
                            <xsl:with-param name=   "attrGroup" select= "."         />
                            <xsl:with-param name=   "object"    select= "$object"   />
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template   name=   "xds:listId_createEbXMLObjectRef">
        <xsl:param      name=   "listId"    />

        <xsl:for-each   select= "$listId">
            <ebxml_rim:ObjectRef    id= "{.}"   />
        </xsl:for-each>
    </xsl:template>

    <xsl:template   name=   "private:processAttributeGroup">
        <xsl:param      name=   "attrGroup" />
        <xsl:param      name=   "object"    />

        <xsl:for-each   select= "$xds:dictionary/attribute[@parent = name($object)][@repr = $attrGroup]">
            <xsl:variable   name=   "descriptionAttr"   select= "." />
            <xsl:variable   name=   "listValue"         select= "$object/*[name() = $descriptionAttr/@name]"    />

            <xsl:variable   name=   "maxCountValue"
                            select= "util:alternative($descriptionAttr/@max &lt; 0, count($listValue), $descriptionAttr/@max)"  />
            <xsl:variable   name=   "listValue_use"     select= "$listValue[position() &lt;= $maxCountValue]"   />

            <xsl:if test=   "$listValue_use">
                <xsl:call-template  name=   "private:createContainerWithListAttributeValue">
                    <xsl:with-param name=   "descriptionAttr"   select= "$descriptionAttr"  />
                    <xsl:with-param name=   "listValue"         select= "$listValue_use"    />
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>
        <xsl:if test=   "not(xds:object_isReference($object))">
            <xsl:call-template  name=   "private:createClassDiscriminator">
                <xsl:with-param name=   "attrGroup" select= "$attrGroup"    />
                <xsl:with-param name=   "object"    select= "$object"       />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template   name=   "private:createContainerWithListAttributeValue">
        <xsl:param      name=   "descriptionAttr"   />
        <xsl:param      name=   "listValue"         />

        <xsl:choose>
            <xsl:when   test=   "$descriptionAttr/@repr = 'slot'">
                <ebxml_rim:Slot name=   "{$descriptionAttr/@name}">
                    <ebxml_rim:ValueList>
                        <xsl:call-template  name=   "private:createListAttributeValue">
                            <xsl:with-param name=   "descriptionAttr"   select= "$descriptionAttr"  />
                            <xsl:with-param name=   "listValue"         select= "$listValue"        />
                        </xsl:call-template>
                    </ebxml_rim:ValueList>
                </ebxml_rim:Slot>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template  name=   "private:createListAttributeValue">
                    <xsl:with-param name=   "descriptionAttr"   select= "$descriptionAttr"  />
                    <xsl:with-param name=   "listValue"         select= "$listValue"        />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template   name=   "private:createListAttributeValue">
        <xsl:param      name=   "descriptionAttr"   />
        <xsl:param      name=   "listValue"         />

        <xsl:for-each   select= "$listValue">
            <xsl:call-template  name=   "private:createAttributeValue">
                <xsl:with-param name=   "descriptionAttr"   select= "$descriptionAttr"  />
                <xsl:with-param name=   "value"             select= "."                 />
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template   name=   "private:createAttributeValue">
        <xsl:param      name=   "descriptionAttr"   />
        <xsl:param      name=   "value"             />

        <xsl:choose>
            <xsl:when   test=   "$descriptionAttr/@repr = 'attr'">
                <xsl:attribute  name=   "{$descriptionAttr/@discr}">
                    <xsl:value-of   select= "$value"    />
                </xsl:attribute>
            </xsl:when>
            <xsl:when   test=   "$descriptionAttr/@repr = 'elem'">
                <xsl:element    name=   "{concat('ebxml_rim:', $descriptionAttr/@discr)}">
                    <ebxml_rim:LocalizedString  value=  "{$value}"  />
                </xsl:element>
            </xsl:when>
            <xsl:when   test=   "$descriptionAttr/@repr = 'slot'">
                <ebxml_rim:Value>
                    <xsl:value-of   select= "$value"    />
                </ebxml_rim:Value>
            </xsl:when>
            <xsl:when   test=   "$descriptionAttr/@repr = 'extcl'">
                <ebxml_rim:Classification   classifiedObject=   "{$value/../@id}"   classificationScheme=   "{$descriptionAttr/@discr}" nodeRepresentation= "{$value}">
                    <ebxml_rim:Name>
                        <ebxml_rim:LocalizedString  value=  "{$value/@displayName}" />
                    </ebxml_rim:Name>
                    <ebxml_rim:Slot name=   "codingScheme">
                        <ebxml_rim:ValueList>
                            <ebxml_rim:Value>
                                <xsl:value-of   select= "$value/@codingScheme"  />
                            </ebxml_rim:Value>
                        </ebxml_rim:ValueList>
                    </ebxml_rim:Slot>
                </ebxml_rim:Classification>
            </xsl:when>
            <xsl:when   test=   "$descriptionAttr/@repr = 'extid'">
                <ebxml_rim:ExternalIdentifier   identificationScheme=   "{$descriptionAttr/@discr}" value=  "{$value}">
                    <ebxml_rim:Name>
                        <ebxml_rim:LocalizedString  value=  "{concat(concat($descriptionAttr/@parent, '.'), $descriptionAttr/@name)}"   />
                    </ebxml_rim:Name>
                </ebxml_rim:ExternalIdentifier>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message    terminate=  "yes">NOT_IMPLEMENTED</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template   name=   "private:createClassDiscriminator">
        <xsl:param      name=   "attrGroup" />
        <xsl:param      name=   "object"    />

        <xsl:variable   name=   "descriptionClass" select= "$xds:dictionary/class[@name = name($object)]"   />
        <xsl:choose>
            <xsl:when   test=   "($descriptionClass/@repr = 'ExtrinsicObject') and ($attrGroup = 'attr')">
                <xsl:attribute  name=   "objectType">
                    <xsl:value-of   select= "$descriptionClass/@discr"  />
                </xsl:attribute>
            </xsl:when>
            <xsl:when   test=   "($descriptionClass/@repr = 'RegistryPackage') and ($attrGroup = 'extcl')">
                <ebxml_rim:Classification   classifiedObject=   "{$object/@id}"   classificationNode= "{$descriptionClass/@discr}"  />
            </xsl:when>
            <xsl:when   test=   "($descriptionClass/@repr = 'Association') and ($attrGroup = 'attr')">
                <xsl:attribute  name=   "associationType">
                    <xsl:value-of   select= "$descriptionClass/@discr"  />
                </xsl:attribute>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
