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

package ucl.physiol.neuroconstruct.gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * GUI element to allow easy linking between neuroConstruct tabs
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class ClickLink extends JLabel
{
    String text  = null;
    Color prefColour = Color.black;
    
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
                setTempForeground(Color.lightGray.darker());
            };

            public void mouseExited(MouseEvent e)
            {
                //System.out.println("Exited");
                setFont(originalFont);
                setForeground(prefColour);
            };

        });
    }

    @Override
    public String getName()
    {
        return text;
    }

    
    public void setTempForeground(Color fg)
    {
        super.setForeground(fg);
    }
    
    @Override
    public void setForeground(Color fg)
    {
        super.setForeground(fg);
        this.prefColour = fg;
    }
    
    

    public static void main(String[] args)
    {
        ClickLink cl = new ClickLink("Click it!", "<html>Go ahead, have a <b>click em</b></html>");
        
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
