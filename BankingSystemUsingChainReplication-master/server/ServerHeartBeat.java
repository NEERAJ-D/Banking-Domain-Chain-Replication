package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

import common.ChainReplicationLogger;

/* 
 * Class: ServerHeartBeat
 * Purpose: Send the heart beat of the server to the Master  
 * Who Uses this: Servers to the send UDP HeartBeat packet to the Master
 */
public class ServerHeartBeat extends Thread {
	
	private DatagramSocket sendHeartBeat;
	private String whichServer;
	private String whichBank;
	private String masterIP;
	private String masterPort;
	ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.whichServer);
	public ServerHeartBeat(String whichServer, String whichBank, String masterIP, String masterPort){
			this.whichServer = whichServer;
			this.whichBank = whichBank;
			this.masterIP = masterIP;
			this.masterPort = masterPort;
	}
	
	public void run(){
		/*
		 * Every second, send heart beat to server. 
		 * In the earlier version of the code, I had made use of an Alive TCP Connection 
		 * between the Server and the Master, and after 5 seconds, the server sends a string to
		 * the master, and the master in turn responds back saying that, 
		 * Yes, I know you are alive. But, now, I get that we only need to send a UDP Packet
		 * to the Master indicating my health. Hearing back from the master process is not expected. 
		 */
		boolean firstTime = true;
		while(true){
			try
			{
				sendHeartBeat = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(this.masterIP);
			    String aliveNotification = ""; 
			    // Only for the first time send your details
			    String serverName = this.whichServer;
			    serverName = serverName.split("s")[1];
			    
			    if(firstTime || ServerProcess.reInitiateChainExtension){
			    	aliveNotification = "Add_Me:"+serverName+":"+ this.whichBank.trim();
			    	firstTime=false;
			    	if(ServerProcess.reInitiateChainExtension){
			    		System.out.println("Restarted Chain Extension");
			    		//chainReplicationLogger.myLogger.log(Level.INFO, "$$$$$ Restarted Chain Extension");
			    		ServerProcess.reInitiateChainExtension = false;
			    	}else {
			    		chainReplicationLogger.myLogger.log(Level.INFO, "Sent First HeartBeat to Master");
			    		
			    	}
			    	
			    } else {
			    	aliveNotification = "I_Am_Alive:"+serverName;
			    }
			    
			    DatagramPacket sendPacket = new DatagramPacket(aliveNotification.getBytes(), aliveNotification.getBytes().length, 
			    		IPAddress, Integer.parseInt(this.masterPort));
			    sendHeartBeat.send(sendPacket);
			    //System.out.println("I Sent the alive notification "+aliveNotification);
			    sendHeartBeat.close();
			    // Sleep for few ms, and again send alive notification to the server
				Thread.sleep(2000);
				Runtime.getRuntime().freeMemory();
				
			} catch(SocketTimeoutException s)
			{
				System.out.println("Socket timed out you client!");
			} catch(IOException e)
			{
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		

	}
	
}