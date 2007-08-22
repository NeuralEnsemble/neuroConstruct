/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.project;


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
             0.5f);

    }


    public SingleElectricalInput(String electricalInputType,
                                 String cellGroup,
                                 int cellNumber,
                                 int segmentId,
                                 float fractionAlong)
    {
        this.electricalInputType = electricalInputType;
        this.cellGroup = cellGroup;
        this.cellNumber = cellNumber;
        this.segmentId = segmentId;
        this.fractionAlong = fractionAlong;
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

    public String getElectricalInputType()
    {
        return this.electricalInputType;
    }




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
