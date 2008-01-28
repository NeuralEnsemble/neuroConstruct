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
 * Parameter used for storing internal variables, in string or float form
 *
 * @author Padraig Gleeson
 *  
 */


public class InternalStringFloatParameter extends InternalParameter
{
    String stringValue = null;

    String defaultStringValue = null;

    /**
     * String or Float array of valued it can take. Exception handling should be done in this
     * class, but its done when this is used elsewhere...
     */
    Object[] acceptableValues = null;


    public InternalStringFloatParameter(String parameterName,String parameterDescription, float defaultValue)
    {
        super(parameterName, parameterDescription, defaultValue);
    }

    public InternalStringFloatParameter(String parameterName,String parameterDescription, String defaultStringValue)
    {
        this.parameterName = parameterName;
        this.parameterDescription = parameterDescription;
        this.defaultStringValue = defaultStringValue;
        this.stringValue = defaultStringValue;
    }


    public void setAcceptableParameterValues(Object[] acceptableValues)
    {
        this.acceptableValues = acceptableValues;
    }


    public Object[] getAcceptableParameterValues()
    {
        return acceptableValues;
    }



    public String toString()
    {
        if (stringValue!=null)
        {
            return "Internal Parameter (string): " + this.parameterName
                + " (" + parameterDescription
                + ") Value: " + stringValue
                + " (default: " + defaultStringValue
                + ")";
        }
        return "Internal Parameter: " + parameterName
               + " ("+ parameterDescription
               + ") Value: "+ value
               + " (default: "+ defaultValue
               + ")";
    }

    public String getDefaultStringValue()
    {
        return defaultStringValue;
    }

    public void setDefaultStringValue(String defaultStringValue)
    {
        this.defaultStringValue = defaultStringValue;
    }



    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }




}
