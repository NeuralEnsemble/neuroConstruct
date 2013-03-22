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

import ucl.physiol.neuroconstruct.utils.*;


/**
 * A utility class for working out axial and membrane resistances, etc.
 * A lot of the maths taken from http://mathworld.wolfram.com/ConicalFrustum.html
 *
 * @author Padraig Gleeson
 *  
 */

public class CompartmentHelper
{
    private static ClassLogger logger = new ClassLogger("CompartmentHelper");

    public CompartmentHelper()
    {

    }


    /**
     * Gets the radius of a cylinder which has the same surface area and height as a
     * conical frustum with these coords
     * @param startRadius radius at the start
     * @param endRadius radius at the end
     * @param height height of the conical frustum
     * @return the equivalent radius
     */
    public static double getEquivalentRadius(double startRadius,
                                            double endRadius,
                                            double height)
    {
        //logger.logComment("Getting equiv cyl for startRadius: " + startRadius+", endRadius: "+ endRadius+", height: "+height);
        if (startRadius == endRadius) return endRadius;

        SimpleCompartment comp = new SimpleCompartment(startRadius,
                                   endRadius,
                                  height);



        return comp.getCurvedSurfaceArea()/(2 * Math.PI * height);

    }


    public static SimpleCompartment getCylWithSameArea(SimpleCompartment[] multiComp)
    {
        if (multiComp.length==1) return getCylWithSameArea(multiComp[0]);

        double totalCArea = getTotalCurvedSurfArea(multiComp);

        double totalLength = getLength(multiComp);

        double circumf = totalCArea / totalLength;

        double rad = circumf / (2*Math.PI);

        return new SimpleCompartment(rad, rad, totalLength);
    }


    /**
     * Gets 2 cylinders with the same length, which have the same total area and the same TOTAL
     * axial resistance. Assumes same specAxRes in both cylinders
     */
    public static SimpleCompartment[] getDoubleCylinder(SimpleCompartment[] multiComp)
    {
        int numCylinders = 2;
        double totalLength = getLength(multiComp);
        double newCylLength = totalLength/numCylinders;

        SimpleCompartment[] cyls = new SimpleCompartment[numCylinders];

        boolean allRadiiEqual = true;
        double startRad = multiComp[0].getStartRadius();

        for (int i = 0; i < multiComp.length; i++)
        {
            if (!(multiComp[i].getStartRadius()==startRad  &&
                multiComp[i].getEndRadius()==startRad))
          {
              allRadiiEqual = false;
          }
        }
        if (allRadiiEqual)
        {
            cyls[0] = new SimpleCompartment(startRad,startRad,newCylLength);
            cyls[1] = new SimpleCompartment(startRad,startRad,newCylLength);

            return cyls;
        }

        double dummyAxRes = 1; // dummy as the absolute val is unimportant...

        double totCSAorig = getTotalCurvedSurfArea(multiComp);
        double totAxResOrig = getTotalAxialRes(multiComp, dummyAxRes);

        // know if rA and rB are the two new radii:
        //
        // totCSAorig = 2 * pi * newCylLength * (rA + rB)
        //
        // totAxResOrig = (dummyAxRes * newCylLength / pi) * (1/rA^2 + 1/rB^2)

        float S = (float)(totCSAorig / (2*Math.PI*newCylLength) ); // = rA + rB
        float Q = (float) ( (Math.PI * totAxResOrig) / (dummyAxRes*newCylLength)); // = 1/rA^2 + 1/rB^2

        //float S = 10;
        //float Q = 0.1f;

        // solving for these two gives:
        // rA^4 + a*rA^3 + b*rA^2 + c*rA + d = 0
        // where
        // a = -2S, b = S^2 + 2/Q, c = 2/Q, d = S^2/Q

        float a = -2*S;
        float b = S*S - (2f/Q);
        float c = 2*S/Q;
        float d = -1*S*S/Q;

        //System.out.println("s: "+S+", Q: "+Q);

        float[] roots = getRoots(a, b, c, d);
        float smallerRad = -1;
        float largerRad = -1;

        for (int i = 0; i < roots.length; i++)
        {
            float r = roots[i];
            //System.out.println("Root " + i + ": " + r);
            if (r>0 && r<S)
            {
                if (largerRad<0) largerRad = r;
                else if (smallerRad<0) smallerRad = r;
            }

        }
        if (smallerRad>largerRad)
        {
            smallerRad = largerRad;
            largerRad = smallerRad;
        }
        float firstRad = largerRad;
        float secondRad = smallerRad;

        //System.out.println("Larger rad: "+ largerRad+", smaller rad: "+ smallerRad);

        double firstHalfAxRes = getFractionalAxialRes(multiComp, 0.5, dummyAxRes);

        if (firstHalfAxRes > (totAxResOrig-firstHalfAxRes))
        {
            firstRad = smallerRad;
            secondRad = largerRad;
        }

        cyls[0] = new SimpleCompartment(firstRad,firstRad,newCylLength);
        cyls[1] = new SimpleCompartment(secondRad,secondRad,newCylLength);

/*
        for (int i = 0; i < cyls.length; i++)
        {

            double surfArea = getFractionalSurfArea(multiComp, ((i+1)*fraction)) -
                getFractionalSurfArea(multiComp, (i*fraction)) ;

            double radius = surfArea/(2 * Math.PI * newCylLength);

            System.out.println("Fract Sa: "+ surfArea);


            cyls[i] = new SimpleCompartment(radius,radius,newCylLength);
        }*/

        return cyls;
    }



