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

package ucl.physiol.neuroconstruct.cell;

import java.beans.*;
import java.io.*;
import ucl.physiol.neuroconstruct.utils.units.UnitConverter;



 /**
  * A class representing properties associated with an ion (based on NEURON's USEION)
  *
  * @author Padraig Gleeson
  *  
  *
  */



public class IonProperties implements Serializable
{
    static final long serialVersionUID = -63945762534849L;
    
    public static final String MECHANISM_NAME = "Ion Properties";

    private String name = null;

    private float reversalPotential = Float.NaN;

    private float internalConcentration = Float.NaN;
    private float externalConcentration = Float.NaN;
    

    public IonProperties()
    {
    }


    public IonProperties(String name, float reversalPotential)
    {
        this.name = name;
        this.reversalPotential = reversalPotential;
    }
    public IonProperties(String name, float internalConcentration, float externalConcentration)
    {
        this.name = name;
        this.externalConcentration = externalConcentration;
        this.internalConcentration = internalConcentration;
        
    }


    @Override
    public Object clone()
    {
        IonProperties ip2 = new IonProperties();
        ip2.setName(new String(name));
        ip2.setReversalPotential(reversalPotential);
        ip2.setInternalConcentration(internalConcentration);
        ip2.setExternalConcentration(externalConcentration);
        return ip2;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final IonProperties other = (IonProperties) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
        {
            return false;
        }
        
        if (this.revPotSetByConcs()!=other.revPotSetByConcs())
        {
            return false;
        }
        
        if (this.revPotSetByConcs())
        {
            if (this.internalConcentration != other.internalConcentration)
            {
                return false;
            }
            if (this.externalConcentration != other.externalConcentration)
            {
                return false;
            }
        }
        else
        {
            if (this.reversalPotential != other.reversalPotential)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 71 * hash + Float.floatToIntBits(this.reversalPotential);
        hash = 71 * hash + Float.floatToIntBits(this.internalConcentration);
        hash = 71 * hash + Float.floatToIntBits(this.externalConcentration);
        return hash;
    }




    public float getExternalConcentration()
    {
        return externalConcentration;
    }

    public void setExternalConcentration(float extrenalConcentration)
    {
        this.externalConcentration = extrenalConcentration;
    }

    public float getInternalConcentration()
    {
        return internalConcentration;
    }

    public void setInternalConcentration(float internalConcentration)
    {
        this.internalConcentration = internalConcentration;
    }

    public boolean revPotSetByConcs()
    {
        if (! new Float(reversalPotential).isNaN()) return false;
        return true;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public float getReversalPotential()
    {
        return reversalPotential;
    }

    public void setReversalPotential(float reversalPotential)
    {
        this.reversalPotential = reversalPotential;
    }



    @Override
    public String toString()
    {
        return toString(false);

    }

    public String toString(boolean html)
    {
        String pre = html ? "<b>":"";
        String post = html ? "</b>":"";

        if (revPotSetByConcs())
        {
            String units = " "+UnitConverter.concentrationUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSafeSymbol();

            return "Ion: "+pre + name +post +", int conc: "+pre+internalConcentration+units+post+
                    ", ext conc: "+pre+externalConcentration+units+post;
        }
        else
            return "Ion: "+pre + name+post +", rev pot: "+pre+reversalPotential+" mV"+post;

    }

    public static void main(String[] args) throws CloneNotSupportedException
    {
        IonProperties ion1 = new IonProperties("na", 55);
        IonProperties ion2 = new IonProperties("na", 10, 100);
        IonProperties ion3 = new IonProperties("k", -77);
        IonProperties ion4 = new IonProperties("na", 55);
        IonProperties ion5 = new IonProperties("na", 10, 100);

        System.out.println("New: "+ ion1+"\nh: "+ion1.hashCode());
        System.out.println("New: "+ ion2+"\nh: "+ion2.hashCode());
        System.out.println("New: "+ ion3+"\nh: "+ion3.hashCode());
        System.out.println("New: "+ ion4+"\nh: "+ion4.hashCode());

        System.out.println("Equals n: "+ ion1.equals(ion2));
        System.out.println("Equals y: "+ ion1.equals(ion4));
        System.out.println("Equals n: "+ ion2.equals(ion4));
        System.out.println("Equals y: "+ ion2.equals(ion5));
        
        IonProperties ion1_c = (IonProperties)ion1.clone();
        System.out.println("Equals y: "+ ion1.equals(ion1_c));

        try
        {
            File f = new File("c:\\temp\\ion1.xml");
            FileOutputStream fos = new FileOutputStream(f);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XMLEncoder xmlEncoder = new XMLEncoder(bos);

            xmlEncoder.writeObject(ion1);

            xmlEncoder.flush();
            xmlEncoder.close();

            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            XMLDecoder xmlDecoder = new XMLDecoder(bis);


             Object obj = xmlDecoder.readObject();
             System.out.println("Obj: "+ obj);

        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
            return;
        }

    }


}
