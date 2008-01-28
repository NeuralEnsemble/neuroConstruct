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

package ucl.physiol.neuroconstruct.project;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * Thread to handle generation of the complex connections based on the project settings
 *
 * @author Padraig Gleeson
 *  
 */


public class ComplexConnectionGenerator extends Thread
{
    ClassLogger logger = new ClassLogger("ComplexConnectionGenerator");

    public final static String myGeneratorType = "ComplexConnectionGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    private SimConfig simConfig = null;



    public ComplexConnectionGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New ComplexConnectionGenerator created");
        this.project = project;

        myReportInterface = reportInterface;

    }

    public void setSimConfig(SimConfig simConf)
    {
        this.simConfig = simConf;
    }

    public SimConfig getSimConfig()
    {
        return this.simConfig;
    }



    public void stopGeneration()
    {
        logger.logComment("ComplexConnectionGenerator being told to stop...");
        continueGeneration = false;
    }



    public void run()
    {
        logger.logComment("Running ComplexConnectionGenerator thread...");

        return;
        //////////// removed all func...

        /*

        startGenerationTime = System.currentTimeMillis();

        ArrayList<String> compNetConnsInSimConfig = getRelevantNetConns();

        for (int j = 0; j < compNetConnsInSimConfig.size(); j++)
        {

            if (!continueGeneration)
            {
                logger.logComment("Discontinuing generation...");
                sendGenerationReport(true);
                return;
            }

            String complexConnName =  compNetConnsInSimConfig.get(j);

            logger.logComment("Looking at Complex Connection: " + complexConnName);

            ConnectivityConditions connConds = project.complexConnectionsInfo.getConnectivityConditions(complexConnName);



            Vector synapticPropList = project.complexConnectionsInfo.getSynapseList(complexConnName);

            String sourceCellGroup = project.complexConnectionsInfo.getSourceCellGroup(complexConnName);
            String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);
            Cell sourceCellInstance = project.cellManager.getCell(sourceCellType);
            int numberInSourceCellGroup
                = project.generatedCellPositions.getNumberInCellGroup(sourceCellGroup);


            String targetCellGroup = project.complexConnectionsInfo.getTargetCellGroup(complexConnName);
            String targetCellType = project.cellGroupsInfo.getCellType(targetCellGroup);
            Cell targetCellInstance = project.cellManager.getCell(targetCellType);
            int numberInTargetCellGroup
                = project.generatedCellPositions.getNumberInCellGroup(targetCellGroup);

            String generationStartCellGroup = null;
            String generationStartCellType = null;
            Cell generationStartCellInstance = null;
            int numberInGenStartCellGroup = -1;
            int genStartSectionType;

            String generationFinishCellGroup = null;
            String generationFinishCellType = null;
            Cell generationFinishCellInstance = null;
            int numberInGenFinishCellGroup = -1;
            int genFinishSectionType;


            // Note we create the name "GenerationStartCellGroup", etc for the cell group we generate from
            // and "GenerationFinishCellGroup" for the one we connect to, so if
            // generationDirection = SOURCE_TO_TARGET, GenerationStartCellGroup = source, etc.
            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
            {
                generationStartCellGroup = sourceCellGroup;
                generationStartCellType = sourceCellType;
                generationStartCellInstance = sourceCellInstance;
                numberInGenStartCellGroup = numberInSourceCellGroup;
                //genStartSectionType = PositionedSection.AXONAL_SECTION;

                generationFinishCellGroup = targetCellGroup;
                generationFinishCellType = targetCellType;
                generationFinishCellInstance = targetCellInstance;
                numberInGenFinishCellGroup = numberInTargetCellGroup;
                //genFinishSectionType = PositionedSection.DENDRITIC_SECTION;
            }
            else
            {
                generationStartCellGroup = targetCellGroup;
                generationStartCellType = targetCellType;
                generationStartCellInstance = targetCellInstance;
                numberInGenStartCellGroup = numberInTargetCellGroup;
                //genStartSectionType = PositionedSection.DENDRITIC_SECTION;

                generationFinishCellGroup = sourceCellGroup;
                generationFinishCellType = sourceCellType;
                generationFinishCellInstance = sourceCellInstance;
                numberInGenFinishCellGroup = numberInSourceCellGroup;
                //genFinishSectionType = PositionedSection.AXONAL_SECTION;

            }

            MaxMinLength maxMin = project.complexConnectionsInfo.getMaxMinLength(complexConnName);


            SearchPattern searchPattern = project.complexConnectionsInfo.getSearchPattern(complexConnName);

            logger.logComment("There are "
                              + numberInGenStartCellGroup
                              + " cells in cell group: "
                              + generationStartCellGroup
                              + ", and each will have connections given by: ("
                              + connConds
                              + ")");

            logger.logComment("These will synapse with "
                              + numberInGenFinishCellGroup
                              + " cells in cell group "
                              + generationFinishCellGroup);

            for (int genStartCellNumber = 0; genStartCellNumber < numberInGenStartCellGroup; genStartCellNumber++)
            {
                int numberConnections = (int)connConds.getNumConnsInitiatingCellGroup().getNextNumber();

                logger.logComment("For cell number: " + genStartCellNumber + ", there will be " + numberConnections +
                                  " connections");

                for (int p = 0; p < numberConnections; p++)
                {
                    if (!continueGeneration)
                    {
                        logger.logComment("Discontinuing generation...");
                        sendGenerationReport(true);
                        return;
                    }

                    boolean continueSingleConnGeneration = true;

                    logger.logComment("-----   Connection number "
                                      +p
                                      +" for cell number: "
                                      + genStartCellNumber
                                      +". Asking cell of type: "
                                      + generationStartCellInstance.toString()
                                      +" for a synaptic location");

                    SynapticConnectionLocation genStartConnPoint = null;

                    Vector genFinishCellsAlreadyConnected = null;

                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                    {
                        String[] synPropList = new String[synapticPropList.size()];
                        for (int i = 0; i < synapticPropList.size(); i++)
                        {
                                synPropList[i] = ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                        }
                        genStartConnPoint
                           = CellTopologyHelper.getPossiblePreSynapticTerminal(generationStartCellInstance,
                             synPropList);

                       genStartConnPoint.distanceAlong = 0.5f; // as its a glom...

                        genFinishCellsAlreadyConnected = project.generatedNetworkConnections.getTargetCellIndices(complexConnName,
                            genStartCellNumber, true);
                    }
                    else
                    {
                        String[] synPropList = new String[synapticPropList.size()];
                        for (int i = 0; i < synapticPropList.size(); i++)
                        {
                                synPropList[i] = ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                        }

                        genStartConnPoint
                           = CellTopologyHelper.getPossiblePostSynapticTerminal(generationStartCellInstance,
                             synPropList);

                        genFinishCellsAlreadyConnected = project.generatedNetworkConnections.getSourceCellIndices(complexConnName,
                            genStartCellNumber, true);
                    }

                    if (genFinishCellsAlreadyConnected.size() ==
                        numberInGenFinishCellGroup &&
                        connConds.isOnlyConnectToUniqueCells())
                    {
                        logger.logComment("There are the max number of connections at the opposite cell group, "+
                                          "and each one has to be unique...");
                        continueSingleConnGeneration = false;
                    }
                    else
                    {
                        logger.logComment("There are currently "
                                          +genFinishCellsAlreadyConnected.size()
                                          +" unique connection cells in the opposite cell group: ");
                        logger.logComment(genFinishCellsAlreadyConnected.toString());
                    }

                    if (genStartConnPoint == null)
                    {
                        GuiUtils.showErrorMessage(logger, "Error getting synaptic location on cell of type "+
                                                  generationStartCellInstance.toString() , null, null);
                        continueGeneration = false;
                    }


                    if (continueSingleConnGeneration)
                    {
                        SynapticConnectionLocation genFinishConnPoint = null;

                        int genFinishCellNumber = -1;

            // COMPLETELY_RANDOM case...

                        if (searchPattern.type == SearchPattern.COMPLETELY_RANDOM)
                        {
                            logger.logComment("Linking will be done in completely random manner...");
                            logger.logComment("Asking cell of type: " + generationFinishCellInstance.toString() +
                                              " for a synaptic location");

                            int numFaliedAttempts = 0;
                            boolean foundOne = false;
                            int numToTry = maxMin.numberAttempts;

                            while (!foundOne
                                   && numFaliedAttempts < numToTry
                                   && continueGeneration)
                            {
                                if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                {

                                    String[] synPropList = new String[synapticPropList.size()];
                                    for (int i = 0; i < synapticPropList.size(); i++)
                                    {
                                            synPropList[i] =  ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();;
                                    }

                                    genFinishConnPoint = CellTopologyHelper.getPossiblePostSynapticTerminal(
                                        generationFinishCellInstance, synPropList);
                                }
                                else
                                {

                                    String[] synPropList = new String[synapticPropList.size()];
                                    for (int i = 0; i < synapticPropList.size(); i++)
                                    {
                                            synPropList[i] =  ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                                    }

                                    genFinishConnPoint = CellTopologyHelper.getPossiblePreSynapticTerminal(
                                        generationFinishCellInstance, synPropList);

                                    genFinishConnPoint.distanceAlong = 0.5f;// for a glom...

                                }

                                if (genFinishConnPoint == null)
                                {
                                    logger.logError("Error getting synaptic location on cell of type " +
                                                    generationFinishCellInstance.toString(), null);
                                    continueGeneration = false;
                                }
                                else
                                {
                                    boolean satisfiesUniqueness = false;

                                    while (!satisfiesUniqueness)
                                    {
                                        genFinishCellNumber = ProjectManager.getRandomGenerator().nextInt(numberInGenFinishCellGroup);

                                        if (connConds.isOnlyConnectToUniqueCells())
                                        {
                                            if (!genFinishCellsAlreadyConnected.contains(new Integer(genFinishCellNumber)))
                                                satisfiesUniqueness = true;
                                        }
                                        else
                                        {
                                            satisfiesUniqueness = true;
                                        }
                                    }

                                    float distanceApart = CellTopologyHelper.getSynapticEndpointsDistance(
                                        project,
                                        generationStartCellGroup,
                                        new SynapticConnectionEndPoint(genStartConnPoint,
                                                                       genStartCellNumber),
                                        //genStartSectionType,
                                        generationFinishCellGroup,
                                        new SynapticConnectionEndPoint(genFinishConnPoint,
                                                                       genFinishCellNumber)
                                       //, genFinishSectionType
                                     );

                                    if (distanceApart >= maxMin.minLength
                                        && distanceApart <= maxMin.maxLength)
                                    {
                                        foundOne = true;
                                    }
                                    else
                                    {
                                        logger.logComment("The length: " + distanceApart + " isn't between " +
                                                          maxMin.minLength + " and " + maxMin.maxLength);
                                        numFaliedAttempts++;
                                        genFinishConnPoint = null;
                                        genFinishCellNumber = -1;
                                    }

                                }
                            }
                        }

                // RANDOM_CLOSE case...

                        else if (searchPattern.type == SearchPattern.RANDOM_CLOSE)
                        {
                            logger.logComment("Linking will be to a close, random dendritic section...");

                            int numberOfSectionsToCheck = searchPattern.randomCloseNumber;

                            if (connConds.isOnlyConnectToUniqueCells())
                            {
                                // if there are fewer unique target cells left than the
                                // number we've to try, then we use that number

                                int numGenFinishCellsLeftAvailable
                                    = numberInGenFinishCellGroup
                                      - genFinishCellsAlreadyConnected.size();

                                if (numGenFinishCellsLeftAvailable<numberOfSectionsToCheck)
                                    numberOfSectionsToCheck = numGenFinishCellsLeftAvailable;
                            }


                            float bestDistanceSoFar = Float.MAX_VALUE;

                            for (int o = 0; o < numberOfSectionsToCheck; o++)
                            {
                                logger.logComment("Checking number "+ o+ " of the "+numberOfSectionsToCheck+ " sections I've to check");

                                SynapticConnectionLocation tempGenFinishConnPoint = null;

                                if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                {

                                    String[] synPropList = new String[synapticPropList.size()];
                                    for (int i = 0; i < synapticPropList.size(); i++)
                                    {
                                            synPropList[i] =  ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                                    }

                                    tempGenFinishConnPoint = CellTopologyHelper.
                                    getPossiblePostSynapticTerminal(generationFinishCellInstance,
                                                                    synPropList);

                                }
                                else
                                {

                                    String[] synPropList = new String[synapticPropList.size()];
                                    for (int i = 0; i < synapticPropList.size(); i++)
                                    {
                                            synPropList[i] =  ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                                    }

                                    tempGenFinishConnPoint = CellTopologyHelper.
                                    getPossiblePreSynapticTerminal(generationFinishCellInstance,
                                                                    synPropList);

                                     genFinishConnPoint.distanceAlong = 0.5f;// for a glom...

                                }

                                if (tempGenFinishConnPoint == null)
                                {
                                    GuiUtils.showErrorMessage(logger, "Error getting synaptic location on cell of type " +
                                                    generationFinishCellInstance.toString(), null, null);
                                    return;
                                }

                                int tempGenFinishCellNumber =-1;
                                boolean satisfiesUniqueness = false;

                                while (!satisfiesUniqueness)
                                {
                                    tempGenFinishCellNumber = ProjectManager.getRandomGenerator().nextInt(numberInGenFinishCellGroup);
                                    logger.logComment("Testing if cell num: "+tempGenFinishCellNumber+" is appropriate");
                                    if (connConds.isOnlyConnectToUniqueCells())
                                    {
                                        if (!genFinishCellsAlreadyConnected.contains(new Integer(tempGenFinishCellNumber)))
                                        {
                                            logger.logComment("Unique...");
                                            satisfiesUniqueness = true;
                                        }
                                        else
                                            logger.logComment("Not unique...");
                                    }
                                    else
                                    {
                                        logger.logComment("Doesn't matter. Uniqueness not required...");
                                        satisfiesUniqueness = true;
                                    }
                                }


                                SynapticConnectionEndPoint tempGenFinishEndpoint =
                                    new SynapticConnectionEndPoint(tempGenFinishConnPoint, tempGenFinishCellNumber);

                                float distToThisPoint
                                    = CellTopologyHelper.getSynapticEndpointsDistance(
                                    project,
                                    generationStartCellGroup,
                                    new SynapticConnectionEndPoint(genStartConnPoint,
                                                                   genStartCellNumber),
                                    //genStartSectionType,
                                    generationFinishCellGroup,
                                    tempGenFinishEndpoint
                                     //, genFinishSectionType
                                    );

                                if (distToThisPoint >= maxMin.minLength
                                    && distToThisPoint <= maxMin.maxLength)
                                {
                                    logger.logComment("This point falls inside the max/min range...");

                                    if (distToThisPoint < bestDistanceSoFar)
                                    {
                                        bestDistanceSoFar = distToThisPoint;
                                        genFinishConnPoint = tempGenFinishConnPoint;
                                        genFinishCellNumber = tempGenFinishCellNumber;
                                        logger.logComment("It's the best distance so far...");
                                    }
                                    else
                                    {
                                        logger.logComment("Not good enough...");
                                    }
                                }
                                else
                                {
                                    logger.logComment("It's outside the max/min range. Ignoring...");
                                }

                            }
                            logger.logComment("Finished checking the "+numberOfSectionsToCheck+" cells I'd to check...");
                        }

                // CLOSEST case...

                        else if (searchPattern.type == SearchPattern.CLOSEST)
                        {
                            logger.logComment(
                                "Linking will be to the closest dendritic/axonal section..");

                            Point3f relativeSynPointGenStart
                                    = CellTopologyHelper.convertSegmentDisplacement(
                                    generationStartCellInstance,
                                    //genStartSectionType,
                                    genStartConnPoint.segmentId,
                                    genStartConnPoint.distanceAlong);


                            Point3f genStartCellPosition
                                = project.generatedCellPositions.getOneCellPosition(
                                generationStartCellGroup,
                                genStartCellNumber);

                            Point3f absGenStartSynPosition = new Point3f(genStartCellPosition);
                            absGenStartSynPosition.add(relativeSynPointGenStart);

                            float bestDistanceSoFar = Float.MAX_VALUE;

                            for (int l = 0; l < numberInGenFinishCellGroup; l++)
                            {
                                logger.logComment("Checking cell number: " + l + " in cell group: " +
                                                  generationFinishCellGroup);

                                if(connConds.isOnlyConnectToUniqueCells() &&
                                   genFinishCellsAlreadyConnected.contains(new Integer(l)))
                                {
                                    logger.logComment("Ignoring this cell, as it's already connected to the start cell, and uniqueness is specified");

                                }
                                else
                                {
                                    Point3f absoluteGenFinishCellPosition
                                        = project.generatedCellPositions.getOneCellPosition(
                                                   generationFinishCellGroup,
                                                   l);

                                    Point3f posnStartSynapseRelToFinishCell = new Point3f(absGenStartSynPosition);
                                    posnStartSynapseRelToFinishCell.sub(absoluteGenFinishCellPosition);

                                    SynapticConnectionLocation bestPointOnGenFinishCell = null;

                                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                    {

                                        String[] synPropList = new String[synapticPropList.size()];
                                        for (int i = 0; i < synapticPropList.size(); i++)
                                        {
                                                synPropList[i] =  ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                                        }

                                        bestPointOnGenFinishCell
                                            = CellTopologyHelper.getClosestPostSynapticTerminalLocation(
                                                 generationFinishCellInstance,
                                                 synPropList,
                                                 posnStartSynapseRelToFinishCell);
                                    }
                                    else
                                    {

                                        String[] synPropList = new String[synapticPropList.size()];
                                        for (int i = 0; i < synapticPropList.size(); i++)
                                        {
                                                synPropList[i] =  ((SynapticProperties)synapticPropList.elementAt(i)).getSynapseType();
                                        }

                                        bestPointOnGenFinishCell
                                            = CellTopologyHelper.getClosestPreSynapticTerminalLocation(
                                            generationFinishCellInstance,
                                            synPropList,
                                            posnStartSynapseRelToFinishCell);


                                            bestPointOnGenFinishCell.distanceAlong = 0.5f;// for a glom...

                                    }

                                    if (bestPointOnGenFinishCell == null)
                                    {
                                        GuiUtils.showErrorMessage(logger,
                                                                  "Error getting synaptic location on cell of type " +
                                                                  generationFinishCellInstance.toString(), null, null);
                                        return;
                                    }


                                    SynapticConnectionEndPoint tempGenFinishEndpoint =
                                        new SynapticConnectionEndPoint(bestPointOnGenFinishCell, l);

                                    float distToThisPoint
                                        = CellTopologyHelper.getSynapticEndpointsDistance(
                                        project,
                                        generationStartCellGroup,
                                        new SynapticConnectionEndPoint(genStartConnPoint,
                                                                       genStartCellNumber),
                                        //genStartSectionType,
                                        generationFinishCellGroup,
                                        tempGenFinishEndpoint
                                        //, genFinishSectionType
                                        );

                                    logger.logComment("Distance to that point: " + distToThisPoint);

                                    if (distToThisPoint < bestDistanceSoFar)
                                    {
                                        genFinishConnPoint = bestPointOnGenFinishCell;
                                        genFinishCellNumber = l;
                                        logger.logComment("Best so far...");

                                        bestDistanceSoFar = distToThisPoint;
                                    }
                                    else logger.logComment("Close but no cigar...");
                                }

                            }

                        }

                        if (continueSingleConnGeneration
                            && genFinishCellNumber >= 0
                            && genFinishConnPoint != null)
                        {
                            logger.logComment("Generated a synaptic point for cell number " +
                                              genFinishCellNumber + ": ");

                            logger.logComment(genFinishConnPoint.toString());

                            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                            {
                                project.generatedNetworkConnections.addSynapticConnection(complexConnName,
                                        GeneratedNetworkConnections.COMPLEX_NETWORK_CONNECTION,
                                    genStartCellNumber,
                                    genStartConnPoint.segmentId,
                                    genStartConnPoint.distanceAlong,
                                    genFinishCellNumber,
                                    genFinishConnPoint.segmentId,
                                    genFinishConnPoint.distanceAlong);
                            }
                            else
                            {
                                project.generatedNetworkConnections.addSynapticConnection(complexConnName,
                                        GeneratedNetworkConnections.COMPLEX_NETWORK_CONNECTION,
                                    genFinishCellNumber,
                                    genFinishConnPoint.segmentId,
                                    genFinishConnPoint.distanceAlong,
                                    genStartCellNumber,
                                    genStartConnPoint.segmentId,
                                    genStartConnPoint.distanceAlong);
                            }
                        }
                        else
                        {
                            logger.logComment("Not adding the generated position...");
                        }

                    } // if continue..
                    else
                    {
                        logger.logError("Error getting synaptic location on cell of type " +
                                        generationStartCellInstance.toString(), null);
                    }
                }
                logger.logComment("Finished creating the " + numberConnections + " conns for cell number: " +
                                  genStartCellNumber);
            }
            logger.logComment("Finished looking at all " + numberInGenStartCellGroup + " cells in group: " +
                              generationStartCellGroup);


            if (myReportInterface!=null) myReportInterface.majorStepComplete();

        } // for...


        // Finished the main generation part...

        sendGenerationReport(false);*/

    }
