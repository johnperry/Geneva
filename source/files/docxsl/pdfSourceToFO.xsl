<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">
<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

<xsl:param name="path"/>
<xsl:param name="patient-name"/>
<xsl:param name="full-name"/>
<xsl:param name="given-name"/>
<xsl:param name="family-name"/>
<xsl:param name="patient-id"/>
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
<xsl:param name="pdf"/>

<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="/document">

	<fo:root>

		<xsl:if test="not(format/@layout='landscape')">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="xds"
										margin-top="0.5in"
										margin-bottom="0.5in"
										margin-left="1in"
										margin-right="0.75in"
										page-height="11in"
										page-width="8.5in">
					<fo:region-body margin-top="1.0in"
									margin-bottom="0.6in"/>
					<fo:region-before extent="1.0in"/>
					<fo:region-after extent="0.5in"/>
				</fo:simple-page-master>
			</fo:layout-master-set>
		</xsl:if>
		<xsl:if test="format/@layout='landscape'">
		<fo:layout-master-set>
			<fo:simple-page-master master-name="xds"
									margin-top="0.5in"
									margin-bottom="0.5in"
									margin-left="1.0in"
									margin-right="1in"
									page-height="8.5in"
									page-width="11in">
				<fo:region-body margin-top="0.5in"
								margin-bottom="0.5in"/>
				<fo:region-before extent="0.5in"/>
				<fo:region-after extent="0.5in"/>
			</fo:simple-page-master>
		</fo:layout-master-set>
		</xsl:if>

		<fo:page-sequence master-reference="xds">

			<xsl:apply-templates select="header"/>
			<xsl:apply-templates select="footer"/>
			<xsl:apply-templates select="body"/>

		</fo:page-sequence>

	</fo:root>

</xsl:template>

<xsl:template match="header">
	<fo:static-content flow-name="xsl-region-before">
		<xsl:apply-templates/>
	</fo:static-content>
</xsl:template>

<xsl:template match="body">
	<fo:flow flow-name="xsl-region-body">
		<xsl:apply-templates/>
	</fo:flow>
</xsl:template>

<xsl:template match="footer">
	<fo:static-content flow-name="xsl-region-after">
		<xsl:apply-templates/>
	</fo:static-content>
</xsl:template>

<xsl:template match="h1">
	<fo:block font-size="18pt"
				font-family="sans-serif"
				font-weight="bold"
				line-height="24pt"
				space-after.optimum="12pt"
				text-align="center"
				padding-top="3pt">
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<xsl:template match="h2">
	<fo:block font-size="12pt"
				font-family="sans-serif"
				font-weight="bold"
				line-height="14pt"
				space-after.optimum="3pt"
				text-align="left"
				padding-top="6pt">
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<xsl:template match="br">
	<fo:block space-after.optimum="1pt"
				padding-top="1pt">
	</fo:block>
</xsl:template>

<xsl:template match="p">
	<fo:block font-size="12pt"
				font-family="serif"
				line-height="12pt"
				space-after.optimum="3pt"
				padding-top="3pt">
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<xsl:template match="span">
	<fo:inline>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates/>
	</fo:inline>
</xsl:template>

<xsl:template match="image">
	<fo:external-graphic>
		<xsl:copy-of select="@*"/>
		<xsl:attribute name="src">
			<xsl:value-of select="$path"/>
			<xsl:text>/</xsl:text>
			<xsl:value-of select="@src"/>
		</xsl:attribute>
	</fo:external-graphic>
</xsl:template>

<xsl:template match="hr">
	<fo:block text-align="center">
		<fo:leader leader-length="100%"
					leader-pattern="rule"
					alignment-baseline="middle"
					rule-thickness="0.5pt" color="black">
			<xsl:copy-of select="@*"/>
		</fo:leader>
	</fo:block>
</xsl:template>

<xsl:template match="table">
	<fo:table table-layout="fixed" width="100%" border-collapse="separate">
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="tc"/>
		<fo:table-body>
			<xsl:apply-templates select="tr"/>
		</fo:table-body>
	</fo:table>
</xsl:template>

<xsl:template match="tc">
	<fo:table-column>
		<xsl:copy-of select="@*"/>
	</fo:table-column>
</xsl:template>

<xsl:template match="tr">
	<fo:table-row>
		<xsl:copy-of select="@*"/>
		<xsl:apply-templates select="td"/>
	</fo:table-row>
</xsl:template>

<xsl:template match="td">
	<fo:table-cell>
		<fo:block>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</fo:block>
	</fo:table-cell>
</xsl:template>

<xsl:template match="a">
	<fo:basic-link external-destination="{@href}" show-destination="new">
		<fo:inline text-decoration="underline"><xsl:apply-templates/></fo:inline>
	</fo:basic-link>
</xsl:template>

<xsl:template match="page-number">
	<fo:block text-align-last="center" font-size="10pt">
		<fo:page-number/>
	</fo:block>
</xsl:template>

<xsl:template match="patient-name">
	<xsl:value-of select="$patient-name"/>
</xsl:template>

<xsl:template match="full-name">
	<xsl:value-of select="$full-name"/>
</xsl:template>

<xsl:template match="given-name">
	<xsl:value-of select="$given-name"/>
</xsl:template>

<xsl:template match="family-name">
	<xsl:value-of select="$family-name"/>
</xsl:template>

<xsl:template match="patient-id">
	<xsl:value-of select="$patient-id"/>
</xsl:template>

<xsl:template match="institution-name">
	<xsl:value-of select="$institution-name"/>
</xsl:template>

<xsl:template match="document-id">
	<xsl:value-of select="$document-id"/>
</xsl:template>

<xsl:template match="title">
	<xsl:value-of select="$title"/>
</xsl:template>

<xsl:template match="date">
	<xsl:value-of select="$date"/>
</xsl:template>

<xsl:template match="time">
	<xsl:value-of select="$time"/>
</xsl:template>

<xsl:template match="street">
	<xsl:value-of select="$street"/>
</xsl:template>

<xsl:template match="city">
	<xsl:value-of select="$city"/>
</xsl:template>

<xsl:template match="state">
	<xsl:value-of select="$state"/>
</xsl:template>

<xsl:template match="zip">
	<xsl:value-of select="$zip"/>
</xsl:template>

<xsl:template match="country">
	<xsl:value-of select="$country"/>
</xsl:template>

<xsl:template match="sex">
	<xsl:value-of select="$sex"/>
</xsl:template>

<xsl:template match="birth-date">
	<xsl:value-of select="$birth-date"/>
</xsl:template>

</xsl:stylesheet>