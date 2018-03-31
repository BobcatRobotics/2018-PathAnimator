package org.usfirst.frc.team177.lib;

public class TestNoRobot {
	//static final String filename = "right2scale.txt";
	static final String basefilename = "commands.txt";
	static final double NBR_TEST_RECS = 50;

	public static void main(String[] args) throws InterruptedException {
		boolean testCmd = false;
		boolean testSpeed = true;
		log("TestNoRobot. Started testing filename is  - " + basefilename);
		String[] namesplit = basefilename.split("\\.");
		String speedFileName = namesplit[0] + ".speeds." + namesplit[1];
		log("TestNoRobot. speed filename is  - " + speedFileName);
	
		if (testCmd) {
			//recordCommandFile(basefilename);
			//readCommandFile(basefilename);
		}
		if (testSpeed) {
			recordSpeedFile(speedFileName);
			readSpeedFile(speedFileName);
		}
	}


	private static void testRioLogger() throws InterruptedException {
		log("main() called()");
		RioLoggerThread.getInstance();
		log("main RioLoggerThread instance.");

		for (int i = 0; i < NBR_TEST_RECS; i++)
			RioLogger.debugLog("line number is " + i);
		log("sleeping for (seconds) " + 180000 /1000);

		Thread.currentThread();
		Thread.sleep(32000);
		RioLogger.debugLog("done sleeping for 32 seconds.");
		RioLoggerThread.setLoggingParameters(61, 15);
		RioLogger.debugLog("switched to 1 minute every 15 seconds. ");
		
		Thread.sleep(61500);
		RioLogger.debugLog("main thread finished");
		log("main done sleeping");
		RioLoggerThread.stopLogging();
		RioLogger.debugLog("stopLogging() called");
		
		
	}

	private static void recordSpeedFile(String filename) throws InterruptedException {
		log("Recording Speed File -  " + filename);
		SpeedFile sFile = new SpeedFile(filename);
		sFile.startRecording();
		for (int x = 0; x < NBR_TEST_RECS; x++) {
			sFile.addSpeed(new Double(x / NBR_TEST_RECS), new Double(x / NBR_TEST_RECS), x, -x, x+Math.PI, x-Math.PI);
			Thread.currentThread();
			Thread.sleep(20);
			if (x == NBR_TEST_RECS - 1) {
				log("last record");
			}
		}
		sFile.stopRecording();
		log("Finished recording. FileSize = " + sFile.getNbrOfRows());
	}
	
	private static void readSpeedFile(String filename) throws InterruptedException {
		log("Reading Speed File -  " + filename);
		SpeedFile sFile = new SpeedFile(filename);
		sFile.readRecordingFile();
		int recCtr = 0;
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			log(sRec.toString());
			if (SpeedRecord.EOF==sRec.getID()) {
				log("EOF Record found. Rec Nbr  = " + recCtr);
				break;
			}
				
			double [] power = sFile.getPower();
			if (power[0] == 999.0) {
				break;
			}
			log("[" + power[0] + "," + power[1] + "]");
			recCtr++;
		} while(true);
		//sFile.stopRecording();
		log("Finished reading. FileSize = " + sFile.getNbrOfRows());
	}

	private static void log(String text) {
		RioLogger.log(text);
		RioLogger.debugLog(text);
	}
	

}
