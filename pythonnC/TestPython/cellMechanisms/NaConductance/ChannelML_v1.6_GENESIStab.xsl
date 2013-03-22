<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:meta="http://morphml.org/metadata/schema"
    xmlns:cml="http://morphml.org/channelml/schema">

<!--

    This file is used to convert ChannelML v1.6 files to GENESIS tabchannel/tab2Dchannel/leakage based script files

    This file has been developed as part of the neuroConstruct project

    Funding for this work has been received from the Medical Research Council

    Author: Padraig Gleeson
    Copyright 2007 Department of Physiology, UCL

-->

<xsl:output method="text" indent="yes" />

<!-- Can be altered in this file to include/exclude lines printed during execution, for decoding purposes -->
<xsl:variable name="consoleOutput">no</xsl:variable>

<!-- The unit system (SI or Physiological) as used in the ChannelML file we're converting -->
<xsl:variable name="xmlFileUnitSystem"><xsl:value-of select="/cml:channelml/@units"/></xsl:variable>

<!-- The unit system (SI or Physiological) we wish to convert into (Note changing this value in this file
     will create a GENESIS script in different units) -->
<xsl:variable name="targetUnitSystem">SI Units</xsl:variable>

<!--Main template-->

<xsl:template match="/cml:channelml">
<xsl:text>// This is a GENESIS script file generated from a ChannelML v1.6 file
// ChannelML file is mapped onto a tabchannel object

</xsl:text>
// Units of ChannelML file: <xsl:value-of select="$xmlFileUnitSystem"/>, units of GENESIS file generated: <xsl:value-of
select="$targetUnitSystem"/>

<xsl:if test="count(meta:notes) &gt; 0">

/*
    <xsl:value-of select="meta:notes"/>
*/
</xsl:if>


<!-- Only do the first channel --><xsl:choose><xsl:when test="count(cml:channel_type/cml:ks_gate) &gt; 0">
    *** Note: Kinetic scheme based ChannelML descriptions cannot be mapped on to GENESIS at the present time. ***
</xsl:when>
<xsl:when test="count(cml:channel_type/cml:current_voltage_relation/cml:integrate_and_fire) &gt; 0">
    *** Note: Integrate and Fire mechanisms cannot be mapped on to GENESIS at the present time. ***
</xsl:when>

<xsl:otherwise>
<xsl:apply-templates  select="cml:channel_type"/>
</xsl:otherwise>
</xsl:choose>

<!-- If there is a concentration mechanism present -->
<xsl:apply-templates  select="cml:ion_concentration"/>

<!-- Do a synapse if there -->
<xsl:apply-templates  select="cml:synapse_type"/>

</xsl:template>
<!--End Main template-->


<xsl:template match="cml:channel_type">

