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
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import javax.vecmath.*;

/**
 * Thread to handle generation of the morphology based network connections
 * based on the project settings
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class MorphBasedConnGenerator extends Thread
{
    ClassLogger logger = new ClassLogger("MorphBasedConnGenerator");

    public final static String myGeneratorType = "MorphBasedConnGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    private SimConfig simConfig = null;

    public MorphBasedConnGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New MorphBasedConnGenerator created");
        this.project = project;

        myReportInterface = reportInterface;


    }

    public void setSimConfig(SimConfig simConfig)
    {
        this.simConfig = simConfig;
    }



    public void stopGeneration()
    {
        logger.logComment("MorphBasedConnGenerator being told to stop...");
        continueGeneration = false;
    }


    public ArrayList<String> getRelevantNetConns()
    {
        Vector allSimpleNetConns = project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames();

        ArrayList<String> allNetConnsInSimConfig = simConfig.getNetConns();

        ArrayList<String> simpNetConnsInSimConfig = new ArrayList<String>();

        for (int i = 0; i < allSimpleNetConns.size(); i++)
        {
            if (allNetConnsInSimConfig.contains(allSimpleNetConns.get(i)))
                simpNetConnsInSimConfig.add((String)allSimpleNetConns.get(i));
        }

        return simpNetConnsInSimConfig;

    }

    public void run()
    {
        logger.logComment("Running MorphBasedConnGenerator thread...");

        startGenerationTime = System.currentTimeMillis();

        ArrayList<String> simpNetConnsInSimConfig = getRelevantNetConns();

        for (int j = 0; j < simpNetConnsInSimConfig.size(); j++)
        {
            if (!continueGeneration)
            {
                logger.logComment("Discontinuing generation...");
                sendGenerationReport(true);
                return;
            }

            String netConnName = simpNetConnsInSimConfig.get(j);

            ConnectivityConditions connConds = project.morphNetworkConnectionsInfo.getConnectivityConditions(netConnName);

            logger.logComment("Looking at Network Connection: " + netConnName);


            this.myReportInterface.giveUpdate("Generating Net Conn: " + netConnName+"...");

            Vector<SynapticProperties> synPropList = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);

            String[] synTypeNames = new String[synPropList.size()];
            for (int i = 0; i < synPropList.size(); i++)
            {
                synTypeNames[i] = ((SynapticProperties)synPropList.elementAt(i)).getSynapseType();
            }

            String sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
            String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);
            Cell sourceCellInstance = project.cellManager.getCell(sourceCellType);
            int numberInSourceCellGroup
                = project.generatedCellPositions.getNumberInCellGroup(sourceCellGroup);


            String targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
            String targetCellType = project.cellGroupsInfo.getCellType(targetCellGroup);
            Cell targetCellInstance = project.cellManager.getCell(targetCellType);
            int numberInTargetCellGroup
                = project.generatedCellPositions.getNumberInCellGroup(targetCellGroup);

            String generationStartCellGroup = null;
            Cell generationStartCellInstance = null;
            int numberInGenStartCellGroup = -1;

            String generationFinishCellGroup = null;
            Cell generationFinishCellInstance = null;
            int numberInGenFinishCellGroup = -1;


            // NOTE: we create the name "GenerationStartCellGroup", etc for the cell group we generate from
            // and "GenerationFinishCellGroup" for the one we connect to, so if
            // generationDirection = SOURCE_TO_TARGET, GenerationStartCellGroup = source, etc.


            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
            {
                generationStartCellGroup = sourceCellGroup;
                generationStartCellInstance = sourceCellInstance;
                numberInGenStartCellGroup = numberInSourceCellGroup;

                generationFinishCellGroup = targetCellGroup;
                generationFinishCellInstance = targetCellInstance;
                numberInGenFinishCellGroup = numberInTargetCellGroup;
            }
            else
            {
                generationStartCellGroup = targetCellGroup;
                generationStartCellInstance = targetCellInstance;
                numberInGenStartCellGroup = numberInTargetCellGroup;

                generationFinishCellGroup = sourceCellGroup;
                generationFinishCellInstance = sourceCellInstance;
                numberInGenFinishCellGroup = numberInSourceCellGroup;

            }

            MaxMinLength maxMin = project.morphNetworkConnectionsInfo.getMaxMinLength(netConnName);

            SearchPattern searchPattern = project.morphNetworkConnectionsInfo.getSearchPattern(netConnName);

            logger.logComment("\n");

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


            ArrayList<Integer> finCellsMaxedOut = new ArrayList<Integer>(numberInGenFinishCellGroup);

            if (numberInGenStartCellGroup==0)
            {
                logger.logError("There are no cells in group: "+generationStartCellGroup
                               + " so abandoning generation of net conn: " + netConnName);
            }
            else if (numberInGenFinishCellGroup==0)
            {
                logger.logError("There are no cells in group: "+generationFinishCellGroup
                               + " so abandoning generation of net conn: " + netConnName);
            }
            else
            {
                for (int genStartCellNumber = 0; genStartCellNumber < numberInGenStartCellGroup; genStartCellNumber++)
                {
                    float numConnFloat = connConds.getNumConnsInitiatingCellGroup().getNextNumber();
                    int numberConnections = (int) numConnFloat;

                    if (numConnFloat != numberConnections)
                    {
                        if (ProjectManager.getRandomGenerator().nextFloat() < numConnFloat - numberConnections)
                            numberConnections = numberConnections + 1;
                    }

                    logger.logComment(".........   For cell number: " + genStartCellNumber + ", there will be " + numberConnections +
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
                                          + p
                                          + " for cell number: "
                                          + genStartCellNumber
                                          + ". Asking cell of type: "
                                          + generationStartCellInstance.toString()
                                          + " for a synaptic location");

                        SegmentLocation genStartConnPoint = null;

                        ArrayList<Integer> genFinishCellsAlreadyConnected = null;

                        if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                        {
                            genStartConnPoint
                                = CellTopologyHelper.getPossiblePreSynapticTerminal(generationStartCellInstance,
                                synTypeNames);

                            genFinishCellsAlreadyConnected = project.generatedNetworkConnections.getTargetCellIndices(
                                netConnName,
                                genStartCellNumber, true);
                        }
                        else
                        {
                            genStartConnPoint
                                = CellTopologyHelper.getPossiblePostSynapticTerminal(generationStartCellInstance,
                                synTypeNames);

                            genFinishCellsAlreadyConnected = project.generatedNetworkConnections.getSourceCellIndices(
                                netConnName,
                                genStartCellNumber, true);
                        }


                        if (genFinishCellsAlreadyConnected.size() ==
                            numberInGenFinishCellGroup &&
                            connConds.isOnlyConnectToUniqueCells())
                        {
                            logger.logComment("There are the max number of connections at the opposite cell group, " +
                                              "and each one has to be unique...");
                            continueSingleConnGeneration = false;
                        }
                        else
                        {
                            logger.logComment("There are currently "
                                              + genFinishCellsAlreadyConnected.size()
                                              + " unique connection cells in the opposite cell group: ");
                            logger.logComment(genFinishCellsAlreadyConnected.toString());
                        }



                        if (genStartConnPoint == null)
                        {
                            GuiUtils.showErrorMessage(logger, "Error getting synaptic location for: "+synPropList+" on cell of type " +
                                                      generationStartCellInstance.toString()+".\n"+
                                                      "Please ensure there is a Synaptic Mechanism of that name at tab Cell Mechanisms and that the locations where synaptic connections \n"
                                                      +"of that type are allowed on the cell are specified via Visualisation -> (View cell type) -> Synaptic Conn Locations.", null, null);
                            continueGeneration = false;
                        }

                        if (numberInGenFinishCellGroup == 0)
                        {
                            logger.logComment("There are no cells in The target group to connect to.");
                            continueSingleConnGeneration = false;
                        }

                        if (continueSingleConnGeneration && continueGeneration)
                        {
                            SegmentLocation genFinishConnPoint = null;


                            float connectionDistance = -1;

                            int genFinishCellNumber = -1;


            /****    COMPLETELY_RANDOM case...    ****/

                            if (searchPattern.type == SearchPattern.COMPLETELY_RANDOM)
                            {
                                logger.logComment("Linking will be done in completely random manner...");
                                logger.logComment("Asking cell of type: " + generationFinishCellInstance.toString() +
                                                  " for a synaptic location");

                                int numFaliedAttemptsMaxMin = 0;
                                boolean foundOne = false;
                                int numToTry = maxMin.getNumberAttempts();



                                int availableCellsToConnectTo = numberInGenFinishCellGroup;

                                while (!foundOne
                                       && numFaliedAttemptsMaxMin < numToTry
                                       && continueGeneration
                                       && finCellsMaxedOut.size()<availableCellsToConnectTo)
                                {
                                    logger.logComment("numFaliedAttemptsMaxMin: " + numFaliedAttemptsMaxMin);
                                    logger.logComment("finCellsMaxedOut.size(): " + finCellsMaxedOut.size());

                                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                    {
                                        genFinishConnPoint = CellTopologyHelper.getPossiblePostSynapticTerminal(
                                            generationFinishCellInstance,
                                            synTypeNames);
                                    }
                                    else
                                    {
                                        genFinishConnPoint = CellTopologyHelper.getPossiblePreSynapticTerminal(
                                            generationFinishCellInstance,
                                            synTypeNames);

                                    }

                                    logger.logComment("genFinishConnPoint: " + genFinishConnPoint);

                                    if (genFinishConnPoint == null)
                                    {
                                        logger.logError("Error getting synaptic location on cell of type " +
                                                        generationFinishCellInstance.toString(), null);
                                        continueGeneration = false;
                                    }
                                    else
                                    {
                                        boolean satisfiesMaxNumPerFinCell = false;

                                        boolean satisfiesUniqueness = false;

                                        availableCellsToConnectTo = numberInGenFinishCellGroup;

                                        while (!(satisfiesMaxNumPerFinCell && satisfiesUniqueness)
                                               && finCellsMaxedOut.size()<availableCellsToConnectTo)
                                        {
                                            genFinishCellNumber = ProjectManager.getRandomGenerator().nextInt(numberInGenFinishCellGroup);

                                            logger.logComment("Testing if cell num: " + genFinishCellNumber +
                                                              " is appropriate for cell number: "
                                                          + genStartCellNumber);

                                            logger.logComment("finCellsMaxedOut: " + finCellsMaxedOut);
                                            logger.logComment("availableCellsToConnectTo: " + availableCellsToConnectTo);

                                            ArrayList<Integer> connsOnFinishCell = null;

                                            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                            {
                                                connsOnFinishCell = project.generatedNetworkConnections.getSourceCellIndices(netConnName,
                                                    genFinishCellNumber, false);
                                            }
                                            else
                                            {
                                                connsOnFinishCell = project.generatedNetworkConnections.getTargetCellIndices(netConnName,
                                                    genFinishCellNumber, false);

                                            }

                                            logger.logComment("connsOnFinishCell: " + connsOnFinishCell);


                                            if ((connsOnFinishCell.size()+1) <= connConds.getMaxNumInitPerFinishCell())
                                            {
                                                logger.logComment("There are not more than: " + connConds.getMaxNumInitPerFinishCell()
                                                                  + " src conns on finish cell "+genFinishCellNumber);

                                                satisfiesMaxNumPerFinCell = true;

                                                if (connConds.isOnlyConnectToUniqueCells())
                                                {
                                                    if (!genFinishCellsAlreadyConnected.contains(new Integer(
                                                        genFinishCellNumber)))
                                                    {
                                                        satisfiesUniqueness = true;
                                                        logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                    }
                                                    else
                                                    {
                                                        logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                        satisfiesUniqueness = false;
                                                    }
                                                    availableCellsToConnectTo = numberInGenFinishCellGroup - genFinishCellsAlreadyConnected.size();
                                                    logger.logComment("--- availableCellsToConnectTo: " + availableCellsToConnectTo);

                                                }
                                                else
                                                {
                                                    satisfiesUniqueness = true;
                                                }

                                            }
                                            else
                                            {
                                                logger.logComment("There are already: " + connsOnFinishCell + " src conns on tgt cell "+genFinishCellNumber);

                                                satisfiesMaxNumPerFinCell = false;
                                                satisfiesUniqueness = false;

                                                if (!finCellsMaxedOut.contains(genFinishCellNumber))
                                                    finCellsMaxedOut.add(genFinishCellNumber);
                                            }
                                        }


                                        if (satisfiesMaxNumPerFinCell && satisfiesUniqueness)
                                        {
                                            float distanceApart = CellTopologyHelper.getSynapticEndpointsDistance(
                                                project,
                                                generationStartCellGroup,
                                                new SynapticConnectionEndPoint(genStartConnPoint,
                                                                               genStartCellNumber),
                                                generationFinishCellGroup,
                                                new SynapticConnectionEndPoint(genFinishConnPoint,
                                                                               genFinishCellNumber),
                                                maxMin.getDimension()
                                                );

                                            if (distanceApart >= maxMin.getMinLength()
                                                && distanceApart <= maxMin.getMaxLength())
                                            {
                                                foundOne = true;
                                                connectionDistance = distanceApart;
                                            }
                                            else
                                            {
                                                logger.logComment("The length: " + distanceApart + " isn't between " +
                                                                  maxMin.getMinLength() + " and " + maxMin.getMaxLength());
                                                numFaliedAttemptsMaxMin++;
                                                genFinishConnPoint = null;
                                                genFinishCellNumber = -1;
                                            }
                                        }
                                    }
                                }
                            }

            /****     RANDOM_CLOSE case...     ****/

                            else if (searchPattern.type == SearchPattern.RANDOM_CLOSE)
                            {
                                logger.logComment("Linking will be to a close, random dendritic section...");

                                int numberOfSectionsToCheck = searchPattern.randomCloseNumber;

                                float bestDistanceSoFar = Float.MAX_VALUE;

                                for (int o = 0; o < numberOfSectionsToCheck; o++)
                                {
                                    logger.logComment("Checking number " + o + " of the " + numberOfSectionsToCheck +
                                                      " sections I've to check");

                                    SegmentLocation tempGenFinishConnPoint = null;

                                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                    {
                                        tempGenFinishConnPoint = CellTopologyHelper.
                                            getPossiblePostSynapticTerminal(generationFinishCellInstance,
                                            synTypeNames);
                                    }
                                    else
                                    {
                                        tempGenFinishConnPoint = CellTopologyHelper.
                                            getPossiblePreSynapticTerminal(generationFinishCellInstance,
                                                                           synTypeNames);
                                    }

                                    logger.logComment("tempGenFinishConnPoint: " + tempGenFinishConnPoint);

                                    if (tempGenFinishConnPoint == null)
                                    {
                                        GuiUtils.showErrorMessage(logger,
                                                                  "Error getting synaptic location on cell of type " +
                                                                  generationFinishCellInstance.toString(), null, null);
                                        return;
                                    }

                                    int tempGenFinishCellNumber = -1;

                                    boolean satisfiesMaxNumPerFinCell = false;

                                    boolean satisfiesUniqueness = false;


                                    int numCellsNotConnTo = numberInGenFinishCellGroup;

                                    while (!(satisfiesMaxNumPerFinCell && satisfiesUniqueness)
                                           && finCellsMaxedOut.size() < numCellsNotConnTo)
                                    {
                                        tempGenFinishCellNumber = ProjectManager.getRandomGenerator().nextInt(numberInGenFinishCellGroup);

                                        logger.logComment("Testing if cell num: " + tempGenFinishCellNumber +
                                                          " is appropriate for cell number: "
                                                          + genStartCellNumber);

                                        logger.logComment("finCellsMaxedOut: " + finCellsMaxedOut);
                                        logger.logComment("numCellsNotConnTo: " + numCellsNotConnTo);
                                        logger.logComment("genFinishCellsAlreadyConnected: " + genFinishCellsAlreadyConnected);

                                        int numConnsOnFinishCell = 0;

                                        if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                        {
                                            numConnsOnFinishCell = project.generatedNetworkConnections.getSourceCellIndices(netConnName,
                                                tempGenFinishCellNumber, false).size();
                                        }
                                        else
                                        {
                                            numConnsOnFinishCell = project.generatedNetworkConnections.getTargetCellIndices(netConnName,
                                                tempGenFinishCellNumber, false).size();

                                        }

                                        logger.logComment("numConnsOnFinishCell: " + numConnsOnFinishCell);

                                        if ( (numConnsOnFinishCell + 1) <= connConds.getMaxNumInitPerFinishCell())
                                        {
                                            logger.logComment("There are not more than: " + connConds.getMaxNumInitPerFinishCell()
                                                              + " src conns on finish cell " + tempGenFinishCellNumber);
                                            satisfiesMaxNumPerFinCell = true;

                                            if (connConds.isOnlyConnectToUniqueCells())
                                            {
                                                if (!genFinishCellsAlreadyConnected.contains(new Integer(
                                                    tempGenFinishCellNumber)))
                                                {
                                                    satisfiesUniqueness = true;
                                                        logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                }
                                                else
                                                {
                                                    logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                    satisfiesUniqueness = false;
                                                }
                                                numCellsNotConnTo = numberInGenFinishCellGroup - genFinishCellsAlreadyConnected.size();
                                            }
                                            else
                                            {
                                                satisfiesUniqueness = true;
                                            }

                                        }
                                        else
                                        {
                                            logger.logComment("There are already: " + numConnsOnFinishCell + " src conns on tgt cell " +
                                                              tempGenFinishCellNumber);

                                            satisfiesMaxNumPerFinCell = false;

                                            if (!finCellsMaxedOut.contains(tempGenFinishCellNumber))
                                                finCellsMaxedOut.add(tempGenFinishCellNumber);

                                        }

                                    }

                                    logger.logComment("tempGenFinishCellNumber: " + tempGenFinishCellNumber);

                                    if(satisfiesMaxNumPerFinCell && satisfiesUniqueness)
                                    {
                                        SynapticConnectionEndPoint tempGenFinishEndpoint =
                                            new SynapticConnectionEndPoint(tempGenFinishConnPoint, tempGenFinishCellNumber);

                                        float distToThisPoint
                                            = CellTopologyHelper.getSynapticEndpointsDistance(
                                                project,
                                                generationStartCellGroup,
                                                new SynapticConnectionEndPoint(genStartConnPoint,
                                                                               genStartCellNumber),
                                                generationFinishCellGroup,
                                                tempGenFinishEndpoint,
                                                maxMin.getDimension()
                                            );

                                        if (distToThisPoint >= maxMin.getMinLength()
                                            && distToThisPoint <= maxMin.getMaxLength())
                                        {
                                            logger.logComment("This point falls inside the max/min range...");

                                            if (distToThisPoint < bestDistanceSoFar)
                                            {
                                                bestDistanceSoFar = distToThisPoint;
                                                genFinishConnPoint = tempGenFinishConnPoint;
                                                genFinishCellNumber = tempGenFinishCellNumber;
                                                logger.logComment("It's the best distance so far...");

                                                connectionDistance = bestDistanceSoFar;
                                            }
                                            else
                                            {
                                                logger.logComment("Not good enough, genFinishCellNumber: "+ genFinishCellNumber);
                                            }
                                        }
                                        else
                                        {
                                            logger.logComment("It's outside the max/min range. Ignoring...");
                                        }
                                    }
                                    else
                                    {
                                        logger.logComment("satisfiesMaxNumPerFinCell: " + satisfiesMaxNumPerFinCell);
                                    }
                                }
                                logger.logComment("Finished checking the " + numberOfSectionsToCheck +
                                                  " cells I'd to check...");
                            }




            /****    CLOSEST case...    ****/

                            else if (searchPattern.type == SearchPattern.CLOSEST)
                            {
                                logger.logComment(
                                    "Linking will be to the closest dendritic/axonal section..");

                                Point3f relativeSynPointGenStart
                                    = CellTopologyHelper.convertSegmentDisplacement(
                                    generationStartCellInstance,
                                    genStartConnPoint.getSegmentId(),
                                    genStartConnPoint.getFractAlong());

                                Point3f genStartCellPosition
                                    = project.generatedCellPositions.getOneCellPosition(
                                    generationStartCellGroup,
                                    genStartCellNumber);

                                Point3f absGenStartSynPosition = new Point3f(genStartCellPosition);
                                absGenStartSynPosition.add(relativeSynPointGenStart);

                                float bestDistanceSoFar = Float.MAX_VALUE;

                                RectangularBox surroundingBox = CellTopologyHelper.getSurroundingBox(generationFinishCellInstance, false, false);

                                for (int l = 0; l < numberInGenFinishCellGroup; l++)
                                {
                                    logger.logComment("Checking cell number: " + l + " in cell group: " +
                                                      generationFinishCellGroup);

                                    if (connConds.isOnlyConnectToUniqueCells() &&
                                        genFinishCellsAlreadyConnected.contains(new Integer(l)))
                                    {
                                        logger.logComment(
                                            "Ignoring this cell, as it's already connected to the start cell, and uniqueness is specified");
                                    }
                                    else
                                    {

                                        boolean satisfiesMaxNumPerFinCell = false;

                                        int numConnsOnFinishCell = 0;
                                        if (connConds.getGenerationDirection() ==
                                            ConnectivityConditions.SOURCE_TO_TARGET)
                                        {
                                            numConnsOnFinishCell = project.generatedNetworkConnections.
                                                getSourceCellIndices(netConnName,
                                                                     l, false).size();
                                        }
                                        else
                                        {
                                            numConnsOnFinishCell = project.generatedNetworkConnections.
                                                getTargetCellIndices(netConnName,
                                                                     l, false).size();

                                        }

                                        logger.logComment("numConnsOnFinishCell: " + numConnsOnFinishCell);

                                        if ( (numConnsOnFinishCell + 1) <= connConds.getMaxNumInitPerFinishCell())
                                        {
                                            logger.logComment("There are not more than: " +
                                                              connConds.getMaxNumInitPerFinishCell()
                                                              + " src conns on finish cell " + l);

                                                satisfiesMaxNumPerFinCell = true;



                                        }
                                        else
                                        {
                                            logger.logComment("There are already: " + numConnsOnFinishCell +
                                                              " src conns on tgt cell " + l);

                                            if (!finCellsMaxedOut.contains(l))
                                                finCellsMaxedOut.add(l);

                                            satisfiesMaxNumPerFinCell = false;
                                        }


                                        if(satisfiesMaxNumPerFinCell)
                                        {
                                            Point3f absoluteGenFinishCellPosition
                                                = project.generatedCellPositions.getOneCellPosition(
                                                    generationFinishCellGroup,
                                                    l);

                                            Point3f posnStartSynapseRelToFinishCell = new Point3f(absGenStartSynPosition);
                                            posnStartSynapseRelToFinishCell.sub(absoluteGenFinishCellPosition);

                                            SegmentLocation bestPointOnGenFinishCell = null;

                                            RectangularBox absSurroundingBox = (RectangularBox) surroundingBox.getTranslatedRegion(new Vector3f(
                                                absoluteGenFinishCellPosition));
                                            SphericalRegion enclosingSphere = RectangularBox.getEnclosingSphere(absSurroundingBox);

                                            if (absGenStartSynPosition.distance(enclosingSphere.getCentre()) - enclosingSphere.getRadius() <= bestDistanceSoFar)
                                            {

                                                if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                                {
                                                    bestPointOnGenFinishCell
                                                        = CellTopologyHelper.getClosestPostSynapticTerminalLocation(
                                                            generationFinishCellInstance,
                                                            synTypeNames,
                                                            posnStartSynapseRelToFinishCell);
                                                }
                                                else
                                                {
                                                    bestPointOnGenFinishCell
                                                        = CellTopologyHelper.getClosestPreSynapticTerminalLocation(
                                                            generationFinishCellInstance,
                                                            synTypeNames,
                                                            posnStartSynapseRelToFinishCell);

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
                                                        generationFinishCellGroup,
                                                        tempGenFinishEndpoint,
                                                        maxMin.getDimension()
                                                    );

                                                logger.logComment("Distance to that point: " + distToThisPoint);

                                                if (distToThisPoint < bestDistanceSoFar)
                                                {
                                                    genFinishConnPoint = bestPointOnGenFinishCell;
                                                    genFinishCellNumber = l;
                                                    logger.logComment("Best so far...");

                                                    bestDistanceSoFar = distToThisPoint;


                                                    connectionDistance = bestDistanceSoFar;
                                                }
                                                else logger.logComment("Close but no cigar...");
                                            }
                                        }
                                    }

                                }

                            }


                            if (continueSingleConnGeneration
                                && genFinishCellNumber >= 0
                                && genFinishConnPoint != null
                               && finCellsMaxedOut.size()<numberInGenFinishCellGroup)
                            {
                                logger.logComment("Generated a synaptic point for cell number " +
                                                  genFinishCellNumber + ": ");

                                logger.logComment(genFinishConnPoint.toString());

                                ArrayList<ConnSpecificProps> props = new ArrayList<ConnSpecificProps>();

                                for (SynapticProperties synProp: synPropList)
                                {
                                    if (!synProp.getDelayGenerator().isTypeFixedNum()
                                        || !synProp.getWeightsGenerator().isTypeFixedNum())
                                    {
                                        ConnSpecificProps csp = new ConnSpecificProps(synProp.getSynapseType());

                                        csp.internalDelay = synProp.getDelayGenerator().getNextNumber();
                                        csp.weight = synProp.getWeightsGenerator().getNextNumber();

                                        props.add(csp);
                                    }
                                }
                                if (props.size()==0) props = null;

                                Float propDelay = connectionDistance/project.morphNetworkConnectionsInfo.getAPSpeed(netConnName) ;

                                if (project.morphNetworkConnectionsInfo.getAPSpeed(netConnName)==Float.MAX_VALUE)
                                    propDelay = 0f;


                                if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                {
                                    project.generatedNetworkConnections.addSynapticConnection(netConnName,
                                        GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                        genStartCellNumber,
                                        genStartConnPoint.getSegmentId(),
                                        genStartConnPoint.getFractAlong(),
                                        genFinishCellNumber,
                                        genFinishConnPoint.getSegmentId(),
                                        genFinishConnPoint.getFractAlong(),
                                        propDelay,
                                        props);
                                }
                                else
                                {
                                    project.generatedNetworkConnections.addSynapticConnection(netConnName,
                                        GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION,
                                        genFinishCellNumber,
                                        genFinishConnPoint.getSegmentId(),
                                        genFinishConnPoint.getFractAlong(),
                                        genStartCellNumber,
                                        genStartConnPoint.getSegmentId(),
                                        genStartConnPoint.getFractAlong(),
                                        propDelay,
                                        props);
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
            }
            logger.logComment("Finished looking at all " + numberInGenStartCellGroup + " cells in group: " +
                              generationStartCellGroup);

            if (myReportInterface != null) myReportInterface.majorStepComplete();

        } // for...

        // Finished the main generation part...

        sendGenerationReport(false);

    }

    private void sendGenerationReport(boolean interrupted)
    {
        StringBuffer generationReport = new StringBuffer();

        long netConnsGenerationTime = System.currentTimeMillis();
        float seconds = (float) (netConnsGenerationTime - startGenerationTime) / 1000f;

        int totNum = project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION);

        generationReport.append("<center><b>Morphology Based Network Connections:</b></center>");

        if (interrupted)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");


        if (totNum==0)
        {
            generationReport.append("No connections generated.<br><br>");
        }
        else
        {
            generationReport.append("Time taken to generate "
                                    + totNum
                                    + " network connections: " + seconds + " seconds.<br>");

            generationReport.append(project.generatedNetworkConnections.getHtmlReport(GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION, simConfig));

        }




        logger.logComment("Sending: "+ generationReport);
        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }


}
