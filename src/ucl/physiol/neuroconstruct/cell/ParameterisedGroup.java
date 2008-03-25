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

import java.util.*;
import java.io.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.*;

 /**
  * A parameterisation of a group of sections, akin to the Parameterized 
  * Domain specification in NEURON
  *
  * @author Padraig Gleeson
  *  
  *
  */
public class ParameterisedGroup 
{
    public static boolean allowInhomogenousMechanisms = false;
    
    public enum Metric { 
        
        PATH_LENGTH_FROM_ROOT("Path length from root")/*,
        THREE_D_RADIAL_POSITION,
        THREE_D_PROJ_ONTO_LINE*/;
    
        private String info;
        
        Metric(String info)
        {
            this.info = info;
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
    
    private String name = null;

    private String group = null;
    
    private Metric metric = null;
    
    private ProximalPref proximalPref = null;

    private DistalPref distalPref = null;


    private ParameterisedGroup()
    {
    }    
    
    public ParameterisedGroup(String name, 
                              String group, 
                              Metric metric, 
                              ProximalPref proximalPref, 
                              DistalPref distalPref)
    {
        this.name = name;
        this.group = group;
        this.metric = metric;
        this.proximalPref = proximalPref;
        this.distalPref = distalPref;
    }
    
    
    public double evaluateAt(Cell cell, SegmentLocation location) throws ParameterException
    {
        Segment seg = cell.getSegmentWithId(location.getSegmentId());
        
        if (seg == null) 
            throw new ParameterException("The point: "+location+" is not on the cell: "+ cell );
        
        if (!seg.getSection().getGroups().contains(this.group))
            throw new ParameterException("The point: "+location+" on the cell: "+ cell +" is not a part of group of "+ this);
        
        ArrayList<Segment> segs = cell.getSegmentsInGroup(this.group);
        
        float segLen = CellTopologyHelper.getLengthFromRoot(cell, location);
        
        
        if (metric.equals(Metric.PATH_LENGTH_FROM_ROOT))
        {
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
        }
        
        
        
        //CellTopologyHelper.getOrderedSegmentsInSection(cell, section);
        
        return -1;
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
        return "ParameterisedGroup: "+name + " on: "+group+" with metric: "+ metric;
        
    }
    
    
    public static void main(String[] args) throws CloneNotSupportedException
    {
        try
        {
            SimpleCell cell = new SimpleCell("Testcell");
            
            
            String group1 = Section.ALL;
            String group2 = "Tip";
            cell.getSegmentWithId(3).getSection().addToGroup(group2);
            
            

            ParameterisedGroup pg = new ParameterisedGroup("OneToEnd", 
                                                           group2, 
                                                           Metric.PATH_LENGTH_FROM_ROOT, 
                                                           ProximalPref.MOST_PROX_AT_0, 
                                                           DistalPref.MOST_DIST_AT_1);

            cell.addParameterisedGroup(pg);
            
            System.out.println("Param group: " + pg);
            

            VariableMechanism cm = new VariableMechanism("cm");
            
            String expression1 = "A + B*(p)";


            Variable p = new Variable("p");
            Argument a = new Argument("A", 2);
            Argument b = new Argument("B", 1);

            ArrayList<Argument> expressionArgs1 = new ArrayList<Argument>();
            expressionArgs1.add(a);
            expressionArgs1.add(b);


            VariableParameter vp1 = new VariableParameter("cm", expression1, p, expressionArgs1);

            System.out.println("Var param: " + vp1+", at 0.5: "+ vp1.evaluateAt(0.5));
            
            cm.getParams().add(vp1);
            
            System.out.println("Var mech: " + cm);
            
            cell.associateParamGroupWithVarMech(pg, cm);
            
            Segment seg = cell.getSegmentsInGroup(group2).get(cell.getSegmentsInGroup(group2).size()-1);
            
            System.out.println("Checking seg: "+ seg);
            
            
            System.out.println("Param eval: "+ pg.evaluateAt(cell, new SegmentLocation(seg.getSegmentId(), 0.5f)));
            
            cell.associateGroupWithChanMech(Section.ALL, new ChannelMechanism("pas", 3.333f));
            
            System.out.println("Cell: "+ CellTopologyHelper.printDetails(cell, null, false, true, false));
            
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        
        
    }
    
}




