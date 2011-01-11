package Zip;

import Common.CommonDecompress;
import Common.FileOperations;
import Common.MainVocabulary;
import Gui.StatusDialog;
import java.awt.TrayIcon;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Decompress implements MainVocabulary,CommonDecompress
{
    String className = Decompress.class.getName();
    int BUFFERSIZE = 1024;
    ZipInputStream inStream;
    BufferedOutputStream outStream;
    File inFile, outFileParent;
    StatusDialog decompressDialog;
    boolean overwrite;

    public void Decompress(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start ZIPDecompress from " + inFile + " to " + outFileParent + " at line 28.");
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.overwrite = overwrite;
            decompressDialog = dialog;
            decompressDialog.setIndeterminate(false);
            decompressDialog.setStateToDecompress();
            decompressFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }

    public void decompressFile() throws Exception 
    {
        if(debug)
            System.out.println("Decompress at line 49.");
        boolean fileExist = false;
        File outFile = null;
        try 
        {
            int len;
            long totalEntry = 0, currentEntry = 0;
            String outFileParentPath, outFileName, inFilePath;
            inFilePath = inFile.getAbsolutePath();
            outFileParentPath = outFileParent.getAbsolutePath();
            inStream = new ZipInputStream(new FileInputStream(inFilePath));
            ZipEntry zEntry = inStream.getNextEntry();
            int index = zEntry.getName().indexOf(File.separator, 1);
            if (index > 0) 
                outFileName = zEntry.getName().substring(1, index);
            else 
                outFileName = zEntry.getName();
            outFile = new File(outFileParentPath + File.separator + outFileName);
            if (outFile.exists() && overwrite == false) 
            {
                fileExist = true;
                decompressDialog.cancelDialog();
                trayIcon.displayMessage( "Warning!",fileExistWarning, TrayIcon.MessageType.ERROR);
            } 
            else 
            {
                ZipFile zipArc = new ZipFile(inFile);
                totalEntry = zipArc.size();
                while (zEntry != null && !decompressDialog.isCanceled()) 
                {
                    currentEntry++;
                    File dFile = new File(outFileParentPath + zEntry.getName());
                    if (!zEntry.isDirectory()) 
                    {
                        File makeDir = new File(dFile.getParent());
                        if (!makeDir.exists()) 
                            makeDir.mkdirs();
                        if (!dFile.exists() || overwrite)
                        {
                            outStream = new BufferedOutputStream(new FileOutputStream(dFile), BUFFERSIZE);
                            byte[] data = new byte[BUFFERSIZE];
                            while ((len = inStream.read(data, 0, BUFFERSIZE)) > 0 && !decompressDialog.isCanceled()) 
                                outStream.write(data, 0, len);
                            outStream.close();
                        }
                    } 
                    else 
                        dFile.mkdirs();
                    decompressDialog.setStatus(currentEntry, totalEntry);
                    zEntry = inStream.getNextEntry();
                }
            }
        } 
        catch (Exception ex) 
        {
            decompressDialog.cancelDialog();
            FileOperations.deleteDirectory(outFile);
            throw new Exception(decompressError + className + newline + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (inStream != null) 
                    inStream.close();
                if (outStream != null) 
                {
                    outStream.flush();
                    outStream.close();
                }
                if (decompressDialog.isCanceled() && fileExist == false) 
                    FileOperations.deleteDirectory(outFile);
            } 
            catch (Exception ex) {
                decompressDialog.cancelDialog();
                FileOperations.deleteDirectory(outFile);
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
        }
    }

    public void Decompress(File inFile, File outFileParent, String fileName, boolean overwrite, StatusDialog dialog) throws Exception {
        this.Decompress(inFile,outFileParent,overwrite,dialog);
    }
    public void decompressAndUntarFile() throws Exception {}
}