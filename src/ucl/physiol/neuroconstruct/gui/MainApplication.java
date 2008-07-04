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

package ucl.physiol.neuroconstruct.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.neuron.*;
import javax.swing.UIManager.*;
import java.util.Locale;
import ucl.physiol.neuroconstruct.project.*;
import org.python.util.*;

/**
 * Starts the main neuroConstruct application
 *
 * @author Padraig Gleeson
 *  
 */


public class MainApplication
{
    private MainFrame frame = null;

    private static boolean noGuiMode = false;

    private static String favouredLookAndFeel = null;

    static
    {
        Locale.setDefault(Locale.UK);

        favouredLookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
        try
        {
            MetalTheme theme = new CustomLookAndFeel();
            MetalLookAndFeel.setCurrentTheme(theme);
            UIManager.setLookAndFeel(favouredLookAndFeel);

        }
        catch (Exception ex)
        {
            System.out.println("Error with Look and Feel: " + favouredLookAndFeel);

            favouredLookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            try
            {
                LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
                for (int i = 0; i < lafs.length; i++)
                {
                      System.out.println("Installed Look and Feel: "+ lafs[i]);
                }

                UIManager.setLookAndFeel(favouredLookAndFeel);

                System.out.println("Using Look and Feel: " +UIManager.getLookAndFeel().toString());
            }


            catch (Exception e)
            {
                System.out.println("Error with Look and Feel: " + favouredLookAndFeel);
                // give up, use whatever java prefers...
            }
        }

    }

    public static String getFavouredLookAndFeel()
    {
        return favouredLookAndFeel;
    }


    public MainApplication()
    {
        //Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionInfo(Thread.currentThread().getName()));
        
        frame = new MainFrame();
        
        int active = Thread.activeCount();
        Thread all[] = new Thread[active];
        Thread.enumerate(all);
        for (int i = 0; i < active; i++) 
        {
            //System.out.println(i + ": " + all[i]);
            all[i].setDefaultUncaughtExceptionHandler(new UncaughtExceptionInfo());
        }

        if (!noGuiMode)
        {
            frame.validate();

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
        }

        GuiUtils.setMainFrame(frame);

    }

    private void setOpenProject(String projectFile)
    {
        frame.doLoadProject(projectFile);
    }

