package server;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import common.ServerReply;
import common.Request;

/*
 * Class 		: 	Bank
 * Purpose		: 	Storing all the accounts of the bank and perform transactions  
 */
public class Bank {
	
	/* Singleton Constructor of the Bank object */
	private static Bank instance = null;
	
	
	//Public Array of Objects of Bank Accounts
	Set<Account> AccountsList;
	
	
	//To not allow instantiation from outside and maintain single copy
	private Bank(){
		AccountsList = new HashSet<Account>();
	}
	
	/*
	 * synchronized keyword is added here to ensure that it is a Threadsafe Singleton 	
	 */
	public static synchronized Bank getInstance()
	{
		if(instance == null)
		{
			instance  = new Bank();
		}
		return instance;
	}
	
	/* Gets instance of the Account */
	public Account getAccountInfo(String nRequestedAccountNumber)
	{
		/* Fetch the value from the set
		If the value fetched is null create a new account
		else return the existing Account reference
		*/
		Account aDummy = new Account();
		try
		{	
		boolean bFound = false;
	    Iterator<Account> iterator = AccountsList.iterator();
	    
	    if(true == iterator.hasNext())
	    {
		    while(iterator.hasNext()) {
		        Account  nCurrentAccount = iterator.next();
		        if(nCurrentAccount.accountNumber.equals(nRequestedAccountNumber) ) {
		        	bFound = true;
		        	return nCurrentAccount;
		            
		        }
		    }
		    //At the End of iteration
		    if(false == bFound)
		    {
		    	//Account does not exist so create a new Account
		    	Account nNewAccount = new Account(nRequestedAccountNumber);
		    	AccountsList.add(nNewAccount);
		    	return nNewAccount;
		    }
	    }
	    else
	    {
	    	//Account does not exist so create a new Account
	    	Account nNewAccount = new Account(nRequestedAccountNumber);
	    	AccountsList.add(nNewAccount);
	    	return nNewAccount;
	    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//Dummy return need to modify
		return aDummy;
	}
	


	/* This Function shall be responsible for interpreting the type of the request 
	 * and then assigning the respective functions to actually perform the function
	 * Also, the REply Object shall be created and returned
	 */
	public ServerReply processRequests(String serverName,Request reqObj)
	{
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance(serverName);
		ServerReply serverReply = null;
		// Check if the request that has arrived is already in the HistObj
		boolean isDuplicate = HistObj.getInstance().checkDuplicateTransRequest(reqObj);
		if(isDuplicate){
			chainReplicationLogger.myLogger.log(Level.INFO, "Duplicate Request : "+reqObj);
			return serverReply = HistObj.getInstance().getDuplicateReply(reqObj);
		}
		
		boolean isInconsistent = HistObj.getInstance().checkInconsistentTransRequest(reqObj);
		if(isInconsistent){
			float balanceOfThatAccount = getAccountInfo(reqObj.getAccount()).getBalance();
			return serverReply = new ServerReply(reqObj.getRequestID(), "InconsistentWithHistory", reqObj.getAccount(),balanceOfThatAccount, reqObj);
		}
			
		//Check the type of operation and based on the type perform different actions
		String strOperation = reqObj.getOperation();
		if(strOperation.equalsIgnoreCase("getBalance"))
		{
			Account ref = getAccountInfo(reqObj.getAccount());
			ref.getBalance();
			chainReplicationLogger.myLogger.log(Level.INFO, "Query Request Complete for "+reqObj);
			serverReply = new ServerReply(reqObj.getRequestID(), "Processed", ref.getAccountNumber(),ref.getBalance(), reqObj);
		}
		else if(strOperation.equalsIgnoreCase("deposit"))
		{
			
			// Check if the account exists or not
			Account ref = getAccountInfo(reqObj.getAccount());
			ref.deposit(reqObj);
			serverReply = new ServerReply(reqObj.getRequestID(), "Processed", ref.getAccountNumber(),ref.getBalance(), reqObj);
			chainReplicationLogger.myLogger.log(Level.INFO, "Deposit Request Complete for "+reqObj);
			return serverReply;
			
		}
		else if(strOperation.equalsIgnoreCase("withdraw"))
		{
			Account ref = getAccountInfo(reqObj.getAccount());
			
			boolean status = ref.withdraw(reqObj);
			serverReply = new ServerReply(reqObj.getRequestID(), (status?"Processed":"InsufficientFunds"), 
					ref.getAccountNumber(),ref.getBalance(), reqObj);
			chainReplicationLogger.myLogger.log(Level.INFO, "Withdraw Request Complete for "+reqObj);
			
		}
		else if(strOperation.equalsIgnoreCase("transfer"))
		{
			/* First check who is receiving this transfer request. 
			* If it is from the first chain, then the mathematical operation that will be 
			* performed will be WITHDRAW, and in the second chain, the operation will be DEPOSIT.
			*/
			
			if(ServerProcess.iBelongToBank.equalsIgnoreCase(reqObj.getDestBank().trim())){
				// In Bank 2's chain
				Account ref = getAccountInfo(reqObj.getDestAccount());
				ref.deposit(reqObj);
				//System.out.println("Deposit Placed in Bank 2 Successfully");
				
				ServerProcess.tailToStoreUpdates = true;
				ServerProcess.transferInProgress = true;
				ServerProcess.transferInProgressWithReqID = reqObj.getRequestID();
				
				
				// Explicitly set the value of serverReply object to null, so that when this call
				// returns back, there can be check made, that if the return value is null, then 
				// do a certain operation
				serverReply = null;
				

				
			} else {
				// In Bank 1's chain
				Account ref = getAccountInfo(reqObj.getAccount());
				boolean status = ref.withdraw(reqObj);
				if(status){
					// Successful withdrawal, so send the request to the destination bank
					//System.out.println("Amount Withdrawn Successfully");
					serverReply = new ServerReply(reqObj.getRequestID(), (status?"Processed":"InsufficientFunds"), 
							ref.getAccountNumber(),ref.getBalance(), reqObj);
					
					// Now start transfer process in this bank's chain
					ServerProcess.tailToStoreUpdates = true;
					ServerProcess.transferInProgress = true;
					ServerProcess.transferInProgressWithReqID = reqObj.getRequestID();
				}
				else {
					// Insufficient Funds, so no need to send request to the destination bank
					
					
					serverReply = new ServerReply(reqObj.getRequestID(), (status?"Processed":"InsufficientFunds"), 
							ref.getAccountNumber(),ref.getBalance(), reqObj);
					
				} // End Insufficient Balance Section
			} // End Bank 1's section
		} // End Transfer Operation
		else
		{
			//Invalid Request type
			System.out.println("Invalid Request type");
			chainReplicationLogger.myLogger.log(Level.SEVERE, "Invalid Request Type : "+reqObj.getOperation());
		}
		
		
		return serverReply;
	}
	
}