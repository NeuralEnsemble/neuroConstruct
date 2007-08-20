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

package ucl.physiol.neuroconstruct.gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * GUI element to allow easy linking between neuroConstruct tabs
 *
 * @author Padraig Gleeson
 * @version 1.0.6
 */

@SuppressWarnings("serial")

public class ClickLink extends JLabel
{
    String text  = null;
    public ClickLink(String text, String tip)
    {
        super(text);
        this.text = text;

        this.setToolTipText("<html>"+GeneralUtils.wrapLine(tip, "<br>", 100)+"</html>");

        //this.setBackground(Color.darkGray);

        this.addMouseListener(new MouseListener()
        {
            Font originalFont = getFont();

            public void mouseClicked(MouseEvent e)
            {
                //System.out.println("mouseClicked");
                //setText("Ouch");
            };

            public void mousePressed(MouseEvent e)
            {};

            public void mouseReleased(MouseEvent e)
            {};

            public void mouseEntered(MouseEvent e)
            {
                setForeground(Color.lightGray.darker());
            };

            public void mouseExited(MouseEvent e)
            {
                //System.out.println("Exited");
                setFont(originalFont);
                setForeground(Color.black);
            };

        });
    }

    public String getName()
    {
        return text;
    }

    public static void main(String[] args)
    {
        ClickLink cl = new ClickLink("Click it!", "<html>Go ahead, have a <b>click</b></html>");
        new ClickLink("No! Me!", "Go ahead, <b>click this instead</b>");

        //String favouredLookAndFeel = MainApplication.getFavouredLookAndFeel();
         try
         {
             //Object[] opts = new Object[]{cl2};
             Object[] opts = new Object[]{"optzzz"};


             JOptionPane.showInputDialog(null, cl, "Pick it", JOptionPane.QUESTION_MESSAGE , null,opts, opts[0]);

         }

         catch (Exception ex)
         {
             ex.printStackTrace();
        }

    }
}
