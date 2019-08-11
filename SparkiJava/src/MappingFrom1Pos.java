import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;


public class MappingFrom1Pos {

	private static final int MAX_X = 200;
	private static final int MAX_Y = 200;
	private static final int MAX_RANGE = 30;
	private static final int BLOCK_SIZE = 5;

	private static final int NO_OF_READINGS = 30;
	private static final int SAFE_DISTANCE = 15;
	private static final int STEP = 10;
	
	private Sparki sparki;
	
	private double curX;
	private double curY;
	private double curTheta;
	
	private double[][] map;
	
	public MappingFrom1Pos() {
		sparki = new Sparki("COM8");
		boolean connected = sparki.connect();
		if(!connected) {
			System.out.println("NOT Connected");
		}
		
		curX = MAX_X * BLOCK_SIZE / 2.0;
		curY = MAX_Y * BLOCK_SIZE / 2.0;
		curTheta = 90;
		
		map = new double[MAX_Y][MAX_X];
	}
	
	public void setup() {
		sparki.servo(0);
		System.out.println("Start Calculations:");
		for(int i = 0; i < MAX_X; i++) {
			for(int j = 0; j < MAX_Y; j++) {
				map[j][i] = 0.5;
			}
		}

		for(int j = 0; j < 8; j++) {
			int relAngle = (int) Math.round(j * 25.71 - 90);
			sparki.servo(relAngle);
			sparki.delay(200);
			int ping = sparki.ping();
			System.out.println("Angle " + relAngle + ":   ping=" + ping);
			int absAngle = (int) normalizeAngle(curTheta + relAngle);
			updatePing(absAngle, ping);
		}
		sparki.servo(Sparki.SERVO_CENTER);
		
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileOutputStream(new File("Output.txt")), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(int i = MAX_X - 1; i >= 0; i--) {
			for(int j = 0; j < MAX_Y; j++) {
				pw.print(String.format("%.2f", map[j][i]) + " ");
			}
			pw.println(";");
		}
		pw.close();
	}
	
	private double normalizeAngle(double theta) {
		if(theta > 180) {
			theta -= 360;
		} else if(theta < -180) {
			theta += 360;
		}
		return theta;
	}
	
	private void updatePing(double curTheta, int ping) {
		for(int i = 0; i < 5; i++) {
			double newTheta = curTheta + i * 6.5 - 13;

			double newX = curX + Math.cos(Math.toRadians(newTheta)) * ping;
			double newY = curY + Math.sin(Math.toRadians(newTheta)) * ping;

			int mapX = (int) (newX / BLOCK_SIZE);
			int mapY = (int) (newY / BLOCK_SIZE);

			System.out.println("Updating: X=" + newX + " Y=" + newY + " mapX=" + mapX + " mapY=" + mapY);

			int tempPing = ping;

			if(tempPing < MAX_RANGE) {
				double odds = map[mapX][mapY] / (1 - map[mapX][mapY]);
				odds *= 1.2;
				map[mapX][mapY] = odds / (1 + odds);
				tempPing -= 5;
			} else {
				tempPing = MAX_RANGE;
			}

			while(tempPing > 0) {
				newX = curX + Math.cos(Math.toRadians(newTheta)) * tempPing;
				newY = curY + Math.sin(Math.toRadians(newTheta)) * tempPing;

				mapX = (int) (newX / BLOCK_SIZE);
				mapY = (int) (newY / BLOCK_SIZE);

				double odds = map[mapX][mapY] / (1 - map[mapX][mapY]);
				odds *= 0.8;
				map[mapX][mapY] = odds / (1 + odds);
				
				tempPing -= 5;
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		sparki.disconnect();
	}
	
	public static void main(String[] args) {
		MappingFrom1Pos s = new MappingFrom1Pos();
		s.setup();
	}

}
