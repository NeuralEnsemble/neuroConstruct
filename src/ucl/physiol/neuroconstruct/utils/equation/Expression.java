/**
 *  neuroConstruct
 *  Software for developing large scale 3D networks of biologically realistic neurons
 * 
 *  Copyright (c) 2009 Padraig Gleeson
 *  UCL Department of Neuroscience, Physiology and Pharmacology
 *
 *  Development of this software was made possible with funding from the
 *  Medical Research Council and the Wellcome Trust
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


package ucl.physiol.neuroconstruct.utils.equation;

import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 *  
 */

public class Expression
{
    private static ClassLogger logger = new ClassLogger("Expression");

    private static String commentIndent = "";

    public static String tidyBasicFunctions(String origExp)
    {
        String newExp = new String(origExp);

        for (int i = 0; i < BasicFunctions.allFunctions.length; i++)
        {
            String next = BasicFunctions.allFunctions[i];

            /** @todo  Quick & dirty... */
            newExp = GeneralUtils.replaceAllTokens(newExp, next+" (", next+"(");
            newExp = GeneralUtils.replaceAllTokens(newExp, next+"  (", next+"(");
            newExp = GeneralUtils.replaceAllTokens(newExp, next+"   (", next+"(");
            newExp = GeneralUtils.replaceAllTokens(newExp, next+"    (", next+"(");

        }

        return newExp;
    }