function make_<xsl:value-of select="@name"/>
        <xsl:if test="count(meta:notes) &gt; 0">

        /*
            <xsl:value-of select="meta:notes"/><xsl:for-each select="meta:publication"><xsl:text>

            </xsl:text>Reference: <xsl:value-of select="meta:fullTitle"/>
            Pubmed: <xsl:value-of select="meta:pubmedRef"/></xsl:for-each>
        */
        </xsl:if>

        str chanpath = "/library/<xsl:value-of select="@name"/>"

        if ({exists {chanpath}})
            return
        end<xsl:text>
        </xsl:text>


        <xsl:variable name="ionname"><xsl:value-of select="cml:current_voltage_relation/cml:ohmic/@ion"/></xsl:variable>

        <xsl:choose>
            <xsl:when test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/*) = 0">
        create leakage {chanpath}
            </xsl:when>
            <xsl:when test="count(//cml:voltage_conc_gate) &gt; 0">
        create tab2Dchannel {chanpath}
            </xsl:when>
            <xsl:otherwise>
        create tabchannel {chanpath}
            </xsl:otherwise>
        </xsl:choose>

        setfield {chanpath} \
            Ek              <xsl:call-template name="convert">
                                    <xsl:with-param name="value" select="/cml:channelml/cml:ion[@name=$ionname]/@default_erev"/>
                                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                               </xsl:call-template> \
            Ik              0 <xsl:if test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate[1]) &gt; 0"> \
            Xpower          <xsl:value-of select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate[1]/@power"/>
            </xsl:if><xsl:if test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate[2]) &gt; 0"> \
            Ypower          <xsl:value-of select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate[2]/@power"/>
            </xsl:if><xsl:if test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate[3]) &gt; 0"> \
            Zpower          <xsl:value-of select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate[3]/@power"/>
            </xsl:if>
            <xsl:text>
        </xsl:text>
        <xsl:choose>
            <xsl:when test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/*) = 0">
        setfield {chanpath} Gk <xsl:call-template name="convert">
                                    <xsl:with-param name="value" select="cml:current_voltage_relation/cml:ohmic/cml:conductance/@default_gmax"/>
                                    <xsl:with-param name="quantity">Conductance Density</xsl:with-param>
                               </xsl:call-template><xsl:text>

        </xsl:text>
            </xsl:when>
            <xsl:otherwise>
        setfield {chanpath} \
            Gbar <xsl:call-template name="convert">
                                    <xsl:with-param name="value" select="cml:current_voltage_relation/cml:ohmic/cml:conductance/@default_gmax"/>
                                    <xsl:with-param name="quantity">Conductance Density</xsl:with-param>
                               </xsl:call-template> \
            Gk              0 <xsl:text>

        </xsl:text>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/*) &gt; 0">
            
            
            <xsl:choose>
                <xsl:when test="count(cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:q10_settings) &gt; 0">
        // There is a Q10 factor which will alter the tau of the gates
            <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:q10_settings">
                <xsl:choose>
                    <xsl:when test="count(@gate) &gt; 0">
                        <xsl:choose><xsl:when test="count(@q10_factor) &gt; 0">
        float temp_adj_<xsl:value-of select="@gate"/> = {pow <xsl:value-of select="@q10_factor"/> {(celsius - <xsl:value-of select="@experimental_temp"/>)/10}}
                        </xsl:when><xsl:when test="count(@fixed_q10) &gt; 0">
        float temp_adj_<xsl:value-of select="@gate"/> = <xsl:value-of select="@fixed_q10"/>
                        </xsl:when></xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose><xsl:when test="count(@q10_factor) &gt; 0">
                            <xsl:variable name="expression">{pow <xsl:value-of select="@q10_factor"/> {(celsius - <xsl:value-of select="@experimental_temp"/>)/10}}</xsl:variable>
                            <xsl:for-each select="../../cml:gate">
        float temp_adj_<xsl:value-of select="cml:state/@name"/> = <xsl:value-of select="$expression"/>
                            </xsl:for-each>
                        </xsl:when><xsl:when test="count(@fixed_q10) &gt; 0">
                            <xsl:variable name="expression"><xsl:value-of select="@fixed_q10"/></xsl:variable>
                            <xsl:for-each select="../../cml:gate">
        float temp_adj_<xsl:value-of select="cml:state/@name"/> = <xsl:value-of select="$expression"/>
                            </xsl:for-each>
                        </xsl:when></xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
        // No Q10 temperature adjustment found
    <xsl:for-each select="cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate">    float temp_adj_<xsl:value-of 
    select="cml:state/@name"/> = 1
    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
            
            

         <xsl:variable name="max_v">
            <xsl:choose>
                <xsl:when test="count(cml:impl_prefs/cml:table_settings) = 0"><xsl:choose>
                            <xsl:when test="$targetUnitSystem  = 'Physiological Units'">100</xsl:when>
                            <xsl:otherwise>0.1</xsl:otherwise>
                        </xsl:choose></xsl:when>
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
                <xsl:when test="count(cml:impl_prefs/cml:table_settings) = 0"><xsl:choose>
                            <xsl:when test="$targetUnitSystem  = 'Physiological Units'">-100</xsl:when>
                            <xsl:otherwise>-0.1</xsl:otherwise>
                        </xsl:choose></xsl:when>
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

        float tab_divs = <xsl:value-of select="$table_divisions"/>

        float v_min = <xsl:value-of select="$min_v"/>

        float v_max = <xsl:value-of select="$max_v"/>

        float v, dv, i
        </xsl:if>

        <xsl:for-each select='cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate'>

            <xsl:variable name='gateName'><xsl:value-of select="cml:state/@name"/></xsl:variable>
            <xsl:variable name='gateRef'>
                <xsl:if test='position()=1'>X</xsl:if>
                <xsl:if test='position()=2'>Y</xsl:if>
                <xsl:if test='position()=3'>Z</xsl:if>
            </xsl:variable>

        // Creating table for gate <xsl:value-of select="$gateName"/>, using <xsl:value-of select="$gateRef"/> for it here

        float dv = ({v_max} - {v_min})/{tab_divs}

        <xsl:for-each select="../../../../cml:hh_gate[@state=$gateName]">

            <xsl:choose>
                <xsl:when test="count(cml:transition/cml:voltage_conc_gate) &gt; 0">
                    <xsl:for-each select="cml:transition/cml:voltage_conc_gate/cml:conc_dependence">

        // Channel is dependent on concentration of: <xsl:value-of select="@name"/>, rate equations will involve variable: <xsl:value-of select="@variable_name"/>
        float c
        float conc_min = <xsl:call-template name="convert">
                                <xsl:with-param name="value"><xsl:value-of select="@min_conc"/></xsl:with-param>
                                <xsl:with-param name="quantity">Concentration</xsl:with-param>
                        </xsl:call-template>
        float conc_max = <xsl:call-template name="convert">
                                <xsl:with-param name="value"><xsl:value-of select="@max_conc"/></xsl:with-param>
                                <xsl:with-param name="quantity">Concentration</xsl:with-param>
                        </xsl:call-template>

        float dc = ({conc_max} - {conc_min})/{tab_divs}

        float <xsl:value-of select="@variable_name"/> = {conc_min}

        <!-- Impl here may not be generic enough for all cases -->
        // Setting up the volt/conc dependent 2D table
        setfield {chanpath}  <xsl:value-of select="$gateRef"/>index {VOLT_C1_INDEX} // assumes all gates are volt/conc dep

        call {chanpath} TABCREATE <xsl:value-of select="$gateRef"/> {tab_divs} {v_min} {v_max} {tab_divs} {conc_min} {conc_max}

        for (c = 0; c &lt;= ({tab_divs}); c = c + 1)
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
        call {chanpath} TABCREATE <xsl:value-of select="$gateRef"/> {tab_divs} {v_min} {v_max}
                </xsl:otherwise>
            </xsl:choose>

        v = {v_min}

            <xsl:if test="count(../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset) &gt; 0">
        // There is a voltage offset of <xsl:value-of select="../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset/@value"/>. This will shift the dependency of the rate equations
        v = v - <xsl:call-template name="convert">
                    <xsl:with-param name="value" select="../cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:rate_adjustments/cml:offset/@value"/>
                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                </xsl:call-template><xsl:text>
            </xsl:text>
            </xsl:if>

        for (i = 0; i &lt;= ({tab_divs}); i = i + 1)

            <xsl:for-each select='cml:transition/*/*'>
                <xsl:if test="name()!='conc_dependence'">
            // Looking at rate: <xsl:value-of select="name()"/><xsl:text>
                </xsl:text>
            float <xsl:value-of select="name()"/>    <xsl:text>
                </xsl:text>

                <xsl:choose>
                    <xsl:when  test="count(cml:parameterised_hh) &gt; 0">
            float A, B, k, V0
                        <xsl:call-template name="generateEquation">
                            <xsl:with-param name="name"><xsl:value-of select="name()"/></xsl:with-param>
                            <xsl:with-param name="functionForm" select="cml:parameterised_hh/@type" />
                            <xsl:with-param name="expression"   select="cml:parameterised_hh/@expr" />
                            <xsl:with-param name="A_cml" select="cml:parameterised_hh/cml:parameter[@name='A']/@value"/>
                            <xsl:with-param name="k_cml" select="cml:parameterised_hh/cml:parameter[@name='k']/@value"/>
                            <xsl:with-param name="d_cml" select="cml:parameterised_hh/cml:parameter[@name='d']/@value"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="count(cml:generic_equation_hh) &gt; 0">
            // Found a generic form of rate equation for <xsl:value-of select="name()"/>, using expression: <xsl:value-of select="cml:generic_equation_hh/@expr" />
            // Will translate this for GENESIS compatibility...<xsl:text>
                    </xsl:text>
                    <xsl:if test="string($xmlFileUnitSystem) != string($targetUnitSystem)">
            // Equation (and all ChannelML file values) in <xsl:value-of select="$xmlFileUnitSystem"
            /> but this script in <xsl:value-of select="$targetUnitSystem" /><xsl:text>
            </xsl:text>
            v = v * <xsl:call-template name="convert">
                        <xsl:with-param name="value">1</xsl:with-param>
                        <xsl:with-param name="quantity">InvVoltage</xsl:with-param>
                    </xsl:call-template> // temporarily set v to units of equation...<xsl:text>
            </xsl:text>
                        <xsl:if test="(name()='tau' or name()='inf') and
                                      (contains(string(cml:generic_equation_hh/@expr), 'alpha') or
                                      contains(string(cml:generic_equation_hh/@expr), 'beta'))">
            // Equation depends on alpha/beta, so converting them too...
            alpha = alpha * <xsl:call-template name="convert">
                                <xsl:with-param name="value">1</xsl:with-param>
                                <xsl:with-param name="quantity">Time</xsl:with-param>
                            </xsl:call-template>
            beta = beta * <xsl:call-template name="convert">
                                <xsl:with-param name="value">1</xsl:with-param>
                                <xsl:with-param name="quantity">Time</xsl:with-param>
                            </xsl:call-template> <xsl:text>
            </xsl:text>
                        </xsl:if>

                        <xsl:if test="count(../cml:conc_dependence) &gt; 0">
           // Equation depends on concentration, so converting that too... <xsl:text>
            </xsl:text>
            ca_conc = ca_conc * <xsl:call-template name="convert">
                                <xsl:with-param name="value">1</xsl:with-param>
                                <xsl:with-param name="quantity">InvConcentration</xsl:with-param>
                            </xsl:call-template> <xsl:text>

            </xsl:text>
                        </xsl:if>
                    </xsl:if>
                    <xsl:variable name="newExpression">
                        <xsl:call-template name="formatExpression">
                            <xsl:with-param name="variable">
                                <xsl:value-of select="name()"/>
                            </xsl:with-param>
                            <xsl:with-param name="oldExpression">
                                <xsl:value-of select="cml:generic_equation_hh/@expr" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:variable>
            <xsl:value-of select="$newExpression" /><xsl:text>
            </xsl:text>
                    <xsl:if test="string($xmlFileUnitSystem) != string($targetUnitSystem)">
            v = v * <xsl:call-template name="convert">
                        <xsl:with-param name="value">1</xsl:with-param>
                        <xsl:with-param name="quantity">Voltage</xsl:with-param>
                    </xsl:call-template> // reset v<xsl:text>
            </xsl:text>
                    <xsl:if test="(name()='tau' or name()='inf') and
                                      (contains(string(cml:generic_equation_hh/@expr), 'alpha') or
                                      contains(string(cml:generic_equation_hh/@expr), 'beta'))">
            alpha = alpha * <xsl:call-template name="convert">
                                <xsl:with-param name="value">1</xsl:with-param>
                                <xsl:with-param name="quantity">InvTime</xsl:with-param>
                            </xsl:call-template>  // resetting alpha
            beta = beta * <xsl:call-template name="convert">
                                <xsl:with-param name="value">1</xsl:with-param>
                                <xsl:with-param name="quantity">InvTime</xsl:with-param>
                            </xsl:call-template>  // resetting beta
                        </xsl:if>

                        <xsl:if test="count(../cml:conc_dependence) &gt; 0">
            ca_conc = ca_conc * <xsl:call-template name="convert">
                                <xsl:with-param name="value">1</xsl:with-param>
                                <xsl:with-param name="quantity">Concentration</xsl:with-param>
                            </xsl:call-template> // resetting ca_conc <xsl:text>

            </xsl:text>
                        </xsl:if>
                    </xsl:if>




                    <xsl:if test="(name()='alpha' or name()='beta')
                                   and (string($xmlFileUnitSystem) != string($targetUnitSystem))">
            // Set correct units of <xsl:value-of select="name()"/><xsl:text>
            </xsl:text>
            <xsl:value-of select="name()"/> = <xsl:value-of select="name()"/> * <xsl:call-template name="convert">
                            <xsl:with-param name="value">1</xsl:with-param>
                            <xsl:with-param name="quantity">InvTime</xsl:with-param>
                        </xsl:call-template><xsl:text>

            </xsl:text>
                    </xsl:if>

                    <xsl:if test="name()='tau' and (string($xmlFileUnitSystem) != string($targetUnitSystem))">
            // Set correct units of <xsl:value-of select="name()"/><xsl:text>
            </xsl:text>
            <xsl:value-of select="name()"/> = <xsl:value-of select="name()"/> * <xsl:call-template name="convert">
                            <xsl:with-param name="value">1</xsl:with-param>
                            <xsl:with-param name="quantity">Time</xsl:with-param>
                        </xsl:call-template>
                    </xsl:if>



                    </xsl:when>
                    <xsl:otherwise>
            ? ERROR: Unrecognised form of the rate equation for <xsl:value-of select="name()"/>...

                    </xsl:otherwise>
                </xsl:choose>
                </xsl:if>
            </xsl:for-each> <!-- <xsl:for-each select='cml:transition/*/* ... etc>-->

            <xsl:variable name='tableEntry'>
                <xsl:choose>
                    <xsl:when test="count(cml:transition/cml:voltage_gate) &gt; 0">table[{i}]</xsl:when>
                    <xsl:when test="count(cml:transition/cml:voltage_conc_gate) &gt; 0">table[{i}][{c}]</xsl:when>
                </xsl:choose>
            </xsl:variable>

            <!-- Working out the conversion of alpha and beta to tau & inf-->
            <xsl:choose>
                <xsl:when test="count(cml:transition/cml:voltage_gate/cml:alpha | cml:transition/cml:voltage_conc_gate/cml:alpha)=1 and
                                count(cml:transition/cml:voltage_gate/cml:beta | cml:transition/cml:voltage_conc_gate/cml:beta)=1 and
                                count(cml:transition/cml:voltage_gate/cml:tau | cml:transition/cml:voltage_conc_gate/cml:tau)=0 and
                                count(cml:transition/cml:voltage_gate/cml:inf | cml:transition/cml:voltage_conc_gate/cml:inf)=0">

            // Using the alpha and beta expressions to populate the tables

            float tau = 1/(temp_adj_<xsl:value-of select="$gateName"/> * (alpha + beta))
            <xsl:if test="$consoleOutput='yes'">echo "Tab <xsl:value-of select="$gateRef"/>: v: "{v} ", a: "{alpha} ", b: "{beta} ", tau: "{tau}
                <xsl:if test="count(cml:transition/cml:voltage_conc_gate) &gt; 0">
            echo "Tab <xsl:value-of select="$gateRef"/>: conc: " {<xsl:value-of select="cml:transition/cml:voltage_conc_gate/cml:conc_dependence/@variable_name"/>}
                </xsl:if>
            </xsl:if>
            setfield {chanpath} <xsl:value-of select="$gateRef"/>_A-><xsl:value-of select="$tableEntry"/> {temp_adj_<xsl:value-of select="$gateName"/> * alpha}
            setfield {chanpath} <xsl:value-of select="$gateRef"/>_B-><xsl:value-of select="$tableEntry"/> {temp_adj_<xsl:value-of select="$gateName"/> * (alpha + beta)}
                </xsl:when>
                <xsl:otherwise>

            // Using the tau and inf expressions to populate the tables

                    <xsl:choose>
                        <xsl:when test="count(cml:transition/cml:voltage_gate/cml:tau | cml:transition/cml:voltage_conc_gate/cml:tau)=0">
            float tau = 1/(temp_adj_<xsl:value-of select="$gateName"/> * (alpha + beta))
                        </xsl:when>
                        <xsl:otherwise>
            tau = tau/temp_adj_<xsl:value-of select="$gateName"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="count(cml:transition/cml:voltage_gate/cml:inf | cml:transition/cml:voltage_conc_gate/cml:inf)=0">
            float inf = alpha/(alpha + beta)
                    </xsl:if>

            <xsl:if test="$consoleOutput='yes'">echo "Tab <xsl:value-of select="$gateRef"/>: v: "{v} ", tau: "{tau} ", inf: "{inf}
                <xsl:if test="count(cml:transition/cml:voltage_conc_gate) &gt; 0">
                echo "Tab <xsl:value-of select="$gateRef"/>: conc: " {<xsl:value-of select="cml:transition/cml:voltage_conc_gate/cml:conc_dependence/@variable_name"/>}
                </xsl:if>
            </xsl:if>

            setfield {chanpath} <xsl:value-of select="$gateRef"/>_A-><xsl:value-of select="$tableEntry"/> {tau}

            setfield {chanpath} <xsl:value-of select="$gateRef"/>_B-><xsl:value-of select="$tableEntry"/> {inf}
                </xsl:otherwise>
                </xsl:choose>

            v = v + dv

        end // end of for (i = 0; i &lt;= ({tab_divs}); i = i + 1)

                <xsl:if test="count(cml:transition/cml:voltage_conc_gate) &gt; 0">
            <xsl:value-of select="cml:transition/cml:voltage_conc_gate/cml:conc_dependence/@variable_name"/> = <xsl:value-of
                select="cml:transition/cml:voltage_conc_gate/cml:conc_dependence/@variable_name"/> + dc
        end // end of for (c = 0; c &lt;= ({tab_divs}); c = c + 1)
                </xsl:if>

            <xsl:if test='count(cml:transition/cml:voltage_gate/cml:tau | cml:transition/cml:voltage_conc_gate/cml:tau) &gt; 0 or
                      count(cml:transition/cml:voltage_gate/cml:inf | cml:transition/cml:voltage_conc_gate/cml:inf) &gt; 0'>
        // Using the tau, inf form of rate equations, so tweaking...
        tweaktau {chanpath} <xsl:value-of select="$gateRef"/>
            </xsl:if>

        setfield {chanpath} <xsl:value-of select="$gateRef"/>_A->calc_mode 1 <xsl:value-of select="$gateRef"/>_B->calc_mode 1


        </xsl:for-each> <!-- <xsl:for-each select="cml:hh_gate/[@state=$gateName]"> -->

        </xsl:for-each> <!--<xsl:for-each select='cml:current_voltage_relation/cml:ohmic/cml:conductance/cml:gate'>-->


