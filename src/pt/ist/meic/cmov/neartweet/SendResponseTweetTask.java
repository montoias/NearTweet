package pt.ist.meic.cmov.neartweet;

import java.io.IOException;
import java.util.ArrayList;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;

import android.os.AsyncTask;
import android.util.Log;

public class SendResponseTweetTask extends AsyncTask<Void, Void, Boolean> {

	private String conversationId;
	private byte[] image;
	private String user;
	private String tweet;
	private ArrayList<TweetDto> tweets;
	private boolean privacy;
	private boolean isPollAnswer;
	private String asker;
	
	public SendResponseTweetTask(String tweet, String user, byte[] image, String conversationId, boolean privacy, boolean isPollAnswer, String asker) {
		this.tweet = tweet;
		this.user = user;
		this.image=image;
		this.conversationId = conversationId;
		this.privacy = privacy;
		this.isPollAnswer = isPollAnswer;
		this.asker = asker;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {	
			//TODO: images on responses
			
			Utils.SendResponseTweet(tweet, user, image, conversationId, privacy, isPollAnswer, asker);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Log.d("Paulo", "Do in backgroud executed correctly");	
		} else {
			Log.d("Paulo", "Do in backgroud executed incorrectly");
		}

	}

}