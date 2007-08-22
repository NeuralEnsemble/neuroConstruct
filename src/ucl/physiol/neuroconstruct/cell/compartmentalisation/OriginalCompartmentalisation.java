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

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

import ucl.physiol.neuroconstruct.cell.Cell;


/**
 * Morphological projection/compartmentalisations which simply returns the detailed morphology fed in to it
 *
 * @author Padraig Gleeson
 *  
 *
 */

public class OriginalCompartmentalisation extends MorphCompartmentalisation
{
    public static final String ORIG_COMP = "Original Compartmentalisation";

    public OriginalCompartmentalisation()
    {
        super(ORIG_COMP,
              "Returns identical morphology to that put in. Uses all of the morphological "
              +"data present in the original file. Note: for GENESIS each segment will be "
              +"mapped to a cylindrical compartment. Surface area, but not necessarily axial resistance will be conserved");
    };

    protected Cell generateComp(Cell origCell)
    {
        /** @todo See if there's a problem just returning the original object... */
        Cell newCell = (Cell)origCell.clone();
        return  newCell;
    };
}
