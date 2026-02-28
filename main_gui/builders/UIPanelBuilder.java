package org.ai.intelligent_delivery_drone_planner.main_gui.builders;
import javafx.application.Platform;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import org.ai.intelligent_delivery_drone_planner.main_gui.managers.CanvasManager;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;
import org.ai.intelligent_delivery_drone_planner.main_gui.managers.CityManager;
import org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI;

import java.util.List;

import static org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm.*;
import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.*;
import static org.ai.intelligent_delivery_drone_planner.main_gui.builders.TablesBuilder.updateTrackingTable;
import static org.ai.intelligent_delivery_drone_planner.main_gui.managers.CityManager.commonProcess;

public class UIPanelBuilder {
	public static double AROUND_ONE_VALUE = 0.9995;
	public static String AROUND_ONE = "~1";
	public static double AROUND_ZERO_VALUE = 0.00000000000000000000000001;
	public static String AROUND_ZERO = "~0";
	
	private final Canvas canvas;
	private final List<City> cities;
	private final CityManager cityManager;
	private final CanvasManager canvasManager;
	public static TextField initTempField;
	public static TextField minTempField;
	public static TextField coolingRateField;
	
	public UIPanelBuilder(Canvas canvas, CityManager cityManager, CanvasManager canvasManager) {
		this.canvas = canvas;
		this.cities = getCities();
		this.cityManager = cityManager;
		this.canvasManager = canvasManager;
	}
	
	public VBox buildControlPanel() {
		VBox panel = new VBox();
		panel.setId("control-panel");
		
		Label title = new Label("Control Panel");
		title.setId("title-label");
		Label hintsTitle = new Label("Hints");
		hintsTitle.setId("label-hint");
		
		Label hintLabel = new Label("Click on any city node to edit its...");
		hintLabel.setId("label-hint");
		hintLabel.setWrapText(true);
		hintLabel.setTooltip(new Tooltip(hintLabel.getText()));
		hintLabel.setMaxWidth(250);
		
		Label closeToZeroLabel = new Label("Set this values close to 0/1...");
		closeToZeroLabel.setId("label-hint");
		closeToZeroLabel.setTooltip(new Tooltip("Set the Minimum Temp value close to 0 (~0),\nand set the Cooling Rate value close to 1 (~1)\nto get a more accurate result"));
		closeToZeroLabel.setWrapText(true);
		closeToZeroLabel.setMaxWidth(250);
		
		VBox hints = new VBox();
		hints.setSpacing(0);
		hints.getChildren().addAll(hintsTitle, hintLabel, closeToZeroLabel);
		
		GridPane grid = new GridPane();
		grid.setId("control-grid");
		grid.setVgap(10);
		grid.setHgap(10);
		
		Spinner<Integer> citySpinner = new Spinner<>(3, 200, 30);
		citySpinner.setId("spinner-city-count");
		citySpinner.setEditable(true);
		
		TextField xField = new TextField();
		TextField yField = new TextField();
		TextField tempField = new TextField();
		TextField humidityField = new TextField();
		TextField windField = new TextField();
		
		initTempField = new TextField("1400");
		minTempField = new TextField("~0");
		coolingRateField = new TextField("~1");
		
		Button drawBtn = getDrawBtn(citySpinner);
		Button addBtn = getNewCityBtn(xField, yField, tempField, humidityField, windField);
		Button predictBtn = new Button("Predict Safety");
		predictBtn.setOnAction(_ -> {
			AiAlgorithm.predict();
			canvasManager.clearCanvas();
			canvasManager.redrawAllCities(cities);
			Thread.ofVirtual().start(() -> Platform.runLater(TablesBuilder::updateCitiesTable));
		});
		Button startBtn = getOptimizationBtn();
		
		int rowIndex = 0;
		grid.add(new Label("Number of Cities:"), 0, rowIndex);
		grid.add(citySpinner, 1, rowIndex++);
		grid.add(drawBtn, 0, rowIndex++, 2, 1);
		grid.add(new Label("City Coordinates:"), 0, rowIndex++);
		grid.add(new Label("X:"), 0, rowIndex); grid.add(xField, 1, rowIndex++);
		grid.add(new Label("Y:"), 0, rowIndex); grid.add(yField, 1, rowIndex++);
		grid.add(new Label("Weather (per city):"), 0, rowIndex++);
		grid.add(new Label("Temp:"), 0, rowIndex); grid.add(tempField, 1, rowIndex++);
		grid.add(new Label("Humidity:"), 0, rowIndex); grid.add(humidityField, 1, rowIndex++);
		grid.add(new Label("Wind:"), 0, rowIndex); grid.add(windField, 1, rowIndex++);
		grid.add(addBtn, 0, rowIndex++, 2, 1);
		grid.add(predictBtn, 0, rowIndex, 2, 1);
		
		rowIndex += 2;
		grid.add(new Label("SA Parameters:"), 0, rowIndex++);
		grid.add(new Label("Initial Temp:"), 0, rowIndex); grid.add(initTempField, 1, rowIndex++);
		grid.add(new Label("Minimum Temp:"), 0, rowIndex); grid.add(minTempField, 1, rowIndex++);
		grid.add(new Label("Cooling Rate:"), 0, rowIndex); grid.add(coolingRateField, 1, rowIndex++);
		grid.add(startBtn, 0, rowIndex, 2, 1);
		
		GridPane.setHalignment(drawBtn, HPos.CENTER);
		GridPane.setHalignment(addBtn, HPos.CENTER);
		GridPane.setHalignment(predictBtn, HPos.CENTER);
		GridPane.setHalignment(startBtn, HPos.CENTER);
		
		VBox.setVgrow(grid, Priority.ALWAYS);
		panel.getChildren().addAll(title, hints, grid);
		return panel;
	}
	
