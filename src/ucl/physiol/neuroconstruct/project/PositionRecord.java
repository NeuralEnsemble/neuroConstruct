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

import javax.vecmath.*;



/**
 * Single position record
 *
 * @author Padraig Gleeson
 *  
 */


public class PositionRecord
{
    public int cellNumber;
    public float x_pos;
    public float y_pos;
    public float z_pos;
    
    /*
     * Used to signal which computational node the cell should execute on
     */
    public static final int NO_NODE_ID = -1;
    
    public int nodeId = NO_NODE_ID;

    public PositionRecord(int cellNumber, float x_pos, float y_pos, float z_pos)
    {
        this.cellNumber = cellNumber;
        this.x_pos = x_pos;
        this.y_pos = y_pos;
        this.z_pos = z_pos;
    }
    
    @Override
    public Object clone()
    {
        
        PositionRecord pr = new PositionRecord(this.cellNumber,
                                               this.x_pos,
                                               this.y_pos,
                                               this.z_pos);
        pr.nodeId = this.nodeId;
        
        return pr;
    }
    

    public Point3f getPoint()
    {
        return new Point3f(x_pos, y_pos, z_pos);
    }

    @Override
    public String toString()
    {
        if (nodeId == NO_NODE_ID) return "Cell: [" + cellNumber + "] (" + x_pos + ", " + y_pos + ", " + z_pos + ")";
        return "Cell: [" + cellNumber + "] (" + x_pos + ", " + y_pos + ", " + z_pos + ") Node id: "+nodeId;
    }

    /*
     * Note: no node id when reading in string form as this form is to support pre NetworkML storage of
     * locations
     */
    public PositionRecord(String stringForm)
    {
        int indexFirstSquareBracket = stringForm.indexOf("[");
        int indexFinalSquareBracket = stringForm.indexOf("]");
        int indexLeftBracket = stringForm.indexOf("(");
        int indexFirstComma = stringForm.indexOf(",");
        int indexSecondComma = stringForm.lastIndexOf(",");
        int indexRightBracket = stringForm.indexOf(")");

        cellNumber = Integer.parseInt(stringForm.substring(indexFirstSquareBracket + 1, indexFinalSquareBracket));
        x_pos = Float.parseFloat(stringForm.substring(indexLeftBracket + 1, indexFirstComma));
        y_pos = Float.parseFloat(stringForm.substring(indexFirstComma + 1, indexSecondComma));
        z_pos = Float.parseFloat(stringForm.substring(indexSecondComma + 1, indexRightBracket));

    }
    
    public static void main(String[] args)
    {
        PositionRecord pr = new PositionRecord(2,1.1f,2.2f,3.3f);
        
        System.out.println("pr: "+pr);
        PositionRecord pr2 = (PositionRecord)pr.clone();
        
        pr2.x_pos = 99;
        
        System.out.println("pr: "+pr);
        System.out.println("pr2: "+pr2);
        
    }
}

