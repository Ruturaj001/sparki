import java.util.Arrays;
import java.util.Scanner;

import jssc.SerialPort;
import jssc.SerialPortException;

public class Sparki_backup {

	public static final int SERVO_CENTER = 0;
	public static final int SERVO_LEFT = -90;
	public static final int SERVO_RIGHT = 90;
	
	private static final byte STATUS_OK        = 0;
	private static final byte MOVE_FORWARD     = 1;
	private static final byte MOVE_BACKWARD    = 2;
	private static final byte MOVE_LEFT        = 3;
	private static final byte MOVE_RIGHT       = 4;
	private static final byte SERVO            = 5;
	private static final byte ULTRASONIC_PING  = 6;
	private static final byte DELAY            = 7;
	private static final byte LED              = 8;
	private static final byte GRIPPER          = 9;
	private static final byte MEDIAN_PING      = 10;
	
	private static final int MEDIAN_COUNT = 3;

	private String portName;
	private SerialPort serialPort;

	Sparki_backup(String comPort) {
		portName = comPort;
		serialPort = new SerialPort(portName);
	}

	public boolean connect() {
		System.out.println("Connecting to " + portName);
		try {
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0);
			this.servo(SERVO_CENTER);
		} catch (SerialPortException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void disconnect() {
		System.out.println("Disconnecting " + portName);
		try {
			serialPort.closePort();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * @param distance should be between 0 - 255
	 */
	public void moveForward(int distance) {
		try {
			if(distance < 0 || distance > 255) {
				throw new IllegalArgumentException("Invalid distance: " + distance);
			}

			serialPort.writeByte(MOVE_FORWARD);
			serialPort.writeByte((byte) distance);

			// Check response
			byte[] input = serialPort.readBytes(1);
			if(input[0] != STATUS_OK) {
				System.out.println("ERROR: moveForward(" + distance + ")");
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * 
	 * @param distance should be between 0 - 255
	 */
	public void moveBackward(int distance) {
		if(distance < 0 || distance > 255) {
			throw new IllegalArgumentException("Invalid distance: " + distance);
		}

		try {
			// Send OPCODE and distance
			serialPort.writeByte(MOVE_BACKWARD);
			serialPort.writeByte((byte)distance);

			// Check response
			byte[] input = serialPort.readBytes(1);
			if(input[0] != STATUS_OK) {
				System.out.println("ERROR: moveBackward(" + distance + ")");
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * 
	 * @param angle should be between 0 - 180
	 */
	public void moveLeft(int angle) {
		if(angle < 0 || angle > 180) {
			throw new IllegalArgumentException("Invalid angle: " + angle);
		}

		try {
			// Send OPCODE and angle
			serialPort.writeByte(MOVE_LEFT);
			serialPort.writeByte((byte)angle);
	
			// Check response
			byte[] input = serialPort.readBytes(1);
			if(input[0] != STATUS_OK) {
				System.out.println("ERROR: moveLeft(" + angle + ")");
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * 
	 * @param angle should be between 0 - 180
	 */
	public void moveRight(int angle) {
		if(angle < 0 || angle > 180) {
			throw new IllegalArgumentException("Invalid angle: " + angle);
		}

		try {
			// Send OPCODE and angle
			serialPort.writeByte(MOVE_RIGHT);
			serialPort.writeByte((byte)angle);
	
			// Check response
			byte[] input = serialPort.readBytes(1);
			if(input[0] != STATUS_OK) {
				System.out.println("ERROR: moveRight(" + angle + ")");
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param angle should be between -180 to +180
	 */
	public void turn(int angle) {
		if(angle < 0) {
			moveRight(-1 * angle);
		} else {
			moveLeft(angle);
		}
	}
	
	/**
	 * 
	 * @param angle should be between -90 to +90
	 */
	public void servo(int angle) {
		if(angle < -90 || angle > 90) {
			throw new IllegalArgumentException("Invalid servo angle: " + angle);
		}
		angle += 90;
		
		try {
			// Send OPCODE and angle
			serialPort.writeByte(SERVO);
			serialPort.writeByte((byte)angle);
	
			// Check response
			byte[] input = serialPort.readBytes(1);
			if(input[0] != STATUS_OK) {
				System.out.println("ERROR: servo(" + angle + ")");
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	public int ping() {
		int distance = -1;
		try {
			// Send OPCODE and angle
			serialPort.writeByte(ULTRASONIC_PING);
	
			// Check response
			byte[] input = serialPort.readBytes(2);
			// System.out.println(Arrays.toString(input));
			distance = ((input[0] << 8) & 0xFF00) | (input[1] & 0x00FF);
			if(distance == 0xFFFF) {
				distance = -1;
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		return distance;
	}
	
	public int medianPing() {
		int[] ping = new int[MEDIAN_COUNT];
		for(int i = 0; i < MEDIAN_COUNT; i++) {
			ping[i] = this.ping();
		}
		Arrays.sort(ping);
		if(MEDIAN_COUNT % 2 == 0) {
			return (ping[MEDIAN_COUNT / 2] + ping[MEDIAN_COUNT / 2 - 1]) / 2;
		} else {
			return ping[MEDIAN_COUNT / 2];
		}
	}

	/**
	 * 
	 * 
	 * @param time Delay in milliseconds. Should be between 0 - 25500
	 */
	public void delay(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	/*
		time /= 100;
		if(time < 0 || time > 255) {
			throw new IllegalArgumentException("Invalid distance: " + time);
		}

		try {
			// Send OPCODE and time
			serialPort.writeByte(DELAY);
			serialPort.writeByte((byte)time);

			// Check response
			byte[] input = serialPort.readBytes(1);
			if(input[0] != STATUS_OK) {
				System.out.println("ERROR: delay(" + time + ")");
			}
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	*/}
	
	public static void main(String[] args) {
		Sparki_backup sparki = new Sparki_backup("COM8");
		boolean connected = sparki.connect();

		if(connected) {
			Scanner sc = new Scanner(System.in);
			
			System.out.println("1: Move forward");
			System.out.println("2: Move Right");
			System.out.println("3: Move Left");
			System.out.println("4: Servo");
			System.out.println("5: Delay");
			System.out.println("6: Ping");
			System.out.println("7: Reset Servo");
			System.out.println("8: Get Max Ping");
			
			while(true) {
				
				switch(sc.nextInt()) {
				case 1:
					System.out.println("Moving Forward: 10");
					sparki.moveForward(10);
					break;
					
				case 2:
					System.out.println("Moving Right: 30");
					sparki.moveRight(30);
					break;
					
				case 3:
					System.out.println("Moving Left: 30");
					sparki.moveLeft(30);
					break;
					
				case 4:
					System.out.println("Servo: 13");
					sparki.servo(13);
					break;
					
				case 5:
					System.out.println("Delay: 100");
					sparki.delay(100);
					break;
					
				case 6:
					System.out.print("Ping: ");
					int ping = sparki.ping();
					System.out.println("Obstacle at " + ping);
					break;

				case 7:
					System.out.println("Servo: 0");
					sparki.servo(0);
					break;
					
				case 8:
					int maxPingAngle = 0;
					int maxPing = Integer.MIN_VALUE;
					int[] pingArr = new int[8];
					for(int j = 0; j < 8; j++) {
						int relAngle = (int) Math.round(j * 25.71 - 90);
						sparki.servo(relAngle);
						sparki.delay(400);
						pingArr[j] = sparki.ping();
						if(pingArr[j] > maxPing && pingArr[j] != 255) {
							maxPing = pingArr[j];
							maxPingAngle = relAngle;
						}
					}
					System.out.println(Arrays.toString(pingArr));
					System.out.println("Max ping angle = " + maxPingAngle + " :  " + maxPing);
					sparki.servo(Sparki_backup.SERVO_CENTER);
					break;
					
				case 9:
					System.out.println("Median ping = " + sparki.medianPing());
					break;
				default:
					sparki.disconnect();
					sc.close();
					System.exit(0);
				}
			}

		}
	}

}
