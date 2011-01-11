package Common;

public class StringOperations implements MainVocabulary
{
     public static String getFileName(String fileName, int num) 
     {
        int index = fileName.lastIndexOf('.');
        if(index != -1) 
        {
            fileName = fileName.substring(0, index);
            if(num != 1) 
            {
                --num;
                return getFileName(fileName, num);
            }
            else 
                return fileName;
        } 
        else 
            return null;
    }
    
    public static boolean checkFileName(String fileName) 
    {
        boolean check = true;
        char input [] = fileName.toCharArray();
        for(int i = 0; i< input.length; i++) 
        {
            if(unsupportedChars.indexOf(input[i]) != -1) 
            {
                check = false;
                break;
            }
        }
        return check;
    }
    
    public static boolean checkPartSize(String size) 
    {
        boolean check = true;
        char input [] = size.toCharArray();
        for(int i = 0; i< input.length; i++) 
        {
            if(numericChars.indexOf(input[i]) == -1) 
            {
                check = false;
                break;
            }
        }
        return check;
    }
}
