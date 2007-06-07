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
 * @version 1.0.3
 */

public class FormattingChecker
{
    static ClassLogger logger = new ClassLogger("FormattingChecker");

    static
    {
        logger.setThisClassSilent(true);
    }


    public static boolean checkDimensionFormat(String dimension)
    {
        if (dimension==null) return false;
        if (dimension.indexOf(" ")>0) return false;
        /** @todo more checks... */
        return true;
    }

    public static boolean checkNamedVariableFormat(String name)
    {
        logger.logComment("Checking format of variable name: "+ name);
        if (name==null) return false;
        if (name.indexOf(" ")>0) return false;
        /** @todo more checks...? */
        return true;
    }

    public static boolean checkGeneralNameFormat(String name)
    {
        if (name==null) return false;
        if (name.indexOf(" ")>0) return false;
        /** @todo more checks...? */
        return true;
    }

    public static boolean checkGeneralFunctionLineFormat(String line) throws ModFileException
    {
        if (line==null) throw new ModFileException("Empty line");

        /** @todo More checks.
         * This was just a simple few tests to catch out most lines...
         */
/*
        if ((!line.trim().startsWith(":")) &&
            line.indexOf("=")<0 &&
            line.indexOf("(")<0 &&
            line.indexOf("LOCAL")<0 &&
            line.indexOf("UNITSOFF")<0 &&
            line.indexOf("UNITSON")<0 &&
            line.indexOf("else")<0 &&
            !line.equals("") &&
            line.indexOf("SOLVE")<0)
            throw new ModFileException("Line is neither an equation or a comment: ("+ line+")");
*/

        return true;
    }

    public static boolean checkIfVariableIsRange(String variableName, ModFile modFile)
    {

        return modFile.myNeuronElement.isRangeVariable(variableName);
    }

    public static boolean checkIfVariableIsGlobal(String variableName, ModFile modFile)
    {
        return modFile.myNeuronElement.isGlobalVariable(variableName);
    }



}
