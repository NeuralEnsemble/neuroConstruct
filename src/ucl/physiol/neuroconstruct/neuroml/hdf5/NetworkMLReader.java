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
import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.stimulation.ElectricalInput;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import ucl.physiol.neuroconstruct.simulation.StimulationSettings;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator.EndOfSequenceException;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;

/**
 * Utilities file for reading NetworkML HDF5 files
 *
 * @author Padraig Gleeson
 *
 */
public class NetworkMLReader  implements NetworkMLnCInfo
{    
    private static ClassLogger logger = new ClassLogger("NetworkMLReader");
        
    
    private Project project = null;
    
    
    boolean inPopulations = false;
    boolean inProjections = false;
    boolean inInputs = false;
    
    String currentCellGroup = null;
    String currentNetConn = null;
    String currentInput = null;
    
    private ArrayList<ConnSpecificProps> globConnProps = new ArrayList<ConnSpecificProps>();
    private ArrayList<ConnSpecificProps> localConnProps = new ArrayList<ConnSpecificProps>();
    
    private float globAPDelay = 0;
    private float localAPDelay = 0;
    
    private long foundRandomSeed = Long.MIN_VALUE;
    private String foundSimConfig = null;
    
    private int projUnitSystem = -1;
    private int inputUnitSystem = -1;

    public NetworkMLReader(Project project)
    {        
        //logger.setThisClassVerbose(true);
        this.project = project;

    }
    
    public String getSimConfig()
    {
        return this.foundSimConfig;
    }

    public long getRandomSeed()
    {
        return this.foundRandomSeed;
    }
    
    
    public void parse(File hdf5File) throws Hdf5Exception, EndOfSequenceException
    {
        H5File h5File = Hdf5Utils.openForRead(hdf5File);
        
        Group root = Hdf5Utils.getRootGroup(h5File);
        
        parseGroup(root);
        
        Hdf5Utils.close(h5File);
    }
        
    
        
    public void startGroup(Group g) throws Hdf5Exception
    {
        logger.logComment("-----   Going into a group: "+g.getFullName());
        
        ArrayList<Attribute> attrs = Hdf5Utils.parseGroupForAttributes(g);
            
        for (Attribute attribute : attrs) 
        {
            //attribute.
            logger.logComment("Group: "+g.getName()+ " has attribute: "+ attribute.getName()+" = "+ Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()));
            
        }
        
        if (g.getName().equals(NetworkMLConstants.ROOT_ELEMENT))
        {
            logger.logComment("Found the main group");
            
            String simConfigName = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.NC_SIM_CONFIG);
            
            if (simConfigName!=null)
                this.foundSimConfig = simConfigName;
            
