package pt.ist.meic.cmov.neartweet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import pt.ist.meic.cmov.neartweet.dto.TweetDto;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Utils {
	

	public Utils() {
	}

	public static ArrayList<String> convertTweetsToString(ArrayList<TweetDto> tweets) {
		
		ArrayList<String> values = new ArrayList<String>();
		for (int i = 0; i < tweets.size(); i++) {
//			if(!(tweets.get(i).getType() == TweetDto.TYPE_POLL_ANSWER))
				values.add(convertTweetToString(tweets.get(i)));
		}
		return values;
	}
	
	public static String convertTweetToString(TweetDto tweet) {
		String tweetString = "From: @" + tweet.getSender() + "\n\n\t " + tweet.getTweet();
		if (tweet.getImage() != null) {
			tweetString += " attachment";
		}
		return tweetString;
		
	}
	
	public static ArrayList<TweetDto> retrieveTweetDtosSameID(	ArrayList<TweetDto> tweets, String id) {
		ArrayList<TweetDto> conversation = new ArrayList<TweetDto>();
		HashSet<String> conversationEntities = new HashSet<String>();
		for (TweetDto dto : tweets) {
			if (dto.getConversationID().equalsIgnoreCase(id)){
				conversation.add(dto);
				if(dto.getReceivingEntities() != null)
					for(String entity: dto.getReceivingEntities())
						conversationEntities.add(entity);
			}
		}
		
		for(TweetDto tdto: conversation)
			tdto.setReceivingEntities(conversationEntities);

		return conversation;

	}

	public static void SendTweet(String tweet, String user, byte[] image, String location, NetworkManagerService nms)
			throws IOException {

		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
		tweetDto.setLocation(location);
		tweetDto.setPrivacy(false);
		tweetDto.setAvatar(UserData.getAvatar());				

		String id = System.currentTimeMillis() + user;
		tweetDto.setTweetId(id);
		tweetDto.setConversationID(id);
		
		nms.getConnectionManager().sendMessages(tweetDto);

	}
	
	public static void AddSpammer(String user, String spammer, NetworkManagerService nms) throws IOException {

		TweetDto tweetDto = new TweetDto();
		tweetDto.setSender(user);
		tweetDto.setSpammer(spammer);
		tweetDto.setType(TweetDto.TYPE_SPAMMER);
		tweetDto.setAvatar(UserData.getAvatar());
		String id = System.currentTimeMillis() + user;
		tweetDto.setTweetId(id);
		tweetDto.setConversationID(id);
		nms.getConnectionManager().sendMessages(tweetDto);
	}
	
	

	public static void SendResponseTweet(String tweet, String user, byte[] image, String id, boolean privacy, boolean isPollAnswer, String asker, NetworkManagerService nms) throws IOException {
		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
		tweetDto.setAvatar(UserData.getAvatar());
		if(isPollAnswer) {
			tweetDto.setType(TweetDto.TYPE_POLL_ANSWER);
			tweetDto.setTweetId(System.currentTimeMillis() + user + " " + asker);
		} else
			tweetDto.setTweetId(System.currentTimeMillis() + user);
		tweetDto.setConversationID(id);
		tweetDto.setPrivacy(privacy);
		
		if(privacy){
			for(TweetDto dto : retrieveTweetDtosSameID(NetworkManagerService.dataSource.getAllTweets(), id)) {
				tweetDto.addReceivingEntities("@" + dto.getSender());
			}
			//Add myself to the list
			tweetDto.addReceivingEntities("@" + user);
		}
		
		nms.getConnectionManager().sendMessages(tweetDto);

	}
	
	public static void sendPoll(String question, String user, HashSet<String> answers, NetworkManagerService nms) throws IOException {
		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(question);
		tweetDto.setType(TweetDto.TYPE_POLL);
		tweetDto.setSender(user);
		tweetDto.setAnswers(answers);
		tweetDto.setImage(null);
		tweetDto.setPrivacy(false);
		tweetDto.setLocation(null);
		tweetDto.setAvatar(UserData.getAvatar());
		
		String id = System.currentTimeMillis() + user;
		tweetDto.setTweetId(id);
		tweetDto.setConversationID(id);

		nms.getConnectionManager().sendMessages(tweetDto);
		
		TimeLine.pollResultsChart.addNewPoll(tweetDto.getTweetId(), new ArrayList<String>(answers));
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
	
	public static ArrayList<String> convertBytesToArray(byte[] bytes){
		ArrayList<String> list;
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			list = (ArrayList<String>) ois.readObject();
			return list;
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public static byte[] convertObjectoToBytes(Serializable obj) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();

	}
}