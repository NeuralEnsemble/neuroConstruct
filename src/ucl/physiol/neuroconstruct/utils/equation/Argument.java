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


package ucl.physiol.neuroconstruct.utils.equation;

import java.io.Serializable;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class Argument implements Serializable
{
    static final long serialVersionUID = -7167676767444L;
    
    String name = null;
    double value = 0;

    public Argument(String name, double value)
    {
        this.name = name;
        this.value = value;
    }

    public Argument()
    {
    }
    
    
    @Override
    public Object clone()
    {
        Argument a2 = new Argument(new String(name), value);
        return a2;
    }
    

    public String getName()
    {
        return name;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
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
        final Argument other = (Argument) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name)))
        {
            return false;
        }
        if (this.value != other.value)
        {
            return false;
        }
        return true;
    }
    
    



    @Override
    public String toString()
    {
        return "Argument[name: "+name+", value: "+value+"]";
    }


    public static String toString(Argument[] args)
    {
        StringBuffer sb = new StringBuffer("Argument[]: [");
        for (int i = 0; i < args.length; i++)
        {
            sb.append(args[i].getName()+ " = "+ args[i].getValue() );
            if (i<args.length-1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
