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
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import javax.vecmath.*;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;

/**
 * Thread to handle generation of the morphology based network connections
 * based on the project settings
 *
 * @author Padraig Gleeson
 *  
 */


public class MorphBasedConnGenerator extends Thread
{
    private static ClassLogger logger = new ClassLogger("MorphBasedConnGenerator");

    public final static String myGeneratorType = "MorphBasedConnGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    private SimConfig simConfig = null;

    private ArrayList<String> generatingNetConns = new ArrayList<String>();
    
    private String synLocWarning = "Please ensure there is a Synaptic Mechanism of that name at tab Cell Mechanisms and that the locations where synaptic connections \n"
                                  +"of that type are allowed on the cell are specified via Visualisation -> (View cell type) -> Synaptic Conn Locations in drop down box.\n" +
                                  "Note also the conditions in the Net Conn on which of soma, axon, dendrite are allowed pre/post synaptically";

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

    @Override
    public void run()
    {
        logger.logComment("Running MorphBasedConnGenerator thread...");

        startGenerationTime = System.currentTimeMillis();

        ArrayList<String> simpNetConnsInSimConfig = getRelevantNetConns();

        int maxNumThreads = GeneralProperties.getNumProcessorstoUse();
        int waitMillis = 100;

        for (int j = 0; j < simpNetConnsInSimConfig.size(); j++)
        {
            if (!continueGeneration)
            {
                logger.logComment("Discontinuing generation...");
                sendGenerationReport(true);
                return;
            }

            String netConnName = simpNetConnsInSimConfig.get(j);

            SingleConnGenerator scg = new SingleConnGenerator(myReportInterface, netConnName);
            netConnStarting(netConnName);
            scg.start();

            logger.logComment("Generating: "+generatingNetConns+"...");

            while(netConnsRunning()>=maxNumThreads)
            {
                try
                {
                    logger.logComment("Waiting for: "+generatingNetConns+"...");
                    Thread.sleep(waitMillis);
                }
                catch (InterruptedException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Error when generating "+ netConnName, ex, null);
                }
            }

            
        } // loop over simpNetConnsInSimConfig


        while(netConnsRunning()>0)
        {
            try
            {
                logger.logComment("Waiting for: "+generatingNetConns+"...");
                Thread.sleep(waitMillis);
            }
            catch (InterruptedException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error when generating "+ generatingNetConns, ex, null);
            }
        }

        // Finished the main generation part...

        sendGenerationReport(false);

    }

    protected synchronized void netConnCompleted(String netConnName)
    {
        logger.logComment("Finished generating: "+netConnName+"...");
        generatingNetConns.remove(netConnName);
    }

    protected synchronized void netConnStarting(String netConnName)
    {
        logger.logComment("Starting generating: "+netConnName+"...");
        generatingNetConns.add(netConnName);
    }
    protected synchronized int netConnsRunning()
    {
        return generatingNetConns.size();
    }

    private class SingleConnGenerator extends Thread
    {

        GenerationReport myReportInterface = null;

        String netConnName = null;

        public SingleConnGenerator(GenerationReport reportInterface, String netConnName)
        {
            super("SingleConnGenerator_"+netConnName);
            this.myReportInterface = reportInterface;
            this.netConnName = netConnName;

        }

        @Override
        public void run()
        {

            CellTopologyHelper cth = new CellTopologyHelper(); // so that synapse locations can be cached...

            ConnectivityConditions connConds = project.morphNetworkConnectionsInfo.getConnectivityConditions(netConnName);

            logger.logComment("Looking at Network Connection: " + netConnName);

            this.myReportInterface.giveUpdate("Generating Net Conn: " + netConnName+"...");

            Vector<SynapticProperties> synPropList = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);

