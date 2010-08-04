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
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.gui.view2d.*;
import ucl.physiol.neuroconstruct.project.cellchoice.AllCells;
import ucl.physiol.neuroconstruct.project.cellchoice.CellChooser;
import ucl.physiol.neuroconstruct.project.cellchoice.RegionAssociatedCells;

/**
 * Frame for rerunning simulations recorded in neuron
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimulationRerunFrame extends JFrame
{

    private ClassLogger logger = new ClassLogger("SimulationRerunFrame");

    private SimulationInterface controlledPanel;

    private File mySimulationDir = null;

    private Hashtable<String, ActivityMonitor> activityMonitors = new Hashtable<String, ActivityMonitor>();

    private ArrayList<ViewVoltage2D> view2Ds = new ArrayList<ViewVoltage2D>();


    private Project project = null;

    private String startSimString = "Replay Simulation";
    private String restartSimString = "Restart";


    private int numTimeSteps = 10;

    private javax.swing.Timer simulationTimer = null;

    public static float mostNegValue = -90;
    public static float mostPosValue = 30;

    private long startSim = -1;

    public static double smallestFreq = 10;  // 100ms ISI
    public static double largestFreq = 100; // 10ms ISI

    private static int VOLTAGE_LINEAR_SHADING = 0;
    private static int VOLTAGE_RAINBOW_SHADING = 0;
    private static int ISI_SHADING = 1;
    private static int FREQ_SHADING = 2;
    
    ArrayList<JCheckBox> cellGroupsCheckBoxes = new ArrayList<JCheckBox>();
    //ArrayList<CellChooser> cellChooser = new ArrayList<CellChooser>();
    Hashtable<String, CellChooser> cellGroupsToUse = new Hashtable<String, CellChooser>();
    

    private Hashtable<String, ISIStateInfo> isiInfo = null;

    private double[][] rerunValues = null;

    /**
     * CellItem is a CellSegRef or SynapseRef...
     */
    private String[] rerunCellItemRefs = null;

    ArrayList<String> currentCellItemRefs = null;
    
    boolean standalone = false;


    private SimulationData myCurrSimData = null;

    float thresholdForRun = Float.MAX_VALUE;

    private boolean moreShown = false;

    JLabel jLabelVars = new JLabel();
    JComboBox jComboBoxVars = new JComboBox();

    JPanel jPanelMain = new JPanel();
    JPanel jPanelRun = new JPanel();
    JPanel jPanelVars = new JPanel();
    JPanel jPanelLoop = new JPanel();
    

    JLabel jLabelTimeStep = new JLabel();
    JFormattedTextField jTextFieldDelay = new JFormattedTextField();
    JButton jButtonStart = new JButton();
    JSlider jRunSlider = new JSlider();
    JTextField jTextFieldSimulationTime = new JTextField();
    JButton jButtonStop = new JButton();
    ButtonGroup buttonGroupFileOrDB = new ButtonGroup();
    FlowLayout flowLayout1 = new FlowLayout();
    Border border1;

    FlowLayout flowLayout2 = new FlowLayout();
    JPanel jPanelScale = new JPanel();
    JLabel jLabelHigh = new JLabel();

    ButtonGroup buttonGroup = new ButtonGroup();

    JButton jButton2D = new JButton();

    JLabel jLabelV1 = new JLabel("  ");
    JLabel jLabelV2 = new JLabel("  ");
    JLabel jLabelV3 = new JLabel("  ");
    JLabel jLabelV4 = new JLabel("  ");
    JLabel jLabelV5 = new JLabel("  ");
    JLabel jLabelV6 = new JLabel("  ");
    JLabel jLabelV7 = new JLabel("  ");
    JLabel jLabelV8 = new JLabel("  ");
    JLabel jLabelV9 = new JLabel("  ");
    JLabel jLabelV10 = new JLabel("  ");
    JLabel jLabelV11= new JLabel("  ");
    JLabel jLabelV12 = new JLabel("  ");
    JLabel jLabelV13 = new JLabel("  ");
    JLabel jLabelV14 = new JLabel("  ");
    JLabel jLabelV15 = new JLabel("  ");
    JLabel jLabelV16 = new JLabel("  ");

    JLabel jLabelLow = new JLabel();

    Border border2;
    JPanel jPanelOptions = new JPanel();

    //JRadioButton jRadioButtonContinuous = new JRadioButton();
    JRadioButton jRadioButtonShadLinear = new JRadioButton();
    JRadioButton jRadioButtonShadRainbow = new JRadioButton();
    JRadioButton jRadioButtonSpikesOnly = new JRadioButton();
    JRadioButton jRadioButtonISIShading = new JRadioButton();


    JFormattedTextField  jTextFieldSpikeThreshold = null;
    JLabel jLabelMv = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    //JPanel jPanelColouring = new JPanel();
    JPanel jPanelSpiking2Options = new JPanel();
    JPanel jPanelSpikingInfo = new JPanel();

    JLabel jLabelSpikingThresh = new JLabel();
    JCheckBox jCheckBoxActivityMonitors = new JCheckBox();
    JPanel jPanelNumbers = new JPanel();
    JPanel jPanelExtras = new JPanel();
    JPanel jPanelMainOptions = new JPanel();
    JPanel jPanelCellsToUpdate = new JPanel();
    JPanel jPanelListCellGroups = new JPanel();
    JPanel jPanelOtherCells = new JPanel();
    JPanel jPanelShowMore = new JPanel();
    JButton jButtonShowMore = new JButton(">>");
    
    
    JCheckBox jCheckBoxLoop = new JCheckBox("Loop");
    
    
    ButtonGroup bgOtherCells = new ButtonGroup();
    JRadioButton jRadioButtonBlacken = new JRadioButton();
    JRadioButton jRadioButtonTransp = new JRadioButton();
    
    JLabel jLabelFramesToSkip = new JLabel();
    JFormattedTextField jTextFieldFramesToSkip = new JFormattedTextField();



    public SimulationRerunFrame(Project project,
                                File simulationDir,
                                SimulationInterface simInf)
    {
        controlledPanel = simInf;
        mySimulationDir = simulationDir;

        this.project = project;

        logger.logComment("SimulationRerunFrame created...");
        try
        {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
            jbInit();

            addGradedScale(VOLTAGE_LINEAR_SHADING);
            jRadioButtonShadLinear.setSelected(true);
            extraInit();
        }
        catch(Exception e)
        {
            logger.logComment("Exception starting GUI: "+ e);
        }
    }

    // needed when the 3D panel is reset...
    public void setSimInterface(SimulationInterface simInf)
    {
        controlledPanel = simInf;
    }

