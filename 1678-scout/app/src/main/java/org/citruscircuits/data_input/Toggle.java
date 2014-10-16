package org.citruscircuits.data_input;

import org.citruscircuits.scout.AutoMatchActivity;
import org.citruscircuits.scout.SavedData;
import org.citruscircuits.scout.TeleMatchActivity;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class Toggle extends DataInputView
{
	ToggleButton toggleButton;

	public Toggle(Context context)
	{
		this(context, "my_key", new String[] { "Off", "On" }, false);
	}

	public Toggle(Context context, String dataKey, String[] args, boolean isAuto)
	{
		super(context, dataKey, args, isAuto);

		toggleButton = new ToggleButton(context);

		toggleButton.setTextOff(args[0]);
		toggleButton.setTextOn(args[1]);
		toggleButton.setText(args[0]);

		toggleButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		this.addView(toggleButton);
		
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		        valueUpdated(isChecked);
		    }
		});		
	}

	public Object getDataValue()
	{
		return this.toggleButton.isChecked();
	}
	
	public void valueUpdated(boolean newValue) {

		if(!isAuto){
//			TeleMatchActivity.updateHashMap();
			SavedData.setTeleData(dataKey, newValue ? 1 : 0);
		}
		
		else{
//			AutoMatchActivity.updateHashMap();
			SavedData.setAutoData(dataKey, newValue ? 1 : 0);

		}
	}

}
