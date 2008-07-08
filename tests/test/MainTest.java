/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import org.junit.runner.*;
import org.junit.runner.notification.*;

/**
 *
 * @author padraig
 */
public class MainTest {




    public static void main(String[] args)
            
    {
        System.out.println("Running the main nC tests...");
        
        Result r = org.junit.runner.JUnitCore.runClasses(/*ucl.physiol.neuroconstruct.cell.CellSuite.class, */
                
                ucl.physiol.neuroconstruct.cell.CellTest.class,
                ucl.physiol.neuroconstruct.cell.converters.MorphMLReaderTest.class,
                
                ucl.physiol.neuroconstruct.genesis.GenesisFileManagerTest.class,
                ucl.physiol.neuroconstruct.neuron.NeuronFileManagerTest.class,
                
                ucl.physiol.neuroconstruct.cell.SectionTest.class,
                ucl.physiol.neuroconstruct.cell.SegmentTest.class,
                ucl.physiol.neuroconstruct.utils.NumberGeneratorTest.class,
                ucl.physiol.neuroconstruct.project.ElecInputGeneratorTest.class,
                ucl.physiol.neuroconstruct.neuroml.hdf5.NetworkMLReaderTest.class,
                ucl.physiol.neuroconstruct.project.ConnSpecificPropsTest.class,
                ucl.physiol.neuroconstruct.project.ProjectStructureTest.class,
                ucl.physiol.neuroconstruct.project.ProjectTest.class,
                ucl.physiol.neuroconstruct.project.MorphBasedConnGeneratorTest.class/**/); 
        
        
        System.out.println("Finished the main nC tests. Was successful: "+r.wasSuccessful());
        
        if (!r.wasSuccessful())
        {
            System.out.println("Number of failures: "+r.getFailures().size());

            for (Failure f: r.getFailures())
            {
                System.out.println("Failure: "+f.getDescription());
                System.out.println("Exception: "+f.getMessage());
                System.out.println("Trace: "+f.getTrace());
            }

            if (!r.wasSuccessful()) System.exit(-1);
        }
    }
}