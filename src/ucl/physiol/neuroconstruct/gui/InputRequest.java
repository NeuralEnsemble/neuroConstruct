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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Used to build generic input requests
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class InputRequest extends JDialog
{
    //ArrayList<InputRequestElement> inputs = null;

    Hashtable<JTextField, InputRequestElement> textFieldsVsInputs = new Hashtable<JTextField,InputRequestElement>();


    boolean cancelled =false;

    String request = null;
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelButtons = new JPanel();
    JButton jButtonOk = new JButton();
    JButton jButtonCancel = new JButton();

    public InputRequest(JFrame owner,
                        String request,
                        String title,
                        ArrayList<InputRequestElement> inputs,
                        boolean modal)
    {
        super(owner, title, modal);

        this.request = request;

        try
        {
            //this.inputs = inputs;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            jbInit();
            addInputs(inputs);
            pack();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void addInputs(ArrayList<InputRequestElement> inputs)
    {
        jPanelMain.setLayout(new GridLayout(inputs.size(), 1));

        for (InputRequestElement input: inputs)
        {
            JPanel jPanelNew = new JPanel();
            jPanelNew.setLayout(new BorderLayout());
            JLabel lab = new JLabel(input.getRequest());
            if (input.getUnits()!=null && input.getUnits().length()>0)
            {
                lab = new JLabel(input.getRequest()+" ("+input.getUnits()+")");
            }
            //lab.adde
            JTextField text = new JTextField();

            text.setColumns(12);

            JPanel jPanelText = new JPanel();
            jPanelText.setPreferredSize(new Dimension(150, 28));
            jPanelText.add(text);

            textFieldsVsInputs.put(text, input);

            if (input.getValue()!=null)
            {
                text.setText(input.getValue());
            }
            if (input.getToolTip()!=null)
            {
                text.setToolTipText(input.getToolTip());
                lab.setToolTipText(input.getToolTip());
            }

            jPanelNew.add(lab, java.awt.BorderLayout.CENTER);
            jPanelNew.add(jPanelText, java.awt.BorderLayout.EAST);

            jPanelNew.setBorder(BorderFactory.createEmptyBorder(5,12,5,12));

            jPanelMain.add(jPanelNew);
        }
    }



    private void jbInit() throws Exception
    {
        panel1.setLayout(borderLayout1);
        //jPanelButtons.setBorder(BorderFactory.createLoweredBevelBorder());
        jButtonOk.setText("OK"); jButtonOk.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonOk_actionPerformed(e);
            }
        });
        jButtonCancel.setText("Cancel"); jButtonCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonCancel_actionPerformed(e);
            }
        });
        getContentPane().add(panel1);

        if (this.request !=null)
        {
            JPanel mainRequest = new JPanel();
            mainRequest.add(new JLabel(request));
            panel1.add(mainRequest, java.awt.BorderLayout.NORTH);
        }

        panel1.add(jPanelMain, java.awt.BorderLayout.CENTER);
        panel1.add(jPanelButtons,
                   java.awt.BorderLayout.SOUTH);
        jPanelButtons.add(jButtonOk); jPanelButtons.add(jButtonCancel);
    }

    public static void main(String[] args)
    {
        ArrayList<InputRequestElement> inputs = new ArrayList<InputRequestElement>();
        inputs.add(new InputRequestElement("e1", "Name", "Enter yer name", "Mick", ""));
        inputs.add(new InputRequestElement("e2", "Age", "Enter yer age", "98", "years"));
        inputs.add(new InputRequestElement("e3", "Height", null, "123", "feet"));

        InputRequest dlg = new InputRequest( (JFrame)null, "ppp","hhh", inputs, true);

        dlg.setModal(true);
        dlg.setVisible(true);

        for (InputRequestElement input: inputs)
        {
            System.out.println("Val: " + input.getValue());
        }

    }

    public void jButtonOk_actionPerformed(ActionEvent e)
    {
        Enumeration<JTextField> textFields = this.textFieldsVsInputs.keys();
        while(textFields.hasMoreElements())
        {
            JTextField textField = textFields.nextElement();
            InputRequestElement input = textFieldsVsInputs.get(textField);
            input.setValue(textField.getText());
        }
        this.dispose();
    }

    public boolean cancelled()
    {
        return cancelled;
    }

    public void jButtonCancel_actionPerformed(ActionEvent e)
    {
        cancelled = true;
        this.dispose();
    }
}
