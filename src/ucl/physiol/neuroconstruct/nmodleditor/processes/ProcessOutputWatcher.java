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
    
    @Override
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
