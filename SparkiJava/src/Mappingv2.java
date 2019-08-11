import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;

public class Mappingv2 {

	private static final int MAX_X = 100;
	private static final int MAX_Y = 100;
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

	public Mappingv2() {
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

		Random r = new Random();

		for(int i = 0; i < NO_OF_READINGS; i++) {
			System.out.println("Reading #" + (i+1));
			
			int ping = sparki.ping();
			updatePing(curTheta, ping);
			if(ping >= SAFE_DISTANCE) {
				sparki.moveForward(STEP);
				curX += Math.cos(Math.toRadians(curTheta)) * STEP;
				curY += Math.sin(Math.toRadians(curTheta)) * STEP;
			} else {
				int nextTheta = r.nextInt(180);

				boolean turnLeft = r.nextBoolean();
				if(turnLeft) {
					sparki.moveLeft(nextTheta);
					curTheta += nextTheta;
				} else {
					sparki.moveRight(nextTheta);
					curTheta -= nextTheta;
				}
			}

		}
		System.out.println("Writing to File");

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
		Mappingv2 s = new Mappingv2();
		s.setup();
	}

}
