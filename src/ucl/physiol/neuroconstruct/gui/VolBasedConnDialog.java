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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

 /**
 * Dialog for new network connection based on xonal arbourisations
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")
public class VolBasedConnDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("VolBasedConnDialog");

    boolean cancelled = false;
    JPanel jPanelMain = new JPanel();

    Vector<SynapticProperties> chosenSynapticPropList = null;

    ToolTipHelper toolTipText = ToolTipHelper.getInstance();


    //int synTargetOption =  ComplexConnectionsInfo.SYN_TARGET_UNIQUE;
    ConnectivityConditions connConds = null;

    boolean newConnMode = true;


    float apSpeed = -1;

    EquationUnit inhomoExp = null;

    Vector<JCheckBox> sourceRegionCheckBoxes = new Vector<JCheckBox>();
    Vector<String> sourceRegions = new Vector<String>();


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


    JLabel jLabelAP = new JLabel();
    JTextField jTextFieldAP = new JTextField();
    JLabel jLabelAPunits = new JLabel();

    JLabel jLabelInh = new JLabel();
    JTextField jTextFieldInh = new JTextField();



    JLabel jLabelCellTypeTrgt = new JLabel();
    JTextField jTextFieldCellTypeTrgt = new JTextField();
    GridBagLayout gridBagLayoutTrgt = new GridBagLayout();
    JPanel jPanelName = new JPanel();
    JPanel jPanelType = new JPanel();
    //JRadioButton jRadioButtonClosest = new JRadioButton();
    //JRadioButton jRadioButtonRandClose = new JRadioButton();
    //JRadioButton jRadioButtonCompRandom = new JRadioButton();
    //ButtonGroup buttonGroupSearchPattern = new ButtonGroup();
    JPanel jPanelTop = new JPanel();
    JLabel jLabelName = new JLabel();
    JLabel jLabelSynapseProperties = new JLabel();
    JTextField jTextFieldName = new JTextField();
    ButtonGroup buttonGroupPreSynDistribution = new ButtonGroup();
    JButton jButtonSynPropsAdd = new JButton();
    JPanel jPanelExtraParams = new JPanel();
    JPanel jPanelSynapseProperties = new JPanel();
   // JPanel jPanelGrowth = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    TitledBorder titledBorder1;
    Border border2;
    TitledBorder titledBorder2;
    Border border3;
    TitledBorder titledBorder3;
    Border border4;
    TitledBorder titledBorder4;

    Border border5;
    TitledBorder titledBorder5;
    JTextField jTextFieldRandCloseNumber = new JTextField();
    JPanel jPanelSourceTarget = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();

    JPanel jPanelSrcArbours = new JPanel();

    JPanel jPanelTgtArbours = new JPanel(); // for the distant future...

    JPanel jPanelAP = new JPanel();
    JPanel jPanelInh = new JPanel();

    Border border8;
    Border border9;
    TitledBorder titledBorder8;
    TitledBorder titledBorder9;





    GridBagLayout gridBagLayout1 = new GridBagLayout();

    Border border6;
    TitledBorder titledBorder6;

    JLabel jLabelAttempts = new JLabel();
    JTextField jTextFieldAttempts = new JTextField();
    JPanel jPanelConnConds = new JPanel();
    Border border7;
    TitledBorder titledBorder7;
    JRadioButton jRadioButtonSrcToTrgt = new JRadioButton();
    JRadioButton jRadioButtonTrgtToSrc = new JRadioButton();
    ButtonGroup buttonGroupSrcTargetDir = new ButtonGroup();
    JPanel jPanelConnCondRadios = new JPanel();
    JPanel jPanelConnCondMaxTarget = new JPanel();
    JLabel jLabelConnCondsNum = new JLabel();
    JPanel jPanelConnCondsNum = new JPanel();



    JTextField jTextFieldConnCondsNum = new JTextField();
    JButton jButtonConnCondsNum = new JButton();

    JCheckBox jCheckBoxConnCondsUnique = new JCheckBox();


    JLabel jLabelConnCondsMaxTarget = new JLabel();
    JTextField jTextFieldConnCondsMaxTarget = new JTextField();


    JPanel jPanelConnCondsMaxTarget = new JPanel();



    GridBagLayout gridBagLayout2 = new GridBagLayout();


    private DefaultListModel listModelSyns = new DefaultListModel();
    
    JList jListSyns = new JList(listModelSyns);

    JScrollPane scrollPaneSyns = new JScrollPane(jListSyns);
    JButton jButtonSynPropsRemove = new JButton();
    JButton jButtonSynPropsEdit = new JButton();
    //JPanel jPanelSynTargetOption = new JPanel();
    Border border99;
    TitledBorder titledBorder99;
    //JRadioButton jRadioButtonSynTargetUnique = new JRadioButton();
    //JRadioButton jRadioButtonSynTargetReuse = new JRadioButton();
    //ButtonGroup buttonGroupSynTargetOption = new ButtonGroup();
    
    
    JPanel jPanelConnCondsAutapses = new JPanel();
    JLabel jLabelConnCondsAutapses = new JLabel();
    JCheckBox jCheckBoxAutapses = new JCheckBox();



    public VolBasedConnDialog(Frame frame, Project project, String proposedName)
    {
        super(frame, "New AxArbConnDialog", false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        newConnMode = true;

        connConds = new ConnectivityConditions();

        Vector synapticTypes =  project.cellMechanismInfo.getAllChemElecSynMechNames();

        chosenSynapticPropList = new Vector<SynapticProperties>();
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


    public VolBasedConnDialog(Frame frame,
                           Project project,
                           String name,
                           String source,
                           String target,
                           Vector<SynapticProperties> synProps,
                           Vector<String> sourceRegions,
                           ConnectivityConditions connConds,
                           float apSpeed,
                           String exp)
    {
        super(frame, "Edit Volume Defined Connection", false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        newConnMode = false;

        chosenSynapticPropList = synProps;

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

           for (JCheckBox cb: sourceRegionCheckBoxes)
           {
               if (sourceRegions.contains(cb.getText()))
               {
                   cb.setSelected(true);
               }
           }


            jTextFieldConnCondsNum.setText(connConds.getNumConnsInitiatingCellGroup().toShortString());
            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                jRadioButtonSrcToTrgt.setSelected(true);
            else
                jRadioButtonTrgtToSrc.setSelected(true);

            jCheckBoxConnCondsUnique.setSelected(connConds.isOnlyConnectToUniqueCells());

            this.jTextFieldConnCondsMaxTarget.setText(connConds.getMaxNumInitPerFinishCellString());


            jTextFieldConnCondsMaxTarget.setColumns(8);


            if (apSpeed==Float.MAX_VALUE)
                jTextFieldAP.setText("MAX");
            else
                jTextFieldAP.setText(apSpeed+"");

            this.jTextFieldInh.setText(exp);
            //this.inhomoExp = exp;
            
            
            jCheckBoxAutapses.setSelected(connConds.isAllowAutapses());


        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private VolBasedConnDialog()
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
        border99 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
        titledBorder99 = new TitledBorder(border99,"Synaptic target options");
        jPanelMain.setLayout(borderLayout1);
        jPanelSourceInfo.setBorder(titledBorder1);
        //jPanelSourceInfo.setMaximumSize(new Dimension(350, 150));
        jPanelSourceInfo.setMinimumSize(new Dimension(350, 170));
        jPanelSourceInfo.setPreferredSize(new Dimension(350, 170));
        jPanelSourceInfo.setLayout(gridBagLayoutSrc);
        jPanelTargetInfo.setLayout(gridBagLayoutTrgt);
        jPanelTargetInfo.setBorder(titledBorder2);
        //jPanelTargetInfo.setMaximumSize(new Dimension(350, 150));
        jPanelTargetInfo.setMinimumSize(new Dimension(350, 170));
        jPanelTargetInfo.setPreferredSize(new Dimension(350, 170));
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


        border8 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
        titledBorder8 = new TitledBorder(border8,"Action potential speed");

        border9 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
        titledBorder9 = new TitledBorder(border9,"Connectivity probability");


        jTextFieldAP.setText("MAX");


/*
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
        jRadioButtonCompRandom.setText("Completely random");*/
        //jPanelType.setMaximumSize(new Dimension(450, 35));
        //jPanelType.setMinimumSize(new Dimension(450, 35));
        //jPanelType.setPreferredSize(new Dimension(450, 35));
        jLabelName.setText("Name of new Volume Defined Connection:");
        jLabelSynapseProperties.setText("Synaptic Properties: ");
        jTextFieldName.setText("...");
        jTextFieldName.setColumns(10);
        jPanelName.setMaximumSize(new Dimension(700, 50));
        jPanelName.setMinimumSize(new Dimension(700, 50));
        jPanelName.setPreferredSize(new Dimension(700, 50));
        /*
        jRadioButtonGrowModeJump.setSelected(true);
        jRadioButtonGrowModeJump.setText("Synapses jump across empty space");
        jRadioButtonGrowModeDendGrow.setText("Dendritic sections grow");
        jRadioButtonGrowModeGrowAxon.setText("Axonal sections grow");*/
        jButtonSynPropsAdd.setText("Add");
        jButtonSynPropsAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSynPropsAdd_actionPerformed(e);
            }
        });
        jPanelExtraParams.setLayout(gridBagLayout1);
        //jPanelGrowth.setLayout(borderLayout2);
        //jPanelGrowth.setBorder(titledBorder4);
        jPanelSynapseProperties.setBorder(titledBorder3);
        jPanelExtraParams.setBorder(null);
       ////////////// jTextFieldSynInfo.setEditable(false);
       //////////// jTextFieldSynInfo.setText("");
      /////////////////  jTextFieldSynInfo.setColumns(50);
        ////jPanelSearch.setBorder(titledBorder5);
        jTextFieldRandCloseNumber.setEnabled(false);
        jTextFieldRandCloseNumber.setText("");
        jTextFieldRandCloseNumber.setColumns(5);
        jPanelSourceTarget.setLayout(flowLayout1);
        jPanelSourceTarget.setMaximumSize(new Dimension(715, 180));
        jPanelSourceTarget.setMinimumSize(new Dimension(715, 180));
        jPanelSourceTarget.setPreferredSize(new Dimension(715, 180));
        /*
        jLabelRegionSrc.setVerifyInputWhenFocusTarget(true);
        jLabelRegionSrc.setText("Region:");
        jTextFieldRegionSrc.setEditable(false);
        jTextFieldRegionSrc.setText("...");
        jLabelRegionTgt.setText("Region:");
        jTextFieldRegionTgt.setEditable(false);
        jTextFieldRegionTgt.setText("...");
*/
        /*
        jLabelMax.setRequestFocusEnabled(true);
        jLabelMax.setText("Maximum length:");
        ////jPanelMaxMin.setBorder(titledBorder6);
        jTextFieldMax.setText("MAX_VALUE");
        jTextFieldMax.setColumns(10);
        jLabelMin.setText("Minimum length:");
        jTextFieldMin.setText("0");
        jTextFieldMin.setColumns(10);
         */

        jLabelInh.setText("Expression for relative connection probability:");
        this.jTextFieldInh.setText("1");
        this.jTextFieldInh.setColumns(25);

        this.jLabelAP.setText("Action potential propagation speed across connection: ");
        this.jLabelAPunits.setText(" \u03bcm/ms");
        this.jTextFieldAP.setColumns(6);
        this.jPanelAP.add(jLabelAP);
        this.jPanelAP.add(jTextFieldAP);
        this.jPanelAP.add(jLabelAPunits);

        this.jPanelInh.add(jLabelInh);
        this.jPanelInh.add(jTextFieldInh);

        jPanelInh.setBorder(titledBorder9);



        jPanelAP.setBorder(titledBorder8);

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
        jTextFieldConnCondsNum.setEditable(false);
        jTextFieldConnCondsNum.setColumns(25);
        jButtonConnCondsNum.setText("...");
        jButtonConnCondsNum.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonConnCondsNum_actionPerformed(e);
            }
        });
        jCheckBoxConnCondsUnique.setText("Connections from cells in Source Cell group go to unique cells in Target Cell " +
    "Group");
    this.jLabelConnCondsMaxTarget.setText("Max number of conns from cells in Source Cell Group to each cell in Target Cell Group");
    
    
        jCheckBoxAutapses.setText("Allow autapses (when source Cell Group = target)");
        jPanelConnCondsAutapses.add(jCheckBoxAutapses);


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


        ///jPanelSynTargetOption.setEnabled(false);
        ///jPanelSynTargetOption.setBorder(titledBorder8);
        /*
        jRadioButtonSynTargetUnique.setEnabled(false);
        jRadioButtonSynTargetUnique.setToolTipText("");
        jRadioButtonSynTargetUnique.setSelected(true);
        jRadioButtonSynTargetUnique.setText("Create new postsynaptic mechanism for each conn per segment");
        jRadioButtonSynTargetReuse.setEnabled(false);
        jRadioButtonSynTargetReuse.setText("Use same postsynaptic mechanism for all conns to each segment");

c       */


        jPanelConnCondRadios.add(jRadioButtonSrcToTrgt, null);
        jPanelConnCondRadios.add(jRadioButtonTrgtToSrc, null);


        jPanelConnConds.add(jPanelConnCondRadios, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                                         , GridBagConstraints.CENTER,
                                                                         GridBagConstraints.BOTH, new Insets(0, 93, 0, 94),
                                                                         0, 0));

        jPanelConnConds.add(jPanelConnCondsNum, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                                       , GridBagConstraints.CENTER,
                                                                       GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        jPanelConnCondsMaxTarget.add(this.jLabelConnCondsMaxTarget);
        jPanelConnCondsMaxTarget.add(this.jTextFieldConnCondsMaxTarget);


        jTextFieldConnCondsMaxTarget.setText("MAX     ");


        jPanelConnConds.add(jPanelConnCondsMaxTarget, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.CENTER,
            GridBagConstraints.NONE, new Insets(11, 79, 6, 0), 0, 0));


        jPanelConnConds.add(this.jCheckBoxConnCondsUnique, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, 6, 0), 0, 0));
    
    
        jPanelConnConds.add(jPanelConnCondsAutapses,   new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0, 0), 0, 0));



        jPanelConnCondsNum.add(jLabelConnCondsNum, null);





        jPanelExtraParams.add(jPanelSynapseProperties,     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), -2, 0));



        jPanelExtraParams.add(jPanelAP, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                                                               , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                                               new Insets(0, 0, 0, 0), -2, 0));

        jPanelExtraParams.add(jPanelInh, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                                                                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                                                new Insets(0, 0, 0, 0), -2, 0));

        jPanelExtraParams.add(jPanelConnConds, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                                      , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                                                                      new Insets(0, 0, 0, 0), 0, 0));



        //jPanelGrowth.add(jPanelGrowMode,  BorderLayout.CENTER);

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

        //jPanelSrcArbours.setBackground(Color.red);
        //jPanelTgtArbours.setBackground(Color.blue);

        jPanelSourceInfo.add(jPanelSrcArbours,   new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 12, 12, 0), 0, 0));


    jPanelTargetInfo.add(jPanelTgtArbours,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
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
        jPanelTop.add(jLabelName, null);
        jPanelTop.add(jTextFieldName, null);
        jPanelMain.add(jPanelExtraParams,  BorderLayout.SOUTH);

/*
        buttonGroupSearchPattern.add(jRadioButtonCompRandom);
        buttonGroupSearchPattern.add(jRadioButtonRandClose);
        buttonGroupSearchPattern.add(jRadioButtonClosest);
        jPanelGrowMode.add(jRadioButtonGrowModeJump, null);
        jPanelGrowMode.add(jRadioButtonGrowModeDendGrow, null);
        jPanelGrowMode.add(jRadioButtonGrowModeGrowAxon, null);
        buttonGroupGrowMode.add(jRadioButtonGrowModeJump);
        buttonGroupGrowMode.add(jRadioButtonGrowModeDendGrow);
        buttonGroupGrowMode.add(jRadioButtonGrowModeGrowAxon);*/
        jPanelSynapseProperties.add(jLabelSynapseProperties, null);

       jPanelSynapseProperties.add(scrollPaneSyns, null);

        scrollPaneSyns.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelSynapseProperties.add(jButtonSynPropsAdd, null);
        jPanelSynapseProperties.add(jButtonSynPropsEdit, null);
        /*
        jPanelSearch.add(jRadioButtonCompRandom, null);
        jPanelSearch.add(jRadioButtonClosest, null);
        jPanelSearch.add(jRadioButtonRandClose, null);*/
        ///jPanelSearch.add(jTextFieldRandCloseNumber, null);
        jPanelMain.add(jPanelSourceTarget, BorderLayout.CENTER);
        jPanelSourceTarget.add(jPanelSourceInfo, null);
        jPanelSourceTarget.add(jPanelTargetInfo, null);
        /*
        jPanelSourceInfo.add(jTextFieldRegionSrc,    new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 12), 0, 0));
        jPanelTargetInfo.add(jTextFieldRegionTgt,   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 12), 0, 0));
         */
    /*
        jPanelMaxMin.add(jLabelMax, null);
        jPanelMaxMin.add(jTextFieldMax, null);
        jPanelMaxMin.add(jLabelMin, null);
        jPanelMaxMin.add(jTextFieldMin, null);
        jPanelMaxMin.add(jLabelAttempts, null);
        jPanelMaxMin.add(jTextFieldAttempts, null);
     */
        buttonGroupSrcTargetDir.add(jRadioButtonSrcToTrgt);
        buttonGroupSrcTargetDir.add(jRadioButtonTrgtToSrc);
        jPanelConnCondsNum.add(jTextFieldConnCondsNum, null);
        jPanelConnCondsNum.add(jButtonConnCondsNum, null);
        jPanelSynapseProperties.add(jButtonSynPropsRemove, null);
        /*
        jPanelSynTargetOption.add(jRadioButtonSynTargetReuse, null);
        buttonGroupSynTargetOption.add(jRadioButtonSynTargetUnique);
        buttonGroupSynTargetOption.add(jRadioButtonSynTargetReuse);*/



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

        jButtonSynPropsAdd.setToolTipText(toolTipText.getToolTip("Netconngui Syn Props Button"));


        this.jPanelAP.setToolTipText(toolTipText.getToolTip("Netconngui AP Speed"));
        this.jTextFieldAP.setToolTipText(toolTipText.getToolTip("Netconngui AP Speed"));


        this.jPanelInh.setToolTipText(toolTipText.getToolTip("Netconngui Inhomogenous Conn Prob"));

        jLabelAttempts.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Num Attempts"));
        jTextFieldAttempts.setToolTipText(toolTipText.getToolTip("Netconngui Max Min Num Attempts"));


        this.jTextFieldConnCondsMaxTarget.setToolTipText(toolTipText.getToolTip("Netconngui Max Target"));
        this.jLabelConnCondsMaxTarget.setToolTipText(toolTipText.getToolTip("Netconngui Max Target"));

    }

    //Overridden so we can exit when window is closed
    @Override
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

        if (newConnMode &&
            (project.volBasedConnsInfo.getAllAAConnNames().contains(jTextFieldName.getText()) ||
             project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(jTextFieldName.getText())))
        {
            GuiUtils.showErrorMessage(logger, "That name for a connection has already been used.\nPlease select another one.", null, this);
            return;
        }

        if (listModelSyns.getSize()==0)
        {
            GuiUtils.showErrorMessage(logger, "Please add at least one synaptic process.", null, this);
            return;
        }


        for (JCheckBox cb : sourceRegionCheckBoxes)
        {
            if (cb.isSelected())
                sourceRegions.add(cb.getText());
        }
        if (sourceRegions.size() == 0)
        {
            GuiUtils.showErrorMessage(logger, "There must be at least one axonal arbourisation region specified on the source Cell Group.\n"
                                      +"These can be edited via the Segment Selector dialog when viewing a cell in 3D", null, this);
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


        connConds.setAllowAutapses(jCheckBoxAutapses.isSelected());


        boolean problem = false;


        try
        {
            if (jTextFieldAP.getText().toUpperCase().indexOf("MAX")>=0)
            {
                apSpeed = Float.MAX_VALUE;
            }
            else
            {
                float aps = Float.parseFloat(this.jTextFieldAP.getText());
                if (aps <= 0)
                {
                    problem = true;
                }
                else
                {
                    this.apSpeed = aps;
                }
            }
        }
        catch (NumberFormatException ex)
        {
            problem = true;

        }
        if (problem)
        {
            GuiUtils.showErrorMessage(logger,
                "Please enter a float value (> 0) for the action potential speed", null, this);
            return;
        }



        try
        {
            connConds.setMaxNumInitPerFinishCell(this.jTextFieldConnCondsMaxTarget.getText());
            if (connConds.getMaxNumInitPerFinishCell()<=0) problem = true;

        }
        catch (NumberFormatException ex)
        {
            problem = true;

        }
        if (problem)
        {
            GuiUtils.showErrorMessage(logger, "Please enter an integer (> 0) for number of max number of connections to source cell on each target cell", null, this);
            return;
        }


        if (jComboBoxSource.getSelectedItem().equals(sourceComboString))
        {
            GuiUtils.showErrorMessage(logger, "Please select the cell group which is the source of the Complex connection", null, this);
            return;
        }

        if (jComboBoxTarget.getSelectedItem().equals(targetComboString))
        {
            GuiUtils.showErrorMessage(logger, "Please select the cell group which is the target of the Complex connection", null, this);
            return;
        }

        String expInhomog = this.jTextFieldInh.getText();

        try
        {
            inhomoExp = Expression.parseExpression(expInhomog, VolumeBasedConnGenerator.allowedVars);

            logger.logComment("Equation evaluated as: " + inhomoExp.getNiceString());
        }
        catch (EquationException ex1)
        {
            String allowedVarExp = new String();
            for (int i = 0; i < VolumeBasedConnGenerator.allowedVars.length; i++)
            {
                if (i > 0) allowedVarExp = allowedVarExp + ", ";
                allowedVarExp = allowedVarExp + VolumeBasedConnGenerator.allowedVars[i];
            }
            GuiUtils.showErrorMessage(logger,
                                      "Error parsing: " + expInhomog +
                "\nPlease enter an expression for the relative probability of connection just in terms of: (" +
                                      allowedVarExp + ")", ex1, this);
            return;
        }

        chosenSynapticPropList.removeAllElements();
        for (int i = 0; i < listModelSyns.getSize(); i++)
        {
            chosenSynapticPropList.add((SynapticProperties)listModelSyns.getElementAt(i));
        }


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
            //jTextFieldRegionSrc.setText("");

            jComboBoxTarget.setSelectedItem(targetComboString);
            jTextFieldCellTypeTrgt.setText("");
            //jTextFieldRegionTgt.setText("");
            return;
        }
        String sourceCG = (String)e.getItem();
        logger.logComment("New Cell group selected: "+sourceCG);

        String cellType = project.cellGroupsInfo.getCellType(sourceCG);
        jTextFieldCellTypeSrc.setText(cellType);

        //String region = project.cellGroupsInfo.getRegionName(sourceCG);
        //jTextFieldRegionSrc.setText(region);

        Cell cell = project.cellManager.getCell(cellType);

        Vector<AxonalConnRegion> aas  = cell.getAxonalArbours();

        jPanelSrcArbours.removeAll();
        sourceRegionCheckBoxes.removeAllElements();

        JComponent toAddTo = jPanelSrcArbours;
        
        if (aas.size()>2)
        {
            //JPanel jPanelInner = new JPanel();
            //JList list = new JList();
//            JScrollPane scrollPane = new JScrollPane(list); 
//            jPanelSrcArbours.add(scrollPane);
//                
//                
//            jPanelSrcArbours.setPreferredSize(d);
//            jPanelSrcArbours.setMinimumSize(d);
            
            JPanel innerPanel = new JPanel(); 
            innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS)); 
            //innerPanel.add("goo..."); 
  
  
            JPanel innerList = new JPanel() { 
                Insets insets = new Insets(0, 4, 0, 0); 
                @Override
                public Insets getInsets() { 
                    return insets; 
                } 
            }; 
            innerList.setLayout(new BoxLayout(innerList, BoxLayout.Y_AXIS)); 
            JScrollPane scrollPane = new JScrollPane(innerList); 
            
            scrollPane.getVerticalScrollBar().setUnitIncrement(10); 
            innerPanel.add(scrollPane);  
            Dimension r = new Dimension(50,30);
            //innerPanel.add(Box.createRigidArea(r)); 
            Dimension d1 = new Dimension(250,40);
            Dimension d2 = new Dimension(250,36);
            jPanelSrcArbours.setPreferredSize(d1);
            jPanelSrcArbours.setMinimumSize(d1);
            jPanelSrcArbours.add(innerPanel);
            innerPanel.setPreferredSize(d2);
            innerPanel.setMinimumSize(d2);
            
            
            //jPanelSrcArbours.setBackground(Color.yellow);
            //innerPanel.setBackground(Color.red);
            //innerList.setBackground(Color.blue);
            
            
            toAddTo = innerList;
        }
        for (AxonalConnRegion aa: aas)
        {
            JCheckBox cb = new JCheckBox(aa.getName());
            toAddTo.add(cb);
            sourceRegionCheckBoxes.add(cb);
        }



        ArrayList<String> names = project.cellGroupsInfo.getAllCellGroupNames();
        jComboBoxTarget.removeAllItems();
        jComboBoxTarget.addItem(targetComboString);
        for (int i = 0; i < names.size(); i++)
        {
            ////////if (!names.get(i).equals(sourceCG))
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
            //jTextFieldRegionTgt.setText("");
            return;
        }
        String targetCG = (String)e.getItem();
        logger.logComment("New Cell group selected: "+targetCG);

        String cellType = project.cellGroupsInfo.getCellType(targetCG);
        jTextFieldCellTypeTrgt.setText(cellType);

        //String region = project.cellGroupsInfo.getRegionName(targetCG);
        //jTextFieldRegionTgt.setText(region);


    }

    public float getAPSpeed()
    {
        return apSpeed;
    }


    public EquationUnit getInhomogenousExp()
    {
        return this.inhomoExp;
    }


    public String getSourceCellGroup()
    {
        return (String)jComboBoxSource.getSelectedItem();
    }

    public Vector<String> getSourceRegions()
    {
        return sourceRegions;
    }


    public String getTargetCellGroup()
    {
        return (String)jComboBoxTarget.getSelectedItem();
    }




    public Vector<SynapticProperties> getSynapticProperties()
    {
        return chosenSynapticPropList;
    }

    public String getAAConnName()
    {
        return jTextFieldName.getText();
    }

