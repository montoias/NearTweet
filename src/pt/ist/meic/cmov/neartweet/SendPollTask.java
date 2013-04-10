package pt.ist.meic.cmov.neartweet;

import java.io.IOException;
import java.util.HashSet;

import android.os.AsyncTask;
import android.util.Log;

public class SendPollTask extends AsyncTask<Void, Void, Boolean> {
	
	private String question;
	private String user;
	private HashSet<String> answers;
	
	
	public SendPollTask(String question, String user, HashSet<String> answers) {
		this.question = question;
		this.user = user;
		this.answers = answers;
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
			Utils.sendPoll(question, user, answers);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
