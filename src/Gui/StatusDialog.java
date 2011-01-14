package Gui;

import Common.MainVocabulary;
import Common.MyLogger;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class StatusDialog extends JFrame implements MainVocabulary, ActionListener 
{
	private static final long serialVersionUID = 1291752033458227611L;
	private String className = StatusDialog.class.getName();
    private String cancelMessage, succeedMessage,archiveName,historyMsg;
    public boolean cancel = false, isIndeterminate = false, isEditable = true;
    private JProgressBar statusProgressBar;
    private JLabel stateLabel;
    private JButton cancelButton, okButton;

    public StatusDialog(String archiveName) throws Exception
    {
        try
        {
            this.archiveName=archiveName;
            this.historyMsg=archiveName;
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

    private void addAssistiveSupport() 
    {
       cancelButton.setMnemonic(KeyEvent.VK_C);
       okButton.setMnemonic(KeyEvent.VK_O);
    }

    private void initComponents() 
    {
        stateLabel = new JLabel("Compressing...");
        statusProgressBar = new JProgressBar();
        cancelButton = new JButton("Cancel");
        okButton = new JButton("Ok");
    }

    private boolean createAndShowGUI() throws Exception
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
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            add(stateLabel, constraints);
            constraints.gridy = 1;
            add(statusProgressBar, constraints);
            constraints.gridy = 2;
            add(cancelButton, constraints);
            add(okButton, constraints);

            statusProgressBar.setStringPainted(true);
            stateLabel.setHorizontalAlignment(JLabel.CENTER);
            okButton.setVisible(false);
            okButton.setEnabled(false);

            setSize(250, 125);
            setTitle(archiveName);
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

    private void initiateActions()
    {
        cancelButton.addActionListener(new cancelButtonListener());
        okButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) 
    {
        try
        {
            Gui.FrameOperations.deleteFrame(this.getClass().toString(),false);
            this.dispose();
        } 
        catch (Exception ex) 
        {
            MyLogger.getLogger().info(ex.getMessage());
        }
    }

    class cancelButtonListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
	        cancel = true;
	        stateLabel.setText(cancelMessage);
	        statusProgressBar.setValue(0);
	        
	        if(isIndeterminate)
	            setIndeterminate(false);
	        statusProgressBar.setStringPainted(false);
	        
	        cancelButton.setVisible(false);
	        okButton.setVisible(true);
	        okButton.setEnabled(true);
	    }
    }

    public void setStateToEncrypt () 
    {
        this.historyMsg+=" with encrypt";
        stateLabel.setText("Encrypting...");
        statusProgressBar.setValue(0);
        setIndeterminate(false);
    }
    
    public void setStateToDencrypt () 
    {
        this.historyMsg+=" with decrypt";
        stateLabel.setText("Decrypting...");
        statusProgressBar.setValue(0);
        setIndeterminate(false);
    }
    
    public void setStateToCompress () 
    {
        if(!this.historyMsg.startsWith("Compressed"))
            this.historyMsg="Compressed " + this.historyMsg;
        setTitle(compressionDialogTitle);
        cancelMessage = compressionCanceledMessage;
        succeedMessage = compressionSucceedMessage;
        stateLabel.setText("Compressing...");
        statusProgressBar.setValue(0);
    }
    
    public void setStateToDecompress () 
    {
        if(!this.historyMsg.startsWith("Decompressed"))
            this.historyMsg="Decompressed " + this.historyMsg;
        setTitle(decompressionDialogTitle);
        cancelMessage = decompressionCanceledMessage;
        succeedMessage = decompressionSucceedMessage;
        stateLabel.setText("Decompressing...");
        statusProgressBar.setValue(0);
    }
    
    public void setStateToSplitFile()
    {
        this.historyMsg+=" with spliting";
        setIndeterminate(true);
        stateLabel.setText("Spliting Files...");
    }
    
    public void setStateToJoinFile()
    {
        this.historyMsg+=" with joining";
        setIndeterminate(true);
        stateLabel.setText("Joining Files...");
    }
    
    public void setStatus(long completed , long total ) 
    {
        int milestone = (int)(((double)completed / (double)total) * 100);
        if (milestone <= 100) 
            statusProgressBar.setValue(milestone);
    }

    public boolean isCanceled() 
    {
        return cancel;
    }
    
    public void setCanceled(boolean b) 
    {
        this.cancel=b;
    }
    
    public void setEditable(boolean choise)
    {
        isEditable = choise;
    }

    public void setIndeterminate(boolean choice) 
    {
        if(isEditable)
        {
            statusProgressBar.setIndeterminate(choice);
            statusProgressBar.setStringPainted(!choice);
            isIndeterminate = choice;
        }
    }

    public void completeDialog() throws IOException 
    {
        MyLogger.addHistory(this.historyMsg);
        stateLabel.setText(succeedMessage);
        if(isIndeterminate)
            setIndeterminate(false);
        statusProgressBar.setValue(100);
        trayIcon.displayMessage(archiveName,succeedMessage, TrayIcon.MessageType.INFO);
        Main.playSound();
        cancelButton.setVisible(false);
        okButton.setVisible(true);
        okButton.setEnabled(true);
    }
    
    public void cancelDialog()
    {
        cancel = true;
        setVisible(false);
    }
}
