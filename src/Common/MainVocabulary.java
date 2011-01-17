package Common;

import java.awt.TrayIcon;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;
import Gui.themes.*;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

public interface MainVocabulary {
    public static final boolean debug=true;
    public boolean playable=true;
    //              CHARS & PATHS
    public static final long emptyDirectorySize = 4096;
    public static final String errorLogPath = "Log/errorLog.log";
    public static final String historyLogPath = "Log/historyLog.log";
    public static final String serPath = "Log/ser.ser";
    public static final String tempPath = "tmp";
    public static final String newline = "\n";
    public static final String unsupportedChars  = "`~@#$%^&*\\|\"':;/><";
    public static final String numericChars = "0123456789";
    public static final String logBoldLineStartsWith = "(^[0-9]+).*";
    public static final String errorLabel = "Error";
    public static final String historyLabel = "History";
    //              ARCHIVE FORMATS
    public static final String [] formats = new String[] {"tar","tar.bz2","tar.gz","tar.lzma","zip","bz2","gz","lzma"};
    public static final String [] formatsForDirectory = new String[] {"tar","tar.bz2","tar.gz","tar.lzma","zip"};
    public static final CommonCompress[] Compressors = new CommonCompress[] {new BZip2.Compress(),new GZip.Compress(),new Lzma.Compress(),new Zip.Compress()};
    public static final CommonDecompress[] Decompressors = new CommonDecompress[] {new BZip2.Decompress(),new GZip.Decompress(),new Lzma.Decompress(),new Zip.Decompress()};
    //              PROJECT INFORMATION
    public static final String projectName = "irArchiver";
    public static final String projectVersion = "1.0.1";
    public static final String Author1 = "İbrahim Taşdemir";
    public static final String Author2 = "Ramis Taşgın";
    public static final String Adviser = "Gürhan Gündüz";
    //              MESSAGES
    public static final String eraseLogMessage = "Are you sure you want to erase the log?";
    public static final String quitMessage = "Are you sure you want to quit?";
    public static final String decompressionSucceedMessage = "Decompression is  completed.";
    public static final String compressionSucceedMessage = "Compression is completed.";
    public static final String decompressionCanceledMessage = "Decompression is canceled";
    public static final String compressionCanceledMessage = "Compression is canceled";
    //              WARNINGS
    public static final String outputFolderWarning = "Please select a destination directory!";
    public static final String nullInputFileWarning = "Please select a source file!";
    public static final String nullFileNameWarning = "Please select a archive name!";
    public static final String outputEqualsInputWarning = "Output directory cannot be same as Input directory!";
    public static final String passwordFieldsNotEqualWarning = "Passwords do not match!";
    public static final String nullPasswordFieldWarning = "Password field can not be empty!";
    public static final String fileExistWarning = "Same named file exists in this Directory. \nPlease select a different name or check overwrite box!";
    public static final String onlyNumericCharsWarning = "Part size must be a numeric value!";
    public static final String unsupportedCharWarning = "File name can not contain any of these chars:" +unsupportedChars;
    public static final String fileNotExistError = "File do not exist.";
    public static final String partedFileNotExistError = "This is not a parted file or this part is not the first part of the file.";
    public static final String JCE_EXCEPTION_MESSAGE = "Please make sure "
		+ "\"Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files\" "
		+ "(http://java.sun.com/javase/downloads/index.jsp) is installed on your JRE.";
    //              GENERAL ERRORS
    public static final String unsupportedArchiveError = "*Selected filetype is not supported ";
    public static final String decompressError = "*Decompression  is failed ";
    public static final String compressError = "*Compression  is failed ";
    public static final String preCompressError = "*Error Before Compression ";
    public static final String preDecompressError = "*Error Before Decompression ";
    public static final String EOFError = "*Unexpected End of File ";
    public static final String partedFileBrokenError = "*This parted file was broken ";
    public static final String IOError = "*Basic Input/Output Failure ";
    public static final String StreamCloseError = "*Basic Stream Close Failure ";
    public static final String populateGuiError = "*Failed to draw Graphic User Interface ! ";
    public static final String addActionListenerError = "*Failed to add Action Listeners ! ";
    public static final String constructError = "*Failed to construct class ";
    public static final String checkMultipartError = "*Error at checking Multipart ";
    public static final String findBestFormatError = "*Failed to Find Best Compression Format ! ";
    public static final String findBestFormatTempError = "*Failed to Delete Temporary Archive Files ! ";
    //              FILE OPERATIONS ERROR
    public static final String determineFileTypeBasicFailure = "*Failed to Determine Archive Type! ";
    public static final String getFileSizeError = "*Failed to get file size ";
    //              LZMA ERRORS
    public static final String LzmaShortFileError = "*Input .lzma file is too short ";
    public static final String LzmaPropertiesError = "*Incorrect stream properties ";
    public static final String LzmaStreamSizeError = "*Can't read stream size ";
    public static final String LzmaDataStreamError = "*Error in data stream ";
    //              ZIP ERRORS
    public static final String ZipEmptyFileError = "Empty directories can not be zipped. ";
    //              TITLES
    public static final String decompressionDialogTitle = "Decompression Status";
    public static final String compressionDialogTitle = "Compression Status";
    public static final String encryptionDialogTitle = "Encrypting...";
    public static final String dencryptionDialogTitle = "Dencrypting...";
    public static final String passwordDialogTitle = "Enter Password:";
    public static final String compressionResultTitle = "Compression Result";
    public static final String compressionMenuTitle = "Compression Menu";
    public static final String decompressionMenuTitle = "Decompression Menu";
    //              THEMES
    public static final MetalTheme[] themes = new MetalTheme[] {new EmeraldTheme(),
    new DefaultMetalTheme(),new OceanTheme(),new AquaTheme(),new CharcoalTheme(),
    new RubyTheme(),new ContrastTheme()};
    //              IMAGES
    public static final URL imageURL = MainVocabulary.class.getResource("images/icon.gif");
    public static final TrayIcon trayIcon=new TrayIcon(new ImageIcon(imageURL, projectName).getImage());
    //              FRAMES
    public ArrayList<JFrame> frames = new ArrayList<JFrame>();
    public static final Locale currentLocale = new Locale ("tr","TR");
    public static final String[] styleNames = {"Plain", "Bold", "Italic", "Bold Italic"};
    Properties props = new Properties();
}
