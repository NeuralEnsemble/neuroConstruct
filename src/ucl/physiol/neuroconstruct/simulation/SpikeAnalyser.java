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

package ucl.physiol.neuroconstruct.simulation;

import java.util.*;
import ucl.physiol.neuroconstruct.utils.*;
import ucl.physiol.neuroconstruct.dataset.*;
import ucl.physiol.neuroconstruct.gui.plotter.*;


/**
 * Useful class for analysing spike trains
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */

public class SpikeAnalyser
{
    private static ClassLogger logger = new ClassLogger("SpikeAnalyser");

    private SpikeAnalyser()
    {
    }


    public static double[] getSpikeTimes(float[] voltages,
                                   float[] times,
                                   float threshold,
                                   float startTime,
                                   float stopTime)
    {
        if (voltages.length != times.length)
        {
            logger.logError("Length of voltage array not equal to times array!");
            return null;
        }

        double[] voltsDoub = new double[voltages.length];
        double[] timeDoub = new double[times.length];

        for (int i = 0; i < voltages.length; i++)
        {
            voltsDoub[i] = voltages[i];
            timeDoub[i] = times[i];
        }

        return getSpikeTimes(voltsDoub,
                             timeDoub,
                             threshold,
                             startTime,
                             stopTime);

    }



    public static double[] getSpikeTimes(double[] voltages,
                                       double[] times,
                                       float threshold,
                                       float startTime,
                                       float stopTime)
    {
        if (voltages.length != times.length)
        {
            logger.logError("Length of voltage array not equal to times array!");
            return null;
        }
        Vector<Double> spikeTimes = new Vector<Double>();

        boolean spiking = false;

        for (int i = 0; i < voltages.length; i++)
        {
            double nextY = voltages[i];
            if (nextY >= threshold)
            {
                if (!spiking)
                {
                    if (times[i] >= startTime &&
                        times[i] <= stopTime)
                    {
                        spikeTimes.add(new Double(times[i]));
                    }
                }
                spiking = true;
            }
            else
            {
                spiking = false;
            }
        }

        /** @todo Improve... */
        double[] spikeTimeArray = new double[spikeTimes.size()];

        for (int i = 0; i < spikeTimes.size(); i++)
        {
            spikeTimeArray[i] = spikeTimes.get(i);
        }

        return spikeTimeArray;

    }

    public static Vector<Double> getInterSpikeIntervals(float[] voltages,
                                                float[] times,
                                                float threshold,
                                                float startTime,
                                                float stopTime)
    {
        double[] doubVolts =  new double[voltages.length];
        double[] doubTimes =  new double[times.length];
        for (int i = 0; i < times.length; i++)
        {
            doubVolts[i] = voltages[i];
            doubTimes[i] = times[i];
        }
        return getInterSpikeIntervals(doubVolts,
                                      doubTimes,
                                      threshold,
                                      startTime,
                                      stopTime);

    }


    public static Vector<Double> getInterSpikeIntervals(double[] voltages,
                                                double[] times,
                                                float threshold,
                                                float startTime,
                                                float stopTime)
    {
        if (voltages.length != times.length)
        {
            logger.logError("Length of voltage array not equal to times array!");
            return null;
        }

        boolean spiking = false;
        Vector<Double> spikeTimes = new Vector<Double>();
        Vector<Double> interSpikeIntervals = new Vector<Double>();

        for (int i = 0; i < voltages.length; i++)
        {
            double nextVol = voltages[i];
            double nextTime = times[i];

             if (nextVol>=threshold)
             {
                 if(!spiking)
                 {
                     if (nextTime>=startTime &&
                         nextTime<stopTime)
                     {
                         spikeTimes.add(new Double(nextTime));
                         if (spikeTimes.size()>1)
                         {
                             interSpikeIntervals.add(new Double(
                                    ((Double)spikeTimes.elementAt(spikeTimes.size()-1)).doubleValue() -
                                    ((Double)spikeTimes.elementAt(spikeTimes.size()-2)).doubleValue()));
                         }
                     }
                 }
                 spiking = true;
             }
             else
             {
                 spiking = false;
             }
        }
        return interSpikeIntervals;

    }


