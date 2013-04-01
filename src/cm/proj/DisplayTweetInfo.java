package cm.proj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.example.android_test.R;
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

import dto.TweetDto;

public class DisplayTweetInfo extends Activity {

	int position = 0, conversationID = 0;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
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

	private enum PendingAction { NONE, POST_TIMELINE }

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	
	public void displayTweetImage() {
    	
		((ListView) findViewById(R.id.listTweetInfo)).setOnItemClickListener(
    	        new OnItemClickListener()
    	        {

    	            @Override
    	            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    	            	Log.d("Paulo", conversation.toString());
    	            	if (conversation.get(position).getImage() != null) {
    	        			imageBytes = conversation.get(position).getImage();

    	        			ByteArrayInputStream stream = new ByteArrayInputStream(imageBytes);
    	        			bm = BitmapFactory.decodeStream(stream);

    	        			((ImageView) findViewById(R.id.displayTweetInfoImageView)).setImageBitmap(bm);

    	        		} 
    	            }   
    	        }       
    	);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_tweet_info);
		Intent iin = getIntent();
		Bundle b = iin.getExtras();
		if (b.get("position") != null) {
			position = (Integer) b.get("position");
			tweets = MainMenu.mBoundService.tweets;
			conversation = Utils.retrieveTweetDtosSameID(tweets, tweets.get(position).getConversationID());
			message = tweets.get(position).getTweet();
			conversationID = tweets.get(position).getConversationID();
		} else {
			throw new RuntimeException("Should have a position as argument");
		}

		ListView listView = (ListView) findViewById(R.id.listTweetInfo);
		String[] values = Utils.convertTweetToString(conversation);
		
		for(int i = 1; i < conversation.size(); i++)
			values[i] = "\t" + values[i];
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
		listView.setAdapter(adapter);
		
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
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
				EditText et = (EditText)findViewById(R.id.responseTextTweet);
				SendTweetTask task = new SendTweetTask();
				task.execute(et.getText().toString());
			}
		});
		
		displayTweetImage();

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

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		pendingAction = PendingAction.NONE;
		switch (previouslyPendingAction) {
		case POST_TIMELINE:
			postTimeLine();
			break;
		}
	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
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
			Bitmap image = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.com_facebook_icon);

			Request request = new Request();
			if (image != null) {

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
		return session != null	&& session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
			} else {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
			}
		}
	}
	
	
	public class SendTweetTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				
				//TODO: images on responses
				Utils.SendResponseTweet(params[0], MainMenu.user, null, conversationID);
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Log.d("Paulo", "Do in backgroud executed correctly");
				finish();
				startActivity(getIntent());
				
			} else {
				Log.d("Paulo", "Do in backgroud executed incorrectly");
			}
		}
	
	}


}
