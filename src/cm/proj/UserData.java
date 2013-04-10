package cm.proj;

import android.os.Messenger;

public class UserData {

	static Messenger mService;
	static String user;
	static boolean privacyInTweets = false;
	static TweetsDataSource dataSource;

	public static void setPrivacyInTweets(boolean privateTweets) {
		UserData.privacyInTweets = privateTweets;
	}
	
	public static boolean getPrivacyInTweets() {
		return privacyInTweets;
	}

	public static String getUser() {
		return user;
	}
	
	public static void setUser(String user) {
		UserData.user = user;
	}
	

	public static void setBoundedMessenger(Messenger _mService) {
		mService = _mService;
	}
	
	public static Messenger getBoundedMessenger(){
		return mService;
	}
	
	public static void setBd(TweetsDataSource bd) {
		dataSource = bd;
	}
	
	public static TweetsDataSource getBd(){
		return dataSource;
	}
}
