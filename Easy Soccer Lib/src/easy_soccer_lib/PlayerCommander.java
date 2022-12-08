package easy_soccer_lib;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import easy_soccer_lib.comm.MonitorConnection;
import easy_soccer_lib.comm.PlayerConnection;
import easy_soccer_lib.perception.FieldPerception;
import easy_soccer_lib.perception.MatchPerception;
import easy_soccer_lib.perception.PlayerPerception;
import easy_soccer_lib.utils.EFieldSide;
import easy_soccer_lib.utils.Vector2D;

/**
 * Esta classe serve para facilitar o controle de um jogador no servidor da RoboCup
 * 2D simulada. Para isso, ele encapsula toda a parte de comunicaï¿½ï¿½o com o servidor.
 * 
 * Ela oferece percepï¿½ï¿½es do jogo (vindas do simulador) na forma de objetos de alto 
 * nï¿½vel e oferece mï¿½todos de alto nï¿½vel para enviar as aï¿½ï¿½es de um jogador.
 *  
 * As percepï¿½ï¿½es funcionam na forma de "consumo". Depois de lida uma vez, sï¿½ haverï¿½
 * uma nova percepï¿½ï¿½o disponï¿½vel quando o servidor enviar nova mensagem. Se for 
 * requisitada uma percepï¿½ï¿½o no intervalo, serï¿½ retornando null.
 * 
 */
public class PlayerCommander extends Thread {
	private static final int WAIT_TIME = 5; //testar diferentes valores
	private static final int SIMULATOR_CYCLE = 100;

	private PlayerConnection communicator;
	private MonitorConnection perceiver;
	private boolean setupDone;
	
	private boolean isGoalie;
	private String teamName;
	private int uniformNumber;
	private EFieldSide fieldSide; //no futuro: 1) incluir no player perception 
	                              //    ou     2) abstrair (para enviar/receber comandos como se atacasse para a direita)
	
	private PlayerPerception me;  //as informacoes sobre o prï¿½prio jogador comandado
	private boolean selfConsumed;   //MANTER apenas se for retornar a cï¿½pia
	private FieldPerception field;  //as informacoes sobre os objetos mï¿½veis do campo: bola e outros jogadores
	private boolean fieldConsumed;
	//deixe com acesso de pacote, por conta de MessageDispatcher
	/*private*/ MatchPerception match; 	// as informacoes sobre a partida: nome dos times, placar, lado do campo, tempo e estado do jogo
	private boolean matchConsumed;
		
	//private Vector2D viewDirection; //direï¿½ï¿½o absoluta da visï¿½o, necessï¿½ria para algumas aï¿½ï¿½es --> agora, acessa direto de "self"
	
	private long nextActionTime;
	
	private MessageDispatcher messageDispatcher; // to send messages (temporarily solution?)
	LinkedList<AgentMessage> messages = new LinkedList<AgentMessage>();
	
	
	/**
	 * Recebe o endereï¿½o do servidor e a posiï¿½ï¿½o inicial do jogador, antes do kick-off.
	 */
	public PlayerCommander(String teamName, String host, int port, boolean isGoalie) throws UnknownHostException {
		InetAddress address = InetAddress.getByName(host);
		
		this.teamName = teamName;
		this.communicator = new PlayerConnection(address, port, teamName); // InetAddress lanï¿½a exceï¿½ï¿½o
		this.perceiver = new MonitorConnection(address);
		
		this.isGoalie = isGoalie;
		this.me = new PlayerPerception();
		this.field = new FieldPerception();
		this.match = new MatchPerception();
		this.selfConsumed = this.fieldConsumed = this.matchConsumed = true;
		
		this.setupDone = false;
		this.start();
	}
	
	/**
	 * Retorna nome do time
	 */
	public String getMyTeamName() {
		return this.teamName;
	}

	/**
	 * Returns the initial side of the player's team. Same as reading the self perception (by calling one of
	 * the appropriate methods), then calling {@link easy_soccer_lib.perception.PlayerPerception#getFieldSide()}.
	 */
	public EFieldSide getMyFieldSide() {
		return this.fieldSide;
	}
	
	/**
	 * Returns the player number (which does not change during a match). Same as reading the self perception (by calling one of
	 * the appropriate methods), then calling {@link easy_soccer_lib.perception.PlayerPerception#getUniformNumber()}.
	 */
	public int getMyUniformNumber() {
		return this.communicator.getInitialUniformNumber();
	}
	
