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

package ucl.physiol.neuroconstruct.neuron;

import java.io.*;
import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.converters.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.utils.equation.Variable;
import ucl.physiol.neuroconstruct.utils.equation.Expression;
import ucl.physiol.neuroconstruct.utils.equation.EquationUnit;
import ucl.physiol.neuroconstruct.utils.equation.*;

/**
 *
 * A class for converting NEURON (*.hoc, *.nrn, etc.) files into more explicit representations
 * of the sections/connections present. This is the first step before using NeuronMorphReader
 * to actually create a Cell object with the file
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 *
 */


public class NeuronFileConverter
{
    private static ClassLogger logger = new ClassLogger("NeuronFileConverter");

    Vector fscanData = new Vector();

    int indexOfNextFscan = 0;

    Hashtable allProcedures = new Hashtable();
    Hashtable globalVariables = new Hashtable();

    /**
     * Default constructor
     */
    public NeuronFileConverter()
    {
        //logger.setThisClassSilent(true);
    }

    /**
     * Main method to call, converts the neuFile into a string with the loops and
     * comments removed, the fscan data put in the corect place, and the procedures
     * called
     */
    public String convertNeuronFile(File neuFile) throws NeuronException, IOException
    {
        String commentless = trimComments(neuFile);

        logger.logComment("Commentless: ");
        //printLines(commentless);

        String fscanless = removeFscanData(commentless);

        logger.logComment("fscanless: ");
        //printLines(fscanless);

        String forLoopless = removeForLoops(fscanless);
        logger.logComment("forLoopless: ");
        ///printLines(forLoopless);

        String procedures = parseIntoProcedures(forLoopless);
        logger.logComment("procedures: ");

        String connectEnded = putConnectsAtEnd(procedures);
        logger.logComment("connectEnded: ");
 //       printLines(connectEnded);


        logger.logComment("Number of fscan entries found: "+ fscanData.size());
        logger.logComment("Number of fscan entries used: "+ indexOfNextFscan);

        logger.logComment("Procedures found: "+ allProcedures);


        return connectEnded;
    }


    /**
     * Takes away all lines starting with //
     * and all comments enclosed in /* */
     /*
     */
    private String trimComments(File neuFile) throws NeuronException, IOException
    {
        Reader in = new FileReader(neuFile);
        BufferedReader lineReader = new BufferedReader(in);

        String nextLine = null;
        int lineCount = 0;

        StringBuffer sb = new StringBuffer();

        boolean insideComment = false;

        while ( (nextLine = lineReader.readLine()) != null)
        {
            String originalLine =  new String(nextLine);
            lineCount++;

            //logger.logComment("Line "+ lineCount +" ("+ nextLine+")"
            //                  + ", len: "+nextLine.length() + ", trimmed: "+nextLine.trim().length());

            nextLine = nextLine.trim();

            if (nextLine.length() == 0)
            {
                 //logger.logComment("Empty line...");
            }
            else
            {
                if (nextLine.indexOf("//") >= 0)
                {
                    nextLine = nextLine.substring(0, nextLine.indexOf("//"));
                }

                StringBuffer usableBit = new StringBuffer();

                for (int i = 0; i < nextLine.length(); i++)
                {
                    if ((i < nextLine.length()-1) && nextLine.substring(i, i + 2).equals("/*"))
                    {
                        i++;
                        if (insideComment)
                        {
                            throw new NeuronException(neuFile.getAbsolutePath(),
                                                                "Problem with commenting in line: " + originalLine);
                        }
                        insideComment = true;
                    }
                    else if ((i < nextLine.length()-1) && nextLine.substring(i, i + 2).equals("*/"))
                    {
                        i++;
                        if (!insideComment)
                        {
                            throw new NeuronException(neuFile.getAbsolutePath(),
                                                                "Problem with commenting in line: " + originalLine);
                        }
                        insideComment = false;
                   }
                   else if (!insideComment)
                   {
                       usableBit.append(nextLine.charAt(i));
                   }

                }
                nextLine = usableBit.toString().trim();

                if(nextLine.length()>0)
                {
                    if (nextLine.charAt(nextLine.length()-1)=='\\')
                    {
                        nextLine = nextLine.substring(0, nextLine.length()-1);
                        sb.append(nextLine+" ");
                    }
                    else
                    {
                        sb.append(nextLine + "\n");
                    }
                }
            }
        }

        return sb.toString();
    }


