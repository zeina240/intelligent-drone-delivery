package org.ai.intelligent_delivery_drone_planner.main_gui.popups;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import javafx.scene.canvas.Canvas;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm;
import org.ai.intelligent_delivery_drone_planner.main_gui.managers.CanvasManager;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;
import org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI;

import java.util.List;

import static org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm.getInitialRoute;
import static org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm.getOptimizedRoute;
import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.costMatrix;
import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.isOptimized;

public class CityPopup {
	private final Canvas canvas;
	private final BorderPane root;
	private final CanvasManager canvasManager;
	private final City city;
	private final List<City> cities;
	private static Popup popup;
	
	public CityPopup(BorderPane root, Canvas canvas, CanvasManager canvasManager, List<City> cities, City city) {
		this.root = root;
		this.canvas = canvas;
		this.canvasManager = canvasManager;
		this.city = city;
		this.cities = cities;
	}
	
	public void show() {
		if(popup != null && popup.isShowing()) {
			popup.hide();
		}
		
		GridPane grid = new GridPane();
		grid.getStyleClass().add("popup-box");
		grid.setVgap(8);
		grid.setHgap(6);
		
		Label label = new Label("🏙 City Name: " + city.getName());
		grid.add(label, 0, 0);
		grid.add(new Label("Status: " + switch (city.getSafeToFlyValue()) {
			case 0 -> "✅ Safe to fly";
			case 1 -> "❌ Unsafe to fly";
			default -> "Undetermined";
		}), 0, 1, 2, 1);
		
		TextField xField = new TextField(String.format("%.1f", city.getX()));
		TextField yField = new TextField(String.format("%.1f", city.getY()));
		TextField tempField = new TextField(String.format("%.1f", city.getTemp()));
		TextField humidityField = new TextField(String.format("%.1f", city.getHumidity()));
		TextField windField = new TextField(String.format("%.1f", city.getWindSpeed()));
		
		grid.addRow(3, new Label("\uD83D\uDCCD X Coordinate:"), xField);
		grid.addRow(4, new Label("\uD83D\uDCCD Y Coordinate:"), yField);
		grid.addRow(5, new Label("\uD83C\uDF21 Temp (°C):"), tempField);
		grid.addRow(6, new Label("\uD83D\uDCA7 Humidity (%):"), humidityField);
		grid.addRow(7, new Label("\uD83C\uDF2C Wind (km/h):"), windField);
		
		grid.setHgap(1);
		Button saveBtn = new Button("Save");
		saveBtn.setId("btn-save");
		GridPane.setHalignment(saveBtn, HPos.CENTER);
		grid.add(saveBtn, 0, 8, 1, 1);
		
		Button closeBtn = new Button("Close");
		closeBtn.setId("btn-close");
		GridPane.setHalignment(closeBtn, HPos.CENTER);
		grid.add(closeBtn, 1, 8, 1, 1);
		
		popup = new Popup();
		popup.getContent().add(grid);
		popup.setAutoHide(true);
		
		Bounds bounds = root.localToScreen(root.getBoundsInLocal());
		double x = bounds.getMinX() - 2;
		double y = bounds.getMinY() + 45;
		popup.show(root, x, y);
		
		closeBtn.setOnAction(_ -> popup.hide());
		saveBtn.setOnAction(_ -> {
			try {
				double newX = Double.parseDouble(xField.getText());
				double newY = Double.parseDouble(yField.getText());
				double newTemp = Double.parseDouble(tempField.getText());
				double newHumidity = Double.parseDouble(humidityField.getText());
				double newWind = Double.parseDouble(windField.getText());
				
				if (newX < 15 || newY < 15 || newX > canvas.getWidth() - 15 || newY > canvas.getHeight() - 15) {
					new Alert(Alert.AlertType.WARNING, "City must be at least 15 pixels away from the canvas borders.").showAndWait();
					return;
				}
				
				city.updateData(newX, newY, newTemp, newHumidity, newWind, city.getSafeToFlyValue());
				canvasManager.clearCanvas();
				canvasManager.redrawAllCities(cities);
				popup.hide();
				
				Thread.ofVirtual().start(() -> {
					MainGUI.computeCostMatrix();
					if(isOptimized) {
						AiAlgorithm.optimize();
						canvasManager.drawRoute(cities, getOptimizedRoute(), costMatrix);
					} else {
						canvasManager.drawRoute(cities, getInitialRoute(), costMatrix);
					}
				});
			} catch (NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Please enter valid numeric values.").showAndWait();
			}
		});
	}
}
