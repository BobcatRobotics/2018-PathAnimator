package org.usfirst.frc.team177.lib;

//import edu.wpi.first.wpilibj.Timer;

public class SpeedRecord {
	public static final int EOF = 9999; // 8.3 minutes (if recording 20/second)
	// object keys
	private int id;
	private double totalTime = 0.0;
	private double deltaTime = 0.0;

	// speed characteristics
	private double leftPower = 0.0;
	private double rightPower = 0.0;
	private double leftDistance = 0.0;
	private double rightDistance = 0.0;
	private double leftVelocity = 0.0;
	private double rightVelocity = 0.0;

	public SpeedRecord() {
	}

	public void setSpeedKeys(int id, double startTime, double previousSpeedTime) {
		this.id = id;
		//deltaTime = Timer.getFPGATimestamp() - previousSpeedTime;
		//totalTime = Timer.getFPGATimestamp() - startTime;
		deltaTime = System.currentTimeMillis() - previousSpeedTime;
		totalTime = System.currentTimeMillis() - startTime;
	}

	public void setReadKeys(String speedObjectReadable) {
		String[] result = speedObjectReadable.split("\\s+");
		id = new Integer(result[0]);
		totalTime = new Double(result[1]);
		deltaTime = new Double(result[2]);
	}

	public int getID() {
		return id;
	}
	
	// Check with FPGA recorded files 
	public double getElapsedTime(boolean inMilliSeconds) {
		double tot = totalTime;
		if (inMilliSeconds) {
			tot *= 1000.0;
		}
		return tot;
	}
	
	public double getDeltaTime(boolean inMilliSeconds) {
		double tot = deltaTime;
		if (inMilliSeconds) {
			tot *= 1000.0;
		}
		return tot;	}
	
	public void setPower(double leftPower, double rightPower) {
		this.leftPower = leftPower;
		this.rightPower = rightPower;
	}

	public double[] getPower() {
		double[] row = new double[2];
		row[0] = leftPower;
		row[1] = rightPower;
		return row;
	}

	public void setDistance(double leftDistance, double rightDistance) {
		this.leftDistance = leftDistance;
		this.rightDistance = rightDistance;
	}

	public double[] getDistance() {
		double[] row = new double[2];
		row[0] = leftDistance;
		row[1] = rightDistance;
		// If distances are negative it means encoders are reversed
		if (row[0] < 0.0)
			row[0] *= -1.0;
		if (row[1] < 0.0)
			row[1] *= -1.0;
		return row;
	}

	public void setVelocity(double leftVelocity, double rightVelocity) {
		this.leftVelocity = leftVelocity;
		this.rightVelocity = rightVelocity;
	}

	public double[] getVelocity() {
		double[] row = new double[2];
		row[0] = leftVelocity;
		row[1] = rightVelocity;
		return row;
	}

	public SpeedRecord endOfFile() {
		id = EOF;
		totalTime = 9999999.0; //System.currentTimeMillis  (millis)
		//totalTime = 9999.0; //Timer.getFPGATimestamp()  (seconds)  
		deltaTime = 9999.0;
		leftPower = 999.0;
		rightPower = 999.0;
		leftDistance = 9999.0;
		rightDistance = 9999.0;
		leftVelocity = 999.0;
		rightVelocity = 999.0;
		return this;
	}

	@Override
	public String toString() {   // total time in second
		String formatter =  "%04d %10.5f %8.5f %8.5f %8.5f %8.5f %8.5f %8.5f %8.5f";
		// double totTime = totalTime // Timer.getFPGAtimestamp()
		double totTime = totalTime / 1000.0; // System.currentTimeMillis
		String fmt = String.format(formatter,id,totTime,deltaTime,leftPower,rightPower,leftDistance,rightDistance,leftVelocity,rightVelocity);
		return fmt;
	}
}
