/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
 * @version 1.0.4
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
