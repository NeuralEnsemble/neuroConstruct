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
 

import java.io.*;
import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

 /**
  * A class representing a channel mechanism which can be added to
  *  sections, with variable parameters
  *
  * @author Padraig Gleeson
  *  
  *
  */

@SuppressWarnings("serial")

public class VariableMechanism implements Serializable
{
    static final long serialVersionUID = -7566565188475532L;
    
    private static transient ClassLogger logger = new ClassLogger("VariableMechanism");
    
    private String name = null;
    
    private ArrayList<VariableParameter> params = new ArrayList<VariableParameter>();

    public VariableMechanism()
    {
    }

    public VariableMechanism(String name)
    {
        this.name = name;
    }
    
    
    public ArrayList<VariableParameter> getParams()
    {
        return params;
    }

    public void setParams(ArrayList<VariableParameter> params)
    {
        this.params = params;
    }
    


    @Override
    public String toString()
    {
        
        return name + " with params: "+params;
        
    }
    
   
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    
     
    public static void main(String[] args) throws CloneNotSupportedException
    {
        try
        {

            VariableMechanism cm = new VariableMechanism("cm");

            System.out.println("ChannelMechanism: " + cm);

            String expression1 = "A + B*(p+C)";
            String expression2 = "1";


            Variable p = new Variable("p");
            Argument a = new Argument("A", 2);
            Argument b = new Argument("B", 4);
            Argument c = new Argument("C", -1);

            ArrayList<Argument> expressionArgs1 =  new ArrayList<Argument>();
            ArrayList<Argument> expressionArgs2 =  new ArrayList<Argument>();
            expressionArgs1.add(a);
            expressionArgs1.add(b);
            expressionArgs1.add(c);
            

            VariableParameter vp1 = new VariableParameter("cap", expression1, p, expressionArgs1);
            
            System.out.println("Var param: "+ vp1); 
            
            VariableParameter vp2 = new VariableParameter("Rm", expression2, p, expressionArgs2);
            
            System.out.println("Var param: "+ vp2); 
            
            Argument[] a0 = new Argument[]{new Argument(p.getName(), 1)};
            
            for(double i=0;i<=1;i=i+0.2)
            {
                System.out.println("Value 1: "+ vp1.evaluateAt(i)); 
                System.out.println("Value 2: "+ vp2.evaluateAt(i)); 
            }
            
            
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
    


}
