package pt.ist.meic.cmov.neartweet;

import java.util.HashMap;
import java.util.HashSet;

import pt.utl.ist.cmov.wifidirect.SimWifiP2pBroadcast;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pManager.Channel;
import pt.utl.ist.cmov.wifidirect.service.SimWifiP2pService;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocketManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class NetworkManagerService extends Service {

	static final int MSG_REGISTER_CLIENT = 0;
	static final int RGST_CLIENT_RSP = 1;
	static final int CLOSE_SOCKET = 2;
	static final int SEND_TWEET = 3;
	static final int SEND_RESPONSE_TWEET = 4;
	static final int REGISTER_TO_RECEIVE_UPDATES = 5;
	static final int UNREGISTER_TO_RECEIVE_UPDATES = 6;
	static final int UPDATE_ADAPTER = 7;
	static final int SEND_POLL = 8;
	static final int ADD_SPAMMER = 9;

	final Messenger mMessenger = new Messenger(new IncomingHandler());
	static TweetsDataSource dataSource = UserData.getBd();

	static HashMap<String, Messenger> updateAdapters = new HashMap<String, Messenger>();

	// To refactor the Code, if the fields are static we can in a simple way put
	
	private Messenger mService = null;
	private boolean mBound = false;
	private SimWifiP2pInfo gInfo = null;
	private WifiDirectManager connectionManager;
	private SimWifiP2pManager mManager = null;
	private Channel mChannel = null;
	private ServiceConnection mConnection = new ServiceConnection() {
		// callbacks for service binding, passed to bindService()

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			mManager = new SimWifiP2pManager(mService);
			mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mManager = null;
			mChannel = null;
			mBound = false;
		}
	};
	
	public SimWifiP2pManager getManager() {
		return mManager;
	}

	public Channel getChannel() {
		return mChannel;
	}
	
	public WifiDirectManager getConnectionManager(){
		return connectionManager;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	
	public void initializeReceiver(){
		// initialize the WDSim API
		SimWifiP2pSocketManager.Init(getApplicationContext());

		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
		SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver(	this);
		registerReceiver(receiver, filter);
		
		// initialize WifiDirectManager
		connectionManager = new WifiDirectManager(this);		
		
		//turn on wifi
		Intent intent = new Intent(this, SimWifiP2pService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mBound = true;		
	}

	class IncomingHandler extends Handler { // Handler of incoming messages from

		// clients.
		@Override
		public void handleMessage(Message msg) {
			byte[] image;
			String user, tweet;
			boolean privacy;
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				user = msg.getData().getString("user");
				// This task also executes the Task of receiving Tweets
				initializeReceiver();
				RegisterUserServiceTask rust = new RegisterUserServiceTask(NetworkManagerService.this);
				rust.registerUser(user, msg.replyTo);
				Toast.makeText(getApplicationContext(), "Wifi is now Online", Toast.LENGTH_LONG).show();
				break;

			case SEND_TWEET:
				tweet = msg.getData().getString("tweet");
				image = msg.getData().getByteArray("image");
				user = msg.getData().getString("user");
				String location = msg.getData().getString("location");
				SendTweetTask stt = new SendTweetTask(tweet, user, image, location, NetworkManagerService.this);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					stt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							(Void[]) null);
				else
					stt.execute((Void[]) null);
				break;

			case CLOSE_SOCKET:
				closeSocket();
				break;

			case SEND_RESPONSE_TWEET:
				image = msg.getData().getByteArray("image");
				user = msg.getData().getString("user");
				tweet = msg.getData().getString("tweet");
				String conversationId = msg.getData().getString("conversationId");
				privacy = msg.getData().getBoolean("privacy");
				boolean isPollAnswer = msg.getData().getBoolean("isPollAnswer");
				String asker = msg.getData().getString("asker");
				SendResponseTweetTask srtt = new SendResponseTweetTask(tweet, user, image, conversationId, privacy, isPollAnswer, asker, NetworkManagerService.this);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					srtt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				else
					srtt.execute((Void[]) null);
				break;

			case REGISTER_TO_RECEIVE_UPDATES:
				registerAdapters(msg);
				break;

			case UNREGISTER_TO_RECEIVE_UPDATES:
				unregisterAdapters(msg);
				break;
				
			case SEND_POLL:
				tweet = msg.getData().getString("tweet");
				user = msg.getData().getString("user");
				HashSet<String> answers = new HashSet<String>(Utils.convertBytesToArray(msg.getData().getByteArray("answers")));
				SendPollTask spt = new SendPollTask(tweet, user, answers, NetworkManagerService.this);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					spt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				else
					spt.execute((Void[]) null);
				break;
				
			case ADD_SPAMMER:
				user = msg.getData().getString("sender");
				String spammer = msg.getData().getString("spammer");
				
				AddSpammer as = new AddSpammer(user, spammer, NetworkManagerService.this);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					as.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				else
					as.execute((Void[]) null);
				break;
				
				
			default:
				super.handleMessage(msg);
			}
		}

	}

	private void unregisterAdapters(Message msg) {
		updateAdapters.remove(msg.getData().get("id"));
	}

	private void registerAdapters(Message msg) {
		updateAdapters.put(msg.getData().getString("id"), msg.replyTo);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyService", "Received start id " + startId + ": " + intent);
		return START_STICKY; // run until explicitly stopped.
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		dataSource.close();
		closeSocket();
		
	}

	public void closeSocket() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	public void setGInfo(SimWifiP2pInfo ginfo2) {
		this.gInfo = ginfo2;
	}

	public SimWifiP2pInfo getGInfo() {
		return gInfo;
	}

}
