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


package ucl.physiol.neuroconstruct.utils.equation.functions;

import ucl.physiol.neuroconstruct.project.ProjectManager;
import ucl.physiol.neuroconstruct.utils.equation.*;


/**
 * Helper class for parsing equations. Function random(x) returns a pseudorandom, uniformly distributed value between 0 and x
 *
 * @author Padraig Gleeson
 *  
 */

public class RandomFunctionUnit extends FunctionUnit
{
    static final long serialVersionUID = -66653452445563623L;
                     
    

    public RandomFunctionUnit(EquationUnit internalEqn)
    {
        super(BasicFunctions.RANDOM, internalEqn);
    }

    public RandomFunctionUnit()
    {
    }
    
    

    public double evaluateAt(Argument[] args) throws EquationException
    {
        double arg = internalEqn.evaluateAt(args);
        
        return ProjectManager.getRandomGenerator().nextFloat()*arg;
    }    
    


}
