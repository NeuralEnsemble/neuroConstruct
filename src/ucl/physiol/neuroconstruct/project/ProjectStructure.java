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

import java.io.File;
import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;



/**
 * Class defining, and if necessary creating, project directories, etc.
 *
 * @author Padraig Gleeson
 *  
 */

public class ProjectStructure
{
    private static ClassLogger logger = new ClassLogger("ProjectStructure");


    static
    {
        logger.setThisClassSilent(true);
    }
    
    
    private static final String updateCheckUrl = "http://www.physiol.ucl.ac.uk/research/silver_a/nCinfo/form.php?myversion=";

    private static final String projectFileExtension = new String(".neuro.xml");

    private static final String morphFileExtension = new String(".morph.xml");

    private static final String neuromlExtension = new String(".nml");

    private static final String neuromlCompExtension = new String(".zip");


    public static final String JAVA_XML_FORMAT = new String("Java XML Format");
    public static final String JAVA_OBJ_FORMAT = new String("Java Serialized Object Format");


    private static final String javaXMLFileExtension = new String(".java.xml");

    private static final String javaObjFileExtension = new String(".java.ser");

    private static final String zippedProjectFileExtension = new String(".neuro.zip");

    private static final String dataSetExtension = new String(".ds");

    private static final String zippedDataSetExtension = new String(dataSetExtension + ".zip");

    private static final String README = "README";


    private static final String userSettingsDir
        = System.getProperty("user.home")
        + System.getProperty("file.separator")
        + ".neuroConstruct";

    private static final String generalSettingsFilename = userSettingsDir
        + System.getProperty("file.separator")
        + "neuroConstruct.props";

    private static final String neuConRecentFilesFilename = userSettingsDir
        + System.getProperty("file.separator")
        + "neuroConstruct.recent";

    private static final String nmodlEditRecentFilesFilename = userSettingsDir
        + System.getProperty("file.separator")
        + "nmodlEditor.recent";


    private static String examplesDir = "examples";

    private static String nCprojectsDir = "nC_projects";

    private static String importedMorphologiesDir = "importedMorphologies";

    private static String cellProcessesDir = "cellProcesses";

    private static String cellMechanismDir = "cellMechanisms";

    private static String fileBasedCellProcessesDir = "native";

    private static String morphologiesDir = "morphologies";

    private static String simulationsDir = "simulations";

    private static String genesisCodeDir = "generatedGENESIS";

    private static String neuronCodeDir = "generatedNEURON";

    private static String neosimCodeDir = "generatedNEOSIM";

    private static String neuroMLDir = "generatedNeuroML";

    private static String pythonNeuroMLDir = "pythonNeuroML";

    public static String neuroMLPyUtilsDir = "NeuroMLUtils";
    
    public static String neuronPyUtilsDir = "NEURONUtils";
    

    private static String savedNetworksDir = "savedNetworks";


    private static String dataSetsDir = "dataSets";

    private static String modTemplatesDir = "templates"
        + System.getProperty("file.separator")
        + "modFileTemplates";

    private static String genesisTemplatesDir = "templates"
        + System.getProperty("file.separator")
        + "genesisTemplates";

    private static String genesisUtilsFile = "templates"
        + System.getProperty("file.separator")
        + "genesisUtils"
        + System.getProperty("file.separator")
        + "nCtools.g";

    private static String neuronUtilsFile = "templates"
        + System.getProperty("file.separator")
        + "neuronUtils"
        + System.getProperty("file.separator")
        + "nCtools.hoc";

    private static String xmlTemplatesDir = "templates"
        + System.getProperty("file.separator")
        + "xmlTemplates";

    private static String xmlSchemasDir = xmlTemplatesDir
        + System.getProperty("file.separator")
        + "Schemata";

    private static String xmlExamplesDir = xmlTemplatesDir
        + System.getProperty("file.separator")
        + "Examples";


    private static String cmlSchemasDir = xmlSchemasDir
        + System.getProperty("file.separator")
        + "v"+GeneralProperties.getNeuroMLVersionNumber()
        + System.getProperty("file.separator")
        +"Level2";


    private static String cmlExamplesDir = xmlExamplesDir
        + System.getProperty("file.separator")
        +"ChannelML";





    private static String cmlTemplatesDir = xmlTemplatesDir
        + System.getProperty("file.separator")
        + "ChannelMLPrototypes";



