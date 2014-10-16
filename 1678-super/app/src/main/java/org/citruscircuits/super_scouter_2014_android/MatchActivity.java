package org.citruscircuits.super_scouter_2014_android;

import java.util.HashMap;

import org.citruscircuits.data_input.DataInputView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MatchActivity extends Activity
{
	public final static String INTENT_EXTRA_TEAM1_INT = "org.citruscircuits.super_scouter_2014_android.team1";
	public final static String INTENT_EXTRA_TEAM2_INT = "org.citruscircuits.super_scouter_2014_android.team2";
	public final static String INTENT_EXTRA_TEAM3_INT = "org.citruscircuits.super_scouter_2014_android.team3";
	public final static String INTENT_EXTRA_MATCH_STRING = "org.citruscircuits.super_scouter_2014_android.match";
	public final static String INTENT_EXTRA_ALLIANCE_COLOR_BOOL = "org.citruscircuits.super_scouter_2014_android.allianceColor";

	public final static boolean ALLIANCE_COLOR_RED = false;
	public final static boolean ALLIANCE_COLOR_BLUE = true;

	public final static int REQUEST_ALLIANCE_DATA = 1;
	public final static String RESULT_ALLIANCE_DATA_KEY = "org.citruscircuits.super_scouter_2014_android.matchData";

	int team1;
	int team2;
	int team3;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_match);
		
		

		LinearLayout team1Layout = (LinearLayout) this.findViewById(R.id.team1Layout);
		LinearLayout team2Layout = (LinearLayout) this.findViewById(R.id.team2Layout);
		LinearLayout team3Layout = (LinearLayout) this.findViewById(R.id.team3Layout);

		// Setup layout params to center columns
		setupLayoutParams();

		// Fill the columns with UI controls
		DataInputView.fillLayoutWithItems(this, team1Layout, DataCollectionItems.items);
		DataInputView.fillLayoutWithItems(this, team2Layout, DataCollectionItems.items);
		DataInputView.fillLayoutWithItems(this, team3Layout, DataCollectionItems.items);

		// Set the team label texts and colors to team info from the MainActivity,
		// and set the title to match number from the MainActivity
		Intent intent = getIntent();
		team1 = intent.getIntExtra(INTENT_EXTRA_TEAM1_INT, 0);
		team2 = intent.getIntExtra(INTENT_EXTRA_TEAM2_INT, 0);
		team3 = intent.getIntExtra(INTENT_EXTRA_TEAM3_INT, 0);

		TextView team1TextView = (TextView) this.findViewById(R.id.team1TextView);
		TextView team2TextView = (TextView) this.findViewById(R.id.team2TextView);
		TextView team3TextView = (TextView) this.findViewById(R.id.team3TextView);

		team1TextView.setText(team1 + "");
		team2TextView.setText(team2 + "");
		team3TextView.setText(team3 + "");

		int color = intent.getBooleanExtra(INTENT_EXTRA_ALLIANCE_COLOR_BOOL, ALLIANCE_COLOR_RED) == ALLIANCE_COLOR_RED ? Color.RED : Color.BLUE;
		team1TextView.setTextColor(color);
		team2TextView.setTextColor(color);
		team3TextView.setTextColor(color);

		setTitle("Match " + intent.getStringExtra(INTENT_EXTRA_MATCH_STRING));
	}

	private void setupLayoutParams()
	{
		LinearLayout team1Layout = (LinearLayout) this.findViewById(R.id.team1Layout);
		LinearLayout team2Layout = (LinearLayout) this.findViewById(R.id.team2Layout);
		LinearLayout team3Layout = (LinearLayout) this.findViewById(R.id.team3Layout);

		// Setup layout params to center columns
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
		params.weight = 0.333333f;

		LinearLayout rootLayout = (LinearLayout) this.findViewById(R.id.rootLayout);
		rootLayout.setWeightSum(1.0f);

		team1Layout.setLayoutParams(params);
		team2Layout.setLayoutParams(params);
		team3Layout.setLayoutParams(params);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.match, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		if (item.getItemId() == R.id.action_done)
		{
			Log.e("logcat sucks", "Done pressed");

			LinearLayout team1Layout = (LinearLayout) this.findViewById(R.id.team1Layout);
			LinearLayout team2Layout = (LinearLayout) this.findViewById(R.id.team2Layout);
			LinearLayout team3Layout = (LinearLayout) this.findViewById(R.id.team3Layout);

			HashMap<String, Object> team1Data = DataInputView.collectDataInLayout(team1Layout);
			HashMap<String, Object> team2Data = DataInputView.collectDataInLayout(team2Layout);
			HashMap<String, Object> team3Data = DataInputView.collectDataInLayout(team3Layout);

			HashMap<String, HashMap<String, Object>> allianceData = new HashMap<String, HashMap<String, Object>>();
			allianceData.put(team1 + "", team1Data);
			allianceData.put(team2 + "", team2Data);
			allianceData.put(team3 + "", team3Data);
			
			// Create an intent with the collected match data
			Intent resultIntent = new Intent();
			resultIntent.putExtra(RESULT_ALLIANCE_DATA_KEY, allianceData); // TODO: Replace with actual data (Dictionary)
			setResult(Activity.RESULT_OK, resultIntent);

			finish();
		}
		return true;
	}

}
