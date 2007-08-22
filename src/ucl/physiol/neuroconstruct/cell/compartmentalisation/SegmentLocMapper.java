
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

package ucl.physiol.neuroconstruct.cell.compartmentalisation;

import java.util.*;
import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;


/**
 * Helper class for compartmentalisations. Used to map ranges within segments from one
 * cell to its recompartmentalised counterpart.
 *
 * @author Padraig Gleeson
 *  
 * @see     SegmentRange
 *
 */

public class SegmentLocMapper
{
    private static ClassLogger logger = new ClassLogger("SegmentLocMapper");

    private Hashtable<SegmentRange, SegmentRange[]> maps = new Hashtable<SegmentRange, SegmentRange[]>();
    private ArrayList<Integer> allSegIdsMappedFrom = new ArrayList<Integer>(); // caching...
    private ArrayList<Integer> allSegIdsMappedTo = new ArrayList<Integer>(); // caching...

    protected SegmentLocMapper()
    {
    }

    public void reset()
    {
        maps.clear();
        allSegIdsMappedFrom.clear();
    }

    public void addMapping(SegmentRange from, SegmentRange[] to)
    {
        maps.put(from, to);
        if (!allSegIdsMappedFrom.contains(from.getSegmentId())) allSegIdsMappedFrom.add(from.getSegmentId());
        for (int i = 0; i < to.length; i++)
        {
            if (!allSegIdsMappedTo.contains(to[i].getSegmentId())) allSegIdsMappedTo.add(to[i].getSegmentId());
        }
    }


    public ArrayList<Integer> getAllSegIdsMappedFrom()
    {
        return allSegIdsMappedFrom;
    }

    public ArrayList<Integer> getAllSegIdsMappedTo()
    {
        return allSegIdsMappedTo;
    }


    /**
     * In place of a full reverse mapping
     */
    public int getFromSegmentId(int toSegmentId)
    {
        Enumeration<SegmentRange> enumer = maps.keys();

        while (enumer.hasMoreElements())
        {
            SegmentRange from = enumer.nextElement();
            SegmentRange[] to = maps.get(from);

            for (int i = 0; i < to.length; i++)
            {
                logger.logComment("There's a mapping from seg: "+from.getSegmentId()+": to " + to[i].getSegmentId());
                if (to[i].getSegmentId()==toSegmentId) return from.getSegmentId();
            }

        }

        return -1;
    }



    public SegmentLocation mapSegmentLocation(SegmentLocation oldSegLoc)
    {
        float accuracy = 1e-5f;

        if (!allSegIdsMappedFrom.contains(oldSegLoc.getSegmentId())) return oldSegLoc;

        Enumeration<SegmentRange> enumer = maps.keys();

        while (enumer.hasMoreElements())
        {
            SegmentRange from = enumer.nextElement();

            if (from.getSegmentId() == oldSegLoc.getSegmentId() &&
                from.getStartFract()<=oldSegLoc.getFractAlong() &&
                from.getEndFract()>=oldSegLoc.getFractAlong())
            {
                SegmentRange[] to = maps.get(from);

                float totalToLength = 0;

                for (int i = 0; i < to.length; i++)
                {
                    logger.logComment("There's a mapping from seg: "+from.getSegmentId()+": to " + to[i].getSegmentId());
                    totalToLength+= to[i].getRangeLength();
                }

                logger.logComment("from     : " + from);
                logger.logComment("oldSegLoc: " + oldSegLoc);

                float lengthToTraverse =
                    totalToLength * (oldSegLoc.getFractAlong()-from.getStartFract())/(from.getEndFract()-from.getStartFract());

                logger.logComment("totalToLength: " + totalToLength+", lengthToTraverse: "+lengthToTraverse);

                if (totalToLength==lengthToTraverse)
                {
                    /** @todo Check if this is correct... */
                    logger.logComment("Quickly mapping to the final To SegmentRange: " + to[to.length-1]);
                    float fractLoc = to[to.length - 1].getEndFract();

                    if (fractLoc < accuracy) fractLoc = 0;
                    if (fractLoc > 1 - accuracy) fractLoc = 1;

                       // return new SegmentLocation(to[to.length-1].getSegmentId(), 1);
                       return new SegmentLocation(to[to.length-1].getSegmentId(), fractLoc);
                }

                for (int i = 0; i < to.length; i++)
                {
                    logger.logComment(i+": " + to[i]);
                    logger.logComment("to[i].getRangeLength(): " + to[i].getRangeLength()+", lengthToTraverse: "+lengthToTraverse);
                    if (to[i].getRangeLength()>=lengthToTraverse)
                    {
                        logger.logComment("Got the final range, lengthToTraverse: "+lengthToTraverse+"...");
                        float fract = ((to[i].getStartFract()*to[i].getTotalSegmentLength()) + lengthToTraverse)/to[i].getTotalSegmentLength() ;

                        logger.logComment("fract: " + fract);
                        if (fract < accuracy) fract = 0;
                        if (fract > 1 - accuracy) fract = 1;
                        logger.logComment("fract: " + fract);

                        return new SegmentLocation(to[i].getSegmentId(), fract);
                    }
                    lengthToTraverse-= to[i].getRangeLength();
                }

                logger.logComment("Not found yet, assuming end of final mapped seg...");
                SegmentRange finalSegRange = to[to.length - 1];
                logger.logComment("finalSegRange: " + finalSegRange);

                float fractLoc = finalSegRange.getEndFract();

                if (fractLoc<accuracy) fractLoc = 0;
                if (fractLoc>1-accuracy) fractLoc =1;

                return new SegmentLocation(finalSegRange.getSegmentId(), fractLoc);
            }

        }

        return oldSegLoc; // if not found...

    }

    public String toString()
    {
        StringBuffer info = new StringBuffer("SegmentLocMapper with "+this.maps.size()+" mappings\n");
        Enumeration<SegmentRange> enumer = maps.keys();

        while (enumer.hasMoreElements())
        {
            SegmentRange from = enumer.nextElement();
            info.append("    Mapping from "+ from +" to: "+"\n");

            SegmentRange[] to = maps.get(from);

            for (int i = 0; i < to.length; i++)
            {
                info.append("        "+to[i]+"\n");
            }
        }
        return info.toString();
    }




    public static void main(String[] args)
    {
        SegmentLocMapper ss = new SegmentLocMapper();
        ss.doIt();
    }

    private void doIt()
    {

        SegmentLocation sl = new SegmentLocation(1, .5f);

        SegmentLocMapper slmap = new SegmentLocMapper();

        slmap.addMapping(new SegmentRange(1, 10, 0,1f),
                         new SegmentRange[]{
            new SegmentRange(2, 5, 0f, 0.1f),new SegmentRange(3, 5, 0.1f, 0.4f)});

        System.out.println("Old: " + sl);
        System.out.println("New: " + slmap.mapSegmentLocation(sl));


        slmap = new SegmentLocMapper();

        slmap.addMapping(new SegmentRange(1, 10, 0.4f, 1f),
                         new SegmentRange[]{
            new SegmentRange(2, 5, 0f, 0.1f),new SegmentRange(3, 5, 0.1f, 0.4f)});

        System.out.println("Old: " + sl);
        System.out.println("New: " + slmap.mapSegmentLocation(sl));


        System.out.println("slmap: " + slmap);


    }

}

