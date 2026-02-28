package org.ai.intelligent_delivery_drone_planner.main_gui;

public class City {
	private final String name;
	private double x;
	private double y;
	private double temp;
	private double humidity;
	private double windSpeed;
	private int safeToFly;
	
	public City(String name, double x, double y, double temp, double humidity, double windSpeed, int safeToFly) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.temp = temp;
		this.humidity = humidity;
		this.windSpeed = windSpeed;
		this.safeToFly = safeToFly;
	}
	
	public String getName() { return name; }
	public double getX() { return x; }
	public double getY() { return y; }
	public double getTemp() { return temp; }
	public double getHumidity() { return humidity; }
	public double getWindSpeed() { return windSpeed; }
	public int getSafeToFlyValue() { return safeToFly; }
	public void setSafeToFly(int safeToFly) {
		this.safeToFly = safeToFly;
	}
	
	public boolean isSafeToFly() {
		return safeToFly == 0;
	}
	
	public void updateData(double x, double y, double temp, double humidity, double windSpeed, int safeToFly) {
		this.x = x;
		this.y = y;
		this.temp = temp;
		this.humidity = humidity;
		this.windSpeed = windSpeed;
		this.safeToFly = safeToFly;
	}
	
	public double distanceTo(City other) {
		return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
	}
	
	public boolean containsPoint(double clickX, double clickY) {
		double dx = x - clickX;
		double dy = y - clickY;
		return Math.sqrt(dx * dx + dy * dy) <= 8;
	}
}
