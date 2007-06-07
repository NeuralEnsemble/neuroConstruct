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

import java.io.*;
import java.util.*;

import java.awt.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLException;

/**
 * A class for handling interaction with the project
 * All non gui functionality will (eventually) be transferred here,
 * to reduce the size of MainFrame, and to give access to the core functionality
 * via the command line interface
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class ProjectManager
{
    private static ClassLogger logger = new ClassLogger("ProjectManager");

    private Project activeProject = null;

    public CellPositionGenerator cellPosnGenerator = null;
    public MorphBasedConnGenerator netConnGenerator = null;

    public VolumeBasedConnGenerator arbourConnectionGenerator = null;
    public ElecInputGenerator elecInputGenerator = null;
    public PlotSaveGenerator plotSaveGenerator = null;

    private GenerationReport reportInterface = null;
    private ProjectEventListener projEventListener = null;

    private static Random randomGenerator = new Random();
    private static long currentSeed = 1;



    public ProjectManager(GenerationReport reportInterface,
                          ProjectEventListener projEventListener)
    {
        this.reportInterface = reportInterface;
        this.projEventListener = projEventListener;
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


    public void doSaveNetworkML(File networkmlFile) throws NeuroMLException
    {
        doSaveNetworkML(networkmlFile, false, false, SimConfigInfo.DEFAULT_SIM_CONFIG_NAME);
    }


    public void doSaveNetworkML(File networkmlFile,
                                boolean zip,
                                boolean extraComments,
                                String simConfig) throws NeuroMLException
    {
        StringBuffer notes = new StringBuffer("\nNetwork structure for project: "
                                              + getCurrentProject().getProjectName() +
                                              " saved with neuroConstruct v" +
                                              GeneralProperties.getVersionNumber() + " on: " +
                                              GeneralUtils.getCurrentTimeAsNiceString() + ", "
                                              + GeneralUtils.getCurrentDateAsNiceString() + "\n\n");

        Iterator<String> cellGroups = getCurrentProject().generatedCellPositions.getNamesGeneratedCellGroups();

        while (cellGroups.hasNext())
        {
            String cg = cellGroups.next();
            int numHere = getCurrentProject().generatedCellPositions.getNumberInCellGroup(cg);
            if (numHere > 0)
                notes.append("Cell Group: " + cg + " contains " + numHere + " cells\n");

        }
        notes.append("\n");

        Iterator<String> netConns = getCurrentProject().generatedNetworkConnections.getNamesNetConns();

        while (netConns.hasNext())
        {
            String mc = netConns.next();
            int numHere = getCurrentProject().generatedNetworkConnections.getSynapticConnections(mc).size();
            if (numHere > 0)
                notes.append("Network connection: " + mc + " contains " + numHere + " individual synaptic connections\n");

        }
        notes.append("\n");


            getCurrentProject().saveNetworkStructure(networkmlFile,
                                                                 notes.toString(),
                                                                 zip,
                                                                 extraComments,
                                                                 simConfig);


    }


    public Display3DProperties getProjectDispProps()
    {
        return activeProject.proj3Dproperties;
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

                File cmlFile = cmlCm.getChannelMLFile(activeProject);

                if (cmlFile!=null && cmlFile.exists())
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


                for (SimXSLMapping simMapping: simMappings)
                {
                    File f = simMapping.getXslFileObject(activeProject, next.getInstanceName());

                    if (f!=null && f.exists())
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

            overallValidity = ValidityStatus.combineValidities(overallValidity, cellMechValidity);

        }


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
                                        +"postsynaptically connected to another Cell Group. Unless the cells are spontaneously active, nothing much will happen there in a simulation.",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_WARN + "\"");

                report.addBreak();

                overallValidity = ValidityStatus.combineValidities(overallValidity, ValidityStatus.VALIDATION_WARN);

            }
        }




        report.addTaggedElement("Validating Electrical Inputs...", "p");

        Vector<StimulationSettings> inputs = this.activeProject.elecInputInfo.getAllStims();

        for (StimulationSettings stim: inputs)
        {
            if (!activeProject.cellGroupsInfo.getAllCellGroupNames().contains(stim.getCellGroup()))
            {
                report.addTaggedElement("Error, Input: " + stim.getReference() + " specifies Cell Group: " + stim.getCellGroup() +
                                        ", but there isn't any Cell Group by that name in the project!",
                                        "font color=\"" + ValidityStatus.VALIDATION_COLOUR_ERROR + "\"");
                report.addBreak();

                overallValidity = ValidityStatus.VALIDATION_ERROR;


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

        cellPosnGenerator.start();

    }



    public void doLoadProject(File projFile) throws ProjectFileParsingException
    {
        activeProject = Project.loadProject(projFile, projEventListener);
    }



}
