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
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Dialog for starting new project
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class NewProjectDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("NewProjectDialog");

    String projFileName = null;
    String projDirName = null;

    JPanel panel1 = new JPanel();
    JLabel jLabelProjName = new JLabel();
    JTextField jTextFieldProjName = new JTextField();
    JTextField jTextFieldProjDir = new JTextField();
    JLabel jLabelProjDir = new JLabel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanelLabel = new JPanel();
    JPanel jPanel2 = new JPanel();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel jLabelMain = new JLabel();
    JLabel jLabelFullPath = new JLabel();
    JLabel jLabelFullPathDisplay = new JLabel();

    boolean cancelled = false;
    JButton jButtonChooseDir = new JButton();

    public NewProjectDialog(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

        doUpdateDisplay();
    }

    private NewProjectDialog()
    {
        //this(null, "", false);
    }
    private void jbInit() throws Exception
    {
        panel1.setLayout(gridBagLayout1);
        jLabelProjName.setText("Project Name:");
        jTextFieldProjName.setText("Project_1");
        jTextFieldProjName.addKeyListener(new java.awt.event.KeyAdapter()
        {

            @Override
            public void keyReleased(KeyEvent e)
            {
                jTextFieldProjName_keyReleased(e);
            }
        });
        jTextFieldProjDir.setText(GeneralProperties.getnCProjectsDir().getAbsolutePath());
        jTextFieldProjDir.addKeyListener(new java.awt.event.KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                jTextFieldProjDir_keyReleased(e);
            }
        });
        jLabelProjDir.setDisplayedMnemonic('0');
        jLabelProjDir.setText("Project Directory:");
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
        this.getContentPane().setLayout(borderLayout1);
        jLabelMain.setText("Please enter the name of the new Project");
        jLabelFullPath.setText("Main Project File:");
        jLabelFullPathDisplay.setText("...");
        panel1.setMaximumSize(new Dimension(600, 130));
        panel1.setMinimumSize(new Dimension(600, 130));
        panel1.setPreferredSize(new Dimension(600, 130));
        jButtonChooseDir.setText("...");
        jButtonChooseDir.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonChooseDir_actionPerformed(e);
            }
        });
        getContentPane().add(panel1, BorderLayout.CENTER);
        panel1.add(jLabelProjName,                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 75, 14));
        panel1.add(jTextFieldProjName,               new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 14), 86, 0));
        panel1.add(jLabelProjDir,     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        panel1.add(jTextFieldProjDir,          new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 6, 6), 0, 0));
        panel1.add(jLabelFullPath,      new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        this.getContentPane().add(jPanelLabel, BorderLayout.NORTH);
        jPanelLabel.add(jLabelMain, null);
        this.getContentPane().add(jPanel2,  BorderLayout.SOUTH);
        jPanel2.add(jButtonOK, null);
        jPanel2.add(jButtonCancel, null);
        panel1.add(jLabelFullPathDisplay,   new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel1.add(jButtonChooseDir,    new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 6, 6, 14), 0, 0));
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

    public String getProjFileName()
    {
        return this.projFileName;
    }

    public String getProjDirName()
    {
        return this.projDirName;
    }

    void doCancel()
    {
        logger.logComment("Actions for cancel...");

        projFileName = new String("");
        projDirName = new String("");
    }

    void doUpdateDisplay()
    {
        String projCurrDir = jTextFieldProjDir.getText();
        String projCurrName = jTextFieldProjName.getText();

        String fileSep = System.getProperty("file.separator");

        this.jLabelFullPathDisplay.setText(projCurrDir
                                           +fileSep
                                           +projCurrName
                                           +fileSep
                                           +projCurrName
                                           +ProjectStructure.getNewProjectFileExtension());

    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {

        projFileName = new String(this.jTextFieldProjName.getText());


        if (projFileName.indexOf(" ")>=0 ||
            projFileName.indexOf(".")>=0 ||
            projFileName.indexOf(",")>=0||
            projFileName.indexOf("*")>=0||
            projFileName.indexOf("&")>=0||
            projFileName.indexOf(";")>=0||
            projFileName.indexOf(":")>=0)
        {
            GuiUtils.showErrorMessage(logger, "Please enter project name without any spaces nor any of:\n, . * & ; :", null, null);
            return;
        }



        projDirName = new String(this.jTextFieldProjDir.getText());

        //logger.logComment("projDirName: "+projDirName, true);

        this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        cancelled = true;
        this.dispose();

    }


    void jTextFieldProjName_keyReleased(KeyEvent e)
    {
        doUpdateDisplay();
    }

    void jTextFieldProjDir_keyReleased(KeyEvent e)
    {
        doUpdateDisplay();
    }

    void jButtonChooseDir_actionPerformed(ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        try
        {
            File currentSuggestion = new File(jTextFieldProjDir.getText());
            chooser.setCurrentDirectory(currentSuggestion);
            logger.logComment("Set Dialog dir to: "+currentSuggestion.getAbsolutePath());
        }
        catch (Exception ex)
        {
            logger.logError("Problem with default dir setting");
            File defLocation = new File(System.getProperty("user.home"));
            chooser.setCurrentDirectory(defLocation);
        }

        int retval = chooser.showDialog(this, null);

        if (retval == JFileChooser.APPROVE_OPTION )
        {
            logger.logComment("User approved...");
            String dirName = chooser.getSelectedFile().getAbsolutePath();
            jTextFieldProjDir.setText(dirName);
        }

        doUpdateDisplay();

    }

}
