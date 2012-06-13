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

import java.beans.*;
import java.io.*;

import java.awt.*;
import javax.swing.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.hpc.mpi.*;

/**
 * Storing of general properties associated with the application
 *
 * @author Padraig Gleeson
 *
 */


public class GeneralProperties
{
    // FIXED values: Calculated once, can't be changed

    private static final String packageOfCellTypes = "ucl.physiol.neuroconstruct.cell";

    private static final String versionNumber = "1.6.0";

    //private static final String latestNeuroMLVersionNumber = "1.8.1";

    private static final String minimumVersionJava = "J2SE 5.0";

    private static final String websiteNMLValidator = "http://www.neuroml.org/NeuroMLValidator";


    // VARIABLE values: Can be changed by the user
    private static UserSettings userSettings = new UserSettings();

    private static ReplaySettings replaySettings = new ReplaySettings();

    private static MpiSettings mpiSettings = new MpiSettings();



    // DEFAULT values:

    /** @todo Put these in the OptionsFrame dialog */

    private static final float defaultApPropagationVelocity = 1000f; // um/ms or 1m/s, estimate for unmyelinated axons...

    private static final float defaultRegionHeight = 50.0f;
    private static final float defaultRegionWidth = 120.0f;
    private static final float defaultRegionDepth = 120.0f;

    private static final float defaultSimDuration = 100;
    private static final float defaultSimDT = 0.025f;



    private static Display3DProperties defaultDisplay3DProps
        = new Display3DProperties(new Color(144, 166, 232),
                                  Color.white,
                                  false,
                                  true,
                                  true,
                                  true,
                                  true,
                                  true,
                                  Display3DProperties.AA_NOT_SET,
                                  Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID,
                                  0.85f);


    private static ProjectProperties defaultProjectProperties = new ProjectProperties();


