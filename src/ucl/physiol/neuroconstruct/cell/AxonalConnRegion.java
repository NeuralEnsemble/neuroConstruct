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

 package ucl.physiol.neuroconstruct.cell;


import java.io.Serializable;

import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

 /**
  *
  * Region in 3D space around a cell where synaptic connections of certain types can be made,
  * provided there is a dendrite allowing it
  *
  * @author Padraig Gleeson
  *  
  *
  */


public class AxonalConnRegion implements Serializable
{
    
    static final long serialVersionUID = -1837545937744L;
    
    private String name = null;
    private Region region = null;

     //private String connectivityProbability = null;

    public AxonalConnRegion()
    {
    }

    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof AxonalConnRegion)
        {
            AxonalConnRegion other = (AxonalConnRegion) otherObj;

            if (name.equals(other.name)&&
                region.equals(other.region))
            {
                return true;
            }
        }
        return false;
   }


    public String getName()
    {
        return name;
    }

    public Region getRegion()
    {
        return region;
    }

    public void setRegion(Region region)
    {
        this.region = region;
    }


    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return "AxonalConnRegion: "+name+", region: "+region;
    }


    public String getInfo(boolean html)
    {
        //return name+ " allows synapses: "+GeneralUtils.getTabbedString(synapticPropList.toString(), "b", html)+" in region "+GeneralUtils.getTabbedString(region.toString(), "b", html)+" "+GeneralUtils.getEndLine(html);
        return GeneralUtils.getTabbedString(name, "b", html)+ " allows synaptic connections in region "
                                            +GeneralUtils.getTabbedString(region.toString(), "b", html);
    }

    public Object clone()
    {
        AxonalConnRegion acr = new AxonalConnRegion();
        acr.setName(new String(this.name));
        acr.setRegion((Region)region.clone());

        return acr;
    }




    public static void main(String[] args)
    {
        new AxonalConnRegion();
    }
}