    private static String matlabOctaveDir = "matlabOctave";

    private static String globalMatlabOctaveDir = matlabOctaveDir;

    private static String igorNeuroMaticDir = "igorNeuroMatic";

    private static String globalIgorNeuroMaticDir = igorNeuroMaticDir;




    private static String toolTipFile = "docs"
        + System.getProperty("file.separator")
        + "XML"
        + System.getProperty("file.separator")
        + "glossary"
        + System.getProperty("file.separator")
        + "Glossary.xml";

    private static String helpFileDir = "docs"
        + System.getProperty("file.separator")
        + "helpdocs"
        + System.getProperty("file.separator");

    private static String mainHelpFile = helpFileDir
        + "index.html";

    private static String helpMenuFile = helpFileDir + "docMenu.html";

    private static String importDocFile = helpFileDir + "import.html";

    private static String glossaryHtmlFile = helpFileDir + "Glossary_gen.html";
    
    private static String relNotesFile = helpFileDir + "RELEASE_NOTES";
    

   // private static String neuronModSourcesDir = "src"
    //    + System.getProperty("file.separator")
     //   + "nrnoc";

    private static File javaXMLToMorphMLFile = new File("morphml"
                                                        + System.getProperty("file.separator")
                                                        + "JavaXMLToMorphML.xsl");

    private static File morphMLToJavaXMLFile = new File("morphml"
                                                        + System.getProperty("file.separator")
                                                        + "MorphMLToJavaXML.xsl");

    /**
     * Gets a File for the directory dirName, and if it's not there already, creates it
     */
    private static File getDirectoryInProject(File projectDir,
                                              String dirName,
                                              String readme)
    {
        return getDirandReadme(projectDir,
                               dirName,
                               readme,
                               true);

    }

    /**
     * @return 0 if versions identical, 1 if currAppVersion is more recent than projFileVersion,
     * -1 if projFileVersion is more recent than currAppVersion, or if either version string isn't parsable
     */
    public static int compareVersions(String currAppVersion, String projFileVersion)
    {
        if (currAppVersion.equals(projFileVersion)) return 0;

        String[] appVerNums = currAppVersion.split("\\.");
        String[] projVerNums = projFileVersion.split("\\.");

        try
        {
            for (int i = 0; i < Math.min(appVerNums.length, projVerNums.length); i++)
            {
                if (Integer.parseInt(appVerNums[i]) > Integer.parseInt(projVerNums[i])) return 1;
                if (Integer.parseInt(appVerNums[i]) < Integer.parseInt(projVerNums[i])) return -1;

            }

            // passed all the tests...

            if (appVerNums.length < projVerNums.length) return -1; // i.e. app = 1.2 proj = 1.2.1
            if (appVerNums.length > projVerNums.length) return 1; // i.e. app = 1.2.1 proj = 1.2
        }
        catch (Exception ex)
        {
            return -1;

        }

        return -1;

    }


    public static File getDirForSimFiles(String simRef, Project project)
    {
        File dirForSims = ProjectStructure.getSimulationsDir(project.getProjectMainDirectory());

        File dirForDataFiles = new File (dirForSims, simRef);

        if (!dirForDataFiles.isDirectory() || !dirForDataFiles.exists())
        {
            dirForDataFiles.mkdir();
        }
        return dirForDataFiles;
    }



    private static File getDirandReadme(File parentDir,
                                        String newDirName,
                                        String readmeText,
                                        boolean createIfNotFound)
    {


        File newDir = new File(parentDir, newDirName);

        if (!newDir.exists())
        {
            if (!createIfNotFound)
            {
                return null;
            }
            newDir.mkdir();
        }
        File readmeFile = new File(newDir, README);

        if (readmeText!=null && !readmeFile.exists())
        {
            try
            {
                FileWriter fwReadme = new FileWriter(readmeFile);
                fwReadme.write(readmeText);
                fwReadme.close();
            }
            catch (IOException ex)
            {
                logger.logError("Exception creating readme file...");
            }
        }

        return newDir;
    }



    public static String getGeneralSettingsFilename()
    {
        return generalSettingsFilename;
    }

    public static String getToolTipFile()
    {
        return toolTipFile;
    }


    public static String getGlossaryHtmlFile()
    {
        return glossaryHtmlFile;
    }
    
