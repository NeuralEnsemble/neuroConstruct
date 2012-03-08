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
package ucl.physiol.neuroconstruct.gui.plotter;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.*;
import ucl.physiol.neuroconstruct.utils.units.Unit;

/**
 * Main frame of application for popping up a graph of a vector of points
 *
 * @author Padraig Gleeson
 *  
 */
@SuppressWarnings("serial")
public class PlotterFrame extends JFrame {

    private static ClassLogger logger = new ClassLogger("PlotterFrame");
    private boolean standAlone = true;
    private PlotCanvas plotCanvas = null;
    private final String formatMenuIndicator = "-> ";
    private final int maxLengthDescInMenu = 40;
    private String plotFrameReference = null;
    private Project project = null;
    protected static String defaultDataFilePrefix = "DataSet_";
    protected static String savedDataSetMenuPrefix = "(Saved) ";
    protected static String defaultMatplotlibDir = "";
    protected static String defaultMatplotlibTitle = "";
    private boolean rasterised = false;
    private RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());
    protected static String generateNew = "Generate new Data Set from this";
    /**
     * Used to indicate if one of the plots is specified as bar chart, but can't
     * be drawn as such...
     */
    private boolean problemDueToBarSpacing = false;
    // temp assugned false to allow plot specific start/stop to be entered
    private static boolean preferredSpikeValsEntered = false;
    JPanel contentPane;
    JMenuBar jMenuBarMainMenu = new JMenuBar();
    JMenu jMenuView = new JMenu();
    JMenu jMenuTools = new JMenu();
    JMenuItem jMenuDifference = new JMenuItem();
    JMenuItem jMenuAverage = new JMenuItem();
    JMenuItem jMenuAddManual = new JMenuItem();
    JMenuItem jMenuImportData = new JMenuItem();
    JMenuItem jMenuGenerateMatplotlib = new JMenuItem();
    JMenuItem jMenuApAnalysis = new JMenuItem();
    JRadioButtonMenuItem jMenuViewPointsOnly = new JRadioButtonMenuItem();
    JRadioButtonMenuItem jMenuItemViewOrigin = new JRadioButtonMenuItem();
    JRadioButtonMenuItem jMenuItemSelection = new JRadioButtonMenuItem();
    JRadioButtonMenuItem jMenuItemStacked = new JRadioButtonMenuItem();
    JRadioButtonMenuItem jMenuItemCustomView = new JRadioButtonMenuItem();
    ButtonGroup buttonGroupView = new ButtonGroup();
    //JMenu jMenuHelp = new JMenu();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JLabel jLabelStatusBar = new JLabel();
    JMenu jMenuPlotInfo = new JMenu();
    JPanel jPanelCanvasHolder = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border1;
    JMenu jMenuOptions = new JMenu();
    JMenuItem jMenuItemClose = new JMenuItem();
    JCheckBoxMenuItem jMenuItemShowAxes = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuItemShowAxisNums = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuItemShowAxisTicks = new JCheckBoxMenuItem();
    JMenuItem jMenuItemAllOneColour = new JMenuItem();
    JCheckBoxMenuItem jMenuItemRasterise = new JCheckBoxMenuItem();
    boolean siMenuItemEnable = false;
    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    /**
     * Create a new PlotterFrame. Note: this is protected so that new PlotterFrames will normally be created through
     * the PlotManager
     */
    public PlotterFrame(String reference,
            Project project,
            boolean standAlone) {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.standAlone = standAlone;
        this.project = project;

        plotFrameReference = reference;
        this.setTitle(plotFrameReference);

        try {
            // To make haavyweight menus...
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);

            jbInit();
            extraInit();

            //addSampleData();

            this.repaint();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extraInit() {
        plotCanvas = new PlotCanvas(this);

        jPanelCanvasHolder.add(plotCanvas, "Center");

        jMenuOptions.setToolTipText(toolTipText.getToolTip("Plot View"));
        jMenuTools.setToolTipText(toolTipText.getToolTip("Plot Tools"));
        jMenuDifference.setToolTipText(toolTipText.getToolTip("Plot Difference"));
        jMenuAverage.setToolTipText(toolTipText.getToolTip("Plot Average"));
        jMenuViewPointsOnly.setToolTipText(toolTipText.getToolTip("Plot points only"));
        jMenuApAnalysis.setToolTipText(toolTipText.getToolTip("Analyse AP shape"));
    }

    //Component initialization
    private void jbInit() throws Exception {
        buttonGroupView.add(jMenuViewPointsOnly);
        buttonGroupView.add(jMenuItemViewOrigin);
        buttonGroupView.add(jMenuItemStacked);
        buttonGroupView.add(jMenuItemSelection);
        buttonGroupView.add(jMenuItemCustomView);

        jMenuViewPointsOnly.setSelected(true);

        contentPane = (JPanel) this.getContentPane();
        border1 = BorderFactory.createLoweredBevelBorder();
        contentPane.setLayout(borderLayout1);

        this.setSize(new Dimension(600, 500));


        jMenuView.setText("View");
        jMenuViewPointsOnly.setText("Plot points only");
        jMenuViewPointsOnly.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuViewPointsOnly_actionPerformed(e);
            }
        });


        jMenuTools.setText("Tools");
        jMenuDifference.setText("Difference");
        jMenuAverage.setText("Average");
        jMenuAddManual.setText("Add Data Set from Expression...");
        jMenuImportData.setText("Import data from file...");
        jMenuGenerateMatplotlib.setText("Generate matplotlib files for EPS image & PDF (beta)...");
        jMenuApAnalysis.setText("AP shape analysis");
        jMenuTools.add(jMenuDifference);
        jMenuTools.add(jMenuAverage);
        jMenuTools.add(jMenuAddManual);
        jMenuTools.add(jMenuImportData);
        jMenuTools.add(jMenuGenerateMatplotlib);
        jMenuTools.add(jMenuApAnalysis);

        jMenuDifference.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuDifference_actionPerformed(e);
            }
        });
        jMenuAverage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuAverage_actionPerformed(e);
            }
        });

        jMenuAddManual.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuAddManual_actionPerformed(e);
            }
        });

        this.jMenuImportData.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuImportData_actionPerformed(e);
            }
        });

        this.jMenuGenerateMatplotlib.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuGenerateMatplotlib_actionPerformed(e);
            }
        });

        this.jMenuApAnalysis.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuApAnalysis_actionPerformed(e);
            }
        });


        //jMenuHelp.setText("Help");
        jMenuItemViewOrigin.setText("Include origin");
        jMenuItemViewOrigin.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemViewOrigin_actionPerformed(e);
            }
        });
        jPanelMain.setLayout(borderLayout2);
        jLabelStatusBar.setBorder(BorderFactory.createEtchedBorder());
        jLabelStatusBar.setText("...");
        //jMenuAboutInternalMenu.setText("About");
        //jMenuItemSubMenuItem.setText("thiss");
        jMenuPlotInfo.setText("Data Set info");
        jPanelMain.setBorder(border1);
        jPanelCanvasHolder.setMinimumSize(new Dimension(501, 401));
        jPanelCanvasHolder.setPreferredSize(new Dimension(501, 401));
        jPanelCanvasHolder.setLayout(borderLayout3);
        jMenuItemSelection.setText("Only selected area");
        jMenuItemSelection.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemSelection_actionPerformed(e);
            }
        });
        jMenuItemStacked.setText("Stacked plots");
        jMenuItemStacked.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemStacked_actionPerformed(e);
            }
        });

        jMenuItemCustomView.setText("Custom view");
        jMenuItemCustomView.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemCustomView_actionPerformed(e);
            }
        });




        jMenuOptions.setText("Options");
        jMenuView.add(jMenuViewPointsOnly);
        jMenuView.add(jMenuItemViewOrigin);
        jMenuView.add(jMenuItemSelection);
        jMenuView.add(jMenuItemStacked);
        jMenuView.add(jMenuItemCustomView);
        jMenuView.addSeparator();

        jMenuItemClose.setText("Close");
        jMenuView.add(jMenuItemClose);


        jMenuBarMainMenu.add(jMenuView);
        //jMenuBarMainMenu.add(jMenuHelp);
        jMenuBarMainMenu.add(jMenuPlotInfo);

        jMenuBarMainMenu.add(jMenuOptions);
        contentPane.add(jPanelMain, BorderLayout.CENTER);
        jPanelMain.add(jPanelCanvasHolder, BorderLayout.CENTER);
        contentPane.add(jLabelStatusBar, BorderLayout.SOUTH);

        this.setJMenuBar(jMenuBarMainMenu);

        jMenuItemShowAxes.setText("Show axes");
        jMenuItemShowAxes.setSelected(true);

        jMenuOptions.add(jMenuItemShowAxes);

        jMenuItemShowAxes.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemShowAxes_actionPerformed(e);
            }
        });

        jMenuItemClose.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemClose_actionPerformed(e);
            }
        });


        jMenuItemShowAxisNums.setText("Show axis numbering");
        jMenuItemShowAxisNums.setSelected(true);
        jMenuOptions.add(jMenuItemShowAxisNums);

        jMenuItemShowAxisNums.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemShowAxisNums_actionPerformed(e);
            }
        });

        jMenuItemShowAxisTicks.setText("Show axis ticks");
        jMenuItemShowAxisTicks.setSelected(true);
        jMenuOptions.add(jMenuItemShowAxisTicks);

        jMenuItemShowAxisTicks.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemShowAxisTicks_actionPerformed(e);
            }
        });

        jMenuOptions.addSeparator();
        jMenuItemRasterise.setText("Rasterise");
        jMenuItemRasterise.setSelected(false);
        jMenuOptions.add(jMenuItemRasterise);


        jMenuItemRasterise.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemRasterise_actionPerformed(e);
            }
        });


        jMenuOptions.addSeparator();
        jMenuItemAllOneColour.setText("Set all Data Sets same colour");
        jMenuOptions.add(jMenuItemAllOneColour);

        jMenuItemAllOneColour.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jMenuItemAllOneColour_actionPerformed(e);
            }
        });



        jMenuBarMainMenu.add(jMenuTools);

    }

    @Override
    public void dispose() {
        PlotManager.plotFrameClosing(plotFrameReference);
        logger.logComment("dispose()...");
        if (standAlone) {
            System.exit(0);
        }
    }

    public String getPlotFrameReference() {
        return plotFrameReference;
    }

    public void addDataSet(DataSet dataSet) {
        if (dataSet != null) {
            logger.logComment("Adding data set: " + dataSet.getReference());
        }
        plotCanvas.addDataSet(dataSet);

        updateMenus();
    }

    public void removeDataSet(DataSet dataSet) {
        logger.logComment("-----   Being asked to remove data set: " + dataSet.getReference());
        plotCanvas.removeDataSet(dataSet);
        updateMenus();
    }

    public int getNumDataSets() {
        return plotCanvas.getDataSets().length;
    }

    public void setMaxMinScaleValues(double maxXScaleValue,
                                     double minXScaleValue,
                                     double maxYScaleValue,
                                     double minYScaleValue)
    {
        plotCanvas.setMaxMinScaleValues(maxXScaleValue,minXScaleValue,maxYScaleValue,minYScaleValue);
    }

    protected void flagProblemDueToBarSpacing() {
        problemDueToBarSpacing = true;

    }

    protected void removeProblemDueToBarSpacing() {
        problemDueToBarSpacing = false;

    }

    protected boolean isProblemDueToBarSpacing() {
        return problemDueToBarSpacing;

    }

    protected boolean isRasterised() {
        return rasterised;

    }

    private void updateMenus() {
        logger.logComment("Update menus called...");
        jMenuPlotInfo.removeAll();

        if (plotCanvas.getDataSets().length > 5) {
            JMenuItem numInfo = new JMenuItem(plotCanvas.getDataSets().length + " Data Sets:");
            numInfo.setEnabled(false);
            jMenuPlotInfo.add(numInfo);
        }

        for (int i = 0; i < plotCanvas.getDataSets().length; i++) {
            DataSet nextDataSet = plotCanvas.getDataSets()[i];
            JMenu newMenu = new JMenu();


            newMenu.setText(nextDataSet.getReference());
            if (nextDataSet.getDataSetFile() != null) {
                newMenu.setText(savedDataSetMenuPrefix + nextDataSet.getReference());
            }

            newMenu.setForeground(nextDataSet.getGraphColour());
            JMenuItem descMenuItem = new JMenuItem();
            String someDescription = nextDataSet.getDescription();
            if (someDescription.length() > maxLengthDescInMenu) {
                someDescription = someDescription.substring(0, maxLengthDescInMenu - 3) + "...";
            }
            descMenuItem.setText(someDescription);
            descMenuItem.setEnabled(false);
            newMenu.add(descMenuItem);

            newMenu.addSeparator();

            JMenuItem infoMenuItem = new JMenuItem();
            infoMenuItem.setText("Quick info");
            infoMenuItem.setToolTipText("Description associated with the data set");
            newMenu.add(infoMenuItem);

            infoMenuItem.addActionListener(new DataSetInfoMenuListener(nextDataSet));

            JMenuItem listPointsMenuItem = new JMenuItem();
            listPointsMenuItem.setText("List points");
            listPointsMenuItem.setToolTipText("List of points in the data set");
            newMenu.add(listPointsMenuItem);

            listPointsMenuItem.addActionListener(new DataSetListPointsMenuListener(nextDataSet));

            JMenuItem editPointsMenuItem = new JMenuItem();
            editPointsMenuItem.setText("Edit points");
            editPointsMenuItem.setToolTipText("Adjust the points in this data set");
            newMenu.add(editPointsMenuItem);

            editPointsMenuItem.addActionListener(new DataSetEditPointsMenuListener(nextDataSet, this));



            JMenuItem quickStatsItem = new JMenuItem();
            quickStatsItem.setText("Simple statistics");
            quickStatsItem.setToolTipText("Average values, standard distribution, etc.");
            newMenu.add(quickStatsItem);

            quickStatsItem.addActionListener(new DataSetQuickStatsMenuListener(nextDataSet));


            JMenu transformItem = new JMenu();
            newMenu.add(transformItem);
            transformItem.setText("Functions");

            //transformItem.addActionListener(new DataSetDistHistMenuListener(nextDataSet));


            JMenuItem distHistItemY = new JMenuItem();
            distHistItemY.setText("Distribution Histogram (y values)");
            distHistItemY.setToolTipText("Distribution Histogram of Y values of the data set");
            transformItem.add(distHistItemY);

            distHistItemY.addActionListener(new DataSetDistHistMenuListener(nextDataSet, DataSet.yDim));

            JMenuItem distHistItemX = new JMenuItem();
            distHistItemX.setText("Distribution Histogram (x values)");
            distHistItemX.setToolTipText("Distribution Histogram of X values of the data set");
            transformItem.add(distHistItemX);

            distHistItemX.addActionListener(new DataSetDistHistMenuListener(nextDataSet, DataSet.xDim));


            JMenuItem simpDerivItem = new JMenuItem();
            simpDerivItem.setText("Simple Derivative");
            simpDerivItem.setToolTipText("Simple derivative based on slope between adjacent points");
            transformItem.add(simpDerivItem);

            simpDerivItem.addActionListener(new DataSetSimpDerivMenuListener(nextDataSet));


            JMenuItem zerosItem = new JMenuItem();
            zerosItem.setText("Interpolated zeros");
            zerosItem.setToolTipText("Points where the graph crosses the x axis");
            transformItem.add(zerosItem);

            zerosItem.addActionListener(new DataSetZerosMenuListener(nextDataSet));

            JMenuItem areaItem = new JMenuItem();
            areaItem.setText("Simple area under graph");
            areaItem.setToolTipText("Area calculated by shape under adjacent points");
            transformItem.add(areaItem);

            areaItem.addActionListener(new DataSetAreaMenuListener(nextDataSet));

            JMenuItem absAreaItem = new JMenuItem();
            absAreaItem.setText("Absolute area under graph");
            absAreaItem.setToolTipText("Area calculated by absolute area under adjacent points");
            transformItem.add(absAreaItem);

            absAreaItem.addActionListener(new DataSetAbsAreaMenuListener(nextDataSet));







            JMenu spikeAnalysisMenuItem = new JMenu();
            newMenu.add(spikeAnalysisMenuItem);
            spikeAnalysisMenuItem.setText("Spike Analysis");

            JMenuItem simpSpikeMenuItem = new JMenuItem();
            simpSpikeMenuItem.setText("Simple spike analysis");
            simpSpikeMenuItem.setToolTipText("Simple analysis of spiking behaviour of this trace, num spikes, average frequency etc.");
            spikeAnalysisMenuItem.add(simpSpikeMenuItem);
            simpSpikeMenuItem.addActionListener(new DataSetSpikeMenuListener(nextDataSet));

            JMenuItem phasePlaneMenuItem = new JMenuItem();
            phasePlaneMenuItem.setText("Phase plane plot (dV/dt vs V)");
            phasePlaneMenuItem.setToolTipText("Phase plane plot which plots rate of increase of membrane potential versus membrate potential");
            spikeAnalysisMenuItem.add(phasePlaneMenuItem);
            phasePlaneMenuItem.addActionListener(new DataSetPhasePlanePlotMenuListener(nextDataSet));


            JMenuItem peaksMenuItem = new JMenuItem();
            peaksMenuItem.setText("Times of peaks");
            peaksMenuItem.setToolTipText("Peaks in the data above a certain threshold");
            spikeAnalysisMenuItem.add(peaksMenuItem);
            peaksMenuItem.addActionListener(new DataSetPeaksMenuListener(nextDataSet));


            JMenuItem isiHistMenuItem = new JMenuItem();
            isiHistMenuItem.setText("ISI histogram");
            isiHistMenuItem.setToolTipText("Histogram of Inter Spike Intervals");
            spikeAnalysisMenuItem.add(isiHistMenuItem);
            isiHistMenuItem.addActionListener(new DataSetISIHistMenuListener(nextDataSet));


            JMenuItem autocorrMenuItem = new JMenuItem();
            autocorrMenuItem.setText("Autocorrelogram");
            autocorrMenuItem.setToolTipText("This is mainly used for generating autocorrelograms of population histograms, to check for synchrony");
            spikeAnalysisMenuItem.add(autocorrMenuItem);
            autocorrMenuItem.addActionListener(new DataSetAutocorrMenuListener(nextDataSet, this));





            JMenuItem siMenuItem = new JMenuItem();
            siMenuItem.setText("Synchronisation Index");
            siMenuItem.setToolTipText("A Synchronisation Index can be generated for autocorrelograms (see Maex & De Schutter 1998)");


            spikeAnalysisMenuItem.add(siMenuItem);
            siMenuItem.addActionListener(new DataSetSyncIndexMenuListener(nextDataSet, this));
            siMenuItem.setEnabled(this.siMenuItemEnable);






            JMenu saveMenuItem = new JMenu();
            newMenu.add(saveMenuItem);
            saveMenuItem.setText("Save Data Set");
            saveMenuItem.setToolTipText("Save this data set in this project. Can be reloaded via Project -> Data Set Manager");






            if (project != null) {
                JMenuItem saveInProjectMenuItem = new JMenuItem();
                saveInProjectMenuItem.setText("Save Data Set in project");
                saveMenuItem.add(saveInProjectMenuItem);

                saveInProjectMenuItem.addActionListener(new DataSetSaveInProjMenuListener(nextDataSet, this));

            }


            JMenuItem exportMenuItem = new JMenuItem();
            exportMenuItem.setText("Export points to file...");
            exportMenuItem.setToolTipText("Export this data set to a file for use by another application");
            saveMenuItem.add(exportMenuItem);
            File prefFile = new File(".");
            if (project != null) {
                prefFile = ProjectStructure.getDataSetsDir(project.getProjectMainDirectory());
            }

            exportMenuItem.addActionListener(new DataSetExportMenuListener(nextDataSet, this, prefFile));





            JMenuItem colourMenuItem = new JMenuItem();
            colourMenuItem.setText("Colour");
            newMenu.add(colourMenuItem);

            colourMenuItem.addActionListener(new DataSetColourMenuListener(nextDataSet, this));


            JMenu formatMenu = new JMenu();
            formatMenu.setText("Graph format");
            formatMenu.setToolTipText("How to represent the points of the data set: circles, points, lines, etc.");
            newMenu.add(formatMenu);



            String[] options = new String[]{PlotCanvas.USE_POINTS_FOR_PLOT,
                PlotCanvas.USE_LINES_FOR_PLOT,
                PlotCanvas.USE_THICK_LINES_FOR_PLOT,
                PlotCanvas.USE_CIRCLES_FOR_PLOT,
                PlotCanvas.USE_CROSSES_FOR_PLOT,
                PlotCanvas.USE_BARCHART_FOR_PLOT};

            for (int optionNum = 0; optionNum < options.length; optionNum++) {
                JMenuItem newMenuItem = new JMenuItem();
                String text = options[optionNum];
                if (nextDataSet.getGraphFormat().equals(text)) {
                    text = formatMenuIndicator + text;
                }
                newMenuItem.setText(text);
                formatMenu.add(newMenuItem);
                newMenuItem.addActionListener(new DataSetFormatMenuListener(nextDataSet, this));

            }

            jMenuPlotInfo.add(newMenu);



            JMenuItem transformMenuItem = new JMenuItem();
            transformMenuItem.setText(generateNew);
            transformMenuItem.setToolTipText("Create new Data Set from X and Y values of this DataSet");
            newMenu.add(transformMenuItem);

            transformMenuItem.addActionListener(new DataSetTransformListener(nextDataSet, this));


            JMenuItem moveMenuItem = new JMenuItem();
            moveMenuItem.setText("Move Data Set to another Plot Frame...");
            moveMenuItem.setToolTipText("Removes this Data Set from this Plot Frame and move it to another open one");
            newMenu.add(moveMenuItem);

            moveMenuItem.addActionListener(new DataSetMoveMenuListener(nextDataSet, this));

            JMenuItem detachMenuItem = new JMenuItem();
            detachMenuItem.setText("Detach Data Set");
            detachMenuItem.setToolTipText("Removes this Data Set from this Plot Frame and move it to a new one");
            newMenu.add(detachMenuItem);

            detachMenuItem.addActionListener(new DataSetDetachMenuListener(nextDataSet, this));


            JMenuItem removeMenuItem = new JMenuItem();
            removeMenuItem.setText("Remove Data Set from Plot Frame");
            removeMenuItem.setToolTipText("Remove only this set of data points");
            newMenu.add(removeMenuItem);

            removeMenuItem.addActionListener(new DataSetRemoveMenuListener(nextDataSet, this));

        }
        // difference doesn't make sense with less than 2 data sets...
        if (plotCanvas.dataSets.length < 2) {
            jMenuDifference.setEnabled(false);
            jMenuAverage.setEnabled(false);
        } else {
            jMenuDifference.setEnabled(true);
            jMenuAverage.setEnabled(true);
        }

        logger.logComment("Done updating menus, repainting");
        if (this.isVisible()) {
            plotCanvas.repaint();
        }
    }

    public class DataSetInfoMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetInfoMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuffer sb = new StringBuffer();

            sb.append("Information on Data Set: " + dataSet.getReference() + "\n\n");


            if (dataSet.getXLegend().length() > 0 && dataSet.getYLegend().length() > 0) {
                sb.append("X axis: " + dataSet.getXLegend() + "\n"
                        + "Y axis: " + dataSet.getYLegend() + "\n\n");
            }
            if (dataSet.getXUnit().length() > 0 && dataSet.getYUnit().length() > 0) {
                sb.append("Units along X axis: " + dataSet.getXUnit() + "\n"
                        + "Units along Y axis: " + dataSet.getYUnit() + "\n\n");
            }


            sb.append("--------      Description:      --------\n");
            sb.append(dataSet.getDescription() + "\n");
            sb.append("---------------------------\n");

            sb.append("Number of points: " + dataSet.getNumberPoints() + "\n\n");

            sb.append("Max X: " + dataSet.getMaxX()[0] + "\n");
            sb.append("Min X: " + dataSet.getMinX()[0] + "\n");
            sb.append("Max Y: " + dataSet.getMaxY()[1] + "\n");
            sb.append("Min Y: " + dataSet.getMinY()[1] + "\n\n");

            if (sb.length() < 400) {

                GuiUtils.showInfoMessage(null, "Info on Data Set: " + dataSet.getReference(),
                        sb.toString(), null);

            } else {
                SimpleViewer.showString(sb.toString(),
                        "Info on Data Set: " + dataSet.getReference(),
                        12, false, false);
            }

        }

        ;
    }

    public class DataSetTransformListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetTransformListener(DataSet dataSet,
                PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;

        }

        public void actionPerformed(ActionEvent e) {
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

            InputRequestElement xTrans = new InputRequestElement("xTrans", "Enter the expression for the new x values as a function of the original x, y and i (point number)", null, "x", "");

            inputs.add(xTrans);

            InputRequestElement yTrans = new InputRequestElement("yTrans", "Enter the expression for the new y values as a function of the original x, y and i", null, "y * 10", "");

            inputs.add(yTrans);

            InputRequest dlg = new InputRequest(plotFrame,
                    "Please specify the form of the new DataSet",
                    "New Data Set",
                    inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);

            if (dlg.cancelled()) {
                return;
            }


            Variable i = new Variable("i");
            Variable x = new Variable("x");
            Variable y = new Variable("y");

            String xExpression = xTrans.getValue();
            String yExpression = yTrans.getValue();
            String xName = null;
            String yName = null;
            EquationUnit xFunc = null;
            EquationUnit yFunc = null;

            try {
                xFunc = Expression.parseExpression(xExpression,
                        new Variable[]{x, y, i});

                xName = "X expression: xNew = f(x, y, i) = " + xFunc.getNiceString();

                yFunc = Expression.parseExpression(yExpression,
                        new Variable[]{x, y, i});

                yName = "Y expression: yNew = f(x, y, i) = " + yFunc.getNiceString();
            } catch (EquationException ex) {
                GuiUtils.showErrorMessage(logger, "Unable to evaluate expressions:\nxNew = f(x, y, i) = "
                        + xExpression + "\nyNew = f(x, y, i) = " + yExpression + "\n", ex, null);
                return;
            }

            DataSet generatedDataSet = new DataSet("Transformation of " + dataSet.getReference(),
                    "Data Set: " + dataSet.getReference() + "transformed by:\n"
                    + xName + "\n" + yName + "\n" + "Original description:\n"
                    + dataSet.getDescription(),
                    dataSet.getXUnit(),
                    dataSet.getYUnit(),
                    "Transformation of " + dataSet.getXLegend(),
                    "Transformation of " + dataSet.getYLegend());

            for (int j = 0; j < dataSet.getNumberPoints(); j++) {
                double[] point = dataSet.getPoint(j);

                Argument[] x0 = new Argument[]{new Argument(x.getName(), point[0]),
                    new Argument(y.getName(), point[1]),
                    new Argument(i.getName(), j)};

                Argument[] y0 = new Argument[]{new Argument(x.getName(), point[0]),
                    new Argument(y.getName(), point[1]),
                    new Argument(i.getName(), j)};



                try {
                    generatedDataSet.addPoint(xFunc.evaluateAt(x0), yFunc.evaluateAt(y0));
                } catch (EquationException ex) {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate expressions:\n"
                            + xName + "\n" + yName + "\nat point: (" + point[0] + ", " + point[1] + ")", ex, null);
                    return;
                }
            }

            this.plotFrame.addDataSet(generatedDataSet);


        }
    }

    public class DataSetQuickStatsMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetQuickStatsMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuffer sb = new StringBuffer();

            sb.append("Quick stats on y axis of Data Set: " + dataSet.getReference() + "\n\n");

            double total = 0;

            for (int i = 0; i < dataSet.getNumberPoints(); i++) {
                //String next = (String)all.elementAt(i);
                total = total + dataSet.getPoint(i)[1];

            }
            double average = total / (double) dataSet.getNumberPoints();

            double totalDevSqrd = 0;

            for (int i = 0; i < dataSet.getNumberPoints(); i++) {
                //String next = (String)all.elementAt(i);
                double dev = dataSet.getPoint(i)[1] - average;

                totalDevSqrd = totalDevSqrd + (dev * dev);
            }

            double stdDev = Math.sqrt(totalDevSqrd / (double) dataSet.getNumberPoints());


            sb.append("Average value: " + average + "\n");
            sb.append("Standard deviation: " + stdDev + "\n\n");

            sb.append("Number of points: " + dataSet.getNumberPoints() + "\n");
            sb.append("Max X: " + dataSet.getMaxX()[0] + " (y value: " + dataSet.getMaxX()[1] + ") \n");
            sb.append("Min X: " + dataSet.getMinX()[0] + " (y value: " + dataSet.getMinX()[1] + ") \n");
            sb.append("Max Y: " + dataSet.getMaxY()[1] + " (x value: " + dataSet.getMaxY()[0] + ") \n");
            sb.append("Min Y: " + dataSet.getMinY()[1] + " (x value: " + dataSet.getMinY()[0] + ") \n");





            GuiUtils.showInfoMessage(null, "Info on Data Set: " + dataSet.getReference(),
                    sb.toString(), null);

        }

        ;
    }

    public void showSmallInfoBox(String title, String info) {

        SimpleViewer.showString(info,
                title,
                12,
                false,
                false,
                0.5f,
                0.3f);

    }

    public class DataSetEditPointsMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetEditPointsMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {
            logger.logComment("Action performed on DataSetEditPointsMenuListener");
            EditPointsDialog dlg = new EditPointsDialog(plotFrame, dataSet, true);
            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
                    (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);

            updateMenus();
        }

        ;
    }

    public class ApAnalysisMenuListener implements ActionListener {

        DataSet dataSet = null;

        public ApAnalysisMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

            String req = "Threshold for spike";

            float suggestedThresh = -20;
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();
            }
            InputRequestElement threshInput = new InputRequestElement("threshold", req, null, suggestedThresh + "", "mV");
            inputs.add(threshInput);


            req = "Start time from which to analyse the spiking";
            float suggestedStart = (float) dataSet.getMinX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedStart = plotCanvas.getSpikeOptions().getStartTime();
            }

            InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart + "", "ms");
            inputs.add(startInput);



            req = "Finish time from which to analyse the spiking";
            float suggestedEnd = (float) dataSet.getMaxX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedEnd = plotCanvas.getSpikeOptions().getStopTime();
            }

            InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd + "", "ms");
            inputs.add(stopInput);

            InputRequest dlg = new InputRequest(null, "Please enter the parameters for calculating spiking statistics", "Parameters for spiking statistics", inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);


            if (dlg.cancelled()) {
                return;
            }




            if (threshInput.getValue() == null) {
                return; // i.e. cancelled
            }
            float threshold = 0;
            try {
                threshold = Float.parseFloat(threshInput.getValue());
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setThreshold(threshold);

            if (startInput.getValue() == null) {
                return; // i.e. cancelled
            }
            float startTime = 0;
            try {
                startTime = Float.parseFloat(startInput.getValue());
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStartTime(startTime);


            if (stopInput.getValue() == null) {
                return; // i.e. cancelled
            }

            float stopTime = 1000;
            try {
                stopTime = Float.parseFloat(stopInput.getValue());
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStopTime(stopTime);

            PlotterFrame.preferredSpikeValsEntered = true;





            // boolean spiking = false;
            double[] spikeTimes = SpikeAnalyser.getSpikeTimes(dataSet.getYValues(),
                    dataSet.getXValues(),
                    threshold,
                    startTime,
                    stopTime);

            ArrayList interSpikeIntervals = SpikeAnalyser.getInterSpikeIntervals(dataSet.getYValues(),
                    dataSet.getXValues(),
                    threshold,
                    startTime,
                    stopTime);



            double totalISI = 0;

            for (int i = 0; i < interSpikeIntervals.size(); i++) {
                totalISI = totalISI + ((Double) interSpikeIntervals.get(i)).doubleValue();

            }
        }
    }

    public class DataSetSpikeMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetSpikeMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {

            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

            String req = "Threshold for spike";

            float suggestedThresh = -20;
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();
            }
            InputRequestElement threshInput = new InputRequestElement("threshold", req, null, suggestedThresh + "", "mV");
            inputs.add(threshInput);


            req = "Start time from which to analyse the spiking";
            float suggestedStart = (float) dataSet.getMinX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedStart = plotCanvas.getSpikeOptions().getStartTime();
            }

            InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart + "", "ms");
            inputs.add(startInput);



            req = "Finish time from which to analyse the spiking";
            float suggestedEnd = (float) dataSet.getMaxX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedEnd = plotCanvas.getSpikeOptions().getStopTime();
            }

            InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd + "", "ms");
            inputs.add(stopInput);

            InputRequest dlg = new InputRequest(null, "Please enter the parameters for calculating spiking statistics", "Parameters for spiking statistics", inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);


            if (dlg.cancelled()) {
                return;
            }




            if (threshInput.getValue() == null) {
                return; // i.e. cancelled
            }
            float threshold = 0;
            try {
                threshold = Float.parseFloat(threshInput.getValue());
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setThreshold(threshold);

            if (startInput.getValue() == null) {
                return; // i.e. cancelled
            }
            float startTime = 0;
            try {
                startTime = Float.parseFloat(startInput.getValue());
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStartTime(startTime);


            if (stopInput.getValue() == null) {
                return; // i.e. cancelled
            }

            float stopTime = 1000;
            try {
                stopTime = Float.parseFloat(stopInput.getValue());
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStopTime(stopTime);

            PlotterFrame.preferredSpikeValsEntered = true;





            // boolean spiking = false;
            double[] spikeTimes = SpikeAnalyser.getSpikeTimes(dataSet.getYValues(),
                    dataSet.getXValues(),
                    threshold,
                    startTime,
                    stopTime);

            ArrayList interSpikeIntervals = SpikeAnalyser.getInterSpikeIntervals(dataSet.getYValues(),
                    dataSet.getXValues(),
                    threshold,
                    startTime,
                    stopTime);



            double totalISI = 0;

            for (int i = 0; i < interSpikeIntervals.size(); i++) {
                totalISI = totalISI + ((Double) interSpikeIntervals.get(i)).doubleValue();

            }
            double averageISI = totalISI / (double) interSpikeIntervals.size();

            double totalDevSqrd = 0;

            for (int i = 0; i < interSpikeIntervals.size(); i++) {
                double dev = ((Double) interSpikeIntervals.get(i)).doubleValue() - averageISI;

                totalDevSqrd = totalDevSqrd + (dev * dev);
            }
            double stdDev = Math.sqrt(totalDevSqrd / (double) interSpikeIntervals.size());



            StringBuffer sb = new StringBuffer();

            //sb.append("<span style=\"color:#0000FF;\">");
            sb.append("<h2>Simple spiking info on Data Set: " + dataSet.getReference() + "</h2>");

            sb.append("Spiking threshold: " + plotCanvas.getSpikeOptions().getThreshold() + "<br>");
            sb.append("Total number of spikes: " + spikeTimes.length + "<br>");
            sb.append("Start time: " + plotCanvas.getSpikeOptions().getStartTime()
                    + ", stop time: " + plotCanvas.getSpikeOptions().getStopTime() + "<br>");

            sb.append("  <h3>X axis with units milliseconds:</h3>");

            double simDuration = (stopTime - startTime);

            sb.append("Total time: " + Utils3D.trimDouble(simDuration, 5) + " ms<br>");

            sb.append("Average frequency (#spikes/time): <b>"
                    + Utils3D.trimDouble(((double) spikeTimes.length / simDuration) * 1000, 5) + " Hz</b><br>");


            sb.append("Average Inter Spike Interval: " + Utils3D.trimDouble(averageISI, 5) + " ms<br>");

            sb.append("Standard Deviation on ISI   : " + Utils3D.trimDouble(stdDev, 5) + " ms<br>");

            sb.append("Frequency based on ISI: <b>" + Utils3D.trimDouble((1 / averageISI) * 1000, 5) + " Hz</b><br><br><br>");






            sb.append("<h3>X axis with units seconds:</h3>");


            sb.append("Total time: " + Utils3D.trimDouble(simDuration, 5) + " s<br>");

            sb.append("Average frequency (#spikes/time): <b>"
                    + Utils3D.trimDouble(((double) spikeTimes.length / simDuration), 5) + " Hz</b><br>");


            sb.append("Average Inter Spike Interval: " + Utils3D.trimDouble(averageISI, 5) + " s<br>");

            sb.append("Standard Deviation on ISI   : " + Utils3D.trimDouble(stdDev, 5) + " s<br>");

            sb.append("Frequency based on ISI: <b>" + Utils3D.trimDouble((1 / averageISI), 5) + " Hz</b><br><br><br>");


            String spikeTimeInfo = "";
            for (double st : spikeTimes) {
                spikeTimeInfo = spikeTimeInfo + (float) st + "  ";
            }

            sb.append("Actual spikeTimes: <br>" + spikeTimeInfo + "<br><br>");

            sb.append("Number of points: " + dataSet.getNumberPoints() + "<br>");

            //sb.append("Max X: " + plotCanvas.getSpikeOptions().getStartTime()
            //          + ", min X: " + plotCanvas.getSpikeOptions().getStopTime() + "<br>");
            //sb.append("Max Y: " + dataSet.getMaxYvalue() + ", min Y: " + dataSet.getMinYvalue() + "<br>");
            //sb.append("</span>");



            // GuiUtils.showInfoMessage(null, "Spike info on graph: " + dataSet.getReference(),
            //                         sb.toString(), null);
            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                    "Spike info on Data Set: "
                    + dataSet.getReference(),
                    12,
                    false,
                    true);

            simpleViewer.setFrameSize(600, 600);

            GuiUtils.centreWindow(simpleViewer);

            simpleViewer.setVisible(true);


        }

        ;
    }

    public class DataSetSyncIndexMenuListener implements ActionListener {

        DataSet dataSet = null;
        Component parent = null;

        public DataSetSyncIndexMenuListener(DataSet dataSet, Component parent) {
            this.dataSet = dataSet;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e) {
            logger.logComment("-----   Generating SI for: " + dataSet.getReference());

            double xSpacing = -1;
            try {
                xSpacing = dataSet.getXSpacing();
            } catch (DataSetException ex) {
            }

            if (xSpacing < 0 || dataSet.getMinX()[0] != 0) {
                logger.logComment("dataSet.areXvalsStrictlyIncreasing(): " + dataSet.areXvalsStrictlyIncreasing());
                logger.logComment("xSpacing: " + xSpacing);
                logger.logComment("dataSet.getMinXvalue(): " + dataSet.getMinX());
                GuiUtils.showErrorMessage(logger,
                        "The set of points: " + dataSet.getReference()
                        + " are not strictly increasing and evenly spaced, "
                        + " or the lowest x value is not zero, and these are requirements for generating a Synchronisation Index", null,
                        parent);

                return;

            }

            int N = dataSet.getNumberPoints();
            double[] yVals = dataSet.getYValues();
            double[] xVals = dataSet.getXValues();

            double periodAccuracy = xSpacing / 10d;

            double minPeriod = xSpacing * 3;

            double bestInnerProd = -1;
            double optimalPeriod = -1;

            //DataSet ds = new DataSet("fs", "sghdfg");



            for (double period = minPeriod; period <= dataSet.getMaxX()[0]; period += periodAccuracy) {
                double intProd = 0;
                for (int i = 0; i < N; i++) {
                    intProd += yVals[i] * Math.cos(2 * Math.PI * i / period);

                    // ds.addPoint(period, intProd);
                }
                //logger.logComment("intProd for period "+period+": " + intProd, true);
                if (intProd > bestInnerProd) {
                    bestInnerProd = intProd;
                    optimalPeriod = period;
                }
            }
            logger.logComment("optimalPeriod: " + optimalPeriod);

            //PlotterFrame frame = PlotManager.getPlotterFrame(ds.getReference(), false);
            //frame.addDataSet(ds);

            double total = 0;
            double intProdCos = 0;

            for (int i = 0; i < N; i++) {
                intProdCos += Math.cos(2 * Math.PI * xVals[i] / optimalPeriod) * yVals[i];
                total += yVals[i];
            }
            double si = intProdCos / total;

            GuiUtils.showInfoMessage(logger, "Synchronisation Index",
                    "The Synchronisation Index for that autocorrelogram is: " + si + " and the optimal period was: " + optimalPeriod, parent);



        }
    }

    public class DataSetAutocorrMenuListener implements ActionListener {

        DataSet dataSet = null;
        Component parent = null;

        public DataSetAutocorrMenuListener(DataSet dataSet, Component parent) {
            this.dataSet = dataSet;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e) {
            logger.logComment("-----   Generating autocorrelogram for : " + dataSet.getReference());

            double xSpacing = -1;
            try {
                xSpacing = dataSet.getXSpacing();
            } catch (DataSetException ex) {
            }

            double lengthInX = dataSet.getMaxX()[0] - dataSet.getMinX()[0];

            if (xSpacing < 0) {
                GuiUtils.showErrorMessage(logger,
                        "The set of points: " + dataSet.getReference()
                        + " are not strictly increasing and evenly spaced, and this is a requirement for generating an autocorrelogram", null,
                        parent);

                return;

            }
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

            String req = "Please enter the start time of the offset interval of the autocorrelogram";
            float start = 0;

            InputRequestElement startInput = new InputRequestElement("start", req, null, start + "", "ms");
            inputs.add(startInput);

            req = "Please enter the end time of the offset interval of the autocorrelogram";
            float end = (float) (lengthInX) / 2;

            InputRequestElement endInput = new InputRequestElement("end", req, null, end + "", "ms");
            inputs.add(endInput);

            InputRequest dlg = new InputRequest(null,
                    "Please enter the parameters for the autocorrelogram.",
                    "Parameters for the autocorrelogram",
                    inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);

            if (dlg.cancelled()) {
                return;
            }

            start = Float.parseFloat(startInput.getValue());
            end = Float.parseFloat(endInput.getValue());

            if ((end - start) >= lengthInX) {
                GuiUtils.showErrorMessage(logger,
                        "The time interval of [" + start + ", " + end
                        + "] is longer than the range of x values in the dataSet: ["
                        + dataSet.getMinX() + ", " + dataSet.getMaxX() + "] ", null, parent);

                return;

            }

            if (Math.abs(end) > lengthInX || Math.abs(start) > lengthInX) {
                GuiUtils.showErrorMessage(logger,
                        "The time interval of [" + start + ", " + end
                        + "] is not appropriate for the range of x values in the dataSet: ["
                        + dataSet.getMinX() + ", " + dataSet.getMaxX() + "] ", null, parent);

                return;

            }

            int beginOffset = (int) Math.floor(start / xSpacing);
            int endOffset = (int) Math.ceil(end / xSpacing);

            logger.logComment("---  Range: " + beginOffset + " points before and " + endOffset + " points after");

            double total = 0;

            int N = dataSet.getNumberPoints();

            for (int i = 0; i < N; i++) {
                total = total + dataSet.getPoint(i)[1];

            }
            double average = total / (double) N;

            double totalDevSqrd = 0;

            for (int i = 0; i < N; i++) {
                double dev = dataSet.getPoint(i)[1] - average;

                totalDevSqrd = totalDevSqrd + (dev * dev);
            }
            double variance = totalDevSqrd / (double) N;

            logger.logComment("average: " + average + ", variance: " + variance);

            int numPointsOrig = dataSet.getNumberPoints();

            int numInAC = endOffset - beginOffset + 1;

            logger.logComment("endOffset: " + endOffset + ", beginOffset: " + beginOffset + ", numInAC: " + numInAC);

            float[] acVals = new float[numInAC];

            int startIndex = 0;
            if (beginOffset < 0) {
                startIndex = -1 * beginOffset;
            }

            double[] yVals = dataSet.getYValues();

            for (int pointIndex = startIndex; pointIndex < numPointsOrig - endOffset; pointIndex++) {
                double y = yVals[pointIndex];

                int offset = beginOffset;

                while (pointIndex + offset < N && offset <= endOffset) {
                    int offsetPointIndex = pointIndex + offset;
                    //logger.logComment("corr of point: " + pointIndex + " with point: "+offsetPointIndex, true);
                    //double y2 = dataSet.getPoint(offsetPointIndex)[1];
                    double y2 = yVals[offsetPointIndex];

                    double prod = (y2) * (y);
                    //double prod = (y2-average)*(y-average);
                    int pointNum = offset - beginOffset;
                    acVals[pointNum] += prod;

                    offset++;
                }

            }

            DataSet acDataSet = new DataSet("Autocorrelogram of " + dataSet.getReference(),
                    "Autocorrelogram of " + dataSet.getReference(),
                    "ms",
                    "",
                    "Time",
                    "Autocorrelation");

            for (int i = 0; i < numInAC; i++) {

                acDataSet.addPoint(i, acVals[i]);

            }

            //acDataSet.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
            PlotterFrame frame = PlotManager.getPlotterFrame(acDataSet.getReference(), false, true);

            frame.setSIOptionEnabled(true);

            frame.addDataSet(acDataSet);

        }
    }

    public void setSIOptionEnabled(boolean enabled) {
        this.siMenuItemEnable = enabled;
    }

    public class DataSetISIHistMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetISIHistMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            String req = "Please enter the cutoff threshold which will be considered a spike";

            float suggestedThresh = -20;
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();
            }

            String thresh = JOptionPane.showInputDialog(req, "" + suggestedThresh);

            if (thresh == null) {
                return; // i.e. cancelled
            }
            float threshold = 0;
            try {
                threshold = Float.parseFloat(thresh);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setThreshold(threshold);




            req = "Please enter the start time from which to analyse the spiking";
            float suggestedStart = (float) dataSet.getMinX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedStart = plotCanvas.getSpikeOptions().getStartTime();
            }

            String start = JOptionPane.showInputDialog(req, "" + suggestedStart);

            if (start == null) {
                return; // i.e. cancelled
            }
            float startTime = 0;
            try {
                startTime = Float.parseFloat(start);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStartTime(startTime);




            req = "Please enter the finish time from which to analyse the spiking";
            float suggestedEnd = (float) dataSet.getMaxX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedEnd = plotCanvas.getSpikeOptions().getStopTime();
            }

            String stop = JOptionPane.showInputDialog(req, "" + suggestedEnd);

            if (stop == null) {
                return; // i.e. cancelled
            }

            float stopTime = 1000;
            try {
                stopTime = Float.parseFloat(stop);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStopTime(stopTime);


            req = "Please enter the bin size for the ISI Histogram";

            float suggestedBinSize = Math.min(1, (stopTime - startTime) / 60f);

            logger.logComment("Suggested bin size: " + suggestedBinSize);


            String binSizeString = JOptionPane.showInputDialog(req, "" + suggestedBinSize);

            if (binSizeString == null) {
                return; // i.e. cancelled
            }

            float binSize = 1;
            try {
                binSize = Float.parseFloat(binSizeString);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid ISI Histogram bin size", ex, null);
                return;
            }


            req = "Please enter the maximum ISI value to be plotted";
            float suggestedMax = (stopTime - startTime) / 10f;


            String maxString = JOptionPane.showInputDialog(req, "" + suggestedMax);

            if (maxString == null) {
                return; // i.e. cancelled
            }

            float maxSize = 1;
            try {
                maxSize = Float.parseFloat(maxString);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid maximum ISI Histogram size", ex, null);
                return;
            }


            PlotterFrame.preferredSpikeValsEntered = true;




            ArrayList interSpikeIntervals = SpikeAnalyser.getInterSpikeIntervals(dataSet.getYValues(),
                    dataSet.getXValues(),
                    threshold,
                    startTime,
                    stopTime);

            int numBins = Math.round((maxSize) / binSize);

            DataSet isiHist = new DataSet("ISI Histogram of " + dataSet.getReference(),
                    "ISI Histogram of " + dataSet.getReference(),
                    "ms",
                    "",
                    "Interspike interval",
                    "Number per bin");

            for (int i = 0; i < numBins; i++) {
                float startISI = i * binSize;
                float endISI = (i + 1) * binSize;

                int totalHere = 0;

                for (int j = 0; j < interSpikeIntervals.size(); j++) {
                    double isi = ((Double) interSpikeIntervals.get(j)).doubleValue();

                    if (isi >= startISI && isi < endISI) {
                        totalHere++;
                    }
                }
                isiHist.addPoint((endISI + startISI) / 2f, totalHere);

            }
            ///check more...
            boolean warn = false;
            for (int j = 0; j < interSpikeIntervals.size(); j++) {
                double isi = ((Double) interSpikeIntervals.get(j)).doubleValue();

                if (isi >= maxSize) {
                    warn = true;
                }
            }
            if (warn) {
                GuiUtils.showErrorMessage(logger, "Warning. The maximum ISI you have chosen to be plotted, "
                        + maxSize + ", is exceeded by at least one of the ISIs\n"
                        + "in the original spike train, and so not all ISIs will be included on this histogram.", null, null);
            }



            isiHist.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
            PlotterFrame frame = PlotManager.getPlotterFrame(isiHist.getReference(), false, true);

            frame.addDataSet(isiHist);





            /*
            double totalISI = 0;

            for (int i = 0; i < interSpikeTimes.size(); i++)
            {
            totalISI = totalISI + ((Double)interSpikeTimes.elementAt(i)).doubleValue();

            }
            double averageISI = totalISI/(double)interSpikeTimes.size();

            double totalDevSqrd = 0;

            for (int i = 0; i < interSpikeTimes.size(); i++)
            {
            double dev = ((Double)interSpikeTimes.elementAt(i)).doubleValue() - averageISI;

            totalDevSqrd = totalDevSqrd + (dev*dev);
            }
            double stdDev = Math.sqrt(totalDevSqrd/(double)interSpikeTimes.size());

             */



        }

        ;
    }

    public class DataSetSaveInProjMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetSaveInProjMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {
            if (project == null) {
                GuiUtils.showErrorMessage(logger,
                        "Error. There is no project associated with this plot instance",
                        null,
                        plotFrame);
            }

            File saveDir = ProjectStructure.getDataSetsDir(project.getProjectMainDirectory());


            int dataSetCount = 0;

            String suggestedName = DataSetManager.DATA_SET_PREFIX + dataSetCount
                    + ProjectStructure.getDataSetExtension();

            File suggestedFile = new File(saveDir, suggestedName);

            while (suggestedFile.exists()) {
                dataSetCount++;
                suggestedName = DataSetManager.DATA_SET_PREFIX + dataSetCount
                        + ProjectStructure.getDataSetExtension();

                suggestedFile = new File(saveDir, suggestedName);

            }

            dataSet.setDataSetFile(suggestedFile);


            DataSetManager.saveDataSet(dataSet);

            GuiUtils.showInfoMessage(null, "Saved to project.",
                    "Data Set saved to project, in file " + dataSet.getDataSetFile().getAbsolutePath()
                    + "\nTo view these points again select Project -> Data Set Manager",
                    null);

            logger.logComment("Action performed on DataSetSaveInProjMenuListener");

            updateMenus();

        }

        ;
    }

    public class DataSetExportMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;
        File preferredDir = null;

        public DataSetExportMenuListener(DataSet dataSet, PlotterFrame plotFrame, File preferredDir) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
            this.preferredDir = preferredDir;
        }

        public void actionPerformed(ActionEvent e) {
            RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());
            String lastDir = recentFiles.getMyLastExportPointsDir();

            File defaultDir = null;

            if (lastDir != null) {
                defaultDir = new File(lastDir);
            } else {
                defaultDir = preferredDir;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);

            chooser.setCurrentDirectory(defaultDir);
            String prefFileName = GeneralUtils.getBetterFileName(dataSet.getReference() + ".dat");

            chooser.setSelectedFile(new File(prefFileName));

            chooser.setDialogTitle("Choose file to export this data to");

            int retval = chooser.showDialog(plotFrame, "Choose file");

            if (retval == JOptionPane.OK_OPTION) {
                if (chooser.getSelectedFile() != null) {


                    if (chooser.getSelectedFile().exists()
                            && chooser.getSelectedFile().length() > 0) {
                        int ans = JOptionPane.showConfirmDialog(plotFrame,
                                "The file " + chooser.getSelectedFile()
                                + " is not empty. Overwrite?", "Confirm overwrite",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (ans == JOptionPane.NO_OPTION) {
                            return;
                        }

                    }
                    //System.out.println("Exporting to file: " + chooser.getSelectedFile());
                    try {
                        FileWriter fw = new FileWriter(chooser.getSelectedFile());
                        int numPoints = dataSet.getNumberPoints();

                        for (int i = 0; i < numPoints; i++) {
                            fw.write((float) dataSet.getPoint(i)[0] + ", " + (float) dataSet.getPoint(i)[1] + "\n");
                        }
                        fw.close();

                        GuiUtils.showInfoMessage(null, "Points exported",
                                "Those " + numPoints + " points have been saved to file: " + chooser.getSelectedFile()
                                + "\nFile length: " + chooser.getSelectedFile().length() + " bytes",
                                plotFrame);

                        recentFiles.setMyLastExportPointsDir(chooser.getSelectedFile().getAbsoluteFile().getParent());
                        recentFiles.saveToFile();

                    } catch (IOException ex) {
                        GuiUtils.showErrorMessage(null, "Problem exporting those points", ex, plotFrame);
                    }

                } else {
                    //System.out.println("Not continuing...");
                }
            }

        }

        ;
    }

    public class DataSetColourMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetColourMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {

            logger.logComment("Action performed on DataSetColourMenuListener");

            Color c = JColorChooser.showDialog(plotFrame,
                    "Please choose a colour for the data set: " + dataSet.getReference(),
                    dataSet.getGraphColour());
            if (c != null) {
                dataSet.setGraphColour(c);
                plotFrame.updateMenus();
            }
        }

        ;
    }

    public class DataSetRemoveMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetRemoveMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {
            plotFrame.removeDataSet(dataSet);

        }
        ;
    }

    public class DataSetMoveMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetMoveMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {
            ArrayList<String> r = PlotManager.getPlotterFrameReferences();
            int numCurently = plotFrame.getNumDataSets();

            if (r.size() == 1 && numCurently == 1) {
                GuiUtils.showErrorMessage(logger, "Sorry, there seems to be only one Plot Frame with a single Data Set currently open", null, null);
                return;
            }

            String newPlotFrame = "-- New Empty Plot Frame --";

            r.remove(plotFrame.getPlotFrameReference());
            if (numCurently > 1) {
                r.add(newPlotFrame);
            }
            String[] refs = new String[r.size()];
            refs = r.toArray(refs);

            String option = (String) JOptionPane.showInputDialog(plotFrame, "Please select Plot Frame to move Data Set to", "Move Data Set", JOptionPane.OK_CANCEL_OPTION, null, refs, refs[0]);

            if (option == null) {
                return;
            }

            String newPlotFrameRef = option;

            if (newPlotFrameRef.equals(newPlotFrame)) {
                newPlotFrameRef = dataSet.getReference();

                while (r.contains(newPlotFrameRef)) {
                    newPlotFrameRef = "New Plot Frame: " + newPlotFrameRef;
                }

            }

            PlotterFrame frame = PlotManager.getPlotterFrame(newPlotFrameRef);

            frame.addDataSet(dataSet);

            plotFrame.removeDataSet(dataSet);

            if (numCurently == 1) {
                plotFrame.setVisible(false);
                plotFrame.dispose();
            }
        }

        ;
    }

    public class DataSetDetachMenuListener implements ActionListener {

        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetDetachMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {

            String newPlotFrameRef = dataSet.getReference();


            PlotterFrame frame = PlotManager.getPlotterFrame(newPlotFrameRef);

            frame.addDataSet(dataSet);

            plotFrame.removeDataSet(dataSet);

        }

        ;
    }

    public class DataSetDistHistMenuListener implements ActionListener {

        DataSet dataSet = null;
        int dimension = -1;
        //PlotterFrame plotFrame = null;

        public DataSetDistHistMenuListener(DataSet dataSet, int dim) {
            this.dataSet = dataSet;
            this.dimension = dim;
            //this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {
            String info = "";
            String startInfo = "";
            String stopInfo = "";
            float start = Float.NaN;
            float stop = Float.NaN;

            if (dimension == DataSet.xDim) {
                info = "Plotting the distribution of the X values of the Data Set: " + dataSet.getReference() + "\n"
                        + "Max X: " + dataSet.getMaxX()[0] + ", min X: " + dataSet.getMinX()[0] + "\n"
                        + "Please enter the number of bins to use:";

                startInfo = "Please enter start X value from which to generate distribution";
                start = (float) dataSet.getMinX()[0];

                stopInfo = "Please enter final X value from which to generate distribution";
                stop = (float) dataSet.getMaxX()[0];
            } else {
                info = "Plotting the distribution of the y values of the Data Set: " + dataSet.getReference() + "\n"
                        + "Max y: " + dataSet.getMaxY()[1] + ", min y: " + dataSet.getMinY()[1] + "\n"
                        + "Please enter the number of bins to use:";

                startInfo = "Please enter start Y value from which to generate distribution";
                start = (float) dataSet.getMinY()[1];

                stopInfo = "Please enter final Y value from which to generate distribution";
                stop = (float) dataSet.getMaxY()[1];
            }

            //float bin = (float)(dataSet.getMaxYvalue() - dataSet.getMinYvalue()) / 20;

            int numBins = 20;

            String binNumString = JOptionPane.showInputDialog(info, "" + numBins);

            try {
                numBins = Integer.parseInt(binNumString);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid bin number", ex, null);
                return;
            }


            String startString = JOptionPane.showInputDialog(startInfo, "" + start);

            try {
                start = Float.parseFloat(startString);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid start value", ex, null);
                return;
            }



            String stopString = JOptionPane.showInputDialog(stopInfo, "" + stop);

            try {
                stop = Float.parseFloat(stopString);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid stop value", ex, null);
                return;
            }


            float binLength = ((stop - start) / (numBins));

            DataSet ds = SpikeAnalyser.getDistHist(dataSet, dimension, start, binLength, numBins);
            /*
            logger.logComment("binLength: " + binLength);

            int[] numInEach = SpikeAnalyser.getBinnedValues(dataSet.getYValues(),
            start,
            binLength,
            numBins);


            String desc = "";
            String newXval = "Y values";
            if (dataSet.getYLegend().length() > 0)
            {
            desc = "Distribution of: "+ dataSet.getReference();
            newXval = dataSet.getYLegend();
            }
            else
            {
            desc = "Distribution of y values of "+ dataSet.getReference();
            }

            DataSet ds = new DataSet(desc,
            desc,
            dataSet.getYUnit(), "", newXval, "Number per bin");

            for (int i = 0; i < numBins; i++)
            {
            double yVal = start + binLength * (i + 0.5);
            ds.addPoint(yVal, numInEach[i]);
            }

            ds.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);*/

            //ds.set

            PlotterFrame frame = PlotManager.getPlotterFrame(ds.getReference(), false, true);
            frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
            frame.addDataSet(ds);

        }
    }

    public class DataSetSimpDerivMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetSimpDerivMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {

            String newYUnit = "";
            String newYLegend = "";
            if (dataSet.getYUnit().length() > 0 && dataSet.getXUnit().length() > 0) {
                newYUnit = "(" + dataSet.getYUnit() + ") / (" + dataSet.getXUnit() + ")";
            }
            if (dataSet.getYLegend().length() > 0) {
                newYLegend = "Derivative of " + dataSet.getYLegend();
            }
            DataSet ds = new DataSet("Simple derivative of " + dataSet.getReference(),
                    "Simple derivative of \n" + dataSet.getDescription(),
                    dataSet.getXUnit(),
                    newYUnit,
                    dataSet.getXLegend(),
                    newYLegend);

            double[] prevPoint = dataSet.getPoint(0);

            for (int i = 1; i < dataSet.getNumberPoints(); i++) {
                double[] currPoint = dataSet.getPoint(i);
                double x = (prevPoint[0] + currPoint[0]) / 2;
                double y = (currPoint[1] - prevPoint[1]) / (currPoint[0] - prevPoint[0]);
                //System.out.println("Adding point ("+x+","+y+")");
                ds.addPoint(x, y);
                prevPoint = currPoint;
            }

            //ds.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);

            //ds.set

            PlotterFrame frame = PlotManager.getPlotterFrame(ds.getReference(), false, true);
            //frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
            frame.addDataSet(ds);

        }
    }

    public class DataSetPhasePlanePlotMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetPhasePlanePlotMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {

            String newYUnit = "";
            String newYLegend = "";

            if (dataSet.getYUnit().length() > 0 && dataSet.getXUnit().length() > 0) {
                newYUnit = "(" + dataSet.getYUnit() + ") / (" + dataSet.getXUnit() + ")";
            }

            if (dataSet.getYLegend().length() > 0) {
                newYLegend = "Derivative of " + dataSet.getYLegend();
            }

            DataSet ds = new DataSet("Phase plane plot of " + dataSet.getReference(),
                    "Phase plane plot of \n" + dataSet.getDescription(),
                    dataSet.getYUnit(),
                    newYUnit,
                    dataSet.getYLegend(),
                    newYLegend);

            ds.setGraphFormat(PlotCanvas.USE_LINES_FOR_PLOT);
            ds.setGraphColour(new Color(204, 0, 102));

            double[] prevPoint = dataSet.getPoint(0);

            for (int i = 1; i < dataSet.getNumberPoints(); i++) {
                double[] currPoint = dataSet.getPoint(i);

                double newX = (prevPoint[1] + currPoint[1]) / 2;
                double newY = (currPoint[1] - prevPoint[1]) / (currPoint[0] - prevPoint[0]);

                //System.out.println("Adding point ("+x+","+y+")");
                ds.addPoint(newX, newY);
                prevPoint = currPoint;
            }

            PlotterFrame frame = PlotManager.getPlotterFrame(ds.getReference(), false, true);
            //frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
            frame.addDataSet(ds);

        }
    }

    public class DataSetFormatMenuListener implements ActionListener {





        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetFormatMenuListener(DataSet dataSet, PlotterFrame plotFrame) {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e) {
            logger.logComment("Action performed on DataSetFormatMenuListener");
            // System.out.println("Action: " + e);

            String selected = ((JMenuItem) e.getSource()).getText();

            if (selected.startsWith(formatMenuIndicator)) {
                selected = selected.substring(formatMenuIndicator.length());
            }

            if (selected != null) {
                dataSet.setGraphFormat(selected);
                plotFrame.updateMenus();
                // will hopefully have solved the problem, otherwise another
                // warning at repaint...
                plotFrame.removeProblemDueToBarSpacing();
            }

        }

        ;
    }

    public class DataSetZerosMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetZerosMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuffer sb = new StringBuffer();

            sb.append("// Interpolated zeros of     : " + dataSet.getReference() + "\n");

            sb.append("// Number of points: " + dataSet.getNumberPoints() + "\n");
            sb.append("// Max X: " + dataSet.getMaxX()[0] + "\n");
            sb.append("// Min X: " + dataSet.getMinX()[0] + "\n");
            sb.append("// Max Y: " + dataSet.getMaxY()[1] + "\n");
            sb.append("// Min Y: " + dataSet.getMinY()[1] + "\n\n");

            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            boolean pos = (yVals[0] > 0);

            StringBuffer zeros = new StringBuffer();
            int count = 0;

            for (int j = 1; j < xVals.length; j++) {
                if (yVals[j] == 0) {
                    zeros.append(xVals[j] + "\n");
                    count++;
                } else if (pos && yVals[j] < 0) {
                    double zero = xVals[j - 1] + ((xVals[j] - xVals[j - 1]) * (-1 * yVals[j - 1] / (yVals[j] - yVals[j - 1])));

                    zeros.append(zero + " // + -> -\n");
                    pos = false;
                    count++;
                } else if (!pos && yVals[j] > 0) {
                    double zero = xVals[j - 1] + ((xVals[j] - xVals[j - 1]) * (-1 * yVals[j - 1] / (yVals[j] - yVals[j - 1])));
                    zeros.append(zero + " // - -> +\n");
                    pos = true;
                    count++;
                }
            }

            sb.append("// There are " + count + " zeros in the data set:\n\n");
            sb.append(zeros.toString());

            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                    "Zeros of Data Set: "
                    + dataSet.getReference(),
                    12,
                    false,
                    false);

            simpleViewer.setFrameSize(700, 700);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }

            simpleViewer.setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);

        }

        ;
    }

    public class DataSetAreaMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetAreaMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuffer sb = new StringBuffer();


            sb.append("Simple area under Data Set     : " + dataSet.getReference() + "\n");


            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            //StringBuffer zeros = new StringBuffer();
            //int count = 0;
            double totalarea = 0;

            for (int j = 1; j < xVals.length; j++) {
                double base = xVals[j] - xVals[j - 1];
                if (base < 0) {
                    GuiUtils.showErrorMessage(logger,
                            "The set of points are not sequential, and therefore the area cannot be calculated.", null, null);
                    return;
                }
                totalarea = totalarea + (base * yVals[j - 1]);
                totalarea = totalarea + (base * (0.5 * (yVals[j] - yVals[j - 1])));

            }

            sb.append("The area under the Data Set is: " + totalarea);
            //sb.append(zeros.toString())

            showSmallInfoBox("Area under Data Set: "
                    + dataSet.getReference(), sb.toString());

        }

        ;
    }

    public class DataSetAbsAreaMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetAbsAreaMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuffer sb = new StringBuffer();


            sb.append("Absolute area under Data Set     : " + dataSet.getReference() + "\n");


            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            //StringBuffer zeros = new StringBuffer();
            //int count = 0;
            double totalarea = 0;

            for (int j = 1; j < xVals.length; j++) {
                double base = xVals[j] - xVals[j - 1];
                if (base < 0) {
                    GuiUtils.showErrorMessage(logger,
                            "The set of points are not sequential, and therefore the absolute area cannot be calculated.", null, null);
                    return;
                }
                double thisArea = 0;

                if ((yVals[j] >= 0 && yVals[j - 1] >= 0)
                        || (yVals[j] < 0 && yVals[j - 1] < 0)) {
                    thisArea = (base * yVals[j - 1]);
                    thisArea = thisArea + (base * (0.5 * (yVals[j] - yVals[j - 1])));
                    thisArea = Math.abs(thisArea);
                    //System.out.println("thisArea:: "+thisArea);
                } else {
                    double zeroY = xVals[j - 1] + (yVals[j - 1] * (base / (yVals[j - 1] - yVals[j])));

                    //System.out.println("zeroY: "+zeroY);
                    thisArea = Math.abs(0.5 * (yVals[j - 1] * (zeroY - xVals[j - 1])));
                    //System.out.println("thisArea:: "+thisArea);

                    thisArea = thisArea + Math.abs(0.5 * (yVals[j] * (xVals[j] - zeroY)));

                    //System.out.println("thisArea:: "+thisArea);
                }

                totalarea = totalarea + thisArea;

            }

            sb.append("The absolute area under the Data Set is: " + totalarea);
            //sb.append(zeros.toString())

            showSmallInfoBox("Area under Data Set: "
                    + dataSet.getReference(), sb.toString());


        }

        ;
    }

    public class DataSetPeaksMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetPeaksMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            String req = "Please enter the cutoff threshold which will be considered a spike.\n"
                    + "Only maxima in the data above this value will be considered peaks";

            float suggestedThresh = -20;
            if (PlotterFrame.preferredSpikeValsEntered) {
                suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();
            }

            String thresh = JOptionPane.showInputDialog(req, "" + suggestedThresh);

            if (thresh == null) {
                return; // i.e. cancelled
            }
            float threshold = 0;
            try {
                threshold = Float.parseFloat(thresh);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                return;
            }

            StringBuffer sb = new StringBuffer();


            sb.append("// Times of peaks above " + threshold + "(m)V of     : " + dataSet.getReference() + "\n");


            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            //boolean pos = (yVals[0] > 0);

            StringBuffer peaks = new StringBuffer();
            int count = 0;

            for (int j = 2; j < xVals.length; j++) {
                if (yVals[j - 2] < yVals[j - 1] && yVals[j] <= yVals[j - 1]
                        && yVals[j - 1] >= threshold) {
                    peaks.append(xVals[j - 1] + "\n");
                    count++;

                }

                /*
                if (yVals[j]==0)
                {
                zeros.append(xVals[j] + "\n");
                count++;
                }
                else if (pos && yVals[j]<0)
                {
                double zero = xVals[j-1] + ( (xVals[j]-xVals[j-1])*  (-1*yVals[j-1]/(yVals[j]-yVals[j-1])));

                zeros.append(zero + " // + -> -\n");
                pos=false;
                count++;
                }
                else if (!pos && yVals[j]>0)
                {
                double zero = xVals[j-1] + ( (xVals[j]-xVals[j-1])*  (-1*yVals[j-1]/(yVals[j]-yVals[j-1])));
                zeros.append(zero + " // - -> +\n");
                pos=true;
                count++;
                }*/
            }

            sb.append("// There are " + count + " peaks in the data set:\n\n");
            sb.append(peaks.toString());

            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                    "Peaks of graph: "
                    + dataSet.getReference(),
                    12,
                    false,
                    false);

            simpleViewer.setFrameSize(700, 700);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }

            simpleViewer.setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);

        }

        ;
    }

    public class DataSetListPointsMenuListener implements ActionListener {

        DataSet dataSet = null;

        public DataSetListPointsMenuListener(DataSet dataSet) {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e) {
            StringBuffer sb = new StringBuffer();


            sb.append("// Data Set        : " + dataSet.getReference() + "\n");
            if (dataSet.getDescription() != null) {
                sb.append("// Description : " + dataSet.getDescription() + "\n");
            }

            sb.append("// Number of points: " + dataSet.getNumberPoints() + "\n");
            sb.append("// Max X: " + dataSet.getMaxX()[0] + " (y value: " + dataSet.getMaxX()[1] + ") \n");
            sb.append("// Min X: " + dataSet.getMinX()[0] + " (y value: " + dataSet.getMinX()[1] + ") \n");
            sb.append("// Max Y: " + dataSet.getMaxY()[1] + " (x value: " + dataSet.getMaxY()[0] + ") \n");
            sb.append("// Min Y: " + dataSet.getMinY()[1] + " (x value: " + dataSet.getMinY()[0] + ") \n");

            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            for (int j = 0; j < xVals.length; j++) {
                String xVal = (float) xVals[j] + "";
                sb.append(xVal);   // cast to float to prevent rounding issues

                if (xVal.length() < 22) {
                    //sb.append("("+(xVals[j]+"").length()+")");
                    for (int k = 0; k < 22 - (xVal.length()); k++) {
                        sb.append(" ");
                    }
                }


                String comment = dataSet.getComment(j);
                if (comment == null) {
                    comment = "";
                } else {
                    comment = DataSetManager.DATA_SET_COMMENT + " " + comment;
                }

                sb.append("  " + (float) yVals[j] + "     " + comment + "\n");// cast to float to prevent rounding issues

            }

            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                    "Values present in Data Set: "
                    + dataSet.getReference(),
                    12,
                    false,
                    false);

            simpleViewer.setFrameSize(700, 700);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }

            simpleViewer.setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);

        }

        ;
    }

    public void addSampleData() {
        Random rand = new Random();

        double minX = 0;
        double maxX = 20;
        int numPoints = 1000001;

        DataSet data1 = new DataSet("data1", "test data", "", "", "", "");
        DataSet data2 = new DataSet("data2", "more test data, more test data", "", "", "", "");
        DataSet data3 = new DataSet("data3", "333", "", "", "", "");

        data1.setUnits("ms", "mV");
        data1.setLegends("Time", "Membrane Potential");

        data1.setGraphColour(Color.blue);
        data2.setGraphColour(Color.RED);

        data1.setGraphFormat(PlotCanvas.USE_LINES_FOR_PLOT);
        data2.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT);
        data3.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT);


        for (int i = 0; i < numPoints; i++) {
            double x = minX + (maxX - minX) * ((double) i / (numPoints - 1));

            double minonezeroone = 0;//(rand.nextDouble()*2) -1;

            double period = 4.222;

            double y1 = Math.max(0, 5 * Math.cos(2 * Math.PI * x / period) - 2 + (0.5 * minonezeroone));

            double regSpike = x % (int) period == 0 ? 1 : 0;

            //double y2 = 3*Math.cos(x/.1) - 4;

            double y2 = regSpike;

            data1.addPoint(x, y1);
            data2.addPoint(x, y2);


        }
        data3.addPoint(0, 3);
        data3.addPoint(1, 3);
        data3.addPoint(2, 3);

        addDataSet(data1);
        addDataSet(data2);
        addDataSet(data3);

    }

    public void jMenuViewPointsOnly_actionPerformed(ActionEvent e) {
        plotCanvas.setViewMode(PlotCanvas.NORMAL_VIEW);
    }

    //Help | About action performed
    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            PlotManager.plotFrameClosing(plotFrameReference);

            logger.logComment("WINDOW_CLOSING, standalone: " + standAlone);
            if (standAlone) {
                System.exit(0);
            }
        }
    }

    public void setMatplotlibDir(String dir) {
        defaultMatplotlibDir = dir;
    }

    public void setMatplotlibTitle(String title) {
        defaultMatplotlibTitle = title;
    }

    public void setStandAlone(boolean st) {
        standAlone = st;
    }

    protected void setStatus(String message) {
        jLabelStatusBar.setText(message);
    }

    public void setKeepDataSetColours(boolean val) {
        this.plotCanvas.setKeepDataSetColours(val);
    }

    public void setViewMode(String viewMode) {
        if (viewMode.equals(PlotCanvas.NORMAL_VIEW)) {
            this.jMenuViewPointsOnly.setSelected(true);
        } else if (viewMode.equals(PlotCanvas.INCLUDE_ORIGIN_VIEW)) {
            this.jMenuItemViewOrigin.setSelected(true);
        } else if (viewMode.equals(PlotCanvas.STACKED_VIEW)) {
            this.jMenuItemStacked.setSelected(true);
        } else if (viewMode.equals(PlotCanvas.CROPPED_VIEW)) {
            this.jMenuItemSelection.setSelected(true);
        }
        plotCanvas.setViewMode(viewMode); // necessary??
    }

    void jMenuItemViewOrigin_actionPerformed(ActionEvent e) {
        plotCanvas.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
    }

    void jMenuItemSelection_actionPerformed(ActionEvent e) {
        plotCanvas.setViewMode(PlotCanvas.CROPPED_VIEW);
    }

    void jMenuItemStacked_actionPerformed(ActionEvent e) {
        plotCanvas.setViewMode(PlotCanvas.STACKED_VIEW);
    }

    void jMenuItemClose_actionPerformed(ActionEvent e) {
        //System.out.println("clo...");
        this.setVisible(false);
        this.dispose();
    }

    void jMenuItemCustomView_actionPerformed(ActionEvent e) {

        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();
        InputRequestElement maxx = new InputRequestElement("maxx", "Maximum X value", null,
                (float) plotCanvas.getMaxXScaleValue() + "", "");
        inputs.add(maxx);
        InputRequestElement minx = new InputRequestElement("minx", "Minimum X value", null,
                (float) plotCanvas.getMinXScaleValue() + "", "");
        inputs.add(minx);
        InputRequestElement maxy = new InputRequestElement("maxy", "Maximum Y value", null,
                (float) plotCanvas.getMaxYScaleValue() + "", "");
        inputs.add(maxy);
        InputRequestElement miny = new InputRequestElement("miny", "Minimum Y value", null,
                (float) plotCanvas.getMinYScaleValue() + "", "");
        inputs.add(miny);

        InputRequest dlg = new InputRequest(this, "Please enter the new view bounds", "Enter view bounds", inputs, true);

        GuiUtils.centreWindow(dlg);
        dlg.setVisible(true);

        if (dlg.cancelled()) {
            return;
        }

        try {
            plotCanvas.setMaxMinScaleValues(Double.parseDouble(maxx.getValue()),
                    Double.parseDouble(minx.getValue()),
                    Double.parseDouble(maxy.getValue()),
                    Double.parseDouble(miny.getValue()));
        } catch (NumberFormatException ex) {
            GuiUtils.showErrorMessage(logger, "Error setting the view", ex, this);
            return;
        }

        plotCanvas.setViewMode(PlotCanvas.USER_SET_VIEW);
    }

    void jMenuItemShowAxes_actionPerformed(ActionEvent e) {
        plotCanvas.setShowAxes(jMenuItemShowAxes.isSelected());
        if (!jMenuItemShowAxes.isSelected()) {
            plotCanvas.setShowAxisNumbering(false);
            plotCanvas.setShowAxisTicks(false);
            jMenuItemShowAxisNums.setEnabled(false);
            jMenuItemShowAxisTicks.setEnabled(false);
        } else {
            jMenuItemShowAxisNums.setEnabled(true);
            jMenuItemShowAxisTicks.setEnabled(true);
            jMenuItemShowAxisNums_actionPerformed(null);
            jMenuItemShowAxisTicks_actionPerformed(null);
        }

        logger.logComment("jMenuItemShowAxes_actionPerformed, repainting");
        plotCanvas.repaint();
    }

    void jMenuItemAllOneColour_actionPerformed(ActionEvent e) {

        logger.logComment("----   Setting " + plotCanvas.getDataSets().length + " data sets to the same colour...", true);

        Color c = JColorChooser.showDialog(this,
                "Please choose a colour for ALL of the data sets",
                Color.black);
        if (c != null) {
            for (DataSet ds : plotCanvas.getDataSets()) {
                ds.setGraphColour(c);
            }
        }
        plotCanvas.repaint();
    }

    void jMenuDifference_actionPerformed(ActionEvent e) {
        logger.logComment("----   Getting difference between " + plotCanvas.getDataSets().length + " data sets...");

        PlotterFrame frame = PlotManager.getPlotterFrame("Difference between Data Sets in " + this.getTitle(), false, true);

        for (int firstDataSetIndex = 0; firstDataSetIndex < plotCanvas.dataSets.length; firstDataSetIndex++) {
            logger.logComment("firstDataSetIndex: " + firstDataSetIndex);
            for (int secondDataSetIndex = firstDataSetIndex + 1; secondDataSetIndex < plotCanvas.dataSets.length; secondDataSetIndex++) {
                logger.logComment("secondDataSetIndex: " + secondDataSetIndex);
                logger.logComment("plotCanvas.dataSets.length: " + plotCanvas.dataSets.length);

                DataSet data0 = plotCanvas.dataSets[firstDataSetIndex];
                DataSet data1 = plotCanvas.dataSets[secondDataSetIndex];

                String name = data0.getReference() + " minus " + data1.getReference();

                String desc = "  *** Data Set with description: ***\n" + data0.getDescription()
                        + "\n  ***has had the following subtracted from it: ***\n" + data1.getDescription();

                DataSet dataSet = new DataSet(name, desc, "", "", "", "");

                for (int pointIndex = 0; pointIndex < data0.getNumberPoints(); pointIndex++) {

                    if (data1.getNumberPoints() > pointIndex
                            && data1.getPoint(pointIndex)[0] == data0.getPoint(pointIndex)[0]) {
                        dataSet.addPoint(data0.getPoint(pointIndex)[0],
                                data0.getPoint(pointIndex)[1]
                                - data1.getPoint(pointIndex)[1]);
                    }
                }
                if (dataSet.getNumberPoints() == 0) {
                    GuiUtils.showErrorMessage(logger, "Note: data sets " + data0.getReference()
                            + " and " + data1.getReference()
                            + " have no x values in common and so cannot be subtracted", null, this);
                }
                frame.addDataSet(dataSet);

            }
        }
    }

    void jMenuApAnalysis_actionPerformed(ActionEvent e) {

        logger.logComment("Plotting AP shapes...");
        String name = "AP shapes from " + plotCanvas.getDataSets().length + " Data Sets in " + this.getTitle();
        PlotterFrame frame = PlotManager.getPlotterFrame(name, false, true);



        DataSet ds0 = plotCanvas.dataSets[0];

        double[] xVals = ds0.getXValues();
        double[] allYVals = ds0.getYValues();

        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

        String req = "Threshold for spike";

        float suggestedThresh = -20;
        if (PlotterFrame.preferredSpikeValsEntered) {
            suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();
        }
        InputRequestElement threshInput = new InputRequestElement("threshold", req, null, suggestedThresh + "", "mV");
        inputs.add(threshInput);


        req = "Start time from which to analyse the spiking";
        float suggestedStart = (float) ds0.getMinX()[0];
        if (PlotterFrame.preferredSpikeValsEntered) {
            suggestedStart = plotCanvas.getSpikeOptions().getStartTime();
        }

        InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart + "", "ms");
        inputs.add(startInput);



        req = "Finish time from which to analyse the spiking";
        float suggestedEnd = (float) ds0.getMaxX()[0];
        if (PlotterFrame.preferredSpikeValsEntered) {
            suggestedEnd = plotCanvas.getSpikeOptions().getStopTime();
        }

        InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd + "", "ms");
        inputs.add(stopInput);

        InputRequest dlg = new InputRequest(null, "Please enter the parameters for calculating spiking statistics", "Parameters for spiking statistics", inputs, true);

        GuiUtils.centreWindow(dlg);

        dlg.setVisible(true);


        if (dlg.cancelled()) {
            return;
        }




        if (threshInput.getValue() == null) {
            return; // i.e. cancelled
        }
        float threshold = 0;
        try {
            threshold = Float.parseFloat(threshInput.getValue());
        } catch (Exception ex) {
            GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
            return;
        }
        plotCanvas.getSpikeOptions().setThreshold(threshold);

        if (startInput.getValue() == null) {
            return; // i.e. cancelled
        }
        float startTime = 0;
        try {
            startTime = Float.parseFloat(startInput.getValue());
        } catch (Exception ex) {
            GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
            return;
        }
        plotCanvas.getSpikeOptions().setStartTime(startTime);


        if (stopInput.getValue() == null) {
            return; // i.e. cancelled
        }

        float stopTime = 1000;
        try {
            stopTime = Float.parseFloat(stopInput.getValue());
        } catch (Exception ex) {
            GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
            return;
        }
        plotCanvas.getSpikeOptions().setStopTime(stopTime);

        PlotterFrame.preferredSpikeValsEntered = true;


        // prepare the tables where we will store the spike times, the ISIs and the new DataSets
        double[][] spikeTimesAoa = new double[plotCanvas.dataSets.length][];
        ArrayList[] interSpikeIntervalsAoa = new ArrayList[plotCanvas.dataSets.length];
        DataSet[][] spikeShapes = new DataSet[plotCanvas.dataSets.length][];

        // loop over the existing datasets
        for (int dataSetIndex = 0; dataSetIndex < plotCanvas.dataSets.length; dataSetIndex++) {
            String desc = name + dataSetIndex;
            DataSet nextDs = plotCanvas.dataSets[dataSetIndex];

            // save the spiketimes for this cell in the appropriate item of the spikeTimesAoa array of arrays
            spikeTimesAoa[dataSetIndex] = SpikeAnalyser.getSpikeTimes(nextDs.getYValues(),
                    nextDs.getXValues(),
                    threshold,
                    startTime,
                    stopTime);
            // do the same for the ISIs
            interSpikeIntervalsAoa[dataSetIndex] = SpikeAnalyser.getInterSpikeIntervals(nextDs.getYValues(),
                    nextDs.getXValues(),
                    threshold,
                    startTime,
                    stopTime);

            /*
            System.out.print(Arrays.toString(spikeTimesAoa[dataSetIndex]) + "\n");
            System.out.print(interSpikeIntervalsAoa[dataSetIndex].toString() + "\n\n");*/

            spikeShapes[dataSetIndex] = new DataSet[spikeTimesAoa[dataSetIndex].length];

            // loop over all the spikes in this dataset EXCEPT the first and the last
            for (int j = 1; j < spikeTimesAoa[dataSetIndex].length - 1; j++) {
                // create a new dataset to store the information on the single AP profile
                spikeShapes[dataSetIndex][j] = new DataSet(name, desc, ds0.getXUnit(), ds0.getYUnit(), ds0.getXLegend(), ds0.getYLegend());

                // calculate the starting and ending point for the AP profile trace
                double beginning = spikeTimesAoa[dataSetIndex][j] - (Double) interSpikeIntervalsAoa[dataSetIndex].get(j - 1) / 2;
                double end = spikeTimesAoa[dataSetIndex][j] + (Double) interSpikeIntervalsAoa[dataSetIndex].get(j) / 2;

                // iterate over the trace points for this cell and add them to this dataset if they are between beginning and end
                for (int k = 0; k < nextDs.getXValues().length; k++) {
                    if (nextDs.getXValues()[k] > beginning && nextDs.getXValues()[k] < end) {
                        spikeShapes[dataSetIndex][j].addPoint(nextDs.getXValues()[k] - spikeTimesAoa[dataSetIndex][j], nextDs.getYValues()[k]);
                    }
                    if (nextDs.getXValues()[k] > end){
                        break; //this should speed things up a bit
                    }
                }
                frame.addDataSet(spikeShapes[dataSetIndex][j]);

            }

        }
        {
        }




    }

    void jMenuAverage_actionPerformed(ActionEvent e) {

        logger.logComment("----   Getting average of " + plotCanvas.getDataSets().length + " data sets...");

        String name = "Average of " + plotCanvas.getDataSets().length + " Data Sets in " + this.getTitle();

        DataSet ds0 = plotCanvas.dataSets[0];

        double[] xVals = ds0.getXValues();
        double[] allYVals = ds0.getYValues();

        for (int dataSetIndex = 1; dataSetIndex < plotCanvas.dataSets.length; dataSetIndex++) {
            DataSet nextDs = plotCanvas.dataSets[dataSetIndex];
            double[] nextXVals = nextDs.getXValues();
            if (!Arrays.equals(nextXVals, xVals)) {
                GuiUtils.showErrorMessage(logger,
                        "Error, all Data Sets in the Plot Frame must have the same X values to get an average of the Data Sets", null, this);
                return;
            }
            double[] nextYVals = nextDs.getYValues();

            for (int i = 0; i < nextYVals.length; i++) {
                allYVals[i] += nextYVals[i];

                if (dataSetIndex == plotCanvas.dataSets.length - 1) {
                    logger.logComment("Final val at " + i + ": " + nextYVals[i] + ", total: " + allYVals[i]);
                    allYVals[i] = allYVals[i] / (float) plotCanvas.dataSets.length;
                    logger.logComment("Average: " + allYVals[i]);
                }
            }
        }

        PlotterFrame frame = PlotManager.getPlotterFrame(name, false, true);

        String desc = name;

        DataSet dataSet = new DataSet(name, desc, ds0.getXUnit(), ds0.getYUnit(), ds0.getXLegend(), ds0.getYLegend());
        for (int i = 0; i < xVals.length; i++) {
            dataSet.addPoint(xVals[i], allYVals[i]);
        }
        frame.addDataSet(dataSet);

    }

    void jMenuGenerateMatplotlib_actionPerformed(ActionEvent e) {
        generateMatplotlib();
        
    }

    public void generateMatplotlib() {
        String lastDir = recentFiles.getMyLastExportPointsDir();
        File matplotlibFilesDir = null;

        if (defaultMatplotlibDir.equals("")) {  // we want to bring up a dialog for the file

            if (lastDir == null) {
                if (project == null) {
                    lastDir = System.getProperty("user.dir");
                } else {
                    lastDir = project.getProjectMainDirectory().getAbsolutePath();
                }
            }
            File defaultDir = new File(lastDir);

            final JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(defaultDir);
            chooser.setDialogTitle("Choose a location for the matplotlib files to generate the EPS image file");

            int retval = chooser.showDialog(this, "Choose directory");

            logger.logComment("retval: " + retval + "_" + JOptionPane.CANCEL_OPTION + ", curr dir: " + chooser.getCurrentDirectory() + ", curr file: " + chooser.getSelectedFile());

            if (retval != JOptionPane.CANCEL_OPTION) {
                defaultDir = chooser.getSelectedFile();
                String dir = GeneralUtils.replaceAllTokens(getPlotFrameReference(), "/", "_");
                String plotRef = getCompactFilename(dir);

                matplotlibFilesDir = new File(defaultDir, plotRef);

            } else {
                // cancelled dialog.. not sure what we do
                return;
            }

        } else { // we want to quietly use defaultMatplotlibDir to generate

            matplotlibFilesDir = new File(defaultMatplotlibDir);
            
            if (matplotlibFilesDir.exists()) {
                // no overwriting so we want to exit
                return;
            }

        }

        
        StringBuffer mainScript = new StringBuffer("# This is a matplotlib file generated by neuroConstruct version " + GeneralProperties.getVersionNumber() + "\n"
                + "# This file will generate an EPS file when run using: \n\n"
                + "#    python " + ProjectStructure.getMatplotlibEpsFilename() + "\n\n"
                + "# matplotlib is available for Linux, Mac and Windows from http://matplotlib.sourceforge.net\n\n");


        mainScript.append("import matplotlib.pyplot as plt\n");
        mainScript.append("from pylab import *\n");
        mainScript.append("from matplotlib.backends.backend_agg import FigureCanvasAgg as FigureCanvas\n\n");
        mainScript.append("showFlag = 1\n");
        mainScript.append("for arg in sys.argv:\n");
        mainScript.append("    if (arg == '-noshow'):\n");
        mainScript.append("        showFlag = 0\n");
        
        matplotlibFilesDir.mkdir();

        //Hashtable<String, String> dataVsLoadScript = new Hashtable<String, String>();


        mainScript.append("fig = plt.figure(facecolor='#FFFFFF', edgecolor='#FFFFFF')\n");
        mainScript.append("p = fig.add_subplot(111, autoscale_on=False, xlim=(" + this.plotCanvas.getMinXScaleValue() + "," + this.plotCanvas.getMaxXScaleValue() + "),"
                + " ylim=(" + this.plotCanvas.getMinYScaleValue() + "," + this.plotCanvas.getMaxYScaleValue() + "))\n");

        for (int j = 0; j < plotCanvas.getDataSets().length; j++) {
            DataSet ds = plotCanvas.getDataSets()[j];
            String dsFileRef = GeneralUtils.replaceAllTokens(getCompactFilename(ds.getReference()), "/", "_");
            File dsFile = new File(matplotlibFilesDir, dsFileRef + ".dat");

            logger.logComment("Going to write file: " + dsFile);
            try {
                float res = 2000;
                double minXdist = Math.abs((ds.getMaxX()[0] - ds.getMinX()[0]) / res);

                double minYdist = Math.abs((ds.getMaxY()[1] - ds.getMinY()[1]) / res);

                FileWriter fw = new FileWriter(dsFile);
                fw.write("# Data from : " + dsFileRef + "\n");
                fw.write("# NOTE!!! Not all points have necessarily been exported!\n");
                fw.write("# Only enough points are here to ensure resolution of 1/" + res + " between max & min x & y values.\n");

                double[] lastP = new double[]{Float.MIN_VALUE, Float.MIN_VALUE};
                int count = 0;

                for (int i = 0; i < ds.getNumberPoints(); i++) {
                    double[] p = ds.getPoint(i);

                    if (Math.abs(p[0] - lastP[0]) > minXdist || Math.abs(p[1] - lastP[1]) > minYdist) {
                        fw.write((float) p[0] + " " + (float) p[1] + "\n");
                        count++;
                        lastP = p;
                    }
                }
                logger.logComment("Written " + count + " out of points: " + ds.getNumberPoints());


                fw.close();

                String rgb = Integer.toHexString(ds.getGraphColour().getRGB());
                rgb = rgb.substring(2, rgb.length());
                //StringBuffer sb = new StringBuffer();
                String name = dsFile.getName();

                name = GeneralUtils.replaceAllTokens(name, "\\", "\\\\");
                //if (j>0) mainScript.append(", ");

                mainScript.append("file_" + j + " = open('" + name + "','r')\n\n");
                String objRef = ds.getSafeReference();

                mainScript.append(objRef + "_x = []\n");
                mainScript.append(objRef + "_y = []\n\n");

                mainScript.append("for line in file_" + j + ":\n");

                mainScript.append("    if not line.strip().startswith('#'):\n");
                mainScript.append("        data = line.split()\n");

                mainScript.append("        " + objRef + "_x.append(float(data[0]))\n");
                mainScript.append("        " + objRef + "_y.append(float(data[1]))\n\n");

                String linestyle = "-";
                String marker = ".";
                String pre = "";
                String post = "";

                if (ds.getGraphFormat().equals(PlotCanvas.USE_LINES_FOR_PLOT)) {
                    linestyle = "-";
                    marker = "None";
                } else if (ds.getGraphFormat().equals(PlotCanvas.USE_THICK_LINES_FOR_PLOT)) {
                    linestyle = "-";
                    marker = "None";
                    pre = ", linewidth=3";
                } else if (ds.getGraphFormat().equals(PlotCanvas.USE_POINTS_FOR_PLOT)) {
                    linestyle = "dotted";
                    marker = "None";
                } else if (ds.getGraphFormat().equals(PlotCanvas.USE_CIRCLES_FOR_PLOT)) {
                    linestyle = "None";
                    marker = "o";
                    post = ", markerfacecolor='None', markeredgecolor='#" + rgb + "'";
                } else if (ds.getGraphFormat().equals(PlotCanvas.USE_CROSSES_FOR_PLOT)) {
                    linestyle = "None";
                    marker = "x";
                } else if (ds.getGraphFormat().equals(PlotCanvas.USE_BARCHART_FOR_PLOT)) {
                    linestyle = "None";
                    marker = "-";
                }  // TODO, more for barcharts!

                mainScript.append("\np.spines['top'].set_color('none')\n"); // show only bottom and left axis
                mainScript.append("p.spines['right'].set_color('none')\n");
                mainScript.append("p.plot(" + objRef + "_x, " + objRef + "_y" + pre + ", solid_joinstyle ='round', solid_capstyle ='round', color='#" + rgb + "', linestyle='" + linestyle + "', marker='" + marker + "'" + post + ")\n\n");

                if (this.defaultMatplotlibDir != "")
                {
                    mainScript.append("\ntitle('"+this.defaultMatplotlibTitle+"')\n");
                }

            } catch (IOException ex) {
                GuiUtils.showErrorMessage(logger, "Error creating file: " + dsFile
                        + " for generating EPS with matplotlib", ex, this);
                return;

            }
        }

        DataSet dsToUse = plotCanvas.getDataSets()[0];

        if (dsToUse.getYLegend() != null && dsToUse.getYLegend().length() > 0) {
            if (dsToUse.getYUnit() != null && dsToUse.getYUnit().length() > 0) {
                mainScript.append("p.set_ylabel('" + dsToUse.getYLegend() + " (" + Unit.getSafeString(dsToUse.getYUnit()) + ")', fontsize=14)\n");
            } else {
                mainScript.append("p.set_ylabel('" + dsToUse.getYLegend() + "', fontsize=14)\n");
            }
        }

        if (dsToUse.getXLegend() != null && dsToUse.getXLegend().length() > 0) {
            if (dsToUse.getXUnit() != null && dsToUse.getXUnit().length() > 0) {
                mainScript.append("p.set_xlabel('" + dsToUse.getXLegend() + " (" + Unit.getSafeString(dsToUse.getXUnit()) + ")', fontsize=14)\n");
            } else {
                mainScript.append("p.set_xlabel('" + dsToUse.getXLegend() + "', fontsize=14)\n");
            }
        }

        //mainScript.append("fig.set_figheight("+this.getSize().height+"*0.03)  # Approx height in inches\n");
        //mainScript.append("fig.set_figwidth("+this.getSize().width+"*0.03)  # Approx width in inches\n");


        mainScript.append("p.get_axes().get_yaxis().tick_left()\n");
        mainScript.append("p.get_axes().get_xaxis().tick_bottom()\n");

        mainScript.append("canvas = FigureCanvas(fig)\n");

        // plotter output formats
        mainScript.append("canvas.print_eps('plot.eps')\n");
        mainScript.append("canvas.print_pdf('plot.pdf')\n");
        mainScript.append("canvas.print_png('plot.png')\n");

        mainScript.append("if (showFlag == 1): plt.show()\n");
        mainScript.append("\n");

        File mainMatplotlibFile = new File(matplotlibFilesDir, ProjectStructure.getMatplotlibEpsFilename());

        logger.logComment("Going to write file: " + mainMatplotlibFile);
        try {
            FileWriter fw = new FileWriter(mainMatplotlibFile);
            fw.write(mainScript.toString());
            fw.close();

        } catch (IOException ex) {
            GuiUtils.showErrorMessage(logger, "Error creating file: " + mainMatplotlibFile
                    + " for generating EPS with matplotlib", ex, this);
            return;

        }


    }

    private String getCompactFilename(String filename) {
        String compact = GeneralUtils.replaceAllTokens(filename, " ", "_");
        compact = GeneralUtils.replaceAllTokens(compact, "(", "");
        compact = GeneralUtils.replaceAllTokens(compact, ")", "");
        compact = GeneralUtils.replaceAllTokens(compact, "-", "_");
        compact = GeneralUtils.replaceAllTokens(compact, ":", "_");
        compact = GeneralUtils.replaceAllTokens(compact, "+", "_");
        if (compact.length() > 42) {
            compact = compact.substring(0, 20) + "___" + compact.substring(compact.length() - 20, compact.length());
        }
        return compact;
    }

    void jMenuImportData_actionPerformed(ActionEvent e) {
        String lastDir = recentFiles.getMyLastExportPointsDir();

        if (lastDir == null) {
            if (project == null) {
                lastDir = System.getProperty("user.dir");
            } else {
                lastDir = project.getProjectMainDirectory().getAbsolutePath();
            }
        }

        File defaultDir = new File(lastDir);

        ArrayList<DataSet> dataSets = addNewDataSet(defaultDir, this);

        if (dataSets != null) {
            for (DataSet ds : dataSets) {
                this.addDataSet(ds);
            }
        }
    }

    public static void addHDF5DataSets(File dirToLookIn, Component parent) {
        final JFileChooser chooser = new JFileChooser();

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);


        chooser.setCurrentDirectory(dirToLookIn);
        logger.logComment("Set Dialog dir to: " + dirToLookIn.getAbsolutePath());

        chooser.setDialogTitle("Choose a HDF5 file seperated data points");

        JButton helpButton = new JButton("Data file format info");
        JPanel helpPanel = new JPanel();
        helpButton.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        GuiUtils.showInfoMessage(logger,
                                "Data file import format",
                                "The HDF5 file will be parsed for Datasets/arrays of values and these can be plotted....\n"
                                + "Note: after the data points are imported the x axis values (or y axis values) can be corrected (e.g. scaled by 1000)\n"
                                + "by selecting " + PlotterFrame.generateNew + " in the Data Set's menu", chooser);
                    }
                });

        helpPanel.add(helpButton);

        chooser.setAccessory(helpPanel);


        int retval = chooser.showDialog(parent, "Choose HDF5 file");


        if (retval == JOptionPane.OK_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                String ref = file.getName();
                if (ref.lastIndexOf(".") > 0) {
                    ref = ref.substring(0, ref.lastIndexOf("."));
                }

                RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename()).setMyLastExportPointsDir(file.getAbsolutePath());


                HDF5ChooserDialog dialog = new HDF5ChooserDialog(new javax.swing.JFrame(), false, file);

                GuiUtils.centreWindow(dialog);

                dialog.setVisible(true);

                /*

                H5File h5 = Hdf5Utils.openH5file(file);
                
                Group g = Hdf5Utils.getRootGroup(h5);
                
                boolean plotToo = true;
                
                ArrayList<DataSet> dataSets = Hdf5Utils.parseGroupForDatasets(g, plotToo);
                
                StringBuffer summary = new StringBuffer("Number of DataSets found in file: "+ file.getAbsolutePath()+": "+dataSets.size()+"\n\n");
                for(DataSet ds: dataSets)
                {
                summary.append(ds.toString()+"\n"+ds.getDescription()+"\n\n");
                }
                
                if (!plotToo)
                {
                SimpleViewer.showString(summary.toString(), "Info found", 12, false, false);
                }
                else
                {
                PlotterFrame frame = PlotManager.getPlotterFrame("Data from file: " + file.getAbsolutePath(), false, false);
                for (DataSet ds : dataSets)
                {
                frame.addDataSet(ds);
                }
                frame.setVisible(true);
                }
                 */

            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Problem opening HDF5 file: " + file, ex, null);
            }


        }


    }

    public static ArrayList<DataSet> addNewDataSet(File dirToLookIn, Component parent) {
        final JFileChooser chooser = new JFileChooser();

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);

        /*
        String lastCellMechDir = recentFiles.getMyLastExportPointsDir();

        if (lastCellMechDir == null) lastCellMechDir
        = project.getProjectMainDirectory().getAbsolutePath();

        File defaultDir = new File(lastCellMechDir);*/

        chooser.setCurrentDirectory(dirToLookIn);
        logger.logComment("Set Dialog dir to: " + dirToLookIn.getAbsolutePath());

        chooser.setDialogTitle("Choose a file containing space or comma seperated data points");

        JButton helpButton = new JButton("Data file format info");
        JPanel helpPanel = new JPanel();
        helpButton.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        GuiUtils.showInfoMessage(logger,
                                "Data file import format",
                                "Data files can be imported as either a single column (y values) or multiple column ((x and) multiple y values) format.\n"
                                + "If there are 2 or more columns in the file, an option will be given to use the first column as the x values, or treat all columns as y values.\n"
                                + "Each data entry must be followed by a carriage return, and lines not recognised as a sequence of numbers\n"
                                + "separated by a space (or one of , : ;) are ignored.\n"
                                + "Note: after the data points are imported the x axis values (or y axis values) can be corrected (e.g. scaled by 1000)\n"
                                + "by selecting " + PlotterFrame.generateNew + " in the Data Set's menu. Also Data Sets can be moved between Plot Frames.", chooser);
                    }
                });
        helpPanel.add(helpButton);

        chooser.setAccessory(helpPanel);


        int retval = chooser.showDialog(parent, "Choose data file");


        if (retval == JOptionPane.OK_OPTION) {
            File file = chooser.getSelectedFile();
            String ref = file.getName();
            if (ref.lastIndexOf(".") > 0) {
                ref = ref.substring(0, ref.lastIndexOf("."));
            }

            RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename()).setMyLastExportPointsDir(file.getAbsolutePath());
            ArrayList<DataSet> dss = null;
            try {
                dss = DataSetManager.loadFromDataSetFile(file, false, DataSetManager.DataReadFormat.UNSPECIFIED);
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Error loading data from file: " + file, ex, null);
                for (DataSet ds : dss) {
                    ds.setDescription(ds.getDescription() + "\n\nError loading data from file: " + file + "\n" + ex.getMessage());
                }
            }

            return dss;

        }

        return null;

    }

    void jMenuAddManual_actionPerformed(ActionEvent e) {

        int numPoints = 101;
        if (plotCanvas.dataSets.length > 0) {
            numPoints = plotCanvas.dataSets[0].getNumberPoints();
        }


        float min = 0;
        float max = 10;

        if (plotCanvas.getDataSets().length > 0) {
            min = (float) plotCanvas.getMinXval();
            max = (float) plotCanvas.getMaxXval();
        }

        this.addDataSet(addManualPlot(numPoints,
                min,
                max,
                this));
    }

    public static DataSet addManualPlot(int numPoints, float minVal, float maxVal, Frame parent) {

        PlotEquationDialog dlg = new PlotEquationDialog(parent,
                "Please enter details to generate a new Data Set",
                true,
                minVal,
                maxVal,
                numPoints);

        Dimension dlgSize = dlg.getPreferredSize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        dlg.setLocation((screenSize.width - dlgSize.width) / 2,
                (screenSize.height - dlgSize.height) / 2);
        dlg.setModal(true);
        dlg.pack();

        dlg.setVisible(true);

        return dlg.getGeneratedDataSet();

        //updateMenus();


    }

    void jMenuItemShowAxisNums_actionPerformed(ActionEvent e) {
        logger.logComment("jMenuItemShowAxisNums_actionPerformed...");
        plotCanvas.setShowAxisNumbering(jMenuItemShowAxisNums.isSelected());
        plotCanvas.repaint();
    }

    void jMenuItemShowAxisTicks_actionPerformed(ActionEvent e) {
        plotCanvas.setShowAxisTicks(jMenuItemShowAxisTicks.isSelected());
        plotCanvas.repaint();
    }

    public void rasterise(RasterOptions newRasterOptions) {
        rasterised = true;
        jMenuItemRasterise.setSelected(true);
        jMenuItemShowAxes.setSelected(false);
        jMenuItemShowAxes_actionPerformed(null);
        this.setViewMode(PlotCanvas.STACKED_VIEW);

        plotCanvas.setRasterOptions(newRasterOptions);
        plotCanvas.repaint();

    }

    public static void main(String[] args) {

        try {
            // UIManager.setLookAndFeel(favouredLookAndFeel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Using look and feel: " + UIManager.getLookAndFeel().getDescription());

        new PlotterApp();


        if (true) {
            return;
        }
        String dir = "../models/MaexDeSchutter/Gran_layer_1D/results/";

        File[] results = new File[]{new File(dir + "granule_cells_test.history"),
            new File(dir + "mossy_fibers_test.history"),
            new File(dir + "Golgi_cells_test.history")};

        float startTime = 0;
        float endTime = 0;

        float delTime = 0.0001f;

        float up = 100;
        float down = -100;


        for (int res = 0; res < results.length; res++) {
            String name = results[res].getName();
            //DataSet ds = new DataSet(name, name);

            PlotterFrame frame = PlotManager.getPlotterFrame("Rasterplot for " + name);

            Vector<DataSet> dataSets = new Vector<DataSet>();
            try {
                Reader in = new FileReader(results[res]);
                LineNumberReader reader = new LineNumberReader(in);
                String nextLine = null;

                while ((nextLine = reader.readLine()) != null) {
                    String[] nums = nextLine.trim().split("\\s++");
                    logger.logComment("Found: ");
                    for (int j = 0; j < nums.length; j++) {
                        logger.logComment(j + ": (" + nums[j] + ")");
                    }
                    int cellIndex = Integer.parseInt(nums[0]) - 1;
                    float time = Float.parseFloat(nums[1]) * 1000;


                    if (dataSets.size() < cellIndex + 1) {
                        for (int i = dataSets.size(); i <= cellIndex; i++) {
                            dataSets.add(i, null);
                            logger.logComment(dataSets.size() + " data sets now: " + dataSets);
                        }
                    }


                    //logger.logComment("-- Data sets now: " + dataSets);

                    try {
                        logger.logComment("---  For cell " + cellIndex + " Data set already exists: " + dataSets.get(cellIndex).getReference());

                    } catch (Exception e) {
                        logger.logComment("Error: " + e.getMessage());
                        DataSet ds = new DataSet(name + ", cell: " + cellIndex, "Cell " + cellIndex, "", "", "", "");
                        dataSets.setElementAt(ds, cellIndex);
                        ds.addPoint(startTime, down);
                        logger.logComment("Created data set: " + ds.getReference());


                        logger.logComment(dataSets.size() + " data sets now: " + dataSets);
                    }
                    dataSets.get(cellIndex).addPoint(time - delTime, down);
                    dataSets.get(cellIndex).addPoint(time, up);
                    dataSets.get(cellIndex).addPoint(time + delTime, down);


                }
                in.close();


            } catch (Exception e) {
                e.printStackTrace();
            }

            for (DataSet ds : dataSets) {
                if (ds != null) {
                    logger.logComment("Data set: " + ds.getReference() + " has num points: " + ds.getNumberPoints());
                    frame.addDataSet(ds);
                }
            }

            frame.setVisible(true);
        }


    }

    void jMenuItemRasterise_actionPerformed(ActionEvent e) {
        if (jMenuItemRasterise.isSelected()) {
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();
            RasterOptions newRasterOptions = new RasterOptions();

            String req = "Percentage of the height of the plot to use";

            InputRequestElement percentInput = new InputRequestElement("percentage", req, null, newRasterOptions.getPercentage() + "", "");
            inputs.add(percentInput);

            req = "Cutoff threshold which will be considered a spike";

            InputRequestElement threshInput = new InputRequestElement("threshold", req, null, newRasterOptions.getThreshold() + "", "mV");
            inputs.add(threshInput);


            InputRequest dlg = new InputRequest(null,
                    "Please enter the parameters for the rasterplot",
                    "Parameters for the rasterplot",
                    inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);


            if (dlg.cancelled()) {
                return;
            }


            //String percent = JOptionPane.showInputDialog(req, "" + newRasterOptions.getPercentage());
            try {
                newRasterOptions.setPercentage(Float.parseFloat(percentInput.getValue()));
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid precentage", ex, this);
                return;
            }
            if (newRasterOptions.getPercentage() < 0
                    || newRasterOptions.getPercentage() > 100) {
                GuiUtils.showErrorMessage(logger, "Invalid precentage", null, this);
                return;
            }


            //String thresh = JOptionPane.showInputDialog(req, "" + newRasterOptions.getThreshold());
            try {
                newRasterOptions.setThreshold(Float.parseFloat(threshInput.getValue()));
            } catch (Exception ex) {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, this);
                return;
            }

            rasterise(newRasterOptions);

        } else {
            rasterised = false;

            jMenuItemShowAxes.setSelected(true);
            jMenuItemShowAxes_actionPerformed(null);


            plotCanvas.setRasterOptions(null);


            plotCanvas.repaint();
        }
        logger.logComment("Done jMenuItemRasterise_actionPerformed...");

    }
}
