
package Common;

/**
 * İçerisinde kullanılmakta olan bazı string işlemlerinin tanımlı olduğu sınıf.
 */
public class StringOperations implements MainVocabulary{

    /**
     * Dosya ismini, ismin içerisinde içerisinde bulunan '.' karakterlerinden itibaren keserek elde ettiği 
     * stringi döndüren method.
     * @param fileName Dosyanın ismini gösteren değişken.
     * @param lastFound Sondan kaçıncı noktadan sonraki karakterlerin atılacağını gösteren değişken.
     * @return Sondan num uncu noktadan sonraki karakterleri atılmış dosyanın ismi.
     */
     public static String getFileName(String fileName, int num) {
        
        int index = fileName.lastIndexOf('.');
        
        if(index != -1) {
            fileName = fileName.substring(0, index);
            
            if(num != 1) {
                --num;
                return getFileName(fileName, num);
            } else {
                return fileName;
            }
        } else {
            return null;
        }
    }
    
     /**
      * Dosya isminin içerisinde geçersiz karakter olup olmadığını kontrol eden method.
      * @param fileName Kontrol edilecek dosya ismini tutan değişken.
      * @return Dosya ismi geçersiz karakter içeriyorsa false, aksi halde true.
      */
    public static boolean checkFileName(String fileName) {
        
        boolean check = true;
        char input [] = fileName.toCharArray();
        
        for(int i = 0; i< input.length; i++) {
                     
            if(unsupportedChars.indexOf(input[i]) != -1) {
                check = false;
                break;
            }
        }
        
        return check;
    }
    
    /**
     * Parçalanacak dosya boyutunu gösteren değişkenin değerinin nümerik olup olmadığının kontrol 
     * edildiği method.
     * @param size Kontrol edilecek dosya boyutu değerini gösteren değişken.
     * @return Dosya boyutu nümerik ise true , aksi halde false.
     */
    public static boolean checkPartSize(String size) {
        
        boolean check = true;
        char input [] = size.toCharArray();
        
        for(int i = 0; i< input.length; i++) {
                     
            if(numericChars.indexOf(input[i]) == -1) {
                check = false;
                break;
                
            }
        }
        return check;
    }
}
