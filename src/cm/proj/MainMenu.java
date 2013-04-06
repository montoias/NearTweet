package cm.proj;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.example.android_test.R;

public class MainMenu extends Activity {

	static final String EXTRA_MESSAGE = "com.example.MainActivity.MESSAGE";
	byte[] image = null;
	String user = UserData.getUser();
	static Messenger mService = UserData.getBoundedMessenger();

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100,
			RESULT_LOAD_IMAGE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("Paulo", "binded");
	}

	public void galleryPhoto(View view) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, RESULT_LOAD_IMAGE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void sendTweet(View view) {
		 String tweet = ((EditText) findViewById(R.id.tweetString)).getText().toString();


		try {
			Bundle b = new Bundle();
			b.putString("tweet", tweet);
			b.putByteArray("image", image);
			b.putString("user", user);
			Message msg = Message.obtain(null, NetworkManagerService.SEND_TWEET);
			msg.setData(b);
			mService.send(msg);

			image = null;
			((EditText) findViewById(R.id.tweetString)).getText().clear();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void displayTimeLine(View view) {
		Intent i = new Intent(this, TimeLine.class);
		startActivity(i);
	}

	public void takePhoto(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String title = null;
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			title = "Camera";
		} else if (requestCode == RESULT_LOAD_IMAGE) {
			title = "Gallery";
		}

		if (title != null) {
			switch (resultCode) {

			case Activity.RESULT_OK:
				image = Utils.convertBmpToBytes((Bitmap) data.getExtras().get(
						"data"));

				new AlertDialog.Builder(this).setTitle(title)
						.setMessage(title + " has been loaded sucessfully")
						.setPositiveButton("ok", null).show();
				break;

			case Activity.RESULT_CANCELED:
				new AlertDialog.Builder(this)
						.setTitle(title)
						.setMessage(
								"You canceled the load of a image from the "
										+ title).setPositiveButton("ok", null)
						.show();
				break;

			default:
				new AlertDialog.Builder(this).setTitle(title)
						.setMessage(title + " image failed to load")
						.setPositiveButton("ok", null).show();
				break;
			}

		}

	}
}