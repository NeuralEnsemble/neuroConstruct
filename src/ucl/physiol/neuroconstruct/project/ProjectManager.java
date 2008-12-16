/**
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

package ucl.physiol.neuroconstruct.project;

import java.io.*;
import java.util.*;

import java.awt.*;

import java.util.ArrayList;
import java.util.zip.*;
import org.xml.sax.*;

import javax.xml.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.converters.MorphMLConverter;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.neuroml.hdf5.*;
import ucl.physiol.neuroconstruct.neuron.NeuronException;
import ucl.physiol.neuroconstruct.project.segmentchoice.*;
import ucl.physiol.neuroconstruct.project.stimulation.ElectricalInput;
import ucl.physiol.neuroconstruct.project.stimulation.IClamp;
import ucl.physiol.neuroconstruct.project.stimulation.RandomSpikeTrain;
import ucl.physiol.neuroconstruct.utils.SequenceGenerator.EndOfSequenceException;

/**
 * A class for handling interaction with the project
 * All non gui functionality will (eventually) be transferred here,
 * to reduce the size of MainFrame, and to give access to the core functionality
 * via the command line interface
 *
 * @author Padraig Gleeson
 *  
 */

public class ProjectManager implements GenerationReport
{
    private static ClassLogger logger = new ClassLogger("ProjectManager");

    private Project activeProject = null;

    public CellPositionGenerator cellPosnGenerator = null;
    public MorphBasedConnGenerator netConnGenerator = null;

    public VolumeBasedConnGenerator arbourConnectionGenerator = null;
    
    public CompNodeGenerator compNodeGenerator = null;
    public ElecInputGenerator elecInputGenerator = null;
    public PlotSaveGenerator plotSaveGenerator = null;

    private GenerationReport reportInterface = null;
    private ProjectEventListener projEventListener = null;

    private static Random randomGenerator = new Random();
    private static long currentSeed = 1;
    
    private boolean currentlyGenerating = false;


    // Only for Python based startup
    public ProjectManager()
    {
        reportInterface = this;
    }    

    public ProjectManager(GenerationReport ri,
                          ProjectEventListener projEventListener)
    {
        if(ri!=null)
        {
            this.reportInterface = ri;
        }
        else
        {
            reportInterface = this;
        }
        this.projEventListener = projEventListener;
    }
    
    /*
     * Print out status of application including details of currently loaded project
     */
    public String status()
    {
        StringBuffer info = new StringBuffer();
        
        info.append("\n  neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n\n");
            
        if (activeProject==null)
        {
            info.append("No project loaded"+"\n");
        }
        else
        {
            try 
            {
                info.append("Project:             " + activeProject.getProjectName() + "\n");
                info.append("Project file:        " + activeProject.getProjectFullFileName() + "\n");
                info.append("No. Cell Types:      " + activeProject.cellManager.getNumberCellTypes() + "\n");
                info.append("No. Cell Groups:     " + activeProject.cellGroupsInfo.getNumberCellGroups() + "\n");
                info.append("No. Morph Net Conns: " + activeProject.morphNetworkConnectionsInfo.getNumSimpleNetConns() + "\n");
                info.append("No. Vol Net Conns:   " + activeProject.volBasedConnsInfo.getNumConns() + "\n");
                info.append("No. Inputs:          " + activeProject.elecInputInfo.getAllStims().size() + "\n");
            } 
            catch (NoProjectLoadedException ex) 
            {
                info.append("No project loaded!"+"/n");
            }
        }
        
        info.append("\n");
        return info.toString();
    }

    public Project getCurrentProject()
    {
        return activeProject;
    }

    public void setCurrentProject(Project project)
    {
        activeProject = project;

        PlotManager.setCurrentProject(project);
    }

    public boolean doRunNeuron(SimConfig simConfig)
    {
                
        File genNeuronDir = ProjectStructure.getNeuronCodeDir(activeProject.getProjectMainDirectory());


        /**
         * Will be the only sim dir if a single run, will be the dir for the actually run 
         * neuron code when multiple sims are run
         */
        String primarySimDirName = activeProject.simulationParameters.getReference();


        File positionsFile = new File(genNeuronDir, SimulationData.POSITION_DATA_FILE);
        File netConnsFile = new File(genNeuronDir, SimulationData.NETCONN_DATA_FILE);
        File elecInputFile = new File(genNeuronDir, SimulationData.ELEC_INPUT_DATA_FILE);

     
        try
        {
            activeProject.generatedCellPositions.saveToFile(positionsFile);
            activeProject.generatedNetworkConnections.saveToFile(netConnsFile);
            activeProject.generatedElecInputs.saveToFile(elecInputFile);
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving generated positions in file: "+ positionsFile.getAbsolutePath(), ex, null);
            return false;
        }

        // Saving summary of the simulation params
        try
        {
            SimulationsInfo.recordSimulationSummary(activeProject, simConfig, genNeuronDir, "NEURON", null);
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "+ genNeuronDir +
                                      "\nThere will be less info on this simulation in the previous simulation browser dialog", ex2, null);
        }


        File[] generatedNeuronFiles = genNeuronDir.listFiles();


        ArrayList<String> simDirsToCreate = activeProject.neuronFileManager.getGeneratedSimReferences();

        simDirsToCreate.add(primarySimDirName);

        for (String simRef : simDirsToCreate)
        {
            File dirForSimFiles = ProjectStructure.getDirForSimFiles(simRef, activeProject);

            if (dirForSimFiles.exists())
            {
                SimpleFileFilter sff = new SimpleFileFilter(new String[]
                                                            {".dat"}, null);
                File[] files = dirForSimFiles.listFiles(sff);
                for (int i = 0; i < files.length; i++)
                {
                    files[i].delete();
                }
                logger.logComment("Directory " + dirForSimFiles + " being cleansed");
            }
            else
            {
                GuiUtils.showErrorMessage(logger, "Directory " + dirForSimFiles + " doesn't exist...", null, null);
                return false;
            }

            for (int i = 0; i < generatedNeuronFiles.length; i++)
            {
                String fn = generatedNeuronFiles[i].getName();

                if (fn.endsWith(".dat")||
                    fn.endsWith(".props") ||
                        fn.endsWith(".py")||
                        fn.endsWith(".xml") ||
                    (activeProject.neuronSettings.isCopySimFiles() &&
                        (fn.endsWith(".hoc") ||
                        fn.endsWith(".mod") ||
                        fn.endsWith(".dll"))))
                {
                    try
                    {
                        //System.out.println("Saving a copy of file: " + generatedNeuronFiles[i]
                        //                  + " to dir: " +
                        //                  dirForSimFiles);

                        GeneralUtils.copyFileIntoDir(generatedNeuronFiles[i],
                                                     dirForSimFiles);
                    }
                    catch (IOException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Error copying file: " + ex.getMessage(), ex, null);
                        return false;
                    }
                }
                else if (activeProject.neuronSettings.isCopySimFiles() &&
                         (generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_I686) || 
                         generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_64BIT)))
                {
                    File toDir = new File(dirForSimFiles, generatedNeuronFiles[i].getName());
                    toDir.mkdir();
                    logger.logComment("Saving the linux libs from the compiled mods of file: " +
                                      generatedNeuronFiles[i] + " to dir: " + toDir);

                    try
                    {
                        GeneralUtils.copyDirIntoDir(generatedNeuronFiles[i], toDir, true, true);
                    }
                    catch (IOException ex1)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Error while saving the linux libs from the compiled mods from  of file: " +
                                                  generatedNeuronFiles[i]
                                                  + " to dir: " + dirForSimFiles, ex1, null);

