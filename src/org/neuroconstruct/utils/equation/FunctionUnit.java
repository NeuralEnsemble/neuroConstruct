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


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class FunctionUnit extends EquationUnit
{

    protected EquationUnit internalEqn = null;

    public FunctionUnit(String name, EquationUnit internalEqn)
    {
        super(name);
        this.internalEqn = internalEqn;
    }

    public abstract double evaluateAt(Argument[] args) throws EquationException;


    public String getNiceString()
    {
        return getName() + "("+ internalEqn.getNiceString()+ ")";
    }

    public String toString()
    {
        return getNiceString();
    }




}
