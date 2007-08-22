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

package ucl.physiol.neuroconstruct.genesis;

import java.util.*;

/**
 * Class specifying a location in the generated GENESIS script.
 *
 * @author Padraig Gleeson
 *  
 */

public class ScriptLocation
{
    public final static String SIM_CONFIG_INCLUDE_START = "#SIM_CONFIG_INCLUDE";
    public final static String SIM_CONFIG_INCLUDE_END = "#END_SIM_CONFIG_INCLUDE";

    public final static String SIM_CONFIG_EXCLUDE_START = "#SIM_CONFIG_EXCLUDE";
    public final static String SIM_CONFIG_EXCLUDE_END = "#END_SIM_CONFIG_EXCLUDE";

    private int positionReference = Integer.MIN_VALUE;
    private String shortDescription = null;
    private String usage = null;

    public static ArrayList<ScriptLocation> allLocations = new ArrayList<ScriptLocation>();


    public static ScriptLocation BEFORE_CELL_CREATION = new ScriptLocation(-1, "Before Cells created", "The GENESIS code will be called before any of the generated cells are created");
    public static ScriptLocation BEFORE_FINAL_RESET = new ScriptLocation(5, "Before Final reset", "Called just before final reset, which is intended to reinitialise all channels, etc.");
    public static ScriptLocation AFTER_FINAL_RESET = new ScriptLocation(10, "After Final reset", "Called just after final reset, when initial conditions of channels etc. can be altered");
    public static ScriptLocation AFTER_SIMULATION = new ScriptLocation(15, "After simulation run", "Code executed after simulation is completed");

    static
    {
        allLocations.add(BEFORE_CELL_CREATION);
        allLocations.add(BEFORE_FINAL_RESET);
        allLocations.add(AFTER_FINAL_RESET);
        allLocations.add(AFTER_SIMULATION);
    }

    public ScriptLocation()
    {
    }

    private ScriptLocation(int positionReference, String shortDescription, String usage)
    {
        this.positionReference = positionReference;
        this.shortDescription = shortDescription;
        this.usage = usage;
    }


    public boolean equals(Object obj)
    {
        //System.out.println("Checking equality of this: "+ this + " to "+ obj);
        if (obj instanceof ScriptLocation)
        {
            ScriptLocation ncl = (ScriptLocation)obj;

            if (ncl.shortDescription.equals(shortDescription) &&
                ncl.usage.equals(usage) &&
                ncl.positionReference == positionReference)
            {
                return true;
            }
        }
        return false;
    }


    public String getShortDescription()
    {
        return shortDescription;
    }

    public int getPositionReference()
    {
        return positionReference;
    }

    public void setShortDescription(String desc)
    {
        shortDescription = desc;
    }

    public void setPositionReference(int pr)
    {
        positionReference = pr;
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
        if (block == null) return "";

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




    public String toString()
    {
        return "Type "+ positionReference+", "+ this.shortDescription;
    }

    public static ArrayList<ScriptLocation> getAllKnownLocations()
    {
        return allLocations;
    }

    public static void main(String[] args)
    {
        System.out.println("Locations: ");
        ArrayList<ScriptLocation> locs = getAllKnownLocations();
        for (int i = 0; i < locs.size(); i++)
        {
            System.out.println(locs.get(i));
        }

        ScriptLocation ncl = new ScriptLocation(-1,
                                                "Before Cells created",
                                                "Code placed before any of the generated cells are created in hoc");

        System.out.println("Equal? "+ ncl.equals(locs.get(0)));



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

        String newSt = parseForSimConfigSpecifics(dodgy, "Default");


        System.out.println("newSt: \n\n" + newSt);

    }




}
