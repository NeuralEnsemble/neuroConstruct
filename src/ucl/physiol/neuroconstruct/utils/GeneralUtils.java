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

package ucl.physiol.neuroconstruct.utils;

import java.io.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

/**
 * Assorted handy utilities
 *
 * @author Padraig Gleeson
 * @version 1.0.3
 */


public class GeneralUtils
{
    private static ClassLogger logger = new ClassLogger("GeneralUtils");

    private static long lastMeasuredMemory = 0;
    private static long lastTimeMemoryMeasured = 0;


    private static long lastTimeCheck = 0;
    private static String lastTimeCheckString = null;


    public GeneralUtils()
    {
    }


    public static boolean isWindowsBasedPlatform()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("indows") > 0;
    }

    public static boolean isVersionControlDir(String dirname)
    {
        return dirname.equals("CVS") ||  dirname.equals(".svn");
    }

    public static boolean isVersionControlDir(File dir)
    {
        return isVersionControlDir(dir.getName());
    }



    public static boolean isLinuxBasedPlatform()
    {
        /** @todo See if this is general enough */
        return System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0 ||
            System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }


    public static boolean isMacBasedPlatform()
    {
        /** @todo See if this is general enough */
        if (isWindowsBasedPlatform()) return false;
        if (isLinuxBasedPlatform()) return false;

        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }



    public static void printMemory(boolean forcePrint)
    {
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();

        long currUsed = totalMemory - freeMemory;

        String summary = "Max memory: " + maxMemory / 1000000l
            + "MB , total memory available: " + totalMemory / 1000000l
            + "MB (" + ( (float) totalMemory * 100 / maxMemory) + "% of max), free memory: " + freeMemory / 1000000l
            + "MB, used: " + (currUsed / 1000l) / 1000f + "MB (" + (currUsed * 10000 / totalMemory) / 100f + "% of avail)";

        if (forcePrint) System.out.println(summary);
        else logger.logComment(summary);

         long currTime = System.currentTimeMillis();;

         if (lastTimeMemoryMeasured>0)
         {
             long increase = (currUsed - lastMeasuredMemory);

             String change = "Change in mem usage since "
                 + (currTime - lastTimeMemoryMeasured)
                 + "ms ago: " + increase
                 + ",  " + (increase > 0 ? "+" : "") + ( (float) increase / (float) lastMeasuredMemory) * 100 + "%";

             if (forcePrint) System.out.println(change);
             else logger.logComment(change);
         }

         lastTimeMemoryMeasured = currTime;
         lastMeasuredMemory = currUsed;
    }

    /**
     * Prints the current time and the time since the function was last called
     * Useful for timing methods
     * @param marker A string identifing the time step
     */
    public static void timeCheck(String marker)
    {
        boolean alsoSysOut = false; // for debugging
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
        lastTimeCheckString = marker;
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


    public static String getEndLine(boolean html)
    {
        if (!html) return "\n";
        return "<br>\n";
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


        /** @todo Doing it this way to avoid probs with symbolic links in compiles mod on linux...
         * Check if there is a better way to handle links
         */
        if (System.getProperty("os.name").indexOf("indow") < 0)
        {
            //logger.logComment("Assuming we have a symbolic link in linux as abs path "+
            //    originalFile.getAbsolutePath()+" != canonical path"+originalFile.getCanonicalPath());

            String commandToExecute = "cp " + originalFile.getAbsolutePath() + " "
                + copiedFile.getAbsolutePath()+"";

            logger.logComment("Going to execute command: " + commandToExecute);
            Runtime rt = Runtime.getRuntime();
            Process currentProcess = rt.exec(commandToExecute);
            logger.logComment("Have successfully executed command: " + commandToExecute);

            try
            {
                currentProcess.waitFor();
            }
            catch (InterruptedException ex1)
            {
                logger.logError("Wait interrupted..", ex1);
            }

            //while (currentProcess.exitValue())
        }
        else
        {

            InputStream in = new FileInputStream(originalFile);
            OutputStream out = new FileOutputStream(copiedFile);

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
        logger.logComment("Just created: " + copiedFile.getAbsolutePath());

        return copiedFile;
    }



    public static boolean copyDirIntoDir(File originalDir, File dirToCopyTo, boolean includeSubDirs, boolean ignoreCVS) throws IOException
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



    /**
     * Gets a colour from Red to Blue, based on the fraction given.
     * Roughly a colour of the rainbow...
     */
    public static Color getRainbowColour(double fraction)
    {
        double redValue = 0;
        double greenValue = 0;
        double blueValue = 0;

        if (fraction < 0) return Color.red;
        if (fraction > 1) return Color.blue;

        if (fraction<= 0.25)
        {
            double partialFraction = fraction*4d;
            redValue = 255;
            greenValue = 255*partialFraction;
            blueValue = 0;
        }
        else if (fraction>  0.25 && fraction<= 0.5)
        {
            double partialFraction = (fraction - 0.25)*4d;
            redValue = 255* (1-partialFraction);
            greenValue = 255;
            blueValue = 0;
        }
        else if (fraction>  0.5 && fraction<= 0.75)
        {
            double partialFraction = (fraction - 0.5)*4d;
            redValue = 0;
            greenValue = 255;
            blueValue = 255* (partialFraction);
        }
        else
        {
            double partialFraction = (fraction - 0.75)*4d;
            redValue = 0;
            greenValue = 255*(1-partialFraction);
            blueValue = 255;
        }

        return new Color( (int) redValue, (int) greenValue, (int) blueValue);
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


        String drive = winPath.substring(0, winPath.indexOf( ":")).toLowerCase();
        String cygwinPath = "/cygdrive/"+ drive +"/" + replaceAllTokens(winPath.substring(winPath.indexOf( ":")+2), "\\", "/");
        logger.logComment("As cygwin: "+ cygwinPath);
        return cygwinPath;
    }


    public static void removeAllFiles(File directory, boolean warn, boolean removeDirToo)
    {

        File[] allFiles = directory.listFiles();

        if (warn)
        {
            int yesNo = JOptionPane.showConfirmDialog(null, "This will permanently remove all files from directory "
                                                         + directory.getAbsolutePath()+"\nDo you wish to continue?",
                                                         "Confirm delete directory",
                                                         JOptionPane.YES_NO_CANCEL_OPTION);

            if (yesNo==JOptionPane.NO_OPTION)
            {
                logger.logComment("User cancelled...");
                return;
            }
        }
        if (allFiles!=null)
        {
            for (int i = 0; i < allFiles.length; i++)
            {
                if (allFiles[i].isDirectory())
                {
                    removeAllFiles(allFiles[i], false, true);
                }
                else
                {
                    allFiles[i].delete();
                }
            }
        }
        if (removeDirToo)
        {
            directory.delete();
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
