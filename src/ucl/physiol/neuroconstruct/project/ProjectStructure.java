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
import ucl.physiol.neuroconstruct.gui.SimpleFileFilter;
import ucl.physiol.neuroconstruct.utils.*;



/**
 * Class defining, and if necessary creating, project directories, etc.
 *
 * @author Padraig Gleeson
 *  
 */

public class ProjectStructure
{
    static
    {
        //logger.setThisClassVerbose(true);
    }
   
    public ProjectStructure()
    {
    }

    //private static ClassLogger logger = new ClassLogger("ProjectStructure");
    
    private static final String updateCheckUrl = "http://www.neuroConstruct.org/nCinfo/form.php?myversion=";

    private static final String oldProjectFileExtension = ".neuro.xml";
    
    private static final String newProjectFileExtension = ".ncx";

    private static final String morphFileExtension = ".morph.xml";

    private static final String xmlExtension = ".xml";

    private static final String neuromlExtension = ".nml";
    
    private static final String hdf5Extension = ".h5";

    private static final String neuromlCompExtension = ".zip";


    public static final String JAVA_XML_FORMAT = "Java XML Format";
    public static final String JAVA_OBJ_FORMAT = "Java Serialized Object Format";
    public static final String NEUROML1_FORMAT = "NeuroML v1.8.1 Format";
    
    
    
    public static final String NC_HOME_ENV_VAR = "NC_HOME";

    public static final String PROJ_QUICK_SAVE = "Quick Save";
    public static final String PROJ_SVN_SAVE = "SVN friendly save";


    private static final String javaXMLFileExtension = ".java.xml";

    private static final String javaObjFileExtension = ".java.ser";

    private static final String oldZippedProjectFileExtension = ".neuro.zip";

    private static final String newZippedProjectFileExtension = ".ncx.zip";

    private static final String dataSetExtension = ".ds";

    private static final String zippedDataSetExtension = dataSetExtension + ".zip";

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



    private static String nCprojectsDir = "nC_projects";

    private static String importedMorphologiesDir = "importedMorphologies";

    private static String cellProcessesDir = "cellProcesses";

    private static String cellMechanismDir = "cellMechanisms";

    private static String fileBasedCellProcessesDir = "native";

    private static String morphologiesDir = "morphologies";

    private static String simulationsDir = "simulations";

    private static String genesisCodeDir = "generatedGENESIS";

    private static String mooseCodeDir = "generatedMOOSE";
    
    private static String psicsCodeDir = "generatedPSICS";
    
    private static String pynnCodeDir = "generatedPyNN";

    private static String neuronCodeDir = "generatedNEURON";

    private static String neosimCodeDir = "generatedNEOSIM";

    private static String neuroMLDir = "generatedNeuroML";
    
    private static String savedNetworksDir = "savedNetworks";

    private static String dataSetsDir = "dataSets";

    private static String pythonScripts = "pythonScripts";
    
    
    //private static String examplesDirInInstall = "examples";
    
    private static String nCexamplesDirInInstall = "osb/showcase/neuroConstructShowcase";
    //private static String nCmodelsDirInInstall = "nCmodels";
    private static String osbDir = "osb";

    private static String pythonNeuroMLDir = "pythonNeuroML";
    
    public static String neuroMLPyUtilsDir = "NeuroMLUtils";
    
    public static String neuronPyUtilsDir = "NEURONUtils";
    
    public static String pynnUtilsDir = "PyNNUtils";
    

    private static String genesisWinScript = "templates\\genesisUtils\\startxwin2.bat";

    private static String modTemplatesDirInInstall = "templates"
        + System.getProperty("file.separator")
        + "modFileTemplates";

    private static String genesisTemplatesDirInInstall = "templates"
        + System.getProperty("file.separator")
        + "genesisTemplates";

    private static String genesisUtilsFileInInstall = "templates"
        + System.getProperty("file.separator")
        + "genesisUtils"
        + System.getProperty("file.separator")
        + "nCtools.g";

    private static String neuronUtilsFileInInstall = "templates"
        + System.getProperty("file.separator")
        + "neuronUtils"
        + System.getProperty("file.separator")
        + "nCtools.hoc";

    private static String neuronUtilsWinModCompile = "templates"
        + System.getProperty("file.separator")
        + "neuronUtils"
        + System.getProperty("file.separator")
        + "mknrndll.sh";
    
    private static String neuronCellCheckFileInInstall = "templates"
        + System.getProperty("file.separator")
        + "neuronUtils"
        + System.getProperty("file.separator")
        + "cellCheck.hoc";