    public static EquationUnit parseExpression(String line, Variable[] variables) throws EquationException
    {
        line = line.trim();

        line = tidyBasicFunctions(line);
        
        //logger.setThisClassVerbose(true);

        logger.logComment(" ");
        String info = "+++++  Parsing line: [ "+line+ " ] with variables: [ ";
        for(Variable v: variables)
        {
            info = info + v.toString()+" ";
        }
        info = info + " ]";
        
        logger.logComment(info);


        while (line.startsWith("(") && getIndexClosingBracket(line, 0)==line.length()-1 )
        {
            line = line.substring(1, line.length()-1);
            logger.logComment("Converted to: ["+line+ "]");
        }

        Vector  loneBinaryOperators = getLoneBinaryOperators(line);

        logger.logComment("Locations of lone operators (-, +, *, /): "+ loneBinaryOperators);

        boolean isValidNum = false;
        try
        {
            Double.parseDouble(line);
            isValidNum = true;
            logger.logComment("isValidNum!!");
        }
        catch(NumberFormatException nfe)
        {
            // false...
        }


        if (loneBinaryOperators.size()==0 || isValidNum)
        {
            return parseCleansedExpression(line, variables);
        }
        if (loneBinaryOperators.size()==1)
        {
            Integer operatorPos = (Integer)loneBinaryOperators.firstElement();

            int index = operatorPos.intValue();

            char operator = line.charAt(index);

            String before = line.substring(0,index);
            String after = line.substring(index + 1);

            logger.logComment("....   Creating binary op with ["+before+"] and ["+after+"]");

            return new BinaryOperation(parseExpression(before, variables),
                                       parseExpression(after, variables),
                                       operator);
        }

        else
        {
            // do ^
            for (int i = 0; i < loneBinaryOperators.size(); i++)
            {
                Integer currentPos = (Integer)loneBinaryOperators.elementAt(i);

                int index = currentPos.intValue();

                char operator = line.charAt(index);

                int indexOfPrev = -1;
                if (i>=1)
                    indexOfPrev = ((Integer)loneBinaryOperators.elementAt(i-1)).intValue();

                int indexOfNext = line.length();
                if (i<loneBinaryOperators.size()-1)
                    indexOfNext = ((Integer)loneBinaryOperators.elementAt(i+1)).intValue();

                logger.logComment("indexOfPrev: "+ indexOfPrev
                                  + ", index : "+ index
                                  + ", indexOfNext: "+ indexOfNext);


                String before = line.substring(indexOfPrev+1,index);
                String after = line.substring(index + 1, indexOfNext);

                if (before.trim().length()==0)
                {
                    return parseCleansedExpression(line, variables);
                }
                logger.logComment("Found before: " + before);
                logger.logComment("Found operator: " + operator);
                logger.logComment("Found after: " + after);



                if (operator == '^')
                {
                    String beforeNewBracket = line.substring(0, indexOfPrev+1);
                    String afterNewBracket = line.substring(indexOfNext);

                    return parseExpression(beforeNewBracket +
                                          " ("
                                          + before + " "
                                          + operator + " "
                                          + after + ") "
                                          + afterNewBracket, variables);
               }



           }
            // do * and /
            for (int i = 0; i < loneBinaryOperators.size(); i++)
            {
                Integer currentPos = (Integer)loneBinaryOperators.elementAt(i);

                int index = currentPos.intValue();

                char operator = line.charAt(index);

                int indexOfPrev = -1;
                if (i>=1)
                    indexOfPrev = ((Integer)loneBinaryOperators.elementAt(i-1)).intValue();

                int indexOfNext = line.length();
                if (i<loneBinaryOperators.size()-1)
                    indexOfNext = ((Integer)loneBinaryOperators.elementAt(i+1)).intValue();

                logger.logComment("indexOfPrev: "+ indexOfPrev
                                  + ", index : "+ index
                                  + ", indexOfNext: "+ indexOfNext);


                String before = line.substring(indexOfPrev+1,index);
                String after = line.substring(index + 1, indexOfNext);

                if (before.trim().length()==0)
                {
                    return parseCleansedExpression(line, variables);
                }
                logger.logComment("Found before: " + before);
                logger.logComment("Found operator: " + operator);
                logger.logComment("Found after: " + after);



                if (operator == '*' || operator == '/')
                {
                    String beforeNewBracket = line.substring(0, indexOfPrev+1);
                    String afterNewBracket = line.substring(indexOfNext);

                    return parseExpression(beforeNewBracket +
                                          " ("
                                          + before + " "
                                          + operator + " "
                                          + after + ") "
                                          + afterNewBracket, variables);
               }



           }
           // do + and -
           for (int i = 0; i < loneBinaryOperators.size(); i++)
           {
               Integer currentPos = (Integer)loneBinaryOperators.elementAt(i);

               int index = currentPos.intValue();

               char operator = line.charAt(index);

               int indexOfPrev = -1;
               if (i>=1)
                   indexOfPrev = ((Integer)loneBinaryOperators.elementAt(i-1)).intValue();

               int indexOfNext = line.length();
               if (i<loneBinaryOperators.size()-1)
                   indexOfNext = ((Integer)loneBinaryOperators.elementAt(i+1)).intValue();

               logger.logComment("indexOfPrev: "+ indexOfPrev
                                 + ", index : "+ index
                                 + ", indexOfNext: "+ indexOfNext);


               String before = line.substring(indexOfPrev+1,index);
               String after = line.substring(index + 1, indexOfNext);

               if (before.trim().length()==0)
               {
                   return parseCleansedExpression(line, variables);
               }
               logger.logComment("... Found before: " + before);
               logger.logComment("Found operator: " + operator);
               logger.logComment("Found after: " + after);

               String possibleExp = before+operator+after;
                float val = Float.NaN;
               try
               {
                   val = Float.parseFloat(possibleExp);
                   logger.logComment("... Found a value: "+ val);
               }
               catch (NumberFormatException ex)
               {
                   //...

                   logger.logComment("Not a val: "+ possibleExp);
               }

               if (before.endsWith("e") && !Float.isNaN(val))
               {
                   // Ignore, as it's a number...
                   logger.logComment("... Found a value: "+ val);
               }
               else
               {

                   if (operator == '+' || operator == '-')
                   {
                       String beforeNewBracket = line.substring(0, indexOfPrev + 1);
                       String afterNewBracket = line.substring(indexOfNext);

                       return parseExpression(beforeNewBracket +
                                              " ("
                                              + before + " "
                                              + operator + " "
                                              + after + ") "
                                              + afterNewBracket, variables);
                   }
               }


           }

        }

        return null;
    }

    private static Vector getLoneBinaryOperators(String line) throws EquationException
    {
        //BinaryOperation.allBinaryOps

        Vector<Integer> foundBinOps = new Vector<Integer>();

        for (int checkPosn = 0; checkPosn < line.length()-1; checkPosn++)
        {
            String subpart = line.substring(checkPosn);
            logger.logComment("Checking char: "+ subpart.charAt(0));
            if (subpart.startsWith("("))
            {
                int endBracketIndex = getIndexClosingBracket(line, checkPosn);
                checkPosn = endBracketIndex;
            }
            else
            {
                for (int binOpIndex = 0; binOpIndex < BinaryOperation.allBinaryOps.length; binOpIndex++)
                {
                    char nextOp = BinaryOperation.allBinaryOps[binOpIndex];
                    if (subpart.startsWith(nextOp+""))
                    {

                        //logger.logComment("startsWith: "+ nextOp+", prev: "+ line.charAt(checkPosn-1));
                        if (checkPosn>0 && (line.charAt(checkPosn-1)!='E' && line.charAt(checkPosn-1)!='e'))
                        {
                            foundBinOps.add(new Integer(checkPosn));
                        }
                    }
                }
            }
        }

        return foundBinOps;
    }



