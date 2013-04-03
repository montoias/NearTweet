package cm.proj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android_test.R;

public class TimeLine extends Activity {

	ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_time_line);
		
		listView = (ListView) findViewById(R.id.list);
		String[] values = Utils.convertTweetToString(MainMenu.mBoundService.tweets);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, android.R.id.text1, values);

		listView.setAdapter(adapter);
		displayTweet();
	}
	
    public void displayTweet() {
    	
    	listView.setOnItemClickListener(
    	        new OnItemClickListener()
    	        {

    	            @Override
    	            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    	               Intent i = new Intent(getBaseContext(), DisplayTweetInfo.class);
    	               i.putExtra("position", position);
    	               startActivity(i);
    	            }   
    	        }       
    	);
    }
	

}
