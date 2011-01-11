package BZip2;

import Common.MainVocabulary;
import external.org.apache.tools.bzip2.CBZip2OutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import Tar.CreateArchive;
import Gui.StatusDialog;
import javax.swing.JOptionPane;

/**
 * Bu sınıf dosyaları bz2 veya tar.b2z formatında sıkıştırmakta kullanır. 
 */
public class Compress implements MainVocabulary {

    String className = Compress.class.getName();
    int BUFFERSIZE = 1024;
    CBZip2OutputStream outStream;
    BufferedInputStream inStream;
    CreateArchive tarFile;
    File inFile, outFile;
    StatusDialog compressDialog;

    /**
     * bz2 veya tar.bz2 formatında sıkıştırma yapabilmek için BZip2.Compress sınıfının nesnesinin 
     * oluşturulduğu kurucu methodu. Nesne oluşturulduktan sonra tar değişkeninin değerine göre, 
     * bz2 veya tar.bz2 formatında sıkıştırma işlemini gerçekleştirebilmek için uygun method çağırılır.
     * @param inFile Sıkıştıralacak olan kaynak dosyayı gösteren değişken.
     * @param outFile Sıkıştırılacak olan dosyanın yazılacağı hedef dosyayı gösteren değişken.
     * @param tar Sıkıştırma formatının bz2 veya tar.bz2 den hangisinin olacağını belirleyen değişken.
     * tar değişkeni true ise tar.bz2, false ise bz2 formatı kullanılır.
     * @param dialog Sıkıştırma işlemi esnasında görüntülenen StatusDialog arayüzünün daha önceden 
     * oluşturulmuş olan nesnesine ait değişken.
     */
    public Compress(File inFile, File outFile, boolean tar, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFile = outFile;

        compressDialog = dialog;
        compressDialog.setIndeterminate(true);
        compressDialog.setStateToCompress();
        
        if (tar) {
            tarAndCompressFile();
        } else {
            compressFile();
        }
    }

    /**
     * inFile dosyasının sadece tek bir dosyadan oluşması (dizin olmaması) halinde sıkıştırma
     * işlemini bz2 formatında gerçekleştiren method. İlk olarak sıkıştırılacak dosya 
     * BufferedInputStream e atılır. Verinin sıkıştırılmasında CBZip2OutputStream kullanılır. 
     * BufferedInputStream deki bütün veriler okununcaya kadar devam eden bir döngü aracılığıyla, 
     * her seferinde BUFFERSIZE kadar veri BufferedInputStream den okunup, CBZip2OutputStreame 
     * yazılarak sıkıştırma işlemi gerçekleştirilir.
     */
    private void compressFile() {

        int len;
        String outFilePath,inFilePath ;
        
        outFilePath = outFile.getAbsolutePath();
        inFilePath = inFile.getAbsolutePath();

        try {
            outStream = new CBZip2OutputStream(new FileOutputStream(outFilePath));
            inStream = new BufferedInputStream(new FileInputStream(inFilePath));

            byte[] buffer = new byte[BUFFERSIZE];
            while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled()) {
                outStream.write(buffer, 0, len);
            }
        } catch (final Exception ex) {
            compressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
                if (compressDialog.isCanceled()) {
                    outFile.delete();
                }
            } catch (final IOException ex) {
                compressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                outFile.delete();
            }
        }
    }

    /**
     * inFile dosyası bir dizin ise veya sıkıştırma işlemi tar.bz2 formatında yapılmak isteniyorsa 
     * kullanılacak method. Sıkıştırılacak veriyi girdi olarak alan CBZip2OutputStream sınıfına ait
     * bir nesne oluşturulur. Son olarak da sıkıştırma işlemini gerçekleştirecek olan  Tar.CreateArchive 
     * sınıfının bir nesnesi oluşturularak sıkıştırma işlemi başlatılır.
     */
    private void tarAndCompressFile() {

        try {
            String outFilePath = outFile.getAbsolutePath();
            outStream = new CBZip2OutputStream(new FileOutputStream(outFilePath));
            tarFile = new CreateArchive(outStream, inFile, outFile, compressDialog);
        } catch (Exception ex) {
            compressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (final Exception ex) {
                compressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}