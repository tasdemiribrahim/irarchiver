package Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * Sıkıştırılmış dosyanın formatının, şifrelenmiş olup olmadığının, parçalanmış olup olmadığının 
 * kontrolünün yapıldığı sınıf.
 */
public class DetermineFileType {

    String className = DetermineFileType.class.getName();
    File inFile;
    String fileType;
    
    /**
     * Sıkıştırılmış olan dosyanın formatının belirlenmesinin istenmesi durumunda 
     * Common.DetermineFileType sınıfının nesnesini oluşturmakta kullanılan kurucu methodu.
     * @param filePath formatı belirlenecek sıkıştırılmış dosyanın bulunduğu konum.
     */
    public DetermineFileType(File inFile) {
        this.inFile = inFile;
    }

    /**
     * Dosyanın uzantısına bakılarak basit bir şekilde dosyanın türünün belirlemenye çalışıldığı 
     * method. Eğer dosyanın türü bu şekilde belirlenemezse advanceCheck() methoduna başvurulur.
     * @return Dosyanın formatı belirlenebilirse true, aksi halde false. 
     */
    public boolean simpleCheck() {

        String fileName = inFile.getName();
        int strLen = fileName.length();
        
        if(inFile.isDirectory()) {
            return false;
        } else {   
            if ((strLen - 7) > 0 && fileName.substring(strLen - 7, strLen).equalsIgnoreCase("tar.bz2")) {
                fileType = ".tar.bz2";
                return true;
            } else if ((strLen - 6) > 0 && fileName.substring(strLen - 6, strLen).equalsIgnoreCase("tar.gz")) {
                fileType = ".tar.gz";
                return true;
            } else if ((strLen - 8) > 0 && fileName.substring(strLen - 8, strLen).equalsIgnoreCase("tar.lzma")) {
                fileType = ".tar.lzma";
                return true;
            } else if ((strLen - 3) > 0 && fileName.substring(strLen - 3, strLen).equalsIgnoreCase("tar")) {
                fileType = ".tar";
                return true;
            } else if ((strLen - 3) > 0 && fileName.substring(strLen - 3, strLen).equalsIgnoreCase("zip")) {
                fileType = ".zip";
                return true;
            } else if ((strLen - 3) > 0 && fileName.substring(strLen - 3, strLen).equalsIgnoreCase("bz2")) {
                fileType = ".bz2";
                return true;
            } else if ((strLen - 2) > 0 && fileName.substring(strLen - 2, strLen).equalsIgnoreCase("gz")) {
                fileType = ".gz";
                return true;
            } else if ((strLen - 4) > 0 && fileName.substring(strLen - 4, strLen).equalsIgnoreCase("lzma")) {
                fileType = ".lzma";
                return true;
            } else {
                return advanceCheck();
            }
        } 
    }

    /**
     * Dosyanın başında yer alan ilk iki byte ın okunarak dosya formatının belirlenmeye 
     * çalışıldığu method.
     * @return Dosya formatının belirlenmesi halinde true, aksi halde false.
     */
    private boolean advanceCheck() {
        
        byte[] firstTwo = new byte[2];
        final byte[] gzipConst = {(byte) 31, (byte) 139};
        final byte[] lzmaConst = {(byte) 93, (byte) 0};

        FileInputStream fInStream = null;

        try {
            if(!inFile.isDirectory()) {
                fInStream = new FileInputStream(inFile);
                fInStream.read(firstTwo);

                char[] charHead = {(char) firstTwo[0], (char) firstTwo[1]};
                String arcHead = new String(charHead);

                if (arcHead.equals("BZ")) {
                    fileType = ".bz2";
                    return true;
                } else if (arcHead.equals("PK")) {
                    fileType = ".zip";
                    return true;
                } else if (firstTwo[0] == gzipConst[0] && firstTwo[1] == gzipConst[1]) {
                    fileType = ".gz";
                    return true;
                } else if (firstTwo[0] == lzmaConst[0] && firstTwo[1] == lzmaConst[1]) {
                    fileType = ".lzma";
                    return true;
                }
            } else {
                return false;
            }
            
        } catch (final IOException ex) {
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (fInStream != null) {
                    fInStream.close();
                }
            } catch (final IOException ex) {
                JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    /**
     * Sıkıştırılmış dosyanın şifrelenip şifrelenmediğini kontrol eden method.
     * @return dosya şifrelenmiş ise true, aksi halde false.
     */
    public boolean checkAES() {

        FileInputStream fInStream = null;
        byte[] firstThree = new byte[3];

        try {
            if(!inFile.isDirectory()) {
            
                fInStream = new FileInputStream(inFile);
                fInStream.read(firstThree);

                char[] charHeadAES = {(char) firstThree[0], (char) firstThree[1], (char) firstThree[2]};
                String arcHeadAES = new String(charHeadAES);

                if (arcHeadAES.equals("AES")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }            
        } catch (final IOException ex) {
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                if (fInStream != null) {
                    fInStream.close();
                }
            } catch (final IOException ex) {
                JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    /**
     * Sıkıştırılmış dosyanın uzantısına bakarak parçalı olup olmadığını kontrol eden method.
     * @return sıkıştırılmış dosya parçalı ise true, aksi halde false.
     */
    public boolean checkMultiPart() {

        String fileName, check;
        int len, index;
        
        fileName = inFile.getName();
        len = fileName.length();
        index = fileName.lastIndexOf('.');
        
        if(index != -1 && index + 5 <= len) {
            check = fileName.substring(index + 1, index + 5);
            
            if (check.equals("part")) {           
                return true;
            } else {
                return false;
            }        
        } else {
            return false;
        }
    }

    /**
     * Sıkıştırılmış dosyanın formatını döndüren method.
     * @return dosya formatı.
     */
    public String getFileType() {
        return fileType;
    }
}
