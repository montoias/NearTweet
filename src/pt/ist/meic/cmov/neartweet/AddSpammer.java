package pt.ist.meic.cmov.neartweet;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class AddSpammer extends AsyncTask<Void, Void, Boolean> {
	
	private String user;
	private String spammer;
	
	
	public AddSpammer(String user, String spammer) {
		this.user = user;
		this.spammer = spammer;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (result) {
			Log.d("Paulo", "Do in backgroud executed correctly ->> Sended Add Spammer ");
		} else {
			Log.d("Paulo", "Do in backgroud executed incorrectly --> NOT Sended!!! Add spammer");
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			Utils.AddSpammer(user, spammer);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