	private Button getOptimizationBtn() {
		Button startBtn = new Button("Start Optimization");
		startBtn.setOnAction(_ -> Platform.runLater(() -> Thread.ofVirtual().start(() -> {
			AiAlgorithm.optimize();
			commonProcess(canvasManager, cities, costMatrix);
			Thread.ofVirtual().start(() -> Platform.runLater(TablesBuilder::updateCitiesTable));
		})));
		return startBtn;
	}
	
	private Button getDrawBtn(Spinner<Integer> citySpinner) {
		Button drawBtn = new Button("Draw Cities");
		drawBtn.setId("btn-draw");
		drawBtn.setDefaultButton(false);
		drawBtn.setCancelButton(false);
		drawBtn.setFocusTraversable(false);
		drawBtn.setOnAction(_ -> {
			cityManager.drawRandomCities(citySpinner.getValue());
			Thread.ofVirtual().start(() -> Platform.runLater(TablesBuilder::updateCitiesTable));
		});
		
		drawBtn.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
				event.consume();
			}
		});
		return drawBtn;
	}
	
	private Button getNewCityBtn(TextField xField, TextField yField, TextField tempField, TextField humidityField, TextField windField) {
		Button addBtn = new Button("Add City");
		addBtn.setId("btn-new-city");
		addBtn.setDefaultButton(false);
		addBtn.setCancelButton(false);
		addBtn.setFocusTraversable(false);
		addBtn.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
				event.consume();
			}
		});
		
		addBtn.setOnAction(_ -> {
			if (anyInvalid(xField, yField, tempField, humidityField, windField)) {
				new Alert(Alert.AlertType.ERROR, "Please fill in all fields with valid numbers.").showAndWait();
				return;
			}
			
			try {
				double x = Double.parseDouble(xField.getText().trim());
				double y = Double.parseDouble(yField.getText().trim());
				double temp = Double.parseDouble(tempField.getText().trim());
				double humidity = Double.parseDouble(humidityField.getText().trim());
				double wind = Double.parseDouble(windField.getText().trim());
				if (x < 15 || y < 15 || x > canvas.getWidth() - 15 || y > canvas.getHeight() - 15) {
					new Alert(Alert.AlertType.WARNING, "City must be at least 15 pixels from border.").showAndWait();
					return;
				}
				
				String name = "C" + (cities.size());
				City city = new City(name, x, y, temp, humidity, wind, -1);
				cityManager.addCity(city);
				
				xField.clear(); yField.clear(); tempField.clear(); humidityField.clear(); windField.clear();
				Thread.ofVirtual().start(() -> Platform.runLater(TablesBuilder::updateCitiesTable));
				Thread.ofVirtual().start(() -> {
					MainGUI.computeCostMatrix();
					isCitiesEdited = true;
					if(isOptimized) {
						getInitialRoute();
						predict();
						AiAlgorithm.optimize();
						commonProcess(canvasManager, cities, costMatrix);
						initialRouteCanvasManager.drawRoute(cities, getInitialRoute(), costMatrix);
					} else {
						canvasManager.drawRoute(cities, getInitialRoute(), costMatrix);
						initialRouteCanvasManager.drawRoute(cities, getInitialRoute(), costMatrix);
					}
				});
			} catch (NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter valid numeric values.").showAndWait();
			}
		});
		return addBtn;
	}
	
	private boolean anyInvalid(TextField... fields) {
		for (TextField field : fields) {
			String text = field.getText().trim();
			if (text.isEmpty() || !text.matches("-?\\d+(\\.\\d+)?")) return true;
		}
		return false;
	}
}
