package server;

import java.io.Serializable;

import common.Request;
import common.ServerReply;

public class ContainerRequestServerReply implements Serializable {
	private static final long serialVersionUID = 20L;
	private Request request;
	private ServerReply serverReply;
	
	public ContainerRequestServerReply(Request request, ServerReply serverReply) {
		
		this.request = request;
		this.serverReply = serverReply;
	}

	public ContainerRequestServerReply() {
		// TODO Auto-generated constructor stub
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
	 * @return the serverReply
	 */
	public ServerReply getServerReply() {
		return serverReply;
	}

	/**
	 * @param serverReply the serverReply to set
	 */
	public void setServerReply(ServerReply serverReply) {
		this.serverReply = serverReply;
	}
	
	
	
	
	
}
