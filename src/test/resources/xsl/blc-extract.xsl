<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:blz="http://thomas-bayer.com/blz/">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/" name="to-blz-request">
        <bic>
            <xsl:value-of select="//bic" />
        </bic>
    </xsl:template>
</xsl:stylesheet>