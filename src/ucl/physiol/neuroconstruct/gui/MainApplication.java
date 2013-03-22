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

package ucl.physiol.neuroconstruct.gui;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import ucl.physiol.neuroconstruct.gui.plotter.PlotterFrame;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.neuron.*;
import javax.swing.UIManager.*;
import java.util.Locale;
import ucl.physiol.neuroconstruct.project.*;
import org.python.util.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.plotter.PlotManager;

/**
 * Starts the main neuroConstruct application
 *
 * @author Padraig Gleeson
 *  
 */


public class MainApplication
{
    private MainFrame frame = null;

    public enum StartupMode {NORMAL_GUI_MODE, 
                             PLOT_ONLY_MODE,
                             SIM_BROWSER_MODE,
                             COMMAND_LINE_INTERFACE_MODE,
                             TEST_MODE};

    private static StartupMode startupMode = StartupMode.TEST_MODE;

    private static String favouredLookAndFeel = null;

    private static boolean nmlV2TestMode = true;



    static
    {
        Locale.setDefault(Locale.UK);

        favouredLookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
        try
        {
            CustomLookAndFeel theme = new CustomLookAndFeel();
            MetalLookAndFeel.setCurrentTheme(theme);
            UIManager.setLookAndFeel(favouredLookAndFeel);

            //theme.printThemeInfo();

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
 
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionInfo());
  
        
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


        GuiUtils.setMainFrame(frame);

    }

    private void setOpenProject(String projectFile)
    {
        frame.doLoadProject(projectFile);
    }

