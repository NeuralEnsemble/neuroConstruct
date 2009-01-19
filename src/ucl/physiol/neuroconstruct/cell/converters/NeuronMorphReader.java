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
import ucl.physiol.neuroconstruct.neuron.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 *
 * A class for importing Neuron morphology files (.nrn/.hoc files), and
 * creating Cells which can be used by the rest of the application.
 * Note: hoc files should be "passed through" ucl.physiol.neuroconstruct.neuron.NeuronFileConverter
 * before being used by this class
 *
 * @author Padraig Gleeson
 *  
 *
 */


public class NeuronMorphReader extends FormatImporter
{
    private static ClassLogger logger = new ClassLogger("NeuronMorphReader");

    Cell cell = null;

    File morphologyFile = null;


    boolean moveSomaToOrigin = false;

    /**
     * Moves the initial segment of new sections to the point where it is connected
     * to its parent. Doesn't change electrical properties of single cell,
     * but looks better in 3D
     */
    boolean moveDendsToConnectionPoint = false;

    /**
     * Hashtable of the Section name vs. Segment of the first segment in a new section
     */
    private Hashtable<String, Segment> createdFirstSegments = new Hashtable<String, Segment>();
    /**
     * Hashtable of the Section name vs. Segment of the last segment in each section
     */
    private Hashtable<String, Segment> lastAddedChildSegs = new Hashtable<String, Segment>();

    int indexOfValidSegments = 0;

    StringBuffer ignoredLines = new StringBuffer();


    public NeuronMorphReader()
    {
        super("NeuronMorphReader",
                            "Importer of Neuron *.nrn and *.hoc files",
                            new String[]{".nrn", ".hoc"});

                      logger.logComment("---------------------    New NeuronMorphReader");
    }


    /**
     * Creates a Cell based on the contents of the NEURON morphology file
     * Note: hoc files should be "passed through" ucl.physiol.neuroconstruct.hoc.NeuronFileConverter
     * before being used by this class
     */
    public Cell loadFromMorphologyFile(File morphologyFile,
                                       String name,
                                       boolean moveSomaToOrigin,
                                       boolean moveDendsToConnectionPoint) throws MorphologyException
    {
        this.moveSomaToOrigin = moveSomaToOrigin;
        this.moveDendsToConnectionPoint = moveDendsToConnectionPoint;
        Cell newCell = loadFromMorphologyFile(morphologyFile,name);

        logger.logComment("Loaded cell : " + CellTopologyHelper.printDetails(newCell, null));

        CellTopologyHelper.reorderSegsParentsFirst(newCell);

        logger.logComment("Reordered segs cell : " + CellTopologyHelper.printDetails(newCell, null));


        return newCell;
    }
    
    
    public static String getBetterSectionName(String origSecName)
    {
        if (origSecName.indexOf("[")>=0 && origSecName.indexOf("]")>=0)
        {
            String inside = origSecName.substring(origSecName.indexOf("[")+1, origSecName.indexOf("]"));
            String better = "";
            
            if (inside.endsWith("."))
            {
                better = inside.substring(0,inside.length()-1);
                origSecName = GeneralUtils.replaceAllTokens(origSecName, "["+inside+"]", "["+better+"]");                            
            }
            if (inside.endsWith(".0"))
            {
                better = inside.substring(0,inside.length()-2);
                origSecName = GeneralUtils.replaceAllTokens(origSecName, "["+inside+"]", "["+better+"]");                            
            }
        }
        
        String newName = GeneralUtils.replaceAllTokens(origSecName, "[", "_");
        
        newName = GeneralUtils.replaceAllTokens(newName, "]", "");
        
        return newName;
    }
    

