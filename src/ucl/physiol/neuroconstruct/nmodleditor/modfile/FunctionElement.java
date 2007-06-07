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
import ucl.physiol.neuroconstruct.utils.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class FunctionElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("FunctionElement");

    public String myFunctionName = null;
    public String myParameterList = null;

    boolean initialised = false;


    public FunctionElement(ModFileChangeListener changeListener)
    {
        super("FUNCTION", changeListener);
        logger.setThisClassSilent(true);
    }

    public void initialise(String functionName, String parameterList)
    {
        logger.logComment("Initialising with: "+ functionName + " and "+ parameterList);
        myFunctionName = functionName;
        myParameterList = parameterList;
        initialised = true;
    }


    public void addLine(String line) throws ModFileException
    {
        if (!initialised) throw new ModFileException("Function object not initialised");
        FormattingChecker.checkGeneralFunctionLineFormat(line);
        addItemToInternalInfo(line.trim());
    }


    public String[] getUnformattedLines()
    {
        String[] allLines = new String[getInternalInfoSize()];
        for (int i = 0; i < allLines.length; i++)
        {
            allLines[i] = (String) getItemFromInternalInfo(i);
        }
        return allLines;
    }



    public String toString()
    {
        if (getInternalInfoSize() == 0)
            return null; // i.e. no internal info so far...


        StringBuffer sb = new StringBuffer();

        sb.append(myType + " " + myFunctionName + myParameterList + " {");


        sb.append("\n");

        Iterator lineIterator = getInternalInfoIterator();
        while (lineIterator.hasNext())
        {
            sb.append("    " + (String) lineIterator.next() + "\n");
        }
        sb.append("}\n");

        return sb.toString();
    }



    public static void main(String[] args)
    {
        ModFileChangeListener listener = new ModFileChangeListener()
        {
            public void modFileElementChanged(String modFileElementType)
            {
                System.out.println("Change in: " + modFileElementType);
            }
            public void modFileChanged(){};
        };

        FunctionElement fe = new FunctionElement(listener);
        fe.initialise("alpha", "(v (mV))(/ms)");
        try
        {
            fe.addLine("alp = v*2");
            fe.addLine("alp = v*2");

            System.out.println("---------    Internal vals: ");
            System.out.println(fe);
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }
    }
}