    /**
     * Returns a cylinder (startRadius=endRadius) <b>with the same height</b>, and the same curved
     * surface area
     */
    public static SimpleCompartment getCylWithSameArea(SimpleCompartment frustum)
    {
        //logger.logComment("Old curved area: "+ frustum.getCurvedSurfaceArea());

        double newRadius = getEquivalentRadius(frustum.getStartRadius(),
                                            frustum.getEndRadius(),
                                            frustum.getHeight());

        SimpleCompartment comp = new SimpleCompartment(newRadius,
                                                       newRadius,
                                                       frustum.getHeight());

        logger.logComment("New curved area: "+ comp.getCurvedSurfaceArea());

        return comp;

    }


    public static double getTotalAxialRes(SimpleCompartment[] multiComp,
                                          double specRA)
    {
        return getFractionalAxialRes(multiComp, 1, specRA);
    }


    public static double getTotalAxialRes(SimpleCompartment comp,
                                            double specRA)
    {

        if (comp.getStartRadius()==comp.getEndRadius())
        {
            return specRA * comp.getHeight() /
                (Math.PI * comp.getStartRadius() * comp.getStartRadius());
        }

        double r1 = comp.getStartRadius();
        double r2 = comp.getEndRadius();

        // There may well be a simpler way for expressing this...

        double recip1 = 1/(r1*r2 - (r2*r2));
        double recip2 = 1/(r1*r2 - (r1*r1));

        double factor = specRA * comp.getHeight() / (Math.PI);

        return  factor * (recip1 + recip2);

    }


    public static double getTotalMembraneRes(SimpleCompartment comp,
                                             double specMA)
    {

        return specMA / comp.getCurvedSurfaceArea();

    }




    public static double getTotalCurvedSurfArea(SimpleCompartment[] multiComp)
    {
        double total = 0;
        for (int i = 0; i < multiComp.length; i++)
        {
            SimpleCompartment nextComp = multiComp[i];
            logger.logComment("Curv Surf area of inner frustum "+nextComp+": "+ nextComp.getCurvedSurfaceArea());
            total = total + nextComp.getCurvedSurfaceArea();
        }
        return total;
    }


    public static double getTotalVolume(SimpleCompartment[] multiComp)
    {
        double total = 0;
        for (int i = 0; i < multiComp.length; i++)
        {
            SimpleCompartment nextComp = multiComp[i];
            logger.logComment("Volume of inner frustum "+nextComp+": "+ nextComp.getVolume());
            total = total + nextComp.getVolume();
        }
        return total;
    }

    /**
     * Get total length of a compound compartment.
     */
    public static double getLength(SimpleCompartment[] multiComp)
    {

        double totalLength = 0;
        for (int i = 0; i < multiComp.length; i++)
        {
            SimpleCompartment nextComp = multiComp[i];
            totalLength = totalLength + nextComp.getHeight();
        }
        return totalLength;
    }


