import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MappingWith180ServoXY {

	private static final int MAX_X = 100;
	private static final int MAX_Y = 100;
	private static final int MAX_RANGE = 30;
	private static final int BLOCK_SIZE = 5;

	private static final int NO_OF_READINGS = 100;
	private static final int FRONT_SAFE_DISTANCE = 20;
	private static final int SIDE_SAFE_DISTANCE = 10;
	private static final int STEP = 10;
	
	private static final double ODD_DECREMENT = 0.8;
	private static final double ODD_INCREMENT = 1.2;

	private Sparki sparki;

	private double curX;
	private double curY;
	private double curTheta;

	private double[][] map;

	private boolean[][] updated; 
	
	private ArrayList<Reading> readings = new ArrayList<Reading>();

	public MappingWith180ServoXY() {
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
		for(int y = 0; y < MAX_Y; y++) {
			for(int x = 0; x < MAX_X; x++) {
				map[y][x] = 0.5;
			}
		}
		//map[(int) (curY/BLOCK_SIZE)][(int) (curX/BLOCK_SIZE)] = 1.0;
		Random r = new Random();

		for(int i = 0; i < NO_OF_READINGS; i++) {
			System.out.print("Reading #" + (i+1));

			updated = new boolean[MAX_Y][MAX_X];
			
			System.out.println("  curX = " + String.format("%.2f",curX) + "  curY = " + String.format("%.2f",curY) + "  curTheta = " + String.format("%.2f",curTheta));
			int[] pingArr = new int[8];
			int[] angleArr = new int[8];
			for(int j = 0; j < 8; j++) {
				int relAngle = (int) Math.round(j * 25.71 - 90);
				angleArr[j] = relAngle;
				sparki.servo(relAngle);
				sparki.delay(200);
				pingArr[j] = sparki.ping();
				/*if(pingArr[j] > maxPing && pingArr[j] != -1) {
					maxPing = pingArr[j];
					maxPingAngle = relAngle;
				}*/
				int absAngle = (int) normalizeAngle(curTheta + relAngle);
				updatePing(absAngle, pingArr[j]);
			}
//			System.out.println(Arrays.toString(angleArr));
//			System.out.println(Arrays.toString(pingArr));
//			System.out.println();
			
			readings.add(new Reading(curX, curY, curTheta, pingArr));
			
			sparki.servo(Sparki.SERVO_CENTER);
			sparki.delay(300);
			int ping = sparki.ping();
//			System.out.println("------------->" + ping);
			boolean possibleLeftObstacle = false;
			boolean possibleRightObstacle = false;
			for(int k = 0; k < 3; k++) {
				if(pingArr[k] < SIDE_SAFE_DISTANCE) {
					possibleLeftObstacle = true;
				}
				if(pingArr[7 - k] < SIDE_SAFE_DISTANCE) {
					possibleRightObstacle = true;
				}
			}
			if(possibleLeftObstacle) {
				sparki.moveRight(90);
				curTheta = normalizeAngle(curTheta - 90);
			} else if(possibleRightObstacle) {
				sparki.moveLeft(90);
				curTheta = normalizeAngle(curTheta + 90);
			} else if(ping >= FRONT_SAFE_DISTANCE && pingArr[3] >= FRONT_SAFE_DISTANCE && pingArr[4] >= FRONT_SAFE_DISTANCE) {
				sparki.moveForward(STEP);
				curX += Math.cos(Math.toRadians(curTheta)) * STEP;
				curY += Math.sin(Math.toRadians(curTheta)) * STEP;
			} else {
				/*int nextTheta = r.nextInt(180);

				boolean turnLeft = r.nextBoolean();
				if(turnLeft) {
					sparki.moveLeft(nextTheta);
					curTheta += nextTheta;
				} else {
					sparki.moveRight(nextTheta);
					curTheta -= nextTheta;
				}*/

				int maxPingAngle = 0;
				int maxPing = -1;
				for(int j = 1; j < 7; j++) {
					int min = minimum(pingArr[j - 1], pingArr[j], pingArr[j + 1]);
					if(maxPing < min) {
						maxPing = min;
						maxPingAngle = angleArr[j];
					}
				}
				if(maxPing>=FRONT_SAFE_DISTANCE){
					System.out.println("Changing to " + maxPingAngle);
					if(maxPingAngle > 0) {
						sparki.moveRight(maxPingAngle);
						curTheta -= maxPingAngle;
					} else {
						sparki.moveLeft(-1 * maxPingAngle);
						curTheta += -1 * maxPingAngle;
					}
				}else{
					int angle = r.nextInt(90);
					boolean left = r.nextBoolean();
					if(left) {
						sparki.moveLeft(90 + angle);
						curTheta += (90 + angle);
					} else {
						sparki.moveRight(90 + angle);
						curTheta -= (90 + angle);
					}
				}
			}

			if(i % 20 == 0) {
				System.out.println("Writing to File");
	
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(new FileOutputStream(new File("Output" + i + ".txt")), true);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				for(int y = 0; y < MAX_Y; y++) {
					for(int x = 0; x < MAX_X; x++) {
						pw.print(String.format("%.2f", map[y][x]) + " ");
					}
					pw.println(";");
				}
				pw.close();
			}
		}
		System.out.println("Saving readings to file");
		ObjectOutputStream oos = null;
		PrintWriter pw = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream("Readings.bin"));
			pw = new PrintWriter(new FileOutputStream("Readings.txt"), true);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			oos.writeObject(readings);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		for(Reading reading : readings) {
			pw.println(reading.toString());
		}
		
		System.out.println("Writing to File");

		pw.close();
		try {
			pw = new PrintWriter(new FileOutputStream(new File("Output.txt")), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(int y = 0; y < MAX_Y; y++) {
			for(int x = 0; x < MAX_X; x++) {
				pw.print(String.format("%.2f", map[y][x]) + " ");
			}
			pw.println(";");
		}
		pw.close();
		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int minimum(int i, int j, int k) {
		if (i<j && i<k)
			return i;
		if(j<k)
			return j;
		return k;
	}

	private void updatePing(double curTheta, int ping) {
//		System.out.println();
		for(int i = 0; i < 5; i++) {
			double newTheta = curTheta + i * 6 - 12;

			double newX = curX + Math.cos(Math.toRadians(newTheta)) * ping;
			double newY = curY + Math.sin(Math.toRadians(newTheta)) * ping;

			int mapX = (int) (newX / BLOCK_SIZE);
			int mapY = (int) (newY / BLOCK_SIZE);

			//System.out.println("Updating for theta=" + newTheta + " : X=" + String.format("%.2f", newX) + " Y=" + String.format("%.2f", newY) + " mapX=" + mapX + " mapY=" + mapY + " ping=" + ping);

			int tempPing = ping;

			if(tempPing < MAX_RANGE) {
				if(!updated[mapY][mapX]) {
					double odds = map[mapY][mapX] / (1 - map[mapY][mapX]);
					odds *= ODD_INCREMENT;
					map[mapY][mapX] = odds / (1 + odds);
					updated[mapY][mapX] = true;
				}
				tempPing -= BLOCK_SIZE;
			} else {
				tempPing = MAX_RANGE/* - 2 * BLOCK_SIZE*/;
			}

			while(tempPing > 0) {
				newX = curX + Math.cos(Math.toRadians(newTheta)) * tempPing;
				newY = curY + Math.sin(Math.toRadians(newTheta)) * tempPing;

				mapX = (int) (newX / BLOCK_SIZE);
				mapY = (int) (newY / BLOCK_SIZE);

				if(!updated[mapY][mapX]) {
					double odds = map[mapY][mapX] / (1 - map[mapY][mapX]);
					odds *= ODD_DECREMENT;
					map[mapY][mapX] = odds / (1 + odds);
					updated[mapY][mapX] = true;
				}
				tempPing -= BLOCK_SIZE;
			}
		}
	}

	private double normalizeAngle(double theta) {
		if(theta > 180) {
			theta -= 360;
		} else if(theta < -180) {
			theta += 360;
		}
		return theta;
	}

	@Override
	protected void finalize() throws Throwable {
		sparki.disconnect();
	}

	public static void main(String[] args) {
		MappingWith180ServoXY s = new MappingWith180ServoXY();
		s.setup();
	}

}
