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
 * @version 1.0.4
 */

public class Variable extends EquationUnit
{


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
}
