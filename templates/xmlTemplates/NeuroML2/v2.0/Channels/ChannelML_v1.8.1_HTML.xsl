<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cml="http://morphml.org/channelml/schema"
    xmlns:meta="http://morphml.org/metadata/schema" >

    <xsl:import href="../ReadableUtils.xsl"/>

<!--

    This file is used to convert ChannelML files to a "neuroscientist friendly" HTML view.

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

<xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="/cml:channelml/@units"/></xsl:variable>



<!--Main template-->

<xsl:template match="/cml:channelml">

<xsl:if test="count(/cml:channelml/meta:notes) &gt; 0">
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">General notes</xsl:with-param>
        <xsl:with-param name="comment">Notes present in ChannelML file</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="/cml:channelml/meta:notes"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>
</xsl:if>

<br/>

<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Unit system of ChannelML file</xsl:with-param>
        <xsl:with-param name="comment">This can be either <b>SI Units</b> or <b>Physiological Units (milliseconds, centimeters, millivolts, etc.)</b></xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="$xmlFileUnitSystem"/>&lt;/b&gt;</xsl:with-param>
     </xsl:call-template>
</table>

<xsl:if test="count(cml:ion)>0">
    <h3>Ions involved in this channel: </h3>
    <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
        <xsl:apply-templates  select="cml:ion"/>
    </table>
</xsl:if>


<xsl:apply-templates  select="cml:channel_type"/>
<xsl:apply-templates  select="cml:ion_concentration"/>
<xsl:apply-templates  select="cml:synapse_type"/>


</xsl:template>
<!--End Main template-->


<xsl:template match="cml:channel_type">
<h3>Channel: <xsl:value-of select="@name"/></h3>


<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Name</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>



<xsl:apply-templates select="cml:status"/>


<xsl:if test="count(meta:notes) &gt; 0">
<xsl:call-template name="tableRow">
        <xsl:with-param name="name">Description</xsl:with-param>
        <xsl:with-param name="comment">As described in the ChannelML file</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
</xsl:call-template>
</xsl:if>


<xsl:apply-templates select="meta:authorList"/>

<xsl:apply-templates select="meta:publication"/>

<xsl:apply-templates select="meta:neuronDBref"/>

<xsl:apply-templates select="meta:modelDBref"/>

<xsl:apply-templates select="cml:current_voltage_relation"/>


</table>
<br/>
<br/>
                
<xsl:for-each select="cml:current_voltage_relation/cml:gate">
 
<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">  Gate: &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The equations below determine the dynamics of gating state <xsl:value-of select="@name"/></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Instances of gating elements</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@instances"/>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    
    <xsl:for-each select='cml:closed_state'>
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Closed state</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@id"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
    </xsl:for-each>
    
    <xsl:for-each select='cml:open_state'>
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Open state</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@id"/>
            <xsl:if test="count(@fraction)&gt;0">&amp;nbsp;&amp;nbsp;&amp;nbsp;(fractional conductance: <xsl:value-of select="@fraction"/>)</xsl:if>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
    </xsl:for-each>
    
    <xsl:for-each select='cml:transition | cml:time_course | cml:steady_state'>
    
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">&amp;nbsp;</xsl:with-param>
        </xsl:call-template>
        
        <xsl:variable name="typeInfo"><xsl:choose>
            <xsl:when test="name() = 'transition'">Transition</xsl:when>
            <xsl:when test="name() = 'time_course'">Transition time course</xsl:when>
            <xsl:when test="name() = 'steady_state'">Transition steady state</xsl:when>
            <xsl:otherwise>Unknown</xsl:otherwise></xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">&amp;nbsp;&amp;nbsp;&amp;nbsp;&amp;nbsp;<xsl:value-of select="$typeInfo"/>: &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt; from &lt;b&gt;<xsl:value-of select="@from"/>&lt;/b&gt; to &lt;b&gt;<xsl:value-of select="@to"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
        
        <xsl:choose>
            <xsl:when test="@expr_form='generic'">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Generic expression</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>(v) = <xsl:value-of select="@expr"/>&lt;/b&gt; </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            
            <xsl:otherwise>
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Expression</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>(v) = <xsl:choose>
                        <xsl:when test="@expr_form='exp_linear'" >A*((v-V&lt;sub&gt;1/2&lt;/sub&gt;)/B) / (1 - exp(-(v-V&lt;sub&gt;1/2&lt;/sub&gt;)/B))</xsl:when>
                        <xsl:when test="@expr_form='exponential'" >A*exp((v-V&lt;sub&gt;1/2&lt;/sub&gt;)/B)</xsl:when>
                        <xsl:when test="@expr_form='sigmoid'" >A / (1 + exp((v-V&lt;sub&gt;1/2&lt;/sub&gt;)/B))</xsl:when>
                        <xsl:otherwise >Unsupported expression type!</xsl:otherwise>

                    </xsl:choose>&lt;/b&gt; &amp;nbsp;&amp;nbsp; &lt;i&gt;(<xsl:value-of select="@expr_form" />)&lt;/i&gt;</xsl:with-param>
                </xsl:call-template>

                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Parameter values</xsl:with-param>
                    <xsl:with-param name="value">
                        &lt;b&gt;
                        A = <xsl:value-of select="@rate" /> <xsl:call-template name="getUnits">
                            <xsl:with-param name="quantity">InvTime</xsl:with-param></xsl:call-template>&amp;nbsp;&amp;nbsp;
                        B = <xsl:value-of select="@scale" /> <xsl:call-template name="getUnits">
                            <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&amp;nbsp;&amp;nbsp;
                        V&lt;sub&gt;1/2&lt;/sub&gt; = <xsl:value-of select="@midpoint" /> <xsl:call-template name="getUnits">
                            <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>
                        &lt;/b&gt;
                    </xsl:with-param>
                </xsl:call-template>


                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Substituted</xsl:with-param>
                    <xsl:with-param name="value"><xsl:choose>

                        <xsl:when test="@expr_form = 'exponential'">
                            &lt;b&gt;<xsl:value-of select="@name"/>(v) =
                            <xsl:value-of select="@rate"/> * e &lt;sup&gt;
                            (v - (<xsl:value-of select="@midpoint"/>))/<xsl:value-of select="@scale"/>&lt;/sup&gt;</xsl:when>

                        <xsl:when test="@expr_form = 'sigmoid'">
                            &lt;table border="0"&gt;
                                &lt;tr&gt;
                                    &lt;td  rowspan="2" valign="center"&gt;
                                        &lt;b&gt;<xsl:value-of select="@name"/>(v) =&lt;/b&gt;
                                    &lt;/td&gt;
                                    &lt;td align="center"&gt;
                                        &lt;b&gt;<xsl:value-of select="@rate"/>&lt;/b&gt;
                                    &lt;/td&gt;
                                &lt;tr&gt;
                                    &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                        &lt;b&gt;1+ e&lt;sup&gt; (
                                        v - (<xsl:value-of select="@midpoint"/>))/<xsl:value-of select="@scale"/>&lt;/sup&gt;&lt;/b&gt;
                                    &lt;/td&gt;
                                &lt;/tr&gt;
                            &lt;/table&gt;
                        </xsl:when>

                        <xsl:when test="@expr_form = 'exp_linear'">
                            &lt;table border="0"&gt;
                                &lt;tr&gt;
                                    &lt;td  rowspan="2" valign="center"&gt;
                                        &lt;b&gt;<xsl:value-of select="@name"/>(v) =&lt;/b&gt;
                                    &lt;/td&gt;
                                    &lt;td align="center"&gt;
                                        &lt;b&gt;<xsl:value-of select="@rate"/> * (
                                        v - (<xsl:value-of select="@midpoint"/>)) / <xsl:value-of select="@scale"/>&lt;/b&gt;
                                    &lt;/td&gt;
                                &lt;tr&gt;
                                    &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                        &lt;b&gt;1- e&lt;sup&gt; -((
                                        v - (<xsl:value-of select="@midpoint"/>)) / <xsl:value-of select="@scale"/>)&lt;/sup&gt;&lt;/b&gt;
                                    &lt;/td&gt;
                                &lt;/tr&gt;
                            &lt;/table&gt;
                        </xsl:when>

                        <xsl:otherwise>???</xsl:otherwise>

                    </xsl:choose>&lt;/b&gt;</xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

        
    </xsl:for-each>
    
        
