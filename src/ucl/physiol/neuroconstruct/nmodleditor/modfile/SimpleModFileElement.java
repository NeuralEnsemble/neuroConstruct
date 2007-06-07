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

import java.util.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class SimpleModFileElement
{
    String myType = null;

    protected String myContent = null;

    private SimpleModFileElement(){};

    public SimpleModFileElement(String type, String content)
    {
        this.myType = type;
        this.myContent = content;
    }

    public String toString()
    {
        return myContent;
    }


    public String getType()
    {
        return myType;
    }


}
