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

package ucl.physiol.neuroconstruct.project;


/**
 * Parameter used for storing internal variables
 *
 * @author Padraig Gleeson
 *  
 */


public class InternalParameter
{
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
