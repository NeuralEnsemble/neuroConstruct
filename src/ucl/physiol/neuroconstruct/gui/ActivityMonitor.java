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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Frame for showing activity in cell groups
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class ActivityMonitor extends JFrame
{
    ClassLogger logger = new ClassLogger("ActivityMonitor");

    int maximum = 0;

    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelShow = new JPanel();
    JButton jButtonClose = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();
    JProgressBar jProgressBar1 = new JProgressBar();
    JLabel jLabelNumPercentage = new JLabel();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    JLabel jLabelPercentage = new JLabel();
    JLabel jLabelNumActiveCells = new JLabel();
    JLabel jLabelActive = new JLabel();
    JLabel jLabelNumNumCells = new JLabel();
    JLabel jLabelNum = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    GridLayout gridLayout1 = new GridLayout();
    Border border1;
    JPanel jPanel3 = new JPanel();
    JLabel jLabelCellGroup = new JLabel();


    public ActivityMonitor(String cellGroupName, int maximum)
    {
        logger.logComment("Creating Activity monitor for cell group: "
                          + cellGroupName
                          + ", with "
                          + maximum
                          + " cells");
        try
        {
            jbInit();

            this.jLabelCellGroup.setText(cellGroupName);
            this.jProgressBar1.setMaximum(maximum);
            this.maximum = maximum;
            this.jLabelNumNumCells.setText(""+maximum);
        }
        catch(Exception e)
        {
            logger.logComment("Exception starting GUI: "+ e);
        }

    }

    public void setValue(int value)
    {
        this.jProgressBar1.setValue(value);


            this.jLabelNumActiveCells.setText(""+value);

            double percentage = ((double)value/(double)maximum)*100d;
            this.jLabelNumPercentage.setText(Utils3D.trimDouble(percentage, 2));
    }


    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),BorderFactory.createEmptyBorder(1,1,1,1));
        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonClose_actionPerformed(e);
            }
        });
        jPanelShow.setLayout(borderLayout2);
        jPanelMain.setLayout(borderLayout1);
        jProgressBar1.setOrientation(JProgressBar.VERTICAL);
        jProgressBar1.setValue(0);
        jLabelNumPercentage.setVerifyInputWhenFocusTarget(true);
        jLabelNumPercentage.setText("0");
        jLabelPercentage.setText("  %:");
        jLabelNumActiveCells.setText("0");
        jLabelActive.setText("  Active:");
        jLabelNumNumCells.setText("...");
        jLabelNum.setText("  Total:");
        jPanel2.setLayout(gridLayout1);
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(7);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(3);
        jPanel2.setBorder(border1);
        jPanelShow.setBorder(null);
        jPanel3.setBorder(BorderFactory.createEtchedBorder());
        jLabelCellGroup.setText("...");
        jPanel2.add(jLabelNum, null);
        jPanel2.add(jLabelNumNumCells, null);
        jPanel2.add(jLabelActive, null);
        jPanel2.add(jLabelNumActiveCells, null);
        jPanel2.add(jLabelPercentage, null);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelMain.add(jPanelShow,  BorderLayout.CENTER);
        jPanelShow.add(jPanel2,  BorderLayout.SOUTH);
        jPanel2.add(jLabelNumPercentage, null);
        jPanelShow.add(jPanel1,  BorderLayout.CENTER);
        jPanel1.add(jProgressBar1, null);
        jPanelMain.add(jPanel3, BorderLayout.NORTH);
        jPanel3.add(jLabelCellGroup, null);
        jPanelButtons.add(jButtonClose, null);

    }




    public static  void main(String[] args)
    {

        ActivityMonitor am = new ActivityMonitor("Cell Group 1", 50);
        am.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = am.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        am.setLocation( (screenSize.width - frameSize.width) / 2,
                             (screenSize.height - frameSize.height) / 2);

        am.setVisible(true);

        am.setValue(21);

    }

    void jButtonClose_actionPerformed(ActionEvent e)
    {
        this.dispose();
    }


    public void dispose()
    {
        super.dispose();
    }


}
