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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Class for storing types of cells in project
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class CellManager
{
    ClassLogger logger = new ClassLogger("CellManager");

    private Vector<Cell> allCells = new Vector();


    public CellManager()
    {
    }
/*
    public Enumeration getAllCells()
    {
        logger.logComment("Returning info on "+allCells.size()+" cell types");
        return allCells.elements();
    }*/

    public Vector<Cell> getAllCells()
    {
        logger.logComment("Returning info on "+allCells.size()+" cell types");
        return allCells;
    }


    public int getNumberCellTypes()
    {
        logger.logComment("Returning number of cell types ("+allCells.size()+")");
        return allCells.size();
    }


    public ArrayList<String> getAllCellTypeNames()
    {
        logger.logComment("Returning "+allCells.size()+" cell type names");

        ArrayList<String> cellTypeNames = new ArrayList<String>();

        for (int i = 0; i < allCells.size(); i++)
        {
            Cell cell = (Cell)allCells.elementAt(i);
            cellTypeNames.add(cell.getInstanceName());
        }


        return cellTypeNames;
    }



    public Cell getCell(String cellType)
    {
        Cell foundCellType = null;

        for (int i = 0; i < allCells.size(); i++)
        {
            Cell cell = (Cell)allCells.elementAt(i);
            if (cell.getInstanceName().equals(cellType))
            {
                foundCellType = cell;
            }

        }
        return foundCellType;
    }

    public void addCellType(Cell newCell) throws NamingException
    {
        for (int i = 0; i < allCells.size(); i++)
        {
            Cell cell = (Cell)allCells.elementAt(i);
            if (cell.getInstanceName().equals(newCell.getInstanceName()))
            {
                throw new NamingException("The cell name: "
                                          + newCell.getInstanceName()
                                          + " has already been taken");
            }
        }
        if (newCell.getInstanceName().indexOf(" ")>=0)
        {
            throw new NamingException("The cell name cannot contain any spaces.");

        }

        allCells.add(newCell);
    }

    public void deleteCellType(Cell cell)
    {
        allCells.remove(cell);
    }



}



