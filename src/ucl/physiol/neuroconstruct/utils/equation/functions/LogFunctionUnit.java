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

public class LogFunctionUnit extends FunctionUnit
{
    static final long serialVersionUID = -765989678L;
                     

    public LogFunctionUnit(EquationUnit internalEqn)
    {
        super(BasicFunctions.LOG, internalEqn);
    }

    public LogFunctionUnit()
    {
    }
    

    public double evaluateAt(Argument[] args) throws EquationException
    {
        return Math.log10(internalEqn.evaluateAt(args));
    }    
    


}
