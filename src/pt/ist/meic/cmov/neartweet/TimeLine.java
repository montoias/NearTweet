package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import pt.ist.meic.cmov.neartweet.PollChoserDialog.PollChoserListener;
import pt.ist.meic.cmov.neartweet.R;
import pt.ist.meic.cmov.neartweet.dto.TweetDto;


public class TimeLine extends Activity implements PollChoserListener {
	TweetsDataSource dataSource = UserData.getBd();
	ListView listView;
	ArrayAdapter<String> adapter;
	static Messenger mService = UserData.getBoundedMessenger();
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetworkManagerService.UPDATE_ADAPTER:
				Log.d("Paulo","TimeLine: mensagem" + msg.getData().getString("tweet"));
				drawTimeLine();
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	public void drawTimeLine() {

		listView = (ListView) findViewById(R.id.list);
		ArrayList<String> values = Utils.convertTweetsToString(dataSource
				.getAllTweets());
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, values);

		listView.setAdapter(adapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_time_line);

		drawTimeLine();

		// Register on Service the adapter
		try {

			Bundle b = new Bundle();
			b.putString("id", UserData.getUser() + "TimeLine");
			Message msg = Message.obtain(null,
					NetworkManagerService.REGISTER_TO_RECEIVE_UPDATES);
			msg.setData(b);
			msg.replyTo = mMessenger;
			Log.d("Paulo", msg.replyTo.toString());
			mService.send(msg);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

		displayTweet();
	}

	public void displayTweet() {

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				TweetDto tweet = dataSource.getAllTweets().get(position);
				if(tweet.getType() == TweetDto.TYPE_POLL) {
					Toast.makeText(getBaseContext(), "Tweet is a poll", Toast.LENGTH_LONG).show();
					showPollChoserDialog(tweet);
				} else {					
					Intent i = new Intent(getBaseContext(), DisplayTweetInfo.class);
					i.putExtra("position", position);
					startActivity(i);
				}
			}
		});
	}
	
	public void showPollChoserDialog(TweetDto tweet) {
		Bundle b = new Bundle();
		b.putString("sender", tweet.getSender());
		b.putStringArrayList("answers", new ArrayList<String>(tweet.getAnswers()));
		DialogFragment dialog = new PollChoserDialog();
		dialog.setArguments(b);
		dialog.show(getFragmentManager(), "PollChoserFragment");
	}
	
	@Override
	public void onDialogChoice(DialogFragment dialog, String answer, String sender) {
		Toast.makeText(this, "Answer: " + answer, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			Bundle b = new Bundle();
			b.putString("id", UserData.getUser() + "TimeLine");
			Message msg = Message.obtain(null,
					NetworkManagerService.UNREGISTER_TO_RECEIVE_UPDATES);
			msg.setData(b);
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}
