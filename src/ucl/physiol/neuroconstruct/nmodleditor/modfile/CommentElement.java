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

public class CommentElement extends ModFileBlockElement
{
    public CommentElement(ModFileChangeListener changeListener)
    {
        super("COMMENT", changeListener);
    }

    /**
     * Will be ENDCOMMENT for a COMMENT bloack
     */
    public String getBlockEndingString()
    {
        return "ENDCOMMENT";
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
        sb.append("ENDCOMMENT\n\n");
        return sb.toString();
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

        CommentElement ae = new CommentElement(listener);
        try
        {
            ae.addLine("   helloe");
            ae.addLine("dare");
            System.out.println("---------    Internal vals: ");
            System.out.println(ae);

        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }
    }

}
