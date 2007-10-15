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

/**
 * Main frame of application for popping up a graph of a vector of points
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class PlotterFrame extends JFrame
{
    private static ClassLogger logger = new ClassLogger("PlotterFrame");

    private boolean standAlone = true;

    private PlotCanvas plotCanvas = null;

    private final String formatMenuIndicator = "-> ";
    private final int maxLengthDescInMenu = 40;

    private String plotFrameReference = null;

    private Project project = null;

    protected static String defaultDataFilePrefix = "DataSet_";



    protected static String savedDataSetMenuPrefix = "(Saved) ";

    private boolean rasterised  = false;

    private RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());

    protected static String generateNew = "Generate new Data Set from this";

    /**
     * Used to indicate if one of the plots is specified as bar chart, but can't
     * be drawn as such...
     */
    private boolean problemDueToBarSpacing  = false;

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


    JCheckBoxMenuItem jMenuItemRasterise = new JCheckBoxMenuItem();


    boolean siMenuItemEnable = false;


    ToolTipHelper toolTipText = ToolTipHelper.getInstance();


    /**
     * Create a new PlotterFrame. Note: this is protected so that new PlotterFrames will normally be created through
     * the PlotManager
     */
    public PlotterFrame(String reference,
                           Project project,
                           boolean standAlone)
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.standAlone = standAlone;
        this.project = project;

        plotFrameReference = reference;
        this.setTitle(plotFrameReference);

        try
        {
            // To make haavyweight menus...
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);

            jbInit();
            extraInit();

            //addSampleData();

            this.repaint();


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    private void extraInit()
    {
        plotCanvas = new PlotCanvas(this);

        jPanelCanvasHolder.add(plotCanvas, "Center");

        jMenuOptions.setToolTipText(toolTipText.getToolTip("Plot View"));
        jMenuTools.setToolTipText(toolTipText.getToolTip("Plot Tools"));
        jMenuDifference.setToolTipText(toolTipText.getToolTip("Plot Difference"));
        jMenuAverage.setToolTipText(toolTipText.getToolTip("Plot Average"));
        jMenuViewPointsOnly.setToolTipText(toolTipText.getToolTip("Plot points only"));
    }


    //Component initialization
    private void jbInit() throws Exception
    {
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
        jMenuViewPointsOnly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuViewPointsOnly_actionPerformed(e);
            }
        });


        jMenuTools.setText("Tools");
        jMenuDifference.setText("Difference");
        jMenuAverage.setText("Average");
        jMenuAddManual.setText("Add new graph...");
        jMenuImportData.setText("Import data...");
        jMenuTools.add(jMenuDifference);
        jMenuTools.add(jMenuAverage);
        jMenuTools.add(jMenuAddManual);
        jMenuTools.add(jMenuImportData);

        jMenuDifference.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuDifference_actionPerformed(e);
            }
        });
        jMenuAverage.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuAverage_actionPerformed(e);
            }
        });

        jMenuAddManual.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuAddManual_actionPerformed(e);
            }
        });

        this.jMenuImportData.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuImportData_actionPerformed(e);
            }
        });




        //jMenuHelp.setText("Help");
        jMenuItemViewOrigin.setText("Include origin");
        jMenuItemViewOrigin.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemViewOrigin_actionPerformed(e);
            }
        });
        jPanelMain.setLayout(borderLayout2);
        jLabelStatusBar.setBorder(BorderFactory.createEtchedBorder());
        jLabelStatusBar.setText("...");
        //jMenuAboutInternalMenu.setText("About");
        //jMenuItemSubMenuItem.setText("thiss");
        jMenuPlotInfo.setText("Plot info");
        jPanelMain.setBorder(border1);
        jPanelCanvasHolder.setMinimumSize(new Dimension(501, 401));
        jPanelCanvasHolder.setPreferredSize(new Dimension(501, 401));
        jPanelCanvasHolder.setLayout(borderLayout3);
        jMenuItemSelection.setText("Only selected area");
        jMenuItemSelection.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemSelection_actionPerformed(e);
            }
        });
        jMenuItemStacked.setText("Stacked plots");
        jMenuItemStacked.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemStacked_actionPerformed(e);
            }
        });

        jMenuItemCustomView.setText("Custom view");
        jMenuItemCustomView.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
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

        jMenuItemShowAxes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemShowAxes_actionPerformed(e);
            }
        });

        jMenuItemClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemClose_actionPerformed(e);
            }
        });


        jMenuItemShowAxisNums.setText("Show axis numbering");
        jMenuItemShowAxisNums.setSelected(true);
        jMenuOptions.add(jMenuItemShowAxisNums);

        jMenuItemShowAxisNums.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemShowAxisNums_actionPerformed(e);
            }
        });

        jMenuItemShowAxisTicks.setText("Show axis ticks");
        jMenuItemShowAxisTicks.setSelected(true);
        jMenuOptions.add(jMenuItemShowAxisTicks);

        jMenuItemShowAxisTicks.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemShowAxisTicks_actionPerformed(e);
            }
        });

        jMenuOptions.addSeparator();
        jMenuItemRasterise.setText("Rasterise");
        jMenuItemRasterise.setSelected(false);
        jMenuOptions.add(jMenuItemRasterise);


        jMenuItemRasterise.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jMenuItemRasterise_actionPerformed(e);
            }
        });


        jMenuBarMainMenu.add(jMenuTools);

    }

    public void dispose()
    {
        PlotManager.plotFrameClosing(plotFrameReference);
    }


    public void addDataSet(DataSet dataSet)
    {
        logger.logComment("Adding data set: "+dataSet.getRefrence());
        plotCanvas.addDataSet(dataSet);

        updateMenus();
    }


    public void removeDataSet(DataSet dataSet)
    {
        logger.logComment("-----   Being asked to remove data set: "+ dataSet.getRefrence());
        plotCanvas.removeDataSet(dataSet);
        updateMenus();
    }


    protected void flagProblemDueToBarSpacing()
    {
        problemDueToBarSpacing  = true;

    }

    protected void removeProblemDueToBarSpacing()
    {
        problemDueToBarSpacing  = false;

    }


    protected boolean isProblemDueToBarSpacing()
    {
        return problemDueToBarSpacing;

    }


    protected boolean isRasterised()
    {
        return rasterised;

    }



    private void updateMenus()
    {
        logger.logComment("Update menus called...");
        jMenuPlotInfo.removeAll();

        for (int i = 0; i < plotCanvas.getDataSets().length; i++)
        {
            DataSet nextDataSet = plotCanvas.getDataSets()[i];
            JMenu newMenu = new JMenu();


            newMenu.setText(nextDataSet.getRefrence());
            if (nextDataSet.getDataSetFile()!=null)
            {
                newMenu.setText(savedDataSetMenuPrefix + nextDataSet.getRefrence());
            }

            newMenu.setForeground(nextDataSet.getGraphColour());
            JMenuItem descMenuItem = new JMenuItem();
            String someDescription = nextDataSet.getDescription();
            if (someDescription.length()>maxLengthDescInMenu)
            {
                someDescription = someDescription.substring(0,maxLengthDescInMenu-3)+ "...";
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


            JMenuItem distHistItem = new JMenuItem();
            distHistItem.setText("Distribution Histogram");
            distHistItem.setToolTipText("Distribution Histogram of y values of the data set");
            transformItem.add(distHistItem);

            distHistItem.addActionListener(new DataSetDistHistMenuListener(nextDataSet));

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
            saveMenuItem.setText("Save plot");
            saveMenuItem.setToolTipText("Save this data set in this project. Can be reloaded via Project -> Data Set Manager");






            if (project!=null)
            {
                JMenuItem saveInProjectMenuItem = new JMenuItem();
                saveInProjectMenuItem.setText("Save Data Set in project");
                saveMenuItem.add(saveInProjectMenuItem);

                saveInProjectMenuItem.addActionListener(new DataSetSaveInProjMenuListener(nextDataSet, this));

            }


            JMenuItem exportMenuItem = new JMenuItem();
            exportMenuItem.setText("Export points to file...");
            exportMenuItem.setToolTipText("Export this data set to a file for use by another application");
            saveMenuItem.add(exportMenuItem);
            File prefFile = new File (".");
            if (project!=null)
            {
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
                                                        PlotCanvas.USE_CIRCLES_FOR_PLOT,
                                                        PlotCanvas.USE_BARCHART_FOR_PLOT};

            for (int optionNum = 0; optionNum < options.length; optionNum++)
            {
                JMenuItem newMenuItem = new JMenuItem();
                String text = options[optionNum];
                if (nextDataSet.getGraphFormat().equals(text)) text = formatMenuIndicator + text;
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


            JMenuItem removeMenuItem = new JMenuItem();
            removeMenuItem.setText("Remove data plot from frame");
            removeMenuItem.setToolTipText("Remove only this set of data points");
            newMenu.add(removeMenuItem);

            removeMenuItem.addActionListener(new DataSetRemoveMenuListener(nextDataSet, this));

        }
        // difference doesn't make sense with less than 2 data sets...
        if (plotCanvas.dataSets.length<2)
        {
            jMenuDifference.setEnabled(false);
            jMenuAverage.setEnabled(false);
        }
        else
        {
            jMenuDifference.setEnabled(true);
            jMenuAverage.setEnabled(true);
        }

        logger.logComment("Done updating menus, repainting");
        plotCanvas.repaint();
    }

    public class DataSetInfoMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetInfoMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            StringBuffer sb = new StringBuffer();

            sb.append("Information on graph: " + dataSet.getRefrence() + "\n\n");


            if (dataSet.getXLegend().length() > 0 && dataSet.getYLegend().length() > 0)
            {
                sb.append("X axis: " + dataSet.getXLegend()+ "\n"
                          + "Y axis: " + dataSet.getYLegend() + "\n\n");
            }
            if (dataSet.getXUnit().length() > 0 && dataSet.getYUnit().length() > 0)
            {
                sb.append("Units along X axis: " + dataSet.getXUnit()+ "\n"
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

            if (sb.length()<400)
            {

                GuiUtils.showInfoMessage(null, "Info on graph: " + dataSet.getRefrence(),
                                         sb.toString(), null);

            }
            else
            {
                SimpleViewer.showString(sb.toString(),
                                        "Info on graph: " + dataSet.getRefrence(),
                    12, false, false);
            }

        };

    }



    public class DataSetTransformListener implements ActionListener
    {
        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetTransformListener(DataSet dataSet,
                                        PlotterFrame plotFrame)
        {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;

        }

        public void actionPerformed(ActionEvent e)
        {
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

            InputRequestElement xTrans
                = new InputRequestElement("xTrans", "Enter the expression for the new x values as a function of the original x", null, "x", "");

            inputs.add(xTrans);

            InputRequestElement yTrans
                = new InputRequestElement("yTrans", "Enter the expression for the new y values as a function of the original x and y", null, "2 * y", "");

            inputs.add(yTrans);

            InputRequest dlg = new InputRequest(plotFrame,
                                                "Please specify the form of the new DataSet",
                                                "New Data Set",
                                                inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);

            if (dlg.cancelled())return;


            Variable x = new Variable("x");
            Variable y = new Variable("y");

            String xExpression = xTrans.getValue();
            String yExpression = yTrans.getValue();
            String xName = null;
            String yName = null;
            EquationUnit xFunc = null;
            EquationUnit yFunc = null;

            try
            {
                xFunc = Expression.parseExpression(xExpression,
                                                                new Variable[]
                                                                {x});

                xName = "X expression: xNew = f(x) = " + xFunc.getNiceString();

                yFunc = Expression.parseExpression(yExpression,
                                                                new Variable[]
                                                                {x, y});

                yName = "Y expression: yNew = f(x, y) = " + yFunc.getNiceString();
            }
            catch (EquationException ex)
            {
                GuiUtils.showErrorMessage(logger, "Unable to evaluate expressions:\nxNew = f(x) = "
                                          + xExpression + "\nyNew = f(x, y) = " + yExpression + "\n", ex, null);
                return;
            }

            DataSet generatedDataSet = new DataSet("Transformation of "+dataSet.getRefrence(),
                                                   "Data Set: "+dataSet.getRefrence()+ "transformed by:\n"
                                                   +xName+"\n"+yName+"\n"+"Original description:\n"
                                                   +dataSet.getDescription(),
                                                   dataSet.getXUnit(),
                                                   dataSet.getYUnit(),
                                                   "Transformation of "+dataSet.getXLegend(),
                                                   "Transformation of "+dataSet.getYLegend());

            for (int i = 0; i < dataSet.getNumberPoints(); i++)
            {
                double[] point = dataSet.getPoint(i);

                Argument[] x0 = new Argument[]
                    {new Argument(x.getName(), point[0])};

                Argument[] y0 = new Argument[]
                    {new Argument(x.getName(), point[0]),
                    new Argument(y.getName(), point[1])};



                try
                {
                    generatedDataSet.addPoint(xFunc.evaluateAt(x0), yFunc.evaluateAt(y0));
                }
                catch (EquationException ex)
                {
                    GuiUtils.showErrorMessage(logger, "Unable to evaluate expressions:\n"
                                              +xName+"\n"+yName+"\nat point: ("+ point[0]+", "+point[1]+")", ex, null);
                    return;
                }
            }

            this.plotFrame.addDataSet(generatedDataSet);


        }
    }


    public class DataSetQuickStatsMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetQuickStatsMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            StringBuffer sb = new StringBuffer();

            sb.append("Quick stats on y axis of graph: " + dataSet.getRefrence() + "\n\n");

            double total = 0;

            for (int i = 0; i < dataSet.getNumberPoints(); i++)
            {
                    //String next = (String)all.elementAt(i);
                    total = total + dataSet.getPoint(i)[1];

            }
            double average = total/(double)dataSet.getNumberPoints();

            double totalDevSqrd = 0;

            for (int i = 0; i < dataSet.getNumberPoints(); i++)
            {
                    //String next = (String)all.elementAt(i);
                    double dev = dataSet.getPoint(i)[1] - average;

                    totalDevSqrd = totalDevSqrd + (dev*dev);
            }

            double stdDev = Math.sqrt(totalDevSqrd/(double)dataSet.getNumberPoints());


            sb.append("Average value: " + average + "\n");
            sb.append("Standard deviation: " + stdDev + "\n\n");

            sb.append("Number of points: " + dataSet.getNumberPoints() + "\n");
            sb.append("Max X: " + dataSet.getMaxX()[0] + " (y value: "+dataSet.getMaxX()[1]+") \n");
            sb.append("Min X: " + dataSet.getMinX()[0] + " (y value: "+dataSet.getMinX()[1]+") \n");
            sb.append("Max Y: " + dataSet.getMaxY()[1] + " (x value: "+dataSet.getMaxY()[0]+") \n");
            sb.append("Min Y: " + dataSet.getMinY()[1] + " (x value: "+dataSet.getMinY()[0]+") \n");





            GuiUtils.showInfoMessage(null, "Info on graph: " + dataSet.getRefrence(),
                                     sb.toString(), null);

        };

    }


    public void showSmallInfoBox(String title, String info)
    {

        SimpleViewer.showString(info,
                                title,
                                12,
                                false,
                                false,
                                0.5f,
                                0.3f);

    }


    public class DataSetEditPointsMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetEditPointsMenuListener(DataSet dataSet, PlotterFrame plotFrame)
        {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }


        public void actionPerformed(ActionEvent e)
        {
            logger.logComment("Action performed on DataSetEditPointsMenuListener");
            EditPointsDialog dlg = new EditPointsDialog(plotFrame, dataSet, true);
            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);

            updateMenus();
        };

    }




    public class DataSetSpikeMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetSpikeMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {

            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();

            String req = "Threshold for spike";

            float suggestedThresh = -20;
            if (PlotterFrame.preferredSpikeValsEntered) suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();
            InputRequestElement threshInput = new InputRequestElement("threshold", req, null, suggestedThresh+"", "mV");
            inputs.add(threshInput);


            req = "Start time from which to analyse the spiking";
            float suggestedStart = (float)dataSet.getMinX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) suggestedStart = plotCanvas.getSpikeOptions().getStartTime();

            InputRequestElement startInput = new InputRequestElement("start", req, null, suggestedStart+"", "ms");
            inputs.add(startInput);



            req = "Finish time from which to analyse the spiking";
            float suggestedEnd = (float)dataSet.getMaxX()[0];
            if (PlotterFrame.preferredSpikeValsEntered) suggestedEnd = plotCanvas.getSpikeOptions().getStopTime();

            InputRequestElement stopInput = new InputRequestElement("stop", req, null, suggestedEnd+"", "ms");
            inputs.add(stopInput);

            InputRequest dlg = new InputRequest(null, "Please enter the parameters for calculating spiking statistics", "Parameters for spiking statistics", inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);


            if (dlg.cancelled()) return;




            if (threshInput.getValue()==null) return; // i.e. cancelled

            float threshold = 0;
            try
            {
                threshold = Float.parseFloat(threshInput.getValue());
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setThreshold(threshold);

            if (startInput.getValue()==null) return; // i.e. cancelled

            float startTime = 0;
            try
            {
                startTime = Float.parseFloat(startInput.getValue());
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStartTime(startTime);


            if (stopInput.getValue()==null) return; // i.e. cancelled


            float stopTime = 1000;
            try
            {
                stopTime = Float.parseFloat(stopInput.getValue());
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
                return;
            }
            plotCanvas.getSpikeOptions().setStopTime(stopTime);

            PlotterFrame.preferredSpikeValsEntered = true;





           // boolean spiking = false;
            double[]  spikeTimes = SpikeAnalyser.getSpikeTimes(dataSet.getYValues(),
                                                            dataSet.getXValues(),
                                                            threshold,
                                                            startTime,
                                                            stopTime);

            Vector interSpikeIntervals
                = SpikeAnalyser.getInterSpikeIntervals(dataSet.getYValues(),
                                                       dataSet.getXValues(),
                                                       threshold,
                                                       startTime,
                                                       stopTime);



            double totalISI = 0;

            for (int i = 0; i < interSpikeIntervals.size(); i++)
            {
                    totalISI = totalISI + ((Double)interSpikeIntervals.elementAt(i)).doubleValue();

            }
            double averageISI = totalISI/(double)interSpikeIntervals.size();

            double totalDevSqrd = 0;

            for (int i = 0; i < interSpikeIntervals.size(); i++)
            {
                    double dev = ((Double)interSpikeIntervals.elementAt(i)).doubleValue() - averageISI;

                    totalDevSqrd = totalDevSqrd + (dev*dev);
            }
            double stdDev = Math.sqrt(totalDevSqrd/(double)interSpikeIntervals.size());



            StringBuffer sb = new StringBuffer();

            //sb.append("<span style=\"color:#0000FF;\">");
            sb.append("<h2>Simple spiking info on graph: " + dataSet.getRefrence() + "</h2>");

            sb.append("Spiking threshold: " + plotCanvas.getSpikeOptions().getThreshold()+ "<br>");
            sb.append("Total number of spikes: " + spikeTimes.length+ "<br>");
            sb.append("Start time: " + plotCanvas.getSpikeOptions().getStartTime()
                      + ", stop time: " + plotCanvas.getSpikeOptions().getStopTime() + "<br>");

            sb.append("  <h3>X axis with units milliseconds:</h3>");

            double simDuration = (stopTime - startTime);

            sb.append("Total time: " + Utils3D.trimDouble(simDuration, 5) + " ms<br>");

            sb.append("Average frequency (#spikes/time): <b>"
                      + Utils3D.trimDouble(((double)spikeTimes.length/simDuration)*1000,5) + " Hz</b><br>");


            sb.append("Average Inter Spike Interval: "+ Utils3D.trimDouble(averageISI,5) + " ms<br>");

            sb.append("Standard Deviation on ISI   : "+ Utils3D.trimDouble(stdDev,5) + " ms<br>");

            sb.append("Frequency based on ISI: <b>"+ Utils3D.trimDouble((1/averageISI)*1000,5) + " Hz</b><br><br><br>");






            sb.append("<h3>X axis with units seconds:</h3>");


            sb.append("Total time: " + Utils3D.trimDouble(simDuration, 5) + " s<br>");

            sb.append("Average frequency (#spikes/time): <b>"
                       + Utils3D.trimDouble(((double)spikeTimes.length/simDuration),5) + " Hz</b><br>");


             sb.append("Average Inter Spike Interval: "+ Utils3D.trimDouble(averageISI,5) + " s<br>");

             sb.append("Standard Deviation on ISI   : "+ Utils3D.trimDouble(stdDev,5) + " s<br>");

             sb.append("Frequency based on ISI: <b>"+ Utils3D.trimDouble((1/averageISI),5) + " Hz</b><br><br><br>");


             String spikeTimeInfo = "";
             for(double st: spikeTimes) spikeTimeInfo = spikeTimeInfo + (float)st+ "  ";

            sb.append("Actual spikeTimes: <br>" + spikeTimeInfo+ "<br><br>");

            sb.append("Number of points: " + dataSet.getNumberPoints() + "<br>");

            //sb.append("Max X: " + plotCanvas.getSpikeOptions().getStartTime()
            //          + ", min X: " + plotCanvas.getSpikeOptions().getStopTime() + "<br>");
            //sb.append("Max Y: " + dataSet.getMaxYvalue() + ", min Y: " + dataSet.getMinYvalue() + "<br>");
            //sb.append("</span>");



           // GuiUtils.showInfoMessage(null, "Spike info on graph: " + dataSet.getRefrence(),
            //                         sb.toString(), null);
            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                         "Spike info on graph: "
                                                         + dataSet.getRefrence(),
                                                         12,
                                                         false,
                                                         true);

            simpleViewer.setFrameSize(600, 600);

            GuiUtils.centreWindow(simpleViewer);

            simpleViewer.setVisible(true);


        };

    }




    public class DataSetSyncIndexMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        Component parent = null;

        public DataSetSyncIndexMenuListener(DataSet dataSet, Component parent)
        {
            this.dataSet = dataSet;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e)
        {
            logger.logComment("-----   Generating SI for: " + dataSet.getRefrence());

            double xSpacing = dataSet.getXSpacing();

            if (!dataSet.areXvalsStrictlyIncreasing() || xSpacing < 0 || dataSet.getMinX()[0]!=0)
            {
                logger.logComment("dataSet.areXvalsStrictlyIncreasing(): " + dataSet.areXvalsStrictlyIncreasing());
                logger.logComment("xSpacing: " + xSpacing);
                logger.logComment("dataSet.getMinXvalue(): " + dataSet.getMinX());
                GuiUtils.showErrorMessage(logger,
                                          "The set of points: " + dataSet.getRefrence()
                                          +" are not strictly increasing and evenly spaced, "
                                          +" or the lowest x value is not zero, and these are requirements for generating a Synchronisation Index", null,
                                          parent);

                return;

            }

            int N = dataSet.getNumberPoints();
            double[] yVals = dataSet.getYValues();
            double[] xVals = dataSet.getXValues();

            double periodAccuracy = xSpacing/10d;

            double minPeriod = xSpacing *3;

            double bestInnerProd = -1;
            double optimalPeriod = -1;

            //DataSet ds = new DataSet("fs", "sghdfg");



            for (double period = minPeriod; period<=dataSet.getMaxX()[0];period+= periodAccuracy)
            {
                double intProd = 0;
                for (int i = 0; i < N; i++)
                {
                    intProd += yVals[i] * Math.cos(2* Math.PI *i/period);

                   // ds.addPoint(period, intProd);
                }
                //logger.logComment("intProd for period "+period+": " + intProd, true);
                if (intProd>bestInnerProd)
                {
                    bestInnerProd = intProd;
                    optimalPeriod = period;
                }
            }
            logger.logComment("optimalPeriod: " + optimalPeriod);

            //PlotterFrame frame = PlotManager.getPlotterFrame(ds.getRefrence(), false);
            //frame.addDataSet(ds);

            double total = 0;
            double intProdCos = 0;

            for (int i = 0; i < N; i++)
            {
                intProdCos+= Math.cos(2* Math.PI *xVals[i]/optimalPeriod) * yVals[i];
                total+= yVals[i];
            }
            double si = intProdCos/total;

            GuiUtils.showInfoMessage(logger, "Synchronisation Index",
                                     "The Synchronisation Index for that autocorrelogram is: "+si+" and the optimal period was: "+ optimalPeriod, parent);



        }
    }



    public class DataSetAutocorrMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        Component parent = null;

        public DataSetAutocorrMenuListener(DataSet dataSet, Component parent)
        {
            this.dataSet = dataSet;
            this.parent = parent;
        }

        public void actionPerformed(ActionEvent e)
        {
            logger.logComment("-----   Generating autocorrelogram for : " + dataSet.getRefrence());

            double xSpacing = dataSet.getXSpacing();

            double lengthInX = dataSet.getMaxX()[0] - dataSet.getMinX()[0];

            if (!dataSet.areXvalsStrictlyIncreasing() || xSpacing < 0)
            {
                GuiUtils.showErrorMessage(logger,
                                          "The set of points: " + dataSet.getRefrence()
                                          +
                    " are not strictly increasing and evenly spaced, and this is a requirement for generating an autocorrelogram", null,
                                          parent);

                return;

            }
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement> ();

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

            if (dlg.cancelled()) return;

            start = Float.parseFloat(startInput.getValue());
            end = Float.parseFloat(endInput.getValue());

            if ( (end - start) >= lengthInX)
            {
                GuiUtils.showErrorMessage(logger,
                                          "The time interval of [" + start + ", " + end +
                                          "] is longer than the range of x values in the dataSet: [" +
                                          dataSet.getMinX() + ", " + dataSet.getMaxX() + "] ", null, parent);

                return;

            }

            if (Math.abs(end) > lengthInX || Math.abs(start) > lengthInX)
            {
                GuiUtils.showErrorMessage(logger,
                                          "The time interval of [" + start + ", " + end +
                                          "] is not appropriate for the range of x values in the dataSet: [" +
                                          dataSet.getMinX() + ", " + dataSet.getMaxX() + "] ", null, parent);

                return;

            }

            int beginOffset = (int) Math.floor(start / xSpacing);
            int endOffset = (int) Math.ceil(end / xSpacing);

            logger.logComment("---  Range: " + beginOffset + " points before and " + endOffset + " points after");

            double total = 0;

            int N = dataSet.getNumberPoints();

            for (int i = 0; i < N; i++)
            {
                total = total + dataSet.getPoint(i)[1];

            }
            double average = total / (double) N;

            double totalDevSqrd = 0;

            for (int i = 0; i < N; i++)
            {
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
            if (beginOffset < 0) startIndex = -1 * beginOffset;

            double[] yVals = dataSet.getYValues();

            for (int pointIndex = startIndex; pointIndex < numPointsOrig - endOffset; pointIndex++)
            {
                double y = yVals[pointIndex];

                int offset = beginOffset;

                while (pointIndex + offset < N && offset <= endOffset)
                {
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

            DataSet acDataSet = new DataSet("Autocorrelogram of " + dataSet.getRefrence(),
                                            "Autocorrelogram of " + dataSet.getRefrence(),
                                            "ms",
                                            "",
                                            "Time",
                                            "Autocorrelation");

            for (int i = 0; i < numInAC; i++)
            {

                acDataSet.addPoint(i, acVals[i]);

            }

            //acDataSet.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
            PlotterFrame frame = PlotManager.getPlotterFrame(acDataSet.getRefrence(), false, true);

            frame.setSIOptionEnabled(true);

            frame.addDataSet(acDataSet);

        }
    }

    public void setSIOptionEnabled(boolean enabled)
    {
        this.siMenuItemEnable = enabled;
    }

    public class DataSetISIHistMenuListener implements ActionListener
      {
          DataSet dataSet = null;

          public DataSetISIHistMenuListener(DataSet dataSet)
          {
              this.dataSet = dataSet;
          }

          public void actionPerformed(ActionEvent e)
          {
              String req = "Please enter the cutoff threshold which will be considered a spike";

              float suggestedThresh = -20;
              if (PlotterFrame.preferredSpikeValsEntered) suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();

              String thresh = JOptionPane.showInputDialog(req, ""+suggestedThresh);

              if (thresh==null) return; // i.e. cancelled

              float threshold = 0;
              try
              {
                  threshold = Float.parseFloat(thresh);
              }
              catch (Exception ex)
              {
                  GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                  return;
              }
              plotCanvas.getSpikeOptions().setThreshold(threshold);




              req = "Please enter the start time from which to analyse the spiking";
              float suggestedStart = (float)dataSet.getMinX()[0];
              if (PlotterFrame.preferredSpikeValsEntered) suggestedStart = plotCanvas.getSpikeOptions().getStartTime();

              String start = JOptionPane.showInputDialog(req, ""+suggestedStart);

              if (start==null) return; // i.e. cancelled

              float startTime = 0;
              try
              {
                  startTime = Float.parseFloat(start);
              }
              catch (Exception ex)
              {
                  GuiUtils.showErrorMessage(logger, "Invalid start time", ex, null);
                  return;
              }
              plotCanvas.getSpikeOptions().setStartTime(startTime);




              req = "Please enter the finish time from which to analyse the spiking";
              float suggestedEnd = (float)dataSet.getMaxX()[0];
              if (PlotterFrame.preferredSpikeValsEntered) suggestedEnd = plotCanvas.getSpikeOptions().getStopTime();

              String stop = JOptionPane.showInputDialog(req, ""+suggestedEnd);

              if (stop==null) return; // i.e. cancelled


              float stopTime = 1000;
              try
              {
                  stopTime = Float.parseFloat(stop);
              }
              catch (Exception ex)
              {
                  GuiUtils.showErrorMessage(logger, "Invalid stop time", ex, null);
                  return;
              }
              plotCanvas.getSpikeOptions().setStopTime(stopTime);


              req = "Please enter the bin size for the ISI Histogram";

              float suggestedBinSize = Math.min(1, (stopTime -startTime)/60f);

              logger.logComment("Suggested bin size: "+ suggestedBinSize);


              String binSizeString = JOptionPane.showInputDialog(req, ""+suggestedBinSize);

              if (binSizeString==null) return; // i.e. cancelled


              float binSize = 1;
              try
              {
                  binSize = Float.parseFloat(binSizeString);
              }
              catch (Exception ex)
              {
                  GuiUtils.showErrorMessage(logger, "Invalid ISI Histogram bin size", ex, null);
                  return;
              }


              req = "Please enter the maximum ISI value to be plotted";
              float suggestedMax = (stopTime -startTime)/10f;


              String maxString = JOptionPane.showInputDialog(req, ""+suggestedMax);

              if (maxString==null) return; // i.e. cancelled


              float maxSize = 1;
              try
              {
                  maxSize = Float.parseFloat(maxString);
              }
              catch (Exception ex)
              {
                  GuiUtils.showErrorMessage(logger, "Invalid maximum ISI Histogram size", ex, null);
                  return;
              }


              PlotterFrame.preferredSpikeValsEntered = true;




              Vector interSpikeIntervals
                  = SpikeAnalyser.getInterSpikeIntervals(dataSet.getYValues(),
                                                         dataSet.getXValues(),
                                                         threshold,
                                                         startTime,
                                                         stopTime);

              int numBins = Math.round((maxSize)/binSize);

              DataSet isiHist = new DataSet("ISI Histogram of "+dataSet.getRefrence(),
                                            "ISI Histogram of "+dataSet.getRefrence(),
                  "ms",
        "",
        "Interspike interval",
        "Number per bin");

              for (int i = 0; i < numBins; i++)
              {
                  float startISI = i*binSize;
                  float endISI = (i+1)*binSize;

                  int totalHere = 0;

                  for (int j = 0; j < interSpikeIntervals.size(); j++)
                  {
                          double isi = ((Double)interSpikeIntervals.elementAt(j)).doubleValue();

                          if (isi>=startISI && isi<endISI)
                          {
                              totalHere++;
                          }
                  }
                  isiHist.addPoint((endISI+startISI)/2f, totalHere);

              }
              ///check more...
              boolean warn = false;
              for (int j = 0; j < interSpikeIntervals.size(); j++)
              {
                  double isi = ( (Double) interSpikeIntervals.elementAt(j)).doubleValue();

                  if (isi >= maxSize)
                  {
                      warn = true;
                  }
              }
              if (warn)
              {
                  GuiUtils.showErrorMessage(logger, "Warning. The maximum ISI you have chosen to be plotted, "
                                            + maxSize + ", is exceeded by at least one of the ISIs\n"
                                            +"in the original spike train, and so not all ISIs will be included on this histogram.", null, null);
              }



              isiHist.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);
              PlotterFrame frame = PlotManager.getPlotterFrame(isiHist.getRefrence(), false, true);

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



          };

      }









      public class DataSetSaveInProjMenuListener implements ActionListener
      {
          DataSet dataSet = null;
          PlotterFrame plotFrame = null;

          public DataSetSaveInProjMenuListener(DataSet dataSet, PlotterFrame plotFrame)
          {
              this.dataSet = dataSet;
              this.plotFrame = plotFrame;
          }

          public void actionPerformed(ActionEvent e)
          {
              if (project == null)
              {
                  GuiUtils.showErrorMessage(logger,
                                            "Error. There is no project associated with this plot instance",
                                            null,
                                            plotFrame);
              }

              File saveDir = ProjectStructure.getDataSetsDir(project.getProjectMainDirectory());


              int dataSetCount = 0;

              String suggestedName = DataSetManager.DATA_SET_PREFIX + dataSetCount +
                  ProjectStructure.getDataSetExtension();

              File suggestedFile = new File(saveDir, suggestedName);

              while (suggestedFile.exists())
              {
                  dataSetCount++;
                  suggestedName = DataSetManager.DATA_SET_PREFIX + dataSetCount +
                      ProjectStructure.getDataSetExtension();

                  suggestedFile = new File(saveDir, suggestedName);

              }

              dataSet.setDataSetFile(suggestedFile);


              DataSetManager.saveDataSet(dataSet);

              GuiUtils.showInfoMessage(null, "Saved to project.",
                         "Data Set saved to project, in file " + dataSet.getDataSetFile().getAbsolutePath()+
                         "\nTo view these points again select Project -> Data Set Manager",
                         null);

            logger.logComment("Action performed on DataSetSaveInProjMenuListener");

              updateMenus();

          };

      }



      public class DataSetExportMenuListener implements ActionListener
      {
          DataSet dataSet = null;
          PlotterFrame plotFrame = null;
          File preferredDir = null;

          public DataSetExportMenuListener(DataSet dataSet, PlotterFrame plotFrame, File preferredDir)
          {
              this.dataSet = dataSet;
              this.plotFrame = plotFrame;
              this.preferredDir = preferredDir;
          }

          public void actionPerformed(ActionEvent e)
          {
              RecentFiles recentFiles = RecentFiles.getRecentFilesInstance(ProjectStructure.getNeuConRecentFilesFilename());
              String lastDir = recentFiles.getMyLastExportPointsDir();

              File defaultDir = null;

              if (lastDir != null)
              {
                  defaultDir = new File(lastDir);
              }
              else
              {
                  defaultDir = preferredDir;
              }

              JFileChooser chooser = new JFileChooser();
              chooser.setDialogType(JFileChooser.OPEN_DIALOG);

              chooser.setCurrentDirectory(defaultDir);
              String prefFileName = GeneralUtils.getBetterFileName(dataSet.getRefrence() + ".dat");

              chooser.setSelectedFile(new File(prefFileName));

              chooser.setDialogTitle("Choose file to export this data to");

              int retval = chooser.showDialog(plotFrame, "Choose file");

              if (retval == JOptionPane.OK_OPTION)
              {
                  if (chooser.getSelectedFile() != null)
                  {


                      if (chooser.getSelectedFile().exists() &&
                          chooser.getSelectedFile().length() > 0)
                      {
                          int ans = JOptionPane.showConfirmDialog(plotFrame,
                                                                  "The file " + chooser.getSelectedFile() +
                                                                  " is not empty. Overwrite?"
                                                                  , "Confirm overwrite",
                                                                  JOptionPane.YES_NO_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE);

                          if (ans == JOptionPane.NO_OPTION)
                          {
                              return;
                          }

                      }
                      //System.out.println("Exporting to file: " + chooser.getSelectedFile());
                      try
                      {
                          FileWriter fw = new FileWriter(chooser.getSelectedFile());
                          int numPoints = dataSet.getNumberPoints();

                          for (int i = 0; i < numPoints; i++)
                          {
                              fw.write((float)dataSet.getPoint(i)[0] + ", " + (float)dataSet.getPoint(i)[1] + "\n");
                          }
                          fw.close();

                          GuiUtils.showInfoMessage(null, "Points exported",
                                                   "Those "+ numPoints +" points have been saved to file: "+ chooser.getSelectedFile()+
                                                   "\nFile length: "+chooser.getSelectedFile().length()+" bytes",
                                                   plotFrame);

                          recentFiles.setMyLastExportPointsDir(chooser.getSelectedFile().getAbsoluteFile().getParent());
                          recentFiles.saveToFile();

                      }
                      catch (IOException ex)
                      {
                          GuiUtils.showErrorMessage(null, "Problem exporting those points", ex, plotFrame);
                      }

                  }
                  else
                  {
                      //System.out.println("Not continuing...");
                  }
              }

          };

      }

    public class DataSetColourMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetColourMenuListener(DataSet dataSet, PlotterFrame plotFrame)
        {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e)
        {

            logger.logComment("Action performed on DataSetColourMenuListener");

            Color c = JColorChooser.showDialog(plotFrame,
                                               "Please choose a colour for the data set: "+ dataSet.getRefrence(),
                                               dataSet.getGraphColour());
            if (c != null)
            {
                dataSet.setGraphColour(c);
                plotFrame.updateMenus();
            }
        };
    }


    public class DataSetRemoveMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetRemoveMenuListener(DataSet dataSet, PlotterFrame plotFrame)
        {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e)
        {
            plotFrame.removeDataSet(dataSet);
        };
    }


    public class DataSetDistHistMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        //PlotterFrame plotFrame = null;

        public DataSetDistHistMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
            //this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e)
        {

            String req = "Plotting the distribution of the y values of the Data Set: " + dataSet.getRefrence() + "\n"
                + "Max y: " + dataSet.getMaxY()[1] + ", min y: " + dataSet.getMinY()[1] + "\n"
                + "Please enter the number of bins to use:";

            //float bin = (float)(dataSet.getMaxYvalue() - dataSet.getMinYvalue()) / 20;

            int numBins = 20;

            String binNumString = JOptionPane.showInputDialog(req, "" + numBins);

            try
            {
                numBins = Integer.parseInt(binNumString);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid bin number", ex, null);
                return;
            }

            req = "Please enter start Y value from which to generate distribution";
            float start = (float)dataSet.getMinY()[1];

            String startString = JOptionPane.showInputDialog(req, "" + start);

            try
            {
                start = Float.parseFloat(startString);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid start value", ex, null);
                return;
            }


            req = "Please enter final Y value from which to generate distribution";
            float stop = (float)dataSet.getMaxY()[1];

            String stopString = JOptionPane.showInputDialog(req, "" + stop);

            try
            {
                stop = Float.parseFloat(stopString);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid stop value", ex, null);
                return;
            }





            //int numBins = (int) ((dataSet.getMaxYvalue() - dataSet.getMinYvalue()) / bin)+1;

            double binLength = ( (stop - start) / (numBins));

            logger.logComment("binLength: " + binLength);

            int[] numInEach = SpikeAnalyser.getBinnedValues(dataSet.getYValues(),
                                                            start,
                                                            binLength,
                                                            numBins);
            /*
                         int[] numInEach = new int[numBins];

                         for (int i = 0; i < dataSet.getNumberPoints(); i++)
                         {
                double loc = (dataSet.getPoint(i)[1] - dataSet.getMinYvalue())/binLength;

                int binNum =  (int)Math.floor(loc);
                if (loc == numBins) binNum = numBins -1;

                //System.out.println("Point: "+ dataSet.pointToString(i) +", Loc: "+loc+", bin num: "+ binNum);
                numInEach[binNum]++;
                         }*/

            String desc = "";
            String newXval = "Y values";
            if (dataSet.getYLegend().length() > 0)
            {
                desc = "Distribution of: "+ dataSet.getRefrence();
                newXval = dataSet.getYLegend();
            }
            else
            {
                desc = "Distribution of y values of "+ dataSet.getRefrence();
            }

            DataSet ds = new DataSet(desc,
                                     desc,
                                     dataSet.getYUnit(), "", newXval, "Number per bin");

            for (int i = 0; i < numBins; i++)
            {
                double yVal = start + binLength * (i + 0.5);
                ds.addPoint(yVal, numInEach[i]);
            }

            ds.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);

            //ds.set

            PlotterFrame frame = PlotManager.getPlotterFrame(ds.getRefrence(), false, true);
            frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
            frame.addDataSet(ds);

        }
    }



    public class DataSetSimpDerivMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetSimpDerivMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {

            String newYUnit = "";
            String newYLegend = "";
            if (dataSet.getYUnit().length()>0 & dataSet.getXUnit().length()>0)
            {
                newYUnit = "("+dataSet.getYUnit()+") / ("+dataSet.getXUnit()+")";
            }
            if (dataSet.getYLegend().length()>0)
            {
                newYLegend = "Derivative of "+ dataSet.getYLegend();
            }
            DataSet ds = new DataSet("Simple derivative of " + dataSet.getRefrence(),
                                     "Simple derivative of \n" + dataSet.getDescription(),
                                     dataSet.getXUnit(),
                                     newYUnit,
                                     dataSet.getXLegend(),
                                     newYLegend);

            double[] prevPoint = dataSet.getPoint(0);

            for (int i = 1; i < dataSet.getNumberPoints(); i++)
            {
                double[] currPoint = dataSet.getPoint(i);
                double x = (prevPoint[0]+currPoint[0])/2;
                double y = (currPoint[1]-prevPoint[1])/(currPoint[0]-prevPoint[0]);
                //System.out.println("Adding point ("+x+","+y+")");
                ds.addPoint(x, y);
                prevPoint = currPoint;
            }

            //ds.setGraphFormat(PlotCanvas.USE_BARCHART_FOR_PLOT);

            //ds.set

            PlotterFrame frame = PlotManager.getPlotterFrame(ds.getRefrence(), false, true);
            //frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
            frame.addDataSet(ds);

        }
    }



    public class DataSetPhasePlanePlotMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetPhasePlanePlotMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {

            String newYUnit = "";
            String newYLegend = "";
            
            if (dataSet.getYUnit().length()>0 & dataSet.getXUnit().length()>0)
            {
                newYUnit = "("+dataSet.getYUnit()+") / ("+dataSet.getXUnit()+")";
            }
            
            if (dataSet.getYLegend().length()>0)
            {
                newYLegend = "Derivative of "+ dataSet.getYLegend();
            }
            
            DataSet ds = new DataSet("Phase plane plot of " + dataSet.getRefrence(),
                                     "Phase plane plot of \n" + dataSet.getDescription(),
                                     dataSet.getYUnit(),
                                     newYUnit,
                                     dataSet.getYLegend(),
                                     newYLegend);
            
            ds.setGraphFormat(PlotCanvas.USE_LINES_FOR_PLOT);
            ds.setGraphColour(new Color(204,0,102));

            double[] prevPoint = dataSet.getPoint(0);

            for (int i = 1; i < dataSet.getNumberPoints(); i++)
            {
                double[] currPoint = dataSet.getPoint(i);
                
                double newX = (prevPoint[1]+currPoint[1])/2;
                double newY = (currPoint[1]-prevPoint[1])/(currPoint[0]-prevPoint[0]);
                
                //System.out.println("Adding point ("+x+","+y+")");
                ds.addPoint(newX, newY);
                prevPoint = currPoint;
            }

            PlotterFrame frame = PlotManager.getPlotterFrame(ds.getRefrence(), false, true);
            //frame.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
            frame.addDataSet(ds);

        }
    }




    public class DataSetFormatMenuListener implements ActionListener
    {
        DataSet dataSet = null;
        PlotterFrame plotFrame = null;

        public DataSetFormatMenuListener(DataSet dataSet, PlotterFrame plotFrame)
        {
            this.dataSet = dataSet;
            this.plotFrame = plotFrame;
        }

        public void actionPerformed(ActionEvent e)

        {
            logger.logComment("Action performed on DataSetFormatMenuListener");
           // System.out.println("Action: " + e);

            String selected = ((JMenuItem)e.getSource()).getText();

            if (selected.startsWith(formatMenuIndicator))
            {
                selected = selected.substring(formatMenuIndicator.length());
            }

            if (selected != null)
            {
                dataSet.setGraphFormat(  selected);
                plotFrame.updateMenus();
                // will hopefully have solved the problem, otherwise another
                // warning at repaint...
                plotFrame.removeProblemDueToBarSpacing();
            }

        };
    }




    public class DataSetZerosMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetZerosMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            StringBuffer sb = new StringBuffer();

            sb.append("// Interpolated zeros of     : " + dataSet.getRefrence() + "\n");

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

            for (int j = 1; j < xVals.length; j++)
            {
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
                }
            }

            sb.append("// There are "+count+" zeros in the data set:\n\n");
            sb.append(zeros.toString());

            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                         "Zeros of graph: "
                                                         + dataSet.getRefrence(),
                                                         12,
                                                         false,
                                                         false);

            simpleViewer.setFrameSize(700, 700);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;

            simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2,
                                     (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);

        };

    }








    public class DataSetAreaMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetAreaMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            StringBuffer sb = new StringBuffer();


            sb.append("Simple area under graph     : " + dataSet.getRefrence() + "\n");


            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            //StringBuffer zeros = new StringBuffer();
            //int count = 0;
            double totalarea = 0;

            for (int j = 1; j < xVals.length; j++)
            {
                double base = xVals[j] - xVals[j-1];
                if (base<0)
                {
                    GuiUtils.showErrorMessage(logger,
                          "The set of points are not sequential, and therefore the area cannot be calculated.", null, null);
                     return;
                }
                totalarea = totalarea + (base * yVals[j-1]);
                totalarea = totalarea + (base * (0.5 * (yVals[j]- yVals[j-1])));

            }

            sb.append("The area under the graph is: "+totalarea);
            //sb.append(zeros.toString())

            showSmallInfoBox("Area under graph: "
                               + dataSet.getRefrence(), sb.toString()     );

        };

    }







    public class DataSetAbsAreaMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetAbsAreaMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            StringBuffer sb = new StringBuffer();


            sb.append("Absolute area under graph     : " + dataSet.getRefrence() + "\n");


            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            //StringBuffer zeros = new StringBuffer();
            //int count = 0;
            double totalarea = 0;

            for (int j = 1; j < xVals.length; j++)
            {
                double base = xVals[j] - xVals[j-1];
                if (base<0)
                {
                    GuiUtils.showErrorMessage(logger,
                          "The set of points are not sequential, and therefore the absolute area cannot be calculated.", null, null);
                     return;
                }
                double thisArea = 0;

                if ((yVals[j]>=0 && yVals[j-1]>=0) ||
                    (yVals[j]<0 && yVals[j-1]<0))
                {
                    thisArea = (base * yVals[j-1]);
                    thisArea = thisArea + (base * (0.5 * (yVals[j]- yVals[j-1])));
                    thisArea = Math.abs(thisArea);
                    //System.out.println("thisArea:: "+thisArea);
                }
                else
                {
                    double zeroY = xVals[j-1] + (yVals[j-1] * (base/(yVals[j-1]-yVals[j])));

                    //System.out.println("zeroY: "+zeroY);
                    thisArea = Math.abs(0.5 * (yVals[j-1] *(zeroY-xVals[j-1])));
                    //System.out.println("thisArea:: "+thisArea);

                    thisArea = thisArea + Math.abs(0.5 * (yVals[j] *(xVals[j]-zeroY)));

                    //System.out.println("thisArea:: "+thisArea);
                }

                totalarea = totalarea + thisArea;

            }

            sb.append("The absolute area under the graph is: "+totalarea);
            //sb.append(zeros.toString())

            showSmallInfoBox("Area under graph: "
                             + dataSet.getRefrence(), sb.toString());


        };

    }




    public class DataSetPeaksMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetPeaksMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            String req = "Please enter the cutoff threshold which will be considered a spike.\n"
                + "Only maxima in the data above this value will be considered peaks";

            float suggestedThresh = -20;
            if (PlotterFrame.preferredSpikeValsEntered) suggestedThresh = plotCanvas.getSpikeOptions().getThreshold();

            String thresh = JOptionPane.showInputDialog(req, "" + suggestedThresh);

            if (thresh == null)return; // i.e. cancelled

            float threshold = 0;
            try
            {
                threshold = Float.parseFloat(thresh);
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, null);
                return;
            }

            StringBuffer sb = new StringBuffer();


            sb.append("// Times of peaks above "+threshold+"(m)V of     : " + dataSet.getRefrence() + "\n");


            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            //boolean pos = (yVals[0] > 0);

            StringBuffer peaks = new StringBuffer();
            int count = 0;

            for (int j = 2; j < xVals.length; j++)
            {
                if (yVals[j-2]< yVals[j-1] && yVals[j]<= yVals[j-1]
                    && yVals[j-1]>=threshold)
                {
                    peaks.append(xVals[j-1] + "\n");
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

            sb.append("// There are "+count+" peaks in the data set:\n\n");
            sb.append(peaks.toString());

            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                         "Peaks of graph: "
                                                         + dataSet.getRefrence(),
                                                         12,
                                                         false,
                                                         false);

            simpleViewer.setFrameSize(700, 700);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;

            simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2,
                                     (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);

        };

    }





    public class DataSetListPointsMenuListener implements ActionListener
    {
        DataSet dataSet = null;

        public DataSetListPointsMenuListener(DataSet dataSet)
        {
            this.dataSet = dataSet;
        }

        public void actionPerformed(ActionEvent e)
        {
            StringBuffer sb = new StringBuffer();


                sb.append("// Graph     : " + dataSet.getRefrence() + "\n");
            if (dataSet.getDescription() != null)
                sb.append("// Description     : " + dataSet.getDescription() + "\n");

            sb.append("// Number of points: " + dataSet.getNumberPoints() + "\n");
            sb.append("// Max X: " + dataSet.getMaxX()[0] + " (y value: "+dataSet.getMaxX()[1]+") \n");
            sb.append("// Min X: " + dataSet.getMinX()[0] + " (y value: "+dataSet.getMinX()[1]+") \n");
            sb.append("// Max Y: " + dataSet.getMaxY()[1] + " (x value: "+dataSet.getMaxY()[0]+") \n");
            sb.append("// Min Y: " + dataSet.getMinY()[1] + " (x value: "+dataSet.getMinY()[0]+") \n");

            double[] xVals = dataSet.getXValues();
            double[] yVals = dataSet.getYValues();

            for (int j = 0; j < xVals.length; j++)
            {
                sb.append(xVals[j]);
                if ( (xVals[j] + "").length() < 22)
                {
                    //sb.append("("+(xVals[j]+"").length()+")");
                    for (int k = 0; k < 22 - ( (xVals[j] + "").length()); k++)
                    {
                        sb.append(" ");
                    }
                }


                String comment = dataSet.getComment(j);
                if (comment==null) comment = "";
                else comment = DataSetManager.DATA_SET_COMMENT + " " + comment;

                 sb.append("  " + yVals[j] +"     "+ comment + "\n");

            }

            SimpleViewer simpleViewer = new SimpleViewer(sb.toString(),
                                                         "Values present in graph: "
                                                         + dataSet.getRefrence(),
                                                         12,
                                                         false,
                                                         false);

            simpleViewer.setFrameSize(700, 700);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = simpleViewer.getSize();

            if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
            if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;

            simpleViewer.setLocation( (screenSize.width - frameSize.width) / 2,
                                     (screenSize.height - frameSize.height) / 2);
            simpleViewer.setVisible(true);

        };

    }



    public void addSampleData()
    {
        Random rand = new Random();

        double minX = 0;
        double maxX = 20;
        int numPoints = 1001;

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


        for (int i = 0; i < numPoints; i++)
        {
            double x = minX + (maxX-minX)*((double)i/(int)(numPoints-1));

            double minonezeroone = (rand.nextDouble()*2) -1;

            double period = 4.222;

            double y1 = Math.max(0, 5*Math.cos(2*Math.PI*x /period) -2 + (0.5*minonezeroone));

            double regSpike =  x%(int)period == 0? 1:0;

            //double y2 = 3*Math.cos(x/.1) - 4;

            double y2 = regSpike;

            data1.addPoint(x,y1);
            data2.addPoint(x,y2);


        }
        data3.addPoint(3,3);
        data3.addPoint(0,-2);
        data3.addPoint(1,2);

        addDataSet(data1);
        addDataSet(data2);
        addDataSet(data3);

    }


    public void jMenuViewPointsOnly_actionPerformed(ActionEvent e)
    {
        plotCanvas.setViewMode(PlotCanvas.NORMAL_VIEW);
    }

    //Help | About action performed

    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            PlotManager.plotFrameClosing(plotFrameReference);
            if (standAlone) System.exit(0);
        }
    }

    protected void setStatus(String message)
    {
        jLabelStatusBar.setText(message);
    }

    public void setKeepDataSetColours(boolean val)
    {
        this.plotCanvas.setKeepDataSetColours(val);
    }

    public void setViewMode(String viewMode)
    {
        if (viewMode.equals(PlotCanvas.NORMAL_VIEW))
        {
            this.jMenuViewPointsOnly.setSelected(true);
        }
        else if (viewMode.equals(PlotCanvas.INCLUDE_ORIGIN_VIEW))
        {
            this.jMenuItemViewOrigin.setSelected(true);
        }
        else if (viewMode.equals(PlotCanvas.STACKED_VIEW))
        {
            this.jMenuItemStacked.setSelected(true);
        }
        else if (viewMode.equals(PlotCanvas.CROPPED_VIEW))
        {
            this.jMenuItemSelection.setSelected(true);
        }
        plotCanvas.setViewMode(viewMode); // necessary??
    }


    void jMenuItemViewOrigin_actionPerformed(ActionEvent e)
    {
        plotCanvas.setViewMode(PlotCanvas.INCLUDE_ORIGIN_VIEW);
    }

    void jMenuItemSelection_actionPerformed(ActionEvent e)
    {
        plotCanvas.setViewMode(PlotCanvas.CROPPED_VIEW);
    }

    void jMenuItemStacked_actionPerformed(ActionEvent e)
    {
        plotCanvas.setViewMode(PlotCanvas.STACKED_VIEW);
    }

    void jMenuItemClose_actionPerformed(ActionEvent e)
    {
        //System.out.println("clo...");
        this.setVisible(false);
    }

    void jMenuItemCustomView_actionPerformed(ActionEvent e)
    {

        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement> ();
        InputRequestElement maxx = new InputRequestElement("maxx", "Maximum X value", null,
                                                           (float)plotCanvas.getMaxXScaleValue() + "", "");
        inputs.add(maxx);
        InputRequestElement minx = new InputRequestElement("minx", "Minimum X value", null,
                                                           (float)plotCanvas.getMinXScaleValue() + "", "");
        inputs.add(minx);
        InputRequestElement maxy = new InputRequestElement("maxy", "Maximum Y value", null,
                                                           (float)plotCanvas.getMaxYScaleValue() + "", "");
        inputs.add(maxy);
        InputRequestElement miny = new InputRequestElement("miny", "Minimum Y value", null,
                                                           (float)plotCanvas.getMinYScaleValue() + "", "");
        inputs.add(miny);

        InputRequest dlg = new InputRequest( this, "Please enter the new view bounds","Enter view bounds", inputs, true);

        GuiUtils.centreWindow(dlg);
        dlg.setVisible(true);

        if (dlg.cancelled()) return;

        try
        {
            plotCanvas.setMaxMinScaleValues(Double.parseDouble(maxx.getValue()),
                                            Double.parseDouble(minx.getValue()),
                                            Double.parseDouble(maxy.getValue()),
                                            Double.parseDouble(miny.getValue()));
        }
        catch (NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error setting the view", ex, this);
            return;
        }

        plotCanvas.setViewMode(PlotCanvas.USER_SET_VIEW);
    }


    void jMenuItemShowAxes_actionPerformed(ActionEvent e)
    {
        plotCanvas.setShowAxes(jMenuItemShowAxes.isSelected());
        if (!jMenuItemShowAxes.isSelected())
        {
            plotCanvas.setShowAxisNumbering(false);
            plotCanvas.setShowAxisTicks(false);
            jMenuItemShowAxisNums.setEnabled(false);
            jMenuItemShowAxisTicks.setEnabled(false);
        }
        else
        {
            jMenuItemShowAxisNums.setEnabled(true);
            jMenuItemShowAxisTicks.setEnabled(true);
            jMenuItemShowAxisNums_actionPerformed(null);
            jMenuItemShowAxisTicks_actionPerformed(null);
        }

        logger.logComment("jMenuItemShowAxes_actionPerformed, repainting");
        plotCanvas.repaint();
    }

    void jMenuDifference_actionPerformed(ActionEvent e)
    {
        logger.logComment("----   Getting difference between "+plotCanvas.getDataSets().length+" data sets...");

        PlotterFrame frame = PlotManager.getPlotterFrame("Difference between graphs in "+ this.getTitle(), false, true);

        for (int firstDataSetIndex = 0; firstDataSetIndex < plotCanvas.dataSets.length; firstDataSetIndex++)
        {
            logger.logComment("firstDataSetIndex: "+firstDataSetIndex);
            for (int secondDataSetIndex = firstDataSetIndex+1; secondDataSetIndex < plotCanvas.dataSets.length; secondDataSetIndex++)
            {
                logger.logComment("secondDataSetIndex: " + secondDataSetIndex);
                logger.logComment("plotCanvas.dataSets.length: " + plotCanvas.dataSets.length);

                DataSet data0 = plotCanvas.dataSets[firstDataSetIndex];
                DataSet data1 = plotCanvas.dataSets[secondDataSetIndex];

                String name = data0.getRefrence() + " minus " + data1.getRefrence();

                String desc = "  *** Graph with description: ***\n" + data0.getDescription() +
                    "\n  ***has had the following subtracted from it: ***\n" + data1.getDescription();

                DataSet dataSet = new DataSet(name, desc, "", "", "", "");

                for (int pointIndex = 0; pointIndex < data0.getNumberPoints(); pointIndex++)
                {

                    if (data1.getNumberPoints() > pointIndex &&
                        data1.getPoint(pointIndex)[0] == data0.getPoint(pointIndex)[0])
                    {
                        dataSet.addPoint(data0.getPoint(pointIndex)[0],
                                         data0.getPoint(pointIndex)[1]
                                         - data1.getPoint(pointIndex)[1]);
                    }
                }
                if (dataSet.getNumberPoints() == 0)
                {
                    GuiUtils.showErrorMessage(logger, "Note: data sets " + data0.getRefrence()
                                              + " and " + data1.getRefrence()
                                              + " have no x values in common and so cannot be subtracted", null, this);
                }
                frame.addDataSet(dataSet);

            }
        }
    }



    void jMenuAverage_actionPerformed(ActionEvent e)
    {

        logger.logComment("----   Getting average of " + plotCanvas.getDataSets().length + " data sets...");

        String name = "Average of " + plotCanvas.getDataSets().length + " graphs in " + this.getTitle();

        DataSet ds0 = plotCanvas.dataSets[0];

        double[] xVals = ds0.getXValues();
        double[] allYVals = ds0.getYValues();

        for (int dataSetIndex = 1; dataSetIndex < plotCanvas.dataSets.length; dataSetIndex++)
        {
            DataSet nextDs = plotCanvas.dataSets[dataSetIndex];
            double[] nextXVals = nextDs.getXValues();
            if (!Arrays.equals(nextXVals, xVals))
            {
                GuiUtils.showErrorMessage(logger,
                    "Error, all data sets in the plot frame must have the same X values to get an average of the graphs", null, this);
                return;
            }
            double[] nextYVals = nextDs.getYValues();

            for (int i = 0; i < nextYVals.length; i++)
            {
                allYVals[i] += nextYVals[i];

                if (dataSetIndex == plotCanvas.dataSets.length - 1)
                {
                    logger.logComment("Final val at "+i+": " + nextYVals[i]+", total: "+ allYVals[i]);
                    allYVals[i] = allYVals[i] / (float)plotCanvas.dataSets.length;
                    logger.logComment("Average: "+ allYVals[i]);
                }
            }
        }

        PlotterFrame frame = PlotManager.getPlotterFrame(name, false, true);

        String desc = name;

        DataSet dataSet = new DataSet(name, desc, ds0.getXUnit(), ds0.getYUnit(), ds0.getXLegend(), ds0.getYLegend());
        for (int i = 0; i < xVals.length; i++)
        {
            dataSet.addPoint(xVals[i], allYVals[i]);
        }
        frame.addDataSet(dataSet);

    }


    void jMenuImportData_actionPerformed(ActionEvent e)
    {
        String lastDir = recentFiles.getMyLastExportPointsDir();

        if (lastDir == null) lastDir
            = project.getProjectMainDirectory().getAbsolutePath();

        File defaultDir = new File(lastDir);

        DataSet ds = addNewDataSet(defaultDir, this);

        if (ds!=null) this.addDataSet(ds);
    }

    public static DataSet addNewDataSet(File dirToLookIn, Component parent)
    {
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
        new ActionListener(){
            public void actionPerformed(ActionEvent e){
            GuiUtils.showInfoMessage(logger,
                                     "Data file import format",
                "Data files can be imported in either single column (y values) or double column (x, y values) format.\n"+
                "Each data entry must be followed by a carriage return, and lines not recognised as a sequence of numbers\n"
                +"separated by a space (or one of , : ;) are ignored.\n"
                +"Note: after the data points are imported the x axis values (or y axis values) can be corrected (e.g. scaled by 1000)\n"
                +"by selecting "+PlotterFrame.generateNew+" in the Data Set's menu", chooser);}});
        helpPanel.add(helpButton);

        chooser.setAccessory(helpPanel);


        int retval = chooser.showDialog(parent, "Choose data file");


        if (retval == JOptionPane.OK_OPTION)
        {
            File file = chooser.getSelectedFile();
            String ref = file.getName();
            if (ref.lastIndexOf(".")>0)
            {
                ref = ref.substring(0, ref.lastIndexOf("."));
            }
            DataSet ds = new DataSet(ref, "Data loaded from file: "+ file.getAbsolutePath(),"","","","");

            try
            {
                Reader in = new FileReader(file);

                BufferedReader lineReader = new BufferedReader(in);
                String nextLine = null;

                int lineNumber = 0;
                int dataPointIndex = 0;

                while ( (nextLine = lineReader.readLine()) != null)
                {

                    logger.logComment("Looking at line num "+lineNumber+": " + nextLine);

                    String[] words = null;
                    nextLine = nextLine.trim();

                    if (nextLine.indexOf(",")>=0)
                    {
                        words = nextLine.split(",");
                    }
                    else if (nextLine.indexOf(";")>=0)
                    {
                        words = nextLine.split(";");
                    }
                    else if (nextLine.indexOf(":")>=0)
                    {
                        words = nextLine.split(":");
                    }
                    else
                    {
                        words = nextLine.split("\\s+");
                    }
                    boolean dataLine = true;

                    for (int i = 0; i < words.length; i++)
                    {
                        try
                        {
                            Double.parseDouble(words[i].trim());
                            logger.logComment("Found a number: (" + words[i].trim() +")");
                        }
                        catch (NumberFormatException ex1)
                        {

                            logger.logComment("Not a number: (" + words[i].trim() +")");
                            dataLine = false;
                        }
                    }
                    if (dataLine)
                    {
                        if (words.length==1)
                        {
                            ds.addPoint(dataPointIndex, Double.parseDouble(words[0].trim()));
                            dataPointIndex++;
                        }
                        else if (words.length==2)
                        {
                            ds.addPoint(Double.parseDouble(words[0].trim()), Double.parseDouble(words[1].trim()));
                            dataPointIndex++;
                        }
                        else
                        {
                            logger.logComment("Line has too many entries...");
                        }


                    }
                    else
                    {
                        logger.logComment("Unrecognised line...");
                    }



                    lineNumber++;
                }
            }
            catch(Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Error loading data from file: "+file, ex, null);
                ds.setDescription(ds.getDescription()+"\n\nError loading data from file: "+file+"\n"+ex.getMessage());
            }


        return ds;


        }

        return null;

    }



    void jMenuAddManual_actionPerformed(ActionEvent e)
    {

        int numPoints = 101;
        if (plotCanvas.dataSets.length>0)
            numPoints = plotCanvas.dataSets[0].getNumberPoints();
        

        float min = 0;
        float max = 10;

        if (plotCanvas.getDataSets().length>0)
        {
	        min = (float)plotCanvas.getMinXval();
	        max = (float)plotCanvas.getMaxXval();
        }

        this.addDataSet(addManualPlot(numPoints,
                        min,
                        max,
                        this));
    }

    public static DataSet addManualPlot(int numPoints, float minVal, float maxVal, Frame parent)
    {

        PlotEquationDialog dlg
            = new PlotEquationDialog(parent,
                                     "Please enter details on a plot to add to this frame",
                                     true,
                                     minVal,
                                     maxVal,
                                     numPoints);

        Dimension dlgSize = dlg.getPreferredSize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        dlg.setLocation( (screenSize.width - dlgSize.width) / 2,
                        (screenSize.height - dlgSize.height) / 2);
        dlg.setModal(true);
        dlg.pack();

        dlg.setVisible(true);

        return dlg.getGeneratedDataSet();

        //updateMenus();


    }


    void jMenuItemShowAxisNums_actionPerformed(ActionEvent e)
    {
        logger.logComment("jMenuItemShowAxisNums_actionPerformed...");
        plotCanvas.setShowAxisNumbering(jMenuItemShowAxisNums.isSelected());
        plotCanvas.repaint();
    }

    void jMenuItemShowAxisTicks_actionPerformed(ActionEvent e)
    {
        plotCanvas.setShowAxisTicks(jMenuItemShowAxisTicks.isSelected());
        plotCanvas.repaint();
    }


    public void rasterise(RasterOptions newRasterOptions)
    {
        rasterised = true;
        jMenuItemRasterise.setSelected(true);
        jMenuItemShowAxes.setSelected(false);
        jMenuItemShowAxes_actionPerformed(null);
        this.setViewMode(PlotCanvas.STACKED_VIEW);

        plotCanvas.setRasterOptions(newRasterOptions);
        plotCanvas.repaint();

    }

    public static void main(String[] args)
    {

        try
        {
            // UIManager.setLookAndFeel(favouredLookAndFeel);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Using look and feel: " + UIManager.getLookAndFeel().getDescription());

        new PlotterApp();


        if (true) return;
        String dir = "../models/MaexDeSchutter/Gran_layer_1D/results/";

        File[] results = new File[]{new File(dir+"granule_cells_test.history"),
            new File(dir+"mossy_fibers_test.history"),
            new File(dir+"Golgi_cells_test.history")};

        float startTime = 0;
        float endTime = 0;

        float delTime = 0.0001f;

        float up = 100;
        float down = -100;


        for (int res = 0; res < results.length; res++)
        {
            String name = results[res].getName();
            //DataSet ds = new DataSet(name, name);

            PlotterFrame frame = PlotManager.getPlotterFrame("Rasterplot for "+name);

            Vector<DataSet> dataSets = new Vector<DataSet>();
            try
            {
                Reader in = new FileReader(results[res]);
                LineNumberReader reader = new LineNumberReader(in);
                String nextLine = null;

                while ( (nextLine = reader.readLine()) != null)
                {
                    String[] nums = nextLine.trim().split("\\s++");
                    logger.logComment("Found: ");
                    for (int j = 0; j < nums.length; j++)
                    {
                        logger.logComment(j+": (" + nums[j]+")");
                    }
                    int cellIndex = Integer.parseInt(nums[0])-1;
                    float time = Float.parseFloat(nums[1])*1000;


                    if (dataSets.size()<cellIndex+1)
                    {
                        for (int i = dataSets.size(); i <= cellIndex; i++)
                        {
                            dataSets.add(i, null);
                            logger.logComment(dataSets.size()+" data sets now: " + dataSets);
                        }
                    }


                    //logger.logComment("-- Data sets now: " + dataSets);

                    try
                    {
                        logger.logComment("---  For cell "+cellIndex+" Data set already exists: " +dataSets.get(cellIndex).getRefrence());

                    }
                    catch (Exception e)

                    {
                        logger.logComment("Error: " + e.getMessage());
                        DataSet ds = new DataSet(name + ", cell: " + cellIndex, "Cell " + cellIndex, "", "", "", "");
                        dataSets.setElementAt(ds, cellIndex);
                        ds.addPoint(startTime, down);
                        logger.logComment("Created data set: " + ds.getRefrence());


                            logger.logComment(dataSets.size()+" data sets now: " + dataSets);
                    }
                    dataSets.get(cellIndex).addPoint(time-delTime, down);
                    dataSets.get(cellIndex).addPoint(time, up);
                    dataSets.get(cellIndex).addPoint(time+delTime, down);


                }
                in.close();


            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

            for (DataSet ds: dataSets)
            {
                if (ds!=null)
                {
                    logger.logComment("Data set: " + ds.getRefrence() +" has num points: "+ ds.getNumberPoints());
                    frame.addDataSet(ds);
                }
            }

            frame.setVisible(true);
        }


    }

    void jMenuItemRasterise_actionPerformed(ActionEvent e)
    {
        if (jMenuItemRasterise.isSelected())
        {
            ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();
            RasterOptions newRasterOptions = new RasterOptions();

            String req = "Percentage of the height of the plot to use";

            InputRequestElement percentInput = new InputRequestElement("percentage", req, null, newRasterOptions.getPercentage()+"", "");
            inputs.add(percentInput);

            req = "Cutoff threshold which will be considered a spike";

            InputRequestElement threshInput = new InputRequestElement("threshold", req, null, newRasterOptions.getThreshold()+"", "mV");
            inputs.add(threshInput);


            InputRequest dlg = new InputRequest(null,
                                                "Please enter the parameters for the rasterplot",
                                               "Parameters for the rasterplot",
                                               inputs, true);

            GuiUtils.centreWindow(dlg);

            dlg.setVisible(true);


            if (dlg.cancelled()) return;


            //String percent = JOptionPane.showInputDialog(req, "" + newRasterOptions.getPercentage());
            try
            {
                newRasterOptions.setPercentage(Float.parseFloat(percentInput.getValue()));
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid precentage", ex, this);
                return;
            }
            if (newRasterOptions.getPercentage() < 0 ||
                newRasterOptions.getPercentage() > 100)
            {
                GuiUtils.showErrorMessage(logger, "Invalid precentage", null, this);
                return;
            }


            //String thresh = JOptionPane.showInputDialog(req, "" + newRasterOptions.getThreshold());
            try
            {
                newRasterOptions.setThreshold(Float.parseFloat(threshInput.getValue()));
            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger, "Invalid threshold", ex, this);
                return;
            }

            rasterise(newRasterOptions);

        }
        else
        {
            rasterised = false;

            jMenuItemShowAxes.setSelected(true);
            jMenuItemShowAxes_actionPerformed(null);


            plotCanvas.setRasterOptions(null);


            plotCanvas.repaint();
        }
        logger.logComment("Done jMenuItemRasterise_actionPerformed...");

    }



}
