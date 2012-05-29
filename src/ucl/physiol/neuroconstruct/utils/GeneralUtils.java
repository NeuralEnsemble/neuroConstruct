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

package ucl.physiol.neuroconstruct.utils;

import java.io.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import javax.swing.*;
import ucl.physiol.neuroconstruct.hpc.mpi.MpiSettings;
import ucl.physiol.neuroconstruct.project.ProjectStructure;

/**
 * Assorted handy utilities
 *
 * @author Padraig Gleeson
 *  
 */


public class GeneralUtils
{
    private static ClassLogger logger = new ClassLogger("GeneralUtils");
    
    public static final String ARCH_I686 = "i686";
    public static final String ARCH_I386 = "i386";
    public static final String ARCH_64BIT = "amd64";
    public static final String ARCH_POWERPC = "ppc";
    public static final String ARCH_UMAC = "umac";
    
    public static final String DIR_I686 = "i686";
    public static final String DIR_64BIT = "x86_64";
    public static final String DIR_POWERPC = "powerpc";
    public static final String DIR_UMAC = "umac";
    

    private static long lastMeasuredMemory = 0;
    private static long lastTimeMemoryMeasured = 0;


    private static long lastTimeCheck = 0;


    public GeneralUtils()
    {
    }
    
    
    /**
     * @return i686 for most, x86_64 if "64" present in system properties os.arch, 
     * e.g. amd64. Will need updating as Neuron tested on more platforms...
     *
     */
    public static String getArchSpecificDir()
    {
        if (!isMacBasedPlatform() &&
            (System.getProperty("os.arch").equals(ARCH_64BIT) ||
            System.getProperty("os.arch").indexOf("64")>=0))
        {
            return DIR_64BIT;
        }
        else if (isMacBasedPlatform() && System.getProperty("os.arch").indexOf(ARCH_POWERPC)>=0)
        {
            return DIR_POWERPC;
        }
        else if (isMacBasedPlatform() && System.getProperty("os.arch").indexOf(ARCH_I386)>=0)
        {
            return DIR_I686;
        }
        else
        {
            return DIR_I686;
        }
    }

