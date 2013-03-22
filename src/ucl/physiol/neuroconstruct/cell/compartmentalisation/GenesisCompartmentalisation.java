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

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

import java.io.*;
import java.util.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.compartment.*;

/**
 * Morphological compartmentalisations useful for simulators based on compartmental as opposed to cable modelling
 * i.e. GENESIS, but not NEURON
 *
 * @author Padraig Gleeson
 *  
 *
 */

public class GenesisCompartmentalisation extends MorphCompartmentalisation
{
    private static ClassLogger logger = new ClassLogger("GenesisCompartmentalisation");

    private int idOfAlteredSegs = 100000; // to make it obvious which ids have been altered...

    public static final String extraSegSuffix = "__Extra";
    public static final String secondCompSuffix = "_Gen2ndComp";

    public static final String GEN_COMP = "GENESIS Compartmentalisation";

    public GenesisCompartmentalisation()
    {
        super(GEN_COMP,
              "Projection onto a morphology optimized for simulators based on indivisible compartmental"
              +" modelling, i.e. GENESIS as opposed to cable/section based modelling of NEURON. Maps a multi segment section "
              + "on to 2 single segment cylindrical sections which have radii chosen to preserve the section length, curved surface area and axial resistance. ");

        //logger.setThisClassVerbose(true);
    }

