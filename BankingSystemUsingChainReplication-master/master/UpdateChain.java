package master;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import common.Request;
import common.ServerReply;
import server.ServerObjectPassing;
import server.ServersCopyofConfigFile;


/* Class: UpdateChain
 * Purpose: Send update notifications to the servers and the clients.
 */
public class UpdateChain{
	String serverName = "";
	ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance("Master");
	
	public UpdateChain(String serverName){
		/* 
		 * Get the values of the successor and predecessor of the server.
		 */
		
		
		
		
		HashMap configFileDetails = MastersCopyofConfigFile.getInstance().getMasterConfigDetails(); 
				
		String sBank = (String) configFileDetails.get(serverName+"_bank");
		String bankChain = (String) configFileDetails.get(sBank+"_chain");
		
		
		
		
		String[] tokenizedBankChain = bankChain.split(",");
		boolean isHead = tokenizedBankChain[0].equals(serverName);
		boolean isTail = tokenizedBankChain[(tokenizedBankChain.length)- 1 ].equals(serverName);
		
		ServerObjectPassing serverObjectPassing = new ServerObjectPassing();
		String successor = serverObjectPassing.getSucessor(serverName,tokenizedBankChain);
		String predecessor = serverObjectPassing.getPredecessor(serverName,tokenizedBankChain);
		
		System.out.println("Server which was killed = "+serverName);
		System.out.println("Bank which was affected = "+sBank);
		System.out.println("Was the failed server HEAD? : "+ isHead);
		System.out.println("Was the failed server TAIL? : "+ isTail);
		
		chainReplicationLogger.myLogger.log(Level.INFO, "Crashed Server name = ["+serverName+"]");
		chainReplicationLogger.myLogger.log(Level.INFO, "Bank which was affected = ["+sBank+"]");
		chainReplicationLogger.myLogger.log(Level.INFO, "Was the failed server HEAD? : "+ isHead);
		chainReplicationLogger.myLogger.log(Level.INFO, "Was the failed server TAIL? : "+ isTail);
		
		
		// Chain Extension Failure Scenario 1
		// Master will detect that the old tail has crashed during Chain Extension
		// So, now take appropriate actions
		if(isTail && MasterProcess.chainExtensionInProgress){
			// Need to instruct Prospective New tail that you re-initiate the Chain Extension from your side.
			//System.out.println("PREPOS NEW TAIL === "+MasterProcess.prospectiveNewTail);
			//sendNotificationToProspectiveTail(MasterProcess.prospectiveNewTail);
		}
		
		/* Indicates that Tail has crashed */
		if(successor==null && predecessor!=null){
			 
			// Update the chain value in the Singleton HashMap
			updateChainValueInHashmap(sBank,bankChain, serverName);
			// Send update notification to the new tail, and clients
			String newBankChain = (String) configFileDetails.get(sBank+"_chain");
			String tail = serverObjectPassing.getTail(newBankChain.split(","));
			System.out.println("New Tail is = "+tail);
			sendNotificationToNewTail(tail);
			sendNotificationToClients(sBank, newBankChain);
		}
		/* Indicates that Head has crashed */
		else if(predecessor==null && successor != null){
			// Update the chain value in the Singleton HashMap
			updateChainValueInHashmap(sBank,bankChain, serverName);
			String newBankChain = (String) configFileDetails.get(sBank+"_chain");
			
			// Send update notification to the new head, and clients
			String head = serverObjectPassing.getHead(newBankChain.split(","));
			System.out.println("New Head is = "+head);
			sendNotificationToNewHead(head);
			sendNotificationToClients(sBank, newBankChain);
		}

		// Phase 4: Transfers error case 1: 
		// If the master detects that the server which has been dead belongs to the destination bank, 
		// then ask the Tail of Chain 1 to retransmit the transfer request. 
		if(MasterProcess.ongoingTransfer 
				&& (MasterProcess.destBankofOngoingTransfer.equalsIgnoreCase(sBank))){
			
			// This sleep is required so that the Chain 2 gets enough time to set up its new head
			try{
			Thread.sleep(15000);
			} catch(InterruptedException ie){}
			
			String srcBankChain = (String) configFileDetails.get(MasterProcess.srcBankofOngoingTransfer+"_chain");
			String srcBankChainTokens[] = srcBankChain.split(",");
			
			String tail = srcBankChainTokens[(srcBankChainTokens.length)-1];
			System.out.println("TTTTTTTTTTTTTTail of Source Bank is : "+tail);
			
			// Compute the value of Bank2's Head here only!
			String destBankChain = (String) configFileDetails.get(MasterProcess.destBankofOngoingTransfer+"_chain");
			String destBankChainTokens[] = destBankChain.split(",");
			
			String headBank2 = destBankChainTokens[0];
			System.out.println("HHH    headBank2: "+headBank2);
			
			sendNotificationToChain1Tail(tail, headBank2);
		}
				
		
		// Commented out certain cases of failure from consideration for Phase 4.
		/* Indicates that Intermediate Server has crashed*/
		/*else if(successor!=null && predecessor!=null){
			tokenizedBankChain = bankChain.split(",");
			String mySuccessorOfFailedS = serverObjectPassing.getSucessor(serverName, tokenizedBankChain);
			String myPredecessorOfFailedS = serverObjectPassing.getPredecessor(serverName, tokenizedBankChain);
			updateChainValueInHashmap(sBank,bankChain, serverName);
			//Following two lines are only sanity checks
			String newBankChain = (String) configFileDetails.get(sBank+"_chain");
			System.out.println("INTER: BANK CHAIN = "+newBankChain);
			sendNotificationToSPlusAndSMinus(mySuccessorOfFailedS, myPredecessorOfFailedS);
			
		}*/
		/* Indicates that the newly added tail has crashed before entering the chain */
		/*else if(successor==null && predecessor==null){
			// Only detect it, Master need not send updates to anyone 
			// as the existing chain is not disturbed. Only log it
			System.out.println("Found that the prospective new tail has crashed. Graceful Abort.");
			chainReplicationLogger.myLogger.log(Level.INFO, "Found that Prospective New Tail has crashed. Gracefully Aborted Chain Extension");
		}*/
	}
	
