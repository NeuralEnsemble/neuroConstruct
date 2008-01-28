/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.neuroml;

import java.io.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.xml.*;



/**
 * ChannelML file format. Defines tags needed in ChannelML files...
 *
 * @author Padraig Gleeson
 *  
 */

public class ChannelMLConstants
{

    public static String ROOT_ELEMENT = "channelml";

    public static String NAMESPACE_URI = "http://morphml.org/channelml/schema";

    public static String DEFAULT_FILE_EXTENSION = "xml";

    public static String DEFAULT_SCHEMA_EXTENSION = "xsd";

    public static String DEFAULT_MAPPING_EXTENSION = "xsl";

    public static String DEFAULT_SCHEMA_FILENAME = "ChannelML."+DEFAULT_SCHEMA_EXTENSION;


    public static String PREFIX = "cml";



    public static String UNIT_SCHEME = "units";

    public static String SI_UNITS = "SI Units";

    public static String PHYSIOLOGICAL_UNITS = "Physiological Units";

    public static String XSL_ROOT = "stylesheet";

    //public static String XSL_VARIABLES = XSL_ROOT + "/variable";

    public static String XSL_TARGET_UNITS_ELEMENT = XSL_ROOT + "/variable[3]";

    public static String XSL_TARGET_UNITS_ATTR = "targetUnitSystem";

    public static String NON_SPECIFIC_ION_NAME = "non_specific";

    public static String ION_ELEMENT = "ion";

    public static String ION_NAME_ATTR = "name";

    public static String ION_ROLE_ATTR = "role";

    public static String ION_ROLE_PERMEATED = "PermeatedSubstance";
    public static String ION_ROLE_PERMEATED_FIXED_REV_POT = "PermeatedSubstanceFixedRevPot";
    public static String ION_ROLE_MODULATING = "ModulatingSubstance";
    public static String ION_ROLE_SIGNALLING = "SignallingSubstance";


    public static String ION_ROLE_PERMEATED_v1_2 = "Transmitted";
    public static String ION_ROLE_MODULATING_v1_2 = "RateDependence";
    public static String ION_ROLE_SIGNALLING_v1_2 = "ConcVaries";



    public static String NOTES_ELEMENT = "notes";

    public static String DEFAULT_COND_DENSITY_ATTR = "default_gmax";

    public static String ION_REVERSAL_POTENTIAL_ATTR = "default_erev";

    public static String CHAN_TYPE_ELEMENT = "channel_type";

    public static String SYN_TYPE_ELEMENT = "synapse_type";

    public static String DOUB_EXP_SYN_ELEMENT = "doub_exp_syn";
    public static String BLOCKING_SYN_ELEMENT = "blocking_syn";

    public static String DES_MAX_COND_ATTR = "max_conductance";
    public static String DES_RISE_TIME_ATTR = "rise_time";
    public static String DES_DECAY_TIME_ATTR = "decay_time";
    public static String DES_REV_POT_ATTR = "reversal_potential";


    public static String ION_CONC_ELEMENT = "ion_concentration";
    

    public static String ION_CONC_DEC_POOL_ELEMENT = "decaying_pool_model";
    public static String ION_CONC_FIXED_POOL_ELEMENT = "fixed_pool_info";
    public static String ION_CONC_FIXED_POOL_PHI_ELEMENT = "phi";

    public static String CURR_VOLT_REL_ELEMENT = "current_voltage_relation";

    
    public static String I_AND_F_ELEMENT = "integrate_and_fire";

    public static String OHMIC_ELEMENT = "ohmic";

    public static String OHMIC_ION_ATTR = "ion";



    public static String CONDUCTANCE_ELEMENT = "conductance";

    public static String GATE_ELEMENT = "gate";

    public static String STATE_ELEMENT = "state";

    public static String STATE_NAME_ELEMENT = "name";

    public static String NAME_ATTR = "name";

    public static String STATUS_ELEMENT = "status";
    public static String STATUS_VALUE_ATTR = "value";

    public static String STATUS_VALUE_ATTR_STABLE = "stable";
    public static String STATUS_VALUE_ATTR_IN_PROGRESS = "in_progress";
    public static String STATUS_VALUE_ATTR_KNOWN_ISSUES = "known_issues";


    public static String HH_GATE_ELEMENT = "hh_gate";

