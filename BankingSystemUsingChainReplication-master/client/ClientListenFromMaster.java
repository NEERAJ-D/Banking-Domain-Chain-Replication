package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.logging.Level;

import server.ServerObjectPassing;
import common.ChainReplicationLogger;

/*
 * Class 		: 	ListenFromMaster 
 * Purpose		: 	A thread class for listening the notifications from the Master 
 * Who uses this: 	All servers
 */
	public class ClientListenFromMaster extends Thread{
		String clientName;
		String cListenPortFromMaster;
		private ServerSocket serverSocket;

		public ClientListenFromMaster(String clientName, String cListenPortFromMaster) throws Exception{
			this.clientName = clientName;
			this.cListenPortFromMaster = cListenPortFromMaster;
		}
		
		public void run()
		   {
			
		      while(true)
		      {
		    	  ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.clientName);
		         try
		         {
		 			HashMap configFileDetails = ClientsCopyofConfigFile.getInstance().getClientConfigDetails(); 

		 			DatagramSocket clientListeningSocket = new DatagramSocket(Integer.parseInt(this.cListenPortFromMaster));
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					clientListeningSocket.receive(receivePacket);
		 			String receivedNotification = new String(receivePacket.getData());
		 			receivedNotification = receivedNotification.trim();
		 			clientListeningSocket.close();
					System.out.println(" I Received Following from Master ="+receivedNotification);
					chainReplicationLogger.myLogger.log(Level.INFO, "******* Received from Master Update Notification: ["+receivedNotification+"]");
		            // Get the earlier chain configuration
		            String [] rcdTokens = receivedNotification.split(":");
		            String bank = rcdTokens[1];
		            String newChain = rcdTokens[2];
		            
		            HashMap temp = ClientsCopyofConfigFile.getInstance().getClientConfigDetails();
		            String temp1 = bank+"_chain";
		            String oldChain = (String)temp.get(temp1.trim());
		            
		            // In all the cases, update the local copy of HashMap
		            ClientsCopyofConfigFile.getInstance().updateClientHashMap(bank+"_chain", newChain.trim());
		            
		         } catch(SocketTimeoutException s){
		            System.out.println("Server Socket timed out!");
		            break;
		         } catch(IOException e) {
		            e.printStackTrace();
		            break;
		         } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      }
		   }
}

	