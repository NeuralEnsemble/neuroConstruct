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

package ucl.physiol.neuroconstruct.cell;
 

import java.io.Serializable;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.utils.equation.*;

 /**
  * A class representing a variable parameter within a channel mechanism 
  *
  * @author Padraig Gleeson
  *  
  *
  */

@SuppressWarnings("serial")

public class VariableParameter implements Serializable
{
    static final long serialVersionUID = -1345234188475532L;
    
    private String name = null;
    
    private EquationUnit expression = null;
    
    private ArrayList<Argument> expressionArgs =  new ArrayList<Argument>();
    
    private Variable parameterisation =  null;

    private VariableParameter()
    {
    }

    public VariableParameter(String name, 
                             String expr, 
                             Variable paramaterisation, 
                             ArrayList<Argument> expressionArgs)
                                throws EquationException
    {
        this.name = name;
        this.parameterisation = paramaterisation;
        this.expressionArgs = expressionArgs;
        
        this.expression = Expression.parseExpression(expr, getAllVariables());
        
    }
    
    private Variable[] getAllVariables()
    {
        Variable[] varArray = new Variable[expressionArgs.size()+1];
        varArray[0] = parameterisation;
        for(int i=0;i<expressionArgs.size();i++)
        {
            varArray[i+1] = new Variable(expressionArgs.get(i).getName());
        }
        return varArray;
        
    }
    
    public double evaluateAt(double paramVal) throws EquationException
    {
        Argument a0 = new Argument(parameterisation.getName(), paramVal);
        Argument[] evalArgs =  new Argument[1+expressionArgs.size()];
        evalArgs[0] = a0;
        
        for(int i=0;i<expressionArgs.size();i++)
        {
            evalArgs[i+1] = expressionArgs.get(i);
        }
        
        return expression.evaluateAt(evalArgs);
        
    }
    

    @Override
    public String toString()
    {   
        String vars = "";
        
        for(int i=0;i<expressionArgs.size();i++)
        {
            String val = expressionArgs.get(i).getValue()+"";
            if (expressionArgs.get(i).getValue()==(int)expressionArgs.get(i).getValue())
                val = (int)expressionArgs.get(i).getValue()+"";
            vars =  vars + ", "+ expressionArgs.get(i).getName() + " (="+val+")";
        }
        return name + " = f("+parameterisation+vars+") = "+ expression.getNiceString();
        
            
    }
   
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public EquationUnit getExpression()
    {
        return expression;
    }

    public void setExpression(EquationUnit expression)
    {
        this.expression = expression;
    }
    
    
}
