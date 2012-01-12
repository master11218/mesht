package com.davidxu.pennapps;

import java.util.Timer;
import java.util.TimerTask;

import com.davidxu.pennapps.location.MyLocation;
import com.davidxu.pennapps.location.MyLocation.LocationResult;
import com.davidxu.pennapps.util.HttpRequest;
import com.davidxu.pennapps.util.HttpRequest.HttpMethod;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

public class LocationService extends Service{

	private Handler m_handler = new Handler();
	private Runnable m_update_map_task = new Runnable(){
		public void run(){
			locationClick();			
			m_handler.postDelayed(m_update_map_task, 10000);
		}
	};

	private HttpRequest m_request = new HttpRequest();
	private Double m_lat;
	private Double m_long;
	private static final String BASE_URI = "http://www.getpaidtowatchyoutube.com/PennApps/";
	private String m_user_id;

	MyLocation myLocation = new MyLocation();
	private void locationClick() {
		myLocation.getLocation(this, locationResult);
		//		System.out.println("IT'S HITTING THE LOCATION CLICK, BITCHH");
	}

	public LocationResult locationResult = new LocationResult(){
		@Override
		public void gotLocation(final Location location){
			if(location != null){
				//Got the location!
				m_lat = location.getLatitude();
				m_long = location.getLongitude();
				m_request.execHttpRequest(BASE_URI + "updateLocation.php?long=" +
						m_long + "&lat=" + m_lat + "&id=" + m_user_id, HttpMethod.Get, null);
					    	System.out.println("Logging your current latitude and longitude: Latitude = " + m_lat.toString() + " Longitude = " + m_long.toString());
			}
		};
	};

	@Override
	public void onCreate() { 

		super.onCreate(); 
		locationClick();
		m_handler.postDelayed(m_update_map_task, 10000);

	}
	@Override
	public int onStartCommand(Intent i, int a, int b){
		m_user_id = i.getStringExtra("user_id");
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
