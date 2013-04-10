package pt.ist.meic.cmov.neartweet;

import pt.ist.meic.cmov.neartweet.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

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
			Intent i = new Intent(this, MainMenu.class);
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

		user = ((EditText) findViewById(R.id.userString)).getText()
				.toString();
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

}
