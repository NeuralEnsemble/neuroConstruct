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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.Vector;
import javax.swing.*;
import ucl.physiol.neuroconstruct.simulation.SimulationsInfo;
import ucl.physiol.neuroconstruct.simulation.SimulationTree;


/**
 * Frame for viewing simulation results in tree
 *
 * @author Padraig Gleeson
 *
 */

@SuppressWarnings("serial")
public class SimulationTreeFrame extends JFrame 
{
    private JButton jButtonOK;
    private JButton jButtonRefresh;
    private JPanel jPanelMain;
    private JPanel jPanelButtons;
    private JPanel jPanelScrool;

    private JScrollPane jScrollPane1;

    private SimulationTree tree;

    boolean standalone = false;

    SimulationsInfo simInfo = null;


    public SimulationTreeFrame()
    {
        this(false);
    }
    public SimulationTreeFrame(boolean standalone)
    {
        Vector<String> cols = new Vector<String>();
        this.standalone = standalone;
        File f = null;
        f = new File("C:\\nC_projects\\Gran2\\simulations");
        //f = new File("C:\\neuroConstruct\\nCmodels\\Thalamocortical\\simulations");
        simInfo = new SimulationsInfo(f, cols);

        initComponents();
    }

    private void initComponents() {

        jPanelMain = new javax.swing.JPanel();
        jPanelButtons = new javax.swing.JPanel();
        jPanelScrool = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tree = new SimulationTree(simInfo);
        jButtonOK = new javax.swing.JButton("OK");
        jButtonRefresh = new javax.swing.JButton("Refresh");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelScrool.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jScrollPane1.setViewportView(tree);


        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.setLayout(new BorderLayout());
        jPanelScrool.setLayout(new BorderLayout());
        jPanelMain.add(jPanelScrool, BorderLayout.CENTER);
        jPanelMain.add(jPanelButtons, BorderLayout.SOUTH);

        jPanelButtons.add(jButtonOK);
        jPanelButtons.add(jButtonRefresh);

        jPanelScrool.add(jScrollPane1, BorderLayout.CENTER);

        Dimension d = new Dimension(600, 400);
        jPanelScrool.setPreferredSize(d);
        //jPanelScrool.setPreferredSize(d);

        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                dispose();
                if (standalone)
                {
                    System.exit(0);
                }
            }
        });

        jButtonRefresh.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                simInfo.refresh(false);
                jPanelScrool.repaint();
            }
        });

        pack();
    }

    public static void main(String args[])
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        { }

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new SimulationTreeFrame(true).setVisible(true);
            }
        });
    }

}
