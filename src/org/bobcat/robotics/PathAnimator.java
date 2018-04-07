package org.bobcat.robotics;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.usfirst.frc.team177.lib.RioLogger;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PathAnimator extends Application {
	private static String fileName = "center2left.speeds.txt";
	//private final static String fileName = "right2scale.speeds.txt";
	// private final static String fileName = "left2scale.speeds.txt";
	// private final static String fileName = "left2scale_short_switch.speeds.txt";
	// private final static String fileName = "leftright2scaleleft.speeds.txt";
	private final RioFileManager rioFile = new RioFileManager(fileName);
	private final FileChooser fileChooser = new FileChooser();
	private final Desktop desktop = Desktop.getDesktop();
	private Stage primaryStage;
	private BorderPane rootLayout;
	private Label mnuDirection = new Label();
	private boolean direction = true;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Path Animator - " + fileName);

		initRootLayout();
		animate();

		// try {
		// BorderPane root = new BorderPane();
		// Scene scene = new Scene(root, 400, 400);
		// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		// primaryStage.setScene(scene);
		// primaryStage.show();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public void animate() {
		List<RobotPoint> rpath = rioFile.getChartRobotPathData();

		// Draw the robot
		Rectangle robot = new Rectangle();
		robot.setX(0);
		robot.setY(0);
		robot.setWidth(63);
		robot.setHeight(55);
		robot.setFill(Color.MAROON);
		Image img = null;
		if (direction) {
			img = new Image("org/bobcat/robotics/robotimg.jpg", false);
		} else {
			img = new Image("org/bobcat/robotics/robotimgbackwards.jpg", false);
		}
		robot.setFill(new ImagePattern(img));

		double xPixelsPerInch = 527.0 / 324.0;
		double yPixelsPerInch = 527.0 / 324.0;
		RobotPoint start = determineStartingPoint(fileName);
		double xStart = start.x;
		double yStart = start.y;
		// xStart = 0.0;
		// yStart = 0.0;
		// //Creating a Path
		Path path = new Path();

		// Moving to the starting point
		MoveTo moveTo = new MoveTo(xStart, yStart);
		path.getElements().add(moveTo);
		// LineTo line = new LineTo(xStart,yStart);
		// path.getElements().add(line);
		// path.getElements().add(line);
		// path.getElements().add(line);

		// for (int x = 0; x <= 324; x+=10) {
		// //LineTo line = new LineTo((xStart+x),(yStart+x));
		// LineTo line = new LineTo((xStart+x) * xPixelsPerInch,(yStart+x) *
		// yPixelsPerInch);
		// path.getElements().add(line);
		// }

		for (RobotPoint rp : rpath) {
			LineTo line = new LineTo(xStart + (rp.y * xPixelsPerInch), yStart + (rp.x * yPixelsPerInch));
			// LineTo line = new LineTo((xStart+rp.y),(yStart+rp.x));
			path.getElements().add(line);
		}

		// Creating the path transition
		PathTransition pathTransition = new PathTransition();
		pathTransition.setDuration(Duration.millis(20 * rioFile.getTotalRecords()));
		//pathTransition.setDuration(Duration.millis(20 * 58));
		pathTransition.setNode(robot);
		pathTransition.setPath(path);
		pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
		// pathTransition.setOrientation(OrientationType.NONE);
		pathTransition.setCycleCount(1);
		pathTransition.setAutoReverse(false);
		pathTransition.play();

		// Creating a Group object
		Group root = new Group(rootLayout, robot);

		// Pane p = new HBox();
		rootLayout.setPadding(new javafx.geometry.Insets(5, 5, 5, 5));
		// Image img = new Image("robotfield.jpg"));
		Image imgBackground = new Image("org/bobcat/robotics/robotfield.jpg", true);
		// Set your background!
		rootLayout.setBackground(new Background(new BackgroundImage(imgBackground, null, null, null,
				null)));/* , Background.REPEAT, NO_REPEAT, CENTER, DEFAULT))); */

		// Creating a scene object
		Scene scene = new Scene(root/* ,609, 585 */);
		// scene.
		// scene.set

		// Setting title to the Stage
		primaryStage.setTitle("Robot Path - "+ fileName );

		// Adding scene to the stage
		primaryStage.setScene(scene);

		// Displaying the contents of the stage
		primaryStage.show();
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

			// Add Menu
			MenuBar menuBar = new MenuBar();
			menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
			rootLayout.setTop(menuBar);

			// File menu - open, exit
			Menu fileMenu = new Menu("File");
			MenuItem newMenuItem = new MenuItem("Open");
			newMenuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent e) {
					fileChooser.setInitialDirectory(new File("c:\\home\\lvuser"));
					File file = fileChooser.showOpenDialog(primaryStage);
					if (file != null) {
						String newFileName = file.getName(); /* openFile(file); */
						rioFile.setFileName(newFileName);
						fileName = newFileName;
						animate();
					}
				}
			});
			MenuItem exitMenuItem = new MenuItem("Exit");
			exitMenuItem.setOnAction(actionEvent -> Platform.exit());
	
			fileMenu.getItems().addAll(newMenuItem, new SeparatorMenuItem(), exitMenuItem);
			
			// Action menu - restart, direction
			Menu actionMenu = new Menu("Action");
			MenuItem restartItem = new MenuItem("Restart");
			restartItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
					public void handle(final ActionEvent e) {
						animate();
					}
			});
			CheckMenuItem directionMenuItem = new CheckMenuItem("Robot Forward");
			directionMenuItem.setSelected(true);
			directionMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					direction = newValue;
					rioFile.setDirection(direction);
					mnuDirection.setVisible(newValue);
				}
			});
			actionMenu.getItems().addAll(restartItem,directionMenuItem);
			
			menuBar.getMenus().addAll(fileMenu,actionMenu);

			// Show the scene containing the root layout.
			// Scene scene = new Scene(rootLayout);
			// primaryStage.setScene(scene);
			// primaryStage.show();
		} catch (IOException e) {
			RioLogger.errorLog("Error initRootLayout() " + e);
			e.printStackTrace();
		}
	}

//	private String openFile(File file) {
//		String newFileName = fileName;
//		try {
//			desktop.open(file);
//			newFileName = file.getName();
//		} catch (IOException ex) {
//			RioLogger.errorLog("Error openFile() " + ex);
//			ex.printStackTrace();
//		}
//		return newFileName;
//	}

	private RobotPoint determineStartingPoint(String filename) {
		RobotPoint rp = new RobotPoint();
		if (filename.startsWith("center")) {
			rp.x = 75.0;
			rp.y = 300.0;
		}
		if (filename.startsWith("left")) {
			rp.x = 75.0;
			rp.y = 105.0;
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
