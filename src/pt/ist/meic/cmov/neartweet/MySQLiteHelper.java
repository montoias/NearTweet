package pt.ist.meic.cmov.neartweet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_TWEETS = "Tweets";
	public static final String COLUMN_TWEET_ID = "tweetId";
	public static final String COLUMN_TWEET_DTO = "tweetDto";

	public static final String DATABASE_NAME = "tweets.db";
	private static final int DATABASE_VERSION = 8;

	// Database creation sql statement
	static final String DATABASE_CREATE = "create table " + TABLE_TWEETS + "("
			+ COLUMN_TWEET_ID + " integer primary key autoincrement, "
			+ COLUMN_TWEET_DTO + " BLOB" + " );";

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d("Paulo", DATABASE_CREATE);
		database.execSQL(DATABASE_CREATE);
		Log.d("Paulo", "Db created");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWEETS);
		onCreate(db);
	}

	public void deleteDb(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWEETS);
	}

}