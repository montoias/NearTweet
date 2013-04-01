package cm.proj;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.android_test.R;

public class MainMenu extends Activity {

	static final String EXTRA_MESSAGE = "com.example.MainActivity.MESSAGE";
	static Socket socket;
	static ObjectInputStream ois;
	static ObjectOutputStream oos;
	boolean mIsBound;
	static TweetReceiving mBoundService;
	static String user = "Anonimous";
	byte[] image = null;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100, RESULT_LOAD_IMAGE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Intent iin = getIntent();
		Bundle b = iin.getExtras();
		if (b.get("user") != null) {
			user = (String) b.get("user");
		}

		Log.d("Paulo", user);
		doBindService();
		Log.d("Paulo", "binded");

		Button buttonLoadImage = (Button) findViewById(R.id.button4);
		buttonLoadImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
				intent.setType("image/*");
				startActivityForResult(intent, RESULT_LOAD_IMAGE);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void sendTweet(View view) {
		String input = ((EditText) findViewById(R.id.tweetString)).getText()
				.toString();
		SendTweetTask task = new SendTweetTask();
		task.execute(input);

		image = null;
		((EditText) findViewById(R.id.tweetString)).getText().clear();
	}

	public class SendTweetTask extends AsyncTask<String, Void, Boolean> {
		String tweet;

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Log.d("Paulo", "Do in backgroud executed correctly");
			} else {
				Log.d("Paulo", "Do in backgroud executed incorrectly");
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				tweet = params[0];
				Utils.SendTweet(tweet, user, image);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

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
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Bitmap bmp = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				image = stream.toByteArray();

				new AlertDialog.Builder(this).setTitle("Camera")
						.setMessage("Photo has been taken sucessfully")
						.setPositiveButton("ok", null).show();

			} else if (resultCode == Activity.RESULT_CANCELED) {
				new AlertDialog.Builder(this).setTitle("Camera")
						.setMessage("You cancelled the image capture")
						.setPositiveButton("ok", null).show();

			} else {
				new AlertDialog.Builder(this).setTitle("Camera")
						.setMessage(" Image capture failed")
						.setPositiveButton("ok", null).show();
			}
			
		} else if (requestCode == RESULT_LOAD_IMAGE) {
			if (resultCode == Activity.RESULT_OK) {
				Bitmap bmp = (Bitmap) data.getExtras().get("data");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				image = stream.toByteArray();

				new AlertDialog.Builder(this).setTitle("Gallery")
						.setMessage("Photo has been loaded sucessfully")
						.setPositiveButton("ok", null).show();

			} else if (resultCode == Activity.RESULT_CANCELED) {
				new AlertDialog.Builder(this).setTitle("Gallery")
						.setMessage("You canceled the load of a image from the gallery")
						.setPositiveButton("ok", null).show();

			} else {
				new AlertDialog.Builder(this).setTitle("Gallery")
						.setMessage(" Gallery image failed to load")
						.setPositiveButton("ok", null).show();
			}
		}
	}
}
