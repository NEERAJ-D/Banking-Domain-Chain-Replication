package common;

import java.io.Serializable;

import server.Account;


/*
 * Class 		: 	ClientReply
 * Purpose		: 	For sending messages to the client 
 * Who uses this: 	Tail for sending messages to the client
 */
public class ClientReply implements Serializable{
	private static final long serialVersionUID = 4L;
	protected String requestID;
	protected String outcome;
	protected String accountNumber;
	protected float balance;
	
	public ClientReply(){
		
	}
	public ClientReply(String requestID, String outcome, String accountNumber, float balance){
		this.requestID = requestID;
		this.outcome = outcome;
		this.accountNumber = accountNumber;
		this.balance = balance;		
	}
	
	public String toString(){
		return "[Request ID = "+this.requestID+"] " + "[Outcome = "+this.outcome+"]" + " [AccountNumber = "+this.accountNumber+"] " + "[Balance = "+this.balance+"]" ;
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
	
	
	
}
