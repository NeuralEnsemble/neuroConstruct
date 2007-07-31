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
 * @version 1.0.4
 */

public class NeuronElement extends ModFileBlockElement
{
    ClassLogger logger = new ClassLogger("NeuronElement");

    public static final int POINT_PROCESS = 0;
    public static final int DENSITY_MECHANISM = 1;

    public static final int NO_CURRENT_REFERENCED = 2;
    public static final int NONSPECIFIC_CURRENT = 3;
    public static final int ELECTRODE_CURRENT = 4;

    private String myProcessName = null;
    private int myProcess = POINT_PROCESS;

    private String myCurrentVariable = null;
    private int myCurrentReference = NO_CURRENT_REFERENCED;

    public VariableHolder myRangeVariables = new VariableHolder("RANGE");
    public VariableHolder myGlobalVariables = new VariableHolder("GLOBAL");

    private String patternToSplitBy = "[\\s,]";

    public NeuronElement(ModFileChangeListener changeListener)
    {
        super("NEURON", changeListener);
        logger.setThisClassSilent(true);
    }

    /**
     * The name to use beside SUFFIX or POINT_PROCESS
     * @return The process name
     */
    public String getProcessName()
    {
        return myProcessName;

    }

    /**
     * The name to use beside SUFFIX or POINT_PROCESS
     * @param name the new process name
     * @throws ModFileException if the name is badly formateed
     */
    public void setProcessName(String name) throws ModFileException
    {
        if (!FormattingChecker.checkGeneralNameFormat(name))
        throw new ModFileException("Bad process name");
        myProcessName = name;
    }

    /**
     * The name to use beside NONSPECIFIC_CURRENT or ELECTRODE_CURRENT
     * @return The current variable (usually just i)
     */
    public String getCurrentVariable()
    {
        return myCurrentVariable;

    }

    /**
     * The name to use beside NONSPECIFIC_CURRENT or ELECTRODE_CURRENT
     * @param name The new current variable name
     */
    public void setCurrentVariable(String name) throws ModFileException
    {
        if (!FormattingChecker.checkGeneralNameFormat(name))
        throw new ModFileException("Bad current variable name");
        myCurrentVariable = name;
    }

    /**
     * The process type: POINT_PROCESS or DENSITY_MECHANISM
     */
    public void setProcess(int process)
    {
        myProcess = process;
    }

    /**
     * The process type: POINT_PROCESS or DENSITY_MECHANISM
     */
    public int getProcess()
    {
        return myProcess;
    }


    /**
     * NONSPECIFIC_CURRENT or ELECTRODE_CURRENT
     */
    public void setCurrentReference(int currRef)
    {
        myCurrentReference = currRef;
    }

    /**
     * NONSPECIFIC_CURRENT or ELECTRODE_CURRENT
     */
    public int getCurrentReference()
    {
        return myCurrentReference;
    }



    public void addLine(String line) throws ModFileException
    {
        logger.logComment("Checking line: (" + line + ")");
        String originalLine = new String(line);
        line = line.trim();

        if (line.startsWith("SUFFIX"))
        {
            myProcess = DENSITY_MECHANISM;
            String myProcessNameTemp = line.substring("SUFFIX ".length());
            if (!FormattingChecker.checkGeneralNameFormat(myProcessNameTemp))
                problem(originalLine);
            myProcessName = myProcessNameTemp;
            return;
        }
        else if (line.startsWith("POINT_PROCESS"))
        {
            myProcess = POINT_PROCESS;
            String myProcessNameTemp = line.substring("POINT_PROCESS ".length());
            if (!FormattingChecker.checkGeneralNameFormat(myProcessNameTemp))
                problem(originalLine);
            myProcessName = myProcessNameTemp;
            return;
        }
        else if(line.startsWith("RANGE"))
        {
            String[] rangeVariables = line.split(patternToSplitBy);
            boolean rangeHolderInInternalInfo = checkItemInInternalInfo(myRangeVariables);

            for (int i = 1; i < rangeVariables.length; i++) // i=1 to remove RANGE
            {
                myRangeVariables.addVariableName(rangeVariables[i]);
            }
            if (!rangeHolderInInternalInfo)
            {
                addItemToInternalInfo(myRangeVariables);
            }
        }
        else if(line.startsWith("GLOBAL"))
        {
            String[] globalVariables = line.split(patternToSplitBy);
            boolean globalHolderInInternalInfo = checkItemInInternalInfo(myGlobalVariables);

            for (int i = 1; i < globalVariables.length; i++) // i=1 to remove GLOBAL
            {
                myGlobalVariables.addVariableName(globalVariables[i]);
            }
            if (!globalHolderInInternalInfo)
            {
                addItemToInternalInfo(myGlobalVariables);
            }

        }
        else if(line.startsWith("USEION"))
        {
            /** @todo support USEION... */
            addItemToInternalInfo(line);
        }
        else if(line.startsWith("NONSPECIFIC_CURRENT"))
        {
            /** @todo support multiple NONSPECIFIC_CURRENT... */

            String myCurrentVariableTemp = line.substring("NONSPECIFIC_CURRENT".length()).trim();
            if (!FormattingChecker.checkGeneralNameFormat(myCurrentVariableTemp))
                problem(originalLine);
            myCurrentVariable = myCurrentVariableTemp;
            myCurrentReference = NONSPECIFIC_CURRENT;

            /** @todo Ensure this is always the case... */
            myRangeVariables.addVariableName(myCurrentVariable);

        }
        else if(line.startsWith("ELECTRODE_CURRENT"))
        {
            /** @todo support multiple ELECTRODE_CURRENT... */

            String myCurrentVariableTemp = line.substring("ELECTRODE_CURRENT".length()).trim();
            if (!FormattingChecker.checkGeneralNameFormat(myCurrentVariableTemp))
                problem(originalLine);
            myCurrentVariable = myCurrentVariableTemp;
            myCurrentReference = ELECTRODE_CURRENT;

            /** @todo Ensure this is always the case... */
            myRangeVariables.addVariableName(myCurrentVariable);
        }
        else if(line.startsWith("ARTIFICIAL_CELL"))
        {
            problem(  line,"ARTIFICIAL_CELL is not supported in this version of neuroConstruct");
        }


        else if(line.startsWith(":"))
        {
            /** @todo support comments... */
        }
        else if(line.length()==0)
        {
           // ignore...
        }




        else problem(originalLine,"Unknown parameter at start of line");


    }


