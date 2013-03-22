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

import java.awt.*;
import java.util.logging.Level;
import javax.swing.*;
import ucl.physiol.neuroconstruct.utils.*;
import javax.swing.border.*;
import java.awt.event.*;
import ucl.physiol.neuroconstruct.utils.equation.EquationException;

/**
 * Dialog for specifying a type of number generator, and the main parameters
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class NumberGeneratorDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("NumberGeneratorDialog");

    String fixedNumString = "Fixed number";
    String randomNumString = "Random distribution";
    String gaussianNumString = "Gaussian distribution";
    String expressionString = "Generic expression";

    boolean weightGeneratorMode = false;
    
    boolean cancelled = false;

    JLabel fixedValLabel = new JLabel("Value: ");
    JTextField fixedValTextField = new JTextField("0");

    JLabel maxValLabel = new JLabel("Maximum: ");
    JTextField maxValTextField = new JTextField("1");
    JLabel minValLabel = new JLabel("Minimum: ");
    JTextField minValTextField = new JTextField("0");


    JLabel meanValLabel = new JLabel("Mean: ");
    JTextField meanValTextField = new JTextField("0.5");
    JLabel stdDevValLabel = new JLabel("Standard deviation: ");
    JTextField stdDevValTextField = new JTextField("1");



    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();

    NumberGenerator numGen = null;
    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JLabel jLabelType = new JLabel();
    JComboBox jComboBoxType = new JComboBox();
    JPanel jPanelVariables = new JPanel();
    GridLayout gridLayout1 = new GridLayout();
    Border border1;
    JPanel jPanelIntOrFloat = new JPanel();
    JRadioButton jRadioButtonFloat = new JRadioButton();
    JRadioButton jRadioButtonInt = new JRadioButton();
    ButtonGroup buttonGroupNumType = new ButtonGroup();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanelType = new JPanel();

    JPanel jPanelComment = new JPanel();
    JPanel jPanelChoices = new JPanel();

    JLabel jLabelComment = new JLabel();
    
    JLabel jExpLabel = new JLabel("Expression f(r): ");
    JTextField jExpTexField = new JTextField();
    JCheckBox jChekBoxSomaToSoma = new JCheckBox();

    
    public NumberGeneratorDialog(Frame frame, String title, String comment, NumberGenerator numGenOriginal)
    {
        this(frame, title, comment, numGenOriginal, false);
    }
    
    
    public NumberGeneratorDialog(Frame frame, String title, String comment, NumberGenerator numGenOriginal, boolean weightGeneratorMode)
    {
        super(frame, "Number range properties", true);
        this.weightGeneratorMode = weightGeneratorMode;
        
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        this.setTitle(title);

        logger.logComment("NumberGeneratorDialog created with: "+ numGenOriginal);

        if (numGenOriginal == null)
            numGen = new NumberGenerator(1);
        else
            numGen = numGenOriginal;
        try
        {
            jbInit();
            this.jLabelComment.setText(comment);
            extraInit();
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public NumberGeneratorDialog(Dialog dia, String title, String comment, NumberGenerator numGenOriginal)
    {
        this(dia, title, comment, numGenOriginal, false);
        
    }
    
    public NumberGeneratorDialog(Dialog dia, String title, String comment, NumberGenerator numGenOriginal, boolean weightGeneratorMode)
    {
        super(dia, "Number range properties", true);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.weightGeneratorMode = weightGeneratorMode;

        this.setTitle(title);


        logger.logComment("NumberGeneratorDialog created with: "+ numGenOriginal);
        if (numGenOriginal == null)
            numGen = new NumberGenerator(1);
        else
            numGen = numGenOriginal;
        try
        {
            jbInit();
        this.jLabelComment.setText(comment);
            extraInit();
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private void jbInit() throws Exception
    {
        border1 = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),BorderFactory.createEmptyBorder(12,12,12,12));
        panel1.setLayout(borderLayout1);
        jButtonCancel.setText("Cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        jButtonOK.setText("OK");
        jButtonOK.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    jButtonOK_actionPerformed(e);
                } catch (EquationException ex) {
                    java.util.logging.Logger.getLogger(NumberGeneratorDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        jLabelType.setText("Type:");
        jPanelVariables.setBorder(border1);
        jPanelVariables.setLayout(gridLayout1);
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(2);
        gridLayout1.setVgap(10);
        jComboBoxType.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxType_itemStateChanged(e);
            }
        });
        jRadioButtonFloat.setSelected(true);
        jRadioButtonFloat.setText("Float generator");
        jRadioButtonFloat.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jRadioButtonFloat_itemStateChanged(e);
            }
        });
        jRadioButtonInt.setActionCommand("Integer generator");
        jRadioButtonInt.setText("Integer generator");
        jRadioButtonInt.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jRadioButtonInt_itemStateChanged(e);
            }
        });
        jPanelMain.setLayout(borderLayout2);
        getContentPane().add(panel1);
        panel1.add(jPanelMain, BorderLayout.NORTH);
        panel1.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        jPanelIntOrFloat.add(jRadioButtonInt, null);
        jPanelIntOrFloat.add(jRadioButtonFloat, null);
        panel1.add(jPanelVariables,  BorderLayout.CENTER);

        jLabelComment.setText("Please select the type of number...");
        jLabelComment.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jLabelComment.setHorizontalAlignment(JLabel.CENTER);
        jPanelMain.add(jLabelComment, BorderLayout.NORTH);
        jPanelMain.add(jPanelType, BorderLayout.CENTER);
        jPanelMain.add(jPanelIntOrFloat,  BorderLayout.SOUTH);


        jPanelType.add(jLabelType, null);
        jPanelType.add(jComboBoxType, null);


        buttonGroupNumType.add(jRadioButtonFloat);
        buttonGroupNumType.add(jRadioButtonInt);
    }


    private void extraInit()
    {
        jComboBoxType.addItem(fixedNumString);
        jComboBoxType.addItem(randomNumString);
        jComboBoxType.addItem(gaussianNumString);
        if (weightGeneratorMode)
            jComboBoxType.addItem(expressionString);

        if (numGen.distributionType==NumberGenerator.FIXED_NUM) jComboBoxType.setSelectedItem(fixedNumString);
        else if (numGen.distributionType==NumberGenerator.RANDOM_NUM) jComboBoxType.setSelectedItem(randomNumString);
        else if (numGen.distributionType==NumberGenerator.GAUSSIAN_NUM) jComboBoxType.setSelectedItem(gaussianNumString);
        else if (numGen.distributionType==WeightGenerator.FUNCTION) jComboBoxType.setSelectedItem(expressionString);

        if (numGen.numberType==NumberGenerator.INT_GENERATOR) jRadioButtonInt.setSelected(true);
        else jRadioButtonFloat.setSelected(true);

    }

    @Override
    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            doCancel();
        }
        //super.processWindowEvent(e);
    }


    public static void main(String[] args) throws EquationException
    {
        NumberGenerator ng = new NumberGenerator(1);

        //ng.initialiseAsGaussianIntGenerator(277, 07, 17, 17);
        ng.initialiseAsFixedIntGenerator(1);

        ng = showDialog((Frame)null, "Tester", "Tester...", ng);

        //dlg.pack();
        //dlg.show();

        System.out.println("Number Gen: "+ ng);
        

        for (int i = 0; i < 10; i++)
        {
            if (ng instanceof WeightGenerator)
            {
                WeightGenerator wg = (WeightGenerator)ng;
                System.out.println("Next float: " + wg.getNextNumber(i));
            }
            else
            {
                System.out.println("Next float: " + ng.getNextNumber());
                
            }
        }
    }


    public static NumberGenerator showDialog(Dialog dia,
                                             String title,
                                             String comment,
                                             NumberGenerator ngOld)
    {
        NumberGenerator ng = new NumberGenerator(ngOld.toShortString());
        NumberGeneratorDialog dlg = new NumberGeneratorDialog(dia, title, comment, ng);



    //Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dlg.getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }
        dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        dlg.setVisible(true);

        return ng;
    }

        public static NumberGenerator showDialog(Frame frame,
                                                 String title,
                                                 String comment,
                                                 NumberGenerator ngOld)
        {
            NumberGenerator ng = new NumberGenerator(ngOld.toShortString());

            NumberGeneratorDialog dlg = new NumberGeneratorDialog(frame, title, comment, ng);


            //Center the window
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = dlg.getSize();
            if (frameSize.height > screenSize.height)
            {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width)
            {
                frameSize.width = screenSize.width;
            }
            dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
            dlg.setVisible(true);

            return dlg.getFinalNumberGen();

        }

    void doCancel()
    {
        logger.logComment("Actions for cancel...");

        cancelled = true;

        this.dispose();
    }

    void doOK()
    {
        logger.logComment("OK pressed...");

        if (jRadioButtonFloat.isSelected())
        {
            if (jComboBoxType.getSelectedItem().equals(fixedNumString))
            {
                double val;
                try
                {
                    val = Double.parseDouble(fixedValTextField.getText());
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                        "Please type a correct floating decimal number (format: 1.23e-4) in the field for the fixed value of the number", ex, this);
                    return;
                }

                numGen.initialiseAsFixedFloatGenerator( (float) val);
            }
            else if (jComboBoxType.getSelectedItem().equals(randomNumString))
            {
                double minVal;
                double maxVal;
                try
                {
                    minVal = Double.parseDouble(minValTextField.getText());
                    maxVal = Double.parseDouble(maxValTextField.getText());
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                        "Please type correct floating decimal numbers (format: 1.23e-4) in the fields for the maximum and minimum values of the number",
                                              ex, this);
                    return;
                }

                if (maxVal <= minVal)
                {
                    GuiUtils.showErrorMessage(logger, "Please ensure the maximum is greater than the minimum", null, this);
                    return;
                }
                numGen.initialiseAsRandomFloatGenerator( (float) maxVal, (float) minVal);
            }
            else if (jComboBoxType.getSelectedItem().equals(gaussianNumString))
            {
                double minVal;
                double maxVal;
                double meanVal;
                double stdDevVal;
                try
                {
                    minVal = Double.parseDouble(minValTextField.getText());
                    maxVal = Double.parseDouble(maxValTextField.getText());
                    meanVal = Double.parseDouble(meanValTextField.getText());
                    stdDevVal = Double.parseDouble(stdDevValTextField.getText());
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                        "Please type correct floating decimal numbers in the fields for the mean, standard deviation, maximum and minimum values of the number",
                                              null, this);
                    return;
                }
                if (maxVal <= minVal)
                {
                    GuiUtils.showErrorMessage(logger, "Please ensure the maximum is greater than the minimum", null, this);
                    return;
                }
                if (stdDevVal < 0)
                {
                    GuiUtils.showErrorMessage(logger, "Please ensure the standard deviation is positive", null, this);
                    return;
                }

                numGen.initialiseAsGaussianFloatGenerator( (float) maxVal, (float) minVal, (float) meanVal,
                                                          (float) stdDevVal);
            }
            else if (jComboBoxType.getSelectedItem().equals(expressionString))
            {
                try {

                    WeightGenerator weiGen = null;
                    if (numGen instanceof WeightGenerator) {
                        weiGen = (WeightGenerator) numGen;
                    } else {

                        weiGen = new WeightGenerator("1", false);

                        numGen = weiGen;
                    }
                    String exp = jExpTexField.getText();


                    weiGen.initialiseAsFunction(exp, jChekBoxSomaToSoma.isSelected());
                    logger.logComment("New weight gen: " + weiGen);


                } catch (EquationException ex) {

                    ex.printStackTrace();
                }

            }
        }
        else
        {
            if (jComboBoxType.getSelectedItem().equals(fixedNumString))
            {
                int val;
                try
                {
                    val = Integer.parseInt(fixedValTextField.getText());
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Please type an integer in the field for the fixed value of the number",
                                              null, this);
                    return;
                }

                numGen.initialiseAsFixedIntGenerator(val);
            }
            else if (jComboBoxType.getSelectedItem().equals(randomNumString))
            {
                int minVal;
                int maxVal;
                try
                {
                    minVal = Integer.parseInt(minValTextField.getText());
                    maxVal = Integer.parseInt(maxValTextField.getText());
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Please type correct numbers in the fields for the maximum and minimum values of the number",
                                              null, this);
                    return;
                }

                if (maxVal <= minVal)
                {
                    GuiUtils.showErrorMessage(logger, "Please ensure the maximum is greater than the minimum", null, this);
                    return;
                }
                numGen.initialiseAsRandomIntGenerator( maxVal, minVal);
            }
            else if (jComboBoxType.getSelectedItem().equals(gaussianNumString))
            {
                int minVal;
                int maxVal;
                double meanVal;
                double stdDevVal;
                try
                {
                    minVal = Integer.parseInt(minValTextField.getText());
                    maxVal = Integer.parseInt(maxValTextField.getText());
                    meanVal = Double.parseDouble(meanValTextField.getText());
                    stdDevVal = Double.parseDouble(stdDevValTextField.getText());
                }
                catch (NumberFormatException ex)
                {
                    GuiUtils.showErrorMessage(logger,
                                              "Please type integers in the fields for the maximum and minimum values, and floating point decimals (format: 1.23e-4)\nfor the mean and standard deviation of the number",
                                              null, this);
                    return;
                }
                if (maxVal <= minVal)
                {
                    GuiUtils.showErrorMessage(logger, "Please ensure the maximum is greater than the minimum", null, this);
                    return;
                }
                if (stdDevVal < 0)
                {
                    GuiUtils.showErrorMessage(logger, "Please ensure the standard deviation is positive", null, this);
                    return;
                }

                numGen.initialiseAsGaussianIntGenerator(  maxVal, minVal, (float) meanVal,
                                                          (float) stdDevVal);
            }
            else if (jComboBoxType.getSelectedItem().equals(expressionString))
            {
                try {

                    WeightGenerator weiGen = null;
                    if (numGen instanceof WeightGenerator) {
                        weiGen = (WeightGenerator) numGen;
                    } else {

                        weiGen = new WeightGenerator("1", false);

                        numGen = weiGen;
                    }
                    String exp = jExpTexField.getText();


                    weiGen.initialiseAsFunction(exp, jChekBoxSomaToSoma.isSelected());
                    logger.logComment("New weight gen: " + weiGen);


                } catch (EquationException ex) {

                    ex.printStackTrace();
                }
        }
        }



        this.dispose();
    }

    void jButtonOK_actionPerformed(ActionEvent e) throws EquationException
    {
        doOK();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        doCancel();
    }

    public NumberGenerator getFinalNumberGen()
    {
        return numGen;
    }
    
    public WeightGenerator getFinalWeightGen()
    {
        return (WeightGenerator) numGen;
    }
    

    void jComboBoxType_itemStateChanged(ItemEvent e)
    {
        logger.logComment("Event: "+e);
        if (e.getStateChange() == ItemEvent.SELECTED)
        {

            String selected = (String)jComboBoxType.getSelectedItem();
            logger.logComment("Selected: "+ selected);

            jPanelVariables.removeAll();

            if (selected.equals(fixedNumString))
            {
                gridLayout1.setRows(1);
                jPanelVariables.add(fixedValLabel, null);
                jPanelVariables.add(fixedValTextField, null);

                if (numGen.numberType==NumberGenerator.INT_GENERATOR)
                   fixedValTextField.setText((int)numGen.getFixedNum()+"");
                else
                    fixedValTextField.setText(numGen.getFixedNum()+"");
            }
            else if (selected.equals(randomNumString))
            {
                gridLayout1.setRows(2);
                jPanelVariables.add(maxValLabel, null);
                jPanelVariables.add(maxValTextField, null);

                jPanelVariables.add(minValLabel, null);
                jPanelVariables.add(minValTextField, null);

                if (numGen.numberType==NumberGenerator.INT_GENERATOR)
                {
                    minValTextField.setText((int)numGen.getMin() + "");
                    maxValTextField.setText((int)numGen.getMax() + "");
                }
                else
                {
                    minValTextField.setText(numGen.getMin() + "");
                    maxValTextField.setText(numGen.getMax() + "");
                }

            }
            else if (selected.equals(gaussianNumString))
            {
                gridLayout1.setRows(4);


                jPanelVariables.add(meanValLabel, null);
                jPanelVariables.add(meanValTextField, null);
                meanValTextField.setText(numGen.getMean()+"");

                jPanelVariables.add(stdDevValLabel, null);
                jPanelVariables.add(stdDevValTextField, null);

                stdDevValTextField.setText(numGen.getStdDev()+"");


                jPanelVariables.add(maxValLabel, null);
                jPanelVariables.add(maxValTextField, null);

                jPanelVariables.add(minValLabel, null);
                jPanelVariables.add(minValTextField, null);

                if (numGen.numberType==NumberGenerator.INT_GENERATOR)
                {
                    minValTextField.setText((int)numGen.getMin() + "");
                    maxValTextField.setText((int)numGen.getMax() + "");
                }
                else
                {
                    minValTextField.setText(numGen.getMin() + "");
                    maxValTextField.setText(numGen.getMax() + "");
                }

                
            }
            else if (selected.equals(expressionString))
            {
                WeightGenerator weiGen = null;
                if (numGen instanceof WeightGenerator)
                    weiGen = (WeightGenerator) numGen;
                else
                {
                    try 
                    {
                        weiGen = new WeightGenerator("r", false);
                    } 
                    catch (EquationException ex) 
                    {
                        ex.printStackTrace();
                        return;
                    }
                }
                
                gridLayout1.setRows(3);
                jPanelVariables.add(jExpLabel, null);
                jPanelVariables.add(jExpTexField, null);
                jExpTexField.setText(weiGen.toShortString());
                jChekBoxSomaToSoma.setText("r = soma to soma distance");
                jPanelVariables.add(jChekBoxSomaToSoma);
                jChekBoxSomaToSoma.setSelected(weiGen.somaToSoma);
                
            }

                        
            this.pack();
        }
    }

    void jRadioButtonInt_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
            numGen.numberType = NumberGenerator.INT_GENERATOR;
    }

    void jRadioButtonFloat_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
            numGen.numberType = NumberGenerator.FLOAT_GENERATOR;
    }

}



