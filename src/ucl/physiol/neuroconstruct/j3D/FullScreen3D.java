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

package ucl.physiol.neuroconstruct.j3D;

import javax.swing.*;
import java.awt.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.awt.event.*;

/**
 * Frame for displaying the 3D model at maximum size
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class FullScreen3D extends JFrame
{
    ClassLogger logger = new ClassLogger("FullScreen3D");

    boolean standalone = false;

    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMain = new JPanel();
    JButton jButtonReturn = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();


    public FullScreen3D(Base3DPanel panel3D, boolean standalone)
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.standalone = standalone;
        try
        {
            jbInit();
            //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            jPanelMain.add("Center", panel3D);

            setLocation(0,0);
            //setSize(screenSize.width);
            this.setExtendedState(MAXIMIZED_BOTH);
            setVisible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    private void jbInit() throws Exception
    {
        jButtonReturn.setFont(new java.awt.Font("SansSerif", 0, 10));
        jButtonReturn.setHorizontalAlignment(SwingConstants.RIGHT);
        jButtonReturn.setHorizontalTextPosition(SwingConstants.RIGHT);
        jButtonReturn.setMargin(new Insets(0, 0, 0, 0));
        jButtonReturn.setText("Return to full GUI");
        jButtonReturn.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonReturn.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonReturn.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonReturn_actionPerformed(e);
            }
        });
        jPanelButtons.setLayout(borderLayout1);
        jPanelMain.setBorder(BorderFactory.createLoweredBevelBorder());
        this.getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jButtonReturn,  BorderLayout.EAST);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);

        jPanelMain.setLayout(new BorderLayout());
    }


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (standalone && e.getID() == WindowEvent.WINDOW_CLOSING) System.exit(0);
    }


    public static void main(String[] args)
    {
        try
         {

             SimpleCell cell = new SimpleCell();

             System.out.println("Creating 3D representation of cell: " + cell.getClass().getName());

             Project dummyProj = Project.createNewProject("temp", "TempProj", null);

             OneCell3DPanel viewer = new OneCell3DPanel(cell, dummyProj, null);

             new FullScreen3D(viewer, true);

             //frame.add("Center", viewer);
             //frame.pack();
             //frame.setSize(1000, 800);
             //frame.show();

             //System.out.println("Cell details: " + CellTopologyHelper.printDetails(cell));
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
         }

    }

    void jButtonReturn_actionPerformed(ActionEvent e)
    {
        if (standalone) System.exit(0);
    }
}
