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

package ucl.physiol.neuroconstruct.psics;

import java.io.*;


import java.util.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.DataSetManager;
import ucl.physiol.neuroconstruct.gui.DataSetManager.DataReadFormat;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.hpc.utils.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.ChannelMLConstants;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;
import ucl.physiol.neuroconstruct.project.stimulation.IClampInstanceProps;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;
import ucl.physiol.neuroconstruct.utils.xml.*;


/**
 * Main file for generating the PSICS files
 *
 * @author Padraig Gleeson
 *  
 */

public class PsicsFileManager
{
    private static ClassLogger logger = new ClassLogger("PsicsFileManager");

    Project project = null;

    File mainPsicsFile = null;

    int randomSeed = 0;

    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    boolean mainFileGenerated = false;

    ArrayList<String> cellTemplatesGenAndIncl = new ArrayList<String>();
        
    ArrayList<String> includedChanMechNames = new ArrayList<String>();
    
    String theOneCellGroup = null;


    private Hashtable<String, Integer> nextColour = new Hashtable<String, Integer>();

    //private static boolean addComments = true;



    ArrayList<String> graphsCreated = new ArrayList<String>();

    SimConfig simConfig = null;
    
        
    //private boolean quitAfterRun = false;
        
    static
    {
        logger.setThisClassVerbose(false);
    }


    private PsicsFileManager()
    {
    }


    public PsicsFileManager(Project project)
    {
        this.project = project;
    }
    
    
   


    public void reset()
    {
        cellTemplatesGenAndIncl = new ArrayList<String>();
        graphsCreated = new ArrayList<String>();
        nextColour = new Hashtable<String, Integer>(); // reset it...
        includedChanMechNames = new ArrayList<String>();

        //addComments = project.psicsSettings.isGenerateComments();
    }
    
  
    public void generateThePsicsFiles(SimConfig simConfig,
                                        int seed) throws PsicsException, IOException
    {
        logger.logComment("Starting generation of the files...");

        long generationTimeStart = System.currentTimeMillis();
        
        this.simConfig = simConfig;


        this.removeAllPreviousFiles();


        randomSeed = seed;

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed


        FileWriter fw = null;
        nextColour = new Hashtable<String, Integer>(); // reset it...
        
        

        try
        {

            File dirForPsicsFiles = ProjectStructure.getPsicsCodeDir(project.getProjectMainDirectory());


            mainPsicsFile = new File(dirForPsicsFiles, project.getProjectName() + ".xml");

            logger.logComment("generating: "+ mainPsicsFile);
            
            generateChanMechIncludes();
            
            fw = new FileWriter(mainPsicsFile);

            //fw.write(getFileHeader());
            
            //todo: Move strings PSICSRun, etc. to PsicsConstants class...
            
            SimpleXMLElement sxe = new SimpleXMLElement("PSICSRun");
            
            SimpleXMLAttribute dt = new SimpleXMLAttribute("timeStep", project.simulationParameters.getDt()+"ms");
            sxe.addAttribute(dt);
            
            SimpleXMLAttribute dur = new SimpleXMLAttribute("runTime", getSimDuration()+"ms");
            sxe.addAttribute(dur);
            
            float potVal = 0;
            Iterator<String> cgs = project.generatedCellPositions.getNamesGeneratedCellGroups();
            
            if (cgs.hasNext())
            {
               theOneCellGroup = cgs.next();
               Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(theOneCellGroup));
               potVal = cell.getInitialPotential().getNominalNumber();
            }
            if(project.generatedCellPositions.getPositionRecords(theOneCellGroup).size()!=1)
            {
                throw new PsicsException("A cell group with multiple cells detected!\n" +
                "PSICS only supports single cell modelling at the moment!", null);
            }
            if(cgs.hasNext())
            {
                throw new PsicsException("Multiple cell groups detected!\n" +
                "PSICS only supports single cell modelling at the moment!", null);
            }
            
            SimpleXMLAttribute pot = new SimpleXMLAttribute("startPotential", potVal+"mV");
            sxe.addAttribute(pot);
            
            File f = generateEnvironmentFile(dirForPsicsFiles);
            String fileRef = f.getName().substring(0, f.getName().indexOf("."));
            
            SimpleXMLAttribute env = new SimpleXMLAttribute("environment", fileRef);
            sxe.addAttribute(env);
            
            //Iterator<String> cellGroupsproject.generatedCellPositions.getNamesGeneratedCellGroups();
            

           Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(theOneCellGroup));
           
