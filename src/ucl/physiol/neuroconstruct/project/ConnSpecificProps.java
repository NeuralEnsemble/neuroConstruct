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

import ucl.physiol.neuroconstruct.utils.GeneralUtils;



/**
 * Class for extra info on single connection
 *
 * @author Padraig Gleeson
 *  
 */

public class ConnSpecificProps
{
    /** @todo Put in get set etc */
    public String synapseType = null;
    public float weight = 1;
    public float internalDelay = 0;
    //public float threshold = 0;

    private ConnSpecificProps()
    {

    }
    public ConnSpecificProps(String synapseType)
    {
        this.synapseType = synapseType;
    }
    public ConnSpecificProps(ConnSpecificProps props)
    {
        this.synapseType = props.synapseType;
        this.weight = props.weight;
        this.internalDelay = props.internalDelay;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ConnSpecificProps other = (ConnSpecificProps) obj;
        if (this.synapseType != other.synapseType && (this.synapseType == null || !this.synapseType.equals(other.synapseType)))
        {
            return false;
        }
        if (this.weight != other.weight)
        {
            return false;
        }
        if (this.internalDelay != other.internalDelay)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61 * hash + (this.synapseType != null ? this.synapseType.hashCode() : 0);
        hash = 61 * hash + Float.floatToIntBits(this.weight);
        hash = 61 * hash + Float.floatToIntBits(this.internalDelay);
        return hash;
    }
    

    @Override
    public String toString()
    {
        return "ConnSpecificProps [synapseType: "+synapseType
            +", internalDelay: "+internalDelay+", weight: "+weight+"]";
    }

    public String toNiceString()
    {
        return "Props for "+synapseType
            +": int del: "+internalDelay+" ms, weight: "+weight+"";
    }

    public String details(boolean html)
    {
        return "Props for "+GeneralUtils.getBold(synapseType, html)
            +": int del: "+GeneralUtils.getBold(internalDelay, html)+" ms, weight: "+GeneralUtils.getBold(weight, html)+"";
    }



}



