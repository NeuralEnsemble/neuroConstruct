<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mml="http://morphml.org/morphml/schema"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:meta="http://morphml.org/metadata/schema"
    xmlns:nml="http://morphml.org/neuroml/schema"
    xmlns:bio="http://morphml.org/biophysics/schema" >
    
<!--

    This file provides some handy functions for helping conversion of NeuroML files 
    to a "neuroscientist friendly" HTML view

    Funding for this work has been received from the Medical Research Council and the 
    Wellcome Trust. This file was initially developed as part of the neuroConstruct project
    
    Author: Padraig Gleeson
    Copyright 2009 University College London
    
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
-->



<!-- Function to get value converted to proper units.-->
<xsl:template name="getUnitsInSystem">
    <xsl:param name="quantity" />
    <xsl:param name="xmlFileUnitSystem" />
    <xsl:choose> 
        <xsl:when test="$xmlFileUnitSystem  = 'Physiological Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"> mS cm&lt;sup&gt;-2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Conductance'"> mS</xsl:when>
                <xsl:when test="$quantity = 'Voltage'"> mV</xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"> mV&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Time'"> ms</xsl:when>
                <xsl:when test="$quantity = 'InvTime'"> ms&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Length'"> cm</xsl:when>
                <xsl:when test="$quantity = 'Concentration'"> mole cm&lt;sup&gt;-3&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"> mole&lt;sup&gt;-1&lt;/sup&gt; cm&lt;sup&gt;3&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Specific Capacitance'"> uf/cm&lt;sup&gt;2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Specific Resistance'"> Kohm cm</xsl:when>
                <xsl:when test="$quantity = 'Current'"> uA</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:when>           
        <xsl:when test="$xmlFileUnitSystem  = 'SI Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"> S m&lt;sup&gt;-2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Conductance'"> S</xsl:when>
                <xsl:when test="$quantity = 'Voltage'"> V</xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"> V&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Time'"> s</xsl:when>
                <xsl:when test="$quantity = 'InvTime'"> s&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Length'"> m</xsl:when>
                <xsl:when test="$quantity = 'Concentration'"> mole m&lt;sup&gt;-3&lt;/sup&gt; (or mM)</xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"> mole&lt;sup&gt;-1&lt;/sup&gt; m&lt;sup&gt;3&lt;/sup&gt; (or mM&lt;sup&gt;-1&lt;/sup&gt;)</xsl:when>
                <xsl:when test="$quantity = 'Specific Capacitance'"> f/cm&lt;sup&gt;2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Specific Resistance'"> ohm m</xsl:when>
                <xsl:when test="$quantity = 'Current'"> A</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:when>   
    </xsl:choose>
</xsl:template>



<!-- Note, this will be overridden when this file is included in another schema -->
<xsl:variable name="xmlFileUnitSystem">SI Units</xsl:variable>


<!-- Function to get value converted to proper units.-->
<xsl:template name="getUnits">
    <xsl:param name="quantity" />
    <xsl:choose> 
        <xsl:when test="$xmlFileUnitSystem  = 'Physiological Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"> mS cm&lt;sup&gt;-2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Conductance'"> mS</xsl:when>
                <xsl:when test="$quantity = 'Voltage'"> mV</xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"> mV&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Time'"> ms</xsl:when>
                <xsl:when test="$quantity = 'InvTime'"> ms&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Length'"> cm</xsl:when>
                <xsl:when test="$quantity = 'Concentration'"> mole cm&lt;sup&gt;-3&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"> mole&lt;sup&gt;-1&lt;/sup&gt; cm&lt;sup&gt;3&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Specific Capacitance'"> uf/cm&lt;sup&gt;2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Specific Resistance'"> Kohm cm</xsl:when>
                <xsl:when test="$quantity = 'Current'"> uA</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:when>           
        <xsl:when test="$xmlFileUnitSystem  = 'SI Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"> S m&lt;sup&gt;-2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Conductance'"> S</xsl:when>
                <xsl:when test="$quantity = 'Voltage'"> V</xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"> V&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Time'"> s</xsl:when>
                <xsl:when test="$quantity = 'InvTime'"> s&lt;sup&gt;-1&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Length'"> m</xsl:when>
                <xsl:when test="$quantity = 'Concentration'"> mole m&lt;sup&gt;-3&lt;/sup&gt; (or mM)</xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"> mole&lt;sup&gt;-1&lt;/sup&gt; m&lt;sup&gt;3&lt;/sup&gt; (or mM&lt;sup&gt;-1&lt;/sup&gt;)</xsl:when>
                <xsl:when test="$quantity = 'Specific Capacitance'"> f/cm&lt;sup&gt;2&lt;/sup&gt;</xsl:when>
                <xsl:when test="$quantity = 'Specific Resistance'"> ohm m</xsl:when>
                <xsl:when test="$quantity = 'Current'"> A</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:when>   
    </xsl:choose>
