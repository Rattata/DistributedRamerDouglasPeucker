package siege.RDP.domain;

import java.util.ArrayList;
import java.util.List;

import siege.RDP.messages.RDPWork;

public class Line {
	
	private double dx;
    private double dy;
    private double sxey;
    private double exsy;
    private double length;
        
    public IOrderedPoint start;
    public IOrderedPoint end;
    
    private List<IOrderedPoint> points;
    
    public Line(List<IOrderedPoint> points){
    
    	this.points = new ArrayList<IOrderedPoint>(points);
		start = this.points.get(0);
		end = this.points.get(points.size() - 1);
	
		dx = start.getX() - end.getX();
        dy = start.getY() - end.getY();
        sxey = start.getX() * end.getY();
        exsy = end.getX() * start.getY();
        length = Math.sqrt(dx*dx + dy*dy);
	}
    
    
	public double distance(IOrderedPoint p) {
        return Math.abs(dy * p.getX() - dx * p.getY() + sxey - exsy) / length;
    }
	
	public List<IOrderedPoint> getPoints() {
		return points;
	}
	
	public List<Line> split(int index){
		ArrayList<Line> wrappers = new ArrayList<>();
		Line old = new Line(getPoints().subList(0, index + 1));
		wrappers.add(old);
		Line newer = new Line(getPoints().subList(index, getPoints().size() - 1));
		wrappers.add(newer);
		return wrappers;
	}
}
