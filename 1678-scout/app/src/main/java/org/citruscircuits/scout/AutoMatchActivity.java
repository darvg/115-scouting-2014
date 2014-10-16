package org.citruscircuits.scout;

import java.util.HashMap;
import java.util.Set;

import org.citruscircuits.data_input.DataInputView;
import org.citruscircuits.data_input.Stepper;
import org.citruscircuits.data_input.Toggle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class AutoMatchActivity extends Activity {
	public static LinearLayout section1;
	public static LinearLayout section2;
	public static LinearLayout section3;
	public int backToMenu = 0;

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
				AutoDataCollectionItems.items[0], true);
		DataInputView.fillLayoutWithItems(this, section2Layout,
				AutoDataCollectionItems.items[1], true);
		DataInputView.fillLayoutWithItems(this, section3Layout,
				AutoDataCollectionItems.items[2], true);

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
		getMenuInflater().inflate(R.menu.auto_match, menu);
		return true;
	}

	/*public static void updateHashMap() {
		LinearLayout section1Layout = section1;//(LinearLayout) this
				//.findViewById(R.id.section1Layout);
		LinearLayout section2Layout = section2;//(LinearLayout) this
				//.findViewById(R.id.section2Layout);
		LinearLayout section3Layout =section3; //(LinearLayout) this
				//.findViewById(R.id.section3Layout);

		
		
		int count1 = section1Layout.getChildCount();
		for (int i = 0; i < count1; i++) {
			View view = section1Layout.getChildAt(i);
			if (view instanceof Stepper) {
				DataInputView input = (DataInputView) view;

				String key = input.getDataKey();
				Object dataValue = input.getDataValue();
				SavedData.setAutoData(key, (Integer) dataValue);
			}
			else if (view instanceof Toggle){
				Toggle input = (Toggle) view;
				String key = input.getDataKey();
				Boolean dataValue = (Boolean) input.getDataValue();
				SavedData.setAutoData(key, (dataValue)? 1:0);
			}
		}
		int count2 = section2Layout.getChildCount();
		for (int i = 0; i < count2; i++) {
			View view = section2Layout.getChildAt(i);
			if (view instanceof Stepper) {
				DataInputView input = (DataInputView) view;

				String key = input.getDataKey();
				Object dataValue = input.getDataValue();
				SavedData.setAutoData(key, (Integer) dataValue);
			}
			else if (view instanceof Toggle){
				Toggle input = (Toggle) view;
				String key = input.getDataKey();
				Boolean dataValue = (Boolean) input.getDataValue();
				SavedData.setAutoData(key, (dataValue)? 1:0);
			}
		}
		int count3 = section3Layout.getChildCount();
		for (int i = 0; i < count3; i++) {
			View view = section3Layout.getChildAt(i);
			if (view instanceof Stepper) {
				DataInputView input = (DataInputView) view;

				String key = input.getDataKey();
				Object dataValue = input.getDataValue();
				SavedData.setAutoData(key, (Integer) dataValue);
			}
			else if (view instanceof Toggle){
				Toggle input = (Toggle) view;
				String key = input.getDataKey();
				Boolean dataValue = (Boolean) input.getDataValue();
				SavedData.setAutoData(key, (dataValue)? 1:0);
			}
		}

		Log.e("testing", SavedData.getAutoData("Make High Hot").toString());
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items

		if (item.getItemId() == R.id.action_next) {
			Intent teleIntent = new Intent(this, TeleMatchActivity.class);
			Intent oldIntent = getIntent();
			
			teleIntent.putExtra("teamNumber",
					oldIntent.getIntExtra("teamNumber", 0));
			teleIntent.putExtra("matchNumber",
					oldIntent.getStringExtra("matchNumber"));
			teleIntent.putExtra("channel", oldIntent.getIntExtra("channel", 5));
			teleIntent.putExtra("allianceColor",
					oldIntent.getBooleanExtra("allianceColor", true));
			teleIntent.putExtra("scoutName",
					oldIntent.getStringExtra("scoutName"));

			startActivityForResult(teleIntent, backToMenu);
		}
		return true;
	}

	public void onBluetoothConnectStart() {
		Log.e("sfdsfs", "Bluetooth connect start");
	}

	public void onBluetoothFinish(boolean b) {
		Log.e("sfdsfs", "Bluetooth finish");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == backToMenu) {
	        if (resultCode == RESULT_OK) {
	        	this.setResult(42); //SUICIDE_NOTE
	            finish();
	        }
	    }
	}
}
