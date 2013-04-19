package pt.ist.meic.cmov.neartweet;

import java.util.HashSet;

public class SpamData {

	private HashSet<String> votingUsers = new HashSet<String>();
	private int votes = 0;
	
	
	public int getVotes() {
		return votes;
	}
	public void addVotes() {
		this.votes++;
	}
	public HashSet<String> getVotingUsers() {
		return votingUsers;
	}

}
