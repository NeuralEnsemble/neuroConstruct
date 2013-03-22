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

/**
 * Class for converting between the different units used in neuroConstruct/simulators
 *
 * @author Padraig Gleeson
 *  
 */

public class UnitConverter
{
    static ClassLogger logger = new ClassLogger("UnitConverter");

    public static int NEUROCONSTRUCT_UNITS = 0;
    public static int NEURON_UNITS = 1;
    public static int GENESIS_SI_UNITS = 2;
    public static int GENESIS_PHYSIOLOGICAL_UNITS = 3;

    private static String[] unitSystemDescriptions
        = new String[]{"neuroConstruct Units",
                       "NEURON Units",
                       "GENESIS SI Units",
                       "GENESIS Physiological Units"};


    public static Units[] timeUnits = null;
    public static Units[] voltageUnits = null;

    public static Units[] currentDensityUnits = null;

    public static Units[] resistanceUnits = null;

    public static Units[] currentUnits = null;

    public static Units[] specificCapacitanceUnits = null;
    public static Units[] capacitanceUnits = null;
    public static Units[] lengthUnits = null;

    public static Units[] conductanceUnits = null;
    public static Units[] conductanceDensityUnits = null;

    public static Units[] specificAxialResistanceUnits = null;
    public static Units[] specificMembraneResistanceUnits = null;

    public static Units[] concentrationUnits = null;

    public static Units[] dimensionlessUnits = null;




    /** @todo Make handling these units more generic... */

    public static Units[] perUnitVoltageUnits = null;
    public static Units[] areaUnits = null;
    public static Units[] volumeUnits = null;
    public static Units[] rateUnits = null;
    public static Units[] perUnitTimeVoltageUnits = null;

