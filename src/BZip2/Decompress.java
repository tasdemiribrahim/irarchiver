package BZip2;

import Common.MainVocabulary;
import Gui.StatusDialog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import external.org.apache.tools.bzip2.CBZip2InputStream;
import Tar.ExtractArchive;
import javax.swing.JOptionPane;

/**
 * Bu sınıf bz2 veya tar.b2z formatında sıkıştırılmış olan dosyaları açmakta kullanılır.
 */
public class Decompress implements MainVocabulary {

    String className = Decompress.class.getName();
    int BUFFERSIZE = 1024;
    CBZip2InputStream inStream;
    ExtractArchive untarFile;
    BufferedOutputStream outStream;
    File inFile, outFile, outFileParent;
    StatusDialog decompressDialog;
    boolean overwrite;

    /**
     * tar.bz2 formatında sıkıştırılmış olan dosyaları açabilmek için BZip2.Decompress sınıfının 
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
        
        decompressDialog = dialog;
        decompressDialog.setIndeterminate(true);
        decompressDialog.setStateToDecompress();

        decompressAndUntarFile();
    }

    /**
     * bz2 formatında sıkıştırılmış olan dosyaları açabilmek için BZip2.Decompress sınıfının 
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
     * bz2 formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği
     * method. İlk olarak açma işleminin gerçekleştirileceği dizinde aynı isimde başka bir dosya
     * olup olmadığı overwrite değişkeni ile birlikte kontrol edilir. Sıkıştırılmış olan dosyayı
     * girdi olarak alan CBZip2InputStream sınıfına ait bir nesne oluşturulur. Açılmış dosyayı 
     * yazmak için de BufferedOutputStream sınıfına ait bir nesne oluşturulur. CBZip2InputStream 
     * içerisindeki tüm veriler okununcaya kadar devam eden bir döngü aracılığıyla, her seferinde 
     * BUFFERSIZE kadar veri CBzip2InputStream den okunup, BufferedOutputStreame yazılarak dosya 
     * açma işlemi gerçekleştirilir.
     */
    private void decompressFile() {

        boolean fileExist = false;
        try {
            if (outFile.exists() && overwrite == false) {
                fileExist = true;
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
            } else {
                int len;
                String inFilePath,outFilePath ;

                inFilePath = inFile.getAbsolutePath();
                outFilePath = outFile.getAbsolutePath();

                outStream = new BufferedOutputStream(new FileOutputStream(outFilePath));
                FileInputStream in = new FileInputStream(inFilePath);
                in.skip(2);
                inStream = new CBZip2InputStream(in);
                
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
     * tar.bz2 formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği
     * method. Sıkıştırılmış olan dosyayı girdi olarak alan bir CBZip2InputStream sınıfına ait bir 
     * nesne oluşturulur. Son olarak da sıkıştırılmış dosyayı açma işlemini gerçekleştirecek olan 
     * Tar.ExtractArchive sınıfına ait bir nesne oluşturularak dosya açma işlemi başlatılır.
     */
    private void decompressAndUntarFile() {
        try {
            String inFilePath;
            inFilePath = inFile.getAbsolutePath();

            FileInputStream iStream = new FileInputStream(inFilePath);
            iStream.skip(2);

            inStream = new CBZip2InputStream(iStream);
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