end

</xsl:template>
<!--End Main template-->




<xsl:template match="cml:ion_concentration">

function make_<xsl:value-of select="@name"/>
        <xsl:if test="count(meta:notes) &gt; 0">

        /*
            <xsl:value-of select="meta:notes"/>
        */
        </xsl:if>

        str chanpath = "/library/<xsl:value-of select="@name"/>"

        if ({exists {chanpath}})
            return
        end<xsl:text>
        </xsl:text>

        <xsl:variable name="ionname"><xsl:value-of select="cml:current_voltage_relation/cml:ohmic/@ion"/></xsl:variable>

        create Ca_concen {chanpath}

        <xsl:if test="count(cml:decaying_pool_model) &gt; 0">

        // Setting params for a decaying_pool_model

        setfield {chanpath} \
            tau               <xsl:call-template name="convert">
                                    <xsl:with-param name="value" select="cml:decaying_pool_model/cml:decay_constant"/>
                                    <xsl:with-param name="quantity">Time</xsl:with-param>
                               </xsl:call-template> \
            Ca_base               <xsl:call-template name="convert">
                                    <xsl:with-param name="value" select="cml:decaying_pool_model/cml:resting_conc"/>
                                    <xsl:with-param name="quantity">Concentration</xsl:with-param>
                               </xsl:call-template>
        </xsl:if>

        <xsl:if test="count(cml:decaying_pool_model/cml:pool_volume_info) &gt; 0">

        setfield {chanpath} \
            thick               <xsl:call-template name="convert">
                                    <xsl:with-param name="value" select="cml:decaying_pool_model/cml:pool_volume_info/cml:shell_thickness"/>
                                    <xsl:with-param name="quantity">Length</xsl:with-param>
                               </xsl:call-template>
        </xsl:if>

