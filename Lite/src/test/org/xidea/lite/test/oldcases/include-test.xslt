<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:template match='/'>
		<div>
			<xsl:apply-templates select='//meta' />
		</div>
	</xsl:template>
	<xsl:template match='meta'>
		<xsl:value-of select='@name' />
	</xsl:template>
</xsl:stylesheet>
