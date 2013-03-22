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
import javax.swing.*;
import ucl.physiol.neuroconstruct.utils.*;
import javax.swing.border.*;
import java.awt.event.*;

/**
 * Dialog for specifying parameters for sequence generator
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SequenceGeneratorDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("SequenceGeneratorDialog");

    String fixedNumString = "Fixed number";
    String linearSeqString = "Linear Sequence";

    boolean cancelled = false;

    JLabel fixedValLabel = new JLabel("Value: ");
    JTextField fixedValTextField = new JTextField("1");

    JLabel startValLabel = new JLabel("Start value of sequence: ");
    JTextField startValTextField = new JTextField("1");
    JLabel endValLabel = new JLabel("End value of sequence: ");
    JTextField endValTextField = new JTextField("10");


    JLabel intervalValLabel = new JLabel("Interval: ");
    JTextField intervalValTextField = new JTextField("1");



    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();

    SequenceGenerator seqGen = null;

    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonCancel = new JButton();
    JButton jButtonOK = new JButton();
    JLabel jLabelType = new JLabel();

    JLabel jLabelRequest = new JLabel("Please enter the value below");



    JComboBox jComboBoxType = new JComboBox();
    JPanel jPanelVariables = new JPanel();
    GridLayout gridLayout1 = new GridLayout();
    Border border1;
    JPanel jPanel1 = new JPanel();
    //JRadioButton jRadioButtonFloat = new JRadioButton();
    //JRadioButton jRadioButtonInt = new JRadioButton();
    //ButtonGroup buttonGroupNumType = new ButtonGroup();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanelType = new JPanel();
    JPanel jPanelTop = new JPanel();

    public SequenceGeneratorDialog(Frame frame, SequenceGenerator seqGenOriginal)
    {
        super(frame, "Sequence properties", true);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        logger.logComment("SequenceGeneratorDialog created with: "+ seqGenOriginal);

        if (seqGenOriginal == null)
            seqGen = new SequenceGenerator(1);
        else
            seqGen = seqGenOriginal;
        try
        {
            jbInit();
            extraInit();
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public SequenceGeneratorDialog(Dialog dia, SequenceGenerator seqGenOriginal)
    {
        super(dia, "Sequence properties", true);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        logger.logComment("NumberGeneratorDialog created with: "+ seqGenOriginal);
        if (seqGenOriginal == null)
            seqGen = new SequenceGenerator(1);
        else
            seqGen = seqGenOriginal;
        try
        {
            jbInit();
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
                jButtonOK_actionPerformed(e);
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
        /*
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
        });*/
        jPanelMain.setLayout(borderLayout2);
        getContentPane().add(panel1);
        panel1.add(jPanelMain, BorderLayout.NORTH);
        panel1.add(jPanelButtons,  BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        jPanelMain.add(jPanel1,  BorderLayout.CENTER);
        panel1.add(jPanelVariables,  BorderLayout.CENTER);

        jPanelTop.add(jLabelRequest, null);

        //jPanelType.add(jLabelRequest, null);
        jPanelType.add(jLabelType, null);
        jPanelType.add(jComboBoxType, null);
        jPanelTop.add(jPanelType, null);

        Dimension dim = new Dimension(300, 70);
        jPanelTop.setMinimumSize(dim);
        jPanelTop.setPreferredSize(dim);

        jPanelMain.add(jPanelTop, BorderLayout.NORTH);

        /*
        buttonGroupNumType.add(jRadioButtonFloat);
        buttonGroupNumType.add(jRadioButtonInt);
        jPanel1.add(jRadioButtonInt, null);
        jPanel1.add(jRadioButtonFloat, null);*/

    }


    private void extraInit()
    {
        jComboBoxType.addItem(fixedNumString);
        jComboBoxType.addItem(linearSeqString);
        //jComboBoxType.addItem(gaussianNumString);

        if (seqGen.getNumInSequence()==1) jComboBoxType.setSelectedItem(fixedNumString);
        else  jComboBoxType.setSelectedItem(linearSeqString);
        //else if (numGen.distributionType==NumberGenerator.GAUSSIAN_NUM) jComboBoxType.setSelectedItem(gaussianNumString);

        //if (numGen.numberType==NumberGenerator.INT_GENERATOR) jRadioButtonInt.setSelected(true);
        //else jRadioButtonFloat.setSelected(true);

    }

    protected void processWindowEvent(WindowEvent e)
    {
        //super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            doCancel();
        }
        //super.processWindowEvent(e);
    }


    public static void main(String[] args)
    {
        SequenceGenerator sg3 = new SequenceGenerator(1,6,5f);


        sg3 = showDialog((Frame)null, "This is just a simple Tester dialog example", sg3);

        //dlg.pack();
        //dlg.show();

        System.out.println("Seq Gen: "+ sg3);

        try
        {
            while (sg3.hasMoreNumbers())
            {
                System.out.println("Next number: " + sg3.getNumber());
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void setTitle(String title)
    {
        super.setTitle(title);
        this.jLabelRequest.setText(title);
    }


    public static SequenceGenerator showDialog(Dialog dia,
                                             String title,
                                             SequenceGenerator seqGenOld)
    {


        SequenceGeneratorDialog dlg = new SequenceGeneratorDialog(dia, seqGenOld);

        dlg.setTitle(title);

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

        return dlg.getSeqGen();
    }

        public static SequenceGenerator showDialog(Frame frame,
                                                 String title,
                                                 SequenceGenerator seqGenOld)
        {
            SequenceGeneratorDialog dlg = new SequenceGeneratorDialog(frame, seqGenOld);


            dlg.setTitle(title);

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

            return dlg.getSeqGen();


        }

    void doCancel()
    {
        logger.logComment("Actions for cancel...");

        cancelled = true;

        seqGen = null;

        this.dispose();
    }

    public boolean wasCancelled()
    {
        return this.cancelled;
    }

    public SequenceGenerator getSeqGen()
    {
        return seqGen;
    }

    void doOK()
    {
        logger.logComment("OK pressed...");

        if (jComboBoxType.getSelectedItem().equals(fixedNumString))
        {
            float val;
            try
            {
                val = Float.parseFloat(fixedValTextField.getText());
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please type a correct floating decimal number (format: 1.23e-4) in the field for the fixed value of the number",
                                          ex, this);
                return;
            }
            this.seqGen = new SequenceGenerator((float) val);

        }
        else if (jComboBoxType.getSelectedItem().equals(linearSeqString))
        {
            float endVal;
            float startVal;
            float intervalVal;
            try
            {
                endVal = Float.parseFloat(endValTextField.getText());
                startVal = Float.parseFloat(startValTextField.getText());
                intervalVal = Float.parseFloat(intervalValTextField.getText());
            }
            catch (NumberFormatException ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Please type correct floating decimal numbers (format: 1.23e-4) in the fields for the start, end and interval values of the sequence",
                                          ex, this);
                return;
            }
            this.seqGen = new SequenceGenerator((float) startVal, (float) endVal, (float) intervalVal);
        }


        this.dispose();
    }

    void jButtonOK_actionPerformed(ActionEvent e)
    {
        doOK();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        doCancel();
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

                    fixedValTextField.setText(this.seqGen.getStart()+"");
            }
            else if (selected.equals(linearSeqString))
            {
                gridLayout1.setRows(3);
                jPanelVariables.add(startValLabel, null);
                jPanelVariables.add(startValTextField, null);

                jPanelVariables.add(endValLabel, null);
                jPanelVariables.add(endValTextField, null);

                jPanelVariables.add(intervalValLabel, null);
                jPanelVariables.add(intervalValTextField, null);

                startValTextField.setText(this.seqGen.getStart() + "");
                endValTextField.setText(this.seqGen.getEnd() + "");
                intervalValTextField.setText(this.seqGen.getInterval() + "");



            }

            this.pack();
        }
    }


}



