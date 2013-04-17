package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class PollActivity extends Fragment implements OnClickListener{
	
	private ArrayList<String> answers = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	static Messenger mService = UserData.getBoundedMessenger();

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 	Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_poll, container, false);
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, answers);
		((ListView) getActivity().findViewById(R.id.answer_list)).setAdapter(adapter);	
		
		Button tweet = (Button) getActivity().findViewById(R.id.add_answer_button);
        tweet.setOnClickListener(this);
        Button pool = (Button) getActivity().findViewById(R.id.send_poll_button);
        pool.setOnClickListener(this);
	    
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(getActivity());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void addAnswer(View view) {
		String answer = ((EditText)getActivity().findViewById(R.id.answer)).getText().toString();
		
		((EditText)getActivity().findViewById(R.id.answer)).getText().clear();
		adapter.add(answer);
		
	}
	
	public void sendPoll(View view) {
		try {

			Bundle b = new Bundle();

			b.putString("user", UserData.user);
			b.putInt("poll", TweetDto.TYPE_POLL);
			b.putString("tweet", "Poll: " + ((EditText)getActivity().findViewById(R.id.question)).getText().toString());
			b.putStringArrayList("answers", answers);

			Message msg = Message.obtain(null, NetworkManagerService.SEND_POLL);
			msg.setData(b);
			mService.send(msg);
		} catch(RemoteException re) {
			re.printStackTrace();
		}
		
		getActivity().getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
        case R.id.add_answer_button:
        	Log.d("Paulo", "add_answer");
        	addAnswer(v);
        	break;
        case R.id.send_poll_button:
        	Log.d("Paulo", "send_poll");
        	sendPoll(v);
        	break;
		}
	}

}
