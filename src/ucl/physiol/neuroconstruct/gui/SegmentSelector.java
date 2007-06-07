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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.util.*;
import javax.vecmath.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog for selecting and viewing properties of segments
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class SegmentSelector extends JFrame
{
    ClassLogger logger = new ClassLogger("SegmentSelector");
    Project project = null;

    boolean editable = false;

    boolean cancelled = false;

    //when the 3D view is updated and this must respond to changes there
    boolean updatingFrom3DView = false;

    boolean initialising = false;

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

    Vector segmentsForDeletion = new Vector();

    static final int SECTIONS_SHOWN = 0;
    static final int SEGMENTS_SHOWN = 1;

    int shownFields = SEGMENTS_SHOWN;

    private static final String FUNCTION_INSTR = "--  Select one of these functions:  --";
    private static final String ADD_SEG_IN_NEW_SEC = "Add segment in new section";
    private static final String ADD_SEG_IN_THIS_SEC = "Add segment in this section";
    private static final String SPLIT_SEC = "Split section here";
    private static final String SPEC_AXONAL_ARBOUR = "Specify axonal arbour";
    private static final String COMMENT = "Edit Segment comment";

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
    /*
    public SegmentSelector(Dialog dlg,
                           Project project,
                           Cell cell,
                           boolean editable,
                           boolean modal)
    {
        super(dlg, "Segment Selector", modal);

        myCell = cell;
        this.editable = editable;

        this.project = project;

        try
        {
            initialising = true;
            jbInit();
            extraInit();
            pack();

            initialising = false;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }
*/
    public SegmentSelector(Frame frame,
                           Project project,
                           Cell cell,
                           boolean editable)
    {
        super("Segment Selector");

        //this.set

        myCell = cell;
        this.editable = editable;
        this.project = project;

        try
        {
            jbInit();
            extraInit();
            pack();
            refresh();
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
        Dimension dim = new Dimension(410,600);
        this.setPreferredSize(dim);
        this.setMinimumSize(dim);

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
        jTextFieldEndPoint.setText("...");
        jTextFieldParent.setText("...");

        jTextFieldID.setEditable(false);
        jTextFieldParent.setEditable(false);
        jTextFieldNameSecInSegView.setEditable(false);
        jTextFieldStartPoint.setEditable(false);

        jLabelFractAlong.setVerifyInputWhenFocusTarget(true);
        jLabelFractAlong.setText("Fraction along parent:");
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
        jButtonDelete.setText("Delete");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelete_actionPerformed(e);
            }
        }); jButtonCheckValid.setToolTipText("");
        jButtonCheckValid.setText("Cell validity");
        jButtonCheckValid.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCheckValid_actionPerformed(e);
            }
        }); jLabelSectionName.setText("Section name:");
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
        jTextFieldStartSecRadius.setColumns(12); jButtonBiophys.setText("Biophys"); jButtonBiophys.addActionListener(new
            ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBiophys_actionPerformed(e);
            }
        }); jComboBoxFunctions.addActionListener(new ActionListener()
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
        jPanelOptions.add(jButtonDelete, null);
        jPanelOptions.add(jButtonUpdate, null); jPanelOptions.add(jButtonBiophys);
        jPanelOptions.add(jButtonCheckValid, null);
        panelButtons.add(jPanelFunctions, BorderLayout.NORTH); jPanelFunctions.add(jComboBoxFunctions); this.getContentPane().add(jPanelinfo, BorderLayout.NORTH);
        jPanelinfo.add(jComboBoxSection,    new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));
        jPanelinfo.add(jComboBoxSegment,  new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0)); jComboBoxSection.addItem(defaultSectionItem);
        jComboBoxSegment.addItem(defaultSegmentItem);

        jComboBoxFiniteVolume.addItem(finiteVolYes);
        jComboBoxFiniteVolume.addItem(finiteVolNo);

        //////jComboBoxShape.addItem(sphericalShape);
      /////////  jComboBoxShape.addItem(cylindricalShape);

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
        this.jComboBoxFunctions.addItem(this.FUNCTION_INSTR);
        this.jComboBoxFunctions.addItem(ADD_SEG_IN_NEW_SEC);
        this.jComboBoxFunctions.addItem(ADD_SEG_IN_THIS_SEC);
        /////////////this.jComboBoxFunctions.addItem(this.SPLIT_SEC);
        this.jComboBoxFunctions.addItem(this.SPEC_AXONAL_ARBOUR);
        this.jComboBoxFunctions.addItem(this.COMMENT);


    }


    private void addSegementFields()
    {
        shownFields = SEGMENTS_SHOWN;
        logger.logComment("Setting shown fields to: SEGMENTS_SHOWN");

        jPanelParameters.removeAll();

        jPanelParameters.setLayout(gridBagLayout2);
        jPanelParameters.add(jLabelName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(6, 14, 6, 60), 0, 0));
        jPanelParameters.add(jTextFieldSegmentsName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                                                                    , GridBagConstraints.WEST,
                                                                    GridBagConstraints.HORIZONTAL,
                                                                    new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jLabelID, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                              , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                              new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldID, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                                                                  , GridBagConstraints.EAST,
                                                                  GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 14),
                                                                  130, 0));
        jPanelParameters.add(jLabelSection, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                                   , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                   new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldNameSecInSegView, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                                                                       , GridBagConstraints.WEST,
                                                                       GridBagConstraints.HORIZONTAL,
                                                                       new Insets(6, 0, 6, 14), 184, 0));
        jPanelParameters.add(jLabelStartPoint, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.NONE, new Insets(6, 14, 6, 0),
                                                                      0, 0));
        jPanelParameters.add(jTextFieldStartPoint, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                                                                          , GridBagConstraints.WEST,
                                                                          GridBagConstraints.HORIZONTAL,
                                                                          new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jLabelEndPoint, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                                    , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                    new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldEndPoint, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0
                                                                        , GridBagConstraints.WEST,
                                                                        GridBagConstraints.HORIZONTAL,
                                                                        new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jTextFieldParent, new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.HORIZONTAL,
                                                                      new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jLabelFractAlong, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.NONE, new Insets(6, 14, 0, 0),
                                                                      0, 0));
        jPanelParameters.add(jLabelStartRadius, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                                       , GridBagConstraints.WEST,
                                                                       GridBagConstraints.NONE, new Insets(6, 14, 6, 0),
                                                                       0, 0));
        jPanelParameters.add(jTextFieldStartRadius, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
                                                                           , GridBagConstraints.WEST,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jLabelEndRadius, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                     new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jTextFieldEndRadius, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
                                                                         , GridBagConstraints.WEST,
                                                                         GridBagConstraints.HORIZONTAL,
                                                                         new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jLabelParent, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
                                                                  , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                  new Insets(6, 14, 6, 0), 0, 0));
  /*      jPanelParameters.add(jLabelShape, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(6, 14, 6, 0), 0, 0));*/
        jPanelParameters.add(jTextFieldFractAlong, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
                                                                          , GridBagConstraints.WEST,
                                                                          GridBagConstraints.HORIZONTAL,
                                                                          new Insets(6, 0, 6, 14), 0, 0));
        jPanelParameters.add(jLabelFiniteVol, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                     new Insets(6, 14, 6, 0), 0, 0));
        jPanelParameters.add(jComboBoxFiniteVolume, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0
                                                                           , GridBagConstraints.CENTER,
                                                                           GridBagConstraints.HORIZONTAL,
                                                                           new Insets(6, 0, 6, 14), 0, 0));
       // jPanelParameters.add(jComboBoxShape, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0
       //                                                             , GridBagConstraints.WEST,
       //                                                             GridBagConstraints.HORIZONTAL,
       //                                                             new Insets(6, 0, 6, 14), 0, 0));

        jPanelParameters.repaint();


    }


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



    //public void

    void updateCellWithEnteredInfo()
    {
        if (initialising) return;

        if (editable && currentlyAddressedSegment!=null && shownFields==SEGMENTS_SHOWN)
        {
            logger.logComment(">>>>>>>>>>>>  Updating the segment info...");
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
            currentlyAddressedSegment.setEndPointPositionX(newEndPoint.x);
            currentlyAddressedSegment.setEndPointPositionY(newEndPoint.y);
            currentlyAddressedSegment.setEndPointPositionZ(newEndPoint.z);


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
                //currentlyAddressedSegment.setstRadius(Float.parseFloat(jTextFieldEndRadius.getText()));
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
            /*
            if (jComboBoxShape.getSelectedItem().equals(sphericalShape))
                currentlyAddressedSegment.setShape(Segment.SPHERICAL_SHAPE);
            else if (jComboBoxShape.getSelectedItem().equals(cylindricalShape))
                currentlyAddressedSegment.setShape(Segment.CYLINDRICAL_SHAPE);
             */
            currentlyAddressedSegment.setSegmentName(jTextFieldSegmentsName.getText());

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

            //ComplexCell p = new ComplexCell("comp");
            //MossyFiber p = new MossyFiber("MossyFiber");

            Project testProj = Project.loadProject(new File("projects/Simple/Simple.neuro.xml"),
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

            SegmentSelector dlg = new SegmentSelector(new Frame(),
                                                      testProj,
                                                      testProj.cellManager.getCell("CellType_1"),
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

            //dlg.setSelectedSegment((Segment)p.getAllSegments().elementAt(1));

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

        if (e.getStateChange()==e.SELECTED)
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
                    jComboBoxSegment.addItem(((Segment)allSegmentHere.get(i)).getSegmentName());
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

        if (e.getStateChange() == e.SELECTED)
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

        Vector allSegments = myCell.getAllSegments();
        ArrayList<Section> allSections = myCell.getAllSections();

        for (int i = 0; i < segmentsForDeletion.size(); i++)
        {

            Segment nextSeg = (Segment) segmentsForDeletion.elementAt(i);
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

    void jButtonCheckValid_actionPerformed(ActionEvent e)
    {
        updateCellWithEnteredInfo();

        String cellValidity = CellTopologyHelper.getValidityStatus(myCell).getValidity();
        GuiUtils.showInfoMessage(logger, "Cell validity status", cellValidity, this);
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
            newSegment = myCell.addDendriticSegment(currentlyAddressedSegment.getRadius(),
                                       newSegmentName,
                                       newEndPoint,
                                       currentlyAddressedSegment,
                                       1,
                                       newSectionName);
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
                                       currentlyAddressedSegment.getSection().getSectionName());
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


        String requestSeg = "Please enter the name of the new Section which will be created after the split.\n"
                           +"All child Segments after the currently accessed Segment will be part of the new Section";

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

/*
        Segment first = myCell.getFirstSomaSegment();


        RegionsInfoDialog dlg = new RegionsInfoDialog(this,
                                                      suggestedRegion,
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

        myCell.addAxonalArbour(acr);
 */

    }

    public void jComboBoxFunctions_actionPerformed(ActionEvent e)
    {
        String selected = (String)this.jComboBoxFunctions.getSelectedItem();


        if (selected !=null && /* Might be null on delete...*/
            selected != this.FUNCTION_INSTR)
        {
            logger.logComment("Selected: "+ selected);

            if (selected.equals(this.ADD_SEG_IN_NEW_SEC))
            {
                 addInNewSec_actionPerformed(e);

             }
             if (selected.equals(this.ADD_SEG_IN_THIS_SEC))
             {
                  this.addInThisSec_actionPerformed(e);

              }
              else if (selected.equals(this.SPLIT_SEC))
              {
                   this.splitAtCurrentSection();

               }
               else if (selected.equals(this.SPEC_AXONAL_ARBOUR))
               {
                    this.specAxonalArbour();

                }
                else if (selected.equals(this.COMMENT))
                {
                     this.editComment();

                }




              jComboBoxFunctions.setSelectedItem(FUNCTION_INSTR);
        }

    }
}