    public Cell generateComp(Cell originalCell)
    {
        Cell newCell = (Cell) originalCell.clone();

        // Handy just in case cell is imported from a strangely arranged morph file

        CellTopologyHelper.reorderSegsParentsFirst(newCell);

        while (idOfAlteredSegs<=newCell.getNextSegmentId())
        {
            idOfAlteredSegs*=2;
        }

        ArrayList<Section> secs = newCell.getAllSections();

        boolean existApPropSpeeds = originalCell.getApPropSpeedsVsGroups().size() > 0;

        //child segments whose parents need to be checked
        ArrayList<Integer> childrenToFix = new ArrayList<Integer>();

        for (int secIndex = 0; secIndex < secs.size(); secIndex++)
        {
            Section sec = secs.get(secIndex);

            int origNumIntDivs = sec.getNumberInternalDivisions();

            LinkedList<Segment> segsHere = newCell.getAllSegmentsInSection(sec);

            if (segsHere.size() == 1 &&
                segsHere.getFirst().getRadius() == segsHere.getFirst().getSegmentStartRadius())
            {
                logger.logComment("   ---  Leaving section as cylinder...");
                // leave it alone, a cylinder is fine...

                if (origNumIntDivs == 1)
                {
                    logger.logComment("NumIntDivs = 1, so 1 compartment fine.");
                }
                else if (segsHere.getFirst().getSegmentLength() == 0)
                {
                    logger.logComment("Spherical segment so 1 compartment fine.");
                }
                else if (origNumIntDivs >= 2)
                {
                    logger.logComment("NumIntDivs >= 2, so splitting up compartment");

                    ArrayList<Integer> segIds = splitSingleSegment(newCell, segsHere.getFirst(), origNumIntDivs, true);

                    childrenToFix.add(segIds.get(0));
                }
            }
            else if (existApPropSpeeds && newCell.getApPropSpeedForSection(sec) != null)
            {
                logger.logComment("ApPropSpeed defined for section, so leaving it as is...");
            }
           /* else if (segsHere.getFirst().isSomaSegment())
            {
                String excuse = "Not splitting soma, as electrotonic length is unlikely to be an issue, and all 3D detail is usually needed as dends, stims connection location might have big impact on stimulation";

                logger.logComment(excuse);

                segsHere.getFirst().setComment("Recompartmentalisation comment: "+excuse);

            }*/
            else
            {
                SimpleCompartment[] multi = new SimpleCompartment[segsHere.size()];

                logger.logComment("   ---  Adjusting section: " + segsHere);

                ArrayList<SegmentRange> oldSegs = new ArrayList<SegmentRange> ();

                for (int segIndex = 0; segIndex < segsHere.size(); segIndex++)
                {
                    Segment nextSeg = segsHere.get(segIndex);


                    oldSegs.add(new SegmentRange(nextSeg.getSegmentId(), nextSeg.getSegmentLength(), 0, 1));

                    SimpleCompartment comp = new SimpleCompartment(nextSeg.getSegmentStartRadius(),
                                                                   nextSeg.getRadius(),
                                                                   nextSeg.getSegmentLength());
                    multi[segIndex] = comp;

                }

                // The hard bit, trust me...
                SimpleCompartment[] twoCompEquiv = CompartmentHelper.getDoubleCylinder(multi);

                float firstRadius = (float) twoCompEquiv[0].getStartRadius();
                float secondRadius = (float) twoCompEquiv[1].getStartRadius();

                if (firstRadius <= 0 || secondRadius <= 0)
                {
                    logger.logError("Problem with negative radii: twoCompEquiv[0]: "+twoCompEquiv[0]
                                    +", twoCompEquiv[1]: "+twoCompEquiv[1]);
                    logger.logError("Leaving section alone: "+sec);
                }
                else
                {
                    float origSecLength = CellTopologyHelper.getSectionLength(newCell, sec);

                    Segment firstSeg = segsHere.getFirst();
                    Segment endSeg = null;

                    String newSecName = firstSeg.getSection().getSectionName() + secondCompSuffix;

                    Segment secSeg = null;

                    //Vector childrenOfEnd = null;

                    if (segsHere.size() > 1) // should be the case...
                    {
                        secSeg = segsHere.get(1);
                        endSeg = segsHere.getLast();
                        //childrenOfEnd = CellTopologyHelper.getAllChildSegments(newCell, endSeg, false);
                    }
                    else
                    {
                        //childrenOfEnd = CellTopologyHelper.getAllChildSegments(newCell, firstSeg, false);

                        secSeg = (Segment) firstSeg.clone();
                        secSeg.setSegmentId(this.getNextAlteredSegId());
                        secSeg.setSegmentName(secSeg.getSegmentName() + extraSegSuffix);
                        secSeg.setParentSegment(firstSeg);
                        secSeg.setFractionAlongParent(1);

                        newCell.getAllSegments().add(secSeg);
                        endSeg = secSeg;
                    }

                    float lengthToEndSeg = firstSeg.getStartPointPosition().distance(endSeg.getEndPointPosition());

                    float growthFactor = origSecLength / lengthToEndSeg;

                    Point3f start = firstSeg.getStartPointPosition();

                    Point3f endEnd = endSeg.getEndPointPosition();

                    //System.out.println("endSeg: "+endSeg.getSegmentName());
                    //System.out.println("secSeg: "+secSeg.getSegmentName());

                    firstSeg.setEndPointPositionX( (start.x + growthFactor * (endEnd.x - start.x) / 2f));
                    firstSeg.setEndPointPositionY( (start.y + growthFactor * (endEnd.y - start.y) / 2f));
                    firstSeg.setEndPointPositionZ( (start.z + growthFactor * (endEnd.z - start.z) / 2f));

                    //System.out.println("firstSeg EndPoint: "+ firstSeg.getEndPointPosition());

                    firstSeg.getSection().setStartRadius(firstRadius);

                    firstSeg.setRadius(firstRadius); // same as end..

                    Section newSection = (Section) firstSeg.getSection().clone(); // to preserve groups etc.
                    newSection.setSectionName(newSecName);

                    newSection.setStartPointPositionX(firstSeg.getEndPointPositionX());
                    newSection.setStartPointPositionY(firstSeg.getEndPointPositionY());
                    newSection.setStartPointPositionZ(firstSeg.getEndPointPositionZ());
                    newSection.setStartRadius(secondRadius);

                    secSeg.setSection(newSection);

                    secSeg.setEndPointPositionX(start.x + growthFactor * (endEnd.x - start.x));
                    secSeg.setEndPointPositionY(start.y + growthFactor * (endEnd.y - start.y));
                    secSeg.setEndPointPositionZ(start.z + growthFactor * (endEnd.z - start.z));

                    secSeg.setRadius(secondRadius);

                    firstSeg.setSegmentId(this.getNextAlteredSegId());
                    secSeg.setSegmentId(this.getNextAlteredSegId());
/*
                    for (int i = 0; i < childrenOfEnd.size(); i++)
                    {
                        Segment child = (Segment) childrenOfEnd.get(i);
                        child.setParentSegment(secSeg);
                    }*/

                    for (int segIndex = 2; segIndex < segsHere.size(); segIndex++)
                    {
                        Segment nextSeg = segsHere.get(segIndex);
                        newCell.getAllSegments().remove(nextSeg); // remove all except first...
                    }

                    logger.logComment("       firstSeg: " + firstSeg);
                    logger.logComment("       secSeg: " + secSeg);

                    if (origNumIntDivs == 1 || origNumIntDivs == 2)
                    {
                        logger.logComment("NumIntDivs = 1 or 2, so 2 compartments fine.");

                        int segToMapto = firstSeg.getSegmentId();
                        float newSegLength = firstSeg.getSegmentLength();
                        float totalToTraverse = newSegLength;

                        float accuracy = 1e-5f;

                        for (SegmentRange segRange : oldSegs)
                        {
                            logger.logComment("Looking at old seg range: " + segRange+", totalToTraverse: "+totalToTraverse);

                            if (segRange.getRangeLength() < (totalToTraverse+accuracy))
                            {
                                float lenToStartOldSeg = newSegLength - totalToTraverse;
                                float lenToEndOldSeg = lenToStartOldSeg + segRange.getRangeLength();

                                float startFract = (lenToStartOldSeg) / newSegLength;
                                float endFract = (lenToEndOldSeg) / newSegLength;

                                SegmentRange sr = new SegmentRange(segToMapto, newSegLength, startFract, endFract);

                                logger.logComment("Adding mapping: " + sr);

                                mapper.addMapping(segRange, new SegmentRange[]
                                                  {sr});

                                totalToTraverse -= segRange.getRangeLength();
                            }
                            else
                            {

                                float lenToStartOnFirstSeg = newSegLength - totalToTraverse;
                                float startFractFirstSeg = (lenToStartOnFirstSeg) / newSegLength;
                                float endFractFirstSeg = 1;

                                float startFractSecSeg = 0;
                                float lenToEndOnSecSeg = segRange.getRangeLength() - totalToTraverse;
                                float endFractSecSeg = lenToEndOnSecSeg / newSegLength;

                                SegmentRange srFirst = new SegmentRange(segToMapto,
                                                                        newSegLength,
                                                                        startFractFirstSeg,
                                                                        endFractFirstSeg);

                                segToMapto = secSeg.getSegmentId();

                                SegmentRange srSecond = new SegmentRange(segToMapto,
                                                                         newSegLength,
                                                                         startFractSecSeg,
                                                                         endFractSecSeg);

                                logger.logComment("Adding 2 mappings: " + srFirst+", "+srSecond);

                                mapper.addMapping(segRange, new SegmentRange[]
                                                  {srFirst, srSecond});

                                totalToTraverse = newSegLength - lenToEndOnSecSeg;
                            }
                        }

                        childrenToFix.add(firstSeg.getSegmentId());
                    }
                    else if (origNumIntDivs > 2)
                    {
                        int numInEach = (int) Math.ceil( (float) origNumIntDivs / 2f);
                        logger.logComment("NumIntDivs > 2 (" + origNumIntDivs +
                                          "), so splitting up each compartment into " + numInEach);

                        logger.logComment("Previous first seg: " + firstSeg);

                        ArrayList<Integer> firstSegsMappedIds = splitSingleSegment(newCell, firstSeg, numInEach, false);
                        ArrayList<Integer> secSegsMappedIds = splitSingleSegment(newCell, secSeg, numInEach, false);

                        firstSeg.getSection().setNumberInternalDivisions(numInEach);
                        secSeg.getSection().setNumberInternalDivisions(numInEach);

                        logger.logComment("\n                                        Done splitting!!! ");

                        //logger.logComment("Previous first seg: " + firstSeg);

                        int indexToMapTo = 0;

                        ArrayList<Integer> arrayToMapTo = firstSegsMappedIds;
                        arrayToMapTo.addAll(secSegsMappedIds);

                        int newNumSegs = origNumIntDivs;

                        if (Math.floor((float)origNumIntDivs/2.0)!=(origNumIntDivs/2.0))
                            newNumSegs = origNumIntDivs+1;

                        logger.logComment("origNumIntDivs: " + origNumIntDivs+", newNumSegs: " + newNumSegs);

                        float newSegLength = origSecLength/(float)newNumSegs;
                        float totalLeftCurrSeg = newSegLength;

                        for (SegmentRange oldSegRange : oldSegs)
                        {
                            logger.logComment("       +++++++    oldSegs segRange: " + oldSegRange);
                            logger.logComment("totalLeftCurrSeg: " + totalLeftCurrSeg);
                            logger.logComment("indexToMapTo: " + indexToMapTo);
                            logger.logComment("arrayToMapTo: " + arrayToMapTo);

                            if (oldSegRange.getRangeLength() <= totalLeftCurrSeg)
                            {
                                logger.logComment("Old seg shorter than what's left on new segs");
                                float lenToStartOldSeg = newSegLength - totalLeftCurrSeg;
                                float lenToEndOldSeg = lenToStartOldSeg + oldSegRange.getRangeLength();

                                float startFract = (lenToStartOldSeg) / newSegLength;
                                float endFract = (lenToEndOldSeg) / newSegLength;

                                SegmentRange sr = new SegmentRange(arrayToMapTo.get(indexToMapTo), newSegLength, startFract, endFract);

                                mapper.addMapping(oldSegRange, new SegmentRange[]
                                                  {sr});

                                logger.logComment("-- Added mapping to: " + sr);

                                totalLeftCurrSeg -= oldSegRange.getRangeLength();
                            }
                            else
                            {
                                logger.logComment("Old range len longer than new seg len");

                                logger.logComment("oldSegRange.getRangeLength(): " + oldSegRange.getRangeLength());
                                logger.logComment("totalLeftCurrSeg: " + totalLeftCurrSeg);
                                logger.logComment("indexToMapTo: " + indexToMapTo);
                                logger.logComment("arrayToMapTo.get(indexToMapTo): " + arrayToMapTo.get(indexToMapTo));


                                ArrayList<SegmentRange> srs = new ArrayList<SegmentRange>();

                                float rangeLenOrigSegCompleted = 0;

                                float lenToStartOnCurrSeg = newSegLength - totalLeftCurrSeg;


                                float startFractFirstSeg = (lenToStartOnCurrSeg) / newSegLength;
                                float endFractFirstSeg = 1;

                                SegmentRange srFirst = new SegmentRange(arrayToMapTo.get(indexToMapTo),
                                                                        newSegLength,
                                                                        startFractFirstSeg,
                                                                        endFractFirstSeg);

                                logger.logComment("- srFirst: " + srFirst);

                                srs.add(srFirst);

                                indexToMapTo++;
                                rangeLenOrigSegCompleted = rangeLenOrigSegCompleted+totalLeftCurrSeg;

                                while(oldSegRange.getRangeLength()-rangeLenOrigSegCompleted>newSegLength)
                                {
                                    logger.logComment("rangeLenOrigSegCompleted: " + rangeLenOrigSegCompleted);
                                    logger.logComment("indexToMapTo: " + indexToMapTo);
                                    logger.logComment("arrayToMapTo.get(indexToMapTo): " + arrayToMapTo.get(indexToMapTo));

                                    float startFractCurrSeg = 0;
                                    float endFractCurrSeg = 1;

                                    SegmentRange srNext = new SegmentRange(arrayToMapTo.get(indexToMapTo),
                                                                            newSegLength,
                                                                            startFractCurrSeg,
                                                                            endFractCurrSeg);

                                    srs.add(srNext);

                                    rangeLenOrigSegCompleted = rangeLenOrigSegCompleted + newSegLength;


                                    logger.logComment("-- tot srs: " + srs);
                                    indexToMapTo++;
                                    lenToStartOnCurrSeg = 0;
                                    //totalToTraverse = newSegLength;
                                }
                                logger.logComment("Done with inner seg maps");

                                logger.logComment("rangeLenOrigSegCompleted: " + rangeLenOrigSegCompleted);
                                logger.logComment("indexToMapTo: " + indexToMapTo);

                                if (indexToMapTo<arrayToMapTo.size())
                                {
                                    logger.logComment("arrayToMapTo.get(indexToMapTo): " +
                                                      arrayToMapTo.get(indexToMapTo));

                                    if (rangeLenOrigSegCompleted < oldSegRange.getRangeLength())
                                    {
                                        float startFractFinalSeg = 0;
                                        float lenToEndOnFinalSeg = oldSegRange.getRangeLength() -
                                            rangeLenOrigSegCompleted;
                                        float endFractFinalSeg = lenToEndOnFinalSeg / newSegLength;

                                        SegmentRange srFinal = new SegmentRange(arrayToMapTo.get(indexToMapTo),
                                            newSegLength,
                                            startFractFinalSeg,
                                            endFractFinalSeg);

                                        logger.logComment("srFinal: " + srFinal);

                                        totalLeftCurrSeg = newSegLength - lenToEndOnFinalSeg;

                                        if (totalLeftCurrSeg == 0)
                                        {
                                            totalLeftCurrSeg = newSegLength;
                                            indexToMapTo++;

                                        }

                                        srs.add(srFinal);
                                    }
                                }
                                else
                                {
                                    logger.logComment("**********   float rounding off caused all segs to be used up...");

                                    totalLeftCurrSeg = newSegLength;
                                    indexToMapTo++;

                                }

                                SegmentRange[] srarr = new SegmentRange[srs.size()];
                                for (int i = 0; i < srs.size(); i++)
                                {
                                    srarr[i] = srs.get(i);
                                }
                                mapper.addMapping(oldSegRange, srarr);


                                //totalToTraverse -= segRange.getRangeLength();

                                logger.logComment("   --  Done with multipart seg map");

                            }
                        }


                        childrenToFix.add(firstSegsMappedIds.get(0));

                    }

                }
            }
        }

        Vector<Segment> segs = newCell.getAllSegments();
        ArrayList<Integer> idsMappedFrom = mapper.getAllSegIdsMappedFrom();
        ArrayList<Integer> idsMappedTo = mapper.getAllSegIdsMappedTo();

        logger.logComment("idsMappedFrom: " + idsMappedFrom);
        logger.logComment("idsMappedTo: " + idsMappedTo);


        CellTopologyHelper.reorderSegsParentsFirst(newCell);

        logger.logComment("childrenToFix: " + childrenToFix);

        // Reuniting children with their real parents...
        for (Segment seg: segs)
        {

                logger.logComment("\n                            Checking parent info for : " + seg);

            logger.logComment("Parent: "+seg.getParentSegment());

            if (seg.getParentSegment()!=null)
            {
                int childId = seg.getSegmentId();
                int parentId = seg.getParentSegment().getSegmentId();


                if (idsMappedFrom.contains(parentId))
                {
                    logger.logComment("Parent of this is loose original...: " + seg);
                    SegmentLocation newLoc = mapper.mapSegmentLocation(new SegmentLocation(parentId,
                                                                                           seg.getFractionAlongParent()));
                    logger.logComment("Mapped to: " + newLoc);
                    Segment properParent = newCell.getSegmentWithId(newLoc.getSegmentId());
                    seg.setParentSegment(properParent);
                    seg.setFractionAlongParent(newLoc.getFractAlong());
                }
                else if (idsMappedTo.contains(parentId) && childrenToFix.contains(childId))
                {
                    logger.logComment("\n     We will have to do something about parent of: " + seg);

                    if (seg.getSection().getSectionName().equals(seg.getParentSegment().getSection().getSectionName()))
                    {
                        logger.logComment("In same section, ignoring...");
                    }
                    else
                    {
                        logger.logComment("Different sections...");

                        if (seg.getSegmentName().indexOf(secondCompSuffix)<0)
                        {
                            int origChild = mapper.getFromSegmentId(seg.getSegmentId());

                            if (origChild > 0)
                            {
                                logger.logComment("origChild: " + origChild);
                                Segment origChildSeg = originalCell.getSegmentWithId(origChild);
                                logger.logComment("origChildSeg: " + origChildSeg);
                                SegmentLocation newLoc = mapper.mapSegmentLocation(new SegmentLocation(origChildSeg.
                                    getParentSegment().getSegmentId(),
                                    origChildSeg.getFractionAlongParent()));

                                logger.logComment("newLoc: " + newLoc);

                                if (seg.getSegmentId()==newLoc.getSegmentId())
                                {
                                    logger.logComment("Same ID!! Ignoring...");
                                }
                                else
                                {

                                    Segment properParent = newCell.getSegmentWithId(newLoc.getSegmentId());
                                    logger.logComment("   -----   properParent: " + properParent);
                                    seg.setParentSegment(properParent);
                                    seg.setFractionAlongParent(newLoc.getFractAlong());
                                }
                            }
                            else
                            {
                                logger.logComment("    Leaving parent of this as is: : " + seg);
                            }
                        }
                        else
                        {
                            logger.logComment("Its an added seg. Parent will already have been set...");
                        }
                    }
                }

                else
                {
                    logger.logComment("Relationship ok...");
                }

            }
        }

        // One more time...
        CellTopologyHelper.reorderSegsParentsFirst(newCell);





        return newCell;
    };

