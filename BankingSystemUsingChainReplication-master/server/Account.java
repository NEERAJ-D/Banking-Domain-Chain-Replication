package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import common.Request;
import common.ServerReply;

/*
 * Class 		: 	ServerReply
 * Purpose		: 	Responsible for maintenance of the Accounts 
 */
public class Account implements Serializable{
	
	private static final long serialVersionUID = 3L;
	//Member Variables
	String accountNumber;
	float balance;

	public void setBalance(float balance) {
		this.balance = balance;
	}
	//Constructor
	public Account() {
		// TODO Auto-generated constructor stub
		
	}
	/* Parameterized Constructor  called in case withdraw is called with 
	 * Account Number non-existent
	 */
	Account(String nReqAccntNumber)
	{
		accountNumber = nReqAccntNumber;
		balance = 0.0f;
	}
	
	/* Parameterized COnstructor called in case deposit
	is called with Account number non-existent
	*/
	Account(String nReqAccntNumber, float fReqDeposit)
	{
		accountNumber = nReqAccntNumber;
		balance = fReqDeposit;
	}
	
	/* Sets the Account number to a particular requested value
	 */
	public void setAccountNumber(String nRequestedAccountNumber)
	{
		accountNumber = nRequestedAccountNumber;
	}
	
	/* Deposits the particular amount in the bank */
	public boolean deposit(Request reqObj)
	{
		//Increment the Account Balance by the Given Amount
		balance += reqObj.getAmount();
		return true;
	}
	
	/* Withdraws particular amount from the Bank */
	public boolean withdraw(Request reqObj)
	{
		boolean status = false;
		if(reqObj.getAmount() > getBalance())
		{
			status = false;
		}
		else
		{
			//return success parameter
			balance -= reqObj.getAmount();
			status = true;
		}
		return status;
	}
	public float getBalance()
	{
		return balance;
	}
	
	public String getAccountNumber()
	{
		return accountNumber;
	}
}