</table>
<br/>
<br/>
</xsl:for-each>

<xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">

<table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">  Gate: &lt;b&gt;<xsl:value-of select="cml:state/@name"/>&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The equations below determine the dynamics of gating state <xsl:value-of select="cml:state/@name"/></xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Gate power</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@power"/>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:variable name="stateName" select="cml:state/@name"/>

    <xsl:for-each select='../../../../cml:hh_gate[@state=$stateName]'>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Gating model formalism</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;Hodgkin Huxley single state transition&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

        <xsl:for-each select='cml:transition'>

            <xsl:apply-templates/>

        </xsl:for-each>

    </xsl:for-each>

</table>
<br/>
<br/>



        </xsl:for-each>
        



        <xsl:for-each select="cml:ks_gate">

            <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">

                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">  Kinetic Scheme</xsl:with-param>
                    <xsl:with-param name="comment">The states and transitions below form a kinetic scheme description of the channel</xsl:with-param>
                </xsl:call-template>


                <xsl:for-each select='cml:state'>

                    <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">State</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
                </xsl:call-template>

                </xsl:for-each>

                <xsl:for-each select='cml:transition'>

                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Transition from &lt;b&gt;<xsl:value-of select="@src"/><xsl:value-of select="@source"/>&lt;/b&gt;  <!-- to allow for pre v2.0 form...-->
                    to &lt;b&gt;<xsl:value-of select="@target"/>&lt;/b&gt;</xsl:with-param>
                    <xsl:with-param name="comment">Transition between two states</xsl:with-param>
                </xsl:call-template>

                    <xsl:apply-templates/>

                </xsl:for-each>

            </table>
        </xsl:for-each>

        <xsl:apply-templates select="cml:impl_prefs"/>

</xsl:template>


<xsl:template match="cml:current_voltage_relation">

    <xsl:apply-templates select="cml:ohmic"/>                <!-- Pre v1.7.3 form-->
    <xsl:apply-templates select="cml:integrate_and_fire"/>   <!-- Pre v1.7.3 form-->
    
    <xsl:if test="count(@cond_law) &gt; 0">                  <!-- Post v1.7.3 form-->
        
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Current voltage relationship</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@cond_law"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>

       <xsl:variable name="ionname"><xsl:value-of select="@ion"/></xsl:variable>


        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Ion involved in channel</xsl:with-param>

            <xsl:with-param name="comment">The ion which is actually flowing through the channel and its default reversal potential. 
            Note that the reversal potential will normally depend on the internal and external concentrations of the ion at the segment on which the channel is placed.</xsl:with-param>

            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="$ionname"/> (default E&lt;sub&gt;<xsl:value-of
            select="$ionname"/>&lt;/sub&gt; = <xsl:value-of select="@default_erev"/><xsl:call-template name="getUnits">
                <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;)
                <xsl:if test="count(@fixed_erev) &gt; 0"> &lt;br/&gt;Reversal potential of this channel is fixed (not externally influenced): 
                &lt;b&gt;<xsl:value-of select="@fixed_erev"/>&lt;/b&gt; </xsl:if></xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Default maximum conductance density</xsl:with-param>
            <xsl:with-param name="comment">Note that the conductance density of the channel will be set when it is placed on the cell.</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;G&lt;sub&gt;max&lt;/sub&gt; = <xsl:value-of select="@default_gmax"/> <xsl:call-template name="getUnits">
            <xsl:with-param name="quantity">Conductance Density</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>

        <xsl:if test="count(meta:notes) &gt; 0">
            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Comment on conductance</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
            </xsl:call-template>
        </xsl:if>

        <xsl:if test="@cond_law = 'ohmic'">
            
            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Conductance expression</xsl:with-param>
                <xsl:with-param name="comment">Expression giving the actual conductance as a function of time and voltage</xsl:with-param>

                <xsl:with-param name="value">&lt;b&gt;G&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;(v,t) = G&lt;sub&gt;max&lt;/sub&gt;
                    <xsl:for-each select="cml:gate">
                        <xsl:text> * </xsl:text><xsl:if test="count(@fraction)&gt;0 and number(@fraction) !=1">
                            (<xsl:value-of select="@fraction"/><xsl:text> * </xsl:text>
                          </xsl:if>
                        <xsl:value-of select="@name"/>(v,t)
                        <xsl:if test="count(@fraction)&gt;0 and number(@fraction) !=1">)</xsl:if>
                        <xsl:if test="number(@instances) !=1">&lt;sup&gt;<xsl:value-of select="@instances"/>&lt;/sup&gt;</xsl:if></xsl:for-each>&lt;/b&gt;
                </xsl:with-param>
            </xsl:call-template>

            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Current due to channel</xsl:with-param>
                <xsl:with-param name="comment">Ionic current through the channel</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;I&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;(v,t) =
                    G&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;(v,t)<xsl:text> * </xsl:text>(v - E&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;)&lt;/b&gt;</xsl:with-param>
            </xsl:call-template>
            
            
            <xsl:apply-templates select="cml:q10_settings"/>

            <xsl:apply-templates select="cml:offset"/>
            
            <xsl:for-each  select="cml:conc_dependence">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Concentration dependence of gates</xsl:with-param>
                    <xsl:with-param name="comment">The dynamics of one or more gates are dependent on both the potential difference across the
                    channel, and on the concentration of the substance specified here</xsl:with-param>
                    <xsl:with-param name="value">Name: &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;&lt;br/&gt;
                        Ion: &lt;b&gt;<xsl:value-of select="@ion"/>&lt;/b&gt;<xsl:if test="count(@charge) &gt; 0">, charge:  &lt;b&gt;<xsl:value-of select="@charge"/>&lt;/b&gt;</xsl:if>               &lt;br/&gt;
                        Variable as used in rate equations: &lt;b&gt;<xsl:value-of select="@variable_name"/>&lt;/b&gt;&lt;br/&gt;
                        Min concentration: &lt;b&gt;<xsl:value-of select="@min_conc"/>&lt;/b&gt; (required by simulators for table of voltage/conc dependencies)&lt;br/&gt;
                        Max concentration: &lt;b&gt;<xsl:value-of select="@max_conc"/>&lt;/b&gt; (required by simulators for table of voltage/conc dependencies)
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:for-each>
            
            

        </xsl:if>
       
    

    </xsl:if>
    
    <xsl:for-each  select="../cml:parameters">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Parameters</xsl:with-param>
            <xsl:with-param name="comment">A number of parameters which can be used in the rate expressions, etc. for the channels.
            These should be publicly accessible in the objects implementing the channel.</xsl:with-param>
            <xsl:with-param name="value"><xsl:for-each  select="cml:parameter">
                &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt; = &lt;b&gt;<xsl:value-of select="@value"/>&lt;/b&gt; &lt;br/&gt;</xsl:for-each></xsl:with-param>
        </xsl:call-template>
    </xsl:for-each>
    
