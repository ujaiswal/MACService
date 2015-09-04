package com.cnerg.macservice;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.util.Log;

public class FilePullService extends IntentService {

	public FilePullService() {
		super(FilePullService.class.getName());
	}

	private void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	private boolean fileExists(File[] folder_files, String file_name) {

		if (folder_files != null && folder_files.length != 0) {
			for (File folder_file : folder_files) {
				String folder_file_name = folder_file.getName();
				if (folder_file_name == file_name)
					return true;
			}
		}
		return false;
	}

	private int uploadFile(File sourceFile) {
		String uploadServerUri = getString(R.string.upload_url);
		String uploadName = getString(R.string.upload_name);
		
        int serverResponseCode = -1;
        
		String fileName = sourceFile.getName();
		
        HttpURLConnection conn = null;
        DataOutputStream dos = null;  
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
         
                  
                   // Responses from the server (code and message)
				try {
					// open a URL connection to the Servlet
					 FileInputStream fileInputStream = new FileInputStream(sourceFile);
					 URL url = new URL(uploadServerUri);
					  
					 // Open a HTTP  connection to  the URL
					 conn = (HttpURLConnection) url.openConnection(); 
					 conn.setDoInput(true); // Allow Inputs
					 conn.setDoOutput(true); // Allow Outputs
					 conn.setUseCaches(false); // Don't use a Cached Copy
					 conn.setRequestMethod("POST");
					 conn.setRequestProperty("Connection", "Keep-Alive");
					 conn.setRequestProperty("ENCTYPE", "multipart/form-data");
					 conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
					 conn.setRequestProperty("uploaded_file", fileName); 
					  
					 dos = new DataOutputStream(conn.getOutputStream());
      
					 dos.writeBytes(twoHyphens + boundary + lineEnd); 
					 dos.writeBytes("Content-Disposition: form-data; name=\"" + uploadName +"\";filename=\""
					                           + fileName + "\"" + lineEnd);
					  
					 dos.writeBytes(lineEnd);
      
					 // create a buffer of  maximum size
					 bytesAvailable = fileInputStream.available(); 
      
					 bufferSize = Math.min(bytesAvailable, maxBufferSize);
					 buffer = new byte[bufferSize];
      
					 // read file and write it into form...
					 bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
					    
					 while (bytesRead > 0) {
					      
					   dos.write(buffer, 0, bufferSize);
					   bytesAvailable = fileInputStream.available();
					   bufferSize = Math.min(bytesAvailable, maxBufferSize);
					   bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
					    
					  }
      
					 // send multipart form data necesssary after file data...
					 dos.writeBytes(lineEnd);
					 dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
      
					 serverResponseCode = conn.getResponseCode();
					 
					 //close the streams //
					 fileInputStream.close();
					 dos.flush();
					 dos.close();
				} catch (Exception e) {}
                          
            return serverResponseCode; 
             
       }	
	
	@Override
	protected void onHandleIntent(Intent workIntent) {
		ResultReceiver fileListReceiver = workIntent
				.getParcelableExtra("receiver");

		String data = workIntent.getStringExtra("Work_String");

		File directory = Environment.getExternalStorageDirectory();

		File sync_folder = new File(directory + getString(R.string.sync_folder));
		File backup_folder = new File(directory
				+ getString(R.string.backup_folder));

		if (!sync_folder.exists()) {
			sync_folder.mkdirs();
		}

		if (!backup_folder.exists()) {
			backup_folder.mkdirs();
		}

		File[] sync_folder_files = sync_folder.listFiles();
		File[] backup_folder_files = backup_folder.listFiles();

		if (sync_folder_files != null && sync_folder_files.length != 0) {
			for (File source_file : sync_folder_files) {
				String file_name = source_file.getName();

				if (!fileExists(backup_folder_files, file_name)) {
					/* Send these files to the web service */					
					int responseCode = uploadFile(source_file);
					/* Create backup of the file */
					if(responseCode == 200) {
						File backup_file = new File(backup_folder + "//"
								+ file_name);
						try {
							copy(source_file, backup_file);
						} catch (IOException e) {
							Log.d("MACSERVICE", "Copy not permitted");
						}
					}
				}
			}
		}

		Bundle bundle = new Bundle();
		bundle.putString("resultValue", data);

		fileListReceiver.send(Activity.RESULT_OK, bundle);
	}

}
