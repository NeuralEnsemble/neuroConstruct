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

import java.util.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;

/**
 * Class for handling units. These classes are based on the CellML (http://www.cellml.org)
 * scheme for dealing with units
 *
 * @author Padraig Gleeson
 *  
 */

public class Units
{
    ClassLogger logger = new ClassLogger("Units");


    private String name = null;
    private String baseSymbol = null;
    private Unit[] subUnitList = null;
    private boolean siUnit = false;
    private boolean baseUnit = false;
    
    

    public static Units AMPERE = new Units("ampere", "A", true);
    public static Units FARAD = new Units("farad", "F", false);
    public static Units KATAL = new Units("katal", "kat", false);
    public static Units LUX = new Units("lux", "lx", false);
    public static Units PASCAL = new Units("pascal", "Pa", false);
    public static Units TESLA = new Units("tesla", "T", false);
    public static Units BECQUEREL = new Units("becquerel", "Bq", false);
    public static Units GRAM = new Units("gram", "g", false);
    public static Units KELVIN = new Units("kelvin", "K", true);
    public static Units METER = new Units("meter", "m", true);
    public static Units RADIAN = new Units("radian", "rad", false);
    public static Units VOLT = new Units("volt", "V", false);
    public static Units CANDELA = new Units("candela", "cd", true);
    public static Units GRAY = new Units("gray", "Gy", false);
    public static Units KILOGRAM = new Units("kilogram", "kg", true);
    public static Units METRE = new Units("metre", "m", false);
    public static Units SECOND = new Units("second", "s", true);
    public static Units WATT = new Units("watt", "W", false);
    public static Units CELSIUS = new Units("celsius", "C", false);
    public static Units HENRY = new Units("henry", "H", false);
    public static Units LITER = new Units("liter", "l", false);
    public static Units MOLE = new Units("mole", "mol", true);
    public static Units SIEMENS = new Units("siemens", "S", false);
    public static Units WEBER = new Units("weber", "Wb", false);
    public static Units COULOMB = new Units("coulomb", "C", false);
    public static Units HERTZ = new Units("hertz", "Hz", false);
    public static Units LITRE = new Units("litre", "l", false);
    public static Units NEWTON = new Units("newton", "N", false);
    public static Units SIEVERT = new Units("sievert", "Sv", false);
    public static Units DIMENSIONLESS = new Units("dimensionless", "", false);
    public static Units JOULE = new Units("joule", "J", false);
    public static Units LUMEN = new Units("lumen", "lm", false);
    public static Units OHM = new Units("ohm", "ohm", false);
    public static Units STERADIAN = new Units("steradian", "sr", false);


    /**
     * Here to support saving of Units via XMLEncoder
     */
    public Units()
    {
        logger.logComment("Units() called...");
    }

    private Units(String name, String symbol, boolean siUnit)
    {
        this.name = name;
        this.baseSymbol = symbol;
        this.siUnit = siUnit;
        baseUnit = true;
    }


    public Units(String name, Unit[] subUnitList) throws UnitsException
    {
        this.name = name;

        Vector<Units> usedUnits = new Vector<Units>();

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (usedUnits.contains(subUnitList[i].getUnits()))
                throw new UnitsException("The Units object cannot be created with multiple instances of one Unit ("+subUnitList[i].getUnits()+"). Use the exponent instead.");
            usedUnits.add(subUnitList[i].getUnits());
        }
        this.subUnitList = subUnitList;


