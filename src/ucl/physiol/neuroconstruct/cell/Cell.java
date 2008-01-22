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

package ucl.physiol.neuroconstruct.cell;

import java.util.*;
import java.io.*;
import javax.vecmath.*;

import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.simulation.*;
import ucl.physiol.neuroconstruct.utils.*;

 /**
  * The base class for all cells, defining the segments, section etc. Also contains
  * extra information describing the cell, including biophysical information.
  *
  * @author Padraig Gleeson
  *  
  *
  */


public class Cell implements Serializable
{
    private static final long serialVersionUID = -1542517048619766744L;

    private static transient ClassLogger logger = new ClassLogger("Cell");

    /**
     * A short description of the cell type...
     */
    private String cellDescription = "";

    /**
     *  reference to the particular instance
     */
    private String instanceName = null;

    /**
     * A vector containing the segments for the soma/dends/axons
     */
    private Vector<Segment> allSegments = new Vector<Segment>();

    /**
     * For the recording of Synapse potential locations
     */
    private Hashtable<String, Vector<String>> synapsesVsGroups = new Hashtable<String, Vector<String>>();

    /**
     * For the recording of ChannelMechanisms types
     */
    private Hashtable<ChannelMechanism, Vector<String>> chanMechsVsGroups = new Hashtable<ChannelMechanism, Vector<String>>();


    /**
     * For the recording of AP propagation delays. Note: unlike the chanMechsVsGroups and synapsesVsGroups
     * there can only be one ap prop speed assiciated with each group. The ApPropSpeed vs Array of groups storage
     * method is still used so it will work easily in the EditGroupChanMechAssociations dialog
     */
    private Hashtable<ApPropSpeed, Vector<String>> apPropSpeedsVsGroups = new Hashtable<ApPropSpeed, Vector<String>>();

    /**
     * Fos the second connection method, specify the region in space, relative to the coordinate
     * system of the cell, where axonal connections can be made
     */
    private Vector<AxonalConnRegion> axonalArbours = new Vector<AxonalConnRegion>();

    private float defaultInitPot = -65; // mV

    /**
     * Value for the initial membrane potential for the cell
     */
    private NumberGenerator initialPotential = new NumberGenerator(defaultInitPot); 



    /**
     * For the specific axial resistance of the cell
     */
    private Hashtable<Float, Vector<String>> specAxResVsGroups = new Hashtable<Float, Vector<String>>();

    /**
     * Needed to deal with previous handling of spec ax res, when there was just a global value for the cell
     */
    private NumberGenerator tempGlobalSpecAxRes = new NumberGenerator(300);;


    /**
     * For the specific capacitance of the cell
     */
    private Hashtable<Float, Vector<String>> specCapVsGroups = new Hashtable<Float, Vector<String>>();

    /**
     * Needed to deal with previous handling of spec cap, when there was just a global value for the cell
     */
    private NumberGenerator tempGlobalSpecCapacitance = new NumberGenerator(1e-8f);

    
    static {

        /** @todo Double check instantiation of logger when using serialised form of Java... */
        if (logger==null) logger = new ClassLogger("Cell");
    }

    /**
     * Default constructor is needed for XMLEncoder.
     */
    public Cell()
    {
        
    }


    /**
     * Note: used by XMLEncoder 
     */
    public Vector<Segment> getAllSegments()
    {
        return allSegments;
    }


    /**
     * Note: used by XMLEncoder 
     */
    public Vector<AxonalConnRegion> getAxonalArbours()
    {
        return this.axonalArbours;
    }

    public void deleteAxonalArbour(String aaName)
    {
       for (AxonalConnRegion aa: axonalArbours)
       {
           if (aa.getName().equals(aaName))
           {
               axonalArbours.remove(aa);
           }
       }
    }


    public void setAxonalArbours(Vector<AxonalConnRegion> axonalArbours)
    {
        this.axonalArbours = axonalArbours;
    }


    public void addAxonalArbour(AxonalConnRegion axonalArbour)
    {
        this.axonalArbours.add(axonalArbour);
    }

    public void updateAxonalArbour(AxonalConnRegion axonalArbour)
    {
        for (int i = 0; i < axonalArbours.size(); i++)
        {
            if (axonalArbours.get(i).getName().equals(axonalArbour.getName()))
            {
                axonalArbours.setElementAt(axonalArbour, i);
                return;
            }
        }

        this.axonalArbours.add(axonalArbour);
    }



