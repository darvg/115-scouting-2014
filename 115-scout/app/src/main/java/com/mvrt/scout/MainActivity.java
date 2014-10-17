package com.mvrt.scout;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class MainActivity extends Activity {

    boolean allowOverride = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    public void toggleOverride() {
        setOverride(!allowOverride);
    }
}