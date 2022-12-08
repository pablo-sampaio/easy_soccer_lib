package fsm_team;

import java.util.List;

import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.MatchPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.EMatchState;
import easy_soccer_lib.utils.Vector2D;

enum PlayerState { 
	ATTACKING, 
	DEFENDING,
	RETURN_TO_HOME 
};


public class FsmPlayer extends Thread {
	private static final double ERROR_RADIUS = 1.2d;
	
	private PlayerCommander commander;
	private PlayerState state;
	
	private PlayerPerception selfInfo;
	private FieldPerception  fieldInfo;
	private MatchPerception  matchInfo;
	
	private Vector2D homebase; //posicao base do jogador
	
	
	public FsmPlayer(PlayerCommander player, double x, double y) {
		commander = player;
		homebase = new Vector2D(x, y);
	}
	
	@Override
	public void run() {
		_printf("Waiting initial perceptions...");
		selfInfo  = commander.perceiveSelfBlocking();
		fieldInfo = commander.perceiveFieldBlocking();
		matchInfo = commander.perceiveMatchBlocking();
		
		state = PlayerState.RETURN_TO_HOME; //todos comecam neste estado
		
		_printf("Starting in a random position...");
		                        //x: varia de 0 a +/- 52                                                      // y: varia de -34 a +34
		commander.doMoveBlocking(Math.random() * (selfInfo.getFieldSide() == EFieldSide.LEFT ? -52.0 : 52.0), (Math.random() * 68.0) - 34.0);
 
		if (selfInfo.getFieldSide() == EFieldSide.RIGHT) { //ajusta a posicao base de acordo com o lado do jogador (basta mudar os sinais)
			homebase.setX(- homebase.getX());
			homebase.setY(- homebase.getY());
		}

		try {
			Thread.sleep(3000); // espera, para dar tempo de ver as mensagens iniciais
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		while (commander.isActive()) {
			updatePerceptions();  //deixar aqui, no comeco do loop, para ler o resultado do 'doMove'

			if (matchInfo.getState() == EMatchState.PLAY_ON) {
			    
				switch (this.state) {
			    case ATTACKING:
			    	this.stateAttacking();
			    	break;
			    case DEFENDING:
			    	// TODO: não tem transições para ele ainda
			    	this.stateDefending();
			    	break;
			    case RETURN_TO_HOME:
			    	this.stateReturnToHomeBase();
			    	break;
			    }
			    
			} else {
				// situacoes de bola parada...
				// dica: para implementar de forma simples e geral, basta mover o jogador mais proximo da bola para chutar a bola na direcao do 2o mais proximo
				
			}
		}
			
	}
	
	private void updatePerceptions() {
		PlayerPerception newSelf = commander.perceiveSelf();
		FieldPerception newField = commander.perceiveField();
		MatchPerception newMatch = commander.perceiveMatch();
		
		// so atualiza os atributos se tiver nova percepcao (senao, mantem as percepcoes antigas)
		if (newSelf != null) {
			this.selfInfo = newSelf;
		}
		if (newField != null) {
			this.fieldInfo = newField;
		}
		if (newMatch != null) {
			this.matchInfo = newMatch;
		}
	}

	
	////// Estado RETURN_TO_HOME_BASE ///////
	
	private void stateReturnToHomeBase() {
		if (closestToTheBall()) {
			state = PlayerState.ATTACKING;
			return ;
		}
		
		// se  nao chegou na home base...
		if (! arrivedAt(homebase)) {
			
			if (isAlignedTo(homebase)) {
				_printf_once("RTHB: Running to the base...");
				commander.doDashBlocking(100.0d);			
			} else {
				_printf("RTHB: Turning...");
				commander.doTurnToPointBlocking(homebase);
			}
		}
		//se chegou na home base: fica parado
	}

	private boolean closestToTheBall() {
		Vector2D ballPos = fieldInfo.getBall().getPosition();
		List<PlayerPerception> myTeam = fieldInfo.getTeamPlayers( this.commander.getMyFieldSide() );
										//ou: fieldInfo.getTeamPlayers( selfInfo.getFieldSide() );
		
		double pDist;
		double minDistance = 2000d;
		PlayerPerception playerMinDistance = null;
		
		for (PlayerPerception p : myTeam) {
			pDist = p.getPosition().distanceTo( ballPos );
			if (pDist < minDistance) {
				minDistance = pDist;
				playerMinDistance = p;
			}
		}
		
		return selfInfo.getUniformNumber() == playerMinDistance.getUniformNumber();  
	}
	
	private boolean arrivedAt(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		return Vector2D.distance(myPos, targetPosition) <= ERROR_RADIUS;
	}

	private boolean isAlignedTo(Vector2D targetPosition) {
		Vector2D myPos = selfInfo.getPosition();
		double angle = selfInfo.getDirection().angleFrom(targetPosition.sub(myPos));
		return angle > -15.0d && angle < 15.0d;
	}

	/////// Estado ATTACKING ///////
	
	private void stateDefending() {
		// TODO: mudar para cá quando o adversário estiver próximo do gol -- avançar para a bola
	}
	
	
	/////// Estado ATTACKING ///////
	
	private void stateAttacking() {
		if (! closestToTheBall()) {
			state = PlayerState.RETURN_TO_HOME;
			return;
		}

		Vector2D ballPosition = fieldInfo.getBall().getPosition();
		
		if (arrivedAt(ballPosition)) {
			if (selfInfo.getFieldSide() == EFieldSide.LEFT) {
				_printf_once("ATK: Kicking the ball...");
				commander.doKickToPoint(100.0d, new Vector2D(52.0d, 0.0d));
			} else {
				_printf_once("ATK: Kicking the ball (right side)...");
				commander.doKickToPoint(100.0d, new Vector2D(-52.0d, 0.0d));
			}
			
		} else {
			if (isAlignedTo(ballPosition)) {
				_printf_once("ATK: Running to the ball...");
				commander.doDashBlocking(100.0d);
			} else {
				_printf("ATK: Turning...");
				commander.doTurnToPointBlocking(ballPosition);
			}
		}		
	}

	//for debugging
	public void _printf_once(String format, Object...objects) {
		if (! format.equals(lastformat)) {  //dependendo, pode usar ==
			_printf(format, objects);
		}
	}
	private String lastformat = ""; 
	public void _printf(String format, Object...objects) {
		String playerInfo = "";
		if (selfInfo != null) {
			playerInfo += "[" + selfInfo.getTeam() + "/" + selfInfo.getUniformNumber() + "] ";
		}
		System.out.printf(playerInfo + format + "%n", objects);
		lastformat = format;
	}

}

