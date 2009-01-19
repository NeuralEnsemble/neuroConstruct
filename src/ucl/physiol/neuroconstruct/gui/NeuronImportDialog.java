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
 * Dialog for options when importing NEURON hoc/nrn files
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class NeuronImportDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("NeuronImportDialog");

    //public  boolean cancelled = false;

    JPanel panel1 = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanelLabel = new JPanel();
    JPanel jPanel2 = new JPanel();
    JButton jButtonOK = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel jLabelMain = new JLabel();
    JCheckBox jCheckBoxMoveSomaToOrigin = new JCheckBox();
    JCheckBox jCheckBoxMoveDends = new JCheckBox();
    JTextArea jTextAreaMoveDends = new JTextArea();
    JTextArea jTextAreaMoveToOrigin1 = new JTextArea();

    public NeuronImportDialog(Frame frame)
    {
        super(frame, "NEURON import options", false);
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
    }

    private NeuronImportDialog()
    {
        //this(null, "", false);
    }
    private void jbInit() throws Exception
    {
        panel1.setLayout(gridBagLayout1);
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        });
        this.getContentPane().setLayout(borderLayout1);

        jCheckBoxMoveSomaToOrigin.setSelected(true);
        jCheckBoxMoveSomaToOrigin.setText("Translate start of Soma to origin");


        jCheckBoxMoveDends.setSelected(true);
        jCheckBoxMoveDends.setText("Translate dendritic sections to specified parents");
        jTextAreaMoveDends.setBackground(UIManager.getColor("Button.background"));
        jTextAreaMoveDends.setMaximumSize(new Dimension(260, 60));
        jTextAreaMoveDends.setMinimumSize(new Dimension(260, 60));
        jTextAreaMoveDends.setPreferredSize(new Dimension(260, 60));
        jTextAreaMoveDends.setToolTipText("");
        jTextAreaMoveDends.setVerifyInputWhenFocusTarget(true);
        jTextAreaMoveDends.setText("Translates the dendritic sections to connect to the points specified on their parents in the NEURON \"connect\" statement. This is similar to calling define_shape() in NEURON (Highly recommended, unless preserving " +
    "original 3D values in hoc/nrn files is a priority)");
        jTextAreaMoveDends.setColumns(20);
        jTextAreaMoveDends.setLineWrap(true);
        jTextAreaMoveDends.setRows(5);
        jTextAreaMoveDends.setWrapStyleWord(true);
        jTextAreaMoveToOrigin1.setWrapStyleWord(true);
        jTextAreaMoveToOrigin1.setRows(5);
        jTextAreaMoveToOrigin1.setLineWrap(true);
        jTextAreaMoveToOrigin1.setColumns(20);
        jTextAreaMoveToOrigin1.setText("Moves the first section of the soma to (0, 0, 0), and shifts all " +
    "other 3D points accordingly (Highly recommended, unless preserving " +
    "original 3D values in hoc/nrn files is a priority)");
        jTextAreaMoveToOrigin1.setVerifyInputWhenFocusTarget(true);
        jTextAreaMoveToOrigin1.setToolTipText("");
        jTextAreaMoveToOrigin1.setPreferredSize(new Dimension(330, 70));
        jTextAreaMoveToOrigin1.setMinimumSize(new Dimension(260, 60));
        jTextAreaMoveToOrigin1.setMaximumSize(new Dimension(260, 60));
        jTextAreaMoveToOrigin1.setBackground(UIManager.getColor("Button.background"));
        jTextAreaMoveToOrigin1.setBorder(null);
        getContentPane().add(panel1, BorderLayout.CENTER);
        panel1.add(jCheckBoxMoveSomaToOrigin,        new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        this.getContentPane().add(jPanelLabel, BorderLayout.NORTH);
        jPanelLabel.add(jLabelMain, null);
        this.getContentPane().add(jPanel2,  BorderLayout.SOUTH);
        jPanel2.add(jButtonOK, null);
        panel1.add(jCheckBoxMoveDends,        new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 0), 0, 0));
        panel1.add(jTextAreaMoveDends,                new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 81, 5));
        panel1.add(jTextAreaMoveToOrigin1,             new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 14, 6, 14), 0, -9));
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            this.dispose();
        }
        super.processWindowEvent(e);
    }
    //Close the dialog



    void jButtonOK_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK pressed...");



        this.dispose();
    }

    public boolean getMoveToOrigin()
    {
        return jCheckBoxMoveSomaToOrigin.isSelected();
    }

    public boolean getMoveDendrites()
    {
        return jCheckBoxMoveDends.isSelected();
    }



    public static void main(String[] args)
    {
        NeuronImportDialog dlg = new NeuronImportDialog(null);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dlg.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        dlg.setModal(true);

        dlg.setVisible(true);


    }

}
