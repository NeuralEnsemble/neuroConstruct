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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.xml.sax.SAXException;
import ucl.physiol.neuroconstruct.test.MainTest;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.stimulation.ElectricalInput;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import static org.junit.Assert.*;
import ucl.physiol.neuroconstruct.utils.xml.*;


/**
 *
 * @author Matteo
 */
public class Level3ExportTest {
    
    String projName = "TestNetworkML";
    File projDir = new File(MainTest.getTestProjectDirectory()+ projName);
        
    ProjectManager pm = null;    
    
    public Level3ExportTest() 
    {
        
    }
    
     @Before
    public void setUp() 
    {
        System.out.println("---------------   setUp() Level3ExportTest");
        
        File projFile = ProjectStructure.findProjectFile(projDir);
        
        pm = new ProjectManager();
        try 
        {
            pm.loadProject(projFile);
        } 
        catch (ProjectFileParsingException ex) 
        {
            Logger.getLogger(Level3ExportTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Proj status: "+ pm.getCurrentProject().getProjectStatusAsString());

    }
     
     @After
    public void tearDown() {
    }
    
     
     @Test
    public void testLevel3Exporting() throws NeuroMLException 
    {
        try {
            System.out.println("---  testLevel3Exporting");

            Project proj = pm.getCurrentProject();
            SimConfig sc = proj.simConfigInfo.getDefaultSimConfig();
            
            pm.doGenerate(sc.getName(), 1234);
            
            while(pm.isGenerating())
            {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Level3ExportTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            StringBuffer stateString1 = new StringBuffer();
        
            stateString1.append(proj.generatedCellPositions.toLongString(false));
            stateString1.append(proj.generatedNetworkConnections.details(false));
            stateString1.append(proj.generatedElecInputs.toString());

            System.out.println("Generated proj with: "+ proj.generatedCellPositions.getNumberInAllCellGroups()+" cells");
            
            File saveNetsDir = ProjectStructure.getSavedNetworksDir(projDir);
            
            File nmlFile = new File(saveNetsDir, "test.nml");

            File l3 = ProjectManager.saveLevel3NetworkXML(proj, nmlFile, false, false, sc.getName(), NetworkMLConstants.UNITS_PHYSIOLOGICAL);
            
            SimpleXMLDocument doc = SimpleXMLReader.getSimpleXMLDoc(l3);
            
//            ArrayList<String> paths = doc.getXPathLocations(true);
//            for (int index = 0; index < paths.size(); index++) {
//            System.out.println("path: "+paths.get(index));                
//            }

            // check the populations
            SimpleXMLEntity[] l3Groups = doc.getXMLEntities("/neuroml/populations/population/@name");
            ArrayList<String> genGroups = proj.generatedCellPositions.getNonEmptyCellGroups();
            System.out.println("*****");
            System.out.println("groups in doc "+l3Groups.length);
            System.out.println("generated groups "+genGroups.size());
            assertTrue(l3Groups.length == genGroups.size());
            
            
            // check the cell types
            SimpleXMLEntity[] l3Cells = doc.getXMLEntities("/neuroml/cells/cell/@name");
            ArrayList<String> genCells = new ArrayList<String>();
            for (int i = 0; i < genGroups.size(); i++) {
                String cellType = proj.cellGroupsInfo.getCellType(genGroups.get(i));
                if (!genCells.contains(cellType))
                    genCells.add(cellType);                
            }
            System.out.println("*****");
            System.out.println("cell types in doc "+l3Cells.length);
            System.out.println("generated cell types "+genCells.size());
            assertTrue(l3Cells.length == genCells.size());

            
            
            //check the cell mechanisms
            SimpleXMLEntity[] l3Channels = doc.getXMLEntities("/neuroml/channels/channel_type/@name");
            SimpleXMLEntity[] l3Synapses = doc.getXMLEntities("/neuroml/channels/synapse_type/@name");
            SimpleXMLEntity[] l3IonConcs = doc.getXMLEntities("/neuroml/channels/ion_concentration/@name");
            
            ArrayList<String> allMechs = proj.cellMechanismInfo.getAllCellMechanismNames();
            boolean addChan = false;
            
            Vector<String> genMechs = new Vector<String>();

              for (int j = 0; j < allMechs.size(); j++)
            {                
                String m = allMechs.get(j); 
                addChan = false;
                
                //add the synapses that are used in the generated connections
                Iterator<String> connsNames = proj.generatedNetworkConnections.getNamesNetConnsIter();             
                
                while (connsNames.hasNext() && (addChan==false))
                {
                    String netConnName = connsNames.next();
                    
                    if (proj.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                    {
                        Vector<SynapticProperties> synPropsM = proj.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                        for (int k = 0; k < synPropsM.size(); k++) {
                            SynapticProperties synapticProperties = synPropsM.elementAt(k);
                            if (synapticProperties.getSynapseType().equals(m) && !genMechs.contains(m)) {
                                genMechs.add(m);
                                addChan = true;
                            }                  
                        }
                    }
                    
                    if (proj.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                    {
                        Vector<SynapticProperties> synPropsV = proj.volBasedConnsInfo.getSynapseList(netConnName);
                        for (int k = 0; k < synPropsV.size(); k++) {
                            SynapticProperties synapticProperties = synPropsV.elementAt(k);
                            if (synapticProperties.getSynapseType().equals(m) && !genMechs.contains(m)) {
                                genMechs.add(m);
                                addChan = true;
                            }                           
                        }                            
                    }
                }
                                
                //add the synapses that are used in the stimulations
                Iterator<String> inputsNames = proj.generatedElecInputs.getElecInputsItr();
                while (inputsNames.hasNext()&&(addChan==false))
                {
                    ElectricalInput ei  = proj.elecInputInfo.getStim(inputsNames.next()).getElectricalInput();
                    if (ei.getType().equals("RandomSpikeTrainExt") || ei.getType().equals("RandomSpikeTrain"))
                    {
                        RandomSpikeTrain rst = (RandomSpikeTrain)ei;
                        if (rst.getSynapseType().equals(m)
                                && !genMechs.contains(m))
                        {                            
                            genMechs.add(m);
                            addChan = true;
                        }                        
                    }
                }
                                
                //add channels that are used in the generated cell groups
                Iterator<String> cellGroups = proj.generatedCellPositions.getNamesGeneratedCellGroups();
                ArrayList<String> cellGroupsNames = new ArrayList<String>();

                while (cellGroups.hasNext())
                {
                    String cg = cellGroups.next();
                    cellGroupsNames.add(cg);

                }
                Integer i =0;
                while ((i<cellGroupsNames.size())&&(addChan==false))
                {  
                    if (proj.cellManager.getCell(proj.cellGroupsInfo.getCellType(cellGroupsNames.get(i))).getAllChanMechNames(true).contains(m)
                            && !genMechs.contains(m))
                    {
                        genMechs.add(m);
                        addChan = true;
                    }
                    i++;
                 }
            }
                            
            assertTrue(genMechs.size()<=allMechs.size());
            System.out.println("*****");
            System.out.println(l3Channels.length+" channels in doc + " +l3Synapses.length+" synapses in doc " );
            System.out.println("generated mechanisms " +genMechs.size());
            assertTrue((l3Channels.length + l3Synapses.length + l3IonConcs.length) == genMechs.size());
            
            
             //check the network connections
            SimpleXMLEntity[] l3Nets = doc.getXMLEntities("/neuroml/projections/projection/@name");
            Iterator<String> genNets = proj.generatedNetworkConnections.getNamesNetConnsIter();
            System.out.println("*****");
            System.out.println(l3Nets.length+" networks in doc");
            int c = 0;
            while (genNets.hasNext()) {
                genNets.next();
                c++;                
            }            
            System.out.println(c+" generated networks");
            assertTrue(l3Nets.length == c);
            
            //check the electrical inputs
            SimpleXMLEntity[] l3Inputs = doc.getXMLEntities("/neuroml/inputs/input/@name");
            ArrayList<String> genInputs = proj.generatedElecInputs.getInputReferences();
            System.out.println("*****");
            System.out.println(l3Inputs.length+" inputs in doc");
            System.out.println(genInputs.size()+" generated inputs");
            assertTrue(l3Inputs.length == genInputs.size());
            
            
        } catch (IOException ex) {
            Logger.getLogger(Level3ExportTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Level3ExportTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Level3ExportTest.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         
                 
    }
     
     public static void main(String[] args)
    {
        Level3ExportTest ct = new Level3ExportTest();
        Result r = org.junit.runner.JUnitCore.runClasses(ct.getClass());
        MainTest.checkResults(r);
        
    }
}
    