/*
    private void sendGenerationReport(boolean interrupted)
    {
        StringBuffer generationReport = new StringBuffer();


        ArrayList<String> compNetConnsInSimConfig = getRelevantNetConns();


        long netConnsGenerationTime = System.currentTimeMillis();
        float seconds = (float) (netConnsGenerationTime - startGenerationTime) / 1000f;

        generationReport.append("<center><b>Complex Connections:</b></center>");
        generationReport.append("Time taken to generate Complex connections: " + seconds +
                                " seconds.<br>");

        if (interrupted)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");

        if (compNetConnsInSimConfig.size() == 0)
        {
            generationReport.append("No Complex Connections generated<br>");

        }
        for (int i = 0; i < compNetConnsInSimConfig.size(); i++)
        {
            String complexConnName = compNetConnsInSimConfig.get(i);

            generationReport.append("<b>" + complexConnName + "</b> (From: "
                                    + project.complexConnectionsInfo.getSourceCellGroup(complexConnName)
                                    + " to: "
                                    + project.complexConnectionsInfo.getTargetCellGroup(complexConnName) +
                                    ")<br>");
            generationReport.append("Number of individual connections: "
                                    +
                                    project.generatedNetworkConnections.getSynapticConnections(complexConnName).
                                    size()
                                    + "<br>");
        }

        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }
*/

}
