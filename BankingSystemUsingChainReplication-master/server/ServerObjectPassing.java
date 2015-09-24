package server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import client.NetworkDetails;
import common.ChainReplicationLogger;
import common.ClientReply;
import common.ServerReply;

/*
 * Class 		: 	ServerObjectPassing
 * Purpose		: 	For sending messages within servers.  
 */
public class ServerObjectPassing {
	
	
	public ServerObjectPassing(){
		
	}

	/*
	 * Given a Bank Chain, find the head server
	 */
	public String getHead(String[] tokenizedServerChain){
		return tokenizedServerChain[0];
	}
	

	/*
	 * Given a Bank Chain, find the Tail server
	 */
	public String getTail(String[] tokenizedServerChain){
		return tokenizedServerChain[(tokenizedServerChain.length)-1];
	}
	
	/*
	 * Given a server, find the predecessor of the server
	 */
	public String getPredecessor(String serverName, String[] tokenizedBankChain){
		String predecessor = null ;
		for(int i=0;i<tokenizedBankChain.length;i++){
			if(serverName.equalsIgnoreCase(tokenizedBankChain[i])){
				if(i==0){
					predecessor = null;
				}else{
					predecessor = tokenizedBankChain[i-1];
				}
			}
		}
		return predecessor;
	}

