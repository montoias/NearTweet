package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import pt.ist.meic.cmov.neartweet.PollChoserDialog.PollChoserListener;
import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class FragmentMainMenu extends Activity implements PollChoserListener,
		ActionBar.TabListener {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main_menu);

		// OptionsMenu firstFragment = new OptionsMenu();
		// getSupportFragmentManager().beginTransaction().add(R.id.options_menu,
		// firstFragment).commit();

		// ----- to try out

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		actionBar.addTab(actionBar.newTab().setText("Tweet")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Poll")
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Blocked\nFriends")
				.setTabListener(this));

		// correct ----

		TimeLine secondFragment = new TimeLine();
		getFragmentManager().beginTransaction()
				.add(R.id.TimeLine, secondFragment).commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fragment_main_menu, menu);
		return true;
	}

	protected void onDestroy() {
		super.onDestroy();
		UserData.getBd().onDestroy();
	}

	@Override
	public void onDialogChoice(DialogFragment dialog, String answer) {
		Toast.makeText(this, "Answer: " + answer, Toast.LENGTH_LONG).show();

		try {
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
			Message msg = Message.obtain(null,
					NetworkManagerService.SEND_RESPONSE_TWEET);
			msg.setData(b);
			UserData.mService.send(msg);
			ArrayList<TweetDto> tweets = UserData.dataSource.getAllTweets();
			for (TweetDto tweet : tweets)
				if (tweet.getTweetId().equals(id)) {
					tweet.setPollAnswered(); // TODO: tweets are not being
												// marked as answered
					Log.d("Paulo", "Marking tweet " + tweet.getTweetId()
							+ " as marked");
					// break;
				}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		Log.d("Paulo", "position -----------> " + tab.getPosition());
		switch (tab.getPosition()) {
		case 0:
			TweetActivity firstFragment = new TweetActivity();
			getFragmentManager().beginTransaction()
					.replace(R.id.options_menu, firstFragment).commit();
			break;

		case 1:
			PollActivity pollFragment = new PollActivity();
			getFragmentManager().beginTransaction()
					.replace(R.id.options_menu, pollFragment).commit();
			break;

		default:
			break;
		}

		// OptionsMenu firstFragment = new OptionsMenu();
		// getSupportFragmentManager().beginTransaction()
		// .add(R.id.options_menu, firstFragment).commit();

	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}
}
