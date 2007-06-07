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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import javax.swing.event.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Dialog for managing saved data sets
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class DataSetManager extends JFrame implements ListSelectionListener


{
    private static ClassLogger logger = new ClassLogger("DataSetManager");

    //SimulationsInfo allSims = null;

    Project project = null;

    DataSetInfo allDataSets = new DataSetInfo(new File(""));

    public boolean cancelled = false;

    boolean standAlone = false;

    final String defaultPlotLocation = "In new frame";


    public static final String DATA_SET_PREFIX = "DataSet_";


    public static final String DATA_SET_NUM_POINTS_PARAM = "numPoints";
    public static final String DATA_SET_REF_PARAM = "reference";
    public static final String DATA_SET_DESCRIPTION_PARAM = "description";
    public static final String DATA_SET_GRAPH_FORMAT_PARAM = "graphFormat";
    public static final String DATA_SET_GRAPH_COLOUR_PARAM = "graphColour";


    public static final String DATA_SET_X_UNITS = "xUnits";
    public static final String DATA_SET_Y_UNITS = "yUnits";
    public static final String DATA_SET_X_LEGEND = "xLegend";
    public static final String DATA_SET_Y_LEGEND = "yLegend";




    public static final String DATA_SET_COMMENT = "//";

    public static final String DATA_SET_PARAM_PREFIX = "@";



    JMenuBar jMenuBarMainMenu = new JMenuBar();
    //JMenu jMenuColunms = new JMenu();


    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelSelection = new JPanel();
    JButton jButtonPlot = new JButton("Plot");
    JButton jButtonClose = new JButton("Close");
    JButton jButtonRefresh = new JButton("Refresh");
    GridLayout gridLayout1 = new GridLayout();
    JScrollPane jScrollPaneMain = new JScrollPane();
    JTable jTableDataSets  = new JTable(allDataSets);
    JButton jButtonDelete = new JButton("Delete");
    JButton jButtonRename = new JButton("Rename");
    JPanel jPanel1 = new JPanel();
    JPanel jPanel2 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JComboBox jComboBoxPlotFrames = new JComboBox();
    JButton jButtonNew = new JButton("New");
    JTextField jTextFieldSelected = new JTextField();
    JButton jButtonEdit = new JButton("Edit");
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel3 = new JPanel();
    JButton jButtonDesc = new JButton();
    JPanel jPanelDesc = new JPanel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JEditorPane jEditorPane1 = new JEditorPane();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border1 = BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
        Color.white, Color.white, new Color(103, 101, 98), new Color(148, 145, 140)),
                                                        BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Border border2 = BorderFactory.createEmptyBorder(8, 8, 8, 8);


    private DataSetManager()
    {

    }

    public DataSetManager(Project project, boolean standAlone)
    {
        super("Saved Data Set Manager");

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        this.project = project;

        //Vector columnsToShow = recentFiles.getPreferredSimBrowserCols();

        File dataSetDir = ProjectStructure.getDataSetsDir(project.getProjectMainDirectory());
        allDataSets = new DataSetInfo(dataSetDir);

        try
        {
            jTableDataSets  = new JTable(allDataSets);

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
        jButtonDesc.setEnabled(false);

        jButtonRename.setText("Rename");
        jButtonRename.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRename_actionPerformed(e);
            }
        });

        this.setJMenuBar(jMenuBarMainMenu);

        jPanelSelection.setPreferredSize(new Dimension(450, 300));

        jPanelDesc.setBorder(border2); //jEditorPane1.setEnabled(false);

        jEditorPane1.setBorder(BorderFactory.createLoweredBevelBorder());

        jScrollPaneMain.getViewport().add(jTableDataSets, null);


        jPanelMain.setLayout(borderLayout1);
        jPanelSelection.setBorder(BorderFactory.createEtchedBorder());
        jPanelSelection.setMinimumSize(new Dimension(604, 304));
        jPanelSelection.setLayout(gridLayout1);
        jPanelButtons.setBorder(BorderFactory.createEtchedBorder());

        jPanelButtons.setLayout(borderLayout2);
        jButtonPlot.setText("Plot Data Set:");

        jButtonPlot.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonPlot_actionPerformed(e);
            }
        });
        jButtonClose.setText("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonClose_actionPerformed(e);
            }
        });
        jButtonRefresh.setText("Reload Data Set list");
        jButtonRefresh.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonRefresh_actionPerformed(e);
            }
        });
        gridLayout1.setHgap(5);
        gridLayout1.setVgap(5);

       // jPanelMain.setMaximumSize(new Dimension(600, 400));
        jPanelMain.setPreferredSize(new Dimension(750, 560));
        jPanelMain.setMinimumSize(new Dimension(750, 560));

        jButtonDelete.setText("Delete");

        jButtonDelete.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDelete_actionPerformed(e);
            }
        });
        jButtonNew.setText("New Data Set");
        jButtonNew.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonNew_actionPerformed(e);
            }
        });
        jTextFieldSelected.setEditable(false);
        jTextFieldSelected.setText("");
        jTextFieldSelected.setColumns(10);
        jButtonEdit.setText("Edit Points");
        jButtonEdit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonEdit_actionPerformed(e);
            }
        });
        jScrollPaneMain.setMinimumSize(new Dimension(750, 400));
        jScrollPaneMain.setPreferredSize(new Dimension(750, 400));
        jPanel2.setMinimumSize(new Dimension(750, 35));
        jPanel2.setPreferredSize(new Dimension(750, 35)); jButtonDesc.setText("Edit Description"); jButtonDesc.
            addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonDesc_actionPerformed(e);
            }
        }); jPanelDesc.setMaximumSize(new Dimension(300, 170)); jPanelDesc.setMinimumSize(new Dimension(4, 170));
            jPanelDesc.setPreferredSize(new Dimension(831, 170));

            jPanelDesc.setLayout(borderLayout3);


        this.getContentPane().add(jPanelMain, BorderLayout.CENTER); jPanelSelection.add(jScrollPaneMain, null);
        jPanelMain.add(jPanelButtons, BorderLayout.SOUTH); jPanel2.add(jButtonRename, null); jPanel2.add(jButtonDesc);
            jPanelButtons.add(jPanel1,  BorderLayout.SOUTH);
        jPanel1.add(jButtonRefresh, null);
        jPanel1.add(jButtonNew, null);
        jPanel1.add(jButtonClose, null); jPanelButtons.add(jPanel3, java.awt.BorderLayout.NORTH); jPanel3.add(
            jTextFieldSelected); jPanel3.add(
            jButtonPlot); jPanel3.add(jComboBoxPlotFrames); jPanel2.add(jButtonEdit, null); jPanel2.add(
            jButtonDelete, null); jPanelButtons.add(jPanel2, java.awt.BorderLayout.CENTER); jPanelMain.add(jPanelSelection,
            java.awt.BorderLayout.NORTH); jPanelMain.add(jPanelDesc, java.awt.BorderLayout.CENTER);

        jScrollPane1.getViewport().add(jEditorPane1);

        jPanelDesc.add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }



    private void extraInit()
    {
        jTableDataSets.getSelectionModel().addListSelectionListener(this);
        jTableDataSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        refresh();
        //jListSimList.setSelectedIndex(0);
        logger.logComment("Finished initialising...");
        //if ()

        ToolTipHelper toolTips = ToolTipHelper.getInstance();
        //jButtonRefresh.setToolTipText(toolTips.getToolTip("Reload Simulation List"));
    }



    private void refresh()
    {
        allDataSets.refresh();


        int numColumns = allDataSets.getColumnCount();
        for (int i = 0; i < numColumns; i++)
        {
            TableColumn nextColumn = jTableDataSets.getColumnModel().getColumn(i);
            String name = allDataSets.getColumnName(i);
            if (name.equals(DataSetInfo.COL_NAME_DESC))
            {
                nextColumn.setPreferredWidth(300);
            }
            if (name.equals(DataSetInfo.COL_NAME_NAME))
            {
                nextColumn.setPreferredWidth(150);
            }


            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

            renderer.setToolTipText("Value of " + name);

            nextColumn.setCellRenderer(renderer);

        }

        jComboBoxPlotFrames.removeAllItems();

        jComboBoxPlotFrames.addItem(defaultPlotLocation);

        Vector allPlots = PlotManager.getPlotterFrameReferences();
        for (int i = 0; i < allPlots.size(); i++)
        {
            String next = (String) allPlots.elementAt(i);
            jComboBoxPlotFrames.addItem(next);
        }




    }

    public DataSet getSelectedDataSet() throws DataSetException
    {
        int selectedDataSet= jTableDataSets.getSelectedRow();
        return allDataSets.getDataSet(selectedDataSet);
    }


    public void valueChanged(ListSelectionEvent e)
    {
        logger.logComment("Table selection changed...");
        int selectedDataSet= jTableDataSets.getSelectedRow();

        if (selectedDataSet<0)
        {
            jTextFieldSelected.setText("");
            jEditorPane1.setText("");
        }
        else
        {
            try
            {
               // DataSet ds = allDataSets.getDataSet(selectedDataSet);
                jTextFieldSelected.setText( allDataSets.getDataSetReference(selectedDataSet) );
            jEditorPane1.setText( allDataSets.getDataSetDescription(selectedDataSet));

            }
            catch(DataSetException dse)
            {
                logger.logError("Error showing fields...", dse);
            }
        }
    }


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) jButtonClose_actionPerformed(null);
    }




    /**
     * Gets the Data Set from the selected file. If partial = true, only the description, etc,
     * is loaded NOT the points
     * @param dsFile the File containing the data
     * @param partial only loads the reference, description, etc from the comments at the beginning of the file
     *
     */
    public static DataSet loadFromDataSetFile(File dsFile, boolean partial) throws DataSetException
    {
        DataSet dataSet = new DataSet(null, null, "", "", "", "");

        String name = dsFile.getName();


        if (name.endsWith(ProjectStructure.getDataSetExtension()))
        {
            name = name.substring(0, name.length()
                                  -ProjectStructure.getDataSetExtension().length());
        }
        // temp name
        dataSet.setRefrence(name);
        // temp description
        dataSet.setDescription("Data loaded from file: "+ dsFile.getAbsolutePath());


        FileReader fr = null;
        try
        {
            fr = new FileReader(dsFile);
        }
        catch (FileNotFoundException ex)
        {
            throw new DataSetException("Could not find Data Set file: "+ dsFile, ex);
        }

        BufferedReader lineReader = new BufferedReader(fr);
        String nextLine = null;


        int lineNumber = 0;

        boolean reachedDataPoints = false;

        try
        {
            while ( ((nextLine = lineReader.readLine()) != null)
                    && !(reachedDataPoints && partial))  // so ignores points if partial is true
            {
                lineNumber++;
                logger.logComment("Next line: " + nextLine);
                nextLine = nextLine.trim();

                if (nextLine.startsWith(DATA_SET_COMMENT))
                {
                    String commentPart = nextLine.substring(DATA_SET_COMMENT.length()).trim();

                    if (commentPart.startsWith(DATA_SET_PARAM_PREFIX) &&
                        commentPart.indexOf("=")>0)
                    {
                        String paramName = commentPart.substring(DATA_SET_PARAM_PREFIX.length(),
                                                                 commentPart.indexOf("="));
                        String paramVal = commentPart.substring(commentPart.indexOf("=")+1);

                        logger.logComment("Found param: "+ paramName+ ", it's val is: "+ paramVal);

                        if (paramName.equals(DATA_SET_REF_PARAM)) dataSet.setRefrence(paramVal);
                        if (paramName.equals(DATA_SET_DESCRIPTION_PARAM))
                        {
                            String desc = GeneralUtils.replaceAllTokens(paramVal, "\\n", " - ");
                            dataSet.setDescription(desc);
                        };

                        if (paramName.equals(DATA_SET_GRAPH_FORMAT_PARAM)) dataSet.setGraphFormat(paramVal);

                        if (paramName.equals(DATA_SET_X_UNITS)) dataSet.setXUnit(paramVal);
                        if (paramName.equals(DATA_SET_Y_UNITS)) dataSet.setYUnit(paramVal);
                        if (paramName.equals(DATA_SET_X_LEGEND)) dataSet.setXLegend(paramVal);
                        if (paramName.equals(DATA_SET_Y_LEGEND)) dataSet.setYLegend(paramVal);



                        if (paramName.equals(DATA_SET_GRAPH_COLOUR_PARAM))
                        {
                            String red = paramVal.substring(paramVal.indexOf("r=")+2, paramVal.indexOf(",g="));
                            String green = paramVal.substring(paramVal.indexOf(",g=")+3, paramVal.indexOf(",b="));
                            String blue = paramVal.substring(paramVal.indexOf(",b=")+3, paramVal.indexOf("]"));

                            Color newColor = new Color(Integer.parseInt(red),
                                                       Integer.parseInt(green),
                                                       Integer.parseInt(blue));
                            dataSet.setGraphColour(newColor);
                        }

                    }
                    else
                    {
                        logger.logComment("Unknown comment: ("+commentPart+")");
                    }
                }
                else
                {
                    logger.logComment("Numbers line: " + nextLine);
                    String xVal = null;
                    String yVal = null;
                    String comment = null;

                    if (nextLine.indexOf(",") >= 0)
                    {
                        xVal = nextLine.substring(0, nextLine.indexOf(",")).trim();
                        String rest = nextLine.substring(nextLine.indexOf(",") + 1).trim();
                        if (rest.indexOf(DATA_SET_COMMENT)>=0)
                        {

                            yVal = rest.substring(0, rest.indexOf(DATA_SET_COMMENT));

                            comment = rest.substring(rest.indexOf(DATA_SET_COMMENT)
                                                     + DATA_SET_COMMENT.length()).trim();

                        }
                        else
                        {
                            yVal = rest;
                        }
                    }
                    else if (nextLine.indexOf(" ") > 0)
                    {
                        xVal = nextLine.substring(0, nextLine.indexOf(" ")).trim();
                        yVal = nextLine.substring(nextLine.indexOf(" ") + 1).trim();
                    }
                    else
                    {
                        throw new DataSetException("Problem reading Data Set file: " + dsFile +
                                                   "\nBadly formatted line: (" + nextLine+")");
                    }
                    try
                    {
                        double x = Double.parseDouble(xVal);
                        double y = Double.parseDouble(yVal);
                        int pointNum = dataSet.addPoint(x, y);

                        if (comment!=null)
                        {
                            dataSet.setCommentOnPoint(pointNum, comment);
                        }

                    }
                    catch (NumberFormatException ex1)
                    {
                        throw new DataSetException("Problem reading Data Set file: " + dsFile +
                                                   "\nBadly formatted line: " + nextLine, ex1);
                    }

                    reachedDataPoints = true;
                }


            }
        }
        catch (IOException ex1)
        {
            throw new DataSetException("Problem reading Data Set file: "+ dsFile, ex1);
        }

        //fr.read

        dataSet.setDataSetFile(dsFile);

        return dataSet;
    }


    private static String getParamComment(String paramName, String paramValue)
    {
        return DATA_SET_COMMENT + DATA_SET_PARAM_PREFIX
               + paramName + "=" + paramValue+"\n";

    }


    public static void saveDataSet(DataSet dataSet)
    {
        int dataSetCount = 0;
        String suggestedName = DATA_SET_PREFIX + dataSetCount +
            ProjectStructure.getDataSetExtension();


        File suggestedFile =  dataSet.getDataSetFile();

        logger.logComment("Exporting to file: " + suggestedFile);

        try
        {
            FileWriter fw = new FileWriter(suggestedFile);
            int numPoints = dataSet.getNumberPoints();
            fw.write(DATA_SET_COMMENT+" This is a file storing the contents of a neuroConstruct Data Set, along with some other information to facilitate management/display of the Data Set\n");

            fw.write(getParamComment(DataSetManager.DATA_SET_NUM_POINTS_PARAM,
                                     numPoints + ""));

            fw.write(getParamComment(DataSetManager.DATA_SET_REF_PARAM,
                                     dataSet.getRefrence()));

            String desc = dataSet.getDescription();
            if (desc.indexOf("\n") > 0)
            {
                logger.logComment("Reformatting the description...");
                desc = GeneralUtils.replaceAllTokens(desc, "\n", "\\n");
            }

            fw.write(getParamComment(DataSetManager.DATA_SET_DESCRIPTION_PARAM,
                                     desc));

            fw.write(getParamComment(DataSetManager.DATA_SET_GRAPH_FORMAT_PARAM,
                                     dataSet.getGraphFormat()));

            fw.write(getParamComment(DataSetManager.DATA_SET_GRAPH_COLOUR_PARAM,
                                     dataSet.getGraphColour().toString()));


            fw.write(getParamComment(DataSetManager.DATA_SET_X_UNITS,
                                     dataSet.getXUnit()));
            fw.write(getParamComment(DataSetManager.DATA_SET_Y_UNITS,
                                     dataSet.getYUnit()));
            fw.write(getParamComment(DataSetManager.DATA_SET_X_LEGEND,
                                     dataSet.getXLegend()));
            fw.write(getParamComment(DataSetManager.DATA_SET_Y_LEGEND,
                                     dataSet.getYLegend()));






            for (int i = 0; i < numPoints; i++)
            {
                String comment = dataSet.getComment(i);
                if (comment==null) comment = "";
                else comment = DATA_SET_COMMENT + " " + comment;
                fw.write(dataSet.getPoint(i)[0]
                         + ", " + dataSet.getPoint(i)[1]
                         + "  "+comment+"\n");
            }
            fw.close();

            dataSet.setDataSetFile(suggestedFile);



        }
        catch (IOException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem exporting those points", ex, null);
        }
    }


    void jButtonClose_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel button pressed");
        cancelled = true;

        this.dispose();

        if (standAlone) System.exit(0);
    }

    void jButtonPlot_actionPerformed(ActionEvent e)
    {
        logger.logComment("OK button pressed");

        if(jTableDataSets.getSelectedRowCount()==0 ||
           jTableDataSets.getSelectedRow()<0)
            return;

        DataSet dataSet = null;
        try
        {
            dataSet = (DataSet) allDataSets.getDataSet(jTableDataSets.getSelectedRow());
        }
        catch (DataSetException ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem plotting that Data Set\n"+ ex.getMessage(), ex, this);
            return;
        }

        String plotFrameRef = "Plot of "+dataSet.getRefrence();

        String selectedPlotFrame = (String)jComboBoxPlotFrames.getSelectedItem();
        if (!selectedPlotFrame.equals(defaultPlotLocation))
        {
            plotFrameRef = selectedPlotFrame;
        }

        PlotterFrame frame = PlotManager.getPlotterFrame(plotFrameRef);

        frame.addDataSet(dataSet);

        frame.setVisible(true);

        refresh();

      //  recentFiles.setPreferredSimBrowserCols(allSims.getAllShownColumns());
      //  recentFiles.saveToFile();
        //this.dispose();
    }

    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);

            //File simDir = new File("examples/MaexDeSchutter/simulations");

            File dataDir = new File("projects/temp/dataSets");

            //DataSetManager frame = new DataSetManager(dataDir, true);
            //frame.pack();
            //frame.setVisible(true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }

    void jButtonRefresh_actionPerformed(ActionEvent e)
    {
        logger.logComment("Refreshing view");
        refresh();
    }

    void jButtonDelete_actionPerformed(ActionEvent e)
    {
        logger.logComment("Deleting selected sim");

        if(jTableDataSets.getSelectedRowCount()==0) return;

        int[] selectedDataSets= jTableDataSets.getSelectedRows();

        for (int i = 0; i < selectedDataSets.length; i++)
        {
            //DataSet dataSet = null;
            try
            {
                File dataSetFile = allDataSets.getDataSetFile(selectedDataSets[i]);
                String name = (String)jTableDataSets.getValueAt(i, DataSetInfo.COL_NUM_NAME);

                //DataSet dataSet = allDataSets.getDataSet(selectedDataSets[i]);

                int yesNo = JOptionPane.showConfirmDialog(this, "This will permanently the Data Set: "
                                                          + name + " as contained in file: "
                                                          + dataSetFile.getAbsolutePath() + "\nContinue?",
                                                          "Delete Data Set file?",
                                                          JOptionPane.YES_NO_CANCEL_OPTION);

                if (yesNo == JOptionPane.NO_OPTION)
                {
                    logger.logComment("User cancelled...");
                    return;
                }

                dataSetFile.delete();
            }
            catch (DataSetException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem deleting that Data Set\n" + ex.getMessage(), ex, this);
            }

         //

        }
        refresh();

    }

    void jButtonRename_actionPerformed(ActionEvent e)
    {
        logger.logComment("Rename button pressed");

        if(jTableDataSets.getSelectedRowCount()==0) return;

        int selectedDataSet = jTableDataSets.getSelectedRow();


        try
        {
            DataSet dataSet = allDataSets.getDataSet(selectedDataSet);

            String newName = JOptionPane.showInputDialog(this, "Please enter the new name for the Data Set",
                                                         dataSet.getRefrence());

            if (newName==null) return;

            dataSet.setRefrence(newName);

            String goodFileName = GeneralUtils.getBetterFileName(newName
                                          + ProjectStructure.getDataSetExtension());

            File oldFile = dataSet.getDataSetFile();
            File newFile = new File(oldFile.getParentFile(), goodFileName);

            oldFile.delete();
            dataSet.setDataSetFile(newFile);

            saveDataSet(dataSet);

            refresh();

        }
        catch (DataSetException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem editing the Data Set in row: "
                                      + selectedDataSet, ex, null);
        }


    }

    void jButtonNew_actionPerformed(ActionEvent e)
    {

        int dataSetCount = 0;
        Vector allDataSetRefs = allDataSets.getAllDataSetRefs();

        String suggestedName = DATA_SET_PREFIX + dataSetCount;

        project.markProjectAsEdited();


        while (allDataSetRefs.contains(suggestedName))
        {
            dataSetCount++;
            suggestedName = DATA_SET_PREFIX + dataSetCount;

        }


        String newName = JOptionPane.showInputDialog(this, "Please enter the name of the new Data Set",
                                                     suggestedName);

        if (newName==null)
        {
            logger.logComment("User cancelled...");
            return;
        }

        DataSet newDataSet = new DataSet(newName, "-- No description --", "", "", "", "");
        newDataSet.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT);
        newDataSet.setGraphColour(Color.red);
        newDataSet.addPoint(0,0);

        File newFile = new File(allDataSets.getDataSetDir(),
                                newName + ProjectStructure.getDataSetExtension());

        newDataSet.setDataSetFile(newFile);

        saveDataSet(newDataSet);


        refresh();

    }

    void jButtonEdit_actionPerformed(ActionEvent e)
    {
        if(jTableDataSets.getSelectedRowCount()==0) return;

        int selectedDataSet = jTableDataSets.getSelectedRow();

        project.markProjectAsEdited();



        try
        {
            DataSet dataSet = allDataSets.getDataSet(selectedDataSet);
            EditPointsDialog dlg = new EditPointsDialog(this, dataSet, false);
            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = getSize();
            Point loc = getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);

            dlg.setVisible(true);

        }
        catch (DataSetException ex)
        {
            GuiUtils.showErrorMessage(logger,
                                      "Problem editing the Data Set in row: "
                                      + selectedDataSet, ex, null);
        }



    }

    public void jButtonDesc_actionPerformed(ActionEvent e)
    {

    }

}
