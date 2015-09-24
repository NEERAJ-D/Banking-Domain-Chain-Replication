*************************************************************
ChainReplication Project README File
--------------------------------------
Implemented as CSE 535 - Aysnchronous System 
Ashwin Tumma - atumma@cs.stonybrook.edu
Neeraj Devadath - ndevadath@cs.stonybrook.edu

Phase IV - Transfers via Chain Replication
*************************************************************
Source Code Repository:
---------------------------
The entire source code for the entire project is available on bitbucket.org
For accessing the source code, fork and replicate from the following URL: 
https://bitbucket.org/ashwin_tumma/chain-replication

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
* Config files are located in the Phase4Config directory as well as their corresponding Testing directories
* Test Case containing three banks and six clients per bank is marked as Test Case # 7, in the Testing directory
--------------------------------------------------------------------------------------------


[MAIN FILES]
This section lists the main files of our source code. The section only provides a succinct description of the files. For detailed description, kindly refer the source code of the file which contains the purpose of each class file and also who makes use of the file. All files are not included in this section, so as to highlight only the "Main" functionality files.

Shell Scripts:
------------------
The Shell scripts presented in the root directory of the project are only for demo purposes. They easy the process of launching the Master, servers and clients.

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

[OTHER COMMENTS]
- All the test cases that were required for this phase are placed in the Testing directory
- All the artifacts that were submitted as a part of Phase II and Phase III are placed in the Phase2Artifacts and Phase3Artifacts directory respectively.

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

Additions done in Phase IV - Implementation of Transfers (Numbers changed, since Phase III took them!)
21. Handling of Duplicate Transfer Requests
22. Handling of Inconsistent with History Transfer Requests
23. Handling of successful transfer requests from an account in one bank to another account in another bank 
24. Handling of successful transfer operation when the head of the bank 2's chain crashes immediately after receiving the transfer request. The tail of bank 1's chain retransmits the request to the new head of bank 2's chain.
25. Handling of successful transfer operation when the tail of the bank 1's chain crashes immediately after sending the transfer request to the head of bank 2's chain. Next, the head of the bank 2's chain crashes immediately after receiving the transfer request. The new tail of bank 1's chain retransmits the request to the new head of bank 2's chain.
