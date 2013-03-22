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

package ucl.physiol.neuroconstruct.project.cellchoice;

import java.util.*;

/**
 * Helper class for getting list of CellChoice subclasses
 *
 * @author Padraig Gleeson
 *  
 */

public class CellChooserHelper
{
    private static Vector<String> cellChoices = new Vector<String>();

    static
    {
        /** @todo automatically look these up... */
        cellChoices.add("AllCells");
        cellChoices.add("FixedNumberCells");
        cellChoices.add("PercentageOfCells");
        cellChoices.add("IndividualCells");
        cellChoices.add("RegionAssociatedCells");
    }

    public static String[] getAllCellChoosers()
    {
        /** @todo Make more efficient... */

        String[] allCellChoices = new String[cellChoices.size()];

        for (int i = 0; i < cellChoices.size(); i++)
        {
            allCellChoices[i] = (String)cellChoices.elementAt(i);
        }

        return allCellChoices;
    }

    public static CellChooser getCellChooser(String name) throws CellChooserException
    {
        /** @todo replace with reflection... */
        if (name.equals("AllCells"))
            return new AllCells();
        else if (name.equals("FixedNumberCells"))
            return new FixedNumberCells();
        else if (name.equals("PercentageOfCells"))
            return new PercentageOfCells();
        else if (name.equals("IndividualCells"))
            return new IndividualCells();
        else if (name.equals("RegionAssociatedCells"))
            return new RegionAssociatedCells();



        else throw new CellChooserException("No such pattern name");
    }

}
