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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Dialog to specify Cell Chooser parameters
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class CellChooserDialog extends JDialog
{
    private static ClassLogger logger = new ClassLogger("CellChooserDialog");

    Hashtable<String, JTextField> paramInfo = new Hashtable<String, JTextField>();

    boolean cancelled = false;

    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelCombo = new JPanel();
    JLabel jLabelMain = new JLabel();
    JComboBox jComboBoxCellChoosers = new JComboBox();

    JPanel jPanelDescription = new JPanel();
    JTextArea jTextAreaDescription = new JTextArea(8,8);
    JLabel jLabelParams = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanelParameters = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border1;
    GridLayout gridLayoutParameters = new GridLayout();


    public CellChooserDialog(Dialog owner, String title, CellChooser adapter)// throws HeadlessException
    {
        super(owner, title, true);

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

    public CellChooserDialog(Frame owner, String title, CellChooser adapter)// throws HeadlessException
    {
        super(owner, title, true);

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




    private void extraInit(CellChooser cellChooser)
    {
        logger.logComment("-----------------------------New CellChooserDialog created with: "+cellChooser);

        String[] cellChoosers = CellChooserHelper.getAllCellChoosers();

        for (int i = 0; i < cellChoosers.length; i++)
        {
            jComboBoxCellChoosers.addItem(cellChoosers[i]);
            logger.logComment("Added chooser: "+ cellChoosers[i]);
            if (cellChooser.getClass().getName().endsWith(cellChoosers[i]))
            {
                jComboBoxCellChoosers.setSelectedItem(cellChoosers[i]);
            }
        }

        if (cellChooser != null)
        {
            this.jTextAreaDescription.setText(cellChooser.getDescription());
            InternalStringFloatParameter[] params =  cellChooser.getParameterList();

            for (int i = 0; i < params.length; i++)
            {
                String paramName = params[i].parameterName;
                String stringVal = params[i].getStringValue();
                if (stringVal==null) stringVal = params[i].getValue()+"";

                logger.logComment(i+": Looking at: "+paramName+", value: "+stringVal);

                JTextField textField = paramInfo.get(paramName);
                textField.setText(stringVal);
            }
        }
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

        CellChooser chooser = getFinalCellChooser();

        logger.logComment("chooser as string: "+ chooser);

        if (chooser!=null) this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }

    public CellChooser getFinalCellChooser()
    {
        String selected = (String)jComboBoxCellChoosers.getSelectedItem();

        CellChooser chooser = null;

        try
        {
             chooser = CellChooserHelper.getCellChooser(selected);
        }
        catch (CellChooserException ex)
        {
            GuiUtils.showErrorMessage(logger, "Error", ex, null);
            return null;
        }

        Enumeration paramNames = paramInfo.keys();

        while(paramNames.hasMoreElements())
        {
            String nextName = (String)paramNames.nextElement();

            JTextField textField = paramInfo.get(nextName);

            String valueTyped = textField.getText();

            if (!valueTyped.equals(""))
            {
                if (chooser.getParameterStringValue(nextName)!=null)
                {
                    logger.logComment("Parameter supposed to be a string...");
                    try
                    {
                        chooser.setParameter(nextName, valueTyped);
                    }
                    catch (CellChooserException ex1)
                    {
                        GuiUtils.showErrorMessage(logger, "Error setting parameter: " + nextName+": "+ex1.getMessage(),
                                                  ex1, null);
                        return null;
                    }

                }
                else
                {
                    float value;
                    try
                    {
                        value = Float.parseFloat(valueTyped);
                    }
                    catch (NumberFormatException ex2)
                    {
                        GuiUtils.showErrorMessage(logger, "Please enter a correct float value for parameter: " + nextName,
                                                  ex2, null);

                        return null;
                    }

                    logger.logComment("Setting parameter: " + nextName + " to: " + value);

                    try
                    {
                        chooser.setParameter(nextName, value);
                    }
                    catch (CellChooserException ex1)
                    {
                        GuiUtils.showErrorMessage(logger, "Error setting parameter: " + nextName+": "+ex1.getMessage(),
                                                  ex1, null);
                        return null;
                    }
                }

            }
        }

        return chooser;

    }


    public CellChooserDialog()
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
        jLabelMain.setText("Choose a Cell Chooser:");

        jComboBoxCellChoosers.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxChoosers_itemStateChanged(e);
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
        jPanelCombo.add(jComboBoxCellChoosers, null);
        ////jPanelMain.add(jScrollPaneDescription, null);
        jPanelMain.add(jPanelParameters,  BorderLayout.CENTER);
        jPanelMain.add(jPanelDescription,  BorderLayout.NORTH);
        jPanelDescription.add(jLabelParams,  BorderLayout.SOUTH);
        jPanelDescription.add(jTextAreaDescription, BorderLayout.CENTER);

        ////JViewport vp = jScrollPaneDescription.getViewport();

       //// vp.add(jTextAreaDescription);
    }

    void jComboBoxChoosers_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()!=ItemEvent.SELECTED) return;

        String selected = (String)jComboBoxCellChoosers.getSelectedItem();

        CellChooser chooser = null;

        try
        {
             chooser = CellChooserHelper.getCellChooser(selected);
        }
        catch (CellChooserException ex)
        {
            logger.logError("Error", ex);
            return;
        }

        jTextAreaDescription.setText(chooser.getDescription());

        InternalStringFloatParameter[] paramList = chooser.getParameterList();

        paramInfo = new Hashtable<String, JTextField>(); // to remove previous items
        jPanelParameters.removeAll();

        for (int i = 0; i < paramList.length; i++)
        {
            addParameter(paramList[i]);
        }
        gridLayoutParameters.setColumns(1);
        gridLayoutParameters.setRows(paramList.length);
        this.repaint();
        this.pack();

    }

    private void addParameter(InternalStringFloatParameter param)
    {
        JPanel jPanelNew = new JPanel();

        JLabel jLabelName = new JLabel(param.parameterName+": ");
        jLabelName.setPreferredSize(new Dimension(120, 20));
        jLabelName.setMinimumSize(new Dimension(120, 20));
        jPanelNew.add(jLabelName, "West");

        JTextArea jTextAreaShortDescription = new JTextArea(4,32);
        jTextAreaShortDescription.setLineWrap(true);
        jTextAreaShortDescription.setWrapStyleWord(true);
        jTextAreaShortDescription.setText(param.parameterDescription);
        jTextAreaShortDescription.setEnabled(false);

        jPanelNew.add(jTextAreaShortDescription, "Center");

        jTextAreaShortDescription.setBorder(BorderFactory.createEtchedBorder());


        JTextField jTextFieldNew = new JTextField();

        if (param.getStringValue()!=null)
        {
            jTextFieldNew.setText(param.getStringValue());
        }
        else
        {
            jTextFieldNew.setText("" + param.defaultValue);
        }

        paramInfo.put(param.parameterName, jTextFieldNew);

        jTextFieldNew.setColumns(7);
        jPanelNew.add(jTextFieldNew, "East");

        jPanelParameters.add(jPanelNew);

    }


    public static void main(String[] args)
    {
        String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
        try
        {
            UIManager.setLookAndFeel(favouredLookAndFeel);

            FixedNumberCells fixedNumCells = new FixedNumberCells();

            fixedNumCells.setParameter(FixedNumberCells.MAX_NUM_CELLS, 8);

            CellChooserDialog dlg = new CellChooserDialog( (Frame)null, "jdghkjgk",fixedNumCells);

            dlg.setModal(true);
            dlg.setVisible(true);

            CellChooser cc = dlg.getFinalCellChooser();

            logger.logComment("Choosen one: " + cc.toString());

            ArrayList<PositionRecord> cellPositions = new ArrayList<PositionRecord>();
            cellPositions.add(new PositionRecord(0, 0, 0, 0));
            cellPositions.add(new PositionRecord(1, 110, 0, 0));
            cellPositions.add(new PositionRecord(2, 220, 0, 0));
            cellPositions.add(new PositionRecord(6, 660, 0, 0));
            cellPositions.add(new PositionRecord(7, 770, 0, 0));


            cc.initialise(cellPositions);

            while (true)
            {
                logger.logComment("Next cell index found: " + cc.getNextCellIndex());
            }

        }
        catch (AllCellsChosenException ex)
        {
            logger.logComment("Found all: " + ex.getMessage());
        }

        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
