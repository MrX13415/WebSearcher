package net.mrx13415.websearcher.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;


public class InternetConnection {

	private HttpURLConnection connection;
	
	public InternetConnection(URL url, int timeout) throws IOException {
		creatConnection(url, null, timeout);
	}
	
	public InternetConnection(URL url, Proxy proxyServer, int timeout) throws IOException {
		creatConnection(url, proxyServer, timeout);
	}
	
	public InternetConnection(URL url, Proxy proxyServer) throws IOException {
		creatConnection(url, proxyServer, 3000);
	}
	
	public InternetConnection(URL url) throws IOException {
		creatConnection(url, null, 3000);
	}
	
	public static void setRequestPropertys(HttpURLConnection connection, int timeout){		
		connection.setReadTimeout(timeout);
		connection.setConnectTimeout(timeout);
		connection.setRequestProperty("Accept", "text/*,application/*;q=0.9,*/*;q=0.8");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.setRequestProperty("Connection","keep-alive");
		connection.setRequestProperty("Proxy-Connection", "keep-alive");
	}
	
	public HttpURLConnection creatConnection(URL url, Proxy proxyServer, int timeout) throws IOException{
		
		if (proxyServer != null) connection = (HttpURLConnection) url.openConnection(proxyServer);
		else  connection = (HttpURLConnection) url.openConnection();
		
		if (connection instanceof HttpsURLConnection){
			((HttpsURLConnection) connection).setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
		}
			
		
        connection.setRequestMethod("GET");  
        connection.setDoOutput(true);  
        connection.setDoInput(true);  
        connection.setUseCaches(false);  
        connection.setRequestProperty ( "Content-type","text/xml" );   
        connection.setAllowUserInteraction(false);  
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10 (.NET CLR 3.5.30729)");  
        
		setRequestPropertys(connection, timeout);
				
		return connection;
	}
	
	public HttpURLConnection getConnection() {
		return connection;
	}

	public void printHeaderFields(){
		String output = "";
		for (String key : connection.getHeaderFields().keySet()) {
			
			String values = "";
			for (String value : connection.getHeaderFields().get(key)) {
				values += value.trim() + "; "; 
			}
			
			//format ...
			values = values.trim();
			if (values.endsWith(";")) values = values.substring(0, values.length() - 1);
			
			if (key != null && !key.trim().isEmpty()) output += key.trim() + ": " + values + "\n";
			else output += values + "\n";
		}
		System.out.println("==== HeaderFields ====\n" + output + "\n======================\n");
	}
	
	public void printRequestProperties(){
		String output = "";
		for (String key : connection.getRequestProperties().keySet()) {
			
			String values = "";
			for (String value : connection.getRequestProperties().get(key)) {
				values += value.trim() + "; "; 
			}
			
			//format ...
			values = values.trim();
			if (values.endsWith(";")) values = values.substring(0, values.length() - 1);
			
			if (key != null && !key.trim().isEmpty()) output += key.trim() + ": " + values + "\n";
			else output += values + "\n";
		}
		System.out.println("==== RequestProperties ====\n" + output + "\n===========================\n");
	}	
}
