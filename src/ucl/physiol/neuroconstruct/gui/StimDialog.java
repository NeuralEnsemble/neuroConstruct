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
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.GroupDistributedSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Dialog to specify electrophysiological stimulations to add to the network
 *
 * @author Padraig Gleeson
 *
 */


@SuppressWarnings("serial")

public class StimDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("StimDialog");
    
    CellChooser myCellChooser = new AllCells();
    
    IClampSettings tempIClamp = new IClampSettings("tempIClamp",
                                                   null,
                                                   null,
                                                   0,
                                                   20,
                                                   60,
                                                   0.2f,
                                                   false);

    RandomSpikeTrainSettings tempRandSpike = new RandomSpikeTrainSettings("tempRandSpike",
                                                                          null,
                                                                          null,
                                                                          0,
                                                                          new NumberGenerator(0.05f),
                                                                          10,
                                                                          null);

    RandomSpikeTrainExtSettings tempRandSpikeExt = new RandomSpikeTrainExtSettings("tempRandSpikeExt",
                                                                          null,
                                                                          null,
                                                                          0,
                                                                          new NumberGenerator(0.05f),
                                                                          null,
                                                                          new NumberGenerator(20),
                                                                          new NumberGenerator(60),
                                                                          false);
    
    IClampVariableSettings tempIClampVariable = new IClampVariableSettings("tempIClampVariable",
                                                            null, 
                                                            null, 
                                                            0, 
                                                            new NumberGenerator(20), 
                                                            new NumberGenerator(60), 
                                                            "0.2 * sin(2 * 3.14159265 * t/200)");



    RandomSpikeTrainVariableSettings tempRandSpikeVar = new RandomSpikeTrainVariableSettings("tempRandSpikeVar",
                                                                          null,
                                                                          null,
                                                                          0,
                                                                          "0.05 + 0.04 * cos(2 * 3.14159265 * t/400)",
                                                                          null,
                                                                          new NumberGenerator(20),
                                                                          new NumberGenerator(60));


    
    IndividualSegments indSegChooser = new IndividualSegments();
    
    GroupDistributedSegments gds = new GroupDistributedSegments("all", 4);
       
    SegmentLocationChooser chosenSegLocChooser = indSegChooser;
    
   Project project = null;

    boolean cancelled = false;
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelTop = new JPanel();

    Border border1;
    JPanel jPanelNames = new JPanel();
    JLabel jLabelInputReference = new JLabel();
    JTextField jTextFieldReference = new JTextField();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabelCellGroups = new JLabel();
    JComboBox jComboBoxCellGroup = new JComboBox();
    JLabel jLabelCellNumber = new JLabel();
    JLabel jLabelLocation = new JLabel();
    JLabel jLabelType = new JLabel();
    JTextField jTextFieldCellNumber = new JTextField();
