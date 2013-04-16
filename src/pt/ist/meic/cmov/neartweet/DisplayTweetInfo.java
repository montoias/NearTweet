package pt.ist.meic.cmov.neartweet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import pt.ist.meic.cmov.neartweet.R;
import pt.ist.meic.cmov.neartweet.dto.TweetDto;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;


public class DisplayTweetInfo extends Activity {

	TweetsDataSource dataSource = UserData.getBd();
	int position = 0;
	String conversationID;
	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";
	private Button postStatusUpdateButton, responseTweetButton;
	private LoginButton loginButton;
	private ProfilePictureView profilePictureView;
	private TextView greeting;
	private GraphUser user;
	private PendingAction pendingAction = PendingAction.NONE;
	Bitmap bm = null;
	String message = "";
	byte[] imageBytes = null;
	ArrayList<TweetDto> conversation, tweets;
	Messenger mService = UserData.getBoundedMessenger();
	ArrayAdapter<String> adapter;

	final Messenger mMessenger = new Messenger(new IncomingHandler());

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {	//TODO: Broken: Tweets after polls no longer show here even if from same conversation
			switch (msg.what) {
			case NetworkManagerService.UPDATE_ADAPTER:
//				Log.d("Paulo", "TimeLine: mensagem" + msg.getData().getString("tweet"));
//				tweets = dataSource.getAllTweets();
//				conversation = Utils.retrieveTweetDtosSameID(tweets, tweets.get(position).getConversationID());
//				// Need to reverse the tweets in order to show the conversation
//				Collections.reverse(conversation);

				if(!(msg.getData().getInt("type") == TweetDto.TYPE_POLL))
					adapter.add("\t" + msg.getData().getString("tweet"));
				adapter.notifyDataSetChanged();
				break;

			default:
				super.handleMessage(msg);
			}
		}

	}

	private enum PendingAction {
		NONE, POST_TIMELINE
	}

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	public void displayTweetImage() {

		((ListView) findViewById(R.id.listTweetInfo))
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int position, long id) {

						if (conversation.get(position).getImage() != null) {
							imageBytes = conversation.get(position).getImage();
							bm = Utils.convertBytesToBmp(imageBytes);
							((ImageView) findViewById(R.id.displayTweetInfoImageView))
									.setImageBitmap(bm);
						}

						message = conversation.get(position).getTweet();
						String location = conversation.get(position)
								.getLocation();
						Log.d("Paulo", "location" + location);
						((TextView) findViewById(R.id.DisplayLocation))
								.setText("Sent From : " + location);
					}
				});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_tweet_info);
		Intent iin = getIntent();
		InitializeData(iin.getExtras());

		ListView listView = (ListView) findViewById(R.id.listTweetInfo);
		ArrayList<String> values = Utils.convertTweetsToString(conversation);

		// Register on Service the adapter
		try {
			Bundle b = new Bundle();
			b.putString("id", UserData.getUser() + "displayTweetInfo");
			Message msg = Message.obtain(null, NetworkManagerService.REGISTER_TO_RECEIVE_UPDATES);
			msg.setData(b);
			msg.replyTo = mMessenger;
			Log.d("Paulo", msg.replyTo.toString());
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		for (int i = 1; i < conversation.size(); i++)
			values.set(i, "\t" + values.get(i));

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
		listView.setAdapter(adapter);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						DisplayTweetInfo.this.user = user;
						updateUI();
						handlePendingAction();
					}
				});

		profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
		greeting = (TextView) findViewById(R.id.greeting);

		postStatusUpdateButton = (Button) findViewById(R.id.postStatusUpdateButton);
		postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				onClickPostStatusUpdate();
			}
		});

		responseTweetButton = (Button) findViewById(R.id.responseTweetButton);
		responseTweetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				EditText et = (EditText) findViewById(R.id.responseTextTweet);

				try {
					Bundle b = new Bundle();
					b.putString("tweet", et.getText().toString());
					b.putByteArray("image", null);
					b.putString("user", UserData.user);
					b.putString("conversationId", conversationID);
					b.putBoolean("privacy", UserData.getPrivacyInTweets());
					Message msg = Message.obtain(null, NetworkManagerService.SEND_RESPONSE_TWEET);
					msg.setData(b);
					mService.send(msg);

					imageBytes = null;
					et.getText().clear();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				et.getText().clear();
			}
		});

		displayTweetImage();

	}

	private void InitializeData(Bundle b) {
		if (b.get("position") != null) {
			position = (Integer) b.get("position");

			tweets = dataSource.getAllTweets();
			conversation = Utils.retrieveTweetDtosSameID(tweets, tweets.get(position).getConversationID());
			// Need to reverse the tweets in order to show the conversation
			Collections.reverse(conversation);

			message = tweets.get(position).getTweet();
			conversationID = tweets.get(position).getConversationID();

		} else {
			throw new RuntimeException("Should have a position as argument");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.display_tweet_info, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
		try {
			Bundle b = new Bundle();
			b.putString("id", UserData.getUser() + "displayTweetInfo");
			Message msg = Message.obtain(null, 	NetworkManagerService.UNREGISTER_TO_RECEIVE_UPDATES);
			msg.setData(b);
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(DisplayTweetInfo.this).setTitle("cancel")
					.setMessage("permission not granted")
					.setPositiveButton("ok", null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());
		postStatusUpdateButton.setEnabled(enableButtons);
		if (enableButtons && user != null) {
			profilePictureView.setProfileId(user.getId());
			greeting.setText(getString(R.string.hello_user, user.getFirstName()));
		} else {
			profilePictureView.setProfileId(null);
			greeting.setText(null);
		}
	}

	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		pendingAction = PendingAction.NONE;
		if (previouslyPendingAction == PendingAction.POST_TIMELINE)
			postTimeLine();

	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String title = null, alertMessage = null;

		if (error == null) {
			title = "sucess";
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = "sucessfully posted" + id;
		} else {
			title = "error";
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this).setTitle(title).setMessage(alertMessage)
				.setPositiveButton("ok", null).show();
	}

	private void onClickPostStatusUpdate() {
		performPublish(PendingAction.POST_TIMELINE);
	}

	private void postTimeLine() {
		if (hasPublishPermission()) {

			Bundle parameters = new Bundle();
			Request request = new Request();
			if (bm != null) {

				parameters.putByteArray("picture", imageBytes);
				request = Request.newUploadPhotoRequest(
						Session.getActiveSession(), bm, new Request.Callback() {
							@Override
							public void onCompleted(Response response) {
								showPublishResult("post photo",
										response.getGraphObject(),
										response.getError());
							}
						});
				parameters.putString("message", message);
				request.setParameters(parameters);
			} else {

				request = Request.newStatusUpdateRequest(
						Session.getActiveSession(), message,
						new Request.Callback() {
							@Override
							public void onCompleted(Response response) {
								showPublishResult(message,
										response.getGraphObject(),
										response.getError());
							}
						});
			}

			request.executeAsync();

		} else {
			pendingAction = PendingAction.POST_TIMELINE;
		}
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				handlePendingAction();
			} else {
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSIONS));
			}
		}
	}
}
