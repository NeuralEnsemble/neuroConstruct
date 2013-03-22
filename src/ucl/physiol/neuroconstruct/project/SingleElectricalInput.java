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

package ucl.physiol.neuroconstruct.project;


import ucl.physiol.neuroconstruct.project.stimulation.InputInstanceProps;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;


/**
 * Spec of a single electrical input
 *
 * @author Padraig Gleeson
 *  
 */


public class SingleElectricalInput
{
    private String electricalInputType = null;
    private String cellGroup = null;
    private int cellNumber = -1;
    private int segmentId = -1;
    private float fractionAlong = 0.5f;
    
    private InputInstanceProps instanceProps = null;

    private SingleElectricalInput()
    {

    }

    public SingleElectricalInput(String electricalInputType,
                                 String cellGroup,
                                 int cellNumber)
    {
        this(electricalInputType,
             cellGroup,
             cellNumber,
             0,
             0.5f,
             null);

    }


    public SingleElectricalInput(String electricalInputType,
                                 String cellGroup,
                                 int cellNumber,
                                 int segmentId,
                                 float fractionAlong,
                                 InputInstanceProps instanceProps)
    {
        this.electricalInputType = electricalInputType;
        this.cellGroup = cellGroup;
        this.cellNumber = cellNumber;
        this.segmentId = segmentId;
        this.fractionAlong = fractionAlong;
        this.instanceProps = instanceProps;
    }

    public String getCellGroup()
    {
        return this.cellGroup;
    }

    public int getCellNumber()
    {
        return this.cellNumber;
    }

    public int getSegmentId()
    {
        return this.segmentId;
    }

    public float getFractionAlong()
    {
        return this.fractionAlong;
    }

    public InputInstanceProps getInstanceProps()
    {
        return instanceProps;
    }

    public void setInstanceProps(InputInstanceProps instanceProps)
    {
        this.instanceProps = instanceProps;
    }

    public String getElectricalInputType()
    {
        return this.electricalInputType;
    }




    @Override
    public String toString()
    {
        return "SingleElectricalInput: [Input: "
            + electricalInputType
            + ", cellGroup: "
            + cellGroup
            + ", cellNumber: "
            + cellNumber
            + ", segmentId: "
            + segmentId
            + ", fractionAlong: "
            + fractionAlong
            + "]";
    }
    
    
    public String details(boolean html)
    {
        return "Cell "+ GeneralUtils.getBold(cellNumber, html)+", seg: "+ GeneralUtils.getBold(segmentId, html)+", fraction: "+ GeneralUtils.getBold(fractionAlong, html);
    }

    public SingleElectricalInput(String stringForm)
    {
        electricalInputType = stringForm.substring(stringForm.indexOf("[Input: ") + 8,
                                                   stringForm.indexOf(","));

        cellGroup = stringForm.substring(stringForm.indexOf(", cellGroup: ") + 13,
                                         stringForm.indexOf(", cellNumber: "));

        cellNumber = Integer.parseInt(stringForm.substring(stringForm.indexOf(", cellNumber: ") + 14,
                                                           stringForm.indexOf(", segmentId: ")));

        segmentId = Integer.parseInt(stringForm.substring(stringForm.indexOf(", segmentId: ") + 13,
                                                          stringForm.indexOf(", fractionAlong: ")));

        fractionAlong = Float.parseFloat(stringForm.substring(stringForm.indexOf(", fractionAlong: ") + 17,
                                                              stringForm.indexOf("]")));
    }
    
    
    
    
    
}