    static
    {
        try
        {
            timeUnits = new Units[]
                {new Units("neuroConstruct_time"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.SECOND, 1)}),
                new Units("NEURON_time"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SECOND, 1)}),
                new Units("GENESIS_SI_time"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.SECOND, 1)}),
                new Units("GENESIS_PHY_time"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SECOND, 1)})};


            voltageUnits = new Units[]
                {new Units("neuroConstruct_voltage"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.VOLT, 1)}),
                new Units("NEURON_voltage"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.VOLT, 1)}),
                new Units("GENESIS_SI_voltage"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.VOLT, 1)}),
                new Units("GENESIS_PHY_voltage"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.VOLT, 1)})};


            currentDensityUnits = new Units[]
                {new Units("neuroConstruct_currentDensity"
                           , new Unit[]
                           {new Unit(Prefix.MICRO, Units.AMPERE, 1),
                           new Unit(Prefix.MICRO, Units.METER, -2)}),
                new Units("NEURON_currentDensity"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.AMPERE, 1),
                          new Unit(Prefix.CENTI, Units.METER, -2)}),
                new Units("GENESIS_SI_currentDensity"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.AMPERE, 1),
                          new Unit(Prefix.NONE, Units.METER, -2)}),
                new Units("GENESIS_PHY_currentDensity"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.AMPERE, 1),
                          new Unit(Prefix.CENTI, Units.METER, -2)})};

            currentUnits = new Units[]
                {new Units("neuroConstruct_current"
                           , new Unit[]
                           {new Unit(Prefix.NANO, Units.AMPERE, 1)}),
                new Units("NEURON_current"
                          , new Unit[]
                          {new Unit(Prefix.NANO, Units.AMPERE, 1)}),
                new Units("GENESIS_SI_current"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.AMPERE, 1)}),
                new Units("GENESIS_PHY_current"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.AMPERE, 1)})};



            specificCapacitanceUnits = new Units[]
                {new Units("neuroConstruct_specificCapacitance"
                           , new Unit[]
                           {new Unit(Prefix.MICRO, Units.FARAD, 1),
                           new Unit(Prefix.MICRO, Units.METER, -2)}),
                new Units("NEURON_specificCapacitance"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.FARAD, 1),
                          new Unit(Prefix.CENTI, Units.METER, -2)}),
                new Units("GENESIS_SI_specificCapacitance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.FARAD, 1),
                          new Unit(Prefix.NONE, Units.METER, -2)}),
                new Units("GENESIS_PHY_specificCapacitance"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.FARAD, 1),
                          new Unit(Prefix.CENTI, Units.METER, -2)})};

            capacitanceUnits = new Units[]
                {new Units("neuroConstruct_capacitance"
                           , new Unit[]
                           {new Unit(Prefix.MICRO, Units.FARAD, 1)}),
                new Units("NEURON_capacitance"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.FARAD, 1)}),
                new Units("GENESIS_SI_capacitance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.FARAD, 1)}),
                new Units("GENESIS_PHY_capacitance"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.FARAD, 1)})};

            lengthUnits = new Units[]
                {new Units("neuroConstruct_length"
                           , new Unit[]
                           {new Unit(Prefix.MICRO, Units.METER, 1)}),
                new Units("NEURON_length"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.METER, 1)}),
                new Units("GENESIS_SI_length"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.METER, 1)}),
                new Units("GENESIS_PHY_length"
                          , new Unit[]
                          {new Unit(Prefix.CENTI, Units.METER, 1)})};




            resistanceUnits = new Units[]
                {new Units("neuroConstruct_resistance"
                           , new Unit[]
                           {new Unit(Prefix.KILO, Units.OHM, 1)}),
                new Units("NEURON_resistance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.OHM, 1)}),
                new Units("GENESIS_SI_resistance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.OHM, 1)}),
                new Units("GENESIS_PHY_resistance"
                          , new Unit[]
                          {new Unit(Prefix.KILO, Units.OHM, 1)})};

            conductanceUnits = new Units[]
                {new Units("neuroConstruct_conductance"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.SIEMENS, 1)}),
                new Units("NEURON_conductance"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.SIEMENS, 1)}),
                new Units("GENESIS_SI_conductance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.SIEMENS, 1)}),
                new Units("GENESIS_PHY_conductance"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SIEMENS, 1)})};

            conductanceDensityUnits = new Units[]
                {new Units("neuroConstruct_conductanceDensity"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.SIEMENS, 1),
                           new Unit(Prefix.MICRO, Units.METER, -2)}),
                new Units("NEURON_conductanceDensity"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.SIEMENS, 1),
                          new Unit(Prefix.CENTI, Units.METER, -2)}),
                new Units("GENESIS_SI_conductanceDensity"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.SIEMENS, 1),
                          new Unit(Prefix.NONE, Units.METER, -2)}),
                new Units("GENESIS_PHY_conductanceDensity"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SIEMENS, 1),
                          new Unit(Prefix.CENTI, Units.METER, -2)})};


            specificAxialResistanceUnits = new Units[]
                {new Units("neuroConstruct_specAxialResistance"
                           , new Unit[]
                           {new Unit(Prefix.KILO, Units.OHM, 1),
                           new Unit(Prefix.MICRO, Units.METER, 1)}),
                new Units("NEURON_specAxialResistance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.OHM, 1),
                          new Unit(Prefix.CENTI, Units.METER, 1)}),
                new Units("GENESIS_SI_specAxialResistance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.OHM, 1),
                          new Unit(Prefix.NONE, Units.METER, 1)}),
                new Units("GENESIS_PHY_specAxialResistance"
                          , new Unit[]
                          {new Unit(Prefix.KILO, Units.OHM, 1),
                          new Unit(Prefix.CENTI, Units.METER, 1)})};



            specificMembraneResistanceUnits = new Units[]
                {new Units("neuroConstruct_specificMembraneResistance"
                           , new Unit[]
                           {new Unit(Prefix.KILO, Units.OHM, 1),
                           new Unit(Prefix.MICRO, Units.METER, 2)}),
                new Units("NEURON_specificMembraneResistance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.OHM, 1),
                          new Unit(Prefix.CENTI, Units.METER, 2)}),
                new Units("GENESIS_SI_specificMembraneResistance"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.OHM, 1),
                          new Unit(Prefix.NONE, Units.METER, 2)}),
                new Units("GENESIS_PHY_specificMembraneResistance"
                          , new Unit[]
                          {new Unit(Prefix.KILO, Units.OHM, 1),
                          new Unit(Prefix.CENTI, Units.METER, 2)})};



            concentrationUnits = new Units[]
                {new Units("neuroConstruct_concentration"
                           , new Unit[]
                           {new Unit(Prefix.NONE, Units.MOLE, 1),
                           new Unit(Prefix.MICRO, Units.METER, -3)}),
                           
                new Units("NEURON_concentration"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.MOLE, 1),
                          new Unit(Prefix.DECI, Units.METER, -3)}), // NEURON uses mM i.e. milli moles per liter

                new Units("GENESIS_SI_concentration"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.MOLE, 1),
                          new Unit(Prefix.NONE, Units.METER, -3)}),
                          
                new Units("GENESIS_PHY_concentration"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.MOLE, 1),
                          new Unit(Prefix.CENTI, Units.METER, -3)})};






            perUnitVoltageUnits = new Units[]
                {new Units("neuroConstruct_perUnitVoltage"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.VOLT, -1)}),
                new Units("NEURON_perUnitVoltage"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.VOLT, -1)}),
                new Units("GENESIS_SI_perUnitVoltage"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.VOLT, -1)}),
                new Units("GENESIS_PHY_perUnitVoltage"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.VOLT, -1)})};


            rateUnits = new Units[]
                {new Units("neuroConstruct_perUnitTime"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.SECOND, -1)}),
                new Units("NEURON_perUnitTime"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SECOND, -1)}),
                new Units("GENESIS_SI_perUnitTime"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.SECOND, -1)}),
                new Units("GENESIS_PHY_perUnitTime"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SECOND, -1)})};



            areaUnits = new Units[]
                {new Units("neuroConstruct_area"
                           , new Unit[]
                           {new Unit(Prefix.MICRO, Units.METER, 2)}),
                new Units("NEURON_area"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.METER, 2)}),
                new Units("GENESIS_SI_area"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.METER, 2)}),
                new Units("GENESIS_PHY_area"
                          , new Unit[]
                          {new Unit(Prefix.CENTI, Units.METER, 2)})};

            volumeUnits = new Units[]
                {new Units("neuroConstruct_area"
                           , new Unit[]
                           {new Unit(Prefix.MICRO, Units.METER, 3)}),
                new Units("NEURON_area"
                          , new Unit[]
                          {new Unit(Prefix.MICRO, Units.METER, 3)}),
                new Units("GENESIS_SI_area"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.METER, 3)}),
                new Units("GENESIS_PHY_area"
                          , new Unit[]
                          {new Unit(Prefix.CENTI, Units.METER, 3)})};




            perUnitTimeVoltageUnits = new Units[]
                {new Units("neuroConstruct_perUnitTime"
                           , new Unit[]
                           {new Unit(Prefix.MILLI, Units.SECOND, -1),
                           new Unit(Prefix.MILLI, Units.VOLT, -1)}),
                new Units("NEURON_perUnitTime"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SECOND, -1),
                           new Unit(Prefix.MILLI, Units.VOLT, -1)}),
                new Units("GENESIS_SI_perUnitTime"
                          , new Unit[]
                          {new Unit(Prefix.NONE, Units.SECOND, -1),
                           new Unit(Prefix.NONE, Units.VOLT, -1)}),
                new Units("GENESIS_PHY_perUnitTime"
                          , new Unit[]
                          {new Unit(Prefix.MILLI, Units.SECOND, -1),
                           new Unit(Prefix.MILLI, Units.VOLT, -1)})};




             dimensionlessUnits = new Units[]
                 {new Units("neuroConstruct_dimensionless"
                            , new Unit[]
                            {new Unit(Prefix.NONE, Units.DIMENSIONLESS, 1)}),
                 new Units("NEURON_dimensionless"
                           , new Unit[]
                            {new Unit(Prefix.NONE, Units.DIMENSIONLESS, 1)}),
                 new Units("GENESIS_SI_dimensionless"
                           , new Unit[]
                            {new Unit(Prefix.NONE, Units.DIMENSIONLESS, 1)}),
                 new Units("GENESIS_PHY_dimensionless"
                           , new Unit[]
                            {new Unit(Prefix.NONE, Units.DIMENSIONLESS, 1)})};




        }
        catch(Exception e)
        {
            logger.logError("Error initialising units", e);
        }
    }


    public UnitConverter()
    {
    }

    /**
     * Converts values from neuroConstruct native units
     */
    public static PhysicalQuantity convertFromNeuroConstruct(double value,
                                                   Units nCunits,
                                                   int toUnits)
    {

        if (nCunits.equals(timeUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           timeUnits[NEUROCONSTRUCT_UNITS],
                           timeUnits[toUnits]);

        else if (nCunits.equals(voltageUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           voltageUnits[NEUROCONSTRUCT_UNITS],
                           voltageUnits[toUnits]);




        else if (nCunits.equals(currentDensityUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           currentDensityUnits[NEUROCONSTRUCT_UNITS],
                           currentDensityUnits[toUnits]);


        else if (nCunits.equals(currentUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           currentUnits[NEUROCONSTRUCT_UNITS],
                           currentUnits[toUnits]);


        else if (nCunits.equals(specificCapacitanceUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           specificCapacitanceUnits[NEUROCONSTRUCT_UNITS],
                           specificCapacitanceUnits[toUnits]);


        else if (nCunits.equals(capacitanceUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           capacitanceUnits[NEUROCONSTRUCT_UNITS],
                           capacitanceUnits[toUnits]);


        else if (nCunits.equals(lengthUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           lengthUnits[NEUROCONSTRUCT_UNITS],
                           lengthUnits[toUnits]);


        else if (nCunits.equals(conductanceUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           conductanceUnits[NEUROCONSTRUCT_UNITS],
                           conductanceUnits[toUnits]);


        else if (nCunits.equals(conductanceDensityUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           conductanceDensityUnits[NEUROCONSTRUCT_UNITS],
                           conductanceDensityUnits[toUnits]);


        else if (nCunits.equals(specificAxialResistanceUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           specificAxialResistanceUnits[NEUROCONSTRUCT_UNITS],
                           specificAxialResistanceUnits[toUnits]);


        else if (nCunits.equals(resistanceUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           resistanceUnits[NEUROCONSTRUCT_UNITS],
                           resistanceUnits[toUnits]);


        else if (nCunits.equals(specificMembraneResistanceUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           specificMembraneResistanceUnits[NEUROCONSTRUCT_UNITS],
                           specificMembraneResistanceUnits[toUnits]);


        else if (nCunits.equals(concentrationUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           concentrationUnits[NEUROCONSTRUCT_UNITS],
                           concentrationUnits[toUnits]);




        else if (nCunits.equals(rateUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           rateUnits[NEUROCONSTRUCT_UNITS],
                           rateUnits[toUnits]);


        else if (nCunits.equals(areaUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           areaUnits[NEUROCONSTRUCT_UNITS],
                           areaUnits[toUnits]);

        else if (nCunits.equals(volumeUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           volumeUnits[NEUROCONSTRUCT_UNITS],
                           volumeUnits[toUnits]);


        else if (nCunits.equals(perUnitVoltageUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           perUnitVoltageUnits[NEUROCONSTRUCT_UNITS],
                           perUnitVoltageUnits[toUnits]);


        else if (nCunits.equals(perUnitTimeVoltageUnits[NEUROCONSTRUCT_UNITS]))
            return convert(value,
                           perUnitTimeVoltageUnits[NEUROCONSTRUCT_UNITS],
                           perUnitTimeVoltageUnits[toUnits]);




       else return new PhysicalQuantity(value, nCunits);
    }


    public static double getTime(double value, int fromUnits, int toUnits)
    {
        // Quick check..
        if (fromUnits == NEUROCONSTRUCT_UNITS && toUnits == GENESIS_PHYSIOLOGICAL_UNITS)
            return value;
        
        if (toUnits == NEUROCONSTRUCT_UNITS && fromUnits == GENESIS_PHYSIOLOGICAL_UNITS)
            return value;
        
        Units oldUnits = timeUnits[fromUnits];
        Units newUnits = timeUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }


    public static double getRate(double value, int fromUnits, int toUnits)
    {
        // Quick check..
        if (fromUnits == NEUROCONSTRUCT_UNITS && toUnits == GENESIS_PHYSIOLOGICAL_UNITS)
            return value;
        
        if (toUnits == NEUROCONSTRUCT_UNITS && fromUnits == GENESIS_PHYSIOLOGICAL_UNITS)
            return value;
        
        Units oldUnits = rateUnits[fromUnits];
        Units newUnits = rateUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }


    public static double getVoltage(double value, int fromUnits, int toUnits)
    {
        // Quick check..
        if (fromUnits == NEUROCONSTRUCT_UNITS && toUnits == GENESIS_PHYSIOLOGICAL_UNITS)
            return value;
        
        if (toUnits == NEUROCONSTRUCT_UNITS && fromUnits == GENESIS_PHYSIOLOGICAL_UNITS)
            return value;
        
        Units oldUnits = voltageUnits[fromUnits];
        Units newUnits = voltageUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }


    public static double getDimensionless(double value, int fromUnits, int toUnits)
    {
        return value;
    }



    public static double getCurrentDensity(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = currentDensityUnits[fromUnits];
        Units newUnits = currentDensityUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getCurrent(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = currentUnits[fromUnits];
        Units newUnits = currentUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getSpecificCapacitance(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = specificCapacitanceUnits[fromUnits];
        Units newUnits = specificCapacitanceUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getCapacitance(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = capacitanceUnits[fromUnits];
        Units newUnits = capacitanceUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getArea(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = areaUnits[fromUnits];
        Units newUnits = areaUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getVolume(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = volumeUnits[fromUnits];
        Units newUnits = volumeUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getLength(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = lengthUnits[fromUnits];
        Units newUnits = lengthUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getConductance(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = conductanceUnits[fromUnits];
        Units newUnits = conductanceUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getConductanceDensity(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = conductanceDensityUnits[fromUnits];
        Units newUnits = conductanceDensityUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getSpecificAxialResistance(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = specificAxialResistanceUnits[fromUnits];
        Units newUnits = specificAxialResistanceUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getResistance(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = resistanceUnits[fromUnits];
        Units newUnits = resistanceUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getSpecificMembraneResistance(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = specificMembraneResistanceUnits[fromUnits];
        Units newUnits = specificMembraneResistanceUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static double getConcentration(double value, int fromUnits, int toUnits)
    {
        Units oldUnits = concentrationUnits[fromUnits];
        Units newUnits = concentrationUnits[toUnits];
        return convert(value, oldUnits, newUnits).getMagnitude();
    }

    public static String getUnitSystemDescription(int unitSystem)
    {
        return unitSystemDescriptions[unitSystem];
    }

    public static int getUnitSystemIndex(String unitSystemDescription)
    {
        if (unitSystemDescription.equalsIgnoreCase("SI Units"))
            return GENESIS_SI_UNITS;
        
        if (unitSystemDescription.equalsIgnoreCase("Physiological Units"))
            return GENESIS_PHYSIOLOGICAL_UNITS;
        
        for (int i = 0; i < unitSystemDescriptions.length; i++)
        {
            if (unitSystemDescriptions[i].equals(unitSystemDescription))
                return i;
        }
        return -1;
    }



    /**
     * Note: this is private to shield Units classes from code (as they could change in the future)
     */
    private static PhysicalQuantity convert(double value, Units oldUnits, Units newUnits)
    {
        PhysicalQuantity oldPQ = new PhysicalQuantity(value, oldUnits);

        PhysicalQuantity newPQ = null;

        try
        {
            newPQ = oldPQ.cloneWithUnits(newUnits);
        }
        catch (UnitsException ex)
        {
            logger.logError("Problem converting from units:"+oldUnits+" ", ex);
        }

        //logger.logComment("Converted old quantity: "+ oldPQ+ " to new: "+ newPQ);

        return newPQ;
    }



    public static void main(String[] args)
    {
       // double value = 0.1;
        int from = NEUROCONSTRUCT_UNITS;
       // int to = GENESIS_SI_UNITS;
/*
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getTime(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getVoltage(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getConcentration(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getCapacitance(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getCurrentDensity(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getCurrent(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getSpecificAxialResistance(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getSpecificCapacitance(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getSpecificMembraneResistance(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getLength(value, from, to));
        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getResistance(value, from, to));


        System.out.println("From: "+from+" to: "+ to+ ":  "+ UnitConverter.getConductanceDensity(value, from, to));




        System.out.println("Freq: "+ convertFromNeuroConstruct(value,
                UnitConverter.rateUnits[NEUROCONSTRUCT_UNITS], to));
    */

        //System.out.println("Equal: " + areaUnits[from].equals(lengthUnits[from]));

        //PhysicalQuantity area = UnitConverter.convertFromNeuroConstruct(value, areaUnits[from],
                                                                //        to);

        System.out.println("Area: " + areaUnits[from]);

        double specCap = UnitConverter.getSpecificCapacitance(1,
                                                              UnitConverter.NEUROCONSTRUCT_UNITS,
                                                              UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);

        System.out.println("specCap: " + specCap);

        PhysicalQuantity axRes = new PhysicalQuantity(1,
                                                      UnitConverter.specificAxialResistanceUnits[UnitConverter.NEUROCONSTRUCT_UNITS]);

        System.out.println("axRes: " + axRes);

        System.out.print("axRes si:" + UnitConverter.getConductance(axRes.getMagnitude(),
                                                                    UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                    UnitConverter.GENESIS_SI_UNITS) + "\n");

        System.out.print("axRes phys:" + UnitConverter.getConductance(axRes.getMagnitude(),
                                                                      UnitConverter.NEUROCONSTRUCT_UNITS,
                                                                      UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + "\n");

        System.out.println("--------------------------------------");

        int fromUnits = NEURON_UNITS;
        PhysicalQuantity concn = new PhysicalQuantity(7.55e-5, concentrationUnits[fromUnits]);

        System.out.println("Concentration: "+ concn);

        System.out.println("Concentration GENESIS_SI_UNITS: " + UnitConverter.getConcentration(concn.getMagnitude(),
            fromUnits,
            UnitConverter.GENESIS_SI_UNITS) + " " + concentrationUnits[GENESIS_SI_UNITS].getSymbol());

        System.out.println("Concentration GENESIS_PHYSIOLOGICAL_UNITS: " + UnitConverter.getConcentration(concn.getMagnitude(),
            fromUnits,
            UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + " " + concentrationUnits[GENESIS_PHYSIOLOGICAL_UNITS].getSymbol());

        System.out.println("Concentration NEURON: " + UnitConverter.getConcentration(concn.getMagnitude(),
            fromUnits,
            UnitConverter.NEURON_UNITS) + " " + concentrationUnits[NEURON_UNITS].getSymbol());

        System.out.println("Concentration nC: " + UnitConverter.getConcentration(concn.getMagnitude(),
            fromUnits,
            UnitConverter.NEUROCONSTRUCT_UNITS) + " " + concentrationUnits[NEUROCONSTRUCT_UNITS].getSymbol());


        System.out.println("--------------------------------------");

        PhysicalQuantity dimLess = new PhysicalQuantity(100, dimensionlessUnits[fromUnits]);

        System.out.println("dimLess: "+ dimLess);

        System.out.println("Concentration NEURON: " + UnitConverter.getDimensionless(dimLess.getMagnitude(),
            fromUnits,
            UnitConverter.NEURON_UNITS) + dimensionlessUnits[NEURON_UNITS].getSymbol());
        
        
        fromUnits = GENESIS_SI_UNITS;
        PhysicalQuantity cond = new PhysicalQuantity(1, conductanceUnits[fromUnits]);

        System.out.println("cond: "+ cond.getMagnitude()+ " "+cond.getUnits().getSafeSymbol());

        System.out.println("cond GENESIS_SI_UNITS: " + UnitConverter.getConductance(cond.getMagnitude(),
            fromUnits,
            UnitConverter.GENESIS_SI_UNITS) + conductanceUnits[GENESIS_SI_UNITS].getSafeSymbol());

        System.out.println("cond GENESIS_PHYSIOLOGICAL_UNITS: " + UnitConverter.getConductance(cond.getMagnitude(),
            fromUnits,
            UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS) + conductanceUnits[GENESIS_PHYSIOLOGICAL_UNITS].getSafeSymbol());

        System.out.println("cond NEURON: " + UnitConverter.getConductance(cond.getMagnitude(),
            fromUnits,
            UnitConverter.NEURON_UNITS) + conductanceUnits[NEURON_UNITS].getSafeSymbol());



        PhysicalQuantity currDens = new PhysicalQuantity(0.000001, concentrationUnits[NEUROCONSTRUCT_UNITS]);

        System.out.println("currDens GENESIS_PHYSIOLOGICAL_UNITS: " + UnitConverter.getCurrentDensity(currDens.getMagnitude(),
            UnitConverter.NEUROCONSTRUCT_UNITS,
            UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS));

        System.out.println("currDens GENESIS_SI_UNITS: " + UnitConverter.getCurrentDensity(currDens.getMagnitude(),
            UnitConverter.NEUROCONSTRUCT_UNITS,
            UnitConverter.GENESIS_SI_UNITS));

        System.out.println("currDens NEURON_UNITS: " + UnitConverter.getCurrentDensity(currDens.getMagnitude(),
            UnitConverter.NEUROCONSTRUCT_UNITS,
            UnitConverter.NEURON_UNITS));

        float mg_conc = 0.2f;
        System.out.println("mg_conc: "+mg_conc);

        float mg_conc3 = (float)UnitConverter.getConcentration(mg_conc, UnitConverter.GENESIS_SI_UNITS, UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);
        System.out.println("mg_conc3: "+mg_conc3);

        float mg_conc2 = (float)UnitConverter.getConcentration(mg_conc3, UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
        System.out.println("mg_conc2: "+mg_conc2);




        float curr = 0.2f;
        System.out.println("curr: "+curr);

        float curr3 = (float)UnitConverter.getCurrent(curr, UnitConverter.NEUROCONSTRUCT_UNITS, UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS);
        System.out.println("curr3: "+curr3);

        float curr2 = (float)UnitConverter.getCurrent(curr3, UnitConverter.GENESIS_PHYSIOLOGICAL_UNITS, UnitConverter.NEUROCONSTRUCT_UNITS);
        System.out.println("curr2: "+curr2);
        
        


    }

}