           logger.logComment("theOneCellGroup: "+theOneCellGroup);
           

           PsicsMorphologyGenerator morphGen = new PsicsMorphologyGenerator(cell, project, dirForPsicsFiles);

           morphGen.generateFiles();
           String morphCellFilename = morphGen.getCellFile().getName();

           SimpleXMLAttribute cellMorph = new SimpleXMLAttribute("morphology", morphCellFilename.substring(0,morphCellFilename.indexOf(".")));
            sxe.addAttribute(cellMorph);

           String membFilename = morphGen.getMembFile().getName();

           SimpleXMLAttribute memb = new SimpleXMLAttribute("properties", membFilename.substring(0,membFilename.indexOf(".")));
            sxe.addAttribute(memb);


            
            f = generateRecordingFile(dirForPsicsFiles);
            fileRef = f.getName().substring(0, f.getName().indexOf("."));
            
            SimpleXMLAttribute rec = new SimpleXMLAttribute("access", fileRef);
            sxe.addAttribute(rec);
            
            
            SimpleXMLAttribute sto = new SimpleXMLAttribute("stochThreshold", "0");
            sxe.addAttribute(sto);
            
            SimpleXMLAttribute sqCaps = new SimpleXMLAttribute("squareCaps", "true");
            sxe.addAttribute(sqCaps);
            
            File dirForSimDataFiles = getDirectoryForSimulationFiles();
            
            SimpleXMLAttribute outputFolder = new SimpleXMLAttribute("outputFolder", dirForSimDataFiles.getAbsolutePath());
            sxe.addAttribute(outputFolder);
          
            
            sxe.addContent("\n");
            
            SimpleXMLElement info = new SimpleXMLElement("info");
            info.addContent("PSICS project generated from: "+project.getProjectFileName());
            sxe.addChildElement(info);

            sxe.addContent("\n");
            
            SimpleXMLElement disc = new SimpleXMLElement("StructureDiscretization");
            SimpleXMLAttribute baseElementSize = new SimpleXMLAttribute("baseElementSize", project.psicsSettings.getSpatialDiscretisation()+"");
            disc.addAttribute(baseElementSize);
            sxe.addChildElement(disc);


            
            sxe.addContent("\n");
            
            ArrayList<PlotSaveDetails> psds = project.generatedPlotSaves.getSavedPlotSaves();
            
            
            SimpleXMLElement vc = new SimpleXMLElement("ViewConfig");

            Hashtable<String, SimpleXMLElement> lineGraphs = new Hashtable<String, SimpleXMLElement>();
            
            resetColours();
            int colNum = 0;

            colNum++; // As first col will be time
            
            colNum += project.generatedElecInputs.getNumberSingleInputs(); // as input traces will be put in next

