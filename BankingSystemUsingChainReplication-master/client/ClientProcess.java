package client;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import server.ListenFromMaster;
import common.ChainReplicationLogger;
import common.ClientReply;
import common.Request;

/*
 * Class 		: 	ClientProcess
 * Purpose		: 	All clients use this class for starting up, and sending requests to the server
 * 					in a sequential order  
 * Who uses this: 	All clients
 */
public class ClientProcess {
	
		/*
		 * Tokenize the requests of the client, and return a list of separate
		 * requests to be sent to the server
		 */
		public List<Request> readAndFormRequestList(String clientName, String whichRequests){
		List<Request> requestList = new ArrayList<Request>();
		
		String diffItemNRandomReq[]= whichRequests.split("#");
		
		StringBuffer itemRequests = new StringBuffer(diffItemNRandomReq[0]);
		
		if(diffItemNRandomReq.length==2){
			// This means random requests are present
			String randomRequests = diffItemNRandomReq[1];
			
			String randomValue = randomRequests.split("\\(")[1];

			String randomValueReader[] = randomValue.split(",",6);
		
			String seed = randomValueReader[0];
			String numReq = randomValueReader[1];
			int numberOfRequests = Integer.parseInt(numReq);
			String probGetBalance = randomValueReader[2];
			String probDeposit = randomValueReader[3];
			String probWithdraw = randomValueReader[4];
			String probTransfer = randomValueReader[5];
			probTransfer = probTransfer.replace(probTransfer.substring(probTransfer.length()-1), "");
			
			int getBalanceReqCount = Math.round((Float.parseFloat(probGetBalance)*Integer.parseInt(numReq)));
			int depositReqCount = Math.round((Float.parseFloat(probDeposit)*Integer.parseInt(numReq)));
			int withdrawReqCount = Math.round((Float.parseFloat(probWithdraw)*Integer.parseInt(numReq)));
			int transferReqCount = Math.round((Float.parseFloat(probTransfer)*Integer.parseInt(numReq)));

			ArrayList<String> randomRequestList = new ArrayList<String>();
			
			
			
			for(int i=0;i<getBalanceReqCount;i++){
				// Generate Get Balance Queries
				Random randForSequenceNumber = new Random();
				Random rmbank = new Random();
				String reqID = "b"+(rmbank.nextInt(2)+1)+"."+clientName+"."+(randForSequenceNumber.nextInt(210-200)+200);
				Random randForAccountNumber = new Random();
				// Randomness of Accounts will be only 4; i.e. only four account numbers will be generated
				String request = "getBalance("+reqID+", "+(randForAccountNumber.nextInt(1005-1000)+1000)+");";
				randomRequestList.add(request);
			}
			
			for(int i=0;i<depositReqCount;i++){
				// Generate Deposit Queries
				Random randForSequenceNumber = new Random();
				Random rmbank = new Random();
				String reqID = "b"+(rmbank.nextInt(2)+1)+"."+clientName+"."+(randForSequenceNumber.nextInt(210-200)+200);
				Random randForAccountNumber = new Random();
				Random randForAmount = new Random();
				// Randomness of Accounts will be only 4; i.e. only four account numbers will be generated
				String request = "deposit("+reqID+", "+(randForAccountNumber.nextInt(1005-1000)+1000)+","+(randForAmount.nextInt(900 - 100)+100)+".0);";
				randomRequestList.add(request);
			}
			

			for(int i=0;i<withdrawReqCount;i++){
				// Generate Withdraw Queries
				Random randForSequenceNumber = new Random();
				Random rmbank = new Random();
				String reqID = "b"+(rmbank.nextInt(2)+1)+"."+clientName+"."+(randForSequenceNumber.nextInt(210-200)+200);
				Random randForAccountNumber = new Random();
				Random randForAmount = new Random();
				// Randomness of Accounts will be only 4; i.e. only four account numbers will be generated
				String request = "withdraw("+reqID+", "+(randForAccountNumber.nextInt(1005-1000)+1000)+","+(randForAmount.nextInt(900 - 100)+100)+".0);";
				randomRequestList.add(request);
			}
			
			for(int i=0;i<transferReqCount;i++){	
				// Generate Random Transfer Queries - Added as part of Phase IV - Transfers
				Random randForSequenceNumber = new Random();
				Random rmbank = new Random();
				String reqID = "b1."+clientName+"."+(randForSequenceNumber.nextInt(210-200)+200);
				Random randForAccountNumber = new Random();
				Random randForAmount = new Random();
				String request = "transfer("+reqID+","+(randForAccountNumber.nextInt(1005-1000)+1000)+","+(randForAmount.nextInt(900 - 100)+100)+".0,b"+(rmbank.nextInt(3-2+1)+2)+","+(randForAmount.nextInt(200 - 100)+100)+");";
				System.out.println("TRANSFER REQUEST === "+request);
				randomRequestList.add(request);
			}
			
			// Shuffle the above created values using Shuffle function using the seed we have obtained
			Random rnd = new Random(Integer.parseInt(seed));
			Collections.shuffle(randomRequestList, rnd);
			for(int i=0;i<randomRequestList.size();i++){
				itemRequests.append(randomRequestList.get(i));
			}
			
		}
		/*
		 * Following block of code is used to deal with the itemized requests plus the 
		 * random requests that we have generated 
		 */
		String requestsToken[]=itemRequests.toString().split(";");
		for(int i=0; i<requestsToken.length; i++){
			String requestReader[] = requestsToken[i].split("\\(");
			String operation = requestReader[0];
			String requestDetails[] = requestReader[1].split(",");
			String requestID = requestDetails[0];
			String account = requestDetails[1];
			String amount = "0.0";
			String destBank = "";
			String destAccount = "";
			
			// Get amount details only for deposit and withdraw 
			if((operation.equalsIgnoreCase("deposit")) || (operation.equalsIgnoreCase("withdraw"))){
				amount = requestDetails[2];
				// Remove the ending ")" from the amount string
				amount = amount.replace(amount.substring(amount.length()-1), "");
			}
			else if (operation.equalsIgnoreCase("getBalance")){
				account = account.replace(account.substring(account.length()-1), "");				
			}
			else if(operation.equalsIgnoreCase("transfer")){
				amount = requestDetails[2];
				destBank = requestDetails[3];
				destAccount = requestDetails[4];
				destAccount = destAccount.replace(destAccount.substring(destAccount.length()-1), "");
			}
			
			Request requestObj = new Request(requestID, operation, account, Float.valueOf(amount),destBank,destAccount);
			System.out.println("Adding Request Object ::::"+requestObj);
			requestList.add(requestObj);
		}
		return requestList;		
	}
	
