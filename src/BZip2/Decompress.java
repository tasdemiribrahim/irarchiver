package BZip2;

import Common.CommonDecompress;
import Common.MainVocabulary;
import Gui.StatusDialog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import external.org.apache.tools.bzip2.CBZip2InputStream;
import Tar.ExtractArchive;
import java.awt.TrayIcon;

public class Decompress implements MainVocabulary,CommonDecompress
{
    String className = Decompress.class.getName();
    int BUFFERSIZE = 1024;
    CBZip2InputStream inStream;
    BufferedOutputStream outStream;
    File inFile, outFile, outFileParent;
    StatusDialog decompressDialog;
    boolean overwrite;

    public void Decompress(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start BZ2Decompress from " + inFile + " to " + outFileParent + " overwrite: " + overwrite + " at line 26.");
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.overwrite = overwrite;
            decompressDialog = dialog;
            decompressDialog.setIndeterminate(true);
            decompressDialog.setStateToDecompress();
            decompressAndUntarFile();
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }

    public void Decompress(File inFile, File outFileParent, String fileName, boolean overwrite, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start BZ2Decompress from " + inFile + " to " + outFileParent + " name is " + fileName + " overwrite: " + overwrite + " at line 47.");
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.outFile = new File(outFileParent.getAbsolutePath() + File.separator + fileName);
            this.overwrite = overwrite;
            decompressDialog = dialog;
            decompressDialog.setIndeterminate(true);
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
            System.out.println("Start decompress without tar at line 69.");
        boolean fileExist = false;
        try 
        {
            if (outFile.exists() && overwrite == false) 
            {
                fileExist = true;
                decompressDialog.cancelDialog();
                trayIcon.displayMessage( "Warning!",fileExistWarning, TrayIcon.MessageType.ERROR);
            } 
            else 
            {
                int len;
                String inFilePath,outFilePath ;

                inFilePath = inFile.getAbsolutePath();
                outFilePath = outFile.getAbsolutePath();

                outStream = new BufferedOutputStream(new FileOutputStream(outFilePath));
                FileInputStream in = new FileInputStream(inFilePath);
                in.skip(2);
                inStream = new CBZip2InputStream(in);
                
                byte[] fBuffer = new byte[BUFFERSIZE];

                while (!decompressDialog.isCanceled() && (len = inStream.read(fBuffer, 0, BUFFERSIZE)) > 0) 
                    outStream.write(fBuffer, 0, len);
                outStream.close();
            }
        } 
        catch (Exception ex) 
        {   
            decompressDialog.cancelDialog();
            outFile.delete();
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
                if (decompressDialog.isCanceled() && fileExist == false) 
                    outFile.delete();
            }
            catch (Exception ex) 
            { 
                decompressDialog.cancelDialog();
                outFile.delete();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
        }
    }

    public void decompressAndUntarFile() throws Exception 
    {
         if(debug)
            System.out.println("Start decompress with tar at line 130.");
        try 
        {
            String inFilePath;
            inFilePath = inFile.getAbsolutePath();

            FileInputStream iStream = new FileInputStream(inFilePath);
            iStream.skip(2);

            inStream = new CBZip2InputStream(iStream);
            new ExtractArchive(inStream, inFile, outFileParent, overwrite, decompressDialog);
        } 
        catch (Exception ex) 
        {   
            decompressDialog.cancelDialog();
            outFile.delete();
            throw new Exception(decompressError + className + newline + ex.getMessage());
        }
        finally 
        {
            try 
            {
                if (inStream != null) 
                    inStream.close();
            } 
            catch (Exception ex) 
            { 
                decompressDialog.cancelDialog();
                outFile.delete();
                throw new Exception(StreamCloseError + className + newline + ex.getMessage());
            }
        }
    }
}
