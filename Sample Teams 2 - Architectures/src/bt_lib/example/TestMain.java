package bt_lib.example;

import java.util.Random;

import bt_lib.BTNode;
import bt_lib.BTStatus;
import bt_lib.Selector;
import bt_lib.Sequence;


public class TestMain {

	private static BTNode<MyAgent> createTree() {
		Sequence<MyAgent> seq1 = new Sequence<MyAgent>("ImprimeSeEstado3");
		seq1.add(new NodeTestaIgual(3));
		seq1.add(new NodePrint("TREEEEEES!"));
		
		Sequence<MyAgent> seq2 = new Sequence<MyAgent>("ImprimeSeEstado2");
		seq2.add(new NodeTestaIgual(2));
		seq2.add(new NodePrint("Dois, dois, dois!!!"));
		
		Selector<MyAgent> root = new Selector<MyAgent>("RAIZ");
		root.add(seq1);
		root.add(seq2);
		
		return root;
	}

	public static void main(String[] args) throws InterruptedException {
		Random statusGenerator = new Random(); 
		BTNode<MyAgent> btree = createTree();
		MyAgent agent1 = new MyAgent(3);

		while (true) {
			System.out.println("\n===== INICIANDO NOVA DECIS�O =====");
			
			agent1.status = statusGenerator.nextInt(4); // gera um "status" de 0 a 3
			
			BTStatus st = btree.tick(agent1);
			System.out.println("STATUS FINAL: " + st);
			
			Thread.sleep(4000);
		}
	}
	
}

