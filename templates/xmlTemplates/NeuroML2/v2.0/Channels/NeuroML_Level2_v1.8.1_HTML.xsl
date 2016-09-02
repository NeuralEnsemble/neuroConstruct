<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mml="http://morphml.org/morphml/schema"
    xmlns:meta="http://morphml.org/metadata/schema"
    xmlns:nml="http://morphml.org/neuroml/schema"
    xmlns:bio="http://morphml.org/biophysics/schema" >
    
    <xsl:import href="Biophysics_v1.8.1_HTML.xsl"/>
    <xsl:import href="ChannelML_v1.8.1_HTML.xsl"/>

<!--

    This file is used to convert MorphML files to a "neuroscientist friendly" view
    Note this doesn't by any means include all of the information present in the file, it just 
    summarises the notes, etc. embedded in the file and gives a summary of segment numbers, etc.
    
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

<xsl:output method="html" indent="yes" />



<!--Main Level 1 template-->

<xsl:template match="/mml:morphml">
<h3>NeuroML Level 1 file</h3>

<xsl:if test="count(/mml:morphml/meta:notes) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">General notes</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="/mml:morphml/meta:notes"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>

<xsl:if test="count(/mml:morphml/meta:properties) &gt; 0">
<br/>
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

    <xsl:apply-templates  select="/mml:morphml/meta:properties"/>
</table>
</xsl:if>

<xsl:apply-templates  select="/mml:morphml/mml:cells"/>

<br/>

</xsl:template>
<!--End Main template-->





<!--Main Level 2 template-->

<xsl:template match="/nml:neuroml">
<h3>NeuroML Level 2 file</h3>

<xsl:if test="count(/nml:neuroml/meta:notes) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">General notes</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="/nml:neuroml/meta:notes"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>


<xsl:if test="count(/nml:neuroml/meta:properties) &gt; 0">
<br/>
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

    <xsl:apply-templates  select="/mml:morphml/meta:properties"/>
</table>
</xsl:if>

<xsl:apply-templates  select="/nml:neuroml/nml:cells"/>
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
<xsl:apply-templates  select="/nml:neuroml/nml:channels"/>
</table>

<br/>

</xsl:template>
<!--End Main template-->





<xsl:template match="nml:cell|mml:cell">
<xsl:element name="a">
    <xsl:attribute name="name">CellType_<xsl:value-of select="@name"/></xsl:attribute>
</xsl:element>
<h3>Cell: <xsl:value-of select="@name"/></h3>


<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Name</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>


<xsl:apply-templates select="nml:status"/>
<xsl:apply-templates select="mml:status"/>

<xsl:if test="count(meta:notes) &gt; 0">
<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Description</xsl:with-param>
        <xsl:with-param name="comment">As described in the NeuroML file</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>
</xsl:if>

<xsl:apply-templates  select="meta:properties"/>

<xsl:apply-templates select="meta:authorList"/>

<xsl:apply-templates select="meta:publication"/>

<xsl:apply-templates select="meta:neuronDBref"/>



<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Total number of segments</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="count(mml:segments/mml:segment)"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>

<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Total number of cables</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="count(mml:cables/mml:cable)"/>
        with <xsl:value-of select="count(mml:cables/mml:cable[meta:group='soma_group'])"/> soma cable(s),
        <xsl:value-of select="count(mml:cables/mml:cable[meta:group='dendrite_group'])"/> dendritic cable(s)
        and <xsl:value-of select="count(mml:cables/mml:cable[meta:group='axon_group'])"/> axonal cable(s)&lt;/b&gt;
        </xsl:with-param>
</xsl:call-template>

<xsl:apply-templates  select="mml:cables"/>

</table>

<xsl:apply-templates  select="nml:biophysics"/>


</xsl:template>
<xsl:template match="mml:cables">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Cable details</xsl:with-param>
        <xsl:with-param name="value">
            <xsl:for-each select="mml:cable">
                <xsl:variable name="id"><xsl:value-of select="@id"/></xsl:variable>
                &lt;b&gt;<xsl:value-of select="@name"/> (id:<xsl:value-of select="$id"/>)&lt;/b&gt;<xsl:if 
                test="count(@fractAlongParent) &gt; 0 or count(@fract_along_parent) &gt; 0">, fraction along parent cable: &lt;b&gt;<xsl:value-of select="@fractAlongParent"/><xsl:value-of select="@fract_along_parent"/>&lt;/b&gt;</xsl:if>,
                number of segments in cable: &lt;b&gt;<xsl:value-of select="count(../../mml:segments/mml:segment[@cable=$id])"/>&lt;/b&gt;
                <xsl:if test="count(meta:group) &gt; 0">
                    &lt;br/&gt;&amp;nbsp;&amp;nbsp;&lt;/b&gt;This cable is present in groups: &lt;b&gt;<xsl:for-each select="meta:group">
                        <xsl:value-of select="."/>&amp;nbsp;&amp;nbsp;</xsl:for-each>
                </xsl:if>
                
                &lt;/b&gt;&lt;br/&gt;&lt;br/&gt;
            </xsl:for-each>
            
        </xsl:with-param>
</xsl:call-template>

                
<xsl:if test="count(mml:cablegroup) &gt; 0">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Cable Groups</xsl:with-param>
        <xsl:with-param name="comment">Groups of cables defined, for example, to allow easier specification of channel location</xsl:with-param>
        <xsl:with-param name="value">
            &lt;table border="0"  width = "500"&gt; 
                <xsl:for-each select="mml:cablegroup">
                   &lt;tr&gt; &lt;td&gt;
                    &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;
                        contains &lt;b&gt;<xsl:value-of select="count(mml:cable)"/>&lt;/b&gt; cables:  
                        <xsl:for-each select="mml:cable">
                            <xsl:variable name="id"><xsl:value-of select="@id"/></xsl:variable>
                            &lt;b&gt;<xsl:value-of select="../../mml:cable[@id=$id]/@name"/> (id:<xsl:value-of select="$id"/>) &lt;/b&gt;</xsl:for-each>
                    &lt;br/&gt;&lt;/td&gt; &lt;/tr&gt;
                </xsl:for-each>
            &lt;/table&gt; 
        </xsl:with-param>
    </xsl:call-template>
</xsl:if>
    
    
</xsl:template>


<xsl:template match="nml:biophysics">


<table frame="box" rules="all" align="centre" cellpadding="4"  width ="100%">

<h3>Biophysical properties of cell: <xsl:value-of select="../@name"/></h3>



    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Unit system of biophysical entities</xsl:with-param>
        <xsl:with-param name="comment">This can be either <b>SI Units</b> or <b>Physiological Units</b></xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@units"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
     
     
     <xsl:apply-templates/>
     
</table> <!-- round off table...-->
</xsl:template>






</xsl:stylesheet>