    private void importNeuroML(String nmlFilename)
    {
        File nmlFile = new File(nmlFilename);

        if (!nmlFile.exists())
        {
            System.out.println("Error! File not found: "+nmlFile.getAbsolutePath());
            System.exit(1);
        }

        if (frame.projManager.getCurrentProject()==null)
        {
            String projectName = nmlFile.getName().indexOf(".")>1 ? nmlFile.getName().substring(0, nmlFile.getName().indexOf(".")) : nmlFile.getName();

            File projectLocation = ProjectStructure.getDefaultnCProjectsDir();

            frame.doNewProject(false, projectLocation, projectName);
        }

        frame.doImportNeuroML(nmlFile, true, true);
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

    public static boolean isNmlV2TestMode()
    {
        return nmlV2TestMode;
    }

    public static boolean isSumatraTestMode()
    {
        return (new File(ProjectStructure.getnCHome(), "sumatra")).exists();
    }





    private static void printUsageAndQuit()
    {
        String run = GeneralUtils.isWindowsBasedPlatform()?"nC.bat":"./nC.sh";
        
        System.out.println(
            "\nUsage: \n" +
            "    "+run+" [-options] [projectfilename]\n" +
            "or\n" +
            "    "+run+" -plot datafilename\n");
        
        System.out.println("where projectfilename is a *.ncx (or *.neuro.xml) project file,\n" +
                            "datafilename is a file containing a single column of data (or a *.ds file as used in Data Set Manager)");
        System.out.println("and where options can include: \n");

        System.out.println("-? -help                    print this help message");
        System.out.println("-version                    print version information");
        System.out.println("-lastproj                   reloads the last opened project");
        System.out.println("-python [projectfilename]   use Jython based scripting interface");
        System.out.println("-make                       build neuroConstruct from source (JDK 1.5+ is needed)");
        System.out.println("-sims projectfilename       load project with tree based simulation browser");
        System.out.println("-neuroml neuromlfilename    import contents of NeuroML file, creating new project");
        System.out.println("");
        
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
                String error = "You are running Java version " + javaVersion + ". "+"neuroConstruct requires Java 1.5 (J2SE 5) or later.\n\n" +
                    "Please download the latest version of the JDK, and remove any earlier versions (or customise nC.bat/nC.sh to use the latest).";
                
                System.out.println("Error: "+ error);
                
                GuiUtils.showErrorMessage(null, error, null, null);
                
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
                String error = "Problem finding Java 3D. Please ensure it is installed correctly.\n"
                                          +"The latest version can be downloaded from: https://java3d.dev.java.net\n\n"
                                          +"A local installation of Java3D should result in Java 3D jarfiles being installed in:\n"
                                          +System.getProperty("java.ext.dirs")
                                          +"\n\nA version of Java3D is included with neuroConstruct and should normally be found using java.library.path:\n"
                                          +System.getProperty("java.library.path")
                                          +"\nand java.class.path:\n"
                                          +System.getProperty("java.class.path");
                
                System.out.println("Error: "+ error);
                
                GuiUtils.showErrorMessage(null, error, ex, null);
            }


            try
            {
                UIManager.setLookAndFeel(favouredLookAndFeel);
            }
            catch (Exception ex)
            {
                System.out.println("Error with Look and Feel: " + favouredLookAndFeel);

            }

            String projFileToOpen = null;
            ArrayList<String> nmlFilesToOpen = new ArrayList<String>();

            boolean reloadLastProject = false;
            boolean generate = false;
            boolean createHoc = false;
            boolean runHoc = false;
            boolean importNeuroML = false;
            
            String[] pyArgs = null;
            

            for (int i = 0; i < args.length; i++)
            {
                //System.out.println("Checking arg: ("+args[i]+")");

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
                else if (args[i].equalsIgnoreCase("-nmlv2") )
                {
                    nmlV2TestMode = true;
                }
                else if (args[i].equalsIgnoreCase("-neuroml") )
                {
                    if (args.length<2)
                    {
                        printUsageAndQuit();
                    }

                    importNeuroML = true;
                }

                else if (args[i].equalsIgnoreCase("-sims"))
                {

                    if (args.length<i+2)
                    {
                        printUsageAndQuit();
                    }
                    final File projFile = new File(args[i+1]);
                    System.out.println("Goint to load simulation data from project: "+projFile.getCanonicalFile());

                    startupMode = StartupMode.SIM_BROWSER_MODE;

                    try
                    {
                        UIManager.setLookAndFeel(favouredLookAndFeel);
                    }
                    catch (Exception ex)
                    { }

                    java.awt.EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            new SimulationTreeFrame(projFile, true).setVisible(true);
                        }
                    });
                }

                else if (args[i].equalsIgnoreCase("-plot"))
                {

                    if (args.length<i+2)
                    {
                        printUsageAndQuit();
                    }
                    String plotFrameRef = "Plot of data from file(s):";
                    
                    for (int remArg=i+1;remArg<args.length;remArg++)
                    {
                        plotFrameRef = plotFrameRef+" "+args[remArg];
                    }

                    PlotterFrame frame = PlotManager.getPlotterFrame(plotFrameRef);

                    for (int remArg=i+1;remArg<args.length;remArg++)
                    {

                        File dataFile = new File(args[remArg]);

                        ArrayList<DataSet> dataSets = DataSetManager.loadFromDataSetFile(dataFile, false, DataSetManager.DataReadFormat.UNSPECIFIED);

                        //String plotFrameRef = "Plot of data from "+dataFile.getAbsolutePath();


                        RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename()).setMyLastExportPointsDir(dataFile.getAbsolutePath());



                        frame.setStandAlone(true);

                        for(DataSet dataSet: dataSets)
                            frame.addDataSet(dataSet);


                        frame.setVisible(true);
                    }

                    startupMode = StartupMode.PLOT_ONLY_MODE;
                     
                }

                else if (args[i].equalsIgnoreCase("-lastproj"))
                {
                    reloadLastProject = true;
                }
         
                else if  (args[i].equalsIgnoreCase("-python"))
                {
                    startupMode = StartupMode.COMMAND_LINE_INTERFACE_MODE;
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
                    if (!importNeuroML)
                    {
                        projFileToOpen = args[i];
                    }
                    else
                    {
                        nmlFilesToOpen.add(args[i]);
                    }
                }
            }
            
            if ((generate || createHoc) && (projFileToOpen==null && nmlFilesToOpen.isEmpty() && !reloadLastProject))
            {
                System.out.println("Please only use -generate etc. when a project file is specified.");
                System.exit(0);
            }
            
            if (!startupMode.equals(StartupMode.COMMAND_LINE_INTERFACE_MODE) &&
                !startupMode.equals(StartupMode.PLOT_ONLY_MODE)&&
                !startupMode.equals(StartupMode.SIM_BROWSER_MODE))
            {
                MainApplication app = new MainApplication();

                startupMode = StartupMode.NORMAL_GUI_MODE;

                String script = GeneralUtils.isWindowsBasedPlatform()?"nC.bat":"nC.sh";

                System.out.println("\nneuroConstruct v"+GeneralProperties.getVersionNumber()
                        +" starting...\nTo start application with extra memory, see "+script+" in the neuroConstruct home directory.\n");


                if (projFileToOpen!=null)
                {
                    app.setOpenProject(projFileToOpen);
                }
                else if (reloadLastProject)
                {
                    app.reloadLastProject();
                }
                else if (!nmlFilesToOpen.isEmpty())
                {
                    for (String nmlFileToOpen: nmlFilesToOpen)
                    {
                        System.out.println("Importing elements from NeuroML file: "+ nmlFileToOpen);
                        app.importNeuroML(nmlFileToOpen);
                        app.frame.refreshAll();

                        
                    }
                }

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
            else if (startupMode.equals(StartupMode.COMMAND_LINE_INTERFACE_MODE))
            {
                System.out.println("\nneuroConstruct "+GeneralProperties.getVersionNumber()
                        +" starting in Jython scripting mode...\n");
                
                for(String arg: pyArgs)
                {
                    //System.out.println("Arg for jython: "+arg);
                }
                startupMode = StartupMode.COMMAND_LINE_INTERFACE_MODE;
                jython.main(pyArgs); // it's as easy as that!

                
            }
        }
        catch (Exception e)
        {
            System.err.println("An exception has been caught in the main function: ");
            e.printStackTrace();
        }
    }

    public static StartupMode getStartupMode()
    {
        return startupMode;
    }

    public static boolean isGUIBasedStartupMode()
    {
        return startupMode == StartupMode.NORMAL_GUI_MODE ||
               startupMode == StartupMode.PLOT_ONLY_MODE ||
               startupMode == StartupMode.SIM_BROWSER_MODE;
    }
}
