package org.bobcat.robotics;

import java.io.IOException;
import java.util.List;

import org.usfirst.frc.team177.lib.RioLogger;

import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration; 

public class PathAnimator extends Application {
	private final static String fileName = "center2right.speeds.txt";
	//private final static String fileName = "center2left.speeds.txt";
	//private final static String fileName = "left2scale.speeds.txt";
	//private final static String fileName = "left2scale_short_switch.speeds.txt";
	//private final static String fileName = "leftright2scaleleft.speeds.txt";
	private RioFileManager rioFile = new RioFileManager(fileName);
	private Stage primaryStage;
	private BorderPane rootLayout;

	@Override
	public void start(Stage primaryStage) {
		initRootLayout();
		List<RobotPoint> rpath = rioFile.getChartRobotPathData();

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Path Animator");
        
        //Draw the robot 
        Rectangle robot = new Rectangle(); 
        robot.setX(0);
        robot.setY(0);
        robot.setWidth(53);
        robot.setHeight(44);
        
        //Setting the color of the circle 
        robot.setFill(Color.MAROON); 
        
        //Setting the stroke width of the circle 
        robot.setStrokeWidth(20);     
         
        double xPixelsPerInch = 527.0 / 324.0;
        double yPixelsPerInch = 527.0 / 324.0;
        RobotPoint start = determineStartingPoint(fileName);
        double xStart = start.x;
        double yStart = start.y;
//          xStart = 0.0;
//          yStart = 0.0;
//        //Creating a Path 
        Path path = new Path(); 
        
         //Moving to the starting point 
        MoveTo moveTo = new MoveTo(xStart, yStart); 
        path.getElements().add(moveTo); 
//       	LineTo line = new LineTo(xStart,yStart);
//        path.getElements().add(line); 
//        path.getElements().add(line); 
//        path.getElements().add(line); 

//        for (int x = 0; x <= 324; x+=10) {
//        	//LineTo line = new LineTo((xStart+x),(yStart+x));
//        	LineTo line = new LineTo((xStart+x) * xPixelsPerInch,(yStart+x) * yPixelsPerInch);
//        	path.getElements().add(line);
//        }
   
        
        for (RobotPoint rp :rpath) {
        	LineTo line = new LineTo(xStart+(rp.y * xPixelsPerInch),yStart+(rp.x * yPixelsPerInch));
        	//LineTo line = new LineTo((xStart+rp.y),(yStart+rp.x));
        	path.getElements().add(line);
        }
   
        //Creating the path transition 
        PathTransition pathTransition = new PathTransition(); 
        pathTransition.setDuration(Duration.millis(20 * (rioFile.getTotalRecords() - 1)));   
        //pathTransition.setDuration(Duration.millis(75 * 52));     
        
        //pathTransition.setDuration(Duration.millis(1000));       

        pathTransition.setNode(robot); 
        pathTransition.setPath(path); 
        pathTransition.setOrientation(
           PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT); 
//        pathTransition.setOrientation(OrientationType.NONE);
        pathTransition.setCycleCount(2); 
        pathTransition.setAutoReverse(false); 
        pathTransition.play(); 
               
        //Creating a Group object  
        Group root = new Group(rootLayout,robot); 
           
        //Pane p = new HBox();
        rootLayout.setPadding(new javafx.geometry.Insets(5,5,5,5));
        //Image img = new Image("robotfield.jpg"));
        Image img = new Image("org/bobcat/robotics/robotfield.jpg",true);
        //Set your background!
        rootLayout.setBackground(new Background(new BackgroundImage(img, null, null, null, null)));/*, Background.REPEAT, NO_REPEAT, CENTER, DEFAULT)));*/
 
        //Creating a scene object 
        Scene scene = new Scene(root/*,609, 585*/);  
        //scene.
        //scene.set
        
        //Setting title to the Stage 
        primaryStage.setTitle("Robot Path"); 
           
        //Adding scene to the stage 
        primaryStage.setScene(scene); 
           
        //Displaying the contents of the stage 
        primaryStage.show(); 

//		try {
//			BorderPane root = new BorderPane();
//			Scene scene = new Scene(root, 400, 400);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	   /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PathAnimator.class.getResource("Layout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
//            Scene scene = new Scene(rootLayout);
//            primaryStage.setScene(scene);
//            primaryStage.show();
        } catch (IOException e) {
        	RioLogger.errorLog("Error initRootLayout() " + e);
            e.printStackTrace();
        }
    }
    
    private RobotPoint determineStartingPoint(String filename) {
    	RobotPoint rp = new RobotPoint();
    	if (filename.startsWith("center")) {
            rp.x = 75.0;
            rp.y = 300.0;
    	}
       	if (filename.startsWith("left")) {
            rp.x = 75.0;
            rp.y = 90.0;
    	}
      	if (filename.startsWith("right")) {
            rp.x = 75.0;
            rp.y = 465.0;
    	}
    	return rp;
    }
    
	public static void main(String[] args) {
		launch(args);
	}
}
