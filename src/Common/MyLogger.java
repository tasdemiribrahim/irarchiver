package Common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger implements MainVocabulary
{       
    private static FileHandler fileTxt;
    private static SimpleFormatter formatterTxt;
    private static Logger logger;
    private static PrintWriter outputStream = null;
    public static void setup() throws IOException 
    {
        logger = Logger.getLogger(" ");
        logger.setLevel(Level.ALL);
        fileTxt = new FileHandler(errorLogPath,true);
        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
    }
    
    public static Logger getLogger()
    {
        return MyLogger.logger;
    }
    
    public void setLogger(Logger newLogger)
    {
        MyLogger.logger=newLogger;
    }
    
    public void setLevel(Level newLevel)
    {
        MyLogger.logger.setLevel(newLevel);
    }
    
    public static void eraseLog(String type) throws IOException 
    {
       if(type.equals(errorLabel))
           outputStream = new PrintWriter(new FileWriter(errorLogPath));
       else
           outputStream = new PrintWriter(new FileWriter(historyLogPath));
       outputStream.print("");
       outputStream.close();
    }
    
    public static void addHistory(String msg) throws IOException 
    {
       DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT,currentLocale);
       outputStream = new PrintWriter(new FileWriter(historyLogPath,true));
       outputStream.println(dateFormatter.format(new Date()) + ":" + msg);
       outputStream.close();
    }
}