            for(PlotSaveDetails psd: psds)
            {
                if (psd.simPlot.toBeSaved() && psd.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                {
                    String lineGraphRef = psd.simPlot.getGraphWindow();
                    SimpleXMLElement lineGraph = null;

                    if (!lineGraphs.containsKey(lineGraphRef))
                    {
                        SimpleXMLElement lg = new SimpleXMLElement("LineGraph");

                        SimpleXMLAttribute w = new SimpleXMLAttribute("width", "500");
                        lg.addAttribute(w);

                        SimpleXMLAttribute h = new SimpleXMLAttribute("height","400");
                        lg.addAttribute(h);

                        SimpleXMLElement xAxis = new SimpleXMLElement("XAxis");
                        xAxis.addAttribute(new SimpleXMLAttribute("min","0"));
                        xAxis.addAttribute(new SimpleXMLAttribute("max",simConfig.getSimDuration()+""));
                        xAxis.addAttribute(new SimpleXMLAttribute("label","time / ms"));
                        lg.addChildElement(xAxis);
                        lg.addContent("\n        ");


                        SimpleXMLElement yAxis = new SimpleXMLElement("YAxis");
                        yAxis.addAttribute(new SimpleXMLAttribute("min",psd.simPlot.getMinValue()+""));
                        yAxis.addAttribute(new SimpleXMLAttribute("max",psd.simPlot.getMaxValue()+""));
                        lg.addChildElement(yAxis);
                        lg.addContent("\n        ");


                        SimpleXMLElement view = new SimpleXMLElement("View");
                        view.addAttribute(new SimpleXMLAttribute("id",psd.simPlot.getPlotReference()));
                        view.addAttribute(new SimpleXMLAttribute("xmin","0"));
                        view.addAttribute(new SimpleXMLAttribute("xmax",simConfig.getSimDuration()+""));
                        view.addAttribute(new SimpleXMLAttribute("ymin",psd.simPlot.getMinValue()+""));
                        view.addAttribute(new SimpleXMLAttribute("ymax",psd.simPlot.getMaxValue()+""));
                        lg.addChildElement(view);
                        lg.addContent("        \n");

                        lineGraphs.put(lineGraphRef, lg);

                        vc.addChildElement(lg);
                        vc.addContent("\n");

                    }
                    lineGraph = lineGraphs.get(lineGraphRef);

                    SimpleXMLElement line = new SimpleXMLElement("Line");
                    line.addAttribute(new SimpleXMLAttribute("file","psics-out.txt"));
                    line.addAttribute(new SimpleXMLAttribute("color","#"+getNextColourHex(lineGraphRef)));
                    //line.addAttribute(new SimpleXMLAttribute("label",psd.simPlot.getSegmentId()));
                    line.addAttribute(new SimpleXMLAttribute("show",colNum+""));
                    colNum++;

                    lineGraph.addContent("\n        ");
                    lineGraph.addChildElement(line);



                }
                
            }


            sxe.addChildElement(vc);
            
            sxe.addContent("\n");
            
            fw.write(sxe.getXMLString("", false));
            
            
            fw.flush();
            fw.close();


        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            try
            {
                fw.close();
            }
            catch (Exception ex1)
            {
                throw new PsicsException("Error creating file: " + mainPsicsFile.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the PSICS files you are trying to generate are not currently being used", ex1);
            }
            throw new PsicsException("Error creating file: " + mainPsicsFile.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the PSICS files you are trying to generate are not currently being used", ex);

        }

        this.mainFileGenerated = true;

        long generationTimeEnd = System.currentTimeMillis();
        genTime = (float) (generationTimeEnd - generationTimeStart) / 1000f;

        logger.logComment("... Created Main PSICS file: " + mainPsicsFile
                +" in "+genTime+" seconds. ");
        

    }
    
    private File generateRecordingFile(File dir) throws PsicsException
    {
        File f = new File(dir, "recording.xml");
        
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(f);
            
            //fw.write(getFileHeader());
            
            SimpleXMLElement access = new SimpleXMLElement("Access");
            
            SimpleXMLAttribute id = new SimpleXMLAttribute("id", "recording");
            access.addAttribute(id);
            
            SimpleXMLAttribute saveInterval = new SimpleXMLAttribute("saveInterval", project.simulationParameters.getDt()+"ms");
            access.addAttribute(saveInterval);

            SimpleXMLAttribute separateFiles = new SimpleXMLAttribute("separateFiles", "true");
            access.addAttribute(separateFiles);

            SimpleXMLAttribute recordClamps = new SimpleXMLAttribute("recordClamps", "true");
            access.addAttribute(recordClamps);
            
            Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(theOneCellGroup));
            
            ArrayList<String> refs = project.generatedElecInputs.getInputReferences();
            for(String ref:refs)
            {
                ArrayList<SingleElectricalInput> inputs = project.generatedElecInputs.getInputLocations(ref);
                for(SingleElectricalInput input: inputs)
                {
                    if (!input.getCellGroup().equals(theOneCellGroup))
                    {
                        throw new PsicsException("An input has been detected on a Cell Group which is not the only cell group generated ("+theOneCellGroup+")!\n" +
                        "PSICS only supports single cell modelling at the moment!", null);
                    }
                    if (input.getCellNumber() != 0)
                    {
                        throw new PsicsException("An input has been detected on a cell which is not cell_id = 0!\n" +
                        "PSICS only supports single cell modelling at the moment!", null);
                    }
                    
               
                    Segment seg = cell.getSegmentWithId(input.getSegmentId());
                    
                    StimulationSettings ss = project.elecInputInfo.getStim(ref);
                    if (ss instanceof IClampSettings)
                    {
                        IClampSettings ics = (IClampSettings)ss;
                        
                        SimpleXMLElement cc = new SimpleXMLElement("CurrentClamp");
                        
                        SimpleXMLAttribute at = new SimpleXMLAttribute("at", seg.getSegmentName());
                        cc.addAttribute(at);
                        
                        SimpleXMLAttribute col = new SimpleXMLAttribute("lineColor", "red");
                        cc.addAttribute(col);
                        
                        SimpleXMLElement cp = new SimpleXMLElement("CurrentPulse");
                        
                        float del = -1, dur = -1, amp = -1;
                        if (input.getInstanceProps()!=null)
                        {
                            IClampInstanceProps icip = (IClampInstanceProps)input.getInstanceProps();
                            del = icip.getDelay();
                            dur = icip.getDuration();
                            amp = icip.getAmplitude();
                        }
                        else
                        {
                            del = ics.getDel().getNominalNumber(); //should be a fixed num generator anyway...
                            dur = ics.getDur().getNominalNumber(); //should be a fixed num generator anyway...
                            amp = ics.getAmp().getNominalNumber(); //should be a fixed num generator anyway...
                        }
                        
                        SimpleXMLAttribute start = new SimpleXMLAttribute("start", del+"ms");
                        cp.addAttribute(start);
                        
                        SimpleXMLAttribute duration = new SimpleXMLAttribute("duration", dur+"ms");
                        cp.addAttribute(duration);
                        
                        SimpleXMLAttribute ampl = new SimpleXMLAttribute("to", amp+"nA");
                        cp.addAttribute(ampl);
                        
                        
                        cc.addChildElement(cp);
                        
                        cc.addContent("\n");
                        access.addChildElement(cc);
                        
                        access.addContent("\n");
                    }
                    else
                    {
                        SimpleXMLElement cc = new SimpleXMLElement("Only CurrentClamp supported in nC mapping to PSICS so far... Get in touch with PG...");
                        access.addChildElement(cc);
                    }
                    
                }
                    
            }
            
            access.addContent("\n");
            
            
            ArrayList<PlotSaveDetails> psds = project.generatedPlotSaves.getSavedPlotSaves();
            
            for(PlotSaveDetails psd: psds)
            {
                if (psd.simPlot.toBeSaved() && psd.simPlot.getValuePlotted().equals(SimPlot.VOLTAGE))
                {
                    ArrayList<Integer> segIds = new ArrayList<Integer>();
                    if (psd.simPlot.getSegmentId().equals("*"))
                    {
                        for(Segment seg: cell.getAllSegments())
                        {
                            segIds.add(seg.getSegmentId());
                        }
                    }
                    else
                    {
                        segIds.add(Integer.parseInt(psd.simPlot.getSegmentId()));

                    }

                    for(int segId: segIds)
                    {
                        Segment seg = cell.getSegmentWithId(segId);


                        SimpleXMLElement vr = new SimpleXMLElement("VoltageRecorder");

                        SimpleXMLAttribute at = new SimpleXMLAttribute("at", seg.getSegmentName());
                        vr.addAttribute(at);

                        SimpleXMLAttribute col = new SimpleXMLAttribute("lineColor", "0x"+getNextColourHex(psd.simPlot.getGraphWindow()));
                        vr.addAttribute(col);

                        String fileName = SimPlot.getFilename(psd, seg, "0");

                        SimpleXMLAttribute label = new SimpleXMLAttribute("label", fileName);
                        vr.addAttribute(label);

                        access.addChildElement(vr);
                        access.addContent("\n");
                    }
                }
                
            }
            
            
            access.addContent("\n");
            fw.write(access.getXMLString("", false));
            
            
            fw.flush();
            fw.close();


        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            try
            {
                fw.close();
            }
            catch (Exception ex1)
            {
                throw new PsicsException("Error creating file: " + f.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the PSICS files you are trying to generate are not currently being used", ex1);
            }
            throw new PsicsException("Error creating file: " + f.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the PSICS files you are trying to generate are not currently being used", ex);

        }
        return f;
        
    }

    public void resetColours()
    {
        for(String ref: nextColour.keySet())
        {
            nextColour.put(ref, 1);
        }

    }


    public String getNextColour(String plotFrame)
    {
        if (!nextColour.containsKey(plotFrame))
        {
            nextColour.put(plotFrame, 1);
        }
        int colNum = nextColour.get(plotFrame);

        String colour = ColourUtils.getColourName(colNum).toLowerCase();
        int newColour = colNum +1;
        if (newColour >= 10) newColour = 1;

        nextColour.put(plotFrame, newColour);

        return colour;
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
    
    private File generateEnvironmentFile(File dir) throws PsicsException
    {
        File f = new File(dir, "environment.xml");
        
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(f);
            
            //fw.write(getFileHeader());
            
            SimpleXMLElement sxe = new SimpleXMLElement("CellEnvironment");

            SimpleXMLAttribute id = new SimpleXMLAttribute("id", "environment");
            sxe.addAttribute(id);

            SimpleXMLAttribute temperature = new SimpleXMLAttribute("temperature", project.simulationParameters.getTemperature()+"Celsius");
            sxe.addAttribute(temperature);
            
            for(String chanMech: includedChanMechNames)
            {
                try
                {
                    ChannelMLCellMechanism cmlcm = (ChannelMLCellMechanism)project.cellMechanismInfo.getCellMechanism(chanMech);

                    String ion;

                    SimpleXMLElement ionEl = new SimpleXMLElement("Ion");

                    ion = cmlcm.getXMLDoc().getValueByXPath(ChannelMLConstants.getIonNameXPath());
                    
                    String erev  = cmlcm.getXMLDoc().getValueByXPath(ChannelMLConstants.getIonRevPotXPath());

                    SimpleXMLAttribute idAttr = new SimpleXMLAttribute("id", ion);

                    ionEl.addAttribute(idAttr);

                    SimpleXMLAttribute nameAttr = new SimpleXMLAttribute("name", ion+"_ion");
                    ionEl.addAttribute(nameAttr);
                    
                    String units = cmlcm.getXMLDoc().getValueByXPath(ChannelMLConstants.getUnitsXPath());
                    String unit = units.equals(ChannelMLConstants.PHYSIOLOGICAL_UNITS) ? "mV" : "V";
                    
                    SimpleXMLAttribute revPotAttr = new SimpleXMLAttribute("reversalPotential", erev+unit);
                    ionEl.addAttribute(revPotAttr);

                    sxe.addChildElement(ionEl);
                    sxe.addContent("\n    ");
                }
                catch (XMLMechanismException ex)
                {
                    throw new PsicsException("Error finding information on ion in "+chanMech);
                }
                catch (ClassCastException cc)
                {
                    throw new PsicsException("Error casting cell mech "+chanMech+" to ChannelMLCellMechanism", cc);
                }
                
            }
            sxe.addContent("\n");
            
            
            fw.write(sxe.getXMLString("", false));
            
            
            fw.flush();
            fw.close();


        }
        catch (IOException ex)
        {
            logger.logError("Problem: ",ex);
            try
            {
                fw.close();
            }
            catch (Exception ex1)
            {
                throw new PsicsException("Error creating file: " + f.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the PSICS files you are trying to generate are not currently being used", ex1);
            }
            throw new PsicsException("Error creating file: " + f.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the PSICS files you are trying to generate are not currently being used", ex);

        }
        return f;
        
    }


    public int getCurrentRandomSeed()
    {
        return this.randomSeed;
    }

    public float getCurrentGenTime()
    {
        return this.genTime;
    }



    private void removeAllPreviousFiles()
    {
        cellTemplatesGenAndIncl.clear();

        File codeDir = ProjectStructure.getPsicsCodeDir(project.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(codeDir, false, true, true);


    }


    public static String getFileHeader()
    {
        StringBuilder response = new StringBuilder();
        response.append("<!--\n");
        response.append("******************************************************\n");
        response.append("\n");
        response.append("     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append(" \n");
        response.append("******************************************************\n");

        response.append("-->\n\n");
        return response.toString();
    }

    private File getDirectoryForSimulationFiles()
    {
        File dirForSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());

        File dirForDataFiles = new File(dirForSims,
                                        project.simulationParameters.getReference());


        if (!dirForDataFiles.isDirectory() || !dirForDataFiles.exists())
        {
            dirForDataFiles.mkdir();
        }
        return dirForDataFiles;
    }



    private float getSimDuration()
    {
        if (simConfig.getSimDuration()==0) // shouldn't be...
        {
            return project.simulationParameters.getDuration();
        }
        else
            return simConfig.getSimDuration();
    }



    public void runFile(boolean copyToSimDataDir,
                        boolean htmlSummary,
                        boolean showPlot,
                        boolean showConsole) throws PsicsException
    {
        logger.logComment("Trying to run the mainFile...");

        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new PsicsException("PSICS file not yet generated");
        }


        File dirForSimDataFiles = getDirectoryForSimulationFiles();
        File dirToRunFrom = null;
        File genDir = ProjectStructure.getPsicsCodeDir(project.getProjectMainDirectory());
        
        if (copyToSimDataDir)
        {
            dirToRunFrom = dirForSimDataFiles;
            
            try
            {
                GeneralUtils.copyDirIntoDir(genDir, dirForSimDataFiles, false, true);
            }
            catch (Exception e)
            {
                throw new PsicsException("Problem copying the PSICS files from "+genDir+" to "+ dirForSimDataFiles, e);
                
            }
        }
        else
        {
            dirToRunFrom = genDir; 
        }
        

        File positionsFile = new File(dirForSimDataFiles, SimulationData.POSITION_DATA_FILE);
        File netConnsFile = new File(dirForSimDataFiles, SimulationData.NETCONN_DATA_FILE);
        File elecInputFile = new File(dirForSimDataFiles, SimulationData.ELEC_INPUT_DATA_FILE);
        

        
        if (dirToRunFrom.getAbsolutePath().indexOf(" ")>=0)
        {
            throw new PsicsException("PSICS files cannot be run in a directory like: "+ dirToRunFrom
                    + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run PSICS.\n" +
                    "Arguments need to be given to this executable and spaces in filenames cause problems.\n\n"
                    +"Try saving the project (File -> Copy Project (Save As)...) in a directory without spaces.");
        }

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
            SimulationsInfo.recordSimulationSummary(project, simConfig, dirForSimDataFiles, "PSICS", null);
        }
        catch (IOException ex2)
        {
            GuiUtils.showErrorMessage(logger, "Error when trying to save a summary of the simulation settings in dir: "+ dirForSimDataFiles +
                                      "\nThere will be less info on this simulation in the previous simulation browser dialog", ex2, null);
        }


        
        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;

        try
        {
            //String executable = null;
            
            String javaEx = "java -Xmx1000M -jar ";
            
            File fullFileToRun = new File(dirToRunFrom, mainPsicsFile.getName());
            
            String psicsJar = GeneralProperties.getPsicsJar();
            
            if(psicsJar==null || psicsJar.trim().length()==0 || !(new File(psicsJar)).exists())
            {
                GuiUtils.showErrorMessage(logger,
                                      "There is no valid PSICS Jar file set. Download the latest version from www.psics.org, and set the\n" +
                                      "location of the Jar file via Settings -> General Properties & Project Defaults -> PSICS jar file",
                                      null, null);
                return;
            }
            
            String title = "PSICS" + "__" + project.simulationParameters.getReference();
            

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");
                
                File scriptFile = new File(ProjectStructure.getPsicsCodeDir(project.getProjectMainDirectory()),
                                           "runsim.bat");
                
                commandToExecute = "cmd /K start \""+title+"\"  " +  scriptFile.getAbsolutePath();
                if (!showConsole)
                {
                    commandToExecute =  scriptFile.getAbsolutePath();
                }
                String runCommand = javaEx+ " "+psicsJar+" "+fullFileToRun.getName();
                //commandToExecute = "cmd /K start \""+title+"\"  tree c:\\";

                //logger.logComment("Going to execute command: " + commandToExecute);
                
                //String[] env = {};
                
                String scriptText = "@echo off\n\ncd "+ dirToRunFrom.getAbsolutePath()+"\n";
                scriptText = scriptText + runCommand +"\n";
                scriptText = scriptText + "pause\n";
                scriptText = scriptText + "exit\n";

                FileWriter fw = new FileWriter(scriptFile);

                fw.write(scriptText);
                fw.close();

                rt.exec(commandToExecute);

                logger.logComment("Have executed command: " + commandToExecute/*+" in woriking dir: "+ dirToRunFrom*/);

            }
            else
            {
                logger.logComment("Assuming *nix environment...");


                //File dirToRunIn = ProjectStructure.getGenesisCodeDir(project.getProjectMainDirectory());

                String basicCommLine = GeneralProperties.getExecutableCommandLine();

                String executable = "";
                String extraArgs = "";
                String titleOption = "";
                String workdirOption = "";

                if (basicCommLine.indexOf("konsole")>=0)
                {
                    logger.logComment("Assume we're using KDE");
                    titleOption = " --title="+title;
                    workdirOption = " --workdir="+ dirToRunFrom.getAbsolutePath();
                    extraArgs = "-e /bin/bash ";
                    executable = basicCommLine.trim();
                }
                else if (basicCommLine.indexOf("gnome")>=0)
                {
                    logger.logComment("Assume we're using Gnome");
                    titleOption = " --title="+title;
                    workdirOption = " --working-directory="+ dirToRunFrom.getAbsolutePath();

                    if (basicCommLine.trim().indexOf(" ")>0) // case where basicCommLine is gnome-terminal -x
                    {
                        extraArgs = basicCommLine.substring(basicCommLine.trim().indexOf(" ")).trim();

                        executable = basicCommLine.substring(0, basicCommLine.trim().indexOf(" ")).trim();
                    }
                    else
                    {
                        executable = basicCommLine;
                        extraArgs = "-x";
                    }

                }
                else
                {
                    logger.logComment("Unknown console command, going with the flow...");
                    executable = basicCommLine.trim();
                }

                String scriptText = javaEx+ " "+psicsJar+ " "+fullFileToRun.getCanonicalPath()+"\n";
                int secs = 5;
                scriptText = scriptText + "echo \"Finished simulation. Data stored in "+dirToRunFrom.getAbsolutePath()+". Will close terminal in "+secs+" seconds...\"\n";
                scriptText = scriptText + "sleep "+secs+"\n";

                File scriptFile = new File(ProjectStructure.getPsicsCodeDir(project.getProjectMainDirectory()),
                                           "runsim.sh");
                FileWriter fw = new FileWriter(scriptFile);

                fw.write(scriptText);
                fw.close();

                // bit of a hack...
                rt.exec("chmod u+x " + scriptFile.getAbsolutePath());
                try
                {
                    // This is to make sure the file permission is updated..
                    Thread.sleep(600);
                }
                catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }

                commandToExecute = executable
                    + " "
                    + titleOption
                    + " "
                    + workdirOption
                    + " "
                    + extraArgs
                    + " " +
                    scriptFile.getAbsolutePath();

                if (!showConsole){
                    commandToExecute = scriptFile.getAbsolutePath();
                }


                ProcessFeedback pf = new ProcessFeedback()
                {

                    public void comment(String comment)
                    {
                        logger.logComment("ProcessFeedback: "+comment);
                    }

                    public void error(String comment)
                    {
                        logger.logComment("ProcessFeedback: "+comment);
                    }
                };

                logger.logComment("Going to execute command: " + commandToExecute);

                //rt.exec(commandToExecute);

                ProcessManager.runCommand(commandToExecute, pf, 4);

                logger.logComment("Have successfully executed command: " + commandToExecute);
                
               
            }
            
            if(htmlSummary || showPlot)
            {
                File resultsDir = dirForSimDataFiles;
                File resultsHtml = new File(resultsDir, "index.html");
                File resultsDatafile = new File(resultsDir, "psics-out.txt");
                
                logger.logComment("Checking for results info: " + resultsHtml.getAbsolutePath());
                        
                int tries = 15;
                while (tries>0)
                {
                    if (resultsHtml.exists())
                    {
                        tries=0;
                        String browserPath = GeneralProperties.getBrowserPath(true);
                        if (browserPath==null)
                        {
                            GuiUtils.showErrorMessage(logger, "Could not start a browser!", null, null);
                            return;
                        }

                        if(htmlSummary)
                        {
                            String command = browserPath + " " + resultsHtml.toURI();
                            //String command2 = browserPath + " " + resultsHtml.toURI();

                            logger.logComment("Going to execute command: " + command);

                            try
                            {
                                rt.exec(command);
                            }
                            catch (IOException ex)
                            {
                                logger.logError("Error running " + command);
                            }

                            logger.logComment("Have successfully executed command: " + command);
                        }
                        if(showPlot)
                        {
                            ArrayList<DataSet> dataSets = DataSetManager.loadFromDataSetFile(resultsDatafile, false, DataReadFormat.FIRST_COL_TIME);

                            String plotFrameRef = "Plot of data from simulation "+project.simulationParameters.getReference();

                            PlotterFrame frame = PlotManager.getPlotterFrame(plotFrameRef);

                            for(DataSet dataSet: dataSets)
                                frame.addDataSet(dataSet);

                            frame.setVisible(true);
                        }
                        
                    }
                    else
                    {
                        Thread.sleep(1000);
                        tries--;
                    }
                }
            }
            
        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            throw new PsicsException("Error executing the PSICS file: " + mainPsicsFile, ex);
        }
    }



