package pt.ist.meic.cmov.neartweet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class FragmentMainMenu extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main_menu);
		
		OptionsMenu firstFragment = new OptionsMenu();
		getSupportFragmentManager().beginTransaction().add(R.id.options_menu, firstFragment).commit();
		
		TimeLine secondFragment = new TimeLine();
		getSupportFragmentManager().beginTransaction().add(R.id.TimeLine, secondFragment).commit();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fragment_main_menu, menu);
		return true;
	}

}
