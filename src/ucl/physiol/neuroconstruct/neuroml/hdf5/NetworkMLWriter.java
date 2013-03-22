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


import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.File;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.neuroml.NetworkMLReader;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.GeneratedCellPositions;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;
import ucl.physiol.neuroconstruct.neuroml.NetworkMLConstants;
import ucl.physiol.neuroconstruct.project.ConnSpecificProps;
import ucl.physiol.neuroconstruct.project.GeneralProperties;
import ucl.physiol.neuroconstruct.project.GeneratedElecInputs;
import ucl.physiol.neuroconstruct.project.PositionRecord;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.SingleSynapticConnection;
import ucl.physiol.neuroconstruct.project.SimConfig;
import ucl.physiol.neuroconstruct.project.SingleElectricalInput;
import ucl.physiol.neuroconstruct.project.SynapticProperties;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.simulation.StimulationSettings;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;

/**
 * Utilities file for generating NetworkML HDF5 files
 *
 * @author Padraig Gleeson
 *
 */

public class NetworkMLWriter
{
    private static ClassLogger logger = new ClassLogger("NetworkMLWriter");



    public NetworkMLWriter()
    {
        super();
    }

    public static File createNetworkMLH5file(File file,
                                             Project project,
                                             SimConfig simConfig,
                                             String units) throws Hdf5Exception
    {

        int unitSystem = UnitConverter.getUnitSystemIndex(units);
        
        H5File h5File = Hdf5Utils.createH5file(file);

        Hdf5Utils.open(h5File);

        Group root = Hdf5Utils.getRootGroup(h5File);
        Group netmlGroup = null;
        Group popsGroup = null;
        Group projsGroup = null;
        Group inputsGroup = null;
        
        GeneratedCellPositions gcp = project.generatedCellPositions;
        GeneratedNetworkConnections gnc = project.generatedNetworkConnections;
        GeneratedElecInputs gei = project.generatedElecInputs;
        
        
        StringBuilder notes = new StringBuilder("\nNetwork structure saved with neuroConstruct v"+
                        GeneralProperties.getVersionNumber()+" on: "+ GeneralUtils.getCurrentTimeAsNiceString() +", "
                    + GeneralUtils.getCurrentDateAsNiceString()+"\n\n");


        Iterator<String> cellGroups = gcp.getNamesGeneratedCellGroups();

        while (cellGroups.hasNext())
        {
            String cg = cellGroups.next();
            int numHere = gcp.getNumberInCellGroup(cg);
            if (numHere>0)
            notes.append("Cell Group: "+cg+" contains "+numHere+" cells\n");

        }
        notes.append("\n");

        Iterator<String> netConns = gnc.getNamesNetConnsIter();

        while (netConns.hasNext())
        {
            String mc = netConns.next();
            int numHere = gnc.getSynapticConnections(mc).size();
            if (numHere>0)
            notes.append("Network connection: "+mc+" contains "+numHere+" individual synaptic connections\n");

        }

        Iterator<String> elecInputs = gei.getElecInputsItr();

        while (elecInputs.hasNext())
        {
            String ei = elecInputs.next();
            int numHere = gei.getNumberSingleInputs(ei);
            if (numHere>0)
            notes.append("Electrical Input: "+ei+" contains "+numHere+" individual stimulations\n");

        }               
        
        try
        {
            netmlGroup = h5File.createGroup(NetworkMLConstants.ROOT_ELEMENT, root);
            popsGroup = h5File.createGroup(NetworkMLConstants.POPULATIONS_ELEMENT, netmlGroup);
            projsGroup = h5File.createGroup(NetworkMLConstants.PROJECTIONS_ELEMENT, netmlGroup);
            inputsGroup = h5File.createGroup(NetworkMLConstants.INPUTS_ELEMENT, netmlGroup);
            
            Attribute unitsAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.UNITS_ATTR, units, h5File);
            
            projsGroup.writeMetadata(unitsAttr);

            Attribute attr = Hdf5Utils.getSimpleAttr("notes", notes.toString(), h5File);
            
            netmlGroup.writeMetadata(attr);

            Attribute attrSimConf = Hdf5Utils.getSimpleAttr(NetworkMLConstants.NC_SIM_CONFIG, simConfig.getName(), h5File);
            
            netmlGroup.writeMetadata(attrSimConf);

            Attribute attrSeed = Hdf5Utils.getSimpleAttr(NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED, project.generatedCellPositions.getRandomSeed()+"", h5File);
            
            netmlGroup.writeMetadata(attrSeed);
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to create group in HDF5 file: "+ h5File.getFilePath(), ex);
        }

