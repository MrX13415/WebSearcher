package net.mrx13415.websearcher.net;

import java.util.ArrayList;

import net.mrx13415.websearcher.Start;
import net.mrx13415.websearcher.net.SearcherThread.ThreadState;


public class Searcher {

	private volatile int threadCount = 1;
	private String startAddress;
	private String endAddress;
	private int[] ports;
		
	private Thread startThread;
	private Thread stopThread;
	
	public Searcher() {
		this.startAddress = "0.0.0.0";
	}
	
	public Searcher(String startAddress, String endAddress) {
		this(startAddress, endAddress, new int[]{80});
	}
	
	public Searcher(String startAddress, String endAddress, int... ports) {
		this();
		this.startAddress = startAddress;
		this.endAddress = endAddress;
		this.ports = ports;
		if (startAddress.isEmpty()) this.startAddress = "1.0.0.0";
		if (endAddress.isEmpty()) this.endAddress = "255.255.255.255";
	}
	
	public Searcher(String startAddress, String endAddress, int threadCount, int... ports) {
		this(startAddress, endAddress, ports);
		this.threadCount = threadCount;
	}
	
	public static ArrayList<SearcherThread> getThreads() {
		return SearcherThread.getThreads();
	}

	public int getThreadCount(){
		return threadCount;
	}
	
	public synchronized void start(){
		stop();
		
		startThread = new Thread(new Runnable() {
			
			@Override
			public void run() {

				synchronized (stopThread) {
					try {
						stopThread.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				Start.getUserInterface().getThreadState().setMaximum(threadCount);
				Start.getUserInterface().getThreadState().setValue(0);

				try {
					Thread.sleep(250);
				} catch (Exception e) {}
				

				int timeout = 3000;
				
				try {
					timeout = Integer.valueOf(Start.getUserInterface().getTimeout().getText());
				} catch (Exception e) {
				}finally{
					Start.getUserInterface().getTimeout().setText(String.valueOf(timeout));
				}
				
				for (int i = 0; i < threadCount; i++) {
					SearcherThread thread = new SearcherThread(threadCount, startAddress, endAddress, ports);
					thread.setTimeout(timeout);
					thread.start();
					
					try {
						Thread.sleep(1);
					} catch (Exception e) {}
					
					Start.getUserInterface().getThreadState().setValue(i + 1);
				}
				//-----------------------------
			}
		});
		startThread.start();
	}
	
	public synchronized void stop(){
		stopThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Start.getUserInterface().getThreadState().setMaximum(threadCount);
				Start.getUserInterface().getThreadState().setValue(getThreads().size());
				
				SearcherThread[] threads = new SearcherThread[0];
				threads = getThreads().toArray(threads);
				
				for (int i = threads.length - 1; i >= 0 ; i--) {
					SearcherThread th = threads[i];
					
					if (th != null){
						th.stopThread();
					}
				}
				
				for (int i = threads.length - 1; i >= 0 ; i--) {
					SearcherThread th = threads[i];
					
					if (th != null){
						while (th.getThreadState() != ThreadState.STOPPED){
							try {
								Thread.sleep(1);
							} catch (Exception e) {}
						};
					}
					
					try {
						Thread.sleep(1);
					} catch (Exception e) {}
					
					Start.getUserInterface().getThreadState().setValue(i);
				}
				
				threads = null;
				getThreads().clear();
				
				try {
					Thread.sleep(250);
				} catch (Exception e) {}
				
				synchronized (stopThread) {
					stopThread.notify();
				}
			}
		});
		stopThread.start();
	}
	
}
