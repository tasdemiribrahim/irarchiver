package Gui;

import java.awt.event.ActionEvent;
import java.io.File;
import Common.FileOperations;
import Common.MainVocabulary;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Kendisine gelen dosyayı uygun bütün formatlarda sıkıştırarak, en iyi sıkıştırmayı yapan formatı 
 * bulmakta kullanılan sınıf.
 */
public class CompareAndCompress extends JFrame implements ActionListener, MainVocabulary, Runnable {

    String className = CompareAndCompress.class.getName();
    JLabel tarTitleLabel, tarBz2TitleLabel, tarLzmaTitleLabel, tarGzTitleLabel, zipTitleLabel, bz2TitleLabel,
            lzmaTitleLabel, gzTitleLabel, tarResultLabel, tarBz2ResultLabel, tarLzmaResultLabel, tarGzResultLabel,
            zipResultLabel, bz2ResultLabel, lzmaResultLabel, gzResultLabel, supportedFormatsLabel, sizesLabel,
            bestFormatTitleLabel, bestFormatTypeLabel, bestFormatSizeTitleLabel, bestFormatSizelabel,
            resultLabel;
    JButton closeButton;
    boolean overwrite;
    String outFileName;
    File inFile, outFileParent, tarBz2File, tarGzFile, tarLzmaFile, tarFile, zipFile, bz2File, gzFile, lzmaFile;
    StatusDialog compressDialog;
    BZip2.Compress tarBz2Result;
    GZip.Compress tarGzResult;
    Lzma.Compress tarLzmaResult;
    Zip.Compress zipResult;
    BZip2.Compress bz2Result;
    GZip.Compress gzResult;
    Lzma.Compress lzmaResult;
    Tar.CreateArchive tarResult;
    double tarBz2Size = 0, tarGzSize = 0, tarLzmaSize = 0, tarSize = 0, zipSize = 0, bz2Size = 0, gzSize = 0, lzmaSize = 0;
    
    /**
     * En iyi sıkıştırmayı yapan formatı bulmak üzere Gui.CompareAndCompress sınıfının nesnesinin
     * oluşturulduğu kurucu methodu.
     * @param inFile Sıkıştırılmak istenilen dosyayı tutan değişken.
     * @param outFileParent Sıkıştırılacak dosyanının içerisinde bulunacağı dizini tutan değişken
     * @param outFileName Sıkıştırılacak dosyanın adını tutan değişken.
     * @param overwrite Sıkıştıralacak dosyanın bulunacağı dizinde aynı isimli bir dosya mevcut ise
     * üzerine yazma işleminin yapılıp yapılmayacağını belirten değişken.
     */
    public CompareAndCompress(File inFile, File outFileParent, String outFileName, boolean overwrite) {
        this.inFile = inFile;
        this.outFileParent = outFileParent;
        this.outFileName = outFileName;
        this.overwrite = overwrite;

        compressDialog = new StatusDialog();
        compressDialog.setStateToCompress();
        compressDialog.setIndeterminate(true);
        compressDialog.setEditable(false);

        initComponents();
        if (populateMainGui()) {
            initiateActions();
        }
    }

    /**
     * Arayüz bileşenlerinin tanımlandığı method.
     */
    private void initComponents() {

        tarTitleLabel = new JLabel("tar:");
        tarBz2TitleLabel = new JLabel("tar.bz2:");
        tarGzTitleLabel = new JLabel("tar.gz:");
        tarLzmaTitleLabel = new JLabel("tar.lzma:");
        zipTitleLabel = new JLabel("zip:");
        bz2TitleLabel = new JLabel("bz2:");
        gzTitleLabel = new JLabel("gz:");
        lzmaTitleLabel = new JLabel("lzma:");
        tarResultLabel = new JLabel();
        tarBz2ResultLabel = new JLabel();
        tarGzResultLabel = new JLabel();
        tarLzmaResultLabel = new JLabel();
        zipResultLabel = new JLabel();
        bz2ResultLabel = new JLabel();
        gzResultLabel = new JLabel();
        lzmaResultLabel = new JLabel();
        supportedFormatsLabel = new JLabel("<html><u>Formats</u></html>");
        sizesLabel = new JLabel("<html><u>Sizes</u></html>");
        bestFormatTitleLabel = new JLabel("Selected Format:");
        bestFormatTypeLabel = new JLabel();
        bestFormatSizeTitleLabel = new JLabel("Compressed Size:");
        bestFormatSizelabel = new JLabel();
        resultLabel = new JLabel("<html><u>Result</u></html>");
        closeButton = new JButton("Close");
    }

