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

public class ProcedureElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("ProcedureElement");

    public String myProcedureName = null;
    public String myParameterList = null;

    boolean initialised = false;


    public ProcedureElement(ModFileChangeListener changeListener)
    {
        super("PROCEDURE", changeListener);
        logger.setThisClassSilent(true);
    }

    public void initialise(String procedureName, String parameterList)
    {

        logger.logComment("Initialising with: "+ procedureName + " and "+ parameterList);
        myProcedureName = procedureName;
        myParameterList = parameterList;
        initialised = true;
    }


    public void addLine(String line) throws ModFileException
    {
        if (!initialised) throw new ModFileException("Procedure object not initialised");
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

        sb.append(myType + " " + myProcedureName + myParameterList + " {");
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

        ProcedureElement pe = new ProcedureElement(listener);
        pe.initialise("alpha", "(v (mV))(/ms)");
        try
        {
            pe.addLine("alp = v*2");
            pe.addLine("alp = v*2");

            System.out.println("---------    Internal vals: ");
            System.out.println(pe);
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }
    }
}
