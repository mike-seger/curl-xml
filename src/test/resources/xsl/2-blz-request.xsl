<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:blz="http://thomas-bayer.com/blz/">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/" name="to-blz-request">
        <soapenv:Envelope>
            <soapenv:Header/>
            <soapenv:Body>
                <blz:getBank>
                    <blz:blz>41670027</blz:blz>
                </blz:getBank>
                <blz:getBank>
                    <blz:blz>10020200</blz:blz>
                </blz:getBank>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>