     /**
     * Arayüz ve arayüz bileşenlerinin özelliklerinin belirlendeği method. Arayüz bileşenlerinin
     * arayüz içerisindeki konumlarını belirlemek için GridBagLayout() nesnesi kullanılmıştır.
     * @return işlemler sırasında herhangi bir hata olursa false, olmazsa true
     */
    private boolean populateMainGui() {
        try {

            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            add(supportedFormatsLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            add(sizesLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            add(tarTitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            add(tarResultLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            add(tarBz2TitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 2;
            add(tarBz2ResultLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 3;
            add(tarGzTitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 3;
            add(tarGzResultLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 4;
            add(tarLzmaTitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 4;
            add(tarLzmaResultLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 5;
            add(zipTitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 5;
            add(zipResultLabel, constraints);
            
            if (!inFile.isDirectory()) {
                constraints.gridx = 0;
                constraints.gridy = 6;
                add(bz2TitleLabel, constraints);
                constraints.gridx = 1;
                constraints.gridy = 6;
                add(bz2ResultLabel, constraints);
                constraints.gridx = 0;
                constraints.gridy = 7;
                add(gzTitleLabel, constraints);
                constraints.gridx = 1;
                constraints.gridy = 7;
                add(gzResultLabel, constraints);
                constraints.gridx = 0;
                constraints.gridy = 8;
                add(lzmaTitleLabel, constraints);
                constraints.gridx = 1;
                constraints.gridy = 8;
                add(lzmaResultLabel, constraints);
            }

            constraints.gridx = 0;
            constraints.gridy = 9;
            add(resultLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 10;
            add(bestFormatTitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 10;
            add(bestFormatTypeLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 11;
            add(bestFormatSizeTitleLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 11;
            add(bestFormatSizelabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 12;
            constraints.gridwidth = 3;
            add(closeButton, constraints);

            closeButton.setVisible(true);
            closeButton.setEnabled(true);
            
            setSize(300, 400);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension form = getSize();
            setLocation((screen.width - form.width) / 2, (screen.height - form.height) / 2);
            setTitle(compressionResultTitle);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, populateGuiError + "From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }    
    
     
    /**
     * Arayüz bileşenlerine uygun interface atamalarının yapıldığı method. 
     */
    private void initiateActions() {
        closeButton.addActionListener(this);
    }

    /**
     * ActionListener interface inin bir methodudur. closeButton buttonu tarafından ActionEvent oluşturulması 
     * durumunda classa ait dispose() methodu çağrılarak CompareAndCompress sınıfına ait arayüz kapatılır.
     * @param e Meydana gelen ActionEventinin hangi arayüz bileşeni tarafından oluşturulduğunu 
     * belirlemekte kullanılan değişken. 
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(closeButton)) {
            this.dispose();
        }
    }

    /**
     * inFile değişkeniyle belirtilen dosyanın sıkıştırma işlemine başlanan method. İlk olarak setFiles 
     * methoduyla bütün formatlar için birer dosya oluşturulmaktadır. Sonra da inFile dosyası bu formatlar 
     * için teker teker sıkıştırılarak oluşturulan dosyalara kaydedilir. Eğer sıkıştırma işlemi 
     * bitmeden StatusDialog arayüzündeki cancelButton buttonu kullanılırsa işlem iptal edilerek method
     * sonlandırılır.
     */
    private void beginCompress() {

        setFiles();
        tarBz2Result = new BZip2.Compress(inFile, tarBz2File, true, compressDialog);
        if (!compressDialog.isCanceled()) {
            tarGzResult = new GZip.Compress(inFile, tarGzFile, true, compressDialog);
        }
        if (!compressDialog.isCanceled()) {
            tarLzmaResult = new Lzma.Compress(inFile, tarLzmaFile, true, compressDialog);
        }
        if (!compressDialog.isCanceled()) {
            tarResult = new Tar.CreateArchive(inFile, tarFile, compressDialog);
        }
        if (!compressDialog.isCanceled()) {
            zipResult = new Zip.Compress(inFile, zipFile, compressDialog);
        }

        if (!inFile.isDirectory()) {
            if (!compressDialog.isCanceled()) {
                bz2Result = new BZip2.Compress(inFile, bz2File, false, compressDialog);
            }
            if (!compressDialog.isCanceled()) {
                gzResult = new GZip.Compress(inFile, gzFile, false, compressDialog);
            }
            if (!compressDialog.isCanceled()) {
                lzmaResult = new Lzma.Compress(inFile, lzmaFile, false, compressDialog);
            }
        }
    }

    /**
     * Sıkıştırma yapılan bütün formatlar içerisinden en küçük boyutlu sıkıştırma yapan formatın bulunarak,
     * sıkıştırılmış dosyanın hedef dizinine yazıldığı method. İlk olarak inFile değişkeniyle belirtilen 
     * dosya beginCompress methodu aracılığıyla bütün formatlarda sıkıştırılır. Eğer bu sıkıştırma işlemi 
     * sırasında işlem iptal edilmemişse, getBestFormat methodu ile en küçük boyutlu sıkıştırma yapan format 
     * bulunarak outFile değişkeninin göstermiş olduğu dizine yazılır. Eğer overwrite değişkeninin değeri 
     * false ve outFile diye bir dosya mevcut ise dosya yazılmaz ve işlem sonlandırılır. Son olarak da 
     * sıkıştırma yapılan formatları karşılaştırma yaparken kullanılan tüm geçici dosyalar silinir.
     */
    private void findBestFormat() {
        try {
            beginCompress();
            
            if (!compressDialog.isCanceled()) {
                setCompressSizes();
                File bestFormat = getBestFormat();
                File outFile = new File(outFileParent.getAbsolutePath() + File.separator + bestFormat.getName());
                
                if (outFile.exists() && overwrite == false) {                    
                    compressDialog.setVisible(false);
                    JOptionPane.showMessageDialog(null, fileExistWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
                } else {
                    FileOperations.copyFile(bestFormat, outFile);
                    compressDialog.setVisible(false);
                    setVisible(true);              
                } 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (tarBz2File != null) {
                    tarBz2File.delete();
                }
                if (tarGzFile != null) {
                    tarGzFile.delete();
                }
                if (tarLzmaFile != null) {
                    tarLzmaFile.delete();
                }
                if (tarFile != null) {
                    tarFile.delete();
                }
                if (zipFile != null) {
                    zipFile.delete();
                }
                if (bz2File != null) {
                    bz2File.delete();
                }
                if (lzmaFile != null) {
                    lzmaFile.delete();
                }
                if (gzFile != null) {
                    gzFile.delete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Exception Throwed From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Dosyanın sıkıştırılmasında kullanılan bütün formatların kıyaslanarak, en küçük boyutlu sıkıştırma 
     * yapan formatın bulunduğu method. 
     * @return En iyi sıkıştırmayı yapan formatın tutulduğu dosya.
     */
    private File getBestFormat() {
        double formatSizes[],  minFormatSize;
        File compressedFiles[],bestSizedFile  = null;
        String formatName[],bestFormatName  = null;

        if (!inFile.isDirectory()) {

            formatName = new String[]{"tar", "tar.bz2", "tar.gz", "tar.lzma", "zip", "bz2", "gz", "lzma"};
            compressedFiles = new File[]{tarFile, tarBz2File, tarGzFile, tarLzmaFile, zipFile, bz2File, gzFile, lzmaFile};
            formatSizes = new double[]{tarSize, tarBz2Size, tarGzSize, tarLzmaSize, zipSize, bz2Size, gzSize, lzmaSize};
        } else {

            formatName = new String[]{"tar", "tar.bz2", "tar.gz", "tar.lzma", "zip"};
            compressedFiles = new File[]{tarFile, tarBz2File, tarGzFile, tarLzmaFile};
            formatSizes = new double[]{tarSize, tarBz2Size, tarGzSize, tarLzmaSize, zipSize};
        }

        bestSizedFile = tarFile;
        minFormatSize = tarSize;

        for (int i = 0; i < formatSizes.length; i++) {
            if (formatSizes[i] < minFormatSize) {
                bestSizedFile = compressedFiles[i];
                minFormatSize = formatSizes[i];
                bestFormatName = formatName[i];
            }
        }

        bestFormatTypeLabel.setText(bestFormatName);
        bestFormatSizelabel.setText(String.valueOf(minFormatSize));

        return bestSizedFile;
    }

    /**
     * Sıkıştırma yapılan formatlara ait dosya boyutlarının uygun değişkenlere atandığı method. Ayrıca 
     * bu değerler formatlara ait JLabel bileşenlerine de yazılır.
     */
    private void setCompressSizes() {

        tarBz2Size =FileOperations.getFileSizeInMegabytes(tarBz2File);
        tarGzSize = FileOperations.getFileSizeInMegabytes(tarGzFile);
        tarLzmaSize = FileOperations.getFileSizeInMegabytes(tarLzmaFile);
        tarSize = FileOperations.getFileSizeInMegabytes(tarFile);
        zipSize = FileOperations.getFileSizeInMegabytes(zipFile);

        tarBz2ResultLabel.setText(String.valueOf(tarBz2Size));
        tarGzResultLabel.setText(String.valueOf(tarGzSize));
        tarLzmaResultLabel.setText(String.valueOf(tarLzmaSize));
        tarResultLabel.setText(String.valueOf(tarSize));
        zipResultLabel.setText(String.valueOf(zipSize));

        if (!inFile.isDirectory()) {
            bz2Size = FileOperations.getFileSizeInMegabytes(bz2File);
            gzSize = FileOperations.getFileSizeInMegabytes(gzFile);
            lzmaSize = FileOperations.getFileSizeInMegabytes(lzmaFile);

            gzResultLabel.setText(String.valueOf(gzSize));
            bz2ResultLabel.setText(String.valueOf(bz2Size));
            lzmaResultLabel.setText(String.valueOf(lzmaSize));
        }
    }

    /**
     * Sıkıştırma yapılacak formatların herbiri için farklı dosyaların oluşturulduğu method.
     */
    private void setFiles() {
        tarBz2File = new File(tempPath + File.separator + outFileName + ".tar.bz2");
        tarGzFile = new File(tempPath + File.separator + outFileName + ".tar.gz");
        tarLzmaFile = new File(tempPath + File.separator + outFileName + ".tar.lzma");
        tarFile = new File(tempPath + File.separator + outFileName + ".tar");
        zipFile = new File(tempPath + File.separator + outFileName + ".zip");
        
        if(!inFile.isDirectory()) {
            bz2File = new File(tempPath + File.separator + outFileName + ".bz2");
            lzmaFile = new File(tempPath + File.separator + outFileName + ".lzma");
            gzFile = new File(tempPath + File.separator + outFileName + ".gz");
        }
    }
    
    /**
     * Threadi başlatmakta kullanılan method.
     */
    public void run() {
        findBestFormat();
    }
}
