/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
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
 * @version 1.0.3
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

            Reader in = new FileReader(morphologyFile);
            BufferedReader lineReader = new BufferedReader(in);

            String previousSegmentName = null;
            float somaRadius = 0;

            int currentShape = Segment.CYLINDRICAL_SHAPE; // default...

            String somaNameInFile = null; // the name of the initial compartment, which will be assumed to be the soma

            String nextLine = null;

            String currentCompartment = null;

            int lineCount = 0;

            //int dendriteCount = 0;


            boolean inBlockComment = false;
            String commentStart = "/*";
            String commentEnd = "*/";

            Hashtable namesVsSegments = new Hashtable();

            while ( (nextLine = lineReader.readLine()) != null)
            {
                lineCount++;
                nextLine = nextLine.trim();
                logger.logComment("Looking at line num " + lineCount + ": " + nextLine);

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

                    else if (nextLine.startsWith("*compt "))
                    {
                        String compartment = nextLine.substring("*compt ".length()).trim();
                        logger.logComment("Changing current compartment to: " + compartment);
                        currentCompartment = compartment.replace('/', '_');
                        if (currentCompartment.startsWith("_"))
                        {
                            currentCompartment = currentCompartment.substring(1);
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
                            float xCoord = Float.parseFloat(items[2]);
                            float yCoord = Float.parseFloat(items[3]);
                            float zCoord = Float.parseFloat(items[4]);
                            float radius = Float.parseFloat(items[5]) / 2f;

                            if (parentName.equals("."))
                            {
                                parentName = previousSegmentName;
                            }
                            previousSegmentName = segmentName;
                            logger.logComment("      ");
                            logger.logComment("        ------");
                            logger.logComment("SegmentName: " + segmentName + ", parent: " + parentName + ", radius: " +
                                              radius);

                            Segment newSegment = null;

                            if (parentName.equals("none"))
                            {
                                somaNameInFile = new String(segmentName);
                                logger.logComment("Creating the Soma, called: " + somaNameInFile);

                                somaRadius = radius;
                                if (currentShape == Segment.CYLINDRICAL_SHAPE)
                                {
                                    newSegment = cell.addFirstSomaSegment(somaRadius,
                                                             somaRadius,
                                                        somaNameInFile,
                                                        new Point3f(),
                                                        new Point3f(xCoord,
                                                                    yCoord,
                                                                    zCoord),
                                                        new Section(somaNameInFile));
                                }
                                else
                                {
                                    newSegment = cell.addFirstSomaSegment(somaRadius,
                                                             somaRadius,
                                                        somaNameInFile,
                                                        null,
                                                        null,
                                                        new Section(somaNameInFile));

                                }

                                if (currentCompartment != null)
                                {
                                    logger.logComment("Adding soma to group: " + currentCompartment);
                                    newSegment.getSection().addToGroup(currentCompartment);
                                }
                                namesVsSegments.put(parentName, newSegment);

                            }
                            else
                            {
                                if (inRelativeCoordMode &&
                                    xCoord == 0 &&
                                    yCoord == 0 &&
                                    zCoord == 0)
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
                                    if (inRelativeCoordMode)
                                    {
                                        posnEndPoint = new Point3f(xCoord,
                                                                   yCoord,
                                                                   zCoord);

                                        if (cell.getFirstSomaSegment().getSegmentShape()
                                               == Segment.CYLINDRICAL_SHAPE)
                                        {
                                            posnEndPoint.add(cell.getFirstSomaSegment().getEndPointPosition());
                                        }
                                    }
                                    else
                                    {
                                        posnEndPoint = new Point3f(xCoord,
                                                                   yCoord,
                                                                   zCoord);
                                    }
                                    newSegment
                                        = cell.addDendriticSegment(radius,
                                                                   segmentName,
                                                                   posnEndPoint,
                                                                   cell.getFirstSomaSegment(),
                                                                   1,                            // as this is the convention in GENESIS
                                                                   segmentName);

                                    newSegment.getSection().setStartRadius(radius);


                                    namesVsSegments.put(segmentName, newSegment);

                                    if (currentCompartment != null)
                                    {

                                        logger.logComment("Adding newSegment to group: " + currentCompartment);
                                        newSegment.getSection().addToGroup(currentCompartment);
                                    }
                                }
                                else
                                {
                                    Segment parentSeg = (Segment)namesVsSegments.get(parentName);
                                    logger.logComment("Parent seg: "+ parentSeg);
                                    Point3f positionParent = parentSeg.getEndPointPosition();

                                    Point3f posnNewEndPoint = null;
                                    if (inRelativeCoordMode)
                                    {
                                        posnNewEndPoint = new Point3f(positionParent.x + xCoord,
                                                                      positionParent.y + yCoord,
                                                                      positionParent.z + zCoord);
                                    }
                                    else
                                    {
                                        posnNewEndPoint = new Point3f(xCoord,
                                                                      yCoord,
                                                                      zCoord);

                                    }
                                    logger.logComment("End point: " + Utils3D.getShortStringDesc(posnNewEndPoint));


                                        newSegment = cell.addDendriticSegment(radius,
                                                                 segmentName,
                                                                 posnNewEndPoint,
                                                                 parentSeg,
                                                                 1,
                                                                 segmentName);

                                    newSegment.getSection().setStartRadius(radius);


                                    namesVsSegments.put(segmentName, newSegment);

                                    if (currentCompartment != null)
                                    {

                                        logger.logComment("Adding newSegment to group: " + currentCompartment);
                                        newSegment.getSection().addToGroup(currentCompartment);
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
        //logger.logComment("Cell info: " + CellTopologyHelper.printDetails(cell));

        return cell;
    }

    public static void main(String[] args)
    {
        //File f = new File("C:\\nrn54\\Morphology\\Purk2M0.p");
        File f = new File("/home/padraig/genesis/Scripts/pattraub/CA1.p");

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