end

</xsl:template>




<xsl:template match="cml:synapse_type">

function makechannel_<xsl:value-of select="@name"/>(compartment, name)
        <xsl:if test="count(meta:notes) &gt; 0">
        /*
            <xsl:value-of select="meta:notes"/>
        */
        </xsl:if>
        str compartment
        str name

        if (!({exists {compartment}/{name}}))

            create      synchan               {compartment}/{name}
<xsl:if test="count(cml:doub_exp_syn)>0">
            setfield    ^ \
                    Ek                      <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:doub_exp_syn/@reversal_potential"/></xsl:with-param>
              <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template> \
                    tau1                    <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:doub_exp_syn/@decay_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> \
                    tau2                    <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:doub_exp_syn/@rise_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> \
                    gmax                    <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:doub_exp_syn/@max_conductance"/></xsl:with-param>
              <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>

            addmsg   {compartment}/{name}   {compartment} CHANNEL Gk Ek
            addmsg   {compartment}   {compartment}/{name} VOLTAGE Vm
</xsl:if>

<xsl:if test="count(cml:blocking_syn)>0">
            setfield    ^ \
                    Ek                      <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/@reversal_potential"/></xsl:with-param>
              <xsl:with-param name="quantity">Voltage</xsl:with-param></xsl:call-template> \
                    tau1                    <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/@decay_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> \
                    tau2                    <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/@rise_time"/></xsl:with-param>
              <xsl:with-param name="quantity">Time</xsl:with-param></xsl:call-template> \
                    gmax                    <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/@max_conductance"/></xsl:with-param>
              <xsl:with-param name="quantity">Conductance</xsl:with-param></xsl:call-template>

            addmsg   {compartment}   {compartment}/{name} VOLTAGE Vm

            if (! {exists {compartment}/{name}/Mg_BLOCK})

                float CMg = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/cml:block/@conc"/></xsl:with-param>
              <xsl:with-param name="quantity">Concentration</xsl:with-param></xsl:call-template>

                float eta = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/cml:block/@eta"/></xsl:with-param>
              <xsl:with-param name="quantity">InvConcentration</xsl:with-param></xsl:call-template>

                float gamma = <xsl:call-template name="convert">
              <xsl:with-param name="value"><xsl:value-of select="cml:blocking_syn/cml:block/@gamma"/></xsl:with-param>
              <xsl:with-param name="quantity">InvVoltage</xsl:with-param></xsl:call-template>

                create Mg_block {compartment}/{name}/Mg_BLOCK

                setfield {compartment}/{name}/Mg_BLOCK \
                    CMg {CMg}  \
                    KMg_A {1/eta} \
                    KMg_B {1.0/gamma}

                addmsg  {compartment}/{name}             {compartment}/{name}/Mg_BLOCK   CHANNEL    Gk Ek
                addmsg  {compartment}/{name}/Mg_BLOCK    {compartment}                   CHANNEL    Gk Ek
                addmsg  {compartment}                    {compartment}/{name}/Mg_BLOCK   VOLTAGE    Vm
            end

