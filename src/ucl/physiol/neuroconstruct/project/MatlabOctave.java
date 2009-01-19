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
 * Class which generates a simple file for easy loading of simulation results into Matlab,
 * or Octave (freely available at http://www.octave.org)
 *
 * @author Padraig Gleeson
 *  
 */

public class MatlabOctave
{
    private static ClassLogger logger = new ClassLogger("MatlabOctave");

    public static String LOAD_SIM = "loadsimdata";

    public static void createSimulationLoader(Project project, SimConfig simConfig, String simRef)
    {
        File globalDir = ProjectStructure.getGlobalMatlabOctaveDir();


        SimpleFileFilter fileFilter
            = new SimpleFileFilter(new String[]{".m"},
                                   "MATLAB/Octave files: *.m", false);

        File projMODir = ProjectStructure.getProjMatlabOctaveDir(project.getProjectMainDirectory(), false);


        File[] globalIncludes = globalDir.listFiles(fileFilter);


        File simDir = new File(ProjectStructure.getSimulationsDir(project.getProjectMainDirectory()),
                                  simRef);

        File newMOFile = new File(simDir, LOAD_SIM+".m");


        //System.out.println("Going to create file: "+newMOFile);

        StringBuffer contents = new StringBuffer();

        contents.append(getFileHeader());

        contents.append("%  This file allows the data generated during the simulation to be analysed in MATLAB/Octave\n\n\n");


        contents.append("%  Note: this feature is not very detailed at the moment. More suggestions for how to load the generated data into\n"+
                        "%  MATLAB/Octave would be appreciated (info@neuroConstruct.org) \n\n");


        contents.append("% Copying global scripts (*.m) from "+globalDir.getAbsolutePath()+"\n\n");

        if (globalIncludes.length==0)
            contents.append("% No files found in that directory\n\n");


        for (int i = 0; i < globalIncludes.length; i++)
        {
            String fileName = globalIncludes[i].getName();

            try
            {
                GeneralUtils.copyFileIntoDir(globalIncludes[i], simDir);

                //System.out.println("fileName: " + fileName);
                //String script = fileName.substring(0, fileName.lastIndexOf("."));
                contents.append("% Copied "+ fileName + " into directory\n\n");

            }
            catch (IOException ex1)
            {
                logger.logError("Error copying file into "+simDir, ex1);
            }
        }

        contents.append("% Copying project scripts (*.m) from "+ProjectStructure.getProjMatlabOctaveDirName(project.getProjectMainDirectory())+"\n\n");
        if (projMODir != null)
        {
            File[] projectIncludes = projMODir.listFiles(fileFilter);
            for (int i = 0; i < projectIncludes.length; i++)
            {
                try
                {
                    GeneralUtils.copyFileIntoDir(projectIncludes[i], simDir);

                    //String script = projectIncludes[i].getName().substring(0,
                    //                                                       projectIncludes[i].getName().lastIndexOf(
                    //    ".m"));
                    contents.append("% Copied "+ projectIncludes[i].getName() + "\n\n");
                }
                catch (IOException ex1)
                {
                    logger.logError("Error copying file into " + simDir, ex1);
                }

            }

        }
        else
        {
            contents.append("% Directory not found\n\n");
        }


        //contents.append("function "+LOAD_SIM+"()\n\n");

        contents.append("load('"+SimulationData.getStandardTimesFilename()+"');\n\n");


        ArrayList<PlotSaveDetails> recordings = project.generatedPlotSaves.getSavedPlotSaves();

        for (PlotSaveDetails record : recordings)
        {
            String cellGroupName = record.simPlot.getCellGroup();

            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

            String cellType = project.cellGroupsInfo.getCellType(cellGroupName);
            Cell cell = project.cellManager.getCell(cellType);



            if (numInCellGroup > 0)
            {
                boolean spikeData = record.simPlot.getValuePlotted().indexOf(SimPlot.SPIKE) >= 0;


                if (spikeData)
                {
                    contents.append("\n\nnumCells" + cellGroupName + " = " + numInCellGroup + ";\n");
                    contents.append("timespike" + cellGroupName + " = cell(1,numCells" + cellGroupName + ");\n\n\n");
                }
                else
                {
                    contents.append(cellGroupName + " = [");

                }

                for (int i = 0; i < record.segIdsToPlot.size(); i++)
                {
                    Segment segToRecord = cell.getSegmentWithId(record.segIdsToPlot.get(i));

                    for (Integer cellNum : record.cellNumsToPlot)
                    {
                        String fileName = SimPlot.getFilename(record, segToRecord, cellNum + "");
                        if (!spikeData)
                        {
                            contents.append(" load('" + fileName + "') ");
                        }
                        else
                        {
                            contents.append("timespike" + cellGroupName + "{"+(cellNum+1)+"} = load('" + fileName + "');\n");
                        }
                    }
                    if (!spikeData && i < record.segIdsToPlot.size() - 1)
                        contents.append(",");
                }
                if(!spikeData)
                {
                    contents.append("];\n\n\n");
                }
                else
                {
                    contents.append("\nfor cellIndex=1:numCells" + cellGroupName + "\n");


                    contents.append("    " + cellGroupName + "(:, cellIndex) = tracefromspikes(timespike" + cellGroupName + "{cellIndex}, time, 1,0);\n");

/*
                    contents.append("    spikecount = 1;\n");

                    contents.append("    insidespike = 0;\n");

                    contents.append("    for timeStep=1:length(time)\n");

                    contents.append("        newTime = time(timeStep);\n");

                    contents.append("        if ( (spikecount <= length(timespike" + cellGroupName + "{cellIndex})) & (newTime >= (timespike" + cellGroupName + "{cellIndex}(spikecount))))\n");

                    contents.append("            if (insidespike == 0)\n");
                    contents.append("                " + cellGroupName + "(timeStep, cellIndex) = 1;\n");
                    contents.append("                spikecount = spikecount + 1;\n");
                    contents.append("                insidespike = 1;\n");
                    contents.append("            else\n");
                    contents.append("                " + cellGroupName + "(timeStep, cellIndex) = 0;\n");
                    contents.append("                spikecount = spikecount + 1;\n");
                    contents.append("            end;\n");
                    contents.append("        else\n");

                    contents.append("            " + cellGroupName + "(timeStep, cellIndex) = 0;\n");
                    contents.append("            insidespike = 0;\n");
                    contents.append("        end;\n");

                    contents.append("    end;\n");
 */

                    contents.append("end;\n");

                }
                contents.append("\ndisp('Loaded traces of "+numInCellGroup+" cells in cell group: "+cellGroupName+"');\n");

            }



        }
        contents.append("\n\nwho\n");


        /*
        contents.append("\n\nfunction createSpikes()\n\n");

        for (String cellGroupName: project.cellGroupsInfo.getAllCellGroupNames())
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

            if (numInCellGroup > 0)
            {

                contents.append("disp('Creating spike object for cell group: "+cellGroupName+"')\n");
            }
        }

        contents.append("\nend\n");
        */
        //contents.append("\n\nend\n");

        try
        {
            FileWriter fwReadme = new FileWriter(newMOFile);
            fwReadme.write(contents.toString());
            fwReadme.close();
            //System.out.println("Created file: "+newMOFile+", "+newMOFile.exists());
        }
        catch (IOException ex)
        {
            logger.logError("Exception creating file: " + newMOFile + "...", ex);
        }

    }

    private static String getFileHeader()
    {
        StringBuffer response = new StringBuffer();
        response.append("%  ******************************************************\n");
        response.append("% \n");
        response.append("%     File generated by: neuroConstruct v"+GeneralProperties.getVersionNumber()+"\n");
        response.append("% \n");
        response.append("%  ******************************************************\n");

        response.append("\n\n");
        return response.toString();
    }

    public static void main(String[] args)
    {
        new MatlabOctave();
    }
}
