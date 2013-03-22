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
import java.awt.event.*;
import javax.swing.table.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Implementation of AbstractCellEditor for cell colour column
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class SynapticPropertiesEditor extends AbstractCellEditor 
                    implements TableCellEditor, ActionListener
{
    public SynapticPropertiesEditor()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    ClassLogger logger = new ClassLogger("SynapticPropertiesEditor");

    SynapticProperties currentSynapticProperties;
    JButton button;

    Project project = null;
    Frame myParent = null;

    public SynapticPropertiesEditor(Frame parent, Project project)
    {
        this.myParent = parent;

        this.project = project;
        button = new JButton();
        button.setActionCommand("Edit");
        button.addActionListener(this);
        button.setBorderPainted(false);
/*
        colourChooser = new JColorChooser();
        dialog = JColorChooser.createDialog(button,
                                            "Pick a colour for this Cell Group",
                                            true,
                                            colourChooser,
                                            this,
                                            null);
        }
   */     }


    public void actionPerformed(ActionEvent e)
    {
        if ("Edit".equals(e.getActionCommand()))
        {
            logger.logComment("Edit pressed...");
            SynapticPropertiesDialog dlg
                = new SynapticPropertiesDialog(myParent,
                                               currentSynapticProperties,
                                               project);

            Dimension dlgSize = dlg.getPreferredSize();
            Dimension frmSize = myParent.getSize();
            Point loc = myParent.getLocation();
            dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                            (frmSize.height - dlgSize.height) / 2 + loc.y);
            dlg.setModal(true);

            dlg.pack();
            dlg.setVisible(true);


            fireEditingStopped();

        }
        else
        {
            logger.logComment("Other event: "+ e);
        }
    }

    public Object getCellEditorValue()
    {
        return currentSynapticProperties;
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        currentSynapticProperties = (SynapticProperties)value;
        return button;
    }

    private void jbInit() throws Exception
    {
    }
}
