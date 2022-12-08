package example.organized_team;

import java.net.UnknownHostException;


public class MainOrg {

	public static void main(String[] args) throws UnknownHostException {
		OrganizedTeam team1 = new OrganizedTeam("Sport");
		OrganizedTeam team2 = new OrganizedTeam("Nautico");
		
		team1.launchTeamAndServer();
		team2.launchTeam();
	}
	
}
