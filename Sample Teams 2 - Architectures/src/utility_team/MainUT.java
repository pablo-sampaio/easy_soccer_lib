package utility_team;

import java.net.UnknownHostException;


public class MainUT {

	public static void main(String[] args) throws UnknownHostException {
		UtilityTeam team1 = new UtilityTeam("A");
		UtilityTeam team2 = new UtilityTeam("B");
		
		team1.launchTeamAndServer();
		team2.launchTeam();
	}
	
}