    public static String getUpdateCheckUrl()
    {
        return updateCheckUrl;
    }


    public static String getMainHelpFile()
    {
        return mainHelpFile;
    }


    public static String getRelNotesFile()
    {
        return relNotesFile;
    }


    public static String getHelpMenuFile()
    {
        return helpMenuFile;
    }
    
    
    public static String getHelpImportFile()
    {
        return importDocFile;
    }



    public static String getNeuronUtilsFile()
    {
        return neuronUtilsFile;
    }


    public static String getGenesisUtilsFile()
    {
        return genesisUtilsFile;
    }

    public static File getJavaXMLToMorphMLFile()
    {
        return javaXMLToMorphMLFile;
    }


    public static File getMorphMLToJavaXMLFile()
    {
        return morphMLToJavaXMLFile;
    }



    public static File getExamplesDirectory()
    {
        File examplesDirectory = new File(examplesDir); // create relative to working dir, which should be c:\neuroConstruct

        if (!examplesDirectory.exists())
        {
            examplesDirectory.mkdir();
        }
        return examplesDirectory;
    }




    public static File getDefaultnCProjectsDir()
    {

        File homeDir = new File(System.getProperty("user.home"));

        File dir = getDirandReadme(homeDir,
                                   nCprojectsDir,
                                   "This is the default directory for a user's new neuroConstruct projects. The default location to use can be changed through the GUI\n",
                                   true);
        

        logger.logComment("Just defined nCprojectsDir: "+ dir);

        return dir;

    }




    public static File getNeuronCodeDir(File projectDir)
    {
        File dirForNeuronFiles
            = getDirectoryInProject(projectDir,
                                    neuronCodeDir,
                                    "This is the directory for the NEURON files generated by neuroConstruct for this project.\n"
                                    +"Note, when a simulation is run, a directory will be created in ../simulations for the results.");

        return dirForNeuronFiles;
    }


    public static File getGenesisCodeDir(File projectDir)
    {
        File dirForGenesisFiles
            = getDirectoryInProject(projectDir, genesisCodeDir,
                                    "This is the directory for the GENESIS files generated by neuroConstruct for this project.\n"
                                    +"Note, when a simulation is run, a directory will be created in ../simulations for the results.");

        return dirForGenesisFiles;
    }


    public static File getNeosimCodeDir(File projectDir)
    {
        return getDirectoryInProject(projectDir, neosimCodeDir,
                                    "This is the directory for the NeoSim files generated by neuroConstruct for this project.\n");
    }

    public static File getNeuroMLDir(File projectDir)
    {
        return getDirectoryInProject(projectDir, neuroMLDir,
                                    "This is the directory for the NeuroML files generated by neuroConstruct for this project.\n");
    }
    

    public static File getPythonNeuroMLDir(File projectDir)
    {
        return getDirandReadme(new File("."), 
                               pythonNeuroMLDir, 
                               "This is the directory for some Python utility files.\n", 
                               true);
    }
    public static File getPythonNeuroMLUtilsDir(File projectDir)
    {
        return getDirandReadme(getPythonNeuroMLDir(projectDir), 
                                neuroMLPyUtilsDir,
                                "This is the directory for some NeuroML/Python utility files.\n",
                                true);
    }
    public static File getPythonNeuronUtilsDir(File projectDir)
    {
        return getDirandReadme(getPythonNeuroMLDir(projectDir), 
                                neuronPyUtilsDir,
                                "This is the directory for some Python/NEURON utility files.\n",
                                true);
    }
    


    public static File getSavedNetworksDir(File projectDir)
    {
        return getDirectoryInProject(projectDir, savedNetworksDir,
                                    "This is the directory for files describing cell placement/network connections in NetworkML format which have been saved in this project.\n");
    }





    public static File getDataSetsDir(File projectDir)
    {
        File dataSetDir = getDirectoryInProject(projectDir, dataSetsDir,
            "This is the directory for storing interesting dataSets associated with the project. "
            +"These can be plots illustrating points which have been made in the accompanying papers, for example");
        return dataSetDir;
    }



    public static File getImportedMorphologiesDir(File projectDir, boolean createIfNotFound)
    {
        File impMorphDir = getDirandReadme(projectDir, importedMorphologiesDir,
                                                 "This is the directory where imported morphology files,"
                         +" (e.g. in GENESIS, NEURON format) are stored. Note, these aren't reloaded each time,"
                         +" the file is converted to neuroConstruct format and saved that way.\n",
                         createIfNotFound);

        return impMorphDir;

    }


