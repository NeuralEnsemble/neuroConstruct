/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.gui;

import java.awt.Color;


/**
 * A class summarising the validity status of cells, etc.
 * @author Padraig Gleeson
 * @version 1.0.3
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
     * These are used to colour the html representation of the warning, etc.
     */
    public static final Color VALIDATION_COLOUR_OK_OBJ = Color.GREEN;
    public static final Color VALIDATION_COLOUR_WARN_OBJ = new Color(13*16+13,8*16+12,0);
    public static final Color VALIDATION_COLOUR_ERROR_OBJ = Color.red;



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

/*
    public class Validity
    {
        String desc = null;

        public static final Validity VALID;
        //public final Validity WARNING = new Validity("Warning");
        //public final Validity ERROR = new Validity("Error");

        static
        {
            VALID = new Validity("Valid");
        }

        private Validity()
        {

        }

        private Validity(String desc)
        {
            this.desc = desc;
        }

        public String toString()
        {
            return desc;
        }
*/




}
