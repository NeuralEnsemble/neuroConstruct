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

public class StateElement extends NamedVariableElement
{

    public StateElement(ModFile modFile)
    {
        super("STATE", modFile);
        logger = new ClassLogger("StateElement");
        logger.setThisClassSilent(true);
    }
    /**
     * fhfhkgf
     */
    public static void main(String args[])
    {

        try
        {
            ModFile modFile = new ModFile("c:\\temp\\testmod\\test.mod");

            StateElement ae = new StateElement(modFile);

            ae.addLine("h m n");
            ae.addLine("hh (mm) nn (jj) kk(pp)");
/*
            ae.addLine("val   (dim) :comm");
            ae.addLine("val :comm");
            ae.addLine("val   (dim) ");
            ae.addLine(" : comm");
            ae.addLine("vamm  ");
            ae.addLine("val   ( mm)");

*/
            System.out.println("Internal vals: ");
            System.out.println(ae);
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }

    }

}
