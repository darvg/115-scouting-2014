package com.mvrt.scout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
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

import java.util.List;


public class PregameFragment extends DataCollectionFragment {

    boolean allowOverride = false;

    ProgressDialog mProgressDialog;

    public PregameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initProgressBar();
        dataManager.getSchedule(false);
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
            dataManager.downloadSchedule(true);
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
        getActivity().findViewById(R.id.team_number_1).setFocusable(allowOverride);
        getActivity().findViewById(R.id.team_number_1).setFocusableInTouchMode(allowOverride);
        getActivity().findViewById(R.id.team_number_2).setEnabled(allowOverride);
        getActivity().findViewById(R.id.team_number_2).setFocusable(allowOverride);
        getActivity().findViewById(R.id.team_number_2).setFocusableInTouchMode(allowOverride);
        getActivity().findViewById(R.id.team_number_3).setEnabled(allowOverride);
        getActivity().findViewById(R.id.team_number_3).setFocusable(allowOverride);
        getActivity().findViewById(R.id.team_number_3).setFocusableInTouchMode(allowOverride);

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

        Match currentMatch = dataManager.getCurrentMatch();

        if (isRed())
            currentMatch.setRedAlliance(Integer.parseInt(team1NumberText.getText().toString()),
                    Integer.parseInt(team2NumberText.getText().toString()),
                    Integer.parseInt(team3NumberText.getText().toString()));
        else
            currentMatch.setBlueAlliance(Integer.parseInt(team1NumberText.getText().toString()),
                    Integer.parseInt(team2NumberText.getText().toString()),
                    Integer.parseInt(team3NumberText.getText().toString()));
        dataManager.setMatch(currentMatch);
    }

    /**
     *  Sets the team numbers to the editTexts
     */
    public void setUIFromData() {

        if(allowOverride)return; //if the data is being overrided, don't change it

        Match currentMatch = dataManager.getCurrentMatch();
        List<Team> teamList = currentMatch.getBlueAlliance();
        if(dataManager.getScoutId() <= 3)
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

        setDefault(team1NumberText);
        setDefault(team2NumberText);
        setDefault(team3NumberText);
        switch (dataManager.getScoutId() % 3) {
            case 1:
                setSelected(team1NumberText);
                break;
            case 2:
                setSelected(team2NumberText);
                break;
            case 0:
                setSelected(team3NumberText);
        }

    }

    public void setSelected(EditText text) {
        text.setTextColor(getResources().getColor(R.color.accent));
        text.setTypeface(null, Typeface.ITALIC);
        if(allowOverride)text.requestFocus();
    }

    public void setDefault(EditText text) {
        text.setTextColor(getResources().getColor(R.color.text_primary_dark));
        text.setTypeface(null, Typeface.NORMAL);
        text.clearFocus();
    }

    public void setScoutID() {
        //TODO: CHANGE TO SPINNER
        if (BuildConfig.DEBUG)
        Log.d(Constants.Logging.MAIN_LOGCAT.getPath(), "setScoutID");

        final AlertDialog.Builder setScoutID = new AlertDialog.Builder(getActivity());

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

                        int scoutID = dataManager.getScoutId();

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

                        dataManager.setScoutId(scoutID);

                        setOverride(allowOverride); //re-sync data, etc.
                    }
                });

        setScoutID.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                    }

                });
    
        setScoutID.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                closeKeyboardInput();
            }
        });
        setScoutID.show();

    }

    public void closeKeyboardInput() { //waits for a while, so that the alert dialog closes, then hides keyboard
        getActivity().getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) ScoutBase.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 100);
    }



}