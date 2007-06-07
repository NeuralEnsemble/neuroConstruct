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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.nmodleditor.modfile.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Dialog for creating new Synapse types
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class NewModFileDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("NewModFileDialog");
    public boolean cancelled = false;

    String newMechanismName = null;

    ModFile chosenModFile = null;
    String  chosenInBuiltModFile = null;


    String firstLineCombo = "-- Please select a template for the mod file --";

    JPanel jPanel1 = new JPanel();
    JLabel jLabelMain = new JLabel();
    JComboBox jComboBoxTemplates = new JComboBox();
    JButton jButtonOK = new JButton();
    JPanel jPanel2 = new JPanel();
    JButton jButtonCancel = new JButton();
    JLabel jLabelName = new JLabel();
    JTextField jTextFieldNewMechanismName = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabelFileName = new JLabel();
    JTextField jTextFieldFileName = new JTextField();
    JButton jButtonSelectFile = new JButton();

    Frame myParent = null;

    public String selectedFileName = null;

    private NewModFileDialog()
    {

    }



    public NewModFileDialog(Frame owner, String title) throws HeadlessException
    {
        super(owner, title, true);

        myParent = owner;
        try
        {
            jbInit();
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

    }
    private void jbInit() throws Exception
    {
        jLabelMain.setText("Please select a template for the new mod file:");
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
        jLabelName.setVerifyInputWhenFocusTarget(true);

 ////       jTextFieldNew.setText(newSynapseName);

  ////      jTextFieldNewSynapseName.setColumns(12);
        jComboBoxTemplates.setToolTipText("");
        jComboBoxTemplates.setSelectedItem(this);
        jComboBoxTemplates.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxTemplates_itemStateChanged(e);
            }
        });
        jPanel1.setLayout(gridBagLayout1);
        jLabelFileName.setText("Or select another *.mod file:");
        jTextFieldFileName.setEnabled(false);
        jTextFieldFileName.setEditable(false);
        jTextFieldFileName.setText("");
        jTextFieldFileName.setColumns(20);
        jButtonSelectFile.setText("Choose...");
        jButtonSelectFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSelectFile_actionPerformed(e);
            }
        });
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
        this.getContentPane().add(jPanel2,  BorderLayout.SOUTH);
        jPanel2.add(jButtonOK, null);
        jPanel2.add(jButtonCancel, null);
        jPanel1.add(jLabelFileName,             new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 0));
        jPanel1.add(jLabelMain,            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 20), 0, 0));
        jPanel1.add(jLabelName,        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 0));
        jPanel1.add(jTextFieldNewMechanismName,          new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 12, 20), 0, 0));
        jPanel1.add(jComboBoxTemplates,         new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 0, 0));
        jPanel1.add(jTextFieldFileName,        new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 20, 12, 20), 40, 0));
        jPanel1.add(jButtonSelectFile,       new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 12, 0, 12), 0, 0));


    }

    /**
     * Extra initiation stuff, not automatically added by IDE
     */
    public void initialiseAsSynapseMaker()
    {
        logger.logComment("Initialising as a Synapse maker...");
        jComboBoxTemplates.addItem(firstLineCombo);

        jLabelName.setText("Name of New Synapse Type           ");

        File modTemplatesDir = null;
        try
        {
            modTemplatesDir = ProjectStructure.getModTemplatesDir();
            logger.logComment("modTemplatesDir: "+ modTemplatesDir.getAbsolutePath());
        }
        catch (Exception ex)
        {
            logger.logError("Problem...");
            return;
        }
        File[] contents = modTemplatesDir.listFiles();

        logger.logComment("There are "+contents.length+" files in the dir: "+ modTemplatesDir.getAbsolutePath());

        for (int i = 0; i < contents.length; i++)
        {
            logger.logComment("Looking at file: " + contents[i].getAbsolutePath());
            if (contents[i].getName().endsWith(".mod"))
            {
                logger.logComment("Could be a mod file...");
                try
                {
                    ModFile modFile = new ModFile(contents[i].getAbsolutePath());

                    if (modFile != null)
                    {
                        if (modFile.myNetReceiveElement != null &&
                            modFile.myNetReceiveElement.getUnformattedLines().length > 0)
                        {
                            logger.logComment("Has a NET_RECEIVE element...");

                            jComboBoxTemplates.addItem(modFile);
                        }
                        else
                        {
                            logger.logComment("Doesn't have a NET_RECEIVE element...");
                        }
                    }
                    else
                    {
                        logger.logComment("Problem reading file: "
                                    + contents[i].getAbsolutePath()
                                    +" (Ignoring file)");

                    }
                }
                catch (ModFileException ex)
                {
                    logger.logComment("Problem reading file: "
                                    + contents[i].getAbsolutePath()
                                    + ": "
                                    + ex.getMessage()
                                    + " (Ignoring file)");

                }
            }
        }
    }


    /**
     * Extra initiation stuff, not automatically added by IDE
     */
    public void initialiseAsChannelMechanismMaker()
    {
        logger.logComment("Initialising as a Channel Mechanism maker...");
        jComboBoxTemplates.addItem(firstLineCombo);

        jLabelName.setText("Name of New Channel Mechanism         ");

        File modTemplatesDir = null;
        try
        {
            modTemplatesDir = ProjectStructure.getModTemplatesDir();
            logger.logComment("modTemplatesDir: "+ modTemplatesDir.getAbsolutePath());
        }
        catch (Exception ex)
        {
            logger.logError("Problem...");
            return;
        }
        File[] contents = modTemplatesDir.listFiles();

        logger.logComment("There are "+contents.length+" files in the dir: "+ modTemplatesDir.getAbsolutePath());

        for (int i = 0; i < contents.length; i++)
        {
            logger.logComment("Looking at file: " + contents[i].getAbsolutePath());
            if (contents[i].getName().endsWith(".mod"))
            {
                logger.logComment("Could be a mod file...");
                try
                {
                    ModFile modFile = new ModFile(contents[i].getAbsolutePath());

                    if (modFile != null)
                    {
                        if (modFile.myNeuronElement != null
                            && modFile.myNeuronElement.getProcess() == NeuronElement.DENSITY_MECHANISM)
                        {
                            logger.logComment("Is a channel mechanismelement...");
                            jComboBoxTemplates.addItem(modFile);
                        }
                    }
                    else
                    {
                        logger.logComment("Problem reading file: "
                                    + contents[i].getAbsolutePath()
                                    +" (Ignoring file)");


                    }
                }
                catch (ModFileException ex)
                {
                    logger.logComment("Problem reading file: "
                                    + contents[i].getAbsolutePath()
                                    +": "
                                    + ex.getMessage()
                                    + " (Ignoring file)");
                }
            }
        }
    }


    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;
        this.dispose();
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");
        this.newMechanismName = this.jTextFieldNewMechanismName.getText();

        if (jComboBoxTemplates.getSelectedItem().equals(firstLineCombo))
        {
            String filename = jTextFieldFileName.getText();

            if (filename==null||filename.length()<3)
            {
                cancelled = true;
            }
            else
            {
                try
                {
                    ModFile modFile = new ModFile(filename);

                    modFile.myNetReceiveElement.getUnformattedLines();

                    logger.logComment("No problem parsing file: " + modFile.toString());
                    chosenModFile = modFile;
                }
                catch (ModFileException ex)
                {
                    logger.logComment("Problem with file...");
                    GuiUtils.showErrorMessage(logger, "Error parsing mod file: " + filename+ "\n"+ex.toString(),
                                             null, null);
                    cancelled = true;
                }
            }
        }
        else
        {
            Object obj = jComboBoxTemplates.getSelectedItem();
            if (obj instanceof String)
            {
                chosenInBuiltModFile = (String)obj;
            }
            else
            {
                ModFile selection = (ModFile)obj;
                logger.logComment("Selected mod file: " + selection);
                chosenModFile = selection;
            }
        }
        this.dispose();
    }

    void jComboBoxTemplates_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()!=ItemEvent.SELECTED) return;

        logger.logComment("Selection changed...");
        if (e.getItem().equals(firstLineCombo)) return;

        String proposedNewName = null;

        ModFile selection = (ModFile) e.getItem();
        proposedNewName = selection.myNeuronElement.getProcessName();
        logger.logComment("Selection: " + selection);
        jTextFieldNewMechanismName.setEnabled(true);
        if (jTextFieldNewMechanismName.getText().equals(""))
        {
            jTextFieldNewMechanismName.setText(proposedNewName);
        }

    }

    void jButtonSelectFile_actionPerformed(ActionEvent e)
    {
        //String selection = (String)jComboBoxSynapseTemplates.getSelectedItem();
        //logger.logComment("Getting the mod file info for "+ selection);


        String suggestedFileName = this.jTextFieldFileName.getText();

        String shortFileName = null;

        Frame frame = (Frame)myParent;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        SimpleFileFilter fileFilter
            = new SimpleFileFilter(new String[]{".mod"},
                                   "NMODL files. Extension: *.mod");



        chooser.setFileFilter(fileFilter);

        try
        {
            File defaultDir = null;

            if (suggestedFileName == null || suggestedFileName.length() < 3)
            {
                defaultDir = ProjectStructure.getnCProjectsDirectory();
            }
            else
            {
                File suggestDir = new File(suggestedFileName);
                defaultDir = suggestDir.getParentFile();
                if (defaultDir == null || ! (defaultDir.isDirectory()))
                {
                    defaultDir = ProjectStructure.getnCProjectsDirectory();
                }
            }
            chooser.setCurrentDirectory(defaultDir);
            logger.logComment("Set Dialog dir to: " + defaultDir.getAbsolutePath());
        }
        catch (Exception ex)
        {
            logger.logError("Problem with default dir setting: " +
                            ProjectStructure.getnCProjectsDirectory(), ex);
        }

        int retval = chooser.showDialog(frame, null);

        if (retval == JFileChooser.APPROVE_OPTION)
        {
            logger.logComment("User approved...");
            selectedFileName = chooser.getSelectedFile().getAbsolutePath();
            shortFileName = chooser.getSelectedFile().getName();
            this.jTextFieldFileName.setText(selectedFileName);

            if (jTextFieldNewMechanismName.getText().equals(""))
            {
                String proposedNewName = shortFileName.substring(0,shortFileName.lastIndexOf("."));
                jTextFieldNewMechanismName.setText(proposedNewName);
            }
        }


    }


    public String getNewSynapseName()
    {
        return newMechanismName;
    }

    public ModFile getChosenModFile()
    {
        return chosenModFile;
    }


    public String getChosenInBuiltModFile()
    {
        return chosenInBuiltModFile;
    }



}
