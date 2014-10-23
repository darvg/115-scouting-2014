package com.mvrt.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    public final String PREFERENCES_FILE = "com.mvrt.scout.preferences";
    public final String PREFERENCES_SCOUT_KEY = "scoutid";
    public final int ENABLED = Color.BLACK;
    public final int DISABLED = Color.LTGRAY;
    public ArrayList<Match> qualificationSchedule;
    public int currentMatchNumber = 0;
    boolean allowOverride = false;
    ProgressDialog mProgressDialog;
    int scoutID = 0;

    boolean isRed() {
        return scoutID <= 3;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initProgressBar();
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        
        scoutID = preferences.getInt(PREFERENCES_SCOUT_KEY, 1);
        if (scoutID < 1 || scoutID > 6)
            scoutID = 1;
        readJSON();
        setOverride(false);
        //TODO Add JSON checking and downloading
        //TODO Add JSON parsing to match schedule
    }

    private void readJSON() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        File schedule = new File(this.getFilesDir(), "qualificationSchedule.json");

        if (mWifi.isAvailable()) {
            HTTPDownloader httpDownloader = new HTTPDownloader(this);
            httpDownloader.execute(getString(R.string.schedule_url), "false", "false").toString();
            Log.i(Constants.Logging.HTTP_LOGCAT.getPath(), "Attempted to initialize file download");
        } else if (!schedule.exists() || schedule.length() < 1)
            Toaster.burnToast("No Schedule File Found\nPlease retrieve via Wifi or Bluetooth", Toaster.TOAST_LONG);
        char[] rawRead = new char[(int) schedule.length()];
        try {
            FileReader reader = new FileReader(schedule);
            reader.read(rawRead);
            reader.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        String jsonSchedule = String.valueOf(rawRead);
        qualificationSchedule = new ArrayList<Match>();
        try {
            JSONObject jsonFile = new JSONObject(jsonSchedule);
            JSONArray matchArray = jsonFile.getJSONArray("qualificationSchedule");
            for (int i = 0; i < matchArray.length(); i++) {
                JSONObject matchObject = matchArray.getJSONObject(i);
                Match match = new Match();
                match.setMatchNumber(matchObject.getInt("matchNumber"));
                Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "" + matchObject.getJSONArray("redAlliance"));
                match.setRedAllianceJSON(matchObject.getJSONArray("redAlliance"));
                match.setBlueAllianceJSON(matchObject.getJSONArray("blueAlliance"));
                qualificationSchedule.add(match);
            }
        } catch (JSONException e) {

        }
    }

    public void loadMatch() {
        Match currentMatch = qualificationSchedule.get(currentMatchNumber);
        ((EditText) findViewById(R.id.match_id))
                .setText("" + currentMatch.getMatchNumber());
        List<Team> teamList;
        switch (scoutID) {
            case 1:
            case 2:
            case 3: {
                teamList = currentMatch.getRedAlliance();
                break;
            }
            case 4:
            case 5:
            case 6: {
                teamList = currentMatch.getBlueAlliance();
                break;
            }
            default:
                teamList = currentMatch.getRedAlliance();
        }
        ((EditText) findViewById(R.id.team_number_1))
                .setText("" + teamList.get(0).getTeamNumber());
        ((EditText) findViewById(R.id.team_number_2))
                .setText("" + teamList.get(1).getTeamNumber());
        ((EditText) findViewById(R.id.team_number_3))
                .setText("" + teamList.get(2).getTeamNumber());
    }

    private void initProgressBar() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_override) {
            toggleOverride();
        }
        if(id == R.id.action_set_scout_id) {
            setScoutID();
        }
        if (id == R.id.action_download_json_wifi) {
            downloadScheduleWifi();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_download_json_bluetooth) {
            //TODO Add bluetooth
        }
        return super.onOptionsItemSelected(item);
    }

    public void setOverride(boolean override) {
        allowOverride = override;

        EditText matchTextField = (EditText) findViewById(R.id.match_id);
        EditText team1TextField = (EditText) findViewById(R.id.team_number_1);
        EditText team2TextField = (EditText) findViewById(R.id.team_number_2);
        EditText team3TextField = (EditText) findViewById(R.id.team_number_3);

        matchTextField.setEnabled(allowOverride);
        team1TextField.setEnabled(allowOverride);
        team2TextField.setEnabled(allowOverride);
        team3TextField.setEnabled(allowOverride);

        updateUIData();
    }

    public void updateUIData() {
        loadMatch();
        EditText team1NumberText = (EditText) findViewById(R.id.team_number_1);
        EditText team2NumberText = (EditText) findViewById(R.id.team_number_2);
        EditText team3NumberText = (EditText) findViewById(R.id.team_number_3);
        EditText matchIDText = (EditText) findViewById(R.id.match_id);
        TextView allianceDisplayColorText = (TextView) findViewById(R.id.alliance_color_textview);

        if(isRed()) {
            allianceDisplayColorText.setTextColor(getResources().getColor(R.color.Red));
            allianceDisplayColorText.setText("Red Alliance");
        } else {
            allianceDisplayColorText.setTextColor(getResources().getColor(R.color.Blue));
            allianceDisplayColorText.setText("Blue Alliance");
        }
        int textColor = allowOverride ? ENABLED : DISABLED;
        matchIDText.setTextColor(textColor);
        switch (scoutID) {
            case 1:
            case 4: {
                team1NumberText.setTextColor(getResources().getColor(R.color.Green));
                team2NumberText.setTextColor(textColor);
                team3NumberText.setTextColor(textColor);
                break;
            }
            case 2:
            case 5: {
                team2NumberText.setTextColor(getResources().getColor(R.color.Green));
                team1NumberText.setTextColor(textColor);
                team3NumberText.setTextColor(textColor);
                break;
            }
            case 3:
            case 6: {
                team3NumberText.setTextColor(getResources().getColor(R.color.Green));
                team1NumberText.setTextColor(textColor);
                team2NumberText.setTextColor(textColor);
                break;
            }
            default: {
                team1NumberText.setTextColor(textColor);
                team2NumberText.setTextColor(textColor);
                team3NumberText.setTextColor(textColor);
            }
        }
    }

    public void setScoutID () {
        if (BuildConfig.DEBUG)
            Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "setScoutID");
        AlertDialog.Builder setScoutID = new AlertDialog.Builder(this);
        setScoutID.setMessage("Enter the ScoutID");
        setScoutID.setCancelable(true);

        final EditText input = new EditText(this);
        final View v = findViewById(android.R.id.content);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        setScoutID.setView(input);
        setScoutID.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        try {
                            scoutID = Integer.parseInt(input.getText().toString());
                            if (scoutID < 1 || scoutID > 6)
                                throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            Log.e(Constants.Logging.MAIN_LOGCAT.getPath(), "Invalid Scout ID");
                            Toaster.burnToast("Invalid Scout ID", Toaster.TOAST_SHORT);
                        }
                        getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putInt(PREFERENCES_SCOUT_KEY, scoutID).commit();
                        ((InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                        updateUIData();
                    }
                });
        setScoutID.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        ((InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }

                });
        setScoutID.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        setScoutID.show();
    }

    public void downloadScheduleWifi() {
        final HTTPDownloader httpDownloader = new HTTPDownloader(this);
        httpDownloader.execute(getString(R.string.schedule_url), "false", "true");

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                httpDownloader.cancel(true);
            }
        });
    }

    //TODO Add match increment

    public void startAuto(View view) {
        Intent autoIntent = new Intent(this, AutoActivity.class);
        startActivity(autoIntent);
    }
    public void toggleOverride() {
        setOverride(!allowOverride);
    }

    private class HTTPDownloader extends AsyncTask<String, Integer, String> {
        boolean progress;
        boolean successToast;
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public HTTPDownloader(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            progress = Boolean.parseBoolean(sUrl[1]);
            successToast = Boolean.parseBoolean(sUrl[2]);
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(new File(getApplicationContext().getFilesDir(), "qualificationSchedule.json"));

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            if (progress)
                mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Log.e(Constants.Logging.HTTP_LOGCAT.getPath(), "Download error: " + result);
                if (result.split(":")[0].equals("java.net.MalformedURLException") || result.split(":")[0].equals("java.net.UnknownHostException"))
                    Toaster.burnToast("Error downloading the schedule.\nDo you have a wifi connection?", Toaster.TOAST_LONG);
            } else if (successToast)
                Toaster.makeToast("File downloaded", Toaster.TOAST_SHORT);
        }
    }
}