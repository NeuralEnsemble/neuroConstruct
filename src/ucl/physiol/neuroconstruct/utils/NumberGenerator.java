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

package ucl.physiol.neuroconstruct.utils;


import java.io.*;

import ucl.physiol.neuroconstruct.project.*;

/**
 * Generates numbers according to a specified pattern: fixed, random, gaussian.
 * Any number of these can be generated from a single NumberGenerator
 *
 * @author Padraig Gleeson
 *  
 */


public class NumberGenerator implements Serializable
{
    static final long serialVersionUID = -2523254286734506739L;
    
    private transient ClassLogger logger = new ClassLogger("NumberGenerator");

    public static final int FIXED_NUM = 0;
    public static final int RANDOM_NUM = 1;
    public static final int GAUSSIAN_NUM = 2;


    public static final int INT_GENERATOR = 10;
    public static final int FLOAT_GENERATOR = 11;


    public int distributionType = FIXED_NUM;
    public int numberType = FLOAT_GENERATOR;


    // variables for fixed num generator:
    float fixedNum = 0;

    // variables for random num generator:
    float max = 1;
    float min = 0;

    // extra variables for Gaussian
    float mean = 0f;
    float stdDev = 1f;
    

    public boolean isTypeFixedNum()
    {
        return this.distributionType == FIXED_NUM;
    }
    public boolean isTypeRandomNum()
    {
        return this.distributionType == RANDOM_NUM;
    }
    public boolean isTypeGaussianNum()
    {
        return this.distributionType == GAUSSIAN_NUM;
    }
    
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof NumberGenerator)
        {
            NumberGenerator other = (NumberGenerator) otherObj;

            if (distributionType == other.distributionType &&
                numberType == other.numberType &&
                fixedNum == other.fixedNum &&
                max == other.max &&
                min == other.min &&
                mean == other.mean &&
                stdDev == other.stdDev)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + this.distributionType;
        hash = 11 * hash + this.numberType;
        hash = 11 * hash + Float.floatToIntBits(this.fixedNum);
        hash = 11 * hash + Float.floatToIntBits(this.max);
        hash = 11 * hash + Float.floatToIntBits(this.min);
        hash = 11 * hash + Float.floatToIntBits(this.mean);
        hash = 11 * hash + Float.floatToIntBits(this.stdDev);
        return hash;
    }

    @Override
    public Object clone()
    {
        NumberGenerator ng = new NumberGenerator();
        ng.distributionType = this.distributionType;
        ng.numberType = this.numberType;
        ng.fixedNum = this.fixedNum;
        ng.max = this.max;
        ng.min = this.min;
        ng.mean = this.mean;
        ng.stdDev = this.stdDev;

        return ng;
    }

    /**
     * Generates a simple NumberGenerator with a fixed value of 1
     *
     */
    public NumberGenerator()
    {
        distributionType = FIXED_NUM;
        this.numberType = INT_GENERATOR;
        this.fixedNum = 1;

    }

    /**
     * Generates a simple NumberGenerator with a fixed value of fixedNum
     */
    public NumberGenerator(float fixedNum)
    {
        distributionType = FIXED_NUM;
        this.numberType = FLOAT_GENERATOR;
        this.fixedNum = fixedNum;
    }



    /**
     * Generates a simple NumberGenerator with a fixed value of fixedNum
     */
    public NumberGenerator(int fixedNum)
    {
        distributionType = FIXED_NUM;
        this.numberType = INT_GENERATOR;
        this.fixedNum = fixedNum;
    }


    public void initialiseAsFixedFloatGenerator(float num)
    {
        this.distributionType = FIXED_NUM;
        this.numberType = FLOAT_GENERATOR;
        fixedNum = num;
    }

    public void initialiseAsRandomFloatGenerator(float max, float min)
    {
        this.distributionType = RANDOM_NUM;
        this.numberType = FLOAT_GENERATOR;
        this.min = min;
        this.max = max;
    }

    public void initialiseAsGaussianFloatGenerator(float max, float min, float mean, float stdDev)
    {
        this.distributionType = GAUSSIAN_NUM;
        this.numberType = FLOAT_GENERATOR;
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.stdDev = stdDev;
    }
    
    public void initialiseAsFixedIntGenerator(int num)
    {
        this.distributionType = FIXED_NUM;
        this.numberType = INT_GENERATOR;
        fixedNum = num;
    }

    public void initialiseAsRandomIntGenerator(int max, int min)
    {
        this.distributionType = RANDOM_NUM;
        this.numberType = INT_GENERATOR;
        this.min = min;
        this.max = max;
    }

    public void initialiseAsGaussianIntGenerator(int max, int min, float mean, float stdDev)
    {
        this.distributionType = GAUSSIAN_NUM;
        this.numberType = INT_GENERATOR;
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.stdDev = stdDev;
    }
    
    /**
     * Generate the next number based on the internal specs. For an INT_GENERATOR it will return an int
     * as a float
     */
    public float getNextNumber()
    {
        if (numberType == FLOAT_GENERATOR)
        {
            switch (this.distributionType)
            {
                case (FIXED_NUM):
                    return fixedNum;

                case (RANDOM_NUM):
                    float rand0to1 = ProjectManager.getRandomGenerator().nextFloat();
                    return (min + (rand0to1 * (max - min)));

                case (GAUSSIAN_NUM):
                    double possGauss = -1 * Float.MAX_VALUE;

                    while (possGauss <= min || possGauss >= max)
                    {
                        double gaussMean0stdDev1 = ProjectManager.getRandomGenerator().nextGaussian();
                        possGauss = mean + (gaussMean0stdDev1 * stdDev);
                    }
                    return (float) possGauss;
            }
        }
        else if (numberType == INT_GENERATOR)
        {
            switch (this.distributionType)
            {
                case (FIXED_NUM):
                    return (int)fixedNum;

                case (RANDOM_NUM):
                    //float rand0to1 = rand.nextFloat();
                    return (int)min + ProjectManager.getRandomGenerator().nextInt((int)(max - min +1));

                case (GAUSSIAN_NUM):
                  ////  logger.logError("This method getNextInt() for GAUSSIAN_NUM needs more investigation...");
                    double possGauss = -1 * Float.MAX_VALUE;

                    while (possGauss <= min || possGauss >= max)
                    {
                        double gaussMean0stdDev1 = ProjectManager.getRandomGenerator().nextGaussian();
                        possGauss = mean + (gaussMean0stdDev1 * stdDev);
                        int suggestedVal = (int) (possGauss+0.5f); // So it's centred on the mean int value...
                        if (suggestedVal < min || suggestedVal > max)
                            possGauss = -1 * Float.MAX_VALUE;
                        else
                            return suggestedVal;

                    }
                    // Note this is not the best way of doing this...

                    return (int) possGauss;
            }
        }

        return 0;
    }



    /**
     * Generate a nominal value for the number. Useful only for giving an indication of what the value will be.
     * Will be the number itself for a fixed num, half way between the max and min for random, and the
     * mean for gaussian
     */
    public float getNominalNumber()
    {
        switch (this.distributionType)
        {
            case (FIXED_NUM):
                return fixedNum;
            case (RANDOM_NUM):
                return (min + ((max - min)/2));
            case (GAUSSIAN_NUM):
                return mean;

        }
        return Float.NaN;
    }


    public float getMinPossible()
    {
        if (isTypeFixedNum()) return fixedNum;
        else return min;
    }

    public float getMaxPossible()
    {
        if (isTypeFixedNum()) return fixedNum;
        else return max;
    }

    public float getMin()
    {
        return min;
    }

    public float getMax()
    {
        return max;
    }

    public float getFixedNum()
    {
        return this.fixedNum;
    }

    public float getStdDev()
    {
        return this.stdDev;
    }

    public float getMean()
    {
        return this.mean;
    }

    @Override
    public String toString()
    {
        if (this.numberType==FLOAT_GENERATOR)
        {
            switch (this.distributionType)
            {
                case (FIXED_NUM):
                    return ("Fixed Float Generator [value: " + this.fixedNum + "]");
                case (RANDOM_NUM):
                    return ("Random Float Generator [max: " + this.max + ", min: " + this.min + "]");
                case (GAUSSIAN_NUM):
                    return ("Gaussian Float Generator [mean: " + mean + ", std dev: " + stdDev + ", max: " + this.max +
                            ", min: " +
                            this.min + "]");
                default:
                    return "Unknown";
            }
        }
        else if (this.numberType==INT_GENERATOR)
        {
            switch (this.distributionType)
            {
                case (FIXED_NUM):
                    return ("Fixed Integer Generator [value: " + this.fixedNum + "]");
                case (RANDOM_NUM):
                    return ("Random Integer Generator [max: " + this.max + ", min: " + this.min + "]");
                case (GAUSSIAN_NUM):
                    return ("Gaussian Integer Generator [mean: " + mean + ", std dev: " + stdDev + ", max: " + this.max +
                            ", min: " +
                            this.min + "]");
                default:
                    return "Unknown";
            }
        }
        else
            return "Unknown number generator";

    }

    /**
     * Gives a short representation of the string for textfields, etc.
     * For FIXED_NUM returns the number on its own,
     * for RANDOM_NUM, min -> max, e.g. 0 ->10
     * for GAUSSIAN_NUM, mean +/- stdDev (min -> max), e.g. 70 +/- 5 (60 -> 80)
     * If the number type id not FIXED_NUM and is INT_GENERATOR, then (int) is appended
     * e.g. 0 -> 20 (int)
     */
    public String toShortString()
    {
        if (this.numberType==FLOAT_GENERATOR)
        {
            switch (this.distributionType)
            {
                case (FIXED_NUM):
                    if (fixedNum==(int)fixedNum)
                        return (int)fixedNum+"";
                    else
                    return this.fixedNum+"";
                case (RANDOM_NUM):
                    return (  this.min + " -> " + this.max);
                case (GAUSSIAN_NUM):
                    return ( + mean + " +/- " + stdDev + " (" + this.min +
                            " -> " +
                            this.max + ")");
                default:
                    return "Unknown";
            }
        }
        else if (this.numberType==INT_GENERATOR)
        {
            switch (this.distributionType)
            {
                case (FIXED_NUM):
                    if (fixedNum==(int)fixedNum)
                        return (int)fixedNum+"";
                    else
                    return this.fixedNum+"";
                case (RANDOM_NUM):
                    return ( this.min + " -> " + this.max + " (int)");
                case (GAUSSIAN_NUM):
                    return ( mean + " +/- " + stdDev + " (" + this.min +
                            " -> " +
                            this.max + ")"+ " (int)");
                default:
                    return "Unknown";
            }
        }
        else
            return "Unknown number generator";

    }


    /**
     * Will reproduce a NumberGenerator equal to that which returns
     * stringForm from the **toShortString()** function (NOT toString())
     */
    public NumberGenerator(String stringForm)
    {
        logger.logComment("Trying to create NumberGenerator from string: "+ stringForm);
        // Note this is not fully foolproof but works fine for strings generated by toString();

        if (stringForm.indexOf("int")>=0) this.numberType = INT_GENERATOR;
        else this.numberType = FLOAT_GENERATOR;

        if (stringForm.indexOf("->")>=0 &&
            stringForm.indexOf("+/-")<0)
        {
            this.distributionType = RANDOM_NUM;
        }
        else if (stringForm.indexOf("->")>=0 &&
                 stringForm.indexOf("+/-")>=0)
        {
            this.distributionType = GAUSSIAN_NUM;
        }
        else this.distributionType = FIXED_NUM;

        try
        {
            if (distributionType == FIXED_NUM)
            {
                fixedNum = Float.parseFloat(stringForm);
            }
            else if (distributionType == RANDOM_NUM)
            {
                String stringMin = stringForm.substring(0, stringForm.indexOf("->")).trim();
                String stringMax = stringForm.substring(stringForm.indexOf("->") + 2).trim();

                max = Float.parseFloat(stringMax);
                min = Float.parseFloat(stringMin);
            }
            else
            {
                String stringMean = stringForm.substring(0, stringForm.indexOf("+/-")).trim();

                String stringStdDev = stringForm.substring(stringForm.indexOf("+/-") + 3,
                                                           stringForm.indexOf("("));

                mean = Float.parseFloat(stringMean);
                stdDev = Float.parseFloat(stringStdDev);

                String stringMin = stringForm.substring(stringForm.indexOf("(") + 1,
                                                        stringForm.indexOf("->"));

                String stringMax = stringForm.substring(stringForm.indexOf("->") + 2,
                                                        stringForm.indexOf(")"));

                max = Float.parseFloat(stringMax);
                min = Float.parseFloat(stringMin);
            }
        }
        catch (NumberFormatException ex)
        {
            logger.logError("Problem reading the number from string: "+ stringForm);
            logger.logError("Current state: "+ this.toString());
            logger.logError("Error: ", ex);

        }



    }


    public static void main(String[] args)
    {
        NumberGenerator ng1 = new NumberGenerator();
        NumberGenerator ng2 = new NumberGenerator();

        ng1.initialiseAsGaussianIntGenerator(7,3,4,60f);
        ng2.initialiseAsRandomFloatGenerator(9,7);

        //ng.initialiseAsGaussianIntGenerator(5,-5, 0, 2);
        //ng1.initialiseAsFixedIntGenerator(3);


        NumberGenerator ng3 = new NumberGenerator(ng1.toShortString());

        System.out.println("Old NG: "+ ng1.toShortString());
        System.out.println("New NG: "+ ng3.toShortString());

        for (int i = 0; i < 50; i++)
        {
            System.out.println("Next Int: "+ ng1.getNextNumber());
            //System.out.println("Next Float: "+ ng.getNextFloat());
        }
    }


    // Functions needed for XMLEncoder...

    public void setFixedNum(float fixedNum)
    {
        this.fixedNum = fixedNum;
    }
    public void setMax(float max)
    {
        this.max = max;
    }
    public void setMean(float mean)
    {
        this.mean = mean;
    }
    public void setMin(float min)
    {
        this.min = min;
    }
    public int getDistributionType()
    {
        return distributionType;
    }
    public void setDistributionType(int distributionType)
    {
        this.distributionType = distributionType;
    }
    public void setStdDev(float stdDev)
    {
        this.stdDev = stdDev;
    }
    public int getNumberType()
    {
        return numberType;
    }
    public void setNumberType(int numberType)
    {
        this.numberType = numberType;
    }
}
