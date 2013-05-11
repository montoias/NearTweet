package pt.ist.meic.cmov.neartweet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

	private static final int RESULT_LOAD_IMAGE = 10;
	boolean mIsBound;
	String user;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	Messenger mService = null;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			UserData.setBoundedMessenger(mService);
			Log.d("Paulo", "Binding to Service done");
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			Log.d("Paulo", "I am now unbinded from the Service");
		}
	};

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NetworkManagerService.RGST_CLIENT_RSP:
				registerUserResponse(msg);
				break;

			default:
				super.handleMessage(msg);
			}
		}

	}

	private void registerUserResponse(Message msg) {
		boolean loginSucessfull = msg.getData()
				.getBoolean("registerUserResult");

		if (loginSucessfull) {
			Intent i = new Intent(this, FragmentMainMenu.class);
			UserData.setUser(user);
			startActivity(i);

		} else {
			Toast.makeText(this,
					"The Client already Exists. Try Another User Name",
					Toast.LENGTH_LONG).show();

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		

		TweetsDataSource tds = new TweetsDataSource(this);
		tds.open();
		
		UserData.setBd(tds);
		
		// As soon as the program starts, put the Service running
		Intent service = new Intent(this, NetworkManagerService.class);
		startService(service);

		doBindService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	void doBindService() {
		bindService(new Intent(this, NetworkManagerService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}

		//Close Socket on Service
		try {
			Message msg = Message.obtain(null, NetworkManagerService.CLOSE_SOCKET);
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void register(View view) {

		user = ((EditText) findViewById(R.id.userString)).getText().toString();
		if (!mIsBound) {
			new AlertDialog.Builder(this)
					.setTitle(" Service -- Error ")
					.setMessage(
							user
									+ " The service not initialized yet, wait a few seconds and try again ")
					.setPositiveButton("ok", null);
			return;
		}

		try {
			Bundle b = new Bundle();
			b.putString("user", user);
			Message msg = Message.obtain(null, NetworkManagerService.MSG_REGISTER_CLIENT);
			msg.replyTo = mMessenger;
			msg.setData(b);
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void loadDefaultPic(View view) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String title = null;
		if (requestCode == RESULT_LOAD_IMAGE) {
			title = "Gallery";
			switch (resultCode) {

			case Activity.RESULT_OK:

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				Bitmap bm = BitmapFactory.decodeFile(picturePath);
				UserData.setImage(Utils.convertBmpToBytes(bm));
				bm.recycle();

				new AlertDialog.Builder(this).setTitle(title)
						.setMessage(title + " image has been loaded sucessfully")
						.setPositiveButton("ok", null).show();
				
				findViewById(R.id.RegisterOnServer).setEnabled(true);
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