/*
    public GrowMode getGrowMode()
    {
        return growMode;
    }*/


    public ConnectivityConditions getConnectivityConditions()
    {
        return connConds;
    }




    void jButtonSynPropsAdd_actionPerformed(ActionEvent e)
    {

        Vector synapticTypes =  project.cellMechanismInfo.getAllChemElecSynMechNames();
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
/*
    void jRadioButtonRandClose_itemStateChanged(ItemEvent e)
    {
        jTextFieldRandCloseNumber.setEnabled(jRadioButtonRandClose.isSelected());
        if (jTextFieldRandCloseNumber.getText().length()==0) jTextFieldRandCloseNumber.setText("10");
    }*/

    void jRadioButtonSrcToTrgt_itemStateChanged(ItemEvent e)
    {
        if (jRadioButtonSrcToTrgt.isSelected())
        {
            jLabelConnCondsNum.setText(
                  "Number of connections from each cell in Source Cell group:");

            jCheckBoxConnCondsUnique.setText(
                "Connections from cells in Source Cell group go to unique cells in Target Cell Group");

           this.jLabelConnCondsMaxTarget.setText("Max number of connections from cells in Source Cell Group to each cell in Target Cell " +
    "Group");
        }
        else
        {
            jLabelConnCondsNum.setText(
                  "Number of connections from each cell in Target Cell group:");

            jCheckBoxConnCondsUnique.setText(
                 "Connections from cells in Target Cell group go to unique cells in Source Cell Group");

              this.jLabelConnCondsMaxTarget.setText("Max number of connections from cells in Target Cell Group to each cell in Source Cell " +
    "Group");

        }
    }

    void jButtonConnCondsNum_actionPerformed(ActionEvent e)
    {
        NumberGeneratorDialog dlg = new NumberGeneratorDialog((Frame)null, "Connectivity", "Connectivity", connConds.getNumConnsInitiatingCellGroup());

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


    public static void main(String[] args)
    {

        try
        {
            Project p = Project.loadProject(new File("../nC_projects/Project_1ax/Project_1ax.neuro.xml"), null);

            VolBasedConnDialog dlg = new VolBasedConnDialog(null, p, "Jimbo");

            GuiUtils.centreWindow(dlg);

            dlg.pack();
            dlg.setVisible(true);
        }
        catch (ProjectFileParsingException ex)
        {
            ex.printStackTrace();
        }

    }


}
