package org.ai.intelligent_delivery_drone_planner.main_gui.managers;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;
import org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI;
import org.ai.intelligent_delivery_drone_planner.main_gui.popups.CityPopup;

import java.util.List;
import java.util.Random;

import static org.ai.intelligent_delivery_drone_planner.ai_algorithms.AiAlgorithm.*;
import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.*;
import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.isCitiesEdited;
import static org.ai.intelligent_delivery_drone_planner.main_gui.builders.TablesBuilder.updateTrackingTable;

public class CityManager {
	private final Canvas canvas;
	private final BorderPane root;
	private final List<City> cities;
	private final CanvasManager canvasManager;
	private final boolean justInitialRoute;
	
	public CityManager(BorderPane root, Canvas canvas, CanvasManager canvasManager, boolean justInitialRoute) {
		this.root = root;
		this.canvas = canvas;
		this.cities = getCities();
		this.canvasManager = canvasManager;
		this.justInitialRoute = justInitialRoute;
	}
	
	public void drawRandomCities(int count) {
		canvasManager.clearCanvas();
		cities.clear();
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			double x = 25 + random.nextDouble() * (canvas.getWidth() - 51);
			double y = 25 + random.nextDouble() * (canvas.getHeight() - 40);
			double temp = 10 + random.nextDouble() * 30;
			double humidity = 15 + random.nextDouble() * 66;
			double wind = 4 + random.nextDouble() * 34;
			
			String name = "C" + (i);
			City city = new City(name, x, y, temp, humidity, wind, -1);
			cities.add(city);
		}
		
		canvasManager.redrawAllCities(cities);
		initialRouteCanvasManager.redrawAllCities(cities);
		Thread.ofVirtual().start(() -> {
			MainGUI.computeCostMatrix();
			isCitiesEdited = true;
			if(isOptimized && !justInitialRoute) {
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
	}
	
	public void handleCityClick(double x, double y) {
		City city = getCity(x, y);
		if(city != null) new CityPopup(root, canvas, canvasManager, cities, city).show();
	}
	
	public City getCity(double x, double y) {
		for (City city : cities) {
			if (city.containsPoint(x, y)) {
				return city;
			}
		}
		return null;
	}
	
	public void addCity(City city) {
		cities.add(city);
		canvasManager.clearCanvas();
		canvasManager.redrawAllCities(cities);
		initialRouteCanvasManager.clearCanvas();
		initialRouteCanvasManager.redrawAllCities(cities);
	}
	
	public static void commonProcess(CanvasManager canvasManager, List<City> cities, double[][] costMatrix) {
		computeCostMatrix();
		updateTrackingTable();
		canvasManager.clearCanvas();
		canvasManager.redrawAllCities(cities);
		canvasManager.drawRoute(getOptimizedCities(), getOptimizedRoute(), costMatrix);
	}
}
