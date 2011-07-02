<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" />

<xsl:template match="/">
   <PRPA_IN201301UV02 ITSVersion="XML_1.0" xmlns="urn:hl7-org:v3">
      <id root="1.2.3.4.5" />
      <creationTime value="{registration/creationTime}"/>
      <interactionId displayable="true"
                     extension="PRPA_IN201301UV02"
                     root="2.16.840.1.18"/>
      <processingCode code="P"/>
      <processingModeCode code="R"/>
      <acceptAckCode code="AL"/>
      <receiver typeCode="RCV">
         <device classCode="DEV" determinerCode="INSTANCE">
            <id root="{registration/receiver/assigningAuthorityName}"/>
            <name>
               <xsl:value-of select="registration/receiver/name" />
            </name>
          </device>
      </receiver>
      <sender typeCode="SND">
         <device classCode="DEV" determinerCode="INSTANCE">
            <id root="{registration/sender/assigningAuthorityName}"/>
            <name>
               <xsl:value-of select="registration/sender/name" />
            </name>
         </device>
      </sender>
      <controlActProcess classCode="CACT" moodCode="EVN">
         <code code="PRPA_IN201301UV"> </code>
         <subject typeCode="SUBJ">
            <registrationEvent classCode="REG" moodCode="EVN">
               <id extension="10501N17" root="1.2.3.4.5"> </id>
               <statusCode code="active"> </statusCode>
               <subject1 typeCode="SBJ">
                  <patient classCode="PAT">
                     <id extension="{registration/patient/id/extension}"
                         root="{registration/patient/id/root}">
                     </id>
                     <statusCode code="active">
                     </statusCode>
                     <patientPerson classCode="PSN" determinerCode="INSTANCE">
                        <name>
                           <family>
                              <xsl:value-of select="registration/patient/name/family" />
                           </family>
                           <given>
                              <xsl:value-of select="registration/patient/name/given" />
                           </given>
                        </name>
                        <administrativeGenderCode code="{registration/patient/genderCode}">
                        </administrativeGenderCode>
                        <birthTime value="{registration/patient/birthDate}"> </birthTime>
                        <addr>
                           <streetAddressLine>
                              <xsl:value-of select="registration/patient/address/streetAddressLine" />
                           </streetAddressLine>
                           <city>
                              <xsl:value-of select="registration/patient/address/city" />
                           </city>
                           <state>
                              <xsl:value-of select="registration/patient/address/state" />
                           </state>
                           <postalCode>
                              <xsl:value-of select="registration/patient/address/postalCode" />
                           </postalCode>
                        </addr>
                     </patientPerson>
                     <providerOrganization>
                        <id root="{registration/patient/providerOrganization/assigningAuthorityName}">
                        </id>
                        <name>
                           <xsl:value-of select="registration/patient/providerOrganization/name" />
                        </name>
                     </providerOrganization>
                  </patient>
               </subject1>
            </registrationEvent>
         </subject>
      </controlActProcess>
   </PRPA_IN201301UV02>
</xsl:template>

</xsl:stylesheet>
