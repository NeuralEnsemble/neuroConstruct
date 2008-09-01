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

import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.units.*;
import javax.xml.parsers.SAXParserFactory;

/**
 * MorphML file Reader. Importer of MorphML/NeuroML Level1/2/3 files to 
 * neuroConstruct format using SAX
 *
 * @author Padraig Gleeson
 *  
 */

public class MorphMLReader extends XMLFilterImpl
{
    private static ClassLogger logger = new ClassLogger("MorphMLReader");
    
    // If getAncestorElement is called for non existent ancestors...
    private static String NULL_ELEMENT = "--- Null Element ---";

    private StringBuffer warnings = new StringBuffer();

    private String importationComment = "Importation comment: ";

    private Cell cell = null;

    private Stack<String> elementStack = new Stack<String>();

    private String unitsUsed = null;

    private boolean foundSomaSection = false;

    private Hashtable<Integer,Section> sections = new Hashtable<Integer,Section>();
    private Hashtable<Integer,Segment> segments = new Hashtable<Integer,Segment>();

    private Segment currentSegment = null;

    private ArrayList<Integer> finiteVolFound = new ArrayList<Integer>();

    private Section currentSection = null;

    private String currentSynType = null;

    private String currentMechName = null;

    private float currentSpecAxRes = Float.NaN;
    private float currentSpecCap = Float.NaN;


    private String currentSectionGroup = null;

    private StringBuffer notesContents = new StringBuffer();

    private String currentMechType = null;
    private String currentParamName = null;
    private float currentParamValueNconUnits = -1;

    //private ChannelMechanism currentChanMech = null;

    private String currentPropertyTag = null;

    String metadataPrefix = MetadataConstants.PREFIX + ":";

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String contents = new String(ch, start, length);

        //logger.logComment("Contents: "+ contents);

