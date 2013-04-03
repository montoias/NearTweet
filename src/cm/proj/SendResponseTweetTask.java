package cm.proj;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class SendResponseTweetTask extends AsyncTask<Void, Void, Boolean> {

	private int conversationId;
	private byte[] image;
	private String user;
	private String tweet;

	public SendResponseTweetTask(String tweet, String user, byte[] image, int conversationId) {
		this.tweet = tweet;
		this.user = user;
		this.image=image;
		this.conversationId= conversationId;
	
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {	
			//TODO: images on responses
			Utils.SendResponseTweet(tweet, user, image, conversationId);
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

		//NOT HERE- have to find a better way
/*		finish();
		startActivity(getIntent());
*/		
	}

}