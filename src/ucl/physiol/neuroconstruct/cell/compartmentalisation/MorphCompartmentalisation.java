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
 * Base class for instances of morphological projections/compartmentalisations
 *
 * @author Padraig Gleeson
 *  
 *
 */

public abstract class MorphCompartmentalisation
{
    protected String name = null;
    protected String description = null;

    protected SegmentLocMapper mapper = new SegmentLocMapper();

    public final Cell getCompartmentalisation(Cell origCell)
    {
        mapper = new SegmentLocMapper();
        return generateComp(origCell);
    };

    protected abstract Cell generateComp(Cell origCell);


    private MorphCompartmentalisation()
    {

    }


    public SegmentLocMapper getSegmentMapper()
    {
        return this.mapper;
    }


    protected MorphCompartmentalisation(String name, String description)
    {
        this.name = name;
        this.description = description;
    }


    @Override
    public String toString()
    {
        return name;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

}
