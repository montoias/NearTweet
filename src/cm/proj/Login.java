package cm.proj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.example.android_test.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import dto.ResponseDto;
import dto.UserNameDto;

public class Login extends Activity {

	Socket socket; // Network Socket
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
		
	}

	public void register(View view) {

		String input = ((EditText) findViewById(R.id.userString)).getText()
				.toString();

		NetworkTask task = new NetworkTask(input, this);
		task.execute();

	}

	public class NetworkTask extends AsyncTask<Void, Void, Boolean> {
		ObjectOutputStream oos;
		ObjectInputStream ois;
		String input;
		Activity activity;

		public NetworkTask(String _input, Login login) {
			input = _input;
			activity = login;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Intent i = new Intent(activity, MainMenu.class);
				i.putExtra("user", input);
				startActivity(i);

				Intent service = new Intent(activity, TweetReceiving.class);
				startService(service);

			} else {
				Toast.makeText(activity,
						"The Client already Exists. Try Another User Name",
						Toast.LENGTH_LONG).show();

			}
		}

		@Override
		protected Boolean doInBackground(Void... params) { // This runs on a
															// different thread
			try {

				socket = new Socket("10.0.2.2", 8081); // connect to // server
				UserNameDto info = new UserNameDto(input);

				oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(info);
				oos.flush();

				ois = new ObjectInputStream(socket.getInputStream());
				ResponseDto response = (ResponseDto) ois.readObject();
				String serverResponse = response.getResponse();

				if (serverResponse.equalsIgnoreCase("OK")) {
					Log.d("Paulo", "resposta: " + serverResponse);
					MainMenu.socket = socket;
					MainMenu.oos = oos;
					MainMenu.ois = ois;

					return true;
				}
				Log.d("Paulo", "resposta: " + serverResponse);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;

		}

	}

}
