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


package ucl.physiol.neuroconstruct.utils;

import java.util.ArrayList;
import javax.vecmath.*;

/**
 * Calc all the possible coordinates of a tuple of x,y,z and a given length and
 * convert them to cartesian coordinates.
 * 
 * @author Michele Mattioni, EBI
 *
 */

public class CoordCalculator 
{
    static ClassLogger logger = new ClassLogger("CoordCalculator");
    
    /**
     * Convert degree to radians
     * radian = degrees * pi/180
     * @param degree
     * @return radian
     */
    static float toRad(float degree)
    {
            float rad = degree * (float)Math.PI/180f;
            return rad;
    }
    	
    public static void main(String[] args) 
    {
        //logger.setThisClassVerbose(true);
        
        if (args.length != 4 )
        {
                usage();
                System.exit(1);
        }
        
        Point3i coord = new Point3i(); //z
        
        int len = Integer.parseInt(args[3]); //lenght of the seg
        
        System.out.println("Calculating a bunch of possible coordinates\n" +
        "Initial coordinates: " + coord);
        
        ArrayList<Point3f>  points = getCoords(Integer.parseInt(args[0]),  //x 
                                    Integer.parseInt(args[1]),  //y
                                    Integer.parseInt(args[2]), //z
                                    len);
        
      
        
    }
    
    public static ArrayList<Point3f> getCoords(int x, int y, int z, int len)
    {

        Point3f coord = new Point3f(x, y, z); 
        
        ArrayList<Point3f> alreadyFound = new ArrayList<Point3f>();

        long start = System.currentTimeMillis();
        
        for ( float theta = 0; theta <= 360; theta+=0.2)   // Angle in plane of xz around y axis
        {
            float thetaRad = toRad(theta);
            
            for ( float phi = 0; phi <= 180; phi+=0.2)    // Angle between vestor and y axis
            {
                float phiRad = toRad(phi);

                float tmpCoordX = Math.round(len * (Math.sin(phiRad) * Math.cos(thetaRad)));
                float tmpCoordY = Math.round(len * (Math.cos(phiRad)));
                float tmpCoordZ = Math.round(len * (Math.sin(phiRad)* Math.sin(thetaRad)));

                Point3f newCoord = new Point3f(coord.x + tmpCoordX,
                                               coord.y + tmpCoordY,
                                               coord.z + tmpCoordZ);


                float newLen = newCoord.distance(coord);
                

                if (newCoord.x == (int)newCoord.x && 
                    newCoord.y == (int)newCoord.y &&
                    newCoord.z == (int)newCoord.z)
                {
                    
                    if (newLen == len && !alreadyFound.contains(newCoord)) //check to assure we don't experience round problem
                    {
                        logger.logComment(newCoord + "\tdelta\t" + newLen + "\ttheta\t" + theta
                            + "\tphi\t" + phi);
                        
                        alreadyFound.add(newCoord);
                    }

    //               	System.out.println(alreadyPrinted.size());
                }
            }
            
        }
        
        logger.logComment("Finished in: "+ (System.currentTimeMillis()-start)/1000f+" seconds, after finding "+alreadyFound.size()+" points");
        return alreadyFound;
    }

    private static void usage() 
    {
        
            System.out.println("Usage:\n  CoordCalculator x y z l" +
                            "\nGive me the initial coordinates: x, y, z and the length of the segment " +
                            "as arguments, please.");

    }
    
    
}
