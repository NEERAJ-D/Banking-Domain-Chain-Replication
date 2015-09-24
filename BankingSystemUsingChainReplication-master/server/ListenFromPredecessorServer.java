package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import common.ClientReply;
import common.Request;
import common.ServerReply;
/*
 * Class 		: 	ListenFromPredecessorServer 
 * Purpose		: 	A thread class for listening the requests from the predecessor servers 
 * Who uses this: 	All servers expect the head server
 */
	public class ListenFromPredecessorServer extends Thread{
		String serverName;
		String listenPort; 
		private ServerSocket serverSocket;
		
		public ListenFromPredecessorServer(String serverName, String listenPort) throws Exception{
			serverSocket = new ServerSocket(Integer.parseInt(listenPort));
			this.serverName = serverName;
			this.listenPort = listenPort;

		}
		
		public void run()
		   {
			//System.out.println("Started Listening from Predecessors");
			ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.serverName);
		      while(true)
		      {
		    	  
		         try
		         {
					 	
		        	if(ServerProcess.myPredecessor != null){
		        		
			        	ServerObjectPassing serverObjectPassing = new ServerObjectPassing(); 
			            Socket server = serverSocket.accept();
			           
			            ObjectInputStream inStream = new ObjectInputStream(server.getInputStream());
			            
			            
			            Object obj = inStream.readObject();
			            if(obj instanceof ServerReply){
			            	ServerReply serverReply = (ServerReply) obj; 
				            //System.out.println("Received Request from Predecessor");
				            
				            if(!(serverReply.getRequest().getOperation().equalsIgnoreCase("getBalance"))){
				            	ServerProcess.reqSequenceNumber++;
				            }
				            
				            //System.out.println("In PRED = "+ServerProcess.reqSequenceNumber);
				            ServerProcess.receiveSequenceNumber++;
				            chainReplicationLogger.myLogger.log(Level.INFO, "[Receive Seq# = "+ServerProcess.getReceiveSequenceNumber()+"] Received Request with "+serverReply);
		
				            // Using this ServerReply object do operations on this server.
				            Bank.getInstance().processRequests(serverName,serverReply.getRequest());
//				            System.out.println("HHHH mysucc = "+ServerProcess.mySuccessor );
//				            System.out.println("HHHH tranfer in prog = "+ServerProcess.transferInProgress);
//				            System.out.println("HHHH serverReply.getRequest().getDestBank().trim() = "+serverReply.getRequest().getDestBank().trim());
//				            System.out.println("HHHH ServerProcess.iBelongToBank = "+ServerProcess.iBelongToBank );
//				            System.out.println("HHHH serverReply.getRequestID() = "+serverReply.getRequestID());
//				            System.out.println("HHHH ServerProcess.transferInProgressWithReqID = "+ServerProcess.transferInProgressWithReqID);
				            
				            // In case of Chain 1 : Successor is null, Transfer is in Progress, and I am 
				            // not the destination bank, then send to Chain 2 head
				            if((ServerProcess.mySuccessor == null) && (ServerProcess.transferInProgress) 
				            		&& !(ServerProcess.iBelongToBank.equalsIgnoreCase(
				            				serverReply.getRequest().getDestBank().trim())) 
				            				&& (serverReply.getRequestID().equalsIgnoreCase(ServerProcess.transferInProgressWithReqID))){
				            	String destBankHeadValue = serverObjectPassing.sendToBank2(
				            			"WHAT_IS_HEAD_OF#"+serverReply.getRequest().getDestBank()+"#"+
				            					serverReply.getRequestID()+"#"+serverReply.getSrcBank()+":"+this.serverName,chainReplicationLogger);
				            	
			            		ServerProcess.mySuccessor = destBankHeadValue;
			            		// System.out.println("THE VALUEE OF MY SUCC = "+ServerProcess.mySuccessor );
			            		// Now send the ServerReply object to the successor 
			            		serverObjectPassing.sendToSuccessor(ServerProcess.mySuccessor, serverReply,chainReplicationLogger);
			            		chainReplicationLogger.myLogger.log(Level.INFO, "Sent Transfer Request to Destination Bank ["+serverReply.getRequest().getDestBank().trim()+"] with Head ["+destBankHeadValue+"]");
			            		// Reset the value of the successor for the tail of bank 1 to null, 
				            	// So that it again acts  like the tail
				            	String temp = ServerProcess.mySuccessor ; 
			            		ServerProcess.mySuccessor = null;
				            	
				            	// Phase 4 : Failure Scenario 2
				            	// The sending tail of Chain 1 will die immediately after it sends the transfer request
				            	
				            	String whenToFailInChainExtensionAfterSending = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
				            			serverName+"_whenToFailInChainExtensionAfterSending");
				            	if(whenToFailInChainExtensionAfterSending.equalsIgnoreCase("50")){
				            		ServerProcess.transferFailureCase2 = true;
				            		System.out.println("I sent transfer request, and now gonna die");
									chainReplicationLogger.myLogger.log(Level.INFO, "I sent transfer request to Destination Bank. "
											+ "I decide to crash during this process");
									//ServerProcess.sendSequenceNumber=1000;
									System.out.println("Killing Myself for Failure Case 2");
									chainReplicationLogger.myLogger.log(Level.INFO, "Initiate Killing MySelf for Transfers Failure Case 2.");
									Runtime.getRuntime().exec("kill -9 "+ ServerProcess.processID);
				            		
				            	}
								
			            		ServerProcess.sendSequenceNumber++;
				            	SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber,serverReply);
				            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Sent to ["+temp + "] "+serverReply);
				            	HistObj.getInstance().addToHistObj(serverReply.getRequest(), serverReply);
				            	
				            }
				            
				            
				         // In case of Chain 2 : Successor is null, Transfer is in Progress, and I am 
				            // the destination bank, then send response back to Tail of Chain 1
				            if((ServerProcess.mySuccessor == null) && (ServerProcess.transferInProgress) 
				            		&& (ServerProcess.iBelongToBank.equalsIgnoreCase(
				            				serverReply.getRequest().getDestBank().trim()))){
				            	
				            	//*******SEND REPLY BACK TO TAIL1
				            	// Ask Master who is the tail of Chain 1, and send the acknowledgement to it
				            	// Inform master that the transfer operation is complete
				            	// System.out.println("NOW I NEED TO DO THREE OPERATIONS, REPEAT THESE DOWN");
				            	
				            	// Three actions which need to be performed
				            	// 1. Send response to the original client - This will be taken care by block following this one
				            	// 2. Send Reply back to the tail of Chain 1
				            	// 3. Send notification to the Master, stating that the transfer operation now stands as complete. 
				            	
				            	String requestIDReplace = serverReply.getRequestID();
				            	requestIDReplace = requestIDReplace.replace(".", "@");
				            	String srcBank = requestIDReplace.split("@")[0];
				            	
				            	
				            	
				            	// Ask from Master what is the value of Tail server of Chain 1
				            	// Also, hit two birds, in a single shot. When the master 
				            	String srcBankTailValue = serverObjectPassing.sendToBank1(
				            			"WHAT_IS_TAIL_OF#"+srcBank+"#"+
				            					serverReply.getRequestID()+":"+this.serverName,chainReplicationLogger);
				            	
			            		// This line will not be required. I can send any arbitary predecessor directly
				            	// ServerProcess.myPredecessor = srcBankTailValue;
			            		
				            	serverObjectPassing.sendToPredecessor(srcBankTailValue, serverReply.getRequestID());
				            	
				            	chainReplicationLogger.myLogger.log(Level.INFO, "Sent Acknowledgement to Source Bank ["+srcBank+"] with Tail ["+srcBankTailValue+"]");
				            	
				            	ServerProcess.transferInProgress = false;
				            	
				               	
				            }
				            
				            
				            
				            //Now depending on whether it is an intermediate server or the tail, send to appropriate destination
				            if(ServerProcess.mySuccessor!=null){
				            	serverObjectPassing.sendToSuccessor(ServerProcess.mySuccessor, serverReply,chainReplicationLogger);
				            	ServerProcess.sendSequenceNumber++;
				            	SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber,serverReply);
				            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Sent to ["+ServerProcess.mySuccessor + "] "+serverReply);
				            	HistObj.getInstance().addToHistObj(serverReply.getRequest(), serverReply);
				            } 
				            // Code block for the tail server
				            else{
				            	// Chain Extension module's code is in the start
				            	if(ServerProcess.tailToStoreUpdates){
				            		//If this variable is true, then we start saving to Sent Object, 
				            		// and at the same time response to the clients, and also send the acks.
				            		// So, effectively, for only this server, the sent obj will be more, and will be 
				            		// less for others. Send this suffix to the new tail.
				            		SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber,serverReply);
				            		//System.out.println("I Started storing the sent Objs to me .. Tail");
				            		chainReplicationLogger.myLogger.log(Level.INFO, "Storing SENTOBJs");
				            	}
				            	if(ServerProcess.transferInProgress){
				            		// Instead of sending reply to client, send the request to other head
				            		// Ask the master about where to send the data and wait for master to revert back
				            		// Send to the new head the request object. Let the destination bank chain figure out
				            		// what it needs to do.
				            	} else {
					            	ClientReply clientReply = new ClientReply(
					            			serverReply.getRequestID(),		            			
					            			serverReply.getOutcome(),
					            			serverReply.getAccountNumber(),
					            			serverReply.getBalance());
					            	ServerProcess.sendSequenceNumber++;
					            	serverObjectPassing.sendToClient(serverName,clientReply);
					            	HistObj.getInstance().addToHistObj(serverReply.getRequest(), serverReply);
					            	System.out.println("Reply Sent to Client");
					            	
					            	// Once sending is done, then the tail need not store the updates
					            	ServerProcess.tailToStoreUpdates = false;
					            	
					            	/*
					            	 * Following lines mark the beginning of the Acknowledgement Chain
					            	 */
					            	serverObjectPassing.sendToPredecessor(ServerProcess.myPredecessor, serverReply.getRequestID());
					            	ServerProcess.sendSequenceNumber++;
					            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Acknowledgement of [Request =  "+serverReply.getRequestID()+"] Sent to ["+ServerProcess.myPredecessor+"]");
					            	System.out.println("Acknowledgement Process Started");
				            	}
				            }
				            server.close();
			        	}
			            /*
			             * This is the case when S- is sending updates to S+
			             */
			            // Commented this case for Phase 4
			            /*else if (obj instanceof ConcurrentHashMap){
			            	ConcurrentHashMap<Integer, ServerReply> sentObjReceived 
			            	= (ConcurrentHashMap<Integer, ServerReply>) obj; 
				            System.out.println("Received SENT OBJ (Objects) of Size ["+sentObjReceived.size()+"] from Predecessor");
				            
				            ServerProcess.receiveSequenceNumber++;
				            chainReplicationLogger.myLogger.log(Level.INFO, "[Receive Seq# = "+ServerProcess.getReceiveSequenceNumber()+"] Received entire SENTOBJ SENT OBJ (Objects) of Size ["+sentObjReceived.size()+"] from S-");
		
				            // Received updates from S-, now I will die
							// For Failure Scenario 2, the S+ should fail here
				            String dieDuringIntermediateCrash = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
			            			serverName+"_dieDuringIntermediateCrash");
							
							if(Integer.parseInt(dieDuringIntermediateCrash)>1){
								System.out.println("I received updates, and now gonna die");
								
								chainReplicationLogger.myLogger.log(Level.INFO, "I received SENTOBJ from S-. "
										+ "I decide to crash during this process");
								
								ServerProcess.sendSequenceNumber=1000;
								
								Thread.sleep(2000);
							}
				            
				            
				            for(Map.Entry<Integer, ServerReply> entry : sentObjReceived.entrySet()){
								
				            	ServerReply serverReply = entry.getValue();
				             	
							    // Using this ServerReply object do operations on this server.
				            	Bank.getInstance().processRequests(serverName,serverReply.getRequest());
				            	chainReplicationLogger.myLogger.log(Level.INFO, "**** Processed Request received from S-. RequestID was: "+serverReply.getRequestID());
					            //Now depending on whether it is an intermediate server or the tail, send to appropriate destination
					            if(ServerProcess.mySuccessor!=null){
					            	serverObjectPassing.sendToSuccessor(ServerProcess.mySuccessor, serverReply,chainReplicationLogger);
					            	ServerProcess.sendSequenceNumber++;
					            	SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber,serverReply);
					            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Sent to ["+ServerProcess.mySuccessor + "] "+serverReply);
					            	HistObj.getInstance().addToHistObj(serverReply.getRequest(), serverReply);
					            } else{
					            	// Chain Extension module's code is in the start
					            	if(ServerProcess.tailToStoreUpdates){
					            		//If this variable is true, then we start saving to Sent Object, 
					            		// and at the same time response to the clients, and also send the acks.
					            		// So, effectively, for only this server, the sent obj will be more, and will be 
					            		// less for others. Send this suffix to the new tail.
					            		SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber,serverReply);
					            		System.out.println("I Started storing the sent Objs to me .. Tail");
					            		chainReplicationLogger.myLogger.log(Level.INFO, "Storing SENTOBJs");
					            	}
					            	
					            	ClientReply clientReply = new ClientReply(
					            			serverReply.getRequestID(),		            			
					            			serverReply.getOutcome(),
					            			serverReply.getAccountNumber(),
					            			serverReply.getBalance());
					            	ServerProcess.sendSequenceNumber++;
					            	serverObjectPassing.sendToClient(serverName,clientReply);
					            	HistObj.getInstance().addToHistObj(serverReply.getRequest(), serverReply);
					            	System.out.println("Reply Sent to Client");
					            	
					            	serverObjectPassing.sendToPredecessor(ServerProcess.myPredecessor, serverReply.getRequestID());
					            	ServerProcess.sendSequenceNumber++;
					            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Acknowledgement of [Request =  "+serverReply.getRequestID()+"] Sent to ["+ServerProcess.myPredecessor+"]");
					            	System.out.println("Acknowledgement Process Started");
				            }
				            server.close();
			            }
			            }*/
		        	}
		        	/* 
		        	 * This is a special case when a new tail is being added
		        	 */
		        	/* The code for adding new tail to the chain has been commented in Phase 4
		        	 * This is done in interest of conflicting conditions 
		        	else if(ServerProcess.myPredecessor == null && ServerProcess.mySuccessor == null){
		        		String oldTail = null;
		        		Socket server = serverSocket.accept();
			            ObjectInputStream inStream = new ObjectInputStream(server.getInputStream());
			            Object obj = inStream.readObject();
			            ServerProcess.receiveSequenceNumber++;
			            //System.out.println("THIS OBJECT IS AN INSTANCE OF : "+obj.getClass() + 
			            //		"Have received this from "+server.getPort());
			            
			            if(obj instanceof ArrayList<?>  ||
			            		obj instanceof ConcurrentHashMap<?, ?> ||
			            		obj instanceof String){
			            
			           
			            if(obj instanceof ArrayList<?> ){
			            	ArrayList<ContainerRequestServerReply> crs = (ArrayList<ContainerRequestServerReply>) obj;
			            	chainReplicationLogger.myLogger.log(Level.INFO, "Started Receiving Hist Obj piece by piece");
			            	for(int i=0;i<crs.size();i++){
			            		ContainerRequestServerReply c =  crs.get(i);
			            		HistObj.getInstance().addToHistObj(c.getRequest(), c.getServerReply());
			            	}
			            	System.out.println("Done with replicating Piece of Size [" + crs.size()+"] of the History Object on my side");
			            	chainReplicationLogger.myLogger.log(Level.INFO, "Done with replicating Piece of Size [" + crs.size()+"] of the History Object on my side");		
			            } else if(obj instanceof ConcurrentHashMap<?, ?>) {
			            	ConcurrentHashMap<Integer, ServerReply> sentObj = 
				            		(ConcurrentHashMap<Integer,ServerReply>) obj;
			            	
			            	// Process the updates that are received here. 
			            	
			            	
			            	Collection<ServerReply> serverReplyObjsToBeProcessed =  sentObj.values();
			            	System.out.println("VALUES IN SENT = "+serverReplyObjsToBeProcessed.size());
			            	
			            	System.out.println("I have received the Sent Object");
			            	chainReplicationLogger.myLogger.log(Level.INFO, "Received SENT OBJ with Size = "+serverReplyObjsToBeProcessed.size());
			            	
			            	
			            	for(ServerReply s: serverReplyObjsToBeProcessed){
			            		System.out.println("I am processing the following req id  "+s.getRequestID());
			            		Bank.getInstance().processRequests(serverName,s.getRequest());
			            		chainReplicationLogger.myLogger.log(Level.INFO, "Processed Request received from old (current) Tail. RequestID was: "+s.getRequestID());
			            			
			            	}
			            	  
			            } else if(obj instanceof String){
			            	
			            	String markerMessage = String.valueOf(obj);
			            	oldTail = markerMessage.split(":")[0];
			            	System.out.println("MESSAGE IS == "+markerMessage);
			            	
			            	// Close this connection only when the last message is received
			            	server.close();

			            	chainReplicationLogger.myLogger.log(Level.INFO, "Received ["+markerMessage+"] Marker message from old (current) Tail");
			            	
			            	// Send notification to master that I am the new tail
			            	ServerObjectPassing serverObjectPassing = new ServerObjectPassing();
			            	serverObjectPassing.sendToMaster(serverName, serverName + ":MASTER_I_AM_NEW_TAIL",chainReplicationLogger);
			            	
			            	ServerProcess.myPredecessor = oldTail; 
			            	ServerProcess.mySuccessor = null;
			            }
			            
			       }
			            
		        	}// End else if block
		        	*/
		        	
		        	
		        	/*
		        	 * Case of Phase 4: When sending message to the Head of Bank 2, 
		        	 * its Predecessor value will be null, and yet it has to receive the 
		        	 * message that is sent by the tail of Bank 1
		        	 * This case is executed only for Bank Chain 2 
		        	 */
		        	else if(ServerProcess.myPredecessor == null){
		        		
		        		ServerProcess.transferInProgress = true;
		        		Socket server = serverSocket.accept();
				        ObjectInputStream inStream = new ObjectInputStream(server.getInputStream());
			            
			            Object obj = inStream.readObject();
			            if(obj instanceof ServerReply){
			            	ServerReply serverReply = (ServerReply) obj; 
				            System.out.println("Received Request from Bank 1");
				            chainReplicationLogger.myLogger.log(Level.INFO, "I received transfer request from Source Bank with Request ID: ["+serverReply.getRequestID()+"]");
				            // Phase 4, Error scenario 1: Crash the Chain 2's Head here
				            String dieDuringIntermediateCrash = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
			            			serverName+"_dieDuringIntermediateCrash");
							
							if(Integer.parseInt(dieDuringIntermediateCrash)>1){
								ServerProcess.transferFailureCase1 = true;	
								System.out.println("I received transfer request, and now gonna die");
								chainReplicationLogger.myLogger.log(Level.INFO, "I received transfer request from Source Bank. "
										+ "I decide to crash during this process");
								//ServerProcess.sendSequenceNumber=1000;
								System.out.println("Killing Myself for Failure Case 1");
								chainReplicationLogger.myLogger.log(Level.INFO, "Initiate Killing MySelf for Transfers Failure Case 1");
								Runtime.getRuntime().exec("kill -9 "+ ServerProcess.processID);
							}
				            
				            
				            if(!(serverReply.getRequest().getOperation().equalsIgnoreCase("getBalance"))){
				            	ServerProcess.reqSequenceNumber++;
				            }
				            
				            ServerProcess.receiveSequenceNumber++;
				            //chainReplicationLogger.myLogger.log(Level.INFO, "[Receive Seq# = "+ServerProcess.getReceiveSequenceNumber()+"] Received Request with "+serverReply);
		
				            // Using this ServerReply object do operations on this server.
				            
				            
				            Bank.getInstance().processRequests(serverName,serverReply.getRequest());
				            
				            //Now depending on whether it is an intermediate server or the tail, send to appropriate destination
				            if(ServerProcess.mySuccessor!=null){
				            	ServerObjectPassing serverObjectPassing = new ServerObjectPassing();
				            	serverObjectPassing.sendToSuccessor(ServerProcess.mySuccessor, serverReply,chainReplicationLogger);
				            	ServerProcess.sendSequenceNumber++;
				            	SentObjClass.getInstance().putToSentObj(ServerProcess.reqSequenceNumber,serverReply);
				            	chainReplicationLogger.myLogger.log(Level.INFO, "[Send Seq# = "+ServerProcess.getSendSequenceNumber()+"] Sent to ["+ServerProcess.mySuccessor + "] "+serverReply);
				            	HistObj.getInstance().addToHistObj(serverReply.getRequest(), serverReply);
				            } 
				            // This is for Tail of Bank 2, this case will be handled in above block
				            else {
				            	// Should send reply back to T1.
				            	System.out.println("REACHED TAIL OF BANK 2, should send reply to T1");
				            }
				            
			            }
		        	}
		        	
		        	
		         } catch(ClassNotFoundException e) {
			            e.printStackTrace();
			            break;
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

	