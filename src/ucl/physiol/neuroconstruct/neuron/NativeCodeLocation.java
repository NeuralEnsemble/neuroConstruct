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

import java.util.*;

/**
 * Class specifying a location in the generated NEURON code. Based on the NEURON locations used for
 * FInitilizeHandler
 *
 * @author Padraig Gleeson
 *  
 */

public class NativeCodeLocation
{
    public final static String SIM_CONFIG_INCLUDE_START = "#SIM_CONFIG_INCLUDE";
    public final static String SIM_CONFIG_INCLUDE_END = "#END_SIM_CONFIG_INCLUDE";

    public final static String SIM_CONFIG_EXCLUDE_START = "#SIM_CONFIG_EXCLUDE";
    public final static String SIM_CONFIG_EXCLUDE_END = "#END_SIM_CONFIG_EXCLUDE";



    private float positionRef = Integer.MIN_VALUE;

    private String shortDescription = null;
    private String usage = null;


    public static ArrayList<NativeCodeLocation> allLocations = new ArrayList<NativeCodeLocation>();

    public static NativeCodeLocation BEFORE_CELL_CREATION = new NativeCodeLocation(-1, "Before Cells created", "The NEURON code will be called before any of the generated cells are created in hoc");
    public static NativeCodeLocation AFTER_NET_CREATION = new NativeCodeLocation(-0.5f, "After network created", "The NEURON code will be called after all cell groups, net connections, inputs and plots have been created");
    public static NativeCodeLocation BEFORE_INITIAL = new NativeCodeLocation(0, "Before Cell Process mechanism INITIAL blocks", "NEURON FInitializeHandler location. A/c to NEURON docs: \"Called before the mechanism INITIAL blocks\"");
    public static NativeCodeLocation AFTER_INITIAL = new NativeCodeLocation(1, "After Cell Process mechanism INITIAL blocks", "NEURON FInitializeHandler location. A/c to NEURON docs: \"Called after the mechanism INITIAL blocks. This is the best place to change state values.\"");
    public static NativeCodeLocation BEFORE_FINITIALIZE_RETURNS = new NativeCodeLocation(2, "Before return from finitialize()", "NEURON FInitializeHandler location. A/c to NEURON docs: \"Called just before return from finitialize. This is the best place to record values at t=0.\"");
    public static NativeCodeLocation START_FINITIALIZE = new NativeCodeLocation(3, "Beginning of finitialize()", "NEURON FInitializeHandler location. A/c to NEURON docs: \"Called called at the beginning of finitialize. At this point it is allowed to change the structure of the model.\"");
    public static NativeCodeLocation AFTER_SIMULATION = new NativeCodeLocation(10, "After simulation run", "Code executed after simulation is completed");

    static
    {
        allLocations.add(BEFORE_CELL_CREATION);
        allLocations.add(AFTER_NET_CREATION);
        allLocations.add(BEFORE_INITIAL);
        allLocations.add(AFTER_INITIAL);
        allLocations.add(BEFORE_FINITIALIZE_RETURNS);
        allLocations.add(START_FINITIALIZE);
        allLocations.add(AFTER_SIMULATION);
    }

    public NativeCodeLocation()
    {
    }

    private NativeCodeLocation(int positionReference, String shortDescription, String usage)
    {
        this.positionRef = positionReference;
        this.shortDescription = shortDescription;
        this.usage = usage;
    }

    private NativeCodeLocation(float positionReference, String shortDescription, String usage)
    {
        this.positionRef = positionReference;
        this.shortDescription = shortDescription;
        this.usage = usage;
    }