    private int getNextAlteredSegId()
    {
        int nextToUse = this.idOfAlteredSegs;
        idOfAlteredSegs++;
        return nextToUse;
    }

    /**
     * @return ArrayList of segIds of segs here after split
     */
    private ArrayList<Integer> splitSingleSegment(Cell cell, Segment origSegment, int num, boolean addMappings)
    {
        logger.logComment("Trying to split a segment into " + num + " sections");

        ArrayList<Integer> splitSegIds = new ArrayList<Integer> (num);

        if (num <= 1)
        {
            splitSegIds.add(origSegment.getSegmentId());
            return splitSegIds;
        }

        if (cell.getAllSegmentsInSection(origSegment.getSection()).size() > 1)
        {
            logger.logError("There is more than one segment in that section");
            return null;
        }
        int origSegId = origSegment.getSegmentId();
        float origSegLen = origSegment.getSegmentLength();

        Segment parentSeg = origSegment.getParentSegment();
        float origFractAlong = origSegment.getFractionAlongParent();
        Point3f origStart = origSegment.getStartPointPosition();
        Point3f origEnd = origSegment.getEndPointPosition();

        Section newSec = (Section) origSegment.getSection().clone();

        Point3f end = null;

        //ArrayList<Integer> splitSegIds = new ArrayList<Integer>();


        for (int i = 0; i < num - 1; i++)
        {
            logger.logComment("---------  Adding new seg number: " + i);

            Segment newSeg = (Segment) origSegment.clone();

            newSeg.setSection(newSec);

            newSeg.setSegmentId(getNextAlteredSegId());

            splitSegIds.add(newSeg.getSegmentId());

            newSeg.setSegmentName(newSeg.getSegmentName() + extraSegSuffix+"_" + i);
            newSeg.setParentSegment(parentSeg);

            logger.logComment("Parent now: " + newSeg.getParentSegment());

            if (i == 0)
            {
                newSeg.setFractionAlongParent(origFractAlong);
                newSeg.getSection().setNumberInternalDivisions(1);
            }
            else
            {
                newSeg.setFractionAlongParent(1);
            }
            cell.getAllSegments().add(newSeg);

            logger.logComment("cell.getAllSegments() SIZE: " + cell.getAllSegments().size());

            float endFactor = ( (float) i + 1) / num;

            end = new Point3f( (origEnd.x * endFactor) + ( (1 - endFactor) * origStart.x),
                              (origEnd.y * endFactor) + ( (1 - endFactor) * origStart.y),
                              (origEnd.z * endFactor) + ( (1 - endFactor) * origStart.z));

            newSeg.setEndPointPositionX(end.x);
            newSeg.setEndPointPositionY(end.y);
            newSeg.setEndPointPositionZ(end.z);

            logger.logComment("   Created new seg: " + newSeg);
            logger.logComment("   It's section   : " + newSeg.getSection());

            parentSeg = newSeg;

        }

        origSegment.setParentSegment(parentSeg);
        origSegment.setFractionAlongParent(1);
        origSegment.setSection(newSec);
        origSegment.setSegmentId(getNextAlteredSegId());

        splitSegIds.add(origSegment.getSegmentId());

        logger.logComment("   Redone orig seg: " + origSegment);
        logger.logComment("   It's section   : " + origSegment.getSection());
        logger.logComment("   isFirstSectionSegment   : " + origSegment.isFirstSectionSegment());

        logger.logComment("cell.getAllSegments(): " + cell.getAllSegments());
        logger.logComment("cell.getAllSections(): " + cell.getAllSections());

        if (addMappings)
        {
            SegmentRange origSegRange = new SegmentRange(origSegId, origSegLen, 0, 1);

            SegmentRange[] mappedSegs = new SegmentRange[splitSegIds.size()];

            for (int i = 0; i < splitSegIds.size(); i++)
            {
                mappedSegs[i] = new SegmentRange(splitSegIds.get(i),
                                                 origSegLen / (float) splitSegIds.size(), 0, 1);
            }

            mapper.addMapping(origSegRange, mappedSegs);
        }

        return splitSegIds;
    }