    private static String xmlTemplatesDirInInstall = "templates"
        + System.getProperty("file.separator")
        + "xmlTemplates";



    private static String cmlTemplatesDirInInstall = xmlTemplatesDirInInstall
        + System.getProperty("file.separator")
        + "ChannelMLPrototypes";

    private static String sbmlNeuronMappingDir = "templates"
        + System.getProperty("file.separator")+"SBML2NEURON";

    private static String sbmlNeuronMappingFile = "SBML2NEURON.py";

    private static String nml2Dir = "NeuroML2";

    private static String channelml2Neuroml2Dir = "ChannelMLConvert";

    private static String channelml2Neuroml2alphaFile = "ChannelML2NeuroML2.xsl";
    private static String channelml2Neuroml2betaFile = "ChannelML2NeuroML2beta.xsl";


    private static final String matlabOctaveDir = "matlabOctave";

    private static String matlabOctaveDirInInstall = matlabOctaveDir;

    private static final String igorNeuroMaticDir = "igorNeuroMatic";

    private static String igorNeuroMaticDirInInstall = igorNeuroMaticDir;




    private static String toolTipFileInInstall = "docs"
        + System.getProperty("file.separator")
        + "XML"
        + System.getProperty("file.separator")
        + "glossary"
        + System.getProperty("file.separator")
        + "Glossary.xml";

    private static String helpFileDirInInstall = "docs"
        + System.getProperty("file.separator")
        + "helpdocs"
        + System.getProperty("file.separator");

    private static String mainHelpFileInInstall = helpFileDirInInstall
        + "index.html";

    private static String helpMenuFileInInstall = helpFileDirInInstall + "docMenu.html";

    private static String importDocFileInInstall = helpFileDirInInstall + "import.html";

    private static String glossaryHtmlFileInInstall = helpFileDirInInstall + "Glossary_gen.html";

    private static String relNotesFileInInstall = helpFileDirInInstall + "RELEASE_NOTES";

    private static String matplotlibEpsFilename = "generateEps.py";



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
    
    
    public static File getnCHome()
    {
        String nChome = System.getenv(NC_HOME_ENV_VAR);
        if (nChome!=null)
        {
            if (nChome.startsWith("\"")) 
                nChome = nChome.substring(1);
            if (nChome.endsWith("\"")) 
                nChome = nChome.substring(0, nChome.length()-1);
            /*
            System.out.println("nChome: "+ nChome);
            System.out.println("nChome: "+ (new File(nChome)));
            System.out.println("nChome: "+ (new File(nChome).getAbsoluteFile()));*/
            
            return new File(nChome).getAbsoluteFile();
        }
        return new File("").getAbsoluteFile(); // Use working directory
    }
    
    
    
    public static void main(String[] args)
    {
        System.out.println("nc Home: "+ getnCHome().getAbsolutePath());
        System.out.println("Gen temps: "+ getGenesisTemplatesDir());

        String lo = "1.6.0";
        String hi = "1.8.0";

        System.out.println("Comp "+ lo +" to "+hi+": "+compareVersions(lo, hi));
        System.out.println("Comp "+ hi +" to "+lo+": "+compareVersions(hi, lo));

        lo = "1.3beta";

        System.out.println("Comp "+ lo +" to "+hi+": "+compareVersions(lo, hi));
        System.out.println("Comp "+ hi +" to "+lo+": "+compareVersions(hi, lo));

    }

    /**
     * @return 0 if versions identical, 1 if currAppVersion is more recent than projFileVersion,
     * -1 if projFileVersion is more recent than currAppVersion, or if either version string isn't parsable
     */
    public static int compareVersions(String currAppVersion, String projFileVersion)
    {
        if (currAppVersion.equals(projFileVersion)) return 0;

        if (currAppVersion.endsWith("beta"))
            currAppVersion = currAppVersion.substring(0, currAppVersion.length()-4);
        if (currAppVersion.endsWith("alpha"))
            currAppVersion = currAppVersion.substring(0, currAppVersion.length()-5);

        if (projFileVersion.endsWith("beta"))
            projFileVersion = projFileVersion.substring(0, projFileVersion.length()-4);
        if (projFileVersion.endsWith("alpha"))
            projFileVersion = projFileVersion.substring(0, projFileVersion.length()-5);

        //System.out.println(currAppVersion);

        String[] appVerNums = currAppVersion.split("\\.");
        String[] projVerNums = projFileVersion.split("\\.");

        try
        {
            for (int i = 0; i < Math.min(appVerNums.length, projVerNums.length); i++)
            {
                //System.out.println("Comp "+ appVerNums[i] +" to "+projVerNums[i]);
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
                //logger.logError("Exception creating readme file...");
                // Proceed without readme...
            }
        }

        return newDir;
    }



