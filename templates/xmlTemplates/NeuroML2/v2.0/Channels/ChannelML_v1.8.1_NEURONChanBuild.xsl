<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:cml="http://morphml.org/channelml/schema">

<!--

    This file is used to convert ChannelML files to NEURON KS Channel Builder files
    NOTE: Not all cases allowable in the new ChannelML spec are implemented in this yet.
    
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

<xsl:output method="text" indent="yes" />

<xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="/cml:channelml/@units"/></xsl:variable>   

<!--Main template-->

<xsl:template match="/cml:channelml">
<xsl:text>//  This is a NEURON Channel Builder file generated from a ChannelML file
//
//  NOTE: Not all cases allowable in the ChannelML specification are implemented in this yet,
//  generally it's just the parameterised (Adk) form of the channels that's supported. The mod file mapping
//  will support most parts of the specification. This Channel Builder form should be used if 
//  the channel is specified as a kinetic scheme.
//
//  It assumes the ChannelML file contains only a single conductance i.e. one channel_type element.

</xsl:text>
// Unit system of ChannelML file: <xsl:value-of select="$xmlFileUnitSystem"/>, which have been converted to NEURON units<xsl:text>    
</xsl:text>


<xsl:if test="count(//cml:generic_equation_hh) &gt; 0">

//   ***********************************************************************
//     Note: Generic forms of the rate equations are not yet supported in
//     the mapping to Channel Builder. Use the mod file mapping instead!!!
//   ***********************************************************************

</xsl:if>
<xsl:if test="count(//cml:synapse_type) &gt; 0">

//   ***********************************************************************
//     Note: This mapping cannot be used for synaptic mechanisms!!!
//   ***********************************************************************

</xsl:if>

<xsl:if test="count(cml:ion_concentration) &gt; 0">

//   ***********************************************************************
//     Note: Ion concentrations are not supported in this version of the
//     mapping to Channel Builder. Use the mod file mapping instead!!!
//   ***********************************************************************

</xsl:if>

<xsl:choose>
    <xsl:when test="count(cml:channel_type/cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">
    
//   ***********************************************************************
//     Note: Integrate and Fire mechanisms are not supported in the
//     mapping to Channel Builder. Use the mod file mapping instead!!!
//   ***********************************************************************
    </xsl:when>
    <xsl:otherwise>


        <xsl:apply-templates select ="cml:ion"/>

<!-- Only do the first channel -->
        <xsl:apply-templates select ="cml:channel_type"/>

    </xsl:otherwise>
</xsl:choose>

</xsl:template>
<!--End Main template-->


<xsl:template match="cml:ion">
{ ion_register("<xsl:value-of select="@name"/>", <xsl:value-of select="@charge"/>) }
</xsl:template>


<xsl:template match="cml:channel_type">


<xsl:for-each select="cml:current_voltage_relation">
    <xsl:if test="count(@ion)&gt;0">{ ion_register("<xsl:value-of select="@ion"/>", <xsl:choose><xsl:when test="count(@charge)&gt;0">
        <xsl:value-of select="@charge"/></xsl:when><xsl:otherwise>1</xsl:otherwise></xsl:choose>) }</xsl:if>
</xsl:for-each>
    
// Creating the main ks objects

objref ks, ksvec, ksgate, ksstates, kstransitions, tobj
{
    ksvec = new Vector()
    ksstates = new List()
    kstransitions = new List()
    ks = new KSChan(0)
}

<xsl:variable name="ionname"><xsl:value-of select="cml:current_voltage_relation/cml:ohmic/@ion"/><xsl:value-of select="cml:current_voltage_relation/@ion"/></xsl:variable>
// Setting the properties of  KSChan
{
    ks.name("<xsl:value-of select="@name"/>")
    ks.ion("<xsl:value-of select="$ionname"/>")
    ks.iv_type(0)  // Note, only ohmic supported in current version
    ks.gmax(<xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/@default_gmax"/>
            <xsl:with-param name="quantity">Conductance Density</xsl:with-param>
          </xsl:call-template>)
    ks.erev(<xsl:call-template name="convert">
            <xsl:with-param name="value" select="cml:current_voltage_relation/@default_erev"/>
            <xsl:with-param name="quantity">Voltage</xsl:with-param>
            </xsl:call-template>)
}

<xsl:for-each select="cml:current_voltage_relation/cml:gate">
    <xsl:variable name='gateName'><xsl:value-of select="@name"/></xsl:variable>
    <xsl:variable name='gatePower'><xsl:value-of select="@instances"/></xsl:variable>
    
