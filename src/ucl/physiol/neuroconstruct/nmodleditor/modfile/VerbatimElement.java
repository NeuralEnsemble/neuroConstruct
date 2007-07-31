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
 * @version 1.0.4
 */

public class VerbatimElement extends ModFileBlockElement
{
    public VerbatimElement(ModFileChangeListener changeListener)
    {
        super("VERBATIM", changeListener);
    }

    /**
     * Will be ENDVERBATIM for a VERBATIM bloack
     */
    public String getBlockEndingString()
    {
        return "ENDVERBATIM";
    }

    protected String formatLines(Vector lines)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(myType);

        sb.append("\n");
        Iterator lineIterator = lines.iterator();
        while (lineIterator.hasNext())
        {
            sb.append(standardIndentation + (String) lineIterator.next() + "\n");
        }
        sb.append("ENDVERBATIM\n");
        return sb.toString();
    }


}
