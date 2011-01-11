package Common;

import Gui.StatusDialog;
import java.io.File;

public interface CommonDecompress {
    void Decompress(File inFile, File outFileParent, boolean overwrite, StatusDialog dialog) throws Exception; 
    void Decompress(File inFile, File outFileParent, String fileName, boolean overwrite, StatusDialog dialog) throws Exception;
    void decompressFile() throws Exception;
    void decompressAndUntarFile() throws Exception;    
}