    @Override
    public boolean equals(Object obj)
    {
        //System.out.println("Checking equality of this: "+ this + " to "+ obj);
        if (obj instanceof NativeCodeLocation)
        {
            NativeCodeLocation ncl = (NativeCodeLocation)obj;

            if (ncl.shortDescription.equals(shortDescription) &&
                ncl.usage.equals(usage) &&
                ncl.positionRef == positionRef)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Float.floatToIntBits(this.positionRef);
        hash = 97 * hash + (this.shortDescription != null ? this.shortDescription.hashCode() : 0);
        hash = 97 * hash + (this.usage != null ? this.usage.hashCode() : 0);
        return hash;
    }




    public String getShortDescription()
    {
        return shortDescription;
    }

    public float getPositionReference()
    {
        return positionRef;
    }

    public void setShortDescription(String desc)
    {
        shortDescription = desc;
    }

    public void setPositionReference(float pr)
    {
        positionRef = pr;
    }

    public void setPositionReference(int pr)
    {
        positionRef = pr;
    }


    public String getUsage()
    {
        return usage;
    }

    public void setUsage(String usage)
    {
        this.usage = usage;
    }

    public static String parseForSimConfigSpecifics(String block, String simConfig)
    {
        if (block==null) return "";
        StringBuffer parsed = new StringBuffer();
        String[] lines = block.split("\n");

        ArrayList<String> whiteList = new ArrayList<String>();
        ArrayList<String> blackList = new ArrayList<String>();

        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].startsWith(SIM_CONFIG_INCLUDE_START))
            {
                whiteList.add(lines[i].substring(SIM_CONFIG_INCLUDE_START.length()).trim());
            }
            else if (lines[i].startsWith(SIM_CONFIG_EXCLUDE_START))
            {
                blackList.add(lines[i].substring(SIM_CONFIG_EXCLUDE_START.length()).trim());
            }
            else if (lines[i].startsWith(SIM_CONFIG_INCLUDE_END))
            {
                whiteList.remove(lines[i].substring(SIM_CONFIG_INCLUDE_END.length()).trim());
            }
            else if (lines[i].startsWith(SIM_CONFIG_EXCLUDE_END))
            {
                blackList.remove(lines[i].substring(SIM_CONFIG_EXCLUDE_END.length()).trim());
            }
            else
            {
                if ( (whiteList.size()==0 || whiteList.contains(simConfig)) &&
                    !blackList.contains(simConfig))
               {
                   parsed.append(lines[i] + "\n");
               }
            }
        }

        return parsed.toString();
    }

    //public



    @Override
    public String toString()
    {
        String pr = Math.floor(positionRef)==positionRef ? (int)positionRef+"" : positionRef+"";

        return "Type "+ pr+", "+ this.shortDescription /*+ (present? " *": "")*/;
    }

    public String toShortString()
    {
        String pr = Math.floor(positionRef)==positionRef ? (int)positionRef+"" : positionRef+"";
        return "Type "+ pr;
    }

    public static ArrayList<NativeCodeLocation> getAllKnownLocations()
    {
        return allLocations;
    }

    public static void main(String[] args)
    {
        System.out.println("Locations: ");
        ArrayList<NativeCodeLocation> locs = getAllKnownLocations();
        for (int i = 0; i < locs.size(); i++)
        {
            System.out.println(locs.get(i));
        }

        NativeCodeLocation ncl = new NativeCodeLocation(-1, "Before Cells created", "Code placed before any of the generated cells are created in hoc");

        System.out.println("Equal? " + ncl.equals(locs.get(0)));

        String dodgy = "hsfgdhjcgh \n" +
            " ghxgh fgsd dg \n" +
            SIM_CONFIG_INCLUDE_START + " Default \n" +
            "ininiiinin \n" +
            "iniinininiddddd \n" +
            SIM_CONFIG_INCLUDE_END + " Default \n" +
            "aaaaaaaaaa \n" +
            "ghsfghdfgh \n" +
            SIM_CONFIG_EXCLUDE_START + " Default \n" +
            "oooodoodod \n" +
            "ooooooo \n" +


            SIM_CONFIG_EXCLUDE_END + " Default \n" +
            "aaaaaaaaaa \n";

        System.out.println("dodgy: \n\n" + dodgy);

        String newSt = parseForSimConfigSpecifics(dodgy, "Defaultf");


        System.out.println("newSt: \n\n" + newSt);

    }




}
