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
                return "Full Cell v2.0";
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
        NEUROML_VERSION_2
        {
            @Override
            public String toString()
            {
                return "2.x";
            }
        };

        public boolean isVersion2()
        {
            return this.equals(NEUROML_VERSION_2);
        }
    };




    public static String ROOT_ELEMENT = "neuroml";

    public static String NAMESPACE_URI = "http://morphml.org/neuroml/schema";

    public static String NAMESPACE_URI_VERSION_2 = "http://www.neuroml.org/schema/neuroml2";

    public static String DEFAULT_SCHEMA_LOCATION = "http://neuroml.svn.sourceforge.net/viewvc/neuroml/trunk/web/NeuroMLFiles/Schemata/v1.8.1/Level3/NeuroML_Level3_v1.8.1.xsd";

    public static String DEFAULT_SCHEMA_FILENAME_VERSION_2 = "http://neuroml.svn.sourceforge.net/viewvc/neuroml/DemoVer2.0/lems/Schemas/NeuroML2/NeuroML_v2alpha.xsd";

    public static String NEUROML_ID_V2 = "id";

    /**
     * General constants used in NeuroML/MorphML
     */

    public static String XSI_PREFIX = "xsi";

    public static String XML_NS = "xmlns";

    public static String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

    public static String XSI_SCHEMA_LOC = "xsi:schemaLocation";

    private static final String prefixNeuroML2Types = "NeuroML2CoreTypes/";

    public static String NEUROML2_CORE_TYPES_UNITS = prefixNeuroML2Types+"NeuroMLCoreDimensions.xml";
    public static String NEUROML2_CORE_TYPES_CELLS = prefixNeuroML2Types+"Cells.xml";
    public static String NEUROML2_CORE_TYPES_CHANNELS = prefixNeuroML2Types+"Channels.xml";
    public static String NEUROML2_CORE_TYPES_SYNAPSES = prefixNeuroML2Types+"Synapses.xml";
    public static String NEUROML2_CORE_TYPES_INPUTS = prefixNeuroML2Types+"Inputs.xml";
    public static String NEUROML2_CORE_TYPES_NETWORKS = prefixNeuroML2Types+"Networks.xml";

    public static String NEUROML2_CORE_TYPES_SIMULATION = prefixNeuroML2Types+"Simulation.xml";

    public static String NEUROML2_ABST_CELL = "abstractCell";
    public static String NEUROML2_ABST_CELL_MEMB_POT = "abstractCellMembPot";
    public static String NEUROML2_ABST_CELL_MEMB_POT_CAP = "abstractCellMembPotCap";
    
    public static String NEUROML2_ABST_CELL_MEMB_POT_CAP__C = "C";

    

}
