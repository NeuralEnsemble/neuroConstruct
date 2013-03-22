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

package ucl.physiol.neuroconstruct.gui.plotter;

import java.text.*;

import java.awt.*;
import java.awt.event.*;

import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.j3D.*;
import ucl.physiol.neuroconstruct.utils.*;

/**
 * Extenion of Canvas for plotting the graphs
 *
 * @author Padraig Gleeson
 *  
 */

@SuppressWarnings("serial")

public class PlotCanvas extends Canvas
{
    ClassLogger logger = new ClassLogger("PlotCanvas");

    // initial values...
   // private int width = 100;
   // private int height = 100;


    public final static String NORMAL_VIEW = "Normal view";
    public final static String INCLUDE_ORIGIN_VIEW = "Include Origin";
    public final static String CROPPED_VIEW = "Selection Only";
    public final static String STACKED_VIEW = "Plots stacked vertically";

    public final static String USER_SET_VIEW = "Custom view";

    String viewMode =  NORMAL_VIEW;

    boolean showAxes = true;
    boolean showAxisNumbering = true;
    boolean showAxisTicks = true;


    private boolean warnedAboutNaNInfinity = false;

    /**
     * If false, a graph of the same colour as an existing graph will be updated to a new colour
     */
    private boolean keepDataSetColours = false;

    //private boolean keepDataSetColours = false;


    private RasterOptions myRasterOptions = new RasterOptions();
    private SpikeAnalysisOptions spikeOptions = new SpikeAnalysisOptions();


    int nextColour = 1;

    /**
     * Extra area around plots to ensure points don't go to edge...
     */
    private double fractionToAdd = 0.1d;

    private double maxXScaleValue = 10;
    private double minXScaleValue = -10;
    private double maxYScaleValue = 10;
    private double minYScaleValue = -10;

    public final static String USE_CIRCLES_FOR_PLOT = "Circles";
    public final static String USE_LINES_FOR_PLOT = "Lines";
    public final static String USE_THICK_LINES_FOR_PLOT = "Thick lines";
    public final static String USE_CROSSES_FOR_PLOT = "Crosses";
    public final static String USE_POINTS_FOR_PLOT = "Points";
    public final static String USE_BARCHART_FOR_PLOT = "Bar chart";


    int mouseXval = 0;
    int mouseYval = 0;



    DecimalFormat doubleFormatterFull = new DecimalFormat();
    //DecimalFormat doubleFormatterShort = new DecimalFormat();

    DataSet[] dataSets = new DataSet[]{};

    PlotterFrame plotFrame = null;



    boolean selectingArea = false;

    double mouseSelectionStartX = 10;
    double mouseSelectionStartY = 10;
    double mouseSelectionFinishX = -10;
    double mouseSelectionFinishY = -10;


    public PlotCanvas(PlotterFrame plotFrame)
    {
        logger.logComment("Created new PlotCanvas");

        //this.logger.setThisClassVerbose(true);

        this.plotFrame = plotFrame;

        doubleFormatterFull.applyLocalizedPattern("0.0000");
        doubleFormatterFull.setGroupingSize(1000);

        //doubleFormatterShort.applyLocalizedPattern("0.00#");
        //doubleFormatterFull.setGroupingSize(1000);

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

        this.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent e){}

            public void mouseEntered(MouseEvent e){}

            public void mouseExited(MouseEvent e)
            {
                String xUnits = "";
                String yUnits = "";
                if (dataSets.length > 0)
                {
                    xUnits = dataSets[0].getXUnit().trim().length() == 0 ? "" : " " + dataSets[0].getXUnit();
                    yUnits = dataSets[0].getYUnit().trim().length() == 0 ? "" : " " + dataSets[0].getYUnit();

                    String legend = "";
                    if (dataSets[0].getXLegend().length() > 0 ||
                        dataSets[0].getYLegend().length() > 0)
                    {
                        legend = " (" + dataSets[0].getXLegend() + ", " + dataSets[0].getYLegend() + ") =";
                    }

                    double maxXVal = 0;
                    double someYVal = 0;
                    String point = "";

                    if (dataSets[0].getNumberPoints()>0)
                    {

                        maxXVal = dataSets[0].getMaxX()[0];
                        someYVal = dataSets[0].getMaxY()[1];
                        try
                        {
                            someYVal = dataSets[0].getYvalue(maxXVal);
                        }
                        catch (ValueNotPresentException ex)
                        {
                            // use other y val...
                        }
                        point = "(" + formatDouble(maxXVal)
                                     + xUnits + ", " + formatDouble(someYVal)
                                     + yUnits + ")";

                    }
                    setStatusBarText("(x,y) =" + legend + " "+point);
                }

            }

            public void mousePressed(MouseEvent e)
            {
                if (viewMode.equals(STACKED_VIEW))
                {
                    setStatusBarText("Plots are stacked: selection of area not available");

                }
                else
                {
                    mouseSelectionStartX = getXfromScreenVal(e.getX());
                    mouseSelectionStartY = getYfromScreenVal(e.getY());
                    logger.logComment("Mouse pressed at: (" + mouseSelectionStartX + ", " + mouseSelectionStartY + ")");

                }
                selectingArea = true;
            }

