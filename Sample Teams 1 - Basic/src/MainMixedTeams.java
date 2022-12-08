import java.net.UnknownHostException;

import example.ballfollower_team.BallFollowerTeam;
import example.organized_team.OrganizedTeam;


public class MainMixedTeams {
	
	public static void main(String[] args) throws UnknownHostException {
		BallFollowerTeam team1 = new BallFollowerTeam("A");
		OrganizedTeam team2 = new OrganizedTeam("B");
		
		team1.launchTeamAndServer();
		team2.launchTeam();
	}
	
}
