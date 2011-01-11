package Common;

import Gui.StatusDialog;
import java.io.File;

public interface CommonCompress {
    void Compress(File inFile, File outFile, boolean tar, StatusDialog dialog) throws Exception;
    void compressFile() throws Exception;
    void tarAndCompressFile() throws Exception;
}
