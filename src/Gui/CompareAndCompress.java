package Gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import Common.FileOperations;
import Common.MainVocabulary;
import Common.MyLogger;

public class CompareAndCompress extends JFrame implements ActionListener, MainVocabulary, Runnable 
{
    private static final String className = CompareAndCompress.class.getName();
    private JLabel  supportedFormatsLabel, sizesLabel,bestFormatTitleLabel, bestFormatTypeLabel, bestFormatSizeTitleLabel, bestFormatSizelabel,
            resultLabel;
    private JButton closeButton;
    private boolean overwrite;
    private String outFileName,formatName[],tempFormatName;
    private File archivedFiles[],inFile, outFileParent;
    private StatusDialog compressDialog;
    private double formatSizes[],  tempFormatSize;
    GridBagConstraints constraints = new GridBagConstraints();
    
    public CompareAndCompress(File inFile, File outFileParent, String outFileName, boolean overwrite) throws Exception
    {
        try
        {
            this.inFile = inFile;
            this.outFileParent = outFileParent;
            this.outFileName = outFileName;
            this.overwrite = overwrite;

            compressDialog = new StatusDialog(this.outFileName);
            compressDialog.setStateToCompress();
            compressDialog.setIndeterminate(true);
            compressDialog.setEditable(false);

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
        closeButton.setMnemonic(KeyEvent.VK_C);
        
        bestFormatTitleLabel.setLabelFor(bestFormatTypeLabel);
        bestFormatSizeTitleLabel.setLabelFor(bestFormatSizelabel);
    }

    private void initComponents() 
    {
        supportedFormatsLabel = new JLabel("<html><u>Formats</u></html>");
        sizesLabel = new JLabel("<html><u>Sizes</u></html>");
        resultLabel = new JLabel("<html><u>Result</u></html>");
        
        bestFormatTitleLabel = new JLabel("Selected Format:");
        bestFormatTypeLabel = new JLabel();
        bestFormatSizeTitleLabel = new JLabel("Compressed Size:");
        bestFormatSizelabel = new JLabel();
        
        closeButton = new JButton("Close");
    }

    private void addFormatsToGui() throws Exception 
    {
        try
        {
            JLabel newFormatTitleLabel;
            JLabel newFormatResultLabel;
            constraints.gridx=0;
            constraints.gridy=1;
            for(int i=0;i<formatName.length;i++)
            {
                newFormatTitleLabel=new JLabel(formatName[i]);
                add(newFormatTitleLabel, constraints);

                constraints.gridx++;
                newFormatResultLabel=new JLabel(String.valueOf(formatSizes[i]));
                add(newFormatResultLabel, constraints);
                constraints.gridy++;
                constraints.gridx--;
            }
            add(resultLabel, constraints);
            constraints.gridy++;
            add(bestFormatTitleLabel, constraints);
            constraints.gridx++;
            add(bestFormatTypeLabel, constraints);
            constraints.gridx--;
            constraints.gridy++;
            add(bestFormatSizeTitleLabel, constraints);
            constraints.gridx++;
            add(bestFormatSizelabel, constraints);
            constraints.gridx--;
            constraints.gridy++;
            constraints.gridwidth = 3;
            add(closeButton, constraints);
        }
        catch (Exception ex) 
        { 
            trayIcon.setToolTip("Extra" + populateGuiError);
            throw new Exception("Extra" + populateGuiError + className + newline + ex.getMessage());
        } 
    }
    
    private boolean createAndShowGUI() throws Exception 
    {
        try 
        {
            setLayout(new GridBagLayout());
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            add(supportedFormatsLabel, constraints);
            constraints.gridx = 1;
            add(sizesLabel, constraints);
            
            closeButton.setVisible(true);
            closeButton.setEnabled(true);
            
            setLocationRelativeTo(null);
            setTitle(compressionResultTitle);
            setResizable(false);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        closeButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) 
    {
        if (e.getSource().equals(closeButton)) 
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
    }

    private void findBestFormat() throws FileNotFoundException, IOException, Exception
    {
        try
        {
            if (!compressDialog.isCanceled()) 
            {
                compress();
                File bestFormat = getBestFormat();
                File outFile = new File(outFileParent.getAbsolutePath() + File.separator + bestFormat.getName());
                compressDialog.setVisible(false);
                FileOperations.copyFile(bestFormat, outFile);
                pack();
                setLocationRelativeTo(null);
                setResizable(false);
                setVisible(true);
            }
            for(int i=0;i<archivedFiles.length;i++)
                archivedFiles[i].delete();
        } 
        catch (Exception ex) 
        {
            trayIcon.setToolTip("findBestFormat Error");
            throw new Exception("findBestFormat Error" + "at" + className + newline + ex.getMessage());
        } 
    }

    private File getBestFormat() throws Exception 
    {
        try
        {
            for (int i = 0; i < formatName.length-1; i++) 
                for(int j=i+1; j < formatName.length; j++)
                    if (formatSizes[i] > formatSizes[j]) 
                    {
                        tempFormatSize = formatSizes[i];
                        formatSizes[i] = formatSizes[j];
                        formatSizes[j] = tempFormatSize;

                        tempFormatName = formatName[i];
                        formatName[i] = formatName[j];
                        formatName[j] = tempFormatName;
                    }
            addFormatsToGui();
            bestFormatTypeLabel.setText(formatName[0]);
            MyLogger.addHistory("Best Compressed "+outFileName+"."+formatName[0]);
            bestFormatSizelabel.setText(String.valueOf(formatSizes[0]));    
            return new File(tempPath + File.separator + outFileName + "."+formatName[0]);
        } 
        catch (Exception ex) 
        {
            trayIcon.setToolTip("getBestFormat Error");
            throw new Exception("getBestFormat Error" + "at" + className + newline + ex.getMessage());
        } 
    }

    private void compress() throws Exception
    {
        try
        {   
            if (!inFile.isDirectory()) 
                formatName = formats;
            else 
                formatName = formatsForDirectory;
            formatSizes = new double[formatName.length];
            archivedFiles=new File[formatName.length];   
            
            for(int i=0;i<archivedFiles.length;i++)
                archivedFiles[i] = new File(tempPath + File.separator + outFileName + "."+formatName[i]);
            
            new Tar.CreateArchive(inFile, archivedFiles[0], compressDialog);
            for(int i=1;i<archivedFiles.length;i++)
                if (!compressDialog.isCanceled()) 
                {
                    if(i<=Compressors.length)
                        Compressors[i-1].Compress(inFile, archivedFiles[i], true, compressDialog);
                    else    
                        Compressors[(i%Compressors.length)-1].Compress(inFile, archivedFiles[i], false, compressDialog);
                }
            
            for(int i=0;i<formatSizes.length;i++)
                formatSizes[i]=FileOperations.getFileSizeInMegabytes(archivedFiles[i]);
        } 
        catch (Exception ex) 
        {
            trayIcon.setToolTip("compress Error");
            throw new Exception("compress Error" + "at" + className + newline + ex.getMessage());
        } 
    }
    
    public void run() {
        try {
            findBestFormat();
        } catch (Exception ex) {
            MyLogger.getLogger().info(ex.getMessage());
        }
    }
}