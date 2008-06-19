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

import java.util.logging.Level;
import java.util.zip.*;
import org.xml.sax.*;

import javax.xml.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.neuroml.hdf5.*;
import ucl.physiol.neuroconstruct.neuron.NeuronException;
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
    
    public String status()
    {
        StringBuffer info = new StringBuffer();
        
        info.append("neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
            
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
            } 
            catch (NoProjectLoadedException ex) 
            {
                info.append("No project loaded!"+"/n");
            }
        }
        
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
        else if (activeProject.volBasedConnsInfo.isValidAAConn(selectedNetConn))
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

            else if (activeProject.volBasedConnsInfo.isValidAAConn(selectedNetConn))
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
                throw new NeuroMLException("Problem parsing XML based NetworkML file", e);
            }
            
        }

/*
            String prevSimConfig = nmlBuilder.getSimConfig();
            long randomSeed = nmlBuilder.getRandomSeed();

            if (randomSeed!=Long.MIN_VALUE)
            {
                this.jTextFieldRandomGen.setText(randomSeed+"");
                ProjectManager.setRandomGeneratorSeed(randomSeed);
                ProjectManager.reinitialiseRandomGenerator();
            }
            if (prevSimConfig!=null)
            {
                this.jComboBoxSimConfig.setSelectedItem(prevSimConfig);
            }
        }*/
        
    }

    /*public void doSaveNetworkML(File networkmlFile) throws NeuroMLException
    {
        doSaveNetworkML(networkmlFile, false, false, SimConfigInfo.DEFAULT_SIM_CONFIG_NAME);
    }


    public void doSaveNetworkML(File networkmlFile,
                                boolean zip,
                                boolean extraComments,
                                String simConfig) throws NeuroMLException
    {

        logger.logComment("Going to save the networkmlFile: "+networkmlFile);


        getCurrentProject().saveNetworkStructure(networkmlFile,
                                                 zip,
                                                 extraComments,
                                                 simConfig);


    }*/


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
        //StringBuffer info = new StringBuffer();

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

            //System.out.println("ov: "+ overallValidity);

            message = bioStatus.getMessage();

            if (html)
            {
                message = GeneralUtils.replaceAllTokens(message, "\n", "<br>");
            }
            report.addTaggedElement(message, bioFormat);

        }

        report.addBreak();
        report.addBreak();

        report.addTaggedElement("Validating Cell Mechanisms", "p");

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
                if (cell.getSegmentWithId(stim.getSegmentID())==null)
                {
                    report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies segment ID: "+stim.getSegmentID()
                        +" on cells of type " + cellType +
                                        ", but there isn't any segment with that ID on such cells!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");
                    
                    report.addBreak();

                    overallValidity = ValidityStatus.VALIDATION_ERROR;
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
            logger.logError("No project loaded...");
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

            //String currentReport = jEditorPaneGenerateInfo.getText();

            //String update = new String(currentReport.substring(0,currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
            //                           +report);
            
            //jEditorPaneGenerateInfo.setText(update+"  ");



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
            //String currentReport = jEditorPaneGenerateInfo.getText();

            //String update = new String(currentReport.substring(0,currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
            //                           +report);
            
            //jEditorPaneGenerateInfo.setText(update);

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
            //String currentReport = jEditorPaneGenerateInfo.getText();

            //String update = new String(currentReport.substring(0,currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
            //                           +report);

            //jEditorPaneGenerateInfo.setText(update);



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
            //String currentReport = jEditorPaneGenerateInfo.getText();

            //String update = new String(currentReport.substring(0, currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
            //                           + report);
            //jEditorPaneGenerateInfo.setText(update);

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

            //String currentReport = jEditorPaneGenerateInfo.getText();

            //String update = new String(currentReport.substring(0, currentReport.lastIndexOf("</body>")) // as the jEditorPane returns html...
            //                           + report);

            //jEditorPaneGenerateInfo.setText(update);

            //this.jButtonGenerateStop.setEnabled(false);

            //refreshTabGenerate();
            
            currentlyGenerating = false;
        }
        else
        {
            logger.logComment("Don't know the type of that generation report!!: " + generatorType);
            
            currentlyGenerating = false;
        }

        currentlyGenerating = false;


    }
    
    
    public void majorStepComplete()
    {
            logger.logComment(">>> -----------------");
    };
    

    public void giveUpdate(String update)
    {
            logger.logComment(">>> "+ update);
    };


}
