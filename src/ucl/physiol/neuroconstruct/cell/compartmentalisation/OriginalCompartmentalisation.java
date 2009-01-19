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
