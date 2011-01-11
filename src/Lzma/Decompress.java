package Lzma;

import Common.CommonDecompress;
import Common.MainVocabulary;
import Gui.StatusDialog;
import Tar.ExtractArchive;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import external.SevenZip.Compression.LZMA.Decoder;
import java.awt.TrayIcon;

public class Decompress implements MainVocabulary,CommonDecompress
{
    String className = Decompress.class.getName();
    File inFile, outFileParent, outFile;
    boolean overwrite;
    StatusDialog decompressDialog;

    public void Decompress(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) throws Exception 
    {
        if(debug)
            System.out.println("Start LZMADecompress from " + inFile + " to " + outFileParent + " overwrite: " + overwrite + " at line 24.");
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
    
    public void Decompress(File inFile, File outFileParent, String fileName, boolean overwrite, StatusDialog decompressDialog) throws Exception 
    {
        if(debug)
            System.out.println("Start LZMADecompress from " + inFile + " to " + outFileParent + " name is " + fileName + " overwrite: " + overwrite + " at line 45.");
        try
        {
            this.inFile = inFile;
            this.outFile = new File(outFileParent.getAbsolutePath() + File.separator + fileName);
            this.overwrite = overwrite;
            if (decompressDialog != null) 
            {
                this.decompressDialog = decompressDialog;
                this.decompressDialog.setIndeterminate(true);
                this.decompressDialog.setStateToDecompress();
            }
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
        BufferedInputStream inStream = null;
        BufferedOutputStream outStream = null;
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
                inStream = new BufferedInputStream(new FileInputStream(inFile));
                outStream = new BufferedOutputStream(new FileOutputStream(outFile));
                int propertiesSize = 5;
                byte[] properties = new byte[propertiesSize];
                if (inStream.read(properties, 0, propertiesSize) != propertiesSize) 
                    throw new Exception(LzmaShortFileError);
                Decoder decoder = new Decoder(decompressDialog);
                if (!decoder.SetDecoderProperties(properties)) 
                    throw new Exception(LzmaPropertiesError);
                long outSize = 0;
                for (int i = 0; i < 8; i++) 
                {
                    int v = inStream.read();
                    if (v < 0) 
                        throw new Exception(LzmaStreamSizeError);
                    outSize |= ((long) v) << (8 * i);
                }
                if (!decoder.Code(inStream, outStream, outSize)) 
                    throw new Exception(LzmaDataStreamError);
            }
        }
        catch (Exception ex) 
        {
            outFile.delete();
            decompressDialog.cancelDialog();
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
                if (decompressDialog != null) 
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
            System.out.println("Start decompress with tar at line 136.");
        try 
        {
            String tempFilePath;
            tempFilePath = "tmp" + File.separator + "temp.tar";
            outFile = new File(tempFilePath);
            decompressFile();
            if (!decompressDialog.isCanceled()) 
            {
                inFile = outFile;
                new ExtractArchive(inFile, outFileParent, overwrite, decompressDialog);
                inFile.delete();
            }
        }
        catch (Exception ex) 
        {   
            decompressDialog.cancelDialog();
            outFile.delete();
            throw new Exception(decompressError + className + newline + ex.getMessage());
        }
    }
}