</xsl:template>





<xsl:template match="cml:ohmic">
    
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Current voltage relationship</xsl:with-param>
        <xsl:with-param name="comment">Note: only ohmic and integrate_and_fire current voltage relationships are supported in current specification</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;Ohmic&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

   <xsl:variable name="ionname"><xsl:value-of select="@ion"/></xsl:variable>


    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Ion involved in channel</xsl:with-param>

        <xsl:with-param name="comment">The ion which is actually flowing through the channel and its default reversal potential. 
        Note that the reversal potential will depend on the internal and external concentrations of the ion at the segment on which the channel is placed.</xsl:with-param>

        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="$ionname"/> (default E&lt;sub&gt;<xsl:value-of
        select="$ionname"/>&lt;/sub&gt; = <xsl:value-of select="../../../cml:ion[string(@name) = string($ionname)]/@default_erev"/><xsl:call-template name="getUnits">
            <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;)</xsl:with-param>
    </xsl:call-template>

    <xsl:if test="count(cml:conductance) &gt; 0">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Default maximum conductance density</xsl:with-param>
            <xsl:with-param name="comment">Note that the conductance density of the channel will be set when it is placed on the cell.</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;G&lt;sub&gt;max&lt;/sub&gt; = <xsl:value-of select="cml:conductance/@default_gmax"/> <xsl:call-template name="getUnits">
                <xsl:with-param name="quantity">Conductance Density</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
        
        <xsl:if test="count(cml:conductance/meta:notes) &gt; 0">
            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Comment on conductance</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:conductance/meta:notes"/>&lt;/b&gt;</xsl:with-param>
            </xsl:call-template>
        </xsl:if>


        <xsl:apply-templates select="cml:conductance/cml:rate_adjustments/cml:q10_settings"/>
        
        <xsl:apply-templates select="cml:conductance/cml:rate_adjustments/cml:offset"/>


        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Conductance expression</xsl:with-param>
            <xsl:with-param name="comment">Expression giving the actual conductance as a function of time and voltage</xsl:with-param>

            <xsl:with-param name="value">&lt;b&gt;G&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;(v,t) = G&lt;sub&gt;max&lt;/sub&gt;
                <xsl:for-each select="cml:conductance/cml:gate">
                    <xsl:text> * </xsl:text><xsl:if test="count(cml:state/@fraction)&gt;0 and number(cml:state/@fraction) !=1">
                        (<xsl:value-of select="cml:state/@fraction"/><xsl:text> * </xsl:text>
                      </xsl:if>
                    <xsl:value-of select="cml:state/@name"/>(v,t)
                    <xsl:if test="count(cml:state/@fraction)&gt;0 and number(cml:state/@fraction) !=1">)</xsl:if>
                    <xsl:if test="number(@power) !=1">&lt;sup&gt;<xsl:value-of select="@power"/>&lt;/sup&gt;</xsl:if></xsl:for-each>&lt;/b&gt;
            </xsl:with-param>
        </xsl:call-template>

        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Current due to channel</xsl:with-param>
            <xsl:with-param name="comment">Ionic current through the channel</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;I&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;(v,t) =
                G&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;(v,t)<xsl:text> * </xsl:text>(v - E&lt;sub&gt;<xsl:value-of select="$ionname"/>&lt;/sub&gt;)&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>

    </xsl:if>

</xsl:template>


<xsl:template match="cml:offset">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Voltage offset</xsl:with-param>
        <xsl:with-param name="comment">This introduces a shift in the voltage dependence of the rate equations.
            If, for example, the equation parameters being used in a model were from a different species,
            this offset can be introduced to alter the firing threshold to something closer to the species
            being modelled. See mappings for details.</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@value"/> <xsl:call-template name="getUnits">
<xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
</xsl:template>


<xsl:template match="cml:integrate_and_fire">


    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Current voltage relationship</xsl:with-param>
        <xsl:with-param name="comment">Note: only ohmic and integrate_and_fire current voltage relationships are supported in current specification</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;Integrate and Fire&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>


    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Threshold</xsl:with-param>
        <xsl:with-param name="comment">Voltage at which the mechanism causes the segment/cell to fire, i.e. membrane potential will be reset to v_reset</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;threshold =  <xsl:value-of select="@threshold"/>
                <xsl:call-template name="getUnits"><xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Refractory period</xsl:with-param>
        <xsl:with-param name="comment">Time after a spike during which the segment will be clamped to v_reset (clamping current given by i = g_refrac*(v - v_reset))</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;t_refrac =  <xsl:value-of select="@t_refrac"/>
                <xsl:call-template name="getUnits"><xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Reset membrane potential</xsl:with-param>
        <xsl:with-param name="comment">Membrane potential is reset to this after spiking</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt; v_reset = <xsl:value-of select="@v_reset"/>
                <xsl:call-template name="getUnits"><xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Conductance dureing refractory period</xsl:with-param>
        <xsl:with-param name="comment">Conductance during the period t_refrac after a spike, when the current due to this mechanism is given by i = g_refrac*(v - v_reset), therefore a high value for g_refrac, e.g. 100 microsiemens, will effectively clamp the cell at v_reset</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt; g_refrac = <xsl:value-of select="@g_refrac"/>
                <xsl:call-template name="getUnits"><xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>



</xsl:template>


