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
 * Implementation of AbstractCellEditor for generic list of int->String combos
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class DropDownCellValuesEditor extends AbstractCellEditor implements TableCellEditor
{
    ClassLogger logger = new ClassLogger("DropDownCellValuesEditor");

    JComboBox comboBox;

    /** @todo do better? */
    Vector<Integer> allIntegers = new Vector<Integer>();
    Vector<String> allStrings = new Vector<String>();

    public DropDownCellValuesEditor()
    {
        logger.logComment("New DropDownCellValuesEditor created...");

        comboBox = new JComboBox();
        comboBox.setActionCommand("Choose");
    }


    public void addValue(int intValue, String stringAlternative)
    {
        comboBox.addItem(stringAlternative);
        allIntegers.add(new Integer(intValue));
        allStrings.add(stringAlternative);
    }


    public Object getCellEditorValue()
    {
        logger.logComment("getCellEditorValue called");
        String selString = (String)comboBox.getSelectedItem();
        int index = allStrings.indexOf(selString);
        return (Integer)allIntegers.elementAt(index);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        logger.logComment("getTableCellEditorComponent called, object type: "+ value.getClass().getName());
        Integer chosenInt = ((Integer)value);
        int index = allIntegers.indexOf(chosenInt);
        String correspString = (String)allStrings.elementAt(index);
        comboBox.setSelectedItem(correspString);

        return comboBox;
    }


}
