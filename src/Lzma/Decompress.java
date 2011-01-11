package Lzma;

import Common.MainVocabulary;
import Gui.StatusDialog;
import Tar.ExtractArchive;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import external.SevenZip.Compression.LZMA.Decoder;
import javax.swing.JOptionPane;

/**
 * Bu sınıf lzma veya tar.lzma formatında sıkıştırılmış olan dosyaları açmakta kullanılır.
 */
public class Decompress implements MainVocabulary {

    String className = Decompress.class.getName();
    File inFile, outFileParent, outFile;
    boolean overwrite;
    StatusDialog decompressDialog;

   /**
     * tar.lzma formatında sıkıştırılmış olan dosyaları açabilmek için Lzma.Decompress sınıfının 
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
     * lzma formatında sıkıştırılmış olan dosyaları açabilmek için Lzma.Decompress sınıfının 
     * nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile Açılacak olan sıkıştırılmış dosyayı gösteren değişken.
     * @param outFileParent Dosyayının açılacağı hedef dizini gösteren değişken.
     * @param fileName Sıkıştırılmış dosyadan çıkartılacak olan dosyanın hangi isimde 
     * kaydedileceğini gösteren değişken.
     * @param overwrite Dosyanın açılacağı dizinde aynı isimde başka bir dosya var ise 
     * üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     * @param decompressDialog Dosya açma işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
     * önceden oluşturulmuş olan nesnesine ait değişken.
     */
    public Decompress(File inFile, File outFileParent, String fileName, boolean overwrite, StatusDialog decompressDialog) {

        this.inFile = inFile;
        this.outFile = new File(outFileParent.getAbsolutePath() + File.separator + fileName);
        this.overwrite = overwrite;

        if (decompressDialog != null) {
            this.decompressDialog = decompressDialog;
            this.decompressDialog.setIndeterminate(true);
            this.decompressDialog.setStateToDecompress();
        }

        decompressFile();
    }

    /**
     * lzma formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği
     * method. İlk olarak açma işleminin gerçekleştirileceği dizinde aynı isimde başka bir dosya
     * olup olmadığı overwrite değişkeni ile birlikte kontrol edilir. Sıkıştırılmış olan dosyayı
     * girdi olarak alan bir BufferedInputStream nesnesi oluşturulur. Açılmış veriyi yazmak üzere
     * de bir BufferedOutputStream nesnesi oluşturulur. Sıkıştırılmış veriyi açma işlemini yapan
     * Decoder nesnesinin Code methoduna  BufferedInputStream ve BufferedOutputStream nesneleri
     * gönderilerek dosya açma işlemi gerçekleştirilir.
     */
    private void decompressFile() {

        boolean fileExist = false;
        BufferedInputStream inStream = null;
        BufferedOutputStream outStream = null;
        try {

            if (outFile.exists() && overwrite == false) {
                fileExist = true;
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
            } else {

                inStream = new BufferedInputStream(new FileInputStream(inFile));
                outStream = new BufferedOutputStream(new FileOutputStream(outFile));

                int propertiesSize = 5;
                byte[] properties = new byte[propertiesSize];
                if (inStream.read(properties, 0, propertiesSize) != propertiesSize) {
                    throw new Exception(LzmaShortFileError);
                }
                
                Decoder decoder = new Decoder(decompressDialog);
                
                if (!decoder.SetDecoderProperties(properties)) {
                    throw new Exception(LzmaPropertiesError);
                }
                
                long outSize = 0;
                for (int i = 0; i < 8; i++) {
                    int v = inStream.read();
                    if (v < 0) {
                        throw new Exception(LzmaStreamSizeError);
                    }
                    outSize |= ((long) v) << (8 * i);
                }

                if (!decoder.Code(inStream, outStream, outSize)) {
                    throw new Exception(LzmaDataStreamError);
                }
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
                if (decompressDialog != null) {

                    if (decompressDialog.isCanceled() && fileExist == false) {
                        outFile.delete();
                    }
                }
            } catch (Exception ex) {
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                outFile.delete();
            }
        }
    }

    /**
     * tar.lzma formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği
     * method. Lzma formatına ait bir inputstream nesnesi yoktur. Bu yüzden tar.lzma dosyasını 
     * açma işlemi ilk olarak lzma formatıyla sıkıştırılmış bir dosya gibi
     * açılması, daha sonra da bu dosyanın tar arşivinden çıkarılması şeklinde gerçekleşir.
     */
    private void decompressAndUntarFile() {

        try {
            
            String tempFilePath;

            tempFilePath = "tmp" + File.separator + "temp.tar";
            outFile = new File(tempFilePath);

            decompressFile();

            if (!decompressDialog.isCanceled()) {

                inFile = outFile;
                ExtractArchive untarFile = new ExtractArchive(inFile, outFileParent, overwrite, decompressDialog);
                inFile.delete();
            }
        } catch (Exception ex) {
            decompressDialog.setVisible(false);
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
