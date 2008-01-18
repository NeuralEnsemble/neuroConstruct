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

package ucl.physiol.neuroconstruct.cell.converters;

import java.io.*;

import ucl.physiol.neuroconstruct.cell.*;

/**
 *
 * Abstract class for all the external format morphology importers
 * See SWCMorphReader for a simple example of an implementation
 *
 * @author Padraig Gleeson
 *  
 *
 */

public abstract class FormatImporter
{
    private String name = null;
    private String desc = null;
    private String[] fileExtensions = null;

    //private static FormatImporter myInstance = null;


    protected FormatImporter()
    {
    }

    protected FormatImporter(String name, String desc, String[] fileExtensions)
    {
        this.name = name;
        this.desc = desc;
        this.fileExtensions = fileExtensions;
    }




    public abstract Cell loadFromMorphologyFile(File morphologyFile, String name) throws MorphologyException;

    public String getDesc()
    {
        return desc;
    }
    public String[] getFileExtensions()
    {
        return fileExtensions;
    }
    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return name + " ("+desc+")";
    }
    
    
    public String getWarnings()
    {
        return "";
    }


}
