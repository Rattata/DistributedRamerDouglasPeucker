package siege.RDP.domain;

public class PointImpl implements IPoint{
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
