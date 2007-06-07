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
 * @version 1.0.3
 */


public class LocalElement extends SimpleModFileElement
{
    public LocalElement()
    {
        super("LOCAL", null);
    }

    public LocalElement(String title)
    {
        super("LOCAL", "LOCAL " +title);
    }

  //  public String getLocalVariables()
  //  {
  //      if (this.toString()==null) return null;
  //      else return myContent.substring("LOCAL ".length());
  //  }


}