</xsl:template>





<!-- Function add a single table row-->
<xsl:template name="tableRow">
    <xsl:param name="name" />
    <xsl:param name="comment" />
    <xsl:param name="value" />
    <xsl:param name="class" />
    
    <xsl:variable name="commentSize">
        <xsl:choose>
            <xsl:when test="string-length($comment)>200">75%</xsl:when>
            <xsl:when test="string-length($comment)>100">80%</xsl:when>
            <xsl:otherwise>80%</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <tr bgcolor="#FFFFFF">
        <xsl:choose>
            <xsl:when test="string-length($value)>0">
                <td width="30%" valign="top">
                    <xsl:choose>
                        <xsl:when test="string-length($comment)>0">
                            <table border="0">
                                <tr>
                                    <td><xsl:value-of select="$name"  disable-output-escaping="yes"/></td>
                                </tr>
                                <tr>
                                    <td><xsl:element name="span">
                                        <xsl:attribute name="style">color:#a0a0a0;font-style: italic;font-size: <xsl:value-of select="$commentSize"> </xsl:value-of></xsl:attribute>
                                        <xsl:value-of select="$comment"  disable-output-escaping="yes"/>
                                    </xsl:element></td>
                                </tr>
                            </table>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$name" disable-output-escaping="yes"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
                <td width="70%" bgcolor="#FFFFFF" valign="top"><xsl:value-of select="$value"  disable-output-escaping="yes"/></td>
            </xsl:when>
            <xsl:otherwise>
                <td width="30%" bgcolor="#FFFFFF"  colspan="2">
                    <xsl:choose>
                        <xsl:when test="string-length($comment)>0">
                            <p><xsl:value-of select="$name"   disable-output-escaping="yes"/></p>
                            <p><xsl:element name="span">
                                        <xsl:attribute name="style">color:#a0a0a0;font-style: italic;font-size: <xsl:value-of select="$commentSize"> </xsl:value-of></xsl:attribute>
                                        <xsl:value-of select="$comment"  disable-output-escaping="yes"/>
                                    </xsl:element></p>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$name" disable-output-escaping="yes"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </td>
            </xsl:otherwise>
            </xsl:choose>
    </tr> 
</xsl:template>



<!-- Function to try to format the rate expression to something this simulator is a bit happier with-->
<xsl:template name="formatExpression">
    <xsl:param name="variable" />
    <xsl:param name="oldExpression" />
    <xsl:choose>
        <xsl:when test="contains($oldExpression, '?')">
    <!-- Expression contains a condition!!-->
    <xsl:variable name="ifTrue"><xsl:value-of select="substring-before(substring-after($oldExpression,'?'), ':')"/></xsl:variable>
    <xsl:variable name="ifFalse"><xsl:value-of select="substring-after($oldExpression,':')"/></xsl:variable>
    
    When: <xsl:value-of select="substring-before($oldExpression,'?')"/>:&lt;br/&gt;<xsl:text>
    </xsl:text>&amp;nbsp;&amp;nbsp;&amp;nbsp;<xsl:value-of select="$variable"/>(v) = <xsl:value-of select="$ifTrue"/>&lt;br/&gt; otherwise: &lt;br/&gt;<xsl:text>
    
    </xsl:text>&amp;nbsp;&amp;nbsp;&amp;nbsp;<xsl:value-of select="$variable"/>(v) = <xsl:value-of select="$ifFalse"/>
    
        </xsl:when>
        <xsl:otherwise>
    <xsl:value-of select="$variable"/>(v) = <xsl:value-of select="$oldExpression"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>





<xsl:template match="meta:authorList">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Authors</xsl:with-param>
                <xsl:with-param name="value">&lt;table  border="0"&gt;
                
                    <xsl:if test="count(meta:modelAuthor)&gt;0">&lt;tr&gt;&lt;td&gt;Authors of original model: &lt;/td&gt;&lt;/tr&gt;
                        <xsl:for-each select="meta:modelAuthor">
                            &lt;tr&gt;&lt;td&gt;&lt;b&gt; <xsl:apply-templates select="." /> &lt;/b&gt;&lt;/td&gt;&lt;/tr&gt;
                        </xsl:for-each>
                    </xsl:if>
                    
                    <xsl:if test="count(meta:modelTranslator)&gt;0">&lt;tr&gt;&lt;td&gt;Translators of the model to NeuroML: &lt;/td&gt;&lt;/tr&gt;</xsl:if>
                    <xsl:for-each select="meta:modelTranslator">
                        &lt;tr&gt;&lt;td&gt;&lt;b&gt; <xsl:apply-templates select="." /> &lt;/b&gt;&lt;/td&gt;&lt;/tr&gt;
                    </xsl:for-each>
                    &lt;/table&gt;
                </xsl:with-param>
        </xsl:call-template>
