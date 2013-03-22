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

package ucl.physiol.neuroconstruct.gui;

import java.awt.Color;


/**
 * A class summarising the validity status of cells, etc.
 * @author Padraig Gleeson
 *  
 */


public class ValidityStatus
{
    /** @todo Update this class using enum for validity */
    public static final String VALIDATION_OK = "Valid";
    public static final String VALIDATION_WARN = "Warning";
    public static final String VALIDATION_ERROR = "Error";

    /**
     * These are used to colour the html representation of the warning, etc.
     */
    public static final String VALIDATION_COLOUR_OK = "green";
    public static final String VALIDATION_COLOUR_WARN = "#DD8C00";
    public static final String VALIDATION_COLOUR_ERROR = "red";

    /**
     * Extra info on validity status, not necessarily problematic
     */
    public static final String VALIDATION_COLOUR_INFO = "#A9A9A9";

    /**
     * These are used to colour the html representation of the warning, etc.
     */
    public static final Color VALIDATION_COLOUR_OK_OBJ = Color.GREEN;
    public static final Color VALIDATION_COLOUR_WARN_OBJ = new Color(13*16+13,8*16+12,0);
    public static final Color VALIDATION_COLOUR_ERROR_OBJ = Color.red;

    public static final String PROJECT_IS_VALID = "Project is valid";


    private String message = null;
    private String validity = null;

    private ValidityStatus()
    {

    }

    private ValidityStatus(String validity, String message)
    {
        this.validity = validity;
        this.message = message;
    }

    public static ValidityStatus getValidStatus(String message)
    {
        return new ValidityStatus(VALIDATION_OK, message);
    }

    public boolean isValid()
    {
        return this.validity.equals(VALIDATION_OK);
    }
    public boolean isError()
    {
        return this.validity.equals(VALIDATION_ERROR);
    }
    public boolean isWarning()
    {
        return this.validity.equals(VALIDATION_WARN);
    }




    public static ValidityStatus getWarningStatus(String message)
    {
        return new ValidityStatus(VALIDATION_WARN, message);
    }
    public static ValidityStatus getErrorStatus(String message)
    {
        return new ValidityStatus(VALIDATION_ERROR, message);
    }

    public String getMessage()
    {
        return message;
    }

    public String getValidity()
    {
        return validity;
    }

    public String getColour()
    {
        if (this.validity.equals(VALIDATION_OK)) return VALIDATION_COLOUR_OK;
        if (this.validity.equals(VALIDATION_WARN)) return VALIDATION_COLOUR_WARN;
        return VALIDATION_COLOUR_ERROR;
    }
    @Override
    public String toString()
    {
        return message;
    }

    public static String combineValidities(String originalValidity, String newTestValidity)
    {
        if (originalValidity.equals(VALIDATION_ERROR))
        {
            // can't get any worse...
            return originalValidity;
        }
        if (originalValidity.equals(VALIDATION_WARN))
        {
            if (newTestValidity.equals(VALIDATION_ERROR))
                return VALIDATION_ERROR;
            else
                return VALIDATION_WARN;
        }
        else
        {
            return newTestValidity;
        }
    }




}
