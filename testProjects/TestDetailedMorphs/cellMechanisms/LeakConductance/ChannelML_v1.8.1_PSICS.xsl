<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:meta="http://morphml.org/metadata/schema"
    xmlns:cml="http://morphml.org/channelml/schema"
    exclude-result-prefixes="meta cml">
    
<!--
    This file is used to convert ChannelML files to PSICS script files
    
    Funding for this work has been received from the Medical Research Council and the 
    Wellcome Trust. 
    
    Author: Padraig Gleeson, Robert Cannon
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

<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" cdata-section-elements="TauInfCodedTransition"/>

<xsl:variable name="includeComments">0</xsl:variable>

<xsl:variable name="singleChannelCond_pS">30</xsl:variable> <!-- Note this will get overwritten by the value set in the GUI of neuroConstruct-->

<xsl:variable name="physiolunits">
     <xsl:choose>
        <xsl:when test="/cml:channelml/@units = 'Physiological Units'">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<xsl:variable name="vunits">
    <xsl:choose>
        <xsl:when test="$physiolunits = 'true'">mV</xsl:when>
        <xsl:otherwise>V</xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<xsl:variable name="rateunits">
     <xsl:choose>
        <xsl:when test="$physiolunits = 'true'">per_ms</xsl:when>
        <xsl:otherwise>per_s</xsl:otherwise>
    </xsl:choose>
</xsl:variable>


<!--Main template-->

<xsl:template match="/cml:channelml">
    <xsl:if test="$includeComments='1'">
        <xsl:comment>This is a PSICS channel model file generated from a ChannelML v1.8.1 file.</xsl:comment>
    </xsl:if>
 
    <xsl:if test="count(meta:notes) &gt; 0 and $includeComments='1'">
        <xsl:comment>
            <xsl:value-of select="meta:notes"/>
        </xsl:comment>
    </xsl:if>


    <xsl:choose>
        <xsl:when test="count(cml:channel_type/cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">
            <xsl:comment>Error: this channel contains an Integrate and Fire mechanisms that cannot be mapped to PSICS.</xsl:comment>
        </xsl:when>

        <xsl:otherwise>
            <xsl:apply-templates  select="cml:ion_concentration"/>
            <xsl:apply-templates  select="cml:channel_type"/>
            <xsl:apply-templates  select="cml:synapse_type"/>
        </xsl:otherwise>
    </xsl:choose>

</xsl:template>
<!--End Main template-->

<xsl:template match="cml:ion_concentration">
    
        Error: Ion concentration pools not yet supported in PSICS!  
</xsl:template>

<xsl:template match="cml:synapse_type">
    
        Error: Synapses not yet supported in PSICS!  
</xsl:template>

<xsl:template match="cml:channel_type">
    <xsl:choose>
        <xsl:when test="count(//cml:voltage_conc_gate) &gt; 0">
            <xsl:comment>Error: the channel has a voltage_conc gate that is not supported in PSICS</xsl:comment>
        </xsl:when>
        <xsl:when test="count(//cml:conc_dependence) &gt; 0">
            <xsl:comment>Error: the channel has a conc dependent gate that is not supported in PSICS</xsl:comment>
        </xsl:when>
        <xsl:otherwise></xsl:otherwise>
    </xsl:choose>

    <xsl:apply-templates select="cml:current_voltage_relation">
        <xsl:with-param name="name" select="@name"/>
    </xsl:apply-templates>
</xsl:template>


<xsl:template match="cml:current_voltage_relation">
    
    <xsl:param name="name" select="missing_name"/>
    <KSChannel id="{$name}" permeantIon ="{@ion}" gSingle="{$singleChannelCond_pS}pS">
        
        <!--<xsl:if test="count(cml:q10_settings) &gt; 0">
            
            Error!! q10_settings not yet supported in PSICS!
            
        </xsl:if>-->
        <xsl:apply-templates select="cml:gate"/>
        <xsl:if test="count(cml:gate) = 0">
            <OpenState id="o1"/>
        </xsl:if>
    </KSChannel>
</xsl:template>


