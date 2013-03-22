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
import javax.swing.*;

import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Dialog for New Cell Type
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class NewCellTypeDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("NewCellTypeDialog");
    public boolean createCancelled = false;

    private Cell cellChosen = null;
    private String initiallySuggestedName = null;

    RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());


    JPanel jPanel1 = new JPanel();
    JLabel jLabelMain = new JLabel();
    JComboBox jComboBoxCellTypes = new JComboBox();
    JButton jButtonCreate = new JButton();
    JPanel jPanel2 = new JPanel();
    JButton jButtonCancel = new JButton();
    JLabel jLabelName = new JLabel();
    JTextField jTextFieldNewCellName = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabelFileName = new JLabel();
    JTextField jTextFieldFileName = new JTextField();
    JButton jButtonSelectFile = new JButton();

    Frame myParent = null;

    private String selectedFileName = null;
    private Project project = null;

    private NewCellTypeDialog()
    {

    }



    public NewCellTypeDialog(Frame owner,
                             String title,
                             String newCellTypeProposedName,
                             Project project) throws HeadlessException
    {
        super(owner, title, false);
        myParent = owner;
        this.project = project;

        try
        {
            initiallySuggestedName = newCellTypeProposedName;
            jbInit();
            extraInit();
            pack();

            //this.setf
        }
        catch(Exception ex)
        {
            logger.logError("Exception starting GUI", ex);
        }

    }
    private void jbInit() throws Exception
    {
        jLabelMain.setText("Please select the base Cell type:");
        jButtonCreate.setText("Create");
        jButtonCreate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCreate_actionPerformed(e);
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
        jLabelName.setText("Name of New Cell Type:");
        jTextFieldNewCellName.setText(initiallySuggestedName);
        jTextFieldNewCellName.setColumns(20);
        jComboBoxCellTypes.setToolTipText("");
        jComboBoxCellTypes.setSelectedItem(this);
        jComboBoxCellTypes.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxCellTypes_itemStateChanged(e);
            }
        });
        jPanel1.setLayout(gridBagLayout1);
        jLabelFileName.setEnabled(false);
        jLabelFileName.setText("Morphology File:");
        jTextFieldFileName.setEnabled(false);
        jTextFieldFileName.setEditable(false);
        jTextFieldFileName.setText("");
        jTextFieldFileName.setColumns(36);
        jButtonSelectFile.setEnabled(false);
        jButtonSelectFile.setText("...");
        jButtonSelectFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSelectFile_actionPerformed(e);
            }
        });
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(jLabelMain,       new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 20), 0, 0));
        this.getContentPane().add(jPanel2,  BorderLayout.SOUTH);
        jPanel2.add(jButtonCreate, null);
        jPanel2.add(jButtonCancel, null);
        jPanel1.add(jLabelName,           new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 0));
        jPanel1.add(jTextFieldNewCellName,            new GridBagConstraints(1, 2, 2, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 12, 20), 0, 0));
        jPanel1.add(jComboBoxCellTypes,         new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 12, 20), 0, 0));
        jPanel1.add(jLabelFileName,      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 20, 12, 0), 0, 0));
        jPanel1.add(jTextFieldFileName,              new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 12, 0), 0, 0));
        jPanel1.add(jButtonSelectFile,        new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 12, 0, 20), 0, 0));


    }

    /**
     * Extra initiation stuff, not automatically added by IDE
     */
    private void extraInit()
    {
        Enumeration allCellTypeNames = CellTypeHelper.getAllCellTypeNames();

        while (allCellTypeNames.hasMoreElements())
        {

            jComboBoxCellTypes.addItem(allCellTypeNames.nextElement());

        }


    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        createCancelled = true;
        this.dispose();
    }

    void jButtonCreate_actionPerformed(ActionEvent e)
    {

        logger.logComment("Create button pressed");
        String newCellName = this.jTextFieldNewCellName.getText();

        if (newCellName.equals(project.getProjectName()))
        {
            GuiUtils.showErrorMessage(logger, newCellName + " is the name of the project. Please give the Cell Type another name.\n(Use of this name would cause conflicts in the hoc file generation)", null, this);
            return;
        }

        ArrayList<String> cellTypeNames = project.cellManager.getAllCellTypeNames();
        for (int i = 0; i < cellTypeNames.size(); i++)    {
            if (newCellName.equals(cellTypeNames.get(i)))
          {
              GuiUtils.showErrorMessage(logger, newCellName + " is already used for a Cell Type. Please give the Cell Type another name.", null, this);
              return;
          }

        }


        if (newCellName.indexOf(" ")>0 || !newCellName.trim().equals(newCellName))
        {
            GuiUtils.showErrorMessage(logger, "Please choose a Cell Type name without spaces", null, this);
            return;
        }

        try
        {
            Float.parseFloat(newCellName);
            GuiUtils.showErrorMessage(logger, "Please don't use a number for the name", null, this);
            return;
        }
        catch (NumberFormatException ex){};


        Object chosen = this.jComboBoxCellTypes.getSelectedItem();



        logger.logComment("Chosen cell type: ("+ chosen+")");

        if (chosen instanceof FormatImporter)
        {
            if (jTextFieldFileName.getText().trim().length()==0)
            {
                GuiUtils.showErrorMessage(logger, "Please select a valid morphology file", null, this);
                return;
            }
            FormatImporter importer = (FormatImporter)chosen;

            try
            {
                File originalFile = new File(jTextFieldFileName.getText());


                File dirToCopyTo = ProjectStructure.getImportedMorphologiesDir(project.getProjectMainDirectory(), true);

                File fileInProject = GeneralUtils.copyFileIntoDir(originalFile, dirToCopyTo);

                if (importer instanceof NeurolucidaReader)
                {
                    NeurolucidaReader nlReader = (NeurolucidaReader)importer;

                    NeurolucidaImportOptions dlg = new NeurolucidaImportOptions(new Frame(), "Neurolucida import options", true);

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
                    
                    if (dlg.cancelled)
                    {
                        this.dispose();
                        createCancelled = true;
                        return;
                    }

                    nlReader.includeSomaOutline(dlg.includeSomaOutline());
                    nlReader.daughtersInherit(dlg.daughtersInherit());
                }
                else if (importer instanceof SWCMorphReader)
                {
                    SWCMorphReader swcReader = (SWCMorphReader)importer;

                    SwcImportOptions dlg = new SwcImportOptions(new Frame(), "SWC/CVapp import options", true);

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

                    if (dlg.cancelled)
                    {
                        this.dispose();
                        createCancelled = true;
                        return;
                    }

                    //swcReader.includeSomaOutline(dlg.includeSomaOutline());
                    swcReader.daughtersInherit(dlg.daughtersInherit());
                    swcReader.includeAnatFeatures(dlg.includeAnatFeatures());
                }


                cellChosen = importer.loadFromMorphologyFile(fileInProject,
                                                             jTextFieldNewCellName.getText());
                
                String warning = importer.getWarnings();
                if (warning!=null && warning.length()>0)
                {
                    GuiUtils.showWarningMessage(logger, warning, this);
                }
            }
            catch (Exception ex1)
            {
                GuiUtils.showErrorMessage(logger, "Problem creating that cell: "+ex1.getMessage(), ex1, this);

            }

        }
        else
        {

            cellChosen = CellTypeHelper.getCell( (String) chosen, this.jTextFieldNewCellName.getText());


        }


        this.dispose();

    }


    public boolean wasBasedOnMorphML()
    {
        return jComboBoxCellTypes.getSelectedItem() instanceof MorphMLConverter;
    }


    void jComboBoxCellTypes_itemStateChanged(ItemEvent e)
    {
        logger.logComment("Selection changed ");

        Object chosen = this.jComboBoxCellTypes.getSelectedItem();

        if (chosen instanceof FormatImporter)
        {
            logger.logComment("It's a morphology file...");
            this.jLabelFileName.setEnabled(true);
            this.jButtonSelectFile.setEnabled(true);
            this.jTextFieldFileName.setEditable(true);
            this.jTextFieldFileName.setEnabled(true);
        }
        else
        {
            logger.logComment("It's not a morphology file...");
            this.jLabelFileName.setEnabled(false);
            this.jButtonSelectFile.setEnabled(false);
            this.jTextFieldFileName.setEditable(false);
            this.jTextFieldFileName.setEnabled(false);
        }

    }

    void jButtonSelectFile_actionPerformed(ActionEvent e)
    {

        String morphologyDir = recentFiles.getMyLastMorphologiesDir();

        if (morphologyDir==null) morphologyDir =(new File(".")).getAbsolutePath(); // pwd...


        Object chosen = this.jComboBoxCellTypes.getSelectedItem();


        logger.logComment("Getting the morph file info for "+ chosen);
        String suggestedFileName = this.jTextFieldFileName.getText();

        if (chosen instanceof FormatImporter)
        {
            FormatImporter formatImp = (FormatImporter)chosen;
            logger.logComment("It's got a morphology file...");

            Frame frame = (Frame)myParent;

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);

            String[] exts = formatImp.getFileExtensions();

            StringBuffer sb = new StringBuffer("Morphology files, ");

            if (exts.length==1) sb.append("extension: "+ exts[0]);
            else
            {
                sb.append("extensions: ");
                for (int i = 0; i < exts.length; i++)
                {
                    if (i > 0) sb.append(", ");
                    sb.append("*" + exts[i]);
                }
            }

            SimpleFileFilter fileFilter = new SimpleFileFilter(exts, sb.toString());

            chooser.setFileFilter(fileFilter);

            File defaultDir = null;

            try
            {

                if ((suggestedFileName == null || suggestedFileName.length()<3) &&
                   morphologyDir!=null )
                {
                    logger.logComment("Using recent morph file location: "+morphologyDir);
                    defaultDir = new File(morphologyDir);
                }
                else
                {
                    logger.logComment("Using default morph file location");
                    File suggestDir = new File(suggestedFileName);
                    defaultDir = suggestDir.getParentFile();

                    if (defaultDir==null|| !(defaultDir.isDirectory()))
                    {
                        defaultDir = new File(System.getProperty("user.home"));
                    }
                }
                chooser.setCurrentDirectory(defaultDir);
                logger.logComment("Set Dialog dir to: " + defaultDir.getAbsolutePath());
            }
            catch (Exception ex)
            {
                logger.logError("Problem with default dir setting: " + defaultDir, ex);
            }

            //ProjectFileFilter fileFilter = new ProjectFileFilter();
            //chooser.setFileFilter(fileFilter);

            int retval = chooser.showDialog(frame, null);

            if (retval == JFileChooser.APPROVE_OPTION)
            {
                logger.logComment("User approved...");
                selectedFileName = chooser.getSelectedFile().getAbsolutePath();


                recentFiles.setMyLastMorphologiesDir(new String(chooser.getSelectedFile().getParent()));

                //System.out.println("Morph dir1: "+ recentFiles.getMyLastMorphologiesDir());

                this.jTextFieldFileName.setText(selectedFileName);

                if (jTextFieldNewCellName.getText().equals(initiallySuggestedName))
                {
                    String fileName = chooser.getSelectedFile().getName();

                    String newSuggestion
                        = fileName.substring(0,fileName.indexOf("."));
                    jTextFieldNewCellName.setText(newSuggestion);
                }
            }
        }
        else
        {
            logger.logError("Problem, "+chosen+ " is not based on a morphology file");
            return;
        }

    }

    public Cell getChosenCell()
    {
        return cellChosen;


    }

    public static void main(String[] args)
    {
        NewCellTypeDialog dlg
            = new NewCellTypeDialog(null,
                                    "New Cell Type",
                                    "CellType_0",
                                    null);

        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = dlg.getSize();
        Point loc = dlg.getLocation();
        dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                        (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);


        Cell newCell = dlg.getChosenCell();

        System.out.println("Cell created: "+ CellTopologyHelper.printShortDetails(newCell));

    }




}
