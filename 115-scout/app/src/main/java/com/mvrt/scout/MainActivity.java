package com.mvrt.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    public final String PREFERENCES_FILE = "com.mvrt.scout.preferences";
    public final String PREFERENCES_SCOUT_KEY = "scoutid";
    boolean allowOverride = false;
    boolean isRed () {return scoutID <= 3;}
    int scoutID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

        scoutID = preferences.getInt(PREFERENCES_SCOUT_KEY, 1);

        setOverride(false);
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
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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

        switch (scoutID) {
            case 1:
            case 4: {
                team1NumberText.setTextColor(getResources().getColor(R.color.Green));
                team2NumberText.setTextColor(getResources().getColor(R.color.LightGrey));
                team3NumberText.setTextColor(getResources().getColor(R.color.LightGrey));
                break;
            }
            case 2:
            case 5:
                team2NumberText.setTextColor(getResources().getColor(R.color.Green));
                team1NumberText.setTextColor(getResources().getColor(R.color.LightGrey));
                team3NumberText.setTextColor(getResources().getColor(R.color.LightGrey));
                break;
            case 3:
            case 6:
                team3NumberText.setTextColor(getResources().getColor(R.color.Green));
                team1NumberText.setTextColor(getResources().getColor(R.color.LightGrey));
                team2NumberText.setTextColor(getResources().getColor(R.color.LightGrey));
                break;
        }
    }

    public void setScoutID () {
        if (BuildConfig.DEBUG)
            Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "setScoutID");
        AlertDialog.Builder setScoutID = new AlertDialog.Builder(this);
        setScoutID.setMessage("Enter the ScoutID");
        setScoutID.setCancelable(true);

        final EditText input = new EditText(this);
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
                            Toast.makeText(getApplicationContext(), "Invalid Scout ID", Toast.LENGTH_SHORT).show();
                        }
                        getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putInt(PREFERENCES_SCOUT_KEY, scoutID).commit();
                        ((InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                        updateUIData();
                    }
                });
        setScoutID.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        ((InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE))
                                .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }

                });
        setScoutID.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        setScoutID.show();
    }
    public void toggleOverride() {
        setOverride(!allowOverride);
    }
}