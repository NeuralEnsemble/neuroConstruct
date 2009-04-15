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

import javax.vecmath.*;
import ucl.physiol.neuroconstruct.utils.GeneralUtils;



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
    
    private int nodeId = NO_NODE_ID;

    public static final float NO_INIT_V = Float.MIN_VALUE;

    private float initV = NO_INIT_V;

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
        String initVInfo = "";
        if (initV!=NO_INIT_V)
            initVInfo = "; initial v: "+initV;

        if (nodeId == NO_NODE_ID) return "Cell: [" + cellNumber + "] (" + x_pos + ", " + y_pos + ", " + z_pos + ")"+initVInfo;
        return "Cell: [" + cellNumber + "] (" + x_pos + ", " + y_pos + ", " + z_pos + ")"+initVInfo+"; node id: "+nodeId;
    }
    
    
    public String toHtmlString()
    {
        String initVInfo = "";
        if (initV!=NO_INIT_V)
            initVInfo = "; initial v: "+initV;

        if (nodeId == NO_NODE_ID) return "Cell " + cellNumber + ": "+GeneralUtils.getBold("(" + x_pos + ", " + y_pos + ", " + z_pos + ")"+initVInfo, true);
        return "Cell: [" + cellNumber + "] "+GeneralUtils.getBold("(" + x_pos + ", " + y_pos + ", " + z_pos + ")"+initVInfo, true)+"; node id: "+GeneralUtils.getBold(nodeId, true) ;
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
    
    public int getNodeId()
    {
        return nodeId;
    }
    
    public void setNodeId(int nodeId)
    {
        this.nodeId = nodeId;
    }

    public float getInitV()
    {
        return initV;
    }

    public boolean hasUniqueInitV()
    {
        return initV!=NO_INIT_V;
    }

    public void setInitV(float initV)
    {
        this.initV = initV;
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

