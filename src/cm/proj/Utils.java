package cm.proj;

import java.io.IOException;
import java.util.ArrayList;

import dto.TweetDto;

public class Utils {

	public Utils() {
	}

	public static String[] convertTweetToString(ArrayList<TweetDto> tweets) {

		String[] values = new String[tweets.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = "@" + tweets.get(i).getSender() + " "
					+ tweets.get(i).getTweet();
			if (tweets.get(i).getImage() != null) {
				values[i] += " attachment";
			}
		}
		return values;
	}

	public static ArrayList<TweetDto> retrieveTweetDtosSameID(
			ArrayList<TweetDto> tweets, int position) {
		ArrayList<TweetDto> conversation = new ArrayList<TweetDto>();
		for (TweetDto dto : tweets) {
			if (dto.getConversationID() == position)
				conversation.add(dto);
		}

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

	public static void SendResponseTweet(String tweet, String user,
			byte[] image, int id) throws IOException {

		TweetDto tweetDto = new TweetDto();
		tweetDto.setTweet(tweet);
		tweetDto.setSender(user);
		tweetDto.setImage(image);
		tweetDto.setConversationID(id);
		MainMenu.oos.writeObject(tweetDto);
		MainMenu.oos.flush();

	}
}