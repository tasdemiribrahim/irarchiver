package Lzma;

import Common.CommonCompress;
import Common.MainVocabulary;
import Gui.StatusDialog;
import Tar.CreateArchive;
import external.SevenZip.Compression.LZMA.Encoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Compress implements MainVocabulary,CommonCompress
{
    String className = Compress.class.getName();
    int BUFFERSIZE = 1024;
    BufferedInputStream inStream;
    BufferedOutputStream outStream;
    CreateArchive tarFile;
    File inFile, outFile;
    Gui.StatusDialog compressDialog;
    
    int DictionarySize = 1 << 23;
    boolean DictionarySizeIsDefined = false;
    int Lc = 3;
    int Lp = 0;
    int Pb = 2;
    int Fb = 128;
    int Algorithm = 2;
    int MatchFinder = 1;
    boolean FbIsDefined = false, showDialog, encrypt;
    boolean Eos = false;

    public void Compress(File inFile, File outFile, boolean tar, StatusDialog dialog) throws Exception
    {
        if(debug)
            System.out.println("Start BZ2Compress from " + inFile + " to " + outFile + " at line 37.");
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
            System.out.println("Compress without tar at line 60.");
         String inFilePath = inFile.getAbsolutePath();
         String outFilePath = outFile.getAbsolutePath();
         try 
         {
             inStream = new BufferedInputStream(new FileInputStream(inFilePath));
             outStream = new BufferedOutputStream(new FileOutputStream(outFilePath));
             Encoder encoder = new Encoder(compressDialog);
             encoder.SetAlgorithm(Algorithm);
             encoder.SetDictionarySize(DictionarySize);
             encoder.SetNumFastBytes(Fb);
             encoder.SetMatchFinder(MatchFinder);
             encoder.SetLcLpPb(Lc, Lp, Pb);
             encoder.SetEndMarkerMode(Eos);
             encoder.WriteCoderProperties(outStream);
             long fileSize;
             if (Eos) 
                 fileSize = -1;
             else 
                 fileSize = inFile.length();
             for (int i = 0; i < 8; i++) 
                 outStream.write((int) (fileSize >>> (8 * i)) & 0xFF);
             encoder.Code(inStream, outStream, -1, -1, null);
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
        if(debug)
            System.out.println("Compress with tar at line 118.");
        try
        {
            String tempFilePath = "tmp" + File.separator + "temp.tar";
            File tempFile = new File(tempFilePath);
            tarFile = new CreateArchive(inFile, tempFile, compressDialog);
            inFile = new File(tempFilePath);
            compressDialog.setIndeterminate(true);
            compressFile();
            tempFile.delete();
        }
        catch (Exception ex) 
        { 
            compressDialog.cancelDialog();
            throw new Exception(compressError + className + newline + ex.getMessage());
        }
    }
}

