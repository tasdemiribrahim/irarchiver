package Gui;

import Common.CompressHandler;
import Common.MainVocabulary;
import Common.StringOperations;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

/**
 * Dosya şıkıştırma menüsünü oluşturan sınıf.
 */
public class PreCompress extends JFrame implements ActionListener, ChangeListener, KeyListener, MainVocabulary {

    String className = PreCompress.class.getName();
    JLabel inFileLabel, outFileLabel, selectFormatLabel, fileNameLabel, passwordLabel, checkPasswordLabel, multiPartLabel;
    JCheckBox overwriteCheckBox, showPasswordCheckBox, setPasswordCheckBox, setMultiPartCheckBox;
    JTextField inFileTextBox, passwordTextBox, outFileDirectoryTextBox, fileNameTextBox, partSizeTextBox;
    JButton outFileChooserButton, actionButton, cancelButton;
    JComboBox selectFormatComboBox;
    JPanel mainPanel, selectFormatPanel, generalPanel, advancedPanel, passwordPanel, multiPartPanel;
    JRadioButton userControlledCompressionRadioButton, performanceBasedCompressionRadioButton;
    JTabbedPane compressionTabbedPane;
    JPasswordField passwordField, checkPasswordField;
    JFileChooser outFileChooser;
    File inFile;
    CompressHandler handleCompress;
    CompareAndCompress performanceBasedCompress;
    String password = "";
    int partSize = 0, maxFileNameTextBoxLength = 15;

    /**
     * Dosya sıkıştırma seçeneklerinin yer aldığı dosya sıkıştırma menüsünü oluşturmak için 
     * Gui.Precompress sınıfının nesnesinin oluşturulmasında kullanılan kurucu methodu.
     * @param inFile sıkıştırılacak dosyayı tutan değişken.
     */
    public PreCompress(File inFile) {

        this.inFile = inFile;
        initComponents();
        if (populateGui()) {
            initiateActions();
        }
    }

