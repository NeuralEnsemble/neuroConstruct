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

import java.beans.*;
import java.io.*;
import java.util.*;

import javax.vecmath.*;
import javax.xml.parsers.*;
import org.neuroml.model.util.NeuroMLElements;

import org.xml.sax.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.DistalPref;
import ucl.physiol.neuroconstruct.cell.ParameterisedGroup.ProximalPref;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.neuroml.NeuroMLConstants.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import ucl.physiol.neuroconstruct.utils.xml.*;

/**
 *
 * A class for importing XML morphology files, and creating Cells
 * which can be used by the rest of the application
 *
 * @author Padraig Gleeson
 *  
 *
 */


@SuppressWarnings("UseOfObsoleteCollectionType")
public class MorphMLConverter extends FormatImporter
{
    static ClassLogger logger = new ClassLogger("MorphMLConverter");

    //public static String MML_ROOT = "cells";
    //public static String MML_CELLS = "cells";
    
    String warning = "";

    /*
     * If true use the elements with pre 1.7.1 naming conventions, e.g. passiveConductance not
     */
    private static final boolean usePreV1_7_1Format = false;

    private static int preferredExportUnits;
    
    private static Units voltUnit;
    private static Units concUnits;
    
    static {
        setPreferredExportUnits(UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);
    }

    public MorphMLConverter()
    {
        super("NeuroMLConverter",
                            "Importer of NeuroML (Level 1/2/3) files containing a single cell",
                            new String[]{".xml", ".mml"});
        
    }


    public static Cell loadFromJavaXMLFile(File javaXMLFile) throws MorphologyException
    {
        try
        {
            //logger.logComment("-----   Starting decoding java ...");
            //GeneralUtils.timeCheck("-----   Starting decoding java xml morph...");
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(javaXMLFile)));

