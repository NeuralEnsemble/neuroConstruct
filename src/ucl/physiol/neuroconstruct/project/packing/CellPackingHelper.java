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

package ucl.physiol.neuroconstruct.project.packing;

import java.util.*;

/**
 * Helper class for getting list of packing patterns
 *
 * @author Padraig Gleeson
 *  
 */

public class CellPackingHelper
{
    private static Vector<String> cellPackingPatterns = new Vector<String>();

    static
    {
        /** @todo automatically look these up... */
        cellPackingPatterns.add("RandomCellPackingAdapter");
        cellPackingPatterns.add("CubicClosePackedCellPackingAdapter");
        cellPackingPatterns.add("SimpleRegularCellPackingAdapter");
        cellPackingPatterns.add("SinglePositionedCellPackingAdapter");
        cellPackingPatterns.add("HexagonalLayerPackingAdapter");
        cellPackingPatterns.add("OneDimRegSpacingPackingAdapter");
    }

    public static String[] getAllCellPackingPatterns()
    {
        /** @todo Make more efficient... */

        String[] allCellPackPatterns = new String[cellPackingPatterns.size()];

        for (int i = 0; i < cellPackingPatterns.size(); i++)
        {
            allCellPackPatterns[i] = (String)cellPackingPatterns.elementAt(i);
        }

        return allCellPackPatterns;
    }

    public static CellPackingAdapter getCellPackingAdapter(String name) throws CellPackingException
    {
        /** @todo replace with reflection... */
        if (name.equals("RandomCellPackingAdapter"))
            return new RandomCellPackingAdapter();
        else if (name.equals("CubicClosePackedCellPackingAdapter"))
            return new CubicClosePackedCellPackingAdapter();
        else if (name.equals("SimpleRegularCellPackingAdapter"))
            return new SimpleRegularCellPackingAdapter();
        else if (name.equals("SinglePositionedCellPackingAdapter"))
            return new SinglePositionedCellPackingAdapter();
        else if (name.equals("HexagonalLayerPackingAdapter"))
            return new HexagonalLayerPackingAdapter();

        else if (name.equals("OneDimRegSpacingPackingAdapter"))
            return new OneDimRegSpacingPackingAdapter();

        else throw new CellPackingException("No such pattern name");
    }

}