<xsl:template match="cml:q10_settings">
    
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Q10 scaling</xsl:with-param>
        <xsl:with-param name="comment">Q10 scaling affects the tau in the rate equations. It allows rate equations experimentally calculated at one temperature
        to be used at a different temperature.</xsl:with-param>

        <xsl:with-param name="value">
            &lt;table  border="0"&gt;&lt;tr&gt;&lt;td&gt;<xsl:choose>
                <xsl:when test="count(@gate) &gt; 0">Q10 adjustment applied to gate: &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp; &lt;b&gt;<xsl:value-of select="@gate"/>&lt;/b&gt;</xsl:when>
                <xsl:otherwise>Q10 adjustment applied to gates: &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp; &lt;b&gt;all&lt;/b&gt;</xsl:otherwise>
            </xsl:choose> &lt;/td&gt;&lt;/tr&gt;
            <xsl:choose><xsl:when test="count(@q10_factor) &gt; 0">
            &lt;tr&gt;&lt;td&gt; Q10_factor: &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp; &lt;b&gt;<xsl:value-of select="@q10_factor"/>&lt;/b&gt; &lt;/td&gt;&lt;/tr&gt;
            &lt;tr&gt;&lt;td&gt; Experimental temperature (at which rate constants below were determined): &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp; &lt;b&gt;<xsl:value-of select="@experimental_temp"/>  &lt;sup&gt;o&lt;/sup&gt;C  &lt;/b&gt; &lt;/td&gt;&lt;/tr&gt;
            &lt;tr&gt;&lt;td&gt; Expression for tau at T using tauExp as calculated from rate equations: &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp;
                   &lt;b&gt;tau(T) = tauExp / <xsl:value-of select="@q10_factor"/>^((T - <xsl:value-of select="@experimental_temp"/>)/10)&lt;/b&gt; &lt;/td&gt;&lt;/tr&gt;
                   </xsl:when><xsl:when test="count(@fixed_q10) &gt; 0">
            &lt;tr&gt;&lt;td&gt; Fixed Q10 value (i.e. only specified at one temperature): &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp; &lt;b&gt;<xsl:value-of select="@fixed_q10"/>&lt;/b&gt; &lt;/td&gt;&lt;/tr&gt;
            &lt;tr&gt;&lt;td&gt; Experimental temperature (sims at other temps should be disallowed): &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp; &lt;b&gt;<xsl:value-of select="@experimental_temp"/>  &lt;sup&gt;o&lt;/sup&gt;C  &lt;/b&gt; &lt;/td&gt;&lt;/tr&gt;
                                &lt;tr&gt;&lt;td&gt; Expression for tau at T using tauExp as calculated from rate equations: &lt;/td&gt; &lt;td&gt;&amp;nbsp;&amp;nbsp;
                   &lt;b&gt;tau(T) = tauExp / <xsl:value-of select="@fixed_q10"/>&lt;/b&gt; &lt;/td&gt;&lt;/tr&gt;
            </xsl:when></xsl:choose>

            &lt;/table&gt;

        </xsl:with-param>
    </xsl:call-template>

</xsl:template>









<xsl:template match="cml:impl_prefs">

    <table frame="box" rules="all" align="centre" cellpadding="4" width ="100%">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Implementation Preferences</xsl:with-param>
            <xsl:with-param name="comment">Information is provided to help produce the best implementation of the channel
        mechanism. Due to some parameters in the channel mechanism the default values used in the
        simulator mappings may not be sufficient, e.g. if the rate equations change rapidly,
        but the default table size isn't large enough.</xsl:with-param>
        </xsl:call-template>

        <xsl:apply-templates select="cml:comment"/>
        <xsl:apply-templates select="cml:table_settings"/>
    </table>


</xsl:template>

<xsl:template match="cml:comment">

        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Comment</xsl:with-param>
            <xsl:with-param name="comment">Explanation taken from file</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="."/> &lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
</xsl:template>

<xsl:template match="cml:table_settings">

        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Settings for rate equation tables</xsl:with-param>
            <xsl:with-param name="comment">Recommended settings if a table of values is used to speed up calculation
            of the rate equation values.</xsl:with-param>
            <xsl:with-param name="value">Number of table divisions: &lt;b&gt;<xsl:value-of select="@table_divisions"/>&lt;/b&gt;&lt;br/&gt;
            Maximum voltage for tables: &lt;b&gt;<xsl:value-of select="@max_v"/> <xsl:call-template name="getUnits">
               <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;&lt;br/&gt;
            Minimum voltage for tables: &lt;b&gt;<xsl:value-of select="@min_v"/> <xsl:call-template name="getUnits">
               <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;&lt;br/&gt;</xsl:with-param>
        </xsl:call-template>
</xsl:template>


