package pt.ist.meic.cmov.neartweet;

import java.util.TreeMap;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import android.os.Messenger;

public class UserData {

	private static final Integer SPAM_LIMIT = 1;

	static Messenger mService;
	static String user;
	static boolean privacyInTweets = false;
	static TreeMap<String, SpamData> spammersList = new TreeMap<String, SpamData>();
	static TweetsDataSource dataSource;
	static byte[] avatar;

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

	public static void addSpamInfraction(TweetDto tweetDto){
		String user = tweetDto.getSender();
		String spammer = tweetDto.getSpammer();
		
		if(spammersList.containsKey(spammer)){
			if(!spammersList.get(spammer).getVotingUsers().contains(user)){
				spammersList.get(spammer).getVotingUsers().add(user);
				spammersList.get(spammer).addVotes();
			}
		} else {
			SpamData sd = new SpamData();
			sd.addVotes();
			sd.getVotingUsers().add(user);
			spammersList.put(spammer, sd);
		}
			
	}

	public static boolean isSpammer(String user){
		if(spammersList.containsKey(user) && spammersList.get(user).getVotes() > SPAM_LIMIT)
			return true;

		return false;
	}

	public static byte[] getAvatar() {
		return avatar;
	}

	public static void setImage(byte[] avatar) {
		UserData.avatar = avatar;
	}
}
