package com.mvrt.scout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class PregameFragment extends DataCollectionFragment {

    boolean allowOverride = false;

    ProgressDialog mProgressDialog;

    public PregameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initProgressBar();

        readJSON();
        //TODO Add JSON checking and downloading
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        if (id == R.id.action_set_scout_id) {
            setScoutID();
        }
        if (id == R.id.action_download_json_wifi) {
            downloadScheduleWifi();
        }
        if (id == R.id.action_download_json_bluetooth) {
            //TODO Add bluetooth
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "Loaded Fragment");
        return inflater.inflate(R.layout.fragment_pregame, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setOverride(false);
    }

    private void readJSON() {
        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();

        File schedule = new File(getActivity().getFilesDir(), "qualificationSchedule.json");
        Log.d(Constants.Logging.HTTP_LOGCAT.getPath(), "Connection: " + String.valueOf(netInfo.isConnected()));
        if (netInfo.isConnected() && netInfo != null) {
            HTTPDownloader httpDownloader = new HTTPDownloader(getActivity());
            httpDownloader.execute(getString(R.string.schedule_url), "false", "false").toString();
            Log.i(Constants.Logging.HTTP_LOGCAT.getPath(), "Attempted to initialize file download");
        } else if (!schedule.exists() || schedule.length() < 1)
            Toaster.burnToast("No Schedule File Found\nPlease retrieve via Wifi or Bluetooth", Toaster.TOAST_LONG);
        scheduleManager.loadScheduleFromFile(schedule);
    }

    private void initProgressBar() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
    }

    public void toggleOverride() {
        setOverride(!allowOverride);
    }

    public void setOverride(boolean override) {
        allowOverride = override;

        getActivity().findViewById(R.id.team_number_1).setEnabled(allowOverride);
        getActivity().findViewById(R.id.team_number_2).setEnabled(allowOverride);
        getActivity().findViewById(R.id.team_number_3).setEnabled(allowOverride);

        if(!override)setUIFromData();
        updateUIColors();

    }

    /**
     *  Reads the UI data, and saves it to the current match
     */
    public void getDataFromUI(){

        if(!allowOverride)return;
        EditText team1NumberText = (EditText) getActivity().findViewById(R.id.team_number_1);
        EditText team2NumberText = (EditText) getActivity().findViewById(R.id.team_number_2);
        EditText team3NumberText = (EditText) getActivity().findViewById(R.id.team_number_3);

        Match currentMatch = scheduleManager.getCurrentMatch();

        if (isRed())
            currentMatch.setRedAlliance(Integer.parseInt(team1NumberText.getText().toString()),
                    Integer.parseInt(team2NumberText.getText().toString()),
                    Integer.parseInt(team3NumberText.getText().toString()));
        else
            currentMatch.setBlueAlliance(Integer.parseInt(team1NumberText.getText().toString()),
                    Integer.parseInt(team2NumberText.getText().toString()),
                    Integer.parseInt(team3NumberText.getText().toString()));
        scheduleManager.setMatch(currentMatch);
    }

    /**
     *  Sets the team numbers to the editTexts
     */
    public void setUIFromData() {

        if(allowOverride)return; //if the data is being overrided, don't change it

        Match currentMatch = scheduleManager.getCurrentMatch();
        List<Team> teamList = currentMatch.getBlueAlliance();
        if(scoutID <= 3)
                teamList = currentMatch.getRedAlliance();

        ((EditText) getActivity().findViewById(R.id.team_number_1))
                .setText("" + teamList.get(0).getTeamNumber());
        ((EditText) getActivity().findViewById(R.id.team_number_2))
                .setText("" + teamList.get(1).getTeamNumber());
        ((EditText) getActivity().findViewById(R.id.team_number_3))
                .setText("" + teamList.get(2).getTeamNumber());

    }

    /**
     *  Sets UI color to reflect the saved match data
     */
    public void updateUIColors() {

        TextView allianceDisplayColorText = (TextView) getActivity().findViewById(R.id.alliance_color_textview);

        allianceDisplayColorText.setTextColor(getResources().getColor(isRed() ? R.color.red_alliance : R.color.blue_alliance));
        allianceDisplayColorText.setText( (isRed() ? "Red":"Blue") + " Alliance");

        EditText team1NumberText = (EditText) getActivity().findViewById(R.id.team_number_1);
        EditText team2NumberText = (EditText) getActivity().findViewById(R.id.team_number_2);
        EditText team3NumberText = (EditText) getActivity().findViewById(R.id.team_number_3);

        team1NumberText.setTextColor(getResources().getColor(
                (scoutID % 3 == 1)? R.color.primary_dark:R.color.text_primary_dark));
        team2NumberText.setTextColor(getResources().getColor(
                (scoutID % 3 == 2)? R.color.primary_dark:R.color.text_primary_dark));
        team3NumberText.setTextColor(getResources().getColor(
                (scoutID % 3 == 0)? R.color.primary_dark:R.color.text_primary_dark));

    }

    public void setScoutID() {
        //TODO: CHANGE TO SPINNER
        if (BuildConfig.DEBUG)
        Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "setScoutID");

        AlertDialog.Builder setScoutID = new AlertDialog.Builder(getActivity());

        setScoutID.setMessage("Enter the Scout ID:");
        setScoutID.setCancelable(true);

        final EditText input = new EditText(getActivity());
        final LinearLayout layout = new LinearLayout(getActivity());
        input.setHint("ID (1-6)");
        input.setTextColor(getResources().getColor(R.color.text_primary_dark));
        input.setHintTextColor(getResources().getColor(R.color.text_secondary_dark));

        int padding = (int)(20 * getResources().getDisplayMetrics().density);

        input.setPadding(padding, padding / 2, padding, padding / 2);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        final View v = getActivity().findViewById(android.R.id.content);

        layout.setPaddingRelative(padding, 0, padding, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(input);

        setScoutID.setView(layout);

        setScoutID.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        try {
                            int oldId = scoutID;
                            scoutID = Integer.parseInt(input.getText().toString());
                            if (scoutID < 1 || scoutID > 6) {
                                scoutID = oldId;
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException e) {
                            Log.e(Constants.Logging.MAIN_LOGCAT.getPath(), "Invalid Scout ID");
                            Toaster.makeToast("Invalid Scout ID", Toaster.TOAST_SHORT);
                        }

                        getActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putInt(PREFERENCES_SCOUT_KEY, scoutID).commit();

                        ((InputMethodManager) ScoutBase.getAppContext().getSystemService(ScoutBase.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                        setUIFromData();
                        updateUIColors();
                    }
                });

        setScoutID.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                        ((InputMethodManager) ScoutBase.getAppContext().getSystemService(ScoutBase.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }

                });

        setScoutID.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((InputMethodManager) ScoutBase.getAppContext().getSystemService(ScoutBase.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        setScoutID.show();
    }

    public void downloadScheduleWifi() {
        final HTTPDownloader httpDownloader = new HTTPDownloader(ScoutBase.getAppContext());
        httpDownloader.execute(getString(R.string.schedule_url), "false", "true");

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                httpDownloader.cancel(true);
            }
        });
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
                output = new FileOutputStream(new File(ScoutBase.getAppContext().getFilesDir(), "qualificationSchedule.json"));

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