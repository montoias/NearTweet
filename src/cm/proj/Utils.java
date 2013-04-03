package cm.proj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import dto.TweetDto;

public class Utils {

	public Utils() {
	}

	public static String[] convertTweetToString(ArrayList<TweetDto> tweets) {

		String[] values = new String[tweets.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = "@" + tweets.get(i).getSender() + " " + tweets.get(i).getTweet();
			if (tweets.get(i).getImage() != null) {
				values[i] += " attachment";
			}
		}
		return values;
	}

	public static ArrayList<TweetDto> retrieveTweetDtosSameID(	ArrayList<TweetDto> tweets, int id) {
		ArrayList<TweetDto> conversation = new ArrayList<TweetDto>();
		HashSet<String> conversationEntities = new HashSet<String>();
		for (TweetDto dto : tweets) {
			if (dto.getConversationID() == id){
				conversation.add(dto);
				for(String entity: dto.getReceivingEntities())
					conversationEntities.add(entity);
			}
		}
		
		for(TweetDto tdto: conversation)
			tdto.setReceivingEntities(conversationEntities);

		return conversation;

	}

	public static void SendTweet(String tweet, String user, byte[] image)
			throws IOException {

		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
		MainMenu.oos.writeObject(tweetDto);
		MainMenu.oos.flush();

	}

	public static void SendResponseTweet(String tweet, String user, byte[] image, int id) throws IOException {
		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
		tweetDto.setConversationID(id);
		
		for(TweetDto dto : MainMenu.mBoundService.tweets) {
			if(dto.getConversationID() == id)
				tweetDto.getReceivingEntities().add("@" + dto.getSender());
		}
		MainMenu.oos.writeObject(tweetDto);
		MainMenu.oos.flush();

	}
	
	public static byte[] convertBmpToBytes(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}
	
	
	public static Bitmap convertBytesToBmp(byte[] bytes) {
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		return BitmapFactory.decodeStream(stream);
	}
}