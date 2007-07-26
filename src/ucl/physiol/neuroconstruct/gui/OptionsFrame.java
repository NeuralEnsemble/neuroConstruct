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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * General options and preferences dialog
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


@SuppressWarnings("serial")

public class OptionsFrame extends JFrame
{
    ClassLogger logger = new ClassLogger("OptionsFrame");

    private static boolean beingDisplayed = false;

    MainFrame myMainFrame = null;

    public static final int PROJECT_PROPERTIES_MODE = 0;
    public static final int GENERAL_PROPERTIES_MODE = 1;
    //public static final int NMODL_PROPERTIES_MODE = 2;

    private int myStartupMode;


    ToolTipHelper toolTipText = ToolTipHelper.getInstance();


    JPanel panelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JPanel jPanel3D = new JPanel();
    JPanel jPanelLogging = new JPanel();
    JPanel jPanelSaveCancel = new JPanel();
    JButton jButtonSave = new JButton();
    JButton jButtonCancel = new JButton();
    JLabel jLabelLogFileDir = new JLabel();
    JTextField jTextFieldLogDir = new JTextField();
    JCheckBox jCheckBoxConsole = new JCheckBox();
    JCheckBox jCheckBoxFileOutput = new JCheckBox();
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    public final static String GENERAL_PREFERENCES = new String("General Settings");
    public final static String THREE_D_PREFERENCES = new String("3D Settings");
    public final static String LOGGING_PREFERENCES = new String("Logging");

    JLabel jLabelBackgroundColour = new JLabel();
    JButton jButtonBackgroundColour = new JButton();
    JCheckBox jCheckBoxShowAxes = new JCheckBox();

    boolean somethingAlteredInProject = false;
    ButtonGroup buttonGroup3DDisplay = new ButtonGroup();
    JPanel jPanelGeneral = new JPanel();

    JLabel jLabelNeuronDir = new JLabel();
    JTextField jTextFieldNeuronLocation = new JTextField();
    
    JLabel jLabelPrefProjDir = new JLabel();
    JTextField jTextFieldPrefProjDir = new JTextField();

    JLabel jLabelBrowserDir = new JLabel();
    JTextField jTextFieldBrowser = new JTextField();
    JLabel jLabelEditorDir = new JLabel();
    JTextField jTextFieldEditor = new JTextField();




    JButton jButtonApply = new JButton();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel jLabelShow = new JLabel();


    JCheckBox jCheckBoxShowRegions = new JCheckBox();
    JCheckBox jCheckBoxShowInputs = new JCheckBox();
    JCheckBox jCheckBoxShowAxonalArbours = new JCheckBox();
    JCheckBox jCheckBoxShowSynapseConns = new JCheckBox();
    JCheckBox jCheckBoxShowSynEndPoints = new JCheckBox();

    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabelCommandLine = new JLabel();
    JTextField jTextFieldCommandLine = new JTextField();
    JLabel jLabelExplination = new JLabel();
    JLabel jLabel1 = new JLabel();

    JTextPane jTextPaneExplaination = new JTextPane();

    JCheckBox jCheckBoxToolTips = new JCheckBox();

    JLabel jLabelSaveInfo = new JLabel();
    JRadioButton jRadioButtonSaveMorphML = new JRadioButton();
    JRadioButton jRadioButtonJavaXML = new JRadioButton();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    ButtonGroup buttonGroupSaveOptions = new ButtonGroup();
    JLabel jLabelRes3DElements = new JLabel();
    JTextField jTextFieldRes3DElements = new JTextField();

    JLabel jLabelTrans = new JLabel();
    JTextField jTextFieldTrans = new JTextField();


    JLabel jLabelCellColour = new JLabel();
    JButton jButtonCellColour = new JButton();
    JComboBox jComboBoxDisplayOptions = new JComboBox();
    JLabel jLabelDisplayOptoin = new JLabel();

