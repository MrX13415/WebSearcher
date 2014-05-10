package net.mrx13415.websearcher.net;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import net.mrx13415.websearcher.Start;
import net.mrx13415.websearcher.net.StateObject.StateType;


public class SearcherThread extends Thread{

	public enum ThreadState{
		RUNNING, STOP_REQUESTED, STOPPED;
	}

	public static final long Max_IPv4_Address = 4294967295L; //255.255.255.255
	
	private static ArrayList<SearcherThread> threads = new ArrayList<>();
	private static Queue<StateObject> statebuffer = new LinkedList<StateObject>();
	
	private static int count = 1;
	
	private String protocol = "";
	private int[] ports;
	
	private String startAddress = "1.0.0.0";
	private String endAddress = toDotedDecimalIP(Max_IPv4_Address);;
	
	private boolean running = false;
	private ThreadState state = ThreadState.STOPPED;
	
	private String currentFQDN = "";
	private long currentIPAddress = 0; //ip from 0 (0.0.0.0) - Max_IPv4_Address
	private long lastIPAddress = toLongIP(endAddress); //ip from 0 (0.0.0.0) - Max_IPv4_Address
	private int timeout = 3000;
	
	private long startTime = 0; //ms
	private long timePerRequest = 0; //ms
	private long timeLeft = 0; //ms

	private String percentageStr;
	private int percentage;
	
	private boolean pingmode;
	private boolean loopStartAddress;

	
	public SearcherThread(int count) {
		super();
		SearcherThread.count = count;
		threads.add(this);
		determineName();
	}
	
	public SearcherThread() {
		this(1);
	}
	
	public SearcherThread(int count, String startAddress) {
		this(count, startAddress, 80);
	}
	
	public SearcherThread(int count, String startAddress, String endAddress) {
		this(count, startAddress, endAddress, 80);
	}
	
	public SearcherThread(int count, String startAddress, int... ports) {
		this(count);
		this.startAddress = startAddress;
		this.ports = ports;
		if (startAddress.isEmpty()) this.startAddress = "0.0.0.0";
	}
	
	public SearcherThread(int count, String startAddress, String endAddress, int... ports) {
		this(count, startAddress, ports);
		this.endAddress = endAddress;
		if (endAddress.isEmpty()) this.endAddress = "255.255.255.255";
		lastIPAddress = toLongIP(endAddress);
	}
		
	public int getThreadIndex(){
		return threads.indexOf(this);
	}
	
	public int getThreadSize(){
		return threads.size();
	}

	public static ArrayList<SearcherThread> getThreads() {
		return threads;
	}

	private void determineName(){
		int digits = String.valueOf(count).length();
		setName(String.format("WebSearcher-#%0" + digits + "d", getThreadIndex() + 1));
	}
	
	public void start(){
		if (!threads.contains(this)){
			threads.add(this);
			determineName();
		}
		running = true;
		super.start();
	}
	
	public void stopThread(){
		state = ThreadState.STOP_REQUESTED;
		printState();
		running = false;
	}
	
	private void printState(){
		String t = this.getName() + ": State:\t\t\t" + state;
		System.out.println(t);
		Start.getUserInterface().appendResultLine(t, new Color(34, 76, 177));
	}
	
