package easy_soccer_lib;


public class AgentMessage {
	final int tick;
	final String message;

	AgentMessage(int t, String msg) {
		this.tick = t;
		this.message = msg;
	}
}