	public void updateChainValueInHashmap(String whichBank,String bankChain, String serverName) {
		String[] tokenizedBankChain = bankChain.split(",");
		StringBuffer newBankChain = new StringBuffer();
		for(int i=0;i<tokenizedBankChain.length;i++){
			if(!(tokenizedBankChain[i].equalsIgnoreCase(serverName))){
				newBankChain.append(tokenizedBankChain[i] + ",");
			}
		}
		
		String newBankChainString = newBankChain.toString();
		newBankChainString = newBankChainString.substring(0,newBankChainString.length()-1);
		MastersCopyofConfigFile.getInstance().updateMasterHashMap(whichBank+"_chain", newBankChainString);
		
	}
	
	public void sendNotificationToNewHead(String head){
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		String headListeningOn = (String) masterHashMap.get(head+"_listenFromMasterOn");
		String headIP = (String) masterHashMap.get(head+"_IP");
		try {
			Socket client = new Socket(headIP, Integer.parseInt(headListeningOn));
			OutputStream outToServer = client.getOutputStream();
	        DataOutputStream out = new DataOutputStream(outToServer);
	        out.writeUTF("YOU_ARE_NEW:HEAD");
	        chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+head+"(NewHead)] YOU_ARE_NEW:HEAD");
	        System.out.println("Master Wrote this to the New Head -> YOU_ARE_NEW:HEAD");
	        client.close();
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNotificationToNewTail(String tail){
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		String tailListeningOn = (String) masterHashMap.get(tail+"_listenFromMasterOn");
		String tailIP = (String) masterHashMap.get(tail+"_IP");
		try {
			Socket client = new Socket(tailIP, Integer.parseInt(tailListeningOn));
			OutputStream outToServer = client.getOutputStream();
	        DataOutputStream out = new DataOutputStream(outToServer);
	        out.writeUTF("YOU_ARE_NEW:TAIL");
	        chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+tail+"(NewTail)] YOU_ARE_NEW:TAIL");
	        System.out.println("Master Wrote this to the New Tail -> YOU_ARE_NEW:TAIL");
	        client.close();
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void sendNotificationToSPlusAndSMinus(String mySuccessorOfFailedS, String myPredecessorOfFailedS){
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		String mySuccessorOfFailedSListeningOn = (String) masterHashMap.get(mySuccessorOfFailedS+"_listenFromMasterOn");
		String mySuccessorOfFailedSIP = (String) masterHashMap.get(mySuccessorOfFailedS+"_IP");
		String myPredecessorOfFailedSListeningOn = (String) masterHashMap.get(myPredecessorOfFailedS+"_listenFromMasterOn");
		String myPredecessorOfFailedSIP = (String) masterHashMap.get(myPredecessorOfFailedS+"_IP");
		try {
			Socket client = new Socket(mySuccessorOfFailedSIP, Integer.parseInt(mySuccessorOfFailedSListeningOn));
			OutputStream outToServer = client.getOutputStream();
	        DataOutputStream out = new DataOutputStream(outToServer);
	        out.writeUTF("INTERMEDIATE S+:CHANGE_YOUR_PREDECESSOR:"+myPredecessorOfFailedS);
	        System.out.println("Master Wrote this to the S+ -> INTERMEDIATE:CHANGE_YOUR_PREDECESSOR"+myPredecessorOfFailedS);
	        chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+mySuccessorOfFailedS+" (S+)]:"+" Intermediate S+: CHANGE_YOUR_PREDECESSOR to: "+myPredecessorOfFailedS);
	        
	        DataInputStream in = new DataInputStream(client.getInputStream());
	        String latestSeqNo = in.readUTF();
	        System.out.println("Latest Sequence # Obtained from S+ is : "+latestSeqNo);
	        client.close();
	        
	        Socket client1 = new Socket(myPredecessorOfFailedSIP, Integer.parseInt(myPredecessorOfFailedSListeningOn));
			OutputStream outToServer1 = client1.getOutputStream();
	        DataOutputStream out1 = new DataOutputStream(outToServer1);
	        out1.writeUTF("INTERMEDIATE S-:CHANGE_YOUR_SUCCESSOR:"+mySuccessorOfFailedS+":"+latestSeqNo);
	        System.out.println("Master Wrote this to the S- -> INTERMEDIATE:CHANGE_YOUR_SUCCESSOR"+mySuccessorOfFailedS+":"+latestSeqNo);
	        chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+myPredecessorOfFailedS+" (S-)]:"+" "
	        		+ "Intermediate S-: CHANGE_YOUR_SUCCESSOR to: "+mySuccessorOfFailedS+". Additionally, "
	        				+ "send to S+ Items in your SENTOBJ greater than Sequence Number:["+latestSeqNo+"]");
	        
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
			if(currentKey.endsWith("listenPortFromMaster")){
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
			    chainReplicationLogger.myLogger.log(Level.INFO, "[M to Client]"+" UPDATE_NOTIFICATION:"+sBank+":"+newBankChain);
			}
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void sendNotificationToProspectiveTail(String prospectiveTail){
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		String headListeningOn = (String) masterHashMap.get(prospectiveTail+"_listenFromMasterOn");
		String headIP = (String) masterHashMap.get(prospectiveTail+"_IP");
		try {
			Socket client = new Socket(headIP, Integer.parseInt(headListeningOn));
			OutputStream outToServer = client.getOutputStream();
	        DataOutputStream out = new DataOutputStream(outToServer);
	        out.writeUTF("NEW_TAIL:REINITIATE_CHAIN_EXTENSION");
	        chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+prospectiveTail+"(Prospective New Tail)] Reinitiate Chain Extension");
	        System.out.println("Master Wrote this to the Prospective New TAIL => Reinitiate chain extension");
	        client.close();
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNotificationToChain1Tail(String chain1Tail, String headBank2){
		HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
		String tailListeningOn = (String) masterHashMap.get(chain1Tail+"_listenFromMasterOn");
		String tailIP = (String) masterHashMap.get(chain1Tail+"_IP");
		try {
			Socket client = new Socket(tailIP, Integer.parseInt(tailListeningOn));
			OutputStream outToServer = client.getOutputStream();
	        DataOutputStream out = new DataOutputStream(outToServer);
	        out.writeUTF("TAIL:RESEND_TRANSFER_REQUEST#"+headBank2+"#"+MasterProcess.reqIDofOngoingTransfer);
	        //chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+chain1Tail+"(Prospective New Tail)] Reinitiate Chain Extension");
	        System.out.println("Master Wrote this to the tail, retrasmit transfer request");
	        client.close();
        } catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