    /**
     * Get volume of compound frustum truncated at dist (fractionFromStart * total height)
     * from start point, cut in plane parallel to start face.
     */
    public static double getFractionalVolume(SimpleCompartment[] multiComp, double fractionFromStart)
    {
        if (fractionFromStart < 0) fractionFromStart = 0;
        if (fractionFromStart>1) fractionFromStart =1;

        double totalLength = getLength(multiComp);

        double totalVol = 0;
        double lengthTraversed = 0;

        for (int i = 0; i < multiComp.length; i++)
        {
            SimpleCompartment nextComp = multiComp[i];

            if ((lengthTraversed+nextComp.getHeight()) <=
                (totalLength*fractionFromStart))
            {
                logger.logComment("Volume of inner frustum " + nextComp + ": " + nextComp.getVolume());
                totalVol = totalVol + nextComp.getVolume();
                lengthTraversed = lengthTraversed +  nextComp.getHeight();
            }
            else if (lengthTraversed<totalLength)
            {
                double remainder = (totalLength*fractionFromStart) - lengthTraversed;
                double newFruStartRad = nextComp.getStartRadius();
                double newFruEndRad = nextComp.getStartRadius()
                    + ((nextComp.getEndRadius() - nextComp.getStartRadius())*
                       (remainder/nextComp.getHeight()));
                double newFruHeight = remainder;

                SimpleCompartment partialComp = new SimpleCompartment(newFruStartRad,
                                                                      newFruEndRad,
                                                                      newFruHeight);
                totalVol = totalVol + partialComp.getVolume();

                lengthTraversed = totalLength;
            }
        }
        return  totalVol;
    }


    /**
     * Get curved surface area of compound frustum truncated at dist (fractionFromStart * total height)
     * from start point, cut in plane perpendicular to axis of truncated frustum.
     */
    public static double getFractionalSurfArea(SimpleCompartment[] multiComp, double fractionFromStart)
    {
        if (fractionFromStart<0) fractionFromStart =0;
        if (fractionFromStart>1) fractionFromStart =1;

        double totalLength = getLength(multiComp);

        double totalSurfArea = 0;
        double lengthTraversed = 0;

        double fractLength = totalLength*fractionFromStart;

        logger.logComment("Need to get surf area dist "+ fractLength + " along "+totalLength);

        for (int i = 0; i < multiComp.length; i++)
        {
            logger.logComment("Looking at comp: "+i+", lengthTraversed: "+lengthTraversed);
            SimpleCompartment nextComp = multiComp[i];

            if ((lengthTraversed+nextComp.getHeight()) <= fractLength)
            {
                logger.logComment("Surf area of inner frustum " + nextComp + ": " + nextComp.getCurvedSurfaceArea());
                totalSurfArea = totalSurfArea + nextComp.getCurvedSurfaceArea();

                lengthTraversed = lengthTraversed +  nextComp.getHeight();
            }
            else if (lengthTraversed<totalLength)
            {
                logger.logComment("Found comp with fract point...");

                double remainder = fractLength - lengthTraversed;
                double newFruStartRad = nextComp.getStartRadius();
                double newFruEndRad = nextComp.getStartRadius()
                    + ((nextComp.getEndRadius() - nextComp.getStartRadius())*
                       (remainder/nextComp.getHeight()));

                double newFruHeight = remainder;

                SimpleCompartment partialComp = new SimpleCompartment(newFruStartRad,
                                                                      newFruEndRad,
                                                                      newFruHeight);

                totalSurfArea = totalSurfArea + partialComp.getCurvedSurfaceArea();

                lengthTraversed = totalLength;
            }
        }
        return  totalSurfArea;
    }





    /**
     * Get axial resistance up to a poit this fraction along
     */
    public static double getFractionalAxialRes(SimpleCompartment[] multiComp,
                                               double fractionFromStart,
                                               double specRa)
    {
        if (fractionFromStart<0) fractionFromStart =0;
        if (fractionFromStart>1) fractionFromStart =1;

        double totalLength = getLength(multiComp);

        double totalAxRes = 0;
        double lengthTraversed = 0;

        for (int i = 0; i < multiComp.length; i++)
        {
            SimpleCompartment nextComp = multiComp[i];

            if ((lengthTraversed+nextComp.getHeight()) <=
                (totalLength*fractionFromStart))
            {
                logger.logComment("Ax res of inner frustum " + nextComp + ": " + getTotalAxialRes(nextComp, specRa));
                totalAxRes = totalAxRes + getTotalAxialRes(nextComp, specRa);

                lengthTraversed = lengthTraversed +  nextComp.getHeight();
            }
            else if (lengthTraversed<totalLength)
            {
                double remainder = (totalLength*fractionFromStart) - lengthTraversed;
                double newFruStartRad = nextComp.getStartRadius();
                double newFruEndRad = nextComp.getStartRadius()
                    + ((nextComp.getEndRadius() - nextComp.getStartRadius())*
                       (remainder/nextComp.getHeight()));
                double newFruHeight = remainder;

                SimpleCompartment partialComp = new SimpleCompartment(newFruStartRad,
                                                                      newFruEndRad,
                                                                      newFruHeight);

                totalAxRes = totalAxRes + getTotalAxialRes(partialComp, specRa);

                lengthTraversed = totalLength;
            }
        }
        return  totalAxRes;
    }


