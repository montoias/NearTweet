package pt.ist.meic.cmov.neartweet;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class OptionsMenu extends Fragment implements OnClickListener {

	public View onCreateView(LayoutInflater inflater, ViewGroup container, 	Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_options__menu, container, false);

		Button tweet = (Button) v.findViewById(R.id.Tweet);
        tweet.setOnClickListener(this);
        Button pool = (Button) v.findViewById(R.id.Pool);
        pool.setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
        case R.id.Tweet:
        	
        	FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Fragment fragment = new TweetActivity();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
            		android.R.anim.slide_out_right, android.R.anim.slide_in_left,
            		android.R.anim.slide_out_right);

            fragmentTransaction.replace(R.id.options_menu, fragment, null);
            fragmentTransaction.addToBackStack(null); 
            fragmentTransaction.commit();
   
            break;
		case R.id.Pool:
        	
        	fragmentManager = getFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();

            Fragment fragmentPoll = new PollActivity();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right, android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);

            fragmentTransaction.replace(R.id.options_menu, fragmentPoll, null);
            fragmentTransaction.addToBackStack(null); 
            fragmentTransaction.commit();
			
			
			break;
		}
	}

}
