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

package ucl.physiol.neuroconstruct.simulation;


import java.awt.event.*;
import java.util.ArrayList;
import java.util.Properties;
import java.io.*;
import javax.swing.*;

import javax.swing.tree.*;
import ucl.physiol.neuroconstruct.dataset.DataSet;
import ucl.physiol.neuroconstruct.gui.SimpleViewer;
import ucl.physiol.neuroconstruct.gui.SimulationTreeFrame;
import ucl.physiol.neuroconstruct.gui.plotter.PlotManager;
import ucl.physiol.neuroconstruct.gui.plotter.PlotterFrame;
import ucl.physiol.neuroconstruct.project.SimPlot;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Helper class for displaying multiple simulations in a JTree
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SimulationTree extends JTree implements ActionListener
{
    ClassLogger logger = new ClassLogger("SimulationTree");

    private JPopupMenu popupOneSim;
    private JPopupMenu popupCG;
    private JPopupMenu popupOneDataStore;

    private JMenuItem mi;

    private String PLOT = "Plot";

    private String COLOURED_BY_CELL_GROUP = "... coloured by Cell Group";

    private String PLOT_IN = "Plot in...";
    private String PLOT_ALL = "Plot all";
    private String PLOT_ALL_IN = "Plot all in...";
    private String PLOT_ALL_VOLTS = "Plot only voltage traces";
    private String PLOT_NON_VOLTS = "Plot all non voltage traces";
    
    private String PLOT_SEPARATE_CG_VAR_ALL = "Plot all in separate Plot Frames per Cell Group and data type";
    private String PLOT_SEPARATE_CG_VAR_SEL = "Plot selection in separate Plot Frames per Cell Group and data type";

    private String PLOT_SEPARATE_VAR = "Plot all in separate Plot Frames per data type";
    private String SIM_SUMMARY = "Simulation summary";
    private String SIM_INFO = "Simulation full information";

    public SimulationTree(SimulationsInfo stm)
    {
        super(stm);
        logger.logComment("New SimulationTree created");

        popupOneSim = new JPopupMenu();
        mi = new JMenuItem(PLOT_ALL);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(PLOT_ALL_IN);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(PLOT_SEPARATE_CG_VAR_ALL);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(PLOT_SEPARATE_CG_VAR_SEL);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(PLOT_ALL_VOLTS);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(PLOT_NON_VOLTS);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(SIM_SUMMARY);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        mi = new JMenuItem(SIM_INFO);
        mi.addActionListener(this);
        popupOneSim.add(mi);

        popupOneSim.setOpaque(true);
        popupOneSim.setLightWeightPopupEnabled(true);


        popupCG = new JPopupMenu();
        mi = new JMenuItem(PLOT_ALL);
        mi.addActionListener(this);
        popupCG.add(mi);

        mi = new JMenuItem(PLOT_ALL_IN);
        mi.addActionListener(this);
        popupCG.add(mi);


        mi = new JMenuItem(PLOT_ALL_VOLTS);
        mi.addActionListener(this);
        popupCG.add(mi);

        mi = new JMenuItem(PLOT_NON_VOLTS);
        mi.addActionListener(this);
        popupCG.add(mi);

        mi = new JMenuItem(PLOT_SEPARATE_VAR);
        mi.addActionListener(this);
        popupCG.add(mi);

        popupCG.setOpaque(true);
        popupCG.setLightWeightPopupEnabled(true);


        popupOneDataStore = new JPopupMenu();
        
        mi = new JMenuItem(PLOT);
        mi.addActionListener(this);
        popupOneDataStore.add(mi);
        
        mi = new JMenuItem(PLOT_IN);
        mi.addActionListener(this);
        popupOneDataStore.add(mi);


        popupOneDataStore.setOpaque(true);
        popupOneDataStore.setLightWeightPopupEnabled(true);

        addMouseListener(
            new MouseAdapter()
            {
                @Override
                public void mouseReleased( MouseEvent e )
                {
                    logger.logComment("Mouse event: "+ e);
                    
                    if ( e.getButton()==3 && getSelectionPath()!=null)
                    {
                        logger.logComment("Pop up due to: "+e.getSource());
                        logger.logComment("Sel: "+getSelectionPath());

                        Object comp = getSelectionPath().getLastPathComponent();
                        Object userObj = ((DefaultMutableTreeNode)comp).getUserObject();
                        logger.logComment("User obj ("+userObj.getClass()+"): "+userObj);
                        logger.logComment("Comp ("+comp.getClass()+"): "+comp);

                        if (comp instanceof SimulationsInfo.SimNode)
                        {
                            popupOneSim.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                        }
                        else if (comp instanceof SimulationsInfo.CellGroupNode)
                        {
                            popupCG.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                        }
                        else if (comp instanceof SimulationsInfo.CellNode)
                        {
                            popupCG.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                        }
                        else if (comp instanceof SimulationsInfo.DataStoreNode)
                        {
                            popupOneDataStore.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                        }
                    }
                }
            }
        );
        
    }

    public void actionPerformed(ActionEvent ae)
    {
        DefaultMutableTreeNode dmtn;

        TreePath path = this.getSelectionPath();
        Object comp = path.getLastPathComponent();
        dmtn = (DefaultMutableTreeNode) comp;

        logger.logComment("User obj: "+dmtn.getUserObject());
        logger.logComment("ae.getActionCommand(): "+ae.getActionCommand());

        if (comp instanceof SimulationsInfo.AllSimRoot)
        {
        }
        else if (comp instanceof SimulationsInfo.SimNode ||
                comp instanceof SimulationsInfo.CellGroupNode||
                comp instanceof SimulationsInfo.CellNode)
        {
            try
            {
                SimulationData simData = null;
                if (comp instanceof SimulationsInfo.SimNode)
                {
                    simData = (SimulationData)(dmtn.getUserObject());

                    Properties p = simData.getSimulationProperties();
                }
                else if (comp instanceof SimulationsInfo.CellGroupNode)
                {
                    simData = 
                        ((SimulationData)((DefaultMutableTreeNode)path.getParentPath().getLastPathComponent()).getUserObject());
                }
                else if (comp instanceof SimulationsInfo.CellNode)
                {
                    simData = 
                        ((SimulationData)((DefaultMutableTreeNode)path.getParentPath().getParentPath().getLastPathComponent()).getUserObject());
                }

                simData.initialise();
                double[] times = simData.getAllTimes();

                if (ae.getActionCommand().equals(SIM_INFO))
                {
                    logger.logComment("Giving data on: "+simData);

                    String simInfo = SimulationsInfo.getSimProps(simData.getSimulationDirectory(), true);

                    SimpleViewer.showString(simInfo, "Information on simulation: "+simData.getSimulationName(), 12, false, true);

                    return;
                }
                if (ae.getActionCommand().equals(SIM_SUMMARY))
                {
                    logger.logComment("Giving data on: "+simData);

                    String simInfo = SimulationsInfo.getSimProps(simData.getSimulationDirectory(), true, true);

                    SimpleViewer.showString(simInfo, "Summary of simulation: "+simData.getSimulationName(), 12, false, true, 0.5f);

                    return;
                }

                float allProb = -1;

                if (ae.getActionCommand().equals(PLOT_SEPARATE_CG_VAR_SEL))
                {
                    String prob = JOptionPane.showInputDialog(this, "Please enter percentage of selected cells to plot","10");
                    allProb = Float.parseFloat(prob)/100;
                }

                String plotAllInOption = null;

                for (DataStore ds: simData.getAllLoadedDataStores())
                {
                    if (comp instanceof SimulationsInfo.SimNode ||
                        (comp instanceof SimulationsInfo.CellGroupNode &&
                            ds.getCellGroupName().equals(((SimulationsInfo.CellGroupNode)comp).getCellGroupName())) ||
                        (comp instanceof SimulationsInfo.CellNode &&
                            ds.getCellNumber() == ((SimulationsInfo.CellNode)comp).getIndex()))
                    {
                        String frameRef = null;
                        if (ae.getActionCommand().equals(PLOT_ALL))
                        {
                            frameRef = "Plot of data in "+simData.getSimulationName()+"";
                        }
                        if (ae.getActionCommand().equals(PLOT_ALL_IN))
                        {
                            if (plotAllInOption==null)
                            {
                                frameRef = getPlotFrame(simData);
                                if (frameRef==null) return;
                                plotAllInOption = frameRef;
                            }
                            else
                            {
                                frameRef = plotAllInOption;
                            }
                        }
                        else if (ae.getActionCommand().equals(PLOT_SEPARATE_CG_VAR_ALL))
                        {
                            frameRef = "Plot of "+ds.getVariable()+" in "+ds.getCellGroupName()+" in "+simData.getSimulationName()+"";
                        }
                        else if (ae.getActionCommand().equals(PLOT_SEPARATE_CG_VAR_SEL))
                        {
                            frameRef = "Plot of "+ds.getVariable()+" in "+ds.getCellGroupName()+" in "+simData.getSimulationName()+"";
                        }
                        else if (ae.getActionCommand().equals(PLOT_ALL_VOLTS))
                        {
                            frameRef = "Plot of voltage traces in "+simData.getSimulationName()+"";
                        }
                        else if (ae.getActionCommand().equals(PLOT_NON_VOLTS))
                        {
                            frameRef = "Plot of all traces except voltages in "+simData.getSimulationName()+"";
                        }

                        boolean isVoltData = ds.getVariable().endsWith(SimPlot.VOLTAGE);

                        boolean plot = true;

                        if (ae.getActionCommand().equals(PLOT_ALL_VOLTS))
                            plot = isVoltData;
                        else if (ae.getActionCommand().equals(PLOT_NON_VOLTS))
                            plot = !isVoltData;

                        if (allProb>0)
                        {
                            plot = Math.random() <= allProb;
                        }


                        if (plot)
                        {
                            PlotterFrame pf = PlotManager.getPlotterFrame(frameRef);

                            String dataRef = "Plot of "+ ds.getVariable()+" in "+ ds.getCellSegRef()+ " ("+simData.getSimulationName()+")";

                            String desc = dataRef;
                            DataSet dataSet = new DataSet(dataRef, desc,
                                                           "ms",
                                                           SimPlot.getUnitSymbol(ds.getVariable()),
                                                           "Time",
                                                           SimPlot.getLegend(ds.getVariable()));

                            double[] points = ds.getDataPoints();

                            for (int i = 0; i < times.length; i++)
                            {
                                dataSet.addPoint(times[i], points[i]);
                            }
                            //System.out.println("col "+ dataSet.getGraphColour());
                            pf.addDataSet(dataSet);
                        }
                    }

                }

                PlotManager.arrangeFrames();
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error plotting data", ex, this);
            }
            
        }
        else if (comp instanceof SimulationsInfo.DataStoreNode)
        {
            if (ae.getActionCommand().equals(PLOT) || ae.getActionCommand().equals(PLOT_IN))
            {
                try
                {
                    SimulationData simData =
                        ((SimulationData)((DefaultMutableTreeNode)path.getParentPath().getParentPath().getParentPath().getLastPathComponent()).getUserObject());

                    double[] times = simData.getAllTimes();

                    if (ae.getActionCommand().equals(PLOT) || ae.getActionCommand().equals(PLOT_IN))
                    {
                        SimulationsInfo.DataStoreNode dsn = (SimulationsInfo.DataStoreNode)comp;
                        DataStore ds = (DataStore)dsn.getUserObject();

                        String frameRef = "Plot of "+ ds;

                        if (ae.getActionCommand().equals(PLOT_IN))
                        {
                            frameRef = getPlotFrame(simData);
                            if (frameRef==null) return;
                        }


                        PlotterFrame pf = PlotManager.getPlotterFrame(frameRef);

                        String ref = "Plot of "+ ds.getVariable()+" in "+ ds.getCellSegRef()+ " ("+simData.getSimulationName()+")";
                        String desc = ref;
                        DataSet dataSet = new DataSet(ref, desc,
                                                       "ms",
                                                       SimPlot.getUnitSymbol(ds.getVariable()),
                                                       "Time",
                                                       SimPlot.getLegend(ds.getVariable()));

                        double[] points = ds.getDataPoints();

                        for (int i = 0; i < times.length; i++)
                        {
                            dataSet.addPoint(times[i], points[i]);
                        }
                        pf.addDataSet(dataSet);

                    }
                }
                catch (Exception ex)
                {
                    GuiUtils.showErrorMessage(logger, "Error plotting data", ex, this);
                }
            }
        }
        
    }

    private String getPlotFrame(SimulationData simData)
    {
        String frameRef = null;

        ArrayList<String> r = PlotManager.getPlotterFrameReferences();

        if (r.isEmpty())
        {
            frameRef = "Plot of data in "+simData.getSimulationName()+"";
        }
        else
        {
            String newPlotFrame = "-- New Empty Plot Frame --";

            r.add(newPlotFrame);

            String[] refs = new String[r.size()];
            refs = r.toArray(refs);

            String option = (String)JOptionPane.showInputDialog(this,
                    "Please select Plot Frame", "Select Plot Frame", JOptionPane.OK_CANCEL_OPTION, null, refs, refs[0]);

            if (option == null)
                return null;

            if (option.equals(newPlotFrame))
            {
                frameRef = "Plot of data in "+simData.getSimulationName()+"";
                int count = 0;
                while (r.contains(frameRef))
                {
                    count++;
                    frameRef = "Plot of data in "+simData.getSimulationName()+" ("+count+")";
                }
            }
            else
            {
                frameRef = option;
            }
        }

        return frameRef;
    }


    public static void main(String[] args)
    {

        File projFile = new File("nCmodels/Thalamocortical/Thalamocortical.ncx");

        new SimulationTreeFrame(projFile, true).setVisible(true);
    }


   

    



}
