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
import ucl.physiol.neuroconstruct.hpc.mpi.MpiConfiguration;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;

import javax.vecmath.*;

/**
 * Thread to handle generation of node ids of the generated cells, possibly based on cell position and 
 * network connections
 *
 * @author Padraig Gleeson
 *  
 */


public class CompNodeGenerator extends Thread
{
    ClassLogger logger = new ClassLogger("CompNodeGenerator");

    public final static String myGeneratorType = "CompNodeGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;


    private SimConfig simConfig = null;



    public CompNodeGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New CompNodeGenerator created");
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
        logger.logComment("Running CompNodeGenerator thread...");
        startGenerationTime = System.currentTimeMillis();

        ArrayList<String> cellGroupNamesUnordered = simConfig.getCellGroups();
        LinkedList<String> cellGroupNames = new LinkedList<String>();
        
        MpiConfiguration mpiConfig = GeneralProperties.getMpiSettings().getMpiConfigurations().get(MpiSettings.favouredConfig);

        int totalProcs = mpiConfig.getTotalNumProcessors();
        
       

        for (String nextCG: cellGroupNamesUnordered)
        {
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
        
        Random r = ProjectManager.getRandomGenerator();


        for (int l = 0; l < cellGroupNames.size(); l++)
        {
            if (continueGeneration)
            {
                String nextCellGroup = cellGroupNames.get(l);

                logger.logComment(">>>>>   Generating compute nodes for cell group: " + nextCellGroup+", all cell groups: "+ cellGroupNames);

                this.myReportInterface.giveUpdate("Generating compute nodes for Cell Group: " + nextCellGroup+"...");

                ArrayList<PositionRecord> posRecs = project.generatedCellPositions.getPositionRecords(nextCellGroup);
                
                for(PositionRecord pos: posRecs)
                {
                    int nodeID = r.nextInt(totalProcs);
                    pos.nodeId = nodeID;
                }
                

            }
            if (myReportInterface != null) myReportInterface.majorStepComplete();
        }

        long positionsGeneratedTime = System.currentTimeMillis();
        float secondsPosns = (float) (positionsGeneratedTime - startGenerationTime) / 1000f;

        logger.logComment("Generating the report to send...");

        StringBuffer generationReport = new StringBuffer();
        
        String info = mpiConfig.toString();
        info = GeneralUtils.replaceAllTokens(info, "\n", "<br>");
        

        generationReport.append("<center><b>Compute nodes:</b></center>");


        if (!continueGeneration)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");


        generationReport.append("Time taken to generate compute nodes: " + secondsPosns + " seconds.<br>");
        

        generationReport.append("Compute nodes generated for:<br><b>"+info+"</b><br><br>");


        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }

}
