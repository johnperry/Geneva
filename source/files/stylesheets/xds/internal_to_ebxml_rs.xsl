<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl=         "http://exslt.org/common"
    xmlns:set=          "http://exslt.org/sets"
    xmlns:util=         "http://etsmtl.ca/ihe/xdsitest/util"
    xmlns:xds=          "http://etsmtl.ca/ihe/xdsitest/xds"
    extension-element-prefixes= "exsl set util xds"

    xmlns:ebxml_rs=     "urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1"
    xmlns:ebxml_rim=    "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.1"
    exclude-result-prefixes=    "ebxml_rs ebxml_rim">

    <xsl:import href=   "../generic/util.xsl"   />
    <xsl:import href=   "./object_internal.xsl" />
    <xsl:import href=   "./internal_to_ebxml_rim.xsl" />


    <xsl:template   name=   "xds:submissionRequest_createEbXMLRepresentation_rtf">
        <xsl:param      name=   "setObject"                                     />
        <xsl:param      name=   "setSubmissionSetMember_force"  select= "/.."   />

        <xsl:variable   name=   "_setObject"                    select= "exsl:node-set($setObject)" />
        <xsl:variable   name=   "_setSubmissionSetMember_force" select= "exsl:node-set($setSubmissionSetMember_force)"  />
        <xsl:variable   name=   "setXDSDocumentEntry"
                        select= "xds:setObject_filterByClass($_setObject, 'XDSDocumentEntry')"  />
        <xsl:variable   name=   "setXDSFolder"
                        select= "xds:setObject_filterByClass($_setObject, 'XDSFolder')" />
        <xsl:variable   name=   "setXDSRel_Contains"
                        select= "xds:setObject_filterByClass($_setObject, 'XDSRel_Contains')
                                    [container  = $setXDSFolder/@id         ]
                                    [member     = $setXDSDocumentEntry/@id  ]"  />
        <xsl:variable   name=   "setXDSRel_Doc"
                        select= "xds:setObject_filterByClass($_setObject,
                                    $xds:dictionary/table[@name = 'XDSRel']/*[@isRelDoc = '1']/@class)
                                    [newDoc = $setXDSDocumentEntry/@id]
                                    [oldDoc = $setXDSDocumentEntry/@id]"    />
        <xsl:variable   name=   "setObject_keep"
                        select= " xds:setObject_filterByClass($_setObject, 'XDSSubmissionSet')
                                | $setXDSDocumentEntry
                                | $setXDSFolder
                                | $setXDSRel_Contains
                                | $setXDSRel_Doc"   />
        <xsl:variable   name=   "setObject_request"
                        select= " set:difference(
                                    $setXDSDocumentEntry[xds:object_isReference()],
                                    $_setSubmissionSetMember_force)
                                | $setXDSFolder[xds:object_isReference()]
                                | $setXDSRel_Contains
                                | $setXDSRel_Doc"   />
        <xsl:variable   name=   "setObject_submissionSet"   select= "set:difference($setObject_keep, $setObject_request)"   />

        <ebxml_rs:SubmitObjectsRequest>
            <ebxml_rim:LeafRegistryObjectList>

                <xsl:call-template  name=   "xds:submissionSet_createEbXMLRepresentation_rtf">
                    <xsl:with-param name=   "setObject" select= "$setObject_submissionSet"  />
                </xsl:call-template>

                <xsl:for-each   select= "$xds:dictionary/class">
                    <xsl:variable   name=   "setObject_class"   select= "xds:setObject_filterByClass($setObject_request, @name)"    />
                    <xsl:if test=   "$setObject_class">
                        <xsl:comment> &lt;&lt;&lt;&lt; <xsl:value-of select= "@name"    /> &lt;&lt;&lt;&lt; </xsl:comment>
                        <xsl:for-each   select= "$setObject_class">
                            <xsl:call-template  name=   "xds:object_createEbXMLRepresentation_rtf"/>
                        </xsl:for-each>
                        <xsl:comment> &gt;&gt;&gt;&gt; <xsl:value-of select= "@name"    /> &gt;&gt;&gt;&gt; </xsl:comment>
                    </xsl:if>
                </xsl:for-each>

                <xsl:call-template  name=   "xds:listId_createEbXMLObjectRef">
                    <xsl:with-param name=   "listId"
                                    select= "xds:setObject_getUsedDictionaryItemsForcingObjectRef($setObject_keep)/@discr"  />
                </xsl:call-template>

            </ebxml_rim:LeafRegistryObjectList>
        </ebxml_rs:SubmitObjectsRequest>
    </xsl:template>

    <xsl:template   name=   "xds:submissionSet_createEbXMLRepresentation_rtf">
        <xsl:param      name=   "setObject" />

        <xsl:variable   name=   "submissionSet"
                        select= " xds:setObject_filterByClass($setObject, 'XDSSubmissionSet')"  />
        <xsl:variable   name=   "setMember"
                        select= " xds:setObject_filterByClass($setObject,                                'XDSDocumentEntry')
                                | xds:setObject_filterByClass($setObject[not(xds:object_isReference())], 'XDSFolder')"  />

        <!--************
            Checks: exactly one XDSSubmissionSet; patientId.
        **************** -->
        <xsl:if test=   "count($submissionSet) != 1">
            <xsl:message    terminate=  "yes">submissionSet_createEbXMLRepresentation_rtf: Count XDSSubmissionSet must be 1.</xsl:message>
        </xsl:if>
        <xsl:if test=   "count(set:distinct($setMember[not(xds:object_isReference())]/*[@attr = 'patientId'])) &gt; 1">
            <xsl:message    terminate=  "yes">submissionSet_createEbXMLRepresentation_rtf: PatientID must be equal for all members.</xsl:message>
        </xsl:if>

        <xsl:comment> &lt;&lt;&lt;&lt; <xsl:value-of select= "name($submissionSet)" /> &lt;&lt;&lt;&lt; </xsl:comment>
        <xsl:call-template  name=   "xds:object_createEbXMLRepresentation_rtf">
            <xsl:with-param name=   "object"    select= "$submissionSet" />
        </xsl:call-template>
        <xsl:for-each   select= "$xds:dictionary/class">
            <xsl:for-each   select= "xds:setObject_filterByClass($setMember, @name)">

                <xsl:comment> &lt;&lt;&lt;&lt; <xsl:value-of select= "name()" /> &lt;&lt;&lt;&lt; </xsl:comment>
                <xsl:call-template  name=   "xds:object_createEbXMLRepresentation_rtf"/>
                <xsl:call-template  name=   "xds:object_createEbXMLRepresentation_rtf">
                    <xsl:with-param name=   "object"
                                    select= "xds:createXDSRel_Contains( $submissionSet, .,
                                                                        util:alternative(name() != 'XDSDocumentEntry',
                                                                            '',
                                                                            util:alternative(xds:object_isReference(),
                                                                                'Reference',
                                                                                'Original')))"  />
                </xsl:call-template>
                <xsl:comment> &gt;&gt;&gt;&gt; <xsl:value-of select= "name()" /> &gt;&gt;&gt;&gt; </xsl:comment>

            </xsl:for-each>
        </xsl:for-each>
        <xsl:comment> &gt;&gt;&gt;&gt; <xsl:value-of select= "name($submissionSet)" /> &gt;&gt;&gt;&gt; </xsl:comment>

    </xsl:template>

</xsl:stylesheet>
