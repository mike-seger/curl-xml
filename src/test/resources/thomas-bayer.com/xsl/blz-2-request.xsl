<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:blz="http://thomas-bayer.com/blz/">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/" name="blz-2-request">
        <soapenv:Envelope>
            <soapenv:Header/>
            <soapenv:Body>
                <xsl:for-each select="data/row">
                    <blz:getBank>
                        <blz:blz><xsl:value-of select="blz"/></blz:blz>
                    </blz:getBank>
                </xsl:for-each>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>