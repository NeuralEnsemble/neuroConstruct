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

package ucl.physiol.neuroconstruct.nmodleditor.gui;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * nmodlEditor application software. Main Frame of application
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class NmodlEditorFrame extends JFrame  implements ModFileEventListener
{
    ClassLogger logger = new ClassLogger("NmodlEditorFrame");

    // whether it's run alone or via neuroConstruct...
    boolean standAlone = false;

    String titleString = new String("nmodlEditor");

    ModFile currentModFile = null;

    // Only needed if launched from neuroConstruct project
    String projectMainDir = null;

    // Needed to disable "mark mod file as edited" when loading file (i.e. when textfields are being filled in)
    boolean initialisingModFile = false;


    File dirToCheckOnLoad = new File(GeneralProperties.getNeuronHomeDir());


    RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNmodlEditRecentFilesFilename());

    boolean initialTextNeedsChecking = false;
    boolean breakpointTextNeedsChecking = false;
    boolean functionTextNeedsChecking = false;
    boolean procedureTextNeedsChecking = false;
    boolean derivativeTextNeedsChecking = false;
    boolean netReceiveTextNeedsChecking = false;

    //DefaultListModel listModelRangeVariables = new DefaultListModel();
    //DefaultListModel listModelGlobalVariables = new DefaultListModel();

    String defaultFunctionText = new String("No functions at present");
    String defaultProcedureText = new String("No procedures at present");

    String NEURON_TAB = "Neuron";
    String PARAMETERS_TAB = "Parameters";
    String ASSIGNED_TAB = "Assigned";
    String STATE_TAB = "State";
    String UNITS_TAB = "Units";
    String INITIAL_TAB = "Initial";
    String DERIVATIVE_TAB = "Derivative";
    String BREAKPOINT_TAB = "Breakpoint";
    String FUNCTIONS_TAB = "Functions";
    String PROCEDURES_TAB = "Procedures";
    //String INDEPENDENT_TAB = "Independent";
    String NET_RECEIVE_TAB = "Net Receive";
    String VIEW_AS_TEXT_TAB = "View As Text";

    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelEditor = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton jButtonLoad = new JButton();
    JButton jButtonNew = new JButton();
    JButton jButtonSave = new JButton();
    JButton jButtonCompile = new JButton();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel jPanelMainSettings = new JPanel();
    JPanel jPanelViewAsText = new JPanel();
    JLabel jLabelFileName = new JLabel();
    JTextField jTextFieldTitle = new JTextField();
    JTextField jTextFieldFileName = new JTextField();

    JEditorPane jEditorPaneMain = new JEditorPane();

    JScrollPane jScroolPaneEditorPane = new JScrollPane();
    JScrollPane jScroolPaneUnits = new JScrollPane();

    JScrollPane jScroolPaneParameters = new JScrollPane();
    JScrollPane jScroolPaneAssigned = new JScrollPane();
    JScrollPane jScroolPaneState = new JScrollPane();

    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabelTitle = new JLabel();
    TitledBorder titledBorder1;
    JPanel jPanelUnits = new JPanel();

    JList jListUnits = new JList();
    DefaultListModel listModelUnits = new DefaultListModel();

    JList jListParameters = new JList();
    DefaultListModel listModelParameters = new DefaultListModel();

    JList jListAssigned = new JList();
    DefaultListModel listModelAssigned = new DefaultListModel();

    JList jListState = new JList();
    DefaultListModel listModelState = new DefaultListModel();


    JPanel jPanelUnitsControls = new JPanel();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel jPanelNameTitle = new JPanel();
    JPanel jPanelNeuronBlock = new JPanel();

    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanelUnitSummary = new JPanel();


    JLabel jLabelUnitSelectedName = new JLabel();
    JTextField jTextFieldUnitShortName = new JTextField();
    JLabel jLabelUnitFullName = new JLabel();
    JTextField jTextFieldUnitFullName = new JTextField();
    JTextField jTextFieldUnitComment = new JTextField();
    JLabel jLabelUnitComment = new JLabel();


    JLabel jLabelStateName = new JLabel();
    JTextField jTextFieldStateName = new JTextField();
    JLabel jLabelStateDim = new JLabel();
    JTextField jTextFieldStateDim = new JTextField();
    JTextField jTextFieldStateComment = new JTextField();
    JLabel jLabelStateComment = new JLabel();


    JLabel jLabelAssignedName = new JLabel();
    JTextField jTextFieldAssignedName = new JTextField();
    JLabel jLabelAssignedDim = new JLabel();
    JTextField jTextFieldAssignedDim= new JTextField();
    JTextField jTextFieldAssignedComment = new JTextField();
    JLabel jLabelAssignedComment = new JLabel();


    JLabel jLabelParametersName = new JLabel();
    JTextField jTextFieldParametersName = new JTextField();
    JLabel jLabelParametersValue = new JLabel();
    JTextField jTextFieldParametersValue = new JTextField();
    JLabel jLabelParametersDim = new JLabel();
    JTextField jTextFieldParametersDim = new JTextField();
    JTextField jTextFieldParametersComment = new JTextField();
    JLabel jLabelParametersComment = new JLabel();



    JPanel jPanelUnitButtons = new JPanel();
    JButton jButtonUnitRemove = new JButton();
    JButton jButtonUnitAdd = new JButton();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    BorderLayout borderLayout7 = new BorderLayout();
    JPanel jPanelAssigned = new JPanel();
    JPanel jPanelState = new JPanel();
    Border border1;
    TitledBorder titledBorder2;
    JPanel jPanelParameters = new JPanel();
    Border border2;
    TitledBorder titledBorder3;
    Border border3;
    TitledBorder titledBorder4;
    Border border4;
    TitledBorder titledBorder5;
    JPanel jPanelParametersControls = new JPanel();
    BorderLayout borderLayout8 = new BorderLayout();
    JPanel jPanelAssignedControls = new JPanel();
    BorderLayout borderLayout9 = new BorderLayout();
    JPanel jPanelStateControls = new JPanel();
    BorderLayout borderLayout10 = new BorderLayout();
    JButton jButtonUnitUpdate = new JButton();
    JPanel jPanelStateSummary = new JPanel();
    JPanel jPanelStateButtons = new JPanel();
    JPanel jPanelAssignedSummary = new JPanel();
    JPanel jPanelAssignedButtons = new JPanel();
    JPanel jPanelParametersSummary = new JPanel();
    JPanel jPanelParametersButtons = new JPanel();
    JButton jButtonParameterAdd = new JButton();
    JButton jButtonParameterRemove = new JButton();
    JButton jButtonParametersUpdate = new JButton();
    BorderLayout borderLayout11 = new BorderLayout();
    JButton jButtonAssignedUpdate = new JButton();
    JButton jButtonAssignedRemove = new JButton();
    JButton jButtonAssignedAdd = new JButton();
    BorderLayout borderLayout12 = new BorderLayout();
    JButton jButtonStateAdd = new JButton();
    JButton jButtonStateRemove = new JButton();
    JButton jButtonStateUpdate = new JButton();
    BorderLayout borderLayout13 = new BorderLayout();
    JPanel jPanelNeuronType = new JPanel();
    ButtonGroup buttonGroupPointOrDensity = new ButtonGroup();
    JRadioButton jRadioButtonDensityMechnaism = new JRadioButton();
    JRadioButton jRadioButtonPointProcess = new JRadioButton();
    JLabel jLabelNeuronType = new JLabel();
    JTextField jTextFieldNeuronTypeName = new JTextField();
    JPanel jPanelNeuronCurrent = new JPanel();
    JLabel jLabelNeuronCurrent = new JLabel();
    JTextField jTextFieldNeuronCurrent = new JTextField();
    JRadioButton jRadioButtonElectrode = new JRadioButton();
    JRadioButton jRadioButtonNonSpecific = new JRadioButton();
    JRadioButton jRadioButtonNoCurrent = new JRadioButton();
    ButtonGroup buttonGroupCurrent = new ButtonGroup();
    JPanel jPanelBreakpoint = new JPanel();
    JPanel jPanelInitial = new JPanel();
    Border border5;
    TitledBorder titledBorder6;
    Border border6;
    TitledBorder titledBorder7;
    JPanel jPanelInitialControls = new JPanel();
    JButton jButtonInitialParseAndSave = new JButton();
    BorderLayout borderLayout14 = new BorderLayout();
    JPanel jPanelInitialEditor = new JPanel();
    JTextArea jTextAreaInitial = new JTextArea();
    Border border7;
    JPanel jPanelBreakpointEditor = new JPanel();
    JPanel jPanelBreakpointControls = new JPanel();
    JButton jButtonBreakpointParseAndSave = new JButton();
    JTextArea jTextAreaBreakpoint = new JTextArea();
    BorderLayout borderLayout15 = new BorderLayout();
    Border border8;
    ButtonGroup buttonGroupParameters = new ButtonGroup();
    JPanel jPanelParametersRadioButtons = new JPanel();
    JRadioButton jRadioButtonParametersRange = new JRadioButton();
    JRadioButton jRadioButtonParametersGlobal = new JRadioButton();
    JRadioButton jRadioButtonParametersNoRef = new JRadioButton();
    JPanel jPanelNeuronParams = new JPanel();
    JPanel jPanelNeuronGlobal = new JPanel();
    JPanel jPanelNeuronRange = new JPanel();
    JList jListNeuronRange = new JList();
    JList jListNeuronGlobal = new JList();
    BorderLayout borderLayout16 = new BorderLayout();
    JButton jButtonTestUnits = new JButton();
    JLabel jLabelNeuronGlobal = new JLabel();
    Border border9;
    Border border10;
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel jLabelNeuronRange = new JLabel();
    JPanel jPanelAssignedRadioButtons = new JPanel();
    JRadioButton jRadioButtonAssignedNoRef = new JRadioButton();
    JRadioButton jRadioButtonAssignedRange = new JRadioButton();
    JRadioButton jRadioButtonAssignedGlobal = new JRadioButton();
    ButtonGroup buttonGroupAssigned = new ButtonGroup();
    JPanel jPanelFunctions = new JPanel();
    Border border11;
    TitledBorder titledBorder8;
    JPanel jPanelFunctionControls = new JPanel();
    JPanel jPanelFunctionEditor = new JPanel();
    JPanel jPanelFunctionSelection = new JPanel();
    JComboBox jComboBoxFunctionList = new JComboBox();
    JTextArea jTextAreaFunction = new JTextArea();
    JButton jButtonFunctionUpdate = new JButton();
    BorderLayout borderLayout17 = new BorderLayout();
    Border border12;
    JLabel jLabelFunctionName = new JLabel();
    JTextField jTextFieldFunctionParameters = new JTextField();
    JPanel jPanelFunctionNameParams = new JPanel();
    JLabel jLabelFunctionSelect = new JLabel();
    JButton jButtonFunctionAdd = new JButton();
    JTextField jTextFieldFunctionNewName = new JTextField();
    JButton jButtonFunctionRemove = new JButton();
    JPanel jPanelProcedures = new JPanel();
    Border border13;
    TitledBorder titledBorder9;
    JPanel jPanelProcedureControls = new JPanel();
    JPanel jPanelProcedureEditor = new JPanel();
    JPanel jPanelProcedureSelection = new JPanel();
    JLabel jLabelProcedureMain = new JLabel();
    JPanel jPanelProcedureNameParam = new JPanel();
    JLabel jLabelProcedureName = new JLabel();
    JTextArea jTextAreaProcedure = new JTextArea();
    JButton jButtonProcedureAdd = new JButton();
    JButton jButtonProcedureRemove = new JButton();
    JButton jButtonProcedureUpdate = new JButton();
    JTextField jTextFieldProcedureName = new JTextField();
    BorderLayout borderLayout18 = new BorderLayout();
    JComboBox jComboBoxProcedureList = new JComboBox();
    JTextField jTextFieldProcedureParameters = new JTextField();
    Border border14;
    JPanel jPanelDerivative = new JPanel();
    Border border15;
    TitledBorder titledBorder10;
    JPanel jPanelDerivativeControls = new JPanel();
    JPanel jPanelDerivativeEditor = new JPanel();
    JButton jButtonDerivativeParseAndSave = new JButton();
    JTextArea jTextAreaDerivative = new JTextArea();
    BorderLayout borderLayout19 = new BorderLayout();
    Border border16;
    JPanel jPanelDerivativeName = new JPanel();
    JLabel jLabelDerivativeName = new JLabel();
    JTextField jTextFieldDerivaiveName = new JTextField();
    Border border17;
    TitledBorder titledBorder11;
    JPanel jPanelNetReceive = new JPanel();
    Border border18;
    TitledBorder titledBorder12;
    Border border19;
    JPanel jPanelNetReceiveEditor = new JPanel();
    JPanel jPanelNetReceiveControls = new JPanel();
    JTextArea jTextAreaNetReceive = new JTextArea();
    JButton jButtonNetReceiveParseAndSave = new JButton();
    BorderLayout borderLayout20 = new BorderLayout();
    Border border20;
    JMenuItem jMenuNew = new JMenuItem();
    JMenuItem jMenuLoad = new JMenuItem();
    JMenuItem jMenuSave = new JMenuItem();


    ImageIcon imageMain = new ImageIcon();
    JLabel jLabelImage = new JLabel();
    JPanel jPanelMainImage = new JPanel();
    Border border21;
    Border border22;
    Border border23;
    Border border24;
    FlowLayout flowLayout1 = new FlowLayout();
    Border border25;
    JMenuItem jMenuSaveAs = new JMenuItem();
    JMenuItem jMenuItemInternalInfo = new JMenuItem();
    JButton jButtonRunExample = new JButton();
    //JPanel jPanelIndependent = new JPanel();

    private NmodlEditorFrame()
    {

    }

    //Construct the frame
    public NmodlEditorFrame(String projectMainDir)
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            jbInit();
            extraInit();
            setToolTips();
            refreshMenus();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        if (projectMainDir == null)
        {
            this.standAlone = true;
        }
        else
        {
            this.projectMainDir = projectMainDir;
            this.standAlone = false;
        }

    }

    /**
     * This function is automatically generated by JBuilder
     * @throws Exception Jbuilder related...
     */
    private void jbInit() throws Exception
    {
        imageMain = new ImageIcon(ucl.physiol.neuroconstruct.nmodleditor.gui.NmodlEditorFrame.class.getResource("small2.png"));


        border21 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, new Color(228, 228, 228),
                                                  new Color(228, 228, 228), new Color(93, 93, 93),
                                                  new Color(134, 134, 134));

        border22 = BorderFactory.createEmptyBorder(12,0,12,0);
        border23 = BorderFactory.createEmptyBorder();
        border24 = BorderFactory.createEmptyBorder();
        border25 = BorderFactory.createLineBorder(Color.white,1);
        jMenuFile.setText("File");
        jMenuFileExit.setText("Exit");
        jMenuFileExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuFileExit_actionPerformed(e);
            }
        });
        jMenuHelp.setText("Help");
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuHelpAbout_actionPerformed(e);
            }
        });
        jMenuNew.setActionCommand("New *.mod file...");
        jMenuNew.setText("New File...");
        jMenuNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuNew_actionPerformed(e);
            }
        });
        jMenuLoad.setText("Load File...");
        jMenuLoad.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuLoad_actionPerformed(e);
            }
        });
        jMenuSave.setEnabled(false);
        jMenuSave.setText("Save");
        jMenuSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuSave_actionPerformed(e);
            }
        });
        jLabelImage.setBorder(border21);
        jLabelImage.setRequestFocusEnabled(true);
        jLabelImage.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelImage.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabelImage.setIcon(null);
        jPanelNeuronBlock.setMaximumSize(new Dimension(700, 370));
        jPanelNeuronBlock.setMinimumSize(new Dimension(700, 370));
        jPanelNeuronBlock.setPreferredSize(new Dimension(700, 370));
        jPanelMainImage.setBorder(border22);
        jPanelMainImage.setMinimumSize(new Dimension(700, 92));
        jPanelMainImage.setPreferredSize(new Dimension(700, 92));
        jPanelNameTitle.setBorder(null);
        jPanelNameTitle.setMaximumSize(new Dimension(600, 70));
        jPanelNameTitle.setMinimumSize(new Dimension(600, 70));
        jPanelNameTitle.setPreferredSize(new Dimension(600, 70));
        jPanelNeuronType.setBorder(border23);
        jPanelNeuronType.setMaximumSize(new Dimension(600, 33));
        jPanelNeuronType.setMinimumSize(new Dimension(600, 33));
        jPanelNeuronCurrent.setBorder(border24);
        jPanelNeuronCurrent.setMaximumSize(new Dimension(600, 33));
        jPanelNeuronCurrent.setMinimumSize(new Dimension(600, 33));
        jTextFieldFileName.setText("");
        jMenuSaveAs.setEnabled(false);
        jMenuSaveAs.setText("Save As...");
    jMenuSaveAs.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuSaveAs_actionPerformed(e);
      }
    });
    jRadioButtonParametersNoRef.setSelected(true);
        jRadioButtonAssignedNoRef.setSelected(true);
        jTextAreaFunction.setEditable(false);
        jTextFieldFunctionParameters.setEditable(false);
        jTextFieldProcedureParameters.setEditable(false);
        jTextAreaProcedure.setEditable(false);
        jMenuItemInternalInfo.setText("Print Internal Info");
        jMenuItemInternalInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemInternalInfo_actionPerformed(e);
            }
        });
        jButtonNetReceiveParseAndSave.setEnabled(false);
        jButtonRunExample.setEnabled(false);
        jButtonRunExample.setText("Run example");
        jButtonRunExample.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRunExample_actionPerformed(e);
            }
        });
        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenuHelp);
        this.setJMenuBar(jMenuBar1);

        jMenuFile.add(jMenuNew);
        jMenuFile.add(jMenuLoad);
        jMenuFile.add(jMenuSave);
        jMenuFile.add(jMenuSaveAs);

        jMenuFile.add(jMenuFileExit);

        jMenuHelp.add(jMenuItemInternalInfo);
        jMenuHelp.add(jMenuHelpAbout);

        contentPane = (JPanel) this.getContentPane();
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),"");
        border1 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),"UNITS block");
        border2 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder3 = new TitledBorder(border2,"PARAMETERS block");
        border3 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder4 = new TitledBorder(border3,"ASSIGNED block");
        border4 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder5 = new TitledBorder(border4,"STATE block");
        border5 = BorderFactory.createEmptyBorder();
        titledBorder6 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),"INITIAL block");
        border6 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder7 = new TitledBorder(border6,"BREAKPOINT block");
        border7 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border8 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border9 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border10 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border11 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder8 = new TitledBorder(border11,"FUNCTION blocks");
        border12 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border13 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder9 = new TitledBorder(border13,"PROCEDURE blocks");
        border14 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border15 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder10 = new TitledBorder(border15,"DERIVATIVE block");
        border16 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        border17 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder11 = new TitledBorder(border17,"NEURON block");
        border18 = BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134));
        titledBorder12 = new TitledBorder(border18,"NET_RECEIVE block");
        border19 = BorderFactory.createCompoundBorder(titledBorder12,BorderFactory.createEmptyBorder(3,3,3,3));
        border20 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,Color.white,Color.white,new Color(124, 124, 124),new Color(178, 178, 178)),BorderFactory.createEmptyBorder(3,3,3,3));
        contentPane.setLayout(borderLayout1);
        this.setResizable(true);
        this.setSize(new Dimension(1023, 697));
        this.setTitle(titleString);
        jPanelMain.setLayout(borderLayout2);
        jButtonLoad.setText("Load *.mod file");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonLoad_actionPerformed(e);
            }
        });
        jButtonNew.setToolTipText("");
        jButtonNew.setText("New *.mod file");
        jButtonNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNew_actionPerformed(e);
            }
        });
        jButtonSave.setEnabled(false);
        jButtonSave.setText("Save current file");
        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSave_actionPerformed(e);
            }
        });
        jButtonCompile.setEnabled(false);
        jButtonCompile.setDoubleBuffered(false);
        jButtonCompile.setText("Compile");
        jButtonCompile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCompile_actionPerformed(e);
            }
        });
        contentPane.setMaximumSize(new Dimension(500, 600));
        contentPane.setMinimumSize(new Dimension(500, 600));
        contentPane.setPreferredSize(new Dimension(500, 600));
        jPanelMain.setBorder(titledBorder1);
        jPanelMain.setMaximumSize(new Dimension(500, 600));
        jPanelMain.setMinimumSize(new Dimension(500, 600));
        jPanelMain.setPreferredSize(new Dimension(500, 600));
        jPanelEditor.setLayout(borderLayout3);
        jPanelViewAsText.setRequestFocusEnabled(true);
        jPanelViewAsText.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentShown(ComponentEvent e)
            {
                jPanelViewAsText_componentShown(e);
            }
        });

        jPanelViewAsText.setLayout(borderLayout4);
        jLabelFileName.setText("Name of *.mod file:");
        jTextFieldFileName.setEditable(false);
        jTextFieldFileName.setColumns(40);
        jLabelTitle.setText("Title:");
        jTextFieldTitle.setEnabled(false);
        jTextFieldTitle.setEditable(false);
        jTextFieldTitle.setText("");
        jTextFieldTitle.setColumns(60);



        jListUnits.setBorder(BorderFactory.createLoweredBevelBorder());
        jListUnits.setMaximumSize(new Dimension(200, 200));
        jListUnits.setMinimumSize(new Dimension(200, 200));
        jListUnits.setPreferredSize(new Dimension(200, 200));
        jListUnits.setModel(listModelUnits);
        jListUnits.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                jListUnits_valueChanged(e);
            }
        });



        jListParameters.setBorder(BorderFactory.createLoweredBevelBorder());
        jListParameters.setMaximumSize(new Dimension(200, 200));
        jListParameters.setMinimumSize(new Dimension(200, 200));
        jListParameters.setPreferredSize(new Dimension(200, 200));
        jListParameters.setModel(listModelParameters);
        jListParameters.addListSelectionListener(new javax.swing.event.
                                            ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                jListParameters_valueChanged(e);
            }
        });

        jListAssigned.setBorder(BorderFactory.createLoweredBevelBorder());
        jListAssigned.setMaximumSize(new Dimension(200, 200));
        jListAssigned.setMinimumSize(new Dimension(200, 200));
        jListAssigned.setPreferredSize(new Dimension(200, 200));
        jListAssigned.setModel(listModelAssigned);
        jListAssigned.addListSelectionListener(new javax.swing.event.
                                            ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                jListAssigned_valueChanged(e);
            }
        });


        jListState.setBorder(BorderFactory.createLoweredBevelBorder());
        jListState.setMaximumSize(new Dimension(200, 200));
        jListState.setMinimumSize(new Dimension(200, 200));
        jListState.setPreferredSize(new Dimension(200, 200));
        jListState.setModel(listModelState);
        jListState.addListSelectionListener(new javax.swing.event.
                                            ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                jListState_valueChanged(e);
            }
        });


        jPanelUnits.setLayout(borderLayout5);
        jPanelUnitsControls.setBorder(null);
        jPanelUnitsControls.setPreferredSize(new Dimension(14, 14));
        jPanelUnitsControls.setLayout(borderLayout7);
        borderLayout5.setHgap(10);

        jPanelMainSettings.setLayout(flowLayout1);
        jPanelNeuronBlock.setBorder(titledBorder11);
        jPanelNameTitle.setLayout(gridBagLayout1);

        jTextFieldUnitFullName.setEditable(false);
        jTextFieldUnitFullName.setColumns(8);

        jEditorPaneMain.setEditable(false);
        jLabelUnitSelectedName.setText("Short name:");
        jTextFieldUnitShortName.setDebugGraphicsOptions(0);
        jTextFieldUnitShortName.setEditable(false);
        jTextFieldUnitShortName.setColumns(8);
        jTextFieldUnitShortName.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                jTextFieldUnitShortName_keyPressed(e);
            }
        });
        jLabelUnitFullName.setText("Full Name:");


        jTextFieldUnitComment.setEditable(false);
        jTextFieldUnitComment.setColumns(20);
        jLabelUnitComment.setText("Comment:");
        jPanelUnitSummary.setLayout(gridBagLayout2);
        jButtonUnitRemove.setEnabled(false);
        jButtonUnitRemove.setText("Remove selected unit");
        jButtonUnitRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonUnitRemove_actionPerformed(e);
            }
        });
        jButtonUnitAdd.setEnabled(false);
        jButtonUnitAdd.setText("Add new unit");
        jButtonUnitAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonUnitAdd_actionPerformed(e);
            }
        });
        jPanelUnits.setBorder(titledBorder2);
        jPanelParameters.setBorder(titledBorder3);
        jPanelParameters.setLayout(borderLayout8);
        jPanelAssigned.setBorder(titledBorder4);
        jPanelAssigned.setLayout(borderLayout9);
        jPanelState.setBorder(titledBorder5);
        jPanelState.setLayout(borderLayout10);
        jButtonUnitUpdate.setEnabled(false);
        jButtonUnitUpdate.setText("Update selected unit");
        jButtonUnitUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonUnitUpdate_actionPerformed(e);
            }
        });
        jPanelUnitButtons.setMaximumSize(new Dimension(32767, 32767));
        jPanelUnitButtons.setMinimumSize(new Dimension(389, 80));
        jPanelUnitButtons.setPreferredSize(new Dimension(389, 80));
        jButtonParameterAdd.setEnabled(false);
        jButtonParameterAdd.setText("Add new Parameter");
        jButtonParameterAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonParameterAdd_actionPerformed(e);
            }
        });
        jButtonParameterRemove.setEnabled(false);
        jButtonParameterRemove.setText("Remove Parameter");
        jButtonParameterRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonParameterRemove_actionPerformed(e);
            }
        });
        jButtonParametersUpdate.setEnabled(false);
        jButtonParametersUpdate.setToolTipText("");
        jButtonParametersUpdate.setText("Update Parameter");
        jButtonParametersUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonParametersUpdate_actionPerformed(e);
            }
        });
        jPanelParametersControls.setDebugGraphicsOptions(0);
        jPanelParametersControls.setMinimumSize(new Dimension(397, 80));
        jPanelParametersControls.setPreferredSize(new Dimension(397, 80));
        jPanelParametersControls.setLayout(borderLayout11);
        jPanelParametersButtons.setMaximumSize(new Dimension(32767, 32767));
        jPanelParametersButtons.setPreferredSize(new Dimension(397, 80));
        jButtonAssignedUpdate.setEnabled(false);
        jButtonAssignedUpdate.setText("Update Assigned variable");
        jButtonAssignedUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAssignedUpdate_actionPerformed(e);
            }
        });
        jButtonAssignedRemove.setEnabled(false);
        jButtonAssignedRemove.setText("Remove Assigned variable");
        jButtonAssignedRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAssignedRemove_actionPerformed(e);
            }
        });
        jButtonAssignedAdd.setEnabled(false);
        jButtonAssignedAdd.setText("Add new Assigned variable");
        jButtonAssignedAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonAssignedAdd_actionPerformed(e);
            }
        });
        jPanelAssignedControls.setLayout(borderLayout12);
        jPanelAssignedButtons.setMinimumSize(new Dimension(511, 80));
        jPanelAssignedButtons.setPreferredSize(new Dimension(511, 80));
        jButtonStateAdd.setEnabled(false);
        jButtonStateAdd.setText("Add new State variable");
        jButtonStateAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStateAdd_actionPerformed(e);
            }
        });
        jButtonStateRemove.setEnabled(false);
        jButtonStateRemove.setText("Remove State variable");
        jButtonStateRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStateRemove_actionPerformed(e);
            }
        });
        jButtonStateUpdate.setEnabled(false);
        jButtonStateUpdate.setText("Update State variable");
        jButtonStateUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStateUpdate_actionPerformed(e);
            }
        });
        jPanelStateControls.setLayout(borderLayout13);
        jPanelStateButtons.setMinimumSize(new Dimension(449, 80));
        jPanelStateButtons.setPreferredSize(new Dimension(449, 80));


        jTextFieldParametersName.setEditable(false);
        jTextFieldParametersValue.setEditable(false);
        jTextFieldParametersDim.setEditable(false);
        jTextFieldParametersComment.setEditable(false);
        jTextFieldAssignedName.setEditable(false);
        jTextFieldAssignedDim.setEditable(false);
        jTextFieldAssignedComment.setEditable(false);
        jTextFieldStateName.setEditable(false);
        jTextFieldStateDim.setEditable(false);
        jTextFieldStateComment.setEditable(false);
        jRadioButtonDensityMechnaism.setEnabled(false);
        jRadioButtonDensityMechnaism.setText("Density Mechanism");
        jRadioButtonDensityMechnaism.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonDensityMechnaism_actionPerformed(e);
            }
        });
        jRadioButtonPointProcess.setEnabled(false);
        jRadioButtonPointProcess.setText("Point process");
        jRadioButtonPointProcess.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonPointProcess_actionPerformed(e);
            }
        });
        jLabelNeuronType.setText("Name of main process:");
        jTextFieldNeuronTypeName.setEnabled(true);
        jTextFieldNeuronTypeName.setEditable(false);
        jTextFieldNeuronTypeName.setText("");
        jTextFieldNeuronTypeName.setColumns(12);
        jLabelNeuronCurrent.setText("Current variable:");
        jTextFieldNeuronCurrent.setEnabled(true);
        jTextFieldNeuronCurrent.setCaretPosition(0);
        jTextFieldNeuronCurrent.setEditable(false);
        jTextFieldNeuronCurrent.setColumns(12);

        jRadioButtonNoCurrent.setEnabled(false);
        jRadioButtonNoCurrent.setText("No current");
        jRadioButtonNoCurrent.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonNoCurrent_actionPerformed(e);
            }
        });
        jRadioButtonNonSpecific.setEnabled(false);
        jRadioButtonNonSpecific.setText("Non specific");
        jRadioButtonNonSpecific.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonNonSpecific_actionPerformed(e);
            }
        });

        jRadioButtonElectrode.setEnabled(false);
        jRadioButtonElectrode.setDoubleBuffered(false);
        jRadioButtonElectrode.setText("Electrode");
        jRadioButtonElectrode.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonElectrode_actionPerformed(e);
            }
        });
        jPanelInitial.setBorder(titledBorder6);
        jPanelInitial.setLayout(borderLayout14);
        jPanelBreakpoint.setBorder(titledBorder7);
        jPanelBreakpoint.setLayout(borderLayout15);

        jButtonInitialParseAndSave.setEnabled(false);
        jButtonInitialParseAndSave.setText("Parse and Save");
        jButtonInitialParseAndSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonInitialParseAndSave_actionPerformed(e);
            }
        });

        jTextAreaInitial.setEnabled(false);
        jTextAreaInitial.setBorder(border7);
        jTextAreaInitial.setMargin(new Insets(6, 6, 6, 6));

        jTextAreaInitial.setColumns(40);
        jTextAreaInitial.setLineWrap(true);
        jTextAreaInitial.setRows(20);
        jTextAreaInitial.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                jTextAreaInitial_keyReleased(e);
            }
        });
        jTextAreaInitial.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(FocusEvent e)
            {
                jTextAreaInitial_focusLost(e);
            }
        });
        jButtonBreakpointParseAndSave.setEnabled(false);
        jButtonBreakpointParseAndSave.setText("Parse and Save");
        jButtonBreakpointParseAndSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBreakpointParseAndSave_actionPerformed(e);
            }
        });
        jTextAreaBreakpoint.setEnabled(false);
        jTextAreaBreakpoint.setBorder(border8);
        jTextAreaBreakpoint.setCaretPosition(0);
        jTextAreaBreakpoint.setSelectionStart(0);
        jTextAreaBreakpoint.setText("");
        jTextAreaBreakpoint.setColumns(40);
        jTextAreaBreakpoint.setRows(20);
        jTextAreaBreakpoint.addFocusListener(new java.awt.event.FocusAdapter()
        {
            public void focusLost(FocusEvent e)
            {
                jTextAreaBreakpoint_focusLost(e);
            }
        });
        jTextAreaBreakpoint.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                jTextAreaBreakpoint_keyReleased(e);
            }
        });

        jRadioButtonParametersNoRef.setText("None");
        jRadioButtonParametersRange.setText("Range variable");
        jRadioButtonParametersGlobal.setText("Global variable");

        jPanelNeuronParams.setLayout(gridBagLayout3);
        jListNeuronGlobal.setEnabled(false);
        jListNeuronGlobal.setBorder(border9);
        jListNeuronGlobal.setMaximumSize(new Dimension(150, 200));
        jListNeuronGlobal.setMinimumSize(new Dimension(150, 200));
        jListNeuronGlobal.setPreferredSize(new Dimension(150, 200));

        jPanelNeuronParams.setBorder(BorderFactory.createEtchedBorder());
        jListNeuronRange.setEnabled(false);
        jListNeuronRange.setBorder(border10);
        jListNeuronRange.setMaximumSize(new Dimension(150, 200));
        jListNeuronRange.setMinimumSize(new Dimension(150, 200));
        jListNeuronRange.setOpaque(true);
        jListNeuronRange.setPreferredSize(new Dimension(150, 200));

        jButtonTestUnits.setEnabled(false);
        jButtonTestUnits.setText("Test units");
        jButtonTestUnits.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonTestUnits_actionPerformed(e);
            }
        });
        //jLabelNeuron.setText("jLabel1");
        jLabelNeuronGlobal.setText("Range variables");
        jLabelNeuronRange.setText("Global variables");

        jRadioButtonAssignedNoRef.setText("None");
        jRadioButtonAssignedRange.setText("Range variable");
        jRadioButtonAssignedGlobal.setToolTipText("");
        jRadioButtonAssignedGlobal.setText("Global variable");
        jPanelFunctions.setBorder(titledBorder8);
        jPanelFunctions.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                jPanelFunctions_componentHidden(e);
            }
        });
        jPanelProcedures.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                jPanelProcedures_componentHidden(e);
            }
        });

        jPanelFunctions.setLayout(borderLayout17);
        jTextAreaFunction.setEnabled(false);
        jTextAreaFunction.setBorder(border12);
        jTextAreaFunction.setToolTipText("");
        jTextAreaFunction.setText("");
        jTextAreaFunction.setColumns(40);
        jTextAreaFunction.setRows(20);
        jTextAreaFunction.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                jTextAreaFunction_keyPressed(e);
            }
        });
        jTextAreaProcedure.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                jTextAreaProcedure_keyPressed(e);
            }
        });

        jButtonFunctionUpdate.setEnabled(false);
        jButtonFunctionUpdate.setText("Update Current Function");
        jButtonFunctionUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonFunctionUpdate_actionPerformed(e);
            }
        });

        jComboBoxFunctionList.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxFunctionList_itemStateChanged(e);
            }
        });

        jComboBoxProcedureList.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxProcedureList_itemStateChanged(e);
            }
        });


        jLabelFunctionName.setMaximumSize(new Dimension(80, 15));
        jLabelFunctionName.setMinimumSize(new Dimension(80, 15));
        jLabelFunctionName.setPreferredSize(new Dimension(80, 15));
        jLabelFunctionName.setVerifyInputWhenFocusTarget(true);
        jLabelFunctionName.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabelFunctionName.setText("...");
        jLabelFunctionName.setVerticalAlignment(SwingConstants.CENTER);
        jTextFieldFunctionParameters.setEnabled(false);
        jTextFieldFunctionParameters.setText("...");
        jTextFieldFunctionParameters.setColumns(24);
        jTextFieldFunctionParameters.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                jTextFieldFunctionParameters_keyReleased(e);
            }
        });

        jTextFieldProcedureParameters.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyReleased(KeyEvent e)
            {
                jTextFieldProcedureParameters_keyReleased(e);
            }
        });

        jPanelFunctionNameParams.setMinimumSize(new Dimension(500, 30));
        jPanelFunctionNameParams.setPreferredSize(new Dimension(500, 30));
        jLabelFunctionSelect.setText("Please select a function to edit: ");

        jButtonFunctionAdd.setEnabled(false);
        jButtonFunctionAdd.setText("Add New Function:");
        jButtonFunctionAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonFunctionAdd_actionPerformed(e);
            }
        });
        jTextFieldFunctionNewName.setEnabled(false);
        jTextFieldFunctionNewName.setText("func_1");
        jTextFieldFunctionNewName.setColumns(10);
        jButtonFunctionRemove.setEnabled(false);
        jButtonFunctionRemove.setText("Remove Current Function");
        jButtonFunctionRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonFunctionRemove_actionPerformed(e);
            }
        });
        jPanelProcedures.setBorder(titledBorder9);
        jPanelProcedures.setLayout(borderLayout18);
        jLabelProcedureName.setText("Procedure...");
        jLabelProcedureMain.setText("Please select a procedure to edit:");
        jTextAreaProcedure.setEnabled(false);
        jTextAreaProcedure.setBorder(border14);
        jTextAreaProcedure.setText("");
        jTextAreaProcedure.setColumns(40);
        jTextAreaProcedure.setRows(20);
        jTextAreaProcedure.setTabSize(8);

        jTextFieldProcedureName.setEnabled(false);
        jTextFieldProcedureName.setText("proc_1");
        jTextFieldProcedureName.setColumns(10);
        jTextFieldProcedureParameters.setEnabled(false);
        jTextFieldProcedureParameters.setText("");
        jTextFieldProcedureParameters.setColumns(20);
        jPanelProcedureNameParam.setMaximumSize(new Dimension(500, 30));
        jPanelProcedureNameParam.setMinimumSize(new Dimension(500, 30));
        jPanelProcedureNameParam.setPreferredSize(new Dimension(500, 30));
        jButtonProcedureUpdate.setEnabled(false);
        jButtonProcedureUpdate.setText("Update Current Procedure");
        jButtonProcedureUpdate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonProcedureUpdate_actionPerformed(e);
            }
        });
        jButtonProcedureRemove.setEnabled(false);
        jButtonProcedureRemove.setText("Remove Current Procedure");
        jButtonProcedureRemove.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonProcedureRemove_actionPerformed(e);
            }
        });
        jButtonProcedureAdd.setEnabled(false);
        jButtonProcedureAdd.setText("Add New Procedure:");
        jButtonProcedureAdd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonProcedureAdd_actionPerformed(e);
            }
        });

        jPanelDerivative.setBorder(titledBorder10);
        jPanelDerivative.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentHidden(ComponentEvent e)
            {
                jPanelDerivative_componentHidden(e);
            }
        });
        jPanelDerivative.setLayout(borderLayout19);
        jButtonDerivativeParseAndSave.setEnabled(false);
        jButtonDerivativeParseAndSave.setText("Parse and Save");
        jButtonDerivativeParseAndSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDerivativeParseAndSave_actionPerformed(e);
            }
        });
        jTextAreaDerivative.setEnabled(false);
        jTextAreaDerivative.setBorder(border16);
        jTextAreaDerivative.setText("");
        jTextAreaDerivative.setColumns(40);
        jTextAreaDerivative.setRows(20);
        jTextAreaDerivative.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                jTextAreaDerivative_keyPressed(e);
            }
        });
        jLabelDerivativeName.setText("Derivative name:");
        jTextFieldDerivaiveName.setEnabled(false);
        jTextFieldDerivaiveName.setText("");
        jTextFieldDerivaiveName.setColumns(12);
        jTextFieldDerivaiveName.addKeyListener(new java.awt.event.KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                jTextFieldDerivaiveName_keyPressed(e);
            }
        });
        jComboBoxFunctionList.setEnabled(false);
        jComboBoxProcedureList.setEnabled(false);

        jPanelNetReceive.setBorder(border19);
        jPanelNetReceive.setLayout(borderLayout20);

        jButtonNetReceiveParseAndSave.setText("Parse and Save");
        jButtonNetReceiveParseAndSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNetReceiveParseAndSave_actionPerformed(e);
            }
        });

        jTextAreaNetReceive.setBorder(border20);
        jTextAreaNetReceive.setColumns(40);
        jTextAreaNetReceive.setRows(20);
        jPanelDerivative.add(jPanelDerivativeControls,  BorderLayout.SOUTH);
        jPanelDerivativeControls.add(jButtonDerivativeParseAndSave, null);
        jPanelUnitSummary.add(jLabelUnitSelectedName,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));
        jPanelUnitSummary.add(jTextFieldUnitShortName,    new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 0, 0));

        jPanelUnitSummary.add(jLabelUnitFullName,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));
        jPanelUnitSummary.add(jTextFieldUnitFullName,    new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 96, 0));

        jPanelUnitSummary.add(jLabelUnitComment,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));
        jPanelUnitSummary.add(jTextFieldUnitComment,    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 96, 0));


        jPanelStateSummary.setLayout(new GridBagLayout());
        jLabelStateName.setText("State variable name:");
        jPanelStateSummary.add(jLabelStateName,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelStateSummary.add(jTextFieldStateName,    new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 0, 0));
        jLabelStateDim.setText("Dimension:");
        jPanelStateSummary.add(jLabelStateDim,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelStateSummary.add(jTextFieldStateDim,    new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 96, 0));
        jLabelStateComment.setText("Comment:");
        jPanelStateSummary.add(jLabelStateComment,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelStateSummary.add(jTextFieldStateComment,    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 96, 0));




        jPanelAssignedSummary.setLayout(new GridBagLayout());
        jLabelAssignedName.setText("Assigned variable name:");
        jPanelAssignedSummary.add(jLabelAssignedName,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelAssignedSummary.add(jTextFieldAssignedName,    new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 0, 0));
        jLabelAssignedDim.setText("Dimension:");
        jPanelAssignedSummary.add(jLabelAssignedDim,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelAssignedSummary.add(jTextFieldAssignedDim,    new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 96, 0));
        jLabelAssignedComment.setText("Comment:");
        jPanelAssignedSummary.add(jLabelAssignedComment,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelAssignedSummary.add(jTextFieldAssignedComment,    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 96, 0));
        jPanelAssignedControls.add(jPanelAssignedRadioButtons,  BorderLayout.CENTER);




        jPanelParametersSummary.setLayout(new GridBagLayout());
        jLabelParametersName.setText("Parameter name:");
        jPanelParametersSummary.add(jLabelParametersName,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelParametersSummary.add(jTextFieldParametersName,    new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 0, 0));


        jLabelParametersValue.setText("Value:");
        jPanelParametersSummary.add(jLabelParametersValue,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelParametersSummary.add(jTextFieldParametersValue,    new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 96, 0));


        jLabelParametersDim.setText("Dimension:");
        jPanelParametersSummary.add(jLabelParametersDim,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelParametersSummary.add(jTextFieldParametersDim,    new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 96, 0));


        jLabelParametersComment.setText("Comment:");
        jPanelParametersSummary.add(jLabelParametersComment,    new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 6));

        jPanelParametersSummary.add(jTextFieldParametersComment,    new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 96, 0));
        jPanelParametersControls.add(jPanelParametersRadioButtons,  BorderLayout.CENTER);
        jPanelParametersRadioButtons.add(jRadioButtonParametersNoRef, null);
        jPanelParametersRadioButtons.add(jRadioButtonParametersRange, null);
        jPanelParametersRadioButtons.add(jRadioButtonParametersGlobal, null);




        jPanelUnitsControls.add(jPanelUnitButtons,  BorderLayout.SOUTH);
        contentPane.add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelMain.add(jPanelEditor, BorderLayout.CENTER);
        jPanelButtons.add(jButtonNew, null);
        jPanelButtons.add(jButtonLoad, null);
        jPanelButtons.add(jButtonSave, null);
        jPanelButtons.add(jButtonTestUnits, null);
        jPanelButtons.add(jButtonCompile, null);
        jPanelButtons.add(jButtonRunExample, null);
        jPanelEditor.add(jTabbedPane1,  BorderLayout.CENTER);
        jTabbedPane1.add(jPanelMainSettings,    NEURON_TAB);
        jPanelMainSettings.add(jPanelMainImage, null);
        jPanelMainImage.add(jLabelImage, null);
        jPanelMainSettings.add(jPanelNameTitle, null);
        jPanelNameTitle.add(jLabelFileName,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 30, 6, 0), 57, 6));
        jPanelNameTitle.add(jTextFieldFileName,           new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 6, 30), 400, 0));
        jPanelNameTitle.add(jLabelTitle,      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 30, 14, 0), 125, 6));
        jPanelNameTitle.add(jTextFieldTitle,           new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 10, 14, 30), 400, 0));
        jPanelMainSettings.add(jPanelNeuronBlock, null);

        jTabbedPane1.add(jPanelParameters, PARAMETERS_TAB);

        jPanelParameters.add(jPanelParametersControls,  BorderLayout.CENTER);
        jPanelParametersControls.add(jPanelParametersButtons,  BorderLayout.SOUTH);
        jPanelParametersButtons.add(jButtonParametersUpdate, null);
        jPanelParametersButtons.add(jButtonParameterRemove, null);
        jPanelParametersButtons.add(jButtonParameterAdd, null);
        jPanelParametersControls.add(jPanelParametersSummary, BorderLayout.NORTH);
        jPanelParameters.add(jScroolPaneParameters,  BorderLayout.WEST);

        jTabbedPane1.add(jPanelAssigned, ASSIGNED_TAB);
        jPanelAssigned.add(jPanelAssignedControls,  BorderLayout.CENTER);
        jPanelAssignedControls.add(jPanelAssignedButtons,  BorderLayout.SOUTH);
        jPanelAssignedButtons.add(jButtonAssignedUpdate, null);
        jPanelAssignedButtons.add(jButtonAssignedRemove, null);
        jPanelAssignedButtons.add(jButtonAssignedAdd, null);
        jPanelAssignedControls.add(jPanelAssignedSummary, BorderLayout.NORTH);
        jPanelAssigned.add(jScroolPaneAssigned,  BorderLayout.WEST);

        jTabbedPane1.add(jPanelState, STATE_TAB);
        jPanelState.add(jPanelStateControls,  BorderLayout.CENTER);
        jPanelStateControls.add(jPanelStateButtons,  BorderLayout.SOUTH);
        jPanelStateButtons.add(jButtonStateUpdate, null);
        jPanelStateButtons.add(jButtonStateRemove, null);
        jPanelStateButtons.add(jButtonStateAdd, null);
        jPanelStateControls.add(jPanelStateSummary, BorderLayout.NORTH);
        jPanelState.add(jScroolPaneState,  BorderLayout.WEST);
        jTabbedPane1.add(jPanelUnits, UNITS_TAB);
        jTabbedPane1.add(jPanelInitial, INITIAL_TAB);

        jTabbedPane1.add(jPanelDerivative, DERIVATIVE_TAB);
        jPanelDerivative.add(jPanelDerivativeEditor, BorderLayout.CENTER);
        jPanelDerivativeEditor.add(jTextAreaDerivative, null);
        jPanelDerivative.add(jPanelDerivativeName, BorderLayout.NORTH);
        jPanelDerivativeName.add(jLabelDerivativeName, null);
        jPanelDerivativeName.add(jTextFieldDerivaiveName, null);


        jPanelInitial.add(jPanelInitialControls,  BorderLayout.SOUTH);
        jPanelInitialControls.add(jButtonInitialParseAndSave, null);
        jPanelInitial.add(jPanelInitialEditor, BorderLayout.NORTH);
        jPanelInitialEditor.add(jTextAreaInitial, null);
        jTabbedPane1.add(jPanelBreakpoint, BREAKPOINT_TAB);
        jTabbedPane1.add(jPanelFunctions,  FUNCTIONS_TAB);
        jTabbedPane1.add(jPanelProcedures, PROCEDURES_TAB);
        jPanelProcedures.add(jPanelProcedureControls,  BorderLayout.SOUTH);
        jPanelProcedureControls.add(jButtonProcedureUpdate, null);
        jPanelProcedureControls.add(jButtonProcedureRemove, null);
        jPanelProcedures.add(jPanelProcedureEditor, BorderLayout.CENTER);
        jPanelProcedureEditor.add(jPanelProcedureNameParam, null);
        jPanelProcedureEditor.add(jTextAreaProcedure, null);
        jPanelProcedureNameParam.add(jLabelProcedureName, null);
        jPanelProcedureNameParam.add(jTextFieldProcedureParameters, null);
        jPanelProcedures.add(jPanelProcedureSelection,  BorderLayout.NORTH);
        jPanelProcedureSelection.add(jLabelProcedureMain, null);
        jPanelProcedureSelection.add(jComboBoxProcedureList, null);
        jPanelProcedureSelection.add(jButtonProcedureAdd, null);
        jPanelProcedureSelection.add(jTextFieldProcedureName, null);
        jPanelFunctions.add(jPanelFunctionSelection,  BorderLayout.NORTH);
        jPanelFunctionSelection.add(jLabelFunctionSelect, null);
        jPanelFunctionSelection.add(jComboBoxFunctionList, null);
        jPanelFunctionSelection.add(jButtonFunctionAdd, null);
        jPanelFunctionSelection.add(jTextFieldFunctionNewName, null);
        jPanelFunctions.add(jPanelFunctionEditor, BorderLayout.CENTER);
        jPanelFunctionEditor.add(jPanelFunctionNameParams, null);
        jPanelFunctionNameParams.add(jLabelFunctionName, null);
        jPanelFunctionNameParams.add(jTextFieldFunctionParameters, null);
        jPanelFunctionEditor.add(jPanelFunctionNameParams, null);
        jPanelFunctionEditor.add(jTextAreaFunction, null);
        jPanelFunctions.add(jPanelFunctionControls,  BorderLayout.SOUTH);
        jPanelBreakpoint.add(jPanelBreakpointControls,  BorderLayout.SOUTH);
        jPanelBreakpointControls.add(jButtonBreakpointParseAndSave, null);
        jPanelBreakpoint.add(jPanelBreakpointEditor, BorderLayout.CENTER);
        jPanelBreakpointEditor.add(jTextAreaBreakpoint, null);
        //jTabbedPane1.add(jPanelIndependent,  INDEPENDENT_TAB);

        jTabbedPane1.add(jPanelNetReceive, NET_RECEIVE_TAB);
        jPanelNetReceive.add(jPanelNetReceiveControls,  BorderLayout.SOUTH);
        jPanelNetReceiveControls.add(jButtonNetReceiveParseAndSave, null);
        jPanelNetReceive.add(jPanelNetReceiveEditor, BorderLayout.CENTER);
        jPanelNetReceiveEditor.add(jTextAreaNetReceive, null);
        jTabbedPane1.add(jPanelViewAsText, VIEW_AS_TEXT_TAB);

        JViewport jViewportEditorPanel = jScroolPaneEditorPane.getViewport();
        jViewportEditorPanel.add(jEditorPaneMain);
        jPanelViewAsText.add(jScroolPaneEditorPane,  BorderLayout.CENTER);




        jPanelNeuronBlock.add(jPanelNeuronType, null);

        jPanelNeuronCurrent.add(jLabelNeuronCurrent, null);
        jPanelNeuronCurrent.add(jTextFieldNeuronCurrent, null);
        jPanelNeuronCurrent.add(jRadioButtonNoCurrent, null);
        jPanelNeuronCurrent.add(jRadioButtonElectrode, null);
        jPanelNeuronCurrent.add(jRadioButtonNonSpecific, null);

        jPanelNeuronType.add(jLabelNeuronType, null);
        jPanelNeuronType.add(jTextFieldNeuronTypeName, null);
        jPanelNeuronType.add(jRadioButtonPointProcess, null);
        jPanelNeuronType.add(jRadioButtonDensityMechnaism, null);
        jPanelNeuronBlock.add(jPanelNeuronCurrent, null);
        jPanelNeuronBlock.add(jPanelNeuronParams, null);
        jPanelUnits.add(jPanelUnitsControls, BorderLayout.CENTER);


        //jPanelUnits.add(jListUnits,  BorderLayout.WEST);

        JViewport jViewportUnits = jScroolPaneUnits.getViewport();
        JViewport jViewportParameters = jScroolPaneParameters.getViewport();
        JViewport jViewportState = jScroolPaneState.getViewport();
        JViewport jViewportAssigned = jScroolPaneAssigned.getViewport();
        /** @todo Solve the non working of scroolbars */
        //jScroolPaneUnits.setVerticalScrollBarPolicy(jScroolPaneUnits.VERTICAL_SCROLLBAR_ALWAYS);
        jViewportUnits.add(jListUnits);
        jViewportParameters.add(jListParameters);
        jViewportState.add(jListState);
        jViewportAssigned.add(jListAssigned);
        jPanelUnits.add(jScroolPaneUnits,  BorderLayout.WEST);
        jPanelParameters.add(jScroolPaneParameters,  BorderLayout.WEST);
        jPanelState.add(jScroolPaneState,  BorderLayout.WEST);
        jPanelAssigned.add(jScroolPaneAssigned,  BorderLayout.WEST);

        jPanelUnitButtons.add(jButtonUnitUpdate, null);
        jPanelUnitButtons.add(jButtonUnitRemove, null);
        jPanelUnitButtons.add(jButtonUnitAdd, null);

        jPanelUnitsControls.add(jPanelUnitSummary,  BorderLayout.NORTH);
        buttonGroupPointOrDensity.add(jRadioButtonPointProcess);
        buttonGroupPointOrDensity.add(jRadioButtonDensityMechnaism);
        buttonGroupCurrent.add(jRadioButtonNoCurrent);
        buttonGroupCurrent.add(jRadioButtonElectrode);
        buttonGroupCurrent.add(jRadioButtonNonSpecific);
        buttonGroupParameters.add(jRadioButtonParametersNoRef);
        buttonGroupParameters.add(jRadioButtonParametersRange);
        buttonGroupParameters.add(jRadioButtonParametersGlobal);
        jPanelNeuronParams.add(jPanelNeuronRange, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanelNeuronRange.add(jListNeuronGlobal, null);
        jPanelNeuronParams.add(jPanelNeuronGlobal,      new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        jPanelNeuronGlobal.add(jListNeuronRange, null);
        jPanelNeuronParams.add(jLabelNeuronGlobal,        new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));
        jPanelNeuronParams.add(jLabelNeuronRange,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));
        jPanelAssignedRadioButtons.add(jRadioButtonAssignedNoRef, null);
        jPanelAssignedRadioButtons.add(jRadioButtonAssignedRange, null);
        jPanelAssignedRadioButtons.add(jRadioButtonAssignedGlobal, null);
        buttonGroupAssigned.add(jRadioButtonAssignedNoRef);
        buttonGroupAssigned.add(jRadioButtonAssignedRange);
        buttonGroupAssigned.add(jRadioButtonAssignedGlobal);
        jPanelFunctionControls.add(jButtonFunctionUpdate, null);
        jPanelFunctionControls.add(jButtonFunctionRemove, null);
        jPanelFunctionNameParams.add(jLabelFunctionName, null);
        jPanelFunctionNameParams.add(jTextFieldFunctionParameters, null);

        jLabelImage.setIcon(imageMain);

    }


    /**
     * Overridden so we can exit when window is closed
     * @param e window related event
     */
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            logger.logComment("Window closing...");
            boolean continueWithAction = checkToSaveAndContinue();
            logger.logComment("Continue? "+continueWithAction);
            if (continueWithAction)
            {
                super.processWindowEvent(e);
                recentFiles.saveToFile();
                if (standAlone)
                {
                    GeneralProperties.saveToSettingsFile();
                    System.exit(0);
                }
            }
            else return;
        }
    }


    /**
     * Non JBuilder related initialisation stuff
     */
    private void extraInit()
    {
        jComboBoxFunctionList.addItem(defaultFunctionText);
        jComboBoxProcedureList.addItem(defaultProcedureText);

        addNamedDocumentListener(NEURON_TAB, jTextFieldTitle);
        addNamedDocumentListener(NEURON_TAB, jTextFieldNeuronCurrent);
        addNamedDocumentListener(NEURON_TAB, jTextFieldNeuronTypeName);
    }


    /**
     * Load tool tips from the help file. Lots more work needed here...
     */
    private void setToolTips()
    {
        ToolTipHelper toolTipText = ToolTipHelper.getInstance();

        //jPanelMainSettings.setToolTipText(toolTipText.getToolTip("Neuron Tab"));
        jPanelParameters.setToolTipText(toolTipText.getToolTip("Parameters Tab"));

        // NEURON TABs

        jTextFieldTitle.setToolTipText(toolTipText.getToolTip("Neuron Tab Title"));
      //  jLabelTitle.setToolTipText(toolTipText.getToolTip("Title"));

      jTextFieldNeuronCurrent.setToolTipText(toolTipText.getToolTip("Neuron Tab Curr Variable"));
      jListNeuronGlobal.setToolTipText(toolTipText.getToolTip("Neuron Tab Global Variable"));
      jListNeuronRange.setToolTipText(toolTipText.getToolTip("Neuron Tab Range Variable"));

    }


    /**
     * When a user is closing down, loading a file or creating a new one, checks
     * if the current file needs saving, or if the user has changed their mind
     * @return True if the file doesn't need saving, or the user agrees to save
     * or false if the user changes their mind
     */
    private boolean checkToSaveAndContinue()
    {
        if (currentModFile == null ||
            currentModFile.getStatus() != ModFile.FILE_EDITED_NOT_SAVED)
        {
            this.refreshAll();
            this.jTabbedPane1.setSelectedIndex(0);
            return true;
        }
        Object[] options = { "Save", "Don't save", "Cancel" };

        JOptionPane option = new JOptionPane(
            "The NMODL file: " + currentModFile.getFileName() +
            " has been altered. Save?",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]);


        JDialog dialog = option.createDialog(this, "Save file?");
        dialog.setVisible(true);

        Object choice = option.getValue();
        logger.logComment("User has chosen: " + choice);
        if (choice.equals("Cancel"))
        {
            logger.logComment("User has changed their mind...");
            return false;
        }
        else if (choice.equals("Save"))
        {
            logger.logComment("User has decided to save...");
            try
            {
                currentModFile.saveToFile();
            }
            catch (ModFileException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem saving file", ex, this);
                return false;
            }

        }
        else
        {
            logger.logComment("User has decided not to save...");
        }
        this.refreshAll();
        this.jTabbedPane1.setSelectedIndex(0);
        return true;

    }



    private void enableGUIsOnActiveModFile()
    {
        this.jButtonCompile.setEnabled(true);
        //this.jButtonRunExample.setEnabled(true);
        this.jButtonTestUnits.setEnabled(true);
        this.jButtonSave.setEnabled(true);
        this.jTextFieldTitle.setEditable(true);
        this.jTextFieldTitle.setEnabled(true);
        this.jTextFieldUnitFullName.setEditable(true);
        this.jTextFieldUnitShortName.setEditable(true);
        this.jTextFieldUnitComment.setEditable(true);
        this.jButtonUnitRemove.setEnabled(true);
        this.jButtonUnitUpdate.setEnabled(true);
        this.jButtonInitialParseAndSave.setEnabled(true);
        this.jButtonBreakpointParseAndSave.setEnabled(true);

        this.jTextFieldNeuronTypeName.setEditable(true);
        this.jTextFieldNeuronCurrent.setEditable(true);

        this.jTextFieldParametersComment.setEditable(true);
        this.jTextFieldParametersDim.setEditable(true);
        this.jTextFieldParametersName.setEditable(true);
        this.jTextFieldParametersValue.setEditable(true);
        this.jButtonParameterAdd.setEnabled(true);
        this.jButtonParameterRemove.setEnabled(true);
        this.jButtonParametersUpdate.setEnabled(true);



        this.jTextFieldAssignedComment.setEditable(true);
        this.jTextFieldAssignedDim.setEditable(true);
        this.jTextFieldAssignedName.setEditable(true);
        this.jButtonAssignedAdd.setEnabled(true);
        this.jButtonAssignedRemove.setEnabled(true);
        this.jButtonAssignedUpdate.setEnabled(true);

        this.jTextFieldStateComment.setEditable(true);
        this.jTextFieldStateDim.setEditable(true);
        this.jTextFieldStateName.setEditable(true);
        this.jButtonStateAdd.setEnabled(true);
        this.jButtonStateRemove.setEnabled(true);
        this.jButtonStateUpdate.setEnabled(true);

        this.jRadioButtonDensityMechnaism.setEnabled(true);
        this.jRadioButtonElectrode.setEnabled(true);
        this.jRadioButtonNoCurrent.setEnabled(true);
        this.jRadioButtonNonSpecific.setEnabled(true);
        this.jRadioButtonPointProcess.setEnabled(true);
        this.jRadioButtonParametersNoRef.setEnabled(true);
        this.jRadioButtonParametersGlobal.setEnabled(true);
        this.jRadioButtonParametersRange.setEnabled(true);

        this.jTextAreaInitial.setEnabled(true);
        this.jButtonInitialParseAndSave.setEnabled(true);

        this.jTextAreaDerivative.setEnabled(true);
        this.jTextFieldDerivaiveName.setEnabled(true);
        this.jButtonDerivativeParseAndSave.setEnabled(true);

        this.jTextAreaBreakpoint.setEnabled(true);
        this.jButtonBreakpointParseAndSave.setEnabled(true);

        this.jComboBoxFunctionList.setEnabled(true);
        this.jTextFieldFunctionParameters.setEnabled(true);
        this.jTextAreaFunction.setEnabled(true);
        this.jButtonFunctionAdd.setEnabled(true);
        this.jButtonFunctionRemove.setEnabled(true);
        this.jButtonFunctionUpdate.setEnabled(true);
        this.jTextFieldFunctionNewName.setEnabled(true);

        this.jComboBoxProcedureList.setEnabled(true);
        this.jTextFieldProcedureParameters.setEnabled(true);
        this.jTextAreaProcedure.setEnabled(true);
        this.jButtonProcedureAdd.setEnabled(true);
        this.jButtonProcedureRemove.setEnabled(true);
        this.jButtonProcedureUpdate.setEnabled(true);
        this.jTextFieldProcedureName.setEnabled(true);

        this.jButtonNetReceiveParseAndSave.setEnabled(true);

        this.jMenuSave.setEnabled(true);
        this.jMenuSaveAs.setEnabled(true);


    }


    /**
     * Set which modfile to edit
     * @param modFileName The *.mod file to ne edited
     */
    public void editModFile(String modFileName, boolean readonly)
    {
        try
        {
            this.checkToSaveAndContinue();
            logger.logComment("***** Being told to edit file: "+ modFileName);
            initialisingModFile = true;
            currentModFile = new ModFile(modFileName);

            if (!readonly) enableGUIsOnActiveModFile();


                recentFiles.addToList(currentModFile.getCurrentFile().getAbsolutePath());

        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing the file: "+ ex.getMessage(), ex, this);
        }

        refreshAll();
        initialisingModFile = false;
    }


    void jButtonLoad_actionPerformed(ActionEvent e)
    {
        logger.logComment("Load button pressed...");
        this.doLoadModFile();
    }

    void doLoadModFile()
    {
        if (currentModFile!=null)
        {
            dirToCheckOnLoad = currentModFile.getCurrentFile().getParentFile();
        }
        boolean continueWithAction = checkToSaveAndContinue();

        if (!continueWithAction) return;

        try
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);

            SimpleFileFilter fileFilter = new SimpleFileFilter(new String[]{".mod"}, "NMODL files, extension: *.mod");

            chooser.setFileFilter(fileFilter);
            String selectedFileName = null;


            chooser.setCurrentDirectory(dirToCheckOnLoad);
            logger.logComment("Set Dialog dir to: " +
                              dirToCheckOnLoad.getAbsolutePath());

            int retval = chooser.showDialog(this, null);

            if (retval == JFileChooser.APPROVE_OPTION)
            {
                logger.logComment("User approved...");
                selectedFileName = chooser.getSelectedFile().getAbsolutePath();

                initialisingModFile = true;

                currentModFile = new ModFile(selectedFileName);


                logger.logComment("Test: "+FormattingChecker.checkIfVariableIsGlobal("tt", currentModFile));


                recentFiles.addToList(currentModFile.getCurrentFile().getAbsolutePath());

                enableGUIsOnActiveModFile();
            }
            else
            {
                logger.logComment("User changed their mind...");
            }
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing the file: "+ ex.getMessage(), ex, this);
            initialisingModFile = false;
        }
        refreshAll();
        initialisingModFile = false;
    }


    private void refreshAll()
    {
        if (currentModFile!=null)
        {
            refreshGeneral();
            refreshMenus();
            refreshNeuronTab();

            refreshUnitsTab();
            refreshAssignedTab();
            refreshStateTab();
            refreshParametersTab();
            refreshInitialTab();
            refreshDerivativeTab();
            refreshBreakpointTab();
            refreshFunctionsTab();
            refreshProceduresTab();
            refreshNetReceiveTab();
            refreshViewAsTextTab();
        }
    }


    private void refreshGeneral()
    {
        if (currentModFile.getStatus() == ModFile.FILE_EDITED_NOT_SAVED)
        {
            this.setTitle(titleString + ": " + currentModFile.getFullFileName() + "*");
        }
        else
        {
            this.setTitle(titleString + ": " + currentModFile.getFullFileName());
        }

    }


    private void markAsEdited()
    {
        this.currentModFile.modFileChanged();;
        refreshGeneral();
    }


    private void setVectorAsDataModelForJList(Vector vec, JList jList)
    {
        class AbstractListModelHelper extends AbstractListModel
        {
            Vector v = null;
            AbstractListModelHelper(Vector v){this.v = v;};
            public int getSize() {  return v.size(); }
            public Object getElementAt(int i) { return v.elementAt(i); }
        };

        AbstractListModelHelper model = new AbstractListModelHelper(vec);

        jList.setModel(model);


    }

    private void refreshMenus()
    {
        logger.logComment("Refreshing the menus...");

        String[ ] recentFileList = recentFiles.getFileNames();

        if (recentFileList.length == 0)
        {
            logger.logComment("No recent files found...");
            return;
        }

        jMenuFile.removeAll();
        jMenuFile.add(jMenuNew);
        jMenuFile.add(jMenuLoad);
        jMenuFile.add(jMenuSave);
        jMenuFile.add(jMenuSaveAs);

        jMenuFile.addSeparator();

        for (int i = 0; i < recentFileList.length; i++)
        {
            JMenuItem jMenuRecentFileItem = new JMenuItem();
            jMenuRecentFileItem.setText(recentFileList[i]);
            jMenuFile.add(jMenuRecentFileItem);

            jMenuRecentFileItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    jMenuRecentFile_actionPerformed(e);
                }
            });

        }

        jMenuFile.addSeparator();
        jMenuFile.add(jMenuFileExit);

    }


    private void refreshNeuronTab()
    {
        if (currentModFile!=null && currentModFile.myNeuronElement!=null)
        {
            if (initialisingModFile)
            {
                jTextFieldFileName.setText(currentModFile.getFileName());
                jTextFieldTitle.setText(currentModFile.getTitle());
                jTextFieldNeuronTypeName.setText(currentModFile.myNeuronElement.getProcessName());


                if (currentModFile.myNeuronElement.getProcess() == NeuronElement.POINT_PROCESS)
                    jRadioButtonPointProcess.setSelected(true);
                else jRadioButtonDensityMechnaism.setSelected(true);


                String currName = currentModFile.myNeuronElement.getCurrentVariable();
                if (currName != null)
                {
                    jTextFieldNeuronCurrent.setText(currName);
                    if (currentModFile.myNeuronElement.getCurrentReference() == NeuronElement.NONSPECIFIC_CURRENT)
                    {
                        jRadioButtonNonSpecific.setSelected(true);
                        jTextFieldNeuronCurrent.setEnabled(true);
                    }
                    else if (currentModFile.myNeuronElement.getCurrentReference() == NeuronElement.ELECTRODE_CURRENT)
                    {
                        jRadioButtonElectrode.setSelected(true);
                        jTextFieldNeuronCurrent.setEnabled(true);
                    }
                    else
                    {
                        jRadioButtonNoCurrent.setSelected(true);
                        jTextFieldNeuronCurrent.setEnabled(false);
                    }

                }
                else
                {
                    jTextFieldNeuronCurrent.setText("");

                    jRadioButtonNoCurrent.setSelected(true);
                }


            }

            Vector rangeVars = currentModFile.myNeuronElement.myRangeVariables.myVariableNames;
            setVectorAsDataModelForJList(rangeVars, jListNeuronRange);
            Vector globalVars = currentModFile.myNeuronElement.myGlobalVariables.myVariableNames;
            setVectorAsDataModelForJList(globalVars, jListNeuronGlobal);


            logger.logComment(jListNeuronRange.getModel().getSize()+" range variables"); ;



        }

    }


    private void refreshUnitsTab()
    {
        listModelUnits.clear();
        if (currentModFile!=null && currentModFile.myUnitsElement!=null)
        {
            java.util.List units = currentModFile.myUnitsElement.getUnits();
            Iterator unitIterator = units.iterator();

            while (unitIterator.hasNext())
            {
                UnitsElement.UnitEntry unit = (UnitsElement.UnitEntry)unitIterator.next();
                listModelUnits.addElement(unit);
                logger.logComment("Just added unit: "+ unit );
            }
        }
        else
        {
            logger.logComment("No units to add...");
        }
        if (listModelUnits.getSize()>0)
        {
            jListUnits.setSelectedIndex(0);
        }
        else // if there's no variables
        {
            jTextFieldUnitComment.setText("");
            jTextFieldUnitFullName.setText("");
            jTextFieldUnitShortName.setText("");
        }

    }


    private void refreshAssignedTab()
    {
        listModelAssigned.clear();
        if (currentModFile!=null && currentModFile.myAssignedElement!=null)
        {
            java.util.List assignedVars = currentModFile.myAssignedElement.getNamedVariables();
            Iterator iter = assignedVars.iterator();

            while (iter.hasNext())
            {
                NamedVariableElement.NamedVariableEntry namedVar = (NamedVariableElement.NamedVariableEntry)iter.next();
                listModelAssigned.addElement(namedVar);
                //logger.logComment("Just added namedVar: "+ namedVar );
            }
        }
        else
        {
            logger.logComment("No namedVars to add...");
        }
        if (listModelAssigned.getSize()>0)
        {
            jListAssigned.setSelectedIndex(0);
        }
        else // if there's no variables
        {
            jTextFieldAssignedComment.setText("");
            jTextFieldAssignedDim.setText("");
            jTextFieldAssignedName.setText("");
        }
    }


    private void refreshStateTab()
    {
        listModelState.clear();
        if (currentModFile!=null && currentModFile.myStateElement!=null)
        {
            java.util.List stateVars = currentModFile.myStateElement.getNamedVariables();
            Iterator iter = stateVars.iterator();

            while (iter.hasNext())
            {
                NamedVariableElement.NamedVariableEntry namedVar = (NamedVariableElement.NamedVariableEntry)iter.next();
                listModelState.addElement(namedVar);
                //logger.logComment("Just added stateVar: "+ namedVar );
            }
        }
        else
        {
            logger.logComment("No stateVars to add...");
        }
        if (listModelState.getSize()>0)
        {
            jListState.setSelectedIndex(0);
        }
        else
        {
            jTextFieldStateComment.setText("");
            jTextFieldStateDim.setText("");
            jTextFieldStateName.setText("");

        }
    }


    private void refreshParametersTab()
    {
        listModelParameters.clear();
        if (currentModFile!=null && currentModFile.myParametersElement!=null)
        {
            java.util.List paramVars = currentModFile.myParametersElement.getParameters();
            Iterator iter = paramVars.iterator();

            while (iter.hasNext())
            {
                ParametersElement.ParameterEntry paramVar = (ParametersElement.ParameterEntry)iter.next();
                listModelParameters.addElement(paramVar);
                //logger.logComment("Just added paramVar: "+ paramVar );
            }
        }
        else
        {
            logger.logComment("No paramVars to add...");
        }
        if (listModelParameters.getSize()>0)
        {
            jListParameters.setSelectedIndex(0);
        }
        else
        {
            jTextFieldParametersComment.setText("");
            jTextFieldParametersDim.setText("");
            jTextFieldParametersName.setText("");
            jTextFieldParametersValue.setText("");
        }
    }


    private void refreshInitialTab()
    {
        if (!initialTextNeedsChecking) // if it needs checking, just leave it as it is...
        {
            String[] lines = currentModFile.myInitialElement.
                getUnformattedLines();
            if (lines == null)
                return;

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < lines.length; i++)
            {
                sb.append(lines[i] + "\n");
            }
            jTextAreaInitial.setText(sb.toString());
        }
    }


    private void refreshDerivativeTab()
    {
        if ((!derivativeTextNeedsChecking) && (currentModFile.myDerivativeElement!=null)) // if it needs checking, just leave it as it is...
       {
           String[] lines = currentModFile.myDerivativeElement.getUnformattedLines();
           if (lines == null)
               return;
           logger.logComment("Adding " + lines.length + " lines for the DERIVATIVE block");
           StringBuffer sb = new StringBuffer();

           for (int i = 0; i < lines.length; i++)
           {
               sb.append(lines[i] + "\n");
           }
           jTextAreaDerivative.setText(sb.toString());
           jTextFieldDerivaiveName.setText(currentModFile.myDerivativeElement.myDerivativeName);
       }

    }


    private void refreshBreakpointTab()
    {
        if (!breakpointTextNeedsChecking) // if it needs checking, just leave it as it is...
        {
            String[] lines = currentModFile.myBreakpointElement.getUnformattedLines();
            if (lines == null) return;
            logger.logComment("Adding " + lines.length + " lines for the BREAKPOINT block");
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < lines.length; i++)
            {
                sb.append(lines[i] + "\n");
            }
            jTextAreaBreakpoint.setText(sb.toString());
        }
    }


    private void refreshFunctionsTab()
    {
        if (!functionTextNeedsChecking)
        {
            logger.logComment("Refreshing the functions tag");
            int currentlySelected = jComboBoxFunctionList.getSelectedIndex();
            jComboBoxFunctionList.setEnabled(true);
            jComboBoxFunctionList.removeAllItems();
            Vector allFunctions = currentModFile.getFunctions();

            if (allFunctions.size() == 0)
            {
                jComboBoxFunctionList.addItem(defaultFunctionText);
                jTextAreaFunction.setEditable(false);
                jTextFieldFunctionParameters.setEditable(false);
            }
            else
            {
                for (int i = 0; i < allFunctions.size(); i++)
                {
                    FunctionElement funcEl = (FunctionElement) allFunctions.elementAt(i);
                    jComboBoxFunctionList.addItem(funcEl.myFunctionName);
                }
                if (currentlySelected < allFunctions.size()) jComboBoxFunctionList.setSelectedIndex(currentlySelected);
                    else jComboBoxFunctionList.setSelectedIndex(0);


                    jTextAreaFunction.setEditable(true);
                    jTextFieldFunctionParameters.setEditable(true);

            }
        }
        else
        {
            logger.logComment("Text has been altered, not updating yet...");
        }
    }


    private void refreshProceduresTab()
    {
        if (!procedureTextNeedsChecking)
        {
            logger.logComment("Refreshing the procedures tag");
            int currentlySelected = jComboBoxProcedureList.getSelectedIndex();
            jComboBoxProcedureList.setEnabled(true);
            jComboBoxProcedureList.removeAllItems();
            Vector allProcedures = currentModFile.getProcedures();

            if (allProcedures.size() == 0)
            {
                jComboBoxProcedureList.addItem(defaultProcedureText);

                jTextAreaProcedure.setEditable(false);
                jTextFieldProcedureParameters.setEditable(false);

            }
            else
            {
                for (int i = 0; i < allProcedures.size(); i++)
                {
                    ProcedureElement procEl = (ProcedureElement) allProcedures.elementAt(i);
                    jComboBoxProcedureList.addItem(procEl.myProcedureName);
                }
                if (currentlySelected < allProcedures.size())
                    jComboBoxProcedureList.setSelectedIndex(currentlySelected);
                else jComboBoxProcedureList.setSelectedIndex(0);

                jTextAreaProcedure.setEditable(true);
                jTextFieldProcedureParameters.setEditable(true);

            }
        }
        else
        {
            logger.logComment("Text has been altered, not updating yet...");
        }
    }


    private void refreshNetReceiveTab()
    {
        if (!netReceiveTextNeedsChecking) // if it needs checking, just leave it as it is...
        {
            String[] lines = currentModFile.myNetReceiveElement.getUnformattedLines();
            if (lines == null)
                return;

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < lines.length; i++)
            {
                sb.append(lines[i] + "\n");
            }
            jTextAreaNetReceive.setText(sb.toString());
        }
    }


    private void refreshViewAsTextTab()
    {
        jEditorPaneMain.setText(currentModFile.getFileContentsAsString());
    }


    void jButtonNew_actionPerformed(ActionEvent e)
    {
        logger.logComment("New button pressed");
        this.doNewModFile();
    }


    void doNewModFile()
    {
        boolean continueWithAction = checkToSaveAndContinue();

        if (!continueWithAction)return;

        try
        {
            NewFileDialog dlg = new NewFileDialog(this, "New file", true);
            if (!standAlone)
            {
                logger.logComment("Setting the suggested dir to: "+ projectMainDir);
                dlg.setSuggestedDir(projectMainDir);
            }
            else
            {
                logger.logComment("In stand alone mode...");
            }
            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);

            if (dlg.getCancelled())
            {
                logger.logComment("User cancelled the action");
                return;
            }

            String newFileName = dlg.getFullFileName();
            logger.logComment("Filename chosen: " + newFileName);
            int processType = dlg.getProcessType();
            String processName = dlg.getProcessName();


            File tempCheck = new File(newFileName);

            if (tempCheck.exists())
            {
                logger.logComment("The file " + tempCheck.getAbsolutePath() +
                                  " already exists");
                Object[] options = {"OK", "Cancel"};

                JOptionPane option = new JOptionPane(
                    "The file: " + tempCheck.getAbsolutePath() +
                    " already exists. Overwrite?",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);

                JDialog dialog = option.createDialog(this, "Warning");
                dialog.setVisible(true);

                Object choice = option.getValue();
                logger.logComment("User has chosen: " + choice);
                if (choice.equals("Cancel"))
                {
                    logger.logComment("User has changed their mind...");
                    return;
                }
            }
            else
            {
                logger.logComment("The file " + tempCheck.getAbsolutePath() +
                                  " doesn't already exist");
            }
            initialisingModFile = true;
            currentModFile = new ModFile(newFileName, processName, processType);

            recentFiles.addToList(currentModFile.getCurrentFile().getAbsolutePath());

            enableGUIsOnActiveModFile();
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing the file. Error: " + ex.getMessage(), ex, this);
        }
        refreshAll();
        initialisingModFile = false;
    }


    void jButtonSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Save button pressed...");
        this.doSaveModFile();
    }


    void doSaveModFile()
    {
        if (currentModFile==null) return;
        try
        {
            currentModFile.saveToFile();
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving file", ex, this);
        }

        refreshAll();
    }


    void doSaveAsModFile()
    {
        if (currentModFile==null) return;
        try
        {
            File currentParentDir = currentModFile.getCurrentFile().getParentFile();
            JFileChooser jFileChooser1 = new JFileChooser(currentParentDir);

            jFileChooser1.setFileFilter(new SimpleFileFilter(new String[]{".mod"},"NMODL files. Extension: *.mod"));

            logger.logComment("Presenting Save As dialog...");

            int choice = jFileChooser1.showSaveDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION )
            {

                String selectedFile = jFileChooser1.getSelectedFile().getPath();
                logger.logComment("Selected file is: "+ selectedFile);
                if (!selectedFile.endsWith(".mod"))
                {
                    selectedFile = new String(selectedFile + ".mod");
                }
                currentModFile.saveAs(selectedFile);
            }
            else
            {
                logger.logComment("User changed their minds...");
            }



        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem saving file", ex, this);
        }
        refreshAll();
    }



    void jPanelViewAsText_componentShown(ComponentEvent e)
    {
        //logger.logComment("View being switched to text representation...");

        if (initialTextNeedsChecking)
        {
            GuiUtils.showErrorMessage(logger, "INITIAL block not properly formatted", null, this);
            jTabbedPane1.setSelectedComponent(jPanelInitial);
            return;
        }
        if (breakpointTextNeedsChecking)
        {
            GuiUtils.showErrorMessage(logger, "BREAKPOINT block not properly formatted", null, this);
            jTabbedPane1.setSelectedComponent(jPanelBreakpoint);
            return;
        }


        refreshAll();
        jEditorPaneMain.setCaretPosition(0);
    }





    void jButtonUnitRemove_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        //listModelUnits.get
        int indexToBeRemoved = jListUnits.getSelectedIndex();
        UnitsElement.UnitEntry unit = (UnitsElement.UnitEntry)jListUnits.getSelectedValue();
        logger.logComment("Removing a unit: "+ unit);

        boolean success = currentModFile.myUnitsElement.removeUnit(unit);
        if (!success) GuiUtils.showErrorMessage(logger, "Could not remove element: "+ unit, null, this);

        refreshAll();

        if (indexToBeRemoved>0)
        {
            jListUnits.setSelectedIndex(indexToBeRemoved - 1);
        }
        else jListUnits.setSelectedIndex(0);
    }


    void jListUnits_valueChanged(ListSelectionEvent e)
    {
        logger.logComment("Units selection changed: "+e.toString());

        if (!e.getValueIsAdjusting() && currentModFile.myUnitsElement!=null)
        {
            logger.logComment("size: "+ jListUnits.getModel().getSize()); ;
            Object obj = jListUnits.getSelectedValue();
            if (obj!=null)
            {
                UnitsElement.UnitEntry unit = (UnitsElement.UnitEntry)obj;
                jTextFieldUnitShortName.setText(unit.simplifiedName);
                jTextFieldUnitFullName.setText(unit.realUnits);
                String comment = unit.commentOnUnit;
                if (comment!=null) jTextFieldUnitComment.setText(comment);
                    else jTextFieldUnitComment.setText("");
            }
            else
            {
                // i.e. one has just been removed...
                logger.logComment("Entry has been removed...");
            }
        }
    }

    void jListState_valueChanged(ListSelectionEvent e)
    {
        //logger.logComment("State selection changed: "+e.toString());

        if (!e.getValueIsAdjusting() && currentModFile.myStateElement != null)
        {
            //logger.logComment("size: " + jListState.getModel().getSize()); ;
            Object obj = jListState.getSelectedValue();
            if (obj != null)
            {
                StateElement.NamedVariableEntry var = (StateElement.NamedVariableEntry) obj;
                jTextFieldStateName.setText(var.name);
                jTextFieldStateDim.setText(var.dimension);
                String comment = var.comment;
                if (comment != null)
                    jTextFieldStateComment.setText(comment);
                else
                    jTextFieldStateComment.setText("");
            }
            else
            {
                // i.e. one has just been removed...
                logger.logComment("Entry has been removed...");
            }
        }
    }

    void jListParameters_valueChanged(ListSelectionEvent e)
    {
        //logger.logComment("Parameter selection changed: "+e.toString());

        if (!e.getValueIsAdjusting() && currentModFile.myParametersElement != null)
        {
            int size = jListParameters.getModel().getSize();
            logger.logComment("size of parameters list: " + size); ;
            if (size ==0) return;

            Object obj = jListParameters.getSelectedValue();
            if (obj != null)
            {
                ParametersElement.ParameterEntry var = (ParametersElement.ParameterEntry) obj;
                jTextFieldParametersName.setText(var.name);

                if (var.value!=Double.MIN_VALUE) jTextFieldParametersValue.setText(var.value+"");
                else jTextFieldParametersValue.setText("");

                if (var.dimension!=null) jTextFieldParametersDim.setText(var.dimension);
                else jTextFieldParametersDim.setText("");

                if (var.comment != null) jTextFieldParametersComment.setText(var.comment);
                else jTextFieldParametersComment.setText("");

                if (var.variableType == ParametersElement.ParameterEntry.GLOBAL_VARIABLE)
                    jRadioButtonParametersGlobal.setSelected(true);
                else if (var.variableType == ParametersElement.ParameterEntry.RANGE_VARIABLE)
                    jRadioButtonParametersRange.setSelected(true);
                else jRadioButtonParametersNoRef.setSelected(true);

            }
            else
            {
                // i.e. one has just been removed...
                logger.logComment("Entry has been removed...");
            }
        }

    }

    void jListAssigned_valueChanged(ListSelectionEvent e)
    {
        //logger.logComment("Assigned selection changed: "+e.toString());

        if (!e.getValueIsAdjusting() && currentModFile.myAssignedElement != null)
        {
            logger.logComment("size: " + jListAssigned.getModel().getSize()); ;
            Object obj = jListAssigned.getSelectedValue();
            if (obj != null)
            {
                AssignedElement.NamedVariableEntry var = (AssignedElement.NamedVariableEntry) obj;
                jTextFieldAssignedName.setText(var.name);
                jTextFieldAssignedDim.setText(var.dimension);
                String comment = var.comment;
                if (comment != null)
                    jTextFieldAssignedComment.setText(comment);
                else
                    jTextFieldAssignedComment.setText("");

                if (var.variableType == AssignedElement.NamedVariableEntry.GLOBAL_VARIABLE)
                    jRadioButtonAssignedGlobal.setSelected(true);
                else if (var.variableType == AssignedElement.NamedVariableEntry.RANGE_VARIABLE)
                    jRadioButtonAssignedRange.setSelected(true);
                else
                    jRadioButtonAssignedNoRef.setSelected(true);

            }
            else
            {
                // i.e. one has just been removed...
                logger.logComment("Entry has been removed...");
            }
        }

    }


    void jTextFieldUnitShortName_keyPressed(KeyEvent e)
    {
        jButtonUnitAdd.setEnabled(true);
    }


    void jButtonUnitAdd_actionPerformed(ActionEvent e)
    {
        //logger.logComment("Adding a new unit...");
        markAsEdited();
        boolean success = false;
        String comment = jTextFieldUnitComment.getText();

        try
        {
            if (!comment.trim().equals(""))
            {
                success = currentModFile.myUnitsElement.addUnit(
                    jTextFieldUnitShortName.getText(),
                    jTextFieldUnitFullName.getText(),
                    comment);
            }
            else
            {
                success = currentModFile.myUnitsElement.addUnit(
                    jTextFieldUnitShortName.getText(),
                    jTextFieldUnitFullName.getText());
            }
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(),ex, this);
            return;
        }

        if (!success) GuiUtils.showErrorMessage(logger, "Could not add unit of name: "+ jTextFieldUnitShortName.getText(), null, this);

        logger.logComment("Unit info: "+ currentModFile.myUnitsElement.toString());

        refreshAll();

    }

    void jButtonUnitUpdate_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeUpdated = jListUnits.getSelectedIndex();
        UnitsElement.UnitEntry unitAsIsNow = (UnitsElement.UnitEntry)jListUnits.getSelectedValue();
        logger.logComment("Updating a unit: "+ unitAsIsNow);

        boolean success = currentModFile.myUnitsElement.removeUnit(unitAsIsNow);
        if (!success)
        {
            GuiUtils.showErrorMessage(logger, "Could not update element: " + unitAsIsNow, null, this);
            return;
        }

        String comment = jTextFieldUnitComment.getText();

        try
        {
            if (!comment.trim().equals(""))
            {
                success = currentModFile.myUnitsElement.addUnit(
                    jTextFieldUnitShortName.getText(),
                    jTextFieldUnitFullName.getText(),
                    comment);
            }
            else
            {
                success = currentModFile.myUnitsElement.addUnit(
                    jTextFieldUnitShortName.getText(),
                    jTextFieldUnitFullName.getText());
            }
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(),ex, this);
            return;
        }

        if (!success)
            GuiUtils.showErrorMessage(logger, "Could not add unit of name: " +
                             jTextFieldUnitShortName.getText(), null, this);



        refreshAll();

        int unitToSelect =listModelUnits.size() - 1;
        logger.logComment("Setting selected unit to:: "+ unitToSelect);
        jListUnits.setSelectedIndex(unitToSelect);

    }



    void jRadioButtonPointProcess_actionPerformed(ActionEvent e)
    {
        logger.logComment("jRadioButtonPointProcess changed");
        if (jRadioButtonPointProcess.isSelected())
        {
            currentModFile.myNeuronElement.setProcess(NeuronElement.POINT_PROCESS);
        }
    }

    void jRadioButtonDensityMechnaism_actionPerformed(ActionEvent e)
    {
        logger.logComment("jRadioButtonDensityMechnaism changed");
        if (jRadioButtonDensityMechnaism.isSelected())
        {
            currentModFile.myNeuronElement.setProcess(NeuronElement.DENSITY_MECHANISM);
        }

    }



    void jRadioButtonNoCurrent_actionPerformed(ActionEvent e)
    {
        logger.logComment("jRadioButtonNoCurrent changed");
        if (jRadioButtonNoCurrent.isSelected())
        {
            currentModFile.myNeuronElement.setCurrentReference(NeuronElement.NO_CURRENT_REFERENCED);
            jTextFieldNeuronCurrent.setEnabled(false);
        }
    }

    void jRadioButtonElectrode_actionPerformed(ActionEvent e)
    {
        logger.logComment("jRadioButtonElectrode changed");
        if (jRadioButtonElectrode.isSelected())
        {
            currentModFile.myNeuronElement.setCurrentReference(NeuronElement.ELECTRODE_CURRENT);
            jTextFieldNeuronCurrent.setEnabled(true);
        }

    }

    void jRadioButtonNonSpecific_actionPerformed(ActionEvent e)
    {
        logger.logComment("jRadioButtonNonSpecific changed");
        if (jRadioButtonNonSpecific.isSelected())
        {
            currentModFile.myNeuronElement.setCurrentReference(NeuronElement.NONSPECIFIC_CURRENT);
            jTextFieldNeuronCurrent.setEnabled(true);
        }

    }

    void jTextAreaInitial_focusLost(FocusEvent e)
    {
        //logger.logComment("Focus moving away from Initial editor...");
        if (currentModFile == null) return;

        if (jButtonInitialParseAndSave.equals(e.getOppositeComponent()))
        {
            logger.logComment("All is well, they're pressing the parse and save button...");
            return;
        }
        int newlySelectedIndex = jTabbedPane1.getSelectedIndex();

        if (initialTextNeedsChecking)
        {
            // just in case they altered the text then chose a different tab...
            jTabbedPane1.setSelectedComponent(jPanelInitial);

            logger.logComment("Need to check the text...");
            Object[] options = {"Save", "Don't save", "Cancel"};

            JOptionPane option = new JOptionPane(
                "The text has been altered. Save?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

            JDialog dialog = option.createDialog(this, "Save INITIAL block text?");
            dialog.setVisible(true);

            Object choice = option.getValue();
            logger.logComment("User has chosen: " + choice);
            if (choice.equals("Cancel"))
            {
                logger.logComment("User has changed their mind...");
                jTextAreaInitial.requestFocus(); // to let them alter it...
                return;
            }
            else if (choice.equals("Save"))
            {
                doInitialParseAndSave();
            }
            else if (choice.equals("Don't save"))
            {
                initialTextNeedsChecking = false;
                refreshInitialTab(); // reset it to the stored values...
            }

            jTabbedPane1.setSelectedIndex(newlySelectedIndex);
        }
    }

    void jTextAreaInitial_keyReleased(KeyEvent e)
    {
        if (currentModFile == null) return;
        initialTextNeedsChecking = true;
    }

    void jButtonInitialParseAndSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("ParseAndSave button pressed");
        doInitialParseAndSave();
    }

    void doInitialParseAndSave()
    {
        currentModFile.ensureInitialBlockInElements();

        String allLinesAsOne = jTextAreaInitial.getText();
        String[] lines = allLinesAsOne.split("\\n");
        logger.logComment("Looking at "+lines.length+" lines...");
        currentModFile.myInitialElement.reset();
        for (int i = 0; i < lines.length; i++)
        {
            try
            {
                currentModFile.myInitialElement.addLine(lines[i]);
            }
            catch (ModFileException ex)
            {
                initialTextNeedsChecking = true;
                GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
                return;
            }
        }
        initialTextNeedsChecking = false;

        refreshGeneral();

    }

    void doNetReceiveParseAndSave()
    {
        currentModFile.ensureNetReceiveBlockInElements();
        String allLinesAsOne = jTextAreaNetReceive.getText();
        String[] lines = allLinesAsOne.split("\\n");
        logger.logComment("Looking at "+lines.length+" lines...");
        currentModFile.myNetReceiveElement.reset();
        for (int i = 0; i < lines.length; i++)
        {
            try
            {
                currentModFile.myNetReceiveElement.addLine(lines[i]);
            }
            catch (ModFileException ex)
            {
                netReceiveTextNeedsChecking = true;
                GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
                return;
            }
        }
        netReceiveTextNeedsChecking = false;

        refreshGeneral();
    }


    void doFunctionParseAndSave()
    {
        String allLinesAsOne = jTextAreaFunction.getText();
        String[] lines = allLinesAsOne.split("\\n");
        logger.logComment("Looking at "+lines.length+" lines...");

        FunctionElement functionElement = null;
        String currentSelection = (String)jComboBoxFunctionList.getSelectedItem();
        Vector allFunctions = currentModFile.getFunctions();
        for (int i = 0; i < allFunctions.size(); i++)
        {
            FunctionElement nextElement = (FunctionElement) allFunctions.elementAt(i);
            if (nextElement.myFunctionName.equals(currentSelection))
            {
                functionElement = nextElement;
            }
        }
        functionElement.reset();
        String paramList = jTextFieldFunctionParameters.getText().trim();
        if (!paramList.startsWith("(")) paramList = new String("("+paramList);
        if (!paramList.endsWith(")")) paramList = new String(paramList+")");

        functionElement.myParameterList = paramList;

        for (int i = 0; i < lines.length; i++)
        {
            try
            {
                functionElement.addLine(lines[i]);
            }
            catch (ModFileException ex)
            {
                functionTextNeedsChecking = true;
                GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
                return;
            }
        }
        functionTextNeedsChecking = false;
        jComboBoxFunctionList.setEnabled(true);

        refreshGeneral();
    }




    void doProcedureParseAndSave()
    {
        String allLinesAsOne = jTextAreaProcedure.getText();
        String[] lines = allLinesAsOne.split("\\n");
        logger.logComment("Looking at "+lines.length+" lines...");

        ProcedureElement procElement = null;
        String currentSelection = (String)jComboBoxProcedureList.getSelectedItem();
        Vector allProcedures = currentModFile.getProcedures();
        for (int i = 0; i < allProcedures.size(); i++)
        {
            ProcedureElement nextElement = (ProcedureElement) allProcedures.elementAt(i);
            if (nextElement.myProcedureName.equals(currentSelection))
            {
                procElement = nextElement;
            }
        }
        procElement.reset();
        String paramList = jTextFieldProcedureParameters.getText();
        if (!paramList.startsWith("(")) paramList = new String("("+paramList);
        if (!paramList.endsWith(")")) paramList = new String(paramList+")");

        procElement.myParameterList = paramList;

        for (int i = 0; i < lines.length; i++)
        {
            try
            {
                procElement.addLine(lines[i]);
            }
            catch (ModFileException ex)
            {
                procedureTextNeedsChecking = true;
                GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
                return;
            }
        }
        procedureTextNeedsChecking = false;
        jComboBoxProcedureList.setEnabled(true);

        refreshGeneral();
    }


    void jButtonStateUpdate_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeUpdated = jListState.getSelectedIndex();
        StateElement.NamedVariableEntry varAsIs = (StateElement.NamedVariableEntry)jListState.getSelectedValue();
        logger.logComment("Updating a var: "+ varAsIs);

        boolean success = currentModFile.myStateElement.removeNamedVariables(varAsIs);
        if (!success)
        {
            GuiUtils.showErrorMessage(logger, "Could not update element: " + varAsIs, null, this);
            return;
        }
        success = currentModFile.myStateElement.addNamedVariable(
            jTextFieldStateName.getText(),
            jTextFieldStateDim.getText(),
            jTextFieldStateComment.getText(),
            StateElement.NamedVariableEntry.GENERAL_VARIABLE);

        if (!success) GuiUtils.showErrorMessage(logger, "Could not add state var of name: " + jTextFieldStateName.getText(), null, this);

        refreshAll();

        int stateVarToSelect =listModelState.size() - 1;
        logger.logComment("Setting selected val to:: "+ stateVarToSelect);
        jListState.setSelectedIndex(stateVarToSelect);

    }

    void jButtonStateRemove_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeRemoved = jListState.getSelectedIndex();
        StateElement.NamedVariableEntry var = (StateElement.NamedVariableEntry)jListState.getSelectedValue();
        logger.logComment("Removing a var: "+ var);

        boolean success = currentModFile.myStateElement.removeNamedVariables(var);

        if (!success) GuiUtils.showErrorMessage(logger, "Could not remove element: "+ var, null, this);

        refreshAll();

        if (indexToBeRemoved>0)
        {
            jListState.setSelectedIndex(indexToBeRemoved - 1);
        }
        else jListState.setSelectedIndex(0);
    }

    void jButtonStateAdd_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        logger.logComment("Adding a new STATE variable...");

        boolean success = currentModFile.myStateElement.addNamedVariable(jTextFieldStateName.getText(),
                                                            jTextFieldStateDim.getText(),
                                                            jTextFieldStateComment.getText(),
                                                            StateElement.NamedVariableEntry.GENERAL_VARIABLE);

        if (!success) GuiUtils.showErrorMessage(logger, "Could not add state variable of name: "+ jTextFieldStateName.getText(), null, this);

        refreshAll();
    }

    void jButtonAssignedUpdate_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeUpdated = jListAssigned.getSelectedIndex();
        AssignedElement.NamedVariableEntry varAsIs = (AssignedElement.NamedVariableEntry)jListAssigned.getSelectedValue();
        logger.logComment("Updating a var: "+ varAsIs);

        if (varAsIs.variableType == AssignedElement.NamedVariableEntry.GLOBAL_VARIABLE)
        {
            currentModFile.myNeuronElement.removeGlobalVariable(varAsIs.name);
        }
        else if (varAsIs.variableType == AssignedElement.NamedVariableEntry.RANGE_VARIABLE)
        {
            currentModFile.myNeuronElement.removeRangeVariable(varAsIs.name);
        }

        boolean success = currentModFile.myAssignedElement.removeNamedVariables(varAsIs);

        if (!success)
        {
            GuiUtils.showErrorMessage(logger, "Could not update element: " + varAsIs, null, this);
            return;
        }

        int variableType = NamedVariableElement.NamedVariableEntry.GENERAL_VARIABLE;
        String newVariableName = jTextFieldAssignedName.getText();

        if (jRadioButtonAssignedGlobal.isSelected())
        {
            variableType = NamedVariableElement.NamedVariableEntry.GLOBAL_VARIABLE;
            currentModFile.myNeuronElement.addGlobalVariable(newVariableName);
        }
        else if (jRadioButtonAssignedRange.isSelected())
        {
            variableType = NamedVariableElement.NamedVariableEntry.RANGE_VARIABLE;
            currentModFile.myNeuronElement.addRangeVariable(newVariableName);
        }

        success = currentModFile.myAssignedElement.addNamedVariable(
            newVariableName,
            jTextFieldAssignedDim.getText(),
            jTextFieldAssignedComment.getText(),
            variableType);

        if (!success) GuiUtils.showErrorMessage(logger, "Could not add Assigned var of name: " + jTextFieldAssignedName.getText(), null, this);

        refreshAll();

        int assignedVarToSelect =listModelAssigned.size() - 1;
        logger.logComment("Setting selected val to:: "+ assignedVarToSelect);
        jListAssigned.setSelectedIndex(assignedVarToSelect);



    }

    void jButtonAssignedRemove_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeRemoved = jListAssigned.getSelectedIndex();
        AssignedElement.NamedVariableEntry var = (AssignedElement.NamedVariableEntry)
            jListAssigned.getSelectedValue();
        logger.logComment("Removing a var: " + var);

        if (var.variableType == AssignedElement.NamedVariableEntry.GLOBAL_VARIABLE)
        {
            currentModFile.myNeuronElement.removeGlobalVariable(var.name);
        }
        else if (var.variableType == AssignedElement.NamedVariableEntry.RANGE_VARIABLE)
        {
            currentModFile.myNeuronElement.removeRangeVariable(var.name);
        }


        boolean success = currentModFile.myAssignedElement.removeNamedVariables(var);

        if (!success) GuiUtils.showErrorMessage(logger, "Could not remove element: " + var, null, this);

        refreshAll();

        if (indexToBeRemoved > 0)
        {
            jListAssigned.setSelectedIndex(indexToBeRemoved - 1);
        }
        else
        jListAssigned.setSelectedIndex(0);

    }

    void jButtonAssignedAdd_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        logger.logComment("Adding a new ASSIGNED variable...");

        String newVariableName = jTextFieldAssignedName.getText();

        int variableType = NamedVariableElement.NamedVariableEntry.GENERAL_VARIABLE;
        if (jRadioButtonAssignedGlobal.isSelected())
        {
            variableType = NamedVariableElement.NamedVariableEntry.GLOBAL_VARIABLE;
            currentModFile.myNeuronElement.addGlobalVariable(newVariableName);
        }
        else if (jRadioButtonAssignedRange.isSelected())
        {
            variableType = NamedVariableElement.NamedVariableEntry.RANGE_VARIABLE;
            currentModFile.myNeuronElement.addRangeVariable(newVariableName);
        }


        boolean success = currentModFile.myAssignedElement.addNamedVariable(newVariableName,
                                                                    jTextFieldAssignedDim.getText(),
                                                                    jTextFieldAssignedComment.getText(),
                                                                    variableType);


        if (!success) GuiUtils.showErrorMessage(logger, "Could not add Assigned variable of name: "+ jTextFieldAssignedName.getText(), null, this);

        refreshAll();

    }

    void jButtonParametersUpdate_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeUpdated = jListParameters.getSelectedIndex();
        ParametersElement.ParameterEntry varAsIs = (ParametersElement.ParameterEntry)jListParameters.getSelectedValue();

        logger.logComment("Updating a var: "+ varAsIs);

        if (varAsIs.variableType == ParametersElement.ParameterEntry.GLOBAL_VARIABLE)
        {
            currentModFile.myNeuronElement.removeGlobalVariable(varAsIs.name);
        }
        else if (varAsIs.variableType == ParametersElement.ParameterEntry.RANGE_VARIABLE)
        {
            currentModFile.myNeuronElement.removeRangeVariable(varAsIs.name);
        }


        boolean success = currentModFile.myParametersElement.removeParameter(varAsIs);
        if (!success)
        {
            GuiUtils.showErrorMessage(logger, "Could not update element: " + varAsIs, null, this);
            return;
        }
        double value;
        try
        {
            String valString = jTextFieldParametersValue.getText();
            if (valString.equals("")) value = Double.MIN_VALUE;
            else value = Double.parseDouble(valString);
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid number in the value field", null, this);
            return;
        }

        String newVariableName = jTextFieldParametersName.getText();

        int variableType = ParametersElement.ParameterEntry.GENERAL_VARIABLE;
        if (jRadioButtonParametersRange.isSelected())
        {
            variableType = ParametersElement.ParameterEntry.RANGE_VARIABLE;
            currentModFile.myNeuronElement.addRangeVariable(newVariableName);
        }
        else if (jRadioButtonParametersGlobal.isSelected())
        {
            variableType = ParametersElement.ParameterEntry.GLOBAL_VARIABLE;
            currentModFile.myNeuronElement.addGlobalVariable(newVariableName);
        }


        success = currentModFile.myParametersElement.addParameter(
            newVariableName,
            value,
            jTextFieldParametersDim.getText(),
            jTextFieldParametersComment.getText(),
            variableType);

        if (!success) GuiUtils.showErrorMessage(logger, "Could not add Parameters var of name: " + jTextFieldParametersName.getText(), null, this);

        refreshAll();

        int parametersVarToSelect =listModelParameters.size() - 1;
        logger.logComment("Setting selected val to:: "+ parametersVarToSelect);
        jListParameters.setSelectedIndex(parametersVarToSelect);


    }

    void jButtonParameterRemove_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        int indexToBeRemoved = jListParameters.getSelectedIndex();
        ParametersElement.ParameterEntry var = (ParametersElement.ParameterEntry)jListParameters.getSelectedValue();
        logger.logComment("Removing a var: " + var);

        if (var.variableType == ParametersElement.ParameterEntry.GLOBAL_VARIABLE)
        {
            currentModFile.myNeuronElement.removeGlobalVariable(var.name);
        }
        else if (var.variableType == ParametersElement.ParameterEntry.RANGE_VARIABLE)
        {
            currentModFile.myNeuronElement.removeRangeVariable(var.name);
        }

        boolean success = currentModFile.myParametersElement.removeParameter(var);


        if (!success) GuiUtils.showErrorMessage(logger, "Could not remove element: " + var, null, this);

        refreshAll();

        if (indexToBeRemoved > 0)
        {
            jListParameters.setSelectedIndex(indexToBeRemoved - 1);
        }
        else
        jListParameters.setSelectedIndex(0);
    }

    void jButtonParameterAdd_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        logger.logComment("Adding a new Parameter variable...");

        double value;
        try
        {
            String valString = jTextFieldParametersValue.getText();
            if (valString.equals("")) value = Double.MIN_VALUE;
            else value = Double.parseDouble(valString);
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid number in the value field", null, this);
            return;
        }
        String newVariableName = jTextFieldParametersName.getText();

        int variableType = ParametersElement.ParameterEntry.GENERAL_VARIABLE;
        if (jRadioButtonParametersRange.isSelected())
        {
            variableType = ParametersElement.ParameterEntry.RANGE_VARIABLE;
            currentModFile.myNeuronElement.addRangeVariable(newVariableName);
        }
        else if (jRadioButtonParametersGlobal.isSelected())
        {
            variableType = ParametersElement.ParameterEntry.GLOBAL_VARIABLE;
            currentModFile.myNeuronElement.addGlobalVariable(newVariableName);
        }

        boolean success = currentModFile.myParametersElement.addParameter(newVariableName,
                                                                          value,
                                                                    jTextFieldParametersDim.getText(),
                                                                    jTextFieldParametersComment.getText(),
                                                                    variableType);





        if (!success) GuiUtils.showErrorMessage(logger, "Could not add Parameters variable of name: "+ jTextFieldParametersName.getText(), null, this);

        refreshAll();
    }

    void jButtonBreakpointParseAndSave_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        logger.logComment("jButtonBreakpointParseAndSave pressed...");
        doBreakpointParseAndSave();
     }

     void doBreakpointParseAndSave()
     {
         currentModFile.ensureBreakpointBlockInElements();

         String allLinesAsOne = jTextAreaBreakpoint.getText();

         if (allLinesAsOne.trim().length()==0)
         {
             /** @todo check empty... */
         }
         String[] lines = allLinesAsOne.split("\\n");
         logger.logComment("Looking at "+lines.length+" lines...");
         currentModFile.myBreakpointElement.reset();
         for (int i = 0; i < lines.length; i++)
         {
             try
             {
                 currentModFile.myBreakpointElement.addLine(lines[i]);
             }
             catch (ModFileException ex)
             {
                 breakpointTextNeedsChecking = true;
                 GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
                 return;
             }
         }
         breakpointTextNeedsChecking = false;

         refreshGeneral();
     }

    void jTextAreaBreakpoint_keyReleased(KeyEvent e)
    {
        if (currentModFile == null) return;
        breakpointTextNeedsChecking = true;
    }

    void jTextAreaBreakpoint_focusLost(FocusEvent e)
    {
        //logger.logComment("Focus moving away from Breakpoint editor...");
        if (currentModFile == null) return;
        if (jButtonBreakpointParseAndSave.equals(e.getOppositeComponent()))
        {
            logger.logComment("All is well, they're pressing the parse and save button...");
            return;
        }
        int newlySelectedIndex = jTabbedPane1.getSelectedIndex();

        if (breakpointTextNeedsChecking)
        {
            // just in case they altered the text then chose a different tab...
            jTabbedPane1.setSelectedComponent(jPanelBreakpoint);

            logger.logComment("Need to check the text...");
            Object[] options = {"Save", "Don't save", "Cancel"};

            JOptionPane option = new JOptionPane(
                "The text has been altered. Save?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

            JDialog dialog = option.createDialog(this, "Save BREAKPOINT block text?");
            dialog.setVisible(true);

            Object choice = option.getValue();
            logger.logComment("User has chosen: " + choice);
            if (choice.equals("Cancel"))
            {
                logger.logComment("User has changed their mind...");
                jTextAreaBreakpoint.requestFocus(); // to let them alter it...
                return;
            }
            else if (choice.equals("Save"))
            {
                doBreakpointParseAndSave();
            }
            else if (choice.equals("Don't save"))
            {
                breakpointTextNeedsChecking = false;
                refreshBreakpointTab(); // reset it to the stored values...
            }

            jTabbedPane1.setSelectedIndex(newlySelectedIndex);
        }

    }

    void jButtonTestUnits_actionPerformed(ActionEvent e)
    {
        logger.logComment("Testing the file...");
        if (currentModFile.getStatus()==ModFile.FILE_EDITED_NOT_SAVED)
        {
            JOptionPane.showMessageDialog(this,
                                          "Please save the file before proceeding",
                                          "Warning",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        try
        {
            currentModFile.testFileWithNeuron();
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
        }

    }

    void jButtonCompile_actionPerformed(ActionEvent e)
    {
        logger.logComment("Compiling the example...");
        if (currentModFile.getStatus()==ModFile.FILE_EDITED_NOT_SAVED)
        {
            JOptionPane.showMessageDialog(this,
                                          "Please save the file before proceeding",
                                          "Warning",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        try
        {
            boolean success = currentModFile.compileModFile();

            if (! success)
            {
                jButtonRunExample.setEnabled(false);
                logger.logError("Failed to compile the file...");
                 return;
            }

            jButtonRunExample.setEnabled(true);
          //  currentModFile.generateTestHocFile();
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
        }

    }

    void jComboBoxFunctionList_itemStateChanged(ItemEvent e)
    {
        //logger.logComment("Selection of function changed: "+e );

        if (!functionTextNeedsChecking) // if it needs checking, just leave it as it is...
        {
            String newSelection = (String) jComboBoxFunctionList.getSelectedItem();
            if (newSelection == null || newSelection.equals(defaultFunctionText))
            {
                jLabelFunctionName.setText("Function...");
                jTextFieldFunctionParameters.setText("");
                jTextAreaFunction.setText("");
                return;
            }
            FunctionElement functionElement = null;
            Vector allFunctions = currentModFile.getFunctions();
            for (int i = 0; i < allFunctions.size(); i++)
            {
                FunctionElement nextElement = (FunctionElement) allFunctions.elementAt(i);
                if (nextElement.myFunctionName.equals(newSelection))
                {
                    functionElement = nextElement;
                }
            }
            if (functionElement == null)
                return;

            jLabelFunctionName.setText(functionElement.myFunctionName);
            jTextFieldFunctionParameters.setText(functionElement.myParameterList);

            String[] lines = functionElement.getUnformattedLines();
            if (lines == null) return;
            logger.logComment("Adding " + lines.length +" lines for the FUNCTION block");
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < lines.length; i++)
            {
                sb.append(lines[i] + "\n");
            }
            jTextAreaFunction.setText(sb.toString());
        }
        else
        {
            logger.logComment("Text has been changed...");
        }
    }





    void jComboBoxProcedureList_itemStateChanged(ItemEvent e)
    {
        //logger.logComment("Selection of Procedure changed: "+e );

        if (!procedureTextNeedsChecking) // if it needs checking, just leave it as it is...
        {
            String newSelection = (String) jComboBoxProcedureList.getSelectedItem();
            if (newSelection == null || newSelection.equals(defaultProcedureText))
            {
                jLabelProcedureName.setText("Procedure...");
                jTextFieldProcedureParameters.setText("");
                jTextAreaProcedure.setText("");
                return;
            }
            ProcedureElement procElement = null;
            Vector allProcedures = currentModFile.getProcedures();
            for (int i = 0; i < allProcedures.size(); i++)
            {
                ProcedureElement nextElement = (ProcedureElement) allProcedures.elementAt(i);
                if (nextElement.myProcedureName.equals(newSelection))
                {
                    procElement = nextElement;
                }
            }
            if (procElement == null)
                return;

            jLabelProcedureName.setText(procElement.myProcedureName);
            jTextFieldProcedureParameters.setText(procElement.myParameterList);

            String[] lines = procElement.getUnformattedLines();
            if (lines == null) return;
            logger.logComment("Adding " + lines.length +" lines for the PROCEDURE block");
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < lines.length; i++)
            {
                sb.append(lines[i] + "\n");
            }
            jTextAreaProcedure.setText(sb.toString());
        }
        else
        {
            logger.logComment("Text has been changed...");
        }
    }



    void jTextAreaFunction_keyPressed(KeyEvent e)
    {
        if (currentModFile == null) return;
        functionTextNeedsChecking = true;
        jComboBoxFunctionList.setEnabled(false);
    }


    void jTextFieldFunctionParameters_keyReleased(KeyEvent e)
    {
        if (currentModFile == null) return;
        functionTextNeedsChecking = true;
        jComboBoxFunctionList.setEnabled(false);
    }


    void jTextAreaProcedure_keyPressed(KeyEvent e)
    {
        if (currentModFile == null) return;
        procedureTextNeedsChecking = true;
        jComboBoxProcedureList.setEnabled(false);
    }


    void jTextFieldProcedureParameters_keyReleased(KeyEvent e)
    {
        if (currentModFile == null) return;
        procedureTextNeedsChecking = true;
        jComboBoxProcedureList.setEnabled(false);
    }


    /**
     * Using this to sense when the function text has been changed
     * @param e The event related to the cpmponent
     */
    void jPanelFunctions_componentHidden(ComponentEvent e)
    {
        //logger.logComment("jPanelFunctions_componentHidden...");
        if (currentModFile == null) return;
        if (!functionTextNeedsChecking) return;

        int newlySelectedIndex = jTabbedPane1.getSelectedIndex();

        // just in case they altered the text then chose a different tab...
        jTabbedPane1.setSelectedComponent(jPanelFunctions);

        logger.logComment("Need to check the text...");
        Object[] options = {"Save", "Don't save", "Cancel"};

        JOptionPane option = new JOptionPane(
                "The Function text has been altered. Save?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

        JDialog dialog = option.createDialog(this, "Save FUNCTION block text?");
        dialog.setVisible(true);

        Object choice = option.getValue();
        logger.logComment("User has chosen: " + choice);
        if (choice.equals("Cancel"))
        {
            logger.logComment("User has changed their mind...");
            jTextAreaFunction.requestFocus(); // to let them alter it...
            return;
        }
        else if (choice.equals("Save"))
        {
            doFunctionParseAndSave();
        }
        else if (choice.equals("Don't save"))
        {
            functionTextNeedsChecking = false;
            refreshFunctionsTab(); // reset it to the stored values...
        }

        jTabbedPane1.setSelectedIndex(newlySelectedIndex);


    }


    /**
     * Using this to sense when the procedure text has been changed
     * @param e component related event
     */
    void jPanelProcedures_componentHidden(ComponentEvent e)
    {
        //logger.logComment("jPanelProcedures_componentHidden...");
        if (currentModFile == null) return;
        if (!procedureTextNeedsChecking) return;

        int newlySelectedIndex = jTabbedPane1.getSelectedIndex();

        // just in case they altered the text then chose a different tab...
        jTabbedPane1.setSelectedComponent(jPanelProcedures);

        logger.logComment("Need to check the text...");
        Object[] options = {"Save", "Don't save", "Cancel"};

        JOptionPane option = new JOptionPane(
                "The Procedure text has been altered. Save?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

        JDialog dialog = option.createDialog(this, "Save PROCEDURE block text?");
        dialog.setVisible(true);

        Object choice = option.getValue();
        logger.logComment("User has chosen: " + choice);
        if (choice.equals("Cancel"))
        {
            logger.logComment("User has changed their mind...");
            jTextAreaProcedure.requestFocus(); // to let them alter it...
            return;
        }
        else if (choice.equals("Save"))
        {
            doProcedureParseAndSave();
        }
        else if (choice.equals("Don't save"))
        {
            procedureTextNeedsChecking = false;
            refreshProceduresTab(); // reset it to the stored values...
        }

        jTabbedPane1.setSelectedIndex(newlySelectedIndex);


    }

    void jButtonFunctionUpdate_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        doFunctionParseAndSave();
    }

    void jButtonFunctionRemove_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        FunctionElement functionElement = null;
        Vector allFunctions = currentModFile.getFunctions();
        String currSelected = (String)jComboBoxFunctionList.getSelectedItem();
        logger.logComment("Removing the function: "+ currSelected);

        for (int i = 0; i < allFunctions.size(); i++)
        {
            FunctionElement nextElement = (FunctionElement) allFunctions.elementAt(i);
            if (nextElement.myFunctionName.equals(currSelected))
            {
                functionElement = nextElement;
            }
        }
        boolean success = false;
        if (functionElement == null ||
            ! currentModFile.removeFunction(functionElement))
        {
            GuiUtils.showErrorMessage(logger, "It was not possible to remove function: "+ currSelected, null, this);
        }
        refreshAll();
    }

    void jButtonFunctionAdd_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        String nameOfNewFunction = jTextFieldFunctionNewName.getText();
        if (nameOfNewFunction== null||nameOfNewFunction.trim().equals(""))
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid name in the function field", null, this);
            return;
        }
        logger.logComment("Adding new function called: "+ nameOfNewFunction);

        try
        {
            currentModFile.addNewFunction(nameOfNewFunction);
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), null, this);
        }
        refreshAll();

        jComboBoxFunctionList.setSelectedItem(nameOfNewFunction);
    }

    void jButtonProcedureUpdate_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        doProcedureParseAndSave();
    }

    void jButtonProcedureRemove_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        ProcedureElement procElement = null;
        Vector allProcedures = currentModFile.getProcedures();
        String currSelected = (String)jComboBoxProcedureList.getSelectedItem();
        logger.logComment("Removing the procedure: "+ currSelected);

        for (int i = 0; i < allProcedures.size(); i++)
        {
            ProcedureElement nextElement = (ProcedureElement) allProcedures.elementAt(i);
            if (nextElement.myProcedureName.equals(currSelected))
            {
                procElement = nextElement;
            }
        }
        boolean success = false;
        if (procElement == null ||
            ! currentModFile.removeProcedure(procElement))
        {
            GuiUtils.showErrorMessage(logger, "It was not possible to remove procedure: "+ currSelected, null, this);
        }
        refreshAll();

    }

    void jButtonProcedureAdd_actionPerformed(ActionEvent e)
    {
        markAsEdited();
        String nameOfNewProcedure = jTextFieldProcedureName.getText();
        if (nameOfNewProcedure== null||nameOfNewProcedure.trim().equals(""))
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid name in the procedure field", null, this);
            return;
        }
        logger.logComment("Adding new procedure called: "+ nameOfNewProcedure);

        try
        {
            currentModFile.addNewProcedure(nameOfNewProcedure);
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), null, this);
        }
        refreshAll();


        jComboBoxProcedureList.setSelectedItem(nameOfNewProcedure);

    }

    void jTextAreaDerivative_keyPressed(KeyEvent e)
    {
        if (currentModFile == null) return;
        derivativeTextNeedsChecking = true;
    }

    void jPanelDerivative_componentHidden(ComponentEvent e)
    {
        //logger.logComment("Derivative panel hidden");
        if (currentModFile == null) return;
        if (!derivativeTextNeedsChecking) return;

        int newlySelectedIndex = jTabbedPane1.getSelectedIndex();

        // just in case they altered the text then chose a different tab...
        jTabbedPane1.setSelectedComponent(jPanelDerivative);

        logger.logComment("Need to check the text...");
        Object[] options = {"Save", "Don't save", "Cancel"};

        JOptionPane option = new JOptionPane(
                "The Derivative text has been altered. Save?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);

        JDialog dialog = option.createDialog(this, "Save DERIVATIVE block text?");
        dialog.setVisible(true);

        Object choice = option.getValue();
        logger.logComment("User has chosen: " + choice);
        if (choice.equals("Cancel"))
        {
            logger.logComment("User has changed their mind...");
            jTextAreaDerivative.requestFocus(); // to let them alter it...
            return;
        }
        else if (choice.equals("Save"))
        {
            doDerivativeParseAndSave();
        }
        else if (choice.equals("Don't save"))
        {
            derivativeTextNeedsChecking = false;
            refreshDerivativeTab(); // reset it to the stored values...
        }

        jTabbedPane1.setSelectedIndex(newlySelectedIndex);



    }

    private void doDerivativeParseAndSave()
    {
        String allLinesAsOne = jTextAreaDerivative.getText();
        String derivName = jTextFieldDerivaiveName.getText();

        currentModFile.ensureDerivativeBlockInElements();

        String[] lines = allLinesAsOne.split("\\n");
        logger.logComment("Looking at " + lines.length + " lines...");
        currentModFile.myDerivativeElement.reset();
        currentModFile.myDerivativeElement.initialise(derivName);
        for (int i = 0; i < lines.length; i++)
        {
            try
            {
                currentModFile.myDerivativeElement.addLine(lines[i]);
            }
            catch (ModFileException ex)
            {
                derivativeTextNeedsChecking = true;
                GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
                return;
            }
        }
        derivativeTextNeedsChecking = false;

        refreshGeneral();
    }

    void jTextFieldDerivaiveName_keyPressed(KeyEvent e)
    {
        if (currentModFile == null) return;
        derivativeTextNeedsChecking = true;
    }

    void jButtonDerivativeParseAndSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("ParseAndSave button pressed");
        doDerivativeParseAndSave();
    }


    void jButtonNetReceiveParseAndSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("ParseAndSave button pressed");
        doNetReceiveParseAndSave();
    }

    void jMenuHelpAbout_actionPerformed(ActionEvent e)
    {
        NmodlEditorFrame_AboutBox dlg = new NmodlEditorFrame_AboutBox(this);
        /*Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);

       */
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                      (frmSize.height - dlgSize.height) / 2 + loc.y);


        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

    }

    void jMenuFileExit_actionPerformed(ActionEvent e)
    {
        logger.logComment("Exiting nmodlEditor application");

        recentFiles.saveToFile();

        if (standAlone)
        {
            GeneralProperties.saveToSettingsFile();
            System.exit(0);
        }
        else
        {
            this.dispose();
        }

    }

    void jMenuNew_actionPerformed(ActionEvent e)
    {
        logger.logComment("New menu item selected");
        this.doNewModFile();
    }

    void jMenuLoad_actionPerformed(ActionEvent e)
    {
        logger.logComment("Load menu item selected");
        this.doLoadModFile();
    }

    void jMenuSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Save menu item selected");
        this.doSaveModFile();
    }

    void jMenuSaveAs_actionPerformed(ActionEvent e)
    {
        logger.logComment("Save As menu item selected");
        this.doSaveAsModFile();
    }

    public void jMenuRecentFile_actionPerformed(ActionEvent e)
    {
        logger.logComment("Action event: "+e);
        JMenuItem menuItem =(JMenuItem)e.getSource();
        String recentFileName = menuItem.getText();
        logger.logComment("Opening recent file: "+recentFileName);

        File recentFile = new File(recentFileName);

        if (!recentFile.exists())
        {
            logger.logComment("The file: "+recentFileName+" doesn't exist...");
            recentFiles.removeFromList(recentFile);
            refreshAll();
            return;
        }

        boolean continueWithAction = checkToSaveAndContinue();

        if (!continueWithAction)return;


        try
        {
            initialisingModFile = true;
            currentModFile = new ModFile(recentFile.getAbsolutePath());
            recentFiles.addToList(currentModFile.getCurrentFile().getAbsolutePath());

            enableGUIsOnActiveModFile();

        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing the file: "+ recentFile.getAbsolutePath(), ex, this);
            recentFiles.removeFromList(recentFile);
        }
        refreshAll();
        initialisingModFile = false;

    }

    private void addNamedDocumentListener(String tabName, JTextComponent comp)
    {
        GeneralComponentListner newListner = new GeneralComponentListner(tabName, this);
        comp.getDocument().addDocumentListener(newListner);
    }


    private class GeneralComponentListner implements DocumentListener, ItemListener
    {
        String myRef = null;
        ModFileEventListener myEventListner = null;

        GeneralComponentListner(String ref, ModFileEventListener eventListner)
        {
            myRef = ref;
            myEventListner = eventListner;
        }

        private void registerChange(DocumentEvent e)
        {
            //logger.logComment("DocumentEvent: "+e);
            myEventListner.tabUpdated(myRef);
        }

        private void registerChange(ItemEvent e)
        {
            //logger.logComment("ItemEvent: "+e);
            myEventListner.tabUpdated(myRef);
        }


        public void changedUpdate(DocumentEvent e)
        {
            registerChange(e);
        }

        public void insertUpdate(DocumentEvent e)
        {
            registerChange(e);
        }

        public void removeUpdate(DocumentEvent e)
        {
            registerChange(e);
        }

        public void itemStateChanged(ItemEvent e)
        {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                registerChange(e);
            }
        }
    }


    public void tabUpdated(String tableModelName)
    {
        logger.logComment("Tab updated: " + tableModelName);

        if (currentModFile==null)
        {
            logger.logComment("Ignoring due to no mod file loaded...");
            return;
        }

        if (initialisingModFile)
        {
            logger.logComment("Ignoring due to mod file initialising...");
            return;
        }



        if (tableModelName.equals(NEURON_TAB))
        {
            logger.logComment("Updating tab: "+ NEURON_TAB);

            currentModFile.setTitle(jTextFieldTitle.getText());

            try
            {
                this.currentModFile.myNeuronElement.setCurrentVariable(
                     jTextFieldNeuronCurrent.getText());
                this.currentModFile.myNeuronElement.setProcessName(
                     jTextFieldNeuronTypeName.getText());
             }
             catch (ModFileException ex)
             {
                 GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
             }


         }


         this.refreshGeneral();


    }

    void jMenuItemInternalInfo_actionPerformed(ActionEvent e)
    {
        if (currentModFile==null)
        {
            logger.logComment("No mod file currently loaded");
            return;
        }
        currentModFile.printInternalInfo();
    }

    void jButtonRunExample_actionPerformed(ActionEvent e)
    {
        logger.logComment("Running the example...");
        if (currentModFile.getStatus()==ModFile.FILE_EDITED_NOT_SAVED)
        {
            JOptionPane.showMessageDialog(this,
                                          "Please save the file before proceeding",
                                          "Warning",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        try
        {
            currentModFile.generateTestHocFile();
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
        }

    }
}


