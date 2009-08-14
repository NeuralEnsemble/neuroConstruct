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
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import javax.vecmath.*;
import ucl.physiol.neuroconstruct.project.packing.RandomCellPackingAdapter;
import ucl.physiol.neuroconstruct.utils.equation.*;

/**
 * Thread to handle generation of volume based connections/axonal arbourisations
 *
 * @author Padraig Gleeson
 *  
 */


public class VolumeBasedConnGenerator extends Thread
{
    public static Variable[] allowedVars
        = new Variable[]{new Variable("x"),
                         new Variable("y"),
                         new Variable("z"),
                         new Variable("r")};

    public VolumeBasedConnGenerator()
    {

    }

    ClassLogger logger = new ClassLogger("VolumeBasedConnGenerator");

    public final static String myGeneratorType = "VolumeBasedConnGenerator";

    Project project = null;
    long startGenerationTime;
    boolean continueGeneration = true;

    GenerationReport myReportInterface = null;

    private SimConfig simConfig = null;



    public VolumeBasedConnGenerator(Project project, GenerationReport reportInterface)
    {
        super(myGeneratorType);

        logger.logComment("New VolumeBasedConnGenerator created");
        this.project = project;

        myReportInterface = reportInterface;

    }

    public void setSimConfig(SimConfig simConfig)
    {
        this.simConfig = simConfig;
    }


    public void stopGeneration()
    {
        logger.logComment("VolumeBasedConnGenerator being told to stop...");
        continueGeneration = false;
    }


    public ArrayList<String> getRelevantNetConns()
    {
        Vector allVolConns = project.volBasedConnsInfo.getAllAAConnNames();

        ArrayList<String> allNetConnsInSimConfig = simConfig.getNetConns();

        ArrayList<String> volNetConnsInSimConfig = new ArrayList<String> ();

        for (int i = 0; i < allNetConnsInSimConfig.size(); i++)
        {
            if (allVolConns.contains(allNetConnsInSimConfig.get(i)))
                volNetConnsInSimConfig.add(allNetConnsInSimConfig.get(i));
        }

        return volNetConnsInSimConfig;

    }