    static
    {
        defaultProjectProperties.setPreferredSaveFormat(ProjectStructure.JAVA_OBJ_FORMAT);

        GeneralProperties.loadFromSettingsFile();


        if (userSettings.getNeuronHome()==null || userSettings.getNeuronHome().trim().length()==0)
        {
        	if (GeneralUtils.isWindowsBasedPlatform())
        	{

        		if ((new File("C:\\nrn62")).exists()) userSettings.setNeuronHome(new String("C:\\nrn62"));
                else if ((new File("C:\\nrn71")).exists()) userSettings.setNeuronHome(new String("C:\\nrn71"));
                else if ((new File("C:\\nrn70")).exists()) userSettings.setNeuronHome(new String("C:\\nrn70"));
                else if ((new File("C:\\nrn61")).exists()) userSettings.setNeuronHome(new String("C:\\nrn61"));
        		else if ((new File("C:\\nrn60")).exists()) userSettings.setNeuronHome(new String("C:\\nrn60"));
        		else if ((new File("C:\\nrn59")).exists()) userSettings.setNeuronHome(new String("C:\\nrn59"));
        		else if ((new File("C:\\nrn58")).exists()) userSettings.setNeuronHome(new String("C:\\nrn58"));
        		else if ((new File("C:\\nrn57")).exists()) userSettings.setNeuronHome(new String("C:\\nrn57"));
        		else userSettings.setNeuronHome(new String("C:\\nrn62"));

        		//System.out.println("Neuron home: "+userSettings.getNeuronHome());

        		userSettings.setExecutableCommandLine("cmd /K start \"Neuron\" /wait ");
        	}
        	else if (GeneralUtils.isMacBasedPlatform())
        	{
                    if ((new File("/Applications/NEURON-6.2/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.2/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-7.2/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.2/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-7.1/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.1/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-7.0/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.0/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-6.1/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.1/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-6.0/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.0/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-5.9/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.9/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-5.8/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.8/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-5.7/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.7/nrn/powerpc"));
                    else if ((new File("/Applications/NEURON-7.2/nrn/umac")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.2/nrn/umac"));
                    else if ((new File("/Applications/NEURON-7.1/nrn/umac")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.1/nrn/umac"));
                    else if ((new File("/Applications/NEURON-7.0/nrn/umac")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.0/nrn/umac"));
                    else if ((new File("/Applications/NEURON-6.2/nrn/umac")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.2/nrn/umac"));
                    else if ((new File("/Applications/NEURON-6.1/nrn/umac")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.1/nrn/umac"));

                    else if ((new File("/Applications/NEURON-6.2/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.2/nrn/i386"));
                    else if ((new File("/Applications/NEURON-7.1/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.1/nrn/i386"));
                    else if ((new File("/Applications/NEURON-7.0/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-7.0/nrn/i386"));
                    else if ((new File("/Applications/NEURON-6.1/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.1/nrn/i386"));
                    else if ((new File("/Applications/NEURON-6.0/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.0/nrn/i386"));
                    else if ((new File("/Applications/NEURON-5.9/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.9/nrn/i386"));
                    else if ((new File("/Applications/NEURON-5.8/nrn/i386")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.8/nrn/i386"));

                    else  userSettings.setNeuronHome(new String("/Applications/NEURON-6.2/nrn/powerpc"));

                    userSettings.setExecutableCommandLine("/Applications/Utilities/Terminal.app/Contents/MacOS/Terminal ");
        	}
        	else
        	{
        		userSettings.setNeuronHome(new String("/usr/local"));
        		userSettings.setExecutableCommandLine("konsole ");
        	}
        }

        if (userSettings.getLocationLogFiles()==null)
        {
        	userSettings.setLocationLogFiles(System.getProperty("user.dir")
        			+ System.getProperty("file.separator")
        			+ "logs");
        }

        //userSettings.setFormatForSavingMorphologies(UserSettings.JAVAXML_FORMAT);

    }



    /**
     *  Stores these settings in a file for later retrieval
     */
    public static void saveToSettingsFile()
    {
        // Printing to stdout as GeneralProperties must be fully instantiated before
        // logger created...

        //System.out.println("Saving gen props");

        File generalSettingsFile = new File(ProjectStructure.getGeneralSettingsFilename());

        XMLEncoder xmlEncoder = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try
        {
            fos = new FileOutputStream(generalSettingsFile);
            bos = new BufferedOutputStream(fos);
            xmlEncoder = new XMLEncoder(bos);
        }
        catch (FileNotFoundException ex)
        {
            JOptionPane.showMessageDialog(null,
                                          "Problem saving to file: "
                                          + generalSettingsFile.getAbsolutePath()
                                          + "\n"
                                          + ex.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);

        }

        xmlEncoder.flush();
        try
        {
            String message = new String("\n<!-- \n\nThis is a neuroConstruct general properties file. It's best to change\nthese settings in "
                                        + "neuroConstruct, as opposed to editing this file manually.\n\n -->\n\n");

            fos.write(message.getBytes());
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,
                                          "Problem writing to file: "
                                          + generalSettingsFile.getAbsolutePath()
                                          + "\n"
                                          + ex.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);

        }
        xmlEncoder.flush();

        /* -- Writing User Settings -- */
        xmlEncoder.writeObject(userSettings);

        /* -- Writing Replay Settings -- */
        xmlEncoder.writeObject(replaySettings);

        /* -- Writing MPI Settings -- */
        xmlEncoder.writeObject(mpiSettings);


        /* -- Writing proj info -- */
        xmlEncoder.writeObject(defaultProjectProperties);

        /* -- Writing 3D info -- */
        xmlEncoder.writeObject(defaultDisplay3DProps);

        xmlEncoder.close();
    }


    public static void loadFromSettingsFile()
    {
        // Printing to stdout as GeneralProperties must be fully instantiated before
        // logger created...
        String settingsFile = ProjectStructure.getGeneralSettingsFilename();
        File generalSettingsFile = new File(settingsFile);

        XMLDecoder xmlDecoder = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try
        {
            fis = new FileInputStream(generalSettingsFile);
            bis = new BufferedInputStream(fis);
            xmlDecoder = new XMLDecoder(bis);
        }
        catch (FileNotFoundException ex)
        {
            System.out.println("Problem loading from general settings file: "
                               + generalSettingsFile.getAbsolutePath());

            System.out.println("Trying to create new settings file...");

            generalSettingsFile = new File(ProjectStructure.getGeneralSettingsFilename());

            if (!generalSettingsFile.exists())
            {
                File parentDir = generalSettingsFile.getParentFile();
                if (!parentDir.exists())
                {
                    parentDir.mkdir();
                }
                GeneralProperties.saveToSettingsFile();
            }
            return;

        }

        Object nextReadObject = null;

        //System.out.println("replaySettings" + replaySettings);

        try
        {
            while ( (nextReadObject = xmlDecoder.readObject()) != null)
            {
                /* -- Reading User Settings -- */
                if (nextReadObject instanceof UserSettings)
                {
                    userSettings = (UserSettings) nextReadObject;
                }
                /* -- Reading Replay Settings -- */
                if (nextReadObject instanceof ReplaySettings)
                {
                   replaySettings = (ReplaySettings) nextReadObject;

                   //System.out.println("replaySettings" + replaySettings);
                }
                /* -- Reading proj info -- */
                if (nextReadObject instanceof ProjectProperties)
                {
                    defaultProjectProperties = (ProjectProperties) nextReadObject;
                }
                /* -- Reading 3D info -- */
                if (nextReadObject instanceof Display3DProperties)
                {
                    defaultDisplay3DProps = (Display3DProperties) nextReadObject;
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            //System.out.println("Reached end of general settings file...");
        }
        catch (Exception ex)
        {
            System.out.println("Problem with general settings file: ");
            ex.printStackTrace();
        }


        xmlDecoder.close();

    }

    public static String getExecutableCommandLine()
    {
        return userSettings.getExecutableCommandLine();
    }

    public static void setExecutableCommandLine(String commLine)
    {
         userSettings.setExecutableCommandLine(commLine);
     }

    public static File getnCProjectsDir()
    {
        File nCProjectsDir = new File(userSettings.getNCProjectsDir());
        return nCProjectsDir;
    }

    public static void setnCProjectsDir(File nCProjectsDir)
    {
        userSettings.setNCProjectsDir(nCProjectsDir.getAbsolutePath());
    }

    public static String getNeuronHomeDir()
    {
        String nrnHome = userSettings.getNeuronHome();
        return nrnHome;
    }
    public static String getPsicsJar()
    {
        String jar = userSettings.getPsicsJar();
        return jar;
    }

    public static void setNeuronHomeDir(String neuronHomeDir)
    {
        userSettings.setNeuronHome(neuronHomeDir);
    }
    public static void setPsicsJar(String jar)
    {
        userSettings.setPsicsJar(jar);
    }


     public static String getEditorPath(boolean askIfNull)
     {
         if (userSettings.getEditorPath() == null || userSettings.getEditorPath().length()==0)
         {
             if (askIfNull)
             {
                 //String title = "Enter path for text editor";
                 String message = "Please enter the full path to your favourite text editor\n"
                     +"For Windows this could be Wordpad, but Textpad (www.textpad.com) or jEdit (www.jedit.org) are better alternatives\n"
                     +"For Linux, etc. Kate, gedit or jEdit are fine\n"+
                     "A condition is that it should take a filename as an argument and open that file on startup";

                 String suggExec = "";
                 String path = "";

                 while (path.length()==0)
                 {

                     if (GeneralUtils.isWindowsBasedPlatform())
                     {
                         suggExec = "c:\\Program Files\\Windows NT\\Accessories\\wordpad.exe";
                     }
                     else
                     {
                         suggExec = "/opt/kde3/bin/kate";
                     }
                     System.out.println("suggExec: "+suggExec);

                     path = JOptionPane.showInputDialog(message, suggExec);

                     System.out.println("Path: "+path);

                     if (path == null) // cancelled
                     {
                         path =  "";
                     }
                     File execFile = new File(path);
                     if (execFile.exists())
                     {
                         userSettings.setEditorPath(path);
                         return path;
                     }
                     else
                     {
                         GuiUtils.showErrorMessage(null, "The file "+execFile.getAbsolutePath()+" does not exist!", null, null);

                         path = "";
                     }
                 }

             }
             else
             {
                 return "";
             }
         }

         return userSettings.getEditorPath();
     }

     public static void setEditorPath(String path)
     {
         userSettings.setEditorPath(path);
     }


     public static String getBrowserPath(boolean askIfNull)
     {
         if (userSettings.getBrowserPath() == null || userSettings.getBrowserPath().length()==0)
         {
             if (askIfNull)
             {
                 //String title = "Enter path for browser ";
                 String message = "Please enter the full path to your favourite browser.\n"
                     +"For Windows this could be Internet Explorer or Firefox. \n"+
                     "One condition is that it should take a URL as an argument and open that page on startup";

                 String suggExec = "";
                 String path = "";

                 while (path.length()==0)
                 {

                     if (GeneralUtils.isWindowsBasedPlatform())
                     {
                         suggExec = "c:\\Program Files\\Mozilla Firefox\\firefox.exe";
                         if (GeneralUtils.is64bitPlatform())
                         {
                            suggExec = "c:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe";
                         }
                         if (!(new File(suggExec)).exists())
                         {
                             suggExec = "c:\\Program Files\\Internet Explorer\\iexplore.exe";
                             if (GeneralUtils.is64bitPlatform())
                             {
                                suggExec = "c:\\Program Files (x86)\\Internet Explorer\\iexplore.exe";
                             }
                         }
                     }

                     else if (GeneralUtils.isMacBasedPlatform())
                     {
                         suggExec = "open";
                     }
                     else
                     {
                         suggExec = "/usr/bin/firefox";
                     }
                     System.out.println("suggExec: "+suggExec);

                     path = JOptionPane.showInputDialog(message, suggExec);

                     System.out.println("Path: "+path);

                     if (path == null) // cancelled
                     {
                         return null;
                     }
                     File execFile = new File(path);
                     if (execFile.exists())
                     {
                         userSettings.setBrowserPath(path);
                         return path;
                     }
                     else
                     {
                         GuiUtils.showErrorMessage(null, "The file "+execFile.getAbsolutePath()+" does not exist!", null, null);

                         path = "";
                     }
                 }

             }
             else
             {
                 return "";
             }
         }

         return userSettings.getBrowserPath();
     }


     public static void setBrowserPath(String path)
     {
         userSettings.setBrowserPath(path);
     }




    public static File getLogFileDir()
    {
        File logFileDir = new File(userSettings.getLocationLogFiles());

        if (!logFileDir.exists())
        {
            if (GeneralProperties.getLogFileSaveToFilePolicy())
            {
                logFileDir.mkdir();
            }
        }
        return logFileDir;
    }

/*
     public static void setMorphologySaveFormat(String morphFormat)
     {
         userSettings.setFormatForSavingMorphologies(morphFormat);
     }

     public static String getMorphologySaveFormat()
     {
         return userSettings.getFormatForSavingMorphologies();
     }
*/

    public static void setLogFileDir(String logFileDir)
    {
        userSettings.setLocationLogFiles(logFileDir);
    }


    public static boolean getLogFilePrintToScreenPolicy()
    {
        return userSettings.getLogFilePrintToScreenPolicy();
    }

    public static void setLogFilePrintToScreenPolicy(boolean policy)
    {
        userSettings.setLogFilePrintToScreenPolicy(policy);
    }


    public static String getDefaultPreferredSaveFormat()
    {
        return defaultProjectProperties.getPreferredSaveFormat();
    }
    public static void setDefaultPreferredSaveFormat(String preferredSaveFormat)
    {
        defaultProjectProperties.setPreferredSaveFormat(preferredSaveFormat);
    }




    public static int getNumProcessorstoUse()
    {
        return userSettings.getNumProcessorstoUse();
    }

    public static void setNumProcessorstoUse(int num)
    {
        userSettings.setNumProcessorstoUse(num);
    }

    public static boolean getGenerateMatlab()
    {
        return userSettings.getGenerateMatlab();
    }

    public static void setGenerateMatlab(boolean policy)
    {
        userSettings.setGenerateMatlab(policy);
    }


    public static boolean getGenerateIgor()
    {
        return userSettings.getGenerateIgor();
    }

    public static void setGenerateIgor(boolean policy)
    {
        userSettings.setGenerateIgor(policy);
    }


    public static boolean getLogFileSaveToFilePolicy()
    {
        //System.out.println("getLogFileSaveToFilePolicy: "+ userSettings.getLogFileSaveToFilePolicy());
        return userSettings.getLogFileSaveToFilePolicy();
    }

    public static void setLogFileSaveToFilePolicy(boolean policy)
    {
        userSettings.setLogFileSaveToFilePolicy(policy);
    }




    /**
     * The version of the app
     *
     * @return The version number
     */
    public static String getVersionNumber()
    {
        return versionNumber;
    }


    /**
     * The version of the NeuroML specs being used
     *
     * @return The version number
    */
    public static String getLatestNeuroMLVersionNumber()
    {
        String latestNml = "0.0.0";
        String toIgnore = "v1.8.2"; // temp ignoring this dir

        for(File f: ProjectStructure.getNeuroMLSchemataDir().listFiles())
        {
            if (f.isDirectory()&&f.getName().startsWith("v")
                && !f.getName().equals(toIgnore))
            {
                String ver = f.getName().substring(1);
                if (ProjectStructure.compareVersions(ver, latestNml)>0)
                    latestNml = ver;
            }
        }
        return latestNml;
    }

    public static String getLatestNeuroMLVersionString()
    {
        return "v"+getLatestNeuroMLVersionNumber();
    }

    /*
     * Returns the number part of the NeuroML version, e.g. 1.8.1 as a String
     */
    public static String getNeuroMLVersionNumber()
    {
        String ver = userSettings.getPrefNeuroMLVersionString();
        if (ver==null) return null;
        if (ver.startsWith("v"))
            return ver.substring(1);
        return ver;
    }


    /*
     * Returns a string for the NeuroML version, e.g. v1.8.1
     */
    public static String getNeuroMLVersionString()
    {
        String ver = userSettings.getPrefNeuroMLVersionString();
        if (ver==null)
        {
            userSettings.setPrefNeuroMLVersionString(getLatestNeuroMLVersionString());
            ver = userSettings.getPrefNeuroMLVersionString();
        }
        if (!ver.startsWith("v"))
            return "v"+ver;
        return ver;
    }


    public static void setNeuroMLVersionString(String ver)
    {
        userSettings.setPrefNeuroMLVersionString(ver);
    }


    /**
     * Gets the current ChannelML schema
     *
     */
    public static File getChannelMLSchemataDir()
    {
        return new File("templates/xmlTemplates/Schemata/"+getNeuroMLVersionString()
                +"/Level2");
    }
    /**
     * Gets the current ChannelML schema
     *
     */
    public static File getChannelMLSchemaFile()
    {
        return new File(getChannelMLSchemataDir(), "ChannelML_"+getNeuroMLVersionString()+".xsd");
    }

    /**
     * Gets the top level NeuroML schema, to which every NeuroML file should comply
     *
     */
    public static File getNeuroMLSchemaFile()
    {
        return new File("templates/xmlTemplates/Schemata/"+getNeuroMLVersionString()
                +"/Level3/NeuroML_Level3_"+getNeuroMLVersionString()+".xsd");
    }
    /**
     * Gets the top level NeuroML v2 schema, to which every NeuroML v2 file should comply
     *
     */
    public static File getNeuroMLv2SchemaFile()
    {
        return new File("NeuroML2/Schemas/NeuroML2/NeuroML_v2alpha.xsd");
    }



    /**
     * Gets the current ChannelML Readable format XSL mapping
     *
     */
    public static File getChannelMLReadableXSL()
    {
        return new File("templates/xmlTemplates/Schemata/"+getNeuroMLVersionString()
                +"/Level2/ChannelML_"+getNeuroMLVersionString()+"_HTML.xsl");
    }
    /**
     * Gets the current ChannelML 2 NeuroML v2.0 converter
     *
     */
    public static File getChannelML2NeuroML2()
    {
        return new File("NeuroML2/ChannelMLConvert/ChannelML2NeuroML2.xsl");
    }


    /**
     * Gets the SBML Readable format XSL mapping. This may change!!!
     *
     */
    public static File getSBMLReadableXSL()
    {
        return new File("templates/xmlTemplates/Schemata/Miscellaneous/SimpleSBMLView.xsl");
    }



    /**
     * The minimum version of Java supported
     *
     * @return The Java version
     */
    public static String getMinimumVersionJava()
    {
        return minimumVersionJava;
    }


    public static String getWebsiteNMLValidator()
    {
        return websiteNMLValidator;
    }






    /**
     * The package in which the Cell reside
     *
     * @return The package name
     */
    public static String getPackageNameOfCellTypes()
    {
        return packageOfCellTypes;
    }




    public static float getDefaultRegionHeight()
    {
        return defaultRegionHeight;
    }



    public static float getDefaultRegionWidth()
    {
        return defaultRegionWidth;
    }


    public static float getDefaultRegionDepth()
    {
        return defaultRegionDepth;
    }



    public static Color getDefault3DBackgroundColor()
    {
        return defaultDisplay3DProps.getBackgroundColour3D();
    }


    public static void setDefault3DBackgroundColor(Color colour)
    {
        defaultDisplay3DProps.setBackgroundColour3D(colour);
    }

    public static Color getDefaultCellColor3D()
    {
        return defaultDisplay3DProps.getCellColour3D();
    }


    public static void setDefaultCellColor3D(Color colour)
    {
        defaultDisplay3DProps.setCellColour3D(colour);
    }

    public static float getDefaultApPropagationVelocity()
    {
        return defaultApPropagationVelocity;
    }



    public static boolean getDefault3DAxesOption()
    {
        return defaultDisplay3DProps.getShow3DAxes();
    }


    public static void setDefault3DAxesOption(boolean showAxes)
    {
        defaultDisplay3DProps.setShow3DAxes(showAxes);
    }

    public static void setDefaultAntiAliasing(int aa)
    {
        defaultDisplay3DProps.setAntiAliasing(aa);
    }

    /**
     * How to display the dendrites etc: sticks or with diameters
     *
     */
    public static String getDefaultDisplayOption()
    {

        return defaultDisplay3DProps.getDisplayOption();
    }


    /**
     * How to display the dendrites etc, sticks or with diameters
     *
     */
    public static void setDefaultDisplayOption(String dispOpt)
    {
        defaultDisplay3DProps.setDisplayOption(dispOpt);
    }



    public static float getDefaultSimulationDuration()
    {
        return defaultSimDuration;
    }

    public static float getDefaultSimulationDT()
    {
        return defaultSimDT;
    }


    public static boolean getDefaultShowRegions()
    {
        return defaultDisplay3DProps.getShowRegions();
    }


    public static void setDefaultShowRegions(boolean show)
    {
        defaultDisplay3DProps.setShowRegions(show);
    }


    public static boolean getDefaultShowInputs()
    {
        return defaultDisplay3DProps.getShowInputs();
    }

    public static String getDefaultShowInputsAs()
    {
        return defaultDisplay3DProps.getShowInputsAs();
    }

    public static boolean getDefaultShowAxonalArbours()
    {
        return defaultDisplay3DProps.getShowAxonalArbours();
    }


    public static void setDefaultShowInputs(boolean show)
    {
        defaultDisplay3DProps.setShowInputs(show);
    }

    public static void setDefaultShowInputsAs(String probe)
    {
        defaultDisplay3DProps.setShowInputsAs(probe);
    }


    public static void setDefaultShowAxonalArbours(boolean show)
    {
        defaultDisplay3DProps.setShowAxonalArbours(show);
    }




    public static float getDefaultTransparency()
    {
        return defaultDisplay3DProps.getTransparency();
    }


    public static boolean getDefaultShowSynapseEndpoints()
    {
        return defaultDisplay3DProps.getShowSynapseEndpoints();
    }

    public static void setDefaultShowSynapseEndpoints(boolean show)
    {
        defaultDisplay3DProps.setShowSynapseEndpoints(show);
    }

    public static boolean getDefaultShowSynapseConns()
    {
        return defaultDisplay3DProps.getShowSynapseConns();
    }

    public static void setDefaultShowSynapseConns(boolean show)
    {
        defaultDisplay3DProps.setShowSynapseConns(show);
    }

    public static int getDefaultResolution3DElements()
    {
        return defaultDisplay3DProps.getResolution3DElements();
    }

    public static int getDefaultAntiAliasing()
    {
        return defaultDisplay3DProps.getAntiAliasing();
    }

    public static void setDefaultResolution3DElements(int res)
    {
        defaultDisplay3DProps.setResolution3DElements(res);
    }

    public static float getDefaultMinRadius()
    {
        return defaultDisplay3DProps.getMinRadius();
    }

    public static void setDefaultMinRadius(float res)
    {
        defaultDisplay3DProps.setMinRadius(res);
    }

    public static void setDefaultTransparency(float trans)
    {
        defaultDisplay3DProps.setTransparency(trans);
    }




    public static void main(String[] args)
    {
        // NOTE: don't change this! It's used to quickly toggle the log to screen option
        // via the logtog task in build.xml
        boolean logToScreenOn = GeneralProperties.getLogFilePrintToScreenPolicy();

        GeneralProperties.setLogFilePrintToScreenPolicy(!logToScreenOn);

        GeneralProperties.saveToSettingsFile();

        System.out.println("Logging to screen turned on? "
                           + GeneralProperties.getLogFilePrintToScreenPolicy());


    }



    public static ReplaySettings getReplaySettings()
    {
        return replaySettings;
    }
    public static void setReplaySettings(ReplaySettings replaySets)
    {
        replaySettings = replaySets;
    }

    public static MpiSettings getMpiSettings()
    {
        return mpiSettings;
    }
    public static void setMpiSettings(MpiSettings ms)
    {
        mpiSettings = ms;
    }

    private GeneralProperties()
    {
    }




}
