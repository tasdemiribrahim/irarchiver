package Zip;

import Common.CommonCompress;
import Common.FileOperations;
import Common.MainVocabulary;
import Gui.StatusDialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

public class Compress implements MainVocabulary,CommonCompress
{
    String className = Compress.class.getName();
    BufferedInputStream inStream;
    int BUFFERSIZE = 1024;
    String password;
    ZipOutputStream outStream;
    File inFile, outFile;
    StatusDialog compressDialog;

    public void Compress(File inFile, File outFile, boolean tar, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start ZIPCompress from " + inFile + " to " + outFile + " at line 24.");
        try
        {
            this.inFile = inFile;
            this.outFile = outFile;
            compressDialog = dialog;
            compressDialog.setIndeterminate(false);
            compressDialog.setStateToCompress();
            compressFile();
        }
        catch (Exception ex) 
        { 
            this.outFile.delete();
        } 
    }

    public void compressFile() throws Exception 
    {
        if(debug)
            System.out.println("Compress at line 49.");
        String destPath, parentPath;
        try 
        {
            int len;
            long fileSize = 0, completedSize = 0;
            ZipEntry zEntry;
            destPath = outFile.getAbsolutePath();
            outStream = new ZipOutputStream(new FileOutputStream(destPath));
            fileSize = FileOperations.getFileSizeInBytes(inFile);
            parentPath = inFile.getParent();
            if (inFile.isDirectory()) 
            {
                List<File> innerList = new ArrayList<File>();
                innerList = FileOperations.listOfFiles(inFile);
                flag:
                for (File innerFile : innerList) 
                {
                    if (!innerFile.isDirectory()) 
                        if (innerFile.length() > 0) 
                        {
                            zEntry = new ZipEntry(innerFile.getAbsolutePath().substring(parentPath.length(), innerFile.getAbsolutePath().length()));
                            outStream.putNextEntry(zEntry);
                            inStream = new BufferedInputStream(new FileInputStream(innerFile));
                            byte[] buffer = new byte[BUFFERSIZE];
                            while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled()) 
                            {
                                outStream.write(buffer, 0, len);
                                completedSize += len;
                                compressDialog.setStatus(completedSize, fileSize);
                            }
                            if (compressDialog.isCanceled()) 
                                break flag;
                            outStream.closeEntry();
                        }
                     else 
                        if(innerFile != inFile )
                            completedSize += emptyDirectorySize;
                }
            }
            else 
            {
                if (inFile.length() > 0) 
                {
                    zEntry = new ZipEntry(inFile.getAbsolutePath().substring(0 + parentPath.length(), inFile.getAbsolutePath().length()));
                    outStream.putNextEntry(zEntry);
                    inStream = new BufferedInputStream(new FileInputStream(inFile));
                    byte[] buffer = new byte[BUFFERSIZE];
                    while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled()) 
                    {
                        outStream.write(buffer, 0, len);
                        completedSize += len;
                        compressDialog.setStatus(completedSize, fileSize);
                    }
                    outStream.closeEntry();
                }
            }
        } 
        catch (Exception ex) 
        {
            compressDialog.cancelDialog();
            outFile.delete();   
            throw new Exception(compressError + className + newline + ex.getMessage());
        } 
        finally 
        {
            try 
            {
                if (outStream != null) 
                    outStream.close();
                if (inStream != null) 
                    inStream.close();
                if (compressDialog.isCanceled()) 
                    outFile.delete();
            } 
            catch (Exception ex) {
                compressDialog.cancelDialog();
                outFile.delete();
                JOptionPane.showMessageDialog(null,ex.getMessage(), ZipEmptyFileError, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void tarAndCompressFile() throws Exception {}
}
