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
import java.util.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class NetReceiveElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("NetReceiveElement");

    String myParameterList = null;

    boolean initialised = false;


    public NetReceiveElement(ModFileChangeListener changeListener)
    {
        super("NET_RECEIVE", changeListener);
    }

    public void initialise(String parameterList)
    {
        myParameterList = parameterList;
        initialised = true;
    }


    public void addLine(String line) throws ModFileException
    {
        if (!initialised) throw new ModFileException("NetReceiveElement not initialised");
        FormattingChecker.checkGeneralFunctionLineFormat(line);
        addItemToInternalInfo(line.trim());
    }



    public String[] getUnformattedLines()
    {
        String[] allLines = new String[getInternalInfoSize()];
        for (int i = 0; i < allLines.length; i++)
        {
            allLines[i] = (String)getItemFromInternalInfo(i);
        }
        return allLines;
    }





    public String toString()
    {
        if (getInternalInfoSize()==0) return null; // i.e. no internal info so far...


        StringBuffer sb = new StringBuffer();

        sb.append(myType + " "  + myParameterList + " {");


        sb.append("\n");

        Iterator lineIterator = getInternalInfoIterator();
        while (lineIterator.hasNext())
        {
            sb.append("    " + (String) lineIterator.next() + "\n");
        }
        sb.append("}\n");

        return sb.toString();
    }





}
