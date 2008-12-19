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

package ucl.physiol.neuroconstruct.pynn;

import java.io.*;


import ucl.physiol.neuroconstruct.cell.*;
import ucl.physiol.neuroconstruct.cell.utils.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * A single PyNN morphology file from a Cell object
 *
 * @author Padraig Gleeson
 *  
 */

public class PynnMorphologyGenerator
{
    ClassLogger logger = new ClassLogger("PynnMorphologyGenerator");

    Cell cell = null;

    File cellFile = null;
    
    Project project = null;
    
    

    private PynnMorphologyGenerator()
    {
    }


    public PynnMorphologyGenerator(Cell cell,
                                      Project project,
                                      File dirForFile)
    {
        logger.logComment("PynnMorphologyGenerator created for: " + cell.toString());
        this.cell = cell;

        StringBuffer spaceLessName = new StringBuffer();

        this.project = project;

        for (int i = 0; i < cell.getInstanceName().length(); i++)
        {
            char c = cell.getInstanceName().charAt(i);
            if (c != ' ') spaceLessName.append(c);
        }

        cellFile = new File(dirForFile, spaceLessName + ".xml");


    }




    public void generateFiles() throws PynnException
    {
        logger.logComment("Starting generation of template files: " + cellFile);
/*
        FileWriter fwCell = null;
        FileWriter fwMemb = null;
        try
        {
            fwCell = new FileWriter(cellFile);

            //fwCell.write(PsicsFileManager.getFileHeader());
            
            fwCell.write(this.getMainMorphology());

            fwCell.flush();
            fwCell.close();
        }
        catch (Exception ex)
        {
            logger.logError("Error writing to file: " + cellFile, ex);
            try
            {
                fwCell.flush();
                fwCell.close();
            }
            catch (IOException ex1)
            {
            }
            throw new PsicsException("Error writing to file: " + cellFile, ex);

        }
        try
        {
            fwMemb = new FileWriter(membFile);

            //fwMemb.write(PsicsFileManager.getFileHeader());
            
            fwMemb.write(this.getMembraneProps());

            fwMemb.flush();
            fwMemb.close();
        }
        catch (Exception ex)
        {
            logger.logError("Error writing to file: " + membFile, ex);
            try
            {
                fwMemb.flush();
                fwMemb.close();
            }
            catch (IOException ex1)
            {
            }
            throw new PsicsException("Error writing to file: " + membFile, ex);

        }*/

    }
    
/*
    private String getMainMorphology()
    {
        logger.logComment("calling getMainMorphology");
        StringBuffer response = new StringBuffer();
        
        SimpleXMLElement cellMorph = new SimpleXMLElement("CellMorphology");
        
        
        SimpleXMLAttribute id = new SimpleXMLAttribute("id", cell.getInstanceName());
        cellMorph.addAttribute(id);
        Vector<Segment> segs = cell.getAllSegments();
        //Segment lastSeg = null;
        
        for(Segment seg: segs)
        {
            SimpleXMLElement distPoint = new SimpleXMLElement("Point");
            Point3f prox = seg.getStartPointPosition();
            Point3f dist = seg.getEndPointPosition();
            Segment parent = seg.getParentSegment();
            
            // <Point parent="p0" id="p1" x="1000" y="0" z = "0" r="0.5"/>
            
            String parentId = null;
            
            //if (parent != null)
            //{    
            Point3f connectionPointParent = null;
            
            if (parent != null)
                connectionPointParent = parent.getPointAlong(seg.getFractionAlongParent());
            else
                connectionPointParent = seg.getStartPointPosition();

            boolean minor = false;

            if (!connectionPointParent.equals(prox))
            {

                GuiUtils.showErrorMessage(logger,
                                      "Error. Cell: "+cell+" is discontinuous, such cells are not supported in the mapping to PSICS yet!\n" +
                                      "Segment: "+ seg+"\nis not connected to the point "+seg.getFractionAlongParent()+" along parent segment: "+ parent,
                                      null, null);

                return "";
            }
            float connectionPointRadius = -1;

            if (parent==null)
            {
                connectionPointRadius = seg.getSegmentStartRadius();
            }
            else if (seg.getFractionAlongParent()==0)
            {
                connectionPointRadius = parent.getSegmentStartRadius();
            }
            else if(seg.getFractionAlongParent()==1)
            {
                connectionPointRadius = parent.getRadius();
            }
            else if(parent.isSpherical())
            {
                connectionPointRadius = parent.getRadius();
            }
            else
            {
                GuiUtils.showErrorMessage(logger,
                                      "Error. Cell has a segment connected between the end points of a parent segment, such cells are not supported in the mapping to PSICS yet!\n" +
                                      "Segment: "+ seg+"\nis not connected to the 0 or 1 point along (cylindrical) parent segment: "+ parent,
                                      null, null);

                return "";
            }



            if (connectionPointRadius==seg.getSegmentStartRadius() && parent!=null)
            {
                // use existing parent point...
                parentId = parent.getSegmentName();
            }
            else
            {
                if (seg.isSpherical())
                {
                    // just use distal point...
                }
                else
                {
                    SimpleXMLElement proxPoint = new SimpleXMLElement("Point");

                    String newPointId = seg.getSegmentName();
                        
                    if(parent!=null)
                    {
                        proxPoint.addAttribute(new SimpleXMLAttribute("parent", parent.getSegmentName()));
                        newPointId = seg.getSegmentName()+"_minor";
                    }
                    else
                    {
                        newPointId = newPointId+"_root";
                    }

                    SimpleXMLAttribute segIdProx = new SimpleXMLAttribute("id", newPointId);

                    proxPoint.addAttribute(segIdProx);

                    parentId = newPointId;

                    SimpleXMLAttribute xDistProx = new SimpleXMLAttribute("x", connectionPointParent.x+"");
                    SimpleXMLAttribute yDistProx = new SimpleXMLAttribute("y", connectionPointParent.y+"");
                    SimpleXMLAttribute zDistProx = new SimpleXMLAttribute("z", connectionPointParent.z+"");
                    SimpleXMLAttribute rDistProx = new SimpleXMLAttribute("r", seg.getSegmentStartRadius()+"");

                    proxPoint.addAttribute(xDistProx);
                    proxPoint.addAttribute(yDistProx);
                    proxPoint.addAttribute(zDistProx);
                    proxPoint.addAttribute(rDistProx);
                    if(parent!=null)
                    {
                        SimpleXMLAttribute minorProx = new SimpleXMLAttribute("minor", "true");
                        proxPoint.addAttribute(minorProx);
                    }


                    cellMorph.addChildElement(proxPoint);
                    cellMorph.addContent("\n    ");
                }


            }
            
            //}
            
            if (parentId!=null)
            {
                distPoint.addAttribute(new SimpleXMLAttribute("parent", parentId));
            }
                
            
            SimpleXMLAttribute segIdDist = new SimpleXMLAttribute("id", seg.getSegmentName());
            SimpleXMLAttribute xDist = new SimpleXMLAttribute("x", dist.x+"");
            SimpleXMLAttribute yDist = new SimpleXMLAttribute("y", dist.y+"");
            SimpleXMLAttribute zDist = new SimpleXMLAttribute("z", dist.z+"");
            SimpleXMLAttribute rDist = new SimpleXMLAttribute("r", seg.getRadius()+"");
            
            distPoint.addAttribute(segIdDist);
            distPoint.addAttribute(xDist);
            distPoint.addAttribute(yDist);
            distPoint.addAttribute(zDist);
            distPoint.addAttribute(rDist);
            
            
            cellMorph.addChildElement(distPoint);
            cellMorph.addContent("\n    ");
            
        
        }
            
        cellMorph.addContent("\n");
        
        response.append(cellMorph.getXMLString("", false));
        

        return response.toString();
    }*/


    public File getCellFile()
    {
        return cellFile;
    }

    




    public static void main(String[] args)
    {
        try
        {
            Project testProj = Project.loadProject(new File("models/BioMorph/BioMorph.neuro.xml"),
                                                   new ProjectEventListener()
            {
                public void tableDataModelUpdated(String tableModelName)
                {};

                public void tabUpdated(String tabName)
                {};
                public void cellMechanismUpdated()
                {
                };

            });

            //SimpleCell cell = new SimpleCell("DummyCell");
            //ComplexCell cell = new ComplexCell("DummyCell");

            Cell cell = testProj.cellManager.getCell("LongCellDelayLine");

            //File f = new File("/home/padraig/temp/tempNC/NEURON/PatTest/basics/");
            File f = new File("../temp");

            PynnMorphologyGenerator cellTemplateGenerator1 = new PynnMorphologyGenerator(cell,testProj,
                f);

            cellTemplateGenerator1.generateFiles();

            System.out.println("Generated: " + cellTemplateGenerator1.getCellFile().getAbsolutePath());

            System.out.println(CellTopologyHelper.printDetails(cell, null));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