    /**
     * Creates a Cell based on the contents of the NEURON morphology file
     * Note: hoc files should be "passed through" ucl.physiol.neuroconstruct.hoc.NeuronFileConverter
     * before being used by this class
     */
    public Cell loadFromMorphologyFile(File oldFile,
                                       String name) throws MorphologyException
    {
        createdFirstSegments.clear();

        lastAddedChildSegs.clear();

        indexOfValidSegments = 0;

        cell = new Cell();

        File newFile  = getSimplifiedFile(oldFile);

        logger.logComment("-----------------     Parsing file: " + newFile);
        cell.setInstanceName(name);

        this.morphologyFile = newFile;

        //this.moveDendsToConnectionPoint = moveDendsToConnectionPoint;
        //this.moveSomaToOrigin = moveSomaToOrigin;

        Segment somaPrimarySegment = null;

        try
        {
            Reader in = new FileReader(morphologyFile);
            BufferedReader lineReader = new BufferedReader(in);
            String nextLine = null;
            int lineCount = 0;

            Section latestCreatedSection = null;
            Section accessedSection = null;

            while ( (nextLine = lineReader.readLine()) != null)
            {
                lineCount++;
                nextLine = nextLine.trim();

                logger.logComment(" ++++++++++++++       Dealing with line: ("+ nextLine+")");

                String possSectionName = nextLine.split("\\s+")[0];

                if (possSectionName.indexOf("{")>=0) possSectionName
                    = possSectionName.substring(0,possSectionName.indexOf("{"));


                if (nextLine.length()==0)
                {
                }
                else if (nextLine.startsWith("create"))
                {
                    String sectionsToCreate = nextLine.substring("create ".length());

                    String[] origSecNames = sectionsToCreate.split(",");
                    String probSomaSecName = origSecNames[0].trim(); // usually...

                    for (int i = 0; i < origSecNames.length; i++)
                    {
                        if (origSecNames[i].toUpperCase().indexOf("SOMA")>=0)
                        {
                            probSomaSecName = origSecNames[i].trim();
                            if (probSomaSecName.indexOf("[")>=0)
                            {
                                probSomaSecName = probSomaSecName.substring(0, probSomaSecName.indexOf("["))+"_0";
                            }
                        }
                    }
                    logger.logComment("probSomaSecName: " + probSomaSecName);

                    for (int numNewSections = 0; numNewSections < origSecNames.length; numNewSections++)
                    {
                        origSecNames[numNewSections] = origSecNames[numNewSections].trim();
                        
                        logger.logComment("Creating section(s): "+ origSecNames[numNewSections]);
                        
                        ArrayList<String> actualSections = new ArrayList<String>();
                        
                        if (origSecNames[numNewSections].indexOf("[")>0)
                        {
                            String arrayName = origSecNames[numNewSections].substring(0,origSecNames[numNewSections].indexOf("[")).trim();

                            String intString = origSecNames[numNewSections].substring(origSecNames[numNewSections].indexOf("[")+1,
                                    origSecNames[numNewSections].indexOf("]")).trim();

                            
                            int arraySize = Integer.parseInt(intString);

                            logger.logComment("Creating array of "+arrayName+" of  size: " + arraySize);
                            
                            for(int i=0;i<arraySize;i++)
                            {
                                actualSections.add(arrayName+"_"+i);
                            }
                        }
                        else
                        {
                            actualSections.add(origSecNames[numNewSections]);
                        }
                        
                        logger.logComment("actualSections: "+actualSections);
                        
                        
                        for (String newSecName: actualSections)
                        {
                            if (somaPrimarySegment == null && 
                                    newSecName.equals(probSomaSecName))
                            {
                                logger.logComment("Assuming this section: " + newSecName
                                                  + " is soma section");
    
                                somaPrimarySegment = cell.addFirstSomaSegment( -1, -1,
                                                                              newSecName + "__0",
                                                                              null,
                                                                              null,
                                                                              new Section(newSecName));
    
                                somaPrimarySegment.getSection().setStartPointPositionX(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                somaPrimarySegment.getSection().setStartPointPositionY(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                somaPrimarySegment.getSection().setStartPointPositionZ(Float.MAX_VALUE); // since we know there's pt3d data coming...
    
                                ///somaPrimarySegment.setShape(Segment.CYLINDRICAL_SHAPE); // always with neuron morphs
    
                                somaPrimarySegment.setEndPointPositionX(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                somaPrimarySegment.setEndPointPositionY(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                somaPrimarySegment.setEndPointPositionZ(Float.MAX_VALUE); // since we know there's pt3d data coming...
    
                                createdFirstSegments.put(newSecName, somaPrimarySegment);
                                lastAddedChildSegs.put(newSecName, somaPrimarySegment);
    
                                latestCreatedSection = somaPrimarySegment.getSection();
                            }
                            else
                            {
                                /*
                                if (sectionNames[numNewSections].indexOf("[")>0)
                                {
                                    String arrayName = sectionNames[numNewSections].substring(0,sectionNames[numNewSections].indexOf("[")).trim();
    
                                    String intString = sectionNames[numNewSections].substring(sectionNames[numNewSections].indexOf("[")+1,
                                            sectionNames[numNewSections].indexOf("]")).trim();
    
                                    
                                    int arraySize = Integer.parseInt(intString);
    
                                    logger.logComment("Creating array of "+arrayName+" of  size: " + arraySize);
    
                                    for (int i = 0; i < arraySize; i++)
                                    {
                                            String newSectionName = arrayName + "[" +i+"]";
    
                                            Segment newSegment
                                                = cell.addDendriticSegment( -1,
                                                                            newSectionName+"__0",
                                                                            new Point3f(),
                                                                            null,
                                                                            1,
                                                                            newSectionName,
                                                                            false);
    
                                            newSegment.getSection().setStartPointPositionX(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                            newSegment.getSection().setStartPointPositionY(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                            newSegment.getSection().setStartPointPositionZ(Float.MAX_VALUE); // since we know there's pt3d data coming...
    
                                            newSegment.setEndPointPositionX(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                            newSegment.setEndPointPositionY(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                            newSegment.setEndPointPositionZ(Float.MAX_VALUE); // since we know there's pt3d data coming...
    
                                            createdFirstSegments.put(newSectionName, newSegment);
                                            lastAddedChildSegs.put(newSectionName, newSegment);
    
                                            latestCreatedSection = newSegment.getSection();
                                    }
                                }
                                else
                                {*/
                                    String newSectionName = new String(newSecName);
                                    Segment newSegment
                                    = cell.addDendriticSegment( -1,
                                                                newSectionName+"__0",
                                                                new Point3f(),
                                                                null,
                                                                1,
                                                                newSectionName,
                                                                false);
    
                                    newSegment.getSection().setStartPointPositionX(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                    newSegment.getSection().setStartPointPositionY(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                    newSegment.getSection().setStartPointPositionZ(Float.MAX_VALUE); // since we know there's pt3d data coming...
    
                                    newSegment.setEndPointPositionX(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                    newSegment.setEndPointPositionY(Float.MAX_VALUE); // since we know there's pt3d data coming...
                                    newSegment.setEndPointPositionZ(Float.MAX_VALUE); // since we know there's pt3d data coming...
    
    
                                    createdFirstSegments.put(newSectionName, newSegment);
                                    lastAddedChildSegs.put(newSectionName, newSegment);
    
                                    latestCreatedSection = newSegment.getSection();
                                //}
                            }
                            
                            
                        }
                    }

                    logger.logComment("createdFirstSegments: " + createdFirstSegments);
                    logger.logComment("lastAddedChildSegs: " + lastAddedChildSegs);
                }// end else if (nextLine.startsWith("create"))
                // Check if the line starts with a created section name...

                else if (isValidSection(possSectionName))
                {
                    String origSecName = possSectionName;
                    logger.logComment("Line referring to section: " + origSecName);
                    
                    String usedSecName = getBetterSectionName(origSecName);

                    Segment primarySegment = getFirstSegment(usedSecName);

                    String parentName = "-- null --";

                    if (primarySegment.getParentSegment() != null)
                        parentName = primarySegment.getParentSegment().getSegmentName();

                    logger.logComment("It's primarySegment: " + primarySegment.getSegmentName() +
                                      ", parent: " + parentName);

                    String restOfLine = nextLine.substring(nextLine.indexOf(origSecName)+origSecName.length()).trim();

                    logger.logComment("nextLine: " + nextLine);
                    logger.logComment("restOfLine: " + restOfLine);

                    if (restOfLine.equals("{"))
                    {
                        int bracketCount = 1;
                        //Segment lastSectionAdded = null;

                        while (bracketCount > 0)
                        {
                            nextLine = lineReader.readLine();
                            lineCount++;
                            nextLine = nextLine.trim();

                            if (nextLine.indexOf("{") >= 0) bracketCount++;
                            if (nextLine.indexOf("}") >= 0) bracketCount--;

                            if (bracketCount > 0)
                            {
                                logger.logComment("          -----+++---    Using line num "
                                                  + lineCount
                                                  + ": " + nextLine
                                                  + " for section: "
                                                  + primarySegment.getSection());

                                useLineForSection(nextLine, primarySegment);
                            }
                        }

                    }
                    else
                    {
                        logger.logComment("          -----------    Using line num "
                                          + lineCount
                                          + ": " + restOfLine
                                          + " for section: "
                                          + primarySegment.getSection());

                        useLineForSection(restOfLine, primarySegment);
                        logger.logComment("Finished using line in bracket...");
                    }

               }// end else if (createdPrimarySegments.keySet().contains(nextLine.split("\\s+")[0]))
               else if (nextLine.startsWith("connect "))
               {
                   String[] words = nextLine.split("\\s+");

                   String childSectionName = words[1].substring(0,words[1].indexOf("("));
                   String parentSectionName = words[2].substring(0,words[2].indexOf("("));

                   float distAlongChild
                       = Float.parseFloat(words[1].substring(words[1].indexOf("(") + 1,
                                                             words[1].indexOf(")")));

                   float distAlongParentSection
                       = Float.parseFloat(words[2].substring(words[2].indexOf("(") + 1,
                                                             words[2].indexOf(")")));

                   if (distAlongChild != 0)
                   {
                       throw new MorphologyException(
                           "A child section can only be connected at the 0 end in this version of neuroConstruct.\nThis is the advised practice in NEURON Error in line: " + nextLine
                           + "\nof file: " + morphologyFile);
                   }

                   connect(childSectionName, parentSectionName, distAlongParentSection);

               }// end else if (nextLine.startsWith("connect "))

               else if (nextLine.startsWith("access "))
               {
                   String sectionToAccess = getBetterSectionName(nextLine.substring("access ".length()).trim());
                   
                   logger.logComment("Being told to access: " + sectionToAccess);

                   // will generally be the case...
                   if (latestCreatedSection!=null && latestCreatedSection.getSectionName().equals(sectionToAccess))
                   {
                       accessedSection = latestCreatedSection;
                   }
                   else
                   {
                       // slower check...
                       for (Section sec: cell.getAllSections())
                       {
                           if (sec.getSectionName().equals(sectionToAccess))
                               accessedSection = sec;
                       }
                   }
                   logger.logComment("Section accessed: " + accessedSection);
               }

               else if (nextLine.startsWith("nseg "))
               {
                   String val = nextLine.substring(nextLine.indexOf("=")+1).trim();
                   //if (val.index)
                   try
                   {
                       if (accessedSection!=null)
                           accessedSection.setNumberInternalDivisions(Integer.parseInt(val));
                   }
                   catch (NumberFormatException nfe)
                   {
                       GuiUtils.showWarningMessage(logger, "Unable to parse expression for nseg: "+ val, null);
                       
                   }

                   logger.logComment("Section accessed: " + accessedSection);
               }


               else if (accessedSection!=null)
               {
                   logger.logComment("          -----------    Using line num "
                                     + lineCount
                                     + ": " + nextLine
                                     + " for section: "
                                     + accessedSection.getSectionName());


                    Segment primarySegment = getFirstSegment(accessedSection.getSectionName());

                   useLineForSection(nextLine, primarySegment);

               }

               else
               {
                   logger.logComment("Ignored line: " + nextLine);
                   logger.logComment("Finished using line...");
                   ignoredLines.append(nextLine+"\n");
               }

           }

           if (lineCount == 0)
           {
               GuiUtils.showErrorMessage(logger, "Error. No lines found in file: " + morphologyFile, null, null);
               return null;
           }

           logger.logComment("-------------------------------------------------------------------");
           logger.logComment("Completed parsing all " + lineCount + " lines");
        }
        catch (IOException e)
        {
            GuiUtils.showErrorMessage(logger, "Error: "+e.getMessage(), e, null);
            return null;
        }
        if (moveSomaToOrigin)
        {

            Section somaSection = cell.getFirstSomaSegment().getSection();
            Vector3f translation = new Vector3f(-1*somaSection.getStartPointPositionX(),
                                                -1*somaSection.getStartPointPositionY(),
                                                -1*somaSection.getStartPointPositionZ());

            cell = CellTopologyHelper.translateAllPositions(cell, translation);
        }

        if (moveDendsToConnectionPoint)
        {

            cell = CellTopologyHelper.moveSectionsToConnPointsOnParents(cell);
        }

        logger.logComment("Completed parsing of file: "+morphologyFile);

        return cell;
    }

    private boolean isValidSection(String sectionName)
    {
        if (sectionName.equals("")) return false;
        
        Segment seg = createdFirstSegments.get(sectionName);
        logger.logComment("Got for "+sectionName+": " + seg);

        if (seg != null) return true;

        String alternateName = sectionName + "[0]";

        logger.logComment("Searching for "+alternateName+" in segs: " + this.createdFirstSegments.keySet());

        // try...
        if (createdFirstSegments.get(alternateName)!=null) return true;

        alternateName = sectionName + "_0";

        logger.logComment("Searching for "+alternateName+" in segs: " + this.createdFirstSegments.keySet());

        // try...
        if (createdFirstSegments.get(alternateName)!=null) return true;
        
        return (createdFirstSegments.get(getBetterSectionName(sectionName))!=null);

    }


    private Segment getFirstSegment(String sectionName)
    {
        Segment seg = createdFirstSegments.get(sectionName);
        logger.logComment("Got for getFirstSegment: "+sectionName+": " + seg);

        if (seg != null) return seg;

        String alternateName = sectionName + "[0]";

        logger.logComment("Searching for "+alternateName+", maybe  in segs: " + this.createdFirstSegments.keySet());

        // try...
        return createdFirstSegments.get(alternateName);
    }

    private Segment getLastAddedChildSegment(String sectionName)
    {
        Segment seg = this.lastAddedChildSegs.get(sectionName);
        if (seg != null) return seg;

        // try...
        return lastAddedChildSegs.get(sectionName + "[0]");
    }




    private static File getSimplifiedFile(File oldFile)
    {
        File simplifiedNeuronFile = new File(oldFile.getParent(),
                                             "simplified_" + oldFile.getName());


        NeuronFileConverter nfc = new NeuronFileConverter();
        try
        {


            String newStuff = nfc.convertNeuronFile(oldFile);
            FileWriter fw = new FileWriter(simplifiedNeuronFile);
            fw.write(newStuff);
            fw.close();

            GuiUtils.showInfoMessage(logger, "NEURON file simplified",
                                     "That NEURON file has been converted to a simplified format for importation.\nThe new version is at: " +
                                     simplifiedNeuronFile.getCanonicalPath(), null);
        }

        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger, "Problem converting that NEURON morphology file into a simpler format", ex, null);
            return null;
        }


/*
        System.out.println("Written new file to: " + simplifiedNeuronFile);
        Frame parentFrame = GuiUtils.getMainFrame();

        NeuronImportDialog dlg = new NeuronImportDialog(parentFrame);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dlg.getSize();

        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;

        dlg.setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

        dlg.setModal(true);

        dlg.show();

        NeuronMorphReader nmr = new NeuronMorphReader();
        cell = nmr.loadFromMorphologyFile(simplifiedNeuronFile,
                                          cellName,
                                          dlg.getMoveToOrigin(),
                                          dlg.getMoveDendrites());
*/
        return simplifiedNeuronFile;
    }


    public String getIgnoredLines()
    {
        return ignoredLines.toString();
    }


    /**
     * The line given will be applied to the section specified
     */
    private void useLineForSection(String line, Segment primarySegmentOfSection) throws MorphologyException
    {
        logger.logComment("Using line: ("+line+") for section: "+ primarySegmentOfSection.getSection().getSectionName());
        
        
        line = line.trim();
        Section section = primarySegmentOfSection.getSection();
        Segment newSegment = null;
        Segment lastChildSegment = this.getLastAddedChildSegment(primarySegmentOfSection.getSection().
                                                                        getSectionName());

        logger.logComment("Last child segs: " + this.lastAddedChildSegs.size());
        String sectionName = primarySegmentOfSection.getSection().getSectionName();
        logger.logComment("Primary section: "+ sectionName);

        if (lastChildSegment == null)
        {
            logger.logError("Problem with finding lastChildSegment of " +
                            primarySegmentOfSection.getSection().getSectionName());
            logger.logComment("Hashtable: " + lastAddedChildSegs.keySet());
            return;
        }
        if (line.startsWith(section.getSectionName()))
        {
            logger.logComment("Trimming of the section name...");
            line = line.substring(section.getSectionName().length()+1).trim();
            logger.logComment("Now the line is: "+ line);
        }


        if (line.startsWith("{") && line.endsWith("}"))
        {
            logger.logComment("Trimming the curly brackets...");
            line = line.substring(1, line.length()-1).trim();
            logger.logComment("Now the line is: "+ line);
        }

        String notYetUsedInstructions = null;

        if (line.startsWith("pt3dclear() "))
        {
            notYetUsedInstructions = line.substring("pt3dclear() ".length());
            line = "pt3dclear() ";
            logger.logComment("Now the line is: "+ line);
        }

        if (line.startsWith("pt3dadd("))
        {
            int indexOfNextPt3dadd  = line.indexOf("pt3dadd", 7);
            if (indexOfNextPt3dadd>0)
            {
                notYetUsedInstructions = line.substring(indexOfNextPt3dadd).trim();
                line = line.substring(0, indexOfNextPt3dadd).trim();
            }
            logger.logComment("Now the line is: "+ line);
        }



        if (line.startsWith("pt3dclear()"))
        {
            /** @todo Look into impl this */
            logger.logError("The function pt3dclear() isn't supported yet...");
        }
        if (line.startsWith("pt3dadd("))
        {
            logger.logComment("Found pt3dadd(");

            if (lastChildSegment.equals(primarySegmentOfSection) && // ie the first is the last => only
                section.getStartPointPositionX() == Float.MAX_VALUE &&
                section.getStartPointPositionY() == Float.MAX_VALUE &&
                section.getStartPointPositionZ() == Float.MAX_VALUE)
            {
                logger.logComment("Adding first 3d info point, i.e. for start of section");

                Point3f point = getPositionInfofromPt3dadd(line);

                section.setStartPointPositionX(point.x);
                section.setStartPointPositionY(point.y);
                section.setStartPointPositionZ(point.z);

                section.setStartRadius(getDiamInfofromPt3dadd(line) / 2f);
                //primarySegmentOfSection.setFirstSectionSegment(true);

            }
            else if (lastChildSegment.getSegmentName().equals(primarySegmentOfSection.getSegmentName()) &&
                     primarySegmentOfSection.getEndPointPositionX() == Float.MAX_VALUE &&
                     primarySegmentOfSection.getEndPointPositionY() == Float.MAX_VALUE &&
                     primarySegmentOfSection.getEndPointPositionZ() == Float.MAX_VALUE)
            {
                logger.logComment("Adding second 3d info point, i.e. for end of first segment");

                Point3f point = getPositionInfofromPt3dadd(line);

                primarySegmentOfSection.setEndPointPositionX(point.x);
                primarySegmentOfSection.setEndPointPositionY(point.y);
                primarySegmentOfSection.setEndPointPositionZ(point.z);
                primarySegmentOfSection.setRadius(getDiamInfofromPt3dadd(line) / 2f);

                primarySegmentOfSection.setSegmentId(indexOfValidSegments);

                // Swap them about so segs are in proper order (same as id)
                if (!cell.getAllSegments().elementAt(indexOfValidSegments).equals(primarySegmentOfSection))
                {
                    Vector<Segment> segs = cell.getAllSegments();
                    Segment segAtIndex = segs.elementAt(indexOfValidSegments);
                    int indexToSwap = segs.indexOf(primarySegmentOfSection);

                    segs.setElementAt(primarySegmentOfSection, indexOfValidSegments);

                    segs.setElementAt(segAtIndex, indexToSwap);

                }
                indexOfValidSegments++;

                logger.logComment("First section segment now: " + primarySegmentOfSection);

            }
            else
            {
                logger.logComment("Adding 2+th info point, i.e. for end of non first segment");

                Point3f point = getPositionInfofromPt3dadd(line);

                String newSegmentName = null;

                // if name of last segment was dend__6 call new one dend__7
                if (lastChildSegment.getSegmentName().indexOf("__") >= 0)
                {
                    String oldNum = lastChildSegment.getSegmentName().substring(lastChildSegment.getSegmentName().
                        indexOf("__") + 2);
                    int oldNumInt = Integer.parseInt(oldNum);
                    newSegmentName = lastChildSegment.getSegmentName().substring(0,
                        lastChildSegment.getSegmentName().indexOf("__"))
                        + "__" + (oldNumInt + 1);
                }
                else
                {
                    newSegmentName = lastChildSegment.getSegmentName() + "__0";
                }

                if (primarySegmentOfSection.isSomaSegment())
                {
                    logger.logComment("Adding info for a new soma segment");
                    logger.logComment("Creating...");

                    newSegment = cell.addSomaSegment(getDiamInfofromPt3dadd(line) / 2f,
                                                     newSegmentName,
                                                     point,
                                                     lastChildSegment,
                                                     section);

                    logger.logComment("Done creating...");

                    newSegment.setSegmentId(indexOfValidSegments);

                    // Swap them about so segs are in proper order (same as id)
                    if (!cell.getAllSegments().elementAt(indexOfValidSegments).equals(primarySegmentOfSection))
                    {

                        Vector<Segment> segs = cell.getAllSegments();
                        Segment segAtIndex = segs.elementAt(indexOfValidSegments);
                        int indexToSwap = segs.indexOf(newSegment);
                        cell.getAllSegments().setElementAt(newSegment, indexOfValidSegments);
                        cell.getAllSegments().setElementAt(segAtIndex, indexToSwap);
                    }
                    indexOfValidSegments++;
                    logger.logComment("Done updating ids");

                }
                else
                {
                    logger.logComment("Adding info for new dend segment");
                    
                    boolean inheritRadius = true;
                    if (lastChildSegment!=null && lastChildSegment.isSpherical()) inheritRadius=false;

                    newSegment = cell.addDendriticSegment(getDiamInfofromPt3dadd(line) / 2f,
                                                          newSegmentName,
                                                          point,
                                                          lastChildSegment,
                                                          1,
                                                          section.getSectionName(),
                                                          inheritRadius);

                    newSegment.setSegmentId(indexOfValidSegments);
                    // Swap them about so segs are in proper order (same as id)
                    if (!cell.getAllSegments().elementAt(indexOfValidSegments).equals(primarySegmentOfSection))
                    {
                        Vector<Segment> segs = cell.getAllSegments();
                        Segment segAtIndex = cell.getAllSegments().elementAt(indexOfValidSegments);
                        int indexToSwap = cell.getAllSegments().indexOf(newSegment);
                        cell.getAllSegments().setElementAt(newSegment, indexOfValidSegments);
                        segs.setElementAt(segAtIndex, indexToSwap);
                    }
                    indexOfValidSegments++;
                }

                logger.logComment("Created new section: " + newSegment);

                if (newSegment != null)
                {
                    logger.logComment("Replacing..");
                    lastAddedChildSegs.put(new String(sectionName), newSegment);

                    logger.logComment("Done put...");
                }

            }

        } // end if (line.startsWith("pt3dadd("))

        else if (line.startsWith("connect "))
        {
            logger.logComment("connect... " + line);

            String childSectionName = line.substring("connect ".length(), line.indexOf("(")).trim();
            
            childSectionName = getBetterSectionName(childSectionName);

            if (childSectionName.indexOf("(")>=0)
                childSectionName = childSectionName.substring(0,childSectionName.indexOf("("));


            float distAlongChild
                = Float.parseFloat(line.substring(line.indexOf("(") + 1,
                                                      line.indexOf(")")));

            if (distAlongChild != 0)
            {
                throw new MorphologyException("A child section can only be connected at the 0 end in this version of neuroConstruct.\nThis is the advised practice in NEURON Error in line: " + line
                                              + "\nof file: " + morphologyFile);
            }

            float distAlongParentSection = Float.parseFloat(line.substring(line.indexOf(",")+1).trim());

            connect(childSectionName, section.getSectionName(), distAlongParentSection);

        } //  if (line.startsWith("connect ")))

        else if (line.startsWith("nseg"))
        {
            String nsegNum = line.substring(line.indexOf("=") + 1).trim();
            section.setNumberInternalDivisions(Integer.parseInt(nsegNum));
        }
        else if (line.trim().length() == 0)
        {
            logger.logComment("Blank line");
        }
        else
        {
            logger.logComment("Not found anything to do with the line...");
            ignoredLines.append("(Regarding section "+ section.getSectionName()+
                                ") "+ line);
        }


        if (notYetUsedInstructions!=null)
        {
            useLineForSection(notYetUsedInstructions, primarySegmentOfSection);
        }

    }


    private void connect(String childSectionName,
                         String parentSectionName,
                         float distAlongParentSection)
    {
        childSectionName = getBetterSectionName(childSectionName);
        parentSectionName = getBetterSectionName(parentSectionName);        
        
        Segment firstSegOfChildSection = getFirstSegment(childSectionName);
        Segment firstSegOfParentSection = getFirstSegment(parentSectionName);
        Segment lastSegOfParentSection = this.getLastAddedChildSegment(parentSectionName);

        logger.logComment("Going to connect child: "+ childSectionName
                          + " to point at dist: "
                          + distAlongParentSection
                          + " along parent: "+ parentSectionName);

        logger.logComment("firstSegOfChildSection: " + firstSegOfChildSection);
        logger.logComment("firstSegOfParentSection: " + firstSegOfParentSection);
        logger.logComment("lastSegOfParentSection: " + lastSegOfParentSection);

        if (distAlongParentSection == 1)
        {
            logger.logComment("Connecting to top");
            firstSegOfChildSection.setParentSegment(lastSegOfParentSection);
            firstSegOfChildSection.setFractionAlongParent(1);
            //firstSegOfChildSection.getSection().setFractionAlongParentSection(1);
        }
        else if (distAlongParentSection == 0)
        {
            logger.logComment("Connecting to bottom");
            firstSegOfChildSection.setParentSegment(firstSegOfParentSection);
            firstSegOfChildSection.setFractionAlongParent(0);
            //firstSegOfChildSection.getSection().setFractionAlongParentSection(0);

        }
        else
        {
            logger.logComment("Connecting to middle, fract along: "+distAlongParentSection);

            float totalLengthParentSection = 0;
            Segment currentParentSectionSegment = lastSegOfParentSection;

            while (currentParentSectionSegment != null &&
                   (/*currentParentSectionSegment.getParentSegment() == null ||*/
                    currentParentSectionSegment.getSection().getSectionName().equals(parentSectionName)))
            {
                logger.logComment("Adding length of: "
                                  + currentParentSectionSegment.getSegmentName()
                                  + ", length: "
                                  + currentParentSectionSegment.getSegmentLength());

                totalLengthParentSection = totalLengthParentSection +
                    currentParentSectionSegment.getSegmentLength();

                currentParentSectionSegment = currentParentSectionSegment.getParentSegment();

                logger.logComment("Nxt parent: "+ currentParentSectionSegment);
            }

            logger.logComment("Total length of parent section: " + totalLengthParentSection);

            float lengthAlongToConnect = (1 - distAlongParentSection) * totalLengthParentSection;
            // since we're starting at the last section and working back...

            logger.logComment("Distance from end of parent section to connect: " + lengthAlongToConnect);

            float lengthTraversed = 0;

            currentParentSectionSegment = lastSegOfParentSection;

            while (lengthTraversed < lengthAlongToConnect &&
                   currentParentSectionSegment != null &&
                   (currentParentSectionSegment.getParentSegment() == null ||
                    currentParentSectionSegment.getParentSegment().getSection().getSectionName().equals(parentSectionName)))
            {
                lengthTraversed = lengthTraversed + currentParentSectionSegment.getSegmentLength();
                logger.logComment("Traversed: " + lengthTraversed + " at segment: " +
                                  currentParentSectionSegment.getSegmentName());

                if (lengthTraversed >= lengthAlongToConnect)
                {
                    logger.logComment("Traversed enough...");

                    firstSegOfChildSection.setParentSegment(currentParentSectionSegment);

                    float fractAlong = (lengthTraversed - lengthAlongToConnect) / currentParentSectionSegment.getSegmentLength();
                    firstSegOfChildSection.setFractionAlongParent(fractAlong);

                    logger.logComment("Parent: " + firstSegOfChildSection.getParentSegment());
                    logger.logComment("fractAlong: " + fractAlong);

                }
                currentParentSectionSegment = currentParentSectionSegment.getParentSegment();
            }
            //firstSegOfChildSection.getSection().setFractionAlongParentSection(distAlongParentSection);

            logger.logComment("firstSegOfChildSection: "+firstSegOfChildSection);

        }

    }

    private static Point3f getPositionInfofromPt3dadd(String line)
    {
        Point3f point = new Point3f();
        int startIndexX = line.indexOf("(")+1;
        int startIndexY = line.indexOf(",", startIndexX)+1;
        int startIndexZ = line.indexOf(",", startIndexY)+1;
        int startIndexD = line.indexOf(",", startIndexZ)+1;
        String xPos = line.substring(startIndexX, startIndexY-1).trim();
        String yPos = line.substring(startIndexY, startIndexZ-1).trim();
        String zPos = line.substring(startIndexZ, startIndexD-1).trim();
        if (xPos.indexOf("abs(")>=0) xPos = xPos.substring("abs(".length(), xPos.indexOf(")"));
        if (yPos.indexOf("abs(")>=0) yPos = yPos.substring("abs(".length(), yPos.indexOf(")"));
        if (zPos.indexOf("abs(")>=0) zPos = zPos.substring("abs(".length(), zPos.indexOf(")"));

        point.x =Float.parseFloat(xPos);
        point.y =Float.parseFloat(yPos);
        point.z =Float.parseFloat(zPos);

        return point;
    }

    private static float getDiamInfofromPt3dadd(String line)
    {
       
        int startIndexX = line.indexOf("(")+1;
        int startIndexY = line.indexOf(",", startIndexX)+1;
        int startIndexZ = line.indexOf(",", startIndexY)+1;
        int startIndexD = line.indexOf(",", startIndexZ)+1;
        int endIndexD = line.lastIndexOf(")");
        String diam = line.substring(startIndexD, endIndexD).trim();

        if (diam.indexOf("abs(")>=0) diam = diam.substring("abs(".length(), diam.indexOf(")"));


        return Math.abs(Float.parseFloat(diam));
    }




    public static void main(String[] args)
    {
        //File neuronFile = new File("projects/Project_1/pyramid.nrn");

        //File neuronFile = new File("C:\\Documents and Settings\\padraig\\Desktop\\Datas\\Datas\\ub\\simp.hoc");

        //File neuronFile = new File("Y:\\Padraig\\neuron\\examples\\Disjointed\\triang.hoc");
        //File neuronFile = new File("C:\\neuroConstruct\\projects\\Project_1\\10-3.nrn");
        //File neuronFile = new File("C:\\neuroConstruct\\projects\\Project_1\\gran.hoc");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\testFormats\\test.hoc");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\testFormats\\simple.hoc");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\spikeinit\\dks577a.hoc");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\anderson\\anderson\\Cells\\meynert2\\meynert2-.hoc");
        //File neuronFile = new File("C:\\nrn54\\PatTest\\sexions\\morph.hoc");

        //File newFile = new File("C:\\nrn54\\Morphology\\testFormats\\generated.nrn");

        //File newFile = new File("C:\\Documents and Settings\\padraig\\Desktop\\Datas\\Datas\\ub\\simp.hoc");
        File newFile = new File("../temp/hh.hoc");

        try
        {
            /*
            //GeneralProperties.setLogFilePrintToScreenPolicy(false);
            NeuronFileConverter nfc = new NeuronFileConverter();
            String newStuff = nfc.convertNeuronFile(neuronFile);
            FileWriter fw = new FileWriter(newFile);
            fw.write(newStuff);
            fw.close();

            System.out.println("Written new file to: "+ newFile);*/

            NeuronMorphReader nmr = new NeuronMorphReader();
            Cell cell = nmr.loadFromMorphologyFile(newFile, "FunnyCell", false, false);



            System.out.println(CellTopologyHelper.printDetails(cell, null));


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