    public static boolean is64bitPlatform()
    {
        return System.getProperty("os.arch").indexOf("64")>=0; // should be enough in most cases
    }
    public static boolean isWindowsBasedPlatform()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("indows") > 0;
    }

    public static boolean isVersionControlDir(String dirname)
    {
        return dirname.equals("CVS") ||  dirname.equals(".svn") ||  dirname.equals("_svn");
    }

    public static boolean isVersionControlDir(File dir)
    {
        return isVersionControlDir(dir.getName());
    }
    

    /**
     * A simple check on whether to incl parallel/Python functionality.
     * This is turned off in the released version as the parallel func is buggy
     */
    public static boolean includeParallelFunc()
    {
        // If a file named parallel exists in the neuroConstruct main dir, enable testing of
        // parallel func...
        File parallelFile = new File(ProjectStructure.getnCHome(), "parallel");
        return parallelFile.exists();
    }



    public static boolean includeOsbProjects()
    {   
        return ProjectStructure.getOsbProjsDir().exists();
    }
    

    public static boolean isLinuxBasedPlatform()
    {
        ///if (true) return false;
        /** @todo See if this is general enough */
        return System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0 ||
            System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }


    public static boolean isMacBasedPlatform()
    {
        ///if (true) return true;
        /** @todo See if this is general enough */
        if (isWindowsBasedPlatform()) return false;
        if (isLinuxBasedPlatform()) return false;

        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }
    
    public static String getLocalHostname()
    {
        try 
        {
            InetAddress addr = InetAddress.getLocalHost();

            String hostname = addr.getHostName();
            
            return hostname;
        } 
        catch (UnknownHostException e) 
        {
            return MpiSettings.LOCALHOST;
        }
    }



    public static void printMemory(boolean forcePrint) {
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();

        long currUsed = totalMemory - freeMemory;

        String summary = "\n    Max memory: " + maxMemory / 1000000f
            + "MB , total avail: " + totalMemory / 1000000f
            + "MB (" + ((float) totalMemory * 100f / maxMemory) + "% of max), free: " + freeMemory / 1000000f
            + "MB, used: " + (currUsed / 1000) / 1000f + "MB (" + (currUsed * 10000f / totalMemory) / 100f + "% of avail)";


        long currTime = System.currentTimeMillis();

        String change = "";

        if (lastTimeMemoryMeasured > 0) {
            float increase = (currUsed - lastMeasuredMemory);

            change = "\n    Change in mem usage since "
                + (currTime - lastTimeMemoryMeasured)
                + "ms ago: " + (increase/ 1000000f)
                + "MB,  " + (increase > 0 ? "+" : "") + ((float) increase / (float) lastMeasuredMemory) * 100 + "%";
        }

        if (forcePrint) {
            System.out.println(summary + change + "\n");
        } else {
            logger.logComment(summary + change + "\n");
        }

        lastTimeMemoryMeasured = currTime;
        lastMeasuredMemory = currUsed;
    }

    /*
     * Quick & dirty writer for short text file into a String
     */
    public static void writeShortFile(File shortFile, String contents) throws IOException
    {
        Writer out = new FileWriter(shortFile);
        out.write(contents);
        out.flush();
        out.close();
      
    }


    /*
     * Quick & dirty reader for short text file into a String
     */
    public static String readShortFile(File shortFile)
    {
        Reader in = null;
        StringBuffer sb = new StringBuffer();

        try
        {
            in = new FileReader(shortFile);
            BufferedReader lineReader = new BufferedReader(in);
            String nextLine = null;
            int lineNumber = 0;

            while ((nextLine = lineReader.readLine()) != null)
            {
                lineNumber++;
                logger.logComment("Looking at line number: " + lineNumber + " (" + nextLine + ")");
                sb.append(nextLine + "\n");
            }
        }
        catch (Exception ex)
        {
            sb.append("Error reading from file: "+shortFile.getAbsolutePath()+":\n"+ex);
        }
        return sb.toString();

    }

    /**
     * Prints the current time and the time since the function was last called
     * Useful for timing methods
     * @param marker A string identifing the time step
     */
    public static void timeCheck(String marker)
    {
        timeCheck(marker, false);
    }

    /**
     * Prints the current time and the time since the function was last called
     * Useful for timing methods
     * @param marker A string identifing the time step
     * @param alsoSysOut If true, also prints output to system out
     */
    public static void timeCheck(String marker, boolean alsoSysOut)
    {
        String currentTime = null;

        java.util.Date today = null;
        SimpleDateFormat formatter = new SimpleDateFormat("H:mm:ss \' (\'SSS\')\'");

        long currTime = System.currentTimeMillis();
        today = new java.util.Date(currTime);
        currentTime = formatter.format(today);

        logger.logComment(         "                       ****               (" + currentTime.toString() + ") Timecheck: " + marker);
        if (alsoSysOut)
            System.out.println(    "                       ****               (" + currentTime.toString() + ") Timecheck: " + marker);

        if (lastTimeCheck > 0)
        {
            float timeSecDiff = (currTime - lastTimeCheck) / 1000f;
            logger.logComment(     "                       ****               Time since last timecheck:                                    " + timeSecDiff + " secs\n");

            if (alsoSysOut)
                System.out.println("                       ****               Time since last timecheck:                                    " + timeSecDiff + " secs\n");
        }
        lastTimeCheck = currTime;
        //lastTimeCheckString = marker;
    }

    /**
     * Truncates a long string to the maximum length specified, and optionally
     * adds three dots...
     */
    public String truncateString(String longString, int length, boolean dotsToo)
    {
        if (longString.length()<=length)
        {
            return longString;
        }
        String newString = longString.substring(length);
        if (dotsToo) newString = newString + "...";

        return newString;
    }


    public static String getCurrentTimeAsNiceStringWithMillis()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();

        return formatter.format(now);
    }

    public static String getNiceStringForSeconds(String timeInSeconds)
    {

        try
        {
            float val = Float.parseFloat(timeInSeconds);
            if (val/3600>1)
                return (val/3600)+" h";
            else if (val/60>1)
                return (val/60)+" m";
            else
                return (val)+" s";
        }
        catch (Exception e)
        {
            return "??? sec";
        }
    }


    public static String getCurrentTimeAsNiceString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();

        return formatter.format(now);
    }

    public static String getCurrentDateAsNiceString()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
        Date now = new Date();

        return formatter.format(now);
    }

    public static ArrayList<File> toArrayList(File[] files)
    {
        ArrayList<File> fArr = new ArrayList<File>(files.length);
        for(File f: files)
        {
            fArr.add(f);
        }
        return fArr;
    }

    public static File[] reorderAlphabetically(File[] files, boolean ascending)
    {

        if (files.length > 1)
        {
            for (int j = 1; j < files.length; j++)
            {

                for (int k = 0; k < j; k++)
                {
                    if (ascending)
                    {
                        if (files[j].getName().compareToIgnoreCase(files[k].getName()) < 0)
                        {
                            File earlierFile = files[j];
                            File laterFile = files[k];
                            files[j] = laterFile;
                            files[k] = earlierFile;
                        }
                    }
                    else
                    {
                        if (files[j].getName().compareToIgnoreCase(files[k].getName()) > 0)
                        {
                            File earlierFile = files[j];
                            File laterFile = files[k];
                            files[j] = laterFile;
                            files[k] = earlierFile;
                        }
                    }
                }
            }
        }
        return files;
    }




    public static AbstractList reorderAlphabetically(AbstractList list, boolean ascending)
    {
        if (list.size() > 1)
        {
            for (int j = 1; j < list.size(); j++)
            {

                for (int k = 0; k < j; k++)
                {
                    if (ascending)
                    {
                        if (list.get(j).toString().compareToIgnoreCase(list.get(k).toString()) < 0)
                        {
                            Object earlier = list.get(j);
                            Object later = list.get(k);
                            list.set(j, later);
                            list.set(k, earlier);
                        }
                    }
                    else
                    {
                        if (list.get(j).toString().compareToIgnoreCase(list.get(k).toString()) > 0)
                        {
                            Object earlier = list.get(j);
                            Object later = list.get(k);
                            list.set(j, later);
                            list.set(k, earlier);
                        }
                    }
                }
            }
        }
        return list;
    }




    public static ArrayList getOrderedList(Enumeration en, boolean ascending)
    {
        ArrayList a = new ArrayList();
        while(en.hasMoreElements())
            a.add(en.nextElement());
        
        if (a.size() > 1)
        {
            for (int j = 1; j < a.size(); j++)
            {

                for (int k = 0; k < j; k++)
                {
                    if (ascending)
                    {
                        if (a.get(j).toString().compareToIgnoreCase(a.get(k).toString()) < 0)
                        {
                            Object earlier = a.get(j);
                            Object later = a.get(k);
                            a.set(j, later);
                            a.set(k, earlier);
                        }
                    }
                    else
                    {
                        if (a.get(j).toString().compareToIgnoreCase(a.get(k).toString()) > 0)
                        {
                            Object earlier = a.get(j);
                            Object later = a.get(k);
                            a.set(j, later);
                            a.set(k, earlier);
                        }
                    }
                }
            }
        }
        return a;
    }



    public static String parseForHyperlinks(String text)
    {
        String prefix="http://";
        int checkpoint = 0;
        
        while(text.indexOf(prefix, checkpoint)>=0)
        {
            int start = text.indexOf(prefix, checkpoint);
            int end = text.length();
            if (text.indexOf(" ", start)>0)
                end = text.indexOf(" ", start);
            if (text.indexOf("\n", start)>0)
                end = Math.min(end,text.indexOf("\n", start));
            if (text.indexOf(")", start)>0)
                end = Math.min(end,text.indexOf(")", start));
                
            
            //System.out.println("start: ("+start+"), end: ("+end+")");
            String url = text.substring(start, end);
            if (url.endsWith("."))
            {
                url=url.substring(0,url.length()-1);
            }
            //System.out.println("url: ("+url+")");
            
            String link = "<a href=\""+url+"\">"+url+"</a>";
            
            text = replaceToken(text, url, link, start);
            
            checkpoint = start+link.length();
            
            //System.out.println("text: ("+text+"), text len: ("+text.length()+"), checkpoint: ("+checkpoint+")");
        }
        //text = replaceAllTokens(text, "&", "&amp;");
        return text;
    }


    /**
     * Replaces the first occurance of the old token with the new one from the specified index
     */
    public static String replaceToken(String line, String oldToken, String newToken, int fromIndex)
    {
        StringBuffer sb = new StringBuffer(line);
        sb.replace(line.indexOf(oldToken, fromIndex), line.indexOf(oldToken, fromIndex)+oldToken.length(), newToken);
        return sb.toString();
    }

    /**
     * Replaces all occurances of the old token with the new one
     */
    public static String replaceAllTokens(String line, String oldToken, String newToken)
    {
        if (line.indexOf(oldToken)<0) return new String(line);

        StringBuffer sb = new StringBuffer(line);
        int startCheck = 0;
        while (sb.toString().indexOf(oldToken, startCheck)>=0)
        {
            String old = sb.toString();
            int locationFirstInst = old.indexOf(oldToken, startCheck);
            sb.replace(locationFirstInst, locationFirstInst+oldToken.length(), newToken);
            startCheck = locationFirstInst+newToken.length();
            //logger.logComment("Changed to: "+ sb.toString()+", checking from "+ startCheck);
        }
        return sb.toString();
    }

    /**
     * Convenient function for creating an incremented string, e.g. Section_2 out of Section_1, etc.
     */
    public static String incrementName(String name)
    {
        if (name.indexOf("_")<0)
        {
            return name+"_1";
        }
        else
        {
            try
            {
                int indexLastUnderscore = name.lastIndexOf("_");
                int num = Integer.parseInt(name.substring(indexLastUnderscore + 1));
                num++;
                return name.substring(0, indexLastUnderscore) + "_" + num;
            }
            catch (NumberFormatException nfe)
            {
                return name+"_1";
            }
        }
    }

    /**
     * Does a quick check for spaces, etc.
     */
    public static String getBetterFileName(String oldName)
    {
        String goodFileName = GeneralUtils.replaceAllTokens(oldName, " ", "_");
        goodFileName = GeneralUtils.replaceAllTokens(goodFileName, ":", "_");
        return goodFileName;
    }

    /**
     * Quick way to get either a string in plaintext, or a html formatted string depending on a boolean value
     */
    public static String getTabbedString(String text, String tabName, boolean tabIt)
    {
        if (!tabIt) return text;
        if (tabName.trim().length()==0) return text;

        String closingTab = tabName;

        if (closingTab.indexOf(" ") > 0) closingTab = closingTab.substring(0, closingTab.indexOf(" ")).trim();

        return "<" + tabName + ">" + text + "</" + closingTab + ">";
    }
    
    public static String getBold(String text, boolean tabIt)
    {
        if (!tabIt) return text;


        return "<b>" + text + "</b>";
    }
    
    public static String getBold(int num, boolean tabIt)
    {
        if (!tabIt) return num+"";


        return "<b>" + num + "</b>";
    }
    
    public static String getBold(float num, boolean tabIt)
    {
        if (!tabIt) return num+"";


        return "<b>" + num + "</b>";
    }
    
    
    /**
     * Quick way to get either a string in plaintext, or a html coloured string depending on a boolean value
     */
    public static String getColouredString(String text, String colour, boolean html)
    {
        if (!html) return text;
        if (colour.trim().length()==0) return text;

        return "<font color=\""+colour+"\">" + text + "</font>";
    }
    
    /**
     * Quick way to get either a string in plaintext, or a html coloured string depending on a boolean value
     */
    public static String getBoldColouredString(String text, String colour, boolean html)
    {
        if (!html) return text;

        return "<b>" + getColouredString(text, colour, html) + "</b>";
    }


    public static String getEndLine(boolean html)
    {
        if (!html) return "\n";
        return "<br>\n";
    }



    public static String getMaxLenLine(String line, int maxLength)
    {
        if (line==null)
            return null;
        
        if (line.length()<=maxLength)
            return line;
        
        return line.substring(0, maxLength);
    }

    /**
     * Handy to ensure printed comments etc. are the same width...
     */
    public static String getMinLenLine(String line, int minLength)
    {
        if (line.length()>=minLength) return line;

        int extraSpaces = minLength - line.length();

        String newLine = new String(line);

        for (int i = 0; i < extraSpaces; i++)
        {
            newLine = newLine + " ";
        }

        return newLine;

    }

    // From https://gist.github.com/889747
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists())
        {
            destFile.createNewFile();
        }
        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        FileChannel source = null;
        FileChannel destination = null;
        try
        {
            fIn = new FileInputStream(sourceFile);
            source = fIn.getChannel();
            fOut = new FileOutputStream(destFile);
            destination = fOut.getChannel();
            long transfered = 0;
            long bytes = source.size();
            while (transfered < bytes)
            {
                transfered += destination.transferFrom(source, 0, source.size());
                destination.position(transfered);
            }
        } finally
        {
            if (source != null)
            {
                source.close();
            } 
            else if (fIn != null)
            {
                fIn.close();
            }
            if (destination != null)
            {
                destination.close();
            } 
            else if (fOut != null)
            {
                fOut.close();
            }
        }
    }



    public static File copyFileIntoDir(File originalFile, File dirToCopyTo) throws IOException
    {
        /** @todo Get a better/quicker way of doing this. */

        //System.out.println("Copying file: "+ originalFile.getAbsolutePath()
        //                  +" into: "+ dirToCopyTo.getAbsolutePath());

        if (!dirToCopyTo.isDirectory()||!dirToCopyTo.exists())
        {
            dirToCopyTo.mkdir();
        }

        String existingFilename = originalFile.getName();

        String copiedFileFullName = null;
        try
        {
            if (dirToCopyTo.getAbsolutePath().equals(originalFile.getParentFile().getAbsolutePath()))
            {
                logger.logComment("File is already in project...");
                return originalFile;
            }
            copiedFileFullName = dirToCopyTo.getAbsolutePath()
                + System.getProperty("file.separator")
                + existingFilename;
        }
        catch (Exception ex)
        {
            throw new IOException("Exception during copying of file: "+originalFile
                                      + " to dir: "+ dirToCopyTo);
            //return null;
        }

        File copiedFile = new File(copiedFileFullName);


        /** @todo Doing it this way to avoid probs with symbolic links in compiled mod on linux...
         * Check if there is a better way to handle links
         */
        if (!GeneralUtils.isWindowsBasedPlatform() &&
                !originalFile.getAbsolutePath().equals(originalFile.getCanonicalPath()))
        {
            logger.logComment("Assuming we have a symbolic link in linux as abs path: "+
                originalFile.getAbsolutePath()+" != canonical path: "+originalFile.getCanonicalPath());

                String[] commandToExecute = new String[]{"cp", 
                        originalFile.getCanonicalPath(),
                        copiedFile.getCanonicalPath()};

            logger.logComment("Going to execute command: " + commandToExecute[0]+"...");
            Runtime rt = Runtime.getRuntime();
            Process currentProcess = rt.exec(commandToExecute);

            try
            {
                currentProcess.waitFor();

                if (currentProcess.getInputStream()!=null) currentProcess.getInputStream().close();
                if (currentProcess.getOutputStream()!=null) currentProcess.getOutputStream().close();
                if (currentProcess.getErrorStream()!=null) currentProcess.getErrorStream().close();
            }
            catch (InterruptedException ex1)
            {
                logger.logError("Wait interrupted..", ex1);
            }
            logger.logComment("Ret val: "+currentProcess.exitValue()+", from executed command: " + commandToExecute[0]+"...");

            //while (currentProcess.exitValue())
        }
        else
        {

            InputStream in = new FileInputStream(originalFile.getCanonicalFile());
            OutputStream out = new FileOutputStream(copiedFile.getCanonicalFile());

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ( (len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.close();

        }
        logger.logComment("Just created: " + copiedFile.getAbsolutePath()+": "+ copiedFile.exists());

        return copiedFile;
    }



    public static boolean copyDirIntoDir(File originalDir, 
                                           File dirToCopyTo, 
                                           boolean includeSubDirs, 
                                           boolean ignoreCVS) throws IOException
    {
        /** @todo Get a better/quicker way of doing this. */
        logger.logComment("Copying contents of dir: "+ originalDir.getAbsolutePath()
                          + " into: "+ dirToCopyTo.getAbsolutePath());

        if (!dirToCopyTo.isDirectory()||!dirToCopyTo.exists())
        {
            dirToCopyTo.mkdir();
        }
        if (!originalDir.exists())
        {
            throw new IOException(dirToCopyTo + " cannot be found");
        }

        if (!originalDir.isDirectory())
        {
            throw new IOException(dirToCopyTo + " is not a directory");
        }

        File[] contents = originalDir.listFiles();

        for (int i = 0; i < contents.length; i++)
        {
           if (contents[i].isDirectory())
           {
               if (includeSubDirs && !(ignoreCVS && GeneralUtils.isVersionControlDir(contents[i])))
               {
                   File targetDir = new File(dirToCopyTo, contents[i].getName());
                   targetDir.mkdir();
                   copyDirIntoDir(contents[i], targetDir, includeSubDirs, ignoreCVS);
               }
           }
           else
           {
               copyFileIntoDir(contents[i], dirToCopyTo);
           }
        }


        return true;

    }



    /**
     * Gets a colour the specified fraction along a line between the two colours in 3D
     * Red Green Blue space.
     */
    public static Color getFractionalColour(Color zeroColour, Color oneColour, double fraction)
    {

        double redValue = ( (oneColour.getRed() - zeroColour.getRed()) *
                           fraction) + zeroColour.getRed();
        double greenValue = ( (oneColour.getGreen() - zeroColour.getGreen()) *
                             fraction) + zeroColour.getGreen();
        double blueValue = ( (oneColour.getBlue() - zeroColour.getBlue()) *
                            fraction) + zeroColour.getBlue();

        /** @todo replace these with gui interface... */
        if (redValue > 255) redValue = 255;
        if (greenValue > 255) greenValue = 255;
        if (blueValue > 255) blueValue = 255;

        if (redValue < 0) redValue = 0;
        if (greenValue < 0) greenValue = 0;
        if (blueValue < 0) blueValue = 0;

        return new Color( (int) redValue, (int) greenValue, (int) blueValue);
    }

    private static double cachedColourFract = Float.NaN;
    private static Color cachedColour = null;
    
    /**
     * Gets a colour from Red to Violet, based on the fraction given.
     * Roughly a colour of the rainbow...
     */
    public static Color getRainbowColour(double fraction)
    {
        if (fraction == cachedColourFract) return cachedColour;
        float corrFract = ((float)fraction*255/360f);
        Color c = Color.getHSBColor(corrFract, 1, 1);
        cachedColourFract = fraction;
        cachedColour = c;
        return c;
    }

    /**
     * Converts c\:temp to /cygdrive/c/temp  etc. as used by cygwin.
     */
    public static String convertToCygwinPath(String winPath)
    {

        logger.logComment("Converting from windows: "+ winPath);

        if (winPath.indexOf("Program Files")>=0)
        {
            winPath = GeneralUtils.replaceAllTokens(winPath, "Program Files", "Progra~1");
        }
        if (winPath.indexOf("Documents and Settings")>=0)
        {
            winPath = GeneralUtils.replaceAllTokens(winPath, "Documents and Settings", "Docume~1");
        }

        if (GeneralUtils.isWindowsBasedPlatform())
        {
            boolean canFix = true;
                    
            logger.logComment("filename : " + winPath);
            
            // Can catch spaces if a dir is called c:\Padraig Gleeson and change it to c:\Padrai~1
            while (winPath.indexOf(" ") > 0 && canFix)
            {
                int indexOfSpace = winPath.indexOf(" ");

                int prevSlash = winPath.substring(0, indexOfSpace).lastIndexOf("\\");
                int nextSlash = winPath.indexOf("\\", indexOfSpace);

                String spacedWord = winPath.substring(prevSlash + 1, nextSlash);

                logger.logComment("spacedWord: " + spacedWord);

                if (spacedWord.indexOf(" ") < 6) canFix = false;
                else
                {
                    String shortened = spacedWord.substring(0, 6) + "~1";
                    winPath = GeneralUtils.replaceAllTokens(winPath, spacedWord, shortened);
                    logger.logComment("filename now: " + winPath);
                }
            }
        }
        if (winPath.indexOf( ":")<0)
        {
            return winPath;
        }
        String drive = winPath.substring(0, winPath.indexOf( ":")).toLowerCase();
        String cygwinPath = "/cygdrive/"+ drive +"/" + replaceAllTokens(winPath.substring(winPath.indexOf( ":")+2), "\\", "/");
        logger.logComment("As cygwin: "+ cygwinPath);
        return cygwinPath;
    }


    public static void removeAllFiles(File directory, boolean warn, boolean removeDirToo, boolean removeVC)
    {
        File[] allFiles = directory.listFiles();

        //logger.logComment("Removing files from "+ directory+", remove dir too: "+removeDirToo+", remove VC: "+removeVC, true);

        if (warn)
        {
            int yesNo = JOptionPane.showConfirmDialog(null, "This will permanently remove all files from directory "
                                                         + directory.getAbsolutePath()+"\nDo you wish to continue?",
                                                         "Confirm delete directory",
                                                         JOptionPane.YES_NO_OPTION);
            if (yesNo!=JOptionPane.YES_OPTION)
            {
                logger.logComment("User cancelled...");
                return;
            }
        }
        boolean underVersionControl  = false;
        if (allFiles!=null)
        {
            for (int i = 0; i < allFiles.length; i++)
            {
                if (allFiles[i].isDirectory())
                {
                    underVersionControl = underVersionControl || isVersionControlDir(allFiles[i].getName());

                    if (!(isVersionControlDir(allFiles[i].getName()) && !removeVC) )
                    {
                        removeAllFiles(allFiles[i], false, true, removeVC);
                    }
                }
                else
                {
                    try{
                    boolean res = allFiles[i].delete();
                    //System.out.println("Deleted: "+ allFiles[i]+": "+ res);
                    if (!res) allFiles[i].deleteOnExit();
                    }
                    catch(SecurityException se)
                    {
                        se.printStackTrace();;
                    }
                }
            }
        }
        if (removeDirToo)
        {
            if (! (!removeVC && underVersionControl) ){
                boolean res = directory.delete();
                //System.out.println("Deleted: "+ directory+": "+ res);
                if (!res) directory.deleteOnExit();
            }
        }

    }


    /**
     * Assumes a long string input, into which an endLine (usually \n or <br/>)
     * will be placed after every wrap characters
     */
    public static String wrapLine(String origLine, String endLine, int wrapLength)
    {
        if (origLine==null) return "";

        StringBuffer sb = new StringBuffer();

        String remainder = new String(origLine);

        while (remainder.length()>wrapLength)
        {
            String dash = "";
            int indexSpace = wrapLength;
            while (indexSpace>0 && !Character.isWhitespace(remainder.charAt(indexSpace)))
            {
                logger.logComment("Index: "+ indexSpace);
                indexSpace--;
            }
            if (indexSpace==0)
            {
                indexSpace = wrapLength;
                dash = "-";
            }

            sb.append(remainder.substring(0, indexSpace));
            remainder = remainder.substring(indexSpace+1);

            sb.append(dash+endLine);
        }
        sb.append(remainder);

        return sb.toString();
    }







    public static void main(String[] args)
    {
        String ss = "12345.6";
        System.out.println(ss.replaceFirst("45.6", "abc"));
        
        
        String text="Implementation of the Mainen et al. pyramidal cell model from: (http://senselab.med.yale.edu/senselab/modeldb/ShowModel.asp?model=8210). End.";
        
            System.out.println("Old: "+text);
            System.out.println("New: "+parseForHyperlinks(text));
        
        
        //String dir = "/home/padraig/temp/gg gg/";

        logger.setThisClassVerbose(true);

        File srcDir = new File("../temp/from");
        File toDir = new File("../temp/to");

        if (toDir.exists())
        {
            removeAllFiles(toDir, false, true, true);
        }
        toDir.mkdir();
        
        String[] filesToTest = new String[]{"one", "two"};
        try
        {
            copyDirIntoDir(srcDir, toDir, true, true);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }


        
        if (true) return;
        String dir = "../../temp/gg gg/";
        
        File f1 = new File(dir+"uuu");
        File f2 = new File(dir+"temp");
        
        File ftarget = new File(dir+"temp/uuu");
        
        try
        {
            if (ftarget.exists()) ftarget.delete();
            
             File newF = copyFileIntoDir(f1, f2); 
            System.out.println("File: "+newF.getAbsolutePath()+" exists: "+newF.exists());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        
        
        /*
        String path = "c:\\temp\\gg";

        System.out.println("Path in win: "+ path + ", in cygwin: "+ convertToCygwinPath(path));
*/
        timeCheck("Starting...");
        logger.logComment("Logger");
        String name = "Section";
        for (int i = 0; i < 10; i++)
        {
            //System.out.println(i+": "+ name);
            name = incrementName(name);
        }

        printMemory(true);

        System.gc();
        System.gc();

        printMemory(true);


        String[] lots = new String[99999];
        for (int i = 0; i < lots.length; i++)
        {
            lots[i] = "ghfgdjfgjhffgdjfgjhfgjhfgghfgdjfgjhffgdjfgjhfgjhfg"+ i;
        }


        printMemory(true);

        lots = null;
        System.out.println("lots null...");

        printMemory(true);
        System.gc();
        printMemory(true);

        lots = new String[99999];
        for (int i = 0; i < lots.length; i++)
        {
            lots[i] = "ghfgdjfgjhffgdjfgjhfgjhfgghfgdjfgjhffgdjfgjhfgjhfg" + i;
        }
        System.out.println("lots big again");
        System.gc();
        System.gc();
        printMemory(true);


        lots = new String[1];
        System.out.println("lots new & small...");
        System.gc();
        printMemory(true);

        lots = null;
        System.out.println("lots null...");
        System.gc();
        System.gc();
        printMemory(true);
        System.gc();
        System.gc();
        printMemory(true);

        System.out.println("min : " + getMinLenLine("hh", 8)+"|");
        System.out.println("min : " + getMinLenLine("hhkk", 8)+"|");


        String line = "ghfghdf fgjdhjjjjjjjjjjjjjjjdghj dfjdghjfghj hjfdhjkfghk";

        System.out.println("Orig: \n"+ line +"\nNew:  \n"+ wrapLine(line, "\n", 12));


        timeCheck("Done...");
    }



}
