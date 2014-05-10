package net.mrx13415.websearcher.net;

import java.io.*;
import java.net.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


class Download extends Thread  {

	// Max size of download buffer.
	private static final int MAX_BUFFER_SIZE = 1024 * 64; // 64K

	// These are the status names.
	public static final String STATES[] = { "Downloading", "Paused",
			"Complete", "Cancelled", "Error" };

	// These are the state codes.
	public static final int DOWNLOADING = 0;
	public static final int INIT = -1;
	public static final int PAUSED = 1;
	public static final int COMPLETE = 2;
	public static final int CANCELLED = 3;
	public static final int ERROR = 4;

	//error
	private Exception error;
	private int code;
	private String responseMsg; 
	
	private HttpURLConnection connection;
	private long size; // size of download in bytes
	private long alternativeSize = -1;
	private int downloaded; // number of bytes downloaded
	private int status = INIT; // current status of download
	private long startTime;
	private long endTime;
	private long time;
	
	//for 
	private boolean downloadInFile = true;
	private String path = "";
	private String fileContent = "";

	// Constructor for Download.
	public Download(HttpURLConnection connection) {
 		this.connection = connection;
		size = -1;	
		downloaded = 0;
	}
	
	// Get this download's URL.
	public String getUrl() {
		return connection.getURL().toString();
	}

	// Get this download's size.
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		alternativeSize = (size <= 0 ? -1 : size);
	}

	public float getProgress() {
		float p = ((float) (100f / (float)size) * (float)downloaded);
		return (p > 100f ? 100f : p);
	}
	
	// Get this download's progress.
	public float getSpeed() {
		return (float)downloaded / (float)(time / 1000000000f);
	}
	
	// Get this download's progress.
	public String getSpeedFormated() {
		float speed = (float)downloaded / (time / 1000000000f);
		return getBytesUnitString((long)speed) + "/s";
	}
		
	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	//
	public long getTimeNanoSeconds() {
		return time;
	}
	
	//
	public double getTime() {
		return time / 1000000d;
	}

	// Get this download's status.
	public int getStatus() {
		return status;
	}

	public int getDownloaded() {
		return downloaded;
	}

	// Pause this download.
	public void pause() {
		status = PAUSED;
	}

	// Start this download.
	public void start() {
		super.start();
		//download();
	}
		
	// Resume this download.
