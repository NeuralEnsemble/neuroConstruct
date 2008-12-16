/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.cell.converters;

import java.beans.*;
import java.io.*;
import java.util.*;

import javax.vecmath.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.compartmentalisation.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.mechanisms.*;
import ucl.physiol.neuroconstruct.neuroml.*;
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


public class MorphMLConverter extends FormatImporter
{
    static ClassLogger logger = new ClassLogger("MorphMLConverter");

    //public static String MML_ROOT = "cells";
    //public static String MML_CELLS = "cells";
    
    String warning = "";
    
    /*
     * If true use the elements with pre 1.7.1 naming conventions, e.g. passiveConductance not 
     */
    private static boolean usePreV1_7_1Format = false;

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
        catch (Exception e)
        {
            throw new MorphologyException("Problem converting the morphology file: "+ javaXMLFile, e);
        }
    }
    
    @Override
    public String getWarnings()
    {
        return this.warning;
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
        catch (Exception e)
        {
            throw new MorphologyException(error, e);
        }
    }





/* Old way of doing it...
    public Cell loadFromMorphologyFile(File morphologyFile, String name) throws MorphologyException
    {
        try
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();

            StreamSource xls = new StreamSource(ProjectStructure.getMorphMLToJavaXMLFile());

            Transformer transformer = tFactory.newTransformer(xls);


            File tempFile = new File(morphologyFile.getParent()
                                     + System.getProperty("file.separator")
                                     + morphologyFile.getName()+ "_temp.xml");

            StreamResult resultantXML = new StreamResult(tempFile);

            transformer.transform(new StreamSource(morphologyFile), resultantXML);

            System.out.println("Temporary Java XML created: " + tempFile.getAbsolutePath());

            System.out.println("Decoding Java XML...");
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(tempFile)));
            decoder.close();
            Cell cell = (Cell) decoder.readObject();

            if (name!=null)
            {
                cell.setInstanceName(name);
            }
            System.out.println("Finished decoding Java XML...");

            //tempFile.delete();

            return cell;

        }
        catch (Exception e)
        {
            throw new MorphologyException("Problem converting the morphology file: "+ morphologyFile, e);
        }

    }
*/

    public Cell loadFromMorphologyFile(File morphologyFile, String name) throws MorphologyException
    {

        logger.logComment("-----   Starting decoding...");

        logger.logComment("MorphML File " + morphologyFile + " being used to create cell: " + name);

        try
        {

            logger.logComment("Loading mml cell from " + morphologyFile.getAbsolutePath());

            FileInputStream instream = null;
            InputSource is = null;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            MorphMLReader mmlBuilder = new MorphMLReader();
            xmlReader.setContentHandler(mmlBuilder);

            instream = new FileInputStream(morphologyFile);

            is = new InputSource(instream);

            xmlReader.parse(is);

            Cell cell = mmlBuilder.getBuiltCell();

            cell.setInstanceName(name);

            logger.logComment("Cell which has been built: ");
            logger.logComment(CellTopologyHelper.printShortDetails(cell));

            logger.logComment("-----   Finished decoding...");
            
            this.warning = mmlBuilder.getWarnings();
            
            return cell;
        }
        catch (Exception e)
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

        String comment = new String("\n<!-- Note that this XML is specific to the neuroConstruct Java cell object model and not any part of the NeuroML framework -->\n\n");

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


    public static SimpleXMLElement getCellXMLElement(Cell cell, Project project, String level) throws NeuroMLException, CMLMechNotInitException, ChannelMLException
    {
            String mmlPrefix = "";
            if (!level.equals(NeuroMLConstants.NEUROML_LEVEL_1))
                mmlPrefix = MorphMLConstants.PREFIX + ":";
        
            String metadataPrefix = MetadataConstants.PREFIX + ":";
            
            SimpleXMLElement descElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.NOTES_ELEMENT);
            
            descElement.addContent("This "+level+" NeuroML file (specification version "
                    +GeneralProperties.getNeuroMLVersionNumber()
                    +") has been generated by neuroConstruct v"+GeneralProperties.getVersionNumber());
            
            SimpleXMLElement cellElement = new SimpleXMLElement(MorphMLConstants.CELL_ELEMENT);
            //cellsElement.addChildElement(cellElement);

            SimpleXMLAttribute nameAttr = new SimpleXMLAttribute(MorphMLConstants.CELL_NAME_ATTR,
                                                                 cell.getInstanceName());
            cellElement.addAttribute(nameAttr);

            if (cell.getCellDescription()!=null)
            {
                descElement = new SimpleXMLElement(metadataPrefix + MetadataConstants.NOTES_ELEMENT);
                descElement.addContent(cell.getCellDescription());
                cellElement.addChildElement(descElement);
            }
            
            
            SimpleXMLElement segmentsElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.SEGMENTS_ELEMENT);
            cellElement.addChildElement(segmentsElement);

            Vector allSegments = cell.getAllSegments();
            ArrayList<Section> allSections = cell.getAllSections();


            for (int i = 0; i < allSegments.size(); i++)
            {
                Segment nextSegment = (Segment) allSegments.elementAt(i);
                SimpleXMLElement segmentElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.SEGMENT_ELEMENT);

                segmentsElement.addChildElement(segmentElement);

                segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_ID_ATTR,
                                                                   nextSegment.getSegmentId() + ""));

                segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_NAME_ATTR,
                                                                   nextSegment.getSegmentName()));

                if (nextSegment.getParentSegment() != null)
                {
                    segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_PARENT_ATTR,
                                                                       nextSegment.getParentSegment().getSegmentId() + ""));
                }

                int sectionId = allSections.indexOf(nextSegment.getSection());

                segmentElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.SEGMENT_CABLE_ID_ATTR,
                                                                   sectionId + ""));

                if (nextSegment.isFirstSectionSegment())
                {
                    segmentElement.addChildElement(getPointElement(nextSegment.getStartPointPosition(),
                                                                   nextSegment.getSegmentStartRadius(),
                                                                   mmlPrefix+
                                                                   MorphMLConstants.SEGMENT_PROXIMAL_ELEMENT));

                    segmentElement.addContent("\n                "); // to make it more readable...
                }
                segmentElement.addChildElement(getPointElement(nextSegment.getEndPointPosition(),
                                                               nextSegment.getRadius(),
                                                               mmlPrefix+
                                                               MorphMLConstants.SEGMENT_DISTAL_ELEMENT));


                segmentElement.addContent("\n            "); // to make it more readable...

                SimpleXMLElement props = null;


                if (nextSegment.getComment()!=null)
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
                                                 nextSegment.getComment(), "                    ");
                    
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
                                                  "                        ");
                }


            }

            SimpleXMLElement cablesElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLES_ELEMENT);
            cellElement.addChildElement(cablesElement);
            
            boolean useCableGroup  = false;
            
            if (cell.getParameterisedGroups().size()>0)
                useCableGroup  = true;

            for (int i = 0; i < allSections.size(); i++)
            {
                Section nextSection =  allSections.get(i);

                SimpleXMLElement cableElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLE_ELEMENT);

                cablesElement.addChildElement(cableElement);

                cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_ID_ATTR, i + ""));
                cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_NAME_ATTR, nextSection.getSectionName()));

                SimpleXMLElement props = new SimpleXMLElement(MetadataConstants.PREFIX + ":" + MorphMLConstants.PROPS_ELEMENT);

                if (nextSection.getNumberInternalDivisions()!=1)
                {
                    cableElement.addContent("\n    "); // to make it more readable...
                    cableElement.addChildElement(props);
                    props.addContent("\n                        "); // to make it more readable...

                    MetadataConstants.addProperty(props,
                                MorphMLConstants.NUMBER_INTERNAL_DIVS_PROP,
                                nextSection.getNumberInternalDivisions() + "",
                                "                        ");
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
                                "                        ");

                }

                if (!useCableGroup)
                {
                    Vector groups = nextSection.getGroups();
                    for (int j = 0; j < groups.size(); j++)
                    {
                        SimpleXMLElement grpElement = new SimpleXMLElement(metadataPrefix+ MetadataConstants.GROUP_ELEMENT);

                        grpElement.addContent( (String) groups.elementAt(j));
                        cableElement.addChildElement(grpElement);
                    }
                }
                else
                {
                    if (i < allSections.size()-1)
                        cablesElement.addContent("\n            "); // to make it more readable...
                    else
                        cablesElement.addContent("\n        "); // to make it more readable...
                        
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
            if (useCableGroup)
            {
                
                cablesElement.addContent("\n            "); // to make it more readable...
                //TODO: Check there isn't a more efficient way to do this...
                Vector<String> groups = cell.getAllGroupNames();
                
                for(String group: groups)
                {
                    ArrayList<Section> secs = cell.getSectionsInGroup(group);
                    
                    SimpleXMLElement cableGroupElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLE_GROUP_ELEMENT);
                    cableGroupElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_GROUP_NAME, group));
                    
                    cablesElement.addChildElement(cableGroupElement);
                    for(Section sec: secs)
                    {
                        SimpleXMLElement cableElement = new SimpleXMLElement(mmlPrefix+MorphMLConstants.CABLE_ELEMENT);
                        
                        int sectionId = allSections.indexOf(sec);
                        cableElement.addAttribute(new SimpleXMLAttribute(MorphMLConstants.CABLE_ID_ATTR, sectionId+""));
                        
                        cableGroupElement.addContent("\n                "); // to make it more readable...
                        cableGroupElement.addChildElement(cableElement);
                
                    }
                    
                    cableGroupElement.addContent("\n            "); // to make it more readable...
                    cablesElement.addContent("\n            "); // to make it more readable...
                    
                }
                
            }

            if (!level.equals(NeuroMLConstants.NEUROML_LEVEL_1) )
            {
                cellElement.addComment(new SimpleXMLComment("Adding the biophysical parameters"));

                SimpleXMLElement bioElement = new SimpleXMLElement(BiophysicsConstants.ROOT_ELEMENT);

                bioElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.UNITS_ATTR,
                                                               BiophysicsConstants.UNITS_PHYSIOLOGICAL));

                cellElement.addChildElement(bioElement);

                String prefix = BiophysicsConstants.PREFIX + ":";

                ArrayList<ChannelMechanism> allChanMechs = cell.getAllFixedChannelMechanisms(true);

                for (int j = 0; j < allChanMechs.size(); j++)
                {
                    ChannelMechanism chanMech = allChanMechs.get(j);

                    SimpleXMLElement mechElement = new SimpleXMLElement(prefix + BiophysicsConstants.MECHANISM_ELEMENT);
                    bioElement.addChildElement(mechElement);

                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_NAME_ATTR,
                                                                    chanMech.getName()));

                    mechElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.MECHANISM_TYPE_ATTR,
                                                                    BiophysicsConstants.MECHANISM_TYPE_CHAN_MECH));
                    

                    ArrayList<SimpleXMLElement> allParamGrps = new ArrayList<SimpleXMLElement>();                                         

                    SimpleXMLElement gmaxParamElement = new SimpleXMLElement(prefix + BiophysicsConstants.PARAMETER_ELEMENT);

                    gmaxParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR,
                                                                     BiophysicsConstants.PARAMETER_GMAX));

                    gmaxParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                                                     UnitConverter.getConductanceDensity(chanMech.getDensity(),
                        UnitConverter.NEUROCONSTRUCT_UNITS,
                        UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + ""));

                    allParamGrps.add(gmaxParamElement);
                    
                    ArrayList<MechParameter> mps = chanMech.getExtraParameters();
                    
                    for(MechParameter mp: mps)
                    {                            

                        SimpleXMLElement pe = new SimpleXMLElement(prefix + BiophysicsConstants.PARAMETER_ELEMENT);

                        pe.addComment(new SimpleXMLComment("Note: Units of extra parameters are not known!!"));
                        
                        pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_NAME_ATTR,
                                                                         mp.getName()));

                        pe.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                                                         mp.getValue()+""));
                        
                        allParamGrps.add(pe);

                    }
                        
                    
                    CellMechanism cm = project.cellMechanismInfo.getCellMechanism(chanMech.getName());
                    
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

                            SimpleXMLElement revPotParamElement = new SimpleXMLElement(prefix + BiophysicsConstants.PARAMETER_ELEMENT);

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
                            
                            for(MechParameter mp:chanMech.getExtraParameters())
                            {
                                if (mp.getName().equals(BiophysicsConstants.PARAMETER_REV_POT))
                                {
                                    logger.logComment("The reversal potential has ben set as one of the extra params");
                                    
                                    revPot=mp.getValue(); 
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

                            revPotParamElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                                                             (float)UnitConverter.getVoltage(revPot,
                                UnitConverter.NEUROCONSTRUCT_UNITS,
                                UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + ""));
                            

                            mechElement.addChildElement(revPotParamElement);
                            
                            Vector<String> groups = cell.getGroupsWithChanMech(chanMech);

                            for (int k = 0; k < groups.size(); k++)
                            {
                                SimpleXMLElement groupElement = new SimpleXMLElement(prefix + BiophysicsConstants.GROUP_ELEMENT);
                                revPotParamElement.addChildElement(groupElement);
                                groupElement.addContent(groups.get(k));

                            }
                        }
                    }


                    Vector<String> groups = cell.getGroupsWithChanMech(chanMech);

                    for(SimpleXMLElement paramElement: allParamGrps)
                    {
                        mechElement.addChildElement(paramElement);
                        
                        for (int k = 0; k < groups.size(); k++)
                        {
                            SimpleXMLElement groupElement = new SimpleXMLElement(prefix + BiophysicsConstants.GROUP_ELEMENT);
                            paramElement.addChildElement(groupElement);
                            groupElement.addContent(groups.get(k));

                        }
                    }
                }

                SimpleXMLElement specCapElement = new SimpleXMLElement(prefix + BiophysicsConstants.SPECIFIC_CAP_ELEMENT);
                
                if (usePreV1_7_1Format) 
                    specCapElement = new SimpleXMLElement(prefix + BiophysicsConstants.SPECIFIC_CAP_ELEMENT_pre_v1_7_1);

                bioElement.addChildElement(specCapElement);

                ArrayList<Float> specCaps = cell.getDefinedSpecCaps();
                logger.logComment("    ...    specCaps: " + specCaps);

                for (Float specCap : specCaps)
                {
                    SimpleXMLElement paramElement = new SimpleXMLElement(prefix + BiophysicsConstants.PARAMETER_ELEMENT);

                    paramElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                                                     UnitConverter.getSpecificCapacitance(specCap,
                        UnitConverter.NEUROCONSTRUCT_UNITS,
                        UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + ""));

                    specCapElement.addChildElement(paramElement);

                    Vector<String> groups = cell.getGroupsWithSpecCap(specCap);

                    for (String group : groups)
                    {
                        SimpleXMLElement groupElement2 = new SimpleXMLElement(prefix + BiophysicsConstants.GROUP_ELEMENT);
                        paramElement.addChildElement(groupElement2);
                        groupElement2.addContent(group);

                    }

                }

                SimpleXMLElement specAxResElement = new SimpleXMLElement(prefix + BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT);
                
                if (usePreV1_7_1Format)
                    specAxResElement = new SimpleXMLElement(prefix + BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1);

                bioElement.addChildElement(specAxResElement);

                ArrayList<Float> specAxReses = cell.getDefinedSpecAxResistances();
                logger.logComment("specAxReses: " + specAxReses);

                for (Float specAxRes : specAxReses)
                {
                    SimpleXMLElement paramElement = new SimpleXMLElement(prefix + BiophysicsConstants.PARAMETER_ELEMENT);

                    paramElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                                                     UnitConverter.getSpecificAxialResistance(specAxRes,
                        UnitConverter.NEUROCONSTRUCT_UNITS,
                        UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + ""));

                    specAxResElement.addChildElement(paramElement);

                    Vector<String> groups = cell.getGroupsWithSpecAxRes(specAxRes);

                    for (String group : groups)
                    {
                        SimpleXMLElement groupElement3 = new SimpleXMLElement(prefix + BiophysicsConstants.GROUP_ELEMENT);
                        paramElement.addChildElement(groupElement3);
                        groupElement3.addContent(group);
                    }
                }


                SimpleXMLElement initPotElement = new SimpleXMLElement(prefix+BiophysicsConstants.INITIAL_POT_ELEMENT);
                
                if (usePreV1_7_1Format)
                    initPotElement = new SimpleXMLElement(prefix+BiophysicsConstants.INITIAL_POT_ELEMENT_pre_v1_7_1);

                bioElement.addChildElement(initPotElement);

                SimpleXMLElement paramElement = new SimpleXMLElement(prefix + BiophysicsConstants.PARAMETER_ELEMENT);

                paramElement.addAttribute(new SimpleXMLAttribute(BiophysicsConstants.PARAMETER_VALUE_ATTR,
                                                                 UnitConverter.getVoltage(cell.getInitialPotential().
                    getNominalNumber(),
                    UnitConverter.NEUROCONSTRUCT_UNITS,
                    UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + ""));


                initPotElement.addChildElement(paramElement);


               SimpleXMLElement groupElement1 = new SimpleXMLElement(prefix+BiophysicsConstants.GROUP_ELEMENT);
               paramElement.addChildElement(groupElement1);
               groupElement1.addContent("all");


               if (!level.equals(NeuroMLConstants.NEUROML_LEVEL_2))
               {

                   String netPrefix = NetworkMLConstants.PREFIX + ":";

                   ArrayList<String> allSyns = cell.getAllAllowedSynapseTypes();
                   
                   
                   
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
                           bioElement.addChildElement(potSynLocEl);
                       }
                       else
                       {
                           
                           SimpleXMLElement potSynLocEl = new SimpleXMLElement(netPrefix+NetworkMLConstants.POT_SYN_LOC_ELEMENT);
                           
                           connectEl.addChildElement(potSynLocEl);
                           
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
                               potSynLocEl.addChildElement(grpEl);
                           }
                       }

                   }
                   
                   if (!usePreV1_7_1Format) cellElement.addChildElement(connectEl);

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
                       bioElement.addComment(comm);
                   }

                   Vector<AxonalConnRegion> axC = cell.getAxonalArbours();

                   if (axC.size()>0)
                   {
                       String warn = "\nNote that the following Volume based connection arbours are specified for the cell, but this is not yet supported by NeuroML!";

                       for (int i = 0; i < axC.size(); i++)
                       {
                           warn = warn +"\n  - "+axC.get(i);
                       }
                       warn = warn +"\n";
                       SimpleXMLComment comm = new SimpleXMLComment(warn);
                       bioElement.addComment(comm);

                   }

               }
            }
            
            return cellElement;
    }
    
    
    
    /**
     * Creates NeuroML representations of the cell in the specified file. Note: may be better in the neuroml package...
     * @param cell The Cell object to export
     * @param neuroMLFile The file to put the NeuroML in
     * @param level The level (currently 1 for "pure" MorphML, or 2 to include channel distributions, level 3 for net aspects)
     * as defined in NeuroMLConstants
     */
    public static void saveCellInNeuroMLFormat(Cell cell, Project project, File neuroMLFile, String level) throws MorphologyException
    {
        try
        {
            logger.logComment("Going to save file in NeuroML format: " + neuroMLFile);

            SimpleXMLDocument doc = new SimpleXMLDocument();
            
            SimpleXMLElement rootElement = null;
            
            if (level.equals(NeuroMLConstants.NEUROML_LEVEL_1))
            {
                rootElement = new SimpleXMLElement(MorphMLConstants.ROOT_ELEMENT);

                rootElement.addNamespace(new SimpleXMLNamespace("", MorphMLConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(MetadataConstants.PREFIX,
                                                                MetadataConstants.NAMESPACE_URI));

                rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                                NeuroMLConstants.XSI_URI));

                rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                            MorphMLConstants.NAMESPACE_URI
                                                            +"  " +MorphMLConstants.DEFAULT_SCHEMA_FILENAME));

            }
            else if (level.equals(NeuroMLConstants.NEUROML_LEVEL_2) ||
                     level.equals(NeuroMLConstants.NEUROML_LEVEL_3))
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

                if (level.equals(NeuroMLConstants.NEUROML_LEVEL_3))
                {
                    rootElement.addNamespace(new SimpleXMLNamespace(NetworkMLConstants.PREFIX,
                                                                    NetworkMLConstants.NAMESPACE_URI));
                }

                rootElement.addNamespace(new SimpleXMLNamespace(NeuroMLConstants.XSI_PREFIX,
                                                                NeuroMLConstants.XSI_URI));

                rootElement.addAttribute(new SimpleXMLAttribute(NeuroMLConstants.XSI_SCHEMA_LOC,
                                                                NeuroMLConstants.NAMESPACE_URI
                                                                + "  " + NeuroMLConstants.DEFAULT_SCHEMA_FILENAME));

            }


            rootElement.addAttribute(new SimpleXMLAttribute(MetadataConstants.LENGTH_UNITS_OLD, "micron"));  // keeping it old for the moment...
            
            
            rootElement.addContent("\n"); // to make it more readable...

            SimpleXMLElement cellsElement = new SimpleXMLElement(MorphMLConstants.CELLS_ELEMENT);

            cellsElement.addChildElement(MorphMLConverter.getCellXMLElement(cell, project, level));
            
            rootElement.addChildElement(cellsElement);

            doc.addRootElement(rootElement);

            FileWriter fw = new FileWriter(neuroMLFile);

            logger.logComment("    ****    Full XML:  ****");
            logger.logComment("  ");

            String stringForm = doc.getXMLString("mmmm", false);

            logger.logComment(stringForm);
            fw.write(stringForm);
            fw.close();
        }
        catch (Exception ex)
        {
            throw new MorphologyException(neuroMLFile.getAbsolutePath(), "Problem creating MorphML file", ex);
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
                                               String level,
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

            File cellFile = null;

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

                cellFile = new File(destDir,
                                    mappedCell.getInstanceName()
                                    + ProjectStructure.getMorphMLFileExtension());

                MorphMLConverter.saveCellInNeuroMLFormat(mappedCell,project, cellFile, level);
                
                generatedCells.add(mappedCell);
         
            //}
        }
        return generatedCells;
    }





    public static void main(String[] args)
    {
        //File f = new File("");

        //C:\neuroConstruct\projects\Simple\generatedMorphML\CellType_25.morph.xml
        //File neuroMLFile = new File("C:\\neuroConstruct\\morphml\\JavaXMLToMorphML\\XmlMorphReader.morphml.xml");

        //File neuroMLFile = new File("C:\\neuroConstruct\\projects\\DentateGyrus_copy\\generatedMorphML\\GranuleCell.morph.xml");
        //File javaXMLFile = new File("C:\\neuroConstruct\\projects\\Project_1\\CellType_1.java.xml");


        try
        {

           Project testProj = Project.loadProject(new File("examples/Ex1-Simple/Ex1-Simple.neuro.xml"),
                                                  new ProjectEventListener()
           {
               public void tableDataModelUpdated(String tableModelName)
               {};

               public void tabUpdated(String tabName)
               {};
                public void cellMechanismUpdated()
                {
                };

           });

           Cell cell = testProj.cellManager.getAllCells().firstElement();

           cell.getFirstSomaSegment().setComment("This is the cell root...");

           cell.getAllSegments().elementAt(4).setFractionAlongParent(0.5f);

           File oosFile = new File("../temp/cell.oos");
           File jXmlFile = new File("../temp/cell.xml");
           File mmlFile = new File("../temp/cell.mml");

           /*

           System.out.println("Cell details: " + CellTopologyHelper.printDetails(cell, null));

           logger.logComment("Saving the file as: " + neuroMLFile.getAbsolutePath());

           MorphMLConverter.saveCellInNeuroMLFormat(cell, neuroMLFile, NeuroMLConstants.NEUROML_LEVEL_2);
           */

          MorphMLConverter.saveCellInJavaObjFormat(cell, oosFile);
          MorphMLConverter.saveCellInJavaXMLFormat(cell, jXmlFile);
          MorphMLConverter.saveCellInNeuroMLFormat(cell, testProj,  mmlFile, NeuroMLConstants.NEUROML_LEVEL_2);


          System.out.println("Saved MML file as: " + mmlFile.getCanonicalPath());

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}