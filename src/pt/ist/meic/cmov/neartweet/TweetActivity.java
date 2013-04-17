package pt.ist.meic.cmov.neartweet;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class TweetActivity extends Fragment {

	public View onCreateView(LayoutInflater inflater, ViewGroup container, 	Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_tweet, container, false);
	}

}
