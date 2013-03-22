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

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.project.GeneratedPlotSaves.PlotSaveDetails;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.Segment;
import ucl.physiol.neuroconstruct.gui.SimpleFileFilter;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
import ucl.physiol.neuroconstruct.simulation.SimulationData;

/**
 * Class which generates a simple file for easy loading of simulation results into Igor Pro
 * (potentially with the free NeuroMatic set of functions from http://www.neuromatic.thinkrandom.com)
 *
 * @author Padraig Gleeson
 *  
 */

public class IgorNeuroMatic
{
    private static ClassLogger logger = new ClassLogger("IgorNeuroMatic");

    public static String INM_EXTENSION = ".ipf";

    public static String MAIN_LOAD_MACRO = "LoadAllSimData";



    public static void createSimulationLoader(Project project, SimConfig simConfig, String simRef)
    {
        File globalDir = ProjectStructure.getGlobalIgorNeuroMaticDir();

        SimpleFileFilter fileFilter
            = new SimpleFileFilter(new String[]{INM_EXTENSION},
                                   "Igor/NeuroMatic: "+INM_EXTENSION, false);

        File projINMDir = ProjectStructure.getProjIgorNeuroMaticDir(project.getProjectMainDirectory(), false);


        File[] globalIncludes = globalDir.listFiles(fileFilter);


        File simDir = new File(ProjectStructure.getSimulationsDir(project.getProjectMainDirectory()),
                                  simRef);

        File newIFile = new File(simDir, simRef+INM_EXTENSION);


        //System.out.println("Going to create file: "+newIFile);

        StringBuffer contents = new StringBuffer();

        contents.append(getFileHeader());

        contents.append("//  When this file is opened in Igor Pro (usually by double clicking) there will be Macros for loading the data.\n");
        contents.append("//  Select Macros -> Compile, and then Macro -> "+MAIN_LOAD_MACRO+"\n\n\n");


        contents.append("//  Note: this feature not very detailed at the moment. More suggestions for how to load the generated data into\n"+
                        "//  Igor/NeuroMatic would be appreciated (info@neuroConstruct.org) \n\n");


        contents.append("// Loading global scripts (*"+INM_EXTENSION+") from "+globalDir.getAbsolutePath()+"\n");

        if (globalIncludes.length==0)
            contents.append("// No files found in that directory\n\n");


        for (int i = 0; i < globalIncludes.length; i++)
        {
            String fullFileName = globalIncludes[i].getAbsolutePath();

            try
            {
                GeneralUtils.copyFileIntoDir(globalIncludes[i], simDir);

                String script = IgorNeuroMatic.convertFilePath(fullFileName);
                script = script.substring(0, script.lastIndexOf("."));

                contents.append("#include \"" + script + "\"\n\n");

            }
            catch (IOException ex1)
            {
                logger.logError("Error copying file into "+simDir, ex1);
            }
        }

        contents.append("// Loading project scripts (*"+INM_EXTENSION+") from "+projINMDir+"\n");

        if (projINMDir != null)
        {
            File[] projectIncludes = projINMDir.listFiles(fileFilter);
            for (int i = 0; i < projectIncludes.length; i++)
            {
                try
                {
                    String fullFileName = projectIncludes[i].getAbsolutePath();

                    GeneralUtils.copyFileIntoDir(projectIncludes[i], simDir);


                    String script = IgorNeuroMatic.convertFilePath(fullFileName);
                    script = script.substring(0, script.lastIndexOf("."));

                    contents.append("#include \"" + script + "\"\n\n");

                }
                catch (IOException ex1)
                {
                    logger.logError("Error copying file into " + simDir, ex1);
                }

            }

        }
        else
        {
            contents.append("// Directory not found\n\n");
        }

        contents.append("Macro "+MAIN_LOAD_MACRO+"()\n\n");


        contents.append("\n\nPrint\n");
        contents.append("Print \"Loading data from simulation: "+simRef+", project: "+project.getProjectName()+"\"\n\n");

        String loadPrePrefx = "LoadWave/A=";
        String loadPrefix = "/J/D/K=0 \""+ IgorNeuroMatic.convertFilePath(simDir.getAbsolutePath())+":";
        String loadSuffix = "\"";

        contents.append(loadPrePrefx+"time"+loadPrefix+SimulationData.getStandardTimesFilename()+loadSuffix+"\n\n");


        ArrayList<PlotSaveDetails> recordings = project.generatedPlotSaves.getSavedPlotSaves();

        for (PlotSaveDetails record : recordings)
        {
            String cellGroupName = record.simPlot.getCellGroup();

            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

            String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
            Cell cell = project.cellManager.getCell(cellType);

            if (numInCellGroup > 0)
            {
                for (Integer segId : record.segIdsToPlot)
                {
                    Segment segToRecord = cell.getSegmentWithId(segId);

                    for (Integer cellNum : record.cellNumsToPlot)
                    {
                        String fileName = SimPlot.getFilename(record, segToRecord, cellNum+"");
                        String ref = fileName.substring(0, fileName.lastIndexOf("."));
                        ref = GeneralUtils.replaceAllTokens(ref, ".", "_");
                        contents.append(loadPrePrefx+ref+loadPrefix + fileName+loadSuffix + "\n");
                    }

                }
            }
        }
        contents.append("\n\nPrint\n");
        contents.append("Print \"Finished loading data\"\n");

        contents.append("Print\n");
        try
        {
            FileWriter fwReadme = new FileWriter(newIFile);
            fwReadme.write(contents.toString());
            fwReadme.close();
            //System.out.println("Created file: "+newIFile+", "+newIFile.exists());
        }
        catch (IOException ex)
        {
            logger.logError("Exception creating file: " + newIFile + "...", ex);
        }

    }

    private static String convertFilePath(String filePath)
    {
        String newPath = GeneralUtils.replaceAllTokens(filePath, ":", ""); // change c:/folder/file to c/folder/file
        newPath = GeneralUtils.replaceAllTokens(newPath, "/", ":");       //change c/folder/file to c:folder:file
        newPath = GeneralUtils.replaceAllTokens(newPath, "\\", ":");       //change c\folder\file to c:folder:file
        return newPath;
    }

    private static String getFileHeader()
    {
        StringBuffer response = new StringBuffer();
        response.append("//  ******************************************************\n");
        response.append("// \n");
        response.append("//     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append("// \n");
        response.append("//  ******************************************************\n");

        response.append("\n\n");
        return response.toString();
    }

    public static void main(String[] args)
    {
        new IgorNeuroMatic();


        String path = "C:\\neuroConstruct\\projects\\Project_1iii\\igorNeuroMatic\\Tester2.ipf";


        System.out.println("Path: "+ path);
        System.out.println("Path conv: "+ convertFilePath(path));


    }
}
