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

package ucl.physiol.neuroconstruct.utils.compartment;

import ucl.physiol.neuroconstruct.cell.*;


/**
 * A utility class for working out axial and membrane resistances, etc.
 * A lot of the maths taken from http://mathworld.wolfram.com/ConicalFrustum.html
 *
 * @author Padraig Gleeson
 *  
 */
public class SimpleCompartment
{
    //private ClassLogger logger = new ClassLogger("SimpleCompartment");

    private double startRadius = 0;
    private double endRadius = 0;
    private double height = 0;

    private SimpleCompartment()
    {

    }

    public SimpleCompartment(double startRadius,
                             double endRadius,
                             double height)
    {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.height = height;
    }


    public SimpleCompartment(Segment segment)
    {
        this.startRadius = segment.getSegmentStartRadius();
        this.endRadius = segment.getRadius();
        this.height = segment.getSegmentLength();
    }

    public SimpleCompartment(SimpleCompartment sc)
    {
        this.startRadius = sc.getStartRadius();
        this.endRadius = sc.getEndRadius();
        this.height = sc.getHeight();
    }

    public double getCurvedSurfaceArea()
    {
        if (startRadius==endRadius) return getStartCircumference()*height;

        double sum = (startRadius + endRadius);
        double min = (startRadius - endRadius);

        double root =Math.sqrt( (min * min) + (height * height));

        return (Math.PI * sum * root);
    }


    public double getVolume()
    {
        double radSums = (startRadius*startRadius)
               + (startRadius*endRadius)
               + (endRadius*endRadius);

        return (1d/3d) * Math.PI * height * (radSums);
    }



    public double getStartCircumference()
    {

        return 2 * Math.PI * startRadius;
    }

    public double getStartSurfArea()
    {

        return Math.PI * startRadius * startRadius;
    }


    public double getEndSurfArea()
    {

        return Math.PI * endRadius * endRadius;
    }



    public double getEndCircumference()
    {

        return 2 * Math.PI * endRadius;
    }

    public double getTotalSurfArea()
    {
        return getCurvedSurfaceArea()
            + getStartSurfArea()
            + getEndSurfArea();
    }

    public void scale(double scale)
    {
        startRadius = startRadius*scale;
        endRadius = endRadius*scale;
        height = height*scale;
    }



    @Override
    public String toString()
    {
        if (startRadius == endRadius)
        {
            return "SimpleCompartment [radius = "
                + startRadius
                + ", height= "
                + height + "]";

        }
        else
        {
            return "SimpleCompartment [startRadius = "
                + startRadius
                + ", endRadius = "
                + endRadius
                + ", height= "
                + height + "]";
        }
    }

    public static void main(String[] args)
    {
        //SimpleCompartment comp = new SimpleCompartment(1, 1, 1/Math.PI);
        //SimpleCompartment comp = new SimpleCompartment(5, 10, 10);
        SimpleCompartment comp = new SimpleCompartment(5,5,0);

        System.out.println("New comp: " + comp.toString());
        System.out.println("Curved surf area: " + comp.getCurvedSurfaceArea());
        System.out.println("Total surf area: " + comp.getTotalSurfArea());
        System.out.println("Volume: " + comp.getVolume());
        System.out.println("getStartCircumference: " + comp.getStartCircumference());
        System.out.println("getEndCircumference: " + comp.getEndCircumference());

        System.out.println("getStartSurfArea: " + comp.getStartSurfArea());
        System.out.println("getEndSurfArea: " + comp.getEndSurfArea());



    }
    public double getEndRadius()
    {
        return endRadius;
    }
    public double getHeight()
    {
        return height;
    }
    public double getStartRadius()
    {
        return startRadius;
    }
}
