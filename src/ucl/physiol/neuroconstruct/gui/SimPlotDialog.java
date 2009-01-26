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

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog to specify  plots to show during simulations
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SimPlotDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("SimPlotDialog");

    Hashtable textFieldsForParameters = new Hashtable();

    Project project = null;


    ToolTipHelper toolTipText = ToolTipHelper.getInstance();

    boolean cancelled = false;
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelTop = new JPanel();

    Border border1;
    JPanel jPanelNames = new JPanel();
    JLabel jLabelPlotReference = new JLabel();
    JTextField jTextFieldPlotReference = new JTextField();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabelCellGroups = new JLabel();
    JComboBox jComboBoxCellGroup = new JComboBox();
    JLabel jLabelCellNumber = new JLabel();
    JTextField jTextFieldCellNumber = new JTextField();
    JLabel jLabelValuePlotted = new JLabel();
    JTextField jTextFieldValuePlotted = new JTextField();
    JLabel jLabelMin = new JLabel();
    JTextField jTextFieldMin = new JTextField();
    JLabel jLabelMax = new JLabel();
    JTextField jTextFieldMax = new JTextField();
    JLabel jLabelSegment = new JLabel();
    JTextField jTextFieldSegmentId = new JTextField();
    JLabel jLabelGraphWin = new JLabel();
    JComboBox jComboBoxGraphWin = new JComboBox();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JComboBox jComboBoxKnownValues = new JComboBox();
    JLabel jLabelOr = new JLabel();
    JLabel jLabelCellNum2 = new JLabel();


    JLabel jLabelPlotSave = new JLabel();
    JComboBox jComboBoxPlotSave = new JComboBox();


    public SimPlotDialog(Dialog owner, String suggestedRef, Project project)
    {
        super(owner, "Choose something to plot/save during the simulation", false);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            this.project = project;
            jbInit();
            extraInit();
            jTextFieldPlotReference.setText(suggestedRef);
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

    }


    public SimPlotDialog(Frame owner, String suggestedRef, Project project)
    {
        super(owner, "Choose something to plot/save during the simulation", false);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            this.project = project;
            jbInit();
            extraInit();
            jTextFieldPlotReference.setText(suggestedRef);
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }


    private void extraInit()
    {
        logger.logComment("-----------------------------New SimPlotDialog created");

        ArrayList<String> cellGroupNames = project.cellGroupsInfo.getAllCellGroupNames();
        for (int i = 0; i < cellGroupNames.size(); i++)
        {
                jComboBoxCellGroup.addItem(cellGroupNames.get(i));
        }

        int suggestedGraphWinNum = 0;
        String suggestedGraphWin = "GraphWin_"+ suggestedGraphWinNum;

        Vector graphWins = project.simPlotInfo.getAllGraphWindows();

        while (graphWins.contains(suggestedGraphWin))
        {
            suggestedGraphWinNum++;
            suggestedGraphWin = "GraphWin_"+ suggestedGraphWinNum;
        }

        jComboBoxGraphWin.addItem(suggestedGraphWin);

        for (int i = 0; i < graphWins.size(); i++)
        {
            if (!suggestedGraphWin.equals(graphWins.elementAt(i)))
                jComboBoxGraphWin.addItem(graphWins.elementAt(i));
        }
        /** @todo More values will be added here... */
        jComboBoxKnownValues.addItem(SimPlot.VOLTAGE);
        Vector allChanMechs = project.cellMechanismInfo.getAllChannelMechanismNames();
        for (int i = 0; i < allChanMechs.size(); i++)
        {
            //String next = (String) allChanMechs.elementAt(i);
           ///////////////////// jComboBoxKnownValues.addItem(next + ":" + SimPlot.PLOT_COND_DENS);
        }

        this.jComboBoxPlotSave.addItem(SimPlot.PLOT_ONLY);
        this.jComboBoxPlotSave.addItem(SimPlot.SAVE_ONLY);
        this.jComboBoxPlotSave.addItem(SimPlot.PLOT_AND_SAVE);

        String varHelp = toolTipText.getToolTip("Variables to plot/save");

        this.jLabelValuePlotted.setToolTipText(varHelp);
        this.jComboBoxKnownValues.setToolTipText(varHelp);
        this.jTextFieldValuePlotted.setToolTipText(varHelp);

    }



    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            doCancel();
        }
        super.processWindowEvent(e);
    }
    //Close the dialog

    void doCancel()
    {
        logger.logComment("Actions for cancel...");

        cancelled = true;

    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {

        logger.logComment("OK pressed...");

        try
        {
            if (Float.parseFloat(jTextFieldMax.getText()) < Float.parseFloat(jTextFieldMin.getText()))
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please make sure the maximum value for the plot is larger than the minimum",
                                          null, this);
                return;
            }
        }
        catch(NumberFormatException ex)
        {
            GuiUtils.showErrorMessage(logger, "Please enter valid values for the maximum and minimum of the plotted values", ex, this);
            return;
        }


        try
        {
            Integer.parseInt(jTextFieldCellNumber.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            if (!jTextFieldCellNumber.getText().trim().equals("*") &&
                !jTextFieldCellNumber.getText().trim().endsWith("%")&&
                !jTextFieldCellNumber.getText().trim().endsWith("#"))
            {
                GuiUtils.showErrorMessage(logger, "Please enter a positive integer or * for the cell number", ex, this);
                return;
            }
        }



        try
        {
            Integer.parseInt(jTextFieldSegmentId.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            if (!jTextFieldSegmentId.getText().trim().equals("*"))
            {
                GuiUtils.showErrorMessage(logger, "Please enter a positive integer or * for the segment ID", ex, this);
                return;
            }
        }





        this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }




    public SimPlotDialog()
    {
        try
        {
            jbInit();

            textFieldsForParameters = new Hashtable();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createEmptyBorder(6,6,6,6);
        this.getContentPane().setLayout(borderLayout1);
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
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

        jPanelMain.setLayout(gridBagLayout1);
        jLabelPlotReference.setText("Reference:");
        jTextFieldPlotReference.setText("...");
        jTextFieldPlotReference.setColumns(12);
        jPanelTop.setLayout(borderLayout4);
        jLabelCellGroups.setText("Cell Group:");
        jLabelCellNumber.setText("Cell Number (or * for all cells, n% for a random");
        jTextFieldCellNumber.setText("0");
        jLabelValuePlotted.setText("Value to be plotted/saved:");
        jLabelMin.setText("Min value (only needed when plotting):");
        jTextFieldMin.setText("-90");
        jLabelMax.setText("Max value (only needed when plotting):");
        jTextFieldMax.setText("50");
        jLabelSegment.setText("Segment Id (or * for all segments on cell):");
        jTextFieldSegmentId.setText("0");
        jLabelGraphWin.setText("Graph window: (only needed when plotting)");
        jLabelOr.setVerifyInputWhenFocusTarget(true);
        jLabelOr.setText("or");
        jTextFieldValuePlotted.setText("");
        jTextFieldValuePlotted.setColumns(16);
        jLabelCellNum2.setText("n percent of cells, or n# for a max of n cells)");

        this.jLabelPlotSave.setText("Plot values during simulation, save them, or both");

        jComboBoxKnownValues.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxKnownValues_itemStateChanged(e);
            }
        });
        this.getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        this.getContentPane().add(jPanelTop, BorderLayout.NORTH);
        jPanelTop.add(jPanelNames,  BorderLayout.NORTH);
        jPanelNames.add(jLabelPlotReference, null);
        jPanelNames.add(jTextFieldPlotReference, null);


        jPanelMain.add(jLabelCellGroups, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(6, 14, 6, 0), 0, 0));
        jPanelMain.add(jComboBoxCellGroup, new GridBagConstraints(1, 0, 3, 1, 1.0, 0.0
                                                                  , GridBagConstraints.CENTER,
                                                                  GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14),
                                                                  0, 0));
        jPanelMain.add(jLabelCellNumber, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(6, 14, 0, 14), 0, 0));
        jPanelMain.add(jTextFieldCellNumber, new GridBagConstraints(1, 1, 3, 2, 1.0, 0.0
                                                                    , GridBagConstraints.WEST,
                                                                    GridBagConstraints.HORIZONTAL, new Insets(6, 14, 6, 14),
                                                                    0, 0));
        jPanelMain.add(jLabelSegment, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                             , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                             new Insets(6, 14, 6, 0), 0, 0));
        jPanelMain.add(jTextFieldSegmentId, new GridBagConstraints(1, 3, 3, 1, 1.0, 0.0
                                                                   , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                                   new Insets(6, 14, 6, 14), 0, 0));
        jPanelMain.add(jLabelValuePlotted, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                                                                  , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                  new Insets(6, 14, 6, 0), 0, 0));
        jPanelMain.add(jTextFieldValuePlotted, new GridBagConstraints(3, 4, 1, 1, 1.0, 0.0
                                                                      , GridBagConstraints.WEST,
                                                                      GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 14),
                                                                      0, 0));
        jPanelMain.add(jLabelMin, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                         , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                         new Insets(6, 14, 6, 0), 0, 0));
        jPanelMain.add(jTextFieldMin, new GridBagConstraints(1, 5, 3, 1, 1.0, 0.0
                                                             , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                             new Insets(6, 14, 6, 14), 0, 0));
        jPanelMain.add(jLabelMax, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                                                         , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                         new Insets(6, 14, 6, 0), 0, 0));
        jPanelMain.add(jTextFieldMax, new GridBagConstraints(1, 6, 3, 1, 1.0, 0.0
                                                             , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                             new Insets(6, 14, 6, 14), 0, 0));

        jPanelMain.add(jComboBoxKnownValues, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
                                                                    , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                                    new Insets(6, 14, 6, 6), 0, 0));
        jPanelMain.add(jLabelOr, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
                                                        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                        new Insets(6, 6, 6, 6), 0, 0));
        jPanelMain.add(jLabelCellNum2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(0, 14, 6, 0), 0, 0));



          jPanelMain.add(jLabelGraphWin, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(6, 14, 6, 0), 0, 0));
          jPanelMain.add(jComboBoxGraphWin, new GridBagConstraints(1, 7, 3, 1, 1.0, 0.0
                                                                   , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                                                   new Insets(6, 14, 6, 14), 0, 0));

          jPanelMain.add(this.jLabelPlotSave, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
                                                                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                new Insets(6, 14, 6, 0), 0, 0));

          jPanelMain.add(this.jComboBoxPlotSave, new GridBagConstraints(1, 8, 3, 1, 1.0, 0.0
                                                                   , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                                                   new Insets(6, 14, 6, 14), 0, 0));


    }


    public SimPlot getFinalSimPlot()
    {
        SimPlot simPlot = new SimPlot();
        simPlot.setPlotReference(jTextFieldPlotReference.getText());
        simPlot.setCellGroup((String)jComboBoxCellGroup.getSelectedItem());
        simPlot.setCellNumber(jTextFieldCellNumber.getText());
        simPlot.setSegmentId(jTextFieldSegmentId.getText());

        if (jTextFieldValuePlotted.getText().trim().length()>0)
            simPlot.setValuePlotted(jTextFieldValuePlotted.getText());
        else
            simPlot.setValuePlotted((String)jComboBoxKnownValues.getSelectedItem());


        simPlot.setMaxValue(Float.parseFloat(jTextFieldMax.getText()));
        simPlot.setMinValue(Float.parseFloat(jTextFieldMin.getText()));
        simPlot.setGraphWindow((String)jComboBoxGraphWin.getSelectedItem());

        simPlot.setPlotAndOrSave((String)jComboBoxPlotSave.getSelectedItem());

        return simPlot;
    }


    public void setSimPlot(SimPlot simPlot)
    {
        logger.logComment("Resetting values in dialog");
        jTextFieldPlotReference.setText(simPlot.getPlotReference());
        jComboBoxCellGroup.setSelectedItem(simPlot.getCellGroup());
        jTextFieldCellNumber.setText(simPlot.getCellNumber());
        jTextFieldSegmentId.setText(simPlot.getSegmentId());

        //boolean valueFound = false;
        for (int i = 0; i < jComboBoxKnownValues.getItemCount(); i++)
        {
                String nextItem = (String)jComboBoxKnownValues.getItemAt(i);
                if (nextItem.equals(simPlot.getValuePlotted()))
                {
                    jComboBoxKnownValues.setSelectedIndex(i);
                    //valueFound = true;
                }
        }
        jTextFieldValuePlotted.setText(simPlot.getValuePlotted());

        jTextFieldMax.setText(simPlot.getMaxValue()+"");
        jTextFieldMin.setText(simPlot.getMinValue()+"");

        Vector graphWins = project.simPlotInfo.getAllGraphWindows();
        jComboBoxGraphWin.removeAllItems();


        int suggestedGraphWinNum = graphWins.size();
        String suggestedGraphWin = "GraphWin_" + suggestedGraphWinNum;

        while (graphWins.contains(suggestedGraphWin))
        {
            suggestedGraphWinNum++;
            suggestedGraphWin = "GraphWin_" + suggestedGraphWinNum;
        }



        for (int i = 0; i < graphWins.size(); i++)
        {
            //if (!jComboBoxGraphWin.getit)
            jComboBoxGraphWin.addItem(graphWins.elementAt(i));
        }


        jComboBoxGraphWin.addItem(suggestedGraphWin);


        jComboBoxGraphWin.setSelectedItem(simPlot.getGraphWindow());

        this.jComboBoxPlotSave.setSelectedItem(simPlot.getPlotAndOrSave());

    }



    public static void main(String[] args)
    {
        try
        {
            Project p = Project.loadProject(new File("projects/PlotSave/PlotSave.neuro.xml").getCanonicalFile(), null);

            String suggestedName = "Plot_0";

            SimPlotDialog dlg = new SimPlotDialog(new Frame(),
                                                  suggestedName,
                                                  p);

            SimPlot s = dlg.getFinalSimPlot();
            s.setCellGroup("CellGroup_2");
            s.setMaxValue(222);

            dlg.setSimPlot(s);

            //Dimension dlgSize = dlg.getPreferredSize();
            dlg.setModal(true);
            dlg.pack();
            dlg.setVisible(true);



        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }

    void jComboBoxKnownValues_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()==ItemEvent.SELECTED)
        {
            jTextFieldValuePlotted.setText((String)jComboBoxKnownValues.getSelectedItem());
        }
    }


}
