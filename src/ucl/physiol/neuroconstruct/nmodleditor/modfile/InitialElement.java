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

package ucl.physiol.neuroconstruct.nmodleditor.modfile;

/**
 * nmodlEditor application software
 *
 * @author Padraig Gleeson
 * @version 1.0.4
 */

public class InitialElement extends ParsableEquationsElement
{

    public InitialElement(ModFileChangeListener changeListener)
    {
         super("INITIAL", changeListener);
    }


    public static void main(String args[])
    {
        ModFileChangeListener listener = new ModFileChangeListener(){
            public void modFileElementChanged(String modFileElementType)
            {
                System.out.println("Change in: "+ modFileElementType);
            }
            public void modFileChanged(){};
        };

        InitialElement ae = new InitialElement(listener);
        try
        {
            ae.addLine(": comm");
            ae.addLine("val =dim ");
            ae.addLine(": comm");
            ae.addLine("va=mm");
            ae.addLine("yyy=yy");
            System.out.println("---------    Internal vals: ");
            System.out.println(ae);

            String[] lines = ae.getUnformattedLines();
            for (int i = 0; i < lines.length; i++)
            {
                System.out.println("Line "+i+": "+lines[i]);
            }
        }
        catch (ModFileException ex)
        {
            ex.printStackTrace();
        }

    }

}
