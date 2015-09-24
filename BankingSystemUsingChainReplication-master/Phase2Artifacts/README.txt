*************************************************************
ChainReplication Project README File
---------------------------
Implemented as CSE 535 - Aysnchronous System 
Ashwin Tumma - atumma@cs.stonybrook.edu
Neeraj Devadath - ndevadath@cs.stonybrook.edu

Phase 3 - Fault Tolerance Phase 

*************************************************************

[INSTRUCTIONS]
This section elaborates the steps to run a sample testcase. 

Step 1: Clean the class files. From the root directory of the project, launch the following command
	$make clean

Step 2: Compile the entire code 
	$make 

Step 3: For running test case 12 (Test Case mentioned in testing.txt)
	(a) Open terminal and run Server 1:
		$java server.ServerProcess s1 Testing/12_MoreTransTest/12_MoreTransTest_CheckC2Client.txt
	(b) Open another terminal and run Server 2:
		$java server.ServerProcess s2 Testing/12_MoreTransTest/12_MoreTransTest_CheckC2Client.txt
	(c) Open another terminal and run Server 3:
		$java server.ServerProcess s3 Testing/12_MoreTransTest/12_MoreTransTest_CheckC2Client.txt

	(d) Open another terminal and run Client 2:
		$java client.ClientProcess c2 Testing/12_MoreTransTest/12_MoreTransTest_CheckC2Client.txt
	
Step 4: When requests for Client 2 are completed, the client process is completed and the logs are generated in the logs/ directory with the name of the client and the servers. 
	$ls logs
	 	c1.txt s1.txt s2.txt s3.txt


We start the server processes by running the StartServers or by running each servers individually by specifying the path of the Config file. This test case is designed to test how the server processes come up and they generate log files for themselves.


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

[BUGS AND LIMITATIONS]
- When the client sends the request to the server, before sending new request to the server, it waits to get the response of the earlier request.

[CONTRIBUTIONS]

[OTHER COMMENTS]
- We have written a script for starting all the server processes and client processes by running two Java Programs, but they will not be used for the testing purposes. The reason for the same being that, for testing various scenarios "correctly" and "completely", we ahave to start the processes again. So, if that program were used, then everytime, we will have to kill the processes using 'kill -9' (since in this phase, no server is going to go down!). Having said all of the above, our Chain Replication program will perform correctly when all the servers are running continously and we provide requests from multiple clients.

- Though the log files contain the Send Sequence Number and Receive Sequence Number for each message sent and received respectively by the server, they are not used for any functionality pertaining to server lifetime in this phase. As such the config file also does not have any fields for server lifetime and send & receive sequence numbers.

- The test case numbered (12) in testing.txt is a cumulative case of the functionality pertaining to the bank transactions.

- The test case numbered (14) in the testing.txt is designed to test the randomized requests that are sent by the client.

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

