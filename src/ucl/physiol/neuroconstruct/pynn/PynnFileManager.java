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

package ucl.physiol.neuroconstruct.pynn;

import java.io.*;
import java.util.*;


import ucl.physiol.neuroconstruct.neuroml.NetworkMLConstants;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLException;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;


/**
 * Main file for generating the PyNN files
 *
 * @author Padraig Gleeson
 *  
 */

public class PynnFileManager
{
    private static ClassLogger logger = new ClassLogger("PynnFileManager");

    Project project = null;

    File mainFile = null;

    int randomSeed = 0;
    
    public enum PynnSimulator 
    {
        NEURON  ("NEURON", "neuron"),
        NEST2   ("NEST 2", "nest2"),
        PCSIM   ("PCSIM", "pcsim"),
        BRIAN   ("Brian", "brian"),
        PYMOOSE ("PyMOOSE", "pymoose");
        
        
        public final String moduleName;
        public final String name;
        
        PynnSimulator(String name, String packageName)
        {
            this.name = name;
            this.moduleName = packageName;
        }
        
        @Override
        public String toString()
        {
            return name;
        }
    }

    /**
     * The time last taken to generate the main files
     */
    private float genTime = -1;

    boolean mainFileGenerated = false;

    ArrayList<String> cellTemplatesGenAndIncl = new ArrayList<String>();
        
    ArrayList<String> includedChanMechNames = new ArrayList<String>();
    
    String theOneCellGroup = null;
    
    PynnSimulator simulator = null;


    private Hashtable<String, Integer> nextColour = new Hashtable<String, Integer>();

    //private static boolean addComments = true;



    ArrayList<String> graphsCreated = new ArrayList<String>();

    SimConfig simConfig = null;
    
        
    //private boolean quitAfterRun = false;
        
    static
    {
        logger.setThisClassVerbose(false);
    }


    private PynnFileManager()
    {
    }


    public PynnFileManager(Project project)
    {
        this.project = project;
    }
    
    
   


    public void reset()
    {
        cellTemplatesGenAndIncl = new ArrayList<String>();
        graphsCreated = new ArrayList<String>();
        nextColour = new Hashtable<String, Integer>(); // reset it...
        includedChanMechNames = new ArrayList<String>();

    }
    



