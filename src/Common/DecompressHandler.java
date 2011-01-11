package Common;

import Gui.StatusDialog;
import java.awt.TrayIcon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class DecompressHandler implements MainVocabulary, Runnable 
{
    private String className = DecompressHandler.class.getName();
    private String fileName;
    private File inFile, outFileParent;
    private boolean overwrite;
    private StatusDialog decompressDialog;

    public DecompressHandler(File inFile, File outFileParent, boolean overwrite) throws Exception
    {
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.overwrite = overwrite;
            decompressDialog = new StatusDialog(this.inFile.getName());
            if(debug)
                System.out.println("Handling decompress with file:" + inFile + " to directory: " + outFileParent + " overwrite: " + overwrite + " at line 32.");
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }

    private void processDecompress() throws FileNotFoundException, SecurityException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, GeneralSecurityException, IOException, Exception
    {
        try
        {
            if(debug)
                System.out.println("Starting process decompress at line 46.");
            String format = null;
            boolean filejoined = false;
            File tempFile1 = null, tempFile2 = null;
            int respond = 0;

            FileOperations.createTempDirectory();

            fileName = inFile.getName();            
            DetermineFileType findFormat = new DetermineFileType(inFile);

            if (findFormat.checkMultiPart() && !decompressDialog.isCanceled()) 
            {
                if(debug)
                    System.out.println("Archive multipart at line 60.");
                fileName = StringOperations.getFileName(fileName, 1);
                tempFile1 = new File(tempPath + File.separator + fileName);

                FileOperations.joinFiles(inFile, tempFile1, decompressDialog);

                if(!decompressDialog.isCanceled()) 
                {
                    filejoined = true;
                    inFile = tempFile1;
                    findFormat = new DetermineFileType(inFile);
                }
            }

            if (findFormat.checkAES() && !decompressDialog.isCanceled()) 
            {
                if(debug)
                    System.out.println("Archive encrypted at line 77.");
                JPasswordField passField = new JPasswordField();
                Object[] message = {"Please Enter Password: \n", passField};
                respond = JOptionPane.showConfirmDialog(null, message, "Retrieve Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (respond == JOptionPane.OK_OPTION) 
                {
                    String password = new String(passField.getPassword());
                    Encrypter dencryptFile = new Encrypter(password, decompressDialog);

                    if(filejoined) 
                    {
                        if(debug)
                            System.out.println("Archive was multiparted at line 90.");
                        tempFile2 = new File(tempPath + File.separator + "temp");
                        FileOperations.copyFile(tempFile1, tempFile2);
                        dencryptFile.decrypt(tempFile2.getAbsolutePath(), tempFile1.getAbsolutePath());
                    } 
                    else 
                    {
                        if(debug)
                            System.out.println("Archive was not multiparted at line 98.");
                        tempFile1 = new File(tempPath + File.separator + fileName);
                        dencryptFile.decrypt(inFile.getAbsolutePath(), tempFile1.getAbsolutePath());
                    }
                    inFile = tempFile1;
                }
            }

            if (inFile.exists() && respond != JOptionPane.CANCEL_OPTION && !decompressDialog.isCanceled()) 
            {
                findFormat = new DetermineFileType(inFile);
                if ((format=findFormat.simpleCheck())!=null) 
                {
                    fileName = StringOperations.getFileName(fileName, 1);
                    
                    int k=0;
                    for(;k<formats.length;k++)
                        if(format.equals(formats[k]))
                            break;

                    if(debug)
                        System.out.println("Found archive type:" + format + " at line 113.");
                    
                    if (k==0) 
                        new Tar.ExtractArchive(inFile, outFileParent, overwrite, decompressDialog);
                    else
                    {     
                        if(k<=Decompressors.length)
                            Decompressors[k-1].Decompress(inFile, outFileParent, overwrite, decompressDialog);
                        else    
                            Decompressors[(k%Decompressors.length)-1].Decompress(inFile, outFileParent, fileName, overwrite, decompressDialog);
                    }
                } 
                else
                {
                    decompressDialog.cancelDialog();     
                    trayIcon.displayMessage("Error!",unsupportedArchiveError, TrayIcon.MessageType.ERROR);
                }
            }
            if (tempFile1 != null) 
                tempFile1.delete();
            if (tempFile2 != null) 
                tempFile2.delete();
            if (!decompressDialog.isCanceled()) 
                decompressDialog.completeDialog();
        }
        catch (Exception ex) 
        { 
            throw new Exception(preDecompressError + className + newline + ex.getMessage());
        } 
    }
    
    public void run() {
        try {
            processDecompress();
        } catch (Exception ex) {
            MyLogger.getLogger().info(ex.getMessage());
        }
    }
}