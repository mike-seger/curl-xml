<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs">
    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="csv-encoding" as="xs:string" select="'iso-8859-1'"/>
    <xsl:param name="csv-uri" as="xs:string" select="'file:///Users/michael/git/curl-xml/src/test/resources/test2.csv'"/>

    <xsl:template match="/" name="csv2xml">
        <data>
            <xsl:choose>
                <xsl:when test="unparsed-text-available($csv-uri, $csv-encoding)">
                    <xsl:variable name="csv" select="unparsed-text($csv-uri, $csv-encoding)"/>
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
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="error">
                        <xsl:text>Error reading "</xsl:text>
                        <xsl:value-of select="$csv-uri"/>
                        <xsl:text>" (encoding "</xsl:text>
                        <xsl:value-of select="$csv-encoding"/>
                        <xsl:text>").</xsl:text>
                    </xsl:variable>
                    <xsl:message><xsl:value-of select="$error"/></xsl:message>
                    <xsl:value-of select="$error"/>
                </xsl:otherwise>
            </xsl:choose>
        </data>
    </xsl:template>

</xsl:stylesheet>