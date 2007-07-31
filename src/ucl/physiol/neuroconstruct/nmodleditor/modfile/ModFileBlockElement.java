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

public abstract class ModFileBlockElement extends SimpleModFileElement
{
    private Vector internalInfo = new Vector();

    ModFileChangeListener myChangeListener = null;

    String standardIndentation = "    ";

    public ModFileBlockElement(String type, ModFileChangeListener changeListener)
    {
        super(type, null);
        myType = type;
        myChangeListener = changeListener;
    }
    /**
     * Will normally be a closing curly bracket, but for COMMENT it'll be ENDCOMMENT, etc.
     */
    public String getBlockEndingString()
    {
        return "}";
    }



    // These functions are to ensure internalInfo is well "wrapped"
    // so we can keep an eye on changing the data...

    public void addLine(String line) throws ModFileException
    {
        if (line==null) return;
        addItemToInternalInfo(line.trim());
    }

    protected void addItemToInternalInfo(Object item)
    {
        internalInfo.add(item);
        myChangeListener.modFileElementChanged(myType);
    }

    protected boolean checkItemInInternalInfo(Object item)
    {
        return internalInfo.contains(item);
    }


    protected Iterator getInternalInfoIterator()
    {
        return internalInfo.iterator();
    }


    protected int getInternalInfoSize()
    {
        return internalInfo.size();
    }

    protected Object getItemFromInternalInfo(int index)
    {
        return internalInfo.elementAt(index);
    }

    protected int getIndexInInternalInfo(Object item)
    {
        return internalInfo.indexOf(item);
    }

    protected boolean removeFromInternalInfo(Object item)
    {
        return internalInfo.remove(item);
    }


    public void reset()
    {
        internalInfo.removeAllElements();
    }



    public String toString()
    {
         if (internalInfo.size()==0) return null; // i.e. no internal info so far...

        // this extra step is needed if the Vector lineInfo contains extra info,
        // in sub classes e.g. as in UnitsElement
        return formatLines(internalInfo);
    }

    protected String formatLines(Vector lines)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(myType + " {");

        if (lines.size() == 1)
        {
            sb.append(" " + (String) lines.elementAt(0) + " }\n");
        }
        else
        {
            sb.append("\n");

            Iterator lineIterator = lines.iterator();
            while (lineIterator.hasNext())
            {
                sb.append(standardIndentation + (String) lineIterator.next() + "\n");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }



}
