package pt.ist.meic.cmov.neartweet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

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

	final Messenger mMessenger = new Messenger(new IncomingHandler());
	static TweetsDataSource dataSource = UserData.getBd();

	static HashMap<String, Messenger> updateAdapters = new HashMap<String, Messenger>();

	// To refactor the Code, if the fields are static we can in a simple way put
	// in the AsynTask
	static Socket socket;
	static ObjectOutputStream oos;
	static ObjectInputStream ois;
	String user;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
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
				RegisterUserServiceTask rust = new RegisterUserServiceTask();
				rust.registerUser(user, msg.replyTo);
				break;

			case SEND_TWEET:
				tweet = msg.getData().getString("tweet");
				image = msg.getData().getByteArray("image");
				user = msg.getData().getString("user");
				String location = msg.getData().getString("location");
				SendTweetTask stt = new SendTweetTask(tweet, user, image, location);
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
				SendResponseTweetTask srtt = new SendResponseTweetTask(tweet, user, image, conversationId, privacy);
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
				HashSet<String> answers = new HashSet<String>(msg.getData().getStringArrayList("answers"));
				SendPollTask spt = new SendPollTask(tweet, user, answers);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					spt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
				else
					spt.execute((Void[]) null);
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
		
	}

	public void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
