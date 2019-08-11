import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Readerv2 {

	private static final int NO_OF_READINGS = 100;
	
	private static final int FRONT_SAFE_DISTANCE = 20;
	private static final int SIDE_SAFE_DISTANCE = 15;
	private static final int STEP = 10;
	
	private static int[] angleArr = new int[] {
		Sparki.SERVO_LEFT,
		(Sparki.SERVO_LEFT + Sparki.SERVO_CENTER) / 2,
		Sparki.SERVO_CENTER,
		(Sparki.SERVO_RIGHT + Sparki.SERVO_CENTER) / 2,
		Sparki.SERVO_RIGHT
	};
	
	private Sparki sparki;
	private Random r;
	
	public Readerv2() {
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
			
			int[] pingArr = new int[angleArr.length];

			for(int j = 0; j < angleArr.length; j++) {
				sparki.servo(angleArr[j]);
				sparki.delay(200);
				pingArr[j] = sparki.ping();
			}
			
			sparki.servo(Sparki.SERVO_CENTER);
			
			System.out.println("  ping = " + Arrays.toString(pingArr));

			int minLeft = minimum(pingArr[0], pingArr[1]);
			int minFront = minimum(pingArr[1], pingArr[2], pingArr[3]);
			int minRight = minimum(pingArr[3], pingArr[4]);
			
			if(minLeft <= SIDE_SAFE_DISTANCE) {
//				System.out.println("Possible obstace on left. Moving right by 90.");
				sparki.moveRight(90);
			} else if(minRight <= SIDE_SAFE_DISTANCE) {
//				System.out.println("Possible obstace on right. Moving left by 90.");
				sparki.moveLeft(90);
			} else if(minFront >= FRONT_SAFE_DISTANCE) {
				sparki.moveForward(STEP);
			} else {
				int maxPing;
				int maxPingAngle;
				if(minLeft > minRight) {
					maxPing = minLeft;
					maxPingAngle = angleArr[0];
				} else {
					maxPing = minRight;
					maxPingAngle = angleArr[4];
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
	
	private int minimum(int i, int j) {
		return i > j ? j : i;
	}

	private int minimum(int i, int j, int k) {
		int min = minimum(i, j);
		return minimum(min, k);
	}

	public static void main(String[] args) throws IOException {
		Readerv2 reader = new Readerv2();
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