<xsl:template match="cml:voltage_gate | cml:voltage_conc_gate">

    <xsl:variable name="stateName" select="../../@state"/>

            <xsl:if test="count(cml:tau) = 0 and count(cml:inf) = 0">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Expression controlling gate:</xsl:with-param>
                    <xsl:with-param name="value">

                        &lt;table border="0"&gt;
                            &lt;tr&gt;
                                &lt;td&gt;
                                    &lt;b&gt;d<xsl:value-of select="$stateName"/>(v,t)&lt;/b&gt;
                                &lt;/td&gt;

                                &lt;td rowspan="2" valign="center"&gt;
                                    &lt;b&gt; = alpha(v) * (1-<xsl:value-of select="$stateName"/>)
                                                    - beta(v) * <xsl:value-of select="$stateName"/>&lt;/b&gt;
                                &lt;/td&gt;

                                &lt;td rowspan="2" valign="center"&gt;
                                    &amp;nbsp;&amp;nbsp;or&amp;nbsp;&amp;nbsp;
                                &lt;/td&gt;

                                &lt;td&gt;
                                    &lt;b&gt;d<xsl:value-of select="$stateName"/>(v,t)&lt;/b&gt;
                                &lt;/td&gt;

                                 &lt;td rowspan="2" valign="center"&gt;
                                    &lt;b&gt; = &lt;/b&gt;
                                &lt;/td&gt;

                                &lt;td rowspan="1" valign="center"&gt;
                                    &lt;b&gt;inf(v) - <xsl:value-of select="$stateName"/>&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                            &lt;tr&gt;

                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;dt&lt;/b&gt;
                                &lt;/td&gt;

                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;dt&lt;/b&gt;
                                &lt;/td&gt;


                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;tau(v)&lt;/b&gt;
                                &lt;/td&gt;
                            &lt;/tr&gt;
                        &lt;/table&gt;

                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <xsl:if test="count(cml:tau) &gt; 0 or count(cml:inf) &gt; 0">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Expression controlling gate:</xsl:with-param>
                    <xsl:with-param name="value">
                        &lt;b&gt;

                        &lt;table border="0"&gt;
                            &lt;tr&gt;

                                &lt;td&gt;
                                    &lt;b&gt;d<xsl:value-of select="$stateName"/>(v,t)&lt;/b&gt;
                                &lt;/td&gt;

                                 &lt;td rowspan="2" valign="center"&gt;
                                    &lt;b&gt; = &lt;/b&gt;
                                &lt;/td&gt;

                                &lt;td rowspan="1" valign="center"&gt;
                                    &lt;b&gt;inf(v) - <xsl:value-of select="$stateName"/>&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                            &lt;tr&gt;
                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;dt&lt;/b&gt;
                                &lt;/td&gt;

                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;tau(v)&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                        &lt;/table&gt;&lt;/b&gt;

                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>

            <xsl:for-each select='*'> <!-- alpha or beta or tau, ...-->

                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">&amp;nbsp;&amp;nbsp; &lt;b&gt;<xsl:value-of select="name()"/>&lt;/b&gt;</xsl:with-param>
                    <xsl:with-param name="comment"></xsl:with-param>
                </xsl:call-template>

                <xsl:for-each select="meta:notes">
                    <xsl:call-template name="tableRow">
                        <xsl:with-param name="name">Notes</xsl:with-param>
                        <xsl:with-param name="comment">Comment from ChannelML file on this rate equation</xsl:with-param>
                        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="."/>&lt;/b&gt;</xsl:with-param>
                    </xsl:call-template>
                </xsl:for-each>

                <xsl:choose>
                    <xsl:when  test="name()='conc_dependence'">
                        <xsl:call-template name="tableRow">
                            <xsl:with-param name="name">Concentration dependence of gate</xsl:with-param>
                            <xsl:with-param name="comment">The dynamics of this gate are dependent on both the potential difference across the
                            channel, and on the concentration of the substance specified here</xsl:with-param>
                            <xsl:with-param name="value">Name: &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;&lt;br/&gt;
                                Ion: &lt;b&gt;<xsl:value-of select="@ion"/>&lt;/b&gt;&lt;br/&gt;
                                Variable as used in equation below: &lt;b&gt;<xsl:value-of select="@variable_name"/>&lt;/b&gt;&lt;br/&gt;
                                Min concentration: &lt;b&gt;<xsl:value-of select="@min_conc"/>&lt;/b&gt; (required by simulators for table of voltage/conc dependencies)&lt;br/&gt;
                                Max concentration: &lt;b&gt;<xsl:value-of select="@max_conc"/>&lt;/b&gt; (required by simulators for table of voltage/conc dependencies)
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    
                    <xsl:when  test="name()='initialisation'">
                        <!-- Ignore for now... -->
                    </xsl:when>
                    
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when  test="count(cml:parameterised_hh) &gt; 0">
                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Form of rate equation for &lt;b&gt;<xsl:value-of select="name()"/>&lt;/b&gt;</xsl:with-param>
                                    <xsl:with-param name="value">&lt;b&gt;Parameterised HH&lt;/b&gt;</xsl:with-param>
                                </xsl:call-template>

                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Expression</xsl:with-param>
                                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="name()"/>(v) = <xsl:choose>
                                        <xsl:when test="cml:parameterised_hh/@type='linoid'" >A*(k*(v-d)) / (1 - exp(-(k*(v-d))))</xsl:when>
                                        <xsl:when test="cml:parameterised_hh/@type='exponential'" >A*exp(k*(v-d))</xsl:when>
                                        <xsl:when test="cml:parameterised_hh/@type='sigmoid'" >A / (1 + exp(k*(v-d)))</xsl:when>
                                        <xsl:otherwise >Unsupported expression type!</xsl:otherwise>
                                        
                                    </xsl:choose>&lt;/b&gt; &amp;nbsp;&amp;nbsp; &lt;i&gt;(<xsl:value-of select="cml:parameterised_hh/@type" />)&lt;/i&gt;</xsl:with-param>
                                </xsl:call-template>

                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Parameter values</xsl:with-param>
                                    <xsl:with-param name="value">
                                        <xsl:for-each select="cml:parameterised_hh/cml:parameter">
                                            &lt;b&gt;<xsl:value-of select="@name"/> = <xsl:value-of select="@value"/>
                                            <xsl:if test="@name='A'"> <xsl:call-template name="getUnits">
                                            <xsl:with-param name="quantity">InvTime</xsl:with-param></xsl:call-template></xsl:if>
                                            <xsl:if test="@name='k'"> <xsl:call-template name="getUnits">
                                            <xsl:with-param name="quantity">InvVoltage</xsl:with-param></xsl:call-template></xsl:if>
                                            <xsl:if test="@name='d'"> <xsl:call-template name="getUnits">
                                            <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template></xsl:if> &lt;/b&gt;
                                            <xsl:if test="count(meta:notes) &gt; 0"><xsl:text>   </xsl:text>(<xsl:value-of select="meta:notes"/>)</xsl:if>

                                            &lt;br/&gt;
                                        </xsl:for-each>
                                    </xsl:with-param>
                                </xsl:call-template>


                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Substituted</xsl:with-param>
                                    <xsl:with-param name="value"><xsl:choose>

                                        <xsl:when test="cml:parameterised_hh/@type = 'exponential'">
                                            &lt;b&gt;<xsl:value-of select="name()"/>(v) =
                                            <xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='A']/@value"/> * e&lt;sup&gt;
                                            <xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='k']/@value"/> *(
                                            v - (<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='d']/@value"/>))&lt;/sup&gt;</xsl:when>

                                        <xsl:when test="cml:parameterised_hh/@type = 'sigmoid'">
                                            &lt;table border="0"&gt;
                                                &lt;tr&gt;
                                                    &lt;td  rowspan="2" valign="center"&gt;
                                                        &lt;b&gt;<xsl:value-of select="name()"/>(v) =&lt;/b&gt;
                                                    &lt;/td&gt;
                                                    &lt;td align="center"&gt;
                                                        &lt;b&gt;<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='A']/@value"/>&lt;/b&gt;
                                                    &lt;/td&gt;
                                                &lt;tr&gt;
                                                    &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                                        &lt;b&gt;1+ e&lt;sup&gt; <xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='k']/@value"/> * (
                                                        v - (<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='d']/@value"/>))&lt;/sup&gt;&lt;/b&gt;
                                                    &lt;/td&gt;
                                                &lt;/tr&gt;
                                            &lt;/table&gt;
                                        </xsl:when>

                                        <xsl:when test="cml:parameterised_hh/@type = 'linoid'">
                                            &lt;table border="0"&gt;
                                                &lt;tr&gt;
                                                    &lt;td  rowspan="2" valign="center"&gt;
                                                        &lt;b&gt;<xsl:value-of select="name()"/>(v) =&lt;/b&gt;
                                                    &lt;/td&gt;
                                                    &lt;td align="center"&gt;
                                                        &lt;b&gt;<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='A']/@value"/> *
                                                        <xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='k']/@value"/> * (
                                                        v - (<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='d']/@value"/>))&lt;/b&gt;
                                                    &lt;/td&gt;
                                                &lt;tr&gt;
                                                    &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                                        &lt;b&gt;1- e&lt;sup&gt; -(<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='k']/@value"/> * (
                                                        v - (<xsl:value-of select="cml:parameterised_hh/cml:parameter[@name='d']/@value"/>)))&lt;/sup&gt;&lt;/b&gt;
                                                    &lt;/td&gt;
                                                &lt;/tr&gt;
                                            &lt;/table&gt;
                                        </xsl:when>

                                        <xsl:otherwise>???</xsl:otherwise>

                                    </xsl:choose>&lt;/b&gt;</xsl:with-param>
                                </xsl:call-template>


                            </xsl:when>


                            <xsl:when  test="count(cml:generic_equation_hh) &gt; 0 or count(cml:generic) &gt; 0">
                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Form of rate equation for <xsl:value-of select="name()"/></xsl:with-param>
                                    <xsl:with-param name="value">&lt;b&gt;Generic equation&lt;/b&gt;</xsl:with-param>
                                </xsl:call-template>
                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Expression</xsl:with-param>
                                    <xsl:with-param name="value">  &lt;b&gt;
                                        <xsl:call-template name="formatExpression">
                                            <xsl:with-param name="variable">
                                                <xsl:value-of select="name()"/>
                                            </xsl:with-param>
                                            <xsl:with-param name="oldExpression">
                                                <xsl:value-of select="cml:generic_equation_hh/@expr" /><xsl:value-of select="cml:generic/@expr" /><!--Will be one or the other-->
                                            </xsl:with-param>
                                        </xsl:call-template>&lt;/b&gt;
                                    </xsl:with-param>
                                </xsl:call-template>

                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="tableRow">
                                    <xsl:with-param name="name">Form of rate equation</xsl:with-param>
                                    <xsl:with-param name="value">&lt;b&gt;Undetermined&lt;/b&gt;</xsl:with-param>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <xsl:if test="count(cml:alpha) &gt; 0 and
                    count(cml:beta) &gt; 0 and count(cml:tau) = 0">

                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">&amp;nbsp;&amp;nbsp; &lt;b&gt;tau&lt;/b&gt;</xsl:with-param>
                    <xsl:with-param name="comment"></xsl:with-param>
                </xsl:call-template>


                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Expression for tau</xsl:with-param>
                    <xsl:with-param name="comment"></xsl:with-param>
                    <xsl:with-param name="value">

                        &lt;table border="0"&gt;
                            &lt;tr&gt;

                                &lt;td rowspan="2" valign="center"&gt;
                                    &lt;b&gt;tau(v) = &lt;/b&gt;
                                &lt;/td&gt;

                                 &lt;td  align="center"&gt;
                                    &lt;b&gt;1&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                            &lt;tr&gt;

                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;alpha(v) + beta(v)&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                        &lt;/table&gt;

                        &lt;/b&gt;
                    </xsl:with-param>

                </xsl:call-template>

            </xsl:if>

            <xsl:if test="count(cml:alpha) &gt; 0 and
                    count(cml:beta) &gt; 0 and count(cml:inf) = 0">

                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">&amp;nbsp;&amp;nbsp; &lt;b&gt;inf&lt;/b&gt;</xsl:with-param>
                    <xsl:with-param name="comment"></xsl:with-param>
                </xsl:call-template>


                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Expression for inf</xsl:with-param>
                    <xsl:with-param name="comment"></xsl:with-param>
                    <xsl:with-param name="value">

                        &lt;table border="0"&gt;
                            &lt;tr&gt;

                                &lt;td rowspan="2" valign="center"&gt;
                                    &lt;b&gt;inf(v) = &lt;/b&gt;
                                &lt;/td&gt;

                                 &lt;td  align="center"&gt;
                                    &lt;b&gt;alpha(v)&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                            &lt;tr&gt;

                                &lt;td align="center" style="border-top:solid 1px black;"&gt;
                                    &lt;b&gt;alpha(v) + beta(v)&lt;/b&gt;
                                &lt;/td&gt;

                            &lt;/tr&gt;
                        &lt;/table&gt;

                        &lt;/b&gt;
                    </xsl:with-param>
                </xsl:call-template>

            </xsl:if>


