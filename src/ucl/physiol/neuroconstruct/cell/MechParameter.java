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
 

import java.io.Serializable;

 /**
  * A class representing a generic parameter for a channel mechanism
  *
  * @author Padraig Gleeson
  *  
  *
  */

public class MechParameter implements Serializable
{
    static final long serialVersionUID = -6656553218847575L;

    private String name = null;
    private float value = Float.NaN;
    
    
    public MechParameter()
    {
        
    }
    
    public MechParameter(MechParameter mp)
    {
        this.name = new String (mp.getName());
        this.value = mp.getValue();
    }
    
    public MechParameter(String name, float value)
    {
        this.name = name;
        this.value = value;
    }
    
    @Override
    public Object clone()
    {
        MechParameter mp2 = new MechParameter();
        mp2.setName(new String(name));
        mp2.setValue(value);
        return mp2;
    }
    
    
    public String getName()
    {
        return name;
    }
    public float getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MechParameter other = (MechParameter) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + Float.floatToIntBits(this.value);
        return hash;
    }
    
    
    public void setName(String name)
    {
        this.name = name;
    }
    public void setValue(float value)
    {
        this.value = value;
    }
    @Override
    public String toString()
    {
        return name +" = "+value;
    }
    
    public static void main(String[] args) 
    {
        MechParameter mp1 = new MechParameter("mp1", 1234);
        MechParameter mp2 = new MechParameter("mp1",1234);
        
        System.out.println("mp1 == mp2: "+ mp1.equals(mp2));
        
    }

}
