package server;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import common.Request;
import common.ServerReply;
/*
 * Class 		: 	HistObj
 * Purpose		: 	For storing the history of server  
 */
public class HistObj implements Serializable{
	private static final long serialVersionUID = 8L;
	private static HistObj instance = null;
	HashMap<Request, ServerReply> histObj;
	
	public static synchronized HistObj getInstance()
	{
		if(instance == null)
		{
			instance  = new HistObj();
		}
		return instance;
	}
	
	private Object readResolve() throws ObjectStreamException{
		return getInstance();
	}
	
	private HistObj(){
		histObj = new HashMap<Request, ServerReply>();;
	}

	/**
	 * @return the histObj
	 */
	public HashMap<Request, ServerReply> getHistObj() {
		return histObj;
	}

	/**
	 * @param histObj the histObj to set
	 */
	public void setHistObj(HashMap<Request, ServerReply> histObj) {
		this.histObj = histObj;
	}
	
	public void addToHistObj(Request request, ServerReply serverReply) {
		this.histObj.put(request, serverReply);
	}
	

	/*
	 * Check for duplicate request.
	 * A request is duplicate, if it has the same requestID, operation and amount 
	 */
	public boolean checkDuplicateTransRequest(Request reqObj){
		
		boolean isDuplicate = false;
		//HashMap<Request, ServerReply> checkDuplicate= account.getProcessTrans();
		
		if(!this.histObj.isEmpty()){
			String reqIDFromRequest = reqObj.getRequestID();
			String operationFromRequest = reqObj.getOperation();
			float amountFromRequest = reqObj.getAmount();
			String accountNumber = reqObj.getAccount();
			
			Set<Request> requestKeys = this.histObj.keySet();
			Iterator<Request> itr = requestKeys.iterator();
			while(itr.hasNext()){
				Request tempReq = (Request) itr.next();
				if(reqIDFromRequest.equalsIgnoreCase(tempReq.getRequestID()) &&
						accountNumber.equalsIgnoreCase(tempReq.getAccount()) &&
						operationFromRequest.equalsIgnoreCase(tempReq.getOperation()) &&
						amountFromRequest == tempReq.getAmount()){
					isDuplicate = true;
					break;
				}
			}			
		}
		return isDuplicate;
	}
	
	/*
	 * If a request is identified as duplicate, then we need to send the same reply as the 
	 * earlier request. This method finds the earlier reply that was sent for that request. 
	 */
	public ServerReply getDuplicateReply(Request request){
		ServerReply serverReply = null;
		
		if(!this.histObj.isEmpty()){
			String reqIDFromRequest = request.getRequestID();
			String operationFromRequest = request.getOperation();
			float amountFromRequest  = request.getAmount();
			String accountNumber = request.getAccount();
			
			Iterator<Entry<Request, ServerReply>> itr = this.histObj.entrySet().iterator();
			while(itr.hasNext()){
				Map.Entry<Request, ServerReply> pairs = (Map.Entry<Request, ServerReply>)itr.next();
				Request req = pairs.getKey();
				if(req.getRequestID().equalsIgnoreCase(reqIDFromRequest) &&
						req.getAccount().equalsIgnoreCase(accountNumber) &&
						req.getOperation().equalsIgnoreCase(operationFromRequest) &&
						req.getAmount()==amountFromRequest){
					return pairs.getValue();
					
				}
			}
		}
		return serverReply;
	}
	
	
	/*
	 * Check for inconsistency with history for a request. 
	 */
	public boolean checkInconsistentTransRequest(Request reqObj){
		boolean isInconsistent = false;
		if(!this.histObj.isEmpty()){
			String reqIDFromRequest = reqObj.getRequestID();
			String operationFromRequest = reqObj.getOperation();
			float amountFromRequest = reqObj.getAmount();
			String accountNumberFromRequest = reqObj.getAccount();
			Set<Request> requestKeys = this.histObj.keySet();
			Iterator<Request> itr = requestKeys.iterator();
			while(itr.hasNext()){
				Request tempReq = (Request) itr.next();
				if(!accountNumberFromRequest.equalsIgnoreCase(tempReq.getAccount()))
					continue;
				if(reqIDFromRequest.equalsIgnoreCase(tempReq.getRequestID()))
					if(!(operationFromRequest.equalsIgnoreCase(tempReq.getOperation())) || 
							!(amountFromRequest == tempReq.getAmount())){
						isInconsistent = true;	
						
					}
				}
			}
		
		return isInconsistent;
		
	}
	
	
}