    public static String HH_GATE_STATE_ATTR = "state";

    public static String TRANSITION_ELEMENT = "transition";
    public static String VOLTAGE_GATE_ELEMENT = "voltage_gate";
    public static String VOLTAGE_CONC_GATE_ELEMENT = "voltage_conc_gate";

    public static String CONC_DEP_ELEMENT = "conc_dependence";
    public static String CONC_DEP_NAME_ATTR = "name";
    public static String CONC_DEP_ION_ATTR = "ion";
    public static String CONC_DEP_VAR_NAME_ATTR = "variable_name";
    public static String CONC_DEP_MIN_CONC_ATTR = "min_conc";
    public static String CONC_DEP_MAX_CONC_ATTR = "max_conc";

    public static String ALPHA_ELEMENT = "alpha";
    public static String BETA_ELEMENT = "beta";


    public static String PARAMETERISED_HH_ELEMENT = "parameterised_hh";
    public static String PARAMETERISED_HH_TYPE_ATTR = "type";

    public static String GENERIC_HH_ELEMENT = "generic_equation_hh";
    public static String GENERIC_HH_EXPR_ATTR = "expr";

    public static String PARAMETER_ELEMENT = "parameter";
    public static String PARAMETER_NAME_ATTR = "name";
    public static String PARAMETER_VALUE_ATTR = "value";


    public static String LINOID_TYPE = "linoid";
    public static String EXPONENTIAL_TYPE = "exponential";
    public static String SIGMOID_TYPE = "sigmoid";



    public static String getUnitsXPath()
    {
        return ChannelMLConstants.ROOT_ELEMENT +"/@"+ChannelMLConstants.UNIT_SCHEME;
    }

    public static String getChannelTypeXPath()
    {
        return ChannelMLConstants.ROOT_ELEMENT + "/" + CHAN_TYPE_ELEMENT;
    }

    public static String getSynapseTypeXPath()
    {
        return ChannelMLConstants.ROOT_ELEMENT + "/" + SYN_TYPE_ELEMENT;
    }


    public static String getIonConcTypeXPath()
    {
        return ChannelMLConstants.ROOT_ELEMENT + "/" + ION_CONC_ELEMENT;
    }
    
    public static String getIonConcDecPoolXPath()
    {
        return getIonConcTypeXPath() + "/" + ION_CONC_DEC_POOL_ELEMENT;
    }
    
    public static String getIonConcFixedPoolXPath()
    {
        return getIonConcDecPoolXPath() + "/" + ION_CONC_FIXED_POOL_ELEMENT;
    }

    
    public static String getIonConcFixedPoolPhiXPath()
    {
        return getIonConcFixedPoolXPath() + "/" + ION_CONC_FIXED_POOL_PHI_ELEMENT;
    }



    public static String getChannelNameXPath()
    {
        return getChannelTypeXPath()+ "/@" +
            ChannelMLConstants.NAME_ATTR;

    }

    public static String getChannelStatusXPath()
    {
        return getChannelTypeXPath()+ "/" +
            ChannelMLConstants.STATUS_ELEMENT;

    }


    public static String getChannelStatusValueXPath()
    {
        return getChannelStatusXPath()+ "/@" +
            ChannelMLConstants.STATUS_VALUE_ATTR;

    }

    public static String getSynapseStatusXPath()
    {
        return getSynapseTypeXPath()+ "/" +
            ChannelMLConstants.STATUS_ELEMENT;

    }


    public static String getSynapseStatusValueXPath()
    {
        return getSynapseStatusXPath()+ "/@" +
            ChannelMLConstants.STATUS_VALUE_ATTR;

    }

    public static String getIonConcStatusXPath()
    {
        return getIonConcTypeXPath()+ "/" +
            ChannelMLConstants.STATUS_ELEMENT;

    }


    public static String getIonConcStatusValueXPath()
    {
        return getIonConcStatusXPath()+ "/@" +
            ChannelMLConstants.STATUS_VALUE_ATTR;

    }



    public static String getSynapseNameXPath()
    {
        return getSynapseTypeXPath() + "/@" +
            ChannelMLConstants.NAME_ATTR;

    }

    public static String getIonConcNameXPath()
    {
        return getIonConcTypeXPath() + "/@" +
            ChannelMLConstants.NAME_ATTR;

    }


