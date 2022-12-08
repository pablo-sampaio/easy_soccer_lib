package bt_lib.example;

import bt_lib.BTNode;
import bt_lib.BTStatus;


class NodeTestaIgual extends BTNode<MyAgent> {
	private int numberToCompare;
	
	NodeTestaIgual(int x) {
		super("TestaEq" + x);
		this.numberToCompare = x;
	}
	
	@Override
	public BTStatus tick(MyAgent agent) {
		if (agent.status == this.numberToCompare) {
			return BTStatus.SUCCESS;
		}
		return BTStatus.FAILURE;
	}
	
}

