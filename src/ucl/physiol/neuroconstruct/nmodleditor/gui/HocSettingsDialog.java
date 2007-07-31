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

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;
import ucl.physiol.neuroconstruct.nmodleditor.processes.*;
import ucl.physiol.neuroconstruct.gui.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class HocSettingsDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("HocSettingsDialog");

    public boolean wasCancelled = false;
    public boolean fileSuccessfullyGenerated = false;

    public float runtime;
    public float dt;

    public boolean isStimulation = false;
    public float stimDelay;
    public float stimDur;
    public float stimAmp;

    NeuronElement myNeuronElement = null;

    Hashtable paramInfo = new Hashtable();

    File myFile = null;


    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelParameters = new JPanel();
    JPanel jPanelOKCancel = new JPanel();
    JButton jButtonCancel = new JButton();
    JButton jButtonRunFile = new JButton();
    JPanel jPanelStimulations = new JPanel();
    JCheckBox jCheckBoxAddStimulation = new JCheckBox();
    JPanel jPanelStimSettings = new JPanel();
    JLabel jLabelStimDur = new JLabel();
    JTextField jTextFieldstimDur = new JTextField();
    JLabel jLabelStimDel = new JLabel();
    JLabel jLabelStimAmp = new JLabel();
    JTextField jTextFieldStimDelay = new JTextField();
    JTextField jTextFieldStimAmp = new JTextField();
    JPanel jPanelSimRuntime = new JPanel();
    JLabel jLabelSimDT = new JLabel();
    JLabel jLabelSimDuration = new JLabel();
    JTextField jTextFieldSimDT = new JTextField();
    JTextField jTextFieldSimDuration = new JTextField();
    JButton jButtonViewHoc = new JButton();
    JLabel jLabelParameters = new JLabel();

    public HocSettingsDialog(String title,
                             File hocFile,
                             NeuronElement neuronElement,
                             java.util.List paramList)
    {
        super((Frame)null, title, false);
        try
        {
            jbInit();

            myFile = hocFile;
            myNeuronElement = neuronElement;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        Iterator params = paramList.iterator();
        while (params.hasNext())
        {
            ParametersElement.ParameterEntry param = (ParametersElement.ParameterEntry)params.next();
            double value = param.value;
            if (value==Double.MIN_VALUE) value = 0; // this is the case where the valus is not specified in the mod file
            addParameterRequest(param.name, value);
        }

    }


    private void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        jButtonRunFile.setText("Run simulation");
        jButtonRunFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRunFile_actionPerformed(e);
            }
        });
        jCheckBoxAddStimulation.setText("Add Stimulation");
        jCheckBoxAddStimulation.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jCheckBoxAddStimulation_itemStateChanged(e);
            }
        });

        jLabelStimDur.setText("Duration:");
        jTextFieldstimDur.setEditable(false);
        jTextFieldstimDur.setText("");
        jTextFieldstimDur.setColumns(5);
        jLabelStimDel.setText("Delay:");
        jLabelStimAmp.setText("Amplitude:");
        jPanelStimSettings.setMaximumSize(new Dimension(380, 35));
        jPanelStimSettings.setMinimumSize(new Dimension(380, 35));
        jPanelStimSettings.setPreferredSize(new Dimension(380, 35));
        jPanelStimulations.setBorder(BorderFactory.createEtchedBorder());
        jPanelStimulations.setMaximumSize(new Dimension(320, 110));
        jPanelStimulations.setMinimumSize(new Dimension(320, 110));
        jPanelStimulations.setPreferredSize(new Dimension(320, 110));
        jPanelParameters.setBorder(BorderFactory.createEtchedBorder());
        jTextFieldStimAmp.setEditable(false);
        jTextFieldStimAmp.setText("");
        jTextFieldStimAmp.setColumns(5);
        jTextFieldStimDelay.setEditable(false);
        jTextFieldStimDelay.setText("");
        jTextFieldStimDelay.setColumns(5);
        panel1.setMinimumSize(new Dimension(400, 400));
        panel1.setPreferredSize(new Dimension(400, 400));
        jLabelSimDT.setText("Increment (ms):");
        jLabelSimDuration.setText("Simulation duration (ms):");
        jTextFieldSimDT.setText("0.025");
        jTextFieldSimDT.setColumns(5);
        jTextFieldSimDuration.setText("300");
        jTextFieldSimDuration.setColumns(5);
        jButtonViewHoc.setText("View *.hoc file");
        jButtonViewHoc.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonViewHoc_actionPerformed(e);
            }
        });
        jLabelParameters.setText("Parameters for simulation:");
        jPanelStimulations.add(jPanelSimRuntime, null);
        jPanelStimulations.add(jCheckBoxAddStimulation, null);
        jPanelStimulations.add(jPanelStimSettings, null);
        getContentPane().add(panel1);
        panel1.add(jPanelParameters, BorderLayout.CENTER);
        panel1.add(jPanelOKCancel, BorderLayout.SOUTH);
        jPanelOKCancel.add(jButtonRunFile, null);
        jPanelOKCancel.add(jButtonViewHoc, null);
        jPanelOKCancel.add(jButtonCancel, null);
        panel1.add(jPanelStimulations, BorderLayout.NORTH);

        jPanelStimSettings.add(jLabelStimAmp, null);
        jPanelStimSettings.add(jTextFieldStimAmp, null);
        jPanelStimSettings.add(jLabelStimDel, null);
        jPanelStimSettings.add(jTextFieldStimDelay, null);
        jPanelStimSettings.add(jLabelStimDur, null);
        jPanelStimSettings.add(jTextFieldstimDur, null);
        jPanelSimRuntime.add(jLabelSimDuration, null);
        jPanelSimRuntime.add(jTextFieldSimDuration, null);
        jPanelSimRuntime.add(jLabelSimDT, null);
        jPanelSimRuntime.add(jTextFieldSimDT, null);
        jPanelParameters.add(jLabelParameters, null);
    }


    public static void main(String[] args)
    {
        ModFileChangeListener listener = new ModFileChangeListener()
        {
            public void modFileElementChanged(String modFileElementType)
            {
                System.out.println("Change in: " + modFileElementType);
            }
            public void modFileChanged(){};
        };

        NeuronElement ne = new NeuronElement(listener);
        ne.setProcess(NeuronElement.DENSITY_MECHANISM);

        try
        {
            ne.setProcessName("ff");
        }
        catch (ModFileException ex)
        {
        }

        HocSettingsDialog dlg = new HocSettingsDialog("Title", new File("c:\\temp\\temp.hoc"), ne, null);
        Dimension dlgSize = dlg.getPreferredSize();

        dlg.addParameterRequest("Dog", 5);
        dlg.addParameterRequest("Cat", 5);
        dlg.addParameterRequest("Mouse type crreature:", 5);

        dlg.pack();
        dlg.setVisible(true);

        //System.out.println("Val of dog: "+ dlg.getParameterValue("Dog"));

    }


    private String generate() throws ModFileException
    {
        TestHocFileGenerator testHocGen = getTestHocFileGenerator();

        testHocGen.generateTheHocFile();

        return testHocGen.getGeneratedFilename();
    }

    private TestHocFileGenerator getTestHocFileGenerator() throws ModFileException
    {
        TestHocFileGenerator testHocGen = new TestHocFileGenerator(myFile);

        testHocGen.setMainSimulationParams(runtime, dt);

        if (myNeuronElement.getProcess() == NeuronElement.DENSITY_MECHANISM)
        {
            testHocGen.addDensityMechanism(myNeuronElement.getProcessName());
        }
        else if (myNeuronElement.getProcess() == NeuronElement.POINT_PROCESS)
        {
            testHocGen.addPointProcess(myNeuronElement.getProcessName());
        }

        Enumeration paramNames = paramInfo.keys();

        while (paramNames.hasMoreElements())
        {
            String nextParamName = (String)paramNames.nextElement();
            float value = 0;
            try
            {
                JTextField textField = (JTextField) paramInfo.get(nextParamName);
                value = Float.parseFloat(textField.getText());
                testHocGen.addInitialParameterSetting(nextParamName, value);
            }
            catch (Exception ex)
            {
                // just let NEURON use default value...
            }
        }
        if (isStimulation)
        {
            TestStimulationSettings stim = new TestStimulationSettings(stimDelay,stimDur,stimAmp);
            testHocGen.addStimulation(stim);
        }
        return testHocGen;

    }

    private void addParameterRequest(String paramName, double suggestedValue)
    {
        JPanel jPanelNew = new JPanel();
        BorderLayout borderLayoutNew = new BorderLayout();
        jPanelNew.setLayout(borderLayoutNew);
        JLabel jLabelNew = new JLabel(paramName+":");
        jLabelNew.setPreferredSize(new Dimension(200, 20));
        jLabelNew.setMinimumSize(new Dimension(200, 20));
        jPanelNew.add(jLabelNew, "Center");
        JTextField jTextFieldNew = new JTextField(suggestedValue+"");

        paramInfo.put(paramName, jTextFieldNew);

        jTextFieldNew.setColumns(5);
        jPanelNew.add(jTextFieldNew, "East");

        jPanelParameters.add(jPanelNew);
    }
