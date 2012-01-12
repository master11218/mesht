package com.davidxu.pennapps;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.davidxu.pennapps.EventsActivity.EventAdapter;
import com.davidxu.pennapps.util.HttpRequest;
import com.davidxu.pennapps.util.HttpRequest.HttpMethod;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas.EdgeType;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendsActivity extends Activity implements OnClickListener{

	private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
	
	private String M_ID; //current user's id
	private static final String BASE_URI = "http://www.getpaidtowatchyoutube.com/PennApps/";

	private LinearLayout m_listwrapper;
	private Button m_add;
	private ListView m_nearby_list;
	private ListView m_far_list;
	private NearbyFriendsAdapter m_nearby_adapter;
	private FarFriendsAdapter m_far_adapter;
	private Button m_button_setdistance;
	private Dialog m_distance_dialog;
	private Button m_dialog_button;
	private EditText m_dialog_edittext;
	private TextView m_header1_none;
	private TextView m_header2_none;
	
	//search items
	private LinearLayout m_searchwrapper;
	private Button m_search_button;
	private EditText m_search_input;
	private ListView m_search_list;
	private FriendsSearchAdapter m_search_listadapter;
	private TextView m_search_noresults;

	private HttpRequest m_request = new HttpRequest();

	//invite
	private ArrayList<String> m_events = new ArrayList<String>();
	private Dialog invite_list;

	private ArrayList<String> m_friends_search = new ArrayList<String>();
	private ArrayList<String> m_nearby_friends = new ArrayList<String>();
	private ArrayList<Double> m_nearby_distances = new ArrayList<Double>();
	private ArrayList<String> m_far_friends = new ArrayList<String>();
	private ArrayList<Double> m_far_distances = new ArrayList<Double>();
	private ArrayList<String> m_friends = new ArrayList<String>();	//all of them
	private ArrayList<Double> m_distances = new ArrayList<Double>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_list);
		
		sharedPreferences = getPreferences(MODE_PRIVATE);
		editor = sharedPreferences.edit();

		M_ID = sharedPreferences.getString("M_ID", null);

		m_nearby_list = (ListView) this.findViewById(R.id.friends_listView_nearby);

		m_far_list = (ListView) this.findViewById(R.id.friends_listView_far);
		m_nearby_adapter = new NearbyFriendsAdapter(this);
		m_far_adapter = new FarFriendsAdapter(this);
		m_nearby_list.setAdapter(m_nearby_adapter);
		m_far_list.setAdapter(m_far_adapter);
		m_button_setdistance = (Button)this.findViewById(R.id.friends_setheaderdistance);
		m_button_setdistance.setOnClickListener(this);
		m_header1_none = (TextView)this.findViewById(R.id.friends_header1_none);
		m_header2_none = (TextView)this.findViewById(R.id.friends_header2_none);

		m_listwrapper = (LinearLayout)this.findViewById(R.id.friends_listwrapper);
		m_add = (Button)this.findViewById(R.id.friends_button_add);
		m_add.setOnClickListener(this);

		m_searchwrapper = (LinearLayout)this.findViewById(R.id.friends_search_wrapper);
		m_search_button = (Button) this.findViewById(R.id.friends_search_button);
		m_search_input = (EditText) this.findViewById(R.id.friends_search_input);
		m_search_list = (ListView) this.findViewById(R.id.friends_search_results);
		m_search_listadapter = new FriendsSearchAdapter(this);
		m_search_button.setOnClickListener(this);
		m_search_list.setAdapter(m_search_listadapter);
		m_search_noresults = (TextView)this.findViewById(R.id.friends_search_none);





 
		//        TextView textview = new TextView(this);
		//        textview.setText("This is the Artists tab");
		//        setContentView(textview);
	}
	@Override
	public void onResume(){
		super.onResume();

		if(this.getIntent().getStringExtra("user_id") != null)
			M_ID = this.getIntent().getStringExtra("user_id"); //capture the ID7
		if(M_ID != null)
			M_ID.trim();

		//start the service, passing the m_Id
		Intent service_intent = new Intent(this, LocationService.class);
		service_intent.putExtra("user_id", M_ID);
		startService(service_intent);
		
		editor.putBoolean("auto_updating", true);
		editor.commit();
		
		m_friends.clear();
		m_distances.clear();
		m_nearby_adapter.notifyDataSetChanged();
		m_far_adapter.notifyDataSetChanged();
		
		//grab the friends
		String f = m_request.execHttpRequest(BASE_URI + "getFriendsList.php?userId=" + M_ID, HttpRequest.HttpMethod.Get, null);
		
		if(f == null)
			f = "";
		
		StringTokenizer temp = new StringTokenizer(f, ",");
		while(temp.hasMoreTokens()){
			String s = temp.nextToken().trim();
			if(!s.equals(""))
				m_friends.add(s);
		}
		//grab the distances
		for(int i = 0; i < m_friends.size(); i++){
			String temp_float = m_request.execHttpRequest(BASE_URI + "distance.php?id1=" + M_ID + "&id2=" + m_friends.get(i), HttpRequest.HttpMethod.Get, null);
			//add to list, 
			if(temp_float != null)
				m_distances.add(Double.parseDouble(temp_float.trim()));
		}	
		
		m_nearby_distances.clear();
		m_far_friends.clear();
		m_far_distances.clear();
		m_nearby_friends.clear();	

		m_nearby_adapter.notifyDataSetChanged();
		m_far_adapter.notifyDataSetChanged();
		//assign distances and friends to nearby/far
		for(int i = 0; i < m_distances.size(); i++){
			if(m_distances.get(i) < Double.parseDouble(m_button_setdistance.getText().toString().substring(
					0, m_button_setdistance.getText().toString().length()-3))){//compare it to everything but the last 3 char - " mi.
				//if its less then the button distance, then move it to the nearby
				m_nearby_distances.add(m_distances.get(i));
				m_nearby_friends.add(m_friends.get(i));
			}
			else{
				m_far_distances.add(m_distances.get(i));
				m_far_friends.add(m_friends.get(i));
			}
			//if either is empty, update the textview saying they're empty
			if(m_nearby_friends.size() == 0)
				m_header1_none.setVisibility(View.VISIBLE);
			else
				m_header1_none.setVisibility(View.GONE);
			if(m_far_friends.size() == 0)
				m_header2_none.setVisibility(View.VISIBLE);
			else
				m_header2_none.setVisibility(View.GONE);
		}
		
		m_far_adapter.notifyDataSetChanged();
		m_nearby_adapter.notifyDataSetChanged();
	}


	private static final String M_SEARCH_URI= "http://www.getpaidtowatchyoutube.com/PennApps/search.php?name=";
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.equals(m_add)){
			m_searchwrapper.setVisibility(View.VISIBLE);
			m_listwrapper.setVisibility(View.GONE);
		}
		if(v.equals(m_search_button)){
			//make softKeyboard disappear
			removeKeyboard(m_search_input);
			//Make the "no results found" disappear
			m_search_noresults.setVisibility(View.GONE);
			m_friends_search.clear();
			m_search_listadapter.notifyDataSetChanged();
			//execute http request(it returns comma separated values, add it to the list of friends for the list view. 
			String f = m_request.execHttpRequest(M_SEARCH_URI + m_search_input.getText().toString(), HttpRequest.HttpMethod.Get, null);
			StringTokenizer temp = new StringTokenizer(f.trim(), ",");
			//if there are no tokens, no results were found.
			if(!temp.hasMoreTokens())
				m_search_noresults.setVisibility(View.VISIBLE);
			
			while(temp.hasMoreTokens()){
				String s = temp.nextToken();
				if(!s.equals("") && !s.equals("\n"))
					m_friends_search.add(s);
			}
		}
		if(v.equals(m_button_setdistance)){
			//open up a dialog box, allow user to set distance.
			m_distance_dialog = new Dialog(this);
			m_distance_dialog.setContentView(R.layout.friends_distance_dialog); 
			m_dialog_button = (Button) m_distance_dialog.findViewById(R.id.friends_distance_setbutton);
			m_dialog_edittext = (EditText) m_distance_dialog.findViewById(R.id.friends_distance_input);
			m_dialog_button.setOnClickListener(this);
			m_distance_dialog.show();
		}
		if(v.equals(m_dialog_button)){
			if(m_dialog_edittext.getText() != null){
				m_button_setdistance.setText(m_dialog_edittext.getText() + " mi.");

				//assign distances and friends to nearby/far
				m_nearby_friends.clear();
				m_far_friends.clear();
				m_far_distances.clear();
				m_nearby_distances.clear();

				m_nearby_adapter.notifyDataSetChanged();
				m_far_adapter.notifyDataSetChanged();
				
				for(int i = 0; i < m_distances.size(); i++){	
					if(m_distances.get(i) < Double.parseDouble(m_button_setdistance.getText().toString().substring(
							0, m_button_setdistance.getText().toString().length()-3))){//compare it to everything but the last 3 char - " mi.
						//if its less then the button distance, then move it to the nearby
						m_nearby_distances.add(m_distances.get(i));
						m_nearby_friends.add(m_friends.get(i));
					}
					else{
						m_far_distances.add(m_distances.get(i));
						m_far_friends.add(m_friends.get(i));
					}
					//update  textview saying that it's empty
					if(m_nearby_friends.size() == 0)
						m_header1_none.setVisibility(View.VISIBLE);
					else
						m_header1_none.setVisibility(View.GONE);
					if(m_far_friends.size() == 0)
						m_header2_none.setVisibility(View.VISIBLE);
					else
						m_header2_none.setVisibility(View.GONE);
				}
				m_nearby_adapter.notifyDataSetChanged();
				m_far_adapter.notifyDataSetChanged();
				m_distance_dialog.dismiss();
				m_distance_dialog.setCanceledOnTouchOutside(true);
			}
		}
	}

	@Override
	public void onBackPressed(){
		if(m_searchwrapper.getVisibility() == View.VISIBLE){
			m_searchwrapper.setVisibility(View.GONE);
			m_listwrapper.setVisibility(View.VISIBLE);
			return;
		}

		super.onBackPressed();
	}

	class FriendsSearchAdapter extends BaseAdapter{
		private Context m_context;
		public FriendsSearchAdapter(Context c){
			m_context = c;
		}
		@Override
		public int getCount() {
			if(m_friends_search != null)
				return m_friends_search.size();
			else
				return 0;
		}
		@Override
		public Object getItem(int position) {
			if(m_friends_search != null)
				return m_friends_search.get(position);
			else
				return 0;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LinearLayout list_result;
			if(convertView == null){
				//inflate the view
				LayoutInflater inf = (LayoutInflater)m_context.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				list_result = (LinearLayout) inf.inflate(R.layout.friends_search_result, null);
			}
			else 
				list_result = (LinearLayout)convertView;
			//name of friend
			TextView t = (TextView)list_result.findViewById(R.id.friends_search_listresult);
			t.setText(m_request.execHttpRequest(BASE_URI + "getName.php?userId=" + m_friends_search.get(position),
					HttpMethod.Get, null).trim());

			Button b = (Button)list_result.findViewById(R.id.friends_search_addfriend);
			final int a = position;
			final String temp = m_friends_search.get(position);
			b.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					//add the friend by making http request
					String s = temp;
					String check = m_request.execHttpRequest(BASE_URI + "registerFriend.php?userId=" + s + "&ownUserId=" + M_ID, HttpMethod.Get, null);
					if(check == null || check.trim().equals("false"))
						Toast.makeText(m_context, "Adding failed.", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(m_context, "Your friend has been added!.", Toast.LENGTH_SHORT).show();
				}
			});
			return list_result;
		}
	}


	class NearbyFriendsAdapter extends BaseAdapter{
		private Context m_context;
		public NearbyFriendsAdapter(Context c){
			m_context = c;
		}
		@Override
		public int getCount() {
			if(m_nearby_friends != null)
				return m_nearby_friends.size();
			else
				return 0;
		}
		@Override
		public Object getItem(int position) {
			if(m_nearby_friends != null)
				return m_nearby_friends.get(position);
			else
				return 0;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			LinearLayout list_result;
			if(convertView == null){
				//inflate the view
				LayoutInflater inf = (LayoutInflater)m_context.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				list_result = (LinearLayout) inf.inflate(R.layout.friends_list_result, null);
			}
			else 
				list_result = (LinearLayout)convertView;

			//name of friend
			TextView t = (TextView)list_result.findViewById(R.id.friends_list_listresult);
			t.setText(m_request.execHttpRequest(BASE_URI + "getName.php?userId=" + m_nearby_friends.get(position),
					HttpMethod.Get, null).trim());
			//distance from friend
			TextView t2 = (TextView)list_result.findViewById(R.id.friends_list_resultsdistance);
			t2.setText(String.format("%.5g%n", m_nearby_distances.get(position)) + " mi. away");

			Button b = (Button)list_result.findViewById(R.id.friends_list_invitefriend);
			final int a = position;
			final String temp = m_nearby_friends.get(position);
			b.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// insert invite code here

					m_events.clear();
					invite_list = new Dialog(m_context);
					invite_list.setContentView(R.layout.events_layout);

		      		//remove the button
		      		Button b = (Button)invite_list.findViewById(R.id.events_button);
					b.setVisibility(View.GONE);
		      		
					String f = m_request.execHttpRequest(BASE_URI + "getEvents.php?id=" + M_ID, HttpRequest.HttpMethod.Get, null);
		      		
		      		StringTokenizer temp = new StringTokenizer(f, ",");
		      		while(temp.hasMoreTokens()){
		      			String s = temp.nextToken().trim();
		      			if(!s.equals(""))
		      				m_events.add(s);
		      		}
			      		
			      	ListView m_list = (ListView)invite_list.findViewById(R.id.events_listview);
			      	EventAdapter m_listadapter = new EventAdapter(m_context);
			      	m_list.setFocusable(false);
			      	m_list.setAdapter(m_listadapter);
			      	m_list.setClickable(true);
			      	m_list.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							//event id = arg2, uid = position
							m_request.execHttpRequest(BASE_URI + "inviteUserToEvent.php?eid=" + arg2 + "&uid=" + position, HttpRequest.HttpMethod.Get, null);
				      		
						}
			      		
			      	});
					
			      	

					
					
					
			      	invite_list.show();
					
					
					
					
					
					
					
					
					
				}

			});


			return list_result;
		}
	}

	class FarFriendsAdapter extends BaseAdapter{
		private Context m_context;
		public FarFriendsAdapter(Context c){
			m_context = c;
		}
		@Override
		public int getCount() {
			if(m_far_friends != null)
				return m_far_friends.size();
			else
				return 0;
		}
		@Override
		public Object getItem(int position) {
			if(m_far_friends != null)
				return m_far_friends.get(position);
			else
				return 0;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			LinearLayout list_result;
			if(convertView == null){
				//inflate the view
				LayoutInflater inf = (LayoutInflater)m_context.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				list_result = (LinearLayout) inf.inflate(R.layout.friends_list_result, null);
			}
			else 
				list_result = (LinearLayout)convertView;

			//name of friend
			TextView t = (TextView)list_result.findViewById(R.id.friends_list_listresult);
			t.setText(m_request.execHttpRequest(BASE_URI + "getName.php?userId=" + m_far_friends.get(position),
					HttpMethod.Get, null).trim());
			//distance from friend
			TextView t2 = (TextView)list_result.findViewById(R.id.friends_list_resultsdistance);
			
			t2.setText(String.format("%.5g%n", m_far_distances.get(position)) + " mi. away");

			Button b = (Button)list_result.findViewById(R.id.friends_list_invitefriend);
			final int a = position;
			final String temp = m_far_friends.get(position);
			b.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// insert invite code here
					// insert invite code here

					m_events.clear();
					invite_list = new Dialog(m_context);
					invite_list.setContentView(R.layout.events_layout);

		      		//remove the button
		      		Button b = (Button)invite_list.findViewById(R.id.events_button);
					b.setVisibility(View.GONE);
		      		
					String f = m_request.execHttpRequest(BASE_URI + "getEvents.php?id=" + M_ID, HttpRequest.HttpMethod.Get, null);
		      		
		      		StringTokenizer temp = new StringTokenizer(f, ",");
		      		while(temp.hasMoreTokens()){
		      			String s = temp.nextToken().trim();
		      			if(!s.equals(""))
		      				m_events.add(s);
		      		}
			      		
			      	ListView m_list = (ListView)invite_list.findViewById(R.id.events_listview);
			      	EventAdapter m_listadapter = new EventAdapter(m_context);
			      	m_list.setFocusable(false);
			      	m_list.setAdapter(m_listadapter);
			      	m_list.setClickable(true);
			      	m_list.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							//event id = arg2, uid = position
							m_request.execHttpRequest(BASE_URI + "inviteUserToEvent.php?eid=" + arg2 + "&uid=" + position, HttpRequest.HttpMethod.Get, null);
				      		
						}
			      		
			      	});
					
			      	

					
					
					
			      	invite_list.show();
					
				}

			});


			return list_result;
		}
	}
	
	

	public class EventAdapter extends BaseAdapter{
		private Context m_context;
		public EventAdapter(Context c){
			m_context = c;
		}
		@Override
		public int getCount() {
			if(m_events != null)
				return m_events.size();
			else
				return 0;
		}
		@Override
		public Object getItem(int position) {
			if(m_events != null)
				return m_events.get(position);
			else
				return 0;
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			
			LinearLayout list_result;
			if(convertView == null){
				//inflate the view
				LayoutInflater inf = (LayoutInflater)m_context.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
				list_result = (LinearLayout) inf.inflate(R.layout.events_single, null);
			}
			else 
				list_result = (LinearLayout)convertView; 
			
			Button invite_button = (Button)list_result.findViewById(R.id.events_invite_button);
			invite_button.setVisibility(View.VISIBLE);
			invite_button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					m_request.execHttpRequest(BASE_URI + "inviteUserToEvent.php?eid=" + position + "&uid=" + m_nearby_friends.get(position), HttpRequest.HttpMethod.Get, null);
		      		Toast.makeText(m_context, "You've successfully invited them to the event!", Toast.LENGTH_SHORT).show();
		      		invite_list.dismiss();
				}
			});
			
			TextView event_loc = (TextView) list_result.findViewById(R.id.events_single_location);
			TextView event_creator = (TextView) list_result.findViewById(R.id.events_single_creator);
			TextView event_time = (TextView) list_result.findViewById(R.id.events_single_time);
			TextView event_description = (TextView) list_result.findViewById(R.id.events_single_description);
			LinearLayout event_guests = (LinearLayout) list_result.findViewById(R.id.events_single_guests);
			
					
			
			String s = m_request.execHttpRequest(BASE_URI + "getEventDetails.php?id=" + m_events.get(position), HttpMethod.Get, null);
			StringTokenizer temp = new StringTokenizer(s, "|");

			String event_descriptionString = new String();
			String event_creatorID = new String();
			String event_timeString = new String();
			String event_guestIDs = new String();
			
			String event_location = temp.nextToken().trim();
			if(temp.hasMoreTokens())
				event_descriptionString = temp.nextToken().trim();
			if(temp.hasMoreTokens())
				event_creatorID = temp.nextToken().trim();
			if(temp.hasMoreTokens())
				event_timeString = temp.nextToken().trim();
			if(temp.hasMoreTokens())
				event_guestIDs = temp.nextToken().trim();
			
			event_loc.setText(event_location);
			event_description.setText(event_descriptionString);
			event_creator.setText(m_request.execHttpRequest(BASE_URI + "getName.php?userId=" + event_creatorID,
					HttpMethod.Get, null).trim());
			event_time.setText(event_timeString);
			
			temp = new StringTokenizer(event_guestIDs, ",");
			while(temp.hasMoreTokens()){
				s = m_request.execHttpRequest(BASE_URI + "getName.php?userId=" + temp.nextToken(),
						HttpMethod.Get, null).trim();
				TextView t = new TextView(m_context);
				t.setText(s);
				t.setPadding(6, 3, 6, 3);
				event_guests.addView(t);
				
			}
			
			

			
			return list_result;
		}
	}
	
	
	
	public void removeKeyboard(EditText e){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(e.getWindowToken(), 0);
	}
	

	@Override
	public void onPause(){
		editor.putString("M_ID", M_ID);
		editor.commit();
		super.onPause();
	}
}
