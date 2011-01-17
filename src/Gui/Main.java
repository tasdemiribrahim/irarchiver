package Gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.awt.AWTException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import Common.MainVocabulary;
import Common.MyLogger;
import Common.Music;
public class Main extends JFrame implements WindowListener,ActionListener, MainVocabulary, MouseMotionListener,Serializable
{
	private static final long serialVersionUID = 1L;
	static final String className = Main.class.getName();
    JFileChooser mainChooser;
    JMenuBar mainMenu;
    JMenu fileMenu,errorMenu,radioGroup,themeMenu,LNFMenu;
    JMenuItem fileQuitItem, aboutInfoItem, errorLogItem,historyLogItem;
    JButton compressButton, decompressButton;
    File selectedFile;
    SystemTray tray;
    PopupMenu popup;
    MenuItem aboutTrayItem, quitTrayItem, elogTrayItem, hlogTrayItem, hideTrayItem, showTrayItem;
    ButtonGroup group,themeGroup;
    FrameOperations operation = new FrameOperations();
    public Main() throws Exception
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

    private void addAssistiveSupport() throws Exception
    {
       Set forwardKeys = getFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
       Set newForwardKeys = new HashSet(forwardKeys);
       newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
       setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,newForwardKeys);
  
       fileMenu.setMnemonic(KeyEvent.VK_F);
       errorMenu.setMnemonic(KeyEvent.VK_L);
       radioGroup.setMnemonic(KeyEvent.VK_V);
       fileQuitItem.setMnemonic(KeyEvent.VK_Q);
       aboutInfoItem.setMnemonic(KeyEvent.VK_I);
       errorLogItem.setMnemonic(KeyEvent.VK_E);
       historyLogItem.setMnemonic(KeyEvent.VK_H);
       compressButton.setMnemonic(KeyEvent.VK_C);
       decompressButton.setMnemonic(KeyEvent.VK_D);
       
