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

package ucl.physiol.neuroconstruct.project.segmentchoice;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;
import ucl.physiol.neuroconstruct.cell.examples.SimpleCell;




/**
 * Class for selecting a list of individual segments
 *
 * @author Padraig Gleeson
 *  
 */

public class IndividualSegments extends SegmentLocationChooser
{
    ArrayList<Integer> listOfSegmentIds = new ArrayList<Integer>();
    
    float fractionAlong = 0.5f;
    
    int segIdsReturned = 0;
    
    public IndividualSegments()
    {
        super("Individual segments");
    }

    public ArrayList<Integer> getListOfSegmentIds()
    {
        return listOfSegmentIds;
    }

    public void setListOfSegmentIds(ArrayList<Integer> listOfSegmentIds)
    {
        this.listOfSegmentIds = listOfSegmentIds;
    }
    
    public IndividualSegments(ArrayList<Integer> listOfSegments)
    {
        super("Individual segments");
        this.listOfSegmentIds = listOfSegments;
    }

    public float getFractionAlong()
    {
        return fractionAlong;
    }

    public void setFractionAlong(float fractAlong)
    {
        this.fractionAlong = fractAlong;
        if (fractionAlong>1) 
            fractionAlong =1;
        if (fractionAlong<0) 
            fractionAlong =0;
    }
    
    
    
    @Override
    public Object clone()
    {
        IndividualSegments is = new IndividualSegments();
        ArrayList<Integer> listOfSegmentIds2 = new ArrayList<Integer>();
        for(int i: listOfSegmentIds)
            listOfSegmentIds2.add(i);
        is.setListOfSegmentIds(listOfSegmentIds2);
        
        return is;
    
    };
    
    

    @Override
    protected SegmentLocation generateNextSegLoc() throws AllSegmentsChosenException, SegmentChooserException
    {
        if (segIdsReturned>=listOfSegmentIds.size())
            throw new AllSegmentsChosenException();
        
        int toReturn = listOfSegmentIds.get(segIdsReturned);
        
        segIdsReturned++;
        SegmentLocation sl = new SegmentLocation(toReturn, this.fractionAlong);
        return sl;
    }

    @Override
    protected void reinitialise()
    {
        segIdsReturned = 0;
    }

    @Override
    public String toNiceString()
    {
        StringBuffer info = new StringBuffer("Segments: [");
        for(int i=0;i<listOfSegmentIds.size();i++)
        {
            info.append(listOfSegmentIds.get(i));
            if (i<listOfSegmentIds.size()-1)
                info.append(", "); 
        }
        info.append("], fract: "+ fractionAlong); 
        return info.toString();
    }

    
    
    public static void main(String[] args)
    {
        ArrayList<Integer> listOfSegments = new ArrayList<Integer>();
        listOfSegments.add(2);
        listOfSegments.add(3);
        
        
        Cell cell = new SimpleCell("TestCell");
        
        
        IndividualSegments chooser = new IndividualSegments(listOfSegments);
        
        chooser.initialise(cell);
        
        System.out.println("Segment chooser: "+ chooser);
        
        
        try
        {
            while (true)
            {
                System.out.println("Next Segment id: " + chooser.getNextSegLoc());

            }
        }
        catch (AllSegmentsChosenException ex)
        {
            System.out.println("All segments found!");
        }
        catch (SegmentChooserException ex)
        {
            Logger.getLogger(IndividualSegments.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    

}