    /**
     * Gets roots of the quartic equation:
     *
     *  x^4 + a*x^3 + b*x^2 + c*x + d = 0
     *
     */
    public static float[] getRoots(float a, float b, float c, float d)
    {
        float[] roots = new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN};

        logger.logComment("a: "+a+", b: "+b+", c: "+c+", d: "+d);
        logger.logComment("\n     Getting roots of equation: x^4 + "+a+"*x^3 + "+b+"*x^2 + "+c+"*x +"+d);

        /*
        // this method taken from  http://planetmath.org/encyclopedia/QuarticFormula.html didn't quite work...
        //My own groupings...
        float P = b*b - 3*a*c + 12*d;
        float Q = 2*b*b*b - 9*a*b*c + 27*c*c + 27*a*a*d - 72*b*d;
        float S = (float) Math.sqrt((-4f*P*P*P) + (Q*Q));

        float R = (float) ( Math.cbrt(2) * P / (3 * Math.cbrt(Q+S)));
        float T = (float)  Math.cbrt((Q+S)/54);
        float U = a*a/4f - 2f*b/3f;

        float V = -1*a*a*a + 4*a*b - 8*c;

        float W = (float)Math.sqrt(U+R+T);

        float Xp = 2*U - R - T + (V/(4*W));
        float Xm = 2*U - R - T - (V/(4*W));


        System.out.println("P: "+P+" Q: "+Q+" S: "+S+" R: "+R+" T: "+T+" U: "+U+" V: "+V+" W: "+W+" Xp: "+Xp+" Xm: "+Xm);

        roots[0] = (-1 * a / 4f) - (0.5f * W) - (float)(0.5 * Math.sqrt(Xm));
        roots[1] = (-1 * a / 4f) - (0.5f * W) + (float)(0.5 * Math.sqrt(Xm));
        roots[2] = (-1 * a / 4f) + (0.5f * W) - (float)(0.5 * Math.sqrt(Xp));
        roots[3] = (-1 * a / 4f) + (0.5f * W) + (float)(0.5 * Math.sqrt(Xp));
         */

        // Method taken from http://www.1728.com/quartic2.htm, so changing to their notation...
        float e = d;
        d = c;
        c = b;
        b = a;
        a = 1f;

        float f = c - (3*b*b/8f);

        float g = d + (b*b*b / 8f) - (b*c/2f);

        float h = e - (3*b*b*b*b/256f) + (b*b * c/16f) - ( b*d/4f);

        logger.logComment("f: "+f+ ", g: "+g+", h: "+h);

        logger.logComment("Now need to solve cubic eqn:  Y^3 + (f/2)*Y^2 + ((f^2 -4*h)/16)*Y -g^2/64 = 0");

        float ay = 1;
        float by = f/2f;
        float cy = (f*f - 4*h)/16f;
        float dy = -1*g*g/64f;

        //float ay = 2;
        //float by = -4;
        //float cy = -22;
        //float dy = 24;

        // 3x^3   - 10x^2   + 14x + 27 = 0
   /*     float ay = 3;
        float by = -10;
        float cy = 14;
        float dy = 27;*/

   //
   /*
        float ay = 1;
        float by = 6;
        float cy = 12;
        float dy = 8;*/



        logger.logComment("or:  "+ay+"*Y^3 + "+by+"*Y^2 + "+cy+"*Y + "+dy+" = 0");

        // solve the cubic...

        float fcub = ( (3*cy/ay) - (by*by/(ay*ay)) ) / 3f;
        float gcub = ( (2*by*by*by/(ay*ay*ay)) - (9*by*cy/(ay*ay)) + (27*dy/ay)) / 27f;
        float hcub =  (gcub*gcub/4f) + (fcub*fcub*fcub/27f);

        logger.logComment("fcub: "+fcub+ ", gcub: "+gcub+ ", hcub: "+hcub);

        float y1=Float.NaN, y2=Float.NaN, y3=Float.NaN;

        /** @todo Improve... */
        // This is a bit of a hack, but the float approximation usually results in v. small hcub when itshould be zero...
        //if (hcub>0 && hcub < 1e-12) hcub = 0;

