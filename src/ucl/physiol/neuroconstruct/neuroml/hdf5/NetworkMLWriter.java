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

package ucl.physiol.neuroconstruct.neuroml.hdf5;


import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;
import java.io.File;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import ucl.physiol.neuroconstruct.neuroml.NetworkMLReader;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import ucl.physiol.neuroconstruct.project.Project;
import ucl.physiol.neuroconstruct.project.GeneratedCellPositions;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections;
import java.util.Iterator;
import java.util.ArrayList;
import ucl.physiol.neuroconstruct.project.PositionRecord;
import ucl.physiol.neuroconstruct.project.GeneratedNetworkConnections.SingleSynapticConnection;

/**
 * Utilities file for generating NetworkML HDF5 files
 *
 * @author Padraig Gleeson
 *
 */

public class NetworkMLWriter
{
    private static ClassLogger logger = new ClassLogger("NetworkMLWriter");

    public static int POP_COLUMN_NUM = 4;
    public static int PROJ_COLUMN_NUM = 7;


    public NetworkMLWriter()
    {
        super();
    }

    public static void createNetworkMLH5file(File file,
                                             GeneratedCellPositions gcp,
                                             GeneratedNetworkConnections gnc) throws Hdf5Exception
    {

        H5File h5File = Hdf5Utils.createH5file(file);

        Hdf5Utils.open(h5File);

        Group root = Hdf5Utils.getRootGroup(h5File);
        Group netmlGroup = null;
        Group popsGroup = null;
        Group projsGroup = null;
        Group inputsGroup = null;

        try
        {
            netmlGroup = h5File.createGroup("networkml", root);
            popsGroup = h5File.createGroup("populations", netmlGroup);
            projsGroup = h5File.createGroup("projections", netmlGroup);
            inputsGroup = h5File.createGroup("inputs", netmlGroup);
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to create group in HDF5 file: "+ h5File.getFilePath());
        }




        Iterator<String> cellGroups = gcp.getNamesGeneratedCellGroups();

        while(cellGroups.hasNext())
        {
            String cg = cellGroups.next();

            ArrayList<PositionRecord> posRecs = gcp.getPositionRecords(cg);

            try
            {
                Group popGroup = h5File.createGroup("population_"+cg, popsGroup);

                Datatype dtype = getPopDatatype(h5File);

                long[] dims2D = {posRecs.size(), POP_COLUMN_NUM};

                float[] posArray = new float[posRecs.size() * POP_COLUMN_NUM];


                for (int i=0; i<posRecs.size(); i++)
                {
                    PositionRecord p = posRecs.get(i);

                    posArray[i * POP_COLUMN_NUM + 0] = p.cellNumber;

                    posArray[i * POP_COLUMN_NUM + 1] = p.x_pos;
                    posArray[i * POP_COLUMN_NUM + 2] = p.y_pos;
                    posArray[i * POP_COLUMN_NUM + 3] = p.z_pos;

                }


                Dataset dataset = h5File.createScalarDS
                    (cg, popGroup, dtype, dims2D, null, null, 0, posArray);


                /*
                long[] attrDims = {1}; // 1D of size two
int[] attrValue = {3}; // attribute value

                Datatype dtype = h5File.createDatatype(
            Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE);

Attribute attr = new Attribute("data range", dtype, attrDims);
attr.setValue(attrValue); // set the attribute value

            //h5File.writeAttribute(popGroup, attr, true);*/


            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }
        }





        Iterator<String> nCs = gnc.getNamesNetConnsIter();

        while(nCs.hasNext())
        {
            String nc = nCs.next();

            ArrayList<SingleSynapticConnection> conns = gnc.getSynapticConnections(nc);

            try
            {
                Group projGroup = h5File.createGroup("projection_" + nc, projsGroup);

                Datatype dtype = getProjDatatype(h5File);

                long[] dims2D = {conns.size(), PROJ_COLUMN_NUM};

                float[] projArray = new float[conns.size() * PROJ_COLUMN_NUM];

                for (int i = 0; i < conns.size(); i++)
                {
                    SingleSynapticConnection conn = conns.get(i);

                    projArray[i * PROJ_COLUMN_NUM + 0] = i;

                    projArray[i * PROJ_COLUMN_NUM + 1] = conn.sourceEndPoint.cellNumber;
                    projArray[i * PROJ_COLUMN_NUM + 2] = conn.sourceEndPoint.location.getSegmentId();
                    projArray[i * PROJ_COLUMN_NUM + 3] = conn.sourceEndPoint.location.getFractAlong();

                    projArray[i * PROJ_COLUMN_NUM + 4] = conn.targetEndPoint.cellNumber;
                    projArray[i * PROJ_COLUMN_NUM + 5] = conn.targetEndPoint.location.getSegmentId();
                    projArray[i * PROJ_COLUMN_NUM + 6] = conn.targetEndPoint.location.getFractAlong();




                }

                Dataset dataset = h5File.createScalarDS
                    (nc, projGroup, dtype, dims2D, null, null, 0, projArray);

            }
            catch (Exception ex)
            {
                throw new Hdf5Exception("Failed to create group in HDF5 file: " + h5File.getFilePath(), ex);
            }
        }

        //h5File.

        Hdf5Utils.close(h5File);

        logger.logComment("Created file: " + file, true);
        logger.logComment("Size: " + file.length()+" bytes", true);
    }

