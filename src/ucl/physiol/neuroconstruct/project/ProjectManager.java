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

package ucl.physiol.neuroconstruct.project;

import java.io.*;
import java.util.*;

import java.awt.*;  

import java.beans.XMLEncoder;
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
import ucl.physiol.neuroconstruct.genesis.GenesisException;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;
import ucl.physiol.neuroconstruct.neuroml.hdf5.*;
import ucl.physiol.neuroconstruct.neuron.NeuronException;
import ucl.physiol.neuroconstruct.project.segmentchoice.*;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.psics.PsicsException;
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
    public CellInitialiser cellInitialiser = null;

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
        StringBuilder info = new StringBuilder();
        
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
                info.append("No project loaded!"+"/n"+ex.getMessage()+"\n");
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

    public boolean doRunPsics(SimConfig simConfig, boolean showConsole)
    {
        try
        {
            activeProject.psicsFileManager.runFile(true, false, false, showConsole);
        }
        catch (PsicsException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, null);
            return false;
        }
        return true;
    }

    public boolean doRunGenesis(SimConfig simConfig)
    {

        String simRef = activeProject.simulationParameters.getReference();
        try
        {
            if (GeneralProperties.getGenerateMatlab())
            {
                MatlabOctave.createSimulationLoader(activeProject, simConfig, simRef);
            }

            if ((GeneralUtils.isWindowsBasedPlatform() || GeneralUtils.isMacBasedPlatform())
                && GeneralProperties.getGenerateIgor())
            {
                IgorNeuroMatic.createSimulationLoader(activeProject, simConfig, simRef);
            }

            activeProject.genesisFileManager.runGenesisFile();
        }
        catch (GenesisException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, null);
            return false;
        }
        return true;
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
                        fn.endsWith(".h5") ||
                            (activeProject.neuronSettings.isCopySimFiles() &&
                            (fn.endsWith(".hoc") ||
                            fn.endsWith(".mod") ||
                            fn.endsWith(".dll"))))
                {
                    try
                    {
                        //System.out.println("Saving a copy of file: " + generatedNeuronFiles[i]+ " to dir: " + dirForSimFiles);

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
                         generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_I386) ||
                         generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_64BIT) ||
                         generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_UMAC) ||
                         generatedNeuronFiles[i].getName().equals(GeneralUtils.DIR_POWERPC)))
                {
                    File toDir = new File(dirForSimFiles, generatedNeuronFiles[i].getName());
                    toDir.mkdir();
                    logger.logComment("Saving the linux libs from the compiled mods of file: " +
                                      generatedNeuronFiles[i] + " to dir: " + toDir);

                    try
                    {
                        GeneralUtils.copyDirIntoDir(generatedNeuronFiles[i], toDir, true, true);

                        File special = new File(toDir, "special");
                        logger.logComment("Going to try to change permissions on: "+special.getAbsolutePath());
                        if (special.exists())
                        {
                            Runtime.getRuntime().exec(new String[]{"chmod","u+x",special.getAbsolutePath()});
                        }
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
        cellInitialiser = null;

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

    /*
     * This function loads positions, connections & inputs from a NetworkML file, OVERWRITES the inputs by starting the ElecInputGenerator
     * and generates plot/saves. Replaces earlier doLoadNetworkMLAndGenerate() which had a confusing name
     */
    public boolean doLoadNetworkMLAndRegenerateInputs(File networkmlFile, boolean testMode) throws NeuroMLException, Hdf5Exception, EndOfSequenceException
    {
        NetworkMLnCInfo extraInfo = doLoadNetworkML(networkmlFile, testMode);

        String prevSimConfig = extraInfo.getSimConfig();
        long randomSeed = extraInfo.getRandomSeed();

        setRandomGeneratorSeed(randomSeed);

        elecInputGenerator = new ElecInputGenerator(getCurrentProject(), this);

        elecInputGenerator.setSimConfig(getCurrentProject().simConfigInfo.getSimConfig(prevSimConfig));

        currentlyGenerating = true;

        elecInputGenerator.start();


        return false;
    }

    /*
     * This function loads positions, connections & inputs from a NetworkML file, and generates plot/saves.
     */
    public boolean doLoadNetworkMLAndGeneratePlots(File networkmlFile, boolean testMode) throws NeuroMLException, Hdf5Exception, EndOfSequenceException
    {
        NetworkMLnCInfo extraInfo = doLoadNetworkML(networkmlFile, testMode);

        logger.logComment("---------    Seed: "+ extraInfo.getRandomSeed());
        logger.logComment("---------    SimConf: "+ extraInfo.getSimConfig());

        String prevSimConfig = extraInfo.getSimConfig();
        long randomSeed = extraInfo.getRandomSeed();

        setRandomGeneratorSeed(randomSeed);

        plotSaveGenerator = new PlotSaveGenerator(getCurrentProject(), this);

        plotSaveGenerator.setSimConfig(getCurrentProject().simConfigInfo.getSimConfig(prevSimConfig));

        currentlyGenerating = true;

        plotSaveGenerator.start();


        return false;
    }
    
    
    
    public NetworkMLnCInfo doLoadNetworkML(File networkmlFile, boolean acceptDefaults) throws NeuroMLException, Hdf5Exception, EndOfSequenceException
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
                
                nmlBuilder.setTestMode(acceptDefaults);

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

  
    public static File saveCompleteCellXML(Project project,
                                       File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments,
                                       String cellName) throws NeuroMLException
    {
         try
        {
                    
            StringBuilder notes = new StringBuilder("\nComplete cell model generated with project: "
                                +project.getProjectName() + " saved with neuroConstruct v"+
                                GeneralProperties.getVersionNumber()+" on: "+ GeneralUtils.getCurrentTimeAsNiceString() +", "
                                + GeneralUtils.getCurrentDateAsNiceString()+"\n\n");
            
            logger.logComment("Going to save the complete cell model in " + neuroMLFile.getAbsolutePath());

           SimpleXMLDocument doc = new SimpleXMLDocument();

            SimpleXMLElement rootElement = null;

            rootElement = new SimpleXMLElement(NeuroMLConstants.ROOT_ELEMENT);

            rootElement.addNamespace(new SimpleXMLNamespace("", NeuroMLConstants.NAMESPACE_URI));

            rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                            MetadataConstants.NAMESPACE_URI));  
            rootElement.addNamespace(new SimpleXMLNamespace(MorphMLConstants.PREFIX,
                                                             MorphMLConstants.NAMESPACE_URI));
            rootElement.addNamespace(new SimpleXMLNamespace(BiophysicsConstants.PREFIX,
                                                             BiophysicsConstants.NAMESPACE_URI));
            rootElement.addNamespace(new SimpleXMLNamespace(NetworkMLConstants.PREFIX,
                                                             NetworkMLConstants.NAMESPACE_URI));            

            rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, MetadataConstants.LENGTH_UNITS_MICROMETER));

            doc.addRootElement(rootElement);

            logger.logComment("    ****    Complete Cell Model:  ****");
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

            
    // add cell morphology
            
             rootElement.addContent("\n\n");
            
            SimpleXMLElement morphElement = new SimpleXMLElement("cells");
                            
            Cell cell = project.cellManager.getCell(cellName);
            SimpleXMLElement element = MorphMLConverter.getCellXMLElement(cell, project, NeuroMLLevel.NEUROML_LEVEL_3, NeuroMLVersion.NEUROML_VERSION_1);
            morphElement.addChildElement(element);
                
            rootElement.addChildElement(morphElement);

            rootElement.addContent("\n\n");
            
            
    // add channel mechanisms
            
            SimpleXMLElement cmechsElement = new SimpleXMLElement("channels");
            cmechsElement.addAttribute(new SimpleXMLAttribute(ChannelMLConstants.UNIT_SCHEME, ChannelMLConstants.PHYSIOLOGICAL_UNITS));
                        
            ArrayList<String> allm = project.cellManager.getCell(cellName).getAllChanMechNames(true);
            
            for (int i = 0; i < allm.size(); i++) {
               CellMechanism cm = project.cellMechanismInfo.getCellMechanism(allm.get(i));
                if ((cm instanceof ChannelMLCellMechanism))
                {
                        ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;
                        SimpleXMLEntity[] channels = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getChannelTypeXPath());
                        SimpleXMLElement el = (SimpleXMLElement)channels[0];
                        if (!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                        {
                            el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                        }
                        cmechsElement.addChildElement(el);            
                        cmechsElement.addContent("\n\n");
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
                
            rootElement.addChildElement(cmechsElement);
            rootElement.addContent("\n\n");
            
                 
            
    // generate the file
            
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
    
    
    public static File saveLevel3NetworkXML(Project project,
                                       File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments,
                                       String simConfig,
                                       String networkUnits) throws NeuroMLException
                                       
    {
        return saveLevel3NetworkXML(project, neuroMLFile, zipped, extraComments, false, simConfig, networkUnits);
    }
    
    public static File saveLevel3NetworkXML(Project project,
                                       File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments, 
                                       boolean annotations,
                                       String simConfig,
                                       String networkUnits) throws NeuroMLException
                                       
    {
      
        int preferredUnits = UnitConverter.getUnitSystemIndex(networkUnits);
        
        try
        {
                    
            StringBuilder notes = new StringBuilder("\nComplete network structure generated with project: "
                                +project.getProjectName() + " saved with neuroConstruct v"+
                                GeneralProperties.getVersionNumber()+" on: "+ GeneralUtils.getCurrentTimeAsNiceString() +", "
                                + GeneralUtils.getCurrentDateAsNiceString()+"\n\n");
            
            Iterator<String> cellGroups = project.generatedCellPositions.getNamesGeneratedCellGroups();
            ArrayList<String> cellGroupNamesToInc = new ArrayList<String>();

            if (project.generatedCellPositions.getNumberInAllCellGroups()==0)
            {
                // No net positions, so generate all cells in project

                cellGroupNamesToInc = project.cellGroupsInfo.getAllCellGroupNames();
            }
            
            while (cellGroups.hasNext())
            {
                String cg = cellGroups.next();
                cellGroupNamesToInc.add(cg);
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
                                                            NeuroMLConstants.NAMESPACE_URI+" http://www.neuroml.org/NeuroMLValidator/NeuroMLFiles/Schemata/v"+GeneralProperties.getLatestNeuroMLVersionNumber()
                                                            +"/Level3/NeuroML_Level3_v"+GeneralProperties.getLatestNeuroMLVersionNumber()
                                                            +".xsd"));// + "NeuroMLConstants.NAMESPACE_URI + "  " + NeuroMLConstants.DEFAULT_SCHEMA_FILENAME));
            

            rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, MetadataConstants.LENGTH_UNITS_MICROMETER));

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

            SimConfig sc = project.simConfigInfo.getSimConfig(simConfig);
            
            MetadataConstants.addProperty(props,
                                        NetworkMLConstants.NC_SIM_DURATION,
                                        Float.toString(sc.getSimDuration()),
                                        "    ");
            
            MetadataConstants.addProperty(props,
                                        NetworkMLConstants.NC_SIM_TIME_STEP,
                                        Float.toString(project.simulationParameters.getDt()),
                                        "    ");
            
            MetadataConstants.addProperty(props,
                                        NetworkMLConstants.NC_TEMPERATURE,
                                        Float.toString(project.simulationParameters.getTemperature()),
                                        "    ");
        
            if (annotations)
            {
                rootElement.addContent("\n\n");
                rootElement.addComment("Note: the annotation below contains data to facilitate the reloading of this file into an empty neuroConstruct project.\n" +
                        "This information should be ignored by any other NeuroML Level 3 compliant application");
                rootElement.addContent("\n\n");
                XMLEncoder xmlEncoder = null;
                ByteArrayOutputStream fos = null;

                /* -- Writing Regions info -- */
                fos = new ByteArrayOutputStream();
                xmlEncoder = new XMLEncoder(fos);
                xmlEncoder.flush();
                xmlEncoder.writeObject(project.basicProjectInfo);
                xmlEncoder.writeObject(project.regionsInfo);
                xmlEncoder.writeObject(project.cellGroupsInfo);
                xmlEncoder.writeObject(project.elecInputInfo);
                xmlEncoder.writeObject(project.morphNetworkConnectionsInfo);
                xmlEncoder.writeObject(project.simPlotInfo);
                xmlEncoder.writeObject(project.simConfigInfo);
                xmlEncoder.writeObject(project.neuronSettings);
                xmlEncoder.writeObject(project.genesisSettings);
                xmlEncoder.close();            
                SimpleXMLElement annotation = new SimpleXMLElement(MetadataConstants.PREFIX + ":"+ MetadataConstants.ANNOTATION_ELEMENT);  
                String content =  fos.toString();
                content = content.substring(content.indexOf("?>")+2, content.length()); //the XML version has to be removed and to be added later in the NetworkMLReader
                String indent = "    ";
                annotation.addContent("\n"+indent); // to make it more readable...
                annotation.addContent("\n"+content);
                annotation.addContent("\n"+indent); // to make it more readable... 
                rootElement.addChildElement(annotation);
                rootElement.addContent("\n\n");

            }          
           
            
    //The cell types present in the network
         
            rootElement.addContent("\n\n");
            
            SimpleXMLElement cellsElement = new SimpleXMLElement("cells");
            
            int i = -1;
            ArrayList<String> ct = new ArrayList<String>();

            for (int j = 0; j < cellGroupNamesToInc.size(); j++)
            {
                String cg = cellGroupNamesToInc.get(j);

                if (!ct.contains(project.cellGroupsInfo.getCellType(cg)))
                {
                    i++;
                    ct.add(project.cellGroupsInfo.getCellType(cg));
                    Cell cell = project.cellManager.getCell(ct.get(i));
                    SimpleXMLElement element = MorphMLConverter.getCellXMLElement(cell, project, 
                        NeuroMLLevel.NEUROML_LEVEL_3, NeuroMLVersion.NEUROML_VERSION_1);
                    cellsElement.addChildElement(element);                    
                }
                
                      
            }
            
            rootElement.addChildElement(cellsElement);

            rootElement.addContent("\n\n");
            
    //The biophysical mechanisms present in the network
            
            SimpleXMLElement cmechsElement = new SimpleXMLElement("channels");
            String chosenUnits = null;

            //cmechsElement.addAttribute(new SimpleXMLAttribute(ChannelMLConstants.UNIT_SCHEME, ChannelMLConstants.PHYSIOLOGICAL_UNITS));

            boolean addChan = false; //flag to avoid repetitions and unuseful checks
            ArrayList<String> allm = project.cellMechanismInfo.getAllCellMechanismNames();

            Vector<String> cellMechs = new Vector<String>();
            Vector<String> synCellMechs = new Vector<String>();
            Vector<String> ionConcMechs = new Vector<String>();
            
            //loop over all the existing cell mechanisms
            //to find all the cell mechanisms in the generated cell groups
            for (int j = 0; j < allm.size(); j++)
            {                
                String m = allm.get(j); 
                addChan = false;
                
                //add the synapses that are used in the generated connections
                Iterator<String> connsNames = project.generatedNetworkConnections.getNamesNetConnsIter();             
                
                while (connsNames.hasNext() && (addChan==false))
                {
                    String netConnName = connsNames.next();
                    
                    if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                    {
                        Vector<SynapticProperties> synPropsM = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                        for (int k = 0; k < synPropsM.size(); k++) {
                            SynapticProperties synapticProperties = synPropsM.elementAt(k);
                            if (synapticProperties.getSynapseType().equals(m) && !synCellMechs.contains(m)) {
                                synCellMechs.add(m);
                                addChan = true;
                            }                  
                        }
                    }
                    
                    if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                    {
                        Vector<SynapticProperties> synPropsV = project.volBasedConnsInfo.getSynapseList(netConnName);
                        for (int k = 0; k < synPropsV.size(); k++) {
                            SynapticProperties synapticProperties = synPropsV.elementAt(k);
                            if (synapticProperties.getSynapseType().equals(m) && !synCellMechs.contains(m)) {
                                synCellMechs.add(m);
                                addChan = true;
                            }                           
                        }                            
                    }
                }
                                
                //add the synapses that are used in the stimulations
                Iterator<String> inputsNames = project.generatedElecInputs.getElecInputsItr();
                while (inputsNames.hasNext()&&(addChan==false))
                {
                    ElectricalInput ei  = project.elecInputInfo.getStim(inputsNames.next()).getElectricalInput();
                    if (ei.getType().equals("RandomSpikeTrainExt") || ei.getType().equals("RandomSpikeTrain"))
                    {
                        RandomSpikeTrain rst = (RandomSpikeTrain)ei;
                        if (rst.getSynapseType().equals(m)
                                && !synCellMechs.contains(m))
                        {                            
                            synCellMechs.add(m);
                            addChan = true;
                        }                        
                    }
                }
                
                //add channels that are used in the generated cell groups
                i=0;
                while ((i<cellGroupNamesToInc.size())&&(addChan==false))
                {
                    Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroupNamesToInc.get(i)));
                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(m);

                    if ( cell.getAllChanMechNames(true).contains(m)
                            && !cellMechs.contains(m)
                            && cm.isChannelMechanism())
                    {
                        cellMechs.add(m);
                        addChan = true;
                    }
                    if ( cell.getAllChanMechNames(true).contains(m)
                            && !ionConcMechs.contains(m)
                            && cm.isIonConcMechanism())
                    {
                        ionConcMechs.add(m);
                        addChan = true;
                    }
                    i++;
                 }
            }
            
            //System.out.println("synCellMechs "+synCellMechs.toString());
            
            //add the channelML descriptions for all the cell mechanisms selected
            LinkedList<SimpleXMLElement> channels = new LinkedList<SimpleXMLElement>();

            for(String syn: synCellMechs)
            {
                cellMechs.add(syn);                //for some reason the synapses are stored before the channels and NeuroML requires the other way aorund
            }
            for(String ic: ionConcMechs)
            {
                cellMechs.add(ic);
            }

            logger.logComment("cellMechs: "+cellMechs);
            
            for(String cellMech: cellMechs)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cellMech);                

                if ((cm instanceof ChannelMLCellMechanism))
                {
                    ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;

                    String units = cmlCm.getXMLDoc().getValueByXPath(ChannelMLConstants.getUnitsXPath());

                    if (chosenUnits==null)
                    {
                        chosenUnits = units;
                    }
                    else if (!chosenUnits.equals(units))
                    {
                        throw new NeuroMLException("Unfortunately it's not yet possible to export to a single Level 3 file when channels and\n" +
                            "synapses have a mixture of SI & Physiological Units...\n"
                            + "Try exporting as individual MorphML, ChannelML, etc. files\n\n" +
                            cmlCm.getInstanceName()+" has "+ units+"\n");

                    }


                    SimpleXMLEntity[] elements = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getPreV1_7_3IonsXPath());

                    for (int j1 = 0; j1 < elements.length; j1++)
                    {
                        SimpleXMLElement el = (SimpleXMLElement)elements[j1];
                        if(!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                            el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                        channels.addFirst(el);
                    }

                    elements = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getIonConcTypeXPath());

                    for (int j1 = 0; j1 < elements.length; j1++)
                    {
                        SimpleXMLElement el = (SimpleXMLElement)elements[j1];
                        if(!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                            el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                        channels.addLast(el);
                    }

                    elements = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getChannelTypeXPath());

                    for (int j1 = 0; j1 < elements.length; j1++)
                    {
                        SimpleXMLElement el = (SimpleXMLElement)elements[j1];
                        if(!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                            el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                        channels.addLast(el);
                    }
                    
                    elements = cmlCm.getXMLDoc().getXMLEntities(ChannelMLConstants.getSynapseTypeXPath());                    
                    
                    for (int j2 = 0; j2 < elements.length; j2++)
                    {
                        SimpleXMLElement el = (SimpleXMLElement)elements[j2];
                        if(!el.hasAttributeValue(NeuroMLConstants.XML_NS))
                            el.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XML_NS, ChannelMLConstants.NAMESPACE_URI));
                        channels.addLast(el);
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

            for (int j3 = 0; j3 < channels.size(); j3++) {
                cmechsElement.addChildElement(channels.get(j3));                
            }
            
