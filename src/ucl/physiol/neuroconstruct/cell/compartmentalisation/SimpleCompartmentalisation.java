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

import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Morphological projection/compartmentalisations for test purposes only. Will not produce a cell with similar behaviour
 *
 * @author Padraig Gleeson
 *  
 *
 */

public class SimpleCompartmentalisation extends MorphCompartmentalisation
{
    ClassLogger logger = new ClassLogger("SimpleCompartmentalisation");

    public SimpleCompartmentalisation()
    {
        super("Test Compartmentalisation",
              "Used for testing the Morphological compartmentalisation framework. "
              +"Will not produce a morphology with similar electrical behaviour!");
    }

    protected Cell generateComp(Cell origCell)
    {
        Cell newCell = (Cell)origCell.clone();

        Vector<Segment> segs = newCell.getAllSegments();
        int origNumSegs = segs.size();
        logger.logComment("Segs: "+ origNumSegs);

        for (int i = (origNumSegs-1); i > 1; i--)
        {
            logger.logComment("Removing seg: "+i);
            this.mapper.addMapping(new SegmentRange(segs.get(i).getSegmentId(),segs.get(i).getSegmentLength(), 0,1),
                new SegmentRange[]{new SegmentRange(segs.get(1).getSegmentId(),segs.get(1).getSegmentLength(), 0,1)});
            segs.remove(i);
        }


        return newCell;
    };

    public static void main(String[] args)
    {
        SimpleCompartmentalisation sproj = new SimpleCompartmentalisation();

        SimpleCell cell = new SimpleCell("Guinea");

        Cell newCell = sproj.getCompartmentalisation(cell);

        System.out.println("----------------   Original cell   ----------------");

        System.out.println(CellTopologyHelper.printDetails(cell, null));

        System.out.println("----------------   New cell   ----------------");

        System.out.println(CellTopologyHelper.printDetails(newCell, null));

    }
}
