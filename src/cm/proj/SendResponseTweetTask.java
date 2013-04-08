package cm.proj;

import java.io.IOException;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import dto.TweetDto;

public class SendResponseTweetTask extends AsyncTask<Void, Void, Boolean> {

	private String conversationId;
	private byte[] image;
	private String user;
	private String tweet;
	private ArrayList<TweetDto> tweets;
	private boolean privacy;
	
	public SendResponseTweetTask(String tweet, String user, byte[] image, String conversationId, boolean privacy) {
		this.tweet = tweet;
		this.user = user;
		this.image=image;
		this.conversationId = conversationId;
		this.privacy = privacy;
	
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {	
			//TODO: images on responses
			
			Utils.SendResponseTweet(tweet, user, image, conversationId, privacy);
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