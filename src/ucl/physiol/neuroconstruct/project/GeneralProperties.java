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

import java.awt.*;
import javax.swing.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.hpc.mpi.*;
 
/**
 * Storing of general properties associated with the application
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class GeneralProperties
{

    // FIXED values: Calculated once, can't be changed

    private static final String packageOfCellTypes = new String("ucl.physiol.neuroconstruct.cell");

    //private static String defaultLocNewProjects = null;

    /** @todo Make optional... */
    private static float maxElectrotonicLength = 0.1f;


    /** This is changed automatically by Ant. Look in build.xml...*/
    private static final String versionNumber = "1.0.4";

    private static final String neuroMLVersionNumberShort = "1.6";
    private static final String neuroMLVersionNumberLong = "1.6";

    private static final String minimumVersionJava = "J2SE 5.0";

    private static final String websiteNMLValidator = "http://www.morphml.org:8080/NeuroMLValidator";


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
                                  Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID,
                                  0.85f);





    static
    {
        GeneralProperties.loadFromSettingsFile();
        //System.out.println("Neuron home: "+userSettings.getNeuronHome());
        
        
        if (userSettings.getNeuronHome()==null)
        {
        	if (GeneralUtils.isWindowsBasedPlatform())
        	{

        		if ((new File("C:\\nrn61")).exists()) userSettings.setNeuronHome(new String("C:\\nrn61"));
        		else if ((new File("C:\\nrn60")).exists()) userSettings.setNeuronHome(new String("C:\\nrn60"));
        		else if ((new File("C:\\nrn59")).exists()) userSettings.setNeuronHome(new String("C:\\nrn59"));
        		else if ((new File("C:\\nrn58")).exists()) userSettings.setNeuronHome(new String("C:\\nrn58"));
        		else if ((new File("C:\\nrn57")).exists()) userSettings.setNeuronHome(new String("C:\\nrn57"));
        		else userSettings.setNeuronHome(new String("C:\\nrn60"));

        		//System.out.println("Neuron home: "+userSettings.getNeuronHome());

        		userSettings.setExecutableCommandLine("cmd /K start \"Neuron\" /wait ");
        	}
        	else if (GeneralUtils.isMacBasedPlatform())
        	{
        		if ((new File("/Applications/NEURON-6.1/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.1/nrn/powerpc"));
        		else if ((new File("/Applications/NEURON-6.0/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-6.0/nrn/powerpc"));
        		else if ((new File("/Applications/NEURON-5.9/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.9/nrn/powerpc"));
        		else if ((new File("/Applications/NEURON-5.8/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.8/nrn/powerpc"));
        		else if ((new File("/Applications/NEURON-5.7/nrn/powerpc")).exists()) userSettings.setNeuronHome(new String("/Applications/NEURON-5.7/nrn/powerpc"));
        		else  userSettings.setNeuronHome(new String("/Applications/NEURON-6.0/nrn/powerpc"));


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


        /* -- Writing 3D info -- */
        xmlEncoder.writeObject(defaultDisplay3DProps);

        xmlEncoder.close();
    }


    public static void loadFromSettingsFile()
    {
        // Printing to stdout as GeneralProperties must be fully instantiated before
        // logger created...

        File generalSettingsFile = new File(ProjectStructure.getGeneralSettingsFilename());

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

    public static void setNeuronHomeDir(String neuronHomeDir)
    {
        userSettings.setNeuronHome(neuronHomeDir);
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
                         path =  "";
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
         if (!logFileDir.exists()) logFileDir.mkdir();
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

    public static float getMaxElectrotonicLength()
    {
        return maxElectrotonicLength;
    }

    /**
     * The version of the NeuroML specs being used
     *
     * @return The version number
     */
    public static String getNeuroMLVersionNumber()
    {
        return neuroMLVersionNumberLong;
    }


    /**
     * Gets the current ChannelML schema
     *
     */
    public static File getChannelMLSchemaFile()
    {
        return new File("templates/xmlTemplates/Schemata/v"+neuroMLVersionNumberLong+"/Level2/ChannelML_v"+neuroMLVersionNumberShort+".xsd");
    }

    /**
     * Gets the top level NeuroML schema, to which every NeuroML file should comply
     *
     */
    public static File getNeuroMLSchemaFile()
    {
        return new File("templates/xmlTemplates/Schemata/v"+neuroMLVersionNumberLong+"/Level3/NeuroML_Level3_v"+neuroMLVersionNumberShort+".xsd");
    }



    /**
     * Gets the current ChannelML Readable format XSL mapping
     *
     */
    public static File getChannelMLReadableXSL()
    {
        return new File("templates/xmlTemplates/Schemata/v"+neuroMLVersionNumberLong+"/Level2/ChannelML_v"+neuroMLVersionNumberShort+"_HTML.xsl");
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

    public static boolean getDefaultShowAxonalArbours()
    {
        return defaultDisplay3DProps.getShowAxonalArbours();
    }


    public static void setDefaultShowInputs(boolean show)
    {
        defaultDisplay3DProps.setShowInputs(show);
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

    public static void setDefaultResolution3DElements(int res)
    {
        defaultDisplay3DProps.setResolution3DElements(res);
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




}
