package fsm_team;

import java.net.UnknownHostException;


public class MainFsm {

	public static void main(String[] args) throws UnknownHostException {
		FsmTeam team1 = new FsmTeam("FSM");
		//Exercise1Team team2 = new Exercise1Team("Corinthians");
		
		team1.launchTeamAndServer();
		//team2.launchTeam();
	}
	
}