//     Adding information for the gate: <xsl:value-of select="$gateName"/>

{   <xsl:choose>
        <xsl:when test="count(../../../../cml:hh_gate[@state=$gateName])&gt;0">
    ksstates.append(ks.add_hhstate("<xsl:value-of select="$gateName"/>"))
    ksgate = ksstates.object(0).gate
    ksgate.power(<xsl:value-of select="@power"/>)
    <xsl:if test="count(cml:state/@fraction)&gt;0">ksstates.object(0).frac(<xsl:value-of select="cml:state/@fraction"/>)</xsl:if>
    kstransitions.append(ks.trans(ksstates.object(0), ksstates.object(0)))
        </xsl:when>
        <xsl:when test="count(../../../../cml:ks_gate/cml:state[@name=$gateName])&gt;0">
    objref ksgate
    <xsl:for-each select="../../../../cml:ks_gate">
        <xsl:if test="count(cml:state[@name=$gateName])&gt;0">
    // Adding information for KS gate related to <xsl:value-of select="$gateName"/>, which has <xsl:value-of select="count(cml:state)"/> states and <xsl:value-of select="count(cml:transition)"/> transitions
            <xsl:for-each select="cml:state">
                <xsl:variable name='stateIndex'><xsl:value-of select="position() -1"/></xsl:variable>
    ksstates.append(ks.add_ksstate(ksgate, "<xsl:value-of select="@name"/>"))
                <xsl:variable name='stateName'><xsl:value-of select="@name"/></xsl:variable>
                <xsl:choose>
                    <xsl:when test="count(../../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate/cml:state[@name=$stateName])&gt;0">
                        <xsl:choose>
                            <xsl:when test="count(../../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate/cml:state[@name=$stateName]/@fraction)&gt;0">
    ksstates.object(<xsl:value-of select="$stateIndex"/>).frac(<xsl:value-of select="../../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate/cml:state[@name=$stateName]/@fraction"/>)           
                            </xsl:when>
                            <xsl:otherwise>
    ksstates.object(<xsl:value-of select="$stateIndex"/>).frac(1)                            
                            </xsl:otherwise>
                        </xsl:choose>
    
                    </xsl:when>
                    <xsl:otherwise>
    ksstates.object(<xsl:value-of select="$stateIndex"/>).frac(0)
                    </xsl:otherwise>
                </xsl:choose>
            <xsl:if test="position()=1">
    ksgate = ksstates.object(0).gate
    ksgate.power(<xsl:value-of select="$gatePower"/>)
            </xsl:if>    
            </xsl:for-each>
    
        </xsl:if>
    </xsl:for-each>
    
        </xsl:when>


        <xsl:when test="count(cml:open_state)&gt;0">
    objref ksgate

    // Adding information for KS gate related to <xsl:value-of select="$gateName"/>, which has <xsl:value-of select="count(cml:open_state)"/> open and <xsl:value-of select="count(cml:closed_state)"/> closed states and <xsl:value-of select="count(cml:transition)"/> transitions
            <xsl:for-each select="cml:open_state | cml:closed_state">
                <xsl:variable name='stateIndex'><xsl:value-of select="position() -1"/></xsl:variable>
    ksstates.append(ks.add_ksstate(ksgate, "<xsl:value-of select="@id"/>"))  // Note: <xsl:value-of select="name()"/>...
                <xsl:variable name='stateName'><xsl:value-of select="@id"/></xsl:variable>

                <xsl:choose>
                    <xsl:when test="name() = 'open_state'">
                        <xsl:choose>
                            <xsl:when test="count(@fraction)&gt;0">
    ksstates.object(<xsl:value-of select="$stateIndex"/>).frac(<xsl:value-of select="@fraction"/>)
                            </xsl:when>
                            <xsl:otherwise>
    ksstates.object(<xsl:value-of select="$stateIndex"/>).frac(1.0)
                            </xsl:otherwise>
                        </xsl:choose>

                    </xsl:when>
                    <xsl:otherwise>
    ksstates.object(<xsl:value-of select="$stateIndex"/>).frac(0)
                    </xsl:otherwise>
                </xsl:choose>
            <xsl:if test="position()=1">
    ksgate = ksstates.object(0).gate
    ksgate.power(<xsl:value-of select="$gatePower"/>)
            </xsl:if>
            </xsl:for-each>


        </xsl:when>


    </xsl:choose>
}

 <!--   <xsl:for-each select="../../../../cml:hh_gate[@state=$gateMainStateName]/cml:transition"> Never used??
{
    tobj = kstransitions.object(0)
    tobj.type(0)
    <xsl:apply-templates select="cml:voltage_gate"/>
    
    <xsl:if test="count(@name)&gt;0">
        // transition: <xsl:value-of select="@name"/>
    </xsl:if>


}
    </xsl:for-each>-->
{
    <xsl:for-each select="cml:transition">
        <xsl:variable name='src'><xsl:value-of select="@from"/></xsl:variable>
        <xsl:variable name='tgt'><xsl:value-of select="@to"/></xsl:variable>
        <xsl:variable name='srcIndex'>
            <xsl:for-each select="../cml:open_state | ../cml:closed_state ">
                <xsl:if test="@id=$src"><xsl:value-of select="position() -1"/></xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name='tgtIndex'>
            <xsl:for-each select="../cml:open_state | ../cml:closed_state ">
                <xsl:if test="@id=$tgt"><xsl:value-of select="position() -1"/></xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:if test="number($srcIndex) &lt; number($tgtIndex)">
    kstransitions.append(ks.add_transition(ksstates.object(<xsl:value-of select="$srcIndex"/>), ksstates.object(<xsl:value-of select="$tgtIndex"/>)))
        </xsl:if>
    </xsl:for-each>
}

    <xsl:for-each select="cml:transition">
        <xsl:variable name='src'><xsl:value-of select="@from"/></xsl:variable>
        <xsl:variable name='tgt'><xsl:value-of select="@to"/></xsl:variable>
        <xsl:variable name='srcIndex'>
            <xsl:for-each select="../cml:open_state | ../cml:closed_state ">
                <xsl:if test="@id=$src"><xsl:value-of select="position() -1"/></xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name='tgtIndex'>
            <xsl:for-each select="../cml:open_state | ../cml:closed_state ">
                <xsl:if test="@id=$tgt"><xsl:value-of select="position() -1"/></xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:if test="number($srcIndex) &lt; number($tgtIndex)">
{
    // The transition from <xsl:value-of select="@from"/> to <xsl:value-of select="@to"/>
    <xsl:variable name='index'><xsl:value-of select="number(position())-1"/></xsl:variable>
    tobj = kstransitions.object(<xsl:value-of select="number($index) * 0.5"/>) <!-- This assumes that the forward & reverse transitions are adjacent in the list of tranitions! -->
    tobj.type(0)

    // <xsl:value-of select="@from"/> -> <xsl:value-of select="@to"/>: type = <xsl:value-of select="@expr_form"/>, rate = <xsl:value-of select="@rate"/>, scale = <xsl:value-of select="@scale"/>, midpoint = <xsl:value-of select="@midpoint"/>
    tobj.set_f(0, <xsl:call-template name="getFunctionForm"><xsl:with-param name="stringFunctionName"
                select="@expr_form" /></xsl:call-template>, ksvec.c.append(<xsl:value-of
                select="@rate"/>, (1/<xsl:value-of
                select="@scale"/>), <xsl:value-of
                select="@midpoint"/>))

    <xsl:for-each select="../cml:transition">
        <xsl:if test="@from = $tgt and @to = $src">
    // <xsl:value-of select="@from"/> -> <xsl:value-of select="@to"/>: type = <xsl:value-of select="@expr_form"/>, rate = <xsl:value-of select="@rate"/>, scale = <xsl:value-of select="@scale"/>, midpoint = <xsl:value-of select="@midpoint"/>
    tobj.set_f(1, <xsl:call-template name="getFunctionForm"><xsl:with-param name="stringFunctionName"
                select="@expr_form" /></xsl:call-template>, ksvec.c.append(<xsl:value-of
                select="@rate"/>, (1/<xsl:value-of
                select="@scale"/>), <xsl:value-of
                select="@midpoint"/>))
        </xsl:if>

    </xsl:for-each>

}
            </xsl:if>

    </xsl:for-each>



    <xsl:for-each select="../../../../cml:ks_gate">
        <xsl:if test="count(cml:state[@name=$gateName])&gt;0">
{
            <xsl:for-each select="cml:transition">
                <xsl:variable name='src'><xsl:value-of select="@src"/><xsl:value-of select="@source"/></xsl:variable>  <!-- to allow for pre v2.0 form...-->
                <xsl:variable name='tgt'><xsl:value-of select="@target"/></xsl:variable>
                <xsl:variable name='srcIndex'>
                    <xsl:for-each select="../cml:state">
                        <xsl:if test="@name=$src"><xsl:value-of select="position() -1"/></xsl:if>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name='tgtIndex'>
                    <xsl:for-each select="../cml:state">
                        <xsl:if test="@name=$tgt"><xsl:value-of select="position() -1"/></xsl:if>
                    </xsl:for-each>
                </xsl:variable>
    kstransitions.append(ks.add_transition(ksstates.object(<xsl:value-of select="$srcIndex"/>), ksstates.object(<xsl:value-of select="$tgtIndex"/>)))
            </xsl:for-each>    
}
            <xsl:for-each select="cml:transition">