</xsl:template>


<xsl:template match="meta:modelAuthor | meta:modelTranslator">
    &amp;nbsp;&amp;nbsp;&amp;nbsp;<xsl:value-of select="meta:name"/>
    <xsl:if test="count(meta:institution)&gt;0">
        &amp;nbsp;(<xsl:value-of select="meta:institution"/>)
    </xsl:if>
    <xsl:if test="count(meta:email)&gt;0">
        &amp;nbsp;<xsl:value-of select="meta:email"/>
    </xsl:if>
    <xsl:if test="count(meta:comment)&gt;0">
        &amp;nbsp; (<xsl:value-of select="meta:comment"/>)
    </xsl:if>
</xsl:template>



<xsl:template match="meta:publication">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Referenced publication</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:fullTitle"/>
                
                &lt;a href="<xsl:value-of select="meta:pubmedRef"/>"&gt;Pubmed&lt;/a&gt;&lt;/b&gt;
                </xsl:with-param>
        </xsl:call-template>
</xsl:template>

<xsl:template match="meta:neuronDBref">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Reference in NeuronDB</xsl:with-param>
                <xsl:with-param name="value">
                
                &lt;b&gt;&lt;a href="<xsl:value-of select="meta:uri"/>"&gt;<xsl:value-of select="meta:modelName"/>&lt;/a&gt;&lt;/b&gt;
                </xsl:with-param>
        </xsl:call-template>
</xsl:template>

<xsl:template match="meta:modelDBref">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Reference in ModelDB</xsl:with-param>
                <xsl:with-param name="value">
                
                &lt;b&gt;&lt;a href="<xsl:value-of select="meta:uri"/>"&gt;<xsl:value-of select="meta:modelName"/>&lt;/a&gt;&lt;/b&gt;
                </xsl:with-param>
        </xsl:call-template>
</xsl:template>



<xsl:template match="meta:properties">
    
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Properties</xsl:with-param>
        <xsl:with-param name="value">&lt;table&gt;
                    <xsl:for-each select="meta:property">
                        &lt;tr&gt; 
                            &lt;td&gt;<xsl:value-of select="meta:tag"/><xsl:value-of select="@tag"/>&lt;/td&gt; 
                            &lt;td&gt;= &lt;b&gt;<xsl:value-of select="meta:value"/><xsl:value-of select="@value"/>&lt;/b&gt;&lt;/td&gt;
                        &lt;/tr&gt;</xsl:for-each>
                    &lt;/table&gt;</xsl:with-param>
     </xsl:call-template>

</xsl:template>



<xsl:template match="cml:status | mml:status | nml:status">
    <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Status</xsl:with-param>
            <xsl:with-param name="comment">Status of element in file</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;&lt;span style="color:
                <xsl:choose>
                    <xsl:when test="@value='stable'">green"&gt;Stable</xsl:when>
                    <xsl:when test="@value='in_progress'">#DD8C00"&gt;In progress, use with caution</xsl:when>
                    <xsl:when test="@value='known_issues'">red"&gt;Known issues. Should not be used in any simulations!</xsl:when>
                    <xsl:when test="@value='deprecated'">red"&gt;Deprecated. File &lt;b&gt;may&lt;/b&gt; still work but a new form of some elements is preferred!</xsl:when>
                    <xsl:otherwise>red"&gt;Indeterminate status!</xsl:otherwise>
                </xsl:choose>
                &lt;/span&gt;
                &lt;/b&gt;
                <xsl:for-each select="meta:comment">&lt;br/&gt;Comment: <xsl:value-of select="."/></xsl:for-each>
                <xsl:for-each select="meta:issue">&lt;br/&gt; &lt;span style="color:#DD8C00"&gt;  Issue: <xsl:value-of select="."/>&lt;/span&gt;</xsl:for-each>
                <xsl:for-each select="meta:contributor/meta:name">&lt;br/&gt;Contributor: <xsl:value-of select="."/></xsl:for-each>
                </xsl:with-param>
    </xsl:call-template>
</xsl:template>


</xsl:stylesheet>
