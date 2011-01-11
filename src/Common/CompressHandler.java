package Common;

import java.io.File;
import Gui.StatusDialog;
import javax.swing.JOptionPane;

/**
 * Dosyaların sıkıştırma işlemlerinin yürütüldüğü sınıf.
 */
public class CompressHandler implements MainVocabulary, Runnable {

    String className = CompressHandler.class.getName();
    String compressionFormat, password;
    int partSize;
    File inFile, outFile;
    StatusDialog compressDialog;
    boolean overwrite = false, encrypt = false, multiPart = false;
    Thread compressThread;
    BZip2.Compress tarBz2Compress;
    GZip.Compress tarGzCompress;
    Lzma.Compress tarLzmaCompress;
    Tar.CreateArchive tarArchive;
    Zip.Compress zipCompress;
    Lzma.Compress lzmaCompress;
    GZip.Compress gzCompress;
    BZip2.Compress bz2Compress;

    /**
     * Dosyanın şifrelenerek sıkıştırılmasının istenmesi durumunda Common.CompressHandler sınıfının
     * nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile Sıkıştıralacak olan kaynak dosyayı tutan değişken.
     * @param outFile Dosyanın sıkışıtıralacağı hedef dosyayı tutan değişken. 
     * @param compressionFormat Sıkıştırma formatını tutan değişken.
     * @param password Verinin şifrelenmesinde kullanılacak şifreyi tutan değişken.
     * @param overwrite  Sıkıştırılmış dosyanın yazılacağı dizinde aynı isimde başka 
     * bir dosya var ise üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     */
    public CompressHandler(File inFile, File outFile, String compressionFormat, String password, boolean overwrite) {

        this.outFile = outFile;
        this.inFile = inFile;
        this.compressionFormat = compressionFormat;
        this.overwrite = overwrite;
        this.password = password;
        encrypt = true;

        compressDialog = new StatusDialog();
    }

    /**
     * Dosyanın parçalı dosyalar şeklinde sıkıştırılmasının istenmesi durumunda Common.CompressHandler 
     * sınıfının nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile Sıkıştıralacak olan kaynak dosyayı tutan değişken.
     * @param outFile Dosyanın sıkışıtıralacağı hedef dosyayı tutan değişken. 
     * @param compressionFormat Sıkıştırma formatını tutan değişken.
     * @param partSize Dosya parça büyüklüğünü megabyte cinsinden tutan değişken.
     * @param overwrite  Sıkıştırılmış dosyanın yazılacağı dizinde aynı isimde başka 
     * bir dosya var ise üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     */
    public CompressHandler(File inFile, File outFile, String compressionFormat, int partSize, boolean overwrite) {

        this.outFile = outFile;
        this.inFile = inFile;
        this.compressionFormat = compressionFormat;
        this.overwrite = overwrite;
        this.partSize = partSize;
        multiPart = true;

        compressDialog = new StatusDialog();
    }

    /**
     * Dosyanın parçalı dosyalar şeklinde ve şifreli olarak sıkıştırılmasının istenmesi durumunda 
     * Common.CompressHandler sınıfının nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile Sıkıştıralacak olan kaynak dosyayı tutan değişken.
     * @param outFile Dosyanın sıkışıtıralacağı hedef dosyayı tutan değişken. 
     * @param compressionFormat Sıkıştırma formatını tutan değişken.
     * @param password Verinin şifrelenmesinde kullanılacak şifreyi tutan değişken.
     * @param partSize Dosya parça büyüklüğünü megabyte cinsinden tutan değişken.
     * @param overwrite  Sıkıştırılmış dosyanın yazılacağı dizinde aynı isimde başka 
     * bir dosya var ise üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     */
    public CompressHandler(File inFile, File outFile, String compressionFormat, String password, int partSize, boolean overwrite) {

        this.outFile = outFile;
        this.inFile = inFile;
        this.compressionFormat = compressionFormat;
        this.overwrite = overwrite;
        this.password = password;
        this.partSize = partSize;
        encrypt = true;
        multiPart = true;

        compressDialog = new StatusDialog();
    }
   
    /**
     * Dosyanın sıkıştırılmasının istenmesi durumunda Common.CompressHandler sınıfının nesnesinin 
     * oluşturulduğu kurucu methodu. 
     * @param inFile Sıkıştıralacak olan kaynak dosyayı tutan değişken.
     * @param outFile Dosyanın sıkışıtıralacağı hedef dosyayı tutan değişken. 
     * @param compressionFormat Sıkıştırma formatını tutan değişken.
     * @param overwrite  Sıkıştırılmış dosyanın yazılacağı dizinde aynı isimde başka bir dosya var 
     * ise üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     */
    public CompressHandler(File inFile, File outFile, String compressionFormat, boolean overwrite) {

        this.outFile = outFile;
        this.inFile = inFile;
        this.compressionFormat = compressionFormat;
        this.overwrite = overwrite;

        compressDialog = new StatusDialog();
    }

