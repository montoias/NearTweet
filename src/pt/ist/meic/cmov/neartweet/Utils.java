package pt.ist.meic.cmov.neartweet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

	public static void SendTweet(String tweet, String user, byte[] image, String location)
			throws IOException {

		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
		tweetDto.setLocation(location);
		tweetDto.setPrivacy(false);
						
		tweetDto.setTweetId(System.currentTimeMillis() + user);
		NetworkManagerService.oos.writeObject(tweetDto);
		NetworkManagerService.oos.flush();

	}

	public static void SendResponseTweet(String tweet, String user, byte[] image, String id, boolean privacy, boolean isPollAnswer, String asker) throws IOException {
		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
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
		
		NetworkManagerService.oos.writeObject(tweetDto);
		NetworkManagerService.oos.flush();

	}
	
	public static void sendPoll(String question, String user, HashSet<String> answers) throws IOException {
		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(question);
		tweetDto.setType(TweetDto.TYPE_POLL);
		tweetDto.setSender(user);
		tweetDto.setAnswers(answers);
		tweetDto.setImage(null);
		tweetDto.setPrivacy(false);
		tweetDto.setLocation(null);

		tweetDto.setTweetId(System.currentTimeMillis() + user);
		NetworkManagerService.oos.writeObject(tweetDto);
		NetworkManagerService.oos.flush();
		
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
	
	
	
	public static byte[] convertObjectoToBytes(Object obj) {

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