    /**
     * Returns the segments whose sections don't have an ApPropSpeed mechanism specified
     */
    public Vector<Segment> getExplicitlyModelledSegments()
    {
        if (this.apPropSpeedsVsGroups.size()==0)
        {
            return allSegments;
        }

        Vector<Segment> onlyExpModSegments = new Vector<Segment>();
        Vector<String> dodgySections = new Vector<String>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment next =(Segment)allSegments.get(i);

            if (!allSegments.contains(next.getSection().getSectionName()))
            {
                Section sec = next.getSection();
                ApPropSpeed appv =  this.getApPropSpeedForSection(sec);
                if (appv==null)
                {
                    onlyExpModSegments.add(next);
                }
                else
                {
                    dodgySections.add(next.getSection().getSectionName());
                }
            }
        }
        return onlyExpModSegments;
    }

    public Vector<Segment> getOnlySomaSegments()
    {
        Vector<Segment> onlySomaSegments = new Vector<Segment>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Vector groups = ((Segment)allSegments.elementAt(i)).getGroups();
             if (groups.contains(Section.SOMA_GROUP))
                 onlySomaSegments.add(allSegments.get(i));
        }
        return onlySomaSegments;
    }


    public Vector<Segment> getOnlyAxonalSegments()
    {
        Vector<Segment> onlyAxonalSegments = new Vector<Segment>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Vector groups = ((Segment)allSegments.elementAt(i)).getGroups();
             if (groups.contains(Section.AXONAL_GROUP))
                 onlyAxonalSegments.add(allSegments.elementAt(i));
        }
        return onlyAxonalSegments;
    }


    public Vector<Segment> getOnlyDendriticSegments()
    {
        Vector<Segment> onlyDendriticSegments = new Vector<Segment>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Vector groups = ((Segment)allSegments.elementAt(i)).getGroups();
             if (groups.contains(Section.DENDRITIC_GROUP))
                 onlyDendriticSegments.add(allSegments.elementAt(i));
        }
        return onlyDendriticSegments;
    }


    public Segment addDendriticSegment(float radius,
                                       String name,
                                       Point3f endPointPosn,
                                       Segment parent,
                                       float fractionAlongParentSegment,
                                       String sectionName,
                                       boolean inheritParentsRadius)
    {
        int nextId = getNextSegmentId();

        Section segmentSection = null;

        float realFractionAlongParent = 1;

        // Shouldn't be null really...

        if (parent == null)
        {
            segmentSection = new Section(sectionName);
            segmentSection.setStartRadius(radius);
            segmentSection.setStartPointPositionX(endPointPosn.x);
            segmentSection.setStartPointPositionY(endPointPosn.y);
            segmentSection.setStartPointPositionZ(endPointPosn.z);
            //isFirstSection = true;
        }
        else
        {
            // first segment in a new Section
            if (!parent.getSection().getSectionName().equals(sectionName))
            {
                segmentSection = new Section(sectionName);
                //isFirstSection = true;

                if (fractionAlongParentSegment == 1)
                {
                    realFractionAlongParent = 1;
                    segmentSection.setStartPointPositionX(parent.getEndPointPosition().x);
                    segmentSection.setStartPointPositionY(parent.getEndPointPosition().y);
                    segmentSection.setStartPointPositionZ(parent.getEndPointPosition().z);

                    segmentSection.setStartRadius(parent.getRadius());
                }
                else
                {
                    realFractionAlongParent = fractionAlongParentSegment;

                    Point3f parentStart = parent.getStartPointPosition();
                    Point3f parentEnd = parent.getEndPointPosition();
                    
                    Point3f newSectionStart
                        = new Point3f(parentStart.x + (fractionAlongParentSegment) * (parentEnd.x - parentStart.x),
                                      parentStart.y + (fractionAlongParentSegment) * (parentEnd.y - parentStart.y),
                                      parentStart.z + (fractionAlongParentSegment) * (parentEnd.z - parentStart.z));

                        segmentSection.setStartPointPositionX(newSectionStart.x);
                        segmentSection.setStartPointPositionY(newSectionStart.y);
                        segmentSection.setStartPointPositionZ(newSectionStart.z);

                    // use parent end point radius as opposed to a tapered radius between the start and end...
                    ////////////////segmentSection.setStartRadius(parent.getRadius());
                }
                
                
                if (parent.getSegmentShape() == Segment.SPHERICAL_SHAPE)
                {
                    segmentSection.setStartPointPositionX(parent.getEndPointPosition().x);
                    segmentSection.setStartPointPositionY(parent.getEndPointPosition().y);
                    segmentSection.setStartPointPositionZ(parent.getEndPointPosition().z);

                    ////////////segmentSection.setStartRadius(radius);
                    realFractionAlongParent = 0.5f; // for NEURON, so when sphere mapped to
                    // a cylinder, child segment added at middle
                }
                
                if (inheritParentsRadius)
                {
                    float startRad = parent.getSegmentStartRadius();
                    float endRad = parent.getRadius();
                    
                    segmentSection.setStartRadius(startRad 
                            + (realFractionAlongParent * (endRad-startRad)));
                }
                else
                {
                    segmentSection.setStartRadius(radius);
                }
            }
            else
            {
                segmentSection = parent.getSection();
                //isFirstSection = false;
                realFractionAlongParent = fractionAlongParentSegment;
            }


        }


        Segment newSegment
            = new Segment(name,
                          radius,
                          endPointPosn,
                          nextId,
                          parent,
                          realFractionAlongParent,
                          segmentSection);

        //newSegment.setFirstSectionSegment(isFirstSection);

        allSegments.add(newSegment);
        segmentSection.addToGroup(Section.DENDRITIC_GROUP);

        return newSegment;
    }


    public Segment addAxonalSegment(float radius,
                                 String name,
                                 Point3f endPointPosn,
                                 Segment parent,
                                 float fractionAlongParentSegment,
                                 String sectionName)
    {
        int nextId = getNextSegmentId();

        Section segmentSection = null;
        //boolean isFirstSection;
        float realFractionAlongParent = 1;

        // Shouldn't be null really...

        if (parent == null)
        {
            segmentSection = new Section(sectionName);
            segmentSection.setStartRadius(radius);
            segmentSection.setStartPointPositionX(endPointPosn.x);
            segmentSection.setStartPointPositionY(endPointPosn.y);
            segmentSection.setStartPointPositionZ(endPointPosn.z);
            //isFirstSection = true;
        }
        else
        {
            // first segment in a new Section
            if (!parent.getSection().getSectionName().equals(sectionName))
            {
                segmentSection = new Section(sectionName);
                //isFirstSection = true;

                if (fractionAlongParentSegment == 1)
                {
                    realFractionAlongParent = 1;
                    segmentSection.setStartPointPositionX(parent.getEndPointPosition().x);
                    segmentSection.setStartPointPositionY(parent.getEndPointPosition().y);
                    segmentSection.setStartPointPositionZ(parent.getEndPointPosition().z);
                    segmentSection.setStartRadius(parent.getRadius());
                }
                else
                {
                    realFractionAlongParent = fractionAlongParentSegment;
                    Point3f parentStart = parent.getStartPointPosition();
                    Point3f parentEnd = parent.getEndPointPosition();
                    Point3f newSectionStart
                        = new Point3f(parentStart.x + (fractionAlongParentSegment) * (parentEnd.x - parentStart.x),
                                      parentStart.y + (fractionAlongParentSegment) * (parentEnd.y - parentStart.y),
                                      parentStart.z + (fractionAlongParentSegment) * (parentEnd.z - parentStart.z));

                        segmentSection.setStartPointPositionX(newSectionStart.x);
                        segmentSection.setStartPointPositionY(newSectionStart.y);
                        segmentSection.setStartPointPositionZ(newSectionStart.z);

                    // use parent end point radius as opposed to a tapered radius between the start and end...
                    segmentSection.setStartRadius(parent.getRadius());

                }
                if (parent.getSegmentShape() == Segment.SPHERICAL_SHAPE)
                {
                    segmentSection.setStartPointPositionX(parent.getEndPointPosition().x);
                    segmentSection.setStartPointPositionY(parent.getEndPointPosition().y);
                    segmentSection.setStartPointPositionZ(parent.getEndPointPosition().z);

                    segmentSection.setStartRadius(radius);
                    realFractionAlongParent = 0.5f; // for NEURON, so when sphere mapped to
                    // a cylinder, child segment added at middle
                }
            }
            else
            {
                segmentSection = parent.getSection();
               // isFirstSection = false;
                realFractionAlongParent = fractionAlongParentSegment;
            }


        }

        Segment newSegment
            = new Segment(name,
                          radius,
                          endPointPosn,
                          nextId,
                          parent,
                          realFractionAlongParent,
                          segmentSection);

        //newSegment.setFirstSectionSegment(isFirstSection);


        allSegments.add(newSegment);
        segmentSection.addToGroup(Section.AXONAL_GROUP);
        return newSegment;
    }

    /**
     * For adding the second or greater soma section. Use addFirstSomaSegment for
     * the first segment
     */
    public Segment addSomaSegment(float radius,
                                 String name,
                                 Point3f endPointPosn,
                                 Segment parent,
                                 Section section)
    {
       if (allSegments.size()==0)
        {
            return  addFirstSomaSegment(radius,
                                radius,
                                name,
                                endPointPosn,
                                endPointPosn,
                                section);

        }
        if (parent==null || endPointPosn==null) return null;


        int nextId = getNextSegmentId();


         Segment newSegment = new Segment(name,
                                         radius,
                                         endPointPosn,
                                         nextId,
                                         parent,
                                         1,       // soma sections are always simply connected...
                                         section);


/*
        if (parent == null && getFirstSomaSegment() == null)
        {
            newSegment.setFirstSectionSegment(true);
        }
*/
        allSegments.add(newSegment);
        section.addToGroup(Section.SOMA_GROUP);
        newSegment.setFiniteVolume(true);

        return newSegment;
    }


    /**
     * For adding the root segment of the soma. If its spherical have startPointPosn,
     * endPointPosn equal
     */
    public Segment addFirstSomaSegment(float startRadius,
                                       float endRadius,
                                       String name,
                                       Point3f startPointPosn,
                                       Point3f endPointPosn,
                                       Section section)
    {

        logger.logComment("Adding first soma section");
        if (startPointPosn == null) startPointPosn = new Point3f();
        if (endPointPosn == null) endPointPosn = new Point3f();

        Segment rootSegment = new Segment(name,
                                          endRadius,
                                          endPointPosn,
                                          0,
                                          null,
                                          1,
                                          section);

        section.setStartPointPositionX(startPointPosn.x);
        section.setStartPointPositionY(startPointPosn.y);
        section.setStartPointPositionZ(startPointPosn.z);

        section.setStartRadius(startRadius);

        //rootSegment.setFirstSectionSegment(true);

        allSegments.add(rootSegment);
        section.addToGroup(Section.SOMA_GROUP);
        rootSegment.setFiniteVolume(true);
        return rootSegment;
    }


    /* 
     * This function starts from the last added segment and works backwards to find the first seg in 
     * that section. This should be the tip of the section. It then works back along via the parent seg
     * until it comes to a segment outside the section. Under all but very abnormal conditions, this will 
     * return the segments ordered from the 0 point of the section to the 1 point
     * 
     * TODO:  check if this is possible target for optimisation
     */
    public LinkedList<Segment> getAllSegmentsInSection(Section section)
    {
        LinkedList<Segment> onlySegmentsInSection = new LinkedList<Segment>();

        int segIndex = allSegments.size()-1;

        while (onlySegmentsInSection.size()==0)
        {
            Segment nextSeg = (Segment) allSegments.elementAt(segIndex);

            //logger.logComment("segIndex: "+segIndex+", nextSeg: " + nextSeg, true);

            if (nextSeg.getSection().getSectionName().equals(section.getSectionName()))
            {
                if (nextSeg.getSection().equals(section)) // unless there's a serious error...
                {
                    //logger.logComment("Found a seg "+nextSeg+" for : " + section.getSectionName());
                    onlySegmentsInSection.add(nextSeg);
                    Segment parent = nextSeg.getParentSegment();
                        //logger.logComment("Going to check: : " + parent);
                    while (parent!=null && parent.getSection().equals(section))
                    {
                        onlySegmentsInSection.addFirst(parent);
                        parent = parent.getParentSegment();
                        //logger.logComment("Going to check: : " + parent);
                    }
                }
                else
                {
                    logger.logError("Problem: "+nextSeg.getSection());
                    logger.logError("not same as: "+section);
                }

            }
            segIndex--;

        }

/*
        for (int i = 0; i < allSegments.size(); i++)
        {
            Section nextSection = ( (Segment) allSegments.elementAt(i)).getSection();

            // Note: ideally should use equals on the Section object, but this is quicker and
            // any errors will show up in validation...
            if (nextSection.getSectionName().equals(section.getSectionName()))
                onlySegmentsInSection.add((Segment)allSegments.elementAt(i));
        }
         */

        return onlySegmentsInSection;
    }


    public ArrayList<Section> getAllSections()
    {
        ArrayList<Section> sections = new ArrayList<Section>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment) allSegments.elementAt(i);
            Section nextSection = seg.getSection();

            if (!sections.contains(nextSection))
                sections.add(nextSection);
        }
        return sections;
    }


    /** @todo Check not replicated elsewhere... */
    public ArrayList<Section> getSectionsInGroup(String group)
    {
        ArrayList<Section> sections = new ArrayList<Section>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment) allSegments.elementAt(i);
            Section nextSection = seg.getSection();

            if (nextSection.getGroups().contains(group) && !sections.contains(nextSection))
                sections.add(nextSection);
        }
        return sections;
    }






    public Segment getFirstSomaSegment()
    {
        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment) allSegments.elementAt(i);

            // will normally be at index 0...

            if (seg.isSomaSegment() && seg.isFirstSectionSegment()) return seg;
        }
        return null;
    }


    public Segment getSegmentWithId(int id)
    {
        // do this quick check first. This will be the case if the ids are simply incremented
        // when new segments are added. This will fail if segments have been deleted.
        
        if (allSegments.size() > id &&
            allSegments.elementAt(id).getSegmentId()==id)
        {
            return allSegments.elementAt(id);
        }

        // now just cycle through all to try to find it
        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = allSegments.elementAt(i);

            if (seg.getSegmentId()==id) return seg;
        }
        return null;
    }

    /**
     * Normally used to get Segment where segName = getSegmentName(), but if allowSimFriendlyName
     * is true allows SimEnvHelper.getSimulatorFriendlyName(segName) = getSegmentName()
     */
    public Segment getSegmentWithName(String segName, boolean allowSimFriendlyName)
    {
        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment) allSegments.elementAt(i);

            if (allowSimFriendlyName)
            {
                if (SimEnvHelper.getSimulatorFriendlyName(seg.getSegmentName()).equals(segName)) return seg;
            }
            else
            {
                if (seg.getSegmentName().equals(segName)) return seg;
            }
        }
        return null;
    }


    /**
     * Gets a unique new segment id. Will normally be simply incremented (0,1,2...) but allows for
     * breaks in ids (e.g. due to deleted segments)
     */
    public int getNextSegmentId()
    {
        int nextId = allSegments.size();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment seg = (Segment) allSegments.elementAt(i);

            if (seg.getSegmentId()>=nextId) nextId = seg.getSegmentId()+1;
        }
        return nextId;
    }



    public String getCellDescription()
    {
        return this.cellDescription;
    }


    public String getInstanceName()
    {
        return this.instanceName;
    };


    public ArrayList<String> getAllAllowedSynapseTypes()
    {
        ArrayList<String> allTypes = new ArrayList<String>();
        Set<String> synapses = synapsesVsGroups.keySet();
        allTypes.addAll(synapses);

        return allTypes;
    }

    public ArrayList<ChannelMechanism> getAllChannelMechanisms(boolean removeRepeats)
    {
        ArrayList<ChannelMechanism> allChanMechs = new ArrayList<ChannelMechanism>();
        Iterator<ChannelMechanism> chanMechs = chanMechsVsGroups.keySet().iterator();

        while(chanMechs.hasNext())
        {
            ChannelMechanism next = chanMechs.next();

            if (!removeRepeats || !allChanMechs.contains(next))
                allChanMechs.add(next);
        }

        return allChanMechs;
    }

    public ArrayList<Float> getDefinedSpecAxResistances()
    {
        this.checkSpecAxRes();
        ArrayList<Float> allSpecAxRes = new ArrayList<Float>();
        Set<Float> specAxReses = this.specAxResVsGroups.keySet();
        allSpecAxRes.addAll(specAxReses);

        return allSpecAxRes;
    }

    public ArrayList<Float> getDefinedSpecCaps()
    {
        this.checkSpecCap();
        ArrayList<Float> allSpecCaps = new ArrayList<Float>();
        Set<Float> specCaps = this.specCapVsGroups.keySet();
        allSpecCaps.addAll(specCaps);

        return allSpecCaps;
    }



    public ArrayList<ApPropSpeed> getAllApPropSpeeds()
    {
        ArrayList<ApPropSpeed> allAPPVs = new ArrayList<ApPropSpeed>();
        Set<ApPropSpeed> appvs = this.apPropSpeedsVsGroups.keySet();
        allAPPVs.addAll(appvs);

        return allAPPVs;
    }



