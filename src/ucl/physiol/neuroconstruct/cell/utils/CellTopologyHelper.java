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

package ucl.physiol.neuroconstruct.cell.utils;

import java.io.*;
import java.util.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.compartment.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;

/**
 * Helper class for dealing with cell topology (and other aspects, e.g. biophysiology).
 * Convenient as it helps keep Cell class simpler
 *
 * @author Padraig Gleeson
 *  
 *
 */

public class CellTopologyHelper
{
    static ClassLogger logger = new ClassLogger("CellTopologyHelper");

    //static Random random = new Random();

    public static final String CELL_IS_MORPH_VALID = "Cell morphology is valid.";

    public static final String CELL_IS_BIO_VALID = "Cell biophysical parameters are valid.";

    public static final String CELLS_ARE_IDENTICAL = "** Cells are identical **";

    CachedSynLocInfo cachedPostSynLocInfo = null;
    CachedSynLocInfo cachedPreSynLocInfo = null;

    static
    {
        // lots of output here, so let's keep it quiet in general
        /////////logger.setThisClassSilent(true);
    }

    /**
     * Search through the segments on a cell looking for the ones which
     * support a specific synapse type...
     *
     * @param cell The cell in question
     * @param synapseType String with synapse type name
     * @return The location of the synapse or null if not supported
     */
    public PostSynapticTerminalLocation getPossiblePostSynapticTerminal(Cell cell, 
                                                                               String[] synapseType,
                                                                               PrePostAllowedLocs pp)
    {
        float totalLengthOfValidSegments = 0;
        Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();
        Vector<Float> lensOfPossibleSegments = new Vector<Float>();
        
        PostSynapticTerminalLocation postSynTerm = null;
                       
        if (!(cachedPostSynLocInfo!=null && cachedPostSynLocInfo.isApplicable(cell.getInstanceName(), synapseType))) 
        {
            cachedPostSynLocInfo=null;
            Vector<Segment> allSegments = cell.getAllSegments();

            Vector<String> groupsWithSynapse = cell.getGroupsWithSynapse(synapseType[0]); // get first lot

            if (synapseType.length>1)
            {
                for (int remainingSynIndex = 0; remainingSynIndex < synapseType.length; remainingSynIndex++)
                {
                    Vector remainingSynGroups = cell.getGroupsWithSynapse(synapseType[remainingSynIndex]);

                    for (int includedGroups = 0; includedGroups < groupsWithSynapse.size(); includedGroups++)
                    {
                            String includedGroup = groupsWithSynapse.elementAt(includedGroups);
                            if (!remainingSynGroups.contains(includedGroup))
                                groupsWithSynapse.remove(includedGroup);
                    }
                }
            }


            logger.logComment(cell.getInstanceName() + " being asked for pre-synaptic point of type " + synapseType +
                              " among my segments");

            logger.logComment("groupsWithSynapse: "+ groupsWithSynapse);


            logger.logComment(cell.getInstanceName() + " being asked for post-synaptic point of type " + synapseType +
                              " among my segments");

            if (allSegments.isEmpty())
            {
                return null;
            }

            // Quick return if just a 1 seg cell 
            if (allSegments.size()==1 && 
                allSegments.get(0).isSomaSegment() &&
                pp.isSomaAllowedPost() &&
                (groupsWithSynapse.contains(Section.ALL) ||
                 groupsWithSynapse.contains(Section.SOMA_GROUP)))
            {

                float fract = ProjectManager.getRandomGenerator().nextFloat();
                if (allSegments.get(0).isSpherical()) fract = 0.5f;

                postSynTerm = new PostSynapticTerminalLocation(allSegments.get(0).getSegmentId(), fract);

                return postSynTerm;
            }


                //Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();

                for (int i = 0; i < allSegments.size(); i++)
                {
                    Segment dend = allSegments.elementAt(i);
                    Vector groups = dend.getGroups();
                    if ((pp.isAxonsAllowedPost() && groups.contains(Section.AXONAL_GROUP)) || 
                        (pp.isSomaAllowedPost() && groups.contains(Section.SOMA_GROUP)) ||
                        (pp.isDendritesAllowedPost() && groups.contains(Section.DENDRITIC_GROUP)))
                    {
                        for (int j = 0; j < groups.size(); j++)
                        {
                            if (groupsWithSynapse.contains( (String) groups.elementAt(j)))
                            {
                                if (!idsOfPossibleSegments.contains(new Integer(dend.getSegmentId())))
                                {
                                    idsOfPossibleSegments.add(dend.getSegmentId());
                                }
                            }
                        }
                    }
                }

                logger.logComment("Have found " + idsOfPossibleSegments.size() + " possible segments: " +
                                  idsOfPossibleSegments);

                if (idsOfPossibleSegments.isEmpty())
                {
                    return null;
                }
                lensOfPossibleSegments.setSize(idsOfPossibleSegments.size());

                for (int o = 0; o < idsOfPossibleSegments.size(); o++)
                {
                    int nextId = ( idsOfPossibleSegments.elementAt(o)).intValue();
                    Segment segment = cell.getSegmentWithId(nextId);
                    float length = segment.getSegmentLength();
                    lensOfPossibleSegments.setElementAt(length, o);

                    logger.logComment("Looking at segment: " + segment + ", id: " + nextId + ", length: " + length);
                    totalLengthOfValidSegments += length;
                }
                
                cachedPostSynLocInfo = new CachedSynLocInfo(cell.getInstanceName(), synapseType, idsOfPossibleSegments, lensOfPossibleSegments, totalLengthOfValidSegments);
                logger.logComment("Regenerated cachedPostSynLocInfo..");
            }
            else
            {
                totalLengthOfValidSegments = cachedPostSynLocInfo.totalLengthOfValidSegments;
                idsOfPossibleSegments = cachedPostSynLocInfo.idsOfPossibleSegments;
                lensOfPossibleSegments = cachedPostSynLocInfo.lensOfPossibleSegments;
                cachedPostSynLocInfo.stillValid();
                
                if (idsOfPossibleSegments.size()==1 && lensOfPossibleSegments.size()==1)
                {
                    float fract = -1;
                    if (lensOfPossibleSegments.get(0)==0) 
                        fract = 0.5f;
                    else
                        fract = ProjectManager.getRandomGenerator().nextFloat();

                    postSynTerm = new PostSynapticTerminalLocation(idsOfPossibleSegments.get(0), fract);

                    return postSynTerm;
                }
                logger.logComment("Reusing post details..");
            }
        
        
            float chosenDistAlongAll = totalLengthOfValidSegments * (ProjectManager.getRandomGenerator().nextFloat());

            logger.logComment("Total length: " + totalLengthOfValidSegments +
                              ", chosen point :" + chosenDistAlongAll + " along...");

            float distChecked = 0;
            int numSegmentsChecked = 0;
            boolean pointFound = false;
            float fractionAlongChosenSegment = 0;
            int nextIdToCheck = 0;

            if (totalLengthOfValidSegments == 0)
            {
                logger.logComment("Probably synaptic connection on soma...");
                nextIdToCheck = ( idsOfPossibleSegments.elementAt(numSegmentsChecked)).intValue();
                fractionAlongChosenSegment = 0.5f;
            }
            else
            {
                while (!pointFound && numSegmentsChecked <= idsOfPossibleSegments.size())
                {
                    nextIdToCheck = ( idsOfPossibleSegments.elementAt(numSegmentsChecked)).intValue();

                    //Segment segment = cell.getSegmentWithId(nextIdToCheck);
                    float length = lensOfPossibleSegments.get(numSegmentsChecked);
                    if ( (distChecked + length) > chosenDistAlongAll)
                    {
                        pointFound = true;
                        fractionAlongChosenSegment = (chosenDistAlongAll - distChecked) / length;
                    }
                    else
                    {
                        distChecked += length;
                        numSegmentsChecked++;
                    }
                }
            }
            postSynTerm
                = new PostSynapticTerminalLocation(nextIdToCheck,
                                                   fractionAlongChosenSegment);

            return postSynTerm;
        

    }
    
    public class CachedSynLocInfo
    {
        Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();
        Vector<Float> lensOfPossibleSegments = new Vector<Float>();
        float totalLengthOfValidSegments = 0;
        long timeCached = -1;
        String myCellName = null;
        String[] mySynapseType = null;
        
        public boolean isApplicable(String cellName, String[] synapseType)
        {
            if (!myCellName.equals(cellName))
                return false;
            if (System.currentTimeMillis()-timeCached >600) // wear out quickly...
                return false;
            if (mySynapseType.length!=synapseType.length)
                return false;
            for(int i=0;i<mySynapseType.length;i++)
            {
                if (!mySynapseType[i].equals(synapseType[i]))
                    return false;
            }
            
            return true;
        }
        
        private CachedSynLocInfo(){};
        
        public void stillValid()
        {
            timeCached = System.currentTimeMillis();
        }
        
        public CachedSynLocInfo(String cellName, 
                                String[] synapseType, 
                                Vector<Integer> idsOfPossibleSegments, 
                                Vector<Float> lensOfPossibleSegments,
                                float totalLengthOfValidSegments)
        {
            this.myCellName = cellName;
            this.mySynapseType = synapseType;
            this.idsOfPossibleSegments = idsOfPossibleSegments;
            this.lensOfPossibleSegments = lensOfPossibleSegments;
            this.totalLengthOfValidSegments = totalLengthOfValidSegments;
            timeCached = System.currentTimeMillis();
                
        }
            
    }

    /**
     * Search through the segments on a cell looking for the ones which
     * support the specific synapse types...
     *
     * @param cell The cell in question
     * @param synapseTypes String[] with synapse type name
     * @return The location of the synapse or null if not supported
     */

    public PreSynapticTerminalLocation getPossiblePreSynapticTerminal(Cell cell, 
                                                                      String[] synapseTypes,
                                                                      PrePostAllowedLocs pp)
    {
        
        float totalLengthOfValidSegments = 0;
        Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();
        Vector<Float> lensOfPossibleSegments = new Vector<Float>();
            
        if (!(cachedPreSynLocInfo!=null && cachedPreSynLocInfo.isApplicable(cell.getInstanceName(), synapseTypes))) 
        {
            cachedPreSynLocInfo =null;
            Vector<Segment> allSegments = cell.getAllSegments();

            Vector<String> groupsWithSynapse = cell.getGroupsWithSynapse(synapseTypes[0]); // get first lot
            String typesInfo = "";

            if (synapseTypes.length>1)
            {
                for (int remainingSynIndex = 0; remainingSynIndex < synapseTypes.length; remainingSynIndex++)
                {
                    
                    typesInfo = typesInfo+" "+synapseTypes[remainingSynIndex];
                    Vector remainingSynGroups = cell.getGroupsWithSynapse(synapseTypes[remainingSynIndex]);

                    for (int includedGroups = 0; includedGroups < groupsWithSynapse.size(); includedGroups++)
                    {
                            String includedGroup = groupsWithSynapse.elementAt(includedGroups);
                            if (!remainingSynGroups.contains(includedGroup))
                                groupsWithSynapse.remove(includedGroup);
                    }
                }
            }
            else
            {
                typesInfo = synapseTypes[0];
            }


            logger.logComment(cell.getInstanceName() + " being asked for pre-synaptic point of type " + typesInfo +
                              " among my segments");

            logger.logComment("groupsWithSynapse: "+ groupsWithSynapse);


            // Quick return if just a 1 seg cell 
            if (allSegments.size()==1 && 
                allSegments.get(0).isSomaSegment() &&
                pp.isSomaAllowedPre() &&
                (groupsWithSynapse.contains(Section.ALL) ||
                 groupsWithSynapse.contains(Section.SOMA_GROUP)))
            {
                //idsOfPossibleSegments.add(allSegments.get(0).getSegmentId());

                float fract = ProjectManager.getRandomGenerator().nextFloat();
                if (allSegments.get(0).isSpherical()) fract = 0.5f;

                PreSynapticTerminalLocation preSynTerm = new PreSynapticTerminalLocation(allSegments.get(0).getSegmentId(),
                        fract);

                return preSynTerm;
            }
            else
            {
                for (int i = 0; i < allSegments.size(); i++)
                {
                    Segment possSeg = allSegments.elementAt(i);
                    Vector groups = possSeg.getGroups();

                    if ((pp.isAxonsAllowedPre() && groups.contains(Section.AXONAL_GROUP)) || 
                        (pp.isSomaAllowedPre() && groups.contains(Section.SOMA_GROUP)) ||
                        (pp.isDendritesAllowedPre() && groups.contains(Section.DENDRITIC_GROUP)))
                    {
                        for (int j = 0; j < groups.size(); j++)
                        {
                            if (groupsWithSynapse.contains( (String) groups.elementAt(j)))
                            {
                                logger.logComment("seg is candidate: "+ possSeg);
                                if (!idsOfPossibleSegments.contains(possSeg.getSegmentId()))
                                {
                                    idsOfPossibleSegments.add(possSeg.getSegmentId());
                                }
                            }
                        }
                    }
                    else
                    {
                       logger.logComment("seg is not candidate: "+ possSeg);
                    }
                }
            }

            logger.logComment("Have found " + idsOfPossibleSegments.size() + " possible segments: " +
                              idsOfPossibleSegments);

            if (idsOfPossibleSegments.size() == 0)
            {
                return null;
            }
            lensOfPossibleSegments.setSize(idsOfPossibleSegments.size());
            
            for (int o = 0; o < idsOfPossibleSegments.size(); o++)
            {
                int nextId = ( idsOfPossibleSegments.elementAt(o)).intValue();
                Segment segment = cell.getSegmentWithId(nextId);
                float length = segment.getSegmentLength();
                lensOfPossibleSegments.setElementAt(length, o);
                
                logger.logComment("Looking at segment: " + segment + ", id: " + nextId + ", length: " + length);
                totalLengthOfValidSegments += length;
            }
        
            cachedPreSynLocInfo = new CachedSynLocInfo(cell.getInstanceName(), synapseTypes, idsOfPossibleSegments, lensOfPossibleSegments, totalLengthOfValidSegments);
            logger.logComment("Regenerated cachedPreSynLocInfo..");
        }
        else
        {
            totalLengthOfValidSegments = cachedPreSynLocInfo.totalLengthOfValidSegments;
            idsOfPossibleSegments = cachedPreSynLocInfo.idsOfPossibleSegments;
            lensOfPossibleSegments = cachedPreSynLocInfo.lensOfPossibleSegments;
            cachedPreSynLocInfo.stillValid();
            

            if (idsOfPossibleSegments.size()==1 && lensOfPossibleSegments.size()==1)
            {
                float fract = -1;
                if (lensOfPossibleSegments.get(0)==0) 
                    fract = 0.5f;
                else
                    fract = ProjectManager.getRandomGenerator().nextFloat();

                PreSynapticTerminalLocation preSynTerm = new PreSynapticTerminalLocation(idsOfPossibleSegments.get(0), fract);

                return preSynTerm;
            }
            
            logger.logComment("Reusing details..");
        }

        float chosenDistAlongAll = totalLengthOfValidSegments * (ProjectManager.getRandomGenerator().nextFloat());

        logger.logComment("Total length: " + totalLengthOfValidSegments +
                          ", chosen point :" + chosenDistAlongAll + " along...");

        float distChecked = 0;
        int numSegmentsChecked = 0;
        boolean pointFound = false;
        float fractionAlongChosenSegment = 0;
        int nextIdToCheck = 0;

        if (totalLengthOfValidSegments == 0)
        {
            logger.logComment("Probably synaptic connection on soma...");
            nextIdToCheck = ( idsOfPossibleSegments.elementAt(numSegmentsChecked)).intValue();
            fractionAlongChosenSegment = 0.5f;
        }
        else
        {
            while (!pointFound && numSegmentsChecked <= idsOfPossibleSegments.size())
            {
                nextIdToCheck = ( idsOfPossibleSegments.elementAt(numSegmentsChecked)).intValue();
                //Segment segment = cell.getSegmentWithId(nextIdToCheck);
                float length = lensOfPossibleSegments.get(numSegmentsChecked);
                if ( (distChecked + length) > chosenDistAlongAll)
                {
                    pointFound = true;
                    fractionAlongChosenSegment = (chosenDistAlongAll - distChecked) / length;
                }
                else
                {
                    distChecked += length;
                    numSegmentsChecked++;
                }
            }
        }
        PreSynapticTerminalLocation preSynTerm = new PreSynapticTerminalLocation(nextIdToCheck,
            fractionAlongChosenSegment);

        return preSynTerm;

    }
    
    private static int[] cachedAllCellIds = null;
    private static String cachedFinishCellGroup = null;
    private static MaxMinLength cachedMaxMinLength = null;
    private static Point3f cachedStartPos = null;
    private static String cachedStartCellType = null;
    private static RectangularBox cachedCellBoundBox = null;
    
    
    public static int[] getAllowedPostCellIds(Cell startCell,
                                              Cell finishCell,
                                              Point3f startCellPos, 
                                              MaxMinLength maxMin, 
                                              String finishCellGroup, 
                                              ArrayList<PositionRecord> finishPosRecords)
    {
        boolean maxMinIncludesAll = maxMin.getMinLength()==0 && maxMin.getMaxLength()==Float.MAX_VALUE;
        
        if (maxMinIncludesAll &&
            finishCellGroup.equals(cachedFinishCellGroup) &&
            finishPosRecords.size() == cachedAllCellIds.length)
        {
            return cachedAllCellIds;
        }
        
        int[] allowedFinishCells = null;
        
        if (maxMinIncludesAll || finishCell.getAllSegments().size()>1)  // TODO: deal with case where post cell size is taken into account...
        {
            allowedFinishCells = new int[finishPosRecords.size()];

            for(int i=0;i<finishPosRecords.size();i++)
            {
                allowedFinishCells[i] = finishPosRecords.get(i).cellNumber;
            }
        }
        else
        {
            RectangularBox cellBox = null;
            
            if(cachedStartCellType!=null && cachedStartCellType.equals(startCell.getInstanceName()))
            {
                cellBox = cachedCellBoundBox;
            }
            else
            {
                float maxX = CellTopologyHelper.getMaxXExtent(startCell, false, false);
                float maxY = CellTopologyHelper.getMaxYExtent(startCell, false, false);
                float maxZ = CellTopologyHelper.getMaxZExtent(startCell, false, false);
                float minX = CellTopologyHelper.getMinXExtent(startCell, false, false);
                float minY = CellTopologyHelper.getMinYExtent(startCell, false, false);
                float minZ = CellTopologyHelper.getMinZExtent(startCell, false, false);

                cellBox = new RectangularBox(minX, minY, minZ, maxX-minX, maxY-minY, maxZ-minZ);

                if (maxMin.getDimension().equals(MaxMinLength.RADIAL) ||
                   (maxMin.getDimension().equals(MaxMinLength.SOMA)) ||
                    maxMin.getDimension().equals(MaxMinLength.X_DIR))
                {
                    cellBox.setParameter(RectangularBox.X_PARAM, cellBox.getLowestXValue() - maxMin.getMaxLength());
                    cellBox.setParameter(RectangularBox.WIDTH_PARAM, cellBox.getXExtent() + 2*maxMin.getMaxLength());
                }
                if (maxMin.getDimension().equals(MaxMinLength.RADIAL) ||
                   (maxMin.getDimension().equals(MaxMinLength.SOMA)) ||
                    maxMin.getDimension().equals(MaxMinLength.Y_DIR))
                {
                    cellBox.setParameter(RectangularBox.Y_PARAM, cellBox.getLowestYValue() - maxMin.getMaxLength());
                    cellBox.setParameter(RectangularBox.HEIGHT_PARAM, cellBox.getYExtent() + 2*maxMin.getMaxLength());
                }
                if (maxMin.getDimension().equals(MaxMinLength.RADIAL) ||
                   (maxMin.getDimension().equals(MaxMinLength.SOMA)) ||
                    maxMin.getDimension().equals(MaxMinLength.Z_DIR))
                {
                    cellBox.setParameter(RectangularBox.Z_PARAM, cellBox.getLowestZValue() - maxMin.getMaxLength());
                    cellBox.setParameter(RectangularBox.DEPTH_PARAM, cellBox.getZExtent() + 2*maxMin.getMaxLength());
                }
                
                
                if (maxMin.getDimension().equals(MaxMinLength.X_DIR))
                {
                    cellBox.setParameter(RectangularBox.Y_PARAM, Float.MAX_VALUE*-1);
                    cellBox.setParameter(RectangularBox.HEIGHT_PARAM, Float.MAX_VALUE);
                    cellBox.setParameter(RectangularBox.Z_PARAM, Float.MAX_VALUE*-1);
                    cellBox.setParameter(RectangularBox.DEPTH_PARAM, Float.MAX_VALUE);
                }
                
                if (maxMin.getDimension().equals(MaxMinLength.Y_DIR))
                {
                    cellBox.setParameter(RectangularBox.X_PARAM, Float.MAX_VALUE*-1);
                    cellBox.setParameter(RectangularBox.WIDTH_PARAM, Float.MAX_VALUE);
                    cellBox.setParameter(RectangularBox.Z_PARAM, Float.MAX_VALUE*-1);
                    cellBox.setParameter(RectangularBox.DEPTH_PARAM, Float.MAX_VALUE);
                }
                
                if (maxMin.getDimension().equals(MaxMinLength.Z_DIR))
                {
                    cellBox.setParameter(RectangularBox.X_PARAM, Float.MAX_VALUE*-1);
                    cellBox.setParameter(RectangularBox.WIDTH_PARAM, Float.MAX_VALUE);
                    cellBox.setParameter(RectangularBox.Y_PARAM, Float.MAX_VALUE*-1);
                    cellBox.setParameter(RectangularBox.HEIGHT_PARAM, Float.MAX_VALUE);
                }               

                cachedStartCellType = startCell.getInstanceName();
                cachedCellBoundBox = cellBox;
            }
                
            int[] tempPosRecs = new int[finishPosRecords.size()]; // set to max temp

            int countGoodPos = 0;
            
            RectangularBox connBoundBox = (RectangularBox)cellBox.getTranslatedRegion(new Vector3f(startCellPos));

            float minX = connBoundBox.getLowestXValue();
            float minY = connBoundBox.getLowestYValue();
            float minZ = connBoundBox.getLowestZValue();
            float maxX = connBoundBox.getHighestXValue();
            float maxY = connBoundBox.getHighestYValue();
            float maxZ = connBoundBox.getHighestZValue();
            
            for(PositionRecord pos: finishPosRecords)
            {
                if (pos.x_pos >= minX &&
                    pos.x_pos <= maxX &&
                    pos.y_pos >= minY &&
                    pos.y_pos <= maxY &&
                    pos.z_pos >= minZ &&
                    pos.z_pos <= maxZ)
                {
                    tempPosRecs[countGoodPos] = pos.cellNumber;
                    countGoodPos++;
                }
                else
                {
                    logger.logComment("No good point: "+pos+" for start cell: "+ startCellPos);
                }
            }

            allowedFinishCells = new int[countGoodPos];

            for(int i=0;i<countGoodPos;i++)
            {
                allowedFinishCells[i] = tempPosRecs[i];
            }
            logger.logComment("Total finish cells: "+ finishPosRecords.size() +", but returning only: "+ countGoodPos+", box: "+ connBoundBox);
                        
        }
        
        
        cachedFinishCellGroup = finishCellGroup;
        cachedAllCellIds = allowedFinishCells;
        cachedMaxMinLength = maxMin;
        
        return allowedFinishCells;
    }
    
    
    
    
    
    