    @Override
    public void run()
    {
        logger.logComment("Running VolumeBasedConnGenerator thread...");

        startGenerationTime = System.currentTimeMillis();

        ArrayList<String> volNetConnsInSimConfig = getRelevantNetConns();
        
        CellTopologyHelper cth = new CellTopologyHelper(); // so that synapse locations can be cached...

        for (int j = 0; j < volNetConnsInSimConfig.size(); j++)
        {
            if (!continueGeneration)
            {
                logger.logComment("Discontinuing generation...");
                sendGenerationReport(true);
                return;
            }

            String volConnName =  volNetConnsInSimConfig.get(j);
            logger.logComment("\n");
            logger.logComment("------------     Looking at Connection: " + volConnName);

            this.myReportInterface.giveUpdate("Generating Net Conn: " + volConnName+"...");

            ConnectivityConditions connConds = project.volBasedConnsInfo.getConnectivityConditions(volConnName);

            Vector<SynapticProperties> synapticPropList = project.volBasedConnsInfo.getSynapseList(volConnName);

            String exp = project.volBasedConnsInfo.getInhomogenousExp(volConnName);

            EquationUnit inhomoExp = null;

            try
            {
                inhomoExp = Expression.parseExpression(exp, VolumeBasedConnGenerator.allowedVars);

            }
            catch (EquationException ex1)
            {
                GuiUtils.showErrorMessage(logger, "Error evaluating expression: "+exp, ex1, null);
                return;
            }

            String[] synPropList = new String[synapticPropList.size()];
            for (int i = 0; i < synapticPropList.size(); i++)
            {
                synPropList[i] = synapticPropList.elementAt(i).getSynapseType();
            }


            String sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(volConnName);
            String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);

            Cell sourceCellInstance = project.cellManager.getCell(sourceCellType);

            int numberInSourceCellGroup
                = project.generatedCellPositions.getNumberInCellGroup(sourceCellGroup);


            String targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(volConnName);
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


            // Note we create the name "GenerationStartCellGroup", etc for the cell group we generate from
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
            
          
            Vector<AxonalConnRegion> allVolRegions = generationStartCellInstance.getAxonalArbours();

            Vector<String> regionsInConn = project.volBasedConnsInfo.getSourceConnRegions(volConnName);

            Vector<AxonalConnRegion> aaRegions = new Vector<AxonalConnRegion>();

            for(AxonalConnRegion aa: allVolRegions)
            {
                if (regionsInConn.contains(aa.getName()))
                {
                    aaRegions.add(aa);
                }
            }


            for (int genStartCellNumber = 0; genStartCellNumber < numberInGenStartCellGroup; genStartCellNumber++)
            {
                float numConnFloat = connConds.getNumConnsInitiatingCellGroup().getNextNumber();
                int numberConnections = (int)numConnFloat;

                if (numConnFloat!=numberConnections)
                {
                    if (ProjectManager.getRandomGenerator().nextFloat()<numConnFloat-numberConnections)
                        numberConnections = numberConnections +1;
                }

                logger.logComment("For cell number: " + genStartCellNumber + ", there will be " + numberConnections +
                                  " connections");

                ArrayList<Integer> finishCellsMaxedOut = new ArrayList<Integer>(numberInGenFinishCellGroup);

                for (int p = 0; p < numberConnections; p++)
                {
                    if (!continueGeneration)
                    {
                        logger.logComment("Discontinuing generation...");
                        sendGenerationReport(true);
                        return;
                    }

                    boolean continueSingleConnGeneration = true;

                    logger.logComment("-----   Connection number " + p + " for cell number: " + genStartCellNumber);

                    SegmentLocation genStartConnPoint = null;

                    ArrayList<Integer> genFinCellsConnToThis = null;
                    
                    PrePostAllowedLocs startLoc = new PrePostAllowedLocs();
                    startLoc.axonsAllowedPre=false;
                    startLoc.dendritesAllowedPre=false;
                    startLoc.somaAllowedPre=true;

                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                    {
                        genStartConnPoint
                            = cth.getPossiblePreSynapticTerminal(generationStartCellInstance,
                                                                 synPropList,
                                                                 startLoc);

                        genFinCellsConnToThis = project.generatedNetworkConnections.getTargetCellIndices(volConnName,
                            genStartCellNumber, true);
                    }
                    else
                    {
                        genStartConnPoint
                            = cth.getPossiblePostSynapticTerminal(generationStartCellInstance,
                                                                  synPropList,
                                                                  connConds.getPrePostAllowedLoc());

                        genFinCellsConnToThis = project.generatedNetworkConnections.getSourceCellIndices(volConnName,
                            genStartCellNumber, true);
                    }

                    logger.logComment("genStartConnPoint: " + genStartConnPoint);
                    logger.logComment("genFinCellsConnToThis: " + genFinCellsConnToThis);

                    if (genFinCellsConnToThis.size() == numberInGenFinishCellGroup &&
                        connConds.isOnlyConnectToUniqueCells())
                    {
                        logger.logComment("There are the max number of connections at the opposite cell group, " +
                                          "and each one has to be unique...");
                        continueSingleConnGeneration = false;
                    }
                    else
                    {
                        logger.logComment("There are currently "
                                          + genFinCellsConnToThis.size()
                                          + " unique connection cells in the opposite cell group: ");
                        logger.logComment(genFinCellsConnToThis.toString());
                    }

                    if (genStartConnPoint == null)
                    {
                        GuiUtils.showErrorMessage(logger, "Error getting synaptic location on cell of type " +
                                                  generationStartCellInstance.toString(), null, null);
                        continueGeneration = false;
                    }

                    if (continueSingleConnGeneration)
                    {
                        SegmentLocation genFinishConnPoint = null;

                        float connectionDistance = -1;

                        int genFinishCellNumber = -1;

                        logger.logComment("Linking will be done in completely random manner...");
                        logger.logComment("Asking cell of type: " + generationFinishCellInstance.toString() +
                                          " for a synaptic location");

                        int numFailedAttempts = 0;
                        boolean foundOne = false;

                        /** @todo Make option in conn dialog */
                        int numToTry = 300;

                        while (!foundOne
                               && numFailedAttempts < numToTry
                               && continueGeneration
                               && finishCellsMaxedOut.size()<numberInGenFinishCellGroup)
                        {
                            logger.logComment("....   numFaliedAttempts: " + numFailedAttempts);

                            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                            {
                                genFinishConnPoint = cth.getPossiblePostSynapticTerminal(
                                    generationFinishCellInstance, synPropList,
                                                                                     connConds.getPrePostAllowedLoc());
                            }
                            else
                            {
                                genFinishConnPoint = cth.getPossiblePreSynapticTerminal(
                                    generationFinishCellInstance, synPropList,
                                                                                     connConds.getPrePostAllowedLoc());

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
                                boolean satisfiesMaxPerFinish = false;

                                boolean satisfiesUniqueness = false;

                                while (!(satisfiesUniqueness  && satisfiesMaxPerFinish)
                                       && finishCellsMaxedOut.size()<numberInGenFinishCellGroup)
                                {
                                    logger.logComment("Trying to get a finishing cell...");

                                    genFinishCellNumber = ProjectManager.getRandomGenerator().nextInt(numberInGenFinishCellGroup);

                                    logger.logComment("genFinishCellNumber: " + genFinishCellNumber);

                                    //if (!numFinishCellsTried.contains(genFinishCellNumber))
                                    //    numFinishCellsTried.add(genFinishCellNumber);

                                    int numConnsOnFinishCell = 0;
                                    if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                                    {
                                        numConnsOnFinishCell = project.generatedNetworkConnections.getSourceCellIndices(volConnName,
                                            genFinishCellNumber, false).size();
                                    }
                                    else
                                    {
                                        numConnsOnFinishCell = project.generatedNetworkConnections.getTargetCellIndices(volConnName,
                                            genFinishCellNumber, false).size();

                                    }

                                    logger.logComment("numConnsOnFinishCell: " + numConnsOnFinishCell);


                                    if ((numConnsOnFinishCell+1)<=connConds.getMaxNumInitPerFinishCell())
                                    {
                                        logger.logComment("There are not more than: " + connConds.getMaxNumInitPerFinishCell()
                                                          + " src conns on finish cell " + genFinishCellNumber);

                                        satisfiesMaxPerFinish = true;
                                    }
                                    else
                                    {
                                        logger.logComment("There are already: " + numConnsOnFinishCell
                                                          + " src conns on tgt cell "+genFinishCellNumber);

                                        satisfiesMaxPerFinish = false;

                                        if (!finishCellsMaxedOut.contains(genFinishCellNumber))
                                            finishCellsMaxedOut.add(genFinishCellNumber);

                                    }

                                    if (connConds.isOnlyConnectToUniqueCells())
                                    {
                                        if (!genFinCellsConnToThis.contains(new Integer(genFinishCellNumber)))
                                        {
                                            logger.logComment("Conns need to be to unique cells, and this holds");
                                            satisfiesUniqueness = true;
                                        }
                                        else
                                        {
                                            logger.logComment("Conns need to be to unique cells, and this doesn't hold!");
                                            satisfiesUniqueness = false;
                                        }
                                    }
                                    else
                                    {
                                        logger.logComment("Conns don't need to be to unique cells...");
                                        satisfiesUniqueness = true;
                                    }

                                }

                                if (satisfiesMaxPerFinish)
                                {
                                    logger.logComment("Working out posns and distances...");

                                    Point3f cellCoordsSynPointGenStart
                                        = CellTopologyHelper.convertSegmentDisplacement(
                                            generationStartCellInstance,
                                            genStartConnPoint.getSegmentId(),
                                            genStartConnPoint.getFractAlong());

                                    Point3f genStartCellPosition
                                        = project.generatedCellPositions.getOneCellPosition(
                                            generationStartCellGroup,
                                            genStartCellNumber);

                                    Point3f absGenStartSynPosition = new Point3f(genStartCellPosition);

                                    absGenStartSynPosition.add(cellCoordsSynPointGenStart);

                                    Point3f absoluteGenFinishCellPosition
                                        = project.generatedCellPositions.getOneCellPosition(
                                            generationFinishCellGroup,
                                            genFinishCellNumber);

                                    Point3f cellCoordsSynPointGenFinish
                                        = CellTopologyHelper.convertSegmentDisplacement(
                                            generationFinishCellInstance,
                                            genFinishConnPoint.getSegmentId(),
                                            genFinishConnPoint.getFractAlong());

                                    Point3f absGenFinishSynPosition = new Point3f(absoluteGenFinishCellPosition);
                                    absGenFinishSynPosition.add(cellCoordsSynPointGenFinish);

                                    logger.logComment("absGenFinishSynPosition: " + absGenFinishSynPosition);

                                    connectionDistance = absGenFinishSynPosition.distance(absGenStartSynPosition);

                                    Vector<Region> translatedRegions = new Vector<Region> ();

                                    for (AxonalConnRegion aa : aaRegions)
                                    {
                                        translatedRegions.add(aa.getRegion().getTranslatedRegion(new Vector3f(
                                            absGenStartSynPosition)));
                                    }

                                    foundOne = false;

                                    for (Region reg : translatedRegions)
                                    {
                                        logger.logComment("Looking in region: " + reg);

                                        if (reg.isPointInRegion(absGenFinishSynPosition))
                                        {
                                            logger.logComment("Found point in region: " + reg);
                                            foundOne = true;
                                        }
                                    }
                                    
                                    //TODO, optimise by doing this check earlier!!
                                    if (sourceCellGroup.equals(targetCellGroup) && 
                                        !connConds.isAllowAutapses() &&
                                        genStartCellNumber == genFinishCellNumber)
                                    {
                                         logger.logComment("That would be an autapse, which isn't allowed!");
                                         foundOne = false;
                                    }

                                    if (foundOne)
                                    {
                                        logger.logComment("Going to apply probability of connectivity...");

                                        try
                                        {

                                            if (inhomoExp.toString().equals("1"))
                                            {
                                                logger.logComment("Uniform prob of conn...");
                                            }
                                            else
                                            {
                                                logger.logComment("Nonuniform prob of conn given by: " + inhomoExp);

                                                Point3f synPosnRelToStartCell = new Point3f(absoluteGenFinishCellPosition);
                                                synPosnRelToStartCell.sub(genStartCellPosition);

                                                logger.logComment("synPosnRelToStartCell: "+synPosnRelToStartCell);

                                                float x = synPosnRelToStartCell.x;
                                                float y = synPosnRelToStartCell.y;
                                                float z = synPosnRelToStartCell.z;

                                                float r = (float)Math.sqrt((x*x)+(y*y)+(z*z));

                                                Argument[] args = new Argument[]
                                                    {new Argument("x", x),
                                                    new Argument("y", y),
                                                    new Argument("z", z),
                                                    new Argument("r", r)};

                                                double prob = inhomoExp.evaluateAt(args);
                                                logger.logComment("Probability of conn: " + prob);
                                                if (prob <= 0)
                                                {
                                                    logger.logComment("Zero or negative prob, so rejecting...");
                                                    foundOne = false;
                                                }
                                                else if (prob >= 1)
                                                {
                                                    logger.logComment("Prob >= 1, so accepting...");
                                                    foundOne = true;
                                                }
                                                else
                                                {
                                                    float coin = ProjectManager.getRandomGenerator().nextFloat();
                                                    if (coin > prob)
                                                    {
                                                        logger.logComment("Coin said " + coin + " so rejecting");
                                                        foundOne = false;
                                                    }
                                                    else
                                                    {
                                                        logger.logComment("Coin said " + coin + " so accepting...");
                                                        foundOne = true;
                                                    }
                                                }

                                                logger.logComment("Equation evaluated as: " + inhomoExp.getNiceString());
                                            }
                                        }
                                        catch (EquationException ex1)
                                        {
                                            GuiUtils.showErrorMessage(logger, "Error evaluating expression: "+exp, ex1, null);

                                        }
                                    }


                                    if (!foundOne)
                                    {
                                        numFailedAttempts++;
                                        genFinishConnPoint = null;
                                        genFinishCellNumber = -1;
                                    }
                                }
                            }
                        }


                        if (continueSingleConnGeneration
                            && genFinishCellNumber >= 0
                            && genFinishConnPoint != null
                            && finishCellsMaxedOut.size()<numberInGenFinishCellGroup)
                        {
                            logger.logComment("Generated a synaptic point for cell number " +
                                              genFinishCellNumber + ": ");

                            logger.logComment(genFinishConnPoint.toString());

                            ArrayList<ConnSpecificProps> props = new ArrayList<ConnSpecificProps> ();
                            
                            float connDistance = CellTopologyHelper.getSynapticEndpointsDistance(
                                            project,
                                            generationStartCellGroup,
                                            new SynapticConnectionEndPoint(genStartConnPoint,
                                            genStartCellNumber),
                                            generationFinishCellGroup,
                                            new SynapticConnectionEndPoint(genFinishConnPoint,
                                            genFinishCellNumber),
                                            MaxMinLength.RADIAL);
                            
                            
                            Section sourceSec = sourceCellInstance.getFirstSomaSegment().getSection();
                            Section targetSec = targetCellInstance.getFirstSomaSegment().getSection();

                            Point3f sourceSomaPosition = CellTopologyHelper.convertSectionDisplacement(sourceCellInstance, sourceSec, (float) 0.5);

                            Point3f targetSomaPosition = CellTopologyHelper.convertSectionDisplacement(targetCellInstance, targetSec, (float) 0.5);  
                            
                            for (SynapticProperties synProp : synapticPropList)
                            {
                                if (!synProp.getDelayGenerator().isTypeFixedNum()
                                    || !synProp.getWeightsGenerator().isTypeFixedNum())
                                {
                                    ConnSpecificProps csp = new ConnSpecificProps(synProp.getSynapseType());

                                    csp.internalDelay = synProp.getDelayGenerator().getNextNumber();
                                    //csp.weight = synProp.getWeightsGenerator().getNextNumber();
                                    if (synProp.getWeightsGenerator().isTypeFunction()) 
                                    {

                                        if (!synProp.getWeightsGenerator().isSomaToSoma()) {

                                            csp.weight = synProp.getWeightsGenerator().getNextNumber(connDistance);
                                            //System.out.println(synProp.getWeightsGenerator());

                                        } else {

                                            Point3f absoluteStartPoint = project.generatedCellPositions.getOneCellPosition(generationStartCellGroup, genStartCellNumber);
                                            Point3f absoluteEndPoint = project.generatedCellPositions.getOneCellPosition(generationFinishCellGroup, genFinishCellNumber);

                                            absoluteStartPoint.add(sourceSomaPosition); //add the displacement of the soma referred to the cell axes
                                            absoluteEndPoint.add(targetSomaPosition);

                                            float somaConnDistance =  absoluteStartPoint.distance(absoluteEndPoint);

                                            csp.weight = synProp.getWeightsGenerator().getNextNumber(somaConnDistance);
                                            //System.out.println("csp.weight: " + csp.weight + ", dist " + somaConnDistance);
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
                            
                            if (props.size() == 0) props = null;


                            Float propDelay = connectionDistance/project.volBasedConnsInfo.getAPSpeed(volConnName) ;

                            if (project.volBasedConnsInfo.getAPSpeed(volConnName)==Float.MAX_VALUE)
                                propDelay = 0f;

                            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                            {
                                project.generatedNetworkConnections.addSynapticConnection(volConnName,
                                                                                          GeneratedNetworkConnections.VOL_NETWORK_CONNECTION,
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
                                project.generatedNetworkConnections.addSynapticConnection(volConnName,
                                                                                          GeneratedNetworkConnections.VOL_NETWORK_CONNECTION,
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
            logger.logComment("Finished looking at all " + numberInGenStartCellGroup + " cells in group: " +
                              generationStartCellGroup);

            if (myReportInterface!=null) myReportInterface.majorStepComplete();

        } // for...

        // Finished the main generation part...

        sendGenerationReport(false);
    }


    private void sendGenerationReport(boolean interrupted)
    {
        StringBuffer generationReport = new StringBuffer();


        long netConnsGenerationTime = System.currentTimeMillis();
        float seconds = (float) (netConnsGenerationTime - startGenerationTime) / 1000f;

        int totNum = project.generatedNetworkConnections.getNumberSynapticConnections(GeneratedNetworkConnections.VOL_NETWORK_CONNECTION);


        generationReport.append("<center><b>Volume Based Connections:</b></center>");


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
                                    + " connections: " + seconds +
                                    " seconds.<br>");

            generationReport.append(
                project.generatedNetworkConnections.getHtmlReport(GeneratedNetworkConnections.VOL_NETWORK_CONNECTION, simConfig));

        }



        if (myReportInterface!=null)
        {
            myReportInterface.giveGenerationReport(generationReport.toString(),
                                                   myGeneratorType,
                                                   simConfig);
        }
    }

    
    public static void main(String[] args)
    {
        try
        {
            File projFile = new File("testProjects/TestNetworkConns/TestNetworkConns.neuro.xml");

            ProjectManager pm = new ProjectManager();

            Project proj = pm.loadProject(projFile);

            String simConf = "VolBasedTest";
            SimConfig sc = proj.simConfigInfo.getSimConfig(simConf);


            String nc1 = proj.volBasedConnsInfo.getConnNameAt(0);
            String nc2 = proj.volBasedConnsInfo.getConnNameAt(1);

            String src = proj.volBasedConnsInfo.getSourceCellGroup(nc1);
            String tgt = proj.volBasedConnsInfo.getTargetCellGroup(nc1);


            int numPreCells = 200;
            int numPostCells = 200;

            int numPreConns1 = 10;
            int maxPostConns1 = 5;

            int numPreConns2 = 10;
            int maxPostConns2 = 5;


            ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(src)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPreCells);
            ((RandomCellPackingAdapter)proj.cellGroupsInfo.getCellPackingAdapter(tgt)).setParameter(RandomCellPackingAdapter.CELL_NUMBER_POLICY, numPostCells);

            GeneralUtils.timeCheck("Before generation", true);

            pm.doGenerate(sc.getName(), 1234);

            while (pm.isGenerating())
            {
                Thread.sleep(100);
            }

            GeneralUtils.timeCheck("After generation", true);

            System.out.println("Number of cells generated: "+ proj.generatedCellPositions.getNumberInAllCellGroups());
            for (String nc: proj.generatedNetworkConnections.getNamesNonEmptyNetConns())
            {
                System.out.println("with "+ proj.generatedNetworkConnections.getSynapticConnections(nc).size()+" conns in "+ nc);
            }

            





        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
