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
import javax.swing.table.*;
import ucl.physiol.neuroconstruct.utils.*;
import java.util.*;


/**
 * Implementation of AbstractCellEditor for drop down lost of generic objects.
 * Object should have good toString implemented
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class DropDownObjectCellEditor extends AbstractCellEditor implements TableCellEditor
{
    ClassLogger logger = new ClassLogger("DropDownObjectCellEditor");

    JComboBox comboBox;

    Vector<Object> allObjects = new Vector<Object>();

    public DropDownObjectCellEditor()
    {
        logger.logComment("New DropDownObjectCellEditor created...");

        comboBox = new JComboBox();
        comboBox.setActionCommand("Choose");
    }


    public void addValue(Object obj)
    {
        logger.logComment("addValue called, object type: "+ obj.getClass().getName()+", value: "+ obj);

        comboBox.addItem(obj);
        allObjects.add(obj);
    }


    public Object getCellEditorValue()
    {
        logger.logComment("getCellEditorValue called");
        int index = comboBox.getSelectedIndex();
        return allObjects.elementAt(index);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        logger.logComment("getTableCellEditorComponent called, object type: "+ value.getClass().getName()+", value: "+ value);

        comboBox.setSelectedItem(value);

        return comboBox;
    }


}
