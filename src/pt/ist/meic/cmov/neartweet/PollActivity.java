package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;
import java.util.HashSet;

import pt.ist.meic.cmov.neartweet.R;
import pt.ist.meic.cmov.neartweet.dto.TweetDto;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class PollActivity extends Activity {
	
	private ArrayList<String> answers = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	static Messenger mService = UserData.getBoundedMessenger();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll);
		// Show the Up button in the action bar.
		 adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, answers);
		((ListView)findViewById(R.id.answer_list)).setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.poll, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void addAnswer(View view) {
		String answer = ((EditText)findViewById(R.id.answer)).getText().toString();
		
		((EditText)findViewById(R.id.answer)).getText().clear();
		adapter.add(answer);
	}
	
	public void sendPoll(View view) {
		try {

			Bundle b = new Bundle();

			b.putString("user", UserData.user);
			b.putInt("poll", TweetDto.TYPE_POLL);
			b.putString("tweet", ((EditText)findViewById(R.id.question)).getText().toString());
			b.putStringArrayList("answers", answers);

			Message msg = Message.obtain(null, NetworkManagerService.SEND_POLL);
			msg.setData(b);
			mService.send(msg);
		} catch(RemoteException re) {
			re.printStackTrace();
		}
		
		finish();
	}

}
