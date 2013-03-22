<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
    This file is included with the neuroConstruct documentation.
    It is used to format the xml in XML/xmlForHtml/docs into a low graphics
    version for the internal help viewer, ucl.physiol.neuroconstruct.gui.HelpFrame
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" version="4.0" encoding="iso-8859-1" indent="yes"/>

    <xsl:template match="/">

    <xsl:comment>
        This HTML file was automatically generated from the documentation files in /docs/XML/xmlForHtml/docs
    </xsl:comment>

        <html>
          <head>
                <title><xsl:value-of select="document/header/title"/></title>

                 <style type="text/css">
                     h1 {color: gray; font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}
                     h3 {color: gray; font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}
                     p {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}
                     li {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}
                     ol {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}
                     ul {font-family: Dialog, Verdana, Helvetica, Arial, sans-serif}
                 </style>
          </head>
            <body>

                            <xsl:apply-templates/>
            </body>
        </html>

    </xsl:template>





    <xsl:template match="p">
        
        <xsl:element name="p">
           <xsl:attribute name="style"><xsl:value-of select="@style"/></xsl:attribute>

           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="b">
        <xsl:element name="b">
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="strong">
        <xsl:element name="b">
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="h1">
        <xsl:element name="h1">
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="h2">
        <xsl:element name="h2">
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="h3">
        <xsl:element name="h3">
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="section/title">
        <xsl:element name="h3">
            <xsl:attribute name="style">font-size: 16;color: white;background-color: #a5b6c6;padding: 5px 5px 5px 5px</xsl:attribute>
            <xsl:text>&#160;&#160;</xsl:text>
            <xsl:value-of select="section/title"/>
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>


    <xsl:template match="document/header/title">
        <xsl:element name="h3">
            <xsl:attribute name="style">font-size: 16;color: black</xsl:attribute>
            <xsl:value-of select="document/header/title"/>
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>



    <xsl:template match="a">
        <xsl:element name="a">
            <xsl:if test="count(@href) &gt; 0">
            <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="count(@name) &gt; 0">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            </xsl:if>
           <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>




    <xsl:template match="i">
        <i>
            <xsl:value-of select="."/>
        </i>
    </xsl:template>

    <xsl:template match="li">
         <li><xsl:apply-templates/> </li>
    </xsl:template>

    <xsl:template match="ul">
         <ul><xsl:apply-templates/></ul>
    </xsl:template>


    <xsl:template match="ol">
         <ol><xsl:apply-templates/></ol>
    </xsl:template>

    <xsl:template match="table">
         <table><xsl:apply-templates/></table>
    </xsl:template>


    <xsl:template match="tr">
         <tr><xsl:apply-templates/></tr>
    </xsl:template>


    <xsl:template match="td">
         <td><xsl:apply-templates/></td>
    </xsl:template>


    <xsl:template match="br">
        <!-- ignore... -->
    </xsl:template>
</xsl:stylesheet>
