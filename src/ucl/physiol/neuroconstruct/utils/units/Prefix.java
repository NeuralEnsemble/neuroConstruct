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

package ucl.physiol.neuroconstruct.utils.units;

/**
 * Class for handling units. These classes are based on the CellML (http://www.cellml.org)
 * scheme for dealing with units
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class Prefix
{
    private String name =null;
    private String symbol =null;
    private double multiplier = 1;

    public static Prefix YOTTA = new Prefix("yotta", "Y", 1E24);
    public static Prefix ZETTA = new Prefix("zetta", "Z", 1E21);
    public static Prefix EXA = new Prefix("exa", "E", 1E18);
    public static Prefix PETA = new Prefix("peta", "P", 1E15);
    public static Prefix TERA = new Prefix("tera", "T", 1E12);
    public static Prefix GIGA = new Prefix("giga", "G", 1E9);
    public static Prefix MEGA = new Prefix("mega", "M", 1E6);
    public static Prefix KILO = new Prefix("kilo", "k", 1E3);
    public static Prefix HECTO = new Prefix("hecto", "h", 1E2);
    public static Prefix DEKA = new Prefix("deka", "da", 1E1);


    public static Prefix NONE = new Prefix("no prefix", "", 1);


    public static Prefix DECI = new Prefix("deci", "d", 1E-1);
    public static Prefix CENTI = new Prefix("centi", "c", 1E-2);
    public static Prefix MILLI = new Prefix("milli", "m", 1E-3);
    public static Prefix MICRO = new Prefix("micro", "\u03bc", 1E-6);
    public static Prefix NANO = new Prefix("nano", "n", 1E-9);
    public static Prefix PICO = new Prefix("pico", "p", 1E-12);
    public static Prefix FEMTO = new Prefix("femto", "f", 1E-15);
    public static Prefix ATTO = new Prefix("atto", "a", 1E-18);
    public static Prefix ZEPTO = new Prefix("zepto", "z", 1E-21);
    public static Prefix YECTO = new Prefix("yocto", "y", 1E-24);


    private Prefix(String name, String symbol, double multiplier)
    {
        this.name = name;
        this.symbol = symbol;
        this.multiplier = multiplier;

    }

    public String toString()
    {
        return "Prefix: "+ name+ ", symbol: "+ symbol+ ", multiplier: "+ multiplier;
    }

    public boolean equals(Object otherObject)
    {
        if (! (otherObject instanceof Prefix)) return false;
        Prefix otherPrefix = (Prefix) otherObject;
        if (!name.equals(otherPrefix.name)) return false;
        if (!symbol.equals(otherPrefix.symbol)) return false;
        if (multiplier != otherPrefix.multiplier) return false;
        return true;
    }

    public static void main(String[] args)
    {
        System.out.println(Prefix.DECI);
        System.out.println(Prefix.NONE);
        System.out.println(Prefix.MEGA);

        System.out.println("Is "+ Prefix.GIGA
                           + " equal to "+ Prefix.GIGA
                           + "? "+ Prefix.GIGA.equals(Prefix.GIGA));
    }


    public double getMultiplier()
    {
        return multiplier;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public String getSafeSymbol()
    {
        if (this.name.equals("micro")) return "u";
        else return symbol;
    }



    public String getName()
    {
        return name;
    }

}
