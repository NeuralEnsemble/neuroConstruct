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

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.*;
import java.util.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 *
 * A class for importing GENESIS morphology files (.p files), and creating Cells
 * which can be used by the rest of the application
 *
 * @author Padraig Gleeson
 *
 *
 */

public class GenesisMorphReader extends FormatImporter
{
    private ClassLogger logger = new ClassLogger("GenesisMorphReader");


    public GenesisMorphReader()
    {
        super("GenesisMorphReader",
                            "Importer of GENESIS *.p files",
                            new String[]{".p"});
    }

    public Cell loadFromMorphologyFile(File morphologyFile, String name) throws MorphologyException
    {
        logger.logComment("Parsing file: " + morphologyFile);
        Cell cell = new Cell();
        cell.setInstanceName(name);

        try
        {
            boolean inRelativeCoordMode = true;
            boolean doubleEndpoint = false;

            Reader in = new FileReader(morphologyFile);
            BufferedReader lineReader = new BufferedReader(in);

            String previousSegmentName = null;
            float somaRadius = 0;

            int currentShape = Segment.CYLINDRICAL_SHAPE; // default...

            String somaNameInFile = null; // the name of the initial compartment, which will be assumed to be the soma

            String nextLine = null;

            String currentComptGroup = null;

            int lineCount = 0;

            //int dendriteCount = 0;


            boolean inBlockComment = false;
            String commentStart = "/*";
            String commentEnd = "*/";

            Hashtable<String, Segment> namesVsSegments = new Hashtable<String, Segment>();

            while ( (nextLine = lineReader.readLine()) != null)
            {
                lineCount++;
                nextLine = nextLine.trim();
                logger.logComment("----  Looking at line num " + lineCount + ": " + nextLine);

                StringBuffer noComments = new StringBuffer();
                for (int i = 0; i <= nextLine.length(); i++)
                {
                    if (i < nextLine.length() && nextLine.substring(i).startsWith(commentStart))
                       inBlockComment = true;
                    if (i>=2 && nextLine.substring(i-2).startsWith(commentEnd))
                        inBlockComment = false;

                    if (i < nextLine.length() && !inBlockComment) noComments.append(nextLine.charAt(i));
                }
                nextLine = noComments.toString().trim();

                logger.logComment("Continuing with: (" + nextLine+")");


                if (nextLine.startsWith("//") || nextLine.startsWith(" //") || nextLine.startsWith("  //"))
                {
                    logger.logComment("Comment: " + nextLine);
                }
                else if (nextLine.startsWith("*") || nextLine.startsWith(" *") || nextLine.startsWith("  *"))
                {
                    logger.logComment("Macro:   " + nextLine);
                    /** @todo Deal with all macros... */

                    if (nextLine.equals("*spherical"))
                    {
                        logger.logComment("Changing the shape of the soma to a sphere...");
                        currentShape = Segment.SPHERICAL_SHAPE;
                    }
                    else if (nextLine.equals("*cylindrical"))
                    {
                        logger.logComment("Changing the shape of the soma to a cylinder...");
                        currentShape = Segment.CYLINDRICAL_SHAPE;
                    }
                    else if (nextLine.equals("*relative"))
                    {
                        logger.logComment("Changing coord mode to relative...");
                        inRelativeCoordMode = true;
                    }
                    else if (nextLine.equals("*absolute"))
                    {
                        logger.logComment("Changing coord mode to absolute...");
                        inRelativeCoordMode = false;
                    }
                    else if (nextLine.equals("*double_endpoint"))
                    {
                        logger.logComment("Setting double_endpoint on...");
                        doubleEndpoint = true;
                    }
                    else if (nextLine.equals("*double_endpoint_off"))
                    {
                        logger.logComment("Setting double_endpoint off...");
                        doubleEndpoint = false;
                    }




                    else if (nextLine.startsWith("*compt "))
                    {
                        String compartment = nextLine.substring("*compt ".length()).trim();
                        logger.logComment("Changing current compartment to: " + compartment);
                        currentComptGroup = compartment.replace('/', '_');
                        if (currentComptGroup.startsWith("_"))
                        {
                            currentComptGroup = currentComptGroup.substring(1);
                        }
                    }

                    // macros to ignore...
                    else if (nextLine.equals("*cartesian") ||
                             nextLine.equals("*asymmetric") ||
                             nextLine.equals("*symmetric") ||
                             nextLine.startsWith("*lambda_warn") ||
                             nextLine.equals("*lambda_unwarn") ||
                             nextLine.startsWith("*set_compt_param") ||
                             nextLine.startsWith("*rand_spines") ||
                             nextLine.startsWith("*set_global"))
                    {
                        logger.logComment("Ignoring line of macro: " + nextLine);
                    }
                    else
                    {
                        throw new
                            MorphologyException(morphologyFile.getAbsolutePath(),
                                                "The macro: "
                                                + nextLine
                                                +
                                                " is not supported in the current version of GenesisMorphReader...");
                    }

                }
                else if (nextLine.trim().length() < 1)
                {
                }
                else
                {
                    String[] items = nextLine.split("\\s+");

                    if (items.length < 6)
                    {
                        String error = "Problem splitting up line (expecting at least 6 elements): " + nextLine;
                        throw new MorphologyException(morphologyFile.getAbsolutePath(), error);
                    }
                    else
                    {
                        try
                        {
                            String segmentName = items[0];
                            String parentName = items[1];
                            Point3f start = null;

                            Point3f end = new Point3f(Float.parseFloat(items[2]),
                                                        Float.parseFloat(items[3]),
                                                        Float.parseFloat(items[4]));

                            float radius = Float.parseFloat(items[5]) / 2f;

                            if (doubleEndpoint && items.length == 9)
                            {
                                start = new Point3f(end);

                                end = new Point3f(Float.parseFloat(items[5]),
                                                          Float.parseFloat(items[6]),
                                                          Float.parseFloat(items[7]));

                                radius = Float.parseFloat(items[8]) / 2f;
                                logger.logComment("Found endpoint for comp: "+segmentName+": ("
                                                  +end.x+", "+end.y+", "+end.z+")");
                            }

                            if (parentName.equals("."))
                            {
                                parentName = previousSegmentName;
                            }

                            previousSegmentName = segmentName;

                            logger.logComment("      ");
                            logger.logComment("        ------");
                            logger.logComment("SegmentName: " + segmentName + ", parent: " + parentName + ", radius: " +
                                              radius+ ", end: "+ end );

                            Segment newSegment = null;

                            if (parentName.equals("none"))
                            {
                                somaNameInFile = new String(segmentName);
                                logger.logComment("Creating the Soma, called: " + somaNameInFile);

                                somaRadius = radius;

                                if (doubleEndpoint)
                                {
                                    newSegment = cell.addFirstSomaSegment(somaRadius,
                                                             somaRadius,
                                                        somaNameInFile,
                                                        start,
                                                        end,
                                                        new Section(somaNameInFile));
                                }
                                else
                                {
                                    if (currentShape == Segment.CYLINDRICAL_SHAPE)
                                    {
                                        newSegment = cell.addFirstSomaSegment(somaRadius,
                                            somaRadius,
                                            somaNameInFile,
                                            new Point3f(0, 0, 0),
                                            end,
                                            new Section(somaNameInFile));
                                    }
                                    else
                                    {
                                        newSegment = cell.addFirstSomaSegment(somaRadius,
                                            somaRadius,
                                            somaNameInFile,
                                            end,
                                            end,
                                            new Section(somaNameInFile));
                                    }
                                }
                                if (currentComptGroup != null)
                                {
                                    logger.logComment("Adding soma to group: " + currentComptGroup);
                                    newSegment.getSection().addToGroup(currentComptGroup);
                                }
                                namesVsSegments.put(parentName, newSegment);

                            }
                            else
                            {
                                if (inRelativeCoordMode &&
                                    (!doubleEndpoint && end.distance(new Point3f())==0))
                                {
                                    String error = new String("Zero length segment at line " + lineCount + ": " +
                                                              nextLine);
                                    logger.logError("Returning error: " + error);
                                    throw new MorphologyException(morphologyFile.getAbsolutePath(),
                                                                  error);
                                }

                                logger.logComment("Adding segment called: " + segmentName);

                                if (currentShape != Segment.CYLINDRICAL_SHAPE)
                                {

                                    throw new MorphologyException(morphologyFile.getAbsolutePath(),
                                                                  "Cannot support non-cylindrical dendrites/axon in this version (problem with line: \n"
                                                                  + nextLine + ")");
                                }

                                if (parentName.equals(somaNameInFile))
                                {
                                    logger.logComment("Adding it to the soma");

                                    logger.logComment("Adding it as a dendritic tree to \"top\" of soma");
                                    Point3f posnEndPoint = null;
                                    Point3f posnStartPoint = null;


                                    if (inRelativeCoordMode)
                                    {
                                        posnEndPoint = new Point3f(end);
                                        if (start!=null) posnStartPoint = new Point3f(start);

                                        if (cell.getFirstSomaSegment().getSegmentShape()
                                               == Segment.CYLINDRICAL_SHAPE)
                                        {
                                            posnEndPoint.add(cell.getFirstSomaSegment().getEndPointPosition());
                                            if (start!=null) posnStartPoint.add(cell.getFirstSomaSegment().getEndPointPosition());
                                        }
                                    }
                                    else
                                    {
                                        posnEndPoint = new Point3f(end);
                                        if (start!=null) posnStartPoint = new Point3f(start);
                                    }

                                    Segment parent = cell.getFirstSomaSegment();




                                    newSegment
                                        = cell.addDendriticSegment(radius,
                                                                   segmentName,
                                                                   posnEndPoint,
                                                                   parent,
                                                                   1,                            // as this is the convention in GENESIS
                                                                   segmentName,
                                                                   false);       // false, as GENESIS compartments are cylindrical anyway

                                    newSegment.getSection().setStartRadius(radius);

                                    if (doubleEndpoint)
                                    {
                                        newSegment.getSection().setStartPointPositionX(posnStartPoint.x);
                                        newSegment.getSection().setStartPointPositionY(posnStartPoint.y);
                                        newSegment.getSection().setStartPointPositionZ(posnStartPoint.z);
                                    }


                                    namesVsSegments.put(segmentName, newSegment);

                                    if (currentComptGroup != null)
                                    {

                                        logger.logComment("Adding newSegment to group: " + currentComptGroup);
                                        newSegment.getSection().addToGroup(currentComptGroup);
                                    }
                                }
                                else
                                {
                                    Segment parentSeg = (Segment)namesVsSegments.get(parentName);
                                    logger.logComment("Parent seg: "+ parentSeg);
                                    Point3f posParent = parentSeg.getEndPointPosition();

                                    Point3f posnEndPoint = null;
                                    Point3f posnStartPoint = null;

                                    if (inRelativeCoordMode)
                                    {
                                        posnEndPoint = new Point3f(end);
                                        posnEndPoint.add(posParent);

                                        if (start!=null)
                                        {
                                            posnStartPoint = new Point3f(start);
                                            posnStartPoint.add(posParent);
                                        }
                                    }
                                    else
                                    {
                                        posnEndPoint = new Point3f(end);
                                        if (start!=null) posnStartPoint = new Point3f(start);

                                    }
                                    logger.logComment("End point: " + Utils3D.getShortStringDesc(posnEndPoint));


                                    newSegment = cell.addDendriticSegment(radius,
                                                             segmentName,
                                                             posnEndPoint,
                                                             parentSeg,
                                                             1,
                                                             segmentName,
                                                             false);

                                    newSegment.getSection().setStartRadius(radius);

                                    if (doubleEndpoint)
                                    {
                                        newSegment.getSection().setStartPointPositionX(posnStartPoint.x);
                                        newSegment.getSection().setStartPointPositionY(posnStartPoint.y);
                                        newSegment.getSection().setStartPointPositionZ(posnStartPoint.z);
                                    }

                                    namesVsSegments.put(segmentName, newSegment);

                                    if (currentComptGroup != null)
                                    {

                                        logger.logComment("Adding newSegment to group: " + currentComptGroup);
                                        newSegment.getSection().addToGroup(currentComptGroup);
                                    }

                                }
                            }

                        logger.logComment("Created newSegment: "+ newSegment+"\n");

                        }
                        catch (Exception e)
                        {
                            logger.logError("Problem with the morphology file...");
                            throw new MorphologyException(morphologyFile.getAbsolutePath(), e.getMessage(), e);
                        }
                    }
                }
            }
            if (lineCount == 0)
            {
                GuiUtils.showErrorMessage(logger, "Error. No lines found in file: " + morphologyFile, null, null);
            }
        }
        catch (IOException e)
        {
            GuiUtils.showErrorMessage(logger, "Error: " + e.getMessage(), e, null);
            return null;
        }
        logger.logComment("Completed parsing of file: " + morphologyFile);

        return cell;
    }

    public static void main(String[] args)
    {
        //File f = new File("C:\\nrn54\\Morphology\\Purk2M0.p");
        //File f = new File("/home/padraig/genesis/Scripts/pattraub/CA1.p");
        File f = new File("Y:\\Padraig\\Datas\\WvG\\NewPC.p");

        try
        {
            GenesisMorphReader genReader = new GenesisMorphReader();

            Cell genesisCell = genReader.loadFromMorphologyFile(f, "PurkinjeCellll");

            System.out.println("loaded cell: ");

            System.out.println(CellTopologyHelper.printDetails(genesisCell, null));

        }
        catch (MorphologyException ex)
        {
            ex.printStackTrace();
        }
    }

}
