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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import server.ServerObjectPassing;
import server.ServerProcess;



/*
 * Class: MasterListenNotificationThread
 * Purpose: Keep on listening IAmAlive Notifications from the servers
 * Who uses it: Master Process
 */
public class MasterListenNotificationThread implements Runnable{

	
	private String masterListenPort; 
	private String serverNotificationListenPort;
	private ServerSocket serverSocket;
	ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance("Master");
	
	
	public MasterListenNotificationThread(String masterListenPort, String serverNotificationListenPort) 
			throws Exception{
		this.masterListenPort = masterListenPort;
		this.serverNotificationListenPort = serverNotificationListenPort;
		//serverSocket = new ServerSocket(Integer.parseInt(serverNotificationListenPort));
	}

	public void run(){
		byte [] receiveData ;
		try{
		DatagramSocket masterListenSocket = null;
		masterListenSocket = new DatagramSocket(Integer.parseInt(this.masterListenPort));
		
		while(true){
			
			
				receiveData= new byte[1024];
				DatagramPacket receiveAliveDataPacket = new DatagramPacket(receiveData, receiveData.length);
				masterListenSocket.receive(receiveAliveDataPacket);
				String messageReceived = new String (receiveAliveDataPacket.getData());
				
//				System.out.println("I MASTER RECEIVED THIS MESSAGE: "+messageReceived.trim() );	
							
	            // Here we get which server has sent the notification
				// Sample message for Alive Notifications: "I_Am_Alive:s1"
				// Sample message for adding server to the chain: "Add_Me:s4:b1"
	            String aliveTokens[] = messageReceived.split(":");

				if(aliveTokens[0].startsWith("WHAT_IS_HEAD_OF")){
					//System.out.println("VALUE OF Alive tokens 0 is : "+aliveTokens[0]);
	            	
					// This marks that the Tail of Bank 1 wants to start the transfer process
					// And thus, now I as Master will record the reqID for which the process
					// is going on.
					
					
					String destBankAsked = aliveTokens[0].split("#")[1].trim();
	            	System.out.println("Destination Bank asked = "+destBankAsked);
	            	
	            	chainReplicationLogger.myLogger.log(Level.INFO, "Request arrived asking Destination Bank's ["+destBankAsked+"] Head Value");
	            	
	            	String reqIDOfTransfer = aliveTokens[0].split("#")[2].trim();
	            	
	            	System.out.println("ReqID of Transfer = "+reqIDOfTransfer);
	            	
	            	String srcBank = aliveTokens[0].split("#")[3].trim();
	            	System.out.println("Source Bank = "+srcBank);
	            	
	            	
	            	MasterProcess.reqIDofOngoingTransfer = reqIDOfTransfer;
	            	MasterProcess.destBankofOngoingTransfer = destBankAsked;
	            	MasterProcess.srcBankofOngoingTransfer = srcBank;
	            	MasterProcess.ongoingTransfer = true;
	            	
	            	//get only belongingness of crashed server.... refer first field from conf file 
	            	
	            	String destBankChain = MastersCopyofConfigFile.getInstance().getMasterConfigDetails().get(destBankAsked+"_chain");
	            	System.out.println("Destination Bank Chain at Master= "+destBankChain);
	            	String destHead = destBankChain.split(",")[0];
	            	System.out.println("Head Detected = "+destHead);
	            	InetAddress IPA = receiveAliveDataPacket.getAddress();
	            	int toSendPort = receiveAliveDataPacket.getPort();
	            	DatagramPacket sendBackToServer =
	                        new DatagramPacket(destHead.getBytes(), destHead.getBytes().length, 
	                        		IPA, toSendPort);
	            	masterListenSocket.send(sendBackToServer);
	            	
	            	System.out.println("SENT HEAD VALUE TO BACK SERVER ...");
	            	chainReplicationLogger.myLogger.log(Level.INFO, "Sent Destination Bank's ["+destBankAsked+"] Head Value ["+destHead+"] to server.");
	            	// Exit immediately after sending value back to the server
	            	continue;
	            		            	
	            }
				else if(aliveTokens[0].startsWith("WHAT_IS_TAIL_OF")){
//					System.out.println("VALUE OF Alive tokens 0 is : "+aliveTokens[0]);
	            	
					// This marks that the Tail of Bank 1 wants to start the transfer process
					// And thus, now I as Master will record the reqID for which the process
					// is going on.
					String srcBankAsked = aliveTokens[0].split("#")[1].trim();
	            	System.out.println("Source bank asked = "+srcBankAsked);
	            	
	            	chainReplicationLogger.myLogger.log(Level.INFO, "Request arrived asking Source Bank's ["+srcBankAsked+"] Tail Value");
	            	
	            	String reqIDOfTransfer = aliveTokens[0].split("#")[2].trim();
	            	System.out.println("REQ of Transfer = "+reqIDOfTransfer);

	            	// Master will now reset the flags which were keeping track of transfers

	            	MasterProcess.reqIDofOngoingTransfer = "";
	            	MasterProcess.destBankofOngoingTransfer = "";
	            	MasterProcess.ongoingTransfer = false;
	            	
	            	//get only belongingness of crashed server.... refer first field from conf file 
	            	
	            	String srcBankChain = MastersCopyofConfigFile.getInstance().getMasterConfigDetails().get(srcBankAsked+"_chain");
	            	System.out.println("Destination Bank Chain at Master= "+srcBankChain);
	            	
	            	String srcTailTokens[] = srcBankChain.split(",");
	            	String srcTail = srcTailTokens[srcTailTokens.length - 1]; 
	            	System.out.println("Tail Detected = "+srcTail);
	            	InetAddress IPA = receiveAliveDataPacket.getAddress();
	            	int toSendPort = receiveAliveDataPacket.getPort();
	            	DatagramPacket sendBackToServer =
	                        new DatagramPacket(srcTail.getBytes(), srcTail.getBytes().length, 
	                        		IPA, toSendPort);
	            	masterListenSocket.send(sendBackToServer);
	            	
	            	System.out.println("SENT TAIL  VALUE TO BACK SERVER ..."+srcTail);
	            	chainReplicationLogger.myLogger.log(Level.INFO, "Sent Source Bank's ["+srcBankAsked+"] Tail Value ["+srcTail+"] to server.");
	            	// Exit immediately after sending value back to the server
	            	continue;       	
	            }
				
	            MasterProcess.MonitorServerArray[Integer.parseInt(aliveTokens[1].trim())] = System.currentTimeMillis();
	            
	            if(aliveTokens[0].equalsIgnoreCase("Add_Me")){
	            	System.out.println("Added "+aliveTokens[1]);
	            	//chainReplicationLogger.myLogger.log(Level.INFO, "Started Listening HeartBeats from: [s"+aliveTokens[1].trim()+"]");
	            	HashMap masterHashMap = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
	            	String bank = aliveTokens[2].trim()+"_chain";
		            String bankChain = (String) masterHashMap.get(bank);
	            	
		            // As part of Phase IV Extension
		            // Get the list of all the bank chains
		            ArrayList<String> bankChains = new ArrayList<String>();
		    		Set<String> setOfKeys = masterHashMap.keySet();
		    		
		    		Iterator<String> itr = setOfKeys.iterator();
		    		while(itr.hasNext()){
		    			String currentKey = itr.next();
		    			if(currentKey.contains("_chain")){
		    				bankChains.add((String) masterHashMap.get(currentKey));
		    			}
		    		}

		    		
		    		//System.out.println("Bank Chains are: "+bankChains);
		    		
		    		StringBuilder sb = new StringBuilder();
		    		for (String s : bankChains)
		    		{
		    		    sb.append(s);
		    		    sb.append("\t");
		    		}

		    		
	            	/*
	            	 * Code part of Chain Extension Process
	            	 */
		    		String tempProNewTail = "s"+aliveTokens[1].trim();
		    		
	            	//if(!(bankChains.contains(aliveTokens[1].trim()))){
	            		if(!(sb.toString().contains((String)tempProNewTail))){
	            		MasterProcess.prospectiveNewTail = tempProNewTail;
	            		chainReplicationLogger.myLogger.log(Level.INFO, "Started Serving Chain Extension Request from ["+tempProNewTail+"]");
	            		
	            		// Get Tail of this bank
	            		ServerObjectPassing sop = new ServerObjectPassing();
	            		String oldTail = sop.getTail(bankChain.split(","));
	            		
	            		String newWannaBeTail = aliveTokens[1].trim();
	            		
	            		//HashMap masterHashMap1 = MastersCopyofConfigFile.getInstance().getMasterConfigDetails();
	            		String oldTailListeningOn = (String) masterHashMap.get(oldTail+"_listenFromMasterOn");
	            		String oldTailIP = (String) masterHashMap.get(oldTail+"_IP");
	            		System.out.println("oldTailListeningOn = "+oldTailListeningOn);
	            		System.out.println("oldTailIP = "+oldTailIP);
	            		try {
	            			
	            			// Send notification to the old Tail that you are not the tail anymore
	            			Socket client = new Socket(oldTailIP, Integer.parseInt(oldTailListeningOn));
	            			OutputStream outToServer = client.getOutputStream();
	            	        DataOutputStream out = new DataOutputStream(outToServer);
	            	        out.writeUTF("OLD_TAIL:YOU_ARE_NO_MORE_A_TAIL_THE_NEW_TAIL_IS:s"+newWannaBeTail);
	            	        chainReplicationLogger.myLogger.log(Level.INFO, "[M to "+oldTail+"(CurrentTail)] YOU_ARE_NO_MORE_A_TAIL_NEW_TAIL_IS:s"+newWannaBeTail);
	            	        System.out.println("Master Wrote this to the Old Tail -> YOU_ARE_NO_MORE_A_TAIL_NEW_TAIL_IS:s"+newWannaBeTail);
	            	        client.close();
	            	        
	                    } catch (NumberFormatException e) {
	            			e.printStackTrace();
	            		} catch (UnknownHostException e) {
	            			e.printStackTrace();
	            		} catch (IOException e) {
	            			e.printStackTrace();
	            		}
	            		
	            		if(!MasterProcess.chainExtensionInProgress){
	            			new Thread(new SendNotificationsFromMaster(oldTail, newWannaBeTail, serverNotificationListenPort)).start();
	            		}
	            		// Start the chain extension process
            			MasterProcess.chainExtensionInProgress=true;
            			
            			
	            		
	            	}
	            	else {
	            		System.out.println("Not Required to add server "+ aliveTokens[1]+" to the list");
	            	}
	            } 
			}
	            
			} catch(SocketTimeoutException s) {
               s.printStackTrace();
            } catch(IOException e){
               e.printStackTrace();
            } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		
	}
	
	
}