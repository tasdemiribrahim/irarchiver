package Common;

import Gui.StatusDialog;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileOperations implements MainVocabulary 
{
    private static String className = FileOperations.class.getName();

    private static long getDirectorySize(File directory) throws Exception 
    {
        try
        {
            if (!directory.exists()) 
                throw new IllegalArgumentException(directory + " does not exist");
            if (!directory.isDirectory()) 
                throw new IllegalArgumentException(directory + " is not a directory");
            if(debug)
                System.out.println("Getting directory size at line 29.");
            long size = 0;
            File[] files = directory.listFiles();
            if (files == null) 
                return emptyDirectorySize;
            for (int i = 0; i < files.length; i++) 
            {
                File file = files[i];
                if (file.isDirectory()) 
                    size += getDirectorySize(file) + emptyDirectorySize;
                else 
                    size += file.length();
            }
            if(debug)
                System.out.println("Size of "+directory+":"+size+" at line 44.");
            return size; 
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }

    public static double getFileSizeInMegabytes(File file) throws Exception 
    {
        try
        {
            double size;
            if (file.isDirectory()) 
                size = (double) getDirectorySize(file) / 1024 / 1024;
            else 
                size = (double) file.length() / 1024 / 1024;
            BigDecimal bd = new BigDecimal(size);
            bd = bd.setScale(3, BigDecimal.ROUND_UP);
            size = bd.doubleValue();
            if(debug)
                System.out.println("Size of "+file+":"+size+" MB at line 66.");
            return size;
        }
        catch (Exception ex) 
        { 
            throw new Exception(getFileSizeError + className + newline + ex.getMessage());
        } 
    }

    public static double getFileSizeInKilobytes(File file) throws Exception 
    {
        try
        {
            double size;
            if (file.isDirectory()) 
                size = (double) getDirectorySize(file) / 1024;
            else 
                size = (double) file.length() / 1024;
            BigDecimal bd = new BigDecimal(size);
            bd = bd.setScale(3, BigDecimal.ROUND_UP);
            size = bd.doubleValue();
            if(debug)
                System.out.println("Size of "+file+":"+size+" KB at line 89.");
            return size;
        }
        catch (Exception ex) 
        { 
            throw new Exception(getFileSizeError + className + newline + ex.getMessage());
        } 
    }

    public static long getFileSizeInBytes(File file) throws Exception 
    {
        try
        {
            long size;
            if (file.isDirectory()) 
                size = getDirectorySize(file);
            else 
                size = file.length();
            if(debug)
                System.out.println("Size of "+file+":"+size+" B at line 109.");
            return size;
        }
        catch (Exception ex) 
        { 
            throw new Exception(getFileSizeError + className + newline + ex.getMessage());
        } 
    }

    public static void copyFile(File source, File destination) throws FileNotFoundException, IOException 
    {
        FileChannel srcChannel = null, dstChannel = null;
        try 
        {
            srcChannel = new FileInputStream(source).getChannel();
            dstChannel = new FileOutputStream(destination).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            if(debug)
                System.out.println(source + " coppied to " + destination +" at line 128.");
        }
        catch (FileNotFoundException ex) 
        {
            throw new FileNotFoundException(fileNotExistError + "at" + className + newline + ex.getMessage());
        } 
        catch (IOException ex) 
        {
            throw new IOException(IOError + "at" + className + newline + ex.getMessage());
        }  
        finally 
        {
            try 
            {
                if (srcChannel != null) 
                    srcChannel.close();
                if (dstChannel != null) 
                    dstChannel.close();
            } 
            catch (IOException ex) 
            {
                throw new IOException(StreamCloseError + "at" + className + newline + ex.getMessage());
            }
        }
    }

    public static boolean deleteDirectory(File inFile) 
    {
        if (inFile.exists()) 
        {
            File[] files = inFile.listFiles();
            for (int i = 0; i < files.length; i++) 
            {
                if (files[i].isDirectory()) 
                    deleteDirectory(files[i]);
                else 
                {
                    if(debug)
                        System.out.println(files[i] + " deleted at line 169.");
                    files[i].delete();
                }
            }
            return inFile.delete();
        } else 
            return false;
    }

    public static void splitFiles(File inFile, String outFilePath, int partSize, StatusDialog dialog) throws FileNotFoundException, IOException 
    {
        if(debug)
            System.out.println("Started spliting " + inFile + " to directory " + outFilePath + ".Size is " + partSize +" at line 181.");
        dialog.setStateToSplitFile();
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        int count = 1,  len = 0,  partNumber;
        long inFileSize;
        try 
        {
            inStream = new FileInputStream(inFile);
            byte[] buffer = new byte[partSize];
            inFileSize = inFile.length();
            BigDecimal bd = new BigDecimal((double) inFileSize / partSize);
            bd = bd.setScale(0, BigDecimal.ROUND_UP);
            partNumber = (int) bd.doubleValue();
            String fileName;
            while ((len = inStream.read(buffer, 0, partSize)) > 0 && !dialog.isCanceled()) 
            {
                fileName = outFilePath + ".part" + count;
                if(debug)
                    System.out.println(fileName + " splitted at line 200.");
                outStream = new FileOutputStream(fileName);
                if (count == 1) 
                    outStream.write(partNumber);
                outStream.write(buffer, 0, len);
                outStream.flush();
                outStream.close();
                ++count;
            }
            dialog.cancelDialog();
            dialog.setCanceled(false);
        }
        catch (FileNotFoundException ex) 
        {
            dialog.cancelDialog();
            throw new FileNotFoundException(fileNotExistError + "at" + className + newline + ex.getMessage());
        } 
        catch (IOException ex) 
        {
            dialog.cancelDialog();
            throw new IOException(IOError + "at" + className + newline + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (inStream != null) 
                    inStream.close();
                if (dialog != null) 
                    if (dialog.isCanceled()) 
                        for (int i = 1; i < count; i++) 
                        {
                            if(debug)
                                System.out.println(outFilePath + ".part" + i + " deleting at line 234.");
                            new File(outFilePath + ".part" + i).delete();
                        }
            } 
            catch (IOException ex) 
            {
                dialog.cancelDialog();
                throw new IOException(StreamCloseError + "at" + className + newline + ex.getMessage());
            }
        }
    }

    public static void joinFiles(File inFile, File outFile, StatusDialog dialog) throws FileNotFoundException, SecurityException, IOException 
    {
        if(debug)
            System.out.println("Started joining " + inFile + " to file " + outFile + " at line 251.");
        dialog.setStateToJoinFile();
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        int index = inFile.getName().lastIndexOf('.');
        String inFileName = inFile.getName().substring(0, index);
        String partNumber = inFile.getName().substring(index + 1);
        if (!partNumber.equals("part1")) 
        {
            trayIcon.displayMessage("Decompression Failed !", partedFileNotExistError, TrayIcon.MessageType.ERROR);
            dialog.cancelDialog();
        } 
        else 
        {
            int partSize = 0, partCount = 0;
            byte buffer[];
            File joinFile;
            try 
            {
                inStream = new FileInputStream(inFile);
                partCount = inStream.read();
                outStream = new FileOutputStream(outFile);
                for (int i = 1; i <= partCount; i++) 
                {
                    joinFile = new File(inFile.getParent() + File.separator + inFileName + ".part" + i);
                    if(debug)
                        System.out.println("Joining " + joinFile + " at line 277.");
                    if (!joinFile.exists()) 
                    {
                        if(debug)
                            System.out.println(joinFile + " is missing at line 281.");
                        trayIcon.displayMessage("Decompression Failed !", partedFileBrokenError, TrayIcon.MessageType.ERROR);
                        dialog.cancelDialog();
                        break;
                    } 
                    else 
                    {
                        partSize = (int) joinFile.length();
                        buffer = new byte[partSize];
                        inStream = new FileInputStream(joinFile);
                        if (i == 1)
                        {
                            inStream.skip(1);
                            partSize--;
                        }
                        inStream.read(buffer, 0, partSize);
                        outStream.write(buffer, 0, partSize);
                    }
                    if (dialog.isCanceled()) 
                        break;
                }
            } 
            catch (FileNotFoundException ex) 
            {
                dialog.cancelDialog();
                throw new FileNotFoundException(fileNotExistError + "at" + className + newline + ex.getMessage());
            }
            catch (SecurityException ex) 
            {
                dialog.cancelDialog();
                throw new SecurityException("Security Failure at" + className + newline + ex.getMessage());
            } 
            catch (IOException ex) 
            {
                dialog.cancelDialog();
                throw new IOException(IOError + "at" + className + newline + ex.getMessage());
            } 
            finally 
            {
                try 
                {
                    if (inStream != null) 
                        inStream.close();
                    if (outStream != null) 
                        outStream.close();
                    if (dialog.isCanceled()) 
                        if (outFile.exists()) 
                            outFile.delete();
                } 
                catch (IOException ex) 
                {
                    dialog.cancelDialog();
                    throw new IOException(StreamCloseError + "at" + className + newline + ex.getMessage());
                }
            }
        }
    }

    public  static List<File> listOfFiles(File startingDirectory)
    {
        if(debug)
            System.out.println("Getting list of files of " + startingDirectory +" at line 346.");
        if (startingDirectory.exists())
        {
                List<File> results = new ArrayList<File>();
                search(startingDirectory, results);
                return results;
        } 
        else 
           return null;
    }
    
    private static void search(File directory, Collection<File> results)
    {
        if(debug)
            System.out.println("Searching " + directory +" at line 360.");
        results.add(directory);
        File[] childFiles = directory.listFiles();
        if (childFiles != null) 
        {
            for (int i = 0; i < childFiles.length; i++) 
            {
                File childFile = childFiles[i];
                if (childFile.isDirectory()) 
                    search(childFile, results);
                else 
                    results.add(childFile);
            }
        } 
    }
    
    public static void createTempDirectory() 
    { 
        if(debug)
            System.out.println("Creating temp directory at line 379.");
        File tempFile = new File(tempPath);
        if(!tempFile.exists()) 
           tempFile.mkdir(); 
    }
}