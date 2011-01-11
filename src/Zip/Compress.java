package Zip;

import Common.FileOperations;
import Common.MainVocabulary;
import Gui.StatusDialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

/**
 * Bu sınıf dosyaları zip formatında sıkıştırmakta kullanır. 
 */
public class Compress implements MainVocabulary {

    String className = Compress.class.getName();
    BufferedInputStream inStream;
    int BUFFERSIZE = 1024;
    String password;
    ZipOutputStream outStream;
    File inFile, outFile;
    StatusDialog compressDialog;

    /**
     * Dosyaları zip formatında sıkıştırmak için kullanılan Zip.Compress sınıfının nesnesini 
     * oluşturan kurucu methodu.
     * @param inFile Sıkıştıralacak olan kaynak dosyayı gösteren değişken.
     * @param outFile Sıkıştırılacak olan dosyanın yazılacağı hedef dosyayı gösteren değişken.
     * @param  dialog Sıkıştırma işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
     * önceden oluşturulmuş olan nesnesine ait değişken.
     */
    public Compress(File inFile, File outFile, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFile = outFile;
        
        compressDialog = dialog;
        compressDialog.setIndeterminate(false);
        compressDialog.setStateToCompress();
        
        compressFile();
    }

    /**
     * inFile dosyasını zip fomatında sıkıştırmakta kullanılan method. Sıkıştırma işleminde
     * ZipOutputStream ve ZipEntry sınıfları kullanılır. Sıkıştırılacak olan dosya bir dizin
     * ise dizinde bulunan bütün alt dosyalar için birer ZipEntry nesnesi oluşturulur ve 
     * ZipOutputStreamin putNextEntry methoduyla ZipOutputStreame ZipEntry nin ilk kısmı yazılır.
     * Daha sonra bir BufferedInputStream nesnesi oluşturularak, bir döngü aracılığıyla her seferinde 
     * altdosyaya ait BUFFERSIZE kadar veri ZipOutputStreame yazılarak ZipEntry tamamlanır. Diğer bütün
     * altdosyalar için aynı işlem tekrarlanarak dosyanın sıkıştırılması tamamlanır.
     */
    private void compressFile() {

        String destPath, parentPath;;

        try {
            int len;
            boolean fileFound = false;
            long fileSize = 0, completedSize = 0;
            ZipEntry zEntry;
            
            destPath = outFile.getAbsolutePath();
            outStream = new ZipOutputStream(new FileOutputStream(destPath));

            fileSize = FileOperations.getFileSizeInBytes(inFile);

            parentPath = inFile.getParent();
            if (inFile.isDirectory()) {

                List<File> innerList = new ArrayList<File>();
                innerList = FileOperations.listOfFiles(inFile);

                flag:
                for (File innerFile : innerList) {
                    
                    if (!innerFile.isDirectory()) {
                        
                        if (innerFile.length() > 0) {

                            fileFound = true;
                            zEntry = new ZipEntry(innerFile.getAbsolutePath().substring(parentPath.length(), innerFile.getAbsolutePath().length()));
                            outStream.putNextEntry(zEntry);

                            inStream = new BufferedInputStream(new FileInputStream(innerFile));
                            byte[] buffer = new byte[BUFFERSIZE];

                            while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled()) {

                                outStream.write(buffer, 0, len);
                                
                                completedSize += len;
                                compressDialog.setStatus(completedSize, fileSize);
                            }

                            if (compressDialog.isCanceled()) {
                                break flag;
                            }
                            outStream.closeEntry();
                        }
                    } else {
                        if(innerFile != inFile )
                            completedSize += emptyDirectorySize;
                    }
                }
                
               /* if(fileFound == false) {
                    compressDialog.cancelDialog();
                    JOptionPane.showMessageDialog(null,ZipFileNotFoundError, "Error!", JOptionPane.ERROR_MESSAGE);
                }*/
            } else {
                if (inFile.length() > 0) {

                    zEntry = new ZipEntry(inFile.getAbsolutePath().substring(0 + parentPath.length(), inFile.getAbsolutePath().length()));
                    outStream.putNextEntry(zEntry);

                    inStream = new BufferedInputStream(new FileInputStream(inFile));
                    byte[] buffer = new byte[BUFFERSIZE];

                    while ((len = inStream.read(buffer)) > 0 && !compressDialog.isCanceled()) {

                        outStream.write(buffer, 0, len);

                        completedSize += len;
                        compressDialog.setStatus(completedSize, fileSize);
                    }

                    outStream.closeEntry();
                }
            }
        } catch (Exception ex) {
            compressDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                
                if (inStream != null) {
                    inStream.close();
                }

                if (compressDialog.isCanceled()) {
                    outFile.delete();
                }
            } catch (Exception ex) {
                compressDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                outFile.delete();
            }
        }
    }
}
