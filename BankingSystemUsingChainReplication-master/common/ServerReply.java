package common;

import java.io.Serializable;

import server.Account;

/*
 * Class 		: 	ServerReply
 * Purpose		: 	For sending messages within the chain 
 * Who uses this: 	Servers in the chain use the object of this class to propagate the changes.
 * Special Note	:	This class is Serializable because, we will be sending the objects of this class on 
 * the network. Additionally, it is extended from ClientReply object, in the virtue of similarity of both.
 */
public class ServerReply extends ClientReply implements Serializable{
	private static final long serialVersionUID = 2L;
	private Request request;
	
	public ServerReply(String requestID, String outcome, String accountNumber, float balance, Request request){
		this.requestID = requestID;
		this.outcome = outcome;
		this.accountNumber = accountNumber;
		this.balance = balance;
		this.request = request;
	}

	public String toString(){
		return "[Request ID = "+this.requestID+"] " + "[Outcome = "+this.outcome+"]" + " [AccountNumber = "+this.accountNumber+"] " + "[Balance = "+this.balance+"]" ;
	}
	
	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(Request request) {
		this.request = request;
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
	 * @return the outcome
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * @param outcome the outcome to set
	 */
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}
	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	/**
	 * @return the balance
	 */
	public float getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(float balance) {
		this.balance = balance;
	}
	
	/**
	 * method that calculates the value of source bank from the request ID
	 * @return value of source bank
	 */
	public String getSrcBank(){
		String originalReq = this.requestID;
		originalReq = originalReq.replace(".", "@");
		String srcBank = originalReq.split("@")[0];
		return srcBank.trim();
	}
	
}
