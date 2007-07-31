/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2007 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.cell;

import ucl.physiol.neuroconstruct.utils.units.*;


 /**
  * A class representing the propagation velocity of an action potential along a segment
  * This can be used to calculate the time between the AP reaching the final fully simulated section of a cell
  * (where bio realistic ion channels are included) to the pre synaptic point. This therefore gives a pause between, say
  * the soma firing and a point on an axonal arborisation reaching threshold. Any additional delay specified for the synapse
  * (in the Network connection) is added to this delay. Note: a more complex mechanism could be implemented wherby the velocity
  * could be calculated from the diameter, axial/membrane resistance, capacitance, etc. but it is felt that a simple velocity
  * might be most suitable because of the difficulty in obtaining data on axons.
  *
  * @author Padraig Gleeson
  * @version 1.0.4
  *
  */



public class ApPropSpeed
{
     public static final String MECHANISM_NAME = "AP propagation speed";

    //public String name = null;  // needed?
    private float speed;

    public ApPropSpeed()
    {
    }

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


    public ApPropSpeed(float speed)
    {
        this.speed = speed;
    }

    public String toString()
    {
        return "AP propagation speed: " + speed+" "
            +UnitConverter.lengthUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol()+" "
            +UnitConverter.rateUnits[UnitConverter.NEUROCONSTRUCT_UNITS].getSymbol();
    }

    public static void main(String[] args) throws CloneNotSupportedException
    {
        ApPropSpeed appv = new ApPropSpeed(1200);

        System.out.println("New: "+ appv);

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
