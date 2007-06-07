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

package ucl.physiol.neuroconstruct.gui;

import java.io.*;
import java.util.*;
import javax.swing.filechooser.FileFilter;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * FileFilter for project files.
 *
 * @author Padraig Gleeson
 * @version 1.0.3
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
