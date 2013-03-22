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
import java.util.*;

import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

 /**
  * A parameterisation of a group of sections, based on the Parameterized 
  * Domain specification in NEURON
  *
  * @author Padraig Gleeson
  *  
  *
  */
public class ParameterisedGroup implements Serializable
{
    static final long serialVersionUID = -378992774734322L;
    
    public static boolean allowInhomogenousMechanisms = true;
    
    private String name = null;

    private String group = null;
    
    private Metric metric = null;
    
    private ProximalPref proximalPref = null;

    private DistalPref distalPref = null;

    public static final String DEFAULT_VARIABLE = "p";

    private String variable = DEFAULT_VARIABLE;
    
    
    
    private float cachedMinLen = -1;
    private float cachedMaxLen = -1;
    private long cacheTime = -1;
    private final float maxCacheTime = 2000; // ms
    
    public enum Metric implements Serializable { 
        
        PATH_LENGTH_FROM_ROOT("Path Length from root")/*,
        THREE_D_RADIAL_POSITION("3D radial distance from origin"),
        THREE_D_PROJ_ONTO_LINE*/;
    
        private String info = null;
        
        Metric()
        {
            
        }
        //static ArrayList<Metric> getAllMet
        
        Metric(String info)
        {
            this.info = info;
        }

        public static Metric getMetric(String m)
        {
            if (m.equals(PATH_LENGTH_FROM_ROOT.info))
                return PATH_LENGTH_FROM_ROOT;

            return null;
        }
        
        public String info()
        {
            return info;
        }
        
        
        @Override
        public String toString()
        {
            return info;
        }
        
    }
    
    
    public enum ProximalPref { NO_TRANSLATION,
                               MOST_PROX_AT_0/*,
                               ALL_PROX_ENDS_AT_0*/}
    
    public enum DistalPref { NO_NORMALISATION,
                               MOST_DIST_AT_1/*,
                               ALL_DIST_ENDS_AT_1*/}
    

    /* 
     * Needed for Serializable, shouldn't be used!...
     */
    public ParameterisedGroup()
    {
    }    
    
