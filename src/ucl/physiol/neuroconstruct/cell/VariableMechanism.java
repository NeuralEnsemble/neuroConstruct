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
 

import java.io.*;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;
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

public class VariableMechanism implements Serializable, IMechanism
{
    private static final long serialVersionUID = -7566565188475532L;

    private static transient ClassLogger logger = new ClassLogger("VariableMechanism");
    
    private String name = null;
    VariableParameter param = null;
    private ArrayList<MechParameter> extraParameters = null;

    
    public VariableMechanism()
    {
    }

    public VariableMechanism(String name, VariableParameter param)
    {
        this.name = name;
        this.param = param;
        this.extraParameters = new ArrayList<MechParameter>();
    }

    public VariableParameter getParam()
    {
        return param;
    }

    public void setParam(VariableParameter param)
    {
        this.param = param;
    }
    
    @Override
    public Object clone()
    {
        VariableMechanism vm2 = new VariableMechanism(name, (VariableParameter)param.clone());
        for (MechParameter mp : extraParameters) {
            vm2.getExtraParameters().add((MechParameter) mp.clone());
        }
        return vm2;
    }
     public String getExtraParamsDesc()
    {
        StringBuilder info = new StringBuilder();
        
           
        if (extraParameters!=null)
        {
            ArrayList<MechParameter> mpa = (ArrayList<MechParameter>)GeneralUtils.reorderAlphabetically(extraParameters, true);
            
            for (MechParameter mp: mpa)
            {
                info.append(", ").append(mp.toString());
            }
        }
        return info.toString();
    }
    public String getExtraParamsBracket()
    {
        StringBuilder info = new StringBuilder("(");
           
        if (extraParameters!=null)
        {
            for (int i=0;i<extraParameters.size();i++)
            {
                if (i!=0) info.append(", ");
                info.append(extraParameters.get(i).toString());
            }
        }
        info.append(")");
        return info.toString();
    }
    
    public void setExtraParam(String name, float value)
    {       
        if (extraParameters==null)
            extraParameters = new ArrayList<MechParameter>();
        
        for (MechParameter mp: extraParameters)
        {
            if (mp.getName().equals(name))
            {
                logger.logComment("Updating current param: " + mp);
                mp.setValue(value);
                return;
            }
        } 
        // if not found
        MechParameter mp = new MechParameter(name, value);
        logger.logComment("Adding new param: " + mp);
        this.extraParameters.add(mp);
    }
    
    /*
    public ArrayList<VariableParameter> getParams()
    {
        return params;
    }

    public void setParams(ArrayList<VariableParameter> params)
    {
        this.params = params;
    }
    
    public double evaluateAt(String paramName, double varParamValue) throws ParameterException, EquationException
    {
        for(VariableParameter vp: params)
        {
            if (vp.getName().equals(paramName)){
                return vp.evaluateAt(varParamValue);
            }
        }
        throw new ParameterException("Parameter "+paramName+"cannot be evaluated at "+ varParamValue+" in variable mechanism "+ this.toString());
    }*/
    
    public double evaluateAt(double varParamValue) throws ParameterException, EquationException
    {
        return param.evaluateAt(varParamValue);
    }

    @Override
    public String toString()
    {
        
        return name + " with param: "+param;
        
    }
    
   
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
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
        final VariableMechanism other = (VariableMechanism) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
        {
            return false;
        }
        if (this.param != other.param && (this.param == null || !this.param.equals(other.param)))
        {
            return false;
        }

        ArrayList<MechParameter> otherParams = other.getExtraParameters();
        if (extraParameters != null) {
            if (otherParams == null) {
                return false;
            }

            if (otherParams.size() != extraParameters.size()) {
                return false;
            }

            for (int i = 0; i < extraParameters.size(); i++) {
                if (!otherParams.contains(extraParameters.get(i))) {
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 31 * hash + (this.param != null ? this.param.hashCode() : 0);
        return hash;
    }
    
    
    
    
     
    public static void main(String[] args) throws CloneNotSupportedException
    {
        try
        {


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
            
            System.out.println("Var param 1: "+ vp1); 
            
            VariableParameter vp2 = new VariableParameter("Rm", expression2, p, expressionArgs2);
            
            System.out.println("Var param 2: "+ vp2); 
            
            Argument[] a0 = new Argument[]{new Argument(p.getName(), 1)};
            
            for(double i=0;i<=1;i=i+0.2)
            {
                System.out.println("Value 1: "+ vp1.evaluateAt(i)); 
                System.out.println("                  Value 2: "+ vp2.evaluateAt(i)); 
            }
            
            
            VariableMechanism cm = new VariableMechanism("cm", vp1);

            System.out.println("Variable Mechanism: " + cm);
            float val = 1;
            
            System.out.println("Variable Mechanism eval at "+val+": " + cm.evaluateAt(val));
            
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public MechParameter getExtraParameter(String paramName) {
        if (extraParameters == null || extraParameters.isEmpty()) {
            return null;
        }

        for (MechParameter mp : extraParameters) {
            if (mp.getName().equals(paramName)) {
                return mp;
            }
        }
        return null;
    }

    public ArrayList<MechParameter> getExtraParameters() {
        if (extraParameters == null) {
            extraParameters = new ArrayList<MechParameter>();
        }
        return this.extraParameters;
    }

    public void setExtraParameters(ArrayList<MechParameter> params) {
        this.extraParameters = params;
    }


}
