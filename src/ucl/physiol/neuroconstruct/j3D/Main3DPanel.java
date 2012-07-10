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

import java.io.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.sun.j3d.utils.behaviors.vp.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.mechanisms.ChannelMLCellMechanism;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.simulation.SimulationData.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.*;

/**
 * Panel for displaying the network of cells in 3D
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class Main3DPanel extends Base3DPanel implements SimulationInterface
{
    private ClassLogger logger = new ClassLogger("Main3DPanel");
    private Project project = null;

    //Random rand = new Random(System.currentTimeMillis());

    private TransformGroup scaleTG = null;

    private TransformGroup viewTG = null;

    private int prevValSlider = 10;

    private BranchGroup connectionsG = null;

    private boolean preferredSpikeValsEntered = false;

    private SpikeAnalysisOptions spikeOptions = new SpikeAnalysisOptions();

    private final String defaultPlotLocation = "In new frame";
    private final String defaultAnalyse = "-- Select: --";
    private final String analyseSpiking =  "Pop Spiking Rate";
    private final String popSpikingHisto = "Pop Spiking histogram";
    private final String popSpikesSync = "Pop Spikes synchrony";
    private final String popRaster =      "Population rasterplot";
    private final String popISIHisto = "Pop ISI histogram";
    private final String crossCorrel = "Selected cell synchrony";
    private final String vSimpFieldPot = "Very simple field potential";

    private Canvas3D myCanvas3D = null;

    private ArrayList<Integer> selectedCells = null;
    
    private SlicerFrame slicerFrame = null;

    /**
     * To improve performance, the segments are cached against SegmentName, for quick retrieval...
     */
    //Hashtable cachedSegments = new Hashtable();

    private ToolTipHelper toolTipText = ToolTipHelper.getInstance();


    private float optimalScale = 1;

    private SimpleUniverse simpleU = null;

    private String firstLineCellGroupComboBox = "Cell Group:";
    private String firstLineCellNumberComboBox = "Cell num:";
    private String selectManyCellNumComboBox = "Select many...";
    private String selectAllCellNumComboBox = "Select all";

    private String defaultSegmentBoxText = "Segment";

    private Transform3D lastViewingTransform3D = null;

    private Hashtable<String, OneCell3D> all3DCells = null;

    private SimulationRerunFrame simRerunFrame = null;

    private boolean refillingCellNumberComboBox = false;

    private File mySimulationDir = null;

    private String cached_lastCellRef = null;
    private OneCell3D cached_lastOneCell3D = null;


    private Container containerFor3D = this;
    
    private SimConfig simConfig = null;
    
    private boolean showFrame = false;


    JPanel jPanelControls = new JPanel();
    JComboBox jComboBoxCellGroup = new JComboBox();
    JComboBox jComboBoxCellNum = new JComboBox();
    JButton jButtonPlotVoltage = new JButton();
    JButton jButtonPlaySimulation = new JButton();
    JSlider jSliderViewDistance = new JSlider();

    JLabel jLabelZoom = new JLabel();
    JCheckBox jCheckBoxTransparent = new JCheckBox();
    JPanel jPanelSelection = new JPanel();
    JPanel jPanelViewOptions = new JPanel();
    JButton jButtonSimInfo = new JButton();
    JButton jButtonFind = new JButton();
    JButton jButtonDetach = new JButton();
    
    JButton jButtonSlice = new JButton();
    
    
    JButton jButtonNetInfo = new JButton();
    JPanel jPanelSimulation = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    BorderLayout borderLayout1 = new BorderLayout();
    JComboBox jComboBoxWherePlot = new JComboBox();
    Border border1;
    TitledBorder titledBorder1;

    JTextField jTextFieldSegment = new JTextField();
    JTextField jTextFieldSimName = new JTextField();
    JComboBox jComboBoxAnalyse = new JComboBox();
    JLabel jLabelAnalyse = new JLabel();

    public Main3DPanel(Project project, File simulationDirectory, SimConfig simConfig)
    {
        //logger.setThisClassVerbose(true);
        this.project = project;
        this.simConfig = simConfig;
        setLayout(new BorderLayout());
        jbInit();
        extraInit();
        jSliderViewDistance.setValue(prevValSlider);

        if (simulationDirectory != null)
        {
            mySimulationDir = simulationDirectory;
            jButtonPlaySimulation.setEnabled(true);
          //  jLabelSimulationName.setEnabled(true);
            jTextFieldSimName.setText(simulationDirectory.getName());
            jButtonSimInfo.setEnabled(true);
            jButtonPlotVoltage.setEnabled(true);
            jComboBoxWherePlot.setEnabled(true);
            jComboBoxAnalyse.setEnabled(true);
            //jButtonNetInfo.setEnabled(true);
            this.setViewedObject(mySimulationDir);

            // create it but don't show it yet...
            simRerunFrame = new SimulationRerunFrame(project, mySimulationDir, this);

        }
        else
        {
            this.setViewedObject(MainFrame.LATEST_GENERATED_POSITIONS);
        }

        add3DStuff();
        findRegions();
    }
