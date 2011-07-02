<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xsi:schemaLocation="/config/xml/schema/smsr.xsd"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xdsdemo="http://mirc.rsna.org/xdsdemo"
	xmlns="urn:hl7-org:v3">
<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

<xsl:param name="path"/>
<xsl:param name="patient-name"/>
<xsl:param name="full-name"/>
<xsl:param name="given-name"/>
<xsl:param name="family-name"/>
<xsl:param name="patient-id"/>
<xsl:param name="assigning-authority"/>
<xsl:param name="institution-name"/>
<xsl:param name="document-id"/>
<xsl:param name="title"/>
<xsl:param name="date"/>
<xsl:param name="time"/>
<xsl:param name="street"/>
<xsl:param name="city"/>
<xsl:param name="state"/>
<xsl:param name="zip"/>
<xsl:param name="country"/>
<xsl:param name="sex"/>
<xsl:param name="birth-date"/>
<xsl:param name="uuid"/>
<xsl:param name="uid1"/>
<xsl:param name="uid2"/>
<xsl:param name="uid3"/>
<xsl:param name="uid4"/>
<xsl:param name="pdf"/>

<xsl:template match="*">
	<xsl:copy>
		<xsl:apply-templates select="*|@*|text()"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="@*|text()">
	<xsl:copy/>
</xsl:template>

<xsl:template match="/">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="xdsdemo:patientRole">
	<patientRole>
		<xsl:copy-of select="@classCode"/>
		<id root="{@root}" extension="{$patient-id}"/>
		<addr>
			<streetAddressLine><xsl:value-of select="$street"/></streetAddressLine>
			<city><xsl:value-of select="$city"/></city>
			<state><xsl:value-of select="$state"/></state>
			<postalCode><xsl:value-of select="$zip"/></postalCode>
			<country><xsl:value-of select="$country"/></country>
		</addr>
		<patient>
			<name>
				<given><xsl:value-of select="$given-name"/></given>
				<family><xsl:value-of select="$family-name"/></family>
			</name>
			<administrativeGenderCode
				codeSystem="2.16.840.1.113883.5.1"
				code="{$sex}"/>
			<birthTime value="{$birth-date}"/>
		</patient>
	</patientRole>
</xsl:template>

<xsl:template match="xdsdemo:time-element">
	<xsl:element name="{@name}">
		<xsl:attribute name="value">
			<xsl:value-of select="$date"/><xsl:value-of select="$time"/>
		</xsl:attribute>
	</xsl:element>
</xsl:template>

<xsl:template match="xdsdemo:pdf">
	<text mediaType="application/pdf" representation="B64">
		<xsl:value-of select="$pdf"/>
	</text>
</xsl:template>

<xsl:template match="xdsdemo:uuid">
	<xsl:text>urn:uuid:</xsl:text>
	<xsl:value-of select="$uuid"/>
</xsl:template>

<xsl:template match="xdsdemo:patient-name">
	<xsl:value-of select="$patient-name"/>
</xsl:template>

<xsl:template match="xdsdemo:full-name">
	<xsl:value-of select="$full-name"/>
</xsl:template>

<xsl:template match="xdsdemo:given-name">
	<xsl:value-of select="$given-name"/>
</xsl:template>

<xsl:template match="xdsdemo:family-name">
	<xsl:value-of select="$family-name"/>
</xsl:template>

<xsl:template match="xdsdemo:patient-id">
	<xsl:value-of select="$patient-id"/>
</xsl:template>

<xsl:template match="xdsdemo:assigning-authority">
	<xsl:value-of select="$assigning-authority"/>
</xsl:template>

<xsl:template match="xdsdemo:institution-name">
	<xsl:value-of select="$institution-name"/>
</xsl:template>

<xsl:template match="xdsdemo:id">
	<id root="{$document-id}"/>
</xsl:template>

<xsl:template match="xdsdemo:document-id">
	<xsl:value-of select="$document-id"/>
</xsl:template>

<xsl:template match="xdsdemo:title">
	<xsl:value-of select="$title"/>
</xsl:template>

<xsl:template match="xdsdemo:date">
	<xsl:value-of select="$date"/>
</xsl:template>

<xsl:template match="xdsdemo:time">
	<xsl:value-of select="$time"/>
</xsl:template>

<xsl:template match="xdsdemo:street">
	<xsl:value-of select="$street"/>
</xsl:template>

<xsl:template match="xdsdemo:city">
	<xsl:value-of select="$city"/>
</xsl:template>

<xsl:template match="xdsdemo:state">
	<xsl:value-of select="$state"/>
</xsl:template>

<xsl:template match="xdsdemo:zip">
	<xsl:value-of select="$zip"/>
</xsl:template>

<xsl:template match="xdsdemo:country">
	<xsl:value-of select="$country"/>
</xsl:template>

<xsl:template match="xdsdemo:sex">
	<xsl:value-of select="$sex"/>
</xsl:template>

<xsl:template match="xdsdemo:birth-date">
	<xsl:value-of select="$birth-date"/>
</xsl:template>

</xsl:stylesheet>
