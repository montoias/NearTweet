package cm.proj;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class SendTweetTask extends AsyncTask<Void, Void, Boolean> {
	private String tweet;
	private byte[] image = null;
	private String user;
	private String location;

	public SendTweetTask(String tweet, String user, byte[] image,
			String location) {
		this.tweet = tweet;
		this.user = user;
		this.image = image;
		this.location = location;
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
			// Check for any possible image in the url
			if (image == null)
				image = checkForImageUrl(tweet);

			Utils.SendTweet(tweet, user, image, location);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public byte[] checkForImageUrl(String tweet) {
		try {
			String[] tweetParsed = tweet.split(" ");
			for (String each : tweetParsed) {
				if (each.contains("www.") || each.contains("http://")) {
					if(each.contains("www."))
						each = "http://" + each;
					InputStream in = new java.net.URL(each).openStream();
					Bitmap bm = BitmapFactory.decodeStream(in);
					if (bm != null)
						return Utils.convertBmpToBytes(bm);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

}
