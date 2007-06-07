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

import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import ucl.physiol.neuroconstruct.cell.*;
import javax.vecmath.*;

/**
 * Thread to handle generation of the cell positions based on the project settings
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class CellPositionGenerator extends Thread
{
    ClassLogger logger = new ClassLogger("CellPositionGenerator");

    public final static String myGeneratorType = "CellPositionGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    /** @todo Make this settable */
    int MAX_NUM_TRIES = 300;

    private SimConfig simConfig = null;



    public CellPositionGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New CellPositionGenerator created");
        this.project = project;

        myReportInterface = reportInterface;

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

    public void run()
    {
        logger.logComment("Running CellPositionGenerator thread...");
        startGenerationTime = System.currentTimeMillis();


        ArrayList<String> cellGroupNamesUnordered = simConfig.getCellGroups();
        LinkedList<String> cellGroupNames = new LinkedList<String>();

        for (String nextCG: cellGroupNamesUnordered)
        {
            //System.out.println("Putting "+nextCG+" into: "+ cellGroupNames);
            int priority = project.cellGroupsInfo.getPriority(nextCG);
            if (cellGroupNames.size()==0)
            {
                cellGroupNames.add(nextCG);
            }
            else
            {
                int topPriority = project.cellGroupsInfo.getPriority(cellGroupNames.getFirst());
                int bottomPriority = project.cellGroupsInfo.getPriority(cellGroupNames.getLast());

                if (priority>topPriority)
                {
                    cellGroupNames.addFirst(nextCG);
                }
                else if (priority<bottomPriority)
                {
                    cellGroupNames.addLast(nextCG);
                }
                else
                {
                    int numToCheck = cellGroupNames.size()-1;

                    for (int i = 0; i < numToCheck; i++)
                    {
                        int upperPriority = project.cellGroupsInfo.getPriority(cellGroupNames.get(i));
                        int lowerPriority = project.cellGroupsInfo.getPriority(cellGroupNames.get(i+1));
                        if (priority==upperPriority)
                        {
                            cellGroupNames.add(i, nextCG);
                            i = cellGroupNames.size();
                        }
                        else if (priority<upperPriority && priority>lowerPriority )
                        {
                            cellGroupNames.add(i+1, nextCG);
                            i = cellGroupNames.size();
                        }
                    }
                }
            }
        }
        logger.logComment("Old order: "+ cellGroupNamesUnordered);
        logger.logComment("New order: "+ cellGroupNames);

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

                Vector adaptersToCheckForCollisions = new Vector();

                for (int m = 0; m < l; m++) // look through previous cell groups...
                {
                    String otherCellGroup = cellGroupNames.get(m);

                    CellPackingAdapter otherAdapter
                        = project.cellGroupsInfo.getCellPackingAdapter(otherCellGroup);

                    adaptersToCheckForCollisions.add(otherAdapter);

                }

                CellPackingAdapter adapter = project.cellGroupsInfo.getCellPackingAdapter(nextCellGroup);

                adapter.reset();

                Cell cell = project.cellManager.getCell(cellType);

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

                        logger.logComment("Trying position "
                                          + nextPosn
                                          + " for cell number: "
                                          + numCellsInGroupSoFar
                                          + ". Have tried "
                                          + triesAtFittingOneCell
                                          + " time(s) so far to fit it...");

                        boolean canBeUsed = true;

                        if (adapter.avoidOtherCellGroups())
                        {
                            //System.out.println("Checking...");
                            for (int k = 0; k < adaptersToCheckForCollisions.size(); k++)
                            {
                                CellPackingAdapter otherAdapter = (CellPackingAdapter)
                                    adaptersToCheckForCollisions.elementAt(k);

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
                            project.generatedCellPositions.addPosition(nextCellGroup,
                                                                       numCellsInGroupSoFar,
                                                                       nextPosn.x,
                                                                       nextPosn.y,
                                                                       nextPosn.z);
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
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");



        generationReport.append("<center><b>Cell Groups:</b></center>");

        generationReport.append("Time taken to generate positions: " + secondsPosns + " seconds.<br>");

        generationReport.append(project.generatedCellPositions.getHtmlReport());

        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }

}
