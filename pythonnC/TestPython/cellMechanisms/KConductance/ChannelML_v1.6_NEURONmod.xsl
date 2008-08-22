<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:meta="http://morphml.org/metadata/schema" 
    xmlns:cml="http://morphml.org/channelml/schema">

<!--

    This file is used to convert v1.6 ChannelML files to NEURON mod files

    This file has been developed as part of the neuroConstruct project
    
    Funding for this work has been received from the Medical Research Council
    
    Author: Padraig Gleeson
    Copyright 2007 Department of Physiology, UCL
    
-->

<xsl:output method="text" indent="yes" />

<xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="/cml:channelml/@units"/></xsl:variable>   

<!--Main template-->

<xsl:template match="/cml:channelml">
?  This is a NEURON mod file generated from a v1.6 ChannelML file

?  Unit system of original ChannelML file: <xsl:value-of select="$xmlFileUnitSystem"/><xsl:text>
</xsl:text>

<xsl:if test="count(/cml:channelml/cml:channel_type/cml:ks_gate) &gt; 0">
    *** Note: Kinetic scheme based ChannelML description cannot be mapped in to mod files in this version. ***
    Please use the alternative XSL file which maps on to NEURON's KS Channel Builder format 
    (usually ChannelML_v1.2_NEURONChanBuild.xsl)
    
</xsl:if>
<xsl:if test="count(meta:notes) &gt; 0">
COMMENT
    <xsl:value-of select="meta:notes"/>
ENDCOMMENT
</xsl:if>
<!-- Only do the first channel -->
<xsl:apply-templates  select="cml:channel_type"/>

<!-- Do the ion concentrations if there -->
<xsl:apply-templates  select="cml:ion_concentration"/>

<!-- Do a synapse if there -->
<xsl:apply-templates  select="cml:synapse_type"/>

</xsl:template>
<!--End Main template-->

<xsl:template match="cml:channel_type">
TITLE Channel: <xsl:value-of select="@name"/>

<xsl:if test="count(meta:notes) &gt; 0">

COMMENT
    <xsl:value-of select="meta:notes"/>
ENDCOMMENT
</xsl:if>

UNITS {
    (mA) = (milliamp)
    (mV) = (millivolt)
    (S) = (siemens)
    (um) = (micrometer)
    (molar) = (1/liter)
    (mM) = (millimolar)
    (l) = (liter)
}

<xsl:variable name="nonSpecificCurrent">
    <xsl:choose>
        <xsl:when test="cml:current_voltage_relation/cml:ohmic/@ion='non_specific'">yes</xsl:when>
        <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
</xsl:variable>
<!-- Whether there is a voltage and concentration depemnence in the channel-->
<xsl:variable name="voltConcDependence">
    <xsl:choose>
        <xsl:when test="count(//cml:voltage_conc_gate) &gt; 0">yes</xsl:when>
        <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
</xsl:variable>
    
NEURON {
<xsl:choose>
<xsl:when test="count(cml:current_voltage_relation/cml:ohmic) &gt; 0">  <!-- i.e. normal ohmic channel-->
    SUFFIX <xsl:value-of select="@name"/>
    
    <xsl:for-each select="/cml:channelml/cml:ion[@name!='non_specific']">
        <xsl:choose>
            <xsl:when test ="@role='ModulatingSubstance'">
    USEION <xsl:value-of select="@name"/> READ <xsl:value-of select="@name"/>i VALENCE <xsl:value-of select="@charge"/> ? internal concentration of ion is read
            </xsl:when>
            <xsl:when test ="@role='SignallingSubstance'">
    USEION <xsl:value-of select="@name"/> READ i<xsl:value-of select="@name"/> WRITE <xsl:value-of select="@name"/>i VALENCE <xsl:value-of select="@charge"/> ? outgoing current of ion is read, internal concentration is written
            </xsl:when>
            <xsl:otherwise>
    USEION <xsl:value-of select="@name"/> READ e<xsl:value-of select="@name"/> WRITE i<xsl:value-of select="@name"/> VALENCE <xsl:value-of select="@charge"/> ? reversal potential of ion is read, outgoing current is written
            </xsl:otherwise>
        </xsl:choose>
            
    </xsl:for-each>
    
    <xsl:if test="string($nonSpecificCurrent)='yes'">
    ? A non specific current is present
    RANGE e
    NONSPECIFIC_CURRENT i
    </xsl:if>
    RANGE gmax, gion
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    RANGE <xsl:value-of select="cml:state/@name"/>inf, <xsl:value-of select="cml:state/@name"/>tau
    </xsl:for-each>
</xsl:when>
<xsl:when test="count(cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">  <!-- i.e. I&F-->
    ? Note this implementation is based on that used in the COBA based I and F model as used in Brette et al (2006)
    ? and the NEURON script files from http://senselab.med.yale.edu/SenseLab/ModelDB/ShowModel.asp?model=83319
    
    POINT_PROCESS <xsl:value-of select="@name"/>
    GLOBAL thresh, t_refrac, v_reset, g_refrac
    NONSPECIFIC_CURRENT i
</xsl:when>
</xsl:choose>
}

