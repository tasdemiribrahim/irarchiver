package Gui;

import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import Common.MainVocabulary;
import Common.MyLogger;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

public class Log extends JFrame implements MainVocabulary,ActionListener,ChangeListener
{
	private static final long serialVersionUID = -2511711375232899490L;
	private static final String className = Log.class.getName();
	private final Log _this =this;
    private static String type,size,currentFont;
    private int currentStyle;
    private JMenuBar mainMenu;
    private JMenu logMenu;
    private JComboBox fontBox,stylesBox;
    private JMenuItem refreshItem, printItem,eraseItem;
    private static AbstractDocument doc;
    private static JTextPane logTextPane=null;
    private JScrollPane logjScrollPane;
    private JSpinner sizes;
    private JButton closeButton;
    
    public Log(String headerLabel) throws Exception 
    {
        try
        {
            Log.type=headerLabel;
            initComponents();
            if(createAndShowGUI()) 
            {
                initiateActions();
                addAssistiveSupport();
                writeLog();
            }
        }
        catch (Exception ex) 
        { 
            trayIcon.setToolTip(constructError);
            throw new Exception(constructError + className + newline + ex.getMessage());
        }
    }
    
    private void initComponents() 
    {
        fontBox = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        stylesBox = new JComboBox(styleNames);
        sizes = new JSpinner(new SpinnerNumberModel(12, 6, 24, 1));
        mainMenu = new JMenuBar();
        logMenu = new JMenu("Menu");
        logTextPane = new JTextPane();
        logjScrollPane = new JScrollPane(logTextPane);
        logjScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        logjScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        closeButton = new JButton("Close");
        refreshItem = new JMenuItem("Refresh");
        printItem = new JMenuItem("Print");
        eraseItem = new JMenuItem("Erase");
    }
    
    public static void setFont(String currentFont,int currentStyle)
    {
        logTextPane.setFont(new Font(currentFont, currentStyle, 16));
    }

    private void addAssistiveSupport() 
    {
        logMenu.setMnemonic(KeyEvent.VK_M);
        closeButton.setMnemonic(KeyEvent.VK_C);
        refreshItem.setMnemonic(KeyEvent.VK_R);
        printItem.setMnemonic(KeyEvent.VK_P);
        eraseItem.setMnemonic(KeyEvent.VK_E);
    }
    
    private boolean createAndShowGUI() throws Exception 
    {
        try 
        {
            setSize(new Dimension(800, 600)); 
            
            logTextPane.setCaretPosition(0);
            logTextPane.setEditable(false);
            StyledDocument styledDoc = logTextPane.getStyledDocument();
            doc = (AbstractDocument)styledDoc;
            
            add(logjScrollPane,BorderLayout.CENTER);  
            add(closeButton,BorderLayout.PAGE_END);
            
            mainMenu.add(logMenu);
            logMenu.add(refreshItem);
            logMenu.add(printItem);
            logMenu.addSeparator();
            logMenu.add(eraseItem); 
            mainMenu.add(new JLabel("||Fonts:"));  
            mainMenu.add(fontBox);
            fontBox.setMaximumRowCount(5);
            mainMenu.add(stylesBox);
            stylesBox.setMaximumRowCount(5);
            mainMenu.add(sizes);
            
            fontBox.setSelectedItem(props.getProperty("font"));
            
            size = "12";
            currentFont=(String)fontBox.getSelectedItem();
            currentStyle=0;
            setFont();
                    
            setJMenuBar(mainMenu);
            setTitle(projectName + " - " + Log.type + " Log");
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setVisible(true);
        } 
        catch (Exception ex) 
        {
            throw new Exception(populateGuiError + className + newline + ex.getMessage());
        } 
        return true;
    }
    
    private void initiateActions() 
    {
        closeButton.addActionListener(new closeLogListener());
        eraseItem.addActionListener(new eraseLogListener());
        fontBox.addActionListener(new fontBoxListener());
        stylesBox.addActionListener(new styleBoxListener());
        printItem.addActionListener(new printItemListener());
        sizes.addChangeListener(this);
        refreshItem.addActionListener(this);
    }
    
    public static void writeLog() throws IOException
    {
        try 
        {  
            boolean bold;
            BufferedReader inputStream=null;
            logTextPane.setText("");
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            if(type.equals(errorLabel))
                inputStream = new BufferedReader(new FileReader(errorLogPath));
            if(type.equals(historyLabel))
                inputStream = new BufferedReader(new FileReader(historyLogPath));
            String l= "";
            while ((l = inputStream.readLine()) != null) 
            {
                if(l.matches(logBoldLineStartsWith))
                    bold=true;
                else
                    bold=false;
                StyleConstants.setBold(attrs, bold);
                doc.insertString(doc.getLength(), l+newline, attrs);
            }
            inputStream.close();
            logTextPane.setCaretPosition(logTextPane.getDocument().getLength());
        }
        catch (BadLocationException ex) 
        {
            MyLogger.getLogger().info(ex.getMessage());
            MyLogger.send(ex.getMessage());
        }
    }
    
    public void actionPerformed(ActionEvent e) 
    { 
        try {
			Log.writeLog();
		} catch (IOException ex) {
			MyLogger.getLogger().info(ex.getMessage());
            MyLogger.send(ex.getMessage());
		}
    }

    class printItemListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
			try {
	            PrinterJob job = PrinterJob.getPrinterJob();
	            boolean ok = job.printDialog();
	            if (ok)
						logTextPane.print();
			} catch (PrinterException ex) {    
				MyLogger.getLogger().info(ex.getMessage());
                MyLogger.send(ex.getMessage());		      
			}
	    }
	}
    
    class styleBoxListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
            currentStyle=stylesBox.getSelectedIndex();
            setFont();
	    }
    }
    
    class fontBoxListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
            currentFont=(String)fontBox.getSelectedItem();
            props.setProperty("font", currentFont);
            setFont();
	    }
    }
    
    class closeLogListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
	        try
	        {
	            Gui.FrameOperations.deleteFrame(_this.getClass().toString(),true);
	        }
	        catch (Exception ex) 
	        { 
	            trayIcon.setToolTip("Close Error");
	            MyLogger.getLogger().info("Close Error " + className + newline + ex.getMessage());
                MyLogger.send("Close Error " + className + newline + ex.getMessage());
	        }
	    }
    }

    class eraseLogListener implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
	    {
	        int confirm = JOptionPane.showConfirmDialog(_this, eraseLogMessage, type, JOptionPane.YES_NO_OPTION);
	        if (confirm  == JOptionPane.YES_OPTION)
	        {
	            try {
					MyLogger.eraseLog(type);
		            Log.writeLog();
				} catch (IOException ex) {
		            MyLogger.getLogger().info(ex.getMessage());     
		            MyLogger.send(ex.getMessage());
				}
	        }
	    }
    }

    public void stateChanged(ChangeEvent e)
    {
        size = sizes.getModel().getValue().toString();
        setFont();
    }

    private void setFont() 
    {
       logTextPane.setFont(new Font(props.getProperty("font"),currentStyle,Integer.parseInt(size)));
    }

}
