package master;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import common.ChainReplicationLogger;

public class SendNotificationsFromMaster implements Runnable{
	private String masterListenPort; 
	private String serverNotificationListenPort;
	private ServerSocket serverSocket;
	private String oldTail;
	private String newWannaBeTail;
	
	ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance("Master");
	
	public SendNotificationsFromMaster(String oldTail, String newWannaBeTail, String serverNotificationListenPort){
		
		try {
			serverSocket = new ServerSocket(Integer.parseInt(serverNotificationListenPort));
		} catch (NumberFormatException | IOException e1) {
			
			e1.printStackTrace();
		}	
		
	}
		public void run(){
			
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		String oldTailListeningOn = (String) masterHashMap.get(oldTail+"_listenFromMasterOn");
		String oldTailIP = (String) masterHashMap.get(oldTail+"_IP");
		String newWannaBeTailListeningOn = (String) masterHashMap.get(newWannaBeTail+"_listenFromMasterOn");
		String newWannaBeTailTailIP = (String) masterHashMap.get(newWannaBeTail+"_IP");
		try {
	        // Receive notification from the new tail that it is ready to act as the new tail 
			Socket server = serverSocket.accept();
			
   			
			DataInputStream in = new DataInputStream(server.getInputStream());
            String receivedNotification = in.readUTF();
            
            // This marks the end of the Chain Extension
            MasterProcess.chainExtensionInProgress=false;
            server.close();
            
            
            System.out.println("Received I am ready, now updating the HashMap" + receivedNotification);
            
            String tailValueReceivedInNotifcation = receivedNotification.split(":")[0].trim();
            
            chainReplicationLogger.myLogger.log(Level.INFO, "Received Notification from Prospective New Tail ["+tailValueReceivedInNotifcation+"] "
            		+ " that it is ready to take over as Tail. ");
            
            
            // Update the HashMap with the new tail value
            
            String newTailsBank = MastersCopyofConfigFile.getInstance().getMasterConfigDetails().get(tailValueReceivedInNotifcation+"_bank");
            
            
            
    		String bankChain = (String) MastersCopyofConfigFile.getInstance().getMasterConfigDetails().get(newTailsBank+"_chain");
    		
            
    		StringBuffer newBankChain = new StringBuffer(bankChain);
    		if(bankChain.length()<=1){
    			newBankChain.append(tailValueReceivedInNotifcation.trim());
    		} else {
    			newBankChain.append(","+tailValueReceivedInNotifcation.trim());
    		}
    		
    		String newBankChainString = newBankChain.toString();
    		MastersCopyofConfigFile.getInstance().updateMasterHashMap(newTailsBank+"_chain", newBankChainString);
            
    		
    		String newChainToClient = (String) MastersCopyofConfigFile.getInstance().getMasterConfigDetails().get(newTailsBank+"_chain");
    		System.out.println("***********************NEW CHAIN IS ::::"+newChainToClient);
    		
    		 chainReplicationLogger.myLogger.log(Level.INFO, "["+newTailsBank + "]'s new chain is: ["+newChainToClient+"]");
    		
    		// Notify the clients, that the tail value has been updated
    		sendNotificationToClients(newTailsBank, newChainToClient);
    		
    		
    		
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendNotificationToClients(String sBank, String newBankChain){
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		// Run a loop to inform all clients via UDP
		
		/* First get all the port numbers where I have to send update
		* The idea here is to send the notification via UDP message to all the clients, be connected or not
		* In this way, we are assured that all the clients have received an update notification
		* */
		ArrayList<String> portNumbers = new ArrayList<String>();
		ArrayList<String> ipAddresses = new ArrayList<String>();
		Set<String> setOfKeys = masterHashMap.keySet();
		

		
		Iterator<String> itr = setOfKeys.iterator();
		while(itr.hasNext()){
			String currentKey = itr.next();

			if(currentKey.contains("listenPortFromMaster")){

				portNumbers.add((String) masterHashMap.get(currentKey));
				ipAddresses.add((String)masterHashMap.get(currentKey.split("_")));
			}
		}

		try {
			for(int i=0;i<portNumbers.size();i++){
				DatagramSocket sendToClientSocket = new DatagramSocket();
				InetAddress IPAddress = InetAddress.getByName(ipAddresses.get(i));
			    byte[] sendData = new byte[1024];
			    String sentence = "UPDATE_NOTIFICATION:"+sBank+":"+newBankChain;
			    sendData = sentence.getBytes();
			    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(portNumbers.get(i)));
			    sendToClientSocket.send(sendPacket);
			    sendToClientSocket.close();
			    System.out.println("SENT UPDATE NOTIFICATION to "+portNumbers.get(i));
			    chainReplicationLogger.myLogger.log(Level.INFO, "[M to Clients]"+" UPDATE_NOTIFICATION:"+sBank+":"+newBankChain);
			    
			}
			serverSocket.close();
		    Thread.currentThread().stop();
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
