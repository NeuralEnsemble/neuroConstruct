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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.util.*;

import javax.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.Transform3D;
import javax.swing.*;

import javax.swing.event.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog for selecting and viewing properties of segments, as viewed in a OneCell3DPanel
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SegmentSelector extends JFrame implements DocumentListener
{
    ClassLogger logger = new ClassLogger("SegmentSelector");
    Project project = null;

    boolean editable = false;

    boolean cancelled = false;
    
    
    Color okColourBG = Color.white;
    Color okColourFG = Color.black;
    Color warningColour = Color.red;
    Color alteredColour = Color.yellow;

    //when the 3D view is updated and this must respond to changes there
    boolean updatingFrom3DView = false;

    boolean initialising = false;
    
    SimpleViewer endPointViewer = null;
    
    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    Cell myCell = null;
    String mainLabel = "Selection of segments in cell type: ";
    String defaultSectionItem = "-- Please select the section --";
    String defaultSegmentItem = "-- Please select the segment --";

    String sectionDetails = "Section details";

    String finiteVolYes = "Yes";
    String finiteVolNo  = "No";

    String cylindricalShape = "Cylidrical shape";
    String sphericalShape   = "Spherical shape";

    OneCell3DPanel oneCell3DPanel = null;

    Vector<Segment> segmentsForDeletion = new Vector<Segment>();

    static final int SECTIONS_SHOWN = 0;
    static final int SEGMENTS_SHOWN = 1;

    int shownFields = SEGMENTS_SHOWN;

    private static final String FUNCTION_INSTR = "--  Select one of these functions:  --";
    private static final String ADD_SEG_IN_NEW_SEC = "Add segment in new section";
    private static final String ADD_SEG_IN_THIS_SEC = "Add segment in this section";
    private static final String SPLIT_SEC = "Split section here";
    private static final String COMMENT = "Edit Segment comment";
    private static final String SPECIFY_AXONAL_ARBOUR = "Specify axonal arbour";
    private static final String REMESH_CELL =   "Remesh - correct num int divs/nseg";

    Segment currentlyAddressedSegment = null;

    JPanel panelButtons = new JPanel();
    JPanel jPanelParameters = new JPanel();
    JButton jButtonOK = new JButton("OK");
    BorderLayout borderLayout2 = new BorderLayout();
    JButton jButtonCancel = new JButton("Cancel");
    JPanel jPanelinfo = new JPanel();
    JLabel jLabelName = new JLabel();
    JTextField jTextFieldSegmentsName = new JTextField();
    JLabel jLabelSection = new JLabel();
    JTextField jTextFieldID = new JTextField();
    JComboBox jComboBoxSection = new JComboBox();
    JComboBox jComboBoxSegment = new JComboBox();
    JLabel jLabelMain = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabelID = new JLabel();
    JTextField jTextFieldNameSecInSegView = new JTextField();
    JLabel jLabelEndPoint = new JLabel();
    JTextField jTextFieldStartPoint = new JTextField();
    JLabel jLabelStartPoint = new JLabel();
    JLabel jLabelParent = new JLabel();
    
    
    JButton jButtonCoords = new JButton("...");
    JButton jButtonJump = new JButton(">");
    
    
    JLabel jLabelSegLength = new JLabel();
    JTextField jTextFieldSegLength = new JTextField();
    
    JTextField jTextFieldEndPoint = new JTextField();
    JTextField jTextFieldParent = new JTextField();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabelFractAlong = new JLabel();
    JLabel jLabelStartRadius = new JLabel();
    JTextField jTextFieldStartRadius = new JTextField();
    JTextField jTextFieldFractAlong = new JTextField();
    JLabel jLabelEndRadius = new JLabel();
    JTextField jTextFieldEndRadius = new JTextField();
    ///////////JLabel jLabelShape = new JLabel();
    JLabel jLabelFiniteVol = new JLabel();
    JComboBox jComboBoxFiniteVolume = new JComboBox();
    //JComboBox jComboBoxShape = new JComboBox();
    JButton jButtonUpdate = new JButton("Update");
    JButton jButtonDelete = new JButton("Delete");
    JPanel jPanelOptions = new JPanel();
    JPanel jPanelMainOptions = new JPanel();
    JPanel jPanelUpdateOptions = new JPanel();
    JPanel jPanelOkCancel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonCheckValid = new JButton("Cell Validity");
    JPanel jPanelFunctions = new JPanel();
    JLabel jLabelSectionName = new JLabel();
    JTextField jTextFieldSectionsName = new JTextField();
    JLabel jLabelNumberIntDivisions = new JLabel();
    JTextField jTextFieldNumIntDivisions = new JTextField();
    JLabel jLabelStartSecPoint = new JLabel();
    JTextField jTextFieldStartSecPOoint = new JTextField();
    JLabel jLabelStartSecRadius = new JLabel();
    JTextField jTextFieldStartSecRadius = new JTextField();
    JButton jButtonBiophys = new JButton("Biophys");
    JComboBox jComboBoxFunctions = new JComboBox();
    
    JLabel jLabelSegChildren = new JLabel();
    JRadioButton jRadioButtonNoMove = new JRadioButton();
    JRadioButton jRadioButtonTransChildren = new JRadioButton();
    JRadioButton jRadioButtonRotTransChildren = new JRadioButton();
    
    ButtonGroup buttonGrpChildern = new ButtonGroup();
    

    
    public SegmentSelector(Frame frame,
                           Project project,
                           Cell cell,
                           boolean editable)
    {
        super("Segment Selector");


        myCell = cell;
        this.editable = editable;
        this.project = project;

        try
        {
            jbInit();
            extraInit();
            pack();
            refresh();
            registerPointFieldChange();
        }
        catch(Exception ex)
        {
            logger.logError("Exception starting GUI", ex);
        }

    }

    public void set3DDisplay(OneCell3DPanel oneCell3DPanel)
    {
        this.oneCell3DPanel = oneCell3DPanel;
    }

    public void setSelectedSegment(Segment segment)
    {
      
        updatingFrom3DView = true;
        
        logger.logComment("----   Setting selected segment: "+segment);
        if (!myCell.getAllSegments().contains(segment)) return;

        jComboBoxSection.setSelectedItem(segment.getSection().getSectionName());
        jComboBoxSegment.setSelectedItem(segment.getSegmentName());

        updatingFrom3DView = false;

    }

    private void refresh()
    {
        boolean segmentAddressed = true;
        
        if ( endPointViewer!=null)
        {
             endPointViewer.dispose();
              endPointViewer  = null;
        }

        if (currentlyAddressedSegment==null)
            segmentAddressed = false;

        boolean fieldsEditable = editable && segmentAddressed;

        jTextFieldSegmentsName.setEditable(fieldsEditable);
        jTextFieldEndPoint.setEditable(fieldsEditable);
        jTextFieldStartRadius.setEditable(fieldsEditable);
        jTextFieldEndRadius.setEditable(fieldsEditable);
        jTextFieldFractAlong.setEditable(fieldsEditable);
        jComboBoxFiniteVolume.setEnabled(fieldsEditable);

        // Note: only first soma segment can be spherical, so don't allow
        // any other segment shape to be changed.

        boolean isFirstSegment = false;
        if (currentlyAddressedSegment!=null)
            isFirstSegment = (currentlyAddressedSegment.getSegmentId()==0);

        boolean isFirstSectionSegment = false;
        
        if (currentlyAddressedSegment!=null)
            isFirstSectionSegment = currentlyAddressedSegment.isFirstSectionSegment();

        logger.logComment("isFirstSegment: "+isFirstSegment);
        logger.logComment("isFirstSectionSegment: "+isFirstSectionSegment);


        this.jComboBoxFunctions.setEnabled(this.currentlyAddressedSegment!=null);

        jTextFieldStartPoint.setEditable(fieldsEditable);


    }

    private void replaceSegmentSectionInfo()
    {
        Section selSection = getSelectedSection();

        if(currentlyAddressedSegment==null && jComboBoxSegment.getSelectedItem().equals(defaultSegmentItem))
        {
            logger.logComment("Removing all text field info");
            jTextFieldSegmentsName.setText("");
            jTextFieldID.setText("");
            jTextFieldNameSecInSegView.setText("");
            jTextFieldEndPoint.setText("");
            jTextFieldStartPoint.setText("");

            jTextFieldEndRadius.setText("");
            jTextFieldStartRadius.setText("");

            jTextFieldParent.setText("");
            jTextFieldFractAlong.setText("");

            jTextFieldSectionsName.setText("");
            jTextFieldNumIntDivisions.setText("");
            jTextFieldStartSecPOoint.setText("");
            jTextFieldStartSecRadius.setText("");

        }
        else if (currentlyAddressedSegment!=null)
        {
            logger.logComment("Replacing segment text field info");

            jTextFieldSegmentsName.setText(currentlyAddressedSegment.getSegmentName());
            jTextFieldID.setText(currentlyAddressedSegment.getSegmentId()+"");
            jTextFieldNameSecInSegView.setText(currentlyAddressedSegment.getSection().getSectionName());
            jTextFieldEndPoint.setText(currentlyAddressedSegment.getEndPointPosition().toString());
            jTextFieldStartPoint.setText(currentlyAddressedSegment.getStartPointPosition().toString());

            jTextFieldEndRadius.setText(currentlyAddressedSegment.getRadius()+"");
            jTextFieldStartRadius.setText(currentlyAddressedSegment.getSegmentStartRadius()+"");
            jTextFieldFractAlong.setText(currentlyAddressedSegment.getFractionAlongParent()+"");
            
            jTextFieldSegLength.setText(currentlyAddressedSegment.getSegmentLength()+"");

            if (currentlyAddressedSegment.isFiniteVolume())
               jComboBoxFiniteVolume.setSelectedItem(finiteVolYes);
            else
                jComboBoxFiniteVolume.setSelectedItem(finiteVolNo);


            if (currentlyAddressedSegment.getParentSegment()==null)
            {
                jTextFieldParent.setText("-- No parent segment --");
            }
            else
                jTextFieldParent.setText(currentlyAddressedSegment.getParentSegment().getSegmentName()
                                         + "/"
                                         + currentlyAddressedSegment.getParentSegment().getSection().getSectionName());


        }
        else if (selSection!=null)
        {
            logger.logComment("Replacing section text field info");

            jTextFieldSectionsName.setText(selSection.getSectionName());
            jTextFieldNumIntDivisions.setText(selSection.getNumberInternalDivisions()+"");
            jTextFieldStartSecPOoint.setText(selSection.getStartPointPosition()+"");
            jTextFieldStartSecRadius.setText(selSection.getStartRadius()+"");

            this.validate();

        }
        refresh();

    }


    private void jbInit() throws Exception
    {
        /*
        Dimension dim = new Dimension(480,700);
        this.setPreferredSize(dim);
        this.setMinimumSize(dim);*/

        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        panelButtons.setLayout(borderLayout1);
        this.getContentPane().setLayout(borderLayout2);
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        jLabelName.setText("Segment name:");
        jTextFieldSegmentsName.setText("...");
        jTextFieldSegmentsName.setColumns(6);

        jLabelSection.setText("Section name:");
        jTextFieldID.setText("...");
        jTextFieldID.setColumns(6);
        jLabelMain.setText(mainLabel);
        jPanelinfo.setLayout(gridBagLayout1);

        jComboBoxSection.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxSection_itemStateChanged(e);
            }
        });
        jComboBoxSegment.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxSegment_itemStateChanged(e);
            }
        });

        jLabelID.setText("Segment ID:");
        jTextFieldNameSecInSegView.setText("...");
        jLabelEndPoint.setText("End point:");
        jTextFieldStartPoint.setText("...");
        jLabelStartPoint.setText("Start point:");
        jLabelParent.setText("Parent segment/section:");
        
        jLabelSegLength.setText("Segment length:");
        jTextFieldSegLength.setText("...");
        jTextFieldSegLength.setEditable(false);
        
        
        jTextFieldEndPoint.setText("...");
        jTextFieldParent.setText("...");

        jTextFieldID.setEditable(false);
        jTextFieldParent.setEditable(false);
        jTextFieldNameSecInSegView.setEditable(false);
        jTextFieldStartPoint.setEditable(false);

        jLabelFractAlong.setVerifyInputWhenFocusTarget(true);
        jLabelFractAlong.setText("Fraction along parent seg:  ");
        jLabelStartRadius.setText("Start radius:");
        jTextFieldStartRadius.setText("...");
        jTextFieldFractAlong.setText("...");
        jLabelEndRadius.setText("End radius:");
        jTextFieldEndRadius.setText("...");
        //////////jLabelShape.setText("Shape:");
        jLabelFiniteVol.setText("Finite volume:");
        jButtonUpdate.setText("Update");
        jButtonUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonUpdate_actionPerformed(e);
            }
        });
        jButtonDelete.setText("Delete segment");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelete_actionPerformed(e);
            }
        }); 
        
        jButtonCheckValid.setToolTipText("");
        jButtonCheckValid.setText("Cell validity");
        jButtonCheckValid.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCheckValid_actionPerformed(e);
            }
        }); 
        
        jLabelSectionName.setText("Section name:");
        jTextFieldSectionsName.setEditable(false);
        jTextFieldSectionsName.setText("");
        jTextFieldSectionsName.setColumns(12);
        jLabelNumberIntDivisions.setText("Num internal divs:");
        jTextFieldNumIntDivisions.setText("");
        jTextFieldNumIntDivisions.setColumns(12);
        jLabelStartSecPoint.setText("Start point:");
        jTextFieldStartSecPOoint.setEditable(false);
        jTextFieldStartSecPOoint.setText("");
        jTextFieldStartSecPOoint.setColumns(12);

        jLabelStartSecRadius.setRequestFocusEnabled(true);
        jLabelStartSecRadius.setText("Start radius:");
        jTextFieldStartSecRadius.setEditable(false);
        jTextFieldStartSecRadius.setText("");
        jTextFieldStartSecRadius.setColumns(12); 
        
        jButtonBiophys.setText("Biophys"); 
        jButtonBiophys.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBiophys_actionPerformed(e);
            }
        }); 
        Dimension d = new Dimension(24, 24);
        
        jButtonCoords.setPreferredSize(d);
        jButtonCoords.setMaximumSize(d);
        jButtonCoords.setMargin(new Insets(2, 2,2,2));
        jButtonCoords.setToolTipText("This offers a number of suggestions for end points at a given length when the start x, y, z are integer values");
        
        
        jButtonCoords.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCoords_actionPerformed(e);
            }
        }); 
        
        jButtonJump.setPreferredSize(d);
        jButtonJump.setMaximumSize(d);
        jButtonJump.setMargin(new Insets(2, 2,2,2));
        jButtonJump.setToolTipText("Jump to parent Section...");
        
        
        jButtonJump.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonJump_actionPerformed(e);
            }
        }); 
        
        
        jComboBoxFunctions.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jComboBoxFunctions_actionPerformed(e);
            }
        });
        panelButtons.add(jPanelOkCancel,  BorderLayout.SOUTH);
        jPanelOkCancel.add(jButtonOK, null);
        jPanelOkCancel.add(jButtonCancel, null);
        jPanelinfo.add(jLabelMain,       new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 9));
        
        this.getContentPane().add(jPanelParameters, BorderLayout.CENTER);
        this.getContentPane().add(panelButtons,  BorderLayout.SOUTH);
        panelButtons.add(jPanelOptions, BorderLayout.CENTER);
        
        
        jPanelMainOptions.add(jButtonDelete, null);
        jPanelMainOptions.add(jButtonBiophys);
        jPanelMainOptions.add(jButtonCheckValid, null);
        
        jPanelOptions.setLayout(new BorderLayout());
        
        jPanelOptions.add(jPanelMainOptions, BorderLayout.CENTER);
        jPanelOptions.add(jPanelUpdateOptions, BorderLayout.SOUTH);
        
        jLabelSegChildren.setText("Child segments:");
        jPanelUpdateOptions.add(jButtonUpdate, null); 
        jPanelUpdateOptions.add(jLabelSegChildren, null); 
        jPanelUpdateOptions.add(jRadioButtonNoMove, null); 
        jPanelUpdateOptions.add(jRadioButtonTransChildren, null); 
        jPanelUpdateOptions.add(jRadioButtonRotTransChildren, null); 
        
        jRadioButtonNoMove.setText("Don't move");
        jRadioButtonTransChildren.setText("Translate");
        jRadioButtonRotTransChildren.setText("Rotate & translate");
        
        buttonGrpChildern.add(jRadioButtonNoMove);
        buttonGrpChildern.add(jRadioButtonTransChildren);
        buttonGrpChildern.add(jRadioButtonRotTransChildren);
        
        jRadioButtonRotTransChildren.setSelected(true);
        
        
        panelButtons.add(jPanelFunctions, BorderLayout.NORTH); 
        
        jPanelFunctions.add(jComboBoxFunctions); 
        this.getContentPane().add(jPanelinfo, BorderLayout.NORTH);
        
        jPanelinfo.add(jComboBoxSection,    new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));
        
        jPanelinfo.add(jComboBoxSegment,  new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0)); 
        
        jComboBoxSection.addItem(defaultSectionItem);
        jComboBoxSegment.addItem(defaultSegmentItem);

        jComboBoxFiniteVolume.addItem(finiteVolYes);
        jComboBoxFiniteVolume.addItem(finiteVolNo);
        
        
        String info = "To alter the start point of the section, select the first segment in the section, and change its start point";
        jLabelStartSecPoint.setToolTipText(info);
        jTextFieldStartSecPOoint.setToolTipText(info);
        
        jLabelNumberIntDivisions.setToolTipText(toolTipText.getToolTip("Internal number of divisions"));
        jTextFieldNumIntDivisions.setToolTipText(toolTipText.getToolTip("Internal number of divisions"));
        
        jTextFieldEndPoint.getDocument().addDocumentListener(this);
        jTextFieldStartPoint.getDocument().addDocumentListener(this);
        jTextFieldSegmentsName.getDocument().addDocumentListener(this);
        jTextFieldStartRadius.getDocument().addDocumentListener(this);
        jTextFieldEndRadius.getDocument().addDocumentListener(this);
        jTextFieldFractAlong.getDocument().addDocumentListener(this);


        addSectionFields();
        //addSegementFields();
    }
    

    
    

    private void extraInit()
    {
        if (myCell == null)
        {
            jLabelMain.setText("No Cell type given");
            return;
        }

        jLabelMain.setText(mainLabel+ myCell.getInstanceName());

        jComboBoxSection.removeAllItems();
        jComboBoxSection.addItem(defaultSectionItem);
        ArrayList<Section> allSections = myCell.getAllSections();
        logger.logComment("There are "+allSections.size()+" sections");
        for (int i = 0; i < allSections.size(); i++)
        {
           jComboBoxSection.addItem(allSections.get(i).getSectionName());
        }

        jComboBoxSegment.removeAllItems();
        jComboBoxSegment.addItem(defaultSegmentItem);
        jComboBoxSegment.setEnabled(false);

        jComboBoxFunctions.removeAllItems();
        this.jComboBoxFunctions.addItem(FUNCTION_INSTR);
        this.jComboBoxFunctions.addItem(ADD_SEG_IN_NEW_SEC);
        this.jComboBoxFunctions.addItem(ADD_SEG_IN_THIS_SEC);
        this.jComboBoxFunctions.addItem(SPLIT_SEC);
        this.jComboBoxFunctions.addItem(SPECIFY_AXONAL_ARBOUR);
        this.jComboBoxFunctions.addItem(REMESH_CELL);
        this.jComboBoxFunctions.addItem(COMMENT);


    }

    /*
     * Details on a segment are being shown, so add fields for specifying a segment
     */
    private void addSegementFields()
    {
        shownFields = SEGMENTS_SHOWN;
        logger.logComment("Setting shown fields to: SEGMENTS_SHOWN");

        jPanelParameters.removeAll();

        jPanelParameters.setLayout(gridBagLayout2);
        
        
        
        jPanelParameters.add(jLabelName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(6, 14, 6, 60), 0, 0));
        
        jPanelParameters.add(jTextFieldSegmentsName, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
                                                                    , GridBagConstraints.WEST,
                                                                    GridBagConstraints.HORIZONTAL,
                                                                    new Insets(6, 0, 6, 14), 0, 0));
        
        
        
        jPanelParameters.add(jLabelID, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                              , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                              new Insets(6, 14, 6, 0), 0, 0));
        
        jPanelParameters.add(jTextFieldID, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
                                                                  , GridBagConstraints.EAST,
                                                                  GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 14),
                                                                  130, 0));
        
        
        
        jPanelParameters.add(jLabelSection, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                                   , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                   new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldNameSecInSegView, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
                                                                       , GridBagConstraints.WEST,
                                                                       GridBagConstraints.HORIZONTAL,
                                                                       new Insets(6, 0, 6, 14), 184, 0));
        
        
        
        jPanelParameters.add(jLabelStartPoint, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.NONE, new Insets(6, 14, 6, 0),
                                                                      0, 0));
        jPanelParameters.add(jTextFieldStartPoint, new GridBagConstraints(1, 3, 2, 1, 1.0, 0.0
                                                                          , GridBagConstraints.WEST,
                                                                          GridBagConstraints.HORIZONTAL,
                                                                          new Insets(6, 0, 6, 14), 0, 0));
        
        
        
        jPanelParameters.add(jLabelStartRadius, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                                       , GridBagConstraints.WEST,
                                                                       GridBagConstraints.NONE, new Insets(6, 14, 6, 0),
                                                                       0, 0));
        jPanelParameters.add(jTextFieldStartRadius, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0
                                                                           , GridBagConstraints.WEST,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets(6, 0, 6, 14), 0, 0));
        
        
        
        jPanelParameters.add(jLabelEndPoint, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                                    , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                    new Insets(6, 14, 6, 0), 0, 0));
        
        jPanelParameters.add(jTextFieldEndPoint, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
                                                                        , GridBagConstraints.WEST,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets(6, 0, 6, 0), 0, 0));
        
        jPanelParameters.add(jButtonCoords, new GridBagConstraints(2, 5, 1, 1, 1.0, 0.0
                                                                        , GridBagConstraints.CENTER,
                                                                        GridBagConstraints.NONE,
                                                                        new Insets(6, 0, 6, 0), 0, 0));
        
        
        
        jPanelParameters.add(jLabelEndRadius, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                     new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldEndRadius, new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0
                                                                         , GridBagConstraints.WEST,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets(6, 0, 6, 14), 0, 0));
        
        
        
        
        jPanelParameters.add(jLabelSegLength, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
                                                                  , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                  new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldSegLength, new GridBagConstraints(1, 7, 2, 1, 1.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.HORIZONTAL,
                                                                      new Insets(6, 0, 6, 14), 0, 0));
        
        
        
        jPanelParameters.add(jLabelParent, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
                                                                  , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                  new Insets(6, 14, 6, 0), 0, 0));
        
        jPanelParameters.add(jTextFieldParent, new GridBagConstraints(1, 8, 1, 1, 1.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.HORIZONTAL,
                                                                      new Insets(6, 0, 6, 0), 0, 0));
        
        jPanelParameters.add(jButtonJump, new GridBagConstraints(2, 8, 1, 1, 1.0, 0.0
                                                                      , GridBagConstraints.CENTER,
                                                                      GridBagConstraints.NONE,
                                                                      new Insets(6, 0, 6, 0), 0, 0));
        
        
        
        jPanelParameters.add(jLabelFractAlong, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.NONE, new Insets(6, 14, 0, 0),
                                                                      0, 0));
        jPanelParameters.add(jTextFieldFractAlong, new GridBagConstraints(1, 9, 2, 1, 0.0, 0.0
                                                                          , GridBagConstraints.WEST,
                                                                          GridBagConstraints.HORIZONTAL,
                                                                          new Insets(6, 0, 6, 14), 0, 0));
        
        
        
        
        
        jPanelParameters.add(jLabelFiniteVol, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                     new Insets(6, 14, 6, 0), 0, 0));
        
        
        jPanelParameters.add(jComboBoxFiniteVolume, new GridBagConstraints(1, 10, 2, 1, 0.0, 0.0
                                                                           , GridBagConstraints.CENTER,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets(6, 0, 6, 14), 0, 0));
        
       

        jPanelParameters.repaint();


    }


    /*
     * Details on a section are being shown, so add fields for specifying a section
     */
    private void addSectionFields()
    {
        shownFields = SECTIONS_SHOWN;

        logger.logComment("Setting shown fields to: SECTIONS_SHOWN");

        jPanelParameters.removeAll();

        jPanelParameters.setLayout(gridBagLayout2);

        jPanelParameters.add(jLabelSectionName,          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldSectionsName,         new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jTextFieldNumIntDivisions,       new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jTextFieldStartSecPOoint,     new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 14), 0, 0));
        
        
        jPanelParameters.add(jLabelStartSecPoint,       new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        
        
        jPanelParameters.add(jLabelNumberIntDivisions,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 20), 0, 0));
        jPanelParameters.add(jLabelStartSecRadius,   new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldStartSecRadius,   new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 14), 0, 0));

        jPanelParameters.repaint();

        logger.logComment("Added section fields");

    }


    
    void updateCellWithEnteredInfo()
    {
        if (initialising) return; // ignore any updates triggered during initialisation
        
        
        boolean round = true;

        if (editable && currentlyAddressedSegment!=null && shownFields==SEGMENTS_SHOWN)
        {
            logger.logComment(">>>>>>>>>>>>  Updating the segment info...");
            
            Point3f oldEndPoint = new Point3f(currentlyAddressedSegment.getEndPointPosition());
            Point3f newEndPoint = null;
            try
            {
                newEndPoint = parsePoint(jTextFieldEndPoint.getText());
            }
            catch (Exception e)
            {
                GuiUtils.showErrorMessage(logger, "Problem reading the end point info...", e, null);
                return;
            }


            Point3f oldStartPoint = new Point3f(currentlyAddressedSegment.getStartPointPosition());
            Point3f newStartPoint = null;
            try
            {
                newStartPoint = parsePoint(jTextFieldStartPoint.getText());
            }
            catch (Exception e)
            {
                GuiUtils.showErrorMessage(logger, "Problem reading the start point info...", e, null);
                return;
            }
            
            
            currentlyAddressedSegment.setEndPointPositionX(newEndPoint.x);
            currentlyAddressedSegment.setEndPointPositionY(newEndPoint.y);
            currentlyAddressedSegment.setEndPointPositionZ(newEndPoint.z);
            
            
            if (currentlyAddressedSegment.isFirstSectionSegment())
            {
                logger.logComment("Setting start point of section to: " + newStartPoint);
                currentlyAddressedSegment.getSection().setStartPointPositionX(newStartPoint.x);
                currentlyAddressedSegment.getSection().setStartPointPositionY(newStartPoint.y);
                currentlyAddressedSegment.getSection().setStartPointPositionZ(newStartPoint.z);
            }
            else
            {
                logger.logComment("Setting start point of parent segment to: " + newStartPoint);
                currentlyAddressedSegment.getParentSegment().setEndPointPositionX(newStartPoint.x);
                currentlyAddressedSegment.getParentSegment().setEndPointPositionY(newStartPoint.y);
                currentlyAddressedSegment.getParentSegment().setEndPointPositionZ(newStartPoint.z);
            }

            try
            {
                currentlyAddressedSegment.setRadius(Float.parseFloat(jTextFieldEndRadius.getText()));
                float startRadius = Float.parseFloat(jTextFieldStartRadius.getText());
                if (currentlyAddressedSegment.isFirstSectionSegment())
                {
                    logger.logComment("Setting start radius of section to: "+ startRadius);
                    currentlyAddressedSegment.getSection().setStartRadius(startRadius);
                }
                else
                {
                    logger.logComment("Setting start radius of parent segment to: "+ startRadius);
                    currentlyAddressedSegment.getParentSegment().setRadius(startRadius);
                }
            }
            catch (Exception e)
            {
                GuiUtils.showErrorMessage(logger, "Problem reading the radius info...", e, null);
                return;
            }
            try
            {
                float fract = Float.parseFloat(jTextFieldFractAlong.getText());
                if (fract<0||fract>1)
                {
                    GuiUtils.showErrorMessage(logger, "Fraction along parent must be between 1 and 0", null, null);
                    return;
                }
                currentlyAddressedSegment.setFractionAlongParent(fract);
            }
            catch (Exception e)
            {
                GuiUtils.showErrorMessage(logger, "Problem reading the value of Fraction along parent...", e, null);
                return;
            }


            currentlyAddressedSegment.setFiniteVolume(jComboBoxFiniteVolume.getSelectedItem().equals(finiteVolYes));
           
            currentlyAddressedSegment.setSegmentName(jTextFieldSegmentsName.getText());
            
            boolean applyExtraTranslation = false;
            
            if (jRadioButtonRotTransChildren.isSelected())
            {
              //applyExtraTranslation = currentlyAddressedSegment.isFirstSectionSegment();
                
                logger.logComment("rotate oldEndPoint: "+oldEndPoint);
                logger.logComment("rotate oldStartPoint: "+oldStartPoint);
                logger.logComment("rotate newEndPoint: "+newEndPoint);
                
                Vector3f vecToOldEnd = new Vector3f(oldEndPoint);
                vecToOldEnd.sub(oldStartPoint);                       // Subtract the oldStartPoint from the oldEndPoint to get the vector from the old start point
                Vector3f vecToOldEndNorm = new Vector3f(vecToOldEnd); // vector to old end of currently address segment, i.e. the one that has been edited
                vecToOldEndNorm.normalize();                          // normalise it so you can work out the rotation that has been applied by the entered data
                
                logger.logComment("rotate vecToOldEnd: "+vecToOldEnd);
                
                Vector3f vecToNewEnd = new Vector3f(newEndPoint);
              //vecToNewEnd.sub(newStartPoint);
                vecToNewEnd.sub(oldStartPoint);                       // New start point should be the same as the old ??can this change??
                Vector3f vecToNewEndNorm = new Vector3f(vecToNewEnd); // Vector to newEndPoint to allow the rotation angle to be calculated
                vecToNewEndNorm.normalize();
                
                logger.logComment("vecToNewEnd: "+vecToNewEnd);
                
                if (!vecToNewEnd.equals(vecToOldEnd))                 // if there has been no rotation made then no need to adjust the child segments
                {
                    Vector<Segment> allChildren = CellTopologyHelper.getAllChildSegsToBranchEnd(myCell, currentlyAddressedSegment); // Get all the children of the segment being edited 
                    
                    AxisAngle4f angle4 = Utils3D.getAxisAngle(vecToOldEndNorm, vecToNewEndNorm); // works out the angle through which the newdendpoint has been rotated in relation to old endpoint
                    
                    logger.logComment("rotate angle4: "+angle4);
                    
                    Transform3D transform = new Transform3D();        // creates a matrix that will apply a rotation about a point for any vector from that point, in this case the endpoint of the edited segment, or the start point of the first child segment
                    transform.set(angle4);
                    
                    logger.logComment("rotate transform: "+transform);
                    
                    for(Segment seg: allChildren)                     // apply rotate all the child segments, get child and assign it as current seg 
                    {
                        logger.logComment("Rotate Child seg: "+seg);
                        
                        if (seg.isFirstSectionSegment())              // Sections require that the start points to be defined at a section level
                        {
                            Section sec = seg.getSection();           // allocate the current segmenst section as sec
                            
                            Vector3f vecToSecStart = new Vector3f(sec.getStartPointPosition()); // get the start point of the current section
                         // vecToSecStart.sub(vecToOldEnd);                                     // ?? the vector to the start position is zero in the first child??
                            vecToSecStart.sub(oldEndPoint);
                            logger.logComment("Rotate First Seg vecToSecStart: "+vecToSecStart);

                            transform.transform(vecToSecStart);                                 // apply rotation to startpoint, should be zero for first child
                        
                            logger.logComment("Rotate First Seg vecToSecStart after rot: "+vecToSecStart);
                        
              
                            sec.setStartPointPositionX(vecToSecStart.x + newEndPoint.x, round);        // it should rotate about oldendpoint - just the new endpoint for the fist child
                            sec.setStartPointPositionY(vecToSecStart.y + newEndPoint.y, round);
                            sec.setStartPointPositionZ(vecToSecStart.z + newEndPoint.z, round);                            
                        } 
                        else
                        {
                            logger.logComment("Not FirstSectionSegment");
                        }
                        // work out the endpoint for the current child segment
                        logger.logComment("Child seg now: "+seg);
                        logger.logComment("Child seg endPointPosition now: "+seg.getEndPointPosition());
                        logger.logComment("Child seg vecToOldEnd now: "+vecToOldEnd);
                                                        
                        Vector3f vecToSegEnd = new Vector3f(seg.getEndPointPosition());
                        vecToSegEnd.sub(oldEndPoint);        
                        
                        logger.logComment("Rotate vecToSegEnd: "+vecToSegEnd);
                    
                        transform.transform(vecToSegEnd);                                       // apply rotation to vector from oldend (parent seg) to endpoint of current child seg

                        logger.logComment("Rotate vecToSegEnd after rot: "+vecToSegEnd);
                        
          
                        seg.setEndPointPositionX(vecToSegEnd.x + newEndPoint.x, round);                // add vector to new endpoint of parent segment.
                        seg.setEndPointPositionY(vecToSegEnd.y + newEndPoint.y, round);
                        seg.setEndPointPositionZ(vecToSegEnd.z + newEndPoint.z, round);
                        
                        logger.logComment("Child seg finally: "+seg);
                    }
                     // should the apply extra translation be here - only first child segment needs the translation applied
                }
                
                
                
            }
            
            if (jRadioButtonTransChildren.isSelected() || (applyExtraTranslation))
            {
                Point3f translation = new Point3f(newEndPoint);
                translation.sub(oldEndPoint);
                
                Vector<Segment> allChildren = CellTopologyHelper.getAllChildSegsToBranchEnd(myCell, currentlyAddressedSegment);
                for(Segment seg: allChildren)
                {
                    
                        logger.logComment("Child seg to translate: "+seg);
                        
                    if (seg.isFirstSectionSegment())
                    {
                        Section sec = seg.getSection();
                        sec.setStartPointPositionX(sec.getStartPointPositionX() + translation.x);
                        sec.setStartPointPositionY(sec.getStartPointPositionY() + translation.y);
                        sec.setStartPointPositionZ(sec.getStartPointPositionZ() + translation.z);
                    }
                    seg.setEndPointPositionX(seg.getEndPointPositionX() + translation.x);
                    seg.setEndPointPositionY(seg.getEndPointPositionY() + translation.y);
                    seg.setEndPointPositionZ(seg.getEndPointPositionZ() + translation.z);
                    
                    logger.logComment("Child seg translated: "+seg);
                }
            }
            
            this.registerPointFieldChange();

        }
        if (editable && shownFields==SECTIONS_SHOWN && getSelectedSection()!=null)
        {
            logger.logComment(">>>>>>>>>>>>  Updating the section info...");
            Section selSection = getSelectedSection();

            selSection.setSectionName(jTextFieldSectionsName.getText());
            try
            {
                selSection.setNumberInternalDivisions(Integer.parseInt(jTextFieldNumIntDivisions.getText()));
            }
            catch (Exception e)
            {
                GuiUtils.showErrorMessage(logger, "Problem reading the number of internal divisions...", e, null);
                return;
            }

        }
        if (oneCell3DPanel!=null) oneCell3DPanel.markCellAsEdited();
        
        jButtonUpdate.setForeground(okColourFG);
    }

    private Point3f parsePoint(String pointString) throws NumberFormatException, StringIndexOutOfBoundsException
    {
        pointString = pointString.trim();
        if (pointString.startsWith("(")) pointString = pointString.substring(1);
        if (pointString.endsWith(")")) pointString = pointString.substring(0, pointString.length()-1);

        String xString = pointString.substring(0, pointString.indexOf(","));

        String yString = pointString.substring(pointString.indexOf(",")+1
                                               , pointString.lastIndexOf(","));

        String zString = pointString.substring(pointString.lastIndexOf(",")+1);

        return new Point3f(Float.parseFloat(xString.trim()),
                           Float.parseFloat(yString.trim()),
                           Float.parseFloat(zString.trim()));
    }



    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;
        this.dispose();
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        updateCellWithEnteredInfo();

        logger.logComment("OK button pressed");
        cancelled = false;

        if (oneCell3DPanel!=null)
            oneCell3DPanel.repaint3DScene();
        this.dispose();
    }


    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);


            Project testProj = Project.loadProject(new File("nCexamples/Ex6_CerebellumDemo/Ex6_CerebellumDemo.neuro.xml"),
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
            Cell p = testProj.cellManager.getCell("PurkinjeCell");
            SegmentSelector dlg = new SegmentSelector(new Frame(),
                                                      testProj,
                                                      p,
                                                      true);

            //Center the window
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = dlg.getSize();
            if (frameSize.height > screenSize.height)
            {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width)
            {
                frameSize.width = screenSize.width;
            }
            dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            dlg.setVisible(true);

            dlg.setSelectedSegment(p.getSegmentWithId(43));

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public Segment getSelectedSegment()
    {
        return currentlyAddressedSegment;
    }

    void jComboBoxSection_itemStateChanged(ItemEvent e)
    {
        logger.logComment("            Section Item changed: "+ e.getItem());

        //updateCellWithEnteredInfo();

        if (e.getStateChange()==ItemEvent.SELECTED)
        {
            logger.logComment("Selected: "+e.getItem());
            if (e.getItem().equals(defaultSectionItem))
            {

                jComboBoxSegment.removeAllItems();
                jComboBoxSegment.addItem(defaultSegmentItem);
                jComboBoxSegment.setEnabled(false);
                currentlyAddressedSegment = null;
                addSegementFields();
                replaceSegmentSectionInfo();

            }
            else
            {
                currentlyAddressedSegment = null;

                jComboBoxSegment.removeAllItems();
                jComboBoxSegment.addItem(defaultSegmentItem);
                jComboBoxSegment.addItem(sectionDetails);
                jComboBoxSegment.setEnabled(true);

                LinkedList<Segment> allSegmentHere = myCell.getAllSegmentsInSection(getSelectedSection());

                for (int i = 0; i < allSegmentHere.size(); i++)
                {
                    jComboBoxSegment.addItem((allSegmentHere.get(i)).getSegmentName());
                }
                logger.logComment("Setting segment details item selected");
                jComboBoxSegment.setSelectedItem(sectionDetails);


            }
        }
    }

    private Section getSelectedSection()
    {
        String selectedSectionString = (String) jComboBoxSection.getSelectedItem();
        logger.logComment("Selected string for section: "+ selectedSectionString);
        ArrayList<Section> allSections = myCell.getAllSections();

        Section selectedSection = null;

        for (int i = 0; i < allSections.size(); i++)
        {
            Section nextSec = allSections.get(i);

            logger.logComment("Checking... "+ nextSec);
            if ( nextSec.getSectionName().equals(selectedSectionString))
            {
                selectedSection = nextSec;
            }
        }
        logger.logComment("  Asked for selected section: "+ selectedSection);
        return selectedSection;
    }



    void jComboBoxSegment_itemStateChanged(ItemEvent e)
    {
        logger.logComment("++++++++     Segment Item changed: " + e.getItem());

        //updateCellWithEnteredInfo();

        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            if (e.getItem().equals(defaultSegmentItem))
            {
                currentlyAddressedSegment = null;

                addSegementFields();

                replaceSegmentSectionInfo();

                if (oneCell3DPanel != null && !updatingFrom3DView)
                {
                    oneCell3DPanel.markSegmentAsSelected( -1, false);
                    this.toFront();
                }

                return;

            }
            else if (e.getItem().equals(sectionDetails))
            {
                currentlyAddressedSegment = null;

                addSectionFields();

                replaceSegmentSectionInfo();

                if (oneCell3DPanel != null && !updatingFrom3DView)
                {
                    oneCell3DPanel.markSegmentAsSelected( -1, false);
                    this.toFront();
                }

                return;

            }

            else
            {
                String selectedSegmentString = (String) e.getItem();
                Vector allSegments = myCell.getAllSegments();



                for (int i = 0; i < allSegments.size(); i++)
                {
                    if ( ( (Segment) allSegments.elementAt(i)).getSegmentName().equals(selectedSegmentString))
                    {
                        currentlyAddressedSegment = (Segment) allSegments.elementAt(i);
                        //currentlyAddressedSection = null;

                        addSegementFields();
                        replaceSegmentSectionInfo();

                        if (oneCell3DPanel != null && !updatingFrom3DView)
                        {
                            oneCell3DPanel.markSegmentAsSelected(currentlyAddressedSegment.getSegmentId(), false);
                            this.toFront();
                        }
                    }
                }

                if (currentlyAddressedSegment!=null)
                {
                    if (currentlyAddressedSegment.isFirstSectionSegment())
                    {
                        jTextFieldStartPoint.setEditable(editable);
                        jTextFieldStartRadius.setEditable(editable);
                    }
                    else
                    {
                        jTextFieldStartPoint.setEditable(false);
                        jTextFieldStartRadius.setEditable(false);

                    }
                }

            }
        }
    }

    void jButtonUpdate_actionPerformed(ActionEvent e)
    {
        updateCellWithEnteredInfo();

       logger.logComment("Update button pressed");

       if (oneCell3DPanel!=null)
       oneCell3DPanel.markSegmentAsSelected(currentlyAddressedSegment.getSegmentId(), true);

       logger.logComment(CellTopologyHelper.printDetails(myCell, null));

    }

    void jButtonDelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Current section: "+ currentlyAddressedSegment);
        if (currentlyAddressedSegment == null) return;

        if (currentlyAddressedSegment.isSomaSegment()&&currentlyAddressedSegment.isFirstSectionSegment())
        {
            GuiUtils.showErrorMessage(logger, "This is the first section. This section cannot be deleted", null, null);
            return;
        }

        markSegmentForDeletion(currentlyAddressedSegment);

        Segment parent = currentlyAddressedSegment.getParentSegment();

        Vector<Segment> allSegments = myCell.getAllSegments();
        
        //ArrayList<Section> allSections = myCell.getAllSections();

        for (int i = 0; i < segmentsForDeletion.size(); i++)
        {

            Segment nextSeg = segmentsForDeletion.elementAt(i);
            logger.logComment("Looking to delete: "+ nextSeg);

            if (allSegments.contains(nextSeg))
            {
                logger.logComment("--   Removing "+nextSeg+" from cell segment list");
                allSegments.remove(nextSeg);
            }
            else
            {
                logger.logError("Not in list of segments!!");
            }
        }
        myCell.setAllSegments(allSegments);

        extraInit();


        oneCell3DPanel.markSegmentAsSelected(parent.getSegmentId(), true);
        jComboBoxSection.setSelectedItem(parent.getSection().getSectionName());
        jComboBoxSegment.setSelectedItem(parent.getSegmentName());



    }

    private void markSegmentForDeletion(Segment segment)
    {
        logger.logComment("-----    Marking for deletion segment: "+ segment);
        segmentsForDeletion.add(segment);
        Vector allSegments = myCell.getAllSegments();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment nextSeg = (Segment)allSegments.elementAt(i);

            if (nextSeg.getParentSegment()!=null &&
                nextSeg.getParentSegment().equals(segment))
                markSegmentForDeletion(nextSeg);
        }
    }
    
    
    public void changedUpdate(DocumentEvent e)
    {
        registerPointFieldChange();
    }

    public void insertUpdate(DocumentEvent e)
    {
        registerPointFieldChange();
    }

    public void removeUpdate(DocumentEvent e)
    {
        registerPointFieldChange();
    }
    
    
    void registerPointFieldChange()
    {           
        Point3f startPoint = null;
        Point3f endPoint = null;
        boolean altered = false;
        
        // Check start point field
        
        Color startPointColour = okColourBG;
        try
        {
            startPoint = parsePoint(jTextFieldStartPoint.getText());
            
            if (startPoint==null) 
            {
                startPointColour = warningColour;
            }
            else if (currentlyAddressedSegment!=null && 
                    !currentlyAddressedSegment.getStartPointPosition().equals(startPoint))
            {
                startPointColour = alteredColour;
                altered = true;
            }
        }
        catch (Exception e)
        {
            startPointColour = warningColour;
        }
        this.jTextFieldStartPoint.setBackground(startPointColour);
        
        
        // Check end point field

        Color endPointColour = okColourBG;
        try
        {
            endPoint = parsePoint(jTextFieldEndPoint.getText());
                
            if (endPoint==null) 
                endPointColour = warningColour;
            else if (currentlyAddressedSegment!=null && 
                    !currentlyAddressedSegment.getEndPointPosition().equals(endPoint))
            {
                endPointColour = alteredColour;
                altered = true;
            }
        }
        catch (Exception e)
        {
            endPointColour = warningColour;
        }
        this.jTextFieldEndPoint.setBackground(endPointColour);
        
        
        // Update the segment length
        
        float len = -1;
        if (endPoint!=null && startPoint!=null)
        {
            len = endPoint.distance(startPoint);
            jTextFieldSegLength.setText(len+"");

            if (currentlyAddressedSegment!=null && 
                currentlyAddressedSegment.getSegmentLength()!=len)
            {
                jTextFieldSegLength.setBackground(alteredColour);
                jTextFieldSegLength.setText(len+" (\u0394 = "+(len-currentlyAddressedSegment.getSegmentLength())+")");
                altered = true;
            }
            else
            {
                jTextFieldSegLength.setBackground(jTextFieldID.getBackground()); // id field always disabled
            }
        }
        else
        {
            jTextFieldSegLength.setText("Cannot be calculated");
            jTextFieldSegLength.setBackground(warningColour);
        }
        
        
        
        // Check segment name field
        
        
        if (jTextFieldSegmentsName.getText().indexOf(" ")>=0)
        {
            jTextFieldSegmentsName.setBackground(warningColour);
        }
        else
        {
            if (currentlyAddressedSegment!=null && 
                !jTextFieldSegmentsName.getText().equals(currentlyAddressedSegment.getSegmentName()))
            {
                jTextFieldSegmentsName.setBackground(alteredColour);
                altered = true;
            }
            else
            {
                jTextFieldSegmentsName.setBackground(okColourBG);
            }
        }
        
        
        
        // Check start radius field
        
        
        try
        {
            if (Float.parseFloat(jTextFieldStartRadius.getText())>=0) 
            {
                if (currentlyAddressedSegment!=null && 
                    Float.parseFloat(jTextFieldStartRadius.getText()) != currentlyAddressedSegment.getSegmentStartRadius())
                {
                    jTextFieldStartRadius.setBackground(alteredColour);
                    altered = true;
                }
                else
                {
                    jTextFieldStartRadius.setBackground(okColourBG);
                }
            }
            else
            {
                jTextFieldStartRadius.setBackground(warningColour);
            }
        }
        catch (Exception e)
        {
            jTextFieldStartRadius.setBackground(warningColour);
        }
        
        
        
        // Check end radius field
        
        try
        {
            if (Float.parseFloat(jTextFieldEndRadius.getText())>=0) 
            {
                if (currentlyAddressedSegment!=null && 
                    Float.parseFloat(jTextFieldEndRadius.getText()) != currentlyAddressedSegment.getRadius())
                {
                    jTextFieldEndRadius.setBackground(alteredColour);
                    altered = true;
                }
                else
                {
                    jTextFieldEndRadius.setBackground(okColourBG);
                }
            }
            else
            {
                jTextFieldEndRadius.setBackground(warningColour);
            }
                
        }
        catch (Exception e)
        {
            jTextFieldEndRadius.setBackground(warningColour);
        }
        
        
        // Check fract along
        
        try
        {
            if (Float.parseFloat(jTextFieldFractAlong.getText())>=0 &&
                Float.parseFloat(jTextFieldFractAlong.getText())<=1 ) 
            {
                if (currentlyAddressedSegment!=null && 
                    Float.parseFloat(jTextFieldFractAlong.getText()) != currentlyAddressedSegment.getFractionAlongParent())
                {
                    jTextFieldFractAlong.setBackground(alteredColour);
                    altered = true;
                }
                else
                {
                    jTextFieldFractAlong.setBackground(okColourBG);
                }
            }
            else
            {
                jTextFieldFractAlong.setBackground(warningColour);
            }
                
        }
        catch (Exception e)
        {
            jTextFieldFractAlong.setBackground(warningColour);
        }
            
        if (altered)
            jButtonUpdate.setForeground(alteredColour.darker().darker());
        else
            jButtonUpdate.setForeground(okColourFG);
    }
    
    

    void jButtonCheckValid_actionPerformed(ActionEvent e)
    {
        updateCellWithEnteredInfo();

        String cellValidity = CellTopologyHelper.getValidityStatus(myCell).getValidity();
        GuiUtils.showInfoMessage(logger, "Cell morphological validity status", "Morphological validity status: "+cellValidity, this);
    }

    void addInNewSec_actionPerformed(ActionEvent e)
    {
        if (currentlyAddressedSegment==null) return;

        Vector allSegments = myCell.getAllSegments();
        for (int i = 0; i < allSegments.size(); i++)
        {
                Segment nextSeg = (Segment)allSegments.elementAt(i);
                if(nextSeg.getParentSegment() !=null &&
                   nextSeg.getParentSegment().equals(currentlyAddressedSegment) &&
                   nextSeg.getSection().equals(currentlyAddressedSegment.getSection()))
                {
                    GuiUtils.showErrorMessage(logger, "This is not the last segment in the section. Sections must be unbranched, "+
                                                  "\nand so you cannot add a new segment inside a section", null, this);
                        return;
                }
        }

        String requestSec = "Please enter the new Section name";

        int suggestedSectionPrefix  = myCell.getAllSections().size();
        int suggestedSegmentPrefix  = allSegments.size();
        boolean problemWithNames = true;

        while(problemWithNames)
        {
            problemWithNames = false;
            for (int i = 0; i < allSegments.size(); i++)
            {
                Segment nextSeg = (Segment) allSegments.elementAt(i);
                if (nextSeg.getSegmentName().equals("Segment_" + suggestedSegmentPrefix))
                {
                    suggestedSegmentPrefix++;
                    problemWithNames = true;
                }
                if (nextSeg.getSection().getSectionName().equals("Section_" + suggestedSectionPrefix))
                {
                    suggestedSectionPrefix++;
                    problemWithNames = true;
                }

            }
        }

        String newSectionName = JOptionPane.showInputDialog(this, requestSec, "Section_" + suggestedSectionPrefix);

        if (newSectionName==null)
        {
            logger.logComment("User cancelled...");
            return;
        }

        String requestSeg = "Please enter the new Segment name";

        String newSegmentName = JOptionPane.showInputDialog(this, requestSeg, "Segment_" + suggestedSegmentPrefix);


        if (newSegmentName==null)
        {
            logger.logComment("User cancelled...");
            return;
        }



        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment nextSeg = (Segment)allSegments.elementAt(i);
            if (nextSeg.getSegmentName().equals(requestSeg))
            {
                GuiUtils.showErrorMessage(logger, "The Segment name: "+requestSeg+" has been used before.", null, this);
                    return;
            }

            if (nextSeg.getSection().getSectionName().equals(requestSec))
            {
                GuiUtils.showErrorMessage(logger, "The Section name: "+requestSec+" has been used before.", null, this);
                    return;
            }

        }




        if (oneCell3DPanel!=null) oneCell3DPanel.markCellAsEdited();

        //Section newSection = new Section(

        Point3f oldEnd = currentlyAddressedSegment.getEndPointPosition();
        Point3f oldStart = currentlyAddressedSegment.getStartPointPosition();

        Point3f newEndPoint = new Point3f(2*oldEnd.x - oldStart.x,
                                          2*oldEnd.y - oldStart.y,
                                          2*oldEnd.z - oldStart.z);

        Segment newSegment = null;

        if (currentlyAddressedSegment.getSection().getGroups().contains(Section.SOMA_GROUP) ||
            currentlyAddressedSegment.getSection().getGroups().contains(Section.DENDRITIC_GROUP))
        {
            boolean inheritRadius = currentlyAddressedSegment.getSection().getGroups().contains(Section.DENDRITIC_GROUP);
            
            newSegment = myCell.addDendriticSegment(currentlyAddressedSegment.getRadius(),
                                       newSegmentName,
                                       newEndPoint,
                                       currentlyAddressedSegment,
                                       1,
                                       newSectionName,
                                       inheritRadius);
        }
        else
        {
            newSegment = myCell.addAxonalSegment(currentlyAddressedSegment.getRadius(),
                                       newSegmentName,
                                       newEndPoint,
                                       currentlyAddressedSegment,
                                       1,
                                       newSectionName);
        }

        logger.logComment("New segment made: "+ newSegment);


        extraInit();
        oneCell3DPanel.markSegmentAsSelected(newSegment.getSegmentId(), true);
        jComboBoxSection.setSelectedItem(newSegment.getSection().getSectionName());
        jComboBoxSegment.setSelectedItem(newSegment.getSegmentName());


    }

    void addInThisSec_actionPerformed(ActionEvent e)
    {
        if (currentlyAddressedSegment==null) return;

        Vector allSegments = myCell.getAllSegments();
        for (int i = 0; i < allSegments.size(); i++)
        {
                Segment nextSeg = (Segment)allSegments.elementAt(i);

                if(nextSeg.getParentSegment()!=null &&
                   nextSeg.getParentSegment().equals(currentlyAddressedSegment) &&
                   nextSeg.getSection().equals(currentlyAddressedSegment.getSection()))
                {
                    GuiUtils.showErrorMessage(logger, "This is not the last segment in the section. Sections must be unbranched, "+
                                                  "\nand so you cannot add a new segment inside a section", null, this);
                        return;
                }
        }

        String requestSeg = "Please enter the new Segment name";

        int suggestedSegmentPrefix  = allSegments.size();
        boolean problemWithName = true;

        while(problemWithName)
        {
            problemWithName = false;
            for (int i = 0; i < allSegments.size(); i++)
            {
                Segment nextSeg = (Segment) allSegments.elementAt(i);
                if (nextSeg.getSegmentName().equals("Segment_" + suggestedSegmentPrefix))
                {
                    suggestedSegmentPrefix++;
                    problemWithName = true;
                }

            }
        }


        String newSegmentName = JOptionPane.showInputDialog(this, requestSeg, "Segment_" + suggestedSegmentPrefix);


        if (newSegmentName==null)
        {
            logger.logComment("User cancelled...");
            return;
        }


        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment nextSeg = (Segment)allSegments.elementAt(i);
            if (nextSeg.getSegmentName().equals(requestSeg))
            {
                GuiUtils.showErrorMessage(logger, "The Segment name: "+requestSeg+" has been used before.", null, this);
                    return;
            }

        }


        if (oneCell3DPanel!=null) oneCell3DPanel.markCellAsEdited();

        Point3f oldEnd = currentlyAddressedSegment.getEndPointPosition();
        Point3f oldStart = currentlyAddressedSegment.getStartPointPosition();

        Point3f newEndPoint = new Point3f(2*oldEnd.x - oldStart.x,
                                          2*oldEnd.y - oldStart.y,
                                          2*oldEnd.z - oldStart.z);

        Segment newSegment = null;
        if (currentlyAddressedSegment.getSection().getGroups().contains(Section.SOMA_GROUP))
        {
            newSegment = myCell.addSomaSegment(currentlyAddressedSegment.getRadius(),
                                                    newSegmentName,
                                                    newEndPoint,
                                                    currentlyAddressedSegment,
                                                    currentlyAddressedSegment.getSection());
        }
        else if (currentlyAddressedSegment.getSection().getGroups().contains(Section.DENDRITIC_GROUP))
        {
            newSegment = myCell.addDendriticSegment(currentlyAddressedSegment.getRadius(),
                                       newSegmentName,
                                       newEndPoint,
                                       currentlyAddressedSegment,
                                       1,
                                       currentlyAddressedSegment.getSection().getSectionName(),
                                       true);
        }
        else if (currentlyAddressedSegment.getSection().getGroups().contains(Section.AXONAL_GROUP))
        {
            newSegment = myCell.addAxonalSegment(currentlyAddressedSegment.getRadius(),
                                       newSegmentName,
                                       newEndPoint,
                                       currentlyAddressedSegment,
                                       1,
                                       currentlyAddressedSegment.getSection().getSectionName());
        }


        logger.logComment("New segment made: "+ newSegment);

        extraInit();
        oneCell3DPanel.markSegmentAsSelected(newSegment.getSegmentId(), true);
        jComboBoxSection.setSelectedItem(newSegment.getSection().getSectionName());
        jComboBoxSegment.setSelectedItem(newSegment.getSegmentName());
    }
    
    public void jButtonJump_actionPerformed(ActionEvent e)
    {
        if (currentlyAddressedSegment==null) 
            return;
        
        if (currentlyAddressedSegment.getParentSegment()==null)
            return;
        
        setSelectedSegment(currentlyAddressedSegment.getParentSegment());
        
        
        if (oneCell3DPanel != null)
        {
            oneCell3DPanel.markSegmentAsSelected(currentlyAddressedSegment.getSegmentId(), false);
            this.toFront();
        }
        
        /*
        jComboBoxSection.setSelectedItem(currentlyAddressedSegment.getParentSegment().getSection().getSectionName());
        this.repaint();
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException ex)
        {
            // continue...
        }
        //jComboBoxSegment.setSelectedItem(currentlyAddressedSegment.getParentSegment().getSegmentName());*/
    }
    
    
    public void jButtonCoords_actionPerformed(ActionEvent e)
    {
        if (currentlyAddressedSegment==null) return;
        
        Point3f startPoint = parsePoint(jTextFieldStartPoint.getText()); 
        //int x = Integer.
        if (!(startPoint.x==(int)startPoint.x &&
              startPoint.y==(int)startPoint.y &&
              startPoint.z==(int)startPoint.z))
        {
            GuiUtils.showErrorMessage(logger, "Error, this function can only be used when the start point "+startPoint+" has all integer values.", null, this);
            
            return;
        }
        int len = 25;
        try
        {
            len = (int)(Float.parseFloat(jTextFieldSegLength.getText())+0.5);
        }
        catch(NumberFormatException ex)
        {
            len = (int)(currentlyAddressedSegment.getSegmentLength()+0.5);
        }
        
        String lenString = JOptionPane.showInputDialog("Please enter an integer value of the preferred length of the segment", len+"");
        if (lenString==null)
            return;
        
        try
        {
           len = Integer.parseInt(lenString);
        }
        catch(NumberFormatException ex)
        {
            jButtonCoords_actionPerformed(e); // back to start...
        }
        
        ArrayList<Point3f>  points = CoordCalculator.getCoords((int)startPoint.x, (int)startPoint.y, (int)startPoint.z, len);
        StringBuffer info = new StringBuffer("The following points are a distance of "+len +" from point "+startPoint+"\n\n" +
                "Copy and paste the chosen point in the left column into the end point field in the Segment Selector.\n" +
                "Thanks to Michele Mattioni for the end point generation code.\n\n");
        for(Point3f p : points)
        {
            Point3f del = new Point3f(p);
            del.sub(startPoint);
           
            info.append(GeneralUtils.getMinLenLine(p.toString(),30)+" \u0394 from start = "+del+"\n");
        }
        
        endPointViewer = SimpleViewer.showString(info.toString(), "Possible end points", 12, false, 
                false, .40f,.80f);
        //endPointViewer.pack();
        
        
    }
    

    public void jButtonBiophys_actionPerformed(ActionEvent e)
    {
        if (currentlyAddressedSegment==null) return;

        boolean useHtml = true;

        String summary = CellTopologyHelper.getSegmentBiophysics(currentlyAddressedSegment,
                                                                 myCell,
                                                                 project,
                                                                 useHtml);


        SimpleViewer.showString(summary,
                                "Biophysics of segment: " + currentlyAddressedSegment.getSegmentName() + " in cell: " +
                                myCell.getInstanceName(), 10, false, useHtml,
                                .6f);

    }

    private void splitAtCurrentSection()
    {
        if (this.currentlyAddressedSegment==null) return;

        String suggestedNewName = currentlyAddressedSegment.getSection().getSectionName()+"_split";
        int segId = currentlyAddressedSegment.getSegmentId();


        String requestSeg = "Please enter the name of the new Section which will be created after the split.\n"
                           +"This Segment and all child Segments (in the current Section) will be part of the new Section";

        String newName = JOptionPane.showInputDialog(this, requestSeg, suggestedNewName);

        if (newName == null) return;


        if (oneCell3DPanel!=null) oneCell3DPanel.markCellAsEdited();

        Section newSection = new Section(newName);

        newSection.setNumberInternalDivisions(currentlyAddressedSegment.getSection().getNumberInternalDivisions());
        newSection.setStartPointPositionX(currentlyAddressedSegment.getStartPointPosition().x);
        newSection.setStartPointPositionY(currentlyAddressedSegment.getStartPointPosition().y);
        newSection.setStartPointPositionZ(currentlyAddressedSegment.getStartPointPosition().z);
        newSection.setStartRadius(currentlyAddressedSegment.getSegmentStartRadius());

        Vector groups = currentlyAddressedSegment.getSection().getGroups();
        
        for (int i = 0; i < groups.size(); i++)
        {
            newSection.addToGroup((String)groups.elementAt(i));
        }

        this.setSecForSegAndChildren(currentlyAddressedSegment, newSection, true);
        
        currentlyAddressedSegment = myCell.getSegmentWithId(segId);
        
        this.setSelectedSegment(currentlyAddressedSegment);
        
        if (oneCell3DPanel != null)
        {
            oneCell3DPanel.markSegmentAsSelected(currentlyAddressedSegment.getSegmentId(), true);
            this.toFront();
        }


    }

    private void setSecForSegAndChildren(Segment segment, Section section, boolean onlySameSection)
    {
        logger.logComment("Calling setSecForSegAndChildren for: "+ segment);

        Vector allChildren = CellTopologyHelper.getAllChildSegments(myCell, segment, true);
        for (int i = 0; i < allChildren.size(); i++)
        {
            Segment nextSeg = (Segment)allChildren.elementAt(i);
            if (onlySameSection)
            {
                if (nextSeg.getSection().equals(segment.getSection()))
                {
                    this.setSecForSegAndChildren(nextSeg, section, onlySameSection);
                }
                else
                {
                    logger.logComment("Different sections...");
                }
            }
            else
            {
                this.setSecForSegAndChildren(nextSeg, section, onlySameSection);
            }
        }
        segment.setSection(section);
    }




    public void editComment()
    {
        String comment = this.currentlyAddressedSegment.getComment();
        if (comment == null) comment = "";

        String newComment = JOptionPane.showInputDialog(this, "Please enter a comment for segment "+ currentlyAddressedSegment.getSegmentName(), comment);

        if (newComment.length()>0) currentlyAddressedSegment.setComment(newComment);
    }

    
    public void reMesh()
    {
        String note = 
            "This will remesh the cell, choosing the number of internal divisions in each Section to ensure\n"
            +"a sufficiently small electrotonic length per division. That number of divisions is analogous to\n"+
            "nseg in NEURON (and used exactly as such in that simulator). With GENESIS, assuming the GENESIS\n"+
            "Compartmentalisation option is used, this value will be used to determine the number of GENESIS\n"+
            "compartments to use to represent the complex 3D Section with multiple Segments. See Electrotonic\n"+
            "Length and Compartmentalisation in the Glossary.\n\n"
            +"The cell will be remeshed to get the electronic length per internal division as close as possible to:\n"
            +""+ project.simulationParameters.getMaxElectroLen()+" (dimensionless units; set via Common Simulation Settings tab)\n\nProceed?";
        
        //boolean proceed = GuiUtils.showYesNoMessage(logger, note, this);
        
        Object[] vars = new Object[]{"Proceed", "Test only", "Cancel"};

        int sel = JOptionPane.showOptionDialog(this, note, "Proceed?",
                                               JOptionPane.OK_OPTION,
                                               JOptionPane.QUESTION_MESSAGE,
                                               null,
                                               vars,
                                               vars[0]);
        
        if (sel==2) return;

        boolean justTest = (sel==1);

        String report = CellTopologyHelper.recompartmentaliseCell(myCell,
                              project.simulationParameters.getMaxElectroLen(),
                              project,
                              true,
                              justTest);

        /*
        StringBuffer report = new StringBuffer();
        
        ArrayList<Section> secs = myCell.getAllSections();
        
       
        int countNew = 0;
        int countOld = 0;

        for(Section nextSec: secs)
        {
            try
            {
                LinkedList<Segment> segs = myCell.getAllSegmentsInSection(nextSec);
    
                //float specAxResVal = cell.getSpecAxRes().getNominalNumber();
                float totalElecLen = 0;
    
                float specMembRes = CellTopologyHelper.getSpecMembResistance(myCell, project, nextSec);
    
                float specAxRes = myCell.getSpecAxResForSection(nextSec);
                
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
                
                int bestNseg = Math.max(1, (int)Math.ceil(totalElecLen/project.simulationParameters.getMaxElectroLen()));

                String changed = "";

                if(!justTest)
                {
                    nextSec.setNumberInternalDivisions(bestNseg);
                }
                else
                {
                    changed = " (not changed)";
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
*/
        
        SimpleViewer.showString(report, "Remeshing report", 12, false, false, 0.8f, 0.8f);
        
        
    }



    public void specAxonalArbour()
    {
        Vector<AxonalConnRegion> aac = myCell.getAxonalArbours();

        int numToTry = aac.size();

        if (numToTry==0) numToTry = 1;

        for (int i = 0; i < numToTry; i++)
        {
            AxonalConnRegion aa = null;
            String message = null;
            Object[] options = null;

            if (aac.size()>0)
            {
                aa = aac.get(i);

                message = "Axonal Arbourisation connection: " + aa.getName() + " (" + (i + 1) + " of " +
                    aac.size() + ")\n" + "Region associated with this: " + aa.getRegion().toString();

                options = new Object[]
                    {"Add new", "Edit this connection", "Delete this connection"/*"Edit details", "Delete", "Add new"*/};

                if (i < aac.size() - 1)
                {
                    options = new Object[]
                        {"Add new", "Edit this connection", "Delete this connection", "Show next"};
                }
            }
            else
            {
                message = "No Axonal Arbourisation connections present";

                options = new Object[] {"Add new"};

            }
            int sel = JOptionPane.showOptionDialog(this,
                                                   message,
                                                   "Axonal Arbourisation",
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null,
                                                   options,
                                                   options[0]);
            if (sel == JOptionPane.CLOSED_OPTION)
            {
                return;
            }

            if (sel == 2)
            {
                myCell.deleteAxonalArbour(aa.getName());

                if (oneCell3DPanel!=null) oneCell3DPanel.markCellAsEdited();

            }
            else if (sel == 3)
            {
                logger.logComment("Proceeding to next...");
            }
            else
            {
                Region aaRegionToEdit = null;
                String suggestedName = null;

                if (sel == 1)
                {
                    aaRegionToEdit = aa.getRegion();
                    suggestedName = aa.getName();
                }
                else if (sel == 0)
                {
                    Segment first = myCell.getFirstSomaSegment();
                    aaRegionToEdit
                        = new SphericalRegion(first.getStartPointPosition().x,
                                              first.getStartPointPosition().y,
                                              first.getStartPointPosition().z, 50);

                    //if (suggestedRegion.getl)

                    suggestedName = "AxonalArbour_" + aac.size();


                }

                RegionsInfoDialog dlg = new RegionsInfoDialog(this,
                                                              aaRegionToEdit,
                                                              suggestedName);
                Dimension dlgSize = dlg.getPreferredSize();
                Dimension frmSize = getSize();
                Point loc = getLocation();
                dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                                (frmSize.height - dlgSize.height) / 2 + loc.y);
                dlg.setModal(true);
                dlg.pack();
                dlg.setVisible(true);

                String regionName = dlg.getRegionName();
                Region newRegion = dlg.getFinalRegion();

                if (dlg.cancelled)
                {
                    logger.logComment("The action was cancelled...");
                    return;
                }
                AxonalConnRegion acr = new AxonalConnRegion();
                acr.setName(regionName);
                acr.setRegion(newRegion);

                myCell.updateAxonalArbour(acr);

                if (oneCell3DPanel!=null) oneCell3DPanel.markCellAsEdited();


            }
        }


    }

    public void jComboBoxFunctions_actionPerformed(ActionEvent e)
    {
        String selected = (String)this.jComboBoxFunctions.getSelectedItem();


        if (selected !=null && /* Might be null on delete...*/
                !selected.equals(FUNCTION_INSTR))
        {
            logger.logComment("Selected: "+ selected);

            if (selected.equals(ADD_SEG_IN_NEW_SEC))
            {
                addInNewSec_actionPerformed(e);

            }
            if (selected.equals(ADD_SEG_IN_THIS_SEC))
            {
                this.addInThisSec_actionPerformed(e);

            }
            else if (selected.equals(SPLIT_SEC))
            {
                this.splitAtCurrentSection();

            }
            else if (selected.equals(SPECIFY_AXONAL_ARBOUR))
            {
                this.specAxonalArbour();

            }
            else if (selected.equals(REMESH_CELL))
            {
                project.markProjectAsEdited();
                this.reMesh();

            }
            else if (selected.equals(COMMENT))
            {
                this.editComment();

            }


            jComboBoxFunctions.setSelectedItem(FUNCTION_INSTR);
        }

    }
}