    private void problem(String line, String error) throws ModFileException
    {
        throw new ModFileException("Problem: "+error+"\nAt line: "+ line);
    }

    private void problem(String line) throws ModFileException
    {
        problem(line, "Error when parsing.");
    }

    public String toString()
    {
        Vector correctlyFormattedLines = new Vector();
        if (myProcess==POINT_PROCESS)
        {
            correctlyFormattedLines.add("POINT_PROCESS "+ myProcessName);
        }
        else if (myProcess==DENSITY_MECHANISM)
        {
            correctlyFormattedLines.add("SUFFIX "+ myProcessName);
        }

        if (myCurrentReference==ELECTRODE_CURRENT)
        {
            correctlyFormattedLines.add("ELECTRODE_CURRENT "+ myCurrentVariable);
        }
        else if (myCurrentReference==NONSPECIFIC_CURRENT)
        {
            correctlyFormattedLines.add("NONSPECIFIC_CURRENT "+ myCurrentVariable);
        }



        Iterator intInfo = getInternalInfoIterator();
        while (intInfo.hasNext())
        {
            Object obj = intInfo.next();
            try
            {
                VariableHolder vars = (VariableHolder) obj;
                String lineToAdd = vars.toString();
                correctlyFormattedLines.add(lineToAdd);
            }
            catch (ClassCastException ex)
            {
                correctlyFormattedLines.add( (String) obj);
            }

        }
        return formatLines(correctlyFormattedLines);
    }


    public boolean isRangeVariable(String variableName)
    {
        return myRangeVariables.contains(variableName);
    }

    public boolean isGlobalVariable(String variableName)
    {
        return myGlobalVariables.contains(variableName);
    }

    /**
     * This will be needed when a new variable is added via the tab for PARAMETERS or ASSSIGNED
     */
    public void addRangeVariable(String variableName)
    {
        logger.logComment("Adding variable named: "+variableName+" to "+myRangeVariables.getVariableNames().size()+" current variables");
        myRangeVariables.addVariableName(variableName);
    }

    /**
     * This will be needed when a new variable is added via the tab for PARAMETERS or ASSSIGNED
     */
    public void addGlobalVariable(String variableName)
    {
        myGlobalVariables.addVariableName(variableName);
    }

    /**
     * This will be needed when a variable is removed via the tab for PARAMETERS or ASSSIGNED
     */
    public void removeRangeVariable(String variableName)
    {
        myRangeVariables.removeVariableName(variableName);
    }

    /**
     * This will be needed when a variable is removed via the tab for PARAMETERS or ASSSIGNED
     */
    public void removeGlobalVariable(String variableName)
    {
        myGlobalVariables.removeVariableName(variableName);
    }


    public class VariableHolder
    {
        String variableType = null;
        public Vector myVariableNames = new Vector();

        private VariableHolder(){};

        public VariableHolder(String type)
        {
            variableType = type;
        }

        public List getVariableNames()
        {
            logger.logComment("Returning list of "+myVariableNames.size()+" variables of type: "+ variableType);
            return myVariableNames;
        }

        public void addVariableName(String name)
        {
            if (!myVariableNames.contains(name))
            {
                if (name.length() > 0)
                    myVariableNames.add(name);
            }
        }

        public void removeVariableName(String name)
        {
            myVariableNames.remove(name);
        }


        public boolean contains(String name)
        {
            return myVariableNames.contains(name);
        }

        public String toString()
        {
            StringBuffer sb = new StringBuffer();

            sb.append(variableType+" ");
            Iterator iter = myVariableNames.iterator();
            boolean firstEntry = true;
            while (iter.hasNext())
            {
                if (!firstEntry) sb.append(", ");
                firstEntry = false;
                String nextVariableName = (String)iter.next();
                sb.append(nextVariableName);
                //if (isRangeVariable(nextVariableName)) sb.append(" (range) ");
                //if (isGlobalVariable(nextVariableName)) sb.append(" (global) ");
            }
            return sb.toString();
        }
    }




}
