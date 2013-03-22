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

package ucl.physiol.neuroconstruct.hpc.condor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.hpc.utils.*;

/**
 * Support for sending jobs to Condor platform
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class CondorFrame extends JFrame implements ProcessFeedback
{
    ClassLogger logger = new ClassLogger("CondorFrame");

    boolean standalone = true;

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
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
    BorderLayout borderLayout4 = new BorderLayout();
    JButton jButtonHistory = new JButton();
    JButton jButtonRemoveAll = new JButton();
    JButton jButtonStatus = new JButton();

    JPanel jPanelLastCommand = new JPanel();
    JLabel jLabelLastCommand = new JLabel("Last Command: ");
    JTextArea jTextAreaLastCommand = new JTextArea();

    //Construct the frame
    public CondorFrame(boolean standalone)
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.standalone = standalone;
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
        this.setTitle("Condor Monitor");
        jPanelMain.setLayout(borderLayout2);
        jPanelConsole.setBorder(border1);
        jPanelConsole.setLayout(borderLayout3);
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelButtons.setLayout(borderLayout4);
        borderLayout3.setHgap(10);
        borderLayout3.setVgap(10);
        jTextAreaMain.setFont(new java.awt.Font("Fixed Miriam Transparent", 0, 11));
        jTextAreaMain.setEditable(false);
        jTextAreaMain.setText("");
        jButtonCurrentJobs.setText("Current Queue");
        jButtonCurrentJobs.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCurrentJobs_actionPerformed(e);
            }
        });
        jButtonOther.setText("Other:");
        jButtonOther.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOther_actionPerformed(e);
            }
        });
        jTextFieldOther.setText("condor_restart");
        jTextFieldOther.setColumns(12);
        jTextFieldOther.setHorizontalAlignment(SwingConstants.TRAILING);
        jButtonHistory.setText("History");
        jButtonHistory.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonHistory_actionPerformed(e);
            }
        });
        jButtonRemoveAll.setText("Remove All Jobs");
        jButtonRemoveAll.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRemoveAll_actionPerformed(e);
            }
        });
        jButtonStatus.setText("Status");
        jButtonStatus.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonStatus_actionPerformed(e);
            }
        });
        jPanelButtons.add(jPanelOther,  BorderLayout.SOUTH);
        jPanelOther.add(jButtonOther, null);
        jPanelOther.add(jTextFieldOther, null);
        jPanelButtons.add(jPanelMainButtons,  BorderLayout.CENTER);
        jPanelMainButtons.add(jButtonCurrentJobs, null);
        jPanelMainButtons.add(jButtonStatus, null);
        jPanelMainButtons.add(jButtonHistory, null);
        jPanelMainButtons.add(jButtonRemoveAll, null);
        contentPane.add(jPanelMain, BorderLayout.CENTER);

        jTextAreaLastCommand.setEditable(false);
        jTextAreaLastCommand.setBorder(BorderFactory.createEtchedBorder());

        jTextAreaLastCommand.setColumns(40);

        jPanelLastCommand.add(jLabelLastCommand);
        jPanelLastCommand.add(jTextAreaLastCommand);


        jPanelMain.add(jPanelLastCommand,  BorderLayout.NORTH);


        jPanelMain.add(jPanelConsole,  BorderLayout.CENTER);


        jPanelConsole.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTextAreaMain, null);
        jPanelMain.add(jPanelButtons, BorderLayout.SOUTH);

        jTextAreaMain.setBackground(Color.black);
        jTextAreaMain.setForeground(Color.green);

        Font f = new Font("Monospaced", Font.PLAIN, 14);
        jTextAreaMain.setFont(f);


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



    private void runCommand(String command)
    {
        this.jTextAreaLastCommand.setText(command);
        jTextAreaMain.setText("");

        try
        {
            ProcessManager.runCommand(command, this, 2000);
        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error running the command: " + command
                                      +
                "\nPlease ensure Condor is installed and your PATH includes executables condor_q, etc.",
                                      ex, null);
        }
    }



    void jButtonCurrentJobs_actionPerformed(ActionEvent e)
    {
        runCommand("condor_q -global");
    }

    void jButtonOther_actionPerformed(ActionEvent e)
    {
        runCommand(jTextFieldOther.getText());
    }

    void jButtonHistory_actionPerformed(ActionEvent e)
    {
        runCommand("condor_history");
    }

    void jButtonRemoveAll_actionPerformed(ActionEvent e)
    {
        runCommand("condor_rm -all");

    }

    void jButtonStatus_actionPerformed(ActionEvent e)
    {
        runCommand("condor_status");

    }
}
