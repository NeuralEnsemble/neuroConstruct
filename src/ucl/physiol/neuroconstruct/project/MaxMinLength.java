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
 * Helper class for specifying maximum and minimum lengths for synaptic connections
 *
 * @author Padraig Gleeson
 *  
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
    public static final String SOMA = "s";


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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MaxMinLength other = (MaxMinLength) obj;
        if (this.maxLength != other.maxLength) {
            return false;
        }
        if (this.minLength != other.minLength) {
            return false;
        }
        if (this.numberAttempts != other.numberAttempts) {
            return false;
        }
        if (this.dimension != other.dimension && (this.dimension == null || !this.dimension.equals(other.dimension))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Float.floatToIntBits(this.maxLength);
        hash = 11 * hash + Float.floatToIntBits(this.minLength);
        hash = 11 * hash + this.numberAttempts;
        hash = 11 * hash + (this.dimension != null ? this.dimension.hashCode() : 0);
        return hash;
    }
    
    


    /**
     * Return a simple string representation...
     * @return A string summarising the state
     */
    @Override
    public String toString()
    {
        String maxLenStr = minLength+"";
        
        if (maxLength==Float.MAX_VALUE)
            maxLenStr = "MAX";
        
        return "Max: "
            + maxLenStr
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
