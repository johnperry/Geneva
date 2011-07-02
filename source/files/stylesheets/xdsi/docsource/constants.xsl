<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:xdsi=         "http://etsmtl.ca/ihe/xdsitest/xdsi"
    extension-element-prefixes= "xdsi">

    <!-- Get a patientID with its AssigningAuthority from -->
    <!-- http://hcxw2k1.nist.gov:8080/xdsServices2/xds/patientId/assignPatientId.html -->
    <!-- Separate the returned patient around the carrots signs, and replace '&' by "&amp;" (xml parsing) -->

    <xsl:variable   name=   "xdsi:constant_assigningPatientID"          select= "'e13183abf7974fb'"   />
    <xsl:variable   name=   "xdsi:constant_assigningAuthorithyAD"       select= "'&amp;1.3.6.1.4.1.21367.2005.3.7&amp;ISO'"   />
    <xsl:variable   name=   "xdsi:constant_assigningAuthorithyLocal"    select= "'&amp;1.3.6.1.4.1.21367.2005.3.7&amp;ISO'"   />

</xsl:stylesheet>
