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
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;

 /**
 * Dialog for new simple network connection
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")
public class NetworkConnectionDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("NetworkConnectionDialog");

    boolean cancelled = false;
    JPanel jPanelMain = new JPanel();

    //SynapticProperties chosenSynapticProperties = null;

    Vector<SynapticProperties> chosenSynapticPropList = null;

    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    SearchPattern searchPattern = null;

    boolean newNetConnMode = true;

    String initiallySuggestedName = null;

    /**
     * Needed only where searchPattern = RANDOM_CLOSE. It's the number of random
     *  points on the target cell group to pick before selecting the nearest one...
     */
    int randomCloseNumber = 1;

    GrowMode growMode;
    MaxMinLength maxMin = null;
    float apSpeed = -1;
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


    JLabel jLabelAP = new JLabel();
    JTextField jTextFieldAP = new JTextField();
    JLabel jLabelAPunits = new JLabel();


    JTextField jTextFieldName = new JTextField();
    ButtonGroup buttonGroupPreSynDistribution = new ButtonGroup();
    JPanel jPanelGrowMode = new JPanel();
    //JRadioButton jRadioButtonGrowModeJump = new JRadioButton();
    //JRadioButton jRadioButtonGrowModeDendGrow = new JRadioButton();
    //JRadioButton jRadioButtonGrowModeGrowAxon = new JRadioButton();
    //ButtonGroup buttonGroupGrowMode = new ButtonGroup();
    //JButton jButtonSynProps = new JButton();
    JPanel jPanelExtraParams = new JPanel();
    JPanel jPanelSynapseProperties = new JPanel();
    //JPanel jPanelGrowth = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    Border border1;
    TitledBorder titledBorder1;
    Border border2;
    TitledBorder titledBorder2;
    Border border3;
    TitledBorder titledBorder3;
    Border border4;
    TitledBorder titledBorder4;
    //JTextField jTextFieldSynInfo = new JTextField();
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
    JLabel jLabelDim = new JLabel();
    Border border6;
    TitledBorder titledBorder6;
    JTextField jTextFieldMax = new JTextField();
    JLabel jLabelMin = new JLabel();
    JTextField jTextFieldMin = new JTextField();
    JTextField jTextFieldDim = new JTextField();
    JLabel jLabelAttempts = new JLabel();
    JTextField jTextFieldAttempts = new JTextField();
    JPanel jPanelConnConds = new JPanel();

    JPanel jPanelAP = new JPanel();

    Border border7;
    TitledBorder titledBorder7;
    Border border8;
    TitledBorder titledBorder8;


    JRadioButton jRadioButtonSrcToTrgt = new JRadioButton();
    JRadioButton jRadioButtonTrgtToSrc = new JRadioButton();
    ButtonGroup buttonGroupSrcTargetDir = new ButtonGroup();
    JPanel jPanelConnCondRadios = new JPanel();
    JLabel jLabelConnCondsNum = new JLabel();
    JPanel jPanelConnCondsNum = new JPanel();
    JTextField jTextFieldConnCondsNum = new JTextField();
    JButton jButtonConnCondsNum = new JButton();
    JCheckBox jCheckBoxConnCondsUnique = new JCheckBox();


    JPanel jPanelConnCondsMaxTarget = new JPanel();
    
    JPanel jPanelConnCondsAutapses = new JPanel();
    JPanel jPanelConnCondsRecurrent = new JPanel();
    

    JLabel jLabelConnCondsMaxTarget = new JLabel();
    JTextField jTextFieldConnCondsMaxTarget = new JTextField();
    
    
    JLabel jLabelConnCondsAutapses = new JLabel();
    JCheckBox jCheckBoxAutapses = new JCheckBox();      
    JCheckBox jCheckBoxRecurrent = new JCheckBox();
    
    
    JPanel jPanelConnCondsPrePost = new JPanel();
    JCheckBox jCheckBoxSomaPre = new JCheckBox();
    JCheckBox jCheckBoxDendPre = new JCheckBox();
    JCheckBox jCheckBoxAxonPre = new JCheckBox();
    
    JLabel jLabelConnCondsPre = new JLabel();
    JCheckBox jCheckBoxSomaPost = new JCheckBox();
    JCheckBox jCheckBoxDendPost = new JCheckBox();
    JCheckBox jCheckBoxAxonPost = new JCheckBox();
    
    
    JLabel jLabelConnCondsPost = new JLabel();

    JCheckBox jCheckBoxSomaToSoma = new JCheckBox();

    GridBagLayout gridBagLayout2 = new GridBagLayout();



    private DefaultListModel listModelSyns = new DefaultListModel();
    JList jListSyns = new JList(listModelSyns);

    JScrollPane scrollPaneSyns = new JScrollPane(jListSyns);

    JButton jButtonSynPropsAdd = new JButton();
    JButton jButtonSynPropsRemove = new JButton();
    JButton jButtonSynPropsEdit = new JButton();



    public NetworkConnectionDialog(Frame frame, Project project, String proposedName)
    {

        super(frame, "New Network Connection", false);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        Vector synapticTypes =  project.cellMechanismInfo.getAllChemElecSynMechNames();

        initiallySuggestedName = proposedName;

        newNetConnMode = true;

        connConds = new ConnectivityConditions();


        //chosenSynapticProperties
        //    = new SynapticProperties((String)synapticTypes.firstElement());


        chosenSynapticPropList = new Vector<SynapticProperties>();
        chosenSynapticPropList.add(new SynapticProperties((String)synapticTypes.firstElement()));


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


    public NetworkConnectionDialog(Frame frame,
                                  Project project,
                                  String name,
                                  String source,
                                  String target,
                                  /*SynapticProperties synProps,*/
                                  Vector<SynapticProperties> synPropList,
                                  SearchPattern searchPattern,
                                  /*GrowMode growMode,*/
                                  MaxMinLength maxMin,
                                  ConnectivityConditions connConds,
                                  float apSpeed)
    {
        super(frame, "Edit Network Connection", false);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        newNetConnMode = false;

        //chosenSynapticProperties = synProps;
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

            //jTextFieldSynInfo.setText(chosenSynapticProperties.toString());


            for (int i = 0; i < chosenSynapticPropList.size(); i++)
            {
                    listModelSyns.addElement(chosenSynapticPropList.elementAt(i));
            }


            if (searchPattern.type==SearchPattern.CLOSEST)
            {
                jRadioButtonClosest.setSelected(true);
            }
            else if (searchPattern.type==SearchPattern.RANDOM_CLOSE)
            {
                jRadioButtonRandClose.setSelected(true);
                jTextFieldRandCloseNumber.setText(searchPattern.randomCloseNumber+"");
            }
            else jRadioButtonCompRandom.setSelected(true);
/*
            if (growMode.type == GrowMode.GROW_MODE_DEND_GROW)
                jRadioButtonGrowModeDendGrow.setSelected(true);
            else if (growMode.type == GrowMode.GROW_MODE_AXON_GROW)
                jRadioButtonGrowModeGrowAxon.setSelected(true);
            else
                jRadioButtonGrowModeJump.setSelected(true);*/


            this.jTextFieldMin.setText(maxMin.getMinLength()+"");
            String max = maxMin.getMaxLength()+"";
            if (maxMin.getMaxLength()==Float.MAX_VALUE) max = "MAX";
            this.jTextFieldMax.setText(max);
            this.jTextFieldDim.setText(maxMin.getDimension()+"");
            this.jTextFieldAttempts.setText(maxMin.getNumberAttempts()+"");


            jTextFieldConnCondsNum.setText(connConds.getNumConnsInitiatingCellGroup().toShortString());

            if (connConds.getGenerationDirection() == ConnectivityConditions.SOURCE_TO_TARGET)
                jRadioButtonSrcToTrgt.setSelected(true);
            else
                jRadioButtonTrgtToSrc.setSelected(true);

            jCheckBoxConnCondsUnique.setSelected(connConds.isOnlyConnectToUniqueCells());
            
            jCheckBoxAutapses.setSelected(connConds.isAllowAutapses());
            jCheckBoxRecurrent.setSelected(connConds.isNoRecurrent());
            
            jCheckBoxSomaPre.setSelected(connConds.getPrePostAllowedLoc().isSomaAllowedPre());
            jCheckBoxAxonPre.setSelected(connConds.getPrePostAllowedLoc().isAxonsAllowedPre());
            jCheckBoxDendPre.setSelected(connConds.getPrePostAllowedLoc().isDendritesAllowedPre());
            
            
            jCheckBoxSomaPost.setSelected(connConds.getPrePostAllowedLoc().isSomaAllowedPost());
            jCheckBoxAxonPost.setSelected(connConds.getPrePostAllowedLoc().isAxonsAllowedPost());
            jCheckBoxDendPost.setSelected(connConds.getPrePostAllowedLoc().isDendritesAllowedPost());
            
            this.jTextFieldConnCondsMaxTarget.setText(connConds.getMaxNumInitPerFinishCellString());

            jTextFieldConnCondsMaxTarget.setColumns(8);
            String speed = apSpeed+"";
            if (apSpeed == Float.MAX_VALUE) speed = "MAX";
            this.jTextFieldAP.setText(speed);

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private NetworkConnectionDialog()
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
        titledBorder8 = new TitledBorder(border8,"Action potential speed");

        jPanelMain.setLayout(borderLayout1);
        jPanelSourceInfo.setBorder(titledBorder1);
        Dimension d = new Dimension(350, 160);
        
        jPanelSourceInfo.setMaximumSize(d);
        jPanelSourceInfo.setMinimumSize(d);
        jPanelSourceInfo.setPreferredSize(d);
        jPanelSourceInfo.setLayout(gridBagLayoutSrc);
        jPanelTargetInfo.setLayout(gridBagLayoutTrgt);
        jPanelTargetInfo.setBorder(titledBorder2);
        jPanelTargetInfo.setMaximumSize(d);
        jPanelTargetInfo.setMinimumSize(d);
        jPanelTargetInfo.setPreferredSize(d);
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


        jTextFieldAP.setText("MAX");

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
        jLabelName.setText("Name of new network connection:");
        jLabelSynapseProperties.setText("Synaptic Properties: ");
        jTextFieldName.setText("...");
        jTextFieldName.setColumns(10);
        jPanelName.setMaximumSize(new Dimension(700, 50));
        jPanelName.setMinimumSize(new Dimension(700, 50));
        jPanelName.setPreferredSize(new Dimension(700, 50));
 
        jPanelExtraParams.setLayout(gridBagLayout1);
        //jPanelGrowth.setLayout(borderLayout2);
        //jPanelGrowth.setBorder(titledBorder4);
        jPanelSynapseProperties.setBorder(titledBorder3);
        jPanelExtraParams.setBorder(null);
        /*
        jTextFieldSynInfo.setEditable(false);
        jTextFieldSynInfo.setText("");
        jTextFieldSynInfo.setColumns(50);
*/
        jPanelSearch.setBorder(titledBorder5);
        jTextFieldRandCloseNumber.setEnabled(false);
        jTextFieldRandCloseNumber.setText("");
        jTextFieldRandCloseNumber.setColumns(5);
        jPanelSourceTarget.setLayout(flowLayout1);
        jPanelSourceTarget.setMaximumSize(new Dimension(715, 210));
        jPanelSourceTarget.setMinimumSize(new Dimension(715, 210));
        jLabelRegionSrc.setVerifyInputWhenFocusTarget(true);
        jLabelRegionSrc.setText("Region:");
        jTextFieldRegionSrc.setEditable(false);
        jTextFieldRegionSrc.setText("...");
        jLabelRegionTgt.setText("Region:");
        jTextFieldRegionTgt.setEditable(false);
        jTextFieldRegionTgt.setText("...");
        jLabelMax.setRequestFocusEnabled(true);
        jLabelMax.setText("Maximum length:");
        jLabelDim.setText("Dimension:");
        jPanelMaxMin.setBorder(titledBorder6);
        jTextFieldMax.setText("MAX");
        jTextFieldMax.setColumns(8);
        jTextFieldDim.setText(MaxMinLength.RADIAL);
        jTextFieldDim.setColumns(4);


        this.jLabelAP.setText("Action potential propagation speed across connection: ");
        this.jLabelAPunits.setText(" \u03bcm/ms");
        this.jTextFieldAP.setColumns(6);
        this.jPanelAP.add(jLabelAP);
        this.jPanelAP.add(jTextFieldAP);
        this.jPanelAP.add(jLabelAPunits);


        jLabelMin.setText("Minimum length:");
        jTextFieldMin.setText("0");
        jTextFieldMin.setColumns(8);
        jLabelAttempts.setText("Number of attempts:");
        jTextFieldAttempts.setText("100");
        jTextFieldAttempts.setColumns(5);

        jPanelAP.setBorder(titledBorder8);

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
        jTextFieldConnCondsNum.setEditable(false);
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
        jCheckBoxConnCondsUnique.setText("Connections from cells in Source Cell group go to unique cells in Target Cell " +
    "Group");

    this.jLabelConnCondsMaxTarget.setText("Max number of conns from cells in Source Cell Group to each cell in Target Cell Group");

        //



        jTextFieldConnCondsMaxTarget.setText("MAX     ");

        jTextFieldConnCondsMaxTarget.setMinimumSize(new Dimension(100, 32));

        
        
        jPanelConnConds.setBorder(titledBorder7);
        jPanelConnConds.setLayout(gridBagLayout2);
        
        
        jPanelConnConds.add(jPanelConnCondRadios,    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0, 0), 0, 0));
        
        jPanelConnCondRadios.add(jRadioButtonSrcToTrgt, null);
        jPanelConnCondRadios.add(jRadioButtonTrgtToSrc, null);
        
        
        jPanelConnConds.add(jPanelConnCondsNum,   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        
        jPanelConnCondsNum.add(jLabelConnCondsNum, null);
        
        
        jPanelConnConds.add(jPanelConnCondsMaxTarget,   
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                    ,GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0,0,0, 0), 0, 0));
        
        jPanelConnCondsMaxTarget.add(jLabelConnCondsMaxTarget);
        jPanelConnCondsMaxTarget.add(jTextFieldConnCondsMaxTarget);
        

        jPanelConnConds.add(jCheckBoxConnCondsUnique,   new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0, 0), 0, 0));

        
        //jLabelConnCondsAutapses.setText("Allow autapses (when source group = target)");
        jCheckBoxAutapses.setText("Allow autapses (only if target = source)");
        jPanelConnCondsAutapses.add(jCheckBoxAutapses);
        jCheckBoxRecurrent.setText("Direct recurrent connections not allowed (only if target = source)");
        jPanelConnCondsRecurrent.add(jCheckBoxRecurrent);
        
        jPanelConnConds.add(jPanelConnCondsAutapses,   new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0, 0), 0, 0));
        jPanelConnConds.add(jPanelConnCondsRecurrent,   new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0, 0), 0, 0));
        
        
        if (jComboBoxSource.getSelectedItem()==jComboBoxTarget.getSelectedItem())// && !(jComboBoxTarget.getSelectedItem().equals(targetComboString)))
        {
            jCheckBoxAutapses.setEnabled(true);
            jCheckBoxRecurrent.setEnabled(true);
        }
        else
        {
            jCheckBoxAutapses.setEnabled(false);
            jCheckBoxRecurrent.setEnabled(false);
        }
        
        
        
        jLabelConnCondsPre.setText("Allowed presynaptically:");
        
        jLabelConnCondsPost.setText("Allowed postsynaptically:");
        jPanelConnCondsPrePost.add(jLabelConnCondsPre);
        jCheckBoxSomaPre.setText("Soma");
        jCheckBoxSomaPre.setSelected(true);
        jPanelConnCondsPrePost.add(jCheckBoxSomaPre);
        jCheckBoxAxonPre.setText("Axons");
        jCheckBoxAxonPre.setSelected(true);
        
        jPanelConnCondsPrePost.add(jCheckBoxAxonPre);
        jCheckBoxDendPre.setText("Dendrites");
        
        jCheckBoxDendPre.setSelected(false);
        
        jPanelConnCondsPrePost.add(jCheckBoxDendPre);
        jPanelConnCondsPrePost.add(jLabelConnCondsPost);
        jPanelConnCondsPrePost.add(jCheckBoxSomaPost);
        jCheckBoxSomaPost.setText("Soma");
        jCheckBoxSomaPost.setSelected(true);
        
        jPanelConnCondsPrePost.add(jCheckBoxAxonPost);
        jCheckBoxAxonPost.setText("Axons");
        jCheckBoxAxonPost.setSelected(false);
        
        jPanelConnCondsPrePost.add(jCheckBoxDendPost);
        jCheckBoxDendPost.setText("Dendrites");
        jCheckBoxDendPost.setSelected(true);
        
        jPanelConnConds.add(jPanelConnCondsPrePost,   new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0, 0), 0, 0));
        
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


        jPanelSourceInfo.add(jComboBoxSource,                   new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 12, 0), 0, 0));
        jPanelSourceInfo.add(jLabelCellTypeSrc,                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 12, 12, 0), 0, 0));
        jPanelSourceInfo.add(jTextFieldCellTypeSrc,                  new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 12), 0, 0));
        jPanelSourceInfo.add(jLabelRegionSrc,   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 12, 12, 0), 0, 0));



        jPanelTargetInfo.add(jComboBoxTarget,   new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 0, 12, 0), 0, 0));
        jPanelTargetInfo.add(jTextFieldCellTypeTrgt,   new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 12), 0, 0));
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
        jPanelMain.add(jPanelExtraParams,  BorderLayout.SOUTH);
        /*
        jPanelGrowMode.add(jRadioButtonGrowModeJump, null);
        jPanelGrowMode.add(jRadioButtonGrowModeDendGrow, null);
        jPanelGrowMode.add(jRadioButtonGrowModeGrowAxon, null);
        buttonGroupGrowMode.add(jRadioButtonGrowModeJump);
        buttonGroupGrowMode.add(jRadioButtonGrowModeDendGrow);
        buttonGroupGrowMode.add(jRadioButtonGrowModeGrowAxon);*/

        jPanelExtraParams.add(jPanelSynapseProperties,    new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), -2, 0));


        jPanelExtraParams.add(jPanelAP,    new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), -2, 0));



        jPanelSynapseProperties.add(jLabelSynapseProperties, null);

       jPanelSynapseProperties.add(scrollPaneSyns, null);


       jPanelSynapseProperties.add(jButtonSynPropsAdd, null);
        jPanelSynapseProperties.add(jButtonSynPropsEdit, null);
        jPanelSynapseProperties.add(jButtonSynPropsRemove, null);



        //jPanelSynapseProperties.add(jTextFieldSynInfo, null);
        //jPanelSynapseProperties.add(jButtonSynProps, null);


        jPanelExtraParams.add(jPanelSearch,    new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 241, 0));
        jPanelSearch.add(jRadioButtonCompRandom, null);
        jPanelSearch.add(jRadioButtonClosest, null);
        jPanelSearch.add(jRadioButtonRandClose, null);
        jPanelSearch.add(jTextFieldRandCloseNumber, null);
        jPanelExtraParams.add(jPanelMaxMin,    new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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

        jPanelMaxMin.add(jLabelDim, null);
        jPanelMaxMin.add(jTextFieldDim, null);


        jPanelMaxMin.add(jLabelAttempts, null);
        jPanelMaxMin.add(jTextFieldAttempts, null);



        jPanelExtraParams.add(jPanelConnConds,     new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        buttonGroupSrcTargetDir.add(jRadioButtonSrcToTrgt);
        buttonGroupSrcTargetDir.add(jRadioButtonTrgtToSrc);
        jPanelConnCondsNum.add(jTextFieldConnCondsNum, null);
        jPanelConnCondsNum.add(jButtonConnCondsNum, null);

        scrollPaneSyns.setMinimumSize(new Dimension(400, 60));
        scrollPaneSyns.setPreferredSize(new Dimension(400, 60));



        jButtonSynPropsAdd.setText("Add");
        jButtonSynPropsAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                jButtonSynPropsAdd_actionPerformed(e);
                } catch (EquationException ex) {
                    java.util.logging.Logger.getLogger(NetworkConnectionDialog.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        });

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

        ///jTextFieldSynInfo.setText(chosenSynapticProperties.toString());


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
        //jTextFieldSynInfo.setToolTipText(toolTipText.getToolTip("Netconngui Syn Props"));

        this.jPanelAP.setToolTipText(toolTipText.getToolTip("Netconngui AP Speed"));
        this.jTextFieldAP.setToolTipText(toolTipText.getToolTip("Netconngui AP Speed"));

        jButtonSynPropsAdd.setToolTipText(toolTipText.getToolTip("Netconngui Syn Props Button"));

        //jPanelGrowth.setToolTipText(toolTipText.getToolTip("Netconngui Growth"));

        //jRadioButtonGrowModeJump.setToolTipText(toolTipText.getToolTip("Netconngui Growth Jump"));
        //jRadioButtonGrowModeDendGrow.setToolTipText(toolTipText.getToolTip("Netconngui Growth Dend"));
        //jRadioButtonGrowModeGrowAxon.setToolTipText(toolTipText.getToolTip("Netconngui Growth Axon"));

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

        if (newNetConnMode &&
            (/*project.complexConnectionsInfo.getAllComplexConnNames().contains(jTextFieldName.getText()) ||*/
             project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames().contains(jTextFieldName.getText())))
        {
            GuiUtils.showErrorMessage(logger, "That name of Network Connection has already been used.\nPlease select another one.", null, this);
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
            if (maxString.toUpperCase().indexOf("MAX")>=0) maxString = Float.MAX_VALUE+"";

            String dim = jTextFieldDim.getText();

            if (!(dim.equals(MaxMinLength.RADIAL)||
                  dim.equals(MaxMinLength.X_DIR)||
                  dim.equals(MaxMinLength.Y_DIR)||
                  dim.equals(MaxMinLength.Z_DIR)||
                  dim.equals(MaxMinLength.SOMA)))
            {
            GuiUtils.showErrorMessage(logger, "The only possible values for dimension in which to measure the max/min distance are:\n"
                                      +MaxMinLength.RADIAL+": the radial distance between pre and post synaptic connection sites\n"
                                      +MaxMinLength.X_DIR+": the max/min distance in x direction\n"
                                      +MaxMinLength.Y_DIR+": the max/min distance in y direction\n"
                                      +MaxMinLength.Z_DIR+": the max/min distance in z direction\n"
                                      +MaxMinLength.SOMA+": the radial distance between the source cell soma and the target cell soma\n", null, this);
            return;


            }

            float min = Float.parseFloat(jTextFieldMin.getText());
                                         if (min<0) min = 0;

            maxMin = new MaxMinLength(Float.parseFloat(maxString),
                                      min,
                                      jTextFieldDim.getText(),
                                      Integer.parseInt(jTextFieldAttempts.getText()));
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please insert a correct number into the fields for max and min lengths, and number of attempts", null, this);
            return;
        }

        if (jRadioButtonClosest.isSelected()
            && maxMin.getMaxLength()!=Float.MAX_VALUE
            && maxMin.getMaxLength()!=0f)
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
        
        connConds.setAllowAutapses(jCheckBoxAutapses.isSelected());
        
        connConds.setNoRecurrent(jCheckBoxRecurrent.isSelected());
        
        PrePostAllowedLocs pp = new PrePostAllowedLocs();
        
        pp.setSomaAllowedPre(jCheckBoxSomaPre.isSelected());
        pp.setDendritesAllowedPre(jCheckBoxDendPre.isSelected());
        pp.setAxonsAllowedPre(jCheckBoxAxonPre.isSelected());
        
        pp.setSomaAllowedPost(jCheckBoxSomaPost.isSelected());
        pp.setDendritesAllowedPost(jCheckBoxDendPost.isSelected());
        pp.setAxonsAllowedPost(jCheckBoxAxonPost.isSelected());
        
        connConds.setPrePostAllowedLoc(pp);

        if (jTextFieldDim.getText().equals("s"))
        {
             maxMin.setDimension("s");
        }



       boolean problem = false;


       try
       {
           float aps =  -1;
           if (jTextFieldAP.getText().toUpperCase().indexOf("MAX")>=0)
               aps = Float.MAX_VALUE;
           else
               aps = Float.parseFloat(this.jTextFieldAP.getText());

           if (aps <= 0)
           {
               problem = true;
           }
           else
           {
               this.apSpeed = aps;
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
           GuiUtils.showErrorMessage(logger, "Please enter an integer (> 0) for max number of connections to source cell on each target cell,\n"
                                     +" or MAX to allow any number of source cells on a target cell", null, this);
           return;
        }


        if (jComboBoxSource.getSelectedItem().equals( sourceComboString))
        {
            GuiUtils.showErrorMessage(logger, "Please select the cell group which is the source of the network connection", null, this);
            return;
        }

        if (jComboBoxTarget.getSelectedItem().equals(targetComboString))
        {
            GuiUtils.showErrorMessage(logger, "Please select the cell group which is the target of the network connection", null, this);
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


        chosenSynapticPropList.removeAllElements();
        for (int i = 0; i < listModelSyns.getSize(); i++)
        {
            chosenSynapticPropList.add((SynapticProperties)listModelSyns.getElementAt(i));
        }


/*
        if (jRadioButtonGrowModeGrowAxon.isSelected()) growMode = GrowMode.getGrowModeAxonsGrow();
        else if (jRadioButtonGrowModeDendGrow.isSelected()) growMode = GrowMode.getGrowModeDendsGrow();
        else growMode = GrowMode.getGrowModeJump();*/

        this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }



    void jButtonSynPropsAdd_actionPerformed(ActionEvent e) throws EquationException
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
            /** @todo Fix this??? */
           ////////////////pppppppppppp//////// if (!names.elementAt(i).equals(sourceCG))
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
        
        if (jComboBoxSource.getSelectedItem().equals(jComboBoxTarget.getSelectedItem()))
        {
            jCheckBoxAutapses.setEnabled(true);
            jCheckBoxRecurrent.setEnabled(true);
        }
        else
        {
           jCheckBoxAutapses.setEnabled(false);
           jCheckBoxRecurrent.setEnabled(false);
        }
        
        Dimension d = this.getContentPane().getPreferredSize();
        jPanelMain.setMaximumSize(d);
        
        
        String targetCG = (String)e.getItem();
        logger.logComment("New Cell group selected: "+targetCG);

        String cellType = project.cellGroupsInfo.getCellType(targetCG);
        jTextFieldCellTypeTrgt.setText(cellType);

        String region = project.cellGroupsInfo.getRegionName(targetCG);
        jTextFieldRegionTgt.setText(region);

        if (jTextFieldName.getText().equals(initiallySuggestedName))
        {
            logger.logComment("Changing the initial suggestion for the net conn name...");
            String proposedName = "NetConn_"+ jComboBoxSource.getSelectedItem()+ "_"+targetCG;
            Vector allNames = project.morphNetworkConnectionsInfo.getAllSimpleNetConnNames();
            int count = 1;
            while (allNames.contains(proposedName))
            {
                proposedName = "NetConn_"
                    + jComboBoxSource.getSelectedItem()
                    + "_"+targetCG
                    + "_"+count;

                count++;
            }
            jTextFieldName.setText(proposedName);
        }
        
        
        
        
//        try {
//                jbInit();
//            } catch (Exception ex) {
//                java.util.logging.Logger.getLogger(NetworkConnectionDialog.class.getName()).log(Level.SEVERE, null, ex);
//            }


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

    public float getAPSpeed()
    {
        return apSpeed;
    }




    public int getRandomCloseNumber()
    {
        return randomCloseNumber;
    }


/*
    public SynapticProperties getSynapticProperties()
    {

        return chosenSynapticProperties;
    }*/


    public Vector<SynapticProperties> getSynapticPropsList()
    {
        return chosenSynapticPropList;
    }

    public String getNetworkConnName()
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



/*
    void jButtonSynProps_actionPerformed(ActionEvent e)
    {
        SynapticPropertiesDialog dlg = new SynapticPropertiesDialog(this, chosenSynapticProperties,
                                               project);


        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);


        dlg.pack();
        dlg.setVisible(true);

        jTextFieldSynInfo.setText(chosenSynapticProperties.toString());

    }*/

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
        NumberGeneratorDialog dlg = new NumberGeneratorDialog((Frame)null, "Connectivity", "Connectivity",connConds.getNumConnsInitiatingCellGroup());

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
    
    
    public static void main(String[] args) throws EquationException
    {
        File pf = new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml");

        Project p = null;
        try
        {
            p = Project.loadProject(pf, null);
            System.out.println("Opened: "+ p.getProjectFullFileName());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        NetworkConnectionDialog ncd = new NetworkConnectionDialog(null, p, "Netkon");
        
        
        ncd.pack();
        GuiUtils.centreWindow(ncd);
        
        ncd.setVisible(true);
        
    }
    


}
