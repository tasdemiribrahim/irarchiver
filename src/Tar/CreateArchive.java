package Tar;

import external.publicDomain.tar.TarEntry;
import external.publicDomain.tar.TarOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import Common.FileOperations;
import Common.MainVocabulary;
import Gui.StatusDialog;

public class CreateArchive implements MainVocabulary 
{
    String className = CreateArchive.class.getName();
    BufferedInputStream inStream;
    int BUFFERSIZE = 1024;
    File inFile, outFile;
    TarOutputStream outStream;
    StatusDialog archiveDialog;

    public CreateArchive(File inFile, File outFile, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start TARCompress from " + inFile + " to " + outFile + " at line 28.");
        try
        {
            this.inFile = inFile;
            this.outFile = outFile;
            archiveDialog = dialog;
            archiveDialog.setIndeterminate(false);
            archiveDialog.setStateToCompress();
            archiveFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }

    }

    public CreateArchive(OutputStream outStream, File inFile, File outFile, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start TARCompress from " + inFile + " to " + outFile + " with stream at line 49.");
        try
        {
            this.outStream = new TarOutputStream(outStream);
            this.outFile = outFile;
            this.inFile = inFile;
            archiveDialog = dialog;
            archiveDialog.setIndeterminate(false);
            archiveDialog.setStateToCompress();
            archiveFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }

    private void archiveFile() throws Exception
    {
        if(debug)
            System.out.println("Start archiving file at line 70.");
        int diff = 0;
        String parentPath, outFilePath;
        outFilePath = outFile.getAbsolutePath();
        if(System.getProperty("os.name").startsWith("Windows")) 
            diff += 2;
        try 
        {
            int len;
            long fileSize = 0, completedSize = 0;
            fileSize = FileOperations.getFileSizeInBytes(inFile);
            if (outStream == null) 
                outStream = new TarOutputStream(new FileOutputStream(outFilePath));
            TarEntry tEntry;
            parentPath = inFile.getParent();
            tEntry = new TarEntry(inFile);
            if (inFile.isDirectory()) 
            {
                List<File> innerList = new ArrayList<File>();
                innerList = FileOperations.listOfFiles(inFile);
                flag:
                for (File innerFile : innerList) 
                {
                    if (innerFile.isDirectory()) 
                    {
                        tEntry = new TarEntry(innerFile);
                        tEntry.setName(tEntry.getName().substring(parentPath.length() - diff, tEntry.getName().length()));
                        outStream.putNextEntry(tEntry);
                        if (tEntry.getFile() != inFile) 
                        {
                            completedSize += emptyDirectorySize;
                            archiveDialog.setStatus(completedSize, fileSize);
                        }
                    } 
                    else 
                        if (innerFile.length() > 0) 
                        {
                            tEntry = new TarEntry(innerFile);
                            tEntry.setName(tEntry.getName().substring(parentPath.length() - diff, tEntry.getName().length()));
                            outStream.putNextEntry(tEntry);
                            inStream = new BufferedInputStream(new FileInputStream(innerFile));
                            byte[] buffer = new byte[BUFFERSIZE];
                            while ((len = inStream.read(buffer)) > 0 && !archiveDialog.isCanceled()) 
                            {
                                outStream.write(buffer, 0, len);
                                completedSize += len;
                                archiveDialog.setStatus(completedSize, fileSize);
                            }
                            if (!archiveDialog.isCanceled()) 
                                outStream.closeEntry();
                            else 
                                break flag;
                        }
                }
            } 
            else 
                if (inFile.length() > 0) 
                {
                    tEntry.setName(tEntry.getName().substring(parentPath.length() - diff, tEntry.getName().length()));
                    outStream.putNextEntry(tEntry);
                    inStream = new BufferedInputStream(new FileInputStream(inFile));
                    byte[] buffer = new byte[BUFFERSIZE];
                    while ((len = inStream.read(buffer)) > 0 && !archiveDialog.isCanceled()) 
                    {
                        outStream.write(buffer, 0, len);
                        completedSize += len;
                        archiveDialog.setStatus(completedSize, fileSize);
                    }
                    if (!archiveDialog.isCanceled()) 
                        outStream.closeEntry();
                }
        } 
        catch (Exception ex) 
        {
            outFile.delete();
            archiveDialog.cancelDialog();
            throw new Exception(compressError + className + newline + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (outStream != null) 
                {
                    outStream.flush();
                    outStream.close();
                }
                if (inStream != null) 
                    inStream.close();
                if (archiveDialog.isCanceled()) 
                    outFile.delete();
            } 
            catch (Exception ex) 
            {
                outFile.delete();
                archiveDialog.cancelDialog();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
        }
    }
}