                        return false;
                    }
                }
                else if (activeProject.neuronSettings.isCopySimFiles() && 
                         generatedNeuronFiles[i].isDirectory() && 
                         (generatedNeuronFiles[i].getName().equals(ProjectStructure.neuroMLPyUtilsDir) ||
                          generatedNeuronFiles[i].getName().equals(ProjectStructure.neuronPyUtilsDir)))
                {
                    File toDir = new File(dirForSimFiles, generatedNeuronFiles[i].getName());
                    toDir.mkdir();

                    try
                    {
                        GeneralUtils.copyDirIntoDir(generatedNeuronFiles[i], toDir, true, true);
                    }
                    catch (IOException ex1)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Error while copying file: " +
                                                  generatedNeuronFiles[i]
                                                  + " to dir: " + dirForSimFiles, ex1, null);

                        return false;
                    }
                }

            }

            if (GeneralProperties.getGenerateMatlab())
            {
                MatlabOctave.createSimulationLoader(activeProject, simConfig, simRef);
            }

            if ((GeneralUtils.isWindowsBasedPlatform() || GeneralUtils.isMacBasedPlatform()) 
                && GeneralProperties.getGenerateIgor())
            {
                IgorNeuroMatic.createSimulationLoader(activeProject, simConfig, simRef);
            }
        }

        File simRunDir = ProjectStructure.getDirForSimFiles(activeProject.simulationParameters.getReference(),
                                                                activeProject);
        
        if (!activeProject.neuronSettings.isCopySimFiles())
        {
            simRunDir = new File(genNeuronDir.getAbsolutePath());
        }

        try
        {
            File newMainHocFile = new File(simRunDir, activeProject.neuronFileManager.getMainHocFile().getName());


            logger.logComment("Going to run file: "+ newMainHocFile);

            activeProject.neuronFileManager.runNeuronFile(newMainHocFile);
        }
        catch (NeuronException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, null);
            return false;
        }
        return true;
    }


    //public

    public void doAnalyseLengths(String selectedNetConn)
    {
        if (activeProject == null)
        {
            logger.logError("No project loaded...");
            return;
        }


        ArrayList<SingleSynapticConnection> netConns = activeProject.generatedNetworkConnections.getSynapticConnections(selectedNetConn);

        String plotRef = null;

        if (activeProject.morphNetworkConnectionsInfo.isValidSimpleNetConn(selectedNetConn))
        {
            plotRef = activeProject.morphNetworkConnectionsInfo.getSourceCellGroup(selectedNetConn)+ " to "
                + activeProject.morphNetworkConnectionsInfo.getTargetCellGroup(selectedNetConn) +
                " conn lengths";
        }
        else if (activeProject.volBasedConnsInfo.isValidVolBasedConn(selectedNetConn))
        {
            plotRef = activeProject.volBasedConnsInfo.getSourceCellGroup(selectedNetConn)+ " to "
                + activeProject.volBasedConnsInfo.getTargetCellGroup(selectedNetConn) +
                " conn lengths";
        }



        String desc = "Lengths of " + netConns.size() + " conns in: " +
                                          selectedNetConn;

        PlotterFrame frame = PlotManager.getPlotterFrame(desc);

        DataSet data = new DataSet(plotRef, desc, "", "\u03bcm", "Cell number", "Length");

        for (int i = 0; i < netConns.size(); i++)
        {
            SingleSynapticConnection oneConn =  netConns.get(i);

            float length = 0;

            if (activeProject.morphNetworkConnectionsInfo.isValidSimpleNetConn(selectedNetConn))
            {
                length = CellTopologyHelper.getSynapticEndpointsDistance(
                    activeProject,
                    activeProject.morphNetworkConnectionsInfo.getSourceCellGroup(selectedNetConn),
                    oneConn.sourceEndPoint,
                    activeProject.morphNetworkConnectionsInfo.getTargetCellGroup(selectedNetConn),
                    oneConn.targetEndPoint, MaxMinLength.RADIAL);

            }

            else if (activeProject.volBasedConnsInfo.isValidVolBasedConn(selectedNetConn))
            {
                length = CellTopologyHelper.getSynapticEndpointsDistance(
                    activeProject,
                    activeProject.volBasedConnsInfo.getSourceCellGroup(selectedNetConn),
                    oneConn.sourceEndPoint,
                    activeProject.volBasedConnsInfo.getTargetCellGroup(selectedNetConn),
                    oneConn.targetEndPoint, MaxMinLength.RADIAL);

            }


            data.addPoint(i, length);

        }
        frame.addDataSet(data);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        frame.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);


        frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);

        frame.setVisible(true);
    }





    /*
     * Carries out all the actions needed when an active project is closed
     */
    public void doCloseProject()
    {

        logger.logComment(">>>>>>>>>>>>>>>    Closing down the project...");
        if (activeProject == null)
        {
            logger.logComment("No project loaded to close...");
            return;
        }
        activeProject = null;
        cellPosnGenerator = null;
        netConnGenerator = null;

        arbourConnectionGenerator = null;
        compNodeGenerator = null;
        elecInputGenerator = null;
        plotSaveGenerator = null;

        System.gc();
        System.gc();


        PlotManager.setCurrentProject(null);

        logger.logComment(">>>>>>>>>>>>>>>    Project closed");

    }

    public boolean projectLoaded()
    {
        if (activeProject!=null) return true;
        return false;
    }
    
    public boolean doLoadNetworkMLAndGenerate(File networkmlFile) throws NeuroMLException, Hdf5Exception, EndOfSequenceException
    {
        NetworkMLnCInfo extraInfo = doLoadNetworkML(networkmlFile);
                
        String prevSimConfig = extraInfo.getSimConfig();
        long randomSeed = extraInfo.getRandomSeed();
        
        setRandomGeneratorSeed(randomSeed);
        
        elecInputGenerator = new ElecInputGenerator(getCurrentProject(), this);

        elecInputGenerator.setSimConfig(getCurrentProject().simConfigInfo.getSimConfig(prevSimConfig));
        
        
        currentlyGenerating = true;

        elecInputGenerator.start();



        return false;
    }
    
    
    
    public NetworkMLnCInfo doLoadNetworkML(File networkmlFile) throws NeuroMLException, Hdf5Exception, EndOfSequenceException
    {
        if (networkmlFile.getName().endsWith(ProjectStructure.getHDF5FileExtension()))
        {
            try 
            {
                ucl.physiol.neuroconstruct.neuroml.hdf5.NetworkMLReader nmlReader 
                        = new ucl.physiol.neuroconstruct.neuroml.hdf5.NetworkMLReader(getCurrentProject());

                nmlReader.parse(networkmlFile);
                
                return nmlReader;
            } 
            catch (Hdf5Exception ex) 
            {
                throw new NeuroMLException("Problem parsing HDF5 NetworkML file", ex);
            }
        }
        else
        {
            InputSource is = null;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = null;

            try
            {
                xmlReader = spf.newSAXParser().getXMLReader();

                ucl.physiol.neuroconstruct.neuroml.NetworkMLReader nmlBuilder
                    = new ucl.physiol.neuroconstruct.neuroml.NetworkMLReader(getCurrentProject());

                xmlReader.setContentHandler(nmlBuilder);

                if (networkmlFile.getName().endsWith(ProjectStructure.getNeuroMLCompressedFileExtension()))
                {
                    FileInputStream instream = new FileInputStream(networkmlFile);

                    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(instream));

                    ZipEntry entry = zis.getNextEntry();

                    logger.logComment("Reading contents of zip: " + entry);

                    is = new InputSource(zis);
                }
                else
                {
                   FileInputStream instream = new FileInputStream(networkmlFile);
                   is = new InputSource(instream);
                }

                xmlReader.parse(is);
            
                return nmlBuilder;
            }
            catch (Exception e)
            {
                throw new NeuroMLException("Problem parsing XML based NetworkML file: \n\n"+e.getMessage()+"\n", e);
            }
            
        }
        
    }

  
    public static File saveCompleteNetworkXML(Project project,
                                        File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments,
                                       String simConfig,
                                       String networkUnits) throws NeuroMLException
                                       
    {
      
        int preferredUnits = UnitConverter.getUnitSystemIndex(networkUnits);
        
        try
        {
                    
            StringBuffer notes = new StringBuffer("\nComplete network structure generated with project: "
                                +project.getProjectName() + " saved with neuroConstruct v"+
                                GeneralProperties.getVersionNumber()+" on: "+ GeneralUtils.getCurrentTimeAsNiceString() +", "
                                + GeneralUtils.getCurrentDateAsNiceString()+"\n\n");
            
            
    
            
            Iterator<String> cellGroups = project.generatedCellPositions.getNamesGeneratedCellGroups();
            ArrayList<String> cellGroupsNames = new ArrayList<String>();
            
            while (cellGroups.hasNext())
            {
                String cg = cellGroups.next();
                cellGroupsNames.add(cg);
                int numHere = project.generatedCellPositions.getNumberInCellGroup(cg);
                if (numHere>0)
                notes.append("Cell Group: "+cg+" contains "+numHere+" cells\n");
                
            }
            notes.append("\n");
            
            
            Iterator<String> netConns = project.generatedNetworkConnections.getNamesNetConnsIter();
            
            while (netConns.hasNext())
            {
            String mc = netConns.next();
            int numHere = project.generatedNetworkConnections.getSynapticConnections(mc).size();
            if (numHere>0)
            notes.append("Network connection: "+mc+" contains "+numHere+" individual synaptic connections\n");
            
            }
            notes.append("\n");
            
            
            logger.logComment("Going to save complete network in NeuroML format in " + neuroMLFile.getAbsolutePath());

            SimpleXMLDocument doc = new SimpleXMLDocument();

            SimpleXMLElement rootElement = null;

            rootElement = new SimpleXMLElement(NeuroMLConstants.ROOT_ELEMENT);

            rootElement.addNamespace(new SimpleXMLNamespace("", NeuroMLConstants.NAMESPACE_URI));

            rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                            MetadataConstants.NAMESPACE_URI));

            rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                            NeuroMLConstants.XSI_URI));
            
            rootElement.addNamespace(new SimpleXMLNamespace(MorphMLConstants.PREFIX,
                                                             MorphMLConstants.NAMESPACE_URI));
             
            rootElement.addNamespace(new SimpleXMLNamespace(ChannelMLConstants.PREFIX,
                                                             ChannelMLConstants.NAMESPACE_URI));
            
            rootElement.addNamespace(new SimpleXMLNamespace(BiophysicsConstants.PREFIX,
                                                             BiophysicsConstants.NAMESPACE_URI));
            
            rootElement.addNamespace(new SimpleXMLNamespace(NetworkMLConstants.PREFIX,
                                                             NetworkMLConstants.NAMESPACE_URI));

            rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                            NeuroMLConstants.NAMESPACE_URI+"../../Schemata/v1.7.3/Level3/NeuroML_Level3_v1.7.3.xsd"));// + "NeuroMLConstants.NAMESPACE_URI + "  " + NeuroMLConstants.DEFAULT_SCHEMA_FILENAME));
            

            rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, "micron"));

            doc.addRootElement(rootElement);

            logger.logComment("    ****    Full XML:  ****");
            logger.logComment("  ");

            rootElement.addContent("\n\n");

            rootElement.addChildElement(new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                             MetadataConstants.NOTES_ELEMENT, "\n" + notes.toString()));

            SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                          MorphMLConstants.PROPS_ELEMENT);

            rootElement.addContent("\n\n");

            rootElement.addChildElement(props);

            MetadataConstants.addProperty(props,
                                          NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED,
                                          project.generatedCellPositions.getRandomSeed() + "",
                                          "    ");

            if (simConfig!=null)
            {
                MetadataConstants.addProperty(props,
                                              NetworkMLConstants.NC_SIM_CONFIG,
                                              simConfig,
                                              "    ");
            }

            
            
    //The cell types present in the network
         
            rootElement.addContent("\n\n");
            
            SimpleXMLElement cellsElement = new SimpleXMLElement("cells");
            
            int i = -1;
            ArrayList<String> ct = new ArrayList<String>();
            for (int j = 0; j < cellGroupsNames.size(); j++) 
            {
                MorphMLConverter mmlC = new MorphMLConverter();
                String cg = cellGroupsNames.get(j);
                if (!ct.contains(project.cellGroupsInfo.getCellType(cg)))
                {
                    i++;
                    ct.add(project.cellGroupsInfo.getCellType(cg));
                    Cell cell = project.cellManager.getCell(ct.get(i));
                    SimpleXMLElement element = mmlC.getCellXMLElement(cell, project, NeuroMLConstants.NEUROML_LEVEL_3);
                    cellsElement.addChildElement(element);                    
                }
                
                      
            }
            
            rootElement.addChildElement(cellsElement);

            rootElement.addContent("\n\n");
            
    //The biophysical mechanisms present in the network
            
            SimpleXMLElement cmechsElement = new SimpleXMLElement("channels");
            cmechsElement.addAttribute(new SimpleXMLAttribute(ChannelMLConstants.UNIT_SCHEME, ChannelMLConstants.PHYSIOLOGICAL_UNITS));
            String cmlPrefix = ChannelMLConstants.PREFIX + ":";
            

            
            boolean addChan = false; //flag to avoid repetitions and unuseful checks
            Vector<String> allm = project.cellMechanismInfo.getAllCellMechanismNames();
            Vector<String> cellMechs = new Vector<String>();            
            
            //loop over all the existing cell mechanisms
            //to find all the cell mechanisms in the generated cell groups
            for (int j = 0; j < allm.size(); j++)
            {                
                String m = allm.get(j);
                addChan = false;
                
                //add the synapses that are used in the generated connections
                Iterator connsNames = project.generatedNetworkConnections.getNamesNetConnsIter();                
                while (connsNames.hasNext()&&(addChan==false))
                {
                    if (project.morphNetworkConnectionsInfo.getSynapseList((String)connsNames.next()).contains(m)
                            && !cellMechs.contains(m))
                    {
                        cellMechs.add(m);
                        addChan = true;
                    }                    
                }
                                
                //add the synapses that are used in the the stimulations
                Iterator inputsNames = project.generatedElecInputs.getElecInputsItr();
                while (inputsNames.hasNext()&&(addChan==false))
                {
                    ElectricalInput ei  = project.elecInputInfo.getStim((String)inputsNames.next()).getElectricalInput();
                    if (ei.getType().equals("RandomSpikeTrainExt") || ei.getType().equals("RandomSpikeTrain"))
                    {
                        RandomSpikeTrain rst = (RandomSpikeTrain)ei;
                        if (rst.getSynapseType().equals(m)
                                && !cellMechs.contains(m))
                        {                            
                            cellMechs.add(m);
                            addChan = true;
                        }                        
                    }
                }
                
                //add channels that are used in the generated cell groups
                i=0;
                while ((i<cellGroupsNames.size())&&(addChan==false))
                {  
                    if (project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroupsNames.get(i))).getAllChanMechNames(true).contains(m)
                            && !cellMechs.contains(m))
                    {
                        cellMechs.add(m);
                        addChan = true;
                    }
                    i++;
                 }
            }
            
            //System.out.println("cellMechs "+cellMechs.toString());
            
            //add the channelML descriptions for all the cell mechanisms selected
            ArrayList<SimpleXMLElement> channels = new ArrayList<SimpleXMLElement>();
            ArrayList<SimpleXMLElement> synapses = new ArrayList<SimpleXMLElement>();
            
            for(String cellMech: cellMechs)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cellMech);

                if ((cm instanceof ChannelMLCellMechanism))
                {
                    ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;
                    SimpleXMLEntity[] elements = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getChannelTypeXPath());
                    
                    if (elements.length>=0)
                    {
                        for (int j = 0; j < elements.length; j++)
                        {
                            SimpleXMLElement el = (SimpleXMLElement)elements[j];
                            if(!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                                el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                            channels.add(el);
                        }    
                    }
                    
                    elements = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getSynapseTypeXPath());
                    
                    if (elements.length>=0)
                    {
                        for (int j = 0; j < elements.length; j++)
                        {
                            SimpleXMLElement el = (SimpleXMLElement)elements[j];   
                            if(!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                                el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                            synapses.add(el);
                        }    
                    }
                                       
                    
                    
                }
                else
                {
                    logger.logComment("Warning: cell mechanism "+cm.getInstanceName()+" is not implemented in ChannelML");
                    File warnFile = new File(ProjectStructure.getNeuroMLDir(project.getProjectMainDirectory()), cm.getInstanceName()+".warning");
                    try
                    {
                        FileWriter fw = new FileWriter(warnFile);
                        fw.write("Warning: cell mechanism "+cm.getInstanceName()+" is not implemented in ChannelML in the project: "+project.getProjectFileName()+", and so cannot be used in the Python/NeuroML test simulation.");

                        fw.close();

                    }
                    catch(IOException ioe)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem writing to file: " +warnFile, ioe, null);
                    }
                    
                }
            }
            
            //System.out.println("channels "+ channels.size());
            for (int j = 0; j < channels.size(); j++) {
                cmechsElement.addChildElement(channels.get(j));                
            }
            //System.out.println("synapses "+ synapses.size());
            for (int j = 0; j < synapses.size(); j++) {
                cmechsElement.addChildElement(synapses.get(j));                
            }
            
