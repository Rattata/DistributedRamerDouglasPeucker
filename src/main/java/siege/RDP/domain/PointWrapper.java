package siege.RDP.domain;

public class PointWrapper <P extends IPoint> implements IOrderedPoint, IPoint {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	P point;
	int index;
	
	public PointWrapper(P point, int index) {
		this.point = point;
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}

	public double getX() {
		return point.getX();
	}

	public double getY() {
		return point.getY();
	}
	
	public P getPoint(){
		return point;
	}
	
	

}
