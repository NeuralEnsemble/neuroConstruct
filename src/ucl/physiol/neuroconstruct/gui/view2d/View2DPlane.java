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

package ucl.physiol.neuroconstruct.gui.view2d;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Frame for showing 2D view of Cell Group(s)
 *
 * @author Padraig Gleeson
 *  
 */


public class View2DPlane extends JFrame
{
	private static final long serialVersionUID = -5927916856156987118L;


	ClassLogger logger = new ClassLogger("View2DPlane");


    boolean standAlone = true;

    ArrayList<PositionRecord> positions = null;

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelInfo = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMainCanvas = new JPanel();
    JButton jButtonClose = new JButton();
    JLabel jLabelMain = new JLabel();

    ViewCanvas viewCanvas = null;

    String cellGroup = null;

    BorderLayout borderLayout2 = new BorderLayout();
    Border border1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.white, Color.white,
                                                     new Color(103, 101, 98), new Color(148, 145, 140));
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    JLabel jLabelStatusBar = new JLabel();
    Border border2 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
        Color.white, Color.white, new Color(103, 101, 98), new Color(148, 145, 140)),
                                                        BorderFactory.createEmptyBorder(2, 2, 2, 2));
    JPanel jPanelMainLabel = new JPanel();
    JPanel jPanelComboBox = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JComboBox jComboBox1 = new JComboBox();

    JCheckBox jCheckBoxAxes = new JCheckBox();

    public View2DPlane(String cellGroup,
                  ArrayList<PositionRecord> positions,
                  ViewingDirection viewingDir,
                  boolean standAlone)
    {
        this.standAlone = standAlone;

        this.cellGroup = cellGroup;

        this.positions = positions;

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        jLabelMain.setText("2D View: "+ cellGroup);
        this.setTitle("2D View: "+ cellGroup);

        try
        {
            jbInit();
            extraInit();

            jComboBox1.setSelectedItem(viewingDir);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }


        this.viewCanvas.setup(positions, viewingDir);
    }

    private void jbInit() throws Exception
    {
        getContentPane().setLayout(borderLayout1); jButtonClose.setText("Close");
        jButtonClose.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonClose_actionPerformed(e);
            }
        });

        jPanelMainCanvas.setBorder(border2);
        jPanelMainCanvas.setPreferredSize(new Dimension(400, 400));
        jPanelMainCanvas.setLayout(borderLayout2);
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelInfo.setBorder(BorderFactory.createEtchedBorder());
        jPanelInfo.setLayout(borderLayout4);
        jPanelMain.setLayout(borderLayout3);
        jLabelStatusBar.setBorder(BorderFactory.createEtchedBorder());
        jLabelStatusBar.setText("...");

        jComboBox1.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBox1_itemStateChanged(e);
            }
        });
        jPanelMainLabel.add(jLabelMain);
        jPanelButtons.add(jButtonClose);
        jPanelMain.add(jPanelMainCanvas, java.awt.BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons, java.awt.BorderLayout.SOUTH);
        jPanelMain.add(jPanelInfo, java.awt.BorderLayout.NORTH);
        this.getContentPane().add(jLabelStatusBar, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(jPanelMain, java.awt.BorderLayout.CENTER);
        jPanelInfo.add(jPanelMainLabel, java.awt.BorderLayout.CENTER);
        jPanelInfo.add(jPanelComboBox, java.awt.BorderLayout.SOUTH);
        jPanelComboBox.add(jComboBox1);

        jCheckBoxAxes.setSelected(false);
        jCheckBoxAxes.setText("Show axes");


        jPanelComboBox.add(jCheckBoxAxes);

        jCheckBoxAxes.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (viewCanvas!=null) viewCanvas.repaint();
            }
        });



    }

    private void extraInit()
    {
        viewCanvas = new ViewCanvas(this);

        jPanelMainCanvas.add(viewCanvas, "Center");

        jComboBox1.addItem(ViewCanvas.X_Y_NEGZ_DIR );
        jComboBox1.addItem(ViewCanvas.X_Y_POSZ_DIR);
        jComboBox1.addItem(ViewCanvas.Y_Z_NEGX_DIR);
        jComboBox1.addItem(ViewCanvas.Y_Z_POSX_DIR);
        jComboBox1.addItem(ViewCanvas.Z_X_NEGY_DIR);
        jComboBox1.addItem(ViewCanvas.Z_X_POSY_DIR);

        jComboBox1.setLightWeightPopupEnabled(false);
    }

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            if (standAlone) System.exit(0);
        }
    }

    protected boolean showAxes()
    {
        return this.jCheckBoxAxes.isSelected();
    }



    public String getCellGroup()
    {
        return this.cellGroup;
    }

    protected void setStatusBarText(String text)
    {
        this.jLabelStatusBar.setText(text);
    }

/*
    private void setPositions(ArrayList<PositionRecord> positions)
    {
        this.viewCanvas.setPositions(positions);
    }

    public void updateVoltage(float voltage, String cellGroup, int cellNumber, boolean refresh)
    {
        this.updateVoltage(voltage, cellGroup, cellNumber, 0, refresh);
    }

    public void updateVoltage(float voltage, String cellGroup, int cellNumber, int segmentId, boolean refresh)
    {
        //System.out.println("updateVoltage: "+voltage+", "+cellGroup+", "+cellNumber+", "+segmentId);
        logger.logComment("updateVoltage: "+voltage+", "+cellGroup+", "+cellNumber+", "+segmentId);
        if (this.cellGroup.equals(cellGroup))
        viewCanvas.updateVoltage(voltage, cellGroup, cellNumber, segmentId, refresh);
    }
*/
    public static class ViewingDirection
    {
        String desc = null;

        private ViewingDirection(){};

        public ViewingDirection(String desc)
        {
            this.desc = desc;
        }

        public String toString()
        {
            return desc;
        }

        public boolean equals(Object obj)
        {
            if (!(obj instanceof ViewingDirection))
                return false;
            if (((ViewingDirection)obj).desc.equals(desc))
                return true;
            return false;
        }
    }

    public void setTolerance(int colorTolerance)
    {
        this.viewCanvas.colorTolerance = colorTolerance;
    }



    public void jButtonClose_actionPerformed(ActionEvent e)
    {
        this.dispose();
    }

    public void jComboBox1_itemStateChanged(ItemEvent e)
    {
        ViewingDirection vd = (ViewingDirection)jComboBox1.getSelectedItem();
        this.viewCanvas.setup(positions, vd);
        this.viewCanvas.repaint();
    }
}
