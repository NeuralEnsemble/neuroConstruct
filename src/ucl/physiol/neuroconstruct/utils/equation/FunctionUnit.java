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
    static final long serialVersionUID = -97991769713623L;
                     
    protected EquationUnit internalEqn = null;

    public FunctionUnit(String name, EquationUnit internalEqn)
    {
        super(name);
        this.internalEqn = internalEqn;
    }

    public FunctionUnit()
    {
    }
    

    public abstract double evaluateAt(Argument[] args) throws EquationException;
    
    
   @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof FunctionUnit)
        {
            FunctionUnit fu = (FunctionUnit)obj;
            
            if (!fu.getName().equals(getName())) return false;
            if (!fu.internalEqn.equals(internalEqn)) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + getName().hashCode();
        hash = 53 * hash + internalEqn.hashCode();
        return hash;
    }


    public String getNiceString()
    {
        return getName() + "("+ internalEqn.getNiceString()+ ")";
    }

    public String toString()
    {
        return getNiceString();
    }

    public EquationUnit getInternalEqn()
    {
        return internalEqn;
    }

    public void setInternalEqn(EquationUnit internalEqn)
    {
        this.internalEqn = internalEqn;
    }
    
    @Override
    public void setName(String name)
    {
        super.setName(name);
    }




}
