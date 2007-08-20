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


package ucl.physiol.neuroconstruct.utils.equation;

import ucl.physiol.neuroconstruct.utils.ClassLogger;


/**
 * Helper class for parsing equations
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class BinaryOperation extends EquationUnit
{
    ClassLogger logger = new ClassLogger("BinaryOperation");

    protected EquationUnit first = null;
    protected EquationUnit second = null;

    private char operation = '0';




    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char PRODUCT = '*';
    public static final char DIVISION = '/';


    public static char[] allBinaryOps
           = new char[]{PLUS, MINUS, PRODUCT, DIVISION};



    public BinaryOperation(EquationUnit first,
                           EquationUnit second,
                           char operation)
    {
        super("["+first.getNiceString()+ "] " + operation + " ["+ second.getNiceString()+"]");


        this.first = first;
        this.second = second;
        this.operation = operation;

        logger.logComment("New BinaryOperation created: "+ this.getName());
    }

    public double evaluateAt(Argument[] args)  throws EquationException
    {
        if (operation == PLUS)
        {
            return first.evaluateAt(args) + second.evaluateAt(args);
        }
        else if (operation == MINUS)
        {
            return first.evaluateAt(args) - second.evaluateAt(args);
        }
        else if (operation == PRODUCT)
        {
            return first.evaluateAt(args) * second.evaluateAt(args);
        }
        else if (operation == DIVISION)
        {
            return first.evaluateAt(args) / second.evaluateAt(args);
        }




        else
        {
            throw new EquationException("Unknown operation: "+ operation);
        }
    }



    public String getNiceString()
    {
        String niceFirst = first.getNiceString();
        if (first instanceof BinaryOperation) niceFirst = "("+niceFirst+")";
        String niceSecond = second.getNiceString();

        if (second instanceof BinaryOperation) niceSecond = "("+niceSecond+")";

        return niceFirst+ " " + operation + " "+ niceSecond;
    }


    public String toString()
    {
        return getNiceString();
    }




}
