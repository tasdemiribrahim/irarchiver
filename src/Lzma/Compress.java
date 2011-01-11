
package Lzma;

import Common.MainVocabulary;
import Gui.StatusDialog;
import Tar.CreateArchive;
import external.SevenZip.Compression.LZMA.Encoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.swing.JOptionPane;

/**
 * Bu sınıf dosyaları lzma veya tar.lzma formatında sıkıştırmakta kullanır. 
 */
public class Compress implements MainVocabulary{

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

    /**
     * lzma veya tar.lzma formatında sıkıştırma yapabilmek için Lzma.Compress sınıfının nesnesinin 
     * oluşturulduğu kurucu methodu. Nesne oluşturulduktan sonra tar değişkeninin değerine göre, 
     * lzma veya tar.lzma formatında sıkıştırma işlemini gerçekleştirebilmek için uygun method çağırılır.
     * @param inFile Sıkıştıralacak olan kaynak dosyayı gösteren değişken.
     * @param outFile Sıkıştırılacak olan dosyanın yazılacağı hedef dosyayı gösteren değişken.
     * @param tar Sıkıştırma formatının lzma veya tar.lzma dan hangisinin olacağını belirleyen değişken.
     * tar değişkeni true ise tar.lzma, false ise lzma formatı kullanılır.
     * @param dialog Sıkıştırma işlemi esnasında görüntülenen StatusDialog arayüzünün daha önceden 
     * oluşturulmuş olan nesnesine ait değişken.
     */
    public Compress(File inFile, File outFile, boolean tar, StatusDialog dialog){

        this.inFile = inFile;
        this.outFile = outFile;
       
        compressDialog = dialog; 
        compressDialog.setIndeterminate(true);
        compressDialog.setStateToCompress();
        
        if(tar) {
            tarAndCompressFile();
        } else {
            compressFile();
        }
    }   
     
    /**
     * inFile dosyasının sadece tek bir dosyadan oluşması (dizin olmaması) halinde sıkıştırma
     * işlemini lzma formatında gerçekleştiren method. İlk olarak Sıkıştırılacak dosyayı girdi 
     * olarak alan BufferedInputStream ve hedef dizini girdi olarak alan BufferedOutputStream 
     * oluşturulur. Verinin sıkıştırılmasında Encoder sınıfı kullanılır. Sıkıştırma işlemi
     * Encoder sınıfının Code methoduna BufferedInputStream ve BufferedOutputStream nesnelerinin
     * gönderilmesiyle gerçekleştirilir.
     */
    private void compressFile() {

         String inFilePath = inFile.getAbsolutePath();
         String outFilePath = outFile.getAbsolutePath();
         try {

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
             if (Eos) {
                 fileSize = -1;
             } else {
                 fileSize = inFile.length();
             }

             for (int i = 0; i < 8; i++) {
                 
                 outStream.write((int) (fileSize >>> (8 * i)) & 0xFF);
             }
             encoder.Code(inStream, outStream, -1, -1, null);
         } catch (Exception ex) {
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
                 if (compressDialog.isCanceled()){
                     outFile.delete();
                 }
             } catch (Exception ex) {
                 compressDialog.cancelDialog();
                 JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                 outFile.delete();
             }
         }
    }

    /**
     * inFile dosyası bir dizin ise veya sıkıştırma işlemi tar.lzma formatında yapılmak isteniyorsa 
     * kullanılacak method. Lzma formatına ait bir outputstream nesnesi yoktur. Bu yüzden tar.lzma
     * formatında sıkıştırma işlemi sıkıştırılacak olan dosyanın ilk olarak tar olarak arşivlenmesi, 
     * daha sonra da bu arşivin lzma formatında sıkıştırılması şeklinde gerçekleşir. 
     */
    public void tarAndCompressFile() {

        try{
            String tempFilePath = "tmp" + File.separator + "temp.tar";
            File tempFile = new File(tempFilePath);

            tarFile = new CreateArchive(inFile, tempFile, compressDialog);
            inFile = new File(tempFilePath);
            compressDialog.setIndeterminate(true);
            compressFile();

            tempFile.delete();
        } catch (Exception ex){
            compressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
}

