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

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.xml.sax.SAXException;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;

/**
 *
 * @author padraig
 */
public class MorphMLReaderTest {

    static final String projName = "TestMorphs";
    static File projDir = new File("testProjects/"+ projName);
        
    ProjectManager pm = null;
    
    public MorphMLReaderTest() {
    }

    @BeforeClass
    public static void cleanUp()
    {
        System.out.println("---------------   cleanUp() MorphMLReaderTest");
        File generateDir = new File(projDir, "generatedNeuroML");
        GeneralUtils.removeAllFiles(generateDir, false, false, true);
    }

    @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() MorphMLReaderTest");        
        
        File projFile = ProjectStructure.findProjectFile(projDir);
        
        pm = new ProjectManager();
        
        try 
        {
            pm.loadProject(projFile);
        
            System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());

            
        } 
        catch (ProjectFileParsingException ex) 
        {
            fail("Error loading: "+ projFile.getAbsolutePath());
        }
    }

    @Test public void testWriteAndReadLevel1() throws MorphologyException, SAXException, IOException
    {
        doWriteAndRead(NeuroMLLevel.NEUROML_LEVEL_1);
    }

    @Test public void testWriteAndReadLevel2() throws MorphologyException, SAXException, IOException
    {
        doWriteAndRead(NeuroMLLevel.NEUROML_LEVEL_2);
    }

    @Test public void testWriteAndReadLevel3() throws MorphologyException, SAXException, IOException
    {
        doWriteAndRead(NeuroMLLevel.NEUROML_LEVEL_3);
    }

    public void doWriteAndRead(NeuroMLLevel level) throws MorphologyException, SAXException, IOException
    {
        System.out.println("---  testWriteAndRead...");

        Cell cell1 = pm.getCurrentProject().cellManager.getCell("SampleCell_ca");
        ////Cell cell1 = pm.getCurrentProject().cellManager.getCell("Granule_98");
        //Cell cell1 = pm.getCurrentProject().cellManager.getCell("SimpleHH");
        
        cell1.setCellDescription("This is\na test\n...");

        System.out.println("PGs: "+ cell1.getParameterisedGroups());
        
        MorphMLConverter mmlC = new MorphMLConverter();
        
        File savedNeuroMLDir = ProjectStructure.getNeuroMLDir(projDir);

        int[] units = new int[]{UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS, UnitConverter.GENESIS_SI_UNITS};

        for(int unit: units)
        { 
            File morphFile = new File(savedNeuroMLDir, "TestNeuroMLv1__u"+unit+"_"+level.toString().replace(" ", "")+".xml");

            MorphMLConverter.setPreferredExportUnits(unit);
            
            MorphMLConverter.saveCellInNeuroMLFormat(cell1, pm.getCurrentProject(), morphFile,
                level, NeuroMLVersion.NEUROML_VERSION_1);

            assertTrue(morphFile.exists());

            System.out.println("Saved cell in NeuroML "+level+" file: "+ morphFile.getAbsolutePath());

            File schemaFile = GeneralProperties.getNeuroMLSchemaFile();

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            System.out.println("Found the XSD file: " + schemaFile.getAbsolutePath());

            Source schemaFileSource = new StreamSource(schemaFile);
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(morphFile);

            validator.validate(xmlFileSource);

            System.out.println("File is valid "+level+" NeuroML v1.x!");

            Cell cell2 = mmlC.loadFromMorphologyFile(morphFile, cell1.getInstanceName());

            if (level.equals(NeuroMLLevel.NEUROML_LEVEL_1)){
                cell1.removeAllBiophysics();
                cell2.removeAllBiophysics(); // remove default axRes etc
            }

            if (level.equals(NeuroMLLevel.NEUROML_LEVEL_1) || level.equals(NeuroMLLevel.NEUROML_LEVEL_2)){
                cell1.removeAllSynapseInfo();
                cell2.removeAllSynapseInfo(); // remove default axRes etc
            }

            String compare = CellTopologyHelper.compare(cell1, cell2, false);

            System.out.println("Comparison for "+level+": "+ compare);

            System.out.println("Chans 1: "+ cell1.getChanMechsVsGroups());
            System.out.println("Chans 2: "+ cell2.getChanMechsVsGroups());

            assertTrue(compare.indexOf(CellTopologyHelper.CELLS_ARE_IDENTICAL)>=0);

            System.out.println("Reloaded file and cells are identical");

        }
        
    }


    @Test public void testWriteNeuroML2() throws MorphologyException, SAXException, IOException
    {
        System.out.println("---  testWriteNeuroML2...");

        Cell cell1 = pm.getCurrentProject().cellManager.getCell("Granule_98");

        cell1.setCellDescription("This is NeuroML2...");

        System.out.println(CellTopologyHelper.printDetails(cell1, pm.getCurrentProject()));

        MorphMLConverter mmlC = new MorphMLConverter();

        File savedNeuroMLDir = ProjectStructure.getNeuroMLDir(projDir);

        int[] units = new int[]{UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS, UnitConverter.GENESIS_SI_UNITS};

        for(int unit: units)
        {
            MorphMLConverter.setPreferredExportUnits(unit);

            File morphFile = new File(savedNeuroMLDir, "TestNeuroMLv2__"+unit+".xml");

            MorphMLConverter.saveCellInNeuroMLFormat(cell1, pm.getCurrentProject(), morphFile,
                NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL, NeuroMLVersion.NEUROML_VERSION_2);

            assertTrue(morphFile.exists());

            System.out.println("Saved cell in NeuroML Level 3 file: "+ morphFile.getAbsolutePath());


            File schemaFile = GeneralProperties.getNeuroMLv2SchemaFile();

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            System.out.println("Found the XSD file: " + schemaFile.getAbsolutePath());

            Source schemaFileSource = new StreamSource(schemaFile);
            Schema schema = factory.newSchema(schemaFileSource);

            Validator validator = schema.newValidator();

            Source xmlFileSource = new StreamSource(morphFile);

            validator.validate(xmlFileSource);

            System.out.println("File is valid NeuroML v2.x!");
        }
    }
    
    
    public static void main(String[] args)
    {
        MorphMLReaderTest ct = new MorphMLReaderTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }


}