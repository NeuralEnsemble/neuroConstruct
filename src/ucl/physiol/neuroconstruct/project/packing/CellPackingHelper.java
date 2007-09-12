/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
