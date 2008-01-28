/**
 * neuroConstruct
 *
 * Software for developing large scale 3D networks of biologically realistic neurons
 * Copyright (c) 2008 Padraig Gleeson
 * UCL Department of Physiology
 *
 * Development of this software was made possible with funding from the
 * Medical Research Council
 *
 */

package ucl.physiol.neuroconstruct.project.cellchoice;


/**
 * Will be thrown by CellChooser subclass when all cells are chosen
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class AllCellsChosenException extends Exception
{
    public AllCellsChosenException()
    {
        super("All Cells which can be chosen have been");
    }
}