    public String getMainPsicsFileName() throws PsicsException
    {
        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new PsicsException("PSICS file not yet generated");
        }

        return this.mainPsicsFile.getAbsolutePath();

    }
    
    
    
    private String generateChanMechIncludes() throws PsicsException
    {
        StringBuffer response = new StringBuffer();

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();


        String dir = ""; // needed under windows...
        if (GeneralUtils.isWindowsBasedPlatform())
        {
            dir = this.mainPsicsFile.getParentFile().getAbsolutePath() + System.getProperty("file.separator");
        }


        for (int ii = 0; ii < cellGroupNames.size(); ii++)
        {
            String cellGroupName = cellGroupNames.get(ii);

            logger.logComment("***  Looking at cell group number " + ii + ", called: " +
                              cellGroupName);

            if(project.generatedCellPositions.getNumberInCellGroup(cellGroupName)>0)
            {
                String cellTypeName = project.cellGroupsInfo.getCellType(cellGroupName);
                Cell cell = project.cellManager.getCell(cellTypeName);

                ArrayList<String> chanMechsAll = cell.getAllChanMechNames(false);

                //boolean foundFirstPassi

                for (int j = 0; j < chanMechsAll.size(); j++)
                {
                    //ChannelMechanism nextChanMech = chanMechNames.get(j);
                    String nextChanMechName = chanMechsAll.get(j);
                    
                    logger.logComment("Cell in group " + cellGroupName + " needs channel mech: " + nextChanMechName);
                    
                    //String chanMechNameToUse = nextChanMech.getName();
                    
                    if (!includedChanMechNames.contains(nextChanMechName) &&
                        project.generatedCellPositions.getNumberInCellGroup(cellGroupName) > 0)
                    {
                        CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(nextChanMechName);

                        if (cellMech == null)
                        {
                            throw new PsicsException("Problem including cell mech: " + nextChanMechName);

                        }

                        if ( (cellMech.getMechanismType().equals(CellMechanism.CHANNEL_MECHANISM) ||
                              cellMech.getMechanismType().equals(CellMechanism.ION_CONCENTRATION)))
                        {
                            File newMechFile = new File(ProjectStructure.getPsicsCodeDir(project.getProjectMainDirectory()),
                                                           cellMech.getInstanceName() + ".xml");

                            boolean success = false;

                            if (cellMech instanceof AbstractedCellMechanism)
                            {
                                success = false;
                            }
                            else if (cellMech instanceof ChannelMLCellMechanism)
                            {
                                ChannelMLCellMechanism cmlcm =  (ChannelMLCellMechanism) cellMech;

                                success = cmlcm.createImplementationFile(SimEnvHelper.PSICS,
                                    UnitConverter.GENESIS_SI_UNITS,
                                    newMechFile,
                                    project,
                                    false,
                                    false,
                                    false,
                                    false);
                            }

                            if (!success)
                            {
                                throw new PsicsException("Problem generating file for cell mech: "
                                                           + nextChanMechName
                                                           +
                                                           "\nPlease ensure there is an implementation for that mechanism in PSICS");

                            }

                            //response.append("include " + getFriendlyDirName(dir) + cellMech.getInstanceName() + "\n");
                            //response.append("make_" + cellMech.getInstanceName() + "\n\n");

                            includedChanMechNames.add(nextChanMechName);
                        }
                    }
                }
                
                
                
            }
            else
            {
                logger.logComment("No cells in group, ignoring the channels...");
            }

        }
        return response.toString();
    }
    
    
    



    public static void main(String[] args)
    {

        try
        {

            //Project p = Project.loadProject(new File("projects/Moro/Moro.neuro.xml"), null);
            Project p = Project.loadProject(new File(ProjectStructure.getnCExamplesDir(), "Ex7_PSICSDemo/Ex7_PSICSDemo.ncx"), null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);
            
            while(pm.isGenerating())
            {
                Thread.sleep(200);
            }
            System.out.println("Num cells generated: "+ p.generatedCellPositions.getAllPositionRecords().size());
            
            
            PsicsFileManager gen = new PsicsFileManager(p);


            gen.generateThePsicsFiles(p.simConfigInfo.getDefaultSimConfig(), 12345);

            gen.runFile(true, false, true, false);
        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
