package common;

import java.io.Serializable;
	
/*
 * Class 		: 	Request
 * Purpose		: 	The object of this class to represent the request data 
 * Who uses this: 	Clients and Servers 
 * Special Note	:	This class is Serializable because, we will be sending the objects of this class on 
 * the network.
 */
public class Request implements Serializable{
	

	private static final long serialVersionUID = 1L;
	
	private String requestID;
	private String operation;
	private String accountNumber;
	private float amount = 0.0f;
	private String destBank;
	private String destAccount;
	
	public Request(String requestID, String operation, String accountNumber, float amount, 
			String destBank, String destAccount){
		this.setOperation(operation);
		this.setRequestID(requestID);
		this.setAccount(accountNumber);
		this.setAmount(amount);
		this.setDestBank(destBank);
		this.setDestAccount(destAccount);
	}

	/*
	 * Overridden toString method from java.lang.Object class 
	 * This method will print the contents of the Request Object
	 */
	public String toString(){
		String printAmount = "";
		String transferDetails = "";
		if(!(this.operation.equalsIgnoreCase("getBalance"))){
			printAmount = "[Amount = "+this.amount+"]";
		}
		if(this.operation.equalsIgnoreCase("transfer")){
			transferDetails = " [DestBank = "+this.destBank+"] [DestAccount = "+this.destAccount+"]";
		}
		return "[Request ID = "+this.requestID+"] "
				+ "[Operation = "+this.operation+"] "
						+ "[Account Number = "+this.accountNumber + "] "
								+ printAmount + transferDetails;
		
	}
	/**
	 * @return the requestID
	 */
	public String getRequestID() {
		return requestID;
	}

	/**
	 * @param requestID the requestID to set
	 */
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * @return the account
	 */
	public String getAccount() {
		return accountNumber;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.accountNumber = account;
	}
	
	/**
	 * @return the amount
	 */
	public float getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}

	/**
	 * @return the destBank
	 */
	public String getDestBank() {
		return destBank;
	}

	/**
	 * @param destBank the destBank to set
	 */
	public void setDestBank(String destBank) {
		this.destBank = destBank;
	}

	/**
	 * @return the destAccount
	 */
	public String getDestAccount() {
		return destAccount;
	}

	/**
	 * @param destAccount the destAccount to set
	 */
	public void setDestAccount(String destAccount) {
		this.destAccount = destAccount;
	}


}