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

 package ucl.physiol.neuroconstruct.cell;


import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

 /**
  *
  * Region in 3D space around a cell where synaptic connections of certain types can be made,
  * provided there is a dendrite allowing it
  *
  * @author Padraig Gleeson
  * @version 1.0.3
  *
  */

public class AxonalConnRegion
{
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

/*
    public Vector<SynapticProperties> getSynapticPropsList()
    {
        return synapticPropList;
    }


    public void setSynapticPropsList(Vector<SynapticProperties> synPropList)
    {
        this.synapticPropList = synPropList;
    }
*/

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
