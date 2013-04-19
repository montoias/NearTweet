package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SpamFragment extends Fragment {

	ArrayAdapter<String> adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_spam_fragment, container,
				false);
	}

	@Override
	public void onStart() {
		super.onStart();
		Toast.makeText(getActivity(), "size of spammers list: " + UserData.spammersList.size(), Toast.LENGTH_LONG).show();
		ArrayList<String> spammers = new ArrayList<String>();
		ArrayList<SpamData> data = new ArrayList<SpamData>();

		for (String s : UserData.spammersList.keySet()) {
			spammers.add(s);
			data.add(UserData.spammersList.get(s));
		}
		((ListView) getActivity().findViewById(R.id.blockedList))
				.setAdapter(new SpamAdapter(spammers, data, getActivity()));

	}

	public class SpamAdapter extends BaseAdapter {

		private ArrayList<String> _spammers;
		private ArrayList<SpamData> _data;
		Context _c;

		SpamAdapter(ArrayList<String> spammers, ArrayList<SpamData> data, Context c) {
			_data = data;
			_spammers = spammers;
			_c = c;
		}

		public int getCount() {
			return _data.size();
		}

		public Object getItem(int position) {
			return _data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) _c
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.list_spam_message, null);
			}

			String spammer = _spammers.get(position);
			SpamData data = _data.get(position);

			TextView spammerView = (TextView) v.findViewById(R.id.spammer);
			TextView votesView = (TextView) v.findViewById(R.id.numberofvotes);

			spammerView.setText("@" + spammer);
			votesView.setText(" "+ data.getVotes() + " ");

			if (UserData.isSpammer(spammer)) {
				spammerView.setTextColor(Color.RED);
				votesView.setTextColor(Color.RED);
			} else {
				spammerView.setTextColor(Color.parseColor("#33b5e5"));
				votesView.setTextColor(Color.parseColor("#33b5e5"));
			}

			return v;
		}
	}

}
