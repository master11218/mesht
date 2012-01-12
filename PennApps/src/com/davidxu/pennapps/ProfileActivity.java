package com.davidxu.pennapps;

import java.util.StringTokenizer;

import com.davidxu.pennapps.util.HttpRequest;
import com.davidxu.pennapps.util.HttpRequest.HttpMethod;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ProfileActivity extends Activity implements OnClickListener{

	private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
	
	private String M_ID;
	private static final String M_URI = "http://www.getpaidtowatchyoutube.com/PennApps/profileInfo.php?id=";
	private static final String M_LASTUPDATED = "http://www.getpaidtowatchyoutube.com/PennApps/getLastUpdate.php?id=";
	private HttpRequest m_request = new HttpRequest();
	private TextView m_name;
	private TextView m_phone;
	private TextView m_email;
	private TextView m_friends;
	private TextView m_last_updated;
	private Button m_startstop;
	private Button m_logout;
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        try{
    	sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        
        M_ID = sharedPreferences.getString("M_ID", null);
        
        if(this.getIntent().getStringExtra("user_id") != null)
			M_ID = this.getIntent().getStringExtra("user_id"); //capture the ID7
		if(M_ID != null)
			M_ID.trim();
		
        String temp = m_request.execHttpRequest(M_URI + M_ID, HttpMethod.Get, null);
        temp.trim();
        StringTokenizer a = new StringTokenizer(temp.trim(), "|");
        m_name = (TextView) this.findViewById(R.id.profile_name);
        m_phone = (TextView) this.findViewById(R.id.profile_phone);
        m_email = (TextView) this.findViewById(R.id.profile_email);
        m_friends = (TextView) this.findViewById(R.id.profile_friends);
        m_last_updated = (TextView) this.findViewById(R.id.profile_lastupdated);
        m_startstop = (Button) this.findViewById(R.id.profile_startstop);
        m_startstop.setOnClickListener(this);
        m_logout = (Button)this.findViewById(R.id.profile_logout);
        m_startstop.setOnClickListener(this);
        

        if(sharedPreferences.getBoolean("auto_updating", false))
        	m_startstop.setText("Start automatically updating your location.");
        else
        	m_startstop.setText("Stop automatically updating your location.");
        
        m_name.setText(a.nextToken());
        m_phone.setText(a.nextToken());
        m_email.setText(a.nextToken());
        m_friends.setText(a.nextToken());
        m_last_updated.setText(m_request.execHttpRequest(M_LASTUPDATED + M_ID, HttpMethod.Get, null));

    	}
    	catch(Exception E){};
    }


	@Override
	public void onClick(View v) {

		//start the service, passing the m_Id
		if(v.equals(m_startstop)){
			
			if(!sharedPreferences.getBoolean("auto_updating", false)){
				Intent service_intent = new Intent(this, LocationService.class);
				service_intent.putExtra("user_id", M_ID);
				startService(service_intent);
	        	m_startstop.setText("Stop automatically updating your location.");
	        	editor.putBoolean("auto_updating", true);
	        	editor.commit();
			}
			else{
	        	m_startstop.setText("Start automatically updating your location.");
				Intent service_intent = new Intent(this, LocationService.class);
				service_intent.putExtra("user_id", M_ID);
	        	stopService(service_intent);
	        	editor.putBoolean("auto_updating", false);
	        	editor.commit();
			}
				
			return;
		}
		if(v.equals(m_logout)){
			M_ID = null;
			editor.remove("M_ID");		
			editor.putBoolean("logged_in", false);
			editor.putBoolean("auto_updating", false);
			editor.commit();
			onBackPressed();	
		}
	}
	
	@Override
	public void onPause(){
		editor.putString("M_ID", M_ID);
		editor.commit();
		super.onPause();
	}
}
