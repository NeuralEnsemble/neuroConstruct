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

package ucl.physiol.neuroconstruct.project;

/**
 * Helper class for specifying maximum and minimum lengths for synaptic connections
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class MaxMinLength
{
    private float maxLength = Float.MAX_VALUE;
    private float minLength = 0;

    private int numberAttempts = 100;

    public static final String RADIAL = "r";
    public static final String X_DIR = "x";
    public static final String Y_DIR = "y";
    public static final String Z_DIR = "z";


    private String dimension = RADIAL;

    public MaxMinLength()
    {

    }

    /**
     * Creates new MaxMinLength based on explicit values
     */
    public MaxMinLength(float maxLength, float minLength, String dimension, int numberAttempts)
    {
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.numberAttempts = numberAttempts;
        this.dimension = dimension;
    }


    /**
     * Return a simple string representation...
     * @return A string summarising the state
     */
    public String toString()
    {
        return "Max: "
            + maxLength
            + ", min: "
            + minLength
               + ", dim: "
               + dimension


               + " (num attempts: "
               + numberAttempts
               + ")";
    }
    public float getMaxLength()
    {
        return maxLength;
    }
    public float getMinLength()
    {
        return minLength;
    }
    public String getDimension()
    {
        return dimension;
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension;
    }


    public int getNumberAttempts()
    {
        return numberAttempts;
    }
    public void setMaxLength(float maxLength)
    {
        this.maxLength = maxLength;
    }
    public void setMinLength(float minLength)
    {
        this.minLength = minLength;
    }
    public void setNumberAttempts(int numberAttempts)
    {
        this.numberAttempts = numberAttempts;
    }



}
