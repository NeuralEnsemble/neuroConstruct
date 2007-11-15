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

package ucl.physiol.neuroconstruct.neuroml.hdf5;

import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.File;


/**
 * Utilities file for generating HDF5 files
 *
 * @author Padraig Gleeson
 *
 */

public class Hdf5Utils
{
    public Hdf5Utils()
    {
        super();
    }

    public static H5File createH5file(File file) throws Hdf5Exception
    {

        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null)
        {
            throw new Hdf5Exception("Cannot find HDF5 FileFormat.");
        }

        try
        {
            H5File h5File = (H5File) fileFormat.create(file.getAbsolutePath());

            if (h5File == null)
            {
                throw new Hdf5Exception("Failed to create file:"+file);
            }

            return h5File;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Error creating file: " + file + ".", ex);
        }
    }

    public static void open(H5File h5File) throws Hdf5Exception
    {
        try
        {
            h5File.open();
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to open HDF5 file", ex);
        }

    }

    public static void close(H5File h5File) throws Hdf5Exception
    {
        try
        {
            h5File.close();
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to close HDF5 file", ex);
        }

    }



    public static Group getRootGroup(H5File h5File) throws Hdf5Exception
    {
        Group root = (Group) ( (javax.swing.tree.DefaultMutableTreeNode) h5File.getRootNode()).getUserObject();

        if (root == null)
        {
            throw new Hdf5Exception("Failed to obtain root group of HDF5 file: "+ h5File.getFilePath());
        }

        return root;
    }



    public static void main(String[] args)
    {
        Hdf5Utils hdf5utils = new Hdf5Utils();
    }
}