//    JLabel jLabelSegment = new JLabel();
    JTextField jTextFieldSegmentId = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    //JLabel jLabelCellNum2 = new JLabel();
    JButton jButtonSegment = new JButton();

    JRadioButton jRadioButtonSingle = new JRadioButton();
    JRadioButton jRadioButtonDistributed = new JRadioButton();

    JRadioButton jRadioButtonIClamp = new JRadioButton();
    JRadioButton jRadioButtonRandSpike = new JRadioButton();
    JRadioButton jRadioButtonRandSpikeExt = new JRadioButton();
    JRadioButton jRadioButtonIClampVariable = new JRadioButton();
    JRadioButton jRadioButtonRandSpikeVar = new JRadioButton();


    JButton jButtonLocationChange = new JButton();
    JButton jButtonStimChange = new JButton();
    JButton jButtonCellChooserChange = new JButton();

    ButtonGroup buttonGroupSegments = new ButtonGroup();
    ButtonGroup buttonGroupStims = new ButtonGroup();
    JTextField jTextFieldInfo = new JTextField();
    JTextField jTextFieldLocationInfo = new JTextField();
    JLabel jLabelFraction = new JLabel();
    JTextField jTextFieldFractionAlong = new JTextField();




    public StimDialog(Frame owner, String suggestedRef, Project project)
    {
        super(owner, "Choose a stimulation to apply to the network", false);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            this.project = project;

            Vector synapticTypes =  project.cellMechanismInfo.getAllChemSynMechNames();

            if (synapticTypes.size()>0)
            {
                tempRandSpike.setSynapseType((String)synapticTypes.elementAt(0));
                tempRandSpikeExt.setSynapseType((String)synapticTypes.elementAt(0));
                tempRandSpikeVar.setSynapseType((String)synapticTypes.elementAt(0));
            }
            else
            {
                File cmlTemplateDir = ProjectStructure.getCMLTemplatesDir();

                File fromDir = new File(cmlTemplateDir, "DoubleExpSyn");
                Properties props = new Properties();

                try
                {
                    props.load(new FileInputStream(new File(fromDir, "properties")));
                }
                catch (IOException ex)
                {
                    logger.logError("Problem getting properties", ex);
                    //ignore...
                }

                String name = "SynForRndSpike";
                String desc = "Default Synaptic mechanism added automatically when a new Input was created (needed for Random stim option in input)";


                File dirForCMLFiles = ProjectStructure.getDirForCellMechFiles(project, name);

                dirForCMLFiles.mkdir();

                ChannelMLCellMechanism cmlMech = new ChannelMLCellMechanism();

                cmlMech.setInstanceName(name);
                cmlMech.setDescription(desc);

                logger.logComment("Info so far: "+cmlMech.toString());

                cmlMech.setMechanismType(props.getProperty("CellProcessType"));

                cmlMech.setMechanismModel("Template based ChannelML file");

                if (props.getProperty("ChannelMLFile")!=null)
                {
                    String relativeFile = props.getProperty("ChannelMLFile");

                    File absFile = new File(fromDir, relativeFile);
                    absFile = absFile.getCanonicalFile();

                    logger.logComment("ChannelML file found in props to be: "+ absFile);

                    File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);
                    cmlMech.setXMLFile(newFile.getName());
                }
                if (props.getProperty("MappingNEURON")!=null)
                {
                    String relativeFile = props.getProperty("MappingNEURON");

                    File absFile = new File(fromDir, relativeFile);
                    absFile = absFile.getCanonicalFile();

                    logger.logComment("MappingNEURON file found in props to be: "+ absFile);

                    File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                    SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                              SimEnvHelper.NEURON, true); // can be reset later

                    cmlMech.addSimMapping(mapping);

                }

                if (props.getProperty("MappingGENESIS")!=null)
                {
                    String relativeFile = props.getProperty("MappingGENESIS");

                    File absFile = new File(fromDir, relativeFile);
                    absFile = absFile.getCanonicalFile();

                    logger.logComment("MappingGENESIS file found in props to be: "+ absFile);

                    File newFile = GeneralUtils.copyFileIntoDir(absFile, dirForCMLFiles);

                    SimulatorMapping mapping = new SimulatorMapping(newFile.getName(),
                                                              SimEnvHelper.GENESIS, false);

                    cmlMech.addSimMapping(mapping);

                }
                logger.logComment("Info so far: "+cmlMech.toString());


                if (props.getProperty("NEURONNeedsCompilation") != null)
                {
                    Boolean b = Boolean.parseBoolean(props.getProperty("NEURONNeedsCompilation"));
                    cmlMech.getSimMapping(SimEnvHelper.NEURON).setRequiresCompilation(b.booleanValue());
                }

                project.cellMechanismInfo.addCellMechanism(cmlMech);

                cmlMech.initialise(project, true);

                logger.logComment("Info so far: "+cmlMech);

                tempRandSpike.setSynapseType(cmlMech.getInstanceName());
                tempRandSpikeExt.setSynapseType(cmlMech.getInstanceName());
                tempRandSpikeVar.setSynapseType(cmlMech.getInstanceName());
            }
            jbInit();
            extraInit();
            jTextFieldReference.setText(suggestedRef);
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }


    private void extraInit()
    {
        logger.logComment("-----------------------------New StimDialog created");
        
        ArrayList<Integer> someSegIds = new ArrayList<Integer>();
        someSegIds.add(0);
        indSegChooser.setListOfSegmentIds(someSegIds);
        

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        for (int i = 0; i < cellGroupNames.size(); i++)
        {
                jComboBoxCellGroup.addItem(cellGroupNames.get(i));
        }

        jRadioButtonIClamp.setSelected(true);

        jTextFieldInfo.setText(tempIClamp.toString());
        jTextFieldLocationInfo.setText(indSegChooser.toString());

        //String cellGroupToStim = (String)jComboBoxCellGroup.getSelectedItem();

        //String cellType = project.cellGroupsInfo.getCellType(cellGroupToStim);
        //Cell cellForSelectedGroup = project.cellManager.getCell(cellType);

        //Segment segToStim = cellForSelectedGroup.getSegmentWithId(chosenSegmentId);

        setSegChooserInfo(chosenSegLocChooser);

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

        // Check seg id is in cell...
        /////////checkSegId();


        logger.logComment("OK pressed...");

        this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }

    public StimulationSettings getFinalStim()
    {
        StimulationSettings  stim = null;

        if (jRadioButtonIClamp.isSelected())
        {
            stim = tempIClamp;
        }
        else if (jRadioButtonRandSpike.isSelected())
        {
            stim = tempRandSpike;
        }
        else if (jRadioButtonRandSpikeExt.isSelected())
        {
            stim = tempRandSpikeExt;
        }
        else if (jRadioButtonIClampVariable.isSelected())
        {
            stim = tempIClampVariable;
        }
        else if (jRadioButtonRandSpikeVar.isSelected())
        {
            stim = tempRandSpikeVar;
        }

        stim.setReference(jTextFieldReference.getText());
        stim.setCellGroup((String)jComboBoxCellGroup.getSelectedItem());

        stim.setCellChooser(this.myCellChooser);

        stim.setSegChooser(chosenSegLocChooser);
        //stim.setFractionAlong(Float.parseFloat(jTextFieldFractionAlong.getText()));

        return stim;
    }



    public StimDialog()
    {
        try
        {
            jbInit();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        jTextFieldCellNumber.setEditable(false);

        border1 = BorderFactory.createEmptyBorder(6,6,6,6);
        this.getContentPane().setLayout(borderLayout1);
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

        jPanelMain.setLayout(gridBagLayout1);
        jLabelInputReference.setText("Reference:");
        jTextFieldReference.setText("...");
        jTextFieldReference.setColumns(12);
        jPanelTop.setLayout(borderLayout4);
        jLabelCellGroups.setText("Cell Group:");
        jLabelCellNumber.setRequestFocusEnabled(true);
        jLabelCellNumber.setText("Cells to choose:");

        jTextFieldCellNumber.setText(myCellChooser.toString());

        jLabelLocation.setText("Segment location to choose:");
        jLabelType.setText("Type of input:");

        jRadioButtonSingle.setSelected(true);
        jRadioButtonSingle.setText("Single segment");
        
        jRadioButtonSingle.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonSingle_actionPerformed(e);
            }

            private void jRadioButtonSingle_actionPerformed(ActionEvent e)
            {
                chosenSegLocChooser = indSegChooser;
                
                jTextFieldLocationInfo.setText(chosenSegLocChooser.toNiceString());
            }
        });


        jRadioButtonDistributed.setText("Group distributed segments");
        jRadioButtonDistributed.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonDistributed_actionPerformed(e);
            }

            private void jRadioButtonDistributed_actionPerformed(ActionEvent e)
            { 
                chosenSegLocChooser = gds;
                
                jTextFieldLocationInfo.setText(chosenSegLocChooser.toNiceString());
            }
        });

