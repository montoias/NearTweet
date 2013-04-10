package cm.proj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import dto.TweetDto;

public class TweetsDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	private String[] allColumns = {
			MySQLiteHelper.COLUMN_TWEET_ID,
			MySQLiteHelper.COLUMN_TWEET_DTO,
	};


	public TweetsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		Log.d("Paulo", "DataBase Opened");
		Log.d("Paulo", MySQLiteHelper.DATABASE_CREATE);
		database = dbHelper.getWritableDatabase();
	}

	public void onDestroy(){
		dbHelper.deleteDb(database);
		dbHelper.onCreate(database);
	}
	
	public void close() {
		dbHelper.close();
	}

	public void createTweet(TweetDto dto) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TWEET_DTO, convertDtoToBytes(dto));
		long insertId = database.insert(MySQLiteHelper.TABLE_TWEETS, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_TWEETS,	allColumns, MySQLiteHelper.COLUMN_TWEET_ID + " = " + insertId,
				null, null, null, null);
		cursor.moveToFirst();
		cursor.close();

	}

	public ArrayList<TweetDto> getAllTweets() {
		ArrayList<TweetDto> tweets = new ArrayList<TweetDto>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_TWEETS, allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			TweetDto dto = cursorToComment(cursor);
			tweets.add(0,dto);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();

		return tweets;
	}

	private TweetDto cursorToComment(Cursor cursor) {
		try {
			byte[] dtoBytes = cursor.getBlob(1);
			ByteArrayInputStream in = new ByteArrayInputStream(dtoBytes);
			ObjectInputStream is = new ObjectInputStream(in);
			Object obj = is.readObject();
			return (TweetDto) obj;
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] convertDtoToBytes(TweetDto dto) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(dto);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();

	}

}