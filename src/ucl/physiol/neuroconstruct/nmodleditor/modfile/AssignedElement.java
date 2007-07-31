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

package ucl.physiol.neuroconstruct.nmodleditor.modfile;

import ucl.physiol.neuroconstruct.utils.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class AssignedElement extends NamedVariableElement
{

    public AssignedElement(ModFile modFile)
    {
        super("ASSIGNED", modFile);
        logger = new ClassLogger("AssignedElement");
        logger.setThisClassSilent(true);
    }

    public static void main(String args[])
    {
        AssignedElement ae = new AssignedElement(null);
        try
        {
            ae.addLine("val (dim) : comm");
            ae.addLine("val (dim) ");
            ae.addLine(": comm");
            ae.addLine("vamm");
            ae.addLine("v al( mm)");
            System.out.println("Internal vals: ");
            System.out.println(ae);
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }

    }

}