<xsl:template match="cml:gate">
    <xsl:variable name="gate_name" select="@name"/>
    <KSComplex instances="{@instances}">
        <xsl:variable name="open_state_id" select="cml:open_state/@id"/>
        <xsl:apply-templates select="cml:closed_state"/>
        <xsl:apply-templates select="cml:open_state"/>

        <!-- At the moment, the policy is if there are only alpha/beta in exp/explinear/sigmoid format,
            and no q10 settings, use the standard elements (ExpLinearTransition, etc) to define these. In all other cases,
            explicitly write out alpha, beta, etc. -->

        <xsl:choose>
            <xsl:when test="count(cml:transition[@expr_form='generic']) = 0 and
                        count(cml:time_course[@expr_form='generic']) = 0 and
                        count(cml:steady_state[@expr_form='generic']) = 0 and
                        count(../cml:q10_settings) = 0">
                <xsl:apply-templates select="cml:transition"/>
            </xsl:when>
            <xsl:otherwise>
                
            <xsl:variable name="offset_part">
                <xsl:if test="count(../cml:offset) &gt; 0">
     double offset = <xsl:value-of select="../cml:offset/@value"/>;
     v = v + offset;</xsl:if>
            </xsl:variable>

            <xsl:variable name="q10_part">
                <xsl:choose>
                    <xsl:when test="count(../cml:q10_settings) = 0">
    double temp_adj_<xsl:value-of select="$gate_name"/> = 1;
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:for-each select="../cml:q10_settings">
                            <xsl:if test="@gate=$gate_name or count(@gate) = 0">
                                <xsl:choose>
                                    <xsl:when test="count(@q10_factor) &gt; 0">
    double temp_adj_<xsl:value-of select="$gate_name"/> = Math.pow(<xsl:value-of select="@q10_factor" />, (temperature - <xsl:value-of select="@experimental_temp"/>)/10);
                                    </xsl:when>
                                    <xsl:when test="count(@fixed_q10) &gt; 0">
    double temp_adj_<xsl:value-of select="$gate_name"/> = <xsl:value-of select="@fixed_q10" />;
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>


            <xsl:variable name="alpha_beta_part">
                <xsl:for-each select="cml:transition"><!--[@to='$open_state_id']-->
                    <xsl:choose>
                        <xsl:when test="@expr_form='generic'">
    double <xsl:value-of select="@name"/> = 0;
                            <xsl:call-template name="formatExpression">
                                <xsl:with-param name="variable"><xsl:value-of select="@name"/></xsl:with-param>
                                <xsl:with-param name="expr" select="@expr" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="generateStandardEqn">
                                <xsl:with-param name="name" select="@name"/>
                                <xsl:with-param name="functionForm" select="@expr_form" />
                                <xsl:with-param name="rate" select="@rate"/>
                                <xsl:with-param name="scale" select="@scale"/>
                                <xsl:with-param name="midpoint" select="@midpoint"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>

            <xsl:variable name="tau_part">
                <xsl:choose>
                    <xsl:when test="count(cml:time_course[@expr_form='generic'])&gt;0">
                        <xsl:call-template name="formatExpression">
                            <xsl:with-param name="variable"><xsl:value-of select="$gate_name"/>tau</xsl:with-param>
                            <xsl:with-param name="expr" select="cml:time_course/@expr" />
                        </xsl:call-template></xsl:when>
                    <xsl:when test="count(cml:time_course[@expr_form!='generic'])&gt;0">
                            <xsl:call-template name="generateStandardEqn">
                                <xsl:with-param name="name"><xsl:value-of select="$gate_name"/>tau</xsl:with-param>
                                <xsl:with-param name="functionForm" select="cml:time_course/@expr_form" />
                                <xsl:with-param name="rate" select="cml:time_course/@rate"/>
                                <xsl:with-param name="scale" select="cml:time_course/@scale"/>
                                <xsl:with-param name="midpoint" select="cml:time_course/@midpoint"/>
                            </xsl:call-template></xsl:when>
                    <xsl:otherwise>
    <xsl:value-of select="$gate_name"/>tau = 1/(alpha + beta);
    </xsl:otherwise>
                </xsl:choose>
    <xsl:value-of select="$gate_name"/>tau = <xsl:value-of select="$gate_name"/>tau/temp_adj_<xsl:value-of select="$gate_name"/>;
            </xsl:variable>

            <xsl:variable name="inf_part">
                <xsl:choose>
                    <xsl:when test="count(cml:steady_state[@expr_form='generic'])&gt;0">
                        <xsl:call-template name="formatExpression">
                            <xsl:with-param name="variable"><xsl:value-of select="$gate_name"/>inf</xsl:with-param>
                            <xsl:with-param name="expr" select="cml:steady_state/@expr" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="count(cml:steady_state[@expr_form!='generic'])&gt;0">
                            <xsl:call-template name="generateStandardEqn">
                                <xsl:with-param name="name"><xsl:value-of select="$gate_name"/>inf</xsl:with-param>
                                <xsl:with-param name="functionForm" select="cml:steady_state/@expr_form" />
                                <xsl:with-param name="rate" select="cml:steady_state/@rate"/>
                                <xsl:with-param name="scale" select="cml:steady_state/@scale"/>
                                <xsl:with-param name="midpoint" select="cml:steady_state/@midpoint"/>
                            </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
    <xsl:value-of select="$gate_name"/>inf = alpha/(alpha + beta);
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="var_defs"><xsl:if test="string-length($alpha_beta_part)>0
                                                        or contains($tau_part, 'midpoint')
                                                        or contains($inf_part, 'midpoint')">
    double rate = 0;
    double scale = 0;
    double midpoint = 0;</xsl:if></xsl:variable>
                          
        <TauInfCodedTransition from="{cml:closed_state/@id}" to="{cml:open_state/@id}" tauvar="{$gate_name}tau" infvar="{$gate_name}inf" ><!--  baseTemperature="1" q10="1.0"-->
            <xsl:for-each select="../../cml:parameters/cml:parameter">
                <Constant id="{@name}" name="{@name}" value="{@value}"/>
            </xsl:for-each>
