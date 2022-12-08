package easy_soccer_lib;

import java.net.UnknownHostException;

/**
 * Esta classe facilita lancar um time de agentes, possivelmente junto com o servidor.
 * <br><br>
 * Para criar um time, basta estender esta classe e implementar o metodo <b>launchPlayer()</b> para que,
 * a cada chamada deste m�todo (na forma de callback), a sua classe instancie algum agente (com a 
 * classe que quiser) para atuar no jogo. Cada chamada vai receber uma inst�ncia diferente de 
 * {@link easy_soccer_lib.PlayerCommander} que serve para que a classe do agente envio comandos para 
 * e receba percep��es do jogo.
 * 
 * Em seguida, o seu time pode ser inicializado com facilidade usando os metodos 
 * {@link AbstractTeam#launchTeam()} ou {@link AbstractTeam#launchTeamAndServer()}.
 * 
 * @author Pablo Sampaio
 */
public abstract class AbstractTeam {
	private String hostName;
	private int port;

	private String teamName;
	private int numPlayers;
	private boolean withGoalie;
	
	public AbstractTeam(String name, int players, String host, int port, boolean withGoalie) {
		this.hostName = host;
		this.port = port;
		this.teamName = name;
		this.numPlayers = players;
		this.withGoalie = withGoalie;
	}
	
	public AbstractTeam(String name, int players, boolean withGoalie) {
		this.hostName = "localhost";
		this.port = 6000;
		this.teamName = name;
		this.numPlayers = players;
		this.withGoalie = withGoalie;
	}

	/**
	 * Uma subclasse (um time) deve implementar este metodo, que sera chamado uma vez para cada jogador do time. 
	 * A cada chamada, a subclasse deve instanciar alguma classe para controlar o agente (provavelmente em uma thread)
	 * por meio do objeto PlayerCommander dado como parametro. 
	 * <br><br>
	 * O par�metro inteiro � o n�mero do uniforme, que vem do servidor, mas normalmente � sequencial e iniciado em 1. 
	 * Equivale ao valor de {@link easy_soccer_lib.PlayerCommander#getMyUniformNumber()}. Geralmente o n�mero 1 � o goleiro,
	 * mas � melhor confirmar com {@link easy_soccer_lib.PlayerCommander#amIaGollie()}.
	 */
	protected abstract void launchPlayer(int uniform, PlayerCommander commander);
	

	public final void launchTeam(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				PlayerCommander commander;
				
				System.out.println(" >> Iniciando o time...");
				for (int i = 0; i < AbstractTeam.this.numPlayers; i++) {
					try{
						if(i == 0){
							commander = new PlayerCommander(teamName, hostName, port, withGoalie);
						}else{
							commander = new PlayerCommander(teamName, hostName, port, false);
						}
						while (! commander.setupCompleted()) {
							//Thread.onSpinWait();
							Thread.yield();
						}
						launchPlayer(commander.getMyUniformNumber(), commander);
					}catch(UnknownHostException uhe){
						System.err.println("N�o foi poss�vel conectar ao host: " + AbstractTeam.this.hostName);
						uhe.printStackTrace();
					}
					try {
						Thread.sleep(250);
					} catch (Exception e) {}
				}
			}
		}).start();
	}
	
	public final void launchTeamAndServer() throws UnknownHostException {
		launchServer();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		launchTeam();
	}
	
	public final void launchServer() {
		try {
			System.out.println(" >> Iniciando servidor...");
			
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("cmd /c tools\\startServer.cmd");
			p.waitFor();
//			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line = "";
//			while ((line = b.readLine()) != null) {
//			  System.out.println(line);
//			  System.out.println(".");
//			}
//			b.close();

        } catch(Exception e) {
        	e.printStackTrace();
        	System.err.println("N�o pode iniciar o servidor!");
        	return;
        }
	}
	
}
