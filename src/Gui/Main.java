package Gui;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import Common.MainVocabulary;
import Common.CompressHandler;
import java.io.File;
/**
 * Programı başlatarak ana menüyü oluşturan sınıf.
 */
public class Main extends JFrame implements ActionListener, MainVocabulary, MouseMotionListener{

    String className = Main.class.getName(); 
    JFileChooser mainChooser, popupChooser;
    JMenuBar mainMenu;
    JMenu fileMenu, aboutMenu;
    JMenuItem fileQuitItem, aboutInfoItem;
    JButton compressButton, decompressButton;
    PreDecompress decompressFile;
    PreCompress compressFile;
    File selectedFile;
    CompressHandler handleCompress;

    /**
     * Ana menüyü oluşturarak programın başlatılmasını sağlayan Gui.Main sınıfının nesnesinin 
     * oluşturulduğu kurucu methodu.
     */
    public Main() {
        initComponents();
        if (populateGui()) {
            initiateActions();
        }
    }

    /**
     * Arayüz bileşenlerinin tanımlandığı method.
     */
    private void initComponents() {
        mainChooser = new JFileChooser();
        mainMenu = new JMenuBar();
        fileMenu = new JMenu("File");
        aboutMenu = new JMenu("About");
        fileQuitItem = new JMenuItem("Quit");
        aboutInfoItem = new JMenuItem("Info");
        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");
    }

    /**
     * Arayüz ve arayüz bileşenlerinin özelliklerinin belirlendeği method. Arayüz bileşenlerinin
     * arayüz içerisindeki konumlarını belirlemek için GridBagLayout() nesnesi kullanılmıştır.
     * @return İşlemler sırasında herhangi bir hata olursa false, olmazsa true.
     */
    public boolean populateGui() {
        try {
            mainMenu.add(fileMenu);
            mainMenu.add(aboutMenu);
            fileMenu.add(fileQuitItem);
            aboutMenu.add(aboutInfoItem);
            setJMenuBar(mainMenu);

            mainChooser.getComponent(3).setEnabled(false);
            mainChooser.getComponent(3).setVisible(false);
            mainChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridheight = 5;
            constraints.gridwidth = 5;
            add(mainChooser, constraints);
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            add(compressButton, constraints);
            constraints.gridx = 1;
            constraints.gridy = 5;
            add(decompressButton, constraints);
            
            File tempFile = new File("tmp");
            if(!tempFile.exists())
                tempFile.mkdir();
               
            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension form = getSize();
            setLocation((screen.width - form.width) / 2, (screen.height - form.height) / 2);
            setTitle("JArchiver ");
            setResizable(false);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, populateGuiError + "From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Arayüz bileşenlerine uygun interface atamalarının yapıldığı method.
     */
    public void initiateActions() {
        compressButton.addActionListener(this);
        decompressButton.addActionListener(this);
        fileQuitItem.addActionListener(this);
        aboutInfoItem.addActionListener(this);
        mainChooser.addMouseMotionListener(this);
    }

    /**
     * ActionListener interface inin bir methodudur. Herhangi bir ActionEvent meydana 
     * gelmesi durumunda ActionEventi gönderen bileşene ait fonksiyonun çağırıldığı method.
     * @param e Meydana gelen ActionEventinin hangi arayüz bileşeni tarafından oluşturulduğunu 
     * belirlemekte kullanılan değişken. 
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(decompressButton)) {
            decompressButtonActionPerformed();
        } else if (e.getSource().equals(compressButton)) {
            compressButtonActionPerformed();
        } else if (e.getSource().equals(fileQuitItem)) {
            fileQuitItemActionPerformed();
        } else if (e.getSource().equals(aboutInfoItem)) {
            aboutInfoItemActionPerformed();
        }
        if(e.getSource().equals(WindowConstants.EXIT_ON_CLOSE))
            System.out.println("aaa");
    }

    /**
     * decompressButton buttonuna basılması sonucunda yapılacak olan işlemleri belirleyen method. İlk olarak 
     * File türünden bir değişken olan selectedFile değişkeninin null a eşit olup olmadığı kontrol edilir. 
     * Eğer null değil ise  selectedFile değişkeninde tutulan sıkıştırılmış dosyayı açmak üzere decompressFile 
     * isimli PreDecompress sınıfından üretilen nesneye başvurulur.
     */
    public void decompressButtonActionPerformed() {
        selectedFile = mainChooser.getSelectedFile();

        if (selectedFile != null) {
            decompressFile = new PreDecompress(selectedFile);
        } else {
            JOptionPane.showMessageDialog(this, nullInputFileWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * compressButton basılması durumunda yapılacak olan işlemleri belirleyen method. İlk olarak File 
     * türünden bir değişken olan selectedFile değişkeninin null a eşit olup olmadığı kontrol edilir. 
     * Eğer null değil ise  selectedFile değişkeninde tutulan seçilmiş dosyayı sıkıştırmak üzere 
     * compressFile isimli PreCompress sınıfından üretilen nesneye başvurulur.
     */
    private void compressButtonActionPerformed() {
        selectedFile = mainChooser.getSelectedFile();

        if (selectedFile != null) {
            mainChooser.rescanCurrentDirectory();
            compressFile = new PreCompress(selectedFile);
        } else {
            JOptionPane.showMessageDialog(this, nullInputFileWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * fileQuitItem menu iteminin kullanılması durumunda, showConfirmDialog dan dönen değere göre program
     * sonlandırılır veya sonlandırılmaz.
     */
    public void fileQuitItemActionPerformed() {
        int confirm;
        confirm = JOptionPane.showConfirmDialog(this, quitMessage, "Warning!", JOptionPane.YES_NO_OPTION);

        if (confirm  == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * aboutInfoItem menu iteminin  kullanılması durumunda, proje hakkındaki genel bilgileri görüntülemekte 
     * kullanılan About arayüzü oluşturulur.
     */
    public void aboutInfoItemActionPerformed() {
        About aboutGui = new About();
    }

    /**
     * MouseMotionListener interface inin bir methodudur. Fare imlecinin MouseMotionListener
     * interface ine sahip bir bileşen üzerinde hareket ettirilmesi durumunda, JFileChooser türünden 
     * bir değişken olan mainChooser değişkeninin o anda göstermekte olduğu dizin içeriğini yenilenir.
     */
    public void mouseMoved(MouseEvent e) {
        mainChooser.rescanCurrentDirectory();
    }

    /**
     * MouseMotionListener interface ine ait mouseDragged methodu. 
     */
    public void mouseDragged(MouseEvent e) {
        JOptionPane.showMessageDialog(this, "Not Supported", "Warning!", JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Programın çalıştırıldığı main methodu.
     */
    public static void main(String args[]) {
        new Main();
    }
}
