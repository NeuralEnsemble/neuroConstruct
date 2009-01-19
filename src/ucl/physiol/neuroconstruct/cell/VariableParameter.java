/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package ucl.physiol.neuroconstruct.cell;
 

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public VariableParameter()
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
    
    @Override
    public Object clone()
    {
        ArrayList<Argument> expressionArgs2 = new ArrayList<Argument>();
        for(Argument a: expressionArgs)
            expressionArgs2.add((Argument)a.clone());
            
        VariableParameter vp2 = new VariableParameter();
        vp2.setName(new String(name));
        vp2.setParameterisation((Variable)parameterisation.clone());
        vp2.setExpressionArgs(expressionArgs2);
        try
        {
            vp2.setExpression(Expression.parseExpression(expression.toString(), vp2.getAllVariables()));
        }
        catch (EquationException ex)
        {
            return null;
        }
        
        
        return vp2;
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
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final VariableParameter other = (VariableParameter) obj;
        
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
        {
            return false;
        }
        if (this.expression != other.expression && (this.expression == null || !this.expression.equals(other.expression)))
        {
            return false;
        }
        if (this.expressionArgs != other.expressionArgs && (this.expressionArgs == null || !this.expressionArgs.equals(other.expressionArgs)))
        {
            return false;
        }
        if (this.parameterisation != other.parameterisation && (this.parameterisation == null || !this.parameterisation.equals(other.parameterisation)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + (this.expression != null ? this.expression.hashCode() : 0);
        hash = 79 * hash + (this.expressionArgs != null ? this.expressionArgs.hashCode() : 0);
        hash = 79 * hash + (this.parameterisation != null ? this.parameterisation.hashCode() : 0);
        return hash;
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
        public ArrayList<Argument> getExpressionArgs()
    {
        return expressionArgs;
    }

    public void setExpressionArgs(ArrayList<Argument> expressionArgs)
    {
        this.expressionArgs = expressionArgs;
    }

    public Variable getParameterisation()
    {
        return parameterisation;
    }

    public void setParameterisation(Variable parameterisation)
    {
        this.parameterisation = parameterisation;
    }
    
    
}
