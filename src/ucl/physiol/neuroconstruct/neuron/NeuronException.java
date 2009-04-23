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

package ucl.physiol.neuroconstruct.neuron;

import java.io.*;
import java.util.*;

import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 *
 * An exception for badly formatted NEURON files
 *
 * @author Padraig Gleeson
 *  
 *
 */

@SuppressWarnings("serial")
public class NeuronException extends Exception
{
    private NeuronException()
   {
   }

   public NeuronException(String message)
   {
       super(message);
   }

   public NeuronException(String filename, String comment)
   {
       super("Problem with formatting of NEURON file: "+ filename+"\n"+ comment);
   }


   public NeuronException(String comment, Throwable t)
   {
       super(comment, t);
   }


   public NeuronException(String filename, String comment, Throwable t)
   {
       super("Problem with formatting of NEURON file: "+ filename+"\n"+ comment, t);
   }

   public static void main(String[] args)
   {
       System.out.println("Just a convenient place to stick a main function... ");

       try
       {
           Project testProj = Project.loadProject(new File("models/Mainen/Mainen.neuro.xml"),
                                                  new ProjectEventListener()
           {
               public void tableDataModelUpdated(String tableModelName)
               {
               };

               public void tabUpdated(String tabName)
               {
               };
               public void cellMechanismUpdated()
               {
               };

           });

           Cell cell = testProj.cellManager.getCell("MainenNeuroML");

           System.out.println("cell: " + cell);

           ArrayList<Section> secs = cell.getAllSections();

           Section aseg = null;

           for (Section sec: secs)
           {
               if (sec.getStartRadius()==0)
               {
                   System.out.println("Problem with section: " + sec);
               }
               if (sec.getSectionName().equals("soma")) aseg = sec;

           }

           SegmentLocation sl = CellTopologyHelper.getFractionAlongSegment(cell, aseg, 0.5f);

           System.out.println("sl: "+sl);

           //Vector<Segment> segs = cell.getAllSegments();

           if (true) return;

           /*

           //  check segments...
           for (Segment seg: segs)
           {
               if (seg.getSegmentLength()==0)
               {
                   System.out.println("Problem with seg: " + seg);

                   LinkedList<Segment> otherSegs = cell.getAllSegmentsInSection(seg.getSection());
                   float totalSurfaceArea = 0;

                   for (int i = 0; i < otherSegs.size(); i++)
                   {
                       Segment nextSeg = otherSegs.get(i);

                       System.out.println("Adding surf area for "+nextSeg.getSegmentName()+": " + nextSeg.getSegmentSurfaceArea());

                       totalSurfaceArea = totalSurfaceArea + nextSeg.getSegmentSurfaceArea();


                   }

                   System.out.println("totalSurfaceArea before: " + totalSurfaceArea);

                   seg.setEndPointPositionX(seg.getEndPointPositionX() + 0.001f);
                   System.out.println("Now seg: " + seg);

                   totalSurfaceArea = 0;

                   for (int i = 0; i < otherSegs.size(); i++)
                   {
                       Segment nextSeg = otherSegs.get(i);

                       totalSurfaceArea = totalSurfaceArea + nextSeg.getSegmentSurfaceArea();

                       System.out.println("Adding surf area for "+nextSeg.getSegmentName()+": " + nextSeg.getSegmentSurfaceArea());

                   }

                   System.out.println("totalSurfaceArea after: " + totalSurfaceArea);


               }

           }
*/


/*
      // Updating section info from file...

 Section chosenSec = null;

           String secName = "myelin[4]";

           for (Section sec: secs)
           {
               //System.out.println("Section: " + sec);
               if (sec.getSectionName().equals(secName))
               {
                   chosenSec = sec;
                   if ()
               }
           }


           File f = new File("../temp/iseg.txt");
           Reader in = new FileReader(f.getAbsolutePath());
           BufferedReader lineReader = new BufferedReader(in);
           String nextLine = null;

           int lineNumber = 0;

           Segment currentSeg = CellTopologyHelper.getOrderedSegmentsInSection(cell, chosenSec).firstElement();

           while ( (nextLine = lineReader.readLine()) != null)
           {
               lineNumber++;

               //System.out.println("Looking at line number: " + lineNumber + " (" + nextLine + ")");

               String[] vlas = nextLine.trim().split("\\s+");

               String name = vlas[0];
               if (!name.equals(secName))
               {
                   throw new Exception("Error with name of section!!");
               }
               int segNum = Integer.parseInt(vlas[1]);
               float x = Float.parseFloat(vlas[2]);
               float y = Float.parseFloat(vlas[3]);
               float z = Float.parseFloat(vlas[4]);
               float rad = Float.parseFloat(vlas[5])/2f;

               System.out.println("segNum: "+segNum+": " + x+","+y+","+z+" "+rad);

               System.out.println("currentSeg: " + currentSeg);

               if (segNum>=2)
               {
                   currentSeg = cell.addDendriticSegment(rad, secName+"__"+(segNum-1),
                                                         new Point3f(x,y,z), currentSeg, 1,
                                                         chosenSec.getSectionName());

               }

               System.out.println("currentSeg: " + currentSeg);
               System.out.println("Num segs: " + cell.getAllSegments().size());

           }
           */


/*            Fixing nseg...

          File f = new File("../temp/nseg.txt");
          Reader in = new FileReader(f.getAbsolutePath());
          BufferedReader lineReader = new BufferedReader(in);
          String nextLine = null;

          int lineNumber = 0;


          while ( (nextLine = lineReader.readLine()) != null)
          {
              lineNumber++;

              //System.out.println("Looking at line number: " + lineNumber + " (" + nextLine + ")");

              String[] vlas = nextLine.trim().split("\\s+");

              String name = vlas[0];

              int nseg = Integer.parseInt(vlas[1]);

              System.out.println("Section : "+name+" has nseg: " + nseg);

              for (Section sec : secs)
              {
                  if (sec.getSectionName().equals(name))
                  {
                      sec.setNumberInternalDivisions(nseg);
                      System.out.println("Updated: " + sec);
                  }
              }

          }
*/


          File f = new File("../models/spikeinit/gpas.txt");
          Reader in = new FileReader(f.getAbsolutePath());
          BufferedReader lineReader = new BufferedReader(in);
          String nextLine = null;

          int lineNumber = 0;

          while ( (nextLine = lineReader.readLine()) != null)
          {
              lineNumber++;

              //System.out.println("Looking at line number: " + lineNumber + " (" + nextLine + ")");

              String[] vlas = nextLine.trim().split("\\s+");

              String name = vlas[0];

              float gpas = Float.parseFloat(vlas[1]);

              System.out.println("Section : " + name + " has gpas: " + gpas);

              //cm=cm*1e-8f;

              gpas =gpas*1e-5f;

              String channame = "LeakCond";

              for (Section sec : secs)
              {
                  if (sec.getSectionName().equals(name))
                  {
                      //float currGpas = cell.getSpecCapForSection(sec);

                      //System.out.println("Current cm: " + currCm);
                      //sec.setNumberInternalDivisions(nseg);
                      //System.out.println("Updated: " + sec);

                      //if (Float.isNaN(currCm))
                      //{
                          String grpName = GeneralUtils.replaceAllTokens(sec.getSectionName()+"_grp", "[", "_");
                          grpName = GeneralUtils.replaceAllTokens(grpName, "]", "_");
                          sec.addToGroup(grpName);
                          ChannelMechanism cham = new ChannelMechanism(channame, gpas);

                          cell.associateGroupWithChanMech(grpName, cham);

                          //cell.associateGroupWithSpecCap(grpName, cm);
                     // }
                      float newCm = cell.getSpecCapForSection(sec);

                      System.out.println("New sec "+ sec);

                      System.out.println("New chans: "+ cell.getUniformChanMechsForSec(sec));
                     // System.out.println("----------------------------------");

                  }
                      //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
              }

                     // System.out.println("......................................................................");

          }





           testProj.markProjectAsEdited();


           testProj.saveProject();

           //System.out.println(CellTopologyHelper.printShortDetails(testProj.cellManager.getCell("MainenNeuroML")));


           System.out.println("Done...: ");


       }
       catch (Exception ex)
       {

           System.out.println("Error!!!");

           ex.printStackTrace();
       }

   }
}