    /**
     * Kullanılan kurucu fonksiyonuna göre dosya üzerinde sıkıştırma, şifreleme, parçalama 
     * işlemlerinin yapıldığı method. Dosyanın sadece sıkıştırılması isteniyorsa compressionFormat
     * değişkeninin değerine göre uygun sıkıştırma sınıfının nesnesi yaratılarak sıkıştırma işlemi
     * gerçekleştirilir. Sıkıştırmanın yanı sıra şifreleme ve parçalama işlemlerinin de yapılması 
     * isteniyorsa sırayla ilk önce sıkıştırma daha sonra şifreleme ve son olarak da parçalama 
     * işlemleri gerçekleştirilir. Bu işlemler sırasında tempPath değişkeninin gösterdiği dizinde 
     * geçici dosyalar oluşturulur. Bütün işlemler bitince bu dosyalar silinir.
     */
    public void processCompress() {

        String outFilePath = null;
        File tempFile1 = null, tempFile2 = null;

        if (outFile.exists() && overwrite == false) {
            compressDialog.setVisible(false);
            JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
        } else {
            try {
                FileOperations.createTempDirectory();
                
                if (encrypt && multiPart) {
                    outFilePath = outFile.getAbsolutePath();
                    tempFile1 = new File(tempPath + File.separator + outFile.getName() + ".temp1");
                    tempFile2 = new File(tempPath + File.separator + outFile.getName() + ".temp2");
                    outFile = tempFile1;
                } else if (encrypt || multiPart) {
                    outFilePath = outFile.getAbsolutePath();
                    tempFile1 = new File(tempPath + File.separator + outFile.getName() + ".temp1");
                    outFile = tempFile1;
                }

                if (compressionFormat.equals("tar.bz2")) {
                    tarBz2Compress = new BZip2.Compress(inFile, outFile, true, compressDialog);
                } else if (compressionFormat.equals("tar.gz")) {
                    tarGzCompress = new GZip.Compress(inFile, outFile, true, compressDialog);
                } else if (compressionFormat.equals("tar.lzma")) {
                    tarLzmaCompress = new Lzma.Compress(inFile, outFile, true, compressDialog);
                } else if (compressionFormat.equals("tar")) {
                    tarArchive = new Tar.CreateArchive(inFile, outFile, compressDialog);
                } else if (compressionFormat.equals("zip")) {
                    zipCompress = new Zip.Compress(inFile, outFile, compressDialog);
                } else if (compressionFormat.equals("lzma")) {
                    lzmaCompress = new Lzma.Compress(inFile, outFile, false, compressDialog);
                } else if (compressionFormat.equals("gz")) {
                    gzCompress = new GZip.Compress(inFile, outFile, false, compressDialog);
                } else if (compressionFormat.equals("bz2")) {
                    bz2Compress = new BZip2.Compress(inFile, outFile, false, compressDialog);
                }
       
                if (encrypt && !compressDialog.isCanceled()) {
                    Encrypter encryptFile = new Encrypter(password, compressDialog);
                    
                    if (multiPart) {
                        encryptFile.encrypt(2, outFile.getAbsolutePath(), tempFile2.getAbsolutePath());
                    } else {
                        encryptFile.encrypt(2, outFile.getAbsolutePath(), outFilePath);
                    }
                }

                if (multiPart && !compressDialog.isCanceled()) {
                    if(encrypt && (partSize < tempFile2.length())) {
                        FileOperations.splitFiles(tempFile2, outFilePath, partSize, compressDialog);
                    } else if(encrypt) {
                        outFile = new File(outFilePath);
                        FileOperations.copyFile(tempFile2, outFile);
                    } else if(partSize < tempFile1.length()) {
                        FileOperations.splitFiles(tempFile1, outFilePath, partSize, compressDialog);
                    } else {
                        outFile = new File(outFilePath);
                        FileOperations.copyFile(tempFile1, outFile);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (tempFile1 != null) {
                    tempFile1.delete();
                }
                if (tempFile2 != null) {
                    tempFile2.delete();
                }
                if (!compressDialog.isCanceled()) {
                    compressDialog.completeDialog();
                }
            }
        }
    }

    /**
     * Threadi başlatmakta kullanılan method.
     */
    public void run() {
        processCompress();
    }
}