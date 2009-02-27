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

package test;

import org.junit.runner.*;
import org.junit.runner.notification.*;

/**
 *
 * @author padraig
 */
public class MainTest 
{


    public static void main(String[] args)
            
    {
        System.out.println("Running the main nC tests...");
        
        Result r = null;
        
        r = org.junit.runner.JUnitCore.runClasses(/*ucl.physiol.neuroconstruct.cell.CellSuite.class,*/
                 
                ucl.physiol.neuroconstruct.cell.VariableParameterTest.class,
                ucl.physiol.neuroconstruct.cell.VariableMechanismTest.class,
                ucl.physiol.neuroconstruct.cell.SectionTest.class,
                ucl.physiol.neuroconstruct.cell.ParameterisedGroupTest.class,
                ucl.physiol.neuroconstruct.cell.CellTest.class,
                ucl.physiol.neuroconstruct.cell.SegmentTest.class,
                ucl.physiol.neuroconstruct.cell.converters.MorphMLReaderTest.class,
                ucl.physiol.neuroconstruct.genesis.GenesisFileManagerTest.class, 
                ucl.physiol.neuroconstruct.neuron.NeuronFileManagerTest.class,
                ucl.physiol.neuroconstruct.neuroml.NetworkMLReaderTest.class,
                ucl.physiol.neuroconstruct.neuroml.Level3ExportTest.class,
                ucl.physiol.neuroconstruct.neuroml.hdf5.NetworkMLReaderTest.class,
                ucl.physiol.neuroconstruct.project.InputsTest.class,
                ucl.physiol.neuroconstruct.project.ElecInputGeneratorTest.class,
                ucl.physiol.neuroconstruct.project.ConnSpecificPropsTest.class,
                ucl.physiol.neuroconstruct.project.ProjectStructureTest.class,
                ucl.physiol.neuroconstruct.project.ProjectTest.class,
                ucl.physiol.neuroconstruct.project.MorphBasedConnGeneratorTest.class,
                ucl.physiol.neuroconstruct.project.ExtendedNetworkGeneratorTest.class,
                ucl.physiol.neuroconstruct.project.packing.OneDimRegSpacingPackingAdapterTest.class,
                ucl.physiol.neuroconstruct.utils.NumberGeneratorTest.class,
                ucl.physiol.neuroconstruct.utils.equation.ExpressionTest.class/**/); 
        
        
        System.out.println("Finished the main nC tests.");
        
        checkResults(r);

    }
    
    public static void checkResults(Result r)
    {
        if (!r.wasSuccessful())
        {
            System.out.println("");
            System.out.println("**********************************************");
            System.out.println("      Number of failures: "+r.getFailures().size());
            System.out.println("**********************************************");
            System.out.println("");

            for (Failure f: r.getFailures())
            {
                System.out.println("Failure: "+f.getDescription());
                System.out.println("Exception: "+f.getMessage());
                System.out.println("Trace: "+f.getTrace());
            }

            if (!r.wasSuccessful()) System.exit(-1);
        }
        else
        {
            System.out.println("");
            System.out.println("*******************************");
            System.out.println("*****      Success!!!     *****");
            System.out.println("*******************************");
        }
    }
}