    public ParameterisedGroup(String name, 
                              String group, 
                              Metric metric, 
                              ProximalPref proximalPref, 
                              DistalPref distalPref,
                              String variable)
    {
        this.name = name;
        this.group = group;
        this.metric = metric;
        this.proximalPref = proximalPref;
        this.distalPref = distalPref;
        this.variable = variable;
    }

    
    /*
     * Returns the appropriate NEURON object (SubsetDomainIterator) with a constructor relevant for 
     * this instance's settings
     */
    public String getNeuronObject()
    {
        // todo: move these vals to enum...
        int metricRef = -1;
        if (metric.equals(Metric.PATH_LENGTH_FROM_ROOT)) 
            metricRef = 0;
        
        int proxRef = -1;
        if (proximalPref.equals(ProximalPref.NO_TRANSLATION))
            proxRef = 0;
        else if (proximalPref.equals(ProximalPref.MOST_PROX_AT_0))
            proxRef = 1;
        
        int distRef = -1;
        if (distalPref.equals(DistalPref.NO_NORMALISATION))
            distRef = 0;
        else if (distalPref.equals(DistalPref.MOST_DIST_AT_1))
            distRef = 1;
        
        return "SubsetDomainIterator("+group+", "+metricRef+", "+proxRef+", "+distRef+")";
    }
    
    
    public double evaluateAt(Cell cell, Segment seg, float fractAlong) throws ParameterException
    {
        //Segment seg = cell.getSegmentWithId(location.getSegmentId());
        
        if (seg == null) 
            throw new ParameterException("Null segment");
        
        if (!seg.getSection().getGroups().contains(this.group))
            throw new ParameterException("The segment: "+seg+" on the cell: "+ cell +" is not a part of group of "+ this);
               
        
        
        if (metric.equals(Metric.PATH_LENGTH_FROM_ROOT))
        {
            float segLen = CellTopologyHelper.getLengthFromRoot(cell, new SegmentLocation(seg.getSegmentId(), fractAlong));
            
            if (proximalPref.equals(ProximalPref.NO_TRANSLATION))
            {
                if (distalPref.equals(DistalPref.NO_NORMALISATION))
                {
                    //logger.
                    return segLen;
                }
                float maxLen = CellTopologyHelper.getMaxLengthFromRoot(cell, group);
                
                if(distalPref.equals(DistalPref.MOST_DIST_AT_1))
                {
                    return segLen/maxLen;
                }
            }
            else if (proximalPref.equals(ProximalPref.MOST_PROX_AT_0))
            {
                float minLen, maxLen;
//                
//                    //System.out.println(".. time .."+System.currentTimeMillis());
//                    System.out.println("..cachedMinLen.."+cachedMinLen+"..cachedMaxLen.."+cachedMaxLen);
//                    System.out.println("..maxCacheTime.."+maxCacheTime);
//                    System.out.println("..diff.."+(System.currentTimeMillis()-cacheTime));
//                    System.out.println("..use cache?.."+((System.currentTimeMillis()-cacheTime)<maxCacheTime));
//                
                if (cachedMinLen>=0 &&
                    cachedMaxLen>=0&&
                    ((System.currentTimeMillis()-cacheTime)<maxCacheTime) )
                {
                    maxLen = cachedMaxLen;
                    minLen = cachedMinLen;
                    //cacheTime = System.currentTimeMillis();
                    //System.out.println("..using cache.."+cacheTime);
                }
                else
                {
                    minLen = CellTopologyHelper.getMinLengthFromRoot(cell, group);
                    cachedMinLen = minLen;
                    maxLen = CellTopologyHelper.getMaxLengthFromRoot(cell, group);
                    cachedMaxLen = maxLen;
                    cacheTime = System.currentTimeMillis();
                    //System.out.println(".. refilling cache.."+cacheTime);
                }
                
                if (distalPref.equals(DistalPref.NO_NORMALISATION))
                {
                    return segLen - minLen;
                }
                
                if(distalPref.equals(DistalPref.MOST_DIST_AT_1))
                {
                    return (segLen - minLen)/(maxLen - minLen);
                }
                
            }
        }
        /*if (metric.equals(Metric.THREE_D_RADIAL_POSITION))
        {
            CellTopologyHelper.getAbsolutePosSegLoc(project, group, cellNum, location);
            
            if (proximalPref.equals(ProximalPref.NO_TRANSLATION))
            {
                if (distalPref.equals(DistalPref.NO_NORMALISATION))
                {
                    //logger.
                    return segLen;
                }
                float maxLen = CellTopologyHelper.getMaxLengthFromRoot(cell, group);
                
                if(distalPref.equals(DistalPref.MOST_DIST_AT_1))
                {
                    return segLen/maxLen;
                }
            }
            else if (proximalPref.equals(ProximalPref.MOST_PROX_AT_0))
            {
                float minLen = CellTopologyHelper.getMinLengthFromRoot(cell, group);
                
                if (distalPref.equals(DistalPref.NO_NORMALISATION))
                {
                    return segLen - minLen;
                }
                float maxLen = CellTopologyHelper.getMaxLengthFromRoot(cell, group);
                
                if(distalPref.equals(DistalPref.MOST_DIST_AT_1))
                {
                    return (segLen - minLen)/(maxLen - minLen);
                }
                
            }
        }*/
        
        
        
        //CellTopologyHelper.getOrderedSegmentsInSection(cell, section);
        
        return Double.NaN;
    }
    
    
    public double getMinValue(Cell cell) throws ParameterException
    {
        if (metric.equals(Metric.PATH_LENGTH_FROM_ROOT))
        {
            if (proximalPref.equals(ProximalPref.NO_TRANSLATION))
            {
                float minLen = CellTopologyHelper.getMinLengthFromRoot(cell, group);
                return minLen;
            }
            else if (proximalPref.equals(ProximalPref.MOST_PROX_AT_0))
            {
                return 0;                
            }
        }
        return Double.NaN;
    }
    