	/*
	 * Given a server, find the successor of the server
	 */
	public String getSucessor(String serverName, String[] tokenizedBankChain){
		String successor = null ;
		for(int i=0;i<tokenizedBankChain.length;i++){
			if(serverName.equalsIgnoreCase(tokenizedBankChain[i])){
				if(i==tokenizedBankChain.length-1){
					successor = null;
				}else{
					successor = tokenizedBankChain[i+1];
				}
			}
		}
		return successor;
	}
	
	
	/*
	 * sendToPredecessor is used for sending the acknowledgement to the 
	 * predecessor servers, so that they can remove those requests from their 
	 * SentObjClass.
	 */
	public void sendToPredecessor(String predecessor, String ackRequestID) {
		ConcurrentHashMap predecessorHashMap = ServersCopyofConfigFile.getInstance().getServerConfigDetails(); 
				
		try {
			// Following line essentially says that, get the value of the port of the predecessor
			// which it uses for listening from its Successor servers for acknowledgement messages
		String predecessorListenPort = (String) predecessorHashMap.get(predecessor.trim()+"_listenFromSuccessorServersOn");
		String predecessorIP = (String) predecessorHashMap.get(predecessor.trim()+"_IP");
		
		Socket client = new Socket(predecessorIP, Integer.parseInt(predecessorListenPort));
		
		byte[] dataToSend = new byte[1024];
		OutputStream outToServer = client.getOutputStream();
		DataOutputStream out =new DataOutputStream(outToServer);
		out.writeUTF(ackRequestID);
		//System.out.println("I WROTE THE acknowledgement to the pred...");
        client.close();
        } catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	/*
	 * sendToSuccessor is used for sending the ServerReply Object to the successor server
	 */
	public void sendToSuccessor(String successor, ServerReply reply, ChainReplicationLogger chainReplicationLogger) {
		// TODO Auto-generated method stub
		ConcurrentHashMap successorHashMap = ServersCopyofConfigFile.getInstance().getServerConfigDetails(); 
			
		try {
		
		// Following line essentially says that, get the value of the port of the successor
		// which it uses for listening from its Predecessor servers for sending the ServerReply objects
			
			
			
		String successorListenPortsSendToPortNo = (String) successorHashMap.get(successor.trim()+"_listenFromPredecessorServersOn");
		String successorIP = (String) successorHashMap.get(successor.trim()+"_IP");
		Socket client = new Socket(successorIP, Integer.parseInt(successorListenPortsSendToPortNo));
		
		byte[] dataToSend = new byte[1024];
		OutputStream outToServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(reply);
        dataToSend = outputStream.toByteArray();
        
        out.write(dataToSend, 0, dataToSend.length);

        client.close();
        } catch(ConnectException ce){
        	System.out.println("***************************************************");
        	System.out.println("The successor was unreachable, and current server has "
        			+ "not yet notified that successor was dead. So this "
        			+ "request will not be forwarded to the succcessor");
        	
        	chainReplicationLogger.myLogger.log(Level.SEVERE, "The successor was unreachable, and current server has "
        			+ "not yet notified that successor was dead. So this "
        			+ "request will not be forwarded to the succcessor");
        	
        } 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Get IP address and port number of the client to which the response should be sent.
	 */
	public NetworkDetails getClientNetworkDetails(String clientName){
		ConcurrentHashMap clientHashMap = ServersCopyofConfigFile.getInstance().getServerConfigDetails(); 
				
		
		String cListenIP = (String) clientHashMap.get(clientName+"_IP");
		String cListenPort = (String) clientHashMap.get(clientName+"_listenPort");
		NetworkDetails clientDetails = new NetworkDetails(cListenIP, cListenPort,clientName);
		return clientDetails;
	}
	
	/*
	 * Send response back to the client
	 */
	public void sendToClient(String serverName, ClientReply clientReply){
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(serverName);
		
		//Find to whom tail needs to send response to
		String clientName = clientReply.getRequestID().split("\\.")[1];
		NetworkDetails clientDetails = getClientNetworkDetails(clientName);
		
		DatagramSocket sendToClientSocket;
		try {
		sendToClientSocket = new DatagramSocket();
		byte[] dataToSend = new byte[1024];
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(clientReply);
        dataToSend = outputStream.toByteArray();
        DatagramPacket sendPacket=null;

			sendPacket = new DatagramPacket(
					dataToSend, dataToSend.length,
					clientDetails.getIpAddress(),
					clientDetails.getPort());
			
        sendToClientSocket.send(sendPacket);
        
        chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Sent to Client "+clientReply);
        sendToClientSocket.close();
        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * sendToMaster is used for sending the notification to Master
	 */
	public void sendToMaster(String serverName, String iAmNewTail, ChainReplicationLogger chainReplicationLogger) {
		
		ConcurrentHashMap servMasterHashMap = ServersCopyofConfigFile.getInstance().getServerConfigDetails(); 
		try {
		
			// Following line essentially says that, get the value of the port of the successor
			// which it uses for listening from its Predecessor servers for sending the ServerReply objects
		String m_serverNotificationListenPort = (String) servMasterHashMap.get("m_serverNotificationListenPort");
		String masterIP = (String) servMasterHashMap.get("m_IP");

		Socket client = new Socket(masterIP, Integer.parseInt(m_serverNotificationListenPort));
		OutputStream outToServer = client.getOutputStream();
        DataOutputStream out = new DataOutputStream(outToServer);
        out.writeUTF(iAmNewTail);
        System.out.println("I WROTE DATA TO MASTER "+iAmNewTail);
        chainReplicationLogger.myLogger.log(Level.INFO, "Sent to Master: ["+serverName + ":MASTER_I_AM_NEW_TAIL"+"]");
    	chainReplicationLogger.myLogger.log(Level.INFO, "Started the role of Tail");
        
        client.close();
        
		} catch(ConnectException ce){
			ce.printStackTrace();			
        } 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	/*
	 * Send the transfer request to Bank2
	 */
	public String sendToBank2(String destinationBank,ChainReplicationLogger chainReplicationLogger) {
		String destBankHeadValue = "";
		ConcurrentHashMap servMasterHashMap = ServersCopyofConfigFile.getInstance().getServerConfigDetails(); 
		try {
		String m_serverNotificationListenPort = (String) servMasterHashMap.get("m_heartBeatListenPort");
		String masterIP = (String) servMasterHashMap.get("m_IP");

		// First, ask the Master what is the name of the server that is at the head of Bank 2 	
		DatagramSocket sendToMaster = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(masterIP);
        DatagramPacket sendPacket = new DatagramPacket(destinationBank.getBytes(), 
        		destinationBank.getBytes().length, 
	    		IPAddress, Integer.parseInt(m_serverNotificationListenPort));
	    sendToMaster.send(sendPacket);
	    //System.out.println("I Sent the destBank to Master "+destinationBank);
	    chainReplicationLogger.myLogger.log(Level.INFO, "Sent to Master: What is the Head of ["+destinationBank.split("#")[1]+"]");
	    byte[] receiveData = new byte[1024];
	    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    sendToMaster.receive(receivePacket);
	    destBankHeadValue = new String(receivePacket.getData());
	    destBankHeadValue = destBankHeadValue.trim(); 
	    //System.out.println("FROM MASTER DEST BANK HEAD VALUE:" + destBankHeadValue);
	    chainReplicationLogger.myLogger.log(Level.INFO, "Received from Master: Head of ["+destinationBank.split("#")[1]+"] is : ["+destBankHeadValue+"]");
	    sendToMaster.close();
        
        
		} catch(ConnectException ce){
			ce.printStackTrace();			
        } 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return destBankHeadValue;
	}


	/*
	 * Send acknowledgement to Bank1
	 */
	public String sendToBank1(String srcBank,
			ChainReplicationLogger chainReplicationLogger) {
		String srcBankTailValue = "";
		ConcurrentHashMap servMasterHashMap = ServersCopyofConfigFile.getInstance().getServerConfigDetails(); 
		try {
		String m_serverNotificationListenPort = (String) servMasterHashMap.get("m_heartBeatListenPort");
		String masterIP = (String) servMasterHashMap.get("m_IP");

		// First, ask the Master what is the name of the server that is at the head of Bank 2 	
		DatagramSocket sendToMaster = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(masterIP);
        DatagramPacket sendPacket = new DatagramPacket(srcBank.getBytes(), 
        		srcBank.getBytes().length, 
	    		IPAddress, Integer.parseInt(m_serverNotificationListenPort));
	    sendToMaster.send(sendPacket);
	    //System.out.println("I Sent the srcBank to Master "+srcBank);
	    chainReplicationLogger.myLogger.log(Level.INFO, "Sent to Master: What is the Tail of ["+srcBank.split("#")[1]+"]");
	    byte[] receiveData = new byte[1024];
	    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    sendToMaster.receive(receivePacket);
	    srcBankTailValue = new String(receivePacket.getData());
	    srcBankTailValue = srcBankTailValue.trim();
	    //System.out.println("FROM MASTER SRC BANK Tail VALUE:" + srcBankTailValue);
	    sendToMaster.close();
	    chainReplicationLogger.myLogger.log(Level.INFO, "Received from Master: The Tail of ["+srcBank.split("#")[1]+"] is : ["+srcBankTailValue+"]");
        
		} catch(ConnectException ce){
			ce.printStackTrace();			
        } 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return srcBankTailValue;
	}
	
	
}