    /**
     * Arayüz bileşenlerinin tanımlandığı method.
     */
    public void initComponents() {
        inFileLabel = new JLabel("Source:");
        outFileLabel = new JLabel("Destination:");
        selectFormatLabel = new JLabel("Compression Format:");
        fileNameLabel = new JLabel("Archive Name");
        selectFormatComboBox = new JComboBox();
        overwriteCheckBox = new JCheckBox("Overwrite If File Exists", false);
        inFileTextBox = new JTextField(25);
        outFileDirectoryTextBox = new JTextField(25);
        fileNameTextBox = new JTextField(25);
        outFileChooserButton = new JButton("...");
        actionButton = new JButton("Ok");
        cancelButton = new JButton("Cancel");
        outFileChooser = new JFileChooser();
        mainPanel = new JPanel(new GridBagLayout());
        selectFormatPanel = new JPanel(new GridBagLayout());
        userControlledCompressionRadioButton = new JRadioButton("User Selected Compression");
        performanceBasedCompressionRadioButton = new JRadioButton("Select Best Compression");
        compressionTabbedPane = new JTabbedPane();
        generalPanel = new JPanel(new GridBagLayout());
        advancedPanel = new JPanel(new GridBagLayout());
        passwordTextBox = new JTextField(20);
        passwordField = new JPasswordField(20);
        checkPasswordField = new JPasswordField(20);
        passwordPanel = new JPanel(new GridBagLayout());
        passwordLabel = new JLabel("Enter Password:");
        checkPasswordLabel = new JLabel("Repeat Password:");
        showPasswordCheckBox = new JCheckBox("Show Password");
        multiPartPanel = new JPanel(new GridBagLayout());
        partSizeTextBox = new JTextField(5);
        multiPartLabel = new JLabel("Part Size (in MBs):");
        setPasswordCheckBox = new JCheckBox("Set Password");
        setMultiPartCheckBox = new JCheckBox("Set Multi Part");
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

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            add(compressionTabbedPane, constraints);
            constraints.gridx = 0;
            constraints.gridy = 0;
            generalPanel.add(inFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            generalPanel.add(inFileTextBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            generalPanel.add(outFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.gridwidth = 2;
            generalPanel.add(outFileDirectoryTextBox, constraints);
            constraints.gridx = 3;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            generalPanel.add(outFileChooserButton, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            generalPanel.add(fileNameLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 2;
            constraints.gridwidth = 2;
            generalPanel.add(fileNameTextBox, constraints);
            constraints.gridx = 1;
            constraints.gridy = 3;
            constraints.gridwidth = 1;
            generalPanel.add(overwriteCheckBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.gridwidth = 4;
            generalPanel.add(mainPanel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 5;
            constraints.gridwidth = 1;
            generalPanel.add(actionButton, constraints);
            constraints.gridx = 2;
            constraints.gridy = 5;
            generalPanel.add(cancelButton, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            mainPanel.add(userControlledCompressionRadioButton, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            mainPanel.add(selectFormatPanel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            mainPanel.add(performanceBasedCompressionRadioButton, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            selectFormatPanel.add(selectFormatLabel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 2;
            selectFormatPanel.add(selectFormatComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 1;
            advancedPanel.add(setPasswordCheckBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            advancedPanel.add(passwordPanel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            advancedPanel.add(setMultiPartCheckBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 3;
            advancedPanel.add(multiPartPanel, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            passwordPanel.add(passwordLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            passwordPanel.add(passwordTextBox, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            passwordPanel.add(passwordField, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            passwordPanel.add(checkPasswordLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 1;
            passwordPanel.add(checkPasswordField, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            passwordPanel.add(showPasswordCheckBox, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            multiPartPanel.add(multiPartLabel, constraints);
            constraints.gridx = 1;
            constraints.gridy = 0;
            multiPartPanel.add(partSizeTextBox, constraints);

            compressionTabbedPane.addTab("General", generalPanel);
            compressionTabbedPane.addTab("Advanced", advancedPanel);

            if (inFile.isFile()) {
                fillComboBoxForFiles();
            } else if (inFile.isDirectory()) {
                fillComboBoxForDirectories();
            }

            userControlledCompressionRadioButton.setSelected(true);
            outFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileNameTextBox.setText(inFile.getName());
            inFileTextBox.setText(inFile.toString());
            inFileTextBox.setEnabled(false);
            outFileDirectoryTextBox.setEnabled(false);
            passwordTextBox.setEnabled(false);
            passwordTextBox.setVisible(false);
            passwordField.setEnabled(false);
            checkPasswordField.setEnabled(false);
            showPasswordCheckBox.setEnabled(false);
            partSizeTextBox.setEnabled(false);
            passwordLabel.setEnabled(false);
            checkPasswordLabel.setEnabled(false);
            multiPartLabel.setEnabled(false);

            mainPanel.setBorder(BorderFactory.createEtchedBorder());
            selectFormatPanel.setBorder(BorderFactory.createEtchedBorder());
            passwordPanel.setBorder(BorderFactory.createEtchedBorder());
            multiPartPanel.setBorder(BorderFactory.createEtchedBorder());

            pack();
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension form = getSize();
            setLocation((screen.width - form.width) / 2, (screen.height - form.height) / 2);
            setResizable(false);
            setTitle(compressionMenuTitle);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, populateGuiError + " From:" + className + "\n" + ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
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
        userControlledCompressionRadioButton.addActionListener(this);
        performanceBasedCompressionRadioButton.addActionListener(this);
        showPasswordCheckBox.addActionListener(this);
        compressionTabbedPane.addChangeListener(this);
        setPasswordCheckBox.addActionListener(this);
        setMultiPartCheckBox.addActionListener(this);
        fileNameTextBox.addKeyListener(this);
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
        } else if (e.getSource().equals(userControlledCompressionRadioButton)) {
            userControlledCompressionRadioButtonActionPerformed();
        } else if (e.getSource().equals(performanceBasedCompressionRadioButton)) {
            performanceBasedCompressionRadioButtonActionPerformed();
        } else if (e.getSource().equals(showPasswordCheckBox)) {
            showPasswordCheckBoxActionPerformed();
        } else if (e.getSource().equals(setPasswordCheckBox)) {
            setPasswordCheckBoxActionPerformed();
        } else if (e.getSource().equals(setMultiPartCheckBox)) {
            setMultiPartCheckBoxActionPerformed();
        }
    }
    
    /**
     * inFile dosyasının tek bir dosya olması durumunda, dosyanın sıkıştırmasında kullanılacak formatları 
     * gösteren selectFormatComboBox bileşeninine item değerlerinin atandığı method. 
     */
    public void fillComboBoxForFiles() {

        selectFormatComboBox.removeAllItems();
        for (String format : formats) {
            selectFormatComboBox.addItem(format);
        }
    }

    
    /**
     * inFile dosyasının bir dizin olması durumunda, dosyanın sıkıştırmasında kullanılacak formatları 
     * tutan selectFormatComboBox bileşeninine item değerlerinin atandığı method. 
     */
    public void fillComboBoxForDirectories() {

        selectFormatComboBox.removeAllItems();
        for (int i = 3; i < formats.length; i++) {
            selectFormatComboBox.addItem(formats[i]);
        }
    }

    /**
     * showPasswordCheckBox checkBoxuna tıklanması durumunda çağrılacak olan method.
     */
    private void showPasswordCheckBoxActionPerformed() {
        if (!showPasswordCheckBox.isSelected()) {
            passwordField.setVisible(true);
            passwordField.setEnabled(true);
            checkPasswordField.setVisible(true);
            checkPasswordField.setEnabled(true);
            checkPasswordLabel.setVisible(true);
            passwordTextBox.setEnabled(false);
            passwordTextBox.setVisible(false);
            passwordTextBox.setText(null);
        } else {
            passwordField.setVisible(false);
            passwordField.setEnabled(false);
            passwordField.setText(null);
            checkPasswordField.setVisible(false);
            checkPasswordField.setEnabled(false);
            checkPasswordField.setText(null);
            checkPasswordLabel.setVisible(false);
            passwordTextBox.setEnabled(true);
            passwordTextBox.setVisible(true);
        }
    }

    /**
     * outFileChooserButton buttonuna basılması durumunda çağırılan method. outFileChooser adında 
     * bir JFileChooser oluşturularak, sıkıştırılacak olan dosyanın kaydedileceği dizin seçilir.
     */
    public void outFileChooserButtonActionPerformed() {
        int value = outFileChooser.showSaveDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) {
            String outFileDirectory = outFileChooser.getSelectedFile().toString();
            
            if (outFileDirectory.compareTo(inFile.getAbsolutePath()) != 0) {
                outFileDirectoryTextBox.setText(outFileDirectory);
            } else {
                JOptionPane.showMessageDialog(this, outputEqualsInputWarning, "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * actionButton buttonuna basılması durumunda çağırılan method. Gerekli kontroller yapıldıktan sonra
     * kullanıcı tarafından girilmiş olan seçeneklere göre, sıkıştırma işleminin gerçekleşmesi için 
     * handleCompress sınıfı veya CompareAndCompress sınıfı çağrılır.
     */
    private void actionButtonActionPerformed() {
        Thread compressThread;
        boolean check;
        
        if(!fileNameTextBox.getText().isEmpty()) {
            check = StringOperations.checkFileName(fileNameTextBox.getText());
            
            if(check) {
                if (!outFileDirectoryTextBox.getText().isEmpty() && userControlledCompressionRadioButton.isSelected()) {
                    
                    String outputFilePath = outFileDirectoryTextBox.getText() + File.separator + fileNameTextBox.getText();
                    String selectedCompressionType = selectFormatComboBox.getSelectedItem().toString();
                    File outFile = new File(outputFilePath + "." + selectedCompressionType);

                    if (password.length() != 0 && partSize != 0) {
                        handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, password, partSize, overwriteCheckBox.isSelected());
                    } else if (password.length() != 0) {
                        handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, password, overwriteCheckBox.isSelected());
                    } else if (partSize != 0) {
                        handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, partSize, overwriteCheckBox.isSelected());
                    } else {
                        handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, overwriteCheckBox.isSelected());
                    }

                    compressThread = new Thread(handleCompress);
                    compressThread.start();

                    this.dispose();
                } else if (!outFileDirectoryTextBox.getText().isEmpty() && performanceBasedCompressionRadioButton.isSelected()) {
                   
                    String outFileName = fileNameTextBox.getText();
                    File outFileParent = new File(outFileDirectoryTextBox.getText());
                    
                    performanceBasedCompress = new CompareAndCompress(inFile, outFileParent, outFileName, overwriteCheckBox.isSelected());
                   
                    compressThread = new Thread(performanceBasedCompress);
                    compressThread.start();

                    this.dispose();
                } else if (outFileDirectoryTextBox.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, outputFolderWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, unsupportedCharWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
            }
        }  else if (fileNameTextBox.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, nullFileNameWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * performanceBasedCompressionRadioButton radioButtonuna basılması durumunda çağırılacak olan method. 
     * Performans tabanlı sıkıştırma seçilirse arayüzden Advance tabı kaldırılır. Bunun nedeni performans 
     * tabanlı sıkıştırmanın şifreleme ve parçalı sıkıştırmayı desteklememesidir.
     */
    private void performanceBasedCompressionRadioButtonActionPerformed() {

        if (performanceBasedCompressionRadioButton.isSelected()) {     
            selectFormatComboBox.setEnabled(false);
            selectFormatLabel.setEnabled(false);
            selectFormatPanel.setEnabled(false);
            userControlledCompressionRadioButton.setSelected(false);
            compressionTabbedPane.remove(advancedPanel);
        } else {
          
            performanceBasedCompressionRadioButton.setSelected(true);
        }
    }

    /**
     * userControlledCompressionRadioButton radioButtonuna basılması durumunda 
     * çağırılacak olan method.
     */
    private void userControlledCompressionRadioButtonActionPerformed() {

        if (userControlledCompressionRadioButton.isSelected()) {
        
            selectFormatComboBox.setEnabled(true);
            selectFormatLabel.setEnabled(true);
            selectFormatPanel.setEnabled(true);
            performanceBasedCompressionRadioButton.setSelected(false);
            compressionTabbedPane.add("Advance", advancedPanel);
        } else {

            userControlledCompressionRadioButton.setSelected(true);

        }
    }

    /**
     * ChangeListener interface inin bir methodudur. ChangeEvent i gönderen bileşen eğer 
     * compressionTabbedPane ise compressionTabbedPaneStateChanged methodu çağırılır. 
     * @param e ChangeListener interface ine sahip bir bileşen tarafından gönderilen
     * ChangeEvent türünden bir değişken.
     */
    public void stateChanged(ChangeEvent e) {

        if (e.getSource().equals(compressionTabbedPane)) {
            compressionTabbedPaneStateChanged();
        }
    }

    /**
     * compressionTabbedPane de bulunan Advance tabından General tabına geçişte Advance tabına ait
     * bazı kontrollerin ve atamaların gerçekleştirildiği method.
     */
    public void compressionTabbedPaneStateChanged() {

        if (compressionTabbedPane.getSelectedIndex() != 1) {

            if (setPasswordCheckBox.isSelected()) {
            
                if (!passwordTextBox.getText().isEmpty()) {
                    password = passwordTextBox.getText();
                } else if (!passwordField.getText().isEmpty()) {
                
                    String pass = passwordField.getText();
                    String check = checkPasswordField.getText();

                    if (!check.equals(pass)) {
                        compressionTabbedPane.setSelectedIndex(1);
                        JOptionPane.showMessageDialog(null, passwordFieldsNotEqualWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
                    } else {
                        password = passwordField.getText();
                    }
                } 
            }

            if (setMultiPartCheckBox.isSelected()) {

                if (!partSizeTextBox.getText().isEmpty()) {

                    String size = partSizeTextBox.getText();
                    boolean check = StringOperations.checkPartSize(size);
                    
                    if (!check) {
                        compressionTabbedPane.setSelectedIndex(1);
                        JOptionPane.showMessageDialog(null, onlyNumericCharsWarning, "Warning!", JOptionPane.WARNING_MESSAGE);
                    } else {
                        partSize = Integer.parseInt(size) * 1024 * 1024;
                    }
                }
            }
        }
    }

    /**
     * setMultiPartCheckBox checkBoxuna tıklanması durumunda çağırılacak olan method.
     */
    private void setMultiPartCheckBoxActionPerformed() {

        if (setMultiPartCheckBox.isSelected()) {
            partSizeTextBox.setEnabled(true);
            multiPartLabel.setEnabled(true);
        } else {
            partSizeTextBox.setEnabled(false);
            partSizeTextBox.setText(null);
            multiPartLabel.setEnabled(false);
        }
    }

     /**
     * setPasswordCheckBox checkBoxuna tıklanması durumunda çağırılacak olan method.
     */
    private void setPasswordCheckBoxActionPerformed() {

        if (setPasswordCheckBox.isSelected()) {
            passwordField.setEnabled(true);
            checkPasswordField.setEnabled(true);
            showPasswordCheckBox.setEnabled(true);
            passwordLabel.setEnabled(true);
            checkPasswordLabel.setEnabled(true);
        } else {
            passwordLabel.setEnabled(false);
            passwordTextBox.setEnabled(false);
            passwordTextBox.setVisible(false);
            passwordTextBox.setText(null);
            passwordField.setEnabled(false);
            passwordField.setVisible(true);
            passwordField.setText(null);
            checkPasswordField.setEnabled(false);
            checkPasswordField.setVisible(true);
            checkPasswordField.setText(null);
            checkPasswordLabel.setEnabled(false);
            checkPasswordLabel.setVisible(true);
            showPasswordCheckBox.setEnabled(false);
            showPasswordCheckBox.setSelected(false);
        }
    }

    /**
     * Keylistener interface ine ait bir methoddur. fileNameTextBox textboxuna girilen karakter sayısını 
     * maxFileNameTextBoxLength olarak sınırlamakta kullanılır.
     * @param e Klavye ile girilen karakter ile ilgili işlemler yapmakta kullanılan KeyEvent türündeki 
     * değişken. Bu method içerisinde kullanılmamaktadır.
     */
    public void keyPressed(KeyEvent e) {
        if(fileNameTextBox.getText().length() >= maxFileNameTextBoxLength){
             fileNameTextBox.setText(fileNameTextBox.getText().substring(0, maxFileNameTextBoxLength - 1));
        }
    }

    /**
     * KeyListener interface ine ait kullanılmayan bir method.
     */
    public void keyReleased(KeyEvent arg0) {
    }
    
    /**
     * KeyListener interface ine ait kullanılmayan bir method.
     */
    public void keyTyped(KeyEvent arg0) {
    }
}
