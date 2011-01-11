package Tar;

import external.publicDomain.tar.TarEntry;
import external.publicDomain.tar.TarOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import Common.FileOperations;
import Common.MainVocabulary;
import Gui.StatusDialog;
import javax.swing.JOptionPane;

/**
 * Bu sınıf dosyaları tar formatında arşivlemekte veya tar.bz2 ve tar.gz formatlarında 
 * sıkıştırmakta kullanılır.
 */
public class CreateArchive implements MainVocabulary {

    String className = CreateArchive.class.getName();
    BufferedInputStream inStream;
    int BUFFERSIZE = 1024;
    File inFile, outFile;
    TarOutputStream outStream;
    StatusDialog archiveDialog;

    /**
     * tar formatında arşivleme yapmak için kullanılan Tar.CreateArchive sınıfının nesnesini
     * oluşturan kurucu methodu.
     * @param inFile Arşivlenecek olan kaynak dosyayı gösteren değişken.
     * @param outFile Arşivlenecek olan dosyanın yazılacağı hedef dosyayı gösteren değişken.
     * @param  dialog Arşivleme işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
     * önceden oluşturulmuş olan nesnesine ait değişken.
     */
    public CreateArchive(File inFile, File outFile, StatusDialog dialog) {

        this.inFile = inFile;
        this.outFile = outFile;

        archiveDialog = dialog;
        archiveDialog.setIndeterminate(false);
        archiveDialog.setStateToCompress();
        
        archiveFile();

    }

   /**
    * Bir outputstream nesnesi kullanarak tar.bz2 veya tar.gz formatlarında sıkıştırma yapmak için 
    * kullanılan Tar.CreateArchive sınıfının nesnesini oluşturan kurucu methodu.
    * @param outStream Sıkıştırılacak olan dosyayı sıkıştırmakta kullanılacak outputstream değişkeni.
    * @param inFile Sıkıştırılacak olan kaynak dosyayı gösteren değişken.
    * @param outFile Sıkıştırılacak olan dosyanın yazılacağı hedef dosyayı gösteren değişken.
    * @param dialog Sıkıştırma işlemi esnasında görüntülenen StatusDialog arayüzünün daha 
    * önceden oluşturulmuş olan nesnesine ait değişken..
    */
    public CreateArchive(OutputStream outStream, File inFile, File outFile, StatusDialog dialog) {

        this.outStream = new TarOutputStream(outStream);
        this.outFile = outFile;
        this.inFile = inFile;

        archiveDialog = dialog;
        archiveDialog.setIndeterminate(false);
        archiveDialog.setStateToCompress();
        
        archiveFile();
    }

    /**
     * Kullanılmış olan kurucu methoduna göre tar formatında arşivleme ya da tar.bz2 veya tar.gz 
     * formatlarında sıkıştırma yapmakta kullanılan method. Arşivleme işleminde TarOutputStream
     * ve TarEntry sınıfları kullanılır. Sıkıştırılacak olan dosya bir dizin ise dosyanın içerdiği
     * herbir altdosya için TarEntry nesneleri oluşturarak, bu TarEntryler TarOutputStream nesnesinin 
     * putNextEntry methodu aracılığıyla sıkıştırılır. Sıkıştırılacak olan tek bir dosya ise bir 
     * BufferedInputStream nesnesi oluşturularak, bir döngü aracılığıyla BufferedInputStream deki 
     * bütün veriler okunarak TarOutputStreame yazılır.
     */
    private void archiveFile() {

        int diff = 0;
        String parentPath, outFilePath;
        outFilePath = outFile.getAbsolutePath();

        if(System.getProperty("os.name").startsWith("Windows")) {
            diff += 2;
        }
        
        try {

            int len;
            long fileSize = 0, completedSize = 0;

            fileSize = FileOperations.getFileSizeInBytes(inFile);

            if (outStream == null) {
                outStream = new TarOutputStream(new FileOutputStream(outFilePath));
            }

            TarEntry tEntry;
            parentPath = inFile.getParent();
            tEntry = new TarEntry(inFile);

            if (inFile.isDirectory()) {

                List<File> innerList = new ArrayList<File>();
                innerList = FileOperations.listOfFiles(inFile);

                flag:
                for (File innerFile : innerList) {

                    if (innerFile.isDirectory()) {

                        tEntry = new TarEntry(innerFile);
                        tEntry.setName(tEntry.getName().substring(parentPath.length() - diff, tEntry.getName().length()));
                        outStream.putNextEntry(tEntry);

                        if (tEntry.getFile() != inFile) {
                            completedSize += emptyDirectorySize;
                            archiveDialog.setStatus(completedSize, fileSize);
                        }

                    } else {

                        if (innerFile.length() > 0) {

                            tEntry = new TarEntry(innerFile);
                            tEntry.setName(tEntry.getName().substring(parentPath.length() - diff, tEntry.getName().length()));
                            outStream.putNextEntry(tEntry);

                            inStream = new BufferedInputStream(new FileInputStream(innerFile));
                            byte[] buffer = new byte[BUFFERSIZE];

                            while ((len = inStream.read(buffer)) > 0 && !archiveDialog.isCanceled()) {

                                outStream.write(buffer, 0, len);

                                completedSize += len;
                                archiveDialog.setStatus(completedSize, fileSize);
                            }

                            if (!archiveDialog.isCanceled()) {
                                outStream.closeEntry();
                            } else {
                                break flag;
                            }
                        }
                    }
                }
            } else {

                if (inFile.length() > 0) {

                    tEntry.setName(tEntry.getName().substring(parentPath.length() - diff, tEntry.getName().length()));
                    outStream.putNextEntry(tEntry);

                    inStream = new BufferedInputStream(new FileInputStream(inFile));
                    byte[] buffer = new byte[BUFFERSIZE];

                    while ((len = inStream.read(buffer)) > 0 && !archiveDialog.isCanceled()) {

                        outStream.write(buffer, 0, len);

                        completedSize += len;
                        archiveDialog.setStatus(completedSize, fileSize);
                    }

                    if (!archiveDialog.isCanceled()) {
                        outStream.closeEntry();
                    }
                }
            }

        } catch (Exception ex) {
            archiveDialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            outFile.delete();
        } finally {
            try {
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
                
                if (inStream != null) {
                    inStream.close();
                }
                
                if (archiveDialog.isCanceled()) {
                    outFile.delete();
                }
            } catch (Exception ex) {
                archiveDialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                outFile.delete();
            }
        }
    }
}