    public double getMaxValue(Cell cell) throws ParameterException
    {
        if (metric.equals(Metric.PATH_LENGTH_FROM_ROOT))
        {
            if (distalPref.equals(DistalPref.NO_NORMALISATION))
            {
                float maxLen = CellTopologyHelper.getMaxLengthFromRoot(cell, group);
                return maxLen;
            }

            if(distalPref.equals(DistalPref.MOST_DIST_AT_1))
            {
                return 1;
            }
        }
        return Double.NaN;
    }
    

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }


    public String getVariable()
    {
        if (variable==null || variable.equals("null"))
            variable = DEFAULT_VARIABLE;
        return variable;
    }

    public void setVariable(String variable)
    {
        this.variable = variable;
    }

    
    public Metric getMetric()
    {
        return metric;
    }

    public void setMetric(Metric metric)
    {
        this.metric = metric;
    }
    
    
    public DistalPref getDistalPref()
    {
        return distalPref;
    }

    public void setDistalPref(DistalPref distalPref)
    {
        this.distalPref = distalPref;
    }

    public ProximalPref getProximalPref()
    {
        return proximalPref;
    }

    public void setProximalPref(ProximalPref proximalPref)
    {
        this.proximalPref = proximalPref;
    }
    
    
    @Override
    public String toString()
    {
        return "ParameterisedGroup: "+name + " on: "+group+" with metric: "+ metric+" ("+proximalPref+", "+distalPref+"), var: "+getVariable();
        
    }
    
    public String toShortString()
    {
        return name + " ("+group+")";
        
    }
    
    @Override
    public Object clone()
    {
        ParameterisedGroup pg2 = new ParameterisedGroup(new String(name),new String(group), metric, 
            proximalPref, distalPref, new String(getVariable()));
        
        
        
        return pg2;
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
        final ParameterisedGroup other = (ParameterisedGroup) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
        {
            return false;
        }
        if (this.group != other.group && (this.group == null || !this.group.equals(other.group)))
        {
            return false;
        }
        if (this.metric != other.metric)
        {
            return false;
        }
        if (this.proximalPref != other.proximalPref)
        {
            return false;
        }
        if (this.distalPref != other.distalPref)
        {
            return false;
        }
        if (this.variable != other.variable && (this.variable == null || !this.variable.equals(other.variable)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.group != null ? this.group.hashCode() : 0);
        hash = 53 * hash + (this.metric != null ? this.metric.hashCode() : 0);
        hash = 53 * hash + (this.proximalPref != null ? this.proximalPref.hashCode() : 0);
        hash = 53 * hash + (this.distalPref != null ? this.distalPref.hashCode() : 0);
        hash = 53 * hash + (this.variable != null ? this.variable.hashCode() : 0);
        return hash;
    }
    
    
    
    
    public static void main(String[] args) throws CloneNotSupportedException
    {
        try
        {
            Cell cell = new SimpleCell("Testcell");
            
            String group2 = "all";
            cell.getSegmentWithId(3).getSection().addToGroup(group2);
            

            Variable p = new Variable("p");

            ParameterisedGroup pg = new ParameterisedGroup("OneToEnd", 
                                                           group2, 
                                                           Metric.PATH_LENGTH_FROM_ROOT, 
                                                           ProximalPref.MOST_PROX_AT_0, 
                                                           DistalPref.MOST_DIST_AT_1,
                                                           p.getName());

            cell.addParameterisedGroup(pg);
            
            System.out.println("Param group: " + pg);
            

            
            String expression1 = "A + B*(p)";


            Argument a = new Argument("A", 2);
            Argument b = new Argument("B", 1);

            ArrayList<Argument> expressionArgs1 = new ArrayList<Argument>();
            expressionArgs1.add(a);
            expressionArgs1.add(b);


            VariableParameter vp1 = new VariableParameter("cm", expression1, p, expressionArgs1);

            System.out.println("Var param: " + vp1+", at 0.5: "+ vp1.evaluateAt(0.5));
            
            
            VariableMechanism cm = new VariableMechanism("cm", vp1);
            
            
            System.out.println("Var mech: " + cm);
            
            cell.associateParamGroupWithVarMech(pg, cm);
            
            Segment sega = cell.getSegmentsInGroup(group2).get(cell.getSegmentsInGroup(group2).size()-1);
            
            System.out.println("Checking seg: "+ sega);
            
            
            System.out.println("Param eval: "+ pg.evaluateAt(cell, sega, 0.5f));
            
            cell.associateGroupWithChanMech(Section.ALL, new ChannelMechanism("pas", 3.333f));
            
            //System.out.println("Cell: "+ CellTopologyHelper.printDetails(cell, null, false, true, false));
            
            ProjectManager pm = new ProjectManager();
            File projFile = new File("testProjects/TestDetailedMorphs/TestDetailedMorphs.neuro.xml");
        
        
            pm.loadProject(projFile);
      
            Project proj = pm.getCurrentProject();
            
            cell = proj.cellManager.getCell("SampleCell");
            
            long start = System.currentTimeMillis();
            Random r = new Random();
            
            System.out.println("Have cell with: "+ cell.getAllSegments().size()+" segs");
            
            pg = cell.getParameterisedGroups().get(0);
            
            System.out.println("pg: "+ pg);
            
            for(Segment seg: cell.getAllSegments())
            {
                pg.evaluateAt(cell, seg, r.nextFloat());
                //CellTopologyHelper.getLengthFromRoot(cell, sl);
                
            }
            long stop = System.currentTimeMillis();
            System.out.println("Completed in "+(stop-start)+" milli");
            
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        
        
    }
    
}




