package org.ai.intelligent_delivery_drone_planner.main_gui.managers;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;

import java.util.List;

public class CanvasManager {
	private final Canvas canvas;
	private final GraphicsContext gc;
	
	public CanvasManager(Canvas canvas, GraphicsContext gc) {
		this.canvas = canvas;
		this.gc = gc;
	}
	
	public void clearCanvas() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	private void drawCity(City city) {
		Color baseColor = switch (city.getSafeToFlyValue()) {
			case 0 -> Color.rgb(0, 128, 0);
			case 1 -> Color.RED;
			default -> Color.DARKSLATEGRAY;
		};
		
		gc.setFill(baseColor.deriveColor(0, 1, 1, 0.5));
		gc.fillOval(city.getX() - 12, city.getY() - 12, 25, 25);
		gc.setFill(baseColor);
		gc.fillOval(city.getX() - 5, city.getY() - 5, 10, 10);
		gc.setFont(Font.font("System", FontWeight.BOLD, 12));
		gc.fillText(city.getName(), city.getX() + 6, city.getY() - 6);
	}
	
	public void redrawAllCities(List<City> cities) {
		clearCanvas();
		for (City city : cities) {
			drawCity(city);
		}
	}
	
	public void drawRoute(List<City> cities, int[] route, double[][] costMatrix) {
		if (cities == null || cities.size() < 2) return;
		if (route == null || route.length < 2) return;
		
//		gc.setStroke(Color.DARKSLATEGRAY);
//		gc.setLineWidth(2);
//		gc.setFont(Font.font("System", FontWeight.NORMAL, 12));
//		gc.setFill(Color.DARKSLATEGRAY);
//
//		for (int i = 0; i < cities.size() - 1; i++) {
//			City from = cities.get(i);
//			City to = cities.get(i + 1);
//
//			double x1 = from.getX();
//			double y1 = from.getY();
//			double x2 = to.getX();
//			double y2 = to.getY();
//
//			gc.strokeLine(x1, y1, x2, y2);
//			int fromIndex = cities.indexOf(from);
//			int toIndex = cities.indexOf(to);
//			double cost = costMatrix[fromIndex][toIndex];
//
//			double midX = (x1 + x2) / 2;
//			double midY = (y1 + y2) / 2;
//			gc.fillText(String.format("%.1f", cost), midX + 5, midY - 5);
//			drawArrowHead(x1, y1, x2, y2);
//		}
		
		gc.setLineWidth(2);
		gc.setStroke(Color.DARKSLATEGRAY);
		gc.setFont(Font.font("System", FontWeight.BOLD, 12));
		gc.setFill(Color.DARKSLATEGRAY);
		
		for (int i = 0; i < route.length - 1; i++) {
			City from = cities.get(route[i]);
			City to = cities.get(route[i + 1]);
			
			double x1 = from.getX();
			double y1 = from.getY();
			double x2 = to.getX();
			double y2 = to.getY();
			
			gc.strokeLine(x1, y1, x2, y2);
			double cost = costMatrix[route[i]][route[i + 1]];
			double midX = (x1 + x2) / 2;
			double midY = (y1 + y2) / 2;
			gc.fillText(String.format("%.1f", cost), midX + 5, midY - 5);
			drawArrowHead(gc, x1, y1, x2, y2);
		}
	}
	
//	private void drawArrowHead(GraphicsContext gc, double x1, double y1, double x2, double y2) {
//		double arrowLength = 10;
//		double arrowWidth = 7;
//
//		double dx = x2 - x1;
//		double dy = y2 - y1;
//		double angle = Math.atan2(dy, dx);
//		double sin = Math.sin(angle);
//		double cos = Math.cos(angle);
//
//		double xLeft = x2 - arrowLength * cos + arrowWidth * sin;
//		double yLeft = y2 - arrowLength * sin - arrowWidth * cos;
//		double xRight = x2 - arrowLength * cos - arrowWidth * sin;
//		double yRight = y2 - arrowLength * sin + arrowWidth * cos;
//
//		gc.setFill(Color.DARKSLATEGRAY);
//		gc.fillPolygon(new double[]{x2, xLeft, xRight}, new double[]{y2, yLeft, yRight}, 3);
//	}
	
	private void drawArrowHead(GraphicsContext gc, double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		double arrowLength = 10;
		double arrowAngle = Math.toRadians(20);
		
		double xArrow1 = x2 - arrowLength * Math.cos(angle - arrowAngle);
		double yArrow1 = y2 - arrowLength * Math.sin(angle - arrowAngle);
		double xArrow2 = x2 - arrowLength * Math.cos(angle + arrowAngle);
		double yArrow2 = y2 - arrowLength * Math.sin(angle + arrowAngle);
		
		gc.strokeLine(x2, y2, xArrow1, yArrow1);
		gc.strokeLine(x2, y2, xArrow2, yArrow2);
	}
}