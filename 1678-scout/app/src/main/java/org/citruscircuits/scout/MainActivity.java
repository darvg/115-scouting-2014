package org.citruscircuits.scout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class MainActivity extends Activity {

	private String jsonSchedule;
	private int scoutID = 0;
	Competition competition;
	List<Match> qualificationSchedule;
	private int matchIndex = 0;
	private int channel;

	//public ActionListActivity listActivity;
	public ListView storedListView;
	
	public int SUICIDE_NOTE = 42;

	File root;
	File storedMatchesDirectory;

	final String competitionCode = "Newton";
	boolean allowOverride = false;

	public final String PREFERENCES_FILE = "org.citruscircuits.scouter_2014_android.PreferencesFile";
	public final String PREFERENCES_SCHEDULE_KEY = "schedule";
	public final String PREFERENCES_SCOUTID_KEY = "scoutid";

	StoredMatchesAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Parse.initialize(this, "Removed for security, www.parse.com",
				"read the parse docs.");

		root = this.getFilesDir();
		storedMatchesDirectory = new File(root + "/match_data/");

		SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE);
		// scoutID = preferences.getInt(PREFERENCES_SCOUTID_KEY, 1337);
		scoutID = Integer.parseInt(preferences.getString(
				PREFERENCES_SCOUTID_KEY, "0"));

		setContentView(R.layout.activity_main);

		EditText matchTextField = (EditText) findViewById(R.id.matchTextField);

		matchTextField.setText("Q1");

		matchTextField.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 1) {
					updateMatchAndTeamUI();
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});

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
				e.printStackTrace();
			}

			updateMatchAndTeamUI();

		} else {
			Toaster.makeErrorToast("No match schedule...", Toast.LENGTH_LONG);
		}

		setMatchOverride(false);

		storedListView = (ListView) findViewById(R.id.storedMatchesListView);
		adapter = new StoredMatchesAdapter(this,
				android.R.layout.simple_list_item_1);
		adapter.loadDir(storedMatchesDirectory);
		storedListView.setAdapter(adapter);
		storedListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				view.setClickable(true);
				Log.e("looo~g", "the onItemClick worked");
				try {
					File[] arrayOfMatches = storedMatchesDirectory.listFiles();
					InputStream is = new FileInputStream(
							arrayOfMatches[position]);

					int size = is.available();

					byte[] buffer = new byte[size];

					is.read(buffer);

					is.close();

					final String jsonString = new String(buffer, "UTF-8");

					final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();
					if (mBluetoothAdapter == null) {
						// Device does not support Bluetooth
						Toaster.makeErrorToast("Bluetooth not connected",
								Toast.LENGTH_LONG);
						return;
					}

					Toaster.makeToast("Starting Upload...", Toast.LENGTH_LONG);

					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
							.getBondedDevices();
					if (pairedDevices.size() > 0) {
						for (final BluetoothDevice device : pairedDevices) {
							Timer btTimer = new Timer();
							btTimer.schedule(new TimerTask() {

								@Override
								public void run() {
									Log.e("log", "Starting upload");
									ConnectThread connectThread = new ConnectThread(
											device, mBluetoothAdapter,
											jsonString, null,
											MainActivity.this, channel, true);
									connectThread.start();
								}
							}, 2000);

						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					view.setClickable(false);
				}
			}
		});

	}

	private void downloadScheduleWifi() {
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
					Log.e("stupid logcat",
							"Schedule: " + qualificationSchedule.toString());

					jsonSchedule = generateJSON();
					saveScheduleToDisk(jsonSchedule);

					jsonSchedule = getScheduleFromDisk();

					qualificationSchedule = new ArrayList<Match>();
					try {
						JSONArray schedule = new JSONObject(jsonSchedule)
								.getJSONArray("qualificationSchedule");
						for (int i = 0; i < schedule.length(); i++) {
							JSONObject matchJsonObject = schedule
									.getJSONObject(i);
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
						e.printStackTrace();
					}

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							updateMatchAndTeamUI();
						}
					});
				} catch (ParseException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void downloadScheduleClicked() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			Toaster.makeErrorToast("Bluetooth not connected", Toast.LENGTH_LONG);
			return;
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				ConnectThread connectThread = new ConnectThread(device,
						mBluetoothAdapter, null, null, this, channel, false);
				connectThread.start();
			}
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

		if (id == R.id.action_set_scout_id) {

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Set Scout ID");

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
							try {
								scoutID = Integer.parseInt(input.getText()
										.toString());
								if (scoutID < 1 || scoutID > 6) {
									throw new NumberFormatException();
								}
							} catch (NumberFormatException e) {
								Toaster.makeErrorToast("Invalid scout ID",
										Toast.LENGTH_SHORT);
							}

							SharedPreferences preferences = getSharedPreferences(
									PREFERENCES_FILE, Context.MODE_PRIVATE);
							Editor editor = preferences.edit();
							editor.putString(PREFERENCES_SCOUTID_KEY,
									Integer.toString(scoutID));
							editor.commit();

							updateMatchAndTeamUI();

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
							channel = Integer.parseInt(input.getText()
									.toString());

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
		} else if (id == R.id.action_fetch_data) {
			downloadScheduleClicked();
		} else if (id == R.id.action_fetch_data_wifi) {
			downloadScheduleWifi();
		} else if (id == R.id.action_override) {
			toggleMatchOverride();
		}
		return true;
	}

	public void saveScheduleToDisk(String json) {
		SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(PREFERENCES_SCHEDULE_KEY, json);
		editor.commit();

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
				e.printStackTrace();
			}

			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					updateMatchAndTeamUI();
				}
			});

		}
	}

	private String getScheduleFromDisk() {
		return getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
				.getString(PREFERENCES_SCHEDULE_KEY, null);
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
			e.printStackTrace();
		}

		return null;

	}

	public void setMatchIndex(int index) {
		if (qualificationSchedule != null
				&& index < qualificationSchedule.size()) {
			matchIndex = index;
			updateMatchAndTeamUI();
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

		updateMatchAndTeamUI();
	}

	// Updates and fills the match and team number text fields with data
	public void updateMatchAndTeamUI() {

		// Set the correct text and color for the alliance label TextView
		EditText team1TextField = (EditText) findViewById(R.id.team1TextField);
		EditText team2TextField = (EditText) findViewById(R.id.team2TextField);
		EditText team3TextField = (EditText) findViewById(R.id.team3TextField);
		TextView allianceTextView = (TextView) findViewById(R.id.allianceColorTextView);
		EditText matchTextField = (EditText) findViewById(R.id.matchTextField);

		if (isRed()) {
			allianceTextView.setText("Red Alliance");
			allianceTextView.setTextColor(Color.RED);
		} else {
			allianceTextView.setText("Blue Alliance");
			allianceTextView.setTextColor(Color.BLUE);
		}

		if (scoutID == 1 || scoutID == 4) {
			if (allowOverride) {
				team2TextField.setTextColor(Color.BLACK);
				team3TextField.setTextColor(Color.BLACK);
			} else {
				team2TextField.setTextColor(Color.LTGRAY);
				team3TextField.setTextColor(Color.LTGRAY);
			}

			team1TextField.setTextColor(Color.GREEN);
		} else if (scoutID == 2 || scoutID == 5) {
			if (allowOverride) {
				team1TextField.setTextColor(Color.BLACK);
				team3TextField.setTextColor(Color.BLACK);
			} else {
				team1TextField.setTextColor(Color.LTGRAY);
				team3TextField.setTextColor(Color.LTGRAY);
			}

			team2TextField.setTextColor(Color.GREEN);
		} else if (scoutID == 3 || scoutID == 6) {
			if (allowOverride) {
				team1TextField.setTextColor(Color.BLACK);
				team2TextField.setTextColor(Color.BLACK);
			} else {
				team1TextField.setTextColor(Color.LTGRAY);
				team2TextField.setTextColor(Color.LTGRAY);
			}

			team3TextField.setTextColor(Color.GREEN);
		}

		if (qualificationSchedule == null) {
			return;
		}

		// Get the alliance Match
		Match currentMatch = null;
		if (qualificationSchedule.size() > matchIndex) {
			currentMatch = qualificationSchedule.get(matchIndex);

			List<Integer> teamNumbers;
			if (isRed()) {
				teamNumbers = currentMatch.redTeamNumbers;
			} else {
				teamNumbers = currentMatch.blueTeamNumbers;
			}
			// Set the team number text
			team1TextField.setText(teamNumbers.get(0) + "");
			team2TextField.setText(teamNumbers.get(1) + "");
			team3TextField.setText(teamNumbers.get(2) + "");

			JSONObject sobj = null;
			try {
				sobj = new JSONObject(getScheduleFromDisk());
			} catch (JSONException e) {
				e.printStackTrace();
			}

			try {
				JSONArray alliance = null;
				if (!isRed()) {
					alliance = sobj
							.getJSONArray("qualificationSchedule")
							.getJSONObject(
									Integer.parseInt(matchTextField.getText()
											.toString().substring(1)) - 1)
							.getJSONArray("blueAlliance");
					matchIndex = Integer.parseInt(matchTextField.getText()
							.toString().substring(1)) - 1;
				} else {
					alliance = sobj
							.getJSONArray("qualificationSchedule")
							.getJSONObject(
									Integer.parseInt(matchTextField.getText()
											.toString().substring(1)) - 1)
							.getJSONArray("redAlliance");
					matchIndex = Integer.parseInt(matchTextField.getText()
							.toString().substring(1)) - 1;
				}

				team1TextField.setText(alliance.getInt(0) + "");
				team2TextField.setText(alliance.getInt(1) + "");
				team3TextField.setText(alliance.getInt(2) + "");

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {

			}

		} else {
			Toaster.makeErrorToast("End of Match Schedule", Toast.LENGTH_LONG);
		}
	}

	// When the scout button is tapped, read the team and match number info from
	// the text fields, and hand that data off to the MatchActivity

	public void scoutTapped(String scoutName) {
		SavedData.createHashMaps();
		Intent scoutIntent = new Intent(this, AutoMatchActivity.class);

		EditText team1TextField = (EditText) findViewById(R.id.team1TextField);
		EditText team2TextField = (EditText) findViewById(R.id.team2TextField);
		EditText team3TextField = (EditText) findViewById(R.id.team3TextField);
		EditText matchTextField = (EditText) findViewById(R.id.matchTextField);

		int teamNumber = 0;
		if (scoutID == 1 || scoutID == 4) {
			teamNumber = Integer.parseInt(team1TextField.getText().toString());
		} else if (scoutID == 2 || scoutID == 5) {
			teamNumber = Integer.parseInt(team2TextField.getText().toString());
		} else if (scoutID == 3 || scoutID == 6) {
			teamNumber = Integer.parseInt(team3TextField.getText().toString());
		}

		scoutIntent.putExtra("teamNumber", teamNumber);
		scoutIntent
				.putExtra("matchNumber", matchTextField.getText().toString());
		scoutIntent.putExtra("channel", channel);
		scoutIntent.putExtra("allianceColor", isRed());
		scoutIntent.putExtra("scoutName", scoutName);

		startActivityForResult(scoutIntent, SUICIDE_NOTE);
	}

	public void enterInit(View view) {
		// Make scouts fess up and stop being stupid.

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Enter your Initials");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				String name = input.getEditableText().toString();
				if (name.length() >= 2) {
					scoutTapped(input.getEditableText().toString());
				} else {
					Toaster.makeToast("Initials have at least 2 letters...",
							Toast.LENGTH_LONG);
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					}
				});

		alert.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {

				InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		});

		alert.show();

	}

	private boolean isRed() {
		return scoutID <= 3 ? true : false;
	}

	int uploadIndex = -1;

	public void submitStoredJSON(View view) {
		Log.e("logcat sucks", "Button Tapped!");

		uploadIndex = -1;

		onBluetoothFinish(true);
	}

	public void onBluetoothConnectStart() {

	}

	public void onBluetoothFinish(final boolean success) {

		File[] arrayOfMatches = storedMatchesDirectory.listFiles();
		if (success && uploadIndex >= 0 && uploadIndex < arrayOfMatches.length) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					adapter.loadDir(storedMatchesDirectory);
				}
			});
		} else {
			uploadIndex++;
		}

		arrayOfMatches = storedMatchesDirectory.listFiles();

		try {
			if (success)
			{
				runOnUiThread(new Runnable() {
					  public void run() {
							Toaster.makeToast("Match Uploaded!", Toast.LENGTH_LONG);
					  }
					});
			} else {
				runOnUiThread(new Runnable() {
					  public void run() {
							Toaster.makeErrorToast("Uploading Failed... :(", Toast.LENGTH_LONG);
					  }
					});
			}
			if (uploadIndex >= arrayOfMatches.length) {
				Toaster.makeErrorToast("No matches left to upload...", Toast.LENGTH_LONG);
				return;
			}

			InputStream is = new FileInputStream(arrayOfMatches[uploadIndex]);

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);

			is.close();

			final String jsonString = new String(buffer, "UTF-8");

			final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				// Device does not support Bluetooth
				Toaster.makeErrorToast("Bluetooth not connected",
						Toast.LENGTH_LONG);
				return;
			}

			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
					.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (final BluetoothDevice device : pairedDevices) {
					Timer btTimer = new Timer();
					btTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							Log.e("log", "Starting upload");
							//ConnectThread connectThread = new ConnectThread(
							//		device, mBluetoothAdapter, jsonString,
							//		null, MainActivity.this, channel, true);
							//connectThread.start();
						}
					}, 2000);

				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			// Make a fail toast pop up
		}
	}

	public void nextMatch()
	{
		Log.e("log", "match++");
		if (qualificationSchedule != null
				&& matchIndex < qualificationSchedule.size()) {
			matchIndex++;
		} else {
			Toaster.makeErrorToast("End of Match Schedule",
					Toast.LENGTH_LONG);
		}
		EditText matchText = (EditText) findViewById(R.id.matchTextField);
		matchText.setText("Q" + (matchIndex + 1));
		// incrementMatchIndex();
		adapter.loadDir(storedMatchesDirectory);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == SUICIDE_NOTE) {
			Log.e("log", "match++");
			if (qualificationSchedule != null
					&& matchIndex < qualificationSchedule.size()) {
				matchIndex++;
			} else {
				Toaster.makeErrorToast("End of Match Schedule",
						Toast.LENGTH_LONG);
			}
			EditText matchText = (EditText) findViewById(R.id.matchTextField);
			matchText.setText("Q" + (matchIndex + 1));
			// incrementMatchIndex();
			adapter.loadDir(storedMatchesDirectory);

		}
	}

}