        cellGroups = gcp.getNamesGeneratedCellGroups();

        while(cellGroups.hasNext())
        {
            String cg = cellGroups.next();

            ArrayList<PositionRecord> posRecs = gcp.getPositionRecords(cg);

            try
            {
                Group popGroup = h5File.createGroup(NetworkMLConstants.POPULATION_ELEMENT+"_"+cg, popsGroup);
                
                Attribute nameAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.POP_NAME_ATTR, cg, h5File);
                popGroup.writeMetadata(nameAttr);
                
                String cellType = project.cellGroupsInfo.getCellType(cg);
            
                Attribute cellTypeAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.CELLTYPE_ATTR, cellType, h5File);
                popGroup.writeMetadata(cellTypeAttr);

                Datatype dtype = getPopDatatype(h5File);
                
                int numColumns = 4; // cellNum, x, y, z
                
                if (posRecs.get(0).getNodeId()!=PositionRecord.NO_NODE_ID)
                {
                    numColumns = 5; // cellNum, x, y, z
                }

                long[] dims2D = {posRecs.size(), numColumns};

                float[] posArray = new float[posRecs.size() * numColumns];


                for (int i=0; i<posRecs.size(); i++)
                {
                    PositionRecord p = posRecs.get(i);

                    posArray[i * numColumns + 0] = p.cellNumber;
                    posArray[i * numColumns + 1] = p.x_pos;
                    posArray[i * numColumns + 2] = p.y_pos;
                    posArray[i * numColumns + 3] = p.z_pos;
                    if (numColumns>4)
                        posArray[i * numColumns + 4] = p.getNodeId();
                        

                }


                Dataset dataset = h5File.createScalarDS
                    (cg, popGroup, dtype, dims2D, null, null, 0, posArray);

                Attribute attr0 = Hdf5Utils.getSimpleAttr("column_0", NetworkMLConstants.INSTANCE_ID_ATTR, h5File);
                dataset.writeMetadata(attr0);
                Attribute attr1 = Hdf5Utils.getSimpleAttr("column_1", NetworkMLConstants.LOC_X_ATTR, h5File);
                dataset.writeMetadata(attr1);
                Attribute attr2 = Hdf5Utils.getSimpleAttr("column_2", NetworkMLConstants.LOC_Y_ATTR, h5File);
                dataset.writeMetadata(attr2);
                Attribute attr3 = Hdf5Utils.getSimpleAttr("column_3", NetworkMLConstants.LOC_Z_ATTR, h5File);
                dataset.writeMetadata(attr3);
                
