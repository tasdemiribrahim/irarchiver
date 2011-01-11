package Tar;

import Common.FileOperations;
import Common.MainVocabulary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import external.publicDomain.tar.TarEntry;
import external.publicDomain.tar.TarInputStream;
import java.io.InputStream;
import Gui.StatusDialog;
import java.awt.TrayIcon;

public class ExtractArchive implements MainVocabulary 
{
    String className = ExtractArchive.class.getName();
    File inFile, outFileParent;
    TarInputStream inStream;
    FileOutputStream outStream;
    boolean overwrite;
    StatusDialog archiveDialog;

    public ExtractArchive(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start TARDecompress from " + inFile + " to " + outFileParent + " overwrite: " + overwrite + " at line 26.");
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.overwrite = overwrite;
            archiveDialog = dialog;
            archiveDialog.setIndeterminate(true);
            archiveDialog.setStateToDecompress();
            extractFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }

    public ExtractArchive(InputStream inStream, File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start BZ2Decompress from " + inFile + " to " + outFileParent + " overwrite: " + overwrite + " with stream at line 47.");
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.overwrite = overwrite;
            this.inStream = new TarInputStream(inStream);
            archiveDialog = dialog;
            archiveDialog.setIndeterminate(true);
            archiveDialog.setStateToDecompress();
            extractFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }

    private void extractFile() throws Exception 
    {
        if(debug)
            System.out.println("Start extracting archive at line 69.");
        boolean fileExist = false;
        String outFileName = null, outFileParentPath = null;
        File outFile = null;
        try 
        {
            if (inStream == null) 
                inStream = new TarInputStream(new FileInputStream(inFile.getAbsolutePath()));
            outFileParentPath = outFileParent.getAbsolutePath();
            TarEntry tEntry = inStream.getNextEntry();
            outFileName = tEntry.getName();
            outFile = new File(outFileParentPath + File.separator + outFileName);
            if(outFile.exists() && overwrite == false) 
            {
                fileExist = true;
                archiveDialog.cancelDialog();
                trayIcon.displayMessage( "Warning!",fileExistWarning, TrayIcon.MessageType.ERROR);
             }
            else 
                while (tEntry != null && !archiveDialog.isCanceled()) 
                {
                    File dFile = new File(outFileParentPath + File.separator + tEntry.getName());
                    if (!tEntry.isDirectory()) 
                    {
                        File mkDir = new File(dFile.getParent());
                        if (!mkDir.exists()) 
                            mkDir.mkdir();
                        if (!dFile.exists() || overwrite) 
                        {
                            outStream = new FileOutputStream(dFile);
                            inStream.copyEntryContents(outStream);
                            outStream.close();
                        }
                    }
                    else 
                        dFile.mkdirs();
                    tEntry = inStream.getNextEntry();
                }
        } 
        catch (Exception ex) 
        {    
            archiveDialog.cancelDialog();
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
                    outStream.close();
                if (archiveDialog.isCanceled() && fileExist == false) 
                    FileOperations.deleteDirectory(outFile);
            } 
            catch (Exception ex) 
            {
                archiveDialog.cancelDialog();
                FileOperations.deleteDirectory(outFile);
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
        }
    }
}