/*
    private void cacheSegments()
    {
        Vector cellGroups = project.cellGroupsInfo.getAllCellGroupNames();
        for (int cellGroupIndex = 0; cellGroupIndex < cellGroups.size(); cellGroupIndex++)
        {
            String nextCellGroup = (String)cellGroups.elementAt(cellGroupIndex);
            Cell nextCell = project.cellManager.getCellType(project.cellGroupsInfo.getCellType(nextCellGroup));
            Vector allSegments = nextCell.getAllSegments();
            Vector segmentsInIdOrder = new Vector(allSegments.size());
            for (int segmentIndex = 0; segmentIndex < allSegments.size(); segmentIndex++)
            {
                    Segment nextSeg = (Segment)allSegments.elementAt(segmentIndex);
                    if (segmentsInIdOrder.size()<=nextSeg.getSegmentId())
                    {
                        segmentsInIdOrder.setSize(nextSeg.getSegmentId()+1);
                    }
                    segmentsInIdOrder.setElementAt(nextSeg, nextSeg.getSegmentId());
            }
            cachedSegments.put(nextCellGroup, segmentsInIdOrder);
            logger.logComment("segmentsInIdOrder: "+ segmentsInIdOrder);
        }
    }
*/

    /**
     * Will improve performance slightly when retrieving the same cellRef a number of times...
     */
    private OneCell3D getOneCell3D(String cellReference)
    {
        if (this.cached_lastCellRef!=null && cellReference.equals(cached_lastCellRef))
        {
            return this.cached_lastOneCell3D;
        }
        else
        {
            OneCell3D oneCell3D = all3DCells.get(cellReference);
            this.cached_lastCellRef = cellReference;
            this.cached_lastOneCell3D = oneCell3D;

            return oneCell3D;
        }
    }

    private void add3DStuff()
    {
        this.removeAll();

        logger.logComment(" ------- (Re)adding 3D stuff... ------");

        GraphicsConfiguration config = null;
        
        if (project.proj3Dproperties.getAntiAliasing()==Display3DProperties.AA_NOT_SET)
        {
            project.checkAboutAA();
        }

        boolean useAA = project.proj3Dproperties.getAntiAliasing()==Display3DProperties.AA_ON;

        if (!useAA)
        {
            config = SimpleUniverse.getPreferredConfiguration();
        }
        else
        {
            GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
            template.setSceneAntialiasing(GraphicsConfigTemplate3D.PREFERRED);
            config = GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getBestConfiguration(template);
        }

        myCanvas3D = new Canvas3D(config);

        if (!containerFor3D.isVisible()) containerFor3D= this;
        containerFor3D.add("Center", myCanvas3D);

        add("South", jPanelControls);

        simpleU = new SimpleUniverse(myCanvas3D);

        simpleU.getViewer().getView().setSceneAntialiasingEnable(useAA);
        
        simpleU.getViewer().getView().setBackClipDistance(1000);

        OrbitBehavior orbit = new OrbitBehavior(myCanvas3D, OrbitBehavior.REVERSE_ALL);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10000000);
        orbit.setSchedulingBounds(bounds);

        simpleU.getViewingPlatform().setViewPlatformBehavior(orbit);

        BranchGroup scene = createSceneGraph();
        


        PlatformGeometry pg = new PlatformGeometry();
        simpleU.getViewingPlatform().setNominalViewingTransform();

        if (lastViewingTransform3D != null)
        {
            MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();
            logger.logComment("There are " + mtg.getNumTransforms() +
                              " TransformGroups in ViewingPlatform");
            TransformGroup lastTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);
            lastTG.setTransform(lastViewingTransform3D);
        }

        MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();

        //TransformGroup tempTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);

        viewTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);
        Utils3D.addBackgroundLights(bounds, pg);
        simpleU.getViewingPlatform().setPlatformGeometry(pg);
        simpleU.addBranchGraph(scene);
    }

    /**
     * An attempt to clean up and free as much memory as possible when the 3D view is closed. Note: quick and dirty,
     * Java 3D specialist needed to clean up properly...
     */
    @Override
    public void destroy3D()
    {
        System.out.println("------------     Clearing memory...");

        this.removeAll();

        simpleU.cleanup();

        viewTG = null;
        scaleTG = null;
        simpleU = null;
        lastViewingTransform3D = null;
        all3DCells  = null;
        simRerunFrame = null;
        project = null;
        myCanvas3D = null;

        System.gc();
        System.gc();
        
        if (slicerFrame!=null)
            slicerFrame.dispose();

        System.out.println("------------     Memory hopefully cleared...");
    }

    public BranchGroup createSceneGraph()
    {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();
        Transform3D t3d = new Transform3D();
        RectangularBox allRegions = project.regionsInfo.getRegionEnclosingAllRegions(project, simConfig);

        if (project.proj3Dproperties.isCompartmentalisationDisplay())
        {
            // Don't allow these comp. diaplays for the network view. Only confuses matters...
            project.proj3Dproperties.setDisplayOption(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID);
        }

        // This is to get an idea of the size of the objects being viewed to scale the scene accordingly...

        float largestExtent = allRegions.getHighestXValue()- allRegions.getLowestXValue();
        largestExtent = Math.max(largestExtent, allRegions.getHighestYValue()- allRegions.getLowestYValue());
        largestExtent = Math.max(largestExtent, allRegions.getHighestZValue()- allRegions.getLowestZValue());

        logger.logComment("Largest extent for regions: "+ largestExtent);

        ArrayList<String> allCellGroups = project.cellGroupsInfo.getAllCellGroupNames();

        for (int cellGroupIndex = 0; cellGroupIndex < allCellGroups.size(); cellGroupIndex++)
        {
            String nextCellGroup = allCellGroups.get(cellGroupIndex);
            
            if (project.generatedCellPositions.getNumberInCellGroup(nextCellGroup)>0)
            {                
                Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(nextCellGroup));

                //logger.logComment("cell: "+ cell, true);
                largestExtent = Math.max(largestExtent, CellTopologyHelper.getXExtentOfCell(cell, false, true));
                largestExtent = Math.max(largestExtent, CellTopologyHelper.getYExtentOfCell(cell, false, true));
                largestExtent = Math.max(largestExtent, CellTopologyHelper.getZExtentOfCell(cell, false, true));
            }
        }
        logger.logComment("Largest extent for cells too: "+ largestExtent);

        optimalScale = (5f / (largestExtent)) + 0.00035f;



        logger.logComment("optimalScale: "+ optimalScale);


        t3d.setScale(optimalScale);

        scaleTG = new TransformGroup(t3d);
        scaleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        scaleTG.setCapability(Group.ALLOW_CHILDREN_WRITE);
        scaleTG.setCapability(Group.ALLOW_CHILDREN_READ);
        scaleTG.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        logger.logComment("Setting scale to: " + t3d.getScale());

        if (project.proj3Dproperties.getShow3DAxes())
        {
            Utils3D.addAxes(scaleTG, 0.005); // gives axes of 100 units length
        }
        objRoot.addChild(scaleTG);

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 10000000);
        Color backgroundColour = project.proj3Dproperties.getBackgroundColour3D();

        Utils3D.addBackground(bounds, objRoot, backgroundColour);

        if (project.proj3Dproperties.getShowRegions())
        {
            addRegions(scaleTG);
        }
        if (project.proj3Dproperties.getShowInputs())
        {
            addInputs(scaleTG);
        }


        addPositionedCells(scaleTG);

        addSynapses(scaleTG);
        
        if (showFrame)
        {
            allRegions.addLinesAroundRegion(scaleTG);
        }


        SectionPicker pickingBehav = new SectionPicker(objRoot, simpleU.getCanvas(), bounds, this);

        objRoot.addChild(pickingBehav);

        objRoot.compile();

        return objRoot;
    } // end of CreateSceneGraph method



    /**
     * Adding which ever parts of the synapse are set visible in GeneralProperties
     */
    private void addSynapses(TransformGroup mainTG)
    {
        if (!project.proj3Dproperties.getShowSynapseConns()
            && !project.proj3Dproperties.getShowSynapseEndpoints())
        {
            logger.logComment("No parts of the synapse to show...");
            return;
        }

        float sphereRadius = 1.6f;

        Iterator allNetConns = project.generatedNetworkConnections.getNamesNetConnsIter();

        while (allNetConns.hasNext())
        {
            String netConnName = (String) allNetConns.next();
            logger.logComment("-------------   Adding synaptic connections associated with: " +
                              netConnName);

            ArrayList<SingleSynapticConnection> connections = project.generatedNetworkConnections.getSynapticConnections(netConnName);

            if (connections != null)
            {

                logger.logComment("There are " + connections.size() +
                                  " connections associated with this");

                String sourceCellGroup = null;
                String targetCellGroup = null;
                Vector<SynapticProperties> syns = null;


                if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                {
                    sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                    targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                    syns = project.morphNetworkConnectionsInfo.getSynapseList(netConnName);
                }

                else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                {
                    sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                    targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                    syns = project.volBasedConnsInfo.getSynapseList(netConnName);
                }

                String sourceCellType = project.cellGroupsInfo.getCellType(sourceCellGroup);
                String targetCellType = project.cellGroupsInfo.getCellType(targetCellGroup);

                Cell sourceCell = project.cellManager.getCell(sourceCellType);
                Cell targetCell = project.cellManager.getCell(targetCellType);
                boolean gapJunction = false;

                if (syns.size()==1)
                {
                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(syns.get(0).getSynapseType());
                    if (cm instanceof ChannelMLCellMechanism)
                    {
                        if (((ChannelMLCellMechanism)cm).isGapJunctionMechanism())
                            gapJunction = true;

                    }
                }

                Color srcColour = Color.green;
                Color tgtColour = Color.red;

                if(gapJunction)
                {
                    srcColour = Color.orange;
                    tgtColour = Color.orange;
                }

                for (int singleConnIndex = 0; singleConnIndex < connections.size(); singleConnIndex++)
                {
                    GeneratedNetworkConnections.SingleSynapticConnection conn = connections.get(singleConnIndex);

                    logger.logComment("----  Looking at single connection from cell "
                                      + sourceCellGroup
                                      + "("
                                      + conn.sourceEndPoint.cellNumber
                                      + "), axon["
                                      + conn.sourceEndPoint.location.getSegmentId()
                                      + "]("
                                      + conn.sourceEndPoint.location.getFractAlong()
                                      + ") to "
                                      + targetCellGroup
                                      + "("
                                      + conn.targetEndPoint.cellNumber
                                      + ") dend["
                                      + conn.targetEndPoint.location.getSegmentId()
                                      + "]("
                                      + conn.targetEndPoint.location.getFractAlong()
                                      + ")");

                    Point3f relativePointSourceSyn
                        = CellTopologyHelper.convertSegmentDisplacement(
                        sourceCell,
                        conn.sourceEndPoint.location.getSegmentId(),
                        conn.sourceEndPoint.location.getFractAlong());

                    logger.logComment("relativePointSourceSyn: " +
                                      Utils3D.getShortStringDesc(relativePointSourceSyn));

                    Point3f sourceSomaAbsolutePosition
                        = project.generatedCellPositions.getOneCellPosition(sourceCellGroup,
                        conn.sourceEndPoint.cellNumber);

                    Point3f sourceSynAbsolutePosition = new Point3f(sourceSomaAbsolutePosition);
                    sourceSynAbsolutePosition.add(relativePointSourceSyn);

                    logger.logComment("sourceSynAbsolutePosition: " +
                                      Utils3D.getShortStringDesc(sourceSynAbsolutePosition));

                    Point3f relativePointTargetSyn
                        = CellTopologyHelper.convertSegmentDisplacement(
                        targetCell,
                        conn.targetEndPoint.location.getSegmentId(),
                        conn.targetEndPoint.location.getFractAlong());

                    logger.logComment("relativePointTargetSyn: " +
                                      Utils3D.getShortStringDesc(relativePointTargetSyn));

                    Point3f targetSomaAbsolutePosition
                        = project.generatedCellPositions.getOneCellPosition(targetCellGroup,
                        conn.targetEndPoint.cellNumber);

                    Point3f targetSynAbsolutePosition = new Point3f(targetSomaAbsolutePosition);
                    targetSynAbsolutePosition.add(relativePointTargetSyn);

                    logger.logComment("targetSynAbsolutePosition: " +
                                      Utils3D.getShortStringDesc(targetSynAbsolutePosition));

                    Point3f rel = new Point3f(targetSynAbsolutePosition);
                    rel.sub(sourceSynAbsolutePosition);

                    logger.logComment("-- target -> src rel Position: " +
                                      Utils3D.getShortStringDesc(rel)+", len: "+rel.distance(new Point3f()));

                    ArrayList<Integer> selNumbers = this.getSelectedCellNums();

                    if (!jCheckBoxTransparent.isSelected() ||
                        (this.getSelectedCellGroup().equals(sourceCellGroup) &&
                         selNumbers.contains(conn.sourceEndPoint.cellNumber) ||
                         (this.getSelectedCellGroup().equals(targetCellGroup) &&
                         selNumbers.contains(conn.targetEndPoint.cellNumber))))
                    {

                        if (project.proj3Dproperties.getShowSynapseEndpoints())
                        {

                            Primitive srcPrim
                                = addPositionedSphere(mainTG, sourceSynAbsolutePosition, srcColour, sphereRadius, false);

                            float rad = sphereRadius;
                            
                            
                            /*
                            float rad = sphereRadius*2;
                            if (sourceCellGroup.equals("InhCells")) 
                            {
                                c = Color.GREEN;
                                rad = sphereRadius*3;
                            }*/
                            
                            Primitive tgtPrim
                                = addPositionedSphere(mainTG, targetSynAbsolutePosition, tgtColour ,rad , true);

                            String tgtCellRef = SimulationData.getCellRef(targetCellGroup, conn.targetEndPoint.cellNumber);

                            OneCell3D oneCell3D = getOneCell3D(tgtCellRef);

                            String synRef = getSynPrimRef(netConnName, singleConnIndex);

                            oneCell3D.setSynapsePrimitive(synRef, tgtPrim);
                        }
                        if (project.proj3Dproperties.getShowSynapseConns())
                        {
                            joinTheDots(mainTG,
                                        sourceSynAbsolutePosition,
                                        srcColour,
                                        targetSynAbsolutePosition,
                                        tgtColour);
                        }
                    }
                    else
                    {
                        //System.out.println("Hiding conns...");
                    }
                }
            }
        }
    }

    public static String getSynPrimRef(String netConnName, int singleConnIndex)
    {
        return netConnName + "." + singleConnIndex;
    }

    /**
     * Adding electrical inputs if needed
     */
    private void addInputs(TransformGroup mainTG)
    {
        if (!project.proj3Dproperties.getShowInputs())
        {
            logger.logComment("No need to show em...");
            return;
        }

        ArrayList<String> refs = project.generatedElecInputs.getInputReferences();
        
        Appearance inputApp = Utils3D.getTransparentObjectAppearance(Color.white, 0.7f);

        for (int k = 0; k < refs.size(); k++)
        {
            float probeLength = 50;
            float probeStartRadius = 2f;
            float probeEndRadius = 0.5f; 
            ArrayList<SingleElectricalInput> allInputs = project.generatedElecInputs.getInputLocations(refs.get(k));
            
            for (int j = 0; j < allInputs.size(); j++)
            {
                SingleElectricalInput input = allInputs.get(j);

                //int cellNum = input.getCellNumber();

                //Point3d
                Cell probedCell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(input.getCellGroup()));

                Point3f relativePointProbe
                    = CellTopologyHelper.convertSegmentDisplacement(
                        probedCell,
                        input.getSegmentId(),
                        input.getFractionAlong());

                logger.logComment("relativePoint: " +
                                  Utils3D.getShortStringDesc(relativePointProbe));

                Point3f somaAbsolutePosition
                    = project.generatedCellPositions.getOneCellPosition(input.getCellGroup(),
                                                                        input.getCellNumber());

                Point3f absolutePositionProbe = new Point3f(somaAbsolutePosition);
                absolutePositionProbe.add(relativePointProbe);

                logger.logComment("absolutePositionProbe: " +
                  Utils3D.getShortStringDesc(absolutePositionProbe));

                //addPositionedSphere(mainTG, absolutePositionProbe, Color.yellow, probeStartRadius);

                Vector3d move = new Vector3d(absolutePositionProbe);
                
                //Vector3d moveUp = new Vector3d(probeLength/-2,0, 0);
                Transform3D shiftAlong = new Transform3D();
                
/*
                Point3f endProbePosition = new Point3f(absolutePositionProbe.x,
                                                       absolutePositionProbe.y - probeLength,
                                                       absolutePositionProbe.z);*/              
               
                
            
            
            if (project.proj3Dproperties.getShowInputsAs().equals(Display3DProperties.DISPLAY_INPUTS_AS_PROBES)) //visualize inputs as probes
            {
            
            /* scale the value of the probe for a better resolution */

            if ((allInputs.size() > 10) && (allInputs.size() < 100))
            {
                probeLength = 50 - ((allInputs.size()/2)-10);
                probeStartRadius = probeStartRadius * (probeLength/50);
                probeEndRadius = probeEndRadius * (probeLength/50); 
            }
            
            else
            {
                if (allInputs.size() > 99)
                {
                    probeLength = 10;
                    probeStartRadius = 0.5f;
                    probeEndRadius = 0.1f;
                }
            }
            
            /*generate the probe*/
            
                ConicalFrustum probe = new ConicalFrustum(probeStartRadius,
                              probeEndRadius,
                              probeLength,
                              ConicalFrustum.GENERATE_NORMALS |
                              ConicalFrustum.GENERATE_TEXTURE_COORDS |
                              ConicalFrustum.ENABLE_APPEARANCE_MODIFY,
                              project.proj3Dproperties.getResolution3DElements(),
                              inputApp);
                
                Vector3d moveUp = new Vector3d(0, probeLength/-2, 0);
                move.add(moveUp);
                shiftAlong.setTranslation(move);
                TransformGroup shiftedTG = new TransformGroup(shiftAlong);
                shiftedTG.addChild(probe);
                mainTG.addChild(shiftedTG);
            }
                
            else //visualize inputs as spheres (synapses)  
            {
               if (project.proj3Dproperties.getShowInputsAs().equals(Display3DProperties.DISPLAY_INPUTS_AS_SPHERES))
               {

                    Sphere sphere = new Sphere(3,
                                   Utils3D.getGeneralObjectAppearance(Color.RED));
                    shiftAlong.setTranslation(move);
                    TransformGroup shiftedTG = new TransformGroup(shiftAlong);   
                    shiftedTG.addChild(sphere);
                    mainTG.addChild(shiftedTG); 
                }              
            }
              
            }
        }
        }
            
           
          
 



    // for before the 3d panel is reset...
    public SimulationRerunFrame getSimulationFrame()
    {
        if (simRerunFrame == null)
        {
            logger.logComment("getSimulationFrame called but its null");
        }
        return simRerunFrame;
    }

    // for before the 3d panel is reset...
    public SlicerFrame getSlicerFrame()
    {
        if (slicerFrame == null)
        {
            logger.logComment("getSlicerFrame called but its null");
        }
        return slicerFrame;
    }

    
    // for after the 3d panel is reset...
    public void setSlicerFrame(SlicerFrame frame)
    {
        boolean currentlyShown = frame.isVisible();

        if(currentlyShown)
        {
            frame.dispose();
        }
        slicerFrame = frame;
        
        slicerFrame.setSimInterface(this);
        frame.setVisible(true);
    }


    // for after the 3d panel is reset...
    public void setSimulationFrame(SimulationRerunFrame frame)
    {
        boolean currentlyShown = frame.isVisible();

        if(currentlyShown)
        {
            frame.dispose();
        }


        simRerunFrame = frame;

        simRerunFrame.setSimInterface(this);
        jButtonPlotVoltage.setEnabled(true);
        jComboBoxWherePlot.setEnabled(true);
        jTextFieldSimName.setEnabled(true);
        jComboBoxAnalyse.setEnabled(true);
        jLabelAnalyse.setEnabled(true);
        jTextFieldSimName.setText( frame.getSimReference());

        //jButtonNetInfo.setEnabled(true);
        //simRerunFrame.refreshVoltagesCurrTimeStep();

        if(currentlyShown)
        {
            this.jButtonPlaySimulation_actionPerformed(null);
            simRerunFrame.refreshVoltagesCurrTimeStep();
        }
    }

    private Sphere addPositionedSphere(TransformGroup mainTG, Point3f posn, Color colour,
                                     float radius, boolean modifiable)
    {

        Vector3d move = new Vector3d(posn);
        Transform3D shiftAlong = new Transform3D();
        shiftAlong.setTranslation(move);

        TransformGroup shiftedTG = new TransformGroup(shiftAlong);

        int specs = Sphere.GENERATE_NORMALS |
            Sphere.GENERATE_TEXTURE_COORDS;

        if (modifiable) specs = specs | Sphere.ENABLE_APPEARANCE_MODIFY;

        Sphere sphere = new Sphere(radius,
                                   specs,
                                   30,
                                   Utils3D.getGeneralObjectAppearance(colour));

        shiftedTG.addChild(sphere);

        mainTG.addChild(shiftedTG);

        return sphere;
    }

    private void joinTheDots(TransformGroup mainTG, Point3f startPosn, Color startColour, Point3f endPosn,
                             Color endColour)
    {
        logger.logComment("Joining the dots from "+startPosn);

        if (connectionsG == null)
        {
            connectionsG = new BranchGroup();
            logger.logComment("Renewing : connectionsG" + connectionsG);

            connectionsG.setCapability(BranchGroup.ALLOW_DETACH);
            connectionsG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            connectionsG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            mainTG.addChild(connectionsG);
        }

        LineArray lines = new LineArray(2, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
        lines.setCapability(LineArray.ALLOW_COLOR_WRITE);

        Shape3D stickShape = new Shape3D(lines);

        //stickShape.r

        lines.setCoordinate(0, startPosn);
        lines.setCoordinate(1, endPosn);

        lines.setColor(0, new Color3f(startColour));
        lines.setColor(1, new Color3f(endColour));

        //lines.set


        connectionsG.addChild(stickShape);


    }

    private void addRegions(TransformGroup mainTG)
    {
        ArrayList<String> allRegionsToShow = new ArrayList<String>();

        ArrayList<String> cellGroups = project.cellGroupsInfo.getAllCellGroupNames();

        for (int i = 0; i < cellGroups.size(); i++)
        {
            String regionName = project.cellGroupsInfo.getRegionName(cellGroups.get(i));

            if (project.generatedCellPositions.getNumberInCellGroup(cellGroups.get(i)) > 0)
            {
                if (!allRegionsToShow.contains(regionName))
                    allRegionsToShow.add(regionName);
            }
        }

        String[] regs = project.regionsInfo.getAllRegionNames();

        for (int i = 0; i < regs.length; i++)
        {
            Color colour = project.regionsInfo.getRegionColour(regs[i]);

            if (!colour.equals(Color.white))
            {
                if (!allRegionsToShow.contains(regs[i]))
                    allRegionsToShow.add(regs[i]);
            }
        }


        for (int i = 0; i < allRegionsToShow.size(); i++)
        {
            //float currentHeight = ( (Float) project.regionsInfo.getValueAt(i, 2)).floatValue();
            Region nextRegion = project.regionsInfo.getRegionObject(allRegionsToShow.get(i));

            logger.logComment("Attempting to add a region: " + nextRegion);

            Appearance app = null;

                app = Utils3D.getTransparentObjectAppearance(project.regionsInfo.getRegionColour(allRegionsToShow.get(i)), 0.9f);

            nextRegion.addPrimitiveForRegion(mainTG, app);

            /*
             com.sun.j3d.utils.geometry.Box box1 = new com.sun.j3d.utils.geometry.Box(width/2, currentHeight/2, depth/2,
                                           com.sun.j3d.utils.geometry.Box.GENERATE_NORMALS |
                                           com.sun.j3d.utils.geometry.Box.GENERATE_TEXTURE_COORDS,
                                           app);

                        Transform3D moveUp = new Transform3D();

                        // they are placed centered on the origin, hence: + (currentHeight/2)...
                        float displacementFromLast = (lastHeight/2) + (currentHeight/2);
                        logger.logComment("The displacement: "+displacementFromLast);
                        Vector3d vectorUp = new Vector3d(0, displacementFromLast, 0);
                        if (i==0)
                        {
                            Vector3d vectorAcross = new Vector3d(width/2, 0, depth/2);
                            vectorUp.add(vectorAcross);
                        }
                        moveUp.setTranslation(vectorUp);

                        TransformGroup nextTransform = new TransformGroup(moveUp);
                        allTGs.add(i, nextTransform);

                        if (i==0)
                        {
                            mainTG.addChild(nextTransform);
                        }
                        else
                        {
                            TransformGroup lastTransform = (TransformGroup)allTGs.elementAt(i-1);
                            lastTransform.addChild(nextTransform);
                        }
                        nextTransform.addChild(box1);
                        lastHeight = currentHeight;*/
        }
    }



    private void addPositionedCells(TransformGroup mainTG)
    {
        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        logger.logComment("Adding cells from " + cellGroupNames.size() + " cell groups");

        logger.logComment("Cell generated info: " + project.generatedCellPositions);

        all3DCells = new Hashtable<String, OneCell3D>(project.generatedCellPositions.getNumberInAllCellGroups());

        for (int j = 0; j < cellGroupNames.size(); j++)
        {
            String cellGroupName = cellGroupNames.get(j);

            logger.logComment("Looking at cell group: " + cellGroupName);

            ArrayList currentCellGroupPositions = project.generatedCellPositions.getPositionRecords(cellGroupName);

            if (currentCellGroupPositions == null || currentCellGroupPositions.isEmpty())
            {
                logger.logComment("No cells generated for that cell group...");
            }
            else
            {
                logger.logComment("This has "
                                  + currentCellGroupPositions.size()
                                  + " cells");

                Color colorOfCellGroup = project.cellGroupsInfo.getColourOfCellGroup(cellGroupName);

                logger.logComment("*********      Colour will be: " + colorOfCellGroup);


                String cellType = project.cellGroupsInfo.getCellType(cellGroupName);

                Cell cell = project.cellManager.getCell(cellType);


                for (int i = 0; i < currentCellGroupPositions.size(); i++)
                {
                    PositionRecord posRecord
                        = (PositionRecord) currentCellGroupPositions.get(i);

                    int cellNumber = posRecord.cellNumber;

                    OneCell3D cell3D = new OneCell3D(cell, i, project);

                    String newCellReference = SimulationData.getCellRef(cellGroupName, cellNumber);

                    all3DCells.put(newCellReference, cell3D);

                    logger.logComment("Added the 3D object class in hashtable as: " +
                                      newCellReference);

                    cell3D.setDefaultCellAppearance(colorOfCellGroup);

                    TransformGroup cellTG = cell3D.createCellTransformGroup();

                    Transform3D position3D = new Transform3D();

                    Vector3d posn = new Vector3d(posRecord.x_pos,
                                                 posRecord.y_pos,
                                                 posRecord.z_pos);

                    position3D.setTranslation(posn);

                    TransformGroup tgPosn = new TransformGroup(position3D);

                    tgPosn.addChild(cellTG);
                    //////////if (cellGroupName.equals("SampleCellGroup")) 
                    mainTG.addChild(tgPosn);
                }
            }
        }

    }


    private Main3DPanel()
    {
        try
        {
            jbInit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void jbInit()
    {
        border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(border1,"Simulation:");
        jSliderViewDistance.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                jSliderViewDistance_stateChanged(e);
            }
        });
        jComboBoxCellGroup.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxCellGroup_itemStateChanged(e);
            }
        });

        jComboBoxCellNum.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxCellNumber_itemStateChanged(e);
            }
        });

        jButtonPlaySimulation.setEnabled(false);
        jButtonPlaySimulation.setText("Replay");
        jButtonPlaySimulation.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlaySimulation_actionPerformed(e);
            }
        });
        jButtonPlotVoltage.setEnabled(false);
        jButtonPlotVoltage.setText("Plot selected:");
        jButtonPlotVoltage.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlotVoltage_actionPerformed(e);
            }
        });
        jPanelControls.setBorder(BorderFactory.createEtchedBorder());
        jPanelControls.setMinimumSize(new Dimension(531, 90));
        jPanelControls.setPreferredSize(new Dimension(695, 90));
        jPanelControls.setLayout(flowLayout1);
        jLabelZoom.setText("Zoom:");
        jLabelZoom.setToolTipText(ToolTipHelper.getInstance().getToolTip("3D Gui Zoom"));

        jCheckBoxTransparent.setText("Transparent mode");
        jCheckBoxTransparent.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jCheckBoxTransparent_itemStateChanged(e);
            }
        });
        jCheckBoxTransparent.setToolTipText(ToolTipHelper.getInstance().getToolTip("3D Transparency"));

        jButtonSimInfo.setEnabled(false);
        jButtonSimInfo.setText("Info");
        jButtonSimInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSimInfo_actionPerformed(e);
            }
        });
        Insets ins = new Insets(4,4,4,4);
        jButtonFind.setText("0");
        jButtonFind.setMargin(ins);
        jButtonFind.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonFind_actionPerformed(e);
            }
        });
        jButtonSlice.setText("|");
        jButtonSlice.setMargin(ins);
        jButtonSlice.setToolTipText("Option to selectively make transparent portion of network to facilitate viewing of structure");
        
        jButtonSlice.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSlice_actionPerformed(e);
            }
        });
        jButtonDetach.setText("^");
        jButtonDetach.setMargin(ins);

        jButtonDetach.setToolTipText("Display 3D in separate window");

        jButtonDetach.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDetach_actionPerformed(e);
            }
        });


        jButtonNetInfo.setEnabled(false);
        jButtonNetInfo.setToolTipText("");
        jButtonNetInfo.setText("Info");
        jButtonNetInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetInfo_actionPerformed(e);
            }
        });
        this.setLayout(borderLayout1);
        jComboBoxWherePlot.setEnabled(false);
        jPanelSimulation.setEnabled(false);
        jPanelSimulation.setBorder(BorderFactory.createEtchedBorder());
        jTextFieldSimName.setEnabled(false);
        jTextFieldSimName.setText("No simulation loaded");
        jTextFieldSegment.setEnabled(true);
        jTextFieldSegment.setEditable(false);
        jTextFieldSegment.setText(defaultSegmentBoxText);
        jTextFieldSegment.setColumns(10);
        jSliderViewDistance.setPreferredSize(new Dimension(180, 16));
        jTextFieldSimName.setEnabled(false);
        jTextFieldSimName.setEditable(false);
        jTextFieldSimName.setText("No simulation loaded");
        jTextFieldSimName.setColumns(12);
        jComboBoxAnalyse.setEnabled(false);
        jComboBoxAnalyse.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxAnalyse_itemStateChanged(e);
            }
        });
        jLabelAnalyse.setEnabled(false);
        jLabelAnalyse.setText("Analyse:");
        this.add(jPanelControls,  BorderLayout.SOUTH);

        //jPanelSimulation.add(jLabelSimulationName, null);
        jPanelSimulation.add(jTextFieldSimName, null);
        jPanelSimulation.add(jButtonSimInfo, null);
        jPanelSimulation.add(jButtonPlaySimulation, null);
        jPanelSimulation.add(jButtonPlotVoltage, null);
        jPanelSimulation.add(jComboBoxWherePlot, null);
        jPanelSimulation.add(jLabelAnalyse, null);
        jPanelSimulation.add(jComboBoxAnalyse, null);

        jPanelControls.add(jPanelViewOptions, null);
        jPanelControls.add(jPanelSelection, null);
        jPanelControls.add(jPanelSimulation, null);

        jPanelViewOptions.add(jCheckBoxTransparent, null);
        jPanelViewOptions.add(jButtonFind, null);
        jPanelViewOptions.add(jButtonDetach, null);
        jPanelViewOptions.add(jButtonSlice, null);
        jPanelViewOptions.add(jLabelZoom, null);
        jPanelViewOptions.add(jSliderViewDistance, null);
        jPanelSelection.add(jComboBoxCellGroup, null);
        jPanelSelection.add(jComboBoxCellNum, null);
        jPanelSelection.add(jTextFieldSegment, null);
        jPanelSelection.add(jButtonNetInfo, null);

        jComboBoxCellGroup.addItem(firstLineCellGroupComboBox);
        jComboBoxCellNum.addItem(firstLineCellNumberComboBox);

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

        for (int i = 0; i < cellGroupNames.size(); i++)
        {
            if (project.generatedCellPositions.getNumberInCellGroup(cellGroupNames.get(i))>0)
            {
                jComboBoxCellGroup.addItem(cellGroupNames.get(i));
            }
        }
    }

    private void extraInit()
    {

        //jLabelZoom.setToolTipText(toolTipText.getToolTip("Zoom function"));

        jSliderViewDistance.setToolTipText(toolTipText.getToolTip("Zoom function"));


        jButtonFind.setToolTipText(toolTipText.getToolTip("Find cells"));

        //System.out.println("tool tip: "+jButtonFind.getToolTipText());

        jComboBoxCellGroup.setLightWeightPopupEnabled(false);
        jComboBoxCellNum.setLightWeightPopupEnabled(false);
        jComboBoxWherePlot.setLightWeightPopupEnabled(false);
        jComboBoxAnalyse.setLightWeightPopupEnabled(false);


        jComboBoxAnalyse.addItem(defaultAnalyse);
        jComboBoxAnalyse.addItem(analyseSpiking);
        jComboBoxAnalyse.addItem(popSpikingHisto);
        jComboBoxAnalyse.addItem(popSpikesSync);
        jComboBoxAnalyse.addItem(popRaster);
        jComboBoxAnalyse.addItem(popISIHisto);
        jComboBoxAnalyse.addItem(crossCorrel);
        jComboBoxAnalyse.addItem(vSimpFieldPot);


        jComboBoxAnalyse.setSelectedItem(defaultAnalyse);

        updatePlotList();

    }


    void updatePlotList()
    {
        jComboBoxWherePlot.removeAllItems();

        jComboBoxWherePlot.addItem(defaultPlotLocation);
        ArrayList<String> allPlots = PlotManager.getPlotterFrameReferences();
        for (int i = 0; i < allPlots.size(); i++)
        {
                String next = (String)allPlots.get(i);
                jComboBoxWherePlot.addItem(next);
        }
    }


    void jSliderViewDistance_stateChanged(ChangeEvent e)
    {
        JSlider source = (JSlider) e.getSource();
        int val = source.getValue();

        if (viewTG != null)
        {
            Transform3D t3d = new Transform3D();

            float scaleFactor = .3f;

            if (prevValSlider < val) // i.e. value increasing
            {
                t3d.set(new Vector3d(0.0, 0, scaleFactor));
            }
            else
            {
                t3d.set(new Vector3d(0.0, 0, -1 * scaleFactor));
            }
            prevValSlider = val;

            MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();
            TransformGroup tempTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);
            Transform3D trans = new Transform3D();
            tempTG.getTransform(trans);
            trans.mul(t3d);

            tempTG.setTransform(trans);

            Transform3D transFinal = new Transform3D();
            tempTG.getTransform(transFinal);

        }
    }


    private ArrayList<Integer> getSelectedCellNums()
    {
        Object nums = jComboBoxCellNum.getSelectedItem();

        ArrayList<Integer> list = new ArrayList<Integer>();

        if (nums.equals(firstLineCellNumberComboBox))
        {
            selectedCells = list;
            return list;
        }
        else if (nums instanceof String)
        {
            int cellNumber = Integer.parseInt(((String)nums).substring(6)); // due to Cell: 0, etc.

            list.add(cellNumber);
            selectedCells = list;
        }
        else if (nums instanceof CellChooser && selectedCells == null)
        {
            CellChooser chooser = (CellChooser)nums;

            try
            {
                selectedCells = chooser.getOrderedCellList();
            }
            catch (CellChooserException ex)
            {
                GuiUtils.showErrorMessage(logger, "Error picking cells from "+chooser, ex, this);
                return list;

            }
            logger.logComment("Selected some cells: " + selectedCells);

            return selectedCells;
        }
        else
        {
            return selectedCells;
        }
        return list;
    }



    void jComboBoxCellGroup_itemStateChanged(ItemEvent e)
    {
        logger.logComment("Change in jComboBoxCellGroup being detected: " + e.paramString());

        if (e.getStateChange() == ItemEvent.DESELECTED)
        {
            jTextFieldSegment.setText(defaultSegmentBoxText);
            jButtonNetInfo.setEnabled(false);
            // mainly need to remove colour from selected cell in "old" cell group
            String oldCellGroup = (String) e.getItem();
            logger.logComment("Deselecting cell group: " + oldCellGroup);


            ArrayList<Integer> wereSelected =  getSelectedCellNums();

            for (int selNum: wereSelected)
            {

                String selectedCellReference = SimulationData.getCellRef(oldCellGroup, selNum);

                logger.logComment("---  Looking for cell called: " + selectedCellReference);

                OneCell3D oneCell3D = this.getOneCell3D(selectedCellReference);

                if (oneCell3D == null)
                {
                    logger.logError("Can't find OneCell3D for: " + selectedCellReference);
                    return;
                }

                oneCell3D.resetCellAppearance();

                logger.logComment("Colour reset on: " + selectedCellReference);
            }

        }
        else if (e.getStateChange() == ItemEvent.SELECTED)
        {
            String selectedItem = (String) e.getItem();

            logger.logComment("Selected Item: " + selectedItem);
            if (selectedItem.equals(firstLineCellGroupComboBox))
            {
                if (jComboBoxCellNum.getItemCount() > 0)
                {
                    jComboBoxCellNum.setSelectedIndex(0);
                }
                return;
            }
            String cellGroupName = selectedItem;

            int numInThisGroup = project.generatedCellPositions.getNumberInCellGroup(cellGroupName);

            logger.logComment("Refilling the jComboBoxCellNumber");
            refillingCellNumberComboBox = true;

            jComboBoxCellNum.removeAllItems();

            jComboBoxCellNum.addItem(firstLineCellNumberComboBox);
            jComboBoxCellNum.addItem(selectManyCellNumComboBox);
            jComboBoxCellNum.addItem(selectAllCellNumComboBox);

            for (int i = 0; i < numInThisGroup; i++)
            {
                jComboBoxCellNum.addItem("Cell: " + i);
            }
            logger.logComment("Done refilling the jComboBoxCellNumber");
            refillingCellNumberComboBox = false;

        }

    }


    private String getCellGroup(String cellSegRef)
    {
        if (simRerunFrame==null)
        {
            return cellSegRef.substring(0, cellSegRef.lastIndexOf("_"));
        }
        else
        {
            return simRerunFrame.getCellGroup(cellSegRef);
        }
    }


    @Override
    public void markPrimitiveAsSelected(Primitive prim)
    {
        logger.logComment("Being told " + prim + " has been selected...");

        int segForPrim = -1;

        Enumeration allCellNames = all3DCells.keys();

        while (allCellNames.hasMoreElements() && segForPrim<0)
        {
            String nextName = (String) allCellNames.nextElement();
            OneCell3D oneCell = this.getOneCell3D(nextName);

            segForPrim = oneCell.hasPrimitive(prim);

            logger.logComment("Checking cell: " + nextName + " with seg: "+ segForPrim);


            if (segForPrim>=0)
            {
                String cellGroup = getCellGroup(nextName);
                
                int cellNumber = SimulationData.getCellNum(nextName);

                logger.logComment("Found the cell. It's number : " + cellNumber + " in " + cellGroup);

                jComboBoxCellGroup.setSelectedItem(cellGroup);

                jComboBoxCellNum.setSelectedItem("Cell: " + cellNumber);  // updates colour

                Segment seg = oneCell.getDisplayedCell().getSegmentWithId(segForPrim);

                jTextFieldSegment.setText("ID: "+ seg.getSegmentId()+", "+seg.getSegmentName());

            }
        }

        if (segForPrim<0)
        {
            return;
        }

    }


    void jComboBoxCellNumber_itemStateChanged(ItemEvent e)
    {
        logger.logComment("Change in jComboBoxCellNumber being detected: " + e.paramString());

        if (refillingCellNumberComboBox)
        {
            logger.logComment("Ignoring change, due to refreshingCellNumberComboBox");
            return;
        }
        if (e.getStateChange() == ItemEvent.DESELECTED)
        {
            jTextFieldSegment.setText(defaultSegmentBoxText);
            jButtonNetInfo.setEnabled(false);
            logger.logComment("Cell number deselected. Resetting all cells...");
            Enumeration enumeration = all3DCells.elements();
            while (enumeration.hasMoreElements())
            {
                OneCell3D oneCell3D = (OneCell3D) enumeration.nextElement();
                if (oneCell3D.hasTempAppearance())
                {
                    oneCell3D.resetCellAppearance();
                }
            }
        }
        else if (e.getStateChange() == ItemEvent.SELECTED)
        {
            if (this.jComboBoxCellNum.getSelectedItem().equals(this.firstLineCellNumberComboBox))
            {
                jButtonNetInfo.setEnabled(false);
                return;
            }
            else if (jComboBoxCellNum.getSelectedItem().equals(this.selectManyCellNumComboBox))
            {
                logger.logComment("selectManyCellNumComboBox is selected...");

                selectedCells = null;

                CellChooser cellChoice = new FixedNumberCells();

                CellChooserDialog dlg = new CellChooserDialog(GuiUtils.getMainFrame(),
                                                              "Please select a number of cells to highlight",
                                                              cellChoice);

                Dimension dlgSize = dlg.getPreferredSize();
                Dimension frmSize = getSize();
                Point loc = getLocation();
                dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                                (frmSize.height - dlgSize.height) / 2 + loc.y);

                dlg.setModal(true);
                dlg.setVisible(true);

                CellChooser chosen = dlg.getFinalCellChooser();

                logger.logComment("Choosen one: " + chosen);

                chosen.initialise(project.generatedCellPositions.getPositionRecords((String)jComboBoxCellGroup.getSelectedItem()));

                if (chosen instanceof RegionAssociatedCells)
                {
                    RegionAssociatedCells rac = (RegionAssociatedCells) chosen;

                    rac.setProject(project); // to give info on regions...
                }

                jComboBoxCellNum.addItem(chosen);

                jComboBoxCellNum.setSelectedItem(chosen); // will cause another jComboBoxCellNumber_itemStateChanged...

                return;

            }

            else if (jComboBoxCellNum.getSelectedItem().equals(this.selectAllCellNumComboBox))
            {
                logger.logComment("selectAllCellNumComboBox is selected...");

                selectedCells = null;

                CellChooser allCells = new AllCells();

                logger.logComment("Choosen one: " + allCells);

                allCells.initialise(project.generatedCellPositions.getPositionRecords((String)jComboBoxCellGroup.getSelectedItem()));


                jComboBoxCellNum.addItem(allCells);

                jComboBoxCellNum.setSelectedItem(allCells); // will cause another jComboBoxCellNumber_itemStateChanged...

                return;

            }

            ArrayList<Integer> selectedNums = this.getSelectedCellNums();

            logger.logComment("Going to selct cell(s): " + selectedNums);

            for (int cellNumber : selectedNums)
            {
                String cellGroup = (String) jComboBoxCellGroup.getSelectedItem();
                logger.logComment("------------       Changing to selected cell number: " + cellNumber + " in " +
                                  cellGroup);

                String selectedCellReference = SimulationData.getCellRef(cellGroup, cellNumber);

                logger.logComment("Looking for cell called: " + selectedCellReference);

                Color selectedCellColour = Color.red;

                jButtonNetInfo.setEnabled(true);

                if (jCheckBoxTransparent.isSelected())
                {
                    float tranparentness = project.proj3Dproperties.getTransparency();
                    float connTransp = 0.7f * tranparentness;

                    logger.logComment("Changing transparentness: " + tranparentness);

                    selectedCellColour = project.cellGroupsInfo.getColourOfCellGroup(cellGroup);
                    OneCell3D oneCell3D = this.getOneCell3D(selectedCellReference);

                    if (oneCell3D != null)
                    {
                        logger.logComment("Setting col of selected cell to cell to: " + selectedCellColour);
                        oneCell3D.setTempWholeCellAppearance(Utils3D.getGeneralObjectAppearance(selectedCellColour));
                    }

                    Iterator netConnNames = project.generatedNetworkConnections.getNamesNetConnsIter();

                    while (netConnNames.hasNext())
                    {
                        String netConnName = (String) netConnNames.next();

                        String sourceCellGroup = null;
                        String targetCellGroup = null;

                        if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConnName))
                        {
                            sourceCellGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(netConnName);
                            targetCellGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(netConnName);
                        }
                        else if (project.volBasedConnsInfo.isValidVolBasedConn(netConnName))
                        {
                            sourceCellGroup = project.volBasedConnsInfo.getSourceCellGroup(netConnName);
                            targetCellGroup = project.volBasedConnsInfo.getTargetCellGroup(netConnName);
                        }

                        if (sourceCellGroup.equals(cellGroup))
                        {
                            ArrayList<Integer>
                                targetCells = project.generatedNetworkConnections.getTargetCellIndices(netConnName,
                                cellNumber, true);
                            /** @todo Make swifter... */
                            for (int i = 0; i < targetCells.size(); i++)
                            {
                                Integer index = targetCells.get(i);
                                String targetCellReference = SimulationData.getCellRef(targetCellGroup, index.intValue());
                                OneCell3D targetCell3D = this.getOneCell3D(targetCellReference);
                                Color defaultColour = targetCell3D.getDefaultSegmentColour();
                                targetCell3D.setTempWholeCellAppearance(Utils3D.getTransparentObjectAppearance(
                                    defaultColour, connTransp));

                                logger.logComment("Setting to transp: " + targetCellReference);
                            }
                        }
                        if (targetCellGroup.equals(cellGroup))
                        {
                            ArrayList<Integer>
                                sourceCells = project.generatedNetworkConnections.getSourceCellIndices(netConnName,
                                cellNumber, true);
                            /** @todo Make swifter... */
                            for (int i = 0; i < sourceCells.size(); i++)
                            {
                                Integer index = sourceCells.get(i);
                                String sourceCellReference = SimulationData.getCellRef(sourceCellGroup, index.intValue());
                                OneCell3D sourceCell3D = this.getOneCell3D(sourceCellReference);
                                Color defaultColour = sourceCell3D.getDefaultSegmentColour();
                                sourceCell3D.setTempWholeCellAppearance(Utils3D.getTransparentObjectAppearance(
                                    defaultColour, connTransp));

                                logger.logComment("Setting to transp: " + sourceCellReference);
                            }
                        }
                    }
                    logger.logComment("Sel has temp app: " + oneCell3D.hasTempAppearance());

                    Enumeration<String> enumeration = all3DCells.keys();

                    while (enumeration.hasMoreElements())
                    {
                        String nextCellRef = enumeration.nextElement();

                        OneCell3D nextCell3D = all3DCells.get(nextCellRef);
                        int nextCellNum = SimulationData.getCellNum(nextCellRef);
                        String nextCellGroup = getCellGroup(nextCellRef);

                        if (!nextCell3D.hasTempAppearance()
                            && !(selectedNums.contains(nextCellNum) && cellGroup.equals(nextCellGroup)))
                        {
                            logger.logComment("Setting to transp: " + nextCellRef);

                            nextCell3D.setTempWholeCellAppearance(Utils3D.getTransparentObjectAppearance(Color.white.
                                darker(),
                                tranparentness));
                        }
                    }

                }
                else
                {
                    OneCell3D oneCell3D = this.getOneCell3D(selectedCellReference);
                    oneCell3D.setTempWholeCellAppearance(Utils3D.getGeneralObjectAppearance(selectedCellColour));
                }

                //Segment defaultSelectedSeg = this.getOneCell3D(selectedCellReference).getDisplayedCell().
                //    getFirstSomaSegment();

                //jTextFieldSegment.setText("ID: " + defaultSelectedSeg.getSegmentId() + ", " +
               //                           defaultSelectedSeg.getSegmentName());
            }
        }
    }


    @Override
    public Transform3D getLastViewingTransform3D()
    {
        MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();
        Transform3D t3d = new Transform3D();
        mtg.getTransformGroup(mtg.getNumTransforms() - 1).getTransform(t3d);
        return new Transform3D(t3d);
    }


    @Override
    public void setLastViewingTransform3D(Transform3D lastViewingTransform3D)
    {
        if (lastViewingTransform3D != null)
        {
            MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();
            logger.logComment("There are " + mtg.getNumTransforms() + " TransformGroups in ViewingPlatform");
            TransformGroup lastTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);

            try
            {
                lastTG.setTransform(lastViewingTransform3D);
            }
            catch(BadTransformException e)
            {
                // ignore..
            }
        }

    }
    
    


    @Override
    public void refresh3D()
    {
        if (simRerunFrame == null)
        {
            logger.logComment(this.hashCode() + "No simulation to refresh...");
            return;
        }
        simRerunFrame.refreshVoltagesCurrTimeStep();
    }


    void jButtonPlaySimulation_actionPerformed(ActionEvent e)
    {
        logger.logComment("Going to replay a simulation...");

        ///////////ppppppp//////////cacheSegments();

        this.jComboBoxCellGroup.setSelectedItem(firstLineCellGroupComboBox);
        this.jComboBoxCellNum.setSelectedItem(firstLineCellNumberComboBox);

        if (mySimulationDir == null)
        {
            return;
        }

        //simRerunFrame = new SimulationRerunFrame(project, mySimulationDir, this);

        simRerunFrame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Dimension frameSize = simRerunFrame.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        simRerunFrame.setLocation(Math.round( (screenSize.width - frameSize.width) / 1.1f),
                                  Math.round( (screenSize.height - frameSize.height) / 9f));

        simRerunFrame.setVisible(true);

    }


    /**
     * Sets transparent the segment, synapse or whole cell.
     * cellSegReference will be of form
     * SingleGranuleCell_0.gcdend1_2_seg (when individual segs are recorded) or
     * SingleGranuleCell_0 for only soma recorded
     */
    public void setTransparent(String cellItemReference)
    {
        String cellOnlyReference = SimulationData.getCellOnlyReference(cellItemReference);

        logger.logComment("cellItemReference: "+cellItemReference+", cellOnlyReference: "+cellOnlyReference);

        int id = SimulationData.getSegmentId(cellItemReference);

        OneCell3D oneCell = this.getOneCell3D(cellOnlyReference);

        PostSynapticObject pso = SimulationData.getPostSynapticObject(cellItemReference);

        if (pso==null)
        {
            if (id < 0)
            {
                // old way of storing, e.g. CellGroup_0_0.Soma.dat  ...
                String segName = SimulationData.getSegmentName(cellItemReference);

                if (segName != null)
                {
                    Segment seg = oneCell.getDisplayedCell().getSegmentWithName(segName, true);
                    id = seg.getSegmentId();

                    if (oneCell != null)
                    {
                        oneCell.setSegmentAppearance(Utils3D.getTransparentObjectAppearance(Color.white, project.proj3Dproperties.getTransparency()),id);
                        return;
                    }
                }

                oneCell.setWholeCellAppearance(Utils3D.getTransparentObjectAppearance(Color.white, project.proj3Dproperties.getTransparency()));
            }
            else
            {
                if (oneCell != null)
                {
                    oneCell.setSegmentAppearance(Utils3D.getTransparentObjectAppearance(Color.white, project.proj3Dproperties.getTransparency()),
                                                 id);
                }
            }
        }
        else
        {
            String synRef = getSynPrimRef(pso.getNetConnName(), pso.getSynapseIndex());

            if (oneCell != null)
            {
                oneCell.setSynapseAppearance(Utils3D.getTransparentObjectAppearance(Color.white, project.proj3Dproperties.getTransparency()),
                                             synRef);
            }

        }
    }

    
    
    

    /**
     * Sets to app the appearance of the whole cell temporarily.
     * cellSegReference will be of form
     * SingleGranuleCell_0 
     */
    public void setTempAppearance(String cellOnlyReference, Appearance app)
    {
        logger.logComment("cellOnlyReference: "+cellOnlyReference);

        OneCell3D oneCell = this.getOneCell3D(cellOnlyReference);

        if (oneCell != null)
        {
            oneCell.setTempWholeCellAppearance(app);
        }
      
    }

    /**
     * Removes the temp appearance of the whole cell.
     * cellSegReference will be of form
     * SingleGranuleCell_0 
     */
    public void removeTempAppearance(String cellOnlyReference)
    {
        logger.logComment("cellOnlyReference: "+cellOnlyReference);

        OneCell3D oneCell = this.getOneCell3D(cellOnlyReference);

        if (oneCell != null)
        {
            oneCell.resetCellAppearance();
        }
      
    }


    /**
     * Sets to colour of the segment, synapse or whole cell.
     * cellSegReference will be of form
     * SingleGranuleCell_0.gcdend1_2_seg (when individual segs are recorded) or
     * SingleGranuleCell_0 for only soma recorded
     */
    public void setColour(String cellItemReference, Color colour)
    {
        //System.out.println("Receiving.."+colour.getRGB());

        String cellOnlyReference = SimulationData.getCellOnlyReference(cellItemReference);

        logger.logComment("cellItemReference: "+cellItemReference+", cellOnlyReference: "+cellOnlyReference);

        int id = SimulationData.getSegmentId(cellItemReference);

        OneCell3D oneCell = this.getOneCell3D(cellOnlyReference);

        PostSynapticObject pso = SimulationData.getPostSynapticObject(cellItemReference);

        if (pso==null)
        {
            if (id < 0)
            {
                // old way of storing, e.g. CellGroup_0_0.Soma.dat  ...
                String segName = SimulationData.getSegmentName(cellItemReference);

                if (segName != null)
                {
                    Segment seg = oneCell.getDisplayedCell().getSegmentWithName(segName, true);
                    id = seg.getSegmentId();

                    if (oneCell != null)
                    {
                        oneCell.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(colour),
                                                     id);
                        return;
                    }
                }

                oneCell.setWholeCellAppearance(Utils3D.getGeneralObjectAppearance(colour));
            }
            else
            {
                if (oneCell != null)
                {
                    oneCell.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(colour),
                                                 id);
                }
            }
        }
        else
        {
            String synRef = getSynPrimRef(pso.getNetConnName(), pso.getSynapseIndex());

            if (oneCell != null)
            {
                oneCell.setSynapseAppearance(Utils3D.getGeneralObjectAppearance(colour),
                                             synRef);
            }

        }
    }


    public void validSimulationLoaded()
    {
        jButtonPlaySimulation.setEnabled(true);
        jButtonPlotVoltage.setEnabled(true);
        jComboBoxWherePlot.setEnabled(true);
        jTextFieldSimName.setEnabled(true);
        jComboBoxAnalyse.setEnabled(true);
        jLabelAnalyse.setEnabled(true);
    };


    public void noSimulationLoaded()
    {
        jButtonPlotVoltage.setEnabled(false);

        jComboBoxWherePlot.setEnabled(false);
        jButtonPlaySimulation.setEnabled(false);

        jComboBoxAnalyse.setEnabled(false);
        jTextFieldSimName.setEnabled(false);
        jLabelAnalyse.setEnabled(false);
        jTextFieldSimName.setText("No simulation loaded");

    };



    public boolean getTransparencySelected()
    {
        return jCheckBoxTransparent.isSelected();
    }


    public void setTransparencySelected(boolean state)
    {
        jCheckBoxTransparent.setSelected(state);
    }

    // for refreshing the 3d panel...
    public String getSelectedCellGroup()
    {
        return (String) jComboBoxCellGroup.getSelectedItem();
    }

