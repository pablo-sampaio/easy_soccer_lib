package example.keyboard_team;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;

public class KeyboardTeam extends AbstractTeam {

	public KeyboardTeam(boolean pair) {
		super("KEYB", pair? 2 : 1, true);
	}

	@Override
	protected void launchPlayer(int uniform, PlayerCommander commander) {
		if (uniform == 1) {
			System.out.println("Player convencional lancado");
			KeyboardPlayer p = new KeyboardPlayer(commander, true);
			p.start();
		} else {
			KeyboardPlayer p = new KeyboardPlayer(commander, false);
			p.start();
		}
	}

	
}
