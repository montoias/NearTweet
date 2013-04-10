package secure.sms.spam;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public class SpamFilter {

	// A HashMap to keep track of all words
	HashMap<String, Word> words = new HashMap<String, Word>();

	// How to split the String into tokens
	String splitregex = "\\W";

	// Regex to eliminate junk (although we really should welcome the junk)
	Pattern wordregex;

	public SpamFilter() {
		wordregex = Pattern.compile("\\w+");
	}

	public void init(Context context) {
		try {

			InputStream is = context.getAssets().open("SpamList.txt");
			ObjectInputStream ois = new ObjectInputStream(is);
			
			words = (HashMap<String, Word>) ois.readObject();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private int CalculateBadWordCount(String[] tokens) {
		int spamTotal = 0;
		Word w;

		for (int i = 0; i < tokens.length; i++) {
			String word = tokens[i].toLowerCase();
			Matcher m = wordregex.matcher(word);
			if (m.matches()) {
				spamTotal++;
				if (words.containsKey(word))
					w = (Word) words.get(word);
				else {
					w = new Word(word);
					words.put(word, w);
				}

				// increment count of the word
				w.countBad();
			}
		}
		return spamTotal;
	}

	// Receive a file that is marked as "Good"
	public void trainGood(String[] tokens) throws IOException {

		int goodTotal = CalculateGoodWordCount(tokens);

		// Go through all the words and divide
		// by total words
		for (Word w : words.values())
			w.calcGoodProb(goodTotal);

	}

	// Receive a file that is marked as "Spam"
	public void trainSpam(String[] tokens) throws IOException {

		// For every word token
		int spamTotal = CalculateBadWordCount(tokens);

		// Go through all the words and divide by total words
		for (Word word : words.values())
			word.calcBadProb(spamTotal);

	}

	private int CalculateGoodWordCount(String[] tokens) {
		Word w;
		int goodTotal = 0;

		for (int i = 0; i < tokens.length; i++) {
			String word = tokens[i].toLowerCase();
			Matcher m = wordregex.matcher(word);
			if (m.matches()) {
				goodTotal++;

				if (words.containsKey(word))
					w = (Word) words.get(word);
				else {
					w = new Word(word);
					words.put(word, w);
				}
				w.countGood();
			}
		}

		return goodTotal;
	}

	// This method is derived from Paul Graham:
	// http://www.paulgraham.com/spam.html
	public boolean isSpam(String allWords) {

		/**
		 * Create an arraylist of 15 most "interesting" words Words are most
		 * interesting based on how different their Spam probability is from 0.5
		 **/

		TreeMap<Float, Word> interesting = new TreeMap<Float, Word>();
		Word w;

		// For every word in the String to be analyzed

		String[] tokens = allWords.split(splitregex);

		// Decide What is the best Limit
		int limit = 10;

		for (int i = 0; i < tokens.length; i++) {
			String s = tokens[i].toLowerCase();
			Matcher m = wordregex.matcher(s);
			if (m.matches()) {

				if (words.containsKey(s)) {
					w = (Word) words.get(s);
				} else {
					w = new Word(s);
					w.setPSpam(0.4f);
				}

				if (w.getPSpam() >= 0.1)
					interesting.put(w.interesting(), w);
				// System.out.println(w.getWord() + " " + w.interesting() + " "
				// + w.getPSpam());

			}
		}

		// If the list is bigger than the limit, delete entries
		// at the beginning (the more "interesting" ones are at the
		// end of the list

		RemoveExtraWords(interesting, limit);

		// Apply Bayes' rule (via Graham)
		float pposproduct = 1.0f;
		float pnegproduct = 1.0f;

		// For every word, multiply Spam probabilities ("Pspam") together
		// (As well as 1 - Pspam)

		for (Word word : interesting.values()) {
			// System.out.println(word.getWord() + " " + word.getPSpam());
			pposproduct *= word.getPSpam();
			pnegproduct *= (1.0 - word.getPSpam());
		}

		// Apply formula
		float pspam = pposproduct / (pposproduct + pnegproduct);
		System.out.println("\nSpam rating: " + pspam);

		// If the computed value is great than ???? we have a Spam!!
		if (pspam > 0.5)
			return true;

		return false;

	}

	private void RemoveExtraWords(TreeMap<Float, Word> interesting, int limit) {
		float[] mapKeys = new float[interesting.size()];
		int index = 0, pos = 0;

		for (float key : interesting.keySet()) {
			mapKeys[pos++] = key;
		}

		while (interesting.size() > limit)
			interesting.remove(mapKeys[index++]);

	}

	// Display info about the words in the HashMap
	public void displayStats() {
		Log.d("SPAM", "Size: " + words.size());
		for (Word word : words.values()) {
			// System.out.println(word.getWord() + " pBad: " + word.getPBad() +
			// " pGood: " + word.getPGood() + " pSpam: " + word.getPSpam());
			Log.d("SPAM", word.getWord() + " " + word.getPSpam());
		}
	}

	public HashMap<String, Word> getWords() {
		return words;
	}

	public void setWords(HashMap<String, Word> words) {
		this.words = words;
	}

}
