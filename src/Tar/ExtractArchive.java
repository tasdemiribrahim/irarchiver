package Tar;

import Common.FileOperations;
import Common.MainVocabulary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import external.publicDomain.tar.TarEntry;
import external.publicDomain.tar.TarInputStream;
import java.io.InputStream;
import Gui.StatusDialog;
import javax.swing.JOptionPane;

/**
 * Bu sınıf tar olarak arşivlenmiş veya tar.bz2 ve tar.gz olarak sıkıştırılmış dosyaları çıkartmakta 
 * kullanılır. 
 */
public class ExtractArchive implements MainVocabulary {

    String className = ExtractArchive.class.getName();
    File inFile, outFileParent;
    TarInputStream inStream;
    FileOutputStream outStream;
    boolean overwrite;
    StatusDialog archiveDialog;


    /**
     * tar arşivlerini açmakta kullanılan Tar.ExtractArchive sınıfının nesnesini oluşturan 
     * kurucu methodu.
     * @param inFile Arşivlenmiş halde olan dosyayı gösteren değişken.
     * @param outFileParent Dosyayının açılacağı hedef dizini gösteren değişken.
     * @param overwrite Dosyanın açılacağı dizinde aynı isimde başka bir dosya var ise 
     * üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     * @param dialog Arşivi açma işlemi esnasında görüntülenen StatusDialog arayüzünün daha önceden 
     * oluşturulmuş olan nesnesine ait değişken.
     */
    public ExtractArchive(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.overwrite = overwrite;
        
        archiveDialog = dialog;
        archiveDialog.setIndeterminate(true);
        archiveDialog.setStateToDecompress();
        
        extractFile();
    }

    /**
     * Bir inputstream nesnesi kullanarak tar.bz2 veya tar.gz formatlarında sıkıştırılmış dosyaları
     * açmak için kullanılan Tar.ExtractArchive sınıfının nesnesini oluşturan kurucu methodu.
     * @param inStream açılacak olan sıkıştırılmış dosyayı açmakta kullanılacak inputstream değişkeni.
     * @param inFile Sıkıştırılmış halde olan dosyayı gösteren değişken.
     * @param outFileParent Dosyayının açılacağı hedef dizini gösteren değişken.
     * @param overwrite Dosyanın açılacağı dizinde aynı isimde başka bir dosya var ise 
     * üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     * @param dialog Sıkıştırma işlemi esnasında görüntülenen StatusDialog arayüzünün daha önceden 
     * oluşturulmuş olan nesnesine ait değişken.
     */
    public ExtractArchive(InputStream inStream, File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.overwrite = overwrite;
        this.inStream = new TarInputStream(inStream);

        archiveDialog = dialog;
        archiveDialog.setIndeterminate(true);
        archiveDialog.setStateToDecompress();
        
        extractFile();
    }

    /**
     * Kullanılmış olan kurucu methoduna göre tar formatında arşivlenmiş ya da tar.bz2 veya tar.gz 
     * formatlarında sıkıştırılmış dosyaları açmakta kullanılan method. Dosya çıkarma işleminde 
     * TarInputStream  ve TarEntry nesneleri kullanılır. Oluşturulan TarInputStream nesnesi
     * içerisinde bulunan TarEntrylere bir döngü aracılığıyla teker teker erişilip, herbir TarEntrynin
     * TarInputStream nesnesinin bir methodu olan copyEntryContents aracılığıyla bir FileInputStreame 
     * yazılmasıyla dosya çıkarma işlemi gerçekleştirilir.
     */
    private void extractFile() {

        boolean fileExist = false;
        String outFileName = null, outFileParentPath = null;
        File outFile = null;
        
        try {

            if (inStream == null) {
                inStream = new TarInputStream(new FileInputStream(inFile.getAbsolutePath()));
            }
           
            outFileParentPath = outFileParent.getAbsolutePath();
            TarEntry tEntry = inStream.getNextEntry();
            outFileName = tEntry.getName();

            outFile = new File(outFileParentPath + File.separator + outFileName);
           
            if(outFile.exists() && overwrite == false) {
                fileExist = true;
                archiveDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);  
            } else {
 
                while (tEntry != null && !archiveDialog.isCanceled()) {

                    File dFile = new File(outFileParentPath + File.separator + tEntry.getName());

                    if (!tEntry.isDirectory()) {

                        File mkDir = new File(dFile.getParent());

                        if (!mkDir.exists()) {
                            mkDir.mkdir();
                        }
                        if (!dFile.exists() || overwrite) {
                            outStream = new FileOutputStream(dFile);
                            inStream.copyEntryContents(outStream);
                            outStream.close();
                        }

                    } else {
                        dFile.mkdirs();
                    }                   
                        
                    tEntry = inStream.getNextEntry();
                }
            }
        } catch (Exception ex) {    
            archiveDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            FileOperations.deleteDirectory(outFile);
        } finally {
            try {     
                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.close();
                }

                if (archiveDialog.isCanceled() && fileExist == false) {
                    FileOperations.deleteDirectory(outFile);
                }
            } catch (Exception ex) {
                archiveDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                FileOperations.deleteDirectory(outFile);
            }
        }
    }
}