	@Override
	public void run() {	
		try {
			state = ThreadState.RUNNING;
			printState();
			
			//a little delay so all threads will not start at once ...
			try {
				Thread.sleep((timeout * ports.length) / count * getThreadIndex()); 
			} catch (Exception e) {}
			
			if (startAddress.startsWith("!")){
				startAddress = startAddress.substring(1);
				loopStartAddress = true;
			}
			
			if (startAddress.startsWith("/")){
				startAddress = startAddress.substring(1);
				pingmode = true;
			}
			
			String ip = startAddress;
			
			if (startAddress.toLowerCase().contains("://")){
				ip = startAddress.substring(ip.indexOf("://") + 3);
				protocol = startAddress.substring(0, startAddress.indexOf("://"));
			}
			
			if (ip.contains(":")){
//				port = ip.substring(ip.indexOf(":") + 1);
				ip = ip.substring(0, ip.indexOf(":"));
			}
			
			try {
				currentIPAddress = toLongIP(ip) + getThreadIndex();
				if (loopStartAddress) currentIPAddress -= getThreadIndex();
			} catch (Exception e) {
				InetAddress address = InetAddress.getByName(ip); 
				currentIPAddress = toLongIP(address.getHostAddress()) + getThreadIndex();
			}
			
			
			while (currentIPAddress <= lastIPAddress && running) {

				startTime = System.currentTimeMillis();
						
				for (int port : ports) {
					if (!running) break;
					
					String url = "";
					if (!protocol.isEmpty()){
						url = String.format("%s://%s:%s", protocol, toDotedDecimalIP(currentIPAddress), port);
						if (port == 80) url = String.format("http://%s", toDotedDecimalIP(currentIPAddress));
						if (port == 443) url = String.format("https://%s", toDotedDecimalIP(currentIPAddress));
					}
					
					defineFQDN(toDotedDecimalIP(currentIPAddress));
					
					try {
						Thread.sleep(1);
					} catch (Exception e) {}

					if (pingmode)
						checkAdress(toByteIP(currentIPAddress));
					else if (!url.isEmpty())
						checkAdress(toDotedDecimalIP(currentIPAddress), url);
					else
						checkAdress(toDotedDecimalIP(currentIPAddress), port);
				}
				
				setBar(currentIPAddress);
				
				if (!loopStartAddress) currentIPAddress += getThreadSize();
				
				timePerRequest = System.currentTimeMillis() - startTime;
				timePerRequest = timePerRequest <= 0 ? 1 : timePerRequest;
				timeLeft = timePerRequest * (lastIPAddress - currentIPAddress) / getThreadSize();
			}
		} catch (InvalidParameterException e) {
			String ip = startAddress;
			String url = startAddress;
			
			if (!startAddress.toLowerCase().contains("://")){
				url = String.format("%s://%s", protocol, ip);
			}else{
				ip = ip.split("://")[1];
			}
			checkAdress(ip, url);
		} catch (Exception e) {
			Start.getUserInterface().getStateLabel().setText("ERROR: " + e.getMessage());
			e.printStackTrace();
		}

		state = ThreadState.STOPPED;
		threads.remove(this);
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
		printState();
	}

	
	private void setBar(long i){
		double perc = (100d / (double)lastIPAddress) * i;
		
		String percstr = new DecimalFormat("0.00000000").format(perc);
		
		percentageStr = String.format("%s%%", percstr);
		percentage = (int)perc;	
	}
	
	private void checkAdress(String ip, String address){
		try {
			checkConnection(ip, address);
		} catch (Exception e) {
			addState(new StateObject(StateType.Error, address, currentFQDN, 0, null, null, this, e));
		}
	}
	
	private void checkAdress(byte[] address){
		if (address.length != 4) return;
		
		String straddr = String.format("%s.%s.%s.%s", address[0], address[1], address[2], address[3]);
		
		InetAddress inet = null;
		boolean reachable = false;
		
		try {
			inet = InetAddress.getByAddress(address);
			
			reachable = inet.isReachable(timeout);
			
		    addState(new StateObject(inet.toString(), currentFQDN, reachable, this, null));
		} catch (Exception e) {
			addState(new StateObject(inet != null ? inet.toString() : straddr, currentFQDN, reachable, this, e));
		}
	}
	
	private void checkAdress(String hostname, int port){
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(hostname, port), timeout);
			socket.close();
			
