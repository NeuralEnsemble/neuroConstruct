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
 
package ucl.physiol.neuroconstruct.cell.examples;


import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.utils.*;

 /**
  * A simple cell for test purposes.
  *
  * @author Padraig Gleeson
  *  
  *
  */


@SuppressWarnings("serial")

public class OneSegment extends Cell
{
    static ClassLogger logger = new ClassLogger("OneSegment");

    Segment somaSection = null;

    public OneSegment()
    {
        //this("SimpleCell");
    }


    public OneSegment(String instanceName)
    {

        somaSection = addFirstSomaSegment(10,10, "Soma", null, null, new Section("Soma"));

        logger.logComment("Created soma: "+ somaSection);





        setCellDescription("A single segment/compartment cell");

        setInstanceName(instanceName);

    }



    public static void main(String[] args)
    {
        OneSegment cell = new OneSegment("I'm simple...");

        String group = "newgroup";
        String chan = "newchan";

        cell.getFirstSomaSegment().getSection().addToGroup(group);
        cell.getFirstSomaSegment().getSection().addToGroup(group+"*");


           ChannelMechanism chMech = new ChannelMechanism( "ggg", 22);
           cell.associateGroupWithChanMech(group, chMech);
        cell.associateGroupWithSynapse(group+"*", chan+"_syn");



        System.out.println(CellTopologyHelper.printDetails(cell, null));

        Cell newCell = (Cell)cell.clone();
        
        System.out.println(CellTopologyHelper.printDetails(newCell, null));

    }


}
