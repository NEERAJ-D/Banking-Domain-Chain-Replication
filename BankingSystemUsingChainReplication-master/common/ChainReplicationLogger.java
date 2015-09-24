package common;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * Class 		: 	ChainReplicationLogger
 * Purpose		: 	Singleton Logger to log to file 
 * Who uses this: 	Everyone!
 */

public class ChainReplicationLogger{
	public static final Logger myLogger = Logger.getLogger("Test");
	private static ChainReplicationLogger instance = null;

	 public static ChainReplicationLogger getInstance(String fileName) {
	    if(instance == null) {
	        prepareLogger(fileName);
	        instance = new ChainReplicationLogger();
	    }
	    return instance;
	 }

	 private static void prepareLogger(String fileName) {
			 try {
			    FileHandler myFileHandler = new FileHandler("./logs/"+fileName+".txt", false);
			    myFileHandler.setFormatter(new ChainReplicationLogFormatter());
			    myLogger.addHandler(myFileHandler);
			    myLogger.setUseParentHandlers(false);
			    myLogger.setLevel(Level.ALL);
			 } catch (Exception e) {
			 }
		 }

	 
	 
}