//            ArrayList<SimpleXMLAttribute> namesSpaces = cmechsElement.getAttributes();
//            for (int j = 0; j < namesSpaces.size(); j++)
//            {
//                namesSpaces.get(j).;
//            }

                    
            rootElement.addChildElement(cmechsElement);
            rootElement.addContent("\n\n");
            
            
    //The cell populations present in the network
            SimpleXMLElement el = project.generatedCellPositions.getNetworkMLElement();
            el.addAttribute(new SimpleXMLAttribute("xmlns", NetworkMLConstants.NAMESPACE_URI));
            rootElement.addChildElement(el);

            rootElement.addContent("\n\n");
            
    //The projections between populations in the network 


            SimpleXMLEntity netEntity = project.generatedNetworkConnections.getNetworkMLElement(preferredUnits, extraComments);
            
            if (netEntity instanceof SimpleXMLElement)
            {
                SimpleXMLElement el2 = (SimpleXMLElement)netEntity;
                el2.addAttribute(new SimpleXMLAttribute("xmlns", NetworkMLConstants.NAMESPACE_URI));
                rootElement.addChildElement(el2);
            }
            else if (netEntity instanceof SimpleXMLComment)
            {
                rootElement.addComment((SimpleXMLComment)netEntity);
            }

            rootElement.addContent("\n\n");
            
     //The electrical inputs to the cells in the network
            
            SimpleXMLEntity elecInputEntity = project.generatedElecInputs.getNetworkMLElement(preferredUnits);
            
            if (elecInputEntity instanceof SimpleXMLElement)
            {
                SimpleXMLElement el3 = (SimpleXMLElement)elecInputEntity;
                el3.addAttribute(new SimpleXMLAttribute("xmlns", NetworkMLConstants.NAMESPACE_URI));
                rootElement.addChildElement(el3);
            }
            else if (elecInputEntity instanceof SimpleXMLComment)
            {
                rootElement.addComment((SimpleXMLComment)elecInputEntity);
            }
            
            rootElement.addContent("\n\n");
            
            
      //Returning the file...
            
            String stringForm = doc.getXMLString("", false);

            logger.logComment(stringForm);

            if (!zipped)
            {
                FileWriter fw = new FileWriter(neuroMLFile);
                fw.write(stringForm);
                fw.close();
                
                return neuroMLFile;
            }
            else
            {
                File zipFile = neuroMLFile;
                
                if (!neuroMLFile.getName().endsWith(ProjectStructure.getNeuroMLCompressedFileExtension()))
                    zipFile = new File(neuroMLFile.getAbsolutePath() +
                                        ProjectStructure.getNeuroMLCompressedFileExtension());
                
                String internalFilename = GeneralUtils.replaceAllTokens(zipFile.getName(), 
                                        ProjectStructure.getNeuroMLCompressedFileExtension(), 
                                        ProjectStructure.getNeuroMLFileExtension());
                
                ZipUtils.zipStringAsFile(stringForm, zipFile, internalFilename, notes.toString());
                
                return zipFile;
            }
        }
        catch (Exception ex)
        {
            logger.logError("Problem creating NeuroML file: "   + neuroMLFile.getAbsolutePath(), ex);
            
            throw new NeuroMLException("Problem creating NeuroML file: "  + neuroMLFile.getAbsolutePath(), ex);
        }
        
    }
    
    
       
    public static File saveNetworkStructureXML(Project project,
                                        File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments,
                                       String simConfig,
                                       String units) throws NeuroMLException
    {
        int preferredUnits = UnitConverter.getUnitSystemIndex(units);
        
        try
        {
                    
            StringBuffer notes = new StringBuffer("\nNetwork structure for project: "
                                +project.getProjectName() + " saved with neuroConstruct v"+
                                GeneralProperties.getVersionNumber()+" on: "+ GeneralUtils.getCurrentTimeAsNiceString() +", "
                                + GeneralUtils.getCurrentDateAsNiceString()+"\n\n");
            
            
            Iterator<String> cellGroups = project.generatedCellPositions.getNamesGeneratedCellGroups();
            
            while (cellGroups.hasNext())
            {
                String cg = cellGroups.next();
                int numHere = project.generatedCellPositions.getNumberInCellGroup(cg);
                if (numHere>0)
                notes.append("Cell Group: "+cg+" contains "+numHere+" cells\n");
                
            }
            notes.append("\n");
            
            Iterator<String> netConns = project.generatedNetworkConnections.getNamesNetConnsIter();
            
            while (netConns.hasNext())
            {
            String mc = netConns.next();
            int numHere = project.generatedNetworkConnections.getSynapticConnections(mc).size();
            if (numHere>0)
            notes.append("Network connection: "+mc+" contains "+numHere+" individual synaptic connections\n");
            
            }
            notes.append("\n");
            
            logger.logComment("Going to save network in NeuroML format in " + neuroMLFile.getAbsolutePath());

            SimpleXMLDocument doc = new SimpleXMLDocument();

            SimpleXMLElement rootElement = null;

            rootElement = new SimpleXMLElement(NetworkMLConstants.ROOT_ELEMENT);

            rootElement.addNamespace(new SimpleXMLNamespace("", NetworkMLConstants.NAMESPACE_URI));

            rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                            MetadataConstants.NAMESPACE_URI));

            rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                            NeuroMLConstants.XSI_URI));

            rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                            NetworkMLConstants.NAMESPACE_URI
                                                            + "  " + NetworkMLConstants.DEFAULT_SCHEMA_FILENAME));

            rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, "micron"));

            doc.addRootElement(rootElement);

            logger.logComment("    ****    Full XML:  ****");
            logger.logComment("  ");

            rootElement.addContent("\n\n");

            rootElement.addChildElement(new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                             MetadataConstants.NOTES_ELEMENT, "\n" + notes.toString()));

            SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                          MorphMLConstants.PROPS_ELEMENT);

            rootElement.addContent("\n\n");

            rootElement.addChildElement(props);

            MetadataConstants.addProperty(props,
                                          NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED,
                                          project.generatedCellPositions.getRandomSeed() + "",
                                          "    ");

            if (simConfig!=null)
            {
                MetadataConstants.addProperty(props,
                                              NetworkMLConstants.NC_SIM_CONFIG,
                                              simConfig,
                                              "    ");
            }

            rootElement.addContent("\n\n");

            rootElement.addChildElement(project.generatedCellPositions.getNetworkMLElement());

            rootElement.addContent("\n\n");

            SimpleXMLEntity netEntity = project.generatedNetworkConnections.getNetworkMLElement(preferredUnits, extraComments);
            
            if (netEntity instanceof SimpleXMLElement)
            {
                rootElement.addChildElement((SimpleXMLElement)netEntity);
            }
            else if (netEntity instanceof SimpleXMLComment)
            {
                rootElement.addComment((SimpleXMLComment)netEntity);
            }

            rootElement.addContent("\n\n");
            
            SimpleXMLEntity elecInputEntity = project.generatedElecInputs.getNetworkMLElement(preferredUnits);
            
            if (elecInputEntity instanceof SimpleXMLElement)
            {
                rootElement.addChildElement((SimpleXMLElement)elecInputEntity);
            }
            else if (elecInputEntity instanceof SimpleXMLComment)
            {
                rootElement.addComment((SimpleXMLComment)elecInputEntity);
            }
            
            rootElement.addContent("\n\n");
            
            String stringForm = doc.getXMLString("", false);

            logger.logComment(stringForm);

            if (!zipped)
            {
                FileWriter fw = new FileWriter(neuroMLFile);
                fw.write(stringForm);
                fw.close();
                
                return neuroMLFile;
            }
            else
            {
                File zipFile = neuroMLFile;
                
                if (!neuroMLFile.getName().endsWith(ProjectStructure.getNeuroMLCompressedFileExtension()))
                    zipFile = new File(neuroMLFile.getAbsolutePath() +
                                        ProjectStructure.getNeuroMLCompressedFileExtension());
                
                String internalFilename = GeneralUtils.replaceAllTokens(zipFile.getName(), 
                                        ProjectStructure.getNeuroMLCompressedFileExtension(), 
                                        ProjectStructure.getNeuroMLFileExtension());
                
                ZipUtils.zipStringAsFile(stringForm, zipFile, internalFilename, notes.toString());
                
                return zipFile;
            }
        }
        catch (Exception ex)
        {
            logger.logError("Problem creating NeuroML file: "   + neuroMLFile.getAbsolutePath(), ex);
            
            throw new NeuroMLException("Problem creating NeuroML file: "  + neuroMLFile.getAbsolutePath(), ex);
        }
    }
    
    
    public Display3DProperties getProjectDispProps()
    {
        return activeProject.proj3Dproperties;
    }
    
    public ProjectProperties getProjectProps()
    {
        return activeProject.projProperties;
    }

    public static Random getRandomGenerator()
    {
        return randomGenerator;
    }

    public static long getRandomGeneratorSeed()
    {
        return currentSeed;
    }

    public static void setRandomGeneratorSeed(long seed)
    {
        currentSeed = seed;
    }



    /**
     * Zips the files found in dirToZip and places the zip file in a file called nameOfZippedFile
     */
    public static void zipDirectoryContents(File dirToZip, String nameOfZippedFile)
    {
        logger.logComment("Zipping up the project...");

        File zipFile = null;
        try
        {
            ArrayList<String> ignore = new ArrayList<String> ();
            ArrayList<String> ignoreExtn = new ArrayList<String> ();
            ignore.add("i686");
            ignore.add("x86_64");
            ignore.add(".svn");

            zipFile = ZipUtils.zipUp(dirToZip, nameOfZippedFile, ignore, ignoreExtn);

            GuiUtils.showInfoMessage(logger, "Success", "The zip file: "+ zipFile.getAbsolutePath() + " ("+zipFile.length()+" bytes)  contains all of the project files", null);
            return;
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem creating the zipped file: "+ nameOfZippedFile, ex, null);
        }

    }


    /**
     * Uses current seed to setup Random number generator.
     */
    public static void reinitialiseRandomGenerator()
    {
        randomGenerator = new Random(currentSeed);

    }

    public String getCellDensitiesReport(boolean html)
    {
        SimpleHtmlDoc report = new SimpleHtmlDoc();

        ArrayList<String> cgNames = activeProject.cellGroupsInfo.getAllCellGroupNames();

        if (cgNames.size()==0)
        {
            report.addTaggedElement("No Cell Groups in project", "font color=\"red\"");
        }

        for (String cellGroup: cgNames)
        {
            int num = activeProject.generatedCellPositions.getNumberInCellGroup(cellGroup);
            Region region = activeProject.regionsInfo.getRegionObject(activeProject.cellGroupsInfo.getRegionName(cellGroup));
            String cellType = activeProject.cellGroupsInfo.getCellType(cellGroup);
            Cell cell = activeProject.cellManager.getCell(cellType);

            Color colour = activeProject.cellGroupsInfo.getColourOfCellGroup(cellGroup);
            String hexString = "#"+Integer.toHexString( colour.getRGB() & 0xFFFFFF ).toUpperCase();

            float regionVol = (float)region.getVolume();

            report.addTaggedElement("<b>Cell Group: "+cellGroup+"</b>", "font color=\""+hexString+"\"");
            report.addTaggedElement("Number of cells of type: "+cellType+" in Cell Group: "+num, "p");
            report.addTaggedElement("Region: "+region+"<br>"
                                    +"Region volume: "+ regionVol + " \u03bcm\u00b3 ("+(float)(region.getVolume()*1e-9)+" mm\u00b3)", "p");

            if (num>0)
            {
                float numPerMm3 = (float) (num / (region.getVolume() * 1e-9f));

                report.addTaggedElement("Number of cells per mm\u00b3 in this region: " + numPerMm3, "p");

                float cellVol = CellTopologyHelper.getVolume(cell, false);

                report.addTaggedElement("Volume of single cell: " + cellVol + " \u03bcm\u00b3<br>"
                                        + num + " cells fill " + (cellVol * num * 100 / regionVol) + "% of volume of region", "p");

                float somaVol = CellTopologyHelper.getVolume(cell, true);

                report.addTaggedElement("Total volume of soma of cell: " + somaVol + " \u03bcm\u00b3<br>"
                                        + num + " cell somas fill " + (somaVol * num * 100 / regionVol) + "% of volume of region",
                                        "p");
            }
            report.addBreak();

        }

        if (html) return report.toHtmlString();
        return report.toString();

    }



    /**
     * Displays information on the validity of the cells in the project and other
     * project settings
     */
    public void doValidate(boolean html)
    {
        boolean verbose = false;
        
        File schemaFile = GeneralProperties.getNeuroMLSchemaFile();

        SimpleHtmlDoc report = new SimpleHtmlDoc();

        report.addTaggedElement("Validating the project: "+ this.activeProject.getProjectFile(), "h3");

        report.addTaggedElement("Validating cells in project", "p");

        String overallValidity = ValidityStatus.VALIDATION_OK;

        ArrayList<String> cellNames = activeProject.cellManager.getAllCellTypeNames();

        if (cellNames.size()==0)
        {
            overallValidity = ValidityStatus.VALIDATION_ERROR;
            report.addTaggedElement("No Cell Types in project", "font color=\""+ValidityStatus.VALIDATION_COLOUR_ERROR+"\"");
        }

        for (String cellTypeName: cellNames)
        {
            report.addTaggedElement("Checking cell: <b>"+cellTypeName+"</b>", "p");

            Cell cell = activeProject.cellManager.getCell(cellTypeName);

            ValidityStatus status = CellTopologyHelper.getValidityStatus(cell);

            overallValidity = ValidityStatus.combineValidities(overallValidity, status.getValidity());

            String format = "font color=\""+status.getColour()+"\"";

            String message = status.getMessage();

            if (html)
            {
                message = GeneralUtils.replaceAllTokens(message, "\n", "<br>");
            }



            report.addTaggedElement(message, format);
            report.addBreak();

            ValidityStatus bioStatus = CellTopologyHelper.getBiophysicalValidityStatus(cell, this.activeProject);

            overallValidity = ValidityStatus.combineValidities(overallValidity, bioStatus.getValidity());

            String bioFormat = "font color=\""+bioStatus.getColour()+"\"";

            message = bioStatus.getMessage();
            
            if (html)
            {
                message = GeneralUtils.replaceAllTokens(message, "\n", "<br>");
            }
            report.addTaggedElement(message, bioFormat);
            
            if (!activeProject.cellGroupsInfo.getUsedCellTypes().contains(cell.getInstanceName()))
            {
                report.addBreak();
                report.addTaggedElement("Note: Cell Type: "+cell.getInstanceName()+" not currently used in any Cell Group",
                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
            }

        }

        report.addBreak();
        report.addBreak();

        report.addTaggedElement("Validating Cell Mechanisms (using NeuroML version "+GeneralProperties.getNeuroMLVersionNumber()+")", "p");

        Vector<String> cellMechNames = this.activeProject.cellMechanismInfo.getAllCellMechanismNames();

        for (String nextCellMechName: cellMechNames)
        {
            String cellMechValidity = ValidityStatus.VALIDATION_OK;

            report.addTaggedElement("Checking Cell Mechanism: <b>"+nextCellMechName+"</b>", "p");
            CellMechanism next = activeProject.cellMechanismInfo.getCellMechanism(nextCellMechName);

            if (next==null)
            {
                report.addTaggedElement("Error, problem retrieving info on that Cell Mechanism",
                                        "font color=\""+ValidityStatus.VALIDATION_COLOUR_ERROR+"\"");

                cellMechValidity = ValidityStatus.VALIDATION_ERROR;

            }

            if (next instanceof AbstractedCellMechanism)
            {
                MechanismImplementation[] mechImpls = ((AbstractedCellMechanism)next).getMechanismImpls();
                for (int i = 0; i < mechImpls.length; i++)
                {
                    File f = mechImpls[i].getImplementingFileObject(activeProject, next.getInstanceName());
                    if (f.exists())
                    {
                        if (verbose) report.addTaggedElement("Implementation file: "+f.getAbsolutePath()+" found", "p");
                    }
                    else
                    {
                        report.addTaggedElement("Error, implementation file: "+f.getAbsolutePath()+" not found",
                                                "font color=\""+ValidityStatus.VALIDATION_COLOUR_ERROR+"\"");

                        cellMechValidity = ValidityStatus.VALIDATION_ERROR;

                    }
                }
            }
            else if (next instanceof ChannelMLCellMechanism)
            {
                ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)next;

                try
                {
                    File cmlFile = cmlCm.initialise(activeProject, false);

                    if (cmlFile != null && cmlFile.exists())
                    {
                        if (verbose) report.addTaggedElement("ChannelML file: " + cmlFile.getAbsolutePath() + " found", "p");
                    }
                    else
                    {
                        report.addTaggedElement("Error, ChannelML file: " + cmlFile.getAbsolutePath() + " not found",
                                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                        cellMechValidity = ValidityStatus.VALIDATION_ERROR;

                    }

                    ArrayList<SimXSLMapping> simMappings = cmlCm.getSimMappings();

                    for (SimXSLMapping simMapping : simMappings)
                    {
                        File f = simMapping.getXslFileObject(activeProject, next.getInstanceName());

                        if (f != null && f.exists())
                        {
                            if (verbose) report.addTaggedElement("Implementation file: " + f.getAbsolutePath() + " found", "p");
                        }
                        else
                        {
                            report.addTaggedElement("Error, implementation file: "+simMapping
                                    +" for cell mechanism: "+cmlCm.getInstanceName()+" not found",
                                                    "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                            cellMechValidity = ValidityStatus.VALIDATION_ERROR;

                        }
                    }

                    String status = null;
                    
                    if (cmlCm.isChannelMechanism())
                        status = cmlCm.getValue(ChannelMLConstants.getChannelStatusValueXPath());
                    else if (cmlCm.isSynapticMechanism())
                        status = cmlCm.getValue(ChannelMLConstants.getSynapseStatusValueXPath());
                    else if (cmlCm.isIonConcMechanism())
                        status = cmlCm.getValue(ChannelMLConstants.getIonConcStatusValueXPath());

                    if (status != null)
                    {
                        if (status.equals(ChannelMLConstants.STATUS_VALUE_ATTR_STABLE))
                        {
                            report.addTaggedElement("Status of ChannelML file: " + status,
                                                    "font color=\"" + ValidityStatus.VALIDATION_COLOUR_OK + "\"");
                        }
                        else if (status.equals(ChannelMLConstants.STATUS_VALUE_ATTR_IN_PROGRESS))
                        {
                            report.addTaggedElement("Status of ChannelML file: " + status,
                                                    "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");

                            cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_WARN);
                        }
                        else if (status.equals(ChannelMLConstants.STATUS_VALUE_ATTR_KNOWN_ISSUES))
                        {
                            report.addTaggedElement("Status of ChannelML file: " + status,
                                                    "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                            cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_ERROR);
                        }

                    }
                    else
                    {
                        report.addTaggedElement("Note: status of ChannelML file cannot be determined. &lt;status&gt; element added in NeuroML v1.6",
                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
                    }
                    
                    
                    try
                    {
                        SchemaFactory factory 
                            = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                        logger.logComment("Found the XSD file: " + schemaFile.getAbsolutePath());

                        Source schemaFileSource = new StreamSource(schemaFile);
                        Schema schema = factory.newSchema(schemaFileSource);

                        Validator validator = schema.newValidator();

                        Source xmlFileSource = new StreamSource(cmlCm.getChannelMLFile(activeProject));

                        validator.validate(xmlFileSource);

                    }
                    catch (SAXException ex)
                    {
                        logger.logError("Problem validating ChannelML file", ex);
                        
                        report.addTaggedElement("<br>Problem validating ChannelML file against NeuroML v"
                                +GeneralProperties.getNeuroMLVersionNumber()
                                +". Note it may be compliant to an earlier version of the standard and this may "
                                +"not matter if the XSL mappings are for that version of the specification too.",
                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");

                        cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_WARN);
                       
                    }

                    catch (IOException ex)
                    {
                        logger.logError("<br>Problem validating ChannelML file", ex);
                        report.addTaggedElement("Problem validating ChannelML file: "+ cmlCm.getChannelMLFile()
                                +" against XSD file: " +schemaFile.getAbsolutePath()+".",
                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                        cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_ERROR);
                       
                    }
                    
                }
                catch (ChannelMLException ex)
                {
                    report.addTaggedElement("Error instantiating Channel mechanism: " + cmlCm.getInstanceName()
                                            +", file: "+ cmlCm.getChannelMLFile(),
                                            "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                    cellMechValidity = ValidityStatus.VALIDATION_ERROR;

                }
            }

            overallValidity = ValidityStatus.combineValidities(overallValidity, cellMechValidity);

        }
        

        report.addBreak();


        report.addTaggedElement("Validating Cell Groups...", "p");

        ArrayList<String> allCellGroupsWithStims = new ArrayList<String>();
        ArrayList<String> allPostSynCellGroups = new ArrayList<String>();

        for (StimulationSettings stimSet:activeProject.elecInputInfo.getAllStims())
        {
            allCellGroupsWithStims.add(stimSet.getCellGroup());
        }
        for (String netConn:activeProject.morphNetworkConnectionsInfo.getAllSimpleNetConnNames())
        {
            allPostSynCellGroups.add(activeProject.morphNetworkConnectionsInfo.getTargetCellGroup(netConn));
        }
        for (String netConn:activeProject.volBasedConnsInfo.getAllAAConnNames())
        {
            allPostSynCellGroups.add(activeProject.volBasedConnsInfo.getTargetCellGroup(netConn));
        }


        for (String cellGroup: activeProject.cellGroupsInfo.getAllCellGroupNames())
        {
            String cellType = activeProject.cellGroupsInfo.getCellType(cellGroup);
            if (!activeProject.cellManager.getAllCellTypeNames().contains(cellType))
            {
                report.addTaggedElement("Error, Cell Group: " + cellGroup + " specifies cell type: " + cellType +
                                        ", but there is no such cell in project!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                report.addBreak();

                overallValidity = ValidityStatus.VALIDATION_ERROR;
            }

            if (!(allCellGroupsWithStims.contains(cellGroup)||allPostSynCellGroups.contains(cellGroup)))
            {
                report.addTaggedElement("Warning, Cell Group: " + cellGroup + " has neither an electrical stimulation nor is "
                                        +"postsynaptically connected to another Cell Group. Unless the cells are spontaneously active,"
                                        +" nothing much will happen to those cells during a simulation.",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");

                report.addBreak();

                overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);

            }
            if (!activeProject.simConfigInfo.getAllUsedCellGroups().contains(cellGroup))
            {
                report.addBreak();
                report.addTaggedElement("Note: Cell Group: "+cellGroup+" is not currently included in any Simulation Configuration",
                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
            }
        }




        report.addTaggedElement("Validating Electrical Inputs...", "p");

        Vector<StimulationSettings> inputs = this.activeProject.elecInputInfo.getAllStims();

        for (StimulationSettings stim: inputs)
        {
            String cellType = activeProject.cellGroupsInfo.getCellType(stim.getCellGroup());
            Cell cell = activeProject.cellManager.getCell(cellType);
            
            if (cellType==null)
            {
                report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies Cell Group: " + stim.getCellGroup() +
                                        ", but there isn't any Cell Group by that name in the project!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");
                report.addBreak();

                overallValidity = ValidityStatus.VALIDATION_ERROR;
            }
            else if (cell==null)
            {
                report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies Cell Group: " + stim.getCellGroup() +
                                        ", cell type: "+cellType+", but there isn't any Cell of that type in the project!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");
                report.addBreak();

                overallValidity = ValidityStatus.VALIDATION_ERROR;
            }
            else
            {
                
                if (stim.getSegChooser() instanceof IndividualSegments)
                {
                    for(int id: ((IndividualSegments)stim.getSegChooser()).getListOfSegmentIds())
                    {
                        if (cell.getSegmentWithId(id)==null)
                        {
                            report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies segments: "+stim.getSegChooser()
                                +" on cells of type " + cellType +
                                                ", but this generates an invalid id: "+id+" for such cells!",
                                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                            report.addBreak();

                            overallValidity = ValidityStatus.VALIDATION_ERROR;
                        }
                    }
                }
                if(stim.getSegChooser() instanceof GroupDistributedSegments)
                {
                    String grp = ((GroupDistributedSegments)stim.getSegChooser()).getGroup();
                    int num = ((GroupDistributedSegments)stim.getSegChooser()).getNumberOfSegments();
                    if (!cell.isGroup(grp))
                    {
                            report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies segments: "+stim.getSegChooser()
                                +" but there is no group "+grp+" on such cells!",
                                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                            report.addBreak();

                            overallValidity = ValidityStatus.VALIDATION_ERROR;
                    }
                    if (num<=0)
                    {
                            report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies segments: "+stim.getSegChooser()
                                +" but "+num+" is an invalid number of segments for this!",
                                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                            report.addBreak();

                            overallValidity = ValidityStatus.VALIDATION_ERROR;
                    }
                }
                    
                
                if (!activeProject.simConfigInfo.getAllUsedElectInputs().contains(stim.getReference()))
                {
                    report.addBreak();
                    report.addTaggedElement("Note: Input: "+stim.getReference()+" is not currently included in any Simulation Configuration",
                            "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
                }
            }
        }
        
        report.addTaggedElement("Validating Plot Settings...", "p");

        Vector<SimPlot> plots = this.activeProject.simPlotInfo.getAllSimPlots();

        for (SimPlot plot: plots)
        {
            if (!activeProject.cellGroupsInfo.getAllCellGroupNames().contains(plot.getCellGroup()))
            {
                report.addTaggedElement("Error, Plot: " + plot.getPlotReference() + " specifies Cell Group: " + plot.getCellGroup() +
                                        ", but there isn't any Cell Group by that name in the project!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");
                report.addBreak();

                overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);
            }
            
            
            if (!activeProject.simConfigInfo.getAllUsedPlots().contains(plot.getPlotReference()))
            {
                report.addBreak();
                report.addTaggedElement("Note: Plot: "+plot.getPlotReference()+" is not currently included in any Simulation Configuration",
                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
            }
        }


        report.addTaggedElement("Validating simulation settings...", "p");

        if (this.activeProject.simulationParameters.getDt()<=0)
        {
            report.addTaggedElement("Error: Simulation must have a positive, non zero timestep. 0.025 ms is a commonly used value.",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                overallValidity = ValidityStatus.VALIDATION_ERROR;

        }
        if (this.activeProject.simulationParameters.getDt()>=0.1)
        {
            report.addTaggedElement(
                "Warning: Simulation timestep of "+activeProject.simulationParameters.getDt()
                                    +" ms may be too large. 0.025 ms is a commonly used value.",
                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");

                overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);
        }

        ////  Validation of temperature...

        report.addTaggedElement("Validating experimental temperature...", "p");

        if (this.activeProject.simulationParameters.getTemperature() < 0)
        {
            report.addTaggedElement("Error, simulation temperature below zero celsius!",
                                    "font color=\""+ValidityStatus.VALIDATION_COLOUR_ERROR+"\"");

            overallValidity = ValidityStatus.VALIDATION_ERROR;
            //allValid = false;
        }
        else if (this.activeProject.simulationParameters.getTemperature() >=50)
        {
            report.addTaggedElement("Warning, simulation temperature above 50 celsius!",
                                    "font color=\""+ValidityStatus.VALIDATION_COLOUR_WARN+"\"");

            overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);
            //allValid = false;
        }

        else
        {
            report.addTaggedElement("Temperature within bounds",
                                                     "font color=\""+ValidityStatus.VALIDATION_COLOUR_OK+"\"");
        }

        report.addTaggedElement("Validation complete.", "p");

        ValidityStatus projectOverallValidity = null;

        if (overallValidity.equals(ValidityStatus.VALIDATION_OK))
        {
            projectOverallValidity = ValidityStatus.getValidStatus("Project is valid");
        }
        else if (overallValidity.equals(ValidityStatus.VALIDATION_WARN))
        {
            projectOverallValidity = ValidityStatus.getWarningStatus("Project validation has led to a number of warnings. See above for details.");
        }
        else
        {
            projectOverallValidity = ValidityStatus.getErrorStatus("Project validation failed. See above for details.");
        }


        report.addTaggedElement(projectOverallValidity.getMessage(),
                                "font color=\""+projectOverallValidity.getColour()+"\"");



        report.addBreak();
        report.addBreak();


        SimpleViewer.showString(report.toHtmlString(), "Validity status", 12, false, html);
    }



    /**
     * Generate the positions of the cells and the network connections...
     */
    public void doGenerate(String simConfigName, long randomSeed)
    {
        
        logger.logComment("-----  Project Manager generating network...");
        
        if (activeProject == null)
        {
            logger.logError("No project loaded...", true);
            return;
        }
        currentSeed = randomSeed;
        reinitialiseRandomGenerator();

        activeProject.resetGenerated();

        cellPosnGenerator = new CellPositionGenerator(activeProject, reportInterface);

        SimConfig simConfig = getCurrentProject().simConfigInfo.getSimConfig(simConfigName);

        cellPosnGenerator.setSimConfig(simConfig);

        activeProject.generatedCellPositions.setRandomSeed(currentSeed);

        currentlyGenerating = true;
        
        cellPosnGenerator.start();

    }
    
    public boolean isGenerating()
    {
        return currentlyGenerating;
    }



    public Project loadProject(File projFile) throws ProjectFileParsingException
    {
        activeProject = Project.loadProject(projFile, projEventListener);
        return activeProject;
    }
    

    public void giveGenerationReport(String report, String generatorType, SimConfig simConfig)
    {
        logger.logComment(">>> "+ generatorType+ " giving report: "+ report);

        if (generatorType.equals(CellPositionGenerator.myGeneratorType))
        {
            if (activeProject.generatedCellPositions.getNumberPositionRecords() == 0)
            {
                logger.logComment("No cell positions generated. Please ensure the cell bodies will fit in the selected regions.");
                
                currentlyGenerating = false;
                return;
            }

            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of cell positions was interrupted...");
                currentlyGenerating = false;
                return;
            }
            
            netConnGenerator = new MorphBasedConnGenerator(activeProject, this);

            netConnGenerator.setSimConfig(simConfig);

            netConnGenerator.start();

        }
        else if (generatorType.equals(MorphBasedConnGenerator.myGeneratorType))
        {
            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of connections was interrupted...");
                currentlyGenerating = false;
                return;
            }

            arbourConnectionGenerator = new VolumeBasedConnGenerator(activeProject, this);

            arbourConnectionGenerator.setSimConfig(simConfig);

            arbourConnectionGenerator.start();



        }

        else if (simConfig.getMpiConf().isParallel() 
                   && generatorType.equals(VolumeBasedConnGenerator.myGeneratorType))
        {

            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of connections was interrupted...");
                currentlyGenerating = false;
                return;
            }

            compNodeGenerator = new CompNodeGenerator(activeProject, this);

            compNodeGenerator.setSimConfig(simConfig);

            compNodeGenerator.start();
        }

        else if ((!(simConfig.getMpiConf().isParallel())
                && (generatorType.equals(VolumeBasedConnGenerator.myGeneratorType))
                || generatorType.equals(CompNodeGenerator.myGeneratorType)))
        {

            if (report.indexOf("Generation interrupted")>0)
            {
                logger.logComment("It seems the generation of compute nodes was interrupted...");
                currentlyGenerating = false;
                return;
            }


            elecInputGenerator = new ElecInputGenerator(activeProject, this);

            elecInputGenerator.setSimConfig(simConfig);

            elecInputGenerator.start();

        }


        else if (generatorType.equals(ElecInputGenerator.myGeneratorType))
        {

            if (report.indexOf("Generation interrupted") > 0)
            {
                logger.logComment("It seems the generation of cell positions was interrupted...");
                currentlyGenerating = false;
                return;
            }


            plotSaveGenerator = new PlotSaveGenerator(activeProject, this);

            plotSaveGenerator.setSimConfig(simConfig);

            plotSaveGenerator.start();


        }

        else if (generatorType.equals(PlotSaveGenerator.myGeneratorType))
        {

            
            currentlyGenerating = false;
        }
        else
        {
            logger.logComment("Don't know the type of that generation report!!: " + generatorType);
            
            //////////////currentlyGenerating = false;
        }

        //////////////currentlyGenerating = false;


    }
    
    
    public void majorStepComplete()
    {
            logger.logComment(">>> -----------------");
    };
    

    public void giveUpdate(String update)
    {
            logger.logComment(">>> "+ update);
    };
    
    public static void main(String[] args) throws ProjectFileParsingException
    {
        
        Project proj = Project.loadProject(new File("examples/Ex1-Simple/Ex1-Simple.neuro.xml"), null);
        File neuroMLDir = ProjectStructure.getNeuroMLDir(proj.getProjectMainDirectory());
        File generatedNetworkFile = new File(neuroMLDir, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML);
        //saveGeneratedNetworkXML(proj, generatedNetworkFile, false, false, proj.simConfigInfo, NetworkMLConstants.UNITS_PHYSIOLOGICAL);
    }


}
