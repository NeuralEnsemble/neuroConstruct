/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */


package ucl.physiol.neuroconstruct.utils.equation;

import java.io.Serializable;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class EquationUnit implements Serializable
{
    
    private String name = null;

    public EquationUnit()
    {
    }

    protected void setName(String name)
    {
        this.name = name;
    }
    
    public EquationUnit(String name)
    {
        this.name = name;
    }


    public String getName()
    {
        return name;
    }

    public abstract String getNiceString();

    @Override
    public abstract String toString();
    
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    


    public abstract double evaluateAt(Argument[] args) throws EquationException;
}







