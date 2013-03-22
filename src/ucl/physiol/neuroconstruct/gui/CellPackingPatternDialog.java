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

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.project.packing.*;
import javax.swing.border.*;
import ucl.physiol.neuroconstruct.project.*;

/**
 * Dialog to specify Cell packing parameters
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class CellPackingPatternDialog extends JDialog
{
    ClassLogger logger = new ClassLogger("CellPackingPatternDialog");

    Hashtable<String, JTextField> paramInfo = new Hashtable<String, JTextField>();

    boolean cancelled = false;
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelCombo = new JPanel();
    JLabel jLabelMain = new JLabel();
    JComboBox jComboBoxPatterns = new JComboBox();

    JPanel jPanelDescription = new JPanel();
    JTextArea jTextAreaDescription = new JTextArea(8,8);
    JLabel jLabelParams = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanelParameters = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border1;
    GridLayout gridLayoutParameters = new GridLayout();


    public CellPackingPatternDialog(Dialog owner, CellPackingAdapter adapter)// throws HeadlessException
    {
        super(owner, "Choose the Cell packing pattern", true);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            extraInit(adapter);
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

    }


    public CellPackingPatternDialog(Frame owner, CellPackingAdapter adapter)// throws HeadlessException
    {
        super(owner, "Choose the Cell packing pattern", true);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            extraInit(adapter);
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }


    private void extraInit(CellPackingAdapter adapter)
    {
        logger.logComment("-----------------------------New CellPackingPatternDialog created with: "+adapter);

        String[] cellPatterns = CellPackingHelper.getAllCellPackingPatterns();

        for (int i = 0; i < cellPatterns.length; i++)
        {
            jComboBoxPatterns.addItem(cellPatterns[i]);
            logger.logComment("Added pattern: "+ cellPatterns[i]);
            if (adapter.getClass().getName().endsWith(cellPatterns[i]))
            {
                jComboBoxPatterns.setSelectedItem(cellPatterns[i]);
            }
        }

        if (adapter != null)
        {
            this.jTextAreaDescription.setText(adapter.getDescription());
            InternalParameter[] params =  adapter.getParameterList();

            for (int i = 0; i < params.length; i++)
            {
                String paramName = params[i].parameterName;
                double value = params[i].value;

                logger.logComment(i+": Looking at: "+paramName+", value: "+value);

                JTextField textField = (JTextField)paramInfo.get(paramName);
                textField.setText(value+"");
            }
        }
    }

    //Overridden so we can exit when window is closed
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

        CellPackingAdapter adapter = getFinalCellPackingAdapter();

        logger.logComment("Adapter as string: "+ adapter);

        if (adapter!=null) this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }

    public CellPackingAdapter getFinalCellPackingAdapter()
    {
        String selected = (String)jComboBoxPatterns.getSelectedItem();

        CellPackingAdapter adapter = null;

        try
        {
             adapter = CellPackingHelper.getCellPackingAdapter(selected);
        }
        catch (CellPackingException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error", ex, null);
            return null;
        }

        Enumeration paramNames = paramInfo.keys();

        while(paramNames.hasMoreElements())
        {
            String nextName = (String)paramNames.nextElement();

            JTextField textField = (JTextField)paramInfo.get(nextName);

            String valueTyped = textField.getText();
            if (!valueTyped.equals(""))
            {
                float value;
                try
                {
                    value = Float.parseFloat(valueTyped);
                }
                catch (NumberFormatException ex2)
                {
                    GuiUtils.showErrorMessage(logger, "Please enter a correct value for parameter: " + nextName, ex2, null);

                    return null;
                }

                logger.logComment("Setting parameter: " + nextName + " to: " + value);

                try
                {
                    adapter.setParameter(nextName, value);
                }
                catch (CellPackingException ex1)
                {
                    GuiUtils.showErrorMessage(logger, "Please enter a correct value for parameter: " + nextName, ex1, null);
                    return null;
                }
            }
        }

        return adapter;

    }


    public CellPackingPatternDialog()
    {
        try
        {
            jbInit();

            paramInfo = new Hashtable<String, JTextField>();
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
        jLabelMain.setText("Choose a Cell Packing Pattern:");

        jComboBoxPatterns.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxPatterns_itemStateChanged(e);
            }
        });
        jTextAreaDescription.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaDescription.setToolTipText("");
        jTextAreaDescription.setEditable(false);
        jTextAreaDescription.setColumns(26);
        jTextAreaDescription.setLineWrap(true);
        jTextAreaDescription.setRows(5);
        jTextAreaDescription.setTabSize(8);
        jTextAreaDescription.setWrapStyleWord(true);
        jLabelParams.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelParams.setText("Parameters:");
        jPanelDescription.setLayout(borderLayout2);
        jPanelMain.setLayout(borderLayout3);
        jPanelParameters.setBorder(BorderFactory.createEtchedBorder());
        jPanelParameters.setLayout(gridLayoutParameters);
        borderLayout2.setHgap(10);
        borderLayout2.setVgap(10);
        jPanelDescription.setBorder(border1);
        gridLayoutParameters.setColumns(1);
        gridLayoutParameters.setHgap(12);
        gridLayoutParameters.setVgap(12);
        this.getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        this.getContentPane().add(jPanelCombo, BorderLayout.NORTH);
        jPanelCombo.add(jLabelMain, null);
        jPanelCombo.add(jComboBoxPatterns, null);
        ////jPanelMain.add(jScrollPaneDescription, null);
        jPanelMain.add(jPanelParameters,  BorderLayout.CENTER);
        jPanelMain.add(jPanelDescription,  BorderLayout.NORTH);
        jPanelDescription.add(jLabelParams,  BorderLayout.SOUTH);
        jPanelDescription.add(jTextAreaDescription, BorderLayout.CENTER);

        ////JViewport vp = jScrollPaneDescription.getViewport();

       //// vp.add(jTextAreaDescription);
    }

    void jComboBoxPatterns_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()!=ItemEvent.SELECTED) return;

        String selected = (String)jComboBoxPatterns.getSelectedItem();

        CellPackingAdapter adapter = null;

        try
        {
             adapter = CellPackingHelper.getCellPackingAdapter(selected);
        }
        catch (CellPackingException ex)
        {
            logger.logError("Error", ex);
            return;
        }

        jTextAreaDescription.setText(adapter.getDescription());

        InternalParameter[] paramList = adapter.getParameterList();

        paramInfo = new Hashtable<String, JTextField>(); // to remove previous items
        jPanelParameters.removeAll();

        for (int i = 0; i < paramList.length; i++)
        {
            addPatternParameter(paramList[i]);
        }
        gridLayoutParameters.setColumns(1);
        gridLayoutParameters.setRows(paramList.length);
        this.repaint();
        this.pack();

    }

    private void addPatternParameter(InternalParameter param)
    {
        JPanel jPanelNew = new JPanel();
        //BorderLayout borderLayoutNew = new BorderLayout();
        //jPanelNew.setLayout(borderLayoutNew);

        JLabel jLabelName = new JLabel(param.parameterName+": ");
        jLabelName.setPreferredSize(new Dimension(160, 20));
        jLabelName.setMinimumSize(new Dimension(160, 20));
        jPanelNew.add(jLabelName, "West");

        //JLabel jLabelDesc = new JLabel(param.parameterDescription);
        JTextArea jTextAreaShortDescription = new JTextArea(4,32);
        jTextAreaShortDescription.setLineWrap(true);
        jTextAreaShortDescription.setWrapStyleWord(true);
        jTextAreaShortDescription.setText(param.parameterDescription);
        jTextAreaShortDescription.setEnabled(false);
        //jLabelDesc.setPreferredSize(new Dimension(400, 20));
        //jLabelDesc.setMinimumSize(new Dimension(400, 20));
        jPanelNew.add(jTextAreaShortDescription, "Center");

        jTextAreaShortDescription.setBorder(BorderFactory.createEtchedBorder());


        JTextField jTextFieldNew = new JTextField();

        jTextFieldNew.setText(""+param.defaultValue);

        paramInfo.put(param.parameterName, jTextFieldNew);

        jTextFieldNew.setColumns(7);
        //jTextFieldNew.setColumns(7);
        jPanelNew.add(jTextFieldNew, "East");

        jPanelParameters.add(jPanelNew);

    }


}