{
    // The transition from <xsl:value-of select="@src"/><xsl:value-of select="@source"/> to <xsl:value-of select="@target"/>  <!-- to allow for pre v2.0 form...-->

    tobj = kstransitions.object(<xsl:value-of select="number(position())-1"/>)
    tobj.type(0)    
    <xsl:apply-templates select="cml:voltage_gate"/>
}
            </xsl:for-each>
        </xsl:if>
    
    </xsl:for-each>


// Cleaning up...
{ 
    ksstates.remove_all  kstransitions.remove_all 
} 
</xsl:for-each>



{ objref ks, ksvec, ksgate, ksstates, kstransitions, tobj }

</xsl:template>


<xsl:template match="cml:voltage_gate">
    <xsl:for-each select="cml:alpha/cml:parameterised_hh">
    // Function type = <xsl:value-of select="@type"/>, A = <xsl:value-of select="cml:parameter[@name='A']/@value"/>, k = <xsl:value-of select="cml:parameter[@name='k']/@value"/>, d = <xsl:value-of select="cml:parameter[@name='d']/@value"/>
    tobj.set_f(0, <xsl:call-template name="getFunctionForm"><xsl:with-param name="stringFunctionName"
                select="@type" /></xsl:call-template>, ksvec.c.append(<xsl:value-of
                select="cml:parameter[@name='A']/@value"/>, <xsl:value-of
                select="cml:parameter[@name='k']/@value"/>, <xsl:value-of
                select="cml:parameter[@name='d']/@value"/>))
    </xsl:for-each>
    <xsl:for-each select="cml:beta/cml:parameterised_hh">
    // Function type = <xsl:value-of select="@type"/>, A = <xsl:value-of select="cml:parameter[@name='A']/@value"/>, k = <xsl:value-of select="cml:parameter[@name='k']/@value"/>, d = <xsl:value-of select="cml:parameter[@name='d']/@value"/>
    tobj.set_f(1, <xsl:call-template name="getFunctionForm"><xsl:with-param name="stringFunctionName"
                select="@type" /></xsl:call-template>, ksvec.c.append(<xsl:value-of
                select="cml:parameter[@name='A']/@value"/>, <xsl:value-of
                select="cml:parameter[@name='k']/@value"/>, <xsl:value-of
                select="cml:parameter[@name='d']/@value"/>))
    </xsl:for-each>
