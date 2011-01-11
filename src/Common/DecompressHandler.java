package Common;

import Gui.StatusDialog;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * Sıkıştırılmış dosyaların açma işlemlerinin yürütüldüğü sınıf.
 */
public class DecompressHandler implements MainVocabulary, Runnable {

    String className = DecompressHandler.class.getName();
    String fileName;
    File inFile, outFileParent;
    boolean overwrite;
    Thread decompressThread;
    StatusDialog decompressDialog;

    /**
     * Sıkıştırılmış dosyanın açılmasının istenmesi durumunda Common.DecompressHandler sınıfının
     * nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile sıkıştırılmış dosyayı tutan değişken.
     * @param outFileParent sıkıştırılmış dosyanın açılacağı hedef dizin.
     */
    public DecompressHandler(File inFile, File outFileParent, boolean overwrite) {

        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.overwrite = overwrite;

        decompressDialog = new StatusDialog();
    }

    /**
     * Sıkıştırılmış dosyadan çıkarma işleminin gerçekleştirildiği method. Öncelikle 
     * DeterminFileType sınıfı kulanılarak dosyanın parçalanmış veya şifrelenmiş 
     * olup olmadığı kontrol edilir. Alınan sonuca göre dosyaları birleştirme, 
     * şifre çözme ve dosya açma işlemleri sırasıyla gerçekleştirilir. Bu işlemler 
     * sırasında tempPath değişkeninin gösterdiği dizinde geçici dosyalar oluşturulur.
     * Bütün işlemler bitince bu dosyalar silinir.  
     */
    public void processDecompress() {

        String format = null;
        boolean filejoined = false;
        File tempFile1 = null, tempFile2 = null;
        int respond = 0;

        try {
            FileOperations.createTempDirectory();
            
            fileName = inFile.getName();            
            DetermineFileType findFormat = new DetermineFileType(inFile);

            if (findFormat.checkMultiPart() && !decompressDialog.isCanceled()) {
                fileName = StringOperations.getFileName(fileName, 1);
                tempFile1 = new File(tempPath + File.separator + fileName);
                
                FileOperations.joinFiles(inFile, tempFile1, decompressDialog);
                
                if(!decompressDialog.isCanceled()) {
                    filejoined = true;
                    inFile = tempFile1;
                    findFormat = new DetermineFileType(inFile);
                }
            }

            if (findFormat.checkAES() && !decompressDialog.isCanceled()) {

                JPasswordField passField = new JPasswordField();
                Object[] message = {"Please Enter Password: \n", passField};
                respond = JOptionPane.showConfirmDialog(null, message, "Retrieve Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (respond == JOptionPane.OK_OPTION) {
                    String password = new String(passField.getPassword());
                    Encrypter dencryptFile = new Encrypter(password, decompressDialog);
                    
                    if(filejoined) {
                        tempFile2 = new File(tempPath + File.separator + "temp");
                        FileOperations.copyFile(tempFile1, tempFile2);
                        dencryptFile.decrypt(tempFile2.getAbsolutePath(), tempFile1.getAbsolutePath());
                    } else {
                        tempFile1 = new File(tempPath + File.separator + fileName);
                        dencryptFile.decrypt(inFile.getAbsolutePath(), tempFile1.getAbsolutePath());
                    }
                    inFile = tempFile1;
                }
            }

            if (inFile.exists() && respond != JOptionPane.CANCEL_OPTION && !decompressDialog.isCanceled()) {
                findFormat = new DetermineFileType(inFile);

                if (findFormat.simpleCheck()) {
                    format = findFormat.getFileType();

                    if (format.equals(".tar.bz2")) {
                        BZip2.Decompress tarBz2Open = new BZip2.Decompress(inFile, outFileParent, overwrite, decompressDialog);
                    } else if (format.equals(".tar.gz")) {
                        GZip.Decompress tarGzOpen = new GZip.Decompress(inFile, outFileParent, overwrite, decompressDialog);
                    } else if (format.equals(".tar.lzma")) {
                        Lzma.Decompress tarLzmaOpen = new Lzma.Decompress(inFile, outFileParent, overwrite, decompressDialog);
                    } else if (format.equals(".tar")) {
                        Tar.ExtractArchive tarOpen = new Tar.ExtractArchive(inFile, outFileParent, overwrite, decompressDialog);
                    } else if (format.equals(".zip")) {
                        Zip.Decompress zipOpen = new Zip.Decompress(inFile, outFileParent, overwrite, decompressDialog);
                    } else if (format.equals(".bz2")) {
                        fileName = StringOperations.getFileName(fileName, 1);
                        BZip2.Decompress bz2Open = new BZip2.Decompress(inFile, outFileParent, fileName, overwrite, decompressDialog);
                    } else if (format.equals(".gz")) {
                        fileName = StringOperations.getFileName(fileName, 1);
                        GZip.Decompress gzOpen = new GZip.Decompress(inFile, outFileParent, fileName, overwrite, decompressDialog);
                    } else if (format.equals(".lzma")) {
                        fileName = StringOperations.getFileName(fileName, 1);
                        Lzma.Decompress lzmaOpen = new Lzma.Decompress(inFile, outFileParent, fileName, overwrite, decompressDialog);
                    }
                } else {
                    decompressDialog.cancelDialog();
                    JOptionPane.showMessageDialog(null, unsupportedArchiveError, "Error!", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            decompressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (tempFile1 != null) {
                tempFile1.delete();
            }
            if (tempFile2 != null) {
                tempFile2.delete();
            }
            if (!decompressDialog.isCanceled()) {
                decompressDialog.completeDialog();
            }
        }
    }
    
    /**
     * Threadi başlatmakta kullanılan method.
     */
    public void run() {
        processDecompress();
    }
}
