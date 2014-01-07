package com.pc.twitterdemo;

import com.pc.Twitter.TwitterApp;
import com.pc.Twitter.TwitterSession;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	// Consumer key ySrTqr5WKkvMLnmbSq9rg
	// Consumer secret zpovdqptlATNB88DtqgB7luS3CjC83F2B6TpgPDtKg
	private static final String twitter_consumer_key = "nJl9I0u0qs5j1Q0GJv37SQ";
	private static final String twitter_secret_key = "G3Yohj7YK2ngcs78iZY4UXGV3LjIeWM51VK0TJ4On0";
//	private SharedPreferences prefs;
	public static TwitterApp mTwitter;
	static TwitterSession twitterSession;
	Button btntwt;
	TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTwitter = new TwitterApp(this, twitter_consumer_key,
				twitter_secret_key);
		twitterSession = new TwitterSession(MainActivity.this);
		globalconstant.mainActivity = this;
		btntwt = (Button) findViewById(R.id.button1);
		tv = (TextView) findViewById(R.id.textView1);
		btntwt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mTwitter.hasAccessToken()) {
					globalconstant.flagtwitter = true;
					LoginSuccessfully();

					// postToTwitter(edtname.getText().toString());
				} else {
					mTwitter.authorize();
					// LoginSuccessfully();
				}
			}
		});

	}

	public void LoginSuccessfully() {
		if (globalconstant.flagtwitter) {
			String USERNAME = mTwitter.getLoginname();
			System.out.println("USERNAME=" + USERNAME);
			tv.setText(USERNAME);
		}
	}

}
