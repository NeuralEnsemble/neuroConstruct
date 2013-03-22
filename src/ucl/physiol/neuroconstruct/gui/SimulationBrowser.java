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
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  021
 * 11-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.gui;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Dialog for browsing saved simulations
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class SimulationBrowser extends JDialog
{
    ClassLogger logger = new ClassLogger("SimulationBrowser");

    SimulationsInfo allSims = null;

    public boolean cancelled = false;

    RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());


    JMenuBar jMenuBarMainMenu = new JMenuBar();
    JMenu jMenuColumnsMain = new JMenu();
    Hashtable<String, JMenu> jMenuColumnsSub = new Hashtable<String, JMenu>();


    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelSelection = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonOpen = new JButton();
    JButton jButtonCancel = new JButton();
    JButton jButtonInfo = new JButton();
    JButton jButtonRefresh = new JButton();
    JButton jButtonCompare = new JButton();
    GridLayout gridLayout1 = new GridLayout();
    JScrollPane jScrollPaneMain = new JScrollPane();
    JTable jTableSimulations = null;
    JButton jButtonDelete = new JButton();
    JButton jButtonRename = new JButton();

    String subMenuNetConns = "Network Connections";
    String subMenuStimulations = "Stimulations";
    String subMenuCellMechanims = "Cell Mechanisms";
    String subMenuMorphSummaries = "Morph summaries";

    private SimulationBrowser()
    { 

    }

    public SimulationBrowser(File simulationDir, Frame owner)
    {
        super(owner, "Simulation Browser", true);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);


        Vector<String> columnsToShow = recentFiles.getPreferredSimBrowserCols();

        logger.logComment("columnsToShow: "+ columnsToShow);

        if (columnsToShow==null || columnsToShow.size()<=2)
        {
            columnsToShow = new Vector<String>();
            columnsToShow.add(SimulationsInfo.COL_NAME_NAME);
            columnsToShow.add(SimulationsInfo.COL_NAME_DATE);
            columnsToShow.add("Simulator");
            columnsToShow.add("Populations");
        }


        allSims = new SimulationsInfo(simulationDir, columnsToShow);

        Vector allAvailableCols = allSims.getAllColumns();
        for (int i = 0; i < columnsToShow.size(); i++)
        {
                String nextCol = columnsToShow.get(i);
                if (!allAvailableCols.contains(nextCol))
                    columnsToShow.remove(nextCol);
        }

        try
        {
            jbInit();
            extraInit();
        }
        catch (Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }
    }

    private void jbInit() throws Exception
    {

        jButtonRename.setText("Rename");
        jButtonRename.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRename_actionPerformed(e);
            }
        });
        jMenuBarMainMenu.add(jMenuColumnsMain);

        this.setJMenuBar(jMenuBarMainMenu);

        jMenuColumnsMain.setText("     Columns to show");

        JMenu jMenuNetConns = new JMenu(subMenuNetConns);
        jMenuColumnsSub.put(subMenuNetConns, jMenuNetConns);

        JMenu jMenuCellMechanims = new JMenu(subMenuCellMechanims);
        jMenuColumnsSub.put(subMenuCellMechanims, jMenuCellMechanims);

        JMenu jMenuMorphSummaries = new JMenu(subMenuMorphSummaries);
        jMenuColumnsSub.put(subMenuMorphSummaries, jMenuMorphSummaries);

        JMenu jMenuStimulations = new JMenu(subMenuStimulations);
        jMenuColumnsSub.put(subMenuStimulations, jMenuStimulations);


        jTableSimulations = new JTable(allSims);
        jPanelMain.setLayout(borderLayout1);
        jPanelSelection.setBorder(BorderFactory.createEtchedBorder());
        jPanelSelection.setLayout(gridLayout1);
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
        jPanelButtons.setMaximumSize(new Dimension(300, 39));
        jPanelButtons.setMinimumSize(new Dimension(300, 39));
        jButtonOpen.setText("Load simulation");
        jButtonOpen.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOpen_actionPerformed(e);
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
        jButtonInfo.setText("Info on selected");
        jButtonInfo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonInfo_actionPerformed(e);
            }
        });
        jButtonRefresh.setText("Reload list");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRefresh_actionPerformed(e);
            }
        });
        jButtonCompare.setText("Compare sims");
        jButtonCompare.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCompare_actionPerformed(e);
            }
        });
        gridLayout1.setHgap(5);
        gridLayout1.setVgap(5);
        Dimension mainDim = new Dimension(930, 500);
        jPanelMain.setMaximumSize(mainDim);
        jPanelMain.setPreferredSize(mainDim);
        jPanelMain.setMinimumSize(mainDim);

        jButtonDelete.setText("Delete selected");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelete_actionPerformed(e);
            }
        });
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);

        jPanelMain.add(jPanelSelection,  BorderLayout.CENTER);
        jPanelSelection.add(jScrollPaneMain, null);
        jScrollPaneMain.getViewport().add(jTableSimulations, null);

        jTableSimulations.setColumnSelectionAllowed(true);


        jPanelMain.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelButtons.add(jButtonRefresh, null);
        jPanelButtons.add(jButtonRename, null);
        jPanelButtons.add(jButtonInfo, null);
        jPanelButtons.add(jButtonOpen, null);
        jPanelButtons.add(jButtonCompare, null);
        jPanelButtons.add(jButtonDelete, null);
        jPanelButtons.add(jButtonCancel, null);
    }



    private void extraInit()
    {
        //jListSimList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refresh(false);
        //jListSimList.setSelectedIndex(0);
        logger.logComment("Finished initialising...");
        //if ()

        ToolTipHelper toolTips = ToolTipHelper.getInstance();
        jButtonRefresh.setToolTipText(toolTips.getToolTip("Reload Simulation List"));
    }

    public class PropertiesMenuListener implements ActionListener
    {

        public PropertiesMenuListener()
        {
        }

        public void actionPerformed(ActionEvent e)
        {
            JCheckBoxMenuItem source = (JCheckBoxMenuItem)e.getSource();
            if (source.isSelected())
            {
                addShownColumn(source.getText());
            }
            else
            {
                removeShownColumn(source.getText());
            }

        };
    }

    protected void addShownColumn(String prop)
    {
        logger.logComment("Adding column: "+prop);
        allSims.addShownColumn(prop);
        refresh(false);
    }

    protected void removeShownColumn(String prop)
    {
        logger.logComment("Removing column: "+prop);
        allSims.removeShownColumn(prop);
        refresh(false);

    }


    private void refresh(boolean checkRemote)
    {
        allSims.refresh(checkRemote);

        jMenuColumnsMain.removeAll();

        for(String subMenu: jMenuColumnsSub.keySet())
        {
            jMenuColumnsSub.get(subMenu).removeAll();
        }

        Vector allPossibleCols = allSims.getAllColumns();
        Vector shownCols = allSims.getAllShownColumns();

        for (int i = 0; i < allPossibleCols.size(); i++)
        {
                String nextCol = (String)allPossibleCols.get(i);

                JCheckBoxMenuItem jMenuItemNext = new JCheckBoxMenuItem(nextCol);

                if(nextCol.startsWith("Net Conn:"))
                {
                    jMenuColumnsSub.get(subMenuNetConns).add(jMenuItemNext);
                }
                else if(nextCol.startsWith("Stimulation:"))
                {
                    jMenuColumnsSub.get(subMenuStimulations).add(jMenuItemNext);
                }
                else if(nextCol.startsWith("Cell Processes on") || nextCol.startsWith("Cell Mechanisms on") )
                {
                    jMenuColumnsSub.get(subMenuCellMechanims).add(jMenuItemNext);
                }
                else if(nextCol.startsWith("Morph summary")  )
                {
                    jMenuColumnsSub.get(subMenuMorphSummaries).add(jMenuItemNext);
                }
                else
                {
                    jMenuColumnsMain.add(jMenuItemNext);
                }

                jMenuItemNext.setSelected(shownCols.contains(nextCol));

                jMenuItemNext.setEnabled(!nextCol.equals(SimulationsInfo.COL_NAME_NAME)
                                         && !nextCol.equals(SimulationsInfo.COL_NAME_DATE));

                jMenuItemNext.addActionListener(new PropertiesMenuListener());

        }

        for(String subMenu: jMenuColumnsSub.keySet())
        {
            jMenuColumnsMain.add(jMenuColumnsSub.get(subMenu));
        }


        int numColumns = allSims.getColumnCount();
        for (int i = 0; i < numColumns; i++)
        {
            TableColumn nextColumn = jTableSimulations.getColumnModel().getColumn(i);
            String name = allSims.getColumnName(i);
            if (name.equals(SimulationsInfo.COL_NAME_DATE))
            {
                nextColumn.setPreferredWidth(150);
                nextColumn.setMaxWidth(200);
            }
            if (name.equals(SimulationsInfo.COL_NAME_NAME))
            {
                nextColumn.setPreferredWidth(150);
                nextColumn.setMaxWidth(300);
            }

            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

            renderer.setToolTipText("Value of " + name);

            nextColumn.setCellRenderer(renderer);

        }
    }

    public SimulationData getSelectedSimulation() throws SimulationDataException
    {
        int selectedSim= jTableSimulations.getSelectedRow();
        return allSims.getSimulationData(selectedSim);
    }


    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) jButtonOpen_actionPerformed(null);
    }





    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;
        recentFiles.setPreferredSimBrowserCols(allSims.getAllShownColumns());
        recentFiles.saveToFile();
        this.dispose();
    }


    void jButtonCompare_actionPerformed(ActionEvent e)
    {
        if(jTableSimulations.getSelectedRowCount()!=2)
        {
            GuiUtils.showErrorMessage(logger, "Please select 2 simulations (e.g. using ctrl key) to compare the properties of", null, this);
            return;
        }

        int selectedSim1 = jTableSimulations.getSelectedRows()[0];
        int selectedSim2 = jTableSimulations.getSelectedRows()[1];

        SimulationData simData1 = allSims.getSimulationData(selectedSim1);
        SimulationData simData2 = allSims.getSimulationData(selectedSim2);

        StringBuffer sb = new StringBuffer();

        sb.append("<h3>Comparing sim reference: " + simData1.getSimulationName() + " to "+simData2.getSimulationName()+"</h3>");

        sb.append(SimulationsInfo.compareSims(simData1.getSimulationDirectory(), simData2.getSimulationDirectory(), true));

        SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                     "Comparison of simulation: "
                                                     + simData1.getSimulationName() + " to "+simData2.getSimulationName(),
                                                     12,
                                                     false,
                                                     true,
                                                     this,
                                                     true);

        simpleViewer.setFrameSize(600, 500);


        GuiUtils.centreWindow(simpleViewer);
        simpleViewer.setVisible(true);

    }

    void jButtonInfo_actionPerformed(ActionEvent e)
    {
        if(jTableSimulations.getSelectedRowCount()==0) return;

        int selectedSim = jTableSimulations.getSelectedRow();

        SimulationData simData = allSims.getSimulationData(selectedSim);


        StringBuffer sb = new StringBuffer();


        sb.append("<h3>Simulation reference     : " + simData.getSimulationName() + "</h3>");
        sb.append("<p>Simulation directory: <b>" + simData.getSimulationDirectory().getAbsolutePath() + "</b></p>\n");
        sb.append("<p>Date recorded: <b>" + simData.getDateModified() + "</b></p>\n\n");

        sb.append(SimulationsInfo.getSimProps(simData.getSimulationDirectory(), true));

        SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                     "Parameters of simulation: "
                                                     + simData.getSimulationName(),
                                                     12,
                                                     false,
                                                     true,
                                                     this,
                                                     true);

        simpleViewer.setFrameSize(600, 500);

       
        GuiUtils.centreWindow(simpleViewer);
        simpleViewer.setVisible(true);
    }


    void jButtonOpen_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");
        if(jTableSimulations.getSelectedRowCount()==0) cancelled = true;

        recentFiles.setPreferredSimBrowserCols(allSims.getAllShownColumns());
        recentFiles.saveToFile();
        this.dispose();
    }

    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception ex)
        {

        }
        //File simDir = new File("../copyNcModels/Parallel/simulations");
        //File simDir = new File("nCmodels/InProgress/TraubEtAl05/simulations");
        File simDir = new File("nCmodels/Thalamocortical/simulations");
        //File simDir = new File("projects/temp/simulations/");
        SimulationBrowser dlg = new SimulationBrowser(simDir, null);

        dlg.setModal(true);
        dlg.pack();
        GuiUtils.centreWindow(dlg);

        dlg.setVisible(true);

    }

    void jButtonRefresh_actionPerformed(ActionEvent e)
    {
        logger.logComment("Refreshing view");
        refresh(true);
    }

    void jButtonDelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Deleting selected sim");
        if(jTableSimulations.getSelectedRowCount()==0) return;

        int[] selectedSims= jTableSimulations.getSelectedRows();

        for (int i = 0; i < selectedSims.length; i++)
        {
            SimulationData simData = allSims.getSimulationData(selectedSims[i]);
            File dirToDelete = simData.getSimulationDirectory();
            GeneralUtils.removeAllFiles(dirToDelete, true, true, true);

        }
        refresh(false);

    }

    void jButtonRename_actionPerformed(ActionEvent e)
    {
        logger.logComment("Rename button pressed");
        if(jTableSimulations.getSelectedRowCount()==0) cancelled = true;

        int selectedSim= jTableSimulations.getSelectedRow();

        SimulationData simData = allSims.getSimulationData(selectedSim);

        String newName = JOptionPane.showInputDialog(this, "Please enter the new name of this simulation", simData.getSimulationName());

        if (newName==null)
        {
            logger.logComment("User cancelled...");
            return;
        }

        if (newName.indexOf(" ")>0)
        {
            GuiUtils.showErrorMessage(logger, "Invalid Simulation name. Try again without spaces", null, this);
            jButtonRename_actionPerformed(e);
        }


        File dir = simData.getSimulationDirectory();
        File newFileName = new File(dir.getParentFile(), newName);
        boolean success = dir.renameTo(newFileName);

        if (!success)
        {
            GuiUtils.showErrorMessage(logger, "Problem renaming the simulation directory: "+ simData.getSimulationDirectory()+" to "+ newFileName, null, this);
            return;
        }

        simData.getSimulationProperties();



        refresh(false);

        //jTableSimulations
    }


}
