package easy_soccer_lib.perception;

import easy_soccer_lib.utils.Vector2D;

public class ObjectPerception {
	private Vector2D position;
	//private Vector2D direction; // TODO: dire��o do movimento, para vers�es futuras

	public ObjectPerception(Vector2D position){
		this.position = position;
	}
	
	public ObjectPerception(){		
	}
	
	/**
	 * Retorna posicao do objeto no campo
	 */
	public Vector2D getPosition() {
		return position;
	}

	public void setPosition(Vector2D position) {
		this.position = position;
	}
	
	public double distanceTo(Vector2D otherPosition) {
		return this.position.distanceTo(otherPosition);
	}

	public double distanceTo(ObjectPerception obj) {
		return this.position.distanceTo(obj.position);
	}

}