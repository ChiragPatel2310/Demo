/**
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * http://www.londatiga.net
 */

package com.pc.Twitter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import com.pc.twitterdemo.MainActivity;
import com.pc.twitterdemo.globalconstant;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

public class TwitterApp {
	private Twitter mTwitter;
	private TwitterSession mSession;
	private AccessToken mAccessToken;
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;

	private OAuthProvider mHttpOauthprovider;
	private String mConsumerKey;
	private String mSecretKey;
	private ProgressDialog mProgressDlg;
	private TwDialogListener mListener;
	private Context context;
	private boolean mInit = true;

	public static final String CALLBACK_URL = "http://www.xoomsolutions.com/";
	private static final String TAG = "TwitterApp";

	public TwitterApp(Context context, String consumerKey, String secretKey) {
		this.context = context;

		mTwitter = new TwitterFactory().getInstance();
		mSession = new TwitterSession(context);
		mProgressDlg = new ProgressDialog(context);

		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mConsumerKey = consumerKey;
		mSecretKey = secretKey;

		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(mConsumerKey,
				mSecretKey);
		mHttpOauthprovider = new DefaultOAuthProvider(
				"https://twitter.com/oauth/request_token",
				"https://twitter.com/oauth/access_token",
				"https://twitter.com/oauth/authorize");

		// mHttpOauthprovider = new DefaultOAuthProvider(
		// "https://api.twitter.com/oauth/request_token",
		// "https://api.twitter.com/oauth/authorize",
		// "https://api.twitter.com/oauth/access_token");
		// mHttpOauthprovider.setOAuth10a(true);
		// mHttpOauthprovider.retrieveAccessToken(consumer, "");
		mAccessToken = mSession.getAccessToken();

		configureToken();
	}

	public void setListener(TwDialogListener listener) {
		mListener = listener;
	}

	@SuppressWarnings("deprecation")
	private void configureToken() {
		if (mAccessToken != null) {
			if (mInit) {
				mTwitter.setOAuthConsumer(mConsumerKey, mSecretKey);
				mInit = false;
			}

			mTwitter.setOAuthAccessToken(mAccessToken);
		}
	}

	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();

			mAccessToken = null;
		}
	}

	public String getUsername() {
		return mSession.getUsername();
	}

	public String getLoginname() {
		return mSession.getLoginname();
	}

	public void updateStatus(String status) throws Exception {
		try {

			// StatusUpdate s=new StatusUpdate("");
			// s.media(new File(""));
			// mTwitter.updateStatus(s);
			mTwitter.updateStatus(status);
		} catch (TwitterException e) {
			throw e;
		}
	}

	public void authorize() {
		mProgressDlg.setMessage("Initializing ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {
				String authUrl = "";
				int what = 1;
				try {
					authUrl = mHttpOauthprovider.retrieveRequestToken(
							mHttpOauthConsumer, CALLBACK_URL);
					what = 0;
					Log.d(TAG, "Request token url " + authUrl);
				} catch (Exception e) {
					globalconstant.flagtwitter = false;
					Log.d(TAG, "Failed to get request token");
					mSession.resetAccessToken();
					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler
						.obtainMessage(what, 1, 0, authUrl));
			}
		}.start();
	}

	public void processToken(String callbackUrl) {
		mProgressDlg.setMessage("Finalizing ...");
		mProgressDlg.show();

		final String verifier = getVerifier(callbackUrl);

		new Thread() {
			@Override
			public void run() {
				int what = 1;

				try {
					mHttpOauthprovider.retrieveAccessToken(mHttpOauthConsumer,
							verifier);

					mAccessToken = new AccessToken(
							mHttpOauthConsumer.getToken(),
							mHttpOauthConsumer.getTokenSecret());

					configureToken();

					User user = mTwitter.verifyCredentials();

					mSession.storeAccessToken(mAccessToken, user.getName(),
							user.getScreenName());
					System.err.println("Username.ID.." + user.getId()
							+ ".....Name...." + user.getScreenName()
							+ "..Email ID.");
					what = 0;
				} catch (Exception e) {
					Log.d(TAG, "Error getting access token");

					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();
	}

	private String getVerifier(String callbackUrl) {
		String verifier = "";

		try {
			callbackUrl = callbackUrl.replace("twitterapp", "http");
			URL url = new URL(callbackUrl);
			String query = url.getQuery();
			String array[] = query.split("&");
			for (String parameter : array) {
				String v[] = parameter.split("=");

				if (URLDecoder.decode(v[0]).equals(
						oauth.signpost.OAuth.OAUTH_VERIFIER)) {
					verifier = URLDecoder.decode(v[1]);
					break;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return verifier;
	}

	private void showLoginDialog(String url) {
		final TwDialogListener listener = new TwDialogListener() {
			@Override
			public void onComplete(String value) {
				processToken(value);
			}

			@Override
			public void onError(String value) {
				mListener.onError("Failed opening authorization page");
			}
		};

		new TwitterDialog(context, url, listener).show();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mProgressDlg != null) {
				mProgressDlg.dismiss();
			}
			try {
				if (msg.what == 1) {
					if (msg.arg1 == 1) {
						mListener.onError("Error getting request token");
					} else {
						if (mListener != null) {
							mListener.onError("Error getting access token");
						}
					}
				} else {
					if (msg.arg1 == 1) {
						if (msg.obj != null) {
							showLoginDialog((String) msg.obj);
						}
					} else {
						globalconstant.flagtwitter = true;
						if (mListener != null) {
							mListener.onComplete(" ");
						}

						globalconstant.mainActivity.LoginSuccessfully();

					}
				}
			} catch (Exception e) {
				// TODO: handle exception\
				// System.out.println("Exception..."+e);
				/*
				 * //System.out.println("Exception..."+e);
				 * if(globalconstant.flagfromsettingfotwitter){ new
				 * LoginScreen_FromSetting().LoginSuccessfullyfromsetting();
				 * }else{ new LoginScreen().LoginSuccessfully(); }
				 */

			}

		}
	};

	public interface TwDialogListener {
		public void onComplete(String value);

		public void onError(String value);
	}
}