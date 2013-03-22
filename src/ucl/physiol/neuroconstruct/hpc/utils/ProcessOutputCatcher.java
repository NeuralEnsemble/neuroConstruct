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

    @Override
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
