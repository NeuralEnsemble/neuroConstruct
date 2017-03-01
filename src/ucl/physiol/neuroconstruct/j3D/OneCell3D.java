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

package ucl.physiol.neuroconstruct.j3D;

import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.*;

import com.sun.j3d.utils.geometry.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.neuron.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.compartment.*;

/**
 * Class for generating TransformGroup containing a single cell. This class is
 * used once by OneCell3DPanel and multiple times when visualising networks
 * of cells (as in Main3DPanel)
 *
 * @author Padraig Gleeson
 *
 */

public class OneCell3D
{
    private ClassLogger logger = new ClassLogger("OneCell3D");

    /**
     * The cell to visualise
     */
    private Cell myCell = null;

    // To help in debugging multiple OneCell3D instances
    private int myIndex = -1;

    /**
     * The main TG relative to which all the segments are placed
     */
    private TransformGroup mainCellTG = null;

    /**
     * Contains the TGs of each of the cylindrical segments vs. segment id
     */
    private Hashtable<Integer, TransformGroup> segmentTGs = null;

    /**
     * The default colour to show the solid segments in
     */
    private Color currentDefaultSegmentColour = null;

    /**
     * If the whole cell is one colour this is non null...
     */
    private Appearance wholeCellAppearance = null;

    /**
     * To allow temporary change in Appearance
     */
    private Appearance cellAppBeforeTempSwitch = null;
    private Appearance cellAppDuringTempSwitch = null;

    float stickScalingToVanish = 0.0001f;

    /**
     * Reference to the project, for 3d settings etc
     */
    private Project project = null;

    /**
     * Contains the colourable elements for the segments vs. segment id
     */
    private Hashtable<Integer, Primitive> segmentPrimitives = null;

    /**
     * Contains the colourable elements for the synpases vs. synpases ref
     */
    private Hashtable<String, Primitive> synapsePrimitives = null;

    /**
     * Contains the coords in stickSegmentGeom for the segments vs. segment id
     */
    private Hashtable<Integer, Integer> segmentGeomCoords = null;

    /**
     * For when the neurites are displayed as lines
     */
    private LineArray stickSegmentGeom = null;

    /**
     * Colours for internal points for NEURON/GENESIS views
     */
    //private final Color genesisConnectionColour = Color.blue;
    //private final Color genesisMidPointColour = Color.red;
    private final Color neuronConnectionColour = Color.blue;
    private final Color neuronNsegPointColour = Color.red;


    private final Color3f logicalConnectionColour = new Color3f(Color.yellow);

    /**
     * Radius of internal points for NEURON/GENESIS views
     */
    private final float internalPointRadius = 0.5f;


    /**
     * If the display needs to be cancelled. Has crashed KDE if diam of segment infinite...
     */
    private boolean cancelled = false;


    /**
     * Can't be instantiated without cell and project objects
     */
    private OneCell3D()
    {
    }

    /**
     * Main constructor.
     * @param cell The Cell to show in 3D
     * @param project the project, containing among other things the 3D settings
     */
    public OneCell3D(Cell cell, int index, Project project)
    {
        //logger.setThisClassVerbose(true);
        this.myCell = cell;
        this.myIndex = index;

        this.project = project;

        logger.logComment("        ---------       OneCell3D created for cell: " + cell);

        logger.logComment("with " + cell.getAllSegments().size() +" segments");

        currentDefaultSegmentColour = project.proj3Dproperties.getCellColour3D();

        if (currentDefaultSegmentColour==null)
            currentDefaultSegmentColour = GeneralProperties.getDefaultCellColor3D();

        segmentTGs = new Hashtable<Integer, TransformGroup>(cell.getAllSegments().size());
        segmentPrimitives = new Hashtable<Integer, Primitive>(cell.getAllSegments().size());

        wholeCellAppearance = getDefaultSegmentApp();

    }


    public final Appearance getDefaultSegmentApp()
    {
        if (!shiny())
            return Utils3D.getDullObjectAppearance(currentDefaultSegmentColour);
        else if (transparent())
            return Utils3D.getTransparentObjectAppearance(currentDefaultSegmentColour, 0.8f);
        else
            return Utils3D.getGeneralObjectAppearance(currentDefaultSegmentColour);
    }


    public Color getDefaultSegmentColour()
    {
        return currentDefaultSegmentColour;
    }




    public Cell getDisplayedCell()
    {
        return this.myCell;
    }