/*
    public float getParameterValue(String paramName)
    {
        try
        {
            JTextField textField = (JTextField) paramInfo.get(paramName);
            float paramVal = Float.parseFloat(textField.getText());
            return paramVal;
        }
        catch (Exception ex)
        {
            return 0.0F;
        }
    }
*/
    void jCheckBoxAddStimulation_itemStateChanged(ItemEvent e)
    {
        logger.logComment("State changed");
        if (jCheckBoxAddStimulation.isSelected())
        {
            jTextFieldStimAmp.setEditable(true);
            jTextFieldStimDelay.setEditable(true);
            jTextFieldstimDur.setEditable(true);
            if (jTextFieldStimAmp.getText().equals("")) jTextFieldStimAmp.setText("0.1");
            if (jTextFieldStimDelay.getText().equals("")) jTextFieldStimDelay.setText("100");
            if (jTextFieldstimDur.getText().equals("")) jTextFieldstimDur.setText("100");
        }
        else
        {
            jTextFieldStimAmp.setEditable(false);
            jTextFieldStimDelay.setEditable(false);
            jTextFieldstimDur.setEditable(false);
        }
    }

    void jButtonRunFile_actionPerformed(ActionEvent e)
    {

        readValues();

        String generatedFileName = null;

        try
        {
            generatedFileName = generate();
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }

        if (generatedFileName == null)
        {
            GuiUtils.showErrorMessage(logger, "Problem generating *.hoc file for testing mod process", null, this);
            return;
        }

        this.wasCancelled = false;
        this.fileSuccessfullyGenerated = true;

        ProcessManager runProcess = new ProcessManager(new File(generatedFileName));

        try
        {
            runProcess.runAsHocFile();
        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }

        this.dispose();
    }


    private void readValues()
    {
        try
        {
            runtime = Float.parseFloat(jTextFieldSimDuration.getText());
        }
        catch (NumberFormatException ex)
        {
            showFormatError("Simulation duration");
            return;
        }
        try
        {
            dt = Float.parseFloat(jTextFieldSimDT.getText());
        }
        catch (NumberFormatException ex)
        {
            showFormatError("Simulation increment");
            return;
        }
        if (jCheckBoxAddStimulation.isSelected())
        {
            isStimulation = true;
            try
            {
                stimAmp = Float.parseFloat(jTextFieldStimAmp.getText());
            }
            catch (NumberFormatException ex)
            {
                showFormatError("Stimulation amplitude");
                return;
            }
            try
            {
                stimDelay = Float.parseFloat(jTextFieldStimDelay.getText());
            }
            catch (NumberFormatException ex)
            {
                showFormatError("Stimulation delay");
                return;
            }
            try
            {
                stimDur = Float.parseFloat(jTextFieldstimDur.getText());
            }
            catch (NumberFormatException ex)
            {
                showFormatError("Stimulation duration");
                return;
            }
        }
        else
        {
            isStimulation = false;
        }
    }


    private void showFormatError(String fieldName)
    {
        GuiUtils.showErrorMessage(logger, "Please enter a correctly formatted number into the "+fieldName+" field", null, null);
    }


    void jButtonViewHoc_actionPerformed(ActionEvent e)
    {
        readValues();
        try
        {
            TestHocFileGenerator testHocGen = getTestHocFileGenerator();

            testHocGen.generateTheHocFile();

            SimpleViewer simpleViewer = null;

            simpleViewer = new SimpleViewer(testHocGen.getGeneratedFilename(),
                                            "Generated hoc file",
                                            12,
                                            false,
                                            false);

            simpleViewer.pack();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height)
                frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width)
                frameSize.width = screenSize.width;

            simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2,
                                     (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);


        }
        catch (ModFileException ex)
        {
            GuiUtils.showErrorMessage(logger, ex.getMessage(), ex, this);
            return;
        }

    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        this.wasCancelled = true;
        fileSuccessfullyGenerated = false;
        this.dispose();
    }




}
