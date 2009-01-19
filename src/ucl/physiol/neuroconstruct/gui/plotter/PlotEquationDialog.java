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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

/**
 * Dialog to allow any equation to be placed on the PlotterFrame
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class PlotEquationDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("PlotEquationDialog");

    DataSet generatedDataSet = null;


    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JLabel jLabelVariable = new JLabel();
    JTextField jTextFieldVariable = new JTextField();
    JLabel jLabelMaxVal = new JLabel();
    JTextField jTextFieldMax = new JTextField();
    JLabel jLabelMinVal = new JLabel();
    JTextField jTextFieldMin = new JTextField();
    JLabel jLabelNumPoints = new JLabel();
    JTextField jTextFieldNumPoints = new JTextField();
    JLabel jLabelExpression = new JLabel();
    JTextField jTextFieldExpression = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();

    public PlotEquationDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal); try
        {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE); jbInit();
            pack();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public PlotEquationDialog(Frame owner,
                              String title,
                              boolean modal,
                              float minx,
                              float maxx,
                              int numPoints)
    {
        super(owner, title, modal); try
        {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();


            jTextFieldMax.setText(maxx + "");
            jTextFieldMin.setText(minx + "");
            jTextFieldNumPoints.setText(numPoints + "");

            pack();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    public PlotEquationDialog()
    {
        this(new Frame(), "PlotEquationDialog", false);
    }

    private void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);

        jButtonCancel.setText("Cancel"); jButtonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        }); jButtonOK.setText("OK"); jButtonOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOK_actionPerformed(e);
            }
        }); jLabelVariable.
            setToolTipText("");

        jLabelVariable.setText("Variable name:");

        jTextFieldVariable.setText("x");

        jTextFieldVariable.setColumns(12);
        jLabelMaxVal.setText("Maximum value:");
        jTextFieldMax.setText("10");
        jTextFieldMax.setColumns(12);
        jLabelMinVal.setText("Minimum value:");
        jTextFieldMin.setToolTipText("");
        jTextFieldMin.setText("-10");
        jTextFieldMin.setColumns(12); jLabelNumPoints.setText("Number of Points to plot:");
        jTextFieldNumPoints.setToolTipText("");
        jTextFieldNumPoints.setText("100");
        jLabelExpression.setText("Expression:"); jTextFieldExpression.setText(
            "sin(x)"); jTextFieldExpression.setColumns(12);

            jPanelMain.setLayout(gridBagLayout1);
            getContentPane().add(panel1);

            panel1.add(jPanelMain, java.awt.BorderLayout.CENTER);
            jPanelButtons.add(jButtonOK);
            jPanelButtons.add(jButtonCancel);

            panel1.add(jPanelButtons, java.awt.BorderLayout.SOUTH);

            jPanelMain.add(jTextFieldVariable,
                                   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                                                          , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                          new Insets(6, 12, 6, 12), 0, 0));

                    jPanelMain.add(jLabelMaxVal, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                                        , GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                                        new Insets(6, 12, 6, 12), 0, 0));

                    jPanelMain.add(
                        jTextFieldMax, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                                                              , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                              new Insets(6, 12, 6, 12), 0, 0));

                    jPanelMain.add(jLabelMinVal,
                                   new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                          , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 12), 0, 0));

                    jPanelMain.add(jTextFieldMin, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                                                                         , GridBagConstraints.WEST,
                                                                         GridBagConstraints.HORIZONTAL, new Insets(6, 12, 6, 12), 0, 0));

                    jPanelMain.add(jLabelNumPoints,
                                   new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                          , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 12), 0, 0));


                    jPanelMain.add(jTextFieldNumPoints, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                                                                               , GridBagConstraints.WEST,
                                                                               GridBagConstraints.HORIZONTAL,
                                                                               new Insets(6, 12, 6, 12), 0, 0));

        jPanelMain.add(jLabelVariable,
            new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                   , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 12), 0, 0)); jPanelMain.add(jTextFieldExpression,
            new GridBagConstraints(0, 5, 2, 1, 1.0, 0.0
                                   , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 12, 6, 12),
                                   0, 0)); jPanelMain.add(jLabelExpression, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 12, 6, 12), 0, 0));
    }



    public static void main(String[] args)
    {

        PlotEquationDialog dlg = new PlotEquationDialog();
        Dimension dlgSize = dlg.getPreferredSize();
        //Dimension frmSize = getSize();
        //Point loc = getLocation();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


        dlg.setLocation( (screenSize.width - dlgSize.width) / 2 ,
                        (screenSize.height - dlgSize.height) / 2 );
        dlg.setModal(true);
        dlg.pack();


        dlg.setVisible(true);


        PlotterFrame frame = PlotManager.getPlotterFrame("Noo plot", true, true);

        frame.addDataSet(dlg.getGeneratedDataSet());

        frame.setVisible(true);




    }


    public DataSet getGeneratedDataSet()
    {
        return this.generatedDataSet;
    }

    public void jButtonCancel_actionPerformed(ActionEvent e)
    {
        this.dispose();
    }

    public void jButtonOK_actionPerformed(ActionEvent e)
    {
        try
        {
            Variable x = new Variable(jTextFieldVariable.getText());



            int numPoints = Integer.parseInt(jTextFieldNumPoints.getText());
            double max = Double.parseDouble(jTextFieldMax.getText());
            double min = Double.parseDouble(jTextFieldMin.getText());

            String expression = jTextFieldExpression.getText();

            EquationUnit func = Expression.parseExpression(expression,
                                      new Variable[]{x});

            String plotName = "Plot of function: "+ func.getNiceString();

            generatedDataSet = new DataSet(plotName, plotName, "", "", "", "");

            for (int i = 0; i < numPoints; i++)
            {

                double nextXval = min + ( ( (max - min) / (numPoints - 1)) * i);

                Argument[] a0 = new Argument[]
                    {new Argument(x.getName(), nextXval)};

                generatedDataSet.addPoint(nextXval, func.evaluateAt(a0));
            }


/*
            PlotterFrame frame = PlotManager.getPlotterFrame(plotName, true);

            frame.addDataSet(ds);

            frame.setVisible(true);
*/
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem parsing those values: " + ex.getMessage(), ex, this);
            return;
        }

        this.dispose();
    }
}
