 /*
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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

/**
 * Utilities file for reading NetworkML HDF5 files
 *
 * @author Padraig Gleeson
 *
 */
public class NetworkMLReader  implements NetworkMLnCInfo
{    
    private static ClassLogger logger = new ClassLogger("NetworkMLReader");
        
    
    //private GeneratedCellPositions cellPos = null;

    //private GeneratedNetworkConnections netConns = null;
    
    private Project project = null;
    
    
    boolean inPopulations = false;
    boolean inProjections = false;
    boolean inInputs = false;
    //boolean inInput = false;
    
    String currentCellGroup = null;
    String currentNetConn = null;
    String currentInput = null;
    
    private ArrayList<ConnSpecificProps> globConnProps = new ArrayList<ConnSpecificProps>();
    
    private long foundRandomSeed = Long.MIN_VALUE;
    private String foundSimConfig = null;

    public NetworkMLReader(Project project)
    {
        //this.cellPos = project.generatedCellPositions;
        //this.netConns = project.generatedNetworkConnections;
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
        
    
        
    public void startGroup(Group g) throws Hdf5Exception, EndOfSequenceException
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

        }
        else if (g.getName().startsWith(NetworkMLConstants.PROJECTION_ELEMENT) && inProjections)
        {
            String name = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.PROJ_NAME_ATTR);
            String source = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.SOURCE_ATTR);
            String target = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.TARGET_ATTR);
            
            logger.logComment("Found a projection: "+ name+" from "+ source+" to "+ target);
            
            if (!project.morphNetworkConnectionsInfo.isValidSimpleNetConn(name) &&
                !project.volBasedConnsInfo.isValidAAConn(name))
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
            
            cp.internalDelay = Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INTERNAL_DELAY_ATTR));
            cp.weight = Float.parseFloat(Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.WEIGHT_ATTR));
            
            
            logger.logComment("Found: "+ cp);
            
            globConnProps.add(cp);
        }
        else if (g.getName().equals(NetworkMLConstants.INPUTS_ELEMENT))
        {
            logger.logComment("Found the Inputs group");
            inInputs = true;
            String units = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.UNITS_ATTR);
            
            if (project.genesisSettings.isSIUnits() && !units.contains("SI Units"))
            {
                throw new Hdf5Exception("Error: the project units are SI Units whereas the units in the file are not");
            }
            else if (project.genesisSettings.isPhysiologicalUnits() && !units.contains("Physiological"))
            {
                throw new Hdf5Exception("Error: the project units are Physiological Units whereas the units in the file are not");
            }
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
            String cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_CELLGROUP_ATTR);
            String delay = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_DELAY_ATTR);
            String duration = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_DUR_ATTR);
            String amp = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_AMP_ATTR);
            
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            IClamp ic = (IClamp)myElectricalInput;
            
            logger.logComment("Found an IClamp Input");            
            
            if ((!project.elecInputInfo.getStim(inputName).getCellGroup().equals(cellGroup))
                   ||(Float.valueOf(delay.trim()).floatValue()!= ic.getDelay().getNumber())
                   ||(Float.valueOf(duration.trim()).floatValue() != ic.getDuration().getNumber())
                   ||(Float.valueOf(amp.trim()).floatValue() != ic.getAmplitude().getNumber()))                    
            {
                throw new Hdf5Exception("Error: the input properties of the file do not match those in the project for input "+ inputName);
            }
            currentInput = inputName;
        }
        else if (g.getName().startsWith("RandomSpikeTrain") && inInputs)
        {
            String inputName = g.getParent().getName().substring(6);
            // Get the input sites from the table
            String cellGroup = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.INPUT_TARGET_CELLGROUP_ATTR);
            String frequency = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.RND_STIM_FREQ_ATTR);
            String mechanism = Hdf5Utils.getFirstStringValAttr(attrs, NetworkMLConstants.RND_STIM_MECH_ATTR);
            
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
            ElectricalInput myElectricalInput = nextStim.getElectricalInput();
            RandomSpikeTrain rs = (RandomSpikeTrain)myElectricalInput;
            
            logger.logComment("Found an Random Spike Train Input");
            
            if ((!project.elecInputInfo.getStim(inputName).getCellGroup().equals(cellGroup))||
                    Float.valueOf(frequency.trim()).floatValue() != rs.getRate().getFixedNum()||
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
            
            
            for (Attribute attribute : attrs) 
            {
                if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.CONNECTION_ID_ATTR))
                {
                    id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    logger.logComment("id col: "+id_col);
                }
                else if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.PRE_CELL_ID_ATTR))
                {
                    pre_cell_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                else if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.PRE_SEGMENT_ID_ATTR))
                {
                    pre_segment_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    logger.logComment("pre_segment_id_col: "+pre_segment_id_col);
                }
                else if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.PRE_FRACT_ALONG_ATTR))
                {
                    pre_fraction_along_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                    logger.logComment("pre_fraction_along_col: "+pre_fraction_along_col);
                }
                
                
                else if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.POST_CELL_ID_ATTR))
                {
                    post_cell_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                else if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.POST_SEGMENT_ID_ATTR))
                {
                    post_segment_id_col = Integer.parseInt(attribute.getName().substring("column_".length()));
                }
                else if (Hdf5Utils.getFirstStringValAttr(attrs, attribute.getName()).equals(NetworkMLConstants.POST_FRACT_ALONG_ATTR))
                {
                    post_fraction_along_col = Integer.parseInt(attribute.getName().substring("column_".length()));
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
                
                if (pre_segment_id_col>=0) 
                    pre_seg_id = (int)data[i][pre_segment_id_col];
                if (pre_fraction_along_col>=0) 
                    pre_fract_along = (float)data[i][pre_fraction_along_col];
                if (post_segment_id_col>=0) 
                    post_seg_id = (int)data[i][post_segment_id_col];
                if (post_fraction_along_col>=0) 
                    post_fract_along = (float)data[i][post_fraction_along_col];
                
                
                this.project.generatedNetworkConnections.addSynapticConnection(currentNetConn,
                                                                               GeneratedNetworkConnections.ANY_NETWORK_CONNECTION,
                                                                               pre_cell_id, 
                                                                               pre_seg_id,
                                                                               pre_fract_along,
                                                                               post_cell_id,
                                                                               post_seg_id,
                                                                               post_fract_along,
                                                                               0,
                                                                               null);
            }
            
        }
        if (inInputs && currentInput !=null)
        {
            //String inputName = g.getParent().getName().substring(6);
            String fileInputName = d.getName();
            int endIndex = fileInputName.indexOf("_");
            String inputName = fileInputName.substring(0, endIndex);
            StimulationSettings nextStim = project.elecInputInfo.getStim(inputName);
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
                
                SingleElectricalInput singleElectricalInputFromFile = new SingleElectricalInput(electricalInputType,cellGroup,cellId,segmentId,fractionAlong);
               
                this.project.generatedElecInputs.addSingleInput(inputName,singleElectricalInputFromFile);
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
            
            File projFile = new File("../nC_projects/Bignet/Bignet.neuro.xml");
            
            //Project testProj = Project.loadProject(new File("projects/Parall/Parall.neuro.xml"),null);
            //Project testProj = Project.loadProject(new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml"),null);
            Project testProj = Project.loadProject(projFile,null);

            //File h5File = new File(projFile.getParentFile().getAbsolutePath()+ "/savedNetworks/hhh.h5");
            //File h5File = new File(projFile.getParentFile().getAbsolutePath()+ "/savedNetworks/nnnn.h5");
            File h5File = new File("../temp/test.h5");

            logger.logComment("Loading netml cell from "+ h5File.getAbsolutePath(), true);


          
            NetworkMLReader nmlReader = new NetworkMLReader(testProj);
            
            nmlReader.parse(h5File);

            logger.logComment("Contents: "+testProj.generatedCellPositions);
            logger.logComment("Net conns: "+testProj.generatedNetworkConnections);



        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