            String[] synTypeNames = new String[synPropList.size()];
            for (int i = 0; i < synPropList.size(); i++)
            {
                synTypeNames[i] = synPropList.elementAt(i).getSynapseType();
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

            String genStartCellGroup = null;
            Cell genStartCellInstance = null;
            int numberInGenStartCellGroup = -1;

            String genFinishCellGroup = null;
            Cell genFinishCellInstance = null;
            int numberInGenFinishCellGroup = -1;


            // NOTE: we create the name "GenerationStartCellGroup", etc for the cell group we generate from
            // and "GenerationFinishCellGroup" for the one we connect to, so if
            // generationDirection = SOURCE_TO_TARGET, GenerationStartCellGroup = source, etc.


            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
            {
                genStartCellGroup = sourceCellGroup;
                genStartCellInstance = sourceCellInstance;
                numberInGenStartCellGroup = numberInSourceCellGroup;

                genFinishCellGroup = targetCellGroup;
                genFinishCellInstance = targetCellInstance;
                numberInGenFinishCellGroup = numberInTargetCellGroup;
            }
            else
            {
                genStartCellGroup = targetCellGroup;
                genStartCellInstance = targetCellInstance;
                numberInGenStartCellGroup = numberInTargetCellGroup;

                genFinishCellGroup = sourceCellGroup;
                genFinishCellInstance = sourceCellInstance;
                numberInGenFinishCellGroup = numberInSourceCellGroup;

            }

            // the 2 following variables are used only if the soma to soma distance is checked (created here for optimization)

            Section sourceSec = sourceCellInstance.getFirstSomaSegment().getSection();
            Section targetSec = targetCellInstance.getFirstSomaSegment().getSection();

            Point3f sourceSomaPosition = CellTopologyHelper.convertSectionDisplacement(sourceCellInstance, sourceSec, (float) 0.5);

            Point3f targetSomaPosition = CellTopologyHelper.convertSectionDisplacement(targetCellInstance, targetSec, (float) 0.5);

            MaxMinLength maxMin = project.morphNetworkConnectionsInfo.getMaxMinLength(netConnName);
            SearchPattern searchPattern = project.morphNetworkConnectionsInfo.getSearchPattern(netConnName);

            logger.logComment("\nThere are " + numberInGenStartCellGroup
                              + " cells in cell group: " + genStartCellGroup
                              + ", and each will have connections given by: (" + connConds + ")");

            logger.logComment("These will synapse with " + numberInGenFinishCellGroup
                              + " cells in cell group " + genFinishCellGroup);


            ArrayList<Integer> finCellsMaxedOut = new ArrayList<Integer>(numberInGenFinishCellGroup);

            long startGen = System.currentTimeMillis();

            if (numberInGenStartCellGroup==0)
            {
                logger.logError("There are no cells in group: "+genStartCellGroup
                               + " so abandoning generation of net conn: " + netConnName);
            }
            else if (numberInGenFinishCellGroup==0)
            {
                logger.logError("There are no cells in group: "+genFinishCellGroup
                               + " so abandoning generation of net conn: " + netConnName);
            }
            else
            {

    //////////////////////            Main loop over cells in start group

                for (int genStartCellNumber = 0; genStartCellNumber < numberInGenStartCellGroup; genStartCellNumber++)
                {
                    if(genStartCellNumber==0)
                    {
                        for(String synType: synTypeNames)
                        {
                            if (!CellTopologyHelper.isSynapseAllowed(genStartCellInstance, synType))
                            {
                                GuiUtils.showErrorMessage(logger, "Error getting synaptic location for: "+synType+" on cell of type " +
                                                      genStartCellInstance+", netConn: "+this.netConnName+".\n"+synLocWarning, null, null);

                                continueGeneration = false;
                            }
                            if (!CellTopologyHelper.isSynapseAllowed(genFinishCellInstance, synType))
                            {

                                GuiUtils.showErrorMessage(logger, "Error getting synaptic location for: "+synType+" on cell of type " +
                                                      genFinishCellInstance+", netConn: "+this.netConnName+".\n"+synLocWarning, null, null);

                                continueGeneration = false;
                            }
                        }
                    }


                    ArrayList<PositionRecord> finishPosRecords = project.generatedCellPositions.getPositionRecords(genFinishCellGroup);
                    Point3f startCellPos = project.generatedCellPositions.getOneCellPosition(genStartCellGroup, genStartCellNumber);

                    int[] allowedFinishCells
                        = CellTopologyHelper.getAllowedPostCellIds(genStartCellInstance,
                                                                   genFinishCellInstance,
                                                                   startCellPos,
                                                                   maxMin,
                                                                   genFinishCellGroup,
                                                                   finishPosRecords);


                    float numConnFloat = connConds.getNumConnsInitiatingCellGroup().getNextNumber();
                    int numberConnections = (int) numConnFloat;

                    // If false, ignore checks for maximum number of connections to finish cells, e.g. max connections to each target cell
                    boolean checkMaxingOutFinCells = (connConds.getMaxNumInitPerFinishCell() != Integer.MAX_VALUE);

                    if (numberInGenStartCellGroup>1000)
                    {
                        int part1percent = (int)(numberInGenStartCellGroup*0.01f);
                        int part5percent = (int)(numberInGenStartCellGroup*0.05f);
                        int part10percent = (int)(numberInGenStartCellGroup*0.1f);
                        int part20percent = (int)(numberInGenStartCellGroup*0.2f);
                        int part30percent = (int)(numberInGenStartCellGroup*0.3f);
                        int part40percent = (int)(numberInGenStartCellGroup*0.4f);
                        int part50percent = (int)(numberInGenStartCellGroup*0.5f);
                        int part60percent = (int)(numberInGenStartCellGroup*0.6f);
                        int part70percent = (int)(numberInGenStartCellGroup*0.7f);
                        int part80percent = (int)(numberInGenStartCellGroup*0.8f);
                        int part90percent = (int)(numberInGenStartCellGroup*0.9);

                        float time = ( System.currentTimeMillis() - startGen)/1000f;

                        if (genStartCellNumber == part1percent ||
                            genStartCellNumber == part5percent ||
                            genStartCellNumber == part10percent ||
                            genStartCellNumber == part20percent ||
                            genStartCellNumber == part30percent ||
                            genStartCellNumber == part40percent ||
                            genStartCellNumber == part50percent ||
                            genStartCellNumber == part60percent ||
                            genStartCellNumber == part70percent ||
                            genStartCellNumber == part80percent ||
                            genStartCellNumber == part90percent )
                        {
                            float fract = genStartCellNumber / (float)numberInGenStartCellGroup;

                            logger.logComment("Generated conns for "+ genStartCellNumber
                                              + " cells out of " + numberInGenStartCellGroup+" ("
                                              +(100 * fract)+"%) in "+ netConnName+" in "+time+" sec"+", "+time/60f+" min", true);

                            logger.logComment("Estimated time left: "+( (1-fract) * time / ( 60f * fract) )+" mins, est total time: "+( time / ( 60f * fract) )+" mins", true);

                            //////////GeneralUtils.printMemory(true);
                            //System.gc();
                            //GeneralUtils.printMemory(true);

                        }
                        else if(genStartCellNumber== numberInGenStartCellGroup-1)
                        {
                            logger.logComment("Generated conns for all cells in "+ netConnName+" in "+time+" sec"+", "+time/60f+" min", true);
                        }


                    }

                    if (numConnFloat != numberConnections)
                    {
                        if (ProjectManager.getRandomGenerator().nextFloat() < numConnFloat - numberConnections)
                            numberConnections = numberConnections + 1;
                    }

                    logger.logComment(".........   For cell number: " + genStartCellNumber + ", there will be " + numberConnections + " connections");


    //////////////////////            Loop over numberConnections cells in FINISH group

                    ArrayList<Integer> genFinCellsAlreadyConnected = new ArrayList<Integer>();
                    ArrayList<Integer> alreadyReciveConnectionFrom = new ArrayList<Integer>();

                    for (int connNumber = 0; connNumber < numberConnections; connNumber++)
                    {
                        if (!continueGeneration)
                        {
                            logger.logComment("Discontinuing generation...");
                            sendGenerationReport(true);
                            return;
                        }

                        boolean continueSingleConnGeneration = true;

                        logger.logComment("-----   Connection number " + connNumber
                                          + " for cell number: " + genStartCellNumber
                                          + ". Asking cell of type: " + genStartCellInstance.toString()
                                          + " for a synaptic location");

                        SegmentLocation genStartConnPoint = null;


                        if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                        {
                            genStartConnPoint
                                = cth.getPossiblePreSynapticTerminal(genStartCellInstance,
                                                                                    synTypeNames,
                                                                                    connConds.getPrePostAllowedLoc());

    //                            if (connConds.isOnlyConnectToUniqueCells())
    //                            {
    //                                genFinishCellsAlreadyConnected = project.generatedNetworkConnections.getTargetCellIndices(
    //                                    netConnName, genStartCellNumber, true);
    //                            }
                        }
                        else
                        {
                            genStartConnPoint
                                = cth.getPossiblePostSynapticTerminal(genStartCellInstance,
                                                                                     synTypeNames,
                                                                                     connConds.getPrePostAllowedLoc());

    //                            if (connConds.isOnlyConnectToUniqueCells())
    //                            {
    //                                genFinishCellsAlreadyConnected = project.generatedNetworkConnections.getSourceCellIndices(
    //                                    netConnName, genStartCellNumber, true);
    //                            }
                        }


                        if (genFinCellsAlreadyConnected.size() == numberInGenFinishCellGroup &&
                            connConds.isOnlyConnectToUniqueCells())
                        {
                            logger.logComment("There are the max number of connections at the opposite cell group, " +
                                              "and each one has to be unique...");
                            continueSingleConnGeneration = false;
                        }
                        else
                        {
                            logger.logComment("There are currently "
                                              + genFinCellsAlreadyConnected.size()
                                              + " unique connection cells in the opposite cell group: "+
                                              genFinCellsAlreadyConnected.toString());
                        }

                        if (connConds.isNoRecurrent()){
                            for (int i = 0; i < allowedFinishCells.length; i++)
                            {
                                if (project.generatedNetworkConnections.areConnected(netConnName, allowedFinishCells[i], genStartCellNumber))
                                {
                                    if(!alreadyReciveConnectionFrom.contains(allowedFinishCells[i]))
                                        alreadyReciveConnectionFrom.add(allowedFinishCells[i]);
                                }

                            }

                                logger.logComment("alreadyReciveConnectionFrom: "+ alreadyReciveConnectionFrom);


                            if (alreadyReciveConnectionFrom.size() == numberInGenFinishCellGroup)
                            {
                                logger.logComment("Every possible connection will be a recurrent connection, " +
                                                  "and these are not allowed...");
                                continueSingleConnGeneration = false;
                            }
                            else
                            {
                                logger.logComment("The cell already recive connections from "
                                                  + alreadyReciveConnectionFrom.size()
                                                  + " unique cells... ");
                            }
                        }





                        if (genStartConnPoint == null)
                        {
                            GuiUtils.showErrorMessage(logger, "Error getting synaptic location for: "+synPropList+" on cell of type " +
                                                      genStartCellInstance.toString()+", netConn: "+this.netConnName+".\n"+
                                                      synLocWarning, null, null);
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

                            float connDistance = -1;
                            //float maxMinCheckDistance = -1;
                            int genFinishCellNumber = -1;

                            boolean nonZeroPropDelay = !(project.morphNetworkConnectionsInfo.getAPSpeed(netConnName)==Float.MAX_VALUE);


            /****    COMPLETELY_RANDOM case...    ****/

                            if (searchPattern.type == SearchPattern.COMPLETELY_RANDOM)
                            {

                                boolean foundOne = false;

                                logger.logComment("Linking will be done in completely random manner...");
                                logger.logComment("Asking cell of type: " + genFinishCellInstance.toString() +
                                                  " for a synaptic location");

                                int numFailedAttemptsMaxMin = 0;
                                int numToTryMaxMin = maxMin.getNumberAttempts();


                                int availableCellsToConnectTo = numberInGenFinishCellGroup;

                                while (!foundOne
                                       && numFailedAttemptsMaxMin < numToTryMaxMin
                                       && continueGeneration
                                       && continueSingleConnGeneration
                                       && finCellsMaxedOut.size()<availableCellsToConnectTo)
                                {
                                    logger.logComment("***************************************");
                                    logger.logComment("   Restarting loop for random");
                                    logger.logComment("numFaliedAttemptsMaxMin: " + numFailedAttemptsMaxMin);
                                    logger.logComment("finCellsMaxedOut.size(): " + finCellsMaxedOut.size());


                                    if (connConds.isNoRecurrent())
                                    {

                                        int totalPossibleConnectTo = numberInGenFinishCellGroup;

                                        if (!connConds.isAllowAutapses())
                                            totalPossibleConnectTo = totalPossibleConnectTo -1;

                                        logger.logComment("totalPossibleConnectTo: " + totalPossibleConnectTo);
                                        logger.logComment("alreadyReciveConnectionFrom: " + alreadyReciveConnectionFrom);
                                        logger.logComment("genFinCellsAlreadyConnected: " + genFinCellsAlreadyConnected);

                                        if (alreadyReciveConnectionFrom.size() + genFinCellsAlreadyConnected.size() >=
                                                totalPossibleConnectTo)
                                        {
                                            logger.logComment("Total num of cells occupied by conns out or conns in...");
                                            continueSingleConnGeneration = false;
                                        }
                                    }

                                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                    {
                                        genFinishConnPoint = cth.getPossiblePostSynapticTerminal(
                                                                        genFinishCellInstance,
                                                                        synTypeNames,
                                                                        connConds.getPrePostAllowedLoc());
                                    }
                                    else
                                    {
                                        genFinishConnPoint = cth.getPossiblePreSynapticTerminal(
                                            genFinishCellInstance,
                                            synTypeNames,
                                            connConds.getPrePostAllowedLoc());
                                    }

                                    logger.logComment("genFinishConnPoint: " + genFinishConnPoint);

                                    if (genFinishConnPoint == null)
                                    {
                                        logger.logError("Error getting synaptic location on cell of type " +
                                                        genFinishCellInstance.toString()+", netConn: "+this.netConnName, null);
                                        continueGeneration = false;
                                    }
                                    else
                                    {
                                        boolean satisfiesMaxNumPerFinCell = false;

                                        boolean satisfiesUniqueness = false;

                                        boolean satisfiesGAPj = false;

                                        availableCellsToConnectTo = numberInGenFinishCellGroup;

                                        boolean ignoreDistance = (maxMin.getMinLength()==0) && (maxMin.getMaxLength()==Float.MAX_VALUE);

                                        while (!(satisfiesMaxNumPerFinCell && satisfiesUniqueness) && !(satisfiesGAPj)
                                               && finCellsMaxedOut.size()<availableCellsToConnectTo)
                                        {

                                            genFinishCellNumber = allowedFinishCells[ProjectManager.getRandomGenerator().nextInt(allowedFinishCells.length)];

                                            logger.logComment("--------------------------Testing if cell num: " + genFinishCellNumber +
                                                              " is appropriate for cell number: " + genStartCellNumber+", ignoreDistance: "+ignoreDistance);

                                            logger.logComment("finCellsMaxedOut: " + finCellsMaxedOut);
                                            logger.logComment("availableCellsToConnectTo: " + availableCellsToConnectTo);


                                            ArrayList<Integer> connsOnFinishCell = null;

                                            if (connConds.isNoRecurrent())
                                            {
                                                    logger.logComment("alreadyReciveConnectionFrom: "+ alreadyReciveConnectionFrom);

                                                if (!alreadyReciveConnectionFrom.contains(genFinishCellNumber))
                                                {
                                                    satisfiesGAPj = true;
                                                    logger.logComment("Reccurent connections are not allowed, satisfies condition: " + satisfiesGAPj);

                                                }
                                                else
                                                {
                                                    satisfiesGAPj = false;
                                                    logger.logComment("Reccurent connections are not allowed, satisfies condition: " + satisfiesGAPj + "the two cells are already coupled");
                                                }
                                            }
                                            else
                                            {
                                                satisfiesGAPj = true;
                                            }

                                            if(checkMaxingOutFinCells)
                                            {
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
                                            }

                                            logger.logComment("connsOnFinishCell: " + connsOnFinishCell);


                                            if ( !checkMaxingOutFinCells || ((connsOnFinishCell.size()+1) <= connConds.getMaxNumInitPerFinishCell()) )
                                            {
                                                logger.logComment("There are not more than: " + connConds.getMaxNumInitPerFinishCell()
                                                                  + " src conns on finish cell "+genFinishCellNumber);

                                                satisfiesMaxNumPerFinCell = true;

                                                if (connConds.isOnlyConnectToUniqueCells())
                                                {
                                                    logger.logComment("genFinCellsAlreadyConnected: " + genFinCellsAlreadyConnected);

                                                    if (!genFinCellsAlreadyConnected.contains(genFinishCellNumber))
                                                    {
                                                        satisfiesUniqueness = true;
                                                        logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                    }
                                                    else
                                                    {
                                                        satisfiesUniqueness = false;
                                                        logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                    }
                                                    availableCellsToConnectTo = numberInGenFinishCellGroup - genFinCellsAlreadyConnected.size();
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

                                        if (satisfiesMaxNumPerFinCell && satisfiesUniqueness && satisfiesGAPj)
                                        {

                                            if (sourceCellGroup.equals(targetCellGroup) &&
                                                !connConds.isAllowAutapses() &&
                                                genStartCellNumber == genFinishCellNumber)
                                            {
                                                logger.logComment("That would be an autapse, which isn't allowed!");
                                            }
                                            else
                                            {
                                                float distApart = -1;
                                                float distForMaxMin = -1;

                                                if(!ignoreDistance || nonZeroPropDelay)
                                                {
                                                    logger.logComment("Distancebeing calc, ignored dist: "+ ignoreDistance+", nonZeroPropDelay: "+nonZeroPropDelay);
                                                    distApart = CellTopologyHelper.getSynapticEndpointsDistance(
                                                                                            project,
                                                                                            genStartCellGroup,
                                                                                            new SynapticConnectionEndPoint(genStartConnPoint,
                                                                                                    genStartCellNumber),
                                                                                                    genFinishCellGroup,
                                                                                            new SynapticConnectionEndPoint(genFinishConnPoint,
                                                                                                    genFinishCellNumber),
                                                                                            maxMin.getDimension());
                                                    
                                                    if (!maxMin.getDimension().equals("s"))
                                                    {

                                                            distForMaxMin = distApart;
                                                    }
                                                    else
                                                    {
                                                        Point3f absoluteStartPoint = project.generatedCellPositions.getOneCellPosition(genStartCellGroup, genStartCellNumber);
                                                        Point3f absoluteEndPoint = project.generatedCellPositions.getOneCellPosition(genFinishCellGroup, genFinishCellNumber);

                                                        absoluteStartPoint.add(sourceSomaPosition); //add the displacement of the soma referred to the cell axes
                                                        absoluteEndPoint.add(targetSomaPosition);

                                                        distForMaxMin =  absoluteStartPoint.distance(absoluteEndPoint);
                                                     }
                                                }

                                                if (ignoreDistance || (distForMaxMin >= maxMin.getMinLength()
                                                    && distForMaxMin <= maxMin.getMaxLength()))
                                                {
                                                    logger.logComment("Distance condition satisfied, ignored dist: "+ ignoreDistance+", nonZeroPropDelay: "+nonZeroPropDelay);
                                                    foundOne = true;
                                                    connDistance = distApart;
                                                }
                                                else
                                                {
                                                    logger.logComment("The length: " + distForMaxMin + " isn't between " +
                                                                      maxMin.getMinLength() + " and " + maxMin.getMaxLength());
                                                    numFailedAttemptsMaxMin++;
                                                    genFinishConnPoint = null;
                                                    genFinishCellNumber = -1;
                                                }
                                            }
                                        }
    //                                        else { System.out.println("something wrong with conditions...");}
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
                                        tempGenFinishConnPoint = cth.getPossiblePostSynapticTerminal(
                                                                        genFinishCellInstance,
                                                                        synTypeNames,
                                                                        connConds.getPrePostAllowedLoc());
                                    }
                                    else
                                    {
                                        tempGenFinishConnPoint = cth.getPossiblePreSynapticTerminal(genFinishCellInstance,
                                                                           synTypeNames,
                                                                            connConds.getPrePostAllowedLoc());
                                    }

                                    logger.logComment("tempGenFinishConnPoint: " + tempGenFinishConnPoint);

                                    if (tempGenFinishConnPoint == null)
                                    {
                                        GuiUtils.showErrorMessage(logger,
                                                                  "Error getting synaptic location on cell of type " +
                                                                  genFinishCellInstance.toString()+", netConn: "+this.netConnName+"", null, null);
                                        return;
                                    }

                                    int tempGenFinishCellNumber = -1;

                                    boolean satisfiesMaxNumPerFinCell = false;

                                    boolean satisfiesUniqueness = false;

                                    boolean satisfiesGAPj = false;

                                    int numCellsNotConnTo = numberInGenFinishCellGroup;

                                    while (!(satisfiesMaxNumPerFinCell && satisfiesUniqueness) && !(satisfiesGAPj)
                                           && finCellsMaxedOut.size() < numCellsNotConnTo)
                                    {
                                        //tempGenFinishCellNumber = ProjectManager.getRandomGenerator().nextInt(numberInGenFinishCellGroup);

                                        tempGenFinishCellNumber = allowedFinishCells[ProjectManager.getRandomGenerator().nextInt(allowedFinishCells.length)];

                                        logger.logComment("----  Testing if cell num: " + tempGenFinishCellNumber +
                                                          " is appropriate for cell number: "
                                                          + genStartCellNumber);

                                        logger.logComment("finCellsMaxedOut: " + finCellsMaxedOut);
                                        logger.logComment("numCellsNotConnTo: " + numCellsNotConnTo);
                                        logger.logComment("genFinCellsAlreadyConnected: " + genFinCellsAlreadyConnected);

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

                                            if (connConds.isNoRecurrent())
                                            {
                                                if (!(project.generatedNetworkConnections.areConnected(netConnName, genStartCellNumber, tempGenFinishCellNumber))
                                                    && (!(project.generatedNetworkConnections.areConnected(netConnName, tempGenFinishCellNumber, genStartCellNumber)))) {
                                                    satisfiesGAPj = true;
                                                    logger.logComment("Reccurent connections are not allowed, satisfies condition: " + satisfiesGAPj);
                                                }
                                                else
                                                {
                                                    satisfiesGAPj = false;
                                                    logger.logComment("Reccurent connections are not allowed, satisfies condition: " + satisfiesGAPj + "the two cells are already connected");
                                                }
                                            }
                                            else
                                            {
                                                satisfiesGAPj = true;
                                            }

                                            if (connConds.isOnlyConnectToUniqueCells())
                                            {
                                                if (!genFinCellsAlreadyConnected.contains(new Integer(tempGenFinishCellNumber)))
                                                {
                                                    satisfiesUniqueness = true;
                                                        logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);

                                                }
                                                else
                                                {
                                                    logger.logComment("Needs uniqueness, satisfiesUniqueness: " + satisfiesUniqueness);
                                                    satisfiesUniqueness = false;
                                                }
                                                numCellsNotConnTo = numberInGenFinishCellGroup - genFinCellsAlreadyConnected.size();
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

                                    if(satisfiesMaxNumPerFinCell && satisfiesUniqueness && satisfiesGAPj)
                                    {
                                        if (sourceCellGroup.equals(targetCellGroup) &&
                                                !connConds.isAllowAutapses() &&
                                                genStartCellNumber == tempGenFinishCellNumber)
                                        {
                                            logger.logComment("That would be an autapse, which isn't allowed!");
                                        }
                                        else
                                        {

                                            logger.logComment("Not an autapse? "+sourceCellGroup.equals(targetCellGroup) +" "+!connConds.isAllowAutapses()+" "+
                                            (genStartCellNumber == tempGenFinishCellNumber));

                                            SynapticConnectionEndPoint tempGenFinishEndpoint =
                                                new SynapticConnectionEndPoint(tempGenFinishConnPoint, tempGenFinishCellNumber);

                                            // dimension dependent distance of the connection (r,x,y,z or soma distance)

                                            float distToThisPoint;

                                            if (!maxMin.getDimension().equals("s")) {

                                                distToThisPoint = CellTopologyHelper.getSynapticEndpointsDistance(
                                                    project,
                                                    genStartCellGroup,
                                                    new SynapticConnectionEndPoint(genStartConnPoint,
                                                    genStartCellNumber),
                                                    genFinishCellGroup,
                                                    tempGenFinishEndpoint,
                                                    maxMin.getDimension());

                                            } else {

                                                Point3f absoluteStartPoint = project.generatedCellPositions.getOneCellPosition(genStartCellGroup, genStartCellNumber);
                                                Point3f absoluteEndPoint = project.generatedCellPositions.getOneCellPosition(genFinishCellGroup, tempGenFinishCellNumber);

                                                absoluteStartPoint.add(sourceSomaPosition); //add the displacement of the soma referred to the cell axes
                                                absoluteEndPoint.add(targetSomaPosition);

                                                distToThisPoint = absoluteStartPoint.distance(absoluteEndPoint);
                                            }

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

                                                    connDistance = bestDistanceSoFar;
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
                                    }
                                    else
                                    {
                                        logger.logComment("satisfiesMaxNumPerFinCell: " + satisfiesMaxNumPerFinCell);
                                        logger.logComment("satisfiesUniqueness: " + satisfiesUniqueness);
                                        logger.logComment("satisfiesGAPj: " + satisfiesGAPj);

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
                                    genStartCellInstance,
                                    genStartConnPoint.getSegmentId(),
                                    genStartConnPoint.getFractAlong());

                                Point3f genStartCellPosition = project.generatedCellPositions.getOneCellPosition(
                                    genStartCellGroup,
                                    genStartCellNumber);

                                Point3f absGenStartSynPosition = new Point3f(genStartCellPosition);
                                absGenStartSynPosition.add(relativeSynPointGenStart);

                                float bestDistanceSoFar = Float.MAX_VALUE;

                                RectangularBox surroundingBox = CellTopologyHelper.getSurroundingBox(genFinishCellInstance, false, false);



                                for (int nextGenFinishCellIndex = 0; nextGenFinishCellIndex < allowedFinishCells.length; nextGenFinishCellIndex++)
                                {
                                    int nextGenFinishCellNum = allowedFinishCells[nextGenFinishCellIndex];

                                    logger.logComment("Checking cell number: " + nextGenFinishCellNum + " in cell group: " +
                                                      genFinishCellGroup);

                                    boolean alreadyConnected;
                                    boolean satisfiesGAPj = false;

                                   if (connConds.isOnlyConnectToUniqueCells() &&
                                        genFinCellsAlreadyConnected.contains(new Integer(nextGenFinishCellNum)))
                                    {
                                       alreadyConnected = true;
                                        logger.logComment(
                                            "Ignoring this cell, as it's already connected to the start cell, and uniqueness is specified");
                                    }

                                   else
                                   {
                                       alreadyConnected = false;

                                       if (connConds.isNoRecurrent())
                                        {
                                            if ((project.generatedNetworkConnections.areConnected(netConnName, genStartCellNumber, nextGenFinishCellNum))
                                                || ((project.generatedNetworkConnections.areConnected(netConnName, nextGenFinishCellNum, genStartCellNumber))))
                                            {
                                                satisfiesGAPj = false;
                                                logger.logComment("Needs GAPj, satisfiesGAPj: " + satisfiesGAPj + "the two cells are already coupled");

                                            }
                                            else
                                            {
                                                satisfiesGAPj = true;
                                                logger.logComment("Needs GAPj, satisfiesGAPj: " + satisfiesGAPj);
                                            }
                                        }
                                        else
                                        {
                                            satisfiesGAPj = true;
                                        }
                                  }

                                    if ((!alreadyConnected) && (satisfiesGAPj))
                                    {

                                        boolean satisfiesMaxNumPerFinCell = false;

                                        int numConnsOnFinishCell = 0;
                                        if (connConds.getGenerationDirection() ==
                                            ConnectivityConditions.SOURCE_TO_TARGET)
                                        {
                                            numConnsOnFinishCell = project.generatedNetworkConnections.
                                                getSourceCellIndices(netConnName,
                                                                     nextGenFinishCellNum, false).size();
                                        }
                                        else
                                        {
                                            numConnsOnFinishCell = project.generatedNetworkConnections.
                                                getTargetCellIndices(netConnName,
                                                                     nextGenFinishCellNum, false).size();

                                        }

                                        logger.logComment("numConnsOnFinishCell: " + numConnsOnFinishCell);

                                        if ( (numConnsOnFinishCell + 1) <= connConds.getMaxNumInitPerFinishCell())
                                        {
                                            logger.logComment("There are not more than: " +
                                                              connConds.getMaxNumInitPerFinishCell()
                                                              + " src conns on finish cell " + nextGenFinishCellNum);

                                                satisfiesMaxNumPerFinCell = true;



                                        }
                                        else
                                        {
                                            logger.logComment("There are already: " + numConnsOnFinishCell +
                                                              " src conns on tgt cell " + nextGenFinishCellNum);

                                            if (!finCellsMaxedOut.contains(nextGenFinishCellNum))
                                                finCellsMaxedOut.add(nextGenFinishCellNum);

                                            satisfiesMaxNumPerFinCell = false;
                                        }


                                        if(satisfiesMaxNumPerFinCell && satisfiesGAPj)
                                        {
                                            if (sourceCellGroup.equals(targetCellGroup) &&
                                                !connConds.isAllowAutapses() &&
                                                genStartCellNumber == nextGenFinishCellNum)
                                            {
                                                logger.logComment("That would be an autapse, which isn't allowed!");
                                            }
                                            else
                                            {
                                                Point3f absoluteGenFinishCellPosition
                                                    = project.generatedCellPositions.getOneCellPosition(
                                                        genFinishCellGroup,
                                                        nextGenFinishCellNum);

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
                                                                genFinishCellInstance,
                                                                synTypeNames,
                                                                posnStartSynapseRelToFinishCell,
                                                                connConds.getPrePostAllowedLoc());
                                                    }
                                                    else
                                                    {
                                                        bestPointOnGenFinishCell
                                                            = CellTopologyHelper.getClosestPreSynapticTerminalLocation(
                                                                genFinishCellInstance,
                                                                synTypeNames,
                                                                posnStartSynapseRelToFinishCell,
                                                                connConds.getPrePostAllowedLoc());

                                                    }

                                                    if (bestPointOnGenFinishCell == null)
                                                    {
                                                        GuiUtils.showErrorMessage(logger,
                                                                                  "Error getting synaptic location on cell of type " +
                                                                                  genFinishCellInstance.toString()+", netConn: "+this.netConnName+"", null, null);
                                                        return;
                                                    }

                                                    SynapticConnectionEndPoint tempGenFinishEndpoint =
                                                        new SynapticConnectionEndPoint(bestPointOnGenFinishCell, nextGenFinishCellNum);

                                                       float distToThisPoint = CellTopologyHelper.getSynapticEndpointsDistance(
                                                                project,
                                                                genStartCellGroup,
                                                                new SynapticConnectionEndPoint(genStartConnPoint,
                                                                genStartCellNumber),
                                                                genFinishCellGroup,
                                                                tempGenFinishEndpoint,
                                                                maxMin.getDimension());

                                                    logger.logComment("Distance to that point: " + distToThisPoint);

                                                    if (distToThisPoint < bestDistanceSoFar)
                                                    {
                                                        genFinishConnPoint = bestPointOnGenFinishCell;
                                                        genFinishCellNumber = nextGenFinishCellNum;
                                                        logger.logComment("Best so far...");

                                                        bestDistanceSoFar = distToThisPoint;


                                                        connDistance = bestDistanceSoFar;
                                                    }
                                                    else logger.logComment("Close but no cigar...");
                                                }
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
                                                  genFinishCellNumber + ": " +genFinishConnPoint.toString());

                                ArrayList<ConnSpecificProps> props = new ArrayList<ConnSpecificProps>();


                                for (SynapticProperties synProp : synPropList) {

                                    if (!synProp.getDelayGenerator().isTypeFixedNum() || !synProp.getWeightsGenerator().isTypeFixedNum())
                                    {
                                        ConnSpecificProps csp = new ConnSpecificProps(synProp.getSynapseType());

                                        csp.internalDelay = synProp.getDelayGenerator().getNextNumber();

                                        if (synProp.getWeightsGenerator().isTypeFunction()) {

                                            if (!synProp.getWeightsGenerator().isSomaToSoma()) {


                                                float distRadial = CellTopologyHelper.getSynapticEndpointsDistance(
                                                            project,
                                                            genStartCellGroup,
                                                            new SynapticConnectionEndPoint(genStartConnPoint,
                                                            genStartCellNumber),
                                                            genFinishCellGroup,
                                                            new SynapticConnectionEndPoint(genFinishConnPoint,
                                                            genFinishCellNumber),
                                                            MaxMinLength.RADIAL);

                                                csp.weight = synProp.getWeightsGenerator().getNextNumber(distRadial);
                                                //System.out.println("csp.weight: " + csp.weight + ", dist " + distRadial);
                                                //System.out.println(synProp.getWeightsGenerator());

                                            }
                                            else
                                            {

                                                Point3f absoluteStartPoint = project.generatedCellPositions.getOneCellPosition(genStartCellGroup, genStartCellNumber);
                                                Point3f absoluteEndPoint = project.generatedCellPositions.getOneCellPosition(genFinishCellGroup, genFinishCellNumber);

                                                absoluteStartPoint.add(sourceSomaPosition); //add the displacement of the soma referred to the cell axes
                                                absoluteEndPoint.add(targetSomaPosition);

                                                float somaConnDistance =  absoluteStartPoint.distance(absoluteEndPoint);

                                                csp.weight = synProp.getWeightsGenerator().getNextNumber(somaConnDistance);
                                                //System.out.println(synProp.getWeightsGenerator());

                                            }

                                        }
                                        else
                                        {
                                            csp.weight = synProp.getWeightsGenerator().getNextNumber();
                                        }

                                        props.add(csp);
                                    }
                                }
                                if (props.size()==0) props = null;


                                Float propDelay = connDistance/project.morphNetworkConnectionsInfo.getAPSpeed(netConnName) ;

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

                                genFinCellsAlreadyConnected.add(genFinishCellNumber);
                            }
                            else
                            {
                                logger.logComment("Not adding the generated position...");
                            }

                        } // if continue..
                        else
                        {
                            logger.logError("Error getting synaptic location on cell of type " +
                                            genStartCellInstance.toString(), null);
                        }
                    }
                    logger.logComment("Finished creating the " + numberConnections + " conns for cell number: " +
                                      genStartCellNumber);
                }
            }
            logger.logComment("Finished looking at all " + numberInGenStartCellGroup + " cells in group: " +
                              genStartCellGroup);

            if (myReportInterface != null) myReportInterface.majorStepComplete();

            netConnCompleted(netConnName);

        }
    }



    private void sendGenerationReport(boolean interrupted)
    {
        StringBuffer generationReport = new StringBuffer();

        long netConnsGenerationTime = System.currentTimeMillis();
        float seconds = (float) (netConnsGenerationTime - startGenerationTime) / 1000f;

        int totNum = project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION);
        int totNNum = project.generatedNetworkConnections.getNumNonEmptyNetConns();

        generationReport.append("<center><b>Morphology Based Network Connections:</b></center>");

        if (interrupted)
            generationReport.append("<center><b>NOTE: Generation interrupted</b></center><br>");


        if (totNum==0)
        {
            generationReport.append("No connections generated.<br><br>");
        }
        else
        {
            generationReport.append("Time taken to generate <b>"
                                    + totNum
                                    + "</b> connections (in <b>"+totNNum+"</b> net conns): " + seconds + " seconds.<br>");

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


    public static void main(String[] args) throws ProjectFileParsingException, InterruptedException, NoProjectLoadedException
    {

        Project project = Project.loadProject(new File("../nC_projects/Speed/Speed.ncx"),null);

        logger.logComment("Loading proj: "+ project);

        ProjectManager pm = new ProjectManager(null, null);

        pm.setCurrentProject(project);

        //            pm.doLoadNetworkMLAndGenerate(f);

        System.out.println("Loaded: "+ project.getProjectFullFileName());

        ((RandomCellPackingAdapter)project.cellGroupsInfo.getCellPackingAdapter(project.cellGroupsInfo.getCellGroupNameAt(0))).setMaxNumberCells(160000);


        pm.doGenerate(SimConfigInfo.DEFAULT_SIM_CONFIG_NAME, 123);

        while (pm.isGenerating())
        {
            Thread.sleep(5000);
            System.out.println("Waiting to generate...");
        }


        System.out.println("Total connections: "+ project.generatedNetworkConnections.getNumAllSynConns());

        GeneralUtils.printMemory(true);
        System.gc();
        GeneralUtils.printMemory(true);

    }

}
