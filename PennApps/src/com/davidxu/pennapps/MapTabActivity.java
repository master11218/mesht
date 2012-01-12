package com.davidxu.pennapps;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.davidxu.pennapps.util.HttpRequest;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MapTabActivity extends MapActivity{
	

	private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
	private LinearLayout linearLayout;
	private MapView m_mapView;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private MapItemizedOverlay itemizedOverlay;
	private HttpRequest m_request = new HttpRequest();
	private String BASE_URI = "http://www.getpaidtowatchyoutube.com/PennApps/";
	private ArrayList<String> m_friends = new ArrayList<String>();
	private String M_ID;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        
        try{
    	sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        M_ID = sharedPreferences.getString("M_ID", null);
        
        if(this.getIntent().getStringExtra("user_id") != null)
        	M_ID = this.getIntent().getStringExtra("user_id");
        
        m_mapView = (MapView) findViewById(R.id.mapview);
        m_mapView.setBuiltInZoomControls(true);
        
        mapOverlays = m_mapView.getOverlays();
        drawable = this.getResources().getDrawable(R.drawable.current_location_marker_bw);
        
        //grab the friends
      		String f = m_request.execHttpRequest(BASE_URI + "getFriendsList.php?userId=" + M_ID, HttpRequest.HttpMethod.Get, null);
      		
      		StringTokenizer temp = new StringTokenizer(f, ",");
      		while(temp.hasMoreTokens()){
      			String s = temp.nextToken().trim();
      			if(!s.equals(""))
      				m_friends.add(s);
      		}
        
      	for(int i=0; i<m_friends.size();i++){
            itemizedOverlay = new MapItemizedOverlay(drawable);
      		f = m_request.execHttpRequest(BASE_URI + "lattitudeTest.php?id=" + m_friends.get(i), HttpRequest.HttpMethod.Get, null);
      		temp = new StringTokenizer(f, ",");
      		String latitude = temp.nextToken().trim();
      		String longitude = temp.nextToken().trim();
      		GeoPoint point = new GeoPoint((int)(Double.parseDouble(latitude) * 1000000),(int)(Double.parseDouble(longitude) * 1000000));
            OverlayItem overlayitem = new OverlayItem(point, "", "");
            itemizedOverlay.addOverlay(overlayitem);
            mapOverlays.add(itemizedOverlay);
      	}


        drawable = this.getResources().getDrawable(R.drawable.current_location_marker);
        itemizedOverlay = new MapItemizedOverlay(drawable);
  		f = m_request.execHttpRequest(BASE_URI + "lattitudeTest.php?id=" + M_ID, HttpRequest.HttpMethod.Get, null);
  		temp = new StringTokenizer(f, ",");
  		String latitude = temp.nextToken().trim();
  		String longitude = temp.nextToken().trim();
  		GeoPoint point = new GeoPoint((int)(Double.parseDouble(latitude) * 1000000),(int)(Double.parseDouble(longitude) * 1000000));
        OverlayItem overlayitem = new OverlayItem(point, "", "");
        itemizedOverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedOverlay);
  			

    	}
    	catch(Exception E){};
      	
    }
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
