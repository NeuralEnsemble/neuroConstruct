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

import javax.vecmath.*;
import ucl.physiol.neuroconstruct.cell.utils.*;

 /**
  * A simple cell for test purposes. Note: no longer included as 
  * example under Add New Cell Type...
  *
  * @author Padraig Gleeson
  *  
  *
  */


@SuppressWarnings("serial")

public class ComplexCell extends Cell
{
    Segment somaStartSection = null;
    Segment somaFinishSection = null;

    public ComplexCell()
    {
        this("ComplexCell");
    }


    public ComplexCell(String instanceName)
    {

        somaStartSection = addFirstSomaSegment(2f, 3f, "Soma_0", new Point3f(0, 10, 0), new Point3f(0, 11, 0), new Section("Soma"));

        Segment soma2 = addSomaSegment(4, "Soma_1", new Point3f(0, 13f, 0) , somaStartSection, somaStartSection.getSection());
        Segment soma3 = addSomaSegment(4.5f, "Soma_2", new Point3f(0, 15f, 0) , soma2, soma2.getSection());
        Segment soma4 = addSomaSegment(4, "Soma_3", new Point3f(0, 17f, 0) , soma3, soma3.getSection());
        somaFinishSection = addSomaSegment(3, "Soma_4", new Point3f(0, 18f, 0) , soma4, soma4.getSection());

        somaFinishSection.setSegmentId(10);


        this.createSections();


        setCellDescription("A cell for testing purposes only. Mainly used for testing non-valid morphologies");

        setInstanceName(instanceName);

    }


    private void createSections()
    {
        float dendriteRadius = 1f;

        Point3f posnEndPoint = new Point3f(0,-10,0);
/*
        Segment mainDend1 = addDendriticSegment(dendriteRadius,
                                                "mainDend1",
                                                posnEndPoint,
                                                somaStartSection,
                                                0,
                                                "dendSec1");

        mainDend1.getSection().setStartRadius(dendriteRadius);
        mainDend1.getSection().setStartPointPosition(new Point3f(0,-20,0));

*/
        String longSection1 = new String("longSection1");
        String longSection2 = new String("longSection2");

        posnEndPoint = new Point3f(10, 5, 0);

        Segment dendSeg1 = addDendriticSegment(dendriteRadius,
                                               "mainDend2",
                                               posnEndPoint,
                                               somaStartSection,
                                               0,
                                               longSection1, false);

        dendSeg1.getSection().setStartRadius(dendriteRadius*1.5f);

        posnEndPoint = new Point3f(10, -5, 0);

        addDendriticSegment(dendriteRadius*0.5f, "mainDend3", posnEndPoint, dendSeg1, 1,
                                               longSection1, false);

        posnEndPoint = new Point3f(-10, 0, 0);

        Segment dendSeg3 = addDendriticSegment(dendriteRadius, "mainDend4", posnEndPoint,
                                               somaStartSection, 0, longSection2, false);

        dendSeg3.getSection().setStartRadius(dendriteRadius);

        posnEndPoint = new Point3f(-15, -10, 0);

        addDendriticSegment(dendriteRadius, "mainDend5", posnEndPoint,
                                               dendSeg3, 1, dendSeg3.getSection().getSectionName(), false);



        posnEndPoint = new Point3f(0,20,0);

        Segment mainAxon = addAxonalSegment(dendriteRadius,
                                            "mainAxon",
                                            posnEndPoint,
                                            somaFinishSection,
                                            1,
                                            "axonSec1");

        mainAxon.getSection().setStartRadius(dendriteRadius);

        posnEndPoint = new Point3f(0,30,10);

        addAxonalSegment(dendriteRadius, "subAxon1", posnEndPoint, mainAxon, 1,
                                            "axonSec3");

        posnEndPoint = new Point3f(0,30,-10);

        addAxonalSegment(dendriteRadius, "subAxon2", posnEndPoint, mainAxon, 1,
                                            "axonSec4");
/*
        Segment disJointedDend1 = addDendriticSegment(1,
                                                     "disJointedDend1",
                                                     new Point3f(0, 0, 10),
                                                     mainDend1,
                                                     1,
                                                     "disJointedDends");

        disJointedDend1.getSection().setStartRadius(1);
        disJointedDend1.getSection().setStartPointPosition(new Point3f(0, 0, 0));

        Segment disJointedDend2 = addDendriticSegment(1,
                                                     "disJointedDend2",
                                                     new Point3f(0, 0, 20),
                                                     disJointedDend1,
                                                     1,
                                                     "disJointedDends");
*/




    }

    public static void main(String[] args)
    {
        ComplexCell cell = new ComplexCell("I'm simple...");


        System.out.println(CellTopologyHelper.printDetails(cell, null));

        //CellTopologyHelper.translateAllPositions(cell,  new Vector3f(0, -10, 0));

        //System.out.println(CellTopologyHelper.printDetails(cell));


       // CellTopologyHelper.moveSectionsToConnPointsOnParents(cell);

//
      //  System.out.println(CellTopologyHelper.printDetails(cell));




    }


}
