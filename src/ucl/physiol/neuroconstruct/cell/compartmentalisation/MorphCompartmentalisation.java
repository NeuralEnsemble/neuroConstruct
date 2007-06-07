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
 * Base class for instances of morphological projections/compartmentalisations
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
