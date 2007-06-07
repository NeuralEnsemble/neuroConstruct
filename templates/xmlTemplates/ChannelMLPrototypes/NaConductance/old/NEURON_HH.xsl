<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:cml="http://morphml.org/channelml/schema/1.0.0">
<!--
    This file is used to convert v1.0 ChannelML files to NEURON mod files
    This file is taken from the neuroConstruct source code
    Author: Padraig Gleeson
    Note: Very alpha version!!
-->
<xsl:output method="text" indent="yes" />

<!--Main template-->
<xsl:template match="/cml:channelml">
<xsl:text>?  This is a NEURON mod file generated from a ChannelML file (very early version!)
? It assumes the ChannelML file contains only a HH like conductance</xsl:text>
<!-- Only do the first channel -->
<xsl:apply-templates/>
</xsl:template>
<!--End Main template-->
<xsl:template match="cml:channel_type">
<xsl:text>TITLE Channel: <xsl:value-of select="@name"/></xsl:text>

UNITS {
    (mA) = (milliamp)
    (mV) = (millivolt)
    (S) = (siemens)
}

<xsl:variable name="ion"><xsl:value-of select="current_voltage_relation/ohmic/@ion"/></xsl:variable>

NEURON {
    SUFFIX <xsl:value-of select="@name"/>
    USEION <xsl:value-of select="$ion"/> READ e<xsl:value-of select="$ion"/> WRITE i<xsl:value-of select="$ion"/>
    RANGE gbar, gion
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    RANGE <xsl:value-of select="cml:state/@name"/>inf, <xsl:value-of select="cml:state/@name"/>tau
    </xsl:for-each>
}

PARAMETER { gbar = <xsl:value-of select="cml:current_voltage_relation/cml:ohmic/cml:conductance/@default_gmax"/> (S/cm2) }

STATE {
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    <xsl:value-of select="state/@name"/><xsl:text>
    </xsl:text>
    </xsl:for-each>}

ASSIGNED {
    v (mV)
    celsius (degC)
    e<xsl:value-of select="$ion"/> (mV)
    gion (S/cm2)
    i<xsl:value-of select="$ion"/> (mA/cm2)
    <xsl:for-each select="current_voltage_relation/ohmic/conductance/gate">
    <xsl:value-of select="state/@name"/>inf<xsl:text>
    </xsl:text><xsl:value-of select="state/@name"/>tau (ms)<xsl:text>
    </xsl:text></xsl:for-each>}

?LOCAL Xexp, Yexp

BREAKPOINT {
    SOLVE states METHOD cnexp

    gion = gbar<xsl:for-each select="current_voltage_relation/ohmic/conductance/gate">*((<xsl:value-of select="state/@fraction"/>*<xsl:value-of select="cml:state/@name"/>)^<xsl:value-of select="@power"/>)</xsl:for-each>

    i<xsl:value-of select="$ion"/> = gion*(v - e<xsl:value-of select="$ion"/>)
}

INITIAL {
    ?e<xsl:value-of select="$ion"/> = %Reversal Potential%
    rates(v)
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    <xsl:value-of select="cml:state/@name"/> = <xsl:value-of select="cml:state/@name"/>inf<xsl:text>
    </xsl:text></xsl:for-each>}

DERIVATIVE states {
    rates(v)
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">
    <xsl:value-of select="state/@name"/>' = (<xsl:value-of select="cml:state/@name"/>inf - <xsl:value-of select="cml:state/@name"/>)/<xsl:value-of select="cml:state/@name"/>tau<xsl:text>
    </xsl:text></xsl:for-each>}

?LOCAL q10

PROCEDURE rates(v(mV)) {

    :Call once from HOC to initialize inf at resting v.

    LOCAL  alpha, beta, sum, A, B, V0

    TABLE Xinf, Xtau, Yinf, Ytau DEPEND celsius FROM -100 TO 100 WITH 200

    UNITSOFF
    ?q10 = 2.3^((celsius - %Experiment Temperature%)/10)
    q10 = 1

    <xsl:for-each select="hh_gate">
    ? Adding rate equations for gate: <xsl:value-of select="@state"/><xsl:text>
    </xsl:text>
    <xsl:for-each select="transition/voltage_gate/alpha/parameter">
    <xsl:value-of select="@name"/> = <xsl:value-of select="@value"/><xsl:text>
    </xsl:text></xsl:for-each>alpha = <xsl:value-of select="transition/voltage_gate/alpha/@expr"/>    <xsl:text>

    </xsl:text>
    <xsl:for-each select="transition/voltage_gate/beta/parameter">
    <xsl:value-of select="@name"/> = <xsl:value-of select="@value"/><xsl:text>
    </xsl:text></xsl:for-each>beta = <xsl:value-of select="transition/voltage_gate/beta/@expr"/><xsl:text>
    </xsl:text>

    sum = alpha + beta
    <xsl:value-of select="@state"/>tau = 1/(q10*sum)
    <xsl:value-of select="@state"/>inf = alpha/sum

    </xsl:for-each>




}

FUNCTION vtrap(VminV0, B) {
    if (fabs(VminV0/B) &lt; 1e-6) {
    vtrap = B*(1 - VminV0/B/2)
    }else{
    vtrap = VminV0/(exp(VminV0/B) - 1)
    }
}

UNITSON
</xsl:template>
<!--End Main template-->
</xsl:stylesheet>
