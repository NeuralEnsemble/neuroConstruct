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


 /**
  *
  * A simple cell in the cerebellum, used for testing purposes. Note: no longer included as 
  * example under  Add New Cell Type...
  *
  * @author Padraig Gleeson
  *  
  *
  */


@SuppressWarnings("serial")

public class MossyFiber extends Cell
{

    Segment somaSection = null;

    public MossyFiber()
    {
        this("MossyFiber");
    }


    public MossyFiber(String instanceName)
    {
        somaSection = addSomaSegment(8, "Soma", null, null, new Section("Soma"));

    //    this.associateGroupWithChanMech(Section.SOMA_GROUP, "hh");
        setCellDescription("A simple cell for testing purposes only");
        setInstanceName(instanceName);

        this.createSections();

      //  this.associateGroupWithSynapse("rosette", "ExpSyn");
    }


    private void createSections()
    {
        float axonRadius = 1;

        float rosetteRadius = 5;//temp...


        Point3f posnEndPoint = new Point3f(5,50,0);
        Segment rootAxon = addAxonalSegment(axonRadius, "ax_root", posnEndPoint, somaSection, 1, "axonSec");


        Segment branch1 = addRelativeAxon(rootAxon, new Point3f(-5,50,0), axonRadius, "mainAxon");




        Segment rosette = addRelativeAxon(branch1, new Point3f(0, 20, 0),
            rosetteRadius, "Rosette");
        rosette.setFiniteVolume(true);

        rosette.getSection().setStartRadius(rosetteRadius);

        rosette.getSection().addToGroup("rosette");

        float dendriteDiam = 1f;

        posnEndPoint = new Point3f(2,-20,0);
        Segment rootDend = addDendriticSegment(dendriteDiam, "dend_root",posnEndPoint, somaSection, 0, "dendSec", false);

        Point3f posnEndPoint2 = new Point3f(-2,-20,0);
        addRelativeDendrite(rootDend, posnEndPoint2);

    }

    private Segment addRelativeDendrite(Segment parent, Point3f relPosition)
    {
        Point3f newPosition = (new Point3f(parent.getEndPointPosition()));
        newPosition.add(relPosition);

        float newRadius = parent.getRadius()*.6f;

        String newName = "Dend_"+ getOnlyDendriticSegments().size();

        Segment tempDend = addDendriticSegment(newRadius, newName,newPosition, parent, 1, "dendSec", true);

        return tempDend;
    }


    private Segment addRelativeAxon(Segment parent, Point3f relPosition, float radius, String sectionName)
    {
        Point3f newPosition = (new Point3f(parent.getEndPointPosition()));
        newPosition.add(relPosition);

        String newName = "Axon_"+ getOnlyAxonalSegments().size();

        Segment tempAxon = addAxonalSegment(radius, newName,newPosition, parent, 1, sectionName);

        return tempAxon;
    }


}
