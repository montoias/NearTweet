package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class TimeLine extends Fragment {
	TweetsDataSource dataSource = UserData.getBd();
	AdapterView.AdapterContextMenuInfo info;
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
				Log.d("Paulo",
						"TimeLine: mensagem" + msg.getData().getString("tweet"));
				drawTimeLine(); // TODO: redraws everything every time, add to
								// adapter and use adapter notify
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	public void drawTimeLine() {

		listView = (ListView) getActivity().findViewById(R.id.list);
		listView.setAdapter(new CustomAdapter(dataSource.getAllTweets(),getActivity()));
		registerForContextMenu(listView);
		// registerForContextMenu(listView);
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

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

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_display_time_line, container,
				false);
	}

	public void displayTweet() {

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				arg0.showContextMenuForChild(view);
			}
		});
	}

	public void showPollChoserDialog(TweetDto tweet) {
		Bundle b = new Bundle();
		b.putString("question", tweet.getTweet());
		b.putString("asker", tweet.getSender());
		b.putString("conversationId", tweet.getConversationID());
		b.putString("id", tweet.getTweetId());
		b.putStringArrayList("answers",
				new ArrayList<String>(tweet.getAnswers()));
		DialogFragment dialog = new PollChoserDialog();
		dialog.setArguments(b);
		dialog.show(getActivity().getFragmentManager(), "PollChoserFragment");
	}

	@Override
	public void onDestroy() {
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		info = (AdapterContextMenuInfo) menuInfo;

		// In Case it is a Poll jump this step
		if (dataSource.getAllTweets().get(info.position).getType() == TweetDto.TYPE_POLL) {
			displayTweetInfo(info.position);
			return;
		}
		Toast.makeText(getActivity(), "onCreateContextMenu " + info.position,
				Toast.LENGTH_LONG).show();

		menu.setHeaderTitle(dataSource.getAllTweets().get(info.position)
				.getSender());
		menu.add(Menu.NONE, v.getId(), 0, "Reply Private");
		menu.add(Menu.NONE, v.getId(), 0, "Reply All");
		menu.add(Menu.NONE, v.getId(), 0, "Add To SpamList");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int position = menuInfo.position;
		if (item.getTitle() == "Reply Private") {
			Toast.makeText(getActivity(), "REPLY PRIVATE", Toast.LENGTH_LONG)
					.show();
			UserData.setPrivacyInTweets(true);
			displayTweetInfo(position);
			// Do your working
		} else if (item.getTitle() == "Reply All") {
			Toast.makeText(getActivity(), "REPLY ALL", Toast.LENGTH_LONG)
					.show();
			UserData.setPrivacyInTweets(false);
			displayTweetInfo(position);

			// Do your working
		} else if (item.getTitle() == "Add To SpamList") {
			// UserData.addSpamInfraction(dataSource.getAllTweets().get(position).getSender());
			Toast.makeText(
					getActivity(),
					"Added To the SpamList "
							+ dataSource.getAllTweets().get(position)
									.getSender(), Toast.LENGTH_LONG).show();
			sendSpammerUser(dataSource.getAllTweets().get(position));
			// Do your working
		} else {
			return false;
		}
		return true;
	}

	private void sendSpammerUser(TweetDto tweetDto) {
		try {
			Bundle b = new Bundle();
			b.putString("sender", UserData.getUser());
			b.putString("spammer", tweetDto.getSender());
			Message msg = Message.obtain(null, NetworkManagerService.ADD_SPAMMER);
			msg.setData(b);
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void displayTweetInfo(int position) {
		TweetDto tweet = dataSource.getAllTweets().get(position);
		if (tweet.getType() == TweetDto.TYPE_POLL) {
			if (tweet.getSender().equals(UserData.user)) {
				Intent i = pollResultsChart.execute(getActivity(),
						tweet.getTweetId(), tweet.getTweet());
				// Intent i = pollResultsChart.execute(TimeLine.this);
				startActivity(i);
			} else if (!tweet.isPollAnswered()) {
				Log.d("Paulo", "Answering tweet " + tweet.getTweetId());
				showPollChoserDialog(tweet);
			} else
				Toast.makeText(getActivity(), "Poll already answered",
						Toast.LENGTH_LONG).show();
		} else {
			Intent i = new Intent(getActivity(), DisplayTweetInfo.class);
			i.putExtra("position", position);
			startActivity(i);
		}
	}

}
