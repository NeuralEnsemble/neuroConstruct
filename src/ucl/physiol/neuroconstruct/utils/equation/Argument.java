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


package ucl.physiol.neuroconstruct.utils.equation;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class Argument
{
    String name = null;
    double value = 0;

    public Argument(String name, double value)
    {
        this.name = name;
        this.value = value;
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