PARAMETER { 
<xsl:choose>
<xsl:when test="count(cml:current_voltage_relation/cml:ohmic) &gt; 0">  <!-- i.e. normal ohmic channel-->
    gmax = <xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/cml:ohmic/cml:conductance/@default_gmax"/>
            <xsl:with-param name="quantity">Conductance Density</xsl:with-param>
          </xsl:call-template> (S/cm2) 
    <xsl:if test="string($nonSpecificCurrent)='yes'">
    e = <xsl:call-template name="convert">
            <xsl:with-param name="value" select="/cml:channelml/cml:ion[@name='non_specific']/@default_erev"/>
            <xsl:with-param name="quantity">Voltage</xsl:with-param>
            </xsl:call-template> (mV)
    </xsl:if>
</xsl:when>
<xsl:when test="count(cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">  <!-- i.e. I&F-->
    thresh = <xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/cml:integrate_and_fire/@threshold"/>
            <xsl:with-param name="quantity">Voltage</xsl:with-param>
            </xsl:call-template> (mV)
    t_refrac = <xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/cml:integrate_and_fire/@t_refrac"/>
            <xsl:with-param name="quantity">Time</xsl:with-param>
            </xsl:call-template> (ms)
    v_reset = <xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/cml:integrate_and_fire/@v_reset"/>
            <xsl:with-param name="quantity">Voltage</xsl:with-param>
            </xsl:call-template> (mV)
    g_refrac = <xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/cml:integrate_and_fire/@g_refrac"/>
            <xsl:with-param name="quantity">Conductance</xsl:with-param>
            </xsl:call-template> (uS)
</xsl:when>
</xsl:choose>
}



ASSIGNED {
<xsl:choose>
<xsl:when test="count(cml:current_voltage_relation/cml:ohmic) &gt; 0">  <!-- i.e. normal ohmic channel-->
    v (mV)
    <xsl:choose>
        <xsl:when test="string($nonSpecificCurrent)='yes'">    
    i (mA/cm2)
        </xsl:when>
        <xsl:otherwise>
    celsius (degC)
    <xsl:for-each select="/cml:channelml/cml:ion[@name!='non_specific']">
        <xsl:choose>
            <xsl:when test ="@role='ModulatingSubstance'">
    ? The internal concentration of ion: <xsl:value-of select="@name"/> is used in the rate equations...
    <xsl:value-of select="@name"/>i (mM)           
            </xsl:when>
            <xsl:when test ="@role='SignallingSubstance'">
            ? Error!! ion: <xsl:value-of select="@name"/> with role="SignallingSubstance" shouldn't be in a channel_type...
            </xsl:when>
            <xsl:otherwise>
    ? Reversal potential of <xsl:value-of select="@name"/>
    e<xsl:value-of select="@name"/> (mV)
    ? The outward flow of ion: <xsl:value-of select="@name"/> calculated by rate equations...
    i<xsl:value-of select="@name"/> (mA/cm2)
            </xsl:otherwise>
        </xsl:choose>
    </xsl:for-each>
    gion (S/cm2)
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
<xsl:value-of select="cml:state/@name"/>inf<xsl:text>
    </xsl:text><xsl:value-of select="cml:state/@name"/>tau (ms)<xsl:text>
    </xsl:text></xsl:for-each>
        </xsl:otherwise>
        </xsl:choose>
</xsl:when>
<xsl:when test="count(cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">  <!-- i.e. I&F-->
    i (nanoamp)
    v (millivolt)
    g (microsiemens)

</xsl:when>
</xsl:choose>
}

BREAKPOINT {
<xsl:choose>
<xsl:when test="count(cml:current_voltage_relation/cml:ohmic) &gt; 0">  <!-- i.e. normal ohmic channel-->
    <xsl:choose>
        <xsl:when test="string($nonSpecificCurrent)='yes'">
    i = gmax*(v - e) 
        </xsl:when>
        <xsl:otherwise>
    <xsl:choose><xsl:when test="$voltConcDependence='yes'">SOLVE states METHOD derivimplicit</xsl:when> <!-- Needed for concentration dependence-->
    <xsl:when test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate) &gt; 0">SOLVE states METHOD cnexp</xsl:when> <!-- When it's not a nonSpecificCurrent but there are no gates, this statement is not needed-->
    </xsl:choose>
    
    gion = gmax<xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">*((<xsl:if test="count(cml:state/@fraction) &gt; 0">
            <xsl:value-of select="cml:state/@fraction"/>*</xsl:if><xsl:value-of select="cml:state/@name"/>)^<xsl:value-of select="@power"/>)</xsl:for-each>
    
            <xsl:for-each select="/cml:channelml/cml:ion">
                <xsl:if test ="count(@role) = 0 or @role='PermeatedSubstance'">
    i<xsl:value-of select="@name"/> = gion*(v - e<xsl:value-of select="@name"/>)
                </xsl:if>
            </xsl:for-each>
        </xsl:otherwise>
        </xsl:choose>
</xsl:when>
<xsl:when test="count(cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">  <!-- i.e. I&F-->
    i = g*(v - v_reset)
</xsl:when>
</xsl:choose>
}


<xsl:if test="count(cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">

INITIAL {
    net_send(0, 3)
    g = 0
}

NET_RECEIVE(w) {

    if (flag == 1) {
        net_event(t)
        net_send(t_refrac, 2)
        v = v_reset
        g = g_refrac
    }else if (flag == 2) {
        g = 0
    }else if (flag == 3) {
        WATCH (v > thresh) 1
    }	
}   
</xsl:if>
    
