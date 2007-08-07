/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.project;

import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.event.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.genesis.*;
import ucl.physiol.neuroconstruct.neuron.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.utils.xml.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import javax.swing.*;

/**
 * Main class holding references to the important data objects of the project
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class Project implements TableModelListener
{
    private static ClassLogger logger = new ClassLogger("Project");

    public final static int PROJECT_NOT_INITIALISED = 0;
    public final static int PROJECT_NOT_YET_EDITED = 1;
    public final static int PROJECT_SAVED = 2;
    public final static int PROJECT_EDITED_NOT_SAVED = 3;

    /**
     * marker for whether the project has been altered
     */
    private int myStatus = Project.PROJECT_NOT_INITIALISED;

    /**
     * Ref to the file to save it to...
     */
    File currentProjectFile = null;

    // Interface to let the project know if something has been altered...
    ProjectEventListener projectEventListner;

    private String preferredSaveFormat = ProjectStructure.JAVA_XML_FORMAT;
   // private String preferredSaveFormat = ProjectStructure.JAVA_OBJ_FORMAT;

    /**
     * These hold the main info of the project. They will be written to/read from
     * file when saving/loading
     */
    private BasicProjectInfo basicProjectInfo = null;

    public CellGroupsInfo cellGroupsInfo = null;
    public RegionsInfo regionsInfo = null;

    public SimpleNetworkConnectionsInfo morphNetworkConnectionsInfo = null;

    public ArbourConnectionsInfo volBasedConnsInfo = null;

    public SimulationParameters simulationParameters = null;

    public NeuronSettings neuronSettings = null;
    public GenesisSettings genesisSettings = null;

    public Display3DProperties proj3Dproperties = null;

    public CellMechanismInfo cellMechanismInfo = null;

    public SimPlotInfo simPlotInfo = null;

    public ElecInputInfo elecInputInfo = null;

    public SimConfigInfo simConfigInfo = null;

    /**
     * This will be fed with info on the cell morphologies in the projectMorphologies folder
     */
    public CellManager cellManager = null;

    /**
     * Holders for the generated cell positions info, etc.
     */
    public GeneratedCellPositions generatedCellPositions = null;
    public GeneratedNetworkConnections generatedNetworkConnections = null;
    public GeneratedElecInputs generatedElecInputs = null;
    public GeneratedPlotSaves generatedPlotSaves = null;

    /**
     * Generators of the main code for NEURON and GENESIS
     */
    public NeuronFileManager neuronFileManager = null;
    public GenesisFileManager genesisFileManager = null;

    /**
     * Private, so initialisation methods will have to be used...
     */
    private Project()
    {
    }

    /**
     * Gets a new empty project
     */
    public static Project createNewProject(String projectDir,
                                           String projectName,
                                           ProjectEventListener projectEventListner)
    {
        Project proj = new Project();
        proj.currentProjectFile = new File(projectDir, projectName + ProjectStructure.getProjectFileExtension());

        proj.initialiseInternalObjects();

        proj.projectEventListner = projectEventListner;

        proj.myStatus = Project.PROJECT_EDITED_NOT_SAVED;

        proj.basicProjectInfo = new BasicProjectInfo();
        proj.basicProjectInfo.setProjectName(projectName);

        File newProjectSpecificDir = proj.currentProjectFile.getParentFile();

        if (! (newProjectSpecificDir.exists() && newProjectSpecificDir.isDirectory()))
        {
            newProjectSpecificDir.mkdir();
        }

        // make some of the project structure...
        ProjectStructure.getMorphologiesDir(newProjectSpecificDir);
        ProjectStructure.getSimulationsDir(newProjectSpecificDir);

        return proj;
    }

    /*
        public void createSimulationsDir()
        {

            File newProjectSimulationsDir = new File(this.getProjectMainDirectory(),
                                                     GeneralProperties.getDirForSimulations());

            logger.logComment("Creating new dir: "+ newProjectSimulationsDir);

            if (!(newProjectSimulationsDir.isDirectory()) || !newProjectSimulationsDir.exists())
            {
                newProjectSimulationsDir.mkdir();
                File simulationsDirReadme = new File(newProjectSimulationsDir, "README");

                try
                {
                    FileWriter fw = new FileWriter(simulationsDirReadme);
                    fw.write("This is the directory for the simulation data associated with the project\n");
                    fw.close();
                }
                catch (IOException ex)
                {
                    logger.logError("Exception creating readme file...", ex);
                }
            }

        }
     */


    /**
     * Only for testing purposes!!
     */
    public static ProjectEventListener getDummyProjectEventListener()
    {
        return new ProjectEventListener()
        {
            public void tableDataModelUpdated(String tableModelName)
            {
                System.out.println("ProjectEventListener: tableDataModelUpdated: " + tableModelName);
            };
            public void tabUpdated(String tabName)
            {
                System.out.println("ProjectEventListener: tabUpdated: " + tabName);
            };
            public void cellMechanismUpdated()
            {
                System.out.println("ProjectEventListener: cellMechanismUpdated()");
            };

        };
    }

    /**
     * Gets the project object for a specified project file
     */
    public static Project loadProject(File projectFile,
                                      ProjectEventListener projectEventListner) throws ProjectFileParsingException
    {
        boolean userAgreedUpdate = false;


        if (!projectFile.exists())
            throw new ProjectFileParsingException("The project file: "+projectFile.getAbsolutePath()+" does not exist!");
        

        try
        {
            userAgreedUpdate = ProjectFileUpdate.updateProjectFile(projectFile);
        }
        catch (Exception ex3)
        {
            throw new ProjectFileParsingException("Problem while attempting to check the version of the project file",
                                                  ex3);
        }



       // System.exit(-9);

        if (!userAgreedUpdate)
        {
            logger.logComment("User cancelled update...");
            return null;
        }

        Project proj = new Project();

        proj.currentProjectFile = projectFile;
        proj.projectEventListner = projectEventListner;

        XMLDecoder xmlDecoder = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try
        {
            fis = new FileInputStream(projectFile);
            bis = new BufferedInputStream(fis);
            xmlDecoder = new XMLDecoder(bis);
        }
        catch (FileNotFoundException ex)
        {
            throw new ProjectFileParsingException("Problem reading from file: " + projectFile);
        }

        // To get defaults for objects not included in file
        proj.initialiseInternalObjects();

        Object nextReadObject = null;

        logger.logComment("Reading project file: " + projectFile.getAbsolutePath());

        boolean oldCellProcObjectFound = false;

        File dirForCellMechs = ProjectStructure.getCellMechanismDir(proj.getProjectMainDirectory(), false);

        boolean newCellMechDirPresent = (dirForCellMechs != null && dirForCellMechs.exists());

        ComplexConnectionsInfo tempComplexConnectionsInfo = null;

        try
        {
            while ( (nextReadObject = xmlDecoder.readObject()) != null)
            {
                /* --  Reading Basic Info -- */
                if (nextReadObject instanceof BasicProjectInfo)
                {
                    logger.logComment("Found BasicProjectInfo object in project file...");
                    proj.basicProjectInfo = (BasicProjectInfo) nextReadObject;
                }

                /* --  Reading Regions Info -- */
                if (nextReadObject instanceof RegionsInfo)
                {
                    logger.logComment("Found RegionsInfo object in project file...");
                    proj.regionsInfo = (RegionsInfo) nextReadObject;
                    proj.regionsInfo.addTableModelListener(proj);
                }

                /* --  Reading Sim Plot Info -- */
                if (nextReadObject instanceof SimPlotInfo)
                {
                    logger.logComment("Found SimPlotInfo object in project file...");
                    proj.simPlotInfo = (SimPlotInfo) nextReadObject;
                    proj.simPlotInfo.addTableModelListener(proj);
                }

                /* --  Reading Cell Group Info -- */
                if (nextReadObject instanceof CellGroupsInfo)
                {
                    logger.logComment("Found CellGroupsInfo object in project file...");
                    proj.cellGroupsInfo = (CellGroupsInfo) nextReadObject;
                    proj.cellGroupsInfo.addTableModelListener(proj);
                }

                /* --  Reading 3D Info -- */
                if (nextReadObject instanceof Display3DProperties)
                {
                    logger.logComment("Found Project3DProperties object in project file...");
                    proj.proj3Dproperties = (Display3DProperties) nextReadObject;
                }

                /* --  Reading NEURON Info --*/
                if (nextReadObject instanceof NeuronSettings)
                {
                    logger.logComment("Found NeuronSettings object in project file...");
                    proj.neuronSettings = (NeuronSettings) nextReadObject;
                }

                /* --  Reading GENESIS Info --*/
                if (nextReadObject instanceof GenesisSettings)
                {
                    logger.logComment("Found GenesisSettings object in project file...");
                    proj.genesisSettings = (GenesisSettings) nextReadObject;
                }

                /* --  Reading Simulation Info --*/
                if (nextReadObject instanceof SimulationParameters)
                {
                    logger.logComment("Found SimulationParameters object in project file...");
                    proj.simulationParameters = (SimulationParameters) nextReadObject;
                }

                /* --  Reading SimConfigInfo --*/
                if (nextReadObject instanceof SimConfigInfo)
                {
                    logger.logComment("Found SimConfigInfo object in project file...");
                    proj.simConfigInfo = (SimConfigInfo) nextReadObject;
                }

                /* --  Reading ElecInputInfo --*/
                if (nextReadObject instanceof ElecInputInfo)
                {
                    logger.logComment("Found ElecInputInfo object in project file...");
                    proj.elecInputInfo = (ElecInputInfo) nextReadObject;
                }

                /* --  Reading Simple Net Conn Info -- */
                if (nextReadObject instanceof SimpleNetworkConnectionsInfo)
                {
                    logger.logComment("Found SimpleNetworkConnectionsInfo object in project file...");
                    proj.morphNetworkConnectionsInfo = (SimpleNetworkConnectionsInfo) nextReadObject;
                    proj.morphNetworkConnectionsInfo.addTableModelListener(proj);
                }

                /* --  Reading Complex Conn Info -- */
                if (nextReadObject instanceof ComplexConnectionsInfo)
                {
                    logger.logComment("Found ComplexConnectionsInfo object in project file...");
                    tempComplexConnectionsInfo = (ComplexConnectionsInfo) nextReadObject;
                    //tempComplexConnectionsInfo.addTableModelListener(proj);
                }

                /* --  Reading Complex Conn Info -- */
                if (nextReadObject instanceof ArbourConnectionsInfo)
                {
                    logger.logComment("Found ArbourConnectionsInfo object in project file...");
                    proj.volBasedConnsInfo = (ArbourConnectionsInfo) nextReadObject;
                    proj.volBasedConnsInfo.addTableModelListener(proj);
                }

                /* --  Reading Cell Mechanism Info -- */
                if (nextReadObject instanceof CellMechanismInfo) // shouldn't be stored anymore...
                {
                    if (!newCellMechDirPresent)
                    {
                        //System.out.println("----- Found CellMechanismInfo object");
                        logger.logComment("Found CellMechanismInfo object in project file...");
                        oldCellProcObjectFound = true;
                        proj.cellMechanismInfo = (CellMechanismInfo) nextReadObject;
                        proj.cellMechanismInfo.addTableModelListener(proj);
                    }
                    else
                    {
                        //System.out.println("Using new cell mech method");
                    }
                }

            }
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            logger.logComment("Reached end of project file...");
        }
        catch (NoSuchElementException ex)
        {
            logger.logComment("Couldn't find a proper element...");
        }

        if (proj.basicProjectInfo == null)
        {
            logger.logError("Reached end of file without finding BasicProjectInfo");
            throw new ProjectFileParsingException("Problem reading the BasicProjectInfo element in file: "
                                                  + projectFile
                                                  + "\nThis is probably due to the file not being a properly formatted"
                                                  + "\n project file for neuroConstruct version 0.5 or greater.");
        }

        int compVer = ProjectStructure.compareVersions(GeneralProperties.getVersionNumber(),
                                                       proj.basicProjectInfo.getProjectFileVersionNumber());

        //System.out.println("compVer: " + compVer);

        if (compVer < 0)
        {
            int yesNo = JOptionPane.showConfirmDialog(GuiUtils.getMainFrame(),
                "Note, this project has been last saved with neuroConstruct version: v"
                                                      + proj.basicProjectInfo.getProjectFileVersionNumber() +
                                                      ", but this version of the application is: v" +
                                                      GeneralProperties.getVersionNumber()
                                                      + "\nDo you wish to continue?",
                                                      "Confirm load project",
                                                      JOptionPane.YES_NO_CANCEL_OPTION);

            if (yesNo == JOptionPane.NO_OPTION)
            {
                logger.logComment("User cancelled...");
                return null;
            }

        }

        xmlDecoder.close();
        try
        {
            fis.close();
            bis.close();
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Error loading project from file: "+ projectFile, ex, null);
            return null;
        }


        // As the complexConnectionsInfo is deprecated...
        if (tempComplexConnectionsInfo != null)
        {
            proj.morphNetworkConnectionsInfo.stealComplexConns(tempComplexConnectionsInfo);
        }

        if (!oldCellProcObjectFound && newCellMechDirPresent)
        {
            logger.logComment("<><><>  Loading the cell mechanisms...");

            File[] cellMechDirs = dirForCellMechs.listFiles();

            cellMechDirs = GeneralUtils.reorderAlphabetically(cellMechDirs, true);

            for (int i = 0; i < cellMechDirs.length; i++)
            {
                if (cellMechDirs[i].isDirectory() && !GeneralUtils.isVersionControlDir(cellMechDirs[i]))
                {
                    Properties cellMechProps = new Properties();
                    File propsFile = new File(cellMechDirs[i], CellMechanismHelper.PROPERTIES_FILENAME);

                    //System.out.println("propsFile: "+propsFile.getAbsolutePath());

                    try
                    {
                        cellMechProps.loadFromXML(new FileInputStream(propsFile));
                        String implMethod = cellMechProps.getProperty(CellMechanismHelper.PROP_IMPL_METHOD);

                        if (implMethod.equals(CellMechanism.CHANNELML_BASED_CELL_MECHANISM))
                        {
                            ChannelMLCellMechanism cmlcm = new ChannelMLCellMechanism();

                            cmlcm.initPropsFromPropsFile(propsFile);

                            try
                            {
                                cmlcm.initialise(proj, false);
                            }
                            catch (ChannelMLException ex1)
                            {
                                GuiUtils.showErrorMessage(logger,
                                                          "Error creating implementation of Cell Mechanism: " +
                                                          cmlcm.getInstanceName(),
                                                          ex1,
                                                          null);
                            }

                            proj.cellMechanismInfo.addCellMechanism(cmlcm);

                        }
                        else if (implMethod.equals(CellMechanism.ABSTRACTED_CELL_MECHANISM) ||
                                 implMethod.equals(CellMechanism.FILE_BASED_CELL_MECHANISM))
                        {
                            String mechType = cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_TYPE);
                            AbstractedCellMechanism acm = null;

                            File internalPropsFile = new File(cellMechDirs[i],
                                                              CellMechanismHelper.INTERNAL_PROPS_FILENAME);

                            if (implMethod.equals(CellMechanism.FILE_BASED_CELL_MECHANISM))
                            {
                                FileBasedMembraneMechanism fmm = new FileBasedMembraneMechanism();

                                fmm.specifyMechanismType(cellMechProps.getProperty(CellMechanismHelper.
                                    PROP_CELL_MECH_TYPE));

                                acm = fmm;
                            }
                            else
                            {
                                if (mechType.equals(CellMechanism.CHANNEL_MECHANISM))
                                {
                                    logger.logComment("Size of file: " + internalPropsFile.length());

                                    if (internalPropsFile.length() > 4000) // very hack like way to distinguish a pass mech from a big hh mech file
                                    {
                                        acm = new HHMembraneMechanism();
                                    }
                                    else
                                    {
                                        acm = new PassiveMembraneMechanism();
                                    }
                                }
                                else if (mechType.equals(CellMechanism.SYNAPTIC_MECHANISM))
                                {
                                    acm = new Exp2SynMechanism(); // Not always, but usually...
                                }
                            }

                            acm.setInstanceName(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_NAME));
                            acm.setDescription(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_DESCRIPTION));
                            acm.setMechanismModel(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_MODEL));
                            acm.setMechanismType(cellMechProps.getProperty(CellMechanismHelper.PROP_CELL_MECH_TYPE));
                            acm.setDefaultInstanceName(cellMechProps.getProperty(CellMechanismHelper.
                                PROP_CELL_MECH_DEFAULT_NAME));

                            acm.setPlotInfoFile(cellMechProps.getProperty(CellMechanismHelper.PROP_PLOT_INFO_FILE));

                            //System.out.println("Int props file: "+internalPropsFile.getAbsolutePath());

                            try
                            {
                                XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(
                                    internalPropsFile)));

                                Object nextParams = null;

                                try
                                {
                                    while ( (nextParams = d.readObject()) != null)
                                    {
                                        InternalPhysicalParameter ipp = (InternalPhysicalParameter) nextParams;
                                        //System.out.println("Param found: "+ipp);
                                        boolean success = acm.setParameter(ipp.getParameterName(), ipp.getValue());

                                        if (!success)
                                        {
                                            acm.addNewParameter(ipp.getParameterName(), ipp.getParameterDescription(),
                                                                ipp.getDefaultValue(), ipp.getUnits());

                                            success = acm.setParameter(ipp.getParameterName(), ipp.getValue());

                                            logger.logComment("Tried adding new param & setting: " + success);

                                        }
                                    }
                                }
                                catch (ArrayIndexOutOfBoundsException ex5)
                                {
                                    logger.logComment("End of objects...");
                                }
                            }
                            catch (IOException ex4)
                            {
                                GuiUtils.showErrorMessage(logger,
                                                          "Error loading information on Cell Mechanism in directory: " +
                                                          cellMechDirs[i], ex4, null);
                            }
                            catch (CellMechanismException ex4)
                            {
                                GuiUtils.showErrorMessage(logger,
                                                          "Error setting information on Cell Mechanism in directory: " +
                                                          cellMechDirs[i], ex4, null);
                            }

                            Enumeration names = cellMechProps.propertyNames();

                            while (names.hasMoreElements())
                            {
                                String nextPropName = (String) names.nextElement();
                                //System.out.println("nextPropName: " + nextPropName);

                                if (nextPropName.endsWith(CellMechanismHelper.PROP_SIMENV_SUFFIX))
                                {
                                    String simEnv = nextPropName.substring(0, nextPropName.lastIndexOf(" "));
                                    //System.out.println("-- Impl: "+ simEnv);

                                    acm.specifyNewImplFile(simEnv, cellMechProps.getProperty(nextPropName));
                                }
                            }
                            acm.printDetails();

                            proj.cellMechanismInfo.addCellMechanism(acm);

                        }
                    }
                    catch (IOException ex4)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Error loading information on Cell Mechanism in directory: " +
                                                  cellMechDirs[i], ex4, null);

                    }

                }

            }
        }



        try
        {
            // this is to make sure all the tables which have just been populated
            // have time to send tableChanged notifications before the status of the
            // project is changed
            Thread.sleep(200);
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }

        logger.logComment("Ensuring the sim config info is up to date...");
        proj.simConfigInfo.validateStoredSimConfigs(proj);

        logger.logComment("<> <> <> Populating the cellManager with the cell morphology file data...");

        File[] contents
            = ProjectStructure.getMorphologiesDir(proj.getProjectFile().getParentFile()).listFiles();

        if (contents != null)
        {
            for (int i = 0; i < contents.length; i++)
            {
                if (contents[i].getName().endsWith(ProjectStructure.getMorphMLFileExtension()))
                {
                    logger.logComment("Reading Cell Type info from: " + contents[i]);

                    Cell cellGenerated = null;
                    MorphMLConverter morphMLConverter = new MorphMLConverter();
                    try
                    {
                        cellGenerated = morphMLConverter.loadFromMorphologyFile(contents[i], null);

                        logger.logComment(CellTopologyHelper.printShortDetails(cellGenerated));
                    }
                    catch (MorphologyException ex1)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Problem loading the Cell morphology information in: " + contents[i],
                                                  ex1, null);
                    }

                    try
                    {
                        // to cope with a legacy method for storing channels
                        CellTopologyHelper.updateChannelMechanisms(cellGenerated, proj);

                        proj.cellManager.addCellType(cellGenerated);
                    }
                    catch (NamingException ex2)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem with morphology file: " + contents[i], ex2, null);
                    }

                    logger.logComment("Loaded: " + cellGenerated);

                    logger.logComment(CellTopologyHelper.printShortDetails(cellGenerated));
                }

                if (contents[i].getName().endsWith(ProjectStructure.getJavaXMLFileExtension()))
                {
                    logger.logComment("Reading Cell Type info from: " + contents[i]);

                    Cell cellGenerated = null;
                    try
                    {
                        try
                        {
                            userAgreedUpdate = MorphologyFileUpdate.updateMorphologyFile(contents[i]);
                        }
                        catch (Exception ex3)
                        {
                            throw new ProjectFileParsingException(
                                "Problem while attempting to check the version of the morphology file", ex3);
                        }

                        if (!userAgreedUpdate)
                        {
                            logger.logComment("User cancelled update...");
                            return null;
                        }
                        cellGenerated = MorphMLConverter.loadFromJavaXMLFile(contents[i]);
                    }
                    catch (MorphologyException ex1)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Problem loading the Cell morphology information in: " + contents[i],
                                                  ex1, null);
                    }
                    try
                    {
                        // to cope with a legacy method for storing channels
                        CellTopologyHelper.updateChannelMechanisms(cellGenerated, proj);

                        /*
                                                 // to cope with a time before cell specific  biophys...
                                                 if (cellGenerated.getInitialPotential()==null)
                                                 {
                            NumberGenerator initPot = new NumberGenerator();
                            initPot.initialiseAsFixedFloatGenerator(proj.simulationParameters.getInitVm());
                            cellGenerated.setInitialPotential(initPot);
                                                 }
                                                 // to cope with a time before cell specific  biophys...
                                                 if (cellGenerated.getSpecAxRes()==null)
                                                 {
                            //NumberGenerator specAxRes = new NumberGenerator();
                            //specAxRes.initialiseAsFixedFloatGenerator(proj.simulationParameters.getGlobalRa());
                            //cellGenerated.setSpecAxRes(specAxRes);
                         cellGenerated.associateGroupWithSpecAxRes(Section.ALL, proj.simulationParameters.getGlobalRa());
                                                 }*/

                        proj.cellManager.addCellType(cellGenerated);
                    }
                    catch (NamingException ex2)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem with morphology file: " + contents[i], ex2, null);
                    }

                    logger.logComment("Loaded: " + cellGenerated);
                }

                if (contents[i].getName().endsWith(ProjectStructure.getJavaObjFileExtension()) ||
                    contents[i].getName().endsWith(".obj")) /** @todo remove... */
                {
                    System.out.println("----Reading Cell Type info from: " + contents[i]);

                    Cell cellGenerated = null;
                    try
                    {
                        cellGenerated = MorphMLConverter.loadFromJavaObjFile(contents[i]);

                        //System.out.println("----cellGenerated: " + cellGenerated.getAllSegments());
                        //System.out.println("----cellGenerated: " + CellTopologyHelper.printDetails(cellGenerated, null) );
                        
                    }
                    catch (MorphologyException ex1)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "Problem loading the Cell morphology information in: " + contents[i],
                                                  ex1, null);
                    }
                    try
                    {
                        proj.cellManager.addCellType(cellGenerated);
                    }
                    catch (NamingException ex2)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem with morphology file: " + contents[i], ex2, null);
                    }

                    logger.logComment("Loaded: " + cellGenerated);
                }

            }
        }

        proj.myStatus = Project.PROJECT_SAVED;

        return proj;
    }

    private void initialiseInternalObjects()
    {
        logger.logComment("Internal project objects initialisind...");
        cellGroupsInfo = new CellGroupsInfo();

        cellManager = new CellManager();

        regionsInfo = new RegionsInfo();

        cellMechanismInfo = new CellMechanismInfo();

        simPlotInfo = new SimPlotInfo();

        elecInputInfo = new ElecInputInfo();

        simConfigInfo = new SimConfigInfo();

        morphNetworkConnectionsInfo = new SimpleNetworkConnectionsInfo();
        morphNetworkConnectionsInfo.addTableModelListener(this);

        //complexConnectionsInfo = new ComplexConnectionsInfo();
        //complexConnectionsInfo.addTableModelListener(this);

        volBasedConnsInfo = new ArbourConnectionsInfo();
        volBasedConnsInfo.addTableModelListener(this);

        simulationParameters = new SimulationParameters();
        simulationParameters.initialiseDefaultValues();

        //stimSettings = new StimulationSettings();
        //stimSettings.i

        neuronSettings = new NeuronSettings();
        genesisSettings = new GenesisSettings();

        generatedCellPositions = new GeneratedCellPositions(this);
        generatedNetworkConnections = new GeneratedNetworkConnections(this);
        generatedElecInputs = new GeneratedElecInputs();
        generatedPlotSaves = new GeneratedPlotSaves();

        neuronFileManager = new NeuronFileManager(this);
        genesisFileManager = new GenesisFileManager(this);

        proj3Dproperties = new Display3DProperties();
        proj3Dproperties.initialiseDefaultValues();

        ///synapticProcessInfo = new SynapticProcessInfo();
        ///channelMechanismInfo = new ChannelMechanismInfo();

        //File projNeuronCodeDir = ProjectStructure.getNeuronCodeDir(getProjectMainDirectory());

        ///synapticProcessInfo.setDirectories(projNeuronCodeDir,
        //                                   ProjectStructure.getModTemplatesDir());

        //channelMechanismInfo.setDirectories(projNeuronCodeDir,
        //                                   ProjectStructure.getModTemplatesDir());

    }

    public void markProjectAsEdited()
    {
        myStatus = Project.PROJECT_EDITED_NOT_SAVED;
        logger.logComment("---------------------   Being told project is edited... ");
        if (projectEventListner != null)
        {
            projectEventListner.tableDataModelUpdated("");
        }
    }

    public void resetGenerated()
    {
        generatedNetworkConnections.reset();
        generatedCellPositions.reset();
        generatedElecInputs.reset();
        generatedPlotSaves.reset();
    }

    public void tableChanged(TableModelEvent e)
    {
        logger.logComment("Being told a table has changed: " + e);
        logger.logComment("Project status: " + getProjectStatusAsString());

        if (this.myStatus == Project.PROJECT_NOT_INITIALISED)
        {
            logger.logComment("Ignoring because the project is being loaded...");
        }
        else
        {
            logger.logComment("Marking as edited...");
            markProjectAsEdited();
        }
    }

    public String getProjectStatusAsString()
    {
        switch (myStatus)
        {
            case PROJECT_NOT_INITIALISED:
            {
                return "Project not intitialised";
            }
            case PROJECT_NOT_YET_EDITED:
            {
                return "Project not yet edited";
            }

            case PROJECT_EDITED_NOT_SAVED:
            {
                return "Project edited but not saved";
            }
            case PROJECT_SAVED:
            {
                return "Project saved";
            }
            default:
                return "Unknown Status";
        }
    }

    /**
     * Might be a better place for this...
     */
    public void saveNetworkStructure(File neuroMLFile,
                                     String comment,
                                     boolean zipped,
                                     boolean extraComments,
                                     String simConfig) throws NeuroMLException
    {
        try
        {
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

            rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS, "micron"));

            doc.addRootElement(rootElement);

            logger.logComment("    ****    Full XML:  ****");
            logger.logComment("  ");

            rootElement.addContent("\n\n");

            rootElement.addChildElement(new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                             MetadataConstants.NOTES_ELEMENT, "\n" + comment));

            SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" +
                                                          MorphMLConstants.PROPS_ELEMENT);

            rootElement.addContent("\n\n");

            rootElement.addChildElement(props);

            MetadataConstants.addProperty(props,
                                          NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED,
                                          this.generatedCellPositions.getRandomSeed() + "",
                                          "    ");

            MetadataConstants.addProperty(props,
                                          NetworkMLConstants.NC_SIM_CONFIG,
                                          simConfig,
                                          "    ");

            rootElement.addContent("\n\n");

            rootElement.addChildElement(this.generatedCellPositions.getNetworkMLElement());

            rootElement.addContent("\n\n");

            rootElement.addChildElement(this.generatedNetworkConnections.getNetworkMLElement(UnitConverter.
                GENESIS_PHYSIOLOGICAL_UNITS, extraComments));

            rootElement.addContent("\n");

            String stringForm = doc.getXMLString("", false);

            logger.logComment(stringForm);

            if (!zipped)
            {
                FileWriter fw = new FileWriter(neuroMLFile);
                fw.write(stringForm);
                fw.close();
            }
            else
            {
                File zipFile = new File(neuroMLFile.getAbsolutePath() +
                                        ProjectStructure.getNeuroMLCompressedFileExtension());
                ZipUtils.zipStringAsFile(stringForm, zipFile, neuroMLFile.getName(), comment);
            }
        }
        catch (Exception ex)
        {
            logger.logError("Problem creating NeuroML file: " 
                    + neuroMLFile.getAbsolutePath(), ex);
            throw new NeuroMLException("Problem creating NeuroML file: " 
                    + neuroMLFile.getAbsolutePath(), ex);
        }
    }

    public void saveProject() throws NoProjectLoadedException
    {
        logger.logComment(">>>>>  Saving the project...");
        if (this.myStatus == Project.PROJECT_NOT_INITIALISED)
        {
            logger.logError("Project not yet initialised");
            throw new NoProjectLoadedException();
        }
        if (this.myStatus == Project.PROJECT_NOT_YET_EDITED)
        {
            logger.logComment("Project was opened but not edited, so returning...");
            return;
        }
        if (this.myStatus == Project.PROJECT_SAVED)
        {
            logger.logComment("Project was already saved...");
            return;
        }

        GeneralProperties.saveToSettingsFile(); // why not?

        basicProjectInfo.setProjectFileVersion("neuroConstruct v"
                                               + GeneralProperties.getVersionNumber());

        XMLEncoder xmlEncoder = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try
        {
            fos = new FileOutputStream(currentProjectFile);
            bos = new BufferedOutputStream(fos);
            xmlEncoder = new XMLEncoder(bos);
        }
        catch (FileNotFoundException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem writing to file: " + currentProjectFile, ex, null);
        }

        xmlEncoder.flush();
        try
        {
            String message = new String("\n<!-- This is a neuroConstruct project file. It's best to open this\n"
                                        + "file with neuroConstruct, as opposed to editing it directly. \n\n"
                                        + "Note that this file is neuroConstruct specific and isn't any \n"
                                        + "part of NeuroML or any standardised XML specification. -->\n\n");

            fos.write(message.getBytes());
        }
        catch (Exception ex)
        {
            logger.logError("Problem writing to project file...", ex);
        }
        xmlEncoder.flush();

        /* -- Writing basic info -- */
        xmlEncoder.writeObject(basicProjectInfo);

        /* -- Writing Regions info -- */
        xmlEncoder.writeObject(regionsInfo);

        /* -- Writing Cell Groups info -- */
        xmlEncoder.writeObject(cellGroupsInfo);

        /* -- Writing Simulation plot info -- */
        xmlEncoder.writeObject(simPlotInfo);

        /* -- Writing 3D info -- */
        xmlEncoder.writeObject(proj3Dproperties);

        /* -- Writing NEURON settings -- */
        xmlEncoder.writeObject(neuronSettings);

        /* -- Writing GENESIS settings -- */
        xmlEncoder.writeObject(genesisSettings);

        /* -- Writing Simulation info --*/
        xmlEncoder.writeObject(simulationParameters);

        /* -- Writing Stimulation info --*/
        xmlEncoder.writeObject(elecInputInfo);

        /* -- Writing Simulation config info --*/
        xmlEncoder.writeObject(simConfigInfo);

        /* -- Writing Simple Net Conn info -- */
        xmlEncoder.writeObject(morphNetworkConnectionsInfo);

        /* -- Writing Complex info --
                 xmlEncoder.writeObject(complexConnectionsInfo);   // save no more
         */
        /* -- Writing Complex info -- */
        xmlEncoder.writeObject(volBasedConnsInfo);

        /* -- Writing Cell Mechanism info --
                 xmlEncoder.writeObject(cellMechanismInfo);
         */

        xmlEncoder.close();

        logger.logComment("<><><>  Saving the cell mechanisms...");

        ///...

        File dirForCellMechs = ProjectStructure.getCellMechanismDir(this.getProjectMainDirectory());

        Vector<String> allCellMechNames = this.cellMechanismInfo.getAllCellMechanismNames();

        for (int j = 0; j < allCellMechNames.size(); j++)
        {
            String nextCellMech = allCellMechNames.get(j);

            CellMechanism cellMech = cellMechanismInfo.getCellMechanism(nextCellMech);

            File cellMechDir = new File(dirForCellMechs, nextCellMech);
            if (!cellMechDir.exists())
            {
                cellMechDir.mkdir();
            }

            Properties cellMechProps = new Properties();

            cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_MODEL, cellMech.getMechanismModel());
            cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_TYPE, cellMech.getMechanismType());

            if (cellMech instanceof AbstractedCellMechanism)
            {
                AbstractedCellMechanism acm = (AbstractedCellMechanism) cellMech;

                cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_NAME, acm.getInstanceName());
                cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_DESCRIPTION, acm.getDescription());
                cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_DEFAULT_NAME, acm.getDefaultInstanceName());
                if (acm.getPlotInfoFile() != null)
                {
                    cellMechProps.setProperty(CellMechanismHelper.PROP_PLOT_INFO_FILE, acm.getPlotInfoFile());
                }
                File internalPropsFile = new File(cellMechDir, CellMechanismHelper.INTERNAL_PROPS_FILENAME);

                try
                {
                    XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(internalPropsFile)));

                    InternalPhysicalParameter[] params = acm.getPhysicalParameterList();
                    for (int i = 0; i < params.length; i++)
                    {
                        e.writeObject(params[i]);
                    }

                    e.close();
                }
                catch (IOException ex3)
                {
                    GuiUtils.showErrorMessage(logger, "Error creating cell mechanism info file: " + internalPropsFile,
                                              ex3, null);

                }

                MechanismImplementation[] mechsImpls = acm.getMechanismImpls();

                for (int i = 0; i < mechsImpls.length; i++)
                {
                    File implFile = mechsImpls[i].getImplementingFileObject(this, acm.getInstanceName());

                    cellMechProps.setProperty(mechsImpls[i].getSimulationEnvironment() +
                                              CellMechanismHelper.PROP_SIMENV_SUFFIX, implFile.getName());

                    File nativeFileProcDir = ProjectStructure.getFileBasedCellProcessesDir(this.getProjectMainDirectory(), false);

                    if (nativeFileProcDir != null && nativeFileProcDir.exists())
                    {
                        File oldNativeFile = mechsImpls[i].getImplementingFileObject(this, acm.getInstanceName());
                        File targetFile = new File(cellMechDir, mechsImpls[i].getImplementingFile());

                        if (!targetFile.exists())
                        {
                            try
                            {
                                GeneralUtils.copyFileIntoDir(oldNativeFile, cellMechDir);
                            }
                            catch (IOException ex3)
                            {
                                GuiUtils.showErrorMessage(logger, "Error copying cell process file: " + oldNativeFile,
                                                          ex3, null);

                            }
                        }
                    }
                }

                if (cellMech instanceof FileBasedMembraneMechanism)
                {
                    cellMechProps.setProperty(CellMechanismHelper.PROP_IMPL_METHOD,
                                              CellMechanism.FILE_BASED_CELL_MECHANISM);
                }
                else
                {
                    cellMechProps.setProperty(CellMechanismHelper.PROP_IMPL_METHOD,
                                              CellMechanism.ABSTRACTED_CELL_MECHANISM);
                }
            }
            else if (cellMech instanceof ChannelMLCellMechanism)
            {
                ChannelMLCellMechanism cmlcm = (ChannelMLCellMechanism) cellMech;
                cellMechProps.setProperty(CellMechanismHelper.PROP_CHANNELML_FILE, cmlcm.getChannelMLFile());

                // Note name and description will be taken from the channelml file...
                cellMechProps.setProperty(CellMechanismHelper.PROP_IMPL_METHOD,
                                          CellMechanism.CHANNELML_BASED_CELL_MECHANISM);

                //cmlcm.initialise(this, false);

                cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_NAME, cellMech.getInstanceName());
                cellMechProps.setProperty(CellMechanismHelper.PROP_CELL_MECH_DESCRIPTION, cellMech.getDescription());

                ArrayList<SimXSLMapping> simMaps = cmlcm.getSimMappings();

                for (SimXSLMapping simMap : simMaps)
                {
                    cellMechProps.setProperty(simMap.getSimEnv() + CellMechanismHelper.PROP_MAPPING_SUFFIX,
                                              simMap.getXslFile());

                    cellMechProps.setProperty(simMap.getSimEnv() + CellMechanismHelper.PROP_NEEDS_COMP_SUFFIX,
                                              simMap.isRequiresCompilation() + "");

                }
            }

            if (!cellMechDir.exists())
            {
                cellMechDir.mkdir();
            }
            File propsFile = new File(cellMechDir, CellMechanismHelper.PROPERTIES_FILENAME);

            FileOutputStream fos2 = null;
            try
            {
                fos2 = new FileOutputStream(propsFile);
                cellMechProps.storeToXML(fos2,
                    "\nProperties associated with the Cell Mechanism which allow it to be loaded into neuroConstruct.\n\n"
                                         + "Note the following: \n" +
                                         "   The Cell Mechanism name should not contain spaces and should match the name of the directory it's in\n" +
                                         "   The name and description here will be replaced by the corresponding values in a ChannelML file if found\n" +
                                         "   The filenames for the mappings are relative to the cellMechanism/(cellMechInstanceName) directory\n" +
                                         "   Mechanism Type should only have values: " +
                                         CellMechanism.CHANNEL_MECHANISM + ", " + CellMechanism.SYNAPTIC_MECHANISM
                                         + ", " + CellMechanism.ION_CONCENTRATION + "\n\n");
                fos2.close();

            }
            catch (Exception ex2)
            {
                GuiUtils.showErrorMessage(logger, "Error storing information on Cell Mechanism: " + nextCellMech, ex2, null);

            }

            File oldDirForCellProcs = ProjectStructure.getCellProcessesDir(this.getProjectMainDirectory(), false);

            if (oldDirForCellProcs != null && oldDirForCellProcs.exists())
            {
                File cellProcDir = new File(oldDirForCellProcs, nextCellMech);

                if (cellProcDir.exists())
                {
                    File[] contents = cellProcDir.listFiles();

                    for (int k = 0; k < contents.length; k++)
                    {
                        if (contents[k].isFile())
                        {
                            try
                            {
                                File correspondingFile = new File(cellMechDir, contents[k].getName());
                                if (!correspondingFile.exists())
                                {
                                    GeneralUtils.copyFileIntoDir(contents[k], cellMechDir);
                                }
                            }
                            catch (IOException ex3)
                            {
                                GuiUtils.showErrorMessage(logger, "Error copying cell process file: " + contents[k],
                                                          ex3, null);

                            }
                        }
                    }
                }
            }
        }

        File oldDirForCellProcs = ProjectStructure.getCellProcessesDir(this.getProjectMainDirectory(), false);

        if (oldDirForCellProcs != null && oldDirForCellProcs.exists())
        {
            //oldDirForCellProcs = oldDirForCellProcs.getCanonicalFile();
            File newName = new File(oldDirForCellProcs.getParentFile(), "oldCP");
            oldDirForCellProcs.renameTo(newName);
        }

        logger.logComment("<><><>  Saving the morphologies...");

        File dirForProjectMorphologies = ProjectStructure.getMorphologiesDir(this.getProjectMainDirectory());

        // saving the cell morphologies...

        Vector<Cell> cells = cellManager.getAllCells();

        boolean problemSaving = false;

        ArrayList<String> filesSaved = new ArrayList<String> ();

        for (Cell cell : cells)
        {

            try
            {

                logger.logComment("Saving cell: " + cell.getInstanceName()
                                  + " in " + this.preferredSaveFormat);

                if (preferredSaveFormat.equals(ProjectStructure.JAVA_OBJ_FORMAT))
                {
                    File objFile = new File(dirForProjectMorphologies,
                                            cell.getInstanceName()
                                            + ProjectStructure.getJavaObjFileExtension());

                    problemSaving = ! (MorphMLConverter.saveCellInJavaObjFormat(cell, objFile) && !problemSaving);
                    filesSaved.add(objFile.getName());

                }
                else if (preferredSaveFormat.equals(ProjectStructure.JAVA_XML_FORMAT))
                {
                    File xmlFile = new File(dirForProjectMorphologies,
                                            cell.getInstanceName()
                                            + ProjectStructure.getJavaXMLFileExtension());

                    problemSaving = ! (MorphMLConverter.saveCellInJavaXMLFormat(cell, xmlFile) && !problemSaving);

                    filesSaved.add(xmlFile.getName());

                }

            }
            catch (MorphologyException ex1)
            {
                GuiUtils.showErrorMessage(logger, "Problem saving cell: " + cell.getInstanceName(), ex1, null);

                this.myStatus = Project.PROJECT_EDITED_NOT_SAVED;

                problemSaving = true;

            }

        }
        if (!problemSaving)
        {
            File[] currFiles = dirForProjectMorphologies.listFiles();
            for (int i = 0; i < currFiles.length; i++)
            {
                if (! (currFiles[i].getName().equals("README") ||
                       currFiles[i].getName().endsWith(".zip") || // in case cells achived due to MorphologyFileUpdate
                       currFiles[i].getName().endsWith(".tmp") ||
                       currFiles[i].getName().endsWith(".bak") ||
                       filesSaved.contains(currFiles[i].getName())))
                {
                    currFiles[i].delete();
                }
            }

        }
        else
        {
            logger.logComment("Problem saving...");
        }
        this.myStatus = Project.PROJECT_SAVED;

        logger.logComment("<<<<<  Finished saving the project...");
    }

    public int getProjectStatus()
    {
        return this.myStatus;
    }

    public String getProjectFileName()
    {
        return currentProjectFile.getName();
    }

    public String getProjectFullFileName() throws NoProjectLoadedException
    {
        return currentProjectFile.getAbsolutePath();
    }

    public File getProjectFile()
    {
        return currentProjectFile;
    }

    public File getProjectMainDirectory()
    {
        return this.currentProjectFile.getParentFile();
    }

    public String getProjectDescription()
    {
        return basicProjectInfo.getProjectDescription();
    }

    public String getProjectName()
    {
        return basicProjectInfo.getProjectName();
    }

    public void setProjectName(String projectName)
    {
        basicProjectInfo.setProjectName(projectName);
    }

    public String getProjectFileVersion()
    {
        return basicProjectInfo.getProjectFileVersion();
    }

    /**
     * Put here so we can watch for changes
     */
    public void setProjectDescription(String projectDescription)
    {
        basicProjectInfo.setProjectDescription(projectDescription);
        this.markProjectAsEdited();
    }

}
