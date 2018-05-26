package siege.RDP.domain;

import java.io.Serializable;

public class PointImpl implements IPoint, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7821158289024549129L;
	double X, Y;
	
	public PointImpl(double x, double y) {
		this.X = x;
		this.Y = y;
	}
	
	@Override
	public double getX() {
		return X;
	}

	@Override
	public double getY() {
		return Y;
	}

}
