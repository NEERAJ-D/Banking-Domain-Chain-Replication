*************************************************************
ChainReplication Project README File
--------------------------------------
Implemented as CSE 535 - Aysnchronous System 
Ashwin Tumma - atumma@cs.stonybrook.edu
Neeraj Devadath - ndevadath@cs.stonybrook.edu

Phase III - Fault Tolerance Phase 

*************************************************************

[INSTRUCTIONS]
This section elaborates the steps to run a sample testcase. 

Step 1: Clean the class files. From the root directory of the project, launch the following command
	$make clean

Step 2: Compile the entire code 
	$make 

Step 3: 
	For running the Master Process and the Servers, run the following command: 
	$ ./StartMasterServerProcess.sh <ConfigFileName>
	$ java client.ClientProcess c1 <ConfigFileName>
	
Step 4: When requests for Client 2 are completed, the client process is completed and the logs are generated in the logs/ directory with the name of the client and the servers. 
	$ls logs
	 	c1.txt s1.txt s2.txt s3.txt Master.txt


* testing.txt file is located in the Testing directory
* Config files are located in the Phase3Config directory as well as their corresponding Testing directories
--------------------------------------------------------------------------------------------


[MAIN FILES]
This section lists the main files of our source code. The section only provides a succinct description of the files. For detailed description, kindly refer the source code of the file which contains the purpose of each class file and also who makes use of the file. All files are not included in this section, so as to highlight only the "Main" functionality files.


Common Files:
----------------
common/ChainReplicationLogger.java	: Singleton logger class that logs to the log file of the invoking server/ client
common/ClientReply.java			: Data structure for sending messages to the client
common/ServerReply.java			: Data structure for sending messages between the servers
common/Request.java			: Data structure that contains the requests data 

Client Files:
----------------
client/ClientProcess.java	: 	Main java program to invoke the clients and send requests to the server
client/NetworkDetails.java	: 	Class to get the IP Address and Port numbers of any server

Server Files:
----------------
server/ServerProcess.java		: Main java program to invoke the servers
server/Account.java			: Data structure for storing the account details
server/Bank.java			: Store various accounts and perform bank transactions
server/ListenFromClient.java		: Thread class for listening the requests from the client
server/ListenFromPredecessorServer.java	: Thread class for listening the requests from the predecessors. Essentially, it listens for the update propogations that the predecessor server sends to this server. 
server/ListenFromSucessorServer.java	: Thread class for listening the requests from the successors. Essentially, it listens for the acknowledgement messages that the successor server sends to this server. 
server/ServerObjectPassing.java		: Class responsible for sending messages within servers	
server/SentObjClass			: Class for storing the Sent Objs

Master Files
-----------------------
master/MasterProcess.java		: Main java program for Master
master/UpdateChain.java			: Class for update chain logic
master/MonitorServerHealth.java		: Thread for monitoring the health of the servers

[BUGS AND LIMITATIONS]
- When the client sends the request to the server, before sending new request to the server, it waits to get the response of the earlier request. 
- Due to limitation on the memory of our laptops, we could not monitor how the system scaled up when multiple banks are operational at the same time. This lacuna is because of that fact that we make use of threads in the system, and the configuration of the laptop that we are running is very low.


[CONTRIBUTIONS]
Ashwin : Java Code
Neeraj: DistAlgo Code

[OTHER COMMENTS]
- A comparison of the languages used in this phase is presented in a separate document in the root directory of the submission. 
- All the test cases that were required for this phase are placed in the Testing directory
- All the artifacts that were submitted as a part of Phase II are placed in the Phase2Artifacts directory.

[FUNCTIONALITIES IMPLEMENTED]
1. Specification and creation of multiple chains for different banks 
2. Specification of multiple clients and servers associated to each bank 
3. Generation of itemized requests from the client 
4. Generation of randomized requests from the client to one bank 
5. Retransmission of requests from the client in an event of no response from the server
6. Handling deposit functionality at the server. If the account is not present, a new one is created using the account number specified in the request, and then is populated with the required balance.  
7. Handling withdraw functionality at the server. If the account is not present, a new one is created using the account number specified in the request and a zero balance account. If the request specifies withdrawal value greater than the available funds, then Insufficient Funds reply is sent to the client.
8. Handling of InconsistentWithHistory for deposit and withdraw. If the account number specified is different, then it is not a InconsistentWithHistory request.
9. Handling of duplicate requests for deposit and withdraw. In such cases, the same reply that was sent for the original request is sent back to the client. 
10. A comprehensive log file containing logs for each significant activity done by the client and server. The log file also contains the sequence numbers for sent and received messages by the client and servers.

Additions done in Phase III - Fault Tolerant System
11. Server Lifetime can be specified in the following ways: 
		 * Read the values of the lifeTime, and decide the course of action
		 * Possible values are: 
		 * (1) AfterReceiving(value)
		 * (2) AfterReceiving(random)
		 * (3) AfterSending(value)
		 * (4) AfterSending(random)
		 * (5) Unbounded -> Server never dies
12. Failure of the head 
13. Failure of the tail 
14. Using Acks to remove from SentObjs
15. Failure of Intermediate Server and handling of updates
16. Failure of S, S- and set updates from S-- to S+
17. Failure of S, S+ and set updates to S++
18. Chain Extension
19. Chain Extension with failure of T
20. Abort of Chain Extension when new tail has failed

