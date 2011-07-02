<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:xds=          "http://etsmtl.ca/ihe/xdsitest/xds"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "xds xdsi">

    <xsl:import href=   "../../xds/object_internal.xsl" />
    <xsl:import href=   "./extract_from_dicom_instance_document_entry.xsl"  />
    <xsl:import href=   "./extract_from_sreport_document_entry.xsl" />


    <xsl:template   match="/">
        <xsl:variable   name=   "defaultsDocumentEntry" select= "document('defaults_document_entry.xml')/*" />
        <xsl:variable   name=   "paramsDocumentEntry"   select= "params/XDSDocumentEntry"   />
        <xsl:variable   name=   "dicomInstance"         select= "document(params/dicomInstance/@href)/*"    />
        <xsl:variable   name=   "sreport"               select= "document(params/sreport/@href)/*"  />

        <xsl:variable   name=   "tmp1"
                        select= "xds:object_addAttributes($paramsDocumentEntry,
                                                          $defaultsDocumentEntry)"  />
        <xsl:variable   name=   "tmp2"
                        select= "xds:object_addAttributes(xdsi:extractDocumentEntryInfoFromDicomInstance($dicomInstance),
                                                          $tmp1)"   />
        <xsl:variable   name=   "tmp3"
                        select= "xds:object_addAttributes(xdsi:extractDocumentEntryInfoFromSReport($sreport),
                                                          $tmp2)"   />
        <xsl:copy-of    select= "$tmp3" />
    </xsl:template>

</xsl:stylesheet>
