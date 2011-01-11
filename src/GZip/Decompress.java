package GZip;

import Common.MainVocabulary;
import Gui.StatusDialog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import Tar.ExtractArchive;
import javax.swing.JOptionPane;

/**
 * Bu sınıf gz veya tar.gz formatında sıkıştırılmış olan dosyaları açmakta kullanılır.
 */
public class Decompress implements MainVocabulary {

    String className = Decompress.class.getName();
    int BUFFERSIZE = 1024;
    GZIPInputStream inStream;
    ExtractArchive untarFile;
    BufferedOutputStream outStream;
    File inFile, outFile, outFileParent;
    StatusDialog decompressDialog;
    boolean overwrite;


    /**
     * tar.gz formatında sıkıştırılmış olan dosyaları açabilmek için GZip.Decompress sınıfının 
     * nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile Açılacak olan sıkıştırılmış dosyayı gösteren değişken.
     * @param outFileParent Dosyanın açılacağı hedef dizini gösteren değişken.
     * @param overwrite Dosyanın açılacağı dizinde aynı isimde başka bir dosya var ise üzerine yazma 
     * işleminin yapılıp yapılmayacağını gösteren değişken.
     * @param dialog Dosya açma işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
     * önceden oluşturulmuş olan nesnesine ait değişken.
     */
    public Decompress(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.overwrite = overwrite;
        
        decompressDialog =dialog;
        decompressDialog.setIndeterminate(true);
        decompressDialog.setStateToDecompress();

        decompressAndUntarFile();
    }

    /**
     * gz formatında sıkıştırılmış olan dosyaları açabilmek için GZip.Decompress sınıfının 
     * nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile Açılacak olan sıkıştırılmış dosyayı gösteren değişken.
     * @param outFileParent Dosyayının açılacağı hedef dizini gösteren değişken.
     * @param fileName Sıkıştırılmış dosyadan çıkartılacak olan dosyanın hangi isimde 
     * kaydedileceğini gösteren değişken.
     * @param overwrite Dosyanın açılacağı dizinde aynı isimde başka bir dosya var ise 
     * üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     * @param dialog Dosya açma işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
     * önceden oluşturulmuş olan nesnesine ait değişken.
     */
    public Decompress(File inFile, File outFileParent, String fileName, boolean overwrite, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.outFile = new File(outFileParent.getAbsolutePath() + File.separator + fileName);
        this.overwrite = overwrite;
        
        decompressDialog = dialog;
        decompressDialog.setIndeterminate(true);
        decompressDialog.setStateToDecompress();

        decompressFile();
    }
   
    /**
     * gz formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği
     * method. İlk olarak açma işleminin gerçekleştirileceği dizinde aynı isimde başka bir dosya
     * olup olmadığı overwrite değişkeni ile birlikte kontrol edilir. Sıkıştırılmış olan dosyayı
     * girdi olarak alan GZIPInputStream sınıfına ait bir nesne oluşturulur. Açılmış dosyayı 
     * yazmak için de BufferedOutputStream sınıfına ait bir nesne oluşturulur. GZIPInputStream 
     * içerisindeki tüm veriler okununcaya kadar devam eden bir döngü aracılığıyla, her seferinde BUFFERSIZE 
     * kadar veri GZIPInputStream den okunup, BufferedOutputStreame yazılarak dosya açma işlemi 
     * gerçekleştirilir.
     */
    private void decompressFile() {

        boolean fileExist = false;
        try {

            int len;
            String inFilePath,outFilePath ;

            inFilePath = inFile.getAbsolutePath();
            outFilePath = outFile.getAbsolutePath();

            if (outFile.exists() && overwrite == false) {
                fileExist = true;
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
            } else {

                
                outStream = new BufferedOutputStream(new FileOutputStream(outFilePath));
                inStream = new GZIPInputStream(new FileInputStream(inFilePath));
                
                byte[] fBuffer = new byte[BUFFERSIZE];
                while (!decompressDialog.isCanceled() && (len = inStream.read(fBuffer, 0, BUFFERSIZE)) > 0) {

                    outStream.write(fBuffer, 0, len);
                }
                outStream.close();
            }
        } catch (Exception ex) {
            decompressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }
                if (decompressDialog.isCanceled() && fileExist == false) {
                    outFile.delete();
                }
            } catch (Exception ex) {
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                outFile.delete();
            }
        }
    }

    /**
     * tar.gz formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği
     * method. Sıkıştırılmış olan dosyayı girdi olarak alan bir GZIPInputStream sınıfına ait bir 
     * nesne oluşturulur. Son olarak da sıkıştırılmış dosyayı açma işlemini gerçekleştirecek olan 
     * Tar.ExtractArchive sınıfına ait bir nesne oluşturularak dosya açma işlemi başlatılır.
     */
    private void decompressAndUntarFile() {

        try {
            String inFilePath;
            inFilePath = inFile.getAbsolutePath();

            FileInputStream iStream = new FileInputStream(inFilePath);

            inStream = new GZIPInputStream(iStream);
            untarFile = new ExtractArchive(inStream, inFile, outFileParent, overwrite, decompressDialog);
        } catch (Exception ex) {
            decompressDialog.setVisible(false);
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (Exception ex) {  
                decompressDialog.setVisible(false);
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                outFile.delete();
            }
        }
    }
}