        if (contents.trim().length() > 0)
        {
            logger.logComment("Got a string: (" + contents + ") at: "+ elementStack);

            if (getParentElement().equals(MorphMLConstants.CELL_ELEMENT) &&
                getCurrentElement().equals(MetadataConstants.NOTES_ELEMENT))
            {
                //cell.setCellDescription(contents);
                notesContents.append(contents);
            }

            else if (getCurrentElement().equals(MetadataConstants.GROUP_ELEMENT) &&
                     this.getAncestorElement(1).equals(MorphMLConstants.CABLE_ELEMENT))
            {

                this.currentSection.addToGroup(contents);

                // Ensure soma segs are finite vol unless something already specified for it
                if (contents.equals(MorphMLConstants.SOMA_CABLE_GROUP))
                {
                    for (Segment seg: cell.getAllSegmentsInSection(currentSection))
                    {
                        if (!this.finiteVolFound.contains(seg.getSegmentId()))
                            seg.setFiniteVolume(true);
                    }

                    this.foundSomaSection = true;
                }
            }

            else if (getAncestorElement(3).equals(MorphMLConstants.CABLE_ELEMENT) &&
                     getAncestorElement(2).equals(MorphMLConstants.PROPS_ELEMENT) &&
                     getAncestorElement(1).equals(MetadataConstants.PROP_ELEMENT))
            {
                logger.logComment("Looking at a property...");

                if (getCurrentElement().equals(MetadataConstants.PROP_TAG_ELEMENT))
                {
                    currentPropertyTag = contents;
                }
                if (getCurrentElement().equals(MetadataConstants.PROP_VALUE_ELEMENT))
                {
                    if (currentPropertyTag.equals(MorphMLConstants.NUMBER_INTERNAL_DIVS_PROP))
                    {
                        this.currentSection.setNumberInternalDivisions(Integer.parseInt(contents));
                    }
                }
            }
            else if (getAncestorElement(3).equals(MorphMLConstants.SEGMENT_ELEMENT) &&
                     getAncestorElement(2).equals(MorphMLConstants.PROPS_ELEMENT) &&
                     getAncestorElement(1).equals(MetadataConstants.PROP_ELEMENT))
            {
                logger.logComment("Looking at a property...");

                if (getCurrentElement().equals(MetadataConstants.PROP_TAG_ELEMENT))
                {
                    currentPropertyTag = contents;
                }
                if (getCurrentElement().equals(MetadataConstants.PROP_VALUE_ELEMENT))
                {
                    if (currentPropertyTag.equals(MorphMLConstants.COMMENT_PROP))
                    {
                        if (currentSegment.getComment()!=null)
                        {
                            currentSegment.setComment(currentSegment.getComment() + " " + contents);
                        }
                        currentSegment.setComment(contents);
                    }
                    else if (currentPropertyTag.equals(MorphMLConstants.FRACT_ALONG_PARENT_ATTR) || 
                        currentPropertyTag.equals(MorphMLConstants.FRACT_ALONG_PARENT_ATTR_pre_v1_7_1))
                    {
                        currentSegment.setFractionAlongParent(Float.parseFloat(contents));
                    }
                    else if (currentPropertyTag.equals(MorphMLConstants.FINITE_VOL_PROP))
                    {
                        currentSegment.setFiniteVolume(Boolean.parseBoolean(contents));
                        finiteVolFound.add(currentSegment.getSegmentId());
                    }
                    //currentPropertyTag = null;
                }

            }

            else if (getCurrentElement().equals(NetworkMLConstants.SYN_TYPE_ELEMENT) &&
                     this.getAncestorElement(1).equals(NetworkMLConstants.POT_SYN_LOC_ELEMENT_preV1_7_1))
            {
                logger.logComment("Found a syn type el: " + contents);

                currentSynType = contents;

            }

            else if (getCurrentElement().equals(NetworkMLConstants.GROUP_ELEMENT) &&
                     (this.getAncestorElement(1).equals(NetworkMLConstants.POT_SYN_LOC_ELEMENT_preV1_7_1)||
                      this.getAncestorElement(1).equals(NetworkMLConstants.POT_SYN_LOC_ELEMENT)))
            {
                logger.logComment("Found a syn group: " + contents);

                cell.associateGroupWithSynapse(contents, currentSynType);

            }

            else if (getCurrentElement().equals(BiophysicsConstants.GROUP_ELEMENT) &&
                     this.getAncestorElement(1).equals(BiophysicsConstants.PARAMETER_ELEMENT) &&
                     this.getAncestorElement(2).equals(BiophysicsConstants.MECHANISM_ELEMENT))
            {
                String group = contents;
                logger.logComment("Found a group: "+group+" for the biophysics mech");

                if (this.currentMechType.equals(BiophysicsConstants.MECHANISM_TYPE_SYN_LOC))
                {
                    cell.associateGroupWithSynapse(group, this.currentMechName);
                }
                else if (this.currentMechType.equals(BiophysicsConstants.MECHANISM_TYPE_CHAN_MECH))
                {
                    if (this.currentParamName.equals(BiophysicsConstants.PARAMETER_GMAX))
                    {
                        ChannelMechanism cm = new ChannelMechanism(currentMechName, this.currentParamValueNconUnits);
                        logger.logComment("Setting value: "+ currentParamValueNconUnits
                            +" for parameter: "+ currentParamName+ " on mechanism: "+ currentMechName+", in group: "+group+"\n");
                        
                        cell.associateGroupWithChanMech(group, cm);
                    }
                    else
                    {
                        //warnings.append("Unable to set value: "+ currentParamValueNconUnits
                        //    +" for parameter: "+ currentParamName+ " on mechanism: "+ currentMechName+", in group: "+group+"\n");
                        
                        ChannelMechanism cm = new ChannelMechanism(currentMechName, -1);
                        
                        logger.logComment("Setting value: "+ currentParamValueNconUnits
                            +" for parameter: "+ currentParamName+ " on mechanism: "+ currentMechName+", in group: "+group+"\n");
                        
                        MechParameter mp = new MechParameter(currentParamName, currentParamValueNconUnits);
                        
                        cm.getExtraParameters().add(mp);
                        
                        cell.associateGroupWithChanMech(group, cm);
                        
                    }
                    //cell.associateGroupWithChanMech(contents, this.currentChanMech);
                }
            }
            else if (getCurrentElement().equals(BiophysicsConstants.GROUP_ELEMENT) &&
                     this.getAncestorElement(1).equals(BiophysicsConstants.PARAMETER_ELEMENT) &&
                     (this.getAncestorElement(2).equals(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT) ||
                     this.getAncestorElement(2).equals(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1)))
            {
                logger.logComment("Found a group: " + contents + " for spec ax res");

                cell.associateGroupWithSpecAxRes(contents, this.currentSpecAxRes);
            }
            else if (getCurrentElement().equals(BiophysicsConstants.GROUP_ELEMENT) &&
                     this.getAncestorElement(1).equals(BiophysicsConstants.PARAMETER_ELEMENT) &&
                     (this.getAncestorElement(2).equals(BiophysicsConstants.SPECIFIC_CAP_ELEMENT) ||
                     this.getAncestorElement(2).equals(BiophysicsConstants.SPECIFIC_CAP_ELEMENT_pre_v1_7_1)))
            {
                logger.logComment("Found a group: " + contents + " for spec cap");

                cell.associateGroupWithSpecCap(contents, this.currentSpecCap);
            }


        }
    }

    public Cell getBuiltCell()
    {
        return cell;
    }

    public String getWarnings()
    {
        return warnings.toString();
    }

    @Override
    public void startDocument()
    {
        logger.logComment("startDocument...");

        cell = new Cell();
    }

    @Override
    public void endDocument()
    {
        CellTopologyHelper.zeroFirstSomaSegId(cell);
        CellTopologyHelper.reorderSegsParentsFirst(cell);
    }

    public String getCurrentElement()
    {
        return elementStack.peek();
    }

    public String getParentElement()
    {
        return getAncestorElement(1);
    }

    /**
     * Taking the child parent thing to it's logical extension...
     * parent element is 1 generation back, parent's parent is 2 back, etc.
     */
    public String getAncestorElement(int generationsBack)
    {
        if (elementStack.size()<generationsBack+1) return NULL_ELEMENT;
        return elementStack.elementAt(elementStack.size()-(generationsBack+1));
    }





    public void setCurrentElement(String newElement)
    {
        this.elementStack.push(newElement);

         logger.logComment("Elements: "+ elementStack);

     }

     public void stepDownElement()
     {
         elementStack.pop();
     }


    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes)
    throws SAXException
    {
        logger.logComment("\n                                            -----   Start element: namespaceURI: " + namespaceURI
                           + ", localName: " + localName
                           + ", qName: " + qName);


         int attrsLength = attributes.getLength();
         for (int i = 0; i < attrsLength; i++)
         {
             String name = attributes.getLocalName(i);
             String val = attributes.getValue(i);

             logger.logComment("Attr name: " + name+ ", val: " + val+ ", qname: "
                               + attributes.getQName(i)+ ", uri: " + attributes.getURI(i));

         }

         //setCurrentElement(qName);
         setCurrentElement(localName);

         if (getCurrentElement().equals(MorphMLConstants.CELL_ELEMENT))
         {
             logger.logComment("                                    Cell element....");
             String name = attributes.getValue(MorphMLConstants.CELL_NAME_ATTR);
             cell.setInstanceName(name);
         }

         else if (getCurrentElement().equals(MorphMLConstants.SEGMENT_ELEMENT))
         {
             logger.logComment("\n                           +++++    New Segment!!  +++++");


             String id = attributes.getValue(MorphMLConstants.SEGMENT_ID_ATTR);
             String name = attributes.getValue(MorphMLConstants.SEGMENT_NAME_ATTR);
             String sectionId = attributes.getValue(MorphMLConstants.SEGMENT_CABLE_ID_ATTR);

             logger.logComment("ID: "+id+", name: "+ name);

             Segment newSeg = new Segment();
             newSeg.setSegmentId(Integer.parseInt(id));
             newSeg.setSegmentName(name);

             Section section = null;

             Integer sectionIdInteger = new Integer(sectionId);

             if (this.sections.containsKey(sectionIdInteger))
             {
                 section = sections.get(sectionIdInteger);
             }
             else
             {
                 section = new Section();
                 sections.put(sectionIdInteger, section);
             }

             newSeg.setSection(section);

             cell.getAllSegments().add(newSeg);

             this.segments.put(new Integer(id), newSeg);

             currentSegment = newSeg;


             String parent = attributes.getValue(MorphMLConstants.SEGMENT_PARENT_ATTR);
             if (parent!=null)
             {
                 try
                 {
                     Segment parentSeg = segments.get(new Integer(parent));
                     parentSeg.getSegmentName(); // Just to check for null pointer...
                     newSeg.setParentSegment(parentSeg);
                 }
                 catch(Exception e)
                 {
                     throw new SAXException("Problem locating parent of "+name);
                 }
             }

         }

         else if (getCurrentElement().equals(MorphMLConstants.SEGMENT_PROXIMAL_ELEMENT))
         {
             logger.logComment("Proximal element found...");

             String xVal = attributes.getValue(MorphMLConstants.POINT_X_ATTR);
             String yVal = attributes.getValue(MorphMLConstants.POINT_Y_ATTR);
             String zVal = attributes.getValue(MorphMLConstants.POINT_Z_ATTR);
             String diamVal = attributes.getValue(MorphMLConstants.POINT_DIAM_ATTR);


             this.currentSegment.getSection().setStartPointPositionX(Float.parseFloat(xVal));
             this.currentSegment.getSection().setStartPointPositionY(Float.parseFloat(yVal));
             this.currentSegment.getSection().setStartPointPositionZ(Float.parseFloat(zVal));
             this.currentSegment.getSection().setStartRadius(Float.parseFloat(diamVal)/2f);
         }


         else if (getCurrentElement().equals(MorphMLConstants.SEGMENT_DISTAL_ELEMENT))
         {
             String xVal = attributes.getValue(MorphMLConstants.POINT_X_ATTR);
             String yVal = attributes.getValue(MorphMLConstants.POINT_Y_ATTR);
             String zVal = attributes.getValue(MorphMLConstants.POINT_Z_ATTR);
             String diamVal = attributes.getValue(MorphMLConstants.POINT_DIAM_ATTR);


             this.currentSegment.setEndPointPositionX(Float.parseFloat(xVal));
             this.currentSegment.setEndPointPositionY(Float.parseFloat(yVal));
             this.currentSegment.setEndPointPositionZ(Float.parseFloat(zVal));
             this.currentSegment.setRadius(Float.parseFloat(diamVal)/2f);
         }



         else if (getCurrentElement().equals(MorphMLConstants.CABLE_ELEMENT)
                  &&getAncestorElement(1).equals(MorphMLConstants.CABLES_ELEMENT))
         {

             logger.logComment("\n                           +++++    New Cable/Section!!  +++++");
             String id = attributes.getValue(MorphMLConstants.CABLE_ID_ATTR);
             String name = attributes.getValue(MorphMLConstants.CABLE_NAME_ATTR);

             logger.logComment("     Have found a cable id: " + id);

             Section section = null;

             Integer sectionIdInteger = new Integer(id);

             section = sections.get(sectionIdInteger);

             if (section==null)
             {
                 throw new SAXException("Problem parsing MorphML file. Section/cable "+name
                                        +" does not refer to one referenced in the list of segments");
             }
             section.setSectionName(name);

             currentSection = section;

             String fractAttr = attributes.getValue(MorphMLConstants.FRACT_ALONG_PARENT_ATTR);
             
             if (fractAttr==null)
                 fractAttr = attributes.getValue(MorphMLConstants.FRACT_ALONG_PARENT_ATTR_pre_v1_7_1);
             
             if(fractAttr!=null)
             {
                 float fractAlongSec = Float.parseFloat(fractAttr);
                 Segment firstSegInSec = cell.getAllSegmentsInSection(currentSection).getFirst();
                 logger.logComment("fract along sec: " + fractAlongSec);
                 
                 if (firstSegInSec.getParentSegment() !=null)
                 {
                     Section parentSec = firstSegInSec.getParentSegment().getSection();
    
                     SegmentLocation loc = CellTopologyHelper.getFractionAlongSegment(cell, parentSec, fractAlongSec);
    
                     firstSegInSec.setFractionAlongParent(loc.getFractAlong());
                 }
                 else
                 {
                     logger.logComment("Ignoring fract along sec; no parent on the first segment of this section");
                 }
             }

         }
         else if (getCurrentElement().equals(MorphMLConstants.CABLE_GROUP_ELEMENT)
                  && getAncestorElement(1).equals(MorphMLConstants.CABLES_ELEMENT))
         {
             String name = attributes.getValue(MorphMLConstants.CABLE_NAME_ATTR);

             logger.logComment("     Have found a cable/section group: " + name);
             currentSectionGroup = name;


         }
         else if (getCurrentElement().equals(MorphMLConstants.CABLE_GROUP_ENTRY_ELEMENT)
                  && getAncestorElement(1).equals(MorphMLConstants.CABLE_GROUP_ELEMENT))
         {
             String id = attributes.getValue(MorphMLConstants.CABLE_ID_ATTR);

             logger.logComment("     Have found a cable id inside a cable group: " + id);
             logger.logComment("sections: " + sections);
             Section sec = sections.get(Integer.parseInt(id));
             sec.addToGroup(currentSectionGroup);
             if (sec.getComment()!=null && sec.getComment().indexOf(importationComment)>=0)
                 sec.setComment(null);
             logger.logComment("Section now: " + sec);
             
             
            // Ensure soma segs are finite vol unless something already specified for it
            if (currentSectionGroup.equals(MorphMLConstants.SOMA_CABLE_GROUP))
            {
                for (Segment seg: cell.getAllSegmentsInSection(sec))
                {
                    if (!this.finiteVolFound.contains(seg.getSegmentId()))
                        seg.setFiniteVolume(true);
                }

                this.foundSomaSection = true;
            }
             

         }



         else if (getCurrentElement().equals(MetadataConstants.GROUP_ELEMENT))
         {
             logger.logComment("Found new group, parent element: "+ this.getAncestorElement(1));

             //currentSection = section;
         }

         else if (getCurrentElement().equals(BiophysicsConstants.ROOT_ELEMENT))
         {
             this.unitsUsed = attributes.getValue(BiophysicsConstants.UNITS_ATTR);

             logger.logComment("Found new biophysics element with units: "+ unitsUsed);

         }

         else if (getCurrentElement().equals(BiophysicsConstants.MECHANISM_ELEMENT))
         {
             logger.logComment("Found new biophysics mechanism");

             this.currentMechName = attributes.getValue(BiophysicsConstants.MECHANISM_NAME_ATTR);
             this.currentMechType = attributes.getValue(BiophysicsConstants.MECHANISM_TYPE_ATTR);

             logger.logComment("Found new mechanism: "+ currentMechName+", type: "+ currentMechType);

         }
         else if (getCurrentElement().equals(NetworkMLConstants.POT_SYN_LOC_ELEMENT_preV1_7_1))
         {
             logger.logComment("Found pre v1.7.1 pot syn loc element...");

         }
         else if (getCurrentElement().equals(NetworkMLConstants.POT_SYN_LOC_ELEMENT))
         {
             logger.logComment("Found v2.0 compliant pot syn loc element...");

             currentSynType = attributes.getValue(NetworkMLConstants.SYN_TYPE_ATTR);

            logger.logComment("Found a syn type el: " + currentSynType);
             

         }
         else if (getCurrentElement().equals(NetworkMLConstants.SYN_TYPE_ELEMENT))
         {
             logger.logComment("Found syn type element...");

         }
         else if (getCurrentElement().equals(BiophysicsConstants.SPECIFIC_CAP_ELEMENT) ||
                    getCurrentElement().equals(BiophysicsConstants.SPECIFIC_CAP_ELEMENT_pre_v1_7_1))
         {
             logger.logComment("Found spec cap element...");
             cell.getSpecCapVsGroups().clear();   // to remove default global cm

         }
         else if (getCurrentElement().equals(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT) ||
             getCurrentElement().equals(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1))
         {
             logger.logComment("Found spec ax res element...");
             cell.getSpecAxResVsGroups().clear();   // to remove default global ra

         }
         else if (getCurrentElement().equals(BiophysicsConstants.INITIAL_POT_ELEMENT) ||
             getCurrentElement().equals(BiophysicsConstants.INITIAL_POT_ELEMENT_pre_v1_7_1))
         {
             logger.logComment("Found initial membrane potential element...");

         }
         else if (getCurrentElement().equals(BiophysicsConstants.PARAMETER_ELEMENT))
         {
             logger.logComment("Found new biophysics parameter");

             String paramName = attributes.getValue(BiophysicsConstants.PARAMETER_NAME_ATTR);
             String paramVal = attributes.getValue(BiophysicsConstants.PARAMETER_VALUE_ATTR);


             logger.logComment("Found new parameter: "+ paramName+", paramVal: "+ paramVal);

             if (getAncestorElement(1).equals(BiophysicsConstants.MECHANISM_ELEMENT) &&
                 currentMechType != null && paramName != null &&
                 currentMechType.equals(BiophysicsConstants.MECHANISM_TYPE_CHAN_MECH))
             {
                 float nmlUnits = Float.parseFloat(paramVal);
                 float neuroConUnits = -1;

                 if (paramName.equals(BiophysicsConstants.PARAMETER_GMAX))
                 {
                     if (this.unitsUsed.equals(BiophysicsConstants.UNITS_SI))
                     {
                         neuroConUnits
                             = (float) UnitConverter.getConductanceDensity(nmlUnits,
                                                                           UnitConverter.GENESIS_SI_UNITS,
                                                                           UnitConverter.NEUROCONSTRUCT_UNITS);

                     }
                     if (this.unitsUsed.equals(BiophysicsConstants.UNITS_PHYSIOLOGICAL))
                     {
                         neuroConUnits
                             = (float) UnitConverter.getConductanceDensity(nmlUnits,
                                                                           UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                           UnitConverter.NEUROCONSTRUCT_UNITS);
                     }
                 }
                 else
                 {
                     neuroConUnits = nmlUnits;
                 }

                 this.currentParamName = paramName;
                 this.currentParamValueNconUnits = neuroConUnits;
                 
                 //this.currentChanMech = new ChannelMechanism(currentMechName, neuroConUnitsCondDens);
             }

             if (getAncestorElement(1).equals(BiophysicsConstants.SPECIFIC_CAP_ELEMENT) ||
                 getAncestorElement(1).equals(BiophysicsConstants.SPECIFIC_CAP_ELEMENT_pre_v1_7_1))
             {
                 logger.logComment("Found param for spec cap");
                 double nmlUnitsSpecCap = Float.parseFloat(paramVal);
                 float neuroConUnitsSpecCap = -1;

                 if (this.unitsUsed.equals(BiophysicsConstants.UNITS_SI))
                 {
                     neuroConUnitsSpecCap
                         = (float) UnitConverter.getSpecificCapacitance(nmlUnitsSpecCap,
                                                                       UnitConverter.GENESIS_SI_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                 }
                 if (this.unitsUsed.equals(BiophysicsConstants.UNITS_PHYSIOLOGICAL))
                 {
                     neuroConUnitsSpecCap
                         = (float) UnitConverter.getSpecificCapacitance(nmlUnitsSpecCap,
                                                                       UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                 }
                 //cell.setSpecCapacitance(new NumberGenerator(neuroConUnitsSpecCap));
                 this.currentSpecCap = neuroConUnitsSpecCap;
             }

             if (getAncestorElement(1).equals(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT) ||
                 getAncestorElement(1).equals(BiophysicsConstants.SPECIFIC_AX_RES_ELEMENT_pre_v1_7_1))
             {
                 logger.logComment("Found param for spec ax res");
                 double nmlUnitsSpecAxRes = Float.parseFloat(paramVal);
                 float neuroConUnitsSpecAxRes = -1;

                 if (this.unitsUsed.equals(BiophysicsConstants.UNITS_SI))
                 {
                     neuroConUnitsSpecAxRes
                         = (float) UnitConverter.getSpecificAxialResistance(nmlUnitsSpecAxRes,
                                                                       UnitConverter.GENESIS_SI_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                 }
                 if (this.unitsUsed.equals(BiophysicsConstants.UNITS_PHYSIOLOGICAL))
                 {
                     neuroConUnitsSpecAxRes
                         = (float) UnitConverter.getSpecificAxialResistance(nmlUnitsSpecAxRes,
                                                                       UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                 }
                 ///cell.setSpecAxRes(new NumberGenerator(neuroConUnitsSpecAxRes));
                 this.currentSpecAxRes = neuroConUnitsSpecAxRes;
             }


             if (getAncestorElement(1).equals(BiophysicsConstants.INITIAL_POT_ELEMENT) ||
                 getAncestorElement(1).equals(BiophysicsConstants.INITIAL_POT_ELEMENT_pre_v1_7_1))
             {
                 logger.logComment("Found param for init memb pot");
                 double nmlUnitsInitPot = Float.parseFloat(paramVal);
                 float neuroConUnitsInitPot = -1;

                 if (this.unitsUsed.equals(BiophysicsConstants.UNITS_SI))
                 {
                     neuroConUnitsInitPot
                         = (float) UnitConverter.getVoltage(nmlUnitsInitPot,
                                                                       UnitConverter.GENESIS_SI_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                 }
                 if (this.unitsUsed.equals(BiophysicsConstants.UNITS_PHYSIOLOGICAL))
                 {
                     neuroConUnitsInitPot
                         = (float) UnitConverter.getVoltage(nmlUnitsInitPot,
                                                                       UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS,
                                                                       UnitConverter.NEUROCONSTRUCT_UNITS);
                 }
                 cell.setInitialPotential(new NumberGenerator(neuroConUnitsInitPot));
             }

         }


         else
         {
             logger.logComment("Warning, unhandled element: "+ getCurrentElement());
         }


    }


    @Override
    public void endElement(String namespaceURI, String localName, String qName)
    {
        //if (currentElement.)
       // currentElement = currentElement.getParent();
        logger.logComment("-----   End element: " + localName);

        if (getAncestorElement(1).equals(MorphMLConstants.CELL_ELEMENT) &&
                getCurrentElement().equals(MetadataConstants.NOTES_ELEMENT))
        {
                cell.setCellDescription(notesContents.toString());
                notesContents = new StringBuffer();
        }
        else if (getCurrentElement().equals(MorphMLConstants.CABLE_ELEMENT)
            && getAncestorElement(1).equals(MorphMLConstants.CABLES_ELEMENT))
        {
            Vector<String> grps = currentSection.getGroups();

            logger.logComment("foundSomaSection: "+foundSomaSection+", currentSection: "+currentSection+", grps: " + grps);

            // checking it's in one of the standard groups...

            if (!(grps.contains(Section.SOMA_GROUP) ||
                  grps.contains(Section.DENDRITIC_GROUP) ||
                  grps.contains(Section.AXONAL_GROUP)))
            {
                if (!foundSomaSection && 
                    (currentSection.getSectionName().toLowerCase().indexOf("soma") >= 0))
                {
                    logger.logComment("Making soma section: " + currentSection);
                    foundSomaSection = true;
                    currentSection.addToGroup(Section.SOMA_GROUP);
                    currentSection.setComment(importationComment +" Determined to be a soma section (group SOMA_GROUP not in original file)");
                }
                else
                {
                    logger.logComment("Making dend section: " + currentSection);
                    currentSection.addToGroup(Section.DENDRITIC_GROUP);
                    currentSection.setComment(importationComment +" Determined to be a dendrite section (group DENDRITE_GROUP not in original file)");
                }
            }
        }

        else if (getCurrentElement().equals(MorphMLConstants.CABLE_GROUP_ELEMENT)
                 && getAncestorElement(1).equals(MorphMLConstants.CABLES_ELEMENT))
        {
            currentSectionGroup = null;
            logger.logComment("currentSectionGroup: " + currentSectionGroup);


        }

        else if (getCurrentElement().equals(MorphMLConstants.SEGMENT_ELEMENT))
        {
            logger.logComment("<<<<      End of segment: " + this.currentSegment);

        }

        else if (getCurrentElement().equals(MorphMLConstants.CABLES_ELEMENT))
        {
            logger.logComment("<<<<      End of sections");
            if (foundSomaSection==false)
            {
                logger.logComment("Not yet found soma!");
                
                Vector<Segment> segs = cell.getAllSegments();
                for(Segment seg: segs)
                {
                    if (!foundSomaSection)
                    {
                        if (seg.getParentSegment()==null)
                        {
                            logger.logComment("Making soma section: " + seg.getSection());
                            foundSomaSection = true;
                            seg.getSection().addToGroup(Section.SOMA_GROUP);
                        }
                    }
                }
            }


        }


        stepDownElement();
    }



    public static void main(String args[])
    {

        try
        {

/*
            File mmlFile = new File("../temp/e040426A.morph.xml");
            File jxmlFile = new File("../temp/e040426A.java.xml");

            //File mmlFile = new File("../temp/SampleCell.morph.xml");
            //File jxmlFile = new File("../temp/SampleCell.java.xml");


            File mmlSaveFile = new File("../temp/pp.morph.xml");
            File jxmlSaveFile = new File("../temp/pp.java.xml");



            GeneralUtils.timeCheck("Loading mml file...");

            MorphMLConverter mmlc = new MorphMLConverter();

            Cell mmlCell = mmlc.loadFromMorphologyFile(mmlFile, "MorphMLCell");

            GeneralUtils.timeCheck("Finished loading mml file...");


            System.out.println("Details: "+ CellTopologyHelper.printShortDetails(mmlCell));


            GeneralUtils.timeCheck("Finished validating cell...");


            mmlc.saveCellInMorphMLFormat(mmlCell, mmlSaveFile, NeuroMLConstants.NEUROML_LEVEL_2);



            GeneralUtils.timeCheck("Finished saving cell...");


            GeneralUtils.timeCheck("Loading java XML file...");


            Cell jxmlCell = mmlc.loadFromJavaXMLFile(jxmlFile);

            GeneralUtils.timeCheck("Finished loading Java XML file...");


            System.out.println("Details: "+ CellTopologyHelper.printShortDetails(jxmlCell));


            GeneralUtils.timeCheck("Finished validating cell...");



            mmlc.saveCellInJavaXMLFormat(jxmlCell, jxmlSaveFile);



            GeneralUtils.timeCheck("Finished saving cell...");

*/



            File f = new File("..\\copynCmodels\\TraubEtAl2005\\generatedNEURON\\ttt.xml");
           // File f = new File("/bernal/a4d.xml");

            logger.logComment("Loading mml cell from "+ f.getAbsolutePath());

            FileInputStream instream = null;
            InputSource is = null;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            MorphMLReader mmlBuilder = new MorphMLReader();
            xmlReader.setContentHandler(mmlBuilder);

            instream = new FileInputStream(f);

            is = new InputSource(instream);

            xmlReader.parse(is);

            Cell builtCell = mmlBuilder.getBuiltCell();

            logger.logComment("Cell which has been built: ");
            logger.logComment(CellTopologyHelper.printDetails(builtCell, null));
            
            logger.logComment(mmlBuilder.getWarnings());

            //logger.logComment("Segments: "+ builtCell.getAllSegments());

            //logger.logComment("First: "+ builtCell.getFirstSomaSegment().getRadius());



        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }

    }

}
