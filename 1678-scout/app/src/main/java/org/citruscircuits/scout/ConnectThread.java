package org.citruscircuits.scout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.UUID;

import android.R.integer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

class ConnectThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final BluetoothDevice mmDevice;
	private final BluetoothAdapter mBluetoothAdapter;
	private final String MY_UUID = "3da6971e-f039-4c35-acab-dd46321505a1";
	private String toWrite;
	private TeleMatchActivity matchActivity;
	private MainActivity mainActivity;
	private boolean shouldSendMatchData;

	public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter,
			String write, TeleMatchActivity teleMatchActivity,
			MainActivity mainActivity, int channel, boolean sendData) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;
		mmDevice = device;
		mBluetoothAdapter = adapter;
		this.matchActivity = teleMatchActivity;
		this.mainActivity = mainActivity;
		toWrite = write;
		shouldSendMatchData = sendData;

		if (channel == 0) {
			channel = 5;
		}

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code

			Method m = device.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			tmp = (BluetoothSocket) m.invoke(device, channel);

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		mmSocket = tmp;
	}

	public void run() {
		mBluetoothAdapter.cancelDiscovery();

		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			Log.e("stupid logcat", "Waiting to connect to server...");
			if (matchActivity != null) {
				matchActivity.onBluetoothConnectStart();
			}
			if (mainActivity != null) {
				mainActivity.onBluetoothConnectStart();
			}
			mmSocket.connect();
		} catch (IOException connectException) {
			connectException.printStackTrace();
			Log.e("stupid logcat", "Error connecting to super: "
					+ connectException.toString());
			if (matchActivity != null) {
				matchActivity.onBluetoothFinish(false);
			}
			if (mainActivity != null) {
				mainActivity.onBluetoothFinish(false);
			}
			// Unable to connect; close the socket and get out
			try {
				mmSocket.close();

			} catch (IOException closeException) {
				if (matchActivity != null) {
					matchActivity.onBluetoothFinish(false);
				}
				if (mainActivity != null) {
					mainActivity.onBluetoothFinish(false);
				}
			}
			return;
		}

		// Do work to manage the connection (in a separate thread)
		Log.e("stupid logcat", "Do stuff with client socket!");

		try {
			OutputStream output = mmSocket.getOutputStream();
			Log.e("stupid logcat", "writing data!");

			// output.write(toWrite.getBytes());

			if (shouldSendMatchData) { // TODO Logic for sending match data or
										// requesting
				// match schedule
				output.write(0);
				output.write(toWrite.getBytes());
				Log.e("stupid logcat", "Wrote match data");
			} else {
				Log.e("stupid logcat", "Ready to write 1");
				output.write(1);
				output.flush();
				Log.e("stupid logcat", "waiting to get data!");
				InputStream inputStream = mmSocket.getInputStream();
				String scheduleString = convertStreamToString(inputStream);
				mainActivity.saveScheduleToDisk(scheduleString);
				Log.e("stupid logcat", scheduleString);
			}

			output.close();
			mmSocket.close();
			if (matchActivity != null) {
				matchActivity.onBluetoothFinish(true);
			}
			if (mainActivity != null) {
				mainActivity.onBluetoothFinish(true);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("stupid logcat",
					"Error transmitting to super: " + e.toString());
			if (matchActivity != null) {
				matchActivity.onBluetoothFinish(false);
			}
			if (mainActivity != null) {
				mainActivity.onBluetoothFinish(false);
			}
		}
	}

	private String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	// Will cancel an in-progress connection, and close the socket
	public void cancel() {
		try {
			mmSocket.close();
			if (matchActivity != null) {
				matchActivity.onBluetoothFinish(false);
			}
			if (mainActivity != null) {
				mainActivity.onBluetoothFinish(false);
			}
		} catch (IOException e) {
			if (matchActivity != null) {
				matchActivity.onBluetoothFinish(false);
			}
			if (mainActivity != null) {
				mainActivity.onBluetoothFinish(false);
			}
		}
	}
}