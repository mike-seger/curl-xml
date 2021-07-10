<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs">
    <xsl:template name="main">
        <out>
            <xsl:for-each select="tokenize(unparsed-text('file:///Users/michael/git/curl-xml/src/test/resources/test2.csv'), '\n')">
                <line>
                    <xsl:value-of select="tokenize(., ',')" separator="|"/>
                </line>
            </xsl:for-each>
        </out>
    </xsl:template>

    <xsl:output cdata-section-elements="line"/>
</xsl:stylesheet>