    public static File getFileBasedCellProcessesDir(File projectDir, boolean createIfNotFound)
    {
        logger.logComment("Getting FileBasedCellProcessesDir");

        // Update from older dir structure...
        File oldImportedCellProcessDir = new File(projectDir, "importedCellProcesses");
        File cellProcDir = getCellProcessesDir(projectDir,  createIfNotFound);

        if (cellProcDir==null) return null;

        File fileBasedCPDir = new File(cellProcDir, fileBasedCellProcessesDir);

        logger.logComment("Updating "+oldImportedCellProcessDir +" to "+ fileBasedCPDir);

        if (oldImportedCellProcessDir.exists())
        {
            oldImportedCellProcessDir.renameTo(fileBasedCPDir);
        }

        fileBasedCPDir = getDirandReadme(cellProcDir, fileBasedCellProcessesDir,
                                                    "This is the directory where Cell Processes which have implementations in a single native environment files (e.g. *.mod for NEURON) are placed\n",
                                                    createIfNotFound);

        logger.logComment("Returning FileBasedCellProcessesDir: "+ fileBasedCPDir);
        return fileBasedCPDir;
    }


/*
    public static File getCellProcessesDir(File projectDir)
    {
        //System.out.println("---- getCellProcessesDir ...");
        return getCellProcessesDir(projectDir, true);
    }
*/



    public static File getCellProcessesDir(File projectDir, boolean createIfNotFound)
    {
        logger.logComment("---- getCellProcessesDir, createIfNotFound: "+ createIfNotFound);


        File cellProcDir = getDirandReadme(projectDir,
                                           cellProcessesDir,
                                           "This is the directory where Cell Processes files **used to be** stored, "
                                           + "either the native environment (e.g. *.mod) files, or ChannelML based files. "
                                           + "\n\nNow, a restructured cellMechanism directory will be used.\n",
                                           createIfNotFound);

        logger.logComment("Returning CellProcessesDir: "+ cellProcDir);
        return cellProcDir;
    }



    public static File getCellMechanismDir(File projectDir)
    {
        return getCellMechanismDir(projectDir, true);
    }

    public static File getDirForCellMechFiles(Project project, String cellMechName)
    {
        File projDir = project.getProjectMainDirectory();

        File dir = ProjectStructure.getCellMechanismDir(projDir, false);

        if (dir != null && dir.exists())
        {
            return new File(dir, cellMechName);
        }
        else
            // old method...
            return new File(ProjectStructure.getCellProcessesDir(projDir, false),
                            cellMechName);
    }



    public static File getGlobalMatlabOctaveDir()
    {
        File pwd = (new File("")).getAbsoluteFile();

        File dir = getDirandReadme(pwd,
                                   globalMatlabOctaveDir,
                                   "Any *.m files found in this directory will be automatically included in the Matlab/Octave file generated when a simulation is run.\n",
                                   true);

        return dir;


    }

    public static File getGlobalIgorNeuroMaticDir()
    {
        File pwd = (new File("")).getAbsoluteFile();

        File dir = getDirandReadme(pwd,
                                   globalIgorNeuroMaticDir,
                                   "Files found in this directory will be automatically included in the Igor/NeuroMatic file generated when a simulation is run.\n",
                                   true);

        return dir;


    }



    public static File getCellMechanismDir(File projectDir, boolean createIfNotFound)
    {
        logger.logComment("Getting CellMechanismDir");


        File cellMechDir = getDirandReadme(projectDir, cellMechanismDir,
                                                    "This is the directory where Cell Mechanism (channel, synapse, etc) files are stored, either the native environment (e.g. *.mod) files, or ChannelML based files.\n"+
               "Unlike the older cellProcesses dir, simply cutting and pasting these cell mech directories into another neuroConstruct project is sufficient for copying the cell mech to that project.\n",
        createIfNotFound);



        logger.logComment("Returning CellMechanismDir: "+ cellMechDir);
        return cellMechDir;
    }


