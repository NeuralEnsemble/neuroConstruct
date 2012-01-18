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

package ucl.physiol.neuroconstruct.simulation;

import java.util.ArrayList;
import ucl.physiol.neuroconstruct.project.cellchoice.*;
import ucl.physiol.neuroconstruct.project.segmentchoice.IndividualSegments;
import ucl.physiol.neuroconstruct.project.segmentchoice.SegmentLocationChooser;
import ucl.physiol.neuroconstruct.project.stimulation.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Parameters for a stimulation to add to the simulation
 *
 * @author Padraig Gleeson
 *  
 */

public abstract class StimulationSettings
{
    private static ClassLogger logger = new ClassLogger("StimulationSettings");

    private String cellGroup;
    private String reference;

    private CellChooser cellChooser = new AllCells(); // a default...
    
    private SegmentLocationChooser segmentChooser = null;
    
    private float fractionAlong = 0.5f;


    public StimulationSettings()
    {
    }

    public StimulationSettings(String reference,
                               String cellGroup,
                               CellChooser cellChooser,
                               int segmentID)
    {
        this.reference = reference;
        this.cellGroup = cellGroup;
        this.cellChooser = cellChooser;
        //this.segmentID = segmentID;
        
        ArrayList<Integer> listOfSegmentIds = new ArrayList<Integer>();
        listOfSegmentIds.add(segmentID);
        this.segmentChooser = new IndividualSegments(listOfSegmentIds);
        
    }
    
    public StimulationSettings(String reference,
                               String cellGroup,
                               CellChooser cellChooser,
                               SegmentLocationChooser segs)
    {
        this.reference = reference;
        this.cellGroup = cellGroup;
        this.cellChooser = cellChooser;
        this.segmentChooser = segs;
        
    }
    
    @Override
    public abstract Object clone();

    @Override
    public abstract String toString();

    public String toLongString()
    {
        return toString() + " on: "+segmentChooser+" of: "+cellChooser+" in: "+cellGroup;
    };

    

    public abstract ElectricalInput getElectricalInput();

    public String getCellGroup()
    {
        return cellGroup;
    }



    public CellChooser getCellChooser()
    {
        //System.out.println("Cell chooser requested: "+ cellChooser + " in " + this.toString());
        return this.cellChooser;
    }


    public void setCellChooser(CellChooser cellChooser)
    {
        //System.out.println("Setting cell chooser: "+ cellChooser);
        this.cellChooser = cellChooser;
    }


    /**
     * Kept from an older version where a string was used to encode which cells to stimulate
     */
    public void setCellNumberString(String cellNumberString)
    {
        //System.out.println("Setting cellNumberString: "+ cellNumberString);
        if (cellNumberString.equals("*"))
        {
            this.cellChooser = new AllCells();

        }
        else if(cellNumberString.endsWith("%"))
        {
            String percent = cellNumberString.substring(0,cellNumberString.length()-1);
            try
            {
                Float percentage = Float.parseFloat(percent);
                this.cellChooser = new PercentageOfCells();
                cellChooser.setParameter(PercentageOfCells.PERCENTAGE_CELLS, percentage);


            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Error calculating the percentage of cells to stimulate from string: "
                                          +cellNumberString, ex, null);
            }
        }
        else
        {
            try
            {
                this.cellChooser = new IndividualCells();
                cellChooser.setParameter(IndividualCells.LIST_OF_CELLS, cellNumberString);


            }
            catch (Exception ex)
            {
                GuiUtils.showErrorMessage(logger,
                                          "Error calculating the cell to stimulate from: "
                                          +cellNumberString, ex, null);
            }
        }

            //System.out.println("New input cells: "+ cellChooser.toString());

    }





    public void setCellGroup(String cellGroup)
    {
        this.cellGroup = cellGroup;
    }
/*
    public int getSegmentID()
    {
        return segmentID;
    }*/
    public void setSegmentID(int segmentID)
    {
        //this.segmentID = segmentID;
        
        ArrayList<Integer> listOfSegmentIds = new ArrayList<Integer>();
        listOfSegmentIds.add(segmentID);
        this.segmentChooser = new IndividualSegments(listOfSegmentIds);
    }

    public SegmentLocationChooser getSegChooser()
    {
        if (segmentChooser==null)
        {
            ArrayList<Integer> listOfSegmentIds = new ArrayList<Integer>();
            listOfSegmentIds.add(0);          // default value
            this.segmentChooser = new IndividualSegments(listOfSegmentIds);
        }
        return segmentChooser;
    }

    public void setSegChooser(SegmentLocationChooser segChooser)
    {
        this.segmentChooser = segChooser;
    }
    
    
    
    public String getReference()
    {
        return reference;
    }
    public void setReference(String reference)
    {
        this.reference = reference;
    }
    public float getFractionAlong()
    {
        return fractionAlong;
    }
    public void setFractionAlong(float fractionAlong)
    {
        this.fractionAlong = fractionAlong;
    }

}
