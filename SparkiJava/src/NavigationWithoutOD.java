import java.util.Random;


public class NavigationWithoutOD {
	
	private Sparki sparki;
	private Random r;
	
	public NavigationWithoutOD() {
		sparki = new Sparki("COM8", 250, 250);
		
		boolean connected = sparki.connect();
		if(!connected) {
			System.exit(1);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		sparki.disconnect();
		super.finalize();
	}
	
	public void go_to(double x, double y) {
		double destTheta = Math.toDegrees(Math.atan2(y - sparki.curY, x - sparki.curX));
		
		int turnBy = (int) (destTheta - sparki.curTheta);
		
		sparki.turn(turnBy);
		
		int dist = (int) Math.sqrt((y - sparki.curY) * (y - sparki.curY) + (x - sparki.curX) * (x - sparki.curX));
		
		sparki.moveForward(dist);
	}
	
	public static void main(String[] args) {
		NavigationWithoutOD navigator = new NavigationWithoutOD();
		Sparki s = navigator.sparki;
		s.moveRight(30);
		s.moveForward(60);
		s.moveLeft(120);
		s.moveForward(60);
		s.moveLeft(120);
		s.moveForward(120);
		s.moveRight(120);
		s.moveForward(60);
		s.moveRight(120);
		s.moveForward(60);
		/*s.moveForward(20);
		s.moveRight(180);
		s.moveRight(180);
		s.moveBackward(15);*/
	}

}
