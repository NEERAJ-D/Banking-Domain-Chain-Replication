package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import common.ClientReply;
import common.ServerReply;
/*
 * Class 		: 	ListenFromSucessorServer 
 * Purpose		: 	A thread class for listening the acknowledgement(s) from the successor servers 
 * Who uses this: 	All servers expect the tail server
 */
	public class ListenFromSuccessorServer extends Thread{
		String serverName;
		String listenPort;
		private ServerSocket serverSocket;

		public ListenFromSuccessorServer(String serverName, String listenPort) throws Exception{
			serverSocket = new ServerSocket(Integer.parseInt(listenPort));
			this.serverName = serverName;
			this.listenPort = listenPort;

		}
		
		public void run()
		   {
				//System.out.println("Started Listening from Successors");
		      while(true)
		      {
		    	  
		    	  ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.serverName);
		         try
		         {
		        	 
					ServerObjectPassing serverObjPassing = new ServerObjectPassing();
					
					
					//if(ServerProcess.mySuccessor!=null){
					
					
		        	ServerObjectPassing serverObjectPassing = new ServerObjectPassing(); 
		            Socket server = serverSocket.accept();
		            DataInputStream in = new DataInputStream(server.getInputStream());
		            
		            String ackRequestID = in.readUTF();
		            System.out.println("Received Ack From Successor with ID::: "+ackRequestID);
		            server.close();
		            
		            if(ServerProcess.transferInProgress){
						if(ServerProcess.transferInProgressWithReqID.equalsIgnoreCase(ackRequestID)){
							ServerProcess.transferInProgress = false;
						}
					}
					
		            
		            ServerProcess.receiveSequenceNumber++;
		            chainReplicationLogger.myLogger.log(Level.INFO, "[Receive Seq# = "+ServerProcess.getReceiveSequenceNumber()+"] Received Acknowledgement with "+ackRequestID);
		            
		            SentObjClass.getInstance().removeFromSentObj(ackRequestID,chainReplicationLogger);
	            	chainReplicationLogger.myLogger.log(Level.INFO, "Removed [RequestID = " +ackRequestID+"] from SentObjClass");
		            
		            //Now depending on whether it is an intermediate server or the tail, send to appropriate destination
		            if(ServerProcess.myPredecessor!=null){
		            	serverObjectPassing.sendToPredecessor(ServerProcess.myPredecessor, ackRequestID);
		            	ServerProcess.sendSequenceNumber++;
		            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Acknowledgement of [Request =  "+ackRequestID+"] Sent to ["+ServerProcess.myPredecessor+"]");
		            	System.out.println("Sent ACK to Predecessor "+ServerProcess.myPredecessor);
		            }
	            		
					//}
		         } catch(SocketTimeoutException s){
		            System.out.println("Server Socket timed out!");
		            break;
		         } catch(IOException e) {
		            e.printStackTrace();
		            break;
		         }
		      }
		      Runtime.getRuntime().freeMemory();
		      
		   }		
}

	