//        jLabelSegment.setText("Segment Id:");
//        jTextFieldSegmentId.setEditable(false);
//        jTextFieldSegmentId.setText("...");
//        jTextFieldSegmentId.setColumns(10);


        jButtonSegment.setText("...");
        jButtonSegment.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSegment_actionPerformed(e);
            }
        });
        jRadioButtonIClamp.setSelected(true);
        jRadioButtonIClamp.setText("Current clamp");
        jRadioButtonIClamp.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonIClamp_actionPerformed(e);
            }
        });


        jRadioButtonRandSpike.setText("Random spike input");
        jRadioButtonRandSpike.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonRandSpike_actionPerformed(e);
            }
        });
        jRadioButtonRandSpikeExt.setText("Random spikes with delay & duration (note: NEURON only)");
        jRadioButtonRandSpikeExt.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonRandSpikeExt_actionPerformed(e);
            }
        });

        jRadioButtonIClampVariable.setText("Current Clamp with variable amp (note: NEURON only)");
        jRadioButtonIClampVariable.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonIClampVariable_actionPerformed(e);
            }
        });
        jRadioButtonRandSpikeVar.setText("Random spikes with variable rate (note: NEURON only)");
        jRadioButtonRandSpikeVar.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonRandSpikeVar_actionPerformed(e);
            }
        });



        jButtonLocationChange.setText("Change..");
        jButtonStimChange.setText("Change..");
        jButtonCellChooserChange.setText("Change...");
        jButtonLocationChange.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonLocationChange_actionPerformed(e);
            }
        });
        jButtonStimChange.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStimChange_actionPerformed(e);
            }
        });
        jButtonCellChooserChange.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCellChooserChange_actionPerformed(e);
            }
        });


        jTextFieldInfo.setEnabled(true);
        jTextFieldInfo.setEditable(false);
        jTextFieldInfo.setText("...");
        jTextFieldLocationInfo.setEnabled(true);
        jTextFieldLocationInfo.setEditable(false);
        jTextFieldLocationInfo.setText("...");
