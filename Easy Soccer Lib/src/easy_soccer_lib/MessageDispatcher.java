package easy_soccer_lib;

import java.util.ArrayList;
import java.util.List;

import easy_soccer_lib.utils.EFieldSide;

/**
 * Class used to send messages (broadcasts) between teams.
 * 
 * Temporary solution! Create a single solution together with a new 
 * class for sending perceptions (in a single thread).
 * 
 * @author Pablo A. Sampaio
 *
 */
class MessageDispatcher {
	private static MessageDispatcher instance = new MessageDispatcher(); 
		
	/*private String team1;
	private String msgTeam1;
	private int tickTeam1;
	
	private String team2;
	private String msgTeam2;
	private int tickTeam2;*/
	
	private List<PlayerCommander> teamLeft;
	private List<PlayerCommander> teamRight;

	private MessageDispatcher() {
		teamLeft = new ArrayList<PlayerCommander>(11);
		teamRight = new ArrayList<PlayerCommander>(11);
	}
	
	static MessageDispatcher getInstance() {
		return MessageDispatcher.instance;
	}
	
	synchronized void register(PlayerCommander p) {
		if (p.getMyFieldSide() == EFieldSide.LEFT) {
			teamLeft.add(p);
		} else {
			teamRight.add(p);
		}
	}
	
	void sendMessage(PlayerCommander sender, String message, int tick) {
		List<PlayerCommander> team;
		if (sender.getMyFieldSide() == EFieldSide.LEFT) {
			team = teamLeft;
		} else {
			team = teamRight;
		}
		
		for (PlayerCommander p : team) {
			if (p != sender) {
				p.addNewMessage(new AgentMessage(tick,message));
			}
		}
	}
	
	/*
	// DEL
	static MessageDispatcher getInstance(String teamLeft, String teamRight) {
		if (teamLeft != null) {
			MessageDispatcher.instance.team1 = teamLeft;
		}
		if (teamRight != null) {
			MessageDispatcher.instance.team2 = teamRight;
		}
		return MessageDispatcher.instance;
	}

	//DELE
	String getMessage(PlayerCommander receiver) {
		int delta;
		if (receiver.getMyTeamName().equals(team1)) {
			delta = receiver.match.getTime() - tickTeam1;
			if (delta == 1) {
				return msgTeam1;
			}
		} else {
			delta = receiver.match.getTime() - tickTeam2;
			if (delta == 1) {
				return msgTeam2;
			}
		}
		return null;
	}
	
	//DEL
	void sendMessageOld(PlayerCommander sender, String message) {
		int tick = sender.match.getTime();
		
		if (sender.getMyTeamName().equals(team1) && this.tickTeam1 != tick) {
			this.msgTeam1 = message;
			this.tickTeam1 = tick;
		} else if (this.tickTeam2 != tick) {
			this.msgTeam2 = message;
			this.tickTeam2 = tick;
		}
	}
	*/
}
