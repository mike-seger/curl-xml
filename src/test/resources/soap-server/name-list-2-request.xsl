<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
>
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/" name="name-list-2-request">
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:gs="http://net128.com/soap-server">
            <soapenv:Header/>
            <soapenv:Body>
                <gs:countryNameList>
                    <xsl:for-each select="data/row">
                        <gs:name><xsl:value-of select="name"/></gs:name>
                    </xsl:for-each>
                </gs:countryNameList>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>