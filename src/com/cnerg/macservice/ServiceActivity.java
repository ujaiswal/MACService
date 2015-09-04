package com.cnerg.macservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ServiceActivity extends Activity implements OnClickListener {

	Button mStart;
	Button mStop;
	
	FileListResultReciever mFileListReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service);
		
		mStart = (Button) findViewById(R.id.button_start);
		mStart.setOnClickListener(this);
		mStop = (Button) findViewById(R.id.button_stop);
		mStop.setOnClickListener(this);
		setupServiceReceiver();
	}

	@Override
	public void onClick(View arg) {
		switch(arg.getId()) {
			case R.id.button_start:
				Intent mServiceIntent = new Intent(this, FilePullService.class);
				mServiceIntent.putExtra("Work_String", "Files synced!!");
				mServiceIntent.putExtra("receiver",  mFileListReceiver);
				startService(mServiceIntent);
				break;
			case R.id.button_stop:
				Toast.makeText(this, "Stop Pressed!!", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}
		
	}
	
	// Setup the callback for when data is received from the service
	  public void setupServiceReceiver() {
	    mFileListReceiver = new FileListResultReciever(new Handler());
	    // This is where we specify what happens when data is received from the service
	    mFileListReceiver.setReceiver(new FileListResultReciever.Receiver() {
	      @Override
	      public void onReceiveResult(int resultCode, Bundle resultData) {
	        if (resultCode == RESULT_OK) {
	          String resultValue = resultData.getString("resultValue");
	          Toast.makeText(ServiceActivity.this, resultValue, Toast.LENGTH_SHORT).show();
	        }
	      }
	    });
	  }
	
}
