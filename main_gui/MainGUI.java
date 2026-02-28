package org.ai.intelligent_delivery_drone_planner.main_gui;
import javafx.animation.PathTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm;
import org.ai.intelligent_delivery_drone_planner.main_gui.builders.TablesBuilder;
import org.ai.intelligent_delivery_drone_planner.main_gui.builders.UIPanelBuilder;
import org.ai.intelligent_delivery_drone_planner.main_gui.managers.CanvasManager;
import org.ai.intelligent_delivery_drone_planner.main_gui.managers.CityManager;
import org.ai.intelligent_delivery_drone_planner.main_gui.popups.CostsPopup;

import java.util.*;

import static org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm.*;
import static org.ai.intelligent_delivery_drone_planner.main_gui.popups.DistancePopup.distance;

public class MainGUI extends Application {
	private static final String ASCII = "73 110 116 101 108 108 105 103 101 110 116 32 68 101 108 105 118 101 114 121 32 68 114 111 110 101 32 80 108 97 110 110 101 114 32 45 32 65 73 32 80 114 111 106 101 99 116";
	public static List<String> tracking = new ArrayList<>();
	private static final List<City> cities = new ArrayList<>();
	private static List<City> optimizedCities = new ArrayList<>();
	public static double[][] costMatrix;
	
	private static City fromCity = null;
	private static City toCity = null;
	
	public static TableView<City> citiesTable;
	public static TableView<String> trackingTable;
	public static CanvasManager initialRouteCanvasManager;
	public static Canvas initialRouteCanvas;
	private static Pane frontOverlay;
	private static Pane backOverlay;
	
	private static PathTransition frontTransition;
	private static PathTransition backTransition;
	private static Label moverFront;
	private static Label moverBack;
	public static Label initialCostLabel;
	public static Label optimizedCostLabel;
	
	private static boolean isDistanceModeOn = false;
	public static boolean isOptimized = false;
	public static boolean isCitiesEdited = false;
	public static boolean isFrontShowing = true;
	
	@Override
	public void start(Stage stage) {
		Thread.ofVirtual().start(AiAlgorithm::train);
		BorderPane root = getRoot(stage);
		Scene scene = new Scene(root, 1250, 770);
		scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/main.css")).toExternalForm());
		stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/icons/logo.png")).toExternalForm()));
		stage.setTitle(asciiToText());
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}
	
	private BorderPane getRoot(Stage stage) {
		BorderPane root = new BorderPane();
		Button flipButton = new Button("\uD83D\uDD04");
		flipButton.setId("flip-map-button");
		Button distanceButton = new Button("\uD83D\uDCCD");
		distanceButton.setId("distance-button");
		Button flyButton = new Button("\uD83D\uDEEB");
		flyButton.setId("fly-button");
		
		HBox bottomRight = getActionButtonsHBox(flyButton, distanceButton, flipButton);
		StackPane.setAlignment(bottomRight, Pos.BOTTOM_RIGHT);
		
		Canvas canvas = new Canvas(800, 550);
		canvas.setId("map-canvas");
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		frontOverlay = new Pane();
		frontOverlay.setPrefSize(canvas.getWidth(), canvas.getHeight());
		canvas.setLayoutX(0);
		canvas.setLayoutY(0);
		frontOverlay.getChildren().add(canvas);
		Node canvasFrontCard = createCanvasCardWithOverlay("\uD83D\uDE80 Optimized Route", frontOverlay);
		
		initialRouteCanvas = new Canvas(800, 550);
		initialRouteCanvas.setId("map-canvas");
		GraphicsContext igc = initialRouteCanvas.getGraphicsContext2D();
		initialRouteCanvasManager = new CanvasManager(initialRouteCanvas, igc);
		
		backOverlay = new Pane();
		backOverlay.setPrefSize(initialRouteCanvas.getWidth(), initialRouteCanvas.getHeight());
		initialRouteCanvas.setLayoutX(0);
		initialRouteCanvas.setLayoutY(0);
		backOverlay.getChildren().add(initialRouteCanvas);
		Node canvasBackCard = createCanvasCardWithOverlay("🗺 Initial Route", backOverlay);
		
		citiesTable = TablesBuilder.buildCityTable();
		trackingTable = TablesBuilder.buildTrackingTable();
		TabPane centerTabs = new TabPane();
		centerTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		
		Tab mapTab = new Tab("🗺 Map");
		Node flippableCard = createFlippableCard(canvasFrontCard, canvasBackCard, flipButton);
		flippableCard.setId("map-flip-card");
		
		StackPane mapFlipStack = new StackPane(flippableCard, bottomRight);
		mapFlipStack.setId("map-flip-stack");
		
		BorderPane cardContainer = new BorderPane();
		cardContainer.setCenter(mapFlipStack);
		cardContainer.setId("map-flip-card");
		mapTab.setContent(cardContainer);
		
		Tab tableTab = new Tab("🏙 Cities Table");
		tableTab.setContent(citiesTable);
		Tab trackingTableTab = new Tab("\uD83D\uDCE6 Tracking History");
		trackingTableTab.setContent(trackingTable);
		centerTabs.getTabs().addAll(mapTab, tableTab, trackingTableTab);
		root.setCenter(centerTabs);
		
		CanvasManager canvasManager = new CanvasManager(canvas, gc);
		CityManager cityManager = new CityManager(root, canvas, canvasManager, false);
		UIPanelBuilder panelBuilder = new UIPanelBuilder(canvas, cityManager, canvasManager);
		addListeners(root, cityManager, canvas, distanceButton);
		root.setLeft(panelBuilder.buildControlPanel());
		createFly(flyButton);
		CostsPopup.showCosts(root, stage);
		return root;
	}
	