<xsl:text>
    </xsl:text><xsl:value-of select="$var_defs"/>
<xsl:text>
    </xsl:text><xsl:value-of select="$q10_part"/>
<xsl:text>
    </xsl:text><xsl:value-of select="$offset_part"/>
<xsl:text>
    </xsl:text><xsl:value-of select="$alpha_beta_part"/>
<xsl:text>
    </xsl:text><xsl:value-of select="$tau_part"/>
<xsl:text>
    </xsl:text><xsl:value-of select="$inf_part"/>
<xsl:text>
    </xsl:text>

        </TauInfCodedTransition>
        
            </xsl:otherwise>
        </xsl:choose>
        
    </KSComplex>
</xsl:template>




<xsl:template match="cml:closed_state">
    <ClosedState id="{@id}"/>
</xsl:template>

<xsl:template match="cml:open_state">
    <OpenState id="{@id}"/>
</xsl:template>

<xsl:template match="cml:transition">
    <xsl:choose>
        <xsl:when test="@expr_form = 'exp_linear'">
            <ExpLinearTransition from="{@from}" to="{@to}"
                rate="{@rate}{$rateunits}" scale="{@scale}{$vunits}" midpoint="{@midpoint}{$vunits}"/>
        </xsl:when>
        <xsl:when test="@expr_form = 'exponential'">
            <ExpTransition from="{@from}" to="{@to}"
                rate="{@rate}{$rateunits}" scale="{@scale}{$vunits}" midpoint="{@midpoint}{$vunits}"/>
        </xsl:when>
        <xsl:when test="@expr_form = 'sigmoid'">
            <!-- Note the different use of scale in ChannelML!! -->
            <xsl:variable name="psicsScale" select="number(@scale) * -1"/>
            <SigmoidTransition from="{@from}" to="{@to}"
                rate="{@rate}{$rateunits}" scale="{$psicsScale}{$vunits}" midpoint="{@midpoint}{$vunits}"/>
        </xsl:when>

        <xsl:when test="@expr_form = 'generic'">
            <!-- Note will be parsed in <xsl:template match="cml:gate"> -->
        </xsl:when>

        <xsl:otherwise>
            <xsl:comment>unsupported transition type <xsl:value-of select="@expr_form"/></xsl:comment>
        </xsl:otherwise>


    </xsl:choose>

</xsl:template>

<!-- Found at http://www.dpawson.co.uk/xsl/sect2/replace.html#d9701e43-->
<xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
        <xsl:when test="contains($text,$replace)">
            <xsl:value-of select="substring-before($text,$replace)"/>
            <xsl:value-of select="$with"/>
            <xsl:call-template name="replace-string">
                <xsl:with-param name="text" select="substring-after($text,$replace)"/>
                <xsl:with-param name="replace" select="$replace"/>
                <xsl:with-param name="with" select="$with"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$text"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


