package org.ai.intelligent_delivery_drone_planner.ai_algorithms;
import javafx.application.Platform;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.perceptron.ExcelReader;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.perceptron.ModelTester;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.perceptron.Perceptron;
import org.ai.intelligent_delivery_drone_planner.ai_algorithms.perceptron.WeatherData;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;
import org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI;
import org.ai.intelligent_delivery_drone_planner.main_gui.builders.TablesBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.ai.intelligent_delivery_drone_planner.main_gui.MainGUI.*;
import static org.ai.intelligent_delivery_drone_planner.main_gui.builders.UIPanelBuilder.*;

public class AiAlgorithm {
	private static Perceptron perceptronModel;
	private static List<Integer> optimizedRoute;
	static double initialCost = 0.0;
	static double optimizedCost = 0.0;
	static List<Integer> initialRoute;
	
	public static void train() {
		MainGUI.tracking.clear();
		MainGUI.tracking.add("🚀 [A1] Initializing AI-driven optimization process...");
		
		String filePath = "assets/weather_data_linearly_separable.xlsx";
		List<WeatherData> weatherData = ExcelReader.readExcel(filePath);
		MainGUI.tracking.add("📊 [A1] Weather dataset loaded: " + weatherData.size() + " rows");
		
		perceptronModel = new Perceptron(3, 0.09, 0.00001, 1000);
		perceptronModel.train(weatherData);
		perceptronModel.logWeights();
		
		ModelTester.testModel(perceptronModel, "assets/weather_data_test.xlsx");
		MainGUI.tracking.add("🔁 [A2/A3] Updating safety status for all cities...");
		TablesBuilder.updateTrackingTable();
	}
	
	public static void predict() {
		updateCitySafetyStatus();
	}
	
	public static void optimize() {
		isOptimized = true;
		if(getCities().isEmpty()) return;
		setOptimizedCities(getCities().stream()
				.map(city -> new City(
						city.getName(),
						city.getX(),
						city.getY(),
						city.getTemp(),
						city.getHumidity(),
						city.getWindSpeed(),
						city.getSafeToFlyValue()))
				.toList());
		
		SimulatedAnnealing sa = getSimulatedAnnealing();
		getInitialRoute();
		initialCost = sa.calculateTotalCost(initialRoute);
		
		MainGUI.tracking.add(" ");
		MainGUI.tracking.add(String.format("📌 [A4] Initial route cost: %.2f", initialCost));
		
		optimizedRoute = sa.optimize();
		Platform.runLater(() -> {
			initialCostLabel.setText(String.format("⏵  Initial Cost: %.2f", initialCost));
			optimizedCostLabel.setText(String.format("Optimized Cost: %.2f  ", optimizedCost));
		});
		prepareA5Outputs();
		MainGUI.tracking.add("✅ [Done] AI model training and route optimization completed successfully.");
	}
	
	private static SimulatedAnnealing getSimulatedAnnealing() {
		double initialTemp;
		double minimumTemp;
		double coolingRate;
		try {
			initialTemp = Double.parseDouble(initTempField.getText().trim());
			if(coolingRateField.getText().trim().equalsIgnoreCase(AROUND_ONE)) {
				coolingRate = AROUND_ONE_VALUE;
			} else {
				coolingRate = Double.parseDouble(coolingRateField.getText().trim());
				if(coolingRate == 1 || coolingRate > AROUND_ONE_VALUE) {
					coolingRate = AROUND_ONE_VALUE;
				}
			} if(minTempField.getText().trim().equalsIgnoreCase(AROUND_ZERO)) {
				minimumTemp = AROUND_ZERO_VALUE;
			} else {
				minimumTemp = Double.parseDouble(minTempField.getText().trim());
				if(minimumTemp == 0 || minimumTemp < AROUND_ZERO_VALUE) {
					minimumTemp = AROUND_ZERO_VALUE;
				}
			}
		} catch (NumberFormatException e) {
			initialTemp = 1400;
			minimumTemp = AROUND_ZERO_VALUE;
			coolingRate = AROUND_ONE_VALUE;
		}
		return new SimulatedAnnealing(getOptimizedCities(), initialTemp, coolingRate, minimumTemp);
	}
	
	private static void updateCitySafetyStatus() {
		for (City city : getCities()) {
			double[] features = new double[] {
					city.getTemp(),
					city.getHumidity(),
					city.getWindSpeed()
			};
			int prediction = perceptronModel.predict(features);
			city.setSafeToFly(prediction);
		}
	}
	
	private static List<Integer> generateRandomRoute(List<City> cities) {
		int n = cities.size();
		List<Integer> route = new ArrayList<>();
		Random rand = new Random();
		int start = rand.nextInt(n);
		
		route.add(start);
		List<Integer> rest = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			if (i != start) {
				rest.add(i);
			}
		}
		
		Collections.shuffle(rest, rand);
		route.addAll(rest);
		route.add(start);
		return route;
	}
	
	private static void prepareA5Outputs() {
		MainGUI.tracking.add(" ");
		MainGUI.tracking.add("📦 [A5] Optimization Summary:");
		MainGUI.tracking.add(" ┣ Initial route: " + initialRoute);
		MainGUI.tracking.add(" ┣ Optimized route: " + optimizedRoute);
		MainGUI.tracking.add(String.format(" ┣ Initial cost: %.2f", initialCost));
		MainGUI.tracking.add(String.format(" ┗ Optimized cost: %.2f", optimizedCost));
		
		StringBuilder statusBuilder = new StringBuilder("📋 [A5] City Safety Status:\n");
		List<City> cities = getCities();
		for (int i = 0; i < cities.size(); i++) {
			City city = cities.get(i);
			statusBuilder.append("    City ").append(i).append(": ")
					.append(city.isSafeToFly() ? "Safe ✅" : "Unsafe ❌")
					.append("\n");
		}
		statusBuilder.append("\n");
		MainGUI.tracking.add(statusBuilder.toString());
	}
	
	public static int[] getInitialRoute() {
		if(getCities().isEmpty()) {
			return null;
		} if (isCitiesEdited) {
			isCitiesEdited = false;
			initialRoute = generateRandomRoute(getCities());
		} else if (initialRoute == null || initialRoute.isEmpty()) {
			initialRoute = generateRandomRoute(getCities());
		}
		return initialRoute.stream().mapToInt(Integer::intValue).toArray();
	}
	
	public static int[] getOptimizedRoute() {
		return optimizedRoute.stream().mapToInt(Integer::intValue).toArray();
	}
}
