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

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

/**
 * Dialog to specify electrophysiological stimulations to add to the network
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */


@SuppressWarnings("serial")

public class StimDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("StimDialog");

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
                                                                          10,
                                                                          null);



    int chosenSegmentId = 0;

    CellChooser myCellChooser = new AllCells();

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
    JTextField jTextFieldCellNumber = new JTextField();
    JLabel jLabelSegment = new JLabel();
    JTextField jTextFieldSegmentId = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    //JLabel jLabelCellNum2 = new JLabel();
    JButton jButtonSegment = new JButton();


    JRadioButton jRadioButtonIClamp = new JRadioButton();
    JRadioButton jRadioButtonRandSpike = new JRadioButton();
    JRadioButton jRadioButtonRandSpikeExt = new JRadioButton();



    JButton jButtonStimChange = new JButton();
    JButton jButtonCellChooserChange = new JButton();


    ButtonGroup buttonGroupStims = new ButtonGroup();
    JTextField jTextFieldInfo = new JTextField();
    JLabel jLabelFraction = new JLabel();
    JTextField jTextFieldFractionAlong = new JTextField();


    public StimDialog(Dialog owner, String suggestedRef, Project project)
    {
        super(owner, "Choose a stimulation to apply to the network", false);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            this.project = project;

            jbInit();
            extraInit();
            jTextFieldReference.setText(suggestedRef);
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

    }


    public StimDialog(Frame owner, String suggestedRef, Project project)
    {
        super(owner, "Choose a stimulation to apply to the network", false);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            this.project = project;

            Vector synapticTypes =  project.cellMechanismInfo.getAllSynMechNames();

            if (synapticTypes.size()>0)
            {
                tempRandSpike.setSynapseType((String)synapticTypes.elementAt(0));
                tempRandSpikeExt.setSynapseType((String)synapticTypes.elementAt(0));
            }
            else
            {

                Exp2SynMechanism exp2 = new Exp2SynMechanism();
                
                exp2.setInstanceName("SynForRndSpike");
                project.cellMechanismInfo.addCellMechanism(exp2);

                tempRandSpike.setSynapseType(exp2.getInstanceName());
                tempRandSpikeExt.setSynapseType(exp2.getInstanceName());
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

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        for (int i = 0; i < cellGroupNames.size(); i++)
        {
                jComboBoxCellGroup.addItem(cellGroupNames.get(i));
        }

        jRadioButtonIClamp.setSelected(true);

        jTextFieldInfo.setText(tempIClamp.toString());

        String cellGroupToStim = (String)jComboBoxCellGroup.getSelectedItem();

        String cellType = project.cellGroupsInfo.getCellType(cellGroupToStim);
        Cell cellForSelectedGroup = project.cellManager.getCell(cellType);


        Segment segToStim = cellForSelectedGroup.getSegmentWithId(chosenSegmentId);


        jTextFieldSegmentId.setText(segToStim.getSegmentName()
                            + " (ID: "
                            + segToStim.getSegmentId()
                            + ")");


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

        stim.setReference(jTextFieldReference.getText());
        stim.setCellGroup((String)jComboBoxCellGroup.getSelectedItem());

        stim.setCellChooser(this.myCellChooser);

        stim.setSegmentID(chosenSegmentId);
        stim.setFractionAlong(Float.parseFloat(jTextFieldFractionAlong.getText()));

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

        jLabelSegment.setText("Segment Id:");
        jTextFieldSegmentId.setEditable(false);
        jTextFieldSegmentId.setText("...");
        jTextFieldSegmentId.setColumns(10);


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
        jRadioButtonRandSpikeExt.setText("Random spike input (extended, not complete!!)");
        jRadioButtonRandSpikeExt.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jRadioButtonRandSpikeExt_actionPerformed(e);
            }
        });





        jButtonStimChange.setText("Change..");
        jButtonCellChooserChange.setText("Change...");
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
        jLabelFraction.setText("Fraction along (only used for NEURON):");
        jTextFieldFractionAlong.setText("0.5");
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

        jPanelMain.add(jLabelSegment,            new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jTextFieldSegmentId,               new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 6), 0, 0));


        jPanelMain.add(this.jButtonCellChooserChange,          new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 14, 6, 0), 0, 0));


        jPanelMain.add(jButtonSegment,          new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 6, 6, 14), 0, 0));

        jPanelMain.add(jRadioButtonIClamp,
                       new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonRandSpike,
                       new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));

        jPanelMain.add(jRadioButtonRandSpikeExt,
                       new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.WEST,
                                              GridBagConstraints.NONE,
                                              new Insets(6, 14, 6, 0), 0, 0));


        jPanelMain.add(jButtonStimChange,    new GridBagConstraints(1, 5, 2, 3, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        jPanelMain.add(jTextFieldInfo,   new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));


        jPanelMain.add(jLabelFraction,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));




        buttonGroupStims.add(jRadioButtonIClamp);
        buttonGroupStims.add(jRadioButtonRandSpike);
        buttonGroupStims.add(jRadioButtonRandSpikeExt);


        jPanelMain.add(jTextFieldFractionAlong,   new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14), 0, 0));
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



    void jButtonStimChange_actionPerformed(ActionEvent e)
    {
        if (jRadioButtonIClamp.isSelected())
        {
            SequenceGenerator sg1 = tempIClamp.getDelay();

            SequenceGenerator newSg1 = SequenceGeneratorDialog.showDialog(this,
                                                                          "Please enter the delay before the pulse (ms)",
                                                                          sg1);

            if (newSg1==null) return;

            tempIClamp.setDelay(newSg1);

            SequenceGenerator sg2 = tempIClamp.getDuration();

            SequenceGenerator newSg2 = SequenceGeneratorDialog.showDialog(this,
                                                                          "Please enter the duration of the pulse (ms)",
                                                                          sg2);

            if (newSg2==null) return;

            tempIClamp.setDuration(newSg2);


            SequenceGenerator sg3 = tempIClamp.getAmplitude();

            String message = "Please enter the amplitude of the pulse ("+
                                                     UnitConverter.currentUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+")";

            SequenceGenerator newSg3 = SequenceGeneratorDialog.showDialog(this, message, sg3);


            tempIClamp.setAmplitude(newSg3);



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
        else if (jRadioButtonRandSpike.isSelected())
        {
            //String inputValue = JOptionPane.showInputDialog(this, "Please enter frequency of the spike train (ms^-1)",
            //                                                tempRandSpike.getRate()+"");

            NumberGenerator oldNumGen = tempRandSpike.getRate();

            NumberGenerator newNumGen = NumberGeneratorDialog.showDialog(this,
                                                                         "Frequency of the spike train",
                                                                         "Please enter frequency of the spike train (ms\u207b\u00b9). Note that frequency of this input will not necessarily be equal to the resultant firing frequency of the cell.", oldNumGen);

            //if (inputValue==null) return;

            try
            {
                tempRandSpike.setRate(newNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the spike rate", ex, this);
                return;
            }

            Vector synapticTypes =  project.cellMechanismInfo.getAllSynMechNames();

            if (synapticTypes.size()==0)
            {
                GuiUtils.showErrorMessage(logger, "Please add at least one synapse type at the Cell Process Tab", null, this);
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

            Vector synapticTypes =  project.cellMechanismInfo.getAllSynMechNames();

            if (synapticTypes.size()==0)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please add at least one synapse type at the Cell Process Tab",
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

            try
            {
                tempRandSpikeExt.setRate(newNumGen);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the spike rate", ex, this);
                return;
            }



            String delayString = JOptionPane.showInputDialog("Please enter the delay before onset of the stimulation (ms)",
                                                       tempRandSpikeExt.getDelay());


            try
            {
                float delay = Float.parseFloat(delayString);
                assert delay>=0;
                tempRandSpikeExt.setDelay(delay);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the delay (>=0ms)", ex, this);
                return;
            }

            String durationString = JOptionPane.showInputDialog("Please enter the duration of the stimulation (ms)",
                                                       tempRandSpikeExt.getDuration());


            try
            {
                float duration = Float.parseFloat(durationString);
                assert duration>=0;
                tempRandSpikeExt.setDuration(duration);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Please enter a proper value for the duration (>=0ms)", ex, this);
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


        //Vector segments = cellForSelectedGroup.getAllSegments();
        Segment segToStim = cellForSelectedGroup.getSegmentWithId(stim.getSegmentID());

        jTextFieldSegmentId.setText(segToStim.getSegmentName()
                                    + " (ID: "
                                    + segToStim.getSegmentId()
                                    + ")");

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


        jTextFieldInfo.setText(stim.toString());

    }

    void jButtonSegment_actionPerformed(ActionEvent e)
    {
        String cellGroupToStim = (String)jComboBoxCellGroup.getSelectedItem();

        String cellType = project.cellGroupsInfo.getCellType(cellGroupToStim);
        Cell cellForSelectedGroup = project.cellManager.getCell(cellType);

        String message = "Please specify which segment to stimulate (0 to "
            + ((Segment)cellForSelectedGroup.getAllSegments().lastElement()).getSegmentId()+")";

        String answer = JOptionPane.showInputDialog(message, this.chosenSegmentId+"");

        try
        {
            chosenSegmentId = Integer.parseInt(answer);


            jTextFieldSegmentId.setText(cellForSelectedGroup.getSegmentWithId(chosenSegmentId).getSegmentName()
                                        + " (ID: "
                                        + chosenSegmentId
                                        + ")");

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






/*
        SegmentSelector dlg = new SegmentSelector((Frame)this.getOwner(), project,
                                                  cellForSelectedGroup, false);

        //Vector segments = cellForSelectedGroup.getAllSegments();
        Segment segToStim = cellForSelectedGroup.getSegmentWithId(chosenSegmentId);

        dlg.setSelectedSegment(segToStim);

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


        if (dlg.cancelled) return;

        if (dlg.getSelectedSegment() == null) return;


        chosenSegmentId =  dlg.getSelectedSegment().getSegmentId();

        jTextFieldSegmentId.setText(dlg.getSelectedSegment().getSegmentName()
                                    + " (ID: "
                                    + dlg.getSelectedSegment().getSegmentId()
                                    + ")");
*/
    }


}