<xsl:template name="generateStandardEqn">
    <xsl:param name="name" />
    <xsl:param name="functionForm" />
    <xsl:param name="rate" />
    <xsl:param name="scale" />
    <xsl:param name="midpoint" />

    <xsl:choose>
        <xsl:when test="string($name) = 'alpha' or string($name) = 'beta' or string($name) = 'gamma' or string($name) = 'zeta'">
    double <xsl:value-of select="$name"/> = 0;
    rate = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$rate"/></xsl:with-param>
                    <xsl:with-param name="quantity">InvTime</xsl:with-param>
                </xsl:call-template>;
        </xsl:when>
        <xsl:when test="contains($name,'tau')">
    rate = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$rate"/></xsl:with-param>
                    <xsl:with-param name="quantity">Time</xsl:with-param>
                </xsl:call-template>;
        </xsl:when>
        <xsl:when test="contains($name,'inf')">
    rate = <xsl:value-of select="$rate"/>; <!-- Note: inf value dimensionless-->
        </xsl:when>
        <xsl:otherwise>
    double <xsl:value-of select="$name"/> = 0;
    rate = <xsl:value-of select="$rate"/>; // Warning: unrecognised rate variable! Don't know how to convert units!
        </xsl:otherwise>
    </xsl:choose>
    scale = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$scale"/></xsl:with-param>
                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                </xsl:call-template>;
    midpoint = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$midpoint"/></xsl:with-param>
                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                </xsl:call-template>;<xsl:text>
    </xsl:text>

    <xsl:choose>
        <xsl:when test="$functionForm = 'exponential'">
    <xsl:value-of select="$name"/> = rate * Math.exp((v - midpoint) / scale);
        </xsl:when>
        <xsl:when test="$functionForm = 'sigmoid'">
    <xsl:value-of select="$name"/> = rate / (Math.exp((v - midpoint) / scale) + 1);
        </xsl:when>
            <xsl:when test="$functionForm = 'exp_linear'">
    if ( 1e-6 > (Math.abs((v - midpoint)/ scale))) {
        <xsl:value-of select="$name"/> = rate * (1 + (v - midpoint)/scale/2);
    } else {
        <xsl:value-of select="$name"/> = rate * ((v - midpoint) / scale) /(1 - (Math.exp(-1 * (v - midpoint)/scale)));
    }

        </xsl:when>
    </xsl:choose>

</xsl:template>



<!-- Function to get value converted to proper units.-->
<xsl:template name="convert">
    <xsl:param name="value" />
    <xsl:param name="quantity" />
    <xsl:choose>
        <xsl:when test="$physiolunits  = 'true'"><xsl:value-of select="$value"/></xsl:when>

        <xsl:otherwise>
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"><xsl:value-of select="number($value div 10)"/></xsl:when>
                <xsl:when test="$quantity = 'Conductance'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Voltage'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Time'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Length'"><xsl:value-of select="number($value div 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvTime'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Concentration'"><xsl:value-of select="number($value div 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'Current'"><xsl:value-of select="number($value div 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvCurrent'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="number($value)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:otherwise>

    </xsl:choose>
</xsl:template>

<xsl:template name="formatExpression">
    <xsl:param name="variable" />
    <xsl:param name="expr" />

    <xsl:variable name="expr1">
        <xsl:call-template name="replace-string">
            <xsl:with-param name="text" select="$expr"/>
            <xsl:with-param name="replace">exp</xsl:with-param>
            <xsl:with-param name="with">Math.exp</xsl:with-param>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="expr_fin">
        <xsl:call-template name="replace-string">
            <xsl:with-param name="text" select="$expr1"/>
            <xsl:with-param name="replace">celsius</xsl:with-param>
            <xsl:with-param name="with">temperature</xsl:with-param>
        </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
        <xsl:when test="contains($expr_fin, '?')">
    <!-- Expression contains a condition!!-->
    <xsl:variable name="ifTrue"><xsl:value-of select="substring-before(substring-after($expr_fin,'?'), ':')"/></xsl:variable>
    <xsl:variable name="ifFalse"><xsl:value-of select="substring-after($expr_fin,':')"/></xsl:variable>
    <xsl:variable name="condition"><xsl:value-of select="substring-before($expr_fin,'?')"/></xsl:variable>

    if (<xsl:value-of select="$condition"/>) {<xsl:text>
        </xsl:text><xsl:value-of select="$variable"/> = <xsl:value-of select="$ifTrue"/>;
    } else {<xsl:text>
        </xsl:text><xsl:value-of select="$variable"/> = <xsl:value-of select="$ifFalse"/>;
    }
        </xsl:when>
        <xsl:otherwise>
    <xsl:value-of select="$variable"/> = <xsl:value-of select="$expr_fin"/>;
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


</xsl:stylesheet>