            public void mouseReleased(MouseEvent e)
            {
                if (viewMode.equals(STACKED_VIEW))
                {
                    setStatusBarText("Plots are stacked: selection of area not available");

                }
                else
                {

                    mouseSelectionFinishX = getXfromScreenVal(e.getX());
                    mouseSelectionFinishY = getYfromScreenVal(e.getY());
                    logger.logComment("Mouse released at: (" + mouseSelectionFinishX + ", " + mouseSelectionFinishY +
                                      ")");
                }
                selectingArea = false;
            }

        });



    }

    /** @todo Add to general utils... */
    private String formatDouble(double val)
    {
        if (val==(int)val) return (int)val+"";
        if (val<0.0001) return (float)val+"";
        return doubleFormatterFull.format(val);
    }


    public double getMaxXval()
    {
        double max = -1*Double.MIN_VALUE;
        for (int i = 0; i < dataSets.length; i++)
        {
            if (dataSets[i].getMaxX()[0]>max)
                max = dataSets[i].getMaxX()[0];
        }
        return max;

    }

    public double getMinXval()
    {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < dataSets.length; i++)
        {
            if (dataSets[i].getMinX()[0]<min)
                min = dataSets[i].getMinX()[0];
        }
        return min;

    }



    public double getXfromScreenVal(int screenXVal)
    {
        int width = this.getWidth();

        double xLengthSinglePixel = (maxXScaleValue - minXScaleValue) / (double) width;
        double lengthXalong = (xLengthSinglePixel * screenXVal) + (0.5 * xLengthSinglePixel);
        return (lengthXalong + minXScaleValue);

    }
    public double getYfromScreenVal(int screenYVal)
    {
        int height = this.getHeight();
        if (viewMode.equals(STACKED_VIEW))
        {

            int heightOneArea = (int)((float)height/(float)dataSets.length);

            double yLenSinglePixInArea = (maxYScaleValue - minYScaleValue) / (double) heightOneArea;

            int areaNumMouseIn = (int)Math.floor(((double)screenYVal/(double)height)*(double)dataSets.length);
            //System.out.println("Area in: "+ areaNumMouseIn);
            int areaMouseLocation = screenYVal - (areaNumMouseIn*heightOneArea);

            double lengthYalong = (yLenSinglePixInArea * areaMouseLocation) + (0.5 * yLenSinglePixInArea);
            return maxYScaleValue - lengthYalong;


        }
        else
        {

            double yLengthSinglePixel = (maxYScaleValue - minYScaleValue) / (double) height;
            double lengthYalong = (yLengthSinglePixel * screenYVal) + (0.5 * yLengthSinglePixel);
            return maxYScaleValue - lengthYalong;
        }
    }

    public void setViewMode(String viewMode)
    {
        logger.logComment("Setting view mode: "+ viewMode);
        this.viewMode = viewMode;
        this.repaint();
    }



    void this_mouseMoved(MouseEvent e)
    {
        if(dataSets.length==0)
        {
            return;
        }

        if (selectingArea && viewMode.equals(STACKED_VIEW))
        {
            setStatusBarText("Plots are stacked: selection of area not available");
            return;
        }
        mouseXval = e.getX();
        mouseYval = e.getY();

        //logger.logComment("pix x: "+ mouseXval+ ", pix y: "+ mouseYval);

        double x = getXfromScreenVal(mouseXval);
        double y = getYfromScreenVal(mouseYval);

        //logger.logComment("("+ doubleFormatterFull.format(x)
        //                            +", "+ doubleFormatterFull.format(y)+")");

        String dataSetInfo = "";

        int dataSetForUnits = 0;

        if (viewMode.equals(STACKED_VIEW))
        {

            int areaNumMouseIn = (int) Math.floor( ( (double) mouseYval / (double)this.getHeight()) *
                                                  (double) dataSets.length);
            
            dataSetForUnits = areaNumMouseIn;

            //logger.logComment("areaNumMouseIn: " + areaNumMouseIn);
            dataSetInfo = "Data set: " + dataSets[areaNumMouseIn].getReference() + " ";
        }


        String xUnits = "";
        String yUnits = "";

        if(dataSets.length>0)
        {
            xUnits = dataSets[dataSetForUnits].getXUnit().trim().length()==0?"": " " + dataSets[dataSetForUnits].getXUnit();
            yUnits = dataSets[dataSetForUnits].getYUnit().trim().length()==0?"": " " + dataSets[dataSetForUnits].getYUnit();
        }

        String del = "";
        if (selectingArea)
        {
            del = ", \u0394x = "+formatDouble(x - mouseSelectionStartX)
                +xUnits
                +", \u0394y = "+formatDouble(y - mouseSelectionStartY)
                +yUnits;
        }

        String legend = "";
        if (dataSets[dataSetForUnits].getXLegend().length()>0 ||
            dataSets[dataSetForUnits].getYLegend().length()>0)
        {
            legend = " ("+dataSets[dataSetForUnits].getXLegend()+", "+dataSets[dataSetForUnits].getYLegend()+") =";
        }




        setStatusBarText(dataSetInfo + "(x,y) ="+legend+" (" + formatDouble(x)
                         + xUnits+", " + formatDouble(y)
                         + yUnits+")"
                         + del/*+" Screen: (" + mouseXval
                         + ", " + mouseYval + ") "*/);

        if (selectingArea)
        {
            logger.logComment("selectingArea: " + selectingArea);
            this.repaint();
        }
    }

    protected void setStatusBarText(String comment)
    {
        plotFrame.setStatus(comment);

    }



    public DataSet[] getDataSets()
    {
        return dataSets;
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

        g.setColor(Color.lightGray);

        if (viewMode.equals(STACKED_VIEW) && !plotFrame.isRasterised())
        {
            for (int i = 0; i < dataSets.length; i++)
            {
                int startPoint = i*(height/(dataSets.length));
                int endPoint = (i+1)*(height/(dataSets.length));
                logger.logComment("Adding box startPoint: "+startPoint+", endPoint: "+endPoint);
                g.drawRect(0, startPoint,  width-1, endPoint);
            }
        }
        else
        {

            g.drawRect(0, 0, width-1, height);
        }


        //g.setColor(Color.black);
        //g.draw3DRect(0, 0, this.getSize().width - 1, this.getSize().height -1, false);
    }

    protected void addDataSet(DataSet dataSet)
    {
        DataSet[] tempNew = new DataSet[dataSets.length+1];
        for (int i = 0; i < dataSets.length; i++)
        {
            tempNew[i] = dataSets[i];

            if (!this.keepDataSetColours)
            {
                if (dataSet.getGraphColour().equals(dataSets[i].getGraphColour()))
                {
                    Color next = getNextColour();
                    if (next.equals(dataSets[i].getGraphColour()))
                        dataSet.setGraphColour(getNextColour());
                    else
                        dataSet.setGraphColour(next);
                }
            }
            logger.logComment("Date Set at "+i+": "+ tempNew[i]);
        }

        tempNew[dataSets.length] = dataSet;
            
        logger.logComment("Date Set at "+dataSets.length+": "+ tempNew[dataSets.length]);
        
        dataSets = tempNew;
    }

    protected void removeDataSet(DataSet dataSet)
    {
        DataSet[] tempNew = new DataSet[dataSets.length-1];
        int countAdded = 0;
        boolean foundDataSet = false;
        for (int i = 0; i < dataSets.length; i++)
        {
            if (!(dataSets[i].getReference().equals(dataSet.getReference())
                  && dataSets[i].getDescription().equals(dataSet.getDescription()))
                || foundDataSet)
            {
                tempNew[countAdded] = dataSets[i];
                countAdded++;
            }
            else
                foundDataSet = true;

        }
        if (foundDataSet) dataSets = tempNew;
    }



    public Color getNextColour()
    {
        Color colour = null;
        switch (nextColour)
        {
            case (1):
                colour = Color.BLACK; // corresp to color_index in NEURON
                break;
            case (2):
                colour = Color.red; // corresp to color_index in NEURON
                break;
            case (3):
                colour = Color.blue; // corresp to color_index in NEURON
                break;
            case (4):
                colour = Color.green; // corresp to color_index in NEURON
                break;
            case (5):
                colour = Color.ORANGE; // corresp to color_index in NEURON
                break;
            case (6):
                colour = new Color(128,117,64); // brown, corresp to color_index in NEURON
                break;
            case (7):
                colour = Color.magenta; // corresp to color_index in NEURON
                break;
            case (8):
                colour = Color.yellow; // corresp to color_index in NEURON
                break;
            case (9):
                colour = Color.gray; // corresp to color_index in NEURON
                break;
        }
        nextColour++;
        if (nextColour >= 10) nextColour = 1;
        return colour;
    }


    public void addAxes(Graphics g, int totalNumAreas, int numThisArea)
    {
        if (!showAxes && !showAxisNumbering&&!showAxisTicks) return;
        g.setColor(Color.black);

        logger.logComment("Adding axes for numThisArea: "+ numThisArea + ", totalNumAreas: "+totalNumAreas);

        //Font usedFont = g.getFont();

        if (viewMode.equals(STACKED_VIEW))
        {
            g.setFont(new Font("System", Font.PLAIN, 8));
        }
        else
            g.setFont(new Font("System", Font.PLAIN, 10));




        logger.logComment("X axis at :" + getScreenPosnValForX(maxXScaleValue) + ", " +
                          getScreenPosnValForY(0, totalNumAreas, numThisArea) + ", " +
                          getScreenPosnValForX(minXScaleValue) + ", " +
                          getScreenPosnValForY(0, totalNumAreas, numThisArea));


        logger.logComment("Y axis at :" + getScreenPosnValForX(0) + ", " +
                          getScreenPosnValForY(maxYScaleValue, totalNumAreas, numThisArea) + ", " +
                          getScreenPosnValForX(0) + ", " +
                          getScreenPosnValForY(minYScaleValue, totalNumAreas, numThisArea));


        boolean xAxisInView = true;

        // check if it would be shown in the one num areas case
        if (maxYScaleValue<=0) xAxisInView = false;
        if (minYScaleValue>=0) xAxisInView = false;

        boolean yAxisInView = true;

        // check if ti would be shown in the one num areas case
        if (maxXScaleValue<=0) yAxisInView = false;
        if (minXScaleValue>=0) yAxisInView = false;



        logger.logComment("minYScaleValue: "+ minYScaleValue);
        logger.logComment("maxYScaleValue: "+ maxYScaleValue);
        logger.logComment("xAxisInView: "+ xAxisInView);


        if(showAxes)
        {
            if(xAxisInView)
            {
                g.drawLine(getScreenPosnValForX(maxXScaleValue),
                           getScreenPosnValForY(0, totalNumAreas, numThisArea),
                           getScreenPosnValForX(minXScaleValue),
                           getScreenPosnValForY(0, totalNumAreas, numThisArea));
            }
            if(yAxisInView)
            {
                g.drawLine(getScreenPosnValForX(0),
                           getScreenPosnValForY(maxYScaleValue, totalNumAreas, numThisArea),
                           getScreenPosnValForX(0),
                           getScreenPosnValForY(minYScaleValue, totalNumAreas, numThisArea));
            }
        }


        double lengthX = maxXScaleValue - minXScaleValue;
        double lengthY = maxYScaleValue - minYScaleValue;

        double idealNumTicksX = 10;
        double idealNumTicksY = 10;


        int width = this.getWidth();
        int height = this.getHeight();
        if (viewMode.equals(STACKED_VIEW))
        {
            height = (int)((float)height/(float)dataSets.length);
        }


        if (width>700) idealNumTicksX = 12;
        else if (width>400) idealNumTicksX = 9;
        else if (width>200) idealNumTicksX = 7;
        else idealNumTicksX = 4;

        if (height>600) idealNumTicksY = 12;
        else if (height>400) idealNumTicksY = 9;
        else if (height>200) idealNumTicksY = 7;
        else idealNumTicksY = 4;

        logger.logComment("idealNumTicksX: "+idealNumTicksX);
        logger.logComment("idealNumTicksY: "+idealNumTicksY);





        double perfectTickSpacingX = lengthX/idealNumTicksX;
        double tickSpacingX = this.getEasierTickSpacing(perfectTickSpacingX);

        double perfectTickSpacingY = lengthY/idealNumTicksY;
        double tickSpacingY = this.getEasierTickSpacing(perfectTickSpacingY);


        logger.logComment("tickSpacingX: "+tickSpacingX);
        logger.logComment("tickSpacingY: "+tickSpacingY+", idealNumTicksY: "+idealNumTicksY+", lengthY: "+ lengthY+", perfectTickSpacingY: "+perfectTickSpacingY
            +", maxYScaleValue: "+maxYScaleValue+", minYScaleValue: "+minYScaleValue);


        int optimalTickLength = 5;


        if(xAxisInView)
        {
            double nextXTickLocation = 0;

            while ( (nextXTickLocation = nextXTickLocation + tickSpacingX) < maxXScaleValue)
            {
                logger.logComment("checking plotting..1");
                if (showAxisTicks)
                    g.drawLine(getScreenPosnValForX(nextXTickLocation),
                               getScreenPosnValForY(0, totalNumAreas, numThisArea),
                               getScreenPosnValForX(nextXTickLocation),
                               getScreenPosnValForY(0, totalNumAreas, numThisArea) - optimalTickLength);

                if (showAxisNumbering)
                    g.drawString(Utils3D.trimDouble(nextXTickLocation, 4) + "",
                                 getScreenPosnValForX(nextXTickLocation),
                                 getScreenPosnValForY(0, totalNumAreas, numThisArea) + optimalTickLength * 3);

            }
            nextXTickLocation = 0;
            while ( (nextXTickLocation = nextXTickLocation - tickSpacingX) > minXScaleValue)
            {
                logger.logComment("checking plotting..2");
                if (showAxisTicks)
                    g.drawLine(getScreenPosnValForX(nextXTickLocation),
                               getScreenPosnValForY(0, totalNumAreas, numThisArea),
                               getScreenPosnValForX(nextXTickLocation),
                               getScreenPosnValForY(0, totalNumAreas, numThisArea) - optimalTickLength);

                if (showAxisNumbering)
                    g.drawString(Utils3D.trimDouble(nextXTickLocation, 4) + "",
                                 getScreenPosnValForX(nextXTickLocation),
                                 getScreenPosnValForY(0, totalNumAreas, numThisArea) + optimalTickLength * 3);

            }
        }

        if(yAxisInView)
            {
                logger.logComment("tickSpacingY: " + tickSpacingY);
                int maxTicks = 100;
                int tickCount = 0;
                
                double nextYTickLocation = 0;
                if (minYScaleValue<0)
                {
                    int numFullTicksNeg = (int)Math.floor(-1* minYScaleValue/tickSpacingY);
                    nextYTickLocation = -1 * numFullTicksNeg * tickSpacingY;
                }
                else
                {
                    int numFullTicksPos = (int)Math.floor(minYScaleValue/tickSpacingY);
                    nextYTickLocation = numFullTicksPos * tickSpacingY;
                }

                while (tickCount<maxTicks && (nextYTickLocation = nextYTickLocation + tickSpacingY) < maxYScaleValue)
                {
                    logger.logComment("checking plotting, nextYTickLocation: " + nextYTickLocation
                                      + ",tickSpacingY " + tickSpacingY+", maxYScaleValue: "+ maxYScaleValue);

                    if (showAxisTicks)
                    {
                        g.drawLine(getScreenPosnValForX(0),
                                   getScreenPosnValForY(nextYTickLocation, totalNumAreas, numThisArea),
                                   getScreenPosnValForX(0) + optimalTickLength,
                                   getScreenPosnValForY(nextYTickLocation, totalNumAreas, numThisArea));
                    }
                    if (showAxisNumbering)
                    {
                        String tag = Utils3D.trimDouble(nextYTickLocation, 4) + "";
                        g.drawString(tag,
                                     getScreenPosnValForX(0) - (6 * tag.length()), // move the numbe rover a few places so it's to the left of the axis
                                     getScreenPosnValForY(nextYTickLocation, totalNumAreas, numThisArea));
                    }
                    tickCount++;

                }
                nextYTickLocation = 0;
                tickCount = 0;
                while (tickCount<maxTicks &&  (nextYTickLocation = nextYTickLocation - tickSpacingY) > minYScaleValue)
                {
                    logger.logComment("checking plotting..4, "+ (nextYTickLocation)+", "+tickSpacingY+", "+minYScaleValue);
                    if (showAxisTicks)
                        g.drawLine(getScreenPosnValForX(0),
                                   getScreenPosnValForY(nextYTickLocation, totalNumAreas, numThisArea),
                                   getScreenPosnValForX(0) + optimalTickLength,
                                   getScreenPosnValForY(nextYTickLocation, totalNumAreas, numThisArea));

                    if (showAxisNumbering)
                    {
                        String tag = Utils3D.trimDouble(nextYTickLocation, 4) + "";
                        g.drawString(tag,
                                     getScreenPosnValForX(0) - (6 * tag.length()), // move the numbe rover a few places so it's to the left of the axis
                                     getScreenPosnValForY(nextYTickLocation, totalNumAreas, numThisArea));
                    }
                    tickCount++;

                }
            }

    }


    public int getScreenPosnValForX(double x)
    {
        //logger.logComment("getScreenPosnValForX for "+ x);
        double widthInXY = this.maxXScaleValue - this.minXScaleValue;
       // System.out.println("widthInXY "+widthInXY);
        double fractionAlong = (x - this.minXScaleValue) / widthInXY;
      //  System.out.println("fractionAlong "+fractionAlong);

      //  if ()
      int width = this.getWidth();
        double doubNumPixels = (width-1) * fractionAlong;

        int intNumPixels = (int)Math.floor(doubNumPixels);


        //System.out.println("intVal "+intNumPixels);
        return intNumPixels;

    }

    public int getScreenPosnValForY(double y, int totalNumAreas, int numThisArea)
    {
      //  logger.logComment("getScreenPosnValForY for "+ y +" in area "+ numThisArea+" of "+ totalNumAreas);
        double heightInXY = this.maxYScaleValue - this.minYScaleValue;

        double distanceFromTop = maxYScaleValue - y;

        double fractionDown = distanceFromTop / heightInXY;
        int height = this.getHeight();

        int startHeightPixels = (int)(height * ((float)numThisArea/(float)totalNumAreas));

     //   logger.logComment("Total height "+height+", startHeightPixels "+startHeightPixels);

        int numPixelsAvailable = (int)((float)height/(float)totalNumAreas);
        double doubNumPixels = (numPixelsAvailable-1) * fractionDown;

        int intNumPixels = (int)Math.floor(doubNumPixels);
        int realPixDown = startHeightPixels + intNumPixels;
   //     logger.logComment("intVal "+intNumPixels+", realPixDown: "+realPixDown);
        return realPixDown;
    }


    public double getEasierTickSpacing(double tick)
    {
       // logger.logComment("getEasierTickSpacing for "+tick);

        if (Double.isInfinite(tick)) return tick;
        if (tick==0) return 1; // shouldn't be the case...

        double logNatural = Math.log(tick);
        double log10 = logNatural / Math.log(10);

        int powerOf10 = (int)Math.floor(log10);

        int significantDigits = (int)Math.round(tick / Math.pow(10, powerOf10 -1));

        double closest;
        /*
        if (significantDigits<13) closest = 10;
        else if (significantDigits<38) closest = 25;
        else if (significantDigits<62) closest = 50;
        else if (significantDigits<88) closest = 75;
        else closest = 100;
        */
        if (significantDigits<25) closest = 10;
        else if (significantDigits<75) closest = 50;
        else closest = 100;


        double retVal= closest* Math.pow(10, powerOf10 -1);
        //System.out.println("Tick: "+ tick + ", significantDigits: "+ significantDigits+ ", powerOf10: "+powerOf10+ ", closest "+ closest+ ", retVal "+ retVal);

        if (retVal<0.001) return retVal;

        retVal = Double.parseDouble(""+ doubleFormatterFull.format(retVal));

        return retVal;
    }

    protected double getMaxXScaleValue()
    {
        return this.maxXScaleValue;
    }
    protected double getMaxYScaleValue()
    {
        return this.maxYScaleValue;
    }
    protected double getMinXScaleValue()
    {
        return this.minXScaleValue;
    }
    protected double getMinYScaleValue()
    {
        return this.minYScaleValue;
    }

    protected void setMaxMinScaleValues(double maxXScaleValue,
                                        double minXScaleValue,
                                        double maxYScaleValue,
                                        double minYScaleValue)
    {
        this.maxXScaleValue = maxXScaleValue;
        this.minXScaleValue = minXScaleValue;
        this.maxYScaleValue = maxYScaleValue;
        this.minYScaleValue = minYScaleValue;
    }







    private void generateMaxMinForAxes(int totalNumAreas, int numThisArea)
    {
        if (viewMode.equals(USER_SET_VIEW))
        {
            logger.logComment("Using values set by user");
            logger.logComment("SCREEN NOW has X vals: max: " + maxXScaleValue + " and min: " + minXScaleValue);
            logger.logComment("SCREEN NOW has Y vals: max: " + maxYScaleValue + " and min: " + minYScaleValue);
        }
        else
        {
            if (viewMode.equals(CROPPED_VIEW))
            {
                logger.logComment("View mode cropped. mouseSelectionStartX: " + mouseSelectionStartX +
                                  ", mouseSelectionFinishX: " + mouseSelectionFinishX);
                logger.logComment("View mode cropped. mouseSelectionStartY: " + mouseSelectionStartY +
                                  ", mouseSelectionFinishY: " + mouseSelectionFinishY);

                if (!selectingArea) // so as not to resize when selecting...
                {
                    maxXScaleValue = Math.max(mouseSelectionStartX, mouseSelectionFinishX);
                    minXScaleValue = Math.min(mouseSelectionStartX, mouseSelectionFinishX);

                    maxYScaleValue = Math.max(mouseSelectionStartY, mouseSelectionFinishY);
                    minYScaleValue = Math.min(mouseSelectionStartY, mouseSelectionFinishY);
                }

                logger.logComment("SCREEN NOW has X vals: max: " + maxXScaleValue + " and min: " + minXScaleValue);
                logger.logComment("SCREEN NOW has Y vals: max: " + maxYScaleValue + " and min: " + minYScaleValue);

                return;
            }
            maxXScaleValue = -1 * Float.MAX_VALUE;
            maxYScaleValue = -1 * Float.MAX_VALUE;

            minXScaleValue = Float.MAX_VALUE;
            minYScaleValue = Float.MAX_VALUE;

            for (int i = 0; i < dataSets.length; i++)
            {
                logger.logComment("Incorporating max min for Data set: " + dataSets[i]);

                maxXScaleValue = Math.max(dataSets[i].getMaxX()[0], maxXScaleValue);
                maxYScaleValue = Math.max(dataSets[i].getMaxY()[1], maxYScaleValue);
                minXScaleValue = Math.min(dataSets[i].getMinX()[0], minXScaleValue);
                minYScaleValue = Math.min(dataSets[i].getMinY()[1], minYScaleValue);

                // add on a bit for the width of the bars
                if (dataSets[i].getGraphFormat().equals(USE_BARCHART_FOR_PLOT))
                {

                    double xSpacing;
                    try
                    {
                        xSpacing = dataSets[i].getXSpacing();
                        double extraSpaceForBars = (xSpacing / 2f);
                        maxXScaleValue = maxXScaleValue + extraSpaceForBars;
                        minXScaleValue = minXScaleValue - extraSpaceForBars;
                    }
                    catch (DataSetException ex)
                    {
                        GuiUtils.showErrorMessage(logger, "Problem gettign spacing for X values", ex, this);
                    }

                }
            }
            if (dataSets.length == 0)
            {
                maxXScaleValue = 1;
                maxYScaleValue = 1;
                minXScaleValue = -1;
                minYScaleValue = -1;
            }

            // chuck in the origin if requested
            if (viewMode.equals(INCLUDE_ORIGIN_VIEW))
            {
                logger.logComment("Checking if origin should be there...");
                maxXScaleValue = Math.max(0, maxXScaleValue);
                maxYScaleValue = Math.max(0, maxYScaleValue);
                minXScaleValue = Math.min(0, minXScaleValue);
                minYScaleValue = Math.min(0, minYScaleValue);
            }

            // if there's only one point, or the points are too close...
            if (maxXScaleValue == minXScaleValue)
            {
                if (minXScaleValue < 0)
                    maxXScaleValue = 0;
                else if (minXScaleValue == 0)
                    maxXScaleValue = 1;
                else
                    minXScaleValue = 0;
            }

            if (maxYScaleValue == minYScaleValue)
            {
                if (minYScaleValue < 0)
                    maxYScaleValue = 0;
                else if (minYScaleValue == 0)
                    maxYScaleValue = 1;
                else
                    minYScaleValue = 0;
            }
            logger.logComment("SCREEN has X vals: max: " + maxXScaleValue + " and min: " + minXScaleValue);
            logger.logComment("SCREEN has Y vals: max: " + maxYScaleValue + " and min: " + minYScaleValue);

            logger.logComment("Adding room at sides");

            // add room at the sides for show
            double xBitToAdd = (maxXScaleValue - minXScaleValue) * fractionToAdd;
            double yBitToAdd = (maxYScaleValue - minYScaleValue) * fractionToAdd;

            maxXScaleValue = maxXScaleValue + xBitToAdd;
            maxYScaleValue = maxYScaleValue + yBitToAdd;
            minXScaleValue = minXScaleValue - xBitToAdd;
            minYScaleValue = minYScaleValue - yBitToAdd;

            logger.logComment("SCREEN NOW has X vals: max: " + maxXScaleValue + " and min: " + minXScaleValue);
            logger.logComment("SCREEN NOW has Y vals: max: " + maxYScaleValue + " and min: " + minYScaleValue);

            logger.logComment("Done...");
        }
    }




    @Override
    public void paint(Graphics g)
    {
        long startTime = System.currentTimeMillis();

        logger.logComment("");
        logger.logComment(">>>>>>>>>>>    Paint called...");

        int pointsPlotted = 0;

        if (plotFrame.isProblemDueToBarSpacing()) return;

        setScene(g);

        boolean foundNaNInfinity = false;

        int numSeparateAreas = 1;

        if (viewMode.equals(STACKED_VIEW))
        {
            numSeparateAreas = dataSets.length;
        }

        for (int areaNumber = 0; areaNumber < numSeparateAreas; areaNumber++)
        {

            generateMaxMinForAxes(numSeparateAreas, areaNumber);
            addAxes(g, numSeparateAreas, areaNumber);

            if (selectingArea && !viewMode.equals(STACKED_VIEW))
            {
                int startX, finX, startY, finY;

                if (getScreenPosnValForX(mouseSelectionStartX) < mouseXval)
                {
                    startX = getScreenPosnValForX(mouseSelectionStartX);
                    finX = mouseXval - getScreenPosnValForX(mouseSelectionStartX);
                }
                else
                {
                    startX = mouseXval;
                    finX = getScreenPosnValForX(mouseSelectionStartX) - mouseXval;
                }
                if (getScreenPosnValForY(mouseSelectionStartY, numSeparateAreas, areaNumber) < mouseYval)
                {
                    startY = getScreenPosnValForY(mouseSelectionStartY, numSeparateAreas, areaNumber);
                    finY = mouseYval - getScreenPosnValForY(mouseSelectionStartY, numSeparateAreas, areaNumber);
                }
                else
                {
                    startY = mouseYval;
                    finY = getScreenPosnValForY(mouseSelectionStartY, numSeparateAreas, areaNumber) - mouseYval;
                }

                g.drawRect(startX, startY, finX, finY);
            }

            int dataSetIndexStart = 0;
            int dataSetIndexEnd = dataSets.length-1;

            if (numSeparateAreas>1)
            {
                dataSetIndexEnd = areaNumber;
                dataSetIndexStart = areaNumber;
            }
            logger.logComment("Plotting data sets "+dataSetIndexStart+" to "+dataSetIndexEnd);


            for (int dataSetIndex=dataSetIndexStart; dataSetIndex <= dataSetIndexEnd; dataSetIndex++)
            {
                logger.logComment(">>>>   Plotting data Set: "+ dataSets[dataSetIndex].getReference());

                int numPoints = dataSets[dataSetIndex].getNumberPoints();
                // double[] nextPoint = null;

                int lastXVal = Integer.MAX_VALUE;
                int lastYVal = Integer.MAX_VALUE;

                int lastXplotted = -1 * Integer.MAX_VALUE;
                int lastYplotted = -1 * Integer.MAX_VALUE;

                boolean insideSpike = false;

                for (int pointNum = 0; pointNum < numPoints; pointNum++)
                {
                    double[] nextPoint = dataSets[dataSetIndex].getPoint(pointNum);

                    if ((new Double(nextPoint[0])).isNaN() ||
                        nextPoint[0] == Double.NEGATIVE_INFINITY ||
                        nextPoint[0] == Double.POSITIVE_INFINITY ||
                        (new Double(nextPoint[1])).isNaN() ||
                        nextPoint[1] == Double.NEGATIVE_INFINITY ||
                        nextPoint[1] == Double.POSITIVE_INFINITY)
                    {
                        foundNaNInfinity = true;
                    }
                    int xVal = getScreenPosnValForX(nextPoint[0]);
                    int yVal = getScreenPosnValForY(nextPoint[1], numSeparateAreas, areaNumber);

                    // for first point...
                    if (lastXVal == Integer.MAX_VALUE) lastXVal = xVal;
                    if (lastYVal == Integer.MAX_VALUE) lastYVal = yVal;

                    // logger.logComment("Adding point at: (" + xVal + ", " + yVal + ")");

                    g.setColor(dataSets[dataSetIndex].getGraphColour());

                    boolean plotEveryDataPoint = false;

                    if (xVal != lastXplotted ||
                        yVal != lastYplotted // as there's no point plotting it many times
                        || plotEveryDataPoint)
                    {
                        if (!plotFrame.isRasterised())
                        {
                            if (dataSets[dataSetIndex].getGraphFormat().equals(USE_CIRCLES_FOR_PLOT))
                            {
                                int radius = 2;
                                int upperXcorner = xVal - radius;
                                int upperYcorner = yVal - radius;

                                if (xVal>=0 && xVal<=this.getWidth() &&
                                    yVal >=0 && yVal<=this.getHeight())
                                {
                                   // g.drawLine(xVal, yVal, xVal, yVal);
                                    g.drawOval(upperXcorner,
                                               upperYcorner,
                                               radius * 2,
                                               radius * 2);
                                }
                            }
                            else if (dataSets[dataSetIndex].getGraphFormat().equals(USE_CROSSES_FOR_PLOT))
                            {
                                int radius = 2;
                                int upperXcorner = xVal - radius;
                                int upperYcorner = yVal - radius;
                                int lowerXcorner = xVal + radius;
                                int lowerYcorner = yVal + radius;

                                if (xVal>=0 && xVal<=this.getWidth() &&
                                    yVal >=0 && yVal<=this.getHeight())
                                {
                                    g.drawLine(upperXcorner, upperYcorner, lowerXcorner, lowerYcorner);
                                    g.drawLine(upperXcorner, lowerYcorner, lowerXcorner, upperYcorner);
                                }
                            }
                            else if (dataSets[dataSetIndex].getGraphFormat().equals(USE_LINES_FOR_PLOT))
                            {
                                g.drawLine(xVal, yVal, lastXVal, lastYVal);
                                lastXVal = xVal;
                                lastYVal = yVal;
                            }
                            else if (dataSets[dataSetIndex].getGraphFormat().equals(USE_THICK_LINES_FOR_PLOT))
                            {
                                // TODO: improve...

                                g.drawLine(xVal, yVal, lastXVal, lastYVal);

                                g.drawLine(xVal-1, yVal-1, lastXVal-1, lastYVal-1);
                                g.drawLine(xVal-1, yVal+1, lastXVal-1, lastYVal+1);
                                g.drawLine(xVal+1, yVal-1, lastXVal+1, lastYVal-1);
                                g.drawLine(xVal+1, yVal+1, lastXVal+1, lastYVal+1);

                                g.drawLine(xVal, yVal-1, lastXVal, lastYVal-1);
                                g.drawLine(xVal, yVal+1, lastXVal, lastYVal+1);
                                g.drawLine(xVal-1, yVal, lastXVal-1, lastYVal);
                                g.drawLine(xVal+1, yVal, lastXVal+1, lastYVal);


                                lastXVal = xVal;
                                lastYVal = yVal;
                            }
                            else if (dataSets[dataSetIndex].getGraphFormat().equals(USE_POINTS_FOR_PLOT))
                            {
                                g.drawLine(xVal, yVal, xVal, yVal);
                            }
                            else if (dataSets[dataSetIndex].getGraphFormat().equals(USE_BARCHART_FOR_PLOT))
                            {
                                double xValueSpacing = -1;

                                try
                                {
                                    xValueSpacing = dataSets[dataSetIndex].getXSpacing();
                                }
                                catch (DataSetException ex)
                                {
                                    GuiUtils.showErrorMessage(logger,
                                                              "The set of points are not sequential and evenly spaced, and therefore the bar chart format cannot be used.", ex, this);
                                    plotFrame.flagProblemDueToBarSpacing();
                                    return;
                                }

                                int xValMinusHalf = getScreenPosnValForX(nextPoint[0] - (xValueSpacing / 2f));
                                int xValPlusHalf = getScreenPosnValForX(nextPoint[0] + (xValueSpacing / 2f));

                                //g.drawLine(xVal, yVal, xVal, yVal);

                                int topYVal, height;
                                int atXAxis = getScreenPosnValForY(0, numSeparateAreas, areaNumber);

                                if (nextPoint[1] > 0)
                                {
                                    // box is up from x axis...
                                    topYVal = yVal;
                                    height = atXAxis - yVal;
                                }
                                else
                                {
                                    topYVal = atXAxis;
                                    height = yVal - atXAxis;
                                }
                                g.drawRect(xValMinusHalf, topYVal, (xValPlusHalf - xValMinusHalf), height);
                                /*
                                             logger.logComment("Drawn rect: ("
                                                  + xValMinusHalf
                                                  + ", "
                                                  + topYVal
                                                  +", "
                                                  + (xValPlusHalf - xValMinusHalf)
                                                  +", "
                                                  + height
                                                  +")");*/

                            }
                            else
                            {
                                GuiUtils.showErrorMessage(logger, "Unknown format for graph: " +
                                                          dataSets[dataSetIndex].getGraphFormat(), null, this);
                                return;
                            }
                            lastXplotted = xVal;
                            lastYplotted = yVal;
                            pointsPlotted++;

                        }
                        else
                        {
                            //logger.logComment("Printing rasta style...");

                            if (nextPoint[1]>myRasterOptions.threshold)
                            {
                                if (!insideSpike)
                                {
                                    insideSpike = true;

                                    double topY = maxYScaleValue
                                        - ( (100 - myRasterOptions.getPercentage()) / 200 *
                                           (maxYScaleValue - minYScaleValue));

                                    double bottomY = minYScaleValue
                                        + ( (100 - myRasterOptions.getPercentage()) / 200 *
                                           (maxYScaleValue - minYScaleValue));
/*
                                    logger.logComment("maxYScaleValue: " + maxYScaleValue +
                                                      ", minYScaleValue: " + minYScaleValue +
                                                      ", topY: " + topY +
                                                      ", bottomY: " + bottomY);

                                    g.fillRoundRect(xVal - (myRasterOptions.getThickness()/2),
                                               getScreenPosnValForY(topY, numSeparateAreas, areaNumber),
                                               myRasterOptions.getThickness(),
                                               getScreenPosnValForY(bottomY, numSeparateAreas, areaNumber)
                                               - getScreenPosnValForY(topY, numSeparateAreas, areaNumber),
                                               2,
                                               2);

*/
                                    g.drawLine(xVal,
                                               getScreenPosnValForY(topY, numSeparateAreas, areaNumber),
                                               xVal,
                                               getScreenPosnValForY(bottomY, numSeparateAreas, areaNumber));


                                    lastXplotted = xVal;
                                    lastYplotted = yVal;
                                    pointsPlotted++;
                                }

                            }
                            else
                            {
                                insideSpike = false;
                            }



                        }
                    }
                }
            }
        }
        logger.logComment("Repainting finished after "
                           + (System.currentTimeMillis() -  startTime)
                           + " ms. Actual num points plotted: "+ pointsPlotted);


         if (foundNaNInfinity && !warnedAboutNaNInfinity)
         {
             GuiUtils.showErrorMessage(logger,
                                       "Warning! Some points in these plots were infinite or undefined", null, this);
             warnedAboutNaNInfinity = true;
         }
    }

    public void setKeepDataSetColours(boolean val)
    {
        this.keepDataSetColours = val;
    }


    public static void main(String[] args)
    {
        PlotCanvas pc = new PlotCanvas(null);
        System.out.println("Tick: "+ pc.getEasierTickSpacing(0.000000001123425345));
    }


    public boolean isShowAxisNumbering()
    {
        return showAxisNumbering;
    }
    public boolean isShowAxisTicks()
    {
        return showAxisTicks;
    }
    public void setShowAxisNumbering(boolean showAxisNumbering)
    {
        this.showAxisNumbering = showAxisNumbering;
    }
    public void setShowAxisTicks(boolean showAxisTicks)
    {
        this.showAxisTicks = showAxisTicks;
    }
    public void setShowAxes(boolean showAxes)
    {
        this.showAxes = showAxes;
    }
    public boolean isShowAxes()
    {
        return showAxes;
    }
    public void setRasterOptions(RasterOptions rasterOptions)
    {
        this.myRasterOptions = rasterOptions;
    }

    public RasterOptions getRasterOptions()
    {
        return myRasterOptions;
    }
    public SpikeAnalysisOptions getSpikeOptions()
    {
        return spikeOptions;
    }
    public void setSpikeOptions(SpikeAnalysisOptions spikeOptions)
    {
        this.spikeOptions = spikeOptions;
    }


}
