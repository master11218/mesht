package com.davidxu.pennapps;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabWidgetActivity extends TabActivity{
	private String M_ID;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tab_layout);
	    
	    M_ID = this.getIntent().getStringExtra("user_id");

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)cya
	    intent = new Intent().setClass(this, FriendsActivity.class);
	    intent.putExtra("user_id", M_ID);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("friends").setIndicator("Friends",
	    		res.getDrawable(R.drawable.group))//drawable
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, EventsActivity.class);
	    intent.putExtra("user_id", M_ID);

	    spec = tabHost.newTabSpec("events").setIndicator("Events",
	    		res.getDrawable(R.drawable.calendar))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, MapTabActivity.class);
	    intent.putExtra("user_id", M_ID);

	    spec = tabHost.newTabSpec("map").setIndicator("Map",
	    		res.getDrawable(R.drawable.maps))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, ProfileActivity.class);
	    intent.putExtra("user_id", M_ID);

	    spec = tabHost.newTabSpec("profile").setIndicator("Profile",
	    		res.getDrawable(R.drawable.profile))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}
}
