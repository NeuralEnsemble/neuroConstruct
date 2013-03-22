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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.Segment;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;
import ucl.physiol.neuroconstruct.cell.examples.SimpleCell;
import ucl.physiol.neuroconstruct.project.*;




/**
 * Class for selecting a certain number of segments on a group
 *
 * @author Padraig Gleeson
 *  
 */

public class GroupDistributedSegments extends SegmentLocationChooser
{
    // Main parameters of the class
    String group = null;
    int numberOfPoints;
    
    // Array to store seg ids after they have been initialised
    ArrayList<Integer> generatedSegmentIds = new ArrayList<Integer>();
    
    // Array to store fraction along the choosed segments after they have been initialised
    ArrayList<Float> generatedFractionAlongChosenSegments = new ArrayList<Float>();
    
    // Counts the number of generated ids returned
    int segIdsReturned = 0;
    
    public GroupDistributedSegments()
    {
        super("A number of segments on a group");
    }
    public GroupDistributedSegments(String group, int number)
    {
        super("A number of segments on a group");
        this.group = group;
        this.numberOfPoints = number;
    }
    

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public int getNumberOfSegments()
    {
        return numberOfPoints;
    }

    public void setNumberOfSegments(int numberOfSegments)
    {
        this.numberOfPoints = numberOfSegments;
    }
    
    
    @Override
    public Object clone()
    {
        GroupDistributedSegments gds = new GroupDistributedSegments();
        gds.setNumberOfSegments(this.numberOfPoints);
        gds.setGroup(new String(group));
        return gds;
    }
    
    

    @Override
    protected SegmentLocation generateNextSegLoc() throws AllSegmentsChosenException, SegmentChooserException
    {
        if (segIdsReturned>=generatedSegmentIds.size())
            throw new AllSegmentsChosenException();
        
        SegmentLocation toReturn = new SegmentLocation(generatedSegmentIds.get(segIdsReturned), 
                generatedFractionAlongChosenSegments.get(segIdsReturned));

        segIdsReturned++;
        return toReturn;
    }

  

    @Override
    protected void reinitialise()
    {
        float totalLengthOfValidSegments = 0;
        Vector<Integer> idsOfPossibleSegments = new Vector<Integer>();
        Vector<Float> lensOfPossibleSegments = new Vector<Float>();
        Vector<Float> totalPositions = new Vector<Float>();
        
        Cell cell = this.myCell;
        Vector<Segment> allSegments = cell.getAllSegments();
        
        // store the ids and the lengths of all the segments in the group
        for (int i = 0; i < allSegments.size(); i++) 
        {
            Segment seg = allSegments.get(i);
            if (seg.getGroups().contains(group))
            {
                idsOfPossibleSegments.add(seg.getSegmentId());
                lensOfPossibleSegments.add(seg.getSegmentLength());
                totalLengthOfValidSegments = totalLengthOfValidSegments + seg.getSegmentLength();
            }
        }      
        
        /* generate the positions on the total length of the segments (in this way the probability is proportional to the length) */
        for (int i = 0; i < numberOfPoints; i++) 
        {
            totalPositions.add(ProjectManager.getRandomGenerator().nextFloat() * totalLengthOfValidSegments);
        }
        
        /* retrive the segments on wich the position is located */
        if (totalLengthOfValidSegments == 0 && idsOfPossibleSegments.size()==1)
        {
            logger.logComment("the only segment is the soma...");
            for (int i = 0; i < numberOfPoints; i++) 
            {
                generatedFractionAlongChosenSegments.add(0.5f);
                generatedSegmentIds.add(idsOfPossibleSegments.get(0));
            }
            return;
        }
        else
        {
            for (int i = 0; i < totalPositions.size(); i++) 
            {
                float distChecked = 0;
                int numSegmentsChecked = 0;
                boolean pointFound = false;
                while (!pointFound && numSegmentsChecked <= idsOfPossibleSegments.size())
                {
                    float length = lensOfPossibleSegments.get(numSegmentsChecked);
                    if ( (distChecked + length) > totalPositions.get(i))
                    {
                        pointFound = true;
                        generatedFractionAlongChosenSegments.add(((totalPositions.get(i)) - distChecked) / length);
                        generatedSegmentIds.add(idsOfPossibleSegments.get(numSegmentsChecked));
                    }
                    else
                    {
                        distChecked += length;
                        numSegmentsChecked++;
                    }
                }
            }
        
        }
    }

    @Override
    public String toNiceString()
    {
        
        return numberOfPoints+ " segment locations on group: "+ group;
    }

    
    
    public static void main(String[] args)
    {      
        
        Cell cell = new SimpleCell("TestCell");
        
        String group = Section.DENDRITIC_GROUP;
        
        
        GroupDistributedSegments chooser = new GroupDistributedSegments(group, 10);
        
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
            Logger.getLogger(GroupDistributedSegments.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    

}