   /**
    * Assumes all lines starting with a float are fscan data, so takes those lines out...
    */
    private String removeFscanData(String allLines) throws NeuronException
    {
        String[] lines = allLines.split("\\n+");
        StringBuffer sb = new StringBuffer();

        for (int j = 0; j < lines.length; j++)
        {
            String originalLine =  new String(lines[j]);

            if (lines[j].trim().endsWith(":"))
            {
                // something like SOMA COORDINATES AND DIAMETERS: so ignore...
            }
            else
            {
                String[] words = lines[j].split("\\s+");
                try
                {
                    Float.parseFloat(words[0]);
                    for (int i = 0; i < words.length; i++)
                    {
                        try
                        {
                            float f = Float.parseFloat(words[i]);
                            fscanData.add(new Float(f));
                        }
                        catch (NumberFormatException e)
                        {
                            logger.logError("Error reading part of line: "+ originalLine, e);
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    // doesn't begin with a float, so add to the return string...
                    sb.append(originalLine+"\n");
                }

            }

        }
        //logger.logComment("fscan() data: " + fscanData);
        return sb.toString();
    }

    /**
     * Removes the for loops, replacing the lines inside with the value of the
     * variable at each step
     */
    private String removeForLoops(String allLines) throws NeuronException, IOException
    {
        String[] lines = allLines.split("\\n+");
        StringBuffer sb = new StringBuffer();


        for (int currLineNum = 0; currLineNum < lines.length; currLineNum++)
        {
            String originalLine = new String(lines[currLineNum]);
            logger.logComment("removeForLoops:                          Looking at line: "+ originalLine);

            if (lines[currLineNum].indexOf("=")>0)
            {
                String before = lines[currLineNum].substring(0,lines[currLineNum].indexOf("=")).trim();
                String after = lines[currLineNum].substring(lines[currLineNum].indexOf("=")+1).trim();

                logger.logComment("before: " + before+", after: "+ after);

                try
                {
                    float val = Float.parseFloat(after);
                    if (val == (int)val) globalVariables.put(before, new Integer((int)val));
                    else globalVariables.put(before, new Float(val));

                    logger.logComment("Recording global var: "+ before+ ", value: "+ val);
                }
                catch (Exception ex)
                {
                    logger.logComment("Wasn't a global var after all!");
                    // ignore...
                }
            }

            logger.logComment("globalVariables: " + globalVariables);

            Enumeration globalVarNames = globalVariables.keys();

            while (globalVarNames.hasMoreElements())
            {
                String globalVarName = (String)globalVarNames.nextElement();
                if (lines[currLineNum].indexOf("["+globalVarName+"]")>0)
                {
                    logger.logComment("Found possible global variable: "+globalVarName);
                    Object value = globalVariables.get(globalVarName);
                    lines[currLineNum] = GeneralUtils.replaceToken(lines[currLineNum],
                                 "["+globalVarName+"]",
                                 "["+value+"]",
                                 0);
                    logger.logComment("Line now: "+ lines[currLineNum]);
                }
            }
            //

            while (lines[currLineNum].indexOf("fscan()")>0)
            {
                lines[currLineNum] = GeneralUtils.replaceToken(lines[currLineNum], "fscan()", "" + getFscanValueAt(indexOfNextFscan), 0);
                indexOfNextFscan++;
            }

            logger.logComment("lines[currLineNum]: " + lines[currLineNum]);

            if (lines[currLineNum].startsWith("for "))
            {
                logger.logComment("For loop start line: " + lines[currLineNum]);

                try
                {
                    ForLoop currForLoop = new ForLoop(lines[currLineNum]);

                    StringBuffer forLoopLines = new StringBuffer();

                    int bracketCount = currForLoop.bracketCount;

                    if (currForLoop.extraBit.trim().length() > 0) forLoopLines.append(currForLoop.extraBit + "\n");

                    while (bracketCount > 0)
                    {
                        currLineNum++;
                        //logger.logComment("Checking line: ("+lines[currLineNum]+") in for loop: "+currForLoop);
                        if (lines[currLineNum].indexOf("{") >= 0) bracketCount++;
                        if (lines[currLineNum].indexOf("}") >= 0) bracketCount--;

                        if (bracketCount > 0)
                        {
                            logger.logComment("Adding line to forLoopLines: " + lines[currLineNum]);
                            forLoopLines.append(lines[currLineNum] + "\n");
                        }
                    }

                    logger.logComment("forLoopLines: " + forLoopLines);

                    sb.append(parseSingleForLoop(currForLoop, forLoopLines.toString()) + "\n");
                }
                catch (NumberFormatException ex)
                {
                    sb.append(lines[currLineNum]+ "  // Unable to parse this for loop!! Hopefully it will be removed when procs parsed!!\n");
                }
            }
            else
            {
                sb.append(lines[currLineNum]+"\n");
            }

        }
        return sb.toString();
    }

/*
    private String replaceAccess(String allLines) throws NeuronException
    {
        String[] lines = allLines.split("\\n+");
        StringBuffer sb = new StringBuffer();

        for (int currLineNum = 0; currLineNum < lines.length; currLineNum++)
        {
            String originalLine = new String(lines[currLineNum]);
            logger.logComment("Looking at line: " + originalLine);

        }
        return sb.toString();
    }
*/

    /**
     * extracts the definitions of procedures from the lines, and evaluates them when they
     * are explicitly called
     */
    private String parseIntoProcedures(String allLines) throws NeuronException
    {
        String[] linesOld = allLines.split("\\n+");
        StringBuffer sbBracket = new StringBuffer();

        for (int currLineNum = 0; currLineNum < linesOld.length; currLineNum++)
        {
            String trimmed = linesOld[currLineNum].trim();

            trimmed = GeneralUtils.replaceAllTokens(trimmed, "{", "{\n");
            trimmed = GeneralUtils.replaceAllTokens(trimmed, "}", "\n}\n");


            trimmed = GeneralUtils.replaceAllTokens(trimmed, "pt3dadd", "\npt3dadd");  // when 3d info all in one line...

            sbBracket.append(trimmed+"\n");

        }
        String[] lines = sbBracket.toString().split("\\n+");
        StringBuffer sb = new StringBuffer();

        boolean parsingTemplate = false;


        for (int currLineNum = 0; currLineNum < lines.length; currLineNum++)
        {
            String originalLine = new String(lines[currLineNum]);
            logger.logComment("  Looking at line: "+ originalLine);


            if (lines[currLineNum].trim().startsWith("{") &&
                lines[currLineNum].trim().endsWith("}"))
            {
                lines[currLineNum] = lines[currLineNum].trim();
                lines[currLineNum] = lines[currLineNum].substring(1, lines[currLineNum].length()-1);

                logger.logComment("Line trimmed to: " + lines[currLineNum]);
            }

            if (lines[currLineNum].trim().length()==0)
            {
                // ignore...
            }
            else if (lines[currLineNum].trim().startsWith("begintemplate"))
            {
                GuiUtils.showInfoMessage(logger, "Parsing a template",

                                         "<html>The line: " + lines[currLineNum] +
                                         " indicates that this morphology file is a cell template.\n" +
                                         "Note that in this case the structure of the file is assumed to be similar to the output of \n"
                                         + "Cell Builder. Lines starting with create will be included in the simplified version and lines inside:\n"
                                         + "topol()\n"
                                         + "will be evaluated at the end of the template", null);

                parsingTemplate = true;
            }
            else if (lines[currLineNum].trim().startsWith("endtemplate"))
            {
                parsingTemplate = false;

                sb.append("\n");
                sb.append(evaluateProcedureCall("topol()", allProcedures) + "\n");

                //sb.append(evaluateProcedureCall(lines[currLineNum], allProcedures) + "\n");
            }
            else if (lines[currLineNum].indexOf("proc ") >= 0)
            {
                logger.logComment("Proc header: " + lines[currLineNum]);

                String procName = lines[currLineNum].substring("proc ".length(), lines[currLineNum].indexOf('(')).
                    trim();

                int bracketCount = 1;
                StringBuffer procedureLines = new StringBuffer();

                while (bracketCount > 0)
                {
                    currLineNum++;
                    if (lines[currLineNum].indexOf("{") >= 0) bracketCount++;
                    if (lines[currLineNum].indexOf("}") >= 0) bracketCount--;

                    if (bracketCount > 0) procedureLines.append(lines[currLineNum] + "\n");
                }

                Procedure proc = new Procedure(procName, procedureLines.toString(), allProcedures);

                if (allProcedures.containsKey(procName))
                {
                    Procedure oldProc = (Procedure) allProcedures.get(procName);
                    allProcedures.remove(procName);
                    allProcedures.put(procName + "_OLD", oldProc);
                }
                allProcedures.put(procName, proc);

            }
            else if (lines[currLineNum].indexOf("(") >= 0 &&
                     lines[currLineNum].indexOf("connect ") < 0 &&
                     lines[currLineNum].indexOf("pt3dadd") < 0)     // procedure being called
            {

                sb.append(evaluateProcedureCall(lines[currLineNum], allProcedures) + "\n");
            }
            else if (!parsingTemplate || lines[currLineNum].indexOf("create") >= 0)
            {
                sb.append(lines[currLineNum] + "\n");
            }

        }
        return sb.toString();
    }


    /**
     * When a procedure is called this function will replace the line calling it
     * with the contents of the procedure
     */
    private static String evaluateProcedureCall(String procedureCallLine, Hashtable procedures) throws NeuronException
    {
        StringBuffer sb = new StringBuffer();

        logger.logComment("procedureCallLine: " + procedureCallLine);

        String possProcedureName = procedureCallLine.substring(0, procedureCallLine.indexOf("(")).trim();

        if (possProcedureName.equals("pt3dclear") ||
            possProcedureName.equals("pt3dadd") ||
            possProcedureName.equals("if") ||
            possProcedureName.equals("define_shape") ||
            possProcedureName.equals("load_file"))
        {
            logger.logComment("This is a standard proc, will be put in as is");
            sb.append(procedureCallLine + "\n");
        }
        else
        {
            Procedure proc = (Procedure) procedures.get(possProcedureName);

            if (proc == null) throw new NeuronException("Problem with line: "
                                                       + procedureCallLine
                                                       + "\nProcedure "
                                                       + possProcedureName
                                                       + " not found...");

            String procedureParamLine = procedureCallLine.substring(procedureCallLine.indexOf("(") + 1,
                                                                    procedureCallLine.indexOf(")")).trim();
            String[] params = procedureParamLine.split(",");

            sb.append(proc.evaluate(params) + "\n");
        }

        return sb.toString();

    }


    /**
     * Takes in the definition of the for loop, and returns the lines it contains,
     * evaluated the proper number of times.
     */
    private String parseSingleForLoop(ForLoop forLoop, String forLoopLines)
    {
        StringBuffer sb = new StringBuffer();

        for (int currLoopIndex = forLoop.start; currLoopIndex <= forLoop.end; currLoopIndex++)
        {
           String[] lines = forLoopLines.split("\\n+");

           for (int forLoopLineNum = 0; forLoopLineNum < lines.length; forLoopLineNum++)
           {
               lines[forLoopLineNum] = lines[forLoopLineNum].trim();

               if (lines[forLoopLineNum].indexOf(forLoop.variableName) > 0)
               {
                   int indexToCheckFrom = 0;
                   int varIndex = 0;
                   while ( (varIndex = lines[forLoopLineNum].indexOf(forLoop.variableName, indexToCheckFrom)) > 0)
                   {
                       logger.logComment("Found instance of " + forLoop.variableName + " at pos " + varIndex + " in "
                                         +lines[forLoopLineNum]+", currLoopIndex: "+currLoopIndex+", indexToCheckFrom: "+indexToCheckFrom);

                       if (lines[forLoopLineNum].charAt(varIndex - 1) == '['
                           || (lines[forLoopLineNum].charAt(varIndex - 1) == ' ' &&
                               ((varIndex+forLoop.variableName.length())>=lines[forLoopLineNum].length() ||
                                lines[forLoopLineNum].charAt(varIndex + 1) == ' ')))
                       {
                           int beforeBracket = lines[forLoopLineNum].substring(0,varIndex).lastIndexOf("[");
                           int afterBracket = lines[forLoopLineNum].substring(varIndex+1).indexOf("]") + varIndex+1;

                           String insideBrackets = lines[forLoopLineNum].substring(beforeBracket+1,afterBracket);

                           logger.logComment("insideBrackets: " + insideBrackets);

                           Variable i = new Variable(forLoop.variableName);

                           try
                           {
                               EquationUnit func = Expression.parseExpression(insideBrackets, new Variable[]
                                                                              {i});

                               Argument[] i0 = new Argument[]{new Argument(i.getName(), currLoopIndex)};

                               double eval = func.evaluateAt(i0);

                               String replace = eval+"";
                               if (eval==(int)eval) replace = (int)eval+""; // makes 1.0 to 1

                               lines[forLoopLineNum] = lines[forLoopLineNum].substring(0, beforeBracket + 1)
                                   + replace
                                   + lines[forLoopLineNum].substring(afterBracket);

                           }
                           catch (EquationException ex)
                           {
                               logger.logComment("Unable to parse expression: " + insideBrackets + " in terms of var " + i);
                               String replaced = GeneralUtils.replaceAllTokens(insideBrackets, forLoop.variableName, "" + currLoopIndex);
                               lines[forLoopLineNum] = lines[forLoopLineNum].substring(0, beforeBracket + 1)
                                   + replaced
                                   + lines[forLoopLineNum].substring(afterBracket);

                           }

                           //lines[forLoopLineNum] = GeneralUtils.replaceToken(lines[forLoopLineNum], forLoop.variableName, "" + currLoopIndex,
                           //                                                  indexToCheckFrom);

                           logger.logComment("Changed line to: " + lines[forLoopLineNum]);
                       }
                       else
                       {
                           logger.logComment("Not changing line: "+ lines[forLoopLineNum]);
                       }

                       indexToCheckFrom = varIndex + forLoop.variableName.length();
                   }
               }

               while (lines[forLoopLineNum].indexOf("fscan()") > 0)
               {
                   lines[forLoopLineNum] = GeneralUtils.replaceToken(lines[forLoopLineNum], "fscan()", "" + getFscanValueAt(indexOfNextFscan),0);
                   indexOfNextFscan++;
               }

               if (lines[forLoopLineNum].startsWith("for "))
               {
                   ForLoop internalForLoop = new ForLoop(lines[forLoopLineNum]);
                   StringBuffer internalForLoopLines = new StringBuffer();

                   int bracketCount = 1;
                   while (bracketCount > 0)
                   {
                       forLoopLineNum++;
                       //logger.logComment("Checking line: (" + lines[forLoopLineNum] + ") in for loop: " +internalForLoop);

                       if (lines[forLoopLineNum].indexOf("{") >= 0) bracketCount++;
                       if (lines[forLoopLineNum].indexOf("}") >= 0) bracketCount--;

                       if (bracketCount > 0) internalForLoopLines.append(lines[forLoopLineNum] + "\n");
                   }

                   sb.append(parseSingleForLoop(internalForLoop, internalForLoopLines.toString()) +"\n");

               }
               else
               {
                   sb.append(lines[forLoopLineNum] + "\n");
                   //logger.logComment("Added line: "+ lines[forLoopLineNum]);
               }

           }
        }
        return sb.toString();
    }


    /**
     * Ensures all connect hoc calls are at the end of the file
     * This enables all Segments to be created before the parents are specified
     */
    private String putConnectsAtEnd(String allLines) throws NeuronException
    {
        String[] lines = allLines.split("\\n+");
        StringBuffer sb = new StringBuffer();

        Vector connectLines = new Vector();

        for (int currLineNum = 0; currLineNum < lines.length; currLineNum++)
        {
            String originalLine = new String(lines[currLineNum]);
            logger.logComment("Looking at line: " + originalLine);

            if (lines[currLineNum].startsWith("connect "))
            {
                connectLines.add(lines[currLineNum]);
            }
            else if (lines[currLineNum].split("\\s+").length>2 &&
                     lines[currLineNum].split("\\s+")[1].equals("connect"))
            {
                connectLines.add(lines[currLineNum]);
            }
            else
            {
                sb.append(lines[currLineNum] + "\n");
            }

        }
        for (int i = 0; i < connectLines.size(); i++)
        {
            sb.append(connectLines.elementAt(i) + "\n");
        }

        return sb.toString();
    }



    private String getFscanValueAt(int index)
    {
        Float f = (Float)fscanData.elementAt(index);

        logger.logComment("Was asked for FscanValueAt : "+ index+", it was: "+f);

        if (f.intValue() == f.floatValue()) return ""+f.intValue();
        else return ""+f.floatValue();
    }



    private void printLines(String lines)
    {
        System.out.println("-------------------------------------------------------");
        System.out.println(lines);
        System.out.println("-------------------------------------------------------");

    }





    /**
     * Class to store info on for loop encountered in original file
     */
    public class ForLoop
    {
        String originalLine = null;
        String variableName = null;

        String extraBit = "";
        int start = 0;
        int end = 0;

        int bracketCount = 0;

        public ForLoop(String line) throws NumberFormatException
        {
            logger.logComment("For loop being created with init line: " + line);

            variableName = line.substring("for ".length(), line.indexOf(" ", "for ".length() + 1));

            start = Integer.parseInt(line.substring(line.indexOf("=") + 1, line.indexOf(",")).trim());

            String rest = line.substring(line.indexOf(",") + 1);

            logger.logComment("Rest of line: " + rest);

            String[] items = rest.trim().split("\\s+");

            end = Integer.parseInt(items[0]);   //Integer.parseInt(line.substring(line.indexOf(",") + 1, line.indexOf("{")).trim());

            if (!(items.length==2 && items[1].equals("{")))
            {
                if (items[1].equals("{"))
                {
                    extraBit = line.substring(line.indexOf("{") + 1);
                    bracketCount = 1;
                }
                else extraBit = line.substring(line.indexOf(items[0])+items[0].length());
            }
            else
            {
                bracketCount = 1;
            }
        }

        public String toString()
        {
            return "ForLoop: "+ variableName +" ("+ start+" -> "+end+")";
        }
    }


    /**
     * Class to store info on procedure encountered in original file
     */
    public class Procedure
    {
        String name = null;
        String lines = null;
        Hashtable otherProcedures = null;

        public Procedure(String name, String lines, Hashtable otherProcedures)
        {
            this.name = name;
            this.lines = lines;
            this.otherProcedures = otherProcedures;

        }

        public String evaluate(String[] args) throws NeuronException
        {
            //logger.logComment("Evaluating procedure with "+ args.length +" args");
            String[] individualLines = lines.split("\\n+");
            StringBuffer sb = new StringBuffer();


            for (int currLineNum = 0; currLineNum < individualLines.length; currLineNum++)
            {
                String originalLine = new String(individualLines[currLineNum]);
                //logger.logComment("Evaluating proc line: " + originalLine);
                if (args!=null && args.length>0)
                {
                    for (int i = 0; i < args.length; i++)
                    {
                         while(individualLines[currLineNum].indexOf("$"+(i+1))>0)
                         {
                             individualLines[currLineNum] = GeneralUtils.replaceToken(individualLines[currLineNum],
                                                                         "$"+(i+1),
                                                                         args[i].trim(),
                                                                         0);
                         }
                    }

                    //logger.logComment("New line: " + individualLines[currLineNum]);
                }
                String possProcName = null;
                if (individualLines[currLineNum].indexOf("(")>0)
                {
                    possProcName = individualLines[currLineNum].substring(0, individualLines[currLineNum].indexOf("(")).trim();
                }
                if (possProcName!=null && otherProcedures.containsKey(possProcName))
                {
                    //logger.logComment("This procedure is calling: "+ );

                    sb.append(NeuronFileConverter.evaluateProcedureCall(individualLines[currLineNum], otherProcedures));
                }
                else
                {
                    sb.append(individualLines[currLineNum] + "\n");
                }
            }
            return sb.toString();
            //return lines;
        }

        public String toString()
        {
            String[] individualLines = lines.split("\\n");
            return "Procedure: "+ name +"() which has "+individualLines.length+" lines, first: "+individualLines[0];
        }
    }

    public static void main(String[] args)
    {
        //File neuronFile = new File("Y:\\Padraig\\Datas\\neurolucida\\Reconstruction\\231198B1corrected.nrn");

        //File neuronFile = new File("C:\\Documents and Settings\\padraig\\Desktop\\Datas\\Datas\\ub\\simp.hoc");
        //File neuronFile = new File("C:\\Documents and Settings\\padraig\\Desktop\\Datas\\Datas\\ub\\llvpc_nC_fixed.hoc");

        File neuronFile = new File("C:\\neuroConstruct\\projects\\Project_1ndghhg\\generatedNEURON\\SampleCell.hoc");

        //File neuronFile = new File("C:\\neuroConstruct\\projects\\Project_1ndghhg\\simulations\\Sim_2\\tt.hoc");

        //File neuronFile = new File("C:\\nrn54\\Morphology\\testFormats\\test.hoc");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\testFormats\\simple.nrn");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\spikeinit\\dks577a.hoc");
        //File neuronFile = new File("C:\\neuroConstruct\\projects\\Project_1\\gran.hoc");
        //File neuronFile = new File("C:\\nrn54\\Morphology\\anderson\\anderson\\Cells\\meynert2\\meynert2.hoc");
        //File neuronFile = new File("C:\\nrn54\\PatTest\\basics\\access.hoc");


        File newFile = new File("C:\\temp\\generated.nrn");

        try
        {
            NeuronFileConverter nfc = new NeuronFileConverter();
            String newStuff = nfc.convertNeuronFile(neuronFile);
            FileWriter fw = new FileWriter(newFile);
            //fw.write("load_file(\"nrngui.hoc\")\n");
            fw.write(newStuff);
            //fw.write("\naccess soma\n");
            fw.close();

            System.out.println("Written new file to: " + newFile);


            NeuronMorphReader nmr = new NeuronMorphReader();
            Cell cell = nmr.loadFromMorphologyFile(newFile, "FunnyCell", false, false);

            System.out.println(CellTopologyHelper.printDetails(cell, null));



            /*
                    NeuronMorphReader nmr = new NeuronMorphReader();
                    Cell cell = nmr.loadFromMorphologyFile(newFile, "FunnyCell");



                    System.out.println(CellTopologyHelper.printDetails(cell));
             */

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


}