</xsl:template>



<xsl:template match="cml:ion">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Ion: &lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">One of the ions involved in this channel. Note that the reversal potential used here is a typical value, it should be determined for each cell type based on ionic concentrations</xsl:with-param>
        <xsl:with-param name="value">Charge: &lt;b&gt;<xsl:value-of select="@charge"/>&lt;/b&gt;
            <xsl:if test="count(@default_erev) &gt; 0">
                &lt;br/&gt;Default reversal potential: &lt;b&gt;<xsl:value-of select="@default_erev"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;
            </xsl:if>
            <xsl:if test="count(@role) &gt; 0">
                &lt;br/&gt;Role of ion in process: &lt;b&gt;<xsl:value-of select="@role"/>&lt;/b&gt;
            </xsl:if>
            <xsl:if test="count(meta:notes) &gt; 0">
                &lt;br/&gt;&lt;br/&gt;&lt;span style="color:#a0a0a0;font-style: italic;font-size: 90%"&gt;<xsl:value-of select="meta:notes"/>&lt;/span&gt;
            </xsl:if>

        </xsl:with-param>
    </xsl:call-template>
</xsl:template>

<xsl:template match="cml:ion_concentration">
<h3>Ion concentration: <xsl:value-of select="@name"/></h3>


    <table frame="box" rules="all" align="centre" cellpadding="4">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Name</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
        
                

<xsl:apply-templates select="cml:status"/> 

        
        <xsl:if test="count(meta:notes) &gt; 0">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Description</xsl:with-param>
                <xsl:with-param name="comment">As described in ChannelML file</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
        </xsl:if>


<xsl:apply-templates select="meta:authorList"/>

        <xsl:apply-templates select="meta:publication"/>

        <xsl:apply-templates select="meta:neuronDBref"/>
        <xsl:apply-templates select="meta:modelDBref"/>


        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Ion species</xsl:with-param>
                <xsl:with-param name="comment">The type of ion whose concentration will be altered</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:ion_species"/><xsl:value-of select="cml:ion_species/@name"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>

        <xsl:for-each select="cml:decaying_pool_model">
            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">  Dynamic model: &lt;b&gt;<xsl:value-of select="name()"/>&lt;/b&gt;</xsl:with-param>
                <xsl:with-param name="comment">The model underlying the mechanism altering the ion concentration</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Resting concentration</xsl:with-param>
                <xsl:with-param name="comment">The base level concentration of the ion</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:resting_conc"/><xsl:value-of select="@resting_conc"/> <!-- Either element or attr will be present...-->
                    <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Concentration</xsl:with-param></xsl:call-template>&lt;/b&gt;
                </xsl:with-param>
            </xsl:call-template>
            
            
            <xsl:choose>
                <xsl:when test="count(cml:decay_constant) &gt; 0 or count(@decay_constant) &gt; 0">
                    <xsl:call-template name="tableRow">
                        <xsl:with-param name="name">Decay Constant</xsl:with-param>
                        <xsl:with-param name="comment">The rate of decay (tau) of the concentration back to resting level</xsl:with-param>
                        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:decay_constant"/><xsl:value-of select="@decay_constant"/> <!-- Either element or attr will be present...-->
                            <xsl:call-template name="getUnits">
                            <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="count(cml:inv_decay_constant) &gt; 0 or count(@inv_decay_constant) &gt; 0">
                    <xsl:call-template name="tableRow">
                        <xsl:with-param name="name">Inverse Decay Constant</xsl:with-param>
                        <xsl:with-param name="comment">The reciprocal of the rate of decay of the concentration back to resting level</xsl:with-param>
                        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:inv_decay_constant"/><xsl:value-of select="@inv_decay_constant"/>  <!-- Either element or attr will be present...-->
                            <xsl:call-template name="getUnits">
                            <xsl:with-param name="quantity">InvTime</xsl:with-param></xsl:call-template>&lt;/b&gt;
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
            
            
            
            
            <xsl:if test="count(cml:ceiling) &gt; 0 or count(@ceiling) &gt; 0">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Concentration ceiling</xsl:with-param>
                    <xsl:with-param name="comment">The maximum concentration which the ion pool should be allowed get to.</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:ceiling"/><xsl:value-of select="@ceiling"/> <!-- Either element or attr will be present...-->
                        <xsl:call-template name="getUnits">
                            <xsl:with-param name="quantity">Concentration</xsl:with-param>
                        </xsl:call-template>&lt;/b&gt;
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            
            
            <xsl:if test="count(cml:pool_volume_info) &gt; 0">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Shell Thickness</xsl:with-param>
                    <xsl:with-param name="comment">The thickness of a shell under the cell membrane where all the change in ion concentration is assumed to take place</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:pool_volume_info/cml:shell_thickness"/><xsl:value-of select="cml:pool_volume_info/@shell_thickness"/> 
                    <xsl:call-template name="getUnits">
                        <xsl:with-param name="quantity">Length</xsl:with-param></xsl:call-template>&lt;/b&gt;
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            
            
            <xsl:if test="count(cml:fixed_pool_info) &gt; 0">
                <xsl:call-template name="tableRow">
                    <xsl:with-param name="name">Pool info</xsl:with-param>
                    <xsl:with-param name="comment">In this case the parameter (phi) which determines how quickly the internal pool 'fills' is given as a fixed 
                    value. Note this is not an ideal way to express this value, but needed to be included as this was the parameter which was all 
                    that was present in a number of models, e.g. Traub et al. 2003 Layer 2/3 cell. The dC/dt will be calculated from dC/dt = 
                    - phi * Ica + [Ca]/decay_constant. See mod/GENESIS impl for more details</xsl:with-param>
                    <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="cml:fixed_pool_info/cml:phi"/> &lt;/b&gt;
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:if>
            
        </xsl:for-each>
    </table>
