package server;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import common.ChainReplicationLogger;
import common.ServerReply;
import common.Request;
import common.ChainReplicationLogFormatter;

/*
 * Class 		: 	ServerProcess
 * Purpose		: 	All servers use this class for starting up.  
 * Who uses this: 	All servers
 */
public class ServerProcess {
	
	/*
	 * Sequence Numbers for keeping a count of the number of messages this server
	 * has sent and received from other servers
	 */
	
	public static int sendSequenceNumber = 0;
	public static int receiveSequenceNumber = 0;
	public static int numOfBanks = 0;
	public static String[] bankChains;
	public static String mySuccessor = null;
	public static String myPredecessor = null;
	public static String processID = null;
	public static boolean tailToStoreUpdates = false;
	
	public static boolean reInitiateChainExtension = false;
	
	public static int reqSequenceNumber = 0;
	public static String iBelongToBank = "";
	public static String transferInProgressWithReqID = "";
	public static boolean transferInProgress  = false;
	
	public static boolean transferFailureCase1 = false; 
	public static boolean transferFailureCase2 = false;
	/**
	 * @return the sendSequenceNumber
	 */
	public static int getSendSequenceNumber() {
		return sendSequenceNumber;
	}

	/**
	 * @param sendSequenceNumber the sendSequenceNumber to set
	 */
	public static void setSendSequenceNumber(int sendSequenceNumber) {
		ServerProcess.sendSequenceNumber = sendSequenceNumber;
	}

	/**
	 * @return the receiveSequenceNumber
	 */
	public static int getReceiveSequenceNumber() {
		return receiveSequenceNumber;
	}

	/**
	 * @param receiveSequenceNumber the receiveSequenceNumber to set
	 */
	public static void setReceiveSequenceNumber(int receiveSequenceNumber) {
		ServerProcess.receiveSequenceNumber = receiveSequenceNumber;
	}

	
	/**
	 * @return the mySuccessor
	 */
	public static String getMySuccessor() {
		return mySuccessor;
	}

	/**
	 * @param mySuccessor the mySuccessor to set
	 */
	public static void setMySuccessor(String mySuccessor) {
		ServerProcess.mySuccessor = mySuccessor;
	}

	/**
	 * @return the myPredecessor
	 */
	public static String getMyPredecessor() {
		return myPredecessor;
	}

	/**
	 * @param myPredecessor the myPredecessor to set
	 */
	public static void setMyPredecessor(String myPredecessor) {
		ServerProcess.myPredecessor = myPredecessor;
	}
	
	/*
	 * The name of the server is specified as the argument
	 * Usage: java server.ServerProcess s1
	 * where s1 indicates the name of the server
	 */
	public static void main(String[] args) {

		if(args.length!=2){
			System.out.println("Please specify the name of the server and configuration file path");
			System.out.println("Usage: java server.ServerProcess <Server Name> <Path of Config File>\n Example: java server.ServerProcess s1 Config.txt");
			return;
		}

		/*
		 * Instantiate the logger for file logging 
		 */
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(args[0]);
		processID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		chainReplicationLogger.myLogger.log(Level.INFO, "Server ["+args[0]+"] Process Started with Process ID : "+processID);
		
		System.out.println("Server ["+args[0]+"] started");
		
		SentObjClass.getInstance();
		HistObj.getInstance();
		ServerProcess sp = new ServerProcess();

		ConcurrentHashMap serverHashMap = new ConcurrentHashMap();
		String sSendToPortNo = null;
		String sListenFromClientsOn = null;
		String sListenFromSuccessorServersOn = null;
		String sListenFromPredecessorServersOn = null;
		String sListenFromMasterOn = null;
		String sBank = null;
		String masterIP = null;
		String masterListenPort = null;
		String bankChain = null;
		String predecessor = null;
		String successor = null;
		String serverLifeTimeValue = null;
		String serverStartUpDelay = null;
		boolean isHead = false;
		boolean isTail = false;
		try{
			serverHashMap = ServersCopyofConfigFile.getInstance().readSetupValuesFromServerConfigFile(args[1]); 
					
			String serverPrefix = args[0].toString()+"_";
			
			sListenFromClientsOn = (String) serverHashMap.get(serverPrefix+"listenFromClientsOn");
			sListenFromSuccessorServersOn = (String) serverHashMap.get(serverPrefix+"listenFromSuccessorServersOn");
			sListenFromPredecessorServersOn = (String) serverHashMap.get(serverPrefix+"listenFromPredecessorServersOn");
			sListenFromMasterOn = (String) serverHashMap.get(serverPrefix+"listenFromMasterOn");
			
			serverLifeTimeValue = (String) serverHashMap.get(serverPrefix+"lifeTime");
			serverStartUpDelay = (String) serverHashMap.get(serverPrefix+"startUpDelay");
			//System.out.println("GOING TO SLEEP");
			if(Integer.parseInt(serverStartUpDelay)!=0){
				// Sleep for the time mentioned in the Start up delay
				chainReplicationLogger.myLogger.log(Level.INFO, "Sleeping for Time " + serverStartUpDelay +" mentioned in Config File");
				Thread.sleep(Integer.parseInt(serverStartUpDelay));
			}
			//System.out.println("WOKE UP");
			
			sBank = (String) serverHashMap.get(serverPrefix+"bank");
			ServerProcess.iBelongToBank = sBank;
			System.out.println("I belong to = "+ServerProcess.iBelongToBank);
			bankChain = (String) serverHashMap.get(sBank+"_chain");
			
			String[] tokenizedBankChain = bankChain.split(",");
			ServerObjectPassing sv = new ServerObjectPassing();
			ServerProcess.myPredecessor = sv.getPredecessor(args[0], tokenizedBankChain);
			ServerProcess.mySuccessor = sv.getSucessor(args[0], tokenizedBankChain);
			/* 
			 * Get details of the Master Process
			 */
			masterIP = (String) serverHashMap.get("m_IP");
			masterListenPort = (String) serverHashMap.get("m_heartBeatListenPort");
			
		}
		catch(Exception e){
			e.printStackTrace();			
		}
		
		
		
		/*
		 * Start a thread which will send heart-beat(alive notification) of this server to the Master
		 * Message Sent: <clientName>, I am alive
		 */
		Thread serverHeartBeat = new ServerHeartBeat(args[0], sBank, masterIP, masterListenPort);
		serverHeartBeat.start();
	
		/*
		 * Monitor the lifeTime of the server, and kill itself when value reached
		 */
		//System.out.println("SENDING LIFE TIME VALUE AS === "+serverLifeTimeValue);
		
		Thread serverLifeTime = new ServerLifeTime(args[0], serverLifeTimeValue, processID);
		serverLifeTime.start();
		
		/* 
		 * Listen from the Master process for any updates in the chain		 * 
		 */
		try{
		Thread listenFromMaster = new ListenFromMaster(args[0], sListenFromMasterOn);
		listenFromMaster.start();

		Thread listenFromClient = new ListenFromClient(args[0],sListenFromClientsOn);
		listenFromClient.start();
		
		Thread listenFromPredecessor = new ListenFromPredecessorServer(args[0], sListenFromPredecessorServersOn);
		listenFromPredecessor.start();
		
		Thread listenFromSucessor = new ListenFromSuccessorServer(args[0], sListenFromSuccessorServersOn);
		listenFromSucessor.start();


		} catch (Exception e) {
			e.printStackTrace();
		}


		Runtime.getRuntime().freeMemory();
		
	}



}

