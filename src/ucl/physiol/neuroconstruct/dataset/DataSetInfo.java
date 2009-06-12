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

package ucl.physiol.neuroconstruct.dataset;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import ucl.physiol.neuroconstruct.gui.DataSetManager;
import ucl.physiol.neuroconstruct.project.ProjectStructure;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.utils.GuiUtils;

/**
 * Extension of AbstractTableModel to store the info on saved DataSets
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class DataSetInfo extends AbstractTableModel
{

    ClassLogger logger = new ClassLogger("DataSetInfo");

    public final static int COL_NUM_FILE_NAME = 0;
    public final static int COL_NUM_DATE = 1;
    public final static int COL_NUM_NAME = 2;
    public final static int COL_NUM_DESC = 3;

    public static final String COL_NAME_FILE_NAME = "File";
    public static final String COL_NAME_DATE = "Last saved";
    public static final String COL_NAME_NAME = "Data Set Reference";
    public static final String COL_NAME_DESC = "Description";

    SimpleDateFormat formatter = new SimpleDateFormat("H:mm (MMM d, yy)");

    Vector<String> allColumns = new Vector<String>(3);

    Vector<DataSet> dataSetObjs = new Vector<DataSet>();

    File dataSetDir = null;

    public DataSetInfo()
    {

    }

    public DataSetInfo(File dataSetDir)
    {
        logger.logComment("New DataSetInfo created with dir: "+ dataSetDir);
        this.dataSetDir = dataSetDir;
        //this.columnsShown = preferredColumns;

        refresh();
    }

    public File getDataSetDir()
    {
        if (!dataSetDir.exists())
        {
            logger.logComment("Creating the data set dir...");
            //boolean success = dataSetDir.mkdir();
            //if (!s)
        }
        return dataSetDir;
    }

    public void refresh()
    {
        logger.logComment("Refreshing the contents of the table model");

        allColumns.removeAllElements();
        dataSetObjs.removeAllElements();


        allColumns.add(COL_NUM_FILE_NAME, COL_NAME_FILE_NAME);
        allColumns.add(COL_NUM_DATE, COL_NAME_DATE);
        allColumns.add(COL_NUM_NAME, COL_NAME_NAME);
        allColumns.add(COL_NUM_DESC, COL_NAME_DESC);

        File[] childrenDirs = dataSetDir.listFiles();

        if (childrenDirs!=null)
        {

            logger.logComment("There are " + childrenDirs.length + " files in dir: " +
                              dataSetDir.getAbsolutePath());

            // Quick reorder...
            if (childrenDirs.length > 1)
            {
                for (int j = 1; j < childrenDirs.length; j++)
                {

                    for (int k = 0; k < j; k++)
                    {

                        if (childrenDirs[j].lastModified() < childrenDirs[k].lastModified())
                        {
                            File earlierFile = childrenDirs[j];
                            File laterFile = childrenDirs[k];
                            childrenDirs[j] = laterFile;
                            childrenDirs[k] = earlierFile;
                        }
                    }
                }
            }

            // int rowNumber = 0;

            for (int i = 0; i < childrenDirs.length; i++)
            {
                if (!childrenDirs[i].isDirectory()
                    && childrenDirs[i].getName().endsWith(ProjectStructure.getDataSetExtension()))
                {
                    logger.logComment("Looking at directory: " + childrenDirs[i].getAbsolutePath());

                    ArrayList<DataSet> dataSets = null;

                    try
                    {
                        dataSets = DataSetManager.loadFromDataSetFile(childrenDirs[i], true, DataSetManager.DataReadFormat.FIRST_COL_TIME);

                        for(DataSet ds: dataSets)
                            dataSetObjs.add(ds);

                        logger.logComment("That's a valid data set...");
                    }
                    catch (DataSetException ex1)
                    {
                        logger.logComment("That's not a valid data set");
                        GuiUtils.showErrorMessage(logger, "Problem with that Data Set", ex1, null);
                    }

                }
            }
        }
        this.fireTableStructureChanged();

    }


    public int getColumnCount()
    {
        return allColumns.size();
    }

    public int getRowCount()
    {
        return dataSetObjs.size();
    }

    @Override
    public String getColumnName(int col) {
        return allColumns.elementAt(col);
    }

    public Vector<String> getAllDataSetRefs()
    {
        Vector<String> allRefs = new Vector<String>();
        for (int i = 0; i < dataSetObjs.size(); i++)
        {
            DataSet ds = dataSetObjs.get(i);
            allRefs.add(ds.getReference());
        }
        return allRefs;
    }


    public DataSet getDataSet(int row) throws DataSetException
    {
        DataSet ds = dataSetObjs.elementAt(row);

        return DataSetManager.loadFromDataSetFile(ds.getDataSetFile(), false, DataSetManager.DataReadFormat.FIRST_COL_TIME).get(0);
    }

    public String getDataSetReference(int row) throws DataSetException
    {
        DataSet ds = dataSetObjs.elementAt(row);

        return ds.getReference();
    }


    public String getDataSetDescription(int row) throws DataSetException
    {
        DataSet ds = dataSetObjs.elementAt(row);

        return ds.getDescription();
    }



    public File getDataSetFile(int row) throws DataSetException
    {
        DataSet ds = dataSetObjs.elementAt(row);
        //File
        return ds.getDataSetFile();
    }


    public Object getValueAt(int row, int col)
    {

        DataSet dataSet = dataSetObjs.elementAt(row);

        switch (col)
        {
            case COL_NUM_FILE_NAME:
            {
                return dataSet.getDataSetFile().getName();
            }

            case COL_NUM_DATE:
            {
                long timeModified = dataSet.getDataSetFile().lastModified();
                java.util.Date modified = new java.util.Date(timeModified);
                return formatter.format(modified);
            }

            case COL_NUM_NAME:
            {
                return dataSet.getReference();
            }

            case COL_NUM_DESC:
            {
                return dataSet.getDescription();//sim.getDateModified();
            }
            default:
            {
                return null;//sim.getDateModified();
                /*
                String colName = (String)columnsShown.elementAt(col);
                Properties propsForSim = (Properties)extraColumns.elementAt(row);
                if (propsForSim==null) return "- n/a -";

                return propsForSim.getProperty(colName);*/
            }
        }
    }

/*
    protected class DataSetHolder
    {
        private DataSet partialDataSet = null;
        private File file = null;

        protected DataSetHolder(DataSet partialDataSet, File file)
        {
            this.partialDataSet = partialDataSet;
            this.file = file;
        }

        DataSet getPartialDataSet()
        {
            return partialDataSet;
        }

        File getFile()
        {
            return file;
        }

    }
*/

    @Override
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }



    public Vector getAllColumns()
    {
        return allColumns;
    }

}
