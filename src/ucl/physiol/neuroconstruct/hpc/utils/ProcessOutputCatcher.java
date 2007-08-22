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

package ucl.physiol.neuroconstruct.hpc.utils;

import java.io.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Listens for feedback from processes
 *
 * @author Padraig Gleeson
 *  
 */

public class ProcessOutputCatcher extends Thread
{
    ClassLogger logger = new ClassLogger("ProcessOutputCatcher");

    public static int STDIO_OUTPUT = 0;
    public static int ERR_OUTPUT = 1;

    private InputStreamReader inputStrReader = null;

    private String referenceName = null;

    private StringBuffer log = new StringBuffer();

    ProcessFeedback feedback = null;

    int streamType = 0;

    public ProcessOutputCatcher(InputStream inputStr, String referenceName, ProcessFeedback feedback, int streamType)
    {
        this.inputStrReader = new InputStreamReader(inputStr);


        this.referenceName = referenceName;
        this.feedback = feedback;
        this.streamType = streamType;
    }

    public String getLog()
    {
        return log.toString();
    }

    public void run()
    {
        try
        {
            BufferedReader br = new BufferedReader(inputStrReader);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
                logger.logComment(referenceName +"> "+line);
                log.append(line+"\n");
                if (this.streamType == ERR_OUTPUT)
                {
                    feedback.error(log.toString());
                }
                else
                {
                    feedback.comment(log.toString());
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
