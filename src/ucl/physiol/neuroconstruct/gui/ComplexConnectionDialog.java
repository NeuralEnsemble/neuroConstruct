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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

 /**
 * Dialog for new Complex network connection
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class ComplexConnectionDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("ComplexConnectionDialog");

    boolean cancelled = false;
    JPanel jPanelMain = new JPanel();

    //SynapticProperties chosenSynapticProperties = null;
    Vector chosenSynapticPropList = null;

    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    SearchPattern searchPattern = null;

    int synTargetOption =  ComplexConnectionsInfo.SYN_TARGET_UNIQUE;

    boolean newConnMode = true;

    /**
     * Needed only where searchPattern = RANDOM_CLOSE. It's the number of random
     *  points on the target cell group to pick before selecting the nearest one...
     */
    int randomCloseNumber = 1;

    GrowMode growMode;
    MaxMinLength maxMin = null;
    ConnectivityConditions connConds = null;

    String chosenSynapseType = null;

    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelOKCancel = new JPanel();
    JPanel jPanelSourceInfo = new JPanel();
    JPanel jPanelTargetInfo = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JComboBox jComboBoxSource = new JComboBox();
    JComboBox jComboBoxTarget = new JComboBox();

    Project project = null;

    String sourceComboString = "Please select a source Cell Group";
    String targetComboString = "Please select a target Cell Group";

    JLabel jLabelCellTypeSrc = new JLabel();
    JTextField jTextFieldCellTypeSrc = new JTextField();
    GridBagLayout gridBagLayoutSrc = new GridBagLayout();



    JLabel jLabelCellTypeTrgt = new JLabel();
    JTextField jTextFieldCellTypeTrgt = new JTextField();
    GridBagLayout gridBagLayoutTrgt = new GridBagLayout();
    JPanel jPanelName = new JPanel();
    JPanel jPanelType = new JPanel();
    JRadioButton jRadioButtonClosest = new JRadioButton();
    JRadioButton jRadioButtonRandClose = new JRadioButton();
    JRadioButton jRadioButtonCompRandom = new JRadioButton();
    ButtonGroup buttonGroupSearchPattern = new ButtonGroup();
    JPanel jPanelTop = new JPanel();
    JLabel jLabelName = new JLabel();
    JLabel jLabelSynapseProperties = new JLabel();
    JTextField jTextFieldName = new JTextField();
    ButtonGroup buttonGroupPreSynDistribution = new ButtonGroup();
    JPanel jPanelGrowMode = new JPanel();
    JRadioButton jRadioButtonGrowModeJump = new JRadioButton();
    JRadioButton jRadioButtonGrowModeDendGrow = new JRadioButton();
    JRadioButton jRadioButtonGrowModeGrowAxon = new JRadioButton();
    ButtonGroup buttonGroupGrowMode = new ButtonGroup();
    JButton jButtonSynPropsAdd = new JButton();
    JPanel jPanelExtraParams = new JPanel();
    JPanel jPanelSynapseProperties = new JPanel();
    JPanel jPanelGrowth = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    TitledBorder titledBorder1;
    Border border2;
    TitledBorder titledBorder2;
    Border border3;
    TitledBorder titledBorder3;
    Border border4;
    TitledBorder titledBorder4;
   // JTextField jTextFieldSynInfo = new JTextField();
    JPanel jPanelSearch = new JPanel();
    Border border5;
    TitledBorder titledBorder5;
    JTextField jTextFieldRandCloseNumber = new JTextField();
    JPanel jPanelSourceTarget = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JLabel jLabelRegionSrc = new JLabel();
    JTextField jTextFieldRegionSrc = new JTextField();
    JLabel jLabelRegionTgt = new JLabel();
    JTextField jTextFieldRegionTgt = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanelMaxMin = new JPanel();
    JLabel jLabelMax = new JLabel();
    Border border6;
    TitledBorder titledBorder6;
    JTextField jTextFieldMax = new JTextField();
    JLabel jLabelMin = new JLabel();
    JTextField jTextFieldMin = new JTextField();
    JLabel jLabelAttempts = new JLabel();
    JTextField jTextFieldAttempts = new JTextField();
    JPanel jPanelConnConds = new JPanel();
    Border border7;
    TitledBorder titledBorder7;
    JRadioButton jRadioButtonSrcToTrgt = new JRadioButton();
    JRadioButton jRadioButtonTrgtToSrc = new JRadioButton();
    ButtonGroup buttonGroupSrcTargetDir = new ButtonGroup();
    JPanel jPanelConnCondRadios = new JPanel();
    JLabel jLabelConnCondsNum = new JLabel();
    JPanel jPanelConnCondsNum = new JPanel();
    JTextField jTextFieldConnCondsNum = new JTextField();
    JButton jButtonConnCondsNum = new JButton();
    //JCheckBox jCheckBoxConnCondsUnique = new JCheckBox();


    JLabel jLabelConnCondsMaxTarget = new JLabel();
    JTextField jTextFieldConnCondsMaxTarget = new JTextField();

    GridBagLayout gridBagLayout2 = new GridBagLayout();


    private DefaultListModel listModelSyns = new DefaultListModel();
    JList jListSyns = new JList(listModelSyns);

    JScrollPane scrollPaneSyns = new JScrollPane(jListSyns);
    JButton jButtonSynPropsRemove = new JButton();
    JButton jButtonSynPropsEdit = new JButton();
    JPanel jPanelSynTargetOption = new JPanel();
    Border border8;
    TitledBorder titledBorder8;
    JRadioButton jRadioButtonSynTargetUnique = new JRadioButton();
    JRadioButton jRadioButtonSynTargetReuse = new JRadioButton();
    ButtonGroup buttonGroupSynTargetOption = new ButtonGroup();



    public ComplexConnectionDialog(Frame frame, Project project, String proposedName)
    {
        super(frame, "New Complex Connection", false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        newConnMode = true;

        connConds = new ConnectivityConditions();

        File neuronModDir = ProjectStructure.getModTemplatesDir();


        Vector synapticTypes =  project.cellMechanismInfo.getAllSynMechNames();
        // set it to the first inbuilt synapse type...


        //    chosenSynapticProperties
        //        = new SynapticProperties((String)synapticTypes.firstElement());

        chosenSynapticPropList = new Vector();
        chosenSynapticPropList.add(new SynapticProperties((String)synapticTypes.firstElement()));

        if (synapticTypes.size()>1)
        {
            chosenSynapticPropList.add(new SynapticProperties((String)synapticTypes.elementAt(1)));
        }

        this.project = project;
        try
        {
            jbInit();
            extraInit(proposedName);
            addToolTips();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }
    }


    public ComplexConnectionDialog(Frame frame,
                                  Project project,
                                  String name,
                                  String source,
                                  String target,
                                  Vector synPropList,
                                  int synTargetOption,
                                  SearchPattern searchPattern,
                                  GrowMode growMode,
                                  MaxMinLength maxMin,
                                  ConnectivityConditions connConds)
    {
        super(frame, "Edit Complex Connection", false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        newConnMode = false;

        chosenSynapticPropList = synPropList;

        this.maxMin = maxMin;
        this.connConds = connConds;

        this.project = project;
        try
        {
            jbInit();
            //extraInit(proposedName);
            addToolTips();

            jTextFieldName.setText(name);
            jTextFieldName.setEditable(false);
            jTextFieldName.setEnabled(false);

            jComboBoxSource.addItem(sourceComboString);
            jComboBoxTarget.addItem(targetComboString);

            ArrayList<String> names = project.cellGroupsInfo.getAllCellGroupNames();

            for (int i = 0; i < names.size(); i++)
            {
                jComboBoxSource.addItem(names.get(i));
            }
            jComboBoxSource.setSelectedItem(source);

            jComboBoxTarget.setSelectedItem(target);

           ///////////////////// jTextFieldSynInfo.setText(chosenSynapticPropList.toString());

           for (int i = 0; i < chosenSynapticPropList.size(); i++)
           {
                   listModelSyns.addElement(chosenSynapticPropList.elementAt(i));
           }

            if (searchPattern.type==searchPattern.CLOSEST)
            {
                jRadioButtonClosest.setSelected(true);
            }
            else if (searchPattern.type==searchPattern.RANDOM_CLOSE)
            {
                jRadioButtonRandClose.setSelected(true);
                jTextFieldRandCloseNumber.setText(searchPattern.randomCloseNumber+"");
            }
            else jRadioButtonCompRandom.setSelected(true);

       /*     if (growMode.type == GrowMode.GROW_MODE_DEND_GROW)
                jRadioButtonGrowModeDendGrow.setSelected(true);
            else if (growMode.type == GrowMode.GROW_MODE_AXON_GROW)
                jRadioButtonGrowModeGrowAxon.setSelected(true);
            else*/
                jRadioButtonGrowModeJump.setSelected(true);


            if (synTargetOption==ComplexConnectionsInfo.SYN_TARGET_UNIQUE)
            {
                jRadioButtonSynTargetUnique.setSelected(true);
            }
            else if (synTargetOption==ComplexConnectionsInfo.SYN_TARGET_REUSE)
            {
                jRadioButtonSynTargetReuse.setSelected(true);
            }


            this.jTextFieldMax.setText(maxMin.getMaxLength()+"");
            this.jTextFieldMin.setText(maxMin.getMinLength()+"");
            this.jTextFieldAttempts.setText(maxMin.getNumberAttempts()+"");


            jTextFieldConnCondsNum.setText(connConds.getNumConnsInitiatingCellGroup().toShortString());
            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                jRadioButtonSrcToTrgt.setSelected(true);
            else
                jRadioButtonTrgtToSrc.setSelected(true);

            //jCheckBoxConnCondsUnique.setSelected(connConds.isOnlyConnectToUniqueCells());
            this.jTextFieldConnCondsMaxTarget.setText(connConds.getMaxNumInitPerFinishCellString());

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private ComplexConnectionDialog()
    {
    }


    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(border1,"Source Cell Group");
        border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder2 = new TitledBorder(border2,"Target Cell Group");
        border3 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder3 = new TitledBorder(border3,"Synaptic Properties");
        border4 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder4 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Growth of Axons and Dendrites");
        border5 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder5 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Method of searching for connection point");
        border6 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
        titledBorder6 = new TitledBorder(border6,"Maximum and minimum lengths");
        border7 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
        titledBorder7 = new TitledBorder(border7,"Connection Conditions");
        border8 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
        titledBorder8 = new TitledBorder(border8,"Synaptic target options");
        jPanelMain.setLayout(borderLayout1);
        jPanelSourceInfo.setBorder(titledBorder1);
        jPanelSourceInfo.setMaximumSize(new Dimension(350, 150));
        jPanelSourceInfo.setMinimumSize(new Dimension(350, 150));
        jPanelSourceInfo.setPreferredSize(new Dimension(350, 150));
        jPanelSourceInfo.setLayout(gridBagLayoutSrc);
        jPanelTargetInfo.setLayout(gridBagLayoutTrgt);
        jPanelTargetInfo.setBorder(titledBorder2);
        jPanelTargetInfo.setMaximumSize(new Dimension(350, 150));
        jPanelTargetInfo.setMinimumSize(new Dimension(350, 150));
        jPanelTargetInfo.setPreferredSize(new Dimension(350, 150));
        jComboBoxTarget.setEnabled(false);
        jComboBoxTarget.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxTarget_itemStateChanged(e);
            }
        });

        jComboBoxSource.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxSource_itemStateChanged(e);
            }
        });
        jLabelCellTypeSrc.setText("Cell Type:");
        jTextFieldCellTypeSrc.setEditable(false);
        jTextFieldCellTypeSrc.setText("...");


        jLabelCellTypeTrgt.setText("Cell Type");
        jTextFieldCellTypeTrgt.setEditable(false);
        jTextFieldCellTypeTrgt.setText("...");


        jRadioButtonClosest.setText("Closest");
        jRadioButtonRandClose.setText("Random but close. Number to try:");
        jRadioButtonRandClose.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jRadioButtonRandClose_itemStateChanged(e);
            }
        });

        jRadioButtonCompRandom.setSelected(true);
        jRadioButtonCompRandom.setText("Completely random");
        //jPanelType.setMaximumSize(new Dimension(450, 35));
        //jPanelType.setMinimumSize(new Dimension(450, 35));
        //jPanelType.setPreferredSize(new Dimension(450, 35));
        jLabelName.setText("Name of new Complex connection:");
        jLabelSynapseProperties.setText("Synaptic Properties: ");
        jTextFieldName.setText("...");
        jTextFieldName.setColumns(10);
        jPanelName.setMaximumSize(new Dimension(700, 50));
        jPanelName.setMinimumSize(new Dimension(700, 50));
        jPanelName.setPreferredSize(new Dimension(700, 50));
        jRadioButtonGrowModeJump.setSelected(true);
        jRadioButtonGrowModeJump.setText("Synapses jump across empty space");
        jRadioButtonGrowModeDendGrow.setText("Dendritic sections grow");
        jRadioButtonGrowModeGrowAxon.setText("Axonal sections grow");
        jButtonSynPropsAdd.setText("Add");
        jButtonSynPropsAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSynPropsAdd_actionPerformed(e);
            }
        });
        jPanelExtraParams.setLayout(gridBagLayout1);
        jPanelGrowth.setLayout(borderLayout2);
        jPanelGrowth.setBorder(titledBorder4);
        jPanelSynapseProperties.setBorder(titledBorder3);
        jPanelExtraParams.setBorder(null);
       ////////////// jTextFieldSynInfo.setEditable(false);
       //////////// jTextFieldSynInfo.setText("");
      /////////////////  jTextFieldSynInfo.setColumns(50);
        jPanelSearch.setBorder(titledBorder5);
        jTextFieldRandCloseNumber.setEnabled(false);
        jTextFieldRandCloseNumber.setText("");
        jTextFieldRandCloseNumber.setColumns(5);
        jPanelSourceTarget.setLayout(flowLayout1);
        jPanelSourceTarget.setMaximumSize(new Dimension(715, 160));
        jPanelSourceTarget.setMinimumSize(new Dimension(715, 160));
        jPanelSourceTarget.setPreferredSize(new Dimension(715, 160));
        jLabelRegionSrc.setVerifyInputWhenFocusTarget(true);
        jLabelRegionSrc.setText("Region:");
        jTextFieldRegionSrc.setEditable(false);
        jTextFieldRegionSrc.setText("...");
        jLabelRegionTgt.setText("Region:");
        jTextFieldRegionTgt.setEditable(false);
        jTextFieldRegionTgt.setText("...");
        jLabelMax.setRequestFocusEnabled(true);
        jLabelMax.setText("Maximum length:");
        jPanelMaxMin.setBorder(titledBorder6);
        jTextFieldMax.setText("MAX_VALUE");
        jTextFieldMax.setColumns(10);
        jLabelMin.setText("Minimum length:");
        jTextFieldMin.setText("0");
        jTextFieldMin.setColumns(10);
        jLabelAttempts.setText("Number of attempts:");
        jTextFieldAttempts.setText("100");
        jTextFieldAttempts.setColumns(5);
        jPanelConnConds.setBorder(titledBorder7);
        jPanelConnConds.setLayout(gridBagLayout2);
        jRadioButtonSrcToTrgt.setSelected(true);
        jRadioButtonSrcToTrgt.setText("Generate from Source Cell Group to Target");
        jRadioButtonSrcToTrgt.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jRadioButtonSrcToTrgt_itemStateChanged(e);
            }
        });
        jRadioButtonTrgtToSrc.setText("Generate from Target Cell Group To Source");
        jLabelConnCondsNum.setText("Number of connections from each cell in Source Cell group:");
        jTextFieldConnCondsNum.setText("2");
        jTextFieldConnCondsNum.setColumns(25);
        jButtonConnCondsNum.setText("...");
        jButtonConnCondsNum.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonConnCondsNum_actionPerformed(e);
            }
        });

        //jCheckBoxConnCondsUnique.setText("Connections from cells in Source Cell group go to unique cells in Target Cell " +
    //"Group");
    this.jLabelConnCondsMaxTarget.setText("Max number of connections from cells in Source Cell Group to each cell in Target Cell " +
    "Group");

        scrollPaneSyns.setMinimumSize(new Dimension(400, 60));
        scrollPaneSyns.setPreferredSize(new Dimension(400, 60));

        jButtonSynPropsRemove.setText("Remove");
        jButtonSynPropsRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSynPropsRemove_actionPerformed(e);
            }
        });
        jButtonSynPropsEdit.setText("Edit");
        jButtonSynPropsEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSynPropsEdit_actionPerformed(e);
            }
        });


        jPanelSynTargetOption.setEnabled(false);
        jPanelSynTargetOption.setBorder(titledBorder8);
        jRadioButtonSynTargetUnique.setEnabled(false);
        jRadioButtonSynTargetUnique.setToolTipText("");
        jRadioButtonSynTargetUnique.setSelected(true);
        jRadioButtonSynTargetUnique.setText("Create new postsynaptic mechanism for each conn per segment");
        jRadioButtonSynTargetReuse.setEnabled(false);
        jRadioButtonSynTargetReuse.setText("Use same postsynaptic mechanism for all conns to each segment");


        jPanelConnConds.add(this.jLabelConnCondsMaxTarget,   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(11, 79, 6, 0), 0, 0));

        jPanelConnConds.add(this.jTextFieldConnCondsMaxTarget,   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(11, 79, 6, 0), 0, 0));




        jPanelConnConds.add(jPanelConnCondsNum,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanelConnCondsNum.add(jLabelConnCondsNum, null);
        jPanelConnConds.add(jPanelConnCondRadios,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 93, 0, 94), 0, 0));
        jPanelConnCondRadios.add(jRadioButtonSrcToTrgt, null);
        jPanelConnCondRadios.add(jRadioButtonTrgtToSrc, null);




        jPanelExtraParams.add(jPanelSynapseProperties,     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), -2, 0));

        jPanelExtraParams.add(jPanelSynTargetOption,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanelSynTargetOption.add(jRadioButtonSynTargetUnique, null);

        jPanelExtraParams.add(jPanelGrowth,     new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 200, 0));

        jPanelExtraParams.add(jPanelSearch,     new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 241, 0));

        jPanelExtraParams.add(jPanelMaxMin,     new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        jPanelExtraParams.add(jPanelConnConds,      new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));



        jPanelGrowth.add(jPanelGrowMode,  BorderLayout.CENTER);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);


        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });

        jPanelOKCancel.add(jButtonOK);
        jPanelOKCancel.add(jButtonCancel);


        jPanelSourceInfo.add(jComboBoxSource,                    new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 6, 0), 0, 0));
        jPanelSourceInfo.add(jLabelCellTypeSrc,                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 12, 12, 0), 0, 0));
        jPanelSourceInfo.add(jTextFieldCellTypeSrc,                   new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 6, 12), 0, 0));
        jPanelSourceInfo.add(jLabelRegionSrc,   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 12, 12, 0), 0, 0));



        jPanelTargetInfo.add(jComboBoxTarget,    new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 6, 0), 0, 0));
        jPanelTargetInfo.add(jTextFieldCellTypeTrgt,    new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 6, 12), 0, 0));
        jPanelTargetInfo.add(jLabelCellTypeTrgt,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 12, 0, 0), 0, 0));


        jPanelMain.add(jPanelName,  BorderLayout.NORTH);
        this.getContentPane().add(jPanelOKCancel, BorderLayout.SOUTH);
        jPanelName.add(jPanelTop, null);
        jPanelName.add(jPanelType, null);
        buttonGroupSearchPattern.add(jRadioButtonCompRandom);
        buttonGroupSearchPattern.add(jRadioButtonRandClose);
        buttonGroupSearchPattern.add(jRadioButtonClosest);
        jPanelTop.add(jLabelName, null);
        jPanelTop.add(jTextFieldName, null);
        jPanelGrowMode.add(jRadioButtonGrowModeJump, null);
        jPanelGrowMode.add(jRadioButtonGrowModeDendGrow, null);
        jPanelGrowMode.add(jRadioButtonGrowModeGrowAxon, null);
        jPanelMain.add(jPanelExtraParams,  BorderLayout.SOUTH);
        buttonGroupGrowMode.add(jRadioButtonGrowModeJump);
        buttonGroupGrowMode.add(jRadioButtonGrowModeDendGrow);
        buttonGroupGrowMode.add(jRadioButtonGrowModeGrowAxon);
        jPanelSynapseProperties.add(jLabelSynapseProperties, null);

       jPanelSynapseProperties.add(scrollPaneSyns, null);

        scrollPaneSyns.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelSynapseProperties.add(jButtonSynPropsAdd, null);
        jPanelSynapseProperties.add(jButtonSynPropsEdit, null);
        jPanelSearch.add(jRadioButtonCompRandom, null);
        jPanelSearch.add(jRadioButtonClosest, null);
        jPanelSearch.add(jRadioButtonRandClose, null);
        jPanelSearch.add(jTextFieldRandCloseNumber, null);
        jPanelMain.add(jPanelSourceTarget, BorderLayout.CENTER);
        jPanelSourceTarget.add(jPanelSourceInfo, null);
        jPanelSourceTarget.add(jPanelTargetInfo, null);
        jPanelSourceInfo.add(jTextFieldRegionSrc,    new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 12), 0, 0));
        jPanelTargetInfo.add(jLabelRegionTgt,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 12, 12, 0), 0, 0));
        jPanelTargetInfo.add(jTextFieldRegionTgt,   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 12), 0, 0));
        jPanelMaxMin.add(jLabelMax, null);
        jPanelMaxMin.add(jTextFieldMax, null);
        jPanelMaxMin.add(jLabelMin, null);
        jPanelMaxMin.add(jTextFieldMin, null);
        jPanelMaxMin.add(jLabelAttempts, null);
        jPanelMaxMin.add(jTextFieldAttempts, null);
        buttonGroupSrcTargetDir.add(jRadioButtonSrcToTrgt);
        buttonGroupSrcTargetDir.add(jRadioButtonTrgtToSrc);
        jPanelConnCondsNum.add(jTextFieldConnCondsNum, null);
        jPanelConnCondsNum.add(jButtonConnCondsNum, null);
        jPanelSynapseProperties.add(jButtonSynPropsRemove, null);
        jPanelSynTargetOption.add(jRadioButtonSynTargetReuse, null);
        buttonGroupSynTargetOption.add(jRadioButtonSynTargetUnique);
        buttonGroupSynTargetOption.add(jRadioButtonSynTargetReuse);



    }

    private void extraInit(String proposedName)
    {
        jTextFieldName.setText(proposedName);

        jComboBoxSource.addItem(sourceComboString);
        jComboBoxTarget.addItem(targetComboString);

        ArrayList<String> names = project.cellGroupsInfo.getAllCellGroupNames();

        for (int i = 0; i < names.size(); i++)
        {
            jComboBoxSource.addItem(names.get(i));
        }

      /////////////////  jTextFieldSynInfo.setText(chosenSynapticPropList.toString());


      for (int i = 0; i < chosenSynapticPropList.size(); i++)
      {
          listModelSyns.addElement(chosenSynapticPropList.elementAt(i));
      }


        jTextFieldConnCondsNum.setText(connConds.getNumConnsInitiatingCellGroup().toShortString());

        //conn
    }

    private void addToolTips()
    {
        jTextFieldName.setToolTipText(toolTipText.getToolTip("Netconngui Name"));
        jLabelName.setToolTipText(toolTipText.getToolTip("Netconngui Name"));
        jPanelSourceInfo.setToolTipText(toolTipText.getToolTip("Netconngui Source Cell Group"));
        jComboBoxSource.setToolTipText(toolTipText.getToolTip("Netconngui Source Cell Group"));
        jPanelTargetInfo.setToolTipText(toolTipText.getToolTip("Netconngui Target Cell Group"));
        jComboBoxTarget.setToolTipText(toolTipText.getToolTip("Netconngui Target Cell Group"));

        jPanelSynapseProperties.setToolTipText(toolTipText.getToolTip("Netconngui Syn Props"));
       /////////////////// jTextFieldSynInfo.setToolTipText(toolTipText.getToolTip("Netconngui Syn Props"));
        jButtonSynPropsAdd.setToolTipText(toolTipText.getToolTip("Netconngui Syn Props Button"));

        jRadioButtonSynTargetReuse.setToolTipText(toolTipText.getToolTip("Netconngui Reuse Syn Target"));
        jRadioButtonSynTargetUnique.setToolTipText(toolTipText.getToolTip("Netconngui Unique Syn Target"));

        jPanelGrowth.setToolTipText(toolTipText.getToolTip("Netconngui Growth"));
        jRadioButtonGrowModeJump.setToolTipText(toolTipText.getToolTip("Netconngui Growth Jump"));
        jRadioButtonGrowModeDendGrow.setToolTipText(toolTipText.getToolTip("Netconngui Growth Dend"));
        jRadioButtonGrowModeGrowAxon.setToolTipText(toolTipText.getToolTip("Netconngui Growth Axon"));

        jPanelSearch.setToolTipText(toolTipText.getToolTip("Netconngui Search"));
        jRadioButtonCompRandom.setToolTipText(toolTipText.getToolTip("Netconngui Search Random"));
        jRadioButtonRandClose.setToolTipText(toolTipText.getToolTip("Netconngui Search Random Close"));
        jRadioButtonClosest.setToolTipText(toolTipText.getToolTip("Netconngui Search Closest"));

        jPanelMaxMin.setToolTipText(toolTipText.getToolTip("Netconngui Max Min"));
        jLabelMax.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Max"));
        jTextFieldMax.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Max"));
        jLabelMin.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Min"));
        jTextFieldMin.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Min"));
        jLabelAttempts.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Num Attempts"));
        jTextFieldAttempts.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Num Attempts"));
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            doCancel();
        }
        super.processWindowEvent(e);
    }
    //Close the dialog


    void doCancel()
    {
        logger.logComment("Actions for cancel...");

        cancelled = true;
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK pressed...");
/*
        if (newConnMode &&
            (project.complexConnectionsInfo.getAllComplexConnNames().contains(jTextFieldName.getText()) ||
             project.simpleNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(jTextFieldName.getText())))
        {
            GuiUtils.showErrorMessage(logger, "That name for a Complex Connection has already been used.\nPlease select another one.", null, this);
            return;
        }

        if (listModelSyns.getSize()==0)
        {
            GuiUtils.showErrorMessage(logger, "Please add at least one synaptic process.", null, this);
            return;
        }


        try
        {
            String maxString = jTextFieldMax.getText();
            if (maxString.toUpperCase().startsWith("MAX")) maxString = Float.MAX_VALUE+"";

            maxMin = new MaxMinLength(Float.parseFloat(maxString),
                                      Float.parseFloat(jTextFieldMin.getText()),
                                      Integer.parseInt(jTextFieldAttempts.getText()));
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please insert a correct number into the fields for max and min lengths, and number of attempts", null, this);
            return;
        }

        if (jRadioButtonClosest.isSelected()
            && maxMin.maxLength!=Float.MAX_VALUE
            && maxMin.maxLength!=0f)
        {
            GuiUtils.showErrorMessage(logger, "Note that max/min cannot be used when the search option \"Closest\" is selected.\nPlease set Max = MAX_VALUE and Min = 0 in this case.", null, this);
            return;
        }

        if (jRadioButtonSrcToTrgt.isSelected())
        {
            connConds.setGenerationDirection(ConnectivityConditions.SOURCE_TO_TARGET);
        }
        else
        {
            connConds.setGenerationDirection(ConnectivityConditions.TARGET_TO_SOURCE);
        }
        connConds.setOnlyConnectToUniqueCells(jCheckBoxConnCondsUnique.isSelected());


        if (jRadioButtonSynTargetReuse.isSelected())
        {
            synTargetOption = ComplexConnectionsInfo.SYN_TARGET_REUSE;
        }
        else if (jRadioButtonSynTargetUnique.isSelected())
        {
            synTargetOption = ComplexConnectionsInfo.SYN_TARGET_UNIQUE;
        }



        if (jComboBoxSource.getSelectedItem()==sourceComboString)
        {
            GuiUtils.showErrorMessage(logger, "Please select the cell group which is the source of the Complex connection", null, this);
            return;
        }

        if (jComboBoxTarget.getSelectedItem()==targetComboString)
        {
            GuiUtils.showErrorMessage(logger, "Please select the cell group which is the target of the Complex connection", null, this);
            return;
        }



        if (jRadioButtonClosest.isSelected())
        {
            searchPattern = SearchPattern.getClosestSearchPattern();
        }
        else if (jRadioButtonRandClose.isSelected())
        {
            try
            {
                randomCloseNumber = Integer.parseInt(jTextFieldRandCloseNumber.getText());
            }
            catch(NumberFormatException exx)
            {
                GuiUtils.showErrorMessage(logger, "Please insert a correct number into the field for average number of presynaptic connections", exx, this);
            return;
            }
            searchPattern = SearchPattern.getRandomCloseSearchPattern(randomCloseNumber);
        }
        else
        {
            searchPattern = SearchPattern.getRandomSearchPattern();
        }


        if (jRadioButtonGrowModeGrowAxon.isSelected()) growMode = GrowMode.getGrowModeAxonsGrow();
        else if (jRadioButtonGrowModeDendGrow.isSelected()) growMode = GrowMode.getGrowModeDendsGrow();
        else growMode = GrowMode.getGrowModeJump();

        chosenSynapticPropList.removeAllElements();
        for (int i = 0; i < listModelSyns.getSize(); i++)
        {
            chosenSynapticPropList.add(listModelSyns.getElementAt(i));
        }

*/
        this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }

    void jComboBoxSource_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() != ItemEvent.SELECTED) return;

        if (e.getItem().equals(sourceComboString))
        {
            jComboBoxTarget.setEnabled(false);
            jTextFieldCellTypeSrc.setText("");
            jTextFieldRegionSrc.setText("");

            jComboBoxTarget.setSelectedItem(targetComboString);
            jTextFieldCellTypeTrgt.setText("");
            jTextFieldRegionTgt.setText("");
            return;
        }
        String sourceCG = (String)e.getItem();
        logger.logComment("New Cell group selected: "+sourceCG);

        String cellType = project.cellGroupsInfo.getCellType(sourceCG);
        jTextFieldCellTypeSrc.setText(cellType);

        String region = project.cellGroupsInfo.getRegionName(sourceCG);
        jTextFieldRegionSrc.setText(region);



        ArrayList<String> names = project.cellGroupsInfo.getAllCellGroupNames();
        jComboBoxTarget.removeAllItems();
        jComboBoxTarget.addItem(targetComboString);
        for (int i = 0; i < names.size(); i++)
        {
            if (!names.get(i).equals(sourceCG))
                jComboBoxTarget.addItem(names.get(i));
        }
        jComboBoxTarget.setEnabled(true);


    }



    void jComboBoxTarget_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() != ItemEvent.SELECTED) return;

        if (e.getItem().equals(targetComboString))
        {
            jTextFieldCellTypeTrgt.setText("");
            jTextFieldRegionTgt.setText("");
            return;
        }
        String targetCG = (String)e.getItem();
        logger.logComment("New Cell group selected: "+targetCG);

        String cellType = project.cellGroupsInfo.getCellType(targetCG);
        jTextFieldCellTypeTrgt.setText(cellType);

        String region = project.cellGroupsInfo.getRegionName(targetCG);
        jTextFieldRegionTgt.setText(region);


    }


    public String getSourceCellGroup()
    {
        return (String)jComboBoxSource.getSelectedItem();
    }


    public String getTargetCellGroup()
    {
        return (String)jComboBoxTarget.getSelectedItem();
    }

    public SearchPattern getSearchPattern()
    {
        return searchPattern;
    }

    public MaxMinLength getMaxMinLength()
    {
        return maxMin;
    }


    public int getRandomCloseNumber()
    {
        return randomCloseNumber;
    }

    public int getSynTargetOption()
    {
        return synTargetOption;
    }



    public Vector getSynapticProperties()
    {

        return chosenSynapticPropList;
    }

    public String getComplexConnName()
    {
        return jTextFieldName.getText();
    }


    public GrowMode getGrowMode()
    {
        return growMode;
    }


    public ConnectivityConditions getConnectivityConditions()
    {
        return connConds;
    }




    void jButtonSynPropsAdd_actionPerformed(ActionEvent e)
    {

        Vector synapticTypes =  project.cellMechanismInfo.getAllSynMechNames();
        SynapticProperties newSynProps = new SynapticProperties((String)synapticTypes.firstElement());

        SynapticPropertiesDialog dlg = new SynapticPropertiesDialog(this, newSynProps,
                                               project);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);


        dlg.pack();
        dlg.setVisible(true);

        listModelSyns.addElement(dlg.getFinalSynProps());

    }

    void jRadioButtonRandClose_itemStateChanged(ItemEvent e)
    {
        jTextFieldRandCloseNumber.setEnabled(jRadioButtonRandClose.isSelected());
        if (jTextFieldRandCloseNumber.getText().length()==0) jTextFieldRandCloseNumber.setText("10");
    }

    void jRadioButtonSrcToTrgt_itemStateChanged(ItemEvent e)
    {
        if (jRadioButtonSrcToTrgt.isSelected())
        {
            jLabelConnCondsNum.setText(
                  "Number of connections from each cell in Source Cell group:");

            //jCheckBoxConnCondsUnique.setText(
              //    "Connections from cells in Source Cell group go to unique cells in Target Cell Group");
              this.jLabelConnCondsMaxTarget.setText("Max number of connections from cells in Source Cell Group to each cell in Target Cell " +
    "Group");
        }
        else
        {
            jLabelConnCondsNum.setText(
                  "Number of connections from each cell in Target Cell group:");

            //jCheckBoxConnCondsUnique.setText(
            //      "Connections from cells in Target Cell group go to unique cells in Source Cell Group");
            this.jLabelConnCondsMaxTarget.setText("Max number of connections from cells in Target Cell Group to each cell in Source Cell " +
    "Group");

        }
    }

    void jButtonConnCondsNum_actionPerformed(ActionEvent e)
    {
        NumberGeneratorDialog dlg = new NumberGeneratorDialog((Frame)null, "Please select", "Please select",
                                                              connConds.getNumConnsInitiatingCellGroup());

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

        jTextFieldConnCondsNum.setText(connConds.getNumConnsInitiatingCellGroup().toShortString());

    }

    void jButtonSynPropsRemove_actionPerformed(ActionEvent e)
    {

        listModelSyns.removeElementAt(jListSyns.getSelectedIndex());

    }


    void jButtonSynPropsEdit_actionPerformed(ActionEvent e)
    {

        SynapticProperties selected = (SynapticProperties)listModelSyns.getElementAt(jListSyns.getSelectedIndex());

        SynapticPropertiesDialog dlg = new SynapticPropertiesDialog(this, selected,
                                               project);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);


        dlg.pack();
        dlg.setVisible(true);

    }


}
