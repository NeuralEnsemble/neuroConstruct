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

package ucl.physiol.neuroconstruct.cell.utils;

import java.util.*;

import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.examples.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Helper class storing inbuilt cell types
 *
 * @author Padraig Gleeson
 *  
 *
 */



public class CellTypeHelper
{
    static ClassLogger logger = new ClassLogger("CellTypeHelper");

    private static Vector<String> ordinaryCellTypes = new Vector<String>();
    private static Vector<Object> morphFileCellTypes = new Vector<Object>();

    private static Hashtable extensionsVsMorphCellTypes = new Hashtable();
    private static Hashtable<String, String> descriptionsVsCellTypes = new Hashtable<String, String>();

    static
    {
        /** @todo Use reflection etc. to parse classpath for anything extending Cell  */

        addNewOrdinaryCellType("OneSegment", "Hard coded Test cell");
        addNewOrdinaryCellType("SimpleCell", "Hard coded Test cell");
        //addNewOrdinaryCellType("ComplexCell", "Hard coded Test cell");
        addNewOrdinaryCellType("PurkinjeCell", "Hard coded Test cell");
        addNewOrdinaryCellType("GranuleCell", "Hard coded Test cell");
        //addNewOrdinaryCellType("MossyFiber", "Hard coded Test cell");
        addNewOrdinaryCellType("GolgiCell", "Hard coded Test cell");


        addNewMorphCellType("ucl.physiol.neuroconstruct.cell.converters.MorphMLConverter");
        addNewMorphCellType("ucl.physiol.neuroconstruct.cell.converters.NeurolucidaReader");
        addNewMorphCellType("ucl.physiol.neuroconstruct.cell.converters.GenesisMorphReader");
        addNewMorphCellType("ucl.physiol.neuroconstruct.cell.converters.NeuronMorphReader");
        addNewMorphCellType("ucl.physiol.neuroconstruct.cell.converters.SWCMorphReader");



        Vector<Object> allCellTypes = new Vector<Object>(ordinaryCellTypes);
        allCellTypes.addAll(morphFileCellTypes);
        logger.logComment("All cell types: "+ allCellTypes);

    }

    public static Cell getCell(String cellType, String cellName)
    {
        if (cellType.equals("OneSegment")) return new OneSegment(cellName);
        if (cellType.equals("SimpleCell")) return new SimpleCell(cellName);
        //if (cellType.equals("ComplexCell")) return new ComplexCell(cellName);
        if (cellType.equals("PurkinjeCell")) return new PurkinjeCell(cellName);
        if (cellType.equals("GranuleCell")) return new GranuleCell(cellName);
        //if (cellType.equals("MossyFiber")) return new MossyFiber(cellName);
        if (cellType.equals("GolgiCell")) return new GolgiCell(cellName);


/*
        if (cellType.equals("GenesisMorphReader"))
        {
            Cell cell = null;
            try
            {
                cell = GenesisMorphReader.loadFromMorphologyFile(morphologyFile, cellName);
                return cell;
            }
            catch (MorphologyException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem loading that Genesis morphology file", ex, null);
                return null;
            }
        }

        if (cellType.equals("SWCMorphReader"))
        {
            Cell cell = null;
            try
            {
                cell = SWCMorphReader.loadFromMorphologyFile(morphologyFile, cellName);
                return cell;
            }
            catch (MorphologyException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem loading that SWC morphology file", ex, null);
                return null;
            }
        }

        if (cellType.equals("MorphMLConverter"))
        {
            Cell cell = null;
            try
            {
                cell = MorphMLConverter.loadFromMorphMLFile(morphologyFile);
                cell.setInstanceName(cellName);
                return cell;
            }
            catch (MorphologyException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem loading that MorphML morphology file", ex, null);
                return null;
            }
        }

        if (cellType.equals("NeuronMorphReader"))
        {
            Cell cell = null;
            try
            {
                File simplifiedNeuronFile = new File(morphologyFile.getParent(),
                                                     "simplified_"+ morphologyFile.getName());

                GuiUtils.showInfoMessage(logger, "NEURON file simplified",
                                             "That NEURON file has been converted to a simplified format for importation. The new version is at: " +
                                             simplifiedNeuronFile, null);

                NeuronFileConverter nfc = new NeuronFileConverter();
                String newStuff = nfc.convertNeuronFile(morphologyFile);
                FileWriter fw = new FileWriter(simplifiedNeuronFile);
                fw.write(newStuff);
                fw.close();

                System.out.println("Written new file to: " + simplifiedNeuronFile);
                Frame parentFrame = GuiUtils.getMainFrame();

                NeuronImportDialog dlg = new NeuronImportDialog(parentFrame);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension frameSize = dlg.getSize();

                if (frameSize.height > screenSize.height)
                    frameSize.height = screenSize.height;
                if (frameSize.width > screenSize.width)
                    frameSize.width = screenSize.width;

                dlg.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

                dlg.setModal(true);

                dlg.show();




                NeuronMorphReader nmr = new NeuronMorphReader();
                cell = nmr.loadFromMorphologyFile(simplifiedNeuronFile,
                                                  cellName,
                                                  dlg.getMoveToOrigin(),
                                                  dlg.getMoveDendrites());

                return cell;
            }
            catch (MorphologyException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem loading that NEURON morphology file", ex, null);
                return null;
            }

            catch (NeuronException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem loading that NEURON morphology file", ex, null);
                return null;
            }
            catch (IOException ex)
            {
                GuiUtils.showErrorMessage(logger, "Problem with that file", ex, null);
                return null;
            }


        }

*/
        else return null;
    }

    public static void addNewOrdinaryCellType(String cellType, String description)
    {
        ordinaryCellTypes.add(cellType);
        descriptionsVsCellTypes.put(cellType, description);
    }

    public static void addNewMorphCellType(String fullClassName)
    {
        try
        {
            // This assumes a sngle constructor, with no arguments
            Object[] args = new Object[]{};
            morphFileCellTypes.add(Class.forName(fullClassName).getConstructors()[0].newInstance(args));
        }
        catch (Exception ex)
        {
            GuiUtils.showErrorMessage(logger,
            "Problem adding class for morphology importer: "+fullClassName,
            ex, null);
        }
    }

    public static String[] getExtsForCellType(String cellType)
    {
        String[] fileExt = (String[])extensionsVsMorphCellTypes.get(cellType);
        return fileExt;
    }

    public static String getDescForCellType(String cellType)
    {
        String desc = descriptionsVsCellTypes.get(cellType);
        return desc;
    }



    public static Enumeration getAllCellTypeNames()
    {
        Vector<Object> allCellTypes = new Vector<Object>(ordinaryCellTypes);
        allCellTypes.addAll(morphFileCellTypes);
        return allCellTypes.elements();
    }

    public static boolean isAMorphologyCellType(String cellTypeName)
    {
        logger.logComment("Checking whether "+cellTypeName+" is a morphology file");
        if (morphFileCellTypes.contains(cellTypeName)) return true;
        else return false;
    }
    
    public static void main(String[] args)
    {
        System.out.println((float)(35.7788d - (18.7788d)));
    }

}
