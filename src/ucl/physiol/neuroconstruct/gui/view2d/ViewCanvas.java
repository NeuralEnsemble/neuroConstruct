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

package ucl.physiol.neuroconstruct.gui.view2d;

import java.awt.*;
import ucl.physiol.neuroconstruct.utils.ClassLogger;
import java.util.*;
import ucl.physiol.neuroconstruct.project.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.*;

import ucl.physiol.neuroconstruct.gui.view2d.View2DPlane.*;
import java.awt.event.*;
import java.text.*;
import ucl.physiol.neuroconstruct.j3D.*;
import javax.vecmath.*;

/**
 * Canvas for painting 2D view on
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class ViewCanvas extends Canvas
{
    ClassLogger logger = new ClassLogger("ViewCanvas");

    Vector<CellPaintInfo> cellPaintInfoList = new Vector<CellPaintInfo>();

    ViewingDirection viewingDir = Z_X_NEGY_DIR;

    public static final String X_COORD = "X";
    public static final String Y_COORD = "Y";
    public static final String Z_COORD = "Z";


    public static final ViewingDirection X_Y_NEGZ_DIR = new ViewingDirection("X, Y plane, viewing down Z axis");
    public static final ViewingDirection X_Y_POSZ_DIR = new ViewingDirection("X, Y plane, viewing up Z axis");

    public static final ViewingDirection Y_Z_NEGX_DIR = new ViewingDirection("Y, Z plane, viewing down X axis");
    public static final ViewingDirection Y_Z_POSX_DIR = new ViewingDirection("Y, Z plane, viewing up X axis");

    public static final ViewingDirection Z_X_NEGY_DIR = new ViewingDirection("Z, X plane, viewing down Y axis");
    public static final ViewingDirection Z_X_POSY_DIR = new ViewingDirection("Z, X plane, viewing up Y axis");


    /**
     * There are 3 coord sets:
     * 3DSpace:  coords of the original points (cell positions in 3D)
     * 2DMappedSpace: coords of the 2 dims of the 3d points which are relevant for this
     * ScreenSpace: coords as displayed on the screen (pixel/int coords)
     */

    // these are max and min point vals in 2DMappedSpace, i.e. max extent of 3D points when mapped to
    // the 2D space
    float minPointX2DMappedSpace;
    float maxPointX2DMappedSpace;

    float minPointY2DMappedSpace;
    float maxPointY2DMappedSpace;

    float extraViewFactor = 0.1f;

    int circleWidth = 8;

    View2DPlane view2D = null;

    /**
     * If the colours are within this tolerance, don't bother updating...
     */
    int colorTolerance = 30;


    DecimalFormat df = new DecimalFormat();

    public ViewCanvas(View2DPlane view2D)
    {
        logger.logComment("new ViewCanvas...");

        this.view2D = view2D;

        df.applyLocalizedPattern("0.000");
        df.setGroupingSize(1000);


        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                //logger.logComment("Event: "+e);
                this_mouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e)
            {
                this_mouseMoved(e);
            }

        });

    }

    public void setTolerance(int colorTolerance)
    {
        this.colorTolerance = colorTolerance;
    }

    public void setup(ArrayList<PositionRecord> positions, ViewingDirection viewingDir)
    {
        this.viewingDir = viewingDir;

        // as we could be resetting...
        minPointX2DMappedSpace = Float.MAX_VALUE;
        maxPointX2DMappedSpace = -1*Float.MAX_VALUE;

        minPointY2DMappedSpace = Float.MAX_VALUE;
        maxPointY2DMappedSpace = -1*Float.MAX_VALUE;


        for (int i = 0; i < positions.size(); i++)
        {
            PositionRecord pos = positions.get(i);
            float x2DMappedSpace = get2DMappedSpaceX(pos.getPoint());
            float y2DMappedSpace = get2DMappedSpaceY(pos.getPoint());

            logger.logComment(pos+ " on screen: ("+x2DMappedSpace+", "+y2DMappedSpace+")");

            if (x2DMappedSpace<minPointX2DMappedSpace) minPointX2DMappedSpace = x2DMappedSpace;
            if (x2DMappedSpace>maxPointX2DMappedSpace) maxPointX2DMappedSpace = x2DMappedSpace;
            if (y2DMappedSpace<minPointY2DMappedSpace) minPointY2DMappedSpace = y2DMappedSpace;
            if (y2DMappedSpace>maxPointY2DMappedSpace) maxPointY2DMappedSpace = y2DMappedSpace;

            CellPaintInfo cellPaintInfo = null;

            if (cellPaintInfoList.size()>pos.cellNumber && cellPaintInfoList.get(pos.cellNumber)!=null)
            {
                cellPaintInfo = cellPaintInfoList.get(pos.cellNumber);
            }
            else
            {
                cellPaintInfo = new CellPaintInfo();
            }

            cellPaintInfo.x_pos = x2DMappedSpace;
            cellPaintInfo.y_pos = y2DMappedSpace;
            if (cellPaintInfoList.size()<=pos.cellNumber)
                cellPaintInfoList.setSize(pos.cellNumber+1);

            logger.logComment("Size of cellPaintInfoList: "+cellPaintInfoList.size());

            cellPaintInfoList.set(pos.cellNumber, cellPaintInfo);

        }
        float pointsWidth = (maxPointX2DMappedSpace - minPointX2DMappedSpace);
        minPointX2DMappedSpace = minPointX2DMappedSpace - extraViewFactor * pointsWidth;
        maxPointX2DMappedSpace = maxPointX2DMappedSpace + extraViewFactor * pointsWidth;

        float pointsHeight = (maxPointY2DMappedSpace - minPointY2DMappedSpace);

        minPointY2DMappedSpace = minPointY2DMappedSpace - (extraViewFactor * pointsHeight);
        maxPointY2DMappedSpace = maxPointY2DMappedSpace + (extraViewFactor * pointsHeight);

        logger.logComment("Max, min X: "+ minPointX2DMappedSpace + " -> "+ maxPointX2DMappedSpace);
        logger.logComment("Max, min Y: "+ minPointY2DMappedSpace + " -> "+ maxPointY2DMappedSpace);
    }


    public float get2DMappedSpaceX(Point3f pos3DSpace)
    {
        if (viewingDir.equals(X_Y_NEGZ_DIR))
            return pos3DSpace.x;
        else if (viewingDir.equals(X_Y_POSZ_DIR))
            return -1*pos3DSpace.x;
        else if (viewingDir.equals(Z_X_NEGY_DIR))
            return pos3DSpace.z;
        else if (viewingDir.equals(Z_X_POSY_DIR))
            return -1*pos3DSpace.z;
        else if (viewingDir.equals(Y_Z_NEGX_DIR))
            return pos3DSpace.y;
        else if (viewingDir.equals(Y_Z_POSX_DIR))
            return -1*pos3DSpace.y;

        return Float.NaN;
    }


    public float get2DMappedSpaceY(Point3f pos3DSpace)
    {
        if (viewingDir.equals(X_Y_NEGZ_DIR))
            return pos3DSpace.y;
        else if (viewingDir.equals(X_Y_POSZ_DIR))
            return pos3DSpace.y;
        else if (viewingDir.equals(Z_X_NEGY_DIR))
            return pos3DSpace.x;
        else if (viewingDir.equals(Z_X_POSY_DIR))
            return pos3DSpace.x;
        else if (viewingDir.equals(Y_Z_NEGX_DIR))
            return pos3DSpace.z;
        else if (viewingDir.equals(Y_Z_POSX_DIR))
            return pos3DSpace.z;

        return Float.NaN;
    }


    protected String get3DSpaceCoord(float x2DMappedSpace, float y2DMappedSpace, String coord)
    {
        if (viewingDir.equals(X_Y_NEGZ_DIR))
            return coord.equals(X_COORD)? df.format(x2DMappedSpace) :
                (coord.equals(Y_COORD)? df.format(y2DMappedSpace) : "Z");

        else if (viewingDir.equals(X_Y_POSZ_DIR))
            return coord.equals(X_COORD)? df.format(-1*x2DMappedSpace)+"" :
                (coord.equals(Y_COORD)? df.format(y2DMappedSpace)+"" : "Z");

        else if (viewingDir.equals(Z_X_NEGY_DIR))
            return coord.equals(X_COORD)? df.format(y2DMappedSpace) :
                (coord.equals(Z_COORD)? df.format(x2DMappedSpace)+"" : "Y");

        else if (viewingDir.equals(Z_X_POSY_DIR))
            return coord.equals(X_COORD)? df.format(y2DMappedSpace) :
                (coord.equals(Z_COORD)? df.format(-1*x2DMappedSpace)+"" : "Y");

        else if (viewingDir.equals(Y_Z_NEGX_DIR))
            return coord.equals(Z_COORD)? df.format(y2DMappedSpace) :
                (coord.equals(Y_COORD)? df.format(x2DMappedSpace)+"" : "X");

        else if (viewingDir.equals(Y_Z_POSX_DIR))
            return coord.equals(Z_COORD)? df.format(-1*y2DMappedSpace) :
                (coord.equals(Y_COORD)? df.format(x2DMappedSpace)+"" : "X");


        return "Can't get3DSpaceCoord...";
    }

    public float getWidth2DMappedSpace()
    {
        int screenSpaceWidth = this.getWidth();
        int screenSpaceHeight = this.getHeight();

        float preferredWidth2DMappedSpace =
            (this.maxPointX2DMappedSpace - this.minPointX2DMappedSpace);

        float preferredHeight2DMappedSpace =
            (this.maxPointY2DMappedSpace - this.minPointY2DMappedSpace);

        //logger.logComment("Preferred size shown: ("+ preferredWidth2DMappedSpace+" x "
        //    + preferredHeight2DMappedSpace + ")");

        if ((screenSpaceWidth/preferredWidth2DMappedSpace) <=
            (screenSpaceHeight/preferredHeight2DMappedSpace))
        {
            //logger.logComment("Using width to scale scene...");
            return preferredWidth2DMappedSpace;
        }
        else
        {

            float factor = (float)screenSpaceWidth/(float)screenSpaceHeight;
           // logger.logComment("Using height to scale scene, factor: "+factor+"...");
            return preferredHeight2DMappedSpace * factor;
        }

    }

    public float getHeight2DMappedSpace()
    {
        int screenSpaceWidth = this.getWidth();
        int screenSpaceHeight = this.getHeight();

        float preferredWidth2DMappedSpace =
            (this.maxPointX2DMappedSpace - this.minPointX2DMappedSpace);

        float preferredHeight2DMappedSpace =
            (this.maxPointY2DMappedSpace - this.minPointY2DMappedSpace);

        //logger.logComment("Preferred size shown: ("+ preferredWidth2DMappedSpace+" x "
        //    + preferredHeight2DMappedSpace + ")");

        if ((screenSpaceWidth/preferredWidth2DMappedSpace) <=
            (screenSpaceHeight/preferredHeight2DMappedSpace))
        {
            float factor = (float)screenSpaceHeight/(float)screenSpaceWidth;
            //logger.logComment("Height: Using width to scale scene, factor: "+factor);
            return preferredWidth2DMappedSpace * factor;
        }
        else
        {
            //logger.logComment("Height: Using height to scale scene");
            return preferredHeight2DMappedSpace;
        }

    }





    public int getScreenSpaceX(double x2DMappedSpace)
    {
        logger.logComment("getScreenSpaceX for "+ x2DMappedSpace);

        logger.logComment("2DMappedSpace: "+getWidth2DMappedSpace() +" wide by "+getHeight2DMappedSpace()+ " high");

        logger.logComment("minX2DMappedSpace: "+this.minPointX2DMappedSpace);

        float addedWidthEachEnd = (getWidth2DMappedSpace() - (this.maxPointX2DMappedSpace - this.minPointX2DMappedSpace))/2;

        double fractionAlong = (x2DMappedSpace - (minPointX2DMappedSpace - addedWidthEachEnd)) / this.getWidth2DMappedSpace();
        logger.logComment("fractionAlong "+fractionAlong);

        int screenSpaceWidth = this.getWidth();

        logger.logComment("Screen: ("+ getWidth()+", " + getHeight() + ")");


        double doubNumPixelsScreenSpace = (screenSpaceWidth - 1) * fractionAlong;

        logger.logComment("doubVal "+doubNumPixelsScreenSpace);

        int numPixelsScreenSpace = (int) Math.floor(doubNumPixelsScreenSpace);

        logger.logComment("getScreenSpaceX: "+numPixelsScreenSpace+"\n");
        return numPixelsScreenSpace;

    }


    public int getScreenSpaceY(double y2DMappedSpace)
    {
        logger.logComment("----  getScreenSpaceY for "+ y2DMappedSpace);


        logger.logComment("2DMappedSpace: "+getWidth2DMappedSpace() +" wide by "+getHeight2DMappedSpace()+ " high");


        logger.logComment("maxY2DMappedSpaceShown: "+ this.maxPointY2DMappedSpace);

        float addedHeightEachEnd = (getHeight2DMappedSpace() - (this.maxPointY2DMappedSpace - this.minPointY2DMappedSpace))/2;


        double fractionDown = (maxPointY2DMappedSpace - y2DMappedSpace + addedHeightEachEnd) / getHeight2DMappedSpace();

        logger.logComment("fractionDown "+fractionDown);

        int screenSpaceHeight = this.getHeight();

        logger.logComment("Screen: ("+ getWidth()+", " + getHeight() + ")");

        double doubNumPixelsScreenSpace = (screenSpaceHeight - 1) * fractionDown;

        logger.logComment("doubNumPixelsScreenSpace: "+doubNumPixelsScreenSpace);

        int numPixelsScreenSpace = (int) Math.floor(doubNumPixelsScreenSpace);

        logger.logComment("-----  intVal "+numPixelsScreenSpace);

        return numPixelsScreenSpace;

    }

    /*
    private float getMinX2DMappedSpaceShown()
    {
        return minPointX2DMappedSpace
            - ( (this.maxPointX2DMappedSpace - this.minPointX2DMappedSpace) * this.extraViewFactor);
    }

    private float getMaxY2DMappedSpaceShown()
    {
        return maxPointY2DMappedSpace
            + ( (this.maxPointY2DMappedSpace - this.minPointY2DMappedSpace) * this.extraViewFactor);
    }
*/

    public void updateColour(Color colour, String cellGroup, int cellNumber, boolean refresh)
    {
        logger.logComment("updateColor: " + colour + ", " + cellGroup + ", " + cellNumber  + ", " +
                          refresh);

        CellPaintInfo cpi = this.cellPaintInfoList.elementAt(cellNumber);

        if (cpi != null)
        {
            // only update if the colour difference exceeds the tolerance
            if (Math.abs(cpi.color.getRed() - colour.getRed()) > this.colorTolerance ||
                Math.abs(cpi.color.getGreen() - colour.getGreen()) > this.colorTolerance ||
                Math.abs(cpi.color.getBlue() - colour.getBlue()) > this.colorTolerance)
            {

                logger.logComment("Updating " + cellNumber);
                cpi.color = colour;
            }
            else
            {
                logger.logComment("Not diff for "+ cellNumber+ " at colour: "+colour+", cpi.color: "+cpi.color);
            }

            if (refresh)
            {
                //System.out.println("repainting...");
                repaint();
            }
            else
            {
                //System.out.println("Not repainting...");

            }
        }

        else
        {
            System.out.println("No cpi: (colour: " + colour + ", " + cellGroup + ", " + cellNumber);
        }

    }




    @Override
    public void repaint()
    {
        logger.logComment("Repainting...");
        super.repaint();
    }




    public void setScene(Graphics g)
    {
        int width = this.getWidth();
        int height = this.getHeight();

        logger.logComment("width: "+ width+ ", height: "+height);

        g.setColor(Color.white);

        g.fillRect(0, 0, width, height);

        g.setColor(Color.black);

        g.drawRect(0, 0, width-1, height-1);

    }

    @Override
    public void paint(Graphics g)
    {
        //long startTime = System.currentTimeMillis();

        logger.logComment("");
        logger.logComment(">>>>>>>>>>>    Paint called, view dir: "+ this.viewingDir);

        setScene(g);


        Graphics2D g2 = (Graphics2D) g;


        if (cellPaintInfoList!= null)
        {
            for (int i = 0; i < cellPaintInfoList.size(); i++)
            {
                if (cellPaintInfoList.get(i)!=null)
                {
                    int radius = cellPaintInfoList.get(i).radius;
                    g2.setColor(cellPaintInfoList.get(i).color);

                    g2.fillOval(this.getScreenSpaceX(cellPaintInfoList.get(i).x_pos) - radius,
                           this.getScreenSpaceY(cellPaintInfoList.get(i).y_pos) - radius,
                           radius * 2,
                           radius * 2);
                }


            }
        }

        if (this.view2D.showAxes())
        {
            ///logger.logComment("Adding axes" , true);
            int radius = 1;

            g2.setColor(Color.black);

            g2.fillOval(this.getScreenSpaceX(0) - radius,
                        this.getScreenSpaceY(0) - radius,
                        radius * 2,
                        radius * 2);

            g2.setColor(Utils3D.X_AXIS_COLOUR);

            Point3f endPoint = new Point3f(100,0,0);

            g2.drawLine(this.getScreenSpaceX(0), this.getScreenSpaceY(0),
                        this.getScreenSpaceX(this.get2DMappedSpaceX(endPoint)), this.getScreenSpaceY(this.get2DMappedSpaceY(endPoint)));

            g2.setColor(Utils3D.Y_AXIS_COLOUR);

            endPoint = new Point3f(0, 100, 0);

            g2.drawLine(this.getScreenSpaceX(0), this.getScreenSpaceY(0),
                        this.getScreenSpaceX(this.get2DMappedSpaceX(endPoint)), this.getScreenSpaceY(this.get2DMappedSpaceY(endPoint)));

            g2.setColor(Utils3D.Z_AXIS_COLOUR);

            endPoint = new Point3f(0, 0, 100);

            g2.drawLine(this.getScreenSpaceX(0), this.getScreenSpaceY(0),
                        this.getScreenSpaceX(this.get2DMappedSpaceX(endPoint)), this.getScreenSpaceY(this.get2DMappedSpaceY(endPoint)));




        }
    }




    public float get2DMappedSpaceX(int screenSpaceX)
    {
        int widthScreenSpace = this.getWidth();

        float addedWidthEachEnd = (getWidth2DMappedSpace() - (this.maxPointX2DMappedSpace - this.minPointX2DMappedSpace))/2;

        float x2DSpaceLengthOnePixel = (this.getWidth2DMappedSpace()) / (float) widthScreenSpace;

        float lenAlongX2DMappedSpace = (x2DSpaceLengthOnePixel * screenSpaceX) + (0.5f * x2DSpaceLengthOnePixel);

        return (lenAlongX2DMappedSpace + (this.minPointX2DMappedSpace - addedWidthEachEnd));

    }

    public float get2DMappedSpaceY(int screenSpaceY)
    {
        int heightScreenSpace = this.getHeight();

        float addedHeightEachEnd = (getHeight2DMappedSpace() - (this.maxPointY2DMappedSpace - this.minPointY2DMappedSpace))/2;

        float y2DSpaceLengthOnePixel = (this.getHeight2DMappedSpace()) / (float) heightScreenSpace;
        float lenAlongY2DMappedSpace = (y2DSpaceLengthOnePixel * screenSpaceY) + (0.5f * y2DSpaceLengthOnePixel);
        return (maxPointY2DMappedSpace + addedHeightEachEnd) - lenAlongY2DMappedSpace;

    }




    void this_mouseMoved(MouseEvent e)
    {
       int mouseXval = e.getX();
       int mouseYval = e.getY();

       //logger.logComment("pix x: "+ mouseXval+ ", pix y: "+ mouseYval);

       float x2DMappedSpace = get2DMappedSpaceX(mouseXval);
       float y2DMappedSpace = get2DMappedSpaceY(mouseYval);

       //logger.logComment("("+ doubleFormatterFull.format(x)
      //                            +", "+ doubleFormatterFull.format(y)+")");

      String coords= "("+ get3DSpaceCoord(x2DMappedSpace, y2DMappedSpace, X_COORD)
                   + ", " + get3DSpaceCoord(x2DMappedSpace, y2DMappedSpace, Y_COORD)
                   + ", " + get3DSpaceCoord(x2DMappedSpace, y2DMappedSpace, Z_COORD) + ")";


       setStatusBarText(this.viewingDir + ": "+ coords
                                  +", point on screen: ("+mouseXval
                                  +", "+ mouseYval+") ");

    }



    protected void setStatusBarText(String comment)
    {
        //plotFrame.setStatus(comment);

        logger.logComment(comment);

        this.view2D.setStatusBarText(comment);

    }


    public static void main(String[] args)
    {

        PlotterFrame frame = PlotManager.getPlotterFrame("Colours versus voltage", true, true);

        frame.setKeepDataSetColours(true);

        DataSet red = new DataSet("Red value", "Red versus voltage", "", "", "", "");
        DataSet blue = new DataSet("Blue value", "Blue versus voltage", "", "", "", "");
        DataSet green = new DataSet("Green value", "Green versus voltage", "", "", "", "");

        red.setGraphColour(Color.red);
        blue.setGraphColour(Color.blue);
        green.setGraphColour(Color.green);

        for (float x = 10; x < 100; x=x+1f)
        {
            Color c = SimulationRerunFrame.getColorBasedOnISI(x);
            red.addPoint(x, c.getRed());
            blue.addPoint(x, c.getBlue());
            green.addPoint(x, c.getGreen());
           // if ((x/10f)==(int)(x/10))
           // {
                String ref = "Colour at "+x+" ms, "+(1000/x)+" Hz: "+c.toString();
                DataSet newColorPoint = new DataSet(ref, ref, "", "", "", "");
                newColorPoint.addPoint(x, 10);
                newColorPoint.setGraphColour(c);
                newColorPoint.setGraphFormat(PlotCanvas.USE_CIRCLES_FOR_PLOT);
                frame.addDataSet(newColorPoint);
           // }
        }

        frame.addDataSet(red);
        frame.addDataSet(blue);
        frame.addDataSet(green);
    }




    public class CellPaintInfo
    {
        private float x_pos;
        private float y_pos;

        private int radius = 4;

        private Color color = Color.red;
    }



}
