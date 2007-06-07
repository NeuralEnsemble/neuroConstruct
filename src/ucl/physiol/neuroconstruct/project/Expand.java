package ucl.physiol.neuroconstruct.project;

import ucl.physiol.neuroconstruct.utils.*;
import java.io.*;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.*;








public class Expand
{
    public Expand()
    {
    }


    public static void generateProjectView(Project project, File dir)
    {

    }

    public static void generateMainPage(Project project, File htmlFile)
    {
        SimpleHtmlDoc doc = new SimpleHtmlDoc();

        doc.addTaggedElement("Project: "+ project.getProjectFileName(), "h1");

        doc.addTaggedElement(""+project.getProjectDescription(), "p");

        doc.addTaggedElement("Cells", "h2");

        Vector<Cell> cells = project.cellManager.getAllCells();

        for (Cell cell: cells)
        {

            doc.addTaggedElement("Cell: "+cell.getInstanceName(), "p");
        }

        //doc.
    }

    public static void main(String[] args)
    {
        Expand expand = new Expand();
    }
}
