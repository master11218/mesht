package com.davidxu.pennapps;

import com.davidxu.pennapps.util.HttpRequest;
import com.davidxu.pennapps.util.HttpRequest.HttpMethod;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class PennAppsActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
	
	private String M_ID; // the id of the current user.
	
	//splash view
	private RelativeLayout m_splash_wrapper;
	private EditText m_splash_et_username;
	private EditText m_splash_et_password;
	private Button m_splash_button_login;
	private Button m_splash_button_register;
	
	private HttpRequest request = new HttpRequest();

	//register view
	private LinearLayout m_splash_register;
	private EditText m_splash_register_username;
	private EditText m_splash_register_password;
	private EditText m_splash_register_email;
	private EditText m_splash_register_phone;
	private Button m_splash_register_button_register;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        
        M_ID = sharedPreferences.getString("M_ID", null);
        //check, if logged in, open tab menu
        if(sharedPreferences.getBoolean("logged_in", false)){
        	Intent intent = new Intent(this, TabWidgetActivity.class);
        	intent.putExtra("user_id", M_ID);
        	startActivity(intent);
        }
        
        m_splash_wrapper = (RelativeLayout) this.findViewById(R.id.splash_wrapper);
        
        //grab the button/edittexts
        m_splash_et_username = (EditText) this.findViewById(R.id.splash_et_username);
        m_splash_et_password = (EditText) this.findViewById(R.id.splash_et_password);
        m_splash_button_login = (Button) this.findViewById(R.id.splash_button_login);
        m_splash_button_register = (Button) this.findViewById(R.id.splash_button_register);
        m_splash_button_login.setOnClickListener(this);
        m_splash_button_register.setOnClickListener(this);
        

        m_splash_register = (LinearLayout) this.findViewById(R.id.splash_register_wrapper);
        m_splash_register_username = (EditText) this.findViewById(R.id.splash_register_et_name);
        m_splash_register_password = (EditText) this.findViewById(R.id.splash_register_et_password);
        m_splash_register_email = (EditText) this.findViewById(R.id.splash_register_et_email);
        m_splash_register_phone = (EditText) this.findViewById(R.id.splash_register_et_phone);
        m_splash_register_button_register = (Button) this.findViewById(R.id.splash_register_button_register);
        m_splash_register_button_register.setOnClickListener(this);
        
        
    }
    
    
    
    public void onResume(){
    	super.onResume();
    	
    	
//    	request.execHttpRequest("
//    	http://www.getpaidtowatchyoutube.com/PennApps/register.php?name=?&phone=?&email=?&password=?
//    	"
//    				, HttpMethod.Get, null);
    	
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.equals(m_splash_button_login)){
			String password_text = m_splash_et_password.getText().toString().trim();
			String username_text = m_splash_et_username.getText().toString().trim();
			
			if(password_text.equals("")){
				Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show(); return;}
			if(username_text.equals("")){
				Toast.makeText(this, "Please enter a username.", Toast.LENGTH_SHORT).show(); return;}
			else{
				M_ID = request.execHttpRequest(
						"http://www.getpaidtowatchyoutube.com/PennApps/login.php?email="
								+ username_text + "&password=" + password_text,
								HttpMethod.Get, null);  
				if(M_ID != null && !M_ID.equals("false")){
					
					M_ID.trim();
					editor.putBoolean("logged_in", true);
					editor.putString("M_ID", M_ID);
					editor.commit();
					
					Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show(); 
				    Intent intent = new Intent(this, TabWidgetActivity.class);
				    intent.putExtra("user_id", M_ID);
				    startActivity(intent);
				}
				else
					Toast.makeText(this, "Login failed!", Toast.LENGTH_SHORT).show(); 					
			}
		}
		
		if(v.equals(m_splash_button_register)){
			if(m_splash_wrapper.getVisibility() == View.GONE){
				m_splash_wrapper.setVisibility(View.VISIBLE);
				m_splash_register.setVisibility(View.GONE);
			}
			if(m_splash_register.getVisibility() == View.GONE){
				m_splash_wrapper.setVisibility(View.GONE);
				m_splash_register.setVisibility(View.VISIBLE);
			}
		}
		
		if(v.equals(m_splash_register_button_register)){

			String name = m_splash_register_username.getText().toString().trim();
			String password = m_splash_register_password.getText().toString().trim();
			String email = m_splash_register_email.getText().toString().trim();
			String phone = m_splash_register_phone.getText().toString().trim();

			if(name.equals("") || password.equals("") || email.equals("") || phone.equals("")){
				Toast.makeText(this, "Please fill out all spaces!", Toast.LENGTH_SHORT).show();	return;}

			String f = request.execHttpRequest(
					"http://www.getpaidtowatchyoutube.com/PennApps/register.php?name="
							+ name + "&email=" + email + "&password=" + password + "&phone=" + phone,
							HttpMethod.Get, null); 

			if(f.equals("true")){
				Toast.makeText(this, "Success! You are now registered", Toast.LENGTH_SHORT).show();	onBackPressed(); return;}
			else if(f.equals("email exists\n")){
				Toast.makeText(this, "Failure. Please try again.", Toast.LENGTH_SHORT).show();	return;}
			else{
				Toast.makeText(this, "There has already been an email registered with that password. Sorry!",
						Toast.LENGTH_SHORT).show();	return;}
				
		}

	}
	@Override
	public void onBackPressed(){
		if(m_splash_register.getVisibility() == View.VISIBLE){
			m_splash_register.setVisibility(View.GONE);
			m_splash_wrapper.setVisibility(View.VISIBLE);
			return;
		}
		super.onBackPressed();
	}
	

	@Override
	public void onPause(){
		editor.putString("M_ID", M_ID);
		editor.commit();
		super.onPause();
	}
}