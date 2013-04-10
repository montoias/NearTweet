package pt.ist.meic.cmov.neartweet;

import java.util.TreeMap;

import android.os.Messenger;

public class UserData {

	private static final Integer SPAM_LIMIT = 10;

	static Messenger mService;
	static String user;
	static boolean privacyInTweets = false;
	static TreeMap<String, Integer> spammersList = new TreeMap<String, Integer>();
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

	public static void addSpamInfraction(String user){
		if(spammersList.containsKey(user))
			spammersList.put(user, spammersList.get(user) + 1);
		else
			spammersList.put(user, 1);
	}

	public static boolean isSpammer(String user){
		if(spammersList.containsKey(user) && spammersList.get(user) > SPAM_LIMIT)
			return true;

		return false;
	}
}
