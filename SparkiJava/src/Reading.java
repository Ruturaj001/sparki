import java.io.Serializable;
import java.util.Arrays;


public class Reading implements Serializable{

	private double x;
	private double y;
	private double curTheta;
	
	private int[] ping;
	
//	public static final int[] angles = {};
	
	public Reading(double x, double y, double curTheta, int[] ping) {
		this.x = x;
		this.y = y;
		this.curTheta = curTheta;
		this.ping = ping;
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + curTheta + "): " + Arrays.toString(ping);
	}
}