    /**
     * Depending on the display option in Display3DProperties of the project, should the soma segments
     * be shown
     * @return true if they are to be shown
     */
    public boolean showSomaDiam()
    {
        return
            this.showingProjection() ||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID_UNSHINY)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_SOLID_NEURITE_NONE)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_SOLID_NEURITE_LINE)/*||
            //project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_NEURON)||
            //project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_SIMPLE)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_MULTI)*/;
    }

    /**
     * Depending on the display option in Display3DProperties of the project, should the neurite segments
     * be shown as solid cylinders
     * @return true if they are to be shown
     */
    public boolean showNeuriteDiam()
    {
        return
            this.showingProjection() ||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID_UNSHINY)/*||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_NEURON)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_SIMPLE)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_MULTI)*/;
    }

    /**
     * Depending on the display option in Display3DProperties of the project, should the neurite segments
     * be shown as lines
     * @return true if sticks are to be shown
     */
    public boolean showSticks()
    {
        return
            this.showingProjection() ||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_LINE_NEURITE_LINE)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_SOLID_NEURITE_LINE)/*||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_NEURON)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_SIMPLE)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_MULTI)*/;
    }

    /**
     * Depending on the display option in Display3DProperties of the project, should the segments be
     * transparent
     * @return true if segments are to be transparent
     */
    public boolean transparent()
    {
        return this.showingProjection();
        /*
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_NEURON)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_SIMPLE)||
            project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_GENESIS_MULTI)*/
    }

    /**
     * Show one of the projections
     */
    public boolean showingProjection()
    {
        return project.proj3Dproperties.isCompartmentalisationDisplay();
    }

    /**
     * Depending on the display option in Display3DProperties of the project, should the segments be
     * shiny, i.e if false, a clearer picture of the true colour of the segments will emerge
     * @return true if segments are to be shiny
     */
    public boolean shiny()
    {

        logger.logComment("proj3Dproperties: "+ project.proj3Dproperties);
        return
            !project.proj3Dproperties.getDisplayOption().equals(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID_UNSHINY);
    }




    /**
     * Used once by OneCell3DPanel when displaying a single cell, or multiple times by
     * Main3DPanel for networks
     * @return TransformGroup containing all of the 3D objects
     */
    public TransformGroup createCellTransformGroup()
    {
        //GeneralUtils.timeCheck("Start creating TransformGroup");

        mainCellTG = new TransformGroup();

        logger.logComment("");
        logger.logComment("---  Adding The Segments...");
        logger.logComment("");

        Vector allSegs = this.myCell.getAllSegments();

        int segSize = allSegs.size();

        this.addAxonalArbours(mainCellTG);


        if (showSticks() && segSize > 0)
        {
            stickSegmentGeom = new LineArray(segSize * 2,
                                             GeometryArray.COORDINATES
                                             | GeometryArray.COLOR_3);

            segmentGeomCoords = new Hashtable<Integer, Integer>(segSize);

            stickSegmentGeom.setCapability(LineArray.ALLOW_COLOR_WRITE);
            stickSegmentGeom.setCapability(LineArray.ALLOW_COORDINATE_READ);
            stickSegmentGeom.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
            stickSegmentGeom.setCapability(LineArray.ALLOW_COUNT_READ);
        }

        Hashtable<String, Vector3f> sectionEndPoints = new Hashtable<String, Vector3f>();

        ArrayList<Section> allSections = this.myCell.getAllSections();
        int secSize = allSections.size();


        int nextStickNumber = 0;


        //GeneralUtils.timeCheck("Step 1..");

        for (int currSectionIndex = 0; currSectionIndex < secSize; currSectionIndex++)
        {
            Section currSection = allSections.get(currSectionIndex);

            /** @todo !! Possible optimization by caching these arranged by section... */
            LinkedList<Segment> allSegsInSec = this.myCell.getAllSegmentsInSection(currSection);



            for (int currSegIndex = 0; currSegIndex < allSegsInSec.size(); currSegIndex++)
            {

                Segment currSegment = allSegsInSec.get(currSegIndex);

                logger.logComment("");
                logger.logComment("****************    Looking at segment id " + currSegment.getSegmentId()
                                  + ", called: " + currSegment.getSegmentName()
                                  + "   ****************");

                Segment parent = currSegment.getParentSegment();

                TransformGroup tgOfParent = null;
                TransformGroup addedSegmentTG = mainCellTG;

                // Creating internal points
                if (this.showingProjection())
                {
                    if (currSegment.isFirstSectionSegment())
                    {
                        Utils3D.addSphereAtLocation(internalPointRadius,
                                                    new Vector3f(currSegment.getStartPointPosition()),
                                                    mainCellTG,
                                                    neuronConnectionColour);

                        int nseg = currSegment.getSection().getNumberInternalDivisions();

                        for (int i = 1; i <= nseg; i++)
                        {
                            float distToMidSeg = (float) ( (2 * i) - 1) / (float) (2 * nseg);
                            float distToEndSeg = (float) i / (float) (nseg);

                            /** @todo Optimization: make this faster, though should not be used
                             * when displaying more than one cell... */
                            Point3f segMidPoint =
                                CellTopologyHelper.convertSectionDisplacement(this.myCell,
                                currSegment.getSection(),
                                distToMidSeg);

                            logger.logComment("Adding nseg mid point num " + i + " dist "
                                              + distToMidSeg + ": " + segMidPoint);

                            Utils3D.addSphereAtLocation(internalPointRadius,
                                                        new Vector3f(segMidPoint),
                                                        mainCellTG,
                                                        neuronNsegPointColour);
                            if (i < nseg)
                            {
                                Point3f segEndPoint =
                                    CellTopologyHelper.convertSectionDisplacement(this.myCell,
                                    currSegment.getSection(),
                                    distToEndSeg);

                                logger.logComment("Adding nseg end point num " + i
                                                  + " dist " + distToEndSeg + ": " + segEndPoint);

                                Utils3D.addSphereAtLocation(internalPointRadius / 2f,
                                                            new Vector3f(segEndPoint),
                                                            mainCellTG,
                                                            neuronConnectionColour);
                            }
                        }

                        if (currSegment.getParentSegment()!=null)
                        {
                            Point3f connPointOnParent = CellTopologyHelper.convertSegmentDisplacement(this.myCell,
                                currSegment.getParentSegment().getSegmentId(), currSegment.getFractionAlongParent());

                            Point3f startPoint = currSegment.getStartPointPosition();

                            if (connPointOnParent.x != startPoint.x ||
                                connPointOnParent.y != startPoint.y ||
                                connPointOnParent.z != startPoint.z)
                            {
                                logger.logComment("Drawing a line from " + connPointOnParent + " to " + startPoint + " for "+ currSegment);

                                LineArray connectStick = new LineArray(2,
                                                                       GeometryArray.COORDINATES
                                                                       | GeometryArray.COLOR_3);

                                connectStick.setCapability(LineArray.ALLOW_COLOR_WRITE);
                                connectStick.setCapability(LineArray.ALLOW_COORDINATE_READ);
                                connectStick.setCapability(LineArray.ALLOW_COUNT_READ);

                                connectStick.setCoordinate(0, connPointOnParent);
                                connectStick.setCoordinate(1, startPoint);

                                connectStick.setColor(0, logicalConnectionColour);
                                connectStick.setColor(1, logicalConnectionColour);

                                Shape3D connShape = new Shape3D(connectStick);
                                mainCellTG.addChild(connShape);

                            }

                        }
                    }

                    sectionEndPoints.put(currSegment.getSection().getSectionName(),
                                         new Vector3f(currSegment.getEndPointPosition()));
                }


                // Adding the first soma segment
                if (showSomaDiam() &&
                    (currSegment.isSomaSegment() && currSegment.isFirstSectionSegment()))
                {

                    logger.logComment("Adding a solid segment 0: "+ currSegment);

                    addedSegmentTG = addFirstSomaSeg(currSegment);


                    logger.logComment("------    Added soma");
                    Utils3D.printTransformGroupDetails(addedSegmentTG, "Soma TG");

                    // note ID might not necessarily be zero...
                    segmentTGs.put(currSegment.getSegmentId(), addedSegmentTG);
                    logger.logComment("segmentTGs: " + segmentTGs);
                }

                if (showNeuriteDiam() || showSomaDiam() || showSticks())
                {
                    if (parent == null)
                    {
                        logger.logComment("Using mainCellTG for parent TG");
                        tgOfParent = mainCellTG;
                    }
                    else
                    {
                        logger.logComment("Using TG of parent: " + parent);
                        logger.logComment("TGs already stored for ids: " + segmentTGs.keySet());

                        /** @todo !! Optimization: perhaps store last TransformGroup, check id of parent and use this? */
                        tgOfParent = segmentTGs.get(new Integer(parent.getSegmentId()));

                    }

                    if (showNeuriteDiam() ||
                        (showSomaDiam() && currSegment.isSomaSegment()))
                    {
                        // Only add Segment if it's not already added as FIRST soma segment
                        if (! (currSegment.isFirstSectionSegment() && currSegment.isSomaSegment()))
                        {
                            logger.logComment("Adding a solid segment 1: "+ currSegment);
                            addedSegmentTG = addPositionedSegment(currSegment, tgOfParent);

                            if (cancelled) return null;

                            segmentTGs.put(currSegment.getSegmentId(), addedSegmentTG);
                        }

                    }
                    if (showSticks())
                    {
                        logger.logComment("Adding a stick...");

                        nextStickNumber = addPositionedStick(currSegment,
                                                             stickSegmentGeom,
                                                             nextStickNumber);
/*
                        // adding the remaining soma segments as solid if we are only showing soma solid
                        if (currSegment.isSomaSegment()
                            && !currSegment.isFirstSectionSegment()
                            && showSomaDiam())
                        {
                            if (!segmentTGs.contains(currSegment.getSegmentId()))
                            {
                                logger.logComment("Adding a solid segment 2: "+ currSegment, true);


                                addedSegmentTG = addPositionedSegment(currSegment, tgOfParent);

                                if (cancelled) return null;

                                segmentTGs.put(currSegment.getSegmentId(), addedSegmentTG);
                            }

                        }*/
                    }
                }
            }

        }

        //GeneralUtils.timeCheck("Step 4..");

        // adding the section endpoints
        if (this.showingProjection())
        {
            Enumeration<Vector3f> enumeration = sectionEndPoints.elements();
            while (enumeration.hasMoreElements())
            {
                Vector3f endPoint = enumeration.nextElement();

                Utils3D.addSphereAtLocation(0.5f,
                                            endPoint,
                                            mainCellTG,
                                            Color.BLUE);
            }
        }

        // adding the newly created sticks...
        if (showSticks() && this.myCell.getAllSegments().size() > 0)
        {
            Shape3D stickShape = new Shape3D(stickSegmentGeom);
            mainCellTG.addChild(stickShape);
        }

        logger.logComment("---  Finished adding the Segments...");

        //GeneralUtils.timeCheck("Finished creating TransformGroup");

        return mainCellTG;
    }


    private TransformGroup addFirstSomaSeg(Segment soma)
    {
        logger.logComment("_____       Adding first soma segment...   ");

        if (soma.getSegmentShape() == Segment.CYLINDRICAL_SHAPE /*||
            neuronStyle()*/)
        {
            Point3f startPosition = soma.getSection().getStartPointPosition();
            Point3f endPosition = soma.getEndPointPosition();

            if (/*neuronStyle() && */soma.getSegmentShape() == Segment.SPHERICAL_SHAPE)
            {
                startPosition = NeuronTemplateGenerator.getSphericalSegmentStartPoint(soma, this.myCell);
                endPosition =  NeuronTemplateGenerator.getSphericalSegmentEndPoint(soma, this.myCell);
            }

            logger.logComment(" ***  Adding a cylinder of radius " + soma.getRadius()
                              + " starting at point: "+ Utils3D.getShortStringDesc(startPosition));

            TransformGroup tgToStart = new TransformGroup();
            Vector3f vectorToStart = new Vector3f(startPosition);
            Transform3D t3dToStart = new Transform3D();
            t3dToStart.setTranslation(vectorToStart);
            tgToStart.setTransform(t3dToStart);


            mainCellTG.addChild(tgToStart);

            Vector3f parallelVector = new Vector3f(endPosition);
            parallelVector.sub(startPosition);

            float length = parallelVector.length();

            Vector3f halfMove = new Vector3f(parallelVector);
            halfMove.scale(0.5f);

            Vector3f yAxis = new Vector3f(0, 1, 0);

            AxisAngle4f angle4 = Utils3D.getAxisAngle(yAxis, parallelVector);

            Transform3D shiftAlongFirstHalf = new Transform3D();
            shiftAlongFirstHalf.setRotation(angle4);
            shiftAlongFirstHalf.setTranslation(halfMove);

            TransformGroup tgToMidpoint = new TransformGroup(shiftAlongFirstHalf);
            tgToStart.addChild(tgToMidpoint);

            float endRadius = soma.getRadius();
            float startRadius = soma.getSection().getStartRadius();
/*
            if (genesisSimpleStyle() || genesisMultiCompStyle())
            {

                float equivCylinderRadius
                    = (float)CompartmentHelper.getEquivalentRadius(startRadius, endRadius, soma.getSegmentLength());

                startRadius = equivCylinderRadius;
                endRadius = equivCylinderRadius;

            }*/
            float minRadius = project.proj3Dproperties.getMinRadius();


            ConicalFrustum somaPrimitive = new ConicalFrustum(Math.max(startRadius, minRadius), Math.max(endRadius, minRadius), length,
                                                         ConicalFrustum.GENERATE_NORMALS |
                                                         ConicalFrustum.GENERATE_TEXTURE_COORDS |
                                                         ConicalFrustum.ENABLE_APPEARANCE_MODIFY,
                                                         project.proj3Dproperties.getResolution3DElements(),
                                                         getDefaultSegmentApp());


            tgToMidpoint.addChild(somaPrimitive);


            segmentPrimitives.put(new Integer(soma.getSegmentId()), somaPrimitive);

            Transform3D shiftAlongFullSegment = new Transform3D();
            shiftAlongFullSegment.setTranslation(parallelVector);

            Transform3D t3dToEnd = new Transform3D();

            Vector3f vectorToEnd = new Vector3f(vectorToStart);
            vectorToEnd.add(parallelVector);
            t3dToEnd.setTranslation(vectorToEnd);

            TransformGroup tgToEnd = new TransformGroup(t3dToEnd);

            logger.logComment("Returning : ");
            Utils3D.printTransformGroupDetails(tgToEnd, "tgToEnd");

            if (/*neuronStyle() && */soma.getSegmentShape() == Segment.SPHERICAL_SHAPE)
            {
                TransformGroup tgToRealSomaCentre = new TransformGroup();
                Vector3f vectorToRealSomaCentre = new Vector3f(soma.getStartPointPosition());
                Transform3D t3dToRealSomaCentre = new Transform3D();
                t3dToRealSomaCentre.setTranslation(vectorToRealSomaCentre);
                tgToRealSomaCentre.setTransform(t3dToRealSomaCentre);

                mainCellTG.addChild(tgToRealSomaCentre);
                return tgToRealSomaCentre;

            }
            return tgToEnd;
        }
        else if (soma.getSegmentShape() == Segment.SPHERICAL_SHAPE)
        {
            logger.logComment(" ***  Adding a sphere of radius " + soma.getRadius());

            TransformGroup tg1 = new TransformGroup();

            if (soma.getStartPointPosition().distance(new Point3f(0,0,0)) != 0)
            {
                //TransformGroup tgToStart = new TransformGroup();
                Vector3f vectorToStart = new Vector3f(soma.getStartPointPosition());
                Transform3D t3dToStart = new Transform3D();
                t3dToStart.setTranslation(vectorToStart);
                tg1.setTransform(t3dToStart);
            }


            mainCellTG.addChild(tg1);

            float minRadius = project.proj3Dproperties.getMinRadius();

            Primitive somaPrimitive = new Sphere(Math.max(soma.getRadius(), minRadius),
                                                 Sphere.GENERATE_NORMALS |
                                                 Sphere.GENERATE_TEXTURE_COORDS |
                                                 Sphere.ENABLE_APPEARANCE_MODIFY,
                                                 project.proj3Dproperties.getResolution3DElements(),
                                                 getDefaultSegmentApp());

            tg1.addChild(somaPrimitive);


            segmentPrimitives.put(new Integer(soma.getSegmentId()), somaPrimitive);

            return tg1;
        }
        else
        {
            logger.logError("Unknown shape of soma...");
            return null;
        }
    }


    /**
     * Adding axonal arbours
     */
    private void addAxonalArbours(TransformGroup mainTG)
    {
        if (!project.proj3Dproperties.getShowAxonalArbours())
        {
            logger.logComment("No need to show em...");
            return;
        }

        Vector<AxonalConnRegion> aas = myCell.getAxonalArbours();

        for (AxonalConnRegion aa: aas)
        {
            aa.getRegion().addPrimitiveForRegion(mainTG, Utils3D.getTransparentObjectAppearance(Color.white, 0.85f));
        }

    }

    public void setSynapsePrimitive(String synRef, Primitive prim)
    {
        if (synapsePrimitives == null)
            synapsePrimitives = new Hashtable<String, Primitive>(10); // in preparation...

        this.synapsePrimitives.put(synRef, prim);
    }


    /**
     * Add a solid (i.e. conical fustrum or sphere) segment at a particular point
     * @param segment the Segment to add
     * @param parentTG The Transform group of the parent segment
     * @return The new TransformGroup corresponding to the endpoint of this
     * segment or null the segment cannot be added
     */
    TransformGroup addPositionedSegment(Segment segment,
                                        TransformGroup parentTG)
    {
        logger.logComment("---   Adding positioned segment: "+ segment);
        logger.logComment("---   Positioned segment section: "+ segment.getSection());
        logger.logComment("---   Positioned segment parent: "+ segment.getParentSegment());


        if (Float.isInfinite(segment.getRadius())||Float.isInfinite(segment.getSection().getStartRadius()))
        {
            GuiUtils.showErrorMessage(logger, "Warning cannot display in 3D:\n"+segment+"\n"+segment.getSection()
                                      , null, null);
            cancelled = true;
            return null;
        }

        TransformGroup newElementTG = null; // TG for element to be placed at, i.e. up to midpoint of segment
        TransformGroup endPointTG = null; // TG to return as 'parent' TG, i.e. to end of segment

        Transform3D newElementT3D = null;

        Transform3D parentT3D = new Transform3D();
        parentTG.getTransform(parentT3D);


        // This may be due to segment.getFractionAlongParent() != 1
        // or just funny points read in from a hoc file
        Vector3f displacementDueToStartPoint
            = new Vector3f(segment.getStartPointPosition());

        if (segment.getParentSegment()!=null)
            displacementDueToStartPoint.sub(segment.getParentSegment().getEndPointPosition());


        // transform from end of parent to start of child
        // NOTE: Usually zero vector when segments are simply connected
        Transform3D shiftToStartPointChild = new Transform3D();
        shiftToStartPointChild.setTranslation(displacementDueToStartPoint);

        // Get part for difference between parent end & child start
        newElementT3D = new Transform3D(shiftToStartPointChild);
        // Get part for parent end
        newElementT3D.mul(parentT3D);

        logger.logComment(">>>   Shift to correct start point disp: "+ Utils3D.getShortStringDesc(displacementDueToStartPoint));

        Matrix3d unitRotationMatrix = new Matrix3d();
        unitRotationMatrix.m00 = 1;
        unitRotationMatrix.m11 = 1;
        unitRotationMatrix.m22 = 1;

        newElementT3D.setRotation(unitRotationMatrix);

        // get vector parallel & same length as new segment
        Point3f minusStartPoint = new Point3f(segment.getStartPointPosition());
        minusStartPoint.negate();
        Point3f relPoint = new Point3f(segment.getEndPointPosition());
        relPoint.add(minusStartPoint);
        Vector3f parallelVector = new Vector3f(relPoint);

        // get length of vector
        float length = segment.getStartPointPosition().distance(segment.getEndPointPosition());



        logger.logComment("Adding segment ("+segment.getSegmentName()+") type: "+ segment.getClass());
        logger.logComment("Len: " + Utils3D.trimDouble(length)
                          + ", rad: " + segment.getRadius()
                          + ", " + Utils3D.getShortStringDesc(segment.getStartPointPosition())
                          + " -> " + Utils3D.getShortStringDesc(segment.getEndPointPosition())
                          + ", parallel to: " + Utils3D.getShortStringDesc(parallelVector));


        Vector3f yAxis = new Vector3f(0, 1, 0);

        AxisAngle4f angle4 = Utils3D.getAxisAngle(yAxis, parallelVector);

        newElementT3D.setRotation(angle4);

        Vector3f halfMove = new Vector3f(0, length / 2, 0);

        Vector3f totalDisplacement = new Vector3f(halfMove);

        Transform3D shift = new Transform3D();
        shift.setTranslation(totalDisplacement);

        newElementT3D.mul(shift);

        newElementTG = new TransformGroup(newElementT3D);

        float endRadius = segment.getRadius();
        float startRadius;

        if (segment.isFirstSectionSegment())
            startRadius = segment.getSection().getStartRadius();
        else
            startRadius = segment.getParentSegment().getRadius();


        logger.logComment("\nCalculated positions, now to decide on the shape...");

       // logger.logComment("segment.isFirstSectionSegment(): "+ segment.isFirstSectionSegment()+
      //                    ", genesisSimpleStyle(): "+ genesisSimpleStyle()+
       //                   ", genesisMultiCompStyle(): "+ genesisMultiCompStyle()+
        //                  ", neuronStyle(): "+ neuronStyle());


        if (segment.getSegmentShape()==Segment.CYLINDRICAL_SHAPE)
        {
            if (false /*genesisSimpleStyle() ||
                (genesisMultiCompStyle() &&
                 startRadius == endRadius)*/)
            {
                 logger.logComment("Adding simple cylinder...");
                Primitive newPrim = null;

                float equivCylinderRadius
                    = (float) CompartmentHelper.getEquivalentRadius(startRadius, endRadius, segment.getSegmentLength());

                 float minRadius = project.proj3Dproperties.getMinRadius();

                newPrim = new ConicalFrustum(Math.max(equivCylinderRadius, minRadius),
                                              Math.max(equivCylinderRadius, minRadius),
                                              length,
                                              ConicalFrustum.GENERATE_NORMALS |
                                              ConicalFrustum.GENERATE_TEXTURE_COORDS |
                                              ConicalFrustum.ENABLE_APPEARANCE_MODIFY,
                                              project.proj3Dproperties.getResolution3DElements(),
                                              getDefaultSegmentApp());

                newElementTG.addChild(newPrim);
                segmentPrimitives.put(new Integer(segment.getSegmentId()), newPrim);

            }

            else
            {
                 logger.logComment("Adding ConicalFrustum...");

                 float minRadius = project.proj3Dproperties.getMinRadius();

                ConicalFrustum conFru
                    = new ConicalFrustum(Math.max(startRadius, minRadius),
                                          Math.max(endRadius, minRadius) ,
                                          length,
                                          ConicalFrustum.GENERATE_NORMALS |
                                          ConicalFrustum.GENERATE_TEXTURE_COORDS |
                                          ConicalFrustum.ENABLE_APPEARANCE_MODIFY,
                                          project.proj3Dproperties.getResolution3DElements(),
                                          getDefaultSegmentApp());

                newElementTG.addChild(conFru);
                segmentPrimitives.put(new Integer(segment.getSegmentId()), conFru);

            }
        }
        else
        {
            logger.logComment("Adding Sphere...");

            float minRadius = project.proj3Dproperties.getMinRadius();

            Primitive newPrim = null;
            newPrim = new Sphere(Math.max(segment.getRadius(), minRadius),
                                 Sphere.GENERATE_NORMALS |
                                 Sphere.GENERATE_TEXTURE_COORDS |
                                 Sphere.ENABLE_APPEARANCE_MODIFY,
                                 project.proj3Dproperties.getResolution3DElements(),
                                 getDefaultSegmentApp());

            newElementTG.addChild(newPrim);
            segmentPrimitives.put(new Integer(segment.getSegmentId()), newPrim);


        }

        mainCellTG.addChild(newElementTG);


        newElementT3D.mul(shift);

        endPointTG = new TransformGroup(newElementT3D);

        return endPointTG;
    }



    int addPositionedStick(Segment segment,
                                     /* TransformGroup parentTG,*/
                                      LineArray stickGeom,
                                      int stickNumber)
    {

        TransformGroup newElementTG = null; // TG for element to be placed at, i.e. up to midpoint of cylinder
        TransformGroup endPointTG = null; // TG to return as 'parent' TG, i.e. to end of cyl

        Transform3D newElementT3D = new Transform3D();

        Matrix3d unitRotationMatrix = new Matrix3d();
        unitRotationMatrix.m00 = 1;
        unitRotationMatrix.m11 = 1;
        unitRotationMatrix.m22 = 1;

        newElementT3D.setRotation(unitRotationMatrix);

        // get vector parallel & same length as new cylinder
        Point3f minusStartPoint = new Point3f(segment.getStartPointPosition());
        minusStartPoint.negate();
        Point3f relPoint = new Point3f(segment.getEndPointPosition());
        relPoint.add(minusStartPoint);
        Vector3f parallelVector = new Vector3f(relPoint);

        // get length of vector
        double length = segment.getStartPointPosition().distance(segment.getEndPointPosition());

        logger.logComment(" Adding segment: " + segment.getSegmentName()
                          + ", id: "
                          + segment.getSegmentId()
                          + ", len: "
                          + Utils3D.trimDouble(length)
                          + " from: "
                          + Utils3D.getShortStringDesc(segment.getStartPointPosition())
                          + " to: "
                          + Utils3D.getShortStringDesc(segment.getEndPointPosition())
                          + " parallel to: "
                          + Utils3D.getShortStringDesc(parallelVector));

        // See how far the y-vector's been transformed
        Vector3f yAxis = new Vector3f(0, 1, 0);

        AxisAngle4f angle4 = Utils3D.getAxisAngle(yAxis, parallelVector);

        newElementT3D.setRotation(angle4);

        Vector3d halfMove = new Vector3d(0, length / 2, 0);
        Transform3D shiftAlongHalfWay = new Transform3D();
        shiftAlongHalfWay.setTranslation(halfMove);

        newElementT3D.mul(shiftAlongHalfWay);

        newElementTG = new TransformGroup(newElementT3D);

        //int coordsSoFar
        logger.logComment("Size of Geom array: " + stickGeom.getVertexCount());

        int newStickStartIndex = stickNumber * 2;
        int newStickEndIndex = (stickNumber * 2) + 1;

        stickGeom.setCoordinate(newStickStartIndex, segment.getStartPointPosition());
        stickGeom.setCoordinate(newStickEndIndex, segment.getEndPointPosition());

        logger.logComment("Added new coords at points: "+ newStickStartIndex
                          + " and "+ newStickEndIndex);

        segmentGeomCoords.put(segment.getSegmentId(), newStickStartIndex);

        Color3f colourOfStick = new Color3f(currentDefaultSegmentColour);

        stickGeom.setColor(newStickStartIndex, colourOfStick);
        stickGeom.setColor(newStickEndIndex, colourOfStick);

        mainCellTG.addChild(newElementTG);

        newElementT3D.mul(shiftAlongHalfWay);

        endPointTG = new TransformGroup(newElementT3D);

        logger.logComment("Returning: ");
        Utils3D.printTransformGroupDetails(endPointTG, "endPointTG");

        return stickNumber+1;
    }


    public void setSynapseAppearance(Appearance app, String synRef)
    {
        logger.logComment("Setting app of synapse: " + synRef);

        if (synRef == null)
        {
            logger.logError("Null synRef...");
            return;
        }
        Primitive prim = this.synapsePrimitives.get(synRef);

        if (prim != null)
        {
            if (!prim.getAppearance().equals(app))
            {
                prim.setAppearance(app);
            }
        }

    }



    public void setSegmentAppearance(Appearance app, int segId)//Segment segment)
    {
        logger.logComment("Setting app of segment: "+ segId);

        if (segId == -1)
        {
            logger.logError("Null segment...");
            return;

        }

        if (showSticks() && stickSegmentGeom!=null)
        {
            Color3f color = new Color3f();
            app.getMaterial().getDiffuseColor(color);

            int stickStartIndex =
                (segmentGeomCoords.get(segId)).intValue();

            stickSegmentGeom.setColor(stickStartIndex, color);
            stickSegmentGeom.setColor(stickStartIndex + 1, color);

            // if the segment is finite vol, then paint the solid cylinder too...

            if (segmentPrimitives.get(segId) != null)
            {

                Primitive segmentShape = segmentPrimitives.get(segId);

                if (!segmentShape.getAppearance().equals(app))
                {
                    logger.logComment("Setting the color of the finite vol cylinder of segment: "+segId);
                    //Appearance tempApp = Utils3D.getGeneralObjectAppearance(Color.green);
                    segmentShape.setAppearance(app);
                }

            }
        }
        else if (showNeuriteDiam() || showSomaDiam())
        {
            Primitive segmentShape
                = segmentPrimitives.get(segId);

            if (segmentShape!=null && !segmentShape.getAppearance().equals(app))
            {
                segmentShape.setAppearance(app);
            }
        }

        if (wholeCellAppearance != null && !wholeCellAppearance.equals(app))
        {
            wholeCellAppearance = null;
        }

    }



    public void setDefaultCellAppearance(Color color)
    {
        logger.logComment("Resetting cell color to: "+ color);
        currentDefaultSegmentColour = new Color(color.getRed(),color.getGreen(), color.getBlue());

        setWholeCellAppearance(getDefaultSegmentApp());
        cellAppBeforeTempSwitch = null;
    }


    public boolean hasTempAppearance()
    {
        return cellAppDuringTempSwitch!=null;
    }




    public void setWholeCellAppearance(Appearance app)
    {

        logger.logComment("Setting appearance of cell with " + segmentPrimitives.size() + " segments: "+ app.hashCode());

        if (showSomaDiam())
        {
            Enumeration allPrims = segmentPrimitives.elements();

            while (allPrims.hasMoreElements())
            {
                Object nextObj = allPrims.nextElement();
                if (nextObj !=null && nextObj instanceof Primitive)
                {
                    ((Primitive)nextObj).setAppearance(app);
                }
            }
        }

        if (showSticks())
        {
            logger.logComment("Resetting sticks...");
            Color3f newColor = new Color3f();
            app.getMaterial().getDiffuseColor(newColor);
            if (stickSegmentGeom!=null) // reset could be called before the sticks are created...
            {
                int numVerts = stickSegmentGeom.getValidVertexCount();
                for (int i = 0; i < numVerts; i++)
                {
                    stickSegmentGeom.setColor(i, newColor);
                }
            }
        }

        wholeCellAppearance = app;
    }

    /*
     * Set a temporary appearance to all segments. Useful for e.g. setting cell transparent
     */
    public void setTempWholeCellAppearance(Appearance app)
    {
        cellAppDuringTempSwitch = app;

        Primitive primarySomaPrimitive = segmentPrimitives.elements().nextElement();

        cellAppBeforeTempSwitch = primarySomaPrimitive.getAppearance();

        setWholeCellAppearance(app);

        if (showSticks() && Utils3D.isTransparent(app))
        {
            logger.logComment("Vanishing sticks for "+ myIndex);

            if (stickSegmentGeom!=null)
            {
                int numVerts = stickSegmentGeom.getValidVertexCount();
                for (int i = 0; i < numVerts; i++)
                {
                    //TODO: Get a better way to temproarily get rid of the sticks
                    Point3f p = new Point3f();
                    stickSegmentGeom.getCoordinate(i, p);
                    p.scale(stickScalingToVanish);
                    stickSegmentGeom.setCoordinate(i, p);
                }
            }
        }
    }

    public void resetCellAppearance()
    {
        logger.logComment("-------------                Resetting cell app   ");

        if (cellAppBeforeTempSwitch == null || cellAppDuringTempSwitch == null)
        {
            logger.logComment("There wasn't any temp colour, resetting to default: " + currentDefaultSegmentColour);
            setWholeCellAppearance(getDefaultSegmentApp());

        }
        else
        {
            logger.logComment("Resetting to colour before temp...: " + cellAppBeforeTempSwitch);
            setWholeCellAppearance(cellAppBeforeTempSwitch);


            if (showSticks() && Utils3D.isTransparent(cellAppDuringTempSwitch))
            {
                logger.logComment("Reconstructing sticks for "+ myIndex);

                if (stickSegmentGeom!=null)
                {
                    int numVerts = stickSegmentGeom.getValidVertexCount();
                    for (int i = 0; i < numVerts; i++)
                    {
                        //TODO: Get a better way to temproarily get rid of the sticks
                        Point3f p = new Point3f();
                        stickSegmentGeom.getCoordinate(i, p);
                        p.scale(1/stickScalingToVanish);
                        stickSegmentGeom.setCoordinate(i, p);
                    }
                }
            }
        }

        cellAppBeforeTempSwitch = null;
        cellAppDuringTempSwitch = null;
    }


    public int hasPrimitive(Primitive prim)
    {
        if (segmentPrimitives.contains(prim))
        {
            Enumeration enumeration = segmentPrimitives.keys();
            while (enumeration.hasMoreElements()) {

                Integer next = (Integer)enumeration.nextElement();
                if (segmentPrimitives.get(next).equals(prim))
                    return next.intValue();
            }
            return -1;
        }
        else
        {
            return -1;
        }
    }

    public Segment markPrimitiveAsSelected(Primitive prim,
                                           Color colourSelectedSegment)
    {
        if (prim==null)
        {
            return markSegmentAsSelected(-1, colourSelectedSegment);
        }
        if (segmentPrimitives.contains(prim))
        {
            Enumeration enumeration = segmentPrimitives.keys();
            while (enumeration.hasMoreElements())
            {
                Integer id = (Integer)enumeration.nextElement();

                if (segmentPrimitives.get(id).equals(prim))

                return markSegmentAsSelected(id.intValue(), colourSelectedSegment);
            }

        }
        return null;
    }

    public Segment getSelectedPrimitive(Primitive prim)
    {
        if (prim==null)
        {
            return null;
        }
        if (segmentPrimitives.contains(prim))
        {
            Enumeration enumeration = segmentPrimitives.keys();
            while (enumeration.hasMoreElements())
            {
                Integer id = (Integer)enumeration.nextElement();

                if (segmentPrimitives.get(id).equals(prim))

                return this.myCell.getSegmentWithId(id);

            }

        }
        return null;
    }

    /**
     * Applies colouring to the basic view only if there are groups named Colour_Blue.
     * These are normally present if the morphology has been imported from a neurolucida file.
     */
    public void applyGroupColouring()
    {
        ArrayList<Section> allSections = this.myCell.getAllSections();

        for (int i = 0; i < allSections.size(); i++)
        {
            Section nextSec = allSections.get(i);

            Vector allGroups = nextSec.getGroups();
            for (int j = 0; j < allGroups.size(); j++)
            {
                String group = (String)allGroups.elementAt(j);
                if (group.startsWith("Color_") || group.startsWith("Colour_") ||
                    group.startsWith("color_") || group.startsWith("colour_"))
                {
                    String colourName = group.substring(group.indexOf("_")+1);
                    Color c = ColourUtils.getColour(colourName);
                    if (c!=null)
                    {
                        LinkedList<Segment> allSegs = this.myCell.getAllSegmentsInSection(nextSec);
                        for (int k = 0; k < allSegs.size(); k++)
                        {
                            Segment seg = allSegs.get(k);
                            this.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(c), seg.getSegmentId());
                        }
                    }
                }
            }
        }
    }


    public void markSectionAsSelected(String secName,
                                      Color colourSelectedSection)
    {
        logger.logComment("markSectionAsSelected: "+secName+", "+colourSelectedSection);

        Color otherColour = Color.yellow;

        float[] secColourRGB = colourSelectedSection.getRGBColorComponents(null);
        //float[] otherColourRGB = otherColour.getRGBColorComponents(null);

        //Appearance appSegment = Utils3D.getGeneralObjectAppearance(colourSelectedSection);

        //Color intermediateColour = new Color((segColourRGB[0]+otherColourRGB[0]*2)/3f,
        //                                     (segColourRGB[1]+otherColourRGB[1]*2)/3f,
        //                                     (segColourRGB[2]+otherColourRGB[2]*2)/3f);

        Appearance appSection = Utils3D.getGeneralObjectAppearance(colourSelectedSection);




        for (int i = 0; i < this.myCell.getAllSegments().size(); i++)
        {
            Segment nextSeg = myCell.getAllSegments().get(i);

            if (nextSeg.getSection().getSectionName().equals(secName))
            {
                setSegmentAppearance(appSection, nextSeg.getSegmentId());
            }

        }
        logger.logComment("Setting the one selected section");


    }


    public Segment markSegmentAsSelected(int segmentID,
                                        Color colourSelectedSegment)
    {
        logger.logComment("markSegmentAsSelected: "+segmentID+", "+colourSelectedSegment);

        Color otherColour = Color.yellow;

        float[] segColourRGB = colourSelectedSegment.getRGBColorComponents(null);
        float[] otherColourRGB = otherColour.getRGBColorComponents(null);

        Appearance appSegment = Utils3D.getGeneralObjectAppearance(colourSelectedSegment);

        Color intermediateColour = new Color((segColourRGB[0]+otherColourRGB[0]*2)/3f,
                                             (segColourRGB[1]+otherColourRGB[1]*2)/3f,
                                             (segColourRGB[2]+otherColourRGB[2]*2)/3f);

        Appearance appSection = Utils3D.getGeneralObjectAppearance(intermediateColour);

        Segment selectedSegment = null;

        // Note if segmentId<0 just reset the cell...
        if (segmentID >= 0) selectedSegment = this.myCell.getSegmentWithId(segmentID);

        for (int i = 0; i < this.myCell.getAllSegments().size(); i++)
        {
            Segment nextSeg = myCell.getAllSegments().get(i);

            if (selectedSegment!=null && nextSeg.getSection().equals(selectedSegment.getSection()))
            {
                logger.logComment("Setting seg in sec: "+nextSeg);
                setSegmentAppearance(appSection, nextSeg.getSegmentId());
            }
            else
            {
                logger.logComment("Resetting seg outside of sec");
                setSegmentAppearance(getDefaultSegmentApp(), nextSeg.getSegmentId());
            }

        }
        logger.logComment("Setting the one selected segment");
        // Note if segmentId<0 just reset the cell...
        if (segmentID >= 0) setSegmentAppearance(appSegment, segmentID);

        return selectedSegment;

    }

}
