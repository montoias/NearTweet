package pt.ist.meic.cmov.neartweet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

public class FragmentMainMenu extends FragmentActivity implements
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
		getSupportFragmentManager().beginTransaction()
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
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		Log.d("Paulo", "position -----------> " + tab.getPosition());
		switch (tab.getPosition()) {
		case 0:
			TweetActivity firstFragment = new TweetActivity();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.options_menu, firstFragment).commit();
			break;
			
		case 1:
			PollActivity pollFragment = new PollActivity();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.options_menu, pollFragment).commit();
			break;

		default:
			break;
		}
		
		
//		OptionsMenu firstFragment = new OptionsMenu();
//		getSupportFragmentManager().beginTransaction()
//				.add(R.id.options_menu, firstFragment).commit();

	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

}
