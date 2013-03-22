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
 * Defines tags needed in LEMS files...
 *
 * @author Padraig Gleeson
 *  
 */

public class LemsConstants
{

    public static String ROOT_LEMS = "Lems";

    public static String ID_ATTR = "id";

    public static String DEFAULT_SIM_ID = "sim1";

    public static String DEFAULT_RUN_ELEMENT = "DefaultRun";
    public static String TARGET_ELEMENT = "Target";

    public static String COMPONENT_ATTR = "component";


    public static String INCLUDE_ELEMENT = "Include";

    public static String FILE_ATTR = "file";


    public static String SIMULATION_ELEMENT = "Simulation";
    public static String LENGTH_ATTR = "length";
    public static String STEP_ATTR = "step";
    public static String TARGET_ATTR = "target";
    public static String REPORT_FILE_ATTR = "reportFile";
    public static String TIMES_FILE_ATTR = "timesFile";

    public static String DISPLAY_ELEMENT = "Display";
    public static String TITLE_ATTR = "title";
    public static String TIMESCALE_ATTR = "timeScale";

    public static String LINE_ELEMENT = "Line";
    public static String QUANTITY_ATTR = "quantity";
    public static String SCALE_ATTR = "scale";
    public static String COLOR_ATTR = "color";
    public static String SAVE_ATTR = "save";
    


    public enum LemsOption
    {
        NONE
        {
            @Override
            public String toString()
            {
                return "Do nothing";
            }
        },
        EXECUTE_MODEL
        {
            @Override
            public String toString()
            {
                return "Run with LEMS Interpreter";
            }
        },
        GENERATE_GRAPH
        {
            @Override
            public String toString()
            {
                return "Generate graph from network structure";
            }
        },
        GENERATE_NINEML
        {
            @Override
            public String toString()
            {
                return "Generate NineML equivalent";
            }
        },
        GENERATE_NEURON
        {
            @Override
            public String toString()
            {
                return "Generate NEURON scripts";
            }
        };

        public boolean doSomething()
        {
            return !this.equals(NONE);
        }

    };




}
