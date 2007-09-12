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

package ucl.physiol.neuroconstruct.nmodleditor.processes;

import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 *  
 */

public class ProcessOutputWatcher extends Thread
{
    ClassLogger logger = new ClassLogger("ProcessOutputWatcher");

    private InputStreamReader inputStrReader = null;

    private String referenceName = null;

    private StringBuffer log = new StringBuffer();

    public ProcessOutputWatcher(InputStream inputStr, String referenceName)
    {
        this.inputStrReader = new InputStreamReader(inputStr);
        this.referenceName = referenceName;
    }

    public String getLog()
    {
        return log.toString();
    }

    public void run()
    {
        try
        {
            //int numberOfBytesRead;

            BufferedReader br = new BufferedReader(inputStrReader);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                logger.logComment(referenceName +"> "+line);
                log.append(line+"\n");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
