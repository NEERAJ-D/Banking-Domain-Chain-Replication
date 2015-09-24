package server;

import java.awt.List;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import common.ClientReply;
import common.Request;
import common.ServerReply;
/*
 * Class 		: 	ListenFromMaster 
 * Purpose		: 	A thread class for listening the notifications from the Master 
 * Who uses this: 	All servers
 */
	public class ListenFromMaster extends Thread{
		String serverName;
		String listenPort; 
		private ServerSocket serverSocket;
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(this.serverName);

		public ListenFromMaster(String serverName, String listenPort) throws Exception{
			serverSocket = new ServerSocket(Integer.parseInt(listenPort));
			this.serverName = serverName;
			this.listenPort = listenPort;

		}
		
		public  void run()
		   {
			
			  while(true)
		      {
		    	  
		         try
		         {
		   			Socket server = serverSocket.accept();
					DataInputStream in = new DataInputStream(server.getInputStream());
		            String receivedNotification = in.readUTF();
		            chainReplicationLogger.myLogger.log(Level.INFO, "******* Received from Master : ["+receivedNotification+"]");
		            System.out.println("I received update from Master == "+receivedNotification);	
		            // Get the earlier chain configuration
		            String [] rcdTokens = receivedNotification.split(":");
		            String whatUpdate = rcdTokens[1];
		            
		            // New Head Update
		            if(whatUpdate.equalsIgnoreCase("HEAD")){
		            	ServerProcess.myPredecessor = null;
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* I, ["+serverName+"] started operating as the new Head");
		            }
		            
		            else if(whatUpdate.equalsIgnoreCase("TAIL")){
		            	ServerProcess.mySuccessor = null;
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* I, ["+serverName+"] started operating as the new Tail");
		            }
		            
		            else if(whatUpdate.contains("RESEND_TRANSFER_REQUEST")){
		            	
		            	
		            	String headBank2 = whatUpdate.split("#")[1].trim();
		            	System.out.println("DESTINATION headBank2 = "+headBank2);
		            	
		            	String reqIDofTransfer = whatUpdate.split("#")[2].trim();
		            	
	    				String headBank2IP = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			headBank2+"_IP");
		            	String headBank2ListenPort = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			headBank2+"_listenFromPredecessorServersOn");
		            
		            	sendToNextChainHead(headBank2, headBank2IP, headBank2ListenPort, reqIDofTransfer);
	
	
		            	
		            }
		            else if(whatUpdate.equalsIgnoreCase("CHANGE_YOUR_PREDECESSOR")){
		            	ServerProcess.myPredecessor = rcdTokens[2].trim();
		            	System.out.println("IN LFM PRED = "+ServerProcess.myPredecessor);
		            	
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* I, ["+serverName+"] "
		            			+ "received from Master that my Precedessor has crashed");
		            	// Send to Master the latest Sequence Number 
		            	System.out.println("Sending to Master, seq# = "+ServerProcess.reqSequenceNumber);
		            	
		            	DataOutputStream out = new DataOutputStream(server.getOutputStream());
		            	out.writeUTF(ServerProcess.reqSequenceNumber+"");
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* I, ["+serverName+"] "
		            			+ "sent to Master Latest Sequence Number Received: "+ServerProcess.reqSequenceNumber);
		            }
		            
		            else if(whatUpdate.equalsIgnoreCase("CHANGE_YOUR_SUCCESSOR")){
		            	ServerProcess.mySuccessor=rcdTokens[2].trim();
		            	String latestSeqNoOfSPlus = rcdTokens[3].trim();
		            	System.out.println("I am S-, and I have got "+latestSeqNoOfSPlus);
		            	
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* I, ["+serverName+"] "
		            			+ "received notification from Master that my Successor has crashed. "
		            			+ "Master Sent following sequence number that was received by S+ ["+ ServerProcess.mySuccessor+ "] :"+latestSeqNoOfSPlus);
		            	
		            	chainReplicationLogger.myLogger.log(Level.INFO, "Sending Updates to ["+ServerProcess.mySuccessor+"]");
		            	
		            	// Send updates to the next server
		            	String nextServerIP = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			ServerProcess.mySuccessor+"_IP");
		            	String nextServerListenPort = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			ServerProcess.mySuccessor+"_listenFromPredecessorServersOn");
		            	String dieDuringIntermediateCrash = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			serverName+"_dieDuringIntermediateCrash");
		            	sendToSPlus(latestSeqNoOfSPlus, SentObjClass.getInstance().getSentObj(),
		            			nextServerIP, nextServerListenPort, dieDuringIntermediateCrash);	
		            }
		            
		            else if(whatUpdate.equalsIgnoreCase("YOU_ARE_NO_MORE_A_TAIL_THE_NEW_TAIL_IS")){
		            	ServerProcess.tailToStoreUpdates = true;
		            	
		            	
		            	
		            	String newWannaBeTail = rcdTokens[2].trim();
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* Received from Master that I am not the Tail. New Tail is : "+newWannaBeTail);
		            	// Get the Network Details of the New Wanna be Tail 
		            	String wannaBeTailIP = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			newWannaBeTail+"_IP");
		            	String wannaBeTailListenPort = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			newWannaBeTail+"_listenFromPredecessorServersOn");
		            	String whenToFailInChainExtensionAfterSending = ServersCopyofConfigFile.getInstance().getServerConfigDetails().get(
		            			serverName+"_whenToFailInChainExtensionAfterSending");
		            	
