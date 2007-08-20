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

package ucl.physiol.neuroconstruct.genesis;

import ucl.physiol.neuroconstruct.utils.*;


/**
 * A class containing the information on the numerical method to use. Also centralises
 * generation of the code for each numerical method.
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

public class NumericalMethod
{
    static ClassLogger logger = new ClassLogger("NumericalMethod");


    int methodNumber = 11;
    boolean hsolve = true;
    int chanMode = 0;


    public NumericalMethod()
    {
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    public int getMethodNumber()
    {
        return methodNumber;
    }
    public void setMethodNumber(int methodNumber)
    {
        this.methodNumber = methodNumber;
    }
    public boolean isHsolve()
    {
        return hsolve;
    }
    public void setHsolve(boolean hsolve)
    {
        this.hsolve = hsolve;
    }
    public int getChanMode()
    {
        return chanMode;
    }
    public void setChanMode(int chanMode)
    {
        this.chanMode = chanMode;
    }

    /**
     * Short description of the NumericalMethod
     */
    public String toString()
    {
        StringBuffer info = new StringBuffer();



        switch (methodNumber)
        {
            case -1:
                info.append("Forward Euler");
                break;

            case 0:
                info.append("Exponential Euler");
                break;

            case 2:
                info.append("Adams-Bashforth 2nd order");
                break;


            case 3:
                info.append("Adams-Bashforth 3rd order");
                break;


            case 4:
                info.append("Adams-Bashforth 4th order");
                break;


            case 5:
                info.append("Adams-Bashforth 5th order");
                break;


            case 10:
                info.append("Backward Euler");
                break;

            case 11:
                info.append("Crank-Nicholson");
                break;

            default:
                info.append("Unknown method");
                break;

        }

        info.append(" num integration method ("
                    + methodNumber
                    +"), using hsolve: " + hsolve);

        if (hsolve)
        {
            info.append(", chanmode: "+chanMode);
        }
        return info.toString();
    }

    public String getScript()
    {
       StringBuffer genesisScript = new StringBuffer();

       GenesisFileManager.addMajorComment(genesisScript, this.toString());


       if (hsolve)
       {
           genesisScript.append("echo \"----------- Specifying hsolve\"\n\n");
/*
           String pulseHsolveElement = GenesisFileManager.PULSE_ELEMENT_ROOT+"/"
                                                    +GenesisFileManager.HSOLVE_ELEMENT_NAME;

            genesisScript.append("create hsolve "+pulseHsolveElement+"\n");

            genesisScript.append("setfield " + pulseHsolveElement + " path " +
                                 GenesisFileManager.PULSE_ELEMENT_ROOT
                                 + "/#[][TYPE=pulsegen] comptmode 1\n");

            genesisScript.append("setmethod " + pulseHsolveElement + " " + methodNumber + "\n");
            genesisScript.append("setfield " + pulseHsolveElement + " chanmode " + chanMode + "\n");

            genesisScript.append("call " + pulseHsolveElement + " SETUP\n");

            genesisScript.append("echo \"Created and set up hsolve at: \" " + pulseHsolveElement + "\n");
            genesisScript.append("reset\n\n");
*/

           genesisScript.append("str cellName\n");
           genesisScript.append("foreach cellName ({el "+GenesisFileManager.CELL_ELEMENT_ROOT+"/#/#})\n");

           String hsolveElement = "{cellName}/"+GenesisFileManager.HSOLVE_ELEMENT_NAME;

           genesisScript.append("    create hsolve "+hsolveElement+"\n");

           genesisScript.append("    setfield "+hsolveElement+" path " +
                                "{cellName}/#[][TYPE=compartment],"+
                                "{cellName}/#[][TYPE=symcompartment] comptmode 1\n");

           genesisScript.append("    setmethod "+hsolveElement+" " + methodNumber + "\n");
           genesisScript.append("    setfield "+hsolveElement+" chanmode " + chanMode + "\n");

           genesisScript.append("    call "+hsolveElement+" SETUP\n");


          // genesisScript.append("    echo \"Created and set up hsolve at: \" "+hsolveElement+"\n");

           genesisScript.append("    reset\n");

           genesisScript.append("end\n");

           genesisScript.append("reset\n");






           genesisScript.append("echo \"-----------Done specifying hsolve \"\n\n");
       }
       else
       {

           GenesisFileManager.addComment(genesisScript,
                                                "Note: the same method is applied to every element.");

           genesisScript.append("setmethod "+methodNumber+"\n");
       }


       return genesisScript.toString();
    }

    public static void main(String[] args)
    {
        NumericalMethod nm = new NumericalMethod();

        nm.setChanMode(2);
        nm.setHsolve(true);
        nm.setMethodNumber(10);

        logger.logComment("Created: "+nm);

        logger.logComment("Code: ");
        logger.logComment(nm.getScript());

    }

    private void jbInit() throws Exception
    {
    }
}