//            ArrayList<SimpleXMLAttribute> namesSpaces = cmechsElement.getAttributes();
//            for (int j2 = 0; j2 < namesSpaces.size(); j2++)
//            {
//                namesSpaces.get(j2).;
//            }

            cmechsElement.addAttribute(new SimpleXMLAttribute(ChannelMLConstants.UNIT_SCHEME, chosenUnits));
            rootElement.addChildElement(cmechsElement);



            rootElement.addContent("\n\n");
            

            if (project.generatedCellPositions.getNumberInAllCellGroups()>0)
            {
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
            
            throw new NeuroMLException("Problem creating NeuroML file: "  + neuroMLFile.getAbsolutePath()+"\n"+ex.getMessage(), ex);
        }
        
    }

    public static void generateSedML(Project project,
                                     File sedMLFile,
                                     SimConfig simConfig,
                                     NeuroMLVersion version) throws IOException
    {
        SimpleXMLDocument sedMlDoc = new SimpleXMLDocument();


        SimpleXMLElement root = new SimpleXMLElement("sedML");

        root.addNamespace(new SimpleXMLNamespace("", "http://sed-ml.org/"));


        root.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                            NeuroMLConstants.XSI_URI));

        root.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                 "http://sed-ml.org/   ../../../NeuroML2/Schemas/SED-ML/sed-ml-L1-V1.xsd"));



        root.addAttribute("version", "1");
        root.addAttribute("level", "1");

        sedMlDoc.addRootElement(root);

        SimpleXMLElement listOfSimulations = new SimpleXMLElement("listOfSimulations");
        SimpleXMLElement listOfModels = new SimpleXMLElement("listOfModels");
        SimpleXMLElement listOfTasks = new SimpleXMLElement("listOfTasks");
        SimpleXMLElement listOfDataGenerators = new SimpleXMLElement("listOfDataGenerators");
        SimpleXMLElement listOfOutputs = new SimpleXMLElement("listOfOutputs");


        root.addContent("\n    ");
        root.addChildElement(listOfSimulations);
        root.addContent("\n\n    ");
        root.addChildElement(listOfModels);
        root.addContent("\n\n    ");
        root.addChildElement(listOfTasks);
        root.addContent("\n\n    ");
        root.addChildElement(listOfDataGenerators);
        root.addContent("\n\n    ");
        root.addChildElement(listOfOutputs);
        root.addContent("\n\n");



        SimpleXMLElement simulation = new SimpleXMLElement("uniformTimeCourse");
        simulation.addAttribute("id", "utc1");
        simulation.addAttribute("initialTime", "0");
        simulation.addAttribute("outputStartTime", "0");
        simulation.addAttribute("outputEndTime", simConfig.getSimDuration()+"");
        int numPoints = (int)(simConfig.getSimDuration()/project.simulationParameters.getDt());
        simulation.addAttribute("numberOfPoints", numPoints+"");
        
        
        SimpleXMLElement algorithm = new SimpleXMLElement("algorithm");
        algorithm.addAttribute("kisaoID", "KISAO:0000030");

        simulation.addContent("\n            ");
        simulation.addChildElement(algorithm);
        simulation.addContent("\n        ");

        listOfSimulations.addContent("\n        ");
        listOfSimulations.addChildElement(simulation);
        listOfSimulations.addContent("\n   ");
        


        SimpleXMLElement model = new SimpleXMLElement("model");

        String ver = "urn:sedml:language:neuroml";

        if (version.equals(NeuroMLVersion.NEUROML_VERSION_1))
            ver=ver+"version-1_8_1.level-3";
        else if (version.equals(NeuroMLVersion.NEUROML_VERSION_2_ALPHA))
            ver=ver+"version-2alpha";
        else if (version.equals(NeuroMLVersion.NEUROML_VERSION_2_BETA))
            ver=ver+"version-2beta";

        model.addAttribute("language", ver);

        String sourceFile = null;
        if (version.isVersion1())
        {
            sourceFile = "Generated.net.xml";
        }
        else if(version.isVersion2())
        {
            sourceFile = project.getProjectName() + ProjectStructure.getNeuroMLFileExtension();
        }

        model.addAttribute("source", sourceFile);


        model.addAttribute("id", project.getProjectName());

        listOfModels.addContent("\n        ");
        listOfModels.addChildElement(model);
        listOfModels.addContent("\n    ");


        SimpleXMLElement task = new SimpleXMLElement("task");
        task.addAttribute("simulationReference", project.simulationParameters.getReference());
        String mainTaskRef = "RUN_"+project.simulationParameters.getReference();
        task.addAttribute("id", mainTaskRef);
        task.addAttribute("modelReference", project.getProjectName());

        listOfTasks.addContent("\n        ");
        listOfTasks.addChildElement(task);
        listOfTasks.addContent("\n    ");

        SimpleXMLElement dg = new SimpleXMLElement("dataGenerator");
        dg.addAttribute("id", "time");
        dg.addContent("\n            ");
        SimpleXMLElement lov = new SimpleXMLElement("listOfVariables");
        dg.addChildElement(lov);
        SimpleXMLElement var = new SimpleXMLElement("variable");
        lov.addContent("\n                ");
        lov.addChildElement(var);
        lov.addContent("\n            ");
        dg.addContent("\n            ");
        String time_id = "var_time_0";
        var.addAttribute("id", time_id);
        var.addAttribute("taskReference", mainTaskRef);
        var.addAttribute("symbol", "urn:sedml:symbol:time");
        
        SimpleXMLElement math = new SimpleXMLElement("math");
        dg.addChildElement(math);
        math.addAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
        SimpleXMLElement ci = new SimpleXMLElement("ci");
        ci.addContent( time_id );
        math.addContent("\n                ");
        math.addChildElement(ci);
        math.addContent("\n            ");
        dg.addContent("\n        ");




        listOfDataGenerators.addContent("\n        ");
        listOfDataGenerators.addChildElement(dg);
        listOfDataGenerators.addContent("\n    ");


        for(String plot: simConfig.getPlots())
        {
            SimPlot sp = project.simPlotInfo.getSimPlot(plot);
            if (simConfig.getCellGroups().contains(sp.getCellGroup()))
            {
                dg = new SimpleXMLElement("dataGenerator");
                dg.addAttribute("id", sp.getPlotReference());

                listOfDataGenerators.addContent("\n        ");
                listOfDataGenerators.addChildElement(dg);
                listOfDataGenerators.addContent("\n    ");

                dg.addContent("\n            ");
                lov = new SimpleXMLElement("listOfVariables");
                dg.addChildElement(lov);
                var = new SimpleXMLElement("variable");
                lov.addContent("\n                ");
                lov.addChildElement(var);
                lov.addContent("\n            ");
                dg.addContent("\n            ");

                String varName = sp.getValuePlotted();
                // Quick & dirty substitution...
                if (varName.equals(SimPlot.VOLTAGE))
                    varName = "v";
                varName = varName.replaceAll(":", "_");
                var.addAttribute("id", varName);
                var.addAttribute("taskReference", mainTaskRef);
                var.addAttribute("target", "/neuroml/populations/population[@name='"+sp.getCellGroup()+"']/instances/instance[@id='"+sp.getCellNumber()
                    +"']/segments/segment[@id='"+sp.getSegmentId()+"']/"+varName);

                math = new SimpleXMLElement("math");
                dg.addChildElement(math);
                math.addAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
                ci = new SimpleXMLElement("ci");
                ci.addContent( varName );
                math.addContent("\n                ");
                math.addChildElement(ci);
                math.addContent("\n            ");
                dg.addContent("\n        ");
            }

        }



        String contents = sedMlDoc.getXMLString("  ", false);

        GeneralUtils.writeShortFile(sedMLFile, contents);



    }
    


    public static File saveNetworkStructureHDF5(Project project,
                                       File neuroMLFile,
                                       String simConfig,
                                       String units) throws Hdf5Exception
    {
            return NetworkMLWriter.createNetworkMLH5file(neuroMLFile, project, project.simConfigInfo.getSimConfig(simConfig), units);
    
    }

    public static File saveNetworkStructureXML(Project project,
                                       File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments,
                                       String simConfig,
                                       String units) throws NeuroMLException
    {
        return NeuroMLFileManager.saveNetworkStructureXML(project,
                                       neuroMLFile,
                                       zipped,
                                       extraComments,
                                       simConfig,
                                       units,
                                       NeuroMLVersion.NEUROML_VERSION_1);
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

        if (cgNames.isEmpty())
        {
            report.addTaggedElement("No Cell Groups in project", "font color=\"red\"");
        }
        
        HashMap<Region, Integer> regTotals = new HashMap<Region, Integer>();

        for (String cellGroup: cgNames)
        {
            int num = activeProject.generatedCellPositions.getNumberInCellGroup(cellGroup);
            Region region = activeProject.regionsInfo.getRegionObject(activeProject.cellGroupsInfo.getRegionName(cellGroup));
            String cellType = activeProject.cellGroupsInfo.getCellType(cellGroup);
            Cell cell = activeProject.cellManager.getCell(cellType);

            Color colour = activeProject.cellGroupsInfo.getColourOfCellGroup(cellGroup);
            String hexString = "#"+Integer.toHexString( colour.getRGB() & 0xFFFFFF ).toUpperCase();

            float regionVol = (float)region.getVolume();
            
            if (!regTotals.containsKey(region))
            {
                regTotals.put(region, 0);
            }

            if (num>0)
            {

                report.addTaggedElement("<b>Cell Group: "+cellGroup+"</b>", "font color=\""+hexString+"\"");
                report.addTaggedElement("Number of cells of type: "+cellType+" in Cell Group: "+num, "p");
                regTotals.put(region, regTotals.get(region)+num);
                
                report.addTaggedElement("Region: <b>"+region+"</b><br>"
                                        +"Region volume: "+ regionVol + " \u03bcm\u00b3 ("+(float)(region.getVolume()*1e-9)+" mm\u00b3)", "p");

                float numPerMm3 = (float) (num / (region.getVolume() * 1e-9f));

                report.addTaggedElement("Number of cells of this type per mm\u00b3 in this region: <b>" + numPerMm3+"</b>", "p");

                float cellVol = CellTopologyHelper.getVolume(cell, false);

                report.addTaggedElement("Volume of single cell: <b>" + cellVol + " \u03bcm\u00b3</b><br><b>"
                                        + num + "</b> cells fill <b>" + (cellVol * num * 100 / regionVol) + "%</b> of volume of region", "p");

                float somaVol = CellTopologyHelper.getVolume(cell, true);

                report.addTaggedElement("Total volume of soma of cell: <b>" + somaVol + " \u03bcm\u00b3</b><br><b>"
                                        + num + "</b> cell somas fill <b>" + (somaVol * num * 100 / regionVol) + "%</b> of volume of region",
                                        "p");
                
                report.addBreak();
            }

        }
        report.addBreak();
        report.addBreak();
/*
        for(Region region: regTotals.keySet())
        {

            report.addTaggedElement("Region: <b>"+region+"</b><br>"
                                    +"Region volume: "+ (float)region.getVolume()+ " \u03bcm\u00b3 ("+(float)(region.getVolume()*1e-9)+" mm\u00b3)", "p");

            float numPerMm3 = (float) (regTotals.get(region) / (region.getVolume() * 1e-9f));

            report.addTaggedElement("Total number of cells per mm\u00b3 in this region: <b>" + numPerMm3+"</b>", "p");
        }*/

        if (html) return report.toHtmlString();
        return report.toString();

    }





    public String getValidityReport(boolean html)
    {
        return getValidityReport(html, false, false, false, null, false, false, false);
    }

    public String getValidityReport(boolean html, 
                                    boolean ignoreNoCellInput,
                                    boolean ignoreNoCells,
                                    boolean ignoreCellAtOrigin,
                                    ArrayList<String> cellsToIgnore,
                                    boolean ignoreDisconnectedSegments,
                                    boolean ignoreNotInSimConfigOrCellGroup,
                                    boolean checkBioBounds)
    {
        boolean verbose = false;
        
        File schemaFile = GeneralProperties.getNeuroMLSchemaFile();

        SimpleHtmlDoc report = new SimpleHtmlDoc();

        report.addTaggedElement("Validating the project: "+ this.activeProject.getProjectFile(), "h3");
        
        String latestNml = GeneralProperties.getLatestNeuroMLVersionNumber();
        
        if (ProjectStructure.compareVersions(latestNml, GeneralProperties.getNeuroMLVersionNumber())>0)
        {
            report.addTaggedElement("<b>Note: validating using NeuroML "+GeneralProperties.getNeuroMLVersionString()
                +", but the latest version available is v"+latestNml+"</b><br/></br/>" +
                "This can be corrected via Settings -> General Properties & Project Defaults -> NeuroML version", "font color=\""+ValidityStatus.VALIDATION_COLOUR_WARN+"\"");
        }

        report.addTaggedElement("Validating cells in project", "p");

        String overallValidity = ValidityStatus.VALIDATION_OK;

        ArrayList cellNames = activeProject.cellManager.getAllCellTypeNames();
        cellNames = (ArrayList)GeneralUtils.reorderAlphabetically(cellNames, true);

        if (cellNames.isEmpty() && !ignoreNoCells)
        {
            overallValidity = ValidityStatus.VALIDATION_ERROR;
            report.addTaggedElement("No Cell Types in project", "font color=\""+ValidityStatus.VALIDATION_COLOUR_ERROR+"\"");
        }

        for (Object ctName: cellNames)
        {
            String cellTypeName = (String)ctName;
            
            if(cellsToIgnore!=null && cellsToIgnore.contains(cellTypeName))
            {
                report.addTaggedElement("Ignoring cell: <b>"+cellTypeName+"</b>", "p");
            }
            else
            {
                report.addTaggedElement("Checking cell: <b>"+cellTypeName+"</b>", "p");

                Cell cell = activeProject.cellManager.getCell(cellTypeName);

                ValidityStatus status = CellTopologyHelper.getValidityStatus(cell, ignoreCellAtOrigin, ignoreDisconnectedSegments);

                overallValidity = ValidityStatus.combineValidities(overallValidity, status.getValidity());

                String format = "font color=\""+status.getColour()+"\"";

                String message = status.getMessage();

                if (html)
                {
                    message = GeneralUtils.replaceAllTokens(message, "\n", "<br>");
                }



                report.addTaggedElement(message, format);
                report.addBreak();

                ValidityStatus bioStatus = CellTopologyHelper.getBiophysicalValidityStatus(cell, this.activeProject, checkBioBounds);


                String bioFormat = "font color=\""+bioStatus.getColour()+"\"";

                String thisStatus = bioStatus.getValidity();

                if (!ignoreNotInSimConfigOrCellGroup && !activeProject.cellGroupsInfo.getUsedCellTypes().contains(cell.getInstanceName()))
                {
                    report.addTaggedElement("Note: Cell Type: "+cell.getInstanceName()+" not currently used in any Cell Group",
                            "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
                    report.addBreak();
                    report.addBreak();

                    if (bioStatus.isError())
                    {
                        report.addTaggedElement("Note: Biological valididity status below downgraded from error to warning as cell isn't used in project!\"",
                            "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "");

                        report.addBreak();
                        report.addBreak();
                        thisStatus = ValidityStatus.VALIDATION_WARN;
                        bioFormat = "font color=\""+ValidityStatus.VALIDATION_COLOUR_WARN+"\"";
                    }
                }


                overallValidity = ValidityStatus.combineValidities(overallValidity, thisStatus);

                message = bioStatus.getMessage();

                if (html)
                {
                    message = GeneralUtils.replaceAllTokens(message, "\n", "<br>");
                }
                report.addTaggedElement(message, bioFormat);
            
            }
        }

        report.addBreak();
        report.addBreak();

        report.addTaggedElement("Validating Cell Mechanisms (using NeuroML version "+GeneralProperties.getNeuroMLVersionNumber()+")", "p");

        ArrayList cellMechNames = this.activeProject.cellMechanismInfo.getAllCellMechanismNames();

        cellMechNames = (ArrayList)GeneralUtils.reorderAlphabetically(cellMechNames, true);

        String soleUnits = null; // Should only be one units scheme used...

        for (Object nextCellMech: cellMechNames)
        {
            String cellMechValidity = ValidityStatus.VALIDATION_OK;
            String nextCellMechName = (String)nextCellMech;
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

                    ArrayList<SimulatorMapping> simMappings = cmlCm.getSimMappings();

                    for (SimulatorMapping simMapping : simMappings)
                    {
                        File f = simMapping.getXslFileObject(activeProject, next.getInstanceName());

                        if (f != null && f.exists())
                        {
                            if (verbose) report.addTaggedElement("Implementation file: " + f.getAbsolutePath() + " found", "p");
                            
                            String prefNeuroML = GeneralProperties.getLatestNeuroMLVersionString();

                            if(f.getName().indexOf(prefNeuroML)<=0)
                            {
                                report.addTaggedElement("Warning, implementation file: "+f.getAbsolutePath()+" does not seem to be of your preferred " +
                                        "NeuroML format: "+prefNeuroML+". This can be rectified by the Update mapping files button on the Cell Mechanisms tab",
                                                        "font color=\""+ValidityStatus.VALIDATION_COLOUR_WARN+"\"", "p");

                                cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_WARN);
                            }
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
                    String units = cmlCm.getValue(ChannelMLConstants.getUnitsXPath());

                    if (soleUnits==null)
                        soleUnits = units;

                    if (!units.equals(soleUnits))
                    {
                        report.addTaggedElement("Warning, there is a mixture of SI and Physiological Units used in the ChannelML files ("+cmlCm.getInstanceName()+" has units "+units+"). This will prevent the generated"
                            + " project being exported as a single Level 3 NeuroML file.",
                                                "font color=\""+ValidityStatus.VALIDATION_COLOUR_WARN+"\"", "p");

                        cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_WARN);
                    }

                    
                    if (cmlCm.isChannelMechanism())
                    {
                        status = cmlCm.getValue(ChannelMLConstants.getChannelStatusValueXPath());
                    }
                    else if (cmlCm.isSynapticMechanism())
                    {
                        status = cmlCm.getValue(ChannelMLConstants.getSynapseStatusValueXPath());
                    }
                    else if (cmlCm.isGapJunctionMechanism())
                    {
                        status = cmlCm.getValue(ChannelMLConstants.getGapJunctionStatusValueXPath());
                    }
                    else if (cmlCm.isIonConcMechanism())
                    {
                        status = cmlCm.getValue(ChannelMLConstants.getIonConcStatusValueXPath());
                    }

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

                        Source xmlFileSource = new StreamSource(cmlCm.getXMLFile(activeProject));

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
                        report.addTaggedElement("Problem validating ChannelML file: "+ cmlCm.getXMLFile()
                                +" against XSD file: " +schemaFile.getAbsolutePath()+".",
                                "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");

                        cellMechValidity = ValidityStatus.combineValidities(cellMechValidity, ValidityStatus.VALIDATION_ERROR);
                       
                    }
                    
                }
                catch (XMLMechanismException ex)
                {
                    report.addTaggedElement("Error instantiating Channel mechanism: " + cmlCm.getInstanceName()
                                            +", file: "+ cmlCm.getXMLFile(),
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

            if ( (!(allCellGroupsWithStims.contains(cellGroup)||allPostSynCellGroups.contains(cellGroup))) &&
                  !ignoreNoCellInput)
            {
                report.addTaggedElement("Cell Group: " + cellGroup + " has neither an electrical stimulation nor is "
                                        +"postsynaptically connected to another Cell Group. Unless the cells are spontaneously active,"
                                        +" nothing much will happen to those cells during a simulation.",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");

                report.addBreak();

                //overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);

            }
            if (!ignoreNotInSimConfigOrCellGroup && !activeProject.simConfigInfo.getAllUsedCellGroups().contains(cellGroup))
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
            
            if (stim.getElectricalInput() instanceof RandomSpikeTrain || stim.getElectricalInput() instanceof RandomSpikeTrainExt)
            {
                String syn = null;
                if (stim.getElectricalInput() instanceof RandomSpikeTrain)
                    syn = ((RandomSpikeTrain)stim.getElectricalInput()).getSynapseType();
                if (stim.getElectricalInput() instanceof RandomSpikeTrainExt)
                    syn = ((RandomSpikeTrainExt)stim.getElectricalInput()).getSynapseType();
                if(activeProject.cellMechanismInfo.getCellMechanism(syn) == null ||
                   !activeProject.cellMechanismInfo.getCellMechanism(syn).isSynapticMechanism())
                {
                    report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies synaptic mechanism: " + syn +
                                            ", but there isn't any synaptic mechanism by that name in the project!",
                                            "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");
                    report.addBreak();

                    overallValidity = ValidityStatus.VALIDATION_ERROR;
                }
            }
            
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
                if (stim.getSegChooser() instanceof GroupDistributedSegments)
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
                    
                
                if (!ignoreNotInSimConfigOrCellGroup && !activeProject.simConfigInfo.getAllUsedElectInputs().contains(stim.getReference()))
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
            
            
            if (!ignoreNotInSimConfigOrCellGroup && !activeProject.simConfigInfo.getAllUsedPlots().contains(plot.getPlotReference()))
            {
                report.addBreak();
                report.addTaggedElement("Note: Plot: "+plot.getPlotReference()+" is not currently included in any Simulation Configuration",
                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_INFO + "\"");
            }
            
            if (!plot.getSegmentId().equals("*"))
            {
                String cellType = activeProject.cellGroupsInfo.getCellType(plot.getCellGroup());
                Cell cell = activeProject.cellManager.getCell(cellType);
                
                try
                {
                    int id = Integer.parseInt(plot.getSegmentId());
                    cell.getSegmentWithId(id).getSegmentName();
                }
                catch(Exception e) // Null pointer or parse exception
                {
                    report.addTaggedElement("Error, Plot: " + plot.getPlotReference() + " specifies segment Id: " + plot.getSegmentId() +
                                        ", but this doesn't refer to a valid segment in this cell!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");
                    report.addBreak();

                    overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);
                    
                }
                    
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
        else if (this.activeProject.simulationParameters.getTemperature() >=38)
        {
            report.addTaggedElement("Warning, simulation temperature above 38 celsius!",
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
            projectOverallValidity = ValidityStatus.getValidStatus(ValidityStatus.PROJECT_IS_VALID);
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
        if (!html) return report.toString();
        
        return report.toHtmlString();
    }

        /**
     * Displays information on the validity of the cells in the project and other
     * project settings
     */
    public void doValidate(boolean html)
    {
        String report = getValidityReport(html);


        SimpleViewer.showString(report, "Validity status", 12, false, html);
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

    public Project copyProject(File oldProjFile, File newProjFile) throws Exception
    {
        File newProjDir = newProjFile.getParentFile();


        if (newProjDir.exists() && newProjDir.listFiles().length>0)
        {
            throw new Exception("Cannot copy project to: "+ newProjDir+"; directory not empty!");
            
        }

        if (!newProjDir.exists())
            newProjDir.mkdir();


        try
        {
            File tempProjectFile = GeneralUtils.copyFileIntoDir(oldProjFile, newProjDir);


            tempProjectFile.renameTo(newProjFile);


            GeneralUtils.copyDirIntoDir(ProjectStructure.getMorphologiesDir(oldProjFile.getParentFile()),
                                        ProjectStructure.getMorphologiesDir(newProjDir), false, true);

        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem creating copy of the project", ex, null);
            return null;
        }
        try
        {
            File impMorphDir = ProjectStructure.getImportedMorphologiesDir(oldProjFile.getParentFile(), false);

            if (impMorphDir!=null)
            {
                GeneralUtils.copyDirIntoDir(impMorphDir,
                                            ProjectStructure.getImportedMorphologiesDir(newProjDir, true),
                    false, true);
            }
        }
        catch (IOException ex)
        {
            logger.logError("Problem copying DirForImportedMorphologies", ex);
            // continuing...
        }

        File cellMechDir = ProjectStructure.getCellMechanismDir(oldProjFile.getParentFile(), false);
        if (cellMechDir!=null)
        {
            try
            {
                GeneralUtils.copyDirIntoDir(cellMechDir,
                                            ProjectStructure.getCellMechanismDir(newProjDir), true, true);
            }
            catch (IOException ex)
            {
                logger.logError("Problem copying DirFormportedCellProcesses", ex);
                // continuing...
            }
        }

        File oldCellProcDir = ProjectStructure.getCellProcessesDir(oldProjFile.getParentFile(), false);
        if (oldCellProcDir!=null)
        {
            try
            {
                GeneralUtils.copyDirIntoDir(oldCellProcDir,
                                            ProjectStructure.getCellProcessesDir(newProjDir, true), true, true);

            }
            catch (IOException ex)
            {
                logger.logError("Problem copying DirFormportedCellProcesses", ex);
                // continuing...
            }
        }


        File pythonDir = ProjectStructure.getPythonScriptsDir(oldProjFile.getParentFile(), false);
        if (pythonDir!=null)
        {
            try
            {
                GeneralUtils.copyDirIntoDir(pythonDir,
                                            ProjectStructure.getPythonScriptsDir(newProjDir, true), true, true);
            }
            catch (IOException ex)
            {
                logger.logError("Problem copying python dirs", ex);
                // continuing...
            }
        }



        Project p = loadProject(newProjFile.getAbsoluteFile());

        p.markProjectAsEdited();
        String newProjectName = newProjFile.getName();
        if (newProjectName.endsWith(ProjectStructure.getNewProjectFileExtension()))
            newProjectName = newProjectName.substring(0,newProjectName.length()-ProjectStructure.getNewProjectFileExtension().length());
        p.setProjectName(newProjectName);

        p.saveProject();

        activeProject = p;

        return activeProject;

    }


    public Project loadProject(File projFile) throws ProjectFileParsingException
    {
        activeProject = Project.loadProject(projFile, projEventListener);
        return activeProject;
    }


    public SimulationData reloadSimulation(String simRef)
    {
        File simDataFile = ProjectStructure.getDirForSimFiles(simRef, activeProject);
        SimulationData simData = null;
        try
        {
            simData = new SimulationData(simDataFile, true);
            simData.initialise();

        }
        catch (SimulationDataException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Error getting the simulation info from "+ simDataFile, ex1, null);
            return null;
        }


        activeProject.generatedCellPositions.reset();
        try
        {
            activeProject.generatedCellPositions.loadFromFile(simData.getCellPositionsFile());

        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem loading the cell position data from: " +
                                      simData.getCellPositionsFile(), ex2, null);


            activeProject.resetGenerated();
            return null;
        }


        activeProject.generatedNetworkConnections.reset();
        try
        {
            activeProject.generatedNetworkConnections.loadFromFile(simData.getNetConnectionsFile());
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem loading the net connections data from: " +
                                      simData.getNetConnectionsFile(), ex2, null);

            activeProject.resetGenerated();
            return null;
        }


        activeProject.generatedElecInputs.reset();
        try
        {
            activeProject.generatedElecInputs.loadFromFile(simData.getElecInputsFile());

        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem loading the electrical inputs data from: " +
                                      simData.getElecInputsFile(), ex2, null);

            activeProject.resetGenerated();
            return null;
        }


        Iterator cellGroups = activeProject.generatedCellPositions.getNamesGeneratedCellGroups();

        while (cellGroups.hasNext())
        {
            String nextCellGroup = (String) cellGroups.next();
            boolean isCellGroup = activeProject.cellGroupsInfo.isValidCellGroup(nextCellGroup);

            if (!isCellGroup)
            {
                GuiUtils.showErrorMessage(logger, "The Cell Group " + nextCellGroup +
                    ", as recorded in the simulation data is not a valid Cell Group for this project.\n" +
                    "This may be due to the project file being altered (e.g Cell Groups changed) after running the simulation.", null, null);

                activeProject.resetGenerated();
                return null;
            }
        }

        Iterator netConns = activeProject.generatedNetworkConnections.getNamesNetConnsIter();

        while (netConns.hasNext())
        {
            String nextNetConn = (String) netConns.next();
            boolean isNetConn = activeProject.morphNetworkConnectionsInfo.isValidSimpleNetConn(nextNetConn);
            boolean isAAConn = activeProject.volBasedConnsInfo.isValidVolBasedConn(nextNetConn);

            if (!(isNetConn||isAAConn))
            {
                GuiUtils.showErrorMessage(logger, "The Network Connection " + nextNetConn +
                    ", as recorded in the simulation data is not a valid Network Connection for this project.\n" +
                    "This may be due to the project file being altered (e.g Network Connections changed) after running the simulation.", null, null);

                activeProject.resetGenerated();
                return null;
            }
        }
        // No real need to check the inputs, they're just probe positions...

        logger.logComment("Resetting plot/save info...");

        activeProject.generatedPlotSaves.reset();

        return simData;
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

        else if (simConfig.getMpiConf().isParallelOrRemote()
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

        else if ((!(simConfig.getMpiConf().isParallelOrRemote())
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

            if (report.indexOf("Generation interrupted") > 0)
            {
                logger.logComment("It seems the generation of plots was interrupted...");
                currentlyGenerating = false;
                return;
            }

            cellInitialiser = new CellInitialiser(activeProject, this);

            cellInitialiser.setSimConfig(simConfig);

            cellInitialiser.start();


        }

        else if (generatorType.equals(CellInitialiser.myGeneratorType))
        {

            if (report.indexOf("Generation interrupted") > 0)
            {
                logger.logComment("It seems the generation of initial cell settings was interrupted...");
                currentlyGenerating = false;
                return;
            }

            currentlyGenerating = false;
        }
        else
        {
            logger.logComment("Don't know the type of that generation report!!: " + generatorType);
            
            //////////////currentlyGenerating = false;
        }


    }
    
    
    public void majorStepComplete()
    {
            logger.logComment(">>> -----------------");
    };
    

    public void giveUpdate(String update)
    {
            logger.logComment(">>> "+ update);
    };
    
    public static void main(String[] args) throws ProjectFileParsingException, InterruptedException, IOException
    {

        //Project proj = Project.loadProject(new File("nCexamples/Ex1_Simple/Ex1_Simple.ncx"), null);
        Project proj = Project.loadProject(new File("osb/showcase/neuroConstructShowcase/Ex6_CerebellumDemo/Ex6_CerebellumDemo.ncx"), null);

        File neuroMLDir = ProjectStructure.getNeuroMLDir(proj.getProjectMainDirectory());


        ProjectManager pm = new ProjectManager(null,null);
        pm.setCurrentProject(proj);

        String simConfName = SimConfigInfo.DEFAULT_SIM_CONFIG_NAME;

        pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);

        while(pm.isGenerating())
        {
            Thread.sleep(200);
        }
        System.out.println("Num cells generated: "+ proj.generatedCellPositions.getAllPositionRecords().size());


        File sedMlFile = new File(neuroMLDir, "SED.xml");

        generateSedML(proj, sedMlFile, proj.simConfigInfo.getSimConfig(simConfName), NeuroMLVersion.NEUROML_VERSION_2_ALPHA);


        System.out.println("SED-ML doc generated in "+ sedMlFile.getAbsolutePath());
    }


}