</xsl:if>

        end

end

</xsl:template>




<!-- Function to return 1 for exponential, 2 for sigmoid, 3 for linoid-->
<xsl:template name="getFunctionForm">
    <xsl:param name="stringFunctionName"/>
    <xsl:choose>
        <xsl:when test="$stringFunctionName = 'exponential'">1</xsl:when>
        <xsl:when test="$stringFunctionName = 'sigmoid'">2</xsl:when>
        <xsl:when test="$stringFunctionName = 'linoid'">3</xsl:when>
    </xsl:choose>
</xsl:template>



<!-- Function to get value converted to proper units.-->
<xsl:template name="convert">
    <xsl:param name="value" />
    <xsl:param name="quantity" />
    <xsl:choose>
        <xsl:when test="$xmlFileUnitSystem  = $targetUnitSystem"><xsl:value-of select="$value"/></xsl:when>
        <xsl:when test="$xmlFileUnitSystem  = 'Physiological Units' and $targetUnitSystem  = 'SI Units'">
            <xsl:choose>
                <xsl:when test="$quantity = 'Conductance Density'"><xsl:value-of select="number($value*10)"/></xsl:when>
                <xsl:when test="$quantity = 'Conductance'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Voltage'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvVoltage'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Time'"><xsl:value-of select="number($value div 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Length'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvTime'"><xsl:value-of select="number($value * 1000)"/></xsl:when>
                <xsl:when test="$quantity = 'Concentration'"><xsl:value-of select="number($value * 1000000)"/></xsl:when>
                <xsl:when test="$quantity = 'InvConcentration'"><xsl:value-of select="number($value div 1000000)"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="number($value)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:when test="$xmlFileUnitSystem  = 'SI Units' and $targetUnitSystem  = 'Physiological Units'">
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
                <xsl:otherwise><xsl:value-of select="number($value)"/></xsl:otherwise>
            </xsl:choose>
        </xsl:when>
        <xsl:when test="$xmlFileUnitSystem  = 'SI Units'">si</xsl:when>
    </xsl:choose>
