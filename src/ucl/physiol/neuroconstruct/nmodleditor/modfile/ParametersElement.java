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
 * @version 1.0.4
 */

public class ParametersElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("ParametersElement");

    ModFile myModFile = null;

    public ParametersElement(ModFile modFile)
    {
        super("PARAMETER", modFile);
        myModFile = modFile;
        logger.setThisClassSilent(true);

    }

    public void addLine(String line) throws ModFileException
    {
        logger.logComment("Checking line: ("+ line+")");
        String originalLine = new String(line);
        line = line.trim();

        String name =null;
        double value = Double.MIN_VALUE;
        String dimension =null;
        String comment =null;



        int indexOfColon = line.indexOf(":");
        if (indexOfColon>=0)
        {
            comment = line.substring(indexOfColon + 1).trim();
            logger.logComment("Found comment: ("+comment+")");
            line = line.substring(0,indexOfColon).trim();

            if (line.length()==0) // i.e. just a comment lien
            {
                addItemToInternalInfo(": "+comment);
                return;
            }
        }

        int firstBracketIndex = line.indexOf("(");
        int secondBracketIndex = line.indexOf(")");
        if (firstBracketIndex>0) // dimensions present
        {
            if (secondBracketIndex<0) problem(originalLine); // i.e. unclosed bracket
            dimension = line.substring(firstBracketIndex+1, secondBracketIndex);
            line = line.substring(0, firstBracketIndex);
            if (!FormattingChecker.checkDimensionFormat(dimension)) problem(originalLine);
        }


        int indexOfEqualsSign = line.indexOf("=");

        if (indexOfEqualsSign>0)
        {
            try
            {
                value = Double.parseDouble(line.substring(indexOfEqualsSign + 1).
                                           trim());
            }
            catch (NumberFormatException ex)
            {
                problem(originalLine, "Badly formatted value");
            }

            name = line.substring(0, indexOfEqualsSign).trim();
        }
        else name = line.trim();
        logger.logComment("Found name of parameter: ("+name+")");
        if (!FormattingChecker.checkNamedVariableFormat(name)) problem(originalLine);

        ParameterEntry param = new ParameterEntry();
        param.name = name;
        param.value = value;
        param.dimension = dimension;
        param.comment  = comment;

        // check if it's a range or global variable

        if (FormattingChecker.checkIfVariableIsRange(name, myModFile))
        {
            logger.logComment("Looks like parameter "+name+" is a range variable");
            param.variableType = ParameterEntry.RANGE_VARIABLE;
        }
        else if (FormattingChecker.checkIfVariableIsGlobal(name, myModFile))
        {
            logger.logComment("Looks like parameter "+name+" is a global variable");
            param.variableType = ParameterEntry.GLOBAL_VARIABLE;
        }
        addItemToInternalInfo(param);
    }



    public boolean addParameter(String name, double value, String dimension, String comment, int variableType)
    {
        if (parameterNameAlreadyExists(name)) return false;
        ParameterEntry param = new ParameterEntry();
        param.name = name;

        if (value != Double.MIN_VALUE) param.value = value;

        if (dimension!=null && !dimension.equals("")) param.dimension = dimension;
        if (comment!=null && !comment.equals("")) param.comment = comment;

        param.variableType = variableType;

        addItemToInternalInfo(param);

        return true;
    }


    private boolean parameterNameAlreadyExists(String name)
    {
        Iterator iter = getParameters().iterator();
        while (iter.hasNext())
        {
            try
            {
                ParameterEntry param = (ParameterEntry) iter.next();
                if (param.name.equals(name)) return true;
            }
            catch (ClassCastException ex)
            {
            }
        }
        return false;

    }


    public String toString()
    {
        if (getInternalInfoSize()==0) return null; // i.e. no internal info so far...

        Vector correctlyFormattedLines = new Vector();
        Iterator intInfo = getInternalInfoIterator();
        while (intInfo.hasNext())
        {
            Object obj = intInfo.next();
            try
            {
                ParameterEntry param = (ParameterEntry) obj;
                String lineToAdd = param.toString();
                correctlyFormattedLines.add(lineToAdd);
            }
            catch (ClassCastException ex)
            {
                correctlyFormattedLines.add((String)obj);
            }
        }
        return formatLines(correctlyFormattedLines);
    }

    public List getParameters()
    {
        List justParameters = new Vector();
        Iterator iter = getInternalInfoIterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            try
            {
                ParameterEntry param = (ParameterEntry) obj;
                justParameters.add(param);
            }
            catch (ClassCastException ex)
            {
            }
        }
        logger.logComment("Returning info on "+justParameters.size()+" named variables");
        return justParameters;
    }

    public boolean removeParameter(ParameterEntry param)
    {
        logger.logComment("internalInfo before: "+ this.toString());
        int indexOfParameter = getIndexInInternalInfo(param);
        if (indexOfParameter<0) return false;

        if (param.variableType==ParameterEntry.RANGE_VARIABLE)
        {

        }

        boolean removeUnit = removeFromInternalInfo(param);
        logger.logComment("internalInfo after: "+ this.toString());
        return removeUnit;
    }

    private void problem(String line, String error) throws ModFileException
    {
        throw new ModFileException("Problem: "+error+"\nAt line: "+ line);
    }

    private void problem(String line) throws ModFileException
    {
        problem(line, "Error when parsing.");
    }



    public class ParameterEntry
    {
        public static final int RANGE_VARIABLE = 0;
        public static final int GLOBAL_VARIABLE = 1;
        public static final int GENERAL_VARIABLE = 2;

        public String name = null;
        public double value = Double.MIN_VALUE;
        public String dimension = null;
        public String comment = null;
        public int variableType = GENERAL_VARIABLE;

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(name);
            if (value!=Double.MIN_VALUE) sb.append(" = "+ value);
            if (dimension!=null) sb.append(" ("+ dimension + ")");
            if (comment!=null) sb.append(" : "+ comment);
            return sb.toString();
        }
    }




}