            Cell cell = (Cell) decoder.readObject();
            decoder.close();
            //GeneralUtils.timeCheck("-----   Finished decoding java xml morph...");
            return cell;
        }
        catch (FileNotFoundException e)
        {
            throw new MorphologyException("Problem converting the morphology file: "+ javaXMLFile, e);
        }
    }
    
    @Override
    public String getWarnings()
    {
        return this.warning;
    }

    public static int getPreferredExportUnits()
    {
        return preferredExportUnits;
    }

    public static void setPreferredExportUnits(int prefUnits)
    {
        preferredExportUnits = prefUnits;
        voltUnit = UnitConverter.voltageUnits[preferredExportUnits];
        concUnits= UnitConverter.concentrationUnits[preferredExportUnits];
    }


    public static Cell loadFromJavaObjFile(File objFile) throws MorphologyException
    {
        String error = "Problem converting the morphology file: "+ objFile+
                "\n\nThis may be due to incompatibilities between the version of neuroConstruct used to save the morphology file and the current version.\n\n"+
                "One possible solution is to open the project with the previous version, go to Settings -> Project Properties and change the save format to Java XML.\n" +
                "Save the project, and reload it in the new neuroConstruct version.\n";
        try
        {
            //GeneralUtils.timeCheck("-----   Starting decoding java obj morph...");
            FileInputStream fi = new FileInputStream(objFile);
            ObjectInputStream si = new ObjectInputStream(fi);

            Cell cell = null;
            try
            {
                cell = (Cell) si.readObject();
            }
            catch (InvalidClassException e)
            {
                logger.logComment("Cell details: "+ CellTopologyHelper.printDetails(cell, null));
                GuiUtils.showErrorMessage(logger, error, e, null);
            }
            si.close();
            //GeneralUtils.timeCheck("-----   Finished decoding java obj morph...");
            return cell;
        }
        catch (IOException e)
        {
            throw new MorphologyException(error, e);
        } 
        catch (ClassNotFoundException e) 
        {
            throw new MorphologyException(error, e);
        }
    }


    public Cell loadFromMorphologyFile(File morphologyFile, String name) throws MorphologyException
    {
        logger.logComment("-----   Starting decoding...");

        logger.logComment("MorphML File " + morphologyFile + " being used to create cell: " + name);

        try
        {
            logger.logComment("Loading mml cell from " + morphologyFile.getAbsolutePath());

            FileInputStream instream;
            InputSource is;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            MorphMLReader mmlBuilder = new MorphMLReader();
            xmlReader.setContentHandler(mmlBuilder);

            instream = new FileInputStream(morphologyFile);

            is = new InputSource(instream);

            xmlReader.parse(is);

            Cell cell = mmlBuilder.getBuiltCell();

            if (name != null)
                cell.setInstanceName(name);

            logger.logComment("Cell which has been built: ");
            logger.logComment(cell+"");
            logger.logComment(CellTopologyHelper.printShortDetails(cell));

            logger.logComment("-----   Finished decoding...");
            
            this.warning = mmlBuilder.getWarnings();
            
            return cell;
        }
        catch (IOException e)
        {
            throw new MorphologyException("IO problem when converting the morphology file: " + morphologyFile, e);
        } 
        catch (ParserConfigurationException e) 
        {
            throw new MorphologyException("Parse problem when converting the morphology file: " + morphologyFile, e);
        } 
        catch (SAXException e) 
        {
            throw new MorphologyException("Problem converting the morphology file: " + morphologyFile, e);
        }

    }

    private static boolean saveObjectJavaXML(Object obj, File xmlFile) throws FileNotFoundException, IOException
    {
        File tmpXml = new File(xmlFile.getAbsolutePath()+".tmp");

        logger.logComment("Saving morphology to: "+ tmpXml);

        FileOutputStream fos = new FileOutputStream(tmpXml);
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(fos));

        e.flush();

        String comment = "\n<!-- Note that this XML is specific to the neuroConstruct Java cell object model and not any part of the NeuroML framework -->\n\n";

        fos.write(comment.getBytes());

        e.flush();

        e.writeObject(obj);

        e.close();

        logger.logComment("Closed "+tmpXml);

        return tmpXml.renameTo(xmlFile);

    }


    private static boolean saveObjectJava(Object obj, File objFile) throws FileNotFoundException, IOException
    {
        File tmpObj = new File(objFile.getAbsolutePath()+".tmp");

        logger.logComment("Saving morphology to: "+ tmpObj);

        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmpObj)));

        oos.writeObject(obj);

        oos.close();

        return tmpObj.renameTo(objFile);

    }





    public static boolean saveCellInJavaXMLFormat(Cell cell, File javaXMLFile) throws MorphologyException
    {

        boolean success = false;
        if (javaXMLFile.exists())
        {
            File backupFile = new File(javaXMLFile.getAbsolutePath()+".bak");
            logger.logComment("Backup file: " + backupFile+" exists: "+ backupFile.exists());
            backupFile.delete();
            success = javaXMLFile.renameTo(backupFile);

            if (!success)
            {
                logger.logComment("Problem backing up "+javaXMLFile+" as "+backupFile);
            }
            else
            {
                logger.logComment("Backed up old morph to: " + backupFile);
            }
        }

        try
        {
            logger.logComment("Saving to file: " + javaXMLFile);
            success = saveObjectJavaXML(cell, javaXMLFile);
            logger.logComment("Saved to file.");

        }
        catch (FileNotFoundException ex)
        {
            throw new MorphologyException(javaXMLFile.getAbsolutePath(), "Problem saving Java XML file", ex);
        }

        catch (IOException ex)
        {
            throw new MorphologyException(javaXMLFile.getAbsolutePath(), "Problem writing to Java XML file", ex);
        }
        return success;

    }


    public static boolean saveCellInJavaObjFormat(Cell cell, File javaObjFile) throws MorphologyException
    {
        boolean success = false;
        if (javaObjFile.exists())
        {
            File backupFile = new File(javaObjFile.getAbsolutePath()+".bak");
            //success = javaObjFile.renameTo(backupFile);
            logger.logComment("Backup file: " + backupFile+" exists: "+ backupFile.exists());
            backupFile.delete();
            success = javaObjFile.renameTo(backupFile);

            if (!success)
            {
                logger.logComment("Problem backing up "+javaObjFile+" as "+backupFile);
                //return false;
            }
        }
        try
        {
            logger.logComment("Saving to file: " + javaObjFile);
            success = saveObjectJava(cell, javaObjFile);
            logger.logComment("Saved to file.");

        }
        catch (FileNotFoundException ex)
        {
            throw new MorphologyException(javaObjFile.getAbsolutePath(), "Problem saving Java ObjectOutputStream file", ex);
        }
        catch (IOException ex)
        {
            throw new MorphologyException(javaObjFile.getAbsolutePath(), "Problem saving Java ObjectOutputStream file", ex);
        }
        return success;
    }
    
    private static String getNML2SafeId(String id)
    {
        return id.replaceAll("\\[", "_").replaceAll("\\]", "_");
    }


    public static SimpleXMLElement getCellXMLElement(Cell cell, Project project, NeuroMLLevel level, NeuroMLVersion version) throws NeuroMLException, CMLMechNotInitException, XMLMechanismException
    {
            String mmlPrefix = "";
            if (!level.equals(NeuroMLLevel.NEUROML_LEVEL_1))
                mmlPrefix = MorphMLConstants.PREFIX + ":";
        
            String metadataPrefix = MetadataConstants.PREFIX + ":";

            boolean nml2 = version.isVersion2();

            if (nml2)
            {
                mmlPrefix = "";
                metadataPrefix = "";
            }
            
            SimpleXMLElement descElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.NOTES_ELEMENT);
            
            descElement.addContent("This "+level+" NeuroML file (specification version "
                    +GeneralProperties.getNeuroMLVersionNumber()
                    +") has been generated by neuroConstruct v"+GeneralProperties.getVersionNumber());
            
            SimpleXMLElement cellElement = new SimpleXMLElement(MorphMLConstants.CELL_ELEMENT);
            //cellsElement.addChildElement(cellElement);

            String nameIdAttr = MorphMLConstants.CELL_NAME_ATTR;

            if (nml2)
            {
                nameIdAttr = NeuroMLConstants.NEUROML_ID_V2;
            }
            SimpleXMLAttribute nameAttr = new SimpleXMLAttribute(nameIdAttr, cell.getInstanceName());
            
            cellElement.addAttribute(nameAttr);

            cellElement.addContent("\n\n        ");

            if (!nml2) cellElement.addContent("    ");

            if (cell.getCellDescription()!=null)
            {
                descElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.NOTES_ELEMENT);
                String desc = GeneralUtils.replaceAllTokens(cell.getCellDescription(), "&", "and");
                
                
                if (nml2)
                {
                    String descCut = NeuroMLFileManager.parseDescriptionForMetadata(cell.getCellDescription(), cellElement);
                    descElement.addContent(descCut);
                }
                else
                {
                    descElement.addContent(desc);
                }
                cellElement.addChildElement(descElement);
                cellElement.addContent("\n\n        ");
            }

            if (!nml2) cellElement.addContent("    ");
            
            SimpleXMLElement segmentParentElement;

            //String extraIndent = "";

            if (!nml2)
            {
                SimpleXMLElement segsElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.SEGMENTS_ELEMENT);
                cellElement.addChildElement(segsElement);
                segmentParentElement = segsElement;
                segsElement.addContent("\n\n                ");
            }
            else
            {
                SimpleXMLElement morphologyElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.MORPHOLOGY_V2);
                morphologyElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, "morphology_"+cell.getInstanceName()));
                cellElement.addChildElement(morphologyElement);
                segmentParentElement = morphologyElement;
                morphologyElement.addContent("\n\n            ");
            }


            Vector allSegments = cell.getAllSegments();
            ArrayList<Section> allSections = cell.getAllSections();

            String extraInd = "";
            if (!nml2) extraInd = "    ";


            for (int i = 0; i < allSegments.size(); i++)
            {
                Segment nextSegment = (Segment) allSegments.elementAt(i);
                SimpleXMLElement segmentElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.SEGMENT_ELEMENT);

                segmentParentElement.addChildElement(segmentElement);
                segmentParentElement.addContent("\n\n            "+extraInd);

                segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_ID_ATTR,
                                                                   nextSegment.getSegmentId() + ""));

                String id = nextSegment.getSegmentName();
                if (nml2)
                    id = getNML2SafeId(id);
                
                segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_NAME_ATTR,
                                                                   id));

                if (nextSegment.getParentSegment() != null)
                {
                    if (!nml2)
                    {
                        segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_PARENT_ATTR,
                                                                       nextSegment.getParentSegment().getSegmentId() + ""));
                    }
                    else
                    {
                        SimpleXMLElement parentElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.PARENT_V2);
                        parentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_V2,
                                                                       nextSegment.getParentSegment().getSegmentId() + ""));

                        if (nextSegment.getFractionAlongParent()!=1)
                        {
                            parentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.PARENT_FRACT_ALONG_V2,
                                                                       Math.min(1, nextSegment.getFractionAlongParent()) + ""));
                        }
                        segmentElement.addContent("\n                "); // to make it more readable...
                        segmentElement.addChildElement(parentElement);
                    }
                }

                int sectionId = allSections.indexOf(nextSegment.getSection());

                if (!nml2)
                {
                    segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_CABLE_ID_ATTR,
                                                                       sectionId + ""));
                }

                if (nextSegment.isFirstSectionSegment())
                {
                    segmentElement.addContent("\n                "+extraInd); // to make it more readable...

                    segmentElement.addChildElement(getPointElement(nextSegment.getStartPointPosition(),
                                                                   nextSegment.getSegmentStartRadius(),
                                                                   mmlPrefix+
                                                                   MorphMLConstants.SEGMENT_PROXIMAL_ELEMENT));

                }
                segmentElement.addContent("\n                "+extraInd); // to make it more readable...

                segmentElement.addChildElement(getPointElement(nextSegment.getEndPointPosition(),
                                                               nextSegment.getRadius(),
                                                               mmlPrefix+
                                                               MorphMLConstants.SEGMENT_DISTAL_ELEMENT));


                SimpleXMLElement props = null;


                if (nextSegment.getComment()!=null && !nml2)
                {
                    if (props == null)
                    {
                        props = new SimpleXMLElement(mmlPrefix+MorphMLConstants.PROPS_ELEMENT);
                        segmentElement.addContent("    "); // to make it more readable...
                        segmentElement.addChildElement(props);
                        segmentElement.addContent("\n            "); // to make it more readable...
                        props.addContent("\n                    "); // to make it more readable...
                    }

                    MetadataConstants.addProperty(props,
                                                 MorphMLConstants.COMMENT_PROP,
                                                 nextSegment.getComment(), 
                                                 "                    ",
                                                 version);
                    
                    props.addContent("\n                "); // to make it more readable...

                }

                if (nextSegment.isFiniteVolume() && !nextSegment.isSomaSegment())
                {
                    if (props == null)
                    {
                        props = new SimpleXMLElement( /*metadataPrefix + */mmlPrefix + MorphMLConstants.PROPS_ELEMENT);
                        segmentElement.addContent("    "); // to make it more readable...
                        segmentElement.addChildElement(props);
                        props.addContent("\n                        "); // to make it more readable...
                    }

                    MetadataConstants.addProperty(props,
                                                  MorphMLConstants.FINITE_VOL_PROP,
                                                  nextSegment.isFiniteVolume()+"",
                                                  "                        ",
                                                  version);
                }


                segmentElement.addContent("\n            "+extraInd); // to make it more readable...


            }

            if (!nml2) segmentParentElement.addContent("\n        "+extraInd); // to make it more readable...

            SimpleXMLElement cabSegGroupParentElement = null;

            
            if (!nml2)
            {
                SimpleXMLElement cablesElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLES_ELEMENT);
                cellElement.addContent("\n\n            ");
                cellElement.addChildElement(cablesElement);
                cabSegGroupParentElement = cablesElement;
            }
            
            boolean useCableGroup  = false;
            
            if (cell.getParameterisedGroups().size()>0 && !nml2)
                useCableGroup  = true;

            for (int i = 0; i < allSections.size(); i++)
            {
                Section nextSection =  allSections.get(i);

                if (nml2)
                {
                    
                    SimpleXMLElement segGroupElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.SEG_GROUP_V2);
                    String id = nextSection.getSectionName();
                    if (nml2)
                        id = getNML2SafeId(id);

                    segGroupElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, id));
                    
                    if (!version.isVersion2alpha())
                    {
                        // Indicates that it's a Section/cable
                        segGroupElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML2_NEUROLEX_ID, 
                                NeuroMLConstants.NEUROML2_NEUROLEX_UNBRANCHED_NONOVERLAPPING_SEG_GROUP));
                        segGroupElement.addComment("\n                This group contains an unbranched set of segments, and all of the segmentGroups marked with"
                                + "\n                "
                                + NeuroMLConstants.NEUROML2_NEUROLEX_ID+ " = "+NeuroMLConstants.NEUROML2_NEUROLEX_UNBRANCHED_NONOVERLAPPING_SEG_GROUP+""
                                + " form a non-overlapping set of all of the segments. \n                "
                                + "These segmentGroups correspond to the 'cables' of NeuroML v1.8.1. ");
                    }
                    
                    
                    if (nextSection.getNumberInternalDivisions()!=1)
                    {
                        segGroupElement.addContent("\n            ");
                        segGroupElement.addComment("This 'Section' has number of internal divisions (nseg) = "+nextSection.getNumberInternalDivisions());
                        
                        MetadataConstants.addProperty(segGroupElement,
                                                  MorphMLConstants.NUMBER_INTERNAL_DIVS_PROP_V2,
                                                  nextSection.getNumberInternalDivisions()+"",
                                                  "                ",
                                                  version);
                        
                    }
                    
                    
                    segmentParentElement.addChildElement(segGroupElement);

                    for (int p = 0; p < allSegments.size(); p++)
                    {
                        Segment nextSegment = (Segment) allSegments.elementAt(p);

                        if (nextSegment.getSection().getSectionName().equals(nextSection.getSectionName()))
                        {
                            SimpleXMLElement memberElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.MEMBER_V2);
                            memberElement.addAttribute(MorphMLConstants.SEGMENT_V2, nextSegment.getSegmentId()+"");
                            segGroupElement.addContent("\n                "); // to make it more readable...
                            segGroupElement.addChildElement(memberElement);
                            segGroupElement.addContent("\n            "); // to make it more readable...

                        }
                    }
                    
                    
                    segmentParentElement.addContent("\n\n            "); // to make it more readable...
                }
                else
                {
                    SimpleXMLElement cableElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLE_ELEMENT);

                    cabSegGroupParentElement.addContent("\n\n                ");
                    cabSegGroupParentElement.addChildElement(cableElement);

                    cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_ID_ATTR, i + ""));
                    cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_NAME_ATTR, nextSection.getSectionName()));

                    SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" + MorphMLConstants.PROPS_ELEMENT);

                    if (nextSection.getNumberInternalDivisions()!=1)
                    {
                        cableElement.addContent("\n                    "); // to make it more readable...
                        cableElement.addChildElement(props);
                        props.addContent("\n                        "); // to make it more readable...

                        MetadataConstants.addProperty(props,
                                    MorphMLConstants.NUMBER_INTERNAL_DIVS_PROP,
                                    nextSection.getNumberInternalDivisions() + "",
                                    "                    ",
                                    version);
                        
                        props.addContent("\n                    "); // to make it more readable...
                        cableElement.addContent("\n                "); // to make it more readable...
                    }

                    if (nextSection.getComment() != null)
                    {
                        if (props == null)
                        {
                            props = new SimpleXMLElement( /*metadataPrefix + */mmlPrefix + MorphMLConstants.PROPS_ELEMENT);
                            cableElement.addContent("    "); // to make it more readable...
                            cableElement.addChildElement(props);
                            props.addContent("\n                        "); // to make it more readable...
                        }

                        MetadataConstants.addProperty(props,
                                    MorphMLConstants.COMMENT_PROP,
                                    nextSection.getComment(),
                                    "                        ",
                                    version);

                    }

                    if (!useCableGroup)
                    {
                        Vector groups = nextSection.getGroups();
                        for (int j = 0; j < groups.size(); j++)
                        {
                            SimpleXMLElement grpElement = new SimpleXMLElement(metadataPrefix+ MetadataConstants.GROUP_ELEMENT);

                            grpElement.addContent( (String) groups.elementAt(j));
                            cableElement.addContent("\n                    "); // to make it more readable...

                            cableElement.addChildElement(grpElement);
                        }
                        cableElement.addContent("\n                "); // to make it more readable...
                    }
                    else
                    {
                        if (i < allSections.size()-1)
                            cabSegGroupParentElement.addContent("\n            "); // to make it more readable...
                        else
                            cabSegGroupParentElement.addContent("\n        "); // to make it more readable...

                    }

                    Segment firstSeg = cell.getAllSegmentsInSection(nextSection).getFirst();

                    if (firstSeg.getFractionAlongParent()!=1)
                    {
                        float fractAlongParentSec = CellTopologyHelper.getFractionAlongSection(cell, firstSeg.getParentSegment(), firstSeg.getFractionAlongParent());

                        if (usePreV1_7_1Format)
                            cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.FRACT_ALONG_PARENT_ATTR_pre_v1_7_1, fractAlongParentSec+""));
                        else
                            cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.FRACT_ALONG_PARENT_ATTR, fractAlongParentSec+""));

                    }
                }

            }
            
            if (cabSegGroupParentElement!=null)
                cabSegGroupParentElement.addContent("\n\n            ");

            if (nml2)
            {
                Hashtable<String, SimpleXMLElement> segGroupElsVsGroupNames = new Hashtable<String, SimpleXMLElement>();

                for (Section sec: allSections)
                {
                    for(String group: sec.getGroups())
                    {
                        String groupId = getNML2SafeId(group);
                        if (!segGroupElsVsGroupNames.containsKey(groupId))
                        {
                            SimpleXMLElement segGroupElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.SEG_GROUP_V2);
                            
                            segGroupElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, groupId));
                            segGroupElsVsGroupNames.put(groupId, segGroupElement);
                            
                            if (!version.isVersion2alpha())
                            {
                                if (group.equals(Section.SOMA_GROUP)) {
                                    segGroupElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML2_NEUROLEX_ID, 
                                        NeuroMLConstants.NEUROML2_NEUROLEX_SOMA_GROUP));
                                    segGroupElement.addComment("Soma group");
                                }
                                if (group.equals(Section.DENDRITIC_GROUP)) {
                                    segGroupElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML2_NEUROLEX_ID, 
                                        NeuroMLConstants.NEUROML2_NEUROLEX_DENDRITE_GROUP));
                                    segGroupElement.addComment("Dendrite group");
                                }
                                if (group.equals(Section.AXONAL_GROUP)) {
                                    segGroupElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML2_NEUROLEX_ID, 
                                        NeuroMLConstants.NEUROML2_NEUROLEX_AXON_GROUP));
                                    segGroupElement.addComment("Axon group");
                                }
                            }
                                

                            segmentParentElement.addChildElement(segGroupElement);
                            segmentParentElement.addContent("\n\n            "); // to make it more readable...

                        }
                        SimpleXMLElement segGroupElement = segGroupElsVsGroupNames.get(groupId);

                        SimpleXMLElement includeElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INCLUDE_V2);
                        String id = getNML2SafeId(sec.getSectionName());
                        
                        includeElement.addAttribute(MorphMLConstants.SEG_GROUP_V2, id);
                        segGroupElement.addContent("\n                "); // to make it more readable...
                        segGroupElement.addChildElement(includeElement);
                        
                        
                    }
                }
                for (String group: segGroupElsVsGroupNames.keySet())
                {
                    SimpleXMLElement segGroupElement = segGroupElsVsGroupNames.get(group);

                    for(ParameterisedGroup pg: cell.getParameterisedGroups())
                    {
                        if (pg.getGroup().equals(group))
                        {
                            SimpleXMLElement inhomoElement = new SimpleXMLElement(MorphMLConstants.INHOMO_PARAM_V2);
                            inhomoElement.addAttribute(MorphMLConstants.INHOMO_PARAM_ID_ATTR_V2, pg.getName());
                            inhomoElement.addAttribute(MorphMLConstants.INHOMO_PARAM_VARIABLE_ATTR_V2, "p");
                            inhomoElement.addAttribute(MorphMLConstants.INHOMO_PARAM_METRIC_ATTR_V2, pg.getMetric().toString());
                                

                            if (pg.getProximalPref().equals(ProximalPref.MOST_PROX_AT_0))
                            {
                                SimpleXMLElement proximal = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INHOMO_PARAM_PROXIMAL);
                                proximal.addAttribute(MorphMLConstants.INHOMO_PARAM_PROXIMAL_TRANS_START_ATTR, "0");

                                inhomoElement.addContent("\n                    "); // to make it more readable...
                                inhomoElement.addChildElement(proximal);

                            }

                            if (pg.getDistalPref().equals(DistalPref.MOST_DIST_AT_1))
                            {
                                SimpleXMLElement distal = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INHOMO_PARAM_DISTAL);
                                distal.addAttribute(MorphMLConstants.INHOMO_PARAM_DISTAL_NORM_END_ATTR, "1");
                                inhomoElement.addContent("\n                    "); // to make it more readable...
                                inhomoElement.addChildElement(distal);

                            }

                            inhomoElement.addContent("\n                "); // to make it more readable...
                            segGroupElement.addContent("\n\n                "); // to make it more readable...
                            segGroupElement.addChildElement(inhomoElement);
                            segGroupElement.addContent("\n                "); // to make it more readable...


                        }
                    }
                    
                    segGroupElement.addContent("\n            "); // to make it more readable...
                }
                segmentParentElement.addContent("\n        "); // to make it more readable...
            
            }

            if (useCableGroup)
            {
                
                cabSegGroupParentElement.addContent("\n            "); // to make it more readable...
                //TODO: Check there isn't a more efficient way to do this...
                Vector<String> groups = cell.getAllGroupNames();
                
                for(String group: groups)
                {
                    ArrayList<Section> secs = cell.getSectionsInGroup(group);
                    
                    SimpleXMLElement cableGroupElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLE_GROUP_ELEMENT);
                    cableGroupElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_GROUP_NAME, group));

                    cabSegGroupParentElement.addContent("\n                "); // to make it more readable...
                    cabSegGroupParentElement.addChildElement(cableGroupElement);
                    for(Section sec: secs)
                    {
                        SimpleXMLElement cableElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLE_ELEMENT);
                        
                        int sectionId = allSections.indexOf(sec);
                        cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_ID_ATTR, sectionId+""));
                        
                        cableGroupElement.addContent("\n                    "); // to make it more readable...
                        cableGroupElement.addChildElement(cableElement);
                
                    }
                    for(ParameterisedGroup pg: cell.getParameterisedGroups())
                    {
                        if (pg.getGroup().equals(group))
                        {
                            SimpleXMLElement inhomoElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INHOMO_PARAM);
                            inhomoElement.addAttribute(MorphMLConstants.INHOMO_PARAM_NAME_ATTR, pg.getName());
                            inhomoElement.addAttribute(MorphMLConstants.INHOMO_PARAM_VARIABLE_ATTR, "p");

                            SimpleXMLElement metric = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INHOMO_PARAM_METRIC);
                            metric.addContent(pg.getMetric().toString());
                                inhomoElement.addContent("\n                        "); // to make it more readable...
                            inhomoElement.addChildElement(metric);


                            if (pg.getProximalPref().equals(ProximalPref.MOST_PROX_AT_0))
                            {
                                SimpleXMLElement proximal = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INHOMO_PARAM_PROXIMAL);
                                proximal.addAttribute(MorphMLConstants.INHOMO_PARAM_PROXIMAL_TRANS_START_ATTR, "0");

                                inhomoElement.addContent("\n                        "); // to make it more readable...
                                inhomoElement.addChildElement(proximal);

                            }

                            if (pg.getDistalPref().equals(DistalPref.MOST_DIST_AT_1))
                            {
                                SimpleXMLElement distal = new SimpleXMLElement(mmlPrefix+MorphMLConstants.INHOMO_PARAM_DISTAL);
                                distal.addAttribute(MorphMLConstants.INHOMO_PARAM_DISTAL_NORM_END_ATTR, "1");
                                inhomoElement.addContent("\n                        "); // to make it more readable...
                                inhomoElement.addChildElement(distal);

                            }

                            inhomoElement.addContent("\n                    "); // to make it more readable...
                            cableGroupElement.addContent("\n                    "); // to make it more readable...
                            cableGroupElement.addChildElement(inhomoElement);
                            cableGroupElement.addContent("\n                    "); // to make it more readable...


                        }
                    }
                    
                    cableGroupElement.addContent("\n               "); // to make it more readable...
                    cabSegGroupParentElement.addContent("\n            "); // to make it more readable...
                    
                }
                
            }

            if (!level.equals(NeuroMLLevel.NEUROML_LEVEL_1) )
            {
                cellElement.addContent("\n\n        "); // to make it more readable...
                cellElement.addComment(new SimpleXMLComment("Adding the biophysical parameters"));
                cellElement.addContent("\n        "+extraInd); // to make it more readable...

                SimpleXMLElement biophysElement = null;
                SimpleXMLElement membPropsElement = null;
                SimpleXMLElement intraCellPropsElement = null;



                if (!nml2)
                {
                    biophysElement = new SimpleXMLElement(BiophysicsConstants.ROOT_ELEMENT);
                    membPropsElement = biophysElement;
                    intraCellPropsElement = biophysElement;

                }
                else
                {
                    biophysElement = new SimpleXMLElement(BiophysicsConstants.BIOPHYS_PROPS_ELEMENT_V2);

                    biophysElement.addAttribute(NeuroMLConstants.NEUROML_ID_V2, "biophys");

                    membPropsElement = new SimpleXMLElement(BiophysicsConstants.MEMB_PROPS_ELEMENT_V2);

                    biophysElement.addContent("\n\n            "); // to make it more readable...

                    biophysElement.addChildElement(membPropsElement);
                    intraCellPropsElement = new SimpleXMLElement(BiophysicsConstants.INTRACELL_PROPS_ELEMENT_V2);

                    biophysElement.addContent("\n\n            "); // to make it more readable...

                    biophysElement.addChildElement(intraCellPropsElement);
                }

                if (!nml2 && preferredExportUnits==UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS)
                {
                    biophysElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.UNITS_ATTR,
                                                                   BiophysicsConstants.UNITS_PHYSIOLOGICAL));
                }
                else if (!nml2 &&  preferredExportUnits==UnitConverter.GENESIS_SI_UNITS)
                {
                    biophysElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.UNITS_ATTR,
                                                                   BiophysicsConstants.UNITS_SI));
                }

                cellElement.addChildElement(biophysElement);

                String bioPrefix = BiophysicsConstants.PREFIX + ":";

                if (nml2)
                {
                    bioPrefix = "";
                }

                ArrayList<ChannelMechanism> allUniformChanMechs = cell.getAllUniformChanMechs(true, true);
                
                if (nml2) {
                    // Because the generated <channelDensity> elements are added at the start of membPropsElement
                    allUniformChanMechs = (ArrayList<ChannelMechanism>)GeneralUtils.reorderAlphabetically(allUniformChanMechs, false);
                }
                else {
                    allUniformChanMechs = (ArrayList<ChannelMechanism>)GeneralUtils.reorderAlphabetically(allUniformChanMechs, true);
                }

                Units condDensUnit = UnitConverter.conductanceDensityUnits[preferredExportUnits];
                Units permeabilityUnit = UnitConverter.permeabilityUnits[preferredExportUnits];
                
                HashMap<String, SimpleXMLElement> ionSpeciesV2 = new HashMap<String, SimpleXMLElement>();
                
                
                
                /// **********    Uniform channel densities   ********************

                LinkedHashMap<SimpleXMLElement, String> chanMechElements = new LinkedHashMap<SimpleXMLElement, String>();
                LinkedHashMap<SimpleXMLElement, String> chanMechNernstElements = new LinkedHashMap<SimpleXMLElement, String>();
                LinkedHashMap<SimpleXMLElement, String> chanMechGHKElements = new LinkedHashMap<SimpleXMLElement, String>();
                LinkedHashMap<SimpleXMLElement, String> chanMechNonUniElements = new LinkedHashMap<SimpleXMLElement, String>();
                LinkedHashMap<SimpleXMLElement, String> chanMechNonUniNernstElements = new LinkedHashMap<SimpleXMLElement, String>();
                
                for (ChannelMechanism chanMech : allUniformChanMechs) {
                    
                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(chanMech.getName());
                    float condDens = -1, permeability = -1;
                    MechParameter permPar = chanMech.getExtraParameter(BiophysicsConstants.PARAMETER_GHK_2);
                    if (null != permPar) {
                        permeability = (float) UnitConverter.getPermeability(permPar.getValue(), UnitConverter.NEUROCONSTRUCT_UNITS, preferredExportUnits);

                    } else {
                        condDens = (float) UnitConverter.getConductanceDensity(chanMech.getDensity(), UnitConverter.NEUROCONSTRUCT_UNITS, preferredExportUnits);
                        
                        if (nml2 && condDens<0) {
                            
                        GuiUtils.showWarningMessage(logger, "A negative conductance density ("+chanMech.getDensity()+") has been "
                                + "found for channel mechanism: "+chanMech.getName()+" in "+cell.getInstanceName()+".\n"
                                + "Such negative channel densities are normally used in neuroConstruct for specifying extra parameters \n"
                                + "(e.g. erev, shift) across multiple 'real' channel density specifications, which have different values \n"
                                + "for cond density on soma, axon, dends, etc. This convention is not supported in the export to NeuroML2. \n\n"
                                + "The solution is to remove the channel mechanism with negative value/extra params, and add the \n"
                                + "extra parameters to EACH real channel mechanism specification.", null);
                        }
                        
                    }
                    List<String> groups = cell.getGroupsWithChanMech(chanMech);
                    if (nml2) {
                        String comment;

                        for (String group : groups) {
                            //boolean addAtStartMembPropsElement = false;
                            //boolean addAtEndMembPropsElement = false;

                            SimpleXMLElement mechElement = null;
                            if (cm.isChannelMechanism()) {
                                //addAtStartMembPropsElement = true;

                                if (permeability >= 0.0) {
                                    mechElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.CHAN_DENSITY_GHK_ELEMENT_V2);
                                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PERMEABILITY_ATTR_V2,
                                            permeability + " " + permeabilityUnit.getNeuroML2Symbol()));
                                } else {
                                    if (condDens<0) {
                                        GuiUtils.showWarningMessage(logger, "The conductance density for channel mechanism: "+chanMech+"\n"
                                            + "is negative. This convention is usually used in neuroConstruct to specify that a mechanism should be placed on a \n"
                                            + "large group of sections in the cell, which all share the same extra parameters (e.g. voltage shift, erev), and that\n"
                                            + "sub groups of this will have different conductance densities. This convention is not currently supported in NML2 however.\n\n"
                                            + "It is better to explicitly state the conductance density & extra parameters for each sub group.", null);
                                    }
                                    mechElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.CHAN_DENSITY_ELEMENT_V2);
                                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.COND_DENS_ATTR_V2,
                                            condDens + " " + condDensUnit.getNeuroML2Symbol()));

                                }

                                mechElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2,
                                        chanMech.getName() + "_" + group));

                                mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.ION_CHAN_ATTR_V2,
                                        chanMech.getNML2Name()));
                            }
                            else if (cm.isIonConcMechanism()) 
                            {
                                mechElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.SPECIES_ELEMENT_V2);

                                intraCellPropsElement.addContent("\n\n                ");
                                intraCellPropsElement.addChildElement(mechElement);

                            }

                            String comm = handleNml2ChannelDensityAttrs(project, cell, chanMech, mechElement, group, null, bioPrefix, ionSpeciesV2);
                            boolean isNernst = comm!= null && comm.contains("Nernst");
                            boolean isGHK = comm!= null && comm.contains("GHK");
                            
                            comment  = comm;
                            if (!cm.isIonConcMechanism()) {
                                if (isNernst)
                                    chanMechNernstElements.put(mechElement, comment);
                                else if (isGHK)
                                    chanMechGHKElements.put(mechElement, comment);
                                else
                                    chanMechElements.put(mechElement, comment);
                            }
                            
                        }
                        
                    }
                    else   // nml1
                    {

                        SimpleXMLElement mechElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.MECHANISM_ELEMENT);
                        membPropsElement.addContent("\n\n                ");
                        membPropsElement.addChildElement(mechElement);

                        mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_NAME_ATTR,
                                chanMech.getName()));
                        
                        if (!cm.isIonConcMechanism())
                        {
                            mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_TYPE_ATTR,
                                    BiophysicsConstants.MECHANISM_TYPE_CHAN_MECH));
                        }
                        else
                        {
                            mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_TYPE_ATTR,
                                    BiophysicsConstants.MECHANISM_TYPE_ION_CONC));
                            
                        }
                        
                        
                        ArrayList<SimpleXMLElement> allParamGrps = new ArrayList<SimpleXMLElement>();

                        SimpleXMLElement gmaxParamElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                        gmaxParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR,
                                BiophysicsConstants.PARAMETER_GMAX));
                        
                        gmaxParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, condDens+ ""));

                        if (cm.isIonConcMechanism() && chanMech.getDensity() == 0)
                        {
                            
                            mechElement.addComment("Note: Calcium pools are not proper ion channels, thus this parameter does not represent a proper maximum conductance.\n"
                                    +"The scaling factor for converting current into change in ion concentration should be\n"
                                    + " determined from ChannelML file for the CaPool...");
                            
                        }
                        allParamGrps.add(gmaxParamElement);
                        
                        
                        
                        ArrayList<MechParameter> mps = chanMech.getExtraParameters();

                        for(MechParameter mp: mps)
                        {
                            SimpleXMLElement pe = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                            pe.addComment(new SimpleXMLComment("Note: Units of extra parameters are not known, except if it's e!!"));

                            pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR,
                                    mp.getName()));
                            
                            float val = mp.getValue();

                            if (mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT) ||
                                    mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT_2))
                            {
                                val = (float)UnitConverter.getVoltage(val,
                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                        preferredExportUnits);
                            }
                            
                            
                            pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                    val+""));
                            
                            allParamGrps.add(pe);

                        }
                        
                        
                        
                        if (cm instanceof ChannelMLCellMechanism)
                        {
                            ChannelMLCellMechanism cmlCm = (ChannelMLCellMechanism)cm;

                            if (cmlCm.isPassiveNonSpecificCond())
                            {
                                if (!usePreV1_7_1Format)
                                {
                                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_PASSIVE_COND_ATTR,"true"));
                                }
                                else
                                {
                                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_PASSIVE_COND_ATTR_pre_v1_7_1,"true"));
                                }

                                SimpleXMLElement revPotParamElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                                revPotParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR,
                                        BiophysicsConstants.PARAMETER_REV_POT));
                                
                                String xpath = ChannelMLConstants.getPreV1_7_3IonsXPath() +"/@"+ ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR;
                                String val = cmlCm.getXMLDoc().getValueByXPath(xpath);

                                logger.logComment("Trying to get: "+ xpath+": "+ val);

                                logger.logComment("Trying to get: "+ cmlCm.getXMLDoc().getXPathLocations(true));

                                if (val==null || val.trim().length()==0)  // post v1.7.3 format
                                {
                                    xpath = ChannelMLConstants.getCurrVoltRelXPath() +"/@"+ ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR;
                                    val = cmlCm.getXMLDoc().getValueByXPath(xpath);
                                }

                                float revPot = Float.parseFloat(val);
                                
                                String unitsUsedString = cmlCm.getUnitsUsedInFile();
                                int unitsUsedInt = UnitConverter.getUnitSystemIndex(unitsUsedString);

                                for(MechParameter mp:chanMech.getExtraParameters())
                                {
                                    if (mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT) ||
                                            mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT_2))
                                    {
                                        revPot = mp.getValue();
                                        unitsUsedInt = UnitConverter.NEUROCONSTRUCT_UNITS;
                                        logger.logComment("The reversal potential has been set as one of the extra params: "+revPot);
                                               
                                        // remove from param list, as units dealt with here
                                        SimpleXMLElement toRemove = null;
                                        for(SimpleXMLElement paramElement: allParamGrps)
                                        {
                                            if (paramElement.getAttributeValue(BiophysicsConstants.PARAMETER_NAME_ATTR).equals(BiophysicsConstants.PARAMETER_REV_POT))
                                            {
                                                toRemove = paramElement;
                                            }
                                        }
                                        if (toRemove!=null) allParamGrps.remove(toRemove);
                                    }
                                }

                                boolean revPotSetElsewhere = false;

                                Iterator<ChannelMechanism> chanMechs = cell.getChanMechsVsGroups().keySet().iterator();
                                while(chanMechs.hasNext())
                                {
                                    ChannelMechanism other = chanMechs.next();
                                    if (!other.equals(chanMech) && other.getName().equals(chanMech.getName()) && other.getDensity()<0)
                                    {

                                        Vector<String> otherGroups = cell.getGroupsWithChanMech(other);
                                        // todo: make this more generic for any groups!
                                        if (otherGroups.contains(Section.ALL))
                                        {
                                            revPotSetElsewhere = true;
                                        }
                                    }
                                }
                                
                                
                                revPotParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                        (float) UnitConverter.getVoltage(revPot,
                                                unitsUsedInt,
                                                preferredExportUnits) + ""));
                                
                                if (!revPotSetElsewhere)
                                {
                                    mechElement.addContent("\n                    ");
                                    mechElement.addChildElement(revPotParamElement);
                                }

                                Vector<String> gps = cell.getGroupsWithChanMech(chanMech);

                                for (String gp : gps) {
                                    
                                    SimpleXMLElement groupElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.GROUP_ELEMENT);
                                    revPotParamElement.addContent("\n                        ");
                                    revPotParamElement.addChildElement(groupElement);
                                    groupElement.addContent(gp);
                                    
                                }
                                revPotParamElement.addContent("\n                    ");
                            }
                        }
                        
                        
                        
                        for(SimpleXMLElement paramElement: allParamGrps)
                        {
                            mechElement.addContent("\n                    ");
                            mechElement.addChildElement(paramElement);

                            for (String group : groups) {
                                SimpleXMLElement groupElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.GROUP_ELEMENT);
                                paramElement.addContent("\n                        ");
                                paramElement.addChildElement(groupElement);
                                groupElement.addContent(group);
                            }
                            paramElement.addContent("\n                    ");
                        }

                        mechElement.addContent("\n                ");
                    }
                }
                
                /// ********** Non uniform channel densities   ********************
                
                
                Hashtable<VariableMechanism, ParameterisedGroup> allVarChanMechs = cell.getVarMechsVsParaGroups();

                Enumeration<VariableMechanism> varMechEnum = allVarChanMechs.keys();
                ArrayList<VariableMechanism> varMechs = Collections.list(varMechEnum);
                if (nml2) {
                    // Because the generated <channelDensity> elements are added at the start of membPropsElement
                    varMechs = (ArrayList<VariableMechanism>)GeneralUtils.reorderAlphabetically(varMechs, false);
                }
                
                for (VariableMechanism vm: varMechs)
                {
                    ParameterisedGroup pg = allVarChanMechs.get(vm);
                    //membPropsElement.addContent("\n\n            ");

                    if(nml2)
                    {
                        
                        SimpleXMLElement mechElement = new SimpleXMLElement(BiophysicsConstants.CHAN_DENSITY_NON_UNIFORM_ELEMENT_V2);

                        mechElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, vm.getName() + "_" + pg.getGroup()));

                        mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.ION_CHAN_ATTR_V2, vm.getName()));

                        CellMechanism cm = project.cellMechanismInfo.getCellMechanism(vm.getName());

                        SimpleXMLElement pe = new SimpleXMLElement(bioPrefix + BiophysicsConstants.VAR_PARAMETER_ELEMENT_V2);

                        String paramName = vm.getParam().getName();
                        if (paramName.equals(BiophysicsConstants.PARAMETER_GMAX)) {
                            paramName = BiophysicsConstants.COND_DENS_ATTR_V2;
                        }
                        pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR_V2, paramName));

                        pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.SEG_GROUP_ATTR_V2, pg.getGroup()));

                        SimpleXMLElement iv = new SimpleXMLElement(bioPrefix + BiophysicsConstants.INHOMOGENEOUS_VALUE_V2);
                        pe.addContent("\n                        ");
                        pe.addChildElement(iv);
                        iv.addAttribute(BiophysicsConstants.INHOMOGENEOUS_PARAM_NAME_V2, pg.getName());
                        String convFactor = "";
                        
                        int expressionExportUnits = UnitConverter.GENESIS_SI_UNITS;

                        if (vm.getParam().getName().equals(BiophysicsConstants.PARAMETER_GMAX))
                        {
                            convFactor = UnitConverter.getConductanceDensity(1, UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    expressionExportUnits) +" * ";
                        }
                        if (vm.getParam().getName().equals(BiophysicsConstants.PARAMETER_REV_POT)||
                                vm.getParam().getName().equals(BiophysicsConstants.PARAMETER_REV_POT_2))
                        {
                            convFactor = UnitConverter.getVoltage(1, UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    expressionExportUnits) +" * ";
                        }

                        iv.addAttribute(BiophysicsConstants.INHOMOGENEOUS_PARAM_VALUE, convFactor+vm.getParam().getExpression().toString());
                        
                        if (convFactor.length()>0)
                        {
                            //pe.addContent("\n                "); // to make it more readable...
                            pe.addComment("Note: conversion factor ("+convFactor+") included to convert to units: "+UnitConverter.getUnitSystemDescription(expressionExportUnits));
                        }

                        pe.addContent("\n                    "); // to make it more readable...
                        
                        mechElement.addContent("\n                    ");
                        mechElement.addChildElement(pe);
                        mechElement.addContent("\n                ");
                        
                        String comm = handleNml2ChannelDensityAttrs(project, cell, vm, mechElement, null, pg.getGroup(), bioPrefix, ionSpeciesV2);
                        
                        comm = (comm!=null ? comm+"\n    " : "") +vm.toString() + " on "+pg;
                        
                        boolean isNernst = comm!= null && comm.contains("Nernst");
                        boolean isGHK = comm!= null && comm.contains("GHK");

                        String comment  = comm;
                        if (isNernst)
                            chanMechNonUniNernstElements.put(mechElement, comment);
                        else if (isGHK)
                            throw new NeuroMLException("Non uniform GHK channel densities not yet supported...");
                        else
                            chanMechNonUniElements.put(mechElement, comment);

                        
                    }
                    else // nml1
                    {
                        SimpleXMLElement mechElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.MECHANISM_ELEMENT);
                        membPropsElement.addContent("\n\n                ");
                        membPropsElement.addChildElement(mechElement);

                        mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_NAME_ATTR,
                                                                        vm.getName()));

                        CellMechanism cm = project.cellMechanismInfo.getCellMechanism(vm.getName());

                        if (!cm.isIonConcMechanism())
                        {
                            mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_TYPE_ATTR,
                                                                        BiophysicsConstants.MECHANISM_TYPE_CHAN_MECH));
                        }
                        else
                        {

                        }
                        
                        SimpleXMLElement pe = new SimpleXMLElement(bioPrefix + BiophysicsConstants.VAR_PARAMETER_ELEMENT);
                        mechElement.addContent("\n                    ");
                        mechElement.addChildElement(pe);

                        pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR,
                                                                        vm.getParam().getName()));

                        SimpleXMLElement group = new SimpleXMLElement(bioPrefix + BiophysicsConstants.GROUP_ELEMENT);
                        pe.addContent("\n                        ");
                        pe.addChildElement(group);
                        group.addContent(pg.getGroup());

                        SimpleXMLElement iv = new SimpleXMLElement(bioPrefix + BiophysicsConstants.INHOMOGENEOUS_VALUE);
                        pe.addContent("\n                        ");
                        pe.addChildElement(iv);
                        iv.addAttribute(BiophysicsConstants.INHOMOGENEOUS_PARAM_NAME, pg.getName());
                        String convFactor = "";

                        if (vm.getParam().getName().equals(BiophysicsConstants.PARAMETER_GMAX))
                        {
                            convFactor = UnitConverter.getConductanceDensity(1, UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    preferredExportUnits) +" * ";
                        }
                        if (vm.getParam().getName().equals(BiophysicsConstants.PARAMETER_REV_POT)||
                                vm.getParam().getName().equals(BiophysicsConstants.PARAMETER_REV_POT_2))
                        {
                            convFactor = UnitConverter.getVoltage(1, UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    preferredExportUnits) +" * ";
                        }

                        iv.addAttribute(BiophysicsConstants.INHOMOGENEOUS_PARAM_VALUE, convFactor+vm.getParam().getExpression().toString());
                        if (convFactor.length()>0)
                        {
                            //pe.addContent("\n                "); // to make it more readable...
                            pe.addComment("Note: conversion factor ("+convFactor+") included to convert to units: "+UnitConverter.getUnitSystemDescription(preferredExportUnits));
                        }

                        pe.addContent("\n                    "); // to make it more readable...

                        mechElement.addContent("\n                ");
                    }

                }
                
                ArrayList<LinkedHashMap<SimpleXMLElement, String>> chanMechMaps = new ArrayList<LinkedHashMap<SimpleXMLElement, String>>();
                chanMechMaps.add(chanMechElements);
                chanMechMaps.add(chanMechNernstElements);
                chanMechMaps.add(chanMechGHKElements);
                chanMechMaps.add(chanMechNonUniElements);
                chanMechMaps.add(chanMechNonUniNernstElements);
                
                membPropsElement.addContent("\n                ");
                for(LinkedHashMap<SimpleXMLElement, String> chanMechMap: chanMechMaps) {
                
                    Set<SimpleXMLElement> s = chanMechMap.keySet();
                    ArrayList<SimpleXMLElement> al = (ArrayList<SimpleXMLElement>)GeneralUtils.reorderAlphabetically(new ArrayList(s), false);
                    for (SimpleXMLElement sxe : al) {
                        String comment = chanMechMap.get(sxe);

                        if (comment != null) {
                            membPropsElement.addContent("\n\n            ");
                            membPropsElement.addComment(comment);
                            //membPropsElement.addContent("\n                ");
                        }
                        membPropsElement.addContent("\n                ");
                        membPropsElement.addChildElement(sxe);
                        membPropsElement.addContent("\n                ");
                    }
                }

                

                if (nml2)
                {
                    double spikeThresh = -0;
                    Double uniqueThresh = Double.NaN;
                    ArrayList<String> cellGroupsWithCell = new ArrayList<String>();
                    for (String cellGroup: project.generatedCellPositions.getNonEmptyCellGroups())
                    {
                        if(project.cellGroupsInfo.getCellType(cellGroup).equals(cell.getInstanceName())) 
                        {
                            cellGroupsWithCell.add(cellGroup);
                        }
                    }
                    logger.logComment("cellGroupsWithCell: "+cell+" " + cellGroupsWithCell);
                    for (String netConn: project.generatedNetworkConnections.getNamesNonEmptyNetConns()) 
                    {
                        Vector<SynapticProperties> syns = new Vector<SynapticProperties>();
                        if (project.morphNetworkConnectionsInfo.isValidSimpleNetConn(netConn) &&
                            cellGroupsWithCell.contains(project.morphNetworkConnectionsInfo.getSourceCellGroup(netConn))) {
                            syns.addAll(project.morphNetworkConnectionsInfo.getSynapseList(netConn));
                        }
                        if (project.volBasedConnsInfo.isValidVolBasedConn(netConn) &&
                            cellGroupsWithCell.contains(project.volBasedConnsInfo.getSourceCellGroup(netConn))) {
                            syns.addAll(project.volBasedConnsInfo.getSynapseList(netConn));
                        }
                        logger.logComment("syns: "+syns);
                        for (SynapticProperties sp: syns) {
                            spikeThresh = sp.getThreshold();
                            if (uniqueThresh.isNaN())
                                uniqueThresh = spikeThresh;
                            else 
                            {
                                if (spikeThresh!=uniqueThresh) {
                                    throw new NeuroMLException("Error in export to NeuroML2. There are multiple different values for the spiking threshold\n"
                                        + "in the various network connections using cell "+cell.getInstanceName()+", e.g. "+spikeThresh+" for "+netConn+
                                        ", "+uniqueThresh+" elsewhere...\n\nNeuroML2 currently specifies the spike threshold as a property of the cell so this needs\n"
                                        + "to be the same for all network connections.");
                                    
                                }
                            }
                        }
                    }
                    SimpleXMLElement el = new SimpleXMLElement(BiophysicsConstants.SPIKE_THRESHOLD_v2);
                    
                    el.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, spikeThresh + " "+voltUnit.getNeuroML2Symbol()));
                    membPropsElement.addContent("\n\n                ");
                    membPropsElement.addChildElement(el);
                }
                
                

                //***********    Specific Capacitance   ************

                SimpleXMLElement specCapElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.SPECIFIC_CAP_ELEMENT);
                
                if (usePreV1_7_1Format) 
                    specCapElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.SPECIFIC_CAP_ELEMENT_pre_v1_7_1);
              


                if (!nml2)
                {
                    membPropsElement.addContent("\n\n                ");
                    membPropsElement.addChildElement(specCapElement);
                }

                ArrayList<Float> specCaps = cell.getDefinedSpecCaps();
                
                specCaps = (ArrayList<Float>)GeneralUtils.reorderAlphabetically(specCaps, true);

                for (Float specCap : specCaps)
                {
                    SimpleXMLElement paramElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                    float specCapConv = (float)UnitConverter.getSpecificCapacitance(specCap,
                        UnitConverter.NEUROCONSTRUCT_UNITS,
                        preferredExportUnits);

                    Units scUnit = UnitConverter.specificCapacitanceUnits[preferredExportUnits];

                    paramElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, specCapConv+ ""));

                    specCapElement.addContent("\n                    ");
                    specCapElement.addChildElement(paramElement);
                    specCapElement.addContent("\n                ");

                    Vector<String> groups = cell.getGroupsWithSpecCap(specCap);
                    groups = (Vector<String>)GeneralUtils.reorderAlphabetically(groups, true);

                    for (String group : groups)
                    {
                        SimpleXMLElement groupElement2 = new SimpleXMLElement(bioPrefix + BiophysicsConstants.GROUP_ELEMENT);
                        paramElement.addContent("\n                        ");
                        paramElement.addChildElement(groupElement2);
                        paramElement.addContent("\n                    ");
                        groupElement2.addContent(group);

                        if (nml2)
                        {
                            SimpleXMLElement el = new SimpleXMLElement(BiophysicsConstants.SPECIFIC_CAP_ELEMENT_v2);
                            
                            if (!group.equals(Section.ALL))
                                el.addAttribute(BiophysicsConstants.SEG_GROUP_ATTR_V2, group);

                            el.addAttribute(BiophysicsConstants.VALUE_ATTR_V2, specCapConv+" "+scUnit.getNeuroML2Symbol());

                            membPropsElement.addContent("\n\n                ");
                            membPropsElement.addChildElement(el);

                        }

                    }

                }
                
                
                //***********    Axial resistance   ************

                SimpleXMLElement specAxResElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT);
                
                if (usePreV1_7_1Format)
                    specAxResElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1);

                if (!nml2)
                {
                    intraCellPropsElement.addContent("\n\n                ");
                    intraCellPropsElement.addChildElement(specAxResElement);
                }

                ArrayList<Float> specAxReses = cell.getDefinedSpecAxResistances();
                logger.logComment("specAxReses: " + specAxReses);
                specAxReses = (ArrayList<Float>)GeneralUtils.reorderAlphabetically(specAxReses, true);

                //System.out.println("-----   Adding: Axial resistance..."+cell.getInstanceName());

                for (Float specAxRes : specAxReses)
                {
                    SimpleXMLElement paramElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                    float specAxResConv = (float)UnitConverter.getSpecificAxialResistance(specAxRes,
                        UnitConverter.NEUROCONSTRUCT_UNITS,
                        preferredExportUnits);

                    Units saxUnit = UnitConverter.specificAxialResistanceUnits[preferredExportUnits];

                    paramElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, specAxResConv + ""));

                    specAxResElement.addContent("\n                    ");
                    specAxResElement.addChildElement(paramElement);
                    specAxResElement.addContent("\n                ");

                    Vector<String> groups = cell.getGroupsWithSpecAxRes(specAxRes);
                    List<String> lgroups = GeneralUtils.reorderAlphabetically(groups, false);

                    for (String group : lgroups)
                    {
                        //System.out.println("           Adding: "+group);
                        SimpleXMLElement groupElement3 = new SimpleXMLElement(bioPrefix + BiophysicsConstants.GROUP_ELEMENT);
                        paramElement.addContent("\n                        ");
                        paramElement.addChildElement(groupElement3);
                        paramElement.addContent("\n                    ");
                        groupElement3.addContent(group);

                        if (nml2)
                        {
                            SimpleXMLElement el = new SimpleXMLElement(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT_V2);

                            if (!group.equals(Section.ALL))
                                el.addAttribute(BiophysicsConstants.SEG_GROUP_ATTR_V2, group);

                            el.addAttribute(BiophysicsConstants.VALUE_ATTR_V2, specAxResConv+" "+saxUnit.getNeuroML2Symbol());

                            intraCellPropsElement.addContent("\n\n                ");
                            intraCellPropsElement.addChildElement(el);

                        }
                    }
                }

                
                //***********    Initial potential   ************


                SimpleXMLElement initPotElement = new SimpleXMLElement(bioPrefix+BiophysicsConstants.INITIAL_POT_ELEMENT);
                
                if (usePreV1_7_1Format)
                    initPotElement = new SimpleXMLElement(bioPrefix+BiophysicsConstants.INITIAL_POT_ELEMENT_pre_v1_7_1);
                else if (nml2)
                    initPotElement = new SimpleXMLElement(bioPrefix+BiophysicsConstants.INITIAL_POT_ELEMENT_V2);



                SimpleXMLElement paramElement = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                float volts = (float) UnitConverter.getVoltage(cell.getInitialPotential().
                    getNominalNumber(),
                    UnitConverter.NEUROCONSTRUCT_UNITS,
                    preferredExportUnits);

                if (!nml2)
                {
                    paramElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, volts + ""));

                    membPropsElement.addContent("\n\n                ");
                    membPropsElement.addChildElement(initPotElement);

                    initPotElement.addContent("\n                    ");
                    initPotElement.addChildElement(paramElement);
                    initPotElement.addContent("\n                ");
                }
                else
                {
                    membPropsElement.addContent("\n\n                ");
                    membPropsElement.addChildElement(initPotElement);
                    initPotElement.addAttribute(BiophysicsConstants.VALUE_ATTR_V2, volts+" "+voltUnit.getNeuroML2Symbol());
                    membPropsElement.addContent("\n\n            ");
                }


               SimpleXMLElement groupElement1 = new SimpleXMLElement(bioPrefix+BiophysicsConstants.GROUP_ELEMENT);
               paramElement.addContent("\n                        ");
               paramElement.addChildElement(groupElement1);
               paramElement.addContent("\n                    ");
               groupElement1.addContent("all");
               
               
               
                //***********    Ion Properties   ************

               Enumeration<IonProperties> e = cell.getIonPropertiesVsGroups().keys();
               ArrayList<IonProperties> ea = GeneralUtils.getOrderedList(e, true);
               
               //System.out.println(cell.getInstanceName()+": cell.getIonPropertiesVsGroups(): "+cell.getIonPropertiesVsGroups());
               for (IonProperties ip: ea)
               {
                   //System.out.println("  Adding " +ip);

                    Vector<String> groups = cell.getIonPropertiesVsGroups().get(ip);
                    //System.out.println("  grps "+groups);

                    for(String grp: groups)
                    {

                        SimpleXMLElement ionPropEl = new SimpleXMLElement(bioPrefix+BiophysicsConstants.ION_PROPS_ELEMENT);
                        ionPropEl.addAttribute(BiophysicsConstants.ION_PROPS_NAME_ATTR, ip.getName());

                        if (!nml2)
                        {
                            intraCellPropsElement.addContent("\n\n                ");
                            intraCellPropsElement.addChildElement(ionPropEl);
                            intraCellPropsElement.addContent("\n            ");
                        }
                        else
                        {
                            //ionPropEl = ionSpeciesV2.get(ip.getName());
                        }
                    
                        SimpleXMLElement grpEl = new SimpleXMLElement(bioPrefix+BiophysicsConstants.GROUP_ELEMENT);
                        grpEl.addContent(grp);

                        if (ip.revPotSetByConcs())
                        {
                            SimpleXMLElement paramElExt = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);
                            
                            float extConc = (float)UnitConverter.getConcentration(ip.getExternalConcentration(),
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    preferredExportUnits);

                            paramElExt.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR, BiophysicsConstants.PARAMETER_CONC_EXT));
                            paramElExt.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, extConc+""));


                            SimpleXMLElement paramElInt = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);
                            
                            float intConc = (float)UnitConverter.getConcentration(ip.getInternalConcentration(),
                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                    preferredExportUnits);

                            paramElInt.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR, BiophysicsConstants.PARAMETER_CONC_INT));
                            paramElInt.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR, intConc+""));

                            if (!nml2)
                            {
                                paramElExt.addContent("\n                        ");
                                paramElExt.addChildElement(grpEl);
                                paramElExt.addContent("\n                    ");

                                ionPropEl.addContent("\n                    ");
                                ionPropEl.addChildElement(paramElExt);
                                ionPropEl.addContent("\n                ");
                                paramElInt.addContent("\n                        ");
                                paramElInt.addChildElement(grpEl);
                                paramElInt.addContent("\n                    ");

                                ionPropEl.addContent("\n                    ");
                                ionPropEl.addChildElement(paramElInt);
                                ionPropEl.addContent("\n                ");
                            }
                            else
                            {
                                
                                ionPropEl = ionSpeciesV2.get(ip.getName()+"_"+grp);

                                if (ionPropEl!=null)
                                {

                                    ionPropEl.addAttribute(ChannelMLConstants.ION_CONC_INT_ATTR_V2, intConc+" "+concUnits.getNeuroML2Symbol());
                                    ionPropEl.addAttribute(ChannelMLConstants.ION_CONC_EXT_ATTR_V2, extConc+" "+concUnits.getNeuroML2Symbol());
                                    
                                }
                            }
                        }
                        else
                        {
                            if (!nml2)
                            {
                                SimpleXMLElement paramElInt = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);

                                paramElInt.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR, BiophysicsConstants.PARAMETER_REV_POT));
                                paramElInt.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,(float)UnitConverter.getVoltage(ip.getReversalPotential(),
                                                        UnitConverter.NEUROCONSTRUCT_UNITS,
                                                        preferredExportUnits) +""));

                                paramElInt.addContent("\n                        ");
                                paramElInt.addChildElement(grpEl);
                                paramElInt.addContent("\n                    ");

                                try
                                {
                                    ionPropEl.addContent("\n                    ");
                                    ionPropEl.addChildElement(paramElInt);
                                    ionPropEl.addContent("\n                ");
                                }
                                catch(Exception ex)
                                {
                                    GuiUtils.showErrorMessage(logger, "Problem adding ion info: "+ip+", grp: "+grp, ex, null);
                                }
                            }
                            
                        }
                        
                    }

               }


               if (nml2) intraCellPropsElement.addContent("\n\n            ");
               if (nml2) biophysElement.addContent("\n\n        ");



               if (!nml2 && level.equals(NeuroMLLevel.NEUROML_LEVEL_3))
               {

                   String netPrefix = NetworkMLConstants.PREFIX + ":";

                    if (nml2)
                    {
                        netPrefix = "";
                    }

                   ArrayList<String> allSyns = cell.getAllAllowedSynapseTypes(true);
                   
                   
                   
                   SimpleXMLElement connectEl = new SimpleXMLElement(NetworkMLConstants.CONNECTIVITY_ELEMENT);

                   for (String synType: allSyns)
                   {
                       Vector<String> groups = cell.getGroupsWithSynapse(synType);

                       if (usePreV1_7_1Format)
                       {
                           SimpleXMLElement potSynLocEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.POT_SYN_LOC_ELEMENT_preV1_7_1);
                           SimpleXMLElement synTypeEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.SYN_TYPE_ELEMENT);
                           synTypeEl.addContent(synType);
                           potSynLocEl.addChildElement(synTypeEl);

                           if (groups.size()==1)
                           {
                               if (groups.firstElement().equals(Section.AXONAL_GROUP))
                               {
                                   SimpleXMLElement dirEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.SYN_DIR_ELEMENT);
                                   dirEl.addContent(NetworkMLConstants.SYN_DIR_PRE);
                                   potSynLocEl.addChildElement(dirEl);
                               }
                               else if (groups.firstElement().equals(Section.DENDRITIC_GROUP))
                               {
                                   SimpleXMLElement dirEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.SYN_DIR_ELEMENT);
                                   dirEl.addContent(NetworkMLConstants.SYN_DIR_POST);
                                   potSynLocEl.addChildElement(dirEl);
                               }
                               else if (groups.firstElement().equals(Section.SOMA_GROUP))
                               {
                                   SimpleXMLElement dirEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.SYN_DIR_ELEMENT);
                                   dirEl.addContent(NetworkMLConstants.SYN_DIR_PRE_ANDOR_POST);
                                   potSynLocEl.addChildElement(dirEl);
                               }
                           }
                           for (String group: groups)
                           {
                               SimpleXMLElement grpEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.GROUP_ELEMENT);
                               grpEl.addContent(group);
                               potSynLocEl.addChildElement(grpEl);
                           }
                           biophysElement.addChildElement(potSynLocEl);
                       }
                       else
                       {
                           
                           SimpleXMLElement potSynLocEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.POT_SYN_LOC_ELEMENT);

                           connectEl.addContent("\n                ");
                           connectEl.addChildElement(potSynLocEl);
                           connectEl.addContent("\n            ");
                           
                           SimpleXMLAttribute synTypeAttr = new SimpleXMLAttribute(NetworkMLConstants.SYN_TYPE_ATTR, synType);
                           
                           potSynLocEl.addAttribute(synTypeAttr);

                           if (groups.size()==1)
                           {
                               if (groups.firstElement().equals(Section.AXONAL_GROUP))
                               {
                                   SimpleXMLAttribute dirAttr = new SimpleXMLAttribute(NetworkMLConstants.SYN_DIR_ELEMENT,NetworkMLConstants.SYN_DIR_PRE);
                                   potSynLocEl.addAttribute(dirAttr);
                               }
                               else if (groups.firstElement().equals(Section.DENDRITIC_GROUP))
                               {
                                   SimpleXMLAttribute dirAttr = new SimpleXMLAttribute(NetworkMLConstants.SYN_DIR_ELEMENT, NetworkMLConstants.SYN_DIR_POST);
                                   potSynLocEl.addAttribute(dirAttr);
                               }
                               else if (groups.firstElement().equals(Section.SOMA_GROUP))
                               {
                                   SimpleXMLAttribute dirAttr = new SimpleXMLAttribute(NetworkMLConstants.SYN_DIR_ELEMENT,NetworkMLConstants.SYN_DIR_PRE_ANDOR_POST);
                                   potSynLocEl.addAttribute(dirAttr);
                               }
                           }
                           for (String group: groups)
                           {
                               SimpleXMLElement grpEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.GROUP_ELEMENT);
                               grpEl.addContent(group);
                               potSynLocEl.addContent("\n                    ");
                               potSynLocEl.addChildElement(grpEl);
                               potSynLocEl.addContent("\n                ");
                           }
                       }

                   }
                   
                   if (!usePreV1_7_1Format)
                   {
                       cellElement.addContent("\n\n            ");
                       cellElement.addChildElement(connectEl);
                       cellElement.addContent("\n\n        ");
                   }

                   Hashtable<ApPropSpeed, Vector<String>> appTable = cell.getApPropSpeedsVsGroups();

                   if (appTable.size()>0)
                   {
                       String warn = "\nNote that the following Ap Prop Speeds are specified for the cell, but this is not yet supported by NeuroML!";
                       Enumeration<ApPropSpeed> apps =  appTable.keys();

                       while (apps.hasMoreElements())
                       {
                           ApPropSpeed a = apps.nextElement();
                           warn = warn +"\n  - "+a.toString() +" is present on: "+ appTable.get(a);
                       }
                       warn = warn +"\n";
                       SimpleXMLComment comm = new SimpleXMLComment(warn);
                       biophysElement.addComment(comm);
                   }

                   Vector<AxonalConnRegion> axC = cell.getAxonalArbours();

                   if (axC.size()>0)
                   {
                       String warn = "\nNote that the following Volume based connection arbours are specified for the cell, but this is not yet supported by NeuroML!";

                       for (AxonalConnRegion axC1 : axC) {
                           warn = warn +"\n  - " + axC1;
                       }
                       warn = warn +"\n";
                       SimpleXMLComment comm = new SimpleXMLComment(warn);
                       biophysElement.addComment(comm);

                   }

               }

               if (nml2) cellElement.addContent("\n\n    ");
            }
            
            return cellElement;
    }
    
    private static String handleNml2ChannelDensityAttrs(Project project,
                                                 Cell cell,
                                                 IMechanism chanMech, 
                                                 SimpleXMLElement mechElement, 
                                                 String group, 
                                                 String nonUniformgroup, 
                                                 String bioPrefix, 
                                                 HashMap<String, SimpleXMLElement> ionSpeciesV2) throws XMLMechanismException {
        
        //System.out.println("Handling "+chanMech+", group "+group+" (nonuniform group "+nonUniformgroup+") for "+cell.getInstanceName());

        CellMechanism cm = project.cellMechanismInfo.getCellMechanism(chanMech.getName());
        
        if (group!=null && !group.equals(Section.ALL)) {
            mechElement.addAttribute(BiophysicsConstants.SEG_GROUP_ATTR_V2, group);
        }

        String comment = null;

        boolean revPotSetInMP = false;
        
        
        for (MechParameter mp : chanMech.getExtraParameters()) {
            
            boolean addExtraParams = mp.isReversalPotential();

            //SimpleXMLElement pe = new SimpleXMLElement(bioPrefix + BiophysicsConstants.PARAMETER_ELEMENT);
            boolean eOrErev = mp.isReversalPotential();
            
            boolean ghk = mp.getName().equals(BiophysicsConstants.PARAMETER_GHK_2);
            try {
                if (!eOrErev && !ghk) {
                    if (addExtraParams) {
                        mechElement.addContent("\n");
                        mechElement.addComment(new SimpleXMLComment("Note: Units of extra parameters are not known, except if it's e or erev..."));
                    }
                }
            } catch (Exception e) {
                GuiUtils.showErrorMessage(logger, "Problem when annotating chan mech: " + chanMech + ", mechElement: " + mechElement, e, null);
            }

            float val = mp.getValue();
            String unitSuffix = "";

            String attrName = mp.getName();

            if (eOrErev) {
                revPotSetInMP = true;
                val = (float) UnitConverter.getVoltage(val, UnitConverter.NEUROCONSTRUCT_UNITS,
                        preferredExportUnits);

                if (preferredExportUnits == UnitConverter.GENESIS_SI_UNITS) {
                    unitSuffix = " V";
                }
                if (preferredExportUnits == UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) {
                    unitSuffix = " mV";
                }

                attrName = BiophysicsConstants.PARAMETER_REV_POT_2;
            }

            if (!ghk) {
                if (addExtraParams) {
                    mechElement.addAttribute(attrName, val + unitSuffix);
                }
            }

        }
        
        String thisChannelsIon = null;

        if (cm instanceof XMLCellMechanism) {
            XMLCellMechanism cmlCm = (XMLCellMechanism) cm;

            String units = cmlCm.getXMLDoc().getValueByXPath(ChannelMLConstants.getUnitsXPath());

            if (cmlCm.isChannelMechanism()) {
                if (!cmlCm.isNeuroML2()) {
                    String ionXpath = ChannelMLConstants.getCurrVoltRelXPath() + "/@" + ChannelMLConstants.OHMIC_ION_ATTR;
                    logger.logComment("Trying to get now: " + ionXpath);
                    thisChannelsIon = cmlCm.getXMLDoc().getValueByXPath(ionXpath);

                    if (thisChannelsIon != null) {
                        mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.ION_ATTR_V2, thisChannelsIon));
                    } else {
                        thisChannelsIon = "non_specific";
                    }

                    String xpath = ChannelMLConstants.getCurrVoltRelXPath() + "/@" + ChannelMLConstants.FIXED_ION_REV_POT_ATTR;
                    String val = cmlCm.getXMLDoc().getValueByXPath(xpath);

                    if (thisChannelsIon.equals("ca") && (val == null || val.equals("no"))) {
                        if (null != chanMech.getExtraParameter(BiophysicsConstants.PARAMETER_GHK_2)) {
                            comment = "Current for ion " + thisChannelsIon + " in " + cmlCm.getInstanceName()
                                    + " will be calculated using the GHK flux equation";

                            mechElement.setName(bioPrefix + BiophysicsConstants.CHAN_DENSITY_GHK_ELEMENT_V2);
                        } else {
                            comment = "Reversal potential for " + thisChannelsIon + " in " + cmlCm.getInstanceName()
                                    + " will be calculated by Nernst equation from internal & external calcium";
                            
                            if (group!=null) {
                                mechElement.setName(bioPrefix + BiophysicsConstants.CHAN_DENSITY_NERNST_ELEMENT_V2);
                            } else if (nonUniformgroup!=null) {
                                mechElement.setName(bioPrefix + BiophysicsConstants.CHAN_DENSITY_NON_UNIFORM_NERNST_ELEMENT_V2);
                            }
                        }

                    } else {
                        if (!revPotSetInMP) {
                            xpath = ChannelMLConstants.getPreV1_7_3IonsXPath() + "/@" + ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR;
                            val = cmlCm.getXMLDoc().getValueByXPath(xpath);

                            logger.logComment("Trying to get: " + xpath + " in " + cmlCm.getInstanceName() + ": " + val);

                            logger.logComment("Trying to get: " + cmlCm.getXMLDoc().getXPathLocations(true));

                            if (val == null || val.trim().length() == 0) // post v1.7.3 format
                            {
                                xpath = ChannelMLConstants.getCurrVoltRelXPath() + "/@" + ChannelMLConstants.ION_REVERSAL_POTENTIAL_ATTR;
                                logger.logComment("Trying to get now: " + xpath);
                                val = cmlCm.getXMLDoc().getValueByXPath(xpath);
                            }

                            float revPot = Float.parseFloat(val);

                            logger.logComment("Tried to get: " + xpath + " in " + cmlCm.getXMLFile(project) + ", found: " + revPot);

                            float revPotConv = (float) UnitConverter.getVoltage(revPot,
                                    UnitConverter.getUnitSystemIndex(units),
                                    preferredExportUnits);

                            mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.REV_POT_ATTR_V2,
                                    revPotConv + " " + voltUnit.getNeuroML2Symbol() + ""));

                        }
                    }
                } else {
                    String xpath = NeuroMLConstants.ROOT_ELEMENT + "/" + BiophysicsConstants.ION_CHAN_ATTR_V2 + "/@" + BiophysicsConstants.SPECIES_ELEMENT_V2;
                    String ion = cmlCm.getXMLDoc().getValueByXPath(xpath);
                    if (null == ion) {
                        ion = "non_specific";
                    }
                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.ION_ATTR_V2, ion));

                }
            } else if (cm.isIonConcMechanism()) {

                String xpath = ChannelMLConstants.getIonSpeciesNameXPath();
                String ion = cmlCm.getXMLDoc().getValueByXPath(xpath);

                if (cmlCm.isNeuroML2()) {
                    xpath = NeuroMLConstants.ROOT_ELEMENT + "/" + ChannelMLConstants.ION_CONC_DEC_POOL_ELEMENT_V2 + "/@" + ChannelMLConstants.ION_ATTR_V2;
                    ion = cmlCm.getXMLDoc().getValueByXPath(xpath);
                    if (ion == null) {
                        xpath = NeuroMLConstants.ROOT_ELEMENT + "/" + ChannelMLConstants.ION_CONC_MODEL_ELEMENT_V2 + "/@" + ChannelMLConstants.ION_ATTR_V2;
                        ion = cmlCm.getXMLDoc().getValueByXPath(xpath);
                    }

                }

                mechElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, ion));

                //TODO: Remove this as id should be sufficient!!
                mechElement.addAttribute(new SimpleXMLAttribute("ion", ion));

                mechElement.addAttribute(new SimpleXMLAttribute(ChannelMLConstants.ION_CONC_MODEL_ELEMENT_V2, chanMech.getNML2Name()));
                
                ionSpeciesV2.put(ion+"_"+group, mechElement);
                    
                boolean ionPropsSet = false;
                for (IonProperties ip: cell.getIonPropertiesVsGroups().keySet()) {
                    if (ip.getName().equals(ion))
                        ionPropsSet = true;
                }
                if (!ionPropsSet) {
                    GuiUtils.showWarningMessage(logger, "A species element will be added to the NeuroML2 exported cell ("+cell.getInstanceName()+") for ion: "+ion+", "
                            + "which will specify a concentration model to manage the concentration changes.\n"
                            + " However, there are no ion properties (i.e. initial internal/external concentrations) set for "
                            + "the ion in this cell in neuroConstruct!", null);
                }

            }
        }
        
        if (group==null)
            group = nonUniformgroup;
        
        if (thisChannelsIon!=null) {
            for (IonProperties ip: cell.getIonPropertiesVsGroups().keySet()) {
                Vector<String> grps= cell.getIonPropertiesVsGroups().get(ip);
                if (thisChannelsIon.equals(ip.getName())) {
                    for (String grp: grps) {
                        if (CellTopologyHelper.isGroupASubset(group, grp, cell)) {
                            if (!ip.revPotSetByConcs()) {
                                float revPotConv = (float) UnitConverter.getVoltage(ip.getReversalPotential(),
                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                    preferredExportUnits);

                                    mechElement.setAttributeValue(BiophysicsConstants.REV_POT_ATTR_V2,
                                            revPotConv + " " + voltUnit.getNeuroML2Symbol() + "");
                            }
                        }
                    }
                }
            }
        }
        return comment;
    }


    
    /**
     * Creates NeuroML representations of the cell in the specified file. Note: may be better in the neuroml package...
     * @param cell The Cell object to export
     * @param project The nC Project
     * @param neuroMLFile The file to put the NeuroML in
     * @param level The level (currently 1 for "pure" MorphML, or 2 to include channel distributions, level 3 for net aspects)
     * as defined in NeuroMLConstants
     * @param version The NeuroML version
     * @throws ucl.physiol.neuroconstruct.cell.converters.MorphologyException
     */
    public static void saveCellInNeuroMLFormat(Cell cell, Project project, File neuroMLFile, NeuroMLLevel level, NeuroMLVersion version) throws MorphologyException
    {
        try
        {
            String nml = getCellInNeuroMLFormat(cell, project, level, version, false);

            FileWriter fw = new FileWriter(neuroMLFile);

            logger.logComment("    ****    Full XML:  ****");
            logger.logComment("  ");

            logger.logComment(nml);
            fw.write(nml);
            fw.close();
        }
        catch (IOException ex)
        {
            throw new MorphologyException("IO problem when creating MorphML file", ex);
        } 
        catch (MorphologyException ex) 
        {
            throw new MorphologyException("Problem creating MorphML file", ex);
        }
    }
    
    public static String getCellInNeuroMLFormat(Cell cell, Project project, NeuroMLLevel level, NeuroMLVersion version, boolean html) throws MorphologyException
    {
        try
        {
            logger.logComment("Going to save file in NeuroML format...");

            SimpleXMLDocument doc = new SimpleXMLDocument();
            
            SimpleXMLElement rootElement = null;
            
            if (version.isVersion2() &&
                cell.getAllChanMechNames(true).size()==1 &&
                project.cellMechanismInfo.getCellMechanism(cell.getInstanceName())!=null)
            {
                CellMechanism cm = project.cellMechanismInfo.getCellMechanism(cell.getInstanceName());
                XMLCellMechanism xcm = (XMLCellMechanism)cm;
                if (cm.isMechanismForNeuroML2Cell()) {
                    String nml2Content = GeneralUtils.readShortFile(xcm.getXMLFile(project));
                    return nml2Content;
                }
            }
            
            
            if (version.equals(NeuroMLVersion.NEUROML_VERSION_1) &&
                level.equals(NeuroMLLevel.NEUROML_LEVEL_1))
            {
                rootElement = new SimpleXMLElement(MorphMLConstants.ROOT_ELEMENT);

                rootElement.addNamespace(new SimpleXMLNamespace("", MorphMLConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                                MetadataConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                                NeuroMLConstants.XSI_URI));

                rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                            MorphMLConstants.NAMESPACE_URI
                                                            +"  " +MorphMLConstants.DEFAULT_SCHEMA_LOCATION));

            }
            else if (version.equals(NeuroMLVersion.NEUROML_VERSION_1) &&
                     (level.equals(NeuroMLLevel.NEUROML_LEVEL_2) ||
                     level.equals(NeuroMLLevel.NEUROML_LEVEL_3)))
            {
                rootElement = new SimpleXMLElement(NeuroMLConstants.ROOT_ELEMENT);

                rootElement.addNamespace(new SimpleXMLNamespace("", NeuroMLConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                                MetadataConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(MorphMLConstants.PREFIX,
                                                                MorphMLConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(BiophysicsConstants.PREFIX,
                                                                BiophysicsConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(ChannelMLConstants.PREFIX,
                                                                ChannelMLConstants.NAMESPACE_URI));

                if (level.equals(NeuroMLLevel.NEUROML_LEVEL_3))
                {
                    rootElement.addNamespace(new SimpleXMLNamespace(NetworkMLConstants.PREFIX,
                                                                    NetworkMLConstants.NAMESPACE_URI));
                }

                rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                                NeuroMLConstants.XSI_URI));

                rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                                NeuroMLConstants.NAMESPACE_URI
                                                                + "  " + NeuroMLConstants.DEFAULT_SCHEMA_LOCATION));

            }
            else if (version.isVersion2() &&
                     level.equals(NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL))
            {
                rootElement = new SimpleXMLElement(NeuroMLConstants.ROOT_ELEMENT);

                rootElement.addNamespace(new SimpleXMLNamespace("", NeuroMLConstants.NAMESPACE_URI_VERSION_2));



                rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                                NeuroMLConstants.XSI_URI));

                if (version.isVersion2alpha())
                {
                    rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                                    NeuroMLConstants.NAMESPACE_URI_VERSION_2
                                                                    + "  " + NeuroMLElements.DEFAULT_SCHEMA_LOCATION_VERSION_2_ALPHA));
                }
                else if (version.isVersion2Latest())
                {
                    rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                                    NeuroMLConstants.NAMESPACE_URI_VERSION_2
                                                                    + "  " + NeuroMLElements.LATEST_SCHEMA_LOCATION));
                }

            }

            if (!version.isVersion2())
            {
                rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, "micron"));  // keeping it old for the moment...
            }

            if (version.isVersion2())
            {
                rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.NEUROML_ID_V2, cell.getInstanceName()));  // keep it same as cell id for now
            }
            
            
            rootElement.addContent("\n\n    "); // to make it more readable...


            if (version.isVersion2())
            {
                HashMap<String, String> allChanMechsVsOrigName = new HashMap<String, String>();
                
                for (ChannelMechanism cm: cell.getChanMechsVsGroups().keySet()) { 
                    String cmNml2Name = cm.getNML2Name();
                    if (!allChanMechsVsOrigName.containsKey(cmNml2Name))
                        allChanMechsVsOrigName.put(cmNml2Name, cm.getName());
                }
                

                Set<VariableMechanism> varMechEnum = cell.getVarMechsVsParaGroups().keySet();
                for (VariableMechanism vm: varMechEnum) 
                {
                    String cmNml2Name = vm.getNML2Name();
                    if (!allChanMechsVsOrigName.containsKey(cmNml2Name))
                        allChanMechsVsOrigName.put(cmNml2Name, vm.getName());
                }
                ArrayList<String> allChanMechs = new ArrayList<String>();
                allChanMechs.addAll(allChanMechsVsOrigName.keySet());
                
                allChanMechs = (ArrayList<String>)GeneralUtils.reorderAlphabetically(allChanMechs, true);

                for(String chan: allChanMechs)                    
                {
                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(allChanMechsVsOrigName.get(chan));
                    SimpleXMLElement includeElement = new SimpleXMLElement(MorphMLConstants.INCLUDE_V2);

                    String extra = cm.isChannelMechanism() ? ProjectStructure.neuroml2ChannelExtension : "";
                    includeElement.addAttribute(MorphMLConstants.HREF_V2, chan+extra+ProjectStructure.neuroml2Extension);

                    rootElement.addChildElement(includeElement);
                    rootElement.addContent("\n\n    ");
                }
            }

            SimpleXMLElement cellElement = MorphMLConverter.getCellXMLElement(cell, project, level, version);

            if (!version.isVersion2())
            {
                SimpleXMLElement cellsElement = new SimpleXMLElement(MorphMLConstants.CELLS_ELEMENT);

                rootElement.addChildElement(cellsElement);

                cellsElement.addContent("\n        ");

                cellsElement.addChildElement(cellElement);

                cellsElement.addContent("\n    ");
            }
            else
            {
                rootElement.addChildElement(cellElement);
                rootElement.addContent("\n    ");
            }
            

            doc.addRootElement(rootElement);
            rootElement.addContent("\n"); // to make it more readable...


            String stringForm = doc.getXMLString("mmmm", html);

            return stringForm;
        }
        catch (NeuroMLException ex)
        {
            throw new MorphologyException("Problem creating MorphML file", ex);
        } 
        catch (XMLMechanismException ex) {
            
            throw new MorphologyException("Problem creating MorphML file", ex);
        }
    }



    private static SimpleXMLElement getPointElement(Point3f point, double radius, String elementName)
    {
        SimpleXMLElement pointElement = new SimpleXMLElement(elementName);

        pointElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.POINT_X_ATTR, point.x + ""));
        pointElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.POINT_Y_ATTR, point.y + ""));
        pointElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.POINT_Z_ATTR, point.z + ""));
        pointElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.POINT_DIAM_ATTR, (float)(radius * 2) + ""));

        return pointElement;
    }
    
    
    
    public static ArrayList<Cell> saveAllCellsInNeuroML(Project project, 
                                               MorphCompartmentalisation mc,
                                               NeuroMLLevel level,
                                               NeuroMLVersion version,
                                               SimConfig simConfig,
                                               File destDir) throws MorphologyException
    {

        logger.logComment("Saving the cell morphologies in NeuroML form...");
        
        Vector<Cell> cells = new Vector<Cell>();
        
        if (simConfig==null)
        {
            cells = project.cellManager.getAllCells();
        }
        else
        {
            Vector<String> cellsInc = new Vector<String>();
            for(String cg: simConfig.getCellGroups())
            {
                String thisCGCelltype = project.cellGroupsInfo.getCellType(cg);
                if (!cellsInc.contains(thisCGCelltype))
                {
                    Cell cell = project.cellManager.getCell(thisCGCelltype);
                    cells.add(cell);
                    cellsInc.add(thisCGCelltype);
                }
            }
        }
        
        ArrayList<Cell> generatedCells = new ArrayList<Cell>();
        
        for (Cell origCell : cells)
        {
            Cell mappedCell = mc.getCompartmentalisation(origCell);

            File cellFile;

            /*   if (!CellTopologyHelper.checkSimplyConnected(cell))
               {
                   GuiUtils.showErrorMessage(logger, "The cell: "+ cell.getInstanceName()
                                             + " is not Simply Connected.\n"
                                             + "This is a currently a requirement for conversion to MorphML format.\n"
             + "Try making a copy of the cell and making it Simply Connected at the Cell Type tab", null, this);
               }
               else
               {*/
        
                logger.logComment("Cell is of type: " + mappedCell.getClass().getName());
                Cell tempCell = new Cell();
                if (! (mappedCell.getClass().equals(tempCell.getClass())))
                {
                    // This is done because of problems generating MorphML for PurkinjeCell, etc.
                    // These inherit from Cell, but have all their state in the standard constructor.
                    // Saving them in JavaML format would only save the name, and not the segment positions etc.
                    mappedCell = (Cell) mappedCell.clone(); // this produced a copy which is an instance of Cell
                }

                logger.logComment("Saving cell: " + mappedCell.getInstanceName()
                                  + " in " + ProjectStructure.getMorphMLFileExtension()
                                  + " format");

                String ext = ProjectStructure.getMorphMLFileExtension();

                if (version.isVersion2())
                {
                    ext = ProjectStructure.neuroml2CellExtension+ProjectStructure.getNeuroML2FileExtension();
                }

                cellFile = new File(destDir,
                                    mappedCell.getInstanceName()
                                    + ext);

                MorphMLConverter.saveCellInNeuroMLFormat(mappedCell,project, cellFile, level, version);
                
                generatedCells.add(mappedCell);
         
            //}
        }
        return generatedCells;
    }




    @SuppressWarnings("UnusedAssignment")
    public static void main(String[] args) throws ProjectFileParsingException, MorphologyException, IOException
    {
        //File f = new File("nCexamples/Ex1_Simple/Ex1_Simple.neuro.xml");
        //File f = new File("nCexamples/Ex6_CerebellumDemo/Ex6_CerebellumDemo.ncx");

        File f = new File("osb/invertebrate/lobster/PyloricNetwork/neuroConstruct/PyloricPacemakerNetwork.ncx");
        f = new File("osb/showcase/neuroConstructShowcase/Ex10_NeuroML2/Ex10_NeuroML2.ncx");
        f = new File("osb/cerebral_cortex/neocortical_pyramidal_neuron/MainenEtAl_PyramidalCell/neuroConstruct/MainenEtAl_PyramidalCell.ncx");
        f = new File("osb/cerebral_cortex/networks/ACnet2/neuroConstruct/ACnet2.ncx");
        f = new File("testProjects/TestMorphs/TestMorphs.neuro.xml");

        f = new File("testProjects/TestMorphs/TestMorphs.neuro.xml");
        f = new File("osb/cerebellum/cerebellar_granule_cell/GranuleCell/neuroConstruct/GranuleCell.ncx");
        f = new File("osb/cerebral_cortex/neocortical_pyramidal_neuron/L5bPyrCellHayEtAl2011/neuroConstruct/L5bPyrCellHayEtAl2011.ncx");

        f = new File("osb/hippocampus/CA1_pyramidal_neuron/CA1PyramidalCell/neuroConstruct/CA1PyramidalCell.ncx");

        Project testProj = Project.loadProject(f,new ProjectEventListener()
        {
            public void tableDataModelUpdated(String tableModelName)
            {};

            public void tabUpdated(String tabName)
            {};
             public void cellMechanismUpdated()
             {
             };

        });

        Vector<Cell> cells = testProj.cellManager.getAllCells();
        cells = (Vector<Cell>)GeneralUtils.reorderAlphabetically(cells, true);
         System.out.println("Cells: "+cells);
        Cell cell = cells.get(0);

         System.out.println("Found a cell: "+ cell);

        //cell.getFirstSomaSegment().setComment("This is the cell root...");

        File nml_l1File = new File("../temp/cell_l1.xml");
        File nml_l2File = new File("../temp/cell_l2.xml");
        File nml2File = new File("../temp/cell2.xml");

        /*

        System.out.println("Cell details: " + CellTopologyHelper.printDetails(cell, null));

        logger.logComment("Saving the file as: " + neuroMLFile.getAbsolutePath());

        MorphMLConverter.saveCellInNeuroMLFormat(cell, neuroMLFile, NeuroMLConstants.NeuroMLLevel_2);
        */

       MorphMLConverter.saveCellInNeuroMLFormat(cell, testProj,  nml_l1File, NeuroMLLevel.NEUROML_LEVEL_1, NeuroMLVersion.NEUROML_VERSION_1);
       System.out.println("Saved MML file as: " + nml_l1File.getCanonicalPath());

       MorphMLConverter.saveCellInNeuroMLFormat(cell, testProj,  nml_l2File, NeuroMLLevel.NEUROML_LEVEL_2, NeuroMLVersion.NEUROML_VERSION_1);
       System.out.println("Saved MML file as: " + nml_l2File.getCanonicalPath());

       MorphMLConverter.saveCellInNeuroMLFormat(cell, testProj,  nml2File, NeuroMLLevel.NEUROML_VERSION_2_SPIKING_CELL, NeuroMLVersion.getLatestVersion());
       System.out.println("Saved MML file as: " + nml2File.getCanonicalPath());


    }

}
