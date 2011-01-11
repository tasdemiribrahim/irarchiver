package Gui;

import Common.DecompressHandler;
import Common.MainVocabulary;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Sıkıştırılmış dosyaları açmakta kullanılan menüyü oluşturan sınıf.
 */
public class PreDecompress extends JFrame implements ActionListener, MainVocabulary {

    String className = PreDecompress.class.getName();
    JLabel inFileLabel, outFileLabel;
    JCheckBox overwriteCheckBox;
    JTextField inFileTextBox, outFileTextBox;
    JButton outFileChooserButton, actionButton, cancelButton;
    JFileChooser outFileChooser;
    File inFile, outFileParent;
    DecompressHandler handleDecompress;
    Thread dencryptTehread;

    /**
     * Sıkıştırılmış dosyaları açmakta kullanılan arayüzü oluşturmak için Gui.PreDecompress sınıfının 
     * nesnesinin oluşturulduğu kurucu methodu.
     * @param inFile açılacak olan sıkıştırılmış dosyayı gösteren değişken. 
     */
    public PreDecompress(File inFile) {

        this.inFile = inFile;
        
        initComponents();
        if (populateGui()) 
            initiateActions();     
    }

    /**
     * Arayüz bileşenlerinin tanımlandığı method.
     */
    public void initComponents() {

        inFileLabel = new JLabel("Source:");
        outFileLabel = new JLabel("Destination:");
        overwriteCheckBox = new JCheckBox("Overwrite If File Exists", false);
        inFileTextBox = new JTextField(25);
        outFileTextBox = new JTextField(25);
        outFileChooserButton = new JButton("...");
        actionButton = new JButton("Ok");
        cancelButton = new JButton("Cancel");
        outFileChooser = new JFileChooser();
    }

    
    /**
     * Arayüz ve arayüz bileşenlerinin özelliklerinin belirlendeği method. Arayüz bileşenlerinin
     * arayüz içerisindeki konumlarını belirlemek için GridBagLayout() nesnesi kullanılmıştır.
     * @return işlemler sırasında herhangi bir hata olursa false, olmazsa true.
     */
    public boolean populateGui() {

        try {

            setLayout(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            add(inFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            add(inFileTextBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            add(outFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.gridwidth = 2;
            add(outFileTextBox, constraints);
            constraints.gridx = 3;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            add(outFileChooserButton, constraints);
            constraints.gridx = 1;
            constraints.gridy = 2;
            constraints.gridwidth = 1;
            add(overwriteCheckBox, constraints);
            constraints.gridx = 1;
            constraints.gridy = 3;
            add(actionButton, constraints);
            constraints.gridx = 2;
            constraints.gridy = 3;
            add(cancelButton, constraints);

            outFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            inFileTextBox.setText(inFile.toString());
            inFileTextBox.setEnabled(false);
            outFileTextBox.setEnabled(false);

            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension form = getSize();
            setLocation((screen.width - form.width) / 2, (screen.height - form.height) / 2);
            setResizable(false);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        } catch (Exception ex) {
            
            JOptionPane.showMessageDialog(this, populateGuiError +" From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Arayüz bileşenlerine uygun interface atamalarının yapıldığı method. 
     */
    public void initiateActions() {

        actionButton.addActionListener(this);
        cancelButton.addActionListener(this);
        outFileChooserButton.addActionListener(this);
    }

    /**
     * ActionListener interface inin bir methodudur. Herhangi bir ActionEvent meydana 
     * gelmesi durumunda ActionEventi gönderen bileşene ait fonksiyonun çağırıldığı method.
     * @param e Meydana gelen ActionEventinin hangi arayüz bileşeni tarafından oluşturulduğunu 
     * belirlemekte kullanılan değişken. 
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(cancelButton)) {
            this.dispose();
        } else if (e.getSource().equals(outFileChooserButton)) {
            outFileChooserButtonActionPerformed();
        } else if (e.getSource().equals(actionButton)) {
            actionButtonActionPerformed();
        }
    }

    /**
     * outFileChooserButton buttonuna basılması durumunda çağırılan method. 
     * Sıkıştırılmış dosyanın çıkartılacağı dizin belirlenir.
     */
    public void outFileChooserButtonActionPerformed() {
        
        int value = outFileChooser.showSaveDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) {
            outFileParent = outFileChooser.getSelectedFile();
            outFileTextBox.setText(outFileParent.toString());
        }

    }

    /**
     * actionButton buttonuna basılması durumunda çağırılan method. outFileParent isimli 
     * sıkıştırılmış dosyanın açılacağı dizini tutan değişkenin değeri null değilse 
     * handleDecompress sınıfına ait bir nesne yaratılarak dosya açma işlemi gerçekleştirilir.
     */
    public void actionButtonActionPerformed() {

        if (outFileParent != null) {
            handleDecompress = new DecompressHandler(inFile, outFileParent, overwriteCheckBox.isSelected());  
            Thread decompressThread = new Thread(handleDecompress);
            decompressThread.start();
            
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, outputFolderWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }
}
