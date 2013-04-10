package cm.proj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import secure.sms.spam.SpamFilter;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import dto.ResponseDto;
import dto.TweetDto;
import dto.UserNameDto;

public class RegisterUserServiceTask {
	
	public RegisterUserServiceTask() { }
	
	// used to invoke AsyncTask to register the user
	public void registerUser(String user, Messenger msg) {

		RegisterUserTask rtt = new RegisterUserTask(user, msg);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			rtt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		else
			rtt.execute((Void[]) null);
	}

	// Register user Task
	public class RegisterUserTask extends AsyncTask<Void, Void, Boolean> {
		Messenger replyTo;
		String user;

		public RegisterUserTask(String _input, Messenger msg) {
			user = _input;
			this.replyTo = msg;
		}

		@Override
		protected void onPostExecute(Boolean loginSucessfull) {

			Bundle b = new Bundle();
			b.putBoolean("registerUserResult", loginSucessfull);
			Message msg = Message.obtain(null, NetworkManagerService.RGST_CLIENT_RSP);
			msg.setData(b);
			try {
				replyTo.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (loginSucessfull)
				startReceivingTweets();

		}

		@Override
		protected Boolean doInBackground(Void... params) { // This runs on a
															// different thread
			try {
				NetworkManagerService.socket = new Socket("10.0.2.2", 8081);
				Log.d("Paulo", "Socket Created");

				NetworkManagerService.oos = new ObjectOutputStream(NetworkManagerService.socket.getOutputStream());
				Log.d("Paulo", "oos Created");

				UserNameDto info = new UserNameDto(user);

				NetworkManagerService.oos.writeObject(info);
				NetworkManagerService.oos.flush();

				NetworkManagerService.ois = new ObjectInputStream(NetworkManagerService.socket.getInputStream());
				Log.d("Paulo", "ois Created");

				ResponseDto response = (ResponseDto) NetworkManagerService.ois.readObject();
				String serverResponse = response.getResponse();

				if (serverResponse.equalsIgnoreCase("OK")) {
					Log.d("Paulo", "resposta: " + serverResponse);

					return true;
				}
				Log.d("Paulo", "resposta: " + serverResponse);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return false;

		}

	}
	
	public void startReceivingTweets() {
		ReceiveTweetTask task = new ReceiveTweetTask();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		else
			task.execute((Void[]) null);

	}

	public class ReceiveTweetTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				SpamFilter spamFilter = new SpamFilter();
				
				while (true) {
					Log.i("Paulo", "Cheguei");
					TweetDto tweetDto;
					String tweetMessage = Utils.convertTweetToString(tweetDto);
					String sender = tweetDto.sender;
					tweetDto = (TweetDto) NetworkManagerService.ois.readObject();
					
					if(!UserData.isSpammer(sender)){
						
						if(spamFilter.isSpam(tweetMessage)){
							Log.d("Paulo", "Spam detected: " + tweetMessage);
							UserData.addSpamInfraction(sender);
							if(UserData.isSpammer(sender)){
								Log.d("Paulo", "New spammer: " + sender);
								continue;
							}
						}
						//Add to the DB
						NetworkManagerService.dataSource.createTweet(tweetDto);
						
						//Update adapters
						for(Messenger messenger: NetworkManagerService.updateAdapters.values()){
							Log.d("Paulo", "size " + NetworkManagerService.updateAdapters.size());
							Bundle b = new Bundle();
							
							b.putString("tweet", tweetMessage);
							Message msgData = Message.obtain(null, NetworkManagerService.UPDATE_ADAPTER);
							msgData.setData(b);
							try {
								messenger.send(msgData);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						Log.i("Paulo", NetworkManagerService.dataSource.toString());
					}
					else
						Log.d("Paulo", "Spam infractor: " + sender);
					
				}

			} catch (IOException e) {
				//When socket is closed
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}