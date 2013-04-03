package cm.proj;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class SendTweetTask extends AsyncTask<Void, Void, Boolean> {
	private String tweet;
	private byte[] image = null;
	private String user;
	
	public SendTweetTask(String tweet, String user, byte[] image) {
		this.tweet = tweet;
		this.user = user;
		this.image = image;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Log.d("Paulo", "Do in backgroud executed correctly");
		} else {
			Log.d("Paulo", "Do in backgroud executed incorrectly");
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			Utils.SendTweet(tweet, user, image);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
