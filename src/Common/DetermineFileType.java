package Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DetermineFileType implements MainVocabulary 
{
    private String className = DetermineFileType.class.getName();
    private File inFile;
    
    public DetermineFileType(File inFile) throws Exception 
    {
        try
        {
            if(debug)
                System.out.println("Started determine file type for " + inFile + " at line 18.");
            this.inFile = inFile;
        }
        catch (Exception ex) 
        { 
            throw new Exception(constructError + className + newline + ex.getMessage());
        } 
    }

    public String simpleCheck() throws FileNotFoundException, IOException, Exception 
    {
        try
        {
            String fileName = inFile.getName();
            if(debug)
                System.out.println("Simple check for " + fileName + " at line 33.");
            if(inFile.isDirectory()) 
                return null;
            else 
            {   for(int i=0;i<formats.length;i++)
                    if(fileName.endsWith(formats[i]))
                        return formats[i];
                    return advanceCheck();
            }
        }
        catch (Exception ex) 
        { 
            throw new Exception(determineFileTypeBasicFailure + className + newline + ex.getMessage());
        } 
    }

    private String advanceCheck() throws FileNotFoundException, IOException 
    {
        if(debug)
            System.out.println("Advance check for at line 52.");
        byte[] firstTwo = new byte[inFile.getName().length()];
        final byte[] gzipConst = {(byte) 31, (byte) 139};
        final byte[] lzmaConst = {(byte) 93, (byte) 0};
        FileInputStream fInStream = null;
        try 
        {
            if(!inFile.isDirectory()) 
            {
                fInStream = new FileInputStream(inFile);
                fInStream.read(firstTwo);
                char[] charHead = {(char) firstTwo[0], (char) firstTwo[1]};
                String arcHead = new String(charHead);
                if(debug)
                    System.out.println("Archive header " + arcHead + " at line 66.");
                if (arcHead.equals("PK")) 
                    return formats[4];
                else if (arcHead.equals("BZ")) 
                    return formats[5];
                else if (firstTwo[0] == gzipConst[0] && firstTwo[1] == gzipConst[1]) 
                    return formats[6];
                else if (firstTwo[0] == lzmaConst[0] && firstTwo[1] == lzmaConst[1]) 
                    return formats[7];
                else if(inFile.getName().equals(new String(firstTwo)))
                    return formats[0];
            } 
            else 
                return null;
        }
        catch (FileNotFoundException ex) 
        {
            throw new FileNotFoundException(fileNotExistError + "at" + className + newline + ex.getMessage());
        } 
        catch (IOException ex) 
        {
            throw new IOException(IOError + "at" + className + newline + ex.getMessage());
        }  
        finally
        {
            if (fInStream != null) 
                fInStream.close();
        }
        return null;
    }

    public boolean checkAES() throws FileNotFoundException, IOException 
    {
        if(debug)
            System.out.println("Check AES at line 100.");
        FileInputStream fInStream = null;
        byte[] firstThree = new byte[3];
        try 
        {
            if(!inFile.isDirectory()) 
            {
                fInStream = new FileInputStream(inFile);
                fInStream.read(firstThree);

                char[] charHeadAES = {(char) firstThree[0], (char) firstThree[1], (char) firstThree[2]};
                String arcHeadAES = new String(charHeadAES);
                if(debug)
                    System.out.println("Archive header " + arcHeadAES + " at line 113.");
                if (arcHeadAES.equals("AES")) 
                    return true;
                else 
                    return false;
            } else 
                return false;
        }
        catch (FileNotFoundException ex) 
        {
            throw new FileNotFoundException(fileNotExistError + "at" + className + newline + ex.getMessage());
        } 
        catch (IOException ex) 
        {
            throw new IOException(IOError + "at" + className + newline + ex.getMessage());
        } 
        finally 
        {
            if (fInStream != null) 
                fInStream.close();
        }
    }

    public boolean checkMultiPart() throws Exception 
    {
        if(debug)
            System.out.println("Check multipart at line 139.");
        try
        {
            String fileName, check;
            int len, index;

            fileName = inFile.getName();
            len = fileName.length();
            index = fileName.lastIndexOf('.');

            if(index != -1 && index + 5 <= len) 
            {
                check = fileName.substring(index + 1, index + 5);
                if (check.equals("part"))
                    return true;
                else 
                    return false;
            } else 
                return false;
        }
        catch (Exception ex) 
        { 
            throw new Exception(checkMultipartError + "at" + className + newline + ex.getMessage());
        }
    }
}
