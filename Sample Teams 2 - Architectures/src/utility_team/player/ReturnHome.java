package utility_team.player;

import easy_soccer_lib.utils.Vector2D;


public class ReturnHome extends AbstractBehavior {
	private Vector2D home;
	
	ReturnHome(Vector2D homePos) {
		this.home = homePos;
	}

	@Override
	public void perform(Player agent) {
		// agente AINDA não faz nada -- IMPLEMENTE!
	}

	@Override
	public double utility(Player agent) {
		if (!agent.isPlayerClosestToBallInTeam()) {
			return 0.5;
		} else {
			return 0.0;
		}
	}


}
