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

package ucl.physiol.neuroconstruct.cell;
 

import java.io.Serializable;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
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
                        if (!otherParams.get(i).equals(extraParameters.get(i))) 
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
        return info.toString();
    }
    
    public String getExtraParamsDesc()
    {
        StringBuffer info = new StringBuffer();
           
        if (extraParameters!=null)
        {
            for (MechParameter mp: extraParameters)
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
        
        
        System.out.println("ChannelMechanism: "+ cm);
        
        //cell.associateGroupWithChanMech("all", cm);
        cell.associateGroupWithChanMech("soma_group", cm);
        //cell.associateGroupWithChanMech("dendrite_group", cm2);
        cell.associateGroupWithChanMech("all", cm2);
        
        
       // System.out.println(CellTopologyHelper.printDetails(cell, null));
        
        System.out.println("Cell chans: "+ cell.getChanMechsVsGroups());
        
        
/*
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
