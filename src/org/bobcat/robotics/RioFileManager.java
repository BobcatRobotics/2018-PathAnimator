package org.bobcat.robotics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.team177.lib.RioLogger;
import org.usfirst.frc.team177.lib.SpeedFile;
import org.usfirst.frc.team177.lib.SpeedRecord;

public class RioFileManager {
	private String fileName = null;
	private int totRecords = 0;

	// Make up vars for constants, and some arrays to hold data from calculations
	private double eps = 0.000001;           // A small number to keep from dividing by zero
	private double xxmax = 648.0;
	private double xxmin = -648.0;
	private double yymax = 648.0;
	private double yymin = -648.0;
	private double Rmax = 1000000.0;
	private double Rmin = -1000000.0;
	private double deltadot;                 // delta between rddotf and lddotf
	private double wb = 26;                  // robot wheel base in inches
	private double xx[] = new double[10000]; // x position of robot in inches
	private double yy[] = new double[10000]; // y position of robot in inches
	private double w[] = new double[10000];  // angualr velocity of robot, calc'd from wheel velocity in rads/sec
	private double theta[] = new double[10000]; // angle of robot forward vector wrt x axis in rads
	private double iccx[] = new double[10000]; // x position of ICC in inches
	private double iccy[] = new double[10000]; // y position of ICC in inches
	private double R[] = new double[10000]; // signed distance from center of drive train to icc
	private double lddotf[] = new double[10000];  // forward diff calc of left vel [v(i) = (d(i+1) - d(i))/dt(i+1)]
	private double rddotf[] = new double[10000];  // forward diff calc of right vel
	
	private RioFileManager() {
		super();
	}

	public  RioFileManager(String fileName) {
		this();
		this.fileName = fileName;
	}
	
	public int getTotalRecords() {
		return totRecords;
	}

	public List<RobotPoint> getChartRobotPathData() {
		// line plot
		// X Axis = Time, Y Axis = [Speed, or Distance, or Velocity]
		List<RobotPoint> path = new ArrayList<RobotPoint>();
	
		SpeedFile sFile = new SpeedFile(fileName);
		sFile.readRecordingFile();
		int nbrRecords = sFile.getNbrOfRows();
		RioLogger.debugLog("nbr Rows = " + nbrRecords);
		int recCtr = 0;
		double [][] distance = new double[nbrRecords][2];
		double [][] velocity = new double[nbrRecords][2];
		double [][] power = new double[nbrRecords][2];
		double [] deltaTime = new double[nbrRecords];
		double [] elapsedTime =  new double[nbrRecords]; // For Testing only 
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			if (sRec.getID() == SpeedRecord.EOF)  {
				break;
			}
			double [] dist = sFile.getDistance();
			double [] vel = sFile.getVelocity();
			double [] pow = sFile.getPower();
			for (int idx=0;idx < 2;idx++) {
				power[recCtr][idx] = pow[idx];
				distance[recCtr][idx] = dist[idx];
				velocity[recCtr][idx]  = vel[idx];
//				if (idx == 0) {
//					RioLogger.debugLog("id distance,velocity " + sRec.getID() + " " + distance[recCtr][0] + ", " + velocity[recCtr][0]);
//				}
			}
			elapsedTime[recCtr] = sRec.getElapsedTime(false);
			deltaTime[recCtr] = sRec.getDeltaTime(false);

			recCtr++;
		} while(true);
		nbrRecords = recCtr;

		double xMin = 0.0;
		double xMax = 0.0;
		double yMin = 0.0;
		double yMax = 0.0;
		//
		// Set the first point by hand, then loop over the rest of the points stopping 1 before the end.
		xx[0] = 0.0;
		yy[0] = 0.0;
		theta[0] = 1.571;
		RobotPoint rp = new RobotPoint(xx[0],yy[0]);
		path.add(rp);
		nbrRecords = 1;
		for (int point = 0; point < recCtr-1; point++) {
//			lddotf[point] = (distance[point+1][0] - distance[point][0])/deltaTime[point+1];
//			rddotf[point] = (distance[point+1][1] - distance[point][1])/deltaTime[point+1];
			lddotf[point] = velocity[point][0];
			rddotf[point] = velocity[point][1];
			deltadot = rddotf[point] - lddotf[point];
			if ((deltadot < eps) && (deltadot > -eps)) {
				R[point] = 1000000.0;    // robot driving straight or stopped
				w[point] = lddotf[point] / 1000000.0;          // and not turning
			} else {
				w[point] = (rddotf[point] - lddotf[point])/wb;
				R[point] = wb/2.0 * (lddotf[point] + rddotf[point])/deltadot; // robot curving
			}
			if (R[point] > Rmax) {R[point]=Rmax;}
			if (R[point] < Rmin) {R[point]=Rmin;}

			iccx[point] = xx[point] - R[point]*Math.sin(theta[point]);
			iccy[point] = yy[point] + R[point]*Math.cos(theta[point]);

			xx[point+1] = Math.cos(w[point]*deltaTime[point+1])*(xx[point] - iccx[point]) - Math.sin(w[point]*deltaTime[point+1])*(yy[point] - iccy[point]) + iccx[point];
			yy[point+1] = Math.sin(w[point]*deltaTime[point+1])*(xx[point] - iccx[point]) + Math.cos(w[point]*deltaTime[point+1])*(yy[point] - iccy[point]) + iccy[point];
			theta[point+1] = theta[point] + w[point]*deltaTime[point+1];
			
			// Keep xx and yy in the 'vicinity' of the field
			if (xx[point+1] < xxmin) {xx[point+1] = xxmin;}
			if (xx[point+1] > xxmax) {xx[point+1] = xxmax;}
			if (yy[point+1] < yymin) {yy[point+1] = yymin;}
			if (yy[point+1] > yymax) {yy[point+1] = yymax;}

			// Print some debugging info
			// if ((xx[point+1] < -25.0) && (xx[point+1] > -35.0)) {
			if (point < 100) {
				RioLogger.debugLog("pass="+point+" lddotf="+lddotf[point]+" rddotf="+rddotf[point]+" theta="+theta[point]+" iccx="+xx[point]+" iccy="+iccy[point]+" xx="+xx[point+1] + " yy="+yy[point+1]);
			}
//			if (point < 50) {
				rp = new RobotPoint(xx[point+1],yy[point+1]);
				path.add(rp);
				nbrRecords++;
				// Just for testing
				if (xx[point+1] < xMin) xMin = xx[point+1];
				if (xx[point+1] > xMax) xMax = xx[point+1];
				if (yy[point+1] < yMin) yMin = yy[point+1];
				if (yy[point+1] > yMax) yMax = yy[point+1];
			   //lineData1.add(yy[point+1],-1.0*xx[point+1]);  // (X,Y)
//			}
		}
		// Create intermediate vars in the last row just for completeness
		lddotf[recCtr] = lddotf[recCtr-1];
		rddotf[recCtr] = rddotf[recCtr-1];
		w[recCtr] = w[recCtr-1];
		R[recCtr] = R[recCtr-1];
		iccx[recCtr] = iccx[recCtr-1];
		iccy[recCtr] = iccy[recCtr-1];
		RioLogger.debugLog(xMin + ", " + xMax + ", " + yMin + ", " + yMax);
		RioLogger.debugLog("tot recs " + nbrRecords);
		totRecords = path.size();
		return path;
	}
}