    private void generate()
    {
        frame.projManager.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, System.currentTimeMillis());
    }


    private void createHoc()
    {
        frame.doCreateHoc(NeuronFileManager.RUN_HOC);
    }

    private void reloadLastProject()
    {
        frame.doReloadLastProject();
    }


    private void runHoc()
    {
        frame.doRunNeuron();
    }



    private static void printUsageAndQuit()
    {
        System.out.println(
            "Usage: java ucl.physiol.neuroconstruct.gui.MainApplication [-options] [projectfilename]");
        System.out.println("\nwhere projectfilename is a *.neuro.xml project file");
        System.out.println("and where options include: \n");

        System.out.println("-? -help      print this help message");
        System.out.println("-version      print version information");
        System.out.println("-lastproj     reloads the last opened project");
      /*  System.out.println("-nogui        run without main Graphical User Interface");
        System.out.println("-generate     generate the cell positions/connections from the settings stored in the file");
        System.out.println("-createhoc    generates the positions etc. and creates the hoc files");
        System.out.println("-runhoc       generates the positions, creates the hoc files, and runs the main file in NEURON");*/
        System.exit(0);

    }

    private static void printVersionDetails()
    {
        System.out.println("\nneuroConstruct, version: " +
                                   GeneralProperties.getVersionNumber() + "\n");
    }


    public static void main(String[] args)
    {

        try 
        {
            // Check on java version...

            String javaVersion = System.getProperty("java.version");

            if (javaVersion.compareTo("1.5") < 0)
            {
                System.err.println("You are running Java version " + javaVersion + ".");
                System.err.println("neuroConstruct requires Java 1.5 (J2SE 5.0) or later.");
                System.exit(1);
            }

            // Check for presence of Java3D
            
            try
            {
                ClassLoader cl  = ClassLoader.getSystemClassLoader();
                cl.loadClass("javax.vecmath.Point3f");
            }
            catch(Exception ex)
            {
                GuiUtils.showErrorMessage(null, "Problem finding Java 3D. Please ensure it is installed correctly.\n"
                                          +"The latest version can be downloaded from: https://java3d.dev.java.net\n\n"
                                          +"Correct installation should result in Java 3D jarfiles being installed in:\n"
                                          +System.getProperty("java.ext.dirs"), ex, null);
            }


            try
            {
                UIManager.setLookAndFeel(favouredLookAndFeel);
            }
            catch (Exception ex)
            {
                System.out.println("Error with Look and Feel: " + favouredLookAndFeel);

            }

            String fileToOpen = null;
            boolean reloadLastProject = false;
            boolean generate = false;
            boolean createHoc = false;
            boolean runHoc = false;
            
            boolean runPython = false;
            String[] pyArgs = null;
            
            for(String arg: args)
            {
                //System.out.println("Arg for java: "+arg);
            }

            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals("-?") || args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h"))
                {
                    printVersionDetails();
                    printUsageAndQuit();
                }
                else if (args[i].equalsIgnoreCase("-v") || args[i].equalsIgnoreCase("-version"))
                {
                    printVersionDetails();
                    System.exit(0);
                }
                else if (args[i].equalsIgnoreCase("-nogui"))
                {
                    noGuiMode = true;
                }

                else if (args[i].equalsIgnoreCase("-lastproj"))
                {
                    reloadLastProject = true;
                }
                else if (args[i].equalsIgnoreCase("-generate"))
                {
                    generate = true;
                }
               /* else if (args[i].equalsIgnoreCase("-createhoc"))
                {
                    createHoc = true;
                }
                else if (args[i].equalsIgnoreCase("-runhoc"))
                {
                    runHoc = true;
                }*/
                else if  (args[i].equalsIgnoreCase("-python"))
                {
                    runPython = true;
                    int numArgsUsed = i+1;
                    int numArgsLeft = args.length - numArgsUsed;
                    
                    pyArgs = new String[numArgsLeft+1];
                    pyArgs[0] = "-i"; //interactive...
                    
                    
                    if (numArgsLeft>0)
                    {
                        for (int j=0;j<numArgsLeft;j++)
                            pyArgs[j+1] = args[numArgsUsed+j];
                    }
                    i = args.length; // end looping...
                }
                else if (args[i].startsWith("-"))
                {
                    printVersionDetails();
                    System.out.println("Unrecognised option: "+ args[i]+ "\n");
                    printUsageAndQuit();
                }
                else
                {
                    fileToOpen = args[i];
                }
            }
            
            if ((generate || createHoc) &&
                (fileToOpen==null && !reloadLastProject))
            {
                System.out.println("Please only use -generate etc. when a project file is specified.");
                System.exit(0);
            }
            
            if (!runPython)
            {
                MainApplication app = new MainApplication();

                String script = GeneralUtils.isWindowsBasedPlatform()?"run.bat":"run.sh";

                System.out.println("\nneuroConstruct v"+GeneralProperties.getVersionNumber()
                        +" starting...\nTo start application with extra memory, see "+script+" in the neuroConstruct home directory.\n");


                if (fileToOpen!=null) app.setOpenProject(fileToOpen);

                if (reloadLastProject) app.reloadLastProject();

                if (generate|| createHoc|| runHoc)
                {
                    app.generate();
                }
                if (createHoc|| runHoc)
                {
                    app.createHoc();
                }
                if (runHoc)
                {
                    app.runHoc();
                }
            }
            else
            {
                System.out.println("\nneuroConstruct "+GeneralProperties.getVersionNumber()
                        +" starting in Jython scripting mode...\n");
                
                for(String arg: pyArgs)
                {
                    //System.out.println("Arg for jython: "+arg);
                }
                jython.main(pyArgs); // it's as easy as that!
                
            }
        }
        catch (Exception e)
        {
            System.err.println("An exception has been caught in the main function: ");
            e.printStackTrace();
        }


    }
}
