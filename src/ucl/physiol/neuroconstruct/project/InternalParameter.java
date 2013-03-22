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

package ucl.physiol.neuroconstruct.project;

import java.io.Serializable;


/**
 * Parameter used for storing internal variables
 *
 * @author Padraig Gleeson
 *  
 */


public class InternalParameter implements Serializable
{
    static final long serialVersionUID = -6949393265L;
    
    public String parameterName;
    public String parameterDescription;
    public float defaultValue;
    public float value;


    public InternalParameter()
    {

    }

    public InternalParameter(String parameterName,String parameterDescription, float defaultValue)
    {
        this.parameterName = parameterName;
        this.parameterDescription = parameterDescription;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof InternalParameter)
        {
            InternalParameter other = (InternalParameter) otherObj;

            if (parameterName.equals(other.parameterName) &&
                parameterDescription.equals(other.parameterDescription) &&
                defaultValue == other.defaultValue &&
                value == other.value)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + (this.parameterName != null ? this.parameterName.hashCode() : 0);
        hash = 13 * hash + (this.parameterDescription != null ? this.parameterDescription.hashCode() : 0);
        hash = 13 * hash + Float.floatToIntBits(this.defaultValue);
        hash = 13 * hash + Float.floatToIntBits(this.value);
        return hash;
    }


    @Override
    public String toString()
    {
        return "Internal Parameter: " + parameterName
               + " ("+ parameterDescription
               + ") Value: "+ value
               + " (default: "+ defaultValue
               + ")";
    }
    public float getDefaultValue()
    {
        return defaultValue;
    }
    public String getParameterDescription()
    {
        return parameterDescription;
    }
    public String getParameterName()
    {
        return parameterName;
    }
    public float getValue()
    {
        return value;
    }
    public void setDefaultValue(float defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    public void setParameterDescription(String parameterDescription)
    {
        this.parameterDescription = parameterDescription;
    }
    public void setParameterName(String parameterName)
    {
        this.parameterName = parameterName;
    }
    public void setValue(float value)
    {
        this.value = value;
    }
}
