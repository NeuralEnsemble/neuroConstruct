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

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public abstract class ParsableEquationsElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("ParsableEquationsElement");

    public ParsableEquationsElement(String type, ModFileChangeListener changeListener)
    {
        super(type, changeListener);
    }

    public void addLine(String line) throws ModFileException
    {
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




}
