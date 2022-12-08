package fsm_team;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.utils.EFieldSide;


public class FsmTeam extends AbstractTeam {

	public FsmTeam(String suffix) {
		super("Ex1" + suffix, 2, false);
	}

	@Override
	protected void launchPlayer(int unif, PlayerCommander commander) {
		double targetX, targetY;
		
		if (unif == 1) {
			targetY = 34.0d / 2;   //posicao que aparece mais baixa no monitor
		} else {
			targetY = -34.0d / 2;  //posicao mais alta
		}
		
		targetX = -52.5d / 2;	// posicionamento defensivo		
		
		FsmPlayer pl = new FsmPlayer(commander, targetX, targetY);
		pl.start();
	}

}
