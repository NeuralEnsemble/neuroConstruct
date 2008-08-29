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

public class Variable extends EquationUnit
{
    static final long serialVersionUID = -47164166L;

    public Variable(String name)
    {
        super(name);
    }

    public double evaluateAt(Argument[] args) throws EquationException
    {
        for (int i = 0; i < args.length; i++)
        {
            //logger.logComment("Checking if "+ args[i].getName()
            //    + " equals "+ getName());

            if (args[i].getName().equals(getName()))
                return args[i].getValue();
        }
        throw new EquationException("Asked to evaluate variable "
                                    + getName() + " with: "+
                                    Argument.toString(args) +
                                    " but can't find variable in arguments");
    };


    public String getNiceString()
    {
        return getName();
    }

    public String toString()
    {
        return getName();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Variable)
        {
            Variable v = (Variable)obj;
            if (v.getName().equals(getName())) return true;
        }
        return false;
    }
    
    @Override
    public Object clone()
    {
        Variable v2 = new Variable(new String(getName()));
        return v2;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (getName() != null ? getName().hashCode() : 0);
        return hash;
    }
    

    /**
     * Default constructor is needed for XMLEncoder.
     */
    public Variable()
    {
    }
    
    @Override
    public void setName(String name)
    {
        super.setName(name);
    }
    
}
