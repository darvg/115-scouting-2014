package org.citruscircuits.super_scouter_2014_android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class MainActivity extends Activity {

	public DbxAccountManager mDbxAcctMgr;
	public DbxFileSystem dbxFs;
	static final int REQUEST_LINK_TO_DBX = 234; // This value is up to you
	MediaPlayer mp;

	Competition competition;
	List<Match> qualificationSchedule;
	private int matchIndex = 0;

	boolean isRed = true;
	boolean allowOverride = false;
	final String competitionCode = "Newton";
	int portNumber = 5;

	AcceptThread acceptingThread;

	private String jsonSchedule;

	public final String PREFERENCES_FILE = "org.citruscircuits.super_scouter_2014_android.PreferencesFile";
	public final String PREFERENCES_SCHEDULE_KEY = "schedule";
	public final String PREFERENCES_COLOR_KEY = "color";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize and link Dropbox
		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(),
				"zpd7queicyk4jhn", "r7xnsh8jiojt22v");

		if (!mDbxAcctMgr.hasLinkedAccount()) {
			Log.e("log", "Starting DB link");
			mDbxAcctMgr.startLink((Activity) this, REQUEST_LINK_TO_DBX);
		} else {
			try {
				dbxFs = DbxFileSystem
						.forAccount(mDbxAcctMgr.getLinkedAccount());
			} catch (Unauthorized e) {
				Log.e("fml", "Something decided to break.");
				e.printStackTrace();
			}
		}

		// Set the title
		setTitle("Main Menu");

		createBluetoothThread();

		jsonSchedule = getScheduleFromDisk();
		if (jsonSchedule != null) {
			// Parse jsonSchedule and put into array
			Log.e("stupid logcat", "Loading schedule from saved JSON");
			qualificationSchedule = new ArrayList<Match>();
			try {
				JSONArray schedule = new JSONObject(jsonSchedule)
						.getJSONArray("qualificationSchedule");
				for (int i = 0; i < schedule.length(); i++) {
					JSONObject matchJsonObject = schedule.getJSONObject(i);
					Match match = new Match();
					match.setMatchNumber(matchJsonObject
							.getString("matchNumber"));
					match.setRedAllianceTeamNumbers(matchJsonObject
							.getJSONArray("redAlliance"));
					match.setBlueAllianceTeamNumbers(matchJsonObject
							.getJSONArray("blueAlliance"));

					qualificationSchedule.add(match);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			updateMatchAndTeamUI(true);

		} else {

			// Fetch the entire match schedule for the given competition
			final ParseQuery<Competition> competitionQuery = new ParseQuery<Competition>(
					Competition.class);
			competitionQuery.whereEqualTo("code", competitionCode);
			competitionQuery
					.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						competition = competitionQuery.getFirst();
						Log.e("stupid logcat", competition.toString());
						qualificationSchedule = competition
								.findAndFetchQualificationSchedule();
						Log.e("stupid logcat", "Schedule: "
								+ qualificationSchedule.toString());

						jsonSchedule = generateJSON();
						saveScheduleToDisk(jsonSchedule);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								updateMatchAndTeamUI(true);
							}
						});
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}).start();
		}

		isRed = Boolean.valueOf(getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString(PREFERENCES_COLOR_KEY, null));

		updateMatchAndTeamUI(true);
		
		EditText matchTextField = (EditText)findViewById(R.id.matchTextField);
		matchTextField.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 1) {
					matchIndex = Integer.parseInt(s.subSequence(1, s.length()).toString()) - 1;
					updateMatchAndTeamUI(false);
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

		setMatchOverride(false);

	}

	private String generateJSON() {
		JSONArray matchArray = new JSONArray();

		for (int i = 0; i < qualificationSchedule.size(); i++) {
			Match match = qualificationSchedule.get(i);
			List<Team> redAllianceTeams = match.getRedAlliance();
			List<Team> blueAllianceTeams = match.getBlueAlliance();

			JSONArray redAllianceArray = new JSONArray();
			redAllianceArray.put(redAllianceTeams.get(0).getNumber());
			redAllianceArray.put(redAllianceTeams.get(1).getNumber());
			redAllianceArray.put(redAllianceTeams.get(2).getNumber());

			JSONArray blueAllianceArray = new JSONArray();
			blueAllianceArray.put(blueAllianceTeams.get(0).getNumber());
			blueAllianceArray.put(blueAllianceTeams.get(1).getNumber());
			blueAllianceArray.put(blueAllianceTeams.get(2).getNumber());

			JSONObject matchObject = new JSONObject();
			try {
				matchObject.put("matchNumber", match.getMatchNumber());
				matchObject.put("redAlliance", redAllianceArray);
				matchObject.put("blueAlliance", blueAllianceArray);

				matchArray.put(matchObject);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Log.e("FML", "Match: " +
			// qualificationSchedule.get(i).toString());
		}

		JSONObject scheduleObject = new JSONObject();
		try {
			scheduleObject.put("qualificationSchedule", matchArray);
			return scheduleObject.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	private void saveScheduleToDisk(String json) {
		SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(PREFERENCES_SCHEDULE_KEY, json);
		editor.commit();
	}

	private String getScheduleFromDisk() {
		return getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
				.getString(PREFERENCES_SCHEDULE_KEY, null);
	}

	private void createBluetoothThread() {
		// Initiate Bluetooth listening
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null) {
			// Device does not support Bluetooth
			Toaster.makeErrorToast("Device does not support Bluetooth",
					Toast.LENGTH_LONG);
		} else {
			if (acceptingThread != null) {
				acceptingThread.cancel();
			}
			acceptingThread = new AcceptThread(adapter, this, portNumber,
					getScheduleFromDisk());
			acceptingThread.start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_set_match_number) {
			toggleMatchOverride();

		} else if (id == R.id.action_set_color) {

			isRed = !isRed;

			updateMatchAndTeamUI(true);

			SharedPreferences preferences = getSharedPreferences(
					PREFERENCES_FILE, Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString(PREFERENCES_COLOR_KEY, Boolean.toString(isRed));
			editor.commit();

			Log.e("logcat sucks",
					getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
							.getString(PREFERENCES_COLOR_KEY, null));

		} else if (id == R.id.action_set_bluetooth_channel) {

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Set Bluetooth Channel");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			final MainActivity myThis = this;
			final EditText matchTextField = (EditText) findViewById(R.id.matchTextField);
			final EditText team1TextField = (EditText) findViewById(R.id.team1TextField);
			final EditText team2TextField = (EditText) findViewById(R.id.team2TextField);
			final EditText team3TextField = (EditText) findViewById(R.id.team3TextField);
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();

							portNumber = Integer.parseInt(value);
							createBluetoothThread();

							InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(input.getWindowToken(),
									0);
							imm.hideSoftInputFromWindow(
									matchTextField.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(
									team1TextField.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(
									team2TextField.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(
									team3TextField.getWindowToken(), 0);
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(input.getWindowToken(),
									0);
							imm.hideSoftInputFromWindow(
									matchTextField.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(
									team1TextField.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(
									team2TextField.getWindowToken(), 0);
							imm.hideSoftInputFromWindow(
									team3TextField.getWindowToken(), 0);
						}
					});

			alert.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {

					InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(
							matchTextField.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(
							team1TextField.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(
							team2TextField.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(
							team3TextField.getWindowToken(), 0);
				}
			});

			alert.show();

		}
		return true;
	}

	public void setMatchIndex(int index) {
		if (index < qualificationSchedule.size()) {
			matchIndex = index;
			updateMatchAndTeamUI(true);
		} else {
			Toaster.makeErrorToast("No more matches to scout!",
					Toast.LENGTH_LONG);
		}
	}

	public void incrementMatchIndex() {
		setMatchIndex(matchIndex + 1);
	}

	public void toggleMatchOverride() {
		setMatchOverride(!allowOverride);

	}

	public void setMatchOverride(boolean override) {
		allowOverride = override;

		EditText matchTextField = (EditText) findViewById(R.id.matchTextField);
		EditText team1TextField = (EditText) findViewById(R.id.team1TextField);
		EditText team2TextField = (EditText) findViewById(R.id.team2TextField);
		EditText team3TextField = (EditText) findViewById(R.id.team3TextField);

		matchTextField.setEnabled(allowOverride);
		team1TextField.setEnabled(allowOverride);
		team2TextField.setEnabled(allowOverride);
		team3TextField.setEnabled(allowOverride);
	}

	// Updates and fills the match and team number text fields with data
	public void updateMatchAndTeamUI(boolean updateMatch) {

		// Set the correct text and color for the alliance label
		TextView allianceText = (TextView) findViewById(R.id.allianceColorTextView);
		allianceText.setText(isRed ? "Red Alliance" : "Blue Alliance");
		allianceText.setTextColor(isRed ? Color.RED : Color.BLUE);

		if (qualificationSchedule == null) {
			return;
		}

		// Find text fields
		EditText matchTextField = (EditText) findViewById(R.id.matchTextField);
		EditText team1TextField = (EditText) findViewById(R.id.team1TextField);
		EditText team2TextField = (EditText) findViewById(R.id.team2TextField);
		EditText team3TextField = (EditText) findViewById(R.id.team3TextField);

		// Get the alliance
		if(matchIndex >= qualificationSchedule.size()) {
			return;
		}
		
		Match currentMatch = qualificationSchedule.get(matchIndex);
		List<Integer> teamNumbers;
		if (isRed) {
			teamNumbers = currentMatch.redTeamNumbers;
		} else {
			teamNumbers = currentMatch.blueTeamNumbers;
		}

		// Set the match number text
		if(updateMatch) {
			matchTextField.setText(currentMatch.getMatchNumber());
		}

		// Set the team number text
		team1TextField.setText(teamNumbers.get(0) + "");
		team2TextField.setText(teamNumbers.get(1) + "");
		team3TextField.setText(teamNumbers.get(2) + "");
	}

	// When the scout button is tapped, read the team and match number info from
	// the text fields, and hand that data off to the MatchActivity
	public void scoutTapped(View view) {
		try {
			int team1 = Integer
					.parseInt(((EditText) findViewById(R.id.team1TextField))
							.getText().toString());
			int team2 = Integer
					.parseInt(((EditText) findViewById(R.id.team2TextField))
							.getText().toString());
			int team3 = Integer
					.parseInt(((EditText) findViewById(R.id.team3TextField))
							.getText().toString());
			String match = ((EditText) findViewById(R.id.matchTextField))
					.getText().toString();
			if (match == null || match.length() == 0) {
				throw new NumberFormatException();
			}

			Intent intent = new Intent(this, MatchActivity.class);
			intent.putExtra(MatchActivity.INTENT_EXTRA_TEAM1_INT, team1);
			intent.putExtra(MatchActivity.INTENT_EXTRA_TEAM2_INT, team2);
			intent.putExtra(MatchActivity.INTENT_EXTRA_TEAM3_INT, team3);
			intent.putExtra(MatchActivity.INTENT_EXTRA_MATCH_STRING, match);
			intent.putExtra(MatchActivity.INTENT_EXTRA_ALLIANCE_COLOR_BOOL,
					isRed ? MatchActivity.ALLIANCE_COLOR_RED
							: MatchActivity.ALLIANCE_COLOR_BLUE);

			startActivityForResult(intent, MatchActivity.REQUEST_ALLIANCE_DATA);
		} catch (NumberFormatException err) {
			Toast toast = Toast.makeText(this, "Invalid teams or match",
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public void showUploadDataToast(int team, boolean success) {
		if (success) {
			Toaster.makeToast(team + " data uploaded!", Toast.LENGTH_SHORT);

		} else {
			Toaster.makeErrorToast(team + " data failed to upload!",
					Toast.LENGTH_LONG);
		}

	}

	public void uploadRobotDataToDropbox(String jsonData, String matchText,
			int teamNumber, boolean superData) {
		String suffix = superData ? "_super.json" : ".json";
		DbxPath path = new DbxPath("/Public/Scouting 2014/Match Data/"
				+ competitionCode + "/" + matchText + "/" + teamNumber
				+ suffix);

		try {
			DbxFile file = null;
			Log.e("stupid logcat", "Got path: " + path);

			if (!dbxFs.exists(path)) {
				file = dbxFs.create(path);
			} else {
				file = dbxFs.open(path);
			}

			Log.e("stupid logcat", "Writing " + teamNumber + " to Dropbox");

			file.writeString(jsonData);

			file.close();

			Log.e("stupid logcat", "Wrote " + teamNumber + " to Dropbox");
			Log.e("tag", file == null ? "(null)" : file.toString());

		} catch (DbxException err) {
			err.printStackTrace();
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.e("log", "Activity result");
		if (requestCode == REQUEST_LINK_TO_DBX) {
			Log.e("stupid logcat", "Link to dropbox done");
			if (resultCode == Activity.RESULT_OK) {
				try {
					dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr
							.getLinkedAccount());
				} catch (Unauthorized e) {
					Log.e("fml", "Something decided to break.");
					e.printStackTrace();
				}
			} else {
				// ... Link failed or was cancelled by the user.
			}
		} else if (requestCode == MatchActivity.REQUEST_ALLIANCE_DATA) {
			if (resultCode == Activity.RESULT_OK) {
				final HashMap<String, HashMap<String, Object>> matchData = (HashMap<String, HashMap<String, Object>>) data
						.getSerializableExtra(MatchActivity.RESULT_ALLIANCE_DATA_KEY);
				final String matchText = ((EditText) findViewById(R.id.matchTextField))
						.getText().toString();

				Log.e("jdafklja", matchData.keySet().toString());
				Set<String> teamNumbers = matchData.keySet();

				for (String teamString : teamNumbers) {
					HashMap<String, Object> teamData = matchData
							.get(teamString);
					JSONObject teamDataJSON = new JSONObject(teamData);

					JSONObject teamMainJSON = new JSONObject();
					try {
						teamMainJSON.put("matchNumber", matchText);
						teamMainJSON.put("teamNumber",
								Integer.parseInt(teamString));
						teamMainJSON.put("superData", teamDataJSON);
						teamMainJSON.put("isRed", isRed);
						
						uploadRobotDataToDropbox(teamMainJSON.toString(), matchText, Integer.parseInt(teamString), true);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				incrementMatchIndex();
			}
		}
	}

	// Cat stuff...
	public void kittyClicked(View view) {
		int rnd = (int) (Math.random() * 11);
		switch (rnd) {
		case 0:
			playSound(R.raw.purr1);
			break;
		case 1:
			playSound(R.raw.purr2);
			break;
		case 2:
			playSound(R.raw.purr3);
			break;
		case 3:
			playSound(R.raw.distressedkitten);
			break;
		case 4:
			playSound(R.raw.purr5);
			break;
		case 5:
			playSound(R.raw.tinky);
			break;
		case 6:
			playSound(R.raw.tinky);
			break;
		case 7:
			playSound(R.raw.purr5);
			break;
		case 8:
			playSound(R.raw.purr1);
			break;
		case 9:
			playSound(R.raw.distressedkitten);
			break;
		case 10:
			playSound(R.raw.distressedkitten);
			break;
		// HeheheheheheheHahahahahahahahhahahahahahahhahahhh!!!!!!!!!!
		// 3/11!
		// -Wesley.

		}
	}

	public void playSound(int sound) {
		if (mp == null) {
			mp = MediaPlayer.create(this, sound);

			final MainActivity myThis = this;
			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mp.release();
					myThis.mp = null;
				}

			});
			mp.start();
		}
	}
}
