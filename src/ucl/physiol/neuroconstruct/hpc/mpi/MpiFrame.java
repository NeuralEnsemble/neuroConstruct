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

package ucl.physiol.neuroconstruct.hpc.mpi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.hpc.utils.*;


/**
 * Support for interacting with MPI platform
 *
 *  *** STILL IN DEVELOPMENT! SUBJECT TO CHANGE WITHOUT NOTICE! ***
 *
 * @author Padraig Gleeson
 *
 */



@SuppressWarnings("serial")

public class MpiFrame extends JFrame implements ProcessFeedback
{
    ClassLogger logger = new ClassLogger("MpiFrame");

    boolean standalone = true;

    File workingDir = null;

    String selectPrev = "-- Previous --";

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMainSettings = new JPanel();
    JPanel jPanelMainScripting = new JPanel();
    JPanel jPanelConsole = new JPanel();
    JPanel jPanelButtons = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea jTextAreaMain = new JTextArea();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border1;
    JButton jButtonCurrentJobs = new JButton();
    JButton jButtonOther = new JButton();
    JTextField jTextFieldOther = new JTextField();
    JPanel jPanelMainButtons = new JPanel();
    JPanel jPanelOther = new JPanel();
    JPanel jPanelDD = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JButton jButtonHistory = new JButton();
    JButton jButtonRemoveAll = new JButton();
    JButton jButtonStatus = new JButton();

    JPanel jPanelLastCommand = new JPanel();
    JLabel jLabelLastCommand = new JLabel("Last Command: ");
    JLabel jLabelRetVal = new JLabel("Return value: ");
    JTextArea jTextAreaLastCommand = new JTextArea();
    JTextArea jTextAreaRetVal = new JTextArea();

    JComboBox jComboBoxPrevComms = new JComboBox();

    //Construct the frame
    public MpiFrame(File workingDir, boolean standalone)
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.standalone = standalone;
        this.workingDir = workingDir;
        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //Component initialization
    private void jbInit() throws Exception
    {
        contentPane = (JPanel) this.getContentPane();
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),BorderFactory.createEmptyBorder(6,6,6,6));
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(750, 450));
        this.setTitle("MPI Monitor");
        jPanelMainScripting.setLayout(borderLayout2);
        jPanelMainSettings.setLayout(new BorderLayout());
        jPanelConsole.setBorder(border1);
        jPanelConsole.setLayout(borderLayout3);
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelButtons.setLayout(borderLayout4);
        borderLayout3.setHgap(10);
        borderLayout3.setVgap(10);
        jTextAreaMain.setFont(new java.awt.Font("Fixed Miriam Transparent", 0, 11));
        jTextAreaMain.setEditable(false);
        jTextAreaMain.setText("");


        jButtonOther.setText("Execute:");
        jButtonOther.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOther_actionPerformed(e);
            }
        });

        jTextFieldOther.setText("pwd");
        jTextFieldOther.setColumns(46);
        jTextFieldOther.setHorizontalAlignment(SwingConstants.TRAILING);



        jButtonStatus.setText("Status");
        jButtonStatus.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStatus_actionPerformed(e);
            }
        });


        jPanelButtons.add(jPanelOther,  BorderLayout.CENTER);
        jPanelButtons.add(jPanelDD,  BorderLayout.SOUTH);
        jPanelOther.add(jButtonOther, null);
        jPanelOther.add(jTextFieldOther, null);
        jPanelButtons.add(jPanelMainButtons,  BorderLayout.NORTH);

        ///////jPanelMainButtons.add(jButtonCurrentJobs, null);
        jPanelMainButtons.add(jButtonStatus, null);
        ////////jPanelMainButtons.add(jButtonHistory, null);
        /////////jPanelMainButtons.add(jButtonRemoveAll, null);

        contentPane.add(jPanelMainSettings, BorderLayout.NORTH);
        contentPane.add(jPanelMainScripting, BorderLayout.CENTER);

        jTextAreaLastCommand.setEditable(false);
        jTextAreaRetVal.setEditable(false);
        jTextAreaLastCommand.setBorder(BorderFactory.createEtchedBorder());

        jTextAreaLastCommand.setColumns(30);
        jTextAreaRetVal.setColumns(10);

        jPanelLastCommand.add(jLabelLastCommand);
        jPanelLastCommand.add(jTextAreaLastCommand);
        jPanelLastCommand.add(jLabelRetVal);
        jPanelLastCommand.add(jTextAreaRetVal);



        jPanelMainScripting.add(jPanelLastCommand,  BorderLayout.NORTH);


        jPanelMainScripting.add(jPanelConsole,  BorderLayout.CENTER);


        jPanelConsole.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTextAreaMain, null);
        jPanelMainScripting.add(jPanelButtons, BorderLayout.SOUTH);

        jTextAreaMain.setBackground(Color.black);
        jTextAreaMain.setForeground(Color.green);

        Font f = new Font("Monospaced", Font.PLAIN, 14);
        jTextAreaMain.setFont(f);

        jComboBoxPrevComms.addItem(selectPrev);
        jPanelDD.add(jComboBoxPrevComms);

        jComboBoxPrevComms.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxPrevComms_actionPerformed(e);
            }
        });





    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            if (standalone) System.exit(0);
            else this.dispose();
        }
    }

    private void runCommand(String command)
    {
        this.jTextAreaLastCommand.setText(command);
        jTextAreaMain.setText("");

        try
        {
            String retVal = ProcessManager.runCommand(command, this, this.workingDir, 2000);
            this.jTextAreaRetVal.setText(retVal);
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error running the command: " + command
                                      + "\nPlease ensure MPI is installed and your PATH includes executables mpdtrace, etc.",
                                      ex, null);


            this.jTextAreaRetVal.setText("???");
        }
    }

    public void comment(String comment)
    {
        jTextAreaMain.setForeground(Color.green);
        this.jTextAreaMain.setText(comment);
    }

    public void error(String comment)
    {
        jTextAreaMain.setForeground(Color.red);
        this.jTextAreaMain.setText(comment);
    }

    void jComboBoxPrevComms_actionPerformed(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            Object sel = jComboBoxPrevComms.getSelectedItem();
            if (sel.equals(selectPrev))return;
            this.jTextFieldOther.setText((String)sel);
            //jComboBoxPrevComms.setSelectedItem(selectPrev);
            //runCommand( (String) sel);
        }
    }



    void jButtonOther_actionPerformed(ActionEvent e)
    {
        jComboBoxPrevComms.addItem(jTextFieldOther.getText());
        runCommand(jTextFieldOther.getText());
    }

/*
    void jButtonCurrentJobs_actionPerformed(ActionEvent e)
    {
        runCommand("condor_q -global");
    }


    void jButtonHistory_actionPerformed(ActionEvent e)
    {
        runCommand("condor_history");
    }

    void jButtonRemoveAll_actionPerformed(ActionEvent e)
    {
        runCommand("condor_rm -all");

    }
*/
    void jButtonStatus_actionPerformed(ActionEvent e)
    {
        runCommand("tstmachines -v");

    }
}