    public static int[] getBinnedValues(float[] data,
                                        double startDataVal,
                                        double binSize,
                                        int numBins)
    {
        double[] doubData = new double[data.length];
        for (int i = 0; i < data.length; i++)
        {
            doubData[i] = data[i];
        }

        return getBinnedValues(doubData,
                               startDataVal,
                               binSize,
                               numBins);

    }


    public static int[] getBinnedValues(double[] data,
                                        double startDataVal,
                                        double binSize,
                                        int numBins)
    {
        int[] numInEach = new int[numBins];

        for (int i = 0; i < data.length; i++)
        {
            System.out.println("data["+i+"]: " + data[i]);

            // to prevent round of exclusions
            float loc = (float)(data[i] - startDataVal) / (float)binSize;

            System.out.println("loc: " + loc);

            int binNum = (int) Math.floor(loc);

            System.out.println("binNum: " + binNum);

            if (loc == numBins )
            {
                logger.logComment("Adding to final bin...");
                binNum = numBins - 1;
            }
            if (binNum>=0 && binNum<numBins)
            {
                numInEach[binNum]++;
                logger.logComment("numInEach[binNum]: " + numInEach[binNum]);
            }
        }

        return numInEach;
    }


    public static float[] crossCorrelation(double[]  spikes1,
                                           double[]  spikes2,
                                           float binSize,
                                           float window,
                                           float pauseMin)
    {

        logger.logComment("spikes1 len: " + spikes1.length);
        logger.logComment("spikes2 len: " + spikes2.length);


        float[] correl = new float[ ( (int) Math.ceil(window / binSize) * 2) + 1];

        int binNum = (int)Math.ceil(window/binSize);

        logger.logComment("correl size: " + correl.length+", binNum: "+binNum);

        int binCnt, spike1Cnt, spike2Cnt, spike2Start = 0;

        int pSpike1Num = 0;

        for (spike1Cnt = 0; spike1Cnt < spikes1.length; spike1Cnt++)
        {
            logger.logComment("-------------------------------------------------------------------");
            logger.logComment("    Spike num of spikes1: " + spike1Cnt);
            logger.logComment("    spikes1[spike1Cnt]: " + spikes1[spike1Cnt]);

            spike2Cnt = spike2Start;

            logger.logComment("spike2Cnt: " + spike2Cnt+", size: "+ spikes2.length);

            if (spike2Cnt< spikes2.length)
            {
                logger.logComment("spikes2[spike2Cnt]: " + spikes2[spike2Cnt]);

                if (spike1Cnt == 0 || ( (spikes1[spike1Cnt] - spikes1[spike1Cnt - 1]) >= pauseMin))
                {
                    logger.logComment("=======================     relevant");

                    pSpike1Num++;

                    logger.logComment("pSpike1Num: " + pSpike1Num);

                    while ( (spike2Cnt < spikes2.length) && ( (spikes2[spike2Cnt] - spikes1[spike1Cnt]) <= window))
                    {
                        logger.logComment("---   in while, spike2Cnt: " + spike2Cnt);


                        logger.logComment("(spikes2[spike2Cnt] - spikes1[spike1Cnt]): "
                                          + (spikes2[spike2Cnt] - spikes1[spike1Cnt]));


                        if ( (spike2Cnt < spikes2.length) && (spikes1[spike1Cnt] - spikes2[spike2Cnt]) <= window)
                        {
                            logger.logComment("in if");

                            logger.logComment("spikes2[spike2Cnt]: " + spikes2[spike2Cnt]);

                            logger.logComment("(spikes2[spike2Cnt] - spikes1[spike1Cnt]): "
                                              + (spikes2[spike2Cnt] - spikes1[spike1Cnt]));

                            binCnt = (int) (Math.round( (spikes2[spike2Cnt] - spikes1[spike1Cnt]) / binSize) + binNum);
                            logger.logComment("binCnt; " + binCnt);
                            correl[binCnt]++;
                            logger.logComment("correl[binCnt]; " + correl[binCnt]);
                        }
                        else
                        {
                            spike2Start = spike2Cnt + 1;
                            logger.logComment("in else.....spike2Start: " + spike2Start);
                            logger.logComment("in else.....spikes2[spike2Cnt]: " + spikes2[spike2Cnt]);
                        }
                        spike2Cnt++;
                        logger.logComment("---> spike2Cnt: " + spike2Cnt);
                    }
                }
                else
                {
                    logger.logComment("irrelevant");

                }
            }
            else
            {
                logger.logComment("All relevant spikes in 2 dealt with...");
            }
        }
        //correl[0] *= 2;
        //correl[2 * binNum] *= 2;

        for (int i = 0; i < correl.length; i++)
        {
            correl[i] = correl[i]/(float)pSpike1Num;
        }

        logger.logComment(pSpike1Num + " relevant spikes in train 1.");

        return correl;
    }