    private static EquationUnit parseCleansedExpression(String line, Variable[] variables) throws EquationException
    {
        commentIndent = commentIndent + "  ";

        logger.logComment(commentIndent +">>>>>  Parsing cleansed line: ["+line+ "]");

        String originalLine = new String(line);

        line = line.trim();

        if (line.startsWith("=")) line = line.substring(1).trim();

        logger.logComment(commentIndent +"Line: ["+line+ "]");

        boolean isValidNum = false;
        try
        {
            Double.parseDouble(line);
            isValidNum = true;
        }
        catch(NumberFormatException nfe)
        {
            // false...
        }

        EquationUnit equationSoFar = null;
        String remainder = "";

        if (line.startsWith("("))
        {
            logger.logComment(commentIndent +"Found a bracketed part of the expression");
            int endBracket = getIndexClosingBracket(line, 0);

            String internalExp = line.substring(1, endBracket);

            if (endBracket< line.length()-1)
                remainder = line.substring(endBracket+1);

            logger.logComment(commentIndent +"In bracket: "+ internalExp);
            logger.logComment(commentIndent +"After bracket: "+ remainder);

            equationSoFar = parseCleansedExpression(internalExp, variables);

            logger.logComment(commentIndent +"Found internal expression: "+ equationSoFar);


        }
        else if (line.startsWith("-") && !isValidNum)
        {
            logger.logComment(commentIndent +"Found a minus sign in: "+ line);


            Constant con = new Constant(-1);

            //equationSoFar = new BinaryOperation(con,
             //                                   parseExpression(line.substring(1), variables),
           //                                     BinaryOperation.PRODUCT);
            
            
            equationSoFar = parseExpression("-1 * "+line.substring(1), variables);


        }


        else if  (isValidNum || Character.isDigit(line.charAt(0)) ||
                  line.charAt(0)=='.')
        {
            logger.logComment(commentIndent +"Found a number..");

            int lengthNum = 1;

            for (int i = 1; i < line.length(); i++)
            {
                if (Character.isDigit(line.charAt(i))||
                    line.charAt(i)=='.'||
                    line.charAt(i)=='e'||
                    line.charAt(i)=='E'||
                    line.charAt(i)=='-')
                {
                    lengthNum++;
                }
                else
                {
                    i = line.length();
                }
            }

            String num = line.substring(0,lengthNum);

            String restOfLine = line.substring(lengthNum);

            logger.logComment(commentIndent +"Number is ("+ num+"), restOfLine: "+restOfLine);

            double value = Double.parseDouble(num);

            Constant con = new Constant(value);

            if (restOfLine.length()==0)
            {
                return con;
            }

            restOfLine = restOfLine.trim();

            //EquationUnit rest

            for (int i = 0; i < BinaryOperation.allBinaryOps.length; i++)
            {
                char nextOp = BinaryOperation.allBinaryOps[i];

                logger.logComment(commentIndent +"Checking for binary op " + nextOp+ " next to number" );

                if (restOfLine.startsWith(nextOp+""))
                {
                    String restOfExp = restOfLine.substring(1);
                    EquationUnit internalEqn = parseCleansedExpression(restOfExp, variables);

                    equationSoFar = new BinaryOperation(con, internalEqn, nextOp);
                }
            }
            if (equationSoFar == null)
            {
                logger.logComment(commentIndent +"No operator after constant "+ con +", assuming multiplication...");

                equationSoFar = new BinaryOperation(con,
                                                    parseCleansedExpression(restOfLine, variables),
                                                    BinaryOperation.PRODUCT);
            }
        }


        else if (Character.isLetter(line.charAt(0)))
        {
            logger.logComment(commentIndent +"Found an equation or variable...");


            for (int i = 0; i < variables.length; i++)
            {
                String nextVar = variables[i].getName();

                if (line.startsWith(nextVar))
                {
                    logger.logComment(commentIndent +"Found variable " + nextVar);

                    equationSoFar = new Variable(nextVar);

                    remainder = line.substring(nextVar.length());
                }
            }

            for (int i = 0; i < BasicFunctions.allFunctions.length; i++)
            {


                String nextFunc = BasicFunctions.allFunctions[i];

                logger.logComment(commentIndent +"Comparing to: "+ nextFunc);

                if (line.startsWith(nextFunc))
                {
                    logger.logComment(commentIndent +"Think it's "+ nextFunc);
                    int funcBracket  = line.indexOf("(");
                    if (funcBracket<0)
                    {
                        throw new EquationException("Unable to determine brackets for function: "
                                                    +nextFunc
                            + " in line: "+ originalLine);
                    }
                    int endBracket = getIndexClosingBracket(line, nextFunc.length());

                    String internalExp = line.substring(funcBracket+1, endBracket);


                    EquationUnit internalEqn = parseCleansedExpression(internalExp, variables);

                    equationSoFar = BasicFunctions.getFunction(nextFunc, internalEqn);

                    if (endBracket< line.length()-1)
                        remainder = line.substring(endBracket+1);
                    
                    logger.logComment(commentIndent +"remainder: "+ remainder);
                    
                }
            }
        }


        if (equationSoFar!=null)
            logger.logComment(commentIndent +"--------     Equation so far: ["+ equationSoFar.getNiceString()
                + "], Remainder of line: ["+ remainder+"]");
        else
        {
            throw new EquationException("Unable to parse line: "+ originalLine);

        }
        remainder = remainder.trim();

        if (remainder.length()>0)
        {
            for (int i = 0; i < BinaryOperation.allBinaryOps.length; i++)
            {
                char nextOp = BinaryOperation.allBinaryOps[i];

                logger.logComment(commentIndent+"Checking for binary op: "+ nextOp);

                if (remainder.startsWith(nextOp+""))
                {
                    String restOfExp = remainder.substring(1);
                    EquationUnit internalEqn = parseCleansedExpression(restOfExp, variables);

                    equationSoFar = new BinaryOperation(equationSoFar, internalEqn, nextOp);
                }
            }
        }

        logger.logComment(commentIndent +"<<<<<<   Equation evaluated as: "+ equationSoFar.getNiceString());

        if (commentIndent.length()>2)
            commentIndent = commentIndent.substring(2);
        else commentIndent = "";
        return equationSoFar;
    }

