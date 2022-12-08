package bt_lib.example;

import bt_lib.BTNode;
import bt_lib.BTStatus;


class NodePrint extends BTNode<MyAgent> {
	private String message;
	
	NodePrint(String msg) {
		super("Print");
		this.message = msg;
	}
	
	@Override
	public BTStatus tick(MyAgent data) {
		print("ACAO REALIZADA - Print " + message);
		return BTStatus.SUCCESS;
	}
	
}