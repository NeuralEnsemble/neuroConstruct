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
import javax.swing.border.*;
import ucl.physiol.neuroconstruct.project.*;
import javax.swing.event.*;

/**
 * Dialog to specify Region parameters
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class RegionsInfoDialog extends JDialog implements DocumentListener
{
    ClassLogger logger = new ClassLogger("RegionsInfoDialog");

    Hashtable<String, JTextField> textFieldsForParameters = new Hashtable<String, JTextField>();

    boolean cancelled = false;
    JPanel jPanelButtons = new JPanel();
    JPanel jPanelMain = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButtonOK = new JButton();
    JButton jButtonCancel = new JButton();
    JPanel jPanelTop = new JPanel();
    JLabel jLabelMain = new JLabel();
    JComboBox jComboBoxRegions = new JComboBox();

    JPanel jPanelDescription = new JPanel();
    JTextArea jTextAreaDescription = new JTextArea(8,8);
    JLabel jLabelParams = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel jPanelParameters = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    Border border1;
    GridLayout gridLayoutParameters = new GridLayout();
    JPanel jPanelNames = new JPanel();
    JLabel jLabel1 = new JLabel();
    JTextField jTextFieldName = new JTextField();
    JPanel jPanelComboStuff = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();


    public RegionsInfoDialog(Dialog owner, Region region, String suggestedName)
    {
        super(owner, "Choose the 3D Region Type", true);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            extraInit(region);
            jTextFieldName.setText(suggestedName);
            pack();
        }
        catch(Exception ex)
        {
            logger.logComment("Exception starting GUI: "+ ex);
        }

    }


    public RegionsInfoDialog(Frame owner, Region region, String suggestedName)
    {
        super(owner, "Choose the 3D Region Type", true);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        try
        {
            jbInit();
            extraInit(region);
            jTextFieldName.setText(suggestedName);
            pack();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }


    private void extraInit(Region region)
    {
        logger.logComment("-----------------------------New RegionsInfoDialog created with: "+region);

        String[] regionDescs = RegionTypeHelper.getRegionDescriptions();

        for (int i = 0; i < regionDescs.length; i++)
        {
            jComboBoxRegions.addItem(regionDescs[i]);
            logger.logComment("Added pattern: "+ regionDescs[i]);

            if (region.getDescription().equals(regionDescs[i]))
            {
                jComboBoxRegions.setSelectedItem(regionDescs[i]);
            }
        }

        if (region != null)
        {

            this.jTextAreaDescription.setText(region.toString());
            InternalParameter[] params =  region.getParameterList();

            for (int i = 0; i < params.length; i++)
            {
                String paramName = params[i].parameterName;
                double value = params[i].value;

                logger.logComment(i+": Looking at: "+paramName+", value: "+value);

                JTextField textField = (JTextField)textFieldsForParameters.get(paramName);
                textField.setText(value+"");
            }
        }
    }


    public void insertUpdate(DocumentEvent e)
    {
        logger.logComment("DocumentEvent: " + e);
        this.jTextAreaDescription.setText(getFinalRegion().toString());
    };

    public void removeUpdate(DocumentEvent e)
    {
        logger.logComment("DocumentEvent: " + e);
    };


    public void changedUpdate(DocumentEvent e)
    {
        logger.logComment("DocumentEvent: " + e);
        this.jTextAreaDescription.setText(getFinalRegion().toString());
    };



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

        Region region = getFinalRegion();

        logger.logComment("region as string: "+ region);

        if (region!=null) this.dispose();
    }

    void jButtonCancel_actionPerformed(ActionEvent e)
    {
        logger.logComment("Cancel pressed...");
        doCancel();
        this.dispose();

    }

    public String getRegionName()
    {
        return jTextFieldName.getText();
    }

    public Region getFinalRegion()
    {
        String selected = (String)jComboBoxRegions.getSelectedItem();

        Region regionToReturn = RegionTypeHelper.getRegionInstance(selected);

        if (regionToReturn==null)
        {
            logger.logError("problem getting region for: "+ selected);
            return null;
        }

        Enumeration paramNames = textFieldsForParameters.keys();

        while(paramNames.hasMoreElements())
        {
            String nextName = (String)paramNames.nextElement();

            JTextField textField = (JTextField)textFieldsForParameters.get(nextName);

            String valueTyped = textField.getText();
            if (!valueTyped.equals("") && !valueTyped.equals("-"))
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
                regionToReturn.setParameter(nextName, value);
            }
        }

        return regionToReturn;

    }


    public RegionsInfoDialog()
    {
        try
        {
            jbInit();

            textFieldsForParameters = new Hashtable<String, JTextField>();
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
        jLabelMain.setText("Choose a 3D Region");

        jComboBoxRegions.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                jComboBoxRegions_itemStateChanged(e);
            }
        });
        jTextAreaDescription.setBorder(BorderFactory.createLoweredBevelBorder());
        jTextAreaDescription.setToolTipText("");
        jTextAreaDescription.setEditable(false);
        jTextAreaDescription.setColumns(26);
        jTextAreaDescription.setLineWrap(true);
        jTextAreaDescription.setRows(2);
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
        jLabel1.setText("Name of new Region");
        jTextFieldName.setText("...");
        jTextFieldName.setColumns(12);
        jPanelTop.setLayout(borderLayout4);
        jPanelComboStuff.add(jLabelMain, null);
        this.getContentPane().add(jPanelButtons, BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOK, null);
        jPanelButtons.add(jButtonCancel, null);
        this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
        this.getContentPane().add(jPanelTop, BorderLayout.NORTH);
        jPanelTop.add(jPanelComboStuff, BorderLayout.CENTER);
        jPanelComboStuff.add(jComboBoxRegions, null);
        jPanelTop.add(jPanelNames,  BorderLayout.NORTH);
        jPanelNames.add(jLabel1, null);
        jPanelNames.add(jTextFieldName, null);
        ////jPanelMain.add(jScrollPaneDescription, null);
        jPanelMain.add(jPanelParameters,  BorderLayout.CENTER);
        jPanelMain.add(jPanelDescription,  BorderLayout.NORTH);
        jPanelDescription.add(jLabelParams,  BorderLayout.SOUTH);
        jPanelDescription.add(jTextAreaDescription, BorderLayout.CENTER);

        ////JViewport vp = jScrollPaneDescription.getViewport();

       //// vp.add(jTextAreaDescription);
    }

    void jComboBoxRegions_itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange()!=ItemEvent.SELECTED) return;

        String selected = (String)jComboBoxRegions.getSelectedItem();

        Region region = null;

        region = RegionTypeHelper.getRegionInstance(selected);

        jTextAreaDescription.setText(region.toString());

        InternalParameter[] paramList = region.getParameterList();

        textFieldsForParameters = new Hashtable<String, JTextField>(); // to remove previous items
        jPanelParameters.removeAll();

        logger.logComment("Adding "+
                          paramList.length
                          + " params for region: "
                          + region);

        for (int i = 0; i < paramList.length; i++)
        {
            addRegionParameter(paramList[i]);
        }
        gridLayoutParameters.setColumns(1);
        gridLayoutParameters.setRows(paramList.length);

        this.repaint();
        this.pack();

    }

    private void addRegionParameter(InternalParameter param)
    {
        JPanel jPanelNew = new JPanel();
        JLabel jLabelName = new JLabel(param.parameterName+": ");
        jLabelName.setPreferredSize(new Dimension(100, 20));
        jLabelName.setMinimumSize(new Dimension(100, 20));
        jPanelNew.add(jLabelName, "West");

        //JLabel jLabelDesc = new JLabel(param.parameterDescription);
        JTextArea jTextAreaShortDescription = new JTextArea(2,26);
        jTextAreaShortDescription.setLineWrap(true);
        jTextAreaShortDescription.setWrapStyleWord(true);
        jTextAreaShortDescription.setText(param.parameterDescription);
        jTextAreaShortDescription.setEnabled(false);
        jPanelNew.add(jTextAreaShortDescription, "Center");

        jTextAreaShortDescription.setBorder(BorderFactory.createEtchedBorder());


        JTextField jTextFieldNew = new JTextField();

        jTextFieldNew.setText(""+param.defaultValue);

        textFieldsForParameters.put(param.parameterName, jTextFieldNew);

        jTextFieldNew.setColumns(5);
        jPanelNew.add(jTextFieldNew, "East");

        jTextFieldNew.getDocument().addDocumentListener(this);

        jPanelParameters.add(jPanelNew);

    }

    public static void main(String[] args)
    {
        //RectangularBox rect = activeProject.regionsInfo.getRegionEnclosingAllRegions();

        Region suggestedRegion = new RectangularBox(1,1,1,7,7,7);

        String suggestedName = "Regions_0";

        RegionsInfoDialog dlg = new RegionsInfoDialog(new Frame(),
                                                      suggestedRegion,
                                                      suggestedName);
        //Dimension dlgSize = dlg.getPreferredSize();
        //Dimension frmSize = getSize();
        //Point loc = getLocation();
       // dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
       //                 (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);

    }


}
