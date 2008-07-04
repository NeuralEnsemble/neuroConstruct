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
 * Helper class for specifying search patterns (ways to link up presynaptic
 * connection points on axons with dendritic connection points)
 *
 * @author Padraig Gleeson
 *  
 */


public class SearchPattern
{
    public int type = Integer.MIN_VALUE;

    /**
     * Needed only where searchPattern = RANDOM_CLOSE. It's the number of random
     *  points on the target cell group to pick before selecting the nearest one...
     */
    public int randomCloseNumber = Integer.MIN_VALUE;


    public static final int COMPLETELY_RANDOM = 0;
    public static final int RANDOM_CLOSE = 1;
    public static final int CLOSEST = 2;


    public SearchPattern(){};




    public static SearchPattern getRandomSearchPattern()
    {
        SearchPattern sp = new SearchPattern();
        sp.type = COMPLETELY_RANDOM;
        return sp;
    }

    public static SearchPattern getClosestSearchPattern()
    {
        SearchPattern sp = new SearchPattern();
        sp.type = CLOSEST;
        return sp;
    }

    public static SearchPattern getRandomCloseSearchPattern(int randCloseNum)
    {
        SearchPattern sp = new SearchPattern();
        sp.type = RANDOM_CLOSE;
        sp.randomCloseNumber = randCloseNum;
        return sp;
    }


    /**
     * Return a simple string representation...
     * @return A string summarising the state
     */
    @Override
    public String toString()
    {
        switch (type)
        {
            case (COMPLETELY_RANDOM):
                return "Completely Random";
            case (RANDOM_CLOSE):
                return "Random, Close (Num to search: "+randomCloseNumber+")";
            case (CLOSEST):
                return "Closest";
        }
        return "Unknown SearchPattern";
    }


    // Functions needed for XMLEncoder...

    public int getRandomCloseNumber()
    {
        return randomCloseNumber;
    }
    public int getType()
    {
        return type;
    }
    public void setRandomCloseNumber(int randomCloseNumber)
    {
        this.randomCloseNumber = randomCloseNumber;
    }
    public void setType(int type)
    {
        this.type = type;
    }



}
