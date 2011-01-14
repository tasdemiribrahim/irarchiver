package Gui;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Common.MainVocabulary;
import Common.MyLogger;
import java.awt.event.KeyEvent;

public class About extends JFrame implements MainVocabulary, ActionListener
{
	private static final long serialVersionUID = 5165229384063481199L;
	private static final String className = About.class.getName();
    private JButton closeButton;
    private JLabel nameLabel, projectNameLabel, versionLabel, projectVersionLabel, authorNameLabel,
            projectAuthorNameLabel1, projectAuthorNameLabel2, adviserNameLabel, projectAdviserNameLabel;
    
    public About() throws Exception
    {
        try
        {
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
        nameLabel = new JLabel("Project Name:");
        projectNameLabel = new JLabel(projectName);
        versionLabel = new JLabel("Version:");
        projectVersionLabel = new JLabel(projectVersion);
        authorNameLabel = new JLabel("Authors:");
        projectAuthorNameLabel1 = new JLabel(Author1);
        projectAuthorNameLabel2 = new JLabel(Author2);
        closeButton = new JButton("Close");
        adviserNameLabel = new JLabel("Adviser:");
        projectAdviserNameLabel = new JLabel(Adviser);
    }

    private void addAssistiveSupport() 
    {
        closeButton.setMnemonic(KeyEvent.VK_C);
        
        nameLabel.setLabelFor(projectNameLabel);
        versionLabel.setLabelFor(projectVersionLabel);
        authorNameLabel.setLabelFor(projectAuthorNameLabel1);
        authorNameLabel.setLabelFor(projectAuthorNameLabel2);
        adviserNameLabel.setLabelFor(projectAdviserNameLabel);
    }
    
    private boolean createAndShowGUI() throws Exception
    {
        try 
        {
            setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();	   
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5,5,5,5);
            constraints.weightx = .5;
            
            constraints.gridx = 0;
            constraints.gridy = 0;
            add(nameLabel,constraints);      
            constraints.gridx = 1;
            add(projectNameLabel,constraints);
            constraints.gridx = 0;
            constraints.gridy = 1;
            add(versionLabel,constraints);
            constraints.gridx = 1;
            add(projectVersionLabel,constraints);
            constraints.gridx = 0;
            constraints.gridy = 2;
            add(authorNameLabel,constraints);
            constraints.gridx = 1;
            add(projectAuthorNameLabel1,constraints);
            constraints.gridy = 3;
            add(projectAuthorNameLabel2,constraints);
            constraints.gridx = 0;
            constraints.gridy = 4;
            add(adviserNameLabel,constraints);
            constraints.gridx = 1;
            add(projectAdviserNameLabel,constraints);
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.gridwidth = 2;
            add(closeButton,constraints);
            
            setSize(250,200);
            setTitle(projectName + " - Info");
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setVisible(true);
            } 
        catch (Exception ex) 
        {
            trayIcon.setToolTip(populateGuiError);
            throw new Exception(populateGuiError + "at" + className + newline + ex.getMessage());
        } 
        return true;
    }
    
    public void actionPerformed(ActionEvent e) 
    { 
        try
        {
        	Gui.FrameOperations.deleteFrame(this.getClass().toString(),true);
        } 
        catch (Exception ex) 
        {
            MyLogger.getLogger().info(ex.getMessage());
        }
    }

    private void initiateActions() 
    {
        closeButton.addActionListener(this);
    }    
}