	private static HBox getActionButtonsHBox(Button flyButton, Button distanceButton, Button flipButton) {
		Button flyHomeButton = new Button("\uD83D\uDEEC");
		flyHomeButton.setId("fly-button");
		flyHomeButton.setOnAction(_ -> Platform.runLater(() -> {
			if (isFrontShowing) {
				if(frontTransition != null) {
					frontTransition.pause();
				}
				double w = moverFront.getWidth();
				double h = moverFront.getHeight();
				moverFront.setTranslateX((-w/2)+12);
				moverFront.setTranslateY(-h-5);
				moverFront.setRotate(0);
			} else {
				if(backTransition != null) {
					backTransition.pause();
				}
				double w2 = moverBack.getWidth();
				double h2 = moverBack.getHeight();
				moverBack.setTranslateX((-w2/2)+12);
				moverBack.setTranslateY(-h2-5);
				moverBack.setRotate(0);
			}
		}));
		
		HBox topRight = new HBox(10, flyButton, flyHomeButton, distanceButton, flipButton);
		topRight.setAlignment(Pos.BOTTOM_RIGHT);
		topRight.setPadding(new Insets(5));
		topRight.setPickOnBounds(false);
		return topRight;
	}
	
	private Node createFlippableCard(Node front, Node back, Button flipButton) {
		StackPane card = new StackPane(back, front);
		card.setId("map-flip-stack");
		back.setVisible(false);
		flipButton.setOnAction(_ -> {
			Node showing = isFrontShowing ? front : back;
			Node hidden = isFrontShowing ? back : front;
			RotateTransition rotateOut = new RotateTransition(Duration.millis(300), card);
			rotateOut.setAxis(Rotate.Y_AXIS);
			rotateOut.setFromAngle(0);
			rotateOut.setToAngle(90);
			
			RotateTransition rotateIn = new RotateTransition(Duration.millis(300), card);
			rotateIn.setAxis(Rotate.Y_AXIS);
			rotateIn.setFromAngle(-90);
			rotateIn.setToAngle(0);
			rotateOut.setOnFinished(_ -> {
				showing.setVisible(false);
				hidden.setVisible(true);
				rotateIn.play();
				isFrontShowing = !isFrontShowing;
			});
			rotateOut.play();
		});
		return card;
	}
	
	private Node createCanvasCardWithOverlay(String title, Pane overlay) {
		Label titleLabel = new Label(title);
		titleLabel.setId("canvas-card-title");
		
		VBox box = new VBox(titleLabel, overlay);
		box.setId("canvas-card");
		box.setAlignment(Pos.TOP_CENTER);
		box.setSpacing(10);
		box.setPadding(new Insets(10));
		return box;
	}
	