//        jLabelFraction.setText("Fraction along (only used for NEURON):");
//        jTextFieldFractionAlong.setText("0.5");
        this.getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        this.getContentPane().add(jPanelTop, BorderLayout.NORTH);
        jPanelTop.add(jPanelNames,  BorderLayout.NORTH);
        jPanelNames.add(jLabelInputReference, null);
        jPanelNames.add(jTextFieldReference, null);
        jPanelMain.add(jLabelCellGroups,          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jComboBoxCellGroup,          new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));

        jPanelMain.add(jLabelCellNumber,          new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 0, 14), 0, 0));

        jPanelMain.add(jTextFieldCellNumber,          new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 200, 0));

//        jPanelMain.add(jLabelSegment,            new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
//
//        jPanelMain.add(jTextFieldSegmentId,               new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 6), 0, 0));
//
        jPanelMain.add(this.jButtonCellChooserChange,          new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 14, 6, 0), 0, 0));

//        jPanelMain.add(jButtonSegment,          new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 6, 6, 14), 0, 0));


        jPanelMain.add(this.jButtonCellChooserChange,          new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 14, 6, 0), 0, 0));

        jPanelMain.add(jLabelLocation,          new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonSingle,
                       new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonDistributed,
                       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jLabelType,          new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonIClamp,
                       new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonRandSpike,
                       new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonRandSpikeExt,
                       new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonIClampVariable,
                       new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));


        jPanelMain.add(jRadioButtonRandSpikeVar,
                       new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));



        jPanelMain.add(jButtonLocationChange,    new GridBagConstraints(1, 5, 2, 3, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        
        jPanelMain.add(jTextFieldLocationInfo,   new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));

        jPanelMain.add(jButtonStimChange,    new GridBagConstraints(1, 8, 2, 3, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        jPanelMain.add(jTextFieldInfo,   new GridBagConstraints(0, 14, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));


        jPanelMain.add(jLabelFraction,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));


        buttonGroupSegments.add(jRadioButtonSingle);
        buttonGroupSegments.add(jRadioButtonDistributed);


        buttonGroupStims.add(jRadioButtonIClamp);
        buttonGroupStims.add(jRadioButtonIClampVariable);
        buttonGroupStims.add(jRadioButtonRandSpikeVar);
        buttonGroupStims.add(jRadioButtonRandSpike);
        buttonGroupStims.add(jRadioButtonRandSpikeExt);


//        jPanelMain.add(jTextFieldFractionAlong,   new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0
//            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));
    }






    public static void main(String[] args)
    {
        try
        {
            Project p = Project.loadProject(new File("examples/Ex5-Networks/Ex5-Networks.neuro.xml").getCanonicalFile(), null);

            String suggestedName = "Stim_0";

            StimDialog dlg = new StimDialog(new Frame(),
                                            suggestedName,
                                            p);

            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);

            dlg.setStim(p.elecInputInfo.getStim(0));



        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }

    void jRadioButtonIClamp_actionPerformed(ActionEvent e)
    {
        jTextFieldInfo.setText(tempIClamp.toString());
    }

    void jRadioButtonRandSpike_actionPerformed(ActionEvent e)
    {
        jTextFieldInfo.setText(tempRandSpike.toString());

    }

    void jRadioButtonRandSpikeExt_actionPerformed(ActionEvent e)
    {
        jTextFieldInfo.setText(tempRandSpikeExt.toString());

    }

    void jRadioButtonIClampVariable_actionPerformed(ActionEvent e)
    {
        jTextFieldInfo.setText(tempIClampVariable.toString());

    }



    void jRadioButtonRandSpikeVar_actionPerformed(ActionEvent e)
    {
        jTextFieldInfo.setText(tempRandSpikeVar.toString());

    }



    void jButtonCellChooserChange_actionPerformed(ActionEvent e)
    {
        CellChooserDialog dlg = new CellChooserDialog(this, "Please select a number of cells for this stimulation",this.myCellChooser);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);

        dlg.setModal(true);
        dlg.setVisible(true);

        logger.logComment("Choosen one: " + dlg.getFinalCellChooser().toString());

        this.myCellChooser = dlg.getFinalCellChooser();

        this.jTextFieldCellNumber.setText(myCellChooser.toString());


    }

    void jButtonLocationChange_actionPerformed(ActionEvent e)
    {
        String cellGroupToStim = (String)jComboBoxCellGroup.getSelectedItem();
        String cellType = project.cellGroupsInfo.getCellType(cellGroupToStim);

        if (jRadioButtonSingle.isSelected())
        {
            
            IndividualSegments chooser = (IndividualSegments)chosenSegLocChooser; 
            String suggestion = "";
            for(int next: chooser.getListOfSegmentIds())
            {
                suggestion = suggestion +next+" ";
            }
            
            String segIdString = JOptionPane.showInputDialog("Please enter the segment ID, or a space seperated list of IDs: ", suggestion);

            String fraction = JOptionPane.showInputDialog("Please enter the fraction along the segment (only used in NEURON): ", chooser.getFractionAlong());
                  
            ArrayList<Integer> ids = new ArrayList<Integer>();
            String[] segIds = segIdString.split(" ");
            for(String s: segIds)
            {
                ids.add(Integer.parseInt(s));
            }
            
            chooser.setListOfSegmentIds(ids);
            chooser.setFractionAlong(Float.parseFloat(fraction));
            
            jTextFieldLocationInfo.setText(chooser.toNiceString());
        }

        if (jRadioButtonDistributed.isSelected())
        {
            GroupDistributedSegments chooser = (GroupDistributedSegments)chosenSegLocChooser;
            
            Cell cellForSelectedGroup = project.cellManager.getCell(cellType);
            
            Vector<String> grps = cellForSelectedGroup.getAllGroupNames();
            String[] names = new String[grps.size()];
            for(int i=0;i<grps.size();i++)
            {
                names[i] = grps.get(i);
            }
            String selectedGroup  
                    = (String)JOptionPane.showInputDialog(this, "Please enter the name of the group from which to choose segment locations: ", 
                    "Select group", JOptionPane.QUESTION_MESSAGE, null, names, chooser.getGroup());
        

            Integer nPoints = new Integer(JOptionPane.showInputDialog("Please enter the number of segment locations to choose along the group: ", chooser.getNumberOfSegments()));

            
            chooser.setGroup(selectedGroup);
            chooser.setNumberOfSegments(nPoints);
            jTextFieldLocationInfo.setText(chooser.toNiceString());
        }


    }


    void jButtonStimChange_actionPerformed(ActionEvent e)
    {
        if (jRadioButtonIClamp.isSelected())
        {
            NumberGenerator oldNumGenDel = tempIClamp.getDel();
            NumberGenerator newNumGenDel = NumberGeneratorDialog.showDialog(this,
                                                                         "Delay",
                                                                         "Please enter the delay before the pulse (ms)", oldNumGenDel);
            tempIClamp.setDel(newNumGenDel);

            NumberGenerator oldNumGenDur = tempIClamp.getDur();
            NumberGenerator newNumGenDur = NumberGeneratorDialog.showDialog(this,
                                                                         "Duration",
                                                                         "Please enter the duration of the pulse (ms)", oldNumGenDur);
            tempIClamp.setDur(newNumGenDur);

            NumberGenerator oldNumGenAmp = tempIClamp.getAmp();

            NumberGenerator newNumGenAmp = NumberGeneratorDialog.showDialog(this,
                                                                         "Amplitude",
                                                                         "Please enter the amplitude of the pulse ("+
                                                     UnitConverter.currentUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+")", oldNumGenAmp);
            tempIClamp.setAmp(newNumGenAmp);

            Object[] opts = new Object[]{"Pulse once", "Repeat pulse"};

            int selected  = 0;
            if (tempIClamp.isRepeat()) selected  = 1;

            Object sel = JOptionPane.showInputDialog(this,
                                        "Should the stimulation repeat continuously after the given duration?",
                                        "Repeat stimulation?",
                                        JOptionPane.QUESTION_MESSAGE ,
                                        null,
                                        opts,
                                        opts[selected]);

            if (sel.equals(opts[0])) tempIClamp.setRepeat(false);
            else if (sel.equals(opts[1])) tempIClamp.setRepeat(true);

            jTextFieldInfo.setText(tempIClamp.toString());

        }

        else if (jRadioButtonIClampVariable.isSelected())
        {
            NumberGenerator oldNumGenDel = tempIClampVariable.getDel();

            NumberGenerator newNumGenDel = NumberGeneratorDialog.showDialog(this,
                                                                         "Delay",
                                                                         "Please enter the delay before the pulse (ms)", oldNumGenDel);
            tempIClampVariable.setDel(newNumGenDel);

            NumberGenerator oldNumGenDur = tempIClampVariable.getDur();
            NumberGenerator newNumGenDur = NumberGeneratorDialog.showDialog(this,
                                                                         "Duration",
                                                                         "Please enter the duration of the pulse (ms)", oldNumGenDur);
            tempIClampVariable.setDur(newNumGenDur);

            String oldNumGenAmp = tempIClampVariable.getAmp();

            String newNumGenAmp = JOptionPane.showInputDialog(this,
                                                                         "Please enter the expression for the amplitude of the pulse as a function of t ("+
                                                     UnitConverter.currentUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+")", oldNumGenAmp);
            tempIClampVariable.setAmp(newNumGenAmp);


            jTextFieldInfo.setText(tempIClampVariable.toString());
        }
        else if (jRadioButtonRandSpike.isSelected())
        {

            NumberGenerator oldNumGen = tempRandSpike.getRate();

            NumberGenerator newNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Frequency of the spike train",
                                                                         "Please enter frequency of the spike train (ms\u207b\u00b9). Note that frequency of this input will not necessarily be equal to the resultant firing frequency of the cell.", oldNumGen);

            try
            {
                tempRandSpike.setRate(newNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the spike rate", ex, this);
                return;
            }

            Vector synapticTypes =  project.cellMechanismInfo.getAllChemSynMechNames();

            if (synapticTypes.size()==0)
            {
                GuiUtils.showErrorMessage(logger, "Please add at least one synapse type at the Cell Mechanism Tab", null, this);
                return;
            }

            Object selection = JOptionPane.showInputDialog(this,
                                        "Please select the type of synaptic input to use as the input for the random spike train",
                                        "Select synapse type",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        synapticTypes.toArray(),
                                        tempRandSpike.getSynapseType());

            if (selection==null) return;

            tempRandSpike.setSynapseType((String)selection);

            jTextFieldInfo.setText(tempRandSpike.toString());

        }
        else if (jRadioButtonRandSpikeExt.isSelected())
        {
            NumberGenerator oldNumGen = tempRandSpikeExt.getRate();

            NumberGenerator newNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Frequency of the spike train",
                                                                         "Please enter frequency of the spike train (ms\u207b\u00b9). Note that frequency of this input will not necessarily be equal to the resultant firing frequency of the cell.",
                                                                         oldNumGen);
            try
            {
                tempRandSpikeExt.setRate(newNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the spike rate", ex, this);
                return;
            }

            Vector synapticTypes =  project.cellMechanismInfo.getAllChemSynMechNames();

            if (synapticTypes.size()==0)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please add at least one synapse type at the Cell Mechanism Tab",
                                          null, this);
                return;
            }

            Object selection = JOptionPane.showInputDialog(this,
                                        "Please select the type of synaptic input to use as the input for the random spike train",
                                        "Select synapse type",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        synapticTypes.toArray(),
                                        tempRandSpikeExt.getSynapseType());

            if (selection==null) return;

            tempRandSpikeExt.setSynapseType((String)selection);

            NumberGenerator oldDelNumGen = tempRandSpikeExt.getDelay();

            NumberGenerator newDelNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Delay before the stimulation",
                                                                         "Please enter the delay before onset of the stimulation (ms)." +
                                                                         "Note that delay of this input will not necessarily be equal to the resultant delay in the simulations.",
                                                                         oldDelNumGen);
            try
            {
                tempRandSpikeExt.setDelay(newDelNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the delay", ex, this);
                return;
            }

            NumberGenerator oldDurNumGen = tempRandSpikeExt.getDuration();

            NumberGenerator newDurNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Duration of the stimulation",
                                                                         "Please enter the duration of the stimulation (ms)",
                                                                         oldDurNumGen);
            try
            {
                tempRandSpikeExt.setDuration(newDurNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the duration", ex, this);
                return;
            }

            Object[] opts = new Object[]{"Pulse once", "Repeat pulse"};

            int selected  = 0;
            if (tempRandSpikeExt.isRepeat()) selected  = 1;


            Object sel = JOptionPane.showInputDialog(this,
                                        "Should the stimulation repeat continuously after the given duration?",
                                        "Repeat stimulation?",
                                        JOptionPane.QUESTION_MESSAGE ,
                                        null,
                                        opts,
                                        opts[selected]);

            if (sel.equals(opts[0])) tempRandSpikeExt.setRepeat(false);

            else if (sel.equals(opts[1])) tempRandSpikeExt.setRepeat(true);

            jTextFieldInfo.setText(tempRandSpikeExt.toString());
        }
        else if (jRadioButtonRandSpikeVar.isSelected())
        {
            String oldRate = tempRandSpikeVar.getRate();


            String newRate = JOptionPane.showInputDialog(this,
                                                         "Please enter the expression for the rate of firing as a function of t ("+
                                                     UnitConverter.currentUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+")" +
                                                     "\n\nNOTE: with an average firing rate smaller than 0.05 the frequency of the function should be less than 1Hz" +
                                                     "\n              (if you need a zero mean you could use a Random spikes in a \"repeat\" mode instead) \n ", oldRate);

            try
            {
                tempRandSpikeVar.setRate(newRate);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the spike rate", ex, this);
                return;
            }

            Vector synapticTypes =  project.cellMechanismInfo.getAllChemSynMechNames();

            if (synapticTypes.size()==0)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please add at least one synapse type at the Cell Mechanism Tab",
                                          null, this);
                return;
            }

            Object selection = JOptionPane.showInputDialog(this,
                                        "Please select the type of synaptic input to use as the input for the random spike train",
                                        "Select synapse type",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        synapticTypes.toArray(),
                                        tempRandSpikeVar.getSynapseType());

            if (selection==null) return;

            tempRandSpikeVar.setSynapseType((String)selection);

            NumberGenerator oldDelNumGen = tempRandSpikeVar.getDelay();

            NumberGenerator newDelNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Delay before the stimulation",
                                                                         "Please enter the delay before onset of the stimulation (ms)",
                                                                         oldDelNumGen);
            try
            {
                tempRandSpikeVar.setDelay(newDelNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the delay", ex, this);
                return;
            }

            NumberGenerator oldDurNumGen = tempRandSpikeVar.getDuration();

            NumberGenerator newDurNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Duration of the stimulation",
                                                                         "Please enter the duration of the stimulation (ms)",
                                                                         oldDurNumGen);
            try
            {
                tempRandSpikeVar.setDuration(newDurNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the duration", ex, this);
                return;
            }

            jTextFieldInfo.setText(tempRandSpikeVar.toString());
        }


    }


    private void setSegChooserInfo(SegmentLocationChooser segCh)
    {
        jTextFieldSegmentId.setText(chosenSegLocChooser.toString());

    }


    public void setStim(StimulationSettings stim)
    {
        this.jTextFieldReference.setText(stim.getReference());

        this.myCellChooser = stim.getCellChooser();

        this.jTextFieldCellNumber.setText(myCellChooser.toString());


        this.jComboBoxCellGroup.setSelectedItem(stim.getCellGroup());

        this.jTextFieldFractionAlong.setText(stim.getFractionAlong()+"");

        String cellType = project.cellGroupsInfo.getCellType(stim.getCellGroup());
        Cell cellForSelectedGroup = project.cellManager.getCell(cellType);



        if (stim instanceof IClampSettings)
        {
            jRadioButtonIClamp.setSelected(true);
            tempIClamp = (IClampSettings)stim;
        }
        else if (stim instanceof RandomSpikeTrainSettings)
        {
            jRadioButtonRandSpike.setSelected(true);

            tempRandSpike = (RandomSpikeTrainSettings)stim;
        }
        else if (stim instanceof RandomSpikeTrainExtSettings)
        {
            jRadioButtonRandSpikeExt.setSelected(true);

            tempRandSpikeExt = (RandomSpikeTrainExtSettings)stim;
        }
        else if (stim instanceof IClampVariableSettings)
        {
            jRadioButtonIClampVariable.setSelected(true);

            tempIClampVariable = (IClampVariableSettings)stim;
        }
        else if (stim instanceof RandomSpikeTrainVariableSettings)
        {
            jRadioButtonRandSpikeVar.setSelected(true);

            tempRandSpikeVar = (RandomSpikeTrainVariableSettings)stim;
        }

        jTextFieldInfo.setText(stim.toString());


        this.chosenSegLocChooser = stim.getSegChooser();
        if(chosenSegLocChooser instanceof IndividualSegments)
        {
            indSegChooser = (IndividualSegments)chosenSegLocChooser;
            jRadioButtonSingle.setSelected(true);
        }
        else if (chosenSegLocChooser instanceof GroupDistributedSegments)
        {
            gds = (GroupDistributedSegments)chosenSegLocChooser;
            jRadioButtonDistributed.setSelected(true);
        }
        
        jTextFieldLocationInfo.setText(chosenSegLocChooser.toString());

        //Segment segToStim = checkSegId();

        setSegChooserInfo(chosenSegLocChooser);

    }

    void jButtonSegment_actionPerformed(ActionEvent e)
    {
        String cellGroupToStim = (String)jComboBoxCellGroup.getSelectedItem();

        /*
        String cellType = project.cellGroupsInfo.getCellType(cellGroupToStim);
        Cell cellForSelectedGroup = project.cellManager.getCell(cellType);

        String message = "Please specify which segment to stimulate";

        //String answer = JOptionPane.showInputDialog(message, this.chosenSegmentId+"");

        Vector<Segment> segs = cellForSelectedGroup.getAllSegments();
        String[] opts = new String[segs.size()];
        String pref = null;

        for (int i=0;i<segs.size();i++)
        {
            Segment seg = segs.get(i);
            opts[i] = seg.getSegmentId()+": "+seg.getSegmentName();

            if (chosenSegmentId == seg.getSegmentId())
            {
                logger.logComment("Pref is: "+opts[i]);
                pref = opts[i];
            }
        }
        if (pref == null) pref = opts[0];

        String answer = (String)JOptionPane.showInputDialog(this, message, "Select segment to stimulate",
            JOptionPane.QUESTION_MESSAGE, null, opts, pref);

        if (answer == null) return;

        answer = answer.substring(0,answer.indexOf(":"));

        try
        {
            chosenSegmentId = Integer.parseInt(answer);

            Segment segToStim = cellForSelectedGroup.getSegmentWithId(chosenSegmentId);


            setSegInfo(segToStim);

        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a correct integer (0 to "
            + ((Segment)cellForSelectedGroup.getAllSegments().lastElement()).getSegmentId()+") for the ID", ex, this);
            return;
        }
        catch (NullPointerException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter a correct integer (0 to "
            + ((Segment)cellForSelectedGroup.getAllSegments().lastElement()).getSegmentId()+") for the ID", ex, this);
            return;
        }

*/



    }


}
