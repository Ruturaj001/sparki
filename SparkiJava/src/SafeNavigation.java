
public class SafeNavigation {

	private Sparki sparki;

	private double curX;
	private double curY;
	private double curTheta;

	private static final int EPSILON = 1;
	private static final int THRESHOLD = 15;
	private static final int STEP = 10;

	public SafeNavigation() {
		sparki = new Sparki("COM8");
		boolean connected = sparki.connect();
		if(!connected) {
			System.out.println("NOT Connected");
		} else {
			sparki.servo(Sparki.SERVO_CENTER);
		}

		curX = 0;
		curY = 0;
		curTheta = 90;
	}

	public void safeGoTo(double destX, double destY) {
		while(Math.abs(destX - curX) > EPSILON || Math.abs(destY - curY) > EPSILON) {
			turnTowards(destX, destY);

			int ping = sparki.ping();

			if(ping != -1 && ping < THRESHOLD) {
				// Obstacle ahead
				System.out.println("Left");
				sparki.servo(Sparki.SERVO_LEFT);
				System.out.println("Waiting");
				sparki.delay(1000);
				System.out.println("Ping");
				int left = sparki.ping();

				System.out.println("Right");
				sparki.servo(Sparki.SERVO_RIGHT);
				System.out.println("Waiting");
				sparki.delay(1000);
				System.out.println("Ping");
				int right = sparki.ping();

				sparki.servo(Sparki.SERVO_CENTER);
				
				System.out.println("");
				if(left == -1 || (left > right && right != -1)) {
					// Go towards left
					turnBy(90);
				} else {
					// Go towards right
					turnBy(-90);
				}

				goStraight(STEP);
			} else {
				goTowardsDestination(destX, destY);
			}
		}
	}

	public void goTowardsDestination(double destX, double destY) {
		double tempDistance = Math.sqrt((destY - curY) * (destY - curY) + (destX - curX) * (destX - curX));
		
		tempDistance = tempDistance > STEP ? STEP : tempDistance;
		goStraight(tempDistance);
	}

	public void goStraight(double dist) {
		System.out.println("Going straight " + dist + "  curTheta=" + curTheta);
		
		curX += Math.cos(Math.toRadians(curTheta)) * dist;
		curY += Math.sin(Math.toRadians(curTheta)) * dist;
		
		sparki.moveForward((int)Math.round(dist));
	}

	public void turnBy(double turnTheta) {
		if(turnTheta == 0) {
			return;
		}
		turnTheta = normalizeAngle(turnTheta);

		System.out.println("Turning by " + turnTheta);
		
		if(turnTheta > 0) {
			sparki.moveLeft((int)Math.round(turnTheta));
		} else {
			sparki.moveRight(-1 * (int)Math.round(turnTheta));
		}
		
		curTheta = normalizeAngle(curTheta + turnTheta);
	}

	private double normalizeAngle(double theta) {
		if(theta > 180) {
			theta -= 360;
		} else if(theta < -180) {
			theta += 360;
		}
		return theta;
	}

	public void turnTowards(double destX, double destY) {
		System.out.println("curX = " + curX + "  curY = " + curY + "  destX = " + destX + "  destY = " + destY);
		double destTheta = Math.toDegrees(Math.atan2(destY - curY, destX - curX));
		double turnTheta = Math.round(destTheta) - curTheta;

		System.out.println("Turning towards " + destTheta);
		
		turnBy(turnTheta);
	}

	@Override
	protected void finalize() throws Throwable {
		sparki.disconnect();
	}

	public static void main(String[] args) {
		SafeNavigation s = new SafeNavigation();
		s.safeGoTo(0, 600);
	}

}