<xsl:if test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate) &gt; 0">
INITIAL {
    <xsl:variable name="ionname"><xsl:value-of select="cml:current_voltage_relation/cml:ohmic/@ion"/></xsl:variable>  
    <xsl:variable name="defaultErev"><xsl:call-template name="convert">
        <xsl:with-param name="value" select="/cml:channelml/cml:ion[@name=$ionname]/@default_erev"/>
        <xsl:with-param name="quantity">Voltage</xsl:with-param>
        </xsl:call-template>
    </xsl:variable>
    <xsl:for-each select="/cml:channelml/cml:ion">
    <xsl:if test ="count(@role) = 0 or @role='PermeatedSubstance'">e<xsl:value-of select="@name"/> = <xsl:value-of select="$defaultErev"/><xsl:text>
        </xsl:text>
            </xsl:if>
        </xsl:for-each>
        
        <xsl:choose>
            <xsl:when test="$voltConcDependence='yes'">
    settables(v,cai)
    </xsl:when>
            <xsl:otherwise>
    rates(v)
    </xsl:otherwise>
        </xsl:choose>
    
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    <xsl:value-of select="cml:state/@name"/> = <xsl:value-of select="cml:state/@name"/>inf<xsl:text>
    </xsl:text></xsl:for-each>
}
    
STATE {
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    <xsl:value-of select="cml:state/@name"/><xsl:text>
    </xsl:text>
    </xsl:for-each>
}

DERIVATIVE states {
    <xsl:choose>
        <xsl:when test="$voltConcDependence='yes'">settables(v,cai)
    </xsl:when>
        <xsl:otherwise>rates(v)
    </xsl:otherwise>
    </xsl:choose>
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    <xsl:value-of select="cml:state/@name"/>' = (<xsl:value-of select="cml:state/@name"/>inf - <xsl:value-of select="cml:state/@name"/>)/<xsl:value-of select="cml:state/@name"/>tau<xsl:text>
    </xsl:text></xsl:for-each>
}

