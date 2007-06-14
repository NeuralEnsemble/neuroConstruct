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

package ucl.physiol.neuroconstruct.project.cellchoice;

import java.util.*;

/**
 * Helper class for getting list of CellChoice subclasses
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
