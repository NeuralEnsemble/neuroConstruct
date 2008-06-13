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

package ucl.physiol.neuroconstruct.utils.units;

import ucl.physiol.neuroconstruct.utils.ClassLogger;

/**
 * Class for handling units. These classes are based on the CellML (http://www.cellml.org)
 * scheme for dealing with units
 *
 * @author Padraig Gleeson
 *  
 */

public class Unit
{
    ClassLogger logger = new ClassLogger("Unit");

    private Prefix prefix = null;
    private Units units = null;
    private int exponent = 1;

    /**
     * Here to support saving of Unit via XMLEncoder
     */
    public Unit()
    {
        logger.logComment("Calling Unit()");
    }

    public Unit(Prefix prefix, Units units, int exponent) throws UnitsException
    {
        if (!units.isBaseUnit())
                throw new UnitsException("The new Unit object must be created with a base Units object");

        if (prefix == null) prefix = Prefix.NONE;
        this.prefix = prefix;
        this.units = units;
        this.exponent = exponent;
    }

    protected String getSymbol()
    {
        String symbol = prefix.getSymbol() + units.getSymbol();
        if (units.equals(Units.KILOGRAM) && prefix!=Prefix.NONE) symbol = prefix.getSymbol() + "g??";
        if (exponent==2) symbol = symbol +"\u00b2";
        else if (exponent==3) symbol = symbol +"\u00b3";
        else if (exponent==-2) symbol = symbol +"\u207b\u00b2";
        else if (exponent==-3) symbol = symbol +"\u207b\u00b3";
        else if (exponent!=1) symbol = symbol + "^" + exponent;
        else if (exponent==0) symbol = "";

        return symbol;

    }

    /**
     * Needed for NEURON etc which required ascii only in script files...
     */
    protected String getSafeSymbol()
    {
        String symbol = prefix.getSafeSymbol() + units.getSafeSymbol();
        if (units.equals(Units.KILOGRAM) && prefix!=Prefix.NONE) symbol = prefix.getSymbol() + "g??";
        else if (exponent!=1) symbol = symbol + "^" + exponent;
        else if (exponent==0) symbol = "";

        return symbol;

    }




    @Override
    public boolean equals(Object otherObject)
    {
        if (!(otherObject instanceof Unit)) return false;
        Unit otherUnit = (Unit) otherObject;
        if (!prefix.equals(otherUnit.prefix)) return false;
        if (!units.equals(otherUnit.units)) return false;
        if (exponent != otherUnit.exponent) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 13 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
        hash = 13 * hash + (this.units != null ? this.units.hashCode() : 0);
        hash = 13 * hash + this.exponent;
        return hash;
    }

    protected String toLongString()
    {


        return "Unit[prefix: "+prefix
            + ", units: "+ units.toLongString()
            + ", exponent: "+ exponent+"]";

    }



    /**
     * All these are here to support saving of Units via XMLEncoder
     */

    public int getExponent()
    {
        return exponent;
    }

    public Prefix getPrefix()
    {
        return prefix;
    }

    protected Units getUnits()
    {
        //System.out.println("Unit.getUnits called: "+units+"..");
        return units;
    }
    public void setUnits(Units units)
    {
        //System.out.println("Unit.setunits called: "+units+"...");
        this.units = units;
    }
    
    public void setPrefix(Prefix prefix)
    {
        this.prefix = prefix;
    }
    
    public void setExponent(int exponent)
    {
        this.exponent = exponent;
    }


}
