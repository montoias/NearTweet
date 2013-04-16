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
	static PollResultsChart pollResultsChart = new PollResultsChart();

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetworkManagerService.UPDATE_ADAPTER:
				Log.d("Paulo","TimeLine: mensagem" + msg.getData().getString("tweet"));
				drawTimeLine();			//TODO: redraws everything every time, add to adapter and use adapter notify
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
					if(tweet.getSender().equals(UserData.user)) {
						Intent i = pollResultsChart.execute(TimeLine.this, tweet.getTweetId(), tweet.getTweet());
//						Intent i = pollResultsChart.execute(TimeLine.this);
						startActivity(i);
					} else if(!tweet.isPollAnswered()) { //TODO: tweets are not being marked as answered
						Log.d("Paulo", "Answering tweet " + tweet.getTweetId());
						showPollChoserDialog(tweet);
					}
					else
						Toast.makeText(TimeLine.this, "Poll already answered", Toast.LENGTH_LONG).show();
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
		b.putString("question", tweet.getTweet());
		b.putString("asker", tweet.getSender());
		b.putString("conversationId", tweet.getConversationID());
		b.putString("id", tweet.getTweetId());
		b.putStringArrayList("answers", new ArrayList<String>(tweet.getAnswers()));
		DialogFragment dialog = new PollChoserDialog();
		dialog.setArguments(b);
		dialog.show(getFragmentManager(), "PollChoserFragment");
	}
	
	@Override
	public void onDialogChoice(DialogFragment dialog, String answer) {
		Toast.makeText(this, "Answer: " + answer, Toast.LENGTH_LONG).show();
		
		try{	
			Bundle info = dialog.getArguments();
			String convId = info.getString("conversationId");
			String id = info.getString("id");
			Bundle b = new Bundle();
			b.putString("tweet", answer);
			b.putString("user", UserData.user);
			b.putString("asker", info.getString("asker"));
			b.putString("conversationId", convId);
			b.putBoolean("privacy", true);
			b.putBoolean("isPollAnswer", true);
			Message msg = Message.obtain(null, NetworkManagerService.SEND_RESPONSE_TWEET);
			msg.setData(b);
			UserData.mService.send(msg);
			ArrayList<TweetDto> tweets = dataSource.getAllTweets();
			for(TweetDto tweet : tweets)
				if(tweet.getTweetId().equals(id)) {
					tweet.setPollAnswered();		//TODO: tweets are not being marked as answered
					Log.d("Paulo", "Marking tweet " + tweet.getTweetId() + " as marked");
//					break;
				}
		} catch(RemoteException e) {
			e.printStackTrace();
		}
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
