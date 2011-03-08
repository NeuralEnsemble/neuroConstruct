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
import java.util.*;
import java.util.Random;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.hpc.utils.ProcessManager;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.SimulationData;
import ucl.physiol.neuroconstruct.simulation.SimulationsInfo;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.XMLUtils;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.xml.*;


/**
 * Main file for generating simulations based on NeuroML
 *
 * @author Padraig Gleeson
 *  
 */

public class NeuroMLFileManager
{
    private static ClassLogger logger = new ClassLogger("NeuroMLFileManager");

    Project project = null;

    /////File mainSimFile = null;

    int randomSeed = 0;

    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    boolean mainFileGenerated = false;

    private HashMap<String, Integer> nextColour = new HashMap<String, Integer>();


    private NeuroMLFileManager()
    {
    }


    public NeuroMLFileManager(Project project)
    {
        this.project = project;
    }


    public void reset()
    {
        nextColour = new HashMap<String, Integer>();
       
    }



    public static File saveNetworkStructureXML(Project project,
                                       File neuroMLFile,
                                       boolean zipped,
                                       boolean extraComments,
                                       String simConfig,
                                       String units,
                                       NeuroMLVersion version) throws NeuroMLException
    {
        int preferredUnits = UnitConverter.getUnitSystemIndex(units);

        try
        {

            StringBuilder notes = new StringBuilder("\nNetwork structure (NeuroML "+version+") for project: "
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
            
            boolean nml2 = version.isVersion2();

            String rootName = NetworkMLConstants.ROOT_ELEMENT;
            String defNamespace = NetworkMLConstants.NAMESPACE_URI;
            String loc = NetworkMLConstants.DEFAULT_SCHEMA_FILENAME;

            String metaPrefix = MetadataConstants.PREFIX + ":";

            if(nml2)
            {
                rootName = NeuroMLConstants.ROOT_ELEMENT;
                defNamespace = NeuroMLConstants.NAMESPACE_URI_VERSION_2;
                loc = NeuroMLConstants.DEFAULT_SCHEMA_FILENAME_VERSION_2;
                metaPrefix = "";
            }


            rootElement = new SimpleXMLElement(rootName);

            rootElement.addNamespace(new SimpleXMLNamespace("", defNamespace));

            if (!nml2) rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                            MetadataConstants.NAMESPACE_URI));

            rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                            NeuroMLConstants.XSI_URI));

            rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                            defNamespace
                                                            + "  " + loc));

            if (!nml2)
            {
                rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, MetadataConstants.LENGTH_UNITS_MICROMETER));
            }
            else
            {
                rootElement.addAttribute(NeuroMLConstants.NEUROML_ID_V2, "network_"+project.getProjectName());
            }

            doc.addRootElement(rootElement);

            logger.logComment("    ****    Full XML:  ****");
            logger.logComment("  ");

            

            rootElement.addContent("\n\n");

            rootElement.addChildElement(new SimpleXMLElement(metaPrefix +
                                                             MetadataConstants.NOTES_ELEMENT, "\n" + notes.toString()));

            SimpleXMLElement props = new SimpleXMLElement(metaPrefix +MorphMLConstants.PROPS_ELEMENT);

            rootElement.addContent("\n\n");

            if (!nml2) rootElement.addChildElement(props);

            MetadataConstants.addProperty(props,
                                          NetworkMLConstants.NC_NETWORK_GEN_RAND_SEED,
                                          project.generatedCellPositions.getRandomSeed() + "",
                                          "    ",
                                          version);

            if (simConfig!=null)
            {
                MetadataConstants.addProperty(props,
                                              NetworkMLConstants.NC_SIM_CONFIG,
                                              simConfig,
                                              "    ",
                                              version);
            }

            //SimpleXMLElement popRoot = rootElement;

            SimpleXMLElement topLevelCompElement = null;

            if (nml2)
            {
                SimpleXMLElement networkNml2 = new SimpleXMLElement(NetworkMLConstants.NEUROML2_NETWORK_ELEMENT);
                networkNml2.addAttribute(NeuroMLConstants.NEUROML_ID_V2, NetworkMLConstants.NEUROML2_NETWORK_ID_PREFIX+project.getProjectName());

                topLevelCompElement = rootElement;
                
                rootElement = networkNml2;
            }



            ArrayList<SimpleXMLElement> popElements = project.generatedCellPositions.getNetworkMLElements(version);

            for(SimpleXMLElement popEl: popElements)
            {
                rootElement.addContent("\n\n        ");

                rootElement.addChildElement(popEl);

            }

            rootElement.addContent("\n\n");

            ArrayList<SimpleXMLEntity> netEntities = project.generatedNetworkConnections.getNetworkMLElements(preferredUnits, extraComments, version);

            for(SimpleXMLEntity netEntity: netEntities)
            {
                rootElement.addContent("\n\n        ");
                if (netEntity instanceof SimpleXMLElement)
                {
                    rootElement.addChildElement((SimpleXMLElement)netEntity);
                }
                else if (netEntity instanceof SimpleXMLComment)
                {
                    rootElement.addComment((SimpleXMLComment)netEntity);
                }
            }


            ArrayList<SimpleXMLEntity> elecInputEntities = project.generatedElecInputs.getNetworkMLEntities(preferredUnits, version, topLevelCompElement);

            for(SimpleXMLEntity elecInputEntity: elecInputEntities)
            {

                rootElement.addContent("\n\n        ");
                if (elecInputEntity instanceof SimpleXMLElement)
                {
                    rootElement.addChildElement((SimpleXMLElement)elecInputEntity);
                }
                else if (elecInputEntity instanceof SimpleXMLComment)
                {
                    rootElement.addComment((SimpleXMLComment)elecInputEntity);
                }
            }


            if (nml2)
            {
                topLevelCompElement.addContent("\n    ");
                topLevelCompElement.addChildElement(rootElement);
                topLevelCompElement.addContent("\n\n");
            }

            rootElement.addContent("\n\n");
            if (nml2) rootElement.addContent("    ");

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


    public void generateNeuroMLFiles(SimConfig simConf,
                                  MorphCompartmentalisation mc,
                                  int seed,
                                  boolean singleL3File) throws IOException
    {
         generateNeuroMLFiles(simConf, mc, seed, singleL3File, false);
    }

    public void generateNeuroMLFiles(SimConfig simConf,
                                  MorphCompartmentalisation mc,
                                  int seed,
                                  boolean singleL3File,
                                  boolean annotations) throws IOException
    {
        generateNeuroMLFiles(simConf, NeuroMLVersion.NEUROML_VERSION_1, false, mc, seed, singleL3File, annotations);
    }

    public void generateNeuroMLFiles(SimConfig simConf,
                                     NeuroMLVersion version,
                                     boolean generateLems,
                                     MorphCompartmentalisation mc,
                                     int seed,
                                     boolean singleL3File,
                                     boolean annotations) throws IOException
    {

        File neuroMLDir = ProjectStructure.getNeuroMLDir(project.getProjectMainDirectory());

        generateNeuroMLFiles(simConf,
                                     version,
                                     generateLems,
                                     mc,
                                     seed,
                                     singleL3File,
                                     annotations,
                                     neuroMLDir);
    }

    public void generateNeuroMLFiles(SimConfig simConf,
                                     NeuroMLVersion version,
                                     boolean generateLems,
                                     MorphCompartmentalisation mc,
                                     int seed,
                                     boolean singleL3File,
                                     boolean annotations,
                                     File generateDir) throws IOException
    {
        logger.logComment("Starting generation of the files into dir: "+ generateDir.getCanonicalPath(), true);


        GeneralUtils.removeAllFiles(generateDir, false, false, true);

        if (!generateDir.exists())
            generateDir.mkdir();


        randomSeed = seed;

        ArrayList<File> generatedFiles = new ArrayList<File>();
        

        String timeInfo = GeneralUtils.getCurrentDateAsNiceString() +"_"+GeneralUtils.getCurrentTimeAsNiceString();

        timeInfo = GeneralUtils.replaceAllTokens(timeInfo, ":", "-");

        String fileName = "L3Net_" +timeInfo+ ProjectStructure.getNeuroMLFileExtension();

        if (version.isVersion2())
        {
            fileName = project.getProjectName() + ProjectStructure.getNeuroMLFileExtension();
        }

        File generatedNetworkFile = new File(generateDir, fileName);


        
        if (singleL3File)
        {
            File netFile = null;
            if (annotations)
            {
                try {
                    netFile = ProjectManager.saveLevel3NetworkXML(project,
                                                          generatedNetworkFile,
                                                          false, false, annotations,
                                                          simConf.getName(),
                                                          NetworkMLConstants.UNITS_PHYSIOLOGICAL);
                } catch (NeuroMLException ex) {
                     GuiUtils.showErrorMessage(logger, "Problem saving complete network with annotations in NeuroML", ex, null);
                }
            }
            else
            {
                try {
                    netFile = ProjectManager.saveLevel3NetworkXML(project,
                                                          generatedNetworkFile,
                                                          false, false,
                                                          simConf.getName(),
                                                          NetworkMLConstants.UNITS_PHYSIOLOGICAL);
                } catch (NeuroMLException ex) {
                     GuiUtils.showErrorMessage(logger, "Problem saving complete network in NeuroML:\n" +
                         ""+ex.getMessage(), ex, null);
                }
            }
            generatedFiles.add(netFile);

        }           
        else
        {

            ArrayList<Cell> generatedCells = null;

            if (project.generatedCellPositions.getAllPositionRecords().isEmpty())
                simConf = null; // signifies no particular sim conf, so gen all cells, channels, etc.

            try
            {
                NeuroMLLevel level = NeuroMLLevel.NEUROML_LEVEL_3;

                if (version.isVersion2())
                    level = NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL;

                generatedCells = MorphMLConverter.saveAllCellsInNeuroML(project,
                                                       mc,
                                                       level,
                                                       version,
                                                       simConf,
                                                       generateDir);

                for(Cell cell: generatedCells)
                {
                    generatedFiles.add(new File(generateDir, cell.getInstanceName()+ProjectStructure.getNeuroMLFileExtension()));
                }
            }
            catch (MorphologyException ex1)
            {
                GuiUtils.showErrorMessage(logger, "Problem saving cells in NeuroML format" , ex1, null);
                return;
            }

            ArrayList<String> cellMechFilesHandled = new ArrayList<String>();

            for(Cell nextCell: generatedCells)
            {
                ArrayList<String> cellMechs = new ArrayList<String>();
                ArrayList<String> chanMechNames = nextCell.getAllChanMechNames(true);

                for(String cm: chanMechNames)
                {
                    cellMechs.add(cm);
                }
                ArrayList<String> syns = nextCell.getAllAllowedSynapseTypes();

                cellMechs.addAll(syns);

                logger.logComment("cellMechs: "+cellMechs);

                for(String cellMech: cellMechs)
                {
                    if (!cellMechFilesHandled.contains(cellMech))
                    {
                        CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cellMech);

                        if (cm== null)
                        {
                            //??
                        }
                        else if (cm instanceof NeuroML2Component)
                        {
                            NeuroML2Component nmlCm = (NeuroML2Component)cm;
                            String newName = cm.getInstanceName()+".nml";
                            File copied = GeneralUtils.copyFileIntoDir(nmlCm.getXMLFile(project), generateDir);
                            copied.renameTo(new File(generateDir, newName));

                            logger.logComment("copied: "+copied, false);

                        }
                        else if (!(cm instanceof ChannelMLCellMechanism))
                        {
                            File warnFile = new File(generateDir, cm.getInstanceName()+".warning");
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
                        else
                        {
                            ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;

                            File origCmlFile = cmlCm.getXMLFile(project);

                            File newCmlFile = new File(generateDir, cm.getInstanceName()+".xml");

                            if (cmlCm.getMechanismModel().indexOf("ChannelML")>=0 )
                            {
                                newCmlFile = new File(generateDir, cm.getInstanceName()+".nml");

                                File xslChannelML2NeuroML2 = ProjectStructure.getChannelml2Neuroml2File();


                                String xslContents = GeneralUtils.readShortFile(xslChannelML2NeuroML2);

                                //TODO Make celsius a global variable!!
                                String defaultTemp = project.simulationParameters.getTemperature() +" degC";
                                int start = xslContents.indexOf("<xsl:variable name=\"defaultTemp\">")+33;
                                int end = xslContents.indexOf("</xsl:variable>", start);
                                xslContents = xslContents.substring(0, start)+defaultTemp+xslContents.substring(end);
                                //System.out.println(xslContents);

                                String nml2Contents = XMLUtils.transform(origCmlFile, xslContents);

                                GeneralUtils.writeShortFile(newCmlFile, nml2Contents);

                                //XMLUtils.transform(origCmlFile, xslChannelML2NeuroML2, generateDir, ".nml");

                                //String origFileName = origCmlFile.getName();

                                //File generatedFile = new File(generateDir, origFileName.substring(0, origFileName.length()-4)+".nml");


                                //generatedFile.renameTo(newCmlFile);
                            }
                            else
                            {
                                try
                                {
                                    File copied = GeneralUtils.copyFileIntoDir(origCmlFile, generateDir);

                                    copied.renameTo(newCmlFile);

                                }
                                catch(IOException ioe)
                                {
                                    GuiUtils.showErrorMessage(logger, "Problem writing to file: " +cmlCm, ioe, null);
                                }
                            }
                        }
                        cellMechFilesHandled.add(cellMech);
                    }
                }
            }


            for(String cellMech: cellMechFilesHandled)
            {
                generatedFiles.add(new File(generateDir, cellMech+ProjectStructure.getNeuroMLFileExtension()));
            }

            String networkFileName = NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML;
            if (version.isVersion2())
            {
                networkFileName = project.getProjectName()+ProjectStructure.getNeuroMLFileExtension();
            }
            File networkFile = new File(generateDir, networkFileName);

            if (project.generatedCellPositions.getAllPositionRecords().size()>0)
            {
                try
                {

                    File netFile = saveNetworkStructureXML(project,
                                                 networkFile,
                                                 false,
                                                 false,
                                                 simConf.getName(),
                                                 NetworkMLConstants.UNITS_PHYSIOLOGICAL,
                                                 version);

                    generatedFiles.add(netFile);
                }
                catch (NeuroMLException ex1)
                {
                    GuiUtils.showErrorMessage(logger, "Problem saving network in NeuroML", ex1, null);
                }
            }

        }


        if (generateLems && version.isVersion2())
        {
            String lemsFileName = "LEMS_"+project.getProjectName()+".xml";
            File lemsFile = new File(generateDir, lemsFileName);

            SimpleXMLElement lemsElement = new SimpleXMLElement(LemsConstants.ROOT_LEMS);

            lemsElement.addContent("\n\n    "); // to make it more readable...

            SimpleXMLElement defRunElement = new SimpleXMLElement(LemsConstants.DEFAULT_RUN_ELEMENT);
            lemsElement.addChildElement(defRunElement);
            lemsElement.addContent("\n\n    "); // to make it more readable...

            defRunElement.addAttribute(LemsConstants.COMPONENT_ATTR, LemsConstants.DEFAULT_SIM_ID);


            lemsElement.addComment("Include standard NeuroML 2 ComponentType definitions");
            lemsElement.addContent("\n    "); // to make it more readable...

            SimpleXMLElement incEl1 = new SimpleXMLElement(LemsConstants.INCLUDE_ELEMENT);
            incEl1.addAttribute(LemsConstants.FILE_ATTR, NeuroMLConstants.NEUROML2_CORE_TYPES_CELLS);
            lemsElement.addChildElement(incEl1);
            lemsElement.addContent("\n    "); // to make it more readable...


            SimpleXMLElement incEl2 = new SimpleXMLElement(LemsConstants.INCLUDE_ELEMENT);
            incEl2.addAttribute(LemsConstants.FILE_ATTR, NeuroMLConstants.NEUROML2_CORE_TYPES_NETWORKS);
            lemsElement.addChildElement(incEl2);
            lemsElement.addContent("\n    "); // to make it more readable...

            SimpleXMLElement incEl3 = new SimpleXMLElement(LemsConstants.INCLUDE_ELEMENT);
            incEl3.addAttribute(LemsConstants.FILE_ATTR, NeuroMLConstants.NEUROML2_CORE_TYPES_SIMULATION);
            lemsElement.addChildElement(incEl3);
            lemsElement.addContent("\n\n    "); // to make it more readable...


            lemsElement.addContent("\n\n    "); // to make it more readable...

            lemsElement.addComment("Include the generated NeuroML 2 files");

            for(File genFile: generatedFiles)
            {

                SimpleXMLElement incElc = new SimpleXMLElement(LemsConstants.INCLUDE_ELEMENT);
                incElc.addAttribute(LemsConstants.FILE_ATTR, genFile.getAbsolutePath());
                lemsElement.addChildElement(incElc);
                lemsElement.addContent("\n    "); // to make it more readable...
            }



            lemsElement.addContent("\n\n    "); // to make it more readable...


            SimpleXMLElement simEl = new SimpleXMLElement(LemsConstants.SIMULATION_ELEMENT);
            lemsElement.addChildElement(simEl);
            simEl.addContent("\n        "); // to make it more readable...


            simEl.addAttribute(NeuroMLConstants.NEUROML_ID_V2, LemsConstants.DEFAULT_SIM_ID);
            
            simEl.addAttribute(LemsConstants.LENGTH_ATTR, simConf.getSimDuration()+"ms");
            simEl.addAttribute(LemsConstants.STEP_ATTR, project.simulationParameters.getDt()+"ms");
            simEl.addAttribute(LemsConstants.TARGET_ATTR, NetworkMLConstants.NEUROML2_NETWORK_ID_PREFIX+project.getProjectName());



            HashMap<String, SimpleXMLElement> displaysAdded = new HashMap<String, SimpleXMLElement>();

            File dirForAllSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());
            File simDir = new File(dirForAllSims, project.simulationParameters.getReference());

            File summaryFile = new File(simDir, "simulator.props");
            String repFile = summaryFile.getAbsolutePath();
            repFile = repFile.replaceAll("\\\\", "\\\\\\\\");
            simEl.addAttribute(LemsConstants.REPORT_ATTR, repFile);

            File timesFile = new File(simDir, "time.dat");
            String timesFilename =  timesFile.getAbsolutePath();
            timesFilename = timesFilename.replaceAll("\\\\", "\\\\\\\\");
            simEl.addAttribute(LemsConstants.TIMES_FILE_ATTR,timesFilename);

            simDir.mkdir();

            for (SimPlot simPlot: project.simPlotInfo.getAllSimPlots())
            {
                if (simConf.getPlots().contains(simPlot.getPlotReference()))
                {
                    String displayId = simPlot.getGraphWindow();
                    String value = convertValue(simPlot.getValuePlotted());

                    logger.logComment("-+- Adding plot: "+ simPlot+" "+simConf.toLongString());

                    if (!value.equals("???"))
                    {
                        ArrayList<Integer> cellNums = new ArrayList<Integer>();

                        String cellNumPattern = simPlot.getCellNumber();

                        int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(simPlot.getCellGroup());

                        logger.logComment("- Val of: "+ value+" on "+numInCellGroup+", "+ cellNumPattern);

                        if (cellNumPattern.equals("*"))
                        {
                            for(int i=0;i<numInCellGroup;i++)
                            {
                                cellNums.add(i);
                            }
                        }
                        else if(cellNumPattern.indexOf("#") >= 0)
                        {
                            int numToPick = Integer.parseInt(cellNumPattern.substring(0, cellNumPattern.length()-1));
                            if (numToPick>=numInCellGroup)
                            {
                                for(int i=0;i<numInCellGroup;i++)
                                {
                                    cellNums.add(i);
                                }
                            }
                            else
                            {
                                Random r = new Random();

                                while(cellNums.size()<numToPick)
                                {
                                    int next = r.nextInt(numInCellGroup);
                                    if(!cellNums.contains(next)) cellNums.add(next);
                                }
                            }
                        }
                        else
                        {
                            int single = Integer.parseInt(cellNumPattern);
                            cellNums.add(single);
                        }

                        if (cellNums.size()>0)
                        {
                            if (!displaysAdded.containsKey(displayId))
                            {
                                SimpleXMLElement dispEl = new SimpleXMLElement(LemsConstants.DISPLAY_ELEMENT);

                                simEl.addContent("\n        "); // to make it more readable...
                                simEl.addChildElement(dispEl);
                                simEl.addContent("\n    "); // to make it more readable...

                                dispEl.addAttribute(LemsConstants.ID_ATTR, displayId);
                                dispEl.addAttribute(LemsConstants.TITLE_ATTR, project.getProjectName()+": "+ simConf.getName()+", "+simPlot.getCellGroup());
                                dispEl.addAttribute(LemsConstants.TIMESCALE_ATTR, "1ms");

                                displaysAdded.put(displayId, dispEl);
                            }

                            for(int cellNum: cellNums)
                            {
                                SimpleXMLElement lineEl = new SimpleXMLElement(LemsConstants.LINE_ELEMENT);
                                SimpleXMLElement dispEl = displaysAdded.get(displayId);

                                dispEl.addContent("\n            "); // to make it more readable...
                                dispEl.addChildElement(lineEl);
                                dispEl.addContent("\n        "); // to make it more readable...

                                String titleDisp = dispEl.getAttributeValue(LemsConstants.TITLE_ATTR);
                                dispEl.setAttributeValue(LemsConstants.TITLE_ATTR, titleDisp+", "+simPlot.getValuePlotted());

                                lineEl.addAttribute(LemsConstants.ID_ATTR, simPlot.getPlotReference());


                                String path = simPlot.getCellGroup()+"["+cellNum+"]/"+ value;

                                lineEl.addAttribute(LemsConstants.QUANTITY_ATTR, path);

                                if(simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                                {
                                    lineEl.addAttribute(LemsConstants.SCALE_ATTR, "1mV");   //TODO: check for units...
                                }
                                else
                                {
                                    lineEl.addAttribute(LemsConstants.SCALE_ATTR, "1");   //TODO: check for units...
                                }

                                //String colourHex = getNextColourHex(displayId);
                                String colourHex = getNextColourHex("All different colours...");

                                lineEl.addAttribute(LemsConstants.COLOR_ATTR, "#"+colourHex);

                                if (simPlot.toBeSaved())
                                {
                                    String datFile = simPlot.getCellGroup()+"_"+cellNum+".dat";
                                    if (!simPlot.isVoltage())
                                        datFile = simPlot.getCellGroup()+"_"+cellNum+"."+simPlot.getSafeVarName()+".dat";
                                    
                                    File fullFile = new File(simDir, datFile);
                                    String fileStr = fullFile.getAbsolutePath();
                                    fileStr = fileStr.replaceAll("\\\\", "\\\\\\\\");
                                    lineEl.addAttribute(LemsConstants.SAVE_ATTR,fileStr);

                                }
                                logger.logComment("Adding line: "+ lineEl.getXMLString("", false));
                            }
                        }
                    }
                }
            }

            lemsElement.addContent("\n\n"); // to make it more readable...

            GeneralUtils.writeShortFile(lemsFile, lemsElement.getXMLString("", false));

            String runFileName = "runsim.sh";
            String lemsExeName = "./lems";

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                runFileName = "runsim.bat";
                lemsExeName = ProjectStructure.getLemsDir().getAbsolutePath() + "\\lems.bat";
            }

            File runFile = new File(generateDir, runFileName);

            StringBuilder runScript = new StringBuilder();
            runScript.append("cd "+ProjectStructure.getLemsDir().getAbsolutePath()+"\n");

            runScript.append(lemsExeName + " "+lemsFile.getAbsolutePath()+"\n");

            GeneralUtils.writeShortFile(runFile, runScript.toString());

            Runtime rt = Runtime.getRuntime();
            // bit of a hack...
            rt.exec(new String[]{"chmod","u+x",runFile.getAbsolutePath()});

            String executable = runFile.getAbsolutePath();

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                executable = GeneralProperties.getExecutableCommandLine() + " "+ lemsExeName+" "+lemsFile.getAbsolutePath();
            }
            File dirToRunIn = ProjectStructure.getLemsDir();


            File positionsFile = new File(simDir, SimulationData.POSITION_DATA_FILE);
            File netConnsFile = new File(simDir, SimulationData.NETCONN_DATA_FILE);
            File elecInputFile = new File(simDir, SimulationData.ELEC_INPUT_DATA_FILE);
            try
            {
                project.generatedCellPositions.saveToFile(positionsFile);
                project.generatedNetworkConnections.saveToFile(netConnsFile);
                project.generatedElecInputs.saveToFile(elecInputFile);
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Problem saving generated positions in file: " + positionsFile.getAbsolutePath(),
                                          ex, null);
                return;
            }

            // Saving summary of the simulation params
            try
            {
                SimulationsInfo.recordSimulationSummary(project, simConf, simDir, "LEMS", null);
            }
            catch (IOException ex2)
            {
                GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "+ simDir +
                                          "\nThere will be less info on this simulation in the previous simulation browser dialog", ex2, null);
            }


            logger.logComment("Going to execute: " + executable + " in dir: " +
                                      dirToRunIn, true);

            //Process process = rt.exec(executable, null, dirToRunIn);
            ProcessManager.runCommand(executable, "LEMS", 5, dirToRunIn);


      
        }

    }

    private String convertValue(String val)
    {
        if (val.equals(SimPlot.VOLTAGE))
            return "v";
        if (val.split(":").length==2)  // TODO: Make more general!!!!
        {
            String cmName = val.split(":")[0];
            String varName = val.split(":")[1];
            return "biophys/membraneProperties/"+cmName+"_all/"+cmName+"/"+varName+"/q";
        }

        return "???";
    }


    public int getCurrentRandomSeed()
    {
        return this.randomSeed;
    }

    public float getCurrentGenTime()
    {
        return this.genTime;
    }



    public static String getFileHeader()
    {
        StringBuilder response = new StringBuilder();
        response.append("//  ******************************************************\n");
        response.append("// \n");
        response.append("//     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append("// \n");
        response.append("//  ******************************************************\n");

        response.append("\n");
        return response.toString();
    }


    public String getNextColourHex(String plotFrame)
    {
        if (!nextColour.containsKey(plotFrame))
        {
            nextColour.put(plotFrame, 1);
        }
        int colNum = nextColour.get(plotFrame);

        String colour = ColourUtils.getSequentialColourHex(colNum);
        int newColour = colNum +1;
        if (newColour >= 10) newColour = 1;

        nextColour.put(plotFrame, newColour);

        return colour;
    }





    public static void main(String[] args)
    {
        try
        {
            File projFile = new File("lems/nCproject/LemsTest/LemsTest.ncx");
            //projFile = new File("models/VSCSGranCell/VSCSGranCell.neuro.xml");

            String simConf = SimConfigInfo.DEFAULT_SIM_CONFIG_NAME;

            if (projFile.getName().startsWith("VSCSGranCell"))
            {
                simConf = "TestSimConf";
            }
            if (projFile.getName().startsWith("LemsTest"))
            {
                //simConf = "GranCell";
                simConf = "MainenCell";
            }

            Project p = Project.loadProject(projFile, null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            pm.doGenerate(simConf, 123);

            Thread.sleep(1000);

            System.out.println("Generated cells: "+ p.generatedCellPositions.details());
            
            NeuroMLFileManager npfm = new NeuroMLFileManager(p);


            OriginalCompartmentalisation oc = new OriginalCompartmentalisation();

            SimConfig sc = p.simConfigInfo.getSimConfig(simConf);

            boolean runLems = true;
            
            npfm.generateNeuroMLFiles(sc, NeuroMLVersion.NEUROML_VERSION_2, runLems, oc, 123, false, false);

            File tempDir = new File(projFile.getParentFile(), "temp");

            npfm.generateNeuroMLFiles(sc, NeuroMLVersion.NEUROML_VERSION_1, false, oc, 123, false, false, tempDir);
            
            //gen.runGenesisFile();
        }
        catch(Exception e)
        {
            //e.printStackTrace();
        }
    }


}
