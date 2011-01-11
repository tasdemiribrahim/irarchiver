package Common;

import Gui.StatusDialog;
import java.awt.TrayIcon;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class CompressHandler implements MainVocabulary, Runnable 
{
    private String className = CompressHandler.class.getName();
    private String compressionFormat, password;
    private int partSize;
    private File inFile, outFile;
    private StatusDialog compressDialog;
    private boolean overwrite = false, encrypt = false, multiPart = false;

    public CompressHandler(File inFile, File outFile, String compressionFormat, String password, boolean overwrite) throws Exception
    {
        try
        {
            this.outFile = outFile;
            this.inFile = inFile;
            this.compressionFormat = compressionFormat;
            this.overwrite = overwrite;
            this.password = password;
            encrypt = true;
            if(debug)
                System.out.println("Handling compress with encrypt:" +password + " at line 38.");
            compressDialog = new StatusDialog(this.outFile.getName());
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }

    public CompressHandler(File inFile, File outFile, String compressionFormat, int partSize, boolean overwrite) throws Exception
    {
        try
        {
            this.outFile = outFile;
            this.inFile = inFile;
            this.compressionFormat = compressionFormat;
            this.overwrite = overwrite;
            this.partSize = partSize;
            multiPart = true;
            if(debug)
                System.out.println("Handling compress with multipart size:" + partSize + " at line 59.");
            compressDialog = new StatusDialog(this.outFile.getName());
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }

    public CompressHandler(File inFile, File outFile, String compressionFormat, String password, int partSize, boolean overwrite) throws Exception
    {
        try
        {
            this.outFile = outFile;
            this.inFile = inFile;
            this.compressionFormat = compressionFormat;
            this.overwrite = overwrite;
            this.password = password;
            this.partSize = partSize;
            encrypt = true;
            multiPart = true;
            if(debug)
                System.out.println("Handling compress with multipart size:" + partSize +" and encrypt:" +password + " at line 82.");
            compressDialog = new StatusDialog(this.outFile.getName());
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }
   
    public CompressHandler(File inFile, File outFile, String compressionFormat, boolean overwrite) throws Exception
    {
        try
        {
            this.outFile = outFile;
            this.inFile = inFile;
            this.compressionFormat = compressionFormat;
            this.overwrite = overwrite;
            if(debug)
                System.out.println("Just handling compress at line 101.");
            compressDialog = new StatusDialog(this.outFile.getName());
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }

    private void processCompress() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, SocketException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, BadPaddingException, IllegalBlockSizeException, FileNotFoundException, IOException, Exception
    {
        try
        {
            String outFilePath = null;
            File tempFile1 = null, tempFile2 = null;
            if (outFile.exists() && overwrite == false) 
            {
                compressDialog.setVisible(false);
                trayIcon.displayMessage("Warning!",fileExistWarning, TrayIcon.MessageType.WARNING);
            }
            else 
            {
                FileOperations.createTempDirectory();
                if (encrypt && multiPart) 
                {
                    if(debug)
                        System.out.println("Precompress with multipart and encrypt at line 128.");
                    outFilePath = outFile.getAbsolutePath();
                    tempFile1 = new File(tempPath + File.separator + outFile.getName() + ".temp1");
                    tempFile2 = new File(tempPath + File.separator + outFile.getName() + ".temp2");
                    outFile = tempFile1;
                } 
                else if (encrypt || multiPart) 
                {
                    if(debug)
                        System.out.println("Precompress with multipart or encrypt at line 137.");
                    outFilePath = outFile.getAbsolutePath();
                    tempFile1 = new File(tempPath + File.separator + outFile.getName() + ".temp1");
                    outFile = tempFile1;
                }
                int k=0;
                for(;k<formats.length;k++)
                    if(compressionFormat.equals(formats[k]))
                        break;
                
                if (k==0) 
                    new Tar.CreateArchive(inFile, outFile, compressDialog);
                else
                {     
                    if(k<=Compressors.length)
                        Compressors[k-1].Compress(inFile, outFile, true, compressDialog);
                    else    
                        Compressors[(k%Compressors.length)-1].Compress(inFile, outFile, false, compressDialog);
                }
                
                if (encrypt && !compressDialog.isCanceled()) 
                {
                    if(debug)
                        System.out.println("Precompress encrypting:" + password + " at line 162.");
                    Encrypter encryptFile = new Encrypter(password, compressDialog);
                    if (multiPart)
                    {
                        if(debug)
                            System.out.println("with multipart." + " at line 167.");
                        encryptFile.encrypt(2, outFile.getAbsolutePath(), tempFile2.getAbsolutePath());
                     }
                    else
                    {
                        if(debug)
                            System.out.println("without multipart." + " at line 173.");
                        encryptFile.encrypt(2, outFile.getAbsolutePath(), outFilePath);
                    }
                }
                if (multiPart && !compressDialog.isCanceled()) 
                {
                    if(debug)
                        System.out.println("Precompress parting:" + partSize + " at line 180.");
                    if(encrypt && (partSize < tempFile2.length()))
                    {
                        if(debug)
                            System.out.println("with encrypt and part size bigger than archive." + " at line 184.");
                        FileOperations.splitFiles(tempFile2, outFilePath, partSize, compressDialog);
                    }
                    else if(encrypt) 
                    {
                        if(debug)
                            System.out.println("with encrypt." + " at line 190.");
                        outFile = new File(outFilePath);
                        FileOperations.copyFile(tempFile2, outFile);
                    }
                    else if(partSize < tempFile1.length())
                    {
                        if(debug)
                            System.out.println("without encrypt." + " at line 197.");
                        FileOperations.splitFiles(tempFile1, outFilePath, partSize, compressDialog);
                    }
                    else 
                    {
                        if(debug)
                            System.out.println("and part size bigger than archive." + " at line 203.");
                        outFile = new File(outFilePath);
                        FileOperations.copyFile(tempFile1, outFile);
                    }
                }
                if (tempFile1 != null) 
                    tempFile1.delete();
                if (tempFile2 != null) 
                    tempFile2.delete();
                if (!compressDialog.isCanceled()) 
                    compressDialog.completeDialog();
            }
        }
        catch (Exception ex) 
        { 
            throw new Exception(preCompressError + className + newline + ex.getMessage());
        } 
    }

    public void run() {
        try {
            processCompress();
        }catch (Exception ex) {
            MyLogger.getLogger().info(ex.getMessage());
        }
    }
}