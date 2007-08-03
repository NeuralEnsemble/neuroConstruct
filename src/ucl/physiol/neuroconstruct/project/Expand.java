package ucl.physiol.neuroconstruct.project;

import ucl.physiol.neuroconstruct.utils.*;

import java.io.*;
import java.util.Vector;
import ucl.physiol.neuroconstruct.cell.*;


public class Expand
{
    private static ClassLogger logger = new ClassLogger("Expand");
    
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

        doc.addTaggedElement(""+ handleWhitespaces(project.getProjectDescription()), "p");

        doc.addTaggedElement("Cells", "h2");

        Vector<Cell> cells = project.cellManager.getAllCells();

        for (Cell cell: cells)
        {

            doc.addTaggedElement("Cell: "+cell.getInstanceName(), "h3");
            doc.addTaggedElement(cell.getCellDescription(), "p");
            
        }

        doc.saveAsFile(htmlFile);
        
    }
    
    public static String handleWhitespaces(String text)
    {
        return GeneralUtils.replaceAllTokens(text, "\n", "<br/>");
    }

    public static void main(String[] args)
    {
        Expand expand = new Expand();
        
        try
        {
            
            Project testProj = Project.loadProject((new File("examples/Ex6-Cerebellum/Ex6-Cerebellum.neuro.xml")), 
                    new ProjectEventListener()
                {
                    public void tableDataModelUpdated(String tableModelName)
                    {};
                    
                    public void tabUpdated(String tabName)
                    {};
                    public void cellMechanismUpdated()
                    {};
                });
            
            File f = new File("../temp/proj.html");
            
            generateMainPage(testProj, f);
            

            logger.logComment("Created doc at: "+ f.getCanonicalPath());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
