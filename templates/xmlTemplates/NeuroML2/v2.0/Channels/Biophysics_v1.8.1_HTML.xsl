<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bio="http://morphml.org/biophysics/schema"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:meta="http://morphml.org/metadata/schema" >

    <xsl:import href="../ReadableUtils.xsl"/>
    
<!--

    This file is used to convert Biophysics info to a "neuroscientist friendly" HTML view
    
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

<xsl:template match="bio:mechanism">

    <xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="../@units"/></xsl:variable>   
    
    <xsl:choose>
    
    
    
    <xsl:when test="@type='Channel Mechanism'">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Channel Mechanism: &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
            <xsl:with-param name="comment">An active membrane conductance</xsl:with-param>
            <xsl:with-param name="value">
            <xsl:if test="(count(@passiveConductance) &gt; 0 and ( @passiveConductance='true' or @passiveConductance='1')) or
                          (count(@passive_conductance) &gt; 0 and ( @passive_conductance='true' or @passive_conductance='1'))">
                This is a &lt;b&gt;Passive Conductance&lt;/b&gt; and so conductance density (&lt;b&gt;gmax&lt;/b&gt;) and 
                reversal potential (&lt;b&gt;e&lt;/b&gt;) should be sufficient parameters to specify the mechanism fully.
            </xsl:if>
            <xsl:for-each select="bio:parameter">&lt;p&gt;
                <xsl:choose>
                    <xsl:when test="@name='gmax'">
                        Conductance density (&lt;b&gt;gmax&lt;/b&gt;) of &lt;b&gt;<xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                <xsl:with-param name="quantity">Conductance Density</xsl:with-param>
                                <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                        </xsl:call-template>&lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                    </xsl:when>
                    <xsl:when test="@name='e'">
                        Reversal potential (&lt;b&gt;e&lt;/b&gt;) of &lt;b&gt;<xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                <xsl:with-param name="quantity">Voltage</xsl:with-param>
                                <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                        </xsl:call-template>&lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                    </xsl:when>
                    <xsl:otherwise>
                        Parameter &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt; has value  &lt;b&gt;<xsl:value-of select="@value"/> &lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                    </xsl:otherwise>
                </xsl:choose>&lt;/p&gt;
            </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:when>
    
    
    
    </xsl:choose>

</xsl:template>



<xsl:template match="bio:specificCapacitance | bio:spec_capacitance">
    
    <xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="../@units"/></xsl:variable>   
    
    <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Specific Capacitance</xsl:with-param>
            <xsl:with-param name="comment">This is the capacitance per unit area of the membrane</xsl:with-param>
            <xsl:with-param name="value">
                
                <xsl:for-each select="bio:parameter">&lt;p&gt;Specific Capacitance of &lt;b&gt;

                    <xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                    <xsl:with-param name="quantity">Specific Capacitance</xsl:with-param>
                                    <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                                    </xsl:call-template>&lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                </xsl:for-each>
              
            </xsl:with-param>
        </xsl:call-template>
</xsl:template>



<xsl:template match="bio:specificAxialResistance | bio:spec_axial_resistance">
    
    <xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="../@units"/></xsl:variable>   
    
    <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Specific Axial Resistance</xsl:with-param>
            <xsl:with-param name="comment">This is the specific cytoplasmic resistance along a dendrite/axon</xsl:with-param>
            <xsl:with-param name="value">
                
                <xsl:for-each select="bio:parameter">&lt;p&gt;Specific Axial Resistance of &lt;b&gt;
                    
                    <xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                        <xsl:with-param name="quantity">Specific Resistance</xsl:with-param>
                        <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                    </xsl:call-template>&lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
</xsl:template>




<xsl:template match="bio:initialMembPotential | bio:init_memb_potential">

    <xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="../@units"/></xsl:variable>

    <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Initial Membrane Potential</xsl:with-param>
            <xsl:with-param name="comment">This quantity is often required for computational simulations and specifies the potential
                difference across the membrane at the start of the simulation.</xsl:with-param>
            <xsl:with-param name="value">

                <xsl:for-each select="bio:parameter">&lt;p&gt;Initial Membrane Potential &lt;b&gt;

                    <xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                                    <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                                    </xsl:call-template>&lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
</xsl:template>

<xsl:template match="bio:ion_props | bio:ionProperties">

    <xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="../@units"/></xsl:variable>

    <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Properties of ion &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
            <xsl:with-param name="comment">Local properties of an ion present at some locations of the cell. Should be limited to either: 1) &lt;b&gt;e&lt;/b&gt; for reversal potential of the ion
            or 2) &lt;b&gt;conc_e&lt;/b&gt; and &lt;b&gt;conc_i&lt;/b&gt; for the external and internal concentrations of the ion.</xsl:with-param>
            <xsl:with-param name="value">

                <xsl:for-each select="bio:parameter">
                    <xsl:choose>
                        <xsl:when test="@name='e'">
                    &lt;p&gt;Reversal potential:
                     &lt;b&gt; <xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                                    <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                                    </xsl:call-template>
                    &lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                        </xsl:when>
                        <xsl:when test="@name='conc_e'">
                    &lt;p&gt;External concentration:
                     &lt;b&gt; <xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                    <xsl:with-param name="quantity">Concentration</xsl:with-param>
                                    <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                                    </xsl:call-template>
                    &lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                        </xsl:when>
                        <xsl:when test="@name='conc_i'">
                    &lt;p&gt;Internal concentration:
                     &lt;b&gt; <xsl:value-of select="@value"/> <xsl:call-template name="getUnitsInSystem">
                                    <xsl:with-param name="quantity">Concentration</xsl:with-param>
                                    <xsl:with-param name="xmlFileUnitSystem"><xsl:value-of select="$xmlFileUnitSystem"/></xsl:with-param>
                                    </xsl:call-template>
                    &lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                        </xsl:when>
                        <xsl:otherwise>
                    &lt;p&gt;Undetermined Ion Property &lt;b&gt; <xsl:value-of select="@name"/> &lt;/b&gt;:
                     &lt;b&gt; <xsl:value-of select="@value"/>
                    &lt;/b&gt; on: &lt;b&gt;<xsl:for-each select="bio:group"><xsl:value-of select="."/>&amp;nbsp;</xsl:for-each>&lt;/b&gt;
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:with-param>
        </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
