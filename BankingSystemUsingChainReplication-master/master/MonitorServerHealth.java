package master;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Set;

import server.ContainerRequestServerReply;
import common.ChainReplicationLogger;
import common.Request;
import common.ServerReply;

/* 
 * Class: MonitorServerHealth
 * Purpose: For each server, a new thread is created which monitors the health of the server
 * using the alive notifications that are sent by the server
 */
public class MonitorServerHealth implements Runnable {
	String serverName= null;
	public MonitorServerHealth(){
		
	}
	
	public void run(){
		ChainReplicationLogger chainReplicationLogger = ChainReplicationLogger.getInstance("Master");
		while(true){
			try {
				Thread.sleep(10000);
				Runtime.getRuntime().freeMemory();
				
			
			// Iterate through the loop and check if any value is less than 10000
		
				for(int i=1; i< MasterProcess.MonitorServerArray.length; i++){
					long currentTimeStamp = System.currentTimeMillis();
					if(MasterProcess.MonitorServerArray[i] != 0.0){
						if((currentTimeStamp - MasterProcess.MonitorServerArray[i]) >= 10000){
							chainReplicationLogger.myLogger.log(Level.SEVERE, "Server "+i+ " is not responding. "
									+ "Concluding it has crashed. Initiating Update Chain Process");
									
							System.out.println("Server "+i+ " is not responding....Some problem there...."+(MasterProcess.MonitorServerArray[i]-currentTimeStamp));
							UpdateChain uc = new UpdateChain("s"+i);
		
							// Set its corresponding timeStamp value again back to zero, so that the monitoring 
							// thread does not see it again
							MasterProcess.MonitorServerArray[i]	= (long) 0.0;				
		
							// Phase 4: Transfers error case 1: 
							// If the master detects that the server which has been dead belongs to the destination bank, 
							// then ask the Tail of Chain 1 to retransmit the transfer request. 
							if(MasterProcess.ongoingTransfer ){
								
							}
							
							
						}
					}
					else {
						//System.out.println("Ignoring for : "+i);
					}
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConcurrentModificationException cme){
				cme.printStackTrace();
			}	

	        
		}
	}
}
