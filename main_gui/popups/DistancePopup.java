package org.ai.intelligent_delivery_drone_planner.main_gui.popups;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Popup;
import org.ai.intelligent_delivery_drone_planner.main_gui.City;

public class DistancePopup {
	private static Popup popup;
	
	public static void distance(BorderPane root, City fromCity, City toCity) {
		if(popup != null && popup.isShowing()) {
			popup.hide();
		}
		
		GridPane grid = new GridPane();
		grid.getStyleClass().add("popup-box");
		grid.setVgap(8);
		grid.setHgap(10);
		
		Button closeBtn = new Button("❌");
		closeBtn.setId("btn-close-distance");
		GridPane.setHalignment(closeBtn, HPos.RIGHT);
		grid.add(closeBtn, 2, 0);
		
		Label title = new Label("City Distance Details");
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		grid.add(title, 0, 0, 2, 1);
		
		grid.add(new Label("🏙 From City: " + fromCity.getName()), 0, 1);
		grid.add(new Label("Status: " + switch (fromCity.getSafeToFlyValue()) {
			case 0 -> "✅ Safe to fly";
			case 1 -> "❌ Unsafe to fly";
			default -> "Undetermined";
		}), 0, 2);
		
		grid.addRow(3, new Label("📍 X Coordinate:"), new Label(String.format("%.1f", fromCity.getX())));
		grid.addRow(4, new Label("📍 Y Coordinate:"), new Label(String.format("%.1f", fromCity.getY())));
		grid.addRow(5, new Label("🌡 Temp (°C):"), new Label(String.format("%.1f", fromCity.getTemp())));
		grid.addRow(6, new Label("💧 Humidity (%):"), new Label(String.format("%.1f", fromCity.getHumidity())));
		grid.addRow(7, new Label("🌬 Wind (km/h):"), new Label(String.format("%.1f", fromCity.getWindSpeed())));
		
		Separator sep1 = new Separator();
		Separator sep2 = new Separator();
		sep1.setPrefWidth(300);
		sep2.setPrefWidth(300);
		grid.add(sep1, 0, 8, 3, 1);
		
		grid.add(new Label("🏙 To City: " + toCity.getName()), 0, 9);
		grid.add(new Label("Status: " + switch (toCity.getSafeToFlyValue()) {
			case 0 -> "✅ Safe to fly";
			case 1 -> "❌ Unsafe to fly";
			default -> "Undetermined";
		}), 0, 10);
		
		grid.addRow(11, new Label("📍 X Coordinate:"), new Label(String.format("%.1f", toCity.getX())));
		grid.addRow(12, new Label("📍 Y Coordinate:"), new Label(String.format("%.1f", toCity.getY())));
		grid.addRow(13, new Label("🌡 Temp (°C):"), new Label(String.format("%.1f", toCity.getTemp())));
		grid.addRow(14, new Label("💧 Humidity (%):"), new Label(String.format("%.1f", toCity.getHumidity())));
		grid.addRow(15, new Label("🌬 Wind (km/h):"), new Label(String.format("%.1f", toCity.getWindSpeed())));
		grid.add(sep2, 0, 16, 3, 1);
		
		double dx = toCity.getX() - fromCity.getX();
		double dy = toCity.getY() - fromCity.getY();
		double distance = Math.sqrt(dx * dx + dy * dy);
		double cost = distance;
		if (fromCity.getSafeToFlyValue() == 1 || toCity.getSafeToFlyValue() == 1) {
			cost += 50;
		}
		
		Label distanceLabel = new Label(String.format("📏 Distance: %.2f units", distance));
		distanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		grid.add(distanceLabel, 0, 17, 3, 1);
		
		Label costLabel = new Label(String.format("💰 Cost: %.2f units", cost));
		costLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: darkgreen;");
		grid.add(costLabel, 0, 18, 3, 1);
		
		popup = new Popup();
		popup.getContent().add(grid);
		popup.setAutoHide(true);
		
		Bounds bounds = root.localToScreen(root.getBoundsInLocal());
		double x = bounds.getMinX() - 2;
		double y = bounds.getMinY() + 90;
		popup.show(root, x, y);
		closeBtn.setOnAction(_ -> popup.hide());
	}
}
