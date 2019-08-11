import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Reader {

	private static final int NO_OF_READINGS = 100;
	
	private static final int FRONT_SAFE_DISTANCE = 20;
	private static final int SIDE_SAFE_DISTANCE = 10;
	private static final int STEP = 10;
	
	private static int[] angleArr = new int[8];
	
	static {
		for(int j = 0; j < 8; j++) {
			int relAngle = (int) Math.round(90 - j * 25.71);
			angleArr[j] = relAngle;
		}
		System.out.println(Arrays.toString(angleArr));
	}
	
	private Sparki sparki;
	private Random r;
	
	public Reader() {
		sparki = new Sparki("COM8", 250, 250);
		r = new Random();
		
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
	
	public ArrayList<Reading> read() {
		ArrayList<Reading> readings = new ArrayList<Reading>();
		
		for(int i = 0; i < NO_OF_READINGS; i++) {

			System.out.print("Reading #" + i + " " + sparki.getState());
			
			int[] pingArr = new int[8];

			sparki.servo(angleArr[0]);
			sparki.delay(200);
			for(int j = 0; j < 8; j++) {
				sparki.servo(angleArr[j]);
				sparki.delay(100);
				pingArr[j] = sparki.ping();
			}
			
			System.out.println("  ping = " + Arrays.toString(pingArr));
//			readings.add(new Reading(sparki.curX, sparki.curY, sparki.curTheta, pingArr));
			
			sparki.servo(Sparki.SERVO_CENTER);
			sparki.delay(200);
			int ping = sparki.ping();
			
			int minLeft = minimum(pingArr[0], pingArr[1], pingArr[2]);
			int minRight = minimum(pingArr[5], pingArr[6], pingArr[7]);
			int minFront = minimum(ping, pingArr[3], pingArr[4]);
			
			if(minLeft < SIDE_SAFE_DISTANCE) {
//				System.out.println("Possible obstace on left. Moving right by 90.");
				sparki.moveRight(90);
			} else if(minRight < SIDE_SAFE_DISTANCE) {
//				System.out.println("Possible obstace on right. Moving left by 90.");
				sparki.moveLeft(90);
			} else if(minFront >= FRONT_SAFE_DISTANCE) {
				sparki.moveForward(STEP);
			} else {
//				System.out.print("Possible obstacle ahead. ");
				int maxPingAngle = 0;
				int maxPing = -1;
				for(int j = 1; j < 7; j++) {
					int min = minimum(pingArr[j - 1], pingArr[j], pingArr[j + 1]);
					if(maxPing < min && min != Integer.MAX_VALUE) {
						maxPing = min;
						maxPingAngle = angleArr[j];
					}
				}
				
				if(maxPing >= FRONT_SAFE_DISTANCE){
//					System.out.println(" Turning by " + maxPingAngle);
					sparki.turn(maxPingAngle);
				} else {
					int angle = r.nextInt(90);
					boolean left = r.nextBoolean();
					if(left) {
						sparki.moveLeft(90 + angle);
					} else {
						sparki.moveRight(90 + angle);
					}
				}
			}
		}
		
		return readings;
	}
	
	private int minimum(int num1, int num2, int num3) {
		if(num1 == -1) {
			num1 = Integer.MAX_VALUE;
		}
		if(num2 == -1) {
			num2 = Integer.MAX_VALUE;
		}
		if(num3 == -1) {
			num3 = Integer.MAX_VALUE;
		}
		if(num1 < num2 && num1 < num3)
			return num1;
		if(num2 < num3)
			return num2;
		return num3;
	}
	
	public static void main(String[] args) throws IOException {
		Reader reader = new Reader();
		ArrayList<Reading> readings = reader.read();
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Readings.bin"));
		PrintWriter pw = new PrintWriter(new FileOutputStream("Readings.txt"), true);

		System.out.println("Saving readings to file");
		oos.writeObject(readings);
		
		for(Reading reading : readings) {
			pw.println(reading.toString());
		}
		
		pw.close();
		oos.close();
	}

}
