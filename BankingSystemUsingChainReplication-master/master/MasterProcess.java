package master;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.text.html.MinimalHTMLWriter;

import common.ChainReplicationLogger;


/*
 * Class: MasterProcess
 * Purpose: The main program for running the Master Process of Chain Replication
 * Who Uses This: Only a single master process will be run, and all the clients and 
 * the servers communicate with the Master process 
 */
public class MasterProcess {
	
	public static long MonitorServerArray[] = new long[10];
	public static boolean chainExtensionInProgress = false;
	public static String prospectiveNewTail = null;
	public static boolean killProspectiveThreadsData = false;
	
	public static String reqIDofOngoingTransfer = "";
	public static boolean ongoingTransfer = false;
	public static String destBankofOngoingTransfer = "";
	public static Object srcBankofOngoingTransfer = "";
	
	public static void main(String[] args) {
		if(args.length!=1){
			System.out.println("Please specify the name of the Configuration file path");
			System.out.println("Usage: java master.MasterProcess <Config.txt>");
			return;
		}
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance("Master");
		chainReplicationLogger.myLogger.log(Level.INFO, "Master Process Started with Process ID : "+ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		
        HashMap masterHashMap = new HashMap();
        String heartBeatListenPort = null;
        String serverNotificationListenPort = null;
        try{
                masterHashMap = MastersCopyofConfigFile.getInstance().readSetupValuesFromMasterConfigFile(args[0]); 
                heartBeatListenPort = (String) masterHashMap.get("m_heartBeatListenPort");
                serverNotificationListenPort = (String) masterHashMap.get("m_serverNotificationListenPort");
                
        } catch (Exception e){
                e.printStackTrace();
        }
        
        new Thread(new MonitorServerHealth()).start();
        chainReplicationLogger.myLogger.log(Level.INFO, "Master started listening Server Heartbeats");
		//System.out.println("Master started listening Server Heartbeats");
        
		
		try {
			new Thread(new MasterListenNotificationThread(heartBeatListenPort,serverNotificationListenPort)).start();
			chainReplicationLogger.myLogger.log(Level.INFO, "Master started listening Server Notifications");
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
