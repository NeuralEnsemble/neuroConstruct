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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import javax.swing.filechooser.FileFilter;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * FileFilter for project files.
 *
 * @author Padraig Gleeson
 *  
 */


public class SimpleFileFilter extends FileFilter implements java.io.FileFilter
{
    ClassLogger logger = new ClassLogger("ProjectFileFilter");
    String[] currentFileFilters = null;
    String description = null;

    boolean acceptDirs = true;

    /**
     * File filter for project info files
     */
    private SimpleFileFilter()
    {
    }

    public SimpleFileFilter(String[] filters, String description)
    {
        this.currentFileFilters = filters;
        this.description = description;
    }
    public SimpleFileFilter(String[] filters, String description, boolean acceptDirs)
    {
        this.currentFileFilters = filters;
        this.description = description;
        this.acceptDirs = acceptDirs;
    }


    public boolean accept(File f)
    {
        //logger.logComment("File being tested: "+f.getAbsolutePath());
        if (f != null)
        {
            if (f.isDirectory())
            {
                return acceptDirs;
            }
            String filenameLowerCase = f.getName().toLowerCase();
            for (int i = 0; i < currentFileFilters.length; i++)
            {
                if (filenameLowerCase.endsWith(currentFileFilters[i].toLowerCase()))
                {
                    return true;
                }

            }

        }
        return false;

    }
    public String getDescription()
    {
        //return ;
        return description;
    }



}