    public static String getGeneralSettingsFilename()
    {
        return generalSettingsFilename;
    }

    public static File getToolTipFile()
    {
        return new File(getnCHome(), toolTipFileInInstall);
    }


    public static File getGlossaryHtmlFile()
    {
        return new File(getnCHome(),glossaryHtmlFileInInstall);
    }
    
    public static String getUpdateCheckUrl()
    {
        return updateCheckUrl;
    }



    public static File getMainHelpFile()
    {
        return new File(getnCHome(), mainHelpFileInInstall);
    }


    public static File getSbml2NeuronFile()
    {
        return new File(sbmlNeuronMappingDir,sbmlNeuronMappingFile);
    }

    public static File getChannelML2NeuroML2alpha()
    {
        return new File(getNeuroML2Dir(),channelml2Neuroml2Dir +"/"+channelml2Neuroml2alphaFile);
    }

    public static File getChannelML2NeuroML2beta()
    {
        return new File(getNeuroML2Dir(),channelml2Neuroml2Dir +"/"+channelml2Neuroml2betaFile);
    }


    public static File getNeuroML2Dir()
    {
        return new File(getnCHome(), nml2Dir);
    }

    public static File getRelNotesFile()
    {
        return new File(getnCHome(),relNotesFileInInstall);
    }


    public static File getHelpMenuFile()
    {
        return new File(getnCHome(),helpMenuFileInInstall);
    }
    
    
    public static File getHelpImportFile()
    {
        return new File(getnCHome(),importDocFileInInstall);
    }



    public static File getNeuronUtilsFile()
    {
        return new File(getnCHome(), neuronUtilsFileInInstall);
    }


    public static File getNeuronUtilsWinModCompileFile()
    {
        return new File(getnCHome(), neuronUtilsWinModCompile);
    }


    public static File getNeuronCellCheckFile()
    {
        return new File(getnCHome(), neuronCellCheckFileInInstall);
    }


    public static File getGenesisUtilsFile()
    {
        return new File(getnCHome(), genesisUtilsFileInInstall);
    }

    //public static File getJavaXMLToMorphMLFile()
    //{
    //    return new File(getnCHome(), javaXMLToMorphMLFile);
    //}


    //public static File getMorphMLToJavaXMLFile()
    //{
   //     return morphMLToJavaXMLFile;
    //}


/*
    public static File getExamplesDirectory()
    {
        File examplesDirectory = new File(getnCHome(), examplesDirInInstall);

        if (!examplesDirectory.exists())
        {
            examplesDirectory.mkdir();
        }
        return examplesDirectory;
    }*/

    public static File getnCExamplesDir()
    {
        File examplesDirectory = new File(getnCHome(), nCexamplesDirInInstall);

        if (!examplesDirectory.exists())
        {
            examplesDirectory.mkdir();
        }
        return examplesDirectory;
    }
    public static File getOsbProjsDir()
    {
        File osbDirectory = new File(getnCHome(), osbDir);

        return osbDirectory;
    }
    
    /*
    public static File getnCModelsDir()
    {
        File examplesDirectory = new File(getnCHome(), nCmodelsDirInInstall);

        if (!examplesDirectory.exists())
        {
            examplesDirectory.mkdir();
        }
        return examplesDirectory;
    }*/



