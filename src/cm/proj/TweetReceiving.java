package cm.proj;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import dto.TweetDto;

public class TweetReceiving extends Service {

	private final IBinder mBinder = new LocalBinder();
	ArrayList<TweetDto> tweets = new ArrayList<TweetDto>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ReceiveTweetTask task = new ReceiveTweetTask();
		task.execute();
		return Service.START_NOT_STICKY;
	}

	public class LocalBinder extends Binder {
		TweetReceiving getService() {
			return TweetReceiving.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class ReceiveTweetTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				while (true) {
					Log.i("Paulo", "Cheguei");
					TweetDto tweetDto;
					tweetDto = (TweetDto) MainMenu.ois.readObject();
					tweets.add(tweetDto);
					Log.i("Paulo", tweets.toString());
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
	}
}
