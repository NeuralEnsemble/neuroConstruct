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

import ucl.physiol.neuroconstruct.utils.*;
import java.text.*;

/**
 * Class for handling units. These classes are based on the CellML (http://www.cellml.org)
 * scheme for dealing with units
 *
 * @author Padraig Gleeson
 *  
 */

public class PhysicalQuantity
{
    ClassLogger logger = new ClassLogger("PhysicalQuantity");

    private double magnitude = 0;
    private Units units = null;

    private PhysicalQuantity()
    {

    }

    public PhysicalQuantity(double magnitude, Units units)
    {
        this.magnitude = magnitude;
        this.units = units;
    }

    @Override
    public String toString()
    {
        return magnitude + " " + units.getSymbol();
    }

    public PhysicalQuantity cloneWithUnits(Units newUnits) throws UnitsException
    {
        //logger.logComment("Cloning PhysicalQuantity: "+this+" with new units: "+ newUnits);

        if (!units.compatibleWith(newUnits))
            throw new UnitsException("The new Units object ("
                                     +newUnits
                                     +") is not compatible with the old Units ("
                                     +this.units
                                     +")");

        double factor = 1;

        int numToTry = 0;
        if (newUnits.isBaseUnit()) numToTry = 1;
        else numToTry = newUnits.getSubUnitList().length;

        for (int i = 0; i < numToTry; i++)
        {
            double oldMultiplier, newMultiplier;
            if (units.isBaseUnit())  oldMultiplier = 1;
            else
            {
                oldMultiplier = units.getSubUnitList()[i].getPrefix().getMultiplier();
                oldMultiplier = Math.pow(oldMultiplier, units.getSubUnitList()[i].getExponent());
            }

            if (newUnits.isBaseUnit())  newMultiplier = 1;
            else
            {
                newMultiplier = newUnits.getSubUnitList()[i].getPrefix().getMultiplier();
                newMultiplier = Math.pow(newMultiplier, newUnits.getSubUnitList()[i].getExponent());
            }

            factor = factor * (oldMultiplier/newMultiplier);

            logger.logComment("Factor for old unit: "+ oldMultiplier
                              + ", new unit: "+ newMultiplier
                              + " therefore new total factor: "+ factor
                              + " and new magnitude: "+magnitude*factor);

        }

        DecimalFormat firstFormatter = new DecimalFormat("0.######E0");

        try
        {
            PhysicalQuantity newPQ
                = new PhysicalQuantity(firstFormatter.parse(firstFormatter.format(magnitude * factor)).doubleValue(), newUnits);

            //System.out.println("Converted "+ this + " to "+ newPQ);
            return newPQ;
        }
        catch(ParseException pe)
        {
            logger.logError("Parsing error creating PhysicalQuantity:", pe);
            return null;
        }


    }



    public static void main(String[] args)
    {
        PhysicalQuantity length = new PhysicalQuantity(12, Units.METER);

        try
        {
            Units area = new Units("area", new Unit[]
                                   {new Unit(Prefix.NONE, Units.METER, 2)});

            Units conductance = new Units("conductance",
                                          new Unit[]
                                          {new Unit(Prefix.NONE, Units.SIEMENS, 1),
                                          new Unit(Prefix.NONE, Units.METER, -2)});


            Units conductance2 = new Units("conductance2",
                                          new Unit[]
                                          {new Unit(Prefix.MICRO, Units.SIEMENS, 1),
                                          new Unit(Prefix.CENTI, Units.METER, -2)});


           Units phyArea = new Units("area", new Unit[]
                                  {new Unit(Prefix.CENTI, Units.METER, 2)});


           PhysicalQuantity theMass = new PhysicalQuantity(20, new Units("milligram",
                                          new Unit[]
                                          {new Unit(Prefix.NONE, Units.KILOGRAM, 1)}));

            System.out.println("The mass: "+ theMass);



            PhysicalQuantity theArea = new PhysicalQuantity(144, area);

            PhysicalQuantity cond = new PhysicalQuantity(0.03, conductance);

            System.out.println("The length is: " + length);

            Units centiLen = new Units("centilength", new Unit[]
                                   {new Unit(Prefix.CENTI, Units.METER, 1)});

            System.out.println("centiLen unit: "+ centiLen+", multiplier: "+ centiLen.getSubUnitList()[0].getPrefix());

            System.out.println("The NEW length with units: "+centiLen+" is: " + length.cloneWithUnits(centiLen));

            System.out.println("The area is: " + theArea);

            PhysicalQuantity newArea = theArea.cloneWithUnits(phyArea);


            System.out.println("The newArea is: " + newArea);

            System.out.println("The conductance is: " + cond);

            System.out.println("Is " + conductance
                               + " equal to " + conductance2
                               + "?  " + conductance.equals(conductance2));

            System.out.println("Is " + conductance
                               + " compatible with " + conductance2
                               + "?  " + conductance.compatibleWith(conductance2));


            System.out.println("The NEW conductance is: " + cond.cloneWithUnits(conductance2));


            Units ph = Units.createNewBaseUnit("pH");

            System.out.println("Acidity: "+ new PhysicalQuantity(7, ph));

            Units permeter = new Units("permeter", new Unit[]
                                   {new Unit(Prefix.NONE, Units.METER, -1)});

        Units permillimeter = new Units("permillimeter", new Unit[]
                                {new Unit(Prefix.MILLI, Units.METER, -1)});


        PhysicalQuantity smalldens = new PhysicalQuantity(4f, permeter);

        System.out.println("permeter: "+ smalldens);
        System.out.println("permillimeter: "+ smalldens.cloneWithUnits(permillimeter));


        PhysicalQuantity condDensity
            = new PhysicalQuantity(1,
                                   UnitConverter.conductanceDensityUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        System.out.println("\n\n\nOrig value: "+ condDensity.toString());

        System.out.println("Neuron value: "
           + UnitConverter.convertFromNeuroConstruct(condDensity.getMagnitude(),
                                                     condDensity.getUnits(),
                                                     UnitConverter.NEURON_UNITS));

            System.out.println("GENESIS SI value: "
               + UnitConverter.convertFromNeuroConstruct(condDensity.getMagnitude(),
                                                         condDensity.getUnits(),
                                                         UnitConverter.GENESIS_SI_UNITS));

            System.out.println("GENESIS PHY value: "
               + UnitConverter.convertFromNeuroConstruct(condDensity.getMagnitude(),
                                                         condDensity.getUnits(),
                                                         UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS));

        PhysicalQuantity conc
            = new PhysicalQuantity(1,
                                   UnitConverter.concentrationUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        System.out.println("\n\n\nOrig value: "+ conc.toString());

        System.out.println("Neuron value: "
           + UnitConverter.convertFromNeuroConstruct(conc.getMagnitude(),
                                                     conc.getUnits(),
                                                     UnitConverter.NEURON_UNITS));

            System.out.println("GENESIS SI value: "
               + UnitConverter.convertFromNeuroConstruct(conc.getMagnitude(),
                                                         conc.getUnits(),
                                                         UnitConverter.GENESIS_SI_UNITS));

            System.out.println("GENESIS PHY value: "
               + UnitConverter.convertFromNeuroConstruct(conc.getMagnitude(),
                                                         conc.getUnits(),
                                                         UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS));



        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


    }
    public double getMagnitude()
    {
        return magnitude;
    }
    public Units getUnits()
    {
        return units;
    }



}
