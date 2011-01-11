package Zip;

import Common.FileOperations;
import Common.MainVocabulary;
import Gui.StatusDialog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JOptionPane;

/**
 * Bu sınıf zip formatında sıkıştırılmış olan dosyaları açmakta kullanılır.
 */
public class Decompress implements MainVocabulary {

    String className = Decompress.class.getName();
    int BUFFERSIZE = 1024;
    ZipInputStream inStream;
    BufferedOutputStream outStream;
    File inFile, outFileParent;
    StatusDialog decompressDialog;
    boolean overwrite;

    /**
     * Zip formatında sıkıştırılmış dosyaları açmakta kullanılan Zip.Decompress sınıfının nesnesini 
     * oluşturan kurucu methodu.
     * @param inFile Sıkıştırılmış halde olan dosyayı gösteren değişken.
     * @param outFileParent Dosyayının açılacağı hedef dizini gösteren değişken.
     * @param overwrite Dosyanın açılacağı dizinde aynı isimde başka bir dosya var ise 
     * üzerine yazma işleminin yapılıp yapılmayacağını gösteren değişken.
     * @param  dialog Dosya açma işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
     * önceden oluşturulmuş olan nesnesine ait değişken.
     */
    public Decompress(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.overwrite = overwrite;

        decompressDialog = dialog;
        decompressDialog.setIndeterminate(false);
        decompressDialog.setStateToDecompress();
        
        decompressFile();
    }

    /**
     * zip formatında sıkıştırılmış olan inFile dosyasının açma işleminin gerçekleştirildiği method.
     * Dosyayı açma işleminde ZipInputStream ve ZipEntry sınıfları kullanılır. ZipInputStreamin 
     * getNextEntry methodu kullanılarak ZipEntry nesnesi okunur ve ZipInputStream nesnesinin stream 
     * içerisindeki pozisyonu ZipEntry verisinin başlangıcına getirilir. Daha sonra ZipInputStreamde
     * bulunan ZipEntry bir döngü aracılığıyla bir BufferedOutputStreame yazılır. Bu işlem ZipInputStream 
     * içerisindeki bütün ZipEntryler için tekrarlanarak dosya açma işlemi tamamlanır.
     */
    private void decompressFile() {

        boolean fileExist = false;
        File outFile = null;

        try {

            int len;
            long totalEntry = 0, currentEntry = 0;
            String outFileParentPath, outFileName, inFilePath;
            
            inFilePath = inFile.getAbsolutePath();
            outFileParentPath = outFileParent.getAbsolutePath();
            
            inStream = new ZipInputStream(new FileInputStream(inFilePath));
            ZipEntry zEntry = inStream.getNextEntry();

            int index = zEntry.getName().indexOf(File.separator, 1);
            if (index > 0) {
                outFileName = zEntry.getName().substring(1, index);
            } else {
                outFileName = zEntry.getName();
            }
            
            outFile = new File(outFileParentPath + File.separator + outFileName);

            if (outFile.exists() && overwrite == false) {
                fileExist = true;
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
            } else {

                ZipFile zipArc = new ZipFile(inFile);
                totalEntry = zipArc.size();

                while (zEntry != null && !decompressDialog.isCanceled()) {

                    currentEntry++;
                    File dFile = new File(outFileParentPath + zEntry.getName());

                    if (!zEntry.isDirectory()) {

                        File makeDir = new File(dFile.getParent());
                        
                        if (!makeDir.exists()) {
                            makeDir.mkdirs();
                        }
                        
                        if (!dFile.exists() || overwrite) {

                            outStream = new BufferedOutputStream(new FileOutputStream(dFile), BUFFERSIZE);
                            byte[] data = new byte[BUFFERSIZE];

                            while ((len = inStream.read(data, 0, BUFFERSIZE)) > 0 && !decompressDialog.isCanceled()) {
                                outStream.write(data, 0, len);
                            }
                            outStream.close();
                        }
                    } else {
                        dFile.mkdirs();
                    }

                    decompressDialog.setStatus(currentEntry, totalEntry);

                    zEntry = inStream.getNextEntry();
                }
            }

        } catch (Exception ex) {
            decompressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            FileOperations.deleteDirectory(outFile);
        } finally {
            try {

                if (inStream != null) {
                    inStream.close();
                }
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }

                if (decompressDialog.isCanceled() && fileExist == false) {
                    FileOperations.deleteDirectory(outFile);
                }

            } catch (Exception ex) {
                decompressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                FileOperations.deleteDirectory(outFile);
            }
        }
    }
}