</xsl:template>





<xsl:template match="cml:synapse_type">
<xsl:element name="a">
    <xsl:attribute name="name">Synapse_<xsl:value-of select="@name"/></xsl:attribute>
</xsl:element><h3>Synapse: <xsl:value-of select="@name"/></h3>

    <table frame="box" rules="all" align="centre" cellpadding="4">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Name</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@name"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
        
        

<xsl:apply-templates select="cml:status"/> 

        
        
        <xsl:if test="count(meta:notes) &gt; 0">
        <xsl:call-template name="tableRow">
                <xsl:with-param name="name">Description</xsl:with-param>
                <xsl:with-param name="comment">As described in ChannelML file</xsl:with-param>
                <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="meta:notes"/>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
        </xsl:if>

<xsl:apply-templates select="meta:authorList"/>

        <xsl:apply-templates select="meta:publication"/>

        <xsl:apply-templates select="meta:neuronDBref"/>
        <xsl:apply-templates select="meta:modelDBref"/>

        <xsl:apply-templates select="cml:electrical_syn"/>
        <xsl:apply-templates select="cml:doub_exp_syn"/>
        <xsl:apply-templates select="cml:blocking_syn"/>
        <xsl:apply-templates select="cml:multi_decay_syn"/>
        <xsl:apply-templates select="cml:fac_dep_syn"/>
        <xsl:apply-templates select="cml:stdp_syn"/>
    </table>

</xsl:template>



<xsl:template match="cml:electrical_syn">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Synaptic Mechanism Model: &lt;b&gt;Electrical Synapse&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The model underlying the synaptic mechanism</xsl:with-param>
    </xsl:call-template>
    

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Conductance</xsl:with-param>
        <xsl:with-param name="comment">The conductance of the electrical connection</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@conductance"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    
    </xsl:template>

<xsl:template match="cml:doub_exp_syn">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Synaptic Mechanism Model: &lt;b&gt;Double Exponential Synapse&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The model underlying the synaptic mechanism</xsl:with-param>
    </xsl:call-template>
    
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Expression for conductance</xsl:with-param>
        <xsl:with-param name="value">
    
    &lt;table border="0"&gt;
                &lt;tr&gt;
                    &lt;td  colspan="2" valign="center"&gt;
                        &lt;b&gt;G(t) = max_conductance * A * ( e&lt;sup&gt;-t/decay_time&lt;/sup&gt; - e&lt;sup&gt;-t/rise_time&lt;/sup&gt; ) &amp;nbsp;&amp;nbsp; for t >= 0  &lt;/b&gt;
                    &lt;/td&gt;
             
                    
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" &gt;&amp;nbsp;&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td colspan="2" &gt;where the normalisation factor is:&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  rowspan="2" valign="center"&gt;
                        &lt;b&gt;A =&lt;/b&gt;
                    &lt;/td&gt;
                    &lt;td align="center"&gt;
                        &lt;b&gt;1&lt;/b&gt;
                    &lt;/td&gt;
                &lt;/tr&gt;
                &lt;tr&gt;
                    &lt;td align="center" style="border-top:solid 1px black;"&gt;
                        &lt;b&gt; e&lt;sup&gt;-peak_time / decay_time&lt;/sup&gt; - e&lt;sup&gt; -peak_time / rise_time&lt;/sup&gt;  &lt;/sup&gt;&lt;/b&gt;
                    &lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" &gt;and the time to reach max conductance is:&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  valign="center"&gt;
                        &lt;b&gt;peak_time =&lt;/b&gt;
                    &lt;/td&gt;
                    &lt;td align="center"&gt;
                        &lt;b&gt;&lt;sup&gt;(decay_time * rise_time)&lt;/sup&gt;/&lt;sub&gt;(decay_time - rise_time)&lt;/sub&gt; * ln(&lt;sup&gt;decay_time&lt;/sup&gt;/&lt;sub&gt;rise_time&lt;/sub&gt;)&lt;/b&gt;
                    &lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" &gt;&amp;nbsp;&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" &gt;Note that if rise_time = 0 this simplifies to a &lt;u&gt;single exponential synapse&lt;/u&gt;:&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" valign="center"&gt;
                        &lt;b&gt;G(t) = max_conductance * e&lt;sup&gt;-t/decay_time&lt;/sup&gt; &amp;nbsp;&amp;nbsp;  for t >= 0    &lt;/b&gt;
                    &lt;/td&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" &gt;&amp;nbsp;&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" &gt;Note also if decay_time = rise_time = alpha_time, the waveform is for an &lt;u&gt;alpha synapse&lt;/u&gt; with peak at alpha_time:&lt;/td&gt;
                &lt;/tr&gt;
                
                &lt;tr&gt;
                    &lt;td  colspan="2" valign="center"&gt;
                        &lt;b&gt;G(t) = max_conductance * (&lt;sup&gt;t&lt;/sup&gt;/&lt;sub&gt;alpha_time&lt;/sub&gt;) * e&lt;sup&gt;( 1 - t/alpha_time)&lt;/sup&gt; &amp;nbsp;&amp;nbsp;  for t >= 0    &lt;/b&gt;
                    &lt;/td&gt;
             
                    
                &lt;/tr&gt;
                
            &lt;/table&gt;
    
    
    </xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Maximum conductance</xsl:with-param>
        <xsl:with-param name="comment">The peak conductance which the synapse will reach</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@max_conductance"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Rise time</xsl:with-param>
        <xsl:with-param name="comment">Characteristic time (tau) over which the double exponential synaptic conductance rises</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@rise_time"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Decay time</xsl:with-param>
        <xsl:with-param name="comment">Characteristic time (tau) over which the double exponential synaptic conductance decays</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@decay_time"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Reversal potential</xsl:with-param>
        <xsl:with-param name="comment">The effective reversal potential for the ion flow through the synapse when the conductance is non zero</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@reversal_potential"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
</xsl:template>



<xsl:template match="@max_conductance">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Maximum conductance</xsl:with-param>
        <xsl:with-param name="comment">The peak conductance which the synapse will reach</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="."/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
</xsl:template>

<xsl:template match="@rise_time">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Rise time</xsl:with-param>
        <xsl:with-param name="comment">Characteristic time (tau) over which the double exponential synaptic conductance rises</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="."/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
</xsl:template>

<xsl:template match="@decay_time">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Decay time</xsl:with-param>
        <xsl:with-param name="comment">Characteristic time (tau) over which the double exponential synaptic conductance decays</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="."/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
</xsl:template>

<xsl:template match="@reversal_potential">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Reversal potential</xsl:with-param>
        <xsl:with-param name="comment">The effective reversal potential for the ion flow through the synapse when the conductance is non zero</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="."/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
</xsl:template>



