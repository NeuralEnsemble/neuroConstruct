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

import java.awt.Color;
import java.beans.XMLDecoder;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.*;
import org.neuroml.export.Utils;
import org.neuroml.model.Connection;
import org.neuroml.model.ExtracellularProperties;
import org.neuroml.model.ExtracellularPropertiesLocal;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.util.NeuroMLConverter;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.converters.MorphMLReader;
import ucl.physiol.neuroconstruct.genesis.GenesisSettings;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.SimulatorMapping;
import ucl.physiol.neuroconstruct.neuron.NeuronSettings;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.cellchoice.AllCells;
import ucl.physiol.neuroconstruct.project.cellchoice.CellChooser;
import ucl.physiol.neuroconstruct.project.packing.CellPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.SinglePositionedCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
import ucl.physiol.neuroconstruct.project.stimulation.ElectricalInput;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.project.stimulation.IClampInstanceProps;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainInstanceProps;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;

import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLAttribute;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLDocument;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLElement;
import ucl.physiol.neuroconstruct.utils.xml.SimpleXMLNamespace;

/**
 * NeuroML 2 file Reader. Importer of NeuroML 2 files to neuroConstruct
 *
 * @author Padraig Gleeson
 *  
 * 
 * Changes made to extend the importer to Level 3 NeuroML files (cells and channels have to be extracted):
 * - if the startElement find a CELL element turn on the flag insideCell.
 * - if the flag insideCell is true the reader start to copy the file line by line in a new file containing a Level 3 Cell Description.
 * - once the CELL is finished the new cell type is loaded in the project.
 * 
 * The same procedure is applied to the CHANNEL type...
 * NB: the channels can't be renamed by the user because the loaded cells will use the old names.
 * 
 * @author  Matteo Farinella
 * 
 */

public class NeuroML2Reader implements NetworkMLnCInfo
{
    private static ClassLogger logger = new ClassLogger("NeuroML2Reader");

    private long foundRandomSeed = Long.MIN_VALUE;

    private String foundSimConfig = null;

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;
    
    private GeneratedElecInputs elecInputs = null;    
    
    private Project project = null;
    
    public boolean testMode = false;

    public NeuroML2Reader(Project project)
    {
        this.cellPos = project.generatedCellPositions;
        this.netConns = project.generatedNetworkConnections;
        this.elecInputs = project.generatedElecInputs;
        this.project = project;
        this.testMode = false;
		

    }
    
    public void setTestMode(boolean test)
    {
        this.testMode = test;
    }


    public String getSimConfig()
    {
        return this.foundSimConfig;
    }

    public long getRandomSeed()
    {
        return this.foundRandomSeed;
    }
    
    public void parse(File nml2File) throws NeuroMLException
    {
        try 
        {
            NeuroMLConverter neuromlConverter=new NeuroMLConverter();

            NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(new URL("file://"+nml2File.getAbsolutePath()));

            logger.logComment("Reading in NeuroML 2: "+ neuroml.getId(), true);

            if (neuroml.getNetwork().size()!=1)
            {
                GuiUtils.showErrorMessage(logger, "Currently it is only possible to load a NeuroML file containing a single <network> element.\n"
                        + "There are "+neuroml.getNetwork().size()+" networks in the file: "+nml2File.getAbsolutePath(), null, null);
                return;
            }

            Network network = neuroml.getNetwork().get(0); // Only first network...

            ExtracellularPropertiesLocal ep = network.getExtracellularProperties().get(0);

            float tempSI = Utils.getMagnitudeInSI(ep.getTemperature());
            float tempnC = Utils.getMagnitudeInSI(project.simulationParameters.getTemperature()+"degC");

            if (Math.abs(tempSI-tempnC)>1e-6)
            {
                GuiUtils.showWarningMessage(logger, "Note that the imported network file specifies a temperature of "+ep.getTemperature()
                        +", but the neuroConstruct project has a temperature setting of "+project.simulationParameters.getTemperature()+" deg C", null);
              
            }

            for (Population population: network.getPopulation())
            {
                for (Instance instance: population.getInstance()) 
                {
                    Location loc = instance.getLocation();

                    logger.logComment("Adding instance "+instance.getId()+" at: "+ loc+" in "+population.getId(), true);
                    this.cellPos.addPosition(population.getId(), new PositionRecord(instance.getId().intValue(), loc.getX(), loc.getY(), loc.getZ()));
                }
            }
            for (Projection projection: network.getProjection())
            {
                String netConn = projection.getId();
                String source = projection.getPresynapticPopulation();
                String target = projection.getPostsynapticPopulation();
                
                //TODO: check source & target in 
                
                for (Connection conn: projection.getConnection())
                {
                    this.netConns.addSynapticConnection(netConn, parseForCellNumber(conn.getPreCellId()), parseForCellNumber(conn.getPostCellId()));
                }
            }
        }
        catch(Exception e) {
            throw new NeuroMLException("Problem parsing NeuroML file: "+nml2File, e);
        }
        
        
    }
    
    private int parseForCellNumber(String cellIdString) 
    {
        int lastSlash = cellIdString.lastIndexOf("/");
        int secondLastSlash = cellIdString.substring(0, lastSlash).lastIndexOf("/");
        return Integer.parseInt(cellIdString.substring(secondLastSlash+1, lastSlash));
    }


    public static void main(String args[])
    {

        try
        {
            Project testProj = Project.loadProject(new File("testProjects/TestNetworkML/TestNetworkML.neuro.xml"),null);

            File f = new File("testProjects/TestNetworkML/savedNetworks/test_nml2.xml");

            logger.logComment("Loading nml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);
            
            ProjectManager pm = new ProjectManager(null, null);
   
            pm.setCurrentProject(testProj);
            
            
            pm.doLoadNeuroML2Network(f, false);
            
            
            while (pm.isGenerating())
            {
                Thread.sleep(2);
                System.out.println("Waiting...");
            }
            
            System.out.println(testProj.generatedCellPositions.details());
            System.out.println(testProj.generatedNetworkConnections.details());
            

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
