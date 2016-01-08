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
 

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.CellTopologyHelper;
import ucl.physiol.neuroconstruct.mechanisms.CellMechanism;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.ProjectEventListener;
import ucl.physiol.neuroconstruct.project.ProjectManager;
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

public class ChannelMechanism implements Serializable, Comparable<ChannelMechanism>, IMechanism
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
    
    public int compareTo(ChannelMechanism otherMech)
    {
        return this.getName().compareTo(otherMech.getName());
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
    
    public String getNML2Name()
    {
        StringBuilder info = new StringBuilder(name);
           
        if (extraParameters!=null)
        {
            for (MechParameter mp: extraParameters)
            {
                if (!mp.isReversalPotential()) {
                    String val = mp.getValue()+"";
                    if ((int)mp.getValue() == mp.getValue())
                        val = (int)mp.getValue()+"";

                    val = GeneralUtils.replaceAllTokens(val, "-", "min"); // - allowed in genesis element name?
                    val = GeneralUtils.replaceAllTokens(val, ".", "_");

                    info.append("__"+ mp.getName()+""+ val);
                }
            }
        }
        return info.toString();
    }
    
    @Override
    public String getExtraParamsDesc()
    {
        StringBuilder info = new StringBuilder();
        
           
        if (extraParameters!=null)
        {
            ArrayList<MechParameter> mpa = (ArrayList<MechParameter>)GeneralUtils.reorderAlphabetically(extraParameters, true);
            
            for (MechParameter mp: mpa)
            {
                info.append(", ").append(mp.toString());
            }
        }
        return info.toString();
    }
    @Override
    public String getExtraParamsBracket()
    {
        StringBuilder info = new StringBuilder("(");
           
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
    
    @Override
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

    public float getDensity()
    {
        return density;
    }
    @Override
    public String getName()
    {
        return name;
    }
    public void setDensity(float density)
    {
        this.density = density;
    }
    @Override
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
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
        
    
    @Override
    public ArrayList<MechParameter> getExtraParameters()
    {
        if (extraParameters==null)
            extraParameters = new ArrayList<MechParameter>();
        return this.extraParameters;
    }
    
    @Override
    public void setExtraParameters(ArrayList<MechParameter> params)
    {
        this.extraParameters = params;
    }
    
    public boolean hasExtraParameters() 
    {
        return !this.extraParameters.isEmpty();
    }

    public static void main(String[] args)
    {
        try{
            File projFile = new File("osb/cerebral_cortex/networks/Thalamocortical/neuroConstruct/Thalamocortical.ncx");

            Project proj = Project.loadProject(projFile, new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};
                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {};

            });
            System.out.println("Project "+proj.getProjectFullFileName()+" loaded");
            
            String[] cells = new String[]{"DeepAxAx", "L23PyrFRB", "L23PyrRS_trim", "L5TuftedPyrRS", "nRT", "SupAxAx", "TCR", "DeepBasket", 
                "L23PyrFRB_varInit", "L4SpinyStellate", "L6NonTuftedPyrRS", "pyrFRB_orig", "SupBasket", 
                "DeepLTSInter", "L23PyrRS", "L5TuftedPyrIB", "nRT_minus75init", "SupLTSInter"};
            //cells = new String[]{"SupAxAx", "L23PyrFRB"};
            
            for (String cellname: cells) {
                Cell cell = proj.cellManager.getCell(cellname);
                //System.out.println(CellTopologyHelper.printDetails(cell, proj, false, false, false));
                System.out.println("----------------\nLooking at: "+cell);
                HashSet<String> cadGroups = new HashSet<String>();
                HashMap<String, ArrayList<MechParameter>> cmEp = new HashMap<String, ArrayList<MechParameter>>();
                for (ChannelMechanism cm: cell.getAllUniformChanMechs(false)) {
                    System.out.println("  Contains: "+cm+" on "+cell.getGroupsWithChanMech(cm));
                    CellMechanism cellm = proj.cellMechanismInfo.getCellMechanism(cm.getName());
                    if (cellm.isChannelMechanism() && cm.getDensity()<0) {
                        System.out.println("    Negative...");
                        cmEp.put(cm.getName(), cm.getExtraParameters());
                    } 
                    if (cm.getName().equals("cad"))
                        cadGroups.addAll(cell.getGroupsWithChanMech(cm));
                }
                System.out.println("cmEp: "+cmEp);
                for (ChannelMechanism cm: cell.getAllUniformChanMechs(false)) {
                    CellMechanism cellm = proj.cellMechanismInfo.getCellMechanism(cm.getName());
                    if (cmEp.containsKey(cm.getName())) {
                        System.out.println("    Changing: "+cm);
                        if (cm.getDensity()>0) {
                            cm.setExtraParameters(cmEp.get(cm.getName()));
                            System.out.println("    Now:      "+cm);
                        } else {
                            for (String grp: cell.getGroupsWithChanMech(cm)){
                                cell.dissociateGroupFromChanMech(grp, cm);
                                System.out.println("    Removed from group: "+grp);
                            }
                        }
                        //cmEp.put(cm.getName(), cm.getExtraParameters());
                    } 
                }
                
                //System.out.println(CellTopologyHelper.printDetails(cell, proj, false, false, false));
                
                System.out.println(cell.getIonPropertiesVsGroups());
                System.out.println(cadGroups);
                if (!cell.getIonPropertiesVsGroups().containsKey("ca") && !cadGroups.isEmpty()) {
                    for (String grp: cadGroups) {
                        IonProperties ip = new IonProperties("ca", 1.0E-20f,2.0E-18f);
                        cell.associateGroupWithIonProperties(grp, ip);
                    }
                }
                System.out.println(cell.getIonPropertiesVsGroups());
            }
            
            proj.markProjectAsEdited();
            proj.saveProject();
            System.out.println("Saved project");
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        


    }

}