<xsl:template match="cml:blocking_syn">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Synaptic Mechanism Model: &lt;b&gt;Blocking Synapse&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The model underlying the synaptic mechanism</xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="@max_conductance"/>
    <xsl:apply-templates select="@rise_time"/>
    <xsl:apply-templates select="@decay_time"/>
    <xsl:apply-templates select="@reversal_potential"/>

    <xsl:apply-templates select="cml:block"/>


</xsl:template>

<xsl:template match="cml:fac_dep_syn">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Synaptic Mechanism Model: &lt;b&gt;Facilitating and Depressing Synapse&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The model underlying the synaptic mechanism</xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="@max_conductance"/>
    <xsl:apply-templates select="@rise_time"/>
    <xsl:apply-templates select="@decay_time"/>
    <xsl:apply-templates select="@reversal_potential"/>

    <xsl:apply-templates select="cml:plasticity"/>

</xsl:template>


<xsl:template match="cml:plasticity">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Plasticity element of synaptic model</xsl:with-param>
        <xsl:with-param name="comment">How the magnitude of the response changes due to plasticity</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Initial release probability</xsl:with-param>
        <xsl:with-param name="comment">A factor which scales the conductance waveform following the first spiking event</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@init_release_prob"/> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Recovery time constant</xsl:with-param>
        <xsl:with-param name="comment">The time constant for the recovery of synaptic resources following a spike</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@tau_rec"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Facilitation time constant</xsl:with-param>
        <xsl:with-param name="comment">The time constant for facilitation of synaptic strength</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@tau_fac"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

</xsl:template>

<xsl:template match="cml:stdp_syn">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Synaptic Mechanism Model: &lt;b&gt;Spike Timing Dependent Plasticity&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The model underlying the synaptic mechanism</xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="@max_conductance"/>
    <xsl:apply-templates select="@rise_time"/>
    <xsl:apply-templates select="@decay_time"/>
    <xsl:apply-templates select="@reversal_potential"/>

    <xsl:apply-templates select="cml:spike_time_dep"/>

</xsl:template>

<!--<spike_time_dep tau_ltp="20" del_weight_ltp="0.1" tau_ltd="20" del_weight_ltd="0.105" max_syn_weight="2" post_spike_thresh="-50"/>-->

<xsl:template match="cml:spike_time_dep">

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Plasticity element of synaptic model</xsl:with-param>
        <xsl:with-param name="comment">How the magnitude of the response changes due to spike times</xsl:with-param>
    </xsl:call-template>


    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Time constant LTP</xsl:with-param>
        <xsl:with-param name="comment">The time constant for potentiation of synaptic strength when a postsynaptic spike follows a pre</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@tau_ltp"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Weight change LTP</xsl:with-param>
        <xsl:with-param name="comment">The change in weight (as a fraction of maximum conductance above) when a postsynaptic spike instantaneously
        follows a pre. Note the magnitude of this change will decay with time constant given above.</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@del_weight_ltp"/> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Time constant LTD</xsl:with-param>
        <xsl:with-param name="comment">The time constant for depression of synaptic strength when a presynaptic spike follows a post</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@tau_ltd"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Weight change LTD</xsl:with-param>
        <xsl:with-param name="comment">The change in weight (as a fraction of maximum conductance above) when a presynaptic spike instantaneously
        follows a post. Note the magnitude of this change will decay with time constant given above.</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@del_weight_ltd"/> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Maximum synaptic weight</xsl:with-param>
        <xsl:with-param name="comment">The maximum weight to which the synapse can increase, as a multiple of maximum conductance above</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@max_syn_weight"/> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Post synaptic threshold</xsl:with-param>
        <xsl:with-param name="comment">The membrane potential of the post synaptic cell which should be considered a spike</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@post_spike_thresh"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>




</xsl:template>

<xsl:template match="cml:block">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Block of synaptic mechanism</xsl:with-param>
        <xsl:with-param name="comment">Information on how a species modifies the conductance of the synapse</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Substance</xsl:with-param>
        <xsl:with-param name="comment">Ion or molecule which blocks the channel</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@species"/> &lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Concentration</xsl:with-param>
        <xsl:with-param name="comment">Concentration of species</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@conc"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Concentration</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>


    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">eta</xsl:with-param>

        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@eta"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">InvConcentration</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>


    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">gamma</xsl:with-param>

        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@gamma"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">InvVoltage</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Expression defining block</xsl:with-param>

        <xsl:with-param name="value">
            &lt;table border="0"&gt;
                &lt;tr&gt;
                    &lt;td  rowspan="2" valign="center"&gt;
                        &lt;b&gt;G&lt;sub&gt;block&lt;/sub&gt;(v) =&lt;/b&gt;
                    &lt;/td&gt;
                    &lt;td align="center"&gt;
                        &lt;b&gt;1&lt;/b&gt;
                    &lt;/td&gt;
                &lt;tr&gt;
                    &lt;td align="center" style="border-top:solid 1px black;"&gt;
                        &lt;b&gt;1+ eta * [<xsl:value-of select="@species"/>] * e&lt;sup&gt;(-1 * gamma * v)&lt;/sup&gt;&lt;/b&gt;
                    &lt;/td&gt;
                &lt;/tr&gt;
            &lt;/table&gt;
        </xsl:with-param>
    </xsl:call-template>


</xsl:template>





<xsl:template match="cml:multi_decay_syn">
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Synaptic Mechanism Model: &lt;b&gt;Multi decay time course Synapse&lt;/b&gt;</xsl:with-param>
        <xsl:with-param name="comment">The model underlying the synaptic mechanism</xsl:with-param>
    </xsl:call-template>


    <xsl:apply-templates select="@max_conductance"/>

    <xsl:apply-templates select="@rise_time"/>
    <xsl:apply-templates select="@decay_time"/>

    

    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Maximum conductance 2</xsl:with-param>
        <xsl:with-param name="comment">The peak value of the 2nd (usually slower) component of the synaptic conductance</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@max_conductance_2"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    
    <xsl:call-template name="tableRow">
        <xsl:with-param name="name">Decay time 2</xsl:with-param>
        <xsl:with-param name="comment">Second characteristic time (tau) over which the 2nd (usually slower) component of the synaptic conductance decays</xsl:with-param>
        <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@decay_time_2"/> <xsl:call-template name="getUnits">
                    <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
    </xsl:call-template>
    
    
    
    <xsl:if test="count(@max_conductance_3) &gt; 0">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Maximum conductance 3</xsl:with-param>
            <xsl:with-param name="comment">The peak value of the 3rd (usually quite slow) component of the synaptic conductance</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@max_conductance_3"/> <xsl:call-template name="getUnits">
                        <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
    </xsl:if>
    
    <xsl:if test="count(@decay_time_3) &gt; 0">
        <xsl:call-template name="tableRow">
            <xsl:with-param name="name">Decay time 3</xsl:with-param>
            <xsl:with-param name="comment">Third characteristic time (tau) over which the 2nd (usually quite slow) component of the synaptic conductance decays</xsl:with-param>
            <xsl:with-param name="value">&lt;b&gt;<xsl:value-of select="@decay_time_3"/> <xsl:call-template name="getUnits">
                        <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template>&lt;/b&gt;</xsl:with-param>
        </xsl:call-template>
    </xsl:if>
        
    <xsl:apply-templates select="@reversal_potential"/>


</xsl:template>









</xsl:stylesheet>