        baseUnit = false;
    }

    /**
     * Creates a new unit, e.g. pH, using the name for the symbol
     */
    public static Units createNewBaseUnit(String name)
    {
        return new Units(name, name, false);
    }

    /**
     * Creates a new unit, e.g. pH
     */
    public static Units createNewBaseUnit(String name, String symbol)
    {
        return new Units(name, symbol, false);
    }


    @Override
    public boolean equals(Object otherObject)
    {
        if (!(otherObject instanceof Units)) return false;
        Units otherUnits = (Units) otherObject;
        //logger.logComment("Testing equality of "+ this.toLongString()+ " and "+ otherUnits.toLongString());
        if (!name.equals(otherUnits.name)) return false;


        if (! (baseSymbol == null && otherUnits.baseSymbol == null))
        {
            try
            {
                if (!baseSymbol.equals(otherUnits.baseSymbol)) return false;
            }
            catch (NullPointerException e)
            {
                return false;
            }
        }

        if (!(subUnitList == null && otherUnits.getSubUnitList() == null))
        {
            try
            {
                if (subUnitList.length != otherUnits.getSubUnitList().length) return false;

                for (int i = 0; i < subUnitList.length; i++)
                {
                        if (!subUnitList[i].getUnits().equals(otherUnits.getSubUnitList()[i].getUnits()))
                            return false;
                }
            }
            catch (NullPointerException e)
            {
                return false;
            }
        }
        return true;
    }


    public boolean compatibleWith(Units newUnits)
    {
        //System.out.println("Comparing "+ this + " with "+ newUnits);
        if (isBaseUnit())
        {
            if (newUnits.equals(this)) return true;
            if (newUnits.getSubUnitList().length != 1) return false;

            if (!newUnits.getSubUnitList()[0].getUnits().equals(this)) return false;
            return true;
        }


        if (newUnits.isBaseUnit())
        {
            if (subUnitList.length != 1) return false;
            if (subUnitList[0].equals(newUnits)) return false;
            return true;
        }


        if (!(subUnitList == null && newUnits.getSubUnitList() == null))
        {
            try
            {
                if (subUnitList.length != newUnits.getSubUnitList().length) return false;

                for (int i = 0; i < subUnitList.length; i++)
                {
                        if (!subUnitList[i].getUnits().equals(newUnits.getSubUnitList()[i].getUnits()))
                            return false;
                }
            }
            catch (NullPointerException e)
            {
                return false;
            }
        }
        return true;
    }


    public String getSymbol()
    {
        if (subUnitList == null) return baseSymbol;

        StringBuilder compositeSymbol = new StringBuilder();

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (subUnitList[i].getExponent()>0) compositeSymbol.append(subUnitList[i].getSymbol() + " ");
        }

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (subUnitList[i].getExponent()<0) compositeSymbol.append(subUnitList[i].getSymbol() + " ");
        }

        return compositeSymbol.toString().trim();
    }



    /**
     * Needed for NEURON etc which required ascii only in script files...
     */
    public String getSafeSymbol()
    {
        if (subUnitList == null) return baseSymbol;

        StringBuilder compositeSymbol = new StringBuilder();

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (subUnitList[i].getExponent()>0) compositeSymbol.append(subUnitList[i].getSafeSymbol() + " ");
        }

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (subUnitList[i].getExponent()<0) compositeSymbol.append(subUnitList[i].getSafeSymbol() + " ");
        }

        return compositeSymbol.toString().trim();
    }

    /**
     * Needed for NeuroML 2
     */
    public String getNeuroML2Symbol()
    {
        if (subUnitList == null) return baseSymbol;

        StringBuilder compositeSymbol = new StringBuilder();

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (subUnitList[i].getExponent()>0) compositeSymbol.append(subUnitList[i].getNeuroML2Symbol() + "_");
        }

        for (int i = 0; i < subUnitList.length; i++)
        {
            if (subUnitList[i].getExponent()<0) compositeSymbol.append(subUnitList[i].getNeuroML2Symbol() + "_");
        }
        
        String sym = compositeSymbol.toString();
        if (sym.endsWith("_"))
            sym = sym.substring(0, sym.length()-1);

        return sym.trim();
    }




    @Override
    public String toString()
    {
        return "Units["+ name + ", symbol: "+ getSymbol()+"]";
    }

    public String toLongString()
    {
        String longString =  "Units["+ name
            + ", symbol: "+ getSymbol()
            + ", base unit? "+ baseUnit
            + ", subUnitList: ";

        if (subUnitList!=null)
        {
            for (int i = 0; i < subUnitList.length; i++)
            {

                longString = longString + subUnitList[i].toLongString();
                if (i!=subUnitList.length-1) longString = longString + ", ";
            }
        }
        else
        {
            longString = longString + "null";
        }
        longString = longString +"]";

        return longString;
    }


    /**
     * Here to support saving of Units via XMLEncoder
     */
    public boolean isBaseUnit()
    {
       // logger.logComment("isBaseUnit being called: "+ baseUnit);
        return baseUnit;
    }

    /**
     * Here to support saving of Units via XMLEncoder
     */
    public Unit[] getSubUnitList()
    {
        //logger.logComment("getSubUnitList called on " + subUnitList[0].toLongString()+ ", etc.");
        return subUnitList;
    }


    /**
     * Here to support saving of Units via XMLEncoder
     */
    public void setSubUnitList(Unit[] subUnitList)
    {
        //logger.logComment("setSubUnitList called with " + subUnitList[0].toLongString()+ ", etc.");
        this.subUnitList = subUnitList;
    }





    /**
     * Here to support saving of Units via XMLEncoder
     */
    public boolean isSiUnit()
    {
        return siUnit;
    }


    /**
     * Here to support saving of Units via XMLEncoder
     */
    public void setSiUnit(boolean siUnit)
    {
        this.siUnit = siUnit;
    }

    /**
     * Here to support saving of Units via XMLEncoder
     */
    public String getName()
    {
        return name;
    }

    /**
     * Here to support saving of Units via XMLEncoder
     */
    public void setBaseSymbol(String baseSymbol)
    {
        this.baseSymbol = baseSymbol;
    }

    /**
     * Here to support saving of Units via XMLEncoder
     */
    public String getBaseSymbol()
    {
        return baseSymbol;
    }

    /**
     * Here to support saving of Units via XMLEncoder
     */
    public void setName(String name)
    {
        this.name = name;
    }



    /**
     * Here to support saving of Units via XMLEncoder
     */
    public void setBaseUnit(boolean baseUnit)
    {
        //logger.logComment("Base unit? being set to: "+ baseUnit);
        this.baseUnit = baseUnit;
    }

    public static void main(String[] args)
    {
        Units specCap = UnitConverter.specificCapacitanceUnits[UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS];

        System.out.println("Spec cap: "+ specCap.getBaseSymbol());
        System.out.println("Spec cap: "+ specCap.getSafeSymbol());
        System.out.println("Spec cap: "+ specCap.getNeuroML2Symbol());

        Units condDens = UnitConverter.conductanceDensityUnits[UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS];

        System.out.println("Cond dens: "+ condDens.getBaseSymbol());
        System.out.println("Cond dens: "+ condDens.getSafeSymbol());
        System.out.println("Cond dens: "+ condDens.getNeuroML2Symbol());
    }

}
