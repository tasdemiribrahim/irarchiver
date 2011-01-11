package GZip;

import Common.MainVocabulary;
import Common.CommonCompress;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import Tar.CreateArchive;
import Gui.StatusDialog;

public class Compress implements MainVocabulary,CommonCompress
{
    String className = Compress.class.getName();
    int BUFFERSIZE = 1024;
    GZIPOutputStream outStream;
    BufferedInputStream inStream;
    File inFile, outFile; 
    StatusDialog compressDialog;
    
    public void Compress(File inFile, File outFile, boolean tar, StatusDialog dialog) throws Exception
    {
         if(debug)
            System.out.println("Start GZCompress from " + inFile + " to " + outFile + " at line 25.");
        try
        {
            this.inFile = inFile;
            this.outFile = outFile;
            compressDialog = dialog; 
            compressDialog.setIndeterminate(true);
            compressDialog.setStateToCompress();
            if(tar) 
                tarAndCompressFile();
            else 
                compressFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }   

    public void compressFile() throws Exception 
    {
        if(debug)
            System.out.println("Compress without tar at line 48.");
        int len;
        String outFilePath, inFilePath;
        outFilePath = outFile.getAbsolutePath();
        inFilePath = inFile.getAbsolutePath();
        try 
        {
             inStream = new BufferedInputStream(new FileInputStream(inFilePath));
             outStream = new GZIPOutputStream(new FileOutputStream(outFilePath));
             byte[] buffer = new byte[BUFFERSIZE];
             while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled())
                 outStream.write(buffer,0,len); 
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
                 if (inStream != null)
                     inStream.close();
                 if (outStream != null){
                     outStream.flush();
                     outStream.close();
                 }
                 if(compressDialog.isCanceled()) 
                     outFile.delete();     
            }
            catch (IOException ex) 
            { 
                compressDialog.cancelDialog();
                outFile.delete();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
         }
    }

    public void tarAndCompressFile() throws Exception
    {
        if(debug)
            System.out.println("Compress with tar at line 94.");
        try
        {
            String outFilePath = outFile.getAbsolutePath();
            outStream = new GZIPOutputStream(new FileOutputStream(outFilePath));
            new CreateArchive(outStream, inFile, outFile, compressDialog);
        } 
        catch (Exception ex) 
        { 
            compressDialog.cancelDialog();
            throw new Exception(compressError + className + newline + ex.getMessage());
        }
        finally 
        {
            try 
            {
                if (outStream != null)
                    outStream.close();
            } 
            catch (IOException ex) 
            { 
                compressDialog.cancelDialog();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
        }
    }
}