/*

    public Vector getAllCellProcesses()
    {
        Vector allCellProcesses = new Vector();
        Set chanMechs = chanMechsVsGroups.keySet();
        Set synapses = synapsesVsGroups.keySet();

        allCellProcesses.addAll(chanMechs);
        allCellProcesses.addAll(synapses);

        return allCellProcesses;
    }
*/


    public Vector<String> getAllGroupNames()
    {
        Vector<String> allGroups = new Vector<String>();

        Vector<String> groups = new Vector<String>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment segment = allSegments.get(i);
            groups = segment.getGroups();
            for (int j = 0; j < groups.size(); j++)
            {
                if (!allGroups.contains(groups.elementAt(j)))
                    allGroups.add(groups.elementAt(j));
            }
        }
        return allGroups;
    }




    public String toString()
    {
        String cellName = this.getClass().getName();
        int indexLastDot = cellName.lastIndexOf('.');
        return instanceName+ " (" + cellName.substring(indexLastDot+1) +")";
    }



    public Vector<String> getGroupsWithSynapse(String synapseType)
    {
        if (!synapsesVsGroups.containsKey(synapseType)) return new Vector<String>();
        
        return (Vector<String>)synapsesVsGroups.get(synapseType);
    }


    public Vector<String> getGroupsWithSpecAxRes(float specAxRes)
    {
        checkSpecAxRes();
        if (!this.specAxResVsGroups.containsKey(specAxRes)) return new Vector<String>();
        return (Vector<String>)specAxResVsGroups.get(specAxRes);
    }


    public Vector<String> getGroupsWithSpecCap(float specCap)
    {
        this.checkSpecCap();
        if (!this.specCapVsGroups.containsKey(specCap)) return new Vector<String>();
        return (Vector<String>)specCapVsGroups.get(specCap);
    }




    public Vector<String> getGroupsWithChanMech(ChannelMechanism chanMech)
    {
        // Note: chanMechsVsGroups.get(chanMech) isn't good enough if the chanMechs aren't exactly same obj

        Vector<String> theGroups = new Vector<String>();

        for (ChannelMechanism nextChanMech : chanMechsVsGroups.keySet())
        {
            if (nextChanMech.equals(chanMech))
                theGroups.addAll(chanMechsVsGroups.get(nextChanMech));
        }

        return theGroups;
    }




    public Vector getGroupsWithApPropSpeed(ApPropSpeed appv)
    {
        if (!this.apPropSpeedsVsGroups.containsKey(appv)) return new Vector();
        return (Vector)apPropSpeedsVsGroups.get(appv);
    }



    /**
     *
     */
    public ApPropSpeed getApPropSpeedForGroup(String group)
    {

        Enumeration allAppvs = this.apPropSpeedsVsGroups.keys();
        while (allAppvs.hasMoreElements())
        {
            ApPropSpeed nextAppv = (ApPropSpeed) allAppvs.nextElement();
            Vector groupsForThis = (Vector) apPropSpeedsVsGroups.get(nextAppv);

            if (groupsForThis.contains(group))
            {
                return nextAppv; // note there could in theory be more than ApPropSpeed associated with
                                 // the group, but other checks should disallow this.
            }

        }
        return null;
    }


    public ArrayList<ChannelMechanism> getChanMechsForGroup(String group)
    {
        ArrayList<ChannelMechanism> chanMechs = new ArrayList<ChannelMechanism>();

        Enumeration allChanMechs = chanMechsVsGroups.keys();
        while (allChanMechs.hasMoreElements())
        {
            ChannelMechanism nextChanMech = (ChannelMechanism) allChanMechs.nextElement();
            Vector groupsForThisChanMech = (Vector) chanMechsVsGroups.get(nextChanMech);
            if (groupsForThisChanMech.contains(group))
            {
                if (!chanMechs.contains(nextChanMech)) chanMechs.add(nextChanMech);
            }
        }
        return chanMechs;
    }

    public Vector<String> getSynapsesForGroup(String group)
    {
        Vector<String> syns = new Vector<String>();

        Enumeration allSyns = synapsesVsGroups.keys();
        while (allSyns.hasMoreElements())
        {
            String nextSyn = (String) allSyns.nextElement();
            Vector groupsForThisSyn = (Vector) synapsesVsGroups.get(nextSyn);
            if (groupsForThisSyn.contains(group))
            {
                if (!syns.contains(nextSyn)) syns.add(nextSyn);
            }
        }
        return syns;
    }

    public float getSpecCapForGroup(String group)
    {
        this.checkSpecCap();

        Enumeration<Float> allSpecCaps = this.specCapVsGroups.keys();

        while (allSpecCaps.hasMoreElements())
        {
            float nextSpecCap = allSpecCaps.nextElement();
            Vector groupsForThisCap = (Vector) specCapVsGroups.get(nextSpecCap);
            if (groupsForThisCap.contains(group))
            {
                return nextSpecCap;
            }
        }
        return Float.NaN;
    }

    /**
     * To compensate for previous use of global spec ax res
     */
    private void checkSpecAxRes()
    {
        //logger.logComment("checkSpecAxRes called");
        if (specAxResVsGroups.size()==0 && tempGlobalSpecAxRes !=null)
        {
            Vector<String> all = new Vector<String>();
            all.add(Section.ALL);
            specAxResVsGroups.put(this.tempGlobalSpecAxRes.getNominalNumber(), all);
            tempGlobalSpecAxRes = null;
        }
        //logger.logComment("specAxResVsGroups: " + specAxResVsGroups);
    }

    /**
     * To compensate for previous use of global spec cap
     */
    private void checkSpecCap()
    {
        //logger.logComment("checkSpecCap called");
        if (specCapVsGroups.size()==0 && tempGlobalSpecCapacitance != null)
        {
            logger.logComment("** Adding global spec cap: " + tempGlobalSpecCapacitance +" in cell: "+this.hashCode());
            Vector<String> all = new Vector<String>();
            all.add(Section.ALL);
            specCapVsGroups.put(this.tempGlobalSpecCapacitance.getNominalNumber(), all);
            tempGlobalSpecCapacitance = null;
        }
    }



    public float getSpecAxResForGroup(String group)
    {
        checkSpecAxRes();

        Enumeration<Float> allSpecAxRes = this.specAxResVsGroups.keys();

        while (allSpecAxRes.hasMoreElements())
        {
            float nextSpecAxRes = allSpecAxRes.nextElement();
            Vector groupsForThisAxRes = (Vector) specAxResVsGroups.get(nextSpecAxRes);
            if (groupsForThisAxRes.contains(group))
            {
                return nextSpecAxRes;
            }
        }
        return Float.NaN;
    }

    public float getSpecAxResForSection(Section section)
    {
        checkSpecAxRes();

        Vector<String> groupsHere = section.getGroups();

        for (String group : groupsHere)
        {
            float specAxRes = getSpecAxResForGroup(group);
            if (!Float.isNaN(specAxRes)) return specAxRes;
        }

        return Float.NaN;
    }


    public float getSpecCapForSection(Section section)
    {
        //logger.logComment("getSpecCapForSection: " + section.getSectionName());
        checkSpecCap();

        Vector<String> groupsHere = section.getGroups();

        for (String group : groupsHere)
        {
            float specCap = getSpecCapForGroup(group);
            if (!Float.isNaN(specCap)) return specCap;
        }

        return Float.NaN;
    }






    public ArrayList<ChannelMechanism> getChanMechsForSegment(Segment segment)
    {
        return getChanMechsForSection(segment.getSection());
    }

    public ArrayList<ChannelMechanism> getChanMechsForSection(Section section)
    {
        ArrayList<ChannelMechanism> chanMechs = new ArrayList<ChannelMechanism>();

        Vector groups = section.getGroups();
        for (int i = 0; i < groups.size(); i++)
        {
            String nextGroup = (String) groups.elementAt(i);
            chanMechs.addAll(getChanMechsForGroup(nextGroup));
        }
        return chanMechs;
    }



    public ApPropSpeed getApPropSpeedForSegment(Segment segment)
    {
        return getApPropSpeedForSection(segment.getSection());
    }


    public ApPropSpeed getApPropSpeedForSection(Section section)
    {
        if (apPropSpeedsVsGroups.size()==0) return null;

        Vector groups = section.getGroups();
        for (int i = 0; i < groups.size(); i++)
        {
            String nextGroup = (String) groups.elementAt(i);
            ApPropSpeed apps = this.getApPropSpeedForGroup(nextGroup);

            if (apps!=null) return apps; // Should only be one ApPropSpeed per section!!
        }
        return null;
    }




    public boolean associateGroupWithSynapse(String group, String synapseType)
    {
        logger.logComment(this.hashCode()+", being told to associate group: "
                          + group
                          + " with synapse type: "
                          + synapseType);


        if (!getAllGroupNames().contains(group)) return false;
        Vector<String> groups = null;

        if (!synapsesVsGroups.containsKey(synapseType))
        {
            groups = new Vector<String>();
        }
        else
        {
            groups = (Vector<String>)synapsesVsGroups.get(synapseType);
        }
        if (!groups.contains(group)) groups.add(group);
        synapsesVsGroups.put(synapseType, groups);

        logger.logComment("Synapses: "+ synapsesVsGroups);
        logger.logComment("Groups: "+ getAllGroupNames());


        return true;
    }


    public boolean associateGroupWithChanMech(String group, ChannelMechanism chanMech)
    {
        logger.logComment("Cell being told to associate group: "
                          + group
                          + " with channel mechanism: "
                          + chanMech);
        
        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }


        Enumeration<ChannelMechanism> chanMechs = chanMechsVsGroups.keys();
        while (chanMechs.hasMoreElements())
        {
            ChannelMechanism otherChanMech = chanMechs.nextElement();

            if (otherChanMech.getName().equals(chanMech.getName()))
            {
                logger.logComment("otherChanMech: " +otherChanMech+", chanMech: " +chanMech );
                Vector<String> groups = chanMechsVsGroups.get(otherChanMech);

                    logger.logComment("groups: " + groups);
                if (groups.contains(group))
                {
                    logger.logComment("group: " + group+" is there");
                    groups.remove(group);
                    if (groups.size() > 0)
                        chanMechsVsGroups.put(otherChanMech, groups);
                    else
                        chanMechsVsGroups.remove(otherChanMech);
                }
                    logger.logComment("group: " + group+" is NOT there");
            }
        }



        Vector<String> groups = null;

        if (!chanMechsVsGroups.containsKey(chanMech))
        {
            logger.logComment("Making new group" );
            groups = new Vector<String>();
        }
        else
        {
            logger.logComment("Not Making new group" );
            groups = (Vector<String>)chanMechsVsGroups.get(chanMech);
        }
        if (!groups.contains(group)) groups.add(group);


        chanMechsVsGroups.put(chanMech, groups);

        logger.logComment("");

        return true;
    }

    public boolean associateGroupWithSpecCap(String group, float specCap)
    {
        this.checkSpecCap();

        logger.logComment("Cell being told to associate group: "
                          + group + " with specCap: " + specCap);

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        Enumeration<Float> cms =  specCapVsGroups.keys();
        while (cms.hasMoreElements())
        {
            Float cm = cms.nextElement();
            Vector<String> groups = specCapVsGroups.get(cm);
            if (groups.contains(group))
            {
                if (groups.size()==1)
                {
                    specCapVsGroups.remove(cm);
                }
                else
                {
                    groups.remove(group);
                }
            }
        }





        Vector<String> groups = null;

        if (!this.specCapVsGroups.containsKey(specCap))
        {
            groups = new Vector<String>();
        }
        else
        {
            groups = (Vector<String>)specCapVsGroups.get(specCap);
        }
        if (!groups.contains(group)) groups.add(group);
        specCapVsGroups.put(specCap, groups);

        return true;
    }

    public boolean associateGroupWithSpecAxRes(String group, float specAxRes)
    {
        checkSpecAxRes();
        logger.logComment("Cell being told to associate group: "
                          + group
                          + " with specAxRes: "
                          + specAxRes);

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }

        Enumeration<Float> sars =  specAxResVsGroups.keys();
        while (sars.hasMoreElements())
        {
            Float sar = sars.nextElement();
            Vector<String> groups = specAxResVsGroups.get(sar);
            if (groups.contains(group))
            {
                if (groups.size()==1)
                {
                    specAxResVsGroups.remove(sar);
                }
                else
                {
                    groups.remove(group);
                }
            }
        }

        Vector<String> groups = null;

        if (!specAxResVsGroups.containsKey(specAxRes))
        {
            groups = new Vector<String>();
        }
        else
        {
            groups = (Vector<String>)specAxResVsGroups.get(specAxRes);
        }
        if (!groups.contains(group)) groups.add(group);
        specAxResVsGroups.put(specAxRes, groups);

        return true;
    }



    public boolean associateGroupWithApPropSpeed(String group, ApPropSpeed apPropSpeed)
    {
        logger.logComment("Cell being told to associate group: "
                          + group
                          + " with AP propagation speed: "
                          + apPropSpeed);

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        Vector<String> groups = null;

        if (!this.apPropSpeedsVsGroups.containsKey(apPropSpeed))
        {
            groups = new Vector<String>();
        }
        else
        {
            groups = (Vector<String>)apPropSpeedsVsGroups.get(apPropSpeed);
        }

        if (!groups.contains(group)) groups.add(group);

        apPropSpeedsVsGroups.put(apPropSpeed, groups);

        logger.logComment("");

        return true;
    }



    /**
     * Since there should be only one appv per group, disassociate group from all appvs
     */
    public boolean disassociateGroupFromApPropSpeeds(String group)
    {
        logger.logComment("Being told to disassociate group: "
                          + group
                          + " from all groups: "
                          + getAllGroupNames());

        logger.logComment("apPropSpeedsVsGroups: "+apPropSpeedsVsGroups);

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        boolean success = false;

        Enumeration allAppvs = this.apPropSpeedsVsGroups.keys();
        while (allAppvs.hasMoreElements())
        {
            ApPropSpeed nextAppv = (ApPropSpeed) allAppvs.nextElement();
            Vector groupsForThis = (Vector) apPropSpeedsVsGroups.get(nextAppv);
            if (groupsForThis.contains(group))
            {
                success = groupsForThis.remove(group);
                if (groupsForThis.size()==0) // as it should be...
                {
                    apPropSpeedsVsGroups.remove(nextAppv);
                }
            }
        }
         logger.logComment("apPropSpeedsVsGroups: "+apPropSpeedsVsGroups);



        return success;
    }


    public boolean disassociateGroupFromSynapse(String group, String synapseType)
    {
        logger.logComment("Being told to disassociate group: "
                          + group
                          + " from synapse type: "
                          + synapseType
                          + ". My groups: "
                          + getAllGroupNames());

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        Vector<String> groups = null;
        boolean success = false;

        if (synapsesVsGroups.containsKey(synapseType))
        {
            groups = synapsesVsGroups.get(synapseType);
            success = groups.remove(group);
            if (groups.size()>0) synapsesVsGroups.put(synapseType, groups);
            else synapsesVsGroups.remove(synapseType);
        }


        return success;
    }


    /**
     * There should be only one spec cap for any given group
     */
    public boolean disassociateGroupFromSpecCap(String group)
    {
        this.checkSpecCap();

        logger.logComment("Being told to disassociate group: "
                          + group + ". My groups: " + getAllGroupNames());

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        Vector<String> groups = null;
        boolean success = false;

        Enumeration<Float> specCaps = specCapVsGroups.keys();

       while (specCaps.hasMoreElements())
       {
           Float spCaps  = specCaps.nextElement();

            groups = specCapVsGroups.get(spCaps);
            success = groups.remove(group);
            if (groups.size()>0) specCapVsGroups.put(spCaps, groups);
            else specCapVsGroups.remove(spCaps);
        }
        return success;
    }


    /**
     * There should be only one spec ax res for any given group
     */
    public boolean disassociateGroupFromSpecAxRes(String group)
    {
        checkSpecAxRes();

        logger.logComment("Being told to disassociate group: "
                          + group + ". My groups: " + getAllGroupNames());

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        Vector<String> groups = null;
        boolean success = false;

        Enumeration<Float> specAxReses = specAxResVsGroups.keys();

        while (specAxReses.hasMoreElements())
        {
            Float spAxRes  = specAxReses.nextElement();
            groups = (Vector<String>)specAxResVsGroups.get(spAxRes);
            success = groups.remove(group);
            if (groups.size()>0) specAxResVsGroups.put(spAxRes, groups);
            else specAxResVsGroups.remove(spAxRes);
        }
        return success;
    }



    public boolean disassociateGroupFromChanMech(String group, ChannelMechanism chanMech)
    {
        logger.logComment("Being told to disassociate group: "
                          + group
                          + " from synapse type: "
                          + chanMech);

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        Vector<String> groups = null;
        boolean success = false;
        if (chanMechsVsGroups.containsKey(chanMech))
        {
            groups = (Vector<String>)chanMechsVsGroups.get(chanMech);
            success = groups.remove(group);
            if (groups.size()>0) chanMechsVsGroups.put(chanMech, groups);
            else chanMechsVsGroups.remove(chanMech);
        }

        return success;
    }



    public boolean disassociateGroupFromChanMech(String group, String chanMechName)
    {
        logger.logComment("Being told to disassociate group: "
                          + group
                          + " from synapse type: "
                          + chanMechName);

        Vector<String> grps = getAllGroupNames();
        if (!grps.contains(group))
        {
            logger.logError("The group: "+group+" is not present in the set of all groups: "+ grps);
            return false;
        }
        
        //Vector<String> groups = null;
        boolean success = false;

        Enumeration enumeration = chanMechsVsGroups.keys();
        while (enumeration.hasMoreElements())
        {
            ChannelMechanism nextMech = (ChannelMechanism)enumeration.nextElement();
            if (nextMech.getName().equals(chanMechName))
            {
                boolean thisSuccess = disassociateGroupFromChanMech(group, nextMech);
                success = success || thisSuccess;
            }
        }

        return success;
    }

    /**
     * Makes an exact copy of the Cell, with new Segments (as opposed to references)
     * to the old Segments, containing the same morphological data as this Cell
     */
    public Object clone()
    {
        logger.logComment(">>>>>>>>>>>>    Cloning cell: "+ getInstanceName());

        Cell clonedCell = new Cell();
        clonedCell.setInstanceName(this.getInstanceName());
        clonedCell.setCellDescription(this.getCellDescription());
        clonedCell.setInitialPotential((NumberGenerator)this.getInitialPotential().clone());
        //clonedCell.setSpecAxRes((NumberGenerator)this.getSpecAxRes().clone());
        ///clonedCell.setSpecCapacitance((NumberGenerator)this.getSpecCapacitance().clone());

        Vector<Segment> newSegments = new Vector<Segment>();
        Hashtable<Section, Section> newSectionsVsOldSections = new Hashtable<Section, Section>();
        Hashtable<Segment, Segment> newSegmentsVsOldSegments = new Hashtable<Segment, Segment>();

        for (int i = 0; i < allSegments.size(); i++)
        {
            Segment oldSegment = (Segment)allSegments.elementAt(i);
            Segment newSegment = (Segment)oldSegment.clone();

            logger.logComment("Old segment: "+ oldSegment);
            logger.logComment("Old segment section: "+ oldSegment.getSection());


            if (oldSegment.isFirstSectionSegment())
            {
                Section newSection = (Section) oldSegment.getSection().clone();
                newSegment.setSection(newSection);
                newSectionsVsOldSections.put(oldSegment.getSection(), newSection);
            }
            else
            {
                newSegment.setSection( (Section) newSectionsVsOldSections.get(oldSegment.getSection()));
            }


            if (oldSegment.getParentSegment()!=null)
            {
                Segment newParentSegment = (Segment)newSegmentsVsOldSegments.get(oldSegment.getParentSegment());
                newSegment.setParentSegment(newParentSegment);
            }
            newSegments.add(newSegment);
            newSegmentsVsOldSegments.put(oldSegment, newSegment);

            logger.logComment("Cloned segment: "+ newSegment);
            logger.logComment("Cloned segment section: "+ newSegment.getSection());
        }

        logger.logComment("Cloned sections: "+ newSegments.size());

        clonedCell.setAllSegments(newSegments);

        Vector<String> groups = getAllGroupNames();

        clonedCell.getSpecAxResVsGroups().clear(); // to remove default global ra
        clonedCell.getSpecCapVsGroups().clear();   // to remove default global cm



        for (String nextGroup: groups)
        {

            ArrayList<ChannelMechanism> allChanMechs = getChanMechsForGroup(nextGroup);

            for (int k = 0; k < allChanMechs.size(); k++)
            {
                clonedCell.associateGroupWithChanMech(nextGroup, allChanMechs.get(k));
            }

            Vector allSyns = getSynapsesForGroup(nextGroup);

            for (int k = 0; k < allSyns.size(); k++)
            {
                clonedCell.associateGroupWithSynapse(nextGroup,
                                                      (String) allSyns.elementAt(k));
             }

            float specCap = this.getSpecCapForGroup(nextGroup);
            if (!Float.isNaN(specCap)) clonedCell.associateGroupWithSpecCap(nextGroup, specCap);

            float specAxRes = this.getSpecAxResForGroup(nextGroup);
            if (!Float.isNaN(specAxRes)) clonedCell.associateGroupWithSpecAxRes(nextGroup, specAxRes);



            ApPropSpeed apps = this.getApPropSpeedForGroup(nextGroup);

            if (apps!=null)
            {
                clonedCell.associateGroupWithApPropSpeed(nextGroup,
                                                       apps);
            }
        }
        logger.logComment(">>>>>>>>>>>>    Finished cloning cell: "+ getInstanceName());



        Vector<AxonalConnRegion> copy = new Vector<AxonalConnRegion>(axonalArbours.size());

        for (int i = 0; i < this.axonalArbours.size(); i++)
        {
            copy.add((AxonalConnRegion)(axonalArbours.get(i).clone()));
        }
        clonedCell.setAxonalArbours(copy);

        return clonedCell;
    }


    public static void main(String[] args)
    {
        try
        {
            SimpleCell cell = new SimpleCell("Testcell");
            Segment a1 = cell.getSegmentWithId(5);
            System.out.println(a1);

            Segment a2 = cell.addAxonalSegment(1, "axseg2", new Point3f(-60,0,0), a1, 
                    1, a1.getSection().getSectionName());
            

            a2.setSegmentId(1000);
            
            
            cell.addAxonalSegment(1, "axseg3", new Point3f(-80,0,0), a2, 
                    1, a2.getSection().getSectionName());
            

            
            System.out.println(CellTopologyHelper.printDetails(cell, null));
            
            LinkedList<Segment> segs = cell.getAllSegmentsInSection(a1.getSection());
            
            System.out.println(segs);
            
            
            if (true) return; // chop off here
            
           ChannelMechanism chMech = new ChannelMechanism( "ggg", 22);

           cell.associateGroupWithChanMech("dendrite_group", chMech);

           cell.associateGroupWithSpecCap("dendrite_group", 1234);
           cell.associateGroupWithSpecCap("soma_group", 1235);
           cell.associateGroupWithSpecCap("axon_group", 1235);



           cell.disassociateGroupFromSpecCap("all");

           System.out.println("Loaded cell: " + cell);
           System.out.println("Groups: " + cell.getAllGroupNames());

           System.out.println("Chan mechs: " + cell.getAllChannelMechanisms(false));

           ChannelMechanism cm2 = new ChannelMechanism( "ggg", 22);
           ChannelMechanism cm3 = new ChannelMechanism( "ggg", 33);

           System.out.println("cm2.equals(chMech): " + chMech.equals(cm2));
           cell.associateGroupWithChanMech("soma_group", cm2);

           System.out.println("Chan mechs vs groups: " + cell.getChanMechsVsGroups());

           cell.associateGroupWithChanMech("soma_group", cm3);

           System.out.println("Chan mechs vs groups: " + cell.getChanMechsVsGroups());

           if (true) return;


           System.out.println("\n\ngetSpecCapVsGroups: " + cell.getSpecCapVsGroups());

           cell.associateGroupWithSpecCap("axon_group", 777);
           cell.associateGroupWithSpecCap("dendrite_group", 777);

           System.out.println("\n\ngetSpecCapVsGroups: " + cell.getSpecCapVsGroups());


           ApPropSpeed appv = new ApPropSpeed(123);


           cell.associateGroupWithApPropSpeed("axon_group", appv);

           boolean html = true;


          Cell cellB = (Cell)cell.clone();


          cellB.associateGroupWithChanMech("soma_group", chMech);


           System.out.println("Chan mechs B: " + cellB.getChanMechsVsGroups());

           System.out.println("getDefinedSpecCaps: " + cell.getDefinedSpecCaps());

          System.out.println("\n\n\nCompare: "+ CellTopologyHelper.compare(cell, cellB));





        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }


    // These functions are here so XML Encoder can store the data...

    public void setCellDescription(String cellDescription)
    {
        if (cellDescription==null)
            this.cellDescription = "";
        else
            this.cellDescription = cellDescription;
    }
    public void setAllSegments(Vector<Segment> allSegments)
    {
        this.allSegments = allSegments;
    }

    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;
    }

    public Hashtable<String, Vector<String>> getSynapsesVsGroups()
    {
        return synapsesVsGroups;
    }

    public void setSynapsesVsGroups(Hashtable<String, Vector<String>> synapsesVsGroups)
    {
        this.synapsesVsGroups = synapsesVsGroups;
    }

    public Hashtable<ChannelMechanism, Vector<String>> getChanMechsVsGroups()
    {
        return chanMechsVsGroups;
    }

    public Hashtable<Float, Vector<String>> getSpecCapVsGroups()
    {
        this.checkSpecCap();
        return this.specCapVsGroups;
    }
    public Hashtable<Float, Vector<String>> getSpecAxResVsGroups()
    {
        this.checkSpecAxRes();
        return this.specAxResVsGroups;
    }


    public Hashtable<ApPropSpeed, Vector<String>> getApPropSpeedsVsGroups()
    {
        return this.apPropSpeedsVsGroups;
    }


    public void setChanMechsVsGroups(Hashtable<ChannelMechanism, Vector<String>> chanMechsVsGroups)
    {
        this.chanMechsVsGroups = chanMechsVsGroups;
    }

    public void setSpecCapVsGroups(Hashtable<Float, Vector<String>> specCapVsGroups)
    {
        this.specCapVsGroups = specCapVsGroups;
    }


    public void setSpecAxResVsGroups(Hashtable<Float, Vector<String>> sp)
    {
        this.specAxResVsGroups = sp;
    }


    public void setApPropSpeedsVsGroups(Hashtable<ApPropSpeed, Vector<String>> ap)
    {
        this.apPropSpeedsVsGroups = ap;
    }


    /**
     * Needed to support projects saved when spec ax res was a global cell parameter
     * @deprecated
     */
    public NumberGenerator getOldGlobalSpecAxRes()
    {
        logger.logComment("getOldGlobalSpecAxRes called...");
        //tempGlobalSpecAxRes = new NumberGenerator(300);
        return this.tempGlobalSpecAxRes;
    }


    public NumberGenerator getInitialPotential()
    {
    	if (initialPotential == null)
    		initialPotential = new NumberGenerator(defaultInitPot); 
    	
        return initialPotential;
    }
    public void setInitialPotential(NumberGenerator initPot)
    {
        this.initialPotential = initPot;
    }

    /**
     * Needed to support projects saved when spec cap was a global cell parameter
     * @deprecated
     */
    public NumberGenerator getOldGlobalSpecCapacitance()
    {
        logger.logComment("getOldGlobalSpecCapacitance called...");

        //tempGlobalSpecCapacitance = new NumberGenerator(1e-8f);
        return tempGlobalSpecCapacitance;
    }


}
