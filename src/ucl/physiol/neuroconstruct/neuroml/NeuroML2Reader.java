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

import java.io.*;
import java.nio.file.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Random;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.neuroml.export.utils.Utils;
import org.neuroml.model.Connection;
import org.neuroml.model.ConnectionWD;
import org.neuroml.model.Input;
import org.neuroml.model.InputList;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Population;
import org.neuroml.model.Projection;
import org.neuroml.model.ElectricalProjection;
import org.neuroml.model.ElectricalConnectionInstance;
import org.neuroml.model.ElectricalConnection;
import org.neuroml.model.PulseGenerator;
import org.neuroml.model.PoissonFiringSynapse;
import org.neuroml.model.TransientPoissonFiringSynapse;
import org.neuroml.model.util.NeuroMLConverter;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.converters.NeuroML2CellReader;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.cellchoice.CellChooser;
import ucl.physiol.neuroconstruct.project.cellchoice.IndividualCells;
import ucl.physiol.neuroconstruct.project.PrePostAllowedLocs;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.project.packing.CellPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.packing.CellPackingException;
import ucl.physiol.neuroconstruct.project.packing.SinglePositionedCellPackingAdapter;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainExt;
import ucl.physiol.neuroconstruct.project.stimulation.ElectricalInput;
import ucl.physiol.neuroconstruct.simulation.IClampSettings;
import ucl.physiol.neuroconstruct.simulation.RandomSpikeTrainSettings;
import ucl.physiol.neuroconstruct.simulation.RandomSpikeTrainExtSettings;
import ucl.physiol.neuroconstruct.simulation.StimulationSettings;
import java.util.regex.*;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.Segment;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanismHelper;
import ucl.physiol.neuroconstruct.mechanisms.NeuroML2Component;
import ucl.physiol.neuroconstruct.project.stimulation.IClampInstanceProps;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainInstanceProps;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrainExtInstanceProps;


/**
 * NeuroML 2 file Reader. Importer of NeuroML 2 files to neuroConstruct
 *
 * @author Padraig Gleeson
 *  
 * @author Rokas Stanislovas
 */

public class NeuroML2Reader implements NetworkMLnCInfo
{
    public static ClassLogger logger = new ClassLogger("NeuroML2Reader");

    private long foundRandomSeed = Long.MIN_VALUE;

    private String foundSimConfig = null;

    private GeneratedCellPositions cellPos = null;

    private GeneratedNetworkConnections netConns = null;
    
    private ArrayList<ConnSpecificProps> localConnProps = new ArrayList<ConnSpecificProps>();
    
    private GeneratedElecInputs elecInputs = null;    
    
    private Project project = null;
    
    private String currentPopulation = null;
    
    private String preCellType=null;
                    
    private String postCellType=null;
    
    private Cell preCellnC;
    
    private Cell postCellnC;
                    
    private int prePopSize =0;
                    
    private int postPopSize =0;
    
    private String groupCellType = "";
    
    private RectangularBox region = new RectangularBox(0, 0, 0, 100, 100, 100);
    
    private Integer priority = 0;
    
    private String currentElecInput = null;
    
    private String currentInputType = null;
    
    private String currentInputCellGroup = null;
    
    private String currentInputName = null;

    private ElectricalInput currentElectricalInput = null;
    
    private SingleElectricalInput currentSingleInput = null;
    
    private IClampInstanceProps iip=null;
    
    private RandomSpikeTrainInstanceProps rp=null;
    
    private RandomSpikeTrainExtInstanceProps trp= null;
    
    private int inputUnitSystem = -1;
    
    private SimConfig simConfigToUse = new SimConfig();
    
    public boolean testMode = false;
    
    private final static String PERSISTENT_POISSON="persistentPoisson";
    
    private final static String TRANSIENT_POISSON="transientPoisson";
    
    private final static float PICO_TO_MICRO_SCALING = (float) 0.000001;
    
    private final static float PICO_TO_ONE_SCALING = (float) 0.000000000001;
    
    private final static float PER_S_TO_PER_MS_SCALING = (float) 0.001;
    
    private final boolean inferConnectivityConditions;

    public NeuroML2Reader(Project project,boolean inferConnConditions)
    {
        this.cellPos = project.generatedCellPositions;
        this.netConns = project.generatedNetworkConnections;
        this.elecInputs = project.generatedElecInputs;
        this.project = project;
        this.testMode = false;
        this.inferConnectivityConditions=inferConnConditions;
		

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
    
    public HashMap getValueAndUnits(String delay) throws NeuroMLException
    {
        HashMap<String,String> delay_dict = new HashMap<String,String>();
        
        Pattern unit_string= Pattern.compile("[a-zA-Z&&[^E-]]+");
        
        Matcher match_delay=unit_string.matcher(delay);
        
        if(match_delay.find())
        {
           int letter_index = match_delay.start();
           
           String sep_delay = new StringBuffer(delay).insert(letter_index, "_").toString();
           
           String[] split_delay = sep_delay.split("_",2);
        
           delay_dict.put("value",split_delay[0].trim());
        
           delay_dict.put("units",split_delay[1].trim());
        }
        else
        {
           throw new NeuroMLException("neuroConstruct cannot find units inside parameter string");
        }
        
        return delay_dict;
        
    }
    
    public void addNeuroML2Cell(org.neuroml.model.Cell NML2Cell, String CellIdPrefix, File NML2CellFile) throws NeuroMLException, org.neuroml.model.util.NeuroMLException
    {
        String newCellId = CellIdPrefix+NML2Cell.getId();
                
        System.out.println("Found a NeuroML2 cell with id = "+NML2Cell.getId());
                
        //if (project.cellManager.getAllCellTypeNames().contains(newCellId)) 
        //{
            // throw new NeuroMLException("The project "+project.getProjectName() +" already contains a cell with ID "+ newCellId);
        //} 
                         
        for(String cellTypeId: project.cellManager.getAllCellTypeNames())
        {
            if(NML2Cell.getId().equals(cellTypeId))
            {
                // newly found cellType will overide the existent cell type in the project
                                
                project.cellManager.deleteCellType(project.cellManager.getCell(cellTypeId));
            }
        }
        NeuroML2CellReader cellReader= new NeuroML2CellReader(NML2Cell,newCellId);
                         
        cellReader.parse();
                         
        Cell imported_cell= cellReader.getBuiltCell();
                      
        try
        {
            project.cellManager.addCellType(imported_cell);
                         
            project.markProjectAsEdited();
                             
            logger.logComment(imported_cell.getInstanceName() + " added to the project");
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem importing NeuroML2 file "+ NML2CellFile.getAbsolutePath(), ex, null);
        }
        
        logger.logComment("Read in NeuroML 2 cell: "+ CellTopologyHelper.printDetails(imported_cell, project), true);
        
    }
    
    public void addNeuroML2Cell2CaPools(org.neuroml.model.Cell2CaPools NML2Cell, String CellIdPrefix, File NML2CellFile) throws NeuroMLException, org.neuroml.model.util.NeuroMLException
    {
        String newCellId = CellIdPrefix+NML2Cell.getId();
                
        System.out.println("Found a NeuroML2 cell with id = "+NML2Cell.getId());
                
        //if (project.cellManager.getAllCellTypeNames().contains(newCellId)) 
        //{
            // throw new NeuroMLException("The project "+project.getProjectName() +" already contains a cell with ID "+ newCellId);
        //} 
                         
        for(String cellTypeId: project.cellManager.getAllCellTypeNames())
        {
            if(NML2Cell.getId().equals(cellTypeId))
            {
                // newly found cellType will overide the existent cell type in the project
                                
                project.cellManager.deleteCellType(project.cellManager.getCell(cellTypeId));
            }
        }
        NeuroML2CellReader cellReader= new NeuroML2CellReader(NML2Cell,newCellId);
                         
        cellReader.parse();
                         
        Cell imported_cell= cellReader.getBuiltCell();
                      
        try
        {
            project.cellManager.addCellType(imported_cell);
                         
            project.markProjectAsEdited();
                             
            logger.logComment(imported_cell.getInstanceName() + " added to the project");
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem importing NeuroML2 file "+ NML2CellFile.getAbsolutePath(), ex, null);
        }
        
        logger.logComment("Read in NeuroML 2 cell: "+ CellTopologyHelper.printDetails(imported_cell, project), true);
        
    }
    
    public void addNeuroML2CellMechanism(String cellMechanismId, String CellMechanismType, File NML2CellMechanismFile, String CellMechanismNotes)    
    {   
        String PathToNML2CellMechanism= NML2CellMechanismFile.getPath();
                            
        System.out.println("Full path to NeuroML2 cell mechanism: "+PathToNML2CellMechanism);
                            
        File CMLDir = ProjectStructure.getCellMechanismDir(project.getProjectMainDirectory());
                            
        System.out.println("Cell Mechanism dir: "+CMLDir.getPath());
                            
        File newChannelDir = new File(CMLDir, cellMechanismId);
                            
        System.out.println("Cell mechanism will be copied to: "+newChannelDir.getPath());
                            
        newChannelDir.mkdirs();
                            
        File ChannelFile = new File(newChannelDir,cellMechanismId+ ".xml");
                            
        FileWriter fw;
        
        try
        {
            fw = new FileWriter(ChannelFile);
            
            fw.close();
        }
        catch (IOException ex)
        {
            logger.logComment("Problem creating a new file: " +ChannelFile);
        }
                            
        Path CopyIonChannel = FileSystems.getDefault().getPath(PathToNML2CellMechanism);
                            
        String FullSavedPath=ChannelFile.getPath();
                            
        Path CopyIonChannelTo= FileSystems.getDefault().getPath(FullSavedPath);
                            
        System.out.println("A full path of a saved channel:"+FullSavedPath);
        
        try
        {
            Files.copy(CopyIonChannel, CopyIonChannelTo, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ex) 
        {
            GuiUtils.showErrorMessage(logger,"Problem copying a cell mechanism "
                    +cellMechanismId+" from "+PathToNML2CellMechanism+" to "+FullSavedPath , ex, null);
        }
        // only testing; might require another class for NeuroML2
                            
        NeuroML2Component cmlMech = new NeuroML2Component();
                            
        cmlMech.setInstanceName(cellMechanismId);
                            
        cmlMech.setMechanismType(CellMechanismType);
                            
        cmlMech.setDescription(CellMechanismNotes);
                            
        cmlMech.setMechanismModel("NeuroML 2 cell mechanism imported from a network");
                            
        cmlMech.setXMLFile(ChannelFile.getName());
                            
        File propsFile = new File(newChannelDir, CellMechanismHelper.PROPERTIES_FILENAME);
                            
        Properties cellMechProps = new Properties();
                            
        cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_MODEL, cmlMech.getMechanismModel());
                            
        cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_TYPE, cmlMech.getMechanismType());
                            
        cellMechProps.setProperty(CellMechanismHelper.PROP_CHANNELML_FILE, cmlMech.getXMLFile());
                            
        cellMechProps.setProperty(CellMechanismHelper.PROP_IMPL_METHOD,CellMechanism.NEUROML2_BASED_CELL_MECHANISM);
                            
        cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_NAME, cmlMech.getInstanceName());
                            
        cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_DESCRIPTION, cmlMech.getDescription());
                            
        try
        {
            FileOutputStream fos2 = new FileOutputStream(propsFile);
                                
            cellMechProps.storeToXML(fos2,
                "\nProperties associated with the Cell Mechanism which allow it to be loaded into neuroConstruct.\n\n"
                            + "Note the following: \n" +
                            "   The Cell Mechanism name should not contain spaces and should match the name of the directory it's in\n" +
                            "   The name and description here will be replaced by the corresponding values in a ChannelML file if found\n" +
                            "   The filenames for the mappings are relative to the cellMechanism/(cellMechInstanceName) directory\n" +
                            "   Mechanism Type should only have values: " +
                            CellMechanism.CHANNEL_MECHANISM + ", " + CellMechanism.SYNAPTIC_MECHANISM
                            + ", " + CellMechanism.ION_CONCENTRATION + ", " + CellMechanism.POINT_PROCESS + ", " + CellMechanism.GAP_JUNCTION + "\n\n");
                            fos2.close();

        }
                            
        catch (Exception ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error storing information on NeuroML2 Cell Mechanism: " + cmlMech.getInstanceName(), ex2, null);
        }
                            
        try
        {
                             
            cmlMech.initPropsFromPropsFile(propsFile);
                             
        }
        catch (IOException ex4)
        {
            GuiUtils.showErrorMessage(logger,"Error initiating cell mechanism properties in: " +newChannelDir, ex4, null);
        }
                            
        project.cellMechanismInfo.addCellMechanism(cmlMech);
        
    }
    
    public ConnectivityConditions determineConnectivityConditions(Projection projection, 
            int PrePopulationSize, int PostPopulationSize, String preCellComponentType, String postCellComponentType)    
    {   
        Set<Integer> preCellSet= new HashSet<Integer>();
      
        Set<Integer> postCellSet= new HashSet<Integer>();
        
        Set<Integer> postCellAppearance = new HashSet<Integer>();
        
        Set<Integer> preCellAppearance = new HashSet<Integer>();
        
        ArrayList<Integer> preCellList = new ArrayList<Integer>();
        
        ArrayList<Integer> postCellList= new ArrayList<Integer>();
        
        HashMap<Integer,Integer> presynapticDict = new HashMap<Integer,Integer>();
        
        HashMap<Integer,Integer> postsynapticDict = new HashMap<Integer,Integer>();
        
        TreeMap<String, Integer> preSegPerSecCounters= new TreeMap<String,Integer>();
        
        TreeMap<String, Integer> postSegPerSecCounters= new TreeMap<String,Integer>();
        
        LinkedHashMap<String, Integer> preSegPerSecCountersSorted= new LinkedHashMap<String,Integer>();
        
        LinkedHashMap<String, Integer> postSegPerSecCountersSorted= new LinkedHashMap<String,Integer>();
        
        preCellnC = project.cellManager.getCell(preCellComponentType);
                    
        postCellnC=project.cellManager.getCell(postCellComponentType);
        
        Set<Integer> postCellSegIds= new HashSet<Integer>();
        
        Set<Integer> preCellSegIds = new HashSet<Integer>();
        
        ArrayList<Segment> preCellSegs= new ArrayList<Segment>();
        
        ArrayList<Segment> postCellSegs= new ArrayList<Segment>();
        
        ArrayList<String> potentialPreSegGroups = new ArrayList<String>();
        
        ArrayList<String> potentialPostSegGroups = new ArrayList<String>();
        
        boolean autapsesAllowed= false;
        
        boolean preAxonGroupContainsAll= false;
        
        boolean preSomaGroupContainsAll = false;
        
        boolean preDendriteGroupContainsAll= false;
        
        boolean postAxonGroupContainsAll= false;
        
        boolean postSomaGroupContainsAll = false;
        
        boolean postDendriteGroupContainsAll= false;
        
        boolean minimalSetOfPreSegGroups = false;
        
        boolean minimalSetOfPostSegGroups = false;
        
        boolean preAxonAllowed= false;
        
        boolean preSomaAllowed = false;
        
        boolean preDendriteAllowed = false;
        
        boolean postAxonAllowed= false;
        
        boolean postSomaAllowed = false;
        
        boolean postDendriteAllowed = false;
        
        PrePostAllowedLocs lociAllowed = new PrePostAllowedLocs();
        
        boolean setSynLoci = false;
        
        int totalNumOfSegs = 0;
        
        ConnectivityConditions connConds = new ConnectivityConditions();
        
        String synapse =  projection.getSynapse();
        
        int cellCounter;
    
        if (!projection.getConnection().isEmpty())
        {
          for (Connection conn: projection.getConnection())
          {
              int preSeg = conn.getPreSegmentId();
              int postSeg = conn.getPostSegmentId();
                        
              int preCellId=parseForCellNumber(conn.getPreCellId());
                        
              int postCellId=parseForCellNumber(conn.getPostCellId());
              
              if ((preCellId == postCellId) && 
                    (projection.getPostsynapticPopulation().equals(projection.getPresynapticPopulation())))
              {
                  autapsesAllowed= true;
              }
            
              preCellSet.add(preCellId);
            
              preCellList.add(preCellId);
            
              postCellSet.add(postCellId);
            
              postCellList.add(postCellId);
              
              preCellSegIds.add(preSeg);
              
              postCellSegIds.add(postSeg);
                        
          }
        }
        
        else if (!projection.getConnectionWD().isEmpty())
        {
          for (ConnectionWD conn: projection.getConnectionWD())
          {
              int preSeg = conn.getPreSegmentId();
              int postSeg = conn.getPostSegmentId();
                        
              int preCellId=parseForCellNumber(conn.getPreCellId());
                        
              int postCellId=parseForCellNumber(conn.getPostCellId());
              
              if ((preCellId == postCellId) && 
                    (projection.getPostsynapticPopulation().equals(projection.getPresynapticPopulation())))
              {
                  autapsesAllowed= true;
              }
            
              preCellSet.add(preCellId);
            
              preCellList.add(preCellId);
            
              postCellSet.add(postCellId);
            
              postCellList.add(postCellId);
              
              preCellSegIds.add(preSeg);
              
              postCellSegIds.add(postSeg);
                        
          }
        }
        
        for (Integer segId: preCellSegIds)
        {
            Segment targetSeg= preCellnC.getSegmentWithId(segId);
            
            preCellSegs.add(targetSeg);
        }
        
        for (Integer segId: postCellSegIds)
        {
            Segment targetSeg = postCellnC.getSegmentWithId(segId);
          
            postCellSegs.add(targetSeg);
        }
        
        int numOfPreSegs= preCellSegIds.size();
        
        System.out.println("Number of unique pre seg ids: "+numOfPreSegs);
        
        int numOfPostSegs= postCellSegIds.size();
        
        System.out.println("Number of unique post seg ids: "+numOfPostSegs);
        
        for (String segmentGroup: preCellnC.getAllGroupNames() )
        {
           int segCounter = 0;
            
           ArrayList<Section> allSectionsPerGroup= preCellnC.getSectionsInGroup(segmentGroup);
                           
           for (Section section: allSectionsPerGroup)
           {   
                for (Segment preSeg: preCellSegs)
                {    
                    String sectionName= preSeg.getSection().getSectionName(); 
                    
                    if (section.getSectionName().equals(sectionName))
                    {
                        segCounter++;
                    }
                }
           }
           if ((segCounter != 0) && (!segmentGroup.contains("ModelView")) && (!segmentGroup.equals("all")) )
           {
               preSegPerSecCounters.put(segmentGroup, segCounter);  
           }
        }
        
        for (String segmentGroup: postCellnC.getAllGroupNames() )
        {
           int segCounter = 0;
            
           ArrayList<Section> allSectionsPerGroup= postCellnC.getSectionsInGroup(segmentGroup);
                           
           for (Section section: allSectionsPerGroup)
           {   
                for (Segment postSeg: postCellSegs)
                {    
                    String sectionName= postSeg.getSection().getSectionName(); 
                    
                    if (section.getSectionName().equals(sectionName))
                    {
                        segCounter++;
                    }
                }
           }
           if ((segCounter != 0)&& (!segmentGroup.contains("ModelView")) && (!segmentGroup.equals("all")) )
           {
              postSegPerSecCounters.put(segmentGroup, segCounter);
           }  
        }
        
        int maxPreSet= Collections.max(preSegPerSecCounters.values());
        
        int maxPostSet = Collections.max(postSegPerSecCounters.values());
        
        while(!preSegPerSecCounters.isEmpty())
        {
          for (String segGroup: preSegPerSecCounters.keySet())
          {
            if(maxPreSet == preSegPerSecCounters.get(segGroup))
            {
                preSegPerSecCountersSorted.put(segGroup,preSegPerSecCounters.get(segGroup));
                
                preSegPerSecCounters.remove(segGroup);
                
                if (!preSegPerSecCounters.isEmpty())
                {
                  maxPreSet= Collections.max(preSegPerSecCounters.values());
                }
                break;
            }
          }
        }
        
        while(!postSegPerSecCounters.isEmpty())
        {
          for (String segGroup: postSegPerSecCounters.keySet())
          {
            if(maxPostSet == postSegPerSecCounters.get(segGroup))
            {
                postSegPerSecCountersSorted.put(segGroup,postSegPerSecCounters.get(segGroup));
                
                postSegPerSecCounters.remove(segGroup);
                
                if (!postSegPerSecCounters.isEmpty())
                {
                  maxPostSet= Collections.max(postSegPerSecCounters.values());
                }
                break;
            }
          }
        }
        
        System.out.println("Testing order pre segs: "+preSegPerSecCountersSorted);
        
        System.out.println("Testing order post segs: "+postSegPerSecCountersSorted);
        
        System.out.println("Testing order of pre seg keys: "+preSegPerSecCountersSorted.keySet());
        
        System.out.println("Testing order of pre seg keys: "+postSegPerSecCountersSorted.keySet());
        
        for (String segGroup: preSegPerSecCountersSorted.keySet())
        {   
            if( (!segGroup.equals("axon_group")) && (!segGroup.equals("soma_group")) 
                        &&(!segGroup.equals("dendrite_group"))        )
            {
                    potentialPreSegGroups.add(segGroup);
                    
                    totalNumOfSegs = preSegPerSecCountersSorted.get(segGroup)+ totalNumOfSegs;
                    
                    if (segGroup.contains("dend"))
                    {
                        preDendriteAllowed= true;
                    }
                    
                    if (segGroup.contains("soma"))
                    {
                        preSomaAllowed= true;
                    }
                    
                    if (segGroup.contains("axon"))
                    {
                        preAxonAllowed= true;
                    }
                    
                    // cases such as "apic_shaft"
                    if( (!segGroup.contains("dend")) && (!segGroup.contains("soma")) && (!segGroup.contains("axon")) )
                    {
                        preDendriteAllowed = true;
                    }
            }
            
            if(preSegPerSecCountersSorted.get(segGroup) >= numOfPreSegs)
            {
                
                if (segGroup.equals("axon_group"))
                {
                    preAxonGroupContainsAll=true;
                }
                
                if (segGroup.equals("soma_group"))
                {
                    preSomaGroupContainsAll=true;
                }
                
                if (segGroup.equals("dendrite_group"))
                {
                    preDendriteGroupContainsAll= true;
                }
            }
            
            if (totalNumOfSegs >= numOfPreSegs)
            {
                minimalSetOfPreSegGroups = true;
                
                break;
            }
            
        }
        if (!minimalSetOfPreSegGroups)
        {
            preAxonAllowed= false;
            preSomaAllowed= false;
            preDendriteAllowed= false;
            
            if(preAxonGroupContainsAll)
            {
                preCellnC.associateGroupWithSynapse("axon_group", synapse);
                
                preAxonAllowed=true;
            }
            else if (preSomaGroupContainsAll )
            {
                preCellnC.associateGroupWithSynapse("soma_group", synapse); 
                
                preSomaAllowed=true;
            }
            else if (preDendriteGroupContainsAll )
            {
                 preCellnC.associateGroupWithSynapse("dendrite_group", synapse); 
                 
                 preDendriteAllowed=true;
            }
            //// revert to the widest scope of the segment group specificty
            else
            {
                preCellnC.associateGroupWithSynapse("axon_group", synapse);
                preCellnC.associateGroupWithSynapse("soma_group", synapse);
                preCellnC.associateGroupWithSynapse("dendrite_group", synapse);
                preAxonAllowed=true;
                preSomaAllowed=true;
                preDendriteAllowed=true;
            }
        }
        else
        {
            for(String preSegGroup: potentialPreSegGroups)
            {
                preCellnC.associateGroupWithSynapse(preSegGroup, synapse);
            }
        }
        
        totalNumOfSegs= 0;
        
        for (String segGroup: postSegPerSecCountersSorted.keySet())
        {   
            if( (!segGroup.equals("axon_group")) && (!segGroup.equals("soma_group")) 
                        &&(!segGroup.equals("dendrite_group"))        )
            {
                    potentialPostSegGroups.add(segGroup);
                    
                    totalNumOfSegs = postSegPerSecCountersSorted.get(segGroup)+ totalNumOfSegs;
                    
                    if (segGroup.contains("dend"))
                    {
                        postDendriteAllowed= true;
                    }
                    
                    if (segGroup.contains("soma"))
                    {
                        postSomaAllowed= true;
                    }
                    
                    if (segGroup.contains("axon"))
                    {
                        postAxonAllowed= true;
                    }
                    
                    // cases such as "apic_shaft"
                    if( (!segGroup.contains("dend")) && (!segGroup.contains("soma")) && (!segGroup.contains("axon")) )
                    {
                        postDendriteAllowed = true;
                    }
            }
            
            if(postSegPerSecCountersSorted.get(segGroup) >= numOfPostSegs)
            {
                
                if (segGroup.equals("axon_group"))
                {
                    postAxonGroupContainsAll=true;
                }
                
                if (segGroup.equals("soma_group"))
                {
                    postSomaGroupContainsAll=true;
                }
                
                if (segGroup.equals("dendrite_group"))
                {
                    postDendriteGroupContainsAll= true;
                }
            }
            
            if (totalNumOfSegs >= numOfPostSegs)
            {
                minimalSetOfPostSegGroups = true;
                
                break;
            }
            
        }
        if (!minimalSetOfPostSegGroups)
        {
            postAxonAllowed = false;
            postSomaAllowed= false;
            postDendriteAllowed= false;
            
            if(postAxonGroupContainsAll)
            {
                postCellnC.associateGroupWithSynapse("axon_group", synapse);
                
                postAxonAllowed=true;
            }
            else if (postSomaGroupContainsAll )
            {
                postCellnC.associateGroupWithSynapse("soma_group", synapse);  
                
                postSomaAllowed=true;
            }
            else if (postDendriteGroupContainsAll )
            {
                 postCellnC.associateGroupWithSynapse("dendrite_group", synapse); 
                 
                 postDendriteAllowed=true;
            }
            //// revert to the widest scope of the segment group specificty
            else
            {
                postCellnC.associateGroupWithSynapse("axon_group", synapse);
                postCellnC.associateGroupWithSynapse("soma_group", synapse);
                postCellnC.associateGroupWithSynapse("dendrite_group", synapse);
                postAxonAllowed=true;
                postSomaAllowed=true;
                postDendriteAllowed=true;
            }
        }
        else
        {
            for(String postSegGroup: potentialPostSegGroups)
            {
                postCellnC.associateGroupWithSynapse(postSegGroup, synapse);
            }
        }
        ////////////////////////////////////////////////////////////////
        System.out.println("Printing presynaptic cell ids for "+projection.getId()+": "+preCellSet);
        
        System.out.println("Printing postsynaptic cell ids for"+projection.getId()+": "+postCellSet);
        
        for (Integer preCellId: preCellSet)
        {
            cellCounter = 0;
            
            for (Integer preCell: preCellList)
            {
              if (preCellId == preCell)
              {
                  cellCounter ++;
              }
            }
            presynapticDict.put(preCellId,cellCounter);
        }
        
        for (Integer postCellId: postCellSet)
        {
            cellCounter = 0;
            
            for (Integer postCell: postCellList)
            {
              if (postCellId == postCell)
              {
                  cellCounter ++;
              }
            }
            postsynapticDict.put(postCellId,cellCounter);
        }
        
        for (Integer CellId : presynapticDict.keySet())
        {
            preCellAppearance.add(presynapticDict.get(CellId));
            
        }
        
        for (Integer CellId : postsynapticDict.keySet())
        {
            postCellAppearance.add(postsynapticDict.get(CellId));
            
        }
        
        
        System.out.println("Presynaptic dictionary for "+projection.getId()+": "+presynapticDict);
        
        System.out.println("Postsynaptic dictionary for "+projection.getId()+": "+postsynapticDict);
        
        System.out.println("Values of presynaptic topology dictionary for "+projection.getId()+": "+preCellAppearance);
        
        System.out.println("Values of postsynaptic topology dictionary for "+projection.getId()+": "+postCellAppearance);
        
        if(postCellAppearance.size() ==1)
        {
            int NumPerPostCell=0;
            
            int NumPerPreCell=0;
            
            if(preCellAppearance.size()==1)
            {
               
               for(Integer value: postCellAppearance)
               {
                   NumPerPostCell= value;
               }
               for(Integer value: preCellAppearance)
               {
                   NumPerPreCell = value;
               }
               
               if (NumPerPreCell>NumPerPostCell)
               {
                  connConds.setGenerationDirection(1);
                  connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPostCell));
                  System.out.println("This is a convergent projection with "+NumPerPostCell+" connections per postsynaptic target cell");
               }
               else if (NumPerPreCell < NumPerPostCell)
               {
                  connConds.setGenerationDirection(0);
                  connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPreCell));
                  System.out.println("This a divergent projection with "+NumPerPreCell+" connections per presynaptic source cell");
               }
               else if ((NumPerPreCell==1) && (NumPerPostCell==1))
               {
                   connConds.setOnlyConnectToUniqueCells(true);
                   
               }
            }
            else
            {
               for(Integer value: postCellAppearance)
               {
                   NumPerPostCell= value;
               }
               connConds.setGenerationDirection(1);
               connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPostCell));
               System.out.println("This is a convergent projection with "+NumPerPostCell+" connections per postsynaptic target cell");

            }
        }
        else
        {
          if(preCellAppearance.size() ==1)
          {
            int NumPerPreCell =0;
            
            for(Integer value: preCellAppearance)
            {
                NumPerPreCell= value;
                
            }
            connConds.setGenerationDirection(0);
            connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPreCell));
             System.out.println("This a divergent projection with "+NumPerPreCell+" connections per presynaptic source cell");
          }
          else
          {
              //TODO, for now stays to the default values in neuroConstruct, e.g. SOURCE_TO_TARGET only
          }
          
        }
        if(postAxonAllowed || postDendriteAllowed || postSomaAllowed)
        {
          lociAllowed.setAxonsAllowedPost(postAxonAllowed);
          lociAllowed.setDendritesAllowedPost(postDendriteAllowed);
          lociAllowed.setSomaAllowedPost(postSomaAllowed);
          
          setSynLoci = true;
        }
        if(preAxonAllowed || preDendriteAllowed || preSomaAllowed)
        {
          lociAllowed.setAxonsAllowedPre(preAxonAllowed);
          lociAllowed.setDendritesAllowedPre(preDendriteAllowed);
          lociAllowed.setSomaAllowedPre(preSomaAllowed);
          
          setSynLoci = true;
          
        }
        if (setSynLoci)
        {
            connConds.setPrePostAllowedLoc(lociAllowed); 
        }
        
        connConds.setAllowAutapses(autapsesAllowed);
        
        return connConds;
        
    }
    ////// overload method for handling electrical projections /////////////////////////////////////
    public ConnectivityConditions determineConnectivityConditions(ElectricalProjection projection, 
            int PrePopulationSize, int PostPopulationSize, String preCellComponentType, String postCellComponentType)    
    {   
        Set<Integer> preCellSet= new HashSet<Integer>();
      
        Set<Integer> postCellSet= new HashSet<Integer>();
        
        Set<Integer> postCellAppearance = new HashSet<Integer>();
        
        Set<Integer> preCellAppearance = new HashSet<Integer>();
        
        ArrayList<Integer> preCellList = new ArrayList<Integer>();
        
        ArrayList<Integer> postCellList= new ArrayList<Integer>();
        
        HashMap<Integer,Integer> presynapticDict = new HashMap<Integer,Integer>();
        
        HashMap<Integer,Integer> postsynapticDict = new HashMap<Integer,Integer>();
        
        TreeMap<String, Integer> preSegPerSecCounters= new TreeMap<String,Integer>();
        
        TreeMap<String, Integer> postSegPerSecCounters= new TreeMap<String,Integer>();
        
        LinkedHashMap<String, Integer> preSegPerSecCountersSorted= new LinkedHashMap<String,Integer>();
        
        LinkedHashMap<String, Integer> postSegPerSecCountersSorted= new LinkedHashMap<String,Integer>();
        
        ArrayList<String> potentialPreSegGroups = new ArrayList<String>();
        
        ArrayList<String> potentialPostSegGroups = new ArrayList<String>();
        
        preCellnC = project.cellManager.getCell(preCellComponentType);
                    
        postCellnC=project.cellManager.getCell(postCellComponentType);
        
        Set<Integer> postCellSegIds= new HashSet<Integer>();
        
        Set<Integer> preCellSegIds = new HashSet<Integer>();
        
        ArrayList<Segment> preCellSegs= new ArrayList<Segment>();
        
        ArrayList<Segment> postCellSegs= new ArrayList<Segment>();
        
        ArrayList<String> gapJunctionList = new ArrayList<String>();
        
        boolean autapsesAllowed= false;
        
        boolean preAxonGroupContainsAll= false;
        
        boolean preSomaGroupContainsAll = false;
        
        boolean preDendriteGroupContainsAll= false;
        
        boolean postAxonGroupContainsAll= false;
        
        boolean postSomaGroupContainsAll = false;
        
        boolean postDendriteGroupContainsAll= false;
        
        boolean minimalSetOfPreSegGroups = false;
        
        boolean minimalSetOfPostSegGroups = false;
        
        boolean preAxonAllowed= false;
        
        boolean preSomaAllowed = false;
        
        boolean preDendriteAllowed = false;
        
        boolean postAxonAllowed= false;
        
        boolean postSomaAllowed = false;
        
        boolean postDendriteAllowed = false;
        
        PrePostAllowedLocs lociAllowed = new PrePostAllowedLocs();
        
        int totalNumOfSegs = 0;
        
        ConnectivityConditions connConds = new ConnectivityConditions();
        
        int cellCounter;
    
        if (!projection.getElectricalConnection().isEmpty())
        {
          for (ElectricalConnection conn: projection.getElectricalConnection())
          {
              int preSeg = conn.getPreSegment();
              int postSeg = conn.getPostSegment();
                        
              int preCellId=parseForCellNumber(conn.getPreCell());
                        
              int postCellId=parseForCellNumber(conn.getPostCell());
              
              if ((preCellId == postCellId) && 
                    (projection.getPostsynapticPopulation().equals(projection.getPresynapticPopulation())))
              {
                  autapsesAllowed= true;
              }
            
              preCellSet.add(preCellId);
            
              preCellList.add(preCellId);
            
              postCellSet.add(postCellId);
            
              postCellList.add(postCellId);
              
              preCellSegIds.add(preSeg);
              
              postCellSegIds.add(postSeg);
              
              gapJunctionList.add(conn.getSynapse());
                        
          }
        }
        
        else if (!projection.getElectricalConnectionInstance().isEmpty())
        {
          for (ElectricalConnectionInstance conn: projection.getElectricalConnectionInstance())
          {
              int preSeg = conn.getPreSegment();
              int postSeg = conn.getPostSegment();
                        
              int preCellId=parseForCellNumber(conn.getPreCell());
                        
              int postCellId=parseForCellNumber(conn.getPostCell());
              
              if ((preCellId == postCellId) && 
                    (projection.getPostsynapticPopulation().equals(projection.getPresynapticPopulation())))
              {
                  autapsesAllowed= true;
              }
            
              preCellSet.add(preCellId);
            
              preCellList.add(preCellId);
            
              postCellSet.add(postCellId);
            
              postCellList.add(postCellId);
              
              preCellSegIds.add(preSeg);
              
              postCellSegIds.add(postSeg);
              
              gapJunctionList.add(conn.getSynapse());
                        
          }
        }
        
        for (Integer segId: preCellSegIds)
        {
            Segment targetSeg= preCellnC.getSegmentWithId(segId);
            
            preCellSegs.add(targetSeg);
        }
        
        for (Integer segId: postCellSegIds)
        {
            Segment targetSeg = postCellnC.getSegmentWithId(segId);
          
            postCellSegs.add(targetSeg);
        }
        
        int numOfPreSegs= preCellSegIds.size();
        
        System.out.println("Number of unique pre seg ids: "+numOfPreSegs);
        
        int numOfPostSegs= postCellSegIds.size();
        
        System.out.println("Number of unique post seg ids: "+numOfPostSegs);
        
        for (String segmentGroup: preCellnC.getAllGroupNames() )
        {
           int segCounter = 0;
            
           ArrayList<Section> allSectionsPerGroup= preCellnC.getSectionsInGroup(segmentGroup);
                           
           for (Section section: allSectionsPerGroup)
           {   
                for (Segment preSeg: preCellSegs)
                {    
                    String sectionName= preSeg.getSection().getSectionName(); 
                    
                    if (section.getSectionName().equals(sectionName))
                    {
                        segCounter++;
                    }
                }
           }
           if ((segCounter != 0)  && (!segmentGroup.contains("ModelView"))  && (!segmentGroup.equals("all")) )
           {
               preSegPerSecCounters.put(segmentGroup, segCounter);  
           }
        }
        
        for (String segmentGroup: postCellnC.getAllGroupNames() )
        {
           int segCounter = 0;
            
           ArrayList<Section> allSectionsPerGroup= postCellnC.getSectionsInGroup(segmentGroup);
                           
           for (Section section: allSectionsPerGroup)
           {   
                for (Segment postSeg: postCellSegs)
                {    
                    String sectionName= postSeg.getSection().getSectionName(); 
                    
                    if (section.getSectionName().equals(sectionName))
                    {
                        segCounter++;
                    }
                }
           }
           if ((segCounter != 0)  && (!segmentGroup.contains("ModelView")) && (!segmentGroup.equals("all")) )
           {
              postSegPerSecCounters.put(segmentGroup, segCounter);
           }  
        }
        
        int maxPreSet= Collections.max(preSegPerSecCounters.values());
        
        int maxPostSet = Collections.max(postSegPerSecCounters.values());
        
        while(!preSegPerSecCounters.isEmpty())
        {
          for (String segGroup: preSegPerSecCounters.keySet())
          {
            if(maxPreSet == preSegPerSecCounters.get(segGroup))
            {
                preSegPerSecCountersSorted.put(segGroup,preSegPerSecCounters.get(segGroup));
                
                preSegPerSecCounters.remove(segGroup);
                
                if (!preSegPerSecCounters.isEmpty())
                {
                  maxPreSet= Collections.max(preSegPerSecCounters.values());
                }
                break;
            }
          }
        }
        
        while(!postSegPerSecCounters.isEmpty())
        {
          for (String segGroup: postSegPerSecCounters.keySet())
          {
            if(maxPostSet == postSegPerSecCounters.get(segGroup))
            {
                postSegPerSecCountersSorted.put(segGroup,postSegPerSecCounters.get(segGroup));
                
                postSegPerSecCounters.remove(segGroup);
                
                if (!postSegPerSecCounters.isEmpty())
                {
                  maxPostSet= Collections.max(postSegPerSecCounters.values());
                }
                break;
            }
          }
        }
        
        System.out.println("Testing order pre segs: "+preSegPerSecCountersSorted);
        
        System.out.println("Testing order post segs: "+postSegPerSecCountersSorted);
        
        System.out.println("Testing order of pre seg keys: "+preSegPerSecCountersSorted.keySet());
        
        System.out.println("Testing order of pre seg keys: "+postSegPerSecCountersSorted.keySet());
        
        for (String segGroup: preSegPerSecCountersSorted.keySet())
        {   
            if( (!segGroup.equals("axon_group")) && (!segGroup.equals("soma_group")) 
                        &&(!segGroup.equals("dendrite_group"))        )
            {
                    potentialPreSegGroups.add(segGroup);
                    
                    totalNumOfSegs = preSegPerSecCountersSorted.get(segGroup)+ totalNumOfSegs;
                    
                    if (segGroup.contains("dend"))
                    {
                        preDendriteAllowed= true;
                    }
                    
                    if (segGroup.contains("soma"))
                    {
                        preSomaAllowed= true;
                    }
                    
                    if (segGroup.contains("axon"))
                    {
                        preAxonAllowed= true;
                    }
            }
            
            if(preSegPerSecCountersSorted.get(segGroup) >= numOfPreSegs)
            {
                
                if (segGroup.equals("axon_group"))
                {
                    preAxonGroupContainsAll=true;
                }
                
                if (segGroup.equals("soma_group"))
                {
                    preSomaGroupContainsAll=true;
                }
                
                if (segGroup.equals("dendrite_group"))
                {
                    preDendriteGroupContainsAll= true;
                }
            }
            
            if (totalNumOfSegs >= numOfPreSegs)
            {
                minimalSetOfPreSegGroups = true;
                
                break;
            }
            
        }
        if (!minimalSetOfPreSegGroups)
        {   
            preAxonAllowed = false;
            preSomaAllowed= false;
            preDendriteAllowed= false;
            
            if(preAxonGroupContainsAll)
            {
                for(String gapJunctionId: gapJunctionList)
                {
                   preCellnC.associateGroupWithSynapse("axon_group", gapJunctionId);
                }
                
                preAxonAllowed=true;
            }
            else if (preSomaGroupContainsAll )
            {
                for(String gapJunctionId: gapJunctionList)
                {
                  preCellnC.associateGroupWithSynapse("soma_group", gapJunctionId); 
                }
                
                preSomaAllowed=true;
            }
            else if (preDendriteGroupContainsAll )
            {
                for(String gapJunctionId: gapJunctionList)
                {
                   preCellnC.associateGroupWithSynapse("dendrite_group", gapJunctionId); 
                }
                 
                 preDendriteAllowed=true;
            }
            //// revert to the widest scope of the segment group specificty
            else
            {
                for(String gapJunctionId: gapJunctionList)
                {
                   preCellnC.associateGroupWithSynapse("axon_group", gapJunctionId);
                   preCellnC.associateGroupWithSynapse("soma_group", gapJunctionId);
                   preCellnC.associateGroupWithSynapse("dendrite_group", gapJunctionId);
                }
                preAxonAllowed=true;
                preSomaAllowed=true;
                preDendriteAllowed=true;
            }
        }
        else
        {
            for(String gapJunctionId: gapJunctionList)
            {
               for(String preSegGroup: potentialPreSegGroups)
               {
                    preCellnC.associateGroupWithSynapse(preSegGroup, gapJunctionId);
               }
            }
        }
        
        totalNumOfSegs= 0;
        
        for (String segGroup: postSegPerSecCountersSorted.keySet())
        {   
            if( (!segGroup.equals("axon_group")) && (!segGroup.equals("soma_group")) 
                        &&(!segGroup.equals("dendrite_group"))        )
            {
                    potentialPostSegGroups.add(segGroup);
                    
                    totalNumOfSegs = postSegPerSecCountersSorted.get(segGroup)+ totalNumOfSegs;
                    
                    if (segGroup.contains("dend"))
                    {
                        postDendriteAllowed= true;
                    }
                    
                    if (segGroup.contains("soma"))
                    {
                        postSomaAllowed= true;
                    }
                    
                    if (segGroup.contains("axon"))
                    {
                        postAxonAllowed= true;
                    }
            }
            
            if(postSegPerSecCountersSorted.get(segGroup) >= numOfPostSegs)
            {
                
                if (segGroup.equals("axon_group"))
                {
                    postAxonGroupContainsAll=true;
                }
                
                if (segGroup.equals("soma_group"))
                {
                    postSomaGroupContainsAll=true;
                }
                
                if (segGroup.equals("dendrite_group"))
                {
                    postDendriteGroupContainsAll= true;
                }
            }
            
            if (totalNumOfSegs >= numOfPostSegs)
            {
                minimalSetOfPostSegGroups = true;
                
                break;
            }
            
        }
        if (!minimalSetOfPostSegGroups)
        {   
            postAxonAllowed =false;
            postSomaAllowed= false;
            postDendriteAllowed= false;
            
            if(postAxonGroupContainsAll)
            {
                for(String gapJunctionId: gapJunctionList)
                {
                   postCellnC.associateGroupWithSynapse("axon_group", gapJunctionId);
                }
                
                postAxonAllowed=true;
            }
            else if (postSomaGroupContainsAll )
            {
                for(String gapJunctionId: gapJunctionList)
                {
                    postCellnC.associateGroupWithSynapse("soma_group", gapJunctionId);  
                }
                
                postSomaAllowed=true;
            }
            else if (postDendriteGroupContainsAll )
            {
                for(String gapJunctionId: gapJunctionList)
                {
                  postCellnC.associateGroupWithSynapse("dendrite_group", gapJunctionId);  
                }
                 
                 postDendriteAllowed=true;
            }
            //// revert to the widest scope of the segment group specificty
            else
            {
                for(String gapJunctionId: gapJunctionList)
                {
                   postCellnC.associateGroupWithSynapse("axon_group", gapJunctionId);
                   postCellnC.associateGroupWithSynapse("soma_group", gapJunctionId);
                   postCellnC.associateGroupWithSynapse("dendrite_group", gapJunctionId);
                }
                postAxonAllowed=true;
                postSomaAllowed=true;
                postDendriteAllowed=true;
            }
        }
        else
        {
            for(String gapJunctionId: gapJunctionList)
            {
              for(String postSegGroup: potentialPostSegGroups)
              {
                postCellnC.associateGroupWithSynapse(postSegGroup, gapJunctionId);
              }
            }
        }
        ////////////////////////////////////////////////////////////////
        System.out.println("Printing presynaptic cell ids for "+projection.getId()+": "+preCellSet);
        
        System.out.println("Printing postsynaptic cell ids for"+projection.getId()+": "+postCellSet);
        
        for (Integer preCellId: preCellSet)
        {
            cellCounter = 0;
            
            for (Integer preCell: preCellList)
            {
              if (preCellId == preCell)
              {
                  cellCounter ++;
              }
            }
            presynapticDict.put(preCellId,cellCounter);
        }
        
        for (Integer postCellId: postCellSet)
        {
            cellCounter = 0;
            
            for (Integer postCell: postCellList)
            {
              if (postCellId == postCell)
              {
                  cellCounter ++;
              }
            }
            postsynapticDict.put(postCellId,cellCounter);
        }
        
        for (Integer CellId : presynapticDict.keySet())
        {
            preCellAppearance.add(presynapticDict.get(CellId));
            
        }
        
        for (Integer CellId : postsynapticDict.keySet())
        {
            postCellAppearance.add(postsynapticDict.get(CellId));
            
        }
        
        
        System.out.println("Presynaptic dictionary for "+projection.getId()+": "+presynapticDict);
        
        System.out.println("Postsynaptic dictionary for "+projection.getId()+": "+postsynapticDict);
        
        System.out.println("Values of presynaptic topology dictionary for "+projection.getId()+": "+preCellAppearance);
        
        System.out.println("Values of postsynaptic topology dictionary for "+projection.getId()+": "+postCellAppearance);
        
        if(postCellAppearance.size() ==1)
        {
            int NumPerPostCell=0;
            
            int NumPerPreCell=0;
            
            if(preCellAppearance.size()==1)
            {
               
               for(Integer value: postCellAppearance)
               {
                   NumPerPostCell= value;
               }
               for(Integer value: preCellAppearance)
               {
                   NumPerPreCell = value;
               }
               
               if (NumPerPreCell>NumPerPostCell)
               {
                  connConds.setGenerationDirection(1);
                  connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPostCell));
                  System.out.println("This is a convergent projection with "+NumPerPostCell+" connections per postsynaptic target cell");
               }
               else if (NumPerPreCell < NumPerPostCell)
               {
                  connConds.setGenerationDirection(0);
                  connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPreCell));
                  System.out.println("This a divergent projection with "+NumPerPreCell+" connections per presynaptic source cell");
               }
               else if ((NumPerPreCell==1) && (NumPerPostCell==1))
               {
                   connConds.setOnlyConnectToUniqueCells(true);
                   
               }
            }
            else
            {
               for(Integer value: postCellAppearance)
               {
                   NumPerPostCell= value;
               }
               connConds.setGenerationDirection(1);
               connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPostCell));
               System.out.println("This is a convergent projection with "+NumPerPostCell+" connections per postsynaptic target cell");

            }
        }
        else
        {
          if(preCellAppearance.size() ==1)
          {
            int NumPerPreCell =0;
            
            for(Integer value: preCellAppearance)
            {
                NumPerPreCell= value;
                
            }
            connConds.setGenerationDirection(0);
            connConds.setNumConnsInitiatingCellGroup(new NumberGenerator(NumPerPreCell));
             System.out.println("This a divergent projection with "+NumPerPreCell+" connections per presynaptic source cell");
          }
          else
          {
              //TODO, for now stays to the default values in neuroConstruct, e.g. SOURCE_TO_TARGET only
          }
          
        }
        lociAllowed.setAxonsAllowedPost(postAxonAllowed);
        lociAllowed.setDendritesAllowedPost(postDendriteAllowed);
        lociAllowed.setSomaAllowedPost(postSomaAllowed);
        lociAllowed.setAxonsAllowedPre(preAxonAllowed);
        lociAllowed.setDendritesAllowedPre(preDendriteAllowed);
        lociAllowed.setSomaAllowedPre(preSomaAllowed);
        
        connConds.setPrePostAllowedLoc(lociAllowed);
        
        connConds.setAllowAutapses(autapsesAllowed);
        
        return connConds;
        
    }
    
    
    public void addNeuroML2Synapse(org.neuroml.model.BaseSynapse NML2Synapse, File synapseFile,NeuroMLDocument NML2Doc)
    {
       String synapseId = NML2Synapse.getId();
        
       if (project.cellMechanismInfo.getAllCellMechanismNames().contains(synapseId))
       {
          logger.logComment("The project "+project.getProjectName() +" already contains a cell mechanism with ID "+ synapseId);
       } 
       else
       {   
            String SynapseNotes;
                            
            if(NML2Doc.getBlockingPlasticSynapse().size() >1)
            {
                SynapseNotes= null;
            }
            else
            {
                SynapseNotes= NML2Synapse.getNotes();
            }
            if (NML2Synapse instanceof org.neuroml.model.GapJunction)
            {
               this.addNeuroML2CellMechanism(synapseId,CellMechanism.NEUROML2_GAP_JUNCTION,synapseFile,SynapseNotes);
            }
            else
            {
               this.addNeuroML2CellMechanism(synapseId,CellMechanism.NEUROML2_SYNAPSE,synapseFile,SynapseNotes);
            }
       }  
    }
    
    public void parse(File nml2File) throws NeuroMLException, IOException
    {
        parse(nml2File, "");
    }
    
    public void parse(File nml2File, String idPrefix) throws NeuroMLException, IOException
    {
        try 
        {
            NeuroMLConverter neuromlConverter=new NeuroMLConverter();

            NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(nml2File.toURI().toURL());

            logger.logComment("Reading in NeuroML2: "+ neuroml.getId(), true);
            
            /// check included cells and cell mechanisms
               
            for (org.neuroml.model.IncludeType includeInstance: neuroml.getInclude())
            {
                    String include_ref= includeInstance.getHref();
                    
                    File includedInNetwork = new File(nml2File.getParentFile(),include_ref);  
                        
                    NeuroMLDocument neuroml2_doc = neuromlConverter.urlToNeuroML(includedInNetwork.toURI().toURL());
                      
                    logger.logComment("Reading in NeuroML2: "+ neuroml2_doc.getId(), true);
                    
                    if (!neuroml2_doc.getIonChannel().isEmpty())
                    {
                      for(org.neuroml.model.IonChannel ionChannel: neuroml2_doc.getIonChannel())
                      {
                        String ionChannelId= ionChannel.getId();
                            
                        if (project.cellMechanismInfo.getAllCellMechanismNames().contains(ionChannelId))
                        {
                          logger.logComment("The project "+project.getProjectName() +" already contains a cell mechanism with ID "+ ionChannelId);
                        } 
                        else
                        {   
                            String ionChannelNotes=null;
                            
                            File IonChannelFile=new File(includedInNetwork.getParentFile(),includedInNetwork.getName());
                                
                            if(neuroml2_doc.getIonChannel().size() >1)
                            {
                                ionChannelNotes= ionChannel.getNotes();
                            }
                            else
                            {
                                ionChannelNotes=neuroml2_doc.getNotes();
                            }
                                
                            this.addNeuroML2CellMechanism(ionChannelId,CellMechanism.NEUROML2_ION_CHANNEL,IonChannelFile,ionChannelNotes);
                        }  
                      } 
                    }
                    if (!neuroml2_doc.getIonChannelKS().isEmpty())
                    {
                      for(org.neuroml.model.IonChannelKS ionChannel: neuroml2_doc.getIonChannelKS())
                      {
                        String ionChannelId= ionChannel.getId();
                            
                        if (project.cellMechanismInfo.getAllCellMechanismNames().contains(ionChannelId))
                        {
                          logger.logComment("The project "+project.getProjectName() +" already contains a cell mechanism with ID "+ ionChannelId);
                        } 
                        else
                        {   
                            String ionChannelNotes=null;
                            
                            File IonChannelFile=new File(includedInNetwork.getParentFile(),includedInNetwork.getName());
                                
                            if(neuroml2_doc.getIonChannelKS().size() >1)
                            {
                                ionChannelNotes= ionChannel.getNotes();
                            }
                            else
                            {
                                ionChannelNotes=neuroml2_doc.getNotes();
                            }
                                
                            this.addNeuroML2CellMechanism(ionChannelId,CellMechanism.NEUROML2_ION_CHANNEL,IonChannelFile,ionChannelNotes);
                        }  
                      } 
                    }
                    /// GapJunction
                    if (!neuroml2_doc.getGapJunction().isEmpty())
                    {
                        for (org.neuroml.model.GapJunction includedGapJunction: neuroml2_doc.getGapJunction())
                       {
                         this.addNeuroML2Synapse(includedGapJunction,includedInNetwork,neuroml2_doc);
                       }
                    }
                    /// ExpTwoSynapse
                    if (!neuroml2_doc.getExpTwoSynapse().isEmpty())
                    {
                       for (org.neuroml.model.ExpTwoSynapse includedSynapse: neuroml2_doc.getExpTwoSynapse())
                       {
                         this.addNeuroML2Synapse(includedSynapse,includedInNetwork,neuroml2_doc);
                       }
                    }
                    ///ExpThreeSynapse
                    if (!neuroml2_doc.getExpThreeSynapse().isEmpty())
                    {
                       for (org.neuroml.model.ExpThreeSynapse includedSynapse: neuroml2_doc.getExpThreeSynapse())
                       {
                         this.addNeuroML2Synapse(includedSynapse,includedInNetwork,neuroml2_doc);
                       }
                    }
                    // ExpOneSynapse
                    if (!neuroml2_doc.getExpOneSynapse().isEmpty())
                    {
                       for (org.neuroml.model.ExpOneSynapse includedSynapse: neuroml2_doc.getExpOneSynapse())
                       {
                         this.addNeuroML2Synapse(includedSynapse,includedInNetwork,neuroml2_doc);
                       }
                    }
                    /// AlphaSynapse
                    if (!neuroml2_doc.getAlphaSynapse().isEmpty())
                    {
                       for (org.neuroml.model.AlphaSynapse includedSynapse: neuroml2_doc.getAlphaSynapse())
                       {
                         this.addNeuroML2Synapse(includedSynapse,includedInNetwork,neuroml2_doc);
                       }
                    }
                    /// BlockingPlasticSynapse
                    if (!neuroml2_doc.getBlockingPlasticSynapse().isEmpty())
                    {
                       for (org.neuroml.model.BlockingPlasticSynapse includedSynapse: neuroml2_doc.getBlockingPlasticSynapse())
                       {
                         this.addNeuroML2Synapse(includedSynapse,includedInNetwork,neuroml2_doc);
                         
                       }
                    }
                    
                    for (org.neuroml.model.IncludeType includeInIncluded: neuroml2_doc.getInclude())
                    {
                        String included_in_cell_ref= includeInIncluded.getHref();
                        
                        File includedFile = new File(includedInNetwork.getParentFile(),included_in_cell_ref);  
                        
                        NeuroMLDocument neuroml2_included_doc = neuromlConverter.urlToNeuroML(includedFile.toURI().toURL());
                        
                        logger.logComment("Reading NeuroML2 file: "+ neuroml2_included_doc.getId(), true);
                        
                        for(org.neuroml.model.IonChannel ionChannel: neuroml2_included_doc.getIonChannel())
                        {
                            String ionChannelId= ionChannel.getId();
                            
                            if (project.cellMechanismInfo.getAllCellMechanismNames().contains(ionChannelId))
                            {
                              logger.logComment("The project "+project.getProjectName() +" already contains a cell mechanism with ID "+ ionChannelId);
                            } 
                            else
                            {   
                                String ionChannelNotes=null;
                                
                                System.out.println("Included channel name: "+includedFile.getName());
                            
                                File IonChannelFile=new File(includedInNetwork.getParentFile(),includedFile.getName());
                                
                                if(neuroml2_included_doc.getIonChannel().size() >1)
                                {
                                   ionChannelNotes= ionChannel.getNotes();
                                }
                                else
                                {
                                   ionChannelNotes=neuroml2_included_doc.getNotes();
                                }
                                
                                this.addNeuroML2CellMechanism(ionChannelId,CellMechanism.NEUROML2_ION_CHANNEL,IonChannelFile,ionChannelNotes);
                            }  
                        }
                        for(org.neuroml.model.IonChannelKS ionChannel: neuroml2_included_doc.getIonChannelKS())
                        {
                            String ionChannelId= ionChannel.getId();
                            
                            if (project.cellMechanismInfo.getAllCellMechanismNames().contains(ionChannelId))
                            {
                              logger.logComment("The project "+project.getProjectName() +" already contains a cell mechanism with ID "+ ionChannelId);
                            } 
                            else
                            {   
                                String ionChannelNotes=null;
                                
                                System.out.println("Included channel name: "+includedFile.getName());
                            
                                File IonChannelFile=new File(includedInNetwork.getParentFile(),includedFile.getName());
                                
                                if(neuroml2_included_doc.getIonChannelKS().size() >1)
                                {
                                   ionChannelNotes= ionChannel.getNotes();
                                }
                                else
                                {
                                   ionChannelNotes=neuroml2_included_doc.getNotes();
                                }
                                
                                this.addNeuroML2CellMechanism(ionChannelId,CellMechanism.NEUROML2_ION_CHANNEL,IonChannelFile,ionChannelNotes);
                            }  
                        }
                    }
                    
                    for (org.neuroml.model.Cell nml2Cell: neuroml2_doc.getCell()) 
                    {
                        this.addNeuroML2Cell(nml2Cell, idPrefix, includedInNetwork);  
                    }
                    
                    for (org.neuroml.model.Cell2CaPools nml2Cell: neuroml2_doc.getCell2CaPools())
                    {
                        this.addNeuroML2Cell2CaPools(nml2Cell,idPrefix,includedInNetwork);
                    }
                    
                    
            }
            
            for (org.neuroml.model.Cell nml2Cell: neuroml.getCell()) 
            {
               this.addNeuroML2Cell(nml2Cell, idPrefix, nml2File);  
            }
            for (org.neuroml.model.Cell2CaPools nml2Cell: neuroml.getCell2CaPools())
            {
               this.addNeuroML2Cell2CaPools(nml2Cell,idPrefix,nml2File);
            }
            
            HashMap<String, PulseGenerator> pulseGenerators = new HashMap<String, PulseGenerator>();
            for (PulseGenerator pg: neuroml.getPulseGenerator()) 
            {
                pulseGenerators.put(pg.getId(), pg);
            }
            
            HashMap<String, PoissonFiringSynapse> poissonFiringSynapses = new HashMap<String, PoissonFiringSynapse>();
            for (PoissonFiringSynapse pfs: neuroml.getPoissonFiringSynapse()) 
            {
                poissonFiringSynapses.put(pfs.getId(), pfs);
            }
            
            HashMap<String, TransientPoissonFiringSynapse> transientPoissonFiringSynapses = new HashMap<String, TransientPoissonFiringSynapse>();
            for (TransientPoissonFiringSynapse pfs: neuroml.getTransientPoissonFiringSynapse()) 
            {
                transientPoissonFiringSynapses.put(pfs.getId(), pfs);
            }
            //// and so on for other input types ....
            
            /// Networks
            
            if (neuroml.getNetwork().size()>1)
            {
                GuiUtils.showErrorMessage(logger, "Currently it is only possible to load a NeuroML file containing a single <network> element.\n"
                        + "There are "+neuroml.getNetwork().size()+" networks in the file: "+nml2File.getAbsolutePath(), null, null);
                
            }
            else if ((neuroml.getNetwork().isEmpty())  && (!neuroml.getCell().isEmpty()) )
            {
               this.foundSimConfig = "Imported NeuroML2 cells";
                
               String simName = getSimConfig();
                
               simConfigToUse = new SimConfig(simName, "");
                   
               project.simConfigInfo.add(simConfigToUse);
                     
               logger.logComment(">>>Using simulation configuration: "+ simConfigToUse);
            }
            else if (neuroml.getNetwork().size()==1) 
            {
                // GapJunction
                if (!neuroml.getGapJunction().isEmpty())
                {
                  throw new NeuroMLException("neuroConstruct can only import gap junctions when they are"
                          + " included in the main NeuroML2 network file as individual component files!");
               
                }
                /// ExpTwoSynapse
                if (!neuroml.getExpTwoSynapse().isEmpty())
                {
                   throw new NeuroMLException("neuroConstruct can only import synapses when they are"
                          + " included in the main NeuroML2 network file as individual component files!");
                }
                ///ExpThreeSynapse
                if (!neuroml.getExpThreeSynapse().isEmpty())
                {
                  throw new NeuroMLException("neuroConstruct can only import synapses when they are"
                          + " included in the main NeuroML2 network file as individual component files!");
                }
                // ExpOneSynapse
                if (!neuroml.getExpOneSynapse().isEmpty())
                {
                   throw new NeuroMLException("neuroConstruct can only import synapses when they are"
                          + " included in the main NeuroML2 network file as individual component files!");
                } 
                /// AlphaSynapse
                if (!neuroml.getAlphaSynapse().isEmpty())
                {
                   throw new NeuroMLException("neuroConstruct can only import synapses when they are"
                          + " included in the main NeuroML2 network file as individual component files!");
                }
                /// BlockingPlasticSynapse
                if (!neuroml.getBlockingPlasticSynapse().isEmpty())
                {
                   throw new NeuroMLException("neuroConstruct can only import synapses when they are"
                          + " included in the main NeuroML2 network file as individual component files!");
                }

                Network network = neuroml.getNetwork().get(0); // Only first network...

                if (network.getType()!=null && network.getType().toString().equals(NetworkMLConstants.NEUROML2_NETWORK_WITH_TEMP_TYPE)) {

                    float tempSI = Utils.getMagnitudeInSI(network.getTemperature());
                    float tempnC = Utils.getMagnitudeInSI(project.simulationParameters.getTemperature()+"degC");

                    if (Math.abs(tempSI-tempnC)>1e-6)
                    {
                        GuiUtils.showWarningMessage(logger, "Note that the imported network file specifies a temperature of "+network.getTemperature()
                                +", but the neuroConstruct project has a temperature setting of "+project.simulationParameters.getTemperature()+" deg C", null);

                    }
                }
                
                this.foundSimConfig = network.getId();
                
                String simName = getSimConfig();
                
                simConfigToUse = new SimConfig(simName, "");
                
                //project.simConfigInfo.add(simConfigToUse);
                     
                //logger.logComment(">>>Using simulation configuration: "+ simConfigToUse);
                
                for (Population population: network.getPopulation())
                {
                    if (!project.cellGroupsInfo.getAllCellGroupNames().contains(population.getId())) 
                    {
                        //throw new NeuroMLException("neuroConstruct can only import populations from networks when a Cell Group with that name already exists!");
                        
                        currentPopulation = population.getId();
                        
                        groupCellType=population.getComponent();
                        
                        logger.logComment("Going to add a group "+currentPopulation+" for the new cell type "+groupCellType);
                        
                        Integer popNumber = population.getSize();
                        
                        try
                        {
                           Random rand= new  Random();
                           Color col = new Color(rand.nextInt(256),
                                                 rand.nextInt(256),
                                                 rand.nextInt(256));

                           project.regionsInfo.addRow(currentPopulation+"_region", region, col);

                           CellPackingAdapter cp = new RandomCellPackingAdapter();
                           try
                           {
                              cp.setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, popNumber);
                              cp.setParameter(RandomCellPackingAdapter.EDGE_POLICY, 0);
                              cp.setParameter(RandomCellPackingAdapter.SELF_OVERLAP_POLICY, 1);
                              cp.setParameter(RandomCellPackingAdapter.OTHER_OVERLAP_POLICY, 1);


                              if (popNumber==1)
                              {
                                 cp =  new SinglePositionedCellPackingAdapter(0,0,0);
                              }

                           }
                           catch (CellPackingException ex)
                           {
                             logger.logError("Error: "+ex.getMessage(), ex);
                           }
                           project.cellGroupsInfo.addRow(currentPopulation, groupCellType, currentPopulation+"_region", col, cp, priority++);
                           simConfigToUse.addCellGroup(currentPopulation);

                           logger.logComment("simConfigToUse "+simConfigToUse.toLongString());

                           project.markProjectAsEdited();
                        }
                        catch (NamingException ex)
                        {
                           GuiUtils.showErrorMessage(logger, "Problem creating a new cell group...", ex, null);

                        } 
                        
                    }
                    for (Instance instance: population.getInstance()) 
                    {
                        Location loc = instance.getLocation();

                        logger.logComment("Adding instance "+instance.getId()+" at: "+ loc+" in "+population.getId());
                        this.cellPos.addPosition(population.getId(), new PositionRecord(instance.getId().intValue(), loc.getX(), loc.getY(), loc.getZ()));
                    }
                }
                for (Projection projection: network.getProjection())
                {
                    String netConn = projection.getId();
                    String source = projection.getPresynapticPopulation();
                    String target = projection.getPostsynapticPopulation();
                    String synapse = projection.getSynapse();
                    
                    for (Population pop: network.getPopulation())
                    {
                        if (pop.getId().equals(source))
                        {
                            preCellType = pop.getComponent();
                            
                            prePopSize = pop.getSize();
                        }
                        if (pop.getId().equals(target))
                        {
                            postCellType= pop.getComponent();
                            
                            postPopSize = pop.getSize();
                        }
                    }
                    
                    for (Connection conn: projection.getConnection())
                    {
                        int preSeg = conn.getPreSegmentId();
                        int postSeg = conn.getPostSegmentId();

                        float preFract = (new Double(conn.getPreFractionAlong())).floatValue();
                        float postFract = (new Double(conn.getPostFractionAlong())).floatValue();

                        this.netConns.addSynapticConnection(netConn, 
                                                            GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                            parseForCellNumber(conn.getPreCellId()), 
                                                            preSeg,
                                                            preFract,
                                                            parseForCellNumber(conn.getPostCellId()),
                                                            postSeg,
                                                            postFract,
                                                            0,
                                                            null);
                    }
                    
                    for (ConnectionWD conn: projection.getConnectionWD())
                    {
                        int preSeg = conn.getPreSegmentId();
                        int postSeg = conn.getPostSegmentId();

                        float preFract = (new Double(conn.getPreFractionAlong())).floatValue();
                        float postFract = (new Double(conn.getPostFractionAlong())).floatValue();
                        
                        String delay= conn.getDelay();
                        
                        Float weight= conn.getWeight();
                            
                        HashMap delay_map;
                           
                        delay_map= getValueAndUnits(delay);
                        
                        ConnSpecificProps csp = new ConnSpecificProps(projection.getSynapse());
                        
                        csp.weight = weight;
                        
                        localConnProps.add(csp);
                       
                        this.netConns.addSynapticConnection(netConn, 
                                                            GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                            parseForCellNumber(conn.getPreCellId()), 
                                                            preSeg,
                                                            preFract,
                                                            parseForCellNumber(conn.getPostCellId()),
                                                            postSeg,
                                                            postFract,
                                                            Float.parseFloat((String)delay_map.get("value")),
                                                            localConnProps);
                    }
                    
                    if (! (project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConn) ||
                           project.volBasedConnsInfo.getAllAAConnNames().contains(netConn)))
                    {
                        SynapticProperties synProp = new SynapticProperties(synapse);
                        Vector<SynapticProperties> synList = new Vector<SynapticProperties>();
                        synList.add(synProp);
                        SearchPattern sp = SearchPattern.getRandomSearchPattern();
                        MaxMinLength mml = new MaxMinLength(100, 0, "r", 100);
                        ConnectivityConditions connConds = new ConnectivityConditions();
                        
                        if (this.inferConnectivityConditions)
                        {
                            // connsConds may be elaborated based on the information that can be extracted from NeuroML2 network.
                            connConds = this.determineConnectivityConditions(projection,prePopSize,postPopSize,preCellType,postCellType);
                        }
                        else
                        {   
                            // minimal assumptions on the segment group specificity 
                            
                            for(String groupName: project.cellManager.getCell(preCellType).getAllGroupNames())
                            {
                               if (groupName.equals("soma_group"))
                               {
                                  project.cellManager.getCell(preCellType).associateGroupWithSynapse("soma_group", synapse);
                               }
                               else if (groupName.equals("axon_group"))
                               {
                                   project.cellManager.getCell(preCellType).associateGroupWithSynapse("axon_group", synapse);
                               }
                            }
                            
                            for(String groupName: project.cellManager.getCell(postCellType).getAllGroupNames())
                            {
                               if (groupName.equals("dendrite_group"))
                               {
                                  project.cellManager.getCell(postCellType).associateGroupWithSynapse("dendrite_group", synapse);
                               }
                               
                               if (groupName.equals("soma_group"))
                               {
                                  project.cellManager.getCell(postCellType).associateGroupWithSynapse("soma_group", synapse);
                               }
                            }
                        }
                        
                        float jumpSpeed = Float.MAX_VALUE;

                        logger.logComment("Going to add a volume based network connection "+netConn+" from group "+source+" to group "+target);
                        
                        try {
                             project.morphNetworkConnectionsInfo.addRow(netConn, source, target, synList, sp, mml, connConds, jumpSpeed);
                        } catch (NamingException ex) {
                          logger.logComment("Problem creating volume based network connection...");
                        }
                        
                    }
                    if (project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConn)) {
                        if (!project.morphNetworkConnectionsInfo.getSourceCellGroup(netConn).equals(source) ||
                            !project.morphNetworkConnectionsInfo.getTargetCellGroup(netConn).equals(target)) {
                            throw new NeuroMLException("Mismatch in the source/target of net conn "+netConn+" between neuroConstruct/NeuroML!");
                        }
                    }
                    if (project.volBasedConnsInfo.getAllAAConnNames().contains(netConn)) {
                        if (!project.volBasedConnsInfo.getSourceCellGroup(netConn).equals(source) ||
                            !project.volBasedConnsInfo.getTargetCellGroup(netConn).equals(target)) {
                            throw new NeuroMLException("Mismatch in the source/target of net conn "+netConn+" between neuroConstruct/NeuroML!");
                        }
                    }
                    
                    simConfigToUse.addNetConn(netConn);
                    
                }
                
                for (ElectricalProjection projection: network.getElectricalProjection())
                {
                    String netConn = projection.getId();
                    String source = projection.getPresynapticPopulation();
                    String target = projection.getPostsynapticPopulation();
                    
                    for (Population population: network.getPopulation())
                    {
                        if (population.getId().equals(source))
                        {
                            prePopSize = population.getSize();
                            
                            preCellType = population.getComponent();
                        }
                        if (population.getId().equals(target))
                        {
                            postPopSize = population.getSize();
                            
                            postCellType = population.getComponent();
                        }
                    }
                    if (! (project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConn) ||
                           project.volBasedConnsInfo.getAllAAConnNames().contains(netConn)))
                    {
                        
                        Vector<SynapticProperties> synList = new Vector<SynapticProperties>();
                        
                        for (ElectricalConnectionInstance elecConn: projection.getElectricalConnectionInstance())
                        {
                            String electSynapse = elecConn.getSynapse();
                            
                            SynapticProperties synProp= new SynapticProperties(electSynapse);
                            
                            synList.add(synProp);
                            
                            if (!this.inferConnectivityConditions)
                            {
                               // minimal assumptions on the segment group specificity 
                            
                               for(String groupName: project.cellManager.getCell(preCellType).getAllGroupNames())
                               {
                                  if (groupName.equals("soma_group"))
                                  {
                                     project.cellManager.getCell(preCellType).associateGroupWithSynapse("soma_group", electSynapse);
                                  }
                                  else if (groupName.equals("axon_group"))
                                  {
                                   project.cellManager.getCell(preCellType).associateGroupWithSynapse("axon_group", electSynapse);
                                  }
                                  else if (groupName.equals("dendrite_group"))
                                  {
                                      project.cellManager.getCell(preCellType).associateGroupWithSynapse("dendrite_group", electSynapse);
                                  }
                               }
                            
                               for(String groupName: project.cellManager.getCell(postCellType).getAllGroupNames())
                               {
                                  if (groupName.equals("dendrite_group"))
                                  {
                                     project.cellManager.getCell(postCellType).associateGroupWithSynapse("dendrite_group", electSynapse);
                                  }
                               
                                  else if (groupName.equals("soma_group"))
                                  {
                                     project.cellManager.getCell(postCellType).associateGroupWithSynapse("soma_group", electSynapse);
                                  }
                                  else if (groupName.equals("axon_group"))
                                  {
                                     project.cellManager.getCell(postCellType).associateGroupWithSynapse("axon_group", electSynapse);
                                  }
                               }
                               
                            }
                            
                        }
                        
                        for (ElectricalConnection elecConn: projection.getElectricalConnection())
                        {
                            String electSynapse = elecConn.getSynapse();
                            
                            SynapticProperties synProp= new SynapticProperties(electSynapse);
                            
                            synList.add(synProp);
                            
                            if (!this.inferConnectivityConditions)
                            {
                               // minimal assumptions on the segment group specificity 
                            
                               for(String groupName: project.cellManager.getCell(preCellType).getAllGroupNames())
                               {
                                  if (groupName.equals("soma_group"))
                                  {
                                     project.cellManager.getCell(preCellType).associateGroupWithSynapse("soma_group", electSynapse);
                                  }
                                  else if (groupName.equals("axon_group"))
                                  {
                                   project.cellManager.getCell(preCellType).associateGroupWithSynapse("axon_group", electSynapse);
                                  }
                                  else if (groupName.equals("dendrite_group"))
                                  {
                                      project.cellManager.getCell(preCellType).associateGroupWithSynapse("dendrite_group", electSynapse);
                                  }
                               }
                            
                               for(String groupName: project.cellManager.getCell(postCellType).getAllGroupNames())
                               {
                                  if (groupName.equals("dendrite_group"))
                                  {
                                     project.cellManager.getCell(postCellType).associateGroupWithSynapse("dendrite_group", electSynapse);
                                  }
                               
                                  else if (groupName.equals("soma_group"))
                                  {
                                     project.cellManager.getCell(postCellType).associateGroupWithSynapse("soma_group", electSynapse);
                                  }
                                  else if (groupName.equals("axon_group"))
                                  {
                                     project.cellManager.getCell(postCellType).associateGroupWithSynapse("axon_group", electSynapse);
                                  }
                               }
                               
                            }
                            
                        }
                        
                        ConnectivityConditions connConds = new ConnectivityConditions();
                        
                        if (this.inferConnectivityConditions)
                        {
                            // connsConds may be elaborated based on the information that can be extracted from NeuroML2 network.
                            connConds = this.determineConnectivityConditions(projection,prePopSize,postPopSize,preCellType,postCellType);
                        }
                        
                        SearchPattern sp = SearchPattern.getRandomSearchPattern();
                        MaxMinLength mml = new MaxMinLength(100, 0, "r", 100);
                        
                        float jumpSpeed = Float.MAX_VALUE;

                        logger.logComment("Going to add a volume based network connection "+netConn+" from group "+source+" to group "+target);
                        
                        try {
                             project.morphNetworkConnectionsInfo.addRow(netConn, source, target, synList, sp, mml, connConds, jumpSpeed);
   
                        } catch (NamingException ex) {
                          logger.logComment("Problem creating volume based network connection...");
                        }
                        
                    }
                    if (project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(netConn)) {
                        if (!project.morphNetworkConnectionsInfo.getSourceCellGroup(netConn).equals(source) ||
                            !project.morphNetworkConnectionsInfo.getTargetCellGroup(netConn).equals(target)) {
                            throw new NeuroMLException("Mismatch in the source/target of net conn "+netConn+" between neuroConstruct/NeuroML!");
                        }
                    }
                    if (project.volBasedConnsInfo.getAllAAConnNames().contains(netConn)) {
                        if (!project.volBasedConnsInfo.getSourceCellGroup(netConn).equals(source) ||
                            !project.volBasedConnsInfo.getTargetCellGroup(netConn).equals(target)) {
                            throw new NeuroMLException("Mismatch in the source/target of net conn "+netConn+" between neuroConstruct/NeuroML!");
                        }
                    }

                    for (ElectricalConnectionInstance conn: projection.getElectricalConnectionInstance())
                    {
                        
                        int preSeg = conn.getPreSegment();
                        int postSeg = conn.getPostSegment();

                        float preFract = (new Double(conn.getPreFractionAlong())).floatValue();
                        float postFract = (new Double(conn.getPostFractionAlong())).floatValue();
                        
                        this.netConns.addSynapticConnection(netConn, 
                                                            GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                            parseForCellNumber(conn.getPreCell()), 
                                                            preSeg,
                                                            preFract,
                                                            parseForCellNumber(conn.getPostCell()),
                                                            postSeg,
                                                            postFract,
                                                            0,
                                                            null);
                    }
                    for (ElectricalConnection conn: projection.getElectricalConnection())
                    {
                        
                        int preSeg = conn.getPreSegment();
                        int postSeg = conn.getPostSegment();

                        float preFract = (new Double(conn.getPreFractionAlong())).floatValue();
                        float postFract = (new Double(conn.getPostFractionAlong())).floatValue();
                        
                        this.netConns.addSynapticConnection(netConn, 
                                                            GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                                            parseForCellNumber(conn.getPreCell()), 
                                                            preSeg,
                                                            preFract,
                                                            parseForCellNumber(conn.getPostCell()),
                                                            postSeg,
                                                            postFract,
                                                            0,
                                                            null);
                    }
                    
                    simConfigToUse.addNetConn(netConn);
                    
                }
                for (InputList inputList: network.getInputList()) 
                {
                    String inputId = inputList.getId();
                    
                    currentInputCellGroup=inputList.getPopulation();
                    
                    if (pulseGenerators.containsKey(inputList.getComponent()))
                    {
                        currentInputType = IClamp.TYPE;
                        
                    }
                    else if (poissonFiringSynapses.containsKey(inputList.getComponent()))
                    {
                        currentInputType = PERSISTENT_POISSON;   
                    }
                    else if (transientPoissonFiringSynapses.containsKey(inputList.getComponent()))
                    {
                        currentInputType = TRANSIENT_POISSON; 
                    }
                    else 
                    {
                       throw new NeuroMLException("Can not determine the type of the electrical input to "+inputId+"!");
                    }
                    
                    boolean annotate_inputs = false;
                    
                    if (!project.elecInputInfo.getAllStimRefs().contains(inputId)) 
                    {
                        //throw new NeuroMLException("neuroConstruct can only import inputLists from NeuroML when an electrical input with that name already exists in the project!");
                        annotate_inputs=true;
                        
                        if (currentInputType.equals(IClamp.TYPE))
                        {
                           PulseGenerator pg = pulseGenerators.get(inputList.getComponent()); 
                           
                           String delay= pg.getDelay();
                           
                           String duration = pg.getDuration();
                           
                           String amplitude = pg.getAmplitude();
                           
                           HashMap delay_map;
                           
                           HashMap duration_map;
                           
                           HashMap amplitude_map;
                           
                           delay_map= getValueAndUnits(delay);
                           
                           duration_map= getValueAndUnits(duration);
                           
                           amplitude_map= getValueAndUnits(amplitude);
                           
                           if (amplitude_map.get("units").equals("pA") && delay_map.get("units").equals("ms") &&
                                  duration_map.get("units").equals("ms") )
                           {
                              float ampInMicroA= Float.parseFloat((String)amplitude_map.get("value"))*PICO_TO_MICRO_SCALING;
                              amplitude_map.put("value", Float.toString(ampInMicroA));
                              amplitude_map.put("units", "uA");
                              
                           }
                           
                           if (amplitude_map.get("units").equals("pA") && delay_map.get("units").equals("s") &&
                                  duration_map.get("units").equals("s") )
                           {
                              float ampInA= Float.parseFloat((String)amplitude_map.get("value"))*PICO_TO_ONE_SCALING;
                              amplitude_map.put("value", Float.toString(ampInA));
                              amplitude_map.put("units", "A");
                              
                           }
                           
                           if(delay_map.get("units").equals("ms") && duration_map.get("units").equals("ms")
                                   && amplitude_map.get("units").equals("uA"))
                           {
                                inputUnitSystem = UnitConverter.getUnitSystemIndex("Physiological Units");
                               
                           }
                           else if (delay_map.get("units").equals("s") && duration_map.get("units").equals("s")
                                   && amplitude_map.get("units").equals("A"))
                           {
                               inputUnitSystem = UnitConverter.getUnitSystemIndex("SI Units");
                           }
                           else
                           {
                              throw new NeuroMLException("neuroConstruct can only import PulseGenertors when all of the parameter values are specified in Physiological Units or SI Units");
                           }
                           
                           Float currentPulseDelay = 
                         (float)UnitConverter.getTime(Float.parseFloat((String)delay_map.get("value")), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                 Float currentPulseDur = 
                         (float)UnitConverter.getTime(Float.parseFloat ((String)duration_map.get("value")), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                 Float currentPulseAmp = 
                         (float)UnitConverter.getCurrent(Float.parseFloat((String)amplitude_map.get("value")), inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                           currentElectricalInput = new IClamp(currentPulseDelay, currentPulseDur, currentPulseAmp, false); 
                           
                           iip = new IClampInstanceProps();

                           iip.setDelay(currentPulseDelay);
                           iip.setDuration(currentPulseDur);
                           iip.setAmplitude(currentPulseAmp);  
                           
                           logger.logComment("New PulseGenerator props: "+" ("+currentPulseDelay+", "+currentPulseDur+", "+currentPulseAmp+")");
                        }
                        if (currentInputType.equals(PERSISTENT_POISSON))
                        {
                            PoissonFiringSynapse pfs = poissonFiringSynapses.get(inputList.getComponent()); 
                           
                            String rate= pfs.getAverageRate();
                            
                            String currentSynapseType=pfs.getSynapse();
                           
                            HashMap rate_map;
                           
                            rate_map= getValueAndUnits(rate);
                            
                            if(((String)rate_map.get("units")).equals("per_ms") )
                           {
                                inputUnitSystem = UnitConverter.getUnitSystemIndex("Physiological Units");
                               
                           }
                           else if (((String)rate_map.get("units")).equals("Hz") || ((String)rate_map.get("units")).equals("per_s"))
                           {
                               inputUnitSystem = UnitConverter.getUnitSystemIndex("SI Units");
                           }
                           else
                           {
                              throw new NeuroMLException("neuroConstruct can only import PoissonFiringSynapse when all of the parameter values are specified in Physiological Units or SI Units");
                           }
                           Float currentRate =
                     (float)UnitConverter.getRate(Float.parseFloat((String)rate_map.get("value")),inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                           
                           currentElectricalInput = new RandomSpikeTrain(new NumberGenerator(currentRate), currentSynapseType);
                             
                           rp = new RandomSpikeTrainInstanceProps();

                           rp.setRate(currentRate);
                           
                           logger.logComment("New RandomSpikeTrain props: "+" ("+currentRate+")"); 
                            
                        }
                        if (currentInputType.equals(TRANSIENT_POISSON))
                        {
                            TransientPoissonFiringSynapse tpfs = transientPoissonFiringSynapses.get(inputList.getComponent()); 
                           
                            String rate= tpfs.getAverageRate();
                            
                            String duration =tpfs.getDuration();
                            
                            String delay= tpfs.getDelay();
                            
                            String currentSynapseType=tpfs.getSynapse();
                           
                            HashMap rate_map;
                    
                            HashMap delay_map;
                            
                            HashMap duration_map;
                           
                            rate_map= getValueAndUnits(rate);
                            
                            delay_map= getValueAndUnits(delay);
                            
                            duration_map=getValueAndUnits(duration);
                            
                            if ( (rate_map.get("units").equals("per_s")||rate_map.get("units").equals("Hz")) 
                                 && delay_map.get("units").equals("ms") && duration_map.get("units").equals("ms") )
                           {
                              float rateInPerMS= Float.parseFloat((String)rate_map.get("value"))*PER_S_TO_PER_MS_SCALING;
                              rate_map.put("value", Float.toString(rateInPerMS));
                              rate_map.put("units", "per_ms");
                              
                           }
                            
                            if( rate_map.get("units").equals("per_ms") && duration_map.get("units").equals("ms") 
                                    && delay_map.get("units").equals("ms") )
                            {
                                inputUnitSystem = UnitConverter.getUnitSystemIndex("Physiological Units");
                               
                            }
                            else if ( ( rate_map.get("units").equals("Hz") || rate_map.get("units").equals("per_s") )
                                    &&( duration_map.get("units").equals("s") && delay_map.get("units").equals("s") ) )
                            {
                               inputUnitSystem = UnitConverter.getUnitSystemIndex("SI Units");
                            }
                            //else if 
                            //{
                                       //TODO 
                            //}
                            else
                            {
                              throw new NeuroMLException("neuroConstruct can only import TransientPoissonFiringSynapse when all of the parameter values are specified in Physiological Units or SI Units");
                            }
                            Float currentRate =
                     (float)UnitConverter.getRate(Float.parseFloat((String)rate_map.get("value")),inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                            
                            Float currentDuration =
                     (float)UnitConverter.getRate(Float.parseFloat((String)duration_map.get("value")),inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                            
                            Float currentDelay =
                     (float)UnitConverter.getRate(Float.parseFloat((String)duration_map.get("value")),inputUnitSystem, UnitConverter.NEUROCONSTRUCT_UNITS);
                            
                            currentElectricalInput = new RandomSpikeTrainExt(new NumberGenerator(currentRate), 
                                    currentSynapseType,new NumberGenerator(currentDelay),new NumberGenerator(currentDuration),false);
                             
                            trp = new RandomSpikeTrainExtInstanceProps();

                            trp.setRate(currentRate);
                            
                            trp.setDuration(currentDuration);
                            
                            trp.setDelay(currentDelay);
                            
                            logger.logComment("New RandomSpikeTrain props: "+" ("+currentRate+")"+" ("+currentDuration+")"+" ("+currentDelay+")"); 
                            
                        }
                    }
                    
                    for (Input input: inputList.getInput()) 
                    {
                        int segmentId = input.getSegmentId()!=null ? input.getSegmentId() : 0;

                        float fractAlong = input.getFractionAlong()!=null ? input.getFractionAlong().floatValue() : 0.5f;
                        
                        if (annotate_inputs)
                        {
                            StimulationSettings stim = null;
                            
                            String cell_id =Integer.toString(parseForCellNumber(input.getTarget()));
                            
                            CellChooser cellChoose = new IndividualCells(cell_id);
                            
                            ArrayList<Integer> segs = new ArrayList<Integer>();
                            
                            if(input.getSegmentId() ==null)
                            {
                             segs.add(0);
                            }
                            else
                            {
                                segs.add(input.getSegmentId());
                            }

                            SegmentLocationChooser segChoose = new IndividualSegments(segs);
                            
                            if (currentInputType.equals(IClamp.TYPE))
                            {
                              IClamp iClamp = (IClamp)currentElectricalInput;
                              stim = new IClampSettings(inputId, currentInputCellGroup, cellChoose, segChoose, iClamp.getDel(), iClamp.getDur(), iClamp.getAmp(), false);
                              project.elecInputInfo.addStim(stim);
                              simConfigToUse.addInput(stim.getReference());
                              
                              project.markProjectAsEdited();
                              
                            }
                            if (currentInputType.equals(PERSISTENT_POISSON))
                            {
                              RandomSpikeTrain rst = (RandomSpikeTrain)currentElectricalInput;
                              stim = new RandomSpikeTrainSettings(inputId, currentInputCellGroup, cellChoose, segChoose,  rst.getRate(), rst.getSynapseType());
                              project.elecInputInfo.addStim(stim);
                              simConfigToUse.addInput(stim.getReference());
                              
                              project.markProjectAsEdited();
                              
                            }
                            
                            if (currentInputType.equals(TRANSIENT_POISSON))
                            {
                              RandomSpikeTrainExt rst = (RandomSpikeTrainExt)currentElectricalInput;
                              stim = new RandomSpikeTrainExtSettings(inputId, currentInputCellGroup, cellChoose, segChoose,  rst.getRate(), rst.getSynapseType(),
                              rst.getDelay(),rst.getDuration(),false);
                              project.elecInputInfo.addStim(stim);
                              simConfigToUse.addInput(stim.getReference());
                              
                              project.markProjectAsEdited();
                              
                            }
                            logger.logComment(currentInputType+" electrical input "+inputId+" on the cell group "+ currentInputCellGroup+" added to the project.");
                            
                        }
                        currentSingleInput = new SingleElectricalInput(currentInputType, currentInputCellGroup,parseForCellNumber(input.getTarget()) , segmentId, fractAlong, null);
                        logger.logComment("New instance: "+ currentSingleInput);

                        if (currentInputType.equals(IClamp.TYPE))
                        {   
                           currentSingleInput.setInstanceProps(iip);
                        }
                        
                        if (currentInputType.equals(PERSISTENT_POISSON))
                        {   
                           currentSingleInput.setInstanceProps(rp);
                        }
                        if (currentInputType.equals(TRANSIENT_POISSON))
                        {
                           currentSingleInput.setInstanceProps(trp);
                        }
                        elecInputs.addSingleInput(inputId, currentSingleInput);
                        
                    }
                }
                
                project.simConfigInfo.add(simConfigToUse);
                
                logger.logComment(">>>Using simulation configuration: "+ simConfigToUse);
            }
        }
        catch (MalformedURLException e) 
        {
            throw new NeuroMLException("Problem parsing NeuroML2 file: "+nml2File, e);
        } 
        catch (org.neuroml.model.util.NeuroMLException e) 
        {
            throw new NeuroMLException("Problem parsing NeuroML2 file: "+nml2File, e);
        }
        
        
    }
    
    private int parseForCellNumber(String cellIdString) 
    {
        //System.out.println("cellIdString: "+cellIdString);
        int lastSlash = cellIdString.lastIndexOf("/");
        int secondLastSlash = cellIdString.substring(0, lastSlash).lastIndexOf("/");
        return Integer.parseInt(cellIdString.substring(secondLastSlash+1, lastSlash));
    }
    
    


    public static void main(String args[])
    {

        try
        {
            //Project testProj = Project.loadProject(new File("testProjects/TestNetworkML/TestNetworkML.neuro.xml"),null);
            Project testProj = Project.loadProject(new File("osb/invertebrate/celegans/CElegansNeuroML/CElegans/CElegans.ncx"),null);
            testProj = Project.loadProject(new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/GranuleCell.ncx"),null);
            testProj = Project.loadProject(new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/ACnet2.ncx"),null);

            File f = new File("testProjects/TestNetworkML/savedNetworks/test_nml2.xml");
            f = new File("testProjects/TestNetworkML/savedNetworks/nnn.nml");
            
            boolean network = true;
            if (network) 
            {
                f = new File("osb/invertebrate/celegans/CElegansNeuroML/CElegans/pythonScripts/CElegansConnectome.nml");
                f = new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/ACnet2.net.nml");

                logger.logComment("Loading nml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);

                ProjectManager pm = new ProjectManager(null, null);

                //pm.setCurrentProject(testProj);

                if (!f.exists())
                {
                  System.out.println("Error! File not found: "+f.getAbsolutePath());
                  System.exit(1);
                }
                
                if (pm.getCurrentProject()==null)
                {  
                   String projectName = f.getName().indexOf(".")>1 ? f.getName().substring(0, f.getName().indexOf(".")) : f.getName();
                   System.out.println("Will make a new project: "+projectName);
                   Project testNeuroML2Proj;
                   
                   File projDir = new File(ProjectStructure.getDefaultnCProjectsDir().getPath(), projectName);
                   
                   System.out.println("Project in: "+projDir.getAbsolutePath());
                           
                   testNeuroML2Proj=Project.createNewProject(projDir.getAbsolutePath(),
                                           projectName,
                                           Project.getDummyProjectEventListener());
                   
                   pm.setCurrentProject(testNeuroML2Proj);
                   
                   pm.getCurrentProject().saveProject();
                   
                }
                
                if (pm.getCurrentProject() != null)
                {
                   System.out.println("Test project is set in "+System.getProperty("user.home")+"/nC_projects/");
                   pm.doLoadNeuroML2Network(f, false);
                }

                while (pm.isGenerating())
                {
                    Thread.sleep(2);
                    System.out.println("Waiting...");
                }
                
                System.out.println(pm.getCurrentProject().generatedCellPositions.details());
                System.out.println(pm.getCurrentProject().generatedNetworkConnections.details());
                
            }
            else 
            {
                f = new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/generatedNeuroML2/Granule_98.cell.nml");
                f = new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/generatedNeuroML2/bask.cell.nml");
                
                logger.logComment("Loading nml cell from "+ f.getAbsolutePath()+" for proj: "+ testProj);


                NeuroML2Reader nml2Reader = new NeuroML2Reader(testProj,false);

                nml2Reader.parse(f, "New_");
            }
            

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