/*
    public void addListener(VoltageEventListener vel, String cellGroup)
    {
        ArrayList<VoltageEventListener> velList = listeners.get(cellGroup);
        velList.add(vel);
    }
*/

    private void jbInit() throws Exception
    {
        NumberFormat format1 = NumberFormat.getInstance();
        format1.setMaximumFractionDigits(2);

        jLabelVars.setText("Variable: ");
        this.jPanelVars.add(jLabelVars);
        this.jPanelVars.add(jComboBoxVars);
        
        
        jPanelVars.add(this.jCheckBoxLoop, null);
        jCheckBoxLoop.setToolTipText("Rerun simulation after finishing");

        jTextFieldSpikeThreshold = new JFormattedTextField(format1);

        NumberFormat format2 = NumberFormat.getInstance();
        format2.setMaximumFractionDigits(0);
        jTextFieldDelay = new JFormattedTextField(format2);

        NumberFormat format3 = NumberFormat.getInstance();
        format3.setMaximumFractionDigits(0);
        jTextFieldFramesToSkip = new JFormattedTextField(format3);


        border1 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),BorderFactory.createEmptyBorder(5,5,5,5));

        border2 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(134, 134, 134)),BorderFactory.createEmptyBorder(5,5,5,5));
        
        //////////this.setResizable(false);
        //this.setTitle("Rerun Simulation");
        jPanelMain.setBorder(border1);
        
        jPanelMain.setRequestFocusEnabled(true);
        
        jLabelTimeStep.setRequestFocusEnabled(true);
        jLabelTimeStep.setText("Delay (ms):");

        jTextFieldDelay.setColumns(3);
        jButtonStart.setEnabled(false);
        jButtonStart.setText(startSimString);
        jButtonStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStart_actionPerformed(e);
            }
        });
        jTextFieldSimulationTime.setEnabled(false);
        jTextFieldSimulationTime.setEditable(false);
        jTextFieldSimulationTime.setText("0");
        jTextFieldSimulationTime.setColumns(6);
        jTextFieldSimulationTime.setHorizontalAlignment(SwingConstants.RIGHT);
        
        jRunSlider.setMajorTickSpacing(20);

        Dimension sd = new Dimension(200, 28);
        jRunSlider.setMaximumSize(sd);
        jRunSlider.setMinimumSize(sd);
        jRunSlider.setPreferredSize(sd);
        jRunSlider.setValue(0);
        jRunSlider.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                jSlider1_mouseReleased(e);
            }
        });


        jComboBoxVars.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxVars_itemStateChanged(e);
            }
        });




        jPanelRun.setBorder(BorderFactory.createEtchedBorder());
        Dimension runDim = new Dimension(300, 76);
        jPanelRun.setMaximumSize(runDim);
        jPanelRun.setMinimumSize(runDim);
        jPanelRun.setPreferredSize(runDim);
        jPanelRun.setOpaque(true);
        jPanelRun.setLayout(flowLayout1);
        jButtonStop.setEnabled(false);
        jButtonStop.setText("Pause");
        jButtonStop.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStop_actionPerformed(e);
            }
        });
        jButton2D.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButton2D_actionPerformed(e);
            }
        });

        
        
        jPanelSpiking2Options.setPreferredSize(new Dimension(300, 27));
        this.jPanelSpikingInfo.setPreferredSize(new Dimension(300, 27));

        jPanelOptions.setMaximumSize(new Dimension(300, 100));
        jPanelOptions.setMinimumSize(new Dimension(300, 100));
        jPanelOptions.setOpaque(true);
        jPanelOptions.setPreferredSize(new Dimension(300, 100));

        this.setTitle("Simulation Reference: "+ mySimulationDir.getName());


        buttonGroup.add(jRadioButtonShadLinear);
        buttonGroup.add(jRadioButtonShadRainbow);
        buttonGroup.add(jRadioButtonSpikesOnly);
        buttonGroup.add(jRadioButtonISIShading);
        


        
        this.jRadioButtonShadLinear.setText("Linear v(t)");
        this.jRadioButtonShadRainbow.setText("Rainbow v(t)");
        
        this.jRadioButtonISIShading.setText("ISI shading");
        this.jRadioButtonSpikesOnly.setText("Spikes Only Threshold:");



        jButton2D.setText("Add 2D view");
        jButton2D.setToolTipText("In development...");

        jRadioButtonSpikesOnly.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButton_actionPerformed(e);
            }
        });
        jRadioButtonShadLinear.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButton_actionPerformed(e);
            }
        });
        jRadioButtonShadRainbow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButton_actionPerformed(e);
            }
        });
        jRadioButtonISIShading.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButton_actionPerformed(e);
            }
        });



        jTextFieldSpikeThreshold.setText("-30");
        jTextFieldSpikeThreshold.setColumns(4);
            this.jTextFieldSpikeThreshold.setEnabled(false);
        jLabelMv.setText("mV");
        jPanelScale.setLayout(borderLayout1);
        Dimension vd = new Dimension(280, 30);
        jPanelVars.setMaximumSize(vd);
        jPanelVars.setMinimumSize(vd);
        jPanelVars.setPreferredSize(vd);
        
        flowLayout2.setHgap(2);
        flowLayout2.setVgap(2);

        jCheckBoxActivityMonitors.setText("Activity Monitors");
        jCheckBoxActivityMonitors.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jCheckBoxActivityMonitors_actionPerformed(e);
            }
        });
        jLabelFramesToSkip.setText("Frames to skip:");
        jTextFieldFramesToSkip.setText("");
        jTextFieldFramesToSkip.setColumns(3);
        jPanelNumbers.setMinimumSize(new Dimension(260, 32));
        jPanelNumbers.setPreferredSize(new Dimension(260, 32));
        jPanelRun.add(jRunSlider, null);
        jPanelRun.add(jTextFieldSimulationTime, null);
        jPanelRun.add(jButtonStart, null);
        jPanelRun.add(jButtonStop, null);
        
        
        jPanelNumbers.add(jLabelTimeStep, null);
        jPanelNumbers.add(jTextFieldDelay, null);
        
        
        
        
        


        //jPanelOptions.add(jPanel2, null);
        //jPanelOptions.add(jPanelColouring, null);
        jPanelOptions.add(jPanelSpiking2Options, null);
        jPanelOptions.add(jPanelSpikingInfo, null);


        jPanelSpiking2Options.add(this.jRadioButtonShadLinear, null);
        jPanelSpiking2Options.add(this.jRadioButtonShadRainbow, null);
        jPanelSpiking2Options.add(this.jRadioButtonISIShading, null);
        jPanelSpikingInfo.add(jRadioButtonSpikesOnly, null);

        //jPanelSpikingInfo.add(jLabelSpikingThresh, null);
        jPanelSpikingInfo.add(jTextFieldSpikeThreshold, null);
        jPanelSpikingInfo.add(jLabelMv, null);




        jPanelOptions.add(jCheckBoxActivityMonitors, null);
        jPanelOptions.add(this.jButton2D, null);


        jPanelNumbers.add(jLabelFramesToSkip, null);
        jPanelNumbers.add(jTextFieldFramesToSkip, null);

        jPanelScale.setBorder(border2);
        
        
        
        //jPanelShowMore.setBackground(Color.red);
        jPanelShowMore.add(jButtonShowMore);
        Dimension d = new Dimension(24, 24);
        jButtonShowMore.setPreferredSize(d);
        jButtonShowMore.setMargin(new Insets(1, 1, 1, 1));
        jButtonShowMore.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                toggleShowMore();
            }
        });
        
        
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        
        
        jPanelMain.setLayout(new BorderLayout(4, 4));
        
        jPanelMain.add(jPanelRun, BorderLayout.NORTH);
        jPanelMain.add(jPanelScale, BorderLayout.CENTER);
        jPanelMain.add(jPanelShowMore, BorderLayout.EAST);
        
        jPanelExtras.setLayout(new BorderLayout());
        jPanelMainOptions.setLayout(new BorderLayout());
        
        jPanelMainOptions.add(jPanelNumbers, BorderLayout.NORTH);
        jPanelMainOptions.add(jPanelVars, BorderLayout.CENTER);
        jPanelMainOptions.add(jPanelOptions, BorderLayout.SOUTH);
        
        jPanelExtras.add(jPanelMainOptions, BorderLayout.CENTER);
        
        
        ArrayList<String> cgs = project.generatedCellPositions.getNonEmptyCellGroups();
        jPanelListCellGroups.setLayout(new GridLayout( cgs.size(), 1));
        
        for(String cellGroup: cgs)
        {
            JPanel jPanelNew = new JPanel();
            JCheckBox cb = new JCheckBox(cellGroup);
            cb.setSelected(true);
            jPanelNew.add(cb);
            CellChooser cc = new AllCells();
            JButton choose = new JButton(cc.toShortString());
            choose.setToolTipText(cellGroup); // for a clue to which cell group...
            
            //JTextField choice = new JTextField(cc.toNiceString());
            //choice.setColumns(12);
            //choice.setEnabled(false);
            
            //jPanelNew.add(choice);
            jPanelNew.add(choose);
            
            cb.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    updateCellForRerun();
                }
            });
            
            choose.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    updateCellChoice(e);
                }
            });
                    
            cellGroupsCheckBoxes.add(cb);
            cellGroupsToUse.put(cellGroup, cc);
            jPanelListCellGroups.add(jPanelNew);
        }
        jPanelCellsToUpdate.setLayout(new BorderLayout());
        jPanelCellsToUpdate.add(jPanelListCellGroups, BorderLayout.CENTER);
        jPanelCellsToUpdate.add(jPanelOtherCells, BorderLayout.SOUTH);
        
        jRadioButtonBlacken.setText("Black");
        jRadioButtonTransp.setText("Transparent");
        bgOtherCells.add(jRadioButtonBlacken);
        bgOtherCells.add(jRadioButtonTransp);
        jRadioButtonTransp.setSelected(true);
        jPanelOtherCells.add(new JLabel("Other cells:"));
        jPanelOtherCells.add(jRadioButtonBlacken);
        jPanelOtherCells.add(jRadioButtonTransp);
            
        jPanelCellsToUpdate.setBorder(BorderFactory.createEtchedBorder());
        jPanelExtras.add(jPanelCellsToUpdate, BorderLayout.SOUTH);

        
        if (moreShown)
        {
            jPanelMain.add(jPanelExtras, BorderLayout.SOUTH);
        }
        
        
        
        
    }
    
    private void updateCellChoice(ActionEvent e)
    {
        String cellGroup = ((JButton)e.getSource()).getToolTipText();
        logger.logComment("Cell choice change for: "+ cellGroup);
        
        for(String cg: cellGroupsToUse.keySet())
        {
            if (cg.equals(cellGroup))
            {
                CellChooser cc = cellGroupsToUse.get(cg);
                CellChooserDialog ccd = new CellChooserDialog(this, "Choose which cells in "+cg+" to replay", cc);
                GuiUtils.centreWindow(ccd);
                ccd.setVisible(true);
                
                CellChooser chosen = ccd.getFinalCellChooser();

                logger.logComment("Choosen one: " + chosen);
                
                ((JButton)e.getSource()).setText(chosen.toShortString());
                cellGroupsToUse.put(cg, chosen);
            }
        }
        
        
        
    }
    
    private void updateCellForRerun()
    {
        if (!jCheckBoxLoop.isSelected())
        {
            if (jRadioButtonBlacken.isSelected())
                blackenAllCells();
            else if (jRadioButtonTransp.isSelected())
                transparentAllCells();
        }
        
        refreshRerunVals((String)jComboBoxVars.getSelectedItem());
        
        refreshVoltagesCurrTimeStep();
    }
    

    /** @todo Put these in glossary? */
    private void addToolTips()
    {
        String delay = "An extra delay to add between each shown frame/timestep."
            +" A non zero value (e.g. 50ms) will slow replayed simulations down";
        String skip = "The number of frames/timesteps to skip before redisplaying the voltages etc."
            +" A value of ~10 results in a reasonable speedup of the replayed simulation.";

        this.jTextFieldDelay.setToolTipText(delay);
        this.jLabelTimeStep.setToolTipText(delay);
        this.jLabelFramesToSkip.setToolTipText(skip);
        this.jTextFieldFramesToSkip.setToolTipText(skip);
    }
    
    private void toggleShowMore()
    {
        logger.logComment("Toggling extra buttons...");
        moreShown = !moreShown;
        if (moreShown) {
            jButtonShowMore.setText("<<");
            jPanelMain.add(jPanelExtras, BorderLayout.SOUTH);
        } else {
            jButtonShowMore.setText(">>");
            jPanelMain.remove(jPanelExtras);
        }
        pack();
        
    }


    void addSpikingVoltage()
    {
        jPanelScale.removeAll();
        FlowLayout flowLayout3 = new FlowLayout();

        jPanelScale.setLayout(flowLayout3);

        flowLayout3.setHgap(0);
        flowLayout3.setVgap(0);

        //float spikingThreshold = Float.parseFloat(jTextFieldSpikeThreshold.getText());

        jLabelHigh.setText("Spiking cells");
        jLabelHigh.setHorizontalAlignment(JLabel.CENTER);
        jLabelLow.setText("Non spiking");
        jLabelLow.setHorizontalAlignment(JLabel.CENTER);

        jPanelScale.add(jLabelLow, null);

        jPanelScale.add(jLabelV1, null);
        jPanelScale.add(jLabelV2, null);
        jPanelScale.add(jLabelV3, null);
        jPanelScale.add(jLabelV4, null);
        jPanelScale.add(jLabelV5, null);
        jPanelScale.add(jLabelV6, null);
        jPanelScale.add(jLabelV7, null);
        jPanelScale.add(jLabelV8, null);
        jPanelScale.add(jLabelV9, null);
        jPanelScale.add(jLabelV10, null);
        jPanelScale.add(jLabelV11, null);
        jPanelScale.add(jLabelV12, null);
        jPanelScale.add(jLabelV13, null);
        jPanelScale.add(jLabelV14, null);
        jPanelScale.add(jLabelV14, null);
        jPanelScale.add(jLabelV15, null);


        flowLayout3.setHgap(0);
        flowLayout3.setVgap(0);

        jLabelV1.setOpaque(true);
        jLabelV2.setOpaque(true);
        jLabelV3.setOpaque(true);
        jLabelV4.setOpaque(true);
        jLabelV5.setOpaque(true);
        jLabelV6.setOpaque(true);
        jLabelV7.setOpaque(true);
        jLabelV8.setOpaque(true);
        jLabelV8.setOpaque(true);
        jLabelV9.setOpaque(true);
        jLabelV10.setOpaque(true);
        jLabelV11.setOpaque(true);
        jLabelV12.setOpaque(true);
        jLabelV13.setOpaque(true);
        jLabelV14.setOpaque(true);
        jLabelV15.setOpaque(true);
        jLabelV16.setOpaque(true);



        jLabelV1.setBackground(getColBasedOnValue( mostNegValue));
        jLabelV2.setBackground(getColBasedOnValue( mostNegValue));
        jLabelV3.setBackground(getColBasedOnValue(  mostNegValue));
        jLabelV4.setBackground(getColBasedOnValue(  mostNegValue));
        jLabelV5.setBackground(getColBasedOnValue(  mostNegValue));
        jLabelV6.setBackground(getColBasedOnValue(  mostNegValue));
        jLabelV7.setBackground(getColBasedOnValue(  mostNegValue));
        jLabelV8.setBackground(getColBasedOnValue( mostNegValue));
        jLabelV9.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV10.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV11.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV12.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV13.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV14.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV15.setBackground(getColBasedOnValue(  mostPosValue));
        jLabelV16.setBackground(getColBasedOnValue(  mostPosValue));


        jPanelScale.add(jLabelHigh, null);


        this.repaint();
    }

    void addGradedScale(int shadingOption)
    {
        jPanelScale.removeAll();

        FlowLayout flowLayout3 = new FlowLayout();

        jPanelScale.setLayout(flowLayout3);

        double low = 0;
        double high = 100;
        String units = null;

        if (shadingOption == VOLTAGE_LINEAR_SHADING ||
            shadingOption == VOLTAGE_RAINBOW_SHADING)
        {
            units = "mV";
            jLabelHigh.setText(" High: " + mostPosValue + units);
            jLabelLow.setText("Low: " + mostNegValue +units +" ");
            low = mostNegValue;
            high = mostPosValue;
        }
        else if (shadingOption == FREQ_SHADING)
        {
            units = "Hz";
            jLabelHigh.setText(" High: " + largestFreq + units);
            jLabelLow.setText("Low: " + smallestFreq + units+" ");
            low = smallestFreq;
            high = largestFreq;

        }
        jPanelScale.add(jLabelLow, null);

        jPanelScale.add(jLabelV1, null);
        jPanelScale.add(jLabelV2, null);
        jPanelScale.add(jLabelV3, null);
        jPanelScale.add(jLabelV4, null);
        jPanelScale.add(jLabelV5, null);
        jPanelScale.add(jLabelV6, null);
        jPanelScale.add(jLabelV7, null);
        jPanelScale.add(jLabelV8, null);
        jPanelScale.add(jLabelV9, null);
        jPanelScale.add(jLabelV10, null);
        jPanelScale.add(jLabelV11, null);
        jPanelScale.add(jLabelV12, null);
        jPanelScale.add(jLabelV13, null);
        jPanelScale.add(jLabelV14, null);
        jPanelScale.add(jLabelV14, null);
        jPanelScale.add(jLabelV15, null);


        flowLayout3.setHgap(0);
        flowLayout3.setVgap(0);

        jLabelV1.setOpaque(true);
        jLabelV2.setOpaque(true);
        jLabelV3.setOpaque(true);
        jLabelV4.setOpaque(true);
        jLabelV5.setOpaque(true);
        jLabelV6.setOpaque(true);
        jLabelV7.setOpaque(true);
        jLabelV8.setOpaque(true);
        jLabelV8.setOpaque(true);
        jLabelV9.setOpaque(true);
        jLabelV10.setOpaque(true);
        jLabelV11.setOpaque(true);
        jLabelV12.setOpaque(true);
        jLabelV13.setOpaque(true);
        jLabelV14.setOpaque(true);
        jLabelV15.setOpaque(true);
        jLabelV16.setOpaque(true);

        jLabelV1.setBackground(getShadingColour(shadingOption, (float) low));
        jLabelV2.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 1))));
        jLabelV3.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 2))));

        jLabelV4.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 3))));
        jLabelV5.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 4))));

        jLabelV6.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 5))));
        jLabelV7.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 6))));

        jLabelV8.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 7))));
        jLabelV9.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 8))));

        jLabelV10.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 9))));
        jLabelV11.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 10))));

        jLabelV12.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 11))));
        jLabelV13.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 12))));

        jLabelV14.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 13))));
        jLabelV15.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 14))));

        jLabelV16.setBackground(getShadingColour(shadingOption, (float) (low
            + ( ( (high - low) / 15) * 15))));

        jPanelScale.add(jLabelHigh, null);

        this.repaint();

    }

    private Color getShadingColour(int shadingOption, float value)
    {
        if (shadingOption == VOLTAGE_LINEAR_SHADING ||
            shadingOption == VOLTAGE_RAINBOW_SHADING)
        {
            return getColBasedOnValue(value);
            //return getRainbowColor(value);
        }
        else if (shadingOption == ISI_SHADING)
        {
            return getColorBasedOnISI(value);
        }
        else if (shadingOption == FREQ_SHADING)
        {
            return getColorBasedOnFreq(value);
        }
        else
         return Color.black;


    }

    void extraInit()
    {
        addToolTips();
        jTextFieldDelay.setText(GeneralProperties.getReplaySettings().getMillisBetweenShownFrames()+"");
        jTextFieldFramesToSkip.setText(GeneralProperties.getReplaySettings().getNumberofFramesToHide()+"");


        this.jRadioButtonISIShading.setToolTipText("Shades the cells with a colour based on a simple frequency generated from each\n"
                                                   +" successive interspike interval. Cells with fewer than 2 spikes are black");

        try
        {
            GeneralUtils.timeCheck("Loading sim data...");
            myCurrSimData = new SimulationData(mySimulationDir, true);
            myCurrSimData.initialise();

            GeneralUtils.timeCheck("Loaded sim data...");
            enableSimControls();
        }
        catch (SimulationDataException ex1)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem loading the data from simulation folder: " + mySimulationDir.getAbsolutePath(),
                                      ex1, this);
            disableSimControls();
            return;
        }

        ArrayList<String>  vars = this.myCurrSimData.getVariablesForAny();
        this.jComboBoxVars.removeAllItems();

        for (String  var: vars)
        {
            if (var.indexOf(SimPlot.SPIKE)>=0)
            {
                if (!vars.contains(SimPlot.VOLTAGE))
                    jComboBoxVars.addItem(SimPlot.VOLTAGE);
            }
            else
            {
                jComboBoxVars.addItem(var);
            }
        }
        if (vars.contains(SimPlot.VOLTAGE)) 
            jComboBoxVars.setSelectedItem(SimPlot.VOLTAGE);


    }



    public boolean isOnlySomaValues()
    {
        return this.myCurrSimData.isOnlySomaValues();
    }

    private void refreshRerunVals(String var)
    {
        double mostNeg = Double.MAX_VALUE;
        double mostPos = -1*Double.MAX_VALUE;
        try
        {
            this.rerunCellItemRefs = new String[currentCellItemRefs.size()];
            this.rerunValues = new double[currentCellItemRefs.size()][myCurrSimData.getNumberTimeSteps()];

            for (int i=0;i<currentCellItemRefs.size();i++)
            {
                String cellSegRef = currentCellItemRefs.get(i);
                DataStore ds = myCurrSimData.getDataAtAllTimes(cellSegRef, var, true);

                if (ds.getPostSynapticObject()==null)
                {
                    rerunCellItemRefs[i] = cellSegRef;
                    double[] vals = ds.getDataPoints();
                    rerunValues[i] = vals;
                    mostNeg = Math.min(mostNeg, ds.getMinVal());
                    mostPos = Math.max(mostPos, ds.getMaxVal());
                }
                else
                {
                    rerunCellItemRefs[i] = cellSegRef+"."+ds.getPostSynapticObject().getSynRef();
                    double[] vals = ds.getDataPoints();
                    rerunValues[i] = vals;
                    mostNeg = Math.min(mostNeg, ds.getMinVal());
                    mostPos = Math.max(mostPos, ds.getMaxVal());

                }

            }
            logger.logComment("rerunValues: "+ rerunValues[0][0]+" -> "+
                              rerunValues[0][myCurrSimData.getNumberTimeSteps()-1]+" -> "+
                              rerunValues[currentCellItemRefs.size()-1][0]+" -> "+
                              rerunValues[currentCellItemRefs.size()-1][myCurrSimData.getNumberTimeSteps()-1]);

            if (rerunCellItemRefs.length>2)
            {
                logger.logComment("rerunCellItemRefs: " + rerunCellItemRefs[0] + ", "
                                  + rerunCellItemRefs[1] +  "... , " + rerunCellItemRefs[rerunCellItemRefs.length-1]);
            }
            else if (rerunCellItemRefs.length>0)
            {
                logger.logComment("rerunCellItemRefs: " + rerunCellItemRefs[0] + "...");

            }
            else
            {
                logger.logComment("rerunCellItemRefs is empty");

            }




        }
        catch (SimulationDataException ex)
        {
            logger.logError("Data not loaded", ex);
        }
        mostNegValue = getBetterMin(mostNeg, mostPos);
        mostPosValue = getBetterMax(mostNeg, mostPos);


    }

    private static float getBetterMin(double min, double max)
    {
        double diff = max-min;

        if (diff/min>7)
        {
            return 0;
        }

        if (min==(int)min) return (float)min;

        if (diff >7 && diff<1000)
        {
            return (float)Math.floor(min);
        }
        double better = min - (diff/10f);
        //System.out.println("Better: " + better);
        int power = (int)Math.floor(Math.log10(better)) -2 ;
        //System.out.println("power: " + power);
        double factor = Math.pow(10, power);
        //System.out.println("factor: " + factor);
        better = (factor * (float)Math.floor(better/factor));

        if (better>min) return (float)min; // last check...
        return (float)better;
    }

    private static float getBetterMax(double min, double max)
    {

        double diff = max-min;

        if (max==(int)max) return (float)max;

        double distToNextInt = Math.ceil(max) - max;

        if (diff/distToNextInt>7) return (float)Math.ceil(max);

        if (diff > 7 && diff < 1000)
        {
            return (float) Math.ceil(max);
        }

        double better = max + (diff/10f);
        //System.out.println("Better: " + better);
        int power = (int)Math.floor(Math.log10(better)) -2 ;
        //System.out.println("power: " + power);
        double factor = Math.pow(10, power);
        //System.out.println("factor: " + factor);
        better = (factor * Math.ceil(better/factor));

        if (better<max) return (float)max; // last check...

        return (float)better;

    }




    void jComboBoxVars_itemStateChanged(ItemEvent e)
    {
        this.rerunCellItemRefs = null;
        this.rerunValues = null;
    }





    void jButtonStart_actionPerformed(ActionEvent e)
    {
        if(simulationTimer!=null && simulationTimer.isRunning())
        {
            logger.logComment("simulationTimer!=null && simulationTimer.isRunning()...");
            return;
        }
        Hashtable<String, ArrayList<Integer>> cellGroupsEnabled = new Hashtable<String, ArrayList<Integer>>();
        
        startSim = System.currentTimeMillis();
        
        for(JCheckBox cb: cellGroupsCheckBoxes)
        {
            if (cb.isSelected())
            {
                String cg = cb.getText();
                CellChooser cc = cellGroupsToUse.get(cg);
                try
                {
                    ArrayList<Integer> cellList = null;
                    if (!cc.isInitialised())
                    {
                        cc.initialise(project.generatedCellPositions.getAllPositionRecords());
                        
                        if (cc instanceof RegionAssociatedCells)
                        {
                            RegionAssociatedCells rac = (RegionAssociatedCells) cc;

                            rac.setProject(project); // to give info on regions...
                        }
                        cellList = cc.getOrderedCellList();
                    }
                    else
                    {
                        cellList = cc.getCachedCellList();
                    }
                    
                    cellGroupsEnabled.put(cg, cellList);
                }
                catch (Exception ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to generate a cell list fom cell chooser: "+ cc+" for cell group: "+ cg, ex, this);
                    return;
                }
            }
                
        }

        String selVariable = (String)jComboBoxVars.getSelectedItem();

        currentCellItemRefs  =  myCurrSimData.getCellItemRefsForVar(selVariable, true, cellGroupsEnabled);


        try
        {
            thresholdForRun = Float.parseFloat(jTextFieldSpikeThreshold.getText());
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a valid number in the field for threshold voltage.", ex, this);
            return;
        }

        if (jButtonStart.getText().equals(restartSimString))
        {
            jRunSlider.setValue(0);
            jButtonStart.setText(startSimString);
        }

        jRadioButtonSpikesOnly.setEnabled(false);
        jCheckBoxActivityMonitors.setEnabled(false);
        
        
        jButtonStop.setEnabled(true);
        jButtonStart.setEnabled(false);

        if (myCurrSimData == null)
        {
            logger.logComment("myCurrSimData == null");
            return;
        }

        if (rerunValues==null || rerunCellItemRefs == null)
        {
            String oldText = this.jButtonStart.getText();
            jButtonStart.setText("Loading...");

            refreshRerunVals(selVariable);
            jButtonStart.setText(oldText);
        }

        if (this.jRadioButtonISIShading.isSelected())
        {
            isiInfo = new Hashtable<String, ISIStateInfo>();

        }

        try
        {
            DataStore ds = this.myCurrSimData.getDataAtAllTimes(currentCellItemRefs.get(0), selVariable, true);


            /** @todo Put this update check in a separate function!! */

            if (this.jRadioButtonShadLinear.isSelected() || this.jRadioButtonShadRainbow.isSelected())
            {
                String lowText = "Low: " + mostNegValue + ds.getYUnit() + " ";
                this.jLabelLow.setText(lowText);
                this.jLabelLow.setToolTipText(lowText);
                String highText = " High: " + mostPosValue + ds.getYUnit();
                this.jLabelHigh.setText(highText);
                this.jLabelHigh.setToolTipText(highText);

                this.jLabelMv.setText(ds.getYUnit());
            }
            else if (this.jRadioButtonISIShading.isSelected())
            {
                String units = "Hz";
                jLabelHigh.setText(" High: " + largestFreq + units);
                jLabelLow.setText("Low: " + smallestFreq + units + " ");


            }

        }
        catch(SimulationDataException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error showing the data", ex, this);
            return;

        }


        final int numFramesToSkip = getNumFramesToHide();


        simulationTimer = new javax.swing.Timer(getDelay(), new ActionListener()
        {
            int currentTimeStep = jRunSlider.getValue();
            int count = 0;

            //String selectedValue = (String)jComboBoxVars.getSelectedItem();

            public void actionPerformed(ActionEvent evt)
            {
                if (currentTimeStep >= numTimeSteps)
                {
                    simulationTimer.stop();
                    //GeneralUtils.timeCheck("Stopped simulation");
                    jButtonStart.setText(restartSimString);

                    jButtonStop.setEnabled(false);
                    jButtonStart.setEnabled(true);

                    jRadioButtonSpikesOnly.setEnabled(true);
                    jCheckBoxActivityMonitors.setEnabled(true);
                    //return;

                    currentTimeStep = numTimeSteps-1; // to display final value...
                    
                    logger.logComment("Wall time since start pressed: "+ (System.currentTimeMillis()-startSim)/1000f);
                    
                    if (jCheckBoxLoop.isSelected())
                    {
                        logger.logComment("Looping back to start...");
                        jButtonStart_actionPerformed(null);
                    }
                }

                //System.out.println("At time: " + currentTimeStep);

                int toStepForward = -1;
                try
                {
                    if (jRadioButtonISIShading.isSelected())
                    {

                        if (count<numFramesToSkip)
                        {
                            count++;
                            return;
                        }
                        else
                        {
                            count = 0;
                        }

                        updateISIs(currentTimeStep);

                        toStepForward = 1;

                    }
                    else
                    {
                        toStepForward = numFramesToSkip +1;

                    }

                    jRunSlider.setValue(currentTimeStep);
                    GeneralUtils.timeCheck("currentTimeStep: " + currentTimeStep);
                    //showValuesAtTimeStep(currentTimeStep, currentCellSegRefs, selectedValue);
                    showValuesAtTimeStep(currentTimeStep);
                    GeneralUtils.timeCheck("shown volts, numFramesToSkip: " + numFramesToSkip);

                }
                catch (SimulationDataException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Problem running simulation", ex, null);
                    simulationTimer.stop();
                }
                currentTimeStep+=toStepForward;
            }
        });
        //logger.logComment("Starting");
        //GeneralUtils.timeCheck("Starting simulation");


        this.updateCellForRerun();

        simulationTimer.start();
    }

    private int getDelay()
    {
        /** @todo Do this properly... */
        return Integer.parseInt(GeneralUtils.replaceAllTokens(jTextFieldDelay.getText(), ",",""));
    }


    private int getNumFramesToHide()
    {
        return Integer.parseInt(GeneralUtils.replaceAllTokens(jTextFieldFramesToSkip.getText(), ",",""));
    }


    // needed when the frame is refreshed...
    public void refreshVoltagesCurrTimeStep()
    {
        logger.logComment("Refreshing the voltages...");
        if (simulationTimer==null)
        {
            logger.logComment("No sim timer...");
            return;
        }
        int currentTimeStep = 0;
        try
        {
            currentTimeStep = jRunSlider.getValue();

            //ArrayList<String> cellSegReferences =  myCurrSimData.getCellSegRefs(true);
            //showValuesAtTimeStep(currentTimeStep, currentCellSegRefs,(String)jComboBoxVars.getSelectedItem());
            showValuesAtTimeStep(currentTimeStep);
        }
        catch (SimulationDataException ex)
        {
            logger.logError("Problem refreshing for time step: "+ currentTimeStep);
        }
    }



    public String getCellGroup(String cellRefOrCellSegRef)
    {
        return myCurrSimData.getCellGroup(cellRefOrCellSegRef);
    }


    private void updateISIs(int timeStep) throws SimulationDataException
    {
        for (int cellSegIndex = 0;cellSegIndex<this.rerunCellItemRefs.length;cellSegIndex++)
        {
            String ref = rerunCellItemRefs[cellSegIndex];

            String cellOnlyReference = SimulationData.getCellOnlyReference(ref);
            String cellGroupName = myCurrSimData.getCellGroup(cellOnlyReference);
            int cellNumber = SimulationData.getCellNum(cellOnlyReference);

            double value = this.rerunValues[cellSegIndex][ timeStep];

            //logger.logComment("Showing " + voltage + " mV for " + cellSegReferences[j] + " at time: " +
            //                  myCurrSimData.getSimulationTime(timeStep));

            if (isiInfo.get(ref) == null)
            {
                ISIStateInfo isiState = new ISIStateInfo();
                isiInfo.put(ref, isiState);
            }

            ISIStateInfo isiState = this.isiInfo.get(ref);

            //logger.logComment("Before: " + isiState);

            if (!isiState.spiking && value > thresholdForRun) // New spike!
            {
                isiState.spiking = true;

                if (isiState.timeLastSpike < 0)
                {
                    logger.logComment("First spike...");
                    isiState.timeLastSpike = (float)myCurrSimData.getSimulationTime(timeStep);
                    isiState.runningISIAverage = 0;
                    isiState.numberISIs = 0;
                }
                else
                {
                    logger.logComment(cellGroupName+", "+cellNumber+": spiking: There have been " + isiState.numberISIs + " spikes already...");

                    float newestISI = (float)myCurrSimData.getSimulationTime(timeStep) - isiState.timeLastSpike;

                    isiState.runningISIAverage
                        = ( (isiState.runningISIAverage * isiState.numberISIs) + newestISI) /
                        (isiState.numberISIs + 1);

                    isiState.numberISIs++;

                    isiState.timeLastSpike = (float)myCurrSimData.getSimulationTime(timeStep);

                }

            }
            if (isiState.spiking && value < thresholdForRun)
            {
                isiState.spiking = false;
            }

            //logger.logComment("After : " + isiState);

        }
    }

    private void blackenAllCells()
    {
        ArrayList<String> cellSegRefs =  myCurrSimData.getCellSegRefs(false);
        for (String ref: cellSegRefs)
        {
            controlledPanel.setColour(ref, Color.black);
        }
    }
    
    private void transparentAllCells()
    {
        ArrayList<String> cellSegRefs =  myCurrSimData.getCellSegRefs(false);
        for (String ref: cellSegRefs)
        {
            controlledPanel.setTransparent(ref);
        }
    }

           


    /**
     * Updates everything which needs updating at the particular time step
     * @param timeStep The time step
     */
    private void showValuesAtTimeStep(int timeStep) throws SimulationDataException
    {
        jTextFieldSimulationTime.setText((float)myCurrSimData.getSimulationTime(timeStep)+"");

        /** @todo Improve collection handling here... */

        Vector<String> allActMonCellNames  = new Vector<String>();
        allActMonCellNames.addAll(activityMonitors.keySet());

        int[] countActive = new int[activityMonitors.keySet().size()];

        for (int cellSegIndex = 0;cellSegIndex<this.rerunCellItemRefs.length;cellSegIndex++)
        {
            String ref = rerunCellItemRefs[cellSegIndex];

            String cellOnlyReference = SimulationData.getCellOnlyReference(ref);

            String cellGroupName = myCurrSimData.getCellGroup(cellOnlyReference);

            int cellNumber = SimulationData.getCellNum(cellOnlyReference);

            //float value = myCurrSimData.getValueAtTimeStep(timeStep, ref, var);
            double value = this.rerunValues[cellSegIndex][ timeStep];

            logger.logComment("Showing val "+value+" for "+ref+" at time: "+
                myCurrSimData.getSimulationTime( timeStep));

            Color newColour = null;


            if (this.jRadioButtonSpikesOnly.isSelected())
            {
                if (value<thresholdForRun)
                    newColour = getColBasedOnValue(mostNegValue);
                else
                    newColour = getColBasedOnValue(mostPosValue);

            }
            else if (this.jRadioButtonISIShading.isSelected())
            {
                if (isiInfo.get(ref)==null)
                {
                    ISIStateInfo isiState = new ISIStateInfo();
                    isiInfo.put(ref, isiState);
                }

                ISIStateInfo isiState = this.isiInfo.get(ref);

                logger.logComment("Current isi info for "+cellGroupName+", "+cellNumber+": "+ isiState);

                if (isiState.runningISIAverage<=0 || isiState.numberISIs<=0)
                {
                    newColour  = Color.black;
                }
                else
                {
                    //newColour = this.getColorBasedOnISI(isiState.runningISIAverage);
                    newColour = getColorBasedOnFreq(1000/isiState.runningISIAverage);
                }
            }
            else if (jRadioButtonShadLinear.isSelected()|| jRadioButtonShadRainbow.isSelected())
            {
                newColour = getColBasedOnValue((float)value);
            }

            logger.logComment("Decided on a colour: "+ newColour.toString());

            controlledPanel.setColour(ref, newColour);

            if(jCheckBoxActivityMonitors.isSelected() && value > thresholdForRun)
            {
                int cellGroupIndex = allActMonCellNames.indexOf(cellGroupName);
                countActive[cellGroupIndex]++;
            }

            for (int i = 0; i < view2Ds.size(); i++)
            {
                //System.out.println("Checking if to update view "+view2Ds.get(i).getCellGroup()+
               //     ", voltage is from cellGroup: "+ cellGroupName);
                if (view2Ds.get(i).getCellGroup().equals(cellGroupName))
                {
                    view2Ds.get(i).updateVoltage((float)value,
                                                 cellGroupName,
                                                 cellNumber,
                                                 (cellNumber == 0)); // i.e. Refresh only when the first cell
                                                                     // in that cell group is being referred to. Ensures
                                                                     // that once per time step the group view is updated...
                }
            }
            //ArrayList<> velList = listeners.get(cellGroupName);


        }

        if (jCheckBoxActivityMonitors.isSelected())
        {

            for (int i = 0; i < allActMonCellNames.size(); i++)
            {
                 String cellGroupName = allActMonCellNames.elementAt(i);
                 ActivityMonitor am = activityMonitors.get(cellGroupName);
                 if (am!=null) am.setValue(countActive[i]);
            }
           //
            //am.setv
        }

    }


    public static Color getRainbowCol(float value)
    {
       float fractionAlong = (value - mostNegValue) / (mostPosValue - mostNegValue);
       
       return GeneralUtils.getRainbowColour(1-fractionAlong);
   }


    public Color getColBasedOnValue(float value)
    {
        if (jRadioButtonShadRainbow.isSelected())
            return getRainbowCol(value);
        else
            return getLinearColor(value);
    }

    public static Color getLinearColor(float value)
    {
        Color mostNegVoltageColour = new Color(105, 0, 105); // dark purple
        Color mostPosVoltageColour = new Color(255, 255, 0); // yellowy

        double fractionAlong = (value - mostNegValue) / (mostPosValue - mostNegValue);

       Color thisColour = GeneralUtils.getFractionalColour(mostNegVoltageColour,
                                                           mostPosVoltageColour,
                                                           fractionAlong);

       return thisColour;
   }


   public static Color getColorBasedOnISI(float isi)
   {
       double smallestISI = (1/largestFreq)*1000;
       double largestISI = (1/smallestFreq)*1000;

       double fractionAlong = (isi - smallestISI) / (largestISI - smallestISI);

      Color newCol = GeneralUtils.getRainbowColour(fractionAlong);


      return newCol;
  }

  public static Color getColorBasedOnFreq(float freq)
  {
      double smallestISI = smallestFreq;
      double largestISI = largestFreq;

      double fractionAlong = (freq - smallestISI) / (largestISI - smallestISI);


     Color newCol = GeneralUtils.getRainbowColour(1-fractionAlong); // to ensure red = high freq


     return newCol;
  }



    private void enableSimControls()
    {
        jButtonStart.setEnabled(true);
        jButtonStop.setEnabled(false);
        jRunSlider.setEnabled(true);
        jTextFieldSimulationTime.setEnabled(true);

        jRunSlider.setMinimum(0);
        try
        {
            jRunSlider.setMaximum(myCurrSimData.getNumberTimeSteps()-1);
            numTimeSteps = myCurrSimData.getNumberTimeSteps();

             controlledPanel.validSimulationLoaded();
        }
        catch (SimulationDataException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem reading from data", ex, this);
            controlledPanel.noSimulationLoaded();
        }


    }

    private void disableSimControls()
    {
        jButtonStart.setEnabled(false);
        jButtonStop.setEnabled(false);
        jRunSlider.setEnabled(false);
        jTextFieldSimulationTime.setEnabled(false);

        //controlledPanel.noSimulationLoaded();
    }


    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            dispose();
            if (standalone)
                System.exit(0);
        }
    }




    public static  void main(String[] args)
    {

        for (float v=mostNegValue; v<=mostPosValue; v=v+10)
        {
            Color c = getRainbowCol(v);
            System.out.println("Color for "+v+": "+ c);
        }

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

        File simFile = new File(pf.getParentFile(),"simulations/Sim_1" );
        Main3DPanel m = new Main3DPanel(p, simFile, null);

        SimulationRerunFrame simRerun = new SimulationRerunFrame(p, simFile, m);
        simRerun.standalone = true;
        simRerun.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = simRerun.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        simRerun.setLocation( (screenSize.width - frameSize.width) / 2,
                             (screenSize.height - frameSize.height) / 2);

        simRerun.setVisible(true);


    }

    void jSlider1_mouseReleased(MouseEvent e)
    {
        jButtonStop_actionPerformed(null);

        if (simulationTimer!=null) simulationTimer.stop();
        int timeStep = jRunSlider.getValue();
        logger.logComment("Mouse released on slider. Value is: "+ timeStep);

        if (jRunSlider.getValue() == jRunSlider.getMaximum())
        {
            jButtonStart.setText(restartSimString);
        }

        try
        {
            //ArrayList<String> cellSegReferences =  myCurrSimData.getCellSegRefs(true);
            //showValuesAtTimeStep(timeStep, currentCellSegRefs, (String)jComboBoxVars.getSelectedItem());
            showValuesAtTimeStep(timeStep);
        }
        catch (SimulationDataException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem running simulation", ex, this);
            simulationTimer.stop();
        }
    }


    public double[] getVoltageAtAllTimes(String cellSegRef)  throws SimulationDataException
    {
        return myCurrSimData.getVoltageAtAllTimes(cellSegRef);
    }

    public double[] getAllTimes()  throws SimulationDataException
    {
        return myCurrSimData.getAllTimes();
    }


   // public boolean dataOnlyForSoma()
   // {
   //    return false;// return myCurrSimData.dataOnlyForSoma();
  //  }



    void jButtonStop_actionPerformed(ActionEvent e)
    {
        logger.logComment("Stopping data perusal");
        if (simulationTimer!=null) simulationTimer.stop();

        jRadioButtonSpikesOnly.setEnabled(true);
        jCheckBoxActivityMonitors.setEnabled(true);
        
        jButtonStop.setEnabled(false);
        jButtonStart.setEnabled(true);
    }



    @Override
    public void dispose()
    {
        if (simulationTimer!=null) simulationTimer.stop();

        super.dispose();

        GeneralProperties.getReplaySettings().setMillisBetweenShownFrames(getDelay());
        GeneralProperties.getReplaySettings().setNumberofFramesToHide(getNumFramesToHide());

        logger.logComment("Delay: "+ getDelay());
        logger.logComment("Delay stored: "+ GeneralProperties.getReplaySettings().getMillisBetweenShownFrames());

        removeActivityMonitors();
        remove2DViews();

        activityMonitors = null;
        activityMonitors = new Hashtable<String, ActivityMonitor>();
        isiInfo = null;

        GeneralProperties.saveToSettingsFile();
    }


    public ArrayList<String> getCellSegRefsForCellRef(String cellRef)
    {
        return this.myCurrSimData.getCellSegRefsForCellRef(cellRef);
    }

    public ArrayList<DataStore> getDataForCellSegRef(String cellSegRef, boolean inclSynapses)
    {
        return this.myCurrSimData.getDataForCellSegRef(cellSegRef, inclSynapses);
    }

    public DataStore getDataAtAllTimes(String cellSegRef, String variable, boolean incSpikeOrVoltage)  throws SimulationDataException
    {
        return this.myCurrSimData.getDataAtAllTimes(cellSegRef, variable, incSpikeOrVoltage);
    }


    void jButton2D_actionPerformed(ActionEvent e)
    {
        logger.logComment("Adding 2d view...");

        ArrayList<String> everyCellGroup = project.cellGroupsInfo.getAllCellGroupNames();

        Vector<String> allNonEmptyCellGroups = new Vector<String>();

        for (int i = 0; i < everyCellGroup.size(); i++)
        {
            int numInCellGroup = project.generatedCellPositions.getNumberInCellGroup(everyCellGroup.get(i));
            if (numInCellGroup>0) allNonEmptyCellGroups.add(everyCellGroup.get(i));
        }



        String[] allCellGroups = new String[allNonEmptyCellGroups.size()];
        allNonEmptyCellGroups.copyInto(allCellGroups);

        String message = "Please select the Cell Group to display in 2D";

        String cellGroup = (String) JOptionPane.showInputDialog(this,
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

        ArrayList<PositionRecord> positions = project.generatedCellPositions.getPositionRecords(cellGroup);

        ViewVoltage2D view2d = new ViewVoltage2D(cellGroup, positions, ViewCanvas.Z_X_NEGY_DIR, false);
        view2d.pack();
        view2d.validate();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = view2d.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        view2d.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        view2d.setVisible(true);

        this.view2Ds.add(view2d);

    }


    public String getSimReference()
    {
        return myCurrSimData.getSimulationName();
    }

    public File getSimulationDirectory()
    {
        return myCurrSimData.getSimulationDirectory();
    }

    public SimulationData getSimulationData()
    {
        return this.myCurrSimData;
    }


    public String getDateModified()
    {
        return myCurrSimData.getDateModified();
    }



    void jRadioButton_actionPerformed(ActionEvent e)
    {
        if (jRadioButtonSpikesOnly.isSelected())
        {
            this.jTextFieldSpikeThreshold.setEnabled(true);
            this.addSpikingVoltage();
        }
        else if (jRadioButtonShadLinear.isSelected())
        {
            this.jTextFieldSpikeThreshold.setEnabled(false);
            this.addGradedScale(VOLTAGE_LINEAR_SHADING);
        }
        else if (jRadioButtonShadRainbow.isSelected())
        {
            this.jTextFieldSpikeThreshold.setEnabled(false);
            this.addGradedScale(VOLTAGE_RAINBOW_SHADING);
        }
        else if (jRadioButtonISIShading.isSelected())
        {
            this.jTextFieldSpikeThreshold.setEnabled(true);
            this.addGradedScale(FREQ_SHADING);
        }


    }

    void jCheckBoxActivityMonitors_actionPerformed(ActionEvent e)
    {

        if (jCheckBoxActivityMonitors.isSelected())
        {
            if (activityMonitors.size()>0) return;
            logger.logComment("Activity monitors being turned on...");
            //Enumeration enumeration = cellGroupInfo.keys();

            ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();

            for (int i = 0; i < cellGroupNames.size(); i++)
            {
                String nextCellGroup = cellGroupNames.get(i);
                int population = project.generatedCellPositions.getNumberInCellGroup(nextCellGroup);

                if (population>0)
                {
                    ActivityMonitor actMon = new ActivityMonitor(nextCellGroup, population);
                    activityMonitors.put(nextCellGroup, actMon);

                    actMon.pack();

                    Dimension frameSize = actMon.getSize();

                    actMon.setLocation(20 + ( (frameSize.width + 20) * i), 20);

                    actMon.setVisible(true);
                }

            }
        }
        else
        {
            removeActivityMonitors();
        }
    }

    private void removeActivityMonitors()
    {
        logger.logComment("Activity monitors being turned off...");
        Enumeration enumeration = activityMonitors.elements();

        while (enumeration.hasMoreElements())
        {
            ActivityMonitor item = (ActivityMonitor) enumeration.nextElement();
            item.dispose();

        }
        activityMonitors.clear();

    }

    private void remove2DViews()
    {
        logger.logComment("2DViews being turned off...");
        for (int i = 0; i < this.view2Ds.size(); i++)
        {
            view2Ds.get(i).dispose();
        }
        view2Ds.clear();

    }

    private class ISIStateInfo
    {
        boolean spiking = false;
        float timeLastSpike = -1;
        float runningISIAverage = 0;
        int numberISIs = 0;

        @Override
        public String toString()
        {
            return ("ISIStateInfo [spiking: "+spiking+", timeLastSpike: "+timeLastSpike
                +", runningISIAverage: "+runningISIAverage+", numberISIs: "+numberISIs+"]");
        }
    }

}