    public static File getDefaultnCProjectsDir()
    {

        File homeDir = new File(System.getProperty("user.home"));

        File dir = getDirandReadme(homeDir,
                                   nCprojectsDir,
                                   "This is the default directory for a user's new neuroConstruct projects. The default location to use can be changed through the GUI\n",
                                   true);
        

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

    public static File getMooseCodeDir(File projectDir)
    {
        File dirForMooseFiles
            = getDirectoryInProject(projectDir, mooseCodeDir,
                                    "This is the directory for the MOOSE files generated by neuroConstruct for this project.\n"
                                    +"Note, when a simulation is run, a directory will be created in ../simulations for the results.");

        return dirForMooseFiles;
    }


    public static File getPsicsCodeDir(File projectDir)
    {
        File dirForPsicsFiles
            = getDirectoryInProject(projectDir, psicsCodeDir,
                                    "This is the directory for the PSICS files generated by neuroConstruct for this project.\n"
                                    +"Note, when a simulation is run, a directory will be created in ../simulations for the results.");

        return dirForPsicsFiles;
    }


    public static File getPynnCodeDir(File projectDir)
    {
        File dirForPynnFiles
            = getDirectoryInProject(projectDir, pynnCodeDir,
                                    "This is the directory for the PyNN files generated by neuroConstruct for this project.\n"
                                    +"Note, when a simulation is run, a directory will be created in ../simulations for the results.");

        return dirForPynnFiles;
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
        return getDirandReadme(getnCHome(),
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
    
    public static File getPynnUtilsDir(File projectDir)
    {
        return getDirandReadme(getPythonNeuroMLDir(projectDir), 
                                pynnUtilsDir,
                                "This is the directory for some PyNN utility files.\n",
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



    public static File getPythonScriptsDir(File projectDir, boolean createIfNotFound)
    {
        File dir = getDirandReadme(projectDir, pythonScripts, "", createIfNotFound);
        return dir;
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

        // Update from older dir structure...
        File oldImportedCellProcessDir = new File(projectDir, "importedCellProcesses");
        File cellProcDir = getCellProcessesDir(projectDir,  createIfNotFound);

        if (cellProcDir==null) return null;

        File fileBasedCPDir = new File(cellProcDir, fileBasedCellProcessesDir);


        if (oldImportedCellProcessDir.exists())
        {
            oldImportedCellProcessDir.renameTo(fileBasedCPDir);
        }

        fileBasedCPDir = getDirandReadme(cellProcDir, fileBasedCellProcessesDir,
                                                    "This is the directory where Cell Processes which have implementations in a single native environment files (e.g. *.mod for NEURON) are placed\n",
                                                    createIfNotFound);

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


        File cellProcDir = getDirandReadme(projectDir,
                                           cellProcessesDir,
                                           "This is the directory where Cell Processes files **used to be** stored, "
                                           + "either the native environment (e.g. *.mod) files, or ChannelML based files. "
                                           + "\n\nNow, a restructured cellMechanism directory will be used.\n",
                                           createIfNotFound);

        return cellProcDir;
    }



    public static File getCellMechanismDir(File projectDir)
    {
        return getCellMechanismDir(projectDir, true);
    }

    public static File getDirForCellMechFiles(Project project, String cellMechName)
    {
        return getDirForCellMechFiles(project, cellMechName, false);
    }

    public static File getDirForCellMechFiles(Project project, String cellMechName, boolean createIfNotFound)
    {
        File projDir = project.getProjectMainDirectory();

        File dir = ProjectStructure.getCellMechanismDir(projDir, false);

        if (dir != null && dir.exists())
        {
            File f = new File(dir, cellMechName);

            if (createIfNotFound && !f.exists())
            {
                f.mkdir();
            }
            return f;
        }
        else
            // old method...
            return new File(ProjectStructure.getCellProcessesDir(projDir, createIfNotFound),
                            cellMechName);
    }



    public static File getGlobalMatlabOctaveDir()
    {

        File dir = getDirandReadme(getnCHome(),
                                   matlabOctaveDirInInstall,
                                   "Any *.m files found in this directory will be automatically included in the Matlab/Octave file generated when a simulation is run.\n",
                                   true);

        return dir;


    }

    public static File getGlobalIgorNeuroMaticDir()
    {

        File dir = getDirandReadme(getnCHome(),
                                   igorNeuroMaticDirInInstall,
                                   "Files found in this directory will be automatically included in the Igor/NeuroMatic file generated when a simulation is run.\n",
                                   true);

        return dir;


    }



    public static File getCellMechanismDir(File projectDir, boolean createIfNotFound)
    {


        File cellMechDir = getDirandReadme(projectDir, cellMechanismDir,
                                                    "This is the directory where Cell Mechanism (channel, synapse, etc) files are stored, either the native environment (e.g. *.mod) files, or ChannelML based files.\n"+
               "Unlike the older cellProcesses dir, simply cutting and pasting these cell mech directories into another neuroConstruct project is sufficient for copying the cell mech to that project.\n",
        createIfNotFound);

        return cellMechDir;
    }


    public static File getMorphologiesDir(File projectDir)
    {
        File morphDir = new File(projectDir, morphologiesDir);
        // Update from older dir structure...
        File oldProjMorphDir = new File(projectDir, "projectMorphologies");



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
        return new File(getnCHome(), modTemplatesDirInInstall);
    }

    public static File getXMLTemplatesDir()
    {
        return new File(getnCHome(), xmlTemplatesDirInInstall);
    }
    /*
    public static File getNeuroMLSchemataDir()
    {
        return new File(getXMLTemplatesDir(), "Schemata");
    }*/

    public static File getCMLTemplatesDir()
    {
        return new File(getnCHome(), cmlTemplatesDirInInstall);
    }


    public static File getNeuroMLSchemataDir()
    {
        return new File(getXMLTemplatesDir(), "Schemata");
    }



    public static File getCMLExamplesDir()
    {
        return new File(getXMLTemplatesDir(), "Examples");
    }





    public static File getGenesisWinScript()
    {
        return new File(getnCHome(),genesisWinScript);
    }



    public static File getGenesisTemplatesDir()
    {
        return new File(getnCHome(),genesisTemplatesDirInInstall);
    }



    public static String getNeuConRecentFilesFilename()
    {
        return neuConRecentFilesFilename;
    }


    public static String getNmodlEditRecentFilesFilename()
    {
        return nmodlEditRecentFilesFilename;
    }


    public static String getOldProjectFileExtension()
    {
        return oldProjectFileExtension;
    }

    public static String getNewProjectFileExtension()
    {
        return newProjectFileExtension;
    }

    public static SimpleFileFilter getProjectFileFilter()
    {
        SimpleFileFilter fileFilter
            = new SimpleFileFilter(new String[]{getNewProjectFileExtension(),
                                                getOldProjectFileExtension()},
                                   "neuroConstruct files. Extension: *"+getNewProjectFileExtension()
                                   +", *"+getOldProjectFileExtension());

        return fileFilter;
    }

    public static SimpleFileFilter getZippedProjectFileFilter()
    {
        SimpleFileFilter fileFilter
            = new SimpleFileFilter(new String[]{getNewProjectZipFileExtension(),
                                                getOldProjectZipFileExtension()},
                                   "Zipped neuroConstruct files. Extension: *"+getNewProjectZipFileExtension()
                                   +", *"+getOldProjectZipFileExtension());

        return fileFilter;
    }

    public static File findProjectFile(File projectMainDir)
    {
        File projFile = new File(projectMainDir, projectMainDir.getName()+ProjectStructure.getNewProjectFileExtension());
        if(projFile.exists())
            return projFile;

        projFile = new File(projectMainDir, projectMainDir.getName()+ProjectStructure.getOldProjectFileExtension());
        if(projFile.exists())
            return projFile;

        return null;
    }

    public static void findProjectFile(File projectMainDir, ArrayList<File> projFiles)
    {
        //System.out.println("Checking for proj file in "+projectMainDir+", so far: "+projFiles);

        boolean lookDeeper = true;
        File projFile = new File(projectMainDir, projectMainDir.getName()+ProjectStructure.getNewProjectFileExtension());
        if(projFile.exists())
        {
            projFiles.add(projFile);
            lookDeeper = false;
        }

        // e.g. ProjName/neuroConstruct/ProjectName.ncx
        projFile = new File(projectMainDir, projectMainDir.getParentFile().getName()+ProjectStructure.getNewProjectFileExtension());
        if(projFile.exists())
        {
            projFiles.add(projFile);
            lookDeeper = false;
        }

        projFile = new File(projectMainDir, projectMainDir.getName()+ProjectStructure.getOldProjectFileExtension());
        if(projFile.exists())
        {
            projFiles.add(projFile);
            lookDeeper = false;
        }

        if (lookDeeper)
        {
            File[] subDir = projectMainDir.listFiles();
            for (File f: subDir)
            {
                if (f.isDirectory() && !GeneralUtils.isVersionControlDir(f))
                {
                    findProjectFile(f, projFiles);
                }
            }
        }

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
     * @return The default extension of XML files
     */
    public static String getXMLFileExtension()
    {
        return xmlExtension;
    }
    
    /**
     * @return The extension of HDF5 files as stored by the application
     */
    public static String getHDF5FileExtension()
    {
        return hdf5Extension;
    }

    /**
     * @return The extension of zipped NeuroML files as stored by the application
     */
    public static String getNeuroMLCompressedFileExtension()
    {
        return neuromlCompExtension;
    }



    public static String getMatplotlibEpsFilename()
    {
        return matplotlibEpsFilename;
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



    public static String getOldProjectZipFileExtension()
    {
        return oldZippedProjectFileExtension;
    }
    public static String getNewProjectZipFileExtension()
    {
        return newZippedProjectFileExtension;
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