    public OptionsFrame(MainFrame parentFrame, String title, int mode)
    {
        super(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        myMainFrame = parentFrame;
        myStartupMode = mode;
        try
        {
            jbInit();
            extraInit();
            pack();
            this.addToolTips();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }
    }

    private OptionsFrame()
    {
    }


    private void addToolTips()
    {
        this.jLabelCellColour.setToolTipText(toolTipText.getToolTip("3D Settings cell colour"));

       this.jCheckBoxShowAxes.setToolTipText(toolTipText.getToolTip("Axes in 3D"));
       this.jButtonCellColour.setToolTipText(toolTipText.getToolTip("3D Settings cell colour"));
       this.jLabelRes3DElements.setToolTipText(toolTipText.getToolTip("3D Resolution"));
       this.jTextFieldRes3DElements.setToolTipText(toolTipText.getToolTip("3D Resolution"));

    }


    private void jbInit() throws Exception
    {
        panelMain.setLayout(borderLayout1);

        Dimension dim = new Dimension(400, 450);

        panelMain.setMaximumSize(dim);
        panelMain.setMinimumSize(dim);
        panelMain.setPreferredSize(dim);
        jButtonSave.setText("Save");

        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSave_actionPerformed(e);
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
        jLabelLogFileDir.setText("Log directory:");
        jCheckBoxConsole.setHorizontalTextPosition(SwingConstants.RIGHT);
        jCheckBoxConsole.setText("Output to screen");
        jCheckBoxConsole.setVerticalAlignment(SwingConstants.CENTER);
        jCheckBoxConsole.setVerticalTextPosition(SwingConstants.CENTER);
        jCheckBoxFileOutput.setEnabled(true);
        jCheckBoxFileOutput.setHorizontalTextPosition(SwingConstants.RIGHT);
        jCheckBoxFileOutput.setText("Log to file");
        jCheckBoxFileOutput.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jCheckBoxFileOutput_actionPerformed(e);
            }
        });

        jPanelLogging.setLayout(gridBagLayout1);


        jTextFieldLogDir.setText("");
        jLabelBackgroundColour.setText("Background Colour:");
        jButtonBackgroundColour.setText("Select...");
        jButtonBackgroundColour.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonBackgroundColour_actionPerformed(e);
            }
        });
        jCheckBoxShowAxes.setSelected(true);
        jCheckBoxShowAxes.setText("Show coloured 3D Axes");


        jPanelGeneral.setDebugGraphicsOptions(0);
        jPanelGeneral.setRequestFocusEnabled(true);
        jPanelGeneral.setLayout(gridBagLayout2);

        jLabelNeuronDir.setText("Neuron home directory:");
        jTextFieldNeuronLocation.setText("");
        jTextFieldNeuronLocation.setColumns(20);

        jLabelPrefProjDir.setText("Default location projects:");
        jTextFieldPrefProjDir.setText("");
        jTextFieldPrefProjDir.setColumns(20);

        jLabelBrowserDir.setText("Path to browser:");
        jTextFieldBrowser.setText("");
        jTextFieldBrowser.setColumns(20);


        jLabelEditorDir.setText("Path to text editor:");
        jTextFieldEditor.setText("");
        jTextFieldEditor.setColumns(20);



        jButtonApply.setText("Apply");
        jButtonApply.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonApply_actionPerformed(e);
            }
        });
        jPanel3D.setDebugGraphicsOptions(0);
        jPanel3D.setLayout(gridBagLayout3);
        jLabelShow.setText("Display:");
        jCheckBoxShowRegions.setText("Regions");
        jCheckBoxShowInputs.setText("Inputs");
        jCheckBoxShowAxonalArbours.setText("Axonal arbours");
        jCheckBoxShowSynapseConns.setText("Synaptic connections");

        jLabelCommandLine.setText("Command line:");

        jTextPaneExplaination.setText("The command line needed to run processes in a new terminal window. "+
                                      "Usually would be: cmd /K start /wait for Windows, gnome-terminal for Gnome desktop Linux "+
                                      "or konsole for KDE on Linux");
        //jTextAreaExplaination.setLineWrap(true);
       // jTextAreaExplaination.setWrapStyleWord(true);

       //jTextPaneExplaination.setStyledDocument();

        jTextPaneExplaination.setBackground(UIManager.getColor(
            "Button.background"));
        jTextPaneExplaination.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxToolTips.setSelected(true);
        jCheckBoxToolTips.setText("Show tool tips");
        jCheckBoxToolTips.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jCheckBoxToolTips_itemStateChanged(e);
            }
        });
        jCheckBoxShowSynEndPoints.setText("Synaptic endpoints");
        jLabelSaveInfo.setText("Format to save morphology files in:");
        jRadioButtonSaveMorphML.setText("MorphML");
        jRadioButtonJavaXML.setText("Java XML");
        //jPanelSave.setLayout(gridBagLayout4);
        //jPanelSave.setAlignmentX((float) 0.5);
        //jPanelSave.setAlignmentY((float) 0.0);
        jLabelRes3DElements.setText("Resolution 3D elements:");
        jLabelTrans.setText("Level of transparency (~0.8 -> 1):");

        jTextFieldRes3DElements.setText("30");
        jTextFieldRes3DElements.setColumns(5);
        jTextFieldRes3DElements.setMinimumSize(new Dimension(100,24));
        jTextFieldRes3DElements.setHorizontalAlignment(SwingConstants.TRAILING);

        this.jTextFieldTrans.setText("0.7");
        jTextFieldTrans.setColumns(5);
        jTextFieldTrans.setMinimumSize(new Dimension(100,24));

        jTextFieldTrans.setHorizontalAlignment(SwingConstants.TRAILING);


        jLabelCellColour.setText("Default Cell Colour:");
        jButtonCellColour.setText("Select...");
        jButtonCellColour.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellColour_actionPerformed(e);
            }
        });
        //jPanelSave.add(jRadioButtonSaveMorphML,         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        //    ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 30, 150, 0), 0, 0));
        //jPanelSave.add(jRadioButtonJavaXML,      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        //    ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 30, 6, 0), 0, 0));
        jLabelDisplayOptoin.setText("Display: ");

        getContentPane().add(panelMain);
        panelMain.add(jTabbedPane1, BorderLayout.CENTER);

        jTabbedPane1.add(jPanelGeneral, GENERAL_PREFERENCES);






        //jTabbedPane1.add(jPanelSave, SAVE_PREFERENCES);

        jTabbedPane1.add(jPanel3D,    THREE_D_PREFERENCES);
        jTabbedPane1.add(jPanelLogging,  LOGGING_PREFERENCES);

        jPanelLogging.add(jLabelLogFileDir,   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 0));
        jPanelLogging.add(jTextFieldLogDir,        new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 12, 6, 20), -1, 0));
        jPanelLogging.add(jCheckBoxConsole,    new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 10, 6, 0), 0, 0));
        jPanelLogging.add(jCheckBoxFileOutput,   new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 10, 150, 0), 0, 0));


        panelMain.add(jPanelSaveCancel,  BorderLayout.SOUTH);
        jPanelSaveCancel.add(jButtonSave, null);
        jPanelSaveCancel.add(jButtonCancel, null);




        jPanel3D.add(jLabelBackgroundColour,
                     new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
                                            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 0), 0,
                                            0));

        jPanel3D.add(jButtonBackgroundColour, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(6, 0, 6, 0), 0, 0));


        jPanel3D.add(jLabelCellColour, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
                                                              , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                              new Insets(6, 12, 6, 0), 0, 0));

        jPanel3D.add(jCheckBoxShowAxes, new GridBagConstraints(0, 2, 5, 1, 0.0, 0.0
                                                               , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                               new Insets(6, 0, 6, 0), 0, 0));



        jPanel3D.add(jLabelRes3DElements, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));

        jPanel3D.add(jTextFieldRes3DElements, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(6, 0, 6, 0), 0, 0));


        jPanel3D.add(this.jLabelTrans, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));

        jPanel3D.add(jTextFieldTrans, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(6, 0, 6, 0), 0, 0));






        jPanel3D.add(jLabelShow, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0
                                                        , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                        new Insets(6, 12, 6, 0), 0, 0));

        jPanel3D.add(jCheckBoxShowRegions, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0
                                                                  , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                  new Insets(6, 6, 0, 0), 0, 0));

        jPanel3D.add(jCheckBoxShowInputs, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(0, 6, 0, 0), 0, 0));

        jPanel3D.add(jCheckBoxShowAxonalArbours, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(0, 6, 0, 0), 0, 0));

        jPanel3D.add(jCheckBoxShowSynapseConns, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0
                                                                       , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                       new Insets(0, 6, 0, 0), 0, 0));

        jPanel3D.add(jCheckBoxShowSynEndPoints, new GridBagConstraints(3, 10, 1, 1, 0.0, 0.0
                                                                       , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                       new Insets(0, 6, 0, 0), 0, 0));


        jPanel3D.add(jButtonApply, new GridBagConstraints(0, 11, 5, 1, 0.0, 0.0
                                                          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                          new Insets(6, 0, 12, 0), 0, 0));




        jPanel3D.add(jButtonCellColour, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
                                                               , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                               new Insets(6, 12, 6, 0), 0, 0));


        jPanel3D.add(jComboBoxDisplayOptions, new GridBagConstraints(1, 3, 3, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(7, 0, 5, 0), 0, 0));

        jPanel3D.add(jLabelDisplayOptoin, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                                 , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));




        jPanelGeneral.setPreferredSize(new Dimension(200,200));
        jPanelGeneral.setMinimumSize(new Dimension(200,200));

        

        jPanelGeneral.add(jLabelPrefProjDir,
                          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jTextFieldPrefProjDir,
                          new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));
        

        jPanelGeneral.add(jLabelNeuronDir,
                          new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jTextFieldNeuronLocation,
                          new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        jPanelGeneral.add(jLabelBrowserDir,
                          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jTextFieldBrowser,
                          new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        jPanelGeneral.add(jLabelEditorDir,
                          new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jTextFieldEditor,
                          new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        jPanelGeneral.add(jLabelCommandLine,
                          new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, 
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 0, 6), 0, 0));

        jPanelGeneral.add(jTextFieldCommandLine,
                          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, 
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));
        jPanelGeneral.add(jLabelExplination,
                          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 6, 0, 0), 0, 0));
        jPanelGeneral.add(jLabel1,
                          new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER, 
                                                 GridBagConstraints.NONE,
                                                 new Insets(0, 0, 0, 0), 0, 0));
        jPanelGeneral.add(jTextPaneExplaination,
                          new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, 
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 20, 6, 20), 0, 0));

        jPanelGeneral.add(jCheckBoxToolTips,
                          new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                 new Insets(12, 12, 12, 12), 0, 0));


        
        
        
        
        

        buttonGroupSaveOptions.add(jRadioButtonJavaXML);
        buttonGroupSaveOptions.add(jRadioButtonSaveMorphML);


    }

    private void extraInit()
    {
        ToolTipManager ttm = ToolTipManager.sharedInstance();

        jCheckBoxToolTips.setSelected(ttm.isEnabled());

        if (this.myStartupMode==PROJECT_PROPERTIES_MODE)
        {
            this.jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfTab(GENERAL_PREFERENCES),false);
            this.jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfTab(LOGGING_PREFERENCES),false);

            Display3DProperties props3D = myMainFrame.projManager.getProjectDispProps();

            jButtonBackgroundColour.setBackground(props3D.getBackgroundColour3D());
            jButtonCellColour.setBackground(props3D.getCellColour3D());

            jCheckBoxShowAxes.setSelected(props3D.getShow3DAxes());

            //JRadioButtonShowDendAxons.setSelected(props3D.getShowDendAxons());

            jCheckBoxShowRegions.setSelected(props3D.getShowRegions());
            jCheckBoxShowInputs.setSelected(props3D.getShowInputs());
            jCheckBoxShowAxonalArbours.setSelected(props3D.getShowAxonalArbours());
            jCheckBoxShowSynapseConns.setSelected(props3D.getShowSynapseConns());
            jCheckBoxShowSynEndPoints.setSelected(props3D.getShowSynapseEndpoints());


            jTextFieldRes3DElements.setText(props3D.getResolution3DElements()+"");
            jTextFieldTrans.setText(props3D.getTransparency()+"");


            String dendDispOpt = props3D.getDisplayOption();

            Vector displayOptions = Display3DProperties.getDisplayOptions(false);

            for (int i = 0; i < displayOptions.size(); i++)
            {
                jComboBoxDisplayOptions.addItem(displayOptions.elementAt(i));
            }
            jComboBoxDisplayOptions.setSelectedItem(dendDispOpt);



        }
        /*
        else if (this.myStartupMode==NMODL_PROPERTIES_MODE)
        {
            jTextFieldNeuronLocation.setText(GeneralProperties.getNeuronHomeDir());
            jTextFieldCommandLine.setText(GeneralProperties.getExecutableCommandLine());

            this.jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfTab(THREE_D_PREFERENCES),false);
            this.jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfTab(LOGGING_PREFERENCES),false);


        }*/

        else if (this.myStartupMode==GENERAL_PROPERTIES_MODE)
        {
            jTextFieldBrowser.setText(GeneralProperties.getBrowserPath(false));
            jTextFieldEditor.setText(GeneralProperties.getEditorPath(false));
            jTextFieldNeuronLocation.setText(GeneralProperties.getNeuronHomeDir());
            jTextFieldCommandLine.setText(GeneralProperties.getExecutableCommandLine());

            jCheckBoxConsole.setSelected(GeneralProperties.getLogFilePrintToScreenPolicy());
            jCheckBoxFileOutput.setSelected(GeneralProperties.getLogFileSaveToFilePolicy());

            jTextFieldLogDir.setText(GeneralProperties.getLogFileDir().getAbsolutePath());

            jButtonBackgroundColour.setBackground(GeneralProperties.getDefault3DBackgroundColor());
            jButtonCellColour.setBackground(GeneralProperties.getDefaultCellColor3D());

            jCheckBoxShowAxes.setSelected(GeneralProperties.getDefault3DAxesOption());

            //JRadioButtonShowDendAxons.setSelected(GeneralProperties.getDefaultShowDendAxons());
            jCheckBoxShowRegions.setSelected(GeneralProperties.getDefaultShowRegions());
            jCheckBoxShowInputs.setSelected(GeneralProperties.getDefaultShowInputs());
            jCheckBoxShowAxonalArbours.setSelected(GeneralProperties.getDefaultShowAxonalArbours());

            jCheckBoxShowSynapseConns.setSelected(GeneralProperties.getDefaultShowSynapseConns());
            jCheckBoxShowSynEndPoints.setSelected(GeneralProperties.getDefaultShowSynapseEndpoints());

            jTextFieldRes3DElements.setText(GeneralProperties.getDefaultResolution3DElements()+"");
            jTextFieldTrans.setText(GeneralProperties.getDefaultTransparency()+"");



            String dendDispOpt = GeneralProperties.getDefaultDisplayOption();

            Vector displayOptions = Display3DProperties.getDisplayOptions(false);

            for (int i = 0; i < displayOptions.size(); i++)
            {
                jComboBoxDisplayOptions.addItem(displayOptions.elementAt(i));
            }
            jComboBoxDisplayOptions.setSelectedItem(dendDispOpt);

 /*
            String saveOpt = GeneralProperties.getMorphologySaveFormat();

            if (saveOpt.equals(UserSettings.JAVAXML_FORMAT))
            {
                jRadioButtonJavaXML.setSelected(true);
            }
            else if (saveOpt.equals(UserSettings.MORPHML_FORMAT))
            {
                jRadioButtonSaveMorphML.setSelected(true);
            }*/

        }

    }


    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            beingDisplayed = false;
        }
        super.processWindowEvent(e);
    }

    public void setVisible(boolean setVis)
    {
        super.setVisible(setVis);
        beingDisplayed = setVis;
    }

    public static boolean isOptionsFrameCurrentlyDisplayed()
    {
        return beingDisplayed;
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed. Doing nothing...");
        somethingAlteredInProject = false;
        this.dispose();
    }

    public void selectTab(String tabName)
    {
        logger.logComment("Setting selected tab to: "+tabName);

        jTabbedPane1.setSelectedIndex(jTabbedPane1.indexOfTab(tabName));

    }

    private void saveToGeneralProperties()
    {

        if (this.myStartupMode==GENERAL_PROPERTIES_MODE
            /*|| this.myStartupMode==NMODL_PROPERTIES_MODE*/)
        {
            GeneralProperties.setNeuronHomeDir(jTextFieldNeuronLocation.getText());
            GeneralProperties.setBrowserPath(jTextFieldBrowser.getText());
            GeneralProperties.setEditorPath(jTextFieldEditor.getText());
            GeneralProperties.setExecutableCommandLine(jTextFieldCommandLine.getText());
        }


        if (this.myStartupMode==GENERAL_PROPERTIES_MODE)
        {
            logger.logComment("Chucking preferences into GeneralProperties...");

            GeneralProperties.setLogFilePrintToScreenPolicy(jCheckBoxConsole.isSelected());
            GeneralProperties.setLogFileSaveToFilePolicy(jCheckBoxFileOutput.isSelected());
            GeneralProperties.setLogFileDir(jTextFieldLogDir.getText());

            GeneralProperties.setDefault3DBackgroundColor(jButtonBackgroundColour.getBackground());
            GeneralProperties.setDefaultCellColor3D(jButtonCellColour.getBackground());

            GeneralProperties.setDefault3DAxesOption(jCheckBoxShowAxes.isSelected());
            //GeneralProperties.setDefaultShowDendAxons(JRadioButtonShowDendAxons.isSelected());
            GeneralProperties.setDefaultDisplayOption((String)jComboBoxDisplayOptions.getSelectedItem());
            GeneralProperties.setDefaultShowRegions(jCheckBoxShowRegions.isSelected());
            GeneralProperties.setDefaultShowInputs(jCheckBoxShowInputs.isSelected());

            GeneralProperties.setDefaultShowAxonalArbours(jCheckBoxShowAxonalArbours.isSelected());

            GeneralProperties.setDefaultShowSynapseConns(jCheckBoxShowSynapseConns.isSelected());
            GeneralProperties.setDefaultShowSynapseEndpoints(jCheckBoxShowSynEndPoints.isSelected());

            GeneralProperties.setDefaultResolution3DElements(Integer.parseInt(jTextFieldRes3DElements.getText()));
            GeneralProperties.setDefaultTransparency(Float.parseFloat(jTextFieldRes3DElements.getText()));


/*
            if (jRadioButtonJavaXML.isSelected())
            {
                GeneralProperties.setMorphologySaveFormat(UserSettings.JAVAXML_FORMAT);
            }
            else if (jRadioButtonSaveMorphML.isSelected())
            {
                GeneralProperties.setMorphologySaveFormat(UserSettings.MORPHML_FORMAT);
            }
*/
        }
        else if (this.myStartupMode==PROJECT_PROPERTIES_MODE)
        {
            logger.logComment("Chucking preferences into Project3DProperties...");

            Display3DProperties props3D = myMainFrame.projManager.getProjectDispProps();

            props3D.setBackgroundColour3D(jButtonBackgroundColour.getBackground());
            props3D.setCellColour3D(jButtonCellColour.getBackground());

            props3D.setShow3DAxes(jCheckBoxShowAxes.isSelected());
            //props3D.setShowDendAxons(JRadioButtonShowDendAxons.isSelected());
            props3D.setDisplayOption((String)jComboBoxDisplayOptions.getSelectedItem());
            props3D.setShowRegions(jCheckBoxShowRegions.isSelected());
            props3D.setShowInputs(jCheckBoxShowInputs.isSelected());
            props3D.setShowAxonalArbours(jCheckBoxShowAxonalArbours.isSelected());
            props3D.setShowSynapseConns(jCheckBoxShowSynapseConns.isSelected());
            props3D.setShowSynapseEndpoints(jCheckBoxShowSynEndPoints.isSelected());

            props3D.setResolution3DElements(Integer.parseInt(jTextFieldRes3DElements.getText()));
            props3D.setTransparency(Float.parseFloat(jTextFieldTrans.getText()));


            somethingAlteredInProject = true;

        }

    }

    void jButtonSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Save button pressed...");
        this.saveToGeneralProperties();
        if (somethingAlteredInProject) myMainFrame.applyNew3DSettings();
        this.dispose();


       myMainFrame.alertChangeToolTipsState();
       myMainFrame.updateConsoleOutState();

    }

    public void dispose()
    {
        super.dispose();
        beingDisplayed = false;

    }

    void jButtonBackgroundColour_actionPerformed(ActionEvent e)
    {
        Color currentColour = jButtonBackgroundColour.getBackground();
        Color c = JColorChooser.showDialog(this,
                                           "Please choose a colour for the new Cell Group",
                                           currentColour);
        if (c!=null) // c= null if cancel pressed...
        {
            jButtonBackgroundColour.setBackground(c);
        }


    }

    void jButtonApply_actionPerformed(ActionEvent e)
    {
        this.saveToGeneralProperties();
        myMainFrame.applyNew3DSettings();

    }


    void jCheckBoxFileOutput_actionPerformed(ActionEvent e)
    {
        logger.logComment("Event: "+e);
        if (jCheckBoxFileOutput.isSelected() &&
            !(logger.getInitialSaveToFileState()))
        {
            GuiUtils.showInfoMessage(logger, "Information", "Note that you will have to restart the application for the change to take effect.", this);
        }
    }

    void jCheckBoxToolTips_itemStateChanged(ItemEvent e)
    {
        ToolTipManager ttm = ToolTipManager.sharedInstance();

        ttm.setEnabled(jCheckBoxToolTips.isSelected());
    }

    void jButtonCellColour_actionPerformed(ActionEvent e)
    {
        Color currentColour = jButtonCellColour.getBackground();
        Color c = JColorChooser.showDialog(this,
                                           "Please choose a colour for the new Cell Group",
                                           currentColour);
        if (c!=null) // c= null if cancel pressed...
        {
            jButtonCellColour.setBackground(c);
        }


    }
}
