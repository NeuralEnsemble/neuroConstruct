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

import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.cell.*;
import javax.vecmath.*;

/**
 * Thread to handle generation of the cell positions based on the project settings
 *
 * @author Padraig Gleeson
 *  
 */


public class CellPositionGenerator extends Thread
{
    private ClassLogger logger = new ClassLogger("CellPositionGenerator");

    public final static String myGeneratorType = "CellPositionGenerator";

    private Project project = null;
    private long startGenerationTime;
    private boolean continueGeneration = true;

    private GenerationReport myReportInterface = null;

    /** @todo Make this settable */
    private int MAX_NUM_TRIES = 300;

    private SimConfig simConfig = null;



    public CellPositionGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New CellPositionGenerator created");
        this.project = project;

        if (reportInterface!=null)
        {
            myReportInterface = reportInterface;
        }

    }
    


    public void setSimConfig(SimConfig simConfig)
    {
        this.simConfig = simConfig;
    }


    public void stopGeneration()
    {
        logger.logComment("CellPositionGenerator being told to stop...");
        continueGeneration = false;
    }


    
 
    @Override
    public void run()
    {
        logger.logComment("Running CellPositionGenerator thread...");
        startGenerationTime = System.currentTimeMillis();


        //ArrayList<String> cellGroupNamesUnordered = simConfig.getCellGroups();
        LinkedList<String> cellGroupNames = simConfig.getPrioritizedCellGroups(project);

        logger.logComment("getPrioritizedCellGroups: "+ cellGroupNames);

        project.generatedCellPositions.reset();

        for (int l = 0; l < cellGroupNames.size(); l++)
        {
            if (continueGeneration)
            {
                String nextCellGroup = cellGroupNames.get(l);

                logger.logComment(">>>>>   Generating cell group: " + nextCellGroup+", all cell groups: "+ cellGroupNames);

                this.myReportInterface.giveUpdate("Generating Cell Group: " + nextCellGroup+"...");

                String cellType = project.cellGroupsInfo.getCellType(nextCellGroup);
                String regionName = project.cellGroupsInfo.getRegionName(nextCellGroup);

                Vector<CellPackingAdapter> adaptersToCheckForCollisions = new Vector<CellPackingAdapter>();

                for (int m = 0; m < l; m++) // look through previous cell groups...
                {
                    String otherCellGroup = cellGroupNames.get(m);

                    CellPackingAdapter otherAdapter
                        = project.cellGroupsInfo.getCellPackingAdapter(otherCellGroup);

                    adaptersToCheckForCollisions.add(otherAdapter);

                }

                CellPackingAdapter adapter = project.cellGroupsInfo.getCellPackingAdapter(nextCellGroup);

                if (adapter==null)
                {
                    String error = "Error finding cell packing adaptor!\nAsked for one for "+nextCellGroup+"\n"
                        + "Have:\n";
                    for(String cg: project.cellGroupsInfo.getAllCellGroupNames())
                    {
                        error = error +cg+": "+ project.cellGroupsInfo.getCellPackingAdapter(cg)+"\n";
                    }
                    error = error+project.getProjectFileName()+ "\n";
                    error = error+project.cellGroupsInfo.getAllCellGroupNames()+ "\n";
                    error = error+project.cellManager.getAllCells()+ "\n";
                    GuiUtils.showErrorMessage(logger, error, null, null);
                    return;
                }

                adapter.reset();

                Cell cell = project.cellManager.getCell(cellType);
                
                if (cell==null)
                {
                    GuiUtils.showErrorMessage(logger, "Error finding cell for type: "+ cellType+".\n" +
                            "Make sure there is a cell of that type in the project.\n" +
                            "Try pressing Validate for more information", null, null);
                    return;
                }

                logger.logComment("Adapter for this cell group: " + adapter);

                adapter.addRegionAndCellInfo(project.regionsInfo.getRegionObject(regionName),
                                             cell);
                try
                {
                    int numCellsInGroupSoFar = 0;
                    int triesAtFittingOneCell = 0;

                    while (triesAtFittingOneCell < MAX_NUM_TRIES && continueGeneration)
                    {

                        Point3f nextPosn = adapter.getNextPosition();

                        logger.logComment("Trying position " + nextPosn
                                          + " for cell number: " + numCellsInGroupSoFar
                                          + ". Have tried " + triesAtFittingOneCell
                                          + " time(s) so far to fit it...");

                        boolean canBeUsed = true;

                        if (adapter.avoidOtherCellGroups())
                        {
                            //System.out.println("Checking...");
                            for (int k = 0; k < adaptersToCheckForCollisions.size(); k++)
                            {
                                CellPackingAdapter otherAdapter = adaptersToCheckForCollisions.elementAt(k);

                                boolean collision
                                    = otherAdapter.doesCellCollideWithExistingCells(nextPosn, cell);

                                if (collision)
                                {
                                    canBeUsed = false;
                                }
                            }
                        }
                        if (canBeUsed)
                        {
                            logger.logComment("That point can be used...");

                            PositionRecord pr = new PositionRecord(numCellsInGroupSoFar,
                                                                       nextPosn.x,
                                                                       nextPosn.y,
                                                                       nextPosn.z);
                            /* This should be done AFTER all cell pos, conns, inputs, etc. generated,
                             * to pereserve ablity to regenerate old networks from neuroConstruct rand seeds
                            if (!cell.getInitialPotential().isTypeFixedNum())
                            {
                                float initPot = cell.getInitialPotential().getNextNumber();
                                pr.setInitV(initPot);
                            }*/

                            project.generatedCellPositions.addPosition(nextCellGroup,
                                                                       pr);
                            numCellsInGroupSoFar++;
                            triesAtFittingOneCell = 0;
                        }
                        else
                        {
                            logger.logComment("That point can't be used...");
                            adapter.cancelPosition(nextPosn);
                            triesAtFittingOneCell++;
                        }

                    }
                    logger.logComment("Reached end of trying to fit the cell...");
                }
                catch (CellPackingException ex)
                {
                    logger.logComment("Reached end of generating positions for cell group: " +
                                      nextCellGroup);
                    logger.logComment("Reason for ending: " + ex);
                    logger.logComment("Number in cell group: " + adapter.getCurrentNumberPositions());
                }

            }
            if (myReportInterface != null) myReportInterface.majorStepComplete();
        }

        long positionsGeneratedTime = System.currentTimeMillis();
        float secondsPosns = (float) (positionsGeneratedTime - startGenerationTime) / 1000f;

        logger.logComment("Generating the report to send...");

        StringBuffer generationReport = new StringBuffer();

        generationReport.append("Cell positions generated for Sim Config: <b>"+this.simConfig.getName()+"</b>.<br><br>");

        if (!continueGeneration)
            generationReport.append("<center><b>NOTE: Generation was interrupted</b></center><br>");



        generationReport.append("<center><b>Cell Groups:</b></center>");

        int totCells = project.generatedCellPositions.getNumberInAllCellGroups();
        int totCG = project.generatedCellPositions.getNumberNonEmptyCellGroups();


        generationReport.append("Time taken to generate <b>"+totCells+"</b> cell positions in <b>"+totCG+"</b> cell groups: " + secondsPosns + " seconds.<br>");

        generationReport.append(project.generatedCellPositions.getHtmlReport());

        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }

}
