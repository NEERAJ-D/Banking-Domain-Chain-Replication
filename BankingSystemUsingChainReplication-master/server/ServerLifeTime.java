package server;

import java.util.Random;
import java.util.logging.Level;

import common.ChainReplicationLogger;

/*
 * Class: ServerLifeTime
 * Purpose: Monitor the lifetime of the server. 
 * Check if the server has reached the value of sending and receiving messages, and if so, 
 * the server process commits suicide! 
 */
public class ServerLifeTime extends Thread {
	private String serverName;
	private String lifeTimeValue;
	private String processID;
	public ServerLifeTime(String serverName, String lifeTimeValue, String processID){
		this.serverName = serverName;
		this.lifeTimeValue = lifeTimeValue;
		this.processID = processID;
	}
	private static int generateRandomValue(){
		Random randForLifeValue = new Random();
		return randForLifeValue.nextInt(10);
	}
	public void run(){
		/*
		 * Read the values of the lifeTime, and decide the course of action
		 * Possible values are: 
		 * (1) AfterReceiving(value)
		 * (2) AfterReceiving(random)
		 * (3) AfterSending(value)
		 * (4) AfterSending(random)
		 * (5) Unbounded -> Server never dies
		 */
		
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.serverName);
		String afterReceiving = "dieAfterReceiving(";
		String afterSending = "dieAfterSending(";
		int numMessages = 0;
		int randomNumMessages = generateRandomValue();
		StringBuffer outputToLog = new StringBuffer();
		outputToLog.append("Operating with LifeTime Value of "+this.lifeTimeValue);
		String randomString = (lifeTimeValue.contains("random"))?" : where random Value is: ["+randomNumMessages+"]":""; 
		outputToLog.append(randomString);
		chainReplicationLogger.myLogger.log(Level.INFO, outputToLog.toString());
		while(true){
			
			try{
				if(lifeTimeValue.equalsIgnoreCase("Unbounded")){
					// The Server never terminates
				}
				else if (lifeTimeValue.startsWith(afterReceiving)){
					
					// Get the value of the number of messages after which we have to kill
					if(lifeTimeValue.contains("random")){
						numMessages = randomNumMessages;
					} else {
						String value = lifeTimeValue.split("\\(")[1];
						value = value.replace(value.substring(value.length()-1), "");
						numMessages = Integer.parseInt(value);
					}
					/* Here we have used less than condition because, there was a
					 * case when the receive seq. no. value was equal to numMessages, 
					 * but was not captured in that fraction of second
					 */
					if(numMessages<=ServerProcess.getReceiveSequenceNumber()){
						// Kill the server process
						System.out.println("Killing Myself");
						chainReplicationLogger.myLogger.log(Level.INFO, "Initiate Killing MySelf");
						Runtime.getRuntime().exec("kill -9 "+ this.processID);
					}
					
				}
				else if (lifeTimeValue.startsWith(afterSending)){
					if(lifeTimeValue.contains("random")){
						numMessages = randomNumMessages;
					} else {
						String value = lifeTimeValue.split("\\(")[1];
						value = value.replace(value.substring(value.length()-1), "");
						numMessages = Integer.parseInt(value);
					}
					if(numMessages<=ServerProcess.getSendSequenceNumber()){
						// Kill the server process
						System.out.println("KILL PROCESS");
						chainReplicationLogger.myLogger.log(Level.INFO, "Initiate Killing MySelf");
						Runtime.getRuntime().exec("kill -9 "+ this.processID);
					}
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			Runtime.getRuntime().freeMemory();
			
		}
	}
}

