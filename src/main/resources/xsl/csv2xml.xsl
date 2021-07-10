<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="csv-data" as="xs:string"/>

    <xsl:template match="/" name="csv2xml">
        <data>
            <xsl:variable name="csv" select="$csv-data"/>
            <!--Get Header-->
            <xsl:variable name="header-tokens" as="xs:string*">
                <xsl:analyze-string select="$csv" regex="\r\n?|\n">
                    <xsl:non-matching-substring>
                        <xsl:if test="position()=1">
                            <xsl:copy-of select="tokenize(.,',')"/>
                        </xsl:if>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:variable>
            <xsl:analyze-string select="$csv" regex="\r\n?|\n">
                <xsl:non-matching-substring>
                    <xsl:if test="not(position()=1)">
                        <row>
                            <xsl:for-each select="tokenize(.,',')">
                                <xsl:variable name="pos" select="position()"/>
                                <xsl:element name="{$header-tokens[$pos]}">
                                    <xsl:value-of select="."/>
                                </xsl:element>
                            </xsl:for-each>
                        </row>
                    </xsl:if>
                </xsl:non-matching-substring>
            </xsl:analyze-string>
        </data>
    </xsl:template>

</xsl:stylesheet>