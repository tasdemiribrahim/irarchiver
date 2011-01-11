package Gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import Common.CompressHandler;
import Common.MainVocabulary;
import Common.MyLogger;
import Common.StringOperations;
import javax.swing.JOptionPane;

public class PreCompress extends JFrame implements ActionListener, ChangeListener, KeyListener, MainVocabulary 
{
    private String className = PreCompress.class.getName(),outFileDirectory,outFileName,password = "";
    private JLabel inFileLabel, outFileLabel, selectFormatLabel, fileNameLabel, passwordLabel, checkPasswordLabel, multiPartLabel;
    private JCheckBox overwriteCheckBox, showPasswordCheckBox, setPasswordCheckBox, setMultiPartCheckBox;
    private JTextField inFileTextBox, passwordTextBox, outFileDirectoryTextBox, fileNameTextBox, partSizeTextBox;
    private JButton outFileChooserButton, actionButton, cancelButton;
    private JSpinner selectFormatSpinner;
    private JPanel mainPanel, selectFormatPanel, generalPanel, advancedPanel, passwordPanel, multiPartPanel;
    private JRadioButton userControlledCompressionRadioButton, performanceBasedCompressionRadioButton;
    private JTabbedPane compressionTabbedPane;
    private JPasswordField passwordField, checkPasswordField;
    private JFileChooser outFileChooser;
    private File inFile;
    private CompressHandler handleCompress;
    private int partSize = 0, maxFileNameTextBoxLength = 15;