            String randomSeed = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED);
            
            if (randomSeed!=null)
                this.foundRandomSeed = Long.parseLong(randomSeed);
            
            

        }
        else if (g.getName().equals(NetworkMLConstants.POPULATIONS_ELEMENT))
        {
            logger.logComment("Found the pops group");
            inPopulations = true;

        }
        else if (g.getName().startsWith(NetworkMLConstants.POPULATION_ELEMENT) && inPopulations)
        {
            String name = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.POP_NAME_ATTR);
            
            logger.logComment("Found a population: "+ name);
            currentCellGroup = name;
        }
        else if (g.getName().equals(NetworkMLConstants.PROJECTIONS_ELEMENT))
        {
            logger.logComment("Found the projections group");
            inProjections = true;
            
            String units = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.UNITS_ATTR);
            
            projUnitSystem = UnitConverter.getUnitSystemIndex(units);

        }
        else if (g.getName().startsWith(NetworkMLConstants.PROJECTION_ELEMENT) && inProjections)
        {
            String name = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.PROJ_NAME_ATTR);
            String source = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.SOURCE_ATTR);
            String target = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.TARGET_ATTR);
            
            logger.logComment("Found a projection: "+ name+" from "+ source+" to "+ target);
            
            if (!project.morphNetworkConnectionsInfo.isValidSimpleNetConn(name) &&
                !project.volBasedConnsInfo.isValidVolBasedConn(name))
            {
                throw new Hdf5Exception("Error: there is a network connection with name: "+ name+" specified in " +
                        "that file, but no such NetConn exists in the project. Add one to allow import of this file");
            }
            
            /* TODO: Add checks on source & target!!
             */
            
            if(project.morphNetworkConnectionsInfo.isValidSimpleNetConn(name))
            {
                //if (project.morphNetworkConnectionsInfo)
            }
            
            currentNetConn = name;
        }
        else if (g.getName().startsWith(NetworkMLConstants.SYN_PROPS_ELEMENT+"_") && inProjections)
        {
            String name = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.SYN_TYPE_ATTR);
            
            ConnSpecificProps cp = new ConnSpecificProps(name);
            
            
            String internalDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INTERNAL_DELAY_ATTR);
            if (internalDelay!=null)
                cp.internalDelay = (float)UnitConverter.getTime(Float.parseFloat(internalDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            
            // Lump them in to the internal delay...
            String preDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.PRE_DELAY_ATTR);
            if (preDelay!=null)
                cp.internalDelay = cp.internalDelay + (float)UnitConverter.getTime(Float.parseFloat(preDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            String postDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.POST_DELAY_ATTR);
            if (postDelay!=null)
                cp.internalDelay = cp.internalDelay + (float)UnitConverter.getTime(Float.parseFloat(postDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            
            cp.weight = Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.WEIGHT_ATTR));
            
            String propDelay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.PROP_DELAY_ATTR);
            if (propDelay!=null)
                globAPDelay = (float)UnitConverter.getTime(Float.parseFloat(propDelay), projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            logger.logComment("Found: "+ cp);
            
            globConnProps.add(cp);
        }
        else if (g.getName().equals(NetworkMLConstants.INPUTS_ELEMENT))
        {
            logger.logComment("Found the Inputs group");
            inInputs = true;
            
            String units = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.UNITS_ATTR);
            
            inputUnitSystem = UnitConverter.getUnitSystemIndex(units);
        }
        else if (g.getName().startsWith(NetworkMLConstants.INPUT_ELEMENT) && inInputs)
        {
            // The table of input sites is within the input group so get sites from here
            
            String inputName = g.getName().substring(6);
            
            //String inputName = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_ELEMENT);
            
            logger.logComment("Found an Input: "+ inputName);
            //inInput = true;
            
            if (project.elecInputInfo.getStim(inputName) == null)
            {
                throw new Hdf5Exception("Error: there is an electrical input with name: "+ inputName+" specified in " +
                        "that file, but no such electrical input exists in the project. Add one to allow import of this file");
            }
            // Get the atributes of the Input and compare them with the attributes within the project
            // Test to find out what type of input this is

        }
        else if (g.getName().startsWith("IClamp") && inInputs)
        {
            String inputName = g.getParent().getName().substring(6);
            // Get the input sites from the table

            String cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR);
            if (cellGroup==null)
            {
                cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_CELLGROUP_OLD_ATTR); // check old name
            }

            float readDelay = (float)UnitConverter.getTime(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_DELAY_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            float readDuration = (float)UnitConverter.getTime(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_DUR_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            float readAmp = (float)UnitConverter.getCurrent(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_AMP_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            IClamp ic = (IClamp)myElectricalInput;
            
            logger.logComment("Found an IClamp Input"); 
            
            float currDelay=-1, currDur=-1, currAmp=-1;
            
           /*
            try
            { 
                ic.getDelay().reset();
                currDelay = ic.getDelay().getNumber();
                ic.getDuration().reset();
                currDur = ic.getDuration().getNumber();
                ic.getAmplitude().reset();
                currAmp = ic.getAmplitude().getNumber();
            } 
            catch (Exception ex)
            {
                logger.logError("Legacy error getting iclamp params!!");
            }*/
            
            
            currDelay = ic.getDel().getNominalNumber();
            currDur = ic.getDur().getNominalNumber();     
            currAmp = ic.getAmp().getNominalNumber();
            
            
            if ((!project.elecInputInfo.getStim(inputName).getCellGroup().equals(cellGroup))
                   ||(readDelay!= currDelay)
                   ||(readDuration != currDur)
                   ||(readAmp != currAmp))                    
            {
                throw new Hdf5Exception("Error: the input properties of the file do not match those in the project for input "+ inputName+"" +
                        "\nreadDelay: "+readDelay+", currDelay: "+currDelay+
                        "\nreadDuration: "+readDuration+", currDur: "+currDur+
                        "\nreadAmp: "+readAmp+", currAmp: "+currAmp+", str: "+Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_AMP_ATTR));
            }
            currentInput = inputName;
        }
        else if (g.getName().startsWith("RandomSpikeTrain") && inInputs)
        {
            String inputName = g.getParent().getName().substring(6);
            // Get the input sites from the table
            String cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_POPULATION_ATTR);
            if (cellGroup==null)
            {
                cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_CELLGROUP_OLD_ATTR); // check old name
            }

            float frequency = (float)UnitConverter.getRate(Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.RND_STIM_FREQ_ATTR)), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
            String mechanism = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.RND_STIM_MECH_ATTR);
            
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            RandomSpikeTrain rs = (RandomSpikeTrain)myElectricalInput;
            
            logger.logComment("Found an Random Spike Train Input");
            
            if ((!project.elecInputInfo.getStim(inputName).getCellGroup().equals(cellGroup))||
                    frequency != rs.getRate().getFixedNum()||
                    !rs.getSynapseType().equals(mechanism))                    
            {
                throw new Hdf5Exception("Error: the input properties of the file do not match those in the project for input "+ inputName);
            }
            currentInput = inputName;
        }        
        
    }
    
    
    
    
    public void endGroup(Group g) throws Hdf5Exception
    {
        logger.logComment("-----   Going out of a group: "+g.getFullName());
        
        if (g.getName().equals(NetworkMLConstants.POPULATIONS_ELEMENT))
        {
            inPopulations = false;
        }
        else if (g.getName().equals(NetworkMLConstants.PROJECTIONS_ELEMENT))
        {
            inProjections = false;
        }
        else if (g.getName().equals(NetworkMLConstants.INPUTS_ELEMENT))
        {
            inInputs = false;
        }
        else if (g.getName().equals(NetworkMLConstants.INPUT_ELEMENT) && inInputs)
        {
            currentInput = null;
        }        
        else if (g.getName().startsWith(NetworkMLConstants.POPULATION_ELEMENT) && inPopulations)
        {
            currentCellGroup = null;
        }
        else if (g.getName().startsWith(NetworkMLConstants.PROJECTION_ELEMENT) && inProjections)
        {
            currentNetConn = null;
            globConnProps = new ArrayList<ConnSpecificProps>();
        }
        else if (g.getName().startsWith(NetworkMLConstants.CONNECTION_ELEMENT))
        {
            localConnProps = new ArrayList<ConnSpecificProps>();
            localAPDelay = 0;
        }
        
    }
    
    private ArrayList<String> getConnectionSynTypes()
    {
        ArrayList<String> a = new ArrayList<String>();
        
        for(ConnSpecificProps c: globConnProps)
        {
            a.add(c.synapseType);
        }
        return a;
    }
    
    
    public void dataSet(Dataset d) throws Hdf5Exception
    {
        logger.logComment("-----   Looking through dataset: "+d);
        
        ArrayList<Attribute> attrs = Hdf5Utils.parseDatasetForAttributes(d);
            
        for (Attribute attribute : attrs) 
        {
            logger.logComment("Dataset: "+d.getName()+ " has attribute: "+ attribute.getName()+" = "+ Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()));
            
        }
        
        float[][] data = Hdf5Utils.parse2Ddataset(d);
        
        logger.logComment("Data has size: ("+data.length+", "+data[0].length+")");
        
        
        if (inPopulations && currentCellGroup!=null)
        {
            for(int i = 0;i<data.length;i++)
            {
                int id = (int)data[i][0];
                float x = data[i][1];
                float y = data[i][2];
                float z = data[i][3];
                
                
                PositionRecord posRec = new PositionRecord(id,x,y,z);
                
                if (data[0].length==5)
                {
                    posRec.setNodeId((int)data[i][4]);
                }
                
                this.project.generatedCellPositions.addPosition(currentCellGroup, posRec);
            }
        }
        if (inProjections && currentNetConn!=null)
        {
            logger.logComment("Adding info for NetConn: "+ currentNetConn);
            
            int id_col = -1;
            
            int pre_cell_id_col = -1;
            int pre_segment_id_col = -1;
            int pre_fraction_along_col = -1;
            
            int post_cell_id_col = -1;
            int post_segment_id_col = -1;
            int post_fraction_along_col = -1;
            
            int prop_delay_col = -1;
            
            
            
            for (Attribute attribute : attrs) 
            {
                String storedInColumn = Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName());
                
                if (storedInColumn.equals(NetworkMLConstants.CONNECTION_ID_ATTR))
                {
                    id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    logger.logComment("id col: "+id_col);
                }
                else if (storedInColumn.equals(NetworkMLConstants.PRE_CELL_ID_ATTR))
                {
                    pre_cell_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                else if (storedInColumn.equals(NetworkMLConstants.PRE_SEGMENT_ID_ATTR))
                {
                    pre_segment_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    logger.logComment("pre_segment_id_col: "+pre_segment_id_col);
                }
                else if (storedInColumn.equals(NetworkMLConstants.PRE_FRACT_ALONG_ATTR))
                {
                    pre_fraction_along_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    logger.logComment("pre_fraction_along_col: "+pre_fraction_along_col);
                }
                
                
                else if (storedInColumn.equals(NetworkMLConstants.POST_CELL_ID_ATTR))
                {
                    post_cell_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                else if (storedInColumn.equals(NetworkMLConstants.POST_SEGMENT_ID_ATTR))
                {
                    post_segment_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                else if (storedInColumn.equals(NetworkMLConstants.POST_FRACT_ALONG_ATTR))
                {
                    post_fraction_along_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                
                
                else if (storedInColumn.startsWith(NetworkMLConstants.PROP_DELAY_ATTR))
                {
                    prop_delay_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                
                
                
                for(String synType: getConnectionSynTypes())
                {
                    if (storedInColumn.endsWith(synType))
                    {
                        ConnSpecificProps cp = null;
                        
                        for(ConnSpecificProps currCp:localConnProps)
                        {
                            if (currCp.synapseType.equals(synType))
                                cp = currCp;
                        }
                        if (cp==null)
                        {
                            cp = new ConnSpecificProps(synType);
                            cp.internalDelay = -1;
                            cp.weight = -1;
                            localConnProps.add(cp);
                        }
                        
                        if (storedInColumn.startsWith(NetworkMLConstants.INTERNAL_DELAY_ATTR))
                        {
                            cp.internalDelay = Integer.parseInt(attribute.getName().substring("column_".length())); // store the col num temporarily..
                        }
                        if (storedInColumn.startsWith(NetworkMLConstants.WEIGHT_ATTR))
                        {
                            cp.weight = Integer.parseInt(attribute.getName().substring("column_".length())); // store the col num temporarily..
                        }
                    }
                }

            }
            
            for(int i = 0;i<data.length;i++)
            {
                int pre_seg_id = 0;
                float pre_fract_along = 0.5f;
                int post_seg_id = 0;
                float post_fract_along = 0.5f;
                
                int id = (int)data[i][id_col];
                int pre_cell_id = (int)data[i][pre_cell_id_col];
                int post_cell_id = (int)data[i][post_cell_id_col];
                
                float prop_delay = 0;
                
                if (pre_segment_id_col>=0) 
                    pre_seg_id = (int)data[i][pre_segment_id_col];
                if (pre_fraction_along_col>=0) 
                    pre_fract_along = data[i][pre_fraction_along_col];
                if (post_segment_id_col>=0) 
                    post_seg_id = (int)data[i][post_segment_id_col];
                if (post_fraction_along_col>=0) 
                    post_fract_along = data[i][post_fraction_along_col];
                
                
                    //(float)UnitConverter.getTime(XXXXXXXXX, UnitConverter.NEUROCONSTRUCT_UNITS, unitSystem)+"";
                if (prop_delay_col>=0) 
                    prop_delay = (float)UnitConverter.getTime(data[i][prop_delay_col], projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                
                
                
                ArrayList<ConnSpecificProps> props = new ArrayList<ConnSpecificProps>();
                
                if (localConnProps.size()>0)
                {
                    for(ConnSpecificProps currCp:localConnProps)
                    {
                        logger.logComment("Pre cp: "+currCp);
                        ConnSpecificProps cp2 = new ConnSpecificProps(currCp.synapseType);
                        
                        if (currCp.internalDelay>0) // index was stored in this val...
                            cp2.internalDelay = (float)UnitConverter.getTime(data[i][(int)currCp.internalDelay], projUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                        if (currCp.weight>0) // index was stored in this val...
                            cp2.weight = data[i][(int)currCp.weight];
                        
                        logger.logComment("Filled cp: "+cp2);
                        
                        props.add(cp2);
                    }
                }
                
                this.project.generatedNetworkConnections.addSynapticConnection(currentNetConn,
                                                                               GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                                               pre_cell_id, 
                                                                               pre_seg_id,
                                                                               pre_fract_along,
                                                                               post_cell_id,
                                                                               post_seg_id,
                                                                               post_fract_along,
                                                                               prop_delay,
                                                                               props);
            }
            
        }
        if (inInputs && currentInput !=null)
        {
            logger.logComment("Adding info for: "+ currentInput);
            StimulationSettings nextStim = project.elecInputInfo.getStim(currentInput);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            String electricalInputType = myElectricalInput.getType();
            String cellGroup = nextStim.getCellGroup();
                    
            for(int i = 0;i<data.length;i++)
            {
                Float fileCellId = data[i][0];
                Float fileSegmentId = data[i][1];
                Float fractionAlong = data[i][2];
                int cellId = fileCellId.intValue();
                int segmentId = fileSegmentId.intValue();                
                
                SingleElectricalInput singleElectricalInputFromFile 
                        = new SingleElectricalInput(electricalInputType,
                                                    cellGroup,
                                                    cellId,
                                                    segmentId,
                                                    fractionAlong,
                                                    null);
               
                this.project.generatedElecInputs.addSingleInput(currentInput,singleElectricalInputFromFile);
            }
        }
        
        
    }
        
        
    public void parseGroup(Group g) throws Hdf5Exception, EndOfSequenceException
    {
        startGroup(g);
                
        java.util.List members = g.getMemberList();

       
        // NOTE: parsing contents twice to ensure subgroups are handled before datasets
        // This is mainly because synapse_props groups will need to be parsed before dataset of connections  
       
        
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Group)
            {
                Group subGroup = (Group)obj;
                
                logger.logComment("---------    Found a sub group: "+subGroup.getName());
                
                parseGroup(subGroup);
            }
        }
        
        for (int j=0; j<members.size(); j++)
        {
            HObject obj = (HObject)members.get(j);
            
            if (obj instanceof Dataset)
            {
                Dataset ds = (Dataset)obj;
                
                logger.logComment("Found a dataset: "+ds.getName());
                
                dataSet(ds);
            }
        }
        
        endGroup(g);
    }    
            
    
    public static void main(String args[])
    {

        try
        {
            
            logger.logComment("Sys prop: "+System.getProperty("java.library.path"), true);
            
            //File projFile = new File("../copyNcModels/NewGranCellLayer/NewGranCellLayer.neuro.xml");
            
            //File projFile = new File("../nC_projects/Bignet/Bignet.neuro.xml");
            File projFile = new File("testProjects/TestNetworkML/TestNetworkML.neuro.xml");
            
            //Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),null);
            //Project testProj = Project.loadProject(new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml"),null);
            Project testProj = Project.loadProject(projFile,null);

            //File h5File = new File(projFile.getParentFile().getAbsolutePath()+ "/savedNetworks/hhh.h5");
            //File h5File = new File(projFile.getParentFile().getAbsolutePath()+ "/savedNetworks/nnnn.h5");
            
            File h5File = new File("testProjects/TestNetworkML/savedNetworks/small.h5");

            //logger.logComment("Loading netml cell from "+ h5File.getAbsolutePath(), true);


          
            NetworkMLReader nmlReader = new NetworkMLReader(testProj);
            
            nmlReader.parse(h5File);

            logger.logComment("Contents: "+testProj.generatedCellPositions);
            logger.logComment("Net conns: "+testProj.generatedNetworkConnections);
            logger.logComment("Inputs: "+testProj.generatedElecInputs.details(false));



        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
