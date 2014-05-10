package net.mrx13415.websearcher;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.mrx13415.websearcher.gui.UserInterface;
import net.mrx13415.websearcher.net.Searcher;
import net.mrx13415.websearcher.net.SearcherThread;
import net.mrx13415.websearcher.net.StateObject;

public class Start implements ActionListener{

	public static final String ApplicationVersionString = "WebSearcher  v1.4.2 (c)  Oliver Daus 2014";
	
	private Thread guiUpdaterThread;
	private boolean running = false;
	
	private static UserInterface userInterface;
	private static Searcher webSearcher;
	
	public static void main(String[] args) {
		System.out.println(ApplicationVersionString);
		new Start();
	}

	public Start() {
		userInterface = new UserInterface(this);
		initGUIUpdaterThread();
	}
	
	public static UserInterface getUserInterface() {
		return userInterface;
	}

	public Thread getGuiUpdaterThread() {
		return guiUpdaterThread;
	}

	public static Searcher getWebSearcher() {
		return webSearcher;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(getUserInterface().getBtnOK())){
			String startIP = getUserInterface().getCurrentIP().getText();
			String endIP = getUserInterface().getEndIP().getText();
			
			int threadCount = 1;
			int[] ports = new int[]{80}; //must contain at least one item
			
			try {
				threadCount = Integer.valueOf(getUserInterface().getThreadCount().getText());
			} catch (Exception e2) {				
			}finally{
				getUserInterface().getThreadCount().setText(String.valueOf(threadCount));
			}
			
			try {
				String strports = getUserInterface().getPort().getText();
				String[] ps = null;
				int[] newports = null;
					
				if (strports.contains(",") || strports.contains(";") || strports.contains(" ")){
					
					ps = strports.contains(",") ? strports.split(",") : 
						 strports.contains(";") ? strports.split(";") : 
					     strports.contains(" ") ? strports.split(" ") : null;
					     
					newports = new int[ps.length];
					
					for (int i = 0; i < newports.length; i++) {
						newports[i] = Integer.valueOf(ps[i].trim());
					}
				}else if (strports.contains("-")){
					ps = strports.split("-");
					
					int sp = Integer.valueOf(ps[0].trim());
					int ep = Integer.valueOf(ps[1].trim());
					
					newports = new int[Math.abs(ep-sp) + 1];
					for (int i = 0; i < newports.length; i++) {
						newports[i] = (ep > sp ? sp : ep) + i;
					}
				}else{
					newports = new int[]{Integer.valueOf(strports)};
				}
				
				ports = newports;
			} catch (Exception e2) {				
			}finally{
				getUserInterface().getPort().setText(String.valueOf(ports[0]));
			}
			
			webSearcher = new Searcher(startIP, endIP, threadCount, ports);
			webSearcher.start();
		}
		if (e.getSource().equals(getUserInterface().getBtnCancel())){
			getUserInterface().getBtnCancel().setEnabled(false);
			if (webSearcher != null) webSearcher.stop();
		}
	}

	private void initGUIUpdaterThread(){
		if (running){
			running = false;
			
			if (guiUpdaterThread != null){
				String t = guiUpdaterThread.getName() + ": State: \t\t\tSTOP_REQUESTED";
				System.out.println(t);
				Start.getUserInterface().appendResultLine(t, new Color(34, 76, 177));
			}
		}
		
		guiUpdaterThread = new Thread(new Runnable() {
			@Override
			public void run() {
				running = true;
				String t = Thread.currentThread().getName() + ": State: \t\t\tRUNNING";
				System.out.println(t);
				Start.getUserInterface().appendResultLine(t, new Color(34, 76, 177));
				
//				int timeUpdateCounter = 0;
				
				while (running) {
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {}
					
					if (getWebSearcher() == null) continue;
					
					Start.getUserInterface().getThreadState().setValue(SearcherThread.getThreads().size());

					if (SearcherThread.getThreads().size() <= 0){
						Start.getUserInterface().getCurrentIP().setEditable(true);
						Start.getUserInterface().getEndIP().setEnabled(true);
						Start.getUserInterface().getPort().setEnabled(true);
						Start.getUserInterface().getTimeout().setEditable(true);
						Start.getUserInterface().getBtnOK().setEnabled(true);
						Start.getUserInterface().getBtnCancel().setEnabled(false);
						Start.getUserInterface().getThreadCount().setEditable(true);
					}else{
						Start.getUserInterface().getCurrentIP().setEditable(false);
						Start.getUserInterface().getEndIP().setEnabled(false);
						Start.getUserInterface().getTimeout().setEditable(false);
						Start.getUserInterface().getBtnOK().setEnabled(false);
						Start.getUserInterface().getBtnCancel().setEnabled(true);
						Start.getUserInterface().getThreadCount().setEditable(false);
						Start.getUserInterface().getPort().setEnabled(false);
					}
					
//					for (int i = 0; i < Searcher.getThreads().size(); i++) {
//						if (!running) break;
//						if (Searcher.getThreads().get(i) == null) continue;

//						final SearcherThread thread = Searcher.getThreads().get(i);
//						
//						if (timeUpdateCounter >= 25 && i == 0){
//							timeUpdateCounter = 0;
//							
//							final long timePerRequest = thread.getTimePerRequest();
//							long timeLeft = thread.getTimeLeft();
//	
//							//all in ms ...
//							long daysMS = (long) (timeLeft / 1000 / 60 / 60 / 24) * 1000 * 60 * 60 * 24;
//							long hoursMS = (long) (timeLeft / 1000 / 60 / 60) * 1000 * 60 * 60;
//							long minsMS = (long) (timeLeft / 1000 / 60) * 1000 * 60;
//							long secsMS = (long) (timeLeft / 1000) * 1000;
//							
//							final int ms = (int) (timeLeft - secsMS);
//							final int secs = (int) ((secsMS - minsMS) / 1000);
//							final int mins = (int) ((minsMS - hoursMS) / 1000 / 60);
//							final int hours = (int) ((hoursMS - daysMS) / 1000 / 60 / 60);
//							final int days = (int) ((daysMS) / 1000 / 60 / 60 / 24);
//							
//							Start.getUserInterface().getTime().setText(String.format("%03d Days %02d:%02d:%02d.%03d (%04d ms/request)", days, hours, mins, secs, ms, timePerRequest));
//						}
						
//						timeUpdateCounter++;
						
						StateObject so = SearcherThread.getStatebuffer().poll(); 
						
						if (so != null){
							Start.getUserInterface().getCurrentIP().setText(so.getAddress());
							Start.getUserInterface().getStateLabel().setText(so.toString());
							Start.getUserInterface().getStateLabel().setForeground(so.getColor());
							Start.getUserInterface().appendResultLine(so.toString(), so.getColor());
							System.out.println(so.toString());
						}
						
//						Start.getUserInterface().getPercentage().setText(thread.getPercentageStr());
//						Start.getUserInterface().getBar().setValue(thread.getPercentage());	
					}
//				}
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}
				
				t = Thread.currentThread().getName() + ": State: \t\t\tSTOPPED";
				System.out.println(t);
				Start.getUserInterface().appendResultLine(t, new Color(34, 76, 177));
			}
		});
		guiUpdaterThread.setName("UserInterfaceSync");
		guiUpdaterThread.start();
	}
}
