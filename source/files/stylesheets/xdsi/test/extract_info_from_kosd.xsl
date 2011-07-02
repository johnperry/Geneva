<xsl:stylesheet
    version=    "1.0"
    xmlns:xsl=          "http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl=         "http://exslt.org/common"
    extension-element-prefixes= "exsl">

    <xsl:template   match=  "/">
        <xsl:variable   name=   "__pass1">
            <xsl:apply-templates    mode=   "doc"   select= "file-format/data-set/*"    />
        </xsl:variable>
        <xsl:variable   name=   "pass1" select= "exsl:node-set($__pass1)"   />

        <xsl:choose>
            <xsl:when   test=   "$pass1//error">
                <error  reason= "invocation SOP Instance Reference Macro">
                    <xsl:copy-of    select= "$pass1"    />
                </error>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable   name=   "__pass2">
                    <xsl:copy-of    select= "$pass1/sopClassUid"    />
                    <xsl:for-each   select= "$pass1/list/list/list">
                        <sopInstance>
                            <xsl:copy-of    select= "ancestor-or-self::*/*[not(self::list)]" />
                        </sopInstance>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable   name=   "pass2" select= "exsl:node-set($__pass2)"   />

                <xsl:choose>
                    <xsl:when   test=   "$pass2/sopInstance[count(*) != 4]">
                        <error  reason= "wrong count of information per SOP instance">
                            <xsl:copy-of    select= "$pass2"    />
                        </error>
                    </xsl:when>
                    <xsl:otherwise>
                        <list>
                            <xsl:copy-of    select= "$pass2"    />
                        </list>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ****************************** document ******************************* -->

    <xsl:template   mode=   "doc"   match=  "sequence[@name = 'CurrentRequestedProcedureEvidenceSequence']
                                                     [position() &gt; 1]">
        <error/>
    </xsl:template>
    <xsl:template   mode=   "doc"   match=  "*">
        <!-- ignore -->
    </xsl:template>
    <xsl:template   mode=   "doc"   match=  "element[@name = 'SOPClassUID'][1]">
        <sopClassUid>   <xsl:value-of   select= "." />  </sopClassUid>
    </xsl:template>
    <xsl:template   mode=   "doc"   match=  "sequence[@name = 'CurrentRequestedProcedureEvidenceSequence'][1][item]">
        <xsl:for-each   select= "item">
            <list>
                <xsl:apply-templates    mode=   "stu"   select= "*" />
            </list>
        </xsl:for-each>
    </xsl:template>

    <!-- ****************************** study ********************************** -->

    <xsl:template   mode=   "stu"   match=  "*">
        
    </xsl:template>
    <xsl:template   mode=   "stu"   match=  "element[@name = 'StudyInstanceUID'][1]">
        <studyUid>  <xsl:value-of   select= "." />  </studyUid>
    </xsl:template>
    <xsl:template   mode=   "stu"   match=  "sequence[@name = 'ReferencedSeriesSequence'][1][item]">
        <xsl:for-each   select= "item">
            <list>
                <xsl:apply-templates    mode=   "ser"   select= "*" />
            </list>
        </xsl:for-each>
    </xsl:template>

    <!-- ****************************** series ********************************* -->

    <xsl:template   mode=   "ser"   match=  "*">
        
    </xsl:template>
    <xsl:template   mode=   "ser"   match=  "element[@name = 'StorageMediaFileSetID'][1]
                                            |element[@name = 'StorageMediaFileSetUID'][1]">
        <!-- ignore -->
    </xsl:template>
    <xsl:template   mode=   "ser"   match=  "element[@name = 'SeriesInstanceUID'][1]">
        <seriesUid> <xsl:value-of   select= "." />  </seriesUid>
    </xsl:template>
    <xsl:template   mode=   "ser"   match=  "element[@name = 'RetrieveAETitle'][1]">
        <retrieveAeTitle>   <xsl:value-of   select= "." />  </retrieveAeTitle>
    </xsl:template>
    <xsl:template   mode=   "ser"   match=  "sequence[@name = 'ReferencedSOPSequence'][1][item]">
        <xsl:for-each   select= "item">
            <list>
                <xsl:apply-templates    mode=   "ins"   select= "*" />
            </list>
        </xsl:for-each>
    </xsl:template>

    <!-- ****************************** instance ******************************* -->

    <xsl:template   mode=   "ins"   match=  "*">
        
    </xsl:template>
    <xsl:template   mode=   "ins"   match=  "element[@name = 'ReferencedSOPClassUID'][1]">
        <!-- ignore -->
    </xsl:template>
    <xsl:template   mode=   "ins"   match=  "element[@name = 'ReferencedSOPInstanceUID'][1]">
        <sopInstanceUid>    <xsl:value-of   select= "." />  </sopInstanceUid>
    </xsl:template>

</xsl:stylesheet>