    public static float[] crossCorrelation(float[] traceA,
                                           float[] traceB,
                                           float[] time,
                                           float threshold,
                                           float binSize,
                                           int binNum,
                                           float window,
                                           float pauseMin)
    {

        double[] spikesA = getSpikeTimes(traceA, time, threshold, time[0], time[time.length - 1]);
        double[] spikesB = getSpikeTimes(traceB, time, threshold, time[0], time[time.length - 1]);

        /*
                System.out.println("spikesA: " );
                for (int i = 0; i < spikesA.length; i++)
                {
                    System.out.print(" " + spikesA[i]);
                }
                System.out.println("\nspikesB: " );
                for (int i = 0; i < spikesB.length; i++)
                {
                    System.out.print(" " + spikesB[i]);
                }
                System.out.println("\n" );*/



        return crossCorrelation(spikesA, spikesB,
                                binSize,
                                window, pauseMin);

    }





    public static void main(String[] args)
    {
         /*
        float[] times = new float[]{0,1,2.1f,3.4f,7};

        int[] bins = getBinnedValues(times, 3, 1f, 6);
        for (int i = 0; i < bins.length; i++)
        {
            System.out.println(" bin "+i+": " + bins[i]);
        }
        System.out.println("");

        float[] traceA = new float[]{0,0,1,1,0,1,0,1,0};
        float[] traceB = new float[]{0,0,0,0,0,1,0,0,0};
        float[] time =   new float[]{0,1,2,3,4,5,6,7,8};
        float tr = 0.5f;



        float[] cc = crossCorrelation(traceA, traceB, time, tr, 1, 3, 3f, 0f);

        int size = 9;

        DataSet dsA = new DataSet("Trace A", "");
        DataSet dsB = new DataSet("Trace B", "");
        DataSet dsCC = new DataSet("Crosscorr", "");

        for (int i = 0; i < size; i++)
        {
            dsA.addPoint(time[i],traceA[i]);
            dsB.addPoint(time[i],traceB[i]);
            if (i<cc.length)
            dsCC.addPoint(time[i],cc[i]);
        }

        PlotterFrame pm = PlotManager.getPlotterFrame("hfghdfj");
        pm.addDataSet(dsA);
        pm.addDataSet(dsB);
        pm.addDataSet(dsCC);


        pm.setVisible(true);*/

        double[] times1 = new double[]{57.0486,   95.5136,             254.416,           297.261};

        double[] times2 = new double[]{58.0263,   95.8474,   216.63,   255.089,   337.633};

        int binNum = 100;
        float binSize = 5;

        float[] cc = crossCorrelation(times1, times2, binSize, binNum*binSize, 0f);

        //System.out.println("cc.length: " + cc.length);

        DataSet dsCC = new DataSet("Crosscorr", "", "", "", "", "");

        for (int i = -1*binNum; i <= binNum; i++)
        {
            dsCC.addPoint(i,cc[i+binNum]);
        }

        PlotterFrame pm = PlotManager.getPlotterFrame("hfghdfj");

        pm.addDataSet(dsCC);


        //pm.
    }




}