</xsl:template>



<!-- Function to get equation in GENESIS format-->
<xsl:template name="generateEquation">
    <xsl:param name="name" />
    <xsl:param name="functionForm" />
    <xsl:param name="expression" />
    <xsl:param name="A_cml" />
    <xsl:param name="k_cml" />
    <xsl:param name="d_cml" />
    <xsl:choose>

        <xsl:when test="string-length($functionForm) &gt; 0"> <!-- So not an empty string-->
            // ChannelML form of equation: <xsl:value-of select="$name"/> = <xsl:value-of select="$expression" />, with params:
            // A = <xsl:value-of select="$A_cml"/>, k = <xsl:value-of select="$k_cml" />, d = <xsl:value-of
            select="$d_cml" />, in units: <xsl:value-of select="$xmlFileUnitSystem"/>

            <xsl:choose>
                <xsl:when test="string($name) = 'alpha' or string($name) = 'beta'">
            A = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$A_cml"/></xsl:with-param>
                    <xsl:with-param name="quantity">InvTime</xsl:with-param>
                </xsl:call-template>
                </xsl:when>
                <xsl:when test="string($name) = 'tau'">
            A = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$A_cml"/></xsl:with-param>
                    <xsl:with-param name="quantity">Time</xsl:with-param>
                </xsl:call-template>
                </xsl:when>
                <xsl:when test="string($name) = 'inf'">
            A = <xsl:value-of select="$A_cml"/>
                </xsl:when>
            </xsl:choose>

            k = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$k_cml"/></xsl:with-param>
                    <xsl:with-param name="quantity">InvVoltage</xsl:with-param>
                </xsl:call-template>
            B = 1/k
            V0 = <xsl:call-template name="convert">
                    <xsl:with-param name="value"><xsl:value-of select="$d_cml"/></xsl:with-param>
                    <xsl:with-param name="quantity">Voltage</xsl:with-param>
                </xsl:call-template><xsl:text>
            </xsl:text>

    <xsl:choose>
        <xsl:when test="$functionForm = 'exponential'">

            <xsl:value-of select="$name"/> = A * {exp {(v - V0) / B}}
        </xsl:when>
        <xsl:when test="$functionForm = 'sigmoid'">
            <xsl:value-of select="$name"/> = A / ( {exp {(v - V0) / B}} + 1)
        </xsl:when>
            <xsl:when test="$functionForm = 'linoid'">

            if ( {abs {(v - V0)/ B}} &lt; 1e-6)
                <xsl:value-of select="$name"/> = A * (1 + (v - V0)/B/2)
            else
                <xsl:value-of select="$name"/> = A * ((v - V0) / B) /(1 - {exp {-1 * (v - V0)/B}})
            end

        </xsl:when>
    </xsl:choose>
        </xsl:when>

            <!-- In the case when the info on the gate is missing -->
        <xsl:otherwise>
            <xsl:value-of select="$name"/> = 1 // Gate is not present, power should = 0 so value of <xsl:value-of
                            select="$name"/> is not relevant
        </xsl:otherwise>
    </xsl:choose>
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
    <xsl:variable name="condition"><xsl:value-of select="substring-before($oldExpression,'?')"/></xsl:variable>

            if (<xsl:value-of select="translate($condition,'()','{}')"/>)<xsl:text>
                </xsl:text><xsl:value-of select="$variable"/> = <xsl:value-of select="translate($ifTrue,'()','{}')"/>
            else<xsl:text>
                </xsl:text><xsl:value-of select="$variable"/> = <xsl:value-of select="translate($ifFalse,'()','{}')"/>
            end
        </xsl:when>
        <xsl:otherwise>
    <xsl:value-of select="$variable"/> = <xsl:value-of select="translate($oldExpression,'()','{}')"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
