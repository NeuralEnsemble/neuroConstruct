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
import javax.swing.*;
import java.awt.event.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;
import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class NewFileDialog extends JDialog
{
    String newFileName = null;
    String newDirectory = null;

    private boolean cancelled = true;

    private int myProcessType;

    String suggestedDir = null;

    JPanel panelMain = new JPanel();
    JLabel jLabelMain = new JLabel();
    JTextField jTextFieldFileName = new JTextField();
    //JTextField jTextFieldName = new JTextField();
    JButton jButtonOK = new JButton();
    JLabel jLabelProcess = new JLabel();
    JTextField jTextFieldProcess = new JTextField();
    JRadioButton jRadioButtonPointProcess = new JRadioButton();
    JRadioButton jRadioButtonDensMech = new JRadioButton();
    JButton jButtonCancel = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    ButtonGroup buttonGroup1 = new ButtonGroup();
    JLabel jLabel1 = new JLabel();
    JTextField jTextFieldDirectory = new JTextField();
    JButton jButtonChooseDir = new JButton();
    JPanel jPanelButtons = new JPanel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();



    public NewFileDialog(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
        try
        {
            jbInit();
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private NewFileDialog()
    {

    }


    private void jbInit() throws Exception
    {
        panelMain.setLayout(gridBagLayout1);
        jLabelMain.setText("New filename:");
        String suggestedFileName = "test.mod";

        suggestedDir = System.getProperty("user.dir");

        jTextFieldFileName.setText(suggestedFileName);

        jTextFieldDirectory.setText(suggestedDir);

        jTextFieldFileName.setColumns(20);
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        jLabelProcess.setToolTipText("");
        jLabelProcess.setVerifyInputWhenFocusTarget(true);
        jLabelProcess.setText("Process name:");
        jTextFieldProcess.setText("test");
        jTextFieldProcess.setColumns(12);
        jRadioButtonPointProcess.setSelected(true);
        jRadioButtonPointProcess.setText("Point process");
        jRadioButtonDensMech.setText("Density Mechanism");
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        jLabel1.setText("Directory");
        jTextFieldDirectory.setColumns(10);
        jButtonChooseDir.setText("...");
        jButtonChooseDir.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonChooseDir_actionPerformed(e);
            }
        });


        jPanelButtons.setLayout(gridBagLayout2);
        getContentPane().add(panelMain, BorderLayout.CENTER);
        panelMain.add(jLabelMain, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                         , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                         new Insets(12, 20, 12, 0), 0, 0));
        panelMain.add(jTextFieldFileName, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
                                                             , GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                                                             new Insets(12, 12, 12, 20), 0, 0));
        panelMain.add(jLabelProcess, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                            , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                            new Insets(12, 20, 12, 0), 0, 0));
        panelMain.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                      , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                      new Insets(12, 20, 12, 0), 0, 0));
        panelMain.add(jTextFieldDirectory, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                                          , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                                          new Insets(12, 12, 12, 12), 200, 0));
        panelMain.add(jTextFieldProcess, new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
                                                                , GridBagConstraints.EAST,
                                                                GridBagConstraints.HORIZONTAL,
                                                                new Insets(12, 12, 12, 20), 0, 0));
        panelMain.add(jButtonChooseDir, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
                                                       , GridBagConstraints.EAST, GridBagConstraints.NONE,
                                                       new Insets(0, 0, 0, 20), 0, 0));
        this.getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jRadioButtonDensMech, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 20, 12, 12), 0, 0));
        jPanelButtons.add(jRadioButtonPointProcess, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(12, 12, 12, 20), 0, 0));
        jPanelButtons.add(jButtonOK, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                            , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                            new Insets(12, 0, 12, 0), 0, 0));
        jPanelButtons.add(jButtonCancel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                new Insets(5, 0, 5, 13), 0, 0));
        buttonGroup1.add(jRadioButtonPointProcess);
        buttonGroup1.add(jRadioButtonDensMech);
    }

    public void setSuggestedDir(String dir)
    {
        this.suggestedDir = dir;
        jTextFieldDirectory.setText(dir);
    }

    public String getFullFileName()
    {
        return newDirectory +System.getProperty("file.separator")+ newFileName;
    }

    public boolean getCancelled()
    {
        return this.cancelled;
    }

    public String getProcessName()
    {
        return this.jTextFieldProcess.getText();
    }

    public int getProcessType()
    {
        return this.myProcessType;
    }


    void jButtonOK_actionPerformed(ActionEvent e)
    {
        newFileName = jTextFieldFileName.getText();
        newDirectory = jTextFieldDirectory.getText();

        if (!newFileName.endsWith(".mod"))
        {
            newFileName = new String(newFileName + ".mod");
        }
        if (jRadioButtonDensMech.isSelected())
            myProcessType = NeuronElement.DENSITY_MECHANISM;
            else myProcessType = NeuronElement.POINT_PROCESS;
        cancelled = false;
        this.dispose();

    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        cancelled = true;
        this.dispose();
    }

    void jButtonChooseDir_actionPerformed(ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        try
        {
            File currentSuggestion = new File(jTextFieldDirectory.getText());
            chooser.setCurrentDirectory(currentSuggestion);
        }
        catch (Exception ex)
        {
            File defLocation = ProjectStructure.getnCProjectsDirectory();
            chooser.setCurrentDirectory(defLocation);
        }

        int retval = chooser.showDialog(this, null);

        if (retval == JFileChooser.APPROVE_OPTION)
        {
            String dirName = chooser.getSelectedFile().getAbsolutePath();
            jTextFieldDirectory.setText(dirName);
        }

    }


}