    /**
     * Converts from a specification of a point along a particular segment of
     * the cell to a Point3f specification of the location.
     *
     * @param cell The cell
     * @param segmentId the id of the segment
     * @param displacementAlong 0 for start 1 for end
     * @return Point3f representation of point
     */
    public static Point3f convertSegmentDisplacement(Cell cell,
                                                     int segmentId,
                                                     float displacementAlong)
    {
        /*
        logger.logComment("Going to convert point on "
                          + cell.getInstanceName()
                          + ", segment id: "
                          + segmentId
                          + " distAlong: "
                          + displacementAlong);*/

        Segment segment = cell.getSegmentWithId(segmentId);

        Point3f startPoint = segment.getStartPointPosition();
        Point3f endPoint = segment.getEndPointPosition();

        Point3f convPoint = new Point3f(startPoint.x +
                                        (displacementAlong * (endPoint.x - startPoint.x)),
                                        startPoint.y +
                                        (displacementAlong * (endPoint.y - startPoint.y)),
                                        startPoint.z +
                                        (displacementAlong * (endPoint.z - startPoint.z)));

        /*
        logger.logComment("*************************    Returning info on segment " + segmentId +
                          ": "
                          + segment.getSegmentName()
                          + ", start point:  "
                          + Utils3D.getShortStringDesc(startPoint));

        logger.logComment("endPoint: "
                          + Utils3D.getShortStringDesc(endPoint)
                          + ", returning point : "
                          + displacementAlong
                          + " along: "
                          + Utils3D.getShortStringDesc(convPoint));*/

        return convPoint;
    }

    /**
     * Gets all the segments in a section in the correct order parent->child->child, etc.
     */
    public static Vector<Segment> getOrderedSegmentsInSection(Cell cell,
                                                     Section section)
    {
        Vector allSegments = cell.getAllSegments();
        Vector<Segment> secSegments = new Vector<Segment>();
        logger.logComment("Getting all segments in section: "+ section);
        for (int j = 0; j < allSegments.size(); j++)
        {
                Segment nextSeg = (Segment)allSegments.elementAt(j);
                if (nextSeg.getSection().equals(section))
                {
                    logger.logComment("Found segment: "+ nextSeg);
                    boolean problem = false;
                    //secSegments.add(nextSeg);
                    for (int k = 0; k < secSegments.size();k++)
                    {
                        Segment oldSeg = (Segment)allSegments.elementAt(k);
                    logger.logComment("Checking old seg: : "+ oldSeg);

                        if (oldSeg.getParentSegment()!=null && oldSeg.getParentSegment().equals(nextSeg))
                        {
                            logger.logComment("Problem...");
                            problem = true;
                            secSegments.setElementAt(nextSeg, k);
                            secSegments.add(oldSeg);
                        }
                    }
                    if (!problem) secSegments.add(nextSeg);
                }
        }
        return secSegments;
    }
    
    
    
    /*
     * Checks whether a syanapse type is allowed somewhere
     */
    public static boolean isSynapseAllowed(Cell cell, String synType)
    {
        Enumeration<String> syns = cell.getSynapsesVsGroups().keys();
        while(syns.hasMoreElements())
        {
            String syn = syns.nextElement();
            if (syn.equals(synType))
            {
                boolean atLeastOneGroupOK = false;
                Vector<String> groups = cell.getSynapsesVsGroups().get(syn);
                for (String grp: groups)
                {
                    if (cell.isGroup(grp))
                        atLeastOneGroupOK = true;
                }
                if (atLeastOneGroupOK) 
                    return true;
                
            }
        }
        return false;
        
    }
    
    
    /*
     * Checks whether all sections of groupA are in groupB for cell
     */
    public static boolean isGroupASubset(String groupA, String groupB, Cell cell)
    {
       ArrayList<Section> setA = cell.getSectionsInGroup(groupA);
       ArrayList<Section> setB = cell.getSectionsInGroup(groupB);
       
       if (setA.size()>setB.size())
           return false;
       
       for(Section sec: setA)
       {
           if (!setB.contains(sec)) return false;
       }
       return true;
    }
    
    
    /*
     * Tests to see if any of the cell mechanisms in this cell have extra params, e.g.
     * cell mech specification on cell changes internal channelml parameters
     */
    public static boolean hasExtraCellMechParams(Cell cell)
    {
        ArrayList<ChannelMechanism> chanMechs = cell.getAllUniformChanMechs(true);
        for(ChannelMechanism cm: chanMechs)
        {
            if (cm.getExtraParameters().size()>0) return true;
        }
        return false;
    }
        
        


    /**
     * Converts from a specification of a point along a particular section of
     * the cell to a Point3f specification of the location.
     *
     * @param cell The cell
     * @param section the section in question
     * @param displacementAlong 0 for start 1 for end
     * @return Point3f representation of point
     */
    public static Point3f convertSectionDisplacement(Cell cell,
                                                     Section section,
                                                     float displacementAlong)
    {
        logger.logComment("Going to convert point on "
                          + cell.getInstanceName()
                          + ", section: "
                          + section
                          + " distAlong: "
                          + displacementAlong);

        //Segment segment = cell.getSegmentWithId(segmentId);

        float totalLengthParentSection = getSectionLength(cell, section);

        logger.logComment("totalLengthParentSection: "+ totalLengthParentSection);

        if (totalLengthParentSection==0)
        {
            return section.getStartPointPosition();
        }

        Vector allSegsInSection = getOrderedSegmentsInSection(cell,section);

        float lengthToGo = totalLengthParentSection * displacementAlong;
        float totalSoFar = 0;
        Segment segmentContainingPoint = null;
        float fractionAlongSegment = 0;
        int count = 0;

        while (segmentContainingPoint==null)
        {
            Segment nextSeg = (Segment) allSegsInSection.elementAt(count);
            count++;
            logger.logComment("Old totalSoFar: "+totalSoFar);

            if(totalSoFar + nextSeg.getSegmentLength()>= lengthToGo)
            {
                logger.logComment("Reached point...");
                float distAlongSeg = lengthToGo - totalSoFar;
                fractionAlongSegment = distAlongSeg/nextSeg.getSegmentLength();

                logger.logComment("fractionAlongSegment: "+fractionAlongSegment);


                segmentContainingPoint = nextSeg;
            }

            totalSoFar = totalSoFar + nextSeg.getSegmentLength();
            logger.logComment("New totalSoFar: "+totalSoFar);
        }

        Point3f point = convertSegmentDisplacement(cell,
                                                   segmentContainingPoint.getSegmentId(),
                                                   fractionAlongSegment);

        return point;
    }


    public static float getTotalAxialResistance(Segment segment, float specAxRes)
    {
        SimpleCompartment equivComp = new SimpleCompartment(segment.getSegmentStartRadius(),
                                                            segment.getRadius(),
                                                            segment.getSegmentLength());

        return (float)CompartmentHelper.getTotalAxialRes(equivComp, specAxRes);

    }


    public static RectangularBox getSurroundingBox(Cell cell,  boolean somaOnly, boolean inclAxArbors)
    {
        float minX = getMinXExtent(cell, somaOnly, inclAxArbors);
        float minY = getMinYExtent(cell, somaOnly, inclAxArbors);
        float minZ = getMinZExtent(cell, somaOnly, inclAxArbors);

        float maxX = getMaxXExtent(cell, somaOnly, inclAxArbors);
        float maxY = getMaxYExtent(cell, somaOnly, inclAxArbors);
        float maxZ = getMaxZExtent(cell, somaOnly, inclAxArbors);


        RectangularBox box = new RectangularBox(minX,
                                                minY,
                                                minZ,
                                                maxX - minX,
                                                maxY - minY,
                                                maxZ - minZ);

        return box;
    }

    /**
     * As outlined in The GENESIS book p61
     */
    public static float getSpaceConstant(Segment segment, float specMembRes, float specAxRes)
    {

        float ratio = specMembRes / specAxRes;

        /** @todo See if this assumption is valid... */
        float diam = (float)CompartmentHelper.getEquivalentRadius(segment.getSegmentStartRadius(),
                                                           segment.getRadius(),
                                                           segment.getSegmentLength()) * 2;

       float lambda = (float)Math.sqrt(ratio * diam / 4);

       return lambda;

    }

    public static float getElectrotonicLength(Segment segment, float specMembRes, float specAxRes)
    {
        float lambda = getSpaceConstant(segment, specMembRes, specAxRes);

        float el = segment.getSegmentLength()/lambda;

        return el;
    }

    public static float getSectionLength(Cell cell,
                                           Section section)
    {

        float totalLengthSection = 0;

        LinkedList<Segment> allSegsInSection = cell.getAllSegmentsInSection(section);

        for (Segment nextSeg: allSegsInSection)
        {
            totalLengthSection = totalLengthSection  + nextSeg.getSegmentLength();
        }

        logger.logComment("totalLengthSection: "+ totalLengthSection);

        return totalLengthSection;
    }


    /**
     * Search through the segments on a cell looking for the one which is
     * closest to the external point in question
     *
     * @param cell The cell to check
     * @param synapseTypes String[] with synapse type names
     * @param extPoint the external point
     * @return The location of the synapse or null if not supported

     */
    public static PostSynapticTerminalLocation getClosestPostSynapticTerminalLocation(Cell cell,
                                                                                      String[] synapseTypes, 
                                                                                      Point3f extPoint,
                                                                                      PrePostAllowedLocs pp)
    {
        Vector allSegments = cell.getAllSegments();
        Vector groupsWithSynapse = cell.getGroupsWithSynapse(synapseTypes[0]); // get first lot

        if (synapseTypes.length > 1)
        {
            for (int remainingSynIndex = 0; remainingSynIndex < synapseTypes.length; remainingSynIndex++)
            {
                Vector remainingSynGroups = cell.getGroupsWithSynapse(synapseTypes[remainingSynIndex]);

                for (int includedGroups = 0; includedGroups < groupsWithSynapse.size(); includedGroups++)
                {
                    String includedGroup = (String) groupsWithSynapse.elementAt(includedGroups);
                    if (!remainingSynGroups.contains(includedGroup))
                        groupsWithSynapse.remove(includedGroup);
                }
            }
        }


        logger.logComment(cell.getInstanceName()
                          + " being asked for synapse of type "
                          +  synapseTypes[0]
                          + ", etc among my dendritic segments, closest to: "
                          + Utils3D.getShortStringDesc(extPoint));

        Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment dend = (Segment) allSegments.elementAt(i);
            Vector groups = dend.getGroups();
            if ((pp.isAxonsAllowedPost() && groups.contains(Section.AXONAL_GROUP)) || 
                (pp.isSomaAllowedPost() && groups.contains(Section.SOMA_GROUP)) ||
                (pp.isDendritesAllowedPost() && groups.contains(Section.DENDRITIC_GROUP)))
            {
                for (int j = 0; j < groups.size(); j++)
                {
                    if (groupsWithSynapse.contains( (String) groups.elementAt(j)))
                    {
                        if (!idsOfPossibleSegments.contains(dend.getSegmentId()))
                        {
                            idsOfPossibleSegments.add(dend.getSegmentId());
                        }
                    }
                }
            }
        }

        logger.logComment("Have found " + idsOfPossibleSegments.size() + " possible segments");

        if (idsOfPossibleSegments.isEmpty())
        {
            return null;
        }

        float bestDistanceSoFar = Float.MAX_VALUE;

        int idOfBest = -1;
        float distAlongBest = -1f;

        for (int o = 0; o < idsOfPossibleSegments.size(); o++)
        {
            int nextId = idsOfPossibleSegments.elementAt(o).intValue();
            logger.logComment("Checking nextId: " + nextId);
            Segment segment = cell.getSegmentWithId(nextId);

            logger.logComment("Checking segment: " + segment);

            Point3f closestPoint = getClosestPointOnLine(segment.getStartPointPosition(),
                                                            segment.getEndPointPosition(),
                                                            extPoint);

            if (extPoint.distance(closestPoint) < bestDistanceSoFar)
            {
                bestDistanceSoFar = extPoint.distance(closestPoint);
                idOfBest = nextId;

                if (segment.getStartPointPosition().distance(segment.getEndPointPosition())==0)
                    distAlongBest = 0;

                else
                    distAlongBest
                    = (segment.getStartPointPosition().distance(closestPoint)) /
                    (segment.getStartPointPosition().distance(segment.getEndPointPosition()));

                logger.logComment("Best so far: "
                                  + bestDistanceSoFar
                                  + " idOfBest: "
                                  + idOfBest
                                  + " distAlongBest: "
                                  + distAlongBest);

            }

        }

        PostSynapticTerminalLocation postSynTerm
            = new PostSynapticTerminalLocation(idOfBest, distAlongBest);

        logger.logComment("Returning: " + postSynTerm);

