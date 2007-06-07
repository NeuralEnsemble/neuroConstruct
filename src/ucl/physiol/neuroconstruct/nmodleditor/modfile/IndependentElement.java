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
 * @version 1.0.3
 */


public class IndependentElement extends SimpleModFileElement
{

    ClassLogger logger = new ClassLogger("IndependentElement");

    public IndependentElement()
    {
        super("INDEPENDENT", null);
        logger.logComment("New IndependentElement created: "+ this);
    }

    public IndependentElement(String content)
    {
        super("INDEPENDENT", content);
        logger.logComment("New IndependentElement created: "+ this);
    }

    public String toString()
    {
        return this.myType + " {" + myContent + "}\n";
    }


}
