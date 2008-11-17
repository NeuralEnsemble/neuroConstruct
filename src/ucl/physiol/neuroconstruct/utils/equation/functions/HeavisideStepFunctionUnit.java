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

import ucl.physiol.neuroconstruct.utils.equation.*;


/**
 * Helper class for parsing equations. Heaviside step function. Returns 0 if argument evaluates to <0, else returns 1
 *
 * @author Padraig Gleeson
 *  
 */

public class HeavisideStepFunctionUnit extends FunctionUnit
{
    static final long serialVersionUID = -53445615634523L;
                     

    public HeavisideStepFunctionUnit(EquationUnit internalEqn)
    {
        super(BasicFunctions.HEAVISIDE, internalEqn);
    }

    public HeavisideStepFunctionUnit()
    {
    }
    
    

    /*
     * Heaviside step function. 
     * @return 0 if argument evaluates to <0, else returns 1
     */
    public double evaluateAt(Argument[] args) throws EquationException
    {
        double val = internalEqn.evaluateAt(args);
        
        if (val<0) 
            return 0;
        else
            return 1;
    }    
    


}
