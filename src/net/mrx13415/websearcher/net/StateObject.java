package net.mrx13415.websearcher.net;

import java.awt.Color;
import java.net.URL;

public class StateObject{
	
	public enum StateType{
		Error, Warning, Success, Info
	}
	
	private boolean isping; 
	private boolean isport;
	
	private StateType type = StateType.Info;
	private String address;
	private String hostFQDN;
	private int port; 
	private int responseCode;
	private String responseMessage;
	private URL url;
	private Thread thread;
	private Exception exception;
	private boolean reacheable;
			
	public StateObject(StateType type, String address, String hostFQDN, int responseCode, String responseMessage, URL url, Thread thread, Exception exception) {
		super();
		this.type = type;
		this.address = address;
		this.hostFQDN = hostFQDN;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.url = url;
		this.thread = thread;
		this.exception = exception;
		reacheable = type == StateType.Success || type == StateType.Warning || type == StateType.Info;
	}
	
	public StateObject(String address, String hostFQDN, boolean reacheable, Thread thread, Exception exception) {
		super();
		this.address = address;
		this.hostFQDN = hostFQDN;
		this.reacheable = reacheable;
		this.thread = thread;
		this.exception = exception;
		type = reacheable ? StateType.Success : StateType.Error;
		isping = true;
	}
	
	public StateObject(String address, String hostFQDN, int port, boolean reacheable, Thread thread, Exception exception) {
		super();
		this.address = address;
		this.hostFQDN = hostFQDN;
		this.port = port;
		this.reacheable = reacheable;
		this.thread = thread;
		this.exception = exception;
		type = reacheable ? StateType.Success : StateType.Error;
		isport = true;
	}

	public boolean isReacheable() {
		return reacheable;
	}

	public String getAddress() {
		return address;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public URL getUrl() {
		return url;
	}

	public Thread getThread() {
		return thread;
	}

	public Exception getException() {
		return exception;
	}

	public StateType getType() {
		return type;
	}
	
	public Color getColor(){
		switch (type) {
		case Error:
			return new Color(177, 34, 34);
		case Warning:
			return new Color(255, 128, 0);
		case Success:
			return new Color(34, 177, 76);
		case Info:
			return new Color(34, 76, 177);
		default:
			return new Color(0, 0, 0);
		}
	}
	
	@Override
	public String toString() {
		if (isping && exception != null) 
			return String.format("\"%s\": Ping: %15s: %50s: ERROR: %s",
					thread.getName(),
					address,
					hostFQDN,
					exception.getMessage());
		else if (isping)
			return String.format("%s: Ping: %15s: %50s: %s",
					thread.getName(),
					address,
					hostFQDN,
					reacheable ? "OK" : "ERROR: Timed out or unreachable network");
		else if (isport && exception == null)
			return String.format("%s: %15s: %50s: Port: %s: %s",
					thread.getName(),
					address,
					hostFQDN,
					port,
					reacheable ? "OPENED" : "CLOSED");
		else if (isport)
			return String.format("%s: %15s: %50s: Port: %s: %s ERROR: %s",
					thread.getName(),
					address,
					hostFQDN,
					port,
					reacheable ? "OPENED" : "CLOSED",
					exception.getMessage());
		else if (url == null)
			return String.format("%s: %15s: %50s: ERROR: %s",
					thread.getName(),
					address,
					hostFQDN,
					exception.getMessage());
		else if (exception == null && responseCode > 0)
			return String.format("%s: %15s: %50s: %3s %-23s: %s",
					thread.getName(),
					address,
					hostFQDN,
					responseCode,
					responseMessage,
					url);
		else if (responseCode < 0 && exception != null)
			return String.format("%s: %15s: %50s: ERROR: %s",
					thread.getName(),
					address,
					hostFQDN,
					exception.getMessage());
		else
			return String.format("%s: %15s: %50s: %3s %-23s: %s: ERROR: %s",
					thread.getName(),
					address,
					hostFQDN,
					responseCode,
					responseMessage,
					url,
					exception);
		
	}
}
