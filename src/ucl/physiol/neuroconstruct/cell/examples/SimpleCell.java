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

import javax.vecmath.*;

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

public class SimpleCell extends Cell
{
    static ClassLogger logger = new ClassLogger("SimpleCell");

    Segment somaSection = null;

    public SimpleCell()
    {
        //this("SimpleCell");
    }


    public SimpleCell(String instanceName)
    {

        somaSection = addFirstSomaSegment(8,8, "Soma", null, null, new Section("Soma"));

        logger.logComment("Created soma: "+ somaSection);

/*

        try
        {
            PassiveMembraneProcess pas = new PassiveMembraneProcess();

            ChannelMechanism pasChan = new ChannelMechanism(pas.getDefaultInstanceName(),
                                                            pas.getParameter(pas.COND_DENSITY));

            this.associateGroupWithChanMech(Section.ALL, pasChan);

            NaChannelProcess na = new NaChannelProcess();

            ChannelMechanism naChan = new ChannelMechanism(na.getDefaultInstanceName(),
                                                            na.getParameter(pas.COND_DENSITY));

            this.associateGroupWithChanMech(Section.ALL, naChan);


            KChannelProcess k = new KChannelProcess();

            ChannelMechanism kChan = new ChannelMechanism(k.getDefaultInstanceName(),
                                                            k.getParameter(pas.COND_DENSITY));

            this.associateGroupWithChanMech(Section.ALL, kChan);

            Exp2SynProcess exp2 = new Exp2SynProcess();
            this.associateGroupWithSynapse(Section.ALL, exp2.getDefaultInstanceName());



        }
        catch (CellProcessException ex)
        {
        }
*/

        this.createSections();



        setCellDescription("A Simple cell for testing purposes");

        setInstanceName(instanceName);

    }


    private void createSections()
    {
        float dendriteRadius = 1f;
        float axonRadius = .5f;
/*
        // Dendrites...
        Point3f posnEndPoint = new Point3f(-10,-30,0);
        addDendriticSegment(dendriteRadius, "mainDend1", posnEndPoint, somaSection, 0,"mainDendSec1");

        posnEndPoint = new Point3f(10,-30,0);
        Segment dendSeg1 = addDendriticSegment(dendriteRadius, "mainDend2", posnEndPoint, somaSection, 0, "mainDendSec2");

        // Axons...
        posnEndPoint = new Point3f(0,20,0);
        Segment mainAxon = addAxonalSegment(dendriteRadius, "mainAxon", posnEndPoint, somaSection, 1,"mainAxonSec");

        posnEndPoint = new Point3f(0,30,10);
        addAxonalSegment(dendriteRadius/2f, "subAxon1", posnEndPoint, mainAxon, 1, "subAxonSec1");

        posnEndPoint = new Point3f(0,30,-10);
        addAxonalSegment(dendriteRadius/2f, "subAxon2", posnEndPoint, mainAxon, 1, "subAxonSec2");
*/

        Point3f posnEndPoint = new Point3f(20,0,0);
        
        Segment mainDendSeg = addDendriticSegment(dendriteRadius, 
                "mainDend", posnEndPoint, somaSection, 0,"mainDendSec", false);

        posnEndPoint = new Point3f(40,15, 0);
        addDendriticSegment(dendriteRadius, "subDend1", posnEndPoint, mainDendSeg, 1, "subDendSec1", true);

        posnEndPoint = new Point3f(45,0,00);
        addDendriticSegment(dendriteRadius, "subDend2", posnEndPoint, mainDendSeg, 1, "subDendSec2", true);

        posnEndPoint = new Point3f(40,-15,0);
        addDendriticSegment(dendriteRadius, "subDend3", posnEndPoint, mainDendSeg, 1, "subDendSec3", true);


        posnEndPoint = new Point3f(-30,0,0);
        addAxonalSegment(axonRadius, "mainAxon", posnEndPoint, somaSection, 1,"mainAxonSec");
/*
        posnEndPoint = new Point3f(0,30,10);
        addAxonalSegment(axonRadius, "subAxon1", posnEndPoint, mainAxon, 1, "subAxonSec1");

        posnEndPoint = new Point3f(0,30,-10);
        addAxonalSegment(axonRadius, "subAxon2", posnEndPoint, mainAxon, 1, "subAxonSec2");
*/



    }

    public static void main(String[] args)
    {
        SimpleCell cell = new SimpleCell("I'm simple...");

        //System.out.println(CellTopologyHelper.printShortDetails(cell));
       // System.out.println(CellTopologyHelper.printDetails(cell));

        String group = "newgroup";
        String chan = "newchan";

        cell.getFirstSomaSegment().getSection().addToGroup(group);
        cell.getFirstSomaSegment().getSection().addToGroup(group+"*");


           ChannelMechanism chMech = new ChannelMechanism( "ggg", 22);
           cell.associateGroupWithChanMech(group, chMech);
        cell.associateGroupWithSynapse(group+"*", chan+"_syn");



        System.out.println(CellTopologyHelper.printDetails(cell, null));

        cell.clone();

/*

        newCell.setInstanceName("gfghf");

        //newCell.disassociateGroupFromChanMech(group, chan);

        System.out.println("OLD:");
        System.out.println(CellTopologyHelper.printDetails(cell));

        System.out.println("NEW:");
        System.out.println(CellTopologyHelper.printDetails(newCell));
        }
       */
    }


}
