package example.organized_team;

import easy_soccer_lib.AbstractTeam;
import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.FieldDimensions;


/**
 * Este time mostra uma ideia básica para implementar formações
 * @author Pablo
 *
 */
public class OrganizedTeam extends AbstractTeam {

	public OrganizedTeam(String suffix) {
		super("Org" + suffix, 4, false);
	}

	@Override
	protected void launchPlayer(int uniform, PlayerCommander commander) {
		double homeX, homeY;
	
		// nesta sequencia de if-elses , as posições foram dadas 
		// assumindo que o time está jogando no lado ESQUERDO do campo
		if (uniform == 1) {
			// defensor direito
			homeX = -FieldDimensions.MAX_X / 2;   //negativo - lado defensivo (lado esquerdo na nossa visão)
			homeY =  FieldDimensions.MAX_Y / 3;   //positivo - posição que aparece mais baixa no monitor (faixa direita do campo, na perspectiva do time)
		} else if (uniform == 2) {
			// defensor esquerdo
			homeX = -FieldDimensions.MAX_X / 2;
			homeY = -FieldDimensions.MAX_Y / 3;
		} else if (uniform == 3) {
			// atacante direito
			homeX =  FieldDimensions.MAX_X / 4;   //positivo - lado ofensivo (=lado do outro time)
			homeY =  FieldDimensions.MAX_Y / 2;
		} else {
			// atacante esquerdo
			homeX =  FieldDimensions.MAX_X / 4;
			homeY = -FieldDimensions.MAX_Y / 2;;
		}
		
		// se o time estiver no lado direito, faz o ajuste
		if (commander.getMyFieldSide() == EFieldSide.RIGHT) {
			homeX = -homeX;
			homeY = -homeY;
		}
		System.out.printf("Jogador %d, lado %s -- x %f, y %f  %n", uniform, commander.getMyFieldSide(), homeX, homeY);
		
		OrganizedPlayer pl = new OrganizedPlayer(commander, homeX, homeY);
		pl.start();
	}

}
