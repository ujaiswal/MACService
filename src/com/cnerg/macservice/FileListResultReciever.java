package com.cnerg.macservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class FileListResultReciever extends ResultReceiver {
	// Defines our event interface for communication
	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	Receiver receiver;

	public FileListResultReciever(Handler handler) {
		super(handler);
	}

	// Setter for assigning the receiver
	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	// Delegate method which passes the result to the receiver if the receiver
	// has been assigned
	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (receiver != null) {
			receiver.onReceiveResult(resultCode, resultData);
		}
	}

}