<xsl:choose>
        <xsl:when test="$voltConcDependence='yes'">PROCEDURE settables(v(mV), cai(mM)) { </xsl:when>
        <xsl:otherwise>PROCEDURE rates(v(mV)) { </xsl:otherwise>
    </xsl:choose> 
    
    ? Note, not all of these may be used, depending on the form of rate equations
    LOCAL  alpha, beta, gamma, zeta, A, B, k, d, tau, inf<xsl:for-each select='cml:hh_gate/cml:transition/cml:voltage_conc_gate/cml:conc_dependence'
    >, <xsl:value-of select="@variable_name"/> </xsl:for-each> <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">, temp_adj_<xsl:value-of 
    select="cml:state/@name"/></xsl:for-each>
    
    <xsl:variable name="numGates"><xsl:value-of select="count(cml:hh_gate)"/></xsl:variable>
    
    <xsl:if test="$voltConcDependence='no'">
        
        <xsl:variable name="max_v">
            <xsl:choose>
                <xsl:when test="count(cml:impl_prefs/cml:table_settings) = 0">100</xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="convert">
                        <xsl:with-param name="value"><xsl:value-of select="cml:impl_prefs/cml:table_settings/@max_v"/></xsl:with-param>
                        <xsl:with-param name="quantity">Voltage</xsl:with-param>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>     
        
        <xsl:variable name="min_v">
            <xsl:choose>
                <xsl:when test="count(cml:impl_prefs/cml:table_settings) = 0">-100</xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="convert">
                        <xsl:with-param name="value"><xsl:value-of select="cml:impl_prefs/cml:table_settings/@min_v"/></xsl:with-param>
                        <xsl:with-param name="quantity">Voltage</xsl:with-param>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>       
        </xsl:variable>
        
        <xsl:variable name="table_divisions">
            <xsl:choose>
                <xsl:when test="count(cml:impl_prefs/cml:table_settings) = 0">400</xsl:when>
                <xsl:otherwise><xsl:value-of select="cml:impl_prefs/cml:table_settings/@table_divisions"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
    TABLE <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate"><xsl:value-of 
    select="cml:state/@name"/>inf, <xsl:value-of select="cml:state/@name"/>tau<xsl:if test="position() &lt; number($numGates)">,</xsl:if> </xsl:for-each> DEPEND celsius FROM <xsl:value-of select="$min_v"/> TO <xsl:value-of select="$max_v"/> WITH <xsl:value-of select="$table_divisions"/></xsl:if>
    
    
    UNITSOFF
    <xsl:choose>
        <xsl:when test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:q10_settings) &gt; 0">
    ? There is a Q10 factor which will alter the tau of the gates 
            <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:q10_settings">
                <xsl:choose>
                    <xsl:when test="count(@gate) &gt; 0">
                        <xsl:choose><xsl:when test="count(@q10_factor) &gt; 0">
    temp_adj_<xsl:value-of select="@gate"/> = <xsl:value-of select="@q10_factor" />^((celsius - <xsl:value-of select="@experimental_temp"/>)/10)
                        </xsl:when><xsl:when test="count(@fixed_q10) &gt; 0">
    temp_adj_<xsl:value-of select="@gate"/> = <xsl:value-of select="@fixed_q10" />
                        </xsl:when></xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose><xsl:when test="count(@q10_factor) &gt; 0">
                            <xsl:variable name="expression"><xsl:value-of select="@q10_factor" />^((celsius - <xsl:value-of select="@experimental_temp"/>)/10)</xsl:variable>
                            <xsl:for-each select="../../cml:gate">
    temp_adj_<xsl:value-of select="cml:state/@name"/> = <xsl:value-of select="$expression"/>
                            </xsl:for-each>
                        </xsl:when><xsl:when test="count(@fixed_q10) &gt; 0">     
                            <xsl:variable name="expression"><xsl:value-of select="@fixed_q10" /></xsl:variable>
                            <xsl:for-each select="../../cml:gate">
    temp_adj_<xsl:value-of select="cml:state/@name"/> = <xsl:value-of select="$expression"/>
                            </xsl:for-each>
                        </xsl:when></xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">temp_adj_<xsl:value-of 
    select="cml:state/@name"/> = 1
    </xsl:for-each>
        </xsl:otherwise>
    </xsl:choose>
    
    <xsl:if test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset) &gt; 0">
    ? There is a voltage offset of <xsl:value-of select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset/@value"/>. This will shift the dependency of the rate equations 
    v = v - (<xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset/@value"/>
            <xsl:with-param name="quantity">Voltage</xsl:with-param>
            </xsl:call-template>)<xsl:text>
    </xsl:text>          
    </xsl:if>
    
    
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
        
        <xsl:variable name="stateName" select="cml:state/@name"/>
            
        <xsl:for-each select='../../../../cml:hh_gate[@state=$stateName]'>
                
        <xsl:for-each select='cml:transition/cml:voltage_conc_gate/cml:conc_dependence'>
    ? Gate depends on the concentration of <xsl:value-of select="@ion"/><xsl:text>
    </xsl:text>   
    <xsl:value-of select="@variable_name"/> = <xsl:value-of select="@ion"/>i ? In NEURON, the variable for the concentration  of <xsl:value-of select="@ion"/> is <xsl:value-of select="@ion"/>i
    </xsl:for-each>
        
    ?      ***  Adding rate equations for gate: <xsl:value-of select="$stateName"/>  ***<xsl:text>
        </xsl:text>   
    <xsl:for-each select='cml:transition/cml:voltage_gate/* | 
                          cml:transition/cml:voltage_conc_gate/*'>
        
        <xsl:if  test="name()!='conc_dependence'">
            <xsl:choose>
                <xsl:when  test="count(cml:parameterised_hh) &gt; 0">
    ? Found a parameterised form of rate equation for <xsl:value-of select="name()"/>, using expression: <xsl:value-of select="cml:parameterised_hh/@expr" /><xsl:text>
    </xsl:text>   
    
                    <xsl:for-each select="cml:parameterised_hh/cml:parameter">
    <xsl:value-of select="@name"/> = <xsl:value-of select="@value"/><xsl:text>
    </xsl:text>
                    </xsl:for-each>
    
                    <xsl:if test="$xmlFileUnitSystem  = 'SI Units'">
    ? Unit system in ChannelML file is SI units, therefore need to 
    ? convert these to NEURON quanities...
                        <xsl:choose>
                            <xsl:when test="string(name()) = 'alpha' or string(name()) = 'beta'">
    A = A * <xsl:call-template name="convert">
                <xsl:with-param name="value">1</xsl:with-param>
                <xsl:with-param name="quantity">InvTime</xsl:with-param>
            </xsl:call-template>   ? 1/ms
                            </xsl:when>
                            <xsl:when test="string(name()) = 'tau'">
    A = A * <xsl:call-template name="convert">
            <xsl:with-param name="value">1</xsl:with-param>
            <xsl:with-param name="quantity">Time</xsl:with-param>
        </xsl:call-template>   ? ms
                            </xsl:when>
                            <xsl:when test="string(name()) = 'inf'">
    A = A   ? Dimensionless
                            </xsl:when>
                        </xsl:choose>
    k = k * <xsl:call-template name="convert">
            <xsl:with-param name="value">1</xsl:with-param>
            <xsl:with-param name="quantity">InvVoltage</xsl:with-param>
          </xsl:call-template>   ? mV
    d = d * <xsl:call-template name="convert">
            <xsl:with-param name="value">1</xsl:with-param>
            <xsl:with-param name="quantity">Voltage</xsl:with-param>
          </xsl:call-template>   ? mV
          
                    </xsl:if>
    B = 1/k<xsl:text> 
    
    </xsl:text>
                    <xsl:choose>
                        <xsl:when test="cml:parameterised_hh/@type='exponential'">
    <xsl:value-of select="name()"/> = A * exp((v - d) / B)<xsl:text>
    
    </xsl:text>
                        </xsl:when>
                        <xsl:when test="cml:parameterised_hh/@type='sigmoid'">
    <xsl:value-of select="name()"/> = A / (exp((v - d) / B) + 1)<xsl:text>
    
    </xsl:text>
                        </xsl:when>
                        <xsl:when test="cml:parameterised_hh/@type='linoid'">
    <xsl:value-of select="name()"/> = A * vtrap((v - d), B)<xsl:text>
    
    </xsl:text>
                        </xsl:when>
                    </xsl:choose>
    
    
                </xsl:when>
                <xsl:when test="count(cml:generic_equation_hh) &gt; 0">
    ? Found a generic form of rate equation for <xsl:value-of select="name()"/>, using expression: <xsl:value-of select="cml:generic_equation_hh/@expr" /><xsl:text>
                    </xsl:text>  
                    <xsl:if test="string($xmlFileUnitSystem) = 'SI Units'">
    ? Note: Equation (and all ChannelML file values) in <xsl:value-of select="$xmlFileUnitSystem"/> so need to convert v first...<xsl:text>
    </xsl:text>
    v = v * <xsl:call-template name="convert">
                <xsl:with-param name="value">1</xsl:with-param>
                <xsl:with-param name="quantity">InvVoltage</xsl:with-param>
            </xsl:call-template>   ? temporarily set v to units of equation...<xsl:text>
            
    </xsl:text>
                        <xsl:if test="(name()='tau' or name()='inf') and 
                      (contains(string(cml:generic_equation_hh/@expr), 'alpha') or
                       contains(string(cml:generic_equation_hh/@expr), 'beta'))">
    ? Equation depends on alpha/beta, so converting them too...
    alpha = alpha * <xsl:call-template name="convert">
                        <xsl:with-param name="value">1</xsl:with-param>
                        <xsl:with-param name="quantity">Time</xsl:with-param>
                    </xsl:call-template>  
    beta = beta * <xsl:call-template name="convert">
                        <xsl:with-param name="value">1</xsl:with-param>
                        <xsl:with-param name="quantity">Time</xsl:with-param>   
                    </xsl:call-template>     
                        </xsl:if>
                    </xsl:if>
                    
    <xsl:call-template name="formatExpression">
        <xsl:with-param name="variable">
            <xsl:value-of select="name()"/>
        </xsl:with-param>
        <xsl:with-param name="oldExpression">
            <xsl:value-of select="cml:generic_equation_hh/@expr" />
        </xsl:with-param>
    </xsl:call-template>
    <xsl:if test="string($xmlFileUnitSystem) = 'SI Units'">
        
        <xsl:if test="name()='alpha' or name()='beta'">
    ? Set correct units of <xsl:value-of select="name()"/> for NEURON<xsl:text>
    </xsl:text>    
    <xsl:value-of select="name()"/> = <xsl:value-of select="name()"/> * <xsl:call-template name="convert">
                            <xsl:with-param name="value">1</xsl:with-param>
                            <xsl:with-param name="quantity">InvTime</xsl:with-param>
                        </xsl:call-template>
        </xsl:if>  
                                      
        <xsl:if test="name()='tau'">
    ? Set correct units of <xsl:value-of select="name()"/> for NEURON<xsl:text>
    </xsl:text>
    <xsl:value-of select="name()"/> = <xsl:value-of select="name()"/> * <xsl:call-template name="convert">
                    <xsl:with-param name="value">1</xsl:with-param>
                    <xsl:with-param name="quantity">Time</xsl:with-param>
                </xsl:call-template>
        </xsl:if> 
    
    v = v * <xsl:call-template name="convert">
                <xsl:with-param name="value">1</xsl:with-param>
                <xsl:with-param name="quantity">Voltage</xsl:with-param>
            </xsl:call-template>   ? reset v
        <xsl:if test="(name()='tau' or name()='inf') and 
                      (contains(string(cml:generic_equation_hh/@expr), 'alpha') or
                       contains(string(cml:generic_equation_hh/@expr), 'beta'))">
    alpha = alpha * <xsl:call-template name="convert">
                        <xsl:with-param name="value">1</xsl:with-param>
                        <xsl:with-param name="quantity">InvTime</xsl:with-param>
                    </xsl:call-template>  ? resetting alpha
    beta = beta * <xsl:call-template name="convert">
                        <xsl:with-param name="value">1</xsl:with-param>
                        <xsl:with-param name="quantity">InvTime</xsl:with-param>   
                    </xsl:call-template>  ? resetting beta
        </xsl:if>
    </xsl:if>      <xsl:text>
    </xsl:text>  
           
            </xsl:when>
            <xsl:otherwise>
    ? ERROR: Unrecognised form of the rate equation for <xsl:value-of select="name()"/>
            
            </xsl:otherwise>
        </xsl:choose>
                
       <xsl:if test="name()='tau'">
    <xsl:value-of select="$stateName"/>tau = tau/temp_adj_<xsl:value-of select="$stateName"/><xsl:text>
    </xsl:text>   
       </xsl:if>    
                   
       <xsl:if test="name()='inf'">
    <xsl:value-of select="$stateName"/>inf = inf<xsl:text>
    </xsl:text>   
       </xsl:if>
      </xsl:if>
    </xsl:for-each>
    
    <!-- Finishing off the alpha & beta to tau & inf conversion... -->

         
        <xsl:if test="count(cml:transition/cml:voltage_gate/cml:tau)=0 and count(cml:transition/cml:voltage_conc_gate/cml:tau)=0">
    <xsl:value-of select="$stateName"/>tau = 1/(temp_adj_<xsl:value-of select="$stateName"/>*(alpha + beta))<xsl:text>
    </xsl:text>
       </xsl:if>       
         
       <xsl:if test="count(cml:transition/cml:voltage_gate/cml:inf)=0 and count(cml:transition/cml:voltage_conc_gate/cml:inf)=0">
    <xsl:value-of select="$stateName"/>inf = alpha/(alpha + beta)<xsl:text>
    </xsl:text>
       </xsl:if>      
       
    
    ?     *** Finished rate equations for gate: <xsl:value-of select="$stateName"/> ***
    
        </xsl:for-each>  <!-- <xsl:for-each select='../../../../cml:hh_gate[@state=$stateName]'>-->

    </xsl:for-each> <!--<xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">-->
}

FUNCTION vtrap(VminV0, B) {
    if (fabs(VminV0/B) &lt; 1e-6) {
    vtrap = (1 + VminV0/B/2)
}else{
    vtrap = (VminV0 / B) /(1 - exp((-1 *VminV0)/B))
    }
}

UNITSON


</xsl:if>

</xsl:template> <!--end of <xsl:template match="cml:channel_type">-->


<xsl:template match="cml:ion_concentration">
<!-- Based on Louise Whiteley's implementation of this while on rotation in the Silver Lab-->
? Creating ion concentration

TITLE Channel: <xsl:value-of select="@name"/>

<xsl:if test="count(meta:notes) &gt; 0">

COMMENT
    <xsl:value-of select="meta:notes"/>
ENDCOMMENT
</xsl:if>

UNITS {
    (mV) = (millivolt)
    (mA) = (milliamp)
    (um) = (micrometer)
    (l) = (liter)
    (molar) = (1/liter)
    (mM) = (millimolar)
}

    
NEURON {
    SUFFIX <xsl:value-of select="@name"/>
    
    <xsl:for-each select="/cml:channelml/cml:ion[@name!='non_specific']">
    USEION <xsl:value-of select="@name"/> READ i<xsl:value-of select="@name"/> WRITE <xsl:value-of select="@name"/>i VALENCE <xsl:value-of select="@charge"/>
  
    </xsl:for-each>
    
    <xsl:variable name="ionused"><xsl:value-of select="cml:ion_species"/></xsl:variable>
    <xsl:variable name="valency"><xsl:value-of select="/cml:channelml/cml:ion[@name=$ionused]/@charge"/></xsl:variable>
    
    RANGE <xsl:value-of select="$ionused"/>i
    
    RANGE rest_conc, tau, F, thickness
    
    GLOBAL volume, surf_area, total_current

}

ASSIGNED {

    i<xsl:value-of select="$ionused"/> (mA/cm2)
    diam (um)
}

INITIAL {
    LOCAL shell_inner_diam

    <xsl:value-of select="$ionused"/>i = rest_conc
    shell_inner_diam = diam - (2*thickness)
    
    volume = (diam*diam*diam)*3.14159/6 - (shell_inner_diam*shell_inner_diam*shell_inner_diam)*3.14159/6
    
    surf_area = (diam*diam)*3.14159
    
    VERBATIM

    //printf("\n\n surf_area: %f\n", surf_area);

    ENDVERBATIM

}

PARAMETER {

    rest_conc = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="cml:decaying_pool_model/cml:resting_conc"/></xsl:with-param>
              <xsl:with-param name="quantity">Concentration</xsl:with-param>
          </xsl:call-template> (mM)
    tau = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:decaying_pool_model/cml:decay_constant"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param>
          </xsl:call-template> (ms)
    F = 96494 (C)
    thickness = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="cml:decaying_pool_model/cml:pool_volume_info/cml:shell_thickness"/></xsl:with-param>
                    <xsl:with-param name="quantity">Length</xsl:with-param>
                </xsl:call-template> (um)   
                
    volume
    total_current
    surf_area
}

STATE {

    <xsl:value-of select="$ionused"/>i (mM)

}

BREAKPOINT {

    SOLVE conc METHOD derivimplicit

}

DERIVATIVE conc {
    LOCAL thickness_cm, surf_area_cm2, volume_cm3 ? Note, normally dimensions are in um, but curr dens is in mA/cm2, etc
    
    thickness_cm = thickness *(1e-4)
    surf_area_cm2 = surf_area * 1e-8
    volume_cm3 = volume * 1e-12
    
    total_current = i<xsl:value-of select="$ionused"/> * surf_area_cm2


    <xsl:value-of select="$ionused"/>i' =  ((-1 * total_current)/(<xsl:value-of select="$valency"/> * F * volume_cm3)) - ((<xsl:value-of select="$ionused"/>i - rest_conc)/tau)

}

</xsl:template>  <!--<xsl:template match="cml:ion_concentration">-->


<!-- Function to get value converted to proper units.-->
<xsl:template name="convert">
    <xsl:param name="value" />
    <xsl:param name="quantity" />
    <xsl:choose> 
        <xsl:when test="$xmlFileUnitSystem  = 'Physiological Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Conductance'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Voltage'"><xsl:value-of select="$value"/></xsl:when>                       <!-- same -->
                <xsl:when test="$quantity = 'InvVoltage'"><xsl:value-of select="$value"/></xsl:when>                    <!-- same -->
                <xsl:when test="$quantity = 'Time'"><xsl:value-of select="number($value)"/></xsl:when>                  <!-- same -->
                <xsl:when test="$quantity = 'Length'"><xsl:value-of select="number($value * 10000)"/></xsl:when>        <!-- same -->
                <xsl:when test="$quantity = 'InvTime'"><xsl:value-of select="number($value)"/></xsl:when>               <!-- same --> 
                <xsl:when test="$quantity = 'Concentration'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"><xsl:value-of select="number($value div 1000000)"/></xsl:when>

                <xsl:otherwise><xsl:value-of select="number($value)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:when>           
        <xsl:when test="$xmlFileUnitSystem  = 'SI Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"><xsl:value-of select="number($value div 10000)"/></xsl:when>
                <xsl:when test="$quantity = 'Conductance'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'Voltage'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"><xsl:value-of select="$value div 1000"/></xsl:when>
                <xsl:when test="$quantity = 'Length'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'Time'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvTime'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Concentration'"><xsl:value-of select="number($value)"/></xsl:when>         <!-- same -->
                <xsl:when test="$quantity = 'InvConcentration'"><xsl:value-of select="number($value)"/></xsl:when>      <!-- same -->

                <xsl:otherwise><xsl:value-of select="number($value)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:when>   
    </xsl:choose>
</xsl:template>







<xsl:template match="cml:synapse_type">
    <xsl:if test="count(cml:doub_exp_syn)>0">
? Creating synaptic mechanism, based on NEURON source impl of Exp2Syn
    </xsl:if>
    <xsl:if test="count(cml:blocking_syn)>0">
? Creating NMDA like synaptic mechanism, based on NEURON source impl of Exp2Syn
    </xsl:if>
    <xsl:if test="count(cml:plastic_syn)>0">
? Creating synaptic mechanism, based on V. Steuber &amp; C. Saviane implementation of 3 decay component facilitating synapse

DEFINE nspikes 50
    </xsl:if>

TITLE Channel: <xsl:value-of select="@name"/>

<xsl:if test="count(meta:notes) &gt; 0">

COMMENT
    <xsl:value-of select="meta:notes"/>
ENDCOMMENT
</xsl:if>

UNITS {
    (nA) = (nanoamp)
    (mV) = (millivolt)
    (uS) = (microsiemens)
}

    
NEURON {
    POINT_PROCESS <xsl:value-of select="@name"/>
<xsl:if test="count(cml:doub_exp_syn)>0">
    RANGE tau1, tau2 
    GLOBAL total
</xsl:if>
<xsl:if test="count(cml:blocking_syn)>0">
    RANGE tau1, tau2, <xsl:value-of select="cml:blocking_syn/cml:block/@species"/>_conc, eta, gamma, gblock
    GLOBAL total
</xsl:if>

<xsl:if test="count(cml:plastic_syn)>0">
    RANGE taur, taud1, taud2, taud3, ampl
    <xsl:if test="count(cml:plastic_syn/cml:plasticity)>0">
    RANGE Taurec, Taufac, Uinit
    :RANGE plastic
    </xsl:if>
    RANGE Des   : Including or not desensitisation
    RANGE Taudes
    RANGE Correlation :Indicates whether the des is correlated to the release prob or not (1 for yes)
</xsl:if>
    RANGE i, e, gmax
    NONSPECIFIC_CURRENT i
    RANGE g

}

PARAMETER {<xsl:for-each select="cml:doub_exp_syn">
    gmax = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@max_conductance"/></xsl:with-param>
              <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>
    tau1 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@rise_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    tau2 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@decay_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    e = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@reversal_potential"/></xsl:with-param>
              <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>  (mV)
</xsl:for-each>
<xsl:for-each select="cml:blocking_syn">
    gmax = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@max_conductance"/></xsl:with-param>
              <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>
    tau1 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@rise_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    tau2 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@decay_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    e = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@reversal_potential"/></xsl:with-param>
              <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>  (mV)
              
    <xsl:value-of select="cml:block/@species"/>_conc = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:block/@conc"/></xsl:with-param>
              <xsl:with-param name="quantity">Concentration</xsl:with-param></xsl:call-template> 
              
    eta = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:block/@eta"/></xsl:with-param>
              <xsl:with-param name="quantity">InvConcentration</xsl:with-param></xsl:call-template> 
              
    gamma = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:block/@gamma"/></xsl:with-param>
              <xsl:with-param name="quantity">InvVoltage</xsl:with-param></xsl:call-template> 
              
</xsl:for-each>
<xsl:for-each select="cml:plastic_syn">
    gmax = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@max_conductance"/></xsl:with-param>
              <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>
    taur = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@rise_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    taud1 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@decay_time_1"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    taud2 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:choose>
                                    <xsl:when test="count(@decay_time_2)>0"><xsl:value-of select="@decay_time_2"/></xsl:when>
                                    <xsl:otherwise>0</xsl:otherwise></xsl:choose>
              </xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    taud3 = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:choose>
                                    <xsl:when test="count(@decay_time_3)>0"><xsl:value-of select="@decay_time_3"/></xsl:when>
                                    <xsl:otherwise>0</xsl:otherwise></xsl:choose>
              </xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt;
    e = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="@reversal_potential"/></xsl:with-param>
              <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template>  (mV)
              
<xsl:if test="count(cml:plasticity)>0">
    Taurec = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:plasticity/@tau_rec"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt; 
    Taufac = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:plasticity/@tau_fac"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> (ms) &lt;1e-9,1e9&gt; 
    Uinit = <xsl:value-of select="cml:plasticity/@init_release_prob"/> :release probability
</xsl:if>
       
    ampl = 1.74362
    Arat1 = 0.8 : 3 decaying components included so that the sum up to 1. 
    Arat2 = 0.16    : 3rd component has amplitude of (1-Arat1-Arat2)

    
       : plastic=1   :if 1 it is depressing, 0 no plasticity
        Des = 0     :des included if 1
        Correlation=1   :this means that the reduction in Q depends on the release P, otherwise it is a constant factor
    taudes=38.8 :fitting DQ 38.8 for correlation 18.7 for constant reduction. Fitting NormSTP: 20.3 for corr, 10.0 for const
</xsl:for-each>}


<xsl:if test="count(cml:doub_exp_syn)>0 or count(cml:blocking_syn)>0 ">
ASSIGNED {
    v (mV)
    i (nA)
    g (uS)
    factor
    total (uS)
<xsl:if test="count(cml:blocking_syn)>0">    gblock</xsl:if>
}

STATE {
    A (uS)
    B (uS)
}

INITIAL {
    LOCAL tp
    total = 0
    if (tau1/tau2 > .9999) {
        tau1 = .9999*tau2
    }
    A = 0
    B = 0
    tp = (tau1*tau2)/(tau2 - tau1) * log(tau2/tau1)
    factor = -exp(-tp/tau1) + exp(-tp/tau2)
    factor = 1/factor
}

BREAKPOINT {
    SOLVE state METHOD cnexp
    <xsl:if test="count(cml:doub_exp_syn)>0">g = gmax * (B - A)</xsl:if>
    <xsl:if test="count(cml:blocking_syn)>0">gblock = 1 / (1+ (<xsl:value-of select="cml:blocking_syn/cml:block/@species"/>_conc * eta * exp(-1 * gamma * v)))
    g = gmax * gblock * (B - A)</xsl:if>
    
    i = g*(v - e)
}


DERIVATIVE state {
    A' = -A/tau1
    B' = -B/tau2 
}

NET_RECEIVE(weight (uS)) {
    state_discontinuity(A, A + weight*factor)
    state_discontinuity(B, B + weight*factor)
    total = total+weight
}

</xsl:if>

<xsl:if test="count(cml:plastic_syn)>0">
ASSIGNED {
        v (mV)
        i (nA)
        g (uS)
    tspike[nspikes] (ms)
    RUD[nspikes]    :multiplicative factor that include the change in probability (fac+dep) and des (D)
    R[nspikes]
    U[nspikes]
    D[nspikes]
}


INITIAL {
    LOCAL cspike
    cspike = 0
    while (cspike &lt; nspikes) {
        tspike[cspike] = 0
        R[cspike] = 1 
<xsl:if test="count(cml:plastic_syn/cml:plasticity)>0">
        U[cspike] = Uinit
</xsl:if>
        D[cspike]=1
        cspike = cspike + 1
            
    }
}

BREAKPOINT {
        g = gtrace(t)
        i = g*(v - e)
}



FUNCTION myexp(x) {
        if (x &lt; -100) {
            myexp = 0
        } else {
            myexp = exp(x)
        }
}

FUNCTION gtrace(x) {
    LOCAL cspike
    cspike = 0
    gtrace = 0
    while ((cspike &lt; nspikes) &amp;&amp; (tspike[cspike] != 0)) {
            gtrace = gtrace + RUD[cspike]*D[cspike]* (-myexp(-(x-tspike[cspike])/taur) + Arat1*myexp(-(x-tspike[cspike])/taud1) + Arat2*myexp(-(x-tspike[cspike])/taud2) + (1-Arat1-Arat2)*myexp(-(x-tspike[cspike])/taud3)) 
        cspike = cspike + 1
    } 
    gtrace = gtrace*ampl*gmax
}


NET_RECEIVE(weight) {
    LOCAL cspike1
    cspike1 = nspikes - 1
    while (cspike1 &gt; 0) {
        tspike[cspike1] = tspike[(cspike1 - 1)]
<xsl:if test="count(cml:plastic_syn/cml:plasticity)>0">
        U[cspike1] = U[(cspike1 - 1)]       
</xsl:if>
        R[cspike1] = R[(cspike1 - 1)]       
        RUD[cspike1] = RUD[(cspike1 - 1)]       
        cspike1 = cspike1 - 1
    }
    
    tspike[0] = t
        
<xsl:choose>
    <xsl:when test="count(cml:plastic_syn/cml:plasticity)>0">
    if ((tspike[1] != 0)) { : check if cell has spiked before; in that case the modifications are occurring
        U[0] = U[1]*myexp(-(tspike[0]-tspike[1])/Taufac)+ Uinit*(1-U[1]*myexp(-(tspike[0]-tspike[1])/Taufac)) 
        R[0] = R[1]*(1-U[1])*myexp(-(tspike[0]-tspike[1])/Taurec) + 1 - myexp(-(tspike[0]-tspike[1])/Taurec)
        RUD[0] = R[0]*U[0]/Uinit
    } else {
        RUD[0] = 1
    }
    </xsl:when>
    <xsl:otherwise>
    RUD[0] = 1
    </xsl:otherwise>
</xsl:choose>

    if ((tspike[1] != 0) &amp;&amp; (Des == 1)) {  : maybe there is no presynaptic plasticity but desensitisation
        if (Correlation == 1){
            D[0]=1-myexp(-(tspike[0]-tspike[1])/taudes)+D[1]*(1-(0.48*RUD[1]-0.1)*myexp(10/taudes))*myexp(-(tspike[0]-tspike[1])/taudes)
        } else {
            D[0]=1-myexp(-(tspike[0]-tspike[1])/taudes)+D[1]*(1-0.193305802*myexp(10/taudes))*myexp(-(tspike[0]-tspike[1])/taudes)  
        }
    } else {
        D[0]=1
    }

}
</xsl:if>




</xsl:template>  <!--<xsl:template match="cml:synapse_type">-->


<!-- Function to try to format the rate expression to something this simulator is a bit happier with-->
<xsl:template name="formatExpression">
    <xsl:param name="variable" />
    <xsl:param name="oldExpression" />
    <xsl:choose>
        <xsl:when test="contains($oldExpression, '?')">
    <!-- Expression contains a condition!!-->
    <xsl:variable name="ifTrue"><xsl:value-of select="substring-before(substring-after($oldExpression,'?'), ':')"/></xsl:variable>
    <xsl:variable name="ifFalse"><xsl:value-of select="substring-after($oldExpression,':')"/></xsl:variable>
    
    if (<xsl:value-of select="substring-before($oldExpression,'?')"/>) {<xsl:text>
        </xsl:text><xsl:value-of select="$variable"/> = <xsl:value-of select="$ifTrue"/>
    } else {<xsl:text>
        </xsl:text><xsl:value-of select="$variable"/> = <xsl:value-of select="$ifFalse"/>
    }</xsl:when>
        <xsl:otherwise>
    <xsl:value-of select="$variable"/> = <xsl:value-of select="$oldExpression"/><xsl:text>
        </xsl:text>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>


</xsl:stylesheet>