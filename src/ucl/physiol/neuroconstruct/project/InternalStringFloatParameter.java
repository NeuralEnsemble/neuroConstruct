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


/**
 * Parameter used for storing internal variables, in string or float form
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

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
