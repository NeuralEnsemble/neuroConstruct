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

public class GenericModFileElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("GenericModFileElement");

    public GenericModFileElement(String elementName, ModFileChangeListener changeListener)
    {
        super(elementName, changeListener);

        logger.setThisClassSilent(true);
        logger.logComment("New GenericModFileElement called: "+elementName+" created");
    }

    public static void main(String args[])
    {
        ModFileChangeListener listener = new ModFileChangeListener()
        {
            public void modFileElementChanged(String modFileElementType)
            {
                System.out.println("Change in: " + modFileElementType);
            }
            public void modFileChanged(){};
        };

        GenericModFileElement el = new GenericModFileElement("NEURON", listener);
        try
        {
            el.addLine("SUFFIX cagk");
            el.addLine("USEION ca");

            System.out.println("-----------------------");
            System.out.println(el.toString());
            System.out.println("-----------------------");
        }
        catch (ModFileException ex)
        {
        }
    }

}