        return postSynTerm;

    }

    /**
     * Search through the segments on a cell looking for the one which is
     * closest to the external point in question
     *
     * @param cell The cell to check
     * @param synapseTypes String with synapse type names
     * @param extPoint the external point
     * @return The location of the synapse or null if not supported

     */
    public static PreSynapticTerminalLocation getClosestPreSynapticTerminalLocation(Cell cell,
                                                                                    String[] synapseTypes, 
                                                                                    Point3f extPoint,
                                                                                    PrePostAllowedLocs pp)
    {
        Vector allSegments = cell.getAllSegments();

        Vector groupsWithSynapse = cell.getGroupsWithSynapse(synapseTypes[0]); // get first lot

        if (synapseTypes.length > 1)
        {
            for (int remainingSynIndex = 0; remainingSynIndex < synapseTypes.length; remainingSynIndex++)
            {
                Vector remainingSynGroups = cell.getGroupsWithSynapse(synapseTypes[remainingSynIndex]);

                for (int includedGroups = 0; includedGroups < groupsWithSynapse.size(); includedGroups++)
                {
                    String includedGroup = (String) groupsWithSynapse.elementAt(includedGroups);
                    if (!remainingSynGroups.contains(includedGroup))
                        groupsWithSynapse.remove(includedGroup);
                }
            }
        }


        logger.logComment(cell.getInstanceName()
                          + " being asked for synapse of type "
                          + synapseTypes[0]
                          + ", etc. among my axonal segments, closest to: "
                          + Utils3D.getShortStringDesc(extPoint));

        Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment axon = (Segment) allSegments.elementAt(i);
            Vector groups = axon.getGroups();
            if ((pp.isAxonsAllowedPre() && groups.contains(Section.AXONAL_GROUP)) || 
                (pp.isSomaAllowedPre() && groups.contains(Section.SOMA_GROUP)) ||
                (pp.isDendritesAllowedPre() && groups.contains(Section.DENDRITIC_GROUP)))
            {
                for (int j = 0; j < groups.size(); j++)
                {
                    if (groupsWithSynapse.contains( (String) groups.elementAt(j)))
                    {
                        if (!idsOfPossibleSegments.contains(axon.getSegmentId()))
                        {
                            idsOfPossibleSegments.add(axon.getSegmentId());
                        }
                    }
                }
            }
        }

        logger.logComment("Have found " + idsOfPossibleSegments.size() + " possible segments");

        if (idsOfPossibleSegments.size() == 0)
        {
            return null;
        }

        float bestDistanceSoFar = Float.MAX_VALUE;

        int idOfBest = -1;
        float distAlongBest = -1f;

        for (int o = 0; o < idsOfPossibleSegments.size(); o++)
        {
            int nextId = idsOfPossibleSegments.elementAt(o).intValue();
            Segment segment = cell.getSegmentWithId(nextId);

            logger.logComment("Checking segment: " + segment);

            Point3f closestPoint = getClosestPointOnLine(segment.getStartPointPosition(),
                                                            segment.getEndPointPosition(),
                                                            extPoint);

            if (extPoint.distance(closestPoint) < bestDistanceSoFar)
            {
                bestDistanceSoFar = extPoint.distance(closestPoint);
                idOfBest = nextId;

                if (segment.getStartPointPosition().distance(segment.getEndPointPosition())==0)
                             distAlongBest = 0;

                         else
                distAlongBest
                    = (segment.getStartPointPosition().distance(closestPoint)) /
                    (segment.getStartPointPosition().distance(segment.getEndPointPosition()));

                logger.logComment("Best so far: "
                                  + bestDistanceSoFar
                                  + " idOfBest: "
                                  + idOfBest
                                  + " distAlongBest: "
                                  + distAlongBest);

            }
            /*
                   // check end point
                         float endPointDist = extPoint.distance(segment.getEndPointPosition());
                         logger.logComment("end: "+ endPointDist);
                         if (endPointDist<bestDistanceSoFar)
                         {
                bestDistanceSoFar = endPointDist;
                indexOfBest = nextIndex;
                distAlongBest = 1f;
                logger.logComment("end is best so far...");
                         }


                     // check start point...
                         float startPointDist = extPoint.distance(segment.getStartPointPosition());
                         logger.logComment("startPointDist: "+ startPointDist);
                         if (startPointDist<bestDistanceSoFar)
                         {
                bestDistanceSoFar = startPointDist;
                indexOfBest = nextIndex;
                distAlongBest = 0f;
                logger.logComment("start is best so far...");
                         }
             */
        }

        PreSynapticTerminalLocation preSynTerm
            = new PreSynapticTerminalLocation(idOfBest, distAlongBest);

        logger.logComment("Returning: " + preSynTerm);

        return preSynTerm;
    }


    public static Point3f getAbsolutePosSegLoc(Project project,
                                               String cellGroup,
                                               int cellNum,
                                               SegmentLocation segLocation)
    {
        Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroup));

        Point3f relativePointSegLoc
            = CellTopologyHelper.convertSegmentDisplacement(cell,
                                                            segLocation.getSegmentId(),
                                                            segLocation.getFractAlong());

        logger.logComment("relativePointSegLoc: " +
                          Utils3D.getShortStringDesc(relativePointSegLoc));

        Point3f cellPosition
            = project.generatedCellPositions.getOneCellPosition(cellGroup,
                                                                cellNum);

        Point3f segLocAbsolutePosition = new Point3f(cellPosition);

        segLocAbsolutePosition.add(relativePointSegLoc);

        logger.logComment("segLocAbsolutePosition: " +
                          Utils3D.getShortStringDesc(segLocAbsolutePosition));

        return segLocAbsolutePosition;

    }


    /**
     * Gets the closest point on the line between startPoint and endPoint, from extPoint
     * Note not just perpendicularly closest, has to be to point ON the line
     */
    private static Point3f getClosestPointOnLine(Point3f startPoint, Point3f endPoint, Point3f extPoint)
    {
        if (startPoint.equals(endPoint)) return startPoint;

        float lengthSquared = startPoint.distanceSquared(endPoint);
        float lengthToStartSquared = extPoint.distanceSquared(startPoint);
        float lengthToEndSquared = extPoint.distanceSquared(endPoint);

        logger.logComment("lengthSquared: " + lengthSquared +
                          ", lengthToStartSquared: " + lengthToStartSquared +
                          ", lengthToEndSquared: " + lengthToEndSquared);

        // if it's "behind" the start point...
        if (lengthToEndSquared >= lengthToStartSquared + lengthSquared)
        {
            logger.logComment("Closest to start point");
            return startPoint;
        }

        // if it's "ahead of" the end point...
        else if (lengthToStartSquared >= lengthToEndSquared + lengthSquared)
        {
            logger.logComment("Closest to end point");
            return endPoint;
        }
        else
        {
            logger.logComment("Closest to point between start and end");
            Vector3f lineRelToOrigin = new Vector3f(endPoint);
            lineRelToOrigin.sub(startPoint);

            Vector3f extPointRelToOrigin = new Vector3f(extPoint);
            extPointRelToOrigin.sub(startPoint);

            Vector3f unitVecParallelLine = new Vector3f(lineRelToOrigin);
            unitVecParallelLine.normalize();

            float projectionExtPoint = extPointRelToOrigin.dot(unitVecParallelLine);

            Vector3f pointOnLine = new Vector3f(unitVecParallelLine);
            pointOnLine.scale(projectionExtPoint);
            pointOnLine.add(startPoint);
            return new Point3f(pointOnLine);
        }

    }
    
    /**
     * Gets the absolute distance between the endpoints on a source and
     * target cell
     */
    public static boolean checkDistanceApart(Project project,
                                             String sourceCellGroup,
                                             Cell sourceCell,           // superfluous, given prev arg, but better performing on multiple calls
                                             SynapticConnectionEndPoint sourceEndPoint,
                                             String targetCellGroup,
                                             Cell targetCell,           // superfluous, given prev arg, but better performing on multiple calls
                                             SynapticConnectionEndPoint targetEndPoint,
                                             float minDistance,
                                             float maxDistance,
                                             String dimension)
    {
        Point3f relativePointSource
            = CellTopologyHelper.convertSegmentDisplacement(
            sourceCell,
            sourceEndPoint.location.getSegmentId(),
            sourceEndPoint.location.getFractAlong());

        Point3f sourceCellPosition
            = project.generatedCellPositions.getOneCellPosition(
            sourceCellGroup,
            sourceEndPoint.cellNumber);

        Point3f sourceSynapsePosition = new Point3f(sourceCellPosition);
        sourceSynapsePosition.add(relativePointSource);


        Point3f relativePointTarget
            = CellTopologyHelper.convertSegmentDisplacement(
            targetCell,
            targetEndPoint.location.getSegmentId(),
            targetEndPoint.location.getFractAlong());

        Point3f targetCellPosition
            = project.generatedCellPositions.getOneCellPosition(
            targetCellGroup,
            targetEndPoint.cellNumber);

        Point3f targetSynapsePosition = new Point3f(targetCellPosition);
        targetSynapsePosition.add(relativePointTarget);
        
        float dist = 0;

        if (dimension.equals(MaxMinLength.X_DIR)) 
        {
            dist = Math.abs(targetSynapsePosition.x - sourceSynapsePosition.x);
            return (dist>=minDistance && dist<=maxDistance);
        }
        else if (dimension.equals(MaxMinLength.Y_DIR)) 
        {
            dist = Math.abs(targetSynapsePosition.y - sourceSynapsePosition.y);
            return (dist>=minDistance && dist<=maxDistance);
        }
        else if (dimension.equals(MaxMinLength.Z_DIR)) 
        {
            dist = Math.abs(targetSynapsePosition.z - sourceSynapsePosition.z);
            return (dist>=minDistance && dist<=maxDistance);
        }
        else
        {
            // Quick check x, y, z dims...
            float xDist = Math.abs(targetSynapsePosition.x - sourceSynapsePosition.x);
            if (xDist<minDistance || xDist>maxDistance) 
                return false;
            
            float yDist = Math.abs(targetSynapsePosition.y - sourceSynapsePosition.y);
            if (yDist<minDistance || yDist>maxDistance) 
                return false;
            
            float zDist = Math.abs(targetSynapsePosition.z - sourceSynapsePosition.z);
            if (zDist<minDistance || zDist>maxDistance) 
                return false;
            
            dist = targetSynapsePosition.distance(sourceSynapsePosition);
            return (dist>=minDistance && dist<=maxDistance);
            
        }

    }

    /**
     * Gets the absolute distance between the endpoints on a source and
     * target cell
     */
    public static float getSynapticEndpointsDistance(Project project,
                                                     String sourceCellGroup,
                                                     SynapticConnectionEndPoint sourceEndPoint,
                                                     String targetCellGroup,
                                                     SynapticConnectionEndPoint targetEndPoint,
                                                     String dimension)
    {
        logger.logComment("getSynapticEndpointsDistance called for: "
                          + "sourceCellGroup: " + sourceCellGroup
                          + ", sourceEndPoint: " + sourceEndPoint
                          + ", targetCellGroup: " + targetCellGroup
                          + ", targetEndPoint: " + targetEndPoint
                          + ", dimension: " + dimension);

        String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);
        Cell sourceCell = project.cellManager.getCell(sourceCellType);

        Point3f relativePointSource
            = CellTopologyHelper.convertSegmentDisplacement(
            sourceCell,
            sourceEndPoint.location.getSegmentId(),
            sourceEndPoint.location.getFractAlong());

        Point3f sourceCellPosition
            = project.generatedCellPositions.getOneCellPosition(
            sourceCellGroup,
            sourceEndPoint.cellNumber);

        Point3f sourceSynapsePosition = new Point3f(sourceCellPosition);
        sourceSynapsePosition.add(relativePointSource);

        String targetCellType = project.cellGroupsInfo.getCellType(targetCellGroup);
        Cell targetCell = project.cellManager.getCell(targetCellType);

        Point3f relativePointTarget
            = CellTopologyHelper.convertSegmentDisplacement(
            targetCell,
            targetEndPoint.location.getSegmentId(),
            targetEndPoint.location.getFractAlong());

        Point3f targetCellPosition
            = project.generatedCellPositions.getOneCellPosition(
            targetCellGroup,
            targetEndPoint.cellNumber);

        Point3f targetSynapsePosition = new Point3f(targetCellPosition);
        targetSynapsePosition.add(relativePointTarget);

        float dist = 0;

        if (dimension.equals(MaxMinLength.X_DIR)) dist = Math.abs(targetSynapsePosition.x - sourceSynapsePosition.x);
        else if (dimension.equals(MaxMinLength.Y_DIR)) dist =  Math.abs(targetSynapsePosition.y - sourceSynapsePosition.y);
        else if (dimension.equals(MaxMinLength.Z_DIR)) dist = Math.abs(targetSynapsePosition.z - sourceSynapsePosition.z);

        // should be r, but just in case something else is entered...
        else dist = targetSynapsePosition.distance(sourceSynapsePosition);

        logger.logComment("Dist between: " + targetSynapsePosition+" and "+sourceSynapsePosition
                          +" in dimension: "+ dimension+" = "+dist);

        return dist;

    }

    /**
     * Shifts all start and end points by the Vector translation
     */
    public static Cell translateAllPositions(Cell oldCell, Vector3f translation)
    {
        logger.logComment("Moving cell: "+ oldCell.getInstanceName() + " with first soma section: ");
        logger.logComment(oldCell.getFirstSomaSegment().toString());
        logger.logComment("with translation: "+ translation);

        Vector allSegments = oldCell.getAllSegments();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment)allSegments.elementAt(i);
            logger.logComment("Segment: "+ seg+", --- "+(seg.getEndPointPositionY())+" = "+(translation.y) +" = "+(seg.getEndPointPositionY()+translation.y));
            seg.setEndPointPositionX(seg.getEndPointPositionX()+translation.x);
            seg.setEndPointPositionY(seg.getEndPointPositionY()+translation.y);
            seg.setEndPointPositionZ(seg.getEndPointPositionZ()+translation.z);
            logger.logComment("Segment: "+ seg);

            if (seg.isFirstSectionSegment())
            {
                Section sec = seg.getSection();
                sec.setStartPointPositionX(sec.getStartPointPositionX()+translation.x);
                sec.setStartPointPositionY(sec.getStartPointPositionY()+translation.y);
                sec.setStartPointPositionZ(sec.getStartPointPositionZ()+translation.z);
            }
        }

        return oldCell;
    }

    /**
     * Shifts all sections to the point where they are connected to their parent,
     * retaining relative shape
     */
    public static Cell moveSectionsToConnPointsOnParents(Cell cell)
    {
        Hashtable<String, Vector3f> sectionMovements = new Hashtable<String, Vector3f>();
        Vector allSegments = cell.getAllSegments();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment)allSegments.elementAt(i);

            logger.logComment("Segment (to move?): "+ seg);

            logger.logComment("so far: "+ sectionMovements);

            if (seg.isSomaSegment())
            {
                // soma segments don't move...
                if (!sectionMovements.containsKey(seg.getSection().getSectionName()))
                sectionMovements.put(new String(seg.getSection().getSectionName()),
                                     new Vector3f(0,0,0));

            }
            else
            {
                Segment parentSegment = seg.getParentSegment();

                if (seg.isFirstSectionSegment())
                {

                    logger.logComment("---  start of new section: "+ seg.getSection().getSectionName());

                  //  Vector3f secTranslationSoFar
                  //      = new Vector3f((Vector3f)sectionMovements.get(parentSegment.getSection().getSectionName()));

                  //  logger.logComment("secTranslationSoFar: "+ secTranslationSoFar);

                    Point3f parentStart = parentSegment.getStartPointPosition();
                    Point3f parentEnd = parentSegment.getEndPointPosition();
                    Point3f pointToConnectTo
                        = new Point3f(parentStart.x + (seg.getFractionAlongParent()) * (parentEnd.x - parentStart.x),
                                      parentStart.y + (seg.getFractionAlongParent()) * (parentEnd.y - parentStart.y),
                                      parentStart.z + (seg.getFractionAlongParent()) * (parentEnd.z - parentStart.z));


                    logger.logComment("Point currently at: "+ seg.getStartPointPosition()
                                      + " is supposed to be at: "+ pointToConnectTo);

                    Vector3f extraTranslation
                        = new Vector3f(pointToConnectTo);
                    extraTranslation.sub(seg.getStartPointPosition());


                    logger.logComment("Diff: "+ extraTranslation);

                    //secTranslationSoFar.add(extraTranslation);
                    //Vector3f secTranslationSoFar


                    sectionMovements.put(seg.getSection().getSectionName(), extraTranslation);


                }
                Vector3f translationForSegment
                        = sectionMovements.get(seg.getSection().getSectionName());

                logger.logComment("translationForSegment: "+ translationForSegment);

                seg.setEndPointPositionX(seg.getEndPointPositionX()+translationForSegment.x);
                seg.setEndPointPositionY(seg.getEndPointPositionY()+translationForSegment.y);
                seg.setEndPointPositionZ(seg.getEndPointPositionZ()+translationForSegment.z);
                if (seg.isFirstSectionSegment())
                {
                    Section sec = seg.getSection();
                    sec.setStartPointPositionX(sec.getStartPointPositionX() + translationForSegment.x);
                    sec.setStartPointPositionY(sec.getStartPointPositionY() + translationForSegment.y);
                    sec.setStartPointPositionZ(sec.getStartPointPositionZ() + translationForSegment.z);
                }


            }
        }

        return cell;
    }