        if (hcub<0)
        {
            logger.logComment("All 3 roots are real!!");

            float i = (float)Math.sqrt((gcub*gcub/4f) - hcub);
            float j = (float)Math.cbrt(i);
            float arg = -1* (gcub / (2f*i));
            float k = (float)Math.acos(arg);

            logger.logComment("arg: "+arg+ ", k: "+k+ ", i: "+i);

            float L = -1*j;
            float M = (float)Math.cos(k/3f);
            float N = (float)(Math.sqrt(3) * Math.sin(k/3f));
            float P = (by/(3f*ay)) * -1;

            logger.logComment("M: "+M+", N: "+N+", P: "+P+", small float: ");

            y1 = (float)(2*j * Math.cos(k/3f)   - (by/(3*ay)));

            y2 = (float)(L * (M + N) + P);
            y3 = (float)(L * (M - N) + P);


        }
        else if (hcub==0)
        {
            y1 = (float)Math.cbrt(dy/ay) * -1f;
            y2 = y1;
            y3 = y1;
        }
        else
        {
            logger.logError("Case not covered!!!");
        }

        logger.logComment("Our 3 roots of the cubic are y1: "+y1+", y2: "+y2+", y3: "+y3);

            float p = (float)Math.sqrt(y1);
            float q = (float)Math.sqrt(y3);
            float r = -1* g / (8f * p * q);
            float s = b/(4f*a);

            roots[0] = p + q + r -s;
            roots[1] = p - q - r -s;
            roots[2] = -p + q - r -s;
            roots[3] = -p - q + r -s;


