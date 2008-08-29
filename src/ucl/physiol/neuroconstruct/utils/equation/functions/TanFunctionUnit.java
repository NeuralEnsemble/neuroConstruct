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
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class TanFunctionUnit extends FunctionUnit
{
    static final long serialVersionUID = -782627684L;
                     

    public TanFunctionUnit(EquationUnit internalEqn)
    {
        super(BasicFunctions.TANGENT, internalEqn);
    }

    public TanFunctionUnit()
    {
    }
    
    

    public double evaluateAt(Argument[] args) throws EquationException
    {
        return Math.tan(internalEqn.evaluateAt(args));
    }    
    


}
