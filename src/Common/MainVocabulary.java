
package Common;

/**
 * Sınıflar içerisinde kullanılan sabit değişkenlerin tutulduğu interface. 
 */
public interface MainVocabulary {

    public static final long emptyDirectorySize = 4096;
    public static final String tempPath = "tmp";
    public static final String [] formats = new String[] {"lzma", "gz", "bz2", "zip", "tar.gz", "tar.lzma", "tar.bz2", "tar"};
    public static final String unsupportedChars  = "`~@#$%^&*\\|\"':;/><";
    public static final String numericChars = "0123456789";

    public static final String projectName = "JArchiver";
    public static final String projectVersion = "0.75";
    
    public static final String quitMessage = "Are you sure you want to quit?";
    public static final String decompressionSucceedMessage = "Decompression is  completed.";
    public static final String compressionSucceedMessage = "Compression is completed.";
    public static final String decompressionCanceledMessage = "Decompression is canceled";
    public static final String compressionCanceledMessage = "Compression is canceled";
    
    public static final String unsupportedArchiveError = "Selected filetype is not supported";
    public static final String decompressError = "Decompression  is failed!";
    public static final String compressError = "Compression  is failed!";
    public static final String aesPasswordError = "Message has been altered or password incorrect";
    public static final String aesCorruptedFileError = "Input file is corrupt";
    public static final String aesUnexpectedEOFError = "Unexpected end of file contents";
    public static final String populateGuiError = "Failed to draw Graphic User Interface !";
    public static final String aesUnsopportedVersionError = "Unsupported version number";
    public static final String aesExtensionError = "Unexpected end of extension";
    public static final String aesHeaderError = "Invalid File Header";
    public static final String LzmaShortFileError = "Input .lzma file is too short";
    public static final String LzmaPropertiesError = "Incorrect stream properties";
    public static final String LzmaStreamSizeError = "Can't read stream size";
    public static final String LzmaDataStreamError = "Error in data stream";
    public static final String EOFError = "Unexpected End of File";
    public static final String partedFileNotExistError = "This is not a parted file or this part is not the first part of the file.";
    public static final String partedFileBrokenError = "This parted file was broken.";
    public static final String fileNotExistError = "File do not exist.";
    public static final String ZipEmptyFileError = "Empty files can not be zipped.";
    
    public static final String outputFolderWarning = "Please select a destination directory!";
    public static final String nullInputFileWarning = "Please select a source file!";
    public static final String nullFileNameWarning = "Please select a archive name!";
    public static final String outputEqualsInputWarning = "Output directory cannot be same as Input directory!";
    public static final String passwordFieldsNotEqualWarning = "Passwords do not match!";
    public static final String nullPasswordFieldWarning = "Password field can not be empty!";
    public static final String fileExistWarning = "Same named file exists in this Directory. \nPlease select a different name or check overwrite box!";
    public static final String onlyNumericCharsWarning = "Part size must be a numeric value!";
    public static final String unsupportedCharWarning = "File name can not contain any of these chars: `~!@#$%^&*()_+=\\|\"':;?/><,";
    
    public static final String decompressionDialogTitle = "Decompression Status";
    public static final String compressionDialogTitle = "Compression Status";
    public static final String encryptionDialogTitle = "Encrypting...";
    public static final String dencryptionDialogTitle = "Dencrypting...";
    public static final String passwordDialogTitle = "Enter Password:";
    public static final String compressionResultTitle = "Compression Result";
    public static final String compressionMenuTitle = "Compression Menu";
    
}