</xsl:template>





<!-- Function to return 2 for exponential, 4 for sigmoid, 3 for linoid or exp_linear-->
<xsl:template name="getFunctionForm">
    <xsl:param name="stringFunctionName" />
    <xsl:choose>
        <xsl:when test="$stringFunctionName = 'exponential'">2</xsl:when>
        <xsl:when test="$stringFunctionName = 'sigmoid'">4</xsl:when>
        <xsl:when test="$stringFunctionName = 'linoid'">3</xsl:when>
        <xsl:when test="$stringFunctionName = 'exp_linear'">3</xsl:when>
    </xsl:choose>
</xsl:template>



<!-- Function to get value converted to proper units.-->
<xsl:template name="convert">
    <xsl:param name="value" />
    <xsl:param name="quantity" />
    <xsl:choose> 
        <xsl:when test="$xmlFileUnitSystem  = 'Physiological Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Conductance'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Voltage'"><xsl:value-of select="$value"/></xsl:when> <!-- same -->
                <xsl:when test="$quantity = 'InvVoltage'"><xsl:value-of select="$value"/></xsl:when> <!-- same -->
                <xsl:when test="$quantity = 'Time'"><xsl:value-of select="number($value)"/></xsl:when> <!-- same -->
                <xsl:when test="$quantity = 'Length'"><xsl:value-of select="number($value * 10000)"/></xsl:when> <!-- same -->
                <xsl:when test="$quantity = 'InvTime'"><xsl:value-of select="number($value)"/></xsl:when> <!-- same --> 

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
                <xsl:when test="$quantity = 'Concentration'"><xsl:value-of select="number($value)"/></xsl:when>

                <xsl:otherwise><xsl:value-of select="number($value)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:when>   
    </xsl:choose>
</xsl:template>



</xsl:stylesheet>