                if (numColumns>4)
                {
                    Attribute attr4 = Hdf5Utils.getSimpleAttr("column_4", NetworkMLConstants.NODE_ID_ATTR, h5File);
                    dataset.writeMetadata(attr4);
                }


            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }
        }





        Iterator<String> nCs = gnc.getNamesNetConnsIter();

        while(nCs.hasNext())
        {
            String nc = nCs.next();

            ArrayList<SingleSynapticConnection> conns = gnc.getSynapticConnections(nc);

            try
            {
                Group projGroup = h5File.createGroup(NetworkMLConstants.PROJECTION_ELEMENT +"_" + nc, projsGroup);
                
                
                Attribute nameAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.PROJ_NAME_ATTR, nc, h5File);
                projGroup.writeMetadata(nameAttr);
                
                String src = null;
                String tgt = null;
                Vector<SynapticProperties>  globalSynPropList = null;
                
                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(nc))
                {
                    src = project.morphNetworkConnectionsInfo.getSourceCellGroup(nc);
                    tgt = project.morphNetworkConnectionsInfo.getTargetCellGroup(nc);
                    globalSynPropList = project.morphNetworkConnectionsInfo.getSynapseList(nc);
                }

                else if (project.volBasedConnsInfo.isValidVolBasedConn(nc))
                {
                    src = project.volBasedConnsInfo.getSourceCellGroup(nc);
                    src = project.volBasedConnsInfo.getTargetCellGroup(nc);
                    globalSynPropList = project.volBasedConnsInfo.getSynapseList(nc);
                }
                
                Attribute srcAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.SOURCE_ATTR, src, h5File);
                projGroup.writeMetadata(srcAttr);
                Attribute tgtAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.TARGET_ATTR, tgt, h5File);
                projGroup.writeMetadata(tgtAttr);
                
                float globWeight = 1;
                float globDelay = 0;
                
                for(SynapticProperties sp:  globalSynPropList)
                {
                    Group synPropGroup = h5File.createGroup(NetworkMLConstants.SYN_PROPS_ELEMENT +"_" + sp.getSynapseType(), projGroup);

                    Attribute synTypeAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.SYN_TYPE_ATTR, sp.getSynapseType(), h5File);
                    synPropGroup.writeMetadata(synTypeAttr);
                    
                    globDelay = (float)UnitConverter.getTime(sp.getDelayGenerator().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                    Attribute synTypeDelay = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INTERNAL_DELAY_ATTR, globDelay+"", h5File);
                    synPropGroup.writeMetadata(synTypeDelay);
                    
                    globWeight = sp.getWeightsGenerator().getNominalNumber();
                    Attribute synTypeWeight = Hdf5Utils.getSimpleAttr(NetworkMLConstants.WEIGHT_ATTR, globWeight+"", h5File);
                    synPropGroup.writeMetadata(synTypeWeight);
                    
                    Attribute synTypeThreshold = Hdf5Utils.getSimpleAttr(NetworkMLConstants.THRESHOLD_ATTR, 
                            (float)UnitConverter.getVoltage(sp.getThreshold(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"", h5File);
                    
                    synPropGroup.writeMetadata(synTypeThreshold);
                }
                
                ArrayList<String> columnsNeeded = new ArrayList<String>();
                
                columnsNeeded.add(NetworkMLConstants.CONNECTION_ID_ATTR);
                columnsNeeded.add(NetworkMLConstants.PRE_CELL_ID_ATTR);
                columnsNeeded.add(NetworkMLConstants.POST_CELL_ID_ATTR);
                
                for (int i = 0; i < conns.size(); i++)
                {
                    SingleSynapticConnection conn = conns.get(i);
                    
                    if (conn.sourceEndPoint.location.getSegmentId()!=0 && !columnsNeeded.contains(NetworkMLConstants.PRE_SEGMENT_ID_ATTR))
                        columnsNeeded.add(NetworkMLConstants.PRE_SEGMENT_ID_ATTR);
                    
                    if (conn.sourceEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN && 
                            !columnsNeeded.contains(NetworkMLConstants.PRE_FRACT_ALONG_ATTR))
                        columnsNeeded.add(NetworkMLConstants.PRE_FRACT_ALONG_ATTR);
                    
                    if (conn.targetEndPoint.location.getSegmentId()!=0 && !columnsNeeded.contains(NetworkMLConstants.POST_SEGMENT_ID_ATTR))
                        columnsNeeded.add(NetworkMLConstants.POST_SEGMENT_ID_ATTR);
                    
                    if (conn.targetEndPoint.location.getFractAlong()!=SegmentLocation.DEFAULT_FRACT_CONN && 
                            !columnsNeeded.contains(NetworkMLConstants.POST_FRACT_ALONG_ATTR))
                        columnsNeeded.add(NetworkMLConstants.POST_FRACT_ALONG_ATTR);
                    
                    if (conn.apPropDelay!=0)
                    {
                        for(SynapticProperties sp:  globalSynPropList)
                        {
                            String colName = NetworkMLConstants.PROP_DELAY_ATTR +"_"+sp.getSynapseType();
                            if (!columnsNeeded.contains(colName))
                            {
                                columnsNeeded.add(colName);
                            }
                        }
                    }
                    
                    if (conn.props!=null)
                    {
                        for(ConnSpecificProps prop: conn.props)
                        {
                            if(prop.weight!=1 && !columnsNeeded.contains(NetworkMLConstants.WEIGHT_ATTR+"_"+prop.synapseType))
                                columnsNeeded.add(NetworkMLConstants.WEIGHT_ATTR+"_"+prop.synapseType);
                            
                            if(prop.internalDelay!=0 && !columnsNeeded.contains(NetworkMLConstants.INTERNAL_DELAY_ATTR+"_"+prop.synapseType))
                                columnsNeeded.add(NetworkMLConstants.INTERNAL_DELAY_ATTR+"_"+prop.synapseType);
                        }
                    }
                }
                

                Datatype dtype = getProjDatatype(h5File);

                long[] dims2D = {conns.size(), columnsNeeded.size()};

                float[] projArray = new float[conns.size() * columnsNeeded.size()];

                for (int i = 0; i < conns.size(); i++)
                {
                    SingleSynapticConnection conn = conns.get(i);

                    int row = 0;
                    projArray[i * columnsNeeded.size() +row] = i;
                    row++;

                    projArray[i * columnsNeeded.size() + row] = conn.sourceEndPoint.cellNumber;
                    row++;
                    
                    projArray[i * columnsNeeded.size() + row] = conn.targetEndPoint.cellNumber;
                    row++;
                    
                    if (columnsNeeded.contains(NetworkMLConstants.PRE_SEGMENT_ID_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.sourceEndPoint.location.getSegmentId();
                        row++;
                    }
                    
                    if (columnsNeeded.contains(NetworkMLConstants.PRE_FRACT_ALONG_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.sourceEndPoint.location.getFractAlong();
                        row++;
                    }

                    
                    if (columnsNeeded.contains(NetworkMLConstants.POST_SEGMENT_ID_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.targetEndPoint.location.getSegmentId();
                        row++;
                    }
                    
                    if (columnsNeeded.contains(NetworkMLConstants.POST_FRACT_ALONG_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.targetEndPoint.location.getFractAlong();
                        row++;
                    }
                    /*
                    if (columnsNeeded.contains(NetworkMLConstants.PROP_DELAY_ATTR))
                    {
                        projArray[i * columnsNeeded.size() + row] = conn.apPropDelay;
                        row++;
                    }*/
                    
                    for(SynapticProperties sp:  globalSynPropList)
                    {
                        String colName = NetworkMLConstants.PROP_DELAY_ATTR +"_"+sp.getSynapseType();
                        if (columnsNeeded.contains(colName))
                        {
                            projArray[i * columnsNeeded.size() + row] = 
                                    (float)UnitConverter.getTime(conn.apPropDelay, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                            row++;
                        }
                    }
                    
                    
                    if (conn.props!=null)
                    {
                        for(ConnSpecificProps prop: conn.props)
                        {
                            if(columnsNeeded.contains(NetworkMLConstants.WEIGHT_ATTR+"_"+prop.synapseType))
                            {
                                projArray[i * columnsNeeded.size() + row] = prop.weight;
                                row++;
                            }
                            if(columnsNeeded.contains(NetworkMLConstants.INTERNAL_DELAY_ATTR+"_"+prop.synapseType))
                            {
                                projArray[i * columnsNeeded.size() + row] = 
                                        (float)UnitConverter.getTime(prop.internalDelay, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem);
                                row++;
                            }
                            
                        }
                    }

                }

                Dataset projDataset = h5File.createScalarDS(nc, projGroup, dtype, dims2D, null, null, 0, projArray);
                
                for(int i=0;i<columnsNeeded.size();i++)
                {
                    Attribute attr = Hdf5Utils.getSimpleAttr("column_"+i, columnsNeeded.get(i), h5File);
                    projDataset.writeMetadata(attr);
                }
                
                
                logger.logComment("Dataset compression: " + projDataset.getCompression());

            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }
        }
         
        // Start of writing the Electrical Inputs into Hdf5 format
        
        // Create Record Inputs
        //                 - Input either IClamp or Random Spike
        //                      - IClamp Type add attributes Delay, Duration, Amplitude)
        //                      - Random Spike add attributes Frequency, Mechanism) 
        //                              - Sites Group Table of 4 Columns (Cell Group ID, Cell ID, Segment ID, Fraction Along)
        
        // Create an iterator to navigate through all the input names
        
        Iterator<String> nEi = gei.getElecInputsItr(); 
        try
        { 
            // Add units for the Inputs
            
                Attribute unitsAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.UNITS_ATTR, units, h5File);
                inputsGroup.writeMetadata(unitsAttr);
           
            
            // loop around all the inputs
            while(nEi.hasNext())        
            {

                String ei = nEi.next();

                // Get the stimulation settings for the input referenc
                StimulationSettings nextStim = project.elecInputInfo.getStim(ei);

                // Get the electrical input for the stim settings
                ElectricalInput myElectricalInput = nextStim.getElectricalInput();

                // Get Input Locations

                ArrayList<SingleElectricalInput> inputsHere =  project.generatedElecInputs.getInputLocations(ei);


                    Group inputGroup = h5File.createGroup(NetworkMLConstants.INPUT_ELEMENT+"_"+ei, inputsGroup);


                    // Build Site Table for both IClamp and RandomSpikeTrain Inputs

                    int inputsNumCols = 3; // Cell ID, Segment ID, Fraction Along Segment

                    int inputNumber = inputsHere.size();

                    long[] dims2D = {inputNumber, inputsNumCols};

                    float[] sitesArray = new float[inputNumber * inputsNumCols];

                    Datatype dtype = getInputDatatype(h5File);

                    // Build array of sites as stim setting

                    for (int i=0; i<inputNumber; i++)
                    {
                        sitesArray[i * inputsNumCols + 0] = inputsHere.get(i).getCellNumber();
                        sitesArray[i * inputsNumCols + 1] = inputsHere.get(i).getSegmentId();
                        sitesArray[i * inputsNumCols + 2] = inputsHere.get(i).getFractionAlong();
                    }               

                    Dataset sitesDataset = h5File.createScalarDS (ei+"_"+"input_sites", inputGroup, dtype, dims2D, null, null, 0, sitesArray);

                    Attribute attr0 = Hdf5Utils.getSimpleAttr("column_0", NetworkMLConstants.INPUT_SITE_CELLID_ATTR, h5File);
                    sitesDataset.writeMetadata(attr0);
                    Attribute attr1 = Hdf5Utils.getSimpleAttr("column_1", NetworkMLConstants.INPUT_SITE_SEGID_ATTR, h5File);
                    sitesDataset.writeMetadata(attr1);
                    Attribute attr2 = Hdf5Utils.getSimpleAttr("column_2", NetworkMLConstants.INPUT_SITE_FRAC_ATTR, h5File);
                    sitesDataset.writeMetadata(attr2);

                    String cellGroup = nextStim.getCellGroup();

                    if (myElectricalInput instanceof IClamp)
                    {
                        IClamp ic = (IClamp)myElectricalInput;

                        Group inputTypeGroup = h5File.createGroup(myElectricalInput.getType()+"_"+"properties", inputGroup);

                        // Get Details of the IClamp attributes
                        
                        
                        String delay = (float)UnitConverter.getTime(ic.getDel().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
                        String duration = (float)UnitConverter.getTime(ic.getDur().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";     
                        String amp = (float)UnitConverter.getCurrent(ic.getAmp().getNominalNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
            
            
//////////                        ic.getDelay().reset();
//////////                        String delay = (float)UnitConverter.getTime(ic.getDelay().getNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
//////////                        ic.getDuration().reset();
//////////                        String duration = (float)UnitConverter.getTime(ic.getDuration().getNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
//////////                        ic.getAmplitude().reset();   
//////////                        String amp = (float)UnitConverter.getCurrent(ic.getAmplitude().getNumber(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";

                        //Assign them to the attibutes of the group

                        Attribute cellGroupAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR, cellGroup, h5File);
                        Attribute delayAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_DELAY_ATTR, delay, h5File);                    
                        Attribute durationAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_DUR_ATTR, duration, h5File);                    
                        Attribute ampAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_AMP_ATTR, amp, h5File);

                         //add them as attributes to the group

                        inputTypeGroup.writeMetadata(cellGroupAttr);
                        inputTypeGroup.writeMetadata(delayAttr);                    
                        inputTypeGroup.writeMetadata(durationAttr);
                        inputTypeGroup.writeMetadata(ampAttr);                    
                    }
                    else if (myElectricalInput instanceof RandomSpikeTrain)
                    {
                        RandomSpikeTrain rst = (RandomSpikeTrain)myElectricalInput;

                        Group inputTypeGroup = h5File.createGroup(myElectricalInput.getType()+"_"+"properties", inputGroup);

                        // Get details of Random Spike Train Attributes

                        String stimFreq = (float)UnitConverter.getRate(rst.getRate().getFixedNum(), UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
                        String stimMech = rst.getSynapseType();

                        //Assign them to the attibutes of the group

                        Attribute cellGroupAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR, cellGroup, h5File);
                        Attribute stimFreqAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.RND_STIM_FREQ_ATTR, stimFreq, h5File);                    
                        Attribute stimMechAttr = Hdf5Utils.getSimpleAttr(NetworkMLConstants.RND_STIM_MECH_ATTR, stimMech, h5File);                    

                        // add them as attributes to the group

                        inputTypeGroup.writeMetadata(cellGroupAttr);                    
                        inputTypeGroup.writeMetadata(stimFreqAttr);                    
                        inputTypeGroup.writeMetadata(stimMechAttr);

                        }
               }
            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }
        
        
        
        //h5File.

        Hdf5Utils.close(h5File);

        logger.logComment("Created file: " + file);
        logger.logComment("Size: " + file.length()+" bytes");
        
        return file;
    }

    public static Datatype getPopDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {

            Datatype popDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return popDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get pop datatype in HDF5 file: " + h5File.getFilePath(), ex);

        }

    }

    public static Datatype getProjDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {
            Datatype projDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return projDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get projection datatype in HDF5 file: " + h5File.getFilePath(),ex);

        }

    }

    public static Datatype getInputDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {
            Datatype projDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return projDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get input datatype in HDF5 file: " + h5File.getFilePath(),ex);
        }
    }

    public static void main(String[] args)
    {
        File h5File = new File("../temp/net.h5");
        try
        {
            System.setProperty("java.library.path", System.getProperty("java.library.path")+":/home/padraig/neuroConstruct");
            
            //logger.logComment("Sys prop: "+System.getProperty("java.library.path"), true);
            
            Project testProj = Project.loadProject(new File("nCmodels/GranCellLayer/GranCellLayer.ncx"),
                                                   null);

            //File nmlFile = new File("examples/Ex9-GranCellLayer/savedNetworks/600.nml");
            File nmlFile = new File("nCmodels/GranCellLayer/savedNetworks/75.nml");
            //File nmlFile = new File("../copynCmodels/Parallel/savedNetworks/50000.nml");
            //File nmlFile = new File("../copynCmodels/NewGranCellLayer/savedNetworks/87000Rand.nml");
            //File nmlFile = new File("../temp/test.nml");
            //File nmlFile = new File("../copynCmodels/Parallel/savedNetworks/50000.nml");


            logger.logComment("Loading netml cell from " + nmlFile.getAbsolutePath(), true);
            logger.logComment("Saving netml to " + h5File.getAbsolutePath(), true);

            GeneratedCellPositions gcp = testProj.generatedCellPositions;
            GeneratedNetworkConnections gnc = testProj.generatedNetworkConnections;
            GeneratedElecInputs gei = testProj.generatedElecInputs;

            FileInputStream instream = null;
            InputSource is = null;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            NetworkMLReader nmlBuilder = new NetworkMLReader(testProj);
            xmlReader.setContentHandler(nmlBuilder);

            instream = new FileInputStream(nmlFile);

            is = new InputSource(instream);

            xmlReader.parse(is);

            //logger.logComment("Cells: " + gcp.getNumberInAllCellGroups(), true);
            //logger.logComment("Net conn num: " + gnc.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION), true);
            //logger.logComment("Stimulations num: " + gei.getNumberSingleInputs(), true);

            NetworkMLWriter.createNetworkMLH5file(h5File, 
                                                  testProj,
                                                  testProj.simConfigInfo.getDefaultSimConfig(),
                                                  NetworkMLConstants.UNITS_PHYSIOLOGICAL);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }


    }
}
