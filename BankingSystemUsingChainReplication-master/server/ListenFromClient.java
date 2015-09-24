package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.ChainReplicationLogFormatter;
import common.ChainReplicationLogger;
import common.ClientReply;
import common.ServerReply;
import common.Request;

/*
 * Class 		: 	ListenFromClient
 * Purpose		: 	A thread class for listening the requests from the client 
 * Who uses this: 	Head, when it receives requests from the client
 * 					Tail, when it receives requests from the client
 */
public class ListenFromClient extends Thread{
	String serverName;
	String listenPort; 
	
	public ListenFromClient(String serverName, String listenPort) throws Exception{	
		this.serverName = serverName;
		this.listenPort = listenPort;
	}
	
	public  void run(){
		byte[] receiveData;
		byte[] sendData;
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.serverName);
		//System.out.println("Started Listening from Clients");
		while(true)
		{
		
			ServerReply serverReply = null;
			DatagramSocket serverSocket = null;
			
			try {
				
			receiveData = new byte[1024];
			sendData = new byte[1024];
			serverSocket = new DatagramSocket(Integer.parseInt(this.listenPort));
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			byte[] receivedDataAsObject = receivePacket.getData();
			//System.out.println("Received Client Request from "+receivePacket.getPort());
			ByteArrayInputStream in = new ByteArrayInputStream(receivedDataAsObject);
            ObjectInputStream is = new ObjectInputStream(in);
            Request r = null;
            r = (Request) is.readObject();
            serverSocket.close();
            if(!(r.getOperation().equalsIgnoreCase("getBalance"))){
            	ServerProcess.reqSequenceNumber++;	
            }
            System.out.println("Req Seq No = "+ServerProcess.reqSequenceNumber + " and REQID is "+r.getRequestID());
    		ServerProcess.receiveSequenceNumber++;
    		chainReplicationLogger.myLogger.log(Level.INFO, "[Receive Seq# = "+ServerProcess.getReceiveSequenceNumber()+"] [From CLIENT] Received Request with "+r);
    		
    		serverReply = Bank.getInstance().processRequests(serverName,r);
            
    		if(ServerProcess.mySuccessor!=null){
	            // send the reply to the successor
    			ServerObjectPassing serverObjectPassing = new ServerObjectPassing();
            	serverObjectPassing.sendToSuccessor(ServerProcess.mySuccessor, serverReply, chainReplicationLogger);
            	HistObj.getInstance().addToHistObj(r, serverReply);
            	SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber, serverReply);
            	ServerProcess.sendSequenceNumber++;
            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Sent to ["+ServerProcess.mySuccessor + "] "+serverReply);
            	System.out.println("Sent processed request "+serverReply.getRequestID()+" to "+ServerProcess.mySuccessor);
            } else {
            	ClientReply clientReply = new ClientReply(
            			serverReply.getRequestID(),
            			serverReply.getOutcome(),
            			serverReply.getAccountNumber(),
            			serverReply.getBalance());
            	ServerProcess.sendSequenceNumber++;
            	ServerObjectPassing serverObjectPassing = new ServerObjectPassing();
            	serverObjectPassing.sendToClient(serverName,clientReply);
            	System.out.println("Reply Sent to Client");       	
            	HistObj.getInstance().addToHistObj(r,serverReply);
            	// Sending ack to predecessor is not required in this case, because, this is only for query operations
            	//serverObjectPassing.sendToPredecessor(clientName, serverReply.getRequestID());
            	//chainReplicationLogger.myLogger.log(Level.INFO, "Started AcknowSent to ["+successor + "] "+serverReply);
            }
            
            } catch (NumberFormatException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				
			}
			
		}
	}

}
