package client;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * Class 		: 	NetworkDetails
 * Purpose		: 	All clients use this class for getting the IP address and the port number of 
 * 					the server to which the request needs to be sent.  
 * Who uses this: 	All clients 
 */
public class NetworkDetails {
	private InetAddress ipAddress;
	private int port;
	private String serverName;
	
	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public NetworkDetails(String IPAddress, String port, String serverName){
		try {
			this.ipAddress = InetAddress.getByName(IPAddress);
			this.port = Integer.parseInt(port);
			this.serverName = serverName;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @return the ipAddress
	 */
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
}
