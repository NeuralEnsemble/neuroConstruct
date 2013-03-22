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

package ucl.physiol.neuroconstruct.utils.units;

/**
 * Class for handling units. These classes are based on the CellML (http://www.cellml.org)
 * scheme for dealing with units
 *
 * @author Padraig Gleeson
 *  
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


    /**
     * Here to support saving of Units via XMLEncoder
     */
    public Prefix()
    {
        //System.out.println("Prefix() called...");
    }
    
    private Prefix(String name, String symbol, double multiplier)
    {
        this.name = name;
        this.symbol = symbol;
        this.multiplier = multiplier;

    }

    @Override
    public String toString()
    {
        return "Prefix: "+ name+ ", symbol: "+ symbol+ ", multiplier: "+ multiplier;
    }

    @Override
    public boolean equals(Object otherObject)
    {
        if (! (otherObject instanceof Prefix)) return false;
        Prefix otherPrefix = (Prefix) otherObject;
        if (!name.equals(otherPrefix.name)) return false;
        if (!symbol.equals(otherPrefix.symbol)) return false;
        if (multiplier != otherPrefix.multiplier) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.symbol != null ? this.symbol.hashCode() : 0);
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.multiplier) ^ (Double.doubleToLongBits(this.multiplier) >>> 32));
        return hash;
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
    public void setMultiplier(double m)
    {
        //System.out.println("Prefix.setMultiplier called...");
        multiplier = m;
    }

    public String getSymbol()
    {
        return symbol;
    }
    
    public void setSymbol(String s)
    {
        symbol = s;
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
    
    public void setName(String n)
    {
        name = n;
    }

}
