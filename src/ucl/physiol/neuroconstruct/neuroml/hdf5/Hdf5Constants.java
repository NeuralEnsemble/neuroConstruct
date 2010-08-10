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

package ucl.physiol.neuroconstruct.neuroml.hdf5;

import java.io.File;
import ucl.physiol.neuroconstruct.gui.plotter.PlotterFrame;




/**
 * Constants for reading standard HDF5 files. Subject to change
 *
 * @author Padraig Gleeson
 *
 */


public class Hdf5Constants
{

    public static final String NEUROCONSTRUCT_POPULATION = "Population"; // As this is the preferred term in NeuroML...

    public static final String NEUROCONSTRUCT_VARIABLE = "Variable"; // As this is the preferred term in NeuroML...


    public static final String NEUROCONSTRUCT_COLUMN_PREFIX = "column_";
    public static final String NEUROCONSTRUCT_CELL_NUM_PREFIX = "cellNum_";

    

    public static final String NEUROSAGE_TRACE_TYPE = "Type";

    public static final String NEUROSAGE_TRACE_TYPE_WAVEFORM = "Waveform";

    public static final String NEUROSAGE_TRACE_SAMPLING_RATE = "Sampling Rate";

    public static final String NEUROSAGE_TRACE_DATA_AXIS = "Data Axis";

    public static final String NEUROSAGE_TRACE_DATA_UNIT = "Data Unit";

    public static final String NEUROSAGE_TRACE_TRANSFORM_TYPE = "Transform.Type";
    public static final String NEUROSAGE_TRACE_TRANSFORM_TYPE_LINEAR = "Linear";

    public static final String NEUROSAGE_TRACE_TRANSFORM_SCALE = "Transform.Scale";
    public static final String NEUROSAGE_TRACE_TRANSFORM_OFFSET = "Transform.Offset";

    public static void main(String[] args)
    {
        File f = new File("../nC_projects/h5/simulations/sim_4");
        f = new File("../nC_projects/h5/simulations/hdf5Big");
        f = new File("testProjects/TestHDF5/simulations/TestH5");
        
        PlotterFrame.addHDF5DataSets(f, null);
    }

}
