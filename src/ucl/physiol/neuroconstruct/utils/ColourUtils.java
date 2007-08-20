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

package ucl.physiol.neuroconstruct.utils;

import java.util.*;
import java.awt.*;

/**
 * Simple colour utilities
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class ColourUtils
{
    public static ArrayList<ColourRecord> allColours = new ArrayList<ColourRecord> ();

    static
    {
        // These allow the same colour to be used for graphs in NEURON and GENESIS
        addColourRecord("Black", Color.black, 1);
        addColourRecord("Red", Color.red, 2);
        addColourRecord("Blue", Color.blue, 3);
        addColourRecord("Green", new Color(0, 155, 0), 4); // easier to see against the 3D default background
        addColourRecord("Orange", Color.orange, 5);
        addColourRecord("Brown", new Color(140, 100, 0), 6);
        addColourRecord("Magenta", Color.magenta, 7);
        addColourRecord("Yellow", Color.yellow, 8);
        addColourRecord("Grey", Color.gray, 9);

        // Java supported colours

        addColourRecord("DarkGray", Color.darkGray, -1);
        addColourRecord("DarkGrey", Color.darkGray, -1);
        addColourRecord("LightGray", Color.lightGray, -1);
        addColourRecord("LightGrey", Color.lightGray, -1);
        addColourRecord("Gray", Color.gray, -1);

        addColourRecord("Pink", Color.pink, -1);
        addColourRecord("White", Color.WHITE, -1);
        addColourRecord("Cyan", Color.cyan, -1);

        // some others I've found in neurolucida files...

        addColourRecord("DarkYellow", Color.yellow.darker(), -1);
        addColourRecord("DarkRed", Color.red.darker(), -1);
        addColourRecord("DarkGreen", Color.green.darker(), -1);
        addColourRecord("DarkMagenta", Color.magenta.darker(), -1);
        addColourRecord("BrightGrey", Color.gray.brighter(), -1);
        addColourRecord("SkyBlue", new Color(133, 233, 255), -1);
        addColourRecord("MoneyGreen", new Color(80,170,42), -1); // depends on the money really...

    }

    public ColourUtils()
    {



    }

    private static void addColourRecord(String name, Color color, int neuronColourIndex)
    {
        ColourRecord cr = new ColourRecord(name, color, neuronColourIndex);
        allColours.add(cr);
    }

    public static Color getColour(String colourName)
    {

        // e.g. as when colour is taken from line in neurolucida file ( (Color RGB (123, 34, 34)) is changed to RGB123_34_34
        if (colourName.indexOf("_")>=0)
        {

            if (colourName.startsWith("RGB")) colourName = colourName.substring(3);

            String[] redGreenBlue = colourName.split("_");

            Color c = new Color(Integer.parseInt(redGreenBlue[0]),
                Integer.parseInt(redGreenBlue[1]),
                Integer.parseInt(redGreenBlue[2]));

            return c;
        }

        for (int i = 0; i < allColours.size(); i++)
        {
            if (colourName.equalsIgnoreCase(allColours.get(i).name))
            {
                return allColours.get(i).colour;
            }
        }
        return null;
    }

    public static ArrayList<String> getAllColourNames()
    {
        ArrayList<String> names = new ArrayList<String>();

        for (int i = 0; i < allColours.size(); i++)
        {
            names.add(allColours.get(i).name);
        }
        return names;
    }

    public static String getColourName(int neuronColourIndex)
    {
        for (int i = 0; i < allColours.size(); i++)
        {
            if (allColours.get(i).neuronColourIndex == neuronColourIndex)
            {
                return allColours.get(i).name;
            };
        }
        return null;
    }




    public static class ColourRecord
    {
        public String name = null;
        public Color colour = null;
        public int neuronColourIndex = -1;

        private ColourRecord(){};

        private ColourRecord(String name, Color colour, int neuronColourIndex)
        {
            this.name = name;
            this.colour = colour;
            this.neuronColourIndex = neuronColourIndex;
        };

        public String toString()
        {
            return "ColourRecord ["+name+", "+colour.toString()+", "+neuronColourIndex+"]";
        }
    }

    public static void main(String[] args)
    {
        System.out.println("All names: "+ ColourUtils.getAllColourNames());

        for (int i = 0; i < allColours.size(); i++)
        {
            System.out.println(allColours.get(i));
        }

        String col = "RGB123_34_34";

        System.out.println("Colour: " + getColour(col));

    }

}
