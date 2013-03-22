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

package ucl.physiol.neuroconstruct.genesis;

import ucl.physiol.neuroconstruct.utils.*;


/**
 * A class containing the information on the numerical method to use. Also centralises
 * generation of the code for each numerical method.
 *
 * @author Padraig Gleeson
 *  
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
    @Override
    public String toString()
    {
        StringBuilder info = new StringBuilder();

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
       StringBuilder genesisScript = new StringBuilder();

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
