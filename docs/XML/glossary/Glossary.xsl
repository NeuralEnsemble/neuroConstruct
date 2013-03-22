<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
    This file is included with the neuroConstruct documentation.
    It is used to format the xml in Glossary.xml, to make an alphabetical
    list of the non-hidden terms in that file. First a Forrest friendly XML file
    (docs/XML/xmlForHtml/docs/Glossary_gen.xml) is created, from which HTML can be generated
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" version="4.0" encoding="iso-8859-1" indent="yes"/>

    <xsl:template match="/">

    <xsl:comment>
        This XML file was automatically generated from the entries in XML/glossary/Glossary.xml
        and the stylesheet XML/glossary/Glossary.xsl. To add entries to the glossary, edit that original file
        AND NOT THIS ONE!!
    </xsl:comment>
    <xsl:text>
</xsl:text>

    <document>
        <header>
            <title>Glossary of the main terms used in neuroConstruct</title>
        </header>
        <body>
            
        <anchor id="top"/>
        <a name="top"/>
        <xsl:for-each select="glossary/term">
            <xsl:sort select="name"/>
            <xsl:if test="not(starts-with(@hidden,'true'))">
                <xsl:element name="a">
                  <xsl:attribute name="href">#<xsl:value-of select="name"/></xsl:attribute> <xsl:value-of select="name"/>
                </xsl:element><xsl:text> - </xsl:text>
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="glossary/term">
            <xsl:sort select="name"/>
            <xsl:if test="not(starts-with(@hidden,'true'))">
                <xsl:apply-templates select = "alias"/>
                <xsl:apply-templates select = "name"/>
                <xsl:apply-templates select = "defined"/>
            </xsl:if>
        </xsl:for-each>


        </body>
    </document>
    </xsl:template>


    <xsl:template match="name">

            <xsl:element name="a">
            <xsl:attribute name="name"><xsl:value-of select="."/></xsl:attribute>

                <p style="font-size: 16;color: white;background-color: #a5b6c6;padding: 5px 5px 5px 5px">
                     <b> <xsl:value-of select="."/>  </b>
                </p>
                </xsl:element>
    </xsl:template>

    <xsl:template match="alias">

            <xsl:element name="a">
                <xsl:attribute name="name"><xsl:value-of select="."/></xsl:attribute>
            </xsl:element>
    </xsl:template>


    <xsl:template match="defined">
        <xsl:element name="span">
            <!--<xsl:attribute name="style">padding: 2px 2px 2px 2px;</xsl:attribute>-->
            <xsl:element name="p">
                <xsl:apply-templates/>
            </xsl:element>
        </xsl:element>
        <p style="font-size: 11"><a href = "#top">index</a></p>
        
    </xsl:template>


    <xsl:template match="a">
        <xsl:element name="a">
            <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>

            <xsl:if test="starts-with(@href,'http')"> <!-- i.e. probably an external website...-->
                <xsl:attribute name="target">_blank</xsl:attribute>
            </xsl:if>

            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="intref">
        <xsl:element name="a">
            <xsl:attribute name="href">#<xsl:value-of select="."/></xsl:attribute>
           <!-- <xsl:attribute name="target">nCtarget</xsl:attribute>-->
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="i">
        <i>
            <xsl:value-of select="."/>
        </i>
    </xsl:template>

    <xsl:template match="b">
        <strong>
            <xsl:value-of select="."/>
        </strong>
    </xsl:template>

    <xsl:template match="li">
         <li><xsl:apply-templates/></li>
    </xsl:template>

    <xsl:template match="ul">
         <ul><xsl:apply-templates/></ul>
    </xsl:template>


    <xsl:template match="br">
        <!-- ignore... -->
    </xsl:template>
</xsl:stylesheet>
