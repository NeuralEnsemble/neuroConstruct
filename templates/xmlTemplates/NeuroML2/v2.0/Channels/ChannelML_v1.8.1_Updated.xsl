<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:meta="http://morphml.org/metadata/schema" >

    <xsl:import href="../ReadableUtils.xsl"/>

<!--

    This file is used to convert pre v1.7.3 ChannelML files to the latest format.

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

<xsl:output method="xml" indent="yes" />

  <xsl:template match="/">
      
      
      
      NOTE: The following is only useful when the file to be converted is a ChannelML file with a 
      pre v1.7.3 structure to the current_voltage_relation element!!
      
      The text below will have to be manually tuned to create a valid ChannelML file in the new format
      
      
      
     <xsl:apply-templates/>
  </xsl:template>

 <xsl:template match="node()|@*">
   <xsl:copy>
   <xsl:apply-templates select="@*"/>
   <xsl:apply-templates/>
   </xsl:copy>
 </xsl:template>
 
 
  <xsl:template match="/cml:channelml/cml:channel_type/cml:current_voltage_relation" >
    <xsl:choose>
        <xsl:when test="count(cml:integrate_and_fire) &gt; 0">
            <xsl:copy>
                <xsl:apply-templates select="@*"/>
                <xsl:apply-templates/>
            </xsl:copy>
        </xsl:when>
        <xsl:otherwise>
            <xsl:comment >
            NOTE: Some manual "tuning" of this code is needed to make a valid post v1.7.3 file!
            Including:
            - Removal of xmlns=...
            - Check through the generic functions, as plus signs can disappear from expressions
            - Also in expressions with conditions, the greater-than sign should be replaced with --ampersand--gt;
            
            </xsl:comment>
            
            
    <xsl:element name="current_voltage_relation" >
        <xsl:variable name="ion" select="cml:ohmic/@ion"/>
        <xsl:attribute name="cond_law">ohmic</xsl:attribute>
        <xsl:attribute name="ion"><xsl:value-of select="$ion"/></xsl:attribute>
        <xsl:attribute name="default_gmax"><xsl:value-of select="cml:ohmic/cml:conductance/@default_gmax"/></xsl:attribute>
        <xsl:attribute name="default_erev"><xsl:value-of select="/cml:channelml/cml:ion[@name=$ion]/@default_erev"/></xsl:attribute>
         
        <xsl:for-each select="cml:ohmic/cml:conductance/cml:rate_adjustments/cml:q10_settings">
        <xsl:text>
            </xsl:text>
           <xsl:copy>
           <xsl:apply-templates select="@*"/>
           <xsl:apply-templates/>
           </xsl:copy>
        </xsl:for-each>
        <xsl:for-each select="cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset">
        <xsl:text>
            </xsl:text>
           <xsl:copy>
           <xsl:apply-templates select="@*"/>
           <xsl:apply-templates/>
           </xsl:copy>
        </xsl:for-each>
        <xsl:for-each select="cml:ohmic/cml:conductance/cml:gate">
            <xsl:variable name="gateName"><xsl:value-of select="cml:state/@name"/></xsl:variable>
            <xsl:text>
            </xsl:text>
            <xsl:element name="gate">
                <xsl:attribute name="name"><xsl:value-of select="$gateName"/></xsl:attribute>
                <xsl:attribute name="instances"><xsl:value-of select="@power"/></xsl:attribute>
                <xsl:variable name="openStateFraction">
                    <xsl:if test="count(cml:state/@fraction) &gt; 0"><xsl:value-of select="cml:state/@fraction"/></xsl:if>
                </xsl:variable>
                <xsl:for-each select="../../../../cml:hh_gate[@state=$gateName] | ../../../../cml:ks_gate[cml:state/@name=$gateName]"> <!-- should only be one...-->
                <xsl:text>
                </xsl:text>
                    <xsl:choose>
                        <xsl:when test="count(cml:state) = 0">
                            <xsl:element name="closed_state">
                                <xsl:attribute name="id"><xsl:value-of select="$gateName"/>0</xsl:attribute>
                            </xsl:element><xsl:text>
                </xsl:text>
                            <xsl:element name="open_state">
                                <xsl:attribute name="id"><xsl:value-of select="$gateName"/></xsl:attribute>
                                <xsl:if test="string-length($openStateFraction) &gt; 0"><xsl:attribute name="fraction"><xsl:value-of select="$openStateFraction"/></xsl:attribute></xsl:if>
                            </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:for-each select="cml:state">
                                <xsl:variable name="stateName"><xsl:value-of select="@name"/></xsl:variable>
                                <xsl:variable name="openClosed"><xsl:choose>
                                    <xsl:when test="count(../../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate/cml:state[@name=$stateName]) = 0">closed_state</xsl:when>
                                    <xsl:otherwise>open_state</xsl:otherwise>
                                    </xsl:choose></xsl:variable>
                                <xsl:text>
                </xsl:text>&lt;<xsl:value-of select="$openClosed"/> id="<xsl:value-of select="$stateName"/>" <xsl:if 
                        test="string-length($openStateFraction) &gt; 0 and $openClosed='open_state'"> fraction="<xsl:value-of select="$openStateFraction"/>"</xsl:if>/&gt;
                            </xsl:for-each>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:for-each select="cml:transition/cml:voltage_gate/* | cml:transition/cml:voltage_conc_gate/* ">
                        <xsl:variable name="isKStrans"><xsl:if test="count(../../@target)&gt;0">1</xsl:if></xsl:variable>
                        <xsl:choose>
                            <xsl:when test="contains(name(.), 'conc_dependence')"><xsl:text>
                                
                </xsl:text> 
                                <xsl:copy>
                                    <xsl:apply-templates select="@*"/>
                                    <xsl:apply-templates/>
                                </xsl:copy>
                            </xsl:when>
                            <xsl:otherwise>
                        <xsl:variable name="rateRef"><xsl:value-of select="name(.)"/></xsl:variable>
                        <xsl:variable name="nameAttr">name="<xsl:value-of select="$rateRef"/><xsl:if 
                            test="$isKStrans = 1">_<xsl:value-of select="../../@source"/>_<xsl:value-of select="../../@target"/></xsl:if>"</xsl:variable>
                        <xsl:variable name="states">
                            <xsl:choose>
                                <xsl:when test="$isKStrans = 1 and $rateRef = 'alpha'"> from="<xsl:value-of select="../../@source"/>" to="<xsl:value-of select="../../@target"/>" </xsl:when>
                                <xsl:when test="$isKStrans = 1 and $rateRef = 'beta'"> from="<xsl:value-of select="../../@target"/>" to="<xsl:value-of select="../../@source"/>" </xsl:when>
                                <xsl:when test="$rateRef = 'alpha'">  from="<xsl:value-of select="$gateName"/>0" to="<xsl:value-of select="$gateName"/>" </xsl:when>
                                <xsl:when test="$rateRef = 'beta'">  from="<xsl:value-of select="$gateName"/>" to="<xsl:value-of select="$gateName"/>0" </xsl:when>
                                <xsl:when test="$rateRef = 'tau'">  from="<xsl:value-of select="$gateName"/>0" to="<xsl:value-of select="$gateName"/>" </xsl:when>
                                <xsl:when test="$rateRef = 'inf'">  from="<xsl:value-of select="$gateName"/>0" to="<xsl:value-of select="$gateName"/>" </xsl:when>
                                <xsl:otherwise>--- rate reference <xsl:value-of select="$rateRef"/> not supported in this automatic translation!! ----</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        
                        <xsl:variable name="elementName">
                            <xsl:choose>
                                <xsl:when test="$rateRef = 'alpha' or $rateRef = 'beta'">transition </xsl:when>
                                <xsl:when test="$rateRef = 'tau'">time_course </xsl:when>
                                <xsl:when test="$rateRef = 'inf'">steady_state </xsl:when>
                                <xsl:otherwise>--- rate reference <xsl:value-of select="$rateRef"/> not supported in this automatic translation!! ----</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        
                        <xsl:variable name="expression">
                            <xsl:choose>
                                <xsl:when test="count(cml:parameterised_hh) &gt; 0">
                                    <xsl:variable name="kVal"><xsl:value-of select="number(cml:parameterised_hh/cml:parameter[@name='k']/@value)"/>
                                    </xsl:variable>expr_form="<xsl:choose>
                                    <xsl:when test="cml:parameterised_hh/@type='exponential'">exponential</xsl:when>
                                    <xsl:when test="cml:parameterised_hh/@type='linoid'">exp_linear</xsl:when>
                                    <xsl:when test="cml:parameterised_hh/@type='sigmoid'">sigmoid</xsl:when>
                                    <xsl:otherwise>---- Unrecognised type of expression: <xsl:value-of select="cml:parameterised_hh/@type"/></xsl:otherwise>
                                </xsl:choose>" rate="<xsl:value-of 
                                      select="cml:parameterised_hh/cml:parameter[@name='A']/@value"/>" scale="<xsl:value-of
                                      select="number(1 div $kVal)"/>" midpoint="<xsl:value-of
                                      select="cml:parameterised_hh/cml:parameter[@name='d']/@value"/>"</xsl:when>
                                <xsl:when test="count(cml:generic) &gt; 0 or count(cml:generic_equation_hh) &gt; 0">
                                    <xsl:variable name="exprOld"><xsl:value-of select="cml:generic/@expr"/><xsl:value-of select="cml:generic_equation_hh/@expr"/></xsl:variable>
                                    <xsl:variable name="exprString"> expr="<xsl:value-of select="$exprOld"/>"</xsl:variable>expr_form="generic" <xsl:value-of select="$exprString"/></xsl:when>
                                <xsl:otherwise>--- unable to determine form of expression for rate!! ----</xsl:otherwise>
                                    
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:text>
                </xsl:text>     
                &lt;<xsl:value-of select="$elementName"/> <xsl:value-of select="$nameAttr"/> <xsl:value-of select="$states"/> <xsl:value-of select="$expression"/> /&gt;
                    </xsl:otherwise>
                    </xsl:choose>
                    </xsl:for-each>
                </xsl:for-each>
                <xsl:text>
            </xsl:text>
            </xsl:element>
            <xsl:text>
        </xsl:text>
        </xsl:for-each>
        
    </xsl:element>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template match="/cml:channelml/cml:channel_type/cml:hh_gate">
   <!-- hh_gate element integrated into current_voltage_relation-->
  </xsl:template>
  
  <xsl:template match="/cml:channelml/cml:channel_type/cml:ks_gate">
   <!-- hh_gate element integrated into current_voltage_relation-->
  </xsl:template>
  
  <xsl:template match="/cml:channelml/cml:ion">
   <!-- ion element integrated into current_voltage_relation-->
  </xsl:template>

</xsl:stylesheet>
