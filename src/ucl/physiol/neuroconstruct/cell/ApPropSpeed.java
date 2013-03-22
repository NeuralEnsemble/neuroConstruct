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

import ucl.physiol.neuroconstruct.utils.units.*;


 /**
  * A class representing the propagation velocity of an action potential along a segment.
  * This can be used to calculate the time between the AP reaching the final fully simulated section of a cell
  * (where bio realistic ion channels are included) to the pre synaptic point. This therefore gives a pause between, say
  * the soma firing and a point on an axonal arborisation reaching threshold. Any additional delay specified for the synapse
  * (in the Network connection) is added to this delay. Note: a more complex mechanism could be implemented wherby the velocity
  * could be calculated from the diameter, axial/membrane resistance, capacitance, etc. but it is felt that a simple velocity
  * might be most suitable because of the difficulty in obtaining data on axons.
  *
  * @author Padraig Gleeson
  *  
  *
  */



public class ApPropSpeed implements Serializable
{
    static final long serialVersionUID = -1837538506739L;
    
    public static final String MECHANISM_NAME = "AP propagation speed";

    //public String name = null;  // needed?
    private float speed;

    public ApPropSpeed()
    {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Float.floatToIntBits(this.speed);
        return hash;
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj instanceof ApPropSpeed)
        {
            ApPropSpeed other = (ApPropSpeed) otherObj;

            if (speed == other.getSpeed())
            {
                return true;
            }
        }
        return false;
    }


    @Override
    public Object clone()
    {
        ApPropSpeed aps2 = new ApPropSpeed();
        aps2.setSpeed(this.speed);

        return aps2;
    }


    public ApPropSpeed(float speed)
    {
        this.speed = speed;
    }

    @Override
    public String toString()
    {
        return "AP propagation speed: " + speed+" "
            +UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+" "
            +UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
    }

    public static void main(String[] args) throws CloneNotSupportedException
    {
        ApPropSpeed appv = new ApPropSpeed(0);

        System.out.println("New: "+ appv+", h: "+appv.hashCode());

    }

    public float getSpeed()
    {
        return speed;
    }
    public void setSpeed(float speed)
    {
        this.speed = speed;
    }

}
