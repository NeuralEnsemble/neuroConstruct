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

package ucl.physiol.neuroconstruct.project;

/**
 * Exception thrown when the parsing of a project file goes bad
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

@SuppressWarnings("serial")

public class ProjectFileParsingException extends Exception
{

    public ProjectFileParsingException()
    {
    }

    public ProjectFileParsingException(String message)
    {
        super(message);
    }

    public ProjectFileParsingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProjectFileParsingException(Throwable cause)
    {
        super(cause);
    }
}