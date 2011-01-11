package Gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import Common.DecompressHandler;
import Common.MainVocabulary;
import Common.MyLogger;
import javax.swing.JOptionPane;

public class PreDecompress extends JFrame implements ActionListener, MainVocabulary 
{
    private String className = PreDecompress.class.getName();
    private JLabel inFileLabel, outFileLabel;
    private JCheckBox overwriteCheckBox;
    private JTextField inFileTextBox, outFileTextBox;
    private JButton outFileChooserButton, actionButton, cancelButton;
    private JFileChooser outFileChooser;
    private File inFile, outFileParent;
    private DecompressHandler handleDecompress;

    public PreDecompress(File inFile) throws Exception 
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

    public boolean createAndShowGUI() throws Exception 
    {
        try 
        {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            add(inFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            add(inFileTextBox, constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            add(outFileLabel, constraints);
            constraints.gridx = 1;
            constraints.gridwidth = 2;
            add(outFileTextBox, constraints);
            constraints.gridx = 3;
            constraints.gridwidth = 1;
            add(outFileChooserButton, constraints);
            constraints.gridx = 1;
            constraints.gridy = 2;
            add(overwriteCheckBox, constraints);
            constraints.gridy = 3;
            add(actionButton, constraints);
            constraints.gridx = 2;
            add(cancelButton, constraints);

            outFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            inFileTextBox.setText(inFile.toString());
            outFileTextBox.setText(inFile.getParent().toString());
            outFileParent=inFile.getParentFile();
            inFileTextBox.setEnabled(false);

            pack();
            setTitle(decompressionMenuTitle);
            setLocationRelativeTo(null);
            setResizable(false);
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
        } 
        catch (Exception ex) 
        { 
            MyLogger.getLogger().info(ex.getMessage());
        } 
    }

    public void outFileChooserButtonActionPerformed()
    {
        int value = outFileChooser.showSaveDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) 
            outFileTextBox.setText(outFileChooser.getSelectedFile().toString());

    }

    public void actionButtonActionPerformed() throws InterruptedException, Exception 
    {
        try
        {
            int confirm=JOptionPane.YES_OPTION;
            outFileParent = new File(outFileTextBox.getText());
            if (outFileParent != null) 
            {
                if(!outFileParent.exists())
                    confirm = JOptionPane.showConfirmDialog(this, "Output Directory Doesn't Exist.\nDo You Want To Create?", "Warning!", JOptionPane.YES_NO_OPTION);
                if(confirm==JOptionPane.YES_OPTION)
                {   
                    outFileParent.mkdir();
                    handleDecompress = new DecompressHandler(inFile, outFileParent, overwriteCheckBox.isSelected());  
                    Thread decompressThread = new Thread(handleDecompress);
                    decompressThread.start();
                    if (Thread.interrupted()) 
                    {
                        trayIcon.setToolTip("decompressThread Thread Interrupted");
                        throw new InterruptedException("decompressThread Thread Interrupted at" + className);
                    }
                    closeActionPerformed();
                }
            } 
            else
                trayIcon.displayMessage("Warning!",outputFolderWarning, TrayIcon.MessageType.WARNING);
        } 
        catch (Exception ex) 
        { 
            trayIcon.setToolTip("Action error");
            throw new Exception("Action error" + " at " + className + newline + ex.getMessage());
        } 
    }

    private void addAssistiveSupport() 
    {
        inFileLabel.setLabelFor(inFileTextBox);
        outFileLabel.setLabelFor(outFileTextBox);
        overwriteCheckBox.setMnemonic(KeyEvent.VK_V);
        outFileChooserButton.setMnemonic(KeyEvent.VK_F1);
        actionButton.setMnemonic(KeyEvent.VK_O);
        cancelButton.setMnemonic(KeyEvent.VK_C);
       
        outFileChooserButton.setToolTipText("Opens input directory chooser window(CTRL+F1)");
    }

    private void closeActionPerformed() throws Exception 
    {
        Gui.FrameOperations.deleteFrame(this.getClass().toString(),false);
        this.dispose();
    }
}