    public PreCompress(File inFile) throws Exception 
    {
        try
        {
            this.inFile = inFile;
            initComponents();
            if (createAndShowGUI()) 
            {
                initiateActions();
                addAssistiveSupport();
            }
        }
        catch (Exception ex) 
        { 
            trayIcon.setToolTip(constructError);
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }
    
    public void initComponents() 
    {
        outFileDirectory=inFile.getParent().toString();
        
        inFileLabel = new JLabel("Source:");
        outFileLabel = new JLabel("Destination:");
        selectFormatLabel = new JLabel("Compression Format:");
        fileNameLabel = new JLabel("Archive Name");
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

    public boolean createAndShowGUI() throws Exception 
    {
        try 
        {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            add(compressionTabbedPane, constraints);
            generalPanel.add(inFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            generalPanel.add(inFileTextBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            generalPanel.add(outFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            generalPanel.add(outFileDirectoryTextBox, constraints);
            constraints.gridx = 3;
            constraints.gridwidth = 1;
            generalPanel.add(outFileChooserButton, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            generalPanel.add(fileNameLabel, constraints);
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            generalPanel.add(fileNameTextBox, constraints);
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
            generalPanel.add(cancelButton, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            mainPanel.add(userControlledCompressionRadioButton, constraints);
            constraints.gridx = 2;
            mainPanel.add(selectFormatPanel, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            mainPanel.add(performanceBasedCompressionRadioButton, constraints);

            if (inFile.isFile()) 
                selectFormatSpinner = new JSpinner(new SpinnerListModel(formats));
            else if (inFile.isDirectory()) 
                selectFormatSpinner = new JSpinner(new SpinnerListModel(formatsForDirectory));
           
            constraints.gridy = 0;
            selectFormatPanel.add(selectFormatLabel, constraints);
            constraints.gridy = 1;
            constraints.gridwidth = 3;
            selectFormatPanel.add(selectFormatSpinner, constraints);

            constraints.gridy = 0;
            constraints.gridwidth = 1;
            advancedPanel.add(setPasswordCheckBox, constraints);
            constraints.gridy = 1;
            advancedPanel.add(passwordPanel, constraints);
            constraints.gridy = 2;
            advancedPanel.add(setMultiPartCheckBox, constraints);
            constraints.gridy = 3;
            advancedPanel.add(multiPartPanel, constraints);

            constraints.gridy = 0;
            passwordPanel.add(passwordLabel, constraints);
            constraints.gridx = 1;
            passwordPanel.add(passwordTextBox, constraints);
            passwordPanel.add(passwordField, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            passwordPanel.add(checkPasswordLabel, constraints);
            constraints.gridx = 1;
            passwordPanel.add(checkPasswordField, constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            passwordPanel.add(showPasswordCheckBox, constraints);

            constraints.gridy = 0;
            multiPartPanel.add(multiPartLabel, constraints);
            constraints.gridx = 1;
            multiPartPanel.add(partSizeTextBox, constraints);

            compressionTabbedPane.addTab("General", generalPanel);
            compressionTabbedPane.addTab("Advanced", advancedPanel);

            userControlledCompressionRadioButton.setSelected(true);
            outFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileNameTextBox.setText(inFile.getName());
            inFileTextBox.setText(inFile.toString());
            inFileTextBox.setEnabled(false);
            outFileDirectoryTextBox.setText(outFileDirectory);
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
            setLocationRelativeTo(null);
            setResizable(false);
            setTitle(compressionMenuTitle);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setVisible(true);
        } 
        catch (Exception ex) 
        {
            trayIcon.setToolTip(populateGuiError);
            throw new Exception(populateGuiError + "at" + className + newline + ex.getMessage());
        } 
        return true;
    }

    public void initiateActions() 
    {
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
    
    public void actionPerformed(ActionEvent e) 
    {
        try
        {
            if (e.getSource().equals(cancelButton)) 
                closeActionPerformed();
            else if (e.getSource().equals(outFileChooserButton)) 
                outFileChooserButtonActionPerformed();
            else if (e.getSource().equals(actionButton)) 
                actionButtonActionPerformed();
            else if (e.getSource().equals(userControlledCompressionRadioButton)) 
                compressionRadioButtonActionPerformed(true);
            else if (e.getSource().equals(performanceBasedCompressionRadioButton))
                compressionRadioButtonActionPerformed(false);
            else if (e.getSource().equals(showPasswordCheckBox))
                showPasswordCheckBoxActionPerformed();
            else if (e.getSource().equals(setPasswordCheckBox))
                setPasswordCheckBoxActionPerformed();
            else if (e.getSource().equals(setMultiPartCheckBox))
                setMultiPartCheckBoxActionPerformed();
        } 
        catch (InterruptedException ex) 
        {
            MyLogger.getLogger().info(ex.getMessage());
        } 
        catch (Exception ex) 
        {
            MyLogger.getLogger().info(ex.getMessage());
        } 
    }

    private void addAssistiveSupport() 
    {
        inFileLabel.setLabelFor(inFileTextBox);
        outFileLabel.setLabelFor(outFileDirectoryTextBox);
        fileNameLabel.setLabelFor(fileNameTextBox);
        selectFormatLabel.setLabelFor(selectFormatSpinner);
        passwordLabel.setLabelFor(passwordTextBox);
        passwordLabel.setLabelFor(passwordField);
        checkPasswordLabel.setLabelFor(checkPasswordField);
        multiPartLabel.setLabelFor(partSizeTextBox);
        
        outFileChooserButton.setToolTipText("Opens output directory chooser window(CTRL+F1)");
        selectFormatSpinner.setToolTipText("Choose archive format(bz2,lzma,gz is avaible for single files)");
        
        overwriteCheckBox.setMnemonic(KeyEvent.VK_V);
        setMultiPartCheckBox.setMnemonic(KeyEvent.VK_M);
        setPasswordCheckBox.setMnemonic(KeyEvent.VK_P);
        showPasswordCheckBox.setMnemonic(KeyEvent.VK_S);
        actionButton.setMnemonic(KeyEvent.VK_O);
        cancelButton.setMnemonic(KeyEvent.VK_C);
        outFileChooserButton.setMnemonic(KeyEvent.VK_F1);
        userControlledCompressionRadioButton.setMnemonic(KeyEvent.VK_U);
        performanceBasedCompressionRadioButton.setMnemonic(KeyEvent.VK_B);
   
    }

    private void closeActionPerformed() throws Exception 
    {
        Gui.FrameOperations.deleteFrame(this.getClass().toString(),false);
        this.dispose();
    }
    
    private void showPasswordCheckBoxActionPerformed() 
    {
        boolean choise=showPasswordCheckBox.isSelected();
        passwordField.setVisible(!choise);
        passwordField.setEnabled(!choise);
        checkPasswordField.setVisible(!choise);
        checkPasswordField.setEnabled(!choise);
        checkPasswordLabel.setVisible(!choise);
        passwordTextBox.setEnabled(choise);
        passwordTextBox.setVisible(choise);
        passwordTextBox.setText(null);
        checkPasswordField.setText(null);
        passwordField.setText(null);
    }

    public void outFileChooserButtonActionPerformed()
    {
        int value = outFileChooser.showSaveDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) 
        {
            outFileDirectory = outFileChooser.getSelectedFile().toString();
            if (outFileDirectory.compareTo(inFile.getAbsolutePath()) != 0)
                outFileDirectoryTextBox.setText(outFileDirectory);
            else
                trayIcon.displayMessage("Warning !",outputEqualsInputWarning, TrayIcon.MessageType.ERROR);
        }
    }

    private void actionButtonActionPerformed() throws InterruptedException, Exception
    {
        try
        {
            Thread compressThread;
            boolean check,overwrite=overwriteCheckBox.isSelected();
            boolean performansSelected=performanceBasedCompressionRadioButton.isSelected();
            outFileName=fileNameTextBox.getText();
            outFileDirectory=outFileDirectoryTextBox.getText();
            if(!outFileName.isEmpty())
            {
                check = StringOperations.checkFileName(outFileName);
                int confirm = JOptionPane.YES_OPTION;
                if(check) 
                {
                    if (!outFileDirectory.isEmpty() && !performansSelected) 
                    {
                        if(!(new File(outFileDirectory)).exists())
                            confirm = JOptionPane.showConfirmDialog(this, "Output Directory Doesn't Exist.\nDo You Want To Create?", "Warning!", JOptionPane.YES_NO_OPTION);
                        if(confirm==JOptionPane.YES_OPTION)
                        {  
                            (new File(outFileDirectory)).mkdir();
                            String selectedCompressionType = selectFormatSpinner.getValue().toString();
                            File outFile = new File(outFileDirectory + File.separator + outFileName + "." + selectedCompressionType);
                            
                            if (password.length() != 0 && partSize != 0)
                                handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, password, partSize, overwrite);
                            else if (password.length() != 0)
                                handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, password, overwrite);
                            else if (partSize != 0)
                                handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, partSize, overwrite);
                            else
                                handleCompress = new CompressHandler(inFile, outFile, selectedCompressionType, overwrite);

                            compressThread = new Thread(handleCompress);
                            compressThread.start();
                            if (Thread.interrupted()) 
                            {
                                trayIcon.setToolTip("HandleCompress Thread Interrupted");
                                throw new InterruptedException("HandleCompress Thread Interrupted at " + className);
                            }
                            closeActionPerformed();
                        }
                    } 
                    else if (!outFileDirectory.isEmpty() && performansSelected) 
                    {
                        File outFileParent = new File(outFileDirectory);

                        frames[Gui.Main.getFramesLenght()] = new CompareAndCompress(inFile, outFileParent, outFileName, overwrite);
                        Gui.Main.setFramesLenght(Gui.Main.getFramesLenght()+1);

                        compressThread = new Thread((CompareAndCompress)frames[Gui.Main.getFramesLenght()-1]);
                        compressThread.start();
                        if (Thread.interrupted()) 
                        {
                            trayIcon.setToolTip("performanceBasedCompress Thread Interrupted");
                            throw new InterruptedException("performanceBasedCompress Thread Interrupted at " + className);
                        }
                        closeActionPerformed();
                    } 
                    else if (outFileDirectory.isEmpty()) 
                        trayIcon.displayMessage("Warning!",outputFolderWarning, TrayIcon.MessageType.WARNING);
                } 
                else 
                    trayIcon.displayMessage("Warning!",unsupportedCharWarning, TrayIcon.MessageType.WARNING);
            }  
            else if (outFileName.isEmpty()) 
                    trayIcon.displayMessage("Warning!",nullFileNameWarning, TrayIcon.MessageType.WARNING);
        } 
        catch (Exception ex) 
        {
            trayIcon.setToolTip("Action error");
            throw new Exception("Action error" + " at " + className + newline + ex.getMessage());
        }
    }

    private void compressionRadioButtonActionPerformed(boolean choise) 
    {
        selectFormatSpinner.setEnabled(choise);
        selectFormatLabel.setEnabled(choise);
        selectFormatPanel.setEnabled(choise);
        overwriteCheckBox.setSelected(!choise);
        overwriteCheckBox.setEnabled(choise);
        userControlledCompressionRadioButton.setSelected(choise);
        performanceBasedCompressionRadioButton.setSelected(!choise);
        if(!choise)
            compressionTabbedPane.remove(advancedPanel);
        else
            compressionTabbedPane.add("Advance", advancedPanel);
    }

    public void stateChanged(ChangeEvent e) 
    {
        if (e.getSource().equals(compressionTabbedPane)) 
            compressionTabbedPaneStateChanged();
    }

    public void compressionTabbedPaneStateChanged() 
    {
        String pass = new String(passwordField.getPassword());
        String checkPass = new String(checkPasswordField.getPassword());
        if (compressionTabbedPane.getSelectedIndex() != 1) 
        {
            if (setPasswordCheckBox.isSelected()) 
            {
                if (!passwordTextBox.getText().isEmpty()) 
                    password = passwordTextBox.getText();
                else if (!pass.isEmpty()) 
                {
                    if (!checkPass.equals(pass)) 
                    {
                        compressionTabbedPane.setSelectedIndex(1);
                        trayIcon.displayMessage("Warning!",passwordFieldsNotEqualWarning, TrayIcon.MessageType.WARNING);
                    } 
                    else 
                        password = pass;
                } 
            }
            if (setMultiPartCheckBox.isSelected()) 
            {
                if (!partSizeTextBox.getText().isEmpty()) 
                {
                    String size = partSizeTextBox.getText();
                    boolean check = StringOperations.checkPartSize(size);
                    if (!check) 
                    {
                        compressionTabbedPane.setSelectedIndex(1);
                        trayIcon.displayMessage("Warning!",onlyNumericCharsWarning, TrayIcon.MessageType.WARNING);
                    } 
                    else 
                        partSize = Integer.parseInt(size) * 1024 * 1024;
                }
            }
        }
    }

    private void setMultiPartCheckBoxActionPerformed() 
    {
        boolean choise=setMultiPartCheckBox.isSelected();
        partSizeTextBox.setEnabled(choise);
        multiPartLabel.setEnabled(choise);
        partSizeTextBox.setText(null);
    }

    private void setPasswordCheckBoxActionPerformed() 
    {
        boolean choise=setPasswordCheckBox.isSelected();
        passwordField.setEnabled(choise);
        checkPasswordField.setEnabled(choise);
        showPasswordCheckBox.setEnabled(choise);
        passwordLabel.setEnabled(choise);
        checkPasswordLabel.setEnabled(choise);
        passwordTextBox.setEnabled(choise);
        passwordTextBox.setText(null);
        passwordField.setText(null);
        checkPasswordField.setText(null);
    }

    public void keyPressed(KeyEvent e)
    {
        if(fileNameTextBox.getText().length() >= maxFileNameTextBoxLength)
             fileNameTextBox.setText(fileNameTextBox.getText().substring(0, maxFileNameTextBoxLength - 1));
    }

    public void keyTyped(KeyEvent e) { }
    public void keyReleased(KeyEvent e) { }
}