//	public void resume() {
//		status = DOWNLOADING;
//		stateChanged();
//		download();
//	}

	// Cancel this download.
	public void cancel() {
		status = CANCELLED;
	}

	// Mark this download as having an error.
	private void error(Exception e){
		error = e;
		status = ERROR;
		System.err.println(e);
	}

	private boolean isError() {
		return status == ERROR;
	}
	public Exception getError() {
		return error;
	}
	
	public void setError(Exception error) {
		this.error = error;
	}

	public boolean isDwonloading(){
		return status == DOWNLOADING;
	}
	
	public boolean isInProgress(){
		return status != Download.COMPLETE && status != Download.ERROR;
	}
	
	public boolean isDownloadInFile() {
		return downloadInFile;
	}

	public int getCode() {
		return code;
	}
	
	public String getResponseMsg() {
		return responseMsg;
	}

	public void setDownloadInFile(boolean downloadInFile) {
		this.downloadInFile = downloadInFile;
	}

	public String getFileContent() {
		return fileContent;
	}

	// Get file name portion of URL.
	public String getFileName(URL url) {
		String fileName = url.getFile();
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}
		
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	// Download file.
	public void run() {

		status = INIT;
		RandomAccessFile file = null;
		InputStream stream = null;

		try {			
			// Make sure response code is in the 200 range.
			int responseCode = code = connection.getResponseCode();
			responseMsg = connection.getResponseMessage();
			
			if (responseCode / 100 != 2) {
				error(new Exception("bad connection: " + responseCode + ": " + responseMsg));
			}

			// Check for valid content length.
			int contentLength = connection.getContentLength();

			/*
			 * Set the size for this download if it hasn't been already set.
			 */
			if (size == -1 && !isError()) {
				size = contentLength;
				size = (size == -1 ? alternativeSize : size);
			}
			
			// Open file and seek to the end of it.
			if (downloadInFile && !isError()) {
				
				if (path != null){
					new File(path).mkdirs();
					file = new RandomAccessFile(path + File.separator + getFileName(connection.getURL()), "rw");
				}else{
					file = new RandomAccessFile(getFileName(connection.getURL()), "rw");
				}
				file.seek(downloaded);
			}
			
			if (!isError()) {
				
				//get content encoding ...
				String contentEncoding = connection.getHeaderField("Content-Encoding");
				if (contentEncoding == null) contentEncoding = "";
				
				if (contentEncoding.equalsIgnoreCase("gzip")){
					//determine 'gzip' encoding
					stream = new GZIPInputStream(connection.getInputStream());
					
				}else if (contentEncoding.equalsIgnoreCase("deflate")){
					//determine 'deflate' encoding
					stream = new InflaterInputStream(connection.getInputStream(), new Inflater(true));

				}else{
					//no encoding ...
					stream = connection.getInputStream();
				}
	
				status = DOWNLOADING;
				int read = 0;
				
				startTime = System.nanoTime();
				time = System.nanoTime() - startTime;
				
				while (status == DOWNLOADING && !isError()) {
					/*
					 * Size buffer according to how much of the file is left to
					 * download.
					 */
					byte buffer[];

					buffer = new byte[MAX_BUFFER_SIZE];

					// Read from server into buffer.
					read = stream.read(buffer);
					if (read == -1)
						break;
	
					// Write buffer to file.
					if (downloadInFile){
						file.write(buffer, 0, read);
					}else{
						byte[] contentBytes = new byte[read];
						for (int byteIndex = 0; byteIndex < read; byteIndex++) {
							contentBytes[byteIndex] = buffer[byteIndex]; 
						}
						fileContent += new String(contentBytes, "UTF-8");
					}
					
					time = System.nanoTime() - startTime;
							
					downloaded += read;
				}
				
				endTime = System.nanoTime();
				
				downloaded += read;
				
				if (downloaded <= 0) new IOException("null bytes receaved");
				/*
				 * Change status to complete if this point was reached because
				 * downloading has finished.
				 */
				if (status == DOWNLOADING) {
					status = COMPLETE;
				}
			}
		} catch (Exception e) {
			error(e);	
		} finally {
			// Close file.
			if (file != null) {
				try {
					file.close();
				} catch (Exception e) {
				}
			}

			// Close connection to server.
			if (stream != null) {
				try {
					stream.close();
					connection.disconnect();
				} catch (Exception e) {
				}
			}
		}
	}
	
	/**
	 * Formats the given bytes to the greatest possible unit as long as the result is not less then 1<p>
	 * <b>The Supported units are:</b> Byte, Kilobyte (KB), Megabyte (MB), Gigabyte (GB), Terabyte (TB), Petabyte (PB), Exabyte (EB)
	 * <p>Zetabyte (ZB) and Yotabyte (YB) are not supported due to the restriction of the data type '<b>long</b>' to 8.0 Exabyte<p>
	 * @param bytes the number to format 
	 * @return
	 * The given bytes in a formated String<p>
	 * e.g.: 643858234 -> 614.03 MB<p> 
	 */
	public static String getBytesUnitString(long bytes){
        String unit = "Byte";
        double unitbytes = bytes;
        
        final String kilobyteUnit = "KB";
        final String megabyteUnit = "MB";
        final String gigabyteUnit = "GB";
        final String terabyteUnit = "TB";
        final String petabyteUnit = "PB";
        final String exabyteUnit = "EB";
        
        final long kilobyte = 1024; // 1KB
        final long megabyte = kilobyte * 1024; // 1MB
        final long gigabyte = megabyte * 1024; // 1GB
        final long terabyte = gigabyte * 1024; // 1TB
        final long petabyte = terabyte * 1024; // 1PB
        final long exabyte = petabyte * 1024; // 1EB
        
        if(bytes >= kilobyte) { unit = kilobyteUnit; unitbytes = (double)bytes / (double)kilobyte; }
        if(bytes >= megabyte) { unit = megabyteUnit; unitbytes = (double)bytes / (double)megabyte; }
        if(bytes >= gigabyte) { unit = gigabyteUnit; unitbytes = (double)bytes / (double)gigabyte; }
        if(bytes >= terabyte) { unit = terabyteUnit; unitbytes = (double)bytes / (double)terabyte; }
        if(bytes >= petabyte) { unit = petabyteUnit; unitbytes = (double)bytes / (double)petabyte; }
        if(bytes >= exabyte) { unit = exabyteUnit; unitbytes = (double)bytes / (double)exabyte; }
        
        unitbytes = Math.round(unitbytes * 100d) / 100d;
        
        return unitbytes + " " + unit;
    }
}
