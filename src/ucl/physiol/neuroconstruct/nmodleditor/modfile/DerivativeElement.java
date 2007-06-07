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

public class DerivativeElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("DerivativeElement");

    public String myDerivativeName = null;

    boolean initialised = false;


    public DerivativeElement(ModFileChangeListener changeListener)
    {
        super("DERIVATIVE", changeListener);
    }

    public void initialise(String derivName)
    {
        this.myDerivativeName = derivName;
        initialised = true;
    }

    public void reset()
    {
        super.reset();
        myDerivativeName = null;
        initialised = false;
    }


    public void addLine(String line) throws ModFileException
    {
        if (!initialised) throw new ModFileException("Derivative object not initialised");
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
        if (getInternalInfoSize() == 0)
            return null; // i.e. no internal info so far...

        StringBuffer sb = new StringBuffer();

        sb.append(myType + " " + myDerivativeName+" {");


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

        DerivativeElement fe = new DerivativeElement(listener);
        fe.initialise("state");
        try
        {
            fe.addLine("alp' = v*2");
            fe.addLine("alp2' = v*2");

            System.out.println("---------    Internal vals: ");
            System.out.println(fe);
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }
    }
}
