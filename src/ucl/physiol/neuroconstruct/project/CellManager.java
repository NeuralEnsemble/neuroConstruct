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

package ucl.physiol.neuroconstruct.project;

import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Class for storing types of cells in project
 *
 * @author Padraig Gleeson
 *  
 */

public class CellManager
{
    ClassLogger logger = new ClassLogger("CellManager");

    private Vector<Cell> allCells = new Vector<Cell>();


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
            Cell cell = allCells.elementAt(i);
            cellTypeNames.add(cell.getInstanceName());
        }


        return cellTypeNames;
    }



    public Cell getCell(String cellType)
    {
        Cell foundCellType = null;

        for (int i = 0; i < allCells.size(); i++)
        {
            Cell cell = allCells.elementAt(i);
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
            Cell cell = allCells.elementAt(i);
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

    public void deleteAllCellTypes()
    {
        allCells = new Vector<Cell>();
    }



}



