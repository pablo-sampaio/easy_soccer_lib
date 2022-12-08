package utility_team;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.utils.Vector2D;
import utility_team.player.Player;


/**
 * Time simples, demonstrado em sala.
 */
public class UtilityTeam extends AbstractTeam {

	public UtilityTeam(String suffix) {
		super("UT-Team-" + suffix, 2, false);
	}

	@Override
	protected void launchPlayer(int uniformNumber, PlayerCommander commander) {
		double x, y;

		switch (uniformNumber) {
		case 1:
			x = -37.0d;
			y = -20.0d;
			break;
		case 2:
			x = -37.0d;
			y = 20.0d;
			break;
		default:
			x = -37.0d;
			y = 0;
		}
		
		Player pl = new Player(commander, new Vector2D(x, y));
		pl.start();
	}

}
