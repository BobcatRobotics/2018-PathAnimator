package org.usfirst.frc.team177.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import edu.wpi.first.wpilibj.DriverStation;
//import edu.wpi.first.wpilibj.Timer;

public class SpeedFile {
	private static String path = File.separator + "home" + File.separator + "lvuser" + File.separator;

	private static List<SpeedRecord> speeds = new ArrayList<SpeedRecord>();
	private static SpeedRecord eof = new SpeedRecord().endOfFile();
	private String fileName;

	private int passCtr = 0;
	private int maxCtr = 0;
	private double speedEntryTime = 0.0;
	private double startTime = 0.0;

	private SpeedFile() {
		//speedEntryTime = Timer.getFPGATimestamp();
		speedEntryTime = System.currentTimeMillis();
	}

	public SpeedFile(String shortName) {
		this();
		// Check filename 
		if (!shortName.contains(File.separator))
			shortName = path + shortName;
		this.fileName = shortName;	
	}

	private void reset() {
		passCtr = 0;
		maxCtr = 0;
		//speedEntryTime = Timer.getFPGATimestamp();
		speedEntryTime = System.currentTimeMillis();
		startTime = speedEntryTime;
	}

	public void startRecording() {
		reset();
		speeds.clear();
	}

	public void stopRecording() {
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				printWriter.println(speedObj.toString());
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.stopRecoring() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
		}
	}

	public void readRecordingFile() {
		reset();
		speeds.clear();
		String sEOF = new Integer(SpeedRecord.EOF).toString();
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			while (sc.hasNextLine()) {
				String row = sc.nextLine();
				String[] result = row.split("\\s+");
				if (sEOF.equals(result[0]))
					break;
				SpeedRecord speedObj = new SpeedRecord();
				speedObj.setReadKeys(row);
				speedObj.setPower(new Double(result[3]), new Double(result[4]));
				speedObj.setDistance(new Double(result[5]), new Double(result[6]));
				speedObj.setVelocity(new Double(result[7]), new Double(result[8]));
				speeds.add(speedObj);
				//RioLogger.debugLog("added record " + passCtr + " " +speedObj.toString());
				passCtr++;
			}
			sc.close();
			maxCtr = passCtr;
			RioLogger.debugLog("maxCtr = " + maxCtr);
			// File was read, now prime the passCounter for reading back each row
			passCtr = 0;
		} catch (FileNotFoundException e) {
			String err = "SpeedFile.readRecoring() error " + e;
			RioLogger.debugLog(err);
		}
	}
	
	public boolean updateRecordingFile(String backupFileName,boolean delete,int fromRec,int toRec) {
		RioLogger.debugLog("fromRec - toRec " + fromRec + ", " + toRec);
		boolean updated = true;
		File source = new File(fileName);
		File dest =  new File(path+backupFileName);
		   try {
			Files.copy(source.toPath(), dest.toPath());
		} catch (IOException e) {
			String err = "SpeedFile.updateRecordingFile() error " + e;
			RioLogger.debugLog(err);
			return false;
		}
		   
		// TODO :: Check delete flag
		// TODO :: Probably want to break this up for add vs delete   
		// Now that the file was backed up
		// For delete figure out total time being deleted, subtract from remaining records.
		double totalTimeFrom = 0.0;
		double totalTimeTo = 0.0;
		boolean deleteZeroRec = (fromRec == 0);
		// Special condition, fromRec = 0
		if (deleteZeroRec) {
			SpeedRecord firstRecord = speeds.get(0);
			totalTimeFrom = firstRecord.getElapsedTime(false);
		} else {
			SpeedRecord fromRecord = speeds.get(fromRec - 1);
			totalTimeFrom = fromRecord.getElapsedTime(false);
		}
		totalTimeTo = speeds.get(toRec).getElapsedTime(false);
		double subtractTime = totalTimeTo - totalTimeFrom;
		int recCtr = 0;
		int newRecCtr = 0;
		boolean hitAdjust = false;
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				// Special condition, fromRec = 0
				if (deleteZeroRec) {
					printWriter.println(speedObj.toString());
				} 
				if (recCtr < fromRec) {
					printWriter.println(speedObj.toString());
					recCtr++;
				} else if (recCtr >= fromRec && recCtr <= toRec) {
					if (!hitAdjust) {
						hitAdjust = true;
						newRecCtr = recCtr;
					}
					recCtr++;
					if (deleteZeroRec) {
						newRecCtr = recCtr;
						deleteZeroRec = false;
					}
				} else {
					if (recCtr > toRec) {
						double newTotalTime = speedObj.getElapsedTime(false) - subtractTime;
						printWriter.println(speedObj.toStringUpdate(newRecCtr, newTotalTime));
						recCtr++;
						newRecCtr++;
					}
				}
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.updateRecordingFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		
		return updated;
	}
	
	

	public SpeedRecord getRawData(int index) {
		SpeedRecord speedObj = eof;
		if (index < maxCtr) {
			speedObj = speeds.get(index);
		}
		return speedObj;
	}
	
	public int getID(int index) {
		SpeedRecord speedObj = speeds.get(index);
		return speedObj.getID();
	}

	public void addSpeed(double leftPower, double rightPower, double leftDistance, double rightDistance,
			double leftVelocity, double rightVelocity) {
		SpeedRecord speedObject = new SpeedRecord();
		speedObject.setSpeedKeys(passCtr, startTime, speedEntryTime);
		speedObject.setPower(leftPower, rightPower);
		speedObject.setDistance(leftDistance, rightDistance);
		speedObject.setVelocity(leftVelocity, rightVelocity);
		speeds.add(speedObject);

		//speedEntryTime = Timer.getFPGATimestamp();
		speedEntryTime = System.currentTimeMillis();
		passCtr++;
		maxCtr = passCtr;
	}

	public int getNbrOfRows() {
		return maxCtr;
	}

	public double getTotalTime() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}	
		return speedObj.getElapsedTime(false);
	}
	// getPower() advances passCtr
	public double[] getPower() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}
		passCtr++;
		return speedObj.getPower();
	}

	public double[] getDistance() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}
		return speedObj.getDistance();
	}

	public double[] getVelocity() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}
		return speedObj.getVelocity();
	}
}
