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

public abstract class NamedVariableElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("NamedVariableElement");
    ModFile myModFile = null;

    public NamedVariableElement(String type, ModFile modFile)
    {
        super(type, modFile);
        myModFile = modFile;

        logger.setThisClassSilent(true);
    }

    public void addLine(String line) throws ModFileException
    {
        logger.logComment("Checking line: "+ line);
        line = line.trim();


        // The possible options we will check for:

        // val
        // val1 val2 val3...
        // val (dim)
        // val1 (dim1) val2 (dim2) val3 (dim3)...
        // : comm
        // val : com
        // val (dim) : com

        int firstOpenBracketIndex = line.indexOf("(");
        int firstClosingBracketIndex = line.indexOf(")");
        int colonIndex = line.indexOf(":");


        // option: val1 val2 val3...
        if (firstOpenBracketIndex<0
            && colonIndex<0
            && line.indexOf(" ")>0)
        {
            logger.logComment("Only one line, multiple entries, splitting up & redoing...");
            String[] lines = line.split("\\s+");
            for (int i = 0; i < lines.length; i++)
            {
                addLine(lines[i]);
            }
            return;
        }


        // option: val1 (dim1) val2 (dim2) val3 (dim3)...
        if (line.indexOf("(", firstClosingBracketIndex)>0)
        {
            logger.logComment("Multiple brakets...");

            String[] bracketEntries = line.split("\\)");
            for (int i = 0; i < bracketEntries.length; i++)
            {
                addLine(bracketEntries[i]+")"); // as the ) is cut in the split..
            }
            return;

        }


        NamedVariableEntry namedVar = new NamedVariableEntry();

        if (colonIndex == 0) // i.e. option:  : comm    , just a comment
        {
            addItemToInternalInfo(line);
        }
        else if (firstOpenBracketIndex < 0) // i.e. option:  val or val : comm
        {
            if (colonIndex<0)
            {
                // no commment in line, option:  val
                if (!FormattingChecker.checkNamedVariableFormat(line)) problem(line);
                namedVar.name = line;
            }
            else
            {
                // comment in line, option:  val : comm
                namedVar.name = line.substring(0,colonIndex).trim();
                if (!FormattingChecker.checkNamedVariableFormat(namedVar.name)) problem(line);
                namedVar.comment = line.substring(colonIndex+1).trim();

            }
        }
        else if (firstOpenBracketIndex>0)
        {
            if (colonIndex<0)
            {
                // no commment in line, option:  val (dim)

                namedVar.name = line.substring(0,firstOpenBracketIndex).trim();
                if (firstClosingBracketIndex < 0) problem(line);
                namedVar.dimension = line.substring(firstOpenBracketIndex+1,firstClosingBracketIndex).trim();
                if (!FormattingChecker.checkNamedVariableFormat(namedVar.name)) problem(line);
                if (!FormattingChecker.checkDimensionFormat(namedVar.dimension)) problem(line);
            }
            else
            {
                // comment in line, option:  val (dim) : comm

                namedVar.name = line.substring(0,firstOpenBracketIndex).trim();
                namedVar.dimension = line.substring(firstOpenBracketIndex+1,firstClosingBracketIndex).trim();

                if (!FormattingChecker.checkNamedVariableFormat(namedVar.name)) problem(line);
                if (!FormattingChecker.checkDimensionFormat(namedVar.dimension)) problem(line);

                namedVar.comment = line.substring(colonIndex +1).trim();
            }

        }
        else
        {
            problem(line);
        }

        // check if it's a range or global variable

        if (FormattingChecker.checkIfVariableIsRange(namedVar.name, myModFile))
        {
            logger.logComment("Looks like variable "+namedVar.name+" is a range variable");
            namedVar.variableType = NamedVariableEntry.RANGE_VARIABLE;
        }
        else if (FormattingChecker.checkIfVariableIsGlobal(namedVar.name, myModFile))
        {
            logger.logComment("Looks like variable "+namedVar.name+" is a global variable");
            namedVar.variableType = NamedVariableEntry.GLOBAL_VARIABLE;
        }
        addItemToInternalInfo(namedVar);
    }

    private void problem(String line)  throws ModFileException
    {
        throw new ModFileException("Problem parsing line in "+this.myType+" block: (" + line +")");
    }

    public boolean addNamedVariable(String name, String dimension, String comment, int variableType)
    {
        if (namedVariableNameAlreadyExists(name)) return false;
        NamedVariableEntry namedVar = new NamedVariableEntry();
        namedVar.name = name;
        if (dimension!=null && !dimension.equals("")) namedVar.dimension = dimension;
        if (comment!=null && !comment.equals("")) namedVar.comment = comment;
        namedVar.variableType = variableType;
        addItemToInternalInfo(namedVar);
        return true;
    }


    private boolean namedVariableNameAlreadyExists(String name)
    {
        Iterator iter = getNamedVariables().iterator();
        while (iter.hasNext())
        {
            try
            {
                NamedVariableEntry namedVar = (NamedVariableEntry) iter.next();
                if (namedVar.name.equals(name)) return true;
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
                NamedVariableEntry namedVar = (NamedVariableEntry) obj;
                String lineToAdd = namedVar.toString();
                correctlyFormattedLines.add(lineToAdd);
            }
            catch (ClassCastException ex)
            {
                correctlyFormattedLines.add((String)obj);
            }

        }
        return formatLines(correctlyFormattedLines);
    }

    public List getNamedVariables()
    {
        List justNamedVariables = new Vector();
        Iterator iter = getInternalInfoIterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            try
            {
                NamedVariableEntry namedVar = (NamedVariableEntry) obj;
                justNamedVariables.add(namedVar);

            }
            catch (ClassCastException ex)
            {
            }
        }
        logger.logComment("Returning info on "+justNamedVariables.size()+" named variables");
        return justNamedVariables;
    }

    public boolean removeNamedVariables(NamedVariableEntry namedVar)
    {
        logger.logComment("internalInfo before: "+ this.toString());
        int indexOfNamedVariables = getIndexInInternalInfo(namedVar);
        if (indexOfNamedVariables<0) return false;

        boolean removeUnit = removeFromInternalInfo(namedVar);
        logger.logComment("internalInfo after: "+ this.toString());
        return removeUnit;
    }


    public class NamedVariableEntry
    {
        public static final int RANGE_VARIABLE = 0;
        public static final int GLOBAL_VARIABLE = 1;
        public static final int GENERAL_VARIABLE = 2;

        public String name = null;
        public String dimension = null;
        public String comment = null;
        public int variableType = GENERAL_VARIABLE;

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(name);
            if (dimension!=null) sb.append(" ("+ dimension + ")");
            if (comment!=null) sb.append(" : "+ comment);
            return sb.toString();
        }
    }



}