    public static File getMorphologiesDir(File projectDir)
    {
        File morphDir = new File(projectDir, morphologiesDir);
        // Update from older dir structure...
        File oldProjMorphDir = new File(projectDir, "projectMorphologies");


        logger.logComment("Updating "+oldProjMorphDir +" to "+ morphDir);

        if (oldProjMorphDir.exists())
        {
            oldProjMorphDir.renameTo(morphDir);
        }
        
        String details = ToolTipHelper.getInstance().getToolTip("Morphology save format", false);

        morphDir = getDirectoryInProject(projectDir, morphologiesDir,
                                         details);

        return morphDir;
    }


    public static File getSimulationsDir(File projectDir)
    {
        File simDir = getDirectoryInProject(projectDir, simulationsDir,
                                            "This is the directory for the simulation data associated with the project\n");


        return simDir;
    }


    public static File getProjMatlabOctaveDir(File projectDir, boolean createIfNotFound)
    {
        File dir = getDirandReadme(projectDir,
                                   matlabOctaveDir,
                                   "Any *.m files found in this directory will be automatically included in the Matlab/Octave file generated when a simulation is run.\n",
                                   createIfNotFound);

        return dir;
    }


    public static String getProjMatlabOctaveDirName(File projectDir)
    {
        File dir = new File(projectDir,
                                   matlabOctaveDir); // may or may not exist...

        return dir.getAbsolutePath();
    }



    public static File getProjIgorNeuroMaticDir(File projectDir, boolean createIfNotFound)
    {
        File dir = getDirandReadme(projectDir,
                                   igorNeuroMaticDir,
                                   "Files found in this directory will be automatically included in the Igor/NeuroMatic file generated when a simulation is run.\n",
                                   createIfNotFound);

        return dir;
    }


    public static String getProjIgorNeuroMaticName(File projectDir)
    {
        File dir = new File(projectDir,
                                   igorNeuroMaticDir); // may or may not exist...

        return dir.getAbsolutePath();
    }




    public static File getModTemplatesDir()
    {
        return new File(modTemplatesDir);
    }

    public static File getXMLTemplatesDir()
    {
        return new File(xmlTemplatesDir);
    }

    public static File getCMLTemplatesDir()
    {
        return new File(cmlTemplatesDir);
    }


    public static File getCMLSchemasDir()
    {
        return new File(cmlSchemasDir);
    }



    public static File getCMLExamplesDir()
    {
        return new File(cmlExamplesDir);
    }





    public static File getGenesisTemplatesDir()
    {
        return new File(genesisTemplatesDir);
    }



    public static String getNeuConRecentFilesFilename()
    {
        return neuConRecentFilesFilename;
    }


    public static String getNmodlEditRecentFilesFilename()
    {
        return nmodlEditRecentFilesFilename;
    }

    /**
     * Just in case I decide to change the extension...
     *
     * @return The extension of files which can be loaded by the app
     */
    public static String getProjectFileExtension()
    {
        return projectFileExtension;
    }


    /**
     * Just in case I decide to change the extension...
     *
     * @return The extension of MorphML files as stored by the application
     */
    public static String getMorphMLFileExtension()
    {
        return morphFileExtension;
    }

    /**
     * @return The extension of NeuroML files as stored by the application
     */
    public static String getNeuroMLFileExtension()
    {
        return neuromlExtension;
    }

    /**
     * @return The extension of zipped NeuroML files as stored by the application
     */
    public static String getNeuroMLCompressedFileExtension()
    {
        return neuromlCompExtension;
    }




    /**
     * Just in case I decide to change the extension...
     *
     * @return The extension of Java XML files as stored by the application
     */
    public static String getJavaXMLFileExtension()
    {
        return javaXMLFileExtension;
    }

    /**
     * @return The extension of Java serialized object files as stored by the application
     */
    public static String getJavaObjFileExtension()
    {
        return javaObjFileExtension;
    }




    /**
     * Just in case I decide to change the extension...
     *
     * @return The extension of the zip file created when a project is zipped
     */
    public static String getProjectZipFileExtension()
    {
        return zippedProjectFileExtension;
    }

    /**
     * Just in case I decide to change the extension...
     *
     * @return The extension of a file storing a data set
     */
    public static String getDataSetExtension()
    {
        return dataSetExtension;
    }

    /**
     * Just in case I decide to change the extension...
     *
     * @return The extension of a file storing a zipped up data set
     */
    public static String getZippedDataSetExtension()
    {
        return zippedDataSetExtension;
    }


}
