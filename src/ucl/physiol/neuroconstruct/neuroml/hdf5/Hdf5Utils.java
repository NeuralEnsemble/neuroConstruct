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
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;

import ucl.physiol.neuroconstruct.neuroml.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;


/**
 * Utilities file for generating HDF5 files
 *
 * @author Padraig Gleeson
 *
 */

public class Hdf5Utils
{
    private static ClassLogger logger = new ClassLogger("Hdf5Utils");
    
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

    public static H5File openH5file(File file) throws Hdf5Exception
    {

        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        if (fileFormat == null)
        {
            throw new Hdf5Exception("Cannot find HDF5 FileFormat.");
        }

        try
        {
            H5File h5File = (H5File)fileFormat.open(file.getAbsolutePath(), FileFormat.READ);

            if (h5File == null)
            {
                throw new Hdf5Exception("Failed to open file:"+file);
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
        String name = "TenMillionSyn";
        File h5File = new File("../temp/"+name+".h5");
        File newNMLFile = new File("../temp/"+name+".nml");
        try
        {
            System.setProperty("java.library.path", System.getProperty("java.library.path")+":/home/padraig/neuroConstruct");
            
            logger.logComment("Sys prop: "+System.getProperty("java.library.path"), true);
            
            Project testProj = Project.loadProject(new File("examples/Ex9-GranCellLayer/Ex9-GranCellLayer.neuro.xml"),
                                                   null);



            GeneratedCellPositions gcp = testProj.generatedCellPositions;
            GeneratedNetworkConnections gnc = testProj.generatedNetworkConnections;

            int sizeCells = 10000;
            int sizeConns = 10000000;
            String preGrp = "Mossies";
            String postGrp = "Grans";
            
            Random r = new Random();
            
            for(int i=0;i<sizeCells;i++)
            {
                gcp.addPosition(preGrp, new PositionRecord(i, 
                                                       r.nextFloat()*1000, 
                                                       r.nextFloat()*1000, 
                                                       r.nextFloat()*1000));
                
                gcp.addPosition(postGrp, new PositionRecord(i, 
                                                            r.nextFloat()*1000, 
                                                            r.nextFloat()*1000, 
                                                            r.nextFloat()*1000));
            }

            for(int i=0;i<sizeConns;i++)
            {
                int pre = r.nextInt(sizeCells);
                int post = r.nextInt(sizeCells);
                gnc.addSynapticConnection("NetConn_"+preGrp+"_"+postGrp, 
                                        GeneratedNetworkConnections.MORPH_NETWORK_CONNECTION, 
                                        pre, 
                                          0, 
                                          0.5f, 
                                          post, 
                                          0, 
                                          0.5f, 
                                          0, 
                                          null);
            }

            logger.logComment("Cells: " + gcp.getNumberInAllCellGroups(), true);
            logger.logComment("Net conn num: " + gnc.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION), true);

            NetworkMLWriter.createNetworkMLH5file(h5File, gcp, gnc);
            
            if (true) System.exit(0);
            
            File fileSaved = null;
          

            fileSaved = testProj.saveNetworkStructure(newNMLFile,
                                                      false,
                                                      false,
                                                      testProj.simConfigInfo.getDefaultSimConfig().getName());
     

            logger.logComment("File saved: " + fileSaved.getCanonicalPath(), true);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }


    }
}
