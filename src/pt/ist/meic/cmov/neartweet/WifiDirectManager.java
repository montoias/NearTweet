package pt.ist.meic.cmov.neartweet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pDeviceList;
import pt.utl.ist.cmov.wifidirect.SimWifiP2pInfo;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;
import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocketServer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class WifiDirectManager {

	private SimWifiP2pSocketServer mSrvSocket = null;
	private HashMap<String, SocketsInfo> mCliSocket = new HashMap<String, SocketsInfo>();
	private String deviceName;

	NetworkManagerService activity;

	public WifiDirectManager(NetworkManagerService activity) {
		this.activity = activity;
	}

	public void updateSockets(SimWifiP2pInfo ginfo, SimWifiP2pDeviceList gdeviceList) {

		activity.setGInfo(ginfo);

		clearsockets();

		if (ginfo.askIsClient()) {
			Log.d("Paulo", "updateMember - get all owners " + ginfo.getAllGroupOwners().size());
			new OutgoingCommTask(ginfo.getAllGroupOwners(), gdeviceList)
					.execute();
		} else {
			Log.d("Paulo", "updateMember - get all devices " + ginfo.getAllDevices().size());
			new OutgoingCommTask(ginfo.getAllDevices(), gdeviceList).execute();
		}

	}

	private void clearsockets() {
		for (SocketsInfo socket : mCliSocket.values()) {
			try {
			//	socket.getSocket().close();
				socket.getOos().close();
			} catch (IOException e) {
				Log.d("Paulo", "clearsockets error");
			}
		}
		mCliSocket.clear();
	}

	public class OutgoingCommTask extends AsyncTask<String, Void, String> {

		Set<String> peerNames;
		SimWifiP2pDeviceList deviceList;

		public OutgoingCommTask(Set<String> peerNames,	SimWifiP2pDeviceList gdeviceList) {

			this.peerNames = peerNames;
			this.deviceList = gdeviceList;
		}

		@Override
		protected String doInBackground(String... params) {
				deviceName = activity.getGInfo().getDeviceName();
				Log.d("Paulo", "MyName: " + deviceName + " peers size: " + peerNames.size());
				for (String peer : peerNames) {
					try {
						SimWifiP2pSocket socket = new SimWifiP2pSocket(deviceList.getByName(peer).getVirtIp(), Integer.parseInt(activity.getString(R.string.port)));
						SocketsInfo socketInfo = new SocketsInfo(socket);
						mCliSocket.put(peer,socketInfo);
						TweetDto tweetDto = new TweetDto();
						tweetDto.setConversationID("-2");
						tweetDto.setTweet(deviceName);
						ObjectOutputStream oos = socketInfo.getOos();
						oos.writeObject(tweetDto);
						oos.flush();
						Log.d("Paulo", "added peer -> " + peer);
					} catch (UnknownHostException e) {
						Log.d("Paulo", "Unknown Host:" + e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
						Log.d("Paulo", "IO error:" + e.getMessage());
					}
				}

			return "nothing happened";
		}

	}

	public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

		String user;
		Messenger replyTo;

		public IncommingCommTask(String user, Messenger replyTo) {
			this.user = user;
			this.replyTo = replyTo;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Bundle b = new Bundle();
			Message msg = Message.obtain(null,	NetworkManagerService.RGST_CLIENT_RSP);
			Log.d("Paulo", "IncommingCommTask started (" + this.hashCode()	+ ").");

			try {
				mSrvSocket = new SimWifiP2pSocketServer(Integer.parseInt(activity.getString(R.string.port)));
				b.putBoolean("registerUserResult", true);

				msg.setData(b);
				replyTo.send(msg);
				Log.d("Paulo", "Wifi On ");

			} catch (IOException e) {

			} catch (RemoteException e) {
				b.putBoolean("registerUserResult", false);
				msg.setData(b);
				Log.d("Paulo", "Wifi Offline ");
				return null;
			}

			while (!Thread.currentThread().isInterrupted()) {
				try {
					SimWifiP2pSocket sock = mSrvSocket.accept();
					Log.d("Paulo", "accepted a socket");
					for (String peer : mCliSocket.keySet()) {
						if (mCliSocket.get(peer).getSocket().isClosed())
							mCliSocket.remove(mCliSocket.get(peer));
					}

					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						new ReceiveCommTask(sock).executeOnExecutor(	AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
					else
						new ReceiveCommTask(sock).execute();

				} catch (IOException e) {
					Log.d("Error accepting socket:", e.getMessage());
					break;
				}
			}
			
			Log.d("Paulo", "SHOULD NOT HAPPEN");
			return null;
		}

	}

	public void turnOnWifi(String user, Messenger replyTo) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			new IncommingCommTask(user, replyTo).executeOnExecutor(
					AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		else
			new IncommingCommTask(user, replyTo).execute();

	}

	public void disconnectPeer() {
		if (!mCliSocket.isEmpty()) {
			try {
				for (SocketsInfo socket : mCliSocket.values()){
					socket.getOos().close();
			//		socket.getSocket().close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mCliSocket.clear();

	}

	public void sendMessages(TweetDto tweetDto, String receivedFrom) {
		Log.d("Paulo", "sendMessages : " + mCliSocket.size());
		for (String socket : mCliSocket.keySet()) {
			if (socket.equalsIgnoreCase(receivedFrom))
				continue;
			try {
				Log.d("Paulo", "about to send to " + socket);
				mCliSocket.get(socket).getOos().writeObject(tweetDto);
				mCliSocket.get(socket).getOos().flush();
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Paulo", "error sending tweet");
			}
		}
	}

	public class ReceiveCommTask extends	AsyncTask<Void, Object, Void> {
		SimWifiP2pSocket s;
		String receivedFrom;
		
		public ReceiveCommTask(SimWifiP2pSocket socket) {
			this.s = socket;
		}

		@Override
		protected Void doInBackground(Void... params) {
			ObjectInputStream sockIn;

			try {
				TweetDto tweetDto;
				Object obj;

				Log.d("Paulo", "before inputstream ");
				sockIn = new ObjectInputStream(s.getInputStream());
				Log.d("Paulo", "after inputstream");

				while ((tweetDto = (TweetDto) sockIn.readObject()) != null) {
					Log.d("Paulo", "obj readed");
					
					if(tweetDto.getConversationID().equals("-2")){
						receivedFrom = tweetDto.getTweet();
						Log.d("Paulo", "Received from" + receivedFrom);
						continue;
					}

					String tweetMessage = Utils.convertTweetToString(tweetDto);
					String sender = tweetDto.getSender();

					Log.d("Paulo", " user: " + tweetDto.getSender());

					if (!UserData.isSpammer(sender)) {

						if (tweetDto.getType() == TweetDto.TYPE_SPAMMER) {
							Log.d("Paulo", "spammer: " + tweetDto.getSpammer() + " user: " + tweetDto.getSender());
							UserData.addSpamInfraction(tweetDto);
							continue;
						}

						// send tweets
						Log.d("Paulo","sendMessages : " + mCliSocket.size());
						for (String socket : mCliSocket.keySet()) {
							if (socket.equalsIgnoreCase(receivedFrom))
								continue;
							try {
								Log.d("Paulo", "about to send to " + socket);
								mCliSocket.get(socket).getOos().writeObject(tweetDto);
								mCliSocket.get(socket).getOos().flush();
							} catch (IOException e) {
								Log.d("Paulo", "couldn't send the tweet to " + socket);
							}
						}

						Log.d("Paulo", "conversation id : " + tweetDto.getConversationID());
						if(!tweetDto.getPrivacy()){
							// Add to the DB
							NetworkManagerService.dataSource.createTweet(tweetDto);
	
							if (tweetDto.getType() == TweetDto.TYPE_POLL_ANSWER && ((tweetDto.getTweetId().split(" "))[1]).equals(UserData.user)) {
								Log.d("Paulo", "Tweet of type " + tweetDto.getType() + " received from " + (tweetDto.getTweetId().split(" "))[1]);
								TimeLine.pollResultsChart.updateCounter( tweetDto.getConversationID(),  tweetDto.getTweet());
							}
	
							// Update adapters
							updateAdapters(tweetMessage, tweetDto);
							Log.i("Paulo", NetworkManagerService.dataSource.toString());
						} else {
							for(String entities: tweetDto.getReceivingEntities()){
								if(entities.contains(UserData.getUser())){
									// Add to the DB
									NetworkManagerService.dataSource.createTweet(tweetDto);
			
									if (tweetDto.getType() == TweetDto.TYPE_POLL_ANSWER && ((tweetDto.getTweetId().split(" "))[1]).equals(UserData.user)) {
										Log.d("Paulo", "Tweet of type " + tweetDto.getType() + " received from " + (tweetDto.getTweetId().split(" "))[1]);
										TimeLine.pollResultsChart.updateCounter( tweetDto.getConversationID(),  tweetDto.getTweet());
									}
			
									// Update adapters
									updateAdapters(tweetMessage, tweetDto);
									Log.i("Paulo", NetworkManagerService.dataSource.toString());
									break;
								}
							}
						}
					} else
						Log.d("Paulo", "Spam infractor: " + sender);
			}
			} catch (IOException e) {
				//e.printStackTrace();
				Log.d("Paulo", "Changed Sockets");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!s.isClosed()) {
				try {
					s.close();
				} catch (Exception e) {
					Log.d("Error closing socket:", e.getMessage());
				}
			}
			s = null;
		}
	}

	public void updateAdapters(String tweetMessage, TweetDto tweetDto) {
		for (Messenger messenger : NetworkManagerService.updateAdapters.values()) {
			Log.d("Paulo", "size " + NetworkManagerService.updateAdapters.size());
			Bundle b = new Bundle();

			b.putString("tweet", tweetMessage);
			b.putInt("type", tweetDto.getType());
			b.putString("id", tweetDto.getConversationID());
			Message msgData = Message.obtain(null, NetworkManagerService.UPDATE_ADAPTER);
			msgData.setData(b);
			try {
				messenger.send(msgData);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendMessages(TweetDto dto) {
		Log.d("Paulo","sendMessages - size of mCliSocket: " + mCliSocket.size());
		for (String socket : mCliSocket.keySet()) {
			try {
				Log.d("Paulo", "about to send to " + socket);
				mCliSocket.get(socket).getOos().writeObject(dto);
				mCliSocket.get(socket).getOos().flush();
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Paulo", "error sending tweet");
			}
		}

		if(dto.getType() == TweetDto.TYPE_SPAMMER){
			Log.d("Paulo", "spammer: " + dto.getSpammer() + " user: " + dto.getSender());
			UserData.addSpamInfraction(dto);
			return;
		}
		// Add to the DB
		NetworkManagerService.dataSource.createTweet(dto);

		// Update adapters
		updateAdapters(Utils.convertTweetToString(dto), dto);

	}
}