       mainMenu.setToolTipText("This menu can do view,log and file operations");
       fileMenu.setToolTipText("This submenu holds information and quit items");
       errorMenu.setToolTipText("This submenu holds error and history logs");
       fileQuitItem.setToolTipText("To quit program");
       aboutInfoItem.setToolTipText("To view information about program and designers");
       errorLogItem.setToolTipText("To view error log");
       historyLogItem.setToolTipText("To view history log");
       compressButton.setToolTipText("To compress selected file");
       decompressButton.setToolTipText("To decompress selected file");
       radioGroup.setToolTipText("To view popup windows differently");
       mainChooser.getAccessibleContext().setAccessibleName("Please choose a file to compress or decompress");
    }

    private void initComponents() throws Exception 
    {
        File[] deleted=new File(tempPath).listFiles();
        
        for(int i=0;i<deleted.length;i++)
            deleted[i].delete();
        props.load(new FileInputStream("general.properties"));
        UIManager.setLookAndFeel(props.getProperty("style"));
            
        Common.FileOperations.createTempDirectory();
        mainChooser = new JFileChooser();
        mainMenu = new JMenuBar();
        fileMenu = new JMenu("File");
        fileQuitItem = new JMenuItem("Quit");
        aboutInfoItem = new JMenuItem("Info");
        errorMenu = new JMenu("Logs");
        errorLogItem = new JMenuItem(errorLabel);
        historyLogItem = new JMenuItem(historyLabel);
        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");
        
        radioGroup = new JMenu("View");
        themeMenu = new JMenu("Themes");
        LNFMenu = new JMenu("Style");
        
        group = new ButtonGroup();
        themeGroup = new ButtonGroup();
        
        aboutTrayItem = new MenuItem("About");
        quitTrayItem = new MenuItem("Quit");
        elogTrayItem = new MenuItem(errorLabel);
        hlogTrayItem = new MenuItem(historyLabel);
        hideTrayItem = new MenuItem("Hide");
        showTrayItem = new MenuItem("Show");
        
        popup = new PopupMenu();
        tray = SystemTray.getSystemTray();
     }

    public boolean createAndShowGUI() throws Exception 
    {
        try 
        {
            if (SystemTray.isSupported()) 
            {
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("Idle");
                
                popup.add(hideTrayItem);
                popup.add(showTrayItem);
                popup.addSeparator();
                popup.add(elogTrayItem);
                popup.add(hlogTrayItem);
                popup.addSeparator();
                popup.add(aboutTrayItem);
                popup.add(quitTrayItem);
                
                trayIcon.setPopupMenu(popup);
                tray.add(trayIcon);
            }
            setIconImage(new ImageIcon(imageURL).getImage());    
            mainMenu.add(fileMenu);
            fileMenu.add(aboutInfoItem);
            fileMenu.add(fileQuitItem);
            
            mainMenu.add(errorMenu);
            errorMenu.add(errorLogItem);
            errorMenu.add(historyLogItem);
            
            mainMenu.add(radioGroup);
            radioGroup.add(LNFMenu);
            radioGroup.add(themeMenu);
            themeMenu.setEnabled(false);
            
            LookAndFeelInfo[] nlf=UIManager.getInstalledLookAndFeels();
            for(int i=0;i<nlf.length;i++)
            {
                JRadioButtonMenuItem Look= new JRadioButtonMenuItem(nlf[i].getName());
                Look.setActionCommand(nlf[i].getClassName());
                Look.setEnabled(operation.isAvailableLookAndFeel(Look.getActionCommand()));
                if(nlf[i].getClassName().equals(props.getProperty("style")))
                {
                    Look.setSelected(true);
                    if(nlf[i].getClassName().equals(props.getProperty("metal"))) 
                        themeMenu.setEnabled(true);
                }
                Look.addActionListener(styleListener);
                LNFMenu.add(Look);
                group.add(Look);
            }
                    
            for(int i=0;i<themes.length;i++)
            {
                JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(themes[i].getName());
                themeMenu.add(themeItem);
                themeGroup.add(themeItem);
                themeItem.addActionListener(themeListener);
                themeItem.setActionCommand(themes[i].getClass().getName());
            }
    
            setJMenuBar(mainMenu);

            mainChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            mainChooser.setControlButtonsAreShown(false);
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(5, 5, 5, 5);
            constraints.weightx = .5;

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridheight = 5;
            constraints.gridwidth = 5;
            add(mainChooser, constraints);
            constraints.gridy = 5;
            constraints.gridheight = 1;
            constraints.gridwidth = 1;
            add(compressButton, constraints);
            constraints.gridx = 1;
            add(decompressButton, constraints);
            
            pack();
            setTitle(projectName);
            setResizable(false);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setVisible(true);
            }
        catch (AWTException ex) 
        { 
            trayIcon.setToolTip("Tray Icon Error");
            throw new Exception("Tray Icon Error" + "at" + className);
        } 
        catch (Exception ex) 
        { 
            trayIcon.setToolTip(populateGuiError);
            throw new Exception(populateGuiError + className + newline + ex.getMessage());
        } 
        return true;
    }

    ActionListener themeListener = new ActionListener() 
    {
        public void actionPerformed(ActionEvent e) 
        {
            try 
            {
                operation.repaintTheme(e.getActionCommand());
            }
            catch (Exception ex) 
            {
                MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
            }
        }
    };
        
    ActionListener styleListener = new ActionListener() 
    {
        public void actionPerformed(ActionEvent e) 
        {
            try 
            {
                if(e.getActionCommand().equals(props.getProperty("metal")))
                    themeMenu.setEnabled(true);
                else 
                    themeMenu.setEnabled(false);
                props.setProperty("style",e.getActionCommand());
                operation.repaintGUI(e.getActionCommand());
            } 
            catch (ClassNotFoundException ex) 
            {
                MyLogger.getLogger().info(ex.getMessage()+" ClassNotFoundException");
                MyLogger.send(ex.getMessage()+" ClassNotFoundException");
            } 
            catch (InstantiationException ex) 
            {
                MyLogger.getLogger().info(ex.getMessage()+" InstantiationException");
                MyLogger.send(ex.getMessage()+" InstantiationException");
            } 
            catch (IllegalAccessException ex)
            {
                MyLogger.getLogger().info(ex.getMessage()+" IllegalAccessException");
                MyLogger.send(ex.getMessage()+" IllegalAccessException");
            } 
            catch (UnsupportedLookAndFeelException ex)
            {
                MyLogger.getLogger().info(ex.getMessage()+" UnsupportedLookAndFeelException");
                MyLogger.send(ex.getMessage()+" UnsupportedLookAndFeelException");
            } 
            catch (Exception ex)
            {
                MyLogger.getLogger().info(ex.getMessage()+" UnknownException");
                MyLogger.send(ex.getMessage()+" UnknownException");
            }
        }
    };

    class decompressListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try 
			{
		        selectedFile = mainChooser.getSelectedFile();
		        if (selectedFile != null)
						frames.add(new PreDecompress(selectedFile));
				else 
		            trayIcon.displayMessage("Warning!",nullInputFileWarning, TrayIcon.MessageType.WARNING);
			}
	        catch (Exception ex) 
	        {
	            MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
	        }
	    }
    }
    
    class compressListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try 
			{
				selectedFile = mainChooser.getSelectedFile();
		        if (selectedFile != null) 
		        {
		            mainChooser.rescanCurrentDirectory();
		            frames.add(new PreCompress(selectedFile));
		        }
		        else 
		            trayIcon.displayMessage("Warning!",nullInputFileWarning, TrayIcon.MessageType.WARNING);
		    }
	        catch (Exception ex) 
	        {
	            MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
	        }
	    }
    }
    
    class aboutListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try 
			{
		        Gui.FrameOperations.deleteFrame(About.class.toString(),true);
		        frames.add(new About());
		    }
	        catch (Exception ex) 
	        {
	            MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
	        }
	    }
    }
    
    class logListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try 
			{
		        Gui.FrameOperations.deleteFrame(Log.class.toString(),true);
		        frames.add(new Log(e.getActionCommand()));
		    }
	        catch (Exception ex) 
	        {
	            MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
	        }
	    }
    }
    
    class trayListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try 
			{
                operation.setVisiblity(true);
                operation.setFocus(true);
		    }
	        catch (Exception ex) 
	        {
	            MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
	        }
	    }
    }
    
    class hideTrayListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try 
			{
                operation.setVisiblity(false);
		    }
	        catch (Exception ex) 
	        {
	            MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());
	        }
	    }
    }
    
    public void actionPerformed(ActionEvent e) 
    {
        try 
        {  
            int confirm = JOptionPane.showConfirmDialog(this, quitMessage, "Warning!", JOptionPane.YES_NO_OPTION);
            if (confirm  == JOptionPane.YES_OPTION)
                System.exit(0);
        } 
        catch (Exception ex) 
        {
            MyLogger.getLogger().info(ex.getMessage());
            MyLogger.send(ex.getMessage());
        }
    }

    public void mouseMoved(MouseEvent e) 
    {
        mainChooser.rescanCurrentDirectory();
    }
    public void mouseDragged(MouseEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) 
    {
        try {
            props.store(new FileOutputStream("general.properties"), null);
        } catch (IOException ex) {
            MyLogger.getLogger().info(ex.getMessage());
            MyLogger.send(ex.getMessage());
        }
    }
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e)
    {
        operation.setVisiblity(false);
    }
    public void windowDeiconified(WindowEvent e) 
    {
        operation.setVisiblity(true);
        operation.setFocus(true);
    }
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    
    public void initiateActions() throws Exception 
    {
        try
        {
            compressButton.addActionListener(new compressListener());
            decompressButton.addActionListener(new decompressListener());
            aboutInfoItem.addActionListener(new aboutListener());
            errorLogItem.addActionListener(new logListener());
            historyLogItem.addActionListener(new logListener());
            fileQuitItem.addActionListener(this);
            mainChooser.addMouseMotionListener(this);
            
            trayIcon.addActionListener(new trayListener());
            aboutTrayItem.addActionListener(new aboutListener());
            quitTrayItem.addActionListener(this);
            elogTrayItem.addActionListener(new logListener());
            hlogTrayItem.addActionListener(new logListener());
            hideTrayItem.addActionListener(new hideTrayListener());
            showTrayItem.addActionListener(new trayListener());
            
            addWindowListener(this);
        }
        catch (Exception ex) 
        { 
            trayIcon.setToolTip(addActionListenerError);
            throw new Exception(addActionListenerError + className + newline + ex.getMessage());
        } 
    }
    
    public static void main(String args[]){
        try {
            MyLogger.setup();
            frames.add(new Main());
            //frames.get(0).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception ex) {
            //(new Thread(new Music())).start();
        	new Music();
            MyLogger.getLogger().info(ex.getMessage());
            MyLogger.send(ex.getMessage());
        }
    }
}