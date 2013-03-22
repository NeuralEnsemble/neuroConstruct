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

import javax.swing.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;

@SuppressWarnings("serial")

public class NeurolucidaImportOptions extends JDialog
{
    ClassLogger logger = new ClassLogger("NeurolucidaImportOptions");


    boolean cancelled = false;

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelSomaOutline = new JPanel();
    JCheckBox jCheckBoxSO = new JCheckBox();
    JTextArea jTextAreaSO = new JTextArea();
    JPanel jPanelDaughter = new JPanel();
    JCheckBox jCheckBoxDaughter = new JCheckBox();
    JTextArea jTextAreaDaughter = new JTextArea();
    BorderLayout borderLayout2 = new BorderLayout();
    JButton jButtonMore = new JButton();

    public NeurolucidaImportOptions(Frame frame, String title, boolean modal)
    {
        super(frame, title, modal);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            pack();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void jbInit() throws Exception
    {

        this.getContentPane().setLayout(borderLayout1); jButtonOK.setActionCommand("OK"); jButtonOK.addActionListener(new
            ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        }); jButtonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        

        jButtonMore.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonMore_actionPerformed(e);
            }
        });

        jButtonMore.setText("More on importing Neurolucida files");
        
        jButtonOK.setText("OK");
        jCheckBoxSO.setText("Include Soma Outline");
       // Button b = new Button();
        jTextAreaSO.setBackground(jCheckBoxSO.getBackground());
        jTextAreaSO.setMaximumSize(new Dimension(350, 30));
        jTextAreaSO.setMinimumSize(new Dimension(350, 30));
        jTextAreaSO.setPreferredSize(new Dimension(350, 45));

        jTextAreaSO.setText("If a trace outline of the cell body is present in the Neurolucida file, include " +
                       "this as a series of segments in a single section (can be removed later)");

        jTextAreaSO.setColumns(16);
        jTextAreaSO.setLineWrap(true);
        jTextAreaSO.setRows(4);
        jTextAreaSO.setWrapStyleWord(true);


        jTextAreaDaughter.setBackground(jCheckBoxSO.getBackground());
        jTextAreaDaughter.setMaximumSize(new Dimension(350, 30));
        jTextAreaDaughter.setMinimumSize(new Dimension(350, 30));
        jTextAreaDaughter.setPreferredSize(new Dimension(350, 45));

        jTextAreaDaughter.setText("If selected, daughter sections will start with the radius of the parent section at the connection point."
                                  +" Otherwise the daughter start radii at the connection points will be the same as the first full daughter point."
                                  +" The second option avoids large cone like initial segments on small side dendrites.");

        jTextAreaDaughter.setColumns(16);
        jTextAreaDaughter.setLineWrap(true);
        jTextAreaDaughter.setRows(6);
        jTextAreaDaughter.setWrapStyleWord(true);

        jTextAreaDaughter.setEditable(false);
        this.jTextAreaSO.setEditable(false);




        jCheckBoxDaughter.setText("Daughters inherit radii");
        jPanelMain.setLayout(borderLayout2);
        jPanelButtons.add(jButtonOK);
        jPanelButtons.add(jButtonMore);
        jPanelButtons.add(jButtonCancel);
        this.getContentPane().add(jPanelButtons, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER); jPanelDaughter.add(jCheckBoxDaughter);
        jPanelDaughter.add(jTextAreaDaughter);
        jPanelSomaOutline.add(jCheckBoxSO);
        jPanelSomaOutline.add(jTextAreaSO);
        jPanelMain.add(jPanelDaughter, java.awt.BorderLayout.CENTER);
        jPanelMain.add(jPanelSomaOutline, java.awt.BorderLayout.NORTH);
        jButtonCancel.setText("Cancel");

        this.jCheckBoxDaughter.setSelected(false);
        this.jCheckBoxSO.setSelected(true);
    }

    public static void main(String[] args)
    {
        // nio = new NeurolucidaImportOptions();
        NeurolucidaImportOptions dlg = new NeurolucidaImportOptions(new Frame(),"Neurolucida options", true);


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

    }

    public boolean includeSomaOutline()
    {
        return this.jCheckBoxSO.isSelected();
    }

    public boolean daughtersInherit()
    {
        return this.jCheckBoxDaughter.isSelected();
    }


    public void jButtonMore_actionPerformed(ActionEvent e)
    {
        logger.logComment("More pressed...");

        cancelled = true;
        this.dispose();

        File f = ProjectStructure.getHelpImportFile();
        
        try 
        {
            HelpFrame.showFrame(f.toURL(), "Importing morphologies", false);
        }
        catch (MalformedURLException m)
        {
            logger.logError("Error showing help file", m);
        }

    }


    public void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK pressed...");

        this.dispose();
    }

    public void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");

        cancelled = true;
        this.dispose();

    }

}
