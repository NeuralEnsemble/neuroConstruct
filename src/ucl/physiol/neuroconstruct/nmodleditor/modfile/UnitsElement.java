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

public class UnitsElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("UnitsElement");


    public UnitsElement(ModFileChangeListener changeListener)
    {
        super("UNITS", changeListener);
        logger.setThisClassSilent(true);
    }

    public void addLine(String line)  throws ModFileException
    {
        logger.logComment("----------------------------Checking line: "+ line);
        line = line.trim();
        int equalsIndex;
        if ((equalsIndex = line.indexOf("=")) > 0)
        {
            String bracketedStringBeforeEquals = line.substring(line.indexOf("(")+1, line.indexOf(")")).trim();
            String bracketedStringAfterEquals = line.substring(line.indexOf("(", equalsIndex)+1,line.indexOf(")", equalsIndex)).trim();

            boolean formattedName = FormattingChecker.checkNamedVariableFormat(bracketedStringBeforeEquals);
            boolean formattedDimension = FormattingChecker.checkDimensionFormat(bracketedStringAfterEquals);

            if (!(formattedDimension && formattedName)) problem(line);
            String comment = null;
            int colonIndex;
            if ((colonIndex = line.indexOf(":"))>0)
            {
                comment = line.substring(colonIndex+1).trim();
                addUnit(bracketedStringBeforeEquals, bracketedStringAfterEquals, comment);
            }
            else addUnit(bracketedStringBeforeEquals, bracketedStringAfterEquals);
        }
        else addItemToInternalInfo(line); // comment line or space...
    }


    private void problem(String line)  throws ModFileException
    {
        throw new ModFileException("Problem parsing line in "+this.myType+" block: (" + line +")");
    }


    public boolean addUnit(String shortName, String realName)   throws ModFileException
    {
        if (unitShortNameAlreadyExists(shortName)) return false;
        UnitEntry unit = new UnitEntry();

        boolean formattedName = FormattingChecker.checkNamedVariableFormat(shortName);
        boolean formattedDimension = FormattingChecker.checkDimensionFormat(realName);
        if (!(formattedDimension && formattedName)) throw new ModFileException("Error in names of fields");

        unit.simplifiedName = shortName;
        unit.realUnits = realName;
        addItemToInternalInfo(unit);
        return true;
    }

    public boolean addUnit(String shortName, String realName, String comment) throws ModFileException
    {
        if (unitShortNameAlreadyExists(shortName)) return false;
        UnitEntry unit = new UnitEntry();

        boolean formattedName = FormattingChecker.checkNamedVariableFormat(shortName);
        boolean formattedDimension = FormattingChecker.checkDimensionFormat(realName);
        if (!(formattedDimension && formattedName)) throw new ModFileException("Error in names of fields");

        unit.simplifiedName = shortName;
        unit.realUnits = realName;
        unit.commentOnUnit = comment;
        addItemToInternalInfo(unit);
        return true;
    }


    private boolean unitShortNameAlreadyExists(String shortName)
    {
        Iterator iter = getUnits().iterator();
        while (iter.hasNext())
        {
            try
            {
                UnitEntry unit = (UnitEntry) iter.next();
                if (unit.simplifiedName.equals(shortName)) return true;
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
            logger.logComment("Looking at info object: "+ obj);

            try
            {
                UnitEntry unit = (UnitEntry) obj;
                String lineToAdd = unit.toString();
                correctlyFormattedLines.add(lineToAdd);
            }
            catch (ClassCastException ex)
            {
                logger.logComment("Not a unit, other line...");
                correctlyFormattedLines.add((String)obj);
            }

        }

        return formatLines(correctlyFormattedLines);
    }

    public List getUnits()
    {
        List justUnits = new Vector();
        Iterator iter = getInternalInfoIterator();
        while (iter.hasNext())
        {
            Object obj = iter.next();
            try
            {
                UnitEntry unit = (UnitEntry) obj;
                justUnits.add(unit);

            }
            catch (ClassCastException ex)
            {
            }
        }
        logger.logComment("----------  Returning info on "+justUnits.size()+" units");
        return justUnits;
    }

    public boolean removeUnit(UnitEntry unit)
    {
        logger.logComment("internalInfo before: "+ this.toString());
        int indexOfUnit = getIndexInInternalInfo(unit);
        if (indexOfUnit<0) return false;
        boolean removeUnit = removeFromInternalInfo(unit);
        logger.logComment("internalInfo after: "+ this.toString());
        return removeUnit;
    }


    public class UnitEntry
    {
        public String simplifiedName;
        public String realUnits;
        public String commentOnUnit = null;

        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("("+simplifiedName+") = ("+realUnits+")");
            if (commentOnUnit!=null) sb.append(" : "+ commentOnUnit);
            return sb.toString();
        }
    }



}
