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