/*
    // for refreshing the 3d panel...
    public String getSelectedCellNumString()
    {
        return (String)jComboBoxCellNumber.getSelectedItem();
    }

    public int getSelectedCellNum()
    {
        if (jComboBoxCellNumber.getSelectedItem().equals(firstLineCellNumberComboBox)) return -1;
        String withColon = (String)jComboBoxCellNumber.getSelectedItem();
        return Integer.parseInt(withColon.substring(withColon.indexOf(":")+1).trim());
    }*/

    // for refreshing the 3d panel...
    public void setSelectedCellGroup(String cellGroup)
    {
        jComboBoxCellGroup.setSelectedItem(cellGroup);
    }

    // for refreshing the 3d panel...
    public void setSelectedCellNumber(String cellNumber)
    {
        jComboBoxCellNum.setSelectedItem(cellNumber);
    }

    void jButtonPlotVoltage_actionPerformed(ActionEvent e)
    {
        if (simRerunFrame == null)
        {
            return;
        }
        String cellGroupSelected = (String) jComboBoxCellGroup.getSelectedItem();

        if (cellGroupSelected.equals(firstLineCellGroupComboBox))
        {
            return;
        }

        ArrayList<Integer> selNums = this.getSelectedCellNums();

        for (int cellNumber: selNums)
        {
            logger.logComment("Going to plot voltage of cell number: " + cellNumber
                              + " in group: " + cellGroupSelected);

            String cellReference = SimulationData.getCellRef(cellGroupSelected, cellNumber);

            ArrayList<String>  cellSegRefsToUse = new ArrayList<String>();

            ArrayList<String> allCellSegRefs = simRerunFrame.getCellSegRefsForCellRef(cellReference);

            logger.logComment("allCellSegRefs: " + allCellSegRefs);

            if (allCellSegRefs.size() == 1)
            {
                cellSegRefsToUse.add(allCellSegRefs.get(0));
            }
            else
            {
                int id = -1;
                String segString = null;
                try
                {
                    segString = jTextFieldSegment.getText();
                    String segId = segString.substring("ID: ".length(), segString.indexOf(",")).trim();
                    id = Integer.parseInt(segId);
                }
                catch(Exception ex)
                {
                    logger.logComment("Error parsing: " + segString+" for seg id. Assuming soma...");
                    id = 0;
                }
                String tempCellRef = SimulationData.getCellSegRef(cellGroupSelected, cellNumber, id);
                logger.logComment("tempCellRef: " + tempCellRef);

                if (allCellSegRefs.contains(tempCellRef))
                {
                    cellSegRefsToUse.add(tempCellRef);
                }
                else if (id == 0 && allCellSegRefs.contains(SimulationData.getCellRef(cellGroupSelected, cellNumber)))
                {
                    cellSegRefsToUse.add(SimulationData.getCellRef(cellGroupSelected, cellNumber));
                }

                else
                {
                    Object[] poss = new Object[allCellSegRefs.size()+1];

                    String all =  "All of the below";
                    poss[0] =all;

                    for (int i = 1; i <= allCellSegRefs.size(); i++)
                    {
                        poss[i] = allCellSegRefs.get(i-1);
                    }
                            
                    if (poss.length == 1)
                    {
                        GuiUtils.showErrorMessage(logger,
                                                  "There is no saved data to plot for segment: " + jTextFieldSegment.getText() +
                                                  " on cell number " +
                                                  cellNumber + " in group " + cellGroupSelected, null, this);
                        return;
                    }
                    
                    String ans = (String) JOptionPane.showInputDialog(
                        this,
                        "There is no info for cell " + cellNumber + " in " + cellGroupSelected + ", segment id: " + id +
                        ". Please select one (or all) of the following to plot:",
                        "Select segment to plot",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        poss,
                        poss[0]);
                    
                    if (ans.equals(all))
                    {
                        for (int i = 0; i < allCellSegRefs.size(); i++)
                        {
                            cellSegRefsToUse.add(allCellSegRefs.get(i));
                        }
                    }
                    else
                    {
                        cellSegRefsToUse.add(ans);
                    }
                         
                    
                }
            }

            PlotterFrame frame = null;
                
            for (String cellSegRefToUse: cellSegRefsToUse)
            {
                ArrayList<DataStore> dsForCellRef = simRerunFrame.getDataForCellSegRef(cellSegRefToUse, true);

                logger.logComment("dsForCellRef: " + dsForCellRef + ", cellSegRefToUse: " + cellSegRefToUse);

                ArrayList<DataStore> dssToPlot = new ArrayList<DataStore>();

                if (dsForCellRef.isEmpty())
                {
                    GuiUtils.showErrorMessage(logger, "There is no saved data to plot for segment: "
                                              + jTextFieldSegment.getText() + " on cell number " +
                                              cellNumber + " in group " + cellGroupSelected, null, this);
                    return;
                }
                if (dsForCellRef.size() > 1)
                {
                    String allOfType = "All data of type: ";
                    Object[] vars = new Object[dsForCellRef.size()];
                    ArrayList<String> showAllOfType = new ArrayList<String>();

                    for (int i = 0; i < dsForCellRef.size(); i++)
                    {
                        String varName = dsForCellRef.get(i).getVariable();

                        if (dsForCellRef.get(i).isSynapticMechData())
                        {
                            if (!showAllOfType.contains(varName))
                            {
                                showAllOfType.add(varName);
                                Object[] tempVars = new Object[dsForCellRef.size()+showAllOfType.size()];
                                System.arraycopy(vars, 0, tempVars, 0, dsForCellRef.size() + showAllOfType.size() - 1);
                                tempVars[i+showAllOfType.size()-1] = allOfType + varName;
                                vars = tempVars;

                            }
                            vars[i+showAllOfType.size()] = varName + " (" + dsForCellRef.get(i).getPostSynapticObject().getSynRef() + ")";
                        }
                        else
                        {
                            vars[i+showAllOfType.size()] = varName;
                        }
                    }

                    String choice = (String) JOptionPane.showInputDialog(this,
                                                           "Please select one for the following variables to plot for cell "
                                                           + cellNumber + " in " + cellGroupSelected,
                                                           "Select variable to plot",
                                                           JOptionPane.PLAIN_MESSAGE,
                                                           null,
                                                           vars,
                                                           vars[0]);

                    if (choice == null)
                    {
                        return;
                    }
                    String synRef = null;

                    if (choice.indexOf("(") > 0)
                    {
                        synRef = choice.substring(choice.lastIndexOf("(")+1, choice.lastIndexOf(")")).trim();
                        choice = choice.substring(0, choice.lastIndexOf("(")).trim();
                    }
                    String allOfTypeSelected = null;
                    if(choice.startsWith(allOfType))
                    {
                        allOfTypeSelected = choice.substring(allOfType.length());
                    }
                    logger.logComment("Selection: " + choice+", allOfTypeSelected: "+allOfTypeSelected);


                    for (DataStore ds : dsForCellRef)
                    {
                        if (!ds.isSynapticMechData())
                        {
                            if (ds.getVariable().equals(choice))
                                dssToPlot.add(ds);
                            else if (ds.getVariable().equals(allOfTypeSelected))
                                dssToPlot.add(ds);
                        }
                        else
                        {
                            logger.logComment("Checking: " + ds + " against var: "+ choice+", syn ref "+ synRef);

                            if ( ( ds.getVariable().equals(choice) && ds.getPostSynapticObject().getSynRef().equals(synRef) )
                                   || ds.getVariable().equals(allOfTypeSelected))
                            {
                                logger.logComment("Got it...");
                                dssToPlot.add(ds);
                            }
                        }
                    }

                }
                else
                {
                    dssToPlot.add(dsForCellRef.get(0));
                }

                logger.logComment("dssToPlot: " + dssToPlot);

                double[] times = null;

                try
                {
                    //ds = simRerunFrame.getDataAtAllTimes(cellRef, varToPlot, true);

                    times = simRerunFrame.getAllTimes();
                }
                catch (SimulationDataException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Problem loading data for " + dssToPlot + " in " + cellSegRefToUse, ex, this);
                    return;
                }

                logger.logComment("Going to plot " + times.length + " values...");

                String whereToPlot = (String) jComboBoxWherePlot.getSelectedItem();

                String plotFramePrefix = "Sim: " + simRerunFrame.getSimReference() + " Plot_";
                int suggestedNum = 0;

                ArrayList<String> allPlotFrameNames = PlotManager.getPlotterFrameReferences();
                //+allPlotFrameNames.


                while (allPlotFrameNames.contains(plotFramePrefix + suggestedNum))
                {
                    suggestedNum++;
                }



                if (whereToPlot.equals(defaultPlotLocation))
                {
                    String frameTitle = plotFramePrefix + suggestedNum;

                    if (selNums.size() > 1)
                    {
                        CellChooser choice = (CellChooser)this.jComboBoxCellNum.getSelectedItem();

                        frameTitle = "Plots of: " + choice.toNiceString() + " in cell group " 
                                + cellGroupSelected + " in sim: "+ simRerunFrame.getSimReference();
                    }
                    else if (cellSegRefsToUse.size() > 1)
                    {
                        frameTitle = "Plots of "+cellSegRefsToUse.size()+" locations in cell group " 
                                + cellGroupSelected + " in sim: "+ simRerunFrame.getSimReference();
                    }
                        

                    frame = PlotManager.getPlotterFrame(frameTitle);

                    updatePlotList();
                }
                else
                {
                    frame = PlotManager.getPlotterFrame(whereToPlot);
                }

                for(DataStore dsToPlot: dssToPlot)
                {

                    String synInfo = "";
                    if (dsToPlot.isSynapticMechData()) synInfo = " (synapse: " + dsToPlot.getPostSynapticObject().getSynRef() + ")";

                    String mainInfo = "Simulation: "
                        + simRerunFrame.getSimReference() +
                        ". Plot of " + dsToPlot.getVariable() + " in seg: " + dsToPlot.getAssumedSegmentId() + synInfo +
                        ", cell num: " + dsToPlot.getCellNumber()
                        + " in: " + dsToPlot.getCellGroupName()
                        + " (type: " + project.cellGroupsInfo.getCellType(cellGroupSelected)
                        + ") ";

                    mainInfo = mainInfo + "\n\n" + SimulationsInfo.getSimProps(simRerunFrame.getSimulationDirectory(), false);

                    Properties props = SimulationsInfo.getSimulationProperties(simRerunFrame.getSimulationDirectory());

                    String graphTitle = null;
                    String simulatorRef = "(" + props.getProperty("Simulator").charAt(0) + ")";

                    String varName = "";

                    if (!dsToPlot.getVariable().equals(SimPlot.VOLTAGE))
                        varName = " " + dsToPlot.getVariable();


                    if (!dsToPlot.isSegmentSpecified())
                    {
                        graphTitle = simRerunFrame.getSimReference() + " " + simulatorRef + ": " + cellGroupSelected + " - " +
                            cellNumber + varName;
                    }
                    else
                    {
                        graphTitle = simRerunFrame.getSimReference() + " " + simulatorRef + ": " + cellGroupSelected + " - " +
                            cellNumber + " (" + dsToPlot.getAssumedSegmentId() + ")" + varName;
                    }

                    DataSet data = new DataSet(graphTitle,
                                               mainInfo,
                                               "ms",
                                               SimPlot.getUnits(dsToPlot.getVariable()),
                                               "Time",
                                               SimPlot.getLegend(dsToPlot.getVariable()));

                    data.setUnits(dsToPlot.getXUnit(), dsToPlot.getYUnit());

                    double[] points = dsToPlot.getDataPoints();

                    logger.logComment("Plotting "+points.length+" data points vs "+times.length+" time points");


                    if (points.length!=times.length && points.length!=times.length+1)  // TODO: check second case: occours when reloading Genesis
                    {
                        GuiUtils.showWarningMessage(logger, "Warning. For data set: "+data.getReference()+" the number of data points is: "+ points.length
                                +" ("+points[0]+", ..., "+points[points.length-1]+")\n" +
                                " while the number of time points in the simulation is: "+times.length+" ("+times[0]+", ..., "+times[times.length-1]+")", frame);
                    }

                    for (int i = 0; i < Math.min(times.length, points.length); i++)
                    {
                        data.addPoint(times[i], points[i]);
                    }
                    logger.logComment("Finished data retrieval...");

                    frame.addDataSet(data);
                    frame.setVisible(true);
                    frame.repaint();
                }
            }
        }
    }


    void jCheckBoxTransparent_itemStateChanged(ItemEvent e)
    {
        logger.logComment("Transparency clicked: " + e.paramString());

        //this.refresh3D();

        /** @todo Replace this temporary measure... */
        this.jComboBoxCellGroup.setEnabled(!jCheckBoxTransparent.isSelected());
       this.jComboBoxCellNum.setEnabled(!jCheckBoxTransparent.isSelected());


        logger.logComment("Selected: " + jCheckBoxTransparent.isSelected());

        // simple way to update appearance
        int selected = jComboBoxCellNum.getSelectedIndex();
        boolean trans = this.jCheckBoxTransparent.isSelected();

        //destroy3D();

        try
        {
            this.scaleTG.removeChild(connectionsG);
        }
        catch (Exception ex)
        {
            logger.logError("Exception: " + ex, ex);
        }

        //connectionsG.removeAllChildren();

        connectionsG = null;


        /////////////addSynapses(scaleTG);

        this.refreshAll3D();

        logger.logComment("Setting index to zero...");
        jComboBoxCellNum.setSelectedIndex(0);


        jCheckBoxTransparent.setSelected(trans);

        logger.logComment("Setting index to: " + selected);
        jComboBoxCellNum.setSelectedIndex(selected);

    }

    public void refreshAll3D()
    {
        logger.logComment("Refreshing all 3D: ");


        Transform3D transf = getLastViewingTransform3D();

        this.connectionsG = null;
        this.simpleU.cleanup();

        add3DStuff();
        findRegions();

        this.repaint();
        this.validate();

        setLastViewingTransform3D(transf);

    }

    void jButtonSimInfo_actionPerformed(ActionEvent e)
    {
        StringBuilder sb = new StringBuilder();

        //Properties props = SimulationsInfo.getSimulationProperties(simRerunFrame.getSimulationDirectory());

        sb.append("\n<h3>Simulation reference     : " + simRerunFrame.getSimReference() + "<h3>\n");
        sb.append("   <p>Date recorded            : <b>" + simRerunFrame.getDateModified() + "</b></p>\n\n");

        
        if (simRerunFrame!=null && simRerunFrame.getSimulationData()!=null)
        {
            ArrayList<DataStore> dss = simRerunFrame.getSimulationData().getAllLoadedDataStores();
            
            sb.append("<p>Number of data sets reloaded  : <b>" + dss.size() + "</b></p>\n\n");
            for(DataStore ds: dss)
            {
                sb.append("<p>"+ds.toString()+"</p>\n\n");
            }
        }
        
        sb.append(SimulationsInfo.getSimProps(simRerunFrame.getSimulationDirectory(), true));

        SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                     "Parameters of simulation: "
                                                     + simRerunFrame.getSimReference(),
                                                     12,
                                                     false,
                                                     true);

        simpleViewer.setFrameSize(800, 700);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = simpleViewer.getSize();

        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;

        }
        simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleViewer.setVisible(true);
    }

    void jButtonFind_actionPerformed(ActionEvent e)
    {
        findRegions();
    }
    
    void jButtonSlice_actionPerformed(ActionEvent e)
    {
        showFrame = !showFrame;
        if (showFrame)
        {
            slicerFrame = new SlicerFrame(project, this, simConfig);
            GuiUtils.centreWindow(slicerFrame);
            
            slicerFrame.pack();
            
            slicerFrame.setVisible(true);
        }
        
        this.refreshAll3D();
        
    }


    final void findRegions()
    {

        logger.logComment("Finding best view of all regions and cells, looking down z axis. optimalScale: "+ optimalScale);

        RectangularBox allRegions = project.regionsInfo.getRegionEnclosingAllRegions(project, simConfig);
        
        logger.logComment("getRegionEnclosingAllRegions: "+ allRegions);
        

        float minX = 10000;
        float minY = 10000;
        float minZ = 10000;
        float maxX = -10000;
        float maxY = -10000;
        float maxZ = -10000;

        if (project.proj3Dproperties.getShow3DAxes())
        {
            minX = -100;
            minY = -100;
            minZ = -100;
            maxX = 100;
            maxY = 100;
            maxZ = 100;
        }

        float lowestXToShow = Math.min(minX, allRegions.getLowestXValue());
        float highestXToShow = Math.max(maxX, allRegions.getHighestXValue());

        float lowestYToShow = Math.min(minY, allRegions.getLowestYValue());
        float highestYToShow = Math.max(maxY, allRegions.getHighestYValue());

        float lowestZToShow = Math.min(minZ, allRegions.getLowestZValue());
        float highestZToShow = Math.max(maxZ, allRegions.getHighestZValue());

        ArrayList<String> allCellGroups = project.cellGroupsInfo.getAllCellGroupNames();

        for (int cellGroupIndex = 0; cellGroupIndex < allCellGroups.size(); cellGroupIndex++)
        {
            String nextCellGroup = allCellGroups.get(cellGroupIndex);
            Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(nextCellGroup));

            if (project.generatedCellPositions.getNumberInCellGroup(nextCellGroup)>0)
            {    
                float lowestXOnCell = CellTopologyHelper.getMinXExtent(cell, false, true);
                float lowestYOnCell = CellTopologyHelper.getMinYExtent(cell, false, true);
                float lowestZOnCell = CellTopologyHelper.getMinZExtent(cell, false, true);

                float highestXOnCell = CellTopologyHelper.getMaxXExtent(cell, false, true);
                float highestYOnCell = CellTopologyHelper.getMaxYExtent(cell, false, true);
                float highestZOnCell = CellTopologyHelper.getMaxZExtent(cell, false, true);


                int numInGroup = project.generatedCellPositions.getNumberInCellGroup(nextCellGroup);

                for (int cellNumber = 0; cellNumber < numInGroup; cellNumber++)
                {
                    Point3f nextPosition = project.generatedCellPositions.getOneCellPosition(nextCellGroup, cellNumber);
                    lowestXToShow =  Math.min(lowestXToShow, nextPosition.x + lowestXOnCell);
                    lowestYToShow =  Math.min(lowestYToShow, nextPosition.y + lowestYOnCell);
                    lowestZToShow =  Math.min(lowestZToShow, nextPosition.z + lowestZOnCell);

                    highestXToShow =  Math.max(highestXToShow, nextPosition.x + highestXOnCell);
                    highestYToShow =  Math.max(highestYToShow, nextPosition.y + highestYOnCell);
                    highestZToShow =  Math.max(highestZToShow, nextPosition.z + highestZOnCell);

                }
            }
        }


        float xCoordAfterScale = (highestXToShow+lowestXToShow)/2f;
        float yCoordAfterScale = (highestYToShow+lowestYToShow)/2f;

        float largestXYExtent =  Math.max(highestXToShow-lowestXToShow,
                                           highestYToShow-lowestYToShow);

        float zCoordAfterScale = (largestXYExtent * 2.2f) + highestZToShow;

        Transform3D t3d = new Transform3D();
        
        Vector3d viewPoint = new Vector3d(xCoordAfterScale,
                                          yCoordAfterScale,
                                          zCoordAfterScale);
        
        logger.logComment("Hi x: "+ highestXToShow+ ", lo x: "+ lowestXToShow);
        logger.logComment("Hi y: "+ highestYToShow+ ", lo y: "+ lowestYToShow);
        logger.logComment("Hi z: "+ highestZToShow+ ", lo z: "+ lowestZToShow);

        logger.logComment("viewPoint Main3D from cell perspective: "+ viewPoint);
        viewPoint.scale(optimalScale);

        logger.logComment("viewPoint scaled: "+ viewPoint);

        t3d.set(viewPoint);
        setLastViewingTransform3D(t3d);

    }

    void jButtonDetach_actionPerformed(ActionEvent e)
    {
        this.detach();
    }


    public final class AppCloser extends WindowAdapter
    {
        Frame parent = null;

        boolean exitOnClose = true;

        public AppCloser(Frame parent, boolean exitOnClose)
        {
            this.parent = parent;
            this.exitOnClose = exitOnClose;
        }

        @Override
        public void windowClosing(WindowEvent e)
        {
            parent.dispose();
            if (exitOnClose) System.exit(0);

            refreshAll3D();
        }
    }




    protected void detach()
    {
        Frame frame = new Frame("3D visualisation");

        frame.addWindowListener(new AppCloser(frame, false));

        this.remove(myCanvas3D);
        containerFor3D = frame;

        //if (myCanvas3D!=null) frame.add("Center", myCanvas3D);

        //containerFor3D = frame;

        GuiUtils.centreWindow(frame);

        frame.setVisible(true);

        frame.setSize(2000,2000);
        frame.setLocation(0,0);

        this.refreshAll3D();

    }

    void jButtonNetInfo_actionPerformed(ActionEvent e)
    {

        String cellGroupSelected = (String) jComboBoxCellGroup.getSelectedItem();

        if (cellGroupSelected.equals(firstLineCellGroupComboBox))
        {
            return;
        }
        ArrayList<Integer> selNums = this.getSelectedCellNums();
        
        String TAB = "&nbsp;&nbsp;&nbsp;";

        for (int cellNumber: selNums)
        {

            logger.logComment("Going to print network info on cell num: " + cellNumber 
                    + " in group: " + cellGroupSelected);

            StringBuilder info = new StringBuilder();

            Cell cell = project.cellManager.getCell(project.cellGroupsInfo.getCellType(cellGroupSelected));

            info.append("Info for cell num: <b>" + cellNumber + "</b> in group: <b>" + cellGroupSelected + "</b> (type: <b>" + cell.toString() +
                        "</b>)<br><br>");

            Point3f posn = project.generatedCellPositions.getOneCellPosition(cellGroupSelected, cellNumber);
            info.append("Position: <b>" + posn + "</b><br><br>\n");

            Vector<String> allNetConns = project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames();
            Vector<String> allAAConns = project.volBasedConnsInfo.getAllAAConnNames();

            allNetConns.addAll(allAAConns);

            for (int netConnNum = 0; netConnNum < allNetConns.size(); netConnNum++)
            {
                String nextNetConn = allNetConns.elementAt(netConnNum);
                ArrayList<GeneratedNetworkConnections.SingleSynapticConnection> connsToTgt = null;
                ArrayList<GeneratedNetworkConnections.SingleSynapticConnection> connsToSrc = null;
                String otherGroup = null;
                //String otherType = null;
                String connType = "morphology based network connection";

                if (allAAConns.contains(nextNetConn))
                {
                    connType = "volume based network connection";

                    Vector<SynapticProperties> synProps = project.volBasedConnsInfo.getSynapseList(nextNetConn);

                    if (cellGroupSelected.equals(project.volBasedConnsInfo.getSourceCellGroup(nextNetConn)))
                    {
                        info.append("Cell Group <b>" + cellGroupSelected
                                    + "</b> is the <b>source</b> of " + connType + ": <b>" + nextNetConn + "</b><br>\n");

                        connsToTgt = project.generatedNetworkConnections.getConnsFromSource(nextNetConn, cellNumber);

                        otherGroup = project.volBasedConnsInfo.getTargetCellGroup(nextNetConn);
                        //otherType = "target";
                        for (SynapticProperties prop : synProps)
                        {
                            info.append(TAB +"Global props: "+ prop.toNiceString() + "<br>\n");

                        }

                    }
                    if (cellGroupSelected.equals(project.volBasedConnsInfo.getTargetCellGroup(nextNetConn)))
                    {
                        info.append("Cell Group <b>" + cellGroupSelected
                                    + "</b> is the <b>target</b> of " + connType + ": <b>" + nextNetConn + "</b><br>\n");

                        connsToSrc = project.generatedNetworkConnections.getConnsToTarget(nextNetConn, cellNumber);

                        otherGroup = project.volBasedConnsInfo.getSourceCellGroup(nextNetConn);
                        //otherType = "source";
                        for (SynapticProperties prop : synProps)
                        {
                            info.append(TAB +"Global syn props: " + prop.toNiceString() + "<br>\n");

                        }

                    }

                }
                else
                {

                    Vector<SynapticProperties> synProps = project.morphNetworkConnectionsInfo.getSynapseList(nextNetConn);
                    boolean intraGroupConn = false;
                    
                    if (cellGroupSelected.equals(project.morphNetworkConnectionsInfo.getSourceCellGroup(nextNetConn)))
                    {
                        info.append("Cell Group <b>" + cellGroupSelected
                                    + "</b> is the <b>source</b> of " + connType + ": <b>" + nextNetConn + "</b><br>\n");

                        connsToTgt = project.generatedNetworkConnections.getConnsFromSource(nextNetConn, cellNumber);

                        otherGroup = project.morphNetworkConnectionsInfo.getTargetCellGroup(nextNetConn);
                        //otherType = "target";
                        if (cellGroupSelected.equals(otherGroup))
                            intraGroupConn = true;
                        
                        for (SynapticProperties prop : synProps)
                        {
                            info.append(TAB +"Global syn props: " + prop.toNiceString() + "<br>\n");

                        }

                    }
                    if (cellGroupSelected.equals(project.morphNetworkConnectionsInfo.getTargetCellGroup(nextNetConn)))
                    {
                        info.append("Cell Group <b>" + cellGroupSelected
                                    + "</b> is the <b>target</b> of " + connType + ": <b>" + nextNetConn + "</b><br>\n");

                        connsToSrc = project.generatedNetworkConnections.getConnsToTarget(nextNetConn, cellNumber);

                        otherGroup = project.morphNetworkConnectionsInfo.getSourceCellGroup(nextNetConn);
                        //otherType = "source";
                        
                        if (cellGroupSelected.equals(otherGroup))
                            intraGroupConn = true;
                        
                        for (SynapticProperties prop : synProps)
                        {
                            info.append(TAB +"Global syn props: "+ prop.toNiceString() + "<br>\n");

                        }
                    }
                    info.append("<br>\n");
                }

                if (connsToTgt != null)
                {

                    info.append(TAB+"Cell num <b>" + cellNumber + "</b> makes <b>" + connsToTgt.size() + "</b> connections to <b>target</b> cells:<br>\n");

                    ArrayList<Integer> unique = new ArrayList<Integer> (connsToTgt.size());

                    /** @todo Make swifter */
                    for (int otherNum = 0; otherNum < connsToTgt.size(); otherNum++)
                    {
                        GeneratedNetworkConnections.SingleSynapticConnection conn = connsToTgt.get(otherNum);
                        
                        Integer nextNum = conn.targetEndPoint.cellNumber;
                        SegmentLocation nextThisLoc = conn.sourceEndPoint.location;
                        SegmentLocation nextOtherLoc = conn.targetEndPoint.location;
                        
                        info.append(TAB+TAB+"Cell num <b>" + nextNum + "</b>, seg: <b>" + nextOtherLoc.getSegmentId() + "</b>, fract: <b>" 
                            + nextOtherLoc.getFractAlong() + "</b> in <b>" + otherGroup+"</b>");

                        Point3f posnOther = project.generatedCellPositions.getOneCellPosition(otherGroup, nextNum.intValue());

                        info.append(" (cell position: <b>" + posnOther + "</b>)");
                        info.append(" is connected to this cell, seg: <b>" + nextThisLoc.getSegmentId() + "</b>, fract: <b>" 
                            + nextThisLoc.getFractAlong() + "</b><br>\n");

                        if (conn.apPropDelay>0) 
                            info.append(TAB+TAB+TAB+"Delay due to AP propagation: " + conn.apPropDelay + " ms. "+ "<br>\n");

                        ArrayList<ConnSpecificProps> props = conn.props;

                        if (props != null)
                        {
                            for (ConnSpecificProps prop : props)
                            {
                                info.append(TAB+TAB+TAB+prop.toNiceString() + "<br>\n");

                            }
                        }
                        else
                        {
                        }

                        if (!unique.contains(nextNum)) unique.add(nextNum);
                    }
                    if (connsToTgt.size() > 1)
                        info.append(TAB+TAB+TAB+"(<b>" + unique.size() + "</b> individual cell(s), so average of <b>"
                                    + (float) connsToTgt.size() / (float) unique.size() + "</b> connection(s) to each)<br>\n");
                    info.append("<br>\n");
                }
                
                

                if (connsToSrc != null)
                {
                    info.append(TAB+"Cell num <b>" + cellNumber + "</b> receives <b>" + connsToSrc.size() 
                        + "</b> connections from <b>source</b> cells:<br>\n");

                    ArrayList<Integer> unique = new ArrayList<Integer> (connsToSrc.size());

                    /** @todo Make swifter */
                    for (int otherNum = 0; otherNum < connsToSrc.size(); otherNum++)
                    {
                        GeneratedNetworkConnections.SingleSynapticConnection conn = connsToSrc.get(otherNum);
                        
                        Integer nextNum = conn.sourceEndPoint.cellNumber;
                        SegmentLocation nextThisLoc = conn.targetEndPoint.location;
                        SegmentLocation nextOtherLoc = conn.sourceEndPoint.location;
                        

                        info.append(TAB+TAB+"Cell num <b>" + nextNum + "</b>, seg: <b>" + nextOtherLoc.getSegmentId() + "</b>, fract: <b>" 
                            + nextOtherLoc.getFractAlong() + "</b> in <b>" + otherGroup+"</b>");

                        Point3f posnOther = project.generatedCellPositions.getOneCellPosition(otherGroup, nextNum.intValue());

                        info.append(" (cell position: <b>" + posnOther + "</b>)");
                        info.append(" is connected to this cell, seg: <b>" + nextThisLoc.getSegmentId() + "</b>, fract: <b>" 
                            + nextThisLoc.getFractAlong() + "</b><br>\n");

                        if (conn.apPropDelay>0) 
                            info.append(TAB+TAB+TAB+"Delay due to AP propagation: " + conn.apPropDelay + " ms. "+ "<br>\n");

                        ArrayList<ConnSpecificProps> props = conn.props;

                        if (props != null)
                        {
                            for (ConnSpecificProps prop : props)
                            {
                                info.append(TAB+TAB+TAB+prop.toNiceString() + "<br>\n");

                            }
                        }
                        else
                        {
                        }

                        if (!unique.contains(nextNum)) unique.add(nextNum);
                    }
                    if (connsToSrc.size() > 1)
                        info.append(TAB+TAB+TAB+"(<b>" + unique.size() + "</b> individual cell(s), so average of <b>"
                                    + (float) connsToSrc.size() / (float) unique.size() + "</b> connection(s) to each)<br>\n");
                    info.append("<br>\n");
                }

            }

            Vector allInputs = project.elecInputInfo.getAllStims();
            for (int i = 0; i < allInputs.size(); i++)
            {
                StimulationSettings nextStim = (StimulationSettings) allInputs.elementAt(i);

                if (nextStim.getCellGroup().equals(cellGroupSelected))
                {

                    ArrayList<SingleElectricalInput>
                        theseInputs = project.generatedElecInputs.getInputLocations(nextStim.getReference());
                    boolean cellGetsStim = false;


                    StringBuilder sbTemp = new StringBuilder();
                    int count = 0;

                    for (int j = 0; j < theseInputs.size(); j++)
                    {
                        if (theseInputs.get(j).getCellNumber() == cellNumber)
                        {
                            sbTemp.append(TAB+"Cell num: <b>" + cellNumber + "</b> receives this input on segment: <b>" +
                                        theseInputs.get(j).getSegmentId() + "</b>, fraction along: <b>" +
                                        theseInputs.get(j).getFractionAlong() + "</b><br>\n");
                            count++;
                            cellGetsStim = true;
                        }
                    }

                    info.append("Cell Group <b>" + cellGroupSelected + "</b> receives <b>"+count+"</b> inputs from stimulation: <b>" + nextStim.toString() +
                                "</b><br>\n");

                    info.append(sbTemp.toString());


                    if (!cellGetsStim) info.append(TAB+"Cell num: <b>" + cellNumber +
                                                   "</b> in this Cell Group does not receive one of these inputs" + "<br><br>\n\n");

                }
            }

            SimpleViewer simpleViewer = new SimpleViewer(info.toString(),
                                                         "Info for cell num: " + cellNumber
                                                         + " in group: " + cellGroupSelected,
                                                         12,
                                                         false,
                                                         true);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            simpleViewer.setFrameSize( (int) (screenSize.getWidth() * 0.7d), (int) (screenSize.getHeight() * 0.7d));

            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height)
                frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width)
                frameSize.width = screenSize.width;

            simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);
        }
    }

    void jComboBoxAnalyse_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()!=ItemEvent.SELECTED) return;

        if (!jComboBoxAnalyse.isEnabled()) return;

        String selected = (String)jComboBoxAnalyse.getSelectedItem();

        if (selected.equals(defaultAnalyse))
        {
            return;
        }
        else if (selected.equals(analyseSpiking))
        {
            try
            {
                analyseCellGroupSpiking();
            }
            catch (SimulationDataException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem analysing the spiking", ex, this);
                return;
            }
            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }
        else if (selected.equals(popSpikingHisto))
        {
            try
            {
                populationSpikingHisto();
            }
            catch (SimulationDataException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem analysing the population spiking", ex, this);
                return;
            }

            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }
        else if (selected.equals(popSpikesSync))
        {
            try
            {
                populationSpikesSync();
            }
            catch (SimulationDataException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem analysing the population spikes synchrony", ex, this);
                return;
            }

            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }
        else if (selected.equals(popRaster))
        {
            try
            {
                populationRasterplot();
            }
            catch (SimulationDataException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem analysing the ISIs", ex, this);
                return;
            }



            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }
        else if (selected.equals(popISIHisto))
        {
            try
            {
                populationISIHisto();
            }
            catch (SimulationDataException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem analysing the ISIs", ex, this);
                return;
            }



            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }
        else if (selected.equals(crossCorrel))
        {
            try
            {
                this.doCrossCorrel();
            }
            catch (SimulationDataException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem analysing the cell synchrony of that cell", ex, this);
                return;
            }
            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }
        else if (selected.equals(vSimpFieldPot))
        {
            this.doVerySimpFieldPot();
          
            jComboBoxAnalyse.setSelectedItem(defaultAnalyse);
        }







    }



    private void analyseCellGroupSpiking()  throws SimulationDataException
    {
        logger.logComment("---- Analysing cell group spiking...");

        double[] times = simRerunFrame.getAllTimes();

        //String[] allCellGroups = project.cellGroupsInfo.getAllCellGroupNamesArray();

        ArrayList<String> everyCellGroup = project.cellGroupsInfo.getAllCellGroupNames();

        Vector<String> allNonEmptyCellGroups = new Vector<String>();

        for (int i = 0; i < everyCellGroup.size(); i++)
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(everyCellGroup.get(i));
            if (numInCellGroup > 0) allNonEmptyCellGroups.add(everyCellGroup.get(i));
        }

        String[] allCellGroups = new String[allNonEmptyCellGroups.size()];
        allNonEmptyCellGroups.copyInto(allCellGroups);


        String message = "Please select the Cell Group whose spiking rates you wish to plot";

        String cellGroup = (String)JOptionPane.showInputDialog(this,
                                                      message,
                                                      "Select Cell Group",
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      null,
                                                      allCellGroups,
                                                      allCellGroups[0]);

        if (cellGroup == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        logger.logComment("Cell group selected: " + cellGroup);

        int total = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

        StringBuilder desc = new StringBuilder("Cell Group : " + cellGroup + " spiking pattern\n");

        DataSet cellGroupFreqData = new DataSet(simRerunFrame.getSimReference()
                                                + ": Spiking rates in " + cellGroup, desc.toString(),
            "", "Hz", "Cell index", "Spiking rate (num spikes/time interval)");


        cellGroupFreqData.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT);

        String req = "Cutoff threshold which will be considered a spike";

        float suggestedThresh = -20;
        if (preferredSpikeValsEntered) suggestedThresh = spikeOptions.getThreshold();


        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement> ();

        InputRequestElement threshInput = new InputRequestElement("Threshold",
                                                                  req, null, suggestedThresh+"", "mV");

        inputs.add(threshInput);