	private void createFly(Button flyButton) {
		moverFront = new Label("✈");
		moverFront.setId("fly-plane-front");
		moverFront.setMouseTransparent(true);
		frontOverlay.getChildren().add(moverFront);
		
		moverBack = new Label("✈");
		moverBack.setId("fly-plane-back");
		moverBack.setMouseTransparent(true);
		backOverlay.getChildren().add(moverBack);
		Platform.runLater(() -> {
			double w = moverFront.getWidth();
			double h = moverFront.getHeight();
			moverFront.setTranslateX((-w/2)+12);
			moverFront.setTranslateY(-h-5);
			double w2 = moverBack.getWidth();
			double h2 = moverBack.getHeight();
			moverBack.setTranslateX((-w2/2)+12);
			moverBack.setTranslateY(-h2-5);
		});
		
		flyButton.setOnAction(_ -> {
			int[] routeIndices = (isOptimized && isFrontShowing) ? getOptimizedRoute() : getInitialRoute();
			if (routeIndices == null) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("No Route Found");
				alert.setContentText("There is no available route to display. Please generate or load a route first.");
				alert.showAndWait();
				return;
			} if (routeIndices.length < 2) {
				Alert alert = new Alert(Alert.AlertType.WARNING);
				alert.setTitle("Warning");
				alert.setHeaderText("Invalid Route");
				alert.setContentText("A route must contain at least two cities.");
				alert.showAndWait();
				return;
			}
			
			Label mover = isFrontShowing ? moverFront : moverBack;
			Path path = new Path();
			City startCity = cities.get(routeIndices[0]);
			path.getElements().add(new MoveTo(startCity.getX(), startCity.getY()));
			
			for (int i = 1; i < routeIndices.length; i++) {
				City c = cities.get(routeIndices[i]);
				path.getElements().add(new LineTo(c.getX(), c.getY()));
			}
			
			if(isFrontShowing) {
				frontTransition = getPathTransition(routeIndices, path, mover);
				frontTransition.play();
			} else {
				backTransition = getPathTransition(routeIndices, path, mover);
				backTransition.play();
			}
		});
	}
	
	private static void addListeners(BorderPane root, CityManager cityManager, Canvas canvas, Button distanceButton) {
		distanceButton.setOnAction(_ -> {
			if(isDistanceModeOn) {
				distanceButton.setText("\uD83D\uDCCD");
				isDistanceModeOn = false;
				fromCity = null;
				toCity = null;
				root.setCursor(Cursor.DEFAULT);
			} else {
				distanceButton.setText("⛔");
				isDistanceModeOn = true;
				fromCity = null;
				toCity = null;
				root.setCursor(Cursor.CROSSHAIR);
			}
		});
		
		canvas.setOnMouseClicked(event -> {
			if(isDistanceModeOn) {
				handleDistanceSelection(root, cityManager, event.getX(), event.getY());
			} else {
				cityManager.handleCityClick(event.getX(), event.getY());
			}
		});
		canvas.setOnMouseMoved(event -> {
			if(!isDistanceModeOn) {
				City city = cityManager.getCity(event.getX(), event.getY());
				if(city != null) {
					root.setCursor(Cursor.HAND);
				} else {
					root.setCursor(Cursor.DEFAULT);
				}
			}
		});
		canvas.setOnMouseExited(_ -> {
			if (!isDistanceModeOn) {
				root.setCursor(Cursor.DEFAULT);
			}
		});
		
		initialRouteCanvas.setOnMouseClicked(event -> {
			if(isDistanceModeOn) {
				handleDistanceSelection(root, cityManager, event.getX(), event.getY());
			} else {
				cityManager.handleCityClick(event.getX(), event.getY());
			}
		});
		initialRouteCanvas.setOnMouseMoved(event -> {
			if (!isDistanceModeOn) {
				City city = cityManager.getCity(event.getX(), event.getY());
				if (city != null) {
					root.setCursor(Cursor.HAND);
				} else {
					root.setCursor(Cursor.DEFAULT);
				}
			}
		});
		initialRouteCanvas.setOnMouseExited(_ -> {
			if (!isDistanceModeOn) {
				root.setCursor(Cursor.DEFAULT);
			}
		});
	}
	
	private static void handleDistanceSelection(BorderPane root, CityManager cityManager, double x, double y) {
		City selectedCity = cityManager.getCity(x, y);
		if (selectedCity == null) {
			return;
		} if (fromCity == null) {
			fromCity = selectedCity;
		} else if (toCity == null) {
			toCity = selectedCity;
			distance(root, fromCity, toCity);
			fromCity = null;
			toCity = null;
		}
	}
	
	private static PathTransition getPathTransition(int[] routeIndices, Path path, Label mover) {
		PathTransition transition = new PathTransition();
		transition.setDuration(Duration.seconds(routeIndices.length));
		transition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
		transition.setPath(path);
		transition.setNode(mover);
		transition.setCycleCount(1);
		transition.setAutoReverse(false);
		return transition;
	}
	
	public static List<City> getCities() {
		return cities;
	}
	
	public static void setOptimizedCities(List<City> optimizedCities) {
		MainGUI.optimizedCities = optimizedCities;
	}
	
	public static List<City> getOptimizedCities() {
		return optimizedCities;
	}
	
	public static void computeCostMatrix() {
		int n = cities.size();
		costMatrix = new double[n][n];
		for (int i = 0; i < n; i++) {
			City cityA = cities.get(i);
			for (int j = 0; j < n; j++) {
				if (i == j) {
					costMatrix[i][j] = 0;
					continue;
				}
				
				City cityB = cities.get(j);
				double dx = cityA.getX() - cityB.getX();
				double dy = cityA.getY() - cityB.getY();
				double distance = Math.sqrt(dx * dx + dy * dy);
				if (cityA.getSafeToFlyValue() == 1 || cityB.getSafeToFlyValue() == 1) {
					distance += 50;
				}
				costMatrix[i][j] = distance;
			}
		}
	}
	
	private String asciiToText() {
		StringBuilder result = new StringBuilder();
		String[] codes = MainGUI.ASCII.split(" ");
		for (String code : codes) {
			int val = Integer.parseInt(code);
			result.append((char) val);
		}
		return result.toString();
	}
}