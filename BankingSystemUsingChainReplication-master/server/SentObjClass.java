package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import common.ChainReplicationLogger;
import common.ServerReply;

/*
 * Class 		: 	SentObjClass
 * Purpose		: 	For storing sent array of the server  
 */
public class SentObjClass implements Serializable{
	private static final long serialVersionUID = 14L;
	private static SentObjClass instance = null;
	
	ConcurrentHashMap<Integer, ServerReply> sentObj  = null;
	ConcurrentHashMap<String, Integer> reqIDSequenceNumber = null;
	
	public static synchronized SentObjClass getInstance()
	{
		if(instance == null)
		{
			instance  = new SentObjClass();
		}
		return instance;
	}
	
	private SentObjClass(){
		sentObj = new ConcurrentHashMap<Integer, ServerReply>();
		reqIDSequenceNumber = new ConcurrentHashMap<String, Integer>();
		
	}

	
	
	public void putToSentObj(int reqSequenceNum, ServerReply serverReply){
		
		sentObj.put(reqSequenceNum,serverReply);
		reqIDSequenceNumber.put(serverReply.getRequestID(), reqSequenceNum);
		}

	/*
	 * Remove the request IDs which are less than that current value
	 */
	public synchronized void removeFromSentObj(String ackRequestID, ChainReplicationLogger chainReplicationLogger){
		
		try{
			int sequenceNumberToDelete = reqIDSequenceNumber.get(ackRequestID);
			
			// sequenceNumberToDelete will contain the sequence number, which needs to be removed 
			Iterator iterator = sentObj.keySet().iterator();
			while(iterator.hasNext()){
				int key =  (int) iterator.next();
				if(key <= sequenceNumberToDelete){
					sentObj.remove(key);
					reqIDSequenceNumber.remove(ackRequestID);
					
					System.out.println("REMOVED RECORD WITH KEY = "+key);
					chainReplicationLogger.myLogger.log(Level.INFO, "Removed Record with Sequence Number : ["+key+"]");
					
				}
			}
		}
		catch(NullPointerException npe){
			System.out.println("The record was a duplicate record and was already removed in an earlier ACK");
			chainReplicationLogger.myLogger.log(Level.WARNING, "The record was a duplicate record and was already removed in an earlier Acknowledgement");
		}
		catch(ArrayIndexOutOfBoundsException e){
			
		}
		
	}

	/**
	 * @return the sentObj
	 */
	public ConcurrentHashMap<Integer, ServerReply> getSentObj() {
		return sentObj;
	}

	/**
	 * @param sentObj the sentObj to set
	 */
	public void setSentObj(ConcurrentHashMap<Integer, ServerReply> sentObj) {
		this.sentObj = sentObj;
	}

	/**
	 * @return the reqIDSequenceNumber
	 */
	public ConcurrentHashMap<String, Integer> getReqIDSequenceNumber() {
		return reqIDSequenceNumber;
	}

	/**
	 * @param reqIDSequenceNumber the reqIDSequenceNumber to set
	 */
	public void setReqIDSequenceNumber(
			ConcurrentHashMap<String, Integer> reqIDSequenceNumber) {
		this.reqIDSequenceNumber = reqIDSequenceNumber;
	}
	
}