		    addState(new StateObject(hostname, currentFQDN, port, true, this, null));
		} catch (Exception e) {
			addState(new StateObject(hostname, currentFQDN, port, false, this, e));
		}
	}
	
	private void defineFQDN(final String host){
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					currentFQDN = InetAddress.getByName(host).getCanonicalHostName();
				} catch (UnknownHostException e) {
					currentFQDN = "(ERROR: " + e.getMessage() + ")";
				}
			}
		});
		th.setName("GetCanonicalHostName-Thread");
		th.start();
	}
	
	private void addState(StateObject o){
		boolean success = false;
		boolean waitmsg = false;
		do{
			try {
				success = statebuffer.add(o);
			} catch (Exception e) {
				if (waitmsg){
					waitmsg = true;
					//TODO: 
					System.out.println("Waiting for space in queue ...");
				}
			}
		}while (!success);
	}
	
	public boolean checkConnection(String ip, String address) throws MalformedURLException, IOException{
		InternetConnection connection = new InternetConnection(new URL(address), timeout);
				
		int responseCode = -1;
		String responseMsg = "";
		Exception ex = null;
		
		try {
			connection.getConnection().connect();
			
			responseCode = connection.getConnection().getResponseCode();
			responseMsg = connection.getConnection().getResponseMessage();
			
		} catch (Exception e) {ex = e;}
		
		if (responseCode / 100 == 1){
			addState(new StateObject(StateType.Info, ip, currentFQDN, responseCode, responseMsg, connection.getConnection().getURL() , this, ex));
			return true;
		}else if (responseCode / 100 == 2){
			addState(new StateObject(StateType.Success, ip, currentFQDN, responseCode, responseMsg, connection.getConnection().getURL() , this, ex));
			return true;
		}else if (responseCode / 100 == 3){
			addState(new StateObject(StateType.Warning, ip, currentFQDN, responseCode, responseMsg, connection.getConnection().getURL() , this, ex));
			return true;
		}else if (responseCode / 100 == 4){
			addState(new StateObject(StateType.Warning, ip, currentFQDN, responseCode, responseMsg, connection.getConnection().getURL() , this, ex));
			return true;
		}else if (responseCode / 100 == 5){
			addState(new StateObject(StateType.Error, ip, currentFQDN, responseCode, responseMsg, connection.getConnection().getURL() , this, ex));
			return true;
		}else{
			addState(new StateObject(StateType.Error, ip, currentFQDN, responseCode, responseMsg, connection.getConnection().getURL() , this, ex));
		}
		
		return false;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getStartAddress() {
		return startAddress;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getEndAddress() {
		return endAddress;
	}

	public boolean isRunning() {
		return running;
	}

	public ThreadState getThreadState() {
		return state;
	}

	public long getCurrentIPAddress() {
		return currentIPAddress;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getTimePerRequest() {
		return timePerRequest;
	}

	public long getTimeLeft() {
		return timeLeft;
	}

	public static Queue<StateObject> getStatebuffer() {
		return statebuffer;
	}

	public String getPercentageStr() {
		return percentageStr;
	}

	public int getPercentage() {
		return percentage;
	}

	public static String toDotedDecimalIP(long ip){
		long a = ip >> 24;
		long b = (ip >> 16) - (a << 8);
		long c = (ip >> 8)  - (b << 8) - (a << 16);
		long d = ip         - (c << 8) - (b << 16) - (a << 24);
		return String.format("%s.%s.%s.%s", a, b, c, d);
	}
	
	public static long toLongIP(String ip) throws InvalidParameterException{
		try {
			String[] ipOct = ip.split("\\."); //split on dot
			
			if (ipOct.length != 4) throw new Exception();
			
			long a = Long.valueOf(ipOct[0]) << 24;
			long b = Long.valueOf(ipOct[1]) << 16;
			long c = Long.valueOf(ipOct[2]) << 8;
			long d = Long.valueOf(ipOct[3]);
			
			return a+b+c+d;
		} catch (Exception e) {
			throw new InvalidParameterException("Parameter IP is not a valied IPv4 address in the format 0.0.0.0");
		}
		
	}
	
	public static byte[] toByteIP(long ip){
		byte[] bip = new byte[4];
		
		long a = ip >> 24;
		long b = (ip >> 16) - (a << 8);
		long c = (ip >> 8)  - (b << 8) - (a << 16);
		long d = ip         - (c << 8) - (b << 16) - (a << 24);
		
		bip[0] = (byte) a;
		bip[1] = (byte) b;
		bip[2] = (byte) c;
		bip[3] = (byte) d;
		
		return bip;
	}

}