	/*
	 * Returns the network details of the Head and Tail servers for a particular bank
	 */
	public NetworkDetails getServerNetworkDetails(String bankName, String headOrTail){

		HashMap clientHashMap = ClientsCopyofConfigFile.getInstance().getClientConfigDetails(); 
		
		String bankChain = (String) clientHashMap.get(bankName+"_chain");
		
		//System.out.println("CLI___CHAIN__"+ClientsCopyofConfigFile.getInstance().getClientConfigDetails().get(bankName+"_chain"));
		
		String[] tokenizedBankChain = bankChain.split(",");
		String serverName = null;
		if(headOrTail.equalsIgnoreCase("head")){
			serverName = tokenizedBankChain[0];
		} else {
			serverName = tokenizedBankChain[tokenizedBankChain.length-1];
		}
		String serverIP = (String) clientHashMap.get((serverName+"_IP").trim());
		String temp = (serverName+"_listenFromClientsOn").trim();
		String serverListeningPort = (String) clientHashMap.get((serverName+"_listenFromClientsOn").trim());
		NetworkDetails serverDetails = new NetworkDetails(serverIP,serverListeningPort,serverName);
		return serverDetails;
	}
	
	/*
	 * For each request of the client, send the request packet to the server.
	 * After sending the packet, the client waits for a known delay time before re-transmitting
	 * the request.
	 */
	public void processClientRequests(String clientName, List<Request> requestList, ChainReplicationLogger chainReplicationLogger){
		HashMap clientHashMap = ClientsCopyofConfigFile.getInstance().getClientConfigDetails();
		String clientPrefix = clientName+"_";
		String cListenPort = (String) clientHashMap.get(clientPrefix+"listenPort");
		String cReTransmitDelay = (String) clientHashMap.get(clientPrefix+"reTransmitDelay");
		int cReTransmitCount = Integer.parseInt((String) clientHashMap.get(clientPrefix+"reTransmitCount"));
		String cReTransmitToNewHead = (String) clientHashMap.get(clientPrefix+"reTransmitToNewHead");
		
		String looseMsg = 	(String) clientHashMap.get(clientPrefix+"looseMsgWhenNumRequestsReachAndSetPortTo");
		int looseMsgWhenNumRequestsReach = Integer.parseInt(looseMsg.split(":")[0]);
		int errorPortNumber = Integer.parseInt(looseMsg.split(":")[1]);
		
		
		String sleepBetweenRequests = (String) clientHashMap.get(clientPrefix+"sleepBetweenRequests");
		int howMuchTimeToSleep  = sleepBetweenRequests == null ? 1000: Integer.parseInt(sleepBetweenRequests);
		
		boolean looseMsgFlag=true;
		for(int i=0; i < requestList.size(); i++){
			System.out.println("Request Size = "+requestList.size() + ", Current served ="+i);
			Request r = requestList.get(i);
			
			String toWhichBank = r.getRequestID().split("\\.")[0];
			String fromWhichClient =  r.getRequestID().split("\\.")[1];
			String requestSequenceNo =  r.getRequestID().split("\\.")[2];

			
			
			int reTransCounter = 0;
			boolean continueSending = true;
			try{
				do{

				// Get head and tail of the bank 
				NetworkDetails headDetails = getServerNetworkDetails(toWhichBank,"head");
				
				NetworkDetails tailDetails = getServerNetworkDetails(toWhichBank,"tail");
				if(i==looseMsgWhenNumRequestsReach && looseMsgFlag){
					// Loose this request 
					chainReplicationLogger.myLogger.log(Level.SEVERE, "Simulating Message Loss: "
							+ "When ["+looseMsgWhenNumRequestsReach+"] th request is about to be sent, "
									+ "I cause a message failure."
									+ " Instead of sending message to correct port, I send it to Port ["
									+ errorPortNumber +"] which does not exist in Chain Replication System. "
											+ "RequestID which was affected because of this message loss is : ["+r.getRequestID()+"]");
					System.out.println("When i="+looseMsgWhenNumRequestsReach+", I am causing message failure for "+r.getRequestID());
					
					headDetails.setPort(errorPortNumber);
					tailDetails.setPort(errorPortNumber);
					looseMsgFlag=false;
				}
				// Introduced a forced wait here. So that the speed of sending requests is lessen.
				Thread.sleep(howMuchTimeToSleep);
				sendRequestToServer(r, headDetails, tailDetails,chainReplicationLogger);
				DatagramSocket receiveClientSocket = null;
				try{
					receiveClientSocket = new DatagramSocket(Integer.parseInt(cListenPort));
					receiveClientSocket.setSoTimeout(Integer.parseInt(cReTransmitDelay));
					byte[] dataToReceive = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(dataToReceive, dataToReceive.length);
					
					receiveClientSocket.receive(receivePacket);
					byte[] receivedDataAsObject = receivePacket.getData();ByteArrayInputStream in = new ByteArrayInputStream(receivedDataAsObject);
			        ObjectInputStream is = new ObjectInputStream(in);
			        ClientReply clientReply = null;
			        clientReply = (ClientReply) is.readObject();
			        chainReplicationLogger.myLogger.log(Level.INFO, "Response received for "+clientReply);
			        continueSending = false;
					receiveClientSocket.close();
				}
				catch(SocketTimeoutException | ClassNotFoundException e){
					chainReplicationLogger.myLogger.log(Level.SEVERE, "No Response received for "+r);
					continueSending=true;
					reTransCounter++;
					receiveClientSocket.close();	
				}
				} while(continueSending==true && reTransCounter<cReTransmitCount);
							
			} catch(SocketException se) {
				
			} catch(UnknownHostException uhe){
				
			} catch(SocketTimeoutException ste){
				
			} catch(IOException ioe) {
				
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			
		}
		
	}
	
	/*
	 * The method that actually sends the request to the server.
	 * Depending on the type of operation the requests are sent to the head or the tail.
	 */
	public static void sendRequestToServer(Request r, NetworkDetails headDetails, NetworkDetails tailDetails, ChainReplicationLogger chainReplicationLogger){
		DatagramSocket sendClientSocket;
		try {
			sendClientSocket = new DatagramSocket();

		byte[] dataToSend = new byte[1024];
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(r);
        dataToSend = outputStream.toByteArray();
        DatagramPacket sendPacket=null;
		// Based on the operation type, send requests to appropriate server
        if(r.getOperation().equalsIgnoreCase("getBalance")){
			sendPacket = 
					new DatagramPacket(dataToSend, dataToSend.length, 
							tailDetails.getIpAddress(),tailDetails.getPort());
			chainReplicationLogger.myLogger.log(Level.INFO, "Sent Request to Tail ["+tailDetails.getServerName()+"] with "+r);
			System.out.println("Sent Request TO Tail ["+tailDetails.getServerName()+"]");
		}
		else{
			sendPacket = 
					new DatagramPacket(dataToSend, dataToSend.length, 
							headDetails.getIpAddress(),headDetails.getPort());
			chainReplicationLogger.myLogger.log(Level.INFO, "Sent Request to Head ["+headDetails.getServerName()+"] with "+r);
			System.out.println("Sent Request To Head ["+headDetails.getServerName()+"]");
		}
        sendClientSocket.send(sendPacket);
        
        
        sendClientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	/*
	 * Main method for invoking the client
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		if(args.length!=2){
			System.out.println("Please specify the name of the client and configuration file path");
			System.out.println("Usage: java client.ClientProcess <Client Name> <Path of Config File>\n Example: java client.ClientProcess c1 Config.txt");					
			return;
		}
		
		/*
		 * Instantiate logger for file logging
		 */
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(args[0]);
		chainReplicationLogger.myLogger.log(Level.INFO, "Client ["+args[0]+"] Process Started with Process ID : "+ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
		
		HashMap clientHashMap = new HashMap();
		String cRequests = null;
		String cListenPort = null;
		String cListenPortFromMaster = null;
		try{
			clientHashMap = ClientsCopyofConfigFile.getInstance().readSetupValuesFromClientConfigFile(args[1]);
			String clientPrefix = args[0].toString()+"_";
			cRequests = (String) clientHashMap.get(clientPrefix+"requests");
			cListenPort = (String) clientHashMap.get(clientPrefix+"listenPort");
			cListenPortFromMaster = (String) clientHashMap.get(clientPrefix+"listenPortFromMaster");
			}
		catch(Exception e){
			e.printStackTrace();			
		}
		
		/* 
		 * Listen from the Master process for any updates in the chain		 * 
		 */
		Thread clientListenFromMaster = null;
		try{
			clientListenFromMaster = new ClientListenFromMaster(args[0], cListenPortFromMaster);
			clientListenFromMaster.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ClientProcess cp = new ClientProcess();
		
		// Get the requests from this client in requestList
		List<Request> requestList = new ArrayList<Request>();
		requestList = cp.readAndFormRequestList(args[0],cRequests);

		cp.processClientRequests(args[0],requestList,chainReplicationLogger);		
		System.out.println("End of Client Request Processing.");
		
	}
}
