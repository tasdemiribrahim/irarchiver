package Common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//import java.net.Socket;
//import java.net.UnknownHostException;
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
    	File f;
    	if(type.equals(errorLabel))
    	   f = new File(errorLogPath);
       else
    	   f = new File(historyLogPath);
    	f.delete();
    }
    
    public static void send(String error)
    {
    	/* try 
		{
			Socket erSocket = new Socket("127.0.0.1",5000);
			PrintWriter writer = new PrintWriter(erSocket.getOutputStream());
			writer.println(error);
		/* Serverside
		 * ServerSocket serverSock= new ServerSocket(5000);	
		 * while(true)
		 * {
		 * Socket sock = serverSock.accept();
		 * PrintWriter writer = new PrintWriter(sock.getOutputStream();
		 * writer.println("Okey");
		 * writer.close () ;
		 * }
			
		} catch (UnknownHostException e) {
			MyLogger.logger.info(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			MyLogger.logger.info(e.getMessage());
			e.printStackTrace();
		}
		 */
    }
    
    public static void addHistory(String msg) throws IOException 
    {
       DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT,currentLocale);
       outputStream = new PrintWriter(new FileWriter(historyLogPath,true));
       outputStream.println(dateFormatter.format(new Date()) + ":" + msg);
       outputStream.close();
    }
}