    public static void main(String[] args)
    {

        Project p = null;
        try
        {
            /// p = Project.loadProject(new File("projects/m2/m2.neuro.xml"), null);
            //p = Project.loadProject(new File("projects/Moff/Moff.neuro.xml"), null);
        //p = Project.loadProject(new File("projects/MMMm/MMMm.neuro.xml"), null);
            p = Project.loadProject(new File("examples/Ex10_MainenEtAl/Ex10-MainenEtAl.neuro.xml"), null);

            Cell cell = p.cellManager.getCell("SimplePurkinje");
            //Cell cell = p.cellManager.getCell("SampleCell");
            //Cell cell = p.cellManager.getCell("SampleCellOne");
            //Cell cell = p.cellManager.getCell("SampleCellBranch");
            //Cell cell = p.cellManager.getCell("MainenSomDend");
            //Cell cell = p.cellManager.getCell("MainenNeuroML");

            //cell.getSegmentWithId(1).getSection().setNumberInternalDivisions(1);

            System.out.println(CellTopologyHelper.printDetails(cell, p));
            GenesisCompartmentalisation gc = new GenesisCompartmentalisation();
            Cell c2 = gc.getCompartmentalisation(cell);


            //if (true) return;

            System.out.println(CellTopologyHelper.printDetails(c2, p));

            System.out.println("getSegmentMapper: " + gc.getSegmentMapper());

            //if (true) return;

           Vector<Segment> segs =  cell.getAllSegments();

           for (Segment seg: segs)
           {
               for (float fract = 0; fract <= 1 ; fract+=0.5f)
               {

                   SegmentLocation sl = new SegmentLocation(seg.getSegmentId(), fract);

                   Point3f p1 = CellTopologyHelper.convertSegmentDisplacement(cell, sl.getSegmentId(), sl.getFractAlong());

                   System.out.println("Old: " + sl + ", point: " + p1);

                   SegmentLocation slNew = gc.getSegmentMapper().mapSegmentLocation(sl);
                   Point3f p2 = CellTopologyHelper.convertSegmentDisplacement(c2, slNew.getSegmentId(), slNew.getFractAlong());
                   System.out.println("New: " + slNew + ", point: " + p2);

                   if (p1.equals(p2))
                   {
                       System.out.println("Equals!!");
                   }
                   else
                   {
                       System.out.println("\n\n\n                NOT Equals!!\n\n\n");
                       System.out.println("Old Segment: " + cell.getSegmentWithId(sl.getSegmentId()));
                       System.out.println("NewSegment: " + c2.getSegmentWithId(slNew.getSegmentId()));

                   }

               }
           }
        }
        catch (ProjectFileParsingException ex)
        {
            ex.printStackTrace();
        }

    }
}
