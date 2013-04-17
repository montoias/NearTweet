package pt.ist.meic.cmov.neartweet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class TweetActivity extends Fragment implements OnClickListener{

	String user = UserData.getUser();
	static Messenger mService = UserData.getBoundedMessenger();
	GPSTracker gps;
	double latitude = 0;
	double longitude = 0;
	boolean showLocation = false;
	byte[] image = null;
	
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100, RESULT_LOAD_IMAGE = 10;

	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 	Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_tweet, container, false);
	}
	
	public void onStart() {
	    super.onStart();
	    
		//Make sure DM is Initialized.
		if(UserData.getBd() == null) {
			TweetsDataSource tds = new TweetsDataSource(getActivity());
			tds.open();
			
			UserData.setBd(tds);
		}
			
		gps = new GPSTracker(getActivity());

//		TextView tv = (TextView) getActivity().findViewById(R.id.WelcomeMessage);
//		tv.setText("Welcome to NearTweet " + UserData.getUser() + "!");		
		Log.d("Paulo", "binded");
	    
		
//		getActivity().findViewById(R.id.GPS).setOnClickListener(this);
		getActivity().findViewById(R.id.galleryButton).setOnClickListener(this);
		getActivity().findViewById(R.id.camera).setOnClickListener(this);
		getActivity().findViewById(R.id.tweet).setOnClickListener(this);
		
		
	}
	
	
/*	public void displayMyLocation(View arg0) {
		final CheckBox checkBox = (CheckBox) getActivity().findViewById(R.id.GPS);

		if (checkBox.isChecked()) {
			this.showLocation = true;
		} else {
			this.showLocation = false;
			this.latitude = 0;
			this.longitude = 0;
		}
	}
*/	
	public void getLocation() {
		gps.getLocation();
		if (gps.canGetLocation()) {
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			Toast.makeText(
					getActivity(),
					"Your Location is - \nLat: " + latitude + "\nLong: "
							+ longitude, Toast.LENGTH_LONG).show();
		} else {
			gps.showSettingsAlert();
		}
	}
	
	
	public void sendTweet(View view) {
		String tweet = ((EditText) getActivity().findViewById(R.id.tweetString)).getText().toString();

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
			((EditText) getActivity().findViewById(R.id.tweetString)).getText().clear();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void galleryPhoto(View view) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}

	public void takePhoto(View view) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.GPS:
//			displayMyLocation(v);
//			break;
			
		case R.id.galleryButton:
			galleryPhoto(v);
			break;
			
		case R.id.camera:
			takePhoto(v);
			break;

		case R.id.tweet:
			Toast.makeText(getActivity(), "about to send tweet", Toast.LENGTH_LONG).show();
			sendTweet(v);
			break;	
			
		default:
			break;
		}
		
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

				new AlertDialog.Builder(getActivity()).setTitle(title)
						.setMessage(title + " has been loaded sucessfully")
						.setPositiveButton("ok", null).show();
				break;

			case Activity.RESULT_CANCELED:
				new AlertDialog.Builder(getActivity())
						.setTitle(title)
						.setMessage(
								"You canceled the load of a image from the "
										+ title).setPositiveButton("ok", null)
						.show();
				break;

			default:
				new AlertDialog.Builder(getActivity()).setTitle(title)
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

				Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				image = Utils.convertBmpToBytes(BitmapFactory.decodeFile(picturePath));

				new AlertDialog.Builder(getActivity()).setTitle(title)
						.setMessage(title + " has been loaded sucessfully")
						.setPositiveButton("ok", null).show();
				break;

			case Activity.RESULT_CANCELED:
				new AlertDialog.Builder(getActivity())
						.setTitle(title)
						.setMessage(
								"You canceled the load of a image from the "
										+ title).setPositiveButton("ok", null)
						.show();
				break;

			default:
				new AlertDialog.Builder(getActivity()).setTitle(title)
						.setMessage(title + " image failed to load")
						.setPositiveButton("ok", null).show();
				break;
			}
		}
	}


}