	public boolean amIaGollie() {
		return this.isGoalie;
	}
	
	public boolean setupCompleted() {
		return this.setupDone;
	}
	
	public void run() {
		this.setupDone = false;
		try {
			this.communicator.connect(this.isGoalie); //conecta com o servidor, no modo jogador ou goleiro
			this.uniformNumber = this.communicator.getInitialUniformNumber();
			this.fieldSide = this.communicator.getInitialSide();			
			this.perceiver.connect();    //conecta com o servidor, no modo monitor
			this.messageDispatcher = MessageDispatcher.getInstance();
			this.setupDone = true;
		
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		this.nextActionTime = System.currentTimeMillis();
			
		while (isActive()) {			
					
			synchronized (this) {
				// 1. Recebe as perceï¿½ï¿½es e faz o parsing delas
				// 2. Constroi uma representaï¿½ï¿½o de alto nï¿½vel das percepï¿½ï¿½es
				//    2.1 Calcular a posiï¿½ï¿½o e orientaï¿½ï¿½o absoluta do jogador (self) 
				//    2.2 Calcular as posiï¿½ï¿½es absolutas dos objetos mï¿½veis do campo (field)
				//	  2.3 Calcular as informaï¿½ï¿½es da partida (match)

				//TODO: problema: cria novas instï¿½ncias a cada iteraï¿½ï¿½o -- problema de memï¿½ria
				//      ideia 1: manter uma instï¿½ncia fixa de cada um no monitor, e copiar para os atributos desta classe
				//      ideia 2: manter os atributos desta classe fixos (final?) e sï¿½ retornar cï¿½pias
				FieldPerception newField = (this.fieldConsumed) ? new FieldPerception() : this.field;
				MatchPerception newMatch = (this.matchConsumed) ? new MatchPerception() : this.match;
														
				boolean hasNewPerceptions = this.perceiver.update(newField, newMatch);
				
				if (hasNewPerceptions) {
					this.field = newField;
					this.match = newMatch;
					this.me = this.field.getTeamPlayer(this.fieldSide, this.uniformNumber);
					if (this.me != null) {   //may happen due to some bug in the server
						this.me.setGoalie(this.isGoalie);
						this.selfConsumed = false;
					} else {
						this.selfConsumed = true;
					}
					this.fieldConsumed = false;
					this.matchConsumed = false;
				}
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		this.communicator.bye();
		System.out.println("PlayerCommander finished.");
	}
	
	/**
	 * Verifica se o agente esta conectado
	 */
	public boolean isActive() {
		//to detect if connection with the server was closed
		//queries only the perceiver because it sends messages regularly 
		return perceiver.isActive();
	}
	
	/**
	 * Envia mensagem de broadcast para o time.
	 * A mensagem deve ter até 20 caracteres sem espaço (se tiver, são substituídos por _).
	 * Só pode enviar 1 mensagem por tick.
	 */
	public void doSendMessage(String msg) {
		msg = msg.replace(' ', '_').substring(0, 20);
		this.messageDispatcher.sendMessage(this, msg, match.getTime());
	}
	
	//so pode ser usado pelo MessageDispatcher
	//precisei criar este método porque a lista usada não é thread-safe
	void addNewMessage(AgentMessage m) {
		synchronized (this.messages) {
			this.messages.addFirst(m);
		}
	}
	
	/**
	 * Para receber alguma mensagen enviadas em algum dos dois últimos ticks. 
	 * Se houve mensagens em ambos, retorna a mais antiga.  
	 * Não retorna mensagem enviada a mais de dois ticks, nem mensagem enviada no mesmo tick (muito recente).
	 */
	public String perceiveMessage() {
		int currTick = match.getTime();
		String result = null;

		AgentMessage m;
		
		synchronized (this.messages) {
			while (! this.messages.isEmpty()) {
				m = this.messages.removeFirst();
	
				if ((currTick - m.tick) == 1 || (currTick - m.tick) == 2) {
					result = m.message;  // achou uma mensagem válida para retornar (deve achar a mais antiga primeiro)
					break;				
				} else if ((currTick - m.tick) == 0) {
					result = null;
					this.messages.addFirst(m); // reinsere, porque é recente demais e encerra (porque a lista está na ordem temporal)
					break;
				} else {
					// mensagem antiga: (currTick - m.tick) > 2
					// apenas continua o laço, para remover e analisar a próxima
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Retorna percepcao do agente.
	 * Este metodo nao aguarda novas percepcoes.
	 */
	synchronized public PlayerPerception perceiveSelf() {
		if (selfConsumed) {
			return null;
		}
		PlayerPerception s = this.me;
		//self = null;
		selfConsumed = true;
		return s; //.copy();
	}
	
	/**
	 * Retorna percepcao do agente.
	 * Este metodo aguarda novas percepcoes.
	 */
	public PlayerPerception perceiveSelfBlocking() {
		PlayerPerception s = perceiveSelf();
		while (s == null) {   //TODO: atencao: risco de live lock, ideia: criar um semaforo apenas para as percepï¿½ï¿½es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			s = perceiveSelf();
		}
		return s;  
	}
	
	/**
	 * Retorna percepcoes do campo.
	 * Este metodo nao aguarda novas percepcoes.
	 */
	synchronized public FieldPerception perceiveField() {
		if (fieldConsumed) {
			return null;
		}
		FieldPerception f = this.field;
		//field = null;
		fieldConsumed = true;
		return f; //.copy();
	}
	
	/**
	 * Retorna percepcao do campo.
	 * Este metodo aguarda novas percepcoes.
	 */
	public FieldPerception perceiveFieldBlocking() {
		FieldPerception f = perceiveField();
		while (f == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percepï¿½ï¿½es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			f = perceiveField();
		}
		return f;
	}
	
	/**
	 * Retorna percepcao da partida.
	 * Este metodo nao aguarda novas percepcoes.
	 */
	synchronized public MatchPerception perceiveMatch() {
		if (matchConsumed) {
			return null;
		}
		MatchPerception m = this.match;
		//field = null;
		matchConsumed = true;
		return m; //.copy();
	}
	
	/**
	 * Retorna percepcao da partida.
	 * Este metodo aguarda novas percepcoes.
	 */
	public MatchPerception perceiveMatchBlocking() {
		MatchPerception m = perceiveMatch();
		while (m == null) {   //atencao: risco de live lock, ideia: criar um semaforo apenas para as percepï¿½ï¿½es
			try {
				Thread.sleep(WAIT_TIME/2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m = perceiveMatch();
		}
		return m;
	}
	
	/**
	 * O agente gira o corpo (e a visï¿½o) de modo que o futuro eixo de visao forme 
	 * o dado angulo em relacao ao eixo de visual atual.
	 */
	synchronized public boolean doTurn(double degreeAngle) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		if (degreeAngle > 180.0) {
			degreeAngle -= 360;
		} else if (degreeAngle < -180.0) {
			degreeAngle += 360; 
		}
		communicator.turn(degreeAngle);		
		nextActionTime = System.currentTimeMillis() + SIMULATOR_CYCLE;		
		return true;
	}
	
	public void doTurnBlocking(double degreeAngle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doTurn(degreeAngle);
	}

	/**
	 * Faz o agente girar para uma direcao.
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 */
	synchronized public boolean doTurnToDirection(Vector2D orientation) {
		double angle = orientation.angleFrom(this.me.getDirection());
		return doTurn(angle);
	}
	
	/**
	 * Faz o agente girar para uma direcao.
	 * Este metodo garante o envio do comando e, por isso, pode bloquear a execuï¿½ï¿½o por um tempo 
	 * de atï¿½ SIMULATOR_CYCLE (100ms) para ser executado.
	 */
	public void doTurnToDirectionBlocking(Vector2D orientation) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doTurnToDirection(orientation);
	}
	
	/**
	 * Faz o agente girar para um ponto no campo.
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 */
	synchronized public boolean doTurnToPoint(Vector2D referencePoint) {				
		Vector2D newDirection = referencePoint.sub(this.me.getPosition());
		double angle = newDirection.angleFrom(this.me.getDirection());
		return doTurn(angle);
	}

	/**
	 * Faz o agente girar para um ponto no campo.
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 */
	public void doTurnToPointBlocking(Vector2D referencePoint) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doTurnToPoint(referencePoint);
	}
		
	/**
	 * Faz o agente andar ou correr de acordo com um esforco.
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	synchronized public boolean doDash(double intensity) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		communicator.dash(intensity); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Faz o agente andar ou correr de acordo com um esforco.
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	public void doDashBlocking(double intensity) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doDash(intensity);
	}
	
	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e direcao relativa (angulo em relacao a direcao de movimento/visao do jogador).
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 * @param intensity esforco entre 0 e 100
	 * @param relativeAngle angulo do chute
	 */
	synchronized public boolean doKick(double intensity, double relativeAngle) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		if (relativeAngle > 180.0d) {
			relativeAngle = relativeAngle - 360.0d;
		} else if (relativeAngle < -180.0) {
			relativeAngle = relativeAngle + 360.0d;
		}
		communicator.kick(intensity, relativeAngle); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e direcao relativa (angulo em relacao a direcao de movimento/visao do jogador).
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 * @param intensity esforco entre 0 e 100
	 * @param relativeAngle angulo do chute
	 */
	public void doKickBlocking(double intensity, double relativeAngle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doKick(intensity, relativeAngle);
	}

	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e direcao absoluta (ponto no campo).
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	synchronized public boolean doKickToDirection(double intensity, Vector2D direction) {
		if (System.currentTimeMillis() < nextActionTime) {
			return false;
		}
		double angle = direction.angleFrom(me.getDirection());
		if (angle > 180.0d) {
			angle -= 360.0d;
		} else if (angle < -180.0) {
			angle += 360.0d;
		}
		communicator.kick(intensity, angle);
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}

	/**
	 * Faz o agente chutar, tocar ou conduzir a bola de acordo com um esforco e uma direcao absoluta na forma de um vetor.
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado.
	 * @param intensity esforco entre 0 e 100
	 */
	public void doKickToDirectionBlocking(double intensity, Vector2D direction) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doKickToDirection(intensity, direction);
	}

	synchronized public boolean doKickToPoint(double intensity, Vector2D targetPoint) {
		Vector2D newDirection = targetPoint.sub(me.getPosition());
		return doKickToDirection(intensity, newDirection);
	}

	public void doKickToPointBlocking(double intensity, Vector2D targetPoint) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		doKickToPoint(intensity, targetPoint);
	}

	/**
	 * Move o jogador para a coordenada dada. Isso sï¿½ pode ser feito em alguns
	 * momentos do jogo (por exemplo: no estado "prepare to kickoff").
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado.
	 */
	synchronized public boolean doMove(double x, double y) {
		if (System.currentTimeMillis() < nextActionTime) {  //TESTAR tambï¿½m se PODE, dependendo do status da partida
			return false;
		}
		if (this.getMyFieldSide() == EFieldSide.RIGHT) { // TODO: testar, novidade implementada em mar/2022
			communicator.move(-x, -y);
		} else {
			communicator.move(x, y);
		}
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	public void doMove(Vector2D pos) {
		doMove(pos.getX(), pos.getY());
	}
	
	/**
	 * Move o jogador para a coordenada dada. Isso sï¿½ pode ser feito em
	 * alguns momentos do jogo (por exemplo: antes do kickoff).
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado
	 */
	public void doMoveBlocking(double x, double y) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doMove(x, y);
	}
	public void doMoveBlocking(Vector2D pos) {
		doMoveBlocking(pos.getX(), pos.getY());
	}
	
	/**
	 * Comando exclusivo do goleiro para agarrar a bola (entre -45 e 45 graus).
	 * Este metodo nao aguarda a janela de envio e por isso pode nao ser executado
	 */
	synchronized public boolean doCatch(double angle) {
		if (System.currentTimeMillis() < nextActionTime) {  //TESTAR tambï¿½m se PODE, dependendo do status da partida
			return false;
		}
		communicator.catchBall(angle); 
		nextActionTime += SIMULATOR_CYCLE;
		return true;
	}
	
	/**
	 * Comando exclusivo do goleiro para agarrar a bola (entre -45 e 45 graus).
	 * Este metodo garante a execucao do comando e por isso pode demorar para ser executado
	 */
	public void doCatchBlocking(double angle) {
		while (System.currentTimeMillis() < nextActionTime) {
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		doCatch(angle);
	}
	
	/**
	 * Desconectar agente
	 * */
	public void disconnect() {
		communicator.bye();
	}
}