    public void generateThePynnFiles(SimConfig simConfig,
                                     PynnSimulator simulator,
                                     int seed) throws PynnException, IOException
    {
        logger.logComment("Starting generation of the files...");

        long generationTimeStart = System.currentTimeMillis();
        
        this.simConfig = simConfig;
        
        this.simulator = simulator;


        this.removeAllPreviousFiles();


        randomSeed = seed;

        // Reinitialise the neuroConstruct rand num gen with the neuroConstruct seed


        FileWriter fw = null;
        nextColour = new Hashtable<String, Integer>(); // reset it...
        
        if (project.generatedCellPositions.getNumberInAllCellGroups()==0)
        {
            GuiUtils.showErrorMessage(logger, "Please generate a network containing at least one " +
                    "cell before generating the PyNN scripts", null, null);
            return;
        }
        
        

        try
        {

            File dirForPynnFiles = ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory());


            mainFile = new File(dirForPynnFiles, project.getProjectName() + ".py");

            logger.logComment("generating: "+ mainFile);
            

            File networkFile = new File(dirForPynnFiles, NetworkMLConstants.DEFAULT_NETWORKML_FILENAME_XML);

            File pyNmlUtils = ProjectStructure.getPythonNeuroMLUtilsDir(project.getProjectMainDirectory());

            File pynnUtils = ProjectStructure.getPynnUtilsDir(project.getProjectMainDirectory());

            File toDir1 = new File(dirForPynnFiles, pyNmlUtils.getName());

            GeneralUtils.copyDirIntoDir(pyNmlUtils, toDir1, true, true);

            File toDir2 = new File(dirForPynnFiles, pynnUtils.getName());
            GeneralUtils.copyDirIntoDir(pynnUtils, toDir2, true, true);
            
            try
            {
                
                ProjectManager.saveNetworkStructureXML(project, 
                                                       networkFile,
                                                       false,
                                                       false,
                                                       simConfig.getName(),
                                                       NetworkMLConstants.UNITS_PHYSIOLOGICAL);
            }
            catch (NeuroMLException ex1)
            {
                GuiUtils.showErrorMessage(logger, "Problem saving network in NeuroML XML file: "+networkFile , ex1, null);
                return;
            }
            
            
            fw = new FileWriter(mainFile);

            fw.write(getFileHeader());
            
            fw.write("simulator = '"+simulator.moduleName+"'\n\n");
            
            fw.write("print \'Generating PyNN script for simulator: \'+simulator\n\n");
            
            
            
            fw.write("try:\n");
            fw.write("    exec(\"from pyNN.%s import *\" % simulator)\n");
            fw.write("except ImportError:\n");
            fw.write("    print \'There was a problem importing the module: pyNN.%s\' % simulator\n");
            fw.write("    print \'Please make sure the PyNN implementation of %s is correctly installed\' % simulator\n");
            fw.write("    exit()\n\n");
            
            
            
            fw.write("import sys\n");
            fw.write("import xml\n");
            fw.write("import logging\n\n");
            
            fw.write("sys.path.append(\"NeuroMLUtils\")\n");
            fw.write("sys.path.append(\"PyNNUtils\")\n\n");
            
            fw.write("from NetworkHandler import NetworkHandler\n");
            fw.write("from NetworkMLSaxHandler import NetworkMLSaxHandler\n");
            fw.write("from PyNNUtils import NetManagerPyNN\n\n");
            
            fw.write("netFileName = '"+networkFile.getName()+"'\n");

            fw.write("logging.basicConfig(level=logging.INFO, format=\"%(name)-19s %(levelname)-5s - %(message)s\")\n");


            fw.write("print(\"Going to read contents of a NetworkML file: \"+netFileName)\n");


            fw.write("parser = xml.sax.make_parser()   # A parser for any XML file\n");

            fw.write("nmlHandler = NetManagerPyNN(simulator)	# Stores (most of) the network structure\n");

            fw.write("curHandler = NetworkMLSaxHandler(nmlHandler) # The SAX handler knows of the structure of NetworkML and calls appropriate functions in NetworkHandler\n");

            fw.write("curHandler.setNodeId(-1) 	# Flags to handle cell info for all nodes, as opposed to only cells with a single nodeId >=0\n");

            fw.write("parser.setContentHandler(curHandler) # Tells the parser to invoke the NetworkMLSaxHandler when elements, characters etc. parsed\n");

            fw.write("parser.parse(open(netFileName)) # The parser opens the file and ultimately the appropriate functions in NetworkHandler get called\n");


            fw.write("print(\"Have read in contents of file: \"+netFileName)\n");
            
            
            
            
            
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
                throw new PynnException("Error creating file: " + mainFile.getAbsolutePath()
                                          + "\n"+ ex.getMessage()+ "\nEnsure the PyNN files you are trying to generate are not currently being used", ex1);
            }
            throw new PynnException("Error creating file: " + mainFile.getAbsolutePath()
                                      + "\n"+ ex.getMessage()+ "\nEnsure the PyNN files you are trying to generate are not currently being used", ex);

        }

        this.mainFileGenerated = true;

        long generationTimeEnd = System.currentTimeMillis();
        genTime = (float) (generationTimeEnd - generationTimeStart) / 1000f;

        logger.logComment("... Created Main Pynn file: " + mainFile
                +" in "+genTime+" seconds. ");
        

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

        File codeDir = ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory());

        GeneralUtils.removeAllFiles(codeDir, false, true, true);


    }


    public static String getFileHeader()
    {
        StringBuffer response = new StringBuffer();
        response.append("'''\n");
        response.append("******************************************************\n");
        response.append("\n");
        response.append("     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append(" \n");
        response.append("******************************************************\n");

        response.append("'''\n\n");
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






    public void runFile(boolean copyToSimDataDir) throws PynnException
    {
        logger.logComment("Trying to run the mainFile...");

        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new PynnException("PyNN files not yet generated");
        }


        File dirForSimDataFiles = getDirectoryForSimulationFiles();
        File dirToRunFrom = null;
        File genDir = ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory());
        
        if (copyToSimDataDir)
        {
            dirToRunFrom = dirForSimDataFiles;
            
            try
            {
                GeneralUtils.copyDirIntoDir(genDir, dirForSimDataFiles, true, true);
            }
            catch (Exception e)
            {
                throw new PynnException("Problem copying the Pynn files from "+genDir+" to "+ dirForSimDataFiles, e);
                
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
            throw new PynnException("PyNN files cannot be run in a directory like: "+ dirToRunFrom
                    + " containing spaces.\nThis is due to the way neuroConstruct starts the external processes (e.g. konsole) to run PyNN.\n" +
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


        
        Runtime rt = Runtime.getRuntime();
        String commandToExecute = null;

        try
        {
            //String executable = null;
            
            String pyEx = "python -i";
            if (simulator.equals(PynnSimulator.NEURON))
            {
                pyEx = "nrniv -python";
            }
            
            File fullFileToRun = new File(dirToRunFrom, mainFile.getName());
            
            
            
            String title = "PyNN_simulation" + "___" + project.simulationParameters.getReference();
            

            if (GeneralUtils.isWindowsBasedPlatform())
            {
                logger.logComment("Assuming Windows environment...");

                String setDir = "";
                //String setDir = " -Duser.dir="+dirToRunFrom.getAbsolutePath();
                
                commandToExecute = "cmd /K start \""+title+"\"  " +  pyEx +setDir+" "+fullFileToRun;

                logger.logComment("Going to execute command: " + commandToExecute);

                rt.exec(commandToExecute);

                logger.logComment("Have executed command: " + commandToExecute/*+" in woriking dir: "+ dirToRunFrom*/, true);

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
                String noClose = " --noclose";

                if (basicCommLine.indexOf("konsole")>=0)
                {
                    logger.logComment("Assume we're using KDE");
                    titleOption = " -T="+title;
                    workdirOption = " --workdir="+ dirToRunFrom.getAbsolutePath();
                    extraArgs = "-e ";
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

                String scriptText = pyEx+ " "+fullFileToRun;

                File scriptFile = new File(ProjectStructure.getPynnCodeDir(project.getProjectMainDirectory()),
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
                    + noClose
                    + " "
                    + titleOption
                    + " "
                    + workdirOption
                    + " "
                    + extraArgs
                    + " " +
                    scriptFile.getAbsolutePath();



                logger.logComment("Going to execute command: " + commandToExecute);

                rt.exec(commandToExecute);

                logger.logComment("Have successfully executed command: " + commandToExecute);
                
               
            }
            
            
        }
        catch (Exception ex)
        {
            logger.logError("Error running the command: " + commandToExecute);
            throw new PynnException("Error executing the PyNN file: " + mainFile, ex);
        }
    }



    public String getMainPyNNFileName() throws PynnException
    {
        if (!this.mainFileGenerated)
        {
            logger.logError("Trying to run without generating first");
            throw new PynnException("PyNN file not yet generated");
        }

        return this.mainFile.getAbsolutePath();

    }
    
    
    



    public static void main(String[] args)
    {

        try
        {

            //Project p = Project.loadProject(new File("projects/Moro/Moro.neuro.xml"), null);
            Project p = Project.loadProject(new File("models/PyNNTest/PyNNTest.neuro.xml"), null);
            //Proje
            ProjectManager pm = new ProjectManager(null,null);
            pm.setCurrentProject(p);

            pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);
            
            while(pm.isGenerating())
            {
                Thread.sleep(200);
            }
            System.out.println("Num cells generated: "+ p.generatedCellPositions.getAllPositionRecords().size());
            
            PynnFileManager gen = new PynnFileManager(p);


            gen.generateThePynnFiles(p.simConfigInfo.getDefaultSimConfig(), PynnFileManager.PynnSimulator.NEST2, 12345);
        
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
