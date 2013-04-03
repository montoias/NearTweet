package cm.proj;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.example.android_test.R;

public class MainMenu extends Activity {

	static final String EXTRA_MESSAGE = "com.example.MainActivity.MESSAGE";
	static Socket socket;
	static ObjectInputStream ois;
	static ObjectOutputStream oos;
	boolean mIsBound;
	static TweetReceiving mBoundService;
	static String user = "";
	byte[] image = null;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100,
			RESULT_LOAD_IMAGE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent iin = getIntent();
		Bundle b = iin.getExtras();
		if (b.get("user") != null) {
			user = (String) b.get("user");
		}

		doBindService();
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
		String input = ((EditText) findViewById(R.id.tweetString)).getText().toString();
		SendTweetTask task = new SendTweetTask(input, user, image);
		task.execute();

		image = null;
		((EditText) findViewById(R.id.tweetString)).getText().clear();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	public void displayTimeLine(View view) {
		Intent i = new Intent(this, TimeLine.class);
		startActivity(i);
	}

	public void takePhoto(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((TweetReceiving.LocalBinder) service).getService();
			Log.d("Paulo", "Binding to Service done");
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
			Log.d("Paulo", "I am now unbinded from the Service");
		}
	};

	void doBindService() {
		bindService(new Intent(this, TweetReceiving.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
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
				image = Utils.convertBmpToBytes((Bitmap) data.getExtras().get("data"));

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