        return roots;
    }



    public static String printDetails(SimpleCompartment[] multiComp)
    {
        StringBuffer sb = new StringBuffer("Multi compartment with "+multiComp.length+" parts:\n");
        for (int i = 0; i < multiComp.length; i++)
        {
            sb.append(i+": "+multiComp[i]+"\n");
        }
        return sb.toString();
    }


    public static void main(String[] args)
    {
        //SimpleCompartment comp1 = new SimpleCompartment(5,5, 10);

        double specAxRes = 300;


        SimpleCompartment[] multiComp
            = new SimpleCompartment[]
            {new SimpleCompartment(5, 5, 10),
            new SimpleCompartment(5, 5.4, 10)/*new SimpleCompartment(0.0005, 0.0005, 0.001),
            new SimpleCompartment(0.0005, 0.0001, 0.001),
            new SimpleCompartment(5, 5.1, 10.0),
            new SimpleCompartment(1, 1, 0.0001)*/};

        System.out.println("Original multi: "+ printDetails(multiComp));


        //System.out.println("Fract surf area: " + getFractionalSurfArea(multiComp, 0.33333333));
        //System.out.println("Fract surf area: " + getFractionalSurfArea(multiComp, 0.66666666));

        System.out.println("Original total surf area: " + getTotalCurvedSurfArea(multiComp));
        //System.out.println("Total memb Res: " + specMembRes/getTotalCurvedSurfArea(multiComp));


        //System.out.println("Fract ax res: " + getFractionalAxialRes(multiComp, 0.25, specAxRes));
        //System.out.println("Fract ax res: " + getFractionalAxialRes(multiComp, 0.5, specAxRes));
        //System.out.println("Fract ax res: " + getFractionalAxialRes(multiComp, 0.75, specAxRes));
        System.out.println("Original total ax res: " + getFractionalAxialRes(multiComp, 1, specAxRes)+"\n\n");
/*
        SimpleCompartment sc = getCylWithSameArea(multiComp);
        SimpleCompartment[] scArray = new SimpleCompartment[]{sc};


        System.out.println("\n\nNew cylinder: " + sc);
        System.out.println("Surf area: " + sc.getCurvedSurfaceArea());

        System.out.println("Total ax res: " + getFractionalAxialRes(scArray, 1, specAxRes));
        System.out.println("Total memb Res: " + getTotalMembraneRes(sc, specMembRes));
*/
        SimpleCompartment[] cyls = getDoubleCylinder(multiComp);

        System.out.println("\n\nNew set of cylinders: " + printDetails(cyls));


        System.out.println("Total surf area: " + getTotalCurvedSurfArea(cyls));
        System.out.println("Total ax res: " + getTotalAxialRes(cyls, specAxRes));


        float a = 2;
        float b = -41;
        float c = -42;
        float d = 360;
/*
         float a = 1;
        float b = 1;
        float c = 1;
        float d = 1;*/

 /*
        float a = 0;
        float b = 6;
        float c = -60;
        float d = 36;

      float a = -20;
        float b = 80;
        float c = 200;
        float d = -1000;*/

        System.out.println("\n-----------------------------------------\n");

        float[] roots = getRoots(a,b,c,d);

        for (int i = 0; i < roots.length; i++)
        {
            //float r = 3.64583896952910f;//roots[i];
            float r = roots[i];
            System.out.println("Root "+i+": "+ r);


                System.out.println("Plugged into quartic: " +(r*r*r*r +a*r*r*r +b*r*r + c*r +d ));

        }

        if (true) return;


/*
        comp1.scale(0.0001);

        System.out.println("comp1: " + comp1.toString());
        System.out.println("Curved surf area: " + comp1.getCurvedSurfaceArea());
        System.out.println("Total surf area: " + comp1.getTotalSurfArea());
        System.out.println("getStartCircumference: " + comp1.getStartCircumference());
        System.out.println("getEndCircumference: " + comp1.getEndCircumference());
        System.out.println("getStartSurfArea: " + comp1.getStartSurfArea());
        System.out.println("getEndSurfArea: " + comp1.getEndSurfArea());
        System.out.println("comp1 getVolume: " + comp1.getVolume() + "\n");
        System.out.println("comp1 getAxialResistance: " + getTotalAxialRes(comp1, specAxRes)+ "\n");
        System.out.println("comp1 getTotalMembraneRes: " + getTotalMembraneRes(comp1, specMembRes)+ "\n");

        //if (true) return;



        double equivRad = getEquivalentRadius(comp1.getStartRadius(),
                                              comp1.getEndRadius(),
                                              comp1.getHeight());


        System.out.println("comp1 equivRad: " + equivRad + "\n");
        SimpleCompartment equivComp1 = new SimpleCompartment(equivRad, equivRad, comp1.getHeight());
        System.out.println("equivComp1 surf area: " + equivComp1.getCurvedSurfaceArea() + "\n");
        System.out.println("equivComp1 total area: " + equivComp1.getTotalSurfArea() + "\n");

        //SimpleCompartment[] multiComp = new SimpleCompartment[]{comp1, comp2, comp3};
        SimpleCompartment[] multiComp = new SimpleCompartment[]{comp1};


        System.out.println("Total curvy area: "+ getTotalCurvedSurfArea(multiComp));
        System.out.println("Total vol: "+ getTotalVolume(multiComp));

        DataSet volData = new DataSet("Volume","Volume");
        DataSet areaData = new DataSet("Surf area","Surf area");
        DataSet axResData = new DataSet("Axial Resistance","Axial Resistance");
        int numSegs = 3;

        for (int i = 0; i <= numSegs+1; i++)
        {
            double fractionToEndSeg = (double)(i)/(numSegs);

            double locMidSeg = (double)((2*i)-1)/(2*numSegs);
            double locLastMidSeg = (double)((2*(i-1))-1)/(2*numSegs);

            if (locLastMidSeg<0) locLastMidSeg = 0;
            if (locMidSeg>1)
            {
                locMidSeg = 1;
                locLastMidSeg =1;
            }
            if (fractionToEndSeg==0) locMidSeg = 0;
            if (fractionToEndSeg>1) fractionToEndSeg = 1;

            System.out.println("---   Looking at segment num: "+ i
                               + ", fractionToEndSeg: "+fractionToEndSeg
                               + ", locMidSeg: "+locMidSeg
                               + ", locLastMidSeg: "+locLastMidSeg);

            double area = getFractionalSurfArea(multiComp, locMidSeg);
            areaData.addPoint(locMidSeg, area);
            System.out.println("Total curvy area at point "+locMidSeg+" along: "+ area);


            double axResTotal = getFractionalAxialRes(multiComp, locMidSeg, specAxRes);


            double  axResThisSeg = axResTotal - getFractionalAxialRes(multiComp, locLastMidSeg, specAxRes);


            axResData.addPoint(locMidSeg, axResTotal);
            System.out.println("Total ax res at point "+locMidSeg+" along: "+ axResTotal);
            System.out.println("Ax res this segment: "+ axResThisSeg);

        }
    //    if (true) return;
        PlotterFrame frame = PlotManager.getPlotterFrame("Plot", true);
        frame.addDataSet(volData);
        frame.addDataSet(areaData);
        frame.addDataSet(axResData);
*/

    }
}
