package pt.ist.meic.cmov.neartweet;

import android.app.Activity;

public class MainMenu extends Activity {

	/*static final String EXTRA_MESSAGE = "com.example.MainActivity.MESSAGE";
	byte[] image = null;
	String user = UserData.getUser();
	static Messenger mService = UserData.getBoundedMessenger();
	double latitude = 0;
	double longitude = 0;
	boolean showLocation = false;
	GPSTracker gps;

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100, RESULT_LOAD_IMAGE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Make sure DM is Initialized.
		if(UserData.getBd() == null) {
			TweetsDataSource tds = new TweetsDataSource(this);
			tds.open();
			
			UserData.setBd(tds);
		}
			
		gps = new GPSTracker(MainMenu.this);

		TextView tv = (TextView) findViewById(R.id.WelcomeMessage);
		tv.setText("Welcome to NearTweet " + UserData.getUser() + "!");		
		Log.d("Paulo", "binded");
	}

	public void displayMyLocation(View arg0) {
		final CheckBox checkBox = (CheckBox) findViewById(R.id.GPS);

		if (checkBox.isChecked()) {
			this.showLocation = true;
		} else {
			this.showLocation = false;
			this.latitude = 0;
			this.longitude = 0;
		}
	}

	public void getLocation() {
		gps.getLocation();
		if (gps.canGetLocation()) {
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			Toast.makeText(
					getApplicationContext(),
					"Your Location is - \nLat: " + latitude + "\nLong: "
							+ longitude, Toast.LENGTH_LONG).show();
		} else {
			gps.showSettingsAlert();
		}
	}

	public void galleryPhoto(View view) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
	
	public void privateTweets(View arg0) {
		final CheckBox checkBox = (CheckBox) findViewById(R.id.PrivateTweet);

		if (checkBox.isChecked()) {
			UserData.setPrivacyInTweets(true);			
		} else {
			UserData.setPrivacyInTweets(false);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void sendTweet(View view) {
		String tweet = ((EditText) findViewById(R.id.tweetString)).getText()
				.toString();

		try {
			String location;
			if(showLocation){
				getLocation();
				location = latitude + " " + longitude ;
			} else {
				location = "Not available";
			}
				
			Bundle b = new Bundle();
			b.putString("tweet", tweet);
			b.putString("user", user);
			b.putString("location", location);
			b.putByteArray("image", image);
			b.putBoolean("privateTweets", UserData.getPrivacyInTweets());
			Message msg = Message.obtain(null, NetworkManagerService.SEND_TWEET);
			msg.setData(b);
			mService.send(msg);

			image = null;
			((EditText) findViewById(R.id.tweetString)).getText().clear();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startPoll(View view) {
		Intent i = new Intent(this, PollActivity.class);
		startActivity(i);
	}

	public void displayTimeLine(View view) {
		Intent i = new Intent(this, TimeLine.class);
		startActivity(i);
	}

	public void takePhoto(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String title = null;
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			title = "Camera";

			switch (resultCode) {

			case Activity.RESULT_OK:
				image = Utils.convertBmpToBytes((Bitmap) data.getExtras().get(
						"data"));

				new AlertDialog.Builder(this).setTitle(title)
						.setMessage(title + " has been loaded sucessfully")
						.setPositiveButton("ok", null).show();
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

		} else if (requestCode == RESULT_LOAD_IMAGE) {
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

				image = Utils.convertBmpToBytes(BitmapFactory.decodeFile(picturePath));

				new AlertDialog.Builder(this).setTitle(title)
						.setMessage(title + " has been loaded sucessfully")
						.setPositiveButton("ok", null).show();
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
	
    protected void onDestroy() {        
        super.onDestroy();
//        UserData.getBd().close();
        UserData.getBd().onDestroy();
    }
*/}