/*
    public static boolean makeSimplyConnected(Cell cell)
    {
        logger.logComment(",,,,,,,,,,,,   Making cell: "+ cell + " Simply Connected");
        // Check first if the cell is properly connected...
        for (int i = 0; i < cell.getAllSegments().size(); i++)
        {
            Segment segment = (Segment) cell.getAllSegments().elementAt(i);

            if (segment.getParentSegment()!=null &&
                !segment.getParentSegment().getSection().equals(segment.getSection()))
            {
                Point3f startSeg = segment.getStartPointPosition();
                Point3f startParent = segment.getParentSegment().getStartPointPosition();
                Point3f endParent = segment.getParentSegment().getEndPointPosition();
                float fract = segment.getFractionAlongParent();
                if (startSeg.x != fract * endParent.x + (1 - fract) * startParent.x ||
                    startSeg.y != fract * endParent.y + (1 - fract) * startParent.y ||
                    startSeg.z != fract * endParent.z + (1 - fract) * startParent.z)
                {
                    return false;
                }
            }
        }
        for (int j = 0; j < cell.getAllSegments().size(); j++)
        {
            Segment segment = (Segment) cell.getAllSegments().elementAt(j);
            Segment parent = segment.getParentSegment();


            if (parent!=null)
            {
                int idOfParent = parent.getSegmentId();

                if (segment.getFractionAlongParent()!=0 &&
                    segment.getFractionAlongParent()!=1)
                {
                    float fractionToSplitAt = segment.getFractionAlongParent();
                    logger.logComment("----   Problem segment: "+ segment);
                    logger.logComment("Problem parent: "+ parent);
                    Vector allSegments = cell.getAllSegments();
                    Vector children = new Vector();
                    for (int jj = 0; jj < allSegments.size(); jj++)
                    {
                        Segment nextSeg = (Segment)allSegments.elementAt(jj);
                        if (nextSeg.getParentSegment()!=null && nextSeg.getParentSegment().equals(parent))
                            children.add(allSegments.elementAt(jj));
                    }
                    //logger.logComment("All my children: "+ children);



                    Point3f parentFirstSegmentEndPoint = new Point3f(segment.getStartPointPosition());
                    Point3f parentSecondSegmentStartPoint = new Point3f(segment.getStartPointPosition());
                    Point3f parentSecondSegmentEndPoint = new Point3f(parent.getEndPointPosition());

                    parent.setEndPointPositionX(parentFirstSegmentEndPoint.x);
                    parent.setEndPointPositionY(parentFirstSegmentEndPoint.y);
                    parent.setEndPointPositionZ(parentFirstSegmentEndPoint.z);

                    Segment secondSegment = new Segment();

                    secondSegment.setSegmentName(parent.getSegmentName()+"_splitFor_"+ segment.getSegmentName());
                    secondSegment.setSection(parent.getSection());
                    secondSegment.setParentSegment(parent);
                    secondSegment.setSegmentId(cell.getAllSegments().size());
                    secondSegment.setFractionAlongParent(1);
                    secondSegment.setFirstSectionSegment(false);
                    secondSegment.setShape(Segment.CYLINDRICAL_SHAPE);
                    secondSegment.setRadius(parent.getRadius());
                    secondSegment.setEndPointPositionX(parentSecondSegmentEndPoint.x);
                    secondSegment.setEndPointPositionY(parentSecondSegmentEndPoint.y);
                    secondSegment.setEndPointPositionZ(parentSecondSegmentEndPoint.z);

                    parent.setEndPointPositionX(parentFirstSegmentEndPoint.x);
                    parent.setEndPointPositionY(parentFirstSegmentEndPoint.y);
                    parent.setEndPointPositionZ(parentFirstSegmentEndPoint.z);

                    logger.logComment("FirstSegment: "+ parent);
                    logger.logComment("SecondSegment: "+ secondSegment);

                    //Point3f newStartParentSegmentEndPoint

                    for (int i = 0; i < children.size(); i++)
                    {
                        Segment nextChild = (Segment) children.elementAt(i);
                        logger.logComment("Fixing child: "+ nextChild);

                        if (nextChild.getFractionAlongParent()==0)
                        {
                            // do nothing
                        }
                        else if (nextChild.getFractionAlongParent()<fractionToSplitAt)
                        {
                            // change the fraction to that along the first segment
                            nextChild.setFractionAlongParent(nextChild.getFractionAlongParent()/fractionToSplitAt);
                        }
                        else if (nextChild.getFractionAlongParent()==fractionToSplitAt)
                        {
                            // ie the original child seg...
                            nextChild.setFractionAlongParent(1);
                        }
                        else if (nextChild.getFractionAlongParent()>fractionToSplitAt)
                        {
                            // attach it to the second seg...
                            nextChild.setFractionAlongParent(
                                (nextChild.getFractionAlongParent()-fractionToSplitAt)/
                                (1-fractionToSplitAt));
                            nextChild.setParentSegment(secondSegment);

                        }

                        logger.logComment("Fixed child: "+ nextChild);
                    }


                    // insert after parent...
                    insertSegment(cell, secondSegment, idOfParent+1);
                }
            }
        }

        logger.logComment("'''''''''''   Finished making cell: "+ cell + " Simply Connected");
        return true;
    }
*/

    /**
     * Inserts a segment into the cell so that it at the id specified
     * This is needed as parent segments must come before child segments for
     * rendering in 3D
     */
 /*   public static void insertSegment(Cell cell, Segment segment, int index)
    {
        logger.logComment("Inserting section: "
                          + segment.getSegmentName()
                          + " into cell at "
                          + index
                          + " where cell already has "
                          + cell.getAllSegments().size()
                          + " segments");
        Vector allSegments = cell.getAllSegments();

        allSegments.setSize(allSegments.size()+1);

        for (int i = allSegments.size()-1; i > index; i--)
        {
            Segment nextSeg = (Segment)allSegments.elementAt(i-1);
            logger.logComment("looking at segment "+i+": "+ nextSeg);
            allSegments.setElementAt(nextSeg, i);
            nextSeg.setSegmentId(nextSeg.getSegmentId()+1);
        }
        allSegments.setElementAt(segment, index);
        segment.setSegmentId(index);
    }
*/
    
    public static float getLengthFromRoot(Cell cell, SegmentLocation location)
    {
        Segment seg = cell.getSegmentWithId(location.getSegmentId());
        
        if (seg==null) return -1;
        
        float totLen = seg.getSegmentLength() * location.getFractAlong();
        
        Segment parent = null;
        
        while ((parent = seg.getParentSegment()) != null)
        {
            totLen = totLen + (parent.getSegmentLength() * seg.getFractionAlongParent());
            
            seg = parent;
        }
        logger.logComment("Length at: "+location+" on cell: "+ cell+" is "+totLen);
        return totLen;
            
    }
    
    
    public static float getMinLengthFromRoot(Cell cell, String group)
    { 
        float minLen = Float.MAX_VALUE;
        
        for(Segment seg: cell.getSegmentsInGroup(group))
        {
            float newLen = getLengthFromRoot(cell, new SegmentLocation(seg.getSegmentId(), 0));
            if (newLen==0) return 0;
            
            if (newLen<minLen) minLen = newLen;
        }
        
        return minLen;
            
    }
    
    public static float getMaxLengthFromRoot(Cell cell, String group)
    { 
        float maxLen = 0;
        
        for(Segment seg: cell.getSegmentsInGroup(group))
        {
            float newLen = getLengthFromRoot(cell, new SegmentLocation(seg.getSegmentId(), 1));
            if (newLen>maxLen) maxLen = newLen;
        }
        
        return maxLen;
            
    }
    
    
        
    /*
    public static LinkedList<Segment> getBranchInGroup(Cell cell, Segment seg, String group)
    {
        if (!seg.getSection().getGroups().contains(group)) return null;
        
        LinkedList<Segment> list = new LinkedList<Segment>();
        
        
    }*/

    
    /**
     * Gets the child segments of the specified segment in the cell all the way to the branch tip, i.e.
     * all segments whose parent, or whose parent's parent, etc. is the specified segment
     */
    public static Vector<Segment> getAllChildSegsToBranchEnd(Cell cell, Segment segment)
    {
        Vector<Segment> allSegments = cell.getAllSegments();
        Vector<Segment> allChildren = new Vector<Segment>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment nextSeg = allSegments.elementAt(i);
            Segment parentSeg = nextSeg.getParentSegment();
            boolean inBranch = false;
            
            while (!inBranch && parentSeg!=null)
            {
                if (parentSeg.equals(segment))
                {
                    inBranch = true;
                }                 
                
                parentSeg = parentSeg.getParentSegment();
            }
            if (inBranch) allChildren.add(nextSeg);
        }

        return allChildren;
    }
    
    
    

    /**
     * Gets the child segments of the specified segment in the cell, i.e. only segments which have the specified
     * segment as a parent. If onlySameSection is true, only returns the segments in the same section
     */
    public static Vector<Segment> getAllChildSegments(Cell cell, Segment segment, boolean onlySameSection)
    {
        Vector<Segment> allSegments = cell.getAllSegments();
        Vector<Segment> allChildren = new Vector<Segment>();

        logger.logComment("Get all kids called for: "+ segment);

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment nextSeg = allSegments.elementAt(i);
            if (nextSeg.getParentSegment()!=null && nextSeg.getParentSegment().equals(segment))
            {
                logger.logComment("A kid is: "+ nextSeg);
                if (onlySameSection)
                {
                    if(nextSeg.getSection().equals(segment.getSection()))
                    {
                        allChildren.add(nextSeg);
                    }
                }
                else
                {
                    allChildren.add(nextSeg);
                }
            }
        }

        return allChildren;
    }

    /**
     * Converts from fraction along segment (i.e. inside single segment of a section)
     * to the fraction along all of the segments in that section
     */
    public static float getFractionAlongSection(Cell cell, Segment segment, float fractionAlongSegment)
    {
        logger.logComment("Getting fract along: " + segment);
        
        if (cell.getAllSegments().size()==1) return fractionAlongSegment; // as only 1 seg & section

        LinkedList<Segment> allSectionSegments = cell.getAllSegmentsInSection(segment.getSection());

        if (allSectionSegments.size()==1) return fractionAlongSegment;// obviously...

        float totalLengthSection = getSectionLength(cell, segment.getSection());

        logger.logComment("totalLengthSection: "+totalLengthSection);

        Segment nextParentSegment = segment.getParentSegment();

        float distanceToStartOfSection = segment.getSegmentLength()* fractionAlongSegment;
        while (nextParentSegment!=null &&
               nextParentSegment.getSection().equals(segment.getSection()))
        {
            logger.logComment("Parent length: "+nextParentSegment.getSegmentLength());
            distanceToStartOfSection = distanceToStartOfSection + nextParentSegment.getSegmentLength();
            nextParentSegment = nextParentSegment.getParentSegment();
        }
        float fract = distanceToStartOfSection/totalLengthSection;
        if (fract >1) fract = 1; // as sometimes it will produce 1.00000001
        return (fract);
    }


    /**
     * Converts from fraction along section to the fraction the appropriate segment in that section
     * @return SynapticConnectionLocation as this encapsulates fract & seg id
     */
    public static SegmentLocation getFractionAlongSegment(Cell cell, Section section, float fractionAlongSection)
    {
        LinkedList<Segment> allSectionSegments = cell.getAllSegmentsInSection(section);



        if (allSectionSegments.size()==1)
            return new SegmentLocation(allSectionSegments.get(0).getSegmentId(), fractionAlongSection);// obviously...

        if (fractionAlongSection==0)
            return new SegmentLocation(allSectionSegments.get(0).getSegmentId(), fractionAlongSection);// obviously...

        float totalLengthSection = getSectionLength(cell, section);


        logger.logComment("totalLengthSection: "+totalLengthSection);
        float totalLengthToPass = totalLengthSection*fractionAlongSection;
        float totalLenPassed = 0;

        for (Segment seg : allSectionSegments)
        {
            float segLength = seg.getSegmentLength();
            if (totalLenPassed + segLength>=totalLengthToPass)
            {
                float lenAlongSeg = totalLengthToPass - totalLenPassed;
                float fractAlongSeg = lenAlongSeg/segLength;
                SegmentLocation loc = new SegmentLocation(seg.getSegmentId(), fractAlongSeg);
                logger.logComment("loc: " + loc);
                return loc ;
            }
            totalLenPassed = totalLenPassed + segLength;
        }
        return new SegmentLocation(allSectionSegments.getLast().getSegmentId(), 1);

    }






    /**
     * Function useful for packing. Gets extent of all (soma) sections
     * @param cell The cell in question
     * @param somaOnly true if only the extent of the soma sections are relevant
     */
    public static float getMaxXExtent(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float maxValue = -1*Float.MAX_VALUE;

        Vector somaSegments = null;
        if (somaOnly)
            somaSegments = cell.getOnlySomaSegments();
        else
            somaSegments = cell.getAllSegments();

        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment segment = (Segment) somaSegments.elementAt(i);

            //if (segment.getEndPointPositionY())
            if (segment.getEndPointPositionX() + segment.getRadius() > maxValue)
            {
                maxValue = segment.getEndPointPositionX() + segment.getRadius();
            }

            if (segment.getStartPointPosition().x + segment.getRadius() > maxValue)
            {
                maxValue = segment.getStartPointPosition().x + segment.getRadius();
            }

        }

        if (inclAxArbors)
        {
            Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

            for (AxonalConnRegion aa : aas)
            {
                maxValue = Math.max(aa.getRegion().getHighestXValue(), maxValue);
            }
        }

        return maxValue;
    }

    /**
     * Function useful for packing. Gets extent of all (soma) sections
     * @param cell The cell in question
     * @param somaOnly true if only the extent of the soma sections are relevant
     */
    public static float getMaxYExtent(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float maxValue = -1*Float.MAX_VALUE;

        Vector somaSegments = null;
        if (somaOnly)
            somaSegments = cell.getOnlySomaSegments();
        else
            somaSegments = cell.getAllSegments();

        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment segment = (Segment) somaSegments.elementAt(i);
            if (segment.getEndPointPositionY() + segment.getRadius() > maxValue)
            {
                maxValue = segment.getEndPointPositionY() + segment.getRadius();
            }
            if (segment.getStartPointPosition().y + segment.getRadius() > maxValue)
            {
                maxValue = segment.getStartPointPosition().y + segment.getRadius();
            }

        }

        if (inclAxArbors)
        {
            Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

            for (AxonalConnRegion aa : aas)
            {
                maxValue = Math.max(aa.getRegion().getHighestYValue(), maxValue);
            }
        }

        return maxValue;
    }


    /**
     * Function useful for packing. Gets extent of all (soma) sections
     * @param cell The cell in question
     * @param somaOnly true if only the extent of the soma sections are relevant
     */

    public static float getMaxZExtent(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float maxValue = -1*Float.MAX_VALUE;

        Vector somaSegments = null;
        if (somaOnly)
            somaSegments = cell.getOnlySomaSegments();
        else
            somaSegments = cell.getAllSegments();

        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment segment = (Segment) somaSegments.elementAt(i);
            if (segment.getEndPointPositionZ() + segment.getRadius() > maxValue)
            {
                maxValue = segment.getEndPointPositionZ() + segment.getRadius();
            }
            if (segment.getStartPointPosition().z + segment.getRadius() > maxValue)
            {
                maxValue = segment.getStartPointPosition().z + segment.getRadius();
            }

        }

        if (inclAxArbors)
        {
            Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

            for (AxonalConnRegion aa : aas)
            {
                maxValue = Math.max(aa.getRegion().getHighestZValue(), maxValue);
            }
        }

        return maxValue;
    }


    /**
     * Function useful for packing. Gets extent of all (soma) sections
     * @param cell The cell in question
     * @param somaOnly true if only the extent of the soma sections are relevant
     */

    public static float getMinXExtent(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float minValue = Float.MAX_VALUE;

        Vector somaSegments = null;

        if (somaOnly)
            somaSegments = cell.getOnlySomaSegments();
        else
            somaSegments = cell.getAllSegments();

        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment segment = (Segment) somaSegments.elementAt(i);
            if (segment.getEndPointPositionX() - segment.getRadius() < minValue)
            {
                minValue = segment.getEndPointPositionX() - segment.getRadius();
            }
            if (segment.getStartPointPosition().x - segment.getRadius() < minValue)
            {
                minValue = segment.getStartPointPosition().x - segment.getRadius();
            }
        }

        if (inclAxArbors)
        {
            Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

            for (AxonalConnRegion aa : aas)
            {
                minValue = Math.min(aa.getRegion().getLowestXValue(), minValue);
            }
        }
        return minValue;
    }



    /**
     * Function useful for packing. Gets extent of all (soma) sections
     * @param cell The cell in question
     * @param somaOnly true if only the extent of the soma sections are relevant
     */

    public static float getMinYExtent(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float minValue = Float.MAX_VALUE;

        Vector somaSegments = null;
                if (somaOnly)
                    somaSegments = cell.getOnlySomaSegments();
                else
                    somaSegments = cell.getAllSegments();

        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment segment = (Segment) somaSegments.elementAt(i);
            if (segment.getEndPointPositionY() - segment.getRadius() < minValue)
            {
                minValue = segment.getEndPointPositionY() - segment.getRadius();
            }
            if (segment.getStartPointPosition().y - segment.getRadius() < minValue)
            {
                minValue = segment.getStartPointPosition().y - segment.getRadius();
            }

        }

        if (inclAxArbors)
        {
            Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

            for (AxonalConnRegion aa : aas)
            {
                minValue = Math.min(aa.getRegion().getLowestYValue(), minValue);
            }
        }

        return minValue;
    }


    /**
     * Function useful for packing. Gets extent of all (soma) sections
     * @param cell The cell in question
     * @param somaOnly true if only the extent of the soma sections are relevant
     */

    public static float getMinZExtent(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float minValue = Float.MAX_VALUE;

        Vector somaSegments = null;
                if (somaOnly)
                    somaSegments = cell.getOnlySomaSegments();
                else
                    somaSegments = cell.getAllSegments();

        for (int i = 0; i < somaSegments.size(); i++)
        {
            Segment segment = (Segment) somaSegments.elementAt(i);
            if (segment.getEndPointPositionZ() - segment.getRadius() < minValue)
            {
                minValue = segment.getEndPointPositionZ() - segment.getRadius();
            }
            if (segment.getStartPointPosition().z - segment.getRadius() < minValue)
            {
                minValue = segment.getStartPointPosition().z - segment.getRadius();
            }

        }

        if (inclAxArbors)
        {
            Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

            for (AxonalConnRegion aa : aas)
            {
                minValue = Math.min(aa.getRegion().getLowestZValue(), minValue);
            }
        }

        return minValue;
    }









    /**
     * Gets the length of the cell in the x direction...
     */
    public static float getXExtentOfCell(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float minXValue = getMinXExtent(cell, somaOnly, inclAxArbors);
        float maxXValue = getMaxXExtent(cell, somaOnly, inclAxArbors);

        return maxXValue - minXValue;
    }

    /**
     * Gets the length of the cell in the y direction...
     */
    public static float getYExtentOfCell(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float minYValue = getMinYExtent(cell, somaOnly, inclAxArbors);
        float maxYValue = getMaxYExtent(cell, somaOnly, inclAxArbors);

        return maxYValue - minYValue;
    }

    /**
     * Gets the length of the cell in the z direction...
     */
    public static float getZExtentOfCell(Cell cell, boolean somaOnly, boolean inclAxArbors)
    {
        float minZValue = getMinZExtent(cell, somaOnly, inclAxArbors);
        float maxZValue = getMaxZExtent(cell, somaOnly, inclAxArbors);

        return maxZValue - minZValue;
    }


    /**
     * This is just to assist in updating from an old method of storing the channel mechanisms...
     */
    public static void updateChannelMechanisms(Cell cell, Project project)
    {
        ArrayList allChanMechs = cell.getAllUniformChanMechs(true);
        boolean usingOldMethod = false;

        for (int i = 0; i < allChanMechs.size(); i++)
        {
            Object nextMech = allChanMechs.get(i);

            if (nextMech instanceof String)
            {
                logger.logComment("----------   Found old channel mechanism: "+ nextMech);
                usingOldMethod = true;
            }
        }
        if (usingOldMethod)
        {
            logger.logComment("Updating channel mechanism storage...");

            Hashtable chanMechs = cell.getChanMechsVsGroups();

            logger.logComment("Old ChanMechsVsGroups: "+ chanMechs);
            Hashtable<ChannelMechanism, Vector<String>> newChanMechs = new Hashtable<ChannelMechanism, Vector<String>>();

            Enumeration enumeration = chanMechs.keys();
            while (enumeration.hasMoreElements())
            {
                String oldName = (String)enumeration.nextElement();
                Vector<String> groups = (Vector)chanMechs.get(oldName);
                float defaultVal = -1;
                try
                {
                    if (project.cellMechanismInfo.getCellMechanism(oldName)!=null)
                    {
                        defaultVal
                            = ((AbstractedCellMechanism)project.cellMechanismInfo.getCellMechanism(oldName))
                                  .getParameter(DistMembraneMechanism.COND_DENSITY);
                    }

                }
                catch (CellMechanismException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Error updating from old Channel Mechanism storing method to new method...", ex, null);

                }
                ChannelMechanism newMech = new ChannelMechanism(oldName, defaultVal);
                newChanMechs.put(newMech, groups);
            }
            logger.logComment("Created new ChanMechsVsGroups: "+ newChanMechs);
            cell.setChanMechsVsGroups(newChanMechs);
        }


    }

    public static String printDetails(Cell cell, Project project)
    {
        return printDetails(cell, project,false, true, false);
    }


    public static String printDetails(Cell cell, Project project, boolean html)
    {
        return printDetails(cell, project,html, true, false);
    }

    public static String printDetails(Cell cell, Project project, boolean html,
    		                          boolean longFormat,
    		                          boolean projHtml)
    {

        return printDetails(cell, project,html, longFormat,  projHtml, false);
    }


    public static String printDetails(Cell cell,
    		                          Project project,
    		                          boolean html,
    		                          boolean longFormat,
    		                          boolean projHtml,
                                      boolean safeSymbol)
    {

        return printDetails(cell, project,html, longFormat,  projHtml, false, safeSymbol);
    }

    public static String printDetails(Cell cell,
    		                          Project project,
    		                          boolean html,
    		                          boolean longFormat,
    		                          boolean projHtml,
    		                          boolean expandHtml,
                                      boolean safeSymbol)
    {

        logger.logComment("Printing cell details...");
        if (cell==null) return "**** Error: cell is null ****";
        
        StringBuilder sb = new StringBuilder();


        String indent = "    ";
        if (html) indent = "&nbsp;&nbsp;&nbsp;&nbsp;";

        if (!html) sb.append("---------------------------------------------------------------------------------" +GeneralUtils.getEndLine(html));
        sb.append("    Cell name                     : " + GeneralUtils.getTabbedString(cell.getInstanceName(), "b", html)  +GeneralUtils.getEndLine(html)+GeneralUtils.getEndLine(html));
        //sb.append("    Base class                    : " + cellName.substring(indexLastDot + 1)  +GeneralUtils.getEndLine(html));
        String desc = cell.getCellDescription();
        if (html) desc = GeneralUtils.replaceAllTokens(desc, "\n", "<br>\n");

        sb.append("    " + GeneralUtils.getTabbedString(desc, "b", html) + GeneralUtils.getEndLine(html));

        sb.append("  "+GeneralUtils.getEndLine(html));
        
        boolean useFullSymbol = !safeSymbol && (html || projHtml);

        //logger.logComment("safeSymbol..."+safeSymbol, true);
        //logger.logComment("useFullSymbol..."+useFullSymbol, true);
        
        String capSymb = useFullSymbol ? UnitConverter.specificCapacitanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() :
        	UnitConverter.specificCapacitanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol();

        String initPot = null;
        if (cell.getInitialPotential()!=null)
        {
            initPot = cell.getInitialPotential().toShortString() +
                  " "+ UnitConverter.voltageUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
        }
        sb.append("    Initial potential             : "
                  + GeneralUtils.getTabbedString(initPot, "b", html) +GeneralUtils.getEndLine(html));


        ArrayList<Float> specCaps = cell.getDefinedSpecCaps();
        for (Float sc: specCaps)
        {
            Vector groups = cell.getGroupsWithSpecCap(sc);

            sb.append("    Specific Capacitance          : " +
                      GeneralUtils.getTabbedString(sc + " " +capSymb
                                                   ,
                                                   "b", html) +
                      " on " + GeneralUtils.getTabbedString(groups.toString(), "b", html) +
                      GeneralUtils.getEndLine(html));

        }

        String sarSymb = useFullSymbol ? UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() :
        	UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol();


        ArrayList<Float> specAxReses = cell.getDefinedSpecAxResistances();
        for (Float sar: specAxReses)
        {
            Vector groups = cell.getGroupsWithSpecAxRes(sar);

            sb.append("    Specific Axial Resistance     : " +
                      GeneralUtils.getTabbedString(sar + " " +sarSymb,
                                                   "b", html) +
                      " on " + GeneralUtils.getTabbedString(groups.toString(), "b", html) +
                      GeneralUtils.getEndLine(html));

        }


        String condDensSymb = useFullSymbol ? UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() :
        	UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol();


        sb.append("  "+GeneralUtils.getEndLine(html));

        ArrayList<ChannelMechanism> allChanMechs = cell.getAllUniformChanMechs(true);
        allChanMechs = (ArrayList<ChannelMechanism>)GeneralUtils.reorderAlphabetically(allChanMechs, true);
        for (int i = 0; i < allChanMechs.size(); i++)
        {
            ChannelMechanism chanMech = allChanMechs.get(i);
            Vector<String> groups = cell.getGroupsWithChanMech(chanMech);
            
            String moreInfo = chanMech.getDensity() + " "+ condDensSymb+chanMech.getExtraParamsDesc();
            
            
            String descCm = chanMech.getName() + " ("+moreInfo +")";
            String grpInfo = GeneralUtils.getTabbedString(groups.toString(), "b", html);
            
            if (html)
            {
            	if (expandHtml)
            	{
            		descCm = "<a href=\"../"+Expand.getCellMechPage(chanMech.getName())+"\">"+chanMech.getName() + "</a> ("+ moreInfo+")";
            	}
                else if (projHtml)
            	{
                    descCm = ClickProjectHelper.getCellMechLink(chanMech.getName())+ " ("+ moreInfo+")";
                    if (false) // nearly there...
                    {
                        String grpString = new String();
                        for(int j=0;j<groups.size();j++)
                        {
                            grpString = grpString + ClickProjectHelper.getCellSectionGroupLink(cell.getInstanceName(), groups.get(j));
                            if (j<groups.size()-1)
                                grpString = grpString + " ,";
                        }
                        grpInfo = "<b>["+grpString+"]</b>";
                    }
            	}
            	else
            	{
                	descCm = chanMech.getName() + " ("+ moreInfo+")";
            	}
            }
            
            String type = "Channel: ";
            if (project!=null)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(chanMech.getName());
                if (cm!=null)
                {
                    type = cm.getMechanismType();
                }
                else
                {
                    type = "-- Unknown type! --";
                }
            }

            sb.append("    "+type+": "+GeneralUtils.getTabbedString(descCm, "b", html)
                      +" is present on groups: "+grpInfo+GeneralUtils.getEndLine(html));
        }
        if (allChanMechs.size()>0) sb.append("  "+GeneralUtils.getEndLine(html));
        
        
        
        Hashtable<VariableMechanism, ParameterisedGroup> varMechsVsParaGroups = cell.getVarMechsVsParaGroups();

        Enumeration<VariableMechanism> varMechs = varMechsVsParaGroups.keys();

        while (varMechs.hasMoreElements())
        {
            VariableMechanism vm = varMechs.nextElement();
            ParameterisedGroup pg = varMechsVsParaGroups.get(vm);
            String mechString = vm.getName();
            if (projHtml)
            {
                mechString = ClickProjectHelper.getCellMechLink(mechString);
            }


            sb.append("    Variable Mechanism: "+GeneralUtils.getTabbedString(mechString, "b", html)
                +" with param: "+GeneralUtils.getTabbedString(vm.getParam().toString(), "b", html)
                      +" is present on: "+GeneralUtils.getTabbedString(pg.getName(), "b", html)+GeneralUtils.getEndLine(html));

        }
        if (varMechsVsParaGroups.size()>0) sb.append("  "+GeneralUtils.getEndLine(html));


        Enumeration<IonProperties> ips = cell.getIonPropertiesVsGroups().keys();

        //logger.logComment("ionPropsVsGroups: " + cell.getIonPropertiesVsGroups(), true);

        while (ips.hasMoreElements())
        {
            IonProperties ip = ips.nextElement();
            Vector<String> groups = cell.getIonPropertiesVsGroups().get(ip);

            String grpInfo = GeneralUtils.getTabbedString(groups.toString(), "b", html);

            sb.append("    "+ip.toString(html)
                +" is present on: "+grpInfo+GeneralUtils.getEndLine(html));

        }
        if (cell.getIonPropertiesVsGroups().size()>0) sb.append("  "+GeneralUtils.getEndLine(html));


        Enumeration<Species> species =  cell.getSpeciesVsGroups().keys();

        while(species.hasMoreElements())
        {
            Species sp = species.nextElement();

            Vector<String> grps =  cell.getSpeciesVsGroups().get(sp);

            sb.append("    Species: "+GeneralUtils.getTabbedString(sp.toString(), "b", html)
                      +" is present on: "+GeneralUtils.getTabbedString(grps.toString(), "b", html)+GeneralUtils.getEndLine(html));
        }
        if (cell.getSpeciesVsGroups().size()>0) sb.append("  "+GeneralUtils.getEndLine(html));

        ArrayList<String> allSynapses = cell.getAllAllowedSynapseTypes();
        allSynapses = (ArrayList<String>)GeneralUtils.reorderAlphabetically(allSynapses, true);
        
        for (int i = 0; i < allSynapses.size(); i++)
        {
            String syn = allSynapses.get(i);
            Vector groups = cell.getGroupsWithSynapse(syn);
            String synString = syn.toString();

            if (expandHtml)
            {
                synString = "<a href=\"../"+Expand.getCellMechPage(syn)+"\">"+syn + "</a>";
            }
            else if(projHtml)
            {
                synString = ClickProjectHelper.getCellMechLink(syn);
            }
            sb.append("    Synapse: "+GeneralUtils.getTabbedString(synString, "b", html)
                      +" is allowed on: "+GeneralUtils.getTabbedString(groups.toString(), "b", html)+GeneralUtils.getEndLine(html));
        }
        if (allSynapses.size()>0) sb.append("  "+GeneralUtils.getEndLine(html));

        ArrayList<ApPropSpeed> allApPropVelocities = cell.getAllApPropSpeeds();
        for (int i = 0; i < allApPropVelocities.size(); i++)
        {
            ApPropSpeed appv = allApPropVelocities.get(i);
            Vector groups = cell.getGroupsWithApPropSpeed(appv);
            sb.append("    Action potential propagation speed of : "+GeneralUtils.getTabbedString(appv.getSpeed() +" "
            +UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+" "
            +UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol(), "b", html)+" is present on segments in: "+GeneralUtils.getTabbedString(groups.toString(), "b", html)+GeneralUtils.getEndLine(html));
        }
        if (allApPropVelocities.size()>0) sb.append("  "+GeneralUtils.getEndLine(html));


        Vector<AxonalConnRegion> aas = cell.getAxonalArbours();

        for (AxonalConnRegion aa: aas)
        {
            sb.append("    "+aa.getInfo(html)+GeneralUtils.getEndLine(html));
        }
        if (aas.size()>0) sb.append("  "+GeneralUtils.getEndLine(html));

        ArrayList<Section> sections = cell.getAllSections();
        int totIntDivs = 0;
        for (Section sec: sections)
            totIntDivs = totIntDivs + sec.getNumberInternalDivisions();


        sb.append("    Number of segments            : " 
                + GeneralUtils.getTabbedString(cell.getAllSegments().size()+"", "b", html)
                  + " (somatic: "+cell.getOnlySomaSegments().size()
                  + ", dendritic: "+cell.getOnlyDendriticSegments().size()
                  + ", axonal: "+cell.getOnlyAxonalSegments().size() +")"
                  + GeneralUtils.getEndLine(html));
        sb.append("    Number of sections            : " + GeneralUtils.getTabbedString(sections.size()+"", "b", html)
                  + GeneralUtils.getEndLine(html));
        sb.append("    Total internal divisions      : " + GeneralUtils.getTabbedString(totIntDivs+"", "b", html)
                  + GeneralUtils.getEndLine(html)+ GeneralUtils.getEndLine(html));
        
        for(ParameterisedGroup pg: cell.getParameterisedGroups())
        {
            sb.append("    ParameterisedGroup: "+GeneralUtils.getBold(pg.getName(), html) 
                + " on: "+GeneralUtils.getBold(pg.getGroup(), html)+", "
                + GeneralUtils.getBold(pg.getMetric()+", ("+pg.getProximalPref()+", "+pg.getDistalPref()+")", html)+", var: "
                + GeneralUtils.getBold(pg.getVariable()+"", html)+ GeneralUtils.getEndLine(html));
        }

        float totalSurfaceArea = 0;
        float totalLength = 0;
        float totalAxResistance = 0;

        //float specAxResVal = cell.getSpecAxRes().getNominalNumber();

        for (int secNum = 0; secNum < sections.size(); secNum++)
        {
                Section section = sections.get(secNum);

                float specAxRes = cell.getSpecAxResForSection(section);

                if (longFormat) sb.append(" "+indent+GeneralUtils.getEndLine(html));
                if (longFormat) sb.append(" "+indent+ section.toHTMLString(html) + GeneralUtils.getEndLine(html));

                LinkedList<Segment> segments = cell.getAllSegmentsInSection(section);

                if (longFormat) sb.append(" "+indent + "Number of segments: " + segments.size() + GeneralUtils.getEndLine(html));
                for (int i = 0; i < segments.size(); i++)
                {
                    try
                    {
                        Segment segment = segments.get(i);
                        if (longFormat) sb.append(" "+indent+indent + i + ":  " + segment.toHTMLString(html) + GeneralUtils.getEndLine(html));

                        totalSurfaceArea = totalSurfaceArea + segment.getSegmentSurfaceArea();
                        //System.out.println("surf "+segment.getSegmentName()+": "+ segment.getSegmentSurfaceArea()+", tot: "+totalSurfaceArea);
                        totalLength = totalLength + segment.getSegmentLength();
                        totalAxResistance = totalAxResistance + getTotalAxialResistance(segment, specAxRes);
                    }
                    catch (NullPointerException e)
                    {
                        logger.logError("", e);
                        return "Problem with segment: " + i + ", info so far: "+GeneralUtils.getEndLine(html) + sb.toString() + GeneralUtils.getEndLine( html);
                    }
                }

        }

        sb.append(" " + GeneralUtils.getEndLine(html)+ GeneralUtils.getEndLine(html));
        

        String areaSymb = useFullSymbol ? UnitConverter.areaUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() :
        	UnitConverter.areaUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol();
        
        String lenSymb = useFullSymbol ? UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() :
        	UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol();


        sb.append("Total surface area of all segments: " + GeneralUtils.getTabbedString(totalSurfaceArea + " "+areaSymb, "b", html)
                  + GeneralUtils.getEndLine(html));

        sb.append("Total length of all segments: " + GeneralUtils.getTabbedString(totalLength + " "+lenSymb, "b", html)
                  + GeneralUtils.getEndLine(html));

        if (longFormat) sb.append("Sum of axial resistance in all segments: " + GeneralUtils.getTabbedString(totalAxResistance + " "
                                  + UnitConverter.resistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol(), "b", html)
                                  + GeneralUtils.getEndLine(html));



        sb.append("   "+GeneralUtils.getEndLine(html));

        if (longFormat)
        {
            sb.append("Length in X direction: "
                      + GeneralUtils.getTabbedString(CellTopologyHelper.getXExtentOfCell(cell, false, false) + " " +lenSymb, "b", html)
                      + GeneralUtils.getEndLine(html));

            sb.append("Length in Y direction: "
                      + GeneralUtils.getTabbedString(CellTopologyHelper.getYExtentOfCell(cell, false, false) + " " +lenSymb, "b", html)
                      + GeneralUtils.getEndLine(html));

            sb.append("Length in Z direction: "
                      + GeneralUtils.getTabbedString(CellTopologyHelper.getZExtentOfCell(cell, false, false) + " " +lenSymb, "b", html)
                      + GeneralUtils.getEndLine(html));

            sb.append("   " + GeneralUtils.getEndLine(html));
        }

        sb.append("   "+indent+"Cell Validity:"+GeneralUtils.getEndLine(html));

        ValidityStatus morphValid = getValidityStatus(cell);

        String errorInfo = morphValid.toString();
        if (!errorInfo.endsWith("\n")) errorInfo = errorInfo +"\n";

        if (html)
        {
            errorInfo = GeneralUtils.replaceAllTokens(errorInfo, "\n", "<br>");
        }

        sb.append(""+  GeneralUtils.getTabbedString(errorInfo,
                                                        "font color=\""+morphValid.getColour()+"\"",
                                                        html));


        ValidityStatus bioValid = getBiophysicalValidityStatus(cell, project);

        if (project!=null)
        {
            String bioErrorInfo = GeneralUtils.getTabbedString(bioValid.getMessage(), "font color=\""+bioValid.getColour()+"\"", html);

            if (html)
            {
                bioErrorInfo = GeneralUtils.replaceAllTokens(bioErrorInfo, "\n", "<br>");
            }
            sb.append(""  + bioErrorInfo + GeneralUtils.getEndLine(html));
        }



        sb.append(" "+GeneralUtils.getEndLine(html));

        if (!html) sb.append("---------------------------------------------------------------------------------"+GeneralUtils.getEndLine(html));
        return sb.toString();
    }

    /**
     * Gets the connection point on the first parent segment (down the tree) of the specified segment, which
     * is explicitly modelled, i.e. doesn't have a ApPropSpeed specified
     */
    public static SegmentLocation getConnLocOnExpModParent(Cell cell, Segment segment)
    {
        Section sec = segment.getSection();

        while (cell.getApPropSpeedForSection(sec)!=null)
        {
            String propSpeedSeg = sec.getSectionName();

            while (segment.getSection().getSectionName().equals(propSpeedSeg))
            {
                segment = segment.getParentSegment();
            }
            sec = segment.getSection();
        }
        SegmentLocation synConnLoc
            = new SegmentLocation(segment.getSegmentId(), segment.getFractionAlongParent());

        return synConnLoc;

    }


    public static float getVolume(Cell cell, boolean somaOnly)
    {
        Vector<Segment> segs = null;
        if (somaOnly)
            segs = cell.getOnlySomaSegments();
        else
            segs = cell.getAllSegments();

        float totalVolume = 0;
        for (Segment nextSegment: segs)
        {
            totalVolume = totalVolume + nextSegment.getSegmentVolume();
        }
        return totalVolume;
    }


    /**
     * Gets the length from a point the specified fraction along the section, to the end of the
     * first explicitly medelled section.
     */
    public static float getDistToFirstExpModParent(Cell cell, Segment segment, float fractAlongSegment)
    {
        Section sec = segment.getSection();

        if (cell.getApPropSpeedForSection(sec)==null)
        {
            return 0;
        }
        float totalDistance = 0;

        float sectionLength = CellTopologyHelper.getSectionLength(cell, sec);
        float fractAlongSec = getFractionAlongSection(cell, segment, fractAlongSegment);

        totalDistance = sectionLength * fractAlongSec;

        Segment nextSeg = cell.getAllSegmentsInSection(sec).getFirst().getParentSegment();
        Section nextSecDown = nextSeg.getSection();
        logger.logComment("totalDistance: "+totalDistance);

        while (cell.getApPropSpeedForSection(nextSecDown)!=null)
        {
                logger.logComment("nextSecDown: "+nextSecDown);
            String propSpeedSeg = nextSecDown.getSectionName();

            while (nextSeg.getSection().getSectionName().equals(propSpeedSeg))
            {
                logger.logComment("nextSeg: "+nextSeg);
                totalDistance = totalDistance + nextSeg.getSegmentLength();
                nextSeg = nextSeg.getParentSegment();
            }
            nextSecDown = nextSeg.getSection();
        }

        return totalDistance;

    }


    /**
     * Gets the time of travel of an AP from a point the specified fraction along the section, to the end of the
     * first explicitly modelled section.
     */
    public static float getTimeToFirstExpModParent(Cell cell, Segment segment, float fractAlongSegment)
    {
        Section sec = segment.getSection();
        //Vector dodgySectionNames = new Vector();

        ApPropSpeed apps = cell.getApPropSpeedForSection(sec);

        if (apps==null)
        {
            return 0;
        }
        float totalTime = 0;

        float sectionLength = CellTopologyHelper.getSectionLength(cell, sec);
        float fractAlongSec = getFractionAlongSection(cell, segment, fractAlongSegment);

        totalTime = sectionLength * fractAlongSec / apps.getSpeed();

        Segment nextSeg = cell.getAllSegmentsInSection(sec).getFirst().getParentSegment();
        Section nextSecDown = nextSeg.getSection();

        logger.logComment("totalTime: "+totalTime);

        while (cell.getApPropSpeedForSection(nextSecDown)!=null)
        {
            logger.logComment("nextSecDown: "+nextSecDown);
            String propSpeedSeg = nextSecDown.getSectionName();
            float speed = cell.getApPropSpeedForSection(nextSecDown).getSpeed();

            while (nextSeg.getSection().getSectionName().equals(propSpeedSeg))
            {
                logger.logComment("nextSeg: "+nextSeg);
                totalTime = totalTime + (nextSeg.getSegmentLength()/speed);
                nextSeg = nextSeg.getParentSegment();
            }
            nextSecDown = nextSeg.getSection();
        }

        return totalTime;

    }



    public static String printShortDetails(Cell cell)
    {
        return printDetails(cell, null, false, false, false);
        /*
        StringBuffer sb = new StringBuffer();

        String cellName = cell.getClass().getName();
        int indexLastDot = cellName.lastIndexOf('.');
        sb.append("---------------------------------------------------------------------------------\n");
        sb.append("-  Cell name                     : " + cell.getInstanceName() + "\n");
        sb.append("-  Base class                    : " + cellName.substring(indexLastDot + 1) + "\n");
        sb.append("-  Description                   : " + cell.getCellDescription() + "\n");
        sb.append("-  Initial potential             : " + cell.getInitialPotential().toShortString() + "\n");
        sb.append("-  Specific Axial Resistance     : " + cell.getSpecAxRes().toShortString() + "\n");
        sb.append("-  Specific Capacitance          : " + cell.getSpecCapacitance().toShortString() + "\n");


        Vector sections = cell.getAllSections();

        sb.append("-  Number of sections            : " + sections.size() + "\n");

        sb.append("-  Number of segments            : " + cell.getAllSegments().size() + "\n");
        sb.append("- \n");

        Vector somaSegments = cell.getOnlySomaSegments();
        sb.append("-  Number of soma segments       : " + somaSegments.size());
        if (somaSegments.size()==1 && cell.getFirstSomaSegment().getSegmentShape()==Segment.SPHERICAL_SHAPE)
            sb.append("   (Spherical with radius "+cell.getFirstSomaSegment().getRadius()+")");
        sb.append("\n");

        Vector axonalSegments = cell.getOnlyAxonalSegments();
        sb.append("-  Number of axonal segments     : " + axonalSegments.size() + "\n");

        Vector dendriticSegments = cell.getOnlyDendriticSegments();
        sb.append("-  Number of dendritic segments  : " + dendriticSegments.size() + "\n");
        sb.append("- \n");
        sb.append("-      "+ getValidityStatus(cell) + "\n");
        //sb.append("-      "+ getBiophysicalValidityStatus(cell) + "\n");


        sb.append("---------------------------------------------------------------------------------\n");
        return sb.toString();
*/
    }


    /**
     * Returns true if the cell is Simply connected, i.e. if each segment is connected
     * to the start or the end of the parent, i.e. segment.getFractionAlongParent() = 0 or 1
     */
    public static boolean checkSimplyConnected(Cell cell)
    {
        for (int i = 0; i < cell.getAllSegments().size(); i++)
        {
            Segment segment = cell.getAllSegments().elementAt(i);

            if (segment.getParentSegment() != null)
            {
                if (segment.getFractionAlongParent() != 1 &&
                    segment.getFractionAlongParent() != 0)
                {
                    // It's ok if the parent is spherical and the segment is connected at 0.5
                    // That convention is used so when generating NEURON code a cylinder is
                    // created and the children are connected at the middle
                    if (! (segment.getParentSegment().getSegmentShape() == Segment.SPHERICAL_SHAPE &&
                           segment.getFractionAlongParent() == 0.5f))
                        return false;
                }
            }
        }
        return true;
    }

    /*
     * Can be used to change the numberInternalDivisions in each Section of the cell to give a 
     * maximum electrotonic length of each internal division of targetMaxElectLen. Used by the remesh
     * function of the Segment Selector.
     */
    public static String recompartmentaliseCell(Cell cell, float targetMaxElectLen, Project project, boolean longReport, boolean justTest)
    {
        StringBuilder report = new StringBuilder();

        ArrayList<Section> secs = cell.getAllSections();


        int countNew = 0;
        int countOld = 0;

        for(Section nextSec: secs)
        {
            try
            {
                LinkedList<Segment> segs = cell.getAllSegmentsInSection(nextSec);

                float totalElecLen = 0;

                float specMembRes = CellTopologyHelper.getSpecMembResistance(cell, project, nextSec);

                float specAxRes = cell.getSpecAxResForSection(nextSec);

                boolean isSpherical = true;

                for (int i = 0; i < segs.size(); i++)
                {
                    Segment nextSeg = segs.get(i);

                    isSpherical = isSpherical && nextSeg.isSpherical();

                    totalElecLen = totalElecLen + CellTopologyHelper.getElectrotonicLength(nextSeg, specMembRes, specAxRes);
                }

                String sphercalNote = isSpherical?" (spherical section)":"";

                report.append(nextSec.getSectionName()+" has a total electrotonic length of: "+ totalElecLen+sphercalNote+"\n");

                int oldNseg = nextSec.getNumberInternalDivisions();
                countOld += oldNseg;

                int bestNseg = Math.max(1, (int)Math.ceil(totalElecLen/targetMaxElectLen));

                String changed = "";

                if(!justTest)
                {
                    nextSec.setNumberInternalDivisions(bestNseg);
                }
                else
                {
                    changed = " (not yet changed!)";
                }

                report.append("Old num internal divs: "+oldNseg+", new number of internal divs: "+bestNseg+changed+"\n\n");
                countNew += bestNseg;

            }
            catch (CellMechanismException ex)
            {
                report.append("Error: Could not determine the electrotonic length of section: " + nextSec.getSectionName() +
                                   "\n");
            }

        }
        String summary = "Total number old divs in cell: "+countOld+", total new divs: "+ countNew;

        if (longReport)
            return summary+"\n\n"+report.toString();
        else
            return summary;

    }

    public static String recompartmentaliseCell(Cell cell, float targetMaxElectLen, Project project)
    {
        return recompartmentaliseCell(cell, targetMaxElectLen, project, false, false);
    }


    public static ValidityStatus getBiophysicalValidityStatus(Cell cell,
                                                              Project project)
    {
        return getBiophysicalValidityStatus(cell, project, true);
    }
    /**
     * Returns a short string detailing the validity of the cell from a biophysical point of view
     *
     * NOTE: Check in Help->Glossary->Cell Validity for the conditions under which a cell is "valid"
     *
     */
    public static ValidityStatus getBiophysicalValidityStatus(Cell cell, 
                                                              Project project,
                                                              boolean checkBioBounds)
    {
        if (project == null)
            return ValidityStatus.getErrorStatus("Error: null project. Cannot check biophysical properties outside of context of a particular project");

        StringBuilder errorReport = new StringBuilder();
        StringBuilder warningReport = new StringBuilder();

        float minPhysiologicalMembPot = -100;  // mV, limit to warn about
        float maxPhysiologicalMembPot = 100;   // mV, limit to warn about

        float minPhysiologicalSpecCap = 2e-10f;   // μF μm-2, limit to warn about
        float maxPhysiologicalSpecCap = 2.5e-8f;  // μF μm-2, limit to warn about

        float minPhysiologicalSpecAxRes = 100f;  //  kohm μm , limit to warn about
        float maxPhysiologicalSpecAxRes = 3000f; //  kohm μm , limit to warn about


        float minPhysiologicalPassCondDens = 1e-10f; //  mS um-2 , limit to warn about
        float maxPhysiologicalPassCondDens = 4e-7f;  //  mS um-2 , limit to warn about


        float maxPhysiologicalCondDens = 1e-3f;  //  mS um-2 , limit to warn about
        


        if (checkBioBounds)
        {
            if (cell.getInitialPotential().getMinPossible()<minPhysiologicalMembPot)
            {
                warningReport.append("Warning: initial potential ("+cell.getInitialPotential()+") exceeds lower bound of physiological membrane potentials: "
                        +minPhysiologicalMembPot+" mV");
            }
            if (cell.getInitialPotential().getMaxPossible()>maxPhysiologicalMembPot)
            {
                warningReport.append("Warning: initial potential ("+cell.getInitialPotential()+") exceeds upper bound of physiological membrane potentials: "
                        +maxPhysiologicalMembPot+" mV");
            }

            Hashtable<Float, Vector<String>> specCapVsGroups = cell.getSpecCapVsGroups();

            for(Float spCap: specCapVsGroups.keySet())
            {
                if (spCap<minPhysiologicalSpecCap)
                {
                    warningReport.append("Warning: specific capacitance: "+spCap+" uF um-2 on group(s) "+specCapVsGroups.get(spCap)
                            +" exceeds lower bound of physiological specific capacitance: "
                        +minPhysiologicalSpecCap+" uF um-2");
                }
                if (spCap>maxPhysiologicalSpecCap)
                {
                    warningReport.append("Warning: specific capacitance: "+spCap+" uF um-2 on group(s) "+specCapVsGroups.get(spCap)
                            +" exceeds upper bound of physiological specific capacitance: "
                        +maxPhysiologicalSpecCap+" uF um-2");
                }
            }

            Hashtable<Float, Vector<String>> specAxResVsGroups = cell.getSpecAxResVsGroups();

            for(Float spAxRes: specAxResVsGroups.keySet())
            {
                if (spAxRes<minPhysiologicalSpecAxRes)
                {
                    warningReport.append("Warning: specific axial resistance: "+spAxRes+" kohm μm on group(s) "+specAxResVsGroups.get(spAxRes)
                            +" exceeds lower bound of physiological specific axial resistance: "
                        +minPhysiologicalSpecAxRes+" kohm μm");
                }
                if (spAxRes>maxPhysiologicalSpecAxRes)
                {
                    warningReport.append("Warning: specific axial resistance: "+spAxRes+" kohm μm on group(s) "+specAxResVsGroups.get(spAxRes)
                            +" exceeds upper bound of physiological specific axial resistance: "
                        +maxPhysiologicalSpecAxRes+" kohm μm");
                }
            }
        }


        if (cell.getAllChanMechNames(true).isEmpty())
        {
            errorReport.append("Error: Cell does not contain any membrane mechanisms");
        }
        else
        {
            ArrayList cellMechNames = project.cellMechanismInfo.getAllCellMechanismNames();
            Vector<String> missingCellMechs = new Vector<String>();
            
            ArrayList<Section> allSections = cell.getAllSections();

            ArrayList<String> passChanNames = getPassiveChannels(cell, project);

            logger.logComment("Passive chans: "+ passChanNames);

            Hashtable<ChannelMechanism, Vector<String>> chanMechsVsGroups = cell.getChanMechsVsGroups();

            for(ChannelMechanism cm: chanMechsVsGroups.keySet())
            {
                if (passChanNames.contains(cm.getName()))
                {
                    if (cm.getDensity()>0 /* as some chan mechs use dens = -1 to set extra channel mech params */
                            && cm.getDensity()<minPhysiologicalPassCondDens)
                    {
                        warningReport.append("Warning: channel mechanism: "+cm.toNiceString()+" on group(s) "+chanMechsVsGroups.get(cm)
                            +" exceeds lower bound of physiological PASSIVE channel conductance density: "
                        +minPhysiologicalPassCondDens+" mS um-2");
                    }
                    if (cm.getDensity()>maxPhysiologicalPassCondDens)
                    {
                        warningReport.append("Warning: channel mechanism: "+cm.toNiceString()+" on group(s) "+chanMechsVsGroups.get(cm)
                            +" exceeds upper bound of physiological PASSIVE channel conductance density: "
                        +maxPhysiologicalPassCondDens+" mS um-2");
                    }
                }
                else
                {
                    if (cm.getDensity()>maxPhysiologicalCondDens)
                    {
                        warningReport.append("Warning: channel mechanism: "+cm.toNiceString()+" on group(s) "+chanMechsVsGroups.get(cm)
                            +" exceeds upper bound of physiological channel conductance density: "
                        +maxPhysiologicalCondDens+" mS um-2");
                    }
                }
            }

            int numNoPassError = 0;
            int numManyPassWarn = 0;

            for (int j = 0; j < allSections.size(); j++)
            {
                Section nextSec = allSections.get(j);
                ArrayList<ChannelMechanism> fixedMechs = cell.getUniformChanMechsForSec(nextSec);
                ArrayList<VariableMechanism> varMechs = cell.getVarChanMechsForSection(nextSec);
                
                ApPropSpeed appv = cell.getApPropSpeedForSection(nextSec);

                float specCap = cell.getSpecCapForSection(nextSec);

                if (Float.isNaN(specCap))
                    errorReport.append("Error: no specific capacitance specified for section: "+nextSec.getSectionName()+"\n");

                float specAxRes = cell.getSpecAxResForSection(nextSec);

                if (Float.isNaN(specAxRes))
                    errorReport.append("Error: no specific axial resistance specified for section: " + nextSec.getSectionName() +
                                   "\n");

                if (appv!=null)
                {
                    if (appv.getSpeed()<=0)
                    {
                        errorReport.append("Error: "+appv+" is not a sensible value for action potential propagation velocity\n");
                    }
                    if (fixedMechs.size()+varMechs.size()>0)
                    {
                        warningReport.append("Warning: Section " + nextSec.getSectionName() +
                                               " has an action potential propagation velocity specified, and so any channel mechanisms placed on it: "+fixedMechs+", "+varMechs+" will not be simulated\n");
                    }
                }

                logger.logComment("mechs here: "+fixedMechs+", "+varMechs);

                //int numPassiveChans = 0;
                ArrayList<ChannelMechanism> passChansHere = new ArrayList<ChannelMechanism>();
                
                ArrayList<String> syns = cell.getAllAllowedSynapseTypes();
                
                for(String synName: syns)
                {
                    if (!cellMechNames.contains(synName) && !missingCellMechs.contains(synName))
                    {
                        errorReport.append("Error: Project does not contain the synaptic mechanism: " + synName +
                                           " referred to in this cell\n");

                        missingCellMechs.add(synName);
                    }
                }
                
                ArrayList<String> posCondDensChans = new ArrayList<String>();
                ArrayList<ChannelMechanism> extraParamsOnlyChans = new ArrayList<ChannelMechanism>();
                boolean nml2Mech = false;

                for (int k = 0; k < fixedMechs.size(); k++)
                {
                    ChannelMechanism cm = fixedMechs.get(k);

                    if (!cellMechNames.contains(cm.getName()) && !missingCellMechs.contains(cm.getName()))
                    {
                        errorReport.append("Error: Project does not contain the cell mechanism: " + cm.getName() +
                                           " referred to in this cell\n");

                        missingCellMechs.add(cm.getName());
                    }
                    if (passChanNames.contains(cm.getName())) passChansHere.add(cm); //numPassiveChans++;
                    if (cm.getDensity()>=0)
                    {
                        if (posCondDensChans.contains(cm.getName()))
                        {
                            errorReport.append("Error: Section "+nextSec.getSectionName()+" has 2 seperate values for the conductance density of: "+cm.getName()+"\n");
                            errorReport.append("Channels on this section: "+fixedMechs+"\n");
                        }
                        posCondDensChans.add(cm.getName());
                    }
                    else
                    {
                        extraParamsOnlyChans.add(cm);
                    }

                    try
                    {
                        NeuroML2Component nml2Comp = (NeuroML2Component)project.cellMechanismInfo.getCellMechanism(cm.getName());
                        nml2Mech = true;
                    }
                    catch (Exception e)
                    {
                        //..
                    }
                }
                for(VariableMechanism vm: varMechs)
                {
                    
                    if (!cellMechNames.contains(vm.getName()) && !missingCellMechs.contains(vm.getName()))
                    {
                        errorReport.append("Error: Project does not contain the cell mechanism: " + vm.getName() +
                                           " referred to in this cell\n");

                        missingCellMechs.add(vm.getName());
                    }
                    // TODO: evaluate at centre of section and ensure val >=0

                    if (posCondDensChans.contains(vm.getName()))
                    {
                        errorReport.append("Error: Section "+nextSec.getSectionName()+" has 2 seperate values for the conductance density of: "+vm.getName()+"\n");
                        errorReport.append("Channels on this section: "+fixedMechs+", "+varMechs+"\n");
                    }
                    posCondDensChans.add(vm.getName());


                    //TODO: Check if pass chan is variable
                    //////////if (passChanNames.contains(vm.getName())) passChansHere.add(cm); //numPassiveChans++;
                }
                
                for(ChannelMechanism cm: extraParamsOnlyChans)
                {
                    if (!posCondDensChans.contains(cm.getName()) && !project.cellMechanismInfo.getCellMechanism(cm.getName()).isIonConcMechanism())
                    {
                        errorReport.append("Error: This channel mechanism is specified for section "+nextSec.getSectionName()+": "+cm+"\n");
                        errorReport.append("However, there is no corresponding channel mechanism specified with gmax >=0!\n");
                    }
                }


                int validPassiveConds = 0;
                
                for (ChannelMechanism cmHere: passChansHere)
                {
                    if (cmHere.getDensity()>=0) 
                        validPassiveConds ++;
                }
                        
                if (validPassiveConds == 0 && !nml2Mech)
                {
                    if (appv==null) // no error if there is a prop vel
                    {
                        numNoPassError++;
                        if (numNoPassError < 5)
                        {
                            errorReport.append("Error: Section " + nextSec.getSectionName() +
                                               " does not contain a passive conductance.\n");
                        }
                        else if (numNoPassError == 5)
                        {
                            errorReport.append(
                                "Error: More sections without passive conductance. Supressing further errors...\n");
                        }
                    }
                }
                else if (validPassiveConds > 1)
                {
                    numManyPassWarn++;
                    if (numManyPassWarn<5)
                    {
                        warningReport.append("Warning: Section " + nextSec.getSectionName() +
                                             " contains multiple passive conductances. This can lead to errors when checking cell's electrotonic length.\n");
                    }
                    else if (numManyPassWarn==5)
                    {
                        warningReport.append("Warning: More sections containing multiple passive conductances. Supressing further warnings...\n");
                    }


                }

                LinkedList<Segment> segs = cell.getAllSegmentsInSection(nextSec);

                float totalElecLen = 0;

                if (appv==null && !nml2Mech) // i.e. no ap prop speed settings & it's not nml2
                {
                    float specMembRes = 0.0F;
                    try
                    {
                        specMembRes = CellTopologyHelper.getSpecMembResistance(cell, project, nextSec);
                        
                        boolean isSpherical = true;

                        for (int i = 0; i < segs.size(); i++)
                        {
                            Segment nextSeg = segs.get(i);
                            
                            isSpherical = isSpherical && nextSeg.isSpherical();

                            totalElecLen = totalElecLen + getElectrotonicLength(nextSeg, specMembRes, specAxRes);
                        }

                        float maxELen = project.simulationParameters.getMaxElectroLen();
                        float minELen = project.simulationParameters.getMinElectroLen();

                        if (totalElecLen/(float)nextSec.getNumberInternalDivisions() > maxELen)
                        {
                            String per = "";
                            if (nextSec.getNumberInternalDivisions()>1)
                                per = ", "+totalElecLen/(float)nextSec.getNumberInternalDivisions()+" per int div ";
                            
                            errorReport.append("Error: Section: " + nextSec.getSectionName() +
                                               " (int divs: "+nextSec.getNumberInternalDivisions()+") has too long an electrotonic length: " 
                                               + totalElecLen +" " + per+"> "+maxELen+"\n"
                                               +"This can be rectified by viewing the cell in 3D, and setting a larger number of internal divisions in the section.\n"
                                               +"This can also be done automatically, see Electronic length in the Glossary.\n");
                        }
                        
                        if (!isSpherical && totalElecLen/(float)nextSec.getNumberInternalDivisions() < minELen)
                        {
                            String per = "";
                            
                            if (nextSec.getNumberInternalDivisions()>1)
                                per = ", "+totalElecLen/(float)nextSec.getNumberInternalDivisions()+" per int div ";
                            
                            errorReport.append("Error: Section: " + nextSec.getSectionName() +
                                               " (int divs: "+nextSec.getNumberInternalDivisions()+") has too short an electrotonic length: " 
                                               + totalElecLen +" " + per+"is less than "+minELen+"\n"
                                               +"This can lead to instabilities in the numeric integration. Consider if such a short section is needed.\n");
                        }
                    }
                    catch (CellMechanismException ex)
                    {
                        errorReport.append("Error: Could not determine the electrotonic length of section: " + nextSec.getSectionName() +
                                           "\n");
                    }
                }
            }
        }
        if (errorReport.length() > 0)
        {
            return ValidityStatus.getErrorStatus(errorReport.toString() + "\n" + warningReport.toString());
        }
        else if (warningReport.length() > 0)
        {
            return ValidityStatus.getWarningStatus(warningReport.toString());
        }
        return ValidityStatus.getValidStatus(CELL_IS_BIO_VALID);

    }


    /**
     * Returns a string with the list of differences between the two cells
     */
    public static String compare(Cell cellA, Cell cellB, boolean html)
    {
        return CellTopologyHelper.compare(cellA, cellB, html, null, null);
    }
    
    public static String compare(Cell cellA, Cell cellB, boolean html, Project projectA, Project projectB)
    {
        
        String EOL = GeneralUtils.getEndLine(html);
        String LT = "<";
        if (html) LT = "&lt;";

        logger.logComment("Comparing " + cellA + " in" +projectA+ " to " + cellB+" in "+ projectB);

        String projAInfo = projectA==null ? "": " from "+projectA.getProjectFile().getAbsolutePath();
        String projBInfo = projectB==null ? "": " from "+projectB.getProjectFile().getAbsolutePath();

        
        String header = GeneralUtils.getTabbedString("Comparing " + cellA.getInstanceName() +projAInfo+ " ("+LT+") to " + cellB.getInstanceName()+ projBInfo +
                                             " (>)", "h3", html) + EOL + EOL;
        boolean identical = true;

        StringBuilder info = new StringBuilder(header);

        if (cellA.equals(cellB))
        {
            info.append(GeneralUtils.getTabbedString("These two cells are identical", "font color=\"green\"", html) + EOL);
        }
        if (!cellA.getInstanceName().equals(cellB.getInstanceName()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Instance names do not match:"+EOL+
                ""+LT+" " + cellA.getInstanceName() +  EOL+"> " + cellB.getInstanceName(), "red", html) + EOL + EOL);
        }
        if (!cellA.getCellDescription().equals(cellB.getCellDescription()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Descriptions do not match:"+EOL
                +LT+" " + cellA.getCellDescription() +EOL+"> " + cellB.getCellDescription() , "red", html) + EOL + EOL);
        }
        if (!cellA.getInitialPotential().equals(cellB.getInitialPotential()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Initial potentials do not match:"+EOL
                +LT+" " + cellA.getInitialPotential() +EOL
                +"> " + cellB.getInitialPotential() , "red", html)+EOL+EOL);
        }
        if (!cellA.getSpecAxResVsGroups().equals(cellB.getSpecAxResVsGroups()))
        {
            ArrayList<Float> specAxResesA = cellA.getDefinedSpecAxResistances();
            ArrayList<Float> specAxResesB = cellB.getDefinedSpecAxResistances();

            boolean mismatch = false;

            if (specAxResesA.size() != specAxResesB.size())
            {
                mismatch = true;
            }
            else
            {
                for (Float specAxRes : specAxResesA)
                {
                    Vector<String> groupsA = cellA.getGroupsWithSpecAxRes(specAxRes);
                    Vector<String> groupsB = cellB.getGroupsWithSpecAxRes(specAxRes);
                    if (! (groupsA.containsAll(groupsB) && groupsB.containsAll(groupsA)))
                        mismatch = true;
                }
            }
            if (mismatch)
            {
                identical = false;
                info.append(GeneralUtils.getColouredString("Specific Axial Resistances do not match:"+EOL
                +LT+" " + cellA.getSpecAxResVsGroups() + ""+EOL
                +"> " + cellB.getSpecAxResVsGroups(), "red", html)+EOL+EOL);
            }
        }
        if (!cellA.getSpecCapVsGroups().equals(cellB.getSpecCapVsGroups()))
        {
            ArrayList<Float> specCapsA = cellA.getDefinedSpecCaps();
            ArrayList<Float> specCapsB = cellB.getDefinedSpecCaps();

            boolean mismatch = false;

            if (specCapsA.size()!=specCapsB.size())
            {
                mismatch = true;
            }
            else
            {
                for (Float specCap: specCapsA)
                {
                    Vector<String> groupsA = cellA.getGroupsWithSpecCap(specCap);
                    Vector<String> groupsB = cellB.getGroupsWithSpecCap(specCap);
                    if (!(groupsA.containsAll(groupsB) && groupsB.containsAll(groupsA)))
                        mismatch = true;
                }
            }
            if(mismatch)
            {
                identical = false;
                info.append(GeneralUtils.getColouredString("Specific Capacitances do not match:"+EOL
                +LT+" " + cellA.getSpecCapVsGroups() + ""+EOL
                +"> " + cellB.getSpecCapVsGroups(), "red", html)+EOL+EOL);
            }
        }

        Vector<Segment> segmentsA = cellA.getAllSegments();
        Vector<Segment> segmentsB = cellB.getAllSegments();
        
        if (segmentsB.size()==segmentsA.size())
        {
            info.append(GeneralUtils.getColouredString("The numbers of segments in the cells are IDENTICAL: "+segmentsB.size(), "green", html)+EOL);
        }
        else
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("The numbers of segments in the cells are DIFFERENT", "red", html)+ EOL );
            info.append(GeneralUtils.getColouredString(LT+" " + segmentsA.size() + ""+EOL
                +"> " + segmentsB.size() + EOL + EOL, "red", html));
        }

        int minSize = Math.min(segmentsA.size(), segmentsB.size());


        logger.logComment("Comparing " + segmentsA.size() + " segs to " +segmentsB.size());
        
        StringBuilder segCompare = new StringBuilder();

        int maxDiffSegs = 30;
        int diffSegs = 0;

        String tooManySegsDiff = "";
        
        for (int i = 0; i < minSize; i++)
        {
            if (!segmentsA.get(i).fullEquals(segmentsB.get(i)))
            {
                diffSegs++;

                if (diffSegs<maxDiffSegs)
                {
                    identical = false;

                    segCompare.append(GeneralUtils.getColouredString("A segment does not match:", "red", html)+EOL
                    +LT+" " + segmentsA.get(i).compareTo(segmentsB.get(i), html) + ""+EOL
                    +"> " + segmentsB.get(i).compareTo(segmentsA.get(i), html) + EOL + EOL);

                }
                else
                {
                    tooManySegsDiff = GeneralUtils.getColouredString(EOL+"  (In total "+diffSegs+" segments do not match, errors suppressed after the first "+maxDiffSegs+")" + EOL, "red", html)+EOL;
                }
            }
        }

        if(segCompare.length()==0)
        {
            info.append(GeneralUtils.getColouredString("Compared "+minSize+" segments which were equal in both cells", "green", html)+EOL+EOL);
        }
        else
        {
            info.append(segCompare.toString());
        }
        info.append(tooManySegsDiff);
        
        
        if (segmentsA.size() > minSize)
        {
            info.append(GeneralUtils.getColouredString("There are " + (segmentsA.size() - minSize) + " extra segment(s) in " +
                        cellA.getInstanceName() + EOL + EOL, "red", html));
            
            for (int j = minSize; j < Math.min(segmentsA.size(),minSize+maxDiffSegs) ; j++)
            {
                info.append(GeneralUtils.getColouredString(LT+" " + segmentsA.get(j) + EOL, "red", html));
            }
            if (segmentsA.size()>minSize+maxDiffSegs)
            {
                info.append(GeneralUtils.getColouredString(EOL+"   (Suppressing further extra segment info...)" + EOL, "red", html));
            }

            info.append(EOL);
        }
        if (segmentsB.size() > minSize)
        {
            info.append(GeneralUtils.getColouredString("There are " + (segmentsB.size() - minSize) + " extra segment(s) in " +
                        cellB.getInstanceName() + EOL + EOL, "red", html));

            for (int j = minSize; j < Math.min(segmentsB.size(),minSize+maxDiffSegs); j++)
            {
                info.append(GeneralUtils.getColouredString("> " +" " + segmentsB.get(j) + EOL, "red", html));
            }
            if (segmentsB.size()>minSize+maxDiffSegs)
            {
                info.append(GeneralUtils.getColouredString(EOL+"   (Suppressing further extra segment info...)" + EOL, "red", html));
            }

            info.append(EOL);
        }
        
        
        ArrayList<Section> secsA = cellA.getAllSections();
        ArrayList<Section> secsB = cellB.getAllSections();
        
        if (secsB.size()==secsA.size())
        {
            info.append(GeneralUtils.getColouredString("The numbers of sections in the cells are IDENTICAL: "+secsA.size(), "green", html)+EOL);
        }
        else
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("The numbers of sections in the cells are DIFFERENT", "red", html)+ EOL );
            info.append(GeneralUtils.getColouredString(LT+" " + secsA.size() + ""+EOL
                +"> " + secsB.size() + EOL + EOL, "red", html));
        }
        
        minSize = Math.min(secsA.size(), secsB.size());
        
        StringBuilder secCompare = new StringBuilder();

        int maxDiffSecs = 30;
        int diffSecs = 0;
        String tooManySecsDiff = "";
        
        for (int i = 0; i < minSize; i++)
        {
            if (!secsA.get(i).equalsG(secsB.get(i), true))
            {
                diffSecs++;

                if(diffSecs<maxDiffSecs)
                {
                    identical = false;
                    secCompare.append(GeneralUtils.getColouredString("A section does not match:", "red", html)+EOL
                    +LT+" " + secsA.get(i).compareTo(secsB.get(i), html) + ""+EOL
                    +"> " + secsB.get(i).compareTo(secsA.get(i), html) + EOL + EOL);
                }
                else
                {
                    tooManySecsDiff = GeneralUtils.getColouredString(EOL+"  (In total "+diffSecs+" sections do not match, errors suppressed after the first "+maxDiffSecs+")" + EOL, "red", html)+EOL;
                }
            }
        }
        if(secCompare.length()==0)
        {
            info.append(GeneralUtils.getColouredString("Compared "+minSize+" sections which were equal in both cells", "green", html)+EOL+EOL);
        }
        else
        {
            info.append(secCompare.toString());
        }

        info.append(tooManySecsDiff);
        
        
        
        if (secsA.size() > minSize)
        {
            info.append(GeneralUtils.getColouredString("There are " + (secsA.size() - minSize) + " extra section(s) in " +
                        cellA.getInstanceName() + EOL + EOL, "red", html));
            
            for (int j = minSize; j < Math.min(secsA.size(),minSize+maxDiffSecs) ; j++)
            {
                info.append(GeneralUtils.getColouredString(LT+" " + secsA.get(j) + EOL, "red", html));
            }
            if (secsA.size()>minSize+maxDiffSecs)
            {
                info.append(GeneralUtils.getColouredString(EOL+"   (Suppressing further extra section info...)" + EOL, "red", html));
            }

            info.append(EOL);
        }
        if (secsB.size() > minSize)
        {
            info.append(GeneralUtils.getColouredString("There are " + (secsB.size() - minSize) + " extra section(s) in " +
                        cellB.getInstanceName() + EOL + EOL, "red", html));

            for (int j = minSize; j < Math.min(secsB.size(),minSize+maxDiffSecs); j++)
            {
                info.append(GeneralUtils.getColouredString("> " +" " + secsB.get(j) + EOL, "red", html));
            }
            if (secsB.size()>minSize+maxDiffSecs)
            {
                info.append(GeneralUtils.getColouredString(EOL+"   (Suppressing further extra section info...)" + EOL, "red", html));
            }

            info.append(EOL);
        }


        /* TODO: Should really check the numbers match and that each in A is present in B */
        if (!cellA.getApPropSpeedsVsGroups().equals(cellB.getApPropSpeedsVsGroups()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Action potential propagation speeds do not match"+EOL
                +LT+" " + cellA.getApPropSpeedsVsGroups() + ""+EOL
                +"> " + cellB.getApPropSpeedsVsGroups() + EOL + EOL, "red", html));

            info.append("\n\n");
        }


        /* TODO: Should really check the numbers match and that each in A is present in B */
        if (!cellA.getIonPropertiesVsGroups().equals(cellB.getIonPropertiesVsGroups()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("IonProperties do not match"+EOL
                +LT+" " + cellA.getIonPropertiesVsGroups() + ""+EOL
                +"> " + cellB.getIonPropertiesVsGroups() + EOL + EOL, "red", html));

            info.append("\n\n");
        }



        /* TODO: Should really check the numbers match and that each in A is present in B */
        if (!cellA.getParameterisedGroups().equals(cellB.getParameterisedGroups()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Parameterised groups do not match"+EOL
                +LT+" " + cellA.getParameterisedGroups() + ""+EOL
                +"> " + cellB.getParameterisedGroups() + EOL + EOL, "red", html));

            info.append("\n\n");
        }
        else
        {
            if (cellA.getParameterisedGroups().size()>0)
                info.append(GeneralUtils.getColouredString("Compared "+minSize+" parameterised groups which were equal in both cells", "green", html)+EOL+EOL);
        }

        /* TODO: Should really check the numbers match and that each in A is present in B */
        if (!cellA.getVarMechsVsParaGroups().equals(cellB.getVarMechsVsParaGroups()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Variable mechanisms vs parameterised groups do not match"+EOL
                +LT+" " + cellA.getVarMechsVsParaGroups() + ""+EOL
                +"> " + cellB.getVarMechsVsParaGroups() + EOL + EOL, "red", html));

            info.append("\n\n");
        }
        else
        {
            if (cellA.getVarMechsVsParaGroups().size()>0)
                info.append(GeneralUtils.getColouredString("Compared variable mechanisms vs parameterised groups which were equal in both cells", "green", html)+EOL+EOL);
        }




        Set<ChannelMechanism> chanMechsA = cellA.getChanMechsVsGroups().keySet();
        Set<ChannelMechanism> chanMechsB = cellB.getChanMechsVsGroups().keySet();

        StringBuilder chanMechMismatch = new StringBuilder();

        if (chanMechsA.size()!=chanMechsB.size())
        {
            identical = false;
            chanMechMismatch.append("Difference in number of channel mechanism to group relations between cells"+EOL);
        }
        else
        {
            //System.out.println("same num");
            Iterator<ChannelMechanism> cmAiter = chanMechsA.iterator();
            //Iterator<ChannelMechanism> cmBiter = chanMechsB.iterator();

            while (cmAiter.hasNext())
            {
                ChannelMechanism cm = cmAiter.next();
                Vector<String> groupsA = cellA.getGroupsWithChanMech(cm);
                Vector<String> groupsB = cellB.getGroupsWithChanMech(cm);

                if (groupsB==null)
                {
                    identical = false;
                    chanMechMismatch.append(cellB.getInstanceName() + " does not contain Channel Mechanism: "+cm+EOL);
                    //error.append("groupsA: "+groupsA+", groupsB: "+groupsB+"\n");
                }
                else
                {
                    //System.out.println("groupsA:" + groupsA);
                    //System.out.println("groupsB: " + groupsB);
                    if (groupsA.size()!=groupsB.size() ||
                        !(groupsA.containsAll(groupsB) && groupsB.containsAll(groupsA)))
                    {
                        identical = false;
                        chanMechMismatch.append("Mismatch in groups containing Channel Mechanism: "+cm+EOL);
                    }
                }
            }
        }

        if (chanMechMismatch.length()>0)
        {
            String condDensSymb = UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
        
            ArrayList<String> infoB = new ArrayList<String>();
            
            ArrayList<ChannelMechanism> allChanMechsB = cellB.getAllUniformChanMechs(true);
            allChanMechsB = (ArrayList<ChannelMechanism>)GeneralUtils.reorderAlphabetically(allChanMechsB, true);
            
            for (int i = 0; i < allChanMechsB.size(); i++)
            {
                ChannelMechanism chanMech = allChanMechsB.get(i);
                Vector<String> groupsB = cellB.getGroupsWithChanMech(chanMech);
            
                String moreInfo = chanMech.getDensity() + " "+ condDensSymb+chanMech.getExtraParamsDesc();
            
                String descCm = chanMech.getName() + " ("+moreInfo +")";
                String grpInfo = groupsB.toString();
                //infoB.append("> " +descCm +" is present on "+grpInfo+EOL);
                infoB.add(descCm +" is present on "+grpInfo);
            }
            
            ArrayList<String> infoA = new ArrayList<String>();
            
            ArrayList<ChannelMechanism> allChanMechsA = cellA.getAllUniformChanMechs(true);
            allChanMechsA = (ArrayList<ChannelMechanism>)GeneralUtils.reorderAlphabetically(allChanMechsA, true);
            for (int i = 0; i < allChanMechsA.size(); i++)
            {
                ChannelMechanism chanMech = allChanMechsA.get(i);
                Vector<String> groupsA = cellA.getGroupsWithChanMech(chanMech);
            
                String moreInfo = chanMech.getDensity() + " "+ condDensSymb+chanMech.getExtraParamsDesc();
            
                String descCm = chanMech.getName() + " ("+moreInfo +")";
                String grpInfo = groupsA.toString();
                //infoA.append(LT+" " +descCm +" is present on "+grpInfo+EOL);
                infoA.add(descCm +" is present on "+grpInfo);
            }
            
            identical = false;
            StringBuffer chanInfo = new StringBuffer();
            
            for(String line: infoA)
            {
                if (!infoB.contains(line))
                    chanInfo.append(GeneralUtils.getColouredString(LT+" "+line+"  ***"+EOL, "red", html));
                else
                    chanInfo.append(LT+" "+line+EOL);
            }
            chanInfo.append(EOL);
            
            for(String line: infoB)
            {
                if (!infoA.contains(line))
                    chanInfo.append(GeneralUtils.getColouredString("> "+line+"  ***"+EOL, "red", html));
                else
                    chanInfo.append("> "+line+EOL);                
            }
            
            info.append(GeneralUtils.getColouredString(chanMechMismatch.toString(), "red", html)+EOL
                + chanInfo + EOL+ EOL);
            
            
        }
        else
        {
            
            info.append(GeneralUtils.getColouredString("The numbers of and densities of channel mechanisms are IDENTICAL", "green", html)+EOL+EOL);
        }
                  
        if ((projectA==null) || (projectB==null))
        {}
        else if (!projectA.getProjectName().equals(projectB.getProjectName()))
        {
            logger.logComment("Comparing cells from different projects, mechanisms with the same name may have different implementations");  

            ArrayList<String> allChanMechsA = cellA.getAllChanMechNames(true);
            ArrayList<String> allChanMechsB = cellB.getAllChanMechNames(true);

            for (String chanMechName: allChanMechsA)
            {               

                if (allChanMechsB.contains(chanMechName))
                {                    
                    logger.logComment("Checking "+chanMechName);
                    
                    String chanComp = compareChannelMech(chanMechName, true, projectA, projectB);                  
                                        
                    info.append(chanComp);
                }
                
                info.append("\n\n");
            }
        }
        
        if (!cellA.getSynapsesVsGroups().equals(cellB.getSynapsesVsGroups()))
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("Synaptic mechanisms do not match"+EOL
                +LT+" " + cellA.getSynapsesVsGroups() + ""+EOL
                +"> " + cellB.getSynapsesVsGroups() + EOL + EOL, "red", html));

            info.append("\n\n");
        }
        else
        {
            info.append(GeneralUtils.getColouredString("The potential synaptic mechanism locations are IDENTICAL", "green", html)+EOL+EOL);
        }


        Vector<AxonalConnRegion> axonalArboursA = cellA.getAxonalArbours();
        Vector<AxonalConnRegion> axonalArboursB = cellB.getAxonalArbours();

        minSize = Math.min(axonalArboursA.size(), axonalArboursB.size());
        for (int i = 0; i < minSize; i++)
        {
            if (!axonalArboursA.get(i).equals(axonalArboursB.get(i)))
            {
                identical = false;
                info.append(GeneralUtils.getColouredString("An Axonal Arbour does not match:"+EOL
                +LT+" " + axonalArboursA.get(i) + ""+EOL
                +"> " + axonalArboursB.get(i) + EOL + EOL, "red", html));
                info.append("\n\n");
            }
        }
        if (axonalArboursA.size() > minSize)
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("There are " + (axonalArboursA.size() - minSize) + " extra Axonal Arbour(s) in " +
                        cellA.getInstanceName() + EOL + EOL, "red", html));
            info.append("\n\n");
        }
        if (axonalArboursB.size() > minSize)
        {
            identical = false;
            info.append(GeneralUtils.getColouredString("There are " + (axonalArboursB.size() - minSize) + " extra Axonal Arbour(s) in " +
                        cellB.getInstanceName() + EOL + EOL, "red", html));
            info.append("\n\n");
        }

        float totalSurfaceAreaA = 0;
        float totalLengthA = 0;
        float totalSurfaceAreaB = 0;
        float totalLengthB = 0;


        for (int segNum = 0; segNum < segmentsA.size(); segNum++)
        {
            totalLengthA = totalLengthA + segmentsA.get(segNum).getSegmentLength();
            totalSurfaceAreaA = totalSurfaceAreaA + segmentsA.get(segNum).getSegmentSurfaceArea();
        }
        for (int segNum = 0; segNum < segmentsB.size(); segNum++)
        {
            totalLengthB = totalLengthB + segmentsB.get(segNum).getSegmentLength();
            totalSurfaceAreaB = totalSurfaceAreaB + segmentsB.get(segNum).getSegmentSurfaceArea();
        }
        
        String colour = (totalLengthA==totalLengthB) ? "green" : "red";
        
        info.append(GeneralUtils.getColouredString(LT+" Total length: " + totalLengthA + ""+EOL
                +"> Total length: " + totalLengthB + EOL + EOL, colour, html));

        colour = (totalSurfaceAreaA==totalSurfaceAreaB) ? "green" : "red";

        info.append(GeneralUtils.getColouredString(LT+" Total surface area: " + totalSurfaceAreaA + ""+EOL
                + "> Total surface area: " + totalSurfaceAreaB + EOL + EOL, colour, html));
        


        if (identical)
        {
            info.append(GeneralUtils.getColouredString(CELLS_ARE_IDENTICAL, "green", html));
        }

        return info.toString();
    }
    
    /**
     * Returns a short string detailing the differences between two channel mechanism with the same name.
     * NOTE: used also in <b>compare</b> function
     * 
     *  Matteo Farinella
     */
    public static String compareChannelMech (String chanMechName, boolean html, Project projectA, Project projectB)
    { 
        return compareChannelMech(chanMechName, chanMechName, html, projectA, projectB);
    }
    
    public static String compareChannelMech (String chanMechNameA, String chanMechNameB, boolean html, Project projectA, Project projectB)
    {      
        String EOL = GeneralUtils.getEndLine(html);
        String LT = "<";
        if (html) LT = "&lt;";
//        
//        String header = GeneralUtils.getTabbedString("Comparing " + cellA.getInstanceName() + " ("+LT+") to " + cellB.getInstanceName() +
//                                             " (>)", "h3", html) + EOL + EOL;
//        
//        StringBuilder infoCM = new StringBuilder(header);
        
        StringBuilder infoCM = new StringBuilder();
        
        CellMechanism chanMechA = projectA.cellMechanismInfo.getCellMechanism(chanMechNameA);
        CellMechanism chanMechB = projectB.cellMechanismInfo.getCellMechanism(chanMechNameB);
        
        String projectNameA = projectA.getProjectFileName();
        String projectNameB = projectB.getProjectFileName();

        // DIFFERENT DESCRIPTION LANGUAGES
        if (!chanMechA.getMechanismModel().equals(chanMechB.getMechanismModel()))
        {
            infoCM.append(GeneralUtils.getColouredString("Mechanism "+chanMechNameA+" from project "+projectNameA+
                                                         " and mechanism "+chanMechNameB+" from project "+projectNameA+
                                                         " use different descriptions languages", "red", html)+EOL);
            infoCM.append("\n");
        }
        

        else if (chanMechA instanceof ChannelMLCellMechanism)
        {
            ChannelMLCellMechanism mechA = (ChannelMLCellMechanism) chanMechA;
            ChannelMLCellMechanism mechB = (ChannelMLCellMechanism) chanMechB; 

            File fileA = mechA.getXMLFile(projectA);                         
            File fileB = mechB.getXMLFile(projectB);           
            String fA = GeneralUtils.readShortFile(fileA);
            String fB = GeneralUtils.readShortFile(fileB);
            if (!fA.equals(fB))
            {
                infoCM.append(GeneralUtils.getColouredString("ChannelML mechanism "+chanMechNameA+" from project "+projectNameA
                                                         +" and ChannelML mechanism "+chanMechNameB+" from project "+projectNameB
                                                         +" have different implementations", "red", html)+EOL);
            }
             else
            {
                infoCM.append(GeneralUtils.getColouredString("ChannelML mechanism "+chanMechNameA+" from project "+projectNameA
                                                         +" and ChannelML mechanism "+chanMechNameB+" from project "+projectNameB
                                                         +" are identical", "green", html)+EOL);
            }                                      
        }

        else if (chanMechA instanceof FileBasedMembraneMechanism)
        {
            FileBasedMembraneMechanism mechA = (FileBasedMembraneMechanism) chanMechA;
            FileBasedMembraneMechanism mechB = (FileBasedMembraneMechanism) chanMechB;
            for (int j = 0; j < mechA.getMechanismImpls().length; j++)
            {
                File fileA = mechA.getMechanismImpls()[j].getImplementingFileObject(projectA, chanMechNameA);                                
                File fileB = mechB.getMechanismImpls()[j].getImplementingFileObject(projectB, chanMechNameB);
                String fA = GeneralUtils.readShortFile(fileA);
                String fB = GeneralUtils.readShortFile(fileB);
                if (!fA.equals(fB))
                {
                    infoCM.append(GeneralUtils.getColouredString("File based mechanism "+chanMechNameA+" from project "+projectNameA
                                                         +" and file based mechanism "+chanMechNameB+" from project "+projectNameB
                                                         +" have different implementations for the "+mechA.getMechanismImpls()[j].getSimulationEnvironment()
                                                         +" simulation envirorment", "red", html)+EOL);
                }
                else
                {
                    infoCM.append(GeneralUtils.getColouredString("File based mechanism "+chanMechNameA+" from project "+projectNameA
                                                         +" and file based mechanism "+chanMechNameB+" from project "+projectNameB
                                                         +" are identical", "green", html)+EOL);
                }
            }                        
        }        
        
//                    else if (chanMechA instanceof SBMLCellMechanism)
//                    {
//                        SBMLCellMechanism mechA = (SBMLCellMechanism) chanMechA;
//                        SBMLCellMechanism mechB = (SBMLCellMechanism) chanMechB;
//                    }   
        
        else
        {
            infoCM.append(GeneralUtils.getColouredString("Not able to compare mechanism "+chanMechNameA+" from project "+projectNameA
                                                         +" with mechanism "+chanMechNameB+" from project "+projectNameB, "black", html)+EOL);
        }      
        
        
        return infoCM.toString();
    }   


    public static ValidityStatus getValidityStatus(Cell cell)
    {
        return getValidityStatus(cell, false, false);
    }


    /**
     * Returns a short string detailing the validity of the cell. <b>This covers only morphological aspects</b>
     *
     * NOTE: Check in Help->Glossary->Cell Validity for the conditions under which a cell is "valid"
     *
     */
    public static ValidityStatus getValidityStatus(Cell cell, 
                                                   boolean ignoreCellNotAtOrigin,
                                                   boolean ignoreDisconnNonSimpConnSegs)
    {
        Vector<String> segmentsWithNullParent = new Vector<String>();
        Vector<String> segmentsWithNoSection = new Vector<String>();
        Vector<String> segmentsWithNoEndPoint = new Vector<String>();
        Vector<String> segmentsWithParentsAstray = new Vector<String>();

        Vector<String> repeatedIds = new Vector<String>();
        Vector<Integer> allSegmentIds = new Vector<Integer>();
        Vector<String> segmentsWithNegRadius = new Vector<String>();

        Vector<String> repeatedNameSegments = new Vector<String>();
        Vector<String> allSegmentNames = new Vector<String>();

        Vector<String> repeatedNameSections = new Vector<String>();
        Vector<String> allSectionNames = new Vector<String>();

        Vector<String> segmentsDisconnected = new Vector<String>();

        Vector<String> badlyConnectedSegments = new Vector<String>();
        
        Vector<String> notAxDendSomaSections =  new Vector<String>();

        Vector<String> sphericalSegments = new Vector<String>();
        Vector<String> sphericalErrors = new Vector<String>();

        StringBuilder errorReport = new StringBuilder();
        StringBuilder warningReport = new StringBuilder();


        for (int i = 0; i < cell.getAllSegments().size(); i++)
        {
            Segment segment = cell.getAllSegments().get(i);

            // Check for null parents

            if(segment.getParentSegment()==null)
            {
                segmentsWithNullParent.add("Segment: "+ segment.getSegmentName()
                                              + ", ID: "
                                              + segment.getSegmentId()

                                           + ", section: "
                                           + segment.getSection().getSectionName()
                                           + " has no parent");
            }

            // Check for no Section

            if(segment.getSection()==null)
            {
                segmentsWithNoSection.add("Segment: " + segment.getSegmentName()
                                          + ", ID: "
                                          + segment.getSegmentId()
                                          + " has no section info");
            }

            // Check end point

            if (segment.getEndPointPosition()==null ||
                segment.getEndPointPositionX()==Float.MAX_VALUE||
                segment.getEndPointPositionY()==Float.MAX_VALUE||
                segment.getEndPointPositionZ()==Float.MAX_VALUE)
            {
                segmentsWithNoEndPoint.add("Segment: "+ segment.getSegmentName()
                                              + ", ID: "
                                              + segment.getSegmentId()

                                           + ", section: "
                                           + segment.getSection().getSectionName()
                                           + " doesn't have proper end point info");

            }

            if (segment.getParentSegment()!=null &&
                !cell.getAllSegments().contains(segment.getParentSegment()))
            {
                segmentsWithParentsAstray.add("Segment: " + segment.getSegmentName()
                                              + ", ID: "
                                              + segment.getSegmentId()
                                              + " specifies parent as: "+ segment.getParentSegment().getSegmentName()
                                              + ", ID: "
                                              + segment.getParentSegment().getSegmentId()
                                              +" but this is not in the list of all segments");


            }


            // Check for repeated section ids

            if (allSegmentIds.contains(new Integer(segment.getSegmentId())))
            {
                repeatedIds.add("Section ID: "
                                + segment.getSegmentId()
                                + " is used more than once");
            }
            allSegmentIds.add(new Integer(segment.getSegmentId()));


            // Check for repeated segment names

            if (allSegmentNames.contains(segment.getSegmentName()))
            {
                repeatedNameSegments.add("Segment name: "
                                         + segment.getSegmentName()
                                         + " is used more than once" );
            }
            allSegmentNames.add(segment.getSegmentName());

            if (segment.getRadius() < 0)
            {
                segmentsWithNegRadius.add("Segment: "
                                          + segment.getSegmentName()
                                          + " has negative end radius");
            }
            if (segment.getRadius() == 0)
            {
                segmentsWithNegRadius.add("Segment: "
                                          + segment.getSegmentName()
                                          + " has zero end radius");
            }
            if (segment.isFirstSectionSegment())
            {
                if (segment.getSection().getStartRadius()==0)
                {
                    segmentsWithNegRadius.add("Section: "
                                          + segment.getSection().getSectionName()
                                          + " has zero start radius");

                }
                if (segment.getSection().getStartRadius()<0)
                {
                    segmentsWithNegRadius.add("Section: "
                                          + segment.getSection().getSectionName()
                                          + " has negative start radius");

                }
                if (!( segment.isAxonalSegment() || segment.isDendriticSegment() || segment.isSomaSegment()))
                {
                    notAxDendSomaSections.add("Section: "+ segment.getSection().getSectionName()
                                          + " is not a soma, axon or dendrite (not in any of groups: soma_group, dendrite_group or axon_group)");
                }

            }
            //segmentsWithNegRadius

            // Check for repeated section names

            if (segment.isFirstSectionSegment() &&
                allSectionNames.contains(segment.getSection().getSectionName()))
            {
                repeatedNameSections.add("Section name: "
                                         + segment.getSection().getSectionName()
                                         + " is used more than once" );
            }
            allSectionNames.add(segment.getSection().getSectionName());


            // check for disconnectedness

            if (!ignoreDisconnNonSimpConnSegs && segment.getParentSegment()!=null)
            {
                if (!segment.getParentSegment().getSection().equals(segment.getSection()))
                {
                    Point3f startSeg = segment.getStartPointPosition();
                    Point3f startParent = segment.getParentSegment().getStartPointPosition();
                    Point3f endParent = segment.getParentSegment().getEndPointPosition();

                    float fract = segment.getFractionAlongParent();

                    float allowedAccuracy = (float)Math.max(0.005,  segment.getSegmentLength() /1000f);
                    
                    Point3f connPoint = new Point3f((fract*endParent.x + (1-fract)*startParent.x),
                                                    (fract*endParent.y + (1-fract)*startParent.y),
                                                    (fract*endParent.z + (1-fract)*startParent.z));

                    //if (Math.abs(startSeg.x - (fract*endParent.x + (1-fract)*startParent.x))>allowedAccuracy ||
                    //    Math.abs(startSeg.y - (fract*endParent.y + (1-fract)*startParent.y))>allowedAccuracy ||
                    //    Math.abs(startSeg.z - (fract*endParent.z + (1-fract)*startParent.z))>allowedAccuracy)
                    if (connPoint.distance(startSeg)>allowedAccuracy)
                    {
                        segmentsDisconnected.add("Start point of seg: "
                                                 + segment.getSegmentName()
                                                 + " (id "
                                              + segment.getSegmentId()

                                                 + ") "
                                                 + startSeg
                                              + " is not "
                                                 + segment.getFractionAlongParent()
                                                 + " along parent: "
                                                 + segment.getParentSegment().getSegmentName()
                                              + " (id "
                                              + segment.getParentSegment().getSegmentId()

                                                 + ") "
                                                 + startParent
                                                 + " -> "
                                                 + endParent+" (dist: "+connPoint.distance(startSeg)+" > "+allowedAccuracy+", child seg len: "+segment.getSegmentLength()+")");
                    }
                }
            }

            // Check for badly connected sections

           if (segment.getParentSegment()!=null)
           {
               if (segment.getParentSegment().getSection().equals(segment.getParentSegment()) &&
                   segment.getFractionAlongParent()!=1)
               {
                   badlyConnectedSegments.add("Segment: "
                                                + segment.getSegmentName()
                                                + " is in same section as parent: "
                                                + segment.getParentSegment().getSegmentName()
                                                + " but is connected to a point "
                                                + segment.getFractionAlongParent()
                                                + " along");
               }
           }

           // Check spherical conditions

           if (segment.getSegmentShape()==Segment.SPHERICAL_SHAPE)
           {
               sphericalSegments.add("Segment: "
                                     + segment.getSegmentName()
                                     + " is spherical/zero length (has end point=start point)");

               if (!segment.getSection().getGroups().contains(Section.SOMA_GROUP))
                   sphericalErrors.add("Segment: "
                                     + segment.getSegmentName()
                                     + " is spherical/zero length, but is not in the soma group!");
           }



            // Check for simply connected

            // ...
        }


        if (segmentsWithNullParent.isEmpty())
        {
            errorReport.append("ERROR: No section found with null parent!!\n");
        }

        if(segmentsWithNullParent.size()>1) // as the first segment will have no parent
        {
            errorReport.append("ERROR: More than one null parent section!!\n");
            for (int i = 0; i < segmentsWithNullParent.size(); i++)
            {
                errorReport.append(" - "+ segmentsWithNullParent.elementAt(i)+"\n");

            }
        }

        if(segmentsWithNoSection.size()>0)
        {
            errorReport.append("ERROR: Segments found without proper section info!!\n");
            for (int i = 0; i < segmentsWithNoSection.size(); i++)
            {
                errorReport.append(" - "+ segmentsWithNoSection.elementAt(i)+"\n");

            }
        }

        if(segmentsWithNoEndPoint.size()>0)
        {
            errorReport.append("ERROR: Segments found without proper end point info!!\n");
            for (int i = 0; i < segmentsWithNoEndPoint.size(); i++)
            {
                errorReport.append(" - "+ segmentsWithNoEndPoint.elementAt(i)+"\n");

            }
        }

        if(segmentsWithParentsAstray.size()>0)
        {
            errorReport.append("ERROR: Segments found with unrecognized parent segments!!\n");
            for (int i = 0; i < segmentsWithParentsAstray.size(); i++)
            {
                errorReport.append(" - "+ segmentsWithParentsAstray.elementAt(i)+"\n");

            }


        }

        if (segmentsWithNegRadius.size()>0)
        {
            errorReport.append("ERROR: Segments found which don't have positive radius!!\n");
            for (int i = 0; i < segmentsWithNegRadius.size(); i++)
            {
                errorReport.append(" - "+ segmentsWithNegRadius.elementAt(i)+"\n");

            }

        }

        if(repeatedIds.size()>0)
        {
            errorReport.append("ERROR: Some segment IDs used more than once!!\n");
            for (int i = 0; i < repeatedIds.size(); i++)
            {
                errorReport.append(" - "+ repeatedIds.elementAt(i)+"\n");
            }
        }

        if(repeatedNameSegments.size()>0)
        {
            errorReport.append("ERROR: Some segment names used more than once!!\n");
            for (int i = 0; i < repeatedNameSegments.size(); i++)
            {
                errorReport.append(" - "+ repeatedNameSegments.elementAt(i)+"\n");
            }
        }


        if(repeatedNameSections.size()>0)
        {
            errorReport.append("ERROR: Some section names used more than once!!\n");
            for (int i = 0; i < repeatedNameSections.size(); i++)
            {
                errorReport.append(" - "+ repeatedNameSections.elementAt(i)+"\n");
            }
        }



        if(badlyConnectedSegments.size()>0)
        {
            errorReport.append("ERROR: Segments found within Section but not properly connected, i.e. 1 -> 0\n");
            for (int i = 0; i < badlyConnectedSegments.size(); i++)
            {
                errorReport.append(" - "+ badlyConnectedSegments.elementAt(i)+"\n");

            }
        }




        if(sphericalSegments.size()>1)
        {
            errorReport.append("ERROR: More than one spherical/zero length segment!!\n");
            for (int i = 0; i < sphericalSegments.size(); i++)
            {
                errorReport.append(" - "+ sphericalSegments.elementAt(i)+"\n");
            }
        }


        if(sphericalErrors.size()>0)
        {
            errorReport.append("ERROR: Problems with some of the spherical/zero length segments!!\n");

            for (int i = 0; i < sphericalErrors.size(); i++)
            {
                errorReport.append(" - "+ sphericalErrors.elementAt(i)+"\n");
            }
        }

        if (cell.getAllSegments().isEmpty())
        {
            errorReport.append("ERROR: No segments in cell!!\n");

        }

        if (cell.getInstanceName()==null)
        {
            errorReport.append("ERROR: No Cell name set!!\n");

        }


        if (cell.getOnlySomaSegments().isEmpty())
        {
            errorReport.append("ERROR: No sections set as soma sections (i.e. are in group "+Section.SOMA_GROUP+")!!\n");

        }

        if (cell.getSegmentWithId(0)==null)
        {
            warningReport.append("NOTE: There is no segment with ID = 0. There doesn't strictly have to be, but the convention is that the root segment has ID=0. This could lead to problems too with inputs/plots, as the default values for these assume ID=0.\n");

        }



        if(segmentsDisconnected.size()>0)
        {
            warningReport.append("NOTE: Some segments found where start point is incompatible with connection point on parent!!\n");
            for (int i = 0; i < segmentsDisconnected.size(); i++)
            {
                warningReport.append(" - "+ segmentsDisconnected.elementAt(i)+"\n");
            }
        }
        
        if (notAxDendSomaSections.size()>0)
        {
            warningReport.append("NOTE: Some sections are not specified as soma, axon or dendrite! This will lead to problems when making network connections.\n");
            for (int i = 0; i < notAxDendSomaSections.size(); i++)
            {
                warningReport.append(" - "+ notAxDendSomaSections.elementAt(i)+"\n");
            }
        }


        if (cell.getFirstSomaSegment()!=null &&
            !cell.getFirstSomaSegment().getStartPointPosition().equals(new Point3f()) &&
            !ignoreCellNotAtOrigin)
        {
            warningReport.append("NOTE: Start of soma not at origin (Packing is ill-advised with cells not at origin)\n");
        }

        if (!ignoreDisconnNonSimpConnSegs && !checkSimplyConnected(cell))
        {
            warningReport.append("NOTE: Cell is not Simply Connected, i.e. some segments are " +
                    "connected at points other than the start or end point (0 or 1) of parent\n");
        }

        if (cell.getCellDescription()==null || cell.getCellDescription().length()<cell.getInstanceName().length())
        {
            warningReport.append("NOTE: Cell description is quite short...\n");
        }

        if (cell.getInstanceName().indexOf(" ")>=0)
        {
            errorReport.append("ERROR: Cell name contains spaces! This will lead to errors whn run on most simulators!\n");
        }



        if (errorReport.length() > 0)
        {
            return ValidityStatus.getErrorStatus(errorReport.toString() + "\n" + warningReport.toString());

        }
        else if (warningReport.length() > 0)
        {
            return ValidityStatus.getWarningStatus(warningReport.toString());

        }
        return ValidityStatus.getValidStatus(CELL_IS_MORPH_VALID);


       /// if (report.length()==0) report.append(CELL_IS_VALID+"\n");
      //  return rep;

    }


    public static ArrayList<String> getPassiveChannels(Cell cell, Project project)
    {
        ArrayList<String> passiveChans = new ArrayList<String>();
        ArrayList<String> allChanMechs = cell.getAllChanMechNames(true);


        for (int i = 0; i < allChanMechs.size(); i++)
        {
            //ChannelMechanism next = allChanMechs.get(i);

            CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(allChanMechs.get(i));
            //try
            //{
            if (cellMech instanceof PassiveMembraneMechanism)
            {
                passiveChans.add(allChanMechs.get(i));
            }
            else if (cellMech instanceof ChannelMLCellMechanism)
            {
                ChannelMLCellMechanism cmlMech = (ChannelMLCellMechanism)cellMech;

                boolean isCMLPassive = false;

                try
                {
                    isCMLPassive = cmlMech.isPassiveNonSpecificCond();
                }
                catch (CMLMechNotInitException e)
                {
                    try
                    {
                        cmlMech.initialise(project, false);
                        isCMLPassive = cmlMech.isPassiveNonSpecificCond();
                    }
                    catch (CMLMechNotInitException cmle)
                    {
                        // nothing more to try...
                    }
                    catch (XMLMechanismException ex2)
                    {
                        logger.logError("Error initialising Cell mech: "+ cmlMech.getInstanceName(), ex2);
                        //return null;
                    }


                }

                if (isCMLPassive)
                {
                //System.out.println("---");
                    passiveChans.add(allChanMechs.get(i));
                }
            }
        }
        return passiveChans;
    }



    /**
     * Gets a String summarising the biophysics of the Segment
     */
    public static String getSegmentBiophysics(Segment segment, Cell cell, Project project, boolean html)
    {
        StringBuilder info = new StringBuilder();

        float specAxRes = cell.getSpecAxResForSection(segment.getSection());
        float specCap = cell.getSpecCapForSection(segment.getSection());

        StringBuilder detailedInfo = new StringBuilder();


        info.append(GeneralUtils.getTabbedString("Biophysics of Segment: " + segment.getSegmentName()
                                    + ", ID: " + segment.getSegmentId(), "h2", html)
                    + GeneralUtils.getEndLine(html));

        info.append(GeneralUtils.getTabbedString("Segment details", "h3", html));


        info.append("Start point: <b>" + segment.getStartPointPosition() +
                    "</b>, start radius: <b>" + segment.getSegmentStartRadius() + " "
                    + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                    + GeneralUtils.getEndLine(html)+ "</b> ") ;

        info.append("End point: <b>" + segment.getEndPointPosition() +
                    "</b>, end radius: <b>" + segment.getRadius() + " "
                    + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()

                    + GeneralUtils.getEndLine(html)+ "</b> ");

        info.append("Segment length: <b>" + segment.getSegmentLength() + " "
                    + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ "</b> "

                    + GeneralUtils.getEndLine(html) + GeneralUtils.getEndLine(html));

        detailedInfo.append(getAllUnitDesc("Surface Area",
                                           segment.getSegmentSurfaceArea(),
                                           UnitConverter.areaUnits,
                                           html) + GeneralUtils.getEndLine(html));
        
        
        if (segment.isSpherical())
        {
            info.append("Spherical segment. Volume: <b>" + segment.getSegmentVolume() +" \u03bcm\u00b3</b>" + GeneralUtils.getEndLine(html));
        }

        info.append(getMainUnitDesc("Surface Area",
                                    segment.getSegmentSurfaceArea(),
                                    UnitConverter.areaUnits,
                                    html) + GeneralUtils.getEndLine(html));



        info.append(getMainUnitDesc("Specific Axial Resistance",
                                           specAxRes,
                                           UnitConverter.specificAxialResistanceUnits,
                                           html) + GeneralUtils.getEndLine(html));

        detailedInfo.append(getAllUnitDesc("Specific Axial Resistance",
                                           specAxRes,
                                           UnitConverter.specificAxialResistanceUnits,
                                           html) + GeneralUtils.getEndLine(html));



        detailedInfo.append(getAllUnitDesc("Total Axial Resistance",
                                           getTotalAxialResistance(segment, specAxRes),
                                           UnitConverter.resistanceUnits,
                                           html) + GeneralUtils.getEndLine(html));



        info.append(getMainUnitDesc("Specific Capacitance",
                                    specCap,
                                    UnitConverter.specificCapacitanceUnits,
                                    html) + GeneralUtils.getEndLine(html));

        detailedInfo.append(getAllUnitDesc("Specific Capacitance",
                                           specCap,
                                           UnitConverter.specificCapacitanceUnits,
                                           html) + GeneralUtils.getEndLine(html));

        detailedInfo.append(getAllUnitDesc("Total Segment Capacitance",
                                           specCap * segment.getSegmentSurfaceArea(),
                                           UnitConverter.capacitanceUnits,
                                           html) + GeneralUtils.getEndLine(html));


        ArrayList<ChannelMechanism>  allFixedChanMechs = cell.getUniformChanMechsForSeg(segment);

        float specMembRes = -1;

        //TODO: check variable chan mechs!!
        for (int i = 0; i < allFixedChanMechs.size(); i++)
        {
            ChannelMechanism next = allFixedChanMechs.get(i);

            CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(next.getName());
            try
            {
                if (cellMech instanceof DistMembraneMechanism || cellMech instanceof ChannelMLCellMechanism)
                {
                    float membCondDens = next.getDensity();

                    detailedInfo.append(getAllUnitDesc("Conductance density for Cell Mechanism: <b>" +
                                                       cellMech.getInstanceName()+"</b>",
                                                       membCondDens,
                                                       UnitConverter.conductanceDensityUnits,
                                                       html) + GeneralUtils.getEndLine(html));
                    
                    String params = next.getExtraParamsDesc();

                    info.append(getMainUnitDesc("Conductance density for Cell Mechanism: <b>" + cellMech.getInstanceName()+"</b>",
                                                membCondDens,
                                                UnitConverter.conductanceDensityUnits,
                                                html)+"<b>"+params+ "</b>"+GeneralUtils.getEndLine(html));

                    detailedInfo.append(getAllUnitDesc("Total Conductance on Segment due to: <b>" +
                                                       cellMech.getInstanceName()+"</b>",
                                                       membCondDens * segment.getSegmentSurfaceArea(),
                                                       UnitConverter.conductanceUnits,
                                                       html) + GeneralUtils.getEndLine(html));

                    boolean membLeak = (cellMech instanceof PassiveMembraneMechanism);

                    if (cellMech instanceof ChannelMLCellMechanism)
                    {
                        ChannelMLCellMechanism cpml = (ChannelMLCellMechanism)cellMech;
                        cpml.initialise(project, false);
                        membLeak = membLeak || cpml.isPassiveNonSpecificCond();
                        //System.out.println("cpml: "+ cpml+", pas? "+cpml.isPassiveNonSpecificCond());
                    }

                    if (membLeak)
                    {

                        specMembRes = 1f / membCondDens;

                        detailedInfo.append(getAllUnitDesc("Specific Membrane resistance for passive mechanism: <b>" +
                                                   cellMech.getInstanceName()+"</b>",
                                                   (1f / membCondDens),
                                                   UnitConverter.specificMembraneResistanceUnits,
                                                   html) + GeneralUtils.getEndLine(html));

                        detailedInfo.append(getAllUnitDesc("Membrane resistance on Segment due to: <b>" + cellMech.getInstanceName()+"</b>",
                                                   (1f / (membCondDens * segment.getSegmentSurfaceArea())),
                                                   UnitConverter.resistanceUnits,
                                                   html) + GeneralUtils.getEndLine(html));

                    }
                }


            }
            catch (Exception ex)
            {
                String errorString = "Problem getting info on Cell Mechanism: " + cellMech;
                GuiUtils.showErrorMessage(logger,
                                          errorString,
                                          ex,
                                          null);
                info.append(GeneralUtils.getTabbedString(errorString, "font color=\"red\"", html));
                return info.toString();

            }

        }

        if (specMembRes == -1)
        {
            info.append("Error, unable to determine the unique Cell Mechanism for membrane resistance!");
        }
        else
        {
            float lambda = getSpaceConstant(segment, specMembRes, specAxRes);
            float el = getElectrotonicLength(segment, specMembRes, specAxRes);

           info.append("Space constant lambda: <b>"+ lambda + " "+UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+"</b>" +GeneralUtils.getEndLine(html));
           info.append("Electrotonic length: <b>"+ el + "</b>" + GeneralUtils.getEndLine(html));
           info.append("Distance to soma connection point from segment proximal point: <b>" + segment.getDistanceFromSoma() + " " +UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+"</b>" + GeneralUtils.getEndLine(html) + "</b> ");
           info.append("Segments between soma connection point and segment proximal point: <b>" + segment.getSegmentsFromSoma() + GeneralUtils.getEndLine(html) + "</b> ");
           
        }



        info.append(GeneralUtils.getTabbedString("Section details", "h3", html));

        Section section = segment.getSection();
        LinkedList<Segment> segs = cell.getAllSegmentsInSection(section);
        float totalLengthSection = 0;
        float totalSurfaceArea = 0;
        float totalAxResistance = 0;
        float totalElecLen = 0;


        for (int i = 0; i < segs.size(); i++)
        {
            Segment nextSeg = segs.get(i);
            totalLengthSection = totalLengthSection + nextSeg.getSegmentLength();
            totalAxResistance = totalAxResistance + getTotalAxialResistance(nextSeg, specAxRes);

            totalSurfaceArea = totalSurfaceArea + nextSeg.getSegmentSurfaceArea();
            totalElecLen = totalElecLen + getElectrotonicLength(nextSeg, specMembRes, specAxRes);
        }

        info.append("Number of segments in section: <b>"+ segs.size()+"</b>"+ ", internal divisions: <b>"+ section.getNumberInternalDivisions()+"</b>"+ GeneralUtils.getEndLine(html));
        info.append("Groups of section: <b>"+ section.getGroups()+"</b>"+ GeneralUtils.getEndLine(html));
        info.append("Start point: <b>" + section.getStartPointPosition() +
                    "</b>, start radius: <b>" + section.getStartRadius() + " "
                    + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                    + GeneralUtils.getEndLine(html) + "</b> ");

        info.append("End point: <b>" + segs.getLast().getEndPointPosition() +
                    "</b>, end radius: <b>" + segs.getLast().getRadius() + " "
                    + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                    + GeneralUtils.getEndLine(html) + "</b> ");               
        
        info.append("Branching order (from root): <b>" +  cell.getSegmentBranchingOrder(segs.getFirst().getSegmentId())
                    + GeneralUtils.getEndLine(html) + "</b> ");

        info.append("Section length: <b>" + totalLengthSection + " "
                    + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ "</b> "
                    + GeneralUtils.getEndLine(html)+ GeneralUtils.getEndLine(html));

        info.append("Section surface area: <b>" + totalSurfaceArea + " "
                    + UnitConverter.areaUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ "</b> "
                    + GeneralUtils.getEndLine(html));

        info.append("Total axial resistance along section: <b>" + totalAxResistance + " "
                    + UnitConverter.resistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+ "</b> "
                    + GeneralUtils.getEndLine(html));

        info.append("Total electrotonic length: <b>" + totalElecLen + "</b> "
                    + GeneralUtils.getEndLine(html));

        if (section.getNumberInternalDivisions()>1)
        {
            info.append("Section electrotonic length/num internal divisions ("+section.getNumberInternalDivisions()
                        +"): <b>" + totalElecLen/(float)section.getNumberInternalDivisions() + "</b> "
                    + GeneralUtils.getEndLine(html));
        }



        info.append(GeneralUtils.getEndLine(html)+GeneralUtils.getEndLine(html));
        info.append(GeneralUtils.getTabbedString("---- Detailed biophysical info on Segment ----", "h3", html));

        info.append(detailedInfo.toString());


        return info.toString();


    }

    /**
     * Gets the specific membrane resistance for the specified section. Note, will
     * calculate the value based on the the first passive conductance, so any function using
     * this will give problems in a cell with multiple passive leak currents.
     */
    public static float getSpecMembResistance(Cell cell, Project project, Section section) throws CellMechanismException
    {
        //TODO: check variable chan mechs!!
        ArrayList<ChannelMechanism>  allFixedChanMechs = cell.getUniformChanMechsForSec(section);

        float specMembRes = -1;

        for (int i = 0; i < allFixedChanMechs.size(); i++)
        {
            ChannelMechanism next = allFixedChanMechs.get(i);

            CellMechanism cellMech = project.cellMechanismInfo.getCellMechanism(next.getName());

            if (cellMech instanceof ChannelMLCellMechanism && next.getDensity()>=0)
            {
                ChannelMLCellMechanism cmlcp = (ChannelMLCellMechanism)cellMech;
                
                if (cmlcp.isPassiveNonSpecificCond())
                {
                    float condDens = next.getDensity();
                    specMembRes = 1/condDens;
                    return specMembRes;
                }
            }
            if (cellMech instanceof PassiveMembraneMechanism)
            {
                float condDens = next.getDensity();
                specMembRes = 1/condDens;
                return specMembRes;

            }
        }
        throw new CellMechanismException("Passive Cell mechanism not found on cell, so could not calculate specific membrane resistance");

    }



    /**
     * Gets a String summarising the biophysics of the Segment

    public static String getSegmentSpaceConstant(Segment segment, Cell cell)
    {
        //info.append("Ax res: "+ cell.getSpecAxRes().getNominalNumber());
        //info.append("Memb res: "+ membRes);
        float ratio = specMembRes / cell.getSpecAxRes().getNominalNumber();

        float diam = (float)CompartmentHelper.getEquivalentRadius(segment.getSegmentStartRadius(),
                                                           segment.getRadius(),
                                                           segment.getSegmentLength()) * 2;

       float lambda = (float)Math.sqrt(ratio * diam);

    }*/


    /**
     * Useful to call when the cell has been restructured/recompartmentalised
     * Causes the parents of segments always to precede the segments, even if parent id > seg id
     * Needed to display the cells properly in 3d
     *
     */
    public static void reorderSegsParentsFirst(Cell cell)
    {
        logger.logComment("");
        logger.logComment("-----------    Reordering segments so parents appear before children");

        for (int segIndex = 0; segIndex < cell.getAllSegments().size(); segIndex++)
        {
            logger.logComment("--- Checking segIndex: " + segIndex + ", seg: " + cell.getAllSegments().get(segIndex));

            if (cell.getAllSegments().get(segIndex).getParentSegment() != null)
            {
                int segId = cell.getAllSegments().get(segIndex).getSegmentId();
                int parentId = cell.getAllSegments().get(segIndex).getParentSegment().getSegmentId();
                logger.logComment("Checking seg at " + segIndex + ", ID: " + segId
                                  + ", parent ID: " + parentId);

                int parentIndex = -1;
                for (int parentSearchIndex = 0; parentSearchIndex < segIndex - 1; parentSearchIndex++)
                {
                    if (cell.getAllSegments().get(parentSearchIndex).getSegmentId() == parentId)
                    {
                        parentIndex = parentSearchIndex;
                        logger.logComment("1 Found  a parent: "+cell.getAllSegments().get(parentSearchIndex));
                    }
                }
                if (parentIndex < 0)
                {
                    for (int parentSearchIndex = segIndex + 1; parentSearchIndex < cell.getAllSegments().size();
                         parentSearchIndex++)
                    {
                        if (cell.getAllSegments().get(parentSearchIndex).getSegmentId() == parentId)
                        {
                            parentIndex = parentSearchIndex;
                            logger.logComment("2 Found  a parent: "+cell.getAllSegments().get(parentSearchIndex));
                        }
                    }
                }

                logger.logComment("Parent found at: " + parentIndex);

                if (parentIndex > segIndex)
                {
                    logger.logComment("Problem with parent");

                    for (int searchIndex = segIndex; searchIndex < cell.getAllSegments().size(); searchIndex++)
                    {
                        if (cell.getAllSegments().get(searchIndex).getSegmentId() == parentId)
                        {
                            Segment parentSeg = cell.getAllSegments().get(searchIndex);
                            logger.logComment("Found lost parent at " + searchIndex + ", " + parentSeg);

                            cell.getAllSegments().removeElementAt(searchIndex);
                            cell.getAllSegments().insertElementAt(parentSeg, segIndex);

                            searchIndex = cell.getAllSegments().size();

                            logger.logComment("  Inserted element which was at " + searchIndex + " in at " + segIndex);
                            segIndex = segIndex - 1; // to check the parent's parent...
                        }
                    }
                }
            }
            else
            {
                logger.logComment("Ignoring due to null parent");
            }
        }

        for (int segIndex = 0; segIndex < cell.getAllSegments().size(); segIndex++)
        {
            Segment seg = cell.getAllSegments().get(segIndex);
            logger.logComment("Index: " + segIndex + ", seg: " + seg.getSegmentName() + ", ID: " + seg.getSegmentId() +
                              ", parent: " + seg.getParentSegment());
        }

        logger.logComment("-----------    Done reordering segments so parents appear before children");


        logger.logComment("");

    }

    /**
     * Best if first soma seg id is 0...
     */
    public static void zeroFirstSomaSegId(Cell cell)
    {
        Segment somaSeg = cell.getFirstSomaSegment();
        int orig = somaSeg.getSegmentId();
        if (somaSeg!=null && somaSeg.getSegmentId()!=0)
        {
            if (cell.getSegmentWithId(0)!=null)
            {
                int newSegId = 10000;
                while(cell.getSegmentWithId(newSegId)!=null)
                {
                    newSegId++;
                }
                cell.getSegmentWithId(0).setSegmentId(newSegId);
            }
            somaSeg.setSegmentId(0);
        }

        logger.logComment("-----------    Set first soma seg id to 0 (originally "+orig+")...");

    }

    private static String getMainUnitDesc(String desc, float value, Units[] unitSet, boolean html)
    {
        StringBuilder info = new StringBuilder();


        info.append(desc + ": ");



        info.append(GeneralUtils.getTabbedString(UnitConverter.convertFromNeuroConstruct(value,
                                                            unitSet[UnitConverter.NEUROCONSTRUCT_UNITS],

                                                            UnitConverter.GENESIS_SI_UNITS)
                    + " ", "b", html));

        info.append(" ("+UnitConverter.convertFromNeuroConstruct(value,
                                                            unitSet[UnitConverter.NEUROCONSTRUCT_UNITS],
                                                            UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)

                    + ") ");


        return info.toString();

    }

    private static String getAllUnitDesc(String desc, float value, Units[] unitSet, boolean html)
    {
        StringBuilder info = new StringBuilder();

        String preBold = "";
        String postBold = "";

        if (html)
        {
            preBold = "<b>";
            postBold = "</b>";
        }

        info.append(GeneralUtils.getTabbedString(desc + ": ", "b", html) + GeneralUtils.getEndLine(html));

        if (html) info.append("<table width=\"100%\" border=\"0\"><tr><td>");

        info.append(preBold+ value + " "
                     + unitSet[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()
                     + postBold + " (neuroConstruct units)" + GeneralUtils.getEndLine(html));

        if (html) info.append("</td><td>");

        info.append(preBold + UnitConverter.convertFromNeuroConstruct(value,
                                                            unitSet[UnitConverter.NEUROCONSTRUCT_UNITS],
                                                            UnitConverter.NEURON_UNITS) + " "
                    + postBold + " (NEURON units)" + GeneralUtils.getEndLine(html));

        if (html) info.append("</td></tr>\n<tr><td>");

        info.append(preBold + UnitConverter.convertFromNeuroConstruct(value,
                                                            unitSet[UnitConverter.NEUROCONSTRUCT_UNITS],
                                                            UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)

                    + postBold + " (GENESIS PHY units)" + GeneralUtils.getEndLine(html));

        if (html) info.append("</td><td>");
        info.append(preBold + UnitConverter.convertFromNeuroConstruct(value,
                                                            unitSet[UnitConverter.NEUROCONSTRUCT_UNITS],

                                                            UnitConverter.GENESIS_SI_UNITS)
                    + postBold + " (GENESIS SI units)" + GeneralUtils.getEndLine(html));

        if (html) info.append("</td></tr></table>\n");
        return info.toString();

    }





    public static void main(String[] args)
    {

        try
        {

            Project testProj = Project.loadProject(new File("../nC_projects/GoCSolinas_testclamp/GoCSolinas_testclamp.ncx"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });

            Cell cellB = testProj.cellManager.getCell("Golgi_040408_blue");
            Cell cellR = testProj.cellManager.getCell("Golgi_040408_red");

            System.out.println("Comp: " +CellTopologyHelper.compare(cellB,cellR, false) );



            if (true) return;

            Cell cell = testProj.cellManager.getCell("Basic");
            //Cell cell = new SimpleCell("dumCell");

            System.out.println("Info: " + CellTopologyHelper.printDetails(cell, testProj));


            boolean useHtml = true;

           CellTopologyHelper.getSegmentBiophysics(cell.getSegmentWithId(7),
                                                                     cell,
                                                                     testProj,
                                                                     useHtml);

          //  SimpleViewer.showString(summary,
          //                          "Biophysics of segment: " + cell.getFirstSomaSegment().getSegmentName() + " in cell: " +
           //                         cell.getInstanceName(), 10, false, useHtml,
          //                      .6f);

          GenesisCompartmentalisation gc = new GenesisCompartmentalisation();

          Cell genCell = gc.getCompartmentalisation(cell);



            System.out.println("Altered: " + CellTopologyHelper.printDetails(genCell, testProj));

            System.out.println("Comp: " +CellTopologyHelper.compare(cell,genCell, false) );

            
            System.out.println("Subset: " +CellTopologyHelper.isGroupASubset("dendrite_group", "all", cell) );
            System.out.println("Subset: " +CellTopologyHelper.isGroupASubset("dendrite_group", "soma_group", cell) );

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
