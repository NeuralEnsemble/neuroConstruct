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


package ucl.physiol.neuroconstruct.utils.equation;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public abstract class EquationUnit
{
    private String name = null;

    public EquationUnit(String name)
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }

    public abstract String getNiceString();

    public abstract String toString();


    public abstract double evaluateAt(Argument[] args) throws EquationException;
}
