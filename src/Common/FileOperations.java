package Common;

import Gui.StatusDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * İçerisinde kullanılmakta olan bazı dosya işlemlerinin tanımlı olduğu sınıf.
 */
public class FileOperations implements MainVocabulary {

    static String className = FileOperations.class.getName();

    /**
     * Parametre olarak aldığı bir dizinin boyutunun kaç byte olduğunu hesaplayan method.
     * @param directory Boyutu belirlenecek dizini gösteren değişken.
     * @return Dizin boyutunun kaç byte olduğu.
     */
    private static long getDirectorySize(File directory) {

        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        long size = 0;

        File[] files = directory.listFiles();
        if (files == null) {
            return emptyDirectorySize;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                size += getDirectorySize(file) + emptyDirectorySize;
            } else {
                size += file.length();
            }
        }


        return size;
    }

    /**
     * Parametre olarak aldığı dosyanın boyutunun kaç megabyte olduğunu hesaplayan method.
     * @param directory Boyutu belirlenecek dosyayı gösteren değişken.
     * @return Dosya boyutunun kaç megabyte olduğu.
     */
    public static double getFileSizeInMegabytes(File file) {

        double size;
        if (file.isDirectory()) {
            size = (double) getDirectorySize(file) / 1024 / 1024;
        } else {
            size = (double) file.length() / 1024 / 1024;
        }
       
        BigDecimal bd = new BigDecimal(size);
        bd = bd.setScale(3, BigDecimal.ROUND_UP);
        size = bd.doubleValue();

        return size;
    }

    /**
     * Parametre olarak aldığı dosyanın boyutunun kaç kilobyte olduğunu hesaplayan method.
     * @param directory Boyutu belirlenecek dosyayı gösteren değişken.
     * @return Dosya boyutunun kaç kilobyte olduğu.
     */
    public static double getFileSizeInKilobytes(File file) {

        double size;
        if (file.isDirectory()) {
            size = (double) getDirectorySize(file) / 1024;
        } else {
            size = (double) file.length() / 1024;
        }
        
        BigDecimal bd = new BigDecimal(size);
        bd = bd.setScale(3, BigDecimal.ROUND_UP);
        size = bd.doubleValue();


        return size;
    }

    /**
     * Parametre olarak aldığı dosyanın boyutunun kaç byte olduğunu hesaplayan method.
     * @param directory Boyutu belirlenecek dosyayı gösteren değişken.
     * @return Dosya boyutunun kaç byte olduğu.
     */
    public static long getFileSizeInBytes(File file) {

        long size;
        if (file.isDirectory()) {
            size = getDirectorySize(file);
        } else {
            size = file.length();
        }
        
        return size;
    }

    /**
     * Bir dosyayı farklı bir isimde farklı ya da aynı dizin içine kopyalamakta kullanılan method.
     * @param source Kopyalanacak kaynak dosyayı gösteren değişken.
     * @param destination Dosyanın kopyalanacağı hedef dosyayı gösteren değişken.
     */
    public static void copyFile(File source, File destination) {

        FileChannel srcChannel = null, dstChannel = null;

        try {
            srcChannel = new FileInputStream(source).getChannel();
            dstChannel = new FileOutputStream(destination).getChannel();

            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        } finally {

            try {
                if (srcChannel != null) {
                    srcChannel.close();
                }
                if (dstChannel != null) {
                    dstChannel.close();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Dizin silmekte kullanılan method.
     * @param inFile Silinecek dizini gösteren değişken.
     * @return Silme işlemi başarıyla gerçekleşmiş ise true, aksi halde false.
     */
    public static boolean deleteDirectory(File inFile) {

        if (inFile.exists()) {

            File[] files = inFile.listFiles();

            for (int i = 0; i < files.length; i++) {

                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
            return inFile.delete();

        } else {
            return false;
        }
    }

    /**
     * Dosyaları parçalamakta kullanılan method.
     * @param inFile Parçalanacak dosyayı gösteren değişken.
     * @param outFilePath Dosyanın parçalanacağı dizini gösteren değişken.
     * @param partSize Parçalanacak herbir dosyanın boyutunu gösteren değişken.
     * @param dialog Dosya parçalama işlemi esnasında görüntülenen arayüz.
     */
    public static void splitFiles(File inFile, String outFilePath, int partSize, StatusDialog dialog) {

        dialog.setStateToSplitFile();
        
        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        int count = 1,  len = 0,  partNumber;
        long inFileSize;

        try {

            inStream = new FileInputStream(inFile);
            byte[] buffer = new byte[partSize];

            inFileSize = inFile.length();

            BigDecimal bd = new BigDecimal((double) inFileSize / partSize);
            bd = bd.setScale(0, BigDecimal.ROUND_UP);
            partNumber = (int) bd.doubleValue();

            String fileName;
            while ((len = inStream.read(buffer, 0, partSize)) > 0 && !dialog.isCanceled()) {

                fileName = outFilePath + ".part" + count;
                outStream = new FileOutputStream(fileName);

                if (count == 1) {
                    outStream.write(partNumber);
                }
                outStream.write(buffer, 0, len);
                outStream.flush();
                outStream.close();

                ++count;
            }
        } catch (Exception ex) {
            dialog.cancelDialog();
            JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        } finally {

            try {

                if (inStream != null) {
                    inStream.close();
                }

                if (dialog != null) {
                    if (dialog.isCanceled()) {

                        for (int i = 1; i < count; i++) {
                            new File(outFilePath + ".part" + i).delete();
                        }
                    }
                }
            } catch (IOException ex) {
                dialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Parçalanmış halde olan dosyaları birleştirmekte kullanılan method.
     * @param inFile Parçalanmış olan dosyaların ilk parçasını gösteren değişken.
     * @param outFile Dosyaların birleştirileceği hedef dosya.
     * @param dialog Dosya birleştirme işlemi esnasında görüntülenen arayüz.
     */
    public static void joinFiles(File inFile, File outFile, StatusDialog dialog) {

        dialog.setStateToJoinFile();

        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        int index = inFile.getName().lastIndexOf('.');
        String inFileName = inFile.getName().substring(0, index);
        String partNumber = inFile.getName().substring(index + 1);


        if (!partNumber.equals("part1")) {
            JOptionPane.showMessageDialog(null, partedFileNotExistError, "Error!", JOptionPane.ERROR_MESSAGE);
            dialog.cancelDialog();
        } else {

            int partSize = 0, partCount = 0;
            byte buffer[];
            File joinFile;

            try {
                inStream = new FileInputStream(inFile);
                partCount = inStream.read();
                outStream = new FileOutputStream(outFile);

                for (int i = 1; i <= partCount; i++) {

                    joinFile = new File(inFile.getParent() + File.separator + inFileName + ".part" + i);

                    if (!joinFile.exists()) {
                        JOptionPane.showMessageDialog(null, partedFileBrokenError, "Error!", JOptionPane.ERROR_MESSAGE);
                        dialog.cancelDialog();
                        break;
                    } else {

                        partSize = (int) joinFile.length();
                        buffer = new byte[partSize];
                        inStream = new FileInputStream(joinFile);

                        if (i == 1) {
                            inStream.skip(1);
                            partSize--;
                        }

                        inStream.read(buffer, 0, partSize);
                        outStream.write(buffer, 0, partSize);

                    }

                    if (dialog.isCanceled()) {
                        break;
                    }
                }
            } catch (Exception ex) {
                dialog.cancelDialog();
                JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            } finally {

                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (outStream != null) {
                        outStream.close();
                    }

                    if (dialog.isCanceled()) {

                        if (outFile.exists()) {
                            outFile.delete();
                        }
                    }
                } catch (Exception ex) {
                    dialog.cancelDialog();
                    JOptionPane.showMessageDialog(null, "Exception Throwed From: " + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Bir dizin içinde bulunan tüm dosyaları liste şeklinde döndüren method. Dizin içerisindeki dosyaların 
     * bulunması işlemi search methodu tarafından gerçekleştirilir.
     * @param startingDirectory İçerisindeki dosyaların listeleneceği kaynak dizini gösteren değişken.
     * @return Kaynak dizin içerisindeki bütün dosyaların listesi.
     */
    public  static List<File> listOfFiles(File startingDirectory){
        
        if (startingDirectory.exists()){
            try {
                List<File> results = new ArrayList<File>();
                search(startingDirectory, results);
                return results;
            } catch (final IOException ioEx){
              
                ioEx.printStackTrace();
                return null;
            }
        } else {
           return null;
        }
    }
    
    /**
     * Dizin içerisindeki dosyaları listelemekte kullanılan method. Tekrarlamalı bir methoddur.
     * Her seferinde sadece o anki dizin içerisindeki dizin olmayan dosyaları listeye ekler.
     * Alt dizinlere bakmaz.
     * @param directory İçerisindeki dizin olmayan dosyaların listeleneceği dizini gösteren değişken.
     * @param results bulunan dosyaların eklendiği listeyi gösteren değişken.
     */
    private static void search(File directory, Collection<File> results) throws IOException {
        
        results.add(directory);
        
        File[] childFiles = directory.listFiles();
                
        if (childFiles != null) {
        
            for (int i = 0; i < childFiles.length; i++) {
                File childFile = childFiles[i];
                if (childFile.isDirectory()) {
                    search(childFile, results);
                } else {
                    results.add(childFile);
                }
            }
        } 
    }
    
    /**
     * Geçici dosyaların yazıldığı tmp dizininin olup olmadığını kontrol eden, eğer yok ise oluşturan method.
     */
    public static void createTempDirectory() {
        File tempFile = new File(tempPath);
        
        if(!tempFile.exists()) {
           tempFile.mkdir(); 
        }
    }
}