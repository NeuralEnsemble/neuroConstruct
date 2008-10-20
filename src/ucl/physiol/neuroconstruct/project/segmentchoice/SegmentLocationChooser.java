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


import ucl.physiol.neuroconstruct.cell.Cell;
import ucl.physiol.neuroconstruct.cell.SegmentLocation;
import ucl.physiol.neuroconstruct.utils.ClassLogger;


/**
 * Base class for all Segment Choosers. This can be extended to allow different subsets of
 * Segments to be selected. The primary purpose of this is to allow elec stims be put
 * on subsets of segments.
 *
 * @author Padraig Gleeson
 *  
 */


public abstract class SegmentLocationChooser
{
    static ClassLogger logger = new ClassLogger("SegmentChooser");

    Cell myCell = null;

    String description = null;


    private SegmentLocationChooser()
    {

    }

    public SegmentLocationChooser(String description)
    {
        this.description = description;
    }
    
    
    // to be overridden by implementing classes
    @Override
    public Object clone(){return null;};
    

    public String getDescription()
    {
        return description;
    };

    public void initialise(Cell cell)
    {
        this.myCell = cell;
        this.reinitialise();
    }

   
    /**
     * Gets the next chosen segment based on the settings in the sub class
     */
    public SegmentLocation getNextSegLoc() throws AllSegmentsChosenException, SegmentChooserException
    {
        if (myCell == null) throw new SegmentChooserException("SegmentChooser not yet initialised");

        return generateNextSegLoc();
    };
    
    public boolean isInitialised()
    {
        return (myCell != null);
    }

    protected abstract SegmentLocation generateNextSegLoc() throws AllSegmentsChosenException, SegmentChooserException;

    protected abstract void reinitialise();



    @Override
    public String toString()
    {

        return (toNiceString());
    }


    public abstract String toNiceString();




}

