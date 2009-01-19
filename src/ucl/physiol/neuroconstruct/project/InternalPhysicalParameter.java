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

package ucl.physiol.neuroconstruct.project;

import ucl.physiol.neuroconstruct.utils.units.*;
import java.beans.*;
import java.io.*;


/**
 * Parameter used for storing internal variables containing physical quantities
 *
 * @author Padraig Gleeson
 *  
 */


@SuppressWarnings("serial")

public class InternalPhysicalParameter extends InternalParameter
{
    public InternalPhysicalParameter()
    {

    }

    public InternalPhysicalParameter(String parameterName,
                                     String parameterDescription,
                                     float defaultValue,
                                     Units units)
    {
        super(parameterName, parameterDescription, defaultValue);
        this.units = units;
    }

    public Units units = null;

    @Override
    public String toString()
    {
        return "Internal Physical Parameter: " + parameterName
               + " ("+ parameterDescription
               + ") Value: "+ value
               + "(default: "+ defaultValue
               + ")"
               + "(units: "+ units.getSymbol()
               + ")";

    }
    public Units getUnits()
    {
        return units;
    }
    public void setUnits(Units units)
    {
        this.units = units;
    }
    
    
    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof InternalPhysicalParameter)
        {
            InternalPhysicalParameter other = (InternalPhysicalParameter) otherObj;

            if (parameterName.equals(other.parameterName) &&
                parameterDescription.equals(other.parameterDescription) &&
                units.equals(other.units) &&
                defaultValue == other.defaultValue &&
                value == other.value)
            {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args)
{
    try
    {
        File f = new File("../temp/unit.xml");
        FileOutputStream fos = new FileOutputStream(f);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        XMLEncoder xmlEncoder = new XMLEncoder(bos);

        Units units = UnitConverter.specificAxialResistanceUnits[0];
        //Units units = new Units("area" , new Unit[]{new Unit(Prefix.CENTI, Units.METER, 2)});
        

        System.out.println("Old units: "+units.getSafeSymbol()+", details: " + units.toLongString());

       // InternalPhysicalParameter ipp = new InternalPhysicalParameter("Jim", "fake param", 33, units);

        System.out.println("writeObject...");
        xmlEncoder.writeObject(units);

        xmlEncoder.flush();
        xmlEncoder.close();

        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        XMLDecoder xmlDecoder = new XMLDecoder(bis);

        System.out.println("readObject...");
        Object obj = xmlDecoder.readObject();

        System.out.println("Obj: "+obj);
        
        System.out.println("New Units: "+((Units)obj).getSafeSymbol()+", details: " + ((Units)obj).toLongString());

    }
    catch (Exception ex)
    {
        ex.printStackTrace();
        return;
    }

}


}
