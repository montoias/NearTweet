package pt.ist.meic.cmov.neartweet;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class AddSpammer extends AsyncTask<Void, Void, Boolean> {
	
	private String user;
	private String spammer;
	private NetworkManagerService nms;
	
	
	public AddSpammer(String user, String spammer, NetworkManagerService networkManagerService) {
		this.user = user;
		this.spammer = spammer;
		this.nms = networkManagerService;
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
			Utils.AddSpammer(user, spammer, nms);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