    public static Datatype getPopDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {

            Datatype popDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return popDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get pop datatype in HDF5 file: " + h5File.getFilePath());

        }

    }

    public static Datatype getProjDatatype(H5File h5File) throws Hdf5Exception
    {
        try
        {
            Datatype projDataType = h5File.createDatatype(Datatype.CLASS_FLOAT, 4, Datatype.NATIVE, -1);
            return projDataType;
        }
        catch (Exception ex)
        {
            throw new Hdf5Exception("Failed to get projection datatype in HDF5 file: " + h5File.getFilePath());

        }

    }



    public static void main(String[] args)
    {
        File h5File = new File("../temp/net.h5");
        try
        {
            System.setProperty("java.library.path", System.getProperty("java.library.path")+":/home/padraig/neuroConstruct");
            
            logger.logComment("Sys prop: "+System.getProperty("java.library.path"), true);
            
            Project testProj = Project.loadProject(new File("examples/Ex9-GranCellLayer/Ex9-GranCellLayer.neuro.xml"),
                                                   null);

            //File nmlFile = new File("examples/Ex9-GranCellLayer/savedNetworks/600.nml");
            //File nmlFile = new File("examples/Ex9-GranCellLayer/savedNetworks/75.nml");
            //File nmlFile = new File("../copynCmodels/Parallel/savedNetworks/50000.nml");
            //File nmlFile = new File("../copynCmodels/NewGranCellLayer/savedNetworks/87000Rand.nml");
            File nmlFile = new File("../temp/test.nml");
            //File nmlFile = new File("../copynCmodels/Parallel/savedNetworks/50000.nml");


            logger.logComment("Loading netml cell from " + nmlFile.getAbsolutePath(), true);

            GeneratedCellPositions gcp = new GeneratedCellPositions(testProj);
            GeneratedNetworkConnections gnc = new GeneratedNetworkConnections(testProj);

            FileInputStream instream = null;
            InputSource is = null;

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            NetworkMLReader nmlBuilder = new NetworkMLReader(gcp, gnc);
            xmlReader.setContentHandler(nmlBuilder);

            instream = new FileInputStream(nmlFile);

            is = new InputSource(instream);

            xmlReader.parse(is);

            logger.logComment("Cells: " + gcp.getNumberInAllCellGroups(), true);
            logger.logComment("Net conn num: " + gnc.getNumberSynapticConnections(GeneratedNetworkConnections.ANY_NETWORK_CONNECTION), true);

            NetworkMLWriter.createNetworkMLH5file(h5File, gcp, gnc);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }


    }
}
