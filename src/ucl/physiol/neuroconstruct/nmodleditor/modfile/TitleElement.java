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

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */


public class TitleElement extends SimpleModFileElement
{
    public TitleElement()
    {
        super("TITLE", null);
    }

    public TitleElement(String title)
    {
        super("TITLE", "TITLE " +title);
    }

    public String getTitle()
    {
        if (this.toString()==null) return null;
        else return myContent.substring("TITLE ".length());
    }


    public void setTitle(String title)
    {
        if (title.trim().length()==0) myContent=null;
        else myContent = "TITLE " +title;
    }


}
