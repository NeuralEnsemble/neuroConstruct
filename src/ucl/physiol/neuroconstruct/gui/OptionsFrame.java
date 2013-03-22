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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;

import ucl.physiol.neuroconstruct.utils.*;

import java.util.*;
import javax.swing.border.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * General options and preferences dialog
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class OptionsFrame extends JFrame
{
    ClassLogger logger = new ClassLogger("OptionsFrame");

    private static boolean beingDisplayed = false;

    MainFrame mainFrame = null;

    public static final int PROJECT_PROPERTIES_MODE = 0;
    public static final int GENERAL_PROPERTIES_MODE = 1;
    //public static final int NMODL_PROPERTIES_MODE = 2;

    private int myStartupMode;


    ToolTipHelper toolTipText = ToolTipHelper.getInstance();


    JPanel panelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JPanel jPanelProjProps = new JPanel();
    JPanel jPanelLogging = new JPanel();
    JPanel jPanelSaveCancel = new JPanel();
    JPanel jPanelSave = new JPanel();
    
    JButton jButtonSave = new JButton();
    JButton jButtonCancel = new JButton();
    JLabel jLabelLogFileDir = new JLabel();
    JTextField jTextFieldLogDir = new JTextField();
    JCheckBox jCheckBoxConsole = new JCheckBox();
    JCheckBox jCheckBoxFileOutput = new JCheckBox();
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    public final static String GENERAL_PREFERENCES = new String("General Settings");
    public final static String PROJ_PREFERENCES = new String("Project Settings");
    public final static String LOGGING_PREFERENCES = new String("Logging");

    JLabel jLabelBackgroundColour = new JLabel();
    JButton jButtonBackgroundColour = new JButton();
    JCheckBox jCheckBoxShowAxes = new JCheckBox();
    JCheckBox jCheckBoxAntiAliasing = new JCheckBox();

    boolean somethingAlteredInProject = false;
    ButtonGroup buttonGroup3DDisplay = new ButtonGroup();
    JPanel jPanelGeneral = new JPanel();

    JLabel jLabelNeuronDir = new JLabel();
    JTextField jTextFieldNeuronLocation = new JTextField();

    JLabel jLabelPsicsDir = new JLabel();
    JTextField jTextFieldPsicsLocation = new JTextField();
    
    JLabel jLabelPrefProjDir = new JLabel();
    JTextField jTextFieldPrefProjDir = new JTextField();

    JLabel jLabelBrowserDir = new JLabel();
    JTextField jTextFieldBrowser = new JTextField();

    JLabel jLabelEditorDir = new JLabel();
    JTextField jTextFieldEditor = new JTextField();

    JLabel jLabelNumProcs = new JLabel();
    JSpinner jSpinnerNumProcs = null;
    
    JLabel jLabelNeuroML = new JLabel();
    JComboBox jComboBoxNeuroML = new JComboBox();

    JButton jButtonApply = new JButton();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel jLabelShow = new JLabel();


    JCheckBox jCheckBoxShowRegions = new JCheckBox();
    JCheckBox jCheckBoxShowInputs = new JCheckBox();
    JCheckBox jCheckBoxShowAxonalArbours = new JCheckBox();
    JCheckBox jCheckBoxShowSynapseConns = new JCheckBox();
    JCheckBox jCheckBoxShowSynEndPoints = new JCheckBox();
    
    JButton jButtonChangeInput = new JButton();

    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabelCommandLine = new JLabel();
    JTextField jTextFieldCommandLine = new JTextField();
    JLabel jLabelExplaination = new JLabel();
    JLabel jLabel1 = new JLabel();

    JTextPane jTextPaneExplaination = new JTextPane();

    JCheckBox jCheckBoxToolTips = new JCheckBox();
    JCheckBox jCheckBoxGenMatlab = new JCheckBox();
    JCheckBox jCheckBoxGenIgor = new JCheckBox();
    

    JLabel jLabelSaveInfo = new JLabel();
    JRadioButton jRadioButtonSaveSer = new JRadioButton();
    JRadioButton jRadioButtonJavaXML = new JRadioButton();
    JRadioButton jRadioButtonNML1 = new JRadioButton();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    ButtonGroup buttonGroupSaveOptions = new ButtonGroup();

    JLabel jLabelMinRadius = new JLabel();
    JTextField jTextFieldMinRadius = new JTextField();

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
        mainFrame = parentFrame;
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
       this.jCheckBoxAntiAliasing.setToolTipText(toolTipText.getToolTip("Anti Aliasing"));
       this.jButtonCellColour.setToolTipText(toolTipText.getToolTip("3D Settings cell colour"));
       this.jLabelRes3DElements.setToolTipText(toolTipText.getToolTip("3D Resolution"));
       this.jTextFieldRes3DElements.setToolTipText(toolTipText.getToolTip("3D Resolution"));

       this.jLabelMinRadius.setToolTipText(toolTipText.getToolTip("Minimum radius to display for 3D segments"));
       this.jTextFieldMinRadius.setToolTipText(toolTipText.getToolTip("Minimum radius to display for 3D segments"));
       
       
        jPanelSave.setToolTipText(toolTipText.getToolTip("Morphology save format"));

    }


    private void jbInit() throws Exception
    {
        panelMain.setLayout(borderLayout1);

        Dimension dim = new Dimension(550, 550);

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

        jCheckBoxAntiAliasing.setSelected(true);
        jCheckBoxAntiAliasing.setText("Use anti-aliasing");


        jPanelGeneral.setDebugGraphicsOptions(0);
        jPanelGeneral.setRequestFocusEnabled(true);
        jPanelGeneral.setLayout(gridBagLayout2);

        jLabelNeuronDir.setText("NEURON home directory:");
        jTextFieldNeuronLocation.setText("");
        jTextFieldNeuronLocation.setColumns(20);

        jLabelPsicsDir.setText("PSICS jarfile:");
        jTextFieldPsicsLocation.setText("");
        jTextFieldPsicsLocation.setColumns(20);

        jLabelPrefProjDir.setText("Default location projects:");
        jTextFieldPrefProjDir.setText("");
        jTextFieldPrefProjDir.setColumns(20);

        jLabelBrowserDir.setText("Path to browser:");
        jTextFieldBrowser.setText("");
        jTextFieldBrowser.setColumns(20);


        jLabelEditorDir.setText("Path to text editor:");
        jTextFieldEditor.setText("");
        jTextFieldEditor.setColumns(20);

        jLabelNumProcs.setText("Num processes for generate:");
        SpinnerModel model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        jSpinnerNumProcs = new JSpinner(model);

        ((JSpinner.DefaultEditor)jSpinnerNumProcs.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.LEFT);



        String info = "<html>Multithreading: Select the max number of processes to run at one time when generating networks in neuroConstruct.<br>" +
                      "Note for a quad core machine, up to 4 processes can run be at once for generating the network. As all processes<br>" +
                      "(or threads) are accessing some common data, the speedup will be less than 4. Multithreading is only implemented<br>" +
                      "for a few parts of the network generation, currently Morphology based network connections only. </html>";

        jLabelNumProcs.setToolTipText(info);
        jSpinnerNumProcs.setToolTipText(info);

        jLabelNeuroML.setText("NeuroML version:");
        //jTextFieldNeuroML.setText("");
        //jTextFieldNeuroML.setColumns(20);



        jButtonApply.setText("Apply");
        jButtonApply.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonApply_actionPerformed(e);
            }
        });
        jPanelProjProps.setDebugGraphicsOptions(0);
        jPanelProjProps.setLayout(gridBagLayout3);
        jLabelShow.setText("Display:");
        jCheckBoxShowRegions.setText("Regions");
        jCheckBoxShowInputs.setText("Inputs");
        jCheckBoxShowAxonalArbours.setText("Axonal arbours");
        jCheckBoxShowSynapseConns.setText("Synaptic connections");

        jButtonChangeInput.setText("Change...");

        jButtonChangeInput.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonChangeInput_actionPerformed(e);
            }
        });
        
        jLabelCommandLine.setText("Command line:");

        jTextPaneExplaination.setContentType("text/html");

        jTextPaneExplaination.setText("The command needed to run processes in a new terminal window. "+
                                      "This would usually would be: <b>cmd /K start /wait</b> for Windows, <b>gnome-terminal -x</b> for Gnome desktop Linux (e.g. Ubuntu) "+
                                      "or <b>konsole</b> for KDE on Linux");
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
        
        
        jCheckBoxGenIgor.setText("Generate Igor files");
        jCheckBoxGenIgor.setToolTipText("Select this so that a number of helper files will be generated with every simulation, \n"
            +"to enable easy loading of traces into IgorPro/Neuromatic (Windows/Mac only). \nLook for *.ipf files.");
        
        
        jCheckBoxGenMatlab.setText("Generate Matlab files");
        jCheckBoxGenMatlab.setToolTipText("Select this so that a number of helper files will be generated with every simulation, \n"
            +"to enable easy loading of traces into Matlab/Octave. \nLook for *.m files.");
        
        
        jCheckBoxShowSynEndPoints.setText("Synaptic endpoints");
        jLabelSaveInfo.setText("Format to save morphology files in:");
        jRadioButtonSaveSer.setText("Serialised");
        jRadioButtonJavaXML.setText("Java XML");
        jRadioButtonNML1.setText("NeuroML v1.8.1");
        //jPanelSave.setLayout(gridBagLayout4);
        //jPanelSave.setAlignmentX((float) 0.5);
        //jPanelSave.setAlignmentY((float) 0.0);
        jLabelRes3DElements.setText("Resolution 3D elements:");
        jLabelTrans.setText("Level of transparency (~0.8 -> 1):");


        jLabelMinRadius.setText("Min radius segments:");

        jTextFieldMinRadius.setText("0");
        jTextFieldMinRadius.setColumns(5);
        jTextFieldMinRadius.setMinimumSize(new Dimension(100,20));
        jTextFieldMinRadius.setHorizontalAlignment(SwingConstants.TRAILING);

        jTextFieldRes3DElements.setText("30");
        jTextFieldRes3DElements.setColumns(5);
        jTextFieldRes3DElements.setMinimumSize(new Dimension(100,20));
        jTextFieldRes3DElements.setHorizontalAlignment(SwingConstants.TRAILING);

        this.jTextFieldTrans.setText("0.7");
        jTextFieldTrans.setColumns(5);
        jTextFieldTrans.setMinimumSize(new Dimension(100,20));

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
        
        jPanelSave.add(new JLabel("Morphology save format:"));
        jPanelSave.add(jRadioButtonSaveSer);
        jPanelSave.add(jRadioButtonJavaXML);
        jPanelSave.add(jRadioButtonNML1);
        
        
        jLabelDisplayOptoin.setText("Display: ");

        getContentPane().add(panelMain);
        panelMain.add(jTabbedPane1, BorderLayout.CENTER);

        jTabbedPane1.add(jPanelGeneral, GENERAL_PREFERENCES);



        //jTabbedPane1.add(jPanelSave, SAVE_PREFERENCES);

        jTabbedPane1.add(jPanelProjProps,    PROJ_PREFERENCES);
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



        
        jPanelProjProps.add(jPanelSave,
                     new GridBagConstraints(0, 0, 5, 1, 0.0, 0.0
                                            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 0), 0,
                                            0));
        

        

        jPanelProjProps.add(jLabelBackgroundColour,
                     new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0
                                            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 0), 0,0));

        jPanelProjProps.add(jButtonBackgroundColour, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
                                            , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));

        
        
        jPanelProjProps.add(jLabelCellColour, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0
                                         , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 0), 0, 0));
        
        jPanelProjProps.add(jButtonCellColour, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
                                         , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 12, 6, 0), 0, 0));
        
        

        jPanelProjProps.add(jCheckBoxShowAxes, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                                  GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));

        jPanelProjProps.add(jCheckBoxAntiAliasing, new GridBagConstraints(1, 3, 4, 1, 0.0, 0.0,
                                  GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 6, 0), 0, 0));


        

        jPanelProjProps.add(jComboBoxDisplayOptions, new GridBagConstraints(1, 4, 3, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(7, 0, 5, 0), 0, 0));

        jPanelProjProps.add(jLabelDisplayOptoin, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                                 , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));



        jPanelProjProps.add(jLabelMinRadius, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));

        jPanelProjProps.add(jTextFieldMinRadius, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(6, 0, 6, 0), 0, 0));

        jPanelProjProps.add(jLabelRes3DElements, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));

        jPanelProjProps.add(jTextFieldRes3DElements, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(6, 0, 6, 0), 0, 0));


        jPanelProjProps.add(this.jLabelTrans, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0
                                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                 new Insets(6, 12, 6, 0), 0, 0));

        jPanelProjProps.add(jTextFieldTrans, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0
                                                                     , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                     new Insets(6, 0, 6, 0), 0, 0));






        jPanelProjProps.add(jLabelShow, new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 0), 0, 0));

        jPanelProjProps.add(jCheckBoxShowRegions, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 6, 0, 0), 0, 0));
        
        
        

        jPanelProjProps.add(jCheckBoxShowInputs, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
        
        jPanelProjProps.add(jButtonChangeInput, new GridBagConstraints(4, 9, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));

             
             
        jPanelProjProps.add(jCheckBoxShowAxonalArbours, new GridBagConstraints(3, 10, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
        
        

        jPanelProjProps.add(jCheckBoxShowSynapseConns, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
        
        

        jPanelProjProps.add(jCheckBoxShowSynEndPoints, new GridBagConstraints(3, 12, 1, 1, 0.0, 0.0,
                                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 6, 0, 0), 0, 0));
        
        


        jPanelProjProps.add(jButtonApply, new GridBagConstraints(0, 13, 5, 1, 0.0, 0.0
                                                          , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                          new Insets(6, 0, 12, 0), 0, 0));


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

        jPanelGeneral.add(jLabelPsicsDir,
                          new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));
        
        jPanelGeneral.add(jTextFieldPsicsLocation,
                          new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        jPanelGeneral.add(jLabelBrowserDir,
                          new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jTextFieldBrowser,
                          new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        jPanelGeneral.add(jLabelEditorDir,
                          new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jTextFieldEditor,
                          new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        if (false)   /// Disabling multithreading as it's unstable. May be readded later
        {
            jPanelGeneral.add(jLabelNumProcs,
                          new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        
            jPanelGeneral.add(jSpinnerNumProcs,
                          new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));
        }
        
        jPanelGeneral.add(jLabelNeuroML,
                          new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 6), 0, 0));

        jPanelGeneral.add(jComboBoxNeuroML,
                          new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));

        jPanelGeneral.add(jLabelCommandLine,
                          new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, 
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 20, 0, 6), 0, 0));

        jPanelGeneral.add(jTextFieldCommandLine,
                          new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, 
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 6, 6, 20), 0, 0));
        jPanelGeneral.add(jLabelExplaination,
                          new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(6, 6, 0, 0), 0, 0));
        jPanelGeneral.add(jTextPaneExplaination,
                          new GridBagConstraints(0, 8, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, 
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(6, 20, 6, 20), 0, 0));
        jPanelGeneral.add(jLabel1,
                          new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER, 
                                                 GridBagConstraints.NONE,
                                                 new Insets(0, 0, 0, 0), 0, 0));
        
        jPanelGeneral.add(jCheckBoxToolTips,
                          new GridBagConstraints(0, 9, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 12), 0, 0));


        
        jPanelGeneral.add(jCheckBoxGenMatlab,
                          new GridBagConstraints(0, 10, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                 new Insets(6, 20, 6, 12), 0, 0));

        jPanelGeneral.add(jCheckBoxGenIgor,
                          new GridBagConstraints(0, 11, 2, 1, 0.0, 0.0
                                                 , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                 new Insets(6, 20, 12, 12), 0, 0));


        
        
        
        
        

        buttonGroupSaveOptions.add(jRadioButtonJavaXML);
        buttonGroupSaveOptions.add(jRadioButtonSaveSer);
        buttonGroupSaveOptions.add(jRadioButtonNML1);


    }

    private void extraInit()
    {
        ToolTipManager ttm = ToolTipManager.sharedInstance();

        jCheckBoxToolTips.setSelected(ttm.isEnabled());

        if (this.myStartupMode==PROJECT_PROPERTIES_MODE)
        {
            this.jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfTab(GENERAL_PREFERENCES),false);
            this.jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfTab(LOGGING_PREFERENCES),false);

            Display3DProperties props3D = mainFrame.projManager.getProjectDispProps();
            
            
            ProjectProperties pp = mainFrame.projManager.getProjectProps();
            
            
            String saveOpt = pp.getPreferredSaveFormat();
            

            if (saveOpt.equals(ProjectStructure.JAVA_XML_FORMAT))
            {
                jRadioButtonJavaXML.setSelected(true);
            }
            else if (saveOpt.equals(ProjectStructure.JAVA_OBJ_FORMAT))
            {
                jRadioButtonSaveSer.setSelected(true);
            }
            else if (saveOpt.equals(ProjectStructure.NEUROML1_FORMAT))
            {
                jRadioButtonNML1.setSelected(true);
            }


            jButtonBackgroundColour.setBackground(props3D.getBackgroundColour3D());
            jButtonCellColour.setBackground(props3D.getCellColour3D());

            jCheckBoxShowAxes.setSelected(props3D.getShow3DAxes());
            if (props3D.getAntiAliasing()==Display3DProperties.AA_NOT_SET)
            {
                mainFrame.projManager.getCurrentProject().checkAboutAA();
            }
            if (props3D.getAntiAliasing()==Display3DProperties.AA_ON) jCheckBoxAntiAliasing.setSelected(true);
            if (props3D.getAntiAliasing()==Display3DProperties.AA_OFF) jCheckBoxAntiAliasing.setSelected(false);

            //JRadioButtonShowDendAxons.setSelected(props3D.getShowDendAxons());

            jCheckBoxShowRegions.setSelected(props3D.getShowRegions());
            jCheckBoxShowInputs.setSelected(props3D.getShowInputs());
            jCheckBoxShowAxonalArbours.setSelected(props3D.getShowAxonalArbours());
            jCheckBoxShowSynapseConns.setSelected(props3D.getShowSynapseConns());
            jCheckBoxShowSynEndPoints.setSelected(props3D.getShowSynapseEndpoints());


            jTextFieldMinRadius.setText(props3D.getMinRadius()+"");
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

        else if (this.myStartupMode==GENERAL_PROPERTIES_MODE)
        {
            jTextFieldBrowser.setText(GeneralProperties.getBrowserPath(false));
            jTextFieldEditor.setText(GeneralProperties.getEditorPath(false));

            jSpinnerNumProcs.setValue(GeneralProperties.getNumProcessorstoUse());

            jTextFieldNeuronLocation.setText(GeneralProperties.getNeuronHomeDir());
            
            String psicsJar = GeneralProperties.getPsicsJar();
            if (psicsJar!=null)
                jTextFieldPsicsLocation.setText(GeneralProperties.getPsicsJar());
            
            jTextFieldCommandLine.setText(GeneralProperties.getExecutableCommandLine());
            jTextFieldPrefProjDir.setText(GeneralProperties.getnCProjectsDir().getAbsolutePath());

            jCheckBoxConsole.setSelected(GeneralProperties.getLogFilePrintToScreenPolicy());
            jCheckBoxFileOutput.setSelected(GeneralProperties.getLogFileSaveToFilePolicy());

            jTextFieldLogDir.setText(GeneralProperties.getLogFileDir().getAbsolutePath());

            jButtonBackgroundColour.setBackground(GeneralProperties.getDefault3DBackgroundColor());
            jButtonCellColour.setBackground(GeneralProperties.getDefaultCellColor3D());

            jCheckBoxShowAxes.setSelected(GeneralProperties.getDefault3DAxesOption());
            if (GeneralProperties.getDefaultAntiAliasing()==Display3DProperties.AA_NOT_SET)
            {
                mainFrame.projManager.getCurrentProject().checkAboutAA();
            }
            if (GeneralProperties.getDefaultAntiAliasing()==Display3DProperties.AA_ON) jCheckBoxAntiAliasing.setSelected(true);
            if (GeneralProperties.getDefaultAntiAliasing()==Display3DProperties.AA_OFF) jCheckBoxAntiAliasing.setSelected(false);

            //JRadioButtonShowDendAxons.setSelected(GeneralProperties.getDefaultShowDendAxons());
            jCheckBoxShowRegions.setSelected(GeneralProperties.getDefaultShowRegions());
            jCheckBoxShowInputs.setSelected(GeneralProperties.getDefaultShowInputs());
            jCheckBoxShowAxonalArbours.setSelected(GeneralProperties.getDefaultShowAxonalArbours());

            jCheckBoxShowSynapseConns.setSelected(GeneralProperties.getDefaultShowSynapseConns());
            jCheckBoxShowSynEndPoints.setSelected(GeneralProperties.getDefaultShowSynapseEndpoints());

            jTextFieldRes3DElements.setText(GeneralProperties.getDefaultMinRadius()+"");
            jTextFieldRes3DElements.setText(GeneralProperties.getDefaultResolution3DElements()+"");
            jTextFieldTrans.setText(GeneralProperties.getDefaultTransparency()+"");



            String dendDispOpt = GeneralProperties.getDefaultDisplayOption();

            Vector displayOptions = Display3DProperties.getDisplayOptions(false);

            for (int i = 0; i < displayOptions.size(); i++)
            {
                jComboBoxDisplayOptions.addItem(displayOptions.elementAt(i));
            }
            jComboBoxDisplayOptions.setSelectedItem(dendDispOpt);
            
            jCheckBoxGenMatlab.setSelected(GeneralProperties.getGenerateMatlab());
            jCheckBoxGenIgor.setSelected(GeneralProperties.getGenerateIgor());


            String saveOpt = GeneralProperties.getDefaultPreferredSaveFormat();

            if (saveOpt.equals(ProjectStructure.JAVA_XML_FORMAT))
            {
                jRadioButtonJavaXML.setSelected(true);
            }
            else if (saveOpt.equals(ProjectStructure.JAVA_OBJ_FORMAT))
            {
                jRadioButtonSaveSer.setSelected(true);
            }
            else if (saveOpt.equals(ProjectStructure.NEUROML1_FORMAT))
            {
                jRadioButtonNML1.setSelected(true);
            }
            
            
            Border b0 = BorderFactory.createLineBorder(Color.gray, 2);
            Border b = BorderFactory.createTitledBorder(b0, "DEFAULT Project Properties");
            
            jPanelProjProps.setBorder(b);
            
            
            File schemataDir = ProjectStructure.getNeuroMLSchemataDir();
            
            File[] contents = schemataDir.listFiles();
            
            contents = GeneralUtils.reorderAlphabetically(contents, true);
            for(File f: contents)
            {
                if (f.isDirectory() && f.getName().startsWith("v"))
                {
                    jComboBoxNeuroML.addItem(f.getName());
                }
            }
            String tip = "Version of NeuroML specs to use for validation of ChannelML files, etc. \n" +
                    "List generated from directories at "+ schemataDir;
            
            jComboBoxNeuroML.setToolTipText(tip);
            
            jLabelNeuroML.setToolTipText(tip);
            
            if (GeneralProperties.getNeuroMLVersionString()!=null)
                jComboBoxNeuroML.setSelectedItem(GeneralProperties.getNeuroMLVersionString());
            else
            {
                jComboBoxNeuroML.setSelectedItem(GeneralProperties.getLatestNeuroMLVersionString());
                GeneralProperties.setNeuroMLVersionString(GeneralProperties.getLatestNeuroMLVersionString());
            }
                
                    

        }

    }


    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            beingDisplayed = false;
        }
        super.processWindowEvent(e);
    }

    @Override
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

    private boolean saveToGeneralProperties()
    {

        if (this.myStartupMode==GENERAL_PROPERTIES_MODE)
        {
            File propDir = new File(jTextFieldPrefProjDir.getText());
            
            if (propDir.getAbsolutePath().indexOf(" ")>=0)
            {

                GuiUtils.showErrorMessage(logger, "The name of the path for new projects: "+propDir.getAbsolutePath()+" should not contain any spaces.\n"+
                        "This is due to possible errors when processes are started (e.g. NEURON running) which need to be handed arguments containing full pathnames.\n"
                        +"Please select another path without spaces.", null, this);
                
                return false;
            }

            if (!propDir.exists())
            {
            	if (propDir.getParentFile()==null || !propDir.getParentFile().exists())
            	{
            		GuiUtils.showErrorMessage(logger, "Neither "+propDir.getAbsolutePath()+" nor it's parent directory exist. Please choose a correct default location for new projects.", null, this);
            		return false;
            	}
            	else
            	{
            		propDir.mkdir();
            	}
            }
            GeneralProperties.setnCProjectsDir(propDir);
            
            
            GeneralProperties.setNeuronHomeDir(jTextFieldNeuronLocation.getText());
            GeneralProperties.setPsicsJar(jTextFieldPsicsLocation.getText());
            
            GeneralProperties.setBrowserPath(jTextFieldBrowser.getText());
            GeneralProperties.setEditorPath(jTextFieldEditor.getText());
            GeneralProperties.setExecutableCommandLine(jTextFieldCommandLine.getText());
            
            
            GeneralProperties.setNumProcessorstoUse((Integer)jSpinnerNumProcs.getValue());

            GeneralProperties.setGenerateMatlab(jCheckBoxGenMatlab.isSelected());
            GeneralProperties.setGenerateIgor(jCheckBoxGenIgor.isSelected());

            
            GeneralProperties.setNeuroMLVersionString((String)jComboBoxNeuroML.getSelectedItem());
            

        
        }


        if (this.myStartupMode==GENERAL_PROPERTIES_MODE)
        {
            logger.logComment("Chucking preferences into GeneralProperties...");
            
            
            if (jRadioButtonJavaXML.isSelected())
                GeneralProperties.setDefaultPreferredSaveFormat(ProjectStructure.JAVA_XML_FORMAT);

            else if (jRadioButtonSaveSer.isSelected())
                GeneralProperties.setDefaultPreferredSaveFormat(ProjectStructure.JAVA_OBJ_FORMAT);

            else if (jRadioButtonNML1.isSelected())
                GeneralProperties.setDefaultPreferredSaveFormat(ProjectStructure.NEUROML1_FORMAT);
            
        

            GeneralProperties.setLogFilePrintToScreenPolicy(jCheckBoxConsole.isSelected());
            GeneralProperties.setLogFileSaveToFilePolicy(jCheckBoxFileOutput.isSelected());
            GeneralProperties.setLogFileDir(jTextFieldLogDir.getText());

            GeneralProperties.setDefault3DBackgroundColor(jButtonBackgroundColour.getBackground());
            GeneralProperties.setDefaultCellColor3D(jButtonCellColour.getBackground());

            GeneralProperties.setDefault3DAxesOption(jCheckBoxShowAxes.isSelected());
            GeneralProperties.setDefaultAntiAliasing(jCheckBoxAntiAliasing.isSelected()? Display3DProperties.AA_ON : Display3DProperties.AA_OFF);
            
            GeneralProperties.setDefaultDisplayOption((String)jComboBoxDisplayOptions.getSelectedItem());
            GeneralProperties.setDefaultShowRegions(jCheckBoxShowRegions.isSelected());
            GeneralProperties.setDefaultShowInputs(jCheckBoxShowInputs.isSelected());
           
            GeneralProperties.setDefaultShowAxonalArbours(jCheckBoxShowAxonalArbours.isSelected());

            GeneralProperties.setDefaultShowSynapseConns(jCheckBoxShowSynapseConns.isSelected());
            GeneralProperties.setDefaultShowSynapseEndpoints(jCheckBoxShowSynEndPoints.isSelected());

            GeneralProperties.setDefaultMinRadius(Float.parseFloat(jTextFieldMinRadius.getText()));

            GeneralProperties.setDefaultResolution3DElements(Integer.parseInt(jTextFieldRes3DElements.getText()));
            GeneralProperties.setDefaultTransparency(Float.parseFloat(jTextFieldTrans.getText()));


        }
        else if (this.myStartupMode==PROJECT_PROPERTIES_MODE)
        {
            logger.logComment("Chucking preferences into ProjectProperties...");
            
            if (jRadioButtonJavaXML.isSelected())
                mainFrame.projManager.getProjectProps().setPreferredSaveFormat(ProjectStructure.JAVA_XML_FORMAT);

            else if (jRadioButtonSaveSer.isSelected())
                mainFrame.projManager.getProjectProps().setPreferredSaveFormat(ProjectStructure.JAVA_OBJ_FORMAT);

            else if (jRadioButtonNML1.isSelected())
                mainFrame.projManager.getProjectProps().setPreferredSaveFormat(ProjectStructure.NEUROML1_FORMAT);
            

            Display3DProperties props3D = mainFrame.projManager.getProjectDispProps();

            props3D.setBackgroundColour3D(jButtonBackgroundColour.getBackground());
            props3D.setCellColour3D(jButtonCellColour.getBackground());

            props3D.setShow3DAxes(jCheckBoxShowAxes.isSelected());
            props3D.setAntiAliasing(jCheckBoxAntiAliasing.isSelected()? Display3DProperties.AA_ON : Display3DProperties.AA_OFF);
            
            props3D.setDisplayOption((String)jComboBoxDisplayOptions.getSelectedItem());
            props3D.setShowRegions(jCheckBoxShowRegions.isSelected());
            props3D.setShowInputs(jCheckBoxShowInputs.isSelected());
            props3D.setShowAxonalArbours(jCheckBoxShowAxonalArbours.isSelected());
            props3D.setShowSynapseConns(jCheckBoxShowSynapseConns.isSelected());
            props3D.setShowSynapseEndpoints(jCheckBoxShowSynEndPoints.isSelected());

            props3D.setMinRadius(Float.parseFloat(jTextFieldMinRadius.getText()));
            props3D.setResolution3DElements(Integer.parseInt(jTextFieldRes3DElements.getText()));
            
            props3D.setTransparency(Float.parseFloat(jTextFieldTrans.getText()));


            somethingAlteredInProject = true;

        }
        return true;

    }

    void jButtonSave_actionPerformed(ActionEvent e)
    {
        logger.logComment("Save button pressed...");
        boolean cont = this.saveToGeneralProperties();
        if (somethingAlteredInProject) mainFrame.applyNew3DSettings();
        if (cont) this.dispose();


       mainFrame.alertChangeToolTipsState();
       mainFrame.updateConsoleOutState();

    }

    @Override
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
    
    
    void jButtonChangeInput_actionPerformed(ActionEvent e)
    {
        Vector<String> InputType = new Vector<String>();
        InputType.add(Display3DProperties.DISPLAY_INPUTS_AS_PROBES);
        InputType.add(Display3DProperties.DISPLAY_INPUTS_AS_SPHERES);
        String[] names = new String[InputType.size()];
        for(int i=0;i<InputType.size();i++)
        {
            names[i] = InputType.get(i);
        }
        String selectedType  
                = (String)JOptionPane.showInputDialog(this, "Please select the type of shape to use for the electrical input locations: ", 
                     "Input types", JOptionPane.QUESTION_MESSAGE, null, names, mainFrame.projManager.getProjectDispProps().getShowInputsAs());
        
        mainFrame.projManager.getProjectDispProps().setShowInputsAs(selectedType);
    }
    

    void jButtonApply_actionPerformed(ActionEvent e)
    {
        this.saveToGeneralProperties();
        mainFrame.applyNew3DSettings();
        
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
