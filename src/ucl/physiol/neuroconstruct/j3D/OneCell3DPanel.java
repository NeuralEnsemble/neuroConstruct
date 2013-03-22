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
import javax.swing.event.*;

import com.sun.j3d.utils.behaviors.vp.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Panel which will only show a single cell in 3D. Can be run standalone or as
 * part of neuroConstruct application. The segments/sections can be modified
 * through this panel
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class OneCell3DPanel extends Base3DPanel implements UpdateOneCell
{
    private static ClassLogger logger = new ClassLogger("OneCell3DPanel");

    public static String highlightSecSegs = "Pick Sections/Segments";
    public static String highlightGroups = "Groups";
    public static String highlightParamGroups = "Parameterised Groups";
    public static String highlightSynapseLocations = "Synaptic Conn Locations";
    public static String highlightSectionTypes = "Section Types";
    public static String highlightDensMechs = "Cell density mechanisms";

    private Color segmentHighlightMain = new Color(250,80,40);
    private Color segmentHighlightSecondary = new Color(250,250,40);
    
    private Color segmentHighlightNoGmax = Color.BLUE.brighter().brighter();

    private BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 20000);

    private Canvas3D canvas3D = null;

    private SegmentSelector segmentSelector = null;

    private boolean initialising = false;

    private ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    private Segment latestSelectedSegment = null;

    private HashMap<String, JButton> sectionTypeButtons = new HashMap<String, JButton>();
    private HashMap<String, JRadioButton> synLocRadioButtons = new HashMap<String, JRadioButton>();
    
    private ButtonGroup synLocButtonGroup = new ButtonGroup();
    private HashMap<String, JRadioButton> densMechRadioButtons = new HashMap<String, JRadioButton>();
    private ButtonGroup densMechButtonGroup = new ButtonGroup();
    private HashMap<String, JRadioButton> groupRadioButtons = new HashMap<String, JRadioButton>();
    private ButtonGroup groupButtonGroup = new ButtonGroup();
    private ButtonGroup paramGroupButtonGroup = new ButtonGroup();


    private JPanel jPanelControls = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JSlider jSliderViewDistance = new JSlider();

    private TransformGroup viewTG = null;

    private SimpleUniverse simpleU = null;

    private TransformGroup scaleTG = null;
    private int prevValSlider = 20;

    private Project project = null;

    /**
     * The original morphology passed to the class
     */
    private Cell origCell = null;


    /**
     * The morphology after possible projection to a simpler/simulator specific form
     */
    private Cell displayedCell = null;

    private Frame myParent = null;


    private Container containerFor3D = this;

    private float optimalScale = 1;


    private OneCell3D myOneCell3D = null;


    JLabel jLabelZoom = new JLabel();
    JComboBox jComboBoxHighlight = new JComboBox();
    JPanel jPanelColourControls = new JPanel();
    JPanel jPanelMainControls = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton jButtonEditGroups = new JButton();
    JButton jButtonEditSynapses = new JButton();
    JButton jButtonEditChanMechs = new JButton();
    JButton jButtonCellInfo = new JButton();
    JPanel jPanelExtraButton = new JPanel();
    JLabel jLabelInfo = new JLabel();
    JComboBox jComboBoxView = new JComboBox();
    JButton jButtonFind = new JButton();
    JButton jButtonDetach = new JButton();


    JLabel warningLabel1 =  new JLabel("");
    JLabel warningLabel2 =  new JLabel("");

    JLabel sectionChanMechInfo =  new JLabel("");


    JButton jButtonMoreInfo = new JButton("Edit...");
    JButton jButtonBiophysInfo = new JButton("Biophysics");
    JButton jButtonSomaInfo = new JButton("Click on a segment to view/edit details");




    public OneCell3DPanel(Cell originalCell, Project project, Frame parent)
    {
        initialising = true;
        logger.logComment("Creating OneCell3DPanel...");

        this.origCell = originalCell;

        //SimpleProjection sp = new SimpleProjection();

        this.displayedCell = origCell; // for now...


        this.myParent = parent;

        this.project = project;

        myOneCell3D = new OneCell3D(displayedCell, 0, project);

        logger.logComment("Created OneCell3D...");

        float maxExtent = CellTopologyHelper.getYExtentOfCell(displayedCell, false, true);

        if (CellTopologyHelper.getXExtentOfCell(displayedCell, false, true)>maxExtent)
            maxExtent = CellTopologyHelper.getXExtentOfCell(displayedCell, false, true);

        optimalScale = 7f/maxExtent + 0.0035f; // seems to make most of the cells
                                                 // we deal with fit to screen...

        try
        {
            jbInit();
            logger.logComment("Done jbInit...");
            extraInit();
            logger.logComment("Done extraInit...");
            this.updateButtonEnabling();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        repaint3DScene();

        this.myOneCell3D.applyGroupColouring();

        findCell();

        initialising = false;

        logger.logComment("Added BranchGraph...");
    }



    private void extraInit()
    {
        jComboBoxHighlight.addItem(highlightSecSegs);
        jComboBoxHighlight.addItem(highlightGroups);
        if (ParameterisedGroup.allowInhomogenousMechanisms) // can disable func while testing
        {
            jComboBoxHighlight.addItem(highlightParamGroups);
        }
        jComboBoxHighlight.addItem(highlightSectionTypes);
        jComboBoxHighlight.addItem(highlightSynapseLocations);
        jComboBoxHighlight.addItem(highlightDensMechs);

        jComboBoxHighlight.setLightWeightPopupEnabled(false);
        jComboBoxView.setLightWeightPopupEnabled(false);

        jButtonSomaInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSegInfo_actionPerformed(e);
            }
        });

        jPanelColourControls.add(jButtonSomaInfo);


        String dendDispOpt = project.proj3Dproperties.getDisplayOption();

        Vector displayOptions = Display3DProperties.getDisplayOptions(true);

        for (int i = 0; i < displayOptions.size(); i++)
        {
            Object next = displayOptions.elementAt(i);
            jComboBoxView.addItem(next);
            if (next.toString().equals(dendDispOpt))
                jComboBoxView.setSelectedItem(next);
        }
        //jComboBoxView.setSelectedItem(dendDispOpt);

        if (project.proj3Dproperties.isCompartmentalisationDisplay())
        {
            this.jButtonBiophysInfo.setEnabled(false);
            this.jButtonMoreInfo.setEnabled(false);
            this.jButtonSomaInfo.setEnabled(false);
        }


        jLabelZoom.setToolTipText(toolTipText.getToolTip("Zoom function"));
        jSliderViewDistance.setToolTipText(toolTipText.getToolTip("Zoom function"));

        jComboBoxView.setToolTipText(toolTipText.getToolTip("3D View mode"));
        jButtonFind.setToolTipText(toolTipText.getToolTip("3D Find one cell"));


        warningLabel1.setForeground(Color.red);
        warningLabel1.setToolTipText("Check on the validity of the displayed cell");

    }


    public BranchGroup createSceneGraph()
    {
        BranchGroup objRoot = new BranchGroup();

        Transform3D t3d = new Transform3D();
        t3d.setScale(optimalScale); // will be different for different cells...

        scaleTG = new TransformGroup(t3d);

        logger.logComment("Setting scale to: "+ t3d.getScale());

        if (project.proj3Dproperties.getShow3DAxes())
        {
            Utils3D.addAxes(scaleTG, 0.005); // gives axes of 100 units length
        }
        

        Color backgroundColour = project.proj3Dproperties.getBackgroundColour3D();
        Utils3D.addBackground(bounds, objRoot, backgroundColour);

        objRoot.addChild(scaleTG);


        // Get the TransformGroup from the OneCell3D
        TransformGroup tgOneCell = myOneCell3D.createCellTransformGroup();


        scaleTG.addChild(tgOneCell);

        SectionPicker pickingBehav = new SectionPicker(objRoot,simpleU.getCanvas(),bounds, this);

        objRoot.addChild(pickingBehav);

        objRoot.compile();
        return objRoot;
    }


    /**
     * An attempt to clean up and free as much memory as possible when the 3D view is closed. Note: quick and dirty,
     * Java 3D specialist needed to clean up properly...
     */
    @Override
    public void destroy3D()
    {
        System.out.println("------------     Clearing memory...");

        GeneralUtils.printMemory(true);


        simpleU.cleanup();

        viewTG = null;
        scaleTG = null;
        simpleU = null;
        project = null;
        canvas3D = null;
        segmentSelector = null;
        latestSelectedSegment = null;
        origCell = null;
        displayedCell = null;
        myParent = null;
        myOneCell3D = null;

        System.gc();
        System.gc();
        GeneralUtils.printMemory(true);

        System.out.println("------------     Memory hopefully cleared...");
    }



    private OneCell3DPanel()
    {

    }

    private void jbInit() throws Exception
    {
        this.setLayout(borderLayout1);
        jPanelControls.setBorder(BorderFactory.createEtchedBorder());
        jPanelControls.setMaximumSize(new Dimension(400, 170));
        jPanelControls.setMinimumSize(new Dimension(400, 100));
        jPanelControls.setPreferredSize(new Dimension(400, 100));
        jPanelControls.setLayout(borderLayout2);
        jSliderViewDistance.addChangeListener(new javax.swing.event.
                                              ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                jSliderViewDistance_stateChanged(e);
            }
        });
        jSliderViewDistance.setOrientation(JSlider.HORIZONTAL);
        jSliderViewDistance.setMaximum(200);
        jSliderViewDistance.setPreferredSize(new Dimension(150, 24));
        jSliderViewDistance.setValue(20);
        jLabelZoom.setText("Zoom:");

        jLabelZoom.setBackground(Color.red);
        jLabelZoom.setToolTipText(ToolTipHelper.getInstance().getToolTip("3D Gui Zoom"));

        jPanelColourControls.setBorder(BorderFactory.createEtchedBorder());
        jPanelColourControls.setMaximumSize(new Dimension(400, 120));
        jPanelColourControls.setMinimumSize(new Dimension(400, 50));
        jPanelColourControls.setPreferredSize(new Dimension(400, 50));
        jComboBoxHighlight.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxHighlight_itemStateChanged(e);
            }
        });
        jPanelMainControls.setMaximumSize(new Dimension(150, 40));
        jPanelMainControls.setMinimumSize(new Dimension(150, 40));
        jPanelMainControls.setPreferredSize(new Dimension(150, 40));
        jButtonEditGroups.setText("Edit Groups");
        jButtonEditSynapses.setText("Edit Synaptic Locations");

        jButtonEditGroups.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditGroups_actionPerformed(e);
            }
        });
        jButtonEditSynapses.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditSynapses_actionPerformed(e);
            }
        });

        jButtonEditChanMechs.setText("Edit Density Mechs");
        jButtonEditChanMechs.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEditChanMechs_actionPerformed(e);
            }
        });

        jButtonCellInfo.setText("Cell Info");
        jButtonCellInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellInfo_actionPerformed(e);
            }
        });


        jLabelInfo.setText("Click on a segment above for more info");

        jComboBoxView.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxToggleView_itemStateChanged(e);
            }
        });
        jButtonFind.setText("0");

        jButtonFind.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonFind_actionPerformed(e);
            }
        });

        jButtonDetach.setText("^");
        jButtonDetach.setToolTipText("Display 3D in separate window");

        jButtonDetach.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDetach_actionPerformed(e);
            }
        });


        this.add(jPanelControls, BorderLayout.SOUTH);
        jPanelControls.add(jPanelMainControls,  BorderLayout.SOUTH);
        jPanelMainControls.add(jComboBoxView, null);
        jPanelMainControls.add(jButtonFind, null);
        jPanelMainControls.add(jButtonDetach, null);
        jPanelMainControls.add(jLabelZoom, null);
        jPanelMainControls.add(jSliderViewDistance, null);
        jPanelMainControls.add(jComboBoxHighlight, null);
        jPanelControls.add(jPanelColourControls,  BorderLayout.NORTH);
        jPanelColourControls.add(jLabelInfo, null);

        jPanelMainControls.add(jPanelExtraButton, null);
    }


    void jSliderViewDistance_stateChanged(ChangeEvent e)
    {
        JSlider source = (JSlider) e.getSource();
        int val = source.getValue();

        if (viewTG!=null)
        {
            Transform3D t3d = new Transform3D();

            float scaleFactor = .3f;

            if (prevValSlider<val)  // i.e. value increasing
            {
                t3d.set(new Vector3d(0.0, 0, scaleFactor));
            }
            else
            {
                t3d.set(new Vector3d(0.0, 0, -1*scaleFactor));
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
            repaint3DScene();
        }
    }


    void resetColours()
    {
        logger.logComment("Resetting the colours...");
        jPanelColourControls.removeAll();
        jPanelColourControls.repaint();
        myOneCell3D.resetCellAppearance();
    }



    public final void repaint3DScene()
    {
        logger.logComment("Repainting the 3D objets...");

        //GeneralUtils.timeCheck("Repainting...");

        Object selected = jComboBoxView.getSelectedItem();

        if (selected instanceof MorphCompartmentalisation)
        {

            MorphCompartmentalisation mp = (MorphCompartmentalisation)selected;

            logger.logComment("Projecting to: "+ mp);
            displayedCell = mp.getCompartmentalisation(this.origCell);
        }
        else
        {
            displayedCell = this.origCell;
        }

        myOneCell3D = new OneCell3D(displayedCell, 0, project);

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

        if (canvas3D!=null) this.remove(canvas3D);
        canvas3D = new Canvas3D(config);

        if (!containerFor3D.isVisible()) containerFor3D= this;

        containerFor3D.add("Center", canvas3D);

        Transform3D lastT3D = getLastViewingTransform3D();

        simpleU = new SimpleUniverse(canvas3D);

        simpleU.getViewer().getView().setSceneAntialiasingEnable(useAA);

        simpleU.getViewer().getView().setBackClipDistance(1000);

        BranchGroup scene = createSceneGraph();


        this.setViewedObject(this.displayedCell);

        logger.logComment("Created scene graph...");

        logger.logComment("Added gui...");


        PlatformGeometry pg = new PlatformGeometry();

        if (lastT3D==null)
        {
            simpleU.getViewingPlatform().setNominalViewingTransform();
        }
        else
        {
            setLastViewingTransform3D(lastT3D);
        }

        OrbitBehavior orbit = new OrbitBehavior(canvas3D, OrbitBehavior.REVERSE_ALL);

        orbit.setSchedulingBounds(bounds);
        simpleU.getViewingPlatform().setViewPlatformBehavior(orbit);

        MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();

        viewTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);
        Utils3D.addBackgroundLights(bounds, pg);

        simpleU.getViewingPlatform().setPlatformGeometry( pg );
        simpleU.addBranchGraph(scene);

        this.validate();



        ValidityStatus validity = CellTopologyHelper.getValidityStatus(this.displayedCell);

        ValidityStatus bioValidity = CellTopologyHelper.getBiophysicalValidityStatus(this.displayedCell, this.project);

        if (validity.isError()||bioValidity.isError())
        {
            logger.logError("Invalid cell:" + validity);

            //jPanelExtraButton.add(warningLabel);
            warningLabel1.setForeground(ValidityStatus.VALIDATION_COLOUR_ERROR_OBJ);
            warningLabel1.setText("Cell not valid");
        }
        else if (validity.isWarning()||bioValidity.isWarning())
        {
            logger.logError("Warning on cell:" + validity);

            //jPanelExtraButton.add(warningLabel);
            warningLabel1.setForeground(ValidityStatus.VALIDATION_COLOUR_WARN_OBJ);
            warningLabel1.setText("Cell has warnings");
        }

        else
        {
            warningLabel1.setText("");
        }


        //GeneralUtils.timeCheck("Done repainting...");

    }



    void highlightSynLocs(Component cause)
    {
        String selectedSynType = null;
        resetColours();

        try
        {
            JRadioButton selectedRadioButton = (JRadioButton)cause;
            selectedSynType = selectedRadioButton.getText();
            if (selectedSynType.indexOf("(")>0)
                selectedSynType = selectedSynType.substring(0, selectedSynType.indexOf("(")).trim();
        }
        catch (ClassCastException ex)
        {
            // ignore, probably update due to change in combo box...
        }

        logger.logComment("The selected type is: "+ selectedSynType);

        if (synLocRadioButtons.isEmpty())
        {
            ArrayList<String> allAllowedTypes = this.displayedCell.getAllAllowedSynapseTypes();
            for (int i = 0; i < allAllowedTypes.size(); i++)
            {
                String nextSynapseType = allAllowedTypes.get(i);
                if (selectedSynType==null) selectedSynType = nextSynapseType;
                Vector<String> groups = displayedCell.getGroupsWithSynapse(nextSynapseType);
                StringBuilder name = new StringBuilder(nextSynapseType +" (");
                for (String group :groups)
                {
                    if (!group.equals(groups.firstElement())) name.append(", ");
                    name.append(group);
                }
                name.append(")");
                JRadioButton next = createGroupRadioButton(name.toString(), synLocButtonGroup);

                synLocRadioButtons.put(nextSynapseType, next);
            }
        }

        Set<String> synTypeNames = synLocRadioButtons.keySet();

        if (synTypeNames.isEmpty())
        {
            jPanelColourControls.add(new JLabel("No synapse types found in this cell", JLabel.CENTER));
        }

        for (String synTypeName: synTypeNames)
        {

            JRadioButton nextRadioButton = synLocRadioButtons.get(synTypeName);
            jPanelColourControls.add(nextRadioButton);

            logger.logComment("Checking: "+synTypeName+" vs: "+ selectedSynType);

            if (synTypeName.equals(selectedSynType))
            {
                nextRadioButton.setSelected(true);
            }
            else
            {
               nextRadioButton.setSelected(false);
            }
        }
        jPanelColourControls.repaint();

        ColourGenerator colourGen = new ColourGenerator();
        Color c = colourGen.getNextColour();

        if (selectedSynType==null) return;

        Vector groups = this.displayedCell.getGroupsWithSynapse(selectedSynType);

        for (int i = 0; i < groups.size(); i++)
        {
            highlightSingleGroup((String)groups.elementAt(i), c);
        }

    }
    
    
    void highlightParamGroups(Component cause)
    {
        logger.logComment("Highlighting param groups...");
        resetColours();
        
        String selectedParamGroup = null;

        try
        {
            JRadioButton selectedRadioButton = (JRadioButton) cause;
            selectedParamGroup = selectedRadioButton.getText();
        }
        catch (Exception ex)
        {
            // ignore, probably update due to change in combo box...
        }
        
        Vector<ParameterisedGroup> pgs = displayedCell.getParameterisedGroups();
        
        if (pgs.isEmpty())
        {
            JButton jButtonAddDefaultParamGroups = new JButton("Add example parameterised groups (beta impl, subject to change...)");
            
            jPanelColourControls.add(jButtonAddDefaultParamGroups, null);
            
            final Component compCause = cause;
            
            jButtonAddDefaultParamGroups.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    
                    ParameterisedGroup pg1 = new ParameterisedGroup("ZeroToOneOverCell", 
                                                                   Section.ALL, 
                                                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                                                   ProximalPref.MOST_PROX_AT_0, 
                                                                   DistalPref.MOST_DIST_AT_1,
                                                                   ParameterisedGroup.DEFAULT_VARIABLE);
                    
                    displayedCell.addParameterisedGroup(pg1);
                    
                    ParameterisedGroup pg2 = new ParameterisedGroup("PathLengthOverCell", 
                                                                   Section.ALL, 
                                                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                                                   ProximalPref.NO_TRANSLATION, 
                                                                   DistalPref.NO_NORMALISATION,
                                                                   ParameterisedGroup.DEFAULT_VARIABLE);
                    
                    displayedCell.addParameterisedGroup(pg2);
                    
                    ParameterisedGroup pg3 = new ParameterisedGroup("PathLengthOverDendrites", 
                                                                   Section.DENDRITIC_GROUP, 
                                                                   Metric.PATH_LENGTH_FROM_ROOT, 
                                                                   ProximalPref.MOST_PROX_AT_0, 
                                                                   DistalPref.NO_NORMALISATION,
                                                                   ParameterisedGroup.DEFAULT_VARIABLE);
                    
                    displayedCell.addParameterisedGroup(pg3);
                    
                    logger.logComment("Param grpos:"+ displayedCell.getParameterisedGroups());
                    
                    
                    highlightParamGroups(compCause);
                    
                    project.markProjectAsEdited();
                }
            });
        }
        else
        {
            ParameterisedGroup selParamGroup = null;
            JRadioButton selButton = null;
            
            for(ParameterisedGroup pg: pgs)
            {
                logger.logComment("Adding button for: "+pg);
                
                JRadioButton button = createGroupRadioButton(pg.getName(), paramGroupButtonGroup);
                jPanelColourControls.add(button);
                
                if (selectedParamGroup!=null && selectedParamGroup.equals(pg.getName()))
                {
                    logger.logComment("Selected seems to have been: "+pg);
                    button.setSelected(true);
                    selParamGroup = pg;
                }
            }
            if (selParamGroup==null)
            {
                //selParamGroup = pgs.firstElement();
                ((JRadioButton)jPanelColourControls.getComponent(0)).setSelected(true);
                selParamGroup = pgs.firstElement();
            }
            
            highlightParamGroup(selParamGroup);
            //JButton edit = new JButton("Edit");
            
            JButton add = new JButton("Add");
            jPanelColourControls.add(add);
            //jPanelColourControls.add(edit);
            
            add.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    logger.logComment("Adding a ParamGroup");
                    
                    Vector<String> groups = displayedCell.getAllGroupNames();
                    
                    String group = (String)JOptionPane.showInputDialog(null, 
                                   "Please select the group to parameterise over",
                                   "Select group", 
                                   JOptionPane.QUESTION_MESSAGE , 
                                   null,
                                   groups.toArray(), 
                                   Section.ALL);
                    
                    if (group==null || group.length()==0) return;
                    
                    String name = JOptionPane.showInputDialog(null, 
                                   "Please enter the name of the new Parameterised Group",
                                   "ParamGroup_"+group);
                    
                    if (name==null || name.length()==0) return;
                    
                    Metric metric = (Metric)JOptionPane.showInputDialog(null, 
                                   "Please select the metric for the parameterisation",
                                   "Select metric", 
                                   JOptionPane.QUESTION_MESSAGE , 
                                   null,
                                   Metric.values(), 
                                   Metric.values()[0]);
                    
                    
                    ProximalPref pp = (ProximalPref)JOptionPane.showInputDialog(null, 
                                   "Please select the preference for the proximal point of the parameterisation",
                                   "Select proximal", 
                                   JOptionPane.QUESTION_MESSAGE , 
                                   null,
                                   ProximalPref.values(), 
                                   ProximalPref.values()[0]);
                    
                    DistalPref dp = (DistalPref)JOptionPane.showInputDialog(null, 
                                   "Please select the preference for the distal point of the parameterisation",
                                   "Select distal", 
                                   JOptionPane.QUESTION_MESSAGE , 
                                   null,
                                   DistalPref.values(), 
                                   DistalPref.values()[0]);
                    
                    ParameterisedGroup pg = new ParameterisedGroup(name, group, metric, pp, dp,
                                                                   ParameterisedGroup.DEFAULT_VARIABLE);
                    
                    displayedCell.getParameterisedGroups().add(pg);
                    
                    highlightParamGroups(null);
                }
            });
            
            jPanelColourControls.repaint();

        }
        
        
    }


    void highlightGroups(Component cause)
    {
        logger.logComment("Highlighting groups...");
        String selectedGroup = null;
        resetColours();

        try
        {
            JRadioButton selectedRadioButton = (JRadioButton) cause;
            selectedGroup = selectedRadioButton.getText();
        }
        catch (ClassCastException ex)
        {
            // ignore, probably update due to change in combo box...
        }

        logger.logComment("The selected type is: " + selectedGroup);

        if (groupRadioButtons.isEmpty())
        {
            Vector allGroups = this.displayedCell.getAllGroupNames();

            for (int i = 0; i < allGroups.size(); i++)
            {
                String nextGroup = (String) allGroups.elementAt(i);

                JRadioButton next
                    = createGroupRadioButton(nextGroup,
                                        groupButtonGroup);

                if (selectedGroup == null) selectedGroup = nextGroup;

                groupRadioButtons.put(next.getText(), next);
            }
        }
        if (groupRadioButtons.size()>12)
        {
            int height = ((int)(groupRadioButtons.size()/7f) * 32) + 70;
            
            jPanelControls.setPreferredSize(new Dimension(400, Math.min(height, 300)));
            
            jPanelColourControls.setPreferredSize(new Dimension(400, height));
        }
        
        Set<String> radButtonNames = groupRadioButtons.keySet();
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(radButtonNames);
        
        names = (ArrayList<String>)GeneralUtils.reorderAlphabetically(names, true);
        
        if (names.isEmpty())
        {
            jPanelColourControls.add(new JLabel("No groups found in this cell", JLabel.CENTER));
        }
        for(String name: names)
        {
            JRadioButton nextRadioButton = groupRadioButtons.get(name);
            jPanelColourControls.add(nextRadioButton);
            if (nextRadioButton.getText().equals(selectedGroup))
            {
                nextRadioButton.setSelected(true);
            }
            else
            {
                nextRadioButton.setSelected(false);
            }
        }
        jPanelColourControls.repaint();

        ColourGenerator colourGen = new ColourGenerator();
        Color c = colourGen.getNextColour();

        highlightSingleGroup(selectedGroup, c);
    }



    void highlightParamGroup(ParameterisedGroup pg)
    {
        try
        {
            logger.logComment("Highlighting param group: " + pg);
            if (pg == null)
            {
                return;
            }
            ArrayList<Segment> segments = this.displayedCell.getSegmentsInGroup(pg.getGroup());

            double min = pg.getMinValue(this.displayedCell);
            double max = pg.getMaxValue(this.displayedCell);

            for (int i = 0; i < segments.size(); i++)
            {
                Segment nextSegment = segments.get(i);

                double val = pg.evaluateAt(displayedCell, nextSegment, 0.5f);

                double fract = (val - min) / (max - min);

                Color c = GeneralUtils.getFractionalColour(segmentHighlightSecondary, segmentHighlightMain, fract);
                Appearance app = Utils3D.getGeneralObjectAppearance(c);
                
                myOneCell3D.setSegmentAppearance(app, nextSegment.getSegmentId());
                
            }
            
            JLabel valMin = new JLabel(" " + min + " ");
            valMin.setOpaque(true);
            valMin.setBackground(segmentHighlightSecondary);
            jPanelColourControls.add(valMin);
            
            JLabel valMax = new JLabel(" " + max + " ");
            valMax.setOpaque(true);
            valMax.setBackground(segmentHighlightMain);
            jPanelColourControls.add(valMax);
            
        }
        catch (ParameterException ex)
        {
            GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ pg+" on cell: "+displayedCell, ex, myParent);
        }
    }


    void highlightSingleGroup(String group, Color c)
    {
        Appearance app = Utils3D.getGeneralObjectAppearance(c);

        logger.logComment("Highlighting group: "+ group + " in: "+ c);


        Vector segments = this.displayedCell.getAllSegments();

        for (int i = 0; i < segments.size(); i++)
        {
            Segment nextSegment = (Segment) segments.elementAt(i);
            Vector groups = nextSegment.getGroups();

            for (int j = 0; j < groups.size(); j++)
            {
                if (groups.elementAt(j).equals(group)) 
                    myOneCell3D.setSegmentAppearance(app, nextSegment.getSegmentId());
            }
        }
    }


    void highlightChanMechs(Component cause)
    {
        logger.logComment("Highlighting channel/density mechs");
        String selectedDensMechName = null;

        logger.logComment("++++++++++++++ Resetting...");
        resetColours();

        logger.logComment("++++++++++++++ Done resetting...");

        try
        {
            JRadioButton selectedRadioButton = (JRadioButton) cause;
            selectedDensMechName = selectedRadioButton.getText();
        }
        catch (ClassCastException ex)
        {
            // ignore, probably update due to change in combo box...
        }

        logger.logComment("The selected density mech is: " + selectedDensMechName);

        if (densMechRadioButtons.isEmpty())
        {
            ArrayList<String> allAllowedChanMechNames = this.displayedCell.getAllChanMechNames(true);

            for (int i = 0; i < allAllowedChanMechNames.size(); i++)
            {
                String nextChanMechName = allAllowedChanMechNames.get(i);

                JRadioButton next = createGroupRadioButton(nextChanMechName, densMechButtonGroup);

                if (selectedDensMechName==null) selectedDensMechName = nextChanMechName;

                densMechRadioButtons.put(next.getText(), next);
            }

            if (displayedCell.getAllApPropSpeeds().size()>0)
            {
                JRadioButton next
                    = createGroupRadioButton(ApPropSpeed.MECHANISM_NAME, densMechButtonGroup);

                densMechRadioButtons.put(next.getText(), next);
            }

        }
        Set<String> radioButtonNames = densMechRadioButtons.keySet();

        if (radioButtonNames.isEmpty())
        {
            jPanelColourControls.add(new JLabel("No membrane density mechanisms found in this cell", JLabel.CENTER));
        }

        for (String name: radioButtonNames)
        {
            JRadioButton nextRadioButton = densMechRadioButtons.get(name);
            jPanelColourControls.add(nextRadioButton);
            if (nextRadioButton.getText().equals(selectedDensMechName))
            {
                nextRadioButton.setSelected(true);
            }
            else
            {
                nextRadioButton.setSelected(false);
            }
        }
        jPanelColourControls.repaint();

        //ColourGenerator colourGen = new ColourGenerator();
        //Color c = colourGen.getNextColour();

        if (selectedDensMechName==null) return;

        //ChannelMechanism selChanMech = new ChannelMechanism(selectedChanMechName);

        Vector groups = this.displayedCell.getAllGroupNames();

        float maxVal = -1*Float.MAX_VALUE;
        float minVal = Float.MAX_VALUE;
        float noGmaxVal = -1;
        
        JLabel jLabelNoGmax = null;

        for (int k = 0; k < groups.size(); k++)
        {
            String group = (String)groups.elementAt(k);
            if (selectedDensMechName.equals(ApPropSpeed.MECHANISM_NAME))
            {
                ApPropSpeed groupChanMechs = this.displayedCell.getApPropSpeedForGroup(group);

                if (groupChanMechs!=null)
                {
                    if (maxVal < groupChanMechs.getSpeed()) maxVal = groupChanMechs.getSpeed();
                    if (minVal > groupChanMechs.getSpeed()) minVal = groupChanMechs.getSpeed();
                }
            }
            else
            {
                ArrayList<ChannelMechanism> groupChanMechs = this.displayedCell.getChanMechsForGroup(group);
                for (int j = 0; j < groupChanMechs.size(); j++)
                {
                    ChannelMechanism nextChanMech = groupChanMechs.get(j);

                    if (nextChanMech.getName().equals(selectedDensMechName))
                    {
                        if (nextChanMech.getDensity()!= noGmaxVal)
                        {
                            if (maxVal < nextChanMech.getDensity()) maxVal = nextChanMech.getDensity();
                            if (minVal > nextChanMech.getDensity()) minVal = nextChanMech.getDensity();
                        }
                    }
                }
            }
        }
        Enumeration<VariableMechanism> varMechs = this.displayedCell.getVarMechsVsParaGroups().keys();
        
        while (varMechs.hasMoreElements())
        {
            VariableMechanism vm = varMechs.nextElement();
            if(vm.getName().equals(selectedDensMechName))
            {
                ParameterisedGroup pg = this.displayedCell.getVarMechsVsParaGroups().get(vm);
                try
                {

                    double minPGVal = pg.getMinValue(displayedCell);
                    double maxPGVal = pg.getMaxValue(displayedCell);
                    
                    double discretisation = 0.1;
                    for(double i=0;i<=1;i=i+discretisation)
                    {
                        double midPGval = minPGVal + i*(maxPGVal -minPGVal);
                        float midMechVal = (float)vm.evaluateAt(midPGval); 
                        logger.logComment("i: "+i+", midPGval: "+midPGval+", midMechVal: "+midMechVal);
                        
                        if (maxVal < midMechVal) maxVal = midMechVal;
                        if (minVal > midMechVal) minVal = midMechVal;
                    }
                        
                }
                catch (ParameterException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ pg+" on cell: "+displayedCell, ex, myParent);
                    return;
                }
                catch (EquationException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ vm+" on cell: "+displayedCell, ex, myParent);
                    return;
                }
            }
        }
        logger.logComment("Max: "+ maxVal+", min: "+minVal);

        for (int k = 0; k < groups.size(); k++)
        {
            String group = (String)groups.elementAt(k);
            if (selectedDensMechName.equals(ApPropSpeed.MECHANISM_NAME))
            {
                ApPropSpeed groupAppv = this.displayedCell.getApPropSpeedForGroup(group);

                if (groupAppv!=null)
                {
                    float fraction = 1;

                    if (maxVal != minVal)
                    {
                        fraction = (groupAppv.getSpeed() - minVal) / (maxVal - minVal);
                    }
                    highlightSingleGroup(group, GeneralUtils.getFractionalColour(segmentHighlightSecondary,
                                                                                 segmentHighlightMain,
                                                                                 fraction));
                }
            }
            else
            {
                ArrayList<ChannelMechanism> groupChanMechs = this.displayedCell.getChanMechsForGroup(group);

                for (int j = 0; j < groupChanMechs.size(); j++)
                {
                    ChannelMechanism nextChanMech = groupChanMechs.get(j);

                    if (nextChanMech.getName().equals(selectedDensMechName))
                    {
                        float fraction = 1;
                        if (nextChanMech.getDensity()!= noGmaxVal)
                        {
                            if (maxVal != minVal)
                            {
                                fraction = (nextChanMech.getDensity() - minVal) / (maxVal - minVal);
                            }
                            highlightSingleGroup(group, GeneralUtils.getFractionalColour(segmentHighlightSecondary,
                                segmentHighlightMain, fraction));
                        }
                        else
                        {
                            highlightSingleGroup(group, segmentHighlightNoGmax);
                            jLabelNoGmax = new JLabel("No Gmax set");
                            jLabelNoGmax.setOpaque(true);
                            jLabelNoGmax.setBackground(segmentHighlightNoGmax);
                        }
                    }
                }
            }
        }
        
        varMechs = this.displayedCell.getVarMechsVsParaGroups().keys();
        
        while (varMechs.hasMoreElements())
        {
            VariableMechanism vm = varMechs.nextElement();
            if(vm.getName().equals(selectedDensMechName))
            {
                ParameterisedGroup pg = this.displayedCell.getVarMechsVsParaGroups().get(vm);
                try
                {
                    ArrayList<Segment> segs = displayedCell.getSegmentsInGroup(pg.getGroup());
                    for(Segment seg:  segs)
                    {
                        double pgVal = pg.evaluateAt(displayedCell, seg, 0.5f);
                        float vmVal = (float)vm.evaluateAt(pgVal);
                        float fraction = -1;
                        if (maxVal != minVal)
                        {
                            fraction = (vmVal - minVal) / (maxVal - minVal);
                        }
                        
                        Color c = GeneralUtils.getFractionalColour(segmentHighlightSecondary, segmentHighlightMain, fraction);
                        Appearance app = Utils3D.getGeneralObjectAppearance(c);

                        myOneCell3D.setSegmentAppearance(app, seg.getSegmentId());
                    }
                    //pg.
                }
                catch (ParameterException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ pg+" on cell: "+displayedCell, ex, myParent);
                    return;
                }
                catch (EquationException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ vm+" on cell: "+displayedCell, ex, myParent);
                    return;
                }
            }
        }
        
        
        
        JPanel maxMinPanel = new JPanel();

        if (maxVal != -1*Float.MAX_VALUE && minVal != Float.MAX_VALUE)
        {
            String prefix = "Gmax: ";
            String suffix = " "+UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();

            if (selectedDensMechName.equals(ApPropSpeed.MECHANISM_NAME))
            {
                prefix = "Velocity: ";
                suffix = " " + UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol() + " "
                    + UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
            }
            JLabel dens = new JLabel(prefix + maxVal + suffix);
            dens.setOpaque(true);
            dens.setBackground(segmentHighlightMain);

            maxMinPanel.add(dens);

            if (maxVal != minVal)
            {
                JLabel densMin = new JLabel(prefix + minVal + suffix);
                densMin.setOpaque(true);
                densMin.setBackground(segmentHighlightSecondary);
                maxMinPanel.add(densMin);

            }
        }
        if (jLabelNoGmax!=null)
        {
                maxMinPanel.add(jLabelNoGmax);
        }
        jPanelControls.setPreferredSize(new Dimension(400, 150));
        jPanelColourControls.setPreferredSize(new Dimension(400, 80));

        jPanelColourControls.add(maxMinPanel);

        JPanel chanMechInfoPanel = new JPanel();

        jPanelColourControls.add(chanMechInfoPanel);

        chanMechInfoPanel.add(this.sectionChanMechInfo);

    }



    public void markCellAsEdited()
    {
        project.markProjectAsEdited();
    }


    /**
     * Called by SectionPicker to inform the panel a primitive has been selected
     */
    @Override
    public void markPrimitiveAsSelected(Primitive selectedPrim)
    {
        logger.logComment("Marking prim as selected...");

        if (jComboBoxHighlight.getSelectedItem().equals(highlightSecSegs))
        {
            latestSelectedSegment = myOneCell3D.markPrimitiveAsSelected(selectedPrim,
                                                                        new Color(250, 80, 40));

            updateSegmentSummary();

            if (segmentSelector != null) segmentSelector.setSelectedSegment(latestSelectedSegment);

        }

        else if (jComboBoxHighlight.getSelectedItem().equals(highlightDensMechs))
        {
            logger.logComment("Creating chan dens list...");
            latestSelectedSegment = myOneCell3D.getSelectedPrimitive(selectedPrim);

            StringBuilder chanInfo = new StringBuilder("(Seg: "+ latestSelectedSegment.getSegmentName()
                                                     +", Sec: "+ latestSelectedSegment.getSection().getSectionName()+") ");

            ArrayList<ChannelMechanism> chans = this.displayedCell.getUniformChanMechsForSeg(latestSelectedSegment);

            for (int i = 0; i < chans.size(); i++)
            {
                ChannelMechanism nextChanMech = chans.get(i);

                chanInfo.append(nextChanMech.getName() + ": "+nextChanMech.getDensity());
                if (nextChanMech.getExtraParameters()!=null && nextChanMech.getExtraParameters().size()>0)
                {
                    chanInfo.append(" "+nextChanMech.getExtraParamsBracket());
                }
                if (i < chans.size()-1) chanInfo.append(", ");
            }
            ArrayList<VariableMechanism> vms = displayedCell.getVarChanMechsForSegment(latestSelectedSegment);
            
            for(VariableMechanism vm: vms)
            {
                ParameterisedGroup pg = displayedCell.getVarMechsVsParaGroups().get(vm);
                
                double pgVal;
                try
                {
                    pgVal = pg.evaluateAt(displayedCell, latestSelectedSegment, 0.5f);
                    float dens = (float)vm.evaluateAt(pgVal);
                    chanInfo.append(" "+vm.getName() + ": "+dens);


                    logger.logComment("Var mech ("+vm.getParam().getExpression()+") on seg: "+ latestSelectedSegment+" at dist "+pgVal+" as "+ dens);
                }
                catch (ParameterException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ pg+" on cell: "+displayedCell, ex, myParent);
                    return;
                }
                catch (EquationException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate values for: "+ vm+" on cell: "+displayedCell, ex, myParent);
                    return;
                }
                
            }

            ApPropSpeed appv = displayedCell.getApPropSpeedForSegment(latestSelectedSegment);

            if (appv!=null)
            {
                chanInfo.append(appv.toString()+ " ");

            }

            sectionChanMechInfo.setText(chanInfo.toString());
        }
        else {
            logger.logComment("Cell already coloured for: "
                              + jComboBoxHighlight.getSelectedItem()
                              + ", ignoring selection of section");
            return;
        }

    };


    /**
     * Marks the segment with the specified ID as selected, unless segmentID < 0
     * when all segments are deselected
     */
    public void markSegmentAsSelected(int segmentID, boolean refresh)
    {

        if (!jComboBoxHighlight.getSelectedItem().equals(highlightSecSegs))
        {
            logger.logComment("Cell already coloured for: "
                              + jComboBoxHighlight.getSelectedItem()
                              +", ignoring selection of section");
            return;
        }

        if (refresh) repaint3DScene();

        latestSelectedSegment = myOneCell3D.markSegmentAsSelected(segmentID,segmentHighlightMain);

        updateSegmentSummary();
    };

    private void removeAllActionListeners(AbstractButton button)
    {
        ActionListener[] als = button.getActionListeners();
        for (int i = 0; i < als.length; i++)
        {
            button.removeActionListener(als[i]);
        }
    }

    void updateSegmentSummary()
    {
        jPanelColourControls.removeAll();

        if (latestSelectedSegment !=null)
        {
            this.removeAllActionListeners(jButtonMoreInfo);

            jButtonMoreInfo.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jButtonSegInfo_actionPerformed(e);
                }
            });

            this.removeAllActionListeners(jButtonBiophysInfo);

            jButtonBiophysInfo.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (latestSelectedSegment == null) return;

                    boolean useHtml = true;

                    String summary = CellTopologyHelper.getSegmentBiophysics(latestSelectedSegment,
                                                                             displayedCell,
                                                                             project,
                                                                             useHtml);

                    SimpleViewer.showString(summary,
                                            "Biophysics of segment: " + latestSelectedSegment.getSegmentName() + " in cell: " +
                                            displayedCell.getInstanceName(), 10, false, useHtml,
                                            .6f);

                }
            });



            String parentString = "- None -";
            if (latestSelectedSegment.getParentSegment()!=null)
                parentString = latestSelectedSegment.getParentSegment().getSegmentName();

            String divs = "";
            if (latestSelectedSegment.getSection().getNumberInternalDivisions()!=1)
            {
                divs = " (divs: "+latestSelectedSegment.getSection().getNumberInternalDivisions()+")";
            }


            JLabel info1 = new JLabel(latestSelectedSegment.getSegmentName()
                                      + " (ID: "+ latestSelectedSegment.getSegmentId()+", Sec: "+ latestSelectedSegment.getSection().getSectionName()
                                      + divs + ", Par: "+ parentString + ")");
            JLabel info2 = new JLabel(latestSelectedSegment.getStartPointPosition()
                                      + " R: " + latestSelectedSegment.getSegmentStartRadius()
                                      + " -> "+latestSelectedSegment.getEndPointPosition()
                                      + " R: " + latestSelectedSegment.getRadius()
                                      + ", L: " + latestSelectedSegment.getSegmentLength());


            jPanelColourControls.add(jButtonMoreInfo);
            jPanelColourControls.add(jButtonBiophysInfo);
            jPanelColourControls.add(info1);
            jPanelColourControls.add(info2);
        }
        else
        {
            logger.logComment("Null selected");
        }

        jPanelColourControls.repaint();
        jPanelColourControls.validate();

    }



    void highlightSectionTypes()
    {
        logger.logComment("++++++++++++++ Resetting...");
        resetColours();

        logger.logComment("++++++++++++++ Done resetting...");

        if (sectionTypeButtons.isEmpty())
        {
            ColourGenerator colourGen = new ColourGenerator();
            JButton dendButton = createColouredButton("Dendrites", "Dendritic sections", colourGen.getNextColour());
            JButton axonButton = createColouredButton("Axons", "Axonal section", colourGen.getNextColour());
            JButton dendFinVolButton = createColouredButton("Dendrites with finite volume", "Dendritic sections whose volume will be taken into account when packing", colourGen.getNextColour());
            JButton axonFinVolButton = createColouredButton("Axons with finite volume", "Axonal section whose volume will be taken into account when packing", colourGen.getNextColour());

            sectionTypeButtons.put(dendFinVolButton.getText(), dendFinVolButton);
            sectionTypeButtons.put(axonFinVolButton.getText(), axonFinVolButton);

            sectionTypeButtons.put(dendButton.getText(), dendButton);
            sectionTypeButtons.put(axonButton.getText(), axonButton);
        }

        Set<String> buttonNames = sectionTypeButtons.keySet();
        for (String buttonName: buttonNames)
        {
            jPanelColourControls.add(sectionTypeButtons.get(buttonName));
        }
        jPanelColourControls.repaint();

        Vector sections = displayedCell.getAllSegments();

        Color dendColour  = (sectionTypeButtons.get("Dendrites")).getBackground();
        Color axonColour  = (sectionTypeButtons.get("Axons")).getBackground();

        Color dendFinVolColour  = (sectionTypeButtons.get("Dendrites with finite volume")).getBackground();
        Color axonFinVolColour  = (sectionTypeButtons.get("Axons with finite volume")).getBackground();

        for (int i = 0; i < sections.size(); i++)
        {
            Segment seg = (Segment)sections.elementAt(i);

            if (seg.isDendriticSegment())
            {
                if ( seg.isFiniteVolume())
                {
                    myOneCell3D.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(dendFinVolColour), seg.getSegmentId());
                }
                else
                {
                    myOneCell3D.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(dendColour), seg.getSegmentId());
                }
            }
            if(seg.isAxonalSegment())
            {
                if (seg.isFiniteVolume())
                {
                    myOneCell3D.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(axonFinVolColour), seg.getSegmentId());
                }
                else
                {
                    myOneCell3D.setSegmentAppearance(Utils3D.getGeneralObjectAppearance(axonColour), seg.getSegmentId());
                }
            }
        }

    }


    JButton createColouredButton(String name, String tip, Color color)
    {
        JButton newButton = new JButton(name);
        newButton.setToolTipText(tip);
        newButton.setBackground(color);

        newButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //final JButton newButton = null;
                JButton caller = (JButton)e.getSource();
                Color c = JColorChooser.showDialog(null, "Please choose a colour for the new Cell Group", caller.getBackground());
                if (c!=null) caller.setBackground(c);
                updateColouringAndExtraButtons((Component)e.getSource());
            }
        });
        jPanelColourControls.validate();
        return newButton;
    }


    JRadioButton createGroupRadioButton(String name, ButtonGroup group)
    {
        JRadioButton newRadioButton = new JRadioButton(name);
        //newRadioButton.setg
        group.add(newRadioButton);

        newRadioButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateColouringAndExtraButtons((Component)e.getSource());
            }
        });
        jPanelColourControls.validate();
        return newRadioButton;
    }



    void jComboBoxHighlight_itemStateChanged(ItemEvent e)
    {
        logger.logComment("Item changed: "+e);

        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            if (!jComboBoxHighlight.getSelectedItem().equals(highlightSecSegs))
            {
                if (segmentSelector!=null) segmentSelector.dispose();
                segmentSelector = null;
            }

            updateColouringAndExtraButtons(jComboBoxHighlight);
        }
    }


    void updateColouringAndExtraButtons(Component cause)
    {
        String selString = (String) jComboBoxHighlight.getSelectedItem();
        jPanelExtraButton.removeAll();


        if (selString.equals(highlightSecSegs))
        {
            jComboBoxHighlight.setToolTipText(toolTipText.getToolTip("Highlight pick section/segment"));
            jPanelExtraButton.add(jButtonCellInfo);
            jPanelExtraButton.add(this.warningLabel1);
            jPanelExtraButton.add(this.warningLabel2);
            resetColours();

            if (project.proj3Dproperties.getMinRadius()>0)
            {
                warningLabel2.setForeground(ValidityStatus.VALIDATION_COLOUR_WARN_OBJ);
                warningLabel2.setText("Min radius: "+project.proj3Dproperties.getMinRadius());
            }
            else
            {
                warningLabel2.setText("");
            }

            this.myOneCell3D.applyGroupColouring();

        }
        else if (selString.equals(highlightDensMechs))
        {
            jComboBoxHighlight.setToolTipText(toolTipText.getToolTip("Highlight channels"));
            jPanelExtraButton.add(jButtonEditChanMechs, null);
            highlightChanMechs(cause);
        }
        else if (selString.equals(highlightSynapseLocations))
        {
            jComboBoxHighlight.setToolTipText(toolTipText.getToolTip("Highlight syn conn location"));
            jPanelExtraButton.add(jButtonEditSynapses, null);
            highlightSynLocs(cause);
        }

        else if (selString.equals(highlightGroups))
        {
            jComboBoxHighlight.setToolTipText(toolTipText.getToolTip("Highlight groups"));
            jPanelExtraButton.add(jButtonEditGroups, null);
            highlightGroups(cause);
        }

        else if (selString.equals(highlightParamGroups))
        {
            jComboBoxHighlight.setToolTipText(toolTipText.getToolTip("Highlight param groups"));
            //jPanelExtraButton.add(jButtonEditParamGroups, null);
            highlightParamGroups(cause);
        }


        else if (selString.equals(highlightSectionTypes))
        {
            jComboBoxHighlight.setToolTipText(toolTipText.getToolTip("Highlight section types"));
            highlightSectionTypes();
        }
        //jpa
        logger.logComment("Repainting the panel...");
        jPanelControls.repaint();
        jPanelControls.validate();
    }


    private class ColourGenerator
    {
        int colourCount = 0;
        Random rand = new Random();

        Color defaultFirstColour = Color.red;
        Color defaultSecondColour = Color.blue;
        Color defaultThirdColour = Color.green;

        public Color getNextColour()
        {
            Color nextColour = null;
            if (colourCount==0) nextColour = defaultFirstColour;
            else if (colourCount ==1) nextColour = defaultSecondColour;
            else if (colourCount ==2) nextColour = defaultThirdColour;
            else nextColour = new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());

            colourCount ++;
            return nextColour;
        }

        public void reset()
        {
            colourCount = 0;
        }
    }

    void jButtonEditGroups_actionPerformed(ActionEvent e)
    {
        EditGroupsDialog dlg = new EditGroupsDialog(displayedCell, myParent, "Edit groups", this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        
        Set<String> radButtonNames = groupRadioButtons.keySet();
        
        String selected = null;
        for(String name: radButtonNames)
        {
            JRadioButton nextRadioButton = groupRadioButtons.get(name);
            if (nextRadioButton.isSelected())
                selected = name;
        }
        logger.logComment("selected: "+ selected);
        
        if (!selected.equals(Section.ALL))
        {
            dlg.setSelectedGroup(selected);
        }
        
        dlg.setVisible(true);
        

        if (dlg.cancelled)
        {
            logger.logComment("Cancel pressed...");
            return;
        }

        project.markProjectAsEdited();

        groupRadioButtons.clear(); // in case there's new groups...

        jComboBoxHighlight.setSelectedItem(highlightSecSegs);
        jComboBoxHighlight.setSelectedItem(highlightGroups);
    }

    void jButtonEditSynapses_actionPerformed(ActionEvent e)
    {
        Vector<String> synapticTypes =  project.cellMechanismInfo.getAllChemElecSynMechNames();

        EditGroupSynapseAssociations dlg = new EditGroupSynapseAssociations(displayedCell, myParent, "Synaptic process", synapticTypes);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("Cancel pressed...");
            return;
        }

        project.markProjectAsEdited();

        synLocRadioButtons.clear(); // in case there's new groups...

        jComboBoxHighlight.setSelectedItem(highlightSecSegs);
        jComboBoxHighlight.setSelectedItem(highlightSynapseLocations);

    }


    void jButtonEditChanMechs_actionPerformed(ActionEvent e)
    {
        String selected = null;

        Collection<JRadioButton> rButtons = this.densMechRadioButtons.values();

        for (JRadioButton rb: rButtons)
        {
            if (rb.isSelected()) selected = rb.getText();
        }

        EditGroupCellDensMechAssociations dlg = new EditGroupCellDensMechAssociations(displayedCell, myParent,
                                                                              "Cell density mechanism", project);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                         (frmSize.height - dlgSize.height) / 2 + loc.y);

        dlg.setSelected(selected);

        dlg.setModal(true);
        dlg.setVisible(true);

        if (dlg.cancelled)
        {
            logger.logComment("Cancel pressed...");
            return;
        }

        project.markProjectAsEdited();

        densMechRadioButtons.clear(); // in case there's new groups...

        jComboBoxHighlight.setSelectedItem(highlightSecSegs);
        jComboBoxHighlight.setSelectedItem(highlightDensMechs);

    }


    void jButtonCellInfo_actionPerformed(ActionEvent e)
    {
        Cell cell = myOneCell3D.getDisplayedCell();

        String extraWarning = "";

        Object selOpt = jComboBoxView.getSelectedItem();


        boolean html = true;

        if (cell.getAllSegments().size()>500) html = false;


        if (selOpt instanceof MorphCompartmentalisation)
        {
            MorphCompartmentalisation mc = (MorphCompartmentalisation)selOpt;
            if (html)
            {
                extraWarning = "<h3><font color=\"red\">  Note: This is information on the displayed compartmentalisation: " + mc.getName() +
                    "</font></h3>"
                    + "<p><font color=\"red\">" + mc.getDescription() + "</font></p><br>\n";
            }
            else
            {
                extraWarning = "Note: This is information on the displayed compartmentalisation: " + mc.getName() +
                    "\n" + mc.getDescription() + "\n\n";

            }
        }


        SimpleViewer simpleViewer
            = new SimpleViewer(extraWarning + CellTopologyHelper.printDetails(cell, this.project, html)
                               + "\n" + extraWarning,
                               "Cell Info for: " + cell.toString(),
                               11,
                               false,
                               html);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        simpleViewer.setFrameSize( (int) (screenSize.getWidth() * 0.9d), (int) (screenSize.getHeight() * 0.9d));

        Dimension frameSize = simpleViewer.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        simpleViewer.setVisible(true);
    }



    public void tempShowSection(String sectionName, String groupName)
    {
        jComboBoxHighlight.setSelectedItem(highlightSecSegs);

        this.myOneCell3D.markSectionAsSelected(sectionName, Color.yellow);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            ///
        }
        refreshGroup(groupName);

    }


    public void refreshGroup(String groupName)
    {
        logger.logComment("Refreshing...");

        // to populate groupRadioButtons
        jComboBoxHighlight.setSelectedItem(highlightSecSegs);
        jComboBoxHighlight.setSelectedItem(highlightGroups);

        Set<String> radioButtonNames = groupRadioButtons.keySet();

        for (String name: radioButtonNames)
        {
            JRadioButton nextRadioButton = groupRadioButtons.get(name);

            if (nextRadioButton.getText().equals(groupName))
            {
                nextRadioButton.setSelected(true);
                highlightGroups(nextRadioButton);
            }
        }

    };

    @Override
    public void refresh3D()
    {
        /** @todo ... (Needed?) */
    }


    @Override
    public void setLastViewingTransform3D(Transform3D lastViewingTransform3D)
    {
        if (lastViewingTransform3D != null)
        {
            MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();
            logger.logComment("There are "+mtg.getNumTransforms()+" TransformGroups in ViewingPlatform");
            TransformGroup lastTG = mtg.getTransformGroup(mtg.getNumTransforms() - 1);

            lastTG.setTransform(lastViewingTransform3D);
        }

    }


    public void setHighlighted(String choice)
    {
        this.jComboBoxHighlight.setSelectedItem(choice);
    }


    @Override
    public Transform3D getLastViewingTransform3D()
    {
        Transform3D t3d = new Transform3D();
        if (simpleU!=null)
        {
            MultiTransformGroup mtg = simpleU.getViewingPlatform().getMultiTransformGroup();
            mtg.getTransformGroup(mtg.getNumTransforms() - 1).getTransform(t3d);
            return new Transform3D(t3d);
        }
        else return null;

    }


    protected void detach()
    {
        Frame frame = new Frame("3D visualisation");

        frame.addWindowListener(new AppCloser(frame, false));

        //if (canvas3D!=null) frame.add("Center", canvas3D);

        this.remove(canvas3D);
        containerFor3D = frame;

        GuiUtils.centreWindow(frame);

        frame.setVisible(true);

        frame.setSize(2000,2000);
        frame.setLocation(0,0);

        this.repaint3DScene();

    }


    /**
     * Function only for debugging, for quickly visualising cells from other classes
     */
    protected static void testCell(Cell cell)
    {
        try
        {
            Frame frame = new Frame();

            frame.setLayout(new BorderLayout());
            System.out.println("Creating cell");

            GeneralProperties.setDefault3DAxesOption(true);

            System.out.println("Creating 3D representation of cell: " + cell);


            Project testProj = Project.loadProject(new File("nCexamples/Ex6_CerebellumDemo/Ex6_CerebellumDemo.ncx"),
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

            testProj.proj3Dproperties.setDisplayOption(Display3DProperties.DISPLAY_SOMA_NEURITE_SOLID);
            testProj.proj3Dproperties.setShow3DAxes(true);

            if (cell == null) cell = testProj.cellManager.getAllCells().firstElement();

            OneCell3DPanel viewer = new OneCell3DPanel(cell, testProj, null);

            frame.add("Center", viewer);



            frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    System.exit(0);
                }
            }
            );

            frame.pack();
            frame.setSize(1000, 800);
            frame.setVisible(true);

            //System.out.println("Cell details: " + CellTopologyHelper.printDetails(cell));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }



    void jButtonSegInfo_actionPerformed(ActionEvent e)
    {
        if (segmentSelector!=null) segmentSelector.dispose();

        segmentSelector = new SegmentSelector(new Frame(), project,
                                              displayedCell, true);

        segmentSelector.set3DDisplay(this);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = segmentSelector.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        segmentSelector.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        if (latestSelectedSegment==null)
        {
            markSegmentAsSelected(0, false);
        }

        segmentSelector.setSelectedSegment(latestSelectedSegment);

        segmentSelector.setVisible(true);
        segmentSelector.toFront();
    }



    public static void main(String[] args)
    {
        GeneralUtils.timeCheck("Starting application...");

        //PurkinjeCell pCell = new PurkinjeCell("PurkinjeCell");
        //Cell cell = new SimpleCell("SimpleCell");
        Cell cell = new SimpleCell("CompCell");

        Section axSec = cell.getAllSections().get(1);

        File jxmlFile = new File("../temp/final.java.xml");

        //MorphMLConverter mmlc = new MorphMLConverter();

        try
        {
            boolean useLargeCell = false;

            if (useLargeCell)
            {
                cell = MorphMLConverter.loadFromJavaXMLFile(jxmlFile);
            }
            else
            {

                cell.getAllSections().get(4).setNumberInternalDivisions(6);
                /*
                axSec.setStartPointPositionY(15);
                ArrayList<Section> secs = cell.getAllSections();

                secs.get(secs.size()-1);*/

                cell = CellTopologyHelper.translateAllPositions(cell, new Vector3f(0,00,0));



               // cell.addDendriticSegment(lastSec.getStartRadius(), "new", );

            }

            OneCell3DPanel.testCell(cell);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void updateButtonEnabling()
    {
        Object selOpt = jComboBoxView.getSelectedItem();

        if (selOpt instanceof String)
        {
            this.jButtonMoreInfo.setEnabled(true);
            this.jButtonBiophysInfo.setEnabled(true);
            jButtonSomaInfo.setEnabled(true);
            this.jComboBoxHighlight.setEnabled(true);

        }
        else if (selOpt instanceof MorphCompartmentalisation)
        {
            this.jButtonMoreInfo.setEnabled(false);
           ////// this.jButtonBiophysInfo.setEnabled(false);
            jButtonSomaInfo.setEnabled(false);
            this.jComboBoxHighlight.setEnabled(false);

        }

    }

    void jComboBoxToggleView_itemStateChanged(ItemEvent e)
    {
        //System.out.println("jComboBoxToggleView_itemStateChanged: "+e);

        this.updateButtonEnabling();

        if (!initialising && e.getStateChange() == ItemEvent.SELECTED)
        {
            Object selOpt = jComboBoxView.getSelectedItem();

            String selected = null;
            if (selOpt instanceof String)
            {
                selected = (String) selOpt;

            }
            else if (selOpt instanceof MorphCompartmentalisation)
            {
                selected = ( (MorphCompartmentalisation) selOpt).getName();
            }

                 project.proj3Dproperties.setDisplayOption(selected);

            logger.logComment("Changed to: "+ selected);


            // reset the highlight option:
            jComboBoxHighlight.setSelectedIndex(jComboBoxHighlight.getSelectedIndex());

            repaint3DScene();

            if (latestSelectedSegment != null)
                markSegmentAsSelected(latestSelectedSegment.getSegmentId(), true);

            //

        }
    }

    void jButtonFind_actionPerformed(ActionEvent e)
    {
        findCell();
    }

    void jButtonDetach_actionPerformed(ActionEvent e)
    {
        this.detach();
    }


    final void findCell()
    {
        float minX = 10000;
        float minY = 10000;
        float maxX = -10000;
        float maxY = -10000;
        float maxZ = -10000;

        if (project.proj3Dproperties.getShow3DAxes())
        {
            minX = -100;
            minY = -100;
            maxX = 100;
            maxY = 100;
            maxZ = 100;
        }
        float lowestXToShow = Math.min(minX, CellTopologyHelper.getMinXExtent(this.displayedCell, false, true));
        float highestXToShow = Math.max(maxX, CellTopologyHelper.getMaxXExtent(this.displayedCell, false, true));

        float lowestYToShow = Math.min(minY, CellTopologyHelper.getMinYExtent(this.displayedCell, false, true));
        float highestYToShow = Math.max(maxY, CellTopologyHelper.getMaxYExtent(this.displayedCell, false, true));

        //float lowestZToShow = Math.min(0, CellTopologyHelper.getMinZExtent(this.displayedCell, false, true));
        float highestZToShow = Math.max(maxZ, CellTopologyHelper.getMaxZExtent(this.displayedCell, false, true));

        if (highestZToShow <100) highestZToShow = 100;


        logger.logComment("To show x: "+lowestXToShow+"->"+highestXToShow+", y: "+lowestYToShow+"->"+highestYToShow+", z: ??->"+highestZToShow+"", true);

        float xCoordAfterScale = (highestXToShow+lowestXToShow)/2f;
        float yCoordAfterScale = (highestYToShow+lowestYToShow)/2f;

        float largestXYExtent =  Math.max(20, Math.max(highestXToShow-lowestXToShow,
                                           highestYToShow-lowestYToShow));

        //System.out.println("largestXYExtent: "+ largestXYExtent+", highestZToShow: "+ highestZToShow);

        float zCoordAfterScale = (largestXYExtent * 2.2f) + highestZToShow;

        Transform3D t3d = new Transform3D();
        Vector3d viewPoint = new Vector3d(xCoordAfterScale,
                                          yCoordAfterScale,
                                          zCoordAfterScale);

        logger.logComment("viewPoint from cell perspective: "+ viewPoint, true);

        float extra = 1;
        if (largestXYExtent>2000)
        {
            //viewPoint.z = viewPoint.z +1000;
            extra = 0.4f;
        }
        viewPoint.scale(optimalScale*extra);

        logger.logComment("viewPoint scaled: "+ viewPoint);

        t3d.set(viewPoint);
        setLastViewingTransform3D(t3d);

    }


}