/*
        String thresh = JOptionPane.showInputDialog(this, req, "" + suggestedThresh);
        if (thresh == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float threshold = 0;
        try
        {
            threshold = Float.parseFloat(thresh);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        spikeOptions.setThreshold(threshold);*/

        req = "Please enter the start time from which to analyse the spiking";
        float suggestedStart = 0;
        if (preferredSpikeValsEntered) suggestedStart = spikeOptions.getStartTime();


        InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart+"", "ms");
        inputs.add(startInput);


/*
        String start = JOptionPane.showInputDialog(this, req, "" + suggestedStart);
        if (start == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float startTime = 0;
        try
        {
            startTime = Float.parseFloat(start);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
            return;
        }
        spikeOptions.setStartTime(startTime);*/

        req = "Please enter the finish time from which to analyse the spiking";
        double suggestedEnd = times[times.length-1];
        if (preferredSpikeValsEntered) suggestedEnd = spikeOptions.getStopTime();

        InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd+"", "ms");
        inputs.add(stopInput);

/*
        String stop = JOptionPane.showInputDialog(this, req, "" + suggestedEnd);
        if (stop == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float stopTime = 1000;
        try
        {
            stopTime = Float.parseFloat(stop);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
            return;
        }
        spikeOptions.setStopTime(stopTime);*/

        InputRequest dlg = new InputRequest(null,
                                            "Please enter the parameters for analysing the spiking rate of cell group: " +
                                            cellGroup,
                                            "Please enter the parameters for calculating the spiking rate",
                                            inputs, true);

        GuiUtils.centreWindow(dlg);

        dlg.setVisible(true);

        preferredSpikeValsEntered = true;

        float threshold = 0;
        try
        {
            threshold = Float.parseFloat(threshInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        spikeOptions.setThreshold(threshold);


        float startTime = 0;
        try
        {
            startTime = Float.parseFloat(startInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
            return;
        }
        spikeOptions.setStartTime(startTime);

        float stopTime = 1000;
        try
        {
            stopTime = Float.parseFloat(stopInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
            return;
        }
        spikeOptions.setStopTime(stopTime);



        double simDuration = (stopTime - startTime);

        for (int cellNum = 0; cellNum < total; cellNum++)
        {
            double[] voltages = null;


            String cellRef = SimulationData.getCellSegRef(cellGroup, cellNum, 0, simRerunFrame.isOnlySomaValues());


            voltages = simRerunFrame.getVoltageAtAllTimes(cellRef);


            boolean spiking = false;
            Vector<Double> spikeTimes = new Vector<Double>();

            for (int i = 0; i < voltages.length; i++)
            {
                double nextY = voltages[i];
                if (nextY >= threshold)
                {
                    if (!spiking)
                    {
                        if (times[i] >= startTime &&
                            times[i] <= stopTime)
                        {
                            spikeTimes.add(new Double(times[i]));
                        }
                    }
                    spiking = true;
                }
                else
                {
                    spiking = false;
                }
            }
            int numSpikes = spikeTimes.size();
            //double interspikeInterval = simDuration / (double) spikeTimes.size();
            double freq = ( spikeTimes.size() / simDuration) * 1000;

            desc.append("Cell num " + cellNum + ": Total num of spikes: " + numSpikes
                        + ", Average frequency: " + freq + " KHz \n");

            cellGroupFreqData.addPoint(cellNum, freq);

        }
        cellGroupFreqData.setDescription(desc.toString());
        PlotterFrame frame = PlotManager.getPlotterFrame(cellGroupFreqData.getReference());
        updatePlotList();

        frame.addDataSet(cellGroupFreqData);
        frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
        frame.setVisible(true);



    }

    
    private void populationSpikingHisto()  throws SimulationDataException
    {
        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

        double[] times = simRerunFrame.getAllTimes();


        ArrayList<String> everyCellGroup = project.cellGroupsInfo.getAllCellGroupNames();

        Vector<String> allNonEmptyCellGroups = new Vector<String>();

        for (int i = 0; i < everyCellGroup.size(); i++)
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(everyCellGroup.get(i));
            if (numInCellGroup > 0) allNonEmptyCellGroups.add(everyCellGroup.get(i));
        }

        String[] allCellGroups = new String[allNonEmptyCellGroups.size()];
        allNonEmptyCellGroups.copyInto(allCellGroups);


        String message = "Please select the Cell Group whose population spike time histogram you would like to see.";

        String cellGroup = (String)JOptionPane.showInputDialog(this,
                                                      message,
                                                      "Select Cell Group",
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      null,
                                                      allCellGroups,
                                                      allCellGroups[0]);

        if (cellGroup == null)
        {
            logger.logComment("Cancelled...");
            return;
        }


        //int total = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

        ArrayList<PositionRecord> positions = project.generatedCellPositions.getPositionRecords(cellGroup);

        CellChooser myCellChooser = new AllCells();
        String request = "Please select the cells in cell group: " +
            cellGroup + " for which to generate spiking histogram";

        CellChooserDialog cellChooserDlg = new CellChooserDialog(GuiUtils.getMainFrame(), request, myCellChooser);

        GuiUtils.centreWindow(cellChooserDlg);

        cellChooserDlg.setVisible(true);

        CellChooser cellChooser = cellChooserDlg.getFinalCellChooser();

        cellChooser.initialise(positions);

        if (cellChooser instanceof RegionAssociatedCells)
        {
            RegionAssociatedCells rac = (RegionAssociatedCells) cellChooser;

            rac.setProject(project); // to give info on regions...
        }

        ArrayList<Integer> orderedCellNums = null;
        try
        {
            orderedCellNums = cellChooser.getOrderedCellList();
        }
        catch (CellChooserException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Error getting cell numbers from: " + cellChooser.toString(), ex1, this);
            return;
        }






        StringBuilder desc = new StringBuilder(simRerunFrame.getSimReference()
                                                + ": Spiking histogram of " + cellGroup + " for: "+cellChooser.toNiceString());

        DataSet cellGroupHist = new DataSet(desc.toString(), desc.toString(),
            "ms", "", "Time", "Num spikes in bin");



        cellGroupHist.setGraphFormat(PlotCanvas.USE_LINES_FOR_PLOT);










        String req = "Threshold which will be considered a spike for Cell Group: " +
            cellGroup;



        float suggestedThresh = -20;
        if (preferredSpikeValsEntered) suggestedThresh = spikeOptions.getThreshold();

        //String thresh = JOptionPane.showInputDialog(this, req, "" + suggestedThresh);

        InputRequestElement threshInput = new InputRequestElement("threshold", req, null, suggestedThresh+"", "mV");
        inputs.add(threshInput);


        req = "Start time from which to analyse the spiking";
        double suggestedStart = 0;
        if (preferredSpikeValsEntered) suggestedStart = spikeOptions.getStartTime();

        InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart+"", "ms");
        inputs.add(startInput);


        req = "Finish time from which to analyse the spiking";
        double suggestedEnd = times[times.length - 1];
        if (preferredSpikeValsEntered) suggestedEnd = spikeOptions.getStopTime();


        InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd+"", "ms");
        inputs.add(stopInput);


        req = "Please enter the bin size for the histogram";
        double suggestedBinSize = Math.min(1, (suggestedEnd -suggestedStart)/100f);


        InputRequestElement binSizeInput = new InputRequestElement("binsize", req, null, suggestedBinSize+"", "ms");
        inputs.add(binSizeInput);



        InputRequest dlg = new InputRequest(null,
                                            "Please enter the parameters for the histogram",
                                            "Parameters for the histogram",
                                            inputs, true);

        GuiUtils.centreWindow(dlg);

        dlg.setVisible(true);


            if (dlg.cancelled()) return;



        if (threshInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float threshold = 0;
        try
        {
            threshold = Float.parseFloat(threshInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        spikeOptions.setThreshold(threshold);




        //String start = JOptionPane.showInputDialog(this, req, "" + suggestedStart);

        if (startInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float startTime = 0;
        try
        {
            startTime = Float.parseFloat(startInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
            return;
        }
        spikeOptions.setStartTime(startTime);






        //String stop = JOptionPane.showInputDialog(this, req, "" + suggestedEnd);

        if (stopInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float stopTime = 1000;
        try
        {
            stopTime = Float.parseFloat(stopInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
            return;
        }
        spikeOptions.setStopTime(stopTime);






        //String binSizeString = JOptionPane.showInputDialog(this, req, ""+suggestedBinSize);

        if (binSizeInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }


        float binSize = 1;
        try
        {
            binSize = Float.parseFloat(binSizeInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid histogram bin size", ex, null);
            return;
        }

        if (suggestedBinSize<=0 || suggestedBinSize > (stopTime -startTime))
        {
            GuiUtils.showErrorMessage(logger, "Invalid histogram bin size", null, null);
            return;
        }


        preferredSpikeValsEntered = true;

        double simDuration = (stopTime - startTime);


        int numBins = (int)Math.floor((simDuration)/binSize) + 1;

        //DataSet isiHist = new DataSet("Histogram of spiking of Cell Group "+cellGroup, "...");

        int[] numInEachBin = new int[numBins];



        //for (int cellNum = 0; cellNum < total; cellNum++)
        for(Integer next: orderedCellNums)
        {
            int cellNum = next;
            double[] voltages = null;


            String cellRef = SimulationData.getCellSegRef(cellGroup, cellNum, 0, simRerunFrame.isOnlySomaValues());


            voltages = simRerunFrame.getVoltageAtAllTimes(cellRef);



            boolean[] inBin = new boolean[numBins];

            for (int i = 0; i < times.length; i++)
            {
                double nextVolt = voltages[i];
                double nextTime = times[i];

                int binNumber = (int)Math.floor(nextTime/(double)binSize);
                if (nextVolt>=threshold) inBin[binNumber] = true;
            }
            for (int i = 0; i < inBin.length; i++)
            {
                if (inBin[i]) numInEachBin[i]++;
            }


        }

        for (int i = 0; i < numInEachBin.length; i++)
        {
            float midTime = (i+0.5f)*binSize;
            cellGroupHist.addPoint(midTime, numInEachBin[i]);

        }



        cellGroupHist.setDescription(desc.toString());
        cellGroupHist.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
        PlotterFrame frame = PlotManager.getPlotterFrame(cellGroupHist.getReference());
        updatePlotList();
        frame.addDataSet(cellGroupHist);
        frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
        frame.setVisible(true);



    }
    
    
    private void populationSpikesSync() throws SimulationDataException
    {
         ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

        double[] times = simRerunFrame.getAllTimes();

        ArrayList<String> everyCellGroup = project.cellGroupsInfo.getAllCellGroupNames();

        Vector<String> allNonEmptyCellGroups = new Vector<String>();

        for (int i = 0; i < everyCellGroup.size(); i++)
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(everyCellGroup.get(i));
            if (numInCellGroup > 0) allNonEmptyCellGroups.add(everyCellGroup.get(i));
        }

        String[] allCellGroups = new String[allNonEmptyCellGroups.size()];
        allNonEmptyCellGroups.copyInto(allCellGroups);


        String message = "Please select the Cell Group whose population spikes synchrony histogram you would like to see.";

        String cellGroup = (String)JOptionPane.showInputDialog(this,
                                                      message,
                                                      "Select Cell Group",
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      null,
                                                      allCellGroups,
                                                      allCellGroups[0]);

        if (cellGroup == null)
        {
            logger.logComment("Cancelled...");
            return;
        }


        //int total = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

        ArrayList<PositionRecord> positions = project.generatedCellPositions.getPositionRecords(cellGroup);

        CellChooser myCellChooser = new AllCells();
        String request = "Please select the cells in cell group: " +
            cellGroup + " for which to generate spikes synchrony histogram";

        CellChooserDialog cellChooserDlg = new CellChooserDialog(GuiUtils.getMainFrame(), request, myCellChooser);

        GuiUtils.centreWindow(cellChooserDlg);

        cellChooserDlg.setVisible(true);

        CellChooser cellChooser = cellChooserDlg.getFinalCellChooser();

        cellChooser.initialise(positions);

        if (cellChooser instanceof RegionAssociatedCells)
        {
            RegionAssociatedCells rac = (RegionAssociatedCells) cellChooser;

            rac.setProject(project); // to give info on regions...
        }
        
        ArrayList<Integer> orderedCellNums = null;
        try
        {
            orderedCellNums = cellChooser.getOrderedCellList();
            if (orderedCellNums.size()==1){
                                GuiUtils.showErrorMessage(logger, "Error: please choose at least two cells to have a sincrony analysis", null, this);
                                return;
            }
        }
        catch (CellChooserException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Error getting cell numbers from: " + cellChooser.toString(), ex1, this);
            return;
        }


        StringBuilder desc = new StringBuilder(simRerunFrame.getSimReference()
                                                + ": Spikes synchrony in time of " + cellGroup + " for: "+cellChooser.toNiceString());

        DataSet cellGroupSync = new DataSet(desc.toString(), desc.toString(),
            "ms", "", "Time", "Num spikes in bin");



        String req = "Threshold which will be considered a spike for Cell Group: " +
            cellGroup;



        float suggestedThresh = -20;
        if (preferredSpikeValsEntered) suggestedThresh = spikeOptions.getThreshold();

        //String thresh = JOptionPane.showInputDialog(this, req, "" + suggestedThresh);

        InputRequestElement threshInput = new InputRequestElement("threshold", req, null, suggestedThresh+"", "mV");
        inputs.add(threshInput);


        req = "Start time from which to analyse the spikes synchrony";
        double suggestedStart = 0;
        if (preferredSpikeValsEntered) suggestedStart = spikeOptions.getStartTime();

        InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart+"", "ms");
        inputs.add(startInput);


        req = "Finish time from which to analyse the spikes synchrony";
        double suggestedEnd = times[times.length - 1];
        if (preferredSpikeValsEntered) suggestedEnd = spikeOptions.getStopTime();


        InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd+"", "ms");
        inputs.add(stopInput);


        req = "Please enter the slide bin size for the synchrony graph";
        double suggestedBinSize = Math.min(1, (suggestedEnd -suggestedStart)/100f);


        InputRequestElement slideSizeInput = new InputRequestElement("slidesize", req, null, suggestedBinSize+"", "ms");
        inputs.add(slideSizeInput);



        InputRequest dlg = new InputRequest(null,
                                            "Please enter the parameters for the sinchrony graph",
                                            "Parameters for the synchrony graph",
                                            inputs, true);

        GuiUtils.centreWindow(dlg);

        dlg.setVisible(true);


            if (dlg.cancelled()) return;



        if (threshInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float threshold = 0;
        try
        {
            threshold = Float.parseFloat(threshInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        spikeOptions.setThreshold(threshold);




        //String start = JOptionPane.showInputDialog(this, req, "" + suggestedStart);

        if (startInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float startTime = 0;
        try
        {
            startTime = Float.parseFloat(startInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
            return;
        }
        spikeOptions.setStartTime(startTime);






        //String stop = JOptionPane.showInputDialog(this, req, "" + suggestedEnd);

        if (stopInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float stopTime = 1000;
        try
        {
            stopTime = Float.parseFloat(stopInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
            return;
        }
        spikeOptions.setStopTime(stopTime);






        //String binSizeString = JOptionPane.showInputDialog(this, req, ""+suggestedBinSize);

        if (slideSizeInput.getValue() == null)
        {
            logger.logComment("Cancelled...");
            return;
        }


        float slideSize = 1;
        try
        {
            slideSize = Float.parseFloat(slideSizeInput.getValue());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid slideSize", ex, null);
            return;
        }

        if (slideSize<=0 || slideSize > (stopTime -startTime))
        {
            GuiUtils.showErrorMessage(logger, "Invalid slide size", null, null);
            return;
        }


        preferredSpikeValsEntered = true;

        ArrayList<double[]> spikeSets = new ArrayList<double[]>();

        for (int i = 0; i <  orderedCellNums.size(); i++)  
        {
            int cellNum = orderedCellNums.get(i);
            String cellSegRef = SimulationData.getCellSegRef(cellGroup, cellNum, 0, simRerunFrame.isOnlySomaValues());
           
            double[] voltages = simRerunFrame.getVoltageAtAllTimes(cellSegRef);
            spikeSets.add(i, SpikeAnalyser.getSpikeTimes(voltages, times, threshold, startTime, stopTime));
        }

        cellGroupSync = SpikeAnalyser.getSlidingSpikeSynchrony(spikeSets, times, slideSize, startTime, stopTime);

        

        cellGroupSync.setDescription(desc.toString());
        cellGroupSync.setReference(desc.toString());

        cellGroupSync.setGraphFormat(PlotCanvas.USE_LINES_FOR_PLOT);
        PlotterFrame frame = PlotManager.getPlotterFrame(cellGroupSync.getReference());
        updatePlotList();
        frame.addDataSet(cellGroupSync);
        frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
        frame.setVisible(true);
        
    }


    private void populationRasterplot() throws SimulationDataException
    {
        double[] times = simRerunFrame.getAllTimes();

        logger.logComment("Times (" + times.length + "): " + times[0] + ", " + times[1] + ", ... , " +
                          times[times.length - 1]);

        ArrayList<String> everyCellGroup = project.cellGroupsInfo.getAllCellGroupNames();

        Vector<String> allNonEmptyCellGroups = new Vector<String>();

        for (int i = 0; i < everyCellGroup.size(); i++)
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(everyCellGroup.get(i));
            if (numInCellGroup > 0) allNonEmptyCellGroups.add(everyCellGroup.get(i));
        }

        String[] allCellGroups = new String[allNonEmptyCellGroups.size()];
        allNonEmptyCellGroups.copyInto(allCellGroups);

        String message = "Please select the Cell Group whose rasterplot is to be generated";

        String chosenCellGroup = (String) JOptionPane.showInputDialog(this,
                                                                message,
                                                                "Select Cell Group",
                                                                JOptionPane.QUESTION_MESSAGE,
                                                                null,
                                                                allCellGroups,
                                                                allCellGroups[0]);

        if (chosenCellGroup == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        String req = "Please enter the cutoff threshold which will be considered a spike for Cell Group: " +
            chosenCellGroup;

        float suggestedThresh = -20;
        if (preferredSpikeValsEntered) suggestedThresh = spikeOptions.getThreshold();

        String thresh = JOptionPane.showInputDialog(this, req, "" + suggestedThresh);

        if (thresh == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float threshold = 0;
        try
        {
            threshold = Float.parseFloat(thresh);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        spikeOptions.setThreshold(threshold);


        ArrayList<PositionRecord> positions = project.generatedCellPositions.getPositionRecords(chosenCellGroup);


        CellChooser myCellChooser = new AllCells();
        String request = "Please select the cells in cell group: " +
                                            chosenCellGroup + " for which to generate rasterplot";


        CellChooserDialog cellChooserDlg = new CellChooserDialog(GuiUtils.getMainFrame(), request, myCellChooser);

        GuiUtils.centreWindow(cellChooserDlg);

        cellChooserDlg.setVisible(true);

        CellChooser cellChooser = cellChooserDlg.getFinalCellChooser();

        cellChooser.initialise(positions);

        if (cellChooser instanceof RegionAssociatedCells)
        {
            RegionAssociatedCells rac = (RegionAssociatedCells) cellChooser;

            rac.setProject(project); // to give info on regions...
        }


        ArrayList<Integer> orderedCellNums = null;
        try
        {
            orderedCellNums = cellChooser.getOrderedCellList();
        }
        catch (CellChooserException ex1)
        {
            GuiUtils.showErrorMessage(logger, "Error getting cell numbers from: "+cellChooser.toString(), ex1, this);
            return;
        }


        //int total = project.generatedCellPositions.getNumberInCellGroup(chosenCellGroup);

        PlotterFrame frame = PlotManager.getPlotterFrame("Rasterplot for "+chosenCellGroup + " in "+ this.simRerunFrame.getSimReference());
        updatePlotList();

        frame.setKeepDataSetColours(true);

        frame.setViewMode(PlotCanvas.STACKED_VIEW);

        RasterOptions newRasterOptions = new RasterOptions();
        newRasterOptions.setPercentage(90);
        newRasterOptions.setThickness(2);
        newRasterOptions.setThreshold(threshold);

        //frame.setViewMode(PlotCanvas.STACKED_VIEW);

        frame.rasterise(newRasterOptions);


        for (int j = orderedCellNums.size()-1; j >= 0; j--)
        {
            int cellNum = orderedCellNums.get(j);
            double[] voltages = null;

            String cellRef = SimulationData.getCellSegRef(chosenCellGroup, cellNum, 0, simRerunFrame.isOnlySomaValues());


            voltages = simRerunFrame.getVoltageAtAllTimes(cellRef);

            logger.logComment("Voltages:  (" + voltages.length + "): " + voltages[0] + ", " + voltages[1] + ", ... , " +
                              voltages[voltages.length - 1]);

            DataSet ds = new DataSet("Cell "+ cellNum, "Cell "+ cellNum,
                                     UnitConverter.timeUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol(),
                        "", "", "");


            ds.setGraphColour(Color.black);


            // Note: cropping many of the non spiking times...
            boolean firstSpikeDone = false;
            int numPastSpikeToAdd = 2;
            int numLeftToAdd = numPastSpikeToAdd;

            for (int i = 0; i < times.length; i++)
            {
                if (voltages[i]<threshold)
                {
                    firstSpikeDone = false;
                    if (numLeftToAdd>0)
                    {
                        //System.out.println("Adding after spike low");
                        ds.addPoint(times[i], voltages[i]);
                        numLeftToAdd--;
                    }
                }
                else
                {

                    if (i>0)
                    {
                        if (!firstSpikeDone)
                        {
                            //System.out.println("Adding before spike low");
                            // point just before spike
                            ds.addPoint(times[i-1], voltages[i-1]);
                            firstSpikeDone = true;
                        }
                    }
                    //System.out.println("Adding spike high");
                    ds.addPoint(times[i], voltages[i]);

                    numLeftToAdd = numPastSpikeToAdd;
                }
            }

            frame.addDataSet(ds);

        }

        frame.setVisible(true);



    }

    private void doVerySimpFieldPot()
    {
        FieldPotentialFrame fpf = new FieldPotentialFrame(project, simRerunFrame.getSimulationData(), false);

        GuiUtils.centreWindow(fpf);
        fpf.setVisible(true);
    }


    private void doCrossCorrel()   throws SimulationDataException
    {
        String cellGroupSelected = this.getSelectedCellGroup();

        ArrayList<Integer> selNums = this.getSelectedCellNums();



        if (cellGroupSelected == null || selNums.size()!=1)
        {
            GuiUtils.showErrorMessage(logger, "Please select one of the cells, either by clicking on it or choosing the Cell Group and cell number in the drop down boxes", null, this);
            return;
        }

        ArrayList<PositionRecord> positions = project.generatedCellPositions.getPositionRecords(cellGroupSelected);

        int selcellNum = selNums.get(0);

        CellChooser myCellChooser = new AllCells();
        String request = "Please select the other cells in cell group: " +
                                            cellGroupSelected + " with which to compare the spiking of cell "+ selcellNum;


        CellChooserDialog cellChooserDlg = new CellChooserDialog(GuiUtils.getMainFrame(), request, myCellChooser);

        GuiUtils.centreWindow(cellChooserDlg);

        cellChooserDlg.setVisible(true);

        CellChooser cellChooser = cellChooserDlg.getFinalCellChooser();

        cellChooser.initialise(positions);

        if (cellChooser instanceof RegionAssociatedCells)
        {
            RegionAssociatedCells rac = (RegionAssociatedCells) cellChooser;

            rac.setProject(project); // to give info on regions...
        }


        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement> ();

        InputRequestElement binSizeInput = new InputRequestElement("binSize",
                                                                   "Size of bins for cross correlation", null, "1", "ms");

        inputs.add(binSizeInput);

        InputRequestElement windowInput = new InputRequestElement("window",
                                                                  "Time window to correllate over", null, "100", "ms");


            InputRequestElement threshInput = new InputRequestElement("threshold",
                                                                      "Threshold for spike", null, "-20", "mV");
            inputs.add(threshInput);


        inputs.add(windowInput);

        InputRequest dlg = new InputRequest(null,
                                            "Please enter the parameters for the cross correlations of cell " + selcellNum + " in cell group: " +
                                            cellGroupSelected,
                                            "Please enter the parameters for the cross correlation",
                                            inputs, true);

        GuiUtils.centreWindow(dlg);

        dlg.setVisible(true);
        /*
        ViewSynchro2D view2d = new ViewSynchro2D(cellGroupSelected
                                                 , positions, ViewCanvas.Z_X_NEGY_DIR, false);
        view2d.pack();
        view2d.validate();

        GuiUtils.centreWindow(view2d);*/

        //Color selectedColor = Color.red;

        //Point3f posSelected = project.generatedCellPositions.getOneCellPosition(cellGroupSelected, selcellNum);


        String cellRef = SimulationData.getCellRef(cellGroupSelected, selcellNum);

        //ArrayList<String> cellSegRefs = simRerunFrame.getCellSegRefsForCellRef(cellRef);

        ArrayList<DataStore> dsForCellRef = simRerunFrame.getDataForCellSegRef(cellRef, false);


        if (dsForCellRef.isEmpty())
        {
            GuiUtils.showErrorMessage(logger, "There is no voltage data for cell number: "
                                      +selcellNum+" in group "+ cellGroupSelected, null, this);
            return;
        }

        double[] selectedVoltages = simRerunFrame.getVoltageAtAllTimes(cellRef);

        double[] times = simRerunFrame.getAllTimes();

        logger.logComment("Changing colour of : " + positions.size()+" cells");



        PlotterFrame frame = PlotManager.getPlotterFrame("Crosscorrelations between cell:"
                                                         +selcellNum+" and "+cellChooser.toNiceString()
                                                         +" from: "+cellGroupSelected);

        //for (PositionRecord pos2: positions)

        try
        {
            while (true)
            {
                int nextCellIndex = cellChooser.getNextCellIndex();

                //view2d.add
                if (nextCellIndex== selcellNum)
                {
      ////              view2d.updateColour(selectedColor, cellGroupSelected, selcellNum, true);
                }
                else
                {

                    cellRef = SimulationData.getCellRef(cellGroupSelected, nextCellIndex);

                    dsForCellRef = simRerunFrame.getDataForCellSegRef(cellRef, false);

                    if (!dsForCellRef.isEmpty())
                    {

                        double[] voltages = simRerunFrame.getVoltageAtAllTimes(cellRef);

                        DataSet ds = new DataSet(cellRef, cellRef, "ms", "", "Offset", "Cross correlation");

                        int binSize = Integer.parseInt(binSizeInput.getValue());
                        float window = Float.parseFloat(windowInput.getValue());

                        int binNum = (int) Math.ceil(window / binSize);

                        float[] cc = SpikeAnalyser.crossCorrelation(selectedVoltages,
                                                                    voltages,
                                                                    times,
                                                                    Float.parseFloat(threshInput.getValue()),
                                                                    binSize,
                                                                    binNum,
                                                                    binNum,
                                                                    0);

                        for (int i = 0; i <= binNum * 2; i++)
                        {
                            ds.addPoint(i - binNum, cc[i]);
                        }
                        frame.addDataSet(ds);

                   //////     double phase = ds.getMaxY()[0];


                   ///////     view2d.updateValue( (float) phase, cellGroupSelected, nextCellIndex, true);

                    }

                }
            }
        }
        catch (AllCellsChosenException e)
        {
            logger.logComment("All done...:");
        }
        catch (CellChooserException e2)
        {
            GuiUtils.showErrorMessage(logger, "Error when getting next cell position from " + cellChooser, e2, this);
        }

        frame.setVisible(true);

    }



    private void populationISIHisto()   throws SimulationDataException
    {
        double[] times = simRerunFrame.getAllTimes();

        logger.logComment("Times ("+times.length+"): "+ times[0]+", "+ times[1]+", ... , "+ times[times.length-1]);

        //String[] allCellGroups = project.cellGroupsInfo.getAllCellGroupNamesArray();

        ArrayList<String> everyCellGroup = project.cellGroupsInfo.getAllCellGroupNames();

        Vector<String> allNonEmptyCellGroups = new Vector<String>();

        for (int i = 0; i < everyCellGroup.size(); i++)
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(everyCellGroup.get(i));
            if (numInCellGroup > 0) allNonEmptyCellGroups.add(everyCellGroup.get(i));
        }

        String[] allCellGroups = new String[allNonEmptyCellGroups.size()];
        allNonEmptyCellGroups.copyInto(allCellGroups);


        String message = "Please select the Cell Group whose population ISI histogram you would like to see.";

        String cellGroup = (String)JOptionPane.showInputDialog(this,
                                                      message,
                                                      "Select Cell Group",
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      null,
                                                      allCellGroups,
                                                      allCellGroups[0]);

        if (cellGroup == null)
        {
            logger.logComment("Cancelled...");
            return;
        }


        int total = project.generatedCellPositions.getNumberInCellGroup(cellGroup);

        StringBuilder desc = new StringBuilder("Cell Group : " + cellGroup + " ISI histogram\n");

        DataSet popISIHist = new DataSet(simRerunFrame.getSimReference()
                                                + ": ISI histogram of " + cellGroup, desc.toString(), "ms", "", "Interspike interval", "Number in bin");


        String req = "Please enter the cutoff threshold which will be considered a spike for Cell Group: " +
            cellGroup;



        float suggestedThresh = -20;
        if (preferredSpikeValsEntered) suggestedThresh = spikeOptions.getThreshold();

        String thresh = JOptionPane.showInputDialog(this, req, "" + suggestedThresh);

        if (thresh == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float threshold = 0;
        try
        {
            threshold = Float.parseFloat(thresh);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        spikeOptions.setThreshold(threshold);

        req = "Please enter the start time from which to analyse the ISIs for Cell Group: " + cellGroup;
        float suggestedStart = 0;
        if (preferredSpikeValsEntered) suggestedStart = spikeOptions.getStartTime();

        String start = JOptionPane.showInputDialog(this, req, "" + suggestedStart);

        if (start == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float startTime = 0;
        try
        {
            startTime = Float.parseFloat(start);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
            return;
        }
        spikeOptions.setStartTime(startTime);

        req = "Please enter the finish time from which to analyse the ISIs";
        double suggestedEnd = times[times.length-1];
        if (preferredSpikeValsEntered) suggestedEnd = spikeOptions.getStopTime();

        String stop = JOptionPane.showInputDialog(this, req, "" + suggestedEnd);

        if (stop == null)
        {
            logger.logComment("Cancelled...");
            return;
        }

        float stopTime = 1000;
        try
        {
            stopTime = Float.parseFloat(stop);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
            return;
        }
        spikeOptions.setStopTime(stopTime);



        req = "Please enter the bin size for the histogram";
        float suggestedBinSize = Math.min(1, (stopTime -startTime)/100f);


        String binSizeString = JOptionPane.showInputDialog(this, req, ""+suggestedBinSize);

        if (binSizeString == null)
        {
            logger.logComment("Cancelled...");
            return;
        }


        if (binSizeString==null) return; // i.e. cancelled


        float binSize = 1;
        try
        {
            binSize = Float.parseFloat(binSizeString);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid histogram bin size", ex, null);
            return;
        }

        if (suggestedBinSize<=0 || suggestedBinSize > (stopTime -startTime))
        {
            GuiUtils.showErrorMessage(logger, "Invalid histogram bin size", null, null);
            return;
        }


        req = "Please enter the maximum ISI value to be plotted";
        float suggestedMax = (stopTime -startTime)/10f;


        String maxString = JOptionPane.showInputDialog(this, req, ""+suggestedMax);

        if (maxString==null) return; // i.e. cancelled


        float maxSize = 1;
        try
        {
            maxSize = Float.parseFloat(maxString);
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Invalid maximum ISI Histogram size", ex, null);
            return;
        }



        preferredSpikeValsEntered = true;

        //double simDuration = (stopTime - startTime);


        //

        //DataSet isiHist = new DataSet("Histogram of spiking of Cell Group "+cellGroup, "...");

        ArrayList<Double> allInterSpikeIntervals = new ArrayList<Double>();

        for (int cellNum = 0; cellNum < total; cellNum++)
        {
            double[] voltages = null;


            String cellRef = SimulationData.getCellSegRef(cellGroup, cellNum, 0, simRerunFrame.isOnlySomaValues());

            logger.logComment("Data for soma only present...");
            voltages = simRerunFrame.getVoltageAtAllTimes(cellRef);

            logger.logComment("Voltages:  ("+voltages.length+"): "+ voltages[0]+", "+ voltages[1]+", ... , "+ voltages[voltages.length-1]);

            ArrayList<Double> latestInterSpikeIntervals
                = SpikeAnalyser.getInterSpikeIntervals(voltages,
                                                       times,
                                                       threshold,
                                                       startTime,
                                                       stopTime);

            allInterSpikeIntervals.addAll(latestInterSpikeIntervals);
        }



        //int numBins = (int)Math.floor((simDuration)/binSize) + 1;

        int numBins = Math.round((maxSize)/binSize);


        for (int i = 0; i < numBins; i++)
        {
            float startISI = i*binSize;
            float endISI = (i+1)*binSize;

            int totalHere = 0;

            for (int j = 0; j < allInterSpikeIntervals.size(); j++)
            {
                    double isi = (allInterSpikeIntervals.get(j)).doubleValue();

                    if (isi>=startISI && isi<endISI)
                    {
                        totalHere++;
                    }
            }
            popISIHist.addPoint((endISI+startISI)/2f, totalHere);

        }
        logger.logComment("Done bin allocation...");
        ///check more...
        boolean warn = false;
        for (int j = 0; j < allInterSpikeIntervals.size(); j++)
        {
            double isi = allInterSpikeIntervals.get(j).doubleValue();

            if (isi >= maxSize)
            {
                warn = true;
            }
        }
        if (warn)
        {
            GuiUtils.showErrorMessage(logger, "Warning. The maximum ISI you have chosen to be plotted, "
                                      + maxSize + ", is exceeded by at least one of the ISIs\n"
                                      +"in the original spike train, and so not all ISIs will be included on this histogram.", null, null);
              }




        popISIHist.setDescription(desc.toString());
        popISIHist.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
        PlotterFrame frame = PlotManager.getPlotterFrame(popISIHist.getReference());
        updatePlotList();
        frame.addDataSet(popISIHist);
        frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
        frame.setVisible(true);



    }




}
