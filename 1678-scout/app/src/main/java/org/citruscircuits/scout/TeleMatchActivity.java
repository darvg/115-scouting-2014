package org.citruscircuits.scout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import org.citruscircuits.data_input.DataInputView;
import org.citruscircuits.data_input.Stepper;
import org.citruscircuits.data_input.Toggle;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TeleMatchActivity extends Activity {
	public static LinearLayout section1;
	public static LinearLayout section2;
	public static LinearLayout section3;
	public boolean shouldUpload = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_match);

		Intent intent = getIntent();

		LinearLayout section1Layout = (LinearLayout) this
				.findViewById(R.id.section1Layout);
		LinearLayout section2Layout = (LinearLayout) this
				.findViewById(R.id.section2Layout);
		LinearLayout section3Layout = (LinearLayout) this
				.findViewById(R.id.section3Layout);

		section1 = section1Layout;
		section2 = section2Layout;
		section3 = section3Layout;

		// Setup layout params to center columns
		setupLayoutParams();

		// Fill the columns with UI controls
		DataInputView.fillLayoutWithItems(this, section1Layout,
				TeleDataCollectionItems.items[0], false);
		DataInputView.fillLayoutWithItems(this, section2Layout,
				TeleDataCollectionItems.items[1], false);
		DataInputView.fillLayoutWithItems(this, section3Layout,
				TeleDataCollectionItems.items[2], false);

		// HashMap<String, Integer> teleData = SavedData.teleData;

		int count1 = section1Layout.getChildCount();
		for (int i = 0; i < count1; i++) {
			View view = section1Layout.getChildAt(i);
			if (view instanceof Stepper) {
				Stepper input = (Stepper) view;
				String key = input.getDataKey();
				int val = SavedData.getTeleData(key);
				input.setValue(val);
			}
		}

		int count2 = section2Layout.getChildCount();
		for (int i = 0; i < count2; i++) {
			View view = section2Layout.getChildAt(i);
			if (view instanceof Stepper) {
				Stepper input = (Stepper) view;
				String key = input.getDataKey();
				int val = SavedData.getTeleData(key);
				input.setValue(val);
			}
		}

		int count3 = section3Layout.getChildCount();
		for (int i = 0; i < count3; i++) {
			View view = section3Layout.getChildAt(i);
			if (view instanceof Stepper) {
				Stepper input = (Stepper) view;
				String key = input.getDataKey();
				int val = SavedData.getTeleData(key);
				input.setValue(val);
			}
		}

		TextView teamTextView = (TextView) this.findViewById(R.id.teamTextView);
		
		if (intent.getBooleanExtra("allianceColor", false))
		{
			teamTextView.setTextColor(Color.RED);
		} else {
			teamTextView.setTextColor(Color.BLUE);
		}

		teamTextView.setText(((Integer) intent.getIntExtra("teamNumber", 0))
				.toString());
		String matchNumber = intent.getStringExtra("matchNumber");

		setTitle(matchNumber);
	}

	private void setupLayoutParams() {
		LinearLayout section1Layout = (LinearLayout) this
				.findViewById(R.id.section1Layout);
		LinearLayout section2Layout = (LinearLayout) this
				.findViewById(R.id.section2Layout);
		LinearLayout section3Layout = (LinearLayout) this
				.findViewById(R.id.section3Layout);

		// Setup layout params to center columns
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
				LinearLayout.LayoutParams.MATCH_PARENT);
		params.weight = 0.333333f;

		LinearLayout rootLayout = (LinearLayout) this
				.findViewById(R.id.rootLayout);
		rootLayout.setWeightSum(1.0f);

		section1Layout.setLayoutParams(params);
		section2Layout.setLayoutParams(params);
		section3Layout.setLayoutParams(params);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tele_match, menu);
		return true;
	}

	/*
	 * public static void updateHashMap(){ LinearLayout section1Layout =
	 * section1;//(LinearLayout) this.findViewById(R.id.section1Layout);
	 * LinearLayout section2Layout = section2;//(LinearLayout)
	 * this.findViewById(R.id.section2Layout); LinearLayout section3Layout =
	 * section3;//(LinearLayout) this.findViewById(R.id.section3Layout);
	 * 
	 * HashMap<String, Object> section1Data =
	 * DataInputView.collectDataInLayout(section1Layout); HashMap<String,
	 * Object> section2Data = DataInputView.collectDataInLayout(section2Layout);
	 * HashMap<String, Object> section3Data =
	 * DataInputView.collectDataInLayout(section3Layout);
	 * 
	 * HashMap<String, HashMap<String, Object>> allianceData = new
	 * HashMap<String, HashMap<String, Object>>(); allianceData.put("section1",
	 * section1Data); allianceData.put("section2", section2Data);
	 * allianceData.put("section3", section3Data);
	 * 
	 * int count1 = section1Layout.getChildCount(); for (int i = 0; i < count1;
	 * i++) { View view = section1Layout.getChildAt(i); if (view instanceof
	 * Stepper) { DataInputView input = (DataInputView) view;
	 * 
	 * String key = input.getDataKey(); Object dataValue = input.getDataValue();
	 * SavedData.setTeleData(key, (Integer) dataValue); } else if (view
	 * instanceof Toggle){ Toggle input = (Toggle) view; String key =
	 * input.getDataKey(); Boolean dataValue = (Boolean) input.getDataValue();
	 * SavedData.setTeleData(key, (dataValue)? 1:0); } } int count2 =
	 * section2Layout.getChildCount(); for (int i = 0; i < count2; i++) { View
	 * view = section2Layout.getChildAt(i); if (view instanceof Stepper) {
	 * DataInputView input = (DataInputView) view;
	 * 
	 * String key = input.getDataKey(); Object dataValue = input.getDataValue();
	 * SavedData.setTeleData(key, (Integer) dataValue); } else if (view
	 * instanceof Toggle){ Toggle input = (Toggle) view; String key =
	 * input.getDataKey(); Boolean dataValue = (Boolean) input.getDataValue();
	 * SavedData.setTeleData(key, (dataValue)? 1:0); } } int count3 =
	 * section3Layout.getChildCount(); for (int i = 0; i < count3; i++) { View
	 * view = section3Layout.getChildAt(i); if (view instanceof Stepper) {
	 * DataInputView input = (DataInputView) view;
	 * 
	 * String key = input.getDataKey(); Object dataValue = input.getDataValue();
	 * SavedData.setTeleData(key, (Integer) dataValue); } else if (view
	 * instanceof Toggle){ Toggle input = (Toggle) view; String key =
	 * input.getDataKey(); Boolean dataValue = (Boolean) input.getDataValue();
	 * SavedData.setTeleData(key, (dataValue)? 1:0); } } Log.e("testing",
	 * SavedData.getTeleData("Make High").toString()); }
	 */

	private void writeToFile(String text, String fileName) {
		File root = this.getFilesDir();

		File dir = new File(root + "/match_data/");
		dir.mkdirs();

		File file = new File(dir, fileName);

		try {
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);
			pw.println(text);
			pw.flush();
			pw.close();
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.i("logcat sucks",
					"******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		if (item.getItemId() == R.id.action_done) {
			Log.e("logcat sucks", "Done pressed");

			// updateHashMap();

			setResult(Activity.RESULT_OK);

			//
			Intent intent = getIntent();

			String jsonString = SavedData.getJson(
					intent.getStringExtra("matchNumber"),
					intent.getIntExtra("teamNumber", 0),
					intent.getStringExtra("scoutName"),
					intent.getBooleanExtra("allianceColor", true));

			Log.e("data", jsonString);

			writeToFile(
					jsonString,
					intent.getStringExtra("matchNumber")
							+ "_"
							+ ((Integer) intent.getIntExtra("teamNumber", 0))
									.toString() + ".json");

			uploadBluetooth(jsonString);
			
			Log.e("woo", Boolean.toString(shouldUpload));

			Intent menuIntent = new Intent(this, MainActivity.class);
			menuIntent.putExtra("submit", "yes");
			setResult(RESULT_OK, menuIntent);
			finish();
		}
		return true;
	}
	
	public void uploadBluetooth(String jsonString)
	{

			BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				// Device does not support Bluetooth
				Toaster.makeErrorToast("Bluetooth not connected",
						Toast.LENGTH_LONG);
			}

			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
					.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					ConnectThread connectThread = new ConnectThread(device,
							mBluetoothAdapter, jsonString, this, null, 5,
							true);
					connectThread.start();
				}
			}
	}

	public void showUpload(final String jsonString) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Do you want to upload?");
		// alert.setMessage("Message");

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				uploadBluetooth(jsonString);
			}
		});

		alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		alert.show();
	}

	public void onBluetoothConnectStart() {
		Log.e("sfdsfs", "Bluetooth connect start");
	}

	public void onBluetoothFinish(boolean b) {
		Log.e("sfdsfs", "Bluetooth finish");
	}
}
