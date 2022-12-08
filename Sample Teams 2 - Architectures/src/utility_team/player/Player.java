package utility_team.player;

import java.util.ArrayList;
import java.util.List;

import easy_soccer_lib.PlayerCommander;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;


public class Player extends Thread {
	//atributos com acesso de pacote: para serem acessados pelos behaviors
	final PlayerCommander commander;

	// percepções vindas do commander
	PlayerPerception selfPerc;
	FieldPerception  fieldPerc;
	
	// percepções mais detalhadas (extraídas das duas acima)
	Vector2D myDirection;
	Vector2D myPosition;
	EFieldSide mySide;
	Vector2D ballPosition;
	ArrayList<PlayerPerception> playersMyTeam;
	ArrayList<PlayerPerception> playerOtherTeam;
	
	Vector2D homePosition;
	Vector2D offensiveGoalPos;
	Vector2D defensiveGoalPos;
	
	private List<AbstractBehavior> behaviors = new ArrayList<AbstractBehavior>();
	
	
	public Player(PlayerCommander player, Vector2D home) {
		commander = player;
		homePosition = home;
		
		//adiciona behaviors -- ordem não improta
		behaviors.add(new AdvanceWithBall());
		behaviors.add(new GetBall());
		behaviors.add(new KickBall());
		behaviors.add(new ReturnHome(home));
	}

	@Override
	public void run() {
		System.out.println(">> 1. Waiting initial perceptions...");
		updatePerceptions(true);
		
		if (selfPerc.getFieldSide() == EFieldSide.LEFT) {
			offensiveGoalPos = new Vector2D(52.0d, 0);
			defensiveGoalPos = new Vector2D(-52.0d, 0);
		} else {
			offensiveGoalPos = new Vector2D(-52.0d, 0);
			defensiveGoalPos = new Vector2D(52.0d, 0);
			homePosition.setX(- homePosition.getX()); 
			homePosition.setY(- homePosition.getY());
		}

		System.out.println(">> 2. Moving to initial position...");
		commander.doMoveBlocking(this.homePosition);	

		System.out.println(">> 3. Now starting...");
		
		int iterationsRemaining = 0;
		AbstractBehavior selectedBehavior = null;
		double bestUtility;
		
		while (commander.isActive()) {
			updatePerceptions(false); //non-blocking
			
			// selecionar o melhor comportamento, quando acabarem as iterações restantes
			if (iterationsRemaining == 0) {
				// percorre todos os comportamentos e seleciona o de maior utility
				bestUtility = -2000.0f;
				double utility;
				
				_printf("Selecting behavior...");
				for (AbstractBehavior b : this.behaviors) {
					utility = b.utility(this);
					_printf(" - behavior %s -- u=%.2f", b, utility);
					if (utility > bestUtility) {
						bestUtility = utility;
						selectedBehavior = b;
					}					
				}
				_printf("Selected behavior: %s", selectedBehavior);
				
				iterationsRemaining = 20;
			}
			
			// executa o comportamento selecionado, em toda iteração
			selectedBehavior.perform(this);
			iterationsRemaining --;
			
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		System.out.println(">> 4. Terminated!");
	}

	private void updatePerceptions(boolean blocking) {
		PlayerPerception newSelf;
		FieldPerception newField;
		
		if (blocking) {
			newSelf = commander.perceiveSelfBlocking();
			newField = commander.perceiveFieldBlocking();
			
		} else {
			newSelf = commander.perceiveSelf();
			newField = commander.perceiveField();
		}
		
		if (newSelf != null) {
			this.selfPerc = newSelf;
			//percepções mais detalhadas
			this.myPosition = newSelf.getPosition();
			this.myDirection = newSelf.getDirection();
			this.mySide = newSelf.getFieldSide();
		}
		if (newField != null) {
			this.fieldPerc = newField;
			//percepções mais detalhadas
			this.ballPosition = newField.getBall().getPosition();
			this.playersMyTeam = newField.getTeamPlayers(this.mySide);
			this.playerOtherTeam = newField.getTeamPlayers(this.mySide.opposite());
		}
	}
	
	/** Algumas funcoes auxiliares que mais de um tipo de behavior pode precisar **/
	
	boolean isCloseTo(Vector2D pos) {
		return isCloseTo(pos, 1.0);
	}
	
	boolean isCloseTo(Vector2D pos, double minDistance) {
		//Vector2D myPos = selfPerc.getPosition();
		//return pos.distanceTo(myPos) < minDistance;
		return myPosition.distanceTo(pos) < minDistance;
	}

	boolean isAlignedTo(Vector2D position) {
		return isAlignedTo(position, 12.0);
	}
	
	boolean isAlignedTo(Vector2D position, double minAngle) {
		if (minAngle < 0) minAngle = -minAngle;
		
		Vector2D myPos = selfPerc.getPosition();
		
		if (position == null || myPos == null) {
			return false;			
		}
		
		//double angle = selfPerc.getDirection().angleFrom(position.sub(myPos));
		double angle = myDirection.angleFrom(position.sub(myPosition));
		return angle < minAngle && angle > -minAngle;
	}
	
	// diz se é o jogador mais perto, dentre TODOS
	boolean isPlayerClosestToBall() {
		PlayerPerception closestPlayer = getPlayerClosestToBall();
		return closestPlayer.getUniformNumber() == selfPerc.getUniformNumber();
	}
	
	// diz se é o jogador mais perto,considerando apenas o time dele
	boolean isPlayerClosestToBallInTeam() {
		PlayerPerception closestPlayer = getPlayerClosestToBall(mySide);
		return closestPlayer.getUniformNumber() == selfPerc.getUniformNumber();
	}

	PlayerPerception getPlayerClosestToBall(EFieldSide side) {
		PlayerPerception closestPlayer = null;
		double minDistance = 2000.0d;
		double dist;
		for (PlayerPerception p : this.fieldPerc.getTeamPlayers(side)) {
			dist = p.getPosition().distanceTo(ballPosition);
			if (dist < minDistance) {
				minDistance = dist;
				closestPlayer = p;
			}
		}
		return closestPlayer;
	}

	PlayerPerception getPlayerClosestToBall() {
		PlayerPerception closestPlayer = null;
		double minDistance = 2000.0d;
		double dist;
		for (PlayerPerception p : fieldPerc.getAllPlayers()) {
			dist = p.getPosition().distanceTo(ballPosition);
			if (dist < minDistance) {
				minDistance = dist;
				closestPlayer = p;
			}
		}
		return closestPlayer;
	}

	//for debugging
	public void _printf_once(String format, Object...objects) {
		if (! format.equals(lastformat)) {
			_printf(format, objects);
		}
	}
	private String lastformat = ""; 
	public void _printf(String format, Object...objects) {
		String playerInfo = "";
		if (selfPerc != null) {
			playerInfo += "[" + selfPerc.getTeam() + "/" + selfPerc.getUniformNumber() + "] ";
		}
		System.out.printf(playerInfo + format + "%n", objects);
		lastformat = format;
	}

}