    public static String getMainNotesXPath()
    {
        return ChannelMLConstants.ROOT_ELEMENT + "/" + NOTES_ELEMENT;
    }

    public static String getFirstChannelNotesXPath()
    {
        return getChannelTypeXPath() + "/" + NOTES_ELEMENT;
    }

    public static String getHHGateXPath()
    {
        return getChannelTypeXPath() + "/" + HH_GATE_ELEMENT;
    }


    public static String getFirstSynapseNotesXPath()
    {
        return getSynapseTypeXPath() + "/" + NOTES_ELEMENT;
    }

    public static String getFirstIonConcNotesXPath()
    {
        return getIonConcTypeXPath() + "/" + NOTES_ELEMENT;
    }



    public static String getIonsXPath()
    {
        return ChannelMLConstants.ROOT_ELEMENT + "/" + ION_ELEMENT;
    }



    public static String getIonXPath(int index)
    {
        return getIonsXPath() + "["+index+"]";
    }

    public static String getIonRevPotXPath(int index)
    {
        return getIonXPath(index)+"/@"+ ION_REVERSAL_POTENTIAL_ATTR;
    }




    public static String getCurrVoltRelXPath()
    {
        return getChannelTypeXPath() + "/" + CURR_VOLT_REL_ELEMENT;
    }

    public static String getOhmicXPath()
    {
        return getCurrVoltRelXPath() + "/" + OHMIC_ELEMENT;
    }

    public static String getIandFXPath()
    {
        return getCurrVoltRelXPath() + "/" + I_AND_F_ELEMENT;
    }

    public static String getConductanceXPath()
    {
        return getOhmicXPath() + "/" + CONDUCTANCE_ELEMENT;
    }

    public static String getCondDensXPath()
    {
        return getConductanceXPath() + "/@" + DEFAULT_COND_DENSITY_ATTR;
    }


    public static String getGateXPath(int index)
    {
        return getConductanceXPath() + "/" + GATE_ELEMENT+"["+index+"]";
    }







    public static void main(String args[])
    {
        System.out.println("Trying out SimpleXMLReader...");
        try
        {

            File cml = new File("templates/xmlTemplates/Examples/ChannelML/GenericChannel.xml");

            File xsl = new File("../NeuroMLValidator/web/NeuroMLFiles/Schemata/v1.1/Level2/ChannelML_v1.1_GENESIStab.xsl");
            //File xsl = new File("templates/xmlTemplates/Schemata/v1.1/Level2/ChannelML_v1.1_GENESIStab.xsl");

            SimpleXMLDocument xslDoc = SimpleXMLReader.getSimpleXMLDoc(xsl);

            File tempFile = new File("../temp/cml.xsl");

            FileWriter fw = new FileWriter(tempFile);

            fw.write(xslDoc.getXMLString("", false));

            fw.close();

            String transformed = XMLUtils.transform(cml, xsl);

            System.out.println("Transformed before: ");
            System.out.println(transformed);

            System.out.println("Docread: ");
            System.out.println(xslDoc.getXMLString("", false));
            String transformed2 = XMLUtils.transform(cml, tempFile);

            System.out.println("Transformed after: ");
            System.out.println(transformed2);




/*
            String xpath1 = "channelml/channel_type/current_voltage_relation/ohmic/conductance";

            SimpleXMLEntity[] entities = doc.getXMLEntities(xpath1);

            for (int j = 0; j < entities.length; j++)
            {
                System.out.println("Entity "+j+" at "+ xpath1 +": "+entities[j]);

                if (entities[j] instanceof SimpleXMLElement)
                {
                    ArrayList<String> locs = ((SimpleXMLElement)entities[j]).getXPathLocations();
                    //ArrayList<String> locs = doc.getXPathLocations();

                    for (int k = 0; k < locs.size(); k++)
                    {
                        //System.out.println("Subelement : " + locs.get(k) + " = " + doc.getValueByXPath(locs.get(k)));
                        System.out.println("Subelement : " + locs.get(k) + " = " + ((SimpleXMLElement)entities[j]).getValueByXPath(locs.get(k)));
                    }
                }
            }

*/
            //System.out.println("Value of "+xpath1+": "+doc.getValuebyXPath(xpath1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
