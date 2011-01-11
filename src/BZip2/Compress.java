package BZip2;

import Common.CommonCompress;
import Common.MainVocabulary;
import external.org.apache.tools.bzip2.CBZip2OutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import Tar.CreateArchive;
import Gui.StatusDialog;

public class Compress implements MainVocabulary,CommonCompress
{
    String className = Compress.class.getName();
    int BUFFERSIZE = 1024;
    CBZip2OutputStream outStream;
    BufferedInputStream inStream;
    File inFile, outFile;
    StatusDialog compressDialog;

    public void Compress(File inFile, File outFile, boolean tar, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start BZ2Compress from " + inFile + " to " + outFile + " at line 24.");
        try
        {
            this.inFile = inFile;
            this.outFile = outFile;

            compressDialog = dialog;
            compressDialog.setIndeterminate(true);
            compressDialog.setStateToCompress();

            if (tar) 
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
        try 
        {
            if(debug)
                System.out.println("Compress without tar at line 51.");
            int len;
            String outFilePath,inFilePath ;
            outFilePath = outFile.getAbsolutePath();
            inFilePath = inFile.getAbsolutePath();
            outStream = new CBZip2OutputStream(new FileOutputStream(outFilePath));
            inStream = new BufferedInputStream(new FileInputStream(inFilePath));
            byte[] buffer = new byte[BUFFERSIZE];
            while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled()) 
                outStream.write(buffer, 0, len);
        }
        catch (Exception ex) 
        { 
            outFile.delete();
            compressDialog.cancelDialog();
            throw new Exception(compressError + className + newline + ex.getMessage());
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
                if (compressDialog.isCanceled()) 
                    outFile.delete();
            }
            catch (Exception ex) 
            { 
                outFile.delete();
                compressDialog.cancelDialog();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            } 
        }
    }

    public void tarAndCompressFile() throws Exception 
    {
        try 
        {
            if(debug)
                System.out.println("Compress with tar at line 98.");
            String outFilePath = outFile.getAbsolutePath();
            outStream = new CBZip2OutputStream(new FileOutputStream(outFilePath));
            new CreateArchive(outStream, inFile, outFile, compressDialog);
        }
        catch (Exception ex) 
        { 
            outFile.delete();
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
            catch (Exception ex) 
            { 
                compressDialog.cancelDialog();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            } 
        }
    }
}