    private static int getIndexClosingBracket(String line, int indexOpeningBracket) throws EquationException
    {
        int numInternalBrackets = 0;
        logger.logComment("Looking for ( in line ["+line+"] after point: "+indexOpeningBracket);

        for (int i = indexOpeningBracket+1; i < line.length(); i++)
        {
            //logger.logComment("numInternalBrackets: "+ numInternalBrackets);
            char nextChar = line.charAt(i);
            if (nextChar=='(') numInternalBrackets++;

            else if (numInternalBrackets>0 && nextChar==')')
                numInternalBrackets--;

            else if (numInternalBrackets==0 && nextChar==')')
            {
                logger.logComment("Found bracket at index "+ i + " of line: ["+line+"] with "+ line.length() + " chars" );
                return i;
            }
        }
        throw new EquationException("Unable to locate closing bracket for opening bracket at column "+
            indexOpeningBracket+ " in line segment: "+ line);
    }



    public static void main(String[] args)
    {
        //String expression = " = 0.058*exp(-(vm-10)/15)";

        //String expression = "2/((3*exp((x + 4)/5)  + exp(-(x + 6)/7)))";

        //String expression = "-exp((-1*(t1*t2)/(t2-t1)*log(t2/t1))/t1) + exp((-1*(t1*t2)/(t2-t1)*log(t2/t1))/t2)";
        //String expression = "log(t2/t1)";

        //String expression = "2x + 4x * 8 + 4x / 2";
        //String expression = "(2e-2* x / 4x)";

        //String tau = "6.66e-2";

        //String expression = "2e-2 * 3 *exp   (t/"+tau+")";
       //v &lt; -60 ? 0.005 : 0.005 * (exp (-0.05 * (v - (-60))))
        
        //String expression = "v &lt; -60 ? 0.005 :0.005 * (exp (-0.05 * (v - (-60))))";
        //String expression = "exp ((-v - 40)/ 10)";
        //String expression = "((-v - 40)/ 10)";
        String expression = "sin( sin(v) -1 )";

        try
        {
            Variable v = new Variable("v");
            Variable t = new Variable("t");
            Variable t1 = new Variable("t1");
            Variable t2 = new Variable("t2");

            Variable[] vars = new Variable[]{v, t, t1, t2};

            EquationUnit eqn = Expression.parseExpression(expression, vars);

            System.out.println("Expression line " + expression + " parsed as: " + eqn.getNiceString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


}
