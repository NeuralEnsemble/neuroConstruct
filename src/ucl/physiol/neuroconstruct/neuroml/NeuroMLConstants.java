/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 *
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.neuroml;




/**
 * NeuroML constants. Defines tags needed in NeuroML files...
 *
 * @author Padraig Gleeson
 *
 */

public class NeuroMLConstants
{
    public enum NeuroMLLevel
    {
        NEUROML_LEVEL_1
        {
            @Override
            public String toString()
            {
                return "Level 1";
            }
        },
        NEUROML_LEVEL_2
        {
            @Override
            public String toString()
            {
                return "Level 2";
            }
        },
        NEUROML_LEVEL_3
        {
            @Override
            public String toString()
            {
                return "Level 3";
            }
        },
        NEUROML_VERSION_2_SPIKING_CELL
        {
            @Override
            public String toString()
            {
                return "Full Cell v"+NeuroMLVersion.getLatestVersion();
            }
        }
    };


    public enum NeuroMLVersion
    {
        NEUROML_VERSION_1
        {
            @Override
            public String toString()
            {
                return "1.x";
            }
        },
        NEUROML_VERSION_2_ALPHA
        {
            @Override
            public String toString()
            {
                return "2alpha";
            }
        },
        NEUROML_VERSION_2_BETA
        {
            @Override
            public String toString()
            {
                return "2beta";
            }
        },
        NEUROML_VERSION_2_BETA1
        {
            @Override
            public String toString()
            {
                return "2beta1";
            }
        },
        NEUROML_VERSION_2_BETA2
        {
            @Override
            public String toString()
            {
                return "2beta2";
            }
        },
        NEUROML_VERSION_2_BETA3
        {
            @Override
            public String toString()
            {
                return "2beta3";
            }
        },
        NEUROML_VERSION_2_BETA4
        {
            @Override
            public String toString()
            {
                return "2beta4";
            }
        },
        NEUROML_VERSION_2_BETA5
        {
            @Override
            public String toString()
            {
                return "2beta5";
            }
        },
        NEUROML_VERSION_2_0
        {
            @Override
            public String toString()
            {
                return "2.0";
            }
        },
        NEUROML_VERSION_2_1
        {
            @Override
            public String toString()
            {
                return "2.1";
            }
        },
        NEUROML_VERSION_2_2
        {
            @Override
            public String toString()
            {
                return "2.2";
            }
        },
        NEUROML_VERSION_2_3
        {
            @Override
            public String toString()
            {
                return "2.3";
            }
        },
        NEUROML_VERSION_2_3_1
        {
            @Override
            public String toString()
            {
                return "2.3.1";
            }
        };

        public static NeuroMLVersion getLatestVersion()
        {
            return NEUROML_VERSION_2_3_1;
        }

        public boolean isVersion2()
        {
            return !isVersion1();
        }

        public boolean isVersion2alpha()
        {
            return this.equals(NEUROML_VERSION_2_ALPHA);
        }

        public boolean isVersion2Latest()
        {
            return this.equals(getLatestVersion());
        }

        public boolean isVersion2betaOrLater()
        {
            return !this.equals(NEUROML_VERSION_1) && !this.equals(NEUROML_VERSION_2_ALPHA);
        }

        public boolean isVersion1()
        {
            return this.equals(NEUROML_VERSION_1);
        }
    };




    public static String ROOT_ELEMENT = "neuroml";

    public static String NAMESPACE_URI = "http://morphml.org/neuroml/schema";

    public static String NAMESPACE_URI_VERSION_2 = "http://www.neuroml.org/schema/neuroml2";

    public static String DEFAULT_SCHEMA_LOCATION = "http://www.neuroml.org/NeuroMLValidator/NeuroMLFiles/Schemata/v1.8.1/Level3/NeuroML_Level3_v1.8.1.xsd";

    public static String NEUROML_ID_V2 = "id";

    public static String NEUROML_COMP_TYPE_ATTR = "type";

    /**
     * General constants used in NeuroML/MorphML
     */

    public static String XSI_PREFIX = "xsi";

    public static String XML_NS = "xmlns";

    public static String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    public static String XSI_SCHEMA_LOC = "xsi:schemaLocation";

    public static String prefixNeuroML2Types = "NeuroML2CoreTypes/";

    public static String NEUROML2_CORE_TYPES_UNITS_DEF = "NeuroMLCoreDimensions.xml";
    public static String NEUROML2_CORE_TYPES_CELLS_DEF = "Cells.xml";
    public static String NEUROML2_CORE_TYPES_CHANNELS_DEF = "Channels.xml";
    public static String NEUROML2_CORE_TYPES_SYNAPSES_DEF = "Synapses.xml";
    public static String NEUROML2_CORE_TYPES_INPUTS_DEF = "Inputs.xml";
    public static String NEUROML2_CORE_TYPES_NETWORKS_DEF = "Networks.xml";

    public static String NEUROML2_CORE_TYPES_SIMULATION_DEF = "Simulation.xml";

    public static String NEUROML2_CORE_TYPES_PYNN_DEF = "PyNN.xml";

    public static String NEUROML2_ABST_CELL = "baseCell";
    public static String NEUROML2_ABST_CELL_MEMB_POT = "baseCellMembPot";
    public static String NEUROML2_ABST_CELL_MEMB_POT_CAP = "baseCellMembPotCap";

    public static String NEUROML2_ABST_CELL_MEMB_POT_CAP__C = "C";


    public static String NEUROML2_PYNN_IF_CURR_ALPHA = "IF_curr_alpha";
    public static String NEUROML2_PYNN_IF_CURR_EXP = "IF_curr_exp";
    public static String NEUROML2_PYNN_IF_COND_ALPHA = "IF_cond_alpha";
    public static String NEUROML2_PYNN_IF_COND_EXP = "IF_cond_exp";

    public static String NEUROML2_PYNN_EIF_COND_EXP = "EIF_cond_exp_isfa_ista";
    public static String NEUROML2_PYNN_EIF_CURR_ALPHA = "EIF_cond_alpha_isfa_ista";
    public static String NEUROML2_PYNN_HH_COND_EXP = "HH_cond_exp";

    public static String NEUROML2_NEUROLEX_ID = "neuroLexId";

    public static String NEUROML2_NEUROLEX_SOMA_GROUP = "GO:0043025";
    public static String NEUROML2_NEUROLEX_DENDRITE_GROUP = "GO:0030425";
    public static String NEUROML2_NEUROLEX_AXON_GROUP = "GO:0030424";

    /*
    * This is an initial attempt to tag a subset of a cell's segment groups as
    * the non overlapping groups which correspond to the "cables" of MorphML
    */
    public static String NEUROML2_NEUROLEX_UNBRANCHED_NONOVERLAPPING_SEG_GROUP = "sao864921383";




}
