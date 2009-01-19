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

    private static boolean plotOnlyMode = false;

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
        String run = GeneralUtils.isWindowsBasedPlatform()?"nC.bat":"./nC.sh";
        
        System.out.println(
            "Usage: \n" +
            "    "+run+" [-options] [projectfilename]\n" +
            "or\n" +
            "    "+run+" -plot datafilename\n");
        
        System.out.println("\nwhere projectfilename is a *.neuro.xml project file,\n" +
                            "datafilename is a file containing a single column of data (or a *.ds file as used in Data Set Manager)");
        System.out.println("and where options can include: \n");

        System.out.println("-? -help          print this help message");
        System.out.println("-version          print version information");
        System.out.println("-lastproj         reloads the last opened project");
        System.out.println("-python           use PRELIMINARY Jython based scripting interface");
        System.out.println("");
        
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

            if (javaVersion.compareTo("1.6") < 0)
            {
                String error = "You are running Java version " + javaVersion + ". "+"neuroConstruct requires Java 1.6 (J2SE 6) or later.\n\n" +
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

            String fileToOpen = null;
            boolean reloadLastProject = false;
            boolean generate = false;
            boolean createHoc = false;
            boolean runHoc = false;
            
            boolean runPython = false;
            String[] pyArgs = null;
            
            //boolean foundPlot
            
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
               
                else if (args[i].equalsIgnoreCase("-plot"))
                {
                    plotOnlyMode = true;
                    if (args.length<i+2)
                    {
                        printUsageAndQuit();
                    }
                    File dataFile = new File(args[i+1]);
                    
                    ArrayList<DataSet> dataSets = DataSetManager.loadFromDataSetFile(dataFile, false, DataSetManager.DataReadFormat.UNSPECIFIED);
                    
                    String plotFrameRef = "Plot of data from "+dataFile.getAbsolutePath();


                    PlotterFrame frame = PlotManager.getPlotterFrame(plotFrameRef);

                    for(DataSet dataSet: dataSets)
                        frame.addDataSet(dataSet);
                    

                    frame.setVisible(true);
                     
                }

                else if (args[i].equalsIgnoreCase("-lastproj"))
                {
                    reloadLastProject = true;
                }
                else if (args[i].equalsIgnoreCase("-generate"))
                {
                    generate = true;
                }
         
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
            
            if (!runPython && !plotOnlyMode)
            {
                MainApplication app = new MainApplication();

                String script = GeneralUtils.isWindowsBasedPlatform()?"nC.bat":"nC.sh";

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
            else if (runPython)
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