//		            	
//		            	System.out.println("WANNA BE TAIL IP = "+ wannaBeTailIP);
//		            	System.out.println("WANNA BE TAIL Port = "+ wannaBeTailListenPort);
//		            	System.out.println("WHEN = "+whenToFailInChainExtensionAfterSending);
//						
		            	// Do not send to T+ the required data immediately. Take some time to simulate scenarios
		            	// Give time for the new thread to open all its ports. 
						// Also give TAIL some time to start storing its updates to SENT obj.
						//
		            	//System.out.println("TAIL: I am going to wait now, to give time to processes :) ");
						Thread.sleep(6000);
						//System.out.println("TAIL: I have given enough time to processes");
		
						// Let the current tail fail here during chain extension
						// This is done to simulate the test case wherein
						
//						System.out.println("THE VALUE OF HISOBJ IS : "+HistObj.getInstance().getHistObj());
//						System.out.println("THE VALUE OF SENT OBJ IS : "+SentObjClass.getInstance().getSentObj());
//						System.out.println();
//						System.out.println();
		            	// Send to the new tail, HistObj, and requests that are in Sent
		            	sendToNewTail(serverName, newWannaBeTail, wannaBeTailIP, wannaBeTailListenPort, 
		            			HistObj.getInstance().getHistObj(), SentObjClass.getInstance().getSentObj(), whenToFailInChainExtensionAfterSending);
		            	System.out.println("SENT HISTORY AND SENT OBJ TO NEW TAIL");
		            }
		            else if(whatUpdate.equalsIgnoreCase("REINITIATE_CHAIN_EXTENSION")){
		            	ServerProcess.reInitiateChainExtension = true;
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* Earlier Tail in the chain crashed."
		            			+ "So, the Master has sent a notification to Re-initiate Chain Extension.");
		            	chainReplicationLogger.myLogger.log(Level.INFO, "******* Sending fresh Chain Extension Request to Master");
		            }
		            server.close();
		             
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
		
		/*
		 * sendToNewTail is used for sending the details to the New Wannabe Tail
		 */
		public void sendToNewTail(String oldTail, String newTail, String wannaBeTailIP, String wannaBeTailListenPort, 
				HashMap<Request, ServerReply> histObj, 
				ConcurrentHashMap<Integer, ServerReply> sentObj, String whenToFailInChainExtensionAfterSending) {
			try {
				// We need to send the HistObj to the new tail piece by piece.
			
				ArrayList<ContainerRequestServerReply> containerList = new ArrayList<ContainerRequestServerReply>(); 
				
				for(Map.Entry<Request, ServerReply> entry : histObj.entrySet()){
				    ContainerRequestServerReply containerObj = new ContainerRequestServerReply(entry.getKey(), entry.getValue());
				    containerList.add(containerObj);				    
				}
				
				// Now, send only few values from the arraylist, say 2 per packet
				double listByTwo = Math.ceil(containerList.size()/2);
				int indexCounter = 0;
				
				for(int i=0;i<2;i++){
					ArrayList<ContainerRequestServerReply> listToSend = new ArrayList<ContainerRequestServerReply>();
					for(int j=0;j<listByTwo;j++){					
						listToSend.add(containerList.get(indexCounter++));
					}
					System.out.println("Added "+listToSend.size()+" objects to the list and now sending");

					Socket client = new Socket(wannaBeTailIP.trim(), Integer.parseInt(wannaBeTailListenPort.trim()));
					byte[] dataToSend = new byte[1024];
					OutputStream outToServer = client.getOutputStream();
			        DataOutputStream out = new DataOutputStream(outToServer);
			        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			        ObjectOutputStream os = new ObjectOutputStream(outputStream);
			        os.writeObject(listToSend);
			        System.out.println("Wrote listToSend obj to "+client.getPort());
			        dataToSend = outputStream.toByteArray();
			        out.write(dataToSend, 0, dataToSend.length);
			        listToSend = null; // Set this to null, so that the GC collects it
			        
			        // Failure Scenario 1: When current tail fails during chain extension
		        	// Chain Extension Failure Scenario : 1
			        System.out.println("whenToFailInChainExtensionAfterSending = "+whenToFailInChainExtensionAfterSending);
			        if(whenToFailInChainExtensionAfterSending!=null && 
			        		Integer.parseInt(whenToFailInChainExtensionAfterSending)>0 && i==0){
			        	chainReplicationLogger.myLogger.log(Level.INFO, "Sent partial HistObj to New tail."
			        			+ " And now, I decide to die in the middle of Chain Extension");
			        	ServerProcess.sendSequenceNumber = Integer.parseInt(whenToFailInChainExtensionAfterSending);
			        }
			        
			        ServerProcess.sendSequenceNumber++;
			        
			        os.close();
			        client.close();
					
				}
				chainReplicationLogger.myLogger.log(Level.INFO, "Done Sending Hist Objects piece by piece to prospective new Tail");
		        Thread.sleep(3000);
		        
		        Socket client1 = new Socket(wannaBeTailIP.trim(), Integer.parseInt(wannaBeTailListenPort.trim()));
				byte[] dataToSend1 = new byte[1024];
				OutputStream outToServer1 = client1.getOutputStream();
		        DataOutputStream out1 = new DataOutputStream(outToServer1);
		        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
		        ObjectOutputStream os1 = new ObjectOutputStream(outputStream1);
		        os1.writeObject(sentObj);
		        System.out.println("Wrote sent  obj to "+client1.getPort());
		        dataToSend1 = outputStream1.toByteArray();
		        out1.write(dataToSend1, 0, dataToSend1.length);
		        os1.close();
		        client1.close();
		        ServerProcess.sendSequenceNumber++;
		        ServerProcess.tailToStoreUpdates = false;
		        
		        chainReplicationLogger.myLogger.log(Level.INFO, "Sent SENTOBJ to prospective new Tail");
		        Thread.sleep(2000);
		        
		        
		        Socket client2 = new  Socket(wannaBeTailIP.trim(), Integer.parseInt(wannaBeTailListenPort.trim()));
		        byte[] dataToSend2 = new byte[1024];
				OutputStream outToServer2 = client2.getOutputStream();
		        DataOutputStream out2 = new DataOutputStream(outToServer2);
		        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
		        ObjectOutputStream os2 = new ObjectOutputStream(outputStream2);
		        os2.writeObject((String) oldTail+":END_OF_SENT_T");
		        System.out.println("WROTE END T MESSAGE");
		        dataToSend2 = outputStream2.toByteArray();
		        out2.write(dataToSend2, 0, dataToSend2.length);
		        os2.close();
		        ServerProcess.sendSequenceNumber++;
		        client2.close();
		        chainReplicationLogger.myLogger.log(Level.INFO, "Sent END_OF_SENT_T Marker "
		        		+ "Message to prospective new Tail");
		        // For old tail, set the value of its successor
		        ServerProcess.mySuccessor = newTail;
		        
	        } catch(ConnectException ce){
		        	// The control will come to this block when the new server is unreachable.
		        	// i.e. The new tail has crashed 
	        	
		        	// Chain Extension Failure Scenario : 2
		        	if(ce.getMessage().equalsIgnoreCase("Connection refused")){
		        		System.out.println("Could not connect to the new Tail. Graceful abort"
		        				+ ". Resetting all my pointers");	
		        		// Reinforce the Successor value to null once again
			        	ServerProcess.mySuccessor = null;
			        	ServerProcess.tailToStoreUpdates = false;
			        	chainReplicationLogger.myLogger.log(Level.WARNING, "Could not connect to the new Tail. Graceful abort. Resetting all my pointers.");
		        		
		        	}	        
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
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
		
		/*
		 * sendToNewTail is used for sending the details to the New Wannabe Tail
		 */
		public void sendToSPlus(String latestSeqNoOfSPlus, 
				ConcurrentHashMap<Integer, ServerReply> sentObj, 
				String nextServerIP, String nextServerPort, String dieDuringIntermediateCrash) {
			try {
			    // Compute which updates need to be sent to the next server
				ConcurrentHashMap<Integer, ServerReply> sentObjToBeForwarded =
						new ConcurrentHashMap<Integer, ServerReply>();
				
				for(Map.Entry<Integer, ServerReply> entry : sentObj.entrySet()){
					System.out.println("--------- Send Sequence Number: "+entry.getKey()+"-------------");
					chainReplicationLogger.myLogger.log(Level.INFO, "Send Sequence Number: "+entry.getKey());
				    if(entry.getKey() > Integer.parseInt(latestSeqNoOfSPlus)){
				    	// These keys and values need to be sent to the next server
				    	sentObjToBeForwarded.put(entry.getKey(), entry.getValue());
				    }
				}
				
				// For Failure Scenario 1, the S- should fail here
				if(Integer.parseInt(dieDuringIntermediateCrash)>1){
					System.out.println("I learnt the computation, and now gonna die");
					ServerProcess.sendSequenceNumber=1000;
					
					chainReplicationLogger.myLogger.log(Level.INFO, "I learnt which updates I need to send to S+. "
							+ " Now I ["+serverName+"] will forcefully die");
					/*
					 * A sleep is required here because immediately after this line, 
					 * the server tries to establish connection with the next server, 
					 * and it does establish! But during this, it crashes, so we 
					 * get an error that new server. 
					 */
					Thread.sleep(2000);
				}
				
				Socket client1 = new 
		        		Socket(nextServerIP.trim(), Integer.parseInt(nextServerPort.trim()));
				byte[] dataToSend1 = new byte[1024];
				OutputStream outToServer1 = client1.getOutputStream();
		        DataOutputStream out1 = new DataOutputStream(outToServer1);
		        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
		        ObjectOutputStream os1 = new ObjectOutputStream(outputStream1);
		        
		        os1.writeObject(sentObjToBeForwarded);
		        System.out.println("Wrote sentObjToBeForwarded   to "+client1.getPort());
		        dataToSend1 = outputStream1.toByteArray();
		        out1.write(dataToSend1, 0, dataToSend1.length);
		        os1.close();
		        client1.close();
		        ServerProcess.sendSequenceNumber++;	
	        
		        chainReplicationLogger.myLogger.log(Level.INFO, "Sent ["+sentObjToBeForwarded.size()+"] SENT objects to S+");
		        
		        
	        } catch(ConnectException ce){
		        	// The control will come to this block when the new server is unreachable.
		        	// i.e. The new tail has crashed 
	        		if(ce.getMessage().equalsIgnoreCase("Connection refused")){
		        		System.out.println("Could not connect to the new server. Graceful abort"
		        				+ ". Resetting all my pointers");
		        		chainReplicationLogger.myLogger.log(Level.INFO, "Could not connect to the new server S+. Graceful abort. Resetting all my pointers");
		        	}	        
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
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}

		
		
		
		/*
		 * sendToNewTail is used for sending the details to the New Wannabe Tail
		 */
		public void sendToNextChainHead(String headBank2,
				String headBank2IP, String headBank2Port, String requestIDofTransfer) {
			try {
				
				ConcurrentHashMap<Integer, ServerReply> tempSentObj  = null;
				ConcurrentHashMap<String, Integer> tempReqIDSequenceNumber = null;
				
				
				// Firstly get that request ID out from the Sent Array of this server
				tempReqIDSequenceNumber = SentObjClass.getInstance().getReqIDSequenceNumber();
				tempSentObj = SentObjClass.getInstance().getSentObj();
				
				System.out.println("TEMP REQ ID SEQ NO HASHMAP = "+tempReqIDSequenceNumber);
				System.out.println("TEMP SENT HASHMAP = "+tempSentObj);
				
				int mySeqNumber = tempReqIDSequenceNumber.get(requestIDofTransfer);
				System.out.println("mySeqNumber = "+mySeqNumber);
				
				// Bravo, now I got the object which I need to send to the head of Chain 2
				ServerReply sReply = tempSentObj.get(mySeqNumber);
				//System.out.println("S REPLY EETHE IS ++++ "+sReply);
				
				
				
			   // Establish Connection
				Socket client1 = new 
		        		Socket(headBank2IP.trim(), Integer.parseInt(headBank2Port.trim()));
				byte[] dataToSend1 = new byte[1024];
				
				OutputStream outToServer1 = client1.getOutputStream();
		        DataOutputStream out1 = new DataOutputStream(outToServer1);
		        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
		        ObjectOutputStream os1 = new ObjectOutputStream(outputStream1);
		        
		        os1.writeObject(sReply);
		        //System.out.println("EETHE Wrote sReply to "+client1.getPort());
		        dataToSend1 = outputStream1.toByteArray();
		        out1.write(dataToSend1, 0, dataToSend1.length);
		        os1.close();
		        client1.close();
		        ServerProcess.sendSequenceNumber++;	
	        
		        //chainReplicationLogger.myLogger.log(Level.INFO, "Sent ["+sentObjToBeForwarded.size()+"] SENT objects to S+");
		        
		        
	        } catch(ConnectException ce){
		        	// The control will come to this block when the new server is unreachable.
		        	// i.e. The new tail has crashed 
	        		if(ce.getMessage().equalsIgnoreCase("Connection refused")){
		        		System.out.println("Could not connect to the new server. Graceful abort"
		        				+ ". Resetting all my pointers");
		        		chainReplicationLogger.myLogger.log(Level.INFO, "Could not connect to the new server S+. Graceful abort. Resetting all my pointers");
		        	}	        
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
		
		
}

	