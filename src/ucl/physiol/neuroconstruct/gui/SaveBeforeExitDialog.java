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
import javax.swing.*;
import java.awt.event.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog to check before exit
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SaveBeforeExitDialog extends JDialog
{
    /** @todo remove this altogether and replace with JOptionPane.showOptionDialog() */

    ClassLogger logger = new ClassLogger("SaveBeforeExitDialog");

    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JLabel jLabelMain = new JLabel();
    JButton jButtonYes = new JButton();
    JButton jButtonNo = new JButton();
    JPanel jPanel2 = new JPanel();

    public boolean saveTheProject = false;
    public boolean cancelPressed = false;
    JButton jButtonCancel = new JButton();

    public SaveBeforeExitDialog(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
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

    public SaveBeforeExitDialog()
    {
        this(null, "", false);
    }
    private void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);
        jLabelMain.setText("Project has been altered. Save? ");
        jButtonYes.setText("Yes");
        jButtonYes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonYes_actionPerformed(e);
            }
        });
        jButtonNo.setText("No");
        jButtonNo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNo_actionPerformed(e);
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
        getContentPane().add(panel1);
        panel1.add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(jLabelMain, null);
        jPanel2.add(jButtonYes, null);
        jPanel2.add(jButtonNo, null);
        jPanel2.add(jButtonCancel, null);
        panel1.add(jPanel2,  BorderLayout.SOUTH);
    }

    void jButtonYes_actionPerformed(ActionEvent e)
    {
        logger.logComment("Yes button pressed");
        saveTheProject = true;

        this.dispose();

    }

    void jButtonNo_actionPerformed(ActionEvent e)
    {
        logger.logComment("No button pressed");
        saveTheProject = false;
        this.dispose();

    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelPressed = true;
        saveTheProject = false;
        this.dispose();

    }


}
