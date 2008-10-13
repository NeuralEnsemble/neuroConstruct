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

package ucl.physiol.neuroconstruct.project.segmentchoice;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.Section;
import ucl.physiol.neuroconstruct.cell.examples.SimpleCell;




/**
 * Class for selecting a certain number of segments on a group
 *
 * @author Padraig Gleeson
 *  
 */

public class GroupDistributedSegments extends SegmentChooser
{
    // Main parameters of the class
    String group = null;
    int numberOfSegments;
    
    // Array to store seg ids after they have been initialised
    ArrayList<Integer> generatedSegmentIds = new ArrayList<Integer>();
    
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
        this.numberOfSegments = number;
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
        return numberOfSegments;
    }

    public void setNumberOfSegments(int numberOfSegments)
    {
        this.numberOfSegments = numberOfSegments;
    }
    
    
    @Override
    public Object clone()
    {
        GroupDistributedSegments gds = new GroupDistributedSegments();
        gds.setNumberOfSegments(this.numberOfSegments);
        gds.setGroup(new String(group));
        return gds;
    }
    
    

    @Override
    protected int generateNextSegmentId() throws AllSegmentsChosenException, SegmentChooserException
    {
        if (segIdsReturned>=generatedSegmentIds.size())
            throw new AllSegmentsChosenException();
        
        int toReturn = generatedSegmentIds.get(segIdsReturned);
        
        segIdsReturned++;
        return toReturn;
    }

  

    @Override
    protected void reinitialise()
    {
        // TODO: implement this correctly...
        generatedSegmentIds.add(2);
        generatedSegmentIds.add(3);
        
        segIdsReturned = 0;
    }

    @Override
    public String toNiceString()
    {
        
        return numberOfSegments+ " segments on group: "+ group;
    }

    
    
    public static void main(String[] args)
    {
        ArrayList<Integer> listOfSegments = new ArrayList<Integer>();
        listOfSegments.add(2);
        listOfSegments.add(3);
        
        
        Cell cell = new SimpleCell("TestCell");
        
        String group = Section.DENDRITIC_GROUP;
        
        
        GroupDistributedSegments chooser = new GroupDistributedSegments(group, 7);
        
        chooser.initialise(cell);
        
        System.out.println("Segment chooser: "+ chooser);
        
        
        try
        {
            while (true)
            {
                System.out.println("Next Segment id: " + chooser.getNextSegmentId());

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
