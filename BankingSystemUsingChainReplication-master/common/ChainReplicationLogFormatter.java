package common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * Class 		: 	ChainReplicationLogFormatter
 * Purpose		: 	To format the output which will be sent to the log file 
 * Who uses this: 	Everyone!
 */
public class ChainReplicationLogFormatter extends Formatter
{
        public String format(LogRecord record){
            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat ("[yyyy-M-dd hh:mm:ss a]");
        	return ft.format(dNow) + " [" + record.getLevel() + "] " + record.getMessage() + "\r\n";
        }
}