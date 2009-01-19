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
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;

 /**
  * A class representing a channel mechanism which can be added to
  *  sections
  *
  * @author Padraig Gleeson
  *  
  *
  */

@SuppressWarnings("serial")

public class ChannelMechanism implements Serializable
{
    static final long serialVersionUID = -1884757566565532L;
    
    private static transient ClassLogger logger = new ClassLogger("ChannelMechanism");
    
    private String name = null;
    private float density;
    
    private ArrayList<MechParameter> extraParameters = null;

    public ChannelMechanism()
    {
        extraParameters = new ArrayList<MechParameter>();
    }

    public ChannelMechanism(String name, float density)
    {
        extraParameters = new ArrayList<MechParameter>();
        this.name = name;
        this.density = density;
    }
    
    @Override
    public Object clone()
    {
        ChannelMechanism cm2 = new ChannelMechanism();
        cm2.setName(new String(name));
        cm2.setDensity(density);
        for (MechParameter mp: extraParameters)
        {
            cm2.getExtraParameters().add((MechParameter)mp.clone());
        }
        return cm2;
    }
    
    

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof ChannelMechanism)
        {
            ChannelMechanism other = (ChannelMechanism) otherObj;

            if (density == other.density && name.equals(other.name))
            {
                ArrayList<MechParameter> otherParams = other.getExtraParameters();
                if (extraParameters!=null)
                {
                    if (otherParams==null) 
                        return false;
                    
                    if (otherParams.size()!=extraParameters.size()) 
                        return false;

                    for (int i=0;i<extraParameters.size();i++)
                    {
                        if (!otherParams.contains(extraParameters.get(i))) 
                             return false;
                    }
                }
                else // extraParameters==null
                {
                    if (otherParams!=null) 
                        return false;
                }
                return true;
            }
        }
        return false;
    }
/*
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 73 * hash + Float.floatToIntBits(this.density);
        //hash = 73 * hash + (this.extraParameters != null ? this.extraParameters.hashCode() : 0);
        if(extraParameters != null)
        {
            for(MechParameter mp: extraParameters)
                hash = 73 * hash + mp.hashCode();
        }
        return hash;
    }*/


    @Override
    public String toString()
    {
        return name + " (density: " + density+" "
            +UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol()+
            getExtraParamsDesc()+")";
        
            
    }
    
    public String toNiceString()
    {
        return name + " (density: " + density+" "
            +UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+
            getExtraParamsDesc()+")";
        
            
    }
    
    
    public String getUniqueName()
    {
        StringBuffer info = new StringBuffer(name);
           
        if (extraParameters!=null)
        {
            for (MechParameter mp: extraParameters)
            {
                String val = mp.getValue()+"";
                if ((int)mp.getValue() == mp.getValue())
                    val = (int)mp.getValue()+"";
                
                val = GeneralUtils.replaceAllTokens(val+"", "-", "-"); // - allowed in genesis element name?
                val = GeneralUtils.replaceAllTokens(val, ".", "p");
                
                info.append("__"+ mp.getName()+"_"+ val);
            }
        }
        
        if (info.toString().length()>name.length()+12)
        {
            info = new StringBuffer(name);
            int tot = 0;
            if(extraParameters != null)
            {
                for(MechParameter mp: extraParameters)
                    tot+=mp.hashCode();
            }
                   info.append("__"+ tot);
            
        }
        
        return info.toString();
    }
    
    public String getExtraParamsDesc()
    {
        StringBuffer info = new StringBuffer();
        
           
        if (extraParameters!=null)
        {
            ArrayList<MechParameter> mpa = (ArrayList<MechParameter>)GeneralUtils.reorderAlphabetically(extraParameters, true);
            
            for (MechParameter mp: mpa)
            {
                info.append(", "+ mp.toString());
            }
        }
        return info.toString();
    }
    public String getExtraParamsBracket()
    {
        StringBuffer info = new StringBuffer("(");
           
        if (extraParameters!=null)
        {
            for (int i=0;i<extraParameters.size();i++)
            {
                if (i!=0) info.append(", ");
                info.append(extraParameters.get(i).toString());
            }
        }
        info.append(")");
        return info.toString();
    }
    
    public void setExtraParam(String name, float value)
    {       
        if (extraParameters==null)
            extraParameters = new ArrayList<MechParameter>();
        
        for (MechParameter mp: extraParameters)
        {
            if (mp.getName().equals(name))
            {
                logger.logComment("Updating current param: " + mp);
                mp.setValue(value);
                return;
            }
        } 
        // if not found
        MechParameter mp = new MechParameter(name, value);
        logger.logComment("Adding new param: " + mp);
        this.extraParameters.add(mp);
    }

    public static void main(String[] args) throws CloneNotSupportedException
    {
        Cell cell = new SimpleCell("hh");


        ChannelMechanism cm = new ChannelMechanism("na", 0);
        ChannelMechanism cm2 = new ChannelMechanism("na", 0);

        System.out.println("ChannelMechanism: "+ cm);
        
        MechParameter mp1 = new MechParameter("shift", -2.5f);
        MechParameter mp2 = new MechParameter("aaa", 111);
        cm.getExtraParameters().add(mp1);
        cm2.getExtraParameters().add(mp2);
        
        cm.getExtraParameters().add(new MechParameter("a", 0));
        cm.getExtraParameters().add(new MechParameter("b", 0));
        cm.getExtraParameters().add(new MechParameter("c", 0));
        cm.getExtraParameters().add(new MechParameter("d", 0));
        
        
        System.out.println("ChannelMechanism: "+ cm);
        System.out.println("ChannelMechanism: "+ cm.getUniqueName());
        
        
        System.out.println("ChannelMechanism: "+ cm.hashCode());
        
        
        ChannelMechanism cm3 = (ChannelMechanism)cm.clone();
        
        System.out.println("ChannelMechanism 3: "+ cm3);
        System.out.println("ChannelMechanism 3: "+ cm3.hashCode());
        
        
  /*      
        //cell.associateGroupWithChanMech("all", cm);
        cell.associateGroupWithChanMech("soma_group", cm);
        //cell.associateGroupWithChanMech("dendrite_group", cm2);
        cell.associateGroupWithChanMech("all", cm2);
        
        
       // System.out.println(CellTopologyHelper.printDetails(cell, null));
        
        System.out.println("Cell chans: "+ cell.getChanMechsVsGroups());
        
        

        ChannelMechanism cm2 = new ChannelMechanism(cm.toString());



        System.out.println("ChannelMechanism2: " + cm2);

        System.out.println("Equals: "+ cm.equals(cm2));
        System.out.println("Equals: "+ cm.equals(cm3));

        cell.associateGroupWithChanMech("hh", cm);
        c2.associateGroupWithChanMech("hh", cm3);

        System.out.println(CellTopologyHelper.compare(cell, c2));*/


    }
    public float getDensity()
    {
        return density;
    }
    public String getName()
    {
        return name;
    }
    public void setDensity(float density)
    {
        this.density = density;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    
    public MechParameter getExtraParameter(String paramName)
    {
        if (extraParameters==null || extraParameters.size()==0)
            return null;
        
        for (MechParameter mp: extraParameters)
        {
            if (mp.getName().equals(paramName))
            {
                return mp;
            }
        }
        return null;
    }
        
    
    public ArrayList<MechParameter> getExtraParameters()
    {
        if (extraParameters==null)
            extraParameters = new ArrayList<MechParameter>();
        return this.extraParameters;
    }
    
    public void setExtraParameters(ArrayList<MechParameter> params)
    